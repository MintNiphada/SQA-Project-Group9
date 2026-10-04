package org.apache.commons.collections4.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ListOrderedMapTest {

    private ListOrderedMap<String, Integer> map;

    @Before
    public void setUp() {
        map = new ListOrderedMap<String, Integer>();
    }

    // ---------------------------------------------------------------
    // Constructors and Factory
    // ---------------------------------------------------------------
    @Test
    public void testDefaultConstructor() {
        assertNotNull(map);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMap() {
        new ListOrderedMap<String, Integer>(null);
    }

    @Test
    public void testFactoryMethod() {
        Map<String, Integer> src = new HashMap<String, Integer>();
        src.put("a", 1);
        src.put("b", 2);
        ListOrderedMap<String, Integer> ordered = ListOrderedMap.listOrderedMap(src);
        assertEquals(2, ordered.size());
        assertTrue(ordered.containsKey("a"));
        assertTrue(ordered.containsKey("b"));
        assertEquals("a", ordered.firstKey());
        assertEquals("b", ordered.lastKey());
    }

    // ---------------------------------------------------------------
    // Put and Order
    // ---------------------------------------------------------------
    @Test
    public void testPutMaintainsOrder() {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        assertEquals(3, map.size());
        assertEquals("one", map.firstKey());
        assertEquals("three", map.lastKey());
        assertEquals(Arrays.asList("one", "two", "three"), new ArrayList<String>(map.keyList()));
    }

    @Test
    public void testPutExistingDoesNotChangeOrder() {
        map.put("one", 1);
        map.put("two", 2);
        map.put("one", 100);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(100), map.get("one"));
        assertEquals("one", map.firstKey());
        assertEquals("two", map.lastKey());
    }

    // ---------------------------------------------------------------
    // PutAll
    // ---------------------------------------------------------------
    @Test
    public void testPutAllMap() {
        Map<String, Integer> src = new LinkedHashMap<String, Integer>();
        src.put("c", 3);
        src.put("a", 1);
        src.put("b", 2);
        map.putAll(src);
        assertEquals(3, map.size());
        assertEquals(Arrays.asList("c", "a", "b"), new ArrayList<String>(map.keyList()));
    }

    @Test
    public void testPutAllAtIndex() {
        map.put("a", 1);
        map.put("b", 2);
        Map<String, Integer> src = new LinkedHashMap<String, Integer>();
        src.put("c", 3);
        src.put("d", 4);
        map.putAll(1, src);
        // Order should be a, c, d, b
        assertEquals(Arrays.asList("a", "c", "d", "b"), new ArrayList<String>(map.keyList()));
    }

    @Test
    public void testPutAllAtIndexWithExistingKeys() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Map<String, Integer> src = new LinkedHashMap<String, Integer>();
        src.put("b", 20);
        src.put("d", 4);
        map.putAll(1, src);
        // b is removed then re-inserted at index 1, then d is inserted after b's new position
        assertEquals(Arrays.asList("a", "b", "d", "c"), new ArrayList<String>(map.keyList()));
        assertEquals(Integer.valueOf(20), map.get("b"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAllAtIndexNegativeIndexThrows() {
        map.putAll(-1, new HashMap<String, Integer>());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAllAtIndexExceedsSizeThrows() {
        map.putAll(1, new HashMap<String, Integer>());
    }

    // ---------------------------------------------------------------
    // First/Last key
    // ---------------------------------------------------------------
    @Test(expected = NoSuchElementException.class)
    public void testFirstKeyEmptyMap() {
        map.firstKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testLastKeyEmptyMap() {
        map.lastKey();
    }

    @Test
    public void testFirstAndLastKey() {
        map.put("first", 1);
        map.put("middle", 2);
        map.put("last", 3);
        assertEquals("first", map.firstKey());
        assertEquals("last", map.lastKey());
    }

    // ---------------------------------------------------------------
    // Next/Previous key
    // ---------------------------------------------------------------
    @Test
    public void testNextKey() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals("b", map.nextKey("a"));
        assertEquals("c", map.nextKey("b"));
        assertNull(map.nextKey("c"));
        assertNull(map.nextKey("unknown"));
        assertNull(map.nextKey(null));
    }

    @Test
    public void testPreviousKey() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertNull(map.previousKey("a"));
        assertEquals("a", map.previousKey("b"));
        assertEquals("b", map.previousKey("c"));
        assertNull(map.previousKey("unknown"));
        assertNull(map.previousKey(null));
    }

    // ---------------------------------------------------------------
    // Remove
    // ---------------------------------------------------------------
    @Test
    public void testRemoveByKey() {
        map.put("x", 10);
        map.put("y", 20);
        map.put("z", 30);
        assertEquals(Integer.valueOf(20), map.remove("y"));
        assertNull(map.remove("unknown"));
        assertEquals(2, map.size());
        assertEquals(Arrays.asList("x", "z"), new ArrayList<String>(map.keyList()));
    }

    @Test
    public void testRemoveByIndex() {
        map.put("x", 10);
        map.put("y", 20);
        map.put("z", 30);
        assertEquals(Integer.valueOf(20), map.remove(1));
        assertNull(map.remove(5)); // out of bounds? Actually throws IndexOutOfBoundsException? The remove(int) method calls remove(get(index)) which throws IndexOutOfBoundsException.
        // But remove(int index) from map will throw IndexOutOfBoundsException if index invalid.
        // So separate test.
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndexOutOfBounds() {
        map.put("a", 1);
        map.remove(1);
    }

    @Test
    public void testClear() {
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertTrue(map.keyList().isEmpty());
    }

    // ---------------------------------------------------------------
    // KeySet View
    // ---------------------------------------------------------------
    @Test
    public void testKeySet() {
        map.put("one", 1);
        map.put("two", 2);
        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("one"));
        assertFalse(keys.contains("three"));
        keys.clear();
        assertTrue(map.isEmpty());
        // Re-add and iterate order
        map.put("c", 3);
        map.put("a", 1);
        map.put("b", 2);
        Iterator<String> it = map.keySet().iterator();
        assertEquals("c", it.next());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
    }

    // ---------------------------------------------------------------
    // KeyList View (unmodifiable)
    // ---------------------------------------------------------------
    @Test
    public void testKeyList() {
        map.put("a", 1);
        map.put("b", 2);
        List<String> list = map.keyList();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertTrue(list.contains("a"));
        // unmodifiable check
        try {
            list.add("c");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        try {
            list.set(0, "x");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // Values View (modifiable list without add)
    // ---------------------------------------------------------------
    @Test
    public void testValues() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Collection<Integer> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(2));
        assertFalse(values.contains(999));
        // iteration order
        Iterator<Integer> it = values.iterator();
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        // remove via iterator
        it = values.iterator();
        it.next();
        it.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey("a"));
        // adding not supported
        try {
            values.add(99);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testValueList() {
        map.put("a", 10);
        map.put("b", 20);
        List<Integer> list = map.valueList();
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        // set
        assertEquals(Integer.valueOf(10), list.set(0, 100));
        assertEquals(Integer.valueOf(100), map.get("a"));
        // remove
        assertEquals(Integer.valueOf(100), list.remove(0));
        assertNull(map.get("a"));
        // size
        assertEquals(1, list.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testValueListGetOutOfBounds() {
        map.valueList().get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testValueListSetOutOfBounds() {
        map.valueList().set(0, 0);
    }

    // ---------------------------------------------------------------
    // EntrySet View
    // ---------------------------------------------------------------
    @Test
    public void testEntrySetContains() {
        map.put("k1", 1);
        map.put("k2", 2);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<String, Integer>("k1", 1)));
    }

    @Test
    public void testEntrySetRemove() {
        map.put("k1", 1);
        map.put("k2", 2);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertTrue(entries.remove(new AbstractMap.SimpleEntry<String, Integer>("k1", 1)));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));
        // remove non-entry type
        assertFalse(entries.remove("not an entry"));
    }

    @Test
    public void testEntrySetClear() {
        map.put("k1", 1);
        map.entrySet().clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testEntrySetEqualsAndHashCode() {
        ListOrderedMap<String, Integer> other = new ListOrderedMap<String, Integer>();
        other.put("k1", 1);
        other.put("k2", 2);
        map.put("k1", 1);
        map.put("k2", 2);
        assertEquals(map.entrySet(), other.entrySet());
        assertEquals(map.entrySet().hashCode(), other.entrySet().hashCode());
    }

    @Test
    public void testEntrySetIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
    }

    // ---------------------------------------------------------------
    // asList
    // ---------------------------------------------------------------
    @Test
    public void testAsList() {
        map.put("c", 3);
        List<String> list = map.asList();
        assertEquals(map.keyList(), list);
        // unmodifiable check
        try {
            list.add("x");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // Index-based access
    // ---------------------------------------------------------------
    @Test
    public void testGetByIndex() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals("a", map.get(0));
        assertEquals("c", map.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        map.get(0);
    }

    @Test
    public void testGetValueByIndex() {
        map.put("a", 10);
        map.put("b", 20);
        assertEquals(Integer.valueOf(10), map.getValue(0));
        assertEquals(Integer.valueOf(20), map.getValue(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexOutOfBounds() {
        map.getValue(0);
    }

    @Test
    public void testIndexOf() {
        map.put("x", 1);
        map.put("y", 2);
        assertEquals(0, map.indexOf("x"));
        assertEquals(1, map.indexOf("y"));
        assertEquals(-1, map.indexOf("z"));
        assertEquals(-1, map.indexOf(null));
    }

    @Test
    public void testSetValueByIndex() {
        map.put("p", 100);
        map.put("q", 200);
        assertEquals(Integer.valueOf(100), map.setValue(0, 999));
        assertEquals(Integer.valueOf(999), map.get("p"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueByIndexOutOfBounds() {
        map.setValue(0, 0);
    }

    @Test
    public void testPutAtIndexNewKey() {
        map.put("a", 1);
        map.put("c", 3);
        map.put(1, "b", 2);
        assertEquals(Arrays.asList("a", "b", "c"), new ArrayList<String>(map.keyList()));
        assertEquals(Integer.valueOf(2), map.get("b"));
    }

    @Test
    public void testPutAtIndexExistingKeyBeforeIndex() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        // re-insert "a" at index 2 (should move to after removal, then insert at adjusted index)
        assertEquals(Integer.valueOf(1), map.put(2, "a", 100));
        assertEquals(Arrays.asList("b", "c", "a"), new ArrayList<String>(map.keyList()));
        assertEquals(Integer.valueOf(100), map.get("a"));
    }

    @Test
    public void testPutAtIndexExistingKeyAfterIndex() {
        map.put("a", 1);
        map.put("b",2);
        map.put("c",3);
        assertEquals(Integer.valueOf(3), map.put(0, "c", 30));
        assertEquals(Arrays.asList("c", "a", "b"), new ArrayList<String>(map.keyList()));
        assertEquals(Integer.valueOf(30), map.get("c"));
    }

    @Test
    public void testPutAtIndexExistingKeySameIndex() {
        map.put("a",1);
        map.put("b",2);
        map.put("c",3);
        assertEquals(Integer.valueOf(2), map.put(1, "b", 20));
        assertEquals(Arrays.asList("a", "b", "c"), new ArrayList<String>(map.keyList()));
        assertEquals(Integer.valueOf(20), map.get("b"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndexNegative() {
        map.put(-1, "x", 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndexExceedsSize() {
        map.put(1, "x", 1);
    }

    // ---------------------------------------------------------------
    // MapIterator
    // ---------------------------------------------------------------
    @Test
    public void testMapIteratorForward() {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertEquals("two", it.next());
        assertEquals("three", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testMapIteratorBackward() {
        map.put("1", 100);
        map.put("2", 200);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        // move to end
        while (it.hasNext()) it.next();
        assertTrue(it.hasPrevious());
        assertEquals("2", it.previous());
        assertEquals("1", it.previous());
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testMapIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsKey("a"));
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorRemoveBeforeNext() {
        map.put("a", 1);
        map.mapIterator().remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetKeyBeforeNext() {
        map.put("a", 1);
        map.mapIterator().getKey();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetValueBeforeNext() {
        map.put("a", 1);
        map.mapIterator().getValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorSetValueBeforeNext() {
        map.put("a", 1);
        map.mapIterator().setValue(5);
    }

    @Test
    public void testMapIteratorSetValue() {
        map.put("a", 1);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        it.next();
        assertEquals(Integer.valueOf(1), it.setValue(10));
        assertEquals(Integer.valueOf(10), map.get("a"));
    }

    @Test
    public void testMapIteratorReset() {
        map.put("x", 100);
        map.put("y", 200);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        assertEquals("x", it.next());
        it.reset();
        assertEquals("x", it.next());
        assertEquals("y", it.next());
    }

    @Test
    public void testMapIteratorToString() {
        map.put("k", 1);
        OrderedMapIterator<String, Integer> it = map.mapIterator();
        assertEquals("Iterator[]", it.toString());
        it.next();
        assertEquals("Iterator[k=1]", it.toString());
    }

    // ---------------------------------------------------------------
    // ToString
    // ---------------------------------------------------------------
    @Test
    public void testToStringEmpty() {
        assertEquals("{}", map.toString());
    }

    @Test
    public void testToStringNormal() {
        map.put("key1", 1);
        map.put("key2", 2);
        assertEquals("{key1=1, key2=2}", map.toString());
    }

    @Test
    public void testToStringSelfReference() {
        Map<String, Object> selfMap = new ListOrderedMap<String, Object>();
        selfMap.put("self", selfMap);
        assertEquals("{self=(this Map)}", selfMap.toString());
        selfMap.put("map", selfMap); // value self-reference
        // key self-reference is tricky because Map usually cannot have itself as key? But toString checks key==this.
        // We can't add map as key because put expects K, but if K is Map? Possibly.
        Map<Object, Object> weird = new ListOrderedMap<Object, Object>();
        weird.put(weird, "val");
        assertEquals("{(this Map)=val}", weird.toString());
    }

    // ---------------------------------------------------------------
    // Serialization
    // ---------------------------------------------------------------
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        map.put("dog", 4);
        map.put("cat", 5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ListOrderedMap<?, ?> deserialized = (ListOrderedMap<?, ?>) ois.readObject();
        ois.close();

        assertEquals(map.size(), deserialized.size());
        Iterator<?> origKeys = map.keyList().iterator();
        Iterator<?> deserializedKeys = deserialized.keyList().iterator();
        while (origKeys.hasNext()) {
            assertEquals(origKeys.next(), deserializedKeys.next());
        }
        assertFalse(deserializedKeys.hasNext());
        assertEquals(map.get("dog"), deserialized.get("dog"));
        assertEquals(map.get("cat"), deserialized.get("cat"));
    }

    // ---------------------------------------------------------------
    // Edge cases and other methods
    // ---------------------------------------------------------------
    @Test
    public void testPutNullKey() {
        map.put(null, 99);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals(Integer.valueOf(99), map.get(null));
        assertEquals(null, map.firstKey());
    }

    @Test
    public void testContainsValue() {
        map.put("a", 1);
        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(2));
    }

    @Test
    public void testEntrySetContainsAfterModify() {
        map.put("k1", 10);
        map.put("k2", 20);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<String, Integer>("k1", 10);
        assertTrue(entries.contains(entry));
        map.put("k1", 100);
        assertFalse(entries.contains(entry));
    }
}

```
