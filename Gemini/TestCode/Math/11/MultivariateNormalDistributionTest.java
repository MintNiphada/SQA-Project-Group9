package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.moment.VectorialCovariance;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class MultivariateNormalDistributionTest {

    private static final double TOLERANCE = 1e-9;

    @Test
    public void testConstructorAndGetters() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {4.0, 1.0},
            {1.0, 9.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);

        Assert.assertEquals(2, distribution.getDimension());
        Assert.assertArrayEquals(mu, distribution.getMeans(), TOLERANCE);

        RealMatrix covMatrix = distribution.getCovariances();
        Assert.assertEquals(4.0, covMatrix.getEntry(0, 0), TOLERANCE);
        Assert.assertEquals(1.0, covMatrix.getEntry(0, 1), TOLERANCE);
        Assert.assertEquals(1.0, covMatrix.getEntry(1, 0), TOLERANCE);
        Assert.assertEquals(9.0, covMatrix.getEntry(1, 1), TOLERANCE);

        double[] stdDevs = distribution.getStandardDeviations();
        Assert.assertEquals(2, stdDevs.length);
        Assert.assertEquals(2.0, stdDevs[0], TOLERANCE);
        Assert.assertEquals(3.0, stdDevs[1], TOLERANCE);
    }

    @Test
    public void testGettersDefensiveCopy() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {4.0, 1.0},
            {1.0, 9.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);

        double[] meansCopy = distribution.getMeans();
        meansCopy[0] = 999.0;
        Assert.assertEquals(1.0, distribution.getMeans()[0], TOLERANCE);

        RealMatrix covCopy = distribution.getCovariances();
        covCopy.setEntry(0, 0, 999.0);
        Assert.assertEquals(4.0, distribution.getCovariances().getEntry(0, 0), TOLERANCE);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDimensionMismatchRows() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {1.0, 0.0},
            {0.0, 1.0},
            {0.0, 0.0}
        };
        new MultivariateNormalDistribution(mu, sigma);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDimensionMismatchCols() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0}
        };
        new MultivariateNormalDistribution(mu, sigma);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDimensionMismatchRagged() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {1.0, 0.0},
            {0.0}
        };
        new MultivariateNormalDistribution(mu, sigma);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testNonPositiveDefiniteCovariance() {
        double[] mu = {0.0, 0.0};
        double[][] sigma = {
            {1.0, 2.0},
            {2.0, 1.0}
        };
        new MultivariateNormalDistribution(mu, sigma);
    }

    @Test
    public void testDensityUnivariateEquivalent() {
        double[] mu = {0.0};
        double[][] sigma = {{1.0}};

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);

        double expectedDensityAt0 = 1.0 / FastMath.sqrt(2 * FastMath.PI);
        Assert.assertEquals(expectedDensityAt0, distribution.density(new double[]{0.0}), 1e-12);

        double expectedDensityAt1 = expectedDensityAt0 * FastMath.exp(-0.5);
        Assert.assertEquals(expectedDensityAt1, distribution.density(new double[]{1.0}), 1e-12);
        Assert.assertEquals(expectedDensityAt1, distribution.density(new double[]{-1.0}), 1e-12);
    }

    @Test
    public void testDensityBivariate() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {4.0, 1.0},
            {1.0, 9.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);

        double det = 4.0 * 9.0 - 1.0 * 1.0; // 35
        double expectedDensityAtMean = FastMath.pow(2 * FastMath.PI, -1.0) * FastMath.pow(det, -0.5);
        Assert.assertEquals(expectedDensityAtMean, distribution.density(new double[]{1.0, 2.0}), 1e-12);

        double[] x = {2.0, 4.0};
        double[] diff = {1.0, 2.0};
        double inv00 = 9.0 / det;
        double inv01 = -1.0 / det;
        double inv10 = -1.0 / det;
        double inv11 = 4.0 / det;
        double quadForm = diff[0] * (inv00 * diff[0] + inv01 * diff[1])
                + diff[1] * (inv10 * diff[0] + inv11 * diff[1]);
        double expectedDensityAtX = expectedDensityAtMean * FastMath.exp(-0.5 * quadForm);
        Assert.assertEquals(expectedDensityAtX, distribution.density(x), 1e-12);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDensityDimensionMismatch() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {1.0, 0.0},
            {0.0, 1.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);
        distribution.density(new double[]{1.0, 2.0, 3.0});
    }

    @Test
    public void testSamplingStatistics() {
        double[] mu = {-1.5, 2.0};
        double[][] sigma = {
            {2.0, 0.5},
            {0.5, 3.0}
        };

        RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(42L);
        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(rng, mu, sigma);

        int sampleSize = 50000;
        VectorialMean meanStat = new VectorialMean(2);
        VectorialCovariance covStat = new VectorialCovariance(2, true);

        for (int i = 0; i < sampleSize; i++) {
            double[] sample = distribution.sample();
            meanStat.increment(sample);
            covStat.increment(sample);
        }

        double[] sampleMean = meanStat.getResult();
        Assert.assertEquals(mu[0], sampleMean[0], 0.05);
        Assert.assertEquals(mu[1], sampleMean[1], 0.05);

        RealMatrix sampleCov = covStat.getResult();
        Assert.assertEquals(sigma[0][0], sampleCov.getEntry(0, 0), 0.1);
        Assert.assertEquals(sigma[0][1], sampleCov.getEntry(0, 1), 0.1);
        Assert.assertEquals(sigma[1][0], sampleCov.getEntry(1, 0), 0.1);
        Assert.assertEquals(sigma[1][1], sampleCov.getEntry(1, 1), 0.1);
    }

    @Test
    public void testSampleMultiple() {
        double[] mu = {0.0, 0.0};
        double[][] sigma = {
            {1.0, 0.0},
            {0.0, 1.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(new Well19937c(12345L), mu, sigma);
        double[][] samples = distribution.sample(10);

        Assert.assertEquals(10, samples.length);
        for (double[] s : samples) {
            Assert.assertEquals(2, s.length);
        }
    }

    @Test
    public void testReseedRandomGenerator() {
        double[] mu = {1.0, 2.0};
        double[][] sigma = {
            {1.0, 0.2},
            {0.2, 1.0}
        };

        MultivariateNormalDistribution dist1 = new MultivariateNormalDistribution(new Well19937c(100L), mu, sigma);
        MultivariateNormalDistribution dist2 = new MultivariateNormalDistribution(new Well19937c(200L), mu, sigma);

        dist1.reseedRandomGenerator(999L);
        dist2.reseedRandomGenerator(999L);

        double[] sample1 = dist1.sample();
        double[] sample2 = dist2.sample();

        Assert.assertEquals(sample1[0], sample2[0], TOLERANCE);
        Assert.assertEquals(sample1[1], sample2[1], TOLERANCE);
    }

    @Test
    public void testThreeDimensionalDistribution() {
        double[] mu = {1.0, 2.0, 3.0};
        double[][] sigma = {
            {2.0, 0.3, 0.1},
            {0.3, 1.5, 0.2},
            {0.1, 0.2, 1.0}
        };

        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, sigma);

        Assert.assertEquals(3, distribution.getDimension());
        Assert.assertArrayEquals(mu, distribution.getMeans(), TOLERANCE);

        double[] std = distribution.getStandardDeviations();
        Assert.assertEquals(FastMath.sqrt(2.0), std[0], TOLERANCE);
        Assert.assertEquals(FastMath.sqrt(1.5), std[1], TOLERANCE);
        Assert.assertEquals(FastMath.sqrt(1.0), std[2], TOLERANCE);

        double d = distribution.density(new double[]{1.0, 2.0, 3.0});
        Assert.assertTrue(d > 0.0 && !Double.isInfinite(d) && !Double.isNaN(d));
    }
}
