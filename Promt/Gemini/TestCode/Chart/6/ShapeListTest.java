package org.jfree.chart.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Tests for the {@link ShapeList} class.
 */
public class ShapeListTest {

    /**
     * Test the default constructor and basic operations.
     */
    @Test
    public void testConstructorAndGetSet() {
        ShapeList list = new ShapeList();
        Assert.assertNull(list.getShape(0));
        Assert.assertEquals(0, list.size());

        Shape shape1 = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
        Shape shape2 = new Ellipse2D.Double(5.0, 5.0, 15.0, 25.0);

        list.setShape(0, shape1);
        Assert.assertEquals(shape1, list.getShape(0));
        Assert.assertEquals(1, list.size());

        list.setShape(2, shape2);
        Assert.assertEquals(shape1, list.getShape(0));
        Assert.assertNull(list.getShape(1));
        Assert.assertEquals(shape2, list.getShape(2));
        Assert.assertEquals(3, list.size());

        // Overwrite existing shape
        Shape shape3 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        list.setShape(0, shape3);
        Assert.assertEquals(shape3, list.getShape(0));

        // Set null shape
        list.setShape(0, null);
        Assert.assertNull(list.getShape(0));
    }

    /**
     * Test the equals() method across various conditions.
     */
    @Test
    public void testEquals() {
        ShapeList l1 = new ShapeList();
        ShapeList l2 = new ShapeList();

        // Same reference
        Assert.assertTrue(l1.equals(l1));

        // Equal empty lists
        Assert.assertTrue(l1.equals(l2));
        Assert.assertTrue(l2.equals(l1));

        // Comparison with null and non-ShapeList object
        Assert.assertFalse(l1.equals(null));
        Assert.assertFalse(l1.equals("Not a ShapeList"));

        // Different contents
        Shape s1 = new Rectangle(1, 2, 3, 4);
        Shape s2 = new Rectangle(5, 6, 7, 8);

        l1.setShape(0, s1);
        Assert.assertFalse(l1.equals(l2));

        l2.setShape(0, s1);
        Assert.assertTrue(l1.equals(l2));

        l1.setShape(1, s2);
        Assert.assertFalse(l1.equals(l2));

        l2.setShape(1, new Rectangle(1, 2, 3, 4));
        Assert.assertFalse(l1.equals(l2));

        l2.setShape(1, s2);
        Assert.assertTrue(l1.equals(l2));

        // Gap in indices
        l1.setShape(5, s1);
        Assert.assertFalse(l1.equals(l2));

        l2.setShape(5, s1);
        Assert.assertTrue(l1.equals(l2));
    }

    /**
     * Test the hashCode() method.
     */
    @Test
    public void testHashCode() {
        ShapeList l1 = new ShapeList();
        ShapeList l2 = new ShapeList();
        Assert.assertEquals(l1.hashCode(), l2.hashCode());

        Shape s1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        l1.setShape(0, s1);
        l2.setShape(0, s1);
        Assert.assertEquals(l1.hashCode(), l2.hashCode());

        Shape s2 = new Ellipse2D.Double(2.0, 3.0, 4.0, 5.0);
        l1.setShape(3, s2);
        l2.setShape(3, s2);
        Assert.assertEquals(l1.hashCode(), l2.hashCode());
    }

    /**
     * Test the clone() method.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        ShapeList l1 = new ShapeList();
        l1.setShape(0, new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0));
        l1.setShape(2, new Line2D.Double(1.0, 1.0, 2.0, 2.0));

        ShapeList l2 = (ShapeList) l1.clone();

        Assert.assertNotSame(l1, l2);
        Assert.assertSame(l1.getClass(), l2.getClass());
        Assert.assertEquals(l1, l2);

        // Verify independent modification
        l2.setShape(0, new Rectangle2D.Double(99.0, 99.0, 99.0, 99.0));
        Assert.assertFalse(l1.equals(l2));
    }

    /**
     * Test serialization of an empty ShapeList.
     */
    @Test
    public void testSerializationEmpty() throws Exception {
        ShapeList l1 = new ShapeList();
        ShapeList l2 = null;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(l1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        l2 = (ShapeList) in.readObject();
        in.close();

        Assert.assertEquals(l1, l2);
    }

    /**
     * Test serialization with populated and sparse elements.
     */
    @Test
    public void testSerializationPopulated() throws Exception {
        ShapeList l1 = new ShapeList();
        l1.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        l1.setShape(2, new Ellipse2D.Double(10.0, 20.0, 30.0, 40.0));
        l1.setShape(5, new Line2D.Double(0.0, 0.0, 100.0, 100.0));

        ShapeList l2 = null;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(l1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        l2 = (ShapeList) in.readObject();
        in.close();

        Assert.assertEquals(l1, l2);
        Assert.assertEquals(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), l2.getShape(0));
        Assert.assertNull(l2.getShape(1));
        Assert.assertEquals(new Ellipse2D.Double(10.0, 20.0, 30.0, 40.0), l2.getShape(2));
        Assert.assertNull(l2.getShape(3));
        Assert.assertNull(l2.getShape(4));
        Assert.assertEquals(new Line2D.Double(0.0, 0.0, 100.0, 100.0), l2.getShape(5));
    }

    /**
     * Test setting negative index behavior as inherited from AbstractObjectList.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIndexGet() {
        ShapeList list = new ShapeList();
        list.getShape(-1);
    }

    /**
     * Test setting negative index behavior as inherited from AbstractObjectList.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIndexSet() {
        ShapeList list = new ShapeList();
        list.setShape(-1, new Rectangle());
    }
}