package org.jfree.data.time;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * High-coverage unit test suite for {@link TimeSeries}.
 */
public class TimeSeriesTest implements SeriesChangeListener, PropertyChangeListener {

    private TimeSeries series;
    private int seriesChangeEventsCount;
    private int propertyChangeEventsCount;
    private String lastPropertyName;

    @Before
    public void setUp() {
        this.series = new TimeSeries("Test Series", Day.class);
        this.series.addChangeListener(this);
        this.series.addPropertyChangeListener(this);
        this.seriesChangeEventsCount = 0;
        this.propertyChangeEventsCount = 0;
        this.lastPropertyName = null;
    }

    public void seriesChanged(SeriesChangeEvent event) {
        this.seriesChangeEventsCount++;
    }

    public void propertyChange(PropertyChangeEvent event) {
        this.propertyChangeEventsCount++;
        this.lastPropertyName = event.getPropertyName();
    }

    @Test
    public void testConstructors() {
        TimeSeries s1 = new TimeSeries("Series 1");
        assertEquals("Series 1", s1.getKey());
        assertEquals("Time", s1.getDomainDescription());
        assertEquals("Value", s1.getRangeDescription());
        assertEquals(Day.class, s1.getTimePeriodClass());
        assertEquals(0, s1.getItemCount());
        assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, s1.getMaximumItemAge());

        TimeSeries s2 = new TimeSeries("Series 2", Year.class);
        assertEquals("Series 2", s2.getKey());
        assertEquals(Year.class, s2.getTimePeriodClass());

        TimeSeries s3 = new TimeSeries("Series 3", "Domain", "Range", Month.class);
        assertEquals("Series 3", s3.getKey());
        assertEquals("Domain", s3.getDomainDescription());
        assertEquals("Range", s3.getRangeDescription());
        assertEquals(Month.class, s3.getTimePeriodClass());
    }

    @Test
    public void testSetDomainDescription() {
        this.series.setDomainDescription("New Domain");
        assertEquals("New Domain", this.series.getDomainDescription());
        assertEquals(1, this.propertyChangeEventsCount);
        assertEquals("Domain", this.lastPropertyName);

        this.series.setDomainDescription(null);
        assertNull(this.series.getDomainDescription());
    }

    @Test
    public void testSetRangeDescription() {
        this.series.setRangeDescription("New Range");
        assertEquals("New Range", this.series.getRangeDescription());
        assertEquals(1, this.propertyChangeEventsCount);
        assertEquals("Range", this.lastPropertyName);

        this.series.setRangeDescription(null);
        assertNull(this.series.getRangeDescription());
    }

    @Test
    public void testSetMaximumItemCount() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);
        this.series.add(new Day(3, 1, 2020), 30.0);

        this.series.setMaximumItemCount(2);
        assertEquals(2, this.series.getItemCount());
        assertEquals(2, this.series.getMaximumItemCount());
        assertEquals(new Day(2, 1, 2020), this.series.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2020), this.series.getTimePeriod(1));

        this.series.add(new Day(4, 1, 2020), 40.0);
        assertEquals(2, this.series.getItemCount());
        assertEquals(new Day(3, 1, 2020), this.series.getTimePeriod(0));
        assertEquals(new Day(4, 1, 2020), this.series.getTimePeriod(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        this.series.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemAge() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(5, 1, 2020), 20.0);
        this.series.add(new Day(10, 1, 2020), 30.0);

        this.series.setMaximumItemAge(5);
        assertEquals(5L, this.series.getMaximumItemAge());
        // Day 10 - Day 1 = 9 (> 5), Day 10 - Day 5 = 5 (<= 5). Day 1 should be removed.
        assertEquals(2, this.series.getItemCount());
        assertEquals(new Day(5, 1, 2020), this.series.getTimePeriod(0));
        assertEquals(new Day(10, 1, 2020), this.series.getTimePeriod(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        this.series.setMaximumItemAge(-1);
    }

    @Test
    public void testGetDataItemAndIndex() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d3, 30.0);

        assertEquals(d1, this.series.getDataItem(0).getPeriod());
        assertEquals(d3, this.series.getDataItem(1).getPeriod());
        assertEquals(new Double(10.0), this.series.getDataItem(d1).getValue());
        assertNull(this.series.getDataItem(d2));

        assertEquals(0, this.series.getIndex(d1));
        assertEquals(1, this.series.getIndex(d3));
        assertTrue(this.series.getIndex(d2) < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNull() {
        this.series.getIndex(null);
    }

    @Test
    public void testGetItemsUnmodifiable() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        List items = this.series.getItems();
        assertEquals(1, items.size());
        try {
            items.add(new TimeSeriesDataItem(new Day(2, 1, 2020), 20.0));
            fail("Expected UnsupportedOperationException");
        }
        catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testGetTimePeriodAndNext() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        assertEquals(d1, this.series.getTimePeriod(0));
        assertEquals(d2, this.series.getTimePeriod(1));
        assertEquals(new Day(3, 1, 2020), this.series.getNextTimePeriod());
    }

    @Test
    public void testGetTimePeriodsAndUnique() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        Collection periods = this.series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(d1));
        assertTrue(periods.contains(d2));

        TimeSeries other = new TimeSeries("Other", Day.class);
        other.add(d2, 20.0);
        other.add(d3, 30.0);

        Collection unique = this.series.getTimePeriodsUniqueToOtherSeries(other);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(d3));
    }

    @Test
    public void testGetValue() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        this.series.add(d1, 10.5);

        assertEquals(10.5, this.series.getValue(0).doubleValue(), 1e-9);
        assertEquals(10.5, this.series.getValue(d1).doubleValue(), 1e-9);
        assertNull(this.series.getValue(d2));
    }

    @Test
    public void testAddVariants() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d4 = new Day(4, 1, 2020);

        this.series.add(new TimeSeriesDataItem(d1, 1.0));
        assertEquals(1, this.seriesChangeEventsCount);

        this.series.add(new TimeSeriesDataItem(d2, 2.0), false);
        assertEquals(1, this.seriesChangeEventsCount);

        this.series.add(d3, 3.0);
        assertEquals(2, this.seriesChangeEventsCount);

        this.series.add(d4, 4.0, false);
        assertEquals(2, this.seriesChangeEventsCount);

        Day d5 = new Day(5, 1, 2020);
        this.series.add(d5, (Number) new Double(5.0));
        assertEquals(3, this.seriesChangeEventsCount);

        Day d6 = new Day(6, 1, 2020);
        this.series.add(d6, (Number) null, true);
        assertEquals(4, this.seriesChangeEventsCount);
        assertNull(this.series.getValue(d6));

        // Insert in between (out of order)
        Day d2_5 = new Day(2, 1, 2020); // duplicate test handled separately
        Day insertDay = new Day(20, 1, 2019); // before d1
        this.series.add(insertDay, 0.5);
        assertEquals(0, this.series.getIndex(insertDay));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        this.series.add((TimeSeriesDataItem) null, true);
    }

    @Test(expected = SeriesException.class)
    public void testAddWrongPeriodClass() {
        this.series.add(new TimeSeriesDataItem(new Year(2020), 10.0));
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicate() {
        Day d1 = new Day(1, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d1, 20.0);
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicateMiddle() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d3, 30.0);
        this.series.add(d1, 15.0); // Duplicate of first element when count > 0
    }

    @Test
    public void testUpdate() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        int initialEvents = this.seriesChangeEventsCount;
        this.series.update(d1, 15.0);
        assertEquals(15.0, this.series.getValue(d1).doubleValue(), 1e-9);
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);

        this.series.update(1, 25.0);
        assertEquals(25.0, this.series.getValue(1).doubleValue(), 1e-9);
        assertEquals(initialEvents + 2, this.seriesChangeEventsCount);
    }

    @Test(expected = SeriesException.class)
    public void testUpdateNonExistent() {
        this.series.update(new Day(1, 1, 2020), 10.0);
    }

    @Test
    public void testAddOrUpdate() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.setMaximumItemCount(2);

        TimeSeriesDataItem item1 = this.series.addOrUpdate(d1, 10.0);
        assertNull(item1);
        assertEquals(1, this.series.getItemCount());

        TimeSeriesDataItem item2 = this.series.addOrUpdate(d2, 20.0);
        assertNull(item2);
        assertEquals(2, this.series.getItemCount());

        // Update existing d2
        TimeSeriesDataItem itemUpdated = this.series.addOrUpdate(d2, (Number) 25.0);
        assertNotNull(itemUpdated);
        assertEquals(20.0, itemUpdated.getValue().doubleValue(), 1e-9);
        assertEquals(25.0, this.series.getValue(d2).doubleValue(), 1e-9);
        assertEquals(2, this.series.getItemCount());

        // Add d3 (triggers maximumItemCount eviction of d1)
        TimeSeriesDataItem item3 = this.series.addOrUpdate(d3, 30.0);
        assertNull(item3);
        assertEquals(2, this.series.getItemCount());
        assertEquals(d2, this.series.getTimePeriod(0));
        assertEquals(d3, this.series.getTimePeriod(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullPeriod() {
        this.series.addOrUpdate(null, 10.0);
    }

    @Test
    public void testAddAndOrUpdate() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        TimeSeries other = new TimeSeries("Other", Day.class);
        other.add(d2, 25.0);
        other.add(d3, 30.0);

        TimeSeries overwritten = this.series.addAndOrUpdate(other);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(d2, overwritten.getTimePeriod(0));
        assertEquals(20.0, overwritten.getValue(0).doubleValue(), 1e-9);

        assertEquals(3, this.series.getItemCount());
        assertEquals(25.0, this.series.getValue(d2).doubleValue(), 1e-9);
        assertEquals(30.0, this.series.getValue(d3).doubleValue(), 1e-9);
    }

    @Test
    public void testRemoveAgedItems() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(10, 1, 2020);

        this.series.setMaximumItemAge(5);
        this.series.add(d1, 1.0);
        this.series.add(d2, 2.0);
        // Adding d3 should automatically age d1 and d2
        this.series.add(d3, 3.0);
        assertEquals(1, this.series.getItemCount());
        assertEquals(d3, this.series.getTimePeriod(0));

        // Test removeAgedItems with latest timestamp
        Day d4 = new Day(15, 1, 2020);
        this.series.removeAgedItems(d4.getFirstMillisecond(), true);
        assertEquals(1, this.series.getItemCount()); // 15 - 10 = 5 (<= 5)

        Day d5 = new Day(20, 1, 2020);
        this.series.removeAgedItems(d5.getFirstMillisecond(), true);
        assertEquals(0, this.series.getItemCount()); // 20 - 10 = 10 (> 5)
    }

    @Test
    public void testClear() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);
        int initialEvents = this.seriesChangeEventsCount;

        this.series.clear();
        assertEquals(0, this.series.getItemCount());
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);

        // Clearing an empty series should not fire change event
        this.series.clear();
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);
    }

    @Test
    public void testDeletePeriod() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        int initialEvents = this.seriesChangeEventsCount;
        this.series.delete(d1);
        assertEquals(1, this.series.getItemCount());
        assertEquals(d2, this.series.getTimePeriod(0));
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);

        // Deleting non-existent period
        this.series.delete(d3);
        assertEquals(1, this.series.getItemCount());
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);
    }

    @Test
    public void testDeleteRange() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);
        this.series.add(new Day(3, 1, 2020), 30.0);
        this.series.add(new Day(4, 1, 2020), 40.0);

        int initialEvents = this.seriesChangeEventsCount;
        this.series.delete(1, 2);
        assertEquals(2, this.series.getItemCount());
        assertEquals(new Day(1, 1, 2020), this.series.getTimePeriod(0));
        assertEquals(new Day(4, 1, 2020), this.series.getTimePeriod(1));
        assertEquals(initialEvents + 1, this.seriesChangeEventsCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRangeInvalid() {
        this.series.delete(2, 1);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);

        TimeSeries clone = (TimeSeries) this.series.clone();
        assertEquals(this.series, clone);
        assertNotSame(this.series, clone);

        // Modify clone, original should not be affected
        clone.add(new Day(3, 1, 2020), 30.0);
        assertEquals(2, this.series.getItemCount());
        assertEquals(3, clone.getItemCount());
        assertFalse(this.series.equals(clone));
    }

    @Test
    public void testCreateCopyIntRange() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);
        this.series.add(new Day(3, 1, 2020), 30.0);

        TimeSeries copy = this.series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Day(2, 1, 2020), copy.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2020), copy.getTimePeriod(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNegativeStart() throws CloneNotSupportedException {
        this.series.createCopy(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyEndBeforeStart() throws CloneNotSupportedException {
        this.series.createCopy(2, 1);
    }

    @Test
    public void testCreateCopyPeriodRange() throws CloneNotSupportedException {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d4 = new Day(4, 1, 2020);

        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        // Exact match range
        TimeSeries copy1 = this.series.createCopy(d2, d3);
        assertEquals(2, copy1.getItemCount());

        // Subset within range (starting before d2, ending after d3)
        TimeSeries copy2 = this.series.createCopy(d1, d4);
        assertEquals(2, copy2.getItemCount());

        // Range completely before data
        Day d0 = new Day(31, 12, 2019);
        TimeSeries copyBefore = this.series.createCopy(d0, d1);
        assertEquals(0, copyBefore.getItemCount());

        // Range completely after data
        Day d5 = new Day(5, 1, 2020);
        Day d6 = new Day(6, 1, 2020);
        TimeSeries copyAfter = this.series.createCopy(d5, d6);
        assertEquals(0, copyAfter.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullStart() throws CloneNotSupportedException {
        this.series.createCopy(null, new Day(1, 1, 2020));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullEnd() throws CloneNotSupportedException {
        this.series.createCopy(new Day(1, 1, 2020), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyStartAfterEnd() throws CloneNotSupportedException {
        this.series.createCopy(new Day(2, 1, 2020), new Day(1, 1, 2020));
    }

    @Test
    public void testEquals() {
        TimeSeries s1 = new TimeSeries("Series", "Domain", "Range", Day.class);
        TimeSeries s2 = new TimeSeries("Series", "Domain", "Range", Day.class);

        assertTrue(s1.equals(s1));
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Not a TimeSeries"));
        assertTrue(s1.equals(s2));

        // Different key
        s2 = new TimeSeries("Series 2", "Domain", "Range", Day.class);
        assertFalse(s1.equals(s2));

        // Different domain
        s2 = new TimeSeries("Series", "Domain 2", "Range", Day.class);
        assertFalse(s1.equals(s2));

        // Different range
        s2 = new TimeSeries("Series", "Domain", "Range 2", Day.class);
        assertFalse(s1.equals(s2));

        // Different max age
        s2 = new TimeSeries("Series", "Domain", "Range", Day.class);
        s2.setMaximumItemAge(10);
        assertFalse(s1.equals(s2));

        // Different max count
        s2 = new TimeSeries("Series", "Domain", "Range", Day.class);
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));

        // Different item count
        s2 = new TimeSeries("Series", "Domain", "Range", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));

        // Same items
        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));

        // Different item values
        s2.update(0, 20.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testHashCode() {
        TimeSeries s1 = new TimeSeries("Series", "Domain", "Range", Day.class);
        TimeSeries s2 = new TimeSeries("Series", "Domain", "Range", Day.class);
        assertEquals(s1.hashCode(), s2.hashCode());

        // 1 item
        s1.add(new Day(1, 1, 2020), 10.0);
        s2.add(new Day(1, 1, 2020), 10.0);
        assertEquals(s1.hashCode(), s2.hashCode());

        // 2 items
        s1.add(new Day(2, 1, 2020), 20.0);
        s2.add(new Day(2, 1, 2020), 20.0);
        assertEquals(s1.hashCode(), s2.hashCode());

        // 3 items (triggers middle item hash code computation)
        s1.add(new Day(3, 1, 2020), 30.0);
        s2.add(new Day(3, 1, 2020), 30.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Day(2, 1, 2020), 20.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.series);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        TimeSeries deserialized = (TimeSeries) in.readObject();
        in.close();

        assertEquals(this.series, deserialized);
    }
}