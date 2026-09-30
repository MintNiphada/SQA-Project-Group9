package org.apache.commons.collections.keyvalue;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class MultiKeyTest {

    private static final Object ONE = "1";
    private static final Object TWO = "2";
    private static final Object THREE = "3";
    private static final Object FOUR = "4";
    private static final Object FIVE = "5";

    @Test
    public void testConstructor2Keys() {
        MultiKey mk = new MultiKey(ONE, TWO);
        Assert.assertEquals(2, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertArrayEquals(new Object[]{ONE, TWO}, mk.getKeys());
    }

    @Test
    public void testConstructor3Keys() {
        MultiKey mk = new MultiKey(ONE, TWO, THREE);
        Assert.assertEquals(3, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
        Assert.assertArrayEquals(new Object[]{ONE, TWO, THREE}, mk.getKeys());
    }

    @Test
    public void testConstructor4Keys() {
        MultiKey mk = new MultiKey(ONE, TWO, THREE, FOUR);
        Assert.assertEquals(4, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
        Assert.assertEquals(FOUR, mk.getKey(3));
        Assert.assertArrayEquals(new Object[]{ONE, TWO, THREE, FOUR}, mk.getKeys());
    }

    @Test
    public void testConstructor5Keys() {
        MultiKey mk = new MultiKey(ONE, TWO, THREE, FOUR, FIVE);
        Assert.assertEquals(5, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
        Assert.assertEquals(FOUR, mk.getKey(3));
        Assert.assertEquals(FIVE, mk.getKey(4));
        Assert.assertArrayEquals(new Object[]{ONE, TWO, THREE, FOUR, FIVE}, mk.getKeys());
    }

    @Test
    public void testConstructorArrayClonedByDefault() {
        Object[] keys = new Object[]{ONE, TWO};
        MultiKey mk = new MultiKey(keys);
        Assert.assertEquals(2, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));

        // Modifying original array should not affect MultiKey because it is cloned
        keys[0] = "MODIFIED";
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test
    public void testConstructorArrayMakeCloneTrue() {
        Object[] keys = new Object[]{ONE, TWO};
        MultiKey mk = new MultiKey(keys, true);
        Assert.assertEquals(2, mk.size());
        keys[0] = "MODIFIED";
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test
    public void testConstructorArrayMakeCloneFalse() {
        Object[] keys = new Object[]{ONE, TWO};
        MultiKey mk = new MultiKey(keys, false);
        Assert.assertEquals(2, mk.size());
        // Since makeClone is false, keys is assigned directly
        keys[0] = "MODIFIED";
        Assert.assertEquals("MODIFIED", mk.getKey(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayNull() {
        new MultiKey((Object[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayNullWithMakeClone() {
        new MultiKey(null, true);
    }

    @Test
    public void testConstructorEmptyArray() {
        MultiKey mk = new MultiKey(new Object[0]);
        Assert.assertEquals(0, mk.size());
        Assert.assertEquals(0, mk.getKeys().length);
        Assert.assertEquals(0, mk.hashCode());
        Assert.assertEquals("MultiKey[]", mk.toString());
    }

    @Test
    public void testGetKeysReturnsClone() {
        MultiKey mk = new MultiKey(ONE, TWO);
        Object[] keys1 = mk.getKeys();
        Object[] keys2 = mk.getKeys();
        Assert.assertNotSame(keys1, keys2);
        Assert.assertArrayEquals(keys1, keys2);

        // Modifying the returned array should not affect internal state
        keys1[0] = "MODIFIED";
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test
    public void testGetKey() {
        MultiKey mk = new MultiKey(ONE, TWO, THREE);
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        MultiKey mk = new MultiKey(ONE, TWO);
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBoundsIndex() {
        MultiKey mk = new MultiKey(ONE, TWO);
        mk.getKey(2);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, new MultiKey(new Object[0]).size());
        Assert.assertEquals(1, new MultiKey(new Object[]{ONE}).size());
        Assert.assertEquals(2, new MultiKey(ONE, TWO).size());
        Assert.assertEquals(3, new MultiKey(ONE, TWO, THREE).size());
        Assert.assertEquals(4, new MultiKey(ONE, TWO, THREE, FOUR).size());
        Assert.assertEquals(5, new MultiKey(ONE, TWO, THREE, FOUR, FIVE).size());
    }

    @Test
    public void testEqualsAndHashCode() {
        MultiKey mk1 = new MultiKey(ONE, TWO);
        MultiKey mk2 = new MultiKey(ONE, TWO);
        MultiKey mk3 = new MultiKey(ONE, "DIFF");
        MultiKey mk4 = new MultiKey(ONE, TWO, THREE);
        MultiKey mk5 = new MultiKey(ONE, null);
        MultiKey mk6 = new MultiKey(ONE, null);

        // Reflexive
        Assert.assertTrue(mk1.equals(mk1));

        // Symmetric
        Assert.assertTrue(mk1.equals(mk2));
        Assert.assertTrue(mk2.equals(mk1));
        Assert.assertEquals(mk1.hashCode(), mk2.hashCode());

        // Null and different class checks
        Assert.assertFalse(mk1.equals(null));
        Assert.assertFalse(mk1.equals("Not a MultiKey"));
        Assert.assertFalse(mk1.equals(new Object()));

        // Different keys
        Assert.assertFalse(mk1.equals(mk3));
        Assert.assertFalse(mk3.equals(mk1));

        // Different sizes
        Assert.assertFalse(mk1.equals(mk4));
        Assert.assertFalse(mk4.equals(mk1));

        // With null keys
        Assert.assertTrue(mk5.equals(mk6));
        Assert.assertEquals(mk5.hashCode(), mk6.hashCode());
        Assert.assertFalse(mk1.equals(mk5));
    }

    @Test
    public void testHashCodeCalculation() {
        int expectedHash1 = ONE.hashCode() ^ TWO.hashCode();
        MultiKey mk1 = new MultiKey(ONE, TWO);
        Assert.assertEquals(expectedHash1, mk1.hashCode());

        int expectedHash2 = ONE.hashCode() ^ TWO.hashCode() ^ THREE.hashCode();
        MultiKey mk2 = new MultiKey(ONE, TWO, THREE);
        Assert.assertEquals(expectedHash2, mk2.hashCode());

        // Null elements should not contribute to the hash code calculation
        MultiKey mkWithNull = new MultiKey(null, ONE, null, TWO);
        int expectedHashWithNull = ONE.hashCode() ^ TWO.hashCode();
        Assert.assertEquals(expectedHashWithNull, mkWithNull.hashCode());

        MultiKey allNulls = new MultiKey(null, null);
        Assert.assertEquals(0, allNulls.hashCode());
    }

    @Test
    public void testToString() {
        MultiKey mk2 = new MultiKey(ONE, TWO);
        Assert.assertEquals("MultiKey[" + ONE + ", " + TWO + "]", mk2.toString());

        MultiKey mkWithNull = new MultiKey(ONE, null, TWO);
        Assert.assertEquals("MultiKey[" + ONE + ", null, " + TWO + "]", mkWithNull.toString());

        MultiKey mkEmpty = new MultiKey(new Object[0]);
        Assert.assertEquals("MultiKey[]", mkEmpty.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        MultiKey original = new MultiKey(ONE, TWO, THREE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.size(), deserialized.size());
        Assert.assertEquals(original.getKey(0), deserialized.getKey(0));
        Assert.assertEquals(original.getKey(1), deserialized.getKey(1));
        Assert.assertEquals(original.getKey(2), deserialized.getKey(2));
    }
}
