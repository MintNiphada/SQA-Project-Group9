package org.apache.commons.collections4.keyvalue;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * Unit tests for {@link MultiKey}.
 */
public class MultiKeyTest {

    private final Integer ONE = 1;
    private final Integer TWO = 2;
    private final Integer THREE = 3;
    private final Integer FOUR = 4;
    private final Integer FIVE = 5;

    @Test
    public void testConstructor2Keys() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        Assert.assertEquals(2, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
    }

    @Test
    public void testConstructor3Keys() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO, THREE);
        Assert.assertEquals(3, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
    }

    @Test
    public void testConstructor4Keys() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO, THREE, FOUR);
        Assert.assertEquals(4, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
        Assert.assertEquals(FOUR, mk.getKey(3));
    }

    @Test
    public void testConstructor5Keys() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO, THREE, FOUR, FIVE);
        Assert.assertEquals(5, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));
        Assert.assertEquals(FOUR, mk.getKey(3));
        Assert.assertEquals(FIVE, mk.getKey(4));
    }

    @Test
    public void testConstructorArray() {
        final Integer[] array = new Integer[] { ONE, TWO, THREE };
        final MultiKey<Integer> mk = new MultiKey<Integer>(array);
        Assert.assertEquals(3, mk.size());
        Assert.assertEquals(ONE, mk.getKey(0));
        Assert.assertEquals(TWO, mk.getKey(1));
        Assert.assertEquals(THREE, mk.getKey(2));

        // Array is cloned by default constructor, so changing original array doesn't affect MultiKey
        array[0] = 99;
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayNull() {
        new MultiKey<Object>((Object[]) null);
    }

    @Test
    public void testConstructorArrayCloned() {
        final Integer[] array = new Integer[] { ONE, TWO };
        final MultiKey<Integer> mk = new MultiKey<Integer>(array, true);
        Assert.assertEquals(2, mk.size());
        array[0] = 99;
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test
    public void testConstructorArrayNotCloned() {
        final Integer[] array = new Integer[] { ONE, TWO };
        final MultiKey<Integer> mk = new MultiKey<Integer>(array, false);
        Assert.assertEquals(2, mk.size());
        array[0] = 99;
        Assert.assertEquals(Integer.valueOf(99), mk.getKey(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayBooleanNull() {
        new MultiKey<Object>(null, false);
    }

    @Test
    public void testGetKeys() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        final Integer[] keys = mk.getKeys();
        Assert.assertNotNull(keys);
        Assert.assertEquals(2, keys.length);
        Assert.assertEquals(ONE, keys[0]);
        Assert.assertEquals(TWO, keys[1]);

        // Modifying the returned array should not affect the MultiKey
        keys[0] = 99;
        Assert.assertEquals(ONE, mk.getKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBoundsIndex() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        mk.getKey(2);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(0, new MultiKey<Object>(new Object[0]).size());
        Assert.assertEquals(1, new MultiKey<Object>(new Object[] { "A" }).size());
        Assert.assertEquals(2, new MultiKey<Integer>(ONE, TWO).size());
        Assert.assertEquals(3, new MultiKey<Integer>(ONE, TWO, THREE).size());
        Assert.assertEquals(4, new MultiKey<Integer>(ONE, TWO, THREE, FOUR).size());
        Assert.assertEquals(5, new MultiKey<Integer>(ONE, TWO, THREE, FOUR, FIVE).size());
    }

    @Test
    public void testEquals() {
        final MultiKey<String> mk1 = new MultiKey<String>("A", "B");
        final MultiKey<String> mk2 = new MultiKey<String>("A", "B");
        final MultiKey<String> mk3 = new MultiKey<String>("A", "C");
        final MultiKey<String> mk4 = new MultiKey<String>("A", "B", "C");
        final MultiKey<String> mkNull1 = new MultiKey<String>("A", null);
        final MultiKey<String> mkNull2 = new MultiKey<String>("A", null);

        // Same reference
        Assert.assertEquals(mk1, mk1);
        Assert.assertTrue(mk1.equals(mk1));

        // Equal contents
        Assert.assertEquals(mk1, mk2);
        Assert.assertEquals(mk2, mk1);
        Assert.assertEquals(mkNull1, mkNull2);

        // Different contents
        Assert.assertNotEquals(mk1, mk3);
        Assert.assertNotEquals(mk1, mk4);
        Assert.assertNotEquals(mk1, mkNull1);

        // Different types and null
        Assert.assertNotNull(mk1);
        Assert.assertNotEquals("A", mk1);
        Assert.assertNotEquals(mk1, new Object());
    }

    @Test
    public void testHashCode() {
        final MultiKey<String> mk1 = new MultiKey<String>("A", "B");
        final MultiKey<String> mk2 = new MultiKey<String>("A", "B");
        final MultiKey<String> mkNull = new MultiKey<String>("A", null);

        Assert.assertEquals(mk1.hashCode(), mk2.hashCode());
        Assert.assertEquals("A".hashCode() ^ "B".hashCode(), mk1.hashCode());
        Assert.assertEquals("A".hashCode(), mkNull.hashCode());

        final MultiKey<Object> mkAllNull = new MultiKey<Object>(null, null);
        Assert.assertEquals(0, mkAllNull.hashCode());
    }

    @Test
    public void testToString() {
        final MultiKey<String> mk1 = new MultiKey<String>("A", "B");
        Assert.assertEquals("MultiKey" + Arrays.toString(new String[] { "A", "B" }), mk1.toString());

        final MultiKey<String> mkNull = new MultiKey<String>("A", null);
        Assert.assertEquals("MultiKey" + Arrays.toString(new String[] { "A", null }), mkNull.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        final MultiKey<String> original = new MultiKey<String>("Hello", "World");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        final MultiKey<String> deserialized = (MultiKey<String>) ois.readObject();
        ois.close();

        Assert.assertNotSame(original, deserialized);
        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.hashCode(), deserialized.hashCode());
        Assert.assertEquals(original.size(), deserialized.size());
        Assert.assertEquals(original.getKey(0), deserialized.getKey(0));
        Assert.assertEquals(original.getKey(1), deserialized.getKey(1));
    }
}
