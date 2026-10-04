package org.apache.commons.collections.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

public class MultiKeyTest {

    @Test
    public void testConstructorTwoKeys() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals(2, mk.size());
        assertArrayEquals(new Object[]{"a", "b"}, mk.getKeys());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    @Test
    public void testConstructorThreeKeys() {
        MultiKey mk = new MultiKey("a", "b", "c");
        assertEquals(3, mk.size());
        assertArrayEquals(new Object[]{"a", "b", "c"}, mk.getKeys());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
    }

    @Test
    public void testConstructorFourKeys() {
        MultiKey mk = new MultiKey("a", "b", "c", "d");
        assertEquals(4, mk.size());
        assertArrayEquals(new Object[]{"a", "b", "c", "d"}, mk.getKeys());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
        assertEquals("d", mk.getKey(3));
    }

    @Test
    public void testConstructorFiveKeys() {
        MultiKey mk = new MultiKey("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertArrayEquals(new Object[]{"a", "b", "c", "d", "e"}, mk.getKeys());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
        assertEquals("d", mk.getKey(3));
        assertEquals("e", mk.getKey(4));
    }

    @Test
    public void testConstructorArray() {
        Object[] keys = {1, 2, 3};
        MultiKey mk = new MultiKey(keys);
        assertEquals(3, mk.size());
        assertArrayEquals(keys, mk.getKeys());
        assertNotSame(keys, mk.getKeys()); // getKeys returns a clone
        assertEquals(1, mk.getKey(0));
        assertEquals(2, mk.getKey(1));
        assertEquals(3, mk.getKey(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayNull() {
        new MultiKey((Object[]) null);
    }

    @Test
    public void testConstructorArrayEmpty() {
        MultiKey mk = new MultiKey(new Object[0]);
        assertEquals(0, mk.size());
        assertArrayEquals(new Object[0], mk.getKeys());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexTooLarge() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyEmptyArray() {
        MultiKey mk = new MultiKey(new Object[0]);
        mk.getKey(0);
    }

    @Test
    public void testConstructorArrayWithNullElements() {
        Object[] keys = {null, "b", null};
        MultiKey mk = new MultiKey(keys);
        assertEquals(3, mk.size());
        assertArrayEquals(keys, mk.getKeys());
        assertNull(mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertNull(mk.getKey(2));
    }

    @Test
    public void testConstructorArrayMakeCloneTrue() {
        Object[] keys = {1, 2};
        MultiKey mk = new MultiKey(keys, true);
        keys[0] = 99;
        // internal array should be unaffected
        assertEquals(1, mk.getKey(0));
        assertEquals(2, mk.getKey(1));
    }

    @Test
    public void testConstructorArrayMakeCloneFalse() {
        Object[] keys = {1, 2};
        MultiKey mk = new MultiKey(keys, false);
        keys[0] = 99;
        // internal array is the same reference, so change is visible
        assertEquals(99, mk.getKey(0));
        assertEquals(2, mk.getKey(1));
    }

    @Test
    public void testEqualsSameObject() {
        MultiKey mk = new MultiKey("a", "b");
        assertTrue(mk.equals(mk));
    }

    @Test
    public void testEqualsEqualKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
    }

    @Test
    public void testEqualsDifferentKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "c");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsDifferentLength() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsNull() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals(null));
    }

    @Test
    public void testEqualsNonMultiKey() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals("a"));
    }

    @Test
    public void testEqualsWithNullElements() {
        MultiKey mk1 = new MultiKey(new Object[]{null, "b"});
        MultiKey mk2 = new MultiKey(new Object[]{null, "b"});
        assertTrue(mk1.equals(mk2));
    }

    @Test
    public void testEqualsWithNullElementsDifferent() {
        MultiKey mk1 = new MultiKey(new Object[]{null, "b"});
        MultiKey mk2 = new MultiKey(new Object[]{"a", "b"});
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testHashCodeConsistent() {
        MultiKey mk = new MultiKey("a", "b");
        int hc1 = mk.hashCode();
        int hc2 = mk.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testHashCodeWithNulls() {
        MultiKey mk1 = new MultiKey(new Object[]{null, "b"});
        MultiKey mk2 = new MultiKey(new Object[]{null, "b"});
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testHashCodeEmptyArray() {
        MultiKey mk = new MultiKey(new Object[0]);
        assertEquals(0, mk.hashCode());
    }

    @Test
    public void testHashCodeCalculation() {
        // manual calculation: XOR of hash codes of non-null elements
        Object[] keys = {"a", null, "b"};
        int expected = "a".hashCode() ^ "b".hashCode();
        MultiKey mk = new MultiKey(keys);
        assertEquals(expected, mk.hashCode());
    }

    @Test
    public void testToString() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals("MultiKey[a, b]", mk.toString());
    }

    @Test
    public void testToStringEmpty() {
        MultiKey mk = new MultiKey(new Object[0]);
        assertEquals("MultiKey[]", mk.toString());
    }

    @Test
    public void testToStringWithNulls() {
        MultiKey mk = new MultiKey(new Object[]{null, "b"});
        assertEquals("MultiKey[null, b]", mk.toString());
    }

    @Test
    public void testSize() {
        assertEquals(2, new MultiKey("a", "b").size());
        assertEquals(0, new MultiKey(new Object[0]).size());
        assertEquals(5, new MultiKey(1,2,3,4,5).size());
    }

    @Test
    public void testGetKeysReturnsClone() {
        MultiKey mk = new MultiKey("a", "b");
        Object[] keys1 = mk.getKeys();
        Object[] keys2 = mk.getKeys();
        assertNotSame(keys1, keys2);
        assertArrayEquals(keys1, keys2);
    }
}
