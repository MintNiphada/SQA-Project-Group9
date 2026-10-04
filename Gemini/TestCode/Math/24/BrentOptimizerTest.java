package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BrentOptimizerTest {

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorRelativeToleranceTooSmall() {
        new BrentOptimizer(1e-17, 1e-6);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsoluteToleranceZero() {
        new BrentOptimizer(1e-10, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorAbsoluteToleranceNegative() {
        new BrentOptimizer(1e-10, -1e-6);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructorWithCheckerRelativeToleranceTooSmall() {
        ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-6, 1e-6);
        new BrentOptimizer(1e-17, 1e-6, checker);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithCheckerAbsoluteToleranceNegative() {
        ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-6, 1e-6);
        new BrentOptimizer(1e-10, -1.0, checker);
    }

    @Test
    public void testSinMinimization() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 4.0, 5.0, 4.5);
        Assert.assertEquals(3 * FastMath.PI / 2, result.getPoint(), 1e-8);
        Assert.assertEquals(-1.0, result.getValue(), 1e-8);
    }

    @Test
    public void testSinMaximization() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 1.0, 2.0, 1.5);
        Assert.assertEquals(FastMath.PI / 2, result.getPoint(), 1e-8);
        Assert.assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test
    public void testInvertedBounds() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 2.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        // lo > hi
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 5.0, 1.0, 2.0);
        Assert.assertEquals(3.0, result.getPoint(), 1e-8);
        Assert.assertEquals(2.0, result.getValue(), 1e-8);
    }

    @Test
    public void testQuadraticParabola() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.5) * (x - 2.5) - 4.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 5.0);
        Assert.assertEquals(2.5, result.getPoint(), 1e-8);
        Assert.assertEquals(-4.0, result.getValue(), 1e-8);
    }

    @Test
    public void testMaximizationWithCustomChecker() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 1.234) * (x - 1.234) + 10.0;
            }
        };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 5;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 3.0, 0.5);
        Assert.assertNotNull(result);
        Assert.assertEquals(1.234, result.getPoint(), 0.1);
    }

    @Test
    public void testBoundaryProximityAndGoldenStep() {
        // Function with minimum very close to boundary to exercise boundary clipping and golden section branches
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.0001) * (x - 0.0001);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-9, 1e-12);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, 0.9);
        Assert.assertEquals(0.0001, result.getPoint(), 1e-4);
    }

    @Test
    public void testNonQuadraticConvex() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.cosh(x - 0.7);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -2.0, 3.0, 2.5);
        Assert.assertEquals(0.7, result.getPoint(), 1e-7);
        Assert.assertEquals(1.0, result.getValue(), 1e-7);
    }

    @Test
    public void testHighOrderPolynomial() {
        // Polynomial with multiple inflection points
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.pow(x - 1.0, 4) + FastMath.pow(x - 1.0, 2) + 5.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -5.0, 5.0, -4.0);
        Assert.assertEquals(1.0, result.getPoint(), 1e-6);
        Assert.assertEquals(5.0, result.getValue(), 1e-6);
    }

    @Test
    public void testFlatFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 42.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, 10.0, 0.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(42.0, result.getValue(), 1e-10);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluations() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        optimizer.optimize(2, f, GoalType.MINIMIZE, 0.0, 5.0, 2.5);
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
    public void testStartNearUpperBound() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, 0.999);
        Assert.assertEquals(0.5, result.getPoint(), 1e-7);
    }

    @Test
    public void testStartNearLowerBound() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, 0.001);
        Assert.assertEquals(0.5, result.getPoint(), 1e-7);
    }

    @Test
    public void testAsymmetricInterval() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 100.0) * (x - 100.0);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, -1000.0, 500.0, -500.0);
        Assert.assertEquals(100.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testQuinticPolynomialMaximization() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -FastMath.pow(x - 2.0, 4) - FastMath.pow(x - 2.0, 2) + 3.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, -5.0, 10.0, 6.0);
        Assert.assertEquals(2.0, result.getPoint(), 1e-6);
        Assert.assertEquals(3.0, result.getValue(), 1e-6);
    }
}
