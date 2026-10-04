package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.util.MathUtils;
import org.junit.Assert;
import org.junit.Test;

public class LevenbergMarquardtOptimizerTest {

    private static class LinearProblem implements DifferentiableMultivariateVectorialFunction {
        private final double[][] factors;
        private final double[] target;

        public LinearProblem(double[][] factors, double[] target) {
            this.factors = factors;
            this.target = target;
        }

        public double[] value(double[] variables) {
            double[] values = new double[factors.length];
            for (int i = 0; i < factors.length; ++i) {
                double val = 0;
                for (int j = 0; j < variables.length; ++j) {
                    val += factors[i][j] * variables[j];
                }
                values[i] = val;
            }
            return values;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return factors;
                }
            };
        }
    }

    private static class QuadraticProblem implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            return new double[] {
                point[0] * point[0] + point[1] * point[1] - 4.0,
                point[0] - point[1]
            };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] {
                        { 2.0 * point[0], 2.0 * point[1] },
                        { 1.0, -1.0 }
                    };
                }
            };
        }
    }

    private static class NonLinearProblem implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] p) {
            return new double[] {
                10.0 * (p[1] - p[0] * p[0]),
                1.0 - p[0]
            };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] p) {
                    return new double[][] {
                        { -20.0 * p[0], 10.0 },
                        { -1.0, 0.0 }
                    };
                }
            };
        }
    }

    @Test
    public void testSettersAndDefaults() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(50.0);
        optimizer.setCostRelativeTolerance(1.0e-8);
        optimizer.setParRelativeTolerance(1.0e-8);
        optimizer.setOrthoTolerance(1.0e-8);
        optimizer.setQRRankingThreshold(1.0e-12);
        Assert.assertNotNull(optimizer);
    }

    @Test
    public void testTrivialLinearOptimization() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 2.0, 0.0 },
            { 0.0, 3.0 }
        }, new double[] { 4.0, 9.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        Assert.assertEquals(2.0, optimum.getPointRef()[0], 1.0e-6);
        Assert.assertEquals(3.0, optimum.getPointRef()[1], 1.0e-6);
        Assert.assertEquals(0.0, optimizer.getCost(), 1.0e-6);
        Assert.assertEquals(0.0, optimizer.getRMS(), 1.0e-6);
    }

    @Test
    public void testNonLinearOptimization() throws OptimizationException, FunctionEvaluationException {
        NonLinearProblem problem = new NonLinearProblem();
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(100);

        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { -1.2, 1.0 }
        );

        Assert.assertEquals(1.0, optimum.getPointRef()[0], 1.0e-5);
        Assert.assertEquals(1.0, optimum.getPointRef()[1], 1.0e-5);
    }

    @Test
    public void testConvergenceChecker() throws OptimizationException, FunctionEvaluationException {
        QuadraticProblem problem = new QuadraticProblem();
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 2.0, 1.0 }
        );

        double sqrt2 = Math.sqrt(2.0);
        Assert.assertEquals(sqrt2, optimum.getPointRef()[0], 1.0e-4);
        Assert.assertEquals(sqrt2, optimum.getPointRef()[1], 1.0e-4);
    }

    @Test
    public void testRankDeficientProblem() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 1.0, 2.0, 3.0 }
        }, new double[] { 6.0, 12.0, 6.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setQRRankingThreshold(1.0e-7);

        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0, 1.0 },
            new double[] { 0.0, 0.0, 0.0 }
        );

        double[] point = optimum.getPointRef();
        double sum = point[0] + 2.0 * point[1] + 3.0 * point[2];
        Assert.assertEquals(6.0, sum, 1.0e-4);
    }

    @Test
    public void testOverdeterminedSystem() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0, 1.0 },
            { 2.0, 1.0 },
            { 3.0, 1.0 },
            { 4.0, 1.0 }
        }, new double[] { 3.0, 5.0, 7.0, 9.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0, 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        Assert.assertEquals(2.0, optimum.getPointRef()[0], 1.0e-5);
        Assert.assertEquals(1.0, optimum.getPointRef()[1], 1.0e-5);
    }

    @Test
    public void testUnderdeterminedSystem() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0, 2.0, 3.0 }
        }, new double[] { 6.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0 },
            new double[] { 0.0, 0.0, 0.0 }
        );

        double[] point = optimum.getPointRef();
        double val = point[0] + 2.0 * point[1] + 3.0 * point[2];
        Assert.assertEquals(6.0, val, 1.0e-4);
    }

    @Test
    public void testInitialPointAtMinimum() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        }, new double[] { 2.0, 3.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0 },
            new double[] { 2.0, 3.0 }
        );

        Assert.assertEquals(2.0, optimum.getPointRef()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPointRef()[1], 1.0e-10);
    }

    @Test
    public void testInitialStepBoundWithZeroNorm() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        }, new double[] { 0.0, 0.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        Assert.assertEquals(0.0, optimum.getPointRef()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getPointRef()[1], 1.0e-10);
    }

    @Test(expected = OptimizationException.class)
    public void testMaxIterationsExceeded() throws OptimizationException, FunctionEvaluationException {
        NonLinearProblem problem = new NonLinearProblem();
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1);
        optimizer.optimize(
            problem,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { -10.0, -10.0 }
        );
    }

    @Test(expected = OptimizationException.class)
    public void testQRDecompositionWithNaN() throws OptimizationException, FunctionEvaluationException {
        DifferentiableMultivariateVectorialFunction problem = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { Double.NaN } };
                    }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            problem,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );
    }

    @Test(expected = OptimizationException.class)
    public void testQRDecompositionWithInf() throws OptimizationException, FunctionEvaluationException {
        DifferentiableMultivariateVectorialFunction problem = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { Double.POSITIVE_INFINITY } };
                    }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            problem,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );
    }

    @Test
    public void testZeroCostExactFit() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 1.0 }
        }, new double[] { 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0 },
            new double[] { 5.0 }
        );

        Assert.assertEquals(5.0, optimum.getPointRef()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimizer.getCost(), 1.0e-10);
    }

    @Test
    public void testGivensRotationWithDiagonalZero() throws OptimizationException, FunctionEvaluationException {
        LinearProblem problem = new LinearProblem(new double[][] {
            { 0.0, 1.0 },
            { 1.0, 0.0 }
        }, new double[] { 3.0, 2.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(
            problem,
            problem.target,
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        Assert.assertEquals(2.0, optimum.getPointRef()[0], 1.0e-6);
        Assert.assertEquals(3.0, optimum.getPointRef()[1], 1.0e-6);
    }
}
