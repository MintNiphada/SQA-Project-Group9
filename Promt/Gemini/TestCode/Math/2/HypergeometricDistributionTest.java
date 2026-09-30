package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link HypergeometricDistribution}.
 */
public class HypergeometricDistributionTest extends IntegerDistributionAbstractTest {

    private static final double DEFAULT_EPSILON = 1e-11;

    // --------------------- Abstract Test Framework Methods ---------------------

    @Override
    public IntegerDistribution makeDistribution() {
        return new HypergeometricDistribution(10, 5, 4);
    }

    @Override
    public int[] makeDensityTestPoints() {
        return new int[] { -1, 0, 1, 2, 3, 4, 5, 10 };
    }

    @Override
    public double[] makeDensityTestValues() {
        return new double[] {
            0.0,
            5.0 / 210.0,
            50.0 / 210.0,
            100.0 / 210.0,
            50.0 / 210.0,
            5.0 / 210.0,
            0.0,
            0.0
        };
    }

    @Override
    public int[] makeCumulativeTestPoints() {
        return new int[] { -1, 0, 1, 2, 3, 4, 5, 10 };
    }

    @Override
    public double[] makeCumulativeTestValues() {
        return new double[] {
            0.0,
            5.0 / 210.0,
            55.0 / 210.0,
            155.0 / 210.0,
            205.0 / 210.0,
            1.0,
            1.0,
            1.0
        };
    }

    @Override
    public double[] makeInverseCumulativeTestPoints() {
        return new double[] { 0.001, 0.010, 0.025, 0.050, 0.100, 0.900, 0.950, 0.975, 0.990, 0.999 };
    }

    @Override
    public int[] makeInverseCumulativeTestValues() {
        return new int[] { 0, 0, 1, 1, 1, 3, 3, 3, 4, 4 };
    }

    // --------------------- Constructor Validation Tests ---------------------

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorPreconditionsPopulationSizeZero() {
        new HypergeometricDistribution(0, 0, 0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorPreconditionsPopulationSizeNegative() {
        new HypergeometricDistribution(-10, 5, 3);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorPreconditionsNegativeSuccesses() {
        new HypergeometricDistribution(10, -1, 3);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorPreconditionsNegativeSampleSize() {
        new HypergeometricDistribution(10, 5, -1);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorPreconditionsSuccessesGreaterThanPopulation() {
        new HypergeometricDistribution(10, 11, 3);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorPreconditionsSampleSizeGreaterThanPopulation() {
        new HypergeometricDistribution(10, 5, 11);
    }

    @Test
    public void testConstructorWithRng() {
        HypergeometricDistribution dist = new HypergeometricDistribution(new Well19937c(123456L), 20, 10, 5);
        Assert.assertEquals(20, dist.getPopulationSize());
        Assert.assertEquals(10, dist.getNumberOfSuccesses());
        Assert.assertEquals(5, dist.getSampleSize());
    }

    // --------------------- Getters and Support Tests ---------------------

    @Test
    public void testGetters() {
        HypergeometricDistribution dist = new HypergeometricDistribution(100, 30, 20);
        Assert.assertEquals(100, dist.getPopulationSize());
        Assert.assertEquals(30, dist.getNumberOfSuccesses());
        Assert.assertEquals(20, dist.getSampleSize());
    }

    @Test
    public void testSupportBoundsAndConnected() {
        // Support lower bound: max(0, n + m - N)
        // Case 1: n + m <= N -> lower bound = 0
        HypergeometricDistribution dist1 = new HypergeometricDistribution(100, 30, 20);
        Assert.assertEquals(0, dist1.getSupportLowerBound());
        Assert.assertEquals(20, dist1.getSupportUpperBound());
        Assert.assertTrue(dist1.isSupportConnected());

        // Case 2: n + m > N -> lower bound = n + m - N
        HypergeometricDistribution dist2 = new HypergeometricDistribution(10, 7, 8);
        Assert.assertEquals(5, dist2.getSupportLowerBound());
        Assert.assertEquals(7, dist2.getSupportUpperBound());
        Assert.assertTrue(dist2.isSupportConnected());

        // Case 3: m < n vs m > n upper bound
        HypergeometricDistribution dist3 = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(3, dist3.getSupportUpperBound());

        HypergeometricDistribution dist4 = new HypergeometricDistribution(10, 6, 4);
        Assert.assertEquals(4, dist4.getSupportUpperBound());
    }

    // --------------------- Numerical Mean and Variance Tests ---------------------

    @Test
    public void testNumericalMeanAndVariance() {
        int N = 100;
        int m = 30;
        int n = 20;
        HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);

        double expectedMean = (double) (n * m) / (double) N;
        Assert.assertEquals(expectedMean, dist.getNumericalMean(), DEFAULT_EPSILON);

        double expectedVariance = (double) (n * m * (N - n) * (N - m)) / (double) (N * N * (N - 1));
        Assert.assertEquals(expectedVariance, dist.getNumericalVariance(), DEFAULT_EPSILON);
        // Test caching of variance
        Assert.assertEquals(expectedVariance, dist.getNumericalVariance(), DEFAULT_EPSILON);

        HypergeometricDistribution distDegenerate = new HypergeometricDistribution(1, 1, 1);
        Assert.assertEquals(1.0, distDegenerate.getNumericalMean(), DEFAULT_EPSILON);
    }

    // --------------------- Probability and Cumulative Tests ---------------------

    @Test
    public void testProbabilityOutsideDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 7, 8);
        // Domain lower bound is max(0, 7 - (10 - 8)) = 5
        // Domain upper bound is min(7, 8) = 7
        Assert.assertEquals(0.0, dist.probability(4), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.probability(8), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.probability(-1), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.probability(15), DEFAULT_EPSILON);

        // Sum of valid domain probabilities must be 1.0
        double sum = dist.probability(5) + dist.probability(6) + dist.probability(7);
        Assert.assertEquals(1.0, sum, DEFAULT_EPSILON);
    }

    @Test
    public void testCumulativeProbability() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 7, 8);
        // Domain is [5, 7]
        Assert.assertEquals(0.0, dist.cumulativeProbability(4), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.cumulativeProbability(0), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-5), DEFAULT_EPSILON);

        Assert.assertEquals(1.0, dist.cumulativeProbability(7), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, dist.cumulativeProbability(8), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, dist.cumulativeProbability(15), DEFAULT_EPSILON);

        double p5 = dist.probability(5);
        double p6 = dist.probability(6);
        Assert.assertEquals(p5, dist.cumulativeProbability(5), DEFAULT_EPSILON);
        Assert.assertEquals(p5 + p6, dist.cumulativeProbability(6), DEFAULT_EPSILON);
    }

    @Test
    public void testUpperCumulativeProbability() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 7, 8);
        // Domain is [5, 7]
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(5), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(4), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(0), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(-2), DEFAULT_EPSILON);

        Assert.assertEquals(0.0, dist.upperCumulativeProbability(8), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, dist.upperCumulativeProbability(15), DEFAULT_EPSILON);

        double p6 = dist.probability(6);
        double p7 = dist.probability(7);
        Assert.assertEquals(p6 + p7, dist.upperCumulativeProbability(6), DEFAULT_EPSILON);
        Assert.assertEquals(p7, dist.upperCumulativeProbability(7), DEFAULT_EPSILON);
    }

    @Test
    public void testDegenerateDistribution() {
        // Population = 10, Successes = 0, Sample = 5
        HypergeometricDistribution distNoSuccess = new HypergeometricDistribution(10, 0, 5);
        Assert.assertEquals(1.0, distNoSuccess.probability(0), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, distNoSuccess.probability(1), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, distNoSuccess.cumulativeProbability(0), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, distNoSuccess.cumulativeProbability(-1), DEFAULT_EPSILON);

        // Population = 10, Successes = 10, Sample = 5
        HypergeometricDistribution distAllSuccess = new HypergeometricDistribution(10, 10, 5);
        Assert.assertEquals(0.0, distAllSuccess.probability(4), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, distAllSuccess.probability(5), DEFAULT_EPSILON);
        Assert.assertEquals(1.0, distAllSuccess.cumulativeProbability(5), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, distAllSuccess.cumulativeProbability(4), DEFAULT_EPSILON);

        // Sample size = 0
        HypergeometricDistribution distZeroSample = new HypergeometricDistribution(10, 5, 0);
        Assert.assertEquals(1.0, distZeroSample.probability(0), DEFAULT_EPSILON);
        Assert.assertEquals(0.0, distZeroSample.probability(1), DEFAULT_EPSILON);
    }

    @Test
    public void testLargeValues() {
        // Test with large population to exercise SaddlePointExpansion
        HypergeometricDistribution dist = new HypergeometricDistribution(3000, 1200, 1000);
        double mean = dist.getNumericalMean();
        Assert.assertEquals(400.0, mean, DEFAULT_EPSILON);
        double probMean = dist.probability(400);
        Assert.assertTrue(probMean > 0.0 && probMean < 1.0);
        Assert.assertTrue(dist.cumulativeProbability(400) > 0.0);
        Assert.assertTrue(dist.upperCumulativeProbability(400) > 0.0);
    }
}
