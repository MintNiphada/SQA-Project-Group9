package org.joda.time.format;

import static org.junit.Assert.*;
import java.util.Locale;
import org.joda.time.*;
import org.junit.Test;

public class DateTimeParserBucketTest {

    private static final DateTimeZone UTC = DateTimeZone.UTC;
    private static final Chronology ISO_UTC = ISOChronology.getInstanceUTC();

    private Chronology chronoWithZone(DateTimeZone zone) {
        return ISOChronology.getInstance(zone);
    }

    @Test
    public void testDeprecatedConstructor1() {
        Locale loc = Locale.UK;
        Chronology chrono = chronoWithZone(UTC);
        DateTimeParserBucket bucket = new DateTimeParserBucket(1234L, chrono, loc);
        assertEquals(ISO_UTC, bucket.getChronology());
        assertEquals(loc, bucket.getLocale());
        assertEquals(null, bucket.getZone());
        assertEquals(0, bucket.getOffset());
        assertEquals(Integer.valueOf(2000), bucket.getPivotYear());
    }

    @Test
    public void testDeprecatedConstructor2() {
        Locale loc = Locale.US;
        Chronology chrono = chronoWithZone(DateTimeZone.forID("Europe/Paris"));
        Integer pivot = 1990;
        DateTimeParserBucket bucket = new DateTimeParserBucket(5678L, chrono, loc, pivot);
        assertEquals(ISO_UTC, bucket.getChronology());
        assertEquals(loc, bucket.getLocale());
        assertEquals(DateTimeZone.forID("Europe/Paris"), bucket.getZone());
        assertEquals(0, bucket.getOffset());
        assertEquals(pivot, bucket.getPivotYear());
    }

    @Test
    public void testMainConstructor() {
        Locale loc = Locale.CANADA;
        Chronology chrono = chronoWithZone(DateTimeZone.forID("Asia/Tokyo"));
        Integer pivot = null;
        int defaultYear = 2025;
        DateTimeParserBucket bucket = new DateTimeParserBucket(999L, chrono, loc, pivot, defaultYear);
        assertEquals(ISO_UTC, bucket.getChronology());
        assertEquals(loc, bucket.getLocale());
        assertEquals(DateTimeZone.forID("Asia/Tokyo"), bucket.getZone());
        assertEquals(0, bucket.getOffset());
        assertNull(bucket.getPivotYear());
    }

    @Test
    public void testMainConstructorNullLocale() {
        Locale.setDefault(Locale.FRANCE);
        Chronology chrono = ISO_UTC;
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, null, null, 2000);
        assertEquals(Locale.getDefault(), bucket.getLocale());
    }

    @Test
    public void testGetChronology() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        assertEquals(ISO_UTC, bucket.getChronology());
    }

    @Test
    public void testGetLocale() {
        Locale loc = Locale.GERMANY;
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, loc, null, 2000);
        assertEquals(loc, bucket.getLocale());
    }

    @Test
    public void testGetZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = chronoWithZone(zone);
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.US, null, 2000);
        assertEquals(zone, bucket.getZone());
    }

    @Test
    public void testGetOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        assertEquals(0, bucket.getOffset());
        bucket.setOffset(3600);
        assertEquals(3600, bucket.getOffset());
    }

    @Test
    public void testGetPivotYear() {
        Integer pivot = 1980;
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, pivot, 2000);
        assertEquals(pivot, bucket.getPivotYear());
    }

    @Test
    public void testSetZone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.setZone(DateTimeZone.UTC);
        assertNull(bucket.getZone());
        assertEquals(0, bucket.getOffset());
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        bucket.setZone(zone);
        assertEquals(zone, bucket.getZone());
        assertEquals(0, bucket.getOffset());
    }

    @Test
    public void testSetOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.setOffset(7200);
        assertNull(bucket.getZone());
        assertEquals(7200, bucket.getOffset());
        bucket.setOffset(-1800);
        assertEquals(-1800, bucket.getOffset());
    }

    @Test
    public void testSetPivotYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.setPivotYear(1950);
        assertEquals(Integer.valueOf(1950), bucket.getPivotYear());
        bucket.setPivotYear(null);
        assertNull(bucket.getPivotYear());
    }

    @Test
    public void testSaveFieldWithDateTimeField() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        DateTimeField field = ISO_UTC.monthOfYear();
        bucket.saveField(field, 5);
        long result = bucket.computeMillis();
        assertEquals(ISO_UTC.monthOfYear().set(0L, 5), result);
    }

    @Test
    public void testSaveFieldWithDateTimeFieldType() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 20);
        long result = bucket.computeMillis();
        assertEquals(ISO_UTC.dayOfMonth().set(0L, 20), result);
    }

    @Test
    public void testSaveFieldWithText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "February", Locale.ENGLISH);
        long result = bucket.computeMillis();
        long expected = ISO_UTC.monthOfYear().set(0L, "February", Locale.ENGLISH);
        assertEquals(expected, result);
    }

    @Test
    public void testSaveStateAndRestore() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        Object state = bucket.saveState();
        assertNotNull(state);
        Object state2 = bucket.saveState();
        assertSame(state, state2);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.setOffset(1000);
        assertTrue(bucket.restoreState(state));
        assertEquals(0, bucket.getOffset());
        assertEquals(0L, bucket.computeMillis());
        assertTrue(bucket.restoreState(state2));
        assertTrue(bucket.restoreState(state));
    }

    @Test
    public void testRestoreStateInvalid() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        assertFalse(bucket.restoreState(null));
        assertFalse(bucket.restoreState("invalid"));
        DateTimeParserBucket other = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        Object state = other.saveState();
        assertFalse(bucket.restoreState(state));
    }

    @Test
    public void testComputeMillisNoFields() {
        long millis = 1234567L;
        DateTimeParserBucket bucket = new DateTimeParserBucket(millis, ISO_UTC, Locale.US, null, 2000);
        assertEquals(millis, bucket.computeMillis());
        assertEquals(millis, bucket.computeMillis(false));
        assertEquals(millis, bucket.computeMillis(true));
        assertEquals(millis, bucket.computeMillis(false, "text"));
    }

    @Test
    public void testComputeMillisResetFalse() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        long result = bucket.computeMillis(false);
        long expected = ISO_UTC.year().set(0L, 2020);
        expected = ISO_UTC.monthOfYear().set(expected, 3);
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisResetTrue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        long result = bucket.computeMillis(true);
        long expected = ISO_UTC.year().set(0L, 2020);
        expected = ISO_UTC.monthOfYear().set(expected, 3);
        expected = ISO_UTC.monthOfYear().roundFloor(expected);
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisDefaultYearInsertionForMonth() {
        int defaultYear = 2015;
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, defaultYear);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 7);
        long result = bucket.computeMillis();
        long expected = ISO_UTC.year().set(0L, defaultYear);
        expected = ISO_UTC.monthOfYear().set(expected, 7);
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisDefaultYearInsertionForDay() {
        int defaultYear = 2018;
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, defaultYear);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        long result = bucket.computeMillis();
        long expected = ISO_UTC.year().set(0L, defaultYear);
        expected = ISO_UTC.dayOfMonth().set(expected, 15);
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisOutOfRange() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e instanceof IllegalFieldValueException);
        }
    }

    @Test
    public void testComputeMillisOutOfRangeWithText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis(false, "parse me");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("parse me"));
        }
    }

    @Test
    public void testComputeMillisWithOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.setOffset(3600);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 10);
        long result = bucket.computeMillis();
        long expected = ISO_UTC.hourOfDay().set(0L, 10) - 3600;
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisWithZoneNoTransition() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = chronoWithZone(zone);
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2022);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);
        long result = bucket.computeMillis();
        long expected = new DateTime(2022, 1, 15, 12, 0, zone).getMillis();
        assertEquals(expected, result);
    }

    @Test
    public void testComputeMillisWithZoneTransitionGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = chronoWithZone(zone);
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2022);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 13);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        try {
            bucket.computeMillis();
            fail("Expected IllegalArgumentException due to time zone gap");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal instant"));
        }
    }

    @Test
    public void testComputeMillisWithZoneTransitionOverlap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = chronoWithZone(zone);
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2022);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 11);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 6);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 1);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        long result = bucket.computeMillis();
        DateTime dt = new DateTime(2022, 11, 6, 1, 30, zone);
        assertTrue(result == dt.getMillis() || result == dt.withEarlierOffsetAtOverlap().getMillis());
    }

    @Test
    public void testSortWithMoreThan10Fields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 10);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 20);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 30);
        bucket.saveField(DateTimeFieldType.millisOfSecond(), 500);
        bucket.saveField(DateTimeFieldType.weekyear(), 2020);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 25);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 3);
        bucket.saveField(DateTimeFieldType.era(), 1);
        long result = bucket.computeMillis();
        assertTrue(result > 0);
    }

    @Test
    public void testSavedFieldsSharedClone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISO_UTC, Locale.US, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        Object state = bucket.saveState();
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        assertTrue(bucket.restoreState(state));
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);
        long result = bucket.computeMillis();
        long expected = ISO_UTC.year().set(0L, 2010);
        expected = ISO_UTC.dayOfMonth().set(expected, 10);
        assertEquals(expected, result);
    }

    @Test
    public void testCompareReverse() {
        DurationField days = DurationFieldType.days().getField(ISO_UTC);
        DurationField months = DurationFieldType.months().getField(ISO_UTC);
        assertTrue(DateTimeParserBucket.compareReverse(months, days) < 0);
        assertTrue(DateTimeParserBucket.compareReverse(days, months) > 0);
        assertEquals(0, DateTimeParserBucket.compareReverse(null, null));
        assertTrue(DateTimeParserBucket.compareReverse(null, days) < 0);
        assertTrue(DateTimeParserBucket.compareReverse(days, null) > 0);
        assertEquals(0, DateTimeParserBucket.compareReverse(null, null));
        DurationField unsupported = new DelegatedDurationField(DurationFieldType.eras(), null);
        assertEquals(0, DateTimeParserBucket.compareReverse(null, unsupported));
        assertEquals(0, DateTimeParserBucket.compareReverse(unsupported, unsupported));
        assertTrue(DateTimeParserBucket.compareReverse(unsupported, days) < 0);
        assertTrue(DateTimeParserBucket.compareReverse(days, unsupported) > 0);
    }

    static class DelegatedDurationField extends DurationField {
        private final DurationField iWrapped;
        DelegatedDurationField(DurationFieldType type, DurationField wrapped) {
            super(type);
            iWrapped = wrapped;
        }
        @Override public long getUnitMillis() { return iWrapped == null ? 0 : iWrapped.getUnitMillis(); }
        @Override public boolean isSupported() { return iWrapped != null && iWrapped.isSupported(); }
        @Override public long add(long instant, int value) { return iWrapped.add(instant, value); }
        @Override public long add(long instant, long value) { return iWrapped.add(instant, value); }
        @Override public int getDifference(long minuendInstant, long subtrahendInstant) { return iWrapped.getDifference(minuendInstant, subtrahendInstant); }
        @Override public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) { return iWrapped.getDifferenceAsLong(minuendInstant, subtrahendInstant); }
        @Override public int compareTo(DurationField otherField) { return iWrapped.compareTo(otherField); }
    }
}
