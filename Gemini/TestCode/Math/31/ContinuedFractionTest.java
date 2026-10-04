package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.junit.Assert;
import org.junit.Test;

public class ContinuedFractionTest {

    @Test
    public void testGoldenRatio() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        Assert.assertEquals(expected, cf.evaluate(0.0), 1e-8);
        Assert.assertEquals(expected, cf.evaluate(0.0, 1e-9), 1e-8);
        Assert.assertEquals(expected, cf.evaluate(0.0, 100), 1e-8);
        Assert.assertEquals(expected, cf.evaluate(0.0, 1e-9, 100), 1e-8);
    }

    @Test
    public void testInitialZeroA0() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 0.0 : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (FastMath.sqrt(5.0) - 1.0) / 2.0;
        Assert.assertEquals(expected, cf.evaluate(0.0, 1e-9, 50), 1e-8);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testMaxCountExceededException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        cf.evaluate(0.0, 1e-15, 2);
    }

    @Test(expected = ConvergenceException.class)
    public void testNanDivergence() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 1) ? Double.NaN : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        cf.evaluate(0.0, 1e-9, 10);
    }

    @Test(expected = ConvergenceException.class)
    public void testInfinityDivergenceOnHN() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 1e-300 : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return (n == 1) ? Double.MAX_VALUE : 1.0;
            }
        };

        cf.evaluate(0.0, 1e-9, 10);
    }

    @Test(expected = ConvergenceException.class)
    public void testInfinityDivergenceScaleNonPositive() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? -1.0 : -Double.MAX_VALUE;
            }

            @Override
            protected double getB(int n, double x) {
                return -Double.MAX_VALUE;
            }
        };

        cf.evaluate(0.0, 1e-9, 10);
    }

    @Test
    public void testScaleRecoveryAGreaterThanB() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return Double.MAX_VALUE;
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                if (n == 1) return 10.0;
                return 1.0;
            }
        };

        double res = cf.evaluate(0.0, 1e-5, 10);
        Assert.assertFalse(Double.isNaN(res));
    }

    @Test
    public void testScaleRecoveryBGreaterThanOrEqualToA() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 10.0;
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                if (n == 1) return Double.MAX_VALUE;
                return 1.0;
            }
        };

        double res = cf.evaluate(0.0, 1e-5, 10);
        Assert.assertFalse(Double.isNaN(res));
    }

    @Test
    public void testEvaluationWithParameterX() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? x : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return x;
            }
        };

        double res = cf.evaluate(2.0, 1e-9, 50);
        Assert.assertEquals(2.0, res, 1e-8);
    }
}
