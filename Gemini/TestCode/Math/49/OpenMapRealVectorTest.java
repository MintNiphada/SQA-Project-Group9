package org.apache.commons.math.linear;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MathArithmeticException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

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

        OpenMapRealVector v3 = new OpenMapRealVector(5, 2);
        Assert.assertEquals(5, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(5, 2, 1e-6);
        Assert.assertEquals(5, v4.getDimension());

        double[] dArr = new double[]{0.0, 1.0, 0.0, 3.0};
        OpenMapRealVector v5 = new OpenMapRealVector(dArr);
        Assert.assertEquals(4, v5.getDimension());
        Assert.assertEquals(1.0, v5.getEntry(1), 1e-12);
        Assert.assertEquals(0.0, v5.getEntry(0), 1e-12);

        OpenMapRealVector v6 = new OpenMapRealVector(dArr, 1e-5);
        Assert.assertEquals(4, v6.getDimension());

        Double[] objArr = new Double[]{0.0, 2.0, 0.0};
        OpenMapRealVector v7 = new OpenMapRealVector(objArr);
        Assert.assertEquals(3, v7.getDimension());
        Assert.assertEquals(2.0, v7.getEntry(1), 1e-12);

        OpenMapRealVector v8 = new OpenMapRealVector(objArr, 1e-5);
        Assert.assertEquals(3, v8.getDimension());

        OpenMapRealVector v9 = new OpenMapRealVector(v5);
        Assert.assertEquals(4, v9.getDimension());
        Assert.assertEquals(3.0, v9.getEntry(3), 1e-12);

        RealVector rv = new ArrayRealVector(new double[]{0.0, 4.0, 5.0});
        OpenMapRealVector v10 = new OpenMapRealVector(rv);
        Assert.assertEquals(3, v10.getDimension());
        Assert.assertEquals(4.0, v10.getEntry(1), 1e-12);
    }

    @Test
    public void testAdd() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 4.0});
        RealVector v3 = new ArrayRealVector(new double[]{1.0, 1.0, 1.0});

        RealVector sum1 = v1.add(v2);
        Assert.assertEquals(1.0, sum1.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, sum1.getEntry(1), 1e-12);
        Assert.assertEquals(7.0, sum1.getEntry(2), 1e-12);

        OpenMapRealVector vSmall = new OpenMapRealVector(new double[]{0.0, 0.0, 2.0});
        RealVector sum2 = vSmall.add(v1);
        Assert.assertEquals(1.0, sum2.getEntry(0), 1e-12);
        Assert.assertEquals(0.0, sum2.getEntry(1), 1e-12);
        Assert.assertEquals(5.0, sum2.getEntry(2), 1e-12);

        RealVector sum3 = v1.add(v3);
        Assert.assertEquals(2.0, sum3.getEntry(0), 1e-12);
        Assert.assertEquals(1.0, sum3.getEntry(1), 1e-12);
        Assert.assertEquals(4.0, sum3.getEntry(2), 1e-12);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0});
        v1.add(v2);
    }

    @Test
    public void testAppend() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0});

        OpenMapRealVector res1 = v1.append(v2);
        Assert.assertEquals(4, res1.getDimension());
        Assert.assertEquals(1.0, res1.getEntry(0), 1e-12);
        Assert.assertEquals(0.0, res1.getEntry(1), 1e-12);
        Assert.assertEquals(0.0, res1.getEntry(2), 1e-12);
        Assert.assertEquals(2.0, res1.getEntry(3), 1e-12);

        OpenMapRealVector res2 = v1.append(3.0);
        Assert.assertEquals(3, res2.getDimension());
        Assert.assertEquals(3.0, res2.getEntry(2), 1e-12);

        OpenMapRealVector res3 = v1.append(new double[]{4.0, 5.0});
        Assert.assertEquals(4, res3.getDimension());
        Assert.assertEquals(4.0, res3.getEntry(2), 1e-12);
        Assert.assertEquals(5.0, res3.getEntry(3), 1e-12);

        RealVector rv = new ArrayRealVector(new double[]{6.0});
        OpenMapRealVector res4 = v1.append(rv);
        Assert.assertEquals(3, res4.getDimension());
        Assert.assertEquals(6.0, res4.getEntry(2), 1e-12);

        OpenMapRealVector res5 = v1.append((RealVector) v2);
        Assert.assertEquals(4, res5.getDimension());
        Assert.assertEquals(2.0, res5.getEntry(3), 1e-12);
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0.0, 3.0, 0.0});

        Assert.assertEquals(6.0, v1.dotProduct(v2), 1e-12);
        Assert.assertEquals(6.0, v3.dotProduct(v1), 1e-12);
        Assert.assertEquals(6.0, v1.dotProduct((RealVector) v2), 1e-12);

        RealVector rv = new ArrayRealVector(new double[]{2.0, 1.0, 0.0});
        Assert.assertEquals(4.0, v1.dotProduct(rv), 1e-12);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0});
        v1.dotProduct(v2);
    }

    @Test
    public void testEbeOperations() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2.0, 0.0, 6.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 4.0, 3.0});
        double[] arr = new double[]{2.0, 4.0, 3.0};

        OpenMapRealVector div1 = v1.ebeDivide(v2);
        Assert.assertEquals(1.0, div1.getEntry(0), 1e-12);
        Assert.assertEquals(0.0, div1.getEntry(1), 1e-12);
        Assert.assertEquals(2.0, div1.getEntry(2), 1e-12);

        OpenMapRealVector div2 = v1.ebeDivide(arr);
        Assert.assertEquals(1.0, div2.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, div2.getEntry(2), 1e-12);

        OpenMapRealVector mul1 = v1.ebeMultiply(v2);
        Assert.assertEquals(4.0, mul1.getEntry(0), 1e-12);
        Assert.assertEquals(0.0, mul1.getEntry(1), 1e-12);
        Assert.assertEquals(18.0, mul1.getEntry(2), 1e-12);

        OpenMapRealVector mul2 = v1.ebeMultiply(arr);
        Assert.assertEquals(4.0, mul2.getEntry(0), 1e-12);
        Assert.assertEquals(18.0, mul2.getEntry(2), 1e-12);
    }

    @Test
    public void testSubVectorAndData() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 1.0, 2.0, 0.0, 3.0});
        OpenMapRealVector sub = v.getSubVector(1, 3);
        Assert.assertEquals(3, sub.getDimension());
        Assert.assertEquals(1.0, sub.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, sub.getEntry(1), 1e-12);
        Assert.assertEquals(0.0, sub.getEntry(2), 1e-12);

        double[] data = v.getData();
        Assert.assertArrayEquals(new double[]{0.0, 1.0, 2.0, 0.0, 3.0}, data, 1e-12);
        Assert.assertArrayEquals(data, v.toArray(), 1e-12);

        v.setSubVector(1, new double[]{8.0, 9.0});
        Assert.assertEquals(8.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(9.0, v.getEntry(2), 1e-12);

        v.setSubVector(0, new ArrayRealVector(new double[]{7.0}));
        Assert.assertEquals(7.0, v.getEntry(0), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        v.getSubVector(1, 2);
    }

    @Test
    public void testDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 3.0, 0.0});
        RealVector rv = new ArrayRealVector(new double[]{0.0, 2.0, 3.0, 0.0});
        double[] arr = new double[]{0.0, 2.0, 3.0, 0.0};

        Assert.assertEquals(Math.sqrt(5.0), v1.getDistance(v2), 1e-12);
        Assert.assertEquals(Math.sqrt(5.0), v1.getDistance(rv), 1e-12);
        Assert.assertEquals(Math.sqrt(5.0), v1.getDistance(arr), 1e-12);

        Assert.assertEquals(3.0, v1.getL1Distance(v2), 1e-12);
        Assert.assertEquals(3.0, v1.getL1Distance(rv), 1e-12);
        Assert.assertEquals(3.0, v1.getL1Distance(arr), 1e-12);

        Assert.assertEquals(2.0, v1.getLInfDistance(v2), 1e-12);
        Assert.assertEquals(2.0, v1.getLInfDistance(rv), 1e-12);
        Assert.assertEquals(2.0, v1.getLInfDistance(arr), 1e-12);

        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0.0, 5.0, 0.0, 0.0});
        Assert.assertEquals(5.0, v1.getLInfDistance(v3), 1e-12);
    }

    @Test
    public void testIsNaNAndInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 1.0, 2.0});
        Assert.assertFalse(v.isNaN());
        Assert.assertFalse(v.isInfinite());

        v.setEntry(1, Double.NaN);
        Assert.assertTrue(v.isNaN());
        Assert.assertFalse(v.isInfinite());

        v.setEntry(1, Double.POSITIVE_INFINITY);
        Assert.assertFalse(v.isNaN());
        Assert.assertTrue(v.isInfinite());
    }

    @Test
    public void testMapOperationsAndOuterProduct() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector vAdd = v.mapAdd(2.0);
        Assert.assertEquals(3.0, vAdd.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, vAdd.getEntry(1), 1e-12);
        Assert.assertEquals(4.0, vAdd.getEntry(2), 1e-12);

        v.set(5.0);
        Assert.assertEquals(5.0, v.getEntry(0), 1e-12);
        Assert.assertEquals(5.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(5.0, v.getEntry(2), 1e-12);

        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0});
        RealMatrix matrix = v2.outerProduct(new double[]{3.0, 4.0});
        Assert.assertEquals(3.0, matrix.getEntry(0, 0), 1e-12);
        Assert.assertEquals(4.0, matrix.getEntry(0, 1), 1e-12);
        Assert.assertEquals(6.0, matrix.getEntry(1, 0), 1e-12);
        Assert.assertEquals(8.0, matrix.getEntry(1, 1), 1e-12);
    }

    @Test
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        RealVector v2 = new ArrayRealVector(new double[]{0.0, 1.0, 0.0});
        RealVector proj = v1.projection(v2);
        Assert.assertEquals(0.0, proj.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, proj.getEntry(1), 1e-12);
        Assert.assertEquals(0.0, proj.getEntry(2), 1e-12);

        RealVector projArr = v1.projection(new double[]{0.0, 1.0, 0.0});
        Assert.assertEquals(2.0, projArr.getEntry(1), 1e-12);
    }

    @Test
    public void testSubtract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 1.0});

        RealVector diff1 = v1.subtract(v2);
        Assert.assertEquals(1.0, diff1.getEntry(0), 1e-12);
        Assert.assertEquals(-2.0, diff1.getEntry(1), 1e-12);
        Assert.assertEquals(2.0, diff1.getEntry(2), 1e-12);

        RealVector diff2 = v1.subtract((RealVector) v2);
        Assert.assertEquals(1.0, diff2.getEntry(0), 1e-12);
        Assert.assertEquals(-2.0, diff2.getEntry(1), 1e-12);

        RealVector diff3 = v1.subtract(new double[]{0.0, 2.0, 1.0});
        Assert.assertEquals(1.0, diff3.getEntry(0), 1e-12);
        Assert.assertEquals(-2.0, diff3.getEntry(1), 1e-12);
    }

    @Test
    public void testUnitVectorAndUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        OpenMapRealVector unit = v.unitVector();
        Assert.assertEquals(0.0, unit.getEntry(0), 1e-12);
        Assert.assertEquals(0.6, unit.getEntry(1), 1e-12);
        Assert.assertEquals(0.8, unit.getEntry(2), 1e-12);

        v.unitize();
        Assert.assertEquals(0.6, v.getEntry(1), 1e-12);
    }

    @Test(expected = MathArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 0.0});
        v.unitize();
    }

    @Test
    public void testEqualsAndHashCode() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-6);
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-6);
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0}, 1e-5);
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{1.0, 0.0}, 1e-6);
        OpenMapRealVector v5 = new OpenMapRealVector(new double[]{1.0, 3.0, 2.0}, 1e-6);
        OpenMapRealVector v6 = new OpenMapRealVector(new double[]{1.0, 0.0, 4.0}, 1e-6);

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("String"));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(v4));
        Assert.assertFalse(v1.equals(v5));
        Assert.assertFalse(v1.equals(v6));
        Assert.assertFalse(v5.equals(v1));
    }

    @Test
    public void testSparseIteratorAndSparsity() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 2.5, 0.0, 4.5});
        Assert.assertEquals(0.5, v.getSparsity(), 1e-12);

        Iterator<RealVector.Entry> iter = v.sparseIterator();
        int count = 0;
        while (iter.hasNext()) {
            RealVector.Entry entry = iter.next();
            Assert.assertTrue(entry.getIndex() == 1 || entry.getIndex() == 3);
            if (entry.getIndex() == 1) {
                Assert.assertEquals(2.5, entry.getValue(), 1e-12);
                entry.setValue(7.5);
            }
            count++;
        }
        Assert.assertEquals(2, count);
        Assert.assertEquals(7.5, v.getEntry(1), 1e-12);

        v.setEntry(1, 0.0);
        Assert.assertEquals(0.0, v.getEntry(1), 1e-12);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemove() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0});
        Iterator<RealVector.Entry> iter = v.sparseIterator();
        iter.remove();
    }
}
