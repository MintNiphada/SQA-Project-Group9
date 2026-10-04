package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for the {@link TimeSeries} class.
 */
public class TimeSeriesTest {

    // Constants for Day creation
    private static final int JAN = 1;
    private static final int FEB = 2;
    private static final int MAR = 3;
    private static final int APR = 4;
    private static final int MAY = 5;
    private static final int JUN = 6;
    private static final int JUL = 7;
    private static final int AUG = 8;
    private static final int SEP = 9;
    private static final int OCT = 10;
    private static final int NOV = 11;
    private static final int DEC = 12;
    private static final int YEAR = 2010;

    // Helper: create a Day
    private Day day(int d, int m, int y) {
        return new Day(d, m, y);
    }

    // Helper: create a TimeSeries with Day.class, but no data
    private TimeSeries createDaySeries(String name) {
        return new TimeSeries(name, Day.class);
    }

    // ---------- Constructors --------------------

    @Test
    public void testDefaultConstructor() {
        TimeSeries s = new TimeSeries("Test");
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(Day.class, s.getTimePeriodClass());
        assertEquals(0, s.getItemCount());
        assertEquals(Integer.MAX_VALUE, s.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, s.getMaximumItemAge());
    }

    @Test
    public void testConstructorWithClass() {
        TimeSeries s = new TimeSeries("Test", Year.class);
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(Year.class, s.getTimePeriodClass());
    }

    @Test
    public void testConstructorWithDescription() {
        TimeSeries s = new TimeSeries("Test", "Domain", "Range", Day.class);
        assertEquals("Domain", s.getDomainDescription());
        assertEquals("Range", s.getRangeDescription());
        assertEquals(Day.class, s.getTimePeriodClass());
    }

    // ---------- getDomainDescription/setDomainDescription ----------

    @Test
    public void testDomainDescription() {
        TimeSeries s = createDaySeries("A");
        assertNull(s.getDomainDescription()); // because we didn't set explicit? Actually default is "Time", but if we use constructor with default? The default constructor sets "Time". But we used createDaySeries which passes Day.class, that uses default domain "Time". So it's "Time". Let's check: TimeSeries(name, Day.class) -> calls this(name, DEFAULT_DOMAIN_DESCRIPTION, DEFAULT_RANGE_DESCRIPTION, timePeriodClass). So default domain is "Time". So getDomainDescription() should return "Time", not null. So adjust assert.
        assertEquals("Time", s.getDomainDescription());
        s.setDomainDescription("New Domain");
        assertEquals("New Domain", s.getDomainDescription());
    }

    // ---------- getRangeDescription/setRangeDescription ----------

    @Test
    public void testRangeDescription() {
        TimeSeries s = createDaySeries("B");
        assertEquals("Value", s.getRangeDescription());
        s.setRangeDescription("New Range");
        assertEquals("New Range", s.getRangeDescription());
    }

    // ---------- getItemCount, getItems ----------

    @Test
    public void testGetItemCount() {
        TimeSeries s = createDaySeries("C");
        assertEquals(0, s.getItemCount());
        s.add(new Day(1, JAN, YEAR), 1.0);
        assertEquals(1, s.getItemCount());
    }

    @Test
    public void testGetItemsUnmodifiable() {
        TimeSeries s = createDaySeries("D");
        s.add(new Day(1, JAN, YEAR), 1.0);
        List items = s.getItems();
        assertEquals(1, items.size());
        try {
            items.add(new TimeSeriesDataItem(new Day(2, JAN, YEAR), 2.0);
            fail("Should throw UnsupoortedOperationException");
        } catch (UnsupoortedOperationException expected) {
        }
    }

    // ---------- getMaximumItemCount / setMaximumItemCount ----------

    @Test
    public void testGetMaximumItemCountDefault() {
        TimeSeries s = createDaySeries("E");
        assertEquals(Integer.MAX_VALUE, s.getMaximumItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        TimeSeries s = createDaySeries("F");
        s.setMaximumItemCount(-5);
    }

    @Test
    public void testSetMaximumItemCountZero() {
        TimeSeries s = createDaySeries("G");
        s.setMaximumItemCount(0); // allowed, but next add will be removed? Actually set max to 0, no existing items.
        assertEquals(0, s.getMaximumItemCount());
        // add should cause removal
        s.add(new Day(1, JAN, YEAR), 1.0);
        // Since max count is 0, after adding, count becomes 1, then condition count > max triggers remove(0), leaving 0.
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testSetMaximumItemCountTrims() {
        TimeSeries s = createDaySeries("H");
        s.setMaximumItemCount(3);
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        assertEquals(3, s.getItemCount());
        s.setMaximumItemCount(2); // should delete first item
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getDataItem(0).getValue().doubleValue(), 0);
        assertEquals(3.0, s.getDataItem(1).getValue().doubleValue(), 0);
    }

    // ---------- getMaximumItemAge / setMaximumItemAge ----------

    @Test
    public void testGetMaximumItemAgeDefault() {
        TimeSeries s = new TimeSeries("I");
        assertEquals(Long.MAX_VALUE, s.getMaximumItemAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        TimeSeries s = createDaySeries("J");
        s.setMaximumItemAge(-1L);
    }

    @Test
    public void testSetMaximumItemAgeRemovesOldItems() {
        TimeSeries s = createDaySeries("K");
        s.setMaximumItemAge(2); // max age 2 time periods
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(3, JAN, YEAR), 3.0); // day 3 - day 1 = 2 serial indices, which is exactly max age? Actually serial index: Day #1 relative to epoch, so we need to know actual serial. But the condition uses >, so equal is not removed. So after adding day 3, max age 2, (3 serial - 1 serial)=2 not > 2, so item retained.
        s.add(new Day(5, JAN, YEAR), 5.0); // now latest serial index - first serial > 2 likely, then first item removed. We'll assert it's removed.
        // after adding day5, the period (1) should be removed because (5 serial - 1 serial)=4 > 2.
        assertEquals(2, s.getItemCount());
        assertEquals(3.0, s.getDataItem(0).getValue().doubleValue(), 0);
        assertEquals(5.0, s.getDataItem(1).getValue().doubleValue(), 0);
    }

    // ---------- getTimePeriodClass ----------

    @Test
    public void testGetTimePeriodClass() {
        TimeSeries s = new TimeSeries("L", Month.class);
        assertEquals(Month.class, s.getTimePeriodClass());
    }

    // ---------- getDataItem(int) ----------

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemByIndexEmpty() {
        TimeSeries s = createDaySeries("M");
        s.getDataItem(0);
    }

    @Test
    public void testGetDataItemByIndex() {
        TimeSeries s = createDaySeries("N");
        s.add(new Day(1, JAN, YEAR), 10.0);
        TimeSeriesDataItem item = s.getDataItem(0);
        assertEquals(new Day(1, JAN, YEAR), item.getPeriod());
        assertEquals(10.0, item.getValue().doubleValue(), 0);
    }

    // ---------- getDataItem(RegularTimePeriod) ----------

    @Test
    public void testGetDataItemByPeriod() {
        TimeSeries s = createDaySeries("O");
        s.add(new Day(2, JAN, YEAR), 20.0);
        TimeSeriesDataItem found = s.getDataItem(new Day(2, JAN, YEAR));
        assertNotNull(found);
        assertEquals(20.0, found.getValue().doubleValue(), 0);
        // not existing
        assertNull(s.getDataItem(new Day(3, JAN, YEAR)));
    }

    // ---------- getTimePeriod(int) ----------

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTimePeriodInvalidIndex() {
        TimeSeries s = createDaySeries("P");
        s.getTimePeriod(0);
    }

    @Test
    public void testGetTimePeriodValid() {
        TimeSeries s = createDaySeries("Q");
        s.add(new Day(5, JAN, YEAR), 50.0);
        assertEquals(new Day(5, JAN, YEAR), s.getTimePeriod(0));
    }

    // ---------- getNextTimePeriod ----------

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNextTimePeriodEmpty() {
        TimeSeries s = createDaySeries("R");
        s.getNextTimePeriod(); // tries to get item at index -1
    }

    @Test
    public void testGetNextTimePeriodNonEmpty() {
        TimeSeries s = createDaySeries("S");
        s.add(new Day(1, JAN, YEAR), 1.0);
        RegularTimePeriod next = s.getNextTimePeriod();
        assertEquals(new Day(2, JAN, YEAR), next);
    }

    // ---------- getTimePeriods ----------

    @Test
    public void testGetTimePeriods() {
        TimeSeries s = createDaySeries("T");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        Collection periods = s.getTimePeriods();
        assertEquals(2, periods.size());
        Iterator it = periods.iterator();
        assertTrue(it.next().compareTo(new Day(1, JAN, YEAR)) == 0);
        assertTrue(it.next().compareTo(new Day(3, JAN, YEAR)) == 0);
    }

    // ---------- getTimePeriodsUnqueToOtherSeries ----------

    @Test
    public void testGetTimePeriodsUnqueToOtherSeries() {
        TimeSeries s1 = createDaySeries("U");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        s1.add(new Day(2, JAN, YEAR), 2.0);
        s1.add(new Day(4, JAN, YEAR), 4.0);

        TimeSeries s2 = createDaySeries("V");
        s2.add(new Day(2, JAN, YEAR), 20.0);
        s2.add(new Day(3, JAN, YEAR), 30.0);

        Collection unique = s1.getTimePeriodsUnqueToOtherSeries(s2);
        assertEquals(3, unique.size()); // day1, day4 are not in s2, but day3 is in s2 and not in s1? method says: "unique to the specified series". It iterates over s2, for each period checks getIndex(s1), if index<0 add to result. So it collects periods that are in s2 but not in s1. So for our data: s2 has day2 (in s1), day3 (not in s1). So result should contain day3. Wait, day1, day4 are not in s2. So we need to reconsider. Let's verify: It loops i on series (the parameter) itemCount, gets period of series, checks if getIndex(period) < 0. So it collects periods from the passed series that are not in the current series. So for s1.getUniqueTo(s2): we go through s2 periods: day2 is in s1 -> not added, day3 is not in s1 -> added. So result size 1, containing day3. My previous expectation was wrong. Fix test:
        assertEquals(1, unique.size());
        assertTrue(unique.contains(new Day(3, JAN, YEAR)));
    }

    // ---------- getIndex(RegularTimePeriod) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullPeriod() {
        TimeSeries s = createDaySeries("W");
        s.getIndex(null);
    }

    @Test
    public void testGetIndex() {
        TimeSeries s = createDaySeries("X");
        s.add(new Day(10, JAN, YEAR), 10.0);
        s.add(new Day(20, JAN, YEAR), 20.0);
        assertEquals(0, s.getIndex(new Day(10, JAN, YEAR)));
        assertEquals(1, s.getIndex(new Day(20, JAN, YEAR)));
        // non-existing period should return negative (insertion point)
        int idx = s.getIndex(new Day(15, JAN, YEAR));
        assertTrue(idx < 0);
        // insertion point: -(-idx-1) = 1? So idx = -2.
        assertEquals(-2, idx);
    }

    // ---------- getValue(int), getValue(RegularTimePeriod) ----------

    @Test
    public void testGetValueByIndex() {
        TimeSeries s = createDaySeries("Y");
        s.add(new Day(1, JAN, YEAR), 100.0);
        assertEquals(100.0, s.getValue(0).doubleValue(), 0);
        // test with null value
        s.add(new Day(2, JAN, YEAR), (Number) null);
        assertNull(s.getValue(1));
    }

    @Test
    public void testGetValueByPeriod() {
        TimeSeries s = createDaySeries("Z");
        s.add(new Day(1, JAN, YEAR), 200.0);
        assertEquals(200.0, s.getValue(new Day(1, JAN, YEAR)).doubleValue(), 0);
        assertNull(s.getValue(new Day(2, JAN, YEAR)));
    }

    // ---------- add() methods ----------

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        TimeSeries s = createDaySeries("AA");
        s.add((TimeSeriesDataItem) null);
    }

    @Test
    public void testAddNullItemNotify() {
        TimeSeries s = createDaySeries("AA1");
        try {
            s.add((TimeSeriesDataItem) null, false);
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException ex) {
            assertEquals("Null 'item' argument.", ex.getMessage());
        }
    }

    @Test
    public void testAddWrongPeriodClass() {
        TimeSeries s = new TimeSeries("AB", Day.class);
        try {
            s.add(new TimeSeriesDataItem(new Year(2010), 10.0);
            fail("Should throw SeriesException");
        } catch (SeriesException ex) {
            assertTrue(ex.getMessage().contains("You are trying to add data where the time period class"));
            assertTrue(ex.getMessage().contains("Year"));
            assertTrue(ex.getMessage().contains("Day"));
        }
    }

    @Test
    public void testAddToEmpty() {
        TimeSeries s = createDaySeries("AC");
        s.add(new Day(5, JAN, YEAR), 5.0);
        assertEquals(1, s.getItemCount());
        assertEquals(new Day(5, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(5.0, s.getDataItem(0).getValue().doubleValue(), 0);
    }

    @Test
    public void testAddAtEnd() {
        TimeSeries s = createDaySeries("AD");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        assertEquals(2, s.getItemCount());
        assertEquals(new Day(1, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(new Day(3, JAN, YEAR), s.getTimePeriod(1));
    }

    @Test
    public void testAddInMiddle() {
        TimeSeries s = createDaySeries("AE");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(5, JAN, YEAR), 5.0);
        s.add(new Day(3, JAN, YEAR), 3.0); // inserted between 1 and 5
        assertEquals(3, s.getItemCount());
        assertEquals(new Day(1, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(new Day(3, JAN, YEAR), s.getTimePeriod(1));
        assertEquals(new Day(5, JAN, YEAR), s.getTimePeriod(2));
    }

    @Test
    public void testAddDuplicatePeriod() {
        TimeSeries s = createDaySeries("AF");
        s.add(new Day(1, JAN, YEAR), 1.0);
        try {
            s.add(new Day(1, JAN, YEAR), 2.0);
            fail("Should throw SeriesException");
        } catch (SeriesException ex) {
            assertTrue(ex.getMessage().contains("You are attempting to add an observation for the time period"));
        }
    }

    @Test
    public void testAddTriggersMaximumItemCount() {
        TimeSeries s = createDaySeries("AG");
        s.setMaximumItemCount(2);
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        // adding third should remove first
        s.add(new Day(3, JAN, YEAR), 3.0);
        assertEquals(2, s.getItemCount());
        assertEquals(new Day(2, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(3.0, s.getDataItem(1).getValue().doubleValue(), 0);
    }

    @Test
    public void testAddRemovesAgedItems() {
        TimeSeries s = createDaySeries("AH");
        s.setMaximumItemAge(1); // max age 1 period
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0); // serial diff 1, not greater than 1, so kept
        s.add(new Day(4, JAN, YEAR), 4.0); // serial diff 3 >1, first removed
        assertEquals(2, s.getItemCount());
        assertEquals(new Day(2, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(new Day(4, JAN, YEAR), s.getTimePeriod(1));
    }

    // Test add with notify flag false, i.e., no event. We can't directly test, but we'll verify items added.
    @Test
    public void testAddWithNotifyFalse() {
        TimeSeries s = createDaySeries("AI");
        s.add(new Day(1, JAN, YEAR), 1.0, false);
        assertEquals(1, s.getItemCount());
    }

    // ---------- add(RegularTimePeriod, double/Number) ----------

    @Test
    public void testAddPeriodDouble() {
        TimeSeries s = createDaySeries("AJ");
        s.add(new Day(1, JAN, YEAR), 10.0);
        assertEquals(10.0, s.getValue(0).doubleValue(), 0);
    }

    @Test
    public void testAddPeriodNumber() {
        TimeSeries s = createDaySeries("AK");
        s.add(new Day(2, JAN, YEAR), new Double(20.0));
        assertEquals(20.0, s.getValue(0).doubleValue(), 0);
    }

    @Test
    public void testAddPeriodDoubleNotify() {
        TimeSeries s = createDaySeries("AL");
        s.add(new Day(3, JAN, YEAR), 30.0, false);
        assertEquals(30.0, s.getValue(0).doubleValue(), 0);
    }

    @Test
    public void testAddPeriodNumberNotify() {
        TimeSeries s = createDaySeries("AM");
        s.add(new Day(4, JAN, YEAR), (Number) null, false);
        assertEquals(1, s.getItemCount());
        assertNull(s.getValue(0));
    }

    // ---------- update() ----------

    @Test
    public void testUpdateByPeriodExisting() {
        TimeSeries s = createDaySeries("AN");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.update(new Day(1, JAN, YEAR), 100.0);
        assertEquals(100.0, s.getValue(0).doubleValue(), 0);
    }

    @Test
    public void testUpdateByPeriodNonExisting() {
        TimeSeries s = createDaySeries("AO");
        try {
            s.update(new Day(2, JAN, YEAR), 5.0);
            fail("Should throw SeriesException");
        } catch (SeriesException ex) {
            assertEquals("TimeSeries.update(TimePeriod, Number):  period does not exist.", ex.getMessage());
        }
    }

    @Test
    public void testUpdateByIndex() {
        TimeSeries s = createDaySeries("AP");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.update(0, new Double(10.0));
        assertEquals(10.0, s.getDataItem(0).getValue().doubleValue(), 0);
        s.update(0, null);
        assertNull(s.getDataItem(0).getValue());
    }

    // ---------- addAndOrUpdate() ----------

    @Test
    public void testAddAndOrUpdate() {
        TimeSeries s1 = createDaySeries("AQ");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        s1.add(new Day(3, JAN, YEAR), 3.0);

        TimeSeries s2 = createDaySeries("AR");
        s2.add(new Day(3, JAN, YEAR), 30.0);
        s2.add(new Day(4, JAN, YEAR), 40.0);

        TimeSeries overwritten = s1.addAndOrUpdate(s2);
        // s1 now should have day1=1.0, day3=30.0, day4=40.0
        assertEquals(3, s1.getItemCount());
        assertEquals(1.0, s1.getValue(0).doubleValue(), 0);
        assertEquals(30.0, s1.getValue(1).doubleValue(), 0);
        assertEquals(40.0, s1.getValue(2).doubleValue(), 0);
        // overwritten should contain old day3=3.0
        assertEquals(1, overwritten.getItemCount());
        assertEquals(3.0, overwritten.getValue(0).doubleValue(), 0);
    }

    // ---------- addOrUpdate(RegularTimePeriod, double/Number) ----------

    @Test
    public void testAddOrUpdateNewPeriod() {
        TimeSeries s = createDaySeries("AS");
        TimeSeriesDataItem overwritten = s.addOrUpdate(new Day(1, JAN, YEAR), 100.0);
        assertNull(overwritten);
        assertEquals(1, s.getItemCount());
        assertEquals(100.0, s.getValue(0).doubleValue(), 0);
    }

    @Test
    public void testAddOrUpdateExistingPeriod() {
        TimeSeries s = createDaySeries("AT");
        s.add(new Day(1, JAN, YEAR), 10.0);
        TimeSeriesDataItem overwritten = s.addOrUpdate(new Day(1, JAN, YEAR), 20.0);
        assertNotNull(overwritten);
        assertEquals(10.0, overwritten.getValue().doubleValue(), 0);
        assertEquals(20.0, s.getValue(0).doubleValue(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullPeriod() {
        TimeSeries s = createDaySeries("AU");
        s.addOrUpdate(null, 5.0);
    }

    // addOrUpdate with max item count enforcement
    @Test
    public void testAddOrUpdateEnforcsMaxCount() {
        TimeSeries s = createDaySeries("AV");
        s.setMaximumItemCount(2);
        s.addOrUpdate(new Day(1, JAN, YEAR), 1.0);
        s.addOrUpdate(new Day(2, JAN, YEAR), 2.0);
        s.addOrUpdate(new Day(3, JAN, YEAR), 3.0); // should remove first
        assertEquals(2, s.getItemCount());
        assertEquals(new Day(2, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(new Day(3, JAN, YEAR), s.getTimePeriod(1));
    }

    // ---------- removeAgedItems(boolean) ----------

    @Test
    public void testRemoveAgedItemsNoRemoveIfNotEnoughAge() {
        TimeSeries s = createDaySeries("AW");
        s.setMaximumItemAge(5);
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.removeAgedItems(true); // should not remove anything
        assertEquals(2, s.getItemCount());
    }

    @Test
    public void testRemoveAgedItemsRemovesWithNotify() {
        TimeSeries s = createDaySeries("AX");
        s.setMaximumItemAge(1);
        s.add(new Day(5, JAN, YEAR), 5.0);
        s.add(new Day(7, JAN, YEAR), 7.0); // diff 2 >1, so remove day5
        // after add, removal already happened? Actually add calls removeAgedItems(false) internally, so after add, the old item might already be removed. So we need to set up accordingly.
        // Let's set up with age not enforced yet, then call removeAgedItems manually.
        s = createDaySeries("AX");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(5, JAN, YEAR), 5.0);
        s.setMaximumItemAge(2); // now max age 2
        // current diff: 5-1=4 >2, so removeAgedItems should remove day1.
        s.removeAgedItems(true);
        assertEquals(1, s.getItemCount());
        assertEquals(new Day(5, JAN, YEAR), s.getTimePeriod(0));
    }

    @Test
    public void testRemoveAgedItemsRemovesWithoutNotify() {
        TimeSeries s = createDaySeries("AY");
        s.add(new Day(10, JAN, YEAR), 10.0);
        s.add(new Day(12, JAN, YEAR), 12.0);
        s.setMaximumItemAge(1);
        s.removeAgedItems(false);
        assertEquals(1, s.getItemCount());
    }

    // ---------- removeAgedItems(long latest, boolean) ----------
    // This method uses reflection to create RegularTimePeriod from Date.
    // We will test with Day class.

    @Test
    public void testRemoveAgedItemsWithLatest() {
        TimeSeries s = createDaySeries("AZ");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(10, JAN, YEAR), 10.0);
        s.setMaximumItemAge(5); // max age 5 days
        // latest is day 15, so oldest serial index difference should be >5? day1 serial to day15 serial is about 14, so day1 will be removed.
        Date latest = new Day(15, JAN, YEAR).getMiddle().getTime(); // Day has getMiddle()? Actually Day extends RegularTimePeriod, has getMiddle() returns Date. So we can use that.
        s.removeAgedItems(latest.getTime(), true);
        assertEquals(1, s.getItemCount());
        assertEquals(new Day(10, JAN, YEAR), s.getTimePeriod(0));
    }

    // ---------- clear() ----------

    @Test
    public void testClear() {
        TimeSeries s = createDaySeries("BA");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.clear();
        assertEquals(0, s.getItemCount());
    }

    // ---------- delete(RegularTimePeriod) ----------

    @Test
    public void testDeleteByPeriodExisting() {
        TimeSeries s = createDaySeries("BB");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.delete(new Day(1, JAN, YEAR));
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testDeleteByPeriodNonExisting() {
        TimeSeries s = createDaySeries("BC");
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.delete(new Day(1, JAN, YEAR));
        assertEquals(1, s.getItemCount());
    }

    // ---------- delete(int start, int end) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteInvalidRange() {
        TimeSeries s = createDaySeries("BD");
        s.delete(5, 2);
    }

    @Test
    public void testDeleteRange() {
        TimeSeries s = createDaySeries("BE");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        s.delete(1, 1); // delete index 1 only
        assertEquals(2, s.getItemCount());
        assertEquals(new Day(1, JAN, YEAR), s.getTimePeriod(0));
        assertEquals(new Day(3, JAN, YEAR), s.getTimePeriod(1));
    }

    @Test
    public void testDeleteRangeMultiple() {
        TimeSeries s = createDaySeries("BF");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        s.delete(0, 1);
        assertEquals(1, s.getItemCount());
        assertEquals(new Day(3, JAN, YEAR), s.getTimePeriod(0));
    }

    // ---------- clone() ----------

    @Test
    public void testClone() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BG");
        s.add(new Day(1, JAN, YEAR), 1.0);
        TimeSeries clone = (TimeSeries) s.clone();
        assertTrue(s.equals(clone));
        assertEquals(s.getItemCount(), clone.getItemCount());
        // modify original, clone should not change
        s.add(new Day(2, JAN, YEAR), 2.0);
        assertFalse(s.equals(clone));
    }

    // ---------- createCopy(int start, int end) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyIntStartNegative() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BH");
        s.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyIntStartGreaterThanEnd() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BI");
        s.createCopy(2, 1);
    }

    @Test
    public void testCreateCopyInt() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BJ");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        s.add(new Day(5, JAN, YEAR), 5.0);
        TimeSeries copy = s.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Day(3, JAN, YEAR), copy.getTimePeriod(0));
        assertEquals(new Day(5, JAN, YEAR), copy.getTimePeriod(1));
    }

    // ---------- createCopy(RegularTimePeriod start, RegularTimePeriod end) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodNullStart() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BK");
        s.createCopy(null, new Day(1, JAN, YEAR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodNullEnd() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BL");
        s.createCopy(new Day(1, JAN, YEAR), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyPeriodStartAfterEnd() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BM");
        s.createCopy(new Day(5, JAN, YEAR), new Day(3, JAN, YEAR));
    }

    @Test
    public void testCreateCopyPeriodEmptyRangeStartAfterLast() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BN");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(2, JAN, YEAR), 2.0);
        TimeSeries copy = s.createCopy(new Day(5, JAN, YEAR), new Day(10, JAN, YEAR));
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopyPeriodEmptyRangeEndBeforeFirst() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BO");
        s.add(new Day(5, JAN, YEAR), 5.0);
        TimeSeries copy = s.createCopy(new Day(1, JAN, YEAR), new Day(3, JAN, YEAR));
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopyPeriodExactMatch() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BP");
        s.add(new Day(10, JAN, YEAR), 10.0);
        s.add(new Day(15, JAN, YEAR), 15.0);
        s.add(new Day(20, JAN, YEAR), 20.0);
        TimeSeries copy = s.createCopy(new Day(10, JAN, YEAR), new Day(20, JAN, YEAR));
        assertEquals(3, copy.getItemCount());
    }

    @Test
    public void testCreateCopyPeriodPartialOverlap() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BQ");
        s.add(new Day(10, JAN, YEAR), 10.0);
        s.add(new Day(15, JAN, YEAR), 15.0);
        s.add(new Day(20, JAN, YEAR), 20.0);
        TimeSeries copy = s.createCopy(new Day(12, JAN, YEAR), new Day(18, JAN, YEAR));
        // start index should be insertion point for day12: negative, converted to -(-... ) -> after day10? Let's compute: days: 10,15,20. Day12 insertion point is 1 (index 1) because day10 < day12 < day15. So startIndex = -(1)+1? Wait logic: if startIndex<0: startIndex = -(startIndex+1) = -( -2? Actually getIndex(day12) returns -2? Because binarySearch returns -insertionPoint -1. For list [10,15,20], day12 would be inserted at position 1 (between 10 and15). So return -2. Then startIndex = -(-2+1) = -(-1) = 1. That's correct, it gives the index of the first element >= start (which is 15, index 1). But wait, startIndex = 1. Then endIndex = getIndex(day18) -> -3 (insert after 15 before 20? Actually day18 is between 15 and 20, insertion point 2, so binary search returns -3. Then endIndex = -(-3+1) = 2, then endIndex = endIndex-1 = 1. So endIndex becomes 1. So copy from 1 to 1 -> only day15. So copy should contain day15.
        assertEquals(1, copy.getItemCount());
        assertEquals(new Day(15, JAN, YEAR), copy.getTimePeriod(0));
    }

    @Test
    public void testCreateCopyPeriodStartEndSameAndExist() throws CloneNotSupportedException {
        TimeSeries s = createDaySeries("BR");
        s.add(new Day(1, JAN, YEAR), 1.0);
        s.add(new Day(3, JAN, YEAR), 3.0);
        TimeSeries copy = s.createCopy(new Day(3, JAN, YEAR), new Day(3, JAN, YEAR));
        assertEquals(1, copy.getItemCount());
        assertEquals(new Day(3, JAN, YEAR), copy.getTimePeriod(0));
    }

    // ---------- equals() ----------

    @Test
    public void testEqualsSameObject() {
        TimeSeries s = createDaySeries("BS");
        assertTrue(s.equals(s));
    }

    @Test
    public void testEqualsNull() {
        TimeSeries s = createDaySeries("BT");
        assertFalse(s.equals(null));
    }

    @Test
    public void testEqualsOtherClass() {
        TimeSeries s = createDaySeries("BU");
        assertFalse(s.equals("String"));
    }

    @Test
    public void testEqualsDifferentDomain() {
        TimeSeries s1 = new TimeSeries("BV", "Dom1", "Ran1", Day.class);
        TimeSeries s2 = new TimeSeries("BV", "Dom2", "Ran1", Day.class);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentRange() {
        TimeSeries s1 = new TimeSeries("BW", "Dom1", "Ran1", Day.class);
        TimeSeries s2 = new TimeSeries("BW", "Dom1", "Ran2", Day.class);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentClassType() {
        // create subclass
        TimeSeries s1 = new TimeSeries("BX");
        TimeSeries s2 = new TimeSeries("BX") {}; // anonymous subclass
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentMaxItemAge() {
        TimeSeries s1 = createDaySeries("BY");
        s1.setMaximumItemAge(5);
        TimeSeries s2 = createDaySeries("BY");
        s2.setMaximumItemAge(10);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentMaxItemCount() {
        TimeSeries s1 = createDaySeries("BZ");
        s1.setMaximumItemCount(5);
        TimeSeries s2 = createDaySeries("BZ");
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentItemCount() {
        TimeSeries s1 = createDaySeries("CA");
        TimeSeries s2 = createDaySeries("CA");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentItems() {
        TimeSeries s1 = createDaySeries("CB");
        TimeSeries s2 = createDaySeries("CB");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        s2.add(new Day(1, JAN, YEAR), 2.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsEqualSeries() {
        TimeSeries s1 = createDaySeries("CC");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        s1.add(new Day(3, JAN, YEAR), 3.0);
        TimeSeries s2 = createDaySeries("CC");
        s2.add(new Day(1, JAN, YEAR), 1.0);
        s2.add(new Day(3, JAN, YEAR), 3.0);
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEqualsSuperClassInequality() {
        // If the parent Series part differs, it should return false.
        TimeSeries s1 = new TimeSeries("CD");
        TimeSeries s2 = new TimeSeries("CE"); // different key
        assertFalse(s1.equals(s2));
    }

    // ---------- hashCode() ----------
    // verify that equal objects have same hash code

    @Test
    public void testHashCodeConsistentWithEquals() {
        TimeSeries s1 = createDaySeries("CF");
        s1.add(new Day(1, JAN, YEAR), 1.0);
        s1.add(new Day(3, JAN, YEAR), 3.0);
        TimeSeries s2 = createDaySeries("CF");
        s2.add(new Day(1, JAN, YEAR), 1.0);
        s2.add(new Day(3, JAN, YEAR), 3.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // Test hashCode with different cases still consistency if equals true
    // also test that two different series may have different hash codes (not required but verify)

    // Edge cases for createCopy with null descriptions, etc. Already covered.

    // Test getIndex with null item (already)
    // Additional test for update(int) out of bounds? getDataItem throws, so test.
    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateByIndexOutOfBounds() {
        TimeSeries s = createDaySeries("CG");
        s.update(0, 5.0);
    }
}
