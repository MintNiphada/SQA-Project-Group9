package org.apache.commons.collections.set;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.collections.OrderedIterator;
import org.junit.Test;

public class ListOrderedSetTest {

    // Helper to access protected constructors via reflection
    private <E> ListOrderedSet<E> newListOrderedSet(Set<E> set, List<E> list) throws Exception {
        Constructor<ListOrderedSet> c = ListOrderedSet.class.getDeclaredConstructor(Set.class, List.class);
        c.setAccessible(true);
        return c.newInstance(set, list);
    }

    private <E> ListOrderedSet<E> newListOrderedSet(Set<E> set) throws Exception {
        Constructor<ListOrderedSet> c = ListOrderedSet.class.getDeclaredConstructor(Set.class);
        c.setAccessible(true);
        return c.newInstance(set);
    }

    // ---------- Factory method tests ----------

    @Test
    public void testFactoryListOrderedSet_SetAndList() {
        Set<String> set = new HashSet<String>();
        List<String> list = new ArrayList<String>();
        ListOrderedSet<String> los = ListOrderedSet.listOrderedSet(set, list);
        assertNotNull(los);
        assertTrue(los.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_SetAndList_NullSet() {
        ListOrderedSet.listOrderedSet(null, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_SetAndList_NullList() {
        ListOrderedSet.listOrderedSet(new HashSet<String>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_SetAndList_NonEmptySet() {
        Set<String> set = new HashSet<String>();
        set.add("a");
        ListOrderedSet.listOrderedSet(set, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_SetAndList_NonEmptyList() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        ListOrderedSet.listOrderedSet(new HashSet<String>(), list);
    }

    @Test
    public void testFactoryListOrderedSet_Set() {
        Set<String> set = new HashSet<String>();
        ListOrderedSet<String> los = ListOrderedSet.listOrderedSet(set);
        assertNotNull(los);
        assertTrue(los.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_Set_Null() {
        ListOrderedSet.listOrderedSet((Set<String>) null);
    }

    @Test
    public void testFactoryListOrderedSet_List() {
        List<String> list = new ArrayList<String>();
        list.add("b");
        list.add("a");
        list.add("b"); // duplicate
        ListOrderedSet<String> los = ListOrderedSet.listOrderedSet(list);
        assertEquals(2, los.size());
        // order should be as first occurrence: b, a
        assertEquals("b", los.get(0));
        assertEquals("a", los.get(1));
        // original list modified: duplicates removed
        assertEquals(2, list.size());
        assertEquals("b", list.get(0));
        assertEquals("a", list.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryListOrderedSet_List_Null() {
        ListOrderedSet.listOrderedSet((List<String>) null);
    }

    // ---------- Constructor tests ----------

    @Test
    public void testDefaultConstructor() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertTrue(los.isEmpty());
        assertEquals(0, los.size());
    }

    @Test
    public void testConstructorWithSet() throws Exception {
        Set<String> set = new HashSet<String>();
        set.add("x");
        set.add("y");
        ListOrderedSet<String> los = newListOrderedSet(set);
        assertEquals(2, los.size());
        // order is iteration order of set (HashSet unpredictable, but both elements present)
        assertTrue(los.contains("x"));
        assertTrue(los.contains("y"));
    }

    @Test
    public void testConstructorWithSet_NullSet() throws Exception {
        // Constructor does not check null, so it will throw NPE later, but we can test that it doesn't throw immediately
        try {
            ListOrderedSet<String> los = newListOrderedSet(null);
            // if no exception, later operations will fail
            assertNotNull(los);
        } catch (NullPointerException e) {
            // acceptable
        }
    }

    @Test
    public void testConstructorWithSetAndList() throws Exception {
        Set<String> set = new HashSet<String>();
        set.add("a");
        set.add("b");
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        ListOrderedSet<String> los = newListOrderedSet(set, list);
        assertEquals(2, los.size());
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithSetAndList_NullList() throws Exception {
        newListOrderedSet(new HashSet<String>(), null);
    }

    @Test
    public void testConstructorWithSetAndList_NullSet() throws Exception {
        // null set not checked, may cause NPE later
        try {
            ListOrderedSet<String> los = newListOrderedSet(null, new ArrayList<String>());
            assertNotNull(los);
        } catch (NullPointerException e) {
            // acceptable
        }
    }

    // ---------- asList ----------

    @Test
    public void testAsList() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        List<String> list = los.asList();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        // unmodifiable
        try {
            list.add("c");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------- clear ----------

    @Test
    public void testClear() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        los.clear();
        assertTrue(los.isEmpty());
        assertEquals(0, los.size());
        assertEquals(0, los.asList().size());
    }

    // ---------- iterator ----------

    @Test
    public void testIterator() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        Iterator<String> it = los.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        Iterator<String> it = los.iterator();
        it.next();
        it.remove();
        assertEquals(1, los.size());
        assertFalse(los.contains("a"));
        assertEquals("b", los.get(0));
    }

    @Test
    public void testOrderedIteratorHasPreviousAndPrevious() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        OrderedIterator<String> it = los.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.previous());
        assertTrue(it.hasPrevious());
        assertEquals("a", it.previous());
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testIteratorRemoveTwiceThrows() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        Iterator<String> it = los.iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    // ---------- add ----------

    @Test
    public void testAdd() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertTrue(los.add("a"));
        assertTrue(los.contains("a"));
        assertEquals(1, los.size());
        assertEquals("a", los.get(0));
        // duplicate
        assertFalse(los.add("a"));
        assertEquals(1, los.size());
    }

    @Test
    public void testAddNull() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertTrue(los.add(null));
        assertTrue(los.contains(null));
        assertEquals(1, los.size());
        assertNull(los.get(0));
        // duplicate null
        assertFalse(los.add(null));
        assertEquals(1, los.size());
    }

    // ---------- addAll ----------

    @Test
    public void testAddAll() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        Collection<String> coll = Arrays.asList("a", "b", "a");
        assertTrue(los.addAll(coll));
        assertEquals(2, los.size());
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
        // add again, no change
        assertFalse(los.addAll(coll));
    }

    @Test
    public void testAddAllEmpty() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertFalse(los.addAll(new ArrayList<String>()));
    }

    // ---------- remove ----------

    @Test
    public void testRemove() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertTrue(los.remove("a"));
        assertFalse(los.contains("a"));
        assertEquals(1, los.size());
        assertEquals("b", los.get(0));
        assertFalse(los.remove("c"));
    }

    @Test
    public void testRemoveNull() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add(null);
        assertTrue(los.remove(null));
        assertFalse(los.contains(null));
    }

    // ---------- removeAll ----------

    @Test
    public void testRemoveAll() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        los.add("c");
        Collection<String> toRemove = Arrays.asList("a", "c");
        assertTrue(los.removeAll(toRemove));
        assertEquals(1, los.size());
        assertEquals("b", los.get(0));
    }

    @Test
    public void testRemoveAllNoMatch() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        assertFalse(los.removeAll(Arrays.asList("b")));
    }

    // ---------- retainAll ----------

    @Test
    public void testRetainAll() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        los.add("c");
        Collection<String> retain = Arrays.asList("b", "d");
        assertTrue(los.retainAll(retain));
        assertEquals(1, los.size());
        assertEquals("b", los.get(0));
    }

    @Test
    public void testRetainAllNoChange() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertFalse(los.retainAll(Arrays.asList("a", "b")));
        assertEquals(2, los.size());
    }

    @Test
    public void testRetainAllClear() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertTrue(los.retainAll(new ArrayList<String>()));
        assertTrue(los.isEmpty());
        assertEquals(0, los.asList().size());
    }

    @Test
    public void testRetainAllWithNull() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add(null);
        los.add("a");
        Collection<String> retain = new ArrayList<String>();
        retain.add(null);
        assertTrue(los.retainAll(retain));
        assertEquals(1, los.size());
        assertNull(los.get(0));
    }

    // ---------- toArray ----------

    @Test
    public void testToArray() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        Object[] arr = los.toArray();
        assertEquals(2, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
    }

    @Test
    public void testToArrayWithParameter() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        String[] arr = los.toArray(new String[0]);
        assertEquals(2, arr.length);
        assertEquals("a", arr[0]);
    }

    // ---------- get ----------

    @Test
    public void testGet() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.get(0);
    }

    // ---------- indexOf ----------

    @Test
    public void testIndexOf() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertEquals(0, los.indexOf("a"));
        assertEquals(1, los.indexOf("b"));
        assertEquals(-1, los.indexOf("c"));
    }

    @Test
    public void testIndexOfNull() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add(null);
        assertEquals(0, los.indexOf(null));
    }

    // ---------- add(int, E) ----------

    @Test
    public void testAddAtIndex() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("c");
        los.add(1, "b");
        assertEquals(3, los.size());
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
        assertEquals("c", los.get(2));
    }

    @Test
    public void testAddAtIndexDuplicate() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        los.add(1, "a"); // duplicate, should not be added
        assertEquals(2, los.size());
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
    }

    @Test
    public void testAddAtIndexAtEnd() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add(1, "b");
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
    }

    // ---------- addAll(int, Collection) ----------

    @Test
    public void testAddAllAtIndex() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("d");
        List<String> toAdd = Arrays.asList("b", "c");
        assertTrue(los.addAll(1, toAdd));
        assertEquals(4, los.size());
        assertEquals("a", los.get(0));
        assertEquals("b", los.get(1));
        assertEquals("c", los.get(2));
        assertEquals("d", los.get(3));
    }

    @Test
    public void testAddAllAtIndexWithDuplicates() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        List<String> toAdd = Arrays.asList("b", "c");
        assertTrue(los.addAll(1, toAdd)); // "b" duplicate, only "c" added
        assertEquals(3, los.size());
        assertEquals("a", los.get(0));
        assertEquals("c", los.get(1));
        assertEquals("b", los.get(2));
    }

    @Test
    public void testAddAllAtIndexEmpty() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        assertFalse(los.addAll(0, new ArrayList<String>()));
        assertEquals(1, los.size());
    }

    @Test
    public void testAddAllAtIndexAllDuplicates() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        assertFalse(los.addAll(0, Arrays.asList("a", "b")));
        assertEquals(2, los.size());
    }

    // ---------- remove(int) ----------

    @Test
    public void testRemoveIndex() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.add("a");
        los.add("b");
        los.add("c");
        Object removed = los.remove(1);
        assertEquals("b", removed);
        assertEquals(2, los.size());
        assertFalse(los.contains("b"));
        assertEquals("a", los.get(0));
        assertEquals("c", los.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexOutOfBounds() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        los.remove(0);
    }

    // ---------- toString ----------

    @Test
    public void testToString() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertEquals("[]", los.toString());
        los.add("a");
        los.add("b");
        assertEquals("[a, b]", los.toString());
    }

    // ---------- Additional edge cases ----------

    @Test
    public void testAddAllWithNullElements() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        Collection<String> coll = new ArrayList<String>();
        coll.add(null);
        coll.add("a");
        assertTrue(los.addAll(coll));
        assertEquals(2, los.size());
        assertNull(los.get(0));
        assertEquals("a", los.get(1));
    }

    @Test
    public void testRetainAllWithEmptySet() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        assertFalse(los.retainAll(Arrays.asList("a")));
    }

    @Test
    public void testIteratorOnEmpty() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        Iterator<String> it = los.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testOrderedIteratorPreviousOnEmpty() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        OrderedIterator<String> it = los.iterator();
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testAddAtIndexNegativeIndex() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        try {
            los.add(-1, "a");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testAddAllAtIndexNegativeIndex() {
        ListOrderedSet<String> los = new ListOrderedSet<String>();
        try {
            los.addAll(-1, Arrays.asList("a"));
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithSetAndList_MismatchedElements() throws Exception {
        // The constructor does not validate that set and list contain same elements; it trusts the caller.
        Set<String> set = new HashSet<String>(Arrays.asList("a", "b"));
        List<String> list = new ArrayList<String>(Arrays.asList("a", "c")); // mismatch
        ListOrderedSet<String> los = newListOrderedSet(set, list);
        // The set contains a,b but list contains a,c. This is inconsistent but allowed.
        assertEquals(2, los.size()); // size from set
        assertTrue(los.contains("a"));
        assertTrue(los.contains("b"));
        assertFalse(los.contains("c"));
        // get(1) returns "c" from list, but it's not in set
        assertEquals("c", los.get(1));
    }
}
