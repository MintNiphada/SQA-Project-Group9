package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

public class DiscreteDistributionTest {

    @Test
    public void testConstructorSingleArgument() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.3));
        samples.add(new Pair<String, Double>("B", 0.7));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.3, distribution.probability("A"), 1e-9);
        Assert.assertEquals(0.7, distribution.probability("B"), 1e-9);
    }

    @Test(expected = NotPositiveException.class)
    public void testNegativeProbabilityThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", -0.1));
        samples.add(new Pair<String, Double>("B", 1.0));

        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = MathArithmeticException.class)
    public void testZeroProbabilitiesSumThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.0));
        samples.add(new Pair<String, Double>("B", 0.0));

        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testInfiniteProbabilityThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", Double.POSITIVE_INFINITY));
        samples.add(new Pair<String, Double>("B", 1.0));

        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNaNProbabilityThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", Double.NaN));
        samples.add(new Pair<String, Double>("B", 1.0));

        new DiscreteDistribution<String>(samples);
    }

    @Test
    public void testNormalization() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(1, 2.0));
        samples.add(new Pair<Integer, Double>(2, 3.0));
        samples.add(new Pair<Integer, Double>(3, 5.0));

        DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(samples);
        Assert.assertEquals(0.2, distribution.probability(1), 1e-9);
        Assert.assertEquals(0.3, distribution.probability(2), 1e-9);
        Assert.assertEquals(0.5, distribution.probability(3), 1e-9);
    }

    @Test
    public void testDuplicateValuesProbabilitySum() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("B", 0.3));
        samples.add(new Pair<String, Double>("A", 0.5));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.7, distribution.probability("A"), 1e-9);
        Assert.assertEquals(0.3, distribution.probability("B"), 1e-9);
        Assert.assertEquals(0.0, distribution.probability("C"), 1e-9);
    }

    @Test
    public void testNullValuesSupport() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>(null, 0.4));
        samples.add(new Pair<String, Double>("A", 0.6));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.4, distribution.probability(null), 1e-9);
        Assert.assertEquals(0.6, distribution.probability("A"), 1e-9);
        Assert.assertEquals(0.0, distribution.probability("B"), 1e-9);

        List<Pair<String, Double>> pmf = distribution.getSamples();
        Assert.assertEquals(2, pmf.size());
        Assert.assertNull(pmf.get(0).getKey());
        Assert.assertEquals(0.4, pmf.get(0).getValue(), 1e-9);
        Assert.assertEquals("A", pmf.get(1).getKey());
        Assert.assertEquals(0.6, pmf.get(1).getValue(), 1e-9);
    }

    @Test
    public void testGetSamples() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(10, 1.0));
        samples.add(new Pair<Integer, Double>(20, 3.0));

        DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(samples);
        List<Pair<Integer, Double>> result = distribution.getSamples();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Integer.valueOf(10), result.get(0).getKey());
        Assert.assertEquals(0.25, result.get(0).getValue(), 1e-9);
        Assert.assertEquals(Integer.valueOf(20), result.get(1).getKey());
        Assert.assertEquals(0.75, result.get(1).getValue(), 1e-9);
    }

    @Test
    public void testSample() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("First", 1.0));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("First", distribution.sample());
        }
    }

    @Test
    public void testSampleArray() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(42, 1.0));

        DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(samples);
        int sampleSize = 10;
        Integer[] sampledArray = distribution.sample(sampleSize);

        Assert.assertNotNull(sampledArray);
        Assert.assertEquals(sampleSize, sampledArray.length);
        for (Integer val : sampledArray) {
            Assert.assertEquals(Integer.valueOf(42), val);
        }
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleArrayZeroSizeThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        distribution.sample(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleArrayNegativeSizeThrowsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(samples);
        distribution.sample(-5);
    }

    @Test
    public void testReseedRandomGenerator() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(1, 0.5));
        samples.add(new Pair<Integer, Double>(2, 0.5));

        DiscreteDistribution<Integer> dist1 = new DiscreteDistribution<Integer>(new Well19937c(), samples);
        dist1.reseedRandomGenerator(123456L);

        DiscreteDistribution<Integer> dist2 = new DiscreteDistribution<Integer>(new Well19937c(), samples);
        dist2.reseedRandomGenerator(123456L);

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(dist1.sample(), dist2.sample());
        }
    }

    @Test
    public void testSampleFallbackBoundaryBranch() {
        RandomGenerator mockRng = new RandomGenerator() {
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0L; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0f; }
            public double nextDouble() { return 1.0; } // Forces loop to exhaust sum
            public double nextGaussian() { return 0.0; }
        };

        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.5));
        samples.add(new Pair<String, Double>("B", 0.5));

        DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(mockRng, samples);
        String sampled = distribution.sample();
        Assert.assertEquals("B", sampled);
    }
}
