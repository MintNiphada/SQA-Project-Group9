package org.jfree.chart.renderer;

import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrayPaintScaleTest {

    @Test
    public void testDefaultConstructor() {
        GrayPaintScale scale = new GrayPaintScale();
        assertEquals(0.0, scale.getLowerBound(), 0.0000001);
        assertEquals(1.0, scale.getUpperBound(), 0.0000001);
    }

    @Test
    public void testConstructorWithValidBounds() {
        GrayPaintScale scale = new GrayPaintScale(10.0, 20.0);
        assertEquals(10.0, scale.getLowerBound(), 0.0000001);
        assertEquals(20.0, scale.getUpperBound(), 0.0000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEqualBounds() {
        new GrayPaintScale(5.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithLowerGreaterThanUpper() {
        new GrayPaintScale(10.0, 5.0);
    }

    @Test
    public void testGetPaintWithinBounds() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 100.0);
        Paint paint = scale.getPaint(50.0);
        assertTrue(paint instanceof Color);
        Color c = (Color) paint;
        int expected = (int) ((50.0 - 0.0) / (100.0 - 0.0) * 255.0);
        assertEquals(expected, c.getRed());
        assertEquals(expected, c.getGreen());
        assertEquals(expected, c.getBlue());
    }

    @Test
    public void testGetPaintAtLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(0.0);
        Color c = (Color) paint;
        assertEquals(0, c.getRed());
        assertEquals(0, c.getGreen());
        assertEquals(0, c.getBlue());
    }

    @Test
    public void testGetPaintAtUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.0);
        Color c = (Color) paint;
        assertEquals(255, c.getRed());
        assertEquals(255, c.getGreen());
        assertEquals(255, c.getBlue());
    }

    @Test
    public void testGetPaintBelowLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(-1.0);
        Color c = (Color) paint;
        int expected = (int) ((-1.0 - 0.0) / (1.0 - 0.0) * 255.0);
        assertEquals(expected, c.getRed());
        assertEquals(expected, c.getGreen());
        assertEquals(expected, c.getBlue());
    }

    @Test
    public void testGetPaintAboveUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(2.0);
        Color c = (Color) paint;
        int expected = (int) ((2.0 - 0.0) / (1.0 - 0.0) * 255.0);
        assertEquals(expected, c.getRed());
        assertEquals(expected, c.getGreen());
        assertEquals(expected, c.getBlue());
    }

    @Test
    public void testGetPaintWithNegativeBounds() {
        GrayPaintScale scale = new GrayPaintScale(-10.0, -5.0);
        Paint paint = scale.getPaint(-7.5);
        Color c = (Color) paint;
        int expected = (int) ((-7.5 - (-10.0)) / (-5.0 - (-10.0)) * 255.0);
        assertEquals(expected, c.getRed());
        assertEquals(expected, c.getGreen());
        assertEquals(expected, c.getBlue());
    }

    @Test
    public void testEqualsSameObject() {
        GrayPaintScale scale = new GrayPaintScale(1.0, 2.0);
        assertTrue(scale.equals(scale));
    }

    @Test
    public void testEqualsNull() {
        GrayPaintScale scale = new GrayPaintScale(1.0, 2.0);
        assertFalse(scale.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        GrayPaintScale scale = new GrayPaintScale(1.0, 2.0);
        assertFalse(scale.equals("string"));
    }

    @Test
    public void testEqualsSameValues() {
        GrayPaintScale scale1 = new GrayPaintScale(1.0, 2.0);
        GrayPaintScale scale2 = new GrayPaintScale(1.0, 2.0);
        assertTrue(scale1.equals(scale2));
    }

    @Test
    public void testEqualsDifferentLowerBound() {
        GrayPaintScale scale1 = new GrayPaintScale(1.0, 2.0);
        GrayPaintScale scale2 = new GrayPaintScale(1.5, 2.0);
        assertFalse(scale1.equals(scale2));
    }

    @Test
    public void testEqualsDifferentUpperBound() {
        GrayPaintScale scale1 = new GrayPaintScale(1.0, 2.0);
        GrayPaintScale scale2 = new GrayPaintScale(1.0, 3.0);
        assertFalse(scale1.equals(scale2));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        GrayPaintScale original = new GrayPaintScale(5.0, 10.0);
        GrayPaintScale cloned = (GrayPaintScale) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.getLowerBound(), cloned.getLowerBound(), 0.0000001);
        assertEquals(original.getUpperBound(), cloned.getUpperBound(), 0.0000001);
        assertTrue(original.equals(cloned));
    }

    @Test
    public void testGetPaintNeverReturnsNull() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertNotNull(scale.getPaint(0.5));
        assertNotNull(scale.getPaint(0.0));
        assertNotNull(scale.getPaint(1.0));
        assertNotNull(scale.getPaint(-1.0));
        assertNotNull(scale.getPaint(2.0));
    }
}
