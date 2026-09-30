package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

public class DiscreteDistributionTest {

    @Test
    public void testConstructorValid() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.2));
        samples.add(new Pair<String, Double>("b", 0.3));
        samples.add(new Pair<String, Double>("c", 0.5));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        List<Pair<String, Double>> result = dist.getSamples();
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("a", result.get(0).getKey());
        Assert.assertEquals(0.2, result.get(0).getValue(), 1e-15);
        Assert.assertEquals("b", result.get(1).getKey());
        Assert.assertEquals(0.3, result.get(1).getValue(), 1e-15);
        Assert.assertEquals("c", result.get(2).getKey());
        Assert.assertEquals(0.5, result.get(2).getValue(), 1e-15);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorNegativeProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", -0.1));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorZeroSum() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.0));
        samples.add(new Pair<String, Double>("b", 0.0));
        new DiscreteDistribution<String>(samples);
    }

    @Test
    public void testConstructorWithRNG() {
        RandomGenerator rng = new Well19937c(12345);
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(1, 0.5));
        samples.add(new Pair<Integer, Double>(2, 0.5));
        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(rng, samples);
        Assert.assertNotNull(dist);
    }

    @Test
    public void testReseedRandomGenerator() {
        final double[] sequence = {0.1, 0.6, 0.1, 0.6};
        RandomGenerator rng = new RandomGenerator() {
            private int index = 0;
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) { index = 0; }
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0; }
            public double nextDouble() {
                return sequence[index++ % sequence.length];
            }
            public double nextGaussian() { return 0; }
        };
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.3));
        samples.add(new Pair<String, Double>("b", 0.7));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        String first = dist.sample();
        dist.reseedRandomGenerator(0);
        String second = dist.sample();
        Assert.assertEquals(first, second);
    }

    @Test
    public void testProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.2));
        samples.add(new Pair<String, Double>("b", 0.3));
        samples.add(new Pair<String, Double>("c", 0.5));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.2, dist.probability("a"), 1e-15);
        Assert.assertEquals(0.3, dist.probability("b"), 1e-15);
        Assert.assertEquals(0.5, dist.probability("c"), 1e-15);
        Assert.assertEquals(0.0, dist.probability("d"), 1e-15);
    }

    @Test
    public void testProbabilityWithDuplicates() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.2));
        samples.add(new Pair<String, Double>("a", 0.3));
        samples.add(new Pair<String, Double>("b", 0.5));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.5, dist.probability("a"), 1e-15);
        Assert.assertEquals(0.5, dist.probability("b"), 1e-15);
    }

    @Test
    public void testProbabilityWithNull() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>(null, 0.4));
        samples.add(new Pair<String, Double>("a", 0.6));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.4, dist.probability(null), 1e-15);
        Assert.assertEquals(0.6, dist.probability("a"), 1e-15);
        Assert.assertEquals(0.0, dist.probability("b"), 1e-15);
    }

    @Test
    public void testGetSamples() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(10, 0.1));
        samples.add(new Pair<Integer, Double>(20, 0.9));
        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(samples);
        List<Pair<Integer, Double>> result = dist.getSamples();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Integer.valueOf(10), result.get(0).getKey());
        Assert.assertEquals(0.1, result.get(0).getValue(), 1e-15);
        Assert.assertEquals(Integer.valueOf(20), result.get(1).getKey());
        Assert.assertEquals(0.9, result.get(1).getValue(), 1e-15);
    }

    @Test
    public void testSample() {
        final double[] sequence = {0.0, 0.5, 0.999};
        RandomGenerator rng = new RandomGenerator() {
            private int index = 0;
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0; }
            public double nextDouble() {
                return sequence[index++ % sequence.length];
            }
            public double nextGaussian() { return 0; }
        };
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.2));
        samples.add(new Pair<String, Double>("b", 0.3));
        samples.add(new Pair<String, Double>("c", 0.5));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        Assert.assertEquals("a", dist.sample());
        Assert.assertEquals("b", dist.sample());
        Assert.assertEquals("c", dist.sample());
    }

    @Test
    public void testSampleMultiple() {
        final double[] sequence = {0.1, 0.4, 0.7};
        RandomGenerator rng = new RandomGenerator() {
            private int index = 0;
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0; }
            public double nextDouble() {
                return sequence[index++ % sequence.length];
            }
            public double nextGaussian() { return 0; }
        };
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("x", 0.2));
        samples.add(new Pair<String, Double>("y", 0.3));
        samples.add(new Pair<String, Double>("z", 0.5));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        String[] result = dist.sample(3);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("x", result[0]);
        Assert.assertEquals("y", result[1]);
        Assert.assertEquals("z", result[2]);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleInvalidSizeZero() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleInvalidSizeNegative() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(-5);
    }
}
