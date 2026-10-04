package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.random.RandomGenerator;

public class MultivariateNormalDistributionTest {

    @Test
    public void testConstructorValid() {
        double[] means = {0.0};
        double[][] covariances = {{1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        assertNotNull(dist);
        assertEquals(1, dist.getDimension());
    }

    @Test
    public void testConstructorRNGValid() {
        RandomGenerator rng = new Well19937c(12345);
        double[] means = {1.0};
        double[][] covariances = {{4.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(rng, means, covariances);
        assertNotNull(dist);
        assertEquals(1, dist.getDimension());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testConstructorDimensionMismatchCovLength() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {{1.0}}; // length 1 != 2
        new MultivariateNormalDistribution(means, covariances);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testConstructorDimensionMismatchRowLength() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {{1.0, 0.0}, {0.0}}; // second row length 1
        new MultivariateNormalDistribution(means, covariances);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testConstructorNonPositiveDefinite() {
        double[] means = {0.0, 0.0};
        // symmetric but negative eigenvalue (eigenvalues 3 and -1)
        double[][] covariances = {{1.0, 2.0}, {2.0, 1.0}};
        new MultivariateNormalDistribution(means, covariances);
    }

    @Test(expected = SingularMatrixException.class)
    public void testConstructorSingularMatrix() {
        double[] means = {0.0, 0.0};
        // singular (rank 1)
        double[][] covariances = {{1.0, 1.0}, {1.0, 1.0}};
        new MultivariateNormalDistribution(means, covariances);
    }

    @Test
    public void testGetMeans() {
        double[] means = {1.0, 2.0};
        double[][] covariances = {{1.0, 0.5}, {0.5, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        double[] retrieved = dist.getMeans();
        assertArrayEquals(means, retrieved, 1e-15);
        // verify copy is not the same reference
        retrieved[0] = 999.0;
        assertArrayEquals(means, dist.getMeans(), 1e-15);
    }

    @Test
    public void testGetCovariances() {
        double[] means = {1.0};
        double[][] covariances = {{4.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        RealMatrix retrieved = dist.getCovariances();
        assertEquals(covariances[0][0], retrieved.getEntry(0, 0), 1e-15);
        // modify retrieved matrix should not change internal
        retrieved.setEntry(0, 0, 99.0);
        assertEquals(4.0, dist.getCovariances().getEntry(0, 0), 1e-15);
    }

    @Test
    public void testDensity1DIdentity() {
        double[] means = {0.0};
        double[][] covariances = {{1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        // For dim=1, -dim/2 = -1/2 = 0, factor = 1, det^(-0.5)=1, so density = exp(-0.5*x^2)
        assertEquals(1.0, dist.density(new double[]{0.0}), 1e-15);
        assertEquals(Math.exp(-0.5), dist.density(new double[]{1.0}), 1e-15);
        assertEquals(Math.exp(-0.5 * 4.0), dist.density(new double[]{2.0}), 1e-15);
    }

    @Test
    public void testDensity2DIdentity() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {{1.0, 0.0}, {0.0, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        // dim=2, -dim/2 = -1, factor = 1/(2*PI), det=1, exponent=1 => density = 1/(2*PI)
        double expected = 1.0 / (2.0 * Math.PI);
        assertEquals(expected, dist.density(new double[]{0.0, 0.0}), 1e-9);
    }

    @Test
    public void testDensity3DIdentity() {
        double[] means = {0.0, 0.0, 0.0};
        double[][] covariances = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        // dim=3, -dim/2 = -3/2 = -1 (integer division), same factor 1/(2*PI)
        double expected = 1.0 / (2.0 * Math.PI);
        assertEquals(expected, dist.density(new double[]{0.0, 0.0, 0.0}), 1e-9);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDensityDimensionMismatch() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {{1.0, 0.0}, {0.0, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        dist.density(new double[]{1.0}); // wrong length
    }

    @Test
    public void testGetStandardDeviations() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {{4.0, 0.0}, {0.0, 9.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        double[] std = dist.getStandardDeviations();
        assertEquals(2, std.length);
        assertEquals(2.0, std[0], 1e-15);
        assertEquals(3.0, std[1], 1e-15);
    }

    @Test
    public void testGetStandardDeviationsWithOffDiagonal() {
        double[] means = {0.0, 0.0};
        // covariance matrix with off-diagonals, std still sqrt of diag
        double[][] covariances = {{4.0, 1.0}, {1.0, 9.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        double[] std = dist.getStandardDeviations();
        assertEquals(2.0, std[0], 1e-15);
        assertEquals(3.0, std[1], 1e-15);
    }

    @Test
    public void testSampleReturnsValidArray() {
        double[] means = {2.0, -1.0};
        double[][] covariances = {{1.0, 0.5}, {0.5, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        double[] sample = dist.sample();
        assertNotNull(sample);
        assertEquals(2, sample.length);
        for (double v : sample) {
            assertFalse(Double.isNaN(v));
            assertFalse(Double.isInfinite(v));
        }
    }

    @Test
    public void testSampleDeterministicWithFixedSeed() {
        RandomGenerator rng = new Well19937c(12345L);
        double[] means = {1.0, 2.0};
        double[][] covariances = {{2.0, 0.0}, {0.0, 3.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(rng, means, covariances);
        double[] sample1 = dist.sample();
        double[] sample2 = dist.sample();
        // samples should differ because randoms differ
        boolean allSame = true;
        for (int i = 0; i < sample1.length; i++) {
            if (sample1[i] != sample2[i]) {
                allSame = false;
                break;
            }
        }
        assertFalse("Samples should be different", allSame);
        // both have correct length
        assertEquals(2, sample1.length);
        assertEquals(2, sample2.length);
    }

    @Test
    public void testDensityWithNonZeroMeans() {
        double[] means = {3.0};
        double[][] covariances = {{1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        // density at x = means: exp(0) = 1 (since factor 1 for dim=1)
        assertEquals(1.0, dist.density(new double[]{3.0}), 1e-15);
        // at x = 4.0 ( (4-3)^2 = 1, exponent = -0.5)
        assertEquals(Math.exp(-0.5), dist.density(new double[]{4.0}), 1e-15);
    }
}
