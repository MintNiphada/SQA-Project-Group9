package org.apache.commons.math3.distribution;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

public class HypergeometricDistributionTest {

    private static final double TOL = 1e-12;

    @Test
    public void testConstructorValidParameters() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertNotNull(dist);
        Assert.assertEquals(10, dist.getPopulationSize());
        Assert.assertEquals(3, dist.getNumberOfSuccesses());
        Assert.assertEquals(5, dist.getSampleSize());
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorPopulationSizeZero() {
        new HypergeometricDistribution(0, 0, 1);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorPopulationSizeNegative() {
        new HypergeometricDistribution(-1, 0, 1);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorNumberOfSuccessesNegative() {
        new HypergeometricDistribution(10, -1, 5);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorSampleSizeNegative() {
        new HypergeometricDistribution(10, 3, -1);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorNumberOfSuccessesLargerThanPopulation() {
        new HypergeometricDistribution(5, 6, 2);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorSampleSizeLargerThanPopulation() {
        new HypergeometricDistribution(5, 3, 6);
    }

    @Test
    public void testProbabilityBasic() {
        // N=10, m=3, n=5 -> support [0,3]
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(0.08333333333333333, dist.probability(0), TOL);
        Assert.assertEquals(0.4166666666666667, dist.probability(1), TOL);
        Assert.assertEquals(0.4166666666666667, dist.probability(2), TOL);
        Assert.assertEquals(0.08333333333333333, dist.probability(3), TOL);
    }

    @Test
    public void testProbabilityOutsideSupport() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(0.0, dist.probability(-1), TOL);
        Assert.assertEquals(0.0, dist.probability(4), TOL);
        Assert.assertEquals(0.0, dist.probability(-100), TOL);
        Assert.assertEquals(0.0, dist.probability(100), TOL);
    }

    @Test
    public void testProbabilityExtremeParameters() {
        // N=1, m=0, n=1 -> support [0,0]
        HypergeometricDistribution dist = new HypergeometricDistribution(1, 0, 1);
        Assert.assertEquals(1.0, dist.probability(0), TOL);
        Assert.assertEquals(0.0, dist.probability(1), TOL);
    }

    @Test
    public void testCumulativeProbability() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-1), TOL);
        Assert.assertEquals(0.08333333333333333, dist.cumulativeProbability(0), TOL);
        Assert.assertEquals(0.5, dist.cumulativeProbability(1), TOL);
        Assert.assertEquals(0.9166666666666667, dist.cumulativeProbability(2), TOL);
        Assert.assertEquals(1.0, dist.cumulativeProbability(3), TOL);
        Assert.assertEquals(1.0, dist.cumulativeProbability(4), TOL);
    }

    @Test
    public void testCumulativeProbabilityAtLowerBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        int lower = dist.getSupportLowerBound();
        Assert.assertEquals(dist.probability(lower), dist.cumulativeProbability(lower), TOL);
    }

    @Test
    public void testCumulativeProbabilityAtUpperBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        int upper = dist.getSupportUpperBound();
        Assert.assertEquals(1.0, dist.cumulativeProbability(upper), TOL);
    }

    @Test
    public void testUpperCumulativeProbability() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(-1), TOL);
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(0), TOL);
        Assert.assertEquals(0.9166666666666667, dist.upperCumulativeProbability(1), TOL);
        Assert.assertEquals(0.5, dist.upperCumulativeProbability(2), TOL);
        Assert.assertEquals(0.08333333333333333, dist.upperCumulativeProbability(3), TOL);
        Assert.assertEquals(0.0, dist.upperCumulativeProbability(4), TOL);
    }

    @Test
    public void testUpperCumulativeProbabilityAtLowerBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        int lower = dist.getSupportLowerBound();
        Assert.assertEquals(1.0, dist.upperCumulativeProbability(lower), TOL);
    }

    @Test
    public void testUpperCumulativeProbabilityAtUpperBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        int upper = dist.getSupportUpperBound();
        Assert.assertEquals(dist.probability(upper), dist.upperCumulativeProbability(upper), TOL);
    }

    @Test
    public void testGetNumericalMean() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(1.5, dist.getNumericalMean(), TOL);
    }

    @Test
    public void testGetNumericalVariance() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        // variance = (5*3*5*7)/(100*9) = 525/900 = 0.583333...
        Assert.assertEquals(0.5833333333333334, dist.getNumericalVariance(), TOL);
        // test caching: second call returns same
        Assert.assertEquals(dist.getNumericalVariance(), dist.getNumericalVariance(), TOL);
    }

    @Test
    public void testSupportLowerBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(0, dist.getSupportLowerBound());
    }

    @Test
    public void testSupportUpperBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertEquals(3, dist.getSupportUpperBound());
    }

    @Test
    public void testSupportBoundsAllSuccesses() {
        // N=10, m=10, n=5 -> support lower = max(0,5+10-10)=5, upper = min(10,5)=5 => [5,5]
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 10, 5);
        Assert.assertEquals(5, dist.getSupportLowerBound());
        Assert.assertEquals(5, dist.getSupportUpperBound());
        Assert.assertEquals(1.0, dist.probability(5), TOL);
    }

    @Test
    public void testSupportBoundsNoSuccesses() {
        // N=10, m=0, n=5 -> support lower = max(0,5+0-10)=0, upper = min(0,5)=0 => [0,0]
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 0, 5);
        Assert.assertEquals(0, dist.getSupportLowerBound());
        Assert.assertEquals(0, dist.getSupportUpperBound());
        Assert.assertEquals(1.0, dist.probability(0), TOL);
    }

    @Test
    public void testIsSupportConnected() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 5);
        Assert.assertTrue(dist.isSupportConnected());
    }
}
