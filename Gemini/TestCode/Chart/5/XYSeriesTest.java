package org.jfree.data.xy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Assert;
import org.junit.Test;

/**
 * A comprehensive test suite for the {@link XYSeries} class.
 */
public class XYSeriesTest implements SeriesChangeListener {

    private SeriesChangeEvent lastEvent;

    @Override
    public void seriesChanged(SeriesChangeEvent event) {
        this.lastEvent = event;
    }

    /**
     * Test constructor defaults and getters.
     */
    @Test
    public void testConstructorsAndDefaults() {
        XYSeries s1 = new XYSeries("Series 1");
        Assert.assertEquals("Series 1", s1.getKey());
        Assert.assertTrue(s1.getAutoSort());
        Assert.assertTrue(s1.getAllowDuplicateXValues());
        Assert.assertEquals(0, s1.getItemCount());
        Assert.assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());

        XYSeries s2 = new XYSeries("Series 2", false);
        Assert.assertFalse(s2.getAutoSort());
        Assert.assertTrue(s2.getAllowDuplicateXValues());

        XYSeries s3 = new XYSeries("Series 3", false, false);
        Assert.assertFalse(s3.getAutoSort());
        Assert.assertFalse(s3.getAllowDuplicateXValues());
    }

    /**
     * Test adding items with automatic sorting enabled.
     */
    @Test
    public void testAddAutoSorted() {
        XYSeries series = new XYSeries("S", true, true);
        series.add(2.0, 20.0);
        series.add(1.0, 10.0);
        series.add(3.0, 30.0);

        Assert.assertEquals(3, series.getItemCount());
        Assert.assertEquals(1.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(10.0, series.getY(0).doubleValue(), 1e-9);
        Assert.assertEquals(2.0, series.getX(1).doubleValue(), 1e-9);
        Assert.assertEquals(20.0, series.getY(1).doubleValue(), 1e-9);
        Assert.assertEquals(3.0, series.getX(2).doubleValue(), 1e-9);
        Assert.assertEquals(30.0, series.getY(2).doubleValue(), 1e-9);
    }

    /**
     * Test adding duplicate items when duplicates are allowed in auto-sorted mode.
     */
    @Test
    public void testAddDuplicatesAutoSorted() {
        XYSeries series = new XYSeries("S", true, true);
        series.add(2.0, 20.0);
        series.add(1.0, 10.0);
        series.add(2.0, 25.0);
        series.add(2.0, 28.0);
        series.add(3.0, 30.0);

        Assert.assertEquals(5, series.getItemCount());
        Assert.assertEquals(1.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(2.0, series.getX(1).doubleValue(), 1e-9);
        Assert.assertEquals(20.0, series.getY(1).doubleValue(), 1e-9);
        Assert.assertEquals(2.0, series.getX(2).doubleValue(), 1e-9);
        Assert.assertEquals(25.0, series.getY(2).doubleValue(), 1e-9);
        Assert.assertEquals(2.0, series.getX(3).doubleValue(), 1e-9);
        Assert.assertEquals(28.0, series.getY(3).doubleValue(), 1e-9);
        Assert.assertEquals(3.0, series.getX(4).doubleValue(), 1e-9);

        // Add duplicate at the very end
        series.add(3.0, 35.0);
        Assert.assertEquals(6, series.getItemCount());
        Assert.assertEquals(35.0, series.getY(5).doubleValue(), 1e-9);
    }

    /**
     * Test adding duplicate item when duplicate X values are not allowed (autoSort=true).
     */
    @Test(expected = SeriesException.class)
    public void testAddDuplicateAutoSortedException() {
        XYSeries series = new XYSeries("S", true, false);
        series.add(1.0, 10.0);
        series.add(1.0, 20.0);
    }

    /**
     * Test adding duplicate item when duplicate X values are not allowed (autoSort=false).
     */
    @Test(expected = SeriesException.class)
    public void testAddDuplicateUnsortedException() {
        XYSeries series = new XYSeries("S", false, false);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(1.0, 30.0);
    }

    /**
     * Test unsorted insertion order.
     */
    @Test
    public void testAddUnsorted() {
        XYSeries series = new XYSeries("S", false, true);
        series.add(3.0, 30.0);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);

        Assert.assertEquals(3, series.getItemCount());
        Assert.assertEquals(3.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(1.0, series.getX(1).doubleValue(), 1e-9);
        Assert.assertEquals(2.0, series.getX(2).doubleValue(), 1e-9);
    }

    /**
     * Test adding with null item argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        XYSeries series = new XYSeries("S");
        series.add((XYDataItem) null);
    }

    /**
     * Test notification flag during add.
     */
    @Test
    public void testAddNotification() {
        XYSeries series = new XYSeries("S");
        series.addChangeListener(this);

        this.lastEvent = null;
        series.add(1.0, 1.0, false);
        Assert.assertNull(this.lastEvent);

        series.add(2.0, 2.0, true);
        Assert.assertNotNull(this.lastEvent);

        this.lastEvent = null;
        series.add(3.0, (Number) null, false);
        Assert.assertNull(this.lastEvent);

        series.add(4.0, (Number) null);
        Assert.assertNotNull(this.lastEvent);

        this.lastEvent = null;
        series.add(new Double(5.0), new Double(5.0), false);
        Assert.assertNull(this.lastEvent);

        series.add(new Double(6.0), new Double(6.0));
        Assert.assertNotNull(this.lastEvent);
    }

    /**
     * Test maximum item count trimming during add and setMaximumItemCount.
     */
    @Test
    public void testMaximumItemCount() {
        XYSeries series = new XYSeries("S", false, true);
        series.setMaximumItemCount(3);
        Assert.assertEquals(3, series.getMaximumItemCount());

        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        Assert.assertEquals(3, series.getItemCount());
        Assert.assertEquals(1.0, series.getX(0).doubleValue(), 1e-9);

        // Exceed max item count -> oldest removed
        series.add(4.0, 40.0);
        Assert.assertEquals(3, series.getItemCount());
        Assert.assertEquals(2.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(4.0, series.getX(2).doubleValue(), 1e-9);

        // Set smaller max item count trims existing elements and triggers listener
        series.addChangeListener(this);
        this.lastEvent = null;
        series.setMaximumItemCount(1);
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(1, series.getItemCount());
        Assert.assertEquals(4.0, series.getX(0).doubleValue(), 1e-9);

        // Setting larger max item count should not remove data or trigger change event
        this.lastEvent = null;
        series.setMaximumItemCount(10);
        Assert.assertNull(this.lastEvent);
        Assert.assertEquals(1, series.getItemCount());
    }

    /**
     * Test getItems returns unmodifiable list.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetItemsUnmodifiable() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 1.0);
        List items = series.getItems();
        items.add(new XYDataItem(2.0, 2.0));
    }

    /**
     * Test delete range method.
     */
    @Test
    public void testDelete() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        series.add(4.0, 40.0);
        series.addChangeListener(this);

        this.lastEvent = null;
        series.delete(1, 2);
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(2, series.getItemCount());
        Assert.assertEquals(1.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(4.0, series.getX(1).doubleValue(), 1e-9);
    }

    /**
     * Test remove methods (by index and by X value).
     */
    @Test
    public void testRemove() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        series.addChangeListener(this);

        this.lastEvent = null;
        XYDataItem removed = series.remove(1);
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(2.0, removed.getX().doubleValue(), 1e-9);
        Assert.assertEquals(20.0, removed.getY().doubleValue(), 1e-9);
        Assert.assertEquals(2, series.getItemCount());

        this.lastEvent = null;
        removed = series.remove(new Double(1.0));
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(1.0, removed.getX().doubleValue(), 1e-9);
        Assert.assertEquals(1, series.getItemCount());
    }

    /**
     * Test clear method.
     */
    @Test
    public void testClear() {
        XYSeries series = new XYSeries("S");
        series.addChangeListener(this);

        // Clear empty series does not fire event
        this.lastEvent = null;
        series.clear();
        Assert.assertNull(this.lastEvent);

        series.add(1.0, 10.0);
        this.lastEvent = null;
        series.clear();
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(0, series.getItemCount());
    }

    /**
     * Test updateByIndex and update methods.
     */
    @Test
    public void testUpdate() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.addChangeListener(this);

        this.lastEvent = null;
        series.updateByIndex(0, 15.0);
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(15.0, series.getY(0).doubleValue(), 1e-9);

        this.lastEvent = null;
        series.update(new Double(2.0), new Double(25.0));
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(25.0, series.getY(1).doubleValue(), 1e-9);
    }

    /**
     * Test update with non-existing X value.
     */
    @Test(expected = SeriesException.class)
    public void testUpdateNonExisting() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 10.0);
        series.update(new Double(5.0), new Double(50.0));
    }

    /**
     * Test addOrUpdate with null X argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullX() {
        XYSeries series = new XYSeries("S");
        series.addOrUpdate(null, new Double(10.0));
    }

    /**
     * Test addOrUpdate when duplicates are not allowed (overwriting existing value).
     */
    @Test
    public void testAddOrUpdateNoDuplicates() {
        XYSeries series = new XYSeries("S", true, false);
        series.add(1.0, 10.0);
        series.add(3.0, 30.0);
        series.addChangeListener(this);

        this.lastEvent = null;
        XYDataItem overwritten = series.addOrUpdate(new Double(1.0), new Double(15.0));
        Assert.assertNotNull(this.lastEvent);
        Assert.assertNotNull(overwritten);
        Assert.assertEquals(1.0, overwritten.getX().doubleValue(), 1e-9);
        Assert.assertEquals(10.0, overwritten.getY().doubleValue(), 1e-9);
        Assert.assertEquals(15.0, series.getY(0).doubleValue(), 1e-9);
        Assert.assertEquals(2, series.getItemCount());

        // Test double primitive overload
        overwritten = series.addOrUpdate(2.0, 20.0);
        Assert.assertNull(overwritten);
        Assert.assertEquals(3, series.getItemCount());
        Assert.assertEquals(2.0, series.getX(1).doubleValue(), 1e-9);
        Assert.assertEquals(20.0, series.getY(1).doubleValue(), 1e-9);
    }

    /**
     * Test addOrUpdate with unsorted and autoSort with max item count trimming.
     */
    @Test
    public void testAddOrUpdateWithMaxCountAndUnsorted() {
        XYSeries series = new XYSeries("S", false, true);
        series.setMaximumItemCount(2);

        XYDataItem item1 = series.addOrUpdate(new Double(10.0), new Double(100.0));
        Assert.assertNull(item1);
        XYDataItem item2 = series.addOrUpdate(new Double(20.0), new Double(200.0));
        Assert.assertNull(item2);
        Assert.assertEquals(2, series.getItemCount());

        // Third addition should trim first element
        series.addOrUpdate(new Double(30.0), new Double(300.0));
        Assert.assertEquals(2, series.getItemCount());
        Assert.assertEquals(20.0, series.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(30.0, series.getX(1).doubleValue(), 1e-9);

        // Auto-sorted series exceeding max count
        XYSeries sortedSeries = new XYSeries("Sorted", true, true);
        sortedSeries.setMaximumItemCount(2);
        sortedSeries.addOrUpdate(1.0, 10.0);
        sortedSeries.addOrUpdate(3.0, 30.0);
        sortedSeries.addOrUpdate(2.0, 20.0);
        Assert.assertEquals(2, sortedSeries.getItemCount());
        Assert.assertEquals(2.0, sortedSeries.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(3.0, sortedSeries.getX(1).doubleValue(), 1e-9);
    }

    /**
     * Test indexOf for sorted and unsorted series.
     */
    @Test
    public void testIndexOf() {
        XYSeries sorted = new XYSeries("Sorted", true, true);
        sorted.add(10.0, 1.0);
        sorted.add(20.0, 2.0);
        sorted.add(30.0, 3.0);

        Assert.assertEquals(0, sorted.indexOf(new Double(10.0)));
        Assert.assertEquals(1, sorted.indexOf(new Double(20.0)));
        Assert.assertEquals(2, sorted.indexOf(new Double(30.0)));
        Assert.assertTrue(sorted.indexOf(new Double(15.0)) < 0);

        XYSeries unsorted = new XYSeries("Unsorted", false, true);
        unsorted.add(30.0, 3.0);
        unsorted.add(10.0, 1.0);
        unsorted.add(20.0, 2.0);

        Assert.assertEquals(0, unsorted.indexOf(new Double(30.0)));
        Assert.assertEquals(1, unsorted.indexOf(new Double(10.0)));
        Assert.assertEquals(2, unsorted.indexOf(new Double(20.0)));
        Assert.assertEquals(-1, unsorted.indexOf(new Double(40.0)));
    }

    /**
     * Test toArray method including null y-values.
     */
    @Test
    public void testToArray() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 10.0);
        series.add(2.0, (Number) null);
        series.add(3.0, 30.0);

        double[][] array = series.toArray();
        Assert.assertEquals(2, array.length);
        Assert.assertEquals(3, array[0].length);
        Assert.assertEquals(3, array[1].length);

        Assert.assertEquals(1.0, array[0][0], 1e-9);
        Assert.assertEquals(10.0, array[1][0], 1e-9);
        Assert.assertEquals(2.0, array[0][1], 1e-9);
        Assert.assertTrue(Double.isNaN(array[1][1]));
        Assert.assertEquals(3.0, array[0][2], 1e-9);
        Assert.assertEquals(30.0, array[1][2], 1e-9);

        // Empty series toArray
        XYSeries empty = new XYSeries("Empty");
        double[][] emptyArr = empty.toArray();
        Assert.assertEquals(2, emptyArr.length);
        Assert.assertEquals(0, emptyArr[0].length);
        Assert.assertEquals(0, emptyArr[1].length);
    }

    /**
     * Test clone method.
     */
    @Test
    public void testClone() throws Exception {
        XYSeries s1 = new XYSeries("S", true, true);
        s1.add(1.0, 10.0);
        s1.add(2.0, 20.0);

        XYSeries s2 = (XYSeries) s1.clone();
        Assert.assertNotSame(s1, s2);
        Assert.assertSame(s1.getClass(), s2.getClass());
        Assert.assertEquals(s1, s2);

        // Modify clone and check independence
        s2.add(3.0, 30.0);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertEquals(2, s1.getItemCount());
        Assert.assertEquals(3, s2.getItemCount());
    }

    /**
     * Test createCopy method.
     */
    @Test
    public void testCreateCopy() throws Exception {
        XYSeries s1 = new XYSeries("S");
        s1.add(1.0, 10.0);
        s1.add(2.0, 20.0);
        s1.add(3.0, 30.0);
        s1.add(4.0, 40.0);

        XYSeries copy = s1.createCopy(1, 2);
        Assert.assertEquals(2, copy.getItemCount());
        Assert.assertEquals(2.0, copy.getX(0).doubleValue(), 1e-9);
        Assert.assertEquals(20.0, copy.getY(0).doubleValue(), 1e-9);
        Assert.assertEquals(3.0, copy.getX(1).doubleValue(), 1e-9);
        Assert.assertEquals(30.0, copy.getY(1).doubleValue(), 1e-9);

        // Empty copy
        XYSeries empty = new XYSeries("Empty");
        XYSeries emptyCopy = empty.createCopy(0, 0);
        Assert.assertEquals(0, emptyCopy.getItemCount());
    }

    /**
     * Test equals and hashCode methods across all branching paths.
     */
    @Test
    public void testEqualsAndHashCode() {
        XYSeries s1 = new XYSeries("Series", true, true);
        XYSeries s2 = new XYSeries("Series", true, true);

        Assert.assertTrue(s1.equals(s1));
        Assert.assertFalse(s1.equals(null));
        Assert.assertFalse(s1.equals("Not an XYSeries"));
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        // Differences in key
        XYSeries s3 = new XYSeries("Series Diff", true, true);
        Assert.assertFalse(s1.equals(s3));

        // Differences in autoSort
        XYSeries s4 = new XYSeries("Series", false, true);
        Assert.assertFalse(s1.equals(s4));

        // Differences in allowDuplicateXValues
        XYSeries s5 = new XYSeries("Series", true, false);
        Assert.assertFalse(s1.equals(s5));

        // Differences in maximumItemCount
        s2.setMaximumItemCount(50);
        Assert.assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(Integer.MAX_VALUE);
        Assert.assertTrue(s1.equals(s2));

        // Differences in data
        s1.add(1.0, 10.0);
        Assert.assertFalse(s1.equals(s2));
        s2.add(1.0, 10.0);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        // Test hashCode branching with 0, 1, 2, 3+ items
        XYSeries h0 = new XYSeries("H");
        int hc0 = h0.hashCode();

        XYSeries h1 = new XYSeries("H");
        h1.add(1.0, 1.0);
        int hc1 = h1.hashCode();
        Assert.assertNotEquals(hc0, hc1);

        XYSeries h2 = new XYSeries("H");
        h2.add(1.0, 1.0);
        h2.add(2.0, 2.0);
        int hc2 = h2.hashCode();
        Assert.assertNotEquals(hc1, hc2);

        XYSeries h3 = new XYSeries("H");
        h3.add(1.0, 1.0);
        h3.add(2.0, 2.0);
        h3.add(3.0, 3.0);
        int hc3 = h3.hashCode();
        Assert.assertNotEquals(hc2, hc3);
    }

    /**
     * Test serialization.
     */
    @Test
    public void testSerialization() throws Exception {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 10.0);
        s1.add(2.0, 20.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(s1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        XYSeries s2 = (XYSeries) in.readObject();
        in.close();

        Assert.assertEquals(s1, s2);
        Assert.assertEquals(s1.hashCode(), s2.hashCode());
    }
}