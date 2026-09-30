package org.apache.commons.collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.junit.Assert;
import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testDefaultConstructor() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testDecorateMap() {
        Map baseMap = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(baseMap);
        Assert.assertNotNull(map);
        map.put("A", "1");
        Assert.assertTrue(baseMap.containsKey("A"));
        Assert.assertTrue(baseMap.get("A") instanceof ArrayList);
    }

    @Test
    public void testDecorateMapWithClass() {
        Map baseMap = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(baseMap, HashSet.class);
        Assert.assertNotNull(map);
        map.put("A", "1");
        map.put("A", "1"); // duplicate in set should not increase size
        Assert.assertTrue(baseMap.get("A") instanceof HashSet);
        Assert.assertEquals(1, map.size("A"));
    }

    @Test
    public void testDecorateMapWithFactory() {
        Map baseMap = new HashMap();
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList();
            }
        };
        MultiValueMap map = MultiValueMap.decorate(baseMap, factory);
        Assert.assertNotNull(map);
        map.put("A", "1");
        Assert.assertEquals(1, map.totalSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFactory() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test(expected = FunctorException.class)
    public void testReflectionFactoryInstantiationFailure() {
        // Abstract class cannot be instantiated by ReflectionFactory
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), Collection.class);
        map.put("key", "val");
    }

    @Test
    public void testPutAndGetCollection() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertNull(map.getCollection("key1"));

        Object putResult1 = map.put("key1", "val1");
        Assert.assertNull(putResult1); // returns null on first insert because size > 0 branch sets result = false
        Assert.assertEquals(1, map.totalSize());
        Assert.assertEquals(1, map.size("key1"));

        Collection coll = map.getCollection("key1");
        Assert.assertNotNull(coll);
        Assert.assertEquals(1, coll.size());
        Assert.assertTrue(coll.contains("val1"));

        Object putResult2 = map.put("key1", "val2");
        Assert.assertEquals("val2", putResult2);
        Assert.assertEquals(2, map.totalSize());
        Assert.assertEquals(2, map.size("key1"));
        Assert.assertEquals(2, map.getCollection("key1").size());
    }

    @Test
    public void testPutAllCollection() {
        MultiValueMap map = new MultiValueMap();

        // null or empty collection
        Assert.assertFalse(map.putAll("key1", null));
        Assert.assertFalse(map.putAll("key1", Collections.emptyList()));
        Assert.assertEquals(0, map.totalSize());

        // new key
        Collection values1 = Arrays.asList("A", "B");
        boolean result1 = map.putAll("key1", values1);
        Assert.assertFalse(result1); // because coll.size() > 0 resets result to false
        Assert.assertEquals(2, map.size("key1"));

        // existing key
        Collection values2 = Arrays.asList("C", "D");
        boolean result2 = map.putAll("key1", values2);
        Assert.assertTrue(result2);
        Assert.assertEquals(4, map.size("key1"));
        Assert.assertEquals(4, map.totalSize());
    }

    @Test
    public void testPutAllMapStandard() {
        Map standardMap = new HashMap();
        standardMap.put("K1", "V1");
        standardMap.put("K2", "V2");

        MultiValueMap map = new MultiValueMap();
        map.putAll(standardMap);

        Assert.assertEquals(2, map.size());
        Assert.assertEquals(2, map.totalSize());
        Assert.assertTrue(map.containsValue("K1", "V1"));
        Assert.assertTrue(map.containsValue("K2", "V2"));
    }

    @Test
    public void testPutAllMapMultiMap() {
        MultiValueMap source = new MultiValueMap();
        source.put("K1", "V1");
        source.put("K1", "V2");
        source.put("K2", "V3");

        MultiValueMap target = new MultiValueMap();
        target.putAll(source);

        Assert.assertEquals(2, target.size());
        Assert.assertEquals(3, target.totalSize());
        Assert.assertEquals(2, target.size("K1"));
        Assert.assertEquals(1, target.size("K2"));
    }

    @Test
    public void testRemoveMapping() {
        MultiValueMap map = new MultiValueMap();

        // Remove from non-existent key
        Assert.assertNull(map.removeMapping("key1", "val1"));

        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        // Remove non-existent value from existing key
        Assert.assertNull(map.removeMapping("key1", "val_none"));
        Assert.assertEquals(2, map.size("key1"));

        // Remove one value from key with multiple values
        Object removedVal = map.removeMapping("key1", "val1");
        Assert.assertEquals("val1", removedVal);
        Assert.assertEquals(1, map.size("key1"));
        Assert.assertTrue(map.containsKey("key1"));

        // Remove last value from key -> should remove key entirely
        removedVal = map.removeMapping("key1", "val2");
        Assert.assertEquals("val2", removedVal);
        Assert.assertFalse(map.containsKey("key1"));
        Assert.assertNull(map.getCollection("key1"));
        Assert.assertEquals(0, map.size("key1"));
    }

    @Test
    public void testContainsValue() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertFalse(map.containsValue("val1"));

        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        Assert.assertTrue(map.containsValue("val1"));
        Assert.assertTrue(map.containsValue("val2"));
        Assert.assertTrue(map.containsValue("val3"));
        Assert.assertFalse(map.containsValue("val4"));

        // Test with mock/wrapper where entrySet is null
        MultiValueMap customMap = new MultiValueMap(new HashMap() {
            public Set entrySet() {
                return null;
            }
        }, new Factory() {
            public Object create() {
                return new ArrayList();
            }
        });
        Assert.assertFalse(customMap.containsValue("val1"));
    }

    @Test
    public void testContainsValueWithKey() {
        MultiValueMap map = new MultiValueMap();

        Assert.assertFalse(map.containsValue("key1", "val1"));

        map.put("key1", "val1");
        Assert.assertTrue(map.containsValue("key1", "val1"));
        Assert.assertFalse(map.containsValue("key1", "val2"));
        Assert.assertFalse(map.containsValue("key2", "val1"));
    }

    @Test
    public void testSize() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.size("key1"));

        map.put("key1", "val1");
        Assert.assertEquals(1, map.size("key1"));

        map.put("key1", "val2");
        Assert.assertEquals(2, map.size("key1"));
    }

    @Test
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.totalSize());

        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        Assert.assertEquals(3, map.totalSize());

        map.clear();
        Assert.assertEquals(0, map.totalSize());
    }

    @Test
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        Assert.assertEquals(2, map.size());

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
        Assert.assertNull(map.getCollection("k1"));
    }

    @Test
    public void testIteratorForKey() {
        MultiValueMap map = new MultiValueMap();

        // Non-existent key returns EmptyIterator
        Iterator emptyIt = map.iterator("nonexistent");
        Assert.assertSame(EmptyIterator.INSTANCE, emptyIt);
        Assert.assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k1", "v2");

        Iterator it = map.iterator("k1");
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("v1", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("v2", it.next());
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testValuesIteratorRemove() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");

        Iterator it = map.iterator("k1");
        Assert.assertEquals("v1", it.next());
        it.remove();

        Assert.assertEquals(1, map.size("k1"));
        Assert.assertEquals(1, map.totalSize());
        Assert.assertTrue(map.containsKey("k1"));

        Assert.assertEquals("v2", it.next());
        it.remove();

        // Removing the last value should remove key from map
        Assert.assertFalse(map.containsKey("k1"));
        Assert.assertEquals(0, map.size("k1"));
        Assert.assertEquals(0, map.totalSize());
    }

    @Test
    public void testValuesView() {
        MultiValueMap map = new MultiValueMap();
        Collection valuesView = map.values();
        Assert.assertNotNull(valuesView);
        Assert.assertEquals(0, valuesView.size());

        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        // Cached values view returns same instance
        Assert.assertSame(valuesView, map.values());
        Assert.assertEquals(3, valuesView.size());

        Set collected = new HashSet();
        for (Iterator it = valuesView.iterator(); it.hasNext();) {
            collected.add(it.next());
        }
        Assert.assertEquals(3, collected.size());
        Assert.assertTrue(collected.contains("v1"));
        Assert.assertTrue(collected.contains("v2"));
        Assert.assertTrue(collected.contains("v3"));

        valuesView.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, valuesView.size());
    }

    @Test
    public void testCustomCreateCollection() {
        MultiValueMap map = new MultiValueMap(new HashMap(), new Factory() {
            public Object create() {
                return new ArrayList();
            }
        }) {
            protected Collection createCollection(int size) {
                return new HashSet(size);
            }
        };

        map.put("k1", "v1");
        Assert.assertTrue(map.getCollection("k1") instanceof HashSet);

        map.putAll("k2", Arrays.asList("a", "b"));
        Assert.assertTrue(map.getCollection("k2") instanceof HashSet);
    }
}
