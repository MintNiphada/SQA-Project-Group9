package org.apache.commons.math3.optimization.general;

import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.SimpleVectorValueChecker;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static class DummyOptimizer extends AbstractLeastSquaresOptimizer {
        private final boolean callSetUpInOptimize;

        DummyOptimizer() {
            super();
            this.callSetUpInOptimize = true;
        }

        DummyOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
            this.callSetUpInOptimize = true;
        }

        DummyOptimizer(ConvergenceChecker<PointVectorValuePair> checker, boolean callSetUpInOptimize) {
            super(checker);
            this.callSetUpInOptimize = callSetUpInOptimize;
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            if (callSetUpInOptimize) {
                setUp();
                updateResidualsAndCost();
                updateJacobian();
            }
            return new PointVectorValuePair(point, objective);
        }

        public void publicSetUp() {
            setUp();
        }

        public void publicUpdateJacobian() {
            updateJacobian();
        }

        public RealMatrix publicComputeWeightedJacobian(double[] params) {
            return computeWeightedJacobian(params);
        }

        public void publicUpdateResidualsAndCost() {
            updateResidualsAndCost();
        }

        public double publicComputeCost(double[] residuals) {
            return computeCost(residuals);
        }

        public void publicSetCost(double cost) {
            setCost(cost);
        }

        public double[] publicComputeResiduals(double[] objectiveValue) {
            return computeResiduals(objectiveValue);
        }

        public double[][] getWeightedResidualJacobian() {
            return weightedResidualJacobian;
        }

        public double[] getWeightedResiduals() {
            return weightedResiduals;
        }

        public double[] getObjective() {
            return objective;
        }

        public double[] getPoint() {
            return point;
        }

        public void setPoint(double[] point) {
            this.point = point;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }
    }

    private static class LinearProblem implements MultivariateDifferentiableVectorFunction {
        @Override
        public double[] value(double[] point) {
            return new double[] {
                2.0 * point[0] + point[1],
                point[0] - 2.0 * point[1],
                point[0] + point[1]
            };
        }

        @Override
        public DerivativeStructure[] value(DerivativeStructure[] point) {
            return new DerivativeStructure[] {
                point[0].multiply(2.0).add(point[1]),
                point[0].subtract(point[1].multiply(2.0)),
                point[0].add(point[1])
            };
        }
    }

    private static class DifferentiableLinearProblem implements DifferentiableMultivariateVectorFunction {
        @Override
        public double[] value(double[] point) {
            return new double[] {
                2.0 * point[0] + point[1],
                point[0] - 2.0 * point[1],
                point[0] + point[1]
            };
        }

        @Override
        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] point) {
                    return new double[][] {
                        { 2.0, 1.0 },
                        { 1.0, -2.0 },
                        { 1.0, 1.0 }
                    };
                }
            };
        }
    }

    @Test
    public void testOptimizeWithMultivariateDifferentiableFunction() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        double[] target = new double[] { 3.0, -1.0, 2.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        PointVectorValuePair optimum = optimizer.optimize(100, problem, target, weights, startPoint);

        Assert.assertNotNull(optimum);
        Assert.assertEquals(1.0, optimum.getPointRef()[0], 1e-10);
        Assert.assertEquals(1.0, optimum.getPointRef()[1], 1e-10);

        Assert.assertEquals(3.0, optimum.getValueRef()[0], 1e-10);
        Assert.assertEquals(-1.0, optimum.getValueRef()[1], 1e-10);
        Assert.assertEquals(2.0, optimum.getValueRef()[2], 1e-10);

        Assert.assertEquals(0.0, optimizer.getChiSquare(), 1e-10);
        Assert.assertEquals(0.0, optimizer.getRMS(), 1e-10);
        Assert.assertTrue(optimizer.getJacobianEvaluations() > 0);
    }

    @Test
    public void testOptimizeWithDifferentiableMultivariateVectorFunction() {
        ConvergenceChecker<PointVectorValuePair> checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        DummyOptimizer optimizer = new DummyOptimizer(checker);
        DifferentiableLinearProblem problem = new DifferentiableLinearProblem();

        double[] target = new double[] { 3.0, -1.0, 2.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] startPoint = new double[] { 0.0, 0.0 };

        PointVectorValuePair optimum = optimizer.optimize(100, problem, target, weights, startPoint);

        Assert.assertNotNull(optimum);
        Assert.assertEquals(1.0, optimizer.getJacobianEvaluations());
        Assert.assertNotNull(optimizer.getWeightSquareRoot());
        Assert.assertEquals(3, optimizer.getWeightSquareRoot().getRowDimension());
        Assert.assertEquals(3, optimizer.getWeightSquareRoot().getColumnDimension());
    }

    @Test
    public void testCostAndResidualsComputation() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        double[] target = new double[] { 4.0, 0.0, 3.0 };
        double[] weights = new double[] { 2.0, 1.0, 3.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        optimizer.optimize(100, problem, target, weights, startPoint);

        double[] currentObj = optimizer.getObjective();
        Assert.assertEquals(3.0, currentObj[0], 1e-10);
        Assert.assertEquals(-1.0, currentObj[1], 1e-10);
        Assert.assertEquals(2.0, currentObj[2], 1e-10);

        double[] residuals = optimizer.publicComputeResiduals(currentObj);
        Assert.assertEquals(1.0, residuals[0], 1e-10);
        Assert.assertEquals(1.0, residuals[1], 1e-10);
        Assert.assertEquals(1.0, residuals[2], 1e-10);

        double expectedCost = FastMath.sqrt(2.0 * 1.0 * 1.0 + 1.0 * 1.0 * 1.0 + 3.0 * 1.0 * 1.0);
        Assert.assertEquals(expectedCost, optimizer.publicComputeCost(residuals), 1e-10);

        double[] weightedRes = optimizer.getWeightedResiduals();
        Assert.assertEquals(FastMath.sqrt(2.0), weightedRes[0], 1e-10);
        Assert.assertEquals(1.0, weightedRes[1], 1e-10);
        Assert.assertEquals(FastMath.sqrt(3.0), weightedRes[2], 1e-10);

        optimizer.publicSetCost(4.0);
        Assert.assertEquals(16.0, optimizer.getChiSquare(), 1e-10);
        Assert.assertEquals(FastMath.sqrt(16.0 / 3.0), optimizer.getRMS(), 1e-10);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        optimizer.optimize(100, problem, new double[] { 1.0, 2.0, 3.0 }, new double[] { 1.0, 1.0, 1.0 }, new double[] { 1.0, 1.0 });

        optimizer.publicComputeResiduals(new double[] { 1.0, 2.0 });
    }

    @Test
    public void testJacobianAndWeightedResidualJacobian() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        double[] target = new double[] { 3.0, -1.0, 2.0 };
        double[] weights = new double[] { 4.0, 9.0, 1.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        optimizer.optimize(100, problem, target, weights, startPoint);

        double[][] weightedResJac = optimizer.getWeightedResidualJacobian();
        Assert.assertEquals(3, weightedResJac.length);
        Assert.assertEquals(2, weightedResJac[0].length);

        Assert.assertEquals(-4.0, weightedResJac[0][0], 1e-10);
        Assert.assertEquals(-2.0, weightedResJac[0][1], 1e-10);
        Assert.assertEquals(-3.0, weightedResJac[1][0], 1e-10);
        Assert.assertEquals(6.0, weightedResJac[1][1], 1e-10);
        Assert.assertEquals(-1.0, weightedResJac[2][0], 1e-10);
        Assert.assertEquals(-1.0, weightedResJac[2][1], 1e-10);
    }

    @Test
    public void testCovariancesAndSigma() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        double[] target = new double[] { 3.0, -1.0, 2.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        optimizer.optimize(100, problem, target, weights, startPoint);

        double[][] cov1 = optimizer.getCovariances();
        double[][] cov2 = optimizer.getCovariances(1e-12);
        double[][] cov3 = optimizer.computeCovariances(new double[] { 1.0, 1.0 }, 1e-12);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Assert.assertEquals(cov1[i][j], cov2[i][j], 1e-10);
                Assert.assertEquals(cov1[i][j], cov3[i][j], 1e-10);
            }
        }

        double[] sigma = optimizer.computeSigma(new double[] { 1.0, 1.0 }, 1e-12);
        Assert.assertEquals(2, sigma.length);
        Assert.assertEquals(FastMath.sqrt(cov1[0][0]), sigma[0], 1e-10);
        Assert.assertEquals(FastMath.sqrt(cov1[1][1]), sigma[1], 1e-10);
    }

    @Test
    public void testGuessParametersErrors() {
        DummyOptimizer optimizer = new DummyOptimizer();
        LinearProblem problem = new LinearProblem();
        double[] target = new double[] { 3.0, -1.0, 2.5 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        optimizer.optimize(100, problem, target, weights, startPoint);

        double[] errors = optimizer.guessParametersErrors();
        Assert.assertEquals(2, errors.length);

        double c = FastMath.sqrt(optimizer.getChiSquare() / (3 - 2));
        double[][] cov = optimizer.getCovariances();
        Assert.assertEquals(FastMath.sqrt(cov[0][0]) * c, errors[0], 1e-10);
        Assert.assertEquals(FastMath.sqrt(cov[1][1]) * c, errors[1], 1e-10);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testGuessParametersErrorsThrowsWhenRowsLessOrEqualToCols() {
        DummyOptimizer optimizer = new DummyOptimizer();
        MultivariateDifferentiableVectorFunction squareProblem = new MultivariateDifferentiableVectorFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[] { point[0] + point[1], point[0] - point[1] };
            }

            @Override
            public DerivativeStructure[] value(DerivativeStructure[] point) {
                return new DerivativeStructure[] {
                    point[0].add(point[1]),
                    point[0].subtract(point[1])
                };
            }
        };

        optimizer.optimize(100, squareProblem, new double[] { 1.0, 1.0 }, new double[] { 1.0, 1.0 }, new double[] { 0.5, 0.5 });
        optimizer.guessParametersErrors();
    }

    @Test(expected = SingularMatrixException.class)
    public void testComputeCovariancesSingularMatrix() {
        DummyOptimizer optimizer = new DummyOptimizer();
        MultivariateDifferentiableVectorFunction singularProblem = new MultivariateDifferentiableVectorFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[] { point[0] + point[1], 2.0 * point[0] + 2.0 * point[1] };
            }

            @Override
            public DerivativeStructure[] value(DerivativeStructure[] point) {
                return new DerivativeStructure[] {
                    point[0].add(point[1]),
                    point[0].multiply(2.0).add(point[1].multiply(2.0))
                };
            }
        };

        optimizer.optimize(100, singularProblem, new double[] { 1.0, 2.0 }, new double[] { 1.0, 1.0 }, new double[] { 0.0, 0.0 });
        optimizer.computeCovariances(new double[] { 0.0, 0.0 }, 1e-14);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeWeightedJacobianDimensionMismatch() {
        DummyOptimizer optimizer = new DummyOptimizer();
        MultivariateDifferentiableVectorFunction mismatchProblem = new MultivariateDifferentiableVectorFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }

            @Override
            public DerivativeStructure[] value(DerivativeStructure[] point) {
                return new DerivativeStructure[] { point[0] };
            }
        };

        optimizer.optimize(100, mismatchProblem, new double[] { 1.0, 2.0 }, new double[] { 1.0, 1.0 }, new double[] { 0.0 });
    }
}
