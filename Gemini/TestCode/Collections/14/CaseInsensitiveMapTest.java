package org.apache.commons.collections.map;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CaseInsensitiveMapTest {

    @Test
    public void testDefaultConstructor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testInitialCapacityConstructor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(32);
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacityConstructorInvalid() {
        new CaseInsensitiveMap(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacityNegativeConstructor() {
        new CaseInsensitiveMap(-1);
    }

    @Test
    public void testInitialCapacityAndLoadFactorConstructor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(16, 0.75f);
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacityAndLoadFactorConstructorInvalidCapacity() {
        new CaseInsensitiveMap(0, 0.75f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacityAndLoadFactorConstructorInvalidLoadFactor() {
        new CaseInsensitiveMap(16, -0.1f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacityAndLoadFactorConstructorNaNLoadFactor() {
        new CaseInsensitiveMap(16, Float.NaN);
    }

    @Test
    public void testMapConstructor() {
        Map<String, String> initMap = new HashMap<String, String>();
        initMap.put("KeyA", "ValueA");
        initMap.put("keya", "ValueA2");
        initMap.put("KeyB", "ValueB");

        CaseInsensitiveMap map = new CaseInsensitiveMap(initMap);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("ValueB", map.get("KEYB"));
        Assert.assertTrue(map.containsKey("keya"));
    }

    @Test(expected = NullPointerException.class)
    public void testMapConstructorWithNull() {
        new CaseInsensitiveMap(null);
    }

    @Test
    public void testPutAndGetCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "One");
        map.put("Two", "Two");
        map.put(null, "Three");
        map.put("one", "Four");

        Assert.assertEquals(3, map.size());
        Assert.assertEquals("Three", map.get(null));
        Assert.assertEquals("Four", map.get("ONE"));
        Assert.assertEquals("Four", map.get("one"));
        Assert.assertEquals("Four", map.get("One"));
        Assert.assertEquals("Two", map.get("TWO"));
        Assert.assertEquals("Two", map.get("two"));
        Assert.assertNull(map.get("nonexistent"));
    }

    @Test
    public void testContainsKeyAndContainsValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Alpha", "Value1");
        map.put(null, "ValueNull");

        Assert.assertTrue(map.containsKey("alpha"));
        Assert.assertTrue(map.containsKey("ALPHA"));
        Assert.assertTrue(map.containsKey("Alpha"));
        Assert.assertTrue(map.containsKey(null));
        Assert.assertFalse(map.containsKey("Beta"));

        Assert.assertTrue(map.containsValue("Value1"));
        Assert.assertTrue(map.containsValue("ValueNull"));
        Assert.assertFalse(map.containsValue("Missing"));
    }

    @Test
    public void testRemove() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("DeleteMe", "Value");
        map.put(null, "NullValue");

        Assert.assertEquals("Value", map.remove("DELETEME"));
        Assert.assertFalse(map.containsKey("deleteme"));
        Assert.assertNull(map.remove("deleteme"));

        Assert.assertEquals("NullValue", map.remove(null));
        Assert.assertFalse(map.containsKey(null));
        Assert.assertNull(map.remove(null));
    }

    @Test
    public void testNonStringKeys() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Integer intKey = Integer.valueOf(123);
        map.put(intKey, "NumberValue");

        Assert.assertEquals("NumberValue", map.get("123"));
        Assert.assertEquals("NumberValue", map.get(123));
        Assert.assertTrue(map.containsKey("123"));
        Assert.assertTrue(map.containsKey(intKey));

        Object customObj = new Object() {
            @Override
            public String toString() {
                return "CustomObjectKey";
            }
        };

        map.put(customObj, "CustomVal");
        Assert.assertEquals("CustomVal", map.get("customobjectkey"));
        Assert.assertEquals("CustomVal", map.get("CUSTOMOBJECTKEY"));
        Assert.assertEquals("CustomVal", map.get(customObj));
    }

    @Test
    public void testConvertKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Assert.assertEquals(AbstractHashedMap.NULL, map.convertKey(null));
        Assert.assertEquals("hello", map.convertKey("HeLLo"));
        Assert.assertEquals("123", map.convertKey(123));
    }

    @Test
    public void testKeySet() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("HeLLo", "1");
        map.put("WORLD", "2");
        map.put(null, "3");

        Set keys = map.keySet();
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keys.contains("hello"));
        Assert.assertTrue(keys.contains("world"));
        Assert.assertTrue(keys.contains(null));
    }

    @Test
    public void testClone() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("KeyA", "ValA");
        map.put(null, "ValNull");

        CaseInsensitiveMap cloned = (CaseInsensitiveMap) map.clone();
        Assert.assertNotNull(cloned);
        Assert.assertNotSame(map, cloned);
        Assert.assertEquals(map.size(), cloned.size());
        Assert.assertEquals("ValA", cloned.get("keya"));
        Assert.assertEquals("ValNull", cloned.get(null));

        cloned.put("KeyB", "ValB");
        Assert.assertFalse(map.containsKey("keyb"));
        Assert.assertTrue(cloned.containsKey("keyb"));
    }

    @Test
    public void testSerialization() throws Exception {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("KeyOne", "ValOne");
        map.put("KEYTWO", "ValTwo");
        map.put(null, "ValNull");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(3, deserialized.size());
        Assert.assertEquals("ValOne", deserialized.get("keyone"));
        Assert.assertEquals("ValOne", deserialized.get("KEYONE"));
        Assert.assertEquals("ValTwo", deserialized.get("keytwo"));
        Assert.assertEquals("ValNull", deserialized.get(null));
    }

    @Test
    public void testPutAll() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Map<String, String> source = new HashMap<String, String>();
        source.put("A", "1");
        source.put("a", "2");
        source.put(null, "3");

        map.putAll(source);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("3", map.get(null));
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("a"));
    }

    @Test
    public void testClear() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("A", "1");
        map.put(null, "2");
        Assert.assertEquals(2, map.size());

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());
        Assert.assertFalse(map.containsKey("a"));
        Assert.assertFalse(map.containsKey(null));
    }
}
