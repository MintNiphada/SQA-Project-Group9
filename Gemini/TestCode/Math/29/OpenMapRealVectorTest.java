package org.apache.commons.math3.linear;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class OpenMapRealVectorTest {

    @Test
    public void testConstructors() {
        OpenMapRealVector v0 = new OpenMapRealVector();
        Assert.assertEquals(0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(5);
        Assert.assertEquals(5, v1.getDimension());
        Assert.assertEquals(0.0, v1.getEntry(0), 1e-15);

        OpenMapRealVector v2 = new OpenMapRealVector(5, 1e-6);
        Assert.assertEquals(5, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(5, 2);
        Assert.assertEquals(5, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(5, 2, 1e-6);
        Assert.assertEquals(5, v4.getDimension());

        double[] dArray = new double[]{0.0, 1.0, 0.0, 2.0};
        OpenMapRealVector v5 = new OpenMapRealVector(dArray);
        Assert.assertEquals(4, v5.getDimension());
        Assert.assertEquals(1.0, v5.getEntry(1), 1e-15);
        Assert.assertEquals(2.0, v5.getEntry(3), 1e-15);

        OpenMapRealVector v6 = new OpenMapRealVector(dArray, 1e-6);
        Assert.assertEquals(4, v6.getDimension());

        Double[] objArray = new Double[]{0.0, 3.0, 0.0, 4.0};
        OpenMapRealVector v7 = new OpenMapRealVector(objArray);
        Assert.assertEquals(4, v7.getDimension());
        Assert.assertEquals(3.0, v7.getEntry(1), 1e-15);

        OpenMapRealVector v8 = new OpenMapRealVector(objArray, 1e-6);
        Assert.assertEquals(4, v8.getDimension());

        OpenMapRealVector v9 = new OpenMapRealVector(v5);
        Assert.assertEquals(4, v9.getDimension());
        Assert.assertEquals(1.0, v9.getEntry(1), 1e-15);

        RealVector arrVector = new ArrayRealVector(new double[]{0.0, 5.0, 0.0});
        OpenMapRealVector v10 = new OpenMapRealVector(arrVector);
        Assert.assertEquals(3, v10.getDimension());
        Assert.assertEquals(5.0, v10.getEntry(1), 1e-15);

        OpenMapRealVector vResized = new OpenMapRealVector(v5, 2);
        Assert.assertEquals(6, vResized.getDimension());
        Assert.assertEquals(1.0, vResized.getEntry(1), 1e-15);
        Assert.assertEquals(0.0, vResized.getEntry(5), 1e-15);
    }

    @Test
    public void testIsDefaultValue() {
        OpenMapRealVector v = new OpenMapRealVector(3, 1e-5);
        Assert.assertTrue(v.isDefaultValue(1e-6));
        Assert.assertTrue(v.isDefaultValue(-1e-6));
        Assert.assertFalse(v.isDefaultValue(1e-4));
        Assert.assertFalse(v.isDefaultValue(-1e-4));
    }

    @Test
    public void testAdd() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 4.0});
        OpenMapRealVector sum1 = v1.add(v2);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 7.0}, sum1.toArray(), 1e-15);

        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0.0, 2.0, 0.0});
        OpenMapRealVector sum2 = v1.add(v3);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, sum2.toArray(), 1e-15);

        RealVector arrVec = new ArrayRealVector(new double[]{1.0, 1.0, 1.0});
        RealVector sum3 = v1.add(arrVec);
        Assert.assertArrayEquals(new double[]{2.0, 1.0, 4.0}, sum3.toArray(), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testAddGenericDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        RealVector v2 = new ArrayRealVector(4);
        v1.add(v2);
    }

    @Test
    public void testAppend() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0});
        OpenMapRealVector app1 = v1.append(v2);
        Assert.assertEquals(4, app1.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0, 2.0}, app1.toArray(), 1e-15);

        RealVector arrVec = new ArrayRealVector(new double[]{3.0, 4.0});
        OpenMapRealVector app2 = v1.append(arrVec);
        Assert.assertEquals(4, app2.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 3.0, 4.0}, app2.toArray(), 1e-15);

        OpenMapRealVector app3 = v1.append(5.0);
        Assert.assertEquals(3, app3.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 5.0}, app3.toArray(), 1e-15);

        RealVector app4 = v1.append((RealVector) v2);
        Assert.assertEquals(4, app4.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0, 2.0}, app4.toArray(), 1e-15);
    }

    @Test
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0});
        OpenMapRealVector copy = v.copy();
        Assert.assertEquals(v, copy);
        Assert.assertNotSame(v, copy);
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 0.0, 5.0});
        Assert.assertEquals(26.0, v1.dotProduct(v2), 1e-15);
        Assert.assertEquals(26.0, v2.dotProduct(v1), 1e-15);

        RealVector arrVec = new ArrayRealVector(new double[]{2.0, 3.0, 1.0, 5.0});
        Assert.assertEquals(28.0, v1.dotProduct(arrVec), 1e-15);
        Assert.assertEquals(28.0, v1.dotProduct((RealVector) v2), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.dotProduct(v2);
    }

    @Test
    public void testEbeDivideAndMultiply() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2.0, 0.0, 6.0});
        RealVector v2 = new ArrayRealVector(new double[]{2.0, 2.0, 3.0});
        OpenMapRealVector div = v1.ebeDivide(v2);
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 2.0}, div.toArray(), 1e-15);

        OpenMapRealVector mul = v1.ebeMultiply(v2);
        Assert.assertArrayEquals(new double[]{4.0, 0.0, 18.0}, mul.toArray(), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeDivideMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.ebeDivide(v2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeMultiplyMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.ebeMultiply(v2);
    }

    @Test
    public void testGetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 1.0, 2.0, 0.0, 3.0});
        OpenMapRealVector sub = v.getSubVector(1, 3);
        Assert.assertEquals(3, sub.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 0.0}, sub.toArray(), 1e-15);

        OpenMapRealVector emptySub = v.getSubVector(2, 0);
        Assert.assertEquals(0, emptySub.getDimension());
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(-1, 2);
    }

    @Test(expected = NotPositiveException.class)
    public void testGetSubVectorNegativeLength() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(1, -1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(3, 4);
    }

    @Test
    public void testDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 3.0, 4.0});
        RealVector arrVec = new ArrayRealVector(new double[]{0.0, 2.0, 3.0, 4.0});

        double expectedDist = Math.sqrt(1.0 + 4.0 + 0.0 + 16.0);
        Assert.assertEquals(expectedDist, v1.getDistance(v2), 1e-15);
        Assert.assertEquals(expectedDist, v1.getDistance(arrVec), 1e-15);
        Assert.assertEquals(expectedDist, v1.getDistance((RealVector) v2), 1e-15);

        double expectedL1 = 1.0 + 2.0 + 0.0 + 4.0;
        Assert.assertEquals(expectedL1, v1.getL1Distance(v2), 1e-15);
        Assert.assertEquals(expectedL1, v1.getL1Distance(arrVec), 1e-15);
        Assert.assertEquals(expectedL1, v1.getL1Distance((RealVector) v2), 1e-15);

        double expectedLInf = 4.0;
        Assert.assertEquals(expectedLInf, v1.getLInfDistance(arrVec), 1e-15);
        Assert.assertEquals(expectedLInf, v1.getLInfDistance((RealVector) v2), 1e-15);

        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0.0, 0.0});
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{0.0, 5.0});
        Assert.assertEquals(5.0, v3.getLInfDistance((RealVector) v4), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.getDistance(v2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testL1DistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.getL1Distance(v2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testLInfDistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.getLInfDistance(v2);
    }

    @Test
    public void testGetAndSetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(1, 2.5);
        Assert.assertEquals(2.5, v.getEntry(1), 1e-15);
        v.setEntry(1, 0.0);
        Assert.assertEquals(0.0, v.getEntry(1), 1e-15);
        v.setEntry(2, 0.0);
        Assert.assertEquals(0.0, v.getEntry(2), 1e-15);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(3);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(-1, 2.0);
    }

    @Test
    public void testIsInfiniteAndNaN() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        Assert.assertFalse(v.isInfinite());
        Assert.assertFalse(v.isNaN());

        v.setEntry(0, Double.POSITIVE_INFINITY);
        Assert.assertTrue(v.isInfinite());
        Assert.assertFalse(v.isNaN());

        v.setEntry(1, Double.NaN);
        Assert.assertFalse(v.isInfinite());
        Assert.assertTrue(v.isNaN());
    }

    @Test
    public void testMapAddAndSet() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector mapped = v.mapAdd(2.0);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 5.0}, mapped.toArray(), 1e-15);
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 3.0}, v.toArray(), 1e-15);

        v.set(5.0);
        Assert.assertArrayEquals(new double[]{5.0, 5.0, 5.0}, v.toArray(), 1e-15);
    }

    @Test
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        RealVector v2 = new ArrayRealVector(new double[]{0.0, 1.0, 0.0});
        RealVector proj = v1.projection(v2);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 0.0}, proj.toArray(), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testProjectionDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.projection(v2);
    }

    @Test
    public void testSetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        RealVector sub = new ArrayRealVector(new double[]{2.0, 3.0});
        v.setSubVector(1, sub);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 3.0, 0.0, 0.0}, v.toArray(), 1e-15);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setSubVector(-1, new ArrayRealVector(2));
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setSubVector(4, new ArrayRealVector(2));
    }

    @Test
    public void testSubtract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 5.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 3.0});
        OpenMapRealVector diff = v1.subtract(v2);
        Assert.assertArrayEquals(new double[]{1.0, -2.0, 2.0}, diff.toArray(), 1e-15);

        RealVector arrVec = new ArrayRealVector(new double[]{1.0, 1.0, 1.0});
        RealVector diff2 = v1.subtract(arrVec);
        Assert.assertArrayEquals(new double[]{0.0, -1.0, 4.0}, diff2.toArray(), 1e-15);

        RealVector diff3 = v1.subtract((RealVector) v2);
        Assert.assertArrayEquals(new double[]{1.0, -2.0, 2.0}, diff3.toArray(), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.subtract(v2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSubtractGenericDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.subtract(v2);
    }

    @Test
    public void testUnitVectorAndUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        OpenMapRealVector unit = v.unitVector();
        Assert.assertArrayEquals(new double[]{0.0, 0.6, 0.8}, unit.toArray(), 1e-15);

        v.unitize();
        Assert.assertArrayEquals(new double[]{0.0, 0.6, 0.8}, v.toArray(), 1e-15);
    }

    @Test(expected = MathArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    @Test
    public void testEqualsAndHashCode() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-12);
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-12);
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-6);
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0}, 1e-12);
        OpenMapRealVector v5 = new OpenMapRealVector(new double[]{1.0, 0.0}, 1e-12);

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("Not a vector"));
        Assert.assertFalse(v1.equals(v5));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(v4));

        OpenMapRealVector v6 = new OpenMapRealVector(new double[]{1.0, 0.0, 0.0});
        OpenMapRealVector v7 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0});
        Assert.assertFalse(v6.equals(v7));
    }

    @Test
    public void testSparsityAndSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 0.0, 4.0});
        Assert.assertEquals(0.5, v.getSparsity(), 1e-15);

        Iterator<RealVector.Entry> it = v.sparseIterator();
        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry entry = it.next();
            int idx = entry.getIndex();
            if (idx == 0) {
                Assert.assertEquals(1.0, entry.getValue(), 1e-15);
                entry.setValue(10.0);
            } else if (idx == 3) {
                Assert.assertEquals(4.0, entry.getValue(), 1e-15);
            } else {
                Assert.fail("Unexpected non-zero index: " + idx);
            }
            count++;
        }
        Assert.assertEquals(2, count);
        Assert.assertEquals(10.0, v.getEntry(0), 1e-15);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemoveUnsupported() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        Iterator<RealVector.Entry> it = v.sparseIterator();
        it.next();
        it.remove();
    }
}
