package org.apache.commons.math3.optimization.univariate;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.ConvergenceChecker;

public class BrentOptimizerTest {

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorInvalidRel() {
        new BrentOptimizer(1e-16, 1e-6, null);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorInvalidAbsZero() {
        new BrentOptimizer(1e-14, 0, null);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorInvalidAbsNegative() {
        new BrentOptimizer(1e-14, -1, null);
    }

    @Test
    public void testConstructorValid() {
        new BrentOptimizer(1e-14, 1e-6, null);
    }

    @Test
    public void testConstructorWithoutChecker() {
        new BrentOptimizer(1e-14, 1e-6);
    }

    @Test
    public void testMinimizeQuadratic() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0.5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testMaximizeQuadratic() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return -x * x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MAXIMIZE, -1, 1, 0.5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testMinimizeWithLoGreaterThanHi() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, 1, -1, 0);
        Assert.assertEquals(0.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testConvergenceChecker() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * x; }
        };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 5;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6, checker);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0.5);
        Assert.assertTrue(result.getPoint() >= -1 && result.getPoint() <= 1);
    }

    @Test
    public void testBestBothNull() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.best(null, null, true);
        Assert.assertNull(result);
    }

    @Test
    public void testBestFirstNull() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair b = new UnivariatePointValuePair(1.0, 2.0);
        UnivariatePointValuePair result = optimizer.best(null, b, true);
        Assert.assertSame(b, result);
    }

    @Test
    public void testBestSecondNull() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 2.0);
        UnivariatePointValuePair result = optimizer.best(a, null, true);
        Assert.assertSame(a, result);
    }

    @Test
    public void testBestMinimization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 2.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 1.0);
        UnivariatePointValuePair result = optimizer.best(a, b, true);
        Assert.assertSame(b, result);
    }

    @Test
    public void testBestMaximization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 2.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 1.0);
        UnivariatePointValuePair result = optimizer.best(a, b, false);
        Assert.assertSame(a, result);
    }

    @Test
    public void testBestEqualValues() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 2.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 2.0);
        UnivariatePointValuePair result = optimizer.best(a, b, true);
        Assert.assertSame(a, result);
    }

    @Test
    public void testConstantFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return 5.0; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0.5);
        Assert.assertEquals(5.0, result.getValue(), 1e-15);
    }

    @Test
    public void testLinearFunctionMinimize() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0);
        Assert.assertEquals(-1.0, result.getPoint(), 1e-6);
        Assert.assertEquals(-1.0, result.getValue(), 1e-6);
    }

    @Test
    public void testLinearFunctionMaximize() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MAXIMIZE, -1, 1, 0);
        Assert.assertEquals(1.0, result.getPoint(), 1e-6);
        Assert.assertEquals(1.0, result.getValue(), 1e-6);
    }

    @Test
    public void testSinFunctionMinimize() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.sin(x); }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-6);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, 0, 2 * Math.PI, Math.PI);
        Assert.assertEquals(-1.0, result.getValue(), 1e-6);
        Assert.assertTrue(result.getPoint() > 4.0 && result.getPoint() < 5.0);
    }

    @Test
    public void testToleranceAchieved() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * x; }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-14, 1e-8);
        UnivariatePointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0.5);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
    }
}
