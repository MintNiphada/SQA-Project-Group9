package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.junit.Assert;
import org.junit.Test;

public class GJChronologyTest {

    @Test
    public void testFactoryMethodsAndCaching() {
        GJChronology defaultGJ = GJChronology.getInstance();
        Assert.assertNotNull(defaultGJ);
        Assert.assertEquals(DateTimeZone.getDefault(), defaultGJ.getZone());
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, defaultGJ.getGregorianCutover());
        Assert.assertEquals(4, defaultGJ.getMinimumDaysInFirstWeek());

        GJChronology utcGJ = GJChronology.getInstanceUTC();
        Assert.assertEquals(DateTimeZone.UTC, utcGJ.getZone());
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, utcGJ.getGregorianCutover());

        GJChronology londonGJ = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));
        Assert.assertEquals(DateTimeZone.forID("Europe/London"), londonGJ.getZone());

        Instant customCutover = new Instant(0L);
        GJChronology customGJ = GJChronology.getInstance(DateTimeZone.UTC, customCutover);
        Assert.assertEquals(customCutover, customGJ.getGregorianCutover());
        Assert.assertEquals(4, customGJ.getMinimumDaysInFirstWeek());

        GJChronology customGJ2 = GJChronology.getInstance(DateTimeZone.UTC, customCutover, 3);
        Assert.assertEquals(3, customGJ2.getMinimumDaysInFirstWeek());

        GJChronology cachedGJ = GJChronology.getInstance(DateTimeZone.UTC, customCutover, 3);
        Assert.assertSame(customGJ2, cachedGJ);

        GJChronology defaultCutoverMillis = GJChronology.getInstance(
                DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER.getMillis(), 4);
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, defaultCutoverMillis.getGregorianCutover());

        GJChronology customCutoverMillis = GJChronology.getInstance(DateTimeZone.UTC, 1000L, 5);
        Assert.assertEquals(new Instant(1000L), customCutoverMillis.getGregorianCutover());
        Assert.assertEquals(5, customCutoverMillis.getMinimumDaysInFirstWeek());

        GJChronology nullZone = GJChronology.getInstance(null);
        Assert.assertEquals(DateTimeZone.getDefault(), nullZone.getZone());
    }

    @Test
    public void testWithZoneAndWithUTC() {
        GJChronology utcChrono = GJChronology.getInstanceUTC();
        Assert.assertSame(utcChrono, utcChrono.withUTC());
        Assert.assertSame(utcChrono, utcChrono.withZone(DateTimeZone.UTC));

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        Chronology parisChrono = utcChrono.withZone(paris);
        Assert.assertEquals(paris, parisChrono.getZone());

        Chronology defaultZoneChrono = parisChrono.withZone(null);
        Assert.assertEquals(DateTimeZone.getDefault(), defaultZoneChrono.getZone());
    }

    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstanceUTC();
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology chrono3 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L), 4);
        GJChronology chrono4 = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));

        Assert.assertEquals(chrono1, chrono2);
        Assert.assertEquals(chrono1.hashCode(), chrono2.hashCode());

        Assert.assertNotEquals(chrono1, chrono3);
        Assert.assertNotEquals(chrono1, chrono4);
        Assert.assertNotEquals(chrono1, null);
        Assert.assertNotEquals(chrono1, new Object());
    }

    @Test
    public void testToString() {
        GJChronology defaultGJ = GJChronology.getInstanceUTC();
        Assert.assertEquals("GJChronology[UTC]", defaultGJ.toString());

        Instant cutoverDateOnly = new Instant(0L); // 1970-01-01
        GJChronology custom1 = GJChronology.getInstance(DateTimeZone.UTC, cutoverDateOnly, 4);
        Assert.assertEquals("GJChronology[UTC,cutover=1970-01-01]", custom1.toString());

        Instant cutoverWithTime = new Instant(3600000L); // 1970-01-01T01:00:00.000Z
        GJChronology custom2 = GJChronology.getInstance(DateTimeZone.UTC, cutoverWithTime, 3);
        Assert.assertEquals("GJChronology[UTC,cutover=1970-01-01T01:00:00.000Z,mdfw=3]", custom2.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        GJChronology original = GJChronology.getInstance(DateTimeZone.forOffsetHours(2), new Instant(123456789L), 5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        GJChronology deserialized = (GJChronology) ois.readObject();
        ois.close();

        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.getZone(), deserialized.getZone());
        Assert.assertEquals(original.getGregorianCutover(), deserialized.getGregorianCutover());
        Assert.assertEquals(original.getMinimumDaysInFirstWeek(), deserialized.getMinimumDaysInFirstWeek());
    }

    @Test
    public void testGetDateTimeMillis4Args() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        
        // Julian date: 1582-10-04
        long julianMillis = chrono.getDateTimeMillis(1582, 10, 4, 100);
        Assert.assertEquals(1582, chrono.year().get(julianMillis));
        Assert.assertEquals(10, chrono.monthOfYear().get(julianMillis));
        Assert.assertEquals(4, chrono.dayOfMonth().get(julianMillis));
        Assert.assertEquals(100, chrono.millisOfDay().get(julianMillis));

        // Gregorian date: 1582-10-15
        long gregMillis = chrono.getDateTimeMillis(1582, 10, 15, 200);
        Assert.assertEquals(1582, chrono.year().get(gregMillis));
        Assert.assertEquals(10, chrono.monthOfYear().get(gregMillis));
        Assert.assertEquals(15, chrono.dayOfMonth().get(gregMillis));
        Assert.assertEquals(200, chrono.millisOfDay().get(gregMillis));

        // Zoned instance test
        GJChronology zoned = GJChronology.getInstance(DateTimeZone.forOffsetHours(1));
        long zonedMillis = zoned.getDateTimeMillis(2020, 1, 1, 500);
        Assert.assertEquals(2020, zoned.year().get(zonedMillis));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis4ArgsGap() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-05 was cut from calendar
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    @Test
    public void testGetDateTimeMillis7Args() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        
        long julianMillis = chrono.getDateTimeMillis(1582, 10, 4, 12, 30, 40, 500);
        Assert.assertEquals(1582, chrono.year().get(julianMillis));
        Assert.assertEquals(10, chrono.monthOfYear().get(julianMillis));
        Assert.assertEquals(4, chrono.dayOfMonth().get(julianMillis));
        Assert.assertEquals(12, chrono.hourOfDay().get(julianMillis));

        long gregMillis = chrono.getDateTimeMillis(1582, 10, 15, 8, 20, 10, 100);
        Assert.assertEquals(1582, chrono.year().get(gregMillis));
        Assert.assertEquals(10, chrono.monthOfYear().get(gregMillis));
        Assert.assertEquals(15, chrono.dayOfMonth().get(gregMillis));

        GJChronology zoned = GJChronology.getInstance(DateTimeZone.forOffsetHours(1));
        long zonedMillis = zoned.getDateTimeMillis(2020, 1, 1, 12, 0, 0, 0);
        Assert.assertEquals(2020, zoned.year().get(zonedMillis));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis7ArgsGap() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 10, 12, 0, 0, 0);
    }

    @Test
    public void testCutoverFieldsTimeOfDayWhenCutoverAtMidnight() {
        // Cutover with non-zero millisOfDay to execute special time-of-day CutoverFields branch
        Instant cutoverNonMidnight = new Instant(GJChronology.DEFAULT_CUTOVER.getMillis() + 3600000L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutoverNonMidnight);

        long before = cutoverNonMidnight.getMillis() - 1000L;
        long after = cutoverNonMidnight.getMillis() + 1000L;

        Assert.assertNotNull(chrono.millisOfSecond().get(before));
        Assert.assertNotNull(chrono.millisOfSecond().get(after));
        Assert.assertNotNull(chrono.secondOfMinute().get(before));
        Assert.assertNotNull(chrono.secondOfMinute().get(after));
        Assert.assertNotNull(chrono.minuteOfHour().get(before));
        Assert.assertNotNull(chrono.minuteOfHour().get(after));
        Assert.assertNotNull(chrono.hourOfDay().get(before));
        Assert.assertNotNull(chrono.hourOfDay().get(after));
        Assert.assertNotNull(chrono.hourOfHalfday().get(before));
        Assert.assertNotNull(chrono.hourOfHalfday().get(after));
        Assert.assertNotNull(chrono.clockhourOfDay().get(before));
        Assert.assertNotNull(chrono.clockhourOfDay().get(after));
        Assert.assertNotNull(chrono.clockhourOfHalfday().get(before));
        Assert.assertNotNull(chrono.clockhourOfHalfday().get(after));
        Assert.assertNotNull(chrono.halfdayOfDay().get(before));
        Assert.assertNotNull(chrono.halfdayOfDay().get(after));
    }

    @Test
    public void testCutoverFieldMethods() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        long julianInstant = chrono.getDateTimeMillis(1582, 10, 4, 0);
        long gregInstant = chrono.getDateTimeMillis(1582, 10, 15, 0);

        Assert.assertEquals(4, dayOfMonth.get(julianInstant));
        Assert.assertEquals(15, dayOfMonth.get(gregInstant));

        Assert.assertEquals("4", dayOfMonth.getAsText(julianInstant, Locale.ENGLISH));
        Assert.assertEquals("15", dayOfMonth.getAsText(gregInstant, Locale.ENGLISH));
        Assert.assertEquals("4", dayOfMonth.getAsShortText(julianInstant, Locale.ENGLISH));
        Assert.assertEquals("15", dayOfMonth.getAsShortText(gregInstant, Locale.ENGLISH));
        Assert.assertEquals("5", dayOfMonth.getAsText(5, Locale.ENGLISH));
        Assert.assertEquals("5", dayOfMonth.getAsShortText(5, Locale.ENGLISH));

        Assert.assertFalse(dayOfMonth.isLenient());
        Assert.assertNotNull(dayOfMonth.getDurationField());
        Assert.assertNotNull(dayOfMonth.getRangeDurationField());
        Assert.assertNotNull(dayOfMonth.getLeapDurationField());

        // Text lengths
        Assert.assertTrue(chrono.monthOfYear().getMaximumTextLength(Locale.ENGLISH) > 0);
        Assert.assertTrue(chrono.monthOfYear().getMaximumShortTextLength(Locale.ENGLISH) > 0);

        // Leap queries
        DateTimeField year = chrono.year();
        Assert.assertTrue(year.isLeap(chrono.getDateTimeMillis(1500, 1, 1, 0))); // Julian 1500 is leap
        Assert.assertFalse(year.isLeap(chrono.getDateTimeMillis(1700, 1, 1, 0))); // Gregorian 1700 is not leap
        Assert.assertEquals(1, year.getLeapAmount(chrono.getDateTimeMillis(1500, 1, 1, 0)));
        Assert.assertEquals(0, year.getLeapAmount(chrono.getDateTimeMillis(1700, 1, 1, 0)));
    }

    @Test
    public void testCutoverFieldMinMaxAndRounding() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();
        long julianInstant = chrono.getDateTimeMillis(1582, 10, 4, 12, 30, 0, 0);
        long gregInstant = chrono.getDateTimeMillis(1582, 10, 15, 12, 30, 0, 0);

        Assert.assertEquals(1, dayOfMonth.getMinimumValue());
        Assert.assertEquals(31, dayOfMonth.getMaximumValue());
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(julianInstant));
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(gregInstant));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(julianInstant));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(gregInstant));

        YearMonthDay ymd = new YearMonthDay(1582, 10, 4, chrono);
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(ymd));
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(ymd, new int[]{1582, 10, 4}));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(ymd));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(ymd, new int[]{1582, 10, 4}));

        // Rounding
        long floorJulian = dayOfMonth.roundFloor(julianInstant);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0), floorJulian);
        long ceilJulian = dayOfMonth.roundCeiling(julianInstant);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0) + DateTimeConstants.MILLIS_PER_DAY, ceilJulian);

        long floorGreg = dayOfMonth.roundFloor(gregInstant);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 15, 0), floorGreg);
        long ceilGreg = dayOfMonth.roundCeiling(gregInstant);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 15, 0) + DateTimeConstants.MILLIS_PER_DAY, ceilGreg);
    }

    @Test
    public void testCutoverFieldSetCrossingCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        // Julian to Gregorian crossing via set
        long julian = chrono.getDateTimeMillis(1582, 10, 4, 0);
        long setGreg = chrono.year().set(julian, 1583);
        Assert.assertEquals(1583, chrono.year().get(setGreg));

        // Gregorian to Julian crossing via set
        long greg = chrono.getDateTimeMillis(1582, 10, 15, 0);
        long setJulian = chrono.year().set(greg, 1581);
        Assert.assertEquals(1581, chrono.year().get(setJulian));

        // Text set
        long setByText1 = chrono.monthOfYear().set(julian, "11", Locale.ENGLISH);
        Assert.assertEquals(11, chrono.monthOfYear().get(setByText1));

        long setByText2 = chrono.monthOfYear().set(greg, "9", Locale.ENGLISH);
        Assert.assertEquals(9, chrono.monthOfYear().get(setByText2));
    }

    @Test
    public void testImpreciseCutoverFieldOperations() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        // Month addition across cutover
        long beforeCutover = chrono.getDateTimeMillis(1582, 9, 15, 0);
        long afterAddMonth = chrono.monthOfYear().add(beforeCutover, 2);
        Assert.assertEquals(1582, chrono.year().get(afterAddMonth));
        Assert.assertEquals(11, chrono.monthOfYear().get(afterAddMonth));

        long afterCutover = chrono.getDateTimeMillis(1582, 11, 15, 0);
        long afterSubMonth = chrono.monthOfYear().add(afterCutover, -2);
        Assert.assertEquals(1582, chrono.year().get(afterSubMonth));
        Assert.assertEquals(9, chrono.monthOfYear().get(afterSubMonth));

        // Long add
        long afterAddLong = chrono.monthOfYear().add(beforeCutover, 2L);
        Assert.assertEquals(afterAddMonth, afterAddLong);
        long afterSubLong = chrono.monthOfYear().add(afterCutover, -2L);
        Assert.assertEquals(afterSubMonth, afterSubLong);

        // Differences across cutover
        Assert.assertEquals(2, chrono.monthOfYear().getDifference(afterCutover, beforeCutover));
        Assert.assertEquals(-2, chrono.monthOfYear().getDifference(beforeCutover, afterCutover));
        Assert.assertEquals(2L, chrono.monthOfYear().getDifferenceAsLong(afterCutover, beforeCutover));
        Assert.assertEquals(-2L, chrono.monthOfYear().getDifferenceAsLong(beforeCutover, afterCutover));

        // Differences completely on Julian or Gregorian side
        long julian1 = chrono.getDateTimeMillis(1500, 1, 1, 0);
        long julian2 = chrono.getDateTimeMillis(1501, 1, 1, 0);
        Assert.assertEquals(12, chrono.monthOfYear().getDifference(julian2, julian1));
        Assert.assertEquals(12L, chrono.monthOfYear().getDifferenceAsLong(julian2, julian1));

        long greg1 = chrono.getDateTimeMillis(2000, 1, 1, 0);
        long greg2 = chrono.getDateTimeMillis(2001, 1, 1, 0);
        Assert.assertEquals(12, chrono.monthOfYear().getDifference(greg2, greg1));
        Assert.assertEquals(12L, chrono.monthOfYear().getDifferenceAsLong(greg2, greg1));

        // Imprecise field min/max
        Assert.assertEquals(1, chrono.monthOfYear().getMinimumValue(beforeCutover));
        Assert.assertEquals(1, chrono.monthOfYear().getMinimumValue(afterCutover));
        Assert.assertEquals(12, chrono.monthOfYear().getMaximumValue(beforeCutover));
        Assert.assertEquals(12, chrono.monthOfYear().getMaximumValue(afterCutover));
    }

    @Test
    public void testWeekyearConversionsAndAdditions() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        long instant = chrono.getDateTimeMillis(1582, 10, 1, 0);
        long convertedToGreg = chrono.julianToGregorianByWeekyear(instant);
        long convertedBackToJulian = chrono.gregorianToJulianByWeekyear(convertedToGreg);
        Assert.assertEquals(instant, convertedBackToJulian);

        // WeekOfWeekyear field operations
        DateTimeField wowy = chrono.weekOfWeekyear();
        Assert.assertNotNull(wowy.getAsText(instant, Locale.ENGLISH));
        Assert.assertTrue(wowy.get(instant) > 0);

        // Weekyear additions
        long weekyearAdd = chrono.weekyear().add(instant, 1);
        Assert.assertEquals(chrono.weekyear().get(instant) + 1, chrono.weekyear().get(weekyearAdd));

        long weekyearSub = chrono.weekyear().add(weekyearAdd, -1);
        Assert.assertEquals(instant, weekyearSub);
    }

    @Test
    public void testCutoverFieldAddPartial() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        LocalDate ld = new LocalDate(2004, 2, 29, chrono);
        LocalDate result = ld.plusMonths(48);
        Assert.assertEquals(new LocalDate(2008, 2, 29, chrono), result);

        // Test 0 addition
        LocalDate same = ld.plusMonths(0);
        Assert.assertEquals(ld, same);
    }

    @Test
    public void testLinkedDurationField() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DurationField months = chrono.months();

        long instant = chrono.getDateTimeMillis(1582, 9, 15, 0);
        long added = months.add(instant, 2);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 11, 15, 0), added);

        long addedLong = months.add(instant, 2L);
        Assert.assertEquals(added, addedLong);

        Assert.assertEquals(2, months.getDifference(added, instant));
        Assert.assertEquals(2L, months.getDifferenceAsLong(added, instant));
        Assert.assertEquals(DurationFieldType.months(), months.getType());
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testCutoverFieldSetInvalidVerification() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-05 falls into cutover gap and setting it triggers IllegalFieldValueException
        long instant = chrono.getDateTimeMillis(1582, 10, 15, 0);
        chrono.dayOfMonth().set(instant, 5);
    }
}
