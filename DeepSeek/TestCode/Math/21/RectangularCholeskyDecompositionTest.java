package org.apache.commons.math3.linear;

import static org.junit.Assert.*;
import org.junit.Test;

public class RectangularCholeskyDecompositionTest {

    @Test
    public void testPositiveDefiniteFullRank() {
        double[][] aData = {{4, 2, 0}, {2, 5, 3}, {0, 3, 6}};
        RealMatrix matrix = new Array2DRowRealMatrix(aData);
        RealMatrix original = matrix.copy();
        double small = 1e-10;
        RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(matrix, small);
        assertEquals(3, decomposition.getRank());
        RealMatrix root = decomposition.getRootMatrix();
        assertNotNull(root);
        assertEquals(3, root.getRowDimension());
        assertEquals(3, root.getColumnDimension());
        RealMatrix product = root.multiply(root.transpose());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(original.getEntry(i, j), product.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testRankDeficientSemidefinite() {
        double[][] mData = {{1, 0}, {2, 1}, {0, 0}};
        RealMatrix M = new Array2DRowRealMatrix(mData);
        RealMatrix A = M.multiply(M.transpose()); // Rank 2, 3x3
        RealMatrix original = A.copy();
        double small = 1e-10;
        RectangularCholeskyDecomposition decomp = new RectangularCholeskyDecomposition(A, small);
        assertEquals(2, decomp.getRank());
        RealMatrix root = decomp.getRootMatrix();
        assertEquals(3, root.getRowDimension());
        assertEquals(2, root.getColumnDimension());
        RealMatrix product = root.multiply(root.transpose());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals("Entry (" + i + "," + j + ")", original.getEntry(i, j), product.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testNegativeDiagonalFirstPivot() {
        double[][] data = {{-1, 0}, {0, -2}};
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        new RectangularCholeskyDecomposition(matrix, 1e-10);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testIndefiniteMatrixThrowsException() {
        double[][] data = {{1, 2}, {2, 1}};        RealMatrix matrix = new Array2DRowRealMatrix(data);        new RectangularCholeskyDecomponsition(matrix, 0.0);    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testZeroDiagonalFirstElement() {
        double[][] data = {{0.0}};
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        new RectangularCholeskyDecomponsition(matrix, 1e-10);
    }

    @Test(expected = NullPointerException.class)
    public void testNullMatrix() {
        new RectangularCholeskyDecomponsition(null, 1e-10);
    }

    @Test
    public void testOrderOnePositive() {
        double[][] data = {{5.0}};
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        RealMatrix original = matrix.copy();
        RectangularCholeskyDecomponsition decomp = new RectangularCholeskyDecomponsition(matrix, 1e-10);
        assertEquals(1, decomp.getRank());
        RealMatrix root = decomp.getRootMatrix();
        assertEquals(1, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());
        assertEquals(Math.sqrt(5.0), root.getEntry(0, 0), 1e-15);
        // Check that root*root^T equals original (scalar)
        assertEquals(original.getEntry(0, 0), root.multiply(root.transpose()).getEntry(0, 0), 1e-15);
    }

    @Test
    public void testRankDeficientWithSmallPositive() {
        double[][] data = {{1, 1}, {1, 1}}; // rank 1
        RealMatrix A = new Array2DRowRealMatrix(data);
        RealMatrix original = A.copy();
        RectangularCholeskyDecomposition decomp = new RectangularCholeskyDecomposition(A, 1e-10);
        assertEquals(1, decomp.getRank());
        RealMatrix root = decomp.getRootMatrix();
        assertEquals(2, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());
        RealMatrix product = root.multiply(root.transpose());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(original.getEntry(i, j), product.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test
    public void testPermutationAndSwapHappens() {
        // Matrix [[1,0],[0,2]] should trigger pivot to process larger diagonal first
        double[][] data = {{1, 0}, {0, 2}};
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        RealMatrix original = matrix.copy();
        RectangularCholeskyDecomposition decomp = new RectangularCholeskyDecomposition(matrix, 1e-10);
        assertEquals(2, decomp.getRank());
        RealMatrix root = decomp.getRootMatrix();
        assertNotNull(root);
        assertEquals(2, root.getRowDimension());
        assertEquals(2, root.getColumnDimension());
        RealMatrix product = root.multiply(root.transpose());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(original.getEntry(i, j), product.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testGetRootMatrixAndRank() {
        double[][] data = {{2, 0}, {0, 2}};
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        RectangularCholeskyDecomposition decomp = new RectangularCholeskyDecomposition(matrix, 1e-10);
        assertNotNull(decomp.getRootMatrix());
        assertEquals(2, decomp.getRank());
        assertEquals(2, decomp.getRootMatrix().getRowDimension());
        assertEquals(2, decomp.getRootMatrix().getColumnDimension());
    }
}
