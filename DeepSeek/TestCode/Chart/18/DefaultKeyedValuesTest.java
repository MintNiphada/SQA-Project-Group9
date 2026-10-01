package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import org.jfree.chart.util.SortOrder;

public class DefaultKeyedValuesTest {

    private DefaultKeyedValues data;

    @Before
    public void setUp() {
        data = new DefaultKeyedValues();
    }

    @Test
    public void testConstructor() {
        assertEquals(0, data.getItemCount());
        assertTrue(data.getKeys().isEmpty());
        assertEquals(-1, data.getIndex("A"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullKey() {
        data.getIndex(null);
    }

    @Test
    public void testGetIndexNonExistentKey() {
        assertEquals(-1, data.getIndex("A"));
    }

    @Test
    public void testAddValueAndGetIndex() {
        data.addValue("A", 1.0);
        assertEquals(0, data.getIndex("A"));
        data.addValue("B", 2.0);
        assertEquals(1, data.getIndex("B"));
        assertEquals(0, data.getIndex("A"));
    }

    @Test
    public void testAddValueUpdateExisting() {
        data.addValue("A", 1.0);
        data.addValue("A", 2.0);
        assertEquals(1, data.getItemCount());
        assertEquals(2.0, data.getValue("A").doubleValue(), 0.0001);
    }

    @Test
    public void testAddValueNullKey() {
        try {
            data.addValue(null, 1.0);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            data.addValue(null, new Double(1.0));
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddValueNullValue() {
        data.addValue("A", (Number) null);
        assertEquals(1, data.getItemCount());
        assertNull(data.getValue("A"));
    }

    @Test
    public void testSetValueNewKey() {
        data.setValue("A", 5.0);
        assertEquals(5.0, data.getValue("A").doubleValue(), 0.0001);
    }

    @Test
    public void testSetValueUpdateExisting() {
        data.addValue("A", 1.0);
        data.setValue("A", 10.0);
        assertEquals(10.0, data.getValue("A").doubleValue(), 0.0001);
    }

    @Test
    public void testSetValueNullKey() {
        try {
            data.setValue(null, 1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            data.setValue(null, new Double(1.0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetValueByIndex() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        assertEquals(1.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue(1).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexNegative() {
        data.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexTooLarge() {
        data.addValue("A", 1.0);
        data.getValue(1);
    }

    @Test
    public void testGetKeyByIndex() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyByIndexNegative() {
        data.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyByIndexTooLarge() {
        data.addValue("A", 1.0);
        data.getKey(1);
    }

    @Test
    public void testGetValueByKey() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        assertEquals(1.0, data.getValue("A").doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue("B").doubleValue(), 0.0001);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueByKeyUnknown() {
        data.getValue("X");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByKeyNull() {
        data.getValue(null);
    }

    @Test
    public void testGetKeys() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        List keys = data.getKeys();
        assertEquals(2, keys.size());
        assertEquals("A", keys.get(0));
        assertEquals("B", keys.get(1));
        // ensure returned list is a copy
        keys.add("C");
        assertEquals(2, data.getItemCount());
    }

    @Test
    public void testInsertValueNewKeyAtBeginning() {
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);
        data.insertValue(0, "A", 1.0);
        assertEquals(3, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("C", data.getKey(2));
        assertEquals(1.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("B"));
        assertEquals(2, data.getIndex("C"));
    }

    @Test
    public void testInsertValueNewKeyAtEnd() {
        data.addValue("A", 1.0);
        data.insertValue(1, "B", 2.0);
        assertEquals(2, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
    }

    @Test
    public void testInsertValueExistingKeyMove() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);
        // move "A" to position 2
        data.insertValue(2, "A", 10.0);
        assertEquals(3, data.getItemCount());
        assertEquals("B", data.getKey(0));
        assertEquals("C", data.getKey(1));
        assertEquals("A", data.getKey(2));
        assertEquals(10.0, data.getValue(2).doubleValue(), 0.0001);
        assertEquals(2, data.getIndex("A"));
        assertEquals(0, data.getIndex("B"));
        assertEquals(1, data.getIndex("C"));
    }

    @Test
    public void testInsertValueExistingKeySamePosition() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.insertValue(0, "A", 100.0);
        assertEquals(2, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals(100.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValuePositionNegative() {
        data.insertValue(-1, "A", 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValuePositionTooLarge() {
        data.insertValue(1, "A", 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValueNullKey() {
        data.insertValue(0, null, 1.0);
    }

    @Test
    public void testInsertValueNullValue() {
        data.insertValue(0, "A", (Number) null);
        assertEquals(1, data.getItemCount());
        assertNull(data.getValue(0));
    }

    @Test
    public void testRemoveValueByIndexFirst() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);
        data.removeValue(0);
        assertEquals(2, data.getItemCount());
        assertEquals("B", data.getKey(0));
        assertEquals("C", data.getKey(1));
        assertEquals(-1, data.getIndex("A"));
        assertEquals(0, data.getIndex("B"));
        assertEquals(1, data.getIndex("C"));
    }

    @Test
    public void testRemoveValueByIndexMiddle() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);
        data.removeValue(1);
        assertEquals(2, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("C", data.getKey(1));
        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("C"));
        assertEquals(-1, data.getIndex("B"));
    }

    @Test
    public void testRemoveValueByIndexLast() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.removeValue(1);
        assertEquals(1, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals(0, data.getIndex("A"));
        assertEquals(-1, data.getIndex("B"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndexNegative() {
        data.removeValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndexTooLarge() {
        data.addValue("A", 1.0);
        data.removeValue(1);
    }

    @Test
    public void testRemoveValueByKeyExisting() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.removeValue("A");
        assertEquals(1, data.getItemCount());
        assertEquals("B", data.getKey(0));
        assertEquals(-1, data.getIndex("A"));
    }

    @Test
    public void testRemoveValueByKeyNonExisting() {
        data.addValue("A", 1.0);
        // should not throw, just return
        data.removeValue("X");
        assertEquals(1, data.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveValueByKeyNull() {
        data.removeValue(null);
    }

    @Test
    public void testClear() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.clear();
        assertEquals(0, data.getItemCount());
        assertTrue(data.getKeys().isEmpty());
        assertEquals(-1, data.getIndex("A"));
    }

    @Test
    public void testSortByKeysAscending() {
        data.addValue("C", 3.0);
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.sortByKeys(SortOrder.ASCENDING);
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("C", data.getKey(2));
        assertEquals(1.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue(1).doubleValue(), 0.0001);
        assertEquals(3.0, data.getValue(2).doubleValue(), 0.0001);
    }

    @Test
    public void testSortByKeysDescending() {
        data.addValue("A", 1.0);
        data.addValue("C", 3.0);
        data.addValue("B", 2.0);
        data.sortByKeys(SortOrder.DESCENDING);
        assertEquals("C", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("A", data.getKey(2));
    }

    @Test
    public void testSortByKeysEmpty() {
        data.sortByKeys(SortOrder.ASCENDING);
        assertEquals(0, data.getItemCount());
    }

    @Test
    public void testSortByValuesAscending() {
        data.addValue("A", 3.0);
        data.addValue("B", 1.0);
        data.addValue("C", 2.0);
        data.sortByValues(SortOrder.ASCENDING);
        assertEquals("B", data.getKey(0));
        assertEquals("C", data.getKey(1));
        assertEquals("A", data.getKey(2));
        assertEquals(1.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue(1).doubleValue(), 0.0001);
        assertEquals(3.0, data.getValue(2).doubleValue(), 0.0001);
    }

    @Test
    public void testSortByValuesDescending() {
        data.addValue("A", 1.0);
        data.addValue("B", 3.0);
        data.addValue("C", 2.0);
        data.sortByValues(SortOrder.DESCENDING);
        assertEquals("B", data.getKey(0));
        assertEquals("C", data.getKey(1));
        assertEquals("A", data.getKey(2));
    }

    @Test
    public void testSortByValuesWithNulls() {
        data.addValue("A", 2.0);
        data.addValue("B", null);
        data.addValue("C", 1.0);
        data.sortByValues(SortOrder.ASCENDING);
        // nulls should be at the end
        assertEquals("C", data.getKey(0));
        assertEquals("A", data.getKey(1));
        assertEquals("B", data.getKey(2));
        assertNull(data.getValue(2));
    }

    @Test
    public void testSortByValuesEmpty() {
        data.sortByValues(SortOrder.ASCENDING);
        assertEquals(0, data.getItemCount());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(data.equals(data));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(data.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(data.equals("String"));
    }

    @Test
    public void testEqualsSameData() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        DefaultKeyedValues other = new DefaultKeyedValues();
        other.addValue("A", 1.0);
        other.addValue("B", 2.0);
        assertTrue(data.equals(other));
    }

    @Test
    public void testEqualsDifferentOrder() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        DefaultKeyedValues other = new DefaultKeyedValues();
        other.addValue("B", 2.0);
        other.addValue("A", 1.0);
        assertFalse(data.equals(other));
    }

    @Test
    public void testEqualsDifferentCount() {
        data.addValue("A", 1.0);
        DefaultKeyedValues other = new DefaultKeyedValues();
        other.addValue("A", 1.0);
        other.addValue("B", 2.0);
        assertFalse(data.equals(other));
    }

    @Test
    public void testEqualsNullValues() {
        data.addValue("A", null);
        data.addValue("B", 2.0);
        DefaultKeyedValues other = new DefaultKeyedValues();
        other.addValue("A", null);
        other.addValue("B", 2.0);
        assertTrue(data.equals(other));
    }

    @Test
    public void testEqualsNullValueMismatch() {
        data.addValue("A", null);
        DefaultKeyedValues other = new DefaultKeyedValues();
        other.addValue("A", 1.0);
        assertFalse(data.equals(other));
    }

    @Test
    public void testHashCode() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        int hash = data.hashCode();
        assertNotNull(hash); // just ensure no exception
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        DefaultKeyedValues clone = (DefaultKeyedValues) data.clone();
        assertNotSame(data, clone);
        assertEquals(data, clone);
        // modify clone and check original unchanged
        clone.setValue("A", 100.0);
        assertEquals(1.0, data.getValue("A").doubleValue(), 0.0001);
        clone.removeValue(0);
        assertEquals(2, data.getItemCount());
    }

    @Test
    public void testCloneIndependentIndexMap() throws CloneNotSupportedException {
        data.addValue("A", 1.0);
        DefaultKeyedValues clone = (DefaultKeyedValues) data.clone();
        clone.addValue("B", 2.0);
        assertEquals(1, data.getItemCount());
        assertEquals(-1, data.getIndex("B"));
    }

    @Test
    public void testRebuildIndexAfterInsert() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.insertValue(0, "C", 3.0);
        assertEquals(0, data.getIndex("C"));
        assertEquals(1, data.getIndex("A"));
        assertEquals(2, data.getIndex("B"));
    }

    @Test
    public void testRebuildIndexAfterRemoveNotLast() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);
        data.removeValue(1); // remove B
        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("C"));
        assertEquals(-1, data.getIndex("B"));
    }

    @Test
    public void testNoRebuildIndexAfterRemoveLast() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.removeValue(1); // remove B (last)
        assertEquals(0, data.getIndex("A"));
        assertEquals(-1, data.getIndex("B"));
    }

    @Test
    public void testGetItemCountAfterOperations() {
        assertEquals(0, data.getItemCount());
        data.addValue("A", 1.0);
        assertEquals(1, data.getItemCount());
        data.addValue("B", 2.0);
        assertEquals(2, data.getItemCount());
        data.removeValue(0);
        assertEquals(1, data.getItemCount());
        data.clear();
        assertEquals(0, data.getItemCount());
    }

    @Test
    public void testInsertValueAtEndWithNewKey() {
        data.addValue("A", 1.0);
        data.insertValue(1, "B", 2.0);
        assertEquals(2, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
    }

    @Test
    public void testInsertValueAtEndWithExistingKey() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.insertValue(2, "A", 10.0); // move A to end
        assertEquals(2, data.getItemCount());
        assertEquals("B", data.getKey(0));
        assertEquals("A", data.getKey(1));
        assertEquals(10.0, data.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testSetValueDoesNotChangeIndexForExistingKey() {
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.setValue("A", 100.0);
        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("B"));
    }

    @Test
    public void testAddValueWithDoubleAndNumber() {
        data.addValue("A", 1.0);
        data.addValue("B", new Double(2.0));
        assertEquals(2, data.getItemCount());
        assertEquals(1.0, data.getValue("A").doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue("B").doubleValue(), 0.0001);
    }

    @Test
    public void testSetValueWithDoubleAndNumber() {
        data.setValue("A", 1.0);
        data.setValue("B", new Double(2.0));
        assertEquals(2, data.getItemCount());
        assertEquals(1.0, data.getValue("A").doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue("B").doubleValue(), 0.0001);
    }

    @Test
    public void testInsertValueWithDoubleAndNumber() {
        data.insertValue(0, "A", 1.0);
        data.insertValue(1, "B", new Double(2.0));
        assertEquals(2, data.getItemCount());
        assertEquals(1.0, data.getValue(0).doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue(1).doubleValue(), 0.0001);
    }
}
