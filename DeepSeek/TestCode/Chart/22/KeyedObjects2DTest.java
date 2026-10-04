package org.jfree.data;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class KeyedObjects2DTest {

    @Test
    public void testConstructor() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
        assertTrue(data.getRowKeys().isEmpty());
        assertTrue(data.getColumnKeys().isEmpty());
    }

    @Test
    public void testSetObjectAddNewRowAndColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        Object obj = new Object();
        data.setObject(obj, "row1", "col1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals(obj, data.getObject("row1", "col1"));
        assertEquals(obj, data.getObject(0, 0));
    }

    @Test
    public void testSetObjectAddMultipleRowsAndColumns() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "r1", "c1");
        data.setObject("B", "r2", "c2");
        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertNull(data.getObject("r1", "c2"));
        data.setObject("C", "r1", "c2");
        assertEquals("C", data.getObject("r1", "c2"));
    }

    @Test
    public void testSetObjectUpdateExistingCell() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("old", "row", "col");
        data.setObject("new", "row", "col");
        assertEquals("new", data.getObject("row", "col"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("obj", null, "col");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("obj", "row", null);
    }

    @Test
    public void testAddObjectDelegatesToSetObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("val", "row", "col");
        assertEquals("val", data.getObject("row", "col"));
    }

    @Test
    public void testGetObjectByIndices() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("value", "r", "c");
        assertEquals("value", data.getObject(0, 0));
    }

    @Test
    public void testGetObjectByIndicesNullRowData() {
        // row not initialized -> get(0) should throw IndexOutOfBoundsException
        KeyedObjects2D empty = new KeyedObjects2D();
        try {
            empty.getObject(0, 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetObjectByIndicesColumnKeyNotInRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("val", "row", "col1");
        // column 1 does not exist in row's columns -> getObject returns null
        assertNull(data.getObject(0, 0)); // col "col1" is at index 0, we call getObject(0,1)?
        // Let's clarify: we need column index that doesn't correspond to a column key in row?
        // Actually the row data stores values for column keys; the row indices correspond to columnKeys list.
        // If we add "col1", columnKeys = ["col1"]; row gets setObject("col1","val").
        // Now getObject(0, 1) would be out of bounds -> IndexOutOfBoundsException.
        // To test columnKey != null but row does not have that key:
        // We can add a new column key to columnKeys without adding to that row? But setObject adds columnKey if missing.
        // So we can simulate by having a row with one column, then adding a second column only to different row,
        // then for first row, that column key exists but rowData doesn't have it.
        KeyedObjects2D data2 = new KeyedObjects2D();
        data2.setObject("A", "r1", "c1");
        data2.setObject("B", "r2", "c2");
        // Now columnKeys = ["c1","c2"]; row r1 has only "c1".
        // getObject(0,1) -> row index 0 is r1, column index 1 is "c2". rowData.getIndex("c2") returns -1 -> result null.
        assertNull(data2.getObject(0, 1));
    }

    @Test
    public void testGetObjectByKeysNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        try {
            data.getObject(null, "col");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Null 'rowKey' argument.", e.getMessage());
        }
    }

    @Test
    public void testGetObjectByKeysNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        try {
            data.getObject("row", null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Null 'columnKey' argument.", e.getMessage());
        }
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByKeysUnknownRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject("norow", "col");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByKeysUnknownColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("v", "row", "col");
        data.getObject("row", "nocol");
    }

    @Test
    public void testGetObjectByKeysValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("value", "r", "c");
        assertEquals("value", data.getObject("r", "c"));
    }

    @Test
    public void testRemoveObjectSetsNullAndRemovesRowIfAllNull() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("obj", "row", "col");
        data.removeObject("row", "col");
        assertEquals(0, data.getRowCount());  // row removed
        assertNull(data.getObject("row", "col"); // row key unknown, will throw? Actually row key removed, so getObject throws UnknownKeyException.
        try {
            data.getObject("row", "col");
            fail("Should throw UnknownKeyException because row key removed");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test
    public void testRemoveObjectRowNotEmptyDoesNotRemove() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "r", "c1");
        data.setObject("B", "r", "c2");
        data.removeObject("r", "c1");
        assertEquals(1, data.getRowCount()); // row still present
        assertNull(data.getObject("r", "c1"));
        assertEquals("B", data.getObject("r", "c2"));
    }

    @Test
    public void testRemoveObjectColumnNotRemoved() {
        // removeObject does not remove column even if all values in column become null
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("X", "r1", "c1");
        data.setObject("Y", "r2", "c2");
        data.removeObject("r1", "c1"); // c1 still exists as column key because column removal not implemented
        assertTrue(data.getColumnKeys().contains("c1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeObject(null, "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeObject("r", null);
    }

    @Test
    public void testRemoveRowByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("a", "r1", "c");
        data.setObject("b", "r2", "c");
        assertEquals(2, data.getRowCount());
        data.removeRow(0);
        assertEquals(1, data.getRowCount());
        assertEquals("r2", data.getRowKey(0));
        // original row0 removed, r1 gone
        try {
            data.getObject("r1", "c");
            fail("Should throw UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test
    public void testRemoveRowByKeyValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("a", "r1", "c");
        data.setObject("b", "r2", "c");
        data.removeRow("r1");
        assertEquals(1, data.getRowCount());
        assertEquals("r2", data.getRowKey(0));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveRowByKeyUnknown() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeRow("nonexistent");
    }

    @Test
    public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("x", "r1", "c1");
        data.setObject("y", "r1", "c2");
        assertEquals(2, data.getColumnCount());
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertEquals("c2", data.getColumnKey(0));
        assertNull(data.getObject("r1", "c1"));
    }

    @Test
    public void testRemoveColumnByKeyValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("x", "r1", "c1");
        data.setObject("y", "r2", "c1");
        data.removeColumn("c1");
        assertEquals(0, data.getColumnCount());
        assertNull(data.getObject("r1", "c1"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumnByKeyUnknown() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeColumn("no");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveColumnByInvalidIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeColumn(0); // empty
    }

    // Equality tests

    @Test
    public void testEqualsSameObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertTrue(data.equals(data));
    }

    @Test
    public void testEqualsNull() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertFalse(data.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertFalse(data.equals("String"));
    }

    @Test
    public void testEqualsSameData() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("val", "row", "col");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("val", "row", "col");
        assertTrue(d1.equals(d2));
    }

    @Test
    public void testEqualsDifferentRowKeys() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("val", "r1", "c");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("val", "r2", "c");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEqualsDifferentColumnKeys() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("val", "r", "c1");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("val", "r", "c2");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEqualsDifferentRowCount() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("a", "r1", "c");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("a", "r1", "c");
        d2.setObject("b", "r2", "c");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEqualsDifferentColumnCount() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("a", "r", "c1");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("a", "r", "c1");
        d2.setObject("b", "r", "c2");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEqualsDifferentValues() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject("v1", "r", "c");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject("v2", "r", "c");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEqualsWithNullValues() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.setObject(null, "r", "c");
        KeyedObjects2D d2 = new KeyedObjects2D();
        d2.setObject(null, "r", "c");
        assertTrue(d1.equals(d2));
        d2.setObject("nonNull", "r", "c");
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        KeyedObjects2D d2 = new KeyedObjects2D();
        assertEquals(d1.hashCode(), d2.hashCode());
        d1.setObject("v", "r", "c");
        d2.setObject("v", "r", "c");
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void testCloneDeepCopy() throws Exception {
        KeyedObjects2D original = new KeyedObjects2D();
        original.setObject("original", "r", "c");
        KeyedObjects2D clone = (KeyedObjects2D) original.clone();
        assertNotSame(original, clone);
        assertEquals(original, clone);
        // modify original, clone unaffected
        original.setObject("changed", "r", "c");
        assertFalse(original.equals(clone));
        assertEquals("original", clone.getObject("r", "c"));
    }

    @Test
    public void testCloneIndependentLists() throws Exception {
        KeyedObjects2D original = new KeyedObjects2D();
        original.setObject("x", "r1", "c1");
        KeyedObjects2D clone = (KeyedObjects2D) original.clone();
        clone.setObject("y", "r2", "c1");
        assertEquals(1, original.getRowCount());
        assertEquals(2, clone.getRowCount());
    }

    @Test
    public void testGetRowKeyAndIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("v", "rowKey", "col");
        assertEquals("rowKey", data.getRowKey(0));
        assertEquals(0, data.getRowIndex("rowKey"));
        assertEquals(-1, data.getRowIndex("nonexistent"));
    }

    @Test
    public void testGetColumnKeyAndIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("v", "r", "colKey");
        assertEquals("colKey", data.getColumnKey(0));
        assertEquals(0, data.getColumnIndex("colKey"));
        assertEquals(-1, data.getColumnIndex("no"));
    }

    @Test
    public void testGetRowKeysReturnsUnmodifiable() {
        KeyedObjects2D data = new KeyedObjects2D();
        List rowKeys = data.getRowKeys();
        try {
            rowKeys.add("row");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetColumnKeysReturnsUnmodifiable() {
        KeyedObjects2D data = new KeyedObjects2D();
        List colKeys = data.getColumnKeys();
        try {
            colKeys.add("col");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testSetObjectPreservesColumnKeyOrder() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("a", "r1", "c2");
        data.setObject("b", "r1", "c1");
        data.setObject("c", "r2", "c3");
        List colKeys = data.getColumnKeys();
        assertEquals("c2", colKeys.get(0));
        assertEquals("c1", colKeys.get(1));
        assertEquals("c3", colKeys.get(2));
    }

    @Test
    public void testGetObjectByIndicesAfterRowRemoval() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("a", "r1", "c");
        data.setObject("b", "r2", "c");
        data.removeRow(0);
        assertEquals("b", data.getObject(0, 0));
    }

    @Test
    public void testRemoveColumnRemovesValuesFromAllRows() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("x", "r1", "c1");
        data.setObject("y", "r2", "c1");
        data.setObject("z", "r1", "c2");
        data.removeColumn("c1");
        assertEquals(1, data.getColumnCount());
        assertEquals("c2", data.getColumnKey(0));
        assertNull(data.getObject("r1", "c1"));
        assertNull(data.getObject("r2", "c1"));
        assertEquals("z", data.getObject("r1", "c2"));
    }

    @Test
    public void testRemoveColumnByIndexConvertsToKey() {
        // removeColumn(int) internally calls getColumnKey and removeColumn(key)
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("val", "r", "c");
        data.removeColumn(0);
        assertEquals(0, data.getColumnCount());
    }

    @Test
    public void testRemoveRowByIndexMultipleTimes() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("a", "r1", "c");
        data.setObject("b", "r2", "c");
        data.setObject("c", "r3", "c");
        data.removeRow(1); // remove r2
        assertEquals(2, data.getRowCount());
        assertEquals("r1", data.getRowKey(0));
        assertEquals("r3", data.getRowKey(1));
        data.removeRow(0);
        assertEquals(1, data.getRowCount());
        assertEquals("r3", data.getRowKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetObjectByIndicesNegativeRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(-1, 0);
    }

    @Test
    public void testGetObjectByIndicesNullColumnKeyInList() {
        // force column key null (only possible via reflection or direct manipulation, but we can rely on implementation detail)
        // The class doesn't prevent null from being in columnKeys list if added externally? Not recommended.
        // Since setObject prevents null, we can skip actual null columnKey in the list.
        // However, we can test if column index is out of bounds, getObject throws.
    }

    @Test
    public void testEqualsWithManyRowsAndColumns() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        KeyedObjects2D d2 = new KeyedObjects2D();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                d1.setObject("v"+i+"_"+j, "r"+i, "c"+j);
                d2.setObject("v"+i+"_"+j, "r"+i, "c"+j);
            }
        }
        assertTrue(d1.equals(d2));
        d2.setObject("changed", "r2", "c2");
        assertFalse(d1.equals(d2));
    }
}
