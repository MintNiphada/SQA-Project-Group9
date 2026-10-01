package org.apache.commons.collections.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.BufferUnderflowException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for UnboundedFifoBuffer.
 */
public class UnboundedFifoBufferTest {

    // ======== Constructor Tests =======

    @Test
    public void testConstructorDefault() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        assertEquals(0, b.size());
        assertTrue(b.isEmpty());
    }

    @Test
    public void testConstructorPositiveSize() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(5);
        assertEquals(0, b.size());
        assertTrue(b.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroSize() {
        new UnboundedFifoBuffer(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeSize() {
        new UnboundedFifoBuffer(-1);
    }

    // ======== Add/Get/Remove Basic =======

    @Test
    public void testAddAndGet() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        assertTrue(b.add("A"));
        assertTrue(b.add("B"));
        assertEquals(2, b.size());
        assertEquals("A", b.get());
        assertEquals("A", b.get()); // get does not remove
        assertEquals(2, b.size());
    }

    @Test
    public void testAddNull() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        try {
            b.add(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetEmpty() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.get();
    }

    @Test
    public void testRemoveFromEmpty() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        try {
            b.remove();
            fail("Expected BufferUnderflowException");
        } catch (BufferUnderflowException e) {
            // expected
        }
    }

    @Test
    public void testRemoveOrder() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("1");
        b.add("2");
        b.add("3");
        assertEquals("1", b.remove());
        assertEquals(2, b.size());
        assertEquals("2", b.remove());
        assertEquals(1, b.size());
        assertEquals("3", b.remove());
        assertEquals(0, b.size());
        assertTrue(b.isEmpty());
    }

    @Test
    public void testSize() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        assertEquals(0, b.size());
        b.add("a");
        assertEquals(1, b.size());
        b.add("b");
        assertEquals(2, b.size());
        b.remove();
        assertEquals(1, b.size());
        b.remove();
        assertEquals(0, b.size());
    }

    @Test
    public void testIsEmpty() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        assertTrue(b.isEmpty());
        b.add("x");
        assertFalse(b.isEmpty());
        b.remove();
        assertTrue(b.isEmpty());
    }

    // ======== Resize behaviour =======

    @Test
    public void testResizeAfterExactCapacity() {
        // initial size 2, buffer length = 3
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(2);
        b.add("A");
        b.add("B");
        assertEquals(2, b.size());
        // next add triggers resize because size()+1=3 >= buffer.length=3
        b.add("C");
        assertEquals(3, b.size());
        assertEquals("A", b.get());
        assertEquals("A", b.remove());
        assertEquals("B", b.remove());
        assertEquals("C", b.remove());
        assertTrue(b.isEmpty());
    }

    @Test
    public void testResizeWithWrapedBulffer() {
        // initial size 3, buffer length 4
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(3);
        b.add("a");
        b.add("b");
        b.add("c");
        // remove two to create a gap at the beginning
        assertEquals("a", b.remove());
        assertEquals("b", b.remove());
        // now head=2, tail=3, size=1 (only "c")
        // add two more elements to wrap tail around
        b.add("d"); // buffer[3] = d, tail = 0 (wrap)
        b.add("e"); // size = 3 now, need resize? size()+1=4, buffer.length=4 => >=, triggers resize
        assertEquals(3, b.size());
        // after resize, order should be c, d, e
        assertEquals("c", b.remove());
        assertEquals("d", b.remove());
        assertEquals("e", b.remove());
        assertTrue(b.isEmpty());
    }

    @Test
    public void testSizeWhenWraped() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(3);
        b.add("1");
        b.add("2");
        b.add("3");
        b.remove(); // head=1, tail=3, size=2
        assertEquals(2, b.size());
        b.add("4"); // tail wraps to 0 (tail=0), now head=1, tail=0, size = 4-1+0 = 3
        assertEquals(3, b.size());
        assertEquals("2", b.remove());
        assertEquals("3", b.remove());
        assertEquals("4", b.remove());
    }

    // ======== Iterator Tests =======

    @Test
    public void testIteratorHasNext() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        Iterator it = b.iterator();
        assertFalse(it.hasNext());
        b.add("x");
        it = b.iterator();
        assertTrue(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextWithoutElement() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.iterator().next();
    }

    @Test
    public void testIteratorOrder() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("1");
        b.add("2");
        b.add("3");
        Iterator it = b.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertEquals("2", it.next());
        assertEquals("3", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveHead() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("a");
        b.add("b");
        Iterator it = b.iterator();
        it.next();
        it.remove();
        assertEquals(1, b.size());
        assertEquals("b", b.get());
        // calling remove again without next should throw
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("a");
        Iterator it = b.iterator();
        it.remove(); // no next() called
    }

    @Test
    public void testIteratorRemoveMiddle() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("1");
        b.add("2");
        b.add("3");
        Iterator it = b.iterator();
        assertEquals("1", it.next());
        assertEquals("2", it.next());
        it.remove(); // removes "2"
        assertEquals(2, b.size());
        // after remove, the iterator's index was adjusted, next should be "3"
        assertTrue(it.hasNext());
        assertEquals("3", it.next());
        assertFalse(it.hasNext());
        // buffer content should be "1", "3" in order
        Iterator verify = b.iterator();
        assertEquals("1", verify.next());
        assertEquals("3", verify.next());
    }

    @Test
    public void testIteratorRemoveMiddleThenNext() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("x");
        b.add("y");
        b.add("z");
        b.add("w");
        Iterator it = b.iterator();
        it.next(); // x
        it.next(); // y
        it.remove(); // remove y
        assertEquals("z", it.next()); // should be z after shifting
        assertEquals("w", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveMultipleTimes() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        for (int i = 0; i < 5; i++) {
            b.add("val" + i);
        }
        Iterator it = b.iterator();
        // remove val0, val2, val4
        it.next(); // val0
        it.remove();
        it.next(); // val1 (now head)
        it.next(); // val2
        it.remove();
        it.next(); // val3
        it.next(); // val4
        it.remove();
        assertEquals(2, b.size());
        assertEquals("val1", b.remove());
        assertEquals("val3", b.remove());
    }

    // ======== Serialization =======

    @Test
    public void testSerialization() throws Exception {
        UnboundedFifoBuffer original = new UnboundedFifoBuffer();
        original.add("a");
        original.add("b");
        original.add("c");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(original);
        out.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        UnboundedFifoBuffer restored = (UnboundedFifoBuffer) in.readObject();
        in.close();

        assertEquals(original.size(), restored.size());
        assertEquals("a", restored.remove());
        assertEquals("b", restored.remove());
        assertEquals("c", restored.remove());
    }

    // ======== Defensive branch in remove() =======

    @Test
    public void testRemoveWhenHeadElementIsNull() throws Exception {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("x");
        b.add("y");
        // use reflection to set buffer[head] = null
        Field bufferField = UnboundedFifoBuffer.class.getDeclaredField("buffer");
        bufferField.setAccessible(true);
        Object[] internalBuffer = (Object[]) bufferField.get(b);
        // head is 0 at this point
        internalBuffer[0] = null;
        // now call remove: the element is null, so the if block is skipped
        Object removed = b.remove(); // this calls isEmpty -> false, element = null
        // assert that removed is null and head didn't change (should still be 0)
        assertNull(removed);
        // head should still be 0, so size() remains 2? Actually size() computed from head/tail, not data.
        assertEquals(2, b.size());
        // But internal state is inconsistent. That's fine for coverage.
        // Next remove should work normally: head still 0, but buffer[0] is null, but element is null again? 
        // The actual element "x" is lost. We wont't rely on further correct behavior.
    }

    // ======== Additional corner cases =======

    @Test
    public void testAddManyToForceResizeMultipleTimes() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(2);
        for (int i = 0; i < 20; i++) {
            b.add(Integer.toString(i));
        }
        assertEquals(20, b.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(Integer.toString(i), b.remove());
        }
    }

    @Test
    public void testIteratorOnWapedBuffer() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer(3);
        b.add("a");
        b.add("b");
        b.add("c");
        b.remove(); // head=1
        b.remove(); // head=2
        b.add("d"); // tail=3 then wraps to 0
        b.add("e"); // tail=1, size = 4 - 2 + 1 = 3? Actually head=2, tail=1 -> size = 4-2+1=3. elements: c,d,e
        Iterator it = b.iterator();
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertEquals("d", it.next());
        assertEquals("e", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetDoesNotRemove() {
        UnboundedFifoBuffer b = new UnboundedFifoBuffer();
        b.add("x");
        assertEquals("x", b.get());
        assertEquals(1, b.size());
        assertEquals("x", b.get());
    }
}
