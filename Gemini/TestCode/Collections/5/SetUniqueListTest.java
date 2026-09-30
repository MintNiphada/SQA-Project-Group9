package org.apache.commons.collections.list;

import org.junit.Assert;
import org.junit.Before;
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
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class SetUniqueListTest {

    private List<Object> decoratedList;
    private SetUniqueList uniqueList;

    @Before
    public void setUp() {
        decoratedList = new ArrayList<Object>();
        uniqueList = SetUniqueList.decorate(decoratedList);
    }

    @Test
    public void testDecorateNullList() {
        try {
            SetUniqueList.decorate(null);
            Assert.fail("Expected IllegalArgumentException when decorating null list");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("List must not be null", e.getMessage());
        }
    }

    @Test
    public void testDecorateEmptyList() {
        List<Object> empty = new ArrayList<Object>();
        SetUniqueList list = SetUniqueList.decorate(empty);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testDecorateNonEmptyListWithDuplicates() {
        List<Object> initial = new ArrayList<Object>(Arrays.asList("A", "B", "A", "C", "B", "D"));
        SetUniqueList list = SetUniqueList.decorate(initial);

        Assert.assertEquals(4, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertEquals("D", list.get(3));

        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertTrue(list.contains("C"));
        Assert.assertTrue(list.contains("D"));
        Assert.assertFalse(list.contains("E"));
    }

    @Test
    public void testConstructorNullSet() {
        try {
            new SetUniqueList(new ArrayList<Object>(), null);
            Assert.fail("Expected IllegalArgumentException when set is null");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Set must not be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorNullList() {
        try {
            new SetUniqueList(null, new HashSet<Object>());
            Assert.fail("Expected IllegalArgumentException when list is null");
        } catch (IllegalArgumentException e) {
            // expected from AbstractCollectionDecorator
        }
    }

    @Test
    public void testAsSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        Set<Object> setView = uniqueList.asSet();

        Assert.assertEquals(2, setView.size());
        Assert.assertTrue(setView.contains("A"));
        Assert.assertTrue(setView.contains("B"));

        try {
            setView.add("C");
            Assert.fail("asSet() should return an unmodifiable set");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testAdd() {
        Assert.assertTrue(uniqueList.add("A"));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertTrue(uniqueList.contains("A"));

        // Duplicate add
        Assert.assertFalse(uniqueList.add("A"));
        Assert.assertEquals(1, uniqueList.size());

        Assert.assertTrue(uniqueList.add("B"));
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testAddAtIndex() {
        uniqueList.add(0, "A");
        uniqueList.add(1, "C");
        uniqueList.add(1, "B");

        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));

        // Attempt to insert duplicate at index
        uniqueList.add(0, "B");
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
    }

    @Test
    public void testAddAll() {
        List<Object> coll = Arrays.asList("A", "B", "A", "C");
        Assert.assertTrue(uniqueList.addAll(coll));
        Assert.assertEquals(3, uniqueList.size());

        // Adding already contained elements
        Assert.assertFalse(uniqueList.addAll(Arrays.asList("A", "B", "C")));
        Assert.assertEquals(3, uniqueList.size());

        // Adding partial duplicates
        Assert.assertTrue(uniqueList.addAll(Arrays.asList("C", "D", "E")));
        Assert.assertEquals(5, uniqueList.size());
        Assert.assertTrue(uniqueList.contains("D"));
        Assert.assertTrue(uniqueList.contains("E"));
    }

    @Test
    public void testAddAllAtIndex() {
        uniqueList.add("A");
        uniqueList.add("D");

        List<Object> toInsert = Arrays.asList("B", "C", "A");
        // Note: SetUniqueList.addAll(index, coll) delegates each element to add(next),
        // adding unique elements to the end through add(Object).
        Assert.assertTrue(uniqueList.addAll(1, toInsert));
        Assert.assertEquals(4, uniqueList.size());
        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertTrue(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("C"));
        Assert.assertTrue(uniqueList.contains("D"));

        Assert.assertFalse(uniqueList.addAll(0, Arrays.asList("A", "B")));
    }

    @Test
    public void testSetNotPresent() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Object previous = uniqueList.set(1, "X");
        Assert.assertEquals("B", previous);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("X", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("X"));
    }

    @Test
    public void testSetSameIndex() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Object previous = uniqueList.set(1, "B");
        Assert.assertEquals("B", previous);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testSetDuplicateOtherIndex() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        // Set index 1 ("B") to "D" (which is at index 3)
        // pos is 3. pos != -1 and pos != index (3 != 1)
        // super.set(1, "D") replaces index 1 with "D", returning "B"
        // super.remove(3) removes the duplicate "D" at index 3
        // set.remove("B") removes "B" from set
        Object previous = uniqueList.set(1, "D");
        Assert.assertEquals("B", previous);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("D", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("D"));
    }

    @Test
    public void testRemoveObject() {
        uniqueList.add("A");
        uniqueList.add("B");

        Assert.assertTrue(uniqueList.remove("A"));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));

        Assert.assertFalse(uniqueList.remove("NonExistent"));
        Assert.assertEquals(1, uniqueList.size());
    }

    @Test
    public void testRemoveIndex() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Object removed = uniqueList.remove(1);
        Assert.assertEquals("B", removed);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("C", uniqueList.get(1));
    }

    @Test
    public void testRemoveAll() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        Assert.assertTrue(uniqueList.removeAll(Arrays.asList("B", "D", "Z")));
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertFalse(uniqueList.contains("D"));
        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertTrue(uniqueList.contains("C"));

        Assert.assertFalse(uniqueList.removeAll(Collections.singletonList("NonExistent")));
    }

    @Test
    public void testRetainAll() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Assert.assertTrue(uniqueList.retainAll(Arrays.asList("B", "C", "D")));
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertTrue(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("C"));

        Assert.assertFalse(uniqueList.retainAll(Arrays.asList("B", "C")));
    }

    @Test
    public void testClear() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.clear();

        Assert.assertEquals(0, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.isEmpty());
    }

    @Test
    public void testContainsAndContainsAll() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("Z"));

        Assert.assertTrue(uniqueList.containsAll(Arrays.asList("A", "C")));
        Assert.assertFalse(uniqueList.containsAll(Arrays.asList("A", "Z")));
    }

    @Test
    public void testIterator() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Iterator<Object> it = uniqueList.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        it.remove();

        Assert.assertEquals(2, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("C", uniqueList.get(1));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test
    public void testListIterator() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        ListIterator<Object> it = uniqueList.listIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertFalse(it.hasPrevious());
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("B", it.previous());
        Assert.assertEquals("B", it.next());

        it.remove();
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertEquals(2, uniqueList.size());

        // Add element via listIterator
        it.add("D");
        Assert.assertTrue(uniqueList.contains("D"));

        // Add duplicate via listIterator - should be ignored
        it.add("A");
        Assert.assertEquals(3, uniqueList.size()); // A, D, C

        // Set via listIterator is unsupported
        try {
            it.set("Z");
            Assert.fail("ListIterator.set should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("ListIterator does not support set", e.getMessage());
        }
    }

    @Test
    public void testListIteratorWithIndex() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        ListIterator<Object> it = uniqueList.listIterator(1);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("B", it.previous());
        Assert.assertEquals("A", it.previous());
    }

    @Test
    public void testSubList() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        List<Object> sub = uniqueList.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));

        // Sublist is a SetUniqueList
        Assert.assertTrue(sub instanceof SetUniqueList);
        Assert.assertFalse(sub.add("A")); // "A" is already in the underlying set
        Assert.assertEquals(2, sub.size());
    }

    @Test
    public void testSerialization() throws Exception {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(uniqueList);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertTrue(deserialized instanceof SetUniqueList);
        SetUniqueList deserializedList = (SetUniqueList) deserialized;
        Assert.assertEquals(3, deserializedList.size());
        Assert.assertEquals("A", deserializedList.get(0));
        Assert.assertEquals("B", deserializedList.get(1));
        Assert.assertEquals("C", deserializedList.get(2));
        Assert.assertTrue(deserializedList.contains("A"));

        // Ensure uniqueness constraints still hold after deserialization
        Assert.assertFalse(deserializedList.add("A"));
        Assert.assertTrue(deserializedList.add("D"));
        Assert.assertEquals(4, deserializedList.size());
    }
}
