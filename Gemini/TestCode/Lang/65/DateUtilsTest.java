package org.apache.commons.lang.time;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DateUtilsTest {

    @Test
    public void testConstructor() {
        DateUtils utils = new DateUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testIsSameDayDate() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 0, 0);
        cal2.set(2004, Calendar.JULY, 4, 16, 30, 0);
        Assert.assertTrue(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));

        cal2.set(2004, Calendar.JULY, 5, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayDateNullFirst() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayDateNullSecond() {
        DateUtils.isSameDay(new Date(), (Date) null);
    }

    @Test
    public void testIsSameDayCalendar() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 0, 0);
        cal2.set(2004, Calendar.JULY, 4, 16, 30, 0);
        Assert.assertTrue(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2004, Calendar.JULY, 5, 16, 30, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2005, Calendar.JULY, 4, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal1.set(Calendar.ERA, GregorianCalendar.BC);
        cal2.set(Calendar.ERA, GregorianCalendar.AD);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalendarNull() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalendarNullSecond() {
        DateUtils.isSameDay(Calendar.getInstance(), (Calendar) null);
    }

    @Test
    public void testIsSameInstantDate() {
        Date d1 = new Date(1000L);
        Date d2 = new Date(1000L);
        Date d3 = new Date(2000L);
        Assert.assertTrue(DateUtils.isSameInstant(d1, d2));
        Assert.assertFalse(DateUtils.isSameInstant(d1, d3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantDateNull() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantDateNullSecond() {
        DateUtils.isSameInstant(new Date(), (Date) null);
    }

    @Test
    public void testIsSameInstantCalendar() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(1000L);
        cal2.setTimeInMillis(1000L);
        Assert.assertTrue(DateUtils.isSameInstant(cal1, cal2));
        cal2.setTimeInMillis(2000L);
        Assert.assertFalse(DateUtils.isSameInstant(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantCalendarNull() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantCalendarNullSecond() {
        DateUtils.isSameInstant(Calendar.getInstance(), (Calendar) null);
    }

    @Test
    public void testIsSameLocalTime() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 30, 40);
        cal1.set(Calendar.MILLISECOND, 50);
        cal2.set(2004, Calendar.JULY, 4, 12, 30, 40);
        cal2.set(Calendar.MILLISECOND, 50);
        Assert.assertTrue(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MILLISECOND, 51);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MILLISECOND, 50);
        cal2.set(Calendar.SECOND, 41);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.SECOND, 40);
        cal2.set(Calendar.MINUTE, 31);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MINUTE, 30);
        cal2.set(Calendar.HOUR, cal2.get(Calendar.HOUR) == 0 ? 1 : 0);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.HOUR, cal1.get(Calendar.HOUR));
        cal2.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR) + 1);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR));
        cal2.set(Calendar.YEAR, 2005);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.YEAR, 2004);
        cal2.set(Calendar.ERA, GregorianCalendar.BC);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        Calendar customCal = new GregorianCalendar() {};
        customCal.setTime(cal1.getTime());
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, customCal));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTimeNull() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTimeNullSecond() {
        DateUtils.isSameLocalTime(Calendar.getInstance(), null);
    }

    @Test
    public void testParseDate() throws Exception {
        String[] patterns = new String[]{"yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss"};
        Date d1 = DateUtils.parseDate("2004-07-04", patterns);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d1);
        Assert.assertEquals(2004, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        Assert.assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));

        Date d2 = DateUtils.parseDate("2004/07/04 12:30:00", patterns);
        cal.setTime(d2);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(expected = ParseException.class)
    public void testParseDateFail() throws Exception {
        DateUtils.parseDate("2004.07.04", new String[]{"yyyy-MM-dd", "yyyy/MM/dd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullStr() throws Exception {
        DateUtils.parseDate(null, new String[]{"yyyy-MM-dd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullPatterns() throws Exception {
        DateUtils.parseDate("2004-07-04", null);
    }

    @Test
    public void testAddMethods() {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();

        Date r = DateUtils.addYears(base, 1);
        cal.setTime(r);
        Assert.assertEquals(2001, cal.get(Calendar.YEAR));

        r = DateUtils.addMonths(base, 2);
        cal.setTime(r);
        Assert.assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));

        r = DateUtils.addWeeks(base, 1);
        cal.setTime(r);
        Assert.assertEquals(8, cal.get(Calendar.DAY_OF_MONTH));

        r = DateUtils.addDays(base, 5);
        cal.setTime(r);
        Assert.assertEquals(6, cal.get(Calendar.DAY_OF_MONTH));

        r = DateUtils.addHours(base, 3);
        cal.setTime(r);
        Assert.assertEquals(3, cal.get(Calendar.HOUR_OF_DAY));

        r = DateUtils.addMinutes(base, 15);
        cal.setTime(r);
        Assert.assertEquals(15, cal.get(Calendar.MINUTE));

        r = DateUtils.addSeconds(base, 30);
        cal.setTime(r);
        Assert.assertEquals(30, cal.get(Calendar.SECOND));

        r = DateUtils.addMilliseconds(base, 500);
        cal.setTime(r);
        Assert.assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDate() {
        DateUtils.add(null, Calendar.YEAR, 1);
    }

    @Test
    public void testRoundDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 15, 8, 30, 30);
        cal.set(Calendar.MILLISECOND, 600);
        Date d = cal.getTime();

        Date r = DateUtils.round(d, Calendar.SECOND);
        cal.setTime(r);
        Assert.assertEquals(31, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        r = DateUtils.round(d, Calendar.MINUTE);
        cal.setTime(r);
        Assert.assertEquals(31, cal.get(Calendar.MINUTE));

        r = DateUtils.round(d, Calendar.HOUR);
        cal.setTime(r);
        Assert.assertEquals(9, cal.get(Calendar.HOUR_OF_DAY));

        r = DateUtils.round(d, Calendar.DATE);
        cal.setTime(r);
        Assert.assertEquals(15, cal.get(Calendar.DATE));

        r = DateUtils.round(d, Calendar.MONTH);
        cal.setTime(r);
        Assert.assertEquals(Calendar.FEBRUARY, cal.get(Calendar.MONTH));

        r = DateUtils.round(d, Calendar.YEAR);
        cal.setTime(r);
        Assert.assertEquals(2002, cal.get(Calendar.YEAR));
    }

    @Test
    public void testRoundCalendarAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 20, 15, 45, 0);
        Calendar rCal = DateUtils.round(cal, Calendar.DATE);
        Assert.assertEquals(21, rCal.get(Calendar.DATE));

        Date rDate = DateUtils.round((Object) cal, Calendar.DATE);
        cal.setTime(rDate);
        Assert.assertEquals(21, cal.get(Calendar.DATE));

        rDate = DateUtils.round((Object) cal.getTime(), Calendar.MONTH);
        cal.setTime(rDate);
        Assert.assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNullDate() {
        DateUtils.round((Date) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNullCalendar() {
        DateUtils.round((Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNullObject() {
        DateUtils.round((Object) null, Calendar.DATE);
    }

    @Test(expected = ClassCastException.class)
    public void testRoundInvalidObject() {
        DateUtils.round("invalid", Calendar.DATE);
    }

    @Test
    public void testRoundSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 1, 0, 0, 0);
        DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        cal.set(2002, Calendar.FEBRUARY, 9, 0, 0, 0);
        DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, cal.get(Calendar.DATE));

        cal.set(2002, Calendar.FEBRUARY, 16, 0, 0, 0);
        DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, cal.get(Calendar.DATE));

        cal.set(2002, Calendar.FEBRUARY, 24, 0, 0, 0);
        DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, cal.get(Calendar.DATE));
        Assert.assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
    }

    @Test
    public void testRoundAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 1, 3, 0, 0);
        DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));

        cal.set(2002, Calendar.FEBRUARY, 1, 8, 0, 0);
        DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        cal.set(2002, Calendar.FEBRUARY, 1, 14, 0, 0);
        DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        cal.set(2002, Calendar.FEBRUARY, 1, 20, 0, 0);
        DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testTruncateDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 15, 8, 30, 30);
        cal.set(Calendar.MILLISECOND, 600);
        Date d = cal.getTime();

        Date r = DateUtils.truncate(d, Calendar.SECOND);
        cal.setTime(r);
        Assert.assertEquals(30, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        r = DateUtils.truncate(d, Calendar.MINUTE);
        cal.setTime(r);
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(0, cal.get(Calendar.SECOND));

        r = DateUtils.truncate(d, Calendar.HOUR);
        cal.setTime(r);
        Assert.assertEquals(8, cal.get(Calendar.HOUR_OF_DAY));

        r = DateUtils.truncate(d, Calendar.DATE);
        cal.setTime(r);
        Assert.assertEquals(15, cal.get(Calendar.DATE));

        r = DateUtils.truncate(d, Calendar.MONTH);
        cal.setTime(r);
        Assert.assertEquals(1, cal.get(Calendar.DATE));
        Assert.assertEquals(Calendar.FEBRUARY, cal.get(Calendar.MONTH));

        r = DateUtils.truncate(d, Calendar.YEAR);
        cal.setTime(r);
        Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assert.assertEquals(2002, cal.get(Calendar.YEAR));
    }

    @Test
    public void testTruncateCalendarAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 20, 15, 45, 0);
        Calendar tCal = DateUtils.truncate(cal, Calendar.DATE);
        Assert.assertEquals(20, tCal.get(Calendar.DATE));
        Assert.assertEquals(0, tCal.get(Calendar.HOUR_OF_DAY));

        Date tDate = DateUtils.truncate((Object) cal, Calendar.DATE);
        cal.setTime(tDate);
        Assert.assertEquals(20, cal.get(Calendar.DATE));

        tDate = DateUtils.truncate((Object) cal.getTime(), Calendar.MONTH);
        cal.setTime(tDate);
        Assert.assertEquals(1, cal.get(Calendar.DATE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateNullDate() {
        DateUtils.truncate((Date) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateNullCalendar() {
        DateUtils.truncate((Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateNullObject() {
        DateUtils.truncate((Object) null, Calendar.DATE);
    }

    @Test(expected = ClassCastException.class)
    public void testTruncateInvalidObject() {
        DateUtils.truncate("invalid", Calendar.DATE);
    }

    @Test
    public void testTruncateSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.FEBRUARY, 10, 0, 0, 0);
        DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        cal.set(2002, Calendar.FEBRUARY, 20, 0, 0, 0);
        DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, cal.get(Calendar.DATE));
    }

    @Test(expected = ArithmeticException.class)
    public void testModifyLargeYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.round(cal, Calendar.YEAR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModifyUnsupportedField() {
        Calendar cal = Calendar.getInstance();
        DateUtils.round(cal, -999);
    }

    @Test
    public void testIteratorMonthSunday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.JULY, 4);
        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_SUNDAY);
        Assert.assertTrue(it.hasNext());
        Calendar first = (Calendar) it.next();
        Assert.assertEquals(2002, first.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JUNE, first.get(Calendar.MONTH));
        Assert.assertEquals(30, first.get(Calendar.DATE));

        Calendar last = first;
        while (it.hasNext()) {
            last = (Calendar) it.next();
        }
        Assert.assertEquals(2002, last.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.AUGUST, last.get(Calendar.MONTH));
        Assert.assertEquals(3, last.get(Calendar.DATE));
    }

    @Test
    public void testIteratorMonthMonday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.JULY, 4);
        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_MONDAY);
        Calendar first = (Calendar) it.next();
        Assert.assertEquals(Calendar.JULY, first.get(Calendar.MONTH));
        Assert.assertEquals(1, first.get(Calendar.DATE));
        Assert.assertEquals(Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));

        Calendar last = first;
        while (it.hasNext()) {
            last = (Calendar) it.next();
        }
        Assert.assertEquals(Calendar.AUGUST, last.get(Calendar.MONTH));
        Assert.assertEquals(4, last.get(Calendar.DATE));
        Assert.assertEquals(Calendar.SUNDAY, last.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testIteratorWeekStyles() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.JULY, 4);

        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        Calendar first = (Calendar) it.next();
        Assert.assertEquals(Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_MONDAY);
        first = (Calendar) it.next();
        Assert.assertEquals(Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_RELATIVE);
        first = (Calendar) it.next();
        Assert.assertEquals(Calendar.THURSDAY, first.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_CENTER);
        first = (Calendar) it.next();
        Assert.assertEquals(Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testIteratorObjectAndDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, Calendar.JULY, 4);
        Iterator it = DateUtils.iterator(cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(it.next());

        it = DateUtils.iterator((Object) cal, DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(it.next());

        it = DateUtils.iterator((Object) cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(it.next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorNullDate() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorNullCalendar() {
        DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorNullObject() {
        DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = ClassCastException.class)
    public void testIteratorInvalidObject() {
        DateUtils.iterator("invalid", DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorInvalidRangeStyle() {
        Calendar cal = Calendar.getInstance();
        DateUtils.iterator(cal, 999);
    }

    @Test(expected = NoSuchElementException.class)
    public void testDateIteratorNoSuchElement() {
        Calendar cal = Calendar.getInstance();
        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDateIteratorRemove() {
        Calendar cal = Calendar.getInstance();
        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }
}
