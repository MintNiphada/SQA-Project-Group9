package org.apache.commons.math.geometry;

import org.apache.commons.math.exception.MathArithmeticException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class Vector3DTest {

    private static final double EPSILON = 1e-12;

    @Test
    public void testConstructors() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Assert.assertEquals(1.0, v1.getX(), EPSILON);
        Assert.assertEquals(2.0, v1.getY(), EPSILON);
        Assert.assertEquals(3.0, v1.getZ(), EPSILON);

        Vector3D v2 = new Vector3D(FastMath.PI / 2, 0);
        Assert.assertEquals(0.0, v2.getX(), EPSILON);
        Assert.assertEquals(1.0, v2.getY(), EPSILON);
        Assert.assertEquals(0.0, v2.getZ(), EPSILON);

        Vector3D v3 = new Vector3D(2.0, v1);
        Assert.assertEquals(2.0, v3.getX(), EPSILON);
        Assert.assertEquals(4.0, v3.getY(), EPSILON);
        Assert.assertEquals(6.0, v3.getZ(), EPSILON);

        Vector3D v4 = new Vector3D(2.0, v1, -1.0, v2);
        Assert.assertEquals(2.0, v4.getX(), EPSILON);
        Assert.assertEquals(3.0, v4.getY(), EPSILON);
        Assert.assertEquals(6.0, v4.getZ(), EPSILON);

        Vector3D v5 = new Vector3D(1.0, v1, 2.0, v2, 3.0, v3);
        Assert.assertEquals(7.0, v5.getX(), EPSILON);
        Assert.assertEquals(16.0, v5.getY(), EPSILON);
        Assert.assertEquals(21.0, v5.getZ(), EPSILON);

        Vector3D v6 = new Vector3D(1.0, v1, 2.0, v2, 3.0, v3, 4.0, v4);
        Assert.assertEquals(15.0, v6.getX(), EPSILON);
        Assert.assertEquals(28.0, v6.getY(), EPSILON);
        Assert.assertEquals(45.0, v6.getZ(), EPSILON);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNorm(), EPSILON);
        Assert.assertEquals(1.0, Vector3D.PLUS_I.getX(), EPSILON);
        Assert.assertEquals(-1.0, Vector3D.MINUS_I.getX(), EPSILON);
        Assert.assertEquals(1.0, Vector3D.PLUS_J.getY(), EPSILON);
        Assert.assertEquals(-1.0, Vector3D.MINUS_J.getY(), EPSILON);
        Assert.assertEquals(1.0, Vector3D.PLUS_K.getZ(), EPSILON);
        Assert.assertEquals(-1.0, Vector3D.MINUS_K.getZ(), EPSILON);
        Assert.assertTrue(Vector3D.NaN.isNaN());
        Assert.assertTrue(Vector3D.POSITIVE_INFINITY.isInfinite());
        Assert.assertTrue(Vector3D.NEGATIVE_INFINITY.isInfinite());
    }

    @Test
    public void testNorms() {
        Vector3D v = new Vector3D(1.0, -2.0, 2.0);
        Assert.assertEquals(5.0, v.getNorm1(), EPSILON);
        Assert.assertEquals(3.0, v.getNorm(), EPSILON);
        Assert.assertEquals(9.0, v.getNormSq(), EPSILON);
        Assert.assertEquals(2.0, v.getNormInf(), EPSILON);
    }

    @Test
    public void testAngles() {
        Vector3D v = new Vector3D(1.0, 1.0, FastMath.sqrt(2.0));
        Assert.assertEquals(FastMath.PI / 4, v.getAlpha(), EPSILON);
        Assert.assertEquals(FastMath.PI / 4, v.getDelta(), EPSILON);
    }

    @Test
    public void testAddAndSubtract() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        Vector3D sum = v1.add(v2);
        Assert.assertEquals(5.0, sum.getX(), EPSILON);
        Assert.assertEquals(7.0, sum.getY(), EPSILON);
        Assert.assertEquals(9.0, sum.getZ(), EPSILON);

        Vector3D scaledSum = v1.add(2.0, v2);
        Assert.assertEquals(9.0, scaledSum.getX(), EPSILON);
        Assert.assertEquals(12.0, scaledSum.getY(), EPSILON);
        Assert.assertEquals(15.0, scaledSum.getZ(), EPSILON);

        Vector3D diff = v1.subtract(v2);
        Assert.assertEquals(-3.0, diff.getX(), EPSILON);
        Assert.assertEquals(-3.0, diff.getY(), EPSILON);
        Assert.assertEquals(-3.0, diff.getZ(), EPSILON);

        Vector3D scaledDiff = v1.subtract(2.0, v2);
        Assert.assertEquals(-7.0, scaledDiff.getX(), EPSILON);
        Assert.assertEquals(-8.0, scaledDiff.getY(), EPSILON);
        Assert.assertEquals(-9.0, scaledDiff.getZ(), EPSILON);
    }

    @Test
    public void testNormalize() {
        Vector3D v = new Vector3D(2.0, -2.0, 1.0).normalize();
        Assert.assertEquals(1.0, v.getNorm(), EPSILON);
        Assert.assertEquals(2.0 / 3.0, v.getX(), EPSILON);
        Assert.assertEquals(-2.0 / 3.0, v.getY(), EPSILON);
        Assert.assertEquals(1.0 / 3.0, v.getZ(), EPSILON);
    }

    @Test(expected = MathArithmeticException.class)
    public void testNormalizeZero() {
        Vector3D.ZERO.normalize();
    }

    @Test
    public void testOrthogonal() {
        Vector3D v1 = new Vector3D(0.1, 2.0, 3.0);
        Vector3D o1 = v1.orthogonal();
        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, o1), EPSILON);
        Assert.assertEquals(1.0, o1.getNorm(), EPSILON);

        Vector3D v2 = new Vector3D(3.0, 0.1, 2.0);
        Vector3D o2 = v2.orthogonal();
        Assert.assertEquals(0.0, Vector3D.dotProduct(v2, o2), EPSILON);
        Assert.assertEquals(1.0, o2.getNorm(), EPSILON);

        Vector3D v3 = new Vector3D(2.0, 3.0, 0.1);
        Vector3D o3 = v3.orthogonal();
        Assert.assertEquals(0.0, Vector3D.dotProduct(v3, o3), EPSILON);
        Assert.assertEquals(1.0, o3.getNorm(), EPSILON);
    }

    @Test(expected = MathArithmeticException.class)
    public void testOrthogonalZero() {
        Vector3D.ZERO.orthogonal();
    }

    @Test
    public void testAngle() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
        Assert.assertEquals(FastMath.PI / 2, Vector3D.angle(v1, v2), EPSILON);

        Vector3D v3 = new Vector3D(1.0, 0.00001, 0.0);
        Assert.assertTrue(Vector3D.angle(v1, v3) > 0.0);

        Vector3D v4 = new Vector3D(-1.0, -0.00001, 0.0);
        Assert.assertTrue(Vector3D.angle(v1, v4) < FastMath.PI);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAngleZero() {
        Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    @Test
    public void testNegateAndScalarMultiply() {
        Vector3D v = new Vector3D(1.0, -2.0, 3.0);
        Vector3D neg = v.negate();
        Assert.assertEquals(-1.0, neg.getX(), EPSILON);
        Assert.assertEquals(2.0, neg.getY(), EPSILON);
        Assert.assertEquals(-3.0, neg.getZ(), EPSILON);

        Vector3D scaled = v.scalarMultiply(2.5);
        Assert.assertEquals(2.5, scaled.getX(), EPSILON);
        Assert.assertEquals(-5.0, scaled.getY(), EPSILON);
        Assert.assertEquals(7.5, scaled.getZ(), EPSILON);
    }

    @Test
    public void testIsNaN() {
        Assert.assertFalse(new Vector3D(1.0, 2.0, 3.0).isNaN());
        Assert.assertTrue(new Vector3D(Double.NaN, 2.0, 3.0).isNaN());
        Assert.assertTrue(new Vector3D(1.0, Double.NaN, 3.0).isNaN());
        Assert.assertTrue(new Vector3D(1.0, 2.0, Double.NaN).isNaN());
    }

    @Test
    public void testIsInfinite() {
        Assert.assertFalse(new Vector3D(1.0, 2.0, 3.0).isInfinite());
        Assert.assertFalse(new Vector3D(Double.NaN, Double.POSITIVE_INFINITY, 3.0).isInfinite());
        Assert.assertTrue(new Vector3D(Double.POSITIVE_INFINITY, 2.0, 3.0).isInfinite());
        Assert.assertTrue(new Vector3D(1.0, Double.NEGATIVE_INFINITY, 3.0).isInfinite());
        Assert.assertTrue(new Vector3D(1.0, 2.0, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v3 = new Vector3D(2.0, 2.0, 3.0);
        Vector3D v4 = new Vector3D(1.0, 3.0, 3.0);
        Vector3D v5 = new Vector3D(1.0, 2.0, 4.0);

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("test"));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(v4));
        Assert.assertFalse(v1.equals(v5));

        Vector3D nan1 = new Vector3D(Double.NaN, 1.0, 2.0);
        Vector3D nan2 = new Vector3D(1.0, Double.NaN, 2.0);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertFalse(v1.equals(nan1));
        Assert.assertFalse(nan1.equals(v1));
        Assert.assertEquals(8, nan1.hashCode());
    }

    @Test
    public void testDotAndCrossProduct() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        double dot = Vector3D.dotProduct(v1, v2);
        Assert.assertEquals(32.0, dot, EPSILON);

        Vector3D cross = Vector3D.crossProduct(v1, v2);
        Assert.assertEquals(-3.0, cross.getX(), EPSILON);
        Assert.assertEquals(6.0, cross.getY(), EPSILON);
        Assert.assertEquals(-3.0, cross.getZ(), EPSILON);
    }

    @Test
    public void testDistances() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 6.0, 15.0);

        Assert.assertEquals(17.0, Vector3D.distance1(v1, v2), EPSILON);
        Assert.assertEquals(13.0, Vector3D.distance(v1, v2), EPSILON);
        Assert.assertEquals(12.0, Vector3D.distanceInf(v1, v2), EPSILON);
        Assert.assertEquals(169.0, Vector3D.distanceSq(v1, v2), EPSILON);
    }

    @Test
    public void testToString() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        String s = v.toString();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("1"));
        Assert.assertTrue(s.contains("2"));
        Assert.assertTrue(s.contains("3"));
    }
}
