package org.apache.commons.math3.geometry.euclidean.threed;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    @Test
    public void testConstructorTwoPoints() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 5, 6);
        Line line = new Line(p1, p2);
        Assert.assertNotNull(line.getDirection());
        Assert.assertNotNull(line.getOrigin());
        // direction should be normalized
        Assert.assertEquals(1.0, line.getDirection().getNorm(), 1.0e-15);
        // origin should be the projection of (0,0,0) onto the line
        Vector3D expectedZero = new Vector3D(1.0, p1, -p1.dotProduct(p2.subtract(p1)) / p2.subtract(p1).getNormSq(), p2.subtract(p1));
        Assert.assertEquals(0.0, expectedZero.subtract(line.getOrigin()).getNorm(), 1.0e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorEqualPoints() {
        Vector3D p = new Vector3D(1, 2, 3);
        new Line(p, p);
    }

    @Test
    public void testCopyConstructor() {
        Vector3D p1 = new Vector3D(1, 0, 0);
        Vector3D p2 = new Vector3D(2, 0, 0);
        Line original = new Line(p1, p2);
        Line copy = new Line(original);
        Assert.assertEquals(original.getDirection(), copy.getDirection());
        Assert.assertEquals(original.getOrigin(), copy.getOrigin());
    }

    @Test
    public void testReset() {
        Vector3D p1 = new Vector3D(1, 0, 0);
        Vector3D p2 = new Vector3D(2, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D q1 = new Vector3D(0, 1, 0);
        Vector3D q2 = new Vector3D(0, 2, 0);
        line.reset(q1, q2);
        Assert.assertEquals(0.0, line.getDirection().getX(), 1.0e-15);
        Assert.assertEquals(1.0, line.getDirection().getY(), 1.0e-15);
        Assert.assertEquals(0.0, line.getDirection().getZ(), 1.0e-15);
        // origin should be projection of (0,0,0) onto y-axis, which is (0,0,0)
        Assert.assertEquals(0.0, line.getOrigin().getNorm(), 1.0e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testResetEqualPoints() {
        Line line = new Line(new Vector3D(1, 0, 0), new Vector3D(2, 0, 0));
        line.reset(new Vector3D(1, 1, 1), new Vector3D(1, 1, 1));
    }

    @Test
    public void testRevert() {
        Vector3D p1 = new Vector3D(1, 0, 0);
        Vector3D p2 = new Vector3D(2, 0, 0);
        Line line = new Line(p1, p2);
        Line reverted = line.revert();
        // direction should be opposite
        Assert.assertEquals(-1.0, reverted.getDirection().getX(), 1.0e-15);
        Assert.assertEquals(0.0, reverted.getDirection().getY(), 1.0e-15);
        Assert.assertEquals(0.0, reverted.getDirection().getZ(), 1.0e-15);
        // origin should be the same
        Assert.assertEquals(line.getOrigin(), reverted.getOrigin());
    }

    @Test
    public void testGetDirection() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Assert.assertEquals(1.0, line.getDirection().getX(), 1.0e-15);
        Assert.assertEquals(0.0, line.getDirection().getY(), 1.0e-15);
        Assert.assertEquals(0.0, line.getDirection().getZ(), 1.0e-15);
    }

    @Test
    public void testGetOrigin() {
        Vector3D p1 = new Vector3D(1, 0, 0);
        Vector3D p2 = new Vector3D(2, 0, 0);
        Line line = new Line(p1, p2);
        // origin is projection of (0,0,0) onto x-axis, which is (0,0,0)
        Assert.assertEquals(0.0, line.getOrigin().getX(), 1.0e-15);
        Assert.assertEquals(0.0, line.getOrigin().getY(), 1.0e-15);
        Assert.assertEquals(0.0, line.getOrigin().getZ(), 1.0e-15);
    }

    @Test
    public void testGetAbscissa() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        // zero is (0,0,0)
        Assert.assertEquals(0.0, line.getAbscissa(new Vector3D(0, 0, 0)), 1.0e-15);
        Assert.assertEquals(1.0, line.getAbscissa(new Vector3D(1, 0, 0)), 1.0e-15);
        Assert.assertEquals(-1.0, line.getAbscissa(new Vector3D(-1, 0, 0)), 1.0e-15);
        // point off line: (0,1,0) projection is (0,0,0) so abscissa 0
        Assert.assertEquals(0.0, line.getAbscissa(new Vector3D(0, 1, 0)), 1.0e-15);
    }

    @Test
    public void testPointAt() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Assert.assertEquals(0.0, line.pointAt(0).getX(), 1.0e-15);
        Assert.assertEquals(1.0, line.pointAt(1).getX(), 1.0e-15);
        Assert.assertEquals(-1.0, line.pointAt(-1).getX(), 1.0e-15);
    }

    @Test
    public void testToSubSpace() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector1D result = line.toSubSpace(new Vector3D(2, 0, 0));
        Assert.assertEquals(2.0, result.getX(), 1.0e-15);
    }

    @Test
    public void testToSpace() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D result = line.toSpace(new Vector1D(3.0));
        Assert.assertEquals(3.0, result.getX(), 1.0e-15);
        Assert.assertEquals(0.0, result.getY(), 1.0e-15);
        Assert.assertEquals(0.0, result.getZ(), 1.0e-15);
    }

    @Test
    public void testIsSimilarToSameLine() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertTrue(line1.isSimilarTo(line2));
    }

    @Test
    public void testIsSimilarToOppositeDirection() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(-1, 0, 0));
        Assert.assertTrue(line1.isSimilarTo(line2));
    }

    @Test
    public void testIsSimilarToDifferentLine() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        Assert.assertFalse(line1.isSimilarTo(line2));
    }

    @Test
    public void testIsSimilarToParallelButNotContainingZero() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0)); // direction (1,0,0) but zero is (0,1,0) not on line1
        Assert.assertFalse(line1.isSimilarTo(line2));
    }

    @Test
    public void testIsSimilarToAngleNearZero() {
        // create a line with direction very close to (1,0,0)
        Vector3D dir = new Vector3D(1.0, 1.0e-11, 0.0);
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), dir);
        Assert.assertTrue(line1.isSimilarTo(line2));
    }

    @Test
    public void testIsSimilarToAngleNearPI() {
        Vector3D dir = new Vector3D(-1.0, 1.0e-11, 0.0);
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), dir);
        Assert.assertTrue(line1.isSimilarTo(line2));
    }

    @Test
    public void testContainsPointOnLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertTrue(line.contains(new Vector3D(0, 0, 0)));
        Assert.assertTrue(line.contains(new Vector3D(5, 0, 0)));
    }

    @Test
    public void testContainsPointOffLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertFalse(line.contains(new Vector3D(0, 1, 0)));
    }

    @Test
    public void testContainsPointVeryClose() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertTrue(line.contains(new Vector3D(0, 1.0e-11, 0)));
    }

    @Test
    public void testDistancePointOnLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertEquals(0.0, line.distance(new Vector3D(0, 0, 0)), 1.0e-15);
        Assert.assertEquals(0.0, line.distance(new Vector3D(10, 0, 0)), 1.0e-15);
    }

    @Test
    public void testDistancePointOffLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Assert.assertEquals(1.0, line.distance(new Vector3D(0, 1, 0)), 1.0e-15);
        Assert.assertEquals(2.0, line.distance(new Vector3D(3, 2, 0)), 1.0e-15);
    }

    @Test
    public void testDistanceLineParallel() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        // distance should be distance from line1.zero to line2.zero? Actually distance(line) for parallel lines returns distance(line.zero)
        // line1.zero is (0,0,0), line2.zero is (0,1,0) distance = 1
        Assert.assertEquals(1.0, line1.distance(line2), 1.0e-15);
    }

    @Test
    public void testDistanceLineIntersecting() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        Assert.assertEquals(0.0, line1.distance(line2), 1.0e-15);
    }

    @Test
    public void testDistanceLineSkew() {
        // two skew lines: x-axis and line parallel to y-axis but offset in z
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 1), new Vector3D(0, 1, 1));
        // distance should be 1
        Assert.assertEquals(1.0, line1.distance(line2), 1.0e-15);
    }

    @Test
    public void testClosestPointParallel() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        // parallel, closestPoint returns zero of this line
        Assert.assertEquals(line1.getOrigin(), line1.closestPoint(line2));
    }

    @Test
    public void testClosestPointIntersecting() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        // intersection is (0,0,0)
        Vector3D closest = line1.closestPoint(line2);
        Assert.assertEquals(0.0, closest.getX(), 1.0e-15);
        Assert.assertEquals(0.0, closest.getY(), 1.0e-15);
        Assert.assertEquals(0.0, closest.getZ(), 1.0e-15);
    }

    @Test
    public void testClosestPointSkew() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 1), new Vector3D(0, 1, 1));
        // closest point on line1 to line2 should be (0,0,0) because line2 is offset in z, and line1 is x-axis, the shortest segment is along z-axis from (0,0,0) to (0,0,1)
        Vector3D closest = line1.closestPoint(line2);
        Assert.assertEquals(0.0, closest.getX(), 1.0e-15);
        Assert.assertEquals(0.0, closest.getY(), 1.0e-15);
        Assert.assertEquals(0.0, closest.getZ(), 1.0e-15);
    }

    @Test
    public void testIntersectionIntersecting() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        Vector3D intersection = line1.intersection(line2);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.0, intersection.getX(), 1.0e-15);
        Assert.assertEquals(0.0, intersection.getY(), 1.0e-15);
        Assert.assertEquals(0.0, intersection.getZ(), 1.0e-15);
    }

    @Test
    public void testIntersectionParallelDistinct() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        Assert.assertNull(line1.intersection(line2));
    }

    @Test
    public void testIntersectionCoincident() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(2, 0, 0));
        Vector3D intersection = line1.intersection(line2);
        Assert.assertNotNull(intersection);
        // closestPoint returns zero, and line2.contains(zero) is true, so intersection is zero
        Assert.assertEquals(line1.getOrigin(), intersection);
    }

    @Test
    public void testWholeLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine sub = line.wholeLine();
        Assert.assertNotNull(sub);
        Assert.assertEquals(line, sub.getLine());
        Assert.assertTrue(sub.getRemainingRegion() instanceof IntervalsSet);
    }
}
