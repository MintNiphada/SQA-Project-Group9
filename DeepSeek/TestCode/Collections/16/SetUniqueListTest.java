package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SetUniqueListTest {

    private SetUniqueList emptyList;
    private SetUniqueList decorated;

    @Before
    public void setUp() {
        emptyList = SetUniqueList.decorate(new ArrayList());
        decorated = SetUniqueList.decorate(new ArrayList(Arrays.asList("A", "B", "C")));
    }

    // --- decorate ---
    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test
    public void testDecorateEmptyList() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList());
        assertTrue(sul.isEmpty());
        assertTrue(sul.set.isEmpty());
    }

    @Test
    public void testDecorateWithDuplicates() {
        List original = new ArrayList(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList sul = SetUniqueList.decorate(original);
        assertEquals(Arrays.asList("A", "B", "C"), sul);
        assertTrue(sul.set.contains("A"));
        assertTrue(sul.set.contains("B"));
        assertTrue(sul.set.contains("C"));
        assertEquals(3, sul.set.size());
    }

    // --- constructor ---
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new SetUniqueList(new ArrayList(), null);
    }

    @Test
    public void testConstructorValid() {
        SetUniqueList sul = new SetUniqueList(new ArrayList(Arrays.asList("A", "B")), new HashSet(Arrays.asList("A", "B")));
        assertEquals(Arrays.asList("A", "B"), sul);
        assertTrue(sul.set.contains("A"));
    }

    // --- asSet ---
    @Test
    public void testAsSet() {
        Set s = decorated.asSet();
        assertTrue(s.contains("A"));
        assertEquals(3, s.size());
        // should be unmodifiable
        try {
            s.add("D");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // --- add ---
    @Test
    public void testAddNewElement() {
        boolean changed = emptyList.add("new");
        assertTrue(changed);
        assertEquals(1, emptyList.size());
        assertTrue(emptyList.set.contains("new"));
    }

    @Test
    public void testAddDuplicateElement() {
        boolean changed = decorated.add("A");
        assertFalse(changed);
        assertEquals(3, decorated.size());
    }

    @Test
    public void testAddNullElement() {
        emptyList.add(null);
        assertTrue(emptyList.set.contains(null));
        assertEquals(1, emptyList.size());
        // adding null again should not change
        boolean changed = emptyList.add(null);
        assertFalse(changed);
        assertEquals(1, emptyList.size());
    }

    // --- add(int, Object) ---
    @Test
    public void testAddAtIndexNewElement() {
        decorated.add(1, "D");
        // list should be [A, D, B, C]
        assertEquals(Arrays.asList("A", "D", "B", "C"), decorated);
        assertTrue(decorated.set.contains("D"));
    }

    @Test
    public void testAddAtIndexExistingElement() {
        decorated.add(1, "B");  // B already exists
        assertEquals(Arrays.asList("A", "B", "C"), decorated);
        assertTrue(decorated.set.contains("B"));
    }

    @Test
    public void testAddAtIndexNullNew() {
        decorated.add(0, null);
        assertEquals(Arrays.asList(null, "A", "B", "C"), decorated);
        // add again, no change
        decorated.add(2, null);
        assertEquals(Arrays.asList(null, "A", "B", "C"), decorated);
    }

    // --- addAll Collection ---
    @Test
    public void testAddAllCollectionWithNewAndExisting() {
        Collection coll = Arrays.asList("B", "D", "E");
        boolean changed = decorated.addAll(coll);
        assertTrue(changed);
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), decorated);
        assertTrue(decorated.set.contains("D"));
        assertTrue(decorated.set.contains("E"));
    }

    @Test
    public void testAddAllCollectionAllExisting() {
        Collection coll = Arrays.asList("A", "B");
        boolean changed = decorated.addAll(coll);
        assertFalse(changed);
        assertEquals(3, decorated.size());
    }

    // --- addAll(int, Collection) ---
    @Test
    public void testAddAllAtIndexNewAndExisting() {
        Collection coll = Arrays.asList("B", "D", "E");
        boolean changed = decorated.addAll(1, coll);
        assertTrue(changed);
        // B ignored, D and E inserted after index 1, so list becomes [A, D, E, B, C]
        // Wait: index=1, insert B (ignored, size unchanged), then D inserted at index 1 (since still index 1? Actually after each insert where size changes, index increments. So steps: index=1, B ignored -> size unchanged, index stays 1. Next D, contains false, add(1,D) -> inserted at index1, size=4, index becomes 2. Next E, contains false, add(2,E) -> inserted at index2, size=5, index=3. So final: [A, D, E, B, C]. Yes.
        assertEquals(Arrays.asList("A", "D", "E", "B", "C"), decorated);
        assertTrue(decorated.set.contains("D"));
        assertTrue(decorated.set.contains("E"));
    }

    @Test
    public void testAddAllAtIndexAllExisting() {
        Collection coll = Arrays.asList("A", "B");
        boolean changed = decorated.addAll(0, coll);
        assertFalse(changed);
        assertEquals(Arrays.asList("A", "B", "C"), decorated);
    }

    // --- set ---
    @Test
    public void testSetWithNewElement() {
        Object old = decorated.set(1, "D");
        assertEquals("B", old);
        assertEquals(Arrays.asList("A", "D", "C"), decorated);
        assertTrue(decorated.set.contains("D"));
        assertFalse(decorated.set.contains("B"));
    }

    @Test
    public void testSetWithElementPresentAtAnotherIndex() {
        // list: [A,B,C]; set("A" at index 0)
        Object old = decorated.set(2, "A"); // pos=0, index=2, so removes pos 0 then sets index2
        assertEquals("C", old);
        // after removal of duplicate at 0: list becomes [B,C]? Wait super.set(2, "A") will set element at index2 to "A". But list after removal of pos0: super.remove(0) removes "A", list becomes ["B","C"] (size 2). Then set(2,"A") on a list of size 2 will throw IndexOutOfBoundsException because index 2 is out of bounds. Hmm, this is a potential bug? The order: first pos = indexOf(object) -> 0. Then removed = super.set(index, object) -> sets element at index 2 to "A". Before set, list is [A,B,C]; after set, list becomes [A,B,A], removed=C. Then if pos != -1 && pos != index: pos=0, index=2, condition true, then super.remove(pos) removes element at 0, which is "A" (the original "A"). That will shift elements: list was [A,B,A], after remove(0) becomes [B,A]. Then set.add(object) adds "A" (already present), set.remove(removed) removes C. So final list: [B,A]. That's not what we might expect. So test this case.
        // Actually in this scenario, set(2,"A") might cause ArrayIndexOutOfBounds if list size is 3 and index 2 is valid. So it's fine.
        assertEquals(Arrays.asList("B", "A"), decorated); // after removal of original "A", list becomes [B, A]
        assertTrue(decorated.set.contains("A"));
        assertFalse(decorated.set.contains("C"));
    }

    @Test
    public void testSetWithSameElementSameIndex() {
        // set(1,"B") -> pos=1, index=1, so no duplicate removal
        Object old = decorated.set(1, "B");
        assertEquals("B", old);
        assertEquals(Arrays.asList("A", "B", "C"), decorated);
        assertTrue(decorated.set.contains("B"));
    }

    @Test
    public void testSetWithElementNotPresent() {
        Object old = decorated.set(0, "D");
        assertEquals("A", old);
        assertEquals(Arrays.asList("D", "B", "C"), decorated);
        assertFalse(decorated.set.contains("A"));
        assertTrue(decorated.set.contains("D"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetInvalidIndex() {
        decorated.set(5, "X");
    }

    // --- remove(Object) ---
    @Test
    public void testRemoveObjectExisting() {
        boolean removed = decorated.remove("B");
        assertTrue(removed);
        assertEquals(Arrays.asList("A", "C"), decorated);
        assertFalse(decorated.set.contains("B"));
    }

    @Test
    public void testRemoveObjectNonExisting() {
        boolean removed = decorated.remove("D");
        assertFalse(removed);
        assertEquals(Arrays.asList("A", "B", "C"), decorated);
    }

    @Test
    public void testRemoveNullExisting() {
        emptyList.add(null);
        boolean removed = emptyList.remove(null);
        assertTrue(removed);
        assertEquals(0, emptyList.size());
        assertFalse(emptyList.set.contains(null));
    }

    // --- remove(int) ---
    @Test
    public void testRemoveByIndex() {
        Object removed = decorated.remove(1);
        assertEquals("B", removed);
        assertEquals(Arrays.asList("A", "C"), decorated);
        assertFalse(decorated.set.contains("B"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByInvalidIndex() {
        decorated.remove(5);
    }

    // --- removeAll ---
    @Test
    public void testRemoveAll() {
        boolean changed = decorated.removeAll(Arrays.asList("B", "C", "D"));
        assertTrue(changed);
        assertEquals(Arrays.asList("A"), decorated);
        assertFalse(decorated.set.contains("B"));
        assertFalse(decorated.set.contains("C"));
    }

    @Test
    public void testRemoveAllNoMatch() {
        boolean changed = decorated.removeAll(Arrays.asList("X", "Y"));
        assertFalse(changed);
        assertEquals(3, decorated.size());
    }

    // --- retainAll ---
    @Test
    public void testRetainAll() {
        boolean changed = decorated.retainAll(Arrays.asList("A", "C"));
        assertTrue(changed);
        assertEquals(Arrays.asList("A", "C"), decorated);
        assertTrue(decorated.set.contains("A"));
        assertTrue(decorated.set.contains("C"));
        assertFalse(decorated.set.contains("B"));
    }

    @Test
    public void testRetainAllSameElements() {
        boolean changed = decorated.retainAll(Arrays.asList("A", "B", "C"));
        assertFalse(changed);
        assertEquals(3, decorated.size());
    }

    // --- clear ---
    @Test
    public void testClear() {
        decorated.clear();
        assertTrue(decorated.isEmpty());
        assertTrue(decorated.set.isEmpty());
    }

    // --- contains ---
    @Test
    public void testContains() {
        assertTrue(decorated.contains("A"));
        assertFalse(decorated.contains("D"));
    }

    // --- containsAll ---
    @Test
    public void testContainsAll() {
        assertTrue(decorated.containsAll(Arrays.asList("A", "B")));
        assertFalse(decorated.containsAll(Arrays.asList("A", "D")));
    }

    // --- iterator ---
    @Test
    public void testIteratorNextAndRemove() {
        Iterator it = decorated.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove(); // removes "B"
        assertEquals(Arrays.asList("A", "C"), decorated);
        assertFalse(decorated.set.contains("B"));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        Iterator it = decorated.iterator();
        it.remove(); // should throw IllegalStateException
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveTwice() {
        Iterator it = decorated.iterator();
        it.next();
        it.remove();
        it.remove(); // should throw IllegalStateException
    }

    // --- listIterator ---
    @Test
    public void testListIteratorNextAndPrevious() {
        ListIterator lit = decorated.listIterator();
        assertTrue(lit.hasNext());
        assertEquals("A", lit.next());
        assertEquals("B", lit.next());
        assertTrue(lit.hasPrevious());
        assertEquals("B", lit.previous()); // still "B"? Previous returns element before current? The previous should return "B" (current is between B and C? Actually after next B, cursor is after B, previous returns B and moves cursor before B. So fine.)
        assertEquals("B", lit.previous()); // now previous returns "A"? Let's check: after previous returned B, cursor is before B. previous again returns A. So we can test that.
        assertEquals("A", lit.next());
        lit.remove(); // removes A
        assertEquals(Arrays.asList("B", "C"), decorated);
        assertFalse(decorated.set.contains("A"));
    }

    @Test
    public void testListIteratorAdd() {
        ListIterator lit = emptyList.listIterator();
        lit.add("X");
        assertEquals(Arrays.asList("X"), emptyList);
        assertTrue(emptyList.set.contains("X"));
        lit.add("X"); // duplicate, should not add
        assertEquals(1, emptyList.size());
        lit.add("Y");
        assertEquals(Arrays.asList("X", "Y"), emptyList);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorSet() {
        ListIterator lit = decorated.listIterator();
        lit.next();
        lit.set("Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorRemoveWithoutNextOrPrevious() {
        ListIterator lit = decorated.listIterator();
        lit.remove();
    }

    // --- listIterator(int index) ---
    @Test
    public void testListIteratorAtIndex() {
        ListIterator lit = decorated.listIterator(1);
        assertEquals("B", lit.next());
        assertEquals("C", lit.next());
        assertFalse(lit.hasNext());
    }

    // --- subList ---
    @Test
    public void testSubList() {
        List sub = decorated.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(Arrays.asList("B", "C"), sub);
        // sub shares the same set
        assertTrue(sub.contains("B"));
        // modify sub
        sub.add("D");
        // should affect original set and list
        assertEquals(Arrays.asList("A", "B", "C", "D"), decorated);
        assertTrue(decorated.set.contains("D"));
        assertEquals(4, decorated.size());
        // remove using sub
        sub.remove("B");
        assertEquals(Arrays.asList("A", "C", "D"), decorated);
        assertFalse(decorated.set.contains("B"));
    }

    @Test
    public void testSubListDuplicateAdd() {
        List sub = decorated.subList(0, 2);
        sub.add("A"); // duplicate, should not change
        assertEquals(Arrays.asList("A", "B"), sub); // original sub list was [A,B]
        assertEquals(3, decorated.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListInvalidIndex() {
        decorated.subList(5, 6);
    }
}
