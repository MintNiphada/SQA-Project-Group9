package org.apache.commons.collections4.list;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class SetUniqueListTest {

    @Test
    public void testFactoryMethodWithEmptyList() {
        final List<String> list = new ArrayList<String>();
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(list);
        Assert.assertNotNull(uniqueList);
        Assert.assertTrue(uniqueList.isEmpty());
        Assert.assertEquals(0, uniqueList.size());
    }

    @Test
    public void testFactoryMethodWithDuplicates() {
        final List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(list);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
        Assert.assertEquals("B", uniqueList.get(1));
        Assert.assertEquals("C", uniqueList.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryMethodNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullList() {
        new SetUniqueList<String>(null, new HashSet<String>());
    }

    @Test
    public void testAsSet() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        final Set<String> set = list.asSet();
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        try {
            set.add("C");
            Assert.fail("asSet() should return an unmodifiable set");
        } catch (final UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testAdd() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        Assert.assertTrue(list.add("A"));
        Assert.assertFalse(list.add("A"));
        Assert.assertTrue(list.add("B"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
    }

    @Test
    public void testAddAtIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "C")));
        list.add(1, "B");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("B", list.get(1));

        // Duplicate insertion at index should be ignored
        list.add(0, "B");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testAddAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        final boolean changed = list.addAll(Arrays.asList("B", "C", "D", "A", "E"));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), list);

        final boolean changedAgain = list.addAll(Arrays.asList("A", "C"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void testAddAllAtIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "D")));
        final boolean changed = list.addAll(1, Arrays.asList("B", "C", "D", "A", "E"));
        Assert.assertTrue(changed);
        Assert.assertEquals(Arrays.asList("A", "B", "C", "E", "D"), list);

        final boolean noChange = list.addAll(0, Arrays.asList("A", "B"));
        Assert.assertFalse(noChange);
        Assert.assertEquals(Arrays.asList("A", "B", "C", "E", "D"), list);
    }

    @Test
    public void testSet() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));

        // 1. Setting an object not in the list (pos == -1)
        final String old1 = list.set(1, "X");
        Assert.assertEquals("B", old1);
        Assert.assertEquals(Arrays.asList("A", "X", "C", "D"), list);
        Assert.assertTrue(list.contains("X"));
        Assert.assertFalse(list.contains("B"));

        // 2. Setting an object at its current index (pos == index)
        final String old2 = list.set(1, "X");
        Assert.assertEquals("X", old2);
        Assert.assertEquals(Arrays.asList("A", "X", "C", "D"), list);

        // 3. Setting an object already in the list at a different index (pos != -1 && pos != index)
        // Set "D" at index 0 -> "A" is replaced by "D", and old "D" at index 3 is removed
        final String old3 = list.set(0, "D");
        Assert.assertEquals("A", old3);
        Assert.assertEquals(Arrays.asList("D", "X", "C"), list);
        Assert.assertEquals(3, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertTrue(list.contains("D"));
    }

    @Test
    public void testRemoveObject() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        Assert.assertTrue(list.remove("B"));
        Assert.assertFalse(list.remove("B"));
        Assert.assertEquals(Arrays.asList("A", "C"), list);
        Assert.assertFalse(list.contains("B"));

        Assert.assertFalse(list.remove("NonExistent"));
    }

    @Test
    public void testRemoveIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final String removed = list.remove(1);
        Assert.assertEquals("B", removed);
        Assert.assertEquals(Arrays.asList("A", "C"), list);
        Assert.assertFalse(list.contains("B"));
    }

    @Test
    public void testRemoveAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        final boolean changed = list.removeAll(Arrays.asList("B", "D", "Z"));
        Assert.assertTrue(changed);
        Assert.assertEquals(Arrays.asList("A", "C"), list);
        Assert.assertFalse(list.contains("B"));
        Assert.assertFalse(list.contains("D"));

        final boolean noChange = list.removeAll(Arrays.asList("X", "Y"));
        Assert.assertFalse(noChange);
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testRetainAllAllRetained() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final boolean changed = list.retainAll(Arrays.asList("A", "B", "C", "D"));
        Assert.assertFalse(changed);
        Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test
    public void testRetainAllNoneRetained() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final boolean changed = list.retainAll(Arrays.asList("X", "Y"));
        Assert.assertTrue(changed);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
        Assert.assertFalse(list.contains("A"));
    }

    @Test
    public void testRetainAllPartial() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        final boolean changed = list.retainAll(Arrays.asList("B", "D", "X"));
        Assert.assertTrue(changed);
        Assert.assertEquals(Arrays.asList("B", "D"), list);
        Assert.assertFalse(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertFalse(list.contains("C"));
        Assert.assertTrue(list.contains("D"));
    }

    @Test
    public void testClear() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        list.clear();
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
        Assert.assertFalse(list.contains("A"));
        Assert.assertFalse(list.contains("B"));
    }

    @Test
    public void testContainsAndContainsAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        Assert.assertTrue(list.contains("A"));
        Assert.assertFalse(list.contains("Z"));

        Assert.assertTrue(list.containsAll(Arrays.asList("A", "B")));
        Assert.assertFalse(list.containsAll(Arrays.asList("A", "Z")));
    }

    @Test
    public void testIterator() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final Iterator<String> it = list.iterator();

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertEquals(Arrays.asList("B", "C"), list);
        Assert.assertFalse(list.contains("A"));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testListIterator() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final ListIterator<String> it = list.listIterator();

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("A", it.previous());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertEquals(Arrays.asList("B", "C"), list);
        Assert.assertFalse(list.contains("A"));

        it.add("Z");
        Assert.assertTrue(list.contains("Z"));
        Assert.assertEquals(Arrays.asList("Z", "B", "C"), list);

        // Adding existing element via iterator should do nothing
        it.add("B");
        Assert.assertEquals(Arrays.asList("Z", "B", "C"), list);

        try {
            it.set("W");
            Assert.fail("ListIterator.set() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testListIteratorWithIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final ListIterator<String> it = list.listIterator(1);

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("B", it.previous());
        it.remove();
        Assert.assertFalse(list.contains("B"));
        Assert.assertEquals(Arrays.asList("A", "C"), list);
    }

    @Test
    public void testSubList() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        final List<String> sub = list.subList(1, 3);
        Assert.assertEquals(Arrays.asList("B", "C"), sub);
        Assert.assertTrue(sub instanceof SetUniqueList);

        final SetUniqueList<String> uniqueSub = (SetUniqueList<String>) sub;
        Assert.assertTrue(uniqueSub.contains("B"));
        Assert.assertTrue(uniqueSub.contains("C"));
        Assert.assertFalse(uniqueSub.contains("A"));
    }

    @Test
    public void testCreateSetBasedOnListWithTreeSet() {
        final List<String> backingList = new ArrayList<String>(Arrays.asList("A", "B"));
        final Set<String> backingSet = new TreeSet<String>(backingList);
        final SetUniqueList<String> list = new SetUniqueList<String>(backingList, backingSet);

        final List<String> subList = list.subList(0, 1);
        Assert.assertEquals(1, subList.size());
        Assert.assertTrue(subList.contains("A"));
        Assert.assertFalse(subList.contains("B"));
    }

    @Test
    public void testCreateSetBasedOnListWithNonDefaultConstructibleSet() {
        // Set with no default constructor to trigger InstantiationException
        final Set<String> nonInstantiableSet = new AbstractSet<String>() {
            private final Set<String> delegate = new HashSet<String>();

            @Override
            public Iterator<String> iterator() {
                return delegate.iterator();
            }

            @Override
            public int size() {
                return delegate.size();
            }

            @Override
            public boolean add(final String e) {
                return delegate.add(e);
            }
        };
        nonInstantiableSet.add("A");
        nonInstantiableSet.add("B");

        final List<String> backingList = new ArrayList<String>(Arrays.asList("A", "B"));
        final SetUniqueList<String> list = new SetUniqueList<String>(backingList, nonInstantiableSet);

        final Set<String> createdSet = list.createSetBasedOnList(nonInstantiableSet, backingList);
        Assert.assertNotNull(createdSet);
        Assert.assertEquals(2, createdSet.size());
        Assert.assertTrue(createdSet.contains("A"));
        Assert.assertTrue(createdSet.contains("B"));
    }

    private static class PrivateConstructorSet<E> extends HashSet<E> {
        private PrivateConstructorSet() {
            super();
        }
    }

    @Test
    public void testCreateSetBasedOnListWithPrivateConstructor() {
        final Set<String> privateSet = new PrivateConstructorSet<String>();
        privateSet.add("A");
        final List<String> list = new ArrayList<String>(Collections.singletonList("A"));
        final SetUniqueList<String> uniqueList = new SetUniqueList<String>(list, privateSet);

        final Set<String> result = uniqueList.createSetBasedOnList(privateSet, list);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("A"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final SetUniqueList<String> deserialized = (SetUniqueList<String>) ois.readObject();

        Assert.assertEquals(list, deserialized);
        Assert.assertTrue(deserialized.contains("A"));
        Assert.assertTrue(deserialized.contains("B"));
        Assert.assertTrue(deserialized.contains("C"));

        deserialized.add("D");
        Assert.assertEquals(4, deserialized.size());
        Assert.assertFalse(deserialized.add("A"));
    }
}
