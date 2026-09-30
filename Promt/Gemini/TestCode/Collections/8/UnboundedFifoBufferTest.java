package org.apache.commons.collections.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.BufferUnderflowException;
import org.junit.Assert;
import org.junit.Test;

/**
 * High-coverage unit test suite for {@link UnboundedFifoBuffer}.
 */
public class UnboundedFifoBufferTest {

    @Test
    public void testDefaultConstructor() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());
        Assert.assertEquals(33, buffer.buffer.length);
    }

    @Test
    public void testConstructorWithValidInitialSize() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(1);
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());
        Assert.assertEquals(2, buffer.buffer.length);

        UnboundedFifoBuffer buffer10 = new UnboundedFifoBuffer(10);
        Assert.assertEquals(11, buffer10.buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroSizeThrowsException() {
        new UnboundedFifoBuffer(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeSizeThrowsException() {
        new UnboundedFifoBuffer(-5);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullThrowsException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.add(null);
    }

    @Test
    public void testAddAndSizeLinear() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());

        Assert.assertTrue(buffer.add("A"));
        Assert.assertFalse(buffer.isEmpty());
        Assert.assertEquals(1, buffer.size());

        Assert.assertTrue(buffer.add("B"));
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals("A", buffer.get());
    }

    @Test
    public void testGetAndRemove() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.add("One");
        buffer.add("Two");
        buffer.add("Three");

        Assert.assertEquals("One", buffer.get());
        Assert.assertEquals(3, buffer.size());

        Assert.assertEquals("One", buffer.remove());
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals("Two", buffer.get());

        Assert.assertEquals("Two", buffer.remove());
        Assert.assertEquals(1, buffer.size());
        Assert.assertEquals("Three", buffer.get());

        Assert.assertEquals("Three", buffer.remove());
        Assert.assertEquals(0, buffer.size());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetOnEmptyBufferThrowsException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.get();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testRemoveOnEmptyBufferThrowsException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.remove();
    }

    @Test
    public void testBufferWrapAround() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(2); // capacity = 3
        buffer.add("1");
        buffer.add("2");
        Assert.assertEquals("1", buffer.remove()); // head = 1, tail = 2

        buffer.add("3"); // tail wraps to 0; head = 1, tail = 0 => tail < head
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals("2", buffer.get());

        Assert.assertEquals("2", buffer.remove()); // head = 2, tail = 0
        Assert.assertEquals(1, buffer.size());
        Assert.assertEquals("3", buffer.get());

        Assert.assertEquals("3", buffer.remove()); // head = 0, tail = 0
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testAutomaticResizingWithoutWrapAround() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(1); // buffer length = 2
        Assert.assertEquals(2, buffer.buffer.length);

        buffer.add("A");
        Assert.assertEquals(1, buffer.size());

        // This addition should trigger array resizing from length 2 to (1*2)+1 = 3
        buffer.add("B");
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals(3, buffer.buffer.length);

        // Add more to trigger another resize: (2*2)+1 = 5
        buffer.add("C");
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(5, buffer.buffer.length);

        Assert.assertEquals("A", buffer.remove());
        Assert.assertEquals("B", buffer.remove());
        Assert.assertEquals("C", buffer.remove());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testAutomaticResizingWithWrapAround() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(2); // length = 3
        buffer.add("A");
        buffer.add("B");
        Assert.assertEquals("A", buffer.remove()); // head = 1, tail = 2

        buffer.add("C"); // tail = 0 (wrap-around)
        Assert.assertEquals(2, buffer.size());

        // Now buffer is full (size() + 1 >= buffer.length, 2 + 1 >= 3), adding "D" must resize and unwrap correctly
        buffer.add("D"); // should resize to (2*2)+1 = 5
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(5, buffer.buffer.length);
        Assert.assertEquals(0, buffer.head);
        Assert.assertEquals(3, buffer.tail);

        Assert.assertEquals("B", buffer.remove());
        Assert.assertEquals("C", buffer.remove());
        Assert.assertEquals("D", buffer.remove());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIteratorBasicTraversal() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveBeforeNextThrowsException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        Iterator it = buffer.iterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorDoubleRemoveThrowsException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        buffer.add("B");
        Iterator it = buffer.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testIteratorRemoveFirstElement() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        Assert.assertEquals("A", it.next());
        it.remove(); // removes head element

        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals("B", buffer.get());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveMiddleElement() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        buffer.add("D");

        Iterator it = buffer.iterator();
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());
        it.remove(); // remove 'B'

        Assert.assertEquals(3, buffer.size());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertEquals("D", it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals("A", buffer.remove());
        Assert.assertEquals("C", buffer.remove());
        Assert.assertEquals("D", buffer.remove());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIteratorRemoveLastElement() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        it.remove(); // remove 'C'

        Assert.assertEquals(2, buffer.size());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals("A", buffer.remove());
        Assert.assertEquals("B", buffer.remove());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIteratorRemoveWithBufferWrapAround() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3); // array length = 4
        buffer.add("1");
        buffer.add("2");
        buffer.add("3");
        buffer.remove(); // remove "1", head is at 1, tail is at 3

        buffer.add("4"); // tail wrapped to index 0

        // Buffer contents in order: "2" (index 1), "3" (index 2), "4" (index 3)
        // Array layout: [null (0), "2" (1), "3" (2), "4" (3)]
        Iterator it = buffer.iterator();
        Assert.assertEquals("2", it.next());
        Assert.assertEquals("3", it.next());
        it.remove(); // removes '3' which tests shift across array wrap/decrements

        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals("4", it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals("2", buffer.remove());
        Assert.assertEquals("4", buffer.remove());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIteratorRemoveAllIteratively() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }

        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());
    }

    @Test
    public void testSerializationEmptyBuffer() throws Exception {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(10);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();

        Assert.assertTrue(deserialized.isEmpty());
        Assert.assertEquals(0, deserialized.size());
        deserialized.add("NewItem");
        Assert.assertEquals(1, deserialized.size());
        Assert.assertEquals("NewItem", deserialized.remove());
    }

    @Test
    public void testSerializationPopulatedBuffer() throws Exception {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3);
        buffer.add("First");
        buffer.add("Second");
        buffer.add("Third");
        buffer.remove(); // remove "First"
        buffer.add("Fourth"); // trigger wrap-around

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();

        Assert.assertEquals(3, deserialized.size());
        Assert.assertEquals(0, deserialized.head);
        Assert.assertEquals(3, deserialized.tail);
        Assert.assertEquals(4, deserialized.buffer.length);

        Assert.assertEquals("Second", deserialized.remove());
        Assert.assertEquals("Third", deserialized.remove());
        Assert.assertEquals("Fourth", deserialized.remove());
        Assert.assertTrue(deserialized.isEmpty());
    }

    @Test
    public void testCollectionMethods() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.addAll(Arrays.asList("A", "B", "C"));

        Assert.assertEquals(3, buffer.size());
        Assert.assertTrue(buffer.contains("B"));
        Assert.assertFalse(buffer.contains("Z"));

        Object[] array = buffer.toArray();
        Assert.assertArrayEquals(new Object[]{"A", "B", "C"}, array);

        String[] strArray = (String[]) buffer.toArray(new String[0]);
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, strArray);

        buffer.clear();
        Assert.assertTrue(buffer.isEmpty());
        Assert.assertEquals(0, buffer.size());
    }
}
