package org.jfree.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.jfree.chart.util.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link DefaultKeyedValues} class.
 */
public class DefaultKeyedValuesTest {

    private DefaultKeyedValues data;

    @Before
    public void setUp() {
        this.data = new DefaultKeyedValues();
    }

    @Test
    public void testConstructor() {
        Assert.assertEquals(0, this.data.getItemCount());
        Assert.assertTrue(this.data.getKeys().isEmpty());
    }

    @Test
    public void testGetItemCount() {
        Assert.assertEquals(0, this.data.getItemCount());
        this.data.addValue("A", 1.0);
        Assert.assertEquals(1, this.data.getItemCount());
        this.data.addValue("B", 2.0);
        Assert.assertEquals(2, this.data.getItemCount());
        this.data.removeValue("A");
        Assert.assertEquals(1, this.data.getItemCount());
        this.data.clear();
        Assert.assertEquals(0, this.data.getItemCount());
    }

    @Test
    public void testGetValueByIndex() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", null);
        Assert.assertEquals(new Double(1.0), this.data.getValue(0));
        Assert.assertNull(this.data.getValue(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexOutOfBoundsNegative() {
        this.data.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexOutOfBoundsPositive() {
        this.data.getValue(0);
    }

    @Test
    public void testGetKey() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        Assert.assertEquals("A", this.data.getKey(0));
        Assert.assertEquals("B", this.data.getKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBounds() {
        this.data.getKey(0);
    }

    @Test
    public void testGetIndex() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        Assert.assertEquals(0, this.data.getIndex("A"));
        Assert.assertEquals(1, this.data.getIndex("B"));
        Assert.assertEquals(-1, this.data.getIndex("NonExistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullKey() {
        this.data.getIndex(null);
    }

    @Test
    public void testGetKeys() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        List keys = this.data.getKeys();
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals("A", keys.get(0));
        Assert.assertEquals("B", keys.get(1));

        // Modifying the returned list should not affect internal state
        keys.clear();
        Assert.assertEquals(2, this.data.getItemCount());
    }

    @Test
    public void testGetValueByKey() {
        this.data.addValue("A", 10.0);
        this.data.addValue("B", null);
        Assert.assertEquals(new Double(10.0), this.data.getValue("A"));
        Assert.assertNull(this.data.getValue("B"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueByUnknownKey() {
        this.data.getValue("Missing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByNullKey() {
        this.data.getValue(null);
    }

    @Test
    public void testAddValueDouble() {
        this.data.addValue("K1", 1.5);
        Assert.assertEquals(new Double(1.5), this.data.getValue("K1"));
        Assert.assertEquals(0, this.data.getIndex("K1"));
    }

    @Test
    public void testAddValueNumber() {
        this.data.addValue("K1", new Integer(100));
        Assert.assertEquals(new Integer(100), this.data.getValue("K1"));
    }

    @Test
    public void testSetValueDouble() {
        this.data.setValue("K1", 5.0);
        Assert.assertEquals(new Double(5.0), this.data.getValue(0));
        this.data.setValue("K1", 10.0);
        Assert.assertEquals(1, this.data.getItemCount());
        Assert.assertEquals(new Double(10.0), this.data.getValue("K1"));
    }

    @Test
    public void testSetValueNumber() {
        this.data.setValue("K1", new Double(1.0));
        this.data.setValue("K2", new Double(2.0));
        Assert.assertEquals(2, this.data.getItemCount());

        // Update existing key
        this.data.setValue("K1", new Double(9.0));
        Assert.assertEquals(2, this.data.getItemCount());
        Assert.assertEquals(new Double(9.0), this.data.getValue(0));
        Assert.assertEquals("K1", this.data.getKey(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueNullKey() {
        this.data.setValue(null, new Double(1.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueDoubleNullKey() {
        this.data.setValue(null, 1.0);
    }

    @Test
    public void testInsertValueDouble() {
        this.data.insertValue(0, "A", 1.0);
        this.data.insertValue(0, "B", 2.0);
        Assert.assertEquals("B", this.data.getKey(0));
        Assert.assertEquals("A", this.data.getKey(1));
    }

    @Test
    public void testInsertValueAtSamePosition() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        // Overwrite "A" at pos 0
        this.data.insertValue(0, "A", 99.0);
        Assert.assertEquals(2, this.data.getItemCount());
        Assert.assertEquals(new Double(99.0), this.data.getValue(0));
        Assert.assertEquals("A", this.data.getKey(0));
        Assert.assertEquals(0, this.data.getIndex("A"));
    }

    @Test
    public void testInsertValueMoveExisting() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.addValue("C", 3.0);
        // Move "A" to index 2
        this.data.insertValue(2, "A", 10.0);
        Assert.assertEquals(3, this.data.getItemCount());
        Assert.assertEquals("B", this.data.getKey(0));
        Assert.assertEquals("C", this.data.getKey(1));
        Assert.assertEquals("A", this.data.getKey(2));
        Assert.assertEquals(new Double(10.0), this.data.getValue(2));
        Assert.assertEquals(2, this.data.getIndex("A"));
        Assert.assertEquals(0, this.data.getIndex("B"));
        Assert.assertEquals(1, this.data.getIndex("C"));
    }

    @Test
    public void testInsertValueNewKey() {
        this.data.addValue("A", 1.0);
        this.data.addValue("C", 3.0);
        this.data.insertValue(1, "B", 2.0);
        Assert.assertEquals(3, this.data.getItemCount());
        Assert.assertEquals("A", this.data.getKey(0));
        Assert.assertEquals("B", this.data.getKey(1));
        Assert.assertEquals("C", this.data.getKey(2));
        Assert.assertEquals(0, this.data.getIndex("A"));
        Assert.assertEquals(1, this.data.getIndex("B"));
        Assert.assertEquals(2, this.data.getIndex("C"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValueNegativePosition() {
        this.data.insertValue(-1, "A", 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValuePositionTooLarge() {
        this.data.insertValue(1, "A", 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValueNullKey() {
        this.data.insertValue(0, null, 1.0);
    }

    @Test
    public void testRemoveValueByIndex() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.addValue("C", 3.0);

        // Remove first element (index < keys.size() rebuilds index)
        this.data.removeValue(0);
        Assert.assertEquals(2, this.data.getItemCount());
        Assert.assertEquals("B", this.data.getKey(0));
        Assert.assertEquals(0, this.data.getIndex("B"));
        Assert.assertEquals(1, this.data.getIndex("C"));
        Assert.assertEquals(-1, this.data.getIndex("A"));

        // Remove last element (index == keys.size())
        this.data.removeValue(1);
        Assert.assertEquals(1, this.data.getItemCount());
        Assert.assertEquals("B", this.data.getKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndexOutOfBounds() {
        this.data.removeValue(0);
    }

    @Test
    public void testRemoveValueByKey() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.removeValue("A");
        Assert.assertEquals(1, this.data.getItemCount());
        Assert.assertEquals(-1, this.data.getIndex("A"));
        Assert.assertEquals(0, this.data.getIndex("B"));

        // Removing non-existent key does nothing
        this.data.removeValue("NonExistent");
        Assert.assertEquals(1, this.data.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveValueByNullKey() {
        this.data.removeValue(null);
    }

    @Test
    public void testClear() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.clear();
        Assert.assertEquals(0, this.data.getItemCount());
        Assert.assertEquals(-1, this.data.getIndex("A"));
        Assert.assertEquals(-1, this.data.getIndex("B"));
    }

    @Test
    public void testSortByKeysAscending() {
        this.data.addValue("C", 3.0);
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);

        this.data.sortByKeys(SortOrder.ASCENDING);

        Assert.assertEquals("A", this.data.getKey(0));
        Assert.assertEquals(new Double(1.0), this.data.getValue(0));
        Assert.assertEquals("B", this.data.getKey(1));
        Assert.assertEquals(new Double(2.0), this.data.getValue(1));
        Assert.assertEquals("C", this.data.getKey(2));
        Assert.assertEquals(new Double(3.0), this.data.getValue(2));

        Assert.assertEquals(0, this.data.getIndex("A"));
        Assert.assertEquals(1, this.data.getIndex("B"));
        Assert.assertEquals(2, this.data.getIndex("C"));
    }

    @Test
    public void testSortByKeysDescending() {
        this.data.addValue("A", 1.0);
        this.data.addValue("C", 3.0);
        this.data.addValue("B", 2.0);

        this.data.sortByKeys(SortOrder.DESCENDING);

        Assert.assertEquals("C", this.data.getKey(0));
        Assert.assertEquals("B", this.data.getKey(1));
        Assert.assertEquals("A", this.data.getKey(2));
    }

    @Test
    public void testSortByValuesAscending() {
        this.data.addValue("K1", 30.0);
        this.data.addValue("K2", null);
        this.data.addValue("K3", 10.0);
        this.data.addValue("K4", 20.0);

        this.data.sortByValues(SortOrder.ASCENDING);

        Assert.assertEquals("K3", this.data.getKey(0));
        Assert.assertEquals(new Double(10.0), this.data.getValue(0));
        Assert.assertEquals("K4", this.data.getKey(1));
        Assert.assertEquals(new Double(20.0), this.data.getValue(1));
        Assert.assertEquals("K1", this.data.getKey(2));
        Assert.assertEquals(new Double(30.0), this.data.getValue(2));
        Assert.assertEquals("K2", this.data.getKey(3));
        Assert.assertNull(this.data.getValue(3));

        Assert.assertEquals(0, this.data.getIndex("K3"));
        Assert.assertEquals(1, this.data.getIndex("K4"));
        Assert.assertEquals(2, this.data.getIndex("K1"));
        Assert.assertEquals(3, this.data.getIndex("K2"));
    }

    @Test
    public void testSortByValuesDescending() {
        this.data.addValue("K1", 30.0);
        this.data.addValue("K2", null);
        this.data.addValue("K3", 10.0);
        this.data.addValue("K4", 20.0);

        this.data.sortByValues(SortOrder.DESCENDING);

        Assert.assertEquals("K1", this.data.getKey(0));
        Assert.assertEquals(new Double(30.0), this.data.getValue(0));
        Assert.assertEquals("K4", this.data.getKey(1));
        Assert.assertEquals(new Double(20.0), this.data.getValue(1));
        Assert.assertEquals("K3", this.data.getKey(2));
        Assert.assertEquals(new Double(10.0), this.data.getValue(2));
        Assert.assertEquals("K2", this.data.getKey(3));
        Assert.assertNull(this.data.getValue(3));
    }

    @Test
    public void testEqualsAndHashCode() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        DefaultKeyedValues v2 = new DefaultKeyedValues();

        // Same empty instance and equality
        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        // Null and different type check
        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals("Not a KeyedValues"));

        // Different count
        v1.addValue("A", 1.0);
        Assert.assertFalse(v1.equals(v2));

        v2.addValue("A", 1.0);
        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        // Different keys
        DefaultKeyedValues v3 = new DefaultKeyedValues();
        v3.addValue("B", 1.0);
        Assert.assertFalse(v1.equals(v3));

        // Different values
        DefaultKeyedValues v4 = new DefaultKeyedValues();
        v4.addValue("A", 2.0);
        Assert.assertFalse(v1.equals(v4));

        // Value is null in one, not null in other
        DefaultKeyedValues v5 = new DefaultKeyedValues();
        v5.addValue("A", null);
        Assert.assertFalse(v5.equals(v1));
        Assert.assertFalse(v1.equals(v5));

        // Value is null in both
        DefaultKeyedValues v6 = new DefaultKeyedValues();
        v6.addValue("A", null);
        Assert.assertTrue(v5.equals(v6));
        Assert.assertEquals(v5.hashCode(), v6.hashCode());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);

        DefaultKeyedValues clone = (DefaultKeyedValues) this.data.clone();

        Assert.assertNotSame(this.data, clone);
        Assert.assertEquals(this.data, clone);

        // Verify independent copies
        clone.addValue("C", 3.0);
        Assert.assertFalse(this.data.equals(clone));
        Assert.assertEquals(2, this.data.getItemCount());
        Assert.assertEquals(3, clone.getItemCount());
    }

    @Test
    public void testSerialization() throws Exception {
        this.data.addValue("K1", 1.1);
        this.data.addValue("K2", null);
        this.data.addValue("K3", 3.3);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.data);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        DefaultKeyedValues deserialized = (DefaultKeyedValues) in.readObject();
        in.close();

        Assert.assertEquals(this.data, deserialized);
        Assert.assertEquals(0, deserialized.getIndex("K1"));
        Assert.assertEquals(1, deserialized.getIndex("K2"));
        Assert.assertEquals(2, deserialized.getIndex("K3"));
        Assert.assertEquals(new Double(1.1), deserialized.getValue("K1"));
        Assert.assertNull(deserialized.getValue("K2"));
    }
}