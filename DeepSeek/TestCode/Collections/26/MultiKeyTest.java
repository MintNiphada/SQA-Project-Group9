package org.apache.commons.collections4.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

public class MultiKeyTest {

    @Test
    public void testConstructorWithTwoKeys() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertEquals(2, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    @Test
    public void testConstructorWithThreeKeys() {
        MultiKey<Integer> mk = new MultiKey<Integer>(1, 2, 3);
        assertEquals(3, mk.size());
        assertEquals(Integer.valueOf(1), mk.getKey(0));
        assertEquals(Integer.valueOf(2), mk.getKey(1));
        assertEquals(Integer.valueOf(3), mk.getKey(2));
    }

    @Test
    public void testConstructorWithFourKeys() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c", "d");
        assertEquals(4, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
        assertEquals("d", mk.getKey(3));
    }

    @Test
    public void testConstructorWithFiveKeys() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
        assertEquals("d", mk.getKey(3));
        assertEquals("e", mk.getKey(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullArray() {
        new MultiKey<String>((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullArrayAndFalseClone() {
        new MultiKey<String>((String[]) null, false);
    }

    @Test
    public void testConstructorWithArrayCloningTrue() {
        String[] keys = new String[]{"x", "y"};
        MultiKey<String> mk = new MultiKey<String>(keys, true);
        keys[0] = "changed";
        assertEquals("x", mk.getKey(0));
    }

    @Test
    public void testConstructorWithArrayCloningFalse() {
        String[] keys = new String[]{"x", "y"};
        MultiKey<String> mk = new MultiKey<String>(keys, false);
        keys[0] = "changed";
        assertEquals("changed", mk.getKey(0));
    }

    @Test
    public void testGetKeysCloning() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        String[] keys = mk.getKeys();
        keys[0] = "modified";
        assertEquals("a", mk.getKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexEqualToSize() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(2);
    }

    @Test
    public void testSize() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c");
        assertEquals(3, mk.size());
    }

    @Test
    public void testEqualsSameObject() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertTrue(mk.equals(mk));
    }

    @Test
    public void testEqualsEqualKeys() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
    }

    @Test
    public void testEqualsDifferentKeys() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "c");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsDifferentSize() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsNull() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals(null));
    }

    @Test
    public void testEqualsOtherType() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals("a"));
    }

    @Test
    public void testEqualsWithNullKeys() {
        MultiKey<String> mk1 = new MultiKey<String>((String[]) new String[]{null, "b"}, false);
        MultiKey<String> mk2 = new MultiKey<String>((String[]) new String[]{null, "b"}, false);
        assertTrue(mk1.equals(mk2));
    }

    @Test
    public void testEqualsWithNullKeysAndDifferentNonNull() {
        MultiKey<String> mk1 = new MultiKey<String>((String[]) new String[]{null, "b"}, false);
        MultiKey<String> mk2 = new MultiKey<String>((String[]) new String[]{null, "c"}, false);
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testHashCodeConsistency() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        int hc1 = mk.hashCode();
        int hc2 = mk.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testHashCodeWithNullKeys() {
        MultiKey<String> mk = new MultiKey<String>((String[]) new String[]{null, "b"}, false);
        mk.hashCode();
    }

    @Test
    public void testToString() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        String str = mk.toString();
        assertTrue(str.contains("MultiKey"));
        assertTrue(str.contains("a"));
        assertTrue(str.contains("b"));
    }

    @Test
    public void testToStringWithNullKey() {
        MultiKey<String> mk = new MultiKey<String>((String[]) new String[]{null, "b"}, false);
        String str = mk.toString();
        assertTrue(str.contains("null"));
    }

    @Test
    public void testReadResolve() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        int originalHashCode = mk.hashCode();
        MultiKey<String> deserialized = mk;
        assertEquals(originalHashCode, deserialized.hashCode());
    }

    @Test
    public void testConstructorWithEmptyArray() {
        MultiKey<String> mk = new MultiKey<String>(new String[0]);
        assertEquals(0, mk.size());
    }
}
