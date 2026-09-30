package org.jfree.data.time;

import org.jfree.data.event.SeriesChangeEvent;
import org.jfree.data.event.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TimeSeriesTest implements SeriesChangeListener {

    private boolean seriesChangedFlag;

    @Before
    public void setUp() {
        this.seriesChangedFlag = false;
    }

    public void seriesChanged(SeriesChangeEvent event) {
        this.seriesChangedFlag = true;
    }

    @Test
    public void testConstructorsAndDefaults() {
        TimeSeries s1 = new TimeSeries("Series 1");
        Assert.assertEquals("Series 1", s1.getKey());
        Assert.assertEquals("Time", s1.getDomainDescription());
        Assert.assertEquals("Value", s1.getRangeDescription());
        Assert.assertNull(s1.getTimePeriodClass());
        Assert.assertEquals(0, s1.getItemCount());
        Assert.assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        Assert.assertEquals(Long.MAX_VALUE, s1.getMaximumItemAge());
        Assert.assertTrue(Double.isNaN(s1.getMinY()));
        Assert.assertTrue(Double.isNaN(s1.getMaxY()));

        TimeSeries s2 = new TimeSeries("Series 2", "DomainDesc", "RangeDesc");
        Assert.assertEquals("Series 2", s2.getKey());
        Assert.assertEquals("DomainDesc", s2.getDomainDescription());
        Assert.assertEquals("RangeDesc", s2.getRangeDescription());
    }

    @Test
    public void testSetDomainAndRangeDescription() {
        TimeSeries s = new TimeSeries("S");
        s.setDomainDescription("New Domain");
        Assert.assertEquals("New Domain", s.getDomainDescription());
        s.setDomainDescription(null);
        Assert.assertNull(s.getDomainDescription());

        s.setRangeDescription("New Range");
        Assert.assertEquals("New Range", s.getRangeDescription());
        s.setRangeDescription(null);
        Assert.assertNull(s.getRangeDescription());
    }

    @Test
    public void testAddAndGetMinMaxY() {
        TimeSeries s = new TimeSeries("S");
        s.addChangeListener(this);

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s.add(d2, 20.0);
        Assert.assertTrue(this.seriesChangedFlag);
        Assert.assertEquals(Day.class, s.getTimePeriodClass());
        Assert.assertEquals(20.0, s.getMinY(), 0.0001);
        Assert.assertEquals(20.0, s.getMaxY(), 0.0001);

        this.seriesChangedFlag = false;
        // Test add sorted at start
        s.add(d1, 10.0, false);
        Assert.assertFalse(this.seriesChangedFlag);
        Assert.assertEquals(10.0, s.getMinY(), 0.0001);
        Assert.assertEquals(20.0, s.getMaxY(), 0.0001);

        // Test add sorted at end
        s.add(d3, 30.0, true);
        Assert.assertTrue(this.seriesChangedFlag);
        Assert.assertEquals(10.0, s.getMinY(), 0.0001);
        Assert.assertEquals(30.0, s.getMaxY(), 0.0001);
        Assert.assertEquals(3, s.getItemCount());

        // Test item with null value
        Day d4 = new Day(4, 1, 2020);
        s.add(new TimeSeriesDataItem(d4, null));
        Assert.assertEquals(4, s.getItemCount());
        Assert.assertNull(s.getValue(d4));
        Assert.assertNull(s.getValue(3));
        Assert.assertEquals(10.0, s.getMinY(), 0.0001);
        Assert.assertEquals(30.0, s.getMaxY(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        TimeSeries s = new TimeSeries("S");
        s.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class)
    public void testAddDifferentTimePeriodClass() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Year(2020), 2.0);
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicate() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(1, 1, 2020), 2.0);
    }

    @Test
    public void testAddVariants() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), (Number) null);
        s.add(new Day(2, 1, 2020), new Double(2.5));
        s.add(new Day(3, 1, 2020), (Number) new Double(3.5), false);
        Assert.assertEquals(3, s.getItemCount());
        Assert.assertEquals(2.5, s.getValue(1).doubleValue(), 0.0001);
        Assert.assertEquals(3.5, s.getValue(new Day(3, 1, 2020)).doubleValue(), 0.0001);
        Assert.assertNull(s.getValue(new Day(4, 1, 2020)));
    }

    @Test
    public void testSetMaximumItemCount() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(2, 1, 2020), 20.0);
        s.add(new Day(3, 1, 2020), 30.0);

        s.setMaximumItemCount(2);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(20.0, s.getMinY(), 0.0001);
        Assert.assertEquals(30.0, s.getMaxY(), 0.0001);

        // Add exceeding maximumItemCount
        s.add(new Day(4, 1, 2020), 40.0);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(3, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(30.0, s.getMinY(), 0.0001);
        Assert.assertEquals(40.0, s.getMaxY(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        TimeSeries s = new TimeSeries("S");
        s.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemAge() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(10, 1, 2020), 20.0);
        s.add(new Day(20, 1, 2020), 30.0);

        s.setMaximumItemAge(10);
        Assert.assertEquals(10, s.getMaximumItemAge());
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(10, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(20.0, s.getMinY(), 0.0001);

        // Adding an item that ages out another
        s.add(new Day(25, 1, 2020), 40.0);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(20, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(30.0, s.getMinY(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        TimeSeries s = new TimeSeries("S");
        s.setMaximumItemAge(-5);
    }

    @Test
    public void testRemoveAgedItemsByTime() {
        TimeSeries s = new TimeSeries("S");
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(5, 1, 2020);
        Day d3 = new Day(10, 1, 2020);

        s.add(d1, 10.0);
        s.add(d2, 20.0);
        s.add(d3, 30.0);

        s.setMaximumItemAge(5);

        s.removeAgedItems(d3.getFirstMillisecond(), true);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(d2, s.getTimePeriod(0));

        // Test empty series removeAgedItems
        TimeSeries empty = new TimeSeries("Empty");
        empty.removeAgedItems(System.currentTimeMillis(), true);
        Assert.assertEquals(0, empty.getItemCount());
    }

    @Test
    public void testGetDataItemAndRawDataItem() {
        TimeSeries s = new TimeSeries("S");
        Day d1 = new Day(1, 1, 2020);
        s.add(d1, 100.0);

        TimeSeriesDataItem item = s.getDataItem(0);
        Assert.assertEquals(100.0, item.getValue().doubleValue(), 0.0001);
        item.setValue(new Double(999.0));
        // Verify cloning isolation
        Assert.assertEquals(100.0, s.getValue(0).doubleValue(), 0.0001);

        TimeSeriesDataItem itemByPeriod = s.getDataItem(d1);
        Assert.assertNotNull(itemByPeriod);
        Assert.assertEquals(100.0, itemByPeriod.getValue().doubleValue(), 0.0001);
        Assert.assertNull(s.getDataItem(new Day(2, 1, 2020)));

        Assert.assertNotNull(s.getRawDataItem(0));
        Assert.assertNotNull(s.getRawDataItem(d1));
        Assert.assertNull(s.getRawDataItem(new Day(2, 1, 2020)));
    }

    @Test
    public void testGetNextTimePeriodAndCollections() {
        TimeSeries s = new TimeSeries("S");
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s.add(d1, 10.0);
        s.add(d2, 20.0);

        Assert.assertEquals(new Day(3, 1, 2020), s.getNextTimePeriod());

        Collection periods = s.getTimePeriods();
        Assert.assertEquals(2, periods.size());
        Assert.assertTrue(periods.contains(d1));
        Assert.assertTrue(periods.contains(d2));

        TimeSeries s2 = new TimeSeries("S2");
        Day d3 = new Day(3, 1, 2020);
        s2.add(d1, 5.0);
        s2.add(d3, 15.0);

        Collection unique = s.getTimePeriodsUniqueToOtherSeries(s2);
        Assert.assertEquals(1, unique.size());
        Assert.assertTrue(unique.contains(d3));

        List items = s.getItems();
        Assert.assertEquals(2, items.size());
        try {
            items.clear();
            Assert.fail("getItems() should return an unmodifiable list");
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNull() {
        TimeSeries s = new TimeSeries("S");
        s.getIndex(null);
    }

    @Test
    public void testUpdateByIndexAndPeriod() {
        TimeSeries s = new TimeSeries("S");
        s.addChangeListener(this);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s.add(d1, 10.0);
        s.add(d2, 20.0);
        s.add(d3, 30.0);

        // Update within bounds
        this.seriesChangedFlag = false;
        s.update(1, 25.0);
        Assert.assertTrue(this.seriesChangedFlag);
        Assert.assertEquals(25.0, s.getValue(1).doubleValue(), 0.0001);
        Assert.assertEquals(10.0, s.getMinY(), 0.0001);
        Assert.assertEquals(30.0, s.getMaxY(), 0.0001);

        // Update boundary triggering iteration
        s.update(0, 50.0);
        Assert.assertEquals(25.0, s.getMinY(), 0.0001);
        Assert.assertEquals(50.0, s.getMaxY(), 0.0001);

        // Update via period
        s.update(d2, 5.0);
        Assert.assertEquals(5.0, s.getValue(d2).doubleValue(), 0.0001);
        Assert.assertEquals(5.0, s.getMinY(), 0.0001);

        // Update with null
        s.update(1, null);
        Assert.assertNull(s.getValue(1));
        Assert.assertEquals(50.0, s.getMinY(), 0.0001);
        Assert.assertEquals(50.0, s.getMaxY(), 0.0001);
    }

    @Test(expected = SeriesException.class)
    public void testUpdateNonExistingPeriod() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 10.0);
        s.update(new Day(2, 1, 2020), 20.0);
    }

    @Test
    public void testAddOrUpdate() {
        TimeSeries s = new TimeSeries("S");
        s.setMaximumItemCount(2);

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        TimeSeriesDataItem item1 = s.addOrUpdate(d1, 10.0);
        Assert.assertNull(item1);
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertEquals(Day.class, s.getTimePeriodClass());

        TimeSeriesDataItem item2 = s.addOrUpdate(d2, 20.0);
        Assert.assertNull(item2);

        // Test item count cap via addOrUpdate
        TimeSeriesDataItem item3 = s.addOrUpdate(d3, 30.0);
        Assert.assertNull(item3);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(d2, s.getTimePeriod(0));

        // Test updating existing item
        TimeSeriesDataItem overwritten = s.addOrUpdate(d2, 200.0);
        Assert.assertNotNull(overwritten);
        Assert.assertEquals(20.0, overwritten.getValue().doubleValue(), 0.0001);
        Assert.assertEquals(200.0, s.getValue(d2).doubleValue(), 0.0001);

        // Updating with null
        overwritten = s.addOrUpdate(d2, (Number) null);
        Assert.assertEquals(200.0, overwritten.getValue().doubleValue(), 0.0001);
        Assert.assertNull(s.getValue(d2));

        // Test addOrUpdate with non-overwriting item inserted in middle
        TimeSeries s2 = new TimeSeries("S2");
        s2.add(d1, 10.0);
        s2.add(d3, 30.0);
        s2.addOrUpdate(d2, 20.0);
        Assert.assertEquals(3, s2.getItemCount());
        Assert.assertEquals(d2, s2.getTimePeriod(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNull() {
        TimeSeries s = new TimeSeries("S");
        s.addOrUpdate((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class)
    public void testAddOrUpdateWrongClass() {
        TimeSeries s = new TimeSeries("S");
        s.addOrUpdate(new Day(1, 1, 2020), 10.0);
        s.addOrUpdate(new Year(2020), 20.0);
    }

    @Test
    public void testAddAndOrUpdate() {
        TimeSeries s1 = new TimeSeries("S1");
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s1.add(d1, 10.0);
        s1.add(d2, 20.0);

        TimeSeries s2 = new TimeSeries("S2");
        Day d3 = new Day(3, 1, 2020);
        s2.add(d2, 200.0);
        s2.add(d3, 300.0);

        TimeSeries overwritten = s1.addAndOrUpdate(s2);
        Assert.assertEquals(1, overwritten.getItemCount());
        Assert.assertEquals(20.0, overwritten.getValue(0).doubleValue(), 0.0001);
        Assert.assertEquals(3, s1.getItemCount());
        Assert.assertEquals(200.0, s1.getValue(d2).doubleValue(), 0.0001);
        Assert.assertEquals(300.0, s1.getValue(d3).doubleValue(), 0.0001);
    }

    @Test
    public void testClear() {
        TimeSeries s = new TimeSeries("S");
        s.addChangeListener(this);
        s.add(new Day(1, 1, 2020), 10.0);

        this.seriesChangedFlag = false;
        s.clear();
        Assert.assertTrue(this.seriesChangedFlag);
        Assert.assertEquals(0, s.getItemCount());
        Assert.assertNull(s.getTimePeriodClass());
        Assert.assertTrue(Double.isNaN(s.getMinY()));
        Assert.assertTrue(Double.isNaN(s.getMaxY()));

        // Clearing already empty series
        this.seriesChangedFlag = false;
        s.clear();
        Assert.assertFalse(this.seriesChangedFlag);
    }

    @Test
    public void testDeletePeriod() {
        TimeSeries s = new TimeSeries("S");
        s.addChangeListener(this);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s.add(d1, 10.0);
        s.add(d2, 20.0);

        this.seriesChangedFlag = false;
        s.delete(d1);
        Assert.assertTrue(this.seriesChangedFlag);
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertEquals(20.0, s.getMinY(), 0.0001);
        Assert.assertEquals(20.0, s.getMaxY(), 0.0001);

        // Delete remaining
        s.delete(d2);
        Assert.assertEquals(0, s.getItemCount());
        Assert.assertNull(s.getTimePeriodClass());
        Assert.assertTrue(Double.isNaN(s.getMinY()));

        // Delete non-existing
        this.seriesChangedFlag = false;
        s.delete(new Day(3, 1, 2020));
        Assert.assertFalse(this.seriesChangedFlag);
    }

    @Test
    public void testDeleteRange() {
        TimeSeries s = new TimeSeries("S");
        s.addChangeListener(this);
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(2, 1, 2020), 20.0);
        s.add(new Day(3, 1, 2020), 30.0);
        s.add(new Day(4, 1, 2020), 40.0);

        this.seriesChangedFlag = false;
        s.delete(1, 2, false);
        Assert.assertFalse(this.seriesChangedFlag);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(1, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(new Day(4, 1, 2020), s.getTimePeriod(1));
        Assert.assertEquals(10.0, s.getMinY(), 0.0001);
        Assert.assertEquals(40.0, s.getMaxY(), 0.0001);

        s.delete(0, 1);
        Assert.assertEquals(0, s.getItemCount());
        Assert.assertNull(s.getTimePeriodClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRangeInvalidOrder() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 10.0);
        s.delete(1, 0);
    }

    @Test
    public void testClone() throws Exception {
        TimeSeries s = new TimeSeries("S", "D", "R");
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(2, 1, 2020), 20.0);

        TimeSeries clone = (TimeSeries) s.clone();
        Assert.assertEquals(s, clone);
        Assert.assertNotSame(s, clone);

        clone.update(0, 99.0);
        Assert.assertFalse(s.equals(clone));
    }

    @Test
    public void testCreateCopyIndexRange() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(2, 1, 2020), 20.0);
        s.add(new Day(3, 1, 2020), 30.0);

        TimeSeries copy = s.createCopy(1, 2);
        Assert.assertEquals(2, copy.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), copy.getTimePeriod(0));
        Assert.assertEquals(new Day(3, 1, 2020), copy.getTimePeriod(1));
        Assert.assertEquals(20.0, copy.getMinY(), 0.0001);
        Assert.assertEquals(30.0, copy.getMaxY(), 0.0001);

        TimeSeries empty = new TimeSeries("Empty");
        TimeSeries emptyCopy = empty.createCopy(0, 0);
        Assert.assertEquals(0, emptyCopy.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNegativeStart() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyEndLessThanStart() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.createCopy(2, 1);
    }

    @Test
    public void testCreateCopyPeriodRange() throws Exception {
        TimeSeries s = new TimeSeries("S");
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(5, 1, 2020);
        Day d3 = new Day(10, 1, 2020);
        s.add(d1, 10.0);
        s.add(d2, 20.0);
        s.add(d3, 30.0);

        // Exact match
        TimeSeries copy1 = s.createCopy(d1, d2);
        Assert.assertEquals(2, copy1.getItemCount());
        Assert.assertEquals(d1, copy1.getTimePeriod(0));
        Assert.assertEquals(d2, copy1.getTimePeriod(1));

        // Periods that fall between existing items
        TimeSeries copy2 = s.createCopy(new Day(2, 1, 2020), new Day(8, 1, 2020));
        Assert.assertEquals(1, copy2.getItemCount());
        Assert.assertEquals(d2, copy2.getTimePeriod(0));

        // Start after last item -> emptyRange
        TimeSeries copy3 = s.createCopy(new Day(15, 1, 2020), new Day(20, 1, 2020));
        Assert.assertEquals(0, copy3.getItemCount());

        // End before first item -> emptyRange
        TimeSeries copy4 = s.createCopy(new Day(1, 1, 2019), new Day(2, 1, 2019));
        Assert.assertEquals(0, copy4.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodRangeNullStart() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.createCopy((RegularTimePeriod) null, new Day(1, 1, 2020));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodRangeNullEnd() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.createCopy(new Day(1, 1, 2020), (RegularTimePeriod) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodRangeInvalidOrder() throws Exception {
        TimeSeries s = new TimeSeries("S");
        s.createCopy(new Day(2, 1, 2020), new Day(1, 1, 2020));
    }

    @Test
    public void testEqualsAndHashCode() {
        TimeSeries s1 = new TimeSeries("Series", "Domain", "Range");
        TimeSeries s2 = new TimeSeries("Series", "Domain", "Range");

        Assert.assertTrue(s1.equals(s1));
        Assert.assertFalse(s1.equals(null));
        Assert.assertFalse(s1.equals("Not a TimeSeries"));
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        // Domain
        s2.setDomainDescription("Other Domain");
        Assert.assertFalse(s1.equals(s2));
        s2.setDomainDescription("Domain");

        // Range
        s2.setRangeDescription("Other Range");
        Assert.assertFalse(s1.equals(s2));
        s2.setRangeDescription("Range");

        // MaximumItemAge
        s2.setMaximumItemAge(100);
        Assert.assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(Long.MAX_VALUE);

        // MaximumItemCount
        s2.setMaximumItemCount(100);
        Assert.assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(Integer.MAX_VALUE);

        // Add items & check hashCode for count 1, 2, 3
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s1.add(d1, 10.0);
        Assert.assertFalse(s1.equals(s2));
        s2.add(d1, 10.0);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(d2, 20.0);
        s2.add(d2, 20.0);
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(d3, 30.0);
        s2.add(d3, 30.0);
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        // Value difference
        s2.update(2, 35.0);
        Assert.assertFalse(s1.equals(s2));
    }

    @Test
    public void testSerialization() throws Exception {
        TimeSeries s1 = new TimeSeries("Series", "Domain", "Range");
        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(s1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        TimeSeries s2 = (TimeSeries) in.readObject();
        in.close();

        Assert.assertEquals(s1, s2);
        Assert.assertEquals(s1.getMinY(), s2.getMinY(), 0.0001);
        Assert.assertEquals(s1.getMaxY(), s2.getMaxY(), 0.0001);
    }
}