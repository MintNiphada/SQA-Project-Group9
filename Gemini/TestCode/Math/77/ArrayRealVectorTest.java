package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

public class ArrayRealVectorTest {

    @Test
    public void testConstructors() {
        ArrayRealVector v0 = new ArrayRealVector();
        Assert.assertEquals(0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(5);
        Assert.assertEquals(5, v1.getDimension());
        Assert.assertEquals(0.0, v1.getEntry(0), 1e-12);

        ArrayRealVector v2 = new ArrayRealVector(4, 2.5);
        Assert.assertEquals(4, v2.getDimension());
        Assert.assertEquals(2.5, v2.getEntry(3), 1e-12);

        double[] data = new double[]{1.0, 2.0, 3.0};
        ArrayRealVector v3 = new ArrayRealVector(data);
        Assert.assertEquals(3, v3.getDimension());
        data[0] = 100.0;
        Assert.assertEquals(1.0, v3.getEntry(0), 1e-12);

        ArrayRealVector v4 = new ArrayRealVector(data, false);
        Assert.assertSame(data, v4.getDataRef());

        ArrayRealVector v5 = new ArrayRealVector(data, true);
        Assert.assertNotSame(data, v5.getDataRef());

        Double[] objArray = new Double[]{1.0, 2.0, 3.0, 4.0};
        ArrayRealVector v6 = new ArrayRealVector(objArray);
        Assert.assertEquals(4, v6.getDimension());
        Assert.assertEquals(4.0, v6.getEntry(3), 1e-12);

        ArrayRealVector v7 = new ArrayRealVector(new double[]{10, 20, 30, 40, 50}, 1, 3);
        Assert.assertEquals(3, v7.getDimension());
        Assert.assertEquals(20.0, v7.getEntry(0), 1e-12);
        Assert.assertEquals(40.0, v7.getEntry(2), 1e-12);

        ArrayRealVector v8 = new ArrayRealVector(objArray, 1, 2);
        Assert.assertEquals(2, v8.getDimension());
        Assert.assertEquals(2.0, v8.getEntry(0), 1e-12);
        Assert.assertEquals(3.0, v8.getEntry(1), 1e-12);

        ArrayRealVector v9 = new ArrayRealVector((RealVector) v3);
        Assert.assertEquals(3, v9.getDimension());
        Assert.assertEquals(1.0, v9.getEntry(0), 1e-12);

        ArrayRealVector v10 = new ArrayRealVector(v3);
        Assert.assertEquals(3, v10.getDimension());

        ArrayRealVector v11 = new ArrayRealVector(v3, false);
        Assert.assertSame(v3.getDataRef(), v11.getDataRef());

        ArrayRealVector v12 = new ArrayRealVector(v3, true);
        Assert.assertNotSame(v3.getDataRef(), v12.getDataRef());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new ArrayRealVector((double[]) null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyArray() {
        new ArrayRealVector(new double[0], true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSubArrayOutOfBoundsDouble() {
        new ArrayRealVector(new double[]{1, 2, 3}, 2, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSubArrayOutOfBoundsDoubleObject() {
        new ArrayRealVector(new Double[]{1.0, 2.0, 3.0}, 1, 3);
    }

    @Test
    public void testCombineConstructors() {
        ArrayRealVector a = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector b = new ArrayRealVector(new double[]{3.0, 4.0});
        RealVector c = new OpenMapRealVector(new double[]{5.0, 6.0});
        double[] d = new double[]{7.0, 8.0};

        ArrayRealVector res1 = new ArrayRealVector(a, b);
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, res1.getData(), 1e-12);

        ArrayRealVector res2 = new ArrayRealVector(a, c);
        Assert.assertArrayEquals(new double[]{1, 2, 5, 6}, res2.getData(), 1e-12);

        ArrayRealVector res3 = new ArrayRealVector(c, a);
        Assert.assertArrayEquals(new double[]{5, 6, 1, 2}, res3.getData(), 1e-12);

        ArrayRealVector res4 = new ArrayRealVector(a, d);
        Assert.assertArrayEquals(new double[]{1, 2, 7, 8}, res4.getData(), 1e-12);

        ArrayRealVector res5 = new ArrayRealVector(d, a);
        Assert.assertArrayEquals(new double[]{7, 8, 1, 2}, res5.getData(), 1e-12);

        ArrayRealVector res6 = new ArrayRealVector(new double[]{1, 2}, new double[]{3, 4});
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, res6.getData(), 1e-12);
    }

    @Test
    public void testCopyAndGetters() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1, 2, 3});
        AbstractRealVector copy = v.copy();
        Assert.assertTrue(copy instanceof ArrayRealVector);
        Assert.assertNotSame(v.getDataRef(), ((ArrayRealVector) copy).getDataRef());
        Assert.assertArrayEquals(v.getData(), copy.getData(), 1e-12);

        double[] dataRef = v.getDataRef();
        Assert.assertEquals(3, dataRef.length);
        double[] toArr = v.toArray();
        Assert.assertNotSame(dataRef, toArr);
        Assert.assertArrayEquals(dataRef, toArr, 1e-12);
    }

    @Test
    public void testAddAndSubtract() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1, 2, 3});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4, 5, 6});
        RealVector sparse = new OpenMapRealVector(new double[]{1, 0, 2});

        RealVector add1 = v1.add(v2);
        Assert.assertArrayEquals(new double[]{5, 7, 9}, add1.getData(), 1e-12);

        RealVector add2 = v1.add((RealVector) v2);
        Assert.assertArrayEquals(new double[]{5, 7, 9}, add2.getData(), 1e-12);

        RealVector addSparse = v1.add(sparse);
        Assert.assertArrayEquals(new double[]{2, 2, 5}, addSparse.getData(), 1e-12);

        RealVector addArr = v1.add(new double[]{2, 2, 2});
        Assert.assertArrayEquals(new double[]{3, 4, 5}, addArr.getData(), 1e-12);

        RealVector sub1 = v1.subtract(v2);
        Assert.assertArrayEquals(new double[]{-3, -3, -3}, sub1.getData(), 1e-12);

        RealVector sub2 = v1.subtract((RealVector) v2);
        Assert.assertArrayEquals(new double[]{-3, -3, -3}, sub2.getData(), 1e-12);

        RealVector subSparse = v1.subtract(sparse);
        Assert.assertArrayEquals(new double[]{0, 2, 1}, subSparse.getData(), 1e-12);

        RealVector subArr = v1.subtract(new double[]{1, 1, 1});
        Assert.assertArrayEquals(new double[]{0, 1, 2}, subArr.getData(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        new ArrayRealVector(new double[]{1, 2}).add(new double[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractDimensionMismatchSparse() {
        new ArrayRealVector(new double[]{1, 2}).subtract(new OpenMapRealVector(new double[]{1}));
    }

    @Test
    public void testMapToSelfOperations() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.mapAddToSelf(2.0);
        Assert.assertArrayEquals(new double[]{3.0, 4.0, 5.0}, v.getDataRef(), 1e-12);

        v.mapSubtractToSelf(1.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0, 4.0}, v.getDataRef(), 1e-12);

        v.mapMultiplyToSelf(2.0);
        Assert.assertArrayEquals(new double[]{4.0, 6.0, 8.0}, v.getDataRef(), 1e-12);

        v.mapDivideToSelf(2.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0, 4.0}, v.getDataRef(), 1e-12);

        v.mapPowToSelf(2.0);
        Assert.assertArrayEquals(new double[]{4.0, 9.0, 16.0}, v.getDataRef(), 1e-12);

        ArrayRealVector vMath = new ArrayRealVector(new double[]{0.0, 1.0});
        vMath.mapExpToSelf();
        Assert.assertEquals(1.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0, 1.0});
        vMath.mapExpm1ToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.0, Math.E});
        vMath.mapLogToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.0, 10.0});
        vMath.mapLog10ToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0, Math.E - 1});
        vMath.mapLog1pToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapCoshToSelf();
        Assert.assertEquals(1.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapSinhToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapTanhToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapCosToSelf();
        Assert.assertEquals(1.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapSinToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapTanToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.0});
        vMath.mapAcosToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapAsinToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{0.0});
        vMath.mapAtanToSelf();
        Assert.assertEquals(0.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{2.0, 4.0});
        vMath.mapInvToSelf();
        Assert.assertEquals(0.5, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{-2.5, 3.5});
        vMath.mapAbsToSelf();
        Assert.assertEquals(2.5, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{4.0, 9.0});
        vMath.mapSqrtToSelf();
        Assert.assertEquals(2.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{8.0, 27.0});
        vMath.mapCbrtToSelf();
        Assert.assertEquals(2.0, vMath.getEntry(0), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.2, -1.2});
        vMath.mapCeilToSelf();
        Assert.assertEquals(2.0, vMath.getEntry(0), 1e-12);
        Assert.assertEquals(-1.0, vMath.getEntry(1), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.8, -1.8});
        vMath.mapFloorToSelf();
        Assert.assertEquals(1.0, vMath.getEntry(0), 1e-12);
        Assert.assertEquals(-2.0, vMath.getEntry(1), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.2, 1.8});
        vMath.mapRintToSelf();
        Assert.assertEquals(1.0, vMath.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, vMath.getEntry(1), 1e-12);

        vMath = new ArrayRealVector(new double[]{-5.0, 0.0, 5.0});
        vMath.mapSignumToSelf();
        Assert.assertEquals(-1.0, vMath.getEntry(0), 1e-12);
        Assert.assertEquals(0.0, vMath.getEntry(1), 1e-12);
        Assert.assertEquals(1.0, vMath.getEntry(2), 1e-12);

        vMath = new ArrayRealVector(new double[]{1.0});
        vMath.mapUlpToSelf();
        Assert.assertEquals(Math.ulp(1.0), vMath.getEntry(0), 1e-12);
    }

    @Test
    public void testEbeOperations() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{2, 6, 12});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{2, 3, 4});
        RealVector sparse = new OpenMapRealVector(new double[]{2, 3, 4});

        RealVector mul1 = v1.ebeMultiply(v2);
        Assert.assertArrayEquals(new double[]{4, 18, 48}, mul1.getData(), 1e-12);

        RealVector mul2 = v1.ebeMultiply((RealVector) v2);
        Assert.assertArrayEquals(new double[]{4, 18, 48}, mul2.getData(), 1e-12);

        RealVector mulSparse = v1.ebeMultiply(sparse);
        Assert.assertArrayEquals(new double[]{4, 18, 48}, mulSparse.getData(), 1e-12);

        RealVector mulArr = v1.ebeMultiply(new double[]{2, 3, 4});
        Assert.assertArrayEquals(new double[]{4, 18, 48}, mulArr.getData(), 1e-12);

        RealVector div1 = v1.ebeDivide(v2);
        Assert.assertArrayEquals(new double[]{1, 2, 3}, div1.getData(), 1e-12);

        RealVector div2 = v1.ebeDivide((RealVector) v2);
        Assert.assertArrayEquals(new double[]{1, 2, 3}, div2.getData(), 1e-12);

        RealVector divSparse = v1.ebeDivide(sparse);
        Assert.assertArrayEquals(new double[]{1, 2, 3}, divSparse.getData(), 1e-12);

        RealVector divArr = v1.ebeDivide(new double[]{2, 3, 4});
        Assert.assertArrayEquals(new double[]{1, 2, 3}, divArr.getData(), 1e-12);
    }

    @Test
    public void testDotAndNorms() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1, -2, 3});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4, 5, 6});
        RealVector sparse = new OpenMapRealVector(new double[]{4, 5, 6});

        double dot1 = v1.dotProduct(v2);
        Assert.assertEquals(12.0, dot1, 1e-12);

        double dot2 = v1.dotProduct((RealVector) v2);
        Assert.assertEquals(12.0, dot2, 1e-12);

        double dotSparse = v1.dotProduct(sparse);
        Assert.assertEquals(12.0, dotSparse, 1e-12);

        double dotArr = v1.dotProduct(new double[]{4, 5, 6});
        Assert.assertEquals(12.0, dotArr, 1e-12);

        ArrayRealVector vNorm = new ArrayRealVector(new double[]{3, -4});
        Assert.assertEquals(5.0, vNorm.getNorm(), 1e-12);
        Assert.assertEquals(7.0, vNorm.getL1Norm(), 1e-12);
        Assert.assertEquals(4.0, vNorm.getLInfNorm(), 1e-12);
    }

    @Test
    public void testDistances() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1, 2});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4, 6});
        RealVector sparse = new OpenMapRealVector(new double[]{4, 6});

        Assert.assertEquals(5.0, v1.getDistance(v2), 1e-12);
        Assert.assertEquals(5.0, v1.getDistance((RealVector) v2), 1e-12);
        Assert.assertEquals(5.0, v1.getDistance(sparse), 1e-12);
        Assert.assertEquals(5.0, v1.getDistance(new double[]{4, 6}), 1e-12);

        Assert.assertEquals(7.0, v1.getL1Distance(v2), 1e-12);
        Assert.assertEquals(7.0, v1.getL1Distance((RealVector) v2), 1e-12);
        Assert.assertEquals(7.0, v1.getL1Distance(sparse), 1e-12);
        Assert.assertEquals(7.0, v1.getL1Distance(new double[]{4, 6}), 1e-12);

        Assert.assertEquals(4.0, v1.getLInfDistance(v2), 1e-12);
        Assert.assertEquals(4.0, v1.getLInfDistance((RealVector) v2), 1e-12);
        Assert.assertEquals(4.0, v1.getLInfDistance(sparse), 1e-12);
        Assert.assertEquals(4.0, v1.getLInfDistance(new double[]{4, 6}), 1e-12);
    }

    @Test
    public void testUnitVectorAndUnitize() {
        ArrayRealVector v = new ArrayRealVector(new double[]{3, 0, 4});
        RealVector u = v.unitVector();
        Assert.assertEquals(1.0, u.getNorm(), 1e-12);
        Assert.assertEquals(0.6, u.getEntry(0), 1e-12);
        Assert.assertEquals(0.8, u.getEntry(2), 1e-12);

        v.unitize();
        Assert.assertEquals(1.0, v.getNorm(), 1e-12);
        Assert.assertEquals(0.6, v.getEntry(0), 1e-12);
    }

    @Test(expected = ArithmeticException.class)
    public void testUnitVectorZeroNorm() {
        new ArrayRealVector(new double[]{0, 0}).unitVector();
    }

    @Test(expected = ArithmeticException.class)
    public void testUnitizeZeroNorm() {
        new ArrayRealVector(new double[]{0, 0}).unitize();
    }

    @Test
    public void testProjectionAndOuterProduct() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1, 2});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0, 4});
        RealVector sparse = new OpenMapRealVector(new double[]{0, 4});

        ArrayRealVector proj1 = v1.projection(v2);
        Assert.assertArrayEquals(new double[]{0, 2}, proj1.getData(), 1e-12);

        RealVector proj2 = v1.projection((RealVector) v2);
        Assert.assertArrayEquals(new double[]{0, 2}, proj2.getData(), 1e-12);

        RealVector proj3 = v1.projection(new double[]{0, 4});
        Assert.assertArrayEquals(new double[]{0, 2}, proj3.getData(), 1e-12);

        RealMatrix m1 = v1.outerProduct(v2);
        Assert.assertEquals(0.0, m1.getEntry(0, 0), 1e-12);
        Assert.assertEquals(4.0, m1.getEntry(0, 1), 1e-12);
        Assert.assertEquals(0.0, m1.getEntry(1, 0), 1e-12);
        Assert.assertEquals(8.0, m1.getEntry(1, 1), 1e-12);

        RealMatrix m2 = v1.outerProduct((RealVector) v2);
        Assert.assertEquals(m1, m2);

        RealMatrix m3 = v1.outerProduct(sparse);
        Assert.assertEquals(m1, m3);

        RealMatrix m4 = v1.outerProduct(new double[]{0, 4});
        Assert.assertEquals(m1, m4);
    }

    @Test
    public void testAppendMethods() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1, 2});
        ArrayRealVector vOther = new ArrayRealVector(new double[]{3, 4});
        RealVector sparse = new OpenMapRealVector(new double[]{5, 6});

        ArrayRealVector app1 = v.append(vOther);
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, app1.getData(), 1e-12);

        RealVector app2 = v.append((RealVector) vOther);
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, app2.getData(), 1e-12);

        RealVector app3 = v.append(sparse);
        Assert.assertArrayEquals(new double[]{1, 2, 5, 6}, app3.getData(), 1e-12);

        RealVector app4 = v.append(7.0);
        Assert.assertArrayEquals(new double[]{1, 2, 7}, app4.getData(), 1e-12);

        RealVector app5 = v.append(new double[]{8, 9});
        Assert.assertArrayEquals(new double[]{1, 2, 8, 9}, app5.getData(), 1e-12);
    }

    @Test
    public void testSubVectorAndSetOperations() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1, 2, 3, 4, 5});
        RealVector sub = v.getSubVector(1, 3);
        Assert.assertArrayEquals(new double[]{2, 3, 4}, sub.getData(), 1e-12);

        v.setEntry(0, 10.0);
        Assert.assertEquals(10.0, v.getEntry(0), 1e-12);

        v.setSubVector(1, new ArrayRealVector(new double[]{20, 30}));
        Assert.assertEquals(20.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(30.0, v.getEntry(2), 1e-12);

        v.setSubVector(3, new OpenMapRealVector(new double[]{40, 50}));
        Assert.assertEquals(40.0, v.getEntry(3), 1e-12);
        Assert.assertEquals(50.0, v.getEntry(4), 1e-12);

        v.setSubVector(0, new double[]{100, 200});
        Assert.assertEquals(100.0, v.getEntry(0), 1e-12);
        Assert.assertEquals(200.0, v.getEntry(1), 1e-12);

        v.set(1.0, new ArrayRealVector(new double[]{222, 333}));
        Assert.assertEquals(222.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(333.0, v.getEntry(2), 1e-12);

        v.set(9.0);
        Assert.assertArrayEquals(new double[]{9, 9, 9, 9, 9}, v.getData(), 1e-12);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubVectorInvalid() {
        new ArrayRealVector(new double[]{1, 2}).getSubVector(1, 3);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryInvalid() {
        new ArrayRealVector(new double[]{1, 2}).setEntry(5, 1.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVectorInvalidArray() {
        new ArrayRealVector(new double[]{1, 2}).setSubVector(1, new double[]{1, 2, 3});
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVectorInvalidRealVector() {
        new ArrayRealVector(new double[]{1, 2}).setSubVector(1, new OpenMapRealVector(new double[]{1, 2, 3}));
    }

    @Test
    public void testIsNaNAndIsInfinite() {
        ArrayRealVector normal = new ArrayRealVector(new double[]{1, 2});
        Assert.assertFalse(normal.isNaN());
        Assert.assertFalse(normal.isInfinite());

        ArrayRealVector nanVec = new ArrayRealVector(new double[]{1, Double.NaN});
        Assert.assertTrue(nanVec.isNaN());
        Assert.assertFalse(nanVec.isInfinite());

        ArrayRealVector infVec = new ArrayRealVector(new double[]{1, Double.POSITIVE_INFINITY});
        Assert.assertFalse(infVec.isNaN());
        Assert.assertTrue(infVec.isInfinite());

        ArrayRealVector nanInfVec = new ArrayRealVector(new double[]{Double.NaN, Double.NEGATIVE_INFINITY});
        Assert.assertTrue(nanInfVec.isNaN());
        Assert.assertFalse(nanInfVec.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1, 2, 3});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1, 2, 3});
        ArrayRealVector v3 = new ArrayRealVector(new double[]{1, 2, 4});
        ArrayRealVector v4 = new ArrayRealVector(new double[]{1, 2});

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("string"));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(v4));

        ArrayRealVector nan1 = new ArrayRealVector(new double[]{1, Double.NaN});
        ArrayRealVector nan2 = new ArrayRealVector(new double[]{Double.NaN, 2});
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertEquals(9, nan1.hashCode());
        Assert.assertEquals(nan1.hashCode(), nan2.hashCode());
        Assert.assertFalse(nan1.equals(new ArrayRealVector(new double[]{1, 2})));
    }

    @Test
    public void testToString() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        String str = v.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("1") && str.contains("2"));
    }
}
