package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.SinFunction;
import org.apache.commons.math3.analysis.QuinticFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link BrentOptimizer}.
 */
public class BrentOptimizerTest {

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorThrowsNumberIsTooSmallException() {
        new BrentOptimizer(1e-18, 1e-10);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorThrowsNotStrictlyPositiveExceptionZero() {
        new BrentOptimizer(1e-10, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorThrowsNotStrictlyPositiveExceptionNegative() {
        new BrentOptimizer(1e-10, -1.0);
    }

    @Test
    public void testSinMin() {
        UnivariateFunction f = new SinFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 4.0, 5.0);
        Assert.assertEquals(3.0 * FastMath.PI / 2.0, result.getPoint(), 1e-8);
        Assert.assertEquals(-1.0, result.getValue(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test
    public void testSinMax() {
        UnivariateFunction f = new SinFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MAXIMIZE, 1.0, 2.0);
        Assert.assertEquals(FastMath.PI / 2.0, result.getPoint(), 1e-8);
        Assert.assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test
    public void testBoundariesInverted() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.5) * (x - 2.5);
            }
        };
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        // Pass min > max to exercise a = hi, b = lo branch
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 5.0, 1.0, 4.0);
        Assert.assertEquals(2.5, result.getPoint(), 1e-8);
        Assert.assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test
    public void testQuinticMin() {
        // (x - 1)(x - 0.5)(x)(x + 0.5)(x + 1)
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -0.3, -0.2).getPoint(), 1e-8);
        Assert.assertEquals(0.82221643, optimizer.optimize(200, f, GoalType.MINIMIZE, 0.3, 0.9).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        // Maxima
        Assert.assertEquals(-0.82221643, optimizer.optimize(200, f, GoalType.MAXIMIZE, -0.9, -0.3).getPoint(), 1e-8);
        Assert.assertEquals(0.27195613, optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.2, 0.3).getPoint(), 1e-8);
    }

    @Test
    public void testConvergenceCheckerMinimization() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 1.0;
            }
        };

        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 2;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 6.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertTrue(optimizer.getEvaluations() < 10);
    }

    @Test
    public void testConvergenceCheckerMaximization() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 1.5) * (x - 1.5) + 4.0;
            }
        };

        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 3;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 5.0, 0.5);
        Assert.assertNotNull(result);
        Assert.assertTrue(optimizer.getEvaluations() < 15);
    }

    @Test
    public void testParabolaFitBranchCoverage() {
        // Step function or asymmetric quadratic to hit different branches inside doOptimize
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (x < 1.0) {
                    return (x - 1.0) * (x - 1.0) + 2.0;
                } else if (x > 3.0) {
                    return (x - 3.0) * (x - 3.0) + 2.0;
                } else {
                    return FastMath.sin(x);
                }
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-9, 1e-12);
        UnivariatePointValuePair min = optimizer.optimize(200, f, GoalType.MINIMIZE, 0.0, 4.0, 0.2);
        Assert.assertNotNull(min);

        UnivariatePointValuePair max = optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.0, 4.0, 3.8);
        Assert.assertNotNull(max);
    }

    @Test
    public void testFlatFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 42.0;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, 10.0, 0.0);
        Assert.assertEquals(42.0, result.getValue(), 1e-8);
    }

    @Test
    public void testStrictInitialGuessAtBoundaries() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        // Start near lower boundary
        UnivariatePointValuePair res1 = optimizer.optimize(100, f, GoalType.MINIMIZE, -2.0, 2.0, -1.9999);
        Assert.assertEquals(0.0, res1.getPoint(), 1e-6);

        // Start near upper boundary
        UnivariatePointValuePair res2 = optimizer.optimize(100, f, GoalType.MINIMIZE, -2.0, 2.0, 1.9999);
        Assert.assertEquals(0.0, res2.getPoint(), 1e-6);
    }

    @Test
    public void testStepNearEndpoints() {
        // A function with minimum at 0, evaluated in [-1e-5, 10]
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1e-4) * (x - 1e-4);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair res = optimizer.optimize(200, f, GoalType.MINIMIZE, -1e-4, 10.0, 5.0);
        Assert.assertEquals(1e-4, res.getPoint(), 1e-8);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluations() {
        UnivariateFunction f = new QuinticFunction();
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        optimizer.optimize(2, f, GoalType.MINIMIZE, -10.0, 10.0, 0.0);
    }

    @Test
    public void testNonSymmetricCubicFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x * x - 3 * x; // Local max at -1, local min at 1
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair min = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 2.0, 0.5);
        Assert.assertEquals(1.0, min.getPoint(), 1e-7);
        Assert.assertEquals(-2.0, min.getValue(), 1e-7);

        UnivariatePointValuePair max = optimizer.optimize(100, f, GoalType.MAXIMIZE, -2.0, 0.0, -0.5);
        Assert.assertEquals(-1.0, max.getPoint(), 1e-7);
        Assert.assertEquals(2.0, max.getValue(), 1e-7);
    }

    @Test
    public void testMinimumAtStartValue() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        Assert.assertEquals(0.0, result.getPoint(), 1e-8);
        Assert.assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test
    public void testHighOrderFunctionWithMultipleBranches() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.cos(x) + FastMath.cos(2 * x) + FastMath.sin(3 * x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        UnivariatePointValuePair min = optimizer.optimize(200, f, GoalType.MINIMIZE, 0.0, 3.0, 1.5);
        Assert.assertNotNull(min);
        UnivariatePointValuePair max = optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.0, 3.0, 1.5);
        Assert.assertNotNull(max);
    }
}
