package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.junit.Assert;
import org.junit.Test;

public class FDistributionImplTest {

    @Test
    public void testConstructorAndAccessors() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 6.0);
        Assert.assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), 1e-15);
        Assert.assertEquals(6.0, dist.getDenominatorDegreesOfFreedom(), 1e-15);

        dist.setNumeratorDegreesOfFreedom(10.0);
        Assert.assertEquals(10.0, dist.getNumeratorDegreesOfFreedom(), 1e-15);

        dist.setDenominatorDegreesOfFreedom(12.0);
        Assert.assertEquals(12.0, dist.getDenominatorDegreesOfFreedom(), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidNumerator() {
        new FDistributionImpl(0.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeNumerator() {
        new FDistributionImpl(-1.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidDenominator() {
        new FDistributionImpl(5.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeDenominator() {
        new FDistributionImpl(5.0, -1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeratorZero() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setNumeratorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeratorNegative() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setNumeratorDegreesOfFreedom(-2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDenominatorZero() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setDenominatorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDenominatorNegative() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setDenominatorDegreesOfFreedom(-2.0);
    }

    @Test
    public void testCumulativeProbabilityNonPositive() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(0.0), 1e-15);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-1.0), 1e-15);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-100.0), 1e-15);
    }

    @Test
    public void testCumulativeProbabilityPositive() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        double prob = dist.cumulativeProbability(1.0);
        Assert.assertEquals(0.5, prob, 1e-5);

        dist = new FDistributionImpl(1.0, 1.0);
        Assert.assertEquals(0.5, dist.cumulativeProbability(1.0), 1e-5);
    }

    @Test
    public void testInverseCumulativeProbabilityZeroAndOne() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        Assert.assertEquals(0.0, dist.inverseCumulativeProbability(0.0), 1e-15);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 1e-15);
    }

    @Test
    public void testInverseCumulativeProbabilityValidRange() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        double x = dist.inverseCumulativeProbability(0.5);
        Assert.assertEquals(1.0, x, 1e-4);

        double p = 0.25;
        double x2 = dist.inverseCumulativeProbability(p);
        Assert.assertEquals(p, dist.cumulativeProbability(x2), 1e-4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityNegative() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityGreaterThanOne() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testDomainBoundsAndInitialDomain() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 6.0);
        Assert.assertEquals(0.0, dist.getDomainLowerBound(0.5), 1e-15);
        Assert.assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 1e-15);
        Assert.assertEquals(6.0 / (6.0 - 2.0), dist.getInitialDomain(0.5), 1e-15);

        FDistributionImpl dist2 = new FDistributionImpl(5.0, 1.0);
        Assert.assertEquals(1.0 / (1.0 - 2.0), dist2.getInitialDomain(0.5), 1e-15);
    }
}
