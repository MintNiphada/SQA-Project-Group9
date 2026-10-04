package org.apache.commons.math3.linear;

import org.junit.Assert;
import org.junit.Test;

public class RectangularCholeskyDecompositionTest {

    private static final double EPSILON = 1.0e-12;

    @Test
    public void testFullRankIdentity() {
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(3);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(identity, 1.0e-10);

        Assert.assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(3, root.getRowDimension());
        Assert.assertEquals(3, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(identity.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testFullRankPositiveDefinite() {
        double[][] data = {
            { 4.0, 2.0, 2.0 },
            { 2.0, 5.0, 3.0 },
            { 2.0, 3.0, 6.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        Assert.assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(3, root.getRowDimension());
        Assert.assertEquals(3, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testDeficientRankPositiveSemidefinite() {
        double[][] data = {
            { 1.0, 1.0, 1.0 },
            { 1.0, 1.0, 1.0 },
            { 1.0, 1.0, 1.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        Assert.assertEquals(1, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(3, root.getRowDimension());
        Assert.assertEquals(1, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testRank2Matrix4x4() {
        double[][] data = {
            { 2.0, 1.0, 1.0, 0.0 },
            { 1.0, 2.0, 0.0, 1.0 },
            { 1.0, 0.0, 2.0, 1.0 },
            { 0.0, 1.0, 1.0, 2.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testNegativeDiagonalOnFirstStep() {
        double[][] data = {
            { -1.0, 0.0 },
            {  0.0, 1.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testAllZeroDiagonalOnFirstStep() {
        double[][] data = {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testNegativeDiagonalSubsequentStep() {
        double[][] data = {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    @Test
    public void testDiagonalWithSwaps() {
        double[][] data = {
            { 1.0, 0.0, 0.0 },
            { 0.0, 9.0, 0.0 },
            { 0.0, 0.0, 4.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        Assert.assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testSingleElementMatrixPositive() {
        double[][] data = { { 4.0 } };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        Assert.assertEquals(1, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(1, root.getRowDimension());
        Assert.assertEquals(1, root.getColumnDimension());
        Assert.assertEquals(2.0, root.getEntry(0, 0), EPSILON);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testSingleElementMatrixZero() {
        double[][] data = { { 0.0 } };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testSingleElementMatrixNegative() {
        double[][] data = { { -2.0 } };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    @Test
    public void testNonPositiveDefiniteExceptionDetails() {
        double[][] data = {
            { -5.0, 0.0 },
            {  0.0, 0.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        try {
            new RectangularCholeskyDecomposition(matrix, 1.0e-10);
            Assert.fail("Expected NonPositiveDefiniteMatrixException");
        } catch (NonPositiveDefiniteMatrixException e) {
            Assert.assertEquals(0, e.getRow());
            Assert.assertEquals(0, e.getColumn());
            Assert.assertEquals(1.0e-10, e.getThreshold(), 1.0e-15);
        }
    }

    @Test
    public void testRemainingDiagonalNearZeroPositiveSemidefinite() {
        double[][] data = {
            { 2.0, 2.0, 2.0 },
            { 2.0, 2.0, 2.0 },
            { 2.0, 2.0, 2.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-5);
        Assert.assertEquals(1, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(3, root.getRowDimension());
        Assert.assertEquals(1, root.getColumnDimension());
    }

    @Test
    public void testRectangularCholeskyDimensionAndEntries() {
        double[][] data = {
            { 1.0, 0.0, 1.0 },
            { 0.0, 2.0, 2.0 },
            { 1.0, 2.0, 3.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        Assert.assertEquals(2, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        Assert.assertEquals(3, root.getRowDimension());
        Assert.assertEquals(2, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1.0e-10);
            }
        }
    }
}
