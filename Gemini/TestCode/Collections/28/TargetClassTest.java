package org.apache.commons.collections4.trie;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.trie.analyzer.StringKeyAnalyzer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

public class AbstractPatriciaTrieTest {

    private static class TestTrie<K, V> extends AbstractPatriciaTrie<K, V> {
        private static final long serialVersionUID = 1L;

        public TestTrie(final KeyAnalyzer<? super K> keyAnalyzer) {
            super(keyAnalyzer);
        }

        public TestTrie(final KeyAnalyzer<? super K> keyAnalyzer, final Map<? extends K, ? extends V> map) {
            super(keyAnalyzer, map);
        }
    }

    private TestTrie<String, String> trie;

    @Before
    public void setUp() {
        trie = new TestTrie<String, String>(StringKeyAnalyzer.INSTANCE);
    }

    @Test
    public void testConstructors() {
        final Map<String, String> src = new HashMap<String, String>();
        src.put("one", "1");
        src.put("two", "2");
        final TestTrie<String, String> copy = new TestTrie<String, String>(StringKeyAnalyzer.INSTANCE, src);
        Assert.assertEquals(2, copy.size());
        Assert.assertEquals("1", copy.get("one"));
        Assert.assertEquals("2", copy.get("two"));
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullKeyThrowsException() {
        trie.put(null, "value");
    }

    @Test
    public void testPutAndGetRootEmptyKey() {
        Assert.assertNull(trie.put("", "emptyKeyVal"));
        Assert.assertEquals(1, trie.size());
        Assert.assertEquals("emptyKeyVal", trie.get(""));
        Assert.assertTrue(trie.containsKey(""));

        // Replace empty key value
        Assert.assertEquals("emptyKeyVal", trie.put("", "replaced"));
        Assert.assertEquals(1, trie.size());
        Assert.assertEquals("replaced", trie.get(""));

        // Put another key
        Assert.assertNull(trie.put("alpha", "A"));
        Assert.assertEquals(2, trie.size());
        Assert.assertEquals("replaced", trie.get(""));
        Assert.assertEquals("A", trie.get("alpha"));
    }

    @Test
    public void testPutAndReplaceNormalKeys() {
        Assert.assertNull(trie.put("apple", "pie"));
        Assert.assertNull(trie.put("banana", "split"));
        Assert.assertNull(trie.put("app", "application"));
        Assert.assertEquals(3, trie.size());

        Assert.assertEquals("pie", trie.put("apple", "cider"));
        Assert.assertEquals(3, trie.size());
        Assert.assertEquals("cider", trie.get("apple"));
    }

    @Test
    public void testGetAndContainsKeyEdgeCases() {
        Assert.assertNull(trie.get(null));
        Assert.assertFalse(trie.containsKey(null));
        Assert.assertNull(trie.get("nonexistent"));
        Assert.assertFalse(trie.containsKey("nonexistent"));

        trie.put("hello", "world");
        Assert.assertTrue(trie.containsKey("hello"));
        Assert.assertFalse(trie.containsKey("hell"));
        Assert.assertNull(trie.get("hell"));
    }

    @Test
    public void testRemove() {
        Assert.assertNull(trie.remove(null));
        Assert.assertNull(trie.remove("nonexistent"));

        trie.put("", "rootVal");
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("ab", "3");
        trie.put("abc", "4");

        Assert.assertEquals(5, trie.size());

        // Remove root entry
        Assert.assertEquals("rootVal", trie.remove(""));
        Assert.assertEquals(4, trie.size());
        Assert.assertNull(trie.get(""));

        // Remove leaf node
        Assert.assertEquals("4", trie.remove("abc"));
        Assert.assertEquals(3, trie.size());
        Assert.assertNull(trie.get("abc"));

        // Remove internal nodes
        Assert.assertEquals("1", trie.remove("a"));
        Assert.assertEquals("3", trie.remove("ab"));
        Assert.assertEquals("2", trie.remove("b"));
        Assert.assertEquals(0, trie.size());
        Assert.assertTrue(trie.isEmpty());
    }

    @Test
    public void testClear() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("", "empty");
        Assert.assertEquals(3, trie.size());

        trie.clear();
        Assert.assertEquals(0, trie.size());
        Assert.assertTrue(trie.isEmpty());
        Assert.assertNull(trie.get("a"));
        Assert.assertNull(trie.get(""));
    }

    @Test
    public void testSelectMethods() {
        Assert.assertNull(trie.select("anything"));
        Assert.assertNull(trie.selectKey("anything"));
        Assert.assertNull(trie.selectValue("anything"));

        trie.put("H", "1");
        trie.put("L", "2");

        Map.Entry<String, String> selected = trie.select("D");
        Assert.assertNotNull(selected);
        Assert.assertEquals("L", trie.selectKey("D"));
        Assert.assertEquals("2", trie.selectValue("D"));
    }

    @Test
    public void testFirstLastNextPreviousKeys() {
        try {
            trie.firstKey();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ignored) {
        }

        try {
            trie.lastKey();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ignored) {
        }

        trie.put("b", "2");
        trie.put("a", "1");
        trie.put("c", "3");
        trie.put("d", "4");

        Assert.assertEquals("a", trie.firstKey());
        Assert.assertEquals("d", trie.lastKey());

        Assert.assertEquals("b", trie.nextKey("a"));
        Assert.assertEquals("c", trie.nextKey("b"));
        Assert.assertEquals("d", trie.nextKey("c"));
        Assert.assertNull(trie.nextKey("d"));
        Assert.assertNull(trie.nextKey("nonexistent"));

        Assert.assertEquals("c", trie.previousKey("d"));
        Assert.assertEquals("b", trie.previousKey("c"));
        Assert.assertEquals("a", trie.previousKey("b"));
        Assert.assertNull(trie.previousKey("a"));
        Assert.assertNull(trie.previousKey("nonexistent"));

        try {
            trie.nextKey(null);
            Assert.fail("Expected NPE");
        } catch (NullPointerException ignored) {
        }

        try {
            trie.previousKey(null);
            Assert.fail("Expected NPE");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void testCeilingFloorHigherLowerEntries() {
        // Empty trie
        Assert.assertNull(trie.ceilingEntry("a"));
        Assert.assertNull(trie.floorEntry("a"));
        Assert.assertNull(trie.higherEntry("a"));
        Assert.assertNull(trie.lowerEntry("a"));

        // With empty string key
        trie.put("", "root");
        Assert.assertNotNull(trie.ceilingEntry(""));
        Assert.assertEquals("", trie.ceilingEntry("").getKey());
        Assert.assertEquals("", trie.floorEntry("").getKey());
        Assert.assertNull(trie.higherEntry(""));
        Assert.assertNull(trie.lowerEntry(""));

        trie.put("b", "B");
        trie.put("d", "D");
        trie.put("f", "F");

        // Higher entry
        Assert.assertEquals("b", trie.higherEntry("").getKey());
        Assert.assertEquals("d", trie.higherEntry("b").getKey());
        Assert.assertEquals("d", trie.higherEntry("c").getKey());
        Assert.assertNull(trie.higherEntry("f"));

        // Ceiling entry
        Assert.assertEquals("b", trie.ceilingEntry("b").getKey());
        Assert.assertEquals("d", trie.ceilingEntry("c").getKey());
        Assert.assertEquals("f", trie.ceilingEntry("f").getKey());
        Assert.assertNull(trie.ceilingEntry("g"));

        // Lower entry
        Assert.assertEquals("d", trie.lowerEntry("f").getKey());
        Assert.assertEquals("d", trie.lowerEntry("e").getKey());
        Assert.assertEquals("b", trie.lowerEntry("d").getKey());
        Assert.assertEquals("", trie.lowerEntry("b").getKey());
        Assert.assertNull(trie.lowerEntry(""));

        // Floor entry
        Assert.assertEquals("f", trie.floorEntry("f").getKey());
        Assert.assertEquals("d", trie.floorEntry("e").getKey());
        Assert.assertEquals("b", trie.floorEntry("b").getKey());
        Assert.assertEquals("", trie.floorEntry("a").getKey());
        Assert.assertEquals("", trie.floorEntry("").getKey());
    }

    @Test
    public void testEntrySetAndIterator() {
        trie.put("a", "1");
        trie.put("b", "2");

        Set<Map.Entry<String, String>> entries = trie.entrySet();
        Assert.assertEquals(2, entries.size());

        Map.Entry<String, String> entryA = trie.getEntry("a");
        Assert.assertNotNull(entryA);
        Assert.assertTrue(entries.contains(entryA));
        Assert.assertFalse(entries.contains("not an entry"));
        Assert.assertFalse(entries.contains(new AbstractMap.SimpleEntry<String, String>("a", "wrong")));

        Iterator<Map.Entry<String, String>> it = entries.iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> first = it.next();
        Assert.assertEquals("a", first.getKey());

        it.remove();
        Assert.assertEquals(1, trie.size());
        Assert.assertNull(trie.get("a"));

        Assert.assertTrue(entries.remove(trie.getEntry("b")));
        Assert.assertEquals(0, trie.size());
        Assert.assertFalse(entries.remove("not an entry"));

        trie.put("c", "3");
        entries.clear();
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testKeySetAndValues() {
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        Set<String> keys = trie.keySet();
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("k1"));
        Assert.assertTrue(keys.remove("k1"));
        Assert.assertEquals(1, trie.size());

        Iterator<String> keyIt = keys.iterator();
        Assert.assertEquals("k2", keyIt.next());
        try {
            keyIt.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ignored) {
        }

        trie.put("k3", "v3");
        Assert.assertTrue(trie.values().contains("v3"));
        Assert.assertFalse(trie.values().contains("nonexistent"));
        Assert.assertTrue(trie.values().remove("v3"));
        Assert.assertFalse(trie.values().remove("nonexistent"));

        trie.values().clear();
        Assert.assertEquals(0, trie.size());
    }

    @Test
    public void testMapIterator() {
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        OrderedMapIterator<String, String> it = trie.mapIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertFalse(it.hasPrevious());

        try {
            it.getKey();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ignored) {
        }

        Assert.assertEquals("k1", it.next());
        Assert.assertEquals("k1", it.getKey());
        Assert.assertEquals("v1", it.getValue());
        Assert.assertTrue(it.hasPrevious());

        it.setValue("v1_mod");
        Assert.assertEquals("v1_mod", trie.get("k1"));

        Assert.assertEquals("k2", it.next());
        Assert.assertEquals("k2", it.previous());
        Assert.assertEquals("k1", it.previous());

        try {
            it.previous();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationFailFast() {
        trie.put("a", "1");
        trie.put("b", "2");

        Iterator<String> it = trie.keySet().iterator();
        it.next();
        trie.put("c", "3");
        it.next();
    }

    @Test
    public void testSubMapHeadMapTailMap() {
        trie.put("apple", "1");
        trie.put("banana", "2");
        trie.put("cherry", "3");
        trie.put("date", "4");

        SortedMap<String, String> sub = trie.subMap("banana", "date");
        Assert.assertEquals(2, sub.size());
        Assert.assertTrue(sub.containsKey("banana"));
        Assert.assertTrue(sub.containsKey("cherry"));
        Assert.assertFalse(sub.containsKey("apple"));
        Assert.assertFalse(sub.containsKey("date"));

        Assert.assertEquals("banana", sub.firstKey());
        Assert.assertEquals("cherry", sub.lastKey());

        // subMap out of range checks
        Assert.assertNull(sub.get("apple"));
        Assert.assertNull(sub.remove("apple"));
        try {
            sub.put("apple", "err");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }

        SortedMap<String, String> head = trie.headMap("cherry");
        Assert.assertEquals(2, head.size());
        Assert.assertEquals("apple", head.firstKey());
        Assert.assertEquals("banana", head.lastKey());

        SortedMap<String, String> tail = trie.tailMap("cherry");
        Assert.assertEquals(2, tail.size());
        Assert.assertEquals("cherry", tail.firstKey());
        Assert.assertEquals("date", tail.lastKey());

        // Range entry set operations
        Set<Map.Entry<String, String>> subEntries = sub.entrySet();
        Assert.assertFalse(subEntries.isEmpty());
        Assert.assertTrue(subEntries.contains(trie.getEntry("banana")));
        Assert.assertFalse(subEntries.contains(trie.getEntry("apple")));

        Assert.assertTrue(subEntries.remove(trie.getEntry("banana")));
        Assert.assertFalse(trie.containsKey("banana"));
    }

    @Test
    public void testRangeMapInvalidArguments() {
        try {
            trie.subMap("z", "a");
            Assert.fail("Expected IllegalArgumentException for fromKey > toKey");
        } catch (IllegalArgumentException ignored) {
        }

        SortedMap<String, String> sub = trie.subMap("b", "m");
        try {
            sub.subMap("a", "k");
            Assert.fail("Expected IllegalArgumentException for fromKey out of bounds");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            sub.headMap("z");
            Assert.fail("Expected IllegalArgumentException for toKey out of bounds");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            sub.tailMap("a");
            Assert.fail("Expected IllegalArgumentException for fromKey out of bounds");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testPrefixMap() {
        trie.put("car", "1");
        trie.put("cart", "2");
        trie.put("cat", "3");
        trie.put("dog", "4");

        SortedMap<String, String> prefixMap = trie.prefixMap("ca");
        Assert.assertEquals(3, prefixMap.size());
        Assert.assertEquals("car", prefixMap.firstKey());
        Assert.assertEquals("cat", prefixMap.lastKey());
        Assert.assertTrue(prefixMap.containsKey("cart"));
        Assert.assertFalse(prefixMap.containsKey("dog"));

        Assert.assertEquals("1", prefixMap.get("car"));
        Assert.assertNull(prefixMap.get("dog"));

        // Exact full prefix
        SortedMap<String, String> wholeTrie = trie.prefixMap("");
        Assert.assertSame(trie, wholeTrie);

        // Subtree remove through iterator
        Iterator<Map.Entry<String, String>> pIt = prefixMap.entrySet().iterator();
        while (pIt.hasNext()) {
            Map.Entry<String, String> e = pIt.next();
            if (e.getKey().equals("cart")) {
                pIt.remove();
            }
        }
        Assert.assertEquals(2, prefixMap.size());
        Assert.assertFalse(trie.containsKey("cart"));

        // Non-matching prefix
        SortedMap<String, String> emptyPrefix = trie.prefixMap("z");
        Assert.assertEquals(0, emptyPrefix.size());
        try {
            emptyPrefix.firstKey();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void testTrieEntryToStringAndProperties() {
        AbstractPatriciaTrie.TrieEntry<String, String> root =
                new AbstractPatriciaTrie.TrieEntry<String, String>(null, null, -1);
        Assert.assertTrue(root.isEmpty());
        Assert.assertFalse(root.isInternalNode());
        Assert.assertTrue(root.isExternalNode());
        Assert.assertTrue(root.toString().contains("RootEntry"));

        AbstractPatriciaTrie.TrieEntry<String, String> entry =
                new AbstractPatriciaTrie.TrieEntry<String, String>("key", "val", 10);
        Assert.assertFalse(entry.isEmpty());
        Assert.assertTrue(entry.toString().contains("Entry("));
        Assert.assertTrue(entry.toString().contains("key=key [10]"));
    }

    @Test
    public void testSerialization() throws Exception {
        trie.put("alpha", "1");
        trie.put("beta", "2");
        trie.put("gamma", "3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trie);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        TestTrie<String, String> deserialized = (TestTrie<String, String>) ois.readObject();
        ois.close();

        Assert.assertEquals(3, deserialized.size());
        Assert.assertEquals("1", deserialized.get("alpha"));
        Assert.assertEquals("2", deserialized.get("beta"));
        Assert.assertEquals("3", deserialized.get("gamma"));
    }

    @Test
    public void testComparator() {
        Assert.assertNotNull(trie.comparator());
        Assert.assertSame(trie.getKeyAnalyzer(), trie.comparator());
    }
}
