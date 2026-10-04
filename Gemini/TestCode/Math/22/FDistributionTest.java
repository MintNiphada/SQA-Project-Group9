package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link FDistribution}.
 */
public class FDistributionTest {

    private static final double TOLERANCE = 1e-7;

    @Test
    public void testConstructorValidParameters() {
        FDistribution dist = new FDistribution(5.0, 6.0);
        Assert.assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(6.0, dist.getDenominatorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), TOLERANCE);

        FDistribution dist2 = new FDistribution(10.0, 12.0, 1e-6);
        Assert.assertEquals(10.0, dist2.getNumeratorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(12.0, dist2.getDenominatorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(1e-6, dist2.getSolverAbsoluteAccuracy(), TOLERANCE);

        Well19937c rng = new Well19937c(1234567L);
        FDistribution dist3 = new FDistribution(rng, 8.0, 9.0, 1e-8);
        Assert.assertEquals(8.0, dist3.getNumeratorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(9.0, dist3.getDenominatorDegreesOfFreedom(), TOLERANCE);
        Assert.assertEquals(1e-8, dist3.getSolverAbsoluteAccuracy(), TOLERANCE);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroNumeratorDF() {
        new FDistribution(0.0, 5.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeNumeratorDF() {
        new FDistribution(-1.0, 5.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroDenominatorDF() {
        new FDistribution(5.0, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeDenominatorDF() {
        new FDistribution(5.0, -2.5);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithAccuracyZeroNumeratorDF() {
        new FDistribution(0.0, 5.0, 1e-9);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithAccuracyZeroDenominatorDF() {
        new FDistribution(5.0, 0.0, 1e-9);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithRngZeroNumeratorDF() {
        new FDistribution(new Well19937c(), 0.0, 5.0, 1e-9);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorWithRngZeroDenominatorDF() {
        new FDistribution(new Well19937c(), 5.0, 0.0, 1e-9);
    }

    @Test
    public void testDensity() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        // Density of F(1, 1) at x = 1 is 1 / (pi * sqrt(1) * (1 + 1)) = 1 / (2 * pi) ~ 0.15915494309189535
        Assert.assertEquals(1.0 / (2.0 * Math.PI), dist.density(1.0), TOLERANCE);

        FDistribution dist2 = new FDistribution(5.0, 2.0);
        Assert.assertTrue(dist2.density(2.0) > 0.0);
    }

    @Test
    public void testCumulativeProbability() {
        FDistribution dist = new FDistribution(5.0, 10.0);

        Assert.assertEquals(0.0, dist.cumulativeProbability(-1.0), 0.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(0.0), 0.0);

        double prob = dist.cumulativeProbability(1.0);
        Assert.assertTrue(prob > 0.0 && prob < 1.0);

        // F(1, 1) median is 1.0, so cumulative probability at 1.0 is 0.5
        FDistribution distF11 = new FDistribution(1.0, 1.0);
        Assert.assertEquals(0.5, distF11.cumulativeProbability(1.0), TOLERANCE);
    }

    @Test
    public void testInverseCumulativeProbability() {
        FDistribution dist = new FDistribution(5.0, 10.0);

        Assert.assertEquals(0.0, dist.inverseCumulativeProbability(0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);

        double p = 0.5;
        double x = dist.inverseCumulativeProbability(p);
        Assert.assertEquals(p, dist.cumulativeProbability(x), 1e-5);
    }

    @Test
    public void testGetNumericalMean() {
        // Denominator <= 2: Mean should be NaN
        FDistribution dist1 = new FDistribution(5.0, 1.0);
        Assert.assertTrue(Double.isNaN(dist1.getNumericalMean()));

        FDistribution dist2 = new FDistribution(5.0, 2.0);
        Assert.assertTrue(Double.isNaN(dist2.getNumericalMean()));

        // Denominator > 2: Mean is b / (b - 2)
        FDistribution dist3 = new FDistribution(5.0, 3.0);
        Assert.assertEquals(3.0 / (3.0 - 2.0), dist3.getNumericalMean(), TOLERANCE);

        FDistribution dist4 = new FDistribution(10.0, 12.0);
        Assert.assertEquals(12.0 / 10.0, dist4.getNumericalMean(), TOLERANCE);
    }

    @Test
    public void testGetNumericalVariance() {
        // Denominator <= 4: Variance should be NaN
        FDistribution dist1 = new FDistribution(5.0, 2.0);
        Assert.assertTrue(Double.isNaN(dist1.getNumericalVariance()));

        FDistribution dist2 = new FDistribution(5.0, 4.0);
        Assert.assertTrue(Double.isNaN(dist2.getNumericalVariance()));

        // Denominator > 4: Variance is [2 * b^2 * (a + b - 2)] / [a * (b - 2)^2 * (b - 4)]
        FDistribution dist3 = new FDistribution(10.0, 12.0);
        double expectedVariance = (2.0 * (12.0 * 12.0) * (10.0 + 12.0 - 2.0)) /
                (10.0 * ((12.0 - 2.0) * (12.0 - 2.0)) * (12.0 - 4.0));
        Assert.assertEquals(expectedVariance, dist3.getNumericalVariance(), TOLERANCE);

        // Verify caching: calling getNumericalVariance multiple times returns cached value
        Assert.assertEquals(expectedVariance, dist3.getNumericalVariance(), TOLERANCE);

        FDistribution dist4 = new FDistribution(5.0, 5.0);
        double expectedVar55 = (2.0 * 25.0 * (5.0 + 5.0 - 2.0)) / (5.0 * 9.0 * 1.0);
        Assert.assertEquals(expectedVar55, dist4.getNumericalVariance(), TOLERANCE);
    }

    @Test
    public void testSupportBoundsAndCharacteristics() {
        FDistribution dist = new FDistribution(5.0, 10.0);

        Assert.assertEquals(0.0, dist.getSupportLowerBound(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), 0.0);
        Assert.assertTrue(dist.isSupportLowerBoundInclusive());
        Assert.assertFalse(dist.isSupportUpperBoundInclusive());
        Assert.assertTrue(dist.isSupportConnected());
    }

    @Test
    public void testSample() {
        FDistribution dist = new FDistribution(new Well19937c(42L), 10.0, 10.0, 1e-9);
        double sample = dist.sample();
        Assert.assertTrue(sample > 0.0);
        Assert.assertFalse(Double.isInfinite(sample));
        Assert.assertFalse(Double.isNaN(sample));

        double[] samples = dist.sample(10);
        Assert.assertEquals(10, samples.length);
        for (double s : samples) {
            Assert.assertTrue(s > 0.0);
        }
    }
}
