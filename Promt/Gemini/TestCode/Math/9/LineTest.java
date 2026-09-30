package org.apache.commons.math3.geometry.euclidean.threed;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    public void testConstructorsAndGetters() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 6, 3);
        Line line = new Line(p1, p2);

        Vector3D dir = line.getDirection();
        Assert.assertEquals(1.0, dir.getNorm(), EPSILON);
        Assert.assertEquals(3.0 / 5.0, dir.getX(), EPSILON);
        Assert.assertEquals(4.0 / 5.0, dir.getY(), EPSILON);
        Assert.assertEquals(0.0, dir.getZ(), EPSILON);

        Vector3D origin = line.getOrigin();
        Assert.assertEquals(0.0, origin.dotProduct(dir), EPSILON);
        Assert.assertTrue(line.contains(p1));
        Assert.assertTrue(line.contains(p2));

        Line copy = new Line(line);
        Assert.assertEquals(line.getDirection(), copy.getDirection());
        Assert.assertEquals(line.getOrigin(), copy.getOrigin());
        Assert.assertTrue(copy.isSimilarTo(line));
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testIdenticalPointsConstructor() {
        Vector3D p = new Vector3D(1.0, 2.0, 3.0);
        new Line(p, new Vector3D(1.0, 2.0, 3.0));
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testResetWithIdenticalPoints() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        line.reset(new Vector3D(2, 3, 4), new Vector3D(2, 3, 4));
    }

    @Test
    public void testRevert() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 6, 3);
        Line line = new Line(p1, p2);
        Line reverted = line.revert();

        Assert.assertEquals(0.0, reverted.getDirection().add(line.getDirection()).getNorm(), EPSILON);
        Assert.assertEquals(0.0, reverted.getOrigin().distance(line.getOrigin()), EPSILON);
        Assert.assertTrue(line.isSimilarTo(reverted));
    }

    @Test
    public void testAbscissaAndPointAt() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 0, 1));
        Vector3D p = new Vector3D(5, 5, 3);

        double abscissa = line.getAbscissa(p);
        Assert.assertEquals(3.0, abscissa, EPSILON);

        Vector3D projected = line.pointAt(abscissa);
        Assert.assertEquals(0.0, projected.getX(), EPSILON);
        Assert.assertEquals(0.0, projected.getY(), EPSILON);
        Assert.assertEquals(3.0, projected.getZ(), EPSILON);
    }

    @Test
    public void testTransformSpaces() {
        Line line = new Line(new Vector3D(1, 0, 0), new Vector3D(1, 0, 2));
        Vector3D p3D = new Vector3D(1, 0, 5);

        Vector1D p1D = line.toSubSpace(p3D);
        Assert.assertEquals(5.0, p1D.getX(), EPSILON);

        Vector3D reconstructed = line.toSpace(p1D);
        Assert.assertEquals(1.0, reconstructed.getX(), EPSILON);
        Assert.assertEquals(0.0, reconstructed.getY(), EPSILON);
        Assert.assertEquals(5.0, reconstructed.getZ(), EPSILON);
    }

    @Test
    public void testIsSimilarTo() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 6, 3);
        Line l1 = new Line(p1, p2);
        Line l2 = new Line(p2, p1); // reversed direction, same line
        Line l3 = new Line(new Vector3D(1, 2, 4), new Vector3D(4, 6, 4)); // parallel, different line
        Line l4 = new Line(p1, new Vector3D(1, 2, 10)); // intersecting at p1, different direction

        Assert.assertTrue(l1.isSimilarTo(l1));
        Assert.assertTrue(l1.isSimilarTo(l2));
        Assert.assertFalse(l1.isSimilarTo(l3));
        Assert.assertFalse(l1.isSimilarTo(l4));
    }

    @Test
    public void testContains() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
        Assert.assertTrue(line.contains(new Vector3D(2, 2, 2)));
        Assert.assertTrue(line.contains(new Vector3D(-3, -3, -3)));
        Assert.assertFalse(line.contains(new Vector3D(1, 1, 0)));
    }

    @Test
    public void testDistanceToPoint() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 0, 1));
        Assert.assertEquals(5.0, line.distance(new Vector3D(3, 4, 10)), EPSILON);
        Assert.assertEquals(0.0, line.distance(new Vector3D(0, 0, 100)), EPSILON);
    }

    @Test
    public void testDistanceToLine() {
        Line l1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line l2 = new Line(new Vector3D(0, 2, 0), new Vector3D(1, 2, 0)); // Parallel
        Assert.assertEquals(2.0, l1.distance(l2), EPSILON);

        Line l3 = new Line(new Vector3D(0, 0, 3), new Vector3D(0, 1, 3)); // Skew
        Assert.assertEquals(3.0, l1.distance(l3), EPSILON);

        Line l4 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0)); // Intersecting
        Assert.assertEquals(0.0, l1.distance(l4), EPSILON);
    }

    @Test
    public void testClosestPoint() {
        Line l1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line l2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0)); // Intersecting at origin
        Vector3D cp = l1.closestPoint(l2);
        Assert.assertEquals(0.0, cp.getX(), EPSILON);
        Assert.assertEquals(0.0, cp.getY(), EPSILON);
        Assert.assertEquals(0.0, cp.getZ(), EPSILON);

        Line l3 = new Line(new Vector3D(2, 3, 5), new Vector3D(2, 4, 5)); // Skew
        Vector3D cp2 = l1.closestPoint(l3);
        Assert.assertEquals(2.0, cp2.getX(), EPSILON);
        Assert.assertEquals(0.0, cp2.getY(), EPSILON);
        Assert.assertEquals(0.0, cp2.getZ(), EPSILON);

        Line l4 = new Line(new Vector3D(0, 5, 0), new Vector3D(1, 5, 0)); // Parallel
        Vector3D cpParallel = l1.closestPoint(l4);
        Assert.assertEquals(l1.getOrigin(), cpParallel);
    }

    @Test
    public void testIntersection() {
        Line l1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line l2 = new Line(new Vector3D(2, -1, 0), new Vector3D(2, 1, 0)); // Intersects at (2,0,0)
        Vector3D intersection = l1.intersection(l2);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(2.0, intersection.getX(), EPSILON);
        Assert.assertEquals(0.0, intersection.getY(), EPSILON);
        Assert.assertEquals(0.0, intersection.getZ(), EPSILON);

        Line l3 = new Line(new Vector3D(2, -1, 1), new Vector3D(2, 1, 1)); // Skew, no intersection
        Assert.assertNull(l1.intersection(l3));

        Line l4 = new Line(new Vector3D(0, 2, 0), new Vector3D(1, 2, 0)); // Parallel, no intersection
        Assert.assertNull(l1.intersection(l4));
    }

    @Test
    public void testWholeLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        SubLine subLine = line.wholeLine();
        Assert.assertNotNull(subLine);
        Assert.assertEquals(Double.POSITIVE_INFINITY, subLine.getSegments().get(0).getInf().getX(), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, subLine.getSegments().get(0).getInf().getY(), EPSILON);
    }
}
