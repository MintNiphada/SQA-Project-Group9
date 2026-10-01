package org.jfree.chart.util;

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

import org.junit.Assert;
import org.junit.Test;

public class ShapeUtilitiesTest {

    @Test
    public void testClone_Null() {
        Assert.assertNull(ShapeUtilities.clone(null));
    }

    @Test
    public void testClone_CloneableShape() {
        Rectangle2D original = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(original);
        Assert.assertNotNull(cloned);
        Assert.assertNotSame(original, cloned);
        Assert.assertTrue(cloned instanceof Rectangle2D);
        Rectangle2D clonedRect = (Rectangle2D) cloned;
        Assert.assertEquals(original.getX(), clonedRect.getX(), 0.0);
        Assert.assertEquals(original.getY(), clonedRect.getY(), 0.0);
        Assert.assertEquals(original.getWidth(), clonedRect.getWidth(), 0.0);
        Assert.assertEquals(original.getHeight(), clonedRect.getHeight(), 0.0);
    }

    @Test
    public void testClone_NotCloneableShape() {
        Polygon poly = new Polygon();
        poly.addPoint(0, 0);
        Assert.assertNull(ShapeUtilities.clone(poly));
    }

    @Test
    public void testEqual_Shape_NullNull() {
        Assert.assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
    }

    @Test
    public void testEqual_Shape_NullNotNull() {
        Assert.assertFalse(ShapeUtilities.equal(null, new Rectangle2D.Double()));
        Assert.assertFalse(ShapeUtilities.equal(new Rectangle2D.Double(), null));
    }

    @Test
    public void testEqual_Shape_Line2D() {
        Line2D line1 = new Line2D.Double(0, 0, 1, 1);
        Line2D line2 = new Line2D.Double(0, 0, 1, 1);
        Assert.assertTrue(ShapeUtilities.equal(line1, line2));
        Line2D line3 = new Line2D.Double(0, 0, 2, 2);
        Assert.assertFalse(ShapeUtilities.equal(line1, line3));
    }

    @Test
    public void testEqual_Shape_Ellipse2D() {
        Ellipse2D e1 = new Ellipse2D.Double(0, 0, 10, 10);
        Ellipse2D e2 = new Ellipse2D.Double(0, 0, 10, 10);
        Assert.assertTrue(ShapeUtilities.equal(e1, e2));
        Ellipse2D e3 = new Ellipse2D.Double(1, 1, 10, 10);
        Assert.assertFalse(ShapeUtilities.equal(e1, e3));
    }

    @Test
    public void testEqual_Shape_Arc2D() {
        Arc2D a1 = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.PIE);
        Assert.assertTrue(ShapeUtilities.equal(a1, a2));
        Arc2D a3 = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.CHORD);
        Assert.assertFalse(ShapeUtilities.equal(a1, a3));
    }

    @Test
    public void testEqual_Shape_Polygon() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Assert.assertTrue(ShapeUtilities.equal(p1, p2));
        Polygon p3 = new Polygon(new int[]{0, 1, 0}, new int[]{0, 0, 1}, 3);
        Assert.assertFalse(ShapeUtilities.equal(p1, p3));
    }

    @Test
    public void testEqual_Shape_GeneralPath() {
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0f, 0f);
        path1.lineTo(10f, 10f);

        Assert.assertTrue(ShapeUtilities.equal(path1, path1));

        GeneralPath different = new GeneralPath();
        different.moveTo(5f, 5f);
        different.lineTo(15f, 15f);
        Assert.assertTrue(ShapeUtilities.equal(path1, different));
    }

    @Test
    public void testEqual_Shape_Fallback() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(0, 0, 10, 10);
        Assert.assertTrue(ShapeUtilities.equal(r1, r2));
        Rectangle2D r3 = new Rectangle2D.Double(1, 1, 10, 10);
        Assert.assertFalse(ShapeUtilities.equal(r1, r3));
    }

    @Test
    public void testEqual_Line2D_Nulls() {
        Assert.assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        Assert.assertFalse(ShapeUtilities.equal(null, new Line2D.Double()));
        Assert.assertFalse(ShapeUtilities.equal(new Line2D.Double(), null));
    }

    @Test
    public void testEqual_Line2D_Points() {
        Line2D l1 = new Line2D.Double(0, 0, 1, 1);
        Line2D l2 = new Line2D.Double(0, 0, 1, 1);
        Assert.assertTrue(ShapeUtilities.equal(l1, l2));
        Line2D l3 = new Line2D.Double(0, 0, 2, 2);
        Assert.assertFalse(ShapeUtilities.equal(l1, l3));
        Line2D l4 = new Line2D.Double(1, 1, 2, 2);
        Assert.assertFalse(ShapeUtilities.equal(l1, l4));
    }

    @Test
    public void testEqual_Ellipse2D_Nulls() {
        Assert.assertTrue(ShapeUtilities.equal((Ellipse2D) null, (Ellipse2D) null));
        Assert.assertFalse(ShapeUtilities.equal(null, new Ellipse2D.Double()));
        Assert.assertFalse(ShapeUtilities.equal(new Ellipse2D.Double(), null));
    }

    @Test
    public void testEqual_Ellipse2D_Frame() {
        Ellipse2D e1 = new Ellipse2D.Double(0, 0, 10, 10);
        Ellipse2D e2 = new Ellipse2D.Double(0, 0, 10, 10);
        Assert.assertTrue(ShapeUtilities.equal(e1, e2));
        Ellipse2D e3 = new Ellipse2D.Double(1, 0, 10, 10);
        Assert.assertFalse(ShapeUtilities.equal(e1, e3));
    }

    @Test
    public void testEqual_Arc2D_Nulls() {
        Assert.assertTrue(ShapeUtilities.equal((Arc2D) null, (Arc2D) null));
        Assert.assertFalse(ShapeUtilities.equal(null, new Arc2D.Double()));
        Assert.assertFalse(ShapeUtilities.equal(new Arc2D.Double(), null));
    }

    @Test
    public void testEqual_Arc2D_DifferentProperties() {
        Arc2D base = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.PIE);
        Arc2D same = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.PIE);
        Assert.assertTrue(ShapeUtilities.equal(base, same));

        Arc2D diffFrame = new Arc2D.Double(1, 1, 10, 10, 30, 60, Arc2D.PIE);
        Assert.assertFalse(ShapeUtilities.equal(base, diffFrame));

        Arc2D diffStart = new Arc2D.Double(0, 0, 10, 10, 40, 60, Arc2D.PIE);
        Assert.assertFalse(ShapeUtilities.equal(base, diffStart));

        Arc2D diffExtent = new Arc2D.Double(0, 0, 10, 10, 30, 70, Arc2D.PIE);
        Assert.assertFalse(ShapeUtilities.equal(base, diffExtent));

        Arc2D diffType = new Arc2D.Double(0, 0, 10, 10, 30, 60, Arc2D.CHORD);
        Assert.assertFalse(ShapeUtilities.equal(base, diffType));
    }

    @Test
    public void testEqual_Polygon_Nulls() {
        Assert.assertTrue(ShapeUtilities.equal((Polygon) null, (Polygon) null));
        Assert.assertFalse(ShapeUtilities.equal(null, new Polygon()));
        Assert.assertFalse(ShapeUtilities.equal(new Polygon(), null));
    }

    @Test
    public void testEqual_Polygon_DifferentPointCounts() {
        Polygon p1 = new Polygon(new int[]{0, 1, 2}, new int[]{0, 1, 2}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Assert.assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test
    public void testEqual_Polygon_SamePoints() {
        Polygon p1 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Polygon p2 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Assert.assertTrue(ShapeUtilities.equal(p1, p2));
    }

    @Test
    public void testEqual_Polygon_DifferentXPoints() {
        Polygon p1 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Polygon p2 = new Polygon(new int[]{1, 1}, new int[]{0, 1}, 2);
        Assert.assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test
    public void testEqual_Polygon_DifferentYPoints() {
        Polygon p1 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Polygon p2 = new Polygon(new int[]{0, 1}, new int[]{1, 1}, 2);
        Assert.assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test
    public void testEqual_GeneralPath_Nulls() {
        Assert.assertTrue(ShapeUtilities.equal((GeneralPath) null, (GeneralPath) null));
        Assert.assertFalse(ShapeUtilities.equal(null, new GeneralPath()));
        Assert.assertFalse(ShapeUtilities.equal(new GeneralPath(), null));
    }

    @Test
    public void testEqual_GeneralPath_DifferentWindingRule() {
        GeneralPath p1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        p1.moveTo(0, 0);
        GeneralPath p2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        p2.moveTo(0, 0);
        Assert.assertTrue(ShapeUtilities.equal(p1, p2));
    }

    @Test
    public void testEqual_GeneralPath_SelfEquality() {
        GeneralPath path = new GeneralPath();
        path.moveTo(0, 0);
        path.lineTo(1, 1);
        Assert.assertTrue(ShapeUtilities.equal(path, path));
    }

    @Test
    public void testCreateTranslatedShape_Simple() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, 5, 5);
        Assert.assertNotNull(translated);
        Assert.assertTrue(translated instanceof Rectangle2D);
        Rectangle2D translatedRect = (Rectangle2D) translated;
        Assert.assertEquals(5.0, translatedRect.getX(), 0.0);
        Assert.assertEquals(5.0, translatedRect.getY(), 0.0);
        Assert.assertEquals(10.0, translatedRect.getWidth(), 0.0);
        Assert.assertEquals(10.0, translatedRect.getHeight(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShape_NullShape() {
        ShapeUtilities.createTranslatedShape(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShapeWithAnchor_NullShape() {
        ShapeUtilities.createTranslatedShape(null, RectangleAnchor.CENTER, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShapeWithAnchor_NullAnchor() {
        ShapeUtilities.createTranslatedShape(new Rectangle2D.Double(), null, 0, 0);
    }

    @Test
    public void testCreateTranslatedShapeWithAnchor_Valid() {
        Rectangle2D rect = new Rectangle2D.Double(10, 10, 20, 20);
        Shape translated = ShapeUtilities.createTranslatedShape(rect,
                RectangleAnchor.CENTER, 50, 60);
        Assert.assertNotNull(translated);
        Rectangle2D bounds = translated.getBounds2D();
        Assert.assertEquals(50 - bounds.getWidth() / 2, bounds.getCenterX(), 0.001);
        Assert.assertEquals(60 - bounds.getHeight() / 2, bounds.getCenterY(), 0.001);
    }

    @Test
    public void testRotateShape_Null() {
        Assert.assertNull(ShapeUtilities.rotateShape(null, 0.5, 0f, 0f));
    }

    @Test
    public void testRotateShape_Valid() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(rect, Math.PI / 2, 5f, 5f);
        Assert.assertNotNull(rotated);
        Assert.assertNotSame(rect, rotated);
    }

    @Test
    public void testDrawRotatedShape() {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D rect = new Rectangle2D.Double(1, 1, 5, 5);
        ShapeUtilities.drawRotatedShape(g2, rect, Math.PI / 4, 3f, 3f);
        g2.dispose();
    }

    @Test
    public void testCreateDiagonalCross() {
        Shape cross = ShapeUtilities.createDiagonalCross(5f, 1f);
        Assert.assertNotNull(cross);
        Assert.assertTrue(cross instanceof GeneralPath);
    }

    @Test
    public void testCreateRegularCross() {
        Shape cross = ShapeUtilities.createRegularCross(5f, 1f);
        Assert.assertNotNull(cross);
        Assert.assertTrue(cross instanceof GeneralPath);
    }

    @Test
    public void testCreateDiamond() {
        Shape diamond = ShapeUtilities.createDiamond(10f);
        Assert.assertNotNull(diamond);
        Assert.assertTrue(diamond instanceof GeneralPath);
    }

    @Test
    public void testCreateUpTriangle() {
        Shape tri = ShapeUtilities.createUpTriangle(10f);
        Assert.assertNotNull(tri);
        Assert.assertTrue(tri instanceof GeneralPath);
    }

    @Test
    public void testCreateDownTriangle() {
        Shape tri = ShapeUtilities.createDownTriangle(10f);
        Assert.assertNotNull(tri);
        Assert.assertTrue(tri instanceof GeneralPath);
    }

    @Test
    public void testCreateLineRegion_NonVertical() {
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Shape region = ShapeUtilities.createLineRegion(line, 2f);
        Assert.assertNotNull(region);
    }

    @Test
    public void testCreateLineRegion_VerticalLine() {
        Line2D line = new Line2D.Double(5, 0, 5, 10);
        Shape region = ShapeUtilities.createLineRegion(line, 2f);
        Assert.assertNotNull(region);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPointInRectangle_NullArea() {
        ShapeUtilities.getPointInRectangle(0, 0, null);
    }

    @Test
    public void testGetPointInRectangle_Inside() {
        Rectangle2D area = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D pt = ShapeUtilities.getPointInRectangle(5, 5, area);
        Assert.assertEquals(5.0, pt.getX(), 0.0);
        Assert.assertEquals(5.0, pt.getY(), 0.0);
    }

    @Test
    public void testGetPointInRectangle_Outside() {
        Rectangle2D area = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D pt = ShapeUtilities.getPointInRectangle(20, 20, area);
        Assert.assertEquals(10.0, pt.getX(), 0.0);
        Assert.assertEquals(10.0, pt.getY(), 0.0);
    }

    @Test
    public void testGetPointInRectangle_Boundary() {
        Rectangle2D area = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D pt = ShapeUtilities.getPointInRectangle(-5, -5, area);
        Assert.assertEquals(0.0, pt.getX(), 0.0);
        Assert.assertEquals(0.0, pt.getY(), 0.0);
    }

    @Test
    public void testContains_True() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D inner = new Rectangle2D.Double(2, 2, 5, 5);
        Assert.assertTrue(ShapeUtilities.contains(outer, inner));
    }

    @Test
    public void testContains_False_OutOfBounds() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D inner = new Rectangle2D.Double(9, 9, 5, 5);
        Assert.assertFalse(ShapeUtilities.contains(outer, inner));
    }

    @Test
    public void testContains_ZeroDimensionTrue() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D inner = new Rectangle2D.Double(5, 5, 0, 0);
        Assert.assertTrue(ShapeUtilities.contains(outer, inner));
    }

    @Test
    public void testIntersects_True() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(5, 5, 10, 10);
        Assert.assertTrue(ShapeUtilities.intersects(r1, r2));
    }

    @Test
    public void testIntersects_False() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(20, 20, 10, 10);
        Assert.assertFalse(ShapeUtilities.intersects(r1, r2));
    }

    @Test
    public void testIntersects_Touching() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(10, 10, 10, 10);
        Assert.assertTrue(ShapeUtilities.intersects(r1, r2));
    }
}
