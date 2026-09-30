package org.jfree.data.time;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link TimeSeries} class.
 */
public class TimeSeriesTest {

    private TimeSeries series;
    private Day day1;
    private Day day2;
    private Day day3;
    private Day day4;

    @Before
    public void setUp() throws Exception {
        series = new TimeSeries("Test Series");
        day1 = new Day(1, 1, 2010);
        day2 = new Day(2, 1, 2010);
        day3 = new Day(3, 1, 2010);
        day4 = new Day(4, 1, 2010);
    }

    // ------------------- Constructor Tests -------------------

    @Test
    public void testConstructorWithName() {
        TimeSeries s = new TimeSeries("A");
        assertEquals("A", s.getKey());
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(Day.class, s.getTimePeriodClass());
        assertEquals(0, s.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullName() {
        new TimeSeries(null);
    }

    @Test
    public void testConstructorWithNameAndTimePeriodClass() {
        TimeSeries s = new TimeSeries("B", Day.class);
        assertEquals("B", s.getKey());
        assertEquals(Day.class, s.getTimePeriodClass());
    }

    @Test
    public void testConstructorWithNullTimePeriodClass() {
        // The constructor does not explicitly reject null, but later operations
        // may fail.  We test that an instance is created without exception.
        TimeSeries s = new TimeSeries("C", null);
        assertNull(s.getTimePeriodClass());
    }

    @Test
    public void testConstructorWithFullParams() {
        TimeSeries s = new TimeSeries("D", "Domain", "Range", Day.class);
        assertEquals("D", s.getKey());
        assertEquals("Domain", s.getDomainDescription());
        assertEquals("Range", s.getRangeDescription());
        assertEquals(Day.class, s.getTimePeriodClass());
    }

    // ------------------- Property Getters/Setters -------------------

    @Test
    public void testSetDomainDescription() {
        series.setDomainDescription("New Domain");
        assertEquals("New Domain", series.getDomainDescription());
    }

    @Test
    public void testSetDomainDescriptionFiresPropertyChange() {
        final boolean[] fired = {false};
        series.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Domain".equals(evt.getPropertyName())) {
                    assertEquals("Time", evt.getOldValue());
                    assertEquals("Custom", evt.getNewValue());
                    fired[0] = true;
                }
            }
        });
        series.setDomainDescription("Custom");
        assertTrue(fired[0]);
    }

    @Test
    public void testSetRangeDescription() {
        series.setRangeDescription("New Range");
        assertEquals("New Range", series.getRangeDescription());
    }

    @Test
    public void testSetRangeDescriptionFiresPropertyChange() {
        final boolean[] fired = {false};
        series.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Range".equals(evt.getPropertyName())) {
                    assertEquals("Value", evt.getOldValue());
                    assertEquals("Custom Range", evt.getNewValue());
                    fired[0] = true;
                }
            }
        });
        series.setRangeDescription("Custom Range");
        assertTrue(fired[0]);
    }

    @Test
    public void testGetMaximumItemCountDefault() {
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        series.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemCountZero() {
        series.setMaximumItemCount(0);
        assertEquals(0, series.getMaximumItemCount());
        // adding an item will immediately remove it because count > maximum?
        series.add(new Day(1, 1, 2000), 1.0);
        // After exceeding max count, old items are removed; here count becomes 1 > 0,
        // so item at index 0 is removed, count back to 0.
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testSetMaximumItemCountRemovesOldest() {
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        series.add(day3, 30.0); // now 3 items
        series.setMaximumItemCount(2);
        assertEquals(2, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
        assertEquals(day3, series.getTimePeriod(1));
    }

    @Test
    public void testGetMaximumItemAgeDefault() {
        assertEquals(Long.MAX_VALUE, series.getMaximumItemAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        series.setMaximumItemAge(-1);
    }

    @Test
    public void testSetMaximumItemAgeRemovesAgedItems() {
        // Days 1, 30, and 60
        Day d1 = new Day(1, 1, 2010);
        Day d30 = new Day(30, 1, 2010);
        Day d60 = new Day(1, 3, 2010); // ~60 days later
        series.add(d1, 1.0);
        series.add(d30, 2.0);
        series.add(d60, 3.0);
        // maximum age of 10 days should remove d1 and d30, keep only d60
        series.setMaximumItemAge(10);
        assertEquals(1, series.getItemCount());
        assertEquals(d60, series.getTimePeriod(0));
    }

    // ------------------- Add Methods -------------------

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        series.add((TimeSeriesDataItem) null, true);
    }

    @Test(expected = SeriesException.class)
    public void testAddItemWrongTimePeriodClass() {
        // series expects Day, but we add a Month item
        series.add(new TimeSeriesDataItem(new Month(1, 2010), 1.0));
    }

    @Test
    public void testAddItem() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(day1, 55.5);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertEquals(day1, series.getTimePeriod(0));
        assertEquals(55.5, series.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testAddItemKeepOrder() {
        series.add(day3, 3.0);
        series.add(day1, 1.0); // out of order
        series.add(day2, 2.0); // insert in the middle
        assertEquals(3, series.getItemCount());
        assertEquals(day1, series.getTimePeriod(0));
        assertEquals(day2, series.getTimePeriod(1));
        assertEquals(day3, series.getTimePeriod(2));
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicatePeriod() {
        series.add(day1, 1.0);
        series.add(day1, 2.0);
    }

    @Test
    public void testAddWithNotifyFalse() {
        final boolean[] fired = {false};
        series.addChangeListener(e -> fired[0] = true);
        series.add(new TimeSeriesDataItem(day1, 10.0), false);
        assertFalse(fired[0]);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddExceedMaximumItemCount() {
        series.setMaximumItemCount(2);
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0); // should remove day1
        assertEquals(2, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
        assertEquals(day3, series.getTimePeriod(1));
    }

    // Convenience add methods

    @Test
    public void testAddRegularTimePeriodDouble() {
        series.add(day1, 42.0);
        assertEquals(42.0, series.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testAddRegularTimePeriodDoubleNotify() {
        series.add(day1, 7.0, false);
        assertEquals(7.0, series.getValue(0));
    }

    @Test
    public void testAddRegularTimePeriodNumber() {
        series.add(day1, new Double(100.0));
        assertEquals(100.0, series.getValue(0));
    }

    @Test
    public void testAddRegularTimePeriodNumberNotify() {
        series.add(day1, Integer.valueOf(5), false);
        assertEquals(5, series.getValue(0));
    }

    // ------------------- Update Methods -------------------

    @Test(expected = SeriesException.class)
    public void testUpdateNonExistentPeriod() {
        series.update(day1, 10.0);
    }

    @Test
    public void testUpdateExistingPeriod() {
        series.add(day1, 1.0);
        series.update(day1, 99.0);
        assertEquals(99.0, series.getValue(day1).doubleValue(), 1e-10);
    }

    @Test
    public void testUpdateByIndex() {
        series.add(day1, 1.0);
        series.update(0, 50.0);
        assertEquals(50.0, series.getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateByInvalidIndex() {
        series.update(0, 10.0); // empty series
    }

    // ------------------- addOrUpdate -------------------

    @Test
    public void testAddOrUpdateNewPeriod() {
        TimeSeriesDataItem result = series.addOrUpdate(day1, 100.0);
        assertNull(result);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddOrUpdateExistingPeriod() {
        series.add(day1, 10.0);
        TimeSeriesDataItem old = series.addOrUpdate(day1, 20.0);
        assertNotNull(old);
        assertEquals(day1, old.getPeriod());
        assertEquals(10.0, old.getValue().doubleValue(), 1e-10);
        assertEquals(20.0, series.getValue(0).doubleValue(), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullPeriod() {
        series.addOrUpdate(null, 1.0);
    }

    @Test
    public void testAddOrUpdateExceedMaxCount() {
        series.setMaximumItemCount(2);
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.addOrUpdate(day3, 3.0); // should push out day1
        assertEquals(2, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
    }

    @Test
    public void testAddAndOrUpdate() {
        TimeSeries other = new TimeSeries("Other");
        other.add(day1, 10.0);
        other.add(day2, 20.0);
        series.add(day1, 100.0); // will be overwritten

        TimeSeries overwritten = series.addAndOrUpdate(other);
        assertEquals(2, series.getItemCount());
        assertEquals(10.0, series.getValue(day1).doubleValue(), 1e-10);
        assertEquals(20.0, series.getValue(day2).doubleValue(), 1e-10);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(day1, overwritten.getTimePeriod(0));
        assertEquals(100.0, overwritten.getValue(0).doubleValue(), 1e-10);
    }

    // ------------------- Removal / Deletion -------------------

    @Test
    public void testDeletePeriodExisting() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.delete(day1);
        assertEquals(1, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
    }

    @Test
    public void testDeletePeriodNonExistent() {
        series.add(day1, 1.0);
        series.delete(day2); // no effect
        assertEquals(1, series.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteStartGreaterThanEnd() {
        series.delete(2, 1);
    }

    @Test
    public void testDeleteStartEndValid() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        series.delete(0, 1); // delete first two
        assertEquals(1, series.getItemCount());
        assertEquals(day3, series.getTimePeriod(0));
    }

    @Test
    public void testDeleteStartEndSameIndex() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.delete(1, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(day1, series.getTimePeriod(0));
    }

    // ------------------- clear -------------------

    @Test
    public void testClear() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        final boolean[] fired = {false};
        series.addChangeListener(e -> fired[0] = true);
        series.clear();
        assertEquals(0, series.getItemCount());
        assertTrue(fired[0]);
    }

    @Test
    public void testClearEmptySeries() {
        final boolean[] fired = {false};
        series.addChangeListener(e -> fired[0] = true);
        series.clear();
        assertEquals(0, series.getItemCount());
        assertFalse(fired[0]);
    }

    // ------------------- removeAgedItems -------------------

    @Test
    public void testRemoveAgedItemsNoRemoval() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.setMaximumItemAge(10); // age between day1 and day2 is 1 day
        int count = series.getItemCount();
        series.removeAgedItems(true);
        assertEquals(count, series.getItemCount());
    }

    @Test
    public void testRemoveAgedItemsWithRemoval() {
        Day d1 = new Day(1, 1, 2000);
        Day d2 = new Day(2, 1, 2000);
        Day d3 = new Day(3, 1, 2000);
        series.add(d1, 1.0);
        series.add(d2, 2.0);
        series.add(d3, 3.0);
        series.setMaximumItemAge(0); // any difference > 0 will trigger removal
        series.removeAgedItems(false);
        assertEquals(1, series.getItemCount());
        assertEquals(d3, series.getTimePeriod(0));
    }

    @Test
    public void testRemoveAgedItemsWithNotify() {
        series.add(day1, 1.0);
        series.add(day3, 3.0);
        series.setMaximumItemAge(0);
        final boolean[] fired = {false};
        series.addChangeListener(e -> fired[0] = true);
        series.removeAgedItems(true);
        assertTrue(fired[0]);
    }

    @Test
    public void testRemoveAgedItemsWithLatest() {
        // using the method that takes a latest time in millis
        series.add(day1, 1.0);
        series.add(day3, 3.0);
        series.setMaximumItemAge(0);
        long latest = day3.getStart().getTime() + 1; // after day3
        series.removeAgedItems(latest, true);
        assertEquals(1, series.getItemCount());
    }

    // ------------------- getIndex / getDataItem -------------------

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullPeriod() {
        series.getIndex(null);
    }

    @Test
    public void testGetIndex() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        assertEquals(0, series.getIndex(day1));
        assertEquals(1, series.getIndex(day2));
        assertEquals(-1, series.getIndex(day3));
    }

    @Test
    public void testGetDataItemByPeriod() {
        series.add(day1, 1.0);
        TimeSeriesDataItem item = series.getDataItem(day1);
        assertNotNull(item);
        assertEquals(day1, item.getPeriod());
        assertEquals(1.0, item.getValue().doubleValue(), 1e-10);
    }

    @Test
    public void testGetDataItemByPeriodNotFound() {
        series.add(day1, 1.0);
        assertNull(series.getDataItem(day2));
    }

    // ------------------- getTimePeriods / getTimePeriodsUniqueToOtherSeries -

    @Test
    public void testGetTimePeriods() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        Collection<RegularTimePeriod> periods = series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(day1));
        assertTrue(periods.contains(day2));
    }

    @Test
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries other = new TimeSeries("Other");
        other.add(day1, 10.0);
        other.add(day3, 30.0);
        series.add(day1, 1.0);
        Collection<?> unique = series.getTimePeriodsUniqueToOtherSeries(other);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(day3));
    }

    // ------------------- getNextTimePeriod -------------------

    @Test
    public void testGetNextTimePeriod() {
        series.add(day1, 1.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(day2, next);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNextTimePeriodEmptySeries() {
        series.getNextTimePeriod(); // getItemCount()-1 = -1
    }

    // ------------------- getItems unmodifiable -------------------

    @Test(expected = UnsupportedOperationException.class)
    public void testGetItemsReturnsUnmodifiable() {
        series.add(day1, 1.0);
        List<?> items = series.getItems();
        items.add(new TimeSeriesDataItem(day2, 2.0));
    }

    // ------------------- Clone -------------------

    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(day1, 1.0);
        TimeSeries clone = (TimeSeries) series.clone();
        assertNotSame(series, clone);
        assertEquals(series, clone);
        // modify original, clone unchanged
        series.update(0, 99.0);
        assertEquals(1.0, clone.getValue(0));
        // add item to clone, original unchanged
        clone.add(day2, 2.0);
        assertEquals(1, series.getItemCount());
        assertEquals(2, clone.getItemCount());
    }

    // ------------------- createCopy by indices -------------------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyStartNegative() throws Exception {
        series.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyStartGreaterThanEnd() throws Exception {
        series.createCopy(1, 0);
    }

    @Test
    public void testCreateCopyByIndicesEmptySeries() throws Exception {
        TimeSeries copy = series.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopyByIndices() throws Exception {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        TimeSeries copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(day1, copy.getTimePeriod(0));
        assertEquals(day2, copy.getTimePeriod(1));
    }

    // ------------------- createCopy by periods -------------------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullStart() throws Exception {
        series.createCopy(null, day2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullEnd() throws Exception {
        series.createCopy(day1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyStartAfterEnd() throws Exception {
        series.createCopy(day3, day1);
    }

    @Test
    public void testCreateCopyByPeriodsEmptyResult() throws Exception {
        series.add(day1, 1.0);
        TimeSeries copy = series.createCopy(day3, day4); // start after data
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopyByPeriodsPartialRange() throws Exception {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        TimeSeries copy = series.createCopy(day1, day2);
        assertEquals(2, copy.getItemCount());
    }

    // ------------------- equals -------------------

    @Test
    public void testEqualsSameObject() {
        assertTrue(series.equals(series));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(series.equals("String"));
    }

    @Test
    public void testEqualsDifferentDomainDescription() {
        TimeSeries other = new TimeSeries("Test Series");
        other.setDomainDescription("Different");
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentRangeDescription() {
        TimeSeries other = new TimeSeries("Test Series");
        other.setRangeDescription("Different");
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentMaxItemCount() {
        TimeSeries other = new TimeSeries("Test Series");
        other.setMaximumItemCount(10);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentMaxItemAge() {
        TimeSeries other = new TimeSeries("Test Series");
        other.setMaximumItemAge(100);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentDataItems() {
        series.add(day1, 1.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(day1, 2.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsFull() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.setDomainDescription("Domain");
        series.setRangeDescription("Range");
        series.setMaximumItemCount(50);
        series.setMaximumItemAge(365);

        TimeSeries other = new TimeSeries("Test Series");
        other.add(day1, 1.0);
        other.add(day2, 2.0);
        other.setDomainDescription("Domain");
        other.setRangeDescription("Range");
        other.setMaximumItemCount(50);
        other.setMaximumItemAge(365);

        assertTrue(series.equals(other));
    }

    // ------------------- hashCode -------------------

    @Test
    public void testHashCodeConsistency() {
        int h1 = series.hashCode();
        int h2 = series.hashCode();
        assertEquals(h1, h2);
    }

    @Test
    public void testHashCodeEquality() {
        TimeSeries s1 = new TimeSeries("S");
        TimeSeries s2 = new TimeSeries("S");
        s1.add(day1, 1.0);
        s2.add(day1, 1.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
```
