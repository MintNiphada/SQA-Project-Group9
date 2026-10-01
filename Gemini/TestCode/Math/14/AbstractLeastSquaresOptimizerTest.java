package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static class DummyOptimizer extends AbstractLeastSquaresOptimizer {
        public DummyOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            final double[] params = getStartPoint();
            final double[] objectiveValue = computeObjectiveValue(params);
            final double[] residuals = computeResiduals(objectiveValue);
            final double cost = computeCost(residuals);
            setCost(cost);
            return new PointVectorValuePair(params, objectiveValue);
        }

        // Public delegators for protected methods to allow direct testing
        public RealMatrix testComputeWeightedJacobian(double[] params) {
            return computeWeightedJacobian(params);
        }

        public double testComputeCost(double[] residuals) {
            return computeCost(residuals);
        }

        public void testSetCost(double cost) {
            setCost(cost);
        }

        public double[] testComputeResiduals(double[] objectiveValue) {
            return computeResiduals(objectiveValue);
        }
    }

    private DummyOptimizer optimizer;
    private ConvergenceChecker<PointVectorValuePair> checker;

    @Before
    public void setUp() {
        checker = new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6);
        optimizer = new DummyOptimizer(checker);
    }

    @Test
    public void testOptimizeAndBasicMetrics() {
        final double[] target = new double[] { 3.0, 4.0 };
        final double[] initialGuess = new double[] { 1.0, 2.0 };
        final double[] weights = new double[] { 1.0, 1.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] * 2.0, point[1] * 2.0 };
            }
        };

        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 2.0, 0.0 },
                    { 0.0, 2.0 }
                };
            }
        };

        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(initialGuess, result.getPoint(), 1e-10);
        Assert.assertArrayEquals(new double[] { 2.0, 4.0 }, result.getValue(), 1e-10);

        // Target: [3.0, 4.0], Model: [2.0, 4.0], Residuals: [1.0, 0.0]
        // Weight: Identity, Cost: sqrt(1.0^2 + 0.0^2) = 1.0
        double cost = optimizer.getChiSquare();
        Assert.assertEquals(1.0, cost, 1e-10);
        Assert.assertEquals(1.0, optimizer.getChiSquare(), 1e-10);

        // RMS: sqrt(ChiSquare / targetSize) = sqrt(1.0 / 2) = sqrt(0.5)
        Assert.assertEquals(FastMath.sqrt(0.5), optimizer.getRMS(), 1e-10);

        RealMatrix weightSqrt = optimizer.getWeightSquareRoot();
        Assert.assertEquals(1.0, weightSqrt.getEntry(0, 0), 1e-10);
        Assert.assertEquals(0.0, weightSqrt.getEntry(0, 1), 1e-10);
        Assert.assertEquals(0.0, weightSqrt.getEntry(1, 0), 1e-10);
        Assert.assertEquals(1.0, weightSqrt.getEntry(1, 1), 1e-10);
    }

    @Test
    public void testComputeWeightedJacobian() {
        final double[] target = new double[] { 1.0, 2.0 };
        final double[] weights = new double[] { 4.0, 9.0 }; // Sqrt should be diagonal(2, 3)
        final double[] initialGuess = new double[] { 1.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0], point[0] * 2.0 };
            }
        };

        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 2.0 },
                    { 3.0 }
                };
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        RealMatrix weightedJacobian = optimizer.testComputeWeightedJacobian(initialGuess);
        // Sqrt(W) * J = [ [2, 0], [0, 3] ] * [ [2], [3] ] = [ [4], [9] ]
        Assert.assertEquals(2, weightedJacobian.getRowDimension());
        Assert.assertEquals(1, weightedJacobian.getColumnDimension());
        Assert.assertEquals(4.0, weightedJacobian.getEntry(0, 0), 1e-10);
        Assert.assertEquals(9.0, weightedJacobian.getEntry(1, 0), 1e-10);
    }

    @Test
    public void testComputeCost() {
        final double[] target = new double[] { 0.0, 0.0 };
        final RealMatrix weightMatrix = new DiagonalMatrix(new double[] { 2.0, 3.0 });
        final double[] initialGuess = new double[] { 0.0, 0.0 };

        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weightMatrix),
            new InitialGuess(initialGuess),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return point; }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
                }
            })
        );

        double[] residuals = new double[] { 2.0, -1.0 };
        // r = [2, -1], W = diag(2, 3), W*r = [4, -3], r . (W*r) = 2*4 + (-1)*(-3) = 8 + 3 = 11
        // Cost = sqrt(11)
        double cost = optimizer.testComputeCost(residuals);
        Assert.assertEquals(FastMath.sqrt(11.0), cost, 1e-10);
    }

    @Test
    public void testSetCostAndGetChiSquare() {
        optimizer.testSetCost(3.5);
        Assert.assertEquals(12.25, optimizer.getChiSquare(), 1e-10);
    }

    @Test
    public void testComputeResidualsSuccess() {
        final double[] target = new double[] { 5.0, 10.0, -2.0 };
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0, 0.0 }),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return point; }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] {
                        { 1.0, 0.0, 0.0 },
                        { 0.0, 1.0, 0.0 },
                        { 0.0, 0.0, 1.0 }
                    };
                }
            })
        );

        double[] objectiveValue = new double[] { 2.0, 12.0, -5.0 };
        double[] residuals = optimizer.testComputeResiduals(objectiveValue);

        Assert.assertEquals(3, residuals.length);
        Assert.assertEquals(3.0, residuals[0], 1e-10);
        Assert.assertEquals(-2.0, residuals[1], 1e-10);
        Assert.assertEquals(3.0, residuals[2], 1e-10);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        final double[] target = new double[] { 5.0, 10.0 };
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return point; }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
                }
            })
        );

        optimizer.testComputeResiduals(new double[] { 1.0 });
    }

    @Test
    public void testComputeCovariancesAndSigma() {
        final double[] target = new double[] { 0.0, 0.0 };
        final double[] initialGuess = new double[] { 0.0, 0.0 };
        final double[] weights = new double[] { 1.0, 1.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) { return point; }
        };

        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                // J = [ [2, 0], [0, 4] ]
                return new double[][] {
                    { 2.0, 0.0 },
                    { 0.0, 4.0 }
                };
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        // J^T J = [ [4, 0], [0, 16] ]
        // Covariance = (J^T J)^-1 = [ [0.25, 0], [0, 1/16 = 0.0625] ]
        double[][] cov = optimizer.computeCovariances(initialGuess, 1e-14);
        Assert.assertEquals(2, cov.length);
        Assert.assertEquals(2, cov[0].length);
        Assert.assertEquals(0.25, cov[0][0], 1e-10);
        Assert.assertEquals(0.0, cov[0][1], 1e-10);
        Assert.assertEquals(0.0, cov[1][0], 1e-10);
        Assert.assertEquals(0.0625, cov[1][1], 1e-10);

        // Sigma = [ sqrt(0.25), sqrt(0.0625) ] = [ 0.5, 0.25 ]
        double[] sigma = optimizer.computeSigma(initialGuess, 1e-14);
        Assert.assertEquals(2, sigma.length);
        Assert.assertEquals(0.5, sigma[0], 1e-10);
        Assert.assertEquals(0.25, sigma[1], 1e-10);
    }

    @Test(expected = SingularMatrixException.class)
    public void testComputeCovariancesSingular() {
        final double[] target = new double[] { 0.0, 0.0 };
        final double[] initialGuess = new double[] { 0.0, 0.0 };
        final double[] weights = new double[] { 1.0, 1.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) { return point; }
        };

        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                // Rank deficient Jacobian
                return new double[][] {
                    { 1.0, 2.0 },
                    { 2.0, 4.0 }
                };
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        optimizer.computeCovariances(initialGuess, 1e-14);
    }

    @Test(expected = SingularMatrixException.class)
    public void testComputeSigmaSingular() {
        final double[] target = new double[] { 0.0, 0.0 };
        final double[] initialGuess = new double[] { 0.0, 0.0 };
        final double[] weights = new double[] { 1.0, 1.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) { return point; }
        };

        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 0.0, 0.0 },
                    { 0.0, 0.0 }
                };
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        optimizer.computeSigma(initialGuess, 1e-14);
    }

    @Test
    public void testWeightMatrixSqrtCopy() {
        final RealMatrix weight = new Array2DRowRealMatrix(new double[][] {
            { 4.0, 0.0 },
            { 0.0, 16.0 }
        });

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 1.0, 1.0 }),
            new Weight(weight),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return point; }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
                }
            })
        );

        RealMatrix sqrt1 = optimizer.getWeightSquareRoot();
        RealMatrix sqrt2 = optimizer.getWeightSquareRoot();
        Assert.assertNotSame(sqrt1, sqrt2);
        Assert.assertEquals(sqrt1.getEntry(0, 0), sqrt2.getEntry(0, 0), 1e-10);
        Assert.assertEquals(2.0, sqrt1.getEntry(0, 0), 1e-10);
        Assert.assertEquals(4.0, sqrt1.getEntry(1, 1), 1e-10);
    }

    @Test
    public void testOptimizeWithoutWeightOptimizationDataReusesPrevious() {
        final double[] target = new double[] { 1.0, 2.0 };
        final double[] weights = new double[] { 4.0, 9.0 };
        final double[] initialGuess = new double[] { 1.0, 2.0 };

        final MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) { return point; }
        };
        final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
            }
        };

        // First optimization with weight
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        RealMatrix sqrtFirst = optimizer.getWeightSquareRoot();

        // Second optimization without specifying Weight again
        optimizer.optimize(
            new MaxEval(100),
            new InitialGuess(initialGuess)
        );

        RealMatrix sqrtSecond = optimizer.getWeightSquareRoot();
        Assert.assertEquals(sqrtFirst.getEntry(0, 0), sqrtSecond.getEntry(0, 0), 1e-10);
        Assert.assertEquals(sqrtFirst.getEntry(1, 1), sqrtSecond.getEntry(1, 1), 1e-10);
    }
}
