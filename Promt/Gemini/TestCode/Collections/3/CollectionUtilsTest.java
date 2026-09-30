package org.apache.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.collections.collection.UnmodifiableBoundedCollection;
import org.apache.commons.collections.functors.EqualPredicate;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.collections.functors.NullPredicate;
import org.apache.commons.collections.functors.StringValueTransformer;
import org.apache.commons.collections.functors.TruePredicate;
import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

    @Test
    public void testConstructorAndConstants() {
        CollectionUtils utils = new CollectionUtils();
        Assert.assertNotNull(utils);
        Assert.assertNotNull(CollectionUtils.EMPTY_COLLECTION);
        Assert.assertTrue(CollectionUtils.EMPTY_COLLECTION.isEmpty());
        try {
            CollectionUtils.EMPTY_COLLECTION.add("test");
            Assert.fail("EMPTY_COLLECTION should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testUnion() {
        List a = Arrays.asList(new Object[]{"A", "B", "B", "C"});
        List b = Arrays.asList(new Object[]{"B", "C", "C", "D"});
        Collection union = CollectionUtils.union(a, b);
        Assert.assertEquals(6, union.size());
        Assert.assertEquals(1, CollectionUtils.cardinality("A", union));
        Assert.assertEquals(2, CollectionUtils.cardinality("B", union));
        Assert.assertEquals(2, CollectionUtils.cardinality("C", union));
        Assert.assertEquals(1, CollectionUtils.cardinality("D", union));
    }

    @Test
    public void testIntersection() {
        List a = Arrays.asList(new Object[]{"A", "B", "B", "C", "C"});
        List b = Arrays.asList(new Object[]{"B", "B", "C", "D"});
        Collection intersection = CollectionUtils.intersection(a, b);
        Assert.assertEquals(3, intersection.size());
        Assert.assertEquals(0, CollectionUtils.cardinality("A", intersection));
        Assert.assertEquals(2, CollectionUtils.cardinality("B", intersection));
        Assert.assertEquals(1, CollectionUtils.cardinality("C", intersection));
        Assert.assertEquals(0, CollectionUtils.cardinality("D", intersection));
    }

    @Test
    public void testDisjunction() {
        List a = Arrays.asList(new Object[]{"A", "B", "B", "C"});
        List b = Arrays.asList(new Object[]{"B", "C", "C", "D"});
        Collection disjunction = CollectionUtils.disjunction(a, b);
        Assert.assertEquals(3, disjunction.size());
        Assert.assertEquals(1, CollectionUtils.cardinality("A", disjunction));
        Assert.assertEquals(1, CollectionUtils.cardinality("B", disjunction));
        Assert.assertEquals(1, CollectionUtils.cardinality("C", disjunction));
        Assert.assertEquals(1, CollectionUtils.cardinality("D", disjunction));
    }

    @Test
    public void testSubtract() {
        List a = new ArrayList(Arrays.asList(new Object[]{"A", "B", "B", "C"}));
        List b = Arrays.asList(new Object[]{"B", "D"});
        Collection result = CollectionUtils.subtract(a, b);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(1, CollectionUtils.cardinality("A", result));
        Assert.assertEquals(1, CollectionUtils.cardinality("B", result));
        Assert.assertEquals(1, CollectionUtils.cardinality("C", result));
    }

    @Test
    public void testContainsAny() {
        List one = Arrays.asList(new Object[]{"A"});
        List two = Arrays.asList(new Object[]{"B", "C"});
        List three = Arrays.asList(new Object[]{"C", "D", "E"});

        // size coll1 < size coll2 branch
        Assert.assertFalse(CollectionUtils.containsAny(one, two));
        Assert.assertTrue(CollectionUtils.containsAny(one, Arrays.asList(new Object[]{"A", "B"})));

        // size coll1 >= size coll2 branch
        Assert.assertTrue(CollectionUtils.containsAny(three, two));
        Assert.assertFalse(CollectionUtils.containsAny(three, one));
    }

    @Test
    public void testGetCardinalityMap() {
        List list = Arrays.asList(new Object[]{"A", "B", "B", null, null, null});
        Map map = CollectionUtils.getCardinalityMap(list);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(new Integer(1), map.get("A"));
        Assert.assertEquals(new Integer(2), map.get("B"));
        Assert.assertEquals(new Integer(3), map.get(null));
    }

    @Test
    public void testIsSubCollection() {
        List a = Arrays.asList(new Object[]{"A", "B"});
        List b = Arrays.asList(new Object[]{"A", "B", "B"});
        List c = Arrays.asList(new Object[]{"A", "C"});

        Assert.assertTrue(CollectionUtils.isSubCollection(a, b));
        Assert.assertFalse(CollectionUtils.isSubCollection(b, a));
        Assert.assertFalse(CollectionUtils.isSubCollection(c, b));
    }

    @Test
    public void testIsProperSubCollection() {
        List a = Arrays.asList(new Object[]{"A", "B"});
        List b = Arrays.asList(new Object[]{"A", "B", "C"});
        List c = Arrays.asList(new Object[]{"A", "B"});

        Assert.assertTrue(CollectionUtils.isProperSubCollection(a, b));
        Assert.assertFalse(CollectionUtils.isProperSubCollection(b, a));
        Assert.assertFalse(CollectionUtils.isProperSubCollection(a, c));
    }

    @Test
    public void testIsEqualCollection() {
        List a = Arrays.asList(new Object[]{"A", "B", "B"});
        List b = Arrays.asList(new Object[]{"B", "A", "B"});
        List c = Arrays.asList(new Object[]{"A", "B"});
        List d = Arrays.asList(new Object[]{"A", "B", "C"});
        List e = Arrays.asList(new Object[]{"A", "A", "B"});

        Assert.assertTrue(CollectionUtils.isEqualCollection(a, b));
        Assert.assertFalse(CollectionUtils.isEqualCollection(a, c)); // different size
        Assert.assertFalse(CollectionUtils.isEqualCollection(a, d)); // different unique keys size
        Assert.assertFalse(CollectionUtils.isEqualCollection(a, e)); // same size, different frequencies
    }

    @Test
    public void testCardinality() {
        Set set = new HashSet(Arrays.asList(new Object[]{"A", "B"}));
        Assert.assertEquals(1, CollectionUtils.cardinality("A", set));
        Assert.assertEquals(0, CollectionUtils.cardinality("C", set));

        Bag bag = new HashBag();
        bag.add("A", 3);
        Assert.assertEquals(3, CollectionUtils.cardinality("A", bag));
        Assert.assertEquals(0, CollectionUtils.cardinality("B", bag));

        List list = Arrays.asList(new Object[]{"A", null, "B", null, "A"});
        Assert.assertEquals(2, CollectionUtils.cardinality("A", list));
        Assert.assertEquals(2, CollectionUtils.cardinality(null, list));
        Assert.assertEquals(0, CollectionUtils.cardinality("Z", list));
    }

    @Test
    public void testFind() {
        List list = Arrays.asList(new Object[]{"apple", "banana", "cherry"});
        Assert.assertEquals("banana", CollectionUtils.find(list, EqualPredicate.getInstance("banana")));
        Assert.assertNull(CollectionUtils.find(list, EqualPredicate.getInstance("date")));
        Assert.assertNull(CollectionUtils.find(null, TruePredicate.getInstance()));
        Assert.assertNull(CollectionUtils.find(list, null));
    }

    @Test
    public void testForAllDo() {
        List list = new ArrayList(Arrays.asList(new Object[]{"1", "2"}));
        final List executed = new ArrayList();
        Closure closure = new Closure() {
            public void execute(Object input) {
                executed.add(input);
            }
        };

        CollectionUtils.forAllDo(list, closure);
        Assert.assertEquals(2, executed.size());

        // null safety
        CollectionUtils.forAllDo(null, closure);
        CollectionUtils.forAllDo(list, null);
    }

    @Test
    public void testFilter() {
        List list = new ArrayList(Arrays.asList(new Object[]{"A", "B", null, "C"}));
        CollectionUtils.filter(list, NotNullPredicate.getInstance());
        Assert.assertEquals(3, list.size());
        Assert.assertFalse(list.contains(null));

        CollectionUtils.filter(null, NotNullPredicate.getInstance());
        CollectionUtils.filter(list, null);
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void testTransform() {
        // List branch
        List list = new ArrayList(Arrays.asList(new Object[]{"a", "b"}));
        CollectionUtils.transform(list, new Transformer() {
            public Object transform(Object input) {
                return ((String) input).toUpperCase();
            }
        });
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));

        // Non-list branch (Set)
        Set set = new HashSet(Arrays.asList(new Object[]{"a", "b"}));
        CollectionUtils.transform(set, new Transformer() {
            public Object transform(Object input) {
                return ((String) input).toUpperCase();
            }
        });
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        // Null safety
        CollectionUtils.transform(null, StringValueTransformer.getInstance());
        CollectionUtils.transform(list, null);
    }

    @Test
    public void testCountMatches() {
        List list = Arrays.asList(new Object[]{"A", "B", "A", null});
        Assert.assertEquals(2, CollectionUtils.countMatches(list, EqualPredicate.getInstance("A")));
        Assert.assertEquals(1, CollectionUtils.countMatches(list, NullPredicate.getInstance()));
        Assert.assertEquals(0, CollectionUtils.countMatches(null, NullPredicate.getInstance()));
        Assert.assertEquals(0, CollectionUtils.countMatches(list, null));
    }

    @Test
    public void testExists() {
        List list = Arrays.asList(new Object[]{"A", "B"});
        Assert.assertTrue(CollectionUtils.exists(list, EqualPredicate.getInstance("A")));
        Assert.assertFalse(CollectionUtils.exists(list, EqualPredicate.getInstance("C")));
        Assert.assertFalse(CollectionUtils.exists(null, EqualPredicate.getInstance("A")));
        Assert.assertFalse(CollectionUtils.exists(list, null));
    }

    @Test
    public void testSelect() {
        List list = Arrays.asList(new Object[]{"A", "B", "A"});
        Collection result = CollectionUtils.select(list, EqualPredicate.getInstance("A"));
        Assert.assertEquals(2, result.size());

        List out = new ArrayList();
        CollectionUtils.select(list, EqualPredicate.getInstance("B"), out);
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("B", out.get(0));

        CollectionUtils.select(null, TruePredicate.getInstance(), out);
        CollectionUtils.select(list, null, out);
        Assert.assertEquals(1, out.size());
    }

    @Test(expected = NullPointerException.class)
    public void testSelectNullInput() {
        CollectionUtils.select(null, TruePredicate.getInstance());
    }

    @Test
    public void testSelectRejected() {
        List list = Arrays.asList(new Object[]{"A", "B", "A"});
        Collection result = CollectionUtils.selectRejected(list, EqualPredicate.getInstance("A"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("B", result.iterator().next());

        List out = new ArrayList();
        CollectionUtils.selectRejected(list, EqualPredicate.getInstance("A"), out);
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("B", out.get(0));

        CollectionUtils.selectRejected(null, TruePredicate.getInstance(), out);
        CollectionUtils.selectRejected(list, null, out);
        Assert.assertEquals(1, out.size());
    }

    @Test(expected = NullPointerException.class)
    public void testSelectRejectedNullInput() {
        CollectionUtils.selectRejected(null, TruePredicate.getInstance());
    }

    @Test
    public void testCollect() {
        List list = Arrays.asList(new Object[]{1, 2, 3});
        Transformer t = new Transformer() {
            public Object transform(Object input) {
                return "Val" + input;
            }
        };

        Collection c1 = CollectionUtils.collect(list, t);
        Assert.assertEquals(Arrays.asList(new Object[]{"Val1", "Val2", "Val3"}), c1);

        Collection c2 = CollectionUtils.collect(list.iterator(), t);
        Assert.assertEquals(Arrays.asList(new Object[]{"Val1", "Val2", "Val3"}), c2);

        List out = new ArrayList();
        CollectionUtils.collect(list, t, out);
        Assert.assertEquals(3, out.size());

        CollectionUtils.collect((Collection) null, t, out);
        CollectionUtils.collect((Iterator) null, t, out);
        CollectionUtils.collect(list.iterator(), null, out);
        Assert.assertEquals(3, out.size());
    }

    @Test(expected = NullPointerException.class)
    public void testCollectNullInputList() {
        CollectionUtils.collect((Collection) null, StringValueTransformer.getInstance());
    }

    @Test
    public void testAddIgnoreNull() {
        List list = new ArrayList();
        Assert.assertTrue(CollectionUtils.addIgnoreNull(list, "A"));
        Assert.assertFalse(CollectionUtils.addIgnoreNull(list, null));
        Assert.assertEquals(1, list.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddIgnoreNullCollectionNull() {
        CollectionUtils.addIgnoreNull(null, "A");
    }

    @Test
    public void testAddAll() {
        List list = new ArrayList();
        CollectionUtils.addAll(list, Arrays.asList(new Object[]{"A", "B"}).iterator());
        Assert.assertEquals(2, list.size());

        Vector v = new Vector();
        v.add("C");
        v.add("D");
        CollectionUtils.addAll(list, v.elements());
        Assert.assertEquals(4, list.size());

        CollectionUtils.addAll(list, new Object[]{"E", "F"});
        Assert.assertEquals(6, list.size());
    }

    @Test
    public void testIndexMethods() {
        List list = Arrays.asList(new Object[]{"A", "B", "C"});
        Assert.assertEquals("B", CollectionUtils.index(list, 1));
        Assert.assertEquals("B", CollectionUtils.index(list, new Integer(1)));
        Assert.assertEquals(list, CollectionUtils.index(list, -1));
        Assert.assertEquals(list, CollectionUtils.index(list, "invalidKey"));

        Object[] array = new Object[]{"X", "Y", "Z"};
        Assert.assertEquals("Y", CollectionUtils.index(array, 1));

        Vector vec = new Vector(Arrays.asList(new Object[]{"1", "2", "3"}));
        Assert.assertEquals("2", CollectionUtils.index(vec.elements(), 1));
        Assert.assertTrue(CollectionUtils.index(vec.elements(), 10) instanceof Enumeration);

        Iterator it = Arrays.asList(new Object[]{"10", "20"}).iterator();
        Assert.assertEquals("20", CollectionUtils.index(it, 1));
        Iterator it2 = Arrays.asList(new Object[]{"10"}).iterator();
        Assert.assertTrue(CollectionUtils.index(it2, 5) instanceof Iterator);

        Set set = new HashSet(Arrays.asList(new Object[]{"unique"}));
        Assert.assertEquals("unique", CollectionUtils.index(set, 0));

        Map map = new HashMap();
        map.put("key1", "val1");
        map.put(new Integer(2), "two");
        Assert.assertEquals("val1", CollectionUtils.index(map, "key1"));
        Assert.assertEquals("two", CollectionUtils.index(map, 2));

        Map mapForKeys = new HashMap();
        mapForKeys.put("onlyKey", "val");
        Assert.assertEquals("onlyKey", CollectionUtils.index(mapForKeys, 0));
        Assert.assertTrue(CollectionUtils.index(mapForKeys, 10) instanceof Iterator);

        Object notAColl = "StringObj";
        Assert.assertEquals("StringObj", CollectionUtils.index(notAColl, 0));
    }

    @Test
    public void testGet() {
        // Map
        Map map = new HashMap();
        map.put("k", "v");
        Map.Entry entry = (Map.Entry) CollectionUtils.get(map, 0);
        Assert.assertEquals("k", entry.getKey());
        Assert.assertEquals("v", entry.getValue());

        // List
        List list = Arrays.asList(new Object[]{"A", "B"});
        Assert.assertEquals("A", CollectionUtils.get(list, 0));
        Assert.assertEquals("B", CollectionUtils.get(list, 1));

        // Array Object
        Object[] arr = new Object[]{"X", "Y"};
        Assert.assertEquals("X", CollectionUtils.get(arr, 0));

        // Array Primitive
        int[] primArr = new int[]{10, 20};
        Assert.assertEquals(new Integer(20), CollectionUtils.get(primArr, 1));

        // Iterator
        Assert.assertEquals("B", CollectionUtils.get(Arrays.asList(new Object[]{"A", "B"}).iterator(), 1));

        // Collection
        Set set = new HashSet();
        set.add("Single");
        Assert.assertEquals("Single", CollectionUtils.get(set, 0));

        // Enumeration
        Vector v = new Vector();
        v.add("One");
        v.add("Two");
        Assert.assertEquals("Two", CollectionUtils.get(v.elements(), 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        CollectionUtils.get(new ArrayList(), -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIteratorOutOfBounds() {
        CollectionUtils.get(Collections.emptyList().iterator(), 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEnumerationOutOfBounds() {
        CollectionUtils.get(new Vector().elements(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullObject() {
        CollectionUtils.get(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnsupportedObject() {
        CollectionUtils.get(new Object(), 0);
    }

    @Test
    public void testSize() {
        Map map = new HashMap();
        map.put("1", "A");
        Assert.assertEquals(1, CollectionUtils.size(map));

        List list = Arrays.asList(new Object[]{"A", "B"});
        Assert.assertEquals(2, CollectionUtils.size(list));

        Object[] arr = new Object[]{"A", "B", "C"};
        Assert.assertEquals(3, CollectionUtils.size(arr));

        int[] primArr = new int[]{1, 2, 3, 4};
        Assert.assertEquals(4, CollectionUtils.size(primArr));

        Assert.assertEquals(2, CollectionUtils.size(list.iterator()));

        Vector v = new Vector();
        v.add("1");
        Assert.assertEquals(1, CollectionUtils.size(v.elements()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeNull() {
        CollectionUtils.size(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeUnsupported() {
        CollectionUtils.size(new Object());
    }

    @Test
    public void testSizeIsEmpty() {
        Assert.assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList()));
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList(new Object[]{"A"})));

        Assert.assertTrue(CollectionUtils.sizeIsEmpty(new HashMap()));
        Map m = new HashMap();
        m.put("k", "v");
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(m));

        Assert.assertTrue(CollectionUtils.sizeIsEmpty(new Object[0]));
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(new Object[]{"A"}));

        Assert.assertTrue(CollectionUtils.sizeIsEmpty(new int[0]));
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(new int[]{1}));

        Assert.assertTrue(CollectionUtils.sizeIsEmpty(Collections.emptyList().iterator()));
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList(new Object[]{"A"}).iterator()));

        Assert.assertTrue(CollectionUtils.sizeIsEmpty(new Vector().elements()));
        Vector v = new Vector();
        v.add("A");
        Assert.assertFalse(CollectionUtils.sizeIsEmpty(v.elements()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeIsEmptyNull() {
        CollectionUtils.sizeIsEmpty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeIsEmptyUnsupported() {
        CollectionUtils.sizeIsEmpty(new Object());
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        Assert.assertTrue(CollectionUtils.isEmpty(null));
        Assert.assertTrue(CollectionUtils.isEmpty(new ArrayList()));
        Assert.assertFalse(CollectionUtils.isEmpty(Arrays.asList(new Object[]{"A"})));

        Assert.assertFalse(CollectionUtils.isNotEmpty(null));
        Assert.assertFalse(CollectionUtils.isNotEmpty(new ArrayList()));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Arrays.asList(new Object[]{"A"})));
    }

    @Test
    public void testReverseArray() {
        Object[] empty = new Object[0];
        CollectionUtils.reverseArray(empty);
        Assert.assertEquals(0, empty.length);

        Object[] single = new Object[]{"A"};
        CollectionUtils.reverseArray(single);
        Assert.assertArrayEquals(new Object[]{"A"}, single);

        Object[] even = new Object[]{"A", "B", "C", "D"};
        CollectionUtils.reverseArray(even);
        Assert.assertArrayEquals(new Object[]{"D", "C", "B", "A"}, even);

        Object[] odd = new Object[]{"1", "2", "3"};
        CollectionUtils.reverseArray(odd);
        Assert.assertArrayEquals(new Object[]{"3", "2", "1"}, odd);
    }

    @Test
    public void testIsFullAndMaxSize() {
        BoundedFifoBuffer buffer = new BoundedFifoBuffer(2);
        Assert.assertFalse(CollectionUtils.isFull(buffer));
        Assert.assertEquals(2, CollectionUtils.maxSize(buffer));

        buffer.add("A");
        buffer.add("B");
        Assert.assertTrue(CollectionUtils.isFull(buffer));
        Assert.assertEquals(2, CollectionUtils.maxSize(buffer));

        Collection unmodBounded = UnmodifiableBoundedCollection.decorate(buffer);
        Assert.assertTrue(CollectionUtils.isFull(unmodBounded));
        Assert.assertEquals(2, CollectionUtils.maxSize(unmodBounded));

        List normalList = new ArrayList();
        Assert.assertFalse(CollectionUtils.isFull(normalList));
        Assert.assertEquals(-1, CollectionUtils.maxSize(normalList));
    }

    @Test(expected = NullPointerException.class)
    public void testIsFullNull() {
        CollectionUtils.isFull(null);
    }

    @Test(expected = NullPointerException.class)
    public void testMaxSizeNull() {
        CollectionUtils.maxSize(null);
    }

    @Test
    public void testRetainAllAndRemoveAll() {
        List a = Arrays.asList(new Object[]{"A", "B", "C"});
        List b = Arrays.asList(new Object[]{"A", "C", "D"});

        Collection retained = CollectionUtils.retainAll(a, b);
        Assert.assertEquals(2, retained.size());
        Assert.assertTrue(retained.contains("A"));
        Assert.assertTrue(retained.contains("C"));

        Collection removed = CollectionUtils.removeAll(a, b);
        // Note: removeAll in target source delegates to ListUtils.retainAll
        Assert.assertEquals(2, removed.size());
    }

    @Test
    public void testDecorators() {
        List list = new ArrayList();
        list.add("test");

        Collection sync = CollectionUtils.synchronizedCollection(list);
        Assert.assertNotNull(sync);
        Assert.assertEquals(1, sync.size());

        Collection unmod = CollectionUtils.unmodifiableCollection(list);
        Assert.assertNotNull(unmod);
        Assert.assertEquals(1, unmod.size());

        Collection pred = CollectionUtils.predicatedCollection(new ArrayList(), NotNullPredicate.getInstance());
        pred.add("valid");
        try {
            pred.add(null);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        Collection typed = CollectionUtils.typedCollection(new ArrayList(), String.class);
        typed.add("valid");
        try {
            typed.add(new Integer(1));
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        Collection transf = CollectionUtils.transformedCollection(new ArrayList(), new Transformer() {
            public Object transform(Object input) {
                return "Prefix_" + input;
            }
        });
        transf.add("item");
        Assert.assertTrue(transf.contains("Prefix_item"));
    }
}
