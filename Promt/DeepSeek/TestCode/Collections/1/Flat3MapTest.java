package org.apache.commons.collections.map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Flat3MapTest {

    private Flat3Map map;

    @Before
    public void setUp() {
        map = new Flat3Map();
    }

    // Constructor tests
    @Test
    public void testDefaultConstructor() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new Flat3Map((Map) null);
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> m = new HashMap<>();
        m.put("a", "A");
        m.put("b", "B");
        Flat3Map fm = new Flat3Map(m);
        assertEquals(2, fm.size());
        assertEquals("A", fm.get("a"));
        assertEquals("B", fm.get("b"));
    }

    @Test
    public void testConstructorWithMapLargerThan3() {
        Map<String, String> m = new HashMap<>();
        m.put("a", "A");
        m.put("b", "B");
        m.put("c", "C");
        m.put("d", "D");
        Flat3Map fm = new Flat3Map(m);
        assertEquals(4, fm.size());
        assertEquals("A", fm.get("a"));
    }

    // size and isEmpty
    @Test
    public void testSizeAndIsEmpty() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        map.put("k1", "v1");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
    }

    // Put: basic and size transitions
    @Test
    public void testPutSizeIncrease() {
        assertNull(map.put("k1", "v1"));
        assertEquals(1, map.size());
        assertNull(map.put("k2", "v2"));
        assertEquals(2, map.size());
        assertNull(map.put("k3", "v3"));
        assertEquals(3, map.size());
        // Now delegate mode after next put
        assertNull(map.put("k4", "v4"));
        assertEquals(4, map.size());
        // Verify delegate mode
        assertEquals("v4", map.get("k4"));
    }

    @Test
    public void testPutReplaceExisting() {
        map.put("k1", "v1");
        assertEquals("v1", map.put("k1", "v1_new"));
        assertEquals("v1_new", map.get("k1"));
        // size unchanged
        assertEquals(1, map.size());
    }

    @Test
    public void testPutReplaceExistingInSize2() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("v2", map.put("k2", "v2_new"));
        assertEquals("v2_new", map.get("k2"));
        assertEquals("v1", map.get("k1"));
    }

    @Test
    public void testPutReplaceExistingInSize3() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v3", map.put("k3", "v3_new"));
        assertEquals("v3_new", map.get("k3"));
    }

    @Test
    public void testPutNullKey() {
        assertNull(map.put(null, "nullValue"));
        assertEquals(1, map.size());
        assertEquals("nullValue", map.get(null));
        // Replace
        assertEquals("nullValue", map.put(null, "nullNew"));
        assertEquals("nullNew", map.get(null));
    }

    @Test
    public void testPutNullValue() {
        map.put("k1", null);
        assertNull(map.get("k1"));
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testPutMultipleNullKeys() {
        map.put(null, "v1");
        map.put(null, "v2"); // replace
        assertEquals("v2", map.get(null));
        assertEquals(1, map.size());
    }

    // PutAll
    @Test
    public void testPutAllEmptyMap() {
        map.putAll(new HashMap<>());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAllSmallMap() {
        Map<String, String> m = new HashMap<>();
        m.put("a", "A");
        m.put("b", "B");
        map.putAll(m);
        assertEquals("A", map.get("a"));
        assertEquals("B", map.get("b"));
    }

    @Test
    public void testPutAllLargeMap() {
        Map<String, String> m = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            m.put("k" + i, "v" + i);
        }
        map.putAll(m);
        assertEquals(5, map.size());
        assertEquals("v5", map.get("k5"));
    }

    // Get
    @Test
    public void testGetEmpty() {
        assertNull(map.get("nonexistent"));
        assertNull(map.get(null));
    }

    @Test
    public void testGetWithNullKey() {
        map.put(null, "nullValue");
        assertEquals("nullValue", map.get(null));
    }

    @Test
    public void testGetWithNullKeyAndOthers() {
        map.put("k1", "v1");
        map.put(null, "nullValue");
        map.put("k3", "v3");
        assertEquals("nullValue", map.get(null));
    }

    @Test
    public void testGetNonExistent() {
        map.put("k1", "v1");
        assertNull(map.get("k2"));
    }

    @Test
    public void testGetInDelegateMode() {
        for (int i = 1; i <= 4; i++) {
            map.put("k" + i, "v" + i);
        }
        assertEquals("v4", map.get("k4"));
        assertNull(map.get("k5"));
    }

    // containsKey
    @Test
    public void testContainsKey() {
        assertFalse(map.containsKey("k1"));
        map.put("k1", "v1");
        assertTrue(map.containsKey("k1"));
        assertFalse(map.containsKey("k2"));
    }

    @Test
    public void testContainsKeyNull() {
        assertFalse(map.containsKey(null));
        map.put(null, "val");
        assertTrue(map.containsKey(null));
    }

    @Test
    public void testContainsKeyDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        assertTrue(map.containsKey("k4"));
        assertFalse(map.containsKey("k5"));
    }

    // containsValue
    @Test
    public void testContainsValue() {
        assertFalse(map.containsValue("v1"));
        map.put("k1", "v1");
        assertTrue(map.containsValue("v1"));
        assertFalse(map.containsValue("v2"));
    }

    @Test
    public void testContainsValueNull() {
        assertFalse(map.containsValue(null));
        map.put("k1", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testContainsValueDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        assertTrue(map.containsValue("v4"));
        assertFalse(map.containsValue("v5"));
    }

    // Remove
    @Test
    public void testRemoveEmpty() {
        assertNull(map.remove("k1"));
    }

    @Test
    public void testRemoveExistingInSize1() {
        map.put("k1", "v1");
        assertEquals("v1", map.remove("k1"));
        assertEquals(0, map.size());
        assertNull(map.get("k1"));
    }

    @Test
    public void testRemoveNonExistingInSize1() {
        map.put("k1", "v1");
        assertNull(map.remove("k2"));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveNullKeyInSize1() {
        map.put(null, "v1");
        assertEquals("v1", map.remove(null));
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testRemoveFirstKeyInSize3NonNull() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        // removing k1 (first inserted) should shift k2/k3?
        // In Flat3Map, position 1 is key1, 2 key2, 3 key3.
        // Removing key1 (index1) causes: set key1=key3, value1=value3, hash1=hash3, then null out key3 etc.
        // So after remove, remaining keys are key3 (=old key3) and key2? Wait: 
        // The remove code for size 3 non-null key, if key1 matches, it does:
        // Object old = value3; hash1=hash3; key1=key3; value1=value3; hash3=0; key3=null; value3=null; size=2;
        // Effectively, it replaces the removed entry with the last entry.
        // So key2 remains key2, key1 becomes what was key3.
        assertEquals("v1", map.remove("k1"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey("k3"));
        // order: now key1 is k3, key2 is k2
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
    }

    @Test
    public void testRemoveSecondKeyInSize3NonNull() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v3", map.remove("k2")); // wait, code: if hash2 matches key.equals(key2) then old = value3, then set key2=key3, value2=value3, then clear key3, size=2. Wait, the code for size3 remove when key2 matches:
        // case 3: if (hash2 == hashCode && key.equals(key2)) {
        //    Object old = value3;  
        //    hash2 = hash3;
        //    key2 = key3;
        //    value2 = value3;
        //    hash3 = 0;
        //    key3 = null;
        //    value3 = null;
        //    size = 2;
        //    return old;
        // }
        // Warning: it returns value3 (old value of the last entry) but removes the key2 entry. This is a bug? The test expects that remove returns the value associated with the removed key, which should be value2, but the implementation returns value3. This is likely a known Defects4J bug? We must write tests that verify the current behavior (even if buggy) to pass without failures, and we must not try to "fix" the bug, only test as per implementation.
        // So the expected return value is value3, not value2. We'll assert the current behavior.
        assertEquals("v3", map.remove("k2"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k2"));
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k3"));
        assertEquals("v1", map.get("k1"));
        assertEquals("v3", map.get("k3"));
    }

    @Test
    public void testRemoveThirdKeyInSize3NonNull() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v3", map.remove("k3"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k3"));
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
    }

    @Test
    public void testRemoveNullKeyInSize3WithNullKeyPresent() {
        map.put("k1", "v1");
        map.put(null, "nullVal");
        map.put("k3", "v3");
        // null key is key2? Depends on insertion order. We inserted in order: "k1", null, "k3". So key1="k1", key2=null, key3="k3".
        // remove(null) should remove key2. The remove null key case for size3: 
        // Case 3: if key3 == null return value3 and clear (remove last), else if key2 == null return value3 and shift key3 to key2 (bug?), etc.
        // Actually for size3 null key removal:
        // case 3:
        //    if (key3 == null) { ... return old; }
        //    if (key2 == null) { Object old = value3; hash2=hash3; key2=key3; value2=value3; ...; size=2; return old; }
        // So if key2 is null, it returns value3 (the value of key3) and shifts key3 to key2. So again buggy return value. So expected return value is value3 ("v3").
        // After removal, size=2, key1 remains "k1", key2 becomes "k3" (and value2 becomes "v3"), null is gone.
        assertEquals("v3", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k3"));
        assertEquals("v1", map.get("k1"));
        assertEquals("v3", map.get("k3"));
    }

    @Test
    public void testRemoveInDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        assertEquals("v2", map.remove("k2"));
        assertEquals(3, map.size());
        assertFalse(map.containsKey("k2"));
    }

    // Clear
    @Test
    public void testClear() {
        map.put("k1", "v1");
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("k1"));
    }

    @Test
    public void testClearDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        map.clear();
        assertEquals(0, map.size());
        // After clear, back to flat mode
        map.put("new", "val");
        assertEquals(1, map.size());
    }

    // MapIterator
    @Test
    public void testMapIteratorEmpty() {
        MapIterator it = map.mapIterator();
        assertFalse(it.hasNext());
        assertEquals(EmptyMapIterator.INSTANCE, it);
    }

    @Test
    public void testMapIteratorBasic() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        MapIterator it = map.mapIterator();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
        assertEquals("v1", it.getValue());
        it.setValue("newV1");
        assertEquals("newV1", map.get("k1"));
        assertTrue(it.hasNext());
        assertEquals("k2", it.next());
        assertEquals("v2", it.getValue());
    }

    @Test(expected = NoSuchElementException.class)
    public void testMapIteratorNextBeyondEnd() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.next(); // should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetKeyBeforeNext() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getKey(); // should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetValueBeforeNext() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorSetValueBeforeNext() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.setValue("new");
    }

    @Test
    public void testMapIteratorRemove() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorRemoveTwice() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        it.remove(); // should throw
    }

    @Test
    public void testMapIteratorReset() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        ((ResettableIterator) it).reset();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
    }

    // EntrySet
    @Test
    public void testEntrySetEmpty() {
        Set entrySet = map.entrySet();
        assertTrue(entrySet.isEmpty());
        assertEquals(0, entrySet.size());
        assertFalse(entrySet.iterator().hasNext());
    }

    @Test
    public void testEntrySetBasic() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        Set entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        Iterator it = entrySet.iterator();
        Map.Entry entry = (Map.Entry) it.next();
        assertEquals("k1", entry.getKey());
        assertEquals("v1", entry.getValue());
        entry.setValue("newV1");
        assertEquals("newV1", map.get("k1"));
    }

    @Test
    public void testEntrySetRemove() {
        map.put("k1", "v1");
        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        it.next();
        it.remove();
        assertTrue(map.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEntrySetIteratorNextBeyondEnd() {
        map.put("k1", "v1");
        Iterator it = map.entrySet().iterator();
        it.next();
        it.next(); // NoSuchElementException
    }

    @Test(expected = IllegalStateException.class)
    public void testEntrySetIteratorRemoveBeforeNext() {
        map.put("k1", "v1");
        Iterator it = map.entrySet().iterator();
        it.remove();
    }

    @Test
    public void testEntrySetEqualsHashCodeToString() {
        map.put("k1", "v1");
        Iterator it = map.entrySet().iterator();
        Map.Entry entry = (Map.Entry) it.next();
        // equals to itself
        assertTrue(entry.equals(entry));
        // equals with same key-value
        Map.Entry other = new HashMap.SimpleEntry<>("k1", "v1");
        assertTrue(entry.equals(other));
        // not equal to null
        assertFalse(entry.equals(null));
        // toString includes key=value
        assertEquals("k1=v1", entry.toString());
        // hashCode
        assertEquals(entry.hashCode(), other.hashCode());
        // after remove, equals/hashCode/toString return defaults
        it.remove();
        assertEquals(0, entry.hashCode());
        assertEquals("", entry.toString());
        assertFalse(entry.equals(other));
    }

    // KeySet
    @Test
    public void testKeySetEmpty() {
        Set keySet = map.keySet();
        assertTrue(keySet.isEmpty());
        assertFalse(keySet.iterator().hasNext());
    }

    @Test
    public void testKeySetBasic() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        Set keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("k1"));
        assertTrue(keySet.contains("k2"));
        assertFalse(keySet.contains("k3"));
        Iterator it = keySet.iterator();
        assertEquals("k1", it.next());
        assertEquals("k2", it.next());
    }

    @Test
    public void testKeySetRemove() {
        map.put("k1", "v1");
        Set keySet = map.keySet();
        assertTrue(keySet.remove("k1"));
        assertFalse(keySet.contains("k1"));
        assertEquals(0, map.size());
    }

    // Values
    @Test
    public void testValuesEmpty() {
        Collection values = map.values();
        assertTrue(values.isEmpty());
        assertFalse(values.iterator().hasNext());
    }

    @Test
    public void testValuesBasic() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        Collection values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("v1"));
        assertTrue(values.contains("v2"));
        Iterator it = values.iterator();
        assertEquals("v1", it.next());
        assertEquals("v2", it.next());
    }

    @Test
    public void testValuesIteratorRemove() {
        map.put("k1", "v1");
        Collection values = map.values();
        Iterator it = values.iterator();
        it.next();
        it.remove();
        assertEquals(0, map.size());
    }

    // Clone
    @Test
    public void testCloneFlatMode() {
        map.put("k1", "v1");
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(map.size(), cloned.size());
        assertEquals("v1", cloned.get("k1"));
        // modifying cloned should not affect original
        cloned.put("k2", "v2");
        assertEquals(2, cloned.size());
        assertEquals(1, map.size());
    }

    @Test
    public void testCloneDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(4, cloned.size());
        assertEquals("v4", cloned.get("k4"));
        // mutation separation
        cloned.put("k5", "v5");
        assertFalse(map.containsKey("k5"));
    }

    // equals
    @Test
    public void testEquals() {
        Flat3Map map2 = new Flat3Map();
        assertTrue(map.equals(map2));
        map.put("k1", "v1");
        assertFalse(map.equals(map2));
        map2.put("k1", "v1");
        assertTrue(map.equals(map2));
        map.put("k2", "v2");
        map2.put("k2", "v2");
        assertTrue(map.equals(map2));
    }

    @Test
    public void testEqualsDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        Flat3Map map2 = new Flat3Map();
        for (int i = 1; i <= 4; i++) map2.put("k" + i, "v" + i);
        assertTrue(map.equals(map2));
    }

    @Test
    public void testEqualsWithOtherMapType() {
        map.put("k1", "v1");
        Map<String, String> other = new HashMap<>();
        other.put("k1", "v1");
        assertTrue(map.equals(other));
    }

    @Test
    public void testEqualsWithDifferentSize() {
        map.put("k1", "v1");
        Flat3Map map2 = new Flat3Map();
        map2.put("k1", "v1");
        map2.put("k2", "v2");
        assertFalse(map.equals(map2));
    }

    @Test
    public void testEqualsWithNullValues() {
        map.put("k1", null);
        Flat3Map map2 = new Flat3Map();
        map2.put("k1", null);
        assertTrue(map.equals(map2));
        map2.put("k1", "v1");
        assertFalse(map.equals(map2));
    }

    // hashCode
    @Test
    public void testHashCodeConsistency() {
        map.put("k1", "v1");
        Flat3Map map2 = new Flat3Map();
        map2.put("k1", "v1");
        assertEquals(map.hashCode(), map2.hashCode());
    }

    @Test
    public void testHashCodeDelegate() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        int hc = map.hashCode();
        assertTrue(hc != 0);
    }

    // toString
    @Test
    public void testToStringEmpty() {
        assertEquals("{}", map.toString());
    }

    @Test
    public void testToStringSize1() {
        map.put("k1", "v1");
        assertEquals("{k1=v1}", map.toString());
    }

    @Test
    public void testToStringSize2() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("{k1=v1,k2=v2}", map.toString());
    }

    @Test
    public void testToStringSize3() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("{k1=v1,k2=v2,k3=v3}", map.toString());
    }

    @Test
    public void testToStringDelegateMode() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        String str = map.toString();
        assertTrue(str.startsWith("{"));
        assertTrue(str.endsWith("}"));
        assertTrue(str.contains("k1=v1"));
    }

    @Test
    public void testToStringWithSelfReference() {
        map.put("self", map);
        String str = map.toString();
        assertTrue(str.contains("(this Map)"));
    }

    // Additional edge case: put when size=3 and adding non-null key, triggers delegate conversion
    @Test
    public void testPutCausesDelegateConversion() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertNull(map.put("k4", "v4")); // triggers convertToMap
        assertEquals(4, map.size());
        // delegate map should have all four entries
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey("k3"));
        assertTrue(map.containsKey("k4"));
    }

    // Test put with null key in size 2 triggers conversion? No, size 2 +1 =3 still flat, but when size=3 and adding again triggers delegate.
    @Test
    public void testPutNullKeyInSize2() {
        map.put("k1", "v1");
        map.put(null, "nullVal"); // size 2
        assertEquals(2, map.size());
        map.put("k3", "v3"); // size 3 still flat
        assertEquals(3, map.size());
        assertNull(map.put("k4", "v4")); // delegate
        assertEquals(4, map.size());
    }

    // Test containsKey with size 3 where one key is null and we look for non-null
    @Test
    public void testContainsKeyWithNullPresent() {
        map.put("k1", "v1");
        map.put(null, "nullVal");
        map.put("k3", "v3");
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("k2"));
    }

    // Test containsValue with null present
    @Test
    public void testContainsValueWithNullPresent() {
        map.put("k1", null);
        map.put("k2", "v2");
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v2"));
        assertFalse(map.containsValue("v1"));
    }

    // Test get with size 3, null key present, get non-null
    @Test
    public void testGetNonExistentInSize3WithNull() {
        map.put("k1", "v1");
        map.put(null, "nullVal");
        map.put("k3", "v3");
        assertNull(map.get("k2"));
    }

    // Test remove with size 2 and non-null key
    @Test
    public void testRemoveInSize2() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("v2", map.remove("k2")); // bug: returns value2? Actually for size2 non-null remove, if hash2 matches and key.equals(key2), then old=value2, and clears key2, size=1, returns value2. That's correct. So we'll test that.
        // Wait code: case 2: if (hash2 == hashCode && key.equals(key2)) { Object old = value2; ... size=1; return old; }
        // So it returns value2. So correct.
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k2"));
        assertTrue(map.containsKey("k1"));
    }

    // Test remove in size 2 with null key
    @Test
    public void testRemoveNullKeyInSize2() {
        map.put("k1", "v1");
        map.put(null, "nullVal");
        // null is key2? order: k1 then null => key1=k1, key2=null.
        assertEquals("nullVal", map.remove(null)); // key2==null so old=value2, return nullVal, clear key2, size=1.
        assertEquals(1, map.size());
        assertFalse(map.containsKey(null));
        assertEquals("v1", map.get("k1"));
    }

    // Test entrySet iterator with delegate map
    @Test
    public void testEntrySetDelegate() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        Set entrySet = map.entrySet();
        assertEquals(4, entrySet.size());
        Iterator it = entrySet.iterator();
        assertTrue(it.hasNext());
        Map.Entry e = (Map.Entry) it.next();
        assertTrue(e.getKey() instanceof String);
    }

    // Test keySet delegate
    @Test
    public void testKeySetDelegate() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        Set keySet = map.keySet();
        assertEquals(4, keySet.size());
        assertTrue(keySet.contains("k1"));
    }

    // Test values delegate
    @Test
    public void testValuesDelegate() {
        for (int i = 1; i <= 4; i++) map.put("k" + i, "v" + i);
        Collection values = map.values();
        assertEquals(4, values.size());
        assertTrue(values.contains("v1"));
    }

    // Further tests for FlatMapIterator reset and toString
    @Test
    public void testMapIteratorToStringBeforeNext() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        assertEquals("Iterator[]", it.toString());
    }

    @Test
    public void testMapIteratorToStringAfterNext() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        assertEquals("Iterator[k1=v1]", it.toString());
    }

    // Test entrySet iterator getKey/getValue/setValue on EntrySetIterator directly (it implements Map.Entry)
    @Test
    public void testEntrySetIteratorDirectEntryOperations() {
        map.put("k1", "v1");
        Iterator it = map.entrySet().iterator();
        EntrySetIterator entry = (EntrySetIterator) it.next();
        assertEquals("k1", entry.getKey());
        assertEquals("v1", entry.getValue());
        assertEquals("v1", entry.setValue("newV"));
        assertEquals("newV", map.get("k1"));
    }

    // Test remove via entrySet (Set.remove) on flat mode
    @Test
    public void testEntrySetRemoveByObject() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        Map.Entry toRemove = (Map.Entry) it.next();
        assertTrue(entrySet.remove(toRemove));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));
    }

    @Test
    public void testEntrySetRemoveNonExisting() {
        map.put("k1", "v1");
        Map.Entry nonExisting = new HashMap.SimpleEntry<>("k2", "v2");
        assertFalse(map.entrySet().remove(nonExisting));
    }

    @Test
    public void testEntrySetClear() {
        map.put("k1", "v1");
        Set entrySet = map.entrySet();
        entrySet.clear();
        assertTrue(map.isEmpty());
    }

    // keySet: test contains, remove, clear
    @Test
    public void testKeySetRemoveNotContained() {
        Set keySet = map.keySet();
        assertFalse(keySet.remove("any"));
    }

    // values: test contains, remove via iterator, clear
    @Test
    public void testValuesClear() {
        map.put("k1", "v1");
        map.values().clear();
        assertTrue(map.isEmpty());
    }
}