package org.apache.commons.collections4;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import java.lang.reflect.Array;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IteratorUtilsTest {

    @Test
    public void testEmptyIterator() {
        ResettableIterator<Object> it = IteratorUtils.emptyIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyListIterator() {
        ResettableListIterator<Object> it = IteratorUtils.emptyListIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testEmptyOrderedIterator() {
        OrderedIterator<Object> it = IteratorUtils.emptyOrderedIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyMapIterator() {
        MapIterator<Object, Object> it = IteratorUtils.emptyMapIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyOrderedMapIterator() {
        OrderedMapIterator<Object, Object> it = IteratorUtils.emptyOrderedMapIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testSingletonIterator() {
        ResettableIterator<String> it = IteratorUtils.singletonIterator("test");
        assertTrue(it.hasNext());
        assertEquals("test", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSingletonListIterator() {
        ListIterator<String> it = IteratorUtils.singletonListIterator("test");
        assertTrue(it.hasNext());
        assertEquals("test", it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("test", it.previous());
    }

    @Test(expected = NullPointerException.class)
    public void testArrayIteratorNullArray() {
        IteratorUtils.arrayIterator((Object[]) null);
    }

    @Test
    public void testArrayIteratorObjectArray() {
        String[] arr = {"a", "b", "c"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testArrayIteratorPrimitiveArray() {
        int[] arr = {1, 2, 3};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator(arr);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testArrayIteratorNullObjectArray() {
        IteratorUtils.arrayIterator((Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayIteratorNotArray() {
        IteratorUtils.arrayIterator("not an array");
    }

    @Test
    public void testArrayIteratorWithStart() {
        String[] arr = {"a", "b", "c"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr, 1);
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testArrayIteratorStartOutOfBounds() {
        String[] arr = {"a"};
        IteratorUtils.arrayIterator(arr, 2);
    }

    @Test
    public void testArrayIteratorWithStartEnd() {
        String[] arr = {"a", "b", "c", "d"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr, 1, 3);
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayIteratorEndBeforeStart() {
        String[] arr = {"a", "b"};
        IteratorUtils.arrayIterator(arr, 2, 1);
    }

    @Test
    public void testArrayListIterator() {
        String[] arr = {"x", "y"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr);
        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        assertEquals("y", it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("y", it.previous());
    }

    @Test
    public void testArrayListIteratorPrimitiveArray() {
        int[] arr = {10, 20};
        ResettableListIterator<Integer> it = IteratorUtils.arrayListIterator(arr);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(10), it.next());
        assertEquals(Integer.valueOf(20), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testArrayListIteratorWithStart() {
        String[] arr = {"a", "b", "c"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr, 1);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
    }

    @Test
    public void testArrayListIteratorWithStartEnd() {
        String[] arr = {"a", "b", "c", "d"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr, 1, 3);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testBoundedIteratorNullIterator() {
        IteratorUtils.boundedIterator(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedIteratorNegativeMax() {
        IteratorUtils.boundedIterator(Collections.emptyIterator(), -1);
    }

    @Test
    public void testBoundedIterator() {
        List<String> list = Arrays.asList("a", "b", "c", "d");
        BoundedIterator<String> it = IteratorUtils.boundedIterator(list.iterator(), 2);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testBoundedIteratorWithOffset() {
        List<String> list = Arrays.asList("a", "b", "c", "d");
        BoundedIterator<String> it = IteratorUtils.boundedIterator(list.iterator(), 1, 2);
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testUnmodifiableIterator() {
        List<String> list = new ArrayList<>(Arrays.asList("a"));
        Iterator<String> it = IteratorUtils.unmodifiableIterator(list.iterator());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testUnmodifiableListIterator() {
        List<String> list = new ArrayList<>(Arrays.asList("a"));
        ListIterator<String> it = IteratorUtils.unmodifiableListIterator(list.listIterator());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testUnmodifiableMapIterator() {
        MapIterator<String, String> mapIt = new EmptyMapIterator<String, String>();
        MapIterator<String, String> unmod = IteratorUtils.unmodifiableMapIterator(mapIt);
        assertNotNull(unmod);
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorTwoNullFirst() {
        IteratorUtils.chainedIterator(null, Collections.emptyIterator());
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorTwoNullSecond() {
        IteratorUtils.chainedIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testChainedIteratorTwo() {
        Iterator<String> it1 = Arrays.asList("a", "b").iterator();
        Iterator<String> it2 = Arrays.asList("c").iterator();
        Iterator<String> chain = IteratorUtils.chainedIterator(it1, it2);
        assertTrue(chain.hasNext());
        assertEquals("a", chain.next());
        assertEquals("b", chain.next());
        assertEquals("c", chain.next());
        assertFalse(chain.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorArrayNull() {
        IteratorUtils.chainedIterator((Iterator[]) null);
    }

    @Test
    public void testChainedIteratorArray() {
        Iterator<String> it1 = Arrays.asList("a").iterator();
        Iterator<String> it2 = Arrays.asList("b").iterator();
        Iterator<String> chain = IteratorUtils.chainedIterator(it1, it2);
        assertEquals("a", chain.next());
        assertEquals("b", chain.next());
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorCollectionNull() {
        IteratorUtils.chainedIterator((Collection<Iterator<String>>) null);
    }

    @Test
    public void testChainedIteratorCollection() {
        List<Iterator<String>> list = new ArrayList<>();
        list.add(Arrays.asList("x").iterator());
        list.add(Arrays.asList("y").iterator());
        Iterator<String> chain = IteratorUtils.chainedIterator(list);
        assertEquals("x", chain.next());
        assertEquals("y", chain.next());
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorTwoNullFirst() {
        IteratorUtils.collatedIterator(null, null, Collections.emptyIterator());
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorTwoNullSecond() {
        IteratorUtils.collatedIterator(null, Collections.emptyIterator(), null);
    }

    @Test
    public void testCollatedIteratorTwo() {
        Iterator<Integer> it1 = Arrays.asList(1, 3).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2);
        assertEquals(Integer.valueOf(1), collated.next());
        assertEquals(Integer.valueOf(2), collated.next());
        assertEquals(Integer.valueOf(3), collated.next());
        assertEquals(Integer.valueOf(4), collated.next());
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorArrayNull() {
        IteratorUtils.collatedIterator(null, (Iterator[]) null);
    }

    @Test
    public void testCollatedIteratorArray() {
        Iterator<Integer> it1 = Arrays.asList(1).iterator();
        Iterator<Integer> it2 = Arrays.asList(2).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2);
        assertEquals(Integer.valueOf(1), collated.next());
        assertEquals(Integer.valueOf(2), collated.next());
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorCollectionNull() {
        IteratorUtils.collatedIterator(null, (Collection<Iterator<Integer>>) null);
    }

    @Test
    public void testObjectGraphIterator() {
        Iterator<String> it = IteratorUtils.objectGraphIterator("root", new Transformer<String, String>() {
            public String transform(String input) {
                return input;
            }
        });
        assertTrue(it.hasNext());
        assertEquals("root", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testObjectGraphIteratorNullRoot() {
        Iterator<String> it = IteratorUtils.objectGraphIterator(null, null);
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorNullIterator() {
        IteratorUtils.transformedIterator(null, TransformerUtils.constantTransformer("x"));
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorNullTransformer() {
        IteratorUtils.transformedIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testTransformedIterator() {
        Iterator<Integer> source = Arrays.asList(1, 2).iterator();
        Iterator<String> transformed = IteratorUtils.transformedIterator(source, new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return "x" + input;
            }
        });
        assertEquals("x1", transformed.next());
        assertEquals("x2", transformed.next());
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorNullIterator() {
        IteratorUtils.filteredIterator(null, new Predicate<Object>() {
            public boolean evaluate(Object obj) { return true; }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorNullPredicate() {
        IteratorUtils.filteredIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testFilteredIterator() {
        Iterator<Integer> source = Arrays.asList(1, 2, 3, 4).iterator();
        Iterator<Integer> filtered = IteratorUtils.filteredIterator(source, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj % 2 == 0; }
        });
        assertEquals(Integer.valueOf(2), filtered.next());
        assertEquals(Integer.valueOf(4), filtered.next());
        assertFalse(filtered.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredListIteratorNullIterator() {
        IteratorUtils.filteredListIterator(null, new Predicate<Object>() {
            public boolean evaluate(Object obj) { return true; }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredListIteratorNullPredicate() {
        IteratorUtils.filteredListIterator(new ArrayList<Object>().listIterator(), null);
    }

    @Test
    public void testFilteredListIterator() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        ListIterator<Integer> filtered = IteratorUtils.filteredListIterator(list.listIterator(), new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj > 2; }
        });
        assertTrue(filtered.hasNext());
        assertEquals(Integer.valueOf(3), filtered.next());
        assertEquals(Integer.valueOf(4), filtered.next());
        assertFalse(filtered.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testLoopingIteratorNullCollection() {
        IteratorUtils.loopingIterator(null);
    }

    @Test
    public void testLoopingIterator() {
        Collection<String> coll = Arrays.asList("a", "b");
        ResettableIterator<String> it = IteratorUtils.loopingIterator(coll);
        for (int i = 0; i < 5; i++) {
            assertTrue(it.hasNext());
            assertEquals("a", it.next());
            assertTrue(it.hasNext());
            assertEquals("b", it.next());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLoopingListIteratorNullList() {
        IteratorUtils.loopingListIterator(null);
    }

    @Test
    public void testLoopingListIterator() {
        List<String> list = Arrays.asList("x", "y");
        ResettableListIterator<String> it = IteratorUtils.loopingListIterator(list);
        for (int i = 0; i < 3; i++) {
            assertTrue(it.hasNext());
            assertEquals("x", it.next());
            assertTrue(it.hasNext());
            assertEquals("y", it.next());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNodeListIteratorNullNodeList() {
        IteratorUtils.nodeListIterator((NodeList) null);
    }

    @Test(expected = NullPointerException.class)
    public void testNodeListIteratorNullNode() {
        IteratorUtils.nodeListIterator((Node) null);
    }

    @Test
    public void testNodeListIterator() {
        NodeList nodeList = new NodeList() {
            private List<Node> nodes = new ArrayList<>();
            {
                nodes.add(new MockNode("child1"));
                nodes.add(new MockNode("child2"));
            }
            public Node item(int index) {
                return nodes.get(index);
            }
            public int getLength() {
                return nodes.size();
            }
        };
        NodeListIterator it = IteratorUtils.nodeListIterator(nodeList);
        assertTrue(it.hasNext());
        assertEquals("child1", ((MockNode) it.next()).getName());
        assertEquals("child2", ((MockNode) it.next()).getName());
        assertFalse(it.hasNext());
    }

    @Test
    public void testPeekingIterator() {
        Iterator<String> source = Arrays.asList("a", "b").iterator();
        Iterator<String> peeking = IteratorUtils.peekingIterator(source);
        assertTrue(peeking.hasNext());
        assertEquals("a", peeking.next());
        assertEquals("b", peeking.next());
        assertFalse(peeking.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testPeekingIteratorNull() {
        IteratorUtils.peekingIterator(null);
    }

    @Test
    public void testPushbackIterator() {
        Iterator<String> source = Arrays.asList("a", "b").iterator();
        Iterator<String> pushback = IteratorUtils.pushbackIterator(source);
        assertTrue(pushback.hasNext());
        assertEquals("a", pushback.next());
        assertEquals("b", pushback.next());
        assertFalse(pushback.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testPushbackIteratorNull() {
        IteratorUtils.pushbackIterator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkippingIteratorNullIterator() {
        IteratorUtils.skippingIterator(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkippingIteratorNegativeOffset() {
        IteratorUtils.skippingIterator(Collections.emptyIterator(), -1);
    }

    @Test
    public void testSkippingIterator() {
        Iterator<String> source = Arrays.asList("a", "b", "c").iterator();
        SkippingIterator<String> it = IteratorUtils.skippingIterator(source, 2);
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZippingIteratorTwoNullFirst() {
        IteratorUtils.zippingIterator(null, Collections.emptyIterator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZippingIteratorTwoNullSecond() {
        IteratorUtils.zippingIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testZippingIteratorTwo() {
        Iterator<String> a = Arrays.asList("a", "c").iterator();
        Iterator<String> b = Arrays.asList("b", "d").iterator();
        ZippingIterator<String> zip = IteratorUtils.zippingIterator(a, b);
        assertEquals("a", zip.next());
        assertEquals("b", zip.next());
        assertEquals("c", zip.next());
        assertEquals("d", zip.next());
        assertFalse(zip.hasNext());
    }

    @Test
    public void testZippingIteratorThree() {
        Iterator<String> a = Arrays.asList("1", "4").iterator();
        Iterator<String> b = Arrays.asList("2", "5").iterator();
        Iterator<String> c = Arrays.asList("3", "6").iterator();
        ZippingIterator<String> zip = IteratorUtils.zippingIterator(a, b, c);
        assertEquals("1", zip.next());
        assertEquals("2", zip.next());
        assertEquals("3", zip.next());
        assertEquals("4", zip.next());
        assertEquals("5", zip.next());
        assertEquals("6", zip.next());
        assertFalse(zip.hasNext());
    }

    @Test
    public void testZippingIteratorArray() {
        Iterator<String> a = Arrays.asList("x").iterator();
        Iterator<String> b = Arrays.asList("y").iterator();
        ZippingIterator<String> zip = IteratorUtils.zippingIterator(a, b);
        assertEquals("x", zip.next());
        assertEquals("y", zip.next());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNullEnumeration() {
        IteratorUtils.asIterator((Enumeration<?>) null);
    }

    @Test
    public void testAsIterator() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        Enumeration<String> en = v.elements();
        Iterator<String> it = IteratorUtils.asIterator(en);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorWithRemoveCollectionNullEnumeration() {
        IteratorUtils.asIterator(null, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorWithRemoveCollectionNullCollection() {
        IteratorUtils.asIterator(new Vector<String>().elements(), null);
    }

    @Test
    public void testAsIteratorWithRemoveCollection() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        List<String> removeList = new ArrayList<>();
        Iterator<String> it = IteratorUtils.asIterator(v.elements(), removeList);
        assertEquals("a", it.next());
        it.remove();
        assertEquals(1, removeList.size());
        assertEquals("a", removeList.get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testAsEnumerationNullIterator() {
        IteratorUtils.asEnumeration(null);
    }

    @Test
    public void testAsEnumeration() {
        Iterator<String> it = Arrays.asList("x", "y").iterator();
        Enumeration<String> en = IteratorUtils.asEnumeration(it);
        assertTrue(en.hasMoreElements());
        assertEquals("x", en.nextElement());
        assertEquals("y", en.nextElement());
        assertFalse(en.hasMoreElements());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIterableNullIterator() {
        IteratorUtils.asIterable(null);
    }

    @Test
    public void testAsIterable() {
        Iterator<String> it = Arrays.asList("a").iterator();
        Iterable<String> iterable = IteratorUtils.asIterable(it);
        int count = 0;
        for (String s : iterable) {
            assertEquals("a", s);
            count++;
        }
        assertEquals(1, count);
    }

    @Test(expected = NullPointerException.class)
    public void testAsMultipleUseIterableNullIterator() {
        IteratorUtils.asMultipleUseIterable(null);
    }

    @Test
    public void testAsMultipleUseIterable() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Iterable<String> iterable = IteratorUtils.asMultipleUseIterable(it);
        int count = 0;
        for (String s : iterable) {
            count++;
        }
        assertEquals(2, count);
        count = 0;
        for (String s : iterable) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test(expected = NullPointerException.class)
    public void testToListIteratorNullIterator() {
        IteratorUtils.toListIterator(null);
    }

    @Test
    public void testToListIterator() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        ListIterator<String> listIt = IteratorUtils.toListIterator(it);
        assertTrue(listIt.hasNext());
        assertEquals("a", listIt.next());
        assertEquals("b", listIt.next());
        assertFalse(listIt.hasNext());
        assertTrue(listIt.hasPrevious());
        assertEquals("b", listIt.previous());
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayNullIterator() {
        IteratorUtils.toArray(null);
    }

    @Test
    public void testToArray() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Object[] arr = IteratorUtils.toArray(it);
        assertEquals(2, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayWithClassNullIterator() {
        IteratorUtils.toArray(null, String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayWithClassNullClass() {
        IteratorUtils.toArray(Collections.emptyIterator(), null);
    }

    @Test
    public void testToArrayWithClass() {
        Iterator<String> it = Arrays.asList("x", "y").iterator();
        String[] arr = IteratorUtils.toArray(it, String.class);
        assertEquals(2, arr.length);
        assertEquals("x", arr[0]);
        assertEquals("y", arr[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testToListNullIterator() {
        IteratorUtils.toList(null);
    }

    @Test
    public void testToList() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        List<Integer> list = IteratorUtils.toList(it);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testToListEstimatedSizeNullIterator() {
        IteratorUtils.toList(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToListEstimatedSizeInvalid() {
        IteratorUtils.toList(Collections.emptyIterator(), 0);
    }

    @Test
    public void testToListEstimatedSize() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        List<String> list = IteratorUtils.toList(it, 2);
        assertEquals(2, list.size());
    }

    @Test
    public void testGetIteratorNull() {
        Iterator<?> it = IteratorUtils.getIterator(null);
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetIteratorIterator() {
        Iterator<String> source = Arrays.asList("a").iterator();
        Iterator<?> it = IteratorUtils.getIterator(source);
        assertSame(source, it);
    }

    @Test
    public void testGetIteratorIterable() {
        Iterable<String> iterable = Arrays.asList("a", "b");
        Iterator<?> it = IteratorUtils.getIterator(iterable);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
    }

    @Test
    public void testGetIteratorArray() {
        String[] arr = {"x", "y"};
        Iterator<?> it = IteratorUtils.getIterator(arr);
        assertEquals("x", it.next());
        assertEquals("y", it.next());
    }

    @Test
    public void testGetIteratorEnumeration() {
        Vector<String> v = new Vector<>(Arrays.asList("e1"));
        Iterator<?> it = IteratorUtils.getIterator(v.elements());
        assertEquals("e1", it.next());
    }

    @Test
    public void testGetIteratorMap() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        Iterator<?> it = IteratorUtils.getIterator(map);
        assertEquals("v", it.next());
    }

    @Test
    public void testGetIteratorNodeList() {
        NodeList nodeList = new NodeList() {
            public Node item(int index) { return null; }
            public int getLength() { return 0; }
        };
        Iterator<?> it = IteratorUtils.getIterator(nodeList);
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetIteratorNode() {
        Node node = new MockNode("parent");
        Iterator<?> it = IteratorUtils.getIterator(node);
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetIteratorDictionary() {
        Dictionary<String, String> dict = new Hashtable<>();
        dict.put("k", "v");
        Iterator<?> it = IteratorUtils.getIterator(dict);
        assertEquals("v", it.next());
    }

    @Test
    public void testGetIteratorPrimitiveArray() {
        int[] arr = {1, 2};
        Iterator<?> it = IteratorUtils.getIterator(arr);
        assertEquals(1, it.next());
        assertEquals(2, it.next());
    }

    @Test
    public void testGetIteratorObjectWithIteratorMethod() {
        class WithIterator {
            public Iterator<String> iterator() {
                return Arrays.asList("fromMethod").iterator();
            }
        }
        Iterator<?> it = IteratorUtils.getIterator(new WithIterator());
        assertEquals("fromMethod", it.next());
    }

    @Test
    public void testGetIteratorFallbackSingleton() {
        Iterator<?> it = IteratorUtils.getIterator("just a string");
        assertTrue(it.hasNext());
        assertEquals("just a string", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testApplyNullClosure() {
        IteratorUtils.apply(Collections.emptyIterator(), null);
    }

    @Test
    public void testApplyNullIterator() {
        final List<String> result = new ArrayList<>();
        IteratorUtils.apply(null, new Closure<String>() {
            public void execute(String input) {
                result.add(input);
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApply() {
        final List<String> result = new ArrayList<>();
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        IteratorUtils.apply(it, new Closure<String>() {
            public void execute(String input) {
                result.add(input);
            }
        });
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void testFindNullPredicate() {
        IteratorUtils.find(Collections.emptyIterator(), null);
    }

    @Test
    public void testFindNullIterator() {
        assertNull(IteratorUtils.find(null, new Predicate<Object>() {
            public boolean evaluate(Object obj) { return true; }
        }));
    }

    @Test
    public void testFind() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4).iterator();
        Integer found = IteratorUtils.find(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj > 2; }
        });
        assertEquals(Integer.valueOf(3), found);
    }

    @Test
    public void testFindNotFound() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        assertNull(IteratorUtils.find(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj > 5; }
        }));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesAnyNullPredicate() {
        IteratorUtils.matchesAny(Collections.emptyIterator(), null);
    }

    @Test
    public void testMatchesAnyNullIterator() {
        assertFalse(IteratorUtils.matchesAny(null, new Predicate<Object>() {
            public boolean evaluate(Object obj) { return true; }
        }));
    }

    @Test
    public void testMatchesAnyTrue() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        assertTrue(IteratorUtils.matchesAny(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj == 2; }
        }));
    }

    @Test
    public void testMatchesAnyFalse() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        assertFalse(IteratorUtils.matchesAny(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj > 5; }
        }));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesAllNullPredicate() {
        IteratorUtils.matchesAll(Collections.emptyIterator(), null);
    }

    @Test
    public void testMatchesAllNullIterator() {
        assertTrue(IteratorUtils.matchesAll(null, new Predicate<Object>() {
            public boolean evaluate(Object obj) { return false; }
        }));
    }

    @Test
    public void testMatchesAllTrue() {
        Iterator<Integer> it = Arrays.asList(2, 4, 6).iterator();
        assertTrue(IteratorUtils.matchesAll(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj % 2 == 0; }
        }));
    }

    @Test
    public void testMatchesAllFalse() {
        Iterator<Integer> it = Arrays.asList(2, 3, 4).iterator();
        assertFalse(IteratorUtils.matchesAll(it, new Predicate<Integer>() {
            public boolean evaluate(Integer obj) { return obj % 2 == 0; }
        }));
    }

    @Test
    public void testIsEmptyNull() {
        assertTrue(IteratorUtils.isEmpty(null));
    }

    @Test
    public void testIsEmptyEmpty() {
        assertTrue(IteratorUtils.isEmpty(Collections.emptyIterator()));
    }

    @Test
    public void testIsEmptyNonEmpty() {
        assertFalse(IteratorUtils.isEmpty(Arrays.asList("a").iterator()));
    }

    @Test
    public void testContainsNullIterator() {
        assertFalse(IteratorUtils.contains(null, "x"));
    }

    @Test
    public void testContainsTrue() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        assertTrue(IteratorUtils.contains(it, "b"));
    }

    @Test
    public void testContainsFalse() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        assertFalse(IteratorUtils.contains(it, "c"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        IteratorUtils.get(Arrays.asList("a").iterator(), -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexTooLarge() {
        IteratorUtils.get(Arrays.asList("a").iterator(), 1);
    }

    @Test
    public void testGetValid() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertEquals("c", IteratorUtils.get(it, 2));
    }

    @Test
    public void testSizeNull() {
        assertEquals(0, IteratorUtils.size(null));
    }

    @Test
    public void testSizeEmpty() {
        assertEquals(0, IteratorUtils.size(Collections.emptyIterator()));
    }

    @Test
    public void testSizeNonEmpty() {
        assertEquals(3, IteratorUtils.size(Arrays.asList(1, 2, 3).iterator()));
    }

    @Test
    public void testToStringNullIterator() {
        assertEquals("[]", IteratorUtils.toString(null));
    }

    @Test
    public void testToStringEmptyIterator() {
        assertEquals("[]", IteratorUtils.toString(Collections.emptyIterator()));
    }

    @Test
    public void testToStringNonEmpty() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        assertEquals("[a, b]", IteratorUtils.toString(it));
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullTransformer() {
        IteratorUtils.toString(Collections.emptyIterator(), null);
    }

    @Test
    public void testToStringWithTransformer() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        String s = IteratorUtils.toString(it, new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return "x" + input;
            }
        });
        assertEquals("[x1, x2]", s);
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullDelimiter() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), null, "[", "]");
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullPrefix() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), ",", null, "]");
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullSuffix() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), ",", "[", null);
    }

    @Test
    public void testToStringCustomDelimiterPrefixSuffix() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        String s = IteratorUtils.toString(it, TransformerUtils.stringValueTransformer(), "; ", "<", ">");
        assertEquals("<a; b>", s);
    }

    static class MockNode implements Node {
        private String name;
        MockNode(String name) { this.name = name; }
        public String getNodeName() { return name; }
        public String getNodeValue() { return null; }
        public void setNodeValue(String nodeValue) {}
        public short getNodeType() { return 0; }
        public Node getParentNode() { return null; }
        public NodeList getChildNodes() { return null; }
        public Node getFirstChild() { return null; }
        public Node getLastChild() { return null; }
        public Node getPreviousSibling() { return null; }
        public Node getNextSibling() { return null; }
        public NamedNodeMap getAttributes() { return null; }
        public Document getOwnerDocument() { return null; }
        public Node insertBefore(Node newChild, Node refChild) { return null; }
        public Node replaceChild(Node newChild, Node oldChild) { return null; }
        public Node removeChild(Node oldChild) { return null; }
        public Node appendChild(Node newChild) { return null; }
        public boolean hasChildNodes() { return false; }
        public Node cloneNode(boolean deep) { return null; }
        public void normalize() {}
        public boolean isSupported(String feature, String version) { return false; }
        public String getNamespaceURI() { return null; }
        public String getPrefix() { return null; }
        public void setPrefix(String prefix) {}
        public String getLocalName() { return null; }
        public boolean hasAttributes() { return false; }
        public String getBaseURI() { return null; }
        public short compareDocumentPosition(Node other) { return 0; }
        public String getTextContent() { return null; }
        public void setTextContent(String textContent) {}
        public boolean isSameNode(Node other) { return false; }
        public String lookupPrefix(String namespaceURI) { return null; }
        public boolean isDefaultNamespace(String namespaceURI) { return false; }
        public String lookupNamespaceURI(String prefix) { return null; }
        public boolean isEqualNode(Node arg) { return false; }
        public Object getFeature(String feature, String version) { return null; }
        public Object setUserData(String key, Object data, UserDataHandler handler) { return null; }
        public Object getUserData(String key) { return null; }
        public String getName() { return name; }
    }
}
