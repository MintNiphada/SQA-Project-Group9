package org.apache.commons.collections4.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.BoundedCollection;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link UnmodifiableBoundedCollection}.
 */
public class UnmodifiableBoundedCollectionTest {

    private static class MockBoundedCollection<E> extends ArrayList<E> implements BoundedCollection<E> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        MockBoundedCollection(final int maxSize) {
            super();
            this.maxSize = maxSize;
        }

        MockBoundedCollection(final int maxSize, final Collection<? extends E> coll) {
            super(coll);
            this.maxSize = maxSize;
        }

        @Override
        public boolean isFull() {
            return size() >= maxSize;
        }

        @Override
        public int maxSize() {
            return maxSize;
        }
    }

    private static class SimpleCollectionDecorator<E> extends AbstractCollectionDecorator<E> {
        private static final long serialVersionUID = 1L;

        SimpleCollectionDecorator(final Collection<E> coll) {
            super(coll);
        }
    }

    private MockBoundedCollection<String> boundedColl;
    private BoundedCollection<String> unmodifiableColl;

    @Before
    public void setUp() {
        boundedColl = new MockBoundedCollection<String>(3, Arrays.asList("A", "B"));
        unmodifiableColl = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(boundedColl);
    }

    @Test
    public void testFactoryMethodWithBoundedCollection() {
        final BoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(boundedColl);
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(3, coll.maxSize());
        assertFalse(coll.isFull());
    }

    @Test(expected = NullPointerException.class)
    public void testFactoryMethodWithNullBoundedCollection() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<String>) null);
    }

    @Test
    public void testFactoryMethodWithCollectionDirect() {
        final Collection<String> raw = boundedColl;
        final BoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(raw);
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(3, coll.maxSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethodWithNullCollection() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethodWithNonBoundedCollection() {
        final List<String> list = new ArrayList<String>();
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(list);
    }

    @Test
    public void testFactoryMethodWithAbstractCollectionDecorator() {
        final SimpleCollectionDecorator<String> decorated = new SimpleCollectionDecorator<String>(boundedColl);
        final BoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) decorated);
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(3, coll.maxSize());
    }

    @Test
    public void testFactoryMethodWithSynchronizedCollection() {
        final SynchronizedCollection<String> synced = SynchronizedCollection.synchronizedCollection(boundedColl);
        final BoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) synced);
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(3, coll.maxSize());
    }

    @Test
    public void testFactoryMethodWithDeeplyNestedDecorators() {
        Collection<String> current = boundedColl;
        for (int i = 0; i < 5; i++) {
            current = new SimpleCollectionDecorator<String>(current);
            current = SynchronizedCollection.synchronizedCollection(current);
        }
        final BoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(current);
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(3, coll.maxSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethodWithDecoratedNonBoundedCollection() {
        final SimpleCollectionDecorator<String> decorated = new SimpleCollectionDecorator<String>(new ArrayList<String>());
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) decorated);
    }

    @Test
    public void testMaxSizeAndIsFull() {
        assertEquals(3, unmodifiableColl.maxSize());
        assertFalse(unmodifiableColl.isFull());

        final MockBoundedCollection<String> fullBounded = new MockBoundedCollection<String>(2, Arrays.asList("1", "2"));
        final BoundedCollection<String> fullUnmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(fullBounded);
        assertEquals(2, fullUnmodifiable.maxSize());
        assertTrue(fullUnmodifiable.isFull());
    }

    @Test
    public void testIterator() {
        final Iterator<String> it = unmodifiableColl.iterator();
        assertNotNull(it);
        assertTrue(it instanceof UnmodifiableIterator);
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());

        try {
            it.remove();
            fail("Expected UnsupportedOperationException on iterator remove");
        } catch (final UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdd() {
        unmodifiableColl.add("C");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAll() {
        unmodifiableColl.addAll(Collections.singletonList("C"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        unmodifiableColl.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        unmodifiableColl.remove("A");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll() {
        unmodifiableColl.removeAll(Collections.singletonList("A"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() {
        unmodifiableColl.retainAll(Collections.singletonList("A"));
    }

    @Test
    public void testReadOperations() {
        assertEquals(2, unmodifiableColl.size());
        assertFalse(unmodifiableColl.isEmpty());
        assertTrue(unmodifiableColl.contains("A"));
        assertTrue(unmodifiableColl.contains("B"));
        assertFalse(unmodifiableColl.contains("C"));
        assertTrue(unmodifiableColl.containsAll(Arrays.asList("A", "B")));
        assertFalse(unmodifiableColl.containsAll(Arrays.asList("A", "C")));

        final Object[] array = unmodifiableColl.toArray();
        assertEquals(2, array.length);
        assertEquals("A", array[0]);
        assertEquals("B", array[1]);

        final String[] strArray = unmodifiableColl.toArray(new String[0]);
        assertEquals(2, strArray.length);
        assertEquals("A", strArray[0]);
        assertEquals("B", strArray[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(unmodifiableColl);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final BoundedCollection<String> deserialized = (BoundedCollection<String>) ois.readObject();
        ois.close();

        assertEquals(unmodifiableColl.size(), deserialized.size());
        assertEquals(unmodifiableColl.maxSize(), deserialized.maxSize());
        assertEquals(unmodifiableColl.isFull(), deserialized.isFull());
        assertTrue(deserialized.containsAll(unmodifiableColl));
    }
}
