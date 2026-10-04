package org.apache.commons.collections.set;

import org.apache.commons.collections.OrderedIterator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ListOrderedSetTest {

    @Test
    public void testDefaultConstructor() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertEquals("[]", set.toString());
    }

    @Test
    public void testFactoryListOrderedSetFromSet() {
        Set<String> hashSet = new HashSet<String>();
        hashSet.add("A");
        hashSet.add("B");
        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(hashSet);
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromNullSet() {
        ListOrderedSet.listOrderedSet((Set<String>) null);
    }

    @Test
    public void testFactoryListOrderedSetFromSetAndList() {
        Set<String> set = new HashSet<String>();
        List<String> list = new ArrayList<String>();
        ListOrderedSet<String> orderedSet = ListOrderedSet.listOrderedSet(set, list);
        assertNotNull(orderedSet);
        assertTrue(orderedSet.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromNullSetAndList() {
        ListOrderedSet.listOrderedSet(null, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromSetAndNullList() {
        ListOrderedSet.listOrderedSet(new HashSet<String>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromNonEmptySetAndList() {
        Set<String> set = new HashSet<String>();
        set.add("A");
        ListOrderedSet.listOrderedSet(set, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromSetAndNonEmptyList() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        ListOrderedSet.listOrderedSet(new HashSet<String>(), list);
    }

    @Test
    public void testFactoryListOrderedSetFromList() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("A");
        list.add("C");
        list.add("B");

        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(list);
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSetFromNullList() {
        ListOrderedSet.listOrderedSet((List<String>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullList() {
        new ListOrderedSet<String>(new HashSet<String>(), null);
    }

    @Test
    public void testAdd() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.add("First"));
        assertFalse(set.add("First"));
        assertTrue(set.add("Second"));
        assertTrue(set.add(null));
        assertFalse(set.add(null));

        assertEquals(3, set.size());
        assertEquals("First", set.get(0));
        assertEquals("Second", set.get(1));
        assertNull(set.get(2));
    }

    @Test
    public void testAddAtIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("C");

        set.add(1, "B");
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));

        // Inserting an already existing item should be a no-op
        set.add(0, "C");
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBounds() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add(1, "A");
    }

    @Test
    public void testAddAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");

        boolean changed = set.addAll(Arrays.asList("B", "C", "D"));
        assertTrue(changed);
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals("D", set.get(3));

        boolean changedAgain = set.addAll(Arrays.asList("A", "B", "C", "D"));
        assertFalse(changedAgain);
        assertEquals(4, set.size());
    }

    @Test
    public void testAddAllAtIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("D");

        boolean changed = set.addAll(1, Arrays.asList("B", "A", "C", "D"));
        assertTrue(changed);
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals("D", set.get(3));

        // Attempt to add all elements that already exist
        boolean changed2 = set.addAll(1, Arrays.asList("A", "B", "C"));
        assertFalse(changed2);
        assertEquals(4, set.size());
    }

    @Test
    public void testRemoveObject() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        assertTrue(set.remove("B"));
        assertFalse(set.remove("NonExistent"));
        assertFalse(set.remove("B"));
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
    }

    @Test
    public void testRemoveIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        Object removed = set.remove(1);
        assertEquals("B", removed);
        assertEquals(2, set.size());
        assertFalse(set.contains("B"));
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexOutOfBounds() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.remove(0);
    }

    @Test
    public void testRemoveAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");

        boolean changed = set.removeAll(Arrays.asList("B", "D", "E"));
        assertTrue(changed);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));

        boolean changedAgain = set.removeAll(Arrays.asList("X", "Y"));
        assertFalse(changedAgain);
    }

    @Test
    public void testRetainAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");

        // Retain with no changes
        boolean changed = set.retainAll(Arrays.asList("A", "B", "C", "D", "E"));
        assertFalse(changed);
        assertEquals(4, set.size());

        // Retain subset
        changed = set.retainAll(Arrays.asList("B", "D", "E"));
        assertTrue(changed);
        assertEquals(2, set.size());
        assertEquals("B", set.get(0));
        assertEquals("D", set.get(1));

        // Retain none -> empty collection
        changed = set.retainAll(Collections.singletonList("Z"));
        assertTrue(changed);
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    public void testClear() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.clear();

        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
        assertEquals(-1, set.indexOf("A"));
    }

    @Test
    public void testIndexOf() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        assertEquals(0, set.indexOf("A"));
        assertEquals(1, set.indexOf("B"));
        assertEquals(2, set.indexOf("C"));
        assertEquals(-1, set.indexOf("Unknown"));
        assertEquals(-1, set.indexOf(null));

        set.add(null);
        assertEquals(3, set.indexOf(null));
    }

    @Test
    public void testAsList() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");

        List<String> list = set.asList();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));

        try {
            list.add("C");
            fail("asList() must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            list.remove(0);
            fail("asList() must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testToArray() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");

        Object[] array = set.toArray();
        assertArrayEquals(new Object[]{"A", "B"}, array);

        String[] typedArray = set.toArray(new String[0]);
        assertArrayEquals(new String[]{"A", "B"}, typedArray);

        String[] largeArray = new String[4];
        largeArray[2] = "Keep";
        largeArray[3] = "Keep";
        String[] returnedArray = set.toArray(largeArray);
        assertSame(largeArray, returnedArray);
        assertEquals("A", returnedArray[0]);
        assertEquals("B", returnedArray[1]);
        assertNull(returnedArray[2]);
    }

    @Test
    public void testToString() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertEquals("[]", set.toString());

        set.add("One");
        set.add("Two");
        assertEquals("[One, Two]", set.toString());
    }

    @Test
    public void testIteratorForwardAndBackward() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        OrderedIterator<String> it = set.iterator();
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());

        assertEquals("A", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        assertEquals("C", it.previous());
        assertEquals("B", it.previous());
        assertEquals("A", it.previous());
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testIteratorRemove() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        OrderedIterator<String> it = set.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());

        it.remove();
        assertEquals(2, set.size());
        assertFalse(set.contains("B"));
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));

        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        assertEquals("C", it.previous());
        it.remove();
        assertEquals(1, set.size());
        assertFalse(set.contains("C"));
        assertEquals("A", set.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        OrderedIterator<String> it = set.iterator();
        it.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextBeyondEnd() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        OrderedIterator<String> it = set.iterator();
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorPreviousBeyondStart() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        OrderedIterator<String> it = set.iterator();
        it.previous();
    }

    @Test
    public void testOrderPreservationWithDuplicates() {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(20); // duplicate

        assertEquals(3, set.size());
        assertEquals(Integer.valueOf(10), set.get(0));
        assertEquals(Integer.valueOf(20), set.get(1));
        assertEquals(Integer.valueOf(30), set.get(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(set);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ListOrderedSet<String> deserialized = (ListOrderedSet<String>) ois.readObject();
        ois.close();

        assertEquals(set.size(), deserialized.size());
        assertEquals(set.get(0), deserialized.get(0));
        assertEquals(set.get(1), deserialized.get(1));
        assertEquals(set.get(2), deserialized.get(2));
        assertEquals(set.toString(), deserialized.toString());
    }

    @Test
    public void testCustomSetAndListBacking() {
        Set<String> customSet = new HashSet<String>();
        List<String> customList = new LinkedList<String>();
        ListOrderedSet<String> set = new ListOrderedSet<String>(customSet, customList);

        set.add("X");
        set.add("Y");

        assertEquals(2, customSet.size());
        assertEquals(2, customList.size());
        assertEquals("X", set.get(0));
        assertEquals("Y", set.get(1));
    }
}
