package org.apache.commons.collections.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveMapTest {

    @Test
    public void testDefaultConstructor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testConstructorWithInitialCapacity() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10);
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInitialCapacityNegative() {
        new CaseInsensitiveMap(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInitialCapacityZero() {
        new CaseInsensitiveMap(0);
    }

    @Test
    public void testConstructorWithCapacityAndLoadFactor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10, 0.75f);
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityNegativeAndLoadFactor() {
        new CaseInsensitiveMap(-1, 0.75f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityAndNegativeLoadFactor() {
        new CaseInsensitiveMap(10, -0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityAndZeroLoadFactor() {
        new CaseInsensitiveMap(10, 0.0f);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMap() {
        new CaseInsensitiveMap((Map) null);
    }

    @Test
    public void testConstructorWithMapEmpty() {
        Map<String, String> original = new HashMap<String, String>();
        CaseInsensitiveMap map = new CaseInsensitiveMap(original);
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testConstructorWithMap_KeyCaseConversion() {
        Map<String, String> original = new HashMap<String, String>();
        original.put("One", "Value1");
        original.put("Two", "Value2");
        original.put("one", "Value3"); // same as "One" after lowercasing
        CaseInsensitiveMap map = new CaseInsensitiveMap(original);
        // After conversion, keys "One" and "one" become both "one", so second overwrites first.
        Assert.assertEquals(2, map.size());
        // The entry for "One" should have been replaced by "one" because of lowercasing; order depends on iteration but final size is 2.
        // Key set should contain lowercased strings: "one", "two"
        Set keySet = map.keySet();
        Assert.assertTrue(keySet.contains("one"));
        Assert.assertTrue(keySet.contains("two"));
        Assert.assertFalse(keySet.contains("One"));
        Assert.assertEquals("Value3", map.get("one"));
        Assert.assertEquals("Value3", map.get("ONE"));
    }

    @Test
    public void testPutAndGetBasic() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "Value");
        Assert.assertEquals("Value", map.get("Key"));
        Assert.assertEquals("Value", map.get("KEY"));
        Assert.assertEquals("Value", map.get("key"));
        Assert.assertEquals("Value", map.get("kEy"));
    }

    @Test
    public void testPutOverwritesBasedOnLowercase() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(); // typo! fixed.
    }

I need to fix the code. I'll write a full test class without errors.

Let's rewrite the testPutOverwritesBasedOnLowercase method properly.

I will produce the final Java code now.package org.apache.commons.collections.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveMapTest {

    @Test
    public void testDefaultConstructor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testConstructorWithInitialCapacity() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10);
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInitialCapacityNegative() {
        new CaseInsensitiveMap(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInitialCapacityZero() {
        new CaseInsensitiveMap(0);
    }

    @Test
    public void testConstructorWithCapacityAndLoadFactor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10, 0.75f);
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityNegativeAndLoadFactor() {
        new CaseInsensitiveMap(-1, 0.75f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityAndNegativeLoadFactor() {
        new CaseInsensitiveMap(10, -0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithCapacityAndZeroLoadFactor() {
        new CaseInsensitiveMap(10, 0.0f);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMap() {
        new CaseInsensitiveMap((Map) null);
    }

    @Test
    public void testConstructorWithMapEmpty() {
        Map<String, String> original = new HashMap<String, String>();
        CaseInsensitiveMap map = new CaseInsensitiveMap(original);
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testConstructorWithMapKeyCaseConversion() {
        Map<String, String> original = new HashMap<String, String>();
        original.put("One", "Value1");
        original.put("Two", "Value2");
        original.put("one", "Value3");
        CaseInsensitiveMap map = new CaseInsensitiveMap(original);
        Assert.assertEquals(2, map.size());
        Set keySet = map.keySet();
        Assert.assertTrue(keySet.contains("one"));
        Assert.assertTrue(keySet.contains("two"));
        Assert.assertFalse(keySet.contains("One"));
        Assert.assertEquals("Value3", map.get("one"));
        Assert.assertEquals("Value3", map.get("ONE"));
    }

    @Test
    public void testPutAndGetBasic() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "Value");
        Assert.assertEquals("Value", map.get("Key"));
        Assert.assertEquals("Value", map.get("KEY"));
        Assert.assertEquals("Value", map.get("key"));
        Assert.assertEquals("Value", map.get("kEy"));
    }

    @Test
    public void testPutOverwritesBasedOnLowercase() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "First");
        map.put("KEY", "Second");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("Second", map.get("key"));
        Assert.assertEquals("Second", map.get("KEY"));
    }

    @Test
    public void testNullKeySupport() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "nullValue");
        Assert.assertEquals("nullValue", map.get(null));
        Assert.assertTrue(map.containsKey(null));
    }

    @Test
    public void testNullKeyOverwrite() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "first");
        map.put(null, "second");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("second", map.get(null));
    }

    @Test
    public void testContainsKeyCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("AbC", "test");
        Assert.assertTrue(map.containsKey("abc"));
        Assert.assertTrue(map.containsKey("ABC"));
        Assert.assertTrue(map.containsKey("AbC"));
        Assert.assertFalse(map.containsKey("xyz"));
    }

    @Test
    public void testContainsKeyNull() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "nullVal");
        Assert.assertTrue(map.containsKey(null));
    }

    @Test
    public void testRemoveCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Hello", "World");
        Assert.assertEquals("World", map.remove("HELLO"));
        Assert.assertNull(map.get("hello"));
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testRemoveNull() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "nullVal");
        Assert.assertEquals("nullVal", map.remove(null));
        Assert.assertNull(map.get(null));
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testKeySetContainsLowerCaseAndNulls() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "1");
        map.put("TWO", "2");
        map.put(null, "3");
        Set keySet = map.keySet();
        Assert.assertEquals(3, keySet.size());
        Assert.assertTrue(keySet.contains("one"));
        Assert.assertTrue(keySet.contains("two"));
        Assert.assertTrue(keySet.contains(null));
        Assert.assertFalse(keySet.contains("One"));
    }

    @Test
    public void testClone() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("A", "1");
        map.put("b", "2");
        CaseInsensitiveMap cloned = (CaseInsensitiveMap) map.clone();
        Assert.assertEquals(map, cloned);
        Assert.assertNotSame(map, cloned);
        Assert.assertEquals("1", cloned.get("a"));
        Assert.assertEquals("2", cloned.get("B"));
        // Modify original does not affect clone
        map.put("c", "3");
        Assert.assertFalse(cloned.containsKey("c"));
    }

    @Test
    public void testSerialization() throws Exception {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "Value");
        map.put(null, "nullVal");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) ois.readObject();
        Assert.assertEquals(map.size(), deserialized.size());
        Assert.assertEquals("Value", deserialized.get("KEY"));
        Assert.assertEquals("nullVal", deserialized.get(null));
        Assert.assertTrue(deserialized.keySet().contains("key"));
        Assert.assertTrue(deserialized.keySet().contains(null));
    }

    @Test
    public void testConvertKeyIndirectlyWithNonStringKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object customKey = new Object() {
            @Override
            public String toString() {
                return "CUSTOMKEY";
            }
        };
        map.put(customKey, "value");
        Assert.assertEquals("value", map.get("customkey"));
        Assert.assertTrue(map.containsKey("CUSTOMKEY"));
        Assert.assertFalse(map.containsKey("other"));
    }

    @Test
    public void testPutAll() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Map<String, String> source = new HashMap<String, String>();
        source.put("One", "1");
        source.put("TWO", "2");
        map.putAll(source);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("one"));
        Assert.assertEquals("2", map.get("two"));
    }

    @Test
    public void testEqualsAndHashCode() {
        CaseInsensitiveMap map1 = new CaseInsensitiveMap();
        map1.put("key", "value");
        CaseInsensitiveMap map2 = new CaseInsensitiveMap();
        map2.put("KEY", "value");
        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map1.hashCode(), map2.hashCode());
    }
}
