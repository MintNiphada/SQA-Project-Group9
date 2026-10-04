package org.apache.commons.collections4.trie;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractPatriciaTrieTest {

    private PatriciaTrie<String> trie;

    @Before
    public void setUp() {
        trie = new PatriciaTrie<String>();
    }

    @Test
    public void testPutAndGet() {
        Assert.assertNull(trie.put("key1", "value1"));
        Assert.assertEquals("value1", trie.get("key1"));
        Assert.assertEquals(1, trie.size());
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullKey() {
        trie.put(null, "value");
    }

    @Test
    public void testPutZeroLengthKey() {
        trie.put("", "rootValue");
        Assert.assertEquals("rootValue", trie.get(""));
        Assert.assertEquals(1, trie.size());
    }

    @Test
    public void testPutExistingKey() {
        trie.put("key", "old");
        Assert.assertEquals("old", trie.put("key", "new"));
        Assert.assertEquals("new", trie.get("key"));
        Assert.assertEquals(1, trie.size());
    }

    @Test
    public void testPutMultiple() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        Assert.assertEquals(3, trie.size());
        Assert.assertEquals("1", trie.get("a"));
        Assert.assertEquals("2", trie.get("b"));
        Assert.assertEquals("3", trie.get("c"));
    }

    @Test
    public void testGetNonExistent() {
        Assert.assertNull(trie.get("nonexistent"));
    }

    @Test
    public void testGetNull() {
        Assert.assertNull(trie.get(null));
    }

    @Test
    public void testContainsKey() {
        trie.put("key", "value");
        Assert.assertTrue(trie.containsKey("key"));
        Assert.assertFalse(trie.containsKey("other"));
        Assert.assertFalse(trie.containsKey(null));
    }

    @Test
    public void testRemove() {
        trie.put("key", "value");
        Assert.assertEquals("value", trie.remove("key"));
        Assert.assertNull(trie.get("key"));
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testRemoveNonExistent() {
        Assert.assertNull(trie.remove("key"));
    }

    @Test
    public void testRemoveNull() {
        Assert.assertNull(trie.remove(null));
    }

    @Test
    public void testClear() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.clear();
        Assert.assertEquals(0, trie.size());
        Assert.assertNull(trie.get("a"));
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, trie.size());
        trie.put("a", "1");
        Assert.assertEquals(1, trie.size());
        trie.put("b", "2");
        Assert.assertEquals(2, trie.size());
        trie.remove("a");
        Assert.assertEquals(1, trie.size());
    }

    @Test
    public void testEntrySet() {
        trie.put("a", "1");
        trie.put("b", "2");
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        Assert.assertEquals(2, entries.size());
        Assert.assertTrue(entries.contains(new AbstractMap.SimpleEntry<String, String>("a", "1")));
        Assert.assertFalse(entries.contains(new AbstractMap.SimpleEntry<String, String>("c", "3")));
        Iterator<Map.Entry<String, String>> it = entries.iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("a", entry.getKey());
        Assert.assertEquals("1", entry.getValue());
        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testKeySet() {
        trie.put("a", "1");
        trie.put("b", "2");
        Set<String> keys = trie.keySet();
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("a"));
        Assert.assertFalse(keys.contains("c"));
        Iterator<String> it = keys.iterator();
        Assert.assertTrue(it.hasNext());
        String key = it.next();
        Assert.assertEquals("a", key);
        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testValues() {
        trie.put("a", "1");
        trie.put("b", "2");
        java.util.Collection<String> values = trie.values();
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.contains("1"));
        Assert.assertFalse(values.contains("3"));
        Iterator<String> it = values.iterator();
        Assert.assertTrue(it.hasNext());
        String value = it.next();
        Assert.assertEquals("1", value);
        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testSelect() {
        trie.put("ab", "value1");
        trie.put("ac", "value2");
        Map.Entry<String, String> entry = trie.select("aa");
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getKey().equals("ab") || entry.getKey().equals("ac"));
    }

    @Test
    public void testSelectKey() {
        trie.put("ab", "value1");
        String key = trie.selectKey("aa");
        Assert.assertNotNull(key);
        Assert.assertTrue(key.equals("ab") || key.equals("ac"));
    }

    @Test
    public void testSelectValue() {
        trie.put("ab", "value1");
        String value = trie.selectValue("aa");
        Assert.assertNotNull(value);
        Assert.assertTrue(value.equals("value1") || value.equals("value2"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFirstKeyEmpty() {
        trie.firstKey();
    }

    @Test
    public void testFirstKey() {
        trie.put("b", "2");
        trie.put("a", "1");
        Assert.assertEquals("a", trie.firstKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testLastKeyEmpty() {
        trie.lastKey();
    }

    @Test
    public void testLastKey() {
        trie.put("a", "1");
        trie.put("b", "2");
        Assert.assertEquals("b", trie.lastKey());
    }

    @Test(expected = NullPointerException.class)
    public void testNextKeyNull() {
        trie.nextKey(null);
    }

    @Test
    public void testNextKey() {
        trie.put("a", "1");
        trie.put("b", "2");
        Assert.assertEquals("b", trie.nextKey("a"));
        Assert.assertNull(trie.nextKey("b"));
        Assert.assertNull(trie.nextKey("c"));
    }

    @Test(expected = NullPointerException.class)
    public void testPreviousKeyNull() {
        trie.previousKey(null);
    }

    @Test
    public void testPreviousKey() {
        trie.put("a", "1");
        trie.put("b", "2");
        Assert.assertEquals("a", trie.previousKey("b"));
        Assert.assertNull(trie.previousKey("a"));
        Assert.assertNull(trie.previousKey("c"));
    }

    @Test
    public void testMapIterator() {
        trie.put("a", "1");
        trie.put("b", "2");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
        Assert.assertEquals("a", it.getKey());
        Assert.assertEquals("1", it.getValue());
        Assert.assertEquals("1", it.setValue("new1"));
        Assert.assertEquals("new1", trie.get("a"));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("b", it.next());
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("a", it.previous());
        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testPrefixMap() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        trie.put("xyz", "3");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertEquals(2, prefixMap.size());
        Assert.assertEquals("abc", prefixMap.firstKey());
        Assert.assertEquals("abd", prefixMap.lastKey());
        Assert.assertTrue(prefixMap.containsKey("abc"));
        Assert.assertFalse(prefixMap.containsKey("xyz"));
        Assert.assertEquals("1", prefixMap.get("abc"));
        Assert.assertNull(prefixMap.get("xyz"));
        prefixMap.put("abe", "4");
        Assert.assertEquals(3, prefixMap.size());
        Assert.assertEquals("4", trie.get("abe"));
        prefixMap.remove("abc");
        Assert.assertFalse(trie.containsKey("abc"));
    }

    @Test
    public void testHeadMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> headMap = trie.headMap("c");
        Assert.assertEquals(2, headMap.size());
        Assert.assertEquals("a", headMap.firstKey());
        Assert.assertEquals("b", headMap.lastKey());
        Assert.assertTrue(headMap.containsKey("a"));
        Assert.assertFalse(headMap.containsKey("c"));
    }

    @Test
    public void testSubMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertEquals(2, subMap.size());
        Assert.assertEquals("a", subMap.firstKey());
        Assert.assertEquals("b", subMap.lastKey());
        Assert.assertTrue(subMap.containsKey("a"));
        Assert.assertFalse(subMap.containsKey("c"));
    }

    @Test
    public void testTailMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> tailMap = trie.tailMap("b");
        Assert.assertEquals(2, tailMap.size());
        Assert.assertEquals("b", tailMap.firstKey());
        Assert.assertEquals("c", tailMap.lastKey());
        Assert.assertTrue(tailMap.containsKey("b"));
        Assert.assertFalse(tailMap.containsKey("a"));
    }

    @Test
    public void testHigherEntry() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        Assert.assertEquals("b", trie.higherEntry("a").getKey());
        Assert.assertEquals("c", trie.higherEntry("b").getKey());
        Assert.assertNull(trie.higherEntry("c"));
        Assert.assertEquals("a", trie.higherEntry("").getKey());
    }

    @Test
    public void testCeilingEntry() {
        trie.put("a", "1");
        trie.put("c", "3");
        Assert.assertEquals("a", trie.ceilingEntry("a").getKey());
        Assert.assertEquals("c", trie.ceilingEntry("b").getKey());
        Assert.assertNull(trie.ceilingEntry("d"));
        Assert.assertEquals("a", trie.ceilingEntry("").getKey());
    }

    @Test
    public void testLowerEntry() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        Assert.assertEquals("a", trie.lowerEntry("b").getKey());
        Assert.assertEquals("b", trie.lowerEntry("c").getKey());
        Assert.assertNull(trie.lowerEntry("a"));
        Assert.assertNull(trie.lowerEntry(""));
    }

    @Test
    public void testFloorEntry() {
        trie.put("a", "1");
        trie.put("c", "3");
        Assert.assertEquals("a", trie.floorEntry("a").getKey());
        Assert.assertEquals("a", trie.floorEntry("b").getKey());
        Assert.assertEquals("c", trie.floorEntry("c").getKey());
        Assert.assertNull(trie.floorEntry(""));
    }

    @Test
    public void testIteratorRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        Iterator<String> it = trie.keySet().iterator();
        it.next();
        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModification() {
        trie.put("a", "1");
        Iterator<String> it = trie.keySet().iterator();
        trie.put("b", "2");
        it.next();
    }

    @Test
    public void testEmptyTrieOperations() {
        Assert.assertNull(trie.select("any"));
        Assert.assertNull(trie.selectKey("any"));
        Assert.assertNull(trie.selectValue("any"));
        Assert.assertNull(trie.higherEntry("any"));
        Assert.assertNull(trie.ceilingEntry("any"));
        Assert.assertNull(trie.lowerEntry("any"));
        Assert.assertNull(trie.floorEntry("any"));
    }

    @Test
    public void testRootHandling() {
        trie.put("", "root");
        Assert.assertEquals("root", trie.get(""));
        Assert.assertEquals(1, trie.size());
        trie.put("", "newRoot");
        Assert.assertEquals("newRoot", trie.get(""));
        Assert.assertEquals(1, trie.size());
        trie.remove("");
        Assert.assertNull(trie.get(""));
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testComplexPutAndRemove() {
        trie.put("romane", "1");
        trie.put("romanus", "2");
        trie.put("romulus", "3");
        trie.put("rubens", "4");
        trie.put("ruber", "5");
        trie.put("rubicon", "6");
        trie.put("rubicundus", "7");
        Assert.assertEquals(7, trie.size());
        Assert.assertEquals("1", trie.get("romane"));
        trie.remove("romane");
        Assert.assertNull(trie.get("romane"));
        Assert.assertEquals(6, trie.size());
        trie.remove("romanus");
        trie.remove("romulus");
        trie.remove("rubens");
        trie.remove("ruber");
        trie.remove("rubicon");
        trie.remove("rubicundus");
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testEntrySetRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        Assert.assertTrue(entries.remove(new AbstractMap.SimpleEntry<String, String>("a", "1")));
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
        Assert.assertFalse(entries.remove(new AbstractMap.SimpleEntry<String, String>("a", "1")));
    }

    @Test
    public void testValuesRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        java.util.Collection<String> values = trie.values();
        Assert.assertTrue(values.remove("1"));
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
        Assert.assertFalse(values.remove("1"));
    }

    @Test
    public void testKeySetRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        Set<String> keys = trie.keySet();
        Assert.assertTrue(keys.remove("a"));
        Assert.assertEquals(1, trie.size());
        Assert.assertFalse(trie.containsKey("a"));
        Assert.assertFalse(keys.remove("a"));
    }

    @Test
    public void testPrefixMapFirstKeyLastKey() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        trie.put("abe", "3");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertEquals("abc", prefixMap.firstKey());
        Assert.assertEquals("abe", prefixMap.lastKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testPrefixMapFirstKeyEmpty() {
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        prefixMap.firstKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testPrefixMapLastKeyEmpty() {
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        prefixMap.lastKey();
    }

    @Test
    public void testRangeMapFirstKeyLastKey() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertEquals("a", subMap.firstKey());
        Assert.assertEquals("b", subMap.lastKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRangeMapFirstKeyEmpty() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("b", "c");
        subMap.firstKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRangeMapLastKeyEmpty() {
        trie.put("c", "3");
        SortedMap<String, String> subMap = trie.subMap("a", "b");
        subMap.lastKey();
    }

    @Test
    public void testMapIteratorPrevious() {
        trie.put("a", "1");
        trie.put("b", "2");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.next();
        it.next();
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("a", it.previous());
        Assert.assertEquals("a", it.getKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testMapIteratorPreviousWhenNone() {
        trie.put("a", "1");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.previous();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorSetValueBeforeNext() {
        trie.put("a", "1");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.setValue("new");
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetKeyBeforeNext() {
        trie.put("a", "1");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.getKey();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIteratorGetValueBeforeNext() {
        trie.put("a", "1");
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.getValue();
    }

    @Test
    public void testEntrySetContainsNonEntry() {
        trie.put("a", "1");
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        Assert.assertFalse(entries.contains("not an entry"));
    }

    @Test
    public void testEntrySetRemoveNonEntry() {
        trie.put("a", "1");
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        Assert.assertFalse(entries.remove("not an entry"));
    }

    @Test
    public void testRangeMapPutOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        try {
            subMap.put("d", "4");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testRangeMapGetOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertNull(subMap.get("d"));
    }

    @Test
    public void testRangeMapRemoveOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertNull(subMap.remove("d"));
    }

    @Test
    public void testRangeMapContainsKeyOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertFalse(subMap.containsKey("d"));
    }

    @Test
    public void testRangeMapSubMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        SortedMap<String, String> subSubMap = subMap.subMap("a", "b");
        Assert.assertEquals(1, subSubMap.size());
        Assert.assertEquals("a", subSubMap.firstKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeMapSubMapOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        subMap.subMap("d", "e");
    }

    @Test
    public void testRangeMapHeadMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        SortedMap<String, String> headMap = subMap.headMap("b");
        Assert.assertEquals(1, headMap.size());
        Assert.assertEquals("a", headMap.firstKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeMapHeadMapOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        subMap.headMap("d");
    }

    @Test
    public void testRangeMapTailMap() {
        trie.put("a", "1");
        trie.put("b", "2");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        SortedMap<String, String> tailMap = subMap.tailMap("b");
        Assert.assertEquals(1, tailMap.size());
        Assert.assertEquals("b", tailMap.firstKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeMapTailMapOutOfRange() {
        trie.put("a", "1");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        subMap.tailMap("d");
    }

    @Test
    public void testPrefixMapSubMap() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        trie.put("abe", "3");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        SortedMap<String, String> subMap = prefixMap.subMap("abc", "abe");
        Assert.assertEquals(2, subMap.size());
        Assert.assertEquals("abc", subMap.firstKey());
        Assert.assertEquals("abd", subMap.lastKey());
    }

    @Test
    public void testPrefixMapHeadMap() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        SortedMap<String, String> headMap = prefixMap.headMap("abd");
        Assert.assertEquals(1, headMap.size());
        Assert.assertEquals("abc", headMap.firstKey());
    }

    @Test
    public void testPrefixMapTailMap() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        SortedMap<String, String> tailMap = prefixMap.tailMap("abd");
        Assert.assertEquals(1, tailMap.size());
        Assert.assertEquals("abd", tailMap.firstKey());
    }

    @Test
    public void testHigherEntryWithZeroLengthKey() {
        trie.put("a", "1");
        trie.put("b", "2");
        Assert.assertEquals("a", trie.higherEntry("").getKey());
    }

    @Test
    public void testCeilingEntryWithZeroLengthKey() {
        trie.put("a", "1");
        Assert.assertEquals("a", trie.ceilingEntry("").getKey());
    }

    @Test
    public void testLowerEntryWithZeroLengthKey() {
        trie.put("a", "1");
        Assert.assertNull(trie.lowerEntry(""));
    }

    @Test
    public void testFloorEntryWithZeroLengthKey() {
        trie.put("a", "1");
        Assert.assertNull(trie.floorEntry(""));
    }

    @Test
    public void testSelectOnEmptyTrie() {
        Assert.assertNull(trie.select("any"));
    }

    @Test
    public void testSelectKeyOnEmptyTrie() {
        Assert.assertNull(trie.selectKey("any"));
    }

    @Test
    public void testSelectValueOnEmptyTrie() {
        Assert.assertNull(trie.selectValue("any"));
    }

    @Test
    public void testNextKeyOnEmptyTrie() {
        Assert.assertNull(trie.nextKey("any"));
    }

    @Test
    public void testPreviousKeyOnEmptyTrie() {
        Assert.assertNull(trie.previousKey("any"));
    }

    @Test
    public void testMapIteratorOnEmptyTrie() {
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testMapIteratorNextOnEmptyTrie() {
        org.apache.commons.collections4.OrderedMapIterator<String, String> it = trie.mapIterator();
        it.next();
    }

    @Test
    public void testEntrySetIteratorRemoveTwice() {
        trie.put("a", "1");
        Iterator<Map.Entry<String, String>> it = trie.entrySet().iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testKeySetIteratorRemoveTwice() {
        trie.put("a", "1");
        Iterator<String> it = trie.keySet().iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testValuesIteratorRemoveTwice() {
        trie.put("a", "1");
        Iterator<String> it = trie.values().iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testPrefixMapIteratorRemove() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        it.next();
        it.remove();
        Assert.assertEquals(1, prefixMap.size());
        Assert.assertFalse(trie.containsKey("abc"));
    }

    @Test
    public void testRangeMapIteratorRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Iterator<Map.Entry<String, String>> it = subMap.entrySet().iterator();
        it.next();
        it.remove();
        Assert.assertEquals(1, subMap.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testPrefixRangeEntrySetSingletonIterator() {
        trie.put("a", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("a");
        Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("a", entry.getKey());
        Assert.assertFalse(it.hasNext());
        it.remove();
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testPrefixRangeEntrySetSingletonIteratorRemoveTwice() {
        trie.put("a", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("a");
        Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        it.next();
        it.remove();
        try {
            it.remove();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testPrefixRangeEntrySetSingletonIteratorNextAfterRemove() {
        trie.put("a", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("a");
        Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        it.next();
        it.remove();
        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void testRangeEntrySetSize() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertEquals(2, subMap.size());
    }

    @Test
    public void testRangeEntrySetIsEmpty() {
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertTrue(subMap.isEmpty());
        trie.put("a", "1");
        Assert.assertFalse(subMap.isEmpty());
    }

    @Test
    public void testRangeEntrySetContains() {
        trie.put("a", "1");
        trie.put("b", "2");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertTrue(subMap.entrySet().contains(new AbstractMap.SimpleEntry<String, String>("a", "1")));
        Assert.assertFalse(subMap.entrySet().contains(new AbstractMap.SimpleEntry<String, String>("c", "3")));
    }

    @Test
    public void testRangeEntrySetRemove() {
        trie.put("a", "1");
        trie.put("b", "2");
        SortedMap<String, String> subMap = trie.subMap("a", "c");
        Assert.assertTrue(subMap.entrySet().remove(new AbstractMap.SimpleEntry<String, String>("a", "1")));
        Assert.assertEquals(1, subMap.size());
        Assert.assertFalse(trie.containsKey("a"));
    }

    @Test
    public void testPrefixRangeEntrySetSize() {
        trie.put("abc", "1");
        trie.put("abd", "2");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertEquals(2, prefixMap.size());
    }

    @Test
    public void testPrefixRangeEntrySetIsEmpty() {
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertTrue(prefixMap.isEmpty());
        trie.put("abc", "1");
        Assert.assertFalse(prefixMap.isEmpty());
    }

    @Test
    public void testPrefixRangeEntrySetContains() {
        trie.put("abc", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertTrue(prefixMap.entrySet().contains(new AbstractMap.SimpleEntry<String, String>("abc", "1")));
        Assert.assertFalse(prefixMap.entrySet().contains(new AbstractMap.SimpleEntry<String, String>("abd", "2")));
    }

    @Test
    public void testPrefixRangeEntrySetRemove() {
        trie.put("abc", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        Assert.assertTrue(prefixMap.entrySet().remove(new AbstractMap.SimpleEntry<String, String>("abc", "1")));
        Assert.assertEquals(0, prefixMap.size());
        Assert.assertFalse(trie.containsKey("abc"));
    }

    @Test
    public void testModCountIncrementOnPut() {
        int modCount = trie.modCount;
        trie.put("a", "1");
        Assert.assertTrue(trie.modCount > modCount);
    }

    @Test
    public void testModCountIncrementOnRemove() {
        trie.put("a", "1");
        int modCount = trie.modCount;
        trie.remove("a");
        Assert.assertTrue(trie.modCount > modCount);
    }

    @Test
    public void testModCountIncrementOnClear() {
        trie.put("a", "1");
        int modCount = trie.modCount;
        trie.clear();
        Assert.assertTrue(trie.modCount > modCount);
    }

    @Test
    public void testComparator() {
        Assert.assertNotNull(trie.comparator());
    }

    @Test
    public void testPutAll() {
        java.util.Map<String, String> map = new java.util.HashMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        trie.putAll(map);
        Assert.assertEquals(2, trie.size());
        Assert.assertEquals("1", trie.get("a"));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(trie.isEmpty());
        trie.put("a", "1");
        Assert.assertFalse(trie.isEmpty());
    }

    @Test
    public void testContainsValue() {
        trie.put("a", "1");
        Assert.assertTrue(trie.containsValue("1"));
        Assert.assertFalse(trie.containsValue("2"));
    }

    @Test
    public void testEqualsAndHashCode() {
        PatriciaTrie<String> other = new PatriciaTrie<String>();
        other.put("a", "1");
        trie.put("a", "1");
        Assert.assertTrue(trie.equals(other));
        Assert.assertEquals(trie.hashCode(), other.hashCode());
    }

    @Test
    public void testToString() {
        trie.put("a", "1");
        String str = trie.toString();
        Assert.assertTrue(str.contains("a"));
    }

    @Test
    public void testSerialization() throws Exception {
        trie.put("a", "1");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(trie);
        oos.close();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        PatriciaTrie<String> deserialized = (PatriciaTrie<String>) ois.readObject();
        Assert.assertEquals(trie, deserialized);
    }
}
