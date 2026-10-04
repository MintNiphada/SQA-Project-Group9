package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

public class NormalDistributionImplTest {

    @Test
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        Assert.assertEquals(0.0, dist.getMean(), 1e-15);
        Assert.assertEquals(1.0, dist.getStandardDeviation(), 1e-15);
        Assert.assertEquals(NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test
    public void testTwoArgConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 1.5);
        Assert.assertEquals(2.5, dist.getMean(), 1e-15);
        Assert.assertEquals(1.5, dist.getStandardDeviation(), 1e-15);
        Assert.assertEquals(NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test
    public void testThreeArgConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10.0, 2.0, 1e-6);
        Assert.assertEquals(10.0, dist.getMean(), 1e-15);
        Assert.assertEquals(2.0, dist.getStandardDeviation(), 1e-15);
        Assert.assertEquals(1e-6, dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroStandardDeviation() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeStandardDeviation() {
        new NormalDistributionImpl(0.0, -1.0, 1e-9);
    }

    @Test
    public void testDensity() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        double expectedDensityAtMean = 1.0 / Math.sqrt(2.0 * Math.PI);
        Assert.assertEquals(expectedDensityAtMean, dist.density(0.0), 1e-9);

        NormalDistributionImpl dist2 = new NormalDistributionImpl(10.0, 2.0);
        double expectedDensityAtMean2 = 1.0 / (2.0 * Math.sqrt(2.0 * Math.PI));
        Assert.assertEquals(expectedDensityAtMean2, dist2.density(10.0), 1e-9);
        Assert.assertEquals(dist2.density(8.0), dist2.density(12.0), 1e-9);
    }

    @Test
    public void testCumulativeProbability() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        Assert.assertEquals(0.5, dist.cumulativeProbability(0.0), 1e-9);
        Assert.assertEquals(0.8413447460685429, dist.cumulativeProbability(1.0), 1e-9);
        Assert.assertEquals(0.15865525393145705, dist.cumulativeProbability(-1.0), 1e-9);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-40.0), 1e-9);
        Assert.assertEquals(1.0, dist.cumulativeProbability(40.0), 1e-9);
    }

    @Test
    public void testInverseCumulativeProbability() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, dist.inverseCumulativeProbability(0.0), 1e-15);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 1e-15);
        Assert.assertEquals(0.0, dist.inverseCumulativeProbability(0.5), 1e-9);
        Assert.assertEquals(1.0, dist.inverseCumulativeProbability(dist.cumulativeProbability(1.0)), 1e-6);
        Assert.assertEquals(-1.0, dist.inverseCumulativeProbability(dist.cumulativeProbability(-1.0)), 1e-6);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbabilityNegative() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbabilityGreaterThanOne() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.25), 1e-15);
        Assert.assertEquals(5.0, dist.getDomainLowerBound(0.5), 1e-15);
        Assert.assertEquals(5.0, dist.getDomainLowerBound(0.75), 1e-15);
    }

    @Test
    public void testGetDomainUpperBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(5.0, dist.getDomainUpperBound(0.25), 1e-15);
        Assert.assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 1e-15);
        Assert.assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.75), 1e-15);
    }

    @Test
    public void testGetInitialDomain() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);
        Assert.assertEquals(3.0, dist.getInitialDomain(0.25), 1e-15);
        Assert.assertEquals(5.0, dist.getInitialDomain(0.5), 1e-15);
        Assert.assertEquals(7.0, dist.getInitialDomain(0.75), 1e-15);
    }

    @Test
    public void testSample() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        dist.reseedRandomGenerator(42L);
        double sample = dist.sample();
        Assert.assertFalse(Double.isNaN(sample));
        Assert.assertFalse(Double.isInfinite(sample));
    }
}
