package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import java.util.Iterator;

public class OpenMapRealVectorTest {

    @Test
    public void testDefaultConstructor() {
        OpenMapRealVector v = new OpenMapRealVector();
        assertEquals(0, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testDimensionConstructor() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertEquals(3, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
    }

    @Test
    public void testDimensionEpsilonConstructor() {
        OpenMapRealVector v = new OpenMapRealVector(3, 1e-8);
        assertEquals(3, v.getDimension());
        v.setEntry(0, 1e-9);
        assertEquals(0.0, v.getEntry(0), 0.0);
        v.setEntry(0, 1e-7);
        assertEquals(1e-7, v.getEntry(0), 0.0);
    }

    @Test
    public void testDimensionExpectedSizeConstructor() {
        OpenMapRealVector v = new OpenMapRealVector(5, 2);
        assertEquals(5, v.getDimension());
        v.setEntry(0, 1.0);
        assertEquals(1.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testDimensionExpectedSizeEpsilonConstructor() {
        OpenMapRealVector v = new OpenMapRealVector(5, 2, 1e-10);
        assertEquals(5, v.getDimension());
        v.setEntry(0, 1e-11);
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testDoubleArrayConstructor() {
        double[] values = {1.0, 0.0, 2.0, 1e-13};
        OpenMapRealVector v = new OpenMapRealVector(values);
        assertEquals(4, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(2.0, v.getEntry(2), 0.0);
        assertEquals(0.0, v.getEntry(3), 0.0);
    }

    @Test
    public void testDoubleArrayEpsilonConstructor() {
        double[] values = {1.0, 1e-5, 1e-7};
        OpenMapRealVector v = new OpenMapRealVector(values, 1e-6);
        assertEquals(3, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(1e-5, v.getEntry(1), 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
    }

    @Test
    public void testDoubleObjectArrayConstructor() {
        Double[] values = {1.0, null, 2.0};
        try {
            new OpenMapRealVector(values);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Double[] values2 = {1.0, 0.0, 2.0};
        OpenMapRealVector v = new OpenMapRealVector(values2);
        assertEquals(3, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(2.0, v.getEntry(2), 0.0);
    }

    @Test
    public void testDoubleObjectArrayEpsilonConstructor() {
        Double[] values = {1.0, 1e-5, 1e-7};
        OpenMapRealVector v = new OpenMapRealVector(values, 1e-6);
        assertEquals(3, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(1e-5, v.getEntry(1), 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
    }

    @Test
    public void testCopyConstructorOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(v1);
        assertEquals(3, v2.getDimension());
        assertEquals(1.0, v2.getEntry(0), 0.0);
        assertEquals(0.0, v2.getEntry(1), 0.0);
        assertEquals(2.0, v2.getEntry(2), 0.0);
    }

    @Test
    public void testCopyConstructorRealVector() {
        RealVector v1 = new ArrayRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(v1);
        assertEquals(3, v2.getDimension());
        assertEquals(1.0, v2.getEntry(0), 0.0);
        assertEquals(0.0, v2.getEntry(1), 0.0);
        assertEquals(2.0, v2.getEntry(2), 0.0);
    }

    @Test
    public void testGetDimension() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        assertEquals(5, v.getDimension());
    }

    @Test
    public void testIsDefaultValue() {
        OpenMapRealVector v = new OpenMapRealVector(1, 1e-5);
        assertTrue(v.isDefaultValue(1e-6));
        assertFalse(v.isDefaultValue(1e-4));
    }

    @Test
    public void testAddOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(1, 3.0);
        v2.setEntry(2, 4.0);
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(1.0, sum.getEntry(0), 0.0);
        assertEquals(3.0, sum.getEntry(1), 0.0);
        assertEquals(6.0, sum.getEntry(2), 0.0);
    }

    @Test
    public void testAddOpenMapDifferentSizes() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 2.0);
        v2.setEntry(1, 3.0);
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(3.0, sum.getEntry(0), 0.0);
        assertEquals(3.0, sum.getEntry(1), 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test
    public void testAddRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        RealVector v2 = new ArrayRealVector(new double[]{2.0, 0.0, 3.0});
        RealVector sum = v1.add(v2);
        assertEquals(3.0, sum.getEntry(0), 0.0);
        assertEquals(0.0, sum.getEntry(1), 0.0);
        assertEquals(3.0, sum.getEntry(2), 0.0);
    }

    @Test
    public void testSubtractOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 5.0);
        v1.setEntry(2, 3.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 2.0);
        v2.setEntry(1, 1.0);
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(3.0, diff.getEntry(0), 0.0);
        assertEquals(-1.0, diff.getEntry(1), 0.0);
        assertEquals(3.0, diff.getEntry(2), 0.0);
    }

    @Test
    public void testSubtractRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 5.0);
        RealVector v2 = new ArrayRealVector(new double[]{2.0, 1.0, 0.0});
        RealVector diff = v1.subtract(v2);
        assertEquals(3.0, diff.getEntry(0), 0.0);
        assertEquals(-1.0, diff.getEntry(1), 0.0);
        assertEquals(0.0, diff.getEntry(2), 0.0);
    }

    @Test
    public void testDotProductOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 3.0);
        v2.setEntry(1, 4.0);
        v2.setEntry(2, 5.0);
        assertEquals(1.0*3.0 + 2.0*5.0, v1.dotProduct(v2), 0.0);
    }

    @Test
    public void testDotProductRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        RealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0, 5.0});
        assertEquals(1.0*3.0 + 2.0*5.0, v1.dotProduct(v2), 0.0);
    }

    @Test
    public void testGetDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 4.0);
        v2.setEntry(1, 5.0);
        double expected = Math.sqrt((1-4)*(1-4) + (0-5)*(0-5) + (2-0)*(2-0));
        assertEquals(expected, v1.getDistance(v2), 1e-15);
    }

    @Test
    public void testGetDistanceRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        RealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 0.0});
        double expected = Math.sqrt((1-4)*(1-4) + (0-5)*(0-5) + (2-0)*(2-0));
        assertEquals(expected, v1.getDistance(v2), 1e-15);
    }

    @Test
    public void testGetL1DistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 4.0);
        v2.setEntry(1, 5.0);
        double expected = Math.abs(1-4) + Math.abs(0-5) + Math.abs(2-0);
        assertEquals(expected, v1.getL1Distance(v2), 0.0);
    }

    @Test
    public void testGetL1DistanceRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        RealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 0.0});
        double expected = Math.abs(1-4) + Math.abs(0-5) + Math.abs(2-0);
        assertEquals(expected, v1.getL1Distance(v2), 0.0);
    }

    @Test
    public void testGetLInfDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 4.0);
        v2.setEntry(1, 5.0);
        double expected = Math.max(Math.max(Math.abs(1-4), Math.abs(0-5)), Math.abs(2-0));
        assertEquals(expected, v1.getLInfDistance(v2), 0.0);
    }

    @Test
    public void testGetLInfDistanceRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        RealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 0.0});
        double expected = Math.max(Math.max(Math.abs(1-4), Math.abs(0-5)), Math.abs(2-0));
        assertEquals(expected, v1.getLInfDistance(v2), 0.0);
    }

    @Test
    public void testEbeDivide() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 6.0);
        v1.setEntry(2, 8.0);
        RealVector v2 = new ArrayRealVector(new double[]{2.0, 0.0, 4.0});
        OpenMapRealVector res = v1.ebeDivide(v2);
        assertEquals(3.0, res.getEntry(0), 0.0);
        assertEquals(0.0, res.getEntry(1), 0.0);
        assertEquals(2.0, res.getEntry(2), 0.0);
    }

    @Test
    public void testEbeDivideZeroByZero() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(new double[]{0.0, 0.0});
        OpenMapRealVector res = v1.ebeDivide(v2);
        assertTrue(Double.isNaN(res.getEntry(0)));
        assertTrue(Double.isNaN(res.getEntry(1)));
    }

    @Test
    public void testEbeMultiply() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 2.0);
        v1.setEntry(2, 3.0);
        RealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        OpenMapRealVector res = v1.ebeMultiply(v2);
        assertEquals(8.0, res.getEntry(0), 0.0);
        assertEquals(0.0, res.getEntry(1), 0.0);
        assertEquals(18.0, res.getEntry(2), 0.0);
    }

    @Test
    public void testEbeMultiplyWithNaN() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(0, 0.0);
        RealVector v2 = new ArrayRealVector(new double[]{Double.NaN, 1.0});
        OpenMapRealVector res = v1.ebeMultiply(v2);
        assertTrue(Double.isNaN(res.getEntry(0)));
        assertEquals(0.0, res.getEntry(1), 0.0);
    }

    @Test
    public void testGetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(1, 2.0);
        v.setEntry(3, 4.0);
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertEquals(2.0, sub.getEntry(0), 0.0);
        assertEquals(0.0, sub.getEntry(1), 0.0);
        assertEquals(4.0, sub.getEntry(2), 0.0);
    }

    @Test(expected = NotPositiveException.class)
    public void testGetSubVectorNegativeLength() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetSubVectorIndexOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(2, 2);
    }

    @Test
    public void testSetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        assertEquals(1.0, v.getEntry(0), 0.0);
        v.setEntry(0, 0.0);
        assertEquals(0.0, v.getEntry(0), 0.0);
        v.setEntry(0, 1e-13);
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testSet() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.set(2.5);
        assertEquals(2.5, v.getEntry(0), 0.0);
        assertEquals(2.5, v.getEntry(1), 0.0);
        assertEquals(2.5, v.getEntry(2), 0.0);
    }

    @Test
    public void testSetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        RealVector sub = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setSubVector(2, sub);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(1.0, v.getEntry(2), 0.0);
        assertEquals(2.0, v.getEntry(3), 0.0);
        assertEquals(0.0, v.getEntry(4), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetSubVectorIndexOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        RealVector sub = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setSubVector(2, sub);
    }

    @Test
    public void testUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 3.0);
        v.setEntry(1, 4.0);
        v.unitize();
        assertEquals(3.0/5.0, v.getEntry(0), 1e-15);
        assertEquals(4.0/5.0, v.getEntry(1), 1e-15);
        assertEquals(0.0, v.getEntry(2), 0.0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    @Test
    public void testUnitVector() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 3.0);
        v.setEntry(1, 4.0);
        OpenMapRealVector unit = v.unitVector();
        assertEquals(3.0/5.0, unit.getEntry(0), 1e-15);
        assertEquals(4.0/5.0, unit.getEntry(1), 1e-15);
        assertEquals(0.0, unit.getEntry(2), 0.0);
        assertEquals(3.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testMapAdd() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        v.setEntry(2, 2.0);
        OpenMapRealVector res = v.mapAdd(5.0);
        assertEquals(6.0, res.getEntry(0), 0.0);
        assertEquals(5.0, res.getEntry(1), 0.0);
        assertEquals(7.0, res.getEntry(2), 0.0);
        assertEquals(1.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testMapAddToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        v.setEntry(2, 2.0);
        v.mapAddToSelf(5.0);
        assertEquals(6.0, v.getEntry(0), 0.0);
        assertEquals(5.0, v.getEntry(1), 0.0);
        assertEquals(7.0, v.getEntry(2), 0.0);
    }

    @Test
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(1, 2.0);
        RealVector v2 = new ArrayRealVector(new double[]{3.0, 0.0, 4.0});
        RealVector proj = v1.projection(v2);
        double dot = v1.dotProduct(v2);
        double normSq = v2.dotProduct(v2);
        double factor = dot / normSq;
        assertEquals(factor * 3.0, proj.getEntry(0), 1e-15);
        assertEquals(0.0, proj.getEntry(1), 0.0);
        assertEquals(factor * 4.0, proj.getEntry(2), 1e-15);
    }

    @Test
    public void testIsInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertTrue(v.isInfinite());
        v.setEntry(0, Double.NaN);
        assertFalse(v.isInfinite());
    }

    @Test
    public void testIsNaN() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertFalse(v.isNaN());
        v.setEntry(0, Double.NaN);
        assertTrue(v.isNaN());
    }

    @Test
    public void testEquals() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 1.0);
        v2.setEntry(2, 2.0);
        assertTrue(v1.equals(v2));
        v2.setEntry(1, 0.0);
        assertTrue(v1.equals(v2));
        v2.setEntry(1, 1e-13);
        assertFalse(v1.equals(v2));
        OpenMapRealVector v3 = new OpenMapRealVector(3, 1e-5);
        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 1.0);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testGetSparsity() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        v.setEntry(0, 1.0);
        v.setEntry(5, 2.0);
        assertEquals(0.2, v.getSparsity(), 0.0);
    }

    @Test
    public void testSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(0, 1.0);
        v.setEntry(2, 2.0);
        v.setEntry(4, 3.0);
        Iterator<RealVector.Entry> it = v.sparseIterator();
        assertTrue(it.hasNext());
        RealVector.Entry e = it.next();
        assertEquals(1.0, e.getValue(), 0.0);
        assertTrue(it.hasNext());
        e = it.next();
        assertEquals(2.0, e.getValue(), 0.0);
        assertTrue(it.hasNext());
        e = it.next();
        assertEquals(3.0, e.getValue(), 0.0);
        assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemove() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        Iterator<RealVector.Entry> it = v.sparseIterator();
        it.next();
        it.remove();
    }

    @Test
    public void testAppendOpenMapVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(0, 1.0);
        v1.setEntry(1, 2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 3.0);
        v2.setEntry(2, 4.0);
        OpenMapRealVector res = v1.append(v2);
        assertEquals(5, res.getDimension());
        assertEquals(1.0, res.getEntry(0), 0.0);
        assertEquals(2.0, res.getEntry(1), 0.0);
        assertEquals(3.0, res.getEntry(2), 0.0);
        assertEquals(0.0, res.getEntry(3), 0.0);
        assertEquals(4.0, res.getEntry(4), 0.0);
    }

    @Test
    public void testAppendRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(0, 1.0);
        RealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        OpenMapRealVector res = v1.append(v2);
        assertEquals(4, res.getDimension());
        assertEquals(1.0, res.getEntry(0), 0.0);
        assertEquals(0.0, res.getEntry(1), 0.0);
        assertEquals(3.0, res.getEntry(2), 0.0);
        assertEquals(4.0, res.getEntry(3), 0.0);
    }

    @Test
    public void testAppendDouble() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(0, 1.0);
        OpenMapRealVector res = v.append(5.0);
        assertEquals(3, res.getDimension());
        assertEquals(1.0, res.getEntry(0), 0.0);
        assertEquals(0.0, res.getEntry(1), 0.0);
        assertEquals(5.0, res.getEntry(2), 0.0);
    }

    @Test
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        OpenMapRealVector copy = v.copy();
        assertEquals(v, copy);
        copy.setEntry(0, 2.0);
        assertEquals(1.0, v.getEntry(0), 0.0);
    }

    @Test
    public void testToArray() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        v.setEntry(2, 2.0);
        double[] arr = v.toArray();
        assertArrayEquals(new double[]{1.0, 0.0, 2.0}, arr, 0.0);
    }

    @Test
    public void testOpenMapEntry() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 5.0);
        Iterator<RealVector.Entry> it = v.sparseIterator();
        RealVector.Entry e = it.next();
        assertEquals(0, e.getIndex());
        assertEquals(5.0, e.getValue(), 0.0);
        e.setValue(10.0);
        assertEquals(10.0, v.getEntry(0), 0.0);
    }
}
