package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Locale;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MutableDateTimeTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone UTC = DateTimeZone.UTC;
    
    private DateTimeZone originalZone;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        DateTimeZone.setDefault(UTC);
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testFactoryMethods() {
        MutableDateTime now1 = MutableDateTime.now();
        Assert.assertNotNull(now1);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), now1.getChronology());

        MutableDateTime nowParis = MutableDateTime.now(PARIS);
        Assert.assertNotNull(nowParis);
        Assert.assertEquals(ISOChronology.getInstance(PARIS), nowParis.getChronology());

        MutableDateTime nowBuddhist = MutableDateTime.now(BuddhistChronology.getInstance(PARIS));
        Assert.assertNotNull(nowBuddhist);
        Assert.assertEquals(BuddhistChronology.getInstance(PARIS), nowBuddhist.getChronology());

        try {
            MutableDateTime.now((DateTimeZone) null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            Assert.assertEquals("Zone must not be null", e.getMessage());
        }

        try {
            MutableDateTime.now((Chronology) null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            Assert.assertEquals("Chronology must not be null", e.getMessage());
        }
    }

    @Test
    public void testParse() {
        MutableDateTime parsed = MutableDateTime.parse("2023-05-15T12:30:45.123Z");
        Assert.assertEquals(2023, parsed.getYear());
        Assert.assertEquals(5, parsed.getMonthOfYear());
        Assert.assertEquals(15, parsed.getDayOfMonth());
        Assert.assertEquals(12, parsed.getHourOfDay());
        Assert.assertEquals(30, parsed.getMinuteOfHour());
        Assert.assertEquals(45, parsed.getSecondOfMinute());
        Assert.assertEquals(123, parsed.getMillisOfSecond());

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZone(UTC);
        MutableDateTime customParsed = MutableDateTime.parse("2021/11/04 10:15:30", formatter);
        Assert.assertEquals(2021, customParsed.getYear());
        Assert.assertEquals(11, customParsed.getMonthOfYear());
        Assert.assertEquals(4, customParsed.getDayOfMonth());
        Assert.assertEquals(10, customParsed.getHourOfDay());
        Assert.assertEquals(15, customParsed.getMinuteOfHour());
        Assert.assertEquals(30, customParsed.getSecondOfMinute());
    }

    @Test
    public void testConstructors() {
        MutableDateTime mdt1 = new MutableDateTime();
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt1.getChronology());

        MutableDateTime mdt2 = new MutableDateTime(PARIS);
        Assert.assertEquals(ISOChronology.getInstance(PARIS), mdt2.getChronology());

        MutableDateTime mdt3 = new MutableDateTime((DateTimeZone) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt3.getChronology());

        MutableDateTime mdt4 = new MutableDateTime(BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), mdt4.getChronology());

        MutableDateTime mdt5 = new MutableDateTime((Chronology) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt5.getChronology());

        MutableDateTime mdt6 = new MutableDateTime(123456789L);
        Assert.assertEquals(123456789L, mdt6.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt6.getChronology());

        MutableDateTime mdt7 = new MutableDateTime(123456789L, PARIS);
        Assert.assertEquals(123456789L, mdt7.getMillis());
        Assert.assertEquals(ISOChronology.getInstance(PARIS), mdt7.getChronology());

        MutableDateTime mdt8 = new MutableDateTime(123456789L, (DateTimeZone) null);
        Assert.assertEquals(123456789L, mdt8.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt8.getChronology());

        MutableDateTime mdt9 = new MutableDateTime(123456789L, BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(123456789L, mdt9.getMillis());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), mdt9.getChronology());

        MutableDateTime mdt10 = new MutableDateTime(123456789L, (Chronology) null);
        Assert.assertEquals(123456789L, mdt10.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt10.getChronology());

        MutableDateTime mdt11 = new MutableDateTime(new Date(1000L));
        Assert.assertEquals(1000L, mdt11.getMillis());

        MutableDateTime mdt12 = new MutableDateTime((Object) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt12.getChronology());

        MutableDateTime mdt13 = new MutableDateTime(new Date(2000L), PARIS);
        Assert.assertEquals(2000L, mdt13.getMillis());
        Assert.assertEquals(ISOChronology.getInstance(PARIS), mdt13.getChronology());

        MutableDateTime mdt14 = new MutableDateTime(new Date(2000L), (DateTimeZone) null);
        Assert.assertEquals(2000L, mdt14.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt14.getChronology());

        MutableDateTime mdt15 = new MutableDateTime((Object) null, PARIS);
        Assert.assertEquals(ISOChronology.getInstance(PARIS), mdt15.getChronology());

        MutableDateTime mdt16 = new MutableDateTime(new Date(3000L), BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(3000L, mdt16.getMillis());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), mdt16.getChronology());

        MutableDateTime mdt17 = new MutableDateTime(new Date(3000L), (Chronology) null);
        Assert.assertEquals(3000L, mdt17.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt17.getChronology());

        MutableDateTime mdt18 = new MutableDateTime((Object) null, BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), mdt18.getChronology());

        MutableDateTime mdt19 = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400);
        Assert.assertEquals(2020, mdt19.getYear());
        Assert.assertEquals(5, mdt19.getMonthOfYear());
        Assert.assertEquals(12, mdt19.getDayOfMonth());
        Assert.assertEquals(10, mdt19.getHourOfDay());
        Assert.assertEquals(20, mdt19.getMinuteOfHour());
        Assert.assertEquals(30, mdt19.getSecondOfMinute());
        Assert.assertEquals(400, mdt19.getMillisOfSecond());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt19.getChronology());

        MutableDateTime mdt20 = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, PARIS);
        Assert.assertEquals(ISOChronology.getInstance(PARIS), mdt20.getChronology());

        MutableDateTime mdt21 = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, (DateTimeZone) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt21.getChronology());

        MutableDateTime mdt22 = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), mdt22.getChronology());

        MutableDateTime mdt23 = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, (Chronology) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt23.getChronology());
    }

    @Test
    public void testRounding() {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 450, UTC);
        Assert.assertNull(mdt.getRoundingField());
        Assert.assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());

        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour());
        Assert.assertEquals(ISOChronology.getInstanceUTC().minuteOfHour(), mdt.getRoundingField());
        Assert.assertEquals(MutableDateTime.ROUND_FLOOR, mdt.getRoundingMode());
        Assert.assertEquals(0, mdt.getSecondOfMinute());
        Assert.assertEquals(0, mdt.getMillisOfSecond());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 450, UTC);
        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_CEILING);
        Assert.assertEquals(MutableDateTime.ROUND_CEILING, mdt.getRoundingMode());
        Assert.assertEquals(21, mdt.getMinuteOfHour());
        Assert.assertEquals(0, mdt.getSecondOfMinute());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 0, UTC);
        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_HALF_FLOOR);
        Assert.assertEquals(MutableDateTime.ROUND_HALF_FLOOR, mdt.getRoundingMode());
        Assert.assertEquals(20, mdt.getMinuteOfHour());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 0, UTC);
        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_HALF_CEILING);
        Assert.assertEquals(MutableDateTime.ROUND_HALF_CEILING, mdt.getRoundingMode());
        Assert.assertEquals(21, mdt.getMinuteOfHour());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 0, UTC);
        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_HALF_EVEN);
        Assert.assertEquals(MutableDateTime.ROUND_HALF_EVEN, mdt.getRoundingMode());
        Assert.assertEquals(20, mdt.getMinuteOfHour());

        mdt = new MutableDateTime(2020, 5, 12, 10, 21, 30, 0, UTC);
        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_HALF_EVEN);
        Assert.assertEquals(22, mdt.getMinuteOfHour());

        mdt.setRounding(null, MutableDateTime.ROUND_FLOOR);
        Assert.assertNull(mdt.getRoundingField());
        Assert.assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());

        mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), MutableDateTime.ROUND_NONE);
        Assert.assertNull(mdt.getRoundingField());
        Assert.assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());

        try {
            mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            mdt.setRounding(ISOChronology.getInstanceUTC().minuteOfHour(), 6);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetMillisAndAdd() {
        MutableDateTime mdt = new MutableDateTime(0L, UTC);
        mdt.setMillis(1000L);
        Assert.assertEquals(1000L, mdt.getMillis());

        mdt.setMillis(new DateTime(2000L, UTC));
        Assert.assertEquals(2000L, mdt.getMillis());

        mdt.setMillis((ReadableInstant) null);
        Assert.assertTrue(Math.abs(System.currentTimeMillis() - mdt.getMillis()) < 5000L);

        mdt.setMillis(1000L);
        mdt.add(500L);
        Assert.assertEquals(1500L, mdt.getMillis());

        mdt.add(new Duration(200L));
        Assert.assertEquals(1700L, mdt.getMillis());

        mdt.add((ReadableDuration) null);
        Assert.assertEquals(1700L, mdt.getMillis());

        mdt.add(new Duration(100L), 3);
        Assert.assertEquals(2000L, mdt.getMillis());

        mdt.add((ReadableDuration) null, 3);
        Assert.assertEquals(2000L, mdt.getMillis());

        mdt.add(Period.days(1));
        Assert.assertEquals(2000L + 86400000L, mdt.getMillis());

        mdt.add((ReadablePeriod) null);
        Assert.assertEquals(2000L + 86400000L, mdt.getMillis());

        mdt.add(Period.days(2), 2);
        Assert.assertEquals(2000L + 5 * 86400000L, mdt.getMillis());

        mdt.add((ReadablePeriod) null, 2);
        Assert.assertEquals(2000L + 5 * 86400000L, mdt.getMillis());

        mdt.add(DurationFieldType.days(), 1);
        Assert.assertEquals(2000L + 6 * 86400000L, mdt.getMillis());

        try {
            mdt.add((DurationFieldType) null, 1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetZoneAndZoneRetainFields() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 12, 0, 0, 0, UTC);
        
        mdt.setZone(UTC);
        Assert.assertEquals(UTC, mdt.getZone());
        Assert.assertEquals(12, mdt.getHourOfDay());

        mdt.setZone(PARIS);
        Assert.assertEquals(PARIS, mdt.getZone());
        Assert.assertEquals(13, mdt.getHourOfDay());

        mdt.setZone((DateTimeZone) null);
        Assert.assertEquals(UTC, mdt.getZone());

        mdt.setZoneRetainFields(UTC);
        Assert.assertEquals(UTC, mdt.getZone());
        Assert.assertEquals(12, mdt.getHourOfDay());

        mdt.setZoneRetainFields(PARIS);
        Assert.assertEquals(PARIS, mdt.getZone());
        Assert.assertEquals(12, mdt.getHourOfDay());

        mdt.setZoneRetainFields((DateTimeZone) null);
        Assert.assertEquals(UTC, mdt.getZone());
        Assert.assertEquals(12, mdt.getHourOfDay());
    }

    @Test
    public void testSetAndAddSpecificFields() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, UTC);

        mdt.set(DateTimeFieldType.year(), 2021);
        Assert.assertEquals(2021, mdt.getYear());

        try {
            mdt.set(null, 2021);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        mdt.setYear(2022);
        Assert.assertEquals(2022, mdt.getYear());
        mdt.addYears(1);
        Assert.assertEquals(2023, mdt.getYear());

        mdt.setWeekyear(2024);
        Assert.assertEquals(2024, mdt.getWeekyear());
        mdt.addWeekyears(-1);
        Assert.assertEquals(2023, mdt.getWeekyear());

        mdt.setMonthOfYear(6);
        Assert.assertEquals(6, mdt.getMonthOfYear());
        mdt.addMonths(2);
        Assert.assertEquals(8, mdt.getMonthOfYear());

        mdt.setWeekOfWeekyear(10);
        Assert.assertEquals(10, mdt.getWeekOfWeekyear());
        mdt.addWeeks(2);
        Assert.assertEquals(12, mdt.getWeekOfWeekyear());

        mdt.setDayOfYear(100);
        Assert.assertEquals(100, mdt.getDayOfYear());

        mdt.setDayOfMonth(15);
        Assert.assertEquals(15, mdt.getDayOfMonth());

        mdt.setDayOfWeek(3);
        Assert.assertEquals(3, mdt.getDayOfWeek());

        mdt.addDays(5);
        Assert.assertEquals(8, mdt.getDayOfWeek());

        mdt.setHourOfDay(14);
        Assert.assertEquals(14, mdt.getHourOfDay());
        mdt.addHours(2);
        Assert.assertEquals(16, mdt.getHourOfDay());

        mdt.setMinuteOfDay(120);
        Assert.assertEquals(2, mdt.getHourOfDay());
        Assert.assertEquals(0, mdt.getMinuteOfHour());

        mdt.setMinuteOfHour(45);
        Assert.assertEquals(45, mdt.getMinuteOfHour());
        mdt.addMinutes(10);
        Assert.assertEquals(55, mdt.getMinuteOfHour());

        mdt.setSecondOfDay(3600 + 60 + 5);
        Assert.assertEquals(1, mdt.getHourOfDay());
        Assert.assertEquals(1, mdt.getMinuteOfHour());
        Assert.assertEquals(5, mdt.getSecondOfMinute());

        mdt.setSecondOfMinute(25);
        Assert.assertEquals(25, mdt.getSecondOfMinute());
        mdt.addSeconds(5);
        Assert.assertEquals(30, mdt.getSecondOfMinute());

        mdt.setMillisOfDay(5000);
        Assert.assertEquals(0, mdt.getHourOfDay());
        Assert.assertEquals(0, mdt.getMinuteOfHour());
        Assert.assertEquals(5, mdt.getSecondOfMinute());
        Assert.assertEquals(0, mdt.getMillisOfSecond());

        mdt.setMillisOfSecond(789);
        Assert.assertEquals(789, mdt.getMillisOfSecond());
        mdt.addMillis(111);
        Assert.assertEquals(900, mdt.getMillisOfSecond());
    }

    @Test
    public void testSetDateAndTimeMethods() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 14, 30, 45, 500, UTC);

        MutableDateTime otherDate = new MutableDateTime(2021, 5, 10, 0, 0, 0, 0, UTC);
        mdt.setDate(otherDate.getMillis());
        Assert.assertEquals(2021, mdt.getYear());
        Assert.assertEquals(5, mdt.getMonthOfYear());
        Assert.assertEquals(10, mdt.getDayOfMonth());
        Assert.assertEquals(14, mdt.getHourOfDay());
        Assert.assertEquals(30, mdt.getMinuteOfHour());
        Assert.assertEquals(45, mdt.getSecondOfMinute());
        Assert.assertEquals(500, mdt.getMillisOfSecond());

        DateTime dtWithZone = new DateTime(2022, 6, 20, 8, 0, 0, 0, PARIS);
        mdt.setDate(dtWithZone);
        Assert.assertEquals(2022, mdt.getYear());
        Assert.assertEquals(6, mdt.getMonthOfYear());
        Assert.assertEquals(20, mdt.getDayOfMonth());
        Assert.assertEquals(14, mdt.getHourOfDay());

        Instant instantOnly = new Instant(0L); // 1970-01-01 UTC
        mdt.setDate(instantOnly);
        Assert.assertEquals(1970, mdt.getYear());
        Assert.assertEquals(1, mdt.getMonthOfYear());
        Assert.assertEquals(1, mdt.getDayOfMonth());
        Assert.assertEquals(14, mdt.getHourOfDay());

        mdt.setDate(2025, 12, 25);
        Assert.assertEquals(2025, mdt.getYear());
        Assert.assertEquals(12, mdt.getMonthOfYear());
        Assert.assertEquals(25, mdt.getDayOfMonth());
        Assert.assertEquals(14, mdt.getHourOfDay());

        MutableDateTime otherTime = new MutableDateTime(1990, 1, 1, 22, 11, 33, 444, UTC);
        mdt.setTime(otherTime.getMillis());
        Assert.assertEquals(2025, mdt.getYear());
        Assert.assertEquals(12, mdt.getMonthOfYear());
        Assert.assertEquals(25, mdt.getDayOfMonth());
        Assert.assertEquals(22, mdt.getHourOfDay());
        Assert.assertEquals(11, mdt.getMinuteOfHour());
        Assert.assertEquals(33, mdt.getSecondOfMinute());
        Assert.assertEquals(444, mdt.getMillisOfSecond());

        DateTime dtTimeWithZone = new DateTime(2010, 2, 2, 18, 20, 22, 123, PARIS);
        mdt.setTime(dtTimeWithZone);
        Assert.assertEquals(18, mdt.getHourOfDay());
        Assert.assertEquals(20, mdt.getMinuteOfHour());
        Assert.assertEquals(22, mdt.getSecondOfMinute());
        Assert.assertEquals(123, mdt.getMillisOfSecond());

        mdt.setTime(5, 6, 7, 8);
        Assert.assertEquals(5, mdt.getHourOfDay());
        Assert.assertEquals(6, mdt.getMinuteOfHour());
        Assert.assertEquals(7, mdt.getSecondOfMinute());
        Assert.assertEquals(8, mdt.getMillisOfSecond());

        mdt.setDateTime(2030, 7, 8, 9, 10, 11, 12);
        Assert.assertEquals(2030, mdt.getYear());
        Assert.assertEquals(7, mdt.getMonthOfYear());
        Assert.assertEquals(8, mdt.getDayOfMonth());
        Assert.assertEquals(9, mdt.getHourOfDay());
        Assert.assertEquals(10, mdt.getMinuteOfHour());
        Assert.assertEquals(11, mdt.getSecondOfMinute());
        Assert.assertEquals(12, mdt.getMillisOfSecond());
    }

    @Test
    public void testChronologyChange() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, UTC);
        mdt.setChronology(GregorianChronology.getInstanceUTC());
        Assert.assertEquals(GregorianChronology.getInstanceUTC(), mdt.getChronology());

        mdt.setChronology(null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), mdt.getChronology());
    }

    @Test
    public void testCloneAndCopy() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 10, 20, 30, 40, PARIS);
        MutableDateTime copy = mdt.copy();
        Assert.assertEquals(mdt, copy);
        Assert.assertNotSame(mdt, copy);

        Object clone = mdt.clone();
        Assert.assertEquals(mdt, clone);
        Assert.assertNotSame(mdt, clone);
    }

    @Test
    public void testToString() {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, UTC);
        Assert.assertEquals("2020-05-12T10:20:30.400Z", mdt.toString());
    }

    @Test
    public void testPropertiesAndUnsupportedField() {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, UTC);

        Assert.assertNotNull(mdt.era());
        Assert.assertNotNull(mdt.centuryOfEra());
        Assert.assertNotNull(mdt.yearOfCentury());
        Assert.assertNotNull(mdt.yearOfEra());
        Assert.assertNotNull(mdt.year());
        Assert.assertNotNull(mdt.weekyear());
        Assert.assertNotNull(mdt.monthOfYear());
        Assert.assertNotNull(mdt.weekOfWeekyear());
        Assert.assertNotNull(mdt.dayOfYear());
        Assert.assertNotNull(mdt.dayOfMonth());
        Assert.assertNotNull(mdt.dayOfWeek());
        Assert.assertNotNull(mdt.hourOfDay());
        Assert.assertNotNull(mdt.minuteOfDay());
        Assert.assertNotNull(mdt.minuteOfHour());
        Assert.assertNotNull(mdt.secondOfDay());
        Assert.assertNotNull(mdt.secondOfMinute());
        Assert.assertNotNull(mdt.millisOfDay());
        Assert.assertNotNull(mdt.millisOfSecond());

        MutableDateTime.Property p = mdt.property(DateTimeFieldType.monthOfYear());
        Assert.assertEquals(5, p.get());
        Assert.assertEquals(mdt, p.getMutableDateTime());
        Assert.assertEquals(mdt.getMillis(), p.getMillis());
        Assert.assertEquals(mdt.getChronology(), p.getChronology());
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p.getField().getType());

        try {
            mdt.property(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            mdt.property(DateTimeFieldType.era().getField(ISOChronology.getInstanceUTC()).getType());
        } catch (IllegalArgumentException e) {
            Assert.fail("Era should be supported in ISOChronology");
        }
    }

    @Test
    public void testPropertyManipulations() {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, UTC);

        mdt.monthOfYear().add(2);
        Assert.assertEquals(7, mdt.getMonthOfYear());

        mdt.monthOfYear().add(2L);
        Assert.assertEquals(9, mdt.getMonthOfYear());

        mdt.monthOfYear().addWrapField(4);
        Assert.assertEquals(1, mdt.getMonthOfYear());

        mdt.monthOfYear().set(11);
        Assert.assertEquals(11, mdt.getMonthOfYear());

        mdt.monthOfYear().set("December", Locale.UK);
        Assert.assertEquals(12, mdt.getMonthOfYear());

        mdt.monthOfYear().set("January");
        Assert.assertEquals(1, mdt.getMonthOfYear());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, UTC);
        mdt.secondOfMinute().roundFloor();
        Assert.assertEquals(0, mdt.getMillisOfSecond());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, UTC);
        mdt.secondOfMinute().roundCeiling();
        Assert.assertEquals(31, mdt.getSecondOfMinute());
        Assert.assertEquals(0, mdt.getMillisOfSecond());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 500, UTC);
        mdt.secondOfMinute().roundHalfFloor();
        Assert.assertEquals(30, mdt.getSecondOfMinute());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 500, UTC);
        mdt.secondOfMinute().roundHalfCeiling();
        Assert.assertEquals(31, mdt.getSecondOfMinute());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 500, UTC);
        mdt.secondOfMinute().roundHalfEven();
        Assert.assertEquals(30, mdt.getSecondOfMinute());

        mdt = new MutableDateTime(2020, 5, 12, 10, 20, 31, 500, UTC);
        mdt.secondOfMinute().roundHalfEven();
        Assert.assertEquals(32, mdt.getSecondOfMinute());
    }

    @Test
    public void testSerialization() throws Exception {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mdt);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableDateTime deserialized = (MutableDateTime) ois.readObject();
        ois.close();

        Assert.assertEquals(mdt, deserialized);
        Assert.assertEquals(mdt.getChronology(), deserialized.getChronology());
    }

    @Test
    public void testPropertySerialization() throws Exception {
        MutableDateTime mdt = new MutableDateTime(2020, 5, 12, 10, 20, 30, 400, PARIS);
        MutableDateTime.Property prop = mdt.monthOfYear();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableDateTime.Property deserializedProp = (MutableDateTime.Property) ois.readObject();
        ois.close();

        Assert.assertEquals(prop.get(), deserializedProp.get());
        Assert.assertEquals(prop.getField().getType(), deserializedProp.getField().getType());
        Assert.assertEquals(prop.getMutableDateTime().getMillis(), deserializedProp.getMutableDateTime().getMillis());
        Assert.assertEquals(prop.getMutableDateTime().getChronology(), deserializedProp.getMutableDateTime().getChronology());
    }
}
