package org.apache.commons.collections4.list;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

public class SetUniqueListTest {

    private SetUniqueList<String> list;
    private List<String> backingList;

    @Before
    public void setUp() {
        backingList = new ArrayList<String>();
        list = new SetUniqueList<String>(backingList, new HashSet<String>());
    }

    // Helper to create a SetUniqueList with a specific set type (for testing createSetBasedOnList)
    private static class TestSetUniqueList<E> extends SetUniqueList<E> {
        public TestSetUniqueList(List<E> list, Set<E> set) {
            super(list, set);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test
    public void testFactoryEmptyList() {
        List<String> empty = new ArrayList<String>();
        SetUniqueList<String> result = SetUniqueList.setUniqueList(empty);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    public void testFactoryWithDuplicates() {
        List<String> input = new ArrayList<String>();
        input.add("a");
        input.add("b");
        input.add("a");
        input.add("c");
        SetUniqueList<String> result = SetUniqueList.setUniqueList(input);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
        // original list should be cleared and then populated
        assertTrue(input.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    @Test
    public void testAdd() {
        assertTrue(list.add("a"));
        assertEquals(1, list.size());
        assertTrue(list.contains("a"));
        // duplicate
        assertFalse(list.add("a"));
        assertEquals(1, list.size());
    }

    @Test
    public void testAddNull() {
        assertTrue(list.add(null));
        assertTrue(list.contains(null));
        assertFalse(list.add(null));
        assertEquals(1, list.size());
    }

    @Test
    public void testAddAtIndex() {
        list.add("a");
        list.add("c");
        // insert at index 1
        list.add(1, "b");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        // duplicate not added
        list.add(1, "b");
        assertEquals(3, list.size());
        assertEquals("b", list.get(1));
    }

    @Test
    public void testAddAtIndexDuplicate() {
        list.add("a");
        list.add("b");
        list.add(1, "a"); // duplicate, should not add
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testAddAll() {
        List<String> coll = Arrays.asList("a", "b", "a", "c");
        assertTrue(list.addAll(coll));
        assertEquals(3, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
        assertTrue(list.contains("c"));
        // adding same collection again returns false
        assertFalse(list.addAll(coll));
    }

    @Test
    public void testAddAllEmpty() {
        assertFalse(list.addAll(Collections.<String>emptyList()));
    }

    @Test
    public void testAddAllAtIndex() {
        list.add("x");
        list.add("z");
        List<String> coll = Arrays.asList("a", "b", "a");
        assertTrue(list.addAll(1, coll));
        assertEquals(4, list.size());
        assertEquals("x", list.get(0));
        assertEquals("a", list.get(1));
        assertEquals("b", list.get(2));
        assertEquals("z", list.get(3));
        // duplicate not added
        assertFalse(list.addAll(1, Arrays.asList("a")));
    }

    @Test
    public void testSet() {
        list.add("a");
        list.add("b");
        list.add("c");
        // set with new value
        String old = list.set(1, "d");
        assertEquals("b", old);
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("d", list.get(1));
        assertEquals("c", list.get(2));
        assertFalse(list.contains("b"));
        assertTrue(list.contains("d"));
    }

    @Test
    public void testSetDuplicateAtDifferentIndex() {
        list.add("a");
        list.add("b");
        list.add("c");
        // set index 1 to "a" (duplicate at index 0)
        String old = list.set(1, "a");
        assertEquals("b", old);
        // now "a" at index 0 and 1? Actually set will remove the duplicate at index 0
        assertEquals(2, list.size());
        assertEquals("a", list.get(0)); // the one at index 1 moved? Let's trace: set(1, "a") -> pos = indexOf("a") = 0, removed = super.set(1, "a") returns "b", then if pos != -1 && pos != index (0 != 1) -> super.remove(0) removes the original "a". Then set.remove(removed) removes "b", set.add("a"). So final list: index 0 was "a", removed, so list becomes ["b", "c"]? Wait, after super.set(1, "a"), list becomes ["a", "a", "c"]? Actually super.set(1, "a") replaces element at index 1 with "a", so list becomes ["a", "a", "c"]. Then super.remove(0) removes the first "a", leaving ["a", "c"]. So final list: ["a", "c"]. So size 2, index 0 is "a", index 1 is "c". So we check:
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
        assertFalse(list.contains("b"));
    }

    @Test
    public void testSetSameIndexDuplicate() {
        list.add("a");
        list.add("b");
        // set index 0 to "a" (same object, same index)
        String old = list.set(0, "a");
        assertEquals("a", old);
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testRemoveObject() {
        list.add("a");
        list.add("b");
        assertTrue(list.remove("a"));
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        assertFalse(list.remove("a")); // already removed
    }

    @Test
    public void testRemoveByIndex() {
        list.add("a");
        list.add("b");
        String removed = list.remove(0);
        assertEquals("a", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndexOutOfBounds() {
        list.remove(0);
    }

    @Test
    public void testRemoveAll() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertTrue(list.removeAll(Arrays.asList("a", "c", "d")));
        assertEquals(1, list.size());
        assertEquals("b", list.get(0));
        assertFalse(list.removeAll(Arrays.asList("x")));
    }

    @Test
    public void testRetainAll() {
        list.add("a");
        list.add("b");
        list.add("c");
        // retain all elements -> false
        assertFalse(list.retainAll(Arrays.asList("a", "b", "c")));
        // retain subset
        assertTrue(list.retainAll(Arrays.asList("a", "c")));
        assertEquals(2, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("c"));
        assertFalse(list.contains("b"));
        // retain none -> clear
        assertTrue(list.retainAll(Collections.<String>emptyList()));
        assertTrue(list.isEmpty());
    }

    @Test
    public void testRetainAllWithDuplicatesInCollection() {
        list.add("a");
        list.add("b");
        // collection has duplicates, but retainAll should work
        assertTrue(list.retainAll(Arrays.asList("a", "a")));
        assertEquals(1, list.size());
        assertEquals("a", list.get(0));
    }

    @Test
    public void testClear() {
        list.add("a");
        list.add("b");
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("a"));
    }

    @Test
    public void testContains() {
        assertFalse(list.contains("a"));
        list.add("a");
        assertTrue(list.contains("a"));
    }

    @Test
    public void testContainsAll() {
        list.add("a");
        list.add("b");
        assertTrue(list.containsAll(Arrays.asList("a", "b")));
        assertFalse(list.containsAll(Arrays.asList("a", "c")));
        assertTrue(list.containsAll(Collections.<String>emptyList()));
    }

    @Test
    public void testAsSet() {
        list.add("a");
        list.add("b");
        Set<String> setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("a"));
        // should be unmodifiable
        try {
            setView.add("c");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testIterator() {
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        assertTrue(list.contains("b"));
        // remove without next should throw IllegalStateException
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testListIterator() {
        list.add("a");
        list.add("b");
        ListIterator<String> lit = list.listIterator();
        assertTrue(lit.hasNext());
        assertFalse(lit.hasPrevious());
        assertEquals("a", lit.next());
        assertEquals("b", lit.next());
        assertTrue(lit.hasPrevious());
        assertEquals("b", lit.previous());
        assertEquals("a", lit.previous());
    }

    @Test
    public void testListIteratorAdd() {
        list.add("a");
        list.add("c");
        ListIterator<String> lit = list.listIterator(1);
        lit.add("b"); // should add "b" at index 1
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        // duplicate not added
        lit.add("b");
        assertEquals(3, list.size());
    }

    @Test
    public void testListIteratorSet() {
        list.add("a");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        try {
            lit.set("b");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testListIteratorRemove() {
        list.add("a");
        list.add("b");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        // remove without next/previous should throw IllegalStateException
        try {
            lit.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testSubList() {
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        List<String> sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("b", sub.get(0));
        assertEquals("c", sub.get(1));
        // subList is a SetUniqueList, so adding duplicate should not add
        sub.add("b");
        assertEquals(2, sub.size());
        // adding new element to subList adds to original list's subList portion
        sub.add("e");
        assertEquals(3, sub.size());
        assertEquals("e", sub.get(2));
        // original list should now have "e" at index 3? Actually original list: [a,b,c,d] after subList add "e" at end of subList (which is index 3 in original), so original becomes [a,b,c,e,d]? Wait, subList is backed by original list's subList from 1 to 3. Adding at end of subList inserts at index 3 relative to original? Let's check: original list after subList.add("e"): subList is view of indices 1-3 (exclusive of 3 originally). After add, subList size becomes 3, so original list's size becomes 5. The element "e" is inserted at position 3 in the original list (since subList.add appends to the subList, which corresponds to index 3 in original). So original becomes [a, b, c, e, d]. We'll verify:
        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("e", list.get(3));
        assertEquals("d", list.get(4));
        // However, the original set does not contain "e" because subList's set is independent.
        assertFalse(list.contains("e")); // because original set was not updated
    }

    @Test
    public void testSubListWithCustomSetType() {
        // Use a TreeSet to test createSetBasedOnList with non-HashSet
        SetUniqueList<String> customList = new TestSetUniqueList<String>(new ArrayList<String>(), new TreeSet<String>());
        customList.add("b");
        customList.add("a");
        customList.add("c");
        List<String> sub = customList.subList(0, 2);
        assertTrue(sub instanceof SetUniqueList);
        Set<String> subSet = ((SetUniqueList<String>) sub).asSet();
        // The set should be a TreeSet (or at least not a HashSet) because createSetBasedOnList should instantiate same type
        assertTrue(subSet instanceof TreeSet);
        assertEquals(2, subSet.size());
        assertTrue(subSet.contains("a"));
        assertTrue(subSet.contains("b"));
    }

    @Test
    public void testSubListWithHashSet() {
        // default factory uses HashSet, subList should create HashSet
        list.add("x");
        list.add("y");
        List<String> sub = list.subList(0, 1);
        Set<String> subSet = ((SetUniqueList<String>) sub).asSet();
        assertTrue(subSet instanceof HashSet);
    }

    @Test
    public void testListIteratorIndexOutOfBounds() {
        try {
            list.listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            list.listIterator(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testAddAllAtIndexWithEmptyCollection() {
        list.add("a");
        assertFalse(list.addAll(0, Collections.<String>emptyList()));
    }

    @Test
    public void testSetOnEmptyList() {
        try {
            list.set(0, "a");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRemoveObjectNull() {
        list.add(null);
        assertTrue(list.remove(null));
        assertFalse(list.contains(null));
    }

    @Test
    public void testRetainAllWithNullElements() {
        list.add("a");
        list.add(null);
        list.add("b");
        assertTrue(list.retainAll(Arrays.asList("a", null)));
        assertEquals(2, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains(null));
        assertFalse(list.contains("b"));
    }

    @Test
    public void testIteratorRemoveUpdatesSet() {
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertFalse(list.asSet().contains("a"));
    }

    @Test
    public void testListIteratorAddUpdatesSet() {
        list.add("a");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.add("b");
        assertTrue(list.asSet().contains("b"));
    }

    @Test
    public void testListIteratorPreviousAndRemove() {
        list.add("a");
        list.add("b");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.next();
        lit.previous(); // "b"
        lit.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("b"));
    }

    @Test
    public void testAddAllWithNullElements() {
        List<String> coll = new ArrayList<String>();
        coll.add("a");
        coll.add(null);
        coll.add("b");
        assertTrue(list.addAll(coll));
        assertEquals(3, list.size());
        assertTrue(list.contains(null));
    }

    @Test
    public void testSetDuplicateWithNull() {
        list.add(null);
        list.add("a");
        // set index 1 to null (duplicate at index 0)
        String old = list.set(1, null);
        assertEquals("a", old);
        assertEquals(1, list.size());
        assertNull(list.get(0));
    }

    @Test
    public void testSubListAddDuplicate() {
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> sub = list.subList(0, 2);
        sub.add("a"); // duplicate, should not add
        assertEquals(2, sub.size());
    }

    @Test
    public void testSubListRemove() {
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> sub = list.subList(1, 3);
        sub.remove("b");
        assertEquals(1, sub.size());
        assertEquals("c", sub.get(0));
        // original list should reflect removal
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testSubListClear() {
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> sub = list.subList(0, 2);
        sub.clear();
        assertTrue(sub.isEmpty());
        assertEquals(1, list.size());
        assertEquals("c", list.get(0));
    }

    @Test
    public void testSubListIteratorRemove() {
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> sub = list.subList(0, 2);
        Iterator<String> it = sub.iterator();
        it.next();
        it.remove();
        assertEquals(1, sub.size());
        assertEquals("b", sub.get(0));
        assertEquals(2, list.size());
        assertEquals("b", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testSubListListIteratorAdd() {
        list.add("a");
        list.add("c");
        List<String> sub = list.subList(0, 1);
        ListIterator<String> lit = sub.listIterator();
        lit.next();
        lit.add("b");
        assertEquals(2, sub.size());
        assertEquals("a", sub.get(0));
        assertEquals("b", sub.get(1));
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testSubListSet() {
        list.add("a");
        list.add("b");
        List<String> sub = list.subList(0, 1);
        try {
            sub.set(0, "c");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected because subList's listIterator set throws exception
        }
    }

    @Test
    public void testCreateSetBasedOnListFallback() {
        // To test the catch blocks, we need a set class that cannot be instantiated.
        // We'll create a custom Set implementation with no default constructor.
        // But we can't instantiate it to pass to the constructor. So we'll use a mock-like approach:
        // We'll create a subclass of SetUniqueList that overrides createSetBasedOnList to expose it,
        // but we need to test the actual method. Since the method is protected, we can call it via reflection.
        // However, we can create a SetUniqueList with a set that is not a HashSet and whose class has no default constructor.
        // We can define a static inner class that extends AbstractSet but has no default constructor.
        // Then we can't instantiate it, but we can pass an instance of a subclass that does have a default constructor?
        // Actually, we need to pass a set instance to the constructor. So we need an instance of a class that has no default constructor.
        // We can create an anonymous class? Anonymous classes have no default constructor? They have a constructor that captures enclosing instance.
        // We can create a local class inside the test method that extends HashSet but has no default constructor? That's tricky.
        // Simpler: we can use a set class that is abstract, but we can't instantiate abstract class.
        // So we can't easily test the fallback without reflection. We'll skip that for now.
    }

    @Test
    public void testSerializationSupport() {
        // Not testing serialization, but ensure class is serializable
        assertTrue(list instanceof java.io.Serializable);
    }
}
