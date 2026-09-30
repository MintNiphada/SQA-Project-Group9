package org.apache.commons.collections.map;

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

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testDefaultConstructor() {
        MultiValueMap map = new MultiValueMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
        assertNotNull(map.values());
        assertTrue(map.values().isEmpty());
    }

    @Test
    public void testDecorateWithMap() {
        Map base = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(base);
        assertSame(base, map.getMap());
        // default factory should create ArrayList
        map.put("key", "value");
        Collection coll = map.getCollection("key");
        assertTrue(coll instanceof ArrayList);
    }

    @Test
    public void testDecorateWithClass() {
        Map base = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(base, HashSet.class);
        map.put("key", "value");
        Collection coll = map.getCollection("key");
        assertTrue(coll instanceof HashSet);
    }

    @Test
    public void testDecorateWithFactory() {
        Map base = new HashMap();
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList();
            }
        };
        MultiValueMap map = MultiValueMap.decorate(base, factory);
        map.put("key", "value");
        Collection coll = map.getCollection("key");
        assertTrue(coll instanceof ArrayList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFactory() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test
    public void testPutAndGet() {
        MultiValueMap map = new MultiValueMap();
        // put first value
        Object result = map.put("key", "value1");
        assertEquals("value1", result);
        assertEquals(1, map.size("key"));
        assertTrue(map.containsValue("key", "value1"));
        // put second value
        result = map.put("key", "value2");
        assertEquals("value2", result);
        assertEquals(2, map.size("key"));
        assertTrue(map.containsValue("key", "value2"));
        // get collection
        Collection coll = map.getCollection("key");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("value1"));
        assertTrue(coll.contains("value2"));
        // put duplicate value (ArrayList allows duplicates)
        result = map.put("key", "value1");
        assertEquals("value1", result);
        assertEquals(3, map.size("key"));
    }

    @Test
    public void testPutWithSetFactory() {
        Map base = new HashMap();
        MultiValueMap map = MultiValueMap.decorate(base, HashSet.class);
        // first put
        Object result = map.put("key", "value1");
        assertEquals("value1", result);
        assertEquals(1, map.size("key"));
        // put duplicate value - should return null because add returns false
        result = map.put("key", "value1");
        assertNull(result);
        assertEquals(1, map.size("key"));
        // put different value
        result = map.put("key", "value2");
        assertEquals("value2", result);
        assertEquals(2, map.size("key"));
    }

    @Test
    public void testPutWithFactoryReturningNonEmptyCollection() {
        Factory factory = new Factory() {
            public Object create() {
                ArrayList list = new ArrayList();
                list.add("pre-existing");
                return list;
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), factory);
        // put new key: createCollection returns non-empty list, then add value
        Object result = map.put("key", "newValue");
        assertEquals("newValue", result);
        Collection coll = map.getCollection("key");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("pre-existing"));
        assertTrue(coll.contains("newValue"));
    }

    @Test
    public void testPutAllMapWithNormalMap() {
        MultiValueMap map = new MultiValueMap();
        Map normal = new HashMap();
        normal.put("key1", "value1");
        normal.put("key2", "value2");
        map.putAll(normal);
        assertEquals(1, map.size("key1"));
        assertEquals(1, map.size("key2"));
        assertTrue(map.containsValue("key1", "value1"));
        assertTrue(map.containsValue("key2", "value2"));
    }

    @Test
    public void testPutAllMapWithMultiMap() {
        MultiValueMap map = new MultiValueMap();
        MultiValueMap other = new MultiValueMap();
        other.put("key", "value1");
        other.put("key", "value2");
        map.putAll(other);
        assertEquals(2, map.size("key"));
        assertTrue(map.containsValue("key", "value1"));
        assertTrue(map.containsValue("key", "value2"));
    }

    @Test
    public void testPutAllKeyCollection() {
        MultiValueMap map = new MultiValueMap();
        Collection values = new ArrayList();
        values.add("value1");
        values.add("value2");
        boolean changed = map.putAll("key", values);
        assertTrue(changed);
        assertEquals(2, map.size("key"));
        // putAll with null collection
        changed = map.putAll("key2", null);
        assertFalse(changed);
        assertNull(map.getCollection("key2"));
        // putAll with empty collection
        changed = map.putAll("key3", new ArrayList());
        assertFalse(changed);
        assertNull(map.getCollection("key3"));
    }

    @Test
    public void testPutAllKeyCollectionWithExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        Collection newValues = new ArrayList();
        newValues.add("value2");
        newValues.add("value3");
        boolean changed = map.putAll("key", newValues);
        assertTrue(changed);
        assertEquals(3, map.size("key"));
    }

    @Test
    public void testRemoveMapping() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        // remove existing value
        Object removed = map.removeMapping("key", "value1");
        assertEquals("value1", removed);
        assertEquals(1, map.size("key"));
        assertFalse(map.containsValue("key", "value1"));
        // remove non-existing value
        removed = map.removeMapping("key", "value3");
        assertNull(removed);
        // remove last value - key should be removed
        removed = map.removeMapping("key", "value2");
        assertEquals("value2", removed);
        assertNull(map.getCollection("key"));
        assertFalse(map.containsKey("key"));
        // remove from non-existing key
        removed = map.removeMapping("noKey", "value");
        assertNull(removed);
    }

    @Test
    public void testContainsValueObject() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.containsValue("anything"));
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertTrue(map.containsValue("value1"));
        assertTrue(map.containsValue("value2"));
        assertFalse(map.containsValue("value3"));
    }

    @Test
    public void testContainsValueKey() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.containsValue("key", "value"));
        map.put("key", "value1");
        assertTrue(map.containsValue("key", "value1"));
        assertFalse(map.containsValue("key", "value2"));
        assertFalse(map.containsValue("noKey", "value1"));
    }

    @Test
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
        assertNull(map.getCollection("key1"));
    }

    @Test
    public void testValuesView() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        Collection values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertTrue(values.contains("value3"));
        // test clear via values view
        values.clear();
        assertTrue(map.isEmpty());
        // values view should be cached
        assertSame(values, map.values());
    }

    @Test
    public void testIterator() {
        MultiValueMap map = new MultiValueMap();
        // iterator for non-existing key
        Iterator it = map.iterator("noKey");
        assertFalse(it.hasNext());
        assertSame(EmptyIterator.INSTANCE, it);
        // iterator for existing key
        map.put("key", "value1");
        map.put("key", "value2");
        it = map.iterator("key");
        assertTrue(it.hasNext());
        assertEquals("value1", it.next());
        assertEquals("value2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        Iterator it = map.iterator("key");
        it.next();
        it.remove(); // removes value1
        assertEquals(1, map.size("key"));
        assertFalse(map.containsValue("key", "value1"));
        // remove last value
        it.next();
        it.remove(); // removes value2, key should be removed
        assertNull(map.getCollection("key"));
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.totalSize());
        map.put("key1", "value1");
        assertEquals(1, map.totalSize());
        map.put("key1", "value2");
        assertEquals(2, map.totalSize());
        map.put("key2", "value3");
        assertEquals(3, map.totalSize());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        // deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap restored = (MultiValueMap) ois.readObject();
        ois.close();
        // verify
        assertEquals(2, restored.size("key1"));
        assertTrue(restored.containsValue("key1", "value1"));
        assertTrue(restored.containsValue("key1", "value2"));
        assertEquals(1, restored.size("key2"));
        assertTrue(restored.containsValue("key2", "value3"));
        assertEquals(3, restored.totalSize());
        // values view should be recreated
        Collection values = restored.values();
        assertNotNull(values);
        assertEquals(3, values.size());
    }

    @Test
    public void testFactoryThrowsException() {
        Factory badFactory = new Factory() {
            public Object create() {
                throw new RuntimeException("factory failure");
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), badFactory);
        try {
            map.put("key", "value");
            fail("Expected FunctorException");
        } catch (FunctorException e) {
            // expected
        }
    }

    @Test
    public void testNullKeyAndValue() {
        MultiValueMap map = new MultiValueMap();
        // null key
        map.put(null, "value");
        assertEquals(1, map.size(null));
        assertTrue(map.containsValue(null, "value"));
        // null value
        map.put("key", null);
        assertTrue(map.containsValue("key", null));
        // removeMapping with null
        Object removed = map.removeMapping(null, "value");
        assertEquals("value", removed);
        assertNull(map.getCollection(null));
    }

    @Test
    public void testGetCollectionReturnsNullForMissingKey() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.getCollection("missing"));
    }

    @Test
    public void testSizeMethod() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size("key"));
        map.put("key", "value");
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testPutAllKeyCollectionWithFactoryReturningNonEmpty() {
        Factory factory = new Factory() {
            public Object create() {
                ArrayList list = new ArrayList();
                list.add("pre");
                return list;
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), factory);
        Collection newValues = new ArrayList();
        newValues.add("value1");
        boolean changed = map.putAll("key", newValues);
        assertTrue(changed);
        Collection coll = map.getCollection("key");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("pre"));
        assertTrue(coll.contains("value1"));
    }

    @Test
    public void testPutAllKeyCollectionWithSetAndDuplicates() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        Collection values = new ArrayList();
        values.add("value1");
        values.add("value1"); // duplicate
        boolean changed = map.putAll("key", values);
        assertTrue(changed);
        assertEquals(1, map.size("key")); // set ignores duplicate
    }

    @Test
    public void testContainsValueWithNullEntrySet() {
        // This is a tricky case: if the underlying map returns null entrySet,
        // containsValue should return false. We can simulate with a custom map.
        Map base = new HashMap() {
            public Set entrySet() {
                return null;
            }
        };
        MultiValueMap map = MultiValueMap.decorate(base);
        assertFalse(map.containsValue("anything"));
    }

    @Test
    public void testValuesIteratorRemoveWhenCollectionBecomesEmpty() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        Iterator it = map.iterator("key");
        it.next();
        it.remove();
        assertNull(map.getCollection("key"));
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testValuesViewIterator() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        Iterator it = map.values().iterator();
        assertTrue(it.hasNext());
        // order depends on key iteration order, but we can collect
        Set expected = new HashSet();
        expected.add("value1");
        expected.add("value2");
        expected.add("value3");
        int count = 0;
        while (it.hasNext()) {
            Object val = it.next();
            assertTrue(expected.contains(val));
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testPutAllMapWithEmptyMap() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        map.putAll(new HashMap());
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testPutAllMapWithMultiMapEmpty() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        MultiValueMap emptyMulti = new MultiValueMap();
        map.putAll(emptyMulti);
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testRemoveMappingWhenCollectionIsNull() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.removeMapping("key", "value"));
    }

    @Test
    public void testRemoveMappingWhenValueNotPresent() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        assertNull(map.removeMapping("key", "value2"));
        assertEquals(1, map.size("key"));
    }

    @Test
    public void testPutReturnsNullWhenAddReturnsFalse() {
        // Use a Set factory to cause add to return false for duplicate
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map.put("key", "value1");
        Object result = map.put("key", "value1");
        assertNull(result);
    }

    @Test
    public void testPutAllKeyCollectionReturnsFalseWhenAddAllReturnsFalse() {
        // Use a Set factory, addAll may return false if all elements already present
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map.put("key", "value1");
        Collection values = new ArrayList();
        values.add("value1"); // already present
        boolean changed = map.putAll("key", values);
        assertFalse(changed);
    }

    @Test
    public void testCreateCollectionIsCalledWithSize() {
        // We can't easily verify the size argument, but we can ensure it's called.
        // Just a smoke test.
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        assertNotNull(map.getCollection("key"));
    }

    @Test
    public void testSerializationPreservesFactory() throws Exception {
        // Use a custom factory that creates HashSet
        Factory factory = new Factory() {
            public Object create() {
                return new HashSet();
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), factory);
        map.put("key", "value1");
        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        // deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap restored = (MultiValueMap) ois.readObject();
        ois.close();
        // The factory should still produce HashSet
        restored.put("key", "value1"); // duplicate, should be ignored by set
        assertEquals(1, restored.size("key"));
    }
}
