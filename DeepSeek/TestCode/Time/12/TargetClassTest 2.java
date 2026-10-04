package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class LocalDateTest {

    //-----------------------------------------------------------------------
    // now() tests
    //-----------------------------------------------------------------------
    @Test
    public void testNow() {
        LocalDate now = LocalDate.now();
        assertNotNull(now);
        assertEquals(ISOChronology.getInstance(), now.getChronology());
    }

    @Test
    public void testNow_DateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        LocalDate now = LocalDate.now(zone);
        assertNotNull(now);
        assertEquals(ISOChronology.getInstance(zone), now.getChronology());
    }

    @Test(expected = NullPointerException.class)
    public void testNow_nullZone() {
        LocalDate.now((DateTimeZone) null);
    }

    @Test
    public void testNow_Chronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate now = LocalDate.now(chrono);
        assertNotNull(now);
        assertEquals(chrono.withUTC(), now.getChronology());
    }

    @Test(expected = NullPointerException.class)
    public void testNow_nullChronology() {
        LocalDate.now((Chronology) null);
    }

    //-----------------------------------------------------------------------
    // parse() tests
    //-----------------------------------------------------------------------
    @Test
    public void testParse() {
        LocalDate date = LocalDate.parse("2020-06-15");
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testParse_withFormatter() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");
        LocalDate date = LocalDate.parse("2020/06/15", formatter);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // fromCalendarFields() tests
    //-----------------------------------------------------------------------
    @Test
    public void testFromCalendarFields() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15);
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_null() {
        LocalDate.fromCalendarFields(null);
    }

    //-----------------------------------------------------------------------
    // fromDateFields() tests
    //-----------------------------------------------------------------------
    @Test
    public void testFromDateFields() {
        Date date = new Date(2020 - 1900, 5, 15); // June 15, 2020
        LocalDate localDate = LocalDate.fromDateFields(date);
        assertEquals(2020, localDate.getYear());
        assertEquals(6, localDate.getMonthOfYear());
        assertEquals(15, localDate.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_null() {
        LocalDate.fromDateFields(null);
    }

    //-----------------------------------------------------------------------
    // Constructors
    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        LocalDate date = new LocalDate();
        assertNotNull(date);
    }

    @Test
    public void testConstructor_DateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        LocalDate date = new LocalDate(zone);
        assertNotNull(date);
    }

    @Test
    public void testConstructor_Chronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate date = new LocalDate(chrono);
        assertNotNull(date);
    }

    @Test
    public void testConstructor_long() {
        LocalDate date = new LocalDate(100000000000L);
        assertNotNull(date);
    }

    @Test
    public void testConstructor_long_DateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDate date = new LocalDate(100000000000L, zone);
        assertNotNull(date);
    }

    @Test
    public void testConstructor_long_Chronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate date = new LocalDate(100000000000L, chrono);
        assertNotNull(date);
    }

    @Test
    public void testConstructor_Object() {
        LocalDate date = new LocalDate("2020-06-15");
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testConstructor_Object_DateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        LocalDate date = new LocalDate("2020-06-15", zone);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testConstructor_Object_Chronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate date = new LocalDate("2020-06-15", chrono);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testConstructor_int_int_int() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testConstructor_int_int_int_Chronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate date = new LocalDate(2020, 6, 15, chrono);
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // size() and getField()
    //-----------------------------------------------------------------------
    @Test
    public void testSize() {
        LocalDate date = new LocalDate();
        assertEquals(3, date.size());
    }

    @Test
    public void testGetField() {
        LocalDate date = new LocalDate();
        assertNotNull(date.getField(0, ISOChronology.getInstanceUTC()));
        assertNotNull(date.getField(1, ISOChronology.getInstanceUTC()));
        assertNotNull(date.getField(2, ISOChronology.getInstanceUTC()));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetField_invalidIndex() {
        LocalDate date = new LocalDate();
        date.getField(3, ISOChronology.getInstanceUTC());
    }

    //-----------------------------------------------------------------------
    // getValue()
    //-----------------------------------------------------------------------
    @Test
    public void testGetValue() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(2020, date.getValue(0));
        assertEquals(6, date.getValue(1));
        assertEquals(15, date.getValue(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex() {
        LocalDate date = new LocalDate();
        date.getValue(3);
    }

    //-----------------------------------------------------------------------
    // get() and isSupported()
    //-----------------------------------------------------------------------
    @Test
    public void testGet() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(2020, date.get(DateTimeFieldType.year()));
        assertEquals(6, date.get(DateTimeFieldType.monthOfYear()));
        assertEquals(15, date.get(DateTimeFieldType.dayOfMonth()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_nullFieldType() {
        LocalDate date = new LocalDate();
        date.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_unsupportedFieldType() {
        LocalDate date = new LocalDate();
        date.get(DateTimeFieldType.hourOfDay());
    }

    @Test
    public void testIsSupported_DateTimeFieldType() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DateTimeFieldType.year()));
        assertTrue(date.isSupported(DateTimeFieldType.monthOfYear()));
        assertTrue(date.isSupported(DateTimeFieldType.dayOfMonth()));
        assertFalse(date.isSupported(DateTimeFieldType.hourOfDay()));
        assertFalse(date.isSupported(null));
    }

    @Test
    public void testIsSupported_DurationFieldType() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DurationFieldType.years()));
        assertTrue(date.isSupported(DurationFieldType.months()));
        assertTrue(date.isSupported(DurationFieldType.days()));
        assertFalse(date.isSupported(DurationFieldType.hours()));
        assertFalse(date.isSupported(null));
    }

    //-----------------------------------------------------------------------
    // getLocalMillis() and getChronology()
    //-----------------------------------------------------------------------
    @Test
    public void testGetLocalMillis() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertTrue(date.getLocalMillis() > 0);
    }

    @Test
    public void testGetChronology() {
        LocalDate date = new LocalDate();
        assertEquals(ISOChronology.getInstance(), date.getChronology());
    }

    //-----------------------------------------------------------------------
    // equals() and hashCode()
    //-----------------------------------------------------------------------
    @Test
    public void testEquals() {
        LocalDate date1 = new LocalDate(2020, 6, 15);
        LocalDate date2 = new LocalDate(2020, 6, 15);
        LocalDate date3 = new LocalDate(2020, 6, 16);
        assertTrue(date1.equals(date2));
        assertFalse(date1.equals(date3));
        assertTrue(date1.equals(date1));
        assertFalse(date1.equals(null));
        assertFalse(date1.equals("string"));
    }

    @Test
    public void testHashCode() {
        LocalDate date1 = new LocalDate(2020, 6, 15);
        LocalDate date2 = new LocalDate(2020, 6, 15);
        assertEquals(date1.hashCode(), date2.hashCode());
    }

    //-----------------------------------------------------------------------
    // compareTo()
    //-----------------------------------------------------------------------
    @Test
    public void testCompareTo() {
        LocalDate date1 = new LocalDate(2020, 6, 15);
        LocalDate date2 = new LocalDate(2020, 6, 15);
        LocalDate date3 = new LocalDate(2020, 6, 16);
        assertEquals(0, date1.compareTo(date2));
        assertTrue(date1.compareTo(date3) < 0);
        assertTrue(date3.compareTo(date1) > 0);
        assertEquals(0, date1.compareTo(date1));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_null() {
        LocalDate date = new LocalDate();
        date.compareTo(null);
    }

    //-----------------------------------------------------------------------
    // toDateTimeAtStartOfDay()
    //-----------------------------------------------------------------------
    @Test
    public void testToDateTimeAtStartOfDay() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTime dt = date.toDateTimeAtStartOfDay();
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(0, dt.getMillisOfDay());
    }

    @Test
    public void testToDateTimeAtStartOfDay_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime dt = date.toDateTimeAtStartOfDay(zone);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // toDateTimeAtMidnight()
    //-----------------------------------------------------------------------
    @Test
    public void testToDateTimeAtMidnight() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTime dt = date.toDateTimeAtMidnight();
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testToDateTimeAtMidnight_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime dt = date.toDateTimeAtMidnight(zone);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // toDateTimeAtCurrentTime()
    //-----------------------------------------------------------------------
    @Test
    public void testToDateTimeAtCurrentTime() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTime dt = date.toDateTimeAtCurrentTime();
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testToDateTimeAtCurrentTime_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime dt = date.toDateTimeAtCurrentTime(zone);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // toDateMidnight()
    //-----------------------------------------------------------------------
    @Test
    public void testToDateMidnight() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateMidnight dm = date.toDateMidnight();
        assertEquals(2020, dm.getYear());
        assertEquals(6, dm.getMonthOfYear());
        assertEquals(15, dm.getDayOfMonth());
    }

    @Test
    public void testToDateMidnight_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateMidnight dm = date.toDateMidnight(zone);
        assertEquals(2020, dm.getYear());
        assertEquals(6, dm.getMonthOfYear());
        assertEquals(15, dm.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // toLocalDateTime()
    //-----------------------------------------------------------------------
    @Test
    public void testToLocalDateTime() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalTime time = new LocalTime(10, 30);
        LocalDateTime ldt = date.toLocalDateTime(time);
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_nullTime() {
        LocalDate date = new LocalDate();
        date.toLocalDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_mismatchedChronology() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalTime time = new LocalTime(10, 30, 0, 0, GregorianChronology.getInstance());
        date.toLocalDateTime(time);
    }

    //-----------------------------------------------------------------------
    // toDateTime(LocalTime)
    //-----------------------------------------------------------------------
    @Test
    public void testToDateTime_LocalTime() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalTime time = new LocalTime(10, 30);
        DateTime dt = date.toDateTime(time);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
    }

    @Test
    public void testToDateTime_nullTime() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTime dt = date.toDateTime(null);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testToDateTime_LocalTime_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalTime time = new LocalTime(10, 30);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime dt = date.toDateTime(time, zone);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToDateTime_mismatchedChronology() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalTime time = new LocalTime(10, 30, 0, 0, GregorianChronology.getInstance());
        date.toDateTime(time);
    }

    //-----------------------------------------------------------------------
    // toInterval()
    //-----------------------------------------------------------------------
    @Test
    public void testToInterval() {
        LocalDate date = new LocalDate(2020, 6, 15);
        Interval interval = date.toInterval();
        assertEquals(date.toDateTimeAtStartOfDay(), interval.getStart());
        assertEquals(date.plusDays(1).toDateTimeAtStartOfDay(), interval.getEnd());
    }

    @Test
    public void testToInterval_zone() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Interval interval = date.toInterval(zone);
        assertEquals(date.toDateTimeAtStartOfDay(zone), interval.getStart());
        assertEquals(date.plusDays(1).toDateTimeAtStartOfDay(zone), interval.getEnd());
    }

    //-----------------------------------------------------------------------
    // toDate()
    //-----------------------------------------------------------------------
    @Test
    public void testToDate() {
        LocalDate date = new LocalDate(2020, 6, 15);
        Date jdkDate = date.toDate();
        LocalDate check = LocalDate.fromDateFields(jdkDate);
        assertEquals(date, check);
    }

    //-----------------------------------------------------------------------
    // withLocalMillis()
    //-----------------------------------------------------------------------
    @Test
    public void testWithLocalMillis() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withLocalMillis(date.getLocalMillis() + 86400000);
        assertEquals(2020, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(16, newDate.getDayOfMonth());
    }

    @Test
    public void testWithLocalMillis_sameMillis() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.withLocalMillis(date.getLocalMillis()));
    }

    //-----------------------------------------------------------------------
    // withFields()
    //-----------------------------------------------------------------------
    @Test
    public void testWithFields() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withFields(new YearMonthDay(2021, 7, 16));
        assertEquals(2021, newDate.getYear());
        assertEquals(7, newDate.getMonthOfYear());
        assertEquals(16, newDate.getDayOfMonth());
    }

    @Test
    public void testWithFields_null() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.withFields(null));
    }

    //-----------------------------------------------------------------------
    // withField()
    //-----------------------------------------------------------------------
    @Test
    public void testWithField() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withField(DateTimeFieldType.year(), 2021);
        assertEquals(2021, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_nullFieldType() {
        LocalDate date = new LocalDate();
        date.withField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupportedFieldType() {
        LocalDate date = new LocalDate();
        date.withField(DateTimeFieldType.hourOfDay(), 1);
    }

    //-----------------------------------------------------------------------
    // withFieldAdded()
    //-----------------------------------------------------------------------
    @Test
    public void testWithFieldAdded() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withFieldAdded(DurationFieldType.years(), 1);
        assertEquals(2021, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testWithFieldAdded_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.withFieldAdded(DurationFieldType.years(), 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_nullFieldType() {
        LocalDate date = new LocalDate();
        date.withFieldAdded(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_unsupportedFieldType() {
        LocalDate date = new LocalDate();
        date.withFieldAdded(DurationFieldType.hours(), 1);
    }

    //-----------------------------------------------------------------------
    // withPeriodAdded()
    //-----------------------------------------------------------------------
    @Test
    public void testWithPeriodAdded() {
        LocalDate date = new LocalDate(2020, 6, 15);
        Period period = Period.years(1);
        LocalDate newDate = date.withPeriodAdded(period, 1);
        assertEquals(2021, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testWithPeriodAdded_nullPeriod() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.withPeriodAdded(null, 1));
    }

    @Test
    public void testWithPeriodAdded_zeroScalar() {
        LocalDate date = new LocalDate(2020, 6, 15);
        Period period = Period.years(1);
        assertSame(date, date.withPeriodAdded(period, 0));
    }

    //-----------------------------------------------------------------------
    // plus(ReadablePeriod)
    //-----------------------------------------------------------------------
    @Test
    public void testPlus() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.plus(Period.years(1));
        assertEquals(2021, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testPlus_null() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.plus(null));
    }

    //-----------------------------------------------------------------------
    // plusYears()
    //-----------------------------------------------------------------------
    @Test
    public void testPlusYears() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.plusYears(1);
        assertEquals(2021, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testPlusYears_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.plusYears(0));
    }

    //-----------------------------------------------------------------------
    // plusMonths()
    //-----------------------------------------------------------------------
    @Test
    public void testPlusMonths() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.plusMonths(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(7, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testPlusMonths_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.plusMonths(0));
    }

    //-----------------------------------------------------------------------
    // plusWeeks()
    //-----------------------------------------------------------------------
    @Test
    public void testPlusWeeks() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.plusWeeks(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(22, newDate.getDayOfMonth());
    }

    @Test
    public void testPlusWeeks_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.plusWeeks(0));
    }

    //-----------------------------------------------------------------------
    // plusDays()
    //-----------------------------------------------------------------------
    @Test
    public void testPlusDays() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.plusDays(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(16, newDate.getDayOfMonth());
    }

    @Test
    public void testPlusDays_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.plusDays(0));
    }

    //-----------------------------------------------------------------------
    // minus(ReadablePeriod)
    //-----------------------------------------------------------------------
    @Test
    public void testMinus() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.minus(Period.years(1));
        assertEquals(2019, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testMinus_null() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.minus(null));
    }

    //-----------------------------------------------------------------------
    // minusYears()
    //-----------------------------------------------------------------------
    @Test
    public void testMinusYears() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.minusYears(1);
        assertEquals(2019, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testMinusYears_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.minusYears(0));
    }

    //-----------------------------------------------------------------------
    // minusMonths()
    //-----------------------------------------------------------------------
    @Test
    public void testMinusMonths() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.minusMonths(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(5, newDate.getMonthOfYear());
        assertEquals(15, newDate.getDayOfMonth());
    }

    @Test
    public void testMinusMonths_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.minusMonths(0));
    }

    //-----------------------------------------------------------------------
    // minusWeeks()
    //-----------------------------------------------------------------------
    @Test
    public void testMinusWeeks() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.minusWeeks(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(8, newDate.getDayOfMonth());
    }

    @Test
    public void testMinusWeeks_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.minusWeeks(0));
    }

    //-----------------------------------------------------------------------
    // minusDays()
    //-----------------------------------------------------------------------
    @Test
    public void testMinusDays() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.minusDays(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(14, newDate.getDayOfMonth());
    }

    @Test
    public void testMinusDays_zero() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertSame(date, date.minusDays(0));
    }

    //-----------------------------------------------------------------------
    // property()
    //-----------------------------------------------------------------------
    @Test
    public void testProperty() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.property(DateTimeFieldType.year());
        assertNotNull(prop);
        assertEquals(2020, prop.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_nullFieldType() {
        LocalDate date = new LocalDate();
        date.property(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_unsupportedFieldType() {
        LocalDate date = new LocalDate();
        date.property(DateTimeFieldType.hourOfDay());
    }

    //-----------------------------------------------------------------------
    // Getters
    //-----------------------------------------------------------------------
    @Test
    public void testGetEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(1, date.getEra());
    }

    @Test
    public void testGetCenturyOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(20, date.getCenturyOfEra());
    }

    @Test
    public void testGetYearOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(2020, date.getYearOfEra());
    }

    @Test
    public void testGetYearOfCentury() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(20, date.getYearOfCentury());
    }

    @Test
    public void testGetYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(2020, date.getYear());
    }

    @Test
    public void testGetWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertTrue(date.getWeekyear() > 0);
    }

    @Test
    public void testGetMonthOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(6, date.getMonthOfYear());
    }

    @Test
    public void testGetWeekOfWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertTrue(date.getWeekOfWeekyear() > 0);
    }

    @Test
    public void testGetDayOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertTrue(date.getDayOfYear() > 0);
    }

    @Test
    public void testGetDayOfMonth() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testGetDayOfWeek() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(DateTimeConstants.MONDAY, date.getDayOfWeek());
    }

    //-----------------------------------------------------------------------
    // With methods
    //-----------------------------------------------------------------------
    @Test
    public void testWithEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withEra(1);
        assertEquals(2020, newDate.getYear());
    }

    @Test
    public void testWithCenturyOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withCenturyOfEra(20);
        assertEquals(2020, newDate.getYear());
    }

    @Test
    public void testWithYearOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withYearOfEra(2021);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testWithYearOfCentury() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withYearOfCentury(21);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testWithYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withYear(2021);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testWithWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withWeekyear(2021);
        assertTrue(newDate.getWeekyear() == 2021);
    }

    @Test
    public void testWithMonthOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withMonthOfYear(7);
        assertEquals(7, newDate.getMonthOfYear());
    }

    @Test
    public void testWithWeekOfWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withWeekOfWeekyear(1);
        assertEquals(1, newDate.getWeekOfWeekyear());
    }

    @Test
    public void testWithDayOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withDayOfYear(1);
        assertEquals(1, newDate.getDayOfYear());
    }

    @Test
    public void testWithDayOfMonth() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withDayOfMonth(1);
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testWithDayOfWeek() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate newDate = date.withDayOfWeek(DateTimeConstants.TUESDAY);
        assertEquals(DateTimeConstants.TUESDAY, newDate.getDayOfWeek());
    }

    //-----------------------------------------------------------------------
    // Property accessors
    //-----------------------------------------------------------------------
    @Test
    public void testEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.era());
        assertEquals(1, date.era().get());
    }

    @Test
    public void testCenturyOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.centuryOfEra());
    }

    @Test
    public void testYearOfCentury() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.yearOfCentury());
    }

    @Test
    public void testYearOfEra() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.yearOfEra());
    }

    @Test
    public void testYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.year());
    }

    @Test
    public void testWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.weekyear());
    }

    @Test
    public void testMonthOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.monthOfYear());
    }

    @Test
    public void testWeekOfWeekyear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.weekOfWeekyear());
    }

    @Test
    public void testDayOfYear() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.dayOfYear());
    }

    @Test
    public void testDayOfMonth() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.dayOfMonth());
    }

    @Test
    public void testDayOfWeek() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertNotNull(date.dayOfWeek());
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @Test
    public void testToString() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals("2020-06-15", date.toString());
    }

    @Test
    public void testToString_pattern() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals("2020/06/15", date.toString("yyyy/MM/dd"));
    }

    @Test
    public void testToString_nullPattern() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals("2020-06-15", date.toString(null));
    }

    @Test
    public void testToString_pattern_locale() {
        LocalDate date = new LocalDate(2020, 6, 15);
        String str = date.toString("yyyy/MM/dd", Locale.US);
        assertEquals("2020/06/15", str);
    }

    @Test
    public void testToString_nullPattern_locale() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals("2020-06-15", date.toString(null, Locale.US));
    }

    //-----------------------------------------------------------------------
    // Property tests
    //-----------------------------------------------------------------------
    @Test
    public void testPropertyGetField() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        assertNotNull(prop.getField());
    }

    @Test
    public void testPropertyGetMillis() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        assertEquals(date.getLocalMillis(), prop.getMillis());
    }

    @Test
    public void testPropertyGetChronology() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        assertEquals(date.getChronology(), prop.getChronology());
    }

    @Test
    public void testPropertyGetLocalDate() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        assertEquals(date, prop.getLocalDate());
    }

    @Test
    public void testPropertyAddToCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        LocalDate newDate = prop.addToCopy(1);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testPropertyAddWrapFieldToCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.addWrapFieldToCopy(1);
        assertEquals(7, newDate.getMonthOfYear());
    }

    @Test
    public void testPropertySetCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        LocalDate newDate = prop.setCopy(2021);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testPropertySetCopy_text() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        LocalDate newDate = prop.setCopy("2021");
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testPropertySetCopy_text_locale() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        LocalDate newDate = prop.setCopy("2021", Locale.US);
        assertEquals(2021, newDate.getYear());
    }

    @Test
    public void testPropertyWithMaximumValue() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.dayOfMonth();
        LocalDate newDate = prop.withMaximumValue();
        assertEquals(30, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyWithMinimumValue() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.dayOfMonth();
        LocalDate newDate = prop.withMinimumValue();
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyRoundFloorCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.roundFloorCopy();
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyRoundCeilingCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.roundCeilingCopy();
        assertEquals(7, newDate.getMonthOfYear());
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyRoundHalfFloorCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.roundHalfFloorCopy();
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyRoundHalfCeilingCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.roundHalfCeilingCopy();
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(1, newDate.getDayOfMonth());
    }

    @Test
    public void testPropertyRoundHalfEvenCopy() {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.monthOfYear();
        LocalDate newDate = prop.roundHalfEvenCopy();
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(1, newDate.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // Edge cases
    //-----------------------------------------------------------------------
    @Test
    public void testLeapYear() {
        LocalDate date = new LocalDate(2020, 2, 29);
        assertEquals(2020, date.getYear());
        assertEquals(2, date.getMonthOfYear());
        assertEquals(29, date.getDayOfMonth());
    }

    @Test(expected = org.joda.time.IllegalFieldValueException.class)
    public void testInvalidDate() {
        new LocalDate(2020, 2, 30);
    }

    @Test
    public void testPlusMonths_endOfMonth() {
        LocalDate date = new LocalDate(2020, 1, 31);
        LocalDate newDate = date.plusMonths(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(2, newDate.getMonthOfYear());
        assertEquals(29, newDate.getDayOfMonth());
    }

    @Test
    public void testMinusMonths_endOfMonth() {
        LocalDate date = new LocalDate(2020, 3, 31);
        LocalDate newDate = date.minusMonths(1);
        assertEquals(2020, newDate.getYear());
        assertEquals(2, newDate.getMonthOfYear());
        assertEquals(29, newDate.getDayOfMonth());
    }

    @Test
    public void testWithDifferentChronology() {
        Chronology chrono = GregorianChronology.getInstance();
        LocalDate date = new LocalDate(2020, 6, 15, chrono);
        assertEquals(chrono.withUTC(), date.getChronology());
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testReadResolve() {
        LocalDate date = new LocalDate(2020, 6, 15);
        assertEquals(date, date.readResolve());
    }

    @Test
    public void testCompareTo_differentChronology() {
        LocalDate date1 = new LocalDate(2020, 6, 15);
        LocalDate date2 = new LocalDate(2020, 6, 15, GregorianChronology.getInstance());
        assertEquals(0, date1.compareTo(date2));
    }

    @Test
    public void testEquals_differentChronology() {
        LocalDate date1 = new LocalDate(2020, 6, 15);
        LocalDate date2 = new LocalDate(2020, 6, 15, GregorianChronology.getInstance());
        assertFalse(date1.equals(date2));
    }

    @Test
    public void testToDate_DST() {
        // Test around DST transition
        LocalDate date = new LocalDate(2020, 3, 8); // US DST start in 2020
        Date jdkDate = date.toDate();
        assertNotNull(jdkDate);
    }

    @Test
    public void testWithPeriodAdded_unsupportedField() {
        LocalDate date = new LocalDate(2020, 6, 15);
        Period period = Period.hours(24);
        LocalDate newDate = date.withPeriodAdded(period, 1);
        assertEquals(date, newDate);
    }

    @Test
    public void testIsSupported_eras() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DurationFieldType.eras()));
    }

    @Test
    public void testIsSupported_centuries() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DurationFieldType.centuries()));
    }

    @Test
    public void testIsSupported_weekyears() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DurationFieldType.weekyears()));
    }

    @Test
    public void testIsSupported_weeks() {
        LocalDate date = new LocalDate();
        assertTrue(date.isSupported(DurationFieldType.weeks()));
    }

    @Test
    public void testPropertySerialization() throws Exception {
        LocalDate date = new LocalDate(2020, 6, 15);
        LocalDate.Property prop = date.year();
        // Just ensure no exception during serialization/deserialization
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        LocalDate.Property prop2 = (LocalDate.Property) ois.readObject();
        assertEquals(prop.get(), prop2.get());
    }

    // Helper class for withFields test
    private static class YearMonthDay implements ReadablePartial {
        private final int year;
        private final int month;
        private final int day;
        
        YearMonthDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
        
        public int size() { return 3; }
        public int getValue(int index) {
            switch (index) {
                case 0: return year;
                case 1: return month;
                case 2: return day;
                default: throw new IndexOutOfBoundsException();
            }
        }
        public DateTimeField getField(int index, Chronology chrono) {
            switch (index) {
                case 0: return chrono.year();
                case 1: return chrono.monthOfYear();
                case 2: return chrono.dayOfMonth();
                default: throw new IndexOutOfBoundsException();
            }
        }
        public DateTimeFieldType getFieldType(int index) {
            switch (index) {
                case 0: return DateTimeFieldType.year();
                case 1: return DateTimeFieldType.monthOfYear();
                case 2: return DateTimeFieldType.dayOfMonth();
                default: throw new IndexOutOfBoundsException();
            }
        }
        public Chronology getChronology() { return ISOChronology.getInstanceUTC(); }
        public int get(DateTimeFieldType field) { return 0; }
        public boolean isSupported(DateTimeFieldType field) { return true; }
        public DateTime toDateTime(ReadableInstant baseInstant) { return null; }
        public int compareTo(ReadablePartial partial) { return 0; }
    }
}
