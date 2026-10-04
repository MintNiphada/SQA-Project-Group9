package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link UniformRealDistribution}.
 */
public class UniformRealDistributionTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    public void testDefaultConstructor() {
        UniformRealDistribution dist = new UniformRealDistribution();
        Assert.assertEquals(0.0, dist.getSupportLowerBound(), TOLERANCE);
        Assert.assertEquals(1.0, dist.getSupportUpperBound(), TOLERANCE);
        Assert.assertEquals(0.5, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(1.0 / 12.0, dist.getNumericalVariance(), TOLERANCE);
        Assert.assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), TOLERANCE);
    }

    @Test
    public void testTwoArgConstructor() {
        UniformRealDistribution dist = new UniformRealDistribution(-2.0, 5.0);
        Assert.assertEquals(-2.0, dist.getSupportLowerBound(), TOLERANCE);
        Assert.assertEquals(5.0, dist.getSupportUpperBound(), TOLERANCE);
        Assert.assertEquals(1.5, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(49.0 / 12.0, dist.getNumericalVariance(), TOLERANCE);
        Assert.assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), TOLERANCE);
    }

    @Test
    public void testThreeArgConstructor() {
        double customAccuracy = 1e-6;
        UniformRealDistribution dist = new UniformRealDistribution(1.0, 3.0, customAccuracy);
        Assert.assertEquals(1.0, dist.getSupportLowerBound(), TOLERANCE);
        Assert.assertEquals(3.0, dist.getSupportUpperBound(), TOLERANCE);
        Assert.assertEquals(2.0, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(4.0 / 12.0, dist.getNumericalVariance(), TOLERANCE);
        Assert.assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), TOLERANCE);
    }

    @Test
    public void testFourArgConstructor() {
        RandomGenerator rng = new Well19937c(12345L);
        double customAccuracy = 1e-5;
        UniformRealDistribution dist = new UniformRealDistribution(rng, 10.0, 20.0, customAccuracy);
        Assert.assertEquals(10.0, dist.getSupportLowerBound(), TOLERANCE);
        Assert.assertEquals(20.0, dist.getSupportUpperBound(), TOLERANCE);
        Assert.assertEquals(15.0, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(100.0 / 12.0, dist.getNumericalVariance(), TOLERANCE);
        Assert.assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), TOLERANCE);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerEqualsUpper() {
        new UniformRealDistribution(2.0, 2.0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpper() {
        new UniformRealDistribution(5.0, 2.0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpperThreeArgs() {
        new UniformRealDistribution(5.0, 2.0, 1e-6);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpperFourArgs() {
        new UniformRealDistribution(new Well19937c(), 5.0, 2.0, 1e-6);
    }

    @Test
    public void testDensity() {
        double lower = 2.0;
        double upper = 6.0;
        double expectedDensity = 1.0 / (upper - lower);
        UniformRealDistribution dist = new UniformRealDistribution(lower, upper);

        // Outside support
        Assert.assertEquals(0.0, dist.density(1.99), TOLERANCE);
        Assert.assertEquals(0.0, dist.density(0.0), TOLERANCE);
        Assert.assertEquals(0.0, dist.density(-10.0), TOLERANCE);
        Assert.assertEquals(0.0, dist.density(6.01), TOLERANCE);
        Assert.assertEquals(0.0, dist.density(100.0), TOLERANCE);

        // On boundaries
        Assert.assertEquals(expectedDensity, dist.density(lower), TOLERANCE);
        Assert.assertEquals(expectedDensity, dist.density(upper), TOLERANCE);

        // Inside support
        Assert.assertEquals(expectedDensity, dist.density(3.0), TOLERANCE);
        Assert.assertEquals(expectedDensity, dist.density(4.0), TOLERANCE);
        Assert.assertEquals(expectedDensity, dist.density(5.999), TOLERANCE);
    }

    @Test
    public void testCumulativeProbability() {
        double lower = -3.0;
        double upper = 5.0;
        UniformRealDistribution dist = new UniformRealDistribution(lower, upper);

        // Below lower bound
        Assert.assertEquals(0.0, dist.cumulativeProbability(-10.0), TOLERANCE);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-3.1), TOLERANCE);
        Assert.assertEquals(0.0, dist.cumulativeProbability(lower), TOLERANCE);

        // Inside support
        Assert.assertEquals(0.25, dist.cumulativeProbability(-1.0), TOLERANCE);
        Assert.assertEquals(0.5, dist.cumulativeProbability(1.0), TOLERANCE);
        Assert.assertEquals(0.75, dist.cumulativeProbability(3.0), TOLERANCE);

        // Above upper bound
        Assert.assertEquals(1.0, dist.cumulativeProbability(upper), TOLERANCE);
        Assert.assertEquals(1.0, dist.cumulativeProbability(5.1), TOLERANCE);
        Assert.assertEquals(1.0, dist.cumulativeProbability(100.0), TOLERANCE);
    }

    @Test
    public void testInverseCumulativeProbability() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 10.0);

        Assert.assertEquals(2.0, dist.inverseCumulativeProbability(0.0), TOLERANCE);
        Assert.assertEquals(4.0, dist.inverseCumulativeProbability(0.25), TOLERANCE);
        Assert.assertEquals(6.0, dist.inverseCumulativeProbability(0.5), TOLERANCE);
        Assert.assertEquals(8.0, dist.inverseCumulativeProbability(0.75), TOLERANCE);
        Assert.assertEquals(10.0, dist.inverseCumulativeProbability(1.0), TOLERANCE);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbabilityNegative() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbabilityGreaterThanOne() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        dist.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testSupportProperties() {
        UniformRealDistribution dist = new UniformRealDistribution(3.5, 7.5);
        Assert.assertEquals(3.5, dist.getSupportLowerBound(), TOLERANCE);
        Assert.assertEquals(7.5, dist.getSupportUpperBound(), TOLERANCE);
        Assert.assertTrue(dist.isSupportLowerBoundInclusive());
        Assert.assertFalse(dist.isSupportUpperBoundInclusive());
        Assert.assertTrue(dist.isSupportConnected());
    }

    @Test
    public void testMoments() {
        UniformRealDistribution dist = new UniformRealDistribution(-10.0, 10.0);
        Assert.assertEquals(0.0, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(400.0 / 12.0, dist.getNumericalVariance(), TOLERANCE);

        UniformRealDistribution dist2 = new UniformRealDistribution(0.0, 12.0);
        Assert.assertEquals(6.0, dist2.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(144.0 / 12.0, dist2.getNumericalVariance(), TOLERANCE);
    }

    @Test
    public void testSample() {
        final double lower = 5.0;
        final double upper = 15.0;
        RandomGenerator rng = new Well19937c(42L);
        UniformRealDistribution dist = new UniformRealDistribution(rng, lower, upper, UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        final int sampleSize = 1000;
        double sum = 0;
        for (int i = 0; i < sampleSize; i++) {
            double sample = dist.sample();
            Assert.assertTrue("Sample " + sample + " should be >= " + lower, sample >= lower);
            Assert.assertTrue("Sample " + sample + " should be <= " + upper, sample <= upper);
            sum += sample;
        }

        double mean = sum / sampleSize;
        Assert.assertEquals(dist.getNumericalMean(), mean, 0.5);
    }

    @Test
    public void testSampleArray() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        double[] samples = dist.sample(100);
        Assert.assertEquals(100, samples.length);
        for (double sample : samples) {
            Assert.assertTrue(sample >= 0.0 && sample <= 1.0);
        }
    }

    @Test
    public void testExtremeValues() {
        UniformRealDistribution dist = new UniformRealDistribution(-1e10, 1e10);
        Assert.assertEquals(0.0, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(0.5, dist.cumulativeProbability(0.0), TOLERANCE);
        Assert.assertEquals(1.0 / (2e10), dist.density(0.0), TOLERANCE);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-1e10), TOLERANCE);
        Assert.assertEquals(1.0, dist.cumulativeProbability(1e10), TOLERANCE);
    }

    @Test
    public void testSmallInterval() {
        UniformRealDistribution dist = new UniformRealDistribution(1.0, 1.000001);
        Assert.assertEquals(1.0000005, dist.getNumericalMean(), TOLERANCE);
        Assert.assertEquals(1000000.0, dist.density(1.0000005), 1e-4);
        Assert.assertEquals(0.5, dist.cumulativeProbability(1.0000005), TOLERANCE);
    }
}
