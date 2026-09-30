package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.Assert;
import org.junit.Test;

public class SimplexOptimizerTest {

    @Test
    public void testConstructorWithChecker() {
        ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(1e-6, 1e-6);
        SimplexOptimizer optimizer = new SimplexOptimizer(checker);
        Assert.assertEquals(checker, optimizer.getConvergenceChecker());
    }

    @Test
    public void testConstructorWithThresholds() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-4, 1e-7);
        ConvergenceChecker<PointValuePair> checker = optimizer.getConvergenceChecker();
        Assert.assertTrue(checker instanceof SimpleValueChecker);
    }

    @Test(expected = NullArgumentException.class)
    public void testOptimizeMissingSimplexThrowsNullArgumentException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0 })
        );
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimizeWithLowerBoundThrowsMathUnsupportedOperationException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0 }),
            new NelderMeadSimplex(1),
            new SimpleBounds(new double[] { 0.0 }, new double[] { 2.0 })
        );
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimizeWithUpperBoundOnlyThrowsMathUnsupportedOperationException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0 }),
            new NelderMeadSimplex(1),
            new SimpleBounds(new double[] { Double.NEGATIVE_INFINITY }, new double[] { 2.0 })
        );
    }

    @Test
    public void testMinimizeWithNelderMead() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction fourExtrema = new MultivariateFunction() {
            public double value(double[] variables) {
                final double x = variables[0];
                final double y = variables[1];
                return (x - 1) * (x - 1) + (y - 2) * (y - 2);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(fourExtrema),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NelderMeadSimplex(new double[] { 0.2, 0.2 })
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-6);
    }

    @Test
    public void testMaximizeWithNelderMead() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction invertedParaboloid = new MultivariateFunction() {
            public double value(double[] variables) {
                final double x = variables[0];
                final double y = variables[1];
                return -((x - 3) * (x - 3) + (y + 4) * (y + 4)) + 10.0;
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(invertedParaboloid),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NelderMeadSimplex(new double[] { 0.5, 0.5 })
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(-4.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(10.0, optimum.getValue(), 1e-5);
    }

    @Test
    public void testMinimizeWithMultiDirectional() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-11);
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = 1 - x[0];
                double b = x[1] - x[0] * x[0];
                return a * a + 100 * b * b;
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(rosenbrock),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -1.2, 1.0 }),
            new MultiDirectionalSimplex(2)
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-3);
    }

    @Test
    public void testMaximizeWithMultiDirectional() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) {
                return 5.0 - (x[0] * x[0] + x[1] * x[1]);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(300),
            new ObjectiveFunction(func),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 2.0, -1.0 }),
            new MultiDirectionalSimplex(2)
        );

        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(5.0, optimum.getValue(), 1e-5);
    }

    @Test
    public void testReuseSimplexOnSubsequentOptimizations() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 5) * (x[0] - 5);
            }
        };

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 }),
            new NelderMeadSimplex(1)
        );

        // Run second optimization without explicitly providing simplex instance again
        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 10.0 })
        );

        Assert.assertEquals(5.0, optimum.getPoint()[0], 1e-3);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluationsException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-15, 1e-15);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 1.0) * (x[0] - 1.0);
            }
        };

        optimizer.optimize(
            new MaxEval(5),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 100.0 }),
            new NelderMeadSimplex(1)
        );
    }

    @Test
    public void testCustomConvergenceChecker() {
        ConvergenceChecker<PointValuePair> customChecker = new SimplePointChecker<PointValuePair>(1e-5, 1e-5);
        SimplexOptimizer optimizer = new SimplexOptimizer(customChecker);

        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 2.0) * (x[0] - 2.0) + (x[1] - 3.0) * (x[1] - 3.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NelderMeadSimplex(2)
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-3);
    }
}
