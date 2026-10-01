package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
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
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        Assert.assertEquals(60000L, DateUtils.MILLIS_PER_MINUTE);
        Assert.assertEquals(3600000L, DateUtils.MILLIS_PER_HOUR);
        Assert.assertEquals(86400000L, DateUtils.MILLIS_PER_DAY);
        Assert.assertEquals(TimeZone.getTimeZone("GMT"), DateUtils.UTC_TIME_ZONE);
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
        cal1.set(Calendar.ERA, GregorianCalendar.AD);
        cal2.set(Calendar.ERA, GregorianCalendar.AD);

        Assert.assertTrue(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2004, Calendar.JULY, 5, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2005, Calendar.JULY, 4, 12, 0, 0);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2004, Calendar.JULY, 4, 12, 0, 0);
        cal2.set(Calendar.ERA, GregorianCalendar.BC);
        Assert.assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalNull1() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalNull2() {
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
    public void testIsSameInstantDateNull1() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantDateNull2() {
        DateUtils.isSameInstant(new Date(), (Date) null);
    }

    @Test
    public void testIsSameInstantCalendar() {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(1000L);
        c2.setTimeInMillis(1000L);
        Calendar c3 = Calendar.getInstance();
        c3.setTimeInMillis(2000L);

        Assert.assertTrue(DateUtils.isSameInstant(c1, c2));
        Assert.assertFalse(DateUtils.isSameInstant(c1, c3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantCalNull1() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstantCalNull2() {
        DateUtils.isSameInstant(Calendar.getInstance(), (Calendar) null);
    }

    @Test
    public void testIsSameLocalTime() {
        Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        c1.set(2004, Calendar.JULY, 4, 12, 30, 40);
        c1.set(Calendar.MILLISECOND, 500);
        c2.set(2004, Calendar.JULY, 4, 12, 30, 40);
        c2.set(Calendar.MILLISECOND, 500);

        Assert.assertTrue(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.MILLISECOND, 501);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.MILLISECOND, 500);
        c2.set(Calendar.SECOND, 41);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.SECOND, 40);
        c2.set(Calendar.MINUTE, 31);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.MINUTE, 30);
        c2.set(Calendar.HOUR, c1.get(Calendar.HOUR) + 1);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.HOUR, c1.get(Calendar.HOUR));
        c2.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR) + 1);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.DAY_OF_YEAR, c1.get(Calendar.DAY_OF_YEAR));
        c2.set(Calendar.YEAR, 2005);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));

        c2.set(Calendar.YEAR, 2004);
        c2.set(Calendar.ERA, GregorianCalendar.BC);
        Assert.assertFalse(DateUtils.isSameLocalTime(c1, c2));
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
        Date d = DateUtils.parseDate("2004-07-04", "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        Assert.assertEquals(2004, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        Assert.assertEquals(4, cal.get(Calendar.DATE));

        // Multiple patterns
        Date d2 = DateUtils.parseDate("04/07/2004", "yyyy-MM-dd", "dd/MM/yyyy");
        Assert.assertEquals(d, d2);

        // ZZ pattern
        Date d3 = DateUtils.parseDate("2004-07-04T12:00:00+02:00", "yyyy-MM-dd'T'HH:mm:ssZZ");
        Assert.assertNotNull(d3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullStr() throws ParseException {
        DateUtils.parseDate(null, "yyyy-MM-dd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNullPatterns() throws ParseException {
        DateUtils.parseDate("2004-07-04", (String[]) null);
    }

    @Test(expected = ParseException.class)
    public void testParseDateInvalid() throws ParseException {
        DateUtils.parseDate("2004-07-04", "yyyy/MM/dd");
    }

    @Test
    public void testParseDateStrictly() throws ParseException {
        Date d = DateUtils.parseDateStrictly("2004-07-04", "yyyy-MM-dd");
        Assert.assertNotNull(d);
    }

    @Test(expected = ParseException.class)
    public void testParseDateStrictlyInvalidLenient() throws ParseException {
        DateUtils.parseDateStrictly("2004-02-31", "yyyy-MM-dd");
    }

    @Test
    public void testAddMethods() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 30, 40);
        cal.set(Calendar.MILLISECOND, 500);
        Date base = cal.getTime();

        Date d = DateUtils.addYears(base, 1);
        cal.setTime(d);
        Assert.assertEquals(2005, cal.get(Calendar.YEAR));

        d = DateUtils.addMonths(base, 1);
        cal.setTime(d);
        Assert.assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));

        d = DateUtils.addWeeks(base, 1);
        cal.setTime(d);
        Assert.assertEquals(11, cal.get(Calendar.DATE));

        d = DateUtils.addDays(base, 1);
        cal.setTime(d);
        Assert.assertEquals(5, cal.get(Calendar.DATE));

        d = DateUtils.addHours(base, 1);
        cal.setTime(d);
        Assert.assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));

        d = DateUtils.addMinutes(base, 1);
        cal.setTime(d);
        Assert.assertEquals(31, cal.get(Calendar.MINUTE));

        d = DateUtils.addSeconds(base, 1);
        cal.setTime(d);
        Assert.assertEquals(41, cal.get(Calendar.SECOND));

        d = DateUtils.addMilliseconds(base, 1);
        cal.setTime(d);
        Assert.assertEquals(501, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddYearsNull() {
        DateUtils.addYears(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMonthsNull() {
        DateUtils.addMonths(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWeeksNull() {
        DateUtils.addWeeks(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDaysNull() {
        DateUtils.addDays(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHoursNull() {
        DateUtils.addHours(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMinutesNull() {
        DateUtils.addMinutes(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSecondsNull() {
        DateUtils.addSeconds(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMillisecondsNull() {
        DateUtils.addMilliseconds(null, 1);
    }

    @Test
    public void testSetMethods() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 30, 40);
        cal.set(Calendar.MILLISECOND, 500);
        Date base = cal.getTime();

        Date d = DateUtils.setYears(base, 2010);
        cal.setTime(d);
        Assert.assertEquals(2010, cal.get(Calendar.YEAR));

        d = DateUtils.setMonths(base, Calendar.DECEMBER);
        cal.setTime(d);
        Assert.assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));

        d = DateUtils.setDays(base, 20);
        cal.setTime(d);
        Assert.assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));

        d = DateUtils.setHours(base, 5);
        cal.setTime(d);
        Assert.assertEquals(5, cal.get(Calendar.HOUR_OF_DAY));

        d = DateUtils.setMinutes(base, 15);
        cal.setTime(d);
        Assert.assertEquals(15, cal.get(Calendar.MINUTE));

        d = DateUtils.setSeconds(base, 25);
        cal.setTime(d);
        Assert.assertEquals(25, cal.get(Calendar.SECOND));

        d = DateUtils.setMilliseconds(base, 123);
        cal.setTime(d);
        Assert.assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetYearsNull() {
        DateUtils.setYears(null, 2010);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMonthsNull() {
        DateUtils.setMonths(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDaysNull() {
        DateUtils.setDays(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHoursNull() {
        DateUtils.setHours(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinutesNull() {
        DateUtils.setMinutes(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSecondsNull() {
        DateUtils.setSeconds(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMillisecondsNull() {
        DateUtils.setMilliseconds(null, 1);
    }

    @Test
    public void testToCalendar() {
        Date now = new Date();
        Calendar cal = DateUtils.toCalendar(now);
        Assert.assertEquals(now.getTime(), cal.getTimeInMillis());
    }

    @Test(expected = NullPointerException.class)
    public void testToCalendarNull() {
        DateUtils.toCalendar(null);
    }

    @Test
    public void testRoundDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        Date d = cal.getTime();

        Date rSec = DateUtils.round(d, Calendar.SECOND);
        cal.setTime(rSec);
        Assert.assertEquals(57, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        Date rMin = DateUtils.round(d, Calendar.MINUTE);
        cal.setTime(rMin);
        Assert.assertEquals(35, cal.get(Calendar.MINUTE));

        Date rHour = DateUtils.round(d, Calendar.HOUR_OF_DAY);
        cal.setTime(rHour);
        Assert.assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));

        Date rDay = DateUtils.round(d, Calendar.DATE);
        cal.setTime(rDay);
        Assert.assertEquals(5, cal.get(Calendar.DATE));

        Date rMonth = DateUtils.round(d, Calendar.MONTH);
        cal.setTime(rMonth);
        Assert.assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));

        Date rYear = DateUtils.round(d, Calendar.YEAR);
        cal.setTime(rYear);
        Assert.assertEquals(2005, cal.get(Calendar.YEAR));
    }

    @Test
    public void testRoundSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 1, 0, 0, 0);
        Date r1 = DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH);
        cal.setTime(r1);
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        cal.set(2004, Calendar.JULY, 10, 0, 0, 0);
        Date r2 = DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH);
        cal.setTime(r2);
        Assert.assertEquals(16, cal.get(Calendar.DATE));

        cal.set(2004, Calendar.JULY, 20, 0, 0, 0);
        Date r3 = DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH);
        cal.setTime(r3);
        Assert.assertEquals(16, cal.get(Calendar.DATE));

        cal.set(2004, Calendar.JULY, 26, 0, 0, 0);
        Date r4 = DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH);
        cal.setTime(r4);
        Assert.assertEquals(1, cal.get(Calendar.DATE));
        Assert.assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));
    }

    @Test
    public void testRoundAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 3, 0, 0);
        Date r1 = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        cal.setTime(r1);
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));

        cal.set(2004, Calendar.JULY, 4, 8, 0, 0);
        Date r2 = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        cal.setTime(r2);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        cal.set(2004, Calendar.JULY, 4, 18, 0, 0);
        Date r3 = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        cal.setTime(r3);
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(5, cal.get(Calendar.DATE));
    }

    @Test
    public void testRoundObjectAndCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);

        Calendar rCal = DateUtils.round(cal, Calendar.MINUTE);
        Assert.assertEquals(35, rCal.get(Calendar.MINUTE));

        Date rDate = DateUtils.round((Object) cal.getTime(), Calendar.MINUTE);
        Assert.assertNotNull(rDate);

        Date rDateFromCal = DateUtils.round((Object) cal, Calendar.MINUTE);
        Assert.assertNotNull(rDateFromCal);
    }

    @Test(expected = ClassCastException.class)
    public void testRoundObjectInvalidType() {
        DateUtils.round("invalid", Calendar.DATE);
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

    @Test
    public void testTruncate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);

        Date d = cal.getTime();
        Date tSec = DateUtils.truncate(d, Calendar.SECOND);
        cal.setTime(tSec);
        Assert.assertEquals(56, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        Date tMin = DateUtils.truncate(d, Calendar.MINUTE);
        cal.setTime(tMin);
        Assert.assertEquals(34, cal.get(Calendar.MINUTE));
        Assert.assertEquals(0, cal.get(Calendar.SECOND));

        Date tHour = DateUtils.truncate(d, Calendar.HOUR_OF_DAY);
        cal.setTime(tHour);
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        Date tDay = DateUtils.truncate(d, Calendar.DATE);
        cal.setTime(tDay);
        Assert.assertEquals(4, cal.get(Calendar.DATE));
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));

        Date tMonth = DateUtils.truncate(d, Calendar.MONTH);
        cal.setTime(tMonth);
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        Date tYear = DateUtils.truncate(d, Calendar.YEAR);
        cal.setTime(tYear);
        Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));

        Date tMillis = DateUtils.truncate(d, Calendar.MILLISECOND);
        Assert.assertEquals(d, tMillis);

        Calendar tCal = DateUtils.truncate(DateUtils.toCalendar(d), Calendar.HOUR);
        Assert.assertEquals(12, tCal.get(Calendar.HOUR_OF_DAY));

        Date tObjDate = DateUtils.truncate((Object) d, Calendar.HOUR);
        Assert.assertNotNull(tObjDate);

        Date tObjCal = DateUtils.truncate((Object) DateUtils.toCalendar(d), Calendar.HOUR);
        Assert.assertNotNull(tObjCal);
    }

    @Test(expected = ClassCastException.class)
    public void testTruncateObjectInvalid() {
        DateUtils.truncate("invalid", Calendar.DATE);
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

    @Test
    public void testCeiling() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);

        Date d = cal.getTime();
        Date cSec = DateUtils.ceiling(d, Calendar.SECOND);
        cal.setTime(cSec);
        Assert.assertEquals(57, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

        Date cMin = DateUtils.ceiling(d, Calendar.MINUTE);
        cal.setTime(cMin);
        Assert.assertEquals(35, cal.get(Calendar.MINUTE));

        Date cHour = DateUtils.ceiling(d, Calendar.HOUR_OF_DAY);
        cal.setTime(cHour);
        Assert.assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));

        Date cDay = DateUtils.ceiling(d, Calendar.DATE);
        cal.setTime(cDay);
        Assert.assertEquals(5, cal.get(Calendar.DATE));

        Date cMonth = DateUtils.ceiling(d, Calendar.MONTH);
        cal.setTime(cMonth);
        Assert.assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));

        Date cYear = DateUtils.ceiling(d, Calendar.YEAR);
        cal.setTime(cYear);
        Assert.assertEquals(2005, cal.get(Calendar.YEAR));

        Calendar cCal = DateUtils.ceiling(DateUtils.toCalendar(d), Calendar.HOUR);
        Assert.assertEquals(13, cCal.get(Calendar.HOUR_OF_DAY));

        Date cObjDate = DateUtils.ceiling((Object) d, Calendar.HOUR);
        Assert.assertNotNull(cObjDate);

        Date cObjCal = DateUtils.ceiling((Object) DateUtils.toCalendar(d), Calendar.HOUR);
        Assert.assertNotNull(cObjCal);
    }

    @Test
    public void testCeilingSpecialCases() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar ceiledSemi = DateUtils.ceiling(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(16, ceiledSemi.get(Calendar.DATE));

        cal.set(2004, Calendar.JULY, 2, 0, 0, 0);
        ceiledSemi = DateUtils.ceiling(cal, DateUtils.SEMI_MONTH);
        Assert.assertEquals(1, ceiledSemi.get(Calendar.DATE));
        Assert.assertEquals(Calendar.AUGUST, ceiledSemi.get(Calendar.MONTH));

        cal.set(2004, Calendar.JULY, 1, 0, 0, 0);
        Calendar ceiledAmPm = DateUtils.ceiling(cal, Calendar.AM_PM);
        Assert.assertEquals(12, ceiledAmPm.get(Calendar.HOUR_OF_DAY));

        cal.set(2004, Calendar.JULY, 1, 1, 0, 0);
        ceiledAmPm = DateUtils.ceiling(cal, Calendar.AM_PM);
        Assert.assertEquals(0, ceiledAmPm.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(2, ceiledAmPm.get(Calendar.DATE));
    }

    @Test(expected = ClassCastException.class)
    public void testCeilingObjectInvalid() {
        DateUtils.ceiling("invalid", Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeilingNullDate() {
        DateUtils.ceiling((Date) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeilingNullCalendar() {
        DateUtils.ceiling((Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeilingNullObject() {
        DateUtils.ceiling((Object) null, Calendar.DATE);
    }

    @Test(expected = ArithmeticException.class)
    public void testModifyLargeYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.round(cal, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModifyUnsupportedField() {
        Calendar cal = Calendar.getInstance();
        DateUtils.round(cal, -999);
    }

    @Test
    public void testIterator() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4);

        int[] styles = {
                DateUtils.RANGE_MONTH_SUNDAY,
                DateUtils.RANGE_MONTH_MONDAY,
                DateUtils.RANGE_WEEK_SUNDAY,
                DateUtils.RANGE_WEEK_MONDAY,
                DateUtils.RANGE_WEEK_RELATIVE,
                DateUtils.RANGE_WEEK_CENTER
        };

        for (int style : styles) {
            Iterator<Calendar> it = DateUtils.iterator(cal, style);
            Assert.assertTrue(it.hasNext());
            int count = 0;
            while (it.hasNext()) {
                Calendar c = it.next();
                Assert.assertNotNull(c);
                count++;
            }
            Assert.assertTrue(count >= 7);
        }

        // Test Date overload
        Iterator<Calendar> itDate = DateUtils.iterator(cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertTrue(itDate.hasNext());

        // Test Object overload
        Iterator<?> itObj1 = DateUtils.iterator((Object) cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertTrue(itObj1.hasNext());
        Iterator<?> itObj2 = DateUtils.iterator((Object) cal, DateUtils.RANGE_WEEK_SUNDAY);
        Assert.assertTrue(itObj2.hasNext());
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
        DateUtils.iterator(cal, 9999);
    }

    @Test(expected = NoSuchElementException.class)
    public void testDateIteratorNoSuchElement() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4);
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDateIteratorRemove() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 4);
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    @Test
    public void testGetFragmentDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2008, Calendar.JANUARY, 6, 7, 15, 10);
        cal.set(Calendar.MILLISECOND, 538);
        Date d = cal.getTime();

        Assert.assertEquals(538L, DateUtils.getFragmentInMilliseconds(d, Calendar.SECOND));
        Assert.assertEquals(10538L, DateUtils.getFragmentInMilliseconds(d, Calendar.MINUTE));
        Assert.assertEquals(0L, DateUtils.getFragmentInMilliseconds(d, Calendar.MILLISECOND));

        Assert.assertEquals(10L, DateUtils.getFragmentInSeconds(d, Calendar.MINUTE));
        Assert.assertEquals(0L, DateUtils.getFragmentInSeconds(d, Calendar.MILLISECOND));

        Assert.assertEquals(15L, DateUtils.getFragmentInMinutes(d, Calendar.HOUR_OF_DAY));
        Assert.assertEquals(0L, DateUtils.getFragmentInMinutes(d, Calendar.MILLISECOND));

        Assert.assertEquals(7L, DateUtils.getFragmentInHours(d, Calendar.DAY_OF_YEAR));
        Assert.assertEquals(0L, DateUtils.getFragmentInHours(d, Calendar.MILLISECOND));

        Assert.assertEquals(6L, DateUtils.getFragmentInDays(d, Calendar.MONTH));
        Assert.assertEquals(6L, DateUtils.getFragmentInDays(d, Calendar.YEAR));
        Assert.assertEquals(0L, DateUtils.getFragmentInDays(d, Calendar.MILLISECOND));
    }

    @Test
    public void testGetFragmentCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(2008, Calendar.JANUARY, 6, 7, 15, 10);
        cal.set(Calendar.MILLISECOND, 538);

        Assert.assertEquals(538L, DateUtils.getFragmentInMilliseconds(cal, Calendar.SECOND));
        Assert.assertEquals(10538L, DateUtils.getFragmentInMilliseconds(cal, Calendar.MINUTE));
        Assert.assertEquals(0L, DateUtils.getFragmentInMilliseconds(cal, Calendar.MILLISECOND));

        Assert.assertEquals(10L, DateUtils.getFragmentInSeconds(cal, Calendar.MINUTE));
        Assert.assertEquals(26110L, DateUtils.getFragmentInSeconds(cal, Calendar.DAY_OF_YEAR));
        Assert.assertEquals(0L, DateUtils.getFragmentInSeconds(cal, Calendar.MILLISECOND));

        Assert.assertEquals(15L, DateUtils.getFragmentInMinutes(cal, Calendar.HOUR_OF_DAY));
        Assert.assertEquals(435L, DateUtils.getFragmentInMinutes(cal, Calendar.MONTH));
        Assert.assertEquals(0L, DateUtils.getFragmentInMinutes(cal, Calendar.MILLISECOND));

        Assert.assertEquals(7L, DateUtils.getFragmentInHours(cal, Calendar.DAY_OF_YEAR));
        Assert.assertEquals(127L, DateUtils.getFragmentInHours(cal, Calendar.MONTH));
        Assert.assertEquals(0L, DateUtils.getFragmentInHours(cal, Calendar.MILLISECOND));

        Assert.assertEquals(6L, DateUtils.getFragmentInDays(cal, Calendar.MONTH));
        Assert.assertEquals(6L, DateUtils.getFragmentInDays(cal, Calendar.YEAR));
        Assert.assertEquals(0L, DateUtils.getFragmentInDays(cal, Calendar.MILLISECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFragmentDateNull() {
        DateUtils.getFragmentInDays((Date) null, Calendar.YEAR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFragmentCalNull() {
        DateUtils.getFragmentInDays((Calendar) null, Calendar.YEAR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFragmentUnsupportedFragment() {
        Calendar cal = Calendar.getInstance();
        DateUtils.getFragmentInDays(cal, Calendar.ERA);
    }

    @Test
    public void testTruncatedEqualsAndCompareTo() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.set(2004, Calendar.JULY, 4, 12, 34, 56);
        cal2.set(2004, Calendar.JULY, 4, 12, 34, 0);

        Assert.assertTrue(DateUtils.truncatedEquals(cal1, cal2, Calendar.MINUTE));
        Assert.assertFalse(DateUtils.truncatedEquals(cal1, cal2, Calendar.SECOND));
        Assert.assertEquals(0, DateUtils.truncatedCompareTo(cal1, cal2, Calendar.MINUTE));
        Assert.assertTrue(DateUtils.truncatedCompareTo(cal1, cal2, Calendar.SECOND) > 0);
        Assert.assertTrue(DateUtils.truncatedCompareTo(cal2, cal1, Calendar.SECOND) < 0);

        Date d1 = cal1.getTime();
        Date d2 = cal2.getTime();

        Assert.assertTrue(DateUtils.truncatedEquals(d1, d2, Calendar.MINUTE));
        Assert.assertFalse(DateUtils.truncatedEquals(d1, d2, Calendar.SECOND));
        Assert.assertEquals(0, DateUtils.truncatedCompareTo(d1, d2, Calendar.MINUTE));
        Assert.assertTrue(DateUtils.truncatedCompareTo(d1, d2, Calendar.SECOND) > 0);
        Assert.assertTrue(DateUtils.truncatedCompareTo(d2, d1, Calendar.SECOND) < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncatedEqualsNullCal1() {
        DateUtils.truncatedEquals((Calendar) null, Calendar.getInstance(), Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncatedEqualsNullCal2() {
        DateUtils.truncatedEquals(Calendar.getInstance(), (Calendar) null, Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncatedEqualsNullDate1() {
        DateUtils.truncatedEquals((Date) null, new Date(), Calendar.DATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncatedEqualsNullDate2() {
        DateUtils.truncatedEquals(new Date(), (Date) null, Calendar.DATE);
    }
}
