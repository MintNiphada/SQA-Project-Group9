package org.jfree.data.category;

import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DefaultIntervalCategoryDatasetTest {

    @Test
    public void testDoubleArrayConstructorAndBasicGetters() {
        double[][] starts = new double[][] {{0.1, 0.2}, {0.3, 0.4}};
        double[][] ends = new double[][] {{1.1, 1.2}, {1.3, 1.4}};

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        Assert.assertEquals(2, dataset.getSeriesCount());
        Assert.assertEquals(2, dataset.getRowCount());
        Assert.assertEquals(2, dataset.getCategoryCount());
        Assert.assertEquals(2, dataset.getColumnCount());

        Assert.assertEquals("Series 1", dataset.getSeriesKey(0));
        Assert.assertEquals("Series 2", dataset.getSeriesKey(1));
        Assert.assertEquals("Category 1", dataset.getColumnKey(0));
        Assert.assertEquals("Category 2", dataset.getColumnKey(1));

        Assert.assertEquals(0.1, dataset.getStartValue(0, 0).doubleValue(), 1e-9);
        Assert.assertEquals(1.1, dataset.getEndValue(0, 0).doubleValue(), 1e-9);
        Assert.assertEquals(1.1, dataset.getValue(0, 0).doubleValue(), 1e-9);

        Assert.assertEquals(0.1, dataset.getStartValue("Series 1", "Category 1").doubleValue(), 1e-9);
        Assert.assertEquals(1.1, dataset.getEndValue("Series 1", "Category 1").doubleValue(), 1e-9);
        Assert.assertEquals(1.1, dataset.getValue("Series 1", "Category 1").doubleValue(), 1e-9);
    }

    @Test
    public void testStringSeriesNamesConstructor() {
        String[] seriesNames = new String[] {"S1", "S2"};
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{10, 20}, {30, 40}};

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesNames, starts, ends);

        Assert.assertEquals(2, dataset.getSeriesCount());
        Assert.assertEquals("S1", dataset.getSeriesKey(0));
        Assert.assertEquals("S2", dataset.getSeriesKey(1));
        Assert.assertEquals("Category 1", dataset.getColumnKey(0));
        Assert.assertEquals("Category 2", dataset.getColumnKey(1));
    }

    @Test
    public void testEmptyAndNullConstructors() {
        DefaultIntervalCategoryDataset emptyDs = new DefaultIntervalCategoryDataset(new Number[0][0], new Number[0][0]);
        Assert.assertEquals(0, emptyDs.getSeriesCount());
        Assert.assertEquals(0, emptyDs.getCategoryCount());
        Assert.assertNull(emptyDs.getRowKeys() != null && emptyDs.getRowKeys().isEmpty() ? null : "not-empty");
        Assert.assertTrue(emptyDs.getRowKeys().isEmpty());
        Assert.assertTrue(emptyDs.getColumnKeys().isEmpty());

        DefaultIntervalCategoryDataset nullDs = new DefaultIntervalCategoryDataset((Number[][]) null, (Number[][]) null);
        Assert.assertEquals(0, nullDs.getSeriesCount());
        Assert.assertEquals(0, nullDs.getCategoryCount());
        Assert.assertTrue(nullDs.getRowKeys().isEmpty());
        Assert.assertTrue(nullDs.getColumnKeys().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSeriesMismatch() {
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{1, 2}, {3, 4}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCategoryMismatch() {
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{1, 2, 3}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSeriesKeysMismatch() {
        Comparable[] seriesKeys = new Comparable[] {"S1"};
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{1, 2}, {3, 4}};
        new DefaultIntervalCategoryDataset(seriesKeys, null, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCategoryKeysMismatch() {
        Comparable[] catKeys = new Comparable[] {"C1"};
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{1, 2}};
        new DefaultIntervalCategoryDataset(null, catKeys, starts, ends);
    }

    @Test
    public void testIndicesAndKeys() {
        Comparable[] seriesKeys = new Comparable[] {"S1", "S2"};
        Comparable[] catKeys = new Comparable[] {"C1", "C2", "C3"};
        Number[][] starts = new Number[][] {{1, 2, 3}, {4, 5, 6}};
        Number[][] ends = new Number[][] {{10, 20, 30}, {40, 50, 60}};

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesKeys, catKeys, starts, ends);

        Assert.assertEquals(0, dataset.getSeriesIndex("S1"));
        Assert.assertEquals(1, dataset.getSeriesIndex("S2"));
        Assert.assertEquals(-1, dataset.getSeriesIndex("Unknown"));

        Assert.assertEquals(0, dataset.getRowIndex("S1"));
        Assert.assertEquals("S1", dataset.getRowKey(0));
        Assert.assertEquals("S2", dataset.getRowKey(1));

        Assert.assertEquals(0, dataset.getCategoryIndex("C1"));
        Assert.assertEquals(2, dataset.getCategoryIndex("C3"));
        Assert.assertEquals(-1, dataset.getCategoryIndex("Unknown"));

        Assert.assertEquals(1, dataset.getColumnIndex("C2"));
        Assert.assertEquals("C2", dataset.getColumnKey(1));

        List rowKeys = dataset.getRowKeys();
        Assert.assertEquals(2, rowKeys.size());
        Assert.assertEquals("S1", rowKeys.get(0));

        List colKeys = dataset.getColumnKeys();
        Assert.assertEquals(3, colKeys.size());
        Assert.assertEquals("C3", colKeys.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSeriesKeyOutOfBoundsLower() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getSeriesKey(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSeriesKeyOutOfBoundsUpper() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getSeriesKey(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowKeyOutOfBounds() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getRowKey(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexNull() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getColumnIndex(null);
    }

    @Test
    public void testSetSeriesKeys() {
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{10, 20}, {30, 40}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        final boolean[] notified = new boolean[] {false};
        dataset.addChangeListener(new DatasetChangeListener() {
            public void datasetChanged(DatasetChangeEvent event) {
                notified[0] = true;
            }
        });

        dataset.setSeriesKeys(new Comparable[] {"NewS1", "NewS2"});
        Assert.assertTrue(notified[0]);
        Assert.assertEquals("NewS1", dataset.getSeriesKey(0));
        Assert.assertEquals("NewS2", dataset.getSeriesKey(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysNull() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysWrongLength() {
        Number[][] starts = new Number[][] {{1}, {2}};
        Number[][] ends = new Number[][] {{2}, {3}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(new Comparable[] {"OnlyOne"});
    }

    @Test
    public void testSetCategoryKeys() {
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{10, 20}, {30, 40}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        final boolean[] notified = new boolean[] {false};
        dataset.addChangeListener(new DatasetChangeListener() {
            public void datasetChanged(DatasetChangeEvent event) {
                notified[0] = true;
            }
        });

        dataset.setCategoryKeys(new Comparable[] {"NewC1", "NewC2"});
        Assert.assertTrue(notified[0]);
        Assert.assertEquals("NewC1", dataset.getColumnKey(0));
        Assert.assertEquals("NewC2", dataset.getColumnKey(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysNull() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWrongLength() {
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{2, 3}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(new Comparable[] {"OnlyOne"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWithNullElement() {
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{2, 3}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(new Comparable[] {"C1", null});
    }

    @Test
    public void testSetStartAndEndValues() {
        Comparable[] seriesKeys = new Comparable[] {"S1"};
        Comparable[] catKeys = new Comparable[] {"C1"};
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesKeys, catKeys, starts, ends);

        final int[] changeCount = new int[] {0};
        dataset.addChangeListener(new DatasetChangeListener() {
            public void datasetChanged(DatasetChangeEvent event) {
                changeCount[0]++;
            }
        });

        dataset.setStartValue(0, "C1", 5);
        Assert.assertEquals(5, dataset.getStartValue(0, 0));
        Assert.assertEquals(1, changeCount[0]);

        dataset.setEndValue(0, "C1", 15);
        Assert.assertEquals(15, dataset.getEndValue(0, 0));
        Assert.assertEquals(2, changeCount[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueSeriesOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(-1, "Category 1", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueSeriesOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(1, "Category 1", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueInvalidCategory() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(0, "UnknownCategory", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueSeriesOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setEndValue(-1, "Category 1", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueSeriesOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setEndValue(1, "Category 1", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueInvalidCategory() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setEndValue(0, "UnknownCategory", 10);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue("BadSeries", "Category 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue("Series 1", "BadCategory");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue("BadSeries", "Category 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue("Series 1", "BadCategory");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue("BadSeries", "Category 1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue("Series 1", "BadCategory");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueSeriesIndexOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueSeriesIndexOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueCategoryIndexOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValueCategoryIndexOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueSeriesIndexOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueSeriesIndexOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueCategoryIndexOutOfRangeLow() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValueCategoryIndexOutOfRangeHigh() {
        Number[][] starts = new Number[][] {{1}};
        Number[][] ends = new Number[][] {{2}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(0, 1);
    }

    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        Number[][] starts1 = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends1 = new Number[][] {{10, 20}, {30, 40}};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(starts1, ends1);

        Assert.assertTrue(d1.equals(d1));
        Assert.assertFalse(d1.equals("A String"));
        Assert.assertFalse(d1.equals(null));

        Number[][] starts2 = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends2 = new Number[][] {{10, 20}, {30, 40}};
        DefaultIntervalCategoryDataset d2 = new DefaultIntervalCategoryDataset(starts2, ends2);
        Assert.assertTrue(d1.equals(d2));

        // Different series keys
        d2.setSeriesKeys(new Comparable[] {"X1", "X2"});
        Assert.assertFalse(d1.equals(d2));
        d2.setSeriesKeys(new Comparable[] {"Series 1", "Series 2"});
        Assert.assertTrue(d1.equals(d2));

        // Different category keys
        d2.setCategoryKeys(new Comparable[] {"Y1", "Y2"});
        Assert.assertFalse(d1.equals(d2));
        d2.setCategoryKeys(new Comparable[] {"Category 1", "Category 2"});
        Assert.assertTrue(d1.equals(d2));

        // Different start data
        d2.setStartValue(0, "Category 1", 99);
        Assert.assertFalse(d1.equals(d2));
        d2.setStartValue(0, "Category 1", 1);
        Assert.assertTrue(d1.equals(d2));

        // Different end data
        d2.setEndValue(0, "Category 1", 99);
        Assert.assertFalse(d1.equals(d2));
        d2.setEndValue(0, "Category 1", 10);
        Assert.assertTrue(d1.equals(d2));

        // Clone test
        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) d1.clone();
        Assert.assertNotSame(d1, clone);
        Assert.assertEquals(d1, clone);

        clone.setStartValue(0, "Category 1", 999);
        Assert.assertFalse(d1.equals(clone));
    }

    @Test
    public void testEqualsWithDifferentDimensions() {
        Number[][] starts1 = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends1 = new Number[][] {{10, 20}, {30, 40}};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(starts1, ends1);

        Number[][] starts2 = new Number[][] {{1, 2}};
        Number[][] ends2 = new Number[][] {{10, 20}};
        DefaultIntervalCategoryDataset d2 = new DefaultIntervalCategoryDataset(starts2, ends2);

        Assert.assertFalse(d1.equals(d2));
    }
}