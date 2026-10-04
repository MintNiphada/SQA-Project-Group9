package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.junit.Assert;
import org.junit.Test;

public class PolygonsSetTest {

    @Test
    public void testEmptyPolygon() {
        PolygonsSet set = new PolygonsSet(new BSPTree<Euclidean2D>(Boolean.FALSE));
        Assert.assertEquals(0.0, set.getSize(), 1.0e-10);
        Assert.assertEquals(0.0, set.getBarycenter().getX(), 1.0e-10);
        Assert.assertEquals(0.0, set.getBarycenter().getY(), 1.0e-10);
        Assert.assertEquals(0, set.getVertices().length);
    }

    @Test
    public void testFullSpacePolygon() {
        PolygonsSet set = new PolygonsSet();
        Assert.assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
        Assert.assertTrue(Double.isNaN(set.getBarycenter().getX()));
        Assert.assertTrue(Double.isNaN(set.getBarycenter().getY()));
        Assert.assertEquals(0, set.getVertices().length);
    }

    @Test
    public void testBox() {
        PolygonsSet set = new PolygonsSet(0.0, 2.0, 0.0, 3.0);
        Assert.assertEquals(6.0, set.getSize(), 1.0e-10);
        Vector2D barycenter = (Vector2D) set.getBarycenter();
        Assert.assertEquals(1.0, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(1.5, barycenter.getY(), 1.0e-10);

        Vector2D[][] vertices = set.getVertices();
        Assert.assertEquals(1, vertices.length);
        Assert.assertEquals(4, vertices[0].length);

        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(new Vector2D(1.0, 1.0)));
        Assert.assertEquals(Region.Location.OUTSIDE, set.checkPoint(new Vector2D(3.0, 1.0)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector2D(0.0, 1.5)));
    }

    @Test
    public void testHalfPlane() {
        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(0.0, 1.0));
        SubLine sub = line.wholeHyperplane();
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(sub,
                new BSPTree<Euclidean2D>(Boolean.TRUE),
                new BSPTree<Euclidean2D>(Boolean.FALSE),
                null);
        PolygonsSet set = new PolygonsSet(tree);

        Assert.assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
        Assert.assertTrue(Double.isNaN(set.getBarycenter().getX()));
        Vector2D[][] vertices = set.getVertices();
        Assert.assertEquals(1, vertices.length);
        Assert.assertEquals(3, vertices[0].length);
        Assert.assertNull(vertices[0][0]);
        Assert.assertNotNull(vertices[0][1]);
        Assert.assertNotNull(vertices[0][2]);
    }

    @Test
    public void testQuadrantOpenLoop() {
        PolygonsSet set1 = new PolygonsSet(new BSPTree<Euclidean2D>(
                new Line(new Vector2D(0, 0), new Vector2D(1, 0)).wholeHyperplane(),
                new BSPTree<Euclidean2D>(Boolean.TRUE),
                new BSPTree<Euclidean2D>(Boolean.FALSE),
                null));
        PolygonsSet set2 = new PolygonsSet(new BSPTree<Euclidean2D>(
                new Line(new Vector2D(0, 0), new Vector2D(0, 1)).wholeHyperplane(),
                new BSPTree<Euclidean2D>(Boolean.TRUE),
                new BSPTree<Euclidean2D>(Boolean.FALSE),
                null));
        PolygonsSet intersection = (PolygonsSet) new RegionFactory<Euclidean2D>().intersection(set1, set2);

        Assert.assertEquals(Double.POSITIVE_INFINITY, intersection.getSize(), 1.0e-10);
        Assert.assertTrue(Double.isNaN(intersection.getBarycenter().getX()));
        Vector2D[][] vertices = intersection.getVertices();
        Assert.assertEquals(1, vertices.length);
        Assert.assertTrue(vertices[0].length >= 4);
        Assert.assertNull(vertices[0][0]);
    }

    @Test
    public void testInvertedBox() {
        PolygonsSet box = new PolygonsSet(0.0, 2.0, 0.0, 2.0);
        PolygonsSet inverted = (PolygonsSet) new RegionFactory<Euclidean2D>().getComplement(box);

        Assert.assertEquals(Double.POSITIVE_INFINITY, inverted.getSize(), 1.0e-10);
        Assert.assertTrue(Double.isNaN(inverted.getBarycenter().getX()));
        Vector2D[][] vertices = inverted.getVertices();
        Assert.assertEquals(1, vertices.length);
        Assert.assertEquals(4, vertices[0].length);
    }

    @Test
    public void testBoxWithHole() {
        PolygonsSet outer = new PolygonsSet(0.0, 10.0, 0.0, 10.0);
        PolygonsSet inner = new PolygonsSet(2.0, 8.0, 2.0, 8.0);
        PolygonsSet holeSet = (PolygonsSet) new RegionFactory<Euclidean2D>().difference(outer, inner);

        Assert.assertEquals(64.0, holeSet.getSize(), 1.0e-10);
        Vector2D barycenter = (Vector2D) holeSet.getBarycenter();
        Assert.assertEquals(5.0, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(5.0, barycenter.getY(), 1.0e-10);
        Vector2D[][] vertices = holeSet.getVertices();
        Assert.assertEquals(2, vertices.length);
    }

    @Test
    public void testDisjointBoxes() {
        PolygonsSet box1 = new PolygonsSet(0.0, 2.0, 0.0, 2.0);
        PolygonsSet box2 = new PolygonsSet(10.0, 12.0, 10.0, 12.0);
        PolygonsSet union = (PolygonsSet) new RegionFactory<Euclidean2D>().union(box1, box2);

        Assert.assertEquals(8.0, union.getSize(), 1.0e-10);
        Vector2D barycenter = (Vector2D) union.getBarycenter();
        Assert.assertEquals(6.0, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(6.0, barycenter.getY(), 1.0e-10);
        Vector2D[][] vertices = union.getVertices();
        Assert.assertEquals(2, vertices.length);
    }

    @Test
    public void testBoundaryCollectionConstructor() {
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        Line l1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line l2 = new Line(new Vector2D(1, 0), new Vector2D(1, 1));
        Line l3 = new Line(new Vector2D(1, 1), new Vector2D(0, 1));
        Line l4 = new Line(new Vector2D(0, 1), new Vector2D(0, 0));

        boundary.add(new SubLine(new Vector2D(0, 0), new Vector2D(1, 0)));
        boundary.add(new SubLine(new Vector2D(1, 0), new Vector2D(1, 1)));
        boundary.add(new SubLine(new Vector2D(1, 1), new Vector2D(0, 1)));
        boundary.add(new SubLine(new Vector2D(0, 1), new Vector2D(0, 0)));

        PolygonsSet set = new PolygonsSet(boundary);
        Assert.assertEquals(1.0, set.getSize(), 1.0e-10);
        Vector2D barycenter = (Vector2D) set.getBarycenter();
        Assert.assertEquals(0.5, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(0.5, barycenter.getY(), 1.0e-10);
    }

    @Test
    public void testBuildNew() {
        PolygonsSet original = new PolygonsSet(0, 1, 0, 1);
        PolygonsSet created = original.buildNew(original.getTree(false));
        Assert.assertNotNull(created);
        Assert.assertEquals(1.0, created.getSize(), 1.0e-10);
    }

    @Test
    public void testVerticesCaching() {
        PolygonsSet set = new PolygonsSet(0, 1, 0, 1);
        Vector2D[][] v1 = set.getVertices();
        Vector2D[][] v2 = set.getVertices();
        Assert.assertNotSame(v1, v2);
        Assert.assertEquals(v1.length, v2.length);
        Assert.assertEquals(v1[0].length, v2[0].length);
    }

    @Test
    public void testXorOperation() {
        PolygonsSet box1 = new PolygonsSet(0, 2, 0, 2);
        PolygonsSet box2 = new PolygonsSet(1, 3, 0, 2);
        PolygonsSet xor = (PolygonsSet) new RegionFactory<Euclidean2D>().xor(box1, box2);
        Assert.assertEquals(2.0, xor.getSize(), 1.0e-10);
    }

    @Test
    public void testDegeneratedPolygon() {
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        boundary.add(new SubLine(new Vector2D(0, 0), new Vector2D(1, 1)));
        boundary.add(new SubLine(new Vector2D(1, 1), new Vector2D(0, 0)));
        PolygonsSet set = new PolygonsSet(boundary);
        Vector2D[][] vertices = set.getVertices();
        Assert.assertEquals(0, vertices.length);
    }

    @Test
    public void testEmptyBoundaryCollection() {
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        PolygonsSet set = new PolygonsSet(boundary);
        Assert.assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
    }
}
