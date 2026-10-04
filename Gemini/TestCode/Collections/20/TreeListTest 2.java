package org.apache.commons.collections.list;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class TreeListTest {

    private TreeList<String> list;

    @Before
    public void setUp() {
        list = new TreeList<String>();
    }

    @Test
    public void testEmptyList() {
        Assert.assertEquals(0, list.size());
        Assert.assertTrue(list.isEmpty());
        Assert.assertFalse(list.contains("A"));
        Assert.assertEquals(-1, list.indexOf("A"));
        Assert.assertArrayEquals(new Object[0], list.toArray());
    }

    @Test
    public void testConstructorWithCollection() {
        Collection<String> coll = Arrays.asList("A", "B", "C");
        TreeList<String> copy = new TreeList<String>(coll);
        Assert.assertEquals(3, copy.size());
        Assert.assertEquals("A", copy.get(0));
        Assert.assertEquals("B", copy.get(1));
        Assert.assertEquals("C", copy.get(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCollection() {
        new TreeList<String>((Collection<String>) null);
    }

    @Test
    public void testAddAndGet() {
        list.add("A");
        list.add("B");
        list.add("C");

        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testAddAtIndex() {
        list.add(0, "A");
        list.add(0, "B"); // B, A
        list.add(1, "C"); // B, C, A
        list.add(3, "D"); // B, C, A, D

        Assert.assertEquals(4, list.size());
        Assert.assertEquals("B", list.get(0));
        Assert.assertEquals("C", list.get(1));
        Assert.assertEquals("A", list.get(2));
        Assert.assertEquals("D", list.get(3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsNegative() {
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsEmpty() {
        list.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsPositive() {
        list.add("A");
        list.get(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddOutOfBoundsNegative() {
        list.add(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddOutOfBoundsPositive() {
        list.add(1, "A");
    }

    @Test
    public void testSet() {
        list.add("A");
        list.add("B");
        list.add("C");

        String oldVal = list.set(1, "X");
        Assert.assertEquals("B", oldVal);
        Assert.assertEquals("X", list.get(1));
        Assert.assertEquals(3, list.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetOutOfBoundsNegative() {
        list.set(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetOutOfBoundsSize() {
        list.add("A");
        list.set(1, "B");
    }

    @Test
    public void testRemoveByIndex() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        String removed = list.remove(1);
        Assert.assertEquals("B", removed);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("C", list.get(1));
        Assert.assertEquals("D", list.get(2));

        removed = list.remove(0);
        Assert.assertEquals("A", removed);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("C", list.get(0));
        Assert.assertEquals("D", list.get(1));

        removed = list.remove(1);
        Assert.assertEquals("D", removed);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("C", list.get(0));

        removed = list.remove(0);
        Assert.assertEquals("C", removed);
        Assert.assertEquals(0, list.size());
        Assert.assertTrue(list.isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBoundsNegative() {
        list.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBoundsSize() {
        list.add("A");
        list.remove(1);
    }

    @Test
    public void testClear() {
        list.add("A");
        list.add("B");
        list.clear();

        Assert.assertEquals(0, list.size());
        Assert.assertEquals(-1, list.indexOf("A"));
        Assert.assertArrayEquals(new Object[0], list.toArray());
    }

    @Test
    public void testIndexOfAndContains() {
        Assert.assertEquals(-1, list.indexOf("A"));
        Assert.assertFalse(list.contains("A"));

        list.add("A");
        list.add("B");
        list.add(null);
        list.add("C");
        list.add("A");

        Assert.assertTrue(list.contains("A"));
        Assert.assertTrue(list.contains("B"));
        Assert.assertTrue(list.contains(null));
        Assert.assertFalse(list.contains("Z"));

        Assert.assertEquals(0, list.indexOf("A"));
        Assert.assertEquals(1, list.indexOf("B"));
        Assert.assertEquals(2, list.indexOf(null));
        Assert.assertEquals(3, list.indexOf("C"));
        Assert.assertEquals(-1, list.indexOf("Z"));
    }

    @Test
    public void testToArray() {
        list.add("A");
        list.add("B");
        list.add("C");

        Object[] array = list.toArray();
        Assert.assertArrayEquals(new Object[]{"A", "B", "C"}, array);
    }

    @Test
    public void testRotationsDuringInsert() {
        // Test RR Rotation (Single Left)
        TreeList<Integer> rrList = new TreeList<Integer>();
        rrList.add(1);
        rrList.add(2);
        rrList.add(3); // triggers rotateLeft
        Assert.assertEquals(Arrays.asList(1, 2, 3), rrList);

        // Test LL Rotation (Single Right)
        TreeList<Integer> llList = new TreeList<Integer>();
        llList.add(0, 3);
        llList.add(0, 2);
        llList.add(0, 1); // triggers rotateRight
        Assert.assertEquals(Arrays.asList(1, 2, 3), llList);

        // Test LR Rotation (Double Left-Right)
        TreeList<Integer> lrList = new TreeList<Integer>();
        lrList.add(3);
        lrList.add(0, 1);
        lrList.add(1, 2); // triggers LR rotation
        Assert.assertEquals(Arrays.asList(1, 2, 3), lrList);

        // Test RL Rotation (Double Right-Left)
        TreeList<Integer> rlList = new TreeList<Integer>();
        rlList.add(1);
        rlList.add(3);
        rlList.add(1, 2); // triggers RL rotation
        Assert.assertEquals(Arrays.asList(1, 2, 3), rlList);
    }

    @Test
    public void testLargeScaleInsertionsAndRemovals() {
        TreeList<Integer> tList = new TreeList<Integer>();
        List<Integer> refList = new ArrayList<Integer>();

        // Insert at beginning, end, and middle
        for (int i = 0; i < 200; i++) {
            int insertIndex;
            if (i % 3 == 0) {
                insertIndex = 0;
            } else if (i % 3 == 1) {
                insertIndex = tList.size();
            } else {
                insertIndex = tList.size() / 2;
            }
            tList.add(insertIndex, i);
            refList.add(insertIndex, i);
        }

        Assert.assertEquals(refList.size(), tList.size());
        for (int i = 0; i < refList.size(); i++) {
            Assert.assertEquals(refList.get(i), tList.get(i));
        }

        // Search each element
        for (int i = 0; i < 200; i++) {
            Assert.assertEquals(refList.indexOf(i), tList.indexOf(i));
        }

        // Remove from beginning, end, and middle
        while (!tList.isEmpty()) {
            int removeIndex;
            if (tList.size() % 3 == 0) {
                removeIndex = 0;
            } else if (tList.size() % 3 == 1) {
                removeIndex = tList.size() - 1;
            } else {
                removeIndex = tList.size() / 2;
            }
            Integer expected = refList.remove(removeIndex);
            Integer actual = tList.remove(removeIndex);
            Assert.assertEquals(expected, actual);
            Assert.assertEquals(refList.size(), tList.size());
        }
    }

    @Test
    public void testRemoveSelfWithSingleChild() {
        // Construct tree with only left subchild or right subchild scenarios
        TreeList<Integer> tList = new TreeList<Integer>();
        tList.add(10);
        tList.add(5);
        // Node 10 has left child 5, right child null
        tList.remove(0); // removes 5, root remains 10
        Assert.assertEquals(1, tList.size());
        Assert.assertEquals(Integer.valueOf(10), tList.get(0));

        tList.add(15);
        // Node 10 has left null, right child 15
        tList.remove(0); // removes 10, replaces with right child 15
        Assert.assertEquals(1, tList.size());
        Assert.assertEquals(Integer.valueOf(15), tList.get(0));

        tList.remove(0);
        Assert.assertEquals(0, tList.size());
    }

    @Test
    public void testRemoveBalancedSubtreeCases() {
        // Test removing nodes when height difference causes removal from right/left
        TreeList<Integer> tList = new TreeList<Integer>();
        for (int i = 0; i < 15; i++) {
            tList.add(i);
        }

        // Remove internal nodes that have two children
        tList.remove(7);
        tList.remove(3);
        tList.remove(10);

        Assert.assertEquals(12, tList.size());
        List<Integer> expected = Arrays.asList(0, 1, 2, 4, 5, 6, 8, 9, 11, 12, 13, 14);
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), tList.get(i));
        }
    }

    @Test
    public void testIteratorBasic() {
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<String> it = list.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() {
        Iterator<String> it = list.iterator();
        it.next();
    }

    @Test
    public void testListIteratorBidirectional() {
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator<String> it = list.listIterator();
        Assert.assertFalse(it.hasPrevious());
        Assert.assertEquals(0, it.nextIndex());
        Assert.assertEquals(-1, it.previousIndex());

        Assert.assertEquals("A", it.next());
        Assert.assertEquals(1, it.nextIndex());
        Assert.assertEquals(0, it.previousIndex());
        Assert.assertTrue(it.hasPrevious());

        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals("C", it.previous());
        Assert.assertEquals("B", it.previous());
        Assert.assertEquals("A", it.previous());
        Assert.assertFalse(it.hasPrevious());
    }

    @Test(expected = NoSuchElementException.class)
    public void testListIteratorPreviousNoSuchElement() {
        ListIterator<String> it = list.listIterator();
        it.previous();
    }

    @Test
    public void testListIteratorFromIndex() {
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator<String> it = list.listIterator(1);
        Assert.assertEquals(1, it.nextIndex());
        Assert.assertEquals(0, it.previousIndex());
        Assert.assertEquals("B", it.next());

        ListIterator<String> itEnd = list.listIterator(3);
        Assert.assertFalse(itEnd.hasNext());
        Assert.assertTrue(itEnd.hasPrevious());
        Assert.assertEquals("C", itEnd.previous());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorFromIndexOutOfBoundsNegative() {
        list.listIterator(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorFromIndexOutOfBoundsPositive() {
        list.listIterator(1);
    }

    @Test
    public void testListIteratorRemoveAfterNext() {
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // removes B

        Assert.assertEquals(2, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("C", list.get(1));
        Assert.assertEquals(1, it.nextIndex());
        Assert.assertEquals("C", it.next());
    }

    @Test
    public void testListIteratorRemoveAfterPrevious() {
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator<String> it = list.listIterator(3);
        it.previous(); // C
        it.remove(); // removes C

        Assert.assertEquals(2, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertFalse(it.hasNext());
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("B", it.previous());
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorRemoveWithoutNextOrPrevious() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorDoubleRemove() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testListIteratorSet() {
        list.add("A");
        list.add("B");

        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("Z");
        Assert.assertEquals("Z", list.get(0));

        it.next();
        it.previous();
        it.set("Y");
        Assert.assertEquals("Y", list.get(1));
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorSetWithoutNextOrPrevious() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.set("Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorSetAfterRemove() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        it.set("Z");
    }

    @Test
    public void testListIteratorAdd() {
        list.add("A");
        list.add("C");

        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.add("B"); // inserted after A, before C

        Assert.assertEquals(3, list.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
        Assert.assertEquals(2, it.nextIndex());
        Assert.assertEquals("C", it.next());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationNext() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        list.add("B");
        it.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationPrevious() {
        list.add("A");
        ListIterator<String> it = list.listIterator(1);
        list.add("B");
        it.previous();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationRemove() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.next();
        list.add("B");
        it.remove();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationSet() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        it.next();
        list.add("B");
        it.set("Z");
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationAdd() {
        list.add("A");
        ListIterator<String> it = list.listIterator();
        list.add("B");
        it.add("Z");
    }

    @Test
    public void testAVLNodeToString() {
        list.add("A");
        list.add("B");
        list.add("C");
        // Access iterator to get a node and call its toString()
        TreeList.TreeListIterator<String> it = (TreeList.TreeListIterator<String>) list.listIterator();
        it.next();
        Assert.assertNotNull(it.current.toString());
        Assert.assertTrue(it.current.toString().contains("AVLNode"));
    }
}
