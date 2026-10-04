package org.apache.commons.math3.distribution;

import org.junit.Test;
import org.junit.Assert;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;

public class FDistributionTest {

    private static final double TOLERANCE = 1e-9;

    @Test
    public void testConstructorValidParameters() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), 0);
        Assert.assertEquals(10.0, dist.getDenominatorDegreesOfFreedom(), 0);
        Assert.assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNumeratorZero() {
        new FDistribution(0.0, 10.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNumeratorNegative() {
        new FDistribution(-1.0, 10.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorDenominatorZero() {
        new FDistribution(5.0, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorDenominatorNegative() {
        new FDistribution(5.0, -2.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorBothInvalid() {
        new FDistribution(0.0, -1.0);
    }

    @Test
    public void testConstructorWithInverseCumAccuracy() {
        FDistribution dist = new FDistribution(3.0, 7.0, 1e-6);
        Assert.assertEquals(3.0, dist.getNumeratorDegreesOfFreedom(), 0);
        Assert.assertEquals(7.0, dist.getDenominatorDegreesOfFreedom(), 0);
        Assert.assertEquals(1e-6, dist.getSolverAbsoluteAccuracy(), 0);
    }

    @Test
    public void testConstructorWithRandomGenerator() {
        Well19937c rng = new Well19937c(12345);
        FDistribution dist = new FDistribution(rng, 4.0, 9.0, 1e-8);
        Assert.assertEquals(4.0, dist.getNumeratorDegreesOfFreedom(), 0);
        Assert.assertEquals(9.0, dist.getDenominatorDegreesOfFreedom(), 0);
        Assert.assertEquals(1e-8, dist.getSolverAbsoluteAccuracy(), 0);
    }

    @Test
    public void testDensityPositiveX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double density = dist.density(1.0);
        Assert.assertTrue(density > 0);
    }

    @Test
    public void testDensityZeroX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double density = dist.density(0.0);
        Assert.assertTrue(Double.isNaN(density) || Double.isInfinite(density));
    }

    @Test
    public void testDensityNegativeX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double density = dist.density(-1.0);
        Assert.assertTrue(Double.isNaN(density));
    }

    @Test
    public void testDensityLargeX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double density = dist.density(1000.0);
        Assert.assertTrue(density > 0 && density < 1);
    }

    @Test
    public void testCumulativeProbabilityZeroX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(0.0), TOLERANCE);
    }

    @Test
    public void testCumulativeProbabilityNegativeX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-1.0), TOLERANCE);
    }

    @Test
    public void testCumulativeProbabilityPositiveX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double prob = dist.cumulativeProbability(1.0);
        Assert.assertTrue(prob > 0 && prob < 1);
    }

    @Test
    public void testCumulativeProbabilityLargeX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double prob = dist.cumulativeProbability(1000.0);
        Assert.assertTrue(prob > 0.99);
    }

    @Test
    public void testGetNumeratorDegreesOfFreedom() {
        FDistribution dist = new FDistribution(7.5, 12.3);
        Assert.assertEquals(7.5, dist.getNumeratorDegreesOfFreedom(), 0);
    }

    @Test
    public void testGetDenominatorDegreesOfFreedom() {
        FDistribution dist = new FDistribution(7.5, 12.3);
        Assert.assertEquals(12.3, dist.getDenominatorDegreesOfFreedom(), 0);
    }

    @Test
    public void testGetSolverAbsoluteAccuracyDefault() {
        FDistribution dist = new FDistribution(2.0, 3.0);
        Assert.assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 0);
    }

    @Test
    public void testGetSolverAbsoluteAccuracyCustom() {
        FDistribution dist = new FDistribution(2.0, 3.0, 1e-5);
        Assert.assertEquals(1e-5, dist.getSolverAbsoluteAccuracy(), 0);
    }

    @Test
    public void testNumericalMeanDenominatorGreaterThanTwo() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertEquals(5.0 / 3.0, dist.getNumericalMean(), TOLERANCE);
    }

    @Test
    public void testNumericalMeanDenominatorEqualsTwo() {
        FDistribution dist = new FDistribution(5.0, 2.0);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
    }

    @Test
    public void testNumericalMeanDenominatorLessThanTwo() {
        FDistribution dist = new FDistribution(5.0, 1.5);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
    }

    @Test
    public void testNumericalVarianceDenominatorGreaterThanFour() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double expected = (2.0 * 10.0 * 10.0 * (5.0 + 10.0 - 2.0)) / (5.0 * (10.0 - 2.0) * (10.0 - 2.0) * (10.0 - 4.0));
        Assert.assertEquals(expected, dist.getNumericalVariance(), TOLERANCE);
    }

    @Test
    public void testNumericalVarianceDenominatorEqualsFour() {
        FDistribution dist = new FDistribution(5.0, 4.0);
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

    @Test
    public void testNumericalVarianceDenominatorLessThanFour() {
        FDistribution dist = new FDistribution(5.0, 3.0);
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

    @Test
    public void testNumericalVarianceCaching() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double var1 = dist.getNumericalVariance();
        double var2 = dist.getNumericalVariance();
        Assert.assertEquals(var1, var2, 0);
    }

    @Test
    public void testCalculateNumericalVarianceViaGet() {
        FDistribution dist = new FDistribution(2.0, 6.0);
        double var = dist.getNumericalVariance();
        double expected = (2.0 * 6.0 * 6.0 * (2.0 + 6.0 - 2.0)) / (2.0 * (6.0 - 2.0) * (6.0 - 2.0) * (6.0 - 4.0));
        Assert.assertEquals(expected, var, TOLERANCE);
    }

    @Test
    public void testSupportLowerBound() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertEquals(0.0, dist.getSupportLowerBound(), 0);
    }

    @Test
    public void testSupportUpperBound() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), 0);
    }

    @Test
    public void testIsSupportLowerBoundInclusive() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertTrue(dist.isSupportLowerBoundInclusive());
    }

    @Test
    public void testIsSupportUpperBoundInclusive() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertFalse(dist.isSupportUpperBoundInclusive());
    }

    @Test
    public void testIsSupportConnected() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        Assert.assertTrue(dist.isSupportConnected());
    }

    @Test
    public void testDefaultInverseAbsoluteAccuracyConstant() {
        Assert.assertEquals(1e-9, FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, 0);
    }
}
