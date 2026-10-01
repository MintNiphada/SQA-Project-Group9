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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class SetUniqueListTest {

    @Test
    public void testFactoryMethodWithEmptyList() {
        List<String> rawList = new ArrayList<String>();
        SetUniqueList<String> list = SetUniqueList.setUniqueList(rawList);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testFactoryMethodWithPopulatedListContainingDuplicates() {
        List<String> rawList = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList<String> list = SetUniqueList.setUniqueList(rawList);

        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertTrue(list.contains("C"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethodNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullList() {
        new SetUniqueList<String>(null, new HashSet<String>());
    }

    @Test
    public void testAsSet() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        Set<String> setView = list.asSet();
        Assert.assertEquals(2, setView.size());
        Assert.assertTrue(setView.contains("A"));
        Assert.assertTrue(setView.contains("B"));

        try {
            setView.add("C");
            Assert.fail("Expected UnsupportedOperationException for unmodifiable set view");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAdd() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        Assert.assertTrue(list.add("A"));
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(list.add("A"));
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.add("B"));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testAddAtIndex() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add(0, "A");
        list.add(1, "C");
        list.add(1, "B");

        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));

        // Attempt duplicate insertion
        list.add(0, "B");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testAddAll() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");

        Collection<String> toAdd = Arrays.asList("B", "A", "C", "B", "D");
        boolean changed = list.addAll(toAdd);

        Assert.assertTrue(changed);
        Assert.assertEquals(4, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertEquals("D", list.get(3));

        boolean changedAgain = list.addAll(Arrays.asList("A", "C"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("D");

        Collection<String> toAdd = Arrays.asList("B", "A", "C");
        boolean changed = list.addAll(1, toAdd);

        Assert.assertTrue(changed);
        Assert.assertEquals(4, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertEquals("D", list.get(3));

        boolean changedAgain = list.addAll(1, Arrays.asList("B", "D"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void testSetMethod() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");

        // 1. Setting an entirely new element
        String old = list.set(1, "D");
        Assert.assertEquals("B", old);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("D", list.get(1));
        Assert.assertEquals("C", list.get(2));
        Assert.assertTrue(list.contains("D"));
        Assert.assertFalse(list.contains("B"));

        // 2. Setting an element that is already in the list at another position
        // "C" is currently at index 2. We set index 0 to "C".
        old = list.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("C", list.get(0));
        Assert.assertEquals("D", list.get(1));
        Assert.assertFalse(list.contains("A"));
        Assert.assertTrue(list.contains("C"));
        Assert.assertTrue(list.contains("D"));

        // 3. Setting an element at the same index (pos == index)
        old = list.set(1, "D");
        Assert.assertEquals("D", old);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("C", list.get(0));
        Assert.assertEquals("D", list.get(1));
    }

    @Test
    public void testRemoveObject() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        Assert.assertTrue(list.remove("A"));
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.remove("A"));

        Assert.assertFalse(list.remove("NonExistent"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testRemoveIndex() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        String removed = list.remove(0);
        Assert.assertEquals("A", removed);
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
    }

    @Test
    public void testRemoveAll() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");

        boolean changed = list.removeAll(Arrays.asList("A", "C", "Z"));
        Assert.assertTrue(changed);
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("B"));

        boolean changedAgain = list.removeAll(Arrays.asList("X", "Y"));
        Assert.assertFalse(changedAgain);
    }

    @Test
    public void testRetainAll() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");

        // Retain with all present elements
        boolean changed = list.retainAll(Arrays.asList("A", "B", "C", "D"));
        Assert.assertFalse(changed);
        Assert.assertEquals(3, list.size());

        // Retain subset
        changed = list.retainAll(Arrays.asList("B", "D"));
        Assert.assertTrue(changed);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("B", list.get(0));
        Assert.assertTrue(list.contains("B"));
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.contains("C"));

        // Retain empty/disjoint
        changed = list.retainAll(Arrays.asList("X", "Y"));
        Assert.assertTrue(changed);
        Assert.assertEquals(0, list.size());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testClear() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        list.clear();
        Assert.assertEquals(0, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.contains("B"));
    }

    @Test
    public void testContainsAndContainsAll() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertFalse(list.contains("C"));

        Assert.assertTrue(list.containsAll(Arrays.asList("A", "B")));
        Assert.assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    public void testIterator() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        Iterator<String> it = list.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();

        Assert.assertEquals(1, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertTrue(list.contains("B"));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testListIterator() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        ListIterator<String> lit = list.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("A", lit.next());
        Assert.assertTrue(lit.hasPrevious());
        Assert.assertEquals("A", lit.previous());
        Assert.assertEquals("A", lit.next());

        // Add non-duplicate
        lit.add("C");
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("C"));

        // Add duplicate
        lit.add("B");
        Assert.assertEquals(3, list.size());

        // Next to B
        Assert.assertEquals("B", lit.next());
        lit.remove();
        Assert.assertFalse(list.contains("B"));
        Assert.assertEquals(2, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorSetUnsupported() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.set("B");
    }

    @Test
    public void testListIteratorWithIndex() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator<String> lit = list.listIterator(1);
        Assert.assertEquals("B", lit.next());
    }

    @Test
    public void testSubList() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        List<String> sub = list.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));
        Assert.assertTrue(sub instanceof SetUniqueList);

        // Modify subList and check backing behavior
        sub.add("E");
        Assert.assertEquals(3, sub.size());
        Assert.assertEquals(5, list.size());
        Assert.assertEquals("E", list.get(3));
    }

    @Test
    public void testCreateSetBasedOnListWithDifferentSetTypes() {
        // Test with TreeSet
        SetUniqueList<String> treeSetList = new SetUniqueList<String>(new LinkedList<String>(), new TreeSet<String>());
        treeSetList.add("Z");
        treeSetList.add("Y");
        treeSetList.add("X");

        List<String> sub = treeSetList.subList(0, 2);
        Assert.assertEquals(2, sub.size());
        Assert.assertTrue(sub.contains("Z"));
        Assert.assertTrue(sub.contains("Y"));

        // Test with LinkedHashSet
        SetUniqueList<String> linkedHashSetList = new SetUniqueList<String>(new ArrayList<String>(), new LinkedHashSet<String>());
        linkedHashSetList.add("1");
        linkedHashSetList.add("2");
        List<String> subLinked = linkedHashSetList.subList(0, 1);
        Assert.assertEquals(1, subLinked.size());

        // Test with custom set that lacks default constructor (triggers InstantiationException branch)
        NonDefaultConstructibleSet<String> customSet = new NonDefaultConstructibleSet<String>("test");
        SetUniqueList<String> customList = new SetUniqueList<String>(new ArrayList<String>(), customSet);
        customList.add("item1");
        customList.add("item2");
        List<String> subCustom = customList.subList(0, 1);
        Assert.assertEquals(1, subCustom.size());

        // Test with private constructor (triggers IllegalAccessException branch)
        PrivateConstructorSet<String> privateSet = PrivateConstructorSet.create();
        SetUniqueList<String> privList = new SetUniqueList<String>(new ArrayList<String>(), privateSet);
        privList.add("alpha");
        List<String> subPriv = privList.subList(0, 1);
        Assert.assertEquals(1, subPriv.size());
    }

    @Test
    public void testSerialization() throws Exception {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        SetUniqueList<String> deserialized = (SetUniqueList<String>) ois.readObject();
        ois.close();

        Assert.assertEquals(list.size(), deserialized.size());
        Assert.assertEquals(list, deserialized);
        Assert.assertTrue(deserialized.contains("A"));
        Assert.assertFalse(deserialized.add("A"));
    }

    public static class NonDefaultConstructibleSet<E> extends HashSet<E> {
        private static final long serialVersionUID = 1L;

        public NonDefaultConstructibleSet(String dummy) {
            super();
        }
    }

    public static class PrivateConstructorSet<E> extends HashSet<E> {
        private static final long serialVersionUID = 1L;

        private PrivateConstructorSet() {
            super();
        }

        public static <T> PrivateConstructorSet<T> create() {
            return new PrivateConstructorSet<T>();
        }
    }
}
