package org.jfree.data;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class DefaultKeyedValues2DTest {

    @Test
    public void testConstructorAndInitialState() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        Assert.assertEquals(0, data.getRowCount());
        Assert.assertEquals(0, data.getColumnCount());
        Assert.assertNotNull(data.getRowKeys());
        Assert.assertTrue(data.getRowKeys().isEmpty());
        Assert.assertNotNull(data.getColumnKeys());
        Assert.assertTrue(data.getColumnKeys().isEmpty());

        DefaultKeyedValues2D sortedData = new DefaultKeyedValues2D(true);
        Assert.assertEquals(0, sortedData.getRowCount());
        Assert.assertEquals(0, sortedData.getColumnCount());
    }

    @Test
    public void testAddAndSetValueUnsorted() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D(false);
        data.addValue(1.0, "R2", "C1");
        data.setValue(2.0, "R1", "C2");
        data.addValue(3.0, "R2", "C2");

        Assert.assertEquals(2, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());

        Assert.assertEquals("R2", data.getRowKey(0));
        Assert.assertEquals("R1", data.getRowKey(1));

        Assert.assertEquals("C1", data.getColumnKey(0));
        Assert.assertEquals("C2", data.getColumnKey(1));

        Assert.assertEquals(1.0, data.getValue(0, 0));
        Assert.assertEquals(3.0, data.getValue(0, 1));
        Assert.assertNull(data.getValue(1, 0));
        Assert.assertEquals(2.0, data.getValue(1, 1));

        // Overwrite existing value
        data.setValue(4.0, "R2", "C1");
        Assert.assertEquals(4.0, data.getValue("R2", "C1"));
    }

    @Test
    public void testSetValueSorted() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D(true);
        data.setValue(1.0, "R3", "C1");
        data.setValue(2.0, "R1", "C1");
        data.setValue(3.0, "R2", "C1");

        Assert.assertEquals(3, data.getRowCount());
        Assert.assertEquals("R1", data.getRowKey(0));
        Assert.assertEquals("R2", data.getRowKey(1));
        Assert.assertEquals("R3", data.getRowKey(2));

        Assert.assertEquals(0, data.getRowIndex("R1"));
        Assert.assertEquals(1, data.getRowIndex("R2"));
        Assert.assertEquals(2, data.getRowIndex("R3"));

        Assert.assertTrue(data.getRowIndex("R0") < 0);
        Assert.assertTrue(data.getRowIndex("R4") < 0);

        // Update value in sorted container
        data.setValue(20.0, "R2", "C1");
        Assert.assertEquals(20.0, data.getValue("R2", "C1"));
    }

    @Test
    public void testGetRowIndexAndColumnIndex() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");

        Assert.assertEquals(0, data.getRowIndex("R1"));
        Assert.assertEquals(1, data.getRowIndex("R2"));
        Assert.assertEquals(-1, data.getRowIndex("R3"));

        Assert.assertEquals(0, data.getColumnIndex("C1"));
        Assert.assertEquals(1, data.getColumnIndex("C2"));
        Assert.assertEquals(-1, data.getColumnIndex("C3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowIndexNullKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getRowIndex(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexNullKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getColumnIndex(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueNullRowKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.getValue(null, "C1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueNullColumnKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.getValue("R1", null);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownColumnKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.getValue("R1", "UnknownCol");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownRowKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.getValue("UnknownRow", "C1");
    }

    @Test
    public void testGetValueByKeys() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");

        Assert.assertEquals(1.0, data.getValue("R1", "C1"));
        Assert.assertNull(data.getValue("R1", "C2"));
        Assert.assertNull(data.getValue("R2", "C1"));
        Assert.assertEquals(2.0, data.getValue("R2", "C2"));
    }

    @Test
    public void testGetRowKeysAndColumnKeysUnmodifiable() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");

        List rowKeys = data.getRowKeys();
        List colKeys = data.getColumnKeys();

        Assert.assertEquals(1, rowKeys.size());
        Assert.assertEquals(1, colKeys.size());

        try {
            rowKeys.add("R2");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            colKeys.add("C2");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testRemoveValue() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        data.addValue(3.0, "R2", "C1");
        data.addValue(4.0, "R2", "C2");

        // Remove a value where row and col still have remaining entries
        data.removeValue("R1", "C1");
        Assert.assertEquals(2, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertNull(data.getValue("R1", "C1"));
        Assert.assertEquals(2.0, data.getValue("R1", "C2"));

        // Remove another value, causing row R1 to be entirely null -> R1 removed
        data.removeValue("R1", "C2");
        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertEquals("R2", data.getRowKey(0));

        // Remove R2, C1 -> C1 becomes entirely null -> C1 removed
        data.removeValue("R2", "C1");
        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(1, data.getColumnCount());
        Assert.assertEquals("C2", data.getColumnKey(0));

        // Remove the last value -> entire table becomes empty
        data.removeValue("R2", "C2");
        Assert.assertEquals(0, data.getRowCount());
        Assert.assertEquals(0, data.getColumnCount());
    }

    @Test
    public void testRemoveRowByIndexAndKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        data.addValue(3.0, "R3", "C1");

        data.removeRow(1); // removes R2
        Assert.assertEquals(2, data.getRowCount());
        Assert.assertEquals("R1", data.getRowKey(0));
        Assert.assertEquals("R3", data.getRowKey(1));

        data.removeRow("R1"); // removes R1
        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals("R3", data.getRowKey(0));
    }

    @Test
    public void testRemoveColumnByIndexAndKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        data.addValue(3.0, "R1", "C3");

        data.removeColumn(1); // removes C2
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertEquals("C1", data.getColumnKey(0));
        Assert.assertEquals("C3", data.getColumnKey(1));

        data.removeColumn("C1"); // removes C1
        Assert.assertEquals(1, data.getColumnCount());
        Assert.assertEquals("C3", data.getColumnKey(0));
    }

    @Test
    public void testClear() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");
        Assert.assertEquals(2, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());

        data.clear();
        Assert.assertEquals(0, data.getRowCount());
        Assert.assertEquals(0, data.getColumnCount());
        Assert.assertEquals(0, data.getRowKeys().size());
        Assert.assertEquals(0, data.getColumnKeys().size());
    }

    @Test
    public void testEqualsAndHashCode() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();

        Assert.assertTrue(d1.equals(d1));
        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d1.equals("Not a KeyedValues2D"));

        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1.addValue(1.0, "R1", "C1");
        Assert.assertFalse(d1.equals(d2));

        d2.addValue(1.0, "R1", "C1");
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        // Different values
        d1.setValue(2.0, "R1", "C1");
        Assert.assertFalse(d1.equals(d2));
        d2.setValue(2.0, "R1", "C1");
        Assert.assertTrue(d1.equals(d2));

        // One null value vs non-null value
        d1.setValue(null, "R1", "C1");
        Assert.assertFalse(d1.equals(d2));
        d2.setValue(null, "R1", "C1");
        Assert.assertTrue(d1.equals(d2));

        // Different row keys
        DefaultKeyedValues2D d3 = new DefaultKeyedValues2D();
        d3.addValue(1.0, "R2", "C1");
        DefaultKeyedValues2D d4 = new DefaultKeyedValues2D();
        d4.addValue(1.0, "R1", "C1");
        Assert.assertFalse(d3.equals(d4));

        // Different col keys
        DefaultKeyedValues2D d5 = new DefaultKeyedValues2D();
        d5.addValue(1.0, "R1", "C2");
        Assert.assertFalse(d4.equals(d5));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        DefaultKeyedValues2D original = new DefaultKeyedValues2D();
        original.addValue(10.0, "R1", "C1");
        original.addValue(20.0, "R2", "C2");

        DefaultKeyedValues2D cloned = (DefaultKeyedValues2D) original.clone();
        Assert.assertNotSame(original, cloned);
        Assert.assertTrue(original.equals(cloned));

        // Modifying clone does not alter original
        cloned.setValue(99.0, "R1", "C1");
        Assert.assertFalse(original.equals(cloned));
        Assert.assertEquals(10.0, original.getValue("R1", "C1"));
        Assert.assertEquals(99.0, cloned.getValue("R1", "C1"));
    }

    @Test
    public void testSerialization() throws Exception {
        DefaultKeyedValues2D original = new DefaultKeyedValues2D();
        original.addValue(1.5, "R1", "C1");
        original.addValue(2.5, "R2", "C2");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        DefaultKeyedValues2D deserialized = (DefaultKeyedValues2D) in.readObject();
        in.close();

        Assert.assertNotSame(original, deserialized);
        Assert.assertTrue(original.equals(deserialized));
        Assert.assertEquals(original.hashCode(), deserialized.hashCode());
    }
}