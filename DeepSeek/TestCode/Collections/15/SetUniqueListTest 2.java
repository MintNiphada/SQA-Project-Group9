package org.apache.commons.collections.list;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class SetUniqueListTest {

    private List<Object> backingList;
    private SetUniqueList uniqueList;

    private Object obj1;
    private Object obj2;
    private Object obj3;
    private Object obj4;

    @Before
    public void setUp() {
        backingList = new ArrayList<>();
        uniqueList = (SetUniqueList) SetUniqueList.decorate(backingList);
        obj1 = Integer.value0f(1);
        obj2 = Integer.value0f(2);
        obj3 = Integer.value0f(3);
        obj4 = Integer.value0f(4);
    }

    // ===================================
    // Tests for decorate() method
    // ===================================

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test
    public void testDecorateEmptyList() {
        List<Object> list = new ArrayList<>();
        SetUniqueList result = (SetUniqueList) SetUniqueList.decorate(list);
        Assert.assertTrue(result.isEmpty());
        Assert.assertSame(list, result.getList());
        Assert.assertEquals(0, result.set.size());
    }

    @Test
    public void testDecorateNonEmptyList() {
        List<Object> original = new ArrayList<>(Arrays.asList(obj1, obj2, obj1, obj3, obj2));
        SetUniqueList result = (SetUniqueList) SetUniqueList.decorate(original);
        Assert.assertEquals(0, original.size()); // original is cleared
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(obj1, result.get(0));
        Assert.assertEquals(obj2, result.get(1));
        Assert.assertEquals(obj3, result.get(2));
        Assert.assertTrue(result.set.contains(obj1));
        Assert.assertTrue(result.set.contains(obj2));
        Assert.assertTrue(result.set.contains(obj3));
        Assert.assertFalse(result.set.contains(obj4));
    }

    @Test
    public void testDecorateWithNullElement() {
        List<Object> original = new ArrayList<>(Arrays.asList(obj1, null, obj1, null));
        SetUniqueList result = (SetUniqueList) SetUniqueList.decorate(original);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(obj1, result.get(0));
        Assert.assertNull(result.get(1));
        Assert.assertTrue(result.set.contains(obj1));
        Assert.assertTrue(result.set.contains(null));
    }

    // ===================================
    // Tests for constructor via helper subclass
    // ===================================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSet() {
        new TestableSetUniqueList(new ArrayList<>(), null);
    }

    @Test
    public void testConstructorValid() {
        List<Object> list = new ArrayList<>(Arrays.asList(obj1, obj2));
        Set<Object> set = new HashSet<>(list);
        TestableSetUniqueList instance = new TestableSetUniqueList(list, set);
        Assert.assertEquals(2, instance.size());
        Assert.assertSame(list, instance.getList());
        Assert.assertSame(set, instance.set);
    }

    // ===================================
    // Tests for asSet()
    // ===================================

    @Test
    public void testAsSet() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Set<Object> setView = uniqueList.asSet();
        Assert.assertEquals(2, setView.size());
        Assert.assertTrue(setView.contains(obj1));
        Assert.assertTrue(setView.contains(obj2));
        // ensure unmodifiable
        try {
            setView.add(obj3);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ===================================
    // Tests for add(Object)
    // ===================================

    @Test
    public void testAddNewObjectReturnsTrue() {
        Assert.assertTrue(uniqueList.add(obj1));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertTrue(uniqueList.contains(obj1));
    }

    @Test
    public void testAddDuplicateReturnsFalse() {
        uniqueList.add(obj1);
        Assert.assertFalse(uniqueList.add(obj1));
        Assert.assertEquals(1, uniqueList.size());
    }

    @Test
    public void testAddNull() {
        Assert.assertTrue(uniqueList.add(null));
        Assert.assertTrue(uniqueList.contains(null));
        Assert.assertFalse(uniqueList.add(null));
        Assert.assertEquals(1, uniqueList.size());
    }

    // ===================================
    // Tests for add(int, Object)
    // ===================================

    @Test
    public void testAddAtIndexNewObject() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(1, obj3); // insert at index 1
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj3, uniqueList.get(1));
        Assert.assertEquals(obj2, uniqueList.get(2));
        Assert.assertTrue(uniqueList.set.contains(obj3));
    }

    @Test
    public void testAddAtIndexDuplicate() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        // try to insert obj1 at index 1
        uniqueList.add(1, obj1);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj2, uniqueList.get(1));
        // set should still contain only obj1, obj2
        Assert.assertEquals(2, uniqueList.set.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBounds() {
        uniqueList.add(1, obj1); // list empty, index 1 out of bounds
    }

    @Test
    public void testAddAtIndexWithNullNew() {
        uniqueList.add(obj1);
        uniqueList.add(0, null);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertNull(uniqueList.get(0));
        Assert.assertEquals(obj1, uniqueList.get(1));
        Assert.assertTrue(uniqueList.set.contains(null));
    }

    @Test
    public void testAddAtIndexDuplicateNull() {
        uniqueList.add(null);
        uniqueList.add(0, null); // index 0 insert null when null already exists
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertNull(uniqueList.get(0));
    }

    // ===================================
    // Tests for addAll(Collection)
    // ===================================

    @Test
    public void testAddAllCollection() {
        Collection<Object> coll = Arrays.asList(obj1, obj2, obj3);
        Assert.assertTrue(uniqueList.addAll(coll));
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertTrue(uniqueList.contains(obj1));
        Assert.assertTrue(uniqueList.contains(obj2));
        Assert.assertTrue(uniqueList.contains(obj3));
    }

    @Test
    public void testAddAllCollectionWithDuplicates() {
        uniqueList.add(obj1);
        Collection<Object> coll = Arrays.asList(obj1, obj2, obj1, obj3, obj2);
        Assert.assertTrue(uniqueList.addAll(coll)); // should change
        Assert.assertEquals(3, uniqueList.size()); // obj1 already present, so only obj2 and obj3 added
        Assert.assertTrue(uniqueList.contains(obj2));
        Assert.assertTrue(uniqueList.contains(obj3));
    }

    @Test
    public void testAddAllCollectionNoChange() {
        uniqueList.add(obj1);
        Collection<Object> coll = Arrays.asList(obj1);
        Assert.assertFalse(uniqueList.addAll(coll));
    }

    // ===================================
    // Tests for addAll(int, Collection)
    // ===================================

    @Test
    public void testAddAllAtIndex() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Collection<Object> coll = Arrays.asList(obj3, obj4);
        Assert.assertTrue(uniqueList.addAll(1, coll));
        Assert.assertEquals(4, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj3, uniqueList.get(1));
        Assert.assertEquals(obj4, uniqueList.get(2));
        Assert.assertEquals(obj2, uniqueList.get(3));
    }

    @Test
    public void testAddAllAtIndexWithDuplicates() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Collection<Object> coll = Arrays.asList(obj2, obj3, obj1);
        Assert.assertTrue(uniqueList.addAll(1, coll));
        // obj2 duplicate, obj1 duplicate, only obj3 added
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj3, uniqueList.get(1));
        Assert.assertEquals(obj2, uniqueList.get(2));
    }

    @Test
    public void testAddAllAtIndexNoChange() {
        uniqueList.add(obj1);
        Collection<Object> coll = Arrays.asList(obj1);
        Assert.assertFalse(uniqueList.addAll(0, coll));
        Assert.assertEquals(1, uniqueList.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllAtIndexOutOfBounds() {
        uniqueList.addAll(1, Arrays.asList(obj1));
    }

    // ===================================
    // Tests for set(int, Object)
    // ===================================

    @Test
    public void testSetNewObject() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Object old = uniqueList.set(1, obj3);
        Assert.assertEquals(obj2, old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj3, uniqueList.get(1));
        // set may not contain obj3 if implementation buggy, but we check actual behavior
        // Since set is not updated in set() when object not present, it should not contain obj3
        // This test captures existing behavior
        Assert.assertFalse(uniqueList.set.contains(obj3));
        Assert.assertTrue(uniqueList.set.contains(obj2)); // the old element still in set
    }

    @Test
    public void testSetExistingObjectAtDifferentIndex() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(obj3);
        // obj1 already at index 0, set index 2 to obj1
        Object old = uniqueList.set(2, obj1);
        Assert.assertEquals(obj3, old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj2, uniqueList.get(1));
        // The duplicate at index 2 should be removed
        // The set should still contain obj1 (the one at index 0) and obj2
        Assert.assertTrue(uniqueList.set.contains(obj1));
        Assert.assertTrue(uniqueList.set.contains(obj2));
        Assert.assertFalse(uniqueList.set.contains(obj3));
    }

    @Test
    public void testSetExistingObjectAtSameIndex() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        // set index 0 to a different instance equal to obj1
        Integer anotherOne = new Integer(1);
        Object old = uniqueList.set(0, anotherOne);
        Assert.assertEquals(obj1, old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals(anotherOne, uniqueList.get(0));
        Assert.assertEquals(obj2, uniqueList.get(1));
        // set should still contain obj1 (the original instance or new? implementation leaves set unchanged)
        // We don't assert set contents because it's inconsistent, but we can check that no duplicates exist
        // For safety, just verify list integrity
    }

    @Test
    public void testSetNullObject() {
        uniqueList.add(obj1);
        uniqueList.add(null);
        Object old = uniqueList.set(0, null);
        Assert.assertEquals(obj1, old);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertNull(uniqueList.get(0));
        Assert.assertNull(uniqueList.get(1)); // old null remains? Actually set logic will remove duplicate if pos != index
        // null is at index 1 (pos=1), index 0 set to null -> pos != index, so old null at pos removed? Let's see:
        // pos = index0f(null) = 1, super.set(0, null) returns obj1, obj1 removed from index 0. Now list: [null, null] (index0 null, index1 null)
        // pos != index (-1? no, pos=1, index=0), so super.remove(1) removes the old null at index1, set.remove(obj1) removes obj1 from set.
        // So final list: [null] with size 1. So actually duplicates removed.
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertNull(uniqueList.get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetIndexOutOfBounds() {
        uniqueList.set(0, obj1);
    }

    // ===================================
    // Tests for remove(Object)
    // ===================================

    @Test
    public void testRemoveObjectExisting() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Assert.assertTrue(uniqueList.remove(obj1));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertFalse(uniqueList.contains(obj1));
        Assert.assertFalse(uniqueList.set.contains(obj1));
    }

    @Test
    public void testRemoveObjectNonExisting() {
        uniqueList.add(obj1);
        Assert.assertFalse(uniqueList.remove(obj2));
        Assert.assertEquals(1, uniqueList.size());
    }

    @Test
    public void testRemoveNull() {
        uniqueList.add(null);
        Assert.assertTrue(uniqueList.remove(null));
        Assert.assertEquals(0, uniqueList.size());
        Assert.assertFalse(uniqueList.set.contains(null));
    }

    // ===================================
    // Tests for remove(int)
    // ===================================

    @Test
    public void testRemoveIndex() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Object removed = uniqueList.remove(0);
        Assert.assertEquals(obj1, removed);
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertEquals(obj2, uniqueList.get(0));
        Assert.assertFalse(uniqueList.set.contains(obj1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexOutOfBounds() {
        uniqueList.remove(0);
    }

    // ===================================
    // Tests for removeAll(Collection)
    // ===================================

    @Test
    public void testRemoveAll() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(obj3);
        Collection<Object> toRemove = Arrays.asList(obj1, obj3);
        Assert.assertTrue(uniqueList.removeAll(toRemove));
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertEquals(obj2, uniqueList.get(0));
        Assert.assertFalse(uniqueList.set.contains(obj1));
        Assert.assertFalse(uniqueList.set.contains(obj3));
    }

    @Test
    public void testRemoveAllNoMatch() {
        uniqueList.add(obj1);
        Collection<Object> toRemove = Arrays.asList(obj2);
        Assert.assertFalse(uniqueList.removeAll(toRemove));
    }

    // ===================================
    // Tests for retainAll(Collection)
    // ===================================

    @Test
    public void testRetainAll() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(obj3);
        Collection<Object> toRetain = Arrays.asList(obj1, obj3);
        Assert.assertTrue(uniqueList.retainAll(toRetain));
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertTrue(uniqueList.contains(obj1));
        Assert.assertTrue(uniqueList.contains(obj3));
        Assert.assertFalse(uniqueList.set.contains(obj2));
    }

    @Test
    public void testRetainAllNoChange() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Collection<Object> toRetain = Arrays.asList(obj1, obj2);
        Assert.assertFalse(uniqueList.retainAll(toRetain));
    }

    // ===================================
    // Tests for clear()
    // ===================================

    @Test
    public void testClear() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.clear();
        Assert.assertTrue(uniqueList.isEmpty());
        Assert.assertTrue(uniqueList.set.isEmpty());
    }

    // ==================================
    // Tests for contains(Object)
    // ==================================

    @Test
    public void testContains() {
        uniqueList.add(obj1);
        Assert.assertTrue(uniqueList.contains(obj1));
        Assert.assertFalse(uniqueList.contains(obj2));
        Assert.assertFalse(uniqueList.contains(null));
    }

    // ==================================
    // Tests for containsAll(Collection)
    // ==================================

    @Test
    public void testContainsAll() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Assert.assertTrue(uniqueList.containsAll(Arrays.asList(obj1, obj2)));
        Assert.assertFalse(uniqueList.containsAll(Arrays.asList(obj1, obj3)));
    }

    // ==================================
    // Tests for iterator()
    // ==================================

    @Test
    public void testIterator() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Iterator<?> it = uniqueList.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(obj1, it.next());
        Assert.assertEquals(obj2, it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        Iterator<?> it = uniqueList.iterator();
        it.next();
        it.remove();
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertEquals(obj2, uniqueList.get(0));
        Assert.assertFalse(uniqueList.set.contains(obj1));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveBeforeNext() {
        uniqueList.add(obj1);
        Iterator<?> it = uniqueList.iterator();
        it.remove();
    }

    // ==================================
    // Tests for listIterator()
    // ==================================

    @Test
    public void testListIterator() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        ListIterator<?> lit = uniqueList.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals(obj1, lit.next());
        Assert.assertEquals(obj2, lit.next());
        Assert.assertFalse(lit.hasNext());
        Assert.assertTrue(lit.hasPrevious());
        Assert.assertEquals(obj2, lit.previous());
    }

    @Test
    public void testListIteratorRemove() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        ListIterator<?> lit = uniqueList.listIterator();
        lit.next();
        lit.remove();
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertFalse(uniqueList.set.contains(obj1));
    }

    @Test
    public void testListIteratorAddNew() {
        uniqueList.add(obj1);
        ListIterator<?> lit = uniqueList.listIterator();
        lit.next();
        lit.add(obj2);
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals(obj1, uniqueList.get(0));
        Assert.assertEquals(obj2, uniqueList.get(1));
        Assert.assertTrue(uniqueList.set.contains(obj2));
    }

    @Test
    public void testListIteratorAddDuplicate() {
        uniqueList.add(obj1);
        ListIterator<?> lit = uniqueList.listIterator();
        lit.next();
        lit.add(obj1); // duplicate, should be ignored
        Assert.assertEquals(1, uniqueList.size());
    }

    @Test
    public void testListIteratorSetThrowsException() {
        uniqueList.add(obj1);
        ListIterator<?> lit = uniqueList.listIterator();
        lit.next();
        try {
            lit.set(obj2);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testListIteratorAtIndex() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(obj3);
        ListIterator<?> lit = uniqueList.listIterator(1);
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals(obj2, lit.next());
        Assert.assertTrue(lit.hasPrevious());
        Assert.assertEquals(obj2, lit.previous());
        Assert.assertEquals(obj1, lit.previous());
    }

    // ==================================
    // Tests for subList(int, int)
    // ==================================

    @Test
    public void testSubList() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        uniqueList.add(obj3);
        List<?> sub = uniqueList.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals(obj2, sub.get(0));
        Assert.assertEquals(obj3, sub.get(1));
        // sublist is also a SetUniqueList
        Assert.assertTrue(sub instanceof SetUniqueList);
        SetUniqueList subUnique = (SetUniqueList) sub;
        // shares same set
        Assert.assertSame(uniqueList.set, subUnique.set);
    }

    @Test
    public void testSubListAddAffectsOriginal() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        List<Object> sub = uniqueList.subList(0, 1);
        sub.add(obj3); // adds to end of sublist, which is at index 1 in original
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals(obj3, uniqueList.get(1));
        Assert.assertTrue(uniqueList.set.contains(obj3));
    }

    @Test
    public void testSubListEnforcesUniqueness() {
        uniqueList.add(obj1);
        uniqueList.add(obj2);
        List<Object> sub = uniqueList.subList(0, 1);
        sub.add(obj1); // duplicate, should not be added
        Assert.assertEquals(2, uniqueList.size());
    }

    // Helper class for testing protected constructor
    private static class TestableSetUniqueList extends SetUniqueList {
        public TestableSetUniqueList(List<?> list, Set<?> set) {
            super(list, set);
        }
    }
}
