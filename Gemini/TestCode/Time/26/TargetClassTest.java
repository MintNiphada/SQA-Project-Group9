package org.joda.time.chrono;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.field.MillisDurationField;
import org.joda.time.field.PreciseDurationDateTimeField;
import org.joda.time.field.UnsupportedDateTimeField;
import org.joda.time.field.UnsupportedDurationField;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZonedChronologyTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    private DateTimeZone originalDefaultZone;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(LONDON);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
    }

    @Test
    public void testGetInstance() {
        Chronology base = ISOChronology.getInstanceUTC();
        ZonedChronology chrono = ZonedChronology.getInstance(base, PARIS);
        Assert.assertEquals(PARIS, chrono.getZone());
        Assert.assertEquals(base, chrono.getBase());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullBase() {
        ZonedChronology.getInstance(null, PARIS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullZone() {
        ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_baseWithUTCReturnsNull() {
        Chronology mockBase = new BaseChronology() {
            private static final long serialVersionUID = 1L;
            public DateTimeZone getZone() { return null; }
            public Chronology withUTC() { return null; }
            public Chronology withZone(DateTimeZone zone) { return this; }
            public String toString() { return "Mock"; }
        };
        ZonedChronology.getInstance(mockBase, PARIS);
    }

    @Test
    public void testUseTimeArithmetic() {
        Assert.assertFalse(ZonedChronology.useTimeArithmetic(null));
        Assert.assertFalse(ZonedChronology.useTimeArithmetic(ISOChronology.getInstanceUTC().days()));
        Assert.assertTrue(ZonedChronology.useTimeArithmetic(ISOChronology.getInstanceUTC().hours()));
        Assert.assertTrue(ZonedChronology.useTimeArithmetic(ISOChronology.getInstanceUTC().minutes()));
        Assert.assertTrue(ZonedChronology.useTimeArithmetic(ISOChronology.getInstanceUTC().seconds()));
        Assert.assertTrue(ZonedChronology.useTimeArithmetic(ISOChronology.getInstanceUTC().millis()));
    }

    @Test
    public void testWithUTC() {
        Chronology base = ISOChronology.getInstanceUTC();
        ZonedChronology chrono = ZonedChronology.getInstance(base, PARIS);
        Assert.assertSame(base, chrono.withUTC());
    }

    @Test
    public void testWithZone() {
        Chronology base = ISOChronology.getInstanceUTC();
        ZonedChronology chrono = ZonedChronology.getInstance(base, PARIS);

        Assert.assertSame(chrono, chrono.withZone(PARIS));
        Assert.assertSame(base, chrono.withZone(UTC));
        Assert.assertEquals(NEW_YORK, chrono.withZone(NEW_YORK).getZone());

        DateTimeZone.setDefault(LONDON);
        Assert.assertEquals(LONDON, chrono.withZone(null).getZone());
    }

    @Test
    public void testGetDateTimeMillis_4args() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        // 2020-01-01 in Paris is UTC+1 (offset = 3600000)
        long millis = chrono.getDateTimeMillis(2020, 1, 1, 3600000); // 01:00:00 Paris local = 00:00:00 UTC
        Assert.assertEquals(ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0), millis);
    }

    @Test
    public void testGetDateTimeMillis_7args() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        long millis = chrono.getDateTimeMillis(2020, 1, 1, 1, 0, 0, 0); // 01:00:00 Paris local = 00:00:00 UTC
        Assert.assertEquals(ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0), millis);
    }

    @Test
    public void testGetDateTimeMillis_instant_4args() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        long baseInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0);
        long millis = chrono.getDateTimeMillis(baseInstant, 2, 30, 45, 500);
        long expected = chrono.getDateTimeMillis(2020, 1, 1, 2, 30, 45, 500);
        Assert.assertEquals(expected, millis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_transitionGap() {
        // America/New_York DST gap on 2020-03-08 between 02:00:00 and 02:59:59.999
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        chrono.getDateTimeMillis(2020, 3, 8, 2, 30, 0, 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        Chronology baseIso = ISOChronology.getInstanceUTC();
        Chronology baseGj = GJChronology.getInstanceUTC();

        ZonedChronology chrono1 = ZonedChronology.getInstance(baseIso, PARIS);
        ZonedChronology chrono2 = ZonedChronology.getInstance(baseIso, PARIS);
        ZonedChronology chrono3 = ZonedChronology.getInstance(baseIso, LONDON);
        ZonedChronology chrono4 = ZonedChronology.getInstance(baseGj, PARIS);

        Assert.assertTrue(chrono1.equals(chrono1));
        Assert.assertTrue(chrono1.equals(chrono2));
        Assert.assertEquals(chrono1.hashCode(), chrono2.hashCode());

        Assert.assertFalse(chrono1.equals(chrono3));
        Assert.assertFalse(chrono1.equals(chrono4));
        Assert.assertFalse(chrono1.equals(null));
        Assert.assertFalse(chrono1.equals("NotAChronology"));
    }

    @Test
    public void testToString() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        Assert.assertEquals("ZonedChronology[ISOChronology[UTC], Europe/Paris]", chrono.toString());
    }

    @Test
    public void testZonedDurationField_Methods() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);

        DurationField hours = chrono.hours();
        Assert.assertTrue(hours.isPrecise());
        Assert.assertEquals(DateTimeConstants.MILLIS_PER_HOUR, hours.getUnitMillis());

        DurationField months = chrono.months();
        Assert.assertFalse(months.isPrecise());

        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 6, 1, 12, 0, 0, 0);

        Assert.assertEquals(2, hours.getValue(2 * 3600000L, instant));
        Assert.assertEquals(2L, hours.getValueAsLong(2 * 3600000L, instant));
        Assert.assertEquals(7200000L, hours.getMillis(2, instant));
        Assert.assertEquals(7200000L, hours.getMillis(2L, instant));

        // add time field
        long addedHour = hours.add(instant, 3);
        Assert.assertEquals(instant + 3 * 3600000L, addedHour);
        long addedHourLong = hours.add(instant, 3L);
        Assert.assertEquals(instant + 3 * 3600000L, addedHourLong);

        // add date field
        DurationField days = chrono.days();
        long addedDays = days.add(instant, 2);
        Assert.assertEquals(instant + 2 * 86400000L, addedDays);
        long addedDaysLong = days.add(instant, 2L);
        Assert.assertEquals(instant + 2 * 86400000L, addedDaysLong);

        // difference
        Assert.assertEquals(3, hours.getDifference(addedHour, instant));
        Assert.assertEquals(3L, hours.getDifferenceAsLong(addedHour, instant));
        Assert.assertEquals(2, days.getDifference(addedDays, instant));
        Assert.assertEquals(2L, days.getDifferenceAsLong(addedDays, instant));
    }

    @Test
    public void testZonedDateTimeField_GetAndText() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourOfDay = chrono.hourOfDay();
        DateTimeField dayOfWeek = chrono.dayOfWeek();

        Assert.assertFalse(hourOfDay.isLenient());

        // 2020-01-01T00:00:00 UTC = 2020-01-01T01:00:00 Paris (offset +1)
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0);

        Assert.assertEquals(1, hourOfDay.get(instant));
        Assert.assertEquals("1", hourOfDay.getAsText(instant, Locale.ENGLISH));
        Assert.assertEquals("1", hourOfDay.getAsShortText(instant, Locale.ENGLISH));
        Assert.assertEquals("5", hourOfDay.getAsText(5, Locale.ENGLISH));
        Assert.assertEquals("5", hourOfDay.getAsShortText(5, Locale.ENGLISH));

        Assert.assertEquals("Wednesday", dayOfWeek.getAsText(instant, Locale.ENGLISH));
        Assert.assertEquals("Wed", dayOfWeek.getAsShortText(instant, Locale.ENGLISH));
    }

    @Test
    public void testZonedDateTimeField_AddAndWrapField() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourOfDay = chrono.hourOfDay();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0); // 01:00 Paris

        // Time field add & wrapField
        long addedHour = hourOfDay.add(instant, 5);
        Assert.assertEquals(6, hourOfDay.get(addedHour));

        long addedHourLong = hourOfDay.add(instant, 5L);
        Assert.assertEquals(6, hourOfDay.get(addedHourLong));

        long wrappedHour = hourOfDay.addWrapField(instant, 23);
        Assert.assertEquals(0, hourOfDay.get(wrappedHour));

        // Date field add & wrapField
        long addedDay = dayOfMonth.add(instant, 10);
        Assert.assertEquals(11, dayOfMonth.get(addedDay));

        long addedDayLong = dayOfMonth.add(instant, 10L);
        Assert.assertEquals(11, dayOfMonth.get(addedDayLong));

        long wrappedDay = dayOfMonth.addWrapField(instant, 31);
        Assert.assertEquals(1, dayOfMonth.get(wrappedDay));
    }

    @Test
    public void testZonedDateTimeField_SetAndExceptions() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourOfDay = chrono.hourOfDay();
        DateTimeField monthOfYear = chrono.monthOfYear();

        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0); // 01:00 Paris

        long setHour = hourOfDay.set(instant, 15);
        Assert.assertEquals(15, hourOfDay.get(setHour));

        long setMonthText = monthOfYear.set(instant, "June", Locale.ENGLISH);
        Assert.assertEquals(6, monthOfYear.get(setMonthText));

        // Setting a time into a DST spring gap throws IllegalFieldValueException
        ZonedChronology nyChrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        DateTimeField nyHour = nyChrono.hourOfDay();
        long nyInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 3, 8, 5, 0, 0, 0); // 00:00 EST
        try {
            nyHour.set(nyInstant, 2); // 02:xx is skipped on 2020-03-08 in New York
            Assert.fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            // Success
        }
    }

    @Test
    public void testZonedDateTimeField_DifferencesAndDurations() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourOfDay = chrono.hourOfDay();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        long instant1 = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0);
        long instant2 = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 4, 0, 0, 0);

        Assert.assertEquals(4, hourOfDay.getDifference(instant2, instant1));
        Assert.assertEquals(4L, hourOfDay.getDifferenceAsLong(instant2, instant1));

        long instant3 = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 5, 0, 0, 0, 0);
        Assert.assertEquals(4, dayOfMonth.getDifference(instant3, instant1));
        Assert.assertEquals(4L, dayOfMonth.getDifferenceAsLong(instant3, instant1));

        Assert.assertNotNull(hourOfDay.getDurationField());
        Assert.assertNotNull(hourOfDay.getRangeDurationField());
        Assert.assertNull(hourOfDay.getLeapDurationField());
    }

    @Test
    public void testZonedDateTimeField_Leap() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField monthOfYear = chrono.monthOfYear();
        DateTimeField yearField = chrono.year();

        long leapYearInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 2, 1, 0, 0, 0, 0);
        long nonLeapYearInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2019, 2, 1, 0, 0, 0, 0);

        Assert.assertTrue(monthOfYear.isLeap(leapYearInstant));
        Assert.assertFalse(monthOfYear.isLeap(nonLeapYearInstant));

        Assert.assertEquals(1, monthOfYear.getLeapAmount(leapYearInstant));
        Assert.assertEquals(0, monthOfYear.getLeapAmount(nonLeapYearInstant));

        Assert.assertNotNull(monthOfYear.getLeapDurationField());
        Assert.assertTrue(yearField.isLeap(leapYearInstant));
    }

    @Test
    public void testZonedDateTimeField_RoundingAndRemainder() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourOfDay = chrono.hourOfDay();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 1, 30, 15, 500);

        // Time field rounding
        long floorTime = hourOfDay.roundFloor(instant);
        long ceilTime = hourOfDay.roundCeiling(instant);
        Assert.assertEquals(chrono.getDateTimeMillis(2020, 1, 1, 2, 0, 0, 0), floorTime);
        Assert.assertEquals(chrono.getDateTimeMillis(2020, 1, 1, 3, 0, 0, 0), ceilTime);

        // Date field rounding
        long floorDate = dayOfMonth.roundFloor(instant);
        long ceilDate = dayOfMonth.roundCeiling(instant);
        Assert.assertEquals(chrono.getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0), floorDate);
        Assert.assertEquals(chrono.getDateTimeMillis(2020, 1, 2, 0, 0, 0, 0), ceilDate);

        // Remainder
        long rem = hourOfDay.remainder(instant);
        Assert.assertEquals((30 * 60 + 15) * 1000 + 500, rem);
    }

    @Test
    public void testZonedDateTimeField_MinMaxValues() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField dayOfMonth = chrono.dayOfMonth();
        long feb2020 = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 2, 1, 0, 0, 0, 0);

        Assert.assertEquals(1, dayOfMonth.getMinimumValue());
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(feb2020));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue());
        Assert.assertEquals(29, dayOfMonth.getMaximumValue(feb2020));

        MonthDay monthDay = new MonthDay(2, 15, chrono);
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(monthDay));
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(monthDay, new int[]{2, 15}));
        Assert.assertEquals(29, dayOfMonth.getMaximumValue(monthDay));
        Assert.assertEquals(29, dayOfMonth.getMaximumValue(monthDay, new int[]{2, 15}));

        Assert.assertTrue(dayOfMonth.getMaximumTextLength(Locale.ENGLISH) >= 2);
        Assert.assertTrue(dayOfMonth.getMaximumShortTextLength(Locale.ENGLISH) >= 2);
    }

    @Test
    public void testZonedFields_UnsupportedFieldConversion() {
        // Build a mock assembled chronology with unsupported fields
        Chronology base = new AssembledChronology(ISOChronology.getInstanceUTC(), null) {
            private static final long serialVersionUID = 1L;
            protected void assemble(Fields fields) {
                fields.halfdayOfDay = UnsupportedDateTimeField.getInstance(
                    DateTimeFieldType.halfdayOfDay(), UnsupportedDurationField.getInstance(DurationFieldType.halfdays()));
                fields.halfdays = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
            }
            public Chronology withUTC() { return this; }
            public Chronology withZone(DateTimeZone zone) { return this; }
            public String toString() { return "MockAssembled"; }
        };

        ZonedChronology zChrono = ZonedChronology.getInstance(base, PARIS);
        Assert.assertFalse(zChrono.halfdays().isSupported());
        Assert.assertFalse(zChrono.halfdayOfDay().isSupported());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZonedDurationField_UnsupportedThrows() {
        new ZonedChronology.ZonedDurationField(UnsupportedDurationField.getInstance(DurationFieldType.days()), PARIS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZonedDateTimeField_UnsupportedThrows() {
        new ZonedChronology.ZonedDateTimeField(
            UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfMonth(), UnsupportedDurationField.getInstance(DurationFieldType.days())),
            PARIS, null, null, null
        );
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDurationField_OffsetOverflow() {
        // Custom offset that triggers overflow when added to Long.MAX_VALUE
        DateTimeZone zoneWithPositiveOffset = DateTimeZone.forOffsetHours(5);
        ZonedChronology.ZonedDurationField field =
            new ZonedChronology.ZonedDurationField(ISOChronology.getInstanceUTC().days(), zoneWithPositiveOffset);
        field.add(Long.MAX_VALUE - 1000, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDurationField_OffsetSubtractOverflow() {
        // Custom offset that triggers overflow when subtracted from Long.MIN_VALUE
        DateTimeZone zoneWithPositiveOffset = DateTimeZone.forOffsetHours(5);
        ZonedChronology.ZonedDurationField field =
            new ZonedChronology.ZonedDurationField(ISOChronology.getInstanceUTC().days(), zoneWithPositiveOffset);
        field.add(Long.MIN_VALUE + 1000, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDateTimeField_OffsetOverflow() {
        DateTimeZone zoneWithPositiveOffset = DateTimeZone.forOffsetHours(5);
        DateTimeField dayField = ISOChronology.getInstanceUTC().dayOfMonth();
        DurationField dayDurField = ISOChronology.getInstanceUTC().days();
        ZonedChronology.ZonedDateTimeField field =
            new ZonedChronology.ZonedDateTimeField(dayField, zoneWithPositiveOffset, dayDurField, null, null);
        field.getDifference(Long.MAX_VALUE - 1000, 0);
    }
}
