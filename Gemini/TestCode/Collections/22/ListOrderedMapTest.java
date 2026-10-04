package org.apache.commons.collections4.map;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ListOrderedMapTest {

    @Test
    public void testFactoryAndConstructors() {
        ListOrderedMap<String, Integer> map = ListOrderedMap.listOrderedMap(new HashMap<String, Integer>());
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());

        ListOrderedMap<String, String> defaultMap = new ListOrderedMap<String, String>();
        Assert.assertEquals(0, defaultMap.size());

        Map<String, String> initMap = new LinkedHashMap<String, String>();
        initMap.put("a", "1");
        initMap.put("b", "2");
        ListOrderedMap<String, String> wrapped = new ListOrderedMap<String, String>(initMap);
        Assert.assertEquals(2, wrapped.size());
        Assert.assertEquals("a", wrapped.get(0));
        Assert.assertEquals("b", wrapped.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMapThrowsException() {
        new ListOrderedMap<Object, Object>(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFactoryWithNullMapThrowsException() {
        ListOrderedMap.listOrderedMap(null);
    }

    @Test
    public void testFirstAndLastKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();

        try {
            map.firstKey();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        try {
            map.lastKey();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        map.put("k1", "v1");
        Assert.assertEquals("k1", map.firstKey());
        Assert.assertEquals("k1", map.lastKey());

        map.put("k2", "v2");
        map.put("k3", "v3");
        Assert.assertEquals("k1", map.firstKey());
        Assert.assertEquals("k3", map.lastKey());
    }

    @Test
    public void testNextAndPreviousKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        Assert.assertEquals("b", map.nextKey("a"));
        Assert.assertEquals("c", map.nextKey("b"));
        Assert.assertNull(map.nextKey("c"));
        Assert.assertNull(map.nextKey("nonexistent"));

        Assert.assertNull(map.previousKey("a"));
        Assert.assertEquals("a", map.previousKey("b"));
        Assert.assertEquals("b", map.previousKey("c"));
        Assert.assertNull(map.previousKey("nonexistent"));
    }

    @Test
    public void testPutAndRePut() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        Assert.assertNull(map.put("a", "1"));
        Assert.assertNull(map.put("b", "2"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Arrays.asList("a", "b"), map.keyList());

        // Re-adding existing key replaces value but does not change order
        String old = map.put("a", "10");
        Assert.assertEquals("1", old);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("10", map.get("a"));
        Assert.assertEquals(Arrays.asList("a", "b"), map.keyList());
    }

    @Test
    public void testPutAllMap() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        Map<String, String> toAdd = new LinkedHashMap<String, String>();
        toAdd.put("a", "1");
        toAdd.put("b", "2");
        map.putAll(toAdd);

        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("a"));
        Assert.assertEquals("2", map.get("b"));
        Assert.assertEquals(Arrays.asList("a", "b"), map.keyList());
    }

    @Test
    public void testPutAtIndexNewKeys() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("c", "3");

        map.put(1, "b", "2");
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Arrays.asList("a", "b", "c"), map.keyList());
        Assert.assertEquals("2", map.get("b"));
    }

    @Test
    public void testPutAtIndexExistingKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");

        // Move 'b' (pos=1) to pos=3 (pos < index branch: index adjusted to 2, inserted at 2)
        String old = map.put(3, "b", "20");
        Assert.assertEquals("2", old);
        Assert.assertEquals(Arrays.asList("a", "c", "b", "d"), map.keyList());
        Assert.assertEquals("20", map.get("b"));

        // Move 'd' (pos=3) to pos=1 (pos >= index branch: index remains 1)
        old = map.put(1, "d", "40");
        Assert.assertEquals("4", old);
        Assert.assertEquals(Arrays.asList("a", "d", "c", "b"), map.keyList());
        Assert.assertEquals("40", map.get("d"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndexOutOfBounds() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(1, "a", "1");
    }

    @Test
    public void testPutAllAtIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("d", "4");

        Map<String, String> toAdd = new LinkedHashMap<String, String>();
        toAdd.put("b", "2");
        toAdd.put("c", "3");

        map.putAll(1, toAdd);
        Assert.assertEquals(Arrays.asList("a", "b", "c", "d"), map.keyList());

        // Test with existing keys replacement
        Map<String, String> replaceMap = new LinkedHashMap<String, String>();
        replaceMap.put("c", "30");
        replaceMap.put("e", "50");

        map.putAll(0, replaceMap);
        // "c" was replaced at 0 -> new order: c, a, b, d. Then 'e' at index of c + 1 = 1 -> c, e, a, b, d
        Assert.assertEquals(Arrays.asList("c", "e", "a", "b", "d"), map.keyList());
    }

    @Test
    public void testRemoveByKeyAndIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        Assert.assertNull(map.remove("nonexistent"));
        Assert.assertEquals("2", map.remove("b"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Arrays.asList("a", "c"), map.keyList());

        // remove by index
        Assert.assertEquals("3", map.remove(1));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("a", map.get(0));

        // clear
        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());
        Assert.assertTrue(map.keyList().isEmpty());
    }

    @Test
    public void testGetAndSetValueByIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");

        Assert.assertEquals("a", map.get(0));
        Assert.assertEquals("b", map.get(1));
        Assert.assertEquals("1", map.getValue(0));
        Assert.assertEquals("2", map.getValue(1));
        Assert.assertEquals(0, map.indexOf("a"));
        Assert.assertEquals(1, map.indexOf("b"));
        Assert.assertEquals(-1, map.indexOf("z"));

        String old = map.setValue(1, "20");
        Assert.assertEquals("2", old);
        Assert.assertEquals("20", map.getValue(1));
        Assert.assertEquals("20", map.get("b"));
    }

    @Test
    public void testToString() {
        ListOrderedMap<Object, Object> map = new ListOrderedMap<Object, Object>();
        Assert.assertEquals("{}", map.toString());

        map.put("k1", "v1");
        Assert.assertEquals("{k1=v1}", map.toString());

        map.put("k2", "v2");
        Assert.assertEquals("{k1=v1, k2=v2}", map.toString());

        // Self-referencing tests
        map.clear();
        map.put(map, "val");
        Assert.assertEquals("{(this Map)=val}", map.toString());

        map.clear();
        map.put("key", map);
        Assert.assertEquals("{key=(this Map)}", map.toString());
    }

    @Test
    public void testKeyListAndAsList() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");

        List<String> keys = map.keyList();
        Assert.assertEquals(Arrays.asList("a", "b"), keys);
        Assert.assertEquals(keys, map.asList());

        try {
            keys.add("c");
            Assert.fail("keyList should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testKeySetView() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");

        Set<String> keySet = map.keySet();
        Assert.assertEquals(2, keySet.size());
        Assert.assertTrue(keySet.contains("a"));
        Assert.assertFalse(keySet.contains("c"));

        Iterator<String> it = keySet.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("b", it.next());
        Assert.assertFalse(it.hasNext());

        keySet.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testValuesViewAndValueList() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        Collection<String> values = map.values();
        List<String> valueList = map.valueList();
        Assert.assertEquals(3, values.size());
        Assert.assertEquals(3, valueList.size());
        Assert.assertTrue(values.contains("2"));
        Assert.assertFalse(values.contains("99"));

        Assert.assertEquals("2", valueList.get(1));
        Assert.assertEquals("2", valueList.set(1, "20"));
        Assert.assertEquals("20", map.get("b"));

        Assert.assertEquals("20", valueList.remove(1));
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(map.containsKey("b"));

        Iterator<String> it = values.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("1", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("3", it.next());
        Assert.assertFalse(it.hasNext());

        values.clear();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testEntrySetView() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        Assert.assertEquals(2, entrySet.size());
        Assert.assertFalse(entrySet.isEmpty());

        Map.Entry<String, String> entryA = new DefaultMapEntry<String, String>("a", "1");
        Map.Entry<String, String> entryInvalid = new DefaultMapEntry<String, String>("a", "99");
        Assert.assertTrue(entrySet.contains(entryA));
        Assert.assertFalse(entrySet.contains(entryInvalid));
        Assert.assertFalse(entrySet.contains("not an entry"));

        List<Map.Entry<String, String>> coll = new ArrayList<Map.Entry<String, String>>();
        coll.add(entryA);
        Assert.assertTrue(entrySet.containsAll(coll));

        coll.add(entryInvalid);
        Assert.assertFalse(entrySet.containsAll(coll));

        Assert.assertFalse(entrySet.remove("not an entry"));
        Assert.assertFalse(entrySet.remove(entryInvalid));
        Assert.assertTrue(entrySet.remove(entryA));
        Assert.assertEquals(1, map.size());
        Assert.assertFalse(map.containsKey("a"));

        // EntrySet equals and hashCode
        Map<String, String> copy = new HashMap<String, String>();
        copy.put("b", "2");
        Assert.assertTrue(entrySet.equals(entrySet));
        Assert.assertEquals(copy.entrySet(), entrySet);
        Assert.assertEquals(copy.entrySet().hashCode(), entrySet.hashCode());
        Assert.assertEquals(copy.entrySet().toString(), entrySet.toString());

        entrySet.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testEntrySetIteratorAndEntryModifications() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");

        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("a", entry.getKey());
        Assert.assertEquals("1", entry.getValue());

        entry.setValue("10");
        Assert.assertEquals("10", map.get("a"));
        Assert.assertEquals("10", entry.getValue());

        it.remove();
        Assert.assertEquals(1, map.size());
        Assert.assertFalse(map.containsKey("a"));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("b", it.next().getKey());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testMapIterator() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        OrderedMapIterator<String, String> it = map.mapIterator();
        Assert.assertEquals("Iterator[]", it.toString());

        // Test illegal state before next()
        try {
            it.getKey();
            Assert.fail();
        } catch (IllegalStateException e) {}

        try {
            it.getValue();
            Assert.fail();
        } catch (IllegalStateException e) {}

        try {
            it.setValue("x");
            Assert.fail();
        } catch (IllegalStateException e) {}

        try {
            it.remove();
            Assert.fail();
        } catch (IllegalStateException e) {}

        // Forward iteration
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
        Assert.assertEquals("Iterator[a=1]", it.toString());
        Assert.assertEquals("a", it.getKey());
        Assert.assertEquals("1", it.getValue());

        it.setValue("100");
        Assert.assertEquals("100", map.get("a"));
        Assert.assertEquals("100", it.getValue());

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("b", it.next());

        // Backward iteration
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("b", it.previous());
        Assert.assertEquals("b", it.getKey());
        Assert.assertEquals("2", it.getValue());

        // Remove via iterator
        it.remove();
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(map.containsKey("b"));

        try {
            it.remove();
            Assert.fail("Should throw after already removing");
        } catch (IllegalStateException e) {}

        // Reset
        it.reset();
        Assert.assertEquals("Iterator[]", it.toString());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
        Assert.assertEquals("100", it.getValue());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("c", it.next());
        Assert.assertEquals("3", it.getValue());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        ListOrderedMap<String, Integer> map = new ListOrderedMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ListOrderedMap<String, Integer> deserialized = (ListOrderedMap<String, Integer>) ois.readObject();
        ois.close();

        Assert.assertEquals(map.size(), deserialized.size());
        Assert.assertEquals(map.keyList(), deserialized.keyList());
        Assert.assertEquals(Integer.valueOf(1), deserialized.get("one"));
        Assert.assertEquals(Integer.valueOf(2), deserialized.get("two"));
        Assert.assertEquals(Integer.valueOf(3), deserialized.get("three"));
    }
}
