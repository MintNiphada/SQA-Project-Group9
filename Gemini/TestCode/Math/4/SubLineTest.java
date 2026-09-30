package org.apache.commons.math3.geometry.euclidean.threed;

import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class SubLineTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testConstructorFromEndpoints() {
        Vector3D start = new Vector3D(1.0, 2.0, 3.0);
        Vector3D end = new Vector3D(4.0, 6.0, 8.0);
        SubLine subLine = new SubLine(start, end);

        List<Segment> segments = subLine.getSegments();
        Assert.assertNotNull(segments);
        Assert.assertEquals(1, segments.size());

        Segment segment = segments.get(0);
        Assert.assertEquals(0.0, start.distance(segment.getStart()), EPSILON);
        Assert.assertEquals(0.0, end.distance(segment.getEnd()), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorFromIdenticalEndpoints() {
        Vector3D p = new Vector3D(1.0, 2.0, 3.0);
        new SubLine(p, new Vector3D(1.0, 2.0, 3.0));
    }

    @Test
    public void testConstructorFromSegment() {
        Vector3D start = new Vector3D(0.0, 0.0, 0.0);
        Vector3D end = new Vector3D(1.0, 1.0, 1.0);
        Line line = new Line(start, end);
        Segment segment = new Segment(start, end, line);

        SubLine subLine = new SubLine(segment);
        List<Segment> segments = subLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, start.distance(segments.get(0).getStart()), EPSILON);
        Assert.assertEquals(0.0, end.distance(segments.get(0).getEnd()), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorFromDegenerateSegment() {
        Vector3D p = new Vector3D(1.0, 1.0, 1.0);
        Line line = new Line(p, new Vector3D(2.0, 2.0, 2.0));
        Segment segment = new Segment(p, p, line);
        new SubLine(segment);
    }

    @Test
    public void testGetSegmentsWholeLine() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine subLine = new SubLine(line, new IntervalsSet());

        List<Segment> segments = subLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Segment segment = segments.get(0);
        Assert.assertTrue(Double.isInfinite(segment.getStart().getX()));
        Assert.assertTrue(Double.isInfinite(segment.getEnd().getX()));
    }

    @Test
    public void testGetSegmentsEmpty() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine subLine = new SubLine(line, new IntervalsSet(1.0, -1.0));

        List<Segment> segments = subLine.getSegments();
        Assert.assertTrue(segments.isEmpty());
    }

    @Test
    public void testGetSegmentsMultipleIntervals() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        IntervalsSet set1 = new IntervalsSet(0.0, 1.0);
        IntervalsSet set2 = new IntervalsSet(2.0, 3.0);
        IntervalsSet combined = (IntervalsSet) set1.union(set2);

        SubLine subLine = new SubLine(line, combined);
        List<Segment> segments = subLine.getSegments();
        Assert.assertEquals(2, segments.size());
    }

    @Test
    public void testIntersectionInsideInside() {
        SubLine subLine1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(2, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, -1, 0), new Vector3D(1, 1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNotNull(resultIncluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultIncluded), EPSILON);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNotNull(resultExcluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultExcluded), EPSILON);
    }

    @Test
    public void testIntersectionInsideBoundary() {
        SubLine subLine1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(2, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, 0, 0), new Vector3D(1, 1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNotNull(resultIncluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultIncluded), EPSILON);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionBoundaryInside() {
        SubLine subLine1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, -1, 0), new Vector3D(1, 1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNotNull(resultIncluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultIncluded), EPSILON);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionBoundaryBoundary() {
        SubLine subLine1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, 0, 0), new Vector3D(1, 1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNotNull(resultIncluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultIncluded), EPSILON);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionInsideOutside() {
        SubLine subLine1 = new SubLine(new Vector3D(0, 0, 0), new Vector3D(2, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, 1, 0), new Vector3D(1, 2, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNull(resultIncluded);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionOutsideInside() {
        SubLine subLine1 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(3, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, -1, 0), new Vector3D(1, 1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNull(resultIncluded);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionOutsideOutside() {
        SubLine subLine1 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(3, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, 1, 0), new Vector3D(1, 2, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNull(resultIncluded);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNull(resultExcluded);
    }

    @Test
    public void testIntersectionReverseEndpoints() {
        SubLine subLine1 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(0, 0, 0));
        SubLine subLine2 = new SubLine(new Vector3D(1, 1, 0), new Vector3D(1, -1, 0));

        Vector3D resultIncluded = subLine1.intersection(subLine2, true);
        Assert.assertNotNull(resultIncluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultIncluded), EPSILON);

        Vector3D resultExcluded = subLine1.intersection(subLine2, false);
        Assert.assertNotNull(resultExcluded);
        Assert.assertEquals(0.0, new Vector3D(1, 0, 0).distance(resultExcluded), EPSILON);
    }
}
