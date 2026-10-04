package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateFunction;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class BOBYQAOptimizerTest {

    private static class Sphere implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    private static class Linear2D implements MultivariateFunction {
        public double value(double[] point) {
            return 2.0 * point[0] + 3.0 * point[1];
        }
    }

    private static class Rosenbrock implements MultivariateFunction {
        public double value(double[] x) {
            double f0 = x[1] - x[0] * x[0];
            double f1 = 1.0 - x[0];
            return 100.0 * f0 * f0 + f1 * f1;
        }
    }

    private static class DiffPow implements MultivariateFunction {
        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length; i++) {
                f += Math.pow(Math.abs(x[i]), 2.0 + 4.0 * i / (x.length - 1.0));
            }
            return f;
        }
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testDimensionTooSmall() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, new double[] { 1.0 }, new double[] { 0.0 }, new double[] { 2.0 });
    }

    @Test(expected = OutOfRangeException.class)
    public void testInterpolationPointsTooSmall() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, new double[] { 1.0, 1.0 }, new double[] { 0.0, 0.0 }, new double[] { 2.0, 2.0 });
    }

    @Test(expected = OutOfRangeException.class)
    public void testInterpolationPointsTooLarge() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, new double[] { 1.0, 1.0 }, new double[] { 0.0, 0.0 }, new double[] { 2.0, 2.0 });
    }

    @Test
    public void testMinimizeSphere() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 1.0, 2.0 },
            new double[] { -5.0, -5.0 },
            new double[] { 5.0, 5.0 }
        );
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testMaximizeLinearBound() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 1.0, 1e-4);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Linear2D(),
            GoalType.MAXIMIZE,
            new double[] { 0.5, 0.5 },
            new double[] { 0.0, 0.0 },
            new double[] { 2.0, 3.0 }
        );
        Assert.assertEquals(13.0, result.getValue(), 1e-2);
        Assert.assertEquals(2.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(3.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testRosenbrock2D() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 2.0, 1e-6);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Rosenbrock(),
            GoalType.MINIMIZE,
            new double[] { -1.2, 1.0 },
            new double[] { -2.0, -2.0 },
            new double[] { 2.0, 2.0 }
        );
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testStartAtLowerBound() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 1.0, 1e-5);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 1.0, 1.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 5.0, 5.0 }
        );
        Assert.assertEquals(2.0, result.getValue(), 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testStartAtUpperBound() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 1.0, 1e-5);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 5.0, 5.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 5.0, 5.0 }
        );
        Assert.assertEquals(2.0, result.getValue(), 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testStartNearLowerBound() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 2.0, 1e-5);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 1.5, 1.5 },
            new double[] { 1.0, 1.0 },
            new double[] { 10.0, 10.0 }
        );
        Assert.assertEquals(2.0, result.getValue(), 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testStartNearUpperBound() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 2.0, 1e-5);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 9.5, 9.5 },
            new double[] { 1.0, 1.0 },
            new double[] { 10.0, 10.0 }
        );
        Assert.assertEquals(2.0, result.getValue(), 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testDiffPow3D() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(9);
        RealPointValuePair result = optimizer.optimize(
            2000,
            new DiffPow(),
            GoalType.MINIMIZE,
            new double[] { 0.5, 0.5, 0.5 },
            new double[] { -2.0, -2.0, -2.0 },
            new double[] { 2.0, 2.0, 2.0 }
        );
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[2], 1e-2);
    }

    @Test
    public void testSmallBoundDifference() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 10.0, 1e-6);
        RealPointValuePair result = optimizer.optimize(
            1000,
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 0.5, 0.5 },
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 }
        );
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(2, BOBYQAOptimizer.MINIMUM_PROBLEM_DIMENSION);
        Assert.assertEquals(10.0, BOBYQAOptimizer.DEFAULT_INITIAL_RADIUS, 1e-15);
        Assert.assertEquals(1e-8, BOBYQAOptimizer.DEFAULT_STOPPING_RADIUS, 1e-15);
    }
}
