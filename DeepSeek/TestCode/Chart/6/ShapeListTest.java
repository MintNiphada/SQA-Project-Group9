package org.jfree.chart.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShapeListTest {

    @Test
    public void testDefaultConstructor() {
        ShapeList list = new ShapeList();
        assertNotNull(list);
    }

    @Test
    public void testGetAndSetShape() {
        ShapeList list = new ShapeList();
        Shape s1 = new Rectangle(1, 2, 3, 4);
        list.setShape(0, s1);
        assertEquals(s1, list.getShape(0));
        list.setShape(1, null);
        assertNull(list.getShape(1));
        Shape s2 = new Rectangle(5, 6, 7, 8);
        list.setShape(0, s2);
        assertEquals(s2, list.getShape(0));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        ShapeList list = new ShapeList();
        Shape s1 = new Rectangle(1, 2, 3, 4);
        list.setShape(0, s1);
        ShapeList clone = (ShapeList) list.clone();
        assertNotSame(list, clone);
        assertEquals(list, clone);
        assertSame(s1, clone.getShape(0));
        clone.setShape(0, new Rectangle(10, 10, 10, 10));
        assertNotEquals(list, clone);
    }

    @Test
    public void testEqualsSameObject() {
        ShapeList list = new ShapeList();
        assertTrue(list.equals(list));
    }

    @Test
    public void testEqualsNull() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals("SomeString"));
    }

    @Test
    public void testEqualsEmptyLists() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        assertTrue(list1.equals(list2));
        assertTrue(list2.equals(list1));
    }

    @Test
    public void testEqualsEqualContent() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(1, 2, 3, 4));
        list1.setShape(1, null);
        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle(1, 2, 3, 4));
        list2.setShape(1, null);
        assertTrue(list1.equals(list2));
    }

    @Test
    public void testEqualsDifferentContent() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(1, 2, 3, 4));
        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle(5, 6, 7, 8));
        assertFalse(list1.equals(list2));
    }

    @Test
    public void testHashCode() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(1, 2, 3, 4));
        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle(1, 2, 3, 4));
        assertEquals(list1.hashCode(), list2.hashCode());
        list2.setShape(0, new Rectangle(5, 6, 7, 8));
        assertNotEquals(list1.hashCode(), list2.hashCode());
    }

    @Test
    public void testSerializationRoundTrip() throws Exception {
        ShapeList list = new ShapeList();
        Shape s1 = new Rectangle(1, 2, 3, 4);
        Shape s2 = new Rectangle(5, 6, 7, 8);
        list.setShape(0, s1);
        list.setShape(1, null);
        list.setShape(2, s2);

        byte[] data = serialize(list);
        ShapeList deserialized = (ShapeList) deserialize(data);

        assertNotSame(list, deserialized);
        assertEquals(list, deserialized);
        assertEquals(s1, deserialized.getShape(0));
        assertNull(deserialized.getShape(1));
        assertEquals(s2, deserialized.getShape(2));
    }

    @Test
    public void testSerializationAllNullShapes() throws Exception {
        ShapeList list = new ShapeList();
        list.setShape(0, null);
        list.setShape(1, null);
        list.setShape(2, null);

        byte[] data = serialize(list);
        ShapeList deserialized = (ShapeList) deserialize(data);

        assertEquals(list, deserialized);
        assertNull(deserialized.getShape(0));
        assertNull(deserialized.getShape(1));
        assertNull(deserialized.getShape(2));
    }

    private byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        return bos.toByteArray();
    }

    private Object deserialize(byte[] data) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
