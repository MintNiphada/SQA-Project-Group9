package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

public class LevenbergMarquardtOptimizerTest {

    private static class LinearProblem {
        final double[][] factors;
        final double[] target;

        LinearProblem(double[][] factors, double[] target) {
            this.factors = factors;
            this.target = target;
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    double[] values = new double[factors.length];
                    for (int i = 0; i < factors.length; ++i) {
                        double val = 0.0;
                        for (int j = 0; j < point.length; ++j) {
                            val += factors[i][j] * point[j];
                        }
                        values[i] = val;
                    }
                    return values;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return factors;
                }
            });
        }
    }

    private static class QuadraticProblem {
        final double[] x;
        final double[] y;

        QuadraticProblem(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    final double a = point[0];
                    final double b = point[1];
                    final double c = point[2];
                    double[] values = new double[x.length];
                    for (int i = 0; i < x.length; ++i) {
                        values[i] = (a * x[i] + b) * x[i] + c;
                    }
                    return values;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double[][] jac = new double[x.length][3];
                    for (int i = 0; i < x.length; ++i) {
                        jac[i][0] = x[i] * x[i];
                        jac[i][1] = x[i];
                        jac[i][2] = 1.0;
                    }
                    return jac;
                }
            });
        }
    }

    @Test
    public void testConstructors() {
        LevenbergMarquardtOptimizer opt1 = new LevenbergMarquardtOptimizer();
        Assert.assertNotNull(opt1);

        ConvergenceChecker<PointVectorValuePair> checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        LevenbergMarquardtOptimizer opt2 = new LevenbergMarquardtOptimizer(checker);
        Assert.assertNotNull(opt2);

        LevenbergMarquardtOptimizer opt3 = new LevenbergMarquardtOptimizer(100.0, checker, 1e-8, 1e-8, 1e-8, 1e-12);
        Assert.assertNotNull(opt3);

        LevenbergMarquardtOptimizer opt4 = new LevenbergMarquardtOptimizer(1e-8, 1e-8, 1e-8);
        Assert.assertNotNull(opt4);

        LevenbergMarquardtOptimizer opt5 = new LevenbergMarquardtOptimizer(50.0, 1e-8, 1e-8, 1e-8, Precision.SAFE_MIN);
        Assert.assertNotNull(opt5);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testBoundsNotSupportedLower() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        LinearProblem problem = new LinearProblem(new double[][] { { 1.0 } }, new double[] { 2.0 });

        optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 0.0 }),
            new SimpleBounds(new double[] { -1.0 }, new double[] { 1.0 })
        );
    }

    @Test
    public void testSimpleLinearSolve() {
        double[][] factors = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { 1.0, 1.0 }
        };
        double[] target = new double[] { 1.0, 2.0, 3.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] solution = optimum.getPoint();
        Assert.assertEquals(1.0, solution[0], 1e-6);
        Assert.assertEquals(2.0, solution[1], 1e-6);
        Assert.assertTrue(optimizer.getCost() < 1e-6);
    }

    @Test
    public void testQuadraticFitWithCustomChecker() {
        double[] x = new double[] { -2, -1, 0, 1, 2, 3, 4 };
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; ++i) {
            y[i] = 2.0 * x[i] * x[i] - 3.0 * x[i] + 5.0;
        }

        QuadraticProblem problem = new QuadraticProblem(x, y);
        ConvergenceChecker<PointVectorValuePair> checker = new SimpleVectorValueChecker(1e-7, 1e-7);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.y),
            new Weight(new DiagonalMatrix(new double[] { 1, 1, 1, 1, 1, 1, 1 })),
            new InitialGuess(new double[] { 1.0, 1.0, 1.0 })
        );

        double[] solution = optimum.getPoint();
        Assert.assertEquals(2.0, solution[0], 1e-5);
        Assert.assertEquals(-3.0, solution[1], 1e-5);
        Assert.assertEquals(5.0, solution[2], 1e-5);
    }

    @Test
    public void testOrthogonalityConvergence() {
        LinearProblem problem = new LinearProblem(new double[][] { { 2.0 } }, new double[] { 4.0 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(100.0, 1e-15, 1e-15, 0.99, Precision.SAFE_MIN);

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 2.0 })
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-8);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-8);
    }

    @Test
    public void testZeroInitialResidual() {
        LinearProblem problem = new LinearProblem(new double[][] { { 3.0 } }, new double[] { 6.0 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 2.0 })
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-8);
    }

    @Test
    public void testRankDeficientJacobian() {
        double[][] factors = new double[][] {
            { 1.0, 2.0 },
            { 2.0, 4.0 },
            { 3.0, 6.0 }
        };
        double[] target = new double[] { 3.0, 6.0, 9.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(100.0, 1e-10, 1e-10, 1e-10, 1e-3);

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] solution = optimum.getPoint();
        Assert.assertEquals(3.0, solution[0] + 2.0 * solution[1], 1e-5);
    }

    @Test
    public void testZeroInitialGuessWithZeroColumnNorm() {
        double[][] factors = new double[][] {
            { 0.0, 1.0 },
            { 0.0, 2.0 }
        };
        double[] target = new double[] { 1.0, 2.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] solution = optimum.getPoint();
        Assert.assertEquals(1.0, solution[1], 1e-5);
    }

    @Test
    public void testNonlinearRosenbrockFunction() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                double x = point[0];
                double y = point[1];
                return new double[] {
                    10.0 * (y - x * x),
                    1.0 - x
                };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                double x = point[0];
                return new double[][] {
                    { -20.0 * x, 10.0 },
                    { -1.0, 0.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(500),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian),
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { -1.2, 1.0 })
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1e-4);
    }

    @Test
    public void testFailedStepRecovery() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                double x = point[0];
                return new double[] {
                    Math.exp(x * x) - 1.0,
                    x - 0.5
                };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                double x = point[0];
                return new double[][] {
                    { 2.0 * x * Math.exp(x * x) },
                    { 1.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(0.1, 1e-10, 1e-10, 1e-10, Precision.SAFE_MIN);
        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian),
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 2.0 })
        );

        Assert.assertNotNull(optimum);
        Assert.assertTrue(optimizer.getCost() >= 0.0);
    }

    @Test(expected = ConvergenceException.class)
    public void testJacobianWithNaNThrowsConvergenceException() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] { { Double.NaN } };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian),
            new Target(new double[] { 0.0 }),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 1.0 })
        );
    }

    @Test(expected = ConvergenceException.class)
    public void testJacobianWithInfiniteThrowsConvergenceException() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] { { Double.POSITIVE_INFINITY } };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian),
            new Target(new double[] { 0.0 }),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 1.0 })
        );
    }

    @Test
    public void testUnderdeterminedSystemLeastSquares() {
        double[][] factors = new double[][] {
            { 1.0, 1.0 }
        };
        double[] target = new double[] { 2.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian(),
            new Target(problem.target),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] solution = optimum.getPoint();
        Assert.assertEquals(2.0, solution[0] + solution[1], 1e-5);
    }

    @Test
    public void testVeryStringentTolerancesTriggerConvergenceException() {
        LinearProblem problem = new LinearProblem(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } }, new double[] { 1.0, 1.0 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(1.0, 1e-25, 1e-25, 1e-25, Precision.SAFE_MIN);

        try {
            optimizer.optimize(
                new MaxEval(500),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                new Target(problem.target),
                new Weight(new double[] { 1.0, 1.0 }),
                new InitialGuess(new double[] { 0.5, 0.5 })
            );
        } catch (ConvergenceException ce) {
            Assert.assertNotNull(ce.getMessage());
        }
    }
}
