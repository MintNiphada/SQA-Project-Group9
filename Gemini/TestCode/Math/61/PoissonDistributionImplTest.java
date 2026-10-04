package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class PoissonDistributionImplTest {

    private static final double TOLERANCE = 1E-12;

    @Test
    public void testConstructors() {
        PoissonDistributionImpl dist1 = new PoissonDistributionImpl(5.0);
        Assert.assertEquals(5.0, dist1.getMean(), TOLERANCE);

        PoissonDistributionImpl dist2 = new PoissonDistributionImpl(5.0, 1e-10);
        Assert.assertEquals(5.0, dist2.getMean(), TOLERANCE);

        PoissonDistributionImpl dist3 = new PoissonDistributionImpl(5.0, 500000);
        Assert.assertEquals(5.0, dist3.getMean(), TOLERANCE);

        PoissonDistributionImpl dist4 = new PoissonDistributionImpl(5.0, 1e-10, 500000);
        Assert.assertEquals(5.0, dist4.getMean(), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMean() {
        new PoissonDistributionImpl(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMean() {
        new PoissonDistributionImpl(-1.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMeanWithEpsilonAndIterations() {
        new PoissonDistributionImpl(-1.0, 1e-10, 100);
    }

    @Test
    public void testProbability() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(4.0);

        Assert.assertEquals(0.0, dist.probability(-1), TOLERANCE);
        Assert.assertEquals(0.0, dist.probability(-100), TOLERANCE);
        Assert.assertEquals(0.0, dist.probability(Integer.MAX_VALUE), TOLERANCE);

        Assert.assertEquals(FastMath.exp(-4.0), dist.probability(0), TOLERANCE);

        double expectedP1 = FastMath.exp(-4.0) * 4.0;
        Assert.assertEquals(expectedP1, dist.probability(1), 1e-10);

        double expectedP4 = FastMath.exp(-4.0) * FastMath.pow(4.0, 4) / 24.0;
        Assert.assertEquals(expectedP4, dist.probability(4), 1e-10);

        double expectedP10 = FastMath.exp(-4.0) * FastMath.pow(4.0, 10) / 3628800.0;
        Assert.assertEquals(expectedP10, dist.probability(10), 1e-10);
    }

    @Test
    public void testCumulativeProbability() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);

        Assert.assertEquals(0.0, dist.cumulativeProbability(-1), TOLERANCE);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-10), TOLERANCE);
        Assert.assertEquals(1.0, dist.cumulativeProbability(Integer.MAX_VALUE), TOLERANCE);

        double cdf0 = dist.cumulativeProbability(0);
        Assert.assertEquals(dist.probability(0), cdf0, 1e-10);

        double cdf1 = dist.cumulativeProbability(1);
        Assert.assertEquals(dist.probability(0) + dist.probability(1), cdf1, 1e-10);

        double cdf2 = dist.cumulativeProbability(2);
        Assert.assertEquals(dist.probability(0) + dist.probability(1) + dist.probability(2), cdf2, 1e-10);
    }

    @Test
    public void testNormalApproximateProbability() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(100.0);
        double approx = dist.normalApproximateProbability(100);
        Assert.assertEquals(0.5, approx, 0.05);

        double approxLow = dist.normalApproximateProbability(70);
        Assert.assertTrue(approxLow > 0.0 && approxLow < 0.5);

        double approxHigh = dist.normalApproximateProbability(130);
        Assert.assertTrue(approxHigh > 0.5 && approxHigh < 1.0);
    }

    @Test
    public void testSample() throws MathException {
        PoissonDistributionImpl distSmall = new PoissonDistributionImpl(4.0);
        distSmall.reseedRandomGenerator(42L);
        int sampleSmall = distSmall.sample();
        Assert.assertTrue(sampleSmall >= 0);

        PoissonDistributionImpl distLarge = new PoissonDistributionImpl(100.0);
        distLarge.reseedRandomGenerator(42L);
        int sampleLarge = distLarge.sample();
        Assert.assertTrue(sampleLarge >= 0);
    }

    @Test
    public void testDomainBounds() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);
        Assert.assertEquals(0, dist.getDomainLowerBound(0.5));
        Assert.assertEquals(0, dist.getDomainLowerBound(0.0));
        Assert.assertEquals(0, dist.getDomainLowerBound(1.0));

        Assert.assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(0.5));
        Assert.assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(0.0));
        Assert.assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(1.0));
    }

    @Test
    public void testInverseCumulativeProbability() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);

        Assert.assertEquals(-1, dist.inverseCumulativeProbability(0.0));
        Assert.assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0));

        double cdf3 = dist.cumulativeProbability(3);
        Assert.assertEquals(3, dist.inverseCumulativeProbability(cdf3));

        double cdf5 = dist.cumulativeProbability(5);
        Assert.assertEquals(5, dist.inverseCumulativeProbability(cdf5));
    }
}
