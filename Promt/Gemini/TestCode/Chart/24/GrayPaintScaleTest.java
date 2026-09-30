package org.jfree.chart.renderer;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.util.PublicCloneable;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link GrayPaintScale} class.
 */
public class GrayPaintScaleTest {

    private static final double EPSILON = 1e-9;

    /**
     * Test the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        GrayPaintScale scale = new GrayPaintScale();
        Assert.assertEquals(0.0, scale.getLowerBound(), EPSILON);
        Assert.assertEquals(1.0, scale.getUpperBound(), EPSILON);
    }

    /**
     * Test the parameterized constructor with valid bounds.
     */
    @Test
    public void testCustomConstructor() {
        GrayPaintScale scale = new GrayPaintScale(10.0, 20.0);
        Assert.assertEquals(10.0, scale.getLowerBound(), EPSILON);
        Assert.assertEquals(20.0, scale.getUpperBound(), EPSILON);

        GrayPaintScale negativeScale = new GrayPaintScale(-50.0, -10.0);
        Assert.assertEquals(-50.0, negativeScale.getLowerBound(), EPSILON);
        Assert.assertEquals(-10.0, negativeScale.getUpperBound(), EPSILON);
    }

    /**
     * Test the constructor when lower bound is strictly greater than upper bound.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLowerGreaterThanUpper() {
        new GrayPaintScale(2.0, 1.0);
    }

    /**
     * Test the constructor when lower bound equals upper bound.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLowerEqualsUpper() {
        new GrayPaintScale(1.0, 1.0);
    }

    /**
     * Test getPaint method across valid scale ranges and values.
     */
    @Test
    public void testGetPaint() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);

        Color c0 = (Color) scale.getPaint(0.0);
        Assert.assertEquals(new Color(0, 0, 0), c0);

        Color c1 = (Color) scale.getPaint(1.0);
        Assert.assertEquals(new Color(255, 255, 255), c1);

        Color cMid = (Color) scale.getPaint(0.5);
        Assert.assertEquals(new Color(127, 127, 127), cMid);

        GrayPaintScale customScale = new GrayPaintScale(100.0, 200.0);
        Color cCustom0 = (Color) customScale.getPaint(100.0);
        Assert.assertEquals(new Color(0, 0, 0), cCustom0);

        Color cCustom1 = (Color) customScale.getPaint(200.0);
        Assert.assertEquals(new Color(255, 255, 255), cCustom1);

        Color cCustomMid = (Color) customScale.getPaint(150.0);
        Assert.assertEquals(new Color(127, 127, 127), cCustomMid);
    }

    /**
     * Test the equals method thoroughly.
     */
    @Test
    public void testEquals() {
        GrayPaintScale scale1 = new GrayPaintScale(10.0, 20.0);
        GrayPaintScale scale2 = new GrayPaintScale(10.0, 20.0);

        // Reflexivity
        Assert.assertTrue(scale1.equals(scale1));

        // Symmetry
        Assert.assertTrue(scale1.equals(scale2));
        Assert.assertTrue(scale2.equals(scale1));

        // Null comparison
        Assert.assertFalse(scale1.equals(null));

        // Incompatible class comparison
        Assert.assertFalse(scale1.equals("Some String"));

        // Different lower bound
        GrayPaintScale scaleDiffLower = new GrayPaintScale(5.0, 20.0);
        Assert.assertFalse(scale1.equals(scaleDiffLower));
        Assert.assertFalse(scaleDiffLower.equals(scale1));

        // Different upper bound
        GrayPaintScale scaleDiffUpper = new GrayPaintScale(10.0, 25.0);
        Assert.assertFalse(scale1.equals(scaleDiffUpper));
        Assert.assertFalse(scaleDiffUpper.equals(scale1));
    }

    /**
     * Test that the class implements PublicCloneable and clone creates an equal, independent object.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        GrayPaintScale scale1 = new GrayPaintScale(10.0, 50.0);
        Assert.assertTrue(scale1 instanceof PublicCloneable);

        GrayPaintScale scale2 = (GrayPaintScale) scale1.clone();
        Assert.assertNotSame(scale1, scale2);
        Assert.assertSame(scale1.getClass(), scale2.getClass());
        Assert.assertEquals(scale1, scale2);
        Assert.assertEquals(scale1.getLowerBound(), scale2.getLowerBound(), EPSILON);
        Assert.assertEquals(scale1.getUpperBound(), scale2.getUpperBound(), EPSILON);
    }

    /**
     * Test serialization and deserialization.
     */
    @Test
    public void testSerialization() throws Exception {
        GrayPaintScale scale1 = new GrayPaintScale(-10.0, 100.0);
        GrayPaintScale scale2;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(scale1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        scale2 = (GrayPaintScale) in.readObject();
        in.close();

        Assert.assertEquals(scale1, scale2);
        Assert.assertEquals(scale1.getLowerBound(), scale2.getLowerBound(), EPSILON);
        Assert.assertEquals(scale1.getUpperBound(), scale2.getUpperBound(), EPSILON);
    }
}