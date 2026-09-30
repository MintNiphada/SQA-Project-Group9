package org.jfree.chart.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ShapeUtilitiesTest {

    @Test
    public void testClone() {
        Assert.assertNull(ShapeUtilities.clone(null));

        Rectangle2D rect = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape clonedRect = ShapeUtilities.clone(rect);
        Assert.assertNotNull(clonedRect);
        Assert.assertNotSame(rect, clonedRect);
        Assert.assertTrue(ShapeUtilities.equal(rect, clonedRect));

        Line2D line = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape clonedLine = ShapeUtilities.clone(line);
        Assert.assertNotNull(clonedLine);
        Assert.assertNotSame(line, clonedLine);
        Assert.assertTrue(ShapeUtilities.equal(line, clonedLine));

        GeneralPath gp = new GeneralPath();
        gp.moveTo(1.0f, 2.0f);
        gp.lineTo(3.0f, 4.0f);
        Shape clonedGp = ShapeUtilities.clone(gp);
        Assert.assertNotNull(clonedGp);
        Assert.assertNotSame(gp, clonedGp);
        Assert.assertTrue(ShapeUtilities.equal(gp, clonedGp));

        Shape nonCloneableShape = new Shape() {
            public java.awt.Rectangle getBounds() { return null; }
            public Rectangle2D getBounds2D() { return null; }
            public boolean contains(double x, double y) { return false; }
            public boolean contains(Point2D p) { return false; }
            public boolean intersects(double x, double y, double w, double h) { return false; }
            public boolean intersects(Rectangle2D r) { return false; }
            public boolean contains(double x, double y, double w, double h) { return false; }
            public boolean contains(Rectangle2D r) { return false; }
            public java.awt.geom.PathIterator getPathIterator(AffineTransform at) { return null; }
            public java.awt.geom.PathIterator getPathIterator(AffineTransform at, double flatness) { return null; }
        };
        Assert.assertNull(ShapeUtilities.clone(nonCloneableShape));
    }

    @Test
    public void testEqualShape() {
        Assert.assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
        Assert.assertFalse(ShapeUtilities.equal(new Line2D.Double(), null));
        Assert.assertFalse(ShapeUtilities.equal(null, new Line2D.Double()));

        // Line2D
        Shape l1 = new Line2D.Double(0, 0, 10, 10);
        Shape l2 = new Line2D.Double(0, 0, 10, 10);
        Shape l3 = new Line2D.Double(0, 0, 10, 11);
        Assert.assertTrue(ShapeUtilities.equal(l1, l2));
        Assert.assertFalse(ShapeUtilities.equal(l1, l3));

        // Ellipse2D
        Shape e1 = new Ellipse2D.Double(0, 0, 10, 10);
        Shape e2 = new Ellipse2D.Double(0, 0, 10, 10);
        Shape e3 = new Ellipse2D.Double(0, 0, 10, 11);
        Assert.assertTrue(ShapeUtilities.equal(e1, e2));
        Assert.assertFalse(ShapeUtilities.equal(e1, e3));

        // Arc2D
        Shape a1 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.OPEN);
        Shape a2 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.OPEN);
        Shape a3 = new Arc2D.Double(0, 0, 10, 10, 0, 95, Arc2D.OPEN);
        Assert.assertTrue(ShapeUtilities.equal(a1, a2));
        Assert.assertFalse(ShapeUtilities.equal(a1, a3));

        // Polygon
        Polygon p1 = new Polygon(new int[]{0, 10, 0}, new int[]{0, 0, 10}, 3);
        Polygon p2 = new Polygon(new int[]{0, 10, 0}, new int[]{0, 0, 10}, 3);
        Polygon p3 = new Polygon(new int[]{0, 10, 1}, new int[]{0, 0, 10}, 3);
        Assert.assertTrue(ShapeUtilities.equal(p1, p2));
        Assert.assertFalse(ShapeUtilities.equal(p1, p3));

        // GeneralPath
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(10, 10);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(10, 10);
        GeneralPath gp3 = new GeneralPath();
        gp3.moveTo(0, 0);
        gp3.lineTo(10, 11);
        Assert.assertTrue(ShapeUtilities.equal(gp1, gp2));
        Assert.assertFalse(ShapeUtilities.equal(gp1, gp3));

        // Mixed types / Rectangles
        Shape r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Shape r2 = new Rectangle2D.Double(0, 0, 10, 10);
        Shape r3 = new Rectangle2D.Double(0, 0, 10, 11);
        Assert.assertTrue(ShapeUtilities.equal(r1, r2));
        Assert.assertFalse(ShapeUtilities.equal(r1, r3));
        Assert.assertFalse(ShapeUtilities.equal(l1, e1));
    }

    @Test
    public void testEqualLine2D() {
        Assert.assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        Line2D l1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Assert.assertFalse(ShapeUtilities.equal(l1, null));
        Assert.assertFalse(ShapeUtilities.equal(null, l1));

        Line2D l2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Assert.assertTrue(ShapeUtilities.equal(l1, l2));

        Line2D l3 = new Line2D.Double(1.1, 2.0, 3.0, 4.0);
        Assert.assertFalse(ShapeUtilities.equal(l1, l3));

        Line2D l4 = new Line2D.Double(1.0, 2.0, 3.1, 4.0);
        Assert.assertFalse(ShapeUtilities.equal(l1, l4));
    }

    @Test
    public void testEqualEllipse2D() {
        Assert.assertTrue(ShapeUtilities.equal((Ellipse2D) null, (Ellipse2D) null));
        Ellipse2D e1 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Assert.assertFalse(ShapeUtilities.equal(e1, null));
        Assert.assertFalse(ShapeUtilities.equal(null, e1));

        Ellipse2D e2 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Assert.assertTrue(ShapeUtilities.equal(e1, e2));

        Ellipse2D e3 = new Ellipse2D.Double(1.1, 2.0, 3.0, 4.0);
        Assert.assertFalse(ShapeUtilities.equal(e1, e3));
    }

    @Test
    public void testEqualArc2D() {
        Assert.assertTrue(ShapeUtilities.equal((Arc2D) null, (Arc2D) null));
        Arc2D a1 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 10.0, 20.0, Arc2D.OPEN);
        Assert.assertFalse(ShapeUtilities.equal(a1, null));
        Assert.assertFalse(ShapeUtilities.equal(null, a1));

        Arc2D a2 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 10.0, 20.0, Arc2D.OPEN);
        Assert.assertTrue(ShapeUtilities.equal(a1, a2));

        Arc2D a3 = new Arc2D.Double(1.1, 2.0, 3.0, 4.0, 10.0, 20.0, Arc2D.OPEN);
        Assert.assertFalse(ShapeUtilities.equal(a1, a3));

        Arc2D a4 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 10.1, 20.0, Arc2D.OPEN);
        Assert.assertFalse(ShapeUtilities.equal(a1, a4));

        Arc2D a5 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 10.0, 20.1, Arc2D.OPEN);
        Assert.assertFalse(ShapeUtilities.equal(a1, a5));

        Arc2D a6 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 10.0, 20.0, Arc2D.CHORD);
        Assert.assertFalse(ShapeUtilities.equal(a1, a6));
    }

    @Test
    public void testEqualPolygon() {
        Assert.assertTrue(ShapeUtilities.equal((Polygon) null, (Polygon) null));
        Polygon p1 = new Polygon(new int[]{1, 2, 3}, new int[]{4, 5, 6}, 3);
        Assert.assertFalse(ShapeUtilities.equal(p1, null));
        Assert.assertFalse(ShapeUtilities.equal(null, p1));

        Polygon p2 = new Polygon(new int[]{1, 2, 3}, new int[]{4, 5, 6}, 3);
        Assert.assertTrue(ShapeUtilities.equal(p1, p2));

        Polygon p3 = new Polygon(new int[]{1, 2}, new int[]{4, 5}, 2);
        Assert.assertFalse(ShapeUtilities.equal(p1, p3));

        Polygon p4 = new Polygon(new int[]{1, 2, 4}, new int[]{4, 5, 6}, 3);
        Assert.assertFalse(ShapeUtilities.equal(p1, p4));

        Polygon p5 = new Polygon(new int[]{1, 2, 3}, new int[]{4, 5, 7}, 3);
        Assert.assertFalse(ShapeUtilities.equal(p1, p5));
    }

    @Test
    public void testEqualGeneralPath() {
        Assert.assertTrue(ShapeUtilities.equal((GeneralPath) null, (GeneralPath) null));
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(10, 10);
        Assert.assertFalse(ShapeUtilities.equal(gp1, null));
        Assert.assertFalse(ShapeUtilities.equal(null, gp1));

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(10, 10);
        Assert.assertTrue(ShapeUtilities.equal(gp1, gp2));

        GeneralPath gpWinding = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gpWinding.moveTo(0, 0);
        gpWinding.lineTo(10, 10);
        Assert.assertFalse(ShapeUtilities.equal(gp1, gpWinding));

        GeneralPath gpDifferentLen = new GeneralPath();
        gpDifferentLen.moveTo(0, 0);
        gpDifferentLen.lineTo(10, 10);
        gpDifferentLen.lineTo(20, 20);
        Assert.assertFalse(ShapeUtilities.equal(gp1, gpDifferentLen));
        Assert.assertFalse(ShapeUtilities.equal(gpDifferentLen, gp1));

        GeneralPath gpDifferentSegType = new GeneralPath();
        gpDifferentSegType.moveTo(0, 0);
        gpDifferentSegType.quadTo(5, 5, 10, 10);
        Assert.assertFalse(ShapeUtilities.equal(gp1, gpDifferentSegType));

        GeneralPath gpDifferentCoords = new GeneralPath();
        gpDifferentCoords.moveTo(0, 0);
        gpDifferentCoords.lineTo(10, 11);
        Assert.assertFalse(ShapeUtilities.equal(gp1, gpDifferentCoords));
    }

    @Test
    public void testCreateTranslatedShape() {
        Shape shape = new Rectangle2D.Double(10, 20, 30, 40);
        Shape translated = ShapeUtilities.createTranslatedShape(shape, 5.0, -10.0);
        Rectangle2D bounds = translated.getBounds2D();
        Assert.assertEquals(15.0, bounds.getX(), 1e-6);
        Assert.assertEquals(10.0, bounds.getY(), 1e-6);
        Assert.assertEquals(30.0, bounds.getWidth(), 1e-6);
        Assert.assertEquals(40.0, bounds.getHeight(), 1e-6);

        try {
            ShapeUtilities.createTranslatedShape((Shape) null, 1.0, 2.0);
            Assert.fail("Expected IllegalArgumentException on null shape");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCreateTranslatedShapeWithAnchor() {
        Shape shape = new Rectangle2D.Double(0, 0, 100, 50);
        Shape translated = ShapeUtilities.createTranslatedShape(shape, RectangleAnchor.CENTER, 200.0, 200.0);
        Rectangle2D bounds = translated.getBounds2D();
        Assert.assertEquals(150.0, bounds.getX(), 1e-6);
        Assert.assertEquals(175.0, bounds.getY(), 1e-6);

        try {
            ShapeUtilities.createTranslatedShape(null, RectangleAnchor.CENTER, 100, 100);
            Assert.fail("Expected IllegalArgumentException on null shape");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            ShapeUtilities.createTranslatedShape(shape, null, 100, 100);
            Assert.fail("Expected IllegalArgumentException on null anchor");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRotateShape() {
        Assert.assertNull(ShapeUtilities.rotateShape(null, Math.PI / 2, 0, 0));

        Shape shape = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(shape, Math.PI / 2, 0, 0);
        Assert.assertNotNull(rotated);
        Rectangle2D bounds = rotated.getBounds2D();
        Assert.assertEquals(-10.0, bounds.getX(), 1e-4);
        Assert.assertEquals(0.0, bounds.getY(), 1e-4);
        Assert.assertEquals(10.0, bounds.getWidth(), 1e-4);
        Assert.assertEquals(10.0, bounds.getHeight(), 1e-4);
    }

    @Test
    public void testDrawRotatedShape() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        AffineTransform originalTransform = g2.getTransform();

        Shape shape = new Rectangle2D.Double(10, 10, 20, 20);
        ShapeUtilities.drawRotatedShape(g2, shape, Math.PI / 4, 20.0f, 20.0f);

        Assert.assertEquals(originalTransform, g2.getTransform());
        g2.dispose();
    }

    @Test
    public void testShapeCreationMethods() {
        Shape diagCross = ShapeUtilities.createDiagonalCross(10.0f, 2.0f);
        Assert.assertNotNull(diagCross);
        Assert.assertTrue(diagCross.getBounds2D().getWidth() > 0);

        Shape regCross = ShapeUtilities.createRegularCross(10.0f, 2.0f);
        Assert.assertNotNull(regCross);
        Assert.assertTrue(regCross.getBounds2D().getWidth() > 0);

        Shape diamond = ShapeUtilities.createDiamond(10.0f);
        Assert.assertNotNull(diamond);
        Assert.assertTrue(diamond.getBounds2D().getWidth() > 0);

        Shape upTri = ShapeUtilities.createUpTriangle(10.0f);
        Assert.assertNotNull(upTri);
        Assert.assertTrue(upTri.getBounds2D().getWidth() > 0);

        Shape downTri = ShapeUtilities.createDownTriangle(10.0f);
        Assert.assertNotNull(downTri);
        Assert.assertTrue(downTri.getBounds2D().getWidth() > 0);
    }

    @Test
    public void testCreateLineRegion() {
        // Non-vertical line
        Line2D line1 = new Line2D.Double(0, 0, 10, 10);
        Shape region1 = ShapeUtilities.createLineRegion(line1, 2.0f);
        Assert.assertNotNull(region1);
        Assert.assertTrue(region1.getBounds2D().getWidth() > 0);

        // Vertical line (x1 == x2)
        Line2D line2 = new Line2D.Double(5, 0, 5, 10);
        Shape region2 = ShapeUtilities.createLineRegion(line2, 4.0f);
        Assert.assertNotNull(region2);
        Rectangle2D bounds = region2.getBounds2D();
        Assert.assertEquals(3.0, bounds.getX(), 1e-4);
        Assert.assertEquals(7.0, bounds.getMaxX(), 1e-4);
    }

    @Test
    public void testGetPointInRectangle() {
        Rectangle2D rect = new Rectangle2D.Double(10, 20, 30, 40); // x: 10..40, y: 20..60

        Point2D inside = ShapeUtilities.getPointInRectangle(25, 35, rect);
        Assert.assertEquals(25.0, inside.getX(), 1e-6);
        Assert.assertEquals(35.0, inside.getY(), 1e-6);

        Point2D lowX = ShapeUtilities.getPointInRectangle(5, 35, rect);
        Assert.assertEquals(10.0, lowX.getX(), 1e-6);
        Assert.assertEquals(35.0, lowX.getY(), 1e-6);

        Point2D highX = ShapeUtilities.getPointInRectangle(45, 35, rect);
        Assert.assertEquals(40.0, highX.getX(), 1e-6);
        Assert.assertEquals(35.0, highX.getY(), 1e-6);

        Point2D lowY = ShapeUtilities.getPointInRectangle(25, 10, rect);
        Assert.assertEquals(25.0, lowY.getX(), 1e-6);
        Assert.assertEquals(20.0, lowY.getY(), 1e-6);

        Point2D highY = ShapeUtilities.getPointInRectangle(25, 70, rect);
        Assert.assertEquals(25.0, highY.getX(), 1e-6);
        Assert.assertEquals(60.0, highY.getY(), 1e-6);

        try {
            ShapeUtilities.getPointInRectangle(0, 0, null);
            Assert.fail("Expected NullPointerException on null area");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testContains() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 100, 100);

        // Fully inside
        Assert.assertTrue(ShapeUtilities.contains(r1, new Rectangle2D.Double(10, 10, 20, 20)));

        // Equal bounds
        Assert.assertTrue(ShapeUtilities.contains(r1, new Rectangle2D.Double(0, 0, 100, 100)));

        // Zero width/height inside
        Assert.assertTrue(ShapeUtilities.contains(r1, new Rectangle2D.Double(10, 10, 0, 0)));

        // Outside cases
        Assert.assertFalse(ShapeUtilities.contains(r1, new Rectangle2D.Double(-1, 0, 10, 10)));
        Assert.assertFalse(ShapeUtilities.contains(r1, new Rectangle2D.Double(0, -1, 10, 10)));
        Assert.assertFalse(ShapeUtilities.contains(r1, new Rectangle2D.Double(95, 0, 10, 10)));
        Assert.assertFalse(ShapeUtilities.contains(r1, new Rectangle2D.Double(0, 95, 10, 10)));
    }

    @Test
    public void testIntersects() {
        Rectangle2D r1 = new Rectangle2D.Double(10, 10, 100, 100); // 10..110, 10..110

        // Overlapping
        Assert.assertTrue(ShapeUtilities.intersects(r1, new Rectangle2D.Double(50, 50, 20, 20)));
        Assert.assertTrue(ShapeUtilities.intersects(r1, new Rectangle2D.Double(0, 0, 20, 20))); // overlaps at (10, 10)

        // Non-overlapping - left
        Assert.assertFalse(ShapeUtilities.intersects(r1, new Rectangle2D.Double(0, 50, 5, 20)));
        // Non-overlapping - right
        Assert.assertFalse(ShapeUtilities.intersects(r1, new Rectangle2D.Double(120, 50, 10, 10)));
        // Non-overlapping - top
        Assert.assertFalse(ShapeUtilities.intersects(r1, new Rectangle2D.Double(50, 0, 20, 5)));
        // Non-overlapping - bottom
        Assert.assertFalse(ShapeUtilities.intersects(r1, new Rectangle2D.Double(50, 120, 10, 10)));
    }
}