package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class BigMatrixImplTest {

    private final BigDecimal[][] testData = {
            {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")},
            {new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("3")},
            {new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("8")}
    };

    private final double[][] testDataDouble = {
            {1.0, 2.0, 3.0},
            {2.0, 5.0, 3.0},
            {1.0, 0.0, 8.0}
    };

    private final String[][] testDataString = {
            {"1", "2", "3"},
            {"2", "5", "3"},
            {"1", "0", "8"}
    };

    @Test
    public void testConstructors() {
        BigMatrixImpl m1 = new BigMatrixImpl();
        Assert.assertNull(m1.getDataRef());

        BigMatrixImpl m2 = new BigMatrixImpl(2, 3);
        Assert.assertEquals(2, m2.getRowDimension());
        Assert.assertEquals(3, m2.getColumnDimension());

        BigMatrixImpl m3 = new BigMatrixImpl(testData);
        Assert.assertEquals(3, m3.getRowDimension());
        Assert.assertEquals(3, m3.getColumnDimension());

        BigMatrixImpl m4 = new BigMatrixImpl(testData, true);
        Assert.assertEquals(new BigDecimal("1"), m4.getEntry(0, 0));

        BigMatrixImpl m5 = new BigMatrixImpl(testData, false);
        Assert.assertSame(testData, m5.getDataRef());

        BigMatrixImpl m6 = new BigMatrixImpl(testDataDouble);
        Assert.assertEquals(1.0, m6.getEntryAsDouble(0, 0), 1e-10);

        BigMatrixImpl m7 = new BigMatrixImpl(testDataString);
        Assert.assertEquals(new BigDecimal("1"), m7.getEntry(0, 0));

        BigDecimal[] vector = {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")};
        BigMatrixImpl m8 = new BigMatrixImpl(vector);
        Assert.assertEquals(3, m8.getRowDimension());
        Assert.assertEquals(1, m8.getColumnDimension());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidDimensionsRow() {
        new BigMatrixImpl(0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidDimensionsCol() {
        new BigMatrixImpl(2, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArrayNoCopy() {
        new BigMatrixImpl((BigDecimal[][]) null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyRowNoCopy() {
        new BigMatrixImpl(new BigDecimal[0][0], false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyColNoCopy() {
        new BigMatrixImpl(new BigDecimal[1][0], false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNonRectangularNoCopy() {
        BigDecimal[][] nonRect = {
                {new BigDecimal("1"), new BigDecimal("2")},
                {new BigDecimal("3")}
        };
        new BigMatrixImpl(nonRect, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleEmptyRow() {
        new BigMatrixImpl(new double[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleEmptyCol() {
        new BigMatrixImpl(new double[1][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleNonRectangular() {
        double[][] nonRect = {{1.0, 2.0}, {3.0}};
        new BigMatrixImpl(nonRect);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStringEmptyRow() {
        new BigMatrixImpl(new String[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStringEmptyCol() {
        new BigMatrixImpl(new String[1][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStringNonRectangular() {
        String[][] nonRect = {{"1", "2"}, {"3"}};
        new BigMatrixImpl(nonRect);
    }

    @Test
    public void testCopy() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrix copy = m.copy();
        Assert.assertEquals(m, copy);
        Assert.assertNotSame(m.getDataRef(), ((BigMatrixImpl) copy).getDataRef());
    }

    @Test
    public void testAddAndSubtract() {
        BigMatrixImpl m1 = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testData);

        BigMatrix sum = m1.add(m2);
        Assert.assertEquals(new BigDecimal("2"), sum.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("4"), sum.getEntry(0, 1));

        BigMatrix sum2 = m1.add((BigMatrix) m2);
        Assert.assertEquals(sum, sum2);

        BigMatrix diff = m1.subtract(m2);
        Assert.assertEquals(new BigDecimal("0"), diff.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("0"), diff.getEntry(1, 1));

        BigMatrix diff2 = m1.subtract((BigMatrix) m2);
        Assert.assertEquals(diff, diff2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(2, 2);
        BigMatrixImpl m2 = new BigMatrixImpl(2, 3);
        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(2, 2);
        BigMatrixImpl m2 = new BigMatrixImpl(3, 2);
        m1.subtract(m2);
    }

    @Test
    public void testScalarOperations() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrix added = m.scalarAdd(new BigDecimal("2"));
        Assert.assertEquals(new BigDecimal("3"), added.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("4"), added.getEntry(0, 1));

        BigMatrix multiplied = m.scalarMultiply(new BigDecimal("3"));
        Assert.assertEquals(new BigDecimal("3"), multiplied.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("6"), multiplied.getEntry(0, 1));
    }

    @Test
    public void testMultiplyAndPremultiply() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        BigMatrixImpl m2 = new BigMatrixImpl(new double[][]{{2, 0}, {1, 2}});

        BigMatrix product = m1.multiply(m2);
        Assert.assertEquals(new BigDecimal("4.0"), product.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("4.0"), product.getEntry(0, 1));
        Assert.assertEquals(new BigDecimal("10.0"), product.getEntry(1, 0));
        Assert.assertEquals(new BigDecimal("8.0"), product.getEntry(1, 1));

        BigMatrix preProduct = m2.preMultiply(m1);
        Assert.assertEquals(product, preProduct);

        BigMatrix productInterface = m1.multiply((BigMatrix) m2);
        Assert.assertEquals(product, productInterface);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(2, 2);
        BigMatrixImpl m2 = new BigMatrixImpl(3, 2);
        m1.multiply(m2);
    }

    @Test
    public void testDataGetters() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigDecimal[][] data = m.getData();
        Assert.assertEquals(3, data.length);
        Assert.assertNotSame(m.getDataRef(), data);

        double[][] dData = m.getDataAsDoubleArray();
        Assert.assertEquals(1.0, dData[0][0], 1e-10);
        Assert.assertEquals(2.0, dData[0][1], 1e-10);

        Assert.assertNotNull(m.getDataRef());
    }

    @Test
    public void testScaleAndRoundingMode() {
        BigMatrixImpl m = new BigMatrixImpl();
        m.setScale(32);
        Assert.assertEquals(32, m.getScale());
        m.setRoundingMode(BigDecimal.ROUND_FLOOR);
        Assert.assertEquals(BigDecimal.ROUND_FLOOR, m.getRoundingMode());
    }

    @Test
    public void testGetNorm() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{
                {1.0, -2.0, 3.0},
                {-4.0, 5.0, -6.0}
        });
        Assert.assertEquals(new BigDecimal("9.0"), m.getNorm());
    }

    @Test
    public void testSubMatrix() {
        BigMatrixImpl m = new BigMatrixImpl(testData);

        BigMatrix sub1 = m.getSubMatrix(0, 1, 1, 2);
        Assert.assertEquals(2, sub1.getRowDimension());
        Assert.assertEquals(2, sub1.getColumnDimension());
        Assert.assertEquals(new BigDecimal("2"), sub1.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("3"), sub1.getEntry(0, 1));

        BigMatrix sub2 = m.getSubMatrix(new int[]{0, 2}, new int[]{1, 2});
        Assert.assertEquals(2, sub2.getRowDimension());
        Assert.assertEquals(2, sub2.getColumnDimension());
        Assert.assertEquals(new BigDecimal("2"), sub2.getEntry(0, 0));
        Assert.assertEquals(new BigDecimal("8"), sub2.getEntry(1, 1));
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixInvalidIndices() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.getSubMatrix(-1, 1, 0, 1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixInvalidIndicesEndGreaterThanLength() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.getSubMatrix(0, 4, 0, 1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixArrayEmpty() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.getSubMatrix(new int[]{}, new int[]{0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrixArrayOutOfBounds() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.getSubMatrix(new int[]{0, 5}, new int[]{0, 1});
    }

    @Test
    public void testSetSubMatrix() {
        BigMatrixImpl m = new BigMatrixImpl(3, 3);
        BigDecimal[][] sub = {
                {new BigDecimal("5"), new BigDecimal("6")},
                {new BigDecimal("7"), new BigDecimal("8")}
        };
        m.setSubMatrix(sub, 1, 1);
        Assert.assertEquals(new BigDecimal("5"), m.getEntry(1, 1));
        Assert.assertEquals(new BigDecimal("8"), m.getEntry(2, 2));

        BigMatrixImpl uninit = new BigMatrixImpl();
        uninit.setSubMatrix(sub, 0, 0);
        Assert.assertEquals(2, uninit.getRowDimension());
        Assert.assertEquals(new BigDecimal("5"), uninit.getEntry(0, 0));
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixNegativeIndex() {
        BigMatrixImpl m = new BigMatrixImpl(3, 3);
        m.setSubMatrix(new BigDecimal[][]{{new BigDecimal("1")}}, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixEmptyRow() {
        BigMatrixImpl m = new BigMatrixImpl(3, 3);
        m.setSubMatrix(new BigDecimal[0][0], 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixEmptyCol() {
        BigMatrixImpl m = new BigMatrixImpl(3, 3);
        m.setSubMatrix(new BigDecimal[1][0], 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSubMatrixNonRectangular() {
        BigMatrixImpl m = new BigMatrixImpl(3, 3);
        BigDecimal[][] sub = {
                {new BigDecimal("1"), new BigDecimal("2")},
                {new BigDecimal("3")}
        };
        m.setSubMatrix(sub, 0, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixUninitializedOffset() {
        BigMatrixImpl m = new BigMatrixImpl();
        m.setSubMatrix(new BigDecimal[][]{{new BigDecimal("1")}}, 1, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubMatrixOutOfBounds() {
        BigMatrixImpl m = new BigMatrixImpl(2, 2);
        m.setSubMatrix(new BigDecimal[][]{{new BigDecimal("1"), new BigDecimal("2")}, {new BigDecimal("3"), new BigDecimal("4")}}, 1, 1);
    }

    @Test
    public void testRowAndColumnMatrixAndArrays() {
        BigMatrixImpl m = new BigMatrixImpl(testData);

        BigMatrix rowMat = m.getRowMatrix(1);
        Assert.assertEquals(1, rowMat.getRowDimension());
        Assert.assertEquals(3, rowMat.getColumnDimension());
        Assert.assertEquals(new BigDecimal("5"), rowMat.getEntry(0, 1));

        BigMatrix colMat = m.getColumnMatrix(1);
        Assert.assertEquals(3, colMat.getRowDimension());
        Assert.assertEquals(1, colMat.getColumnDimension());
        Assert.assertEquals(new BigDecimal("5"), colMat.getEntry(1, 0));

        BigDecimal[] row = m.getRow(0);
        Assert.assertEquals(3, row.length);
        Assert.assertEquals(new BigDecimal("2"), row[1]);

        double[] rowD = m.getRowAsDoubleArray(0);
        Assert.assertEquals(3, rowD.length);
        Assert.assertEquals(2.0, rowD[1], 1e-10);

        BigDecimal[] col = m.getColumn(0);
        Assert.assertEquals(3, col.length);
        Assert.assertEquals(new BigDecimal("2"), col[1]);

        double[] colD = m.getColumnAsDoubleArray(0);
        Assert.assertEquals(3, colD.length);
        Assert.assertEquals(2.0, colD[1], 1e-10);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowInvalid() {
        new BigMatrixImpl(testData).getRow(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowMatrixInvalid() {
        new BigMatrixImpl(testData).getRowMatrix(-1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowAsDoubleArrayInvalid() {
        new BigMatrixImpl(testData).getRowAsDoubleArray(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnInvalid() {
        new BigMatrixImpl(testData).getColumn(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnMatrixInvalid() {
        new BigMatrixImpl(testData).getColumnMatrix(-1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnAsDoubleArrayInvalid() {
        new BigMatrixImpl(testData).getColumnAsDoubleArray(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryInvalid() {
        new BigMatrixImpl(testData).getEntry(5, 5);
    }

    @Test
    public void testTranspose() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{{1, 2, 3}, {4, 5, 6}});
        BigMatrix t = m.transpose();
        Assert.assertEquals(3, t.getRowDimension());
        Assert.assertEquals(2, t.getColumnDimension());
        Assert.assertEquals(new BigDecimal("4.0"), t.getEntry(0, 1));
    }

    @Test
    public void testTrace() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        Assert.assertEquals(new BigDecimal("14"), m.getTrace());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraceNonSquare() {
        new BigMatrixImpl(2, 3).getTrace();
    }

    @Test
    public void testOperate() {
        BigMatrixImpl m = new BigMatrixImpl(testDataDouble);
        BigDecimal[] v = {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")};
        BigDecimal[] res = m.operate(v);
        Assert.assertEquals(new BigDecimal("14.0"), res[0]);
        Assert.assertEquals(new BigDecimal("21.0"), res[1]);
        Assert.assertEquals(new BigDecimal("25.0"), res[2]);

        double[] vDouble = {1.0, 2.0, 3.0};
        BigDecimal[] resDouble = m.operate(vDouble);
        Assert.assertEquals(res[0], resDouble[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateDimensionMismatch() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.operate(new BigDecimal[]{new BigDecimal("1")});
    }

    @Test
    public void testPreMultiplyVector() {
        BigMatrixImpl m = new BigMatrixImpl(testDataDouble);
        BigDecimal[] v = {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")};
        BigDecimal[] res = m.preMultiply(v);
        Assert.assertEquals(new BigDecimal("8.0"), res[0]);
        Assert.assertEquals(new BigDecimal("12.0"), res[1]);
        Assert.assertEquals(new BigDecimal("33.0"), res[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreMultiplyVectorMismatch() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.preMultiply(new BigDecimal[]{new BigDecimal("1")});
    }

    @Test
    public void testLUDecompositionAndSolve() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{
                {2, 1, 1},
                {4, 1, 0},
                {-2, 2, 1}
        });

        BigDecimal det = m.getDeterminant();
        Assert.assertEquals(new BigDecimal("-6"), det.setScale(0, BigDecimal.ROUND_HALF_UP));

        Assert.assertFalse(m.isSingular());
        Assert.assertNotNull(m.getLUMatrix());
        Assert.assertNotNull(m.getPermutation());

        BigDecimal[] b = {new BigDecimal("1"), new BigDecimal("-2"), new BigDecimal("7")};
        BigDecimal[] x = m.solve(b);
        Assert.assertEquals(new BigDecimal("-1"), x[0].setScale(0, BigDecimal.ROUND_HALF_UP));
        Assert.assertEquals(new BigDecimal("2"), x[1].setScale(0, BigDecimal.ROUND_HALF_UP));
        Assert.assertEquals(new BigDecimal("1"), x[2].setScale(0, BigDecimal.ROUND_HALF_UP));

        double[] bDouble = {1.0, -2.0, 7.0};
        BigDecimal[] xDouble = m.solve(bDouble);
        Assert.assertEquals(x[0], xDouble[0]);

        BigMatrix inv = m.inverse();
        BigMatrix id = m.multiply(inv);
        Assert.assertEquals(1.0, id.getEntryAsDouble(0, 0), 1e-10);
        Assert.assertEquals(0.0, id.getEntryAsDouble(0, 1), 1e-10);
    }

    @Test(expected = InvalidMatrixException.class)
    public void testDeterminantNonSquare() {
        new BigMatrixImpl(2, 3).getDeterminant();
    }

    @Test
    public void testSingularMatrix() {
        BigMatrixImpl singular = new BigMatrixImpl(new double[][]{
                {1, 2},
                {2, 4}
        });
        Assert.assertTrue(singular.isSingular());
        Assert.assertEquals(BigMatrixImpl.ZERO, singular.getDeterminant());
    }

    @Test(expected = InvalidMatrixException.class)
    public void testLUDecomposeNonSquare() {
        new BigMatrixImpl(2, 3).luDecompose();
    }

    @Test(expected = InvalidMatrixException.class)
    public void testSolveSingularMatrix() {
        BigMatrixImpl singular = new BigMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        singular.solve(new BigDecimal[]{new BigDecimal("1"), new BigDecimal("2")});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveVectorDimensionMismatch() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.solve(new BigDecimal[]{new BigDecimal("1")});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveMatrixDimensionMismatch() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.solve(new BigMatrixImpl(2, 2));
    }

    @Test
    public void testToString() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        String str = m.toString();
        Assert.assertTrue(str.startsWith("BigMatrixImpl{"));
        Assert.assertTrue(str.contains("1.0"));

        BigMatrixImpl empty = new BigMatrixImpl();
        Assert.assertEquals("BigMatrixImpl{}", empty.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        BigMatrixImpl m1 = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testData);
        BigMatrixImpl m3 = new BigMatrixImpl(new double[][]{{1, 2}, {3, 4}});

        Assert.assertTrue(m1.equals(m1));
        Assert.assertTrue(m1.equals(m2));
        Assert.assertEquals(m1.hashCode(), m2.hashCode());

        Assert.assertFalse(m1.equals(null));
        Assert.assertFalse(m1.equals("Not a matrix"));
        Assert.assertFalse(m1.equals(m3));

        BigMatrixImpl m4 = new BigMatrixImpl(new BigDecimal[][]{
                {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")},
                {new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("3")},
                {new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("9")}
        });
        Assert.assertFalse(m1.equals(m4));
    }
}
