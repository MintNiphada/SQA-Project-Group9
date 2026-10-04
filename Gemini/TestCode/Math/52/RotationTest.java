package org.apache.commons.math.geometry.euclidean.threed;

import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class RotationTest {

    private static final double EPS = 1.0e-10;

    @Test
    public void testIdentity() {
        Rotation r = Rotation.IDENTITY;
        Assert.assertEquals(1.0, r.getQ0(), EPS);
        Assert.assertEquals(0.0, r.getQ1(), EPS);
        Assert.assertEquals(0.0, r.getQ2(), EPS);
        Assert.assertEquals(0.0, r.getQ3(), EPS);
        Assert.assertEquals(0.0, r.getAngle(), EPS);
        Vector3D axis = r.getAxis();
        Assert.assertEquals(1.0, axis.getX(), EPS);
        Assert.assertEquals(0.0, axis.getY(), EPS);
        Assert.assertEquals(0.0, axis.getZ(), EPS);
    }

    @Test
    public void testQuaternionConstructor() {
        Rotation r1 = new Rotation(2.0, 0.0, 0.0, 0.0, true);
        Assert.assertEquals(1.0, r1.getQ0(), EPS);
        Assert.assertEquals(0.0, r1.getQ1(), EPS);
        Assert.assertEquals(0.0, r1.getQ2(), EPS);
        Assert.assertEquals(0.0, r1.getQ3(), EPS);

        Rotation r2 = new Rotation(0.5, 0.5, 0.5, 0.5, false);
        Assert.assertEquals(0.5, r2.getQ0(), EPS);
        Assert.assertEquals(0.5, r2.getQ1(), EPS);
        Assert.assertEquals(0.5, r2.getQ2(), EPS);
        Assert.assertEquals(0.5, r2.getQ3(), EPS);
    }

    @Test
    public void testAxisAngleConstructor() {
        Rotation r = new Rotation(Vector3D.PLUS_K, FastMath.PI / 2);
        Vector3D v = r.applyTo(Vector3D.PLUS_I);
        Assert.assertEquals(0.0, v.getX(), EPS);
        Assert.assertEquals(1.0, v.getY(), EPS);
        Assert.assertEquals(0.0, v.getZ(), EPS);
        Assert.assertEquals(FastMath.PI / 2, r.getAngle(), EPS);
    }

    @Test(expected = ArithmeticException.class)
    public void testAxisAngleZeroNorm() {
        new Rotation(Vector3D.ZERO, 1.0);
    }

    @Test
    public void testMatrixConstructorSuccess() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 1.0, 0.0, 0.0 },
            { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 }
        };
        Rotation r = new Rotation(m, 1.0e-5);
        Assert.assertEquals(1.0, r.getQ0(), EPS);
        Assert.assertEquals(0.0, r.getQ1(), EPS);
        Assert.assertEquals(0.0, r.getQ2(), EPS);
        Assert.assertEquals(0.0, r.getQ3(), EPS);
    }

    @Test
    public void testMatrixConstructorAllBranches() throws NotARotationMatrixException {
        Rotation r0 = new Rotation(Vector3D.PLUS_I, 0.1);
        Rotation r0Mat = new Rotation(r0.getMatrix(), 1.0e-10);
        Assert.assertEquals(0.0, Rotation.distance(r0, r0Mat), EPS);

        Rotation r1 = new Rotation(Vector3D.PLUS_I, FastMath.PI);
        Rotation r1Mat = new Rotation(r1.getMatrix(), 1.0e-10);
        Assert.assertEquals(0.0, Rotation.distance(r1, r1Mat), EPS);

        Rotation r2 = new Rotation(Vector3D.PLUS_J, FastMath.PI);
        Rotation r2Mat = new Rotation(r2.getMatrix(), 1.0e-10);
        Assert.assertEquals(0.0, Rotation.distance(r2, r2Mat), EPS);

        Rotation r3 = new Rotation(Vector3D.PLUS_K, FastMath.PI);
        Rotation r3Mat = new Rotation(r3.getMatrix(), 1.0e-10);
        Assert.assertEquals(0.0, Rotation.distance(r3, r3Mat), EPS);
    }

    @Test(expected = NotARotationMatrixException.class)
    public void testMatrixWrongDimensionRows() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        };
        new Rotation(m, 1.0e-5);
    }

    @Test(expected = NotARotationMatrixException.class)
    public void testMatrixWrongDimensionCols() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 1.0, 0.0, 0.0 },
            { 0.0, 1.0 },
            { 0.0, 0.0, 1.0 }
        };
        new Rotation(m, 1.0e-5);
    }

    @Test(expected = NotARotationMatrixException.class)
    public void testMatrixNegativeDeterminant() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { -1.0, 0.0, 0.0 },
            { 0.0, -1.0, 0.0 },
            { 0.0, 0.0, -1.0 }
        };
        new Rotation(m, 1.0e-5);
    }

    @Test(expected = NotARotationMatrixException.class)
    public void testMatrixNonOrthogonal() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
        };
        new Rotation(m, 1.0e-15);
    }

    @Test
    public void testTwoPairVectorConstructor() {
        Vector3D u1 = new Vector3D(1, 0, 0);
        Vector3D u2 = new Vector3D(0, 1, 0);
        Vector3D v1 = new Vector3D(0, 1, 0);
        Vector3D v2 = new Vector3D(-1, 0, 0);
        Rotation r = new Rotation(u1, u2, v1, v2);
        Assert.assertEquals(0.0, Vector3D.distance(v1, r.applyTo(u1)), EPS);
        Assert.assertEquals(0.0, Vector3D.distance(v2, r.applyTo(u2)), EPS);
    }

    @Test
    public void testTwoPairVectorSingularities() {
        Rotation rId = new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_J);
        Assert.assertEquals(1.0, rId.getQ0(), EPS);
        Assert.assertEquals(0.0, rId.getQ1(), EPS);

        Rotation rDeg1 = new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);
        Assert.assertEquals(0.0, Vector3D.distance(Vector3D.PLUS_I, rDeg1.applyTo(Vector3D.PLUS_I)), EPS);

        Rotation rDeg2 = new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K, Vector3D.PLUS_J);
        Assert.assertEquals(0.0, Vector3D.distance(Vector3D.PLUS_J, rDeg2.applyTo(Vector3D.PLUS_J)), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwoPairVectorZeroNorm() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);
    }

    @Test
    public void testVectorPairConstructor() {
        Vector3D u = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v = new Vector3D(-2.0, 1.0, 4.0);
        Rotation r = new Rotation(u, v);
        Vector3D transformed = r.applyTo(u);
        Assert.assertEquals(0.0, Vector3D.distance(v.normalize(), transformed.normalize()), EPS);

        Rotation rOpposite = new Rotation(Vector3D.PLUS_I, Vector3D.MINUS_I);
        Vector3D oppTrans = rOpposite.applyTo(Vector3D.PLUS_I);
        Assert.assertEquals(0.0, Vector3D.distance(Vector3D.MINUS_I, oppTrans), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVectorPairZeroNorm() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    @Test
    public void testRevert() {
        Rotation r = new Rotation(new Vector3D(1, 2, 3), 1.2);
        Rotation rev = r.revert();
        Rotation composed = r.applyTo(rev);
        Assert.assertEquals(0.0, Rotation.distance(Rotation.IDENTITY, composed), EPS);
        Assert.assertEquals(-r.getQ0(), rev.getQ0(), EPS);
        Assert.assertEquals(r.getQ1(), rev.getQ1(), EPS);
        Assert.assertEquals(r.getQ2(), rev.getQ2(), EPS);
        Assert.assertEquals(r.getQ3(), rev.getQ3(), EPS);
    }

    @Test
    public void testAxisAndAngleBranches() {
        Rotation rPos = new Rotation(0.8, 0.6, 0.0, 0.0, false);
        Assert.assertEquals(0.6 / FastMath.sin(rPos.getAngle() / 2), -rPos.getAxis().getX(), EPS);

        Rotation rNeg = new Rotation(-0.8, 0.6, 0.0, 0.0, false);
        Assert.assertEquals(0.6 / FastMath.sin(rNeg.getAngle() / 2), rNeg.getAxis().getX(), EPS);

        Rotation rSmallPos = new Rotation(0.05, FastMath.sqrt(1 - 0.0025), 0, 0, false);
        Assert.assertEquals(2 * FastMath.acos(0.05), rSmallPos.getAngle(), EPS);

        Rotation rSmallNeg = new Rotation(-0.05, FastMath.sqrt(1 - 0.0025), 0, 0, false);
        Assert.assertEquals(2 * FastMath.acos(0.05), rSmallNeg.getAngle(), EPS);
    }

    @Test
    public void testEulerCardanAngles() throws CardanEulerSingularityException {
        RotationOrder[] orders = new RotationOrder[] {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX,
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
        };

        for (RotationOrder order : orders) {
            double a1 = 0.1;
            double a2 = 0.2;
            double a3 = 0.3;
            Rotation r = new Rotation(order, a1, a2, a3);
            double[] angles = r.getAngles(order);
            Rotation reconstructed = new Rotation(order, angles[0], angles[1], angles[2]);
            Assert.assertEquals(0.0, Rotation.distance(r, reconstructed), EPS);
        }
    }

    @Test
    public void testCardanSingularities() {
        RotationOrder[] cardanOrders = new RotationOrder[] {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
        };
        for (RotationOrder order : cardanOrders) {
            try {
                Rotation r = new Rotation(order, 0.1, FastMath.PI / 2, 0.3);
                r.getAngles(order);
                Assert.fail();
            } catch (CardanEulerSingularityException e) {
                Assert.assertTrue(e.getMessage().length() > 0);
            }
        }
    }

    @Test
    public void testEulerSingularities() {
        RotationOrder[] eulerOrders = new RotationOrder[] {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
        };
        for (RotationOrder order : eulerOrders) {
            try {
                Rotation.IDENTITY.getAngles(order);
                Assert.fail();
            } catch (CardanEulerSingularityException e) {
                Assert.assertTrue(e.getMessage().length() > 0);
            }
        }
    }

    @Test
    public void testApplyToVectorAndInverse() {
        Rotation r = new Rotation(new Vector3D(1, 1, 1), 1.0);
        Vector3D v = new Vector3D(2, -3, 5);
        Vector3D applied = r.applyTo(v);
        Vector3D back = r.applyInverseTo(applied);
        Assert.assertEquals(0.0, Vector3D.distance(v, back), EPS);
    }

    @Test
    public void testApplyInverseToRotation() {
        Rotation r1 = new Rotation(Vector3D.PLUS_I, 0.5);
        Rotation r2 = new Rotation(Vector3D.PLUS_J, 0.8);
        Rotation comp = r1.applyInverseTo(r2);
        Rotation expected = r1.revert().applyTo(r2);
        Assert.assertEquals(0.0, Rotation.distance(expected, comp), EPS);
    }

    @Test
    public void testDistance() {
        Rotation r1 = new Rotation(Vector3D.PLUS_I, 0.5);
        Rotation r2 = new Rotation(Vector3D.PLUS_I, 0.7);
        Assert.assertEquals(0.2, Rotation.distance(r1, r2), EPS);
        Assert.assertEquals(0.0, Rotation.distance(r1, r1), EPS);
    }
}
