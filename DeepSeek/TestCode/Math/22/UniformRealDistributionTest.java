package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

public class UniformRealDistributionTest {

    private static final double EPSILON = 1e-15;

    @Test
    public void testDefaultConstructor() {
        UniformRealDistribution d = new UniformRealDistribution();
        Assert.assertEquals(0.0, d.getSupportLowerBound(), EPSILON);
        Assert.assertEquals(1.0, d.getSupportUpperBound(), EPSILON);
        Assert.assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, d.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test
    public void testConstructorWithBounds() {
        UniformRealDistribution d = new UniformRealDistribution(-2.0, 3.0);
        Assert.assertEquals(-2.0, d.getSupportLowerBound(), EPSILON);
        Assert.assertEquals(3.0, d.getSupportUpperBound(), EPSILON);
        Assert.assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, d.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test
    public void testConstructorWithAccuracy() {
        UniformRealDistribution d = new UniformRealDistribution(-1.0, 1.0, 1e-5);
        Assert.assertEquals(-1.0, d.getSupportLowerBound(), EPSILON);
        Assert.assertEquals(1.0, d.getSupportUpperBound(), EPSILON);
        Assert.assertEquals(1e-5, d.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test
    public void testConstructorWithAllParams() {
        RandomGenerator rg = new Well19937c();
        UniformRealDistribution d = new UniformRealDistribution(rg, -5.0, 5.0, 1e-7);
        Assert.assertEquals(-5.0, d.getSupportLowerBound(), EPSILON);
        Assert.assertEquals(5.0, d.getSupportUpperBound(), EPSILON);
        Assert.assertEquals(1e-7, d.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpper() {
        new UniformRealDistribution(2.0, 1.0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerEqualToUpper() {
        new UniformRealDistribution(1.0, 1.0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpperWithAccuracy() {
        new UniformRealDistribution(2.0, 1.0, 1e-9);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorLowerEqualToUpperWithAll() {
        new UniformRealDistribution(new Well19937c(), 1.0, 1.0, 1e-9);
    }

    @Test
    public void testDensity() {
        UniformRealDistribution d = new UniformRealDistribution(-3.0, 5.0);
        Assert.assertEquals(0.125, d.density(-3.0), EPSILON);
        Assert.assertEquals(0.125, d.density(5.0), EPSILON);
        Assert.assertEquals(0.125, d.density(0.0), EPSILON);
        Assert.assertEquals(0.0, d.density(-3.0001), EPSILON);
        Assert.assertEquals(0.0, d.density(5.0001), EPSILON);
        Assert.assertEquals(0.0, d.density(-Double.MAX_VALUE), EPSILON);
    }

    @Test
    public void testCumulativeProbability() {
        UniformRealDistribution d = new UniformRealDistribution(-3.0, 5.0);
        Assert.assertEquals(0.0, d.cumulativeProbability(-3.0), EPSILON);
        Assert.assertEquals(0.0, d.cumulativeProbability(-4.0), EPSILON);
        Assert.assertEquals(1.0, d.cumulativeProbability(5.0), EPSILON);
        Assert.assertEquals(1.0, d.cumulativeProbability(6.0), EPSILON);
        Assert.assertEquals(0.5, d.cumulativeProbability(1.0), EPSILON);
        Assert.assertEquals(0.25, d.cumulativeProbability(-1.0), EPSILON);
    }

    @Test
    public void testGetNumericalMean() {
        UniformRealDistribution d = new UniformRealDistribution(-3.0, 5.0);
        Assert.assertEquals(1.0, d.getNumericalMean(), EPSILON);
    }

    @Test
    public void testGetNumericalVariance() {
        UniformRealDistribution d = new UniformRealDistribution(-3.0, 5.0);
        double ul = 5.0 - (-3.0);
        double expected = ul * ul / 12.0;
        Assert.assertEquals(expected, d.getNumericalVariance(), 1e-12);
    }

    @Test
    public void testSupportLowerBound() {
        UniformRealDistribution d = new UniformRealDistribution(-1.5, 2.5);
        Assert.assertEquals(-1.5, d.getSupportLowerBound(), 0);
    }

    @Test
    public void testSupportUpperBound() {
        UniformRealDistribution d = new UniformRealDistribution(-1.5, 2.5);
        Assert.assertEquals(2.5, d.getSupportUpperBound(), 0);
    }

    @Test
    public void testIsSupportLowerBoundInclusive() {
        Assert.assertTrue(new UniformRealDistribution().isSupportLowerBoundInclusive());
    }

    @Test
    public void testIsSupportUpperBoundInclusive() {
        Assert.assertFalse(new UniformRealDistribution().isSupportUpperBoundInclusive());
    }

    @Test
    public void testIsSupportConnected() {
        Assert.assertTrue(new UniformRealDistribution().isSupportConnected());
    }

    @Test
    public void testSampleWithFixedRandom() {
        FixedRandomGenerator rng = new FixedRandomGenerator();
        UniformRealDistribution d = new UniformRealDistribution(rng, 2.0, 5.0, 1e-9);
        rng.setNextDouble(0.0);
        Assert.assertEquals(2.0, d.sample(), 0);
        rng.setNextDouble(1.0);
        Assert.assertEquals(5.0, d.sample(), 0);
        rng.setNextDouble(0.5);
        Assert.assertEquals(3.5, d.sample(), 0);
    }

    private static class FixedRandomGenerator implements RandomGenerator {
        private double nextDoubleValue;
        public void setNextDouble(double value) {
            this.nextDoubleValue = value;
        }
        @Override
        public void setSeed(int seed) {}
        @Override
        public void setSeed(int[] seed) {}
        @Override
        public void setSeed(long seed) {}
        @Override
        public void nextBytes(byte[] bytes) {}
        @Override
        public int nextInt() { return 0; }
        @Override
        public int nextInt(int n) { return 0; }
        @Override
        public long nextLong() { return 0; }
        @Override
        public boolean nextBoolean() { return false; }
        @Override
        public float nextFloat() { return 0; }
        @Override
        public double nextDouble() { return nextDoubleValue; }
        @Override
        public double nextGaussian() { return 0; }
    }
}
