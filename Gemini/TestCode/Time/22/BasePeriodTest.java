package org.joda.time.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.MutablePeriod;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.YearMonth;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasePeriodTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(10000000000L);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.setCurrentMillisSystem();
    }

    // Concrete mock implementation of BasePeriod exposing protected methods for testing
    private static class MockBasePeriod extends BasePeriod {
        private static final long serialVersionUID = 1L;

        public MockBasePeriod(int years, int months, int weeks, int days,
                              int hours, int minutes, int seconds, int millis,
                              PeriodType type) {
            super(years, months, weeks, days, hours, minutes, seconds, millis, type);
        }

        public MockBasePeriod(long startInstant, long endInstant, PeriodType type, Chronology chrono) {
            super(startInstant, endInstant, type, chrono);
        }

        public MockBasePeriod(ReadableInstant startInstant, ReadableInstant endInstant, PeriodType type) {
            super(startInstant, endInstant, type);
        }

        public MockBasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type) {
            super(start, end, type);
        }

        public MockBasePeriod(ReadableInstant startInstant, ReadableDuration duration, PeriodType type) {
            super(startInstant, duration, type);
        }

        public MockBasePeriod(ReadableDuration duration, ReadableInstant endInstant, PeriodType type) {
            super(duration, endInstant, type);
        }

        public MockBasePeriod(long duration) {
            super(duration);
        }

        public MockBasePeriod(long duration, PeriodType type, Chronology chrono) {
            super(duration, type, chrono);
        }

        public MockBasePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }

        public MockBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }

        // Exposing protected methods
        @Override
        public PeriodType checkPeriodType(PeriodType type) {
            return super.checkPeriodType(type);
        }

        @Override
        public void setPeriod(ReadablePeriod period) {
            super.setPeriod(period);
        }

        @Override
        public void setPeriod(int years, int months, int weeks, int days,
                              int hours, int minutes, int seconds, int millis) {
            super.setPeriod(years, months, weeks, days, hours, minutes, seconds, millis);
        }

        @Override
        public void setField(DurationFieldType field, int value) {
            super.setField(field, value);
        }

        @Override
        public void setFieldInto(int[] values, DurationFieldType field, int value) {
            super.setFieldInto(values, field, value);
        }

        @Override
        public void addField(DurationFieldType field, int value) {
            super.addField(field, value);
        }

        @Override
        public void addFieldInto(int[] values, DurationFieldType field, int value) {
            super.addFieldInto(values, field, value);
        }

        @Override
        public void mergePeriod(ReadablePeriod period) {
            super.mergePeriod(period);
        }

        @Override
        public int[] mergePeriodInto(int[] values, ReadablePeriod period) {
            return super.mergePeriodInto(values, period);
        }

        @Override
        public void addPeriod(ReadablePeriod period) {
            super.addPeriod(period);
        }

        @Override
        public int[] addPeriodInto(int[] values, ReadablePeriod period) {
            return super.addPeriodInto(values, period);
        }

        @Override
        public void setValue(int index, int value) {
            super.setValue(index, value);
        }

        @Override
        public void setValues(int[] values) {
            super.setValues(values);
        }
    }

    private static class MockReadWritablePeriod extends BasePeriod implements ReadWritablePeriod {
        private static final long serialVersionUID = 1L;

        public MockReadWritablePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }

        public void clear() {
            setValues(new int[size()]);
        }

        public void setValue(int index, int value) {
            super.setValue(index, value);
        }

        public void set(DurationFieldType field, int value) {
            setField(field, value);
        }

        public void setPeriod(ReadablePeriod period) {
            super.setPeriod(period);
        }

        public void setPeriod(int years, int months, int weeks, int days,
                              int hours, int minutes, int seconds, int millis) {
            super.setPeriod(years, months, weeks, days, hours, minutes, seconds, millis);
        }

        public void setPeriod(org.joda.time.ReadableInterval interval) {
            if (interval != null) {
                setPeriod(interval.toPeriod(getPeriodType()));
            }
        }

        public void add(DurationFieldType field, int value) {
            addField(field, value);
        }

        public void add(ReadablePeriod period) {
            addPeriod(period);
        }

        public void add(int years, int months, int weeks, int days,
                        int hours, int minutes, int seconds, int millis) {
            addPeriod(new Period(years, months, weeks, days, hours, minutes, seconds, millis, getPeriodType()));
        }

        public void add(org.joda.time.ReadableInterval interval) {
            if (interval != null) {
                addPeriod(interval.toPeriod(getPeriodType()));
            }
        }

        public void setYears(int years) { setField(DurationFieldType.years(), years); }
        public void addYears(int years) { addField(DurationFieldType.years(), years); }
        public void setMonths(int months) { setField(DurationFieldType.months(), months); }
        public void addMonths(int months) { addField(DurationFieldType.months(), months); }
        public void setWeeks(int weeks) { setField(DurationFieldType.weeks(), weeks); }
        public void addWeeks(int weeks) { addField(DurationFieldType.weeks(), weeks); }
        public void setDays(int days) { setField(DurationFieldType.days(), days); }
        public void addDays(int days) { addField(DurationFieldType.days(), days); }
        public void setHours(int hours) { setField(DurationFieldType.hours(), hours); }
        public void addHours(int hours) { addField(DurationFieldType.hours(), hours); }
        public void setMinutes(int minutes) { setField(DurationFieldType.minutes(), minutes); }
        public void addMinutes(int minutes) { addField(DurationFieldType.minutes(), minutes); }
        public void setSeconds(int seconds) { setField(DurationFieldType.seconds(), seconds); }
        public void addSeconds(int seconds) { addField(DurationFieldType.seconds(), seconds); }
        public void setMillis(int millis) { setField(DurationFieldType.millis(), millis); }
        public void addMillis(int millis) { addField(DurationFieldType.millis(), millis); }
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: 8 ints + PeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_8ints_PeriodType() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(8, p.size());
        assertEquals(1, p.getValue(0));
        assertEquals(2, p.getValue(1));
        assertEquals(3, p.getValue(2));
        assertEquals(4, p.getValue(3));
        assertEquals(5, p.getValue(4));
        assertEquals(6, p.getValue(5));
        assertEquals(7, p.getValue(6));
        assertEquals(8, p.getValue(7));
    }

    @Test
    public void testConstructor_8ints_NullPeriodType() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, null);
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(1, p.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_8ints_UnsupportedFieldNonZero() {
        // PeriodType.yearMonthDay() does not support hours
        new MockBasePeriod(1, 2, 0, 4, 5, 0, 0, 0, PeriodType.yearMonthDay());
    }

    @Test
    public void testConstructor_8ints_UnsupportedFieldZero() {
        // PeriodType.yearMonthDay() does not support hours/minutes/seconds/millis, but all are 0
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        assertEquals(3, p.size());
        assertEquals(1, p.getValue(0));
        assertEquals(2, p.getValue(1));
        assertEquals(4, p.getValue(2));
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: long, long, PeriodType, Chronology
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_long_long_PeriodType_Chronology() {
        long start = 0L;
        long end = 1000L * 60 * 60 * 24 * 40; // 40 days in millis
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.days(), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.days(), p.getPeriodType());
        assertEquals(40, p.getValue(0));
    }

    @Test
    public void testConstructor_long_long_NullType_NullChrono() {
        long start = 0L;
        long end = 1000L * 60 * 60 * 24; // 1 day
        MockBasePeriod p = new MockBasePeriod(start, end, null, null);
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(1, p.getDays());
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: ReadableInstant, ReadableInstant, PeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_PeriodType() {
        Instant start = new Instant(0L);
        Instant end = new Instant(2000L);
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.seconds());
        assertEquals(2, p.getValue(0));
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_BothNull() {
        MockBasePeriod p = new MockBasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.standard());
        assertEquals(8, p.size());
        for (int i = 0; i < p.size(); i++) {
            assertEquals(0, p.getValue(i));
        }
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_StartNull() {
        // Current fixed millis = 10000000000L
        Instant end = new Instant(10000005000L);
        MockBasePeriod p = new MockBasePeriod(null, end, PeriodType.seconds());
        assertEquals(5, p.getValue(0));
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableInstant_EndNull() {
        Instant start = new Instant(10000000000L - 10000L);
        MockBasePeriod p = new MockBasePeriod(start, null, PeriodType.seconds());
        assertEquals(10, p.getValue(0));
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: ReadablePartial, ReadablePartial, PeriodType
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_StartNull() {
        new MockBasePeriod((ReadablePartial) null, new LocalDate(2020, 1, 1), PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_EndNull() {
        new MockBasePeriod(new LocalDate(2020, 1, 1), (ReadablePartial) null, PeriodType.standard());
    }

    @Test
    public void testConstructor_ReadablePartial_BaseLocalSameClass() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2021, 3, 5);
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(4, p.getDays());
    }

    @Test
    public void testConstructor_ReadablePartial_BaseLocalLocalTime() {
        LocalTime start = new LocalTime(10, 0, 0);
        LocalTime end = new LocalTime(12, 30, 0);
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.time());
        assertEquals(2, p.getHours());
        assertEquals(30, p.getMinutes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_BaseLocalDifferentClasses() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime end = new LocalTime(12, 0, 0);
        // BaseLocal but start.getClass() != end.getClass(), different fields
        new MockBasePeriod(start, end, PeriodType.standard());
    }

    @Test
    public void testConstructor_ReadablePartial_NonBaseLocalContiguous() {
        YearMonth start = new YearMonth(2020, 1);
        YearMonth end = new YearMonth(2022, 5);
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(2, p.getYears());
        assertEquals(4, p.getMonths());
    }

    @Test
    public void testConstructor_ReadablePartial_MonthDayContiguous() {
        MonthDay start = new MonthDay(2, 1);
        MonthDay end = new MonthDay(5, 10);
        MockBasePeriod p = new MockBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(3, p.getMonths());
        assertEquals(9, p.getDays());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_DifferentSizes() {
        YearMonth start = new YearMonth(2020, 1);
        Partial end = new Partial(DateTimeFieldType.year(), 2021);
        new MockBasePeriod(start, end, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_DifferentFieldTypes() {
        Partial start = new Partial(DateTimeFieldType.monthOfYear(), 1);
        Partial end = new Partial(DateTimeFieldType.dayOfMonth(), 2);
        new MockBasePeriod(start, end, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_NotContiguous() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.hourOfDay()};
        int[] values1 = new int[] {2020, 10};
        int[] values2 = new int[] {2021, 12};
        Partial start = new Partial(types, values1);
        Partial end = new Partial(types, values2);
        new MockBasePeriod(start, end, PeriodType.standard());
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: ReadableInstant, ReadableDuration, PeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_ReadableInstant_ReadableDuration_PeriodType() {
        Instant start = new Instant(1000L);
        Duration duration = new Duration(5000L);
        MockBasePeriod p = new MockBasePeriod(start, duration, PeriodType.seconds());
        assertEquals(5, p.getValue(0));
    }

    @Test
    public void testConstructor_ReadableInstant_ReadableDuration_Nulls() {
        MockBasePeriod p = new MockBasePeriod((ReadableInstant) null, (ReadableDuration) null, PeriodType.standard());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: ReadableDuration, ReadableInstant, PeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_ReadableDuration_ReadableInstant_PeriodType() {
        Duration duration = new Duration(5000L);
        Instant end = new Instant(6000L);
        MockBasePeriod p = new MockBasePeriod(duration, end, PeriodType.seconds());
        assertEquals(5, p.getValue(0));
    }

    @Test
    public void testConstructor_ReadableDuration_ReadableInstant_Nulls() {
        MockBasePeriod p = new MockBasePeriod((ReadableDuration) null, (ReadableInstant) null, PeriodType.standard());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: long duration
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_longDuration() {
        long millis = 3600000L * 2 + 60000L * 3 + 1000L * 4 + 5;
        MockBasePeriod p = new MockBasePeriod(millis);
        assertEquals(PeriodType.time(), p.getPeriodType());
        assertEquals(2, p.getHours());
        assertEquals(3, p.getMinutes());
        assertEquals(4, p.getSeconds());
        assertEquals(5, p.getMillis());
    }

    @Test
    public void testConstructor_longDuration_PeriodType_Chronology() {
        long millis = 3600000L * 5;
        MockBasePeriod p = new MockBasePeriod(millis, PeriodType.hours(), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.hours(), p.getPeriodType());
        assertEquals(5, p.getValue(0));
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: Object, PeriodType, Chronology
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_Object_PeriodType_Chronology() {
        Period orig = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        MockBasePeriod p = new MockBasePeriod(orig, PeriodType.standard(), null);
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
    public void testConstructor_Object_NullType() {
        Period orig = new Period(1, 2, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        MockBasePeriod p = new MockBasePeriod(orig, null, null);
        assertEquals(PeriodType.yearMonthDay(), p.getPeriodType());
        assertEquals(1, p.getValue(0));
        assertEquals(2, p.getValue(1));
    }

    @Test
    public void testConstructor_Object_ReadWritableSubclass() {
        Period orig = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        MockReadWritablePeriod rwp = new MockReadWritablePeriod(orig, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(1, rwp.getYears());
        assertEquals(2, rwp.getMonths());
        assertEquals(3, rwp.getWeeks());
        assertEquals(4, rwp.getDays());
        assertEquals(5, rwp.getHours());
        assertEquals(6, rwp.getMinutes());
        assertEquals(7, rwp.getSeconds());
        assertEquals(8, rwp.getMillis());
    }

    // -----------------------------------------------------------------------
    // Tests for Constructor: int[], PeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor_intArray_PeriodType() {
        int[] vals = new int[] {10, 20};
        MockBasePeriod p = new MockBasePeriod(vals, PeriodType.yearMonthDay());
        assertEquals(PeriodType.yearMonthDay(), p.getPeriodType());
        assertEquals(10, p.getValue(0));
        assertEquals(20, p.getValue(1));
    }

    // -----------------------------------------------------------------------
    // Tests for getFieldType, getValue, size, checkPeriodType
    // -----------------------------------------------------------------------
    @Test
    public void testFieldAccessors() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(8, p.size());
        assertEquals(DurationFieldType.years(), p.getFieldType(0));
        assertEquals(DurationFieldType.months(), p.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), p.getFieldType(2));
        assertEquals(DurationFieldType.days(), p.getFieldType(3));
        assertEquals(DurationFieldType.hours(), p.getFieldType(4));
        assertEquals(DurationFieldType.minutes(), p.getFieldType(5));
        assertEquals(DurationFieldType.seconds(), p.getFieldType(6));
        assertEquals(DurationFieldType.millis(), p.getFieldType(7));

        assertEquals(1, p.getValue(0));
        assertEquals(8, p.getValue(7));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_OutOfBounds() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.getFieldType(8);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_OutOfBounds() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.getValue(8);
    }

    @Test
    public void testCheckPeriodType() {
        MockBasePeriod p = new MockBasePeriod(new int[8], PeriodType.standard());
        assertSame(PeriodType.standard(), p.checkPeriodType(PeriodType.standard()));
        assertSame(PeriodType.standard(), p.checkPeriodType(null));
    }

    // -----------------------------------------------------------------------
    // Tests for toDurationFrom & toDurationTo
    // -----------------------------------------------------------------------
    @Test
    public void testToDurationFrom() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 1, 2, 0, 0, 0, PeriodType.standard());
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, ISOChronology.getInstanceUTC());
        Duration duration = p.toDurationFrom(start);
        long expectedMillis = (24L + 2L) * 3600L * 1000L;
        assertEquals(expectedMillis, duration.getMillis());
    }

    @Test
    public void testToDurationFrom_NullStart() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        // Current fixed millis = 10000000000L
        Duration duration = p.toDurationFrom(null);
        assertEquals(3600000L, duration.getMillis());
    }

    @Test
    public void testToDurationTo() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 1, 2, 0, 0, 0, PeriodType.standard());
        DateTime end = new DateTime(2020, 1, 2, 2, 0, 0, ISOChronology.getInstanceUTC());
        Duration duration = p.toDurationTo(end);
        long expectedMillis = (24L + 2L) * 3600L * 1000L;
        assertEquals(expectedMillis, duration.getMillis());
    }

    @Test
    public void testToDurationTo_NullEnd() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        Duration duration = p.toDurationTo(null);
        assertEquals(3600000L, duration.getMillis());
    }

    // -----------------------------------------------------------------------
    // Tests for setPeriod(ReadablePeriod) and setPeriod(8 ints)
    // -----------------------------------------------------------------------
    @Test
    public void testSetPeriod_ReadablePeriod_Null() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setPeriod((ReadablePeriod) null);
        for (int i = 0; i < p.size(); i++) {
            assertEquals(0, p.getValue(i));
        }
    }

    @Test
    public void testSetPeriod_ReadablePeriod_NonNull() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        Period updated = new Period(10, 20, 30, 40, 50, 60, 70, 80);
        p.setPeriod(updated);
        assertEquals(10, p.getValue(0));
        assertEquals(80, p.getValue(7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPeriod_ReadablePeriod_UnsupportedFieldNonZero() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.days());
        Period withYears = new Period(5, 0, 0, 1, 0, 0, 0, 0);
        p.setPeriod(withYears);
    }

    @Test
    public void testSetPeriod_ReadablePeriod_UnsupportedFieldZero() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.days());
        Period withZeroYears = new Period(0, 0, 0, 5, 0, 0, 0, 0);
        p.setPeriod(withZeroYears);
        assertEquals(5, p.getValue(0));
    }

    @Test
    public void testSetPeriod_8ints() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.setPeriod(8, 7, 6, 5, 4, 3, 2, 1);
        assertEquals(8, p.getValue(0));
        assertEquals(1, p.getValue(7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPeriod_8ints_UnsupportedNonZero() {
        MockBasePeriod p = new MockBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.hours());
        p.setPeriod(1, 0, 0, 0, 5, 0, 0, 0);
    }

    // -----------------------------------------------------------------------
    // Tests for setField, setFieldInto
    // -----------------------------------------------------------------------
    @Test
    public void testSetField_Supported() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setField(DurationFieldType.years(), 99);
        assertEquals(99, p.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetField_UnsupportedNonZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        p.setField(DurationFieldType.hours(), 5);
    }

    @Test
    public void testSetField_UnsupportedZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        p.setField(DurationFieldType.hours(), 0);
        assertEquals(1, p.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetField_NullField() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setField(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetField_NullField_ZeroValue() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setField(null, 0);
    }

    @Test
    public void testSetFieldInto() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        int[] values = new int[8];
        p.setFieldInto(values, DurationFieldType.months(), 15);
        assertEquals(15, values[1]);
    }

    // -----------------------------------------------------------------------
    // Tests for addField, addFieldInto
    // -----------------------------------------------------------------------
    @Test
    public void testAddField_Supported() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.addField(DurationFieldType.years(), 10);
        assertEquals(11, p.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddField_UnsupportedNonZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        p.addField(DurationFieldType.hours(), 5);
    }

    @Test
    public void testAddField_UnsupportedZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        p.addField(DurationFieldType.hours(), 0);
        assertEquals(1, p.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddField_NullField() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.addField(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddField_NullField_ZeroValue() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.addField(null, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddField_Overflow() {
        MockBasePeriod p = new MockBasePeriod(Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.addField(DurationFieldType.years(), 1);
    }

    @Test
    public void testAddFieldInto() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        int[] values = new int[] {5, 6, 7, 8, 9, 10, 11, 12};
        p.addFieldInto(values, DurationFieldType.seconds(), 20);
        assertEquals(31, values[6]);
    }

    // -----------------------------------------------------------------------
    // Tests for mergePeriod, mergePeriodInto
    // -----------------------------------------------------------------------
    @Test
    public void testMergePeriod_Null() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.mergePeriod(null);
        assertEquals(1, p.getYears());
        assertEquals(8, p.getMillis());
    }

    @Test
    public void testMergePeriod_NonNull() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        Period toMerge = new Period().withYears(50).withMinutes(30);
        p.mergePeriod(toMerge);
        assertEquals(50, p.getYears());
        assertEquals(0, p.getMonths()); // toMerge has 0 months, overwrites
        assertEquals(30, p.getMinutes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergePeriod_UnsupportedNonZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period toMerge = new Period().withHours(5);
        p.mergePeriod(toMerge);
    }

    @Test
    public void testMergePeriodInto() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        int[] values = new int[8];
        int[] result = p.mergePeriodInto(values, new Period().withHours(12));
        assertSame(values, result);
        assertEquals(12, values[4]);
    }

    // -----------------------------------------------------------------------
    // Tests for addPeriod, addPeriodInto
    // -----------------------------------------------------------------------
    @Test
    public void testAddPeriod_Null() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.addPeriod(null);
        assertEquals(1, p.getYears());
    }

    @Test
    public void testAddPeriod_NonNull() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        Period toAdd = new Period().withYears(5).withDays(10);
        p.addPeriod(toAdd);
        assertEquals(6, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(14, p.getDays());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPeriod_UnsupportedNonZero() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period toAdd = new Period().withHours(5);
        p.addPeriod(toAdd);
    }

    @Test
    public void testAddPeriod_UnsupportedZeroIgnored() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 0, 4, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period toAdd = new Period().withYears(3).withHours(0);
        p.addPeriod(toAdd);
        assertEquals(4, p.getYears());
    }

    @Test
    public void testAddPeriodInto() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        int[] values = new int[8];
        int[] result = p.addPeriodInto(values, new Period().withDays(7));
        assertSame(values, result);
        // p.getValue(3) is 4, plus 7 is 11
        assertEquals(11, values[3]);
    }

    // -----------------------------------------------------------------------
    // Tests for setValue and setValues
    // -----------------------------------------------------------------------
    @Test
    public void testSetValue() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setValue(2, 99);
        assertEquals(99, p.getValue(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValue_IndexOutOfBounds() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.setValue(10, 99);
    }

    @Test
    public void testSetValues() {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        int[] newVals = new int[] {10, 20, 30, 40, 50, 60, 70, 80};
        p.setValues(newVals);
        for (int i = 0; i < 8; i++) {
            assertEquals((i + 1) * 10, p.getValue(i));
        }
    }

    // -----------------------------------------------------------------------
    // Tests for Serialization
    // -----------------------------------------------------------------------
    @Test
    public void testSerialization() throws Exception {
        MockBasePeriod p = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MockBasePeriod deserialized = (MockBasePeriod) ois.readObject();
        ois.close();

        assertEquals(p.getPeriodType(), deserialized.getPeriodType());
        assertEquals(p.size(), deserialized.size());
        for (int i = 0; i < p.size(); i++) {
            assertEquals(p.getValue(i), deserialized.getValue(i));
        }
    }

    // -----------------------------------------------------------------------
    // Tests for MockReadWritablePeriod delegate methods
    // -----------------------------------------------------------------------
    @Test
    public void testMockReadWritablePeriodDelegates() {
        MockReadWritablePeriod rwp = new MockReadWritablePeriod(new Period(), PeriodType.standard(), null);
        rwp.setYears(1);
        rwp.setMonths(2);
        rwp.setWeeks(3);
        rwp.setDays(4);
        rwp.setHours(5);
        rwp.setMinutes(6);
        rwp.setSeconds(7);
        rwp.setMillis(8);
        assertEquals(1, rwp.getYears());
        assertEquals(2, rwp.getMonths());
        assertEquals(3, rwp.getWeeks());
        assertEquals(4, rwp.getDays());
        assertEquals(5, rwp.getHours());
        assertEquals(6, rwp.getMinutes());
        assertEquals(7, rwp.getSeconds());
        assertEquals(8, rwp.getMillis());

        rwp.addYears(1);
        rwp.addMonths(1);
        rwp.addWeeks(1);
        rwp.addDays(1);
        rwp.addHours(1);
        rwp.addMinutes(1);
        rwp.addSeconds(1);
        rwp.addMillis(1);
        assertEquals(2, rwp.getYears());
        assertEquals(3, rwp.getMonths());
        assertEquals(4, rwp.getWeeks());
        assertEquals(5, rwp.getDays());
        assertEquals(6, rwp.getHours());
        assertEquals(7, rwp.getMinutes());
        assertEquals(8, rwp.getSeconds());
        assertEquals(9, rwp.getMillis());

        rwp.clear();
        assertEquals(0, rwp.getYears());
        assertEquals(0, rwp.getMillis());

        rwp.set(DurationFieldType.years(), 10);
        rwp.add(DurationFieldType.years(), 5);
        assertEquals(15, rwp.getYears());

        rwp.add(1, 1, 1, 1, 1, 1, 1, 1);
        assertEquals(16, rwp.getYears());

        org.joda.time.Interval interval = new org.joda.time.Interval(0L, 1000L);
        rwp.setPeriod(interval);
        assertEquals(1, rwp.getSeconds());

        rwp.add(interval);
        assertEquals(2, rwp.getSeconds());

        rwp.setPeriod((org.joda.time.ReadableInterval) null);
        rwp.add((org.joda.time.ReadableInterval) null);
        assertEquals(2, rwp.getSeconds());
    }
}
