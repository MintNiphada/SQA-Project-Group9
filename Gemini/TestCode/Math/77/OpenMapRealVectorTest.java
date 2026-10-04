package org.apache.commons.math.linear;

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

        OpenMapRealVector v2 = new OpenMapRealVector(5, 1e-6);
        Assert.assertEquals(5, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(5, 10);
        Assert.assertEquals(5, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(5, 10, 1e-6);
        Assert.assertEquals(5, v4.getDimension());

        double[] data = new double[]{0.0, 1.0, 0.0, 3.0};
        OpenMapRealVector v5 = new OpenMapRealVector(data);
        Assert.assertEquals(4, v5.getDimension());
        Assert.assertEquals(1.0, v5.getEntry(1), 1e-12);
        Assert.assertEquals(0.0, v5.getEntry(0), 1e-12);

        OpenMapRealVector v6 = new OpenMapRealVector(data, 1e-5);
        Assert.assertEquals(4, v6.getDimension());

        Double[] objData = new Double[]{0.0, 2.0, 0.0};
        OpenMapRealVector v7 = new OpenMapRealVector(objData);
        Assert.assertEquals(3, v7.getDimension());
        Assert.assertEquals(2.0, v7.getEntry(1), 1e-12);

        OpenMapRealVector v8 = new OpenMapRealVector(objData, 1e-5);
        Assert.assertEquals(3, v8.getDimension());

        OpenMapRealVector v9 = new OpenMapRealVector(v5);
        Assert.assertEquals(4, v9.getDimension());
        Assert.assertEquals(3.0, v9.getEntry(3), 1e-12);

        RealVector rv = new ArrayRealVector(new double[]{0.0, 4.0, 0.0});
        OpenMapRealVector v10 = new OpenMapRealVector(rv);
        Assert.assertEquals(3, v10.getDimension());
        Assert.assertEquals(4.0, v10.getEntry(1), 1e-12);

        OpenMapRealVector v11 = new OpenMapRealVector(v5, 2);
        Assert.assertEquals(6, v11.getDimension());
        Assert.assertEquals(1.0, v11.getEntry(1), 1e-12);
    }

    @Test
    public void testIsDefaultValue() {
        OpenMapRealVector v = new OpenMapRealVector(3, 1e-3);
        Assert.assertTrue(v.isDefaultValue(1e-4));
        Assert.assertTrue(v.isDefaultValue(-1e-4));
        Assert.assertFalse(v.isDefaultValue(1e-2));
        Assert.assertFalse(v.isDefaultValue(-1e-2));
    }

    @Test
    public void testAdd() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0, 0.0});
        
        RealVector res1 = v1.add(v2);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 6.0, 0.0}, res1.getData(), 1e-12);

        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0.0, 0.0, 0.0, 5.0});
        RealVector res2 = v3.add(v1);
        Assert.assertArrayEquals(new double[]{1.0, 0.0, 2.0, 5.0}, res2.getData(), 1e-12);

        RealVector res3 = v1.add((RealVector) v2);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 6.0, 0.0}, res3.getData(), 1e-12);

        RealVector arv = new ArrayRealVector(new double[]{1.0, 1.0, 1.0, 1.0});
        RealVector res4 = v1.add(arv);
        Assert.assertArrayEquals(new double[]{2.0, 1.0, 3.0, 1.0}, res4.getData(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddGenericDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        RealVector v2 = new ArrayRealVector(4);
        v1.add(v2);
    }

    @Test
    public void testAppend() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0});

        OpenMapRealVector app1 = v1.append(v2);
        Assert.assertEquals(4, app1.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, app1.getData(), 1e-12);

        OpenMapRealVector app2 = v1.append((RealVector) v2);
        Assert.assertEquals(4, app2.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, app2.getData(), 1e-12);

        RealVector arv = new ArrayRealVector(new double[]{5.0});
        OpenMapRealVector app3 = v1.append(arv);
        Assert.assertEquals(3, app3.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 5.0}, app3.getData(), 1e-12);

        OpenMapRealVector app4 = v1.append(6.0);
        Assert.assertEquals(3, app4.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 6.0}, app4.getData(), 1e-12);

        OpenMapRealVector app5 = v1.append(new double[]{7.0, 8.0});
        Assert.assertEquals(4, app5.getDimension());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 7.0, 8.0}, app5.getData(), 1e-12);
    }

    @Test
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector copy = v.copy();
        Assert.assertEquals(v, copy);
        copy.setEntry(0, 5.0);
        Assert.assertFalse(v.equals(copy));
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 5.0, 2.0});
        Assert.assertEquals(14.0, v1.dotProduct(v2), 1e-12);
        Assert.assertEquals(14.0, v2.dotProduct(v1), 1e-12);

        RealVector v3 = new ArrayRealVector(new double[]{1.0, 1.0, 1.0, 1.0});
        Assert.assertEquals(7.0, v1.dotProduct(v3), 1e-12);
        Assert.assertEquals(14.0, v1.dotProduct((RealVector) v2), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDotProductMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.dotProduct(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDotProductGenericMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        RealVector v2 = new ArrayRealVector(4);
        v1.dotProduct(v2);
    }

    @Test
    public void testEbeOperations() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{4.0, 0.0, 6.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 1.0, 3.0});
        double[] arr = new double[]{2.0, 1.0, 3.0};

        OpenMapRealVector div1 = v1.ebeDivide(v2);
        Assert.assertArrayEquals(new double[]{2.0, 0.0, 2.0}, div1.getData(), 1e-12);

        OpenMapRealVector div2 = v1.ebeDivide(arr);
        Assert.assertArrayEquals(new double[]{2.0, 0.0, 2.0}, div2.getData(), 1e-12);

        OpenMapRealVector mul1 = v1.ebeMultiply(v2);
        Assert.assertArrayEquals(new double[]{8.0, 0.0, 18.0}, mul1.getData(), 1e-12);

        OpenMapRealVector mul2 = v1.ebeMultiply(arr);
        Assert.assertArrayEquals(new double[]{8.0, 0.0, 18.0}, mul2.getData(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeDivideMismatch1() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.ebeDivide(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeDivideMismatch2() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.ebeDivide(new double[4]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeMultiplyMismatch1() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.ebeMultiply(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeMultiplyMismatch2() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.ebeMultiply(new double[4]);
    }

    @Test
    public void testSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        OpenMapRealVector sub = v.getSubVector(1, 3);
        Assert.assertEquals(3, sub.getDimension());
        Assert.assertArrayEquals(new double[]{2.0, 3.0, 4.0}, sub.getData(), 1e-12);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(-1, 2);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubVectorInvalidEndIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(4, 2);
    }

    @Test
    public void testGetDistance() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 3.0, 0.0});

        double dist = v1.getDistance(v2);
        Assert.assertEquals(Math.sqrt(1.0 + 4.0), dist, 1e-12);

        double dist2 = v1.getDistance((RealVector) v2);
        Assert.assertEquals(dist, dist2, 1e-12);

        double dist3 = v1.getDistance(new ArrayRealVector(new double[]{0.0, 2.0, 3.0, 0.0}));
        Assert.assertEquals(dist, dist3, 1e-12);

        double dist4 = v1.getDistance(new double[]{0.0, 2.0, 3.0, 0.0});
        Assert.assertEquals(dist, dist4, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDistanceMismatch() {
        new OpenMapRealVector(3).getDistance(new OpenMapRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDistanceArrayMismatch() {
        new OpenMapRealVector(3).getDistance(new double[4]);
    }

    @Test
    public void testGetL1Distance() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 4.0, 0.0});

        double dist = v1.getL1Distance(v2);
        Assert.assertEquals(1.0 + 2.0 + 1.0, dist, 1e-12);

        double dist2 = v1.getL1Distance((RealVector) v2);
        Assert.assertEquals(dist, dist2, 1e-12);

        double dist3 = v1.getL1Distance(new ArrayRealVector(new double[]{0.0, 2.0, 4.0, 0.0}));
        Assert.assertEquals(dist, dist3, 1e-12);

        double dist4 = v1.getL1Distance(new double[]{0.0, 2.0, 4.0, 0.0});
        Assert.assertEquals(dist, dist4, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetL1DistanceMismatch() {
        new OpenMapRealVector(3).getL1Distance(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetL1DistanceArrayMismatch() {
        new OpenMapRealVector(3).getL1Distance(new double[4]);
    }

    @Test
    public void testGetLInfNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -3.0, 2.0});
        Assert.assertEquals(0.0, v.getLInfNorm(), 1e-12);
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 2.0});
        Assert.assertEquals(6.0, v2.getLInfNorm(), 1e-12);
    }

    @Test
    public void testGetLInfDistance() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 5.0, 2.0, 0.0});

        double dist1 = v1.getLInfDistance((RealVector) v2);
        Assert.assertEquals(5.0, dist1, 1e-12);

        double dist2 = v1.getLInfDistance(new ArrayRealVector(new double[]{0.0, 5.0, 2.0, 0.0}));
        Assert.assertEquals(5.0, dist2, 1e-12);

        double dist3 = v1.getLInfDistance(new double[]{0.0, 5.0, 2.0, 0.0});
        Assert.assertEquals(5.0, dist3, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLInfDistanceMismatch() {
        new OpenMapRealVector(3).getLInfDistance(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLInfDistanceArrayMismatch() {
        new OpenMapRealVector(3).getLInfDistance(new double[4]);
    }

    @Test
    public void testIsInfiniteAndIsNaN() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        Assert.assertFalse(v.isInfinite());
        Assert.assertFalse(v.isNaN());

        OpenMapRealVector vInf = new OpenMapRealVector(new double[]{1.0, Double.POSITIVE_INFINITY, 3.0});
        Assert.assertTrue(vInf.isInfinite());
        Assert.assertFalse(vInf.isNaN());

        OpenMapRealVector vNaN = new OpenMapRealVector(new double[]{1.0, Double.NaN, 3.0});
        Assert.assertFalse(vNaN.isInfinite());
        Assert.assertTrue(vNaN.isNaN());

        OpenMapRealVector vBoth = new OpenMapRealVector(new double[]{Double.POSITIVE_INFINITY, Double.NaN});
        Assert.assertFalse(vBoth.isInfinite());
        Assert.assertTrue(vBoth.isNaN());
    }

    @Test
    public void testMapAddAndSet() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector mapped = v.mapAdd(2.0);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 4.0}, mapped.getData(), 1e-12);

        v.set(5.0);
        Assert.assertArrayEquals(new double[]{5.0, 5.0, 5.0}, v.getData(), 1e-12);
    }

    @Test
    public void testOuterProduct() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        RealMatrix matrix = v.outerProduct(new double[]{2.0, 3.0, 4.0});
        Assert.assertEquals(3, matrix.getRowDimension());
        Assert.assertEquals(3, matrix.getColumnDimension());
        Assert.assertEquals(2.0, matrix.getEntry(0, 0), 1e-12);
        Assert.assertEquals(3.0, matrix.getEntry(0, 1), 1e-12);
        Assert.assertEquals(4.0, matrix.getEntry(0, 2), 1e-12);
        Assert.assertEquals(0.0, matrix.getEntry(1, 0), 1e-12);
        Assert.assertEquals(4.0, matrix.getEntry(2, 0), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOuterProductMismatch() {
        new OpenMapRealVector(3).outerProduct(new double[4]);
    }

    @Test
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 1.0, 0.0});
        RealVector proj = v1.projection(v2);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 0.0}, proj.getData(), 1e-12);

        RealVector projArr = v1.projection(new double[]{0.0, 1.0, 0.0});
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 0.0}, projArr.getData(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProjectionMismatch() {
        new OpenMapRealVector(3).projection(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProjectionArrayMismatch() {
        new OpenMapRealVector(3).projection(new double[4]);
    }

    @Test
    public void testSetEntryAndSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(2, 4.0);
        Assert.assertEquals(4.0, v.getEntry(2), 1e-12);
        v.setEntry(2, 0.0);
        Assert.assertEquals(0.0, v.getEntry(2), 1e-12);

        v.setSubVector(1, new double[]{1.0, 2.0});
        Assert.assertEquals(1.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(2.0, v.getEntry(2), 1e-12);

        v.setSubVector(3, new ArrayRealVector(new double[]{3.0, 4.0}));
        Assert.assertEquals(3.0, v.getEntry(3), 1e-12);
        Assert.assertEquals(4.0, v.getEntry(4), 1e-12);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryInvalidIndexLow() {
        new OpenMapRealVector(3).setEntry(-1, 1.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryInvalidIndexHigh() {
        new OpenMapRealVector(3).setEntry(3, 1.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryInvalidIndex() {
        new OpenMapRealVector(3).getEntry(3);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVectorInvalidIndexLow() {
        new OpenMapRealVector(5).setSubVector(-1, new double[]{1.0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVectorInvalidIndexHigh() {
        new OpenMapRealVector(5).setSubVector(4, new double[]{1.0, 2.0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVectorRealVectorInvalidIndex() {
        new OpenMapRealVector(5).setSubVector(4, new ArrayRealVector(new double[]{1.0, 2.0}));
    }

    @Test
    public void testSubtract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 1.0, 0.0});

        OpenMapRealVector sub1 = v1.subtract(v2);
        Assert.assertArrayEquals(new double[]{3.0, -1.0, 2.0}, sub1.getData(), 1e-12);

        OpenMapRealVector sub2 = v1.subtract((RealVector) v2);
        Assert.assertArrayEquals(new double[]{3.0, -1.0, 2.0}, sub2.getData(), 1e-12);

        OpenMapRealVector sub3 = v1.subtract(new ArrayRealVector(new double[]{2.0, 1.0, 0.0}));
        Assert.assertArrayEquals(new double[]{3.0, -1.0, 2.0}, sub3.getData(), 1e-12);

        OpenMapRealVector sub4 = v1.subtract(new double[]{2.0, 1.0, 0.0});
        Assert.assertArrayEquals(new double[]{3.0, -1.0, 2.0}, sub4.getData(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractMismatch1() {
        new OpenMapRealVector(3).subtract(new OpenMapRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractMismatch2() {
        new OpenMapRealVector(3).subtract(new ArrayRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractMismatch3() {
        new OpenMapRealVector(3).subtract(new double[4]);
    }

    @Test
    public void testUnitVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        OpenMapRealVector unit = v.unitVector();
        Assert.assertEquals(1.0, unit.getNorm(), 1e-12);
        Assert.assertEquals(0.6, unit.getEntry(1), 1e-12);
        Assert.assertEquals(0.8, unit.getEntry(2), 1e-12);
    }

    @Test(expected = ArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    @Test
    public void testToArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, v.toArray(), 1e-12);
    }

    @Test
    public void testHashCodeAndEquals() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-10);
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-10);
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0}, 1e-10);
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-5);
        OpenMapRealVector v5 = new OpenMapRealVector(new double[]{1.0, 0.0});

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("String"));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(v4));
        Assert.assertFalse(v1.equals(v5));

        OpenMapRealVector v6 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0}, 1e-10);
        Assert.assertFalse(v1.equals(v6));
        Assert.assertFalse(v6.equals(v1));
    }

    @Test
    public void testGetSparcity() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.setEntry(0, 1.0);
        Assert.assertEquals(0.25, v.getSparcity(), 1e-12);
    }

    @Test
    public void testSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(1, 10.0);
        v.setEntry(3, 30.0);

        Iterator<RealVector.Entry> it = v.sparseIterator();
        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry entry = it.next();
            if (entry.getIndex() == 1) {
                Assert.assertEquals(10.0, entry.getValue(), 1e-12);
                entry.setValue(15.0);
            } else if (entry.getIndex() == 3) {
                Assert.assertEquals(30.0, entry.getValue(), 1e-12);
            }
            count++;
        }
        Assert.assertEquals(2, count);
        Assert.assertEquals(15.0, v.getEntry(1), 1e-12);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemove() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        Iterator<RealVector.Entry> it = v.sparseIterator();
        it.remove();
    }
}
