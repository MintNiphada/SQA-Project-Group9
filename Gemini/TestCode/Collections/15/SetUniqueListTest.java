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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class SetUniqueListTest {

    @Test
    public void testDecorateFactoryNullList() {
        try {
            SetUniqueList.decorate(null);
            Assert.fail("Expected IllegalArgumentException on null list");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("List must not be null", e.getMessage());
        }
    }

    @Test
    public void testDecorateFactoryEmptyList() {
        List base = new ArrayList();
        SetUniqueList list = SetUniqueList.decorate(base);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testDecorateFactoryWithDuplicates() {
        List base = new ArrayList();
        base.add("A");
        base.add("B");
        base.add("A");
        base.add("C");
        base.add("B");

        SetUniqueList list = SetUniqueList.decorate(base);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testConstructorNullSet() {
        try {
            new SetUniqueList(new ArrayList(), null);
            Assert.fail("Expected IllegalArgumentException on null set");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Set must not be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorNullList() {
        try {
            new SetUniqueList(null, new HashSet());
            Assert.fail("Expected IllegalArgumentException on null list");
        } catch (IllegalArgumentException e) {
            // Handled by AbstractListDecorator / super constructor
        }
    }

    @Test
    public void testAdd() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        Assert.assertTrue(list.add("A"));
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("A"));

        // Duplicate add
        Assert.assertFalse(list.add("A"));
        Assert.assertEquals(1, list.size());

        // Add null
        Assert.assertTrue(list.add(null));
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(null));

        // Duplicate null add
        Assert.assertFalse(list.add(null));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testAddAtIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("C");

        list.add(1, "B");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));

        // Attempt to insert duplicate
        list.add(0, "C");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testAddAllCollection() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");

        Collection toAdd = Arrays.asList("B", "A", "C", "B", "D");
        boolean changed = list.addAll(toAdd);
        Assert.assertTrue(changed);
        Assert.assertEquals(4, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertEquals("D", list.get(3));

        // Add identical collection
        boolean changedAgain = list.addAll(Arrays.asList("A", "C"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("E");

        Collection toAdd = Arrays.asList("B", "A", "C", "D");
        boolean changed = list.addAll(1, toAdd);
        Assert.assertTrue(changed);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertEquals("D", list.get(3));
        Assert.assertEquals("E", list.get(4));

        // Attempt addAll with no new items
        boolean changedNoOp = list.addAll(2, Arrays.asList("B", "C"));
        Assert.assertFalse(changedNoOp);
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void testSetMethod() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        // Swap with itself: pos == index
        Object replacedSelf = list.set(1, "B");
        Assert.assertEquals("B", replacedSelf);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("B", list.get(1));

        // Replace with element not in list: pos == -1
        Object replacedNew = list.set(1, "D");
        Assert.assertEquals("B", replacedNew);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("D", list.get(1));

        // Replace with element already at another index (moving element)
        // Current: [A, D, C], set index 0 to "C" (pos = 2)
        Object replacedOld = list.set(0, "C");
        Assert.assertEquals("A", replacedOld);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("C", list.get(0));
        Assert.assertEquals("D", list.get(1));
        Assert.assertFalse(list.contains("A"));
    }

    @Test
    public void testRemoveObject() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        Assert.assertTrue(list.remove("B"));
        Assert.assertEquals(2, list.size());
        Assert.assertFalse(list.contains("B"));

        Assert.assertFalse(list.remove("NonExistent"));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testRemoveIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        Object removed = list.remove(1);
        Assert.assertEquals("B", removed);
        Assert.assertEquals(2, list.size());
        Assert.assertFalse(list.contains("B"));
        Assert.assertEquals("C", list.get(1));
    }

    @Test
    public void testRemoveAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        boolean modified = list.removeAll(Arrays.asList("B", "D", "Z"));
        Assert.assertTrue(modified);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("C"));
        Assert.assertFalse(list.contains("B"));
        Assert.assertFalse(list.contains("D"));

        boolean modified2 = list.removeAll(Collections.singletonList("Z"));
        Assert.assertFalse(modified2);
    }

    @Test
    public void testRetainAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        boolean modified = list.retainAll(Arrays.asList("B", "D", "Z"));
        Assert.assertTrue(modified);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains("B"));
        Assert.assertTrue(list.contains("D"));
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.contains("C"));

        boolean modified2 = list.retainAll(Arrays.asList("B", "D"));
        Assert.assertFalse(modified2);
    }

    @Test
    public void testClear() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.clear();

        Assert.assertEquals(0, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.contains("B"));
    }

    @Test
    public void testContainsAndContainsAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertFalse(list.contains("C"));

        Assert.assertTrue(list.containsAll(Arrays.asList("A", "B")));
        Assert.assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    public void testAsSet() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        Set setView = list.asSet();
        Assert.assertEquals(2, setView.size());
        Assert.assertTrue(setView.contains("A"));
        Assert.assertTrue(setView.contains("B"));

        try {
            setView.add("C");
            Assert.fail("Expected UnsupportedOperationException when mutating unmodifiable set view");
        } catch (UnsupportedOperationException e) {
            // Success
        }
    }

    @Test
    public void testIterator() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator it = list.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());
        it.remove();

        Assert.assertEquals(2, list.size());
        Assert.assertFalse(list.contains("B"));
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testListIterator() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator it = list.listIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());

        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("B", it.previous());

        Assert.assertEquals("B", it.next());
        it.remove();
        Assert.assertEquals(2, list.size());
        Assert.assertFalse(list.contains("B"));

        // Test add via ListIterator for unique element
        it.add("X");
        Assert.assertTrue(list.contains("X"));

        // Test add via ListIterator for duplicate element
        it.add("A");
        Assert.assertEquals(3, list.size()); // A was duplicate, not added again

        // Test set unsupported
        try {
            it.set("Z");
            Assert.fail("Expected UnsupportedOperationException on ListIterator.set");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("ListIterator does not support set", e.getMessage());
        }
    }

    @Test
    public void testListIteratorWithIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator it = list.listIterator(1);
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testSubList() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        List sub = list.subList(1, 3);
        Assert.assertTrue(sub instanceof SetUniqueList);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));

        // Sublist shares underlying set
        Assert.assertFalse(sub.add("A")); // Duplicate in parent set
        Assert.assertTrue(sub.add("E"));
        Assert.assertTrue(list.contains("E"));
    }

    @Test
    public void testSerialization() throws Exception {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();
        ois.close();

        Assert.assertEquals(list.size(), deserialized.size());
        Assert.assertEquals(list.get(0), deserialized.get(0));
        Assert.assertEquals(list.get(1), deserialized.get(1));
        Assert.assertEquals(list.get(2), deserialized.get(2));
        Assert.assertTrue(deserialized.contains("A"));
        Assert.assertFalse(deserialized.add("A"));
    }
}
