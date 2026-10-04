package org.apache.commons.collections4.trie;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UnmodifiableTrieTest {

    private Trie<String, Integer> delegate;
    private UnmodifiableTrie<String, Integer> trie;
    private UnmodifiableTrie<String, Integer> emptyTrie;

    @Before
    public void setUp() {
        delegate = new PatriciaTrie<String, Integer>();
        delegate.put("a", 1);
        delegate.put("b", 2);
        delegate.put("c", 3);
        delegate.put("ab", 4);
        delegate.put("abc", 5);
        trie = new UnmodifiableTrie<String, Integer>(delegate);
        emptyTrie = new UnmodifiableTrie<String, Integer>(new PatriciaTrie<String, Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTrie() {
        new UnmodifiableTrie<String, Integer>(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryNullTrie() {
        UnmodifiableTrie.unmodifiableTrie(null);
    }

    @Test
    public void testFactoryReturnsUnmodifiableTrie() {
        Trie<String, Integer> result = UnmodifiableTrie.unmodifiableTrie(delegate);
        Assert.assertTrue(result instanceof UnmodifiableTrie);
    }

    @Test
    public void testEntrySet() {
        Set<Map.Entry<String, Integer>> entries = trie.entrySet();
        Assert.assertEquals(5, entries.size());
        Assert.assertTrue(entries.contains(new PatriciaTrie<String, Integer>().put("a", 1) != null ? null : null));
        try {
            entries.add(null);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testKeySet() {
        Set<String> keys = trie.keySet();
        Assert.assertEquals(5, keys.size());
        Assert.assertTrue(keys.contains("a"));
        try {
            keys.add("d");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testValues() {
        Collection<Integer> values = trie.values();
        Assert.assertEquals(5, values.size());
        Assert.assertTrue(values.contains(1));
        try {
            values.add(6);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        trie.clear();
    }

    @Test
    public void testContainsKey() {
        Assert.assertTrue(trie.containsKey("a"));
        Assert.assertFalse(trie.containsKey("z"));
    }

    @Test
    public void testContainsValue() {
        Assert.assertTrue(trie.containsValue(1));
        Assert.assertFalse(trie.containsValue(100));
    }

    @Test
    public void testGet() {
        Assert.assertEquals(Integer.valueOf(1), trie.get("a"));
        Assert.assertNull(trie.get("z"));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertFalse(trie.isEmpty());
        Assert.assertTrue(emptyTrie.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        trie.put("d", 6);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        trie.putAll(Collections.singletonMap("d", 6));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        trie.remove("a");
    }

    @Test
    public void testSize() {
        Assert.assertEquals(5, trie.size());
        Assert.assertEquals(0, emptyTrie.size());
    }

    @Test
    public void testFirstKey() {
        Assert.assertEquals("a", trie.firstKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testFirstKeyEmpty() {
        emptyTrie.firstKey();
    }

    @Test
    public void testLastKey() {
        Assert.assertEquals("c", trie.lastKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testLastKeyEmpty() {
        emptyTrie.lastKey();
    }

    @Test
    public void testHeadMap() {
        SortedMap<String, Integer> head = trie.headMap("b");
        Assert.assertEquals(2, head.size());
        Assert.assertTrue(head.containsKey("a"));
        Assert.assertTrue(head.containsKey("ab"));
        try {
            head.put("z", 100);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testSubMap() {
        SortedMap<String, Integer> sub = trie.subMap("a", "c");
        Assert.assertEquals(4, sub.size());
        Assert.assertTrue(sub.containsKey("a"));
        Assert.assertTrue(sub.containsKey("ab"));
        Assert.assertTrue(sub.containsKey("abc"));
        Assert.assertTrue(sub.containsKey("b"));
        try {
            sub.put("z", 100);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testTailMap() {
        SortedMap<String, Integer> tail = trie.tailMap("b");
        Assert.assertEquals(2, tail.size());
        Assert.assertTrue(tail.containsKey("b"));
        Assert.assertTrue(tail.containsKey("c"));
        try {
            tail.put("z", 100);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testPrefixMap() {
        SortedMap<String, Integer> prefix = trie.prefixMap("ab");
        Assert.assertEquals(2, prefix.size());
        Assert.assertTrue(prefix.containsKey("ab"));
        Assert.assertTrue(prefix.containsKey("abc"));
        try {
            prefix.put("z", 100);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testComparator() {
        Comparator<? super String> comp = trie.comparator();
        Assert.assertNull(comp);
    }

    @Test
    public void testMapIterator() {
        OrderedMapIterator<String, Integer> it = trie.mapIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
        Assert.assertEquals(Integer.valueOf(1), it.getValue());
        try {
            it.remove();
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testNextKey() {
        Assert.assertEquals("ab", trie.nextKey("a"));
        Assert.assertNull(trie.nextKey("c"));
    }

    @Test
    public void testPreviousKey() {
        Assert.assertEquals("abc", trie.previousKey("b"));
        Assert.assertNull(trie.previousKey("a"));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(delegate.hashCode(), trie.hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(trie.equals(delegate));
        UnmodifiableTrie<String, Integer> other = new UnmodifiableTrie<String, Integer>(delegate);
        Assert.assertTrue(trie.equals(other));
        Assert.assertFalse(trie.equals(emptyTrie));
    }

    @Test
    public void testToString() {
        Assert.assertEquals(delegate.toString(), trie.toString());
    }
}
