package org.apache.commons.collections.list;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SetUniqueListTest {

    @Test
    public void testDecorateEmptyList() {
        List<Object> list = new ArrayList<Object>();
        SetUniqueList uniqueList = SetUniqueList.decorate(list);
        Assert.assertNotNull(uniqueList);
        Assert.assertEquals(0, uniqueList.size());
        Assert.assertTrue(uniqueList.isEmpty());
    }

    @Test
    public void testDecorateNonEmptyListWithDuplicates() {
        List<Object> list = new ArrayList<Object>();
        list.add("A");
        list.add("B");
        list.add("A");
        list.add("C");
        list.add("B");

        SetUniqueList uniqueList = SetUniqueList.decorate(list);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new SetUniqueList(new ArrayList<Object>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullList() {
        new SetUniqueList(null, new HashSet<Object>());
    }

    @Test
    public void testAsSet() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        Set set = uniqueList.asSet();
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        try {
            set.add("C");
            Assert.fail("Expected UnsupportedOperationException when modifying unmodifiable set view");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAdd() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        Assert.assertTrue(uniqueList.add("A"));
        Assert.assertFalse(uniqueList.add("A"));
        Assert.assertTrue(uniqueList.add("B"));
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
    }

    @Test
    public void testAddAtIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add(0, "A");
        uniqueList.add(1, "B");
        uniqueList.add(1, "C");
        // list is [A, C, B]
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("C", uniqueList.get(1));
        Assert.assertEquals("B", uniqueList.get(2));

        // Attempting to add duplicate at index
        uniqueList.add(0, "C");
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
    }

    @Test
    public void testAddAll() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");

        Collection<String> toAdd = Arrays.asList("B", "A", "C", "B");
        boolean changed = uniqueList.addAll(toAdd);
        Assert.assertTrue(changed);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));

        boolean changedAgain = uniqueList.addAll(Arrays.asList("A", "B"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(3, uniqueList.size());
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("D");

        Collection<String> toAdd = Arrays.asList("B", "A", "C");
        boolean changed = uniqueList.addAll(1, toAdd);
        Assert.assertTrue(changed);
        // "A" is duplicate, so "B" inserted at 1, "C" inserted at 2, "D" shifted to 3
        Assert.assertEquals(4, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
        Assert.assertEquals("D", uniqueList.get(3));
    }

    @Test
    public void testSetUniqueElement() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Object old = uniqueList.set(1, "X");
        Assert.assertEquals("B", old);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("X", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
        Assert.assertFalse(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("X"));
    }

    @Test
    public void testSetSameElementSameIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        Object old = uniqueList.set(1, "B");
        Assert.assertEquals("B", old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
    }

    @Test
    public void testSetDuplicateElementDifferentIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        // Set element at index 0 to "C", which exists at index 2
        Object old = uniqueList.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals("C", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertTrue(uniqueList.contains("C"));
        Assert.assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testRemoveByObject() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        Assert.assertTrue(uniqueList.remove("A"));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));

        Assert.assertFalse(uniqueList.remove("NonExistent"));
    }

    @Test
    public void testRemoveByIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
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
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        boolean changed = uniqueList.removeAll(Arrays.asList("A", "C", "Z"));
        Assert.assertTrue(changed);
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertEquals("B", uniqueList.get(0));
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("C"));

        boolean changedAgain = uniqueList.removeAll(Arrays.asList("X", "Y"));
        Assert.assertFalse(changedAgain);
    }

    @Test
    public void testRetainAll() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        boolean changed = uniqueList.retainAll(Arrays.asList("B", "C", "D"));
        Assert.assertTrue(changed);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertTrue(uniqueList.contains("B"));
        Assert.assertTrue(uniqueList.contains("C"));

        boolean changedAgain = uniqueList.retainAll(Arrays.asList("B", "C"));
        Assert.assertFalse(changedAgain);
    }

    @Test
    public void testClear() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        uniqueList.clear();
        Assert.assertEquals(0, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("B"));
    }

    @Test
    public void testContainsAndContainsAll() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("C"));

        Assert.assertTrue(uniqueList.containsAll(Arrays.asList("A", "B")));
        Assert.assertFalse(uniqueList.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    public void testSubList() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        List sub = uniqueList.subList(1, 3);
        Assert.assertTrue(sub instanceof SetUniqueList);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));
    }

    @Test
    public void testIterator() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Iterator it = uniqueList.iterator();
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
    }

    @Test
    public void testListIterator() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        ListIterator lit = uniqueList.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("A", lit.next());
        Assert.assertTrue(lit.hasPrevious());
        Assert.assertEquals("A", lit.previous());
        Assert.assertEquals("A", lit.next());
        Assert.assertEquals("B", lit.next());

        lit.remove();
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertFalse(uniqueList.contains("B"));

        lit.add("C");
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertTrue(uniqueList.contains("C"));

        lit.add("A"); // Duplicate add via ListIterator
        Assert.assertEquals(2, uniqueList.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorSetUnsupported() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        ListIterator lit = uniqueList.listIterator();
        lit.next();
        lit.set("Z");
    }

    @Test
    public void testListIteratorWithIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        ListIterator lit = uniqueList.listIterator(1);
        Assert.assertEquals("B", lit.next());
        Assert.assertEquals("C", lit.next());
        Assert.assertFalse(lit.hasNext());
    }

    @Test
    public void testSerialization() throws Exception {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<Object>());
        uniqueList.add("A");
        uniqueList.add("B");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(uniqueList);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();

        Assert.assertEquals(2, deserialized.size());
        Assert.assertTrue(deserialized.contains("A"));
        Assert.assertTrue(deserialized.contains("B"));
        Assert.assertFalse(deserialized.add("A"));
    }
}
