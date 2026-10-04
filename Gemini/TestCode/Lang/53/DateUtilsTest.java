package org.apache.commons.lang.time;

import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

public class DateUtilsTest {

    @Test
    public void testConstructor() {
        DateUtils utils = new DateUtils();
        Assert.assertNotNull(utils);
        Assert.assertNotNull(DateUtils.UTC_TIME_ZONE);
        Assert.assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        Assert.assertEquals(60000L, DateUtils.MILLIS_PER_MINUTE);
        Assert.assertEquals(3600000L, DateUtils.MILLIS_PER_HOUR);
        Assert.assertEquals(86400000L, DateUtils.MILLIS_PER_DAY);
        Assert.assertEquals(1000, DateUtils.MILLIS_IN_SECOND);
        Assert.assertEquals(60000, DateUtils.MILLIS_IN_MINUTE);
        Assert.assertEquals(3600000, DateUtils.MILLIS_IN_HOUR);
        Assert.assertEquals(86400000, DateUtils.MILLIS_IN_DAY);
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

        cal2.set(2005, Calendar.JULY, 4, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayDateNull1() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayDateNull2() {
        DateUtils.isSameDay(new Date(), (Date) null);
    }

    @Test
    public void testIsSameDayCalendar() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 0, 0);
        cal2.set(2004, Calendar.JULY, 4, 16, 30, 0);
        Assert.assertTrue(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2004, Calendar.JULY, 5, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2005, Calendar.JULY, 4, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal1.set(Calendar.ERA, GregorianCalendar.BC);
        cal2.set(Calendar.ERA, GregorianCalendar.AD);
        cal2.set(2004, Calendar.JULY, 4, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalendarNull1() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalendarNull2() {
        DateUtils.isSameDay(Calendar.getInstance(), (Calendar) null);
    }

    @Test
    public void testIsSameInstantDate() {
        Date date1 = new Date(1000L);
        Date date2 = new Date(1000L);
        Date date3 = new Date(2000L);
        Assert.assertTrue(DateUtils.isSameInstant(date1, date2));
        Assert.assertFalse(DateUtils.isSameInstant(date1, date3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantDateNull1() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantDateNull2() {
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
    public void testIsSameInstantCalendarNull1() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantCalendarNull2() {
        DateUtils.isSameInstant(Calendar.getInstance(), (Calendar) null);
    }

    @Test
    public void testIsSameLocalTime() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 30, 25);
        cal1.set(Calendar.MILLISECOND, 500);
        cal2.set(2004, Calendar.JULY, 4, 12, 30, 25);
        cal2.set(Calendar.MILLISECOND, 500);
        Assert.assertTrue(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MILLISECOND, 501);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MILLISECOND, 500);
        cal2.set(Calendar.SECOND, 26);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.SECOND, 25);
        cal2.set(Calendar.MINUTE, 31);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.MINUTE, 30);
        cal2.set(Calendar.HOUR, (cal1.get(Calendar.HOUR) + 1) % 12);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.HOUR, cal1.get(Calendar.HOUR));
        cal2.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR) + 1);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR));
        cal2.set(Calendar.YEAR, 2005);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.set(Calendar.YEAR, 2004);
        cal1.set(Calendar.ERA, GregorianCalendar.BC);
        cal2.set(Calendar.ERA, GregorianCalendar.AD);
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, cal2));

        cal1.set(Calendar.ERA, GregorianCalendar.AD);
        Calendar customCal = new GregorianCalendar() {};
        customCal.setTime(cal1.getTime());
        Assert.assertFalse(DateUtils.isSameLocalTime(cal1, customCal));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTimeNull1() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTimeNull2() {
        DateUtils.isSameLocalTime(Calendar.getInstance(), null);
    }

    @Test
    public void testParseDate() throws ParseException {
        String[] parsers = new String[]{"yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd", "yyyyMMdd"};
        Date parsed = DateUtils.parseDate("2004/07/04", parsers);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        Assert.assertEquals(2004, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        Assert.assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));

        parsed = DateUtils.parseDate("20040704", parsers);
        cal.setTime(parsed);
        Assert.assertEquals(2004, cal.get(Calendar.YEAR));
    }

    @Test(expected = ParseException.class)
    public void testParseDateFail() throws ParseException {
        DateUtils.parseDate("2004-07-04", new String[]{"yyyy/MM/dd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullStr() throws ParseException {
        DateUtils.parseDate(null, new String[]{"yyyyMMdd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullPatterns() throws ParseException {
        DateUtils.parseDate("20040704", null);
    }

    @Test
    public void testAddDateMethods() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 30, 20);
        cal.set(Calendar.MILLISECOND, 100);
        Date base = cal.getTime();

        Date res = DateUtils.addYears(base, 1);
        cal.setTime(res);
        Assert.assertEquals(2005, cal.get(Calendar.YEAR));

        res = DateUtils.addMonths(base, 2);
        cal.setTime(res);
        Assert.assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));

        res = DateUtils.addWeeks(base, 1);
        cal.setTime(res);
        Assert.assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));

        res = DateUtils.addDays(base, 5);
        cal.setTime(res);
        Assert.assertEquals(9, cal.get(Calendar.DAY_OF_MONTH));

        res = DateUtils.addHours(base, 2);
        cal.setTime(res);
        Assert.assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));

        res = DateUtils.addMinutes(base, 15);
        cal.setTime(res);
        Assert.assertEquals(45, cal.get(Calendar.MINUTE));

        res = DateUtils.addSeconds(base, 10);
        cal.setTime(res);
        Assert.assertEquals(30, cal.get(Calendar.SECOND));

        res = DateUtils.addMilliseconds(base, 250);
        cal.setTime(res);
        Assert.assertEquals(350, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDate() {
        DateUtils.add(null, Calendar.YEAR, 1);
    }

    @Test
    public void testRoundDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 15, 12, 30, 30);
        cal.set(Calendar.MILLISECOND, 600);
        Date base = cal.getTime();

        Date rSec = DateUtils.round(base, Calendar.SECOND);
        cal.setTime(rSec);
        Assert.assertEquals(31, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        cal.setTime(base);
        cal.set(Calendar.MILLISECOND, 400);
        rSec = DateUtils.round(cal.getTime(), Calendar.SECOND);
        cal.setTime(rSec);
        Assert.assertEquals(30, cal.get(Calendar.SECOND));

        Date rMin = DateUtils.round(base, Calendar.MINUTE);
        cal.setTime(rMin);
        Assert.assertEquals(31, cal.get(Calendar.MINUTE));
        Assert.assertEquals(0, cal.get(Calendar.SECOND));

        cal.setTime(base);
        cal.set(Calendar.SECOND, 20);
        rMin = DateUtils.round(cal.getTime(), Calendar.MINUTE);
        cal.setTime(rMin);
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));

        Date rHour = DateUtils.round(base, Calendar.HOUR);
        cal.setTime(rHour);
        Assert.assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));

        Date rDate = DateUtils.round(base, Calendar.DATE);
        cal.setTime(rDate);
        Assert.assertEquals(16, cal.get(Calendar.DATE));

        Date rMonth = DateUtils.round(base, Calendar.MONTH);
        cal.setTime(rMonth);
        Assert.assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));

        Date rYear = DateUtils.round(base, Calendar.YEAR);
        cal.setTime(rYear);
        Assert.assertEquals(2004, cal.get(Calendar.YEAR));

        Date rMilli = DateUtils.round(base, Calendar.MILLISECOND);
        Assert.assertEquals(base.getTime(), rMilli.getTime());
    }

    @Test
    public void testRoundCalendarAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 1, 1, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar rCal = DateUtils.round(cal, Calendar.DATE);
        Assert.assertEquals(1, rCal.get(Calendar.DATE));

        Date rObjDate = DateUtils.round((Object) cal.getTime(), Calendar.DATE);
        Assert.assertEquals(cal.getTime(), rObjDate);

        Date rObjCal = DateUtils.round((Object) cal, Calendar.DATE);
        Assert.assertEquals(cal.getTime(), rObjCal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundDateNull() {
        DateUtils.round((Date) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundCalendarNull() {
        DateUtils.round((Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundObjectNull() {
        DateUtils.round((Object) null, Calendar.DATE);
    }

    @Test(expected = ClassCastException.class)
    public void testRoundObjectInvalid() {
        DateUtils.round("invalid", Calendar.DATE);
    }

    @Test
    public void testRoundSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar r = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, r.get(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.DAY_OF_MONTH, 9);
        r = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, r.get(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.DAY_OF_MONTH, 16);
        r = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, r.get(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.DAY_OF_MONTH, 24);
        r = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, r.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(Calendar.JULY, r.get(Calendar.MONTH));
    }

    @Test
    public void testRoundAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 1, 3, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar r = DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(0, r.get(Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 8);
        r = DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(12, r.get(Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 15);
        r = DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(12, r.get(Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 20);
        r = DateUtils.round(cal, Calendar.AM_PM);
        Assert.assertEquals(0, r.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(2, r.get(Calendar.DAY_OF_MONTH));
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundLargeYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.round(cal, Calendar.MONTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundUnsupportedField() {
        Calendar cal = Calendar.getInstance();
        DateUtils.round(cal, -9999);
    }

    @Test
    public void testTruncateDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 15, 12, 30, 30);
        cal.set(Calendar.MILLISECOND, 600);
        Date base = cal.getTime();

        Date tSec = DateUtils.truncate(base, Calendar.SECOND);
        cal.setTime(tSec);
        Assert.assertEquals(30, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        Date tMin = DateUtils.truncate(base, Calendar.MINUTE);
        cal.setTime(tMin);
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(0, cal.get(Calendar.SECOND));

        Date tHour = DateUtils.truncate(base, Calendar.HOUR);
        cal.setTime(tHour);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        Date tDate = DateUtils.truncate(base, Calendar.DATE);
        cal.setTime(tDate);
        Assert.assertEquals(15, cal.get(Calendar.DATE));

        Date tMonth = DateUtils.truncate(base, Calendar.MONTH);
        cal.setTime(tMonth);
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        Date tYear = DateUtils.truncate(base, Calendar.YEAR);
        cal.setTime(tYear);
        Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));

        Date tMilli = DateUtils.truncate(base, Calendar.MILLISECOND);
        Assert.assertEquals(base.getTime(), tMilli.getTime());
    }

    @Test
    public void testTruncateCalendarAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 15, 12, 30, 30);
        cal.set(Calendar.MILLISECOND, 600);

        Calendar tCal = DateUtils.truncate(cal, Calendar.DATE);
        Assert.assertEquals(15, tCal.get(Calendar.DATE));
        Assert.assertEquals(0, tCal.get(Calendar.HOUR_OF_DAY));

        Date tObjDate = DateUtils.truncate((Object) cal.getTime(), Calendar.DATE);
        Assert.assertEquals(tCal.getTime(), tObjDate);

        Date tObjCal = DateUtils.truncate((Object) cal, Calendar.DATE);
        Assert.assertEquals(tCal.getTime(), tObjCal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateDateNull() {
        DateUtils.truncate((Date) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateCalendarNull() {
        DateUtils.truncate((Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateObjectNull() {
        DateUtils.truncate((Object) null, Calendar.DATE);
    }

    @Test(expected = ClassCastException.class)
    public void testTruncateObjectInvalid() {
        DateUtils.truncate("invalid", Calendar.DATE);
    }

    @Test
    public void testTruncateSemiMonthAndAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JUNE, 20, 15, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar tSemi = DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, tSemi.get(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.DAY_OF_MONTH, 10);
        tSemi = DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, tSemi.get(Calendar.DAY_OF_MONTH));

        Calendar tAmPm = DateUtils.truncate(cal, Calendar.AM_PM);
        Assert.assertEquals(12, tAmPm.get(Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 5);
        tAmPm = DateUtils.truncate(cal, Calendar.AM_PM);
        Assert.assertEquals(0, tAmPm.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testIterator() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 0, 0);

        Iterator it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_SUNDAY);
        Assert.assertTrue(it.hasNext());
        Calendar start = (Calendar) it.next();
        Assert.assertEquals(Calendar.SUNDAY, start.get(Calendar.DAY_OF_WEEK));
        int count = 1;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(35, count);

        it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_MONDAY);
        start = (Calendar) it.next();
        Assert.assertEquals(Calendar.MONDAY, start.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(7, count);

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_MONDAY);
        start = (Calendar) it.next();
        Assert.assertEquals(Calendar.MONDAY, start.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_RELATIVE);
        start = (Calendar) it.next();
        Assert.assertEquals(cal.get(Calendar.DAY_OF_WEEK), start.get(Calendar.DAY_OF_WEEK));

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_CENTER);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(7, count);
    }

    @Test
    public void testIteratorDateAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4);
        Iterator itDate = DateUtils.iterator(cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(itDate);

        Iterator itObj1 = DateUtils.iterator((Object) cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(itObj1);

        Iterator itObj2 = DateUtils.iterator((Object) cal, DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertNotNull(itObj2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorDateNull() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorCalendarNull() {
        DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorObjectNull() {
        DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = ClassCastException.class)
    public void testIteratorObjectInvalid() {
        DateUtils.iterator("invalid", DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIteratorInvalidRangeStyle() {
        DateUtils.iterator(Calendar.getInstance(), -1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testDateIteratorNoSuchElement() {
        Iterator it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDateIteratorRemove() {
        Iterator it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }
}
