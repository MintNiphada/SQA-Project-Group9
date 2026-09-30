package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.Assert;
import org.junit.Test;

public class PowellOptimizerTest {

    @Test
    public void testConstructorsValid() {
        PowellOptimizer opt1 = new PowellOptimizer(1e-3, 1e-3);
        Assert.assertNotNull(opt1);

        PowellOptimizer opt2 = new PowellOptimizer(1e-3, 1e-3, new SimpleValueChecker(1e-3, 1e-3));
        Assert.assertNotNull(opt2);

        PowellOptimizer opt3 = new PowellOptimizer(1e-3, 1e-3, 1e-2, 1e-2);
        Assert.assertNotNull(opt3);

        PowellOptimizer opt4 = new PowellOptimizer(1e-3, 1e-3, 1e-2, 1e-2, new SimpleValueChecker(1e-3, 1e-3));
        Assert.assertNotNull(opt4);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorRelativeThresholdTooSmall() {
        new PowellOptimizer(1e-17, 1e-3);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorWithCheckerRelativeThresholdTooSmall() {
        new PowellOptimizer(1e-17, 1e-3, new SimpleValueChecker(1e-3, 1e-3));
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsoluteThresholdZero() {
        new PowellOptimizer(1e-3, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsoluteThresholdNegative() {
        new PowellOptimizer(1e-3, -1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithCheckerAbsoluteThresholdZero() {
        new PowellOptimizer(1e-3, 0.0, new SimpleValueChecker(1e-3, 1e-3));
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testBoundsUnsupportedLower() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-3, 1e-3);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0}),
                new SimpleBounds(new double[]{-1.0}, new double[]{Double.POSITIVE_INFINITY})
        );
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testBoundsUnsupportedUpper() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-3, 1e-3);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0}),
                new SimpleBounds(new double[]{Double.NEGATIVE_INFINITY}, new double[]{10.0})
        );
    }

    @Test
    public void testQuadraticMinimize() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                final double x = point[0] - 3.0;
                final double y = point[1] + 2.0;
                return x * x + y * y + 5.0;
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.0, 0.0})
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(-2.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(5.0, optimum.getValue(), 1e-3);
    }

    @Test
    public void testQuadraticMaximize() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                final double x = point[0] - 1.0;
                final double y = point[1] - 4.0;
                return -(x * x + y * y) + 10.0;
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(func),
                GoalType.MAXIMIZE,
                new InitialGuess(new double[]{0.0, 0.0})
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(4.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(10.0, optimum.getValue(), 1e-3);
    }

    @Test
    public void testRosenbrockMinimize() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-10, 1e-10, 1e-8, 1e-8);
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = x[1] - x[0] * x[0];
                double b = 1.0 - x[0];
                return 100.0 * a * a + b * b;
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(2000),
                new ObjectiveFunction(rosenbrock),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{-1.2, 1.0})
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-3);
    }

    @Test
    public void testCustomConvergenceChecker() {
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(1e-4, 1e-4);
        PowellOptimizer optimizer = new PowellOptimizer(1e-13, 1e-13, checker);

        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 2.0) * (point[0] - 2.0) + (point[1] - 3.0) * (point[1] - 3.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(500),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.0, 0.0})
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-3);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvalExceeded() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-10, 1e-10);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        optimizer.optimize(
                new MaxEval(5),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{10.0, 10.0})
        );
    }

    @Test
    public void testAlreadyAtOptimumMinimize() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-4);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.0, 0.0})
        );

        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    @Test
    public void testAlreadyAtOptimumMaximize() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-4);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return -(point[0] * point[0] + point[1] * point[1]);
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MAXIMIZE,
                new InitialGuess(new double[]{0.0, 0.0})
        );

        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    @Test
    public void testSingleVariableOptimization() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 7.0) * (point[0] - 7.0) + 12.0;
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(500),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.0})
        );

        Assert.assertEquals(7.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(12.0, optimum.getValue(), 1e-3);
    }

    @Test
    public void testHighDimensionRosenbrock() {
        final int dim = 4;
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (int i = 0; i < dim - 1; i++) {
                    double a = x[i + 1] - x[i] * x[i];
                    double b = 1.0 - x[i];
                    sum += 100.0 * a * a + b * b;
                }
                return sum;
            }
        };

        double[] init = new double[dim];
        for (int i = 0; i < dim; i++) {
            init[i] = (i % 2 == 0) ? -1.0 : 1.0;
        }

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(10000),
                new MaxIter(1000),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(init)
        );

        for (int i = 0; i < dim; i++) {
            Assert.assertEquals(1.0, optimum.getPoint()[i], 0.05);
        }
        Assert.assertEquals(0.0, optimum.getValue(), 0.05);
    }
}
