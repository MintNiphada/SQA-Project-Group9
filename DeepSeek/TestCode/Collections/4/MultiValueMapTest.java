package org.apache.commons.collections.map;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.junit.Before;
import org.junit.Test;

public class MultiValueMapTest {

    private MultiValueMap map;
    private Map underlyingMap;

    @Before
    public void setUp() {
        underlyingMap = new HashMap();
        map = new MultiValueMap(underlyingMap, new MultiValueMap.ReflectionFactory(ArrayList.class));
    }

    // Helper to create a MultiValueMap with a custom factory
    private MultiValueMap createMapWithFactory(Factory factory) {
        return new MultiValueMap(new HashMap(), factory);
    }

    // Test constructors and static factory methods

    @Test
    public void testDefaultConstructor() {
        MultiValueMap m = new MultiValueMap();
        assertTrue(m.isEmpty());
        // put something to verify it works
        m.put("key", "value");
        assertEquals(1, m.totalSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFactory() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test
    public void testDecorateWithMapOnly() {
        Map base = new HashMap();
        MultiValueMap m = MultiValueMap.decorate(base);
        assertNotNull(m);
        m.put("a", "1");
        assertTrue(base.containsKey("a"));
        Collection coll = (Collection) base.get("a");
        assertTrue(coll instanceof ArrayList);
        assertEquals(1, coll.size());
    }

    @Test
    public void testDecorateWithMapAndClass() {
        Map base = new HashMap();
        MultiValueMap m = MultiValueMap.decorate(base, ArrayList.class);
        m.put("a", "1");
        Collection coll = (Collection) base.get("a");
        assertTrue(coll instanceof ArrayList);
    }

    @Test
    public void testDecorateWithMapAndFactory() {
        Map base = new HashMap();
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList();
            }
        };
        MultiValueMap m = MultiValueMap.decorate(base, factory);
        m.put("a", "1");
        Collection coll = (Collection) base.get("a");
        assertTrue(coll instanceof ArrayList);
    }

    // Test clear

    @Test
    public void testClear() {
        map.put("a", "1");
        map.put("a", "2");
        map.put("b", "3");
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
        assertTrue(underlyingMap.isEmpty());
    }

    // Test removeMapping

    @Test
    public void testRemoveMappingKeyNotPresent() {
        assertNull(map.removeMapping("nonexistent", "value"));
    }

    @Test
    public void testRemoveMappingValueNotPresent() {
        map.put("a", "1");
        assertNull(map.removeMapping("a", "2"));
        assertEquals(1, map.size("a"));
    }

    @Test
    public void testRemoveMappingSingleValue() {
        map.put("a", "1");
        assertEquals("1", map.removeMapping("a", "1"));
        assertNull(map.getCollection("a"));
        assertFalse(map.containsKey("a"));
    }

    @Test
    public void testRemoveMappingMultipleValues() {
        map.put("a", "1");
        map.put("a", "2");
        assertEquals("1", map.removeMapping("a", "1"));
        assertEquals(1, map.size("a"));
        assertTrue(map.containsValue("a", "2"));
        assertFalse(map.containsValue("a", "1"));
    }

    @Test
    public void testRemoveMappingLastValueRemovesKey() {
        map.put("a", "1");
        map.put("a", "2");
        map.removeMapping("a", "1");
        map.removeMapping("a", "2");
        assertNull(map.getCollection("a"));
        assertFalse(map.containsKey("a"));
    }

    // Test containsValue(Object)

    @Test
    public void testContainsValueWithNullEntrySet() {
        // Use a custom map that returns null for entrySet
        Map customMap = new HashMap() {
            public Set entrySet() {
                return null;
            }
        };
        MultiValueMap m = new MultiValueMap(customMap, new MultiValueMap.ReflectionFactory(ArrayList.class));
        assertFalse(m.containsValue("anything"));
    }

    @Test
    public void testContainsValueEmptyMap() {
        assertFalse(map.containsValue("anything"));
    }

    @Test
    public void testContainsValuePresent() {
        map.put("a", "1");
        map.put("b", "2");
        assertTrue(map.containsValue("1"));
        assertTrue(map.containsValue("2"));
    }

    @Test
    public void testContainsValueNotPresent() {
        map.put("a", "1");
        assertFalse(map.containsValue("2"));
    }

    // Test put

    @Test
    public void testPutFirstValue() {
        Object result = map.put("a", "1");
        assertNull(result);
        assertEquals(1, map.size("a"));
        assertTrue(map.containsValue("a", "1"));
    }

    @Test
    public void testPutSecondValue() {
        map.put("a", "1");
        Object result = map.put("a", "2");
        assertNull(result);
        assertEquals(2, map.size("a"));
    }

    @Test
    public void testPutWithFactoryReturningNonEmptyCollection() {
        // Factory that returns a collection already containing an element
        Factory factory = new Factory() {
            public Object create() {
                ArrayList list = new ArrayList();
                list.add("pre-existing");
                return list;
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        // put a value, collection already has size >0, so it should add to map and return null
        Object result = m.put("a", "new");
        assertNull(result);
        assertEquals(2, m.size("a")); // pre-existing + new
        assertTrue(m.containsValue("a", "pre-existing"));
        assertTrue(m.containsValue("a", "new"));
    }

    @Test
    public void testPutWithCollectionThatRejectsAdd() {
        // Factory returns a collection that refuses to add (add returns false) and size remains 0
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList() {
                    public boolean add(Object o) {
                        return false; // reject
                    }
                    public int size() {
                        return 0;
                    }
                };
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        Object result = m.put("a", "value");
        assertNull(result); // because result = false
        assertNull(m.getCollection("a")); // not added to map because size was 0
    }

    @Test
    public void testPutWithCollectionThatAddsButSizeZero() {
        // Factory returns a collection where add returns true but size remains 0 (impossible normally, but we can simulate)
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList() {
                    public boolean add(Object o) {
                        // add but don't increase size? We'll just override size to return 0 always
                        super.add(o);
                        return true;
                    }
                    public int size() {
                        return 0;
                    }
                };
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        Object result = m.put("a", "value");
        // result is true because add returned true, but size is 0 so it doesn't add to map, and returns value? Actually code: result = coll.add(value); if (coll.size() > 0) { getMap().put(key, coll); result = false; } return (result ? value : null); So if size is 0, result remains true, returns value.
        assertEquals("value", result);
        assertNull(m.getCollection("a")); // not added to map
    }

    // Test putAll(Map)

    @Test
    public void testPutAllWithNormalMap() {
        Map normal = new HashMap();
        normal.put("a", "1");
        normal.put("b", "2");
        map.putAll(normal);
        assertEquals(1, map.size("a"));
        assertEquals(1, map.size("b"));
        assertTrue(map.containsValue("a", "1"));
        assertTrue(map.containsValue("b", "2"));
    }

    @Test
    public void testPutAllWithMultiMap() {
        MultiValueMap other = new MultiValueMap();
        other.put("a", "1");
        other.put("a", "2");
        other.put("b", "3");
        map.putAll(other);
        assertEquals(2, map.size("a"));
        assertEquals(1, map.size("b"));
        assertTrue(map.containsValue("a", "1"));
        assertTrue(map.containsValue("a", "2"));
        assertTrue(map.containsValue("b", "3"));
    }

    @Test
    public void testPutAllWithEmptyMultiMap() {
        MultiValueMap other = new MultiValueMap();
        map.putAll(other);
        assertTrue(map.isEmpty());
    }

    // Test values()

    @Test
    public void testValuesView() {
        map.put("a", "1");
        map.put("a", "2");
        map.put("b", "3");
        Collection values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
        assertTrue(values.contains("3"));
    }

    @Test
    public void testValuesViewClear() {
        map.put("a", "1");
        map.put("b", "2");
        Collection values = map.values();
        values.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testValuesViewIterator() {
        map.put("a", "1");
        map.put("a", "2");
        Iterator it = map.values().iterator();
        assertTrue(it.hasNext());
        // order not guaranteed, but we can collect
        Collection extracted = new ArrayList();
        while (it.hasNext()) {
            extracted.add(it.next());
        }
        assertEquals(2, extracted.size());
        assertTrue(extracted.contains("1"));
        assertTrue(extracted.contains("2"));
    }

    // Test containsValue(Object key, Object value)

    @Test
    public void testContainsValueWithKeyAndValue() {
        map.put("a", "1");
        assertTrue(map.containsValue("a", "1"));
        assertFalse(map.containsValue("a", "2"));
        assertFalse(map.containsValue("b", "1"));
    }

    @Test
    public void testContainsValueWithKeyNotPresent() {
        assertFalse(map.containsValue("a", "anything"));
    }

    // Test getCollection

    @Test
    public void testGetCollection() {
        assertNull(map.getCollection("a"));
        map.put("a", "1");
        Collection coll = map.getCollection("a");
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains("1"));
    }

    // Test size(Object key)

    @Test
    public void testSizeByKey() {
        assertEquals(0, map.size("a"));
        map.put("a", "1");
        assertEquals(1, map.size("a"));
        map.put("a", "2");
        assertEquals(2, map.size("a"));
    }

    // Test putAll(Object key, Collection values)

    @Test
    public void testPutAllWithNullCollection() {
        assertFalse(map.putAll("a", null));
        assertNull(map.getCollection("a"));
    }

    @Test
    public void testPutAllWithEmptyCollection() {
        assertFalse(map.putAll("a", new ArrayList()));
        assertNull(map.getCollection("a"));
    }

    @Test
    public void testPutAllNewKey() {
        Collection values = new ArrayList();
        values.add("1");
        values.add("2");
        assertFalse(map.putAll("a", values)); // returns false because coll.size() > 0 after addAll
        assertEquals(2, map.size("a"));
        assertTrue(map.containsValue("a", "1"));
        assertTrue(map.containsValue("a", "2"));
    }

    @Test
    public void testPutAllExistingKey() {
        map.put("a", "1");
        Collection values = new ArrayList();
        values.add("2");
        values.add("3");
        assertTrue(map.putAll("a", values)); // returns true because coll.addAll returns true
        assertEquals(3, map.size("a"));
    }

    @Test
    public void testPutAllWithFactoryReturningNonEmptyCollection() {
        Factory factory = new Factory() {
            public Object create() {
                ArrayList list = new ArrayList();
                list.add("pre");
                return list;
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        Collection values = new ArrayList();
        values.add("new");
        assertFalse(m.putAll("a", values)); // coll.size() > 0 after addAll
        assertEquals(2, m.size("a"));
        assertTrue(m.containsValue("a", "pre"));
        assertTrue(m.containsValue("a", "new"));
    }

    @Test
    public void testPutAllWithCollectionThatRejectsAddAll() {
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList() {
                    public boolean addAll(Collection c) {
                        return false;
                    }
                    public int size() {
                        return 0;
                    }
                };
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        Collection values = new ArrayList();
        values.add("value");
        assertFalse(m.putAll("a", values)); // addAll returns false, size 0, so returns false
        assertNull(m.getCollection("a"));
    }

    @Test
    public void testPutAllWithCollectionThatAddsAllButSizeZero() {
        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList() {
                    public boolean addAll(Collection c) {
                        super.addAll(c);
                        return true;
                    }
                    public int size() {
                        return 0;
                    }
                };
            }
        };
        MultiValueMap m = createMapWithFactory(factory);
        Collection values = new ArrayList();
        values.add("value");
        assertTrue(m.putAll("a", values)); // addAll returns true, size 0, so returns true
        assertNull(m.getCollection("a")); // not added to map
    }

    // Test iterator(Object key)

    @Test
    public void testIteratorKeyNotPresent() {
        Iterator it = map.iterator("nonexistent");
        assertSame(EmptyIterator.INSTANCE, it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorKeyPresent() {
        map.put("a", "1");
        map.put("a", "2");
        Iterator it = map.iterator("a");
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertEquals("2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        map.put("a", "1");
        map.put("a", "2");
        Iterator it = map.iterator("a");
        it.next();
        it.remove();
        assertEquals(1, map.size("a"));
        assertFalse(map.containsValue("a", "1"));
        assertTrue(map.containsValue("a", "2"));
    }

    @Test
    public void testIteratorRemoveLastValueRemovesKey() {
        map.put("a", "1");
        Iterator it = map.iterator("a");
        it.next();
        it.remove();
        assertNull(map.getCollection("a"));
        assertFalse(map.containsKey("a"));
    }

    // Test totalSize

    @Test
    public void testTotalSize() {
        assertEquals(0, map.totalSize());
        map.put("a", "1");
        assertEquals(1, map.totalSize());
        map.put("a", "2");
        assertEquals(2, map.totalSize());
        map.put("b", "3");
        assertEquals(3, map.totalSize());
    }

    // Test createCollection indirectly via factory that throws exception

    @Test(expected = FunctorException.class)
    public void testReflectionFactoryWithNoDefaultConstructor() {
        // Use a class without a default constructor
        MultiValueMap.decorate(new HashMap(), NoDefaultConstructor.class);
    }

    // A class without a default constructor for testing
    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String arg) {}
    }

    // Test ReflectionFactory directly (though private, we can test via decorate)
    @Test
    public void testReflectionFactorySuccess() {
        MultiValueMap m = MultiValueMap.decorate(new HashMap(), ArrayList.class);
        m.put("a", "1");
        assertTrue(m.getCollection("a") instanceof ArrayList);
    }

    // Additional edge cases

    @Test
    public void testPutNullKey() {
        map.put(null, "value");
        assertEquals(1, map.size(null));
        assertTrue(map.containsValue(null, "value"));
    }

    @Test
    public void testPutNullValue() {
        map.put("a", null);
        assertTrue(map.containsValue("a", null));
    }

    @Test
    public void testRemoveMappingNullKey() {
        map.put(null, "value");
        assertEquals("value", map.removeMapping(null, "value"));
        assertNull(map.getCollection(null));
    }

    @Test
    public void testContainsValueWithNullValue() {
        map.put("a", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testValuesIteratorNoSuchElement() {
        map.put("a", "1");
        Iterator it = map.iterator("a");
        it.next();
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testValuesViewIteratorRemove() {
        map.put("a", "1");
        map.put("a", "2");
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertTrue(map.isEmpty());
    }
}
