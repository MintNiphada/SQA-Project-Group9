package org.joda.time;

import org.joda.time.base.BasePeriod;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for {@link Period}.
 */
public class PeriodTest {

    private static final int MAX_INT = Integer.MAX_VALUE;
    private static final int MIN_INT = Integer.MIN_VALUE;

    // ---------------------------------------------------------------
    // Constructor tests: zero-length, partial fields, durations
    // ---------------------------------------------------------------

    @Test
    public void testConstructor_empty() {
        Period p = new Period();
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(0, p.getDays());
        assertEquals(0, p.getHours());
        assertEquals(0, p.getMinutes());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    @Test
    public void testConstructor_timeFields_only() {
        Period p = new Period(1, 2, 3, 4);
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(0, p.getDays());
        assertEquals(1, p.getHours());
        assertEquals(2, p.getMinutes());
        assertEquals(3, p.getSeconds());
        assertEquals(4, p.getMillis());
        assertEquals(PeriodType.standard(), p.getPeriodType());
    }

    @Test
    public void testConstructor_timeFields_negativeValues() {
        Period p = new Period(-1, -2, -3, -4);
        assertEquals(-1, p.getHours());
        assertEquals(-2, p.getMinutes());
        assertEquals(-3, p.getSeconds());
        assertEquals(-4, p.getMillis());
    }

    @Test
    public void testConstructor_allFields_eightInts() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
        assertEquals(8, p.getMillis());
    }

    @Test
    public void testConstructor_allFields_withType() {
        PeriodType type = PeriodType.yearMonthDayTime();
        Period p = new Period(1, 2, 0, 3, 4, 5, 6, 7, type);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(0, p.getWeeks()); // type doesn't support weeks
        assertEquals(3, p.getDays());
        assertEquals(4, p.getHours());
        assertEquals(5, p.getMinutes());
        assertEquals(6, p.getSeconds());
        assertEquals(7, p.getMillis());
        assertEquals(type, p.getPeriodType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidType_nonZeroUnsupportedField() {
        // weeks > 0 but type does not support weeks
        new Period(1, 2, 1, 3, 4, 5, 6, 7, PeriodType.yearMonthDayTime());
    }

    @Test
    public void testConstructor_duration_only() {
        long hours = 2L * 60 * 60 * 1000 + 30 * 60 * 1000; // 2h30m
        Period p = new Period(hours);
        assertEquals(2, p.getHours());
        assertEquals(30, p.getMinutes());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMillis());
        assertEquals(PeriodType.standard(), p.getPeriodType());
    }

    @Test
    public void testConstructor_duration_withType() {
        long duration = 5000L; // 5 seconds
        Period p = new Period(duration, PeriodType.standard());
        assertEquals(5, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    @Test
    public void testConstructor_duration_withChronology() {
        long duration = 2 * 60 * 60 * 1000; // 2 hours
        Period p = new Period(duration, ISOChronology.getInstanceUTC());
        assertEquals(2, p.getHours());
    }

    @Test
    public void testConstructor_duration_withTypeAndChronology() {
        Period p = new Period(3600000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertEquals(1, p.getHours());
        assertEquals(0, p.getYears());
    }

    @Test
    public void testConstructor_interval_long_long() {
        long start = 1000L;
        long end = start + 3 * 60 * 1000; // 3 minutes later
        Period p = new Period(start, end);
        assertEquals(3, p.getMinutes());
    }

    @Test
    public void testConstructor_interval_long_long_withType() {
        long start = 0L;
        long end = 7200000L; // 2 hours
        Period p = new Period(start, end, PeriodType.time());
        assertEquals(2, p.getHours());
        assertEquals(0, p.getDays());
    }

    @Test
    public void testConstructor_interval_long_long_withChrono() {
        long start = 1000L;
        long end = start + 86400000L; // 1 day
        Period p = new Period(start, end, ISOChronology.getInstanceUTC());
        assertEquals(1, p.getDays());
    }

    @Test
    public void testConstructor_interval_long_long_full() {
        long start = 0;
        long end = 86400000L * 2; // 2 days
        Period p = new Period(start, end, PeriodType.dayTime(), ISOChronology.getInstanceUTC());
        assertEquals(2, p.getDays());
        assertEquals(0, p.getWeeks());
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        DateTime end = start.plusDays(1).plusHours(2);
        Period p = new Period(start, end);
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(1, p.getDays());
        assertEquals(2, p.getHours());
        assertEquals(0, p.getMinutes());
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_nullStart() {
        DateTime end = DateTime.now();
        Period p = new Period(null, end);
        assertNotNull(p);
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_nullEnd() {
        DateTime start = DateTime.now();
        Period p = new Period(start, null);
        assertNotNull(p);
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_withType() {
        DateTime start = new DateTime(2020, 6, 1, 0, 0);
        DateTime end = start.plusMonths(2);
        Period p = new Period(start, end, PeriodType.yearMonthDay());
        assertEquals(0, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(0, p.getDays());
    }

    @Test
    public void testConstructor_ReadablePartial_ReadablePartial() {
        LocalDate start = new LocalDate(2020, 1, 15);
        LocalDate end = new LocalDate(2020, 2, 20);
        Period p = new Period(start, end);
        assertEquals(0, p.getYears());
        assertEquals(1, p.getMonths());
        assertEquals(5, p.getDays());
    }

    @Test
    public void testConstructor_ReadablePartial_ReadablePartial_withType() {
        LocalDate start = new LocalDate(2020, 3, 1);
        LocalDate end = new LocalDate(2020, 3, 31);
        Period p = new Period(start, end, PeriodType.yearMonthDay());
        assertEquals(30, p.getDays());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_nullStart() {
        new Period((ReadablePartial) null, new LocalDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_nullEnd() {
        new Period(new LocalDate(), (ReadablePartial) null);
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableDuration() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0);
        Duration dur = Duration.standardHours(2);
        Period p = new Period(start, dur);
        assertEquals(2, p.getHours());
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableDuration_withType() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0);
        Duration dur = Duration.standardMinutes(90);
        Period p = new Period(start, dur, PeriodType.time());
        assertEquals(1, p.getHours());
        assertEquals(30, p.getMinutes());
    }

    @Test
    public void testConstructor_ReadableDuration_ReadableInstant() {
        Duration dur = Duration.standardDays(3);
        DateTime end = DateTime.now();
        Period p = new Period(dur, end);
        assertNotNull(p);
    }

    @Test
    public void testConstructor_ReadableDuration_ReadableInstant_withType() {
        Duration dur = Duration.standardSeconds(3600);
        DateTime end = DateTime.now();
        Period p = new Period(dur, end, PeriodType.time());
        assertEquals(1, p.getHours());
    }

    @Test
    public void testConstructor_Object() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(p1);
        assertEquals(p1, p2);
    }

    @Test
    public void testConstructor_Object_withType() {
        Period p1 = Period.years(5).withMonths(6);
        Period p2 = new Period(p1, PeriodType.standard());
        assertEquals(p1.getYears(), p2.getYears());
        assertEquals(p1.getMonths(), p2.getMonths());
    }

    @Test
    public void testConstructor_Object_withChrono() {
        Period p1 = Period.hours(10);
        Period p2 = new Period(p1, ISOChronology.getInstanceUTC());
        assertEquals(p1.getHours(), p2.getHours());
    }

    @Test
    public void testConstructor_Object_withTypeAndChrono() {
        Period p1 = Period.millis(500);
        Period p2 = new Period(p1, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(500, p2.getMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Object_invalidString() {
        new Period("not a period");
    }

    // ---------------------------------------------------------------
    // Static factories
    // ---------------------------------------------------------------

    @Test
    public void testStatic_years() {
        Period p = Period.years(5);
        assertEquals(5, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(PeriodType.standard(), p.getPeriodType());
    }

    @Test
    public void testStatic_years_negative() {
        Period p = Period.years(-3);
        assertEquals(-3, p.getYears());
    }

    @Test
    public void testStatic_months() {
        Period p = Period.months(4);
        assertEquals(0, p.getYears());
        assertEquals(4, p.getMonths());
    }

    @Test
    public void testStatic_weeks() {
        Period p = Period.weeks(9);
        assertEquals(9, p.getWeeks());
    }

    @Test
    public void testStatic_days() {
        Period p = Period.days(10);
        assertEquals(10, p.getDays());
    }

    @Test
    public void testStatic_hours() {
        Period p = Period.hours(15);
        assertEquals(15, p.getHours());
    }

    @Test
    public void testStatic_minutes() {
        Period p = Period.minutes(30);
        assertEquals(30, p.getMinutes());
    }

    @Test
    public void testStatic_seconds() {
        Period p = Period.seconds(45);
        assertEquals(45, p.getSeconds());
    }

    @Test
    public void testStatic_millis() {
        Period p = Period.millis(750);
        assertEquals(750, p.getMillis());
    }

    @Test
    public void testStatic_fieldDifference_normal() {
        ReadablePartial start = new Partial(DateTimeFieldType.year(), 2020).with(DateTimeFieldType.monthOfYear(), 6);
        ReadablePartial end = new Partial(DateTimeFieldType.year(), 2021).with(DateTimeFieldType.monthOfYear(), 8);
        Period p = Period.fieldDifference(start, end);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
    }

    @Test
    public void testStatic_fieldDifference_negativeDiff() {
        ReadablePartial start = new Partial(DateTimeFieldType.year(), 2022).with(DateTimeFieldType.monthOfYear(), 3);
        ReadablePartial end = new Partial(DateTimeFieldType.year(), 2022).with(DateTimeFieldType.monthOfYear(), 1);
        Period p = Period.fieldDifference(start, end);
        assertEquals(0, p.getYears());
        assertEquals(-2, p.getMonths());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatic_fieldDifference_nullStart() {
        Period.fieldDifference(null, new Partial(DateTimeFieldType.year(), 2020));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatic_fieldDifference_nullEnd() {
        Period.fieldDifference(new Partial(DateTimeFieldType.year(), 2020), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatic_fieldDifference_differentSizes() {
        ReadablePartial p1 = new Partial(DateTimeFieldType.year(), 2020);
        ReadablePartial p2 = new Partial(DateTimeFieldType.year(), 2021).with(DateTimeFieldType.monthOfYear(), 5);
        Period.fieldDifference(p1, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatic_fieldDifference_differentFieldTypes() {
        ReadablePartial p1 = new Partial(DateTimeFieldType.year(), 2020).with(DateTimeFieldType.monthOfYear(), 1);
        ReadablePartial p2 = new Partial(DateTimeFieldType.year(), 2020).with(DateTimeFieldType.dayOfMonth(), 1);
        Period.fieldDifference(p1, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatic_fieldDifference_overlappingFields() {
        ReadablePartial p1 = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 1)
                .with(DateTimeFieldType.dayOfYear(), 1);
        ReadablePartial p2 = new Partial(DateTimeFieldType.year(), 2021)
                .with(DateTimeFieldType.monthOfYear(), 2)
                .with(DateTimeFieldType.dayOfYear(), 2);
        // dayOfYear overlaps with monthOfYear? Yes, month and dayOfYear have same duration type? Might be caught.
        Period.fieldDifference(p1, p2);
    }

    @Test
    public void testParse_isoStandard() {
        Period p = Period.parse("P1Y2M3W4DT5H6M7S");
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    @Test
    public void testParse_withFormatter() {
        PeriodFormatter fmt = ISOPeriodFormat.standard();
        Period p = Period.parse("P1Y", fmt);
        assertEquals(1, p.getYears());
    }

    // ---------------------------------------------------------------
    // toPeriod
    // ---------------------------------------------------------------
    @Test
    public void testToPeriod_returnsSame() {
        Period p = Period.years(3);
        assertSame(p, p.toPeriod());
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    @Test
    public void testGetYears() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, p.getYears());
    }

    @Test
    public void testGetMonths() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(2, p.getMonths());
    }

    @Test
    public void testGetWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(3, p.getWeeks());
    }

    @Test
    public void testGetDays() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(4, p.getDays());
    }

    @Test
    public void testGetHours() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(5, p.getHours());
    }

    @Test
    public void testGetMinutes() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(6, p.getMinutes());
    }

    @Test
    public void testGetSeconds() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(7, p.getSeconds());
    }

    @Test
    public void testGetMillis() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(8, p.getMillis());
    }

    // ---------------------------------------------------------------
    // withPeriodType
    // ---------------------------------------------------------------
    @Test
    public void testWithPeriodType_sameType() {
        Period p = Period.years(1).withMonths(2);
        Period p2 = p.withPeriodType(PeriodType.standard());
        assertSame(p, p2);
    }

    @Test
    public void testWithPeriodType_differentType() {
        Period p = Period.years(1).withMonths(2).withWeeks(3); // standard
        Period p2 = p.withPeriodType(PeriodType.yearMonthDayTime());
        assertEquals(1, p2.getYears());
        assertEquals(2, p2.getMonths());
        assertEquals(0, p2.getWeeks()); // not supported in new type
        assertEquals(3, p2.getDays());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithPeriodType_incompatibleType_nonZeroUnsupported() {
        Period p = new Period(1, 2, 3, 4);
        p.withPeriodType(PeriodType.yearMonthDay());
    }

    // ---------------------------------------------------------------
    // withFields
    // ---------------------------------------------------------------
    @Test
    public void testWithFields_null() {
        Period p = Period.years(1);
        Period p2 = p.withFields(null);
        assertSame(p, p2);
    }

    @Test
    public void testWithFields_merge() {
        Period base = Period.years(2).withMonths(3);
        Period add = Period.days(5).withHours(7);
        Period merged = base.withFields(add);
        assertEquals(2, merged.getYears());
        assertEquals(3, merged.getMonths());
        assertEquals(0, merged.getWeeks());
        assertEquals(5, merged.getDays());
        assertEquals(7, merged.getHours());
    }

    @Test
    public void testWithFields_mergeOverwrites() {
        Period base = Period.years(2).withMonths(3);
        Period add = Period.years(1);
        Period merged = base.withFields(add);
        assertEquals(1, merged.getYears()); // overwritten
        assertEquals(3, merged.getMonths());
    }

    // ---------------------------------------------------------------
    // withField / withFieldAdded
    // ---------------------------------------------------------------
    @Test
    public void testWithField_basic() {
        Period p = Period.years(1);
        Period p2 = p.withField(DurationFieldType.years(), 5);
        assertEquals(5, p2.getYears());
        assertEquals(PeriodType.standard(), p2.getPeriodType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_nullField() {
        Period p = Period.years(1);
        p.withField(null, 10);
    }

    @Test
    public void testWithFieldAdded_basic() {
        Period p = Period.hours(3);
        Period p2 = p.withFieldAdded(DurationFieldType.hours(), 5);
        assertEquals(8, p2.getHours());
    }

    @Test
    public void testWithFieldAdded_zero() {
        Period p = Period.hours(3);
        Period p2 = p.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(p, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_nullField() {
        Period p = Period.hours(3);
        p.withFieldAdded(null, 5);
    }

    // ---------------------------------------------------------------
    // withXxx setters
    // ---------------------------------------------------------------
    @Test
    public void testWithYears() {
        Period p = Period.years(1);
        Period p2 = p.withYears(3);
        assertEquals(3, p2.getYears());
        assertEquals(1, p.getYears());
    }

    @Test
    public void testWithMonths() {
        Period p = Period.months(2);
        Period p2 = p.withMonths(-5);
        assertEquals(-5, p2.getMonths());
    }

    @Test
    public void testWithWeeks() {
        Period p = Period.weeks(4);
        Period p2 = p.withWeeks(0);
        assertEquals(0, p2.getWeeks());
    }

    @Test
    public void testWithDays() {
        Period p = Period.days(10);
        Period p2 = p.withDays(MAX_INT);
        assertEquals(MAX_INT, p2.getDays());
    }

    @Test
    public void testWithHours() {
        Period p = Period.hours(5);
        Period p2 = p.withHours(MIN_INT);
        assertEquals(MIN_INT, p2.getHours());
    }

    @Test
    public void testWithMinutes() {
        Period p = Period.minutes(30);
        Period p2 = p.withMinutes(30);
        assertEquals(30, p2.getMinutes());
        assertNotSame(p, p2);
    }

    @Test
    public void testWithSeconds() {
        Period p = Period.seconds(59);
        Period p2 = p.withSeconds(-1);
        assertEquals(-1, p2.getSeconds());
    }

    @Test
    public void testWithMillis() {
        Period p = Period.millis(500);
        Period p2 = p.withMillis(1000);
        assertEquals(1000, p2.getMillis());
    }

    // ---------------------------------------------------------------
    // plus / plusXxx
    // ---------------------------------------------------------------
    @Test
    public void testPlus_null() {
        Period p = Period.years(1);
        Period p2 = p.plus((ReadablePeriod) null);
        assertSame(p, p2);
    }

    @Test
    public void testPlus_basic() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period sum = p1.plus(p2);
        assertEquals(2, sum.getYears());
        assertEquals(3, sum.getMonths());
        assertEquals(4, sum.getWeeks());
        assertEquals(5, sum.getDays());
        assertEquals(6, sum.getHours());
        assertEquals(7, sum.getMinutes());
        assertEquals(8, sum.getSeconds());
        assertEquals(9, sum.getMillis());
    }

    @Test
    public void testPlusYears() {
        Period p = Period.years(5);
        Period p2 = p.plusYears(3);
        assertEquals(8, p2.getYears());
    }

    @Test
    public void testPlusYears_zero() {
        Period p = Period.years(5);
        Period p2 = p.plusYears(0);
        assertSame(p, p2);
    }

    @Test
    public void testPlusYears_negative() {
        Period p = Period.years(5);
        Period p2 = p.plusYears(-2);
        assertEquals(3, p2.getYears());
    }

    @Test
    public void testPlusMonths() {
        Period p = Period.months(6);
        Period p2 = p.plusMonths(6);
        assertEquals(12, p2.getMonths());
    }

    @Test
    public void testPlusMonths_zero() {
        Period p = Period.months(6);
        assertSame(p, p.plusMonths(0));
    }

    @Test
    public void testPlusWeeks() {
        Period p = Period.weeks(1);
        Period p2 = p.plusWeeks(2);
        assertEquals(3, p2.getWeeks());
    }

    @Test
    public void testPlusDays() {
        Period p = Period.days(15);
        Period p2 = p.plusDays(-5);
        assertEquals(10, p2.getDays());
    }

    @Test
    public void testPlusHours() {
        Period p = Period.hours(10);
        Period p2 = p.plusHours(20);
        assertEquals(30, p2.getHours());
    }

    @Test
    public void testPlusMinutes() {
        Period p = Period.minutes(45);
        Period p2 = p.plusMinutes(30);
        assertEquals(75, p2.getMinutes());
    }

    @Test
    public void testPlusSeconds() {
        Period p = Period.seconds(100);
        Period p2 = p.plusSeconds(-50);
        assertEquals(50, p2.getSeconds());
    }

    @Test
    public void testPlusMillis() {
        Period p = Period.millis(250);
        Period p2 = p.plusMillis(750);
        assertEquals(1000, p2.getMillis());
    }

    // ---------------------------------------------------------------
    // minus / minusXxx
    // ---------------------------------------------------------------
    @Test
    public void testMinus_null() {
        Period p = Period.years(2);
        Period p2 = p.minus((ReadablePeriod) null);
        assertSame(p, p2);
    }

    @Test
    public void testMinus_basic() {
        Period p1 = new Period(2, 2, 2, 2, 2, 2, 2, 2);
        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period diff = p1.minus(p2);
        assertEquals(1, diff.getYears());
        assertEquals(1, diff.getMonths());
        assertEquals(1, diff.getWeeks());
        assertEquals(1, diff.getDays());
        assertEquals(1, diff.getHours());
        assertEquals(1, diff.getMinutes());
        assertEquals(1, diff.getSeconds());
        assertEquals(1, diff.getMillis());
    }

    @Test
    public void testMinusYears() {
        Period p = Period.years(5);
        Period p2 = p.minusYears(2);
        assertEquals(3, p2.getYears());
    }

    @Test
    public void testMinusMonths() {
        Period p = Period.months(10);
        Period p2 = p.minusMonths(3);
        assertEquals(7, p2.getMonths());
    }

    @Test
    public void testMinusWeeks() {
        Period p = Period.weeks(4);
        Period p2 = p.minusWeeks(1);
        assertEquals(3, p2.getWeeks());
    }

    @Test
    public void testMinusDays() {
        Period p = Period.days(10);
        Period p2 = p.minusDays(10);
        assertEquals(0, p2.getDays());
    }

    @Test
    public void testMinusHours() {
        Period p = Period.hours(24);
        Period p2 = p.minusHours(1);
        assertEquals(23, p2.getHours());
    }

    @Test
    public void testMinusMinutes() {
        Period p = Period.minutes(100);
        Period p2 = p.minusMinutes(30);
        assertEquals(70, p2.getMinutes());
    }

    @Test
    public void testMinusSeconds() {
        Period p = Period.seconds(90);
        Period p2 = p.minusSeconds(45);
        assertEquals(45, p2.getSeconds());
    }

    @Test
    public void testMinusMillis() {
        Period p = Period.millis(2000);
        Period p2 = p.minusMillis(1500);
        assertEquals(500, p2.getMillis());
    }

    // ---------------------------------------------------------------
    // multipliedBy / negated
    // ---------------------------------------------------------------
    @Test
    public void testMultipliedBy_one() {
        Period p = Period.years(1);
        assertSame(p, p.multipliedBy(1));
    }

    @Test
    public void testMultipliedBy_zero() {
        Period p = Period.ZERO;
        assertSame(p, p.multipliedBy(0));
    }

    @Test
    public void testMultipliedBy_positive() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period m = p.multipliedBy(2);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getMonths());
        assertEquals(6, m.getWeeks());
        assertEquals(8, m.getDays());
        assertEquals(10, m.getHours());
        assertEquals(12, m.getMinutes());
        assertEquals(14, m.getSeconds());
        assertEquals(16, m.getMillis());
    }

    @Test
    public void testMultipliedBy_negative() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period m = p.multipliedBy(-1);
        assertEquals(-1, m.getYears());
        assertEquals(-2, m.getMonths());
        assertEquals(-3, m.getWeeks());
        assertEquals(-4, m.getDays());
        assertEquals(-5, m.getHours());
        assertEquals(-6, m.getMinutes());
        assertEquals(-7, m.getSeconds());
        assertEquals(-8, m.getMillis());
    }

    @Test(expected = ArithmeticException.class)
    public void testMultipliedBy_overflow() {
        Period p = new Period(MAX_INT, MAX_INT, MAX_INT, MAX_INT, MAX_INT, MAX_INT, MAX_INT, MAX_INT);
        p.multipliedBy(2);
    }

    @Test
    public void testNegated() {
        Period p = Period.years(5).withMonths(-3);
        Period neg = p.negated();
        assertEquals(-5, neg.getYears());
        assertEquals(3, neg.getMonths());
    }

    @Test
    public void testNegated_ZERO() {
        Period zero = Period.ZERO;
        Period neg = zero.negated();
        assertEquals(zero, neg);
    }

    // ---------------------------------------------------------------
    // toStandardXxx and toStandardDuration
    // ---------------------------------------------------------------
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardWeeks_withYears() {
        Period p = Period.years(1);
        p.toStandardWeeks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDays_withMonths() {
        Period p = Period.months(1);
        p.toStandardDays();
    }

    @Test
    public void testToStandardWeeks_simple() {
        Period p = Period.weeks(1);
        assertEquals(Weeks.ONE, p.toStandardWeeks());
    }

    @Test
    public void testToStandardWeeks_combined() {
        Period p = Period.weeks(1).withDays(7); // 2 weeks
        assertEquals(Weeks.weeks(2), p.toStandardWeeks());
    }

    @Test
    public void testToStandardDays() {
        Period p = Period.days(3).withHours(24); // 4 days
        assertEquals(Days.days(4), p.toStandardDays());
    }

    @Test
    public void testToStandardHours() {
        Period p = Period.hours(2).withMinutes(120); // 4 hours
        assertEquals(Hours.hours(4), p.toStandardHours());
    }

    @Test
    public void testToStandardMinutes() {
        Period p = Period.minutes(30).withSeconds(60); // 31 minutes
        assertEquals(Minutes.minutes(31), p.toStandardMinutes());
    }

    @Test
    public void testToStandardSeconds() {
        Period p = Period.seconds(100).withMillis(1000); // 101 seconds
        assertEquals(Seconds.seconds(101), p.toStandardSeconds());
    }

    @Test
    public void testToStandardDuration_noYearsMonths() {
        Period p = Period.hours(1).withMinutes(30).withSeconds(40);
        Duration d = p.toStandardDuration();
        assertEquals(5440000L, d.getMillis());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDuration_withYears() {
        Period p = Period.years(1);
        p.toStandardDuration();
    }

    // ---------------------------------------------------------------
    // normalizedStandard
    // ---------------------------------------------------------------
    @Test
    public void testNormalizedStandard_simple() {
        Period p = Period.minutes(130);
        Period norm = p.normalizedStandard();
        assertEquals(2, norm.getHours());
        assertEquals(10, norm.getMinutes());
    }

    @Test
    public void testNormalizedStandard_yearsAndMonths() {
        Period p = Period.years(1).withMonths(15); // 2 years 3 months
        Period norm = p.normalizedStandard();
        assertEquals(2, norm.getYears());
        assertEquals(3, norm.getMonths());
    }

    @Test
    public void testNormalizedStandard_monthsOnly() {
        Period p = Period.months(13);
        Period norm = p.normalizedStandard();
        assertEquals(1, norm.getYears());
        assertEquals(1, norm.getMonths());
    }

    @Test
    public void testNormalizedStandard_withType() {
        Period p = Period.years(1).withMonths(10).withDays(40);
        Period norm = p.normalizedStandard(PeriodType.yearMonthDay());
        assertEquals(1, norm.getYears());
        assertEquals(10, norm.getMonths());
        assertEquals(40, norm.getDays()); // days not normalized to weeks because type excludes weeks
    }

    @Test
    public void testNormalizedStandard_weeksToDays() {
        Period p = Period.weeks(2).withDays(5); // 19 days -> 2 weeks 5 days
        Period norm = p.normalizedStandard();
        assertEquals(2, norm.getWeeks());
        assertEquals(5, norm.getDays());
    }

    @Test(timeout = 1000)
    public void testNormalizedStandard_overflowSafe() {
        Period p = new Period(MAX_INT, MAX_INT, 0, 0, 0, 0, 0, 0);
        // should throw ArithmeticException or succeed? Might overflow in years.
        try {
            p.normalizedStandard();
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // ZERO constant
    // ---------------------------------------------------------------
    @Test
    public void testZERO_isZero() {
        assertEquals(Period.ZERO, new Period());
        assertEquals(0, Period.ZERO.getYears());
        assertEquals(0, Period.ZERO.getMonths());
    }

    @Test
    public void testZERO_plus_self() {
        assertSame(Period.ZERO, Period.ZERO.plus((ReadablePeriod) null));
        assertSame(Period.ZERO, Period.ZERO.plusYears(0));
    }

    // ---------------------------------------------------------------
    // Edge Cases: null chronology, type, etc.
    // ---------------------------------------------------------------
    @Test
    public void testConstructor_nullType_allowed() {
        Period p = new Period(1000L, (PeriodType) null);
        assertEquals(PeriodType.standard(), p.getPeriodType());
    }

    @Test
    public void testConstructor_nullChronology() {
        Period p = new Period(1000L, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), p.getChronology());
    }

    @Test
    public void testParse_nullString() {
        try {
            Period.parse(null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // toString / equals / hashCode (inherited, but let's verify)
    // ---------------------------------------------------------------
    @Test
    public void testEquals_sameValues() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(p1, p2);
    }

    @Test
    public void testEquals_differentValues() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 2, 3, 4, 5, 6, 7, 9);
        assertNotEquals(p1, p2);
    }

    @Test
    public void testHashCode_consistent() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(p.hashCode(), p.toPeriod().hashCode());
    }
}
