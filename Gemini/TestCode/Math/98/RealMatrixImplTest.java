package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

public class RealMatrixImplTest {

    @Test
    public void testDefaultConstructor() {
        RealMatrixImpl m = new RealMatrixImpl();
        Assert.assertNull(m.getDataRef());
    }

    @Test
    public void testDimensionsConstructor() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        Assert.assertEquals(2, m.getRowDimension());
        Assert.assertEquals(3, m.getColumnDimension());
        Assert.assertEquals(0.0, m.getEntry(0, 0), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionsConstructorInvalidRow() {
        new RealMatrixImpl(0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionsConstructorInvalidCol() {
        new RealMatrixImpl(2, -1);
    }

    @Test
    public void testArrayConstructor2D() {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        d[0][0] = 100.0;
        Assert.assertEquals(1.0, m.getEntry(0, 0), 1e-15);
    }

    @Test(expected = NullPointerException.class)
    public void testArrayConstructor2DNull() {
        new RealMatrixImpl((double[][]) null);
    }

    @Test
    public void testArrayConstructorCopyFlagFalse() {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d, false);
        Assert.assertSame(d, m.getDataRef());
    }

    @Test(expected = NullPointerException.class)
    public void testArrayConstructorCopyFalseNull() {
        new RealMatrixImpl(null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayConstructorCopyFalseEmptyRows() {
        new RealMatrixImpl(new double[0][0], false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayConstructorCopyFalseEmptyCols() {
        new RealMatrixImpl(new double[][]{{}}, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayConstructorCopyFalseNonRectangular() {
        new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0}}, false);
    }

    @Test
    public void testVectorConstructor() {
        double[] v = {1.0, 2.0, 3.0};
        RealMatrixImpl m = new RealMatrixImpl(v);
        Assert.assertEquals(3, m.getRowDimension());
        Assert.assertEquals(1, m.getColumnDimension());
        Assert.assertEquals(2.0, m.getEntry(1, 0), 1e-15);
    }

    @Test
    public void testCopy() {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        RealMatrix copy = m.copy();
        Assert.assertNotSame(m.getDataRef(), ((RealMatrixImpl) copy).getDataRef());
        Assert.assertEquals(m, copy);
    }

    @Test
    public void testAddAndSubtract() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        
        RealMatrix sum = m1.add(m2);
        Assert.assertEquals(6.0, sum.getEntry(0, 0), 1e-15);
        Assert.assertEquals(8.0, sum.getEntry(0, 1), 1e-15);
        Assert.assertEquals(10.0, sum.getEntry(1, 0), 1e-15);
        Assert.assertEquals(12.0, sum.getEntry(1, 1), 1e-15);

        RealMatrix diff = m2.subtract(m1);
        Assert.assertEquals(4.0, diff.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, diff.getEntry(1, 1), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1.0}, {2.0}});
        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1.0}, {2.0}});
        m1.subtract(m2);
    }

    private static class CustomRealMatrix implements RealMatrix {
        private final double[][] data;
        CustomRealMatrix(double[][] data) { this.data = data; }
        public RealMatrix copy() { return new CustomRealMatrix(data); }
        public RealMatrix add(RealMatrix m) { return null; }
        public RealMatrix subtract(RealMatrix m) { return null; }
        public RealMatrix scalarAdd(double d) { return null; }
        public RealMatrix scalarMultiply(double d) { return null; }
        public RealMatrix multiply(RealMatrix m) { return null; }
        public RealMatrix preMultiply(RealMatrix m) { return null; }
        public double[][] getData() { return data; }
        public double getNorm() { return 0; }
        public double getDeterminant() { return 0; }
        public boolean isSquare() { return getRowDimension() == getColumnDimension(); }
        public boolean isSingular() { return false; }
        public int getRowDimension() { return data.length; }
        public int getColumnDimension() { return data[0].length; }
        public double getTrace() { return 0; }
        public double[] operate(double[] v) { return null; }
        public double[] preMultiply(double[] v) { return null; }
        public double[] solve(double[] b) { return null; }
        public RealMatrix solve(RealMatrix b) { return null; }
        public RealMatrix inverse() { return null; }
        public RealMatrix transpose() { return null; }
        public RealMatrix getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) { return null; }
        public RealMatrix getSubMatrix(int[] selectedRows, int[] selectedColumns) { return null; }
        public double[] getRow(int row) { return data[row]; }
        public double[] getColumn(int col) { return null; }
        public double getEntry(int row, int column) { return data[row][column]; }
        public RealMatrix getRowMatrix(int row) { return null; }
        public RealMatrix getColumnMatrix(int column) { return null; }
    }

    @Test
    public void testAddAndSubtractGenericInterface() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrix generic = new CustomRealMatrix(new double[][]{{2.0, 3.0}, {4.0, 5.0}});

        RealMatrix sum = m1.add(generic);
        Assert.assertEquals(3.0, sum.getEntry(0, 0), 1e-15);

        RealMatrix diff = m1.subtract(generic);
        Assert.assertEquals(-1.0, diff.getEntry(0, 0), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddGenericDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrix generic = new CustomRealMatrix(new double[][]{{1.0}, {2.0}});
        m1.add(generic);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractGenericDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrix generic = new CustomRealMatrix(new double[][]{{1.0}, {2.0}});
        m1.subtract(generic);
    }

    @Test
    public void testScalarOperations() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrix add = m.scalarAdd(2.5);
        Assert.assertEquals(3.5, add.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.5, add.getEntry(0, 1), 1e-15);

        RealMatrix mul = m.scalarMultiply(2.0);
        Assert.assertEquals(2.0, mul.getEntry(0, 0), 1e-15);
        Assert.assertEquals(6.0, mul.getEntry(1, 0), 1e-15);
    }

    @Test
    public void testMultiply() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{7.0, 8.0}, {9.0, 1.0}, {2.0, 3.0}});
        RealMatrix res = m1.multiply(m2);
        Assert.assertEquals(2, res.getRowDimension());
        Assert.assertEquals(2, res.getColumnDimension());
        Assert.assertEquals(31.0, res.getEntry(0, 0), 1e-15);
        Assert.assertEquals(19.0, res.getEntry(0, 1), 1e-15);
        Assert.assertEquals(85.0, res.getEntry(1, 0), 1e-15);
        Assert.assertEquals(55.0, res.getEntry(1, 1), 1e-15);

        RealMatrix premul = m2.preMultiply(m1);
        Assert.assertEquals(res, premul);
    }

    @Test
    public void testMultiplyGeneric() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrix generic = new CustomRealMatrix(new double[][]{{2.0, 0.0}, {1.0, 2.0}});
        RealMatrix res = m1.multiply(generic);
        Assert.assertEquals(4.0, res.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, res.getEntry(0, 1), 1e-15);
        Assert.assertEquals(10.0, res.getEntry(1, 0), 1e-15);
        Assert.assertEquals(8.0, res.getEntry(1, 1), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        m1.multiply(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyGenericDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}});
        RealMatrix generic = new CustomRealMatrix(new double[][]{{1.0, 2.0}});
        m1.multiply(generic);
    }

    @Test
    public void testGetData() {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        double[][] out = m.getData();
        out[0][0] = 999.0;
        Assert.assertEquals(1.0, m.getEntry(0, 0), 1e-15);
    }

    @Test
    public void testGetNorm() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, -5.0}, {3.0, 2.0}});
        Assert.assertEquals(7.0, m.getNorm(), 1e-15);
    }

    @Test
    public void testGetSubMatrixRange() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        });
        RealMatrix sub = m.getSubMatrix(1, 2, 0, 1);
        Assert.assertEquals(2, sub.getRowDimension());
        Assert.assertEquals(2, sub.getColumnDimension());
        Assert.assertEquals(4.0, sub.getEntry(0, 0), 1e-15);
        Assert.assertEquals(8.0, sub.getEntry(1, 1), 1e-15);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixRangeInvalid1() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        m.getSubMatrix(-1, 1, 0, 1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixRangeInvalid2() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        m.getSubMatrix(1, 0, 0, 1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixRangeInvalid3() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        m.getSubMatrix(0, 2, 0, 1);
    }

    @Test
    public void testGetSubMatrixIndices() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        });
        RealMatrix sub = m.getSubMatrix(new int[]{0, 2}, new int[]{1, 2});
        Assert.assertEquals(2.0, sub.getEntry(0, 0), 1e-15);
        Assert.assertEquals(3.0, sub.getEntry(0, 1), 1e-15);
        Assert.assertEquals(8.0, sub.getEntry(1, 0), 1e-15);
        Assert.assertEquals(9.0, sub.getEntry(1, 1), 1e-15);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixIndicesEmpty() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        m.getSubMatrix(new int[0], new int[]{0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixIndicesOutOfBounds() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        m.getSubMatrix(new int[]{0, 5}, new int[]{0});
    }

    @Test
    public void testSetSubMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        });
        m.setSubMatrix(new double[][]{{10.0, 20.0}, {30.0, 40.0}}, 1, 1);
        Assert.assertEquals(10.0, m.getEntry(1, 1), 1e-15);
        Assert.assertEquals(20.0, m.getEntry(1, 2), 1e-15);
        Assert.assertEquals(30.0, m.getEntry(2, 1), 1e-15);
        Assert.assertEquals(40.0, m.getEntry(2, 2), 1e-15);
    }

    @Test
    public void testSetSubMatrixUninitializedData() {
        RealMatrixImpl m = new RealMatrixImpl();
        m.setSubMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}}, 0, 0);
        Assert.assertEquals(1.0, m.getEntry(0, 0), 1e-15);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixUninitializedDataNonZeroIndex() {
        RealMatrixImpl m = new RealMatrixImpl();
        m.setSubMatrix(new double[][]{{1.0, 2.0}}, 1, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixNegativeIndex() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1.0}}, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixEmptyRow() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[0][0], 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixEmptyCol() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{}}, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixNonRectangular() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1.0, 2.0}, {3.0}}, 0, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixOutOfBounds() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}}, 1, 1);
    }

    @Test
    public void testGetRowAndColumnMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrix rowMat = m.getRowMatrix(1);
        Assert.assertEquals(1, rowMat.getRowDimension());
        Assert.assertEquals(2, rowMat.getColumnDimension());
        Assert.assertEquals(3.0, rowMat.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, rowMat.getEntry(0, 1), 1e-15);

        RealMatrix colMat = m.getColumnMatrix(1);
        Assert.assertEquals(2, colMat.getRowDimension());
        Assert.assertEquals(1, colMat.getColumnDimension());
        Assert.assertEquals(2.0, colMat.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, colMat.getEntry(1, 0), 1e-15);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRowMatrix(2);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumnMatrix(-1);
    }

    @Test
    public void testGetRowAndColumn() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        double[] row = m.getRow(0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, row, 1e-15);

        double[] col = m.getColumn(1);
        Assert.assertArrayEquals(new double[]{2.0, 4.0}, col, 1e-15);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRow(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumn(-1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getEntry(2, 0);
    }

    @Test
    public void testTranspose() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        RealMatrix t = m.transpose();
        Assert.assertEquals(3, t.getRowDimension());
        Assert.assertEquals(2, t.getColumnDimension());
        Assert.assertEquals(1.0, t.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, t.getEntry(0, 1), 1e-15);
        Assert.assertEquals(2.0, t.getEntry(1, 0), 1e-15);
    }

    @Test
    public void testTrace() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Assert.assertEquals(5.0, m.getTrace(), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraceNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        m.getTrace();
    }

    @Test
    public void testOperateAndPreMultiplyVector() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        double[] v = {2.0, 3.0};
        double[] resOp = m.operate(v);
        Assert.assertArrayEquals(new double[]{8.0, 18.0}, resOp, 1e-15);

        double[] resPre = m.preMultiply(v);
        Assert.assertArrayEquals(new double[]{11.0, 16.0}, resPre, 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateWrongLength() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.operate(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreMultiplyVectorWrongLength() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.preMultiply(new double[]{1.0, 2.0, 3.0});
    }

    @Test
    public void testLUDecompositionAndSolve() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{0.0, 2.0}, {3.0, 1.0}});
        double[] b = {4.0, 5.0};
        double[] x = m.solve(b);
        Assert.assertEquals(1.0, x[0], 1e-15);
        Assert.assertEquals(2.0, x[1], 1e-15);

        RealMatrix bMatrix = new RealMatrixImpl(new double[][]{{4.0, 8.0}, {5.0, 11.0}});
        RealMatrix xMatrix = m.solve(bMatrix);
        Assert.assertEquals(1.0, xMatrix.getEntry(0, 0), 1e-15);
        Assert.assertEquals(2.0, xMatrix.getEntry(1, 0), 1e-15);
        Assert.assertEquals(2.0, xMatrix.getEntry(0, 1), 1e-15);
        Assert.assertEquals(4.0, xMatrix.getEntry(1, 1), 1e-15);

        RealMatrix inv = m.inverse();
        RealMatrix identity = m.multiply(inv);
        Assert.assertEquals(1.0, identity.getEntry(0, 0), 1e-12);
        Assert.assertEquals(0.0, identity.getEntry(0, 1), 1e-12);
        Assert.assertEquals(0.0, identity.getEntry(1, 0), 1e-12);
        Assert.assertEquals(1.0, identity.getEntry(1, 1), 1e-12);

        Assert.assertEquals(-6.0, m.getDeterminant(), 1e-12);
        Assert.assertFalse(m.isSingular());

        RealMatrix luMat = m.getLUMatrix();
        Assert.assertNotNull(luMat);
        int[] perm = m.getPermutation();
        Assert.assertArrayEquals(new int[]{1, 0}, perm);
    }

    @Test
    public void testDeterminantCachedLU() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{2.0, 0.0}, {0.0, 3.0}});
        m.luDecompose();
        Assert.assertEquals(6.0, m.getDeterminant(), 1e-12);
        Assert.assertFalse(m.isSingular());
    }

    @Test(expected = InvalidMatrixException.class)
    public void testDeterminantNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        m.getDeterminant();
    }

    @Test
    public void testSingularMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {2.0, 4.0}});
        Assert.assertTrue(m.isSingular());
        Assert.assertEquals(0.0, m.getDeterminant(), 1e-15);
    }

    @Test(expected = InvalidMatrixException.class)
    public void testSolveSingularMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {2.0, 4.0}});
        m.solve(new double[]{1.0, 2.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveDimensionMismatchVector() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.solve(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveDimensionMismatchMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.solve(new RealMatrixImpl(3, 1));
    }

    @Test(expected = InvalidMatrixException.class)
    public void testSolveNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        m.solve(new RealMatrixImpl(2, 1));
    }

    @Test(expected = InvalidMatrixException.class)
    public void testLUDecomposeNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.luDecompose();
    }

    @Test
    public void testToString() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Assert.assertEquals("RealMatrixImpl{{1.0,2.0},{3.0,4.0}}", m.toString());

        RealMatrixImpl empty = new RealMatrixImpl();
        Assert.assertEquals("RealMatrixImpl{}", empty.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        RealMatrixImpl m3 = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 5.0}});
        RealMatrixImpl m4 = new RealMatrixImpl(new double[][]{{1.0, 2.0, 0.0}, {3.0, 4.0, 0.0}});

        Assert.assertTrue(m1.equals(m1));
        Assert.assertTrue(m1.equals(m2));
        Assert.assertEquals(m1.hashCode(), m2.hashCode());

        Assert.assertFalse(m1.equals(null));
        Assert.assertFalse(m1.equals("string"));
        Assert.assertFalse(m1.equals(m3));
        Assert.assertFalse(m1.equals(m4));
    }
}
