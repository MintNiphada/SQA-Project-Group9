package org.apache.commons.collections4.map;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.MultiMap;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class MultiValueMapTest {

    @Test
    public void testDefaultConstructor() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testFactoryCreationMethods() {
        final Map<String, Object> baseMap1 = new HashMap<String, Object>();
        final MultiValueMap<String, String> map1 = MultiValueMap.multiValueMap(baseMap1);
        Assert.assertNotNull(map1);

        final Map<String, Object> baseMap2 = new HashMap<String, Object>();
        final MultiValueMap<String, String> map2 = MultiValueMap.multiValueMap(baseMap2, HashSet.class);
        Assert.assertNotNull(map2);
        map2.put("k1", "v1");
        Assert.assertTrue(map2.getCollection("k1") instanceof HashSet);

        final Map<String, Object> baseMap3 = new HashMap<String, Object>();
        final Factory<LinkedList<String>> factory = new Factory<LinkedList<String>>() {
            @Override
            public LinkedList<String> create() {
                return new LinkedList<String>();
            }
        };
        final MultiValueMap<String, String> map3 = MultiValueMap.multiValueMap(baseMap3, factory);
        Assert.assertNotNull(map3);
        map3.put("k1", "v1");
        Assert.assertTrue(map3.getCollection("k1") instanceof LinkedList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFactoryConstructor() {
        new MultiValueMap<String, String>(new HashMap<String, Object>(), null);
    }

    @Test(expected = FunctorException.class)
    public void testReflectionFactoryFailure() {
        // AbstractCollection cannot be instantiated directly via newInstance()
        final MultiValueMap<String, String> map = MultiValueMap.multiValueMap(new HashMap<String, Object>(), AbstractCollection.class);
        map.put("k1", "v1");
    }

    @Test
    public void testPutAndGetCollection() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        final Object ret1 = map.put("A", "1");
        Assert.assertEquals("1", ret1);
        final Object ret2 = map.put("A", "2");
        Assert.assertEquals("2", ret2);

        final Collection<String> coll = map.getCollection("A");
        Assert.assertNotNull(coll);
        Assert.assertEquals(2, coll.size());
        Assert.assertTrue(coll.contains("1"));
        Assert.assertTrue(coll.contains("2"));

        Assert.assertNull(map.getCollection("NonExistent"));
    }

    @Test
    public void testPutAllCollection() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();

        Assert.assertFalse(map.putAll("A", null));
        Assert.assertFalse(map.putAll("A", Collections.<String>emptyList()));

        final List<String> list1 = Arrays.asList("1", "2");
        Assert.assertTrue(map.putAll("A", list1));
        Assert.assertEquals(2, map.size("A"));

        final List<String> list2 = Arrays.asList("3", "4");
        Assert.assertTrue(map.putAll("A", list2));
        Assert.assertEquals(4, map.size("A"));

        final Collection<String> coll = map.getCollection("A");
        Assert.assertTrue(coll.containsAll(Arrays.asList("1", "2", "3", "4")));
    }

    @Test
    public void testPutAllMapNormal() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        final Map<String, String> normalMap = new HashMap<String, String>();
        normalMap.put("A", "1");
        normalMap.put("B", "2");

        map.putAll(normalMap);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(1, map.size("A"));
        Assert.assertEquals(1, map.size("B"));
        Assert.assertTrue(map.containsValue("A", "1"));
        Assert.assertTrue(map.containsValue("B", "2"));
    }

    @Test
    public void testPutAllMapMultiMap() {
        final MultiValueMap<String, String> source = new MultiValueMap<String, String>();
        source.put("A", "1");
        source.put("A", "2");
        source.put("B", "3");

        final MultiValueMap<String, String> target = new MultiValueMap<String, String>();
        target.put("A", "0");
        target.putAll((MultiMap<String, String>) source);

        Assert.assertEquals(2, target.size());
        Assert.assertEquals(3, target.size("A"));
        Assert.assertEquals(1, target.size("B"));
        Assert.assertEquals(4, target.totalSize());
    }

    @Test
    public void testRemoveMapping() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Assert.assertFalse(map.removeMapping("k1", "v1"));

        map.put("k1", "v1");
        map.put("k1", "v2");

        Assert.assertFalse(map.removeMapping("k1", "v3"));
        Assert.assertEquals(2, map.size("k1"));

        Assert.assertTrue(map.removeMapping("k1", "v1"));
        Assert.assertEquals(1, map.size("k1"));
        Assert.assertTrue(map.containsKey("k1"));

        Assert.assertTrue(map.removeMapping("k1", "v2"));
        Assert.assertEquals(0, map.size("k1"));
        Assert.assertFalse(map.containsKey("k1"));
        Assert.assertNull(map.getCollection("k1"));
    }

    @Test
    public void testContainsValue() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Assert.assertFalse(map.containsValue("v1"));

        map.put("k1", "v1");
        map.put("k2", "v2");

        Assert.assertTrue(map.containsValue("v1"));
        Assert.assertTrue(map.containsValue("v2"));
        Assert.assertFalse(map.containsValue("v3"));
    }

    @Test
    public void testContainsValueWithKey() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Assert.assertFalse(map.containsValue("k1", "v1"));

        map.put("k1", "v1");
        map.put("k1", "v2");

        Assert.assertTrue(map.containsValue("k1", "v1"));
        Assert.assertTrue(map.containsValue("k1", "v2"));
        Assert.assertFalse(map.containsValue("k1", "v3"));
        Assert.assertFalse(map.containsValue("k2", "v1"));
    }

    @Test
    public void testSize() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Assert.assertEquals(0, map.size("k1"));

        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        Assert.assertEquals(2, map.size("k1"));
        Assert.assertEquals(1, map.size("k2"));
        Assert.assertEquals(0, map.size("k3"));

        Assert.assertEquals(2, map.size()); // Map keys size
        Assert.assertEquals(3, map.totalSize()); // Total values size
    }

    @Test
    public void testClear() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        Assert.assertEquals(2, map.size());

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, map.totalSize());
        Assert.assertFalse(map.containsKey("k1"));
    }

    @Test
    public void testEntrySet() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("k1", "v1");
        map.put("k1", "v2");

        final Set<Map.Entry<String, Object>> entries = map.entrySet();
        Assert.assertEquals(1, entries.size());

        final Map.Entry<String, Object> entry = entries.iterator().next();
        Assert.assertEquals("k1", entry.getKey());
        final Collection<?> values = (Collection<?>) entry.getValue();
        Assert.assertEquals(2, values.size());
    }

    @Test
    public void testValues() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        final Collection<Object> vals = map.values();
        Assert.assertEquals(3, vals.size());
        Assert.assertSame(vals, map.values()); // Test caching of valuesView

        final List<Object> collected = new ArrayList<Object>();
        for (final Object val : vals) {
            collected.add(val);
        }
        Assert.assertEquals(3, collected.size());
        Assert.assertTrue(collected.contains("v1"));
        Assert.assertTrue(collected.contains("v2"));
        Assert.assertTrue(collected.contains("v3"));

        vals.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, vals.size());
    }

    @Test
    public void testIteratorKey() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        final Iterator<String> emptyIt = map.iterator("nonExistent");
        Assert.assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k1", "v2");

        final Iterator<String> it = map.iterator("k1");
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("v1", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("v2", it.next());
        Assert.assertFalse(it.hasNext());

        // Test remove via iterator
        final MultiValueMap<String, String> map2 = new MultiValueMap<String, String>();
        map2.put("k1", "v1");
        map2.put("k1", "v2");

        final Iterator<String> it2 = map2.iterator("k1");
        while (it2.hasNext()) {
            it2.next();
            it2.remove();
        }
        Assert.assertFalse(map2.containsKey("k1"));
        Assert.assertEquals(0, map2.size());
    }

    @Test
    public void testFlattenedIterator() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        final Iterator<Map.Entry<String, String>> emptyIt = map.iterator();
        Assert.assertFalse(emptyIt.hasNext());

        map.put("A", "1");
        map.put("A", "2");
        map.put("B", "3");

        final Iterator<Map.Entry<String, String>> it = map.iterator();
        int count = 0;
        final Set<String> expectedPairs = new HashSet<String>(Arrays.asList("A:1", "A:2", "B:3"));
        while (it.hasNext()) {
            final Map.Entry<String, String> entry = it.next();
            Assert.assertNotNull(entry.getKey());
            Assert.assertNotNull(entry.getValue());
            final String pair = entry.getKey() + ":" + entry.getValue();
            Assert.assertTrue(expectedPairs.contains(pair));
            count++;
        }
        Assert.assertEquals(3, count);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFlattenedIteratorSetValueUnsupported() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("A", "1");
        final Iterator<Map.Entry<String, String>> it = map.iterator();
        Assert.assertTrue(it.hasNext());
        final Map.Entry<String, String> entry = it.next();
        entry.setValue("2");
    }

    @Test
    public void testCreateCollectionOverride() {
        final MultiValueMap<String, String> customMap = new MultiValueMap<String, String>(new HashMap<String, Object>(), new Factory<Collection<String>>() {
            @Override
            public Collection<String> create() {
                return new TreeSet<String>();
            }
        }) {
            @Override
            protected Collection<String> createCollection(final int size) {
                return new TreeSet<String>();
            }
        };

        customMap.put("k1", "c");
        customMap.put("k1", "a");
        customMap.put("k1", "b");

        final Collection<String> coll = customMap.getCollection("k1");
        Assert.assertTrue(coll instanceof TreeSet);
        final Iterator<String> it = coll.iterator();
        Assert.assertEquals("a", it.next());
        Assert.assertEquals("b", it.next());
        Assert.assertEquals("c", it.next());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final MultiValueMap<String, String> deserialized = (MultiValueMap<String, String>) ois.readObject();
        ois.close();

        Assert.assertEquals(2, deserialized.size());
        Assert.assertEquals(3, deserialized.totalSize());
        Assert.assertEquals(2, deserialized.size("key1"));
        Assert.assertEquals(1, deserialized.size("key2"));
        Assert.assertTrue(deserialized.containsValue("key1", "val1"));
        Assert.assertTrue(deserialized.containsValue("key1", "val2"));
        Assert.assertTrue(deserialized.containsValue("key2", "val3"));
    }

    @Test
    public void testRemoveNonExistentKeyValuesIterator() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("A", "1");
        map.put("A", "2");

        final Iterator<String> it = map.iterator("A");
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("1", it.next());
        it.remove(); // removes "1", "A" still has ["2"]
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertEquals(1, map.size("A"));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("2", it.next());
        it.remove(); // removes "2", collection becomes empty, "A" removed from map
        Assert.assertFalse(map.containsKey("A"));
        Assert.assertEquals(0, map.size("A"));
    }
}
