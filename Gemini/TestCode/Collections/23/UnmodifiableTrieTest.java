package org.apache.commons.collections4.trie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.Unmodifiable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link UnmodifiableTrie}.
 */
public class UnmodifiableTrieTest {

    private Trie<String, Integer> originalTrie;
    private Trie<String, Integer> unmodifiableTrie;

    @Before
    public void setUp() {
        originalTrie = new PatriciaTrie<Integer>();
        originalTrie.put("apple", 1);
        originalTrie.put("app", 2);
        originalTrie.put("banana", 3);
        originalTrie.put("band", 4);
        originalTrie.put("cat", 5);

        unmodifiableTrie = UnmodifiableTrie.unmodifiableTrie(originalTrie);
    }

    @Test
    public void testFactoryAndConstructor() {
        Assert.assertNotNull(unmodifiableTrie);
        Assert.assertTrue(unmodifiableTrie instanceof Unmodifiable);
        Assert.assertTrue(unmodifiableTrie instanceof UnmodifiableTrie);

        UnmodifiableTrie<String, Integer> directInstance = new UnmodifiableTrie<String, Integer>(originalTrie);
        Assert.assertNotNull(directInstance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new UnmodifiableTrie<String, Integer>(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryNull() {
        UnmodifiableTrie.unmodifiableTrie(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        unmodifiableTrie.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        unmodifiableTrie.put("dog", 6);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("dog", 6);
        unmodifiableTrie.putAll(map);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        unmodifiableTrie.remove("apple");
    }

    @Test
    public void testContainsKey() {
        Assert.assertTrue(unmodifiableTrie.containsKey("apple"));
        Assert.assertTrue(unmodifiableTrie.containsKey("app"));
        Assert.assertFalse(unmodifiableTrie.containsKey("dog"));
        Assert.assertFalse(unmodifiableTrie.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        Assert.assertTrue(unmodifiableTrie.containsValue(1));
        Assert.assertTrue(unmodifiableTrie.containsValue(5));
        Assert.assertFalse(unmodifiableTrie.containsValue(100));
        Assert.assertFalse(unmodifiableTrie.containsValue(null));
    }

    @Test
    public void testGet() {
        Assert.assertEquals(Integer.valueOf(1), unmodifiableTrie.get("apple"));
        Assert.assertEquals(Integer.valueOf(2), unmodifiableTrie.get("app"));
        Assert.assertNull(unmodifiableTrie.get("dog"));
        Assert.assertNull(unmodifiableTrie.get(null));
    }

    @Test
    public void testIsEmptyAndSize() {
        Assert.assertFalse(unmodifiableTrie.isEmpty());
        Assert.assertEquals(5, unmodifiableTrie.size());

        Trie<String, Integer> emptyTrie = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<Integer>());
        Assert.assertTrue(emptyTrie.isEmpty());
        Assert.assertEquals(0, emptyTrie.size());
    }

    @Test
    public void testFirstAndLastKey() {
        Assert.assertEquals("app", unmodifiableTrie.firstKey());
        Assert.assertEquals("cat", unmodifiableTrie.lastKey());
    }

    @Test
    public void testComparator() {
        Assert.assertNull(unmodifiableTrie.comparator());
    }

    @Test
    public void testKeySet() {
        Set<String> keys = unmodifiableTrie.keySet();
        Assert.assertEquals(5, keys.size());
        Assert.assertTrue(keys.contains("apple"));

        try {
            keys.remove("apple");
            Assert.fail("Expected UnsupportedOperationException on keySet remove");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            keys.clear();
            Assert.fail("Expected UnsupportedOperationException on keySet clear");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testValues() {
        Collection<Integer> vals = unmodifiableTrie.values();
        Assert.assertEquals(5, vals.size());
        Assert.assertTrue(vals.contains(1));

        try {
            vals.remove(1);
            Assert.fail("Expected UnsupportedOperationException on values remove");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            vals.clear();
            Assert.fail("Expected UnsupportedOperationException on values clear");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testEntrySet() {
        Set<Entry<String, Integer>> entries = unmodifiableTrie.entrySet();
        Assert.assertEquals(5, entries.size());

        Iterator<Entry<String, Integer>> it = entries.iterator();
        Assert.assertTrue(it.hasNext());
        Entry<String, Integer> entry = it.next();

        try {
            entry.setValue(999);
            Assert.fail("Expected UnsupportedOperationException on Entry.setValue");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            it.remove();
            Assert.fail("Expected UnsupportedOperationException on entrySet iterator remove");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            entries.clear();
            Assert.fail("Expected UnsupportedOperationException on entrySet clear");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testHeadMap() {
        SortedMap<String, Integer> headMap = unmodifiableTrie.headMap("banana");
        Assert.assertEquals(2, headMap.size());
        Assert.assertTrue(headMap.containsKey("app"));
        Assert.assertTrue(headMap.containsKey("apple"));
        Assert.assertFalse(headMap.containsKey("banana"));

        try {
            headMap.put("a", 0);
            Assert.fail("Expected UnsupportedOperationException on headMap put");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            headMap.remove("app");
            Assert.fail("Expected UnsupportedOperationException on headMap remove");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testTailMap() {
        SortedMap<String, Integer> tailMap = unmodifiableTrie.tailMap("banana");
        Assert.assertEquals(3, tailMap.size());
        Assert.assertTrue(tailMap.containsKey("banana"));
        Assert.assertTrue(tailMap.containsKey("band"));
        Assert.assertTrue(tailMap.containsKey("cat"));

        try {
            tailMap.put("dog", 10);
            Assert.fail("Expected UnsupportedOperationException on tailMap put");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            tailMap.remove("cat");
            Assert.fail("Expected UnsupportedOperationException on tailMap remove");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testSubMap() {
        SortedMap<String, Integer> subMap = unmodifiableTrie.subMap("apple", "cat");
        Assert.assertEquals(3, subMap.size());
        Assert.assertTrue(subMap.containsKey("apple"));
        Assert.assertTrue(subMap.containsKey("banana"));
        Assert.assertTrue(subMap.containsKey("band"));
        Assert.assertFalse(subMap.containsKey("app"));
        Assert.assertFalse(subMap.containsKey("cat"));

        try {
            subMap.put("ball", 10);
            Assert.fail("Expected UnsupportedOperationException on subMap put");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            subMap.remove("apple");
            Assert.fail("Expected UnsupportedOperationException on subMap remove");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testPrefixMap() {
        SortedMap<String, Integer> prefixMap = unmodifiableTrie.prefixMap("ban");
        Assert.assertEquals(2, prefixMap.size());
        Assert.assertTrue(prefixMap.containsKey("banana"));
        Assert.assertTrue(prefixMap.containsKey("band"));

        try {
            prefixMap.put("bandit", 10);
            Assert.fail("Expected UnsupportedOperationException on prefixMap put");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            prefixMap.remove("banana");
            Assert.fail("Expected UnsupportedOperationException on prefixMap remove");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testNextAndPreviousKey() {
        Assert.assertEquals("apple", unmodifiableTrie.nextKey("app"));
        Assert.assertEquals("banana", unmodifiableTrie.nextKey("apple"));
        Assert.assertNull(unmodifiableTrie.nextKey("cat"));

        Assert.assertEquals("banana", unmodifiableTrie.previousKey("band"));
        Assert.assertEquals("apple", unmodifiableTrie.previousKey("banana"));
        Assert.assertNull(unmodifiableTrie.previousKey("app"));
    }

    @Test
    public void testMapIterator() {
        OrderedMapIterator<String, Integer> it = unmodifiableTrie.mapIterator();
        Assert.assertNotNull(it);
        Assert.assertFalse(it.hasPrevious());
        Assert.assertTrue(it.hasNext());

        Assert.assertEquals("app", it.next());
        Assert.assertEquals("app", it.getKey());
        Assert.assertEquals(Integer.valueOf(2), it.getValue());

        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("apple", it.next());
        Assert.assertEquals("apple", it.getKey());
        Assert.assertEquals(Integer.valueOf(1), it.getValue());

        Assert.assertEquals("apple", it.previous());
        Assert.assertEquals("apple", it.getKey());

        try {
            it.setValue(100);
            Assert.fail("Expected UnsupportedOperationException on mapIterator.setValue");
        } catch (final UnsupportedOperationException ignored) {
        }

        try {
            it.remove();
            Assert.fail("Expected UnsupportedOperationException on mapIterator.remove");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Trie<String, Integer> otherTrie = new PatriciaTrie<Integer>();
        otherTrie.put("apple", 1);
        otherTrie.put("app", 2);
        otherTrie.put("banana", 3);
        otherTrie.put("band", 4);
        otherTrie.put("cat", 5);

        Assert.assertEquals(unmodifiableTrie, originalTrie);
        Assert.assertEquals(unmodifiableTrie, otherTrie);
        Assert.assertEquals(unmodifiableTrie.hashCode(), originalTrie.hashCode());
        Assert.assertEquals(unmodifiableTrie, unmodifiableTrie);

        otherTrie.put("dog", 6);
        Assert.assertNotEquals(unmodifiableTrie, otherTrie);
        Assert.assertNotEquals(unmodifiableTrie, null);
        Assert.assertNotEquals(unmodifiableTrie, "string");
    }

    @Test
    public void testToString() {
        Assert.assertEquals(originalTrie.toString(), unmodifiableTrie.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(unmodifiableTrie);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Trie<String, Integer> deserialized = (Trie<String, Integer>) ois.readObject();
        ois.close();

        Assert.assertEquals(unmodifiableTrie, deserialized);
        Assert.assertEquals(unmodifiableTrie.size(), deserialized.size());
        Assert.assertTrue(deserialized instanceof Unmodifiable);

        try {
            deserialized.put("newKey", 100);
            Assert.fail("Expected UnsupportedOperationException on deserialized trie put");
        } catch (final UnsupportedOperationException ignored) {
        }
    }
}
