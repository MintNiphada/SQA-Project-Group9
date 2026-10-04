package org.apache.commons.math3.optimization.univariate;

import org.junit.Test;
import org.junit.Assert;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import java.lang.reflect.Method;

public class BrentOptimizerTest {

    private static final double MIN_REL = 2 * Math.ulp(1d);

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorRelTooSmall() {
        new BrentOptimizer(MIN_REL - 1e-15, 1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsNotPositive() {
        new BrentOptimizer(1e-4, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsNegative() {
        new BrentOptimizer(1e-4, -1.0);
    }

    @Test
    public void testConstructorValid() {
        new BrentOptimizer(1e-4, 1e-6);
    }

    @Test
    public void testConstructorWithChecker() {
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return false;
            }
        };
        new BrentOptimizer(1e-4, 1e-6, checker);
    }

    @Test
    public void testMinimizeQuadratic() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 3) * (x - 3) + 2;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0, -10, 10);
        Assert.assertEquals(3.0, result.getPoint(), 1e-8);
        Assert.assertEquals(2.0, result.getValue(), 1e-8);
    }

    @Test
    public void testMaximizeQuadratic() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -x * x + 4;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0, -5, 5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
        Assert.assertEquals(4.0, result.getValue(), 1e-8);
    }

    @Test
    public void testBoundsSwapped() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0, 5, -5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
    }

    @Test
    public void testConvergenceCheckerStopsEarly() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 2;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0, -10, 10);
        Assert.assertTrue(result.getPoint() != 0.0);
    }

    @Test
    public void testParabolicInterpolationStep() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1) * (x - 2) * (x - 3);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 2.5, 0, 4);
        Assert.assertTrue(result.getPoint() > 1.5 && result.getPoint() < 2.5);
    }

    @Test
    public void testGoldenSectionStep() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.abs(x) + 1;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 1, -5, 5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
    }

    @Test
    public void testBestBothNull() throws Exception {
        Method best = BrentOptimizer.class.getDeclaredMethod("best", UnivariatePointValuePair.class, UnivariatePointValuePair.class, boolean.class);
        best.setAccessible(true);
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        Object result = best.invoke(optimizer, null, null, true);
        Assert.assertNull(result);
    }

    @Test
    public void testBestFirstNull() throws Exception {
        Method best = BrentOptimizer.class.getDeclaredMethod("best", UnivariatePointValuePair.class, UnivariatePointValuePair.class, boolean.class);
        best.setAccessible(true);
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        UnivariatePointValuePair b = new UnivariatePointValuePair(1.0, 2.0);
        Object result = best.invoke(optimizer, null, b, true);
        Assert.assertSame(b, result);
    }

    @Test
    public void testBestSecondNull() throws Exception {
        Method best = BrentOptimizer.class.getDeclaredMethod("best", UnivariatePointValuePair.class, UnivariatePointValuePair.class, boolean.class);
        best.setAccessible(true);
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 2.0);
        Object result = best.invoke(optimizer, a, null, true);
        Assert.assertSame(a, result);
    }

    @Test
    public void testBestMinimization() throws Exception {
        Method best = BrentOptimizer.class.getDeclaredMethod("best", UnivariatePointValuePair.class, UnivariatePointValuePair.class, boolean.class);
        best.setAccessible(true);
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 5.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 3.0);
        Object result = best.invoke(optimizer, a, b, true);
        Assert.assertSame(b, result);
    }

    @Test
    public void testBestMaximization() throws Exception {
        Method best = BrentOptimizer.class.getDeclaredMethod("best", UnivariatePointValuePair.class, UnivariatePointValuePair.class, boolean.class);
        best.setAccessible(true);
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 5.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 3.0);
        Object result = best.invoke(optimizer, a, b, false);
        Assert.assertSame(a, result);
    }

    @Test
    public void testUpdateLogicFuLessEqualFx() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.5, -1, 1);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
    }

    @Test
    public void testUpdateLogicFuGreaterFx() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5) + 1;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, -1, 1);
        Assert.assertEquals(0.5, result.getPoint(), 1e-8);
    }

    @Test
    public void testStoppingCriterion() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0, -1, 1);
        Assert.assertTrue(result.getPoint() >= -1 && result.getPoint() <= 1);
    }
}
