package org.apache.commons.math.linear;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MatrixDimensionMismatchException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

public class OpenMapRealMatrixTest {

    @Test
    public void testConstructorAndDimensions() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 4);
        Assert.assertEquals(3, matrix.getRowDimension());
        Assert.assertEquals(4, matrix.getColumnDimension());
        Assert.assertEquals(0.0, matrix.getEntry(0, 0), 1e-15);
    }

    @Test
    public void testCopyConstructorAndCopy() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, 1, 3.5);
        matrix.setEntry(1, 0, -2.0);

        OpenMapRealMatrix copy1 = new OpenMapRealMatrix(matrix);
        Assert.assertEquals(2, copy1.getRowDimension());
        Assert.assertEquals(2, copy1.getColumnDimension());
        Assert.assertEquals(3.5, copy1.getEntry(0, 1), 1e-15);
        Assert.assertEquals(-2.0, copy1.getEntry(1, 0), 1e-15);
        Assert.assertEquals(0.0, copy1.getEntry(0, 0), 1e-15);

        OpenMapRealMatrix copy2 = matrix.copy();
        Assert.assertEquals(3.5, copy2.getEntry(0, 1), 1e-15);
        Assert.assertEquals(-2.0, copy2.getEntry(1, 0), 1e-15);

        copy2.setEntry(0, 1, 10.0);
        Assert.assertEquals(3.5, matrix.getEntry(0, 1), 1e-15);
    }

    @Test
    public void testCreateMatrix() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix created = matrix.createMatrix(4, 5);
        Assert.assertEquals(4, created.getRowDimension());
        Assert.assertEquals(5, created.getColumnDimension());
    }

    @Test
    public void testSetEntryAndGetEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);
        matrix.setEntry(1, 2, 5.0);
        Assert.assertEquals(5.0, matrix.getEntry(1, 2), 1e-15);
        Assert.assertEquals(0.0, matrix.getEntry(0, 0), 1e-15);

        matrix.setEntry(1, 2, 0.0);
        Assert.assertEquals(0.0, matrix.getEntry(1, 2), 1e-15);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryInvalidRow() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.getEntry(2, 0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryInvalidNegativeRow() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.getEntry(-1, 0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryInvalidCol() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.getEntry(0, 2);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryInvalidRow() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(2, 0, 1.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryInvalidCol() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, 2, 1.0);
    }

    @Test
    public void testAddToEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.addToEntry(0, 1, 2.5);
        Assert.assertEquals(2.5, matrix.getEntry(0, 1), 1e-15);

        matrix.addToEntry(0, 1, 1.5);
        Assert.assertEquals(4.0, matrix.getEntry(0, 1), 1e-15);

        matrix.addToEntry(0, 1, -4.0);
        Assert.assertEquals(0.0, matrix.getEntry(0, 1), 1e-15);
    }

    @Test(expected = OutOfRangeException.class)
    public void testAddToEntryInvalidRow() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.addToEntry(2, 0, 1.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testAddToEntryInvalidCol() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.addToEntry(0, 2, 1.0);
    }

    @Test
    public void testMultiplyEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, 0, 3.0);
        matrix.multiplyEntry(0, 0, 2.0);
        Assert.assertEquals(6.0, matrix.getEntry(0, 0), 1e-15);

        matrix.multiplyEntry(0, 0, 0.0);
        Assert.assertEquals(0.0, matrix.getEntry(0, 0), 1e-15);

        matrix.multiplyEntry(1, 1, 5.0);
        Assert.assertEquals(0.0, matrix.getEntry(1, 1), 1e-15);
    }

    @Test(expected = OutOfRangeException.class)
    public void testMultiplyEntryInvalidRow() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.multiplyEntry(2, 0, 2.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testMultiplyEntryInvalidCol() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.multiplyEntry(0, 2, 2.0);
    }

    @Test
    public void testAdd() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(1, 1, 2.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 3.0);
        m2.setEntry(0, 1, 4.0);
        m2.setEntry(1, 1, -2.0);

        OpenMapRealMatrix sum = m1.add(m2);
        Assert.assertEquals(4.0, sum.getEntry(0, 0), 1e-15);
        Assert.assertEquals(4.0, sum.getEntry(0, 1), 1e-15);
        Assert.assertEquals(0.0, sum.getEntry(1, 0), 1e-15);
        Assert.assertEquals(0.0, sum.getEntry(1, 1), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testAddIncompatibleDimensions() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 3);
        m1.add(m2);
    }

    @Test
    public void testSubtractOpenMapRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 5.0);
        m1.setEntry(1, 1, 3.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 2.0);
        m2.setEntry(0, 1, 1.0);
        m2.setEntry(1, 1, 3.0);

        OpenMapRealMatrix diff = m1.subtract(m2);
        Assert.assertEquals(3.0, diff.getEntry(0, 0), 1e-15);
        Assert.assertEquals(-1.0, diff.getEntry(0, 1), 1e-15);
        Assert.assertEquals(0.0, diff.getEntry(1, 0), 1e-15);
        Assert.assertEquals(0.0, diff.getEntry(1, 1), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSubtractIncompatibleDimensions() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(3, 2);
        m1.subtract(m2);
    }

    @Test
    public void testSubtractGenericRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 4.0);
        m1.setEntry(1, 1, 2.0);

        RealMatrix m2 = new Array2DRowRealMatrix(new double[][]{{1.0, 2.0}, {3.0, 2.0}});
        OpenMapRealMatrix diff = m1.subtract(m2);

        Assert.assertEquals(3.0, diff.getEntry(0, 0), 1e-15);
        Assert.assertEquals(-2.0, diff.getEntry(0, 1), 1e-15);
        Assert.assertEquals(-3.0, diff.getEntry(1, 0), 1e-15);
        Assert.assertEquals(0.0, diff.getEntry(1, 1), 1e-15);
    }

    @Test
    public void testSubtractGenericRealMatrixAsOpenMap() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 4.0);

        RealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 1.0);

        OpenMapRealMatrix diff = m1.subtract(m2);
        Assert.assertEquals(3.0, diff.getEntry(0, 0), 1e-15);
    }

    @Test
    public void testMultiplyOpenMapRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(0, 2, 2.0);
        m1.setEntry(1, 1, 3.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(3, 2);
        m2.setEntry(0, 0, 2.0);
        m2.setEntry(2, 0, -1.0);
        m2.setEntry(2, 1, 4.0);
        m2.setEntry(1, 1, 1.0);

        OpenMapRealMatrix result = m1.multiply(m2);
        Assert.assertEquals(2, result.getRowDimension());
        Assert.assertEquals(2, result.getColumnDimension());
        Assert.assertEquals(0.0, result.getEntry(0, 0), 1e-15);
        Assert.assertEquals(8.0, result.getEntry(0, 1), 1e-15);
        Assert.assertEquals(0.0, result.getEntry(1, 0), 1e-15);
        Assert.assertEquals(3.0, result.getEntry(1, 1), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testMultiplyOpenMapIncompatible() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m1.multiply(m2);
    }

    @Test
    public void testMultiplyGenericRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(0, 2, 2.0);
        m1.setEntry(1, 1, 3.0);

        RealMatrix m2 = new Array2DRowRealMatrix(new double[][]{
            {2.0, 0.0},
            {0.0, 1.0},
            {-1.0, 4.0}
        });

        RealMatrix result = m1.multiply(m2);
        Assert.assertTrue(result instanceof BlockRealMatrix);
        Assert.assertEquals(2, result.getRowDimension());
        Assert.assertEquals(2, result.getColumnDimension());
        Assert.assertEquals(0.0, result.getEntry(0, 0), 1e-15);
        Assert.assertEquals(8.0, result.getEntry(0, 1), 1e-15);
        Assert.assertEquals(0.0, result.getEntry(1, 0), 1e-15);
        Assert.assertEquals(3.0, result.getEntry(1, 1), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testMultiplyGenericRealMatrixIncompatible() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        RealMatrix m2 = new Array2DRowRealMatrix(new double[][]{{1.0, 2.0}});
        m1.multiply(m2);
    }
}
