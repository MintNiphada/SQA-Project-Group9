package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.general.SeriesException;

public class TimeSeriesTest {
    private TimeSeries series;
    private Day day1, day2, day3, day4;

    @Before
    public void setUp() {
        series = new TimeSeries("Test Series");
        day1 = new Day(1, 1, 2000);
        day2 = new Day(2, 1, 2000);
        day3 = new Day(3, 1, 2000);
        day4 = new Day(4, 1, 2000);
    }

    // Constructors
    @Test
    public void testDefaultConstructor() {
        assertEquals("Test Series", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertNull(series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, series.getMaximumItemAge());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        assertNotNull(series.getItems());
        assertTrue(series.getItems().isEmpty());
    }

    @Test
    public void testConstructorWithDescriptions() {
        TimeSeries s = new TimeSeries("Test", "CustomDomain", "CustomRange");
        assertEquals("Test", s.getKey());
        assertEquals("CustomDomain", s.getDomainDescription());
        assertEquals("CustomRange", s.getRangeDescription());
    }

    // Domain and Range descriptions
    @Test
    public void testSetDomainDescription() {
        series.setDomainDescription("D");
        assertEquals("D", series.getDomainDescription());
    }

    @Test
    public void testSetRangeDescription() {
        series.setRangeDescription("R");
        assertEquals("R", series.getRangeDescription());
    }

    // MaximumItemCount
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        series.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemCountRemovesExcess() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        series.setMaximumItemCount(2);
        assertEquals(2, series.getItemCount());
        // oldest removed, so day1 gone
        assertEquals(day2, series.getTimePeriod(0));
        assertEquals(2.0, series.getValue(0).doubleValue(), 0.0001);
    }

    // MaximumItemAge
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        series.setMaximumItemAge(-1L);
    }

    @Test
    public void testSetMaximumItemAgeRemovesOld() {
        series.setMaximumItemAge(0); // any age gap >0 removes items
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0); // second add already calls removeAgedItems, so only latest remains
        assertEquals(1, series.getItemCount());
        assertEquals(day3, series.getTimePeriod(0));
    }

    // add(TimeSeriesDataItem) and variants
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        series.add((TimeSeriesDataItem) null);
    }

    @Test
    public void testAddValidItem() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(day1, 1.0);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertEquals(1.0, series.getValue(0).doubleValue(), 0.0);
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicatePeriod() {
        series.add(day1, 1.0);
        series.add(day1, 2.0);
    }

    @Test(expected = SeriesException.class)
    public void testAddDifferentPeriodClass() {
        series.add(day1, 1.0);
        series.add(new Year(2000), 2.0);
    }

    @Test
    public void testAddOutOfOrderPeriod() {
        series.add(day3, 3.0);
        series.add(day1, 1.0); // inserted before day3
        assertEquals(2, series.getItemCount());
        assertEquals(day1, series.getTimePeriod(0));
        assertEquals(day3, series.getTimePeriod(1));
    }

    @Test
    public void testAddDouble() {
        series.add(day1, 5.5);
        assertEquals(5.5, series.getValue(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddNumberNull() {
        series.add(day1, (Number) null);
        assertNull(series.getValue(0));
    }

    @Test
    public void testAddWithNotifyFalse() {
        series.add(day1, 1.0, false);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddNumberOverloaded() {
        series.add(day1, new Double(3.14));
        assertEquals(3.14, series.getValue(0).doubleValue(), 0.0);
    }

    // update methods
    @Test(expected = SeriesException.class)
    public void testUpdatePeriodNotExist() {
        series.update(day1, 5.0);
    }

    @Test
    public void testUpdateExistingPeriod() {
        series.add(day1, 1.0);
        series.update(day1, 5.0);
        assertEquals(5.0, series.getValue(0).doubleValue(), 0.0);
    }

    @Test
    public void testUpdateIndex() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.update(0, 10.0);
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0);
    }

    @Test
    public void testUpdateIndexMinMaxRecalc() {
        series.add(day1, 5.0);
        series.add(day2, 10.0);
        assertEquals(5.0, series.getMinY(), 0.0);
        assertEquals(10.0, series.getMaxY(), 0.0);
        series.update(0, 3.0); // new min
        assertEquals(3.0, series.getMinY(), 0.0);
        series.update(1, 12.0); // new max
        assertEquals(12.0, series.getMaxY(), 0.0);
    }

    @Test
    public void testUpdateIndexWithOldNullValue() {
        series.add(day1, (Number) null);
        series.update(0, 5.0);
        assertEquals(5.0, series.getMinY(), 0.0);
        assertEquals(5.0, series.getMaxY(), 0.0);
    }

    // addOrUpdate
    @Test
    public void testAddOrUpdateNewPeriod() {
        assertNull(series.addOrUpdate(day1, 1.0));
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddOrUpdateExistingPeriod() {
        series.add(day1, 1.0);
        TimeSeriesDataItem overwritten = series.addOrUpdate(day1, 2.0);
        assertNotNull(overwritten);
        assertEquals(1.0, overwritten.getValue().doubleValue(), 0.0);
        assertEquals(2.0, series.getValue(0).doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullItem() {
        series.addOrUpdate((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class)
    public void testAddOrUpdateWrongPeriodClass() {
        series.add(day1, 1.0);
        series.addOrUpdate(new Year(2000), 2.0);
    }

    @Test
    public void testAddOrUpdateOverloaded() {
        series.addOrUpdate(day1, new Integer(10));
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0);
    }

    // addAndOrUpdate
    @Test
    public void testAddAndOrUpdate() {
        TimeSeries other = new TimeSeries("Other");
        other.add(day1, 1.0);
        other.add(day2, 2.0);
        other.add(day3, 3.0);

        series.add(day1, 0.5);
        series.add(day2, 2.5);
        TimeSeries overwritten = series.addAndOrUpdate(other);
        assertEquals(2, overwritten.getItemCount());
        assertEquals(0.5, overwritten.getValue(0).doubleValue(), 0.0);
        assertEquals(2.5, overwritten.getValue(1).doubleValue(), 0.0);
        // series updated
        assertEquals(1.0, series.getValue(day1).doubleValue(), 0.0);
        assertEquals(2.0, series.getValue(day2).doubleValue(), 0.0);
        assertEquals(3.0, series.getValue(day3).doubleValue(), 0.0);
    }

    // delete methods
    @Test
    public void testDeletePeriod() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.delete(day1);
        assertEquals(1, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
    }

    @Test
    public void testDeletePeriodNotExist() {
        series.add(day1, 1.0);
        series.delete(day2); // no-op
        assertEquals(1, series.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteStartEndInvalid() {
        series.delete(2, 1);
    }

    @Test
    public void testDeleteStartEnd() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(day3, series.getTimePeriod(0));
    }

    @Test
    public void testDeleteStartEndWithNotifyFalse() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.delete(0, 0, false); // remove first item silently
        assertEquals(1, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
        assertNull(series.getTimePeriodClass()); // after deletion, still one item, class not null
        assertEquals(Day.class, series.getTimePeriodClass());
    }

    @Test
    public void testDeleteAllItemsViaEndIndex() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.delete(0, 1, true);
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
    }

    // removeAgedItems
    @Test
    public void testRemoveAgedItemsBooleanNoRemoval() {
        series.setMaximumItemAge(1000);
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.removeAgedItems(true);
        assertEquals(2, series.getItemCount());
    }

    @Test
    public void testRemoveAgedItemsBooleanRemovesOld() {
        series.setMaximumItemAge(1); // age difference >1 triggers removal
        series.add(day1, 1.0); // add calls removeAgedItems(false), but age between day1 and itself is 0, so none removed
        series.add(day3, 3.0); // now difference between day3 and day1 is 2 >1, so after add day1 removed
        assertEquals(1, series.getItemCount());
        assertEquals(day3, series.getTimePeriod(0));
    }

    @Test
    public void testRemoveAgedItemsLongEmptySeries() {
        series.removeAgedItems(System.currentTimeMillis(), true);
    }

    @Test
    public void testRemoveAgedItemsLongRemoves() {
        series.setMaximumItemAge(0);
        series.add(day1, 1.0);
        series.add(day2, 2.0); // day2 remains, day1 removed inside add
        assertEquals(1, series.getItemCount());
        // now force removal with a future time
        long latest = day4.getMiddleMillisecond();
        series.removeAgedItems(latest, true);
        // day2 serial index difference to day4 >0, so removed
        assertEquals(0, series.getItemCount());
    }

    // clear
    @Test
    public void testClear() {
        series.add(day1, 1.0);
        series.clear();
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    // clone
    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        TimeSeries clone = (TimeSeries) series.clone();
        assertNotSame(series, clone);
        assertEquals(series.getItemCount(), clone.getItemCount());
        assertEquals(series.getDomainDescription(), clone.getDomainDescription());
        assertNotSame(series.getItems(), clone.getItems());
        assertEquals(series.getValue(0), clone.getValue(0));
    }

    // createCopy(int,int)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyIntInvalidStart() throws CloneNotSupportedException {
        series.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyIntEndLessThanStart() throws CloneNotSupportedException {
        series.createCopy(1, 0);
    }

    @Test
    public void testCreateCopyIntValid() throws CloneNotSupportedException {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        TimeSeries copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(day1, copy.getTimePeriod(0));
        assertEquals(day2, copy.getTimePeriod(1));
    }

    @Test
    public void testCreateCopyIntEmptySeries() throws CloneNotSupportedException {
        TimeSeries copy = series.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
    }

    // createCopy(RegularTimePeriod,RegularTimePeriod)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodNullStart() throws CloneNotSupportedException {
        series.createCopy(null, day1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodNullEnd() throws CloneNotSupportedException {
        series.createCopy(day1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodEndBeforeStart() throws CloneNotSupportedException {
        series.createCopy(day2, day1);
    }

    @Test
    public void testCreateCopyPeriodEmptyRange() throws CloneNotSupportedException {
        series.add(day2, 2.0);
        TimeSeries copy = series.createCopy(day1, day1);
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopyPeriodExact() throws CloneNotSupportedException {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        series.add(day3, 3.0);
        TimeSeries copy = series.createCopy(day1, day2);
        assertEquals(2, copy.getItemCount());
        assertEquals(day1, copy.getTimePeriod(0));
        assertEquals(day2, copy.getTimePeriod(1));
    }

    @Test
    public void testCreateCopyPeriodOutOfBounds() throws CloneNotSupportedException {
        series.add(day1, 1.0);
        series.add(day3, 3.0);
        TimeSeries copy = series.createCopy(day2, day4);
        assertEquals(0, copy.getItemCount());
    }

    // equals and hashCode
    @Test
    public void testEquals() {
        assertTrue(series.equals(series));
        assertFalse(series.equals(null));
        assertFalse(series.equals("String"));

        TimeSeries s1 = new TimeSeries("S1");
        s1.add(day1, 1.0);
        TimeSeries s2 = new TimeSeries("S1");
        s2.add(day1, 1.0);
        assertTrue(s1.equals(s2));

        s2.setDomainDescription("Other");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentAttributes() {
        TimeSeries s1 = new TimeSeries("S1");
        s1.add(day1, 1.0);
        TimeSeries s2 = new TimeSeries("S1");
        s2.add(day1, 1.0);
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testHashCodeChangesWithData() {
        series.add(day1, 1.0);
        int hc1 = series.hashCode();
        series.add(day2, 2.0);
        assertNotEquals(hc1, series.hashCode());
    }

    // getIndex
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullPeriod() {
        series.getIndex(null);
    }

    @Test
    public void testGetIndexValid() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        assertEquals(0, series.getIndex(day1));
        assertEquals(1, series.getIndex(day2));
        assertTrue(series.getIndex(day3) < 0);
    }

    // getRawDataItem
    @Test
    public void testGetRawDataItemByIndex() {
        series.add(day1, 1.0);
        TimeSeriesDataItem raw = series.getRawDataItem(0);
        assertNotNull(raw);
        assertEquals(1.0, raw.getValue().doubleValue(), 0.0);
    }

    @Test
    public void testGetRawDataItemByPeriod() {
        series.add(day1, 1.0);
        assertNotNull(series.getRawDataItem(day1));
        assertNull(series.getRawDataItem(day2));
    }

    // getTimePeriod
    @Test
    public void testGetTimePeriod() {
        series.add(day1, 1.0);
        assertEquals(day1, series.getTimePeriod(0));
    }

    // getNextTimePeriod
    @Test
    public void testGetNextTimePeriod() {
        series.add(day1, 1.0);
        assertEquals(day2, series.getNextTimePeriod());
    }

    // getTimePeriods
    @Test
    public void testGetTimePeriods() {
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        Collection periods = series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(day1));
    }

    // getTimePeriodsUniqueToOtherSeries
    @Test
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries other = new TimeSeries("Other");
        other.add(day1, 1.0);
        other.add(day3, 3.0);
        series.add(day1, 1.0);
        series.add(day2, 2.0);
        Collection unique = series.getTimePeriodsUniqueToOtherSeries(other);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(day3));
    }

    // getItems immutability
    @Test(expected = UnsupportedOperationException.class)
    public void testGetItemsListIsUnmodifiable() {
        series.add(day1, 1.0);
        series.getItems().clear();
    }

    // getDataItem returns clone
    @Test
    public void testGetDataItemReturnsClone() {
        series.add(day1, 5.0);
        TimeSeriesDataItem item1 = series.getDataItem(0);
        TimeSeriesDataItem item2 = series.getDataItem(0);
        assertNotSame(item1, item2);
        item1.setValue(10.0);
        assertEquals(5.0, series.getValue(0).doubleValue(), 0.0);
    }

    // getValue via period
    @Test
    public void testGetValueByPeriod() {
        series.add(day1, 7.0);
        assertEquals(7.0, series.getValue(day1).doubleValue(), 0.0);
        assertNull(series.getValue(day2));
    }

    // minY/maxY edge cases with null and NaN
    @Test
    public void testMinYMaxYWithNullValue() {
        series.add(day1, (Number) null);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        series.add(day2, 2.0);
        assertEquals(2.0, series.getMinY(), 0.0);
        assertEquals(2.0, series.getMaxY(), 0.0);
    }

    @Test
    public void testMinYMaxYWithNaNValue() {
        series.add(day1, Double.NaN);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        series.add(day2, 1.0);
        assertEquals(1.0, series.getMinY(), 0.0);
        assertEquals(1.0, series.getMaxY(), 0.0);
    }

    @Test
    public void testMinYMaxYAfterRemovingNonExtreme() {
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        series.add(day3, 30.0);
        series.delete(day2); // remove middle
        assertEquals(10.0, series.getMinY(), 0.0);
        assertEquals(30.0, series.getMaxY(), 0.0);
    }
}
