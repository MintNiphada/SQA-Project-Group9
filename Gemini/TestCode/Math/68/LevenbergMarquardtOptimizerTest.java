package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
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

        public double[] value(double[] point) {
            double[] val = new double[factors.length];
            for (int i = 0; i < factors.length; ++i) {
                double sum = 0;
                for (int j = 0; j < point.length; ++j) {
                    sum += factors[i][j] * point[j];
                }
                val[i] = sum;
            }
            return val;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return factors;
                }
            };
        }
    }

    private static class CircleProblem implements DifferentiableMultivariateVectorialFunction {
        private final double[][] points;

        public CircleProblem(double[][] points) {
            this.points = points;
        }

        public double[] value(double[] params) {
            double[] res = new double[points.length];
            double cx = params[0];
            double cy = params[1];
            double r = params[2];
            for (int i = 0; i < points.length; ++i) {
                double dx = points[i][0] - cx;
                double dy = points[i][1] - cy;
                res[i] = Math.sqrt(dx * dx + dy * dy) - r;
            }
            return res;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] params) {
                    double[][] jac = new double[points.length][3];
                    double cx = params[0];
                    double cy = params[1];
                    for (int i = 0; i < points.length; ++i) {
                        double dx = points[i][0] - cx;
                        double dy = points[i][1] - cy;
                        double d = Math.sqrt(dx * dx + dy * dy);
                        if (d == 0) {
                            jac[i][0] = 0;
                            jac[i][1] = 0;
                        } else {
                            jac[i][0] = -dx / d;
                            jac[i][1] = -dy / d;
                        }
                        jac[i][2] = -1.0;
                    }
                    return jac;
                }
            };
        }
    }

    @Test
    public void testConstructorsAndSetters() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(50.0);
        optimizer.setCostRelativeTolerance(1e-8);
        optimizer.setParRelativeTolerance(1e-8);
        optimizer.setOrthoTolerance(1e-8);
        optimizer.setMaxIterations(500);
        Assert.assertEquals(500, optimizer.getMaxIterations());
    }

    @Test
    public void testLinearSolve() throws FunctionEvaluationException, OptimizationException {
        double[][] factors = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { 1.0, 1.0 }
        };
        double[] target = new double[] { 1.0, 2.0, 3.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] start = new double[] { 0.0, 0.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);

        double[] point = optimum.getPointRef();
        Assert.assertEquals(1.0, point[0], 1e-6);
        Assert.assertEquals(2.0, point[1], 1e-6);
    }

    @Test
    public void testCircleFitting() throws FunctionEvaluationException, OptimizationException {
        double[][] points = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { -1.0, 0.0 },
            { 0.0, -1.0 }
        };
        double[] target = new double[points.length];
        double[] weights = new double[] { 1.0, 1.0, 1.0, 1.0 };
        double[] start = new double[] { 0.1, 0.1, 0.8 };

        CircleProblem problem = new CircleProblem(points);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);

        double[] point = optimum.getPointRef();
        Assert.assertEquals(0.0, point[0], 1e-5);
        Assert.assertEquals(0.0, point[1], 1e-5);
        Assert.assertEquals(1.0, point[2], 1e-5);
    }

    @Test
    public void testRankDeficientProblem() throws FunctionEvaluationException, OptimizationException {
        double[][] factors = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 0.0, 0.0, 0.0 }
        };
        double[] target = new double[] { 6.0, 12.0, 0.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] start = new double[] { 0.0, 0.0, 0.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);
        Assert.assertNotNull(optimum);
    }

    @Test
    public void testWithConvergenceChecker() throws FunctionEvaluationException, OptimizationException {
        double[][] factors = new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        };
        double[] target = new double[] { 4.0, 5.0 };
        double[] weights = new double[] { 1.0, 1.0 };
        double[] start = new double[] { 0.0, 0.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1e-6, 1e-6));
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);

        double[] point = optimum.getPointRef();
        Assert.assertEquals(1.0, point[0], 1e-4);
        Assert.assertEquals(2.0, point[1], 1e-4);
    }

    @Test
    public void testZeroCostInitial() throws FunctionEvaluationException, OptimizationException {
        double[][] factors = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        };
        double[] target = new double[] { 2.0, 3.0 };
        double[] weights = new double[] { 1.0, 1.0 };
        double[] start = new double[] { 2.0, 3.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);

        double[] point = optimum.getPointRef();
        Assert.assertEquals(2.0, point[0], 1e-8);
        Assert.assertEquals(3.0, point[1], 1e-8);
    }

    @Test(expected = OptimizationException.class)
    public void testInfiniteJacobianThrowsException() throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
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
        optimizer.optimize(function, new double[] { 0.0 }, new double[] { 1.0 }, new double[] { 0.0 });
    }

    @Test(expected = OptimizationException.class)
    public void testNaNJacobianThrowsException() throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
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
        optimizer.optimize(function, new double[] { 0.0 }, new double[] { 1.0 }, new double[] { 0.0 });
    }

    @Test
    public void testZeroJacobianNormHandling() throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0, 2.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 0.0 }, { 0.0 } };
                    }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(function, new double[] { 1.0, 2.0 }, new double[] { 1.0, 1.0 }, new double[] { 0.0 });
        Assert.assertEquals(0.0, optimum.getPointRef()[0], 1e-8);
    }

    @Test
    public void testOverdeterminedSystemWithSmallNormCol() throws FunctionEvaluationException, OptimizationException {
        double[][] factors = new double[][] {
            { 1.0, 1e-12 },
            { 2.0, 1e-12 },
            { 3.0, 1e-12 }
        };
        double[] target = new double[] { 1.0, 2.0, 3.0 };
        double[] weights = new double[] { 1.0, 1.0, 1.0 };
        double[] start = new double[] { 0.5, 0.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, start);
        Assert.assertEquals(1.0, optimum.getPointRef()[0], 1e-5);
    }
}
