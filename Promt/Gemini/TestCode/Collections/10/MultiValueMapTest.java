package org.apache.commons.collections.map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
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
import java.util.TreeSet;

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
        Map base = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(base);
        map.put("key1", "val1");
        Assert.assertEquals(1, map.size());
        Assert.assertTrue(base.containsKey("key1"));
        Assert.assertTrue(base.get("key1") instanceof ArrayList);
    }

    @Test
    public void testDecorateMapWithCollectionClass() {
        Map base = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(base, HashSet.class);
        map.put("key1", "val1");
        Assert.assertTrue(map.getCollection("key1") instanceof HashSet);
    }

    @Test
    public void testDecorateMapWithFactory() {
        Map base = new HashMap();
        Factory factory = new Factory() {
            public Object create() {
                return new TreeSet();
            }
        };
        MultiValueMap map = MultiValueMap.decorate(base, factory);
        map.put("key1", "val1");
        Assert.assertTrue(map.getCollection("key1") instanceof TreeSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFactory() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test(expected = FunctorException.class)
    public void testReflectionFactoryInstantiationFailure() {
        // MultiValueMap with an uninstantiable class (e.g. abstract or interface)
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), Collection.class);
        map.put("key", "val");
    }

    @Test
    public void testPutAndGetCollection() {
        MultiValueMap map = new MultiValueMap();
        Object returned = map.put("A", "1");
        Assert.assertEquals("1", returned);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, map.totalSize());

        returned = map.put("A", "2");
        Assert.assertEquals("2", returned);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(2, map.totalSize());

        Collection coll = map.getCollection("A");
        Assert.assertNotNull(coll);
        Assert.assertEquals(2, coll.size());
        Assert.assertTrue(coll.contains("1"));
        Assert.assertTrue(coll.contains("2"));

        Assert.assertNull(map.getCollection("NonExistent"));
    }

    @Test
    public void testPutNoChangeBehavior() {
        // Set will not add duplicate elements
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        Object res1 = map.put("key", "duplicate");
        Assert.assertEquals("duplicate", res1);
        Object res2 = map.put("key", "duplicate");
        Assert.assertNull(res2);
        Assert.assertEquals(1, map.totalSize());
    }

    @Test
    public void testPutCustomCollectionZeroSize() {
        // Test custom collection that ignores elements or stays empty
        Factory emptyCollFactory = new Factory() {
            public Object create() {
                return new AbstractCollection() {
                    public Iterator iterator() {
                        return Collections.emptyIterator();
                    }
                    public int size() {
                        return 0;
                    }
                    public boolean add(Object o) {
                        return false;
                    }
                };
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), emptyCollFactory);
        Object res = map.put("key", "val");
        Assert.assertNull(res);
        Assert.assertFalse(map.containsKey("key"));
    }

    @Test
    public void testPutAllKeyCollection() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertFalse(map.putAll("key1", null));
        Assert.assertFalse(map.putAll("key1", Collections.emptyList()));

        Collection values = Arrays.asList("A", "B", "C");
        Assert.assertTrue(map.putAll("key1", values));
        Assert.assertEquals(3, map.size("key1"));

        Collection moreValues = Arrays.asList("D", "E");
        Assert.assertTrue(map.putAll("key1", moreValues));
        Assert.assertEquals(5, map.size("key1"));
        Assert.assertEquals(5, map.totalSize());
    }

    @Test
    public void testPutAllKeyCollectionNoChange() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map.put("key", "A");
        boolean changed = map.putAll("key", Collections.singletonList("A"));
        Assert.assertFalse(changed);
        Assert.assertEquals(1, map.size("key"));
    }

    @Test
    public void testPutAllKeyCollectionCustomEmpty() {
        Factory emptyCollFactory = new Factory() {
            public Object create() {
                return new ArrayList() {
                    public int size() {
                        return 0;
                    }
                };
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), emptyCollFactory);
        boolean changed = map.putAll("key", Arrays.asList("A", "B"));
        Assert.assertFalse(changed);
        Assert.assertFalse(map.containsKey("key"));
    }

    @Test
    public void testPutAllNormalMap() {
        Map normalMap = new HashMap();
        normalMap.put("k1", "v1");
        normalMap.put("k2", "v2");

        MultiValueMap map = new MultiValueMap();
        map.putAll(normalMap);

        Assert.assertEquals(2, map.size());
        Assert.assertEquals(1, map.size("k1"));
        Assert.assertEquals(1, map.size("k2"));
        Assert.assertTrue(map.containsValue("k1", "v1"));
        Assert.assertTrue(map.containsValue("k2", "v2"));
    }

    @Test
    public void testPutAllMultiMap() {
        MultiValueMap src = new MultiValueMap();
        src.put("k1", "v1");
        src.put("k1", "v2");
        src.put("k2", "v3");

        MultiValueMap dest = new MultiValueMap();
        dest.put("k1", "v0");
        dest.putAll(src);

        Assert.assertEquals(2, dest.size());
        Assert.assertEquals(3, dest.size("k1"));
        Assert.assertEquals(1, dest.size("k2"));
        Assert.assertEquals(4, dest.totalSize());
    }

    @Test
    public void testSizeForKey() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.size("none"));
        map.put("k1", "v1");
        map.put("k1", "v2");
        Assert.assertEquals(2, map.size("k1"));
    }

    @Test
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.totalSize());
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        Assert.assertEquals(3, map.totalSize());
    }

    @Test
    public void testContainsValueObject() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertFalse(map.containsValue("v1"));

        map.put("k1", "v1");
        map.put("k2", "v2");

        Assert.assertTrue(map.containsValue("v1"));
        Assert.assertTrue(map.containsValue("v2"));
        Assert.assertFalse(map.containsValue("v3"));
    }

    @Test
    public void testContainsValueObjectNullPairs() {
        Map mockMap = new HashMap() {
            public Set entrySet() {
                return null;
            }
        };
        MultiValueMap map = MultiValueMap.decorate(mockMap);
        Assert.assertFalse(map.containsValue("any"));
    }

    @Test
    public void testContainsValueKeyObject() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertFalse(map.containsValue("k1", "v1"));

        map.put("k1", "v1");
        map.put("k1", "v2");

        Assert.assertTrue(map.containsValue("k1", "v1"));
        Assert.assertTrue(map.containsValue("k1", "v2"));
        Assert.assertFalse(map.containsValue("k1", "v3"));
        Assert.assertFalse(map.containsValue("k2", "v1"));
    }

    @Test
    public void testRemoveMapping() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertNull(map.removeMapping("k1", "v1"));

        map.put("k1", "v1");
        map.put("k1", "v2");

        Assert.assertNull(map.removeMapping("k1", "v3"));
        Assert.assertEquals(2, map.size("k1"));

        Object removed = map.removeMapping("k1", "v1");
        Assert.assertEquals("v1", removed);
        Assert.assertEquals(1, map.size("k1"));
        Assert.assertTrue(map.containsKey("k1"));

        removed = map.removeMapping("k1", "v2");
        Assert.assertEquals("v2", removed);
        Assert.assertEquals(0, map.size("k1"));
        Assert.assertFalse(map.containsKey("k1"));
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
        Iterator emptyIt = map.iterator("k1");
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
        Assert.assertTrue(map.containsKey("k1"));

        Assert.assertEquals("v2", it.next());
        it.remove();
        Assert.assertEquals(0, map.size("k1"));
        Assert.assertFalse(map.containsKey("k1"));
    }

    @Test
    public void testValuesCollection() {
        MultiValueMap map = new MultiValueMap();
        Collection vals = map.values();
        Assert.assertNotNull(vals);
        Assert.assertSame(vals, map.values()); // Cached view
        Assert.assertEquals(0, vals.size());

        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        Assert.assertEquals(3, vals.size());

        ListContentCollector collector = new ListContentCollector();
        for (Object o : vals) {
            collector.add(o);
        }
        Assert.assertEquals(3, collector.list.size());
        Assert.assertTrue(collector.list.contains("v1"));
        Assert.assertTrue(collector.list.contains("v2"));
        Assert.assertTrue(collector.list.contains("v3"));

        vals.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
    }

    @Test
    public void testValuesIteratorRemoveThroughValuesView() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Collection vals = map.values();
        Iterator it = vals.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }

        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
    }

    @Test
    public void testSerialization() throws Exception {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap deserialized = (MultiValueMap) ois.readObject();
        ois.close();

        Assert.assertEquals(2, deserialized.size());
        Assert.assertEquals(3, deserialized.totalSize());
        Assert.assertTrue(deserialized.containsValue("k1", "v1"));
        Assert.assertTrue(deserialized.containsValue("k1", "v2"));
        Assert.assertTrue(deserialized.containsValue("k2", "v3"));
    }

    private static class ListContentCollector {
        final ArrayList list = new ArrayList();
        void add(Object o) {
            list.add(o);
        }
    }
}
