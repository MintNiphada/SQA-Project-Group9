package org.apache.commons.collections.list;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Unit tests for SetUniqueList.
 */
public class SetUniqueListTest {

    // Helper to expose protected constructor for testing
    private static class TestSetUniqueList extends SetUniqueList {
        public TestSetUniqueList(List list, Set set) {
            super(list, set);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test
    public void testDecorateEmptyList() {
        List<Object> list = new ArrayList<Object>();
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertNotNull(sul);
        assertEquals(0, sul.size());
        assertTrue(sul.asSet().isEmpty());
    }

    @Test
    public void testDecorateWithDuplicates() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "a", "c", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertEquals(3, sul.size());
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
        assertTrue(sul.asSet().contains("a"));
        assertTrue(sul.asSet().contains("b"));
        assertTrue(sul.asSet().contains("c"));
    }

    @Test
    public void testAddNewElement() {
        List<String> list = new ArrayList<String>();
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertTrue(sul.add("x"));
        assertEquals(1, sul.size());
        assertTrue(sul.contains("x"));
        assertTrue(sul.asSet().contains("x"));
    }

    @Test
    public void testAddDuplicateElement() {
        List<String> list = new ArrayList<String>(Arrays.asList("x"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertFalse(sul.add("x"));
        assertEquals(1, sul.size());
        assertEquals("x", sul.get(0));
    }

    @Test
    public void testAddAtIndexNewElement() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        sul.add(1, "b");
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
    }

    @Test
    public void testAddAtIndexDuplicate() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        sul.add(1, "c"); // duplicate
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.size());
    }

    @Test
    public void testAddAllAddsUniqueElements() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("b", "c", "d", "c");
        assertTrue(sul.addAll(coll));
        assertEquals(Arrays.asList("a", "b", "c", "d"), sul);
        assertEquals(4, sul.asSet().size());
    }

    @Test
    public void testAddAllNoChange() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("a", "b", "c");
        assertFalse(sul.addAll(coll));
        assertEquals(3, sul.size());
    }

    @Test
    public void testAddAllAtIndex() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "d"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("b", "c", "b");
        assertTrue(sul.addAll(1, coll));
        assertEquals(Arrays.asList("a", "b", "c", "d"), sul);
    }

    @Test
    public void testSetNewValue() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Object old = sul.set(1, "x");
        assertEquals("b", old);
        assertEquals(Arrays.asList("a", "x", "c"), sul);
        assertTrue(sul.asSet().contains("x"));
        assertFalse(sul.asSet().contains("b"));
    }

    @Test
    public void testSetDuplicateValuePresent() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        // list: a, b, c ; set: a,b,c  (duplicate b removed)
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        // set index 0 to "c" (which is present at index 2)
        Object old = sul.set(0, "c");
        assertEquals("a", old);
        assertEquals(Arrays.asList("c", "b"), sul);
        assertTrue(sul.asSet().contains("c"));
        assertTrue(sul.asSet().contains("b"));
        assertFalse(sul.asSet().contains("a"));
    }

    @Test
    public void testSetSameValueAtSameIndex() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Object old = sul.set(1, "b");
        assertEquals("b", old);
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
    }

    @Test
    public void testSetValueAtSameIndexWhenDuplicateElsewhere() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        // after decorate: a,b,c
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        // set index 1 (which currently has "b") to "b" (duplicate of itself)
        Object old = sul.set(1, "b");
        assertEquals("b", old);
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
    }

    @Test
    public void testRemoveObject() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertTrue(sul.remove("b"));
        assertEquals(Arrays.asList("a", "c"), sul);
        assertFalse(sul.asSet().contains("b"));
        assertFalse(sul.remove("z"));
    }

    @Test
    public void testRemoveIndex() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Object removed = sul.remove(1);
        assertEquals("b", removed);
        assertEquals(Arrays.asList("a", "c"), sul);
        assertFalse(sul.asSet().contains("b"));
    }

    @Test
    public void testRemoveAll() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("b", "d");
        assertTrue(sul.removeAll(coll));
        assertEquals(Arrays.asList("a", "c"), sul);
        assertFalse(sul.asSet().contains("b"));
        assertFalse(sul.asSet().contains("d"));
    }

    @Test
    public void testRetainAll() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("b", "d", "x");
        assertTrue(sul.retainAll(coll));
        assertEquals(Arrays.asList("b", "d"), sul);
        assertTrue(sul.asSet().contains("b"));
        assertTrue(sul.asSet().contains("d"));
        assertFalse(sul.asSet().contains("a"));
        assertFalse(sul.asSet().contains("c"));
    }

    @Test
    public void testClear() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        sul.clear();
        assertEquals(0, sul.size());
        assertTrue(sul.asSet().isEmpty());
    }

    @Test
    public void testContains() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertTrue(sul.contains("a"));
        assertFalse(sul.contains("c"));
    }

    @Test
    public void testContainsAll() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertTrue(sul.containsAll(Arrays.asList("a", "c")));
        assertFalse(sul.containsAll(Arrays.asList("a", "x")));
    }

    @Test
    public void testIteratorRemove() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Iterator<String> it = sul.iterator();
        assertEquals("a", it.next());
        it.remove();
        assertEquals(Arrays.asList("b", "c"), sul);
        assertFalse(sul.asSet().contains("a"));
    }

    @Test
    public void testListIteratorNextAndRemove() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        ListIterator<String> it = sul.listIterator();
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        it.remove(); // removes "b"
        assertEquals(Arrays.asList("a", "c"), sul);
        assertFalse(sul.asSet().contains("b"));
    }

    @Test
    public void testListIteratorPreviousAndRemove() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        ListIterator<String> it = sul.listIterator(2);
        assertEquals("b", it.previous());
        it.remove(); // removes "b"
        assertEquals(Arrays.asList("a", "c"), sul);
        assertFalse(sul.asSet().contains("b"));
    }

    @Test
    public void testListIteratorAddUnique() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        ListIterator<String> it = sul.listIterator();
        assertEquals("a", it.next());
        it.add("b"); // add between a and c
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
    }

    @Test
    public void testListIteratorAddDuplicate() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        ListIterator<String> it = sul.listIterator();
        assertEquals("a", it.next());
        it.add("b"); // duplicate
        assertEquals(Arrays.asList("a", "b", "c"), sul);
        assertEquals(3, sul.asSet().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorSetUnsupported() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        ListIterator<String> it = sul.listIterator();
        it.next();
        it.set("x");
    }

    @Test
    public void testSubList() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        List<String> sub = sul.subList(1, 3);
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(Arrays.asList("b", "c"), sub);
        // Modifying sub affects original (since subList is a view)
        sub.add("x");
        assertEquals(Arrays.asList("a", "b", "c", "x", "d"), sul);
        assertTrue(sul.asSet().contains("x"));
    }

    @Test
    public void testAsSetReturnsUnmodifiableSet() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        SetUniqueList sul = SetUniqueList.decorate(list);
        Set<String> setView = sul.asSet();
        assertNotNull(setView);
        assertEquals(2, setView.size());
        // attempt to modify should throw
        try {
            setView.add("c");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new TestSetUniqueList(new ArrayList<Object>(), null);
    }
}
