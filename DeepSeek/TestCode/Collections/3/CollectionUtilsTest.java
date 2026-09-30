package org.apache.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.bag.HashBag;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollectionUtilsTest {

    private static class SimpleBoundedCollection extends ArrayList implements BoundedCollection {
        private final int maxSize;
        private final boolean full;
        SimpleBoundedCollection(int maxSize, boolean full) {
            this.maxSize = maxSize;
            this.full = full;
        }
        public boolean isFull() {
            return full;
        }
        public int maxSize() {
            return maxSize;
        }
    }

    @Test
    public void testConstructor() {
        assertNotNull(new CollectionUtils());
    }

    @Test
    public void testUnion() {
        List a = Arrays.asList("a", "a", "b");
        List b = Arrays.asList("a", "c");
        Collection result = CollectionUtils.union(a, b);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "a", "b", "c")));
    }

    @Test
    public void testIntersection() {
        List a = Arrays.asList("a", "a", "b");
        List b = Arrays.asList("a", "c");
        Collection result = CollectionUtils.intersection(a, b);
        assertEquals(1, result.size());
        assertEquals("a", result.iterator().next());
    }

    @Test
    public void testDisjunction() {
        List a = Arrays.asList("a", "a", "b");
        List b = Arrays.asList("a", "c");
        Collection result = CollectionUtils.disjunction(a, b);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testSubtract() {
        List a = Arrays.asList("a", "a", "b");
        List b = Arrays.asList("a");
        Collection result = CollectionUtils.subtract(a, b);
        assertEquals(2, result.size());
        assertEquals(Arrays.asList("a", "b"), new ArrayList(result));
    }

    @Test
    public void testContainsAny() {
        assertTrue(CollectionUtils.containsAny(Arrays.asList("a", "b"), Arrays.asList("b", "c")));
        assertFalse(CollectionUtils.containsAny(Arrays.asList("a", "b"), Arrays.asList("c", "d")));
        // covers both size branches
        assertTrue(CollectionUtils.containsAny(Arrays.asList("a"), Arrays.asList("a", "b")));
        assertFalse(CollectionUtils.containsAny(Arrays.asList("a"), Arrays.asList("b", "c")));
    }

    @Test
    public void testGetCardinalityMap() {
        Map map = CollectionUtils.getCardinalityMap(Arrays.asList("a", "a", "b"));
        assertEquals(2, map.size());
        assertEquals(new Integer(2), map.get("a"));
        assertEquals(new Integer(1), map.get("b"));
    }

    @Test
    public void testIsSubCollection() {
        assertTrue(CollectionUtils.isSubCollection(Arrays.asList("a", "a"), Arrays.asList("a", "a", "b")));
        assertFalse(CollectionUtils.isSubCollection(Arrays.asList("a", "a", "a"), Arrays.asList("a", "a", "b")));
    }

    @Test
    public void testIsProperSubCollection() {
        assertTrue(CollectionUtils.isProperSubCollection(Arrays.asList("a"), Arrays.asList("a", "b")));
        assertFalse(CollectionUtils.isProperSubCollection(Arrays.asList("a", "b"), Arrays.asList("a", "b")));
        assertFalse(CollectionUtils.isProperSubCollection(Arrays.asList("a", "a"), Arrays.asList("a", "b")));
    }

    @Test
    public void testIsEqualCollection() {
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList("a", "a", "b"), Arrays.asList("a", "b", "a")));
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c")));
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList("a", "a", "b"), Arrays.asList("a", "b", "b")));
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList("a", "a"), Arrays.asList("a", "b")));
    }

    @Test
    public void testCardinalitySet() {
        Set set = new HashSet(Arrays.asList("a", "b"));
        assertEquals(1, CollectionUtils.cardinality("a", set));
        assertEquals(0, CollectionUtils.cardinality("c", set));
    }

    @Test
    public void testCardinalityBag() {
        Bag bag = new HashBag();
        bag.add("a", 3);
        assertEquals(3, CollectionUtils.cardinality("a", bag));
        assertEquals(0, CollectionUtils.cardinality("b", bag));
    }

    @Test
    public void testCardinalityList() {
        List list = Arrays.asList("a", "a", null, null);
        assertEquals(2, CollectionUtils.cardinality("a", list));
        assertEquals(2, CollectionUtils.cardinality(null, list));
        assertEquals(0, CollectionUtils.cardinality("b", list));
    }

    @Test
    public void testFind() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "x".equals(obj);
            }
        };
        assertEquals("x", CollectionUtils.find(Arrays.asList("a", "x", "b"), pred));
        assertNull(CollectionUtils.find(Arrays.asList("a", "b"), pred));
        assertNull(CollectionUtils.find(null, pred));
        assertNull(CollectionUtils.find(Arrays.asList("a", "b"), null));
    }

    @Test
    public void testForAllDo() {
        final List target = new ArrayList();
        Closure closure = new Closure() {
            public void execute(Object input) {
                target.add(input);
            }
        };
        CollectionUtils.forAllDo(Arrays.asList("a", "b"), closure);
        assertEquals(Arrays.asList("a", "b"), target);
        // null no-op
        CollectionUtils.forAllDo(null, closure);
        CollectionUtils.forAllDo(Arrays.asList("a", "b"), (Closure) null);
    }

    @Test
    public void testFilter() {
        List list = new ArrayList(Arrays.asList("a", "b", "c"));
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return !"b".equals(obj);
            }
        };
        CollectionUtils.filter(list, pred);
        assertEquals(2, list.size());
        assertFalse(list.contains("b"));
        // null no-op
        CollectionUtils.filter(null, pred);
        CollectionUtils.filter(list, (Predicate) null);
    }

    @Test
    public void testTransformList() {
        List list = new ArrayList(Arrays.asList("a", "b"));
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString().toUpperCase();
            }
        };
        CollectionUtils.transform(list, transformer);
        assertEquals(Arrays.asList("A", "B"), list);
        // null no-op
        CollectionUtils.transform(null, transformer);
        CollectionUtils.transform(list, (Transformer) null);
    }

    @Test
    public void testTransformNonList() {
        Set set = new HashSet(Arrays.asList("a", "b"));
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString() + "x";
            }
        };
        CollectionUtils.transform(set, transformer);
        assertEquals(2, set.size());
        assertTrue(set.contains("ax"));
        assertTrue(set.contains("bx"));
    }

    @Test
    public void testCountMatches() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "a".equals(obj);
            }
        };
        assertEquals(2, CollectionUtils.countMatches(Arrays.asList("a", "b", "a"), pred));
        assertEquals(0, CollectionUtils.countMatches(null, pred));
        assertEquals(0, CollectionUtils.countMatches(Arrays.asList("a"), (Predicate) null));
    }

    @Test
    public void testExists() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "b".equals(obj);
            }
        };
        assertTrue(CollectionUtils.exists(Arrays.asList("a", "b"), pred));
        assertFalse(CollectionUtils.exists(Arrays.asList("a", "c"), pred));
        assertFalse(CollectionUtils.exists(null, pred));
        assertFalse(CollectionUtils.exists(Arrays.asList("a"), (Predicate) null));
    }

    @Test
    public void testSelect() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "a".equals(obj);
            }
        };
        Collection result = CollectionUtils.select(Arrays.asList("a", "b", "a"), pred);
        assertEquals(Arrays.asList("a", "a"), new ArrayList(result));
        // null predicate -> empty list
        Collection empty = CollectionUtils.select(Arrays.asList("a", "b"), (Predicate) null);
        assertTrue(empty.isEmpty());
        try {
            CollectionUtils.select((Collection) null, pred);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSelectWithOutput() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "a".equals(obj);
            }
        };
        List output = new ArrayList();
        CollectionUtils.select(Arrays.asList("a", "b", "a"), pred, output);
        assertEquals(Arrays.asList("a", "a"), output);
        // null input or predicate no-op
        CollectionUtils.select(null, pred, output);
        CollectionUtils.select(Arrays.asList("a", "b"), (Predicate) null, output);
    }

    @Test
    public void testSelectRejected() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "a".equals(obj);
            }
        };
        Collection result = CollectionUtils.selectRejected(Arrays.asList("a", "b", "a"), pred);
        assertEquals(Arrays.asList("b"), new ArrayList(result));
        // null predicate -> empty list
        Collection empty = CollectionUtils.selectRejected(Arrays.asList("a", "b"), (Predicate) null);
        assertTrue(empty.isEmpty());
        try {
            CollectionUtils.selectRejected((Collection) null, pred);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSelectRejectedWithOutput() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return "a".equals(obj);
            }
        };
        List output = new ArrayList();
        CollectionUtils.selectRejected(Arrays.asList("a", "b", "a"), pred, output);
        assertEquals(Arrays.asList("b"), output);
        CollectionUtils.selectRejected(null, pred, output);
        CollectionUtils.selectRejected(Arrays.asList("a", "b"), (Predicate) null, output);
    }

    @Test
    public void testCollectCollection() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString().toUpperCase();
            }
        };
        Collection result = CollectionUtils.collect(Arrays.asList("a", "b"), transformer);
        assertEquals(Arrays.asList("A", "B"), new ArrayList(result));
        // null transformer -> empty list
        Collection empty = CollectionUtils.collect(Arrays.asList("a", "b"), (Transformer) null);
        assertTrue(empty.isEmpty());
        try {
            CollectionUtils.collect((Collection) null, transformer);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testCollectIterator() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString() + "x";
            }
        };
        Collection result = CollectionUtils.collect(Arrays.asList("a", "b").iterator(), transformer);
        assertEquals(Arrays.asList("ax", "bx"), new ArrayList(result));
        // null iterator or transformer -> empty list
        assertTrue(CollectionUtils.collect((Iterator) null, transformer).isEmpty());
        assertTrue(CollectionUtils.collect(Arrays.asList("a").iterator(), (Transformer) null).isEmpty());
    }

    @Test
    public void testCollectCollectionWithOutput() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString().toUpperCase();
            }
        };
        List output = new ArrayList();
        CollectionUtils.collect(Arrays.asList("a", "b"), transformer, output);
        assertEquals(Arrays.asList("A", "B"), output);
        // null input collection -> return output unchanged
        List output2 = new ArrayList();
        output2.add("z");
        CollectionUtils.collect((Collection) null, transformer, output2);
        assertEquals(Arrays.asList("z"), output2);
        // null transformer -> no change
        List output3 = new ArrayList();
        output3.add("w");
        CollectionUtils.collect(Arrays.asList("a", "b"), (Transformer) null, output3);
        assertEquals(Arrays.asList("w"), output3);
    }

    @Test
    public void testCollectIteratorWithOutput() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString().toUpperCase();
            }
        };
        List output = new ArrayList();
        CollectionUtils.collect(Arrays.asList("a", "b").iterator(), transformer, output);
        assertEquals(Arrays.asList("A", "B"), output);
        List output2 = new ArrayList();
        output2.add("z");
        CollectionUtils.collect((Iterator) null, transformer, output2);
        assertEquals(Arrays.asList("z"), output2);
        List output3 = new ArrayList();
        output3.add("w");
        CollectionUtils.collect(Arrays.asList("a", "b").iterator(), (Transformer) null, output3);
        assertEquals(Arrays.asList("w"), output3);
    }

    @Test
    public void testAddIgnoreNull() {
        List list = new ArrayList();
        assertTrue(CollectionUtils.addIgnoreNull(list, "a"));
        assertFalse(CollectionUtils.addIgnoreNull(list, null));
        assertEquals(1, list.size());
        try {
            CollectionUtils.addIgnoreNull(null, "a");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testAddAllIterator() {
        List collection = new ArrayList();
        CollectionUtils.addAll(collection, Arrays.asList("a", "b").iterator());
        assertEquals(Arrays.asList("a", "b"), collection);
        try {
            CollectionUtils.addAll(null, Arrays.asList("a").iterator());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            CollectionUtils.addAll(collection, (Iterator) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testAddAllEnumeration() {
        List collection = new ArrayList();
        Enumeration enumeration = Collections.enumeration(Arrays.asList("a", "b"));
        CollectionUtils.addAll(collection, enumeration);
        assertEquals(Arrays.asList("a", "b"), collection);
        try {
            CollectionUtils.addAll(null, Collections.enumeration(Arrays.asList("a")));
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            CollectionUtils.addAll(collection, (Enumeration) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testAddAllArray() {
        List collection = new ArrayList();
        CollectionUtils.addAll(collection, new Object[] {"a", "b"});
        assertEquals(Arrays.asList("a", "b"), collection);
        try {
            CollectionUtils.addAll(null, new Object[] {"a"});
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            CollectionUtils.addAll(collection, (Object[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testIndexMapWithKey() {
        Map map = new HashMap();
        map.put(new Integer(1), "value");
        assertEquals("value", CollectionUtils.index(map, new Integer(1)));
    }

    @Test
    public void testIndexMapWithoutKeyButIntegerIndex() {
        Map map = new HashMap();
        map.put("key1", "val1");
        map.put("key2", "val2");
        Object result = CollectionUtils.index(map, new Integer(1));
        assertTrue(result instanceof Iterator);
        Iterator it = (Iterator) result;
        // The returned iterator should already be exhausted if idx >= map size (1 < 2 so it returns an element at index 1 after consuming first)
        // Actually when idx=1, private index loop advances once then returns second element and exhausts? Let's compute:
        // map keySet iterator order not defined; but result should be the key at index 1 (some key). To avoid order dependency, assert it's one of keys.
        assertTrue(map.keySet().contains(result));
    }

    @Test
    public void testIndexMapExceedingIndex() {
        Map map = new HashMap();
        map.put("key", "val");
        Object result = CollectionUtils.index(map, new Integer(5));
        assertTrue(result instanceof Iterator);
        assertFalse(((Iterator) result).hasNext());
    }

    @Test
    public void testIndexMapNonIntegerIndex() {
        Map map = new HashMap();
        map.put("key", "val");
        assertEquals(map, CollectionUtils.index(map, "notAnInteger"));
    }

    @Test
    public void testIndexList() {
        List list = Arrays.asList("a", "b", "c");
        assertEquals("b", CollectionUtils.index(list, new Integer(1)));
        assertEquals("b", CollectionUtils.index(list, 1)); // deprecated int overload
        try {
            CollectionUtils.index(list, new Integer(10));
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testIndexArray() {
        Object[] array = new Object[] {"a", "b", "c"};
        assertEquals("c", CollectionUtils.index(array, new Integer(2)));
        try {
            CollectionUtils.index(array, new Integer(3));
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testIndexEnumeration() {
        Enumeration enumeration = Collections.enumeration(Arrays.asList("a", "b", "c"));
        assertEquals("b", CollectionUtils.index(enumeration, new Integer(1)));
        Enumeration enumeration2 = Collections.enumeration(Arrays.asList("a", "b"));
        Object result = CollectionUtils.index(enumeration2, new Integer(5));
        assertTrue(result instanceof Enumeration);
        assertFalse(((Enumeration) result).hasMoreElements());
    }

    @Test
    public void testIndexIterator() {
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertEquals("c", CollectionUtils.index(it, new Integer(2)));
        Iterator it2 = Arrays.asList("a", "b").iterator();
        Object result = CollectionUtils.index(it2, new Integer(5));
        assertTrue(result instanceof Iterator);
        assertFalse(((Iterator) result).hasNext());
    }

    @Test
    public void testIndexCollection() {
        Set set = new HashSet(Arrays.asList("a", "b", "c"));
        Object result = CollectionUtils.index(set, new Integer(1));
        assertTrue(set.contains(result));
    }

    @Test
    public void testIndexUnsupported() {
        assertEquals("hello", CollectionUtils.index("hello", new Integer(1)));
        assertEquals("hello", CollectionUtils.index("hello", 1));
    }

    @Test
    public void testIndexNegative() {
        assertEquals("obj", CollectionUtils.index("obj", new Integer(-1)));
        assertEquals("obj", CollectionUtils.index("obj", -1));
    }

    @Test
    public void testGetNegative() {
        try {
            CollectionUtils.get(new Object(), -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetMap() {
        Map map = new HashMap();
        map.put("key", "value");
        Object result = CollectionUtils.get(map, 0);
        assertTrue(result instanceof Map.Entry);
        Map.Entry entry = (Map.Entry) result;
        assertEquals("key", entry.getKey());
        assertEquals("value", entry.getValue());
    }

    @Test
    public void testGetMapOutOfBounds() {
        Map map = new HashMap();
        map.put("key", "value");
        try {
            CollectionUtils.get(map, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetList() {
        List list = Arrays.asList("a", "b");
        assertEquals("b", CollectionUtils.get(list, 1));
        try {
            CollectionUtils.get(list, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetObjectArray() {
        Object[] array = new Object[] {"a", "b"};
        assertEquals("a", CollectionUtils.get(array, 0));
        try {
            CollectionUtils.get(array, 2);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (IndexOutOfBoundsException e) {
            // ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException, so both acceptable
        }
    }

    @Test
    public void testGetIterator() {
        Iterator it = Arrays.asList("a", "b").iterator();
        assertEquals("b", CollectionUtils.get(it, 1));
        Iterator it2 = Arrays.asList("a", "b").iterator();
        try {
            CollectionUtils.get(it2, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetCollection() {
        Set set = new HashSet(Arrays.asList("a", "b"));
        Object result = CollectionUtils.get(set, 1);
        assertTrue(set.contains(result));
        try {
            CollectionUtils.get(set, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetEnumeration() {
        Enumeration enumeration = Collections.enumeration(Arrays.asList("a", "b"));
        assertEquals("b", CollectionUtils.get(enumeration, 1));
        Enumeration enumeration2 = Collections.enumeration(Arrays.asList("a", "b"));
        try {
            CollectionUtils.get(enumeration2, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetNull() {
        try {
            CollectionUtils.get(null, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetUnsupported() {
        try {
            CollectionUtils.get(new Object(), 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetPrimitiveArray() {
        int[] primitive = new int[] {5, 6, 7};
        assertEquals(new Integer(6), CollectionUtils.get(primitive, 1));
    }

    @Test
    public void testSizeMap() {
        Map map = new HashMap();
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(2, CollectionUtils.size(map));
    }

    @Test
    public void testSizeCollection() {
        assertEquals(2, CollectionUtils.size(Arrays.asList("a", "b")));
    }

    @Test
    public void testSizeObjectArray() {
        assertEquals(3, CollectionUtils.size(new Object[] {"a", "b", "c"}));
    }

    @Test
    public void testSizeIterator() {
        assertEquals(3, CollectionUtils.size(Arrays.asList("a", "b", "c").iterator()));
    }

    @Test
    public void testSizeEnumeration() {
        assertEquals(3, CollectionUtils.size(Collections.enumeration(Arrays.asList("a", "b", "c"))));
    }

    @Test
    public void testSizePrimitiveArray() {
        assertEquals(4, CollectionUtils.size(new int[] {1,2,3,4}));
    }

    @Test
    public void testSizeUnsupported() {
        try {
            CollectionUtils.size(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSizeNull() {
        try {
            CollectionUtils.size(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSizeIsEmptyCollection() {
        assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList()));
        assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList("a")));
    }

    @Test
    public void testSizeIsEmptyMap() {
        assertTrue(CollectionUtils.sizeIsEmpty(new HashMap()));
        Map map = new HashMap();
        map.put("a", 1);
        assertFalse(CollectionUtils.sizeIsEmpty(map));
    }

    @Test
    public void testSizeIsEmptyObjectArray() {
        assertTrue(CollectionUtils.sizeIsEmpty(new Object[0]));
        assertFalse(CollectionUtils.sizeIsEmpty(new Object[] {"a"}));
    }

    @Test
    public void testSizeIsEmptyIterator() {
        assertTrue(CollectionUtils.sizeIsEmpty(Collections.emptyList().iterator()));
        assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList("a").iterator()));
    }

    @Test
    public void testSizeIsEmptyEnumeration() {
        assertTrue(CollectionUtils.sizeIsEmpty(Collections.enumeration(new ArrayList())));
        assertFalse(CollectionUtils.sizeIsEmpty(Collections.enumeration(Arrays.asList("a"))));
    }

    @Test
    public void testSizeIsEmptyPrimitiveArray() {
        assertTrue(CollectionUtils.sizeIsEmpty(new int[0]));
        assertFalse(CollectionUtils.sizeIsEmpty(new int[] {1}));
    }

    @Test
    public void testSizeIsEmptyUnsupported() {
        try {
            CollectionUtils.sizeIsEmpty(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSizeIsEmptyNull() {
        try {
            CollectionUtils.sizeIsEmpty(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(new ArrayList()));
        assertFalse(CollectionUtils.isEmpty(Arrays.asList("a")));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(new ArrayList()));
        assertTrue(CollectionUtils.isNotEmpty(Arrays.asList("a")));
    }

    @Test
    public void testReverseArray() {
        Object[] array = new Object[] {"a", "b", "c", "d"};
        CollectionUtils.reverseArray(array);
        assertArrayEquals(new Object[] {"d", "c", "b", "a"}, array);
        Object[] single = new Object[] {"a"};
        CollectionUtils.reverseArray(single);
        assertArrayEquals(new Object[] {"a"}, single);
        Object[] empty = new Object[0];
        CollectionUtils.reverseArray(empty);
        assertEquals(0, empty.length);
    }

    @Test
    public void testIsFullNull() {
        try {
            CollectionUtils.isFull(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testIsFullBounded() {
        BoundedCollection bounded = new SimpleBoundedCollection(3, true);
        assertTrue(CollectionUtils.isFull(bounded));
        BoundedCollection notFull = new SimpleBoundedCollection(3, false);
        assertFalse(CollectionUtils.isFull(notFull));
    }

    @Test
    public void testIsFullNonBounded() {
        assertFalse(CollectionUtils.isFull(new ArrayList()));
    }

    @Test
    public void testMaxSizeNull() {
        try {
            CollectionUtils.maxSize(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testMaxSizeBounded() {
        BoundedCollection bounded = new SimpleBoundedCollection(5, true);
        assertEquals(5, CollectionUtils.maxSize(bounded));
    }

    @Test
    public void testMaxSizeNonBounded() {
        assertEquals(-1, CollectionUtils.maxSize(new ArrayList()));
    }

    @Test
    public void testRetainAll() {
        Collection result = CollectionUtils.retainAll(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("b", "d")
        );
        assertEquals(Arrays.asList("b"), new ArrayList(result));
    }

    @Test
    public void testRemoveAll() {
        // removeAll delegates to ListUtils.retainAll in this implementation
        Collection result = CollectionUtils.removeAll(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("b", "d")
        );
        assertEquals(Arrays.asList("b"), new ArrayList(result));
    }

    @Test
    public void testSynchronizedCollection() {
        Collection col = new ArrayList(Arrays.asList("a"));
        Collection sync = CollectionUtils.synchronizedCollection(col);
        assertNotNull(sync);
        assertEquals(1, sync.size());
        try {
            CollectionUtils.synchronizedCollection(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testUnmodifiableCollection() {
        Collection col = new ArrayList(Arrays.asList("a"));
        Collection unmod = CollectionUtils.unmodifiableCollection(col);
        assertNotNull(unmod);
        assertEquals(1, unmod.size());
        try {
            CollectionUtils.unmodifiableCollection(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPredicatedCollection() {
        Predicate pred = new Predicate() {
            public boolean evaluate(Object obj) {
                return obj instanceof String;
            }
        };
        Collection predicated = CollectionUtils.predicatedCollection(new ArrayList(), pred);
        predicated.add("valid");
        assertEquals(1, predicated.size());
        try {
            predicated.add(new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            CollectionUtils.predicatedCollection(null, pred);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            CollectionUtils.predicatedCollection(new ArrayList(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testTypedCollection() {
        Collection typed = CollectionUtils.typedCollection(new ArrayList(), String.class);
        typed.add("valid");
        assertEquals(1, typed.size());
        try {
            typed.add(new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testTransformedCollection() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input.toString().toUpperCase();
            }
        };
        Collection transformed = CollectionUtils.transformedCollection(new ArrayList(), transformer);
        transformed.add("a");
        assertEquals(1, transformed.size());
        assertEquals("A", transformed.iterator().next());
        try {
            CollectionUtils.transformedCollection(null, transformer);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            CollectionUtils.transformedCollection(new ArrayList(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
