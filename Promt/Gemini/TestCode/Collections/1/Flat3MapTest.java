package org.apache.commons.collections.map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Flat3MapTest {

    @Test
    public void testConstructors() {
        Flat3Map map = new Flat3Map();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());

        Map initMap = new HashMap();
        initMap.put("A", "1");
        initMap.put("B", "2");
        Flat3Map map2 = new Flat3Map(initMap);
        Assert.assertEquals(2, map2.size());
        Assert.assertEquals("1", map2.get("A"));
        Assert.assertEquals("2", map2.get("B"));

        Map bigMap = new HashMap();
        bigMap.put("A", "1");
        bigMap.put("B", "2");
        bigMap.put("C", "3");
        bigMap.put("D", "4");
        Flat3Map map3 = new Flat3Map(bigMap);
        Assert.assertEquals(4, map3.size());
        Assert.assertEquals("1", map3.get("A"));
        Assert.assertEquals("4", map3.get("D"));

        Flat3Map emptyMap = new Flat3Map(new HashMap());
        Assert.assertEquals(0, emptyMap.size());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new Flat3Map(null);
    }

    @Test
    public void testPutAndGetFlatMode() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.get("A"));
        Assert.assertNull(map.get(null));

        // Size 0 -> 1
        Assert.assertNull(map.put("A", "1"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertNull(map.get("B"));
        Assert.assertNull(map.get(null));

        // Update size 1
        Assert.assertEquals("1", map.put("A", "11"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("11", map.get("A"));

        // Size 1 -> 2
        Assert.assertNull(map.put("B", "2"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("11", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        Assert.assertNull(map.get("C"));

        // Update size 2
        Assert.assertEquals("2", map.put("B", "22"));
        Assert.assertEquals("11", map.put("A", "111"));
        Assert.assertEquals("111", map.get("A"));
        Assert.assertEquals("22", map.get("B"));

        // Size 2 -> 3
        Assert.assertNull(map.put("C", "3"));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("111", map.get("A"));
        Assert.assertEquals("22", map.get("B"));
        Assert.assertEquals("3", map.get("C"));
        Assert.assertNull(map.get("D"));

        // Update size 3
        Assert.assertEquals("3", map.put("C", "33"));
        Assert.assertEquals("22", map.put("B", "222"));
        Assert.assertEquals("111", map.put("A", "1111"));
        Assert.assertEquals("1111", map.get("A"));
        Assert.assertEquals("222", map.get("B"));
        Assert.assertEquals("33", map.get("C"));
    }

    @Test
    public void testPutAndGetWithNullKeys() {
        Flat3Map map = new Flat3Map();
        // Size 1 null key
        Assert.assertNull(map.put(null, "nullVal1"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("nullVal1", map.get(null));
        Assert.assertEquals("nullVal1", map.put(null, "nullVal1Updated"));
        Assert.assertEquals("nullVal1Updated", map.get(null));

        map.clear();
        // Null key at position 2
        map.put("A", "1");
        Assert.assertNull(map.put(null, "nullVal2"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("nullVal2", map.get(null));
        Assert.assertEquals("nullVal2", map.put(null, "nullVal2Updated"));
        Assert.assertEquals("nullVal2Updated", map.get(null));

        map.clear();
        // Null key at position 3
        map.put("A", "1");
        map.put("B", "2");
        Assert.assertNull(map.put(null, "nullVal3"));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        Assert.assertEquals("nullVal3", map.get(null));
        Assert.assertEquals("nullVal3", map.put(null, "nullVal3Updated"));
        Assert.assertEquals("nullVal3Updated", map.get(null));
    }

    @Test
    public void testContainsKeyAndContainsValue() {
        Flat3Map map = new Flat3Map();
        Assert.assertFalse(map.containsKey("A"));
        Assert.assertFalse(map.containsKey(null));
        Assert.assertFalse(map.containsValue("1"));
        Assert.assertFalse(map.containsValue(null));

        // Size 1
        map.put("A", "1");
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertFalse(map.containsKey("B"));
        Assert.assertFalse(map.containsKey(null));
        Assert.assertTrue(map.containsValue("1"));
        Assert.assertFalse(map.containsValue("2"));
        Assert.assertFalse(map.containsValue(null));

        // Size 2
        map.put("B", null);
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertFalse(map.containsKey("C"));
        Assert.assertTrue(map.containsValue("1"));
        Assert.assertTrue(map.containsValue(null));
        Assert.assertFalse(map.containsValue("2"));

        // Size 3
        map.put("C", "3");
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertFalse(map.containsKey("D"));
        Assert.assertTrue(map.containsValue("1"));
        Assert.assertTrue(map.containsValue(null));
        Assert.assertTrue(map.containsValue("3"));
        Assert.assertFalse(map.containsValue("4"));

        // Null key containment
        Flat3Map nullKeyMap = new Flat3Map();
        nullKeyMap.put(null, "val");
        Assert.assertTrue(nullKeyMap.containsKey(null));
        nullKeyMap.put("A", "valA");
        Assert.assertTrue(nullKeyMap.containsKey(null));
        nullKeyMap.put("B", "valB");
        Assert.assertTrue(nullKeyMap.containsKey(null));

        Flat3Map nullVal3Map = new Flat3Map();
        nullVal3Map.put("A", "1");
        nullVal3Map.put("B", "2");
        nullVal3Map.put("C", null);
        Assert.assertTrue(nullVal3Map.containsValue(null));
    }

    @Test
    public void testDelegationMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        // Trigger delegation
        Assert.assertNull(map.put("D", "4"));
        Assert.assertEquals(4, map.size());
        Assert.assertFalse(map.isEmpty());

        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        Assert.assertEquals("3", map.get("C"));
        Assert.assertEquals("4", map.get("D"));
        Assert.assertNull(map.get("E"));

        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("D"));
        Assert.assertFalse(map.containsKey("E"));

        Assert.assertTrue(map.containsValue("1"));
        Assert.assertTrue(map.containsValue("4"));
        Assert.assertFalse(map.containsValue("5"));

        Assert.assertEquals("4", map.put("D", "44"));
        Assert.assertEquals("44", map.get("D"));

        Assert.assertEquals("44", map.remove("D"));
        Assert.assertEquals(3, map.size());
        Assert.assertNull(map.get("D"));

        Map putMap = new HashMap();
        putMap.put("E", "5");
        map.putAll(putMap);
        Assert.assertEquals("5", map.get("E"));

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());
        Assert.assertNull(map.get("A"));
    }

    @Test
    public void testPutAllVariations() {
        Flat3Map map = new Flat3Map();
        map.putAll(new HashMap());
        Assert.assertEquals(0, map.size());

        Map smallMap = new HashMap();
        smallMap.put("A", "1");
        smallMap.put("B", "2");
        map.putAll(smallMap);
        Assert.assertEquals(2, map.size());

        map.clear();
        Map largeMap = new HashMap();
        largeMap.put("A", "1");
        largeMap.put("B", "2");
        largeMap.put("C", "3");
        largeMap.put("D", "4");
        map.putAll(largeMap);
        Assert.assertEquals(4, map.size());
    }

    @Test
    public void testRemoveFlatModeNonNullKeys() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.remove("A"));

        // Size 1 remove
        map.put("A", "1");
        Assert.assertNull(map.remove("Z"));
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(0, map.size());

        // Size 2 remove key 2
        map.put("A", "1");
        map.put("B", "2");
        Assert.assertNull(map.remove("Z"));
        Assert.assertEquals("2", map.remove("B"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));

        // Size 2 remove key 1
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("2", map.get("B"));

        // Size 3 remove key 3
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertNull(map.remove("Z"));
        Assert.assertEquals("3", map.remove("C"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));

        // Size 3 remove key 2
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("2", map.remove("B"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("3", map.get("C"));

        // Size 3 remove key 1
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("3", map.get("C"));
        Assert.assertEquals("2", map.get("B"));
    }

    @Test
    public void testRemoveFlatModeNullKeys() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.remove(null));

        // Size 1 remove null
        map.put(null, "nullVal");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(0, map.size());

        map.put("A", "1");
        Assert.assertNull(map.remove(null));

        // Size 2 remove null (at position 2)
        map.clear();
        map.put("A", "1");
        map.put(null, "nullVal");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));

        // Size 2 remove null (at position 1)
        map.clear();
        map.put(null, "nullVal");
        map.put("B", "2");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("2", map.get("B"));

        // Size 2 null not found
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        Assert.assertNull(map.remove(null));

        // Size 3 remove null (at position 3)
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        map.put(null, "nullVal");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));

        // Size 3 remove null (at position 2)
        map.clear();
        map.put("A", "1");
        map.put(null, "nullVal");
        map.put("C", "3");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("3", map.get("C"));

        // Size 3 remove null (at position 1)
        map.clear();
        map.put(null, "nullVal");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("3", map.get("C"));
        Assert.assertEquals("2", map.get("B"));

        // Size 3 null not found
        map.clear();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertNull(map.remove(null));
    }

    @Test
    public void testMapIteratorFlatMode() {
        Flat3Map empty = new Flat3Map();
        MapIterator emptyIt = empty.mapIterator();
        Assert.assertFalse(emptyIt.hasNext());

        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        MapIterator it = map.mapIterator();
        Assert.assertEquals("Iterator[]", it.toString());

        try {
            it.getKey();
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        try {
            it.getValue();
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        try {
            it.setValue("fail");
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        try {
            it.remove();
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("A", it.getKey());
        Assert.assertEquals("1", it.getValue());
        Assert.assertEquals("Iterator[A=1]", it.toString());
        Assert.assertEquals("1", it.setValue("10"));
        Assert.assertEquals("10", it.getValue());

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("B", it.getKey());
        Assert.assertEquals("2", it.getValue());
        Assert.assertEquals("2", it.setValue("20"));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertEquals("C", it.getKey());
        Assert.assertEquals("3", it.getValue());
        Assert.assertEquals("3", it.setValue("30"));

        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (NoSuchElementException ignored) {}

        // Reset iterator
        ((ResettableIterator) it).reset();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(map.containsKey("A"));

        try {
            it.remove();
            Assert.fail();
        } catch (IllegalStateException ignored) {}
    }

    @Test
    public void testMapIteratorDelegationMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");

        MapIterator it = map.mapIterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(4, count);
    }

    @Test
    public void testEntrySetFlatAndDelegation() {
        Flat3Map map = new Flat3Map();
        Set entrySet = map.entrySet();
        Assert.assertEquals(0, entrySet.size());
        Assert.assertFalse(entrySet.iterator().hasNext());

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals(3, entrySet.size());

        Iterator it = entrySet.iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry entry = (Map.Entry) it.next();
        Assert.assertNotNull(entry.toString());
        Assert.assertEquals("A", entry.getKey());
        Assert.assertEquals("1", entry.getValue());
        Assert.assertEquals("1", entry.setValue("100"));
        Assert.assertEquals("100", entry.getValue());
        Assert.assertEquals(entry, entry);
        Assert.assertFalse(entry.equals("NotAnEntry"));
        Assert.assertFalse(entry.equals(null));
        Assert.assertTrue(entry.hashCode() != 0);

        it.remove();
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(map.containsKey("A"));
        Assert.assertFalse(entry.equals(new Flat3Map().entrySet()));

        Assert.assertTrue(entrySet.remove(new Map.Entry() {
            public Object getKey() { return "B"; }
            public Object getValue() { return "2"; }
            public Object setValue(Object value) { return null; }
        }));
        Assert.assertFalse(entrySet.remove("InvalidObj"));
        Assert.assertFalse(entrySet.remove(new Map.Entry() {
            public Object getKey() { return "Z"; }
            public Object getValue() { return "Z"; }
            public Object setValue(Object value) { return null; }
        }));

        entrySet.clear();
        Assert.assertEquals(0, map.size());

        // Delegate EntrySet
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        Set delEntrySet = map.entrySet();
        Assert.assertEquals(4, delEntrySet.size());
        int count = 0;
        for (Iterator iter = delEntrySet.iterator(); iter.hasNext(); ) {
            iter.next();
            count++;
        }
        Assert.assertEquals(4, count);
    }

    @Test
    public void testKeySetFlatAndDelegation() {
        Flat3Map map = new Flat3Map();
        Set keySet = map.keySet();
        Assert.assertEquals(0, keySet.size());
        Assert.assertFalse(keySet.iterator().hasNext());

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals(3, keySet.size());
        Assert.assertTrue(keySet.contains("A"));
        Assert.assertFalse(keySet.contains("Z"));

        Iterator it = keySet.iterator();
        Assert.assertEquals("A", it.next());
        Assert.assertEquals("B", it.next());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertTrue(keySet.remove("B"));
        Assert.assertFalse(keySet.remove("Z"));
        Assert.assertEquals(2, map.size());

        keySet.clear();
        Assert.assertEquals(0, map.size());

        // Delegate KeySet
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        Set delKeySet = map.keySet();
        Assert.assertEquals(4, delKeySet.size());
        Assert.assertTrue(delKeySet.contains("A"));
        Assert.assertTrue(delKeySet.remove("A"));
        Assert.assertEquals(3, map.size());
    }

    @Test
    public void testValuesFlatAndDelegation() {
        Flat3Map map = new Flat3Map();
        Collection values = map.values();
        Assert.assertEquals(0, values.size());
        Assert.assertFalse(values.iterator().hasNext());

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("1"));
        Assert.assertFalse(values.contains("999"));

        Iterator it = values.iterator();
        Assert.assertEquals("1", it.next());
        Assert.assertEquals("2", it.next());
        Assert.assertEquals("3", it.next());
        Assert.assertFalse(it.hasNext());

        values.clear();
        Assert.assertEquals(0, map.size());

        // Delegate Values
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        Collection delValues = map.values();
        Assert.assertEquals(4, delValues.size());
        Assert.assertTrue(delValues.contains("1"));
        int count = 0;
        for (Iterator iter = delValues.iterator(); iter.hasNext();) {
            iter.next();
            count++;
        }
        Assert.assertEquals(4, count);
    }

    @Test
    public void testEqualsAndHashCode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();
        Assert.assertEquals(map1, map1);
        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("A", "1");
        Assert.assertFalse(map1.equals(map2));
        Assert.assertFalse(map1.equals("NotAMap"));

        map2.put("A", "1");
        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("B", "2");
        map2.put("B", "wrong");
        Assert.assertFalse(map1.equals(map2));

        map2.put("B", "2");
        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("C", "3");
        map2.put("C", "3");
        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map2.put("C", "mismatch");
        Assert.assertFalse(map1.equals(map2));

        Map missingKeyMap = new HashMap();
        missingKeyMap.put("A", "1");
        missingKeyMap.put("B", "2");
        missingKeyMap.put("Z", "3");
        Assert.assertFalse(map1.equals(missingKeyMap));

        // Equals with null values
        Flat3Map nullVal1 = new Flat3Map();
        nullVal1.put("A", null);
        Flat3Map nullVal2 = new Flat3Map();
        nullVal2.put("A", null);
        Assert.assertEquals(nullVal1, nullVal2);

        // Equals in delegation mode
        Flat3Map mapDelegate = new Flat3Map();
        mapDelegate.put("A", "1");
        mapDelegate.put("B", "2");
        mapDelegate.put("C", "3");
        mapDelegate.put("D", "4");

        Map stdMap = new HashMap();
        stdMap.put("A", "1");
        stdMap.put("B", "2");
        stdMap.put("C", "3");
        stdMap.put("D", "4");
        Assert.assertEquals(mapDelegate, stdMap);
        Assert.assertEquals(mapDelegate.hashCode(), stdMap.hashCode());
    }

    @Test
    public void testToString() {
        Flat3Map map = new Flat3Map();
        Assert.assertEquals("{}", map.toString());

        map.put("A", "1");
        Assert.assertEquals("{A=1}", map.toString());

        map.put("B", "2");
        Assert.assertEquals("{B=2,A=1}", map.toString());

        map.put("C", "3");
        Assert.assertEquals("{C=3,B=2,A=1}", map.toString());

        map.put("D", "4");
        Assert.assertTrue(map.toString().contains("D=4"));

        Flat3Map selfMap = new Flat3Map();
        selfMap.put(selfMap, selfMap);
        Assert.assertEquals("{(this Map)=(this Map)}", selfMap.toString());
    }

    @Test
    public void testClone() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        Flat3Map clone = (Flat3Map) map.clone();
        Assert.assertEquals(map, clone);
        Assert.assertNotSame(map, clone);

        // Modify original, clone unaffected
        map.put("C", "3");
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(2, clone.size());

        // Delegate mode clone
        map.put("D", "4");
        Flat3Map delegateClone = (Flat3Map) map.clone();
        Assert.assertEquals(map, delegateClone);
        Assert.assertNotSame(map, delegateClone);
    }

    @Test
    public void testSerialization() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserialized = (Flat3Map) ois.readObject();

        Assert.assertEquals(map, deserialized);
        Assert.assertEquals("1", deserialized.get("A"));
        Assert.assertEquals("2", deserialized.get("B"));
        Assert.assertEquals("3", deserialized.get("C"));

        // Serialization for delegate map
        map.put("D", "4");
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        ois = new ObjectInputStream(bais);
        Flat3Map deserializedDelegate = (Flat3Map) ois.readObject();

        Assert.assertEquals(map, deserializedDelegate);
        Assert.assertEquals(4, deserializedDelegate.size());
    }
}