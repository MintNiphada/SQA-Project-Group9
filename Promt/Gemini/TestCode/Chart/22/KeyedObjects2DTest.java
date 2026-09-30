package org.jfree.data;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class KeyedObjects2DTest {

    @Test
    public void testEmptyInstance() {
        KeyedObjects2D data = new KeyedObjects2D();
        Assert.assertEquals(0, data.getRowCount());
        Assert.assertEquals(0, data.getColumnCount());
        Assert.assertTrue(data.getRowKeys().isEmpty());
        Assert.assertTrue(data.getColumnKeys().isEmpty());
        Assert.assertEquals(-1, data.getRowIndex("R1"));
        Assert.assertEquals(-1, data.getColumnIndex("C1"));
    }

    @Test
    public void testAddAndGetObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Value1", "R1", "C1");

        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(1, data.getColumnCount());
        Assert.assertEquals("R1", data.getRowKey(0));
        Assert.assertEquals("C1", data.getColumnKey(0));
        Assert.assertEquals(0, data.getRowIndex("R1"));
        Assert.assertEquals(0, data.getColumnIndex("C1"));

        Assert.assertEquals("Value1", data.getObject(0, 0));
        Assert.assertEquals("Value1", data.getObject("R1", "C1"));

        data.addObject("Value2", "R1", "C2");
        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertEquals("Value1", data.getObject(0, 0));
        Assert.assertEquals("Value2", data.getObject(0, 1));
        Assert.assertEquals("Value2", data.getObject("R1", "C2"));

        data.setObject("Value3", "R2", "C1");
        Assert.assertEquals(2, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertEquals("Value3", data.getObject(1, 0));
        Assert.assertEquals("Value3", data.getObject("R2", "C1"));
        Assert.assertNull(data.getObject(1, 1));
    }

    @Test
    public void testSetObjectOverwritesExisting() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        Assert.assertEquals("V1", data.getObject("R1", "C1"));

        data.setObject("V2", "R1", "C1");
        Assert.assertEquals("V2", data.getObject("R1", "C1"));
        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(1, data.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", null, "C1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Val", null, "C1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Val", "R1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectByKeyNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(null, "C1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectByKeyNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject("R1", null);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByUnknownRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.getObject("UnknownR", "C1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByUnknownColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.getObject("R1", "UnknownC");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetObjectByInvalidRowIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetObjectByInvalidColumnIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.getObject(0, 5);
    }

    @Test
    public void testGetKeysUnmodifiable() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");

        List rowKeys = data.getRowKeys();
        Assert.assertEquals(1, rowKeys.size());
        try {
            rowKeys.add("R2");
            Assert.fail("getRowKeys() should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        List colKeys = data.getColumnKeys();
        Assert.assertEquals(1, colKeys.size());
        try {
            colKeys.add("C2");
            Assert.fail("getColumnKeys() should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testRemoveObjectPartialRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R1", "C2");

        data.removeObject("R1", "C1");

        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals(2, data.getColumnCount());
        Assert.assertNull(data.getObject("R1", "C1"));
        Assert.assertEquals("V2", data.getObject("R1", "C2"));
    }

    @Test
    public void testRemoveObjectEntireRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R2", "C1");

        data.removeObject("R1", "C1");

        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals("R2", data.getRowKey(0));
        Assert.assertEquals(0, data.getRowIndex("R2"));
        Assert.assertEquals(-1, data.getRowIndex("R1"));
    }

    @Test
    public void testRemoveRowByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R2", "C1");

        data.removeRow(0);

        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals("R2", data.getRowKey(0));
        Assert.assertEquals(-1, data.getRowIndex("R1"));
    }

    @Test
    public void testRemoveRowByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R2", "C1");

        data.removeRow("R1");

        Assert.assertEquals(1, data.getRowCount());
        Assert.assertEquals("R2", data.getRowKey(0));
        Assert.assertEquals(-1, data.getRowIndex("R1"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRowByUnknownKeyThrowsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeRow("UnknownKey");
    }

    @Test
    public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R1", "C2");

        data.removeColumn(0);

        Assert.assertEquals(1, data.getColumnCount());
        Assert.assertEquals("C2", data.getColumnKey(0));
        Assert.assertEquals(-1, data.getColumnIndex("C1"));
        Assert.assertEquals("V2", data.getObject("R1", "C2"));
    }

    @Test
    public void testRemoveColumnByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V1", "R1", "C1");
        data.addObject("V2", "R2", "C1");
        data.addObject("V3", "R1", "C2");

        data.removeColumn("C1");

        Assert.assertEquals(1, data.getColumnCount());
        Assert.assertEquals("C2", data.getColumnKey(0));
        Assert.assertEquals(-1, data.getColumnIndex("C1"));
        Assert.assertEquals("V3", data.getObject("R1", "C2"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumnByUnknownKeyThrowsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeColumn("UnknownCol");
    }

    @Test
    public void testEqualsAndHashCode() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        KeyedObjects2D d2 = new KeyedObjects2D();

        Assert.assertTrue(d1.equals(d1));
        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d1.equals("NotAKeyedObjects2D"));
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1.addObject("A", "R1", "C1");
        Assert.assertFalse(d1.equals(d2));

        d2.addObject("A", "R1", "C1");
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1.addObject("B", "R1", "C2");
        d2.addObject("Different", "R1", "C2");
        Assert.assertFalse(d1.equals(d2));

        d2.setObject("B", "R1", "C2");
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        // Test with null values
        d1.setObject(null, "R2", "C1");
        d2.setObject("NotNull", "R2", "C1");
        Assert.assertFalse(d1.equals(d2));

        d2.setObject(null, "R2", "C1");
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        // Test row key differences
        KeyedObjects2D d3 = new KeyedObjects2D();
        d3.addObject("A", "R_Diff", "C1");
        KeyedObjects2D d4 = new KeyedObjects2D();
        d4.addObject("A", "R1", "C1");
        Assert.assertFalse(d3.equals(d4));

        // Test column key differences
        KeyedObjects2D d5 = new KeyedObjects2D();
        d5.addObject("A", "R1", "C_Diff");
        Assert.assertFalse(d4.equals(d5));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.addObject("V1", "R1", "C1");
        d1.addObject("V2", "R2", "C2");

        KeyedObjects2D d2 = (KeyedObjects2D) d1.clone();
        Assert.assertNotSame(d1, d2);
        Assert.assertTrue(d1.equals(d2));

        d1.setObject("Modified", "R1", "C1");
        Assert.assertEquals("Modified", d1.getObject("R1", "C1"));
        Assert.assertEquals("V1", d2.getObject("R1", "C1"));
        Assert.assertFalse(d1.equals(d2));
    }

    @Test
    public void testSerialization() throws Exception {
        KeyedObjects2D d1 = new KeyedObjects2D();
        d1.addObject("Val1", "R1", "C1");
        d1.addObject("Val2", "R2", "C2");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(d1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        KeyedObjects2D d2 = (KeyedObjects2D) in.readObject();
        in.close();

        Assert.assertNotSame(d1, d2);
        Assert.assertTrue(d1.equals(d2));
        Assert.assertEquals(d1.hashCode(), d2.hashCode());
        Assert.assertEquals("Val1", d2.getObject("R1", "C1"));
        Assert.assertEquals("Val2", d2.getObject("R2", "C2"));
    }
}