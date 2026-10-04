package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.junit.Assert;
import org.junit.Test;

public class NormalDistributionImplTest {

    private static final double TOLERANCE = 1E-5;

    @Test
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        Assert.assertEquals(0.0, dist.getMean(), 0.0);
        Assert.assertEquals(1.0, dist.getStandardDeviation(), 0.0);
    }

    @Test
    public void testCustomConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 3.5);
        Assert.assertEquals(2.5, dist.getMean(), 0.0);
        Assert.assertEquals(3.5, dist.getStandardDeviation(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSdZero() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSdNegative() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    @Test
    public void testSetMean() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setMean(5.0);
        Assert.assertEquals(5.0, dist.getMean(), 0.0);
    }

    @Test
    public void testSetStandardDeviation() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setStandardDeviation(2.0);
        Assert.assertEquals(2.0, dist.getStandardDeviation(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviationZero() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setStandardDeviation(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviationNegative() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setStandardDeviation(-0.5);
    }

    @Test
    public void testCumulativeProbability() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        Assert.assertEquals(0.5, dist.cumulativeProbability(0.0), TOLERANCE);
        Assert.assertEquals(0.8413447, dist.cumulativeProbability(1.0), TOLERANCE);
        Assert.assertEquals(0.1586553, dist.cumulativeProbability(-1.0), TOLERANCE);
        Assert.assertEquals(0.9772499, dist.cumulativeProbability(2.0), TOLERANCE);
        Assert.assertEquals(0.0227501, dist.cumulativeProbability(-2.0), TOLERANCE);
    }

    @Test
    public void testCumulativeProbabilityShifted() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(10.0, 2.0);
        Assert.assertEquals(0.5, dist.cumulativeProbability(10.0), TOLERANCE);
        Assert.assertEquals(0.8413447, dist.cumulativeProbability(12.0), TOLERANCE);
        Assert.assertEquals(0.1586553, dist.cumulativeProbability(8.0), TOLERANCE);
    }

    @Test
    public void testInverseCumulativeProbabilitySpecialPoints() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        Assert.assertEquals(Double.NEGATIVE_INFINITY, dist.inverseCumulativeProbability(0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
        Assert.assertEquals(0.0, dist.inverseCumulativeProbability(0.5), TOLERANCE);
    }

    @Test
    public void testInverseCumulativeProbability() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        Assert.assertEquals(2.0, dist.inverseCumulativeProbability(0.5), TOLERANCE);
        Assert.assertEquals(2.0 + 3.0, dist.inverseCumulativeProbability(0.8413447), TOLERANCE);
        Assert.assertEquals(2.0 - 3.0, dist.inverseCumulativeProbability(0.1586553), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityNegative() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityGreaterThanOne() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.25), 0.0);
        Assert.assertEquals(5.0, dist.getDomainLowerBound(0.5), 0.0);
        Assert.assertEquals(5.0, dist.getDomainLowerBound(0.75), 0.0);
    }

    @Test
    public void testGetDomainUpperBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(5.0, dist.getDomainUpperBound(0.25), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.75), 0.0);
    }

    @Test
    public void testGetInitialDomain() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(3.0, dist.getInitialDomain(0.25), 0.0);
        Assert.assertEquals(7.0, dist.getInitialDomain(0.75), 0.0);
        Assert.assertEquals(5.0, dist.getInitialDomain(0.5), 0.0);
    }
}
