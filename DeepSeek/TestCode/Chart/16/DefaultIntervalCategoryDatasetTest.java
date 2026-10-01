package org.jfree.data.category;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.data.UnknownKeyException;

public class DefaultIntervalCategoryDatasetTest {

    private DefaultIntervalCategoryDataset dataset;
    private Number[][] startData;
    private Number[][] endData;
    private Comparable[] seriesKeys = {"Series A", "Series B"};
    private Comparable[] categoryKeys = {"Cat 1", "Cat 2", "Cat 3"};

    @Before
    public void setUp() {
        startData = new Number[][] {
            {1, 2, 3},
            {4, 5, 6}
        };
        endData = new Number[][] {
            {7, 8, 9},
            {10, 11, 12}
        };
        dataset = new DefaultIntervalCategoryDataset(seriesKeys, categoryKeys, startData, endData);
    }

    // --- Constructor tests ---

    @Test
    public void testConstructorDoubleArrayValid() {
        double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] ends = {{5.0, 6.0}, {7.0, 8.0}};
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(starts, ends);
        assertEquals(2, ds.getSeriesCount());
        assertEquals(2, ds.getCategoryCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleArrayMismatchedSeriesLength() {
        double[][] starts = {{1.0, 2.0}};
        double[][] ends = {{1.0, 2.0}, {3.0, 4.0}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test
    public void testConstructorNumberArrayWithNullKeys() {
        Number[][] starts = {{1, 2}, {3, 4}};
        Number[][] ends = {{5, 6}, {7, 8}};
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(starts, ends);
        assertEquals(2, ds.getSeriesCount());
        assertEquals(2, ds.getCategoryCount());
        assertNotNull(ds.getRowKeys());
        assertNotNull(ds.getColumnKeys());
    }

    @Test
    public void testConstructorWithNullStartsAndEnds() {
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset((Number[][]) null, (Number[][]) null);
        assertEquals(0, ds.getSeriesCount());
        assertEquals(0, ds.getCategoryCount());
    }

    @Test
    public void testConstructorStringSeriesNames() {
        String[] names = {"S1", "S2"};
        Number[][] starts = {{1, 2}, {3, 4}};
        Number[][] ends = {{5, 6}, {7, 8}};
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(names, starts, ends);
        assertEquals(2, ds.getSeriesCount());
        assertEquals("S1", ds.getSeriesKey(0));
        assertEquals("S2", ds.getSeriesKey(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedSeriesKeys() {
        Comparable[] wrongSeriesKeys = {"One"};
        new DefaultIntervalCategoryDataset(wrongSeriesKeys, categoryKeys, startData, endData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCategoryKeys() {
        Comparable[] wrongCategoryKeys = {"A", "B"};
        new DefaultIntervalCategoryDataset(seriesKeys, wrongCategoryKeys, startData, endData);
    }

    @Test
    public void testConstructorEmptyData() {
        Number[][] empty = {};
        Number[][] empty2 = {};
        // This should set seriesKeys and categoryKeys to null
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset((Comparable[]) null, (Comparable[]) null, empty, empty2);
        assertEquals(0, ds.getSeriesCount());
        assertEquals(0, ds.getCategoryCount());
        // getRowCount should throw NullPointerException because seriesKeys is null
        try {
            ds.getRowCount();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            ds.getColumnCount();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // --- getSeriesCount / getRowCount / getColumnCount ---

    @Test
    public void testGetSeriesCount() {
        assertEquals(2, dataset.getSeriesCount());
    }

    @Test
    public void testGetRowCount() {
        assertEquals(2, dataset.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(3, dataset.getColumnCount());
    }

    // --- getSeriesIndex / getSeriesKey ---

    @Test
    public void testGetSeriesIndex() {
        assertEquals(0, dataset.getSeriesIndex("Series A"));
        assertEquals(1, dataset.getSeriesIndex("Series B"));
        assertEquals(-1, dataset.getSeriesIndex("NonExistent"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetSeriesIndexNull() {
        dataset.getSeriesIndex(null);
    }

    @Test
    public void testGetSeriesKey() {
        assertEquals("Series A", dataset.getSeriesKey(0));
        assertEquals("Series B", dataset.getSeriesKey(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSeriesKeyNegative() {
        dataset.getSeriesKey(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSeriesKeyTooLarge() {
        dataset.getSeriesKey(2);
    }

    // --- setSeriesKeys ---

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysNull() {
        dataset.setSeriesKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysWrongLength() {
        dataset.setSeriesKeys(new Comparable[] {"OnlyOne"});
    }

    @Test
    public void testSetSeriesKeysValid() {
        Comparable[] newKeys = {"New A", "New B"};
        dataset.setSeriesKeys(newKeys);
        assertEquals("New A", dataset.getSeriesKey(0));
    }

    // --- getCategoryCount / getColumnKeys ---

    @Test
    public void testGetCategoryCount() {
        assertEquals(3, dataset.getCategoryCount());
    }

    @Test
    public void testGetColumnKeys() {
        List keys = dataset.getColumnKeys();
        assertEquals(3, keys.size());
        assertEquals("Cat 1", keys.get(0));
        // list should be unmodifiable
        try {
            keys.add("Another");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetColumnKeysEmptyDataset() {
        DefaultIntervalCategoryDataset empty = new DefaultIntervalCategoryDataset(new Number[0][0], new Number[0][0]);
        List keys = empty.getColumnKeys();
        assertTrue(keys instanceof ArrayList);
        assertEquals(0, keys.size());
    }

    // --- setCategoryKeys ---

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysNull() {
        dataset.setCategoryKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWrongLength() {
        dataset.setCategoryKeys(new Comparable[] {"A", "B"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysNullElement() {
        dataset.setCategoryKeys(new Comparable[] {"A", null, "C"});
    }

    @Test
    public void testSetCategoryKeysValid() {
        Comparable[] newKeys = {"X", "Y", "Z"};
        dataset.setCategoryKeys(newKeys);
        assertEquals("X", dataset.getColumnKey(0));
    }

    // --- getValue (Comparable, Comparable) and (int, int) ---

    @Test
    public void testGetValueByKey() {
        assertEquals(7, dataset.getValue("Series A", "Cat 1"));
        assertEquals(12, dataset.getValue("Series B", "Cat 3"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueInvalidSeries() {
        dataset.getValue("BadSeries", "Cat 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueInvalidCategory() {
        dataset.getValue("Series A", "BadCat");
    }

    @Test
    public void testGetValueByIndex() {
        assertEquals(7, dataset.getValue(0, 0));
        assertEquals(12, dataset.getValue(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByIndexInvalidSeries() {
        dataset.getValue(2, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByIndexInvalidCategory() {
        dataset.getValue(0, 3);
    }

    // --- getStartValue (Comparable, Comparable) and (int, int) ---

    @Test
    public void testGetStartValueByKey() {
        assertEquals(1, dataset.getStartValue("Series A", "Cat 1"));
        assertEquals(6, dataset.getStartValue("Series B", "Cat 3"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueInvalidSeries() {
        dataset.getStartValue("BadSeries", "Cat 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueInvalidCategory() {
        dataset.getStartValue("Series A", "BadCat");
    }

    @Test
    public void testGetStartValueByIndex() {
        assertEquals(1, dataset.getStartValue(0, 0));
        assertEquals(6, dataset.getStartValue(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueByIndexBadSeries() {
        dataset.getStartValue(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueByIndexBadCategory() {
        dataset.getStartValue(0, -1);
    }

    // --- getEndValue (Comparable, Comparable) and (int, int) ---

    @Test
    public void testGetEndValueByKey() {
        assertEquals(7, dataset.getEndValue("Series A", "Cat 1"));
        assertEquals(12, dataset.getEndValue("Series B", "Cat 3"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueInvalidSeries() {
        dataset.getEndValue("BadSeries", "Cat 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueInvalidCategory() {
        dataset.getEndValue("Series A", "BadCat");
    }

    @Test
    public void testGetEndValueByIndex() {
        assertEquals(7, dataset.getEndValue(0, 0));
        assertEquals(12, dataset.getEndValue(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueByIndexBadSeries() {
        dataset.getEndValue(5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueByIndexBadCategory() {
        dataset.getEndValue(0, 100);
    }

    // --- setStartValue / setEndValue ---

    @Test
    public void testSetStartValueValid() {
        dataset.setStartValue(0, "Cat 1", 100);
        assertEquals(100, dataset.getStartValue(0, 0).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueInvalidSeries() {
        dataset.setStartValue(-1, "Cat 1", 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueInvalidCategory() {
        dataset.setStartValue(0, "None", 100);
    }

    @Test
    public void testSetEndValueValid() {
        dataset.setEndValue(1, "Cat 2", 200);
        assertEquals(200, dataset.getEndValue(1, 1).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueInvalidSeries() {
        dataset.setEndValue(2, "Cat 1", 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueInvalidCategory() {
        dataset.setEndValue(0, "Invalid", 50);
    }

    // --- getCategoryIndex / getColumnIndex / getRowIndex ---

    @Test
    public void testGetCategoryIndex() {
        assertEquals(0, dataset.getCategoryIndex("Cat 1"));
        assertEquals(2, dataset.getCategoryIndex("Cat 3"));
        assertEquals(-1, dataset.getCategoryIndex("Missing"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetCategoryIndexNull() {
        dataset.getCategoryIndex(null);
    }

    @Test
    public void testGetColumnIndex() {
        assertEquals(1, dataset.getColumnIndex("Cat 2"));
        assertEquals(-1, dataset.getColumnIndex("NoCat"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexNull() {
        dataset.getColumnIndex(null);
    }

    @Test
    public void testGetRowIndex() {
        assertEquals(0, dataset.getRowIndex("Series A"));
        assertEquals(-1, dataset.getRowIndex("Other"));
    }

    // --- getColumnKey / getRowKey ---

    @Test
    public void testGetColumnKey() {
        assertEquals("Cat 1", dataset.getColumnKey(0));
        assertEquals("Cat 3", dataset.getColumnKey(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetColumnKeyOutOfBounds() {
        dataset.getColumnKey(3);
    }

    @Test
    public void testGetRowKey() {
        assertEquals("Series A", dataset.getRowKey(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowKeyNegative() {
        dataset.getRowKey(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowKeyTooLarge() {
        dataset.getRowKey(dataset.getRowCount());
    }

    // --- getRowKeys ---

    @Test
    public void testGetRowKeys() {
        List keys = dataset.getRowKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("Series A"));
        // unmodifiable check
        try {
            keys.add("Extra");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetRowKeysEmptyDataset() {
        DefaultIntervalCategoryDataset empty = new DefaultIntervalCategoryDataset(new Number[0][0], new Number[0][0]);
        List keys = empty.getRowKeys();
        assertTrue(keys instanceof ArrayList);
        assertEquals(0, keys.size());
    }

    // --- equals ---

    @Test
    public void testEqualsSameObject() {
        assertTrue(dataset.equals(dataset));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(dataset.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(dataset.equals(new Object()));
    }

    @Test
    public void testEqualsIdentical() {
        DefaultIntervalCategoryDataset other = new DefaultIntervalCategoryDataset(
                seriesKeys, categoryKeys, startData, endData);
        assertTrue(dataset.equals(other));
    }

    @Test
    public void testEqualsDifferentSeriesKeys() {
        DefaultIntervalCategoryDataset other = new DefaultIntervalCategoryDataset(
                new Comparable[]{"X", "Y"}, categoryKeys, startData, endData);
        assertFalse(dataset.equals(other));
    }

    @Test
    public void testEqualsDifferentCategoryKeys() {
        DefaultIntervalCategoryDataset other = new DefaultIntervalCategoryDataset(
                seriesKeys, new Comparable[]{"A", "B", "C"}, startData, endData);
        assertFalse(dataset.equals(other));
    }

    @Test
    public void testEqualsDifferentStartData() {
        Number[][] differentStart = {{10, 20, 30}, {40, 50, 60}};
        DefaultIntervalCategoryDataset other = new DefaultIntervalCategoryDataset(
                seriesKeys, categoryKeys, differentStart, endData);
        assertFalse(dataset.equals(other));
    }

    @Test
    public void testEqualsDifferentEndData() {
        Number[][] differentEnd = {{70, 80, 90}, {100, 110, 120}};
        DefaultIntervalCategoryDataset other = new DefaultIntervalCategoryDataset(
                seriesKeys, categoryKeys, startData, differentEnd);
        assertFalse(dataset.equals(other));
    }

    // --- clone ---

    @Test
    public void testClone() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) dataset.clone();
        assertNotNull(clone);
        assertNotSame(dataset, clone);
        assertTrue(dataset.equals(clone));
        // deep copy check: change original start data, clone should not be affected
        dataset.setStartValue(0, "Cat 1", 999);
        assertFalse(dataset.equals(clone));
    }

    @Test
    public void testCloneDeepCopyArrays() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) dataset.clone();
        // verify that arrays are different objects
        assertFalse(clone.startData == dataset.startData);
        assertFalse(clone.endData == dataset.endData);
        assertFalse(clone.categoryKeys == dataset.categoryKeys);
        assertFalse(clone.seriesKeys == dataset.seriesKeys);
    }
}
