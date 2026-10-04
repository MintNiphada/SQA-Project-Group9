package org.apache.commons.collections.list;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class TreeListTest {

    @Test
    public void testEmptyList() {
        TreeList<String> list = new TreeList<String>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.iterator().hasNext());
        assertFalse(list.listIterator().hasNext());
        assertFalse(list.listIterator(0).hasNext());
        assertEquals(0, list.toArray().length);
        assertEquals(-1, list.indexOf("anything"));
        assertFalse(list.contains("anything"));
        try {
            list.get(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test    public void testConstructorWithCollection() {
        List<Integer> input = Arrays.asList(1, 2, 3);
        TreeList<Integer> list = new TreeList<Integer>(input);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCollection() {
        new TreeList<Object>((Collection<Object>) null);
    }

    @Test    public void testAddAtIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add(0, "first");
        list.add(1, "second");
        list.add(0, "newFirst");
        list.add(list.size(), "last");
        assertEquals(4, list.size());
        assertEquals("newFirst", list.get(0));
        assertEquals("first", list.get(1));
        assertEquals("second", list.get(2));
        assertEquals("last", list.get(3));
    }

    @Test    public void testAddAtEndUsingAddMethod() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(10);
        list.add(20);
        list.add(30);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        assertEquals(Integer.valueOf(30), list.get(2));
        // add at index size is equivalent to add at end
        list.add(list.size(), 40);
        assertEquals(Integer.valueOf(40), list.get(3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtNegativeIndex() {
        TreeList<Object> list = new TreeList<Object>();
        list.add(-1, "bad");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexGreaterThanSize() {
        TreeList<Object> list = new TreeList<Object>();
        list.add(1, "bad");
    }

    @Test    public void testGetInvalidIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        try {
            list.get(-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            list.get(1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test    public void testSet() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        String old = list.set(0, "A");
        assertEquals("a", old);
        assertEquals("A", list.get(0));
        assertEquals("b", list.get(1));
        old = list.set(1, "B");
        assertEquals("b", old);
        assertEquals("B", list.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetInvalidIndex() {
        TreeList<String> list = new TreeList<String>();
        list.set(0, "fail");
    }

    @Test expected = IndexOutOfBoundsException.class)
    public void testSetNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.set(-1, "fail");
    }

    @Test    public void testRemoveByIndex() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));
        Integer removed = list.remove(2);
        assertEquals(Integer.valueOf(3), removed);
        assertEquals(4, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(4), list.get(2));
        assertEquals(Integer.valueOf(5), list.get(3));
        // remove first
        removed = list.remove(0);
        assertEquals(Integer.valueOf(1), removed);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        // remove last
        removed = list.remove(list.size() - 1);
        assertEquals(Integer.valueOf(5), removed);
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(4), list.get(1));
        // remove all
        list.remove(0);
        list.remove(0);
        assertEquals(0, list.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveNegativeIndex() {
        TreeList<Object> list = new TreeList<Object>();
        list.add("a");
        list.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexTooHigh() {
        TreeList<Object> list = new TreeList<Object>();
        list.add("a");
        list.remove(1);
    }

    @Test    public void testRemoveOnEmptyList() {
        TreeList<Object> list = new TreeList<Object>();
        try {
            list.remove(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test    public void testClear() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertNull(list.root);
        // after clear, can still add
        list.add("c");
        assertEquals(1, list.size());
        assertEquals("c", list.get(0));
    }

    @Test    public void testContainsAndIndexOf() {
        TreeList<String> list = new TreeList<String>();
        assertFalse(list.contains("null"));
        list.add("a");
        list.add("b");
        list.add("a");
        assertTrue(list.contains("a"));
        assertEquals(0, list.indexOf("a"));
        assertFalse(list.contains("c"));
        assertEquals(-1, list.indexOf("c"));
        // null handling
        list.add(null);
        assertTrue(list.contains(null));
        assertEquals(3, list.indexOf(null));
    }

    @Test    public void testToArray() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        Object[] array = list.toArray();
        assertEquals(3, array.length);
        assertArrayEquals(new Object[]{1, 2, 3}, array);
        // modifying array does not affect list
        array[0] = 99;
        assertEquals(Integer.valueOf(1), list.get(0));
    }

    @Test    public void testIterator() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test    public void testListIteratorForward() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        ListIterator<String> it = list.listIterator();
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        assertEquals("a", it.next());
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(3, it.nextIndex());
        assertEquals(2, it.previousIndex());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test    public void testListIteratorBackward() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        ListIterator<String> it = list.listIterator(list.size());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(3, it.nextIndex());
        assertEquals(2, it.previousIndex());
        assertEquals("c", it.previous());
        assertEquals("b", it.previous());
        assertEquals("a", it.previous());
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        try {
            it.previous();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test    public void testListIteratorNextAndPreviousCrossing() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        ListIterator<Integer> it = list.listIterator();
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(2), it.previous());
        assertEquals(Integer.valueOf(1), it.previous());
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
    }

    @Test    public void testIteratorRemove() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        Iterator<Integer> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        // state after remove: current is null
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(3), list.get(0));
    }

    @Test    public void testListIteratorRemoveAfterNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        assertEquals(2, list.size());
        assertEquals("b", list.get(0));
        assertEquals("c", list.get(1));
        // index decreased
        assertEquals(0, it.nextIndex());
        // after remove, next should still be the element that was at nextIndex (which is 0, element "b")
        assertEquals("b", it.next());
        // now remove previous element (which is "b")
        it.remove();
        assertEquals(1, list.size());
        assertEquals("c", list.get(0));
    }

    @Test    public void testListIteratorRemoveAfterPrevious() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        ListIterator<String> it = list.listIterator(list.size());
        assertEquals("c", it.previous());
        it.remove();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        // nextIndex is still 2? Actually after removing previous, nextIndex should be index of the element that was returned by previous (which was 2 originally, but after removal, elements shift, nextIndex becomes 2? Let's see: previous returned element at index 2, currentIndex was 2, nextIndex remains 2 (since nextIndex = currentIndex + 1? Actually the code sets currentIndex = --nextIndex; so nextIndex was 3 before previous, becomes 2. After remove, nextIndex stays 2? In implementation, remove after previous does: parent.remove(currentIndex); then nextIndex-- (if not after next). So nextIndex-- makes it 1. So it should be 1. Let's test.
        assertEquals(1, it.nextIndex());
        assertEquals("b", it.previous());
    }

    @Test    public void testIteratorAdd() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(3);
        ListIterator<Integer> it = list.listIterator();
        it.next(); // move to 1
        it.add(2);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
        // add sets current to null
        try {
            it.set(99);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        // adding increments nextIndex
        assertEquals(2, it.nextIndex());
        assertEquals(Integer.valueOf(3), it.next());
        // add at beginning
        it = list.listIterator();
        it.add(0);
        assertEquals(1, it.nextIndex()); // nextIndex increment after add
        assertEquals(Integer.valueOf(1), it.next());
    }

    @Test    public void testIteratorSet() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("A");
        assertEquals("A", list.get(0));
        assertEquals("A", it.previous()); // moving back, set still?
       // can set again?
        it.set("AA");
        assertEquals("AA", list.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorSetWithoutNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        ListIterator<String> it = list.listIterator();
        it.set("fail");
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorNextAfterModification() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        list.add("c"); // modifies modCount
        it.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorRemoveAfterModification() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        list.remove(0);
        it.remove();
    }

    @Test    public void testConcurrentModificationOnIteratorAdd() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        ListIterator<String> it = list.listIterator();
        it.next();
        list.add("c"); // modify
        try {
            it.add("d");
            fail("Expected ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }

    @Test    public void testConcurrentModificationOnIteratorSet() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        ListIterator<String> it = list.listIterator();
        it.next();
        list.add("c");
        try {
            it.set("x");
            fail("Expected ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }

    @Test    public void testAddManyElementsSequential() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test    public void testAddManyElementsAtRandomPositions() {
        TreeList<Integer> list = new TreeList<Integer>();
        java.util.Random random = new java.util.Random(12345);
        List<Integer> allElements = new ArrayList<Integer>();
        for (int i = 0; i < 200; i++) {
            int index = list.isEmpty() ? 0 : random.nextInt(list.size() + 1);
            list.add(index, i);
            allElements.add(index, i);
        }
        assertEquals(allElements.size(), list.size());
        for (int i = 0; i < allElements.size(); i++) {
            assertEquals(allElements.get(i), list.get(i));
        }
    }

    @Test    public void testRemoveManyElementsFromRandomPositions() {
        TreeList<Integer> list = new TreeList<Integer>();
        java.util.Random random = new java.util.Random(54321);
        List<Integer> allElements = new ArrayList<Integer>();
        // fill list with sequential numbers
        for (int i = 0; i < 100; i++) {
            allElements.add(i);
            list.add(i);
        }
        while (!list.isEmpty()) {
            int idx = random.nextInt(list.size());
            Integer expected = allElements.remove(idx);
            Integer removed = list.remove(idx);
            assertEquals(expected, removed);
            assertEquals(allElements.size(), list.size());
            for (int i = 0; i < allElements.size(); i++) {
                assertEquals(allElements.get(i), list.get(i));
            }
        }
    }

    @Test    public void testTreeRotationBalance() {
        // Insert elements in decreasing order to trigger rotations
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 100; i >= 1; i--) {
            list.add(0, i);
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i + 1), list.get(i));
        }
        // Remove elements from beginning to test rotation
        for (int i =0; i <50; i++) {
            list.remove(0);
        }
        for (int i = 0; i <50; i++) {
            assertEquals(Integer.valueOf(i + 51), list.get(i));
        }
    }

    @Test    public void testListIteratorFromIndex() {
        TreeList<String> list = new TreeList<String>();
        list.addAll(Arrays.asList("a", "b", "c", "d", "e"));
        ListIterator<String> it = list.listIterator(2);
        assertEquals(2, it.nextIndex());
        assertEquals("c", it.next());
        assertEquals(-1, it.previousIndex());
        assertEquals("b", it.previous());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorFromInvalidNegativeIndex() {
        new TreeList<String>().listIterator(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorFromIndexGreaterThanSize() {
        new TreeList<String>().listIterator(1);
    }

    @Test    public void testLargeTreeOperations() {
        int size = 500;
        TreeList<Integer> list = new TreeList<Integer>();
        // insert at middle alternating
        for (int i = 0; i < size; i++) {
            list.add(list.size() / 2, i);
        }
        assertEquals(size, list.size());
        // check order
        List<Integer> expected = new ArrayList<Integer>(list);
        for (int i =0; i < size; i++) {
            assertEquals(expected.get(i), list.get(i));
        }
        // remove from middle
        for (int i =0; i < size /2; i++) {
            list.remove(list.size() / 2);
            expected.remove(expected.size() / 2);
        }
        assertEquals(expected.size(), list.size());
        for (int i =0; i < expected.size(); i++) {
            assertEquals(expected.get(i), list.get(i));
        }
    }

    @Test    public void testToArrayWithElements() {
        TreeList<Integer> list = new TreeList<Integer>();
        Object[] empty = list.toArray();
        assertNotNull(empty);
        assertEquals(0, empty.length);
        list.add(1);
        list.add(2);
        Object[] array = list.toArray();
        assertEquals(2, array.length);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
    }

    @Test    public void testIndexOfEdgeCases() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add(null);
        list.add("b");
        list.add(null);
        list.add("a");
        assertEquals(0, list.indexOf("a"));
        assertEquals(1, list.indexOf(null));
        assertEquals(-1, list.indexOf("c"));
        // contains
        assertTrue(list.contains(null));
    }

    @Test    public void testIteratorHasNextOnEmpty() {
        Iterator<Object> it = new TreeList<Object>().iterator();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
        }
    }

    @Test    public void testListIteratorPreviousFromStart() {
        ListIterator<String> it = new TreeList<String>(Arrays.asList("a")).listIterator();
        assertFalse(it.hasPrevious());
        try {
            it.previous();
            fail();
        } catch (NoSuchElementException e) {
        }
    }

    @Test    public void testRemoveAllElementsViaIterator() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 1; i <= 10; i++) {
            list.add(i);
        }
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertTrue(list.isEmpty());
    }

    @Test    public void testSetAfterRemove() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove(); // removes "a"
        // current becomes null, so set should fail
        try {
            it.set("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test    public void testAddAfterRemove() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        // can add now
        it.add("b");
        assertEquals(1, list.size());
        assertEquals("b", list.get(0));
    }

    @Test    public void testMultipleIteratorOperationsInterleaved() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(10);
        list.add(20);
        list.add(30);
        ListIterator<Integer> it1 = list.listIterator();
        ListIterator<Integer> it2 = list.listIterator();
        // modifications in one will cause concurrent exception in other
        it1.next();
        it1.remove();
        try {
            it2.next();
            fail("Expected ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
        }
    }

    @Test    public void testAddAllAtEndViaAddAll() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.addAll(Arrays.asList(1, 2, 3));
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    @Test(expected = NullPointerException.class)
    public void testAddAllWithNull() {
        new TreeList<Object>().addAll(null);
    }

    @Test    public void testRemoveByObject() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.remove("b");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
        assertFalse(list.remove("z"));
    }

    @Test    public void testRemoveFirstAndLastViaObject() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.remove(Integer.valueOf(1));
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        list.remove(Integer.valueOf(3));
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
    }

    @Test    public void testLargeSequenceOfAddAndRemove() {
        TreeList<String> list = new TreeList<String>();
        for (int i = 0; i < 1000; i++) {
            list.add("" + i);
        }
        assertEquals(1000, list.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals("" + i, list.get(i));
        }
        // remove all even indices
        int originalSize = list.size();
        int removed = 0;
        for (int i = originalSize - 1; i >= 0; i--) {
            if (i % 2 == 0) {
                list.remove(i);
                removed++;
            }
        }
        assertEquals(originalSize - removed, list.size());
        // verify remaining
        int j = 0;
        for (int i = 0; i < originalSize; i++) {
            if (i % 2 != 0) {
                assertEquals("" + i, list.get(j++));
            }
        }
    }

    @Test    public void testMultipleAddAndRemoveAll() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, 0);
        list.add(1, 1);
        list.add(2, 2);
        list.remove(1);
        list.add(1, 3);
        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        assertEquals(Integer.valueOf(2), list.get(2));
        assertEquals(3, list.size());
    }

    @Test    public void testInsertManyAtZero() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 1000; i >= 0; i--) {
            list.add(0, i);
        }
        for (int i = 0; i <= 1000; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test    public void testInsertAtEndAndMiddle() {
        TreeList<String> list = new TreeList<String>();
        list.add("0");
        list.add("1");
        list.add("2");
        list.add(2, "1.5");
        assertEquals(4, list.size());
        assertEquals("0", list.get(0));
        assertEquals("1", list.get(1));
        assertEquals("1.5", list.get(2));
        assertEquals("2", list.get(3));
    }

    @Test    public void testIteratorAfterAddAll() {
        TreeList<Integer> list = new TreeList<Integer>(Arrays.asList(1, 2, 3, 4));
        ListIterator<Integer> it = list.listIterator();
        it.next();
        it.add(99);
        assertEquals(Integer.valueOf(99), list.get(1));
        assertEquals(5, list.size());
        // move forward
        assertEquals(Integer.valueOf(2), it.next());
    }

    @Test    public void testAddAllAndClear() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.addAll(Collections.nCopies(10, 42));
        assertEquals(10, list.size());
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        // can reuse
        list.addAll(Arrays.asList(1, 2));
        assertEquals(2, list.size());
    }

    @Test    public void testRemoveNonExistent() {
        TreeList<String> list = new TreeList<String>(Arrays.asList("a", "b"));
        assertFalse(list.remove("c"));
    }

    @Test    public void testContainsNullWhenEmpty() {
        assertFalse(new TreeList<String>().contains(null));
    }

    // Test for indexOf with null when root is null (covered)
    @Test    public void testIndexOfOnEmpty() {
        assertEquals(-1, new TreeList<String>().indexOf("anything"));
    }

    @Test    public void testIteratorRemoveTwiceWithoutNext() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        Iterator<Integer> it = list.iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
        }
    }

    @Test    public void testListIteratorSetAfterAdd() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.add("b");
        try {
            it.set("c");
            fail();
        } catch (IllegalStateException e) {
        }
    }

    // tests for AVLNode rotation and edge cases with height difference -2 and 2
    @Test    public void testBalanceLeftHeavy() {
        // inserting in decreasing order at beginning should cause left-heavy tree, rotations
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 64; i++) {
            list.add(0, i);
        }
        // verify correct order
        for (int i = 0; i < 64; i++) {
            assertEquals(Integer.valueOf(63 - i), list.get(i));
        }
    }

    @Test    public void testBalanceRightHeavy() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 64; i++) {
            list.add(list.size(), i);
        }
        for (int i = 0; i < 64; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test    public void testRemoveCausingRotation() {
        TreeList<Integer> list = new TreeList<Integer>();
        // build balanced tree
        for (int i = 0; i < 31; i++) {
            list.add(i);
        }
        // remove elements from left side to trigger rotations
        for (int i = 0; i < 15; i++) {
            list.remove(0);
        }
        for (int i = 0; i < list.size(); i++) {
            assertEquals(Integer.valueOf(i + 15), list.get(i));
        }
    }

    @Test    public void testRemoveSelfWithBothChildrnNull() {
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        list.remove(0);
        assertTrue(list.isEmpty());
        // root should be null
    }

    @Test    public void testRemoveSelfWithNoRightChild() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(2);
        // remove leftmost (1) and ensure tree structure correct
        list.remove(0);
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        // remove remaining
        list.remove(0);
        assertTrue(list.isEmpty());
    }

    @Test    public void testRemoveSelfWithNoLeftChild() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(1);
        list.add(0);
        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        list.remove(1); // remove rightmost
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(0), list.get(0));
    }

    @Test    public void testRemoveWithHeightDifferencPositive() {
        // This should trigger the branch heightRightMinusLeft() > 0 inside removeSelf
        TreeList<Integer> list = new TreeList<Integer>();
        // Build tree that is right-heavy after removing some? We'll try.
        list.add(10);
        list.add(20);
        list.add(30);
        list.add(40);
        // Remove a left node causing right-heavy?
        list.remove(0);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(20), list.get(0));
        assertEquals(Integer.valueOf(30), list.get(1));
        assertEquals(Integer.valueOf(40), list.get(2));
    }

    @Test    public void testAVLNodeToString() {
        // just call to ensure no exception
        TreeList<String> list = new TreeList<String>();
        list.add("a");
        assertTrue(list.toString().length() > 0);
    }
}
