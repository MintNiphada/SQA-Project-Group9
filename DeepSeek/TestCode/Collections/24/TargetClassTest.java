package org.apache.commons.collections4.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.BoundedCollection;
import org.junit.Test;

public class UnmodifiableBoundedCollectionTest {

    static class SimpleBoundedCollection<E> extends ArrayList<E> implements BoundedCollection<E> {
        private final int maxSize;
        public SimpleBoundedCollection(int maxSize) {
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

    static class TestDecorator<E> extends AbstractCollectionDecorator<E> {
        public TestDecorator(Collection<E> coll) {
            super(coll);
        }
    }

    @Test
    public void testFirstFactoryWithBoundedCollection() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("a");
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertEquals(1, unmodifiable.size());
        assertFalse(unmodifiable.isEmpty());
        assertEquals(5, unmodifiable.maxSize());
        assertFalse(unmodifiable.isFull());
        boolean exceptionThrown = false;
        try {
            unmodifiable.add("b");
            fail();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFirstFactoryWithNull() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<String>) null);
    }

    @Test
    public void testSecondFactoryWithNull() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecondFactoryWithNonBoundedCollection() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(new ArrayList<String>());
    }

    @Test
    public void testSecondFactoryWithBoundedCollection() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("x");
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) bounded);
        assertEquals(1, unmodifiable.size());
        assertEquals(3, unmodifiable.maxSize());
    }

    @Test
    public void testSecondFactoryWithDecoratedBoundedCollection() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(10);
        Collection<String> decorated = new TestDecorator<String>(bounded);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(decorated);
        assertEquals(10, unmodifiable.maxSize());
        assertFalse(unmodifiable.isFull());
    }

    @Test
    public void testSecondFactoryWithSynchronizedBoundedCollection() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(2);
        Collection<String> sync = SynchronizedCollection.synchronizedCollection(bounded);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(sync);
        assertEquals(2, unmodifiable.maxSize());
    }

    @Test
    public void testSecondFactoryWithDeepDecoratorChain() {
        SimpleBoundedCollection<Integer> bounded = new SimpleBoundedCollection<Integer>(1);
        Collection<Integer> current = bounded;
        for (int i = 0; i < 50; i++) {
            current = new TestDecorator<Integer>(current);
        }
        BoundedCollection<Integer> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(current);
        assertEquals(1, unmodifiable.maxSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecondFactoryWithDeepNonBoundedDecoratorChain() {
        Collection<String> leaf = new ArrayList<String>();
        Collection<String> current = leaf;
        for (int i = 0; i < 1000; i++) {
            current = new TestDecorator<String>(current);
        }
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(current);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecondFactoryWithUndrillableDecorator() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        Collection<String> custom = new ArrayList<String>(bounded);
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(custom);
    }

    @Test
    public void testIteratorIsUnmodifiable() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("one");
        bounded.add("two");
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        Iterator<String> iterator = unmodifiable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("one", iterator.next());
        assertEquals("two", iterator.next());
        try {
            iterator.remove();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmodifiable.add("fail");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmodifiable.addAll(new ArrayList<String>());
   }

    @Test(expected = UnsupportedOperationException.class)
    public void testClearThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("x");
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmodifiable.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("a");
        BoundedCollection<String> unm = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unm.remove("a");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("a");
        BoundedCollection<String> unm = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unm.removeAll(new ArrayList<String>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllThrows() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("a");
        BoundedCollection<String> unm = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unm.retainAll(new ArrayList<String>());
   }

    @Test
    public void testIsFull() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(1);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertFalse(unmodifiable.isFull());
        bounded.add("a");
        assertTrue(unmodifiable.isFull());
    }

    @Test
    public void testMaxSize() {
        SimpleBoundedCollection<Integer> bounded = new SimpleBoundedCollection<Integer>(42);
        BoundedCollection<Integer> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertEquals(42, unmodifiable.maxSize());
    }

    @Test
    public void testDecoratedType() {
        SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        BoundedCollection<String> unmodifiable = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertTrue(unmodifiable instanceof UnmodifiableBoundedCollection);
        UnmodifiableBoundedCollection<String> typed = (UnmodifiableBoundedCollection<String>) unmodifiable;
        assertTrue(typed.decorated() instanceof SimpleBoundedCollection);
    }
}
