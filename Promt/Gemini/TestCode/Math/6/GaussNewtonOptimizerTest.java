package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Assert;
import org.junit.Test;

public class GaussNewtonOptimizerTest {

    private static class LinearProblem {
        private final double[][] factors;
        private final double[] target;

        public LinearProblem(double[][] factors, double[] target) {
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

        public Target getTarget() {
            return new Target(target);
        }

        public Weight getWeight() {
            double[] weights = new double[target.length];
            for (int i = 0; i < weights.length; ++i) {
                weights[i] = 1.0;
            }
            return new Weight(weights);
        }
    }

    private static class CircleVectorProblem {
        private final double[][] points;

        public CircleVectorProblem(double[][] points) {
            this.points = points;
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double r = params[2];
                    double[] residuals = new double[points.length];
                    for (int i = 0; i < points.length; i++) {
                        double dx = points[i][0] - cx;
                        double dy = points[i][1] - cy;
                        residuals[i] = Math.sqrt(dx * dx + dy * dy) - r;
                    }
                    return residuals;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double[][] jac = new double[points.length][3];
                    for (int i = 0; i < points.length; i++) {
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
            });
        }
    }

    @Test
    public void testDefaultConstructor() {
        SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(checker);
        Assert.assertSame(checker, optimizer.getConvergenceChecker());
    }

    @Test
    public void testConstructorWithUseLU() {
        SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        GaussNewtonOptimizer optimizerLU = new GaussNewtonOptimizer(true, checker);
        Assert.assertSame(checker, optimizerLU.getConvergenceChecker());

        GaussNewtonOptimizer optimizerQR = new GaussNewtonOptimizer(false, checker);
        Assert.assertSame(checker, optimizerQR.getConvergenceChecker());
    }

    @Test(expected = NullArgumentException.class)
    public void testNullCheckerThrowsException() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(null);
        LinearProblem problem = new LinearProblem(new double[][] { { 1.0 } }, new double[] { 2.0 });
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0 })
        );
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testLowerBoundsThrowException() {
        SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(checker);
        LinearProblem problem = new LinearProblem(new double[][] { { 1.0 } }, new double[] { 2.0 });
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0 }),
                new SimpleBounds(new double[] { -1.0 }, new double[] { Double.POSITIVE_INFINITY })
        );
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testUpperBoundsThrowException() {
        SimpleVectorValueChecker checker = new SimpleVectorValueChecker(1e-6, 1e-6);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(checker);
        LinearProblem problem = new LinearProblem(new double[][] { { 1.0 } }, new double[] { 2.0 });
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0 }),
                new SimpleBounds(new double[] { Double.NEGATIVE_INFINITY }, new double[] { 5.0 })
        );
    }

    @Test
    public void testLinearLeastSquaresLU() {
        double[][] factors = {
                { 1.0, 2.0 },
                { 2.0, -1.0 },
                { 3.0, 1.0 }
        };
        double[] target = { 5.0, 1.0, 6.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-10, 1e-10));

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(1.378378378378, point[0], 1e-5);
        Assert.assertEquals(1.783783783783, point[1], 1e-5);
        Assert.assertTrue(optimizer.getCost() > 0.0);
    }

    @Test
    public void testLinearLeastSquaresQR() {
        double[][] factors = {
                { 1.0, 2.0 },
                { 2.0, -1.0 },
                { 3.0, 1.0 }
        };
        double[] target = { 5.0, 1.0, 6.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(false, new SimpleVectorValueChecker(1e-10, 1e-10));

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0, 0.0 })
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(1.378378378378, point[0], 1e-5);
        Assert.assertEquals(1.783783783783, point[1], 1e-5);
        Assert.assertTrue(optimizer.getCost() > 0.0);
    }

    @Test
    public void testNonlinearCircleFittingLU() {
        double[][] points = {
                { 1.0, 0.0 },
                { 0.0, 1.0 },
                { -1.0, 0.0 },
                { 0.0, -1.0 }
        };
        CircleVectorProblem problem = new CircleVectorProblem(points);

        double[] target = new double[points.length];
        double[] weights = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            target[i] = 0.0;
            weights[i] = 1.0;
        }

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, new SimplePointChecker<PointVectorValuePair>(1e-8, 1e-8));

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                new Target(target),
                new Weight(weights),
                new InitialGuess(new double[] { 0.1, 0.1, 0.8 })
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(0.0, point[0], 1e-5);
        Assert.assertEquals(0.0, point[1], 1e-5);
        Assert.assertEquals(1.0, point[2], 1e-5);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-5);
    }

    @Test
    public void testNonlinearCircleFittingQR() {
        double[][] points = {
                { 1.0, 0.0 },
                { 0.0, 1.0 },
                { -1.0, 0.0 },
                { 0.0, -1.0 }
        };
        CircleVectorProblem problem = new CircleVectorProblem(points);

        double[] target = new double[points.length];
        double[] weights = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            target[i] = 0.0;
            weights[i] = 1.0;
        }

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(false, new SimplePointChecker<PointVectorValuePair>(1e-8, 1e-8));

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                new Target(target),
                new Weight(weights),
                new InitialGuess(new double[] { 0.2, -0.1, 1.2 })
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(0.0, point[0], 1e-5);
        Assert.assertEquals(0.0, point[1], 1e-5);
        Assert.assertEquals(1.0, point[2], 1e-5);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-5);
    }

    @Test(expected = ConvergenceException.class)
    public void testSingularProblemLUThrowsConvergenceException() {
        double[][] factors = {
                { 1.0, 2.0 },
                { 2.0, 4.0 }
        };
        double[] target = { 1.0, 2.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-6, 1e-6));

        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0, 0.0 })
        );
    }

    @Test(expected = ConvergenceException.class)
    public void testSingularProblemQRThrowsConvergenceException() {
        double[][] factors = {
                { 1.0, 2.0 },
                { 2.0, 4.0 }
        };
        double[] target = { 1.0, 2.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(false, new SimpleVectorValueChecker(1e-6, 1e-6));

        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0, 0.0 })
        );
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        double[][] factors = {
                { 1.0, 2.0 },
                { 2.0, -1.0 }
        };
        double[] target = { 5.0, 1.0 };

        LinearProblem problem = new LinearProblem(factors, target);
        ConvergenceChecker<PointVectorValuePair> neverConverging = new ConvergenceChecker<PointVectorValuePair>() {
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return false;
            }
        };

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, neverConverging);
        optimizer.optimize(
                new MaxEval(3),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0, 0.0 })
        );
    }

    @Test
    public void testCustomConvergenceCheckerInvocation() {
        double[][] factors = {
                { 2.0 }
        };
        double[] target = { 4.0 };

        LinearProblem problem = new LinearProblem(factors, target);

        final int[] checkerCalls = { 0 };
        ConvergenceChecker<PointVectorValuePair> countingChecker = new ConvergenceChecker<PointVectorValuePair>() {
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                checkerCalls[0]++;
                Assert.assertNotNull(previous);
                Assert.assertNotNull(current);
                return iteration >= 2;
            }
        };

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, countingChecker);
        PointVectorValuePair result = optimizer.optimize(
                new MaxEval(100),
                new MaxIter(100),
                problem.getModelFunction(),
                problem.getModelFunctionJacobian(),
                problem.getTarget(),
                problem.getWeight(),
                new InitialGuess(new double[] { 0.0 })
        );

        Assert.assertTrue(checkerCalls[0] >= 2);
        Assert.assertEquals(2.0, result.getPoint()[0], 1e-10);
    }
}
