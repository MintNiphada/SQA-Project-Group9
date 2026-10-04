package org.apache.commons.collections4.map;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testDefaultConstructor() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFactory() {
        new MultiValueMap<String, String>(new HashMap<String, Collection<String>>(), null);
    }

    @Test
    public void testStaticMultiValueMapWithMapOnly() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testStaticMultiValueMapWithMapAndClass() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base, ArrayList.class);
        assertNotNull(map);
        map.put("key", "value");
        assertTrue(map.getCollection("key") instanceof ArrayList);
    }

    @Test
    public void testStaticMultiValueMapWithMapAndFactory() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        Factory<Collection<String>> factory = new Factory<Collection<String>>() {
            @Override
            public Collection<String> create() {
                return new ArrayList<String>();
            }
        };
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base, factory);
        assertNotNull(map);
        map.put("key", "value");
        assertTrue(map.getCollection("key") instanceof ArrayList);
    }

    @Test
    public void testPutNewKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Object result = map.put("key", "value1");
        assertEquals("value1", result);
        assertEquals(1, map.size("key"));
        assertTrue(map.containsValue("key", "value1"));
    }

    @Test
    public void testPutExistingKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "value1");
        Object result = map.put("key", "value2");
        assertEquals("value2", result);
        assertEquals(2, map.size("key"));
        assertTrue(map.containsValue("key", "value2"));
    }

    @Test
    public void testPutNullValue() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Object result = map.put("key", null);
        assertNull(result);
        assertEquals(1, map.size("key"));
        assertTrue(map.containsValue("key", null));
    }

    @Test
    public void testPutDuplicateWithSet() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base, HashSet.class);
        map.put("key", "value");
        Object result = map.put("key", "value");
        assertNull(result);
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testPutAllMapNormal() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Map<String, String> normal = new HashMap<String, String>();
        normal.put("key1", "val1");
        normal.put("key2", "val2");
        map.putAll(normal);
        assertEquals(1, map.size("key1"));
        assertEquals(1, map.size("key2"));
        assertTrue(map.containsValue("key1", "val1"));
        assertTrue(map.containsValue("key2", "val2"));
    }

    @Test
    public void testPutAllMapMultiMap() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        MultiValueMap<String, String> other = new MultiValueMap<String, String>();
        other.put("key", "val1");
        other.put("key", "val2");
        map.putAll(other);
        assertEquals(2, map.size("key"));
        assertTrue(map.containsValue("key", "val1"));
        assertTrue(map.containsValue("key", "val2"));
    }

    @Test
    public void testPutAllKeyCollectionNull() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        boolean changed = map.putAll("key", null);
        assertFalse(changed);
        assertNull(map.getCollection("key"));
    }

    @Test
    public void testPutAllKeyCollectionEmpty() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        boolean changed = map.putAll("key", new ArrayList<String>());
        assertFalse(changed);
        assertNull(map.getCollection("key"));
    }

    @Test
    public void testPutAllKeyCollectionNewKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Collection<String> values = new ArrayList<String>();
        values.add("val1");
        values.add("val2");
        boolean changed = map.putAll("key", values);
        assertTrue(changed);
        assertEquals(2, map.size("key"));
    }

    @Test
    public void testPutAllKeyCollectionExistingKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "existing");
        Collection<String> values = new ArrayList<String>();
        values.add("val1");
        boolean changed = map.putAll("key", values);
        assertTrue(changed);
        assertEquals(2, map.size("key"));
    }

    @Test
    public void testPutAllKeyCollectionDuplicateWithSet() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base, HashSet.class);
        map.put("key", "val1");
        Collection<String> values = new ArrayList<String>();
        values.add("val1");
        boolean changed = map.putAll("key", values);
        assertFalse(changed);
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testRemoveMappingKeyNotPresent() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertFalse(map.removeMapping("key", "value"));
    }

    @Test
    public void testRemoveMappingValueNotPresent() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "value1");
        assertFalse(map.removeMapping("key", "value2"));
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testRemoveMappingValuePresent() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "value1");
        map.put("key", "value2");
        assertTrue(map.removeMapping("key", "value1"));
        assertEquals(1, map.size("key"));
        assertFalse(map.containsValue("key", "value1"));
    }

    @Test
    public void testRemoveMappingLastValueRemovesKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "value");
        assertTrue(map.removeMapping("key", "value"));
        assertNull(map.getCollection("key"));
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testContainsValueObject() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        assertTrue(map.containsValue("val1"));
        assertTrue(map.containsValue("val2"));
        assertFalse(map.containsValue("val3"));
    }

    @Test
    public void testContainsValueObjectEmpty() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertFalse(map.containsValue("anything"));
    }

    @Test
    public void testContainsValueKeyValue() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        assertTrue(map.containsValue("key", "val"));
        assertFalse(map.containsValue("key", "other"));
        assertFalse(map.containsValue("other", "val"));
    }

    @Test
    public void testGetCollection() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertNull(map.getCollection("key"));
        map.put("key", "val");
        Collection<String> coll = map.getCollection("key");
        assertNotNull(coll);
        assertEquals(1, coll.size());
    }

    @Test
    public void testSizeKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertEquals(0, map.size("key"));
        map.put("key", "val");
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testIteratorKeyPresent() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val1");
        map.put("key", "val2");
        Iterator<String> it = map.iterator("key");
        assertTrue(it.hasNext());
        assertEquals("val1", it.next());
        assertEquals("val2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorKeyAbsent() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Iterator<String> it = map.iterator("key");
        assertTrue(it instanceof EmptyIterator);
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorAll() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");
        Iterator<Map.Entry<String, String>> it = map.iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry1 = it.next();
        assertEquals("key1", entry1.getKey());
        assertEquals("val1", entry1.getValue());
        Map.Entry<String, String> entry2 = it.next();
        assertEquals("key1", entry2.getKey());
        assertEquals("val2", entry2.getValue());
        Map.Entry<String, String> entry3 = it.next();
        assertEquals("key2", entry3.getKey());
        assertEquals("val3", entry3.getValue());
        assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorAllEntrySetValue() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        Iterator<Map.Entry<String, String>> it = map.iterator();
        Map.Entry<String, String> entry = it.next();
        entry.setValue("newval");
    }

    @Test
    public void testTotalSize() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertEquals(0, map.totalSize());
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");
        assertEquals(3, map.totalSize());
    }

    @Test
    public void testValuesView() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        Collection<Object> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("val1"));
        assertTrue(values.contains("val2"));
    }

    @Test
    public void testValuesViewClear() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        Collection<Object> values = map.values();
        values.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, values.size());
    }

    @Test
    public void testValuesViewIteratorRemove() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val1");
        map.put("key", "val2");
        Collection<Object> values = map.values();
        Iterator<Object> it = values.iterator();
        it.next();
        it.remove();
        assertEquals(1, map.totalSize());
        assertEquals(1, values.size());
        assertFalse(map.containsValue("key", "val1"));
    }

    @Test
    public void testValuesViewIteratorRemoveLastRemovesKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        Collection<Object> values = map.values();
        Iterator<Object> it = values.iterator();
        it.next();
        it.remove();
        assertFalse(map.containsKey("key"));
        assertEquals(0, map.totalSize());
    }

    @Test
    public void testEntrySet() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        assertEquals(1, entries.size());
        Map.Entry<String, Object> entry = entries.iterator().next();
        assertEquals("key", entry.getKey());
        assertTrue(entry.getValue() instanceof Collection);
        Collection<?> coll = (Collection<?>) entry.getValue();
        assertTrue(coll.contains("val"));
    }

    @Test
    public void testClear() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap<String, String> deserialized = (MultiValueMap<String, String>) ois.readObject();
        ois.close();

        assertEquals(map.totalSize(), deserialized.totalSize());
        assertTrue(deserialized.containsValue("key1", "val1"));
        assertTrue(deserialized.containsValue("key1", "val2"));
        assertTrue(deserialized.containsValue("key2", "val3"));
    }

    @Test
    public void testValuesIteratorRemove() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val1");
        map.put("key", "val2");
        Iterator<String> it = map.iterator("key");
        it.next();
        it.remove();
        assertEquals(1, map.size("key"));
        assertFalse(map.containsValue("key", "val1"));
    }

    @Test
    public void testValuesIteratorRemoveLastRemovesKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val");
        Iterator<String> it = map.iterator("key");
        it.next();
        it.remove();
        assertFalse(map.containsKey("key"));
        assertEquals(0, map.totalSize());
    }

    @Test
    public void testPutAllMapEmpty() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.putAll(new HashMap<String, String>());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testContainsValueNull() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", null);
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("key", null));
    }

    @Test
    public void testTotalSizeAfterRemoveMapping() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key", "val1");
        map.put("key", "val2");
        map.removeMapping("key", "val1");
        assertEquals(1, map.totalSize());
    }

    @Test
    public void testIteratorAllEmpty() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Iterator<Map.Entry<String, String>> it = map.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testValuesViewIteratorRemoveMultipleKeys() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        Collection<Object> values = map.values();
        Iterator<Object> it = values.iterator();
        it.next();
        it.remove();
        assertEquals(1, map.totalSize());
    }

    @Test
    public void testPutAllKeyCollectionWithSetFactory() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(base, HashSet.class);
        Collection<String> vals = new ArrayList<String>();
        vals.add("a");
        vals.add("b");
        map.putAll("key", vals);
        assertEquals(2, map.size("key"));
        assertTrue(map.getCollection("key") instanceof HashSet);
    }

    @Test
    public void testReflectionFactoryInvalidClass() {
        Map<String, Collection<String>> base = new HashMap<String, Collection<String>>();
        try {
            MultiValueMap.multiValueMap(base, (Class) Integer.class);
            fail("Expected FunctorException");
        } catch (org.apache.commons.collections4.FunctorException e) {
        }
    }
}
