package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Test;

public class PearsonsCorrelationTest {

    private final double[][] testData = new double[][] {
            { 1.0, 2.0, 5.0 },
            { 2.0, 4.0, 4.0 },
            { 3.0, 6.0, 3.0 },
            { 4.0, 8.0, 2.0 },
            { 5.0, 10.0, 1.0 }
    };

    @Test
    public void testDefaultConstructor() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        Assert.assertNull(pc.getCorrelationMatrix());
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] y = new double[] { 2.0, 4.0, 6.0 };
        double r = pc.correlation(x, y);
        Assert.assertEquals(1.0, r, 1e-10);
    }

    @Test
    public void test2DArrayConstructor() {
        PearsonsCorrelation pc = new PearsonsCorrelation(testData);
        RealMatrix matrix = pc.getCorrelationMatrix();
        Assert.assertNotNull(matrix);
        Assert.assertEquals(3, matrix.getRowDimension());
        Assert.assertEquals(3, matrix.getColumnDimension());
        Assert.assertEquals(1.0, matrix.getEntry(0, 0), 1e-10);
        Assert.assertEquals(1.0, matrix.getEntry(0, 1), 1e-10);
        Assert.assertEquals(-1.0, matrix.getEntry(0, 2), 1e-10);
        Assert.assertEquals(matrix.getEntry(0, 1), matrix.getEntry(1, 0), 1e-10);
    }

    @Test
    public void testRealMatrixConstructor() {
        RealMatrix rm = new BlockRealMatrix(testData);
        PearsonsCorrelation pc = new PearsonsCorrelation(rm);
        RealMatrix matrix = pc.getCorrelationMatrix();
        Assert.assertEquals(1.0, matrix.getEntry(1, 1), 1e-10);
        Assert.assertEquals(1.0, matrix.getEntry(1, 0), 1e-10);
        Assert.assertEquals(-1.0, matrix.getEntry(1, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsufficientRowsConstructor() {
        double[][] data = new double[][] { { 1.0, 2.0, 3.0 } };
        new PearsonsCorrelation(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsufficientColsConstructor() {
        double[][] data = new double[][] { { 1.0 }, { 2.0 }, { 3.0 } };
        new PearsonsCorrelation(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelationDifferentLengths() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] y = new double[] { 1.0, 2.0 };
        pc.correlation(x, y);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelationInsufficientLength() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 1.0 };
        double[] y = new double[] { 2.0 };
        pc.correlation(x, y);
    }

    @Test
    public void testCorrelationPair() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        double[] y = new double[] { 5.0, 4.0, 3.0, 2.0, 1.0 };
        Assert.assertEquals(-1.0, pc.correlation(x, y), 1e-10);
    }

    @Test
    public void testComputeCorrelationMatrix() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        RealMatrix matrix = pc.computeCorrelationMatrix(testData);
        Assert.assertEquals(3, matrix.getRowDimension());
        Assert.assertEquals(3, matrix.getColumnDimension());
        Assert.assertEquals(1.0, matrix.getEntry(0, 0), 1e-10);
        Assert.assertEquals(1.0, matrix.getEntry(0, 1), 1e-10);
        Assert.assertEquals(-1.0, matrix.getEntry(0, 2), 1e-10);
    }

    @Test
    public void testGetCorrelationStandardErrors() {
        double[][] data = new double[][] {
                { 1.0, 2.0 },
                { 2.0, 3.0 },
                { 3.0, 5.0 },
                { 4.0, 4.0 },
                { 5.0, 6.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix errors = pc.getCorrelationStandardErrors();
        Assert.assertEquals(2, errors.getRowDimension());
        Assert.assertEquals(2, errors.getColumnDimension());
        double r = pc.getCorrelationMatrix().getEntry(0, 1);
        double expectedError = Math.sqrt((1.0 - r * r) / (5 - 2));
        Assert.assertEquals(expectedError, errors.getEntry(0, 1), 1e-10);
        Assert.assertEquals(0.0, errors.getEntry(0, 0), 1e-10);
        Assert.assertEquals(0.0, errors.getEntry(1, 1), 1e-10);
    }

    @Test
    public void testGetCorrelationPValues() throws MathException {
        double[][] data = new double[][] {
                { 1.0, 2.0 },
                { 2.0, 3.0 },
                { 3.0, 5.0 },
                { 4.0, 4.0 },
                { 5.0, 6.0 },
                { 6.0, 8.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pValues = pc.getCorrelationPValues();
        Assert.assertEquals(2, pValues.getRowDimension());
        Assert.assertEquals(2, pValues.getColumnDimension());
        Assert.assertEquals(0.0, pValues.getEntry(0, 0), 1e-10);
        Assert.assertEquals(0.0, pValues.getEntry(1, 1), 1e-10);
        Assert.assertTrue(pValues.getEntry(0, 1) > 0.0);
        Assert.assertTrue(pValues.getEntry(0, 1) <= 1.0);
        Assert.assertEquals(pValues.getEntry(0, 1), pValues.getEntry(1, 0), 1e-10);
    }

    @Test
    public void testCovarianceConstructor() {
        Covariance cov = new Covariance(testData);
        PearsonsCorrelation pc = new PearsonsCorrelation(cov);
        RealMatrix corr = pc.getCorrelationMatrix();
        Assert.assertEquals(1.0, corr.getEntry(0, 0), 1e-10);
        Assert.assertEquals(1.0, corr.getEntry(0, 1), 1e-10);
        Assert.assertEquals(-1.0, corr.getEntry(0, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCovarianceMatrixConstructor() {
        Covariance nullCov = new Covariance() {
            @Override
            public RealMatrix getCovarianceMatrix() {
                return null;
            }
        };
        new PearsonsCorrelation(nullCov);
    }

    @Test
    public void testCovarianceMatrixAndNObsConstructor() {
        Covariance cov = new Covariance(testData);
        RealMatrix covMatrix = cov.getCovarianceMatrix();
        PearsonsCorrelation pc = new PearsonsCorrelation(covMatrix, testData.length);
        RealMatrix corr = pc.getCorrelationMatrix();
        Assert.assertEquals(1.0, corr.getEntry(0, 0), 1e-10);
        Assert.assertEquals(1.0, corr.getEntry(0, 1), 1e-10);
        Assert.assertEquals(-1.0, corr.getEntry(0, 2), 1e-10);
    }

    @Test
    public void testCovarianceToCorrelation() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        Covariance cov = new Covariance(testData);
        RealMatrix corr = pc.covarianceToCorrelation(cov.getCovarianceMatrix());
        Assert.assertEquals(1.0, corr.getEntry(0, 0), 1e-10);
        Assert.assertEquals(1.0, corr.getEntry(0, 1), 1e-10);
        Assert.assertEquals(-1.0, corr.getEntry(0, 2), 1e-10);
        Assert.assertEquals(corr.getEntry(0, 1), corr.getEntry(1, 0), 1e-10);
    }
}
