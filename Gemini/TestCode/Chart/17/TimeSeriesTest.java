package org.jfree.data.time;

import java.util.Collection;
import java.util.List;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Assert;
import org.junit.Test;

public class TimeSeriesTest implements SeriesChangeListener {

    private SeriesChangeEvent lastEvent;

    public void seriesChanged(SeriesChangeEvent event) {
        this.lastEvent = event;
    }

    @Test
    public void testConstructors() {
        TimeSeries s1 = new TimeSeries("Series 1");
        Assert.assertEquals("Series 1", s1.getKey());
        Assert.assertEquals("Time", s1.getDomainDescription());
        Assert.assertEquals("Value", s1.getRangeDescription());
        Assert.assertEquals(Day.class, s1.getTimePeriodClass());
        Assert.assertEquals(0, s1.getItemCount());
        Assert.assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        Assert.assertEquals(Long.MAX_VALUE, s1.getMaximumItemAge());

        TimeSeries s2 = new TimeSeries("Series 2", Year.class);
        Assert.assertEquals("Series 2", s2.getKey());
        Assert.assertEquals(Year.class, s2.getTimePeriodClass());

        TimeSeries s3 = new TimeSeries("Series 3", "D", "R", Month.class);
        Assert.assertEquals("Series 3", s3.getKey());
        Assert.assertEquals("D", s3.getDomainDescription());
        Assert.assertEquals("R", s3.getRangeDescription());
        Assert.assertEquals(Month.class, s3.getTimePeriodClass());
    }

    @Test
    public void testDomainAndRangeDescription() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setDomainDescription("New Domain");
        Assert.assertEquals("New Domain", s.getDomainDescription());
        s.setRangeDescription("New Range");
        Assert.assertEquals("New Range", s.getRangeDescription());

        s.setDomainDescription(null);
        Assert.assertNull(s.getDomainDescription());
        s.setRangeDescription(null);
        Assert.assertNull(s.getRangeDescription());
    }

    @Test
    public void testSetMaximumItemCount() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(2, 1, 2020), 2.0);
        s.add(new Day(3, 1, 2020), 3.0);
        s.add(new Day(4, 1, 2020), 4.0);
        Assert.assertEquals(4, s.getItemCount());

        s.setMaximumItemCount(2);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(3, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(new Day(4, 1, 2020), s.getTimePeriod(1));

        try {
            s.setMaximumItemCount(-1);
            Assert.fail("Expected IllegalArgumentException on negative count");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSetMaximumItemAge() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 10.0);
        s.add(new Day(10, 1, 2020), 20.0);
        s.add(new Day(20, 1, 2020), 30.0);

        s.setMaximumItemAge(5);
        Assert.assertEquals(5, s.getMaximumItemAge());
        // Since max age is 5, from Day 20, items earlier than Day 15 are removed
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertEquals(new Day(20, 1, 2020), s.getTimePeriod(0));

        try {
            s.setMaximumItemAge(-1);
            Assert.fail("Expected IllegalArgumentException on negative age");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetItems() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 10.0);
        List items = s.getItems();
        Assert.assertEquals(1, items.size());
        try {
            items.add(new TimeSeriesDataItem(new Day(2, 1, 2020), 20.0));
            Assert.fail("Expected UnsupportedOperationException when modifying unmodifiable list");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetDataItemAndGetIndex() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s.add(d1, 100.0);
        s.add(d3, 300.0);

        Assert.assertEquals(100.0, s.getDataItem(0).getValue().doubleValue(), 1e-9);
        Assert.assertEquals(d1, s.getTimePeriod(0));
        Assert.assertEquals(300.0, s.getDataItem(d3).getValue().doubleValue(), 1e-9);
        Assert.assertNull(s.getDataItem(d2));

        Assert.assertEquals(0, s.getIndex(d1));
        Assert.assertEquals(1, s.getIndex(d3));
        Assert.assertTrue(s.getIndex(d2) < 0);

        try {
            s.getIndex(null);
            Assert.fail("Expected IllegalArgumentException on null period in getIndex");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetNextTimePeriod() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        s.add(d1, 10.0);
        RegularTimePeriod next = s.getNextTimePeriod();
        Assert.assertEquals(new Day(2, 1, 2020), next);
    }

    @Test
    public void testGetTimePeriods() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s.add(d1, 10.0);
        s.add(d2, 20.0);

        Collection periods = s.getTimePeriods();
        Assert.assertEquals(2, periods.size());
        Assert.assertTrue(periods.contains(d1));
        Assert.assertTrue(periods.contains(d2));
    }

    @Test
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries s1 = new TimeSeries("S1", Day.class);
        TimeSeries s2 = new TimeSeries("S2", Day.class);

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s1.add(d1, 1.0);
        s1.add(d2, 2.0);

        s2.add(d2, 2.0);
        s2.add(d3, 3.0);

        Collection unique = s1.getTimePeriodsUniqueToOtherSeries(s2);
        Assert.assertEquals(1, unique.size());
        Assert.assertTrue(unique.contains(d3));
        Assert.assertFalse(unique.contains(d2));
    }

    @Test
    public void testGetValue() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s.add(d1, 10.0);
        s.add(d2, (Number) null);

        Assert.assertEquals(10.0, s.getValue(0).doubleValue(), 1e-9);
        Assert.assertNull(s.getValue(1));
        Assert.assertEquals(10.0, s.getValue(d1).doubleValue(), 1e-9);
        Assert.assertNull(s.getValue(d2));
        Assert.assertNull(s.getValue(d3));
    }

    @Test
    public void testAddVariants() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.addChangeListener(this);

        this.lastEvent = null;
        s.add(new Day(1, 1, 2020), 10.0);
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(1, s.getItemCount());

        this.lastEvent = null;
        s.add(new Day(2, 1, 2020), 20.0, false);
        Assert.assertNull(this.lastEvent);
        Assert.assertEquals(2, s.getItemCount());

        this.lastEvent = null;
        s.add(new Day(3, 1, 2020), new Double(30.0));
        Assert.assertNotNull(this.lastEvent);
        Assert.assertEquals(3, s.getItemCount());

        this.lastEvent = null;
        s.add(new Day(4, 1, 2020), new Double(40.0), false);
        Assert.assertNull(this.lastEvent);
        Assert.assertEquals(4, s.getItemCount());

        // Insert in middle (ordered insertion)
        s.add(new TimeSeriesDataItem(new Day(2, 1, 2020).next(), 25.0), true);
        // Day 2 next is Day 3, but Day 3 is already there, so this tests duplicate exception
        // Let's test insert between 1 and 2: none, between 2 and 3: none unless we pick different period,
        // let's create a new series with Day 1 and Day 3
        TimeSeries sOrder = new TimeSeries("SOrder", Day.class);
        sOrder.add(new Day(1, 1, 2020), 10.0);
        sOrder.add(new Day(3, 1, 2020), 30.0);
        sOrder.add(new Day(2, 1, 2020), 20.0);
        Assert.assertEquals(new Day(2, 1, 2020), sOrder.getTimePeriod(1));
    }

    @Test
    public void testAddDuplicateThrowsException() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 10.0);
        try {
            s.add(new Day(1, 1, 2020), 20.0);
            Assert.fail("Expected SeriesException on duplicate addition");
        } catch (SeriesException e) {
            // Expected
        }
    }

    @Test
    public void testAddIncompatiblePeriodClassThrowsException() {
        TimeSeries s = new TimeSeries("S", Day.class);
        try {
            s.add(new TimeSeriesDataItem(new Year(2020), 10.0));
            Assert.fail("Expected SeriesException on incompatible class");
        } catch (SeriesException e) {
            // Expected
        }
    }

    @Test
    public void testAddNullItemThrowsException() {
        TimeSeries s = new TimeSeries("S", Day.class);
        try {
            s.add((TimeSeriesDataItem) null);
            Assert.fail("Expected IllegalArgumentException on null item");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAddWithMaximumItemCountExceeded() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemCount(2);
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(2, 1, 2020), 2.0);
        s.add(new Day(3, 1, 2020), 3.0);

        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(new Day(3, 1, 2020), s.getTimePeriod(1));
    }

    @Test
    public void testUpdateByIndexAndPeriod() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s.add(d1, 10.0);
        s.add(d2, 20.0);

        s.addChangeListener(this);
        this.lastEvent = null;

        s.update(0, 15.0);
        Assert.assertEquals(15.0, s.getValue(0).doubleValue(), 1e-9);
        Assert.assertNotNull(this.lastEvent);

        this.lastEvent = null;
        s.update(d2, 25.0);
        Assert.assertEquals(25.0, s.getValue(d2).doubleValue(), 1e-9);
        Assert.assertNotNull(this.lastEvent);

        try {
            s.update(new Day(3, 1, 2020), 30.0);
            Assert.fail("Expected SeriesException when updating non-existent period");
        } catch (SeriesException e) {
            // Expected
        }
    }

    @Test
    public void testAddOrUpdate() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemCount(2);

        TimeSeriesDataItem item1 = s.addOrUpdate(new Day(1, 1, 2020), 10.0);
        Assert.assertNull(item1);
        Assert.assertEquals(1, s.getItemCount());

        TimeSeriesDataItem item2 = s.addOrUpdate(new Day(2, 1, 2020), 20.0);
        Assert.assertNull(item2);
        Assert.assertEquals(2, s.getItemCount());

        // Update existing item
        TimeSeriesDataItem itemUpdated = s.addOrUpdate(new Day(2, 1, 2020), 22.0);
        Assert.assertNotNull(itemUpdated);
        Assert.assertEquals(20.0, itemUpdated.getValue().doubleValue(), 1e-9);
        Assert.assertEquals(22.0, s.getValue(new Day(2, 1, 2020)).doubleValue(), 1e-9);
        Assert.assertEquals(2, s.getItemCount());

        // Insert new item exceeding maximum count
        TimeSeriesDataItem item3 = s.addOrUpdate(new Day(3, 1, 2020), 30.0);
        Assert.assertNull(item3);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(new Day(3, 1, 2020), s.getTimePeriod(1));

        // Insert in middle with addOrUpdate
        TimeSeries s2 = new TimeSeries("S2", Day.class);
        s2.addOrUpdate(new Day(1, 1, 2020), 10.0);
        s2.addOrUpdate(new Day(3, 1, 2020), 30.0);
        s2.addOrUpdate(new Day(2, 1, 2020), 20.0);
        Assert.assertEquals(3, s2.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), s2.getTimePeriod(1));

        try {
            s.addOrUpdate(null, 1.0);
            Assert.fail("Expected IllegalArgumentException on null period");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAddAndOrUpdate() {
        TimeSeries s1 = new TimeSeries("S1", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);

        TimeSeries s2 = new TimeSeries("S2", Day.class);
        s2.add(new Day(2, 1, 2020), 25.0);
        s2.add(new Day(3, 1, 2020), 30.0);

        TimeSeries overwritten = s1.addAndOrUpdate(s2);
        Assert.assertEquals(3, s1.getItemCount());
        Assert.assertEquals(25.0, s1.getValue(new Day(2, 1, 2020)).doubleValue(), 1e-9);
        Assert.assertEquals(30.0, s1.getValue(new Day(3, 1, 2020)).doubleValue(), 1e-9);

        Assert.assertEquals(1, overwritten.getItemCount());
        Assert.assertEquals(20.0, overwritten.getValue(0).doubleValue(), 1e-9);
        Assert.assertEquals(new Day(2, 1, 2020), overwritten.getTimePeriod(0));
    }

    @Test
    public void testRemoveAgedItemsLong() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemAge(2);

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d5 = new Day(5, 1, 2020);

        s.add(d1, 10.0);
        s.add(d2, 20.0);
        s.add(d3, 30.0);

        s.addChangeListener(this);
        this.lastEvent = null;

        // Age items against d5's millisecond timestamp
        long latestMillis = d5.getFirstMillisecond();
        s.removeAgedItems(latestMillis, true);

        // Max age is 2 days. d5 serial minus d3 serial = 2 (not > 2). d1 and d2 have difference > 2
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertEquals(d3, s.getTimePeriod(0));
        Assert.assertNotNull(this.lastEvent);

        // Aging with no items to remove
        this.lastEvent = null;
        s.removeAgedItems(latestMillis, true);
        Assert.assertNull(this.lastEvent);
    }

    @Test
    public void testClear() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(2, 1, 2020), 2.0);

        s.addChangeListener(this);
        this.lastEvent = null;

        s.clear();
        Assert.assertEquals(0, s.getItemCount());
        Assert.assertNotNull(this.lastEvent);

        this.lastEvent = null;
        s.clear();
        Assert.assertNull(this.lastEvent);
    }

    @Test
    public void testDeletePeriod() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        s.add(d1, 1.0);
        s.add(d2, 2.0);

        s.addChangeListener(this);
        this.lastEvent = null;

        s.delete(d1);
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertEquals(d2, s.getTimePeriod(0));
        Assert.assertNotNull(this.lastEvent);

        // Delete non-existent period
        this.lastEvent = null;
        s.delete(d1);
        Assert.assertEquals(1, s.getItemCount());
        Assert.assertNull(this.lastEvent);
    }

    @Test
    public void testDeleteRange() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(2, 1, 2020), 2.0);
        s.add(new Day(3, 1, 2020), 3.0);
        s.add(new Day(4, 1, 2020), 4.0);

        s.addChangeListener(this);
        this.lastEvent = null;

        s.delete(1, 2);
        Assert.assertEquals(2, s.getItemCount());
        Assert.assertEquals(new Day(1, 1, 2020), s.getTimePeriod(0));
        Assert.assertEquals(new Day(4, 1, 2020), s.getTimePeriod(1));
        Assert.assertNotNull(this.lastEvent);

        try {
            s.delete(2, 1);
            Assert.fail("Expected IllegalArgumentException when start > end");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCloneAndCreateCopy() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", "D", "R", Day.class);
        s.add(new Day(1, 1, 2020), 1.0);
        s.add(new Day(2, 1, 2020), 2.0);
        s.add(new Day(3, 1, 2020), 3.0);

        TimeSeries clone = (TimeSeries) s.clone();
        Assert.assertEquals(s, clone);
        Assert.assertNotSame(s, clone);
        Assert.assertNotSame(s.getDataItem(0), clone.getDataItem(0));

        // Modify clone, ensure original is unchanged
        clone.add(new Day(4, 1, 2020), 4.0);
        Assert.assertEquals(3, s.getItemCount());
        Assert.assertEquals(4, clone.getItemCount());

        // Test createCopy(int, int)
        TimeSeries copySubset = s.createCopy(1, 2);
        Assert.assertEquals(2, copySubset.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), copySubset.getTimePeriod(0));
        Assert.assertEquals(new Day(3, 1, 2020), copySubset.getTimePeriod(1));

        try {
            s.createCopy(-1, 2);
            Assert.fail("Expected IllegalArgumentException when start < 0");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            s.createCopy(2, 1);
            Assert.fail("Expected IllegalArgumentException when end < start");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCreateCopyByRegularTimePeriod() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new Day(2, 1, 2020), 2.0);
        s.add(new Day(4, 1, 2020), 4.0);
        s.add(new Day(6, 1, 2020), 6.0);

        // Exact match
        TimeSeries c1 = s.createCopy(new Day(2, 1, 2020), new Day(4, 1, 2020));
        Assert.assertEquals(2, c1.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), c1.getTimePeriod(0));
        Assert.assertEquals(new Day(4, 1, 2020), c1.getTimePeriod(1));

        // Start and end not in original series, but bracket elements
        TimeSeries c2 = s.createCopy(new Day(1, 1, 2020), new Day(5, 1, 2020));
        Assert.assertEquals(2, c2.getItemCount());
        Assert.assertEquals(new Day(2, 1, 2020), c2.getTimePeriod(0));
        Assert.assertEquals(new Day(4, 1, 2020), c2.getTimePeriod(1));

        // Range before any item in series
        TimeSeries c3 = s.createCopy(new Day(20, 12, 2019), new Day(25, 12, 2019));
        Assert.assertEquals(0, c3.getItemCount());

        // Range after all items in series
        TimeSeries c4 = s.createCopy(new Day(10, 1, 2020), new Day(15, 1, 2020));
        Assert.assertEquals(0, c4.getItemCount());

        // Argument validations
        try {
            s.createCopy((RegularTimePeriod) null, new Day(1, 1, 2020));
            Assert.fail("Expected IllegalArgumentException on null start");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            s.createCopy(new Day(1, 1, 2020), (RegularTimePeriod) null);
            Assert.fail("Expected IllegalArgumentException on null end");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            s.createCopy(new Day(5, 1, 2020), new Day(1, 1, 2020));
            Assert.fail("Expected IllegalArgumentException on start > end");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        TimeSeries s1 = new TimeSeries("Series", "D", "R", Day.class);
        TimeSeries s2 = new TimeSeries("Series", "D", "R", Day.class);

        Assert.assertTrue(s1.equals(s1));
        Assert.assertFalse(s1.equals(null));
        Assert.assertFalse(s1.equals("Not a TimeSeries"));
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.setDomainDescription("D2");
        Assert.assertFalse(s1.equals(s2));
        s2.setDomainDescription("D2");
        Assert.assertTrue(s1.equals(s2));

        s1.setRangeDescription("R2");
        Assert.assertFalse(s1.equals(s2));
        s2.setRangeDescription("R2");
        Assert.assertTrue(s1.equals(s2));

        s1.setMaximumItemAge(100);
        Assert.assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(100);
        Assert.assertTrue(s1.equals(s2));

        s1.setMaximumItemCount(100);
        Assert.assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(100);
        Assert.assertTrue(s1.equals(s2));

        s1.add(new Day(1, 1, 2020), 1.0);
        Assert.assertFalse(s1.equals(s2));
        s2.add(new Day(1, 1, 2020), 1.0);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(2, 1, 2020), 2.0);
        s2.add(new Day(2, 1, 2020), 2.0);
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(3, 1, 2020), 3.0);
        s2.add(new Day(3, 1, 2020), 3.0);
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        // Value difference
        s1.update(0, 99.0);
        Assert.assertFalse(s1.equals(s2));

        // Subclass equality check
        TimeSeries subSeries = new TimeSeries("Series", "D2", "R2", Day.class) {
            private static final long serialVersionUID = 1L;
        };
        Assert.assertFalse(s2.equals(subSeries));
    }
}