package org.apache.commons.collections.map;

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

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Assert;
import org.junit.Test;

public class Flat3MapTest {

    @Test
    public void testConstructors() {
        Flat3Map map = new Flat3Map();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());

        Map<String, String> initMap = new HashMap<String, String>();
        initMap.put("A", "1");
        initMap.put("B", "2");

        Flat3Map copyMap = new Flat3Map(initMap);
        Assert.assertEquals(2, copyMap.size());
        Assert.assertEquals("1", copyMap.get("A"));
        Assert.assertEquals("2", copyMap.get("B"));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new Flat3Map(null);
    }

    @Test
    public void testGetAndContainsKeyFlatModeNonNull() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.get("A"));
        Assert.assertFalse(map.containsKey("A"));

        map.put("A", "valA");
        Assert.assertEquals("valA", map.get("A"));
        Assert.assertNull(map.get("B"));
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertFalse(map.containsKey("B"));

        map.put("B", "valB");
        Assert.assertEquals("valA", map.get("A"));
        Assert.assertEquals("valB", map.get("B"));
        Assert.assertNull(map.get("C"));
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertFalse(map.containsKey("C"));

        map.put("C", "valC");
        Assert.assertEquals("valA", map.get("A"));
        Assert.assertEquals("valB", map.get("B"));
        Assert.assertEquals("valC", map.get("C"));
        Assert.assertNull(map.get("D"));
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertFalse(map.containsKey("D"));
    }

    @Test
    public void testGetAndContainsKeyFlatModeNullKey() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.get(null));
        Assert.assertFalse(map.containsKey(null));

        map.put(null, "valNull");
        Assert.assertEquals("valNull", map.get(null));
        Assert.assertTrue(map.containsKey(null));

        Flat3Map map2 = new Flat3Map();
        map2.put("A", "valA");
        map2.put(null, "valNull");
        Assert.assertEquals("valNull", map2.get(null));
        Assert.assertTrue(map2.containsKey(null));

        Flat3Map map3 = new Flat3Map();
        map3.put("A", "valA");
        map3.put("B", "valB");
        map3.put(null, "valNull");
        Assert.assertEquals("valNull", map3.get(null));
        Assert.assertTrue(map3.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        Flat3Map map = new Flat3Map();
        Assert.assertFalse(map.containsValue("v1"));
        Assert.assertFalse(map.containsValue(null));

        map.put("k1", "v1");
        Assert.assertTrue(map.containsValue("v1"));
        Assert.assertFalse(map.containsValue("v2"));
        Assert.assertFalse(map.containsValue(null));

        map.put("k2", null);
        Assert.assertTrue(map.containsValue("v1"));
        Assert.assertTrue(map.containsValue(null));
        Assert.assertFalse(map.containsValue("v2"));

        map.put("k3", "v3");
        Assert.assertTrue(map.containsValue("v1"));
        Assert.assertTrue(map.containsValue(null));
        Assert.assertTrue(map.containsValue("v3"));
        Assert.assertFalse(map.containsValue("v4"));
    }

    @Test
    public void testPutOverwriteFlatMode() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.put("k1", "v1"));
        Assert.assertNull(map.put("k2", "v2"));
        Assert.assertNull(map.put("k3", "v3"));
        Assert.assertEquals(3, map.size());

        Assert.assertEquals("v1", map.put("k1", "v1_new"));
        Assert.assertEquals("v1_new", map.get("k1"));

        Assert.assertEquals("v2", map.put("k2", "v2_new"));
        Assert.assertEquals("v2_new", map.get("k2"));

        Assert.assertEquals("v3", map.put("k3", "v3_new"));
        Assert.assertEquals("v3_new", map.get("k3"));

        Flat3Map nullKeyMap = new Flat3Map();
        Assert.assertNull(nullKeyMap.put(null, "null1"));
        Assert.assertEquals("null1", nullKeyMap.put(null, "null2"));
        Assert.assertEquals("null2", nullKeyMap.get(null));

        nullKeyMap.put("k2", "v2");
        Assert.assertEquals("null2", nullKeyMap.put(null, "null3"));

        nullKeyMap.put("k3", "v3");
        Assert.assertEquals("null3", nullKeyMap.put(null, "null4"));

        Flat3Map nullKeyAtPos2 = new Flat3Map();
        nullKeyAtPos2.put("k1", "v1");
        nullKeyAtPos2.put(null, "null2");
        Assert.assertEquals("null2", nullKeyAtPos2.put(null, "null2_updated"));

        Flat3Map nullKeyAtPos3 = new Flat3Map();
        nullKeyAtPos3.put("k1", "v1");
        nullKeyAtPos3.put("k2", "v2");
        nullKeyAtPos3.put(null, "null3");
        Assert.assertEquals("null3", nullKeyAtPos3.put(null, "null3_updated"));
    }

    @Test
    public void testDelegateModeTransitionAndOperations() {
        Flat3Map map = new Flat3Map();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");
        Assert.assertEquals(3, map.size());

        map.put("4", "four");
        Assert.assertEquals(4, map.size());
        Assert.assertEquals("one", map.get("1"));
        Assert.assertEquals("two", map.get("2"));
        Assert.assertEquals("three", map.get("3"));
        Assert.assertEquals("four", map.get("4"));
        Assert.assertTrue(map.containsKey("1"));
        Assert.assertTrue(map.containsKey("4"));
        Assert.assertTrue(map.containsValue("four"));

        Assert.assertEquals("four", map.put("4", "four_updated"));
        Assert.assertEquals("four_updated", map.get("4"));

        Assert.assertEquals("four_updated", map.remove("4"));
        Assert.assertEquals(3, map.size());

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());

        map.put("A", "valA");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("valA", map.get("A"));
    }

    @Test
    public void testPutAll() {
        Flat3Map map = new Flat3Map();
        Map<String, String> empty = new HashMap<String, String>();
        map.putAll(empty);
        Assert.assertEquals(0, map.size());

        Map<String, String> small = new HashMap<String, String>();
        small.put("A", "1");
        small.put("B", "2");
        map.putAll(small);
        Assert.assertEquals(2, map.size());

        Map<String, String> large = new HashMap<String, String>();
        large.put("C", "3");
        large.put("D", "4");
        large.put("E", "5");
        large.put("F", "6");
        map.putAll(large);
        Assert.assertEquals(6, map.size());

        Map<String, String> more = new HashMap<String, String>();
        more.put("G", "7");
        map.putAll(more);
        Assert.assertEquals(7, map.size());
    }

    @Test
    public void testRemoveFlatModeNonNullKeys() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.remove("unknown"));

        map.put("A", "1");
        Assert.assertNull(map.remove("B"));
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(0, map.size());

        map.put("A", "1");
        map.put("B", "2");
        Assert.assertNull(map.remove("C"));
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("2", map.get("B"));
        map.clear();

        map.put("A", "1");
        map.put("B", "2");
        Assert.assertEquals("2", map.remove("B"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));
        map.clear();

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertNull(map.remove("D"));
        Assert.assertEquals("3", map.remove("C"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        map.clear();

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("3", map.remove("B"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("3", map.get("C"));
        map.clear();

        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("3", map.remove("A"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("3", map.get("C"));
        Assert.assertEquals("2", map.get("B"));
    }

    @Test
    public void testRemoveFlatModeNullKeys() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.remove(null));

        map.put(null, "valNull");
        Assert.assertEquals("valNull", map.remove(null));
        Assert.assertEquals(0, map.size());

        map.put(null, "valNull");
        map.put("B", "2");
        Assert.assertEquals("2", map.remove(null));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("2", map.get("B"));
        map.clear();

        map.put("A", "1");
        map.put(null, "valNull");
        Assert.assertEquals("valNull", map.remove(null));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));
        map.clear();

        map.put("A", "1");
        map.put("B", "2");
        Assert.assertNull(map.remove(null));

        map.put(null, "valNull");
        Assert.assertEquals("valNull", map.remove(null));
        Assert.assertEquals(2, map.size());
        map.clear();

        map.put("A", "1");
        map.put(null, "valNull");
        map.put("C", "3");
        Assert.assertEquals("3", map.remove(null));
        Assert.assertEquals(2, map.size());
        map.clear();

        map.put(null, "valNull");
        map.put("B", "2");
        map.put("C", "3");
        Assert.assertEquals("3", map.remove(null));
        Assert.assertEquals(2, map.size());
    }

    @Test
    public void testMapIteratorFlatMode() {
        Flat3Map map = new Flat3Map();
        MapIterator emptyIt = map.mapIterator();
        Assert.assertFalse(emptyIt.hasNext());

        map.put("1", "A");
        map.put("2", "B");
        map.put("3", "C");

        MapIterator it = map.mapIterator();
        Assert.assertTrue(it.hasNext());
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
            it.setValue("test");
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        try {
            it.remove();
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        Object k1 = it.next();
        Assert.assertEquals("1", k1);
        Assert.assertEquals("1", it.getKey());
        Assert.assertEquals("A", it.getValue());
        Assert.assertEquals("Iterator[1=A]", it.toString());
        Assert.assertEquals("A", it.setValue("A_mod"));
        Assert.assertEquals("A_mod", map.get("1"));

        Object k2 = it.next();
        Assert.assertEquals("2", k2);
        Assert.assertEquals("2", it.getKey());
        Assert.assertEquals("B", it.getValue());
        Assert.assertEquals("B", it.setValue("B_mod"));
        Assert.assertEquals("B_mod", map.get("2"));

        Object k3 = it.next();
        Assert.assertEquals("3", k3);
        Assert.assertEquals("3", it.getKey());
        Assert.assertEquals("C", it.getValue());
        Assert.assertEquals("C", it.setValue("C_mod"));
        Assert.assertEquals("C_mod", map.get("3"));

        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (NoSuchElementException ignored) {}

        ((ResettableIterator) it).reset();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("1", it.next());
        it.remove();
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(map.containsKey("1"));
    }

    @Test
    public void testMapIteratorDelegateMode() {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        MapIterator it = map.mapIterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(5, count);
    }

    @Test
    public void testEntrySetAndIteratorFlatMode() {
        Flat3Map map = new Flat3Map();
        Set entrySet = map.entrySet();
        Assert.assertEquals(0, entrySet.size());
        Assert.assertFalse(entrySet.iterator().hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Assert.assertEquals(3, entrySet.size());
        Map.Entry dummyNonEntry = new Map.Entry() {
            public Object getKey() { return "k1"; }
            public Object getValue() { return "v1"; }
            public Object setValue(Object value) { return null; }
        };
        Assert.assertTrue(entrySet.remove(dummyNonEntry));
        Assert.assertEquals(2, map.size());
        Assert.assertFalse(entrySet.remove("non-entry-obj"));
        Assert.assertFalse(entrySet.remove(dummyNonEntry));

        map.put("k1", "v1");
        Iterator it = entrySet.iterator();

        try {
            ((Map.Entry) it).getKey();
            Assert.fail();
        } catch (IllegalStateException ignored) {}
        try {
            ((Map.Entry) it).getValue();
            Assert.fail();
        } catch (IllegalStateException ignored) {}
        try {
            ((Map.Entry) it).setValue("x");
            Assert.fail();
        } catch (IllegalStateException ignored) {}
        Assert.assertEquals("", it.toString());
        Assert.assertEquals(0, it.hashCode());
        Assert.assertFalse(it.equals(dummyNonEntry));

        Assert.assertTrue(it.hasNext());
        Map.Entry entry = (Map.Entry) it.next();
        Assert.assertEquals("k2", entry.getKey());
        Assert.assertEquals("v2", entry.getValue());
        Assert.assertEquals("v2", entry.setValue("v2_new"));
        Assert.assertEquals("k2=v2_new", entry.toString());
        Assert.assertTrue(entry.equals(entry));
        Assert.assertFalse(entry.equals("someString"));
        Assert.assertFalse(entry.equals(dummyNonEntry));

        it.remove();
        try {
            it.remove();
            Assert.fail();
        } catch (IllegalStateException ignored) {}

        it.next();
        entry.setValue("v3_new");
        it.next();
        entry.setValue("v1_new");

        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (NoSuchElementException ignored) {}

        entrySet.clear();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testEntrySetDelegateMode() {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        Set entrySet = map.entrySet();
        Assert.assertEquals(5, entrySet.size());
        Iterator it = entrySet.iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(5, count);
    }

    @Test
    public void testKeySetAndIterator() {
        Flat3Map map = new Flat3Map();
        Set keySet = map.keySet();
        Assert.assertEquals(0, keySet.size());
        Assert.assertFalse(keySet.iterator().hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Assert.assertEquals(3, keySet.size());
        Assert.assertTrue(keySet.contains("k2"));
        Assert.assertFalse(keySet.contains("k99"));

        Assert.assertTrue(keySet.remove("k2"));
        Assert.assertFalse(keySet.remove("k99"));
        Assert.assertEquals(2, keySet.size());

        Iterator it = keySet.iterator();
        Assert.assertTrue(it.hasNext());
        Object k = it.next();
        Assert.assertNotNull(k);

        keySet.clear();
        Assert.assertEquals(0, map.size());

        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        Set delegateKeySet = map.keySet();
        Assert.assertEquals(5, delegateKeySet.size());
        Iterator dIt = delegateKeySet.iterator();
        int count = 0;
        while (dIt.hasNext()) {
            dIt.next();
            count++;
        }
        Assert.assertEquals(5, count);
    }

    @Test
    public void testValuesAndIterator() {
        Flat3Map map = new Flat3Map();
        Collection values = map.values();
        Assert.assertEquals(0, values.size());
        Assert.assertFalse(values.iterator().hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("v2"));
        Assert.assertFalse(values.contains("v99"));

        Iterator it = values.iterator();
        Assert.assertTrue(it.hasNext());
        Object v = it.next();
        Assert.assertNotNull(v);

        values.clear();
        Assert.assertEquals(0, map.size());

        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        Collection delegateValues = map.values();
        Assert.assertEquals(5, delegateValues.size());
        Iterator dIt = delegateValues.iterator();
        int count = 0;
        while (dIt.hasNext()) {
            dIt.next();
            count++;
        }
        Assert.assertEquals(5, count);
    }

    @Test
    public void testEqualsAndHashCodeFlatMode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();
        Assert.assertTrue(map1.equals(map1));
        Assert.assertTrue(map1.equals(map2));
        Assert.assertFalse(map1.equals("Not a map"));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("k1", "v1");
        Assert.assertFalse(map1.equals(map2));

        map2.put("k1", "v1");
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("k2", "v2");
        map2.put("k2", "diff");
        Assert.assertFalse(map1.equals(map2));

        map2.put("k2", "v2");
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("k3", null);
        map2.put("k3", "notNull");
        Assert.assertFalse(map1.equals(map2));

        map2.put("k3", null);
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        Map<String, String> diffKeysMap = new HashMap<String, String>();
        diffKeysMap.put("k1", "v1");
        diffKeysMap.put("k2", "v2");
        diffKeysMap.put("kOther", null);
        Assert.assertFalse(map1.equals(diffKeysMap));

        Flat3Map size1NullVal = new Flat3Map();
        size1NullVal.put("k1", null);
        Assert.assertEquals("k1".hashCode() ^ 0, size1NullVal.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeDelegateMode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map1.put("k" + i, "v" + i);
            map2.put("k" + i, "v" + i);
        }
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    public void testClone() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Flat3Map clone = (Flat3Map) map.clone();
        Assert.assertEquals(map.size(), clone.size());
        Assert.assertEquals(map, clone);

        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        Flat3Map delegateClone = (Flat3Map) map.clone();
        Assert.assertEquals(map.size(), delegateClone.size());
        Assert.assertEquals(map, delegateClone);
    }

    @Test
    public void testToString() {
        Flat3Map map = new Flat3Map();
        Assert.assertEquals("{}", map.toString());

        map.put("1", "A");
        Assert.assertEquals("{1=A}", map.toString());

        map.put("2", "B");
        Assert.assertEquals("{2=B,1=A}", map.toString());

        map.put("3", "C");
        Assert.assertEquals("{3=C,2=B,1=A}", map.toString());

        Flat3Map selfMap = new Flat3Map();
        selfMap.put(selfMap, selfMap);
        Assert.assertEquals("{(this Map)=(this Map)}", selfMap.toString());

        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        Assert.assertTrue(map.toString().startsWith("{"));
    }

    @Test
    public void testSerializationFlatAndDelegate() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        Assert.assertEquals(3, deserialized.size());
        Assert.assertEquals("one", deserialized.get("1"));
        Assert.assertEquals("two", deserialized.get("2"));
        Assert.assertEquals("three", deserialized.get("3"));

        for (int i = 4; i <= 6; i++) {
            map.put("k" + i, "v" + i);
        }

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        ois = new ObjectInputStream(bais);
        deserialized = (Flat3Map) ois.readObject();
        ois.close();

        Assert.assertEquals(6, deserialized.size());
        Assert.assertEquals("v4", deserialized.get("k4"));
    }

    @Test
    public void testCustomDelegateMapOverride() {
        class CustomFlat3Map extends Flat3Map {
            @Override
            protected AbstractHashedMap createDelegateMap() {
                return new HashedMap();
            }
        }

        CustomFlat3Map customMap = new CustomFlat3Map();
        for (int i = 0; i < 5; i++) {
            customMap.put("k" + i, "v" + i);
        }
        Assert.assertEquals(5, customMap.size());
        Assert.assertEquals("v0", customMap.get("k0"));
    }
}
