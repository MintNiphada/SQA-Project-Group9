package org.apache.commons.math3.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class SubLineTest {

    @Test
    public void testConstructorLineIntervalsSet() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        IntervalsSet region = new IntervalsSet(0.0, 1.0);
        SubLine sub = new SubLine(line, region);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        Assert.assertEquals(0.0, seg.getStart().getX(), 1e-15);
        Assert.assertEquals(0.0, seg.getStart().getY(), 1e-15);
        Assert.assertEquals(0.0, seg.getStart().getZ(), 1e-15);
        Assert.assertEquals(1.0, seg.getEnd().getX(), 1e-15);
        Assert.assertEquals(0.0, seg.getEnd().getY(), 1e-15);
        Assert.assertEquals(0.0, seg.getEnd().getZ(), 1e-15);
    }

    @Test
    public void testConstructorTwoPointsValid() {
        Vector3D start = new Vector3D(1, 2, 3);
        Vector3D end = new Vector3D(4, 5, 6);
        SubLine sub = new SubLine(start, end);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        Assert.assertEquals(start, seg.getStart());
        Assert.assertEquals(end, seg.getEnd());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorTwoPointsEqual() {
        Vector3D p = new Vector3D(1, 2, 3);
        new SubLine(p, p);
    }

    @Test
    public void testConstructorSegmentValid() {
        Vector3D start = new Vector3D(0, 0, 0);
        Vector3D end = new Vector3D(1, 0, 0);
        Line line = new Line(start, end);
        Segment segment = new Segment(start, end, line);
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        Assert.assertEquals(start, seg.getStart());
        Assert.assertEquals(end, seg.getEnd());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorSegmentEqualEndpoints() {
        Vector3D p = new Vector3D(1, 2, 3);
        Line line = new Line(p, new Vector3D(2, 3, 4)); // line not through p? Actually, line must pass through p, but we can create any line.
        // Segment with equal endpoints will cause buildIntervalSet to throw.
        new SubLine(new Segment(p, p, line));
    }

    @Test
    public void testGetSegmentsEmpty() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        IntervalsSet empty = new IntervalsSet();
        SubLine sub = new SubLine(line, empty);
        List<Segment> segments = sub.getSegments();
        Assert.assertTrue(segments.isEmpty());
    }

    @Test
    public void testGetSegmentsMultipleIntervals() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        List<IntervalsSet> intervals = new ArrayList<IntervalsSet>();
        intervals.add(new IntervalsSet(0.0, 1.0));
        intervals.add(new IntervalsSet(2.0, 3.0));
        IntervalsSet multi = new IntervalsSet(intervals);
        SubLine sub = new SubLine(line, multi);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(2, segments.size());
        // First segment: 0 to 1
        Segment seg0 = segments.get(0);
        Assert.assertEquals(0.0, seg0.getStart().getX(), 1e-15);
        Assert.assertEquals(1.0, seg0.getEnd().getX(), 1e-15);
        // Second segment: 2 to 3
        Segment seg1 = segments.get(1);
        Assert.assertEquals(2.0, seg1.getStart().getX(), 1e-15);
        Assert.assertEquals(3.0, seg1.getEnd().getX(), 1e-15);
    }

    @Test
    public void testGetSegmentsUnbounded() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        IntervalsSet unbounded = new IntervalsSet(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        SubLine sub = new SubLine(line, unbounded);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        Assert.assertTrue(Double.isInfinite(seg.getStart().getX()));
        Assert.assertTrue(Double.isInfinite(seg.getEnd().getX()));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, seg.getStart().getX(), 0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, seg.getEnd().getX(), 0);
    }

    @Test
    public void testIntersectionInsideBothIncludeTrue() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0)); // parallel? No, direction (1,0,0) and (1,0,0) are parallel, they won't intersect. Need intersecting lines.
        // Use lines that intersect at (0,0,0): line1 X axis, line2 Y axis.
        line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        IntervalsSet region1 = new IntervalsSet(-1.0, 1.0);
        IntervalsSet region2 = new IntervalsSet(-1.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.0, intersection.getX(), 1e-15);
        Assert.assertEquals(0.0, intersection.getY(), 1e-15);
        Assert.assertEquals(0.0, intersection.getZ(), 1e-15);
    }

    @Test
    public void testIntersectionInsideBothIncludeFalse() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        IntervalsSet region1 = new IntervalsSet(-1.0, 1.0);
        IntervalsSet region2 = new IntervalsSet(-1.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, false);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.0, intersection.getX(), 1e-15);
        Assert.assertEquals(0.0, intersection.getY(), 1e-15);
        Assert.assertEquals(0.0, intersection.getZ(), 1e-15);
    }

    @Test
    public void testIntersectionAtEndpointIncludeTrue() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(1, 0, 0), new Vector3D(1, 1, 0)); // line2 through (1,0,0) along Y
        IntervalsSet region1 = new IntervalsSet(0.0, 1.0); // includes endpoint 1
        IntervalsSet region2 = new IntervalsSet(0.0, 1.0); // includes endpoint 0 in Y? Actually line2 parameterization: origin (1,0,0), direction (0,1,0). So point (1,0,0) corresponds to abscissa 0. So region2 [0,1] includes that endpoint.
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(1.0, intersection.getX(), 1e-15);
        Assert.assertEquals(0.0, intersection.getY(), 1e-15);
        Assert.assertEquals(0.0, intersection.getZ(), 1e-15);
    }

    @Test
    public void testIntersectionAtEndpointIncludeFalse() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(1, 0, 0), new Vector3D(1, 1, 0));
        IntervalsSet region1 = new IntervalsSet(0.0, 1.0);
        IntervalsSet region2 = new IntervalsSet(0.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, false);
        Assert.assertNull(intersection);
    }

    @Test
    public void testIntersectionOutsideOneRange() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        IntervalsSet region1 = new IntervalsSet(0.5, 1.0); // does not include 0
        IntervalsSet region2 = new IntervalsSet(-1.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNull(intersection);
    }

    @Test
    public void testIntersectionParallelLines() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0)); // parallel to X axis
        IntervalsSet region1 = new IntervalsSet(0.0, 1.0);
        IntervalsSet region2 = new IntervalsSet(0.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNull(intersection);
    }

    @Test
    public void testIntersectionSkewLines() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(0, 1, 1)); // direction (0,0,1)
        IntervalsSet region1 = new IntervalsSet(0.0, 1.0);
        IntervalsSet region2 = new IntervalsSet(0.0, 1.0);
        SubLine sub1 = new SubLine(line1, region1);
        SubLine sub2 = new SubLine(line2, region2);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNull(intersection);
    }

    @Test
    public void testIntersectionUnbounded() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        IntervalsSet unbounded = new IntervalsSet(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        SubLine sub1 = new SubLine(line1, unbounded);
        SubLine sub2 = new SubLine(line2, unbounded);
        Vector3D intersection = sub1.intersection(sub2, true);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.0, intersection.getX(), 1e-15);
        Assert.assertEquals(0.0, intersection.getY(), 1e-15);
        Assert.assertEquals(0.0, intersection.getZ(), 1e-15);
    }
}
