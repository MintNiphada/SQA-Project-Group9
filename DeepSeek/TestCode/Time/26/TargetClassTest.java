package org.joda.time.chrono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.chrono.ZonedChronology.ZonedDateTimeField;
import org.joda.time.chrono.ZonedChronology.ZonedDurationField;
import org.junit.Before;
import org.junit.Test;

public class ZonedChronologyTest {

    private Chronology baseUTC;
    private DateTimeZone zoneUTC;
    private DateTimeZone zoneNY;
    private DateTimeZone zoneFixed;
    private ZonedChronology zonedUTC;
    private ZonedChronology zonedNY;

    @Before
    public void setUp() {
        baseUTC = ISOChronology.getInstanceUTC();
        zoneUTC = DateTimeZone.UTC;
        zoneNY = DateTimeZone.forID("America/New_York");
        zoneFixed = DateTimeZone.forOffsetHours(5);
        zonedUTC = ZonedChronology.getInstance(baseUTC, zoneUTC);
        zonedNY = ZonedChronology.getInstance(baseUTC, zoneNY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNullBase() {
        ZonedChronology.getInstance(null, zoneUTC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNullZone() {
        ZonedChronology.getInstance(baseUTC, null);
    }

    @Test
    public void testGetInstanceValid() {
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zoneUTC);
        assertNotNull(zc);
        assertEquals(zoneUTC, zc.getZone());
        assertEquals(baseUTC, zc.getBase());
    }

    @Test
    public void testGetInstanceBaseWithNonUTC() {
        Chronology baseWithZone = ISOChronology.getInstance(DateTimeZone.forID("Europe/London"));
        ZonedChronology zc = ZonedChronology.getInstance(baseWithZone, zoneUTC);
        assertEquals(ISOChronology.getInstanceUTC(), zc.getBase());
    }

    @Test
    public void testWithZoneNull() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        ZonedChronology zc = zonedUTC.withZone(null);
        assertEquals(defaultZone, zc.getZone());
    }

    @Test
    public void testWithZoneSame() {
        assertSame(zonedUTC, zonedUTC.withZone(zoneUTC));
    }

    @Test
    public void testWithZoneUTC() {
        assertSame(baseUTC, zonedUTC.withZone(DateTimeZone.UTC));
    }

    @Test
    public void testWithZoneDifferent() {
        ZonedChronology zc = zonedUTC.withZone(zoneNY);
        assertNotSame(zonedUTC, zc);
        assertEquals(zoneNY, zc.getZone());
        assertEquals(baseUTC, zc.getBase());
    }

    @Test
    public void testWithUTC() {
        assertSame(baseUTC, zonedUTC.withUTC());
    }

    @Test
    public void testGetZone() {
        assertEquals(zoneUTC, zonedUTC.getZone());
    }

    @Test
    public void testGetDateTimeMillis3Args() {
        long millis = zonedUTC.getDateTimeMillis(2000, 1, 1, 0);
        long expected = baseUTC.getDateTimeMillis(2000, 1, 1, 0);
        assertEquals(expected, millis);
    }

    @Test
    public void testGetDateTimeMillis7Args() {
        long millis = zonedUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 45, 500);
        long expected = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 45, 500);
        assertEquals(expected, millis);
    }

    @Test
    public void testGetDateTimeMillis4Args() {
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0);
        long millis = zonedUTC.getDateTimeMillis(instant, 12, 30, 45, 500);
        long expected = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 45, 500);
        assertEquals(expected, millis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLocalToUTCTransition() {
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zoneNY);
        zc.getDateTimeMillis(2023, 3, 12, 2, 30, 0, 0);
    }

    @Test
    public void testEqualsSame() {
        assertTrue(zonedUTC.equals(zonedUTC));
    }

    @Test
    public void testEqualsDifferentBase() {
        ZonedChronology other = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), zoneNY);
        assertFalse(zonedNY.equals(other));
    }

    @Test
    public void testEqualsDifferentZone() {
        ZonedChronology other = ZonedChronology.getInstance(baseUTC, zoneFixed);
        assertFalse(zonedNY.equals(other));
    }

    @Test
    public void testEqualsNotZonedChronology() {
        assertFalse(zonedUTC.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        ZonedChronology zc1 = ZonedChronology.getInstance(baseUTC, zoneNY);
        ZonedChronology zc2 = ZonedChronology.getInstance(baseUTC, zoneNY);
        assertEquals(zc1.hashCode(), zc2.hashCode());
    }

    @Test
    public void testToString() {
        String str = zonedUTC.toString();
        assertTrue(str.contains("ZonedChronology"));
        assertTrue(str.contains(baseUTC.toString()));
        assertTrue(str.contains(zoneUTC.getID()));
    }

    @Test
    public void testUseTimeArithmeticNull() {
        assertFalse(ZonedChronology.useTimeArithmetic(null));
    }

    @Test
    public void testUseTimeArithmeticLessThan12Hours() {
        DurationField hours = baseUTC.hours();
        assertTrue(ZonedChronology.useTimeArithmetic(hours));
    }

    @Test
    public void testUseTimeArithmetic12HoursOrMore() {
        DurationField halfdays = baseUTC.halfdays();
        assertFalse(ZonedChronology.useTimeArithmetic(halfdays));
    }

    @Test
    public void testZonedDurationFieldIsPreciseFixedZone() {
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zoneFixed);
        DurationField days = zc.days();
        assertTrue(days.isPrecise());
    }

    @Test
    public void testZonedDurationFieldIsPreciseNonFixedZone() {
        DurationField days = zonedNY.days();
        assertFalse(days.isPrecise());
    }

    @Test
    public void testZonedDurationFieldGetUnitMillis() {
        DurationField days = zonedUTC.days();
        assertEquals(DateTimeConstants.MILLIS_PER_DAY, days.getUnitMillis());
    }

    @Test
    public void testZonedDurationFieldGetValue() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(12, hours.getValue(instant));
    }

    @Test
    public void testZonedDurationFieldGetValueAsLong() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(12L, hours.getValueAsLong(instant));
    }

    @Test
    public void testZonedDurationFieldGetMillisInt() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long expected = baseUTC.getDateTimeMillis(2000, 1, 1, 5, 0, 0, 0);
        assertEquals(expected, hours.getMillis(5, instant));
    }

    @Test
    public void testZonedDurationFieldGetMillisLong() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long expected = baseUTC.getDateTimeMillis(2000, 1, 1, 5, 0, 0, 0);
        assertEquals(expected, hours.getMillis(5L, instant));
    }

    @Test
    public void testZonedDurationFieldAddIntTimeField() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = hours.add(instant, 2);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0), result);
    }

    @Test
    public void testZonedDurationFieldAddIntNonTimeField() {
        DurationField days = zonedUTC.days();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = days.add(instant, 1);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 2, 12, 0, 0, 0), result);
    }

    @Test
    public void testZonedDurationFieldAddLong() {
        DurationField hours = zonedUTC.hours();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = hours.add(instant, 2L);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0), result);
    }

    @Test
    public void testZonedDurationFieldGetDifference() {
        DurationField hours = zonedUTC.hours();
        long minuend = baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0);
        long subtrahend = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(2, hours.getDifference(minuend, subtrahend));
    }

    @Test
    public void testZonedDurationFieldGetDifferenceAsLong() {
        DurationField hours = zonedUTC.hours();
        long minuend = baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0);
        long subtrahend = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(2L, hours.getDifferenceAsLong(minuend, subtrahend));
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDurationFieldOverflowAdd() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(12);
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zone);
        DurationField millis = zc.millis();
        long instant = Long.MAX_VALUE - 1000;
        millis.add(instant, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDurationFieldOverflowSubtract() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-12);
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zone);
        DurationField days = zc.days();
        long instant = Long.MIN_VALUE + 1000;
        days.add(instant, -1);
    }

    @Test
    public void testZonedDateTimeFieldGet() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 15, 0, 0, 0);
        assertEquals(15, hourOfDay.get(instant));
    }

    @Test
    public void testZonedDateTimeFieldGetAsText() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        long instant = baseUTC.getDateTimeMillis(2000, 6, 1, 0, 0, 0, 0);
        assertEquals("June", monthOfYear.getAsText(instant, Locale.ENGLISH));
    }

    @Test
    public void testZonedDateTimeFieldGetAsShortText() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        long instant = baseUTC.getDateTimeMillis(2000, 6, 1, 0, 0, 0, 0);
        assertEquals("Jun", monthOfYear.getAsShortText(instant, Locale.ENGLISH));
    }

    @Test
    public void testZonedDateTimeFieldGetAsTextInt() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        assertEquals("June", monthOfYear.getAsText(6, Locale.ENGLISH));
    }

    @Test
    public void testZonedDateTimeFieldGetAsShortTextInt() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        assertEquals("Jun", monthOfYear.getAsShortText(6, Locale.ENGLISH));
    }

    @Test
    public void testZonedDateTimeFieldAddIntTimeField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = hourOfDay.add(instant, 2);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldAddIntNonTimeField() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = dayOfMonth.add(instant, 1);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 2, 12, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldAddLong() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = hourOfDay.add(instant, 2L);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldAddWrapField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 23, 0, 0, 0);
        long result = hourOfDay.addWrapField(instant, 2);
        assertEquals(1, hourOfDay.get(result));
    }

    @Test
    public void testZonedDateTimeFieldSetInt() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        long result = hourOfDay.set(instant, 15);
        assertEquals(15, hourOfDay.get(result));
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testZonedDateTimeFieldSetIntTransition() {
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zoneNY);
        DateTimeField hourOfDay = zc.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2023, 3, 12, 6, 0, 0, 0);
        hourOfDay.set(instant, 2);
    }

    @Test
    public void testZonedDateTimeFieldSetText() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long result = monthOfYear.set(instant, "June", Locale.ENGLISH);
        assertEquals(6, monthOfYear.get(result));
    }

    @Test
    public void testZonedDateTimeFieldGetDifference() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long minuend = baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0);
        long subtrahend = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(2, hourOfDay.getDifference(minuend, subtrahend));
    }

    @Test
    public void testZonedDateTimeFieldGetDifferenceAsLong() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long minuend = baseUTC.getDateTimeMillis(2000, 1, 1, 14, 0, 0, 0);
        long subtrahend = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        assertEquals(2L, hourOfDay.getDifferenceAsLong(minuend, subtrahend));
    }

    @Test
    public void testZonedDateTimeFieldGetDurationField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        assertTrue(hourOfDay.getDurationField() instanceof ZonedDurationField);
    }

    @Test
    public void testZonedDateTimeFieldGetRangeDurationField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        assertTrue(hourOfDay.getRangeDurationField() instanceof ZonedDurationField);
    }

    @Test
    public void testZonedDateTimeFieldIsLeap() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        long instant = baseUTC.getDateTimeMillis(2000, 2, 29, 0, 0, 0, 0);
        assertTrue(dayOfMonth.isLeap(instant));
    }

    @Test
    public void testZonedDateTimeFieldGetLeapAmount() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        long instant = baseUTC.getDateTimeMillis(2000, 2, 29, 0, 0, 0, 0);
        assertEquals(1, dayOfMonth.getLeapAmount(instant));
    }

    @Test
    public void testZonedDateTimeFieldGetLeapDurationField() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        assertNotNull(dayOfMonth.getLeapDurationField());
    }

    @Test
    public void testZonedDateTimeFieldRoundFloorTimeField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 0, 0);
        long result = hourOfDay.roundFloor(instant);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldRoundFloorNonTimeField() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 15, 12, 0, 0, 0);
        long result = dayOfMonth.roundFloor(instant);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 15, 0, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldRoundCeilingTimeField() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 0, 0);
        long result = hourOfDay.roundCeiling(instant);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 1, 13, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldRoundCeilingNonTimeField() {
        DateTimeField dayOfMonth = zonedUTC.dayOfMonth();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 15, 12, 0, 0, 0);
        long result = dayOfMonth.roundCeiling(instant);
        assertEquals(baseUTC.getDateTimeMillis(2000, 1, 16, 0, 0, 0, 0), result);
    }

    @Test
    public void testZonedDateTimeFieldRemainder() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 12, 30, 0, 0);
        long remainder = hourOfDay.remainder(instant);
        assertEquals(30 * DateTimeConstants.MILLIS_PER_MINUTE, remainder);
    }

    @Test
    public void testZonedDateTimeFieldGetMinimumValue() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        assertEquals(0, hourOfDay.getMinimumValue());
    }

    @Test
    public void testZonedDateTimeFieldGetMinimumValueInstant() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        assertEquals(0, hourOfDay.getMinimumValue(instant));
    }

    @Test
    public void testZonedDateTimeFieldGetMaximumValue() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        assertEquals(23, hourOfDay.getMaximumValue());
    }

    @Test
    public void testZonedDateTimeFieldGetMaximumValueInstant() {
        DateTimeField hourOfDay = zonedUTC.hourOfDay();
        long instant = baseUTC.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        assertEquals(23, hourOfDay.getMaximumValue(instant));
    }

    @Test
    public void testZonedDateTimeFieldGetMaximumTextLength() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        assertTrue(monthOfYear.getMaximumTextLength(Locale.ENGLISH) > 0);
    }

    @Test
    public void testZonedDateTimeFieldGetMaximumShortTextLength() {
        DateTimeField monthOfYear = zonedUTC.monthOfYear();
        assertTrue(monthOfYear.getMaximumShortTextLength(Locale.ENGLISH) > 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testZonedDateTimeFieldOverflowAdd() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(12);
        ZonedChronology zc = ZonedChronology.getInstance(baseUTC, zone);
        DateTimeField millisOfSecond = zc.millisOfSecond();
        long instant = Long.MAX_VALUE - 1000;
        millisOfSecond.add(instant, 1);
    }

    @Test
    public void testConvertFieldCaching() {
        DateTimeField field1 = zonedUTC.year();
        DateTimeField field2 = zonedUTC.year();
        assertSame(field1, field2);
    }
}
