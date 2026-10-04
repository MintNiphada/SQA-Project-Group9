package org.joda.time.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;

import java.io.Serializable;

public class BaseSingleFieldPeriodTest {

    // concrete implementations for testing
    static class TestDaysPeriod extends BaseSingleFieldPeriod implements Serializable {
        private static final long serialVersionUID = 1L;
        public TestDaysPeriod(int period) {
            super(period);
        }
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }
    }

    static class TestHoursPeriod extends BaseSingleFieldPeriod implements Serializable {
        private static final long serialVersionUID = 1L;
        public TestHoursPeriod(int period) {
            super(period);
        }
        public DurationFieldType getFieldType() {
            return DurationFieldType.hours();
        }
        public PeriodType getPeriodType() {
            return PeriodType.hours();
        }
    }

    static class TestMonthsPeriod extends BaseSingleFieldPeriod implements Serializable {
        private static final long serialVersionUID = 1L;
        public TestMonthsPeriod(int period) {
            super(period);
        }
        public DurationFieldType getFieldType() {
            return DurationFieldType.months();
        }
        public PeriodType getPeriodType() {
            return PeriodType.months();
        }
    }

    // for non-contiguous partial testing
    static class NonContiguousPartial implements ReadablePartial {
        private final int year;
        private final int dayOfMonth;
        NonContiguousPartial(int year, int dayOfMonth) {
            this.year = year;
            this.dayOfMonth = dayOfMonth;
        }
        public int size() { return 2; }
        public Chronology getChronology() {
            return ISOChronology.getInstanceUTC();
        }
        public DateTimeField getField(int index) {
            if (index == 0) return getChronology().year();
            if (index == 1) return getChronology().dayOfMonth();
            throw new IndexOutOfBoundsException();
        }
        public DurationFieldType getFieldType(int index) {
            if (index == 0) return DurationFieldType.years();
            if (index == 1) return DurationFieldType.days();
            throw new IndexOutOfBoundsException();
        }
        public int getValue(int index) {
            if (index == 0) return year;
            if (index == 1) return dayOfMonth;
            throw new IndexOutOfBoundsException();
        }
        public boolean isSupported(DurationFieldType type) { return false; }
        public DateTime toDateTime(ReadableInstant base) { return null; }
        public ReadablePartial toPartial(Chronology chrono) { return this; }
        public int compareTo(ReadablePartial partial) {
            if (partial == null || partial.size() != 2) return 0;
            int y = partial.getValue(0);
            if (year != y) return year - y;
            int d = partial.getValue(1);
            return dayOfMonth - d;
        }
    }

    // helper
    private ReadableInstant createInstant(long millis) {
        return new DateTime(millis, ISOChronology.getInstanceUTC());
    }

    // ---------- between(ReadableInstant) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testBetweenInstantNullStart() {
        BaseSingleFieldPeriod.between(null, createInstant(1000), DurationFieldType.days());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenInstantNullEnd() {
        BaseSingleFieldPeriod.between(createInstant(0), null, DurationFieldType.days());
    }

    @Test
    public void testBetweenInstantValidPositive() {
        long startMillis = 1000;
        long endMillis = 3 * 86400000L + 1000; // 3 days later
        int days = BaseSingleFieldPeriod.between(
                createInstant(startMillis), createInstant(endMillis), DurationFieldType.days());
        assertEquals(3, days);
    }

    @Test
    public void testBetweenInstantValidNegative() {
        long startMillis = 3 * 86400000L;
        long endMillis = 1000;
        int days = BaseSingleFieldPeriod.between(
                createInstant(startMillis), createInstant(endMillis), DurationFieldType.days());
        assertEquals(-3, days);
    }

    @Test
    public void testBetweenInstantSameInstant() {
        long millis = 123456789L;
        int days = BaseSingleFieldPeriod.between(
                createInstant(millis), createInstant(millis), DurationFieldType.days());
        assertEquals(0, days);
    }

    @Test
    public void testBetweenInstantDifferentFieldHours() {
        long start = 0;
        long end = 2 * 3600000L;
        int hours = BaseSingleFieldPeriod.between(
                createInstant(start), createInstant(end), DurationFieldType.hours());
        assertEquals(2, hours);
    }

    // ---------- between(ReadablePartial) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testBetweenPartialNullStart() {
        BaseSingleFieldPeriod.between(
                null, new LocalDate(2000, 1, 1), Days.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenPartialNullEnd() {
        BaseSingleFieldPeriod.between(
                new LocalDate(2000, 1, 1), null, Days.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenPartialDifferentSizes() {
        ReadablePartial start = new LocalDate(2000, 1, 1);
        ReadablePartial end = new NonContiguousPartial(2000, 20); // size 2
        BaseSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenPartialDifferentFieldTypesSameSize() {
        // MonthDay and LocalDate both size 2 but different field types order
        ReadablePartial start = new MonthDay(5, 10);
        ReadablePartial end = new YearMonth(2000, 6);
        BaseSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenPartialNonContiguous() {
        ReadablePartial start = new NonContiguousPartial(2000, 15);
        ReadablePartial end = new NonContiguousPartial(2000, 25);
        BaseSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    @Test
    public void testBetweenPartialValidContiguous() {
        LocalDate start = new LocalDate(2000, 1, 1);
        LocalDate end = new LocalDate(2000, 1, 4);
        int days = BaseSingleFieldPeriod.between(start, end, Days.ZERO);
        assertEquals(3, days);
    }

    @Test
    public void testBetweenPartialSameDate() {
        LocalDate date = new LocalDate(2005, 6, 9);
        int days = BaseSingleFieldPeriod.between(date, date, Days.ZERO);
        assertEquals(0, days);
    }

    // ---------- standardPeriodIn ----------
    @Test
    public void testStandardPeriodInNullPeriod() {
        assertEquals(0, BaseSingleFieldPeriod.standardPeriodIn(null, 3600000L));
    }

    @Test
    public void testStandardPeriodInZeroValuedPeriod() {
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(0, BaseSingleFieldPeriod.standardPeriodIn(p, 3600000L));
    }

    @Test
    public void testStandardPeriodInHoursToHours() {
        Period p = Period.hours(5);
        assertEquals(5, BaseSingleFieldPeriod.standardPeriodIn(p, 3600000L));
    }

    @Test
    public void testStandardPeriodInHoursToDays() {
        Period p = Period.hours(48);
        assertEquals(2, BaseSingleFieldPeriod.standardPeriodIn(p, 86400000L));
    }

    @Test
    public void testStandardPeriodInCombinedPreciseFields() {
        Period p = new Period(0, 0, 0, 2, 12, 30, 30, 0); // 2 days, 12h, 30m, 30s
        long millisPerHour = 3600000L;
        long totalHours = 2*24 + 12 + 0 + 0; // = 60, no minutes and seconds contribute? Actually minutes:30 -> 0.5 hours, not integer division. Our method uses millis per unit, so 30 minutes = 1800000 ms, 30*60000? Wait safeMultiply(60000L, 30) = 1800000 ms, duration added. Dividing by 3600000 yields back integer division, so 0. So total duration millis = 2*86400000 + 12*3600000 + 30*60000 + 30*1000 = ... divided by 3600000 = (2*24 + 12) hours + (1800000+30000)/3600000 = 60 + 1830000/3600000 = 60.508... -> int = 60. So expected 60.
        assertEquals(60, BaseSingleFieldPeriod.standardPeriodIn(p, millisPerHour));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodInImpreciseMonths() {
        Period p = Period.months(1);
        BaseSingleFieldPeriod.standardPeriodIn(p, 86400000L); // days
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodInImpreciseYears() {
        Period p = Period.years(2);
        BaseSingleFieldPeriod.standardPeriodIn(p, 3600000L);
    }

    @Test(expected = ArithmeticException.class)
    public void testStandardPeriodInOverflowInMultiply() {
        // huge hours causing overflow in safeMultiply
        Period p = Period.hours(Integer.MAX_VALUE);
        long smallMillisPerUnit = 1L;
        BaseSingleFieldPeriod.standardPeriodIn(p, smallMillisPerUnit);
    }

    @Test(expected = ArithmeticException.class)
    public void testStandardPeriodInOverflowInAdd() {
        // create period with many different fields summing to overflow
        Period p = new Period();
        // set huge values for precision fields (weeks, days, hours, minutes, seconds)
        p = p.withWeeks(Integer.MAX_VALUE / 1000);
        p = p.withDays(Integer.MAX_VALUE / 1000);
        p = p.withHours(Integer.MAX_VALUE / 1000);
        // this sum may overflow safeAdd
        BaseSingleFieldPeriod.standardPeriodIn(p, 1L);
    }

    // ---------- constructor, getValue, setValue ----------
    @Test
    public void testConstructorAndGetValue() {
        TestDaysPeriod p = new TestDaysPeriod(42);
        assertEquals(42, p.getValue());
    }

    @Test
    public void testSetValue() {
        TestDaysPeriod p = new TestDaysPeriod(0);
        assertEquals(0, p.getValue());
        p.setValue(99);
        assertEquals(99, p.getValue());
    }

    // ---------- size ----------
    @Test
    public void testSize() {
        assertEquals(1, new TestDaysPeriod(1).size());
    }

    // ---------- getFieldType(int) ----------
    @Test
    public void testGetFieldTypeIntZero() {
        DurationFieldType type = new TestDaysPeriod(1).getFieldType(0);
        assertEquals(DurationFieldType.days(), type);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldTypeIntOutOfBounds() {
        new TestDaysPeriod(1).getFieldType(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldTypeIntNegative() {
        new TestDaysPeriod(1).getFieldType(-1);
    }

    // ---------- getValue(int) ----------
    @Test
    public void testGetValueIntZero() {
        assertEquals(7, new TestDaysPeriod(7).getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIntOutOfBounds() {
        new TestDaysPeriod(1).getValue(1);
    }

    // ---------- get(DurationFieldType) ----------
    @Test
    public void testGetMatchingType() {
        TestDaysPeriod p = new TestDaysPeriod(3);
        assertEquals(3, p.get(DurationFieldType.days()));
    }

    @Test
    public void testGetNonMatchingType() {
        TestDaysPeriod p = new TestDaysPeriod(3);
        assertEquals(0, p.get(DurationFieldType.hours()));
    }

    @Test
    public void testGetNullType() {
        assertEquals(0, new TestDaysPeriod(10).get(null));
    }

    // ---------- isSupported ----------
    @Test
    public void testIsSupportedTrue() {
        assertTrue(new TestDaysPeriod(1).isSupported(DurationFieldType.days()));
    }

    @Test
    public void testIsSupportedFalse() {
        assertFalse(new TestDaysPeriod(1).isSupported(DurationFieldType.months()));
    }

    @Test
    public void testIsSupportedNull() {
        assertFalse(new TestDaysPeriod(1).isSupported(null));
    }

    // ---------- toPeriod ----------
    @Test
    public void testToPeriod() {
        TestDaysPeriod tp = new TestDaysPeriod(5);
        Period p = tp.toPeriod();
        assertEquals(5, p.getDays());
        assertTrue(p.getPeriodType().equals(PeriodType.days()));
    }

    // ---------- toMutablePeriod ----------
    @Test
    public void testToMutablePeriod() {
        TestDaysPeriod tp = new TestDaysPeriod(3);
        MutablePeriod mp = tp.toMutablePeriod();
        assertEquals(3, mp.get(DurationFieldType.days()));
        assertTrue(mp.getPeriodType().equals(PeriodType.days()));
        mp.add(new TestDaysPeriod(2));
        assertEquals(5, mp.get(DurationFieldType.days()));
    }

    // ---------- equals/hashCode ----------
    @Test
    public void testEqualsSameInstance() {
        TestDaysPeriod p = new TestDaysPeriod(10);
        assertTrue(p.equals(p));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(new TestDaysPeriod(1).equals(null));
    }

    @Test
    public void testEqualsNonReadablePeriod() {
        assertFalse(new TestDaysPeriod(1).equals("string"));
    }

    @Test
    public void testEqualsSameTypeSameValue() {
        TestDaysPeriod a = new TestDaysPeriod(5);
        TestDaysPeriod b = new TestDaysPeriod(5);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsSameTypeDifferentValue() {
        TestDaysPeriod a = new TestDaysPeriod(5);
        TestDaysPeriod b = new TestDaysPeriod(6);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        // hashCodes likely differ but not guaranteed by contract, but likely
    }

    @Test
    public void testEqualsDifferentTypeSameValue() {
        // different PeriodType should not be equal
        TestDaysPeriod days = new TestDaysPeriod(10);
        TestHoursPeriod hours = new TestHoursPeriod(10);
        assertFalse(days.equals(hours));
        assertFalse(hours.equals(days));
    }

    @Test
    public void testEqualsReadablePeriodWithSameTypeAndValue() {
        // Create a readable period that matches days type and value
        TestDaysPeriod days = new TestDaysPeriod(5);
        Period p = new Period(0, 0, 0, 5, 0, 0, 0, 0).withPeriodType(PeriodType.days()); // days only
        assertTrue(days.equals(p));
        assertTrue(p.equals(days));
    }

    @Test
    public void testEqualsReadablePeriodWithSameTypeDifferentValue() {
        TestDaysPeriod days = new TestDaysPeriod(5);
        Period p = new Period(0, 0, 0, 10, 0, 0, 0, 0).withPeriodType(PeriodType.days());
        assertFalse(days.equals(p));
    }

    @Test
    public void testHashCodeConsistent() {
        TestDaysPeriod a = new TestDaysPeriod(7);
        TestDaysPeriod b = new TestDaysPeriod(7);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.hashCode() != new TestDaysPeriod(8).hashCode()); // likely
    }

    // ---------- compareTo ----------
    @Test
    public void testCompareToEqual() {
        TestDaysPeriod a = new TestDaysPeriod(3);
        TestDaysPeriod b = new TestDaysPeriod(3);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompareToGreater() {
        TestDaysPeriod a = new TestDaysPeriod(5);
        TestDaysPeriod b = new TestDaysPeriod(2);
        assertEquals(1, a.compareTo(b));
    }

    @Test
    public void testCompareToLess() {
        TestDaysPeriod a = new TestDaysPeriod(2);
        TestDaysPeriod b = new TestDaysPeriod(5);
        assertEquals(-1, a.compareTo(b));
    }

    @Test(expected = ClassCastException.class)
    public void testCompareToDifferentClass() {
        TestDaysPeriod days = new TestDaysPeriod(1);
        TestHoursPeriod hours = new TestHoursPeriod(1);
        days.compareTo(hours);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        TestDaysPeriod days = new TestDaysPeriod(1);
        days.compareTo(null);
    }

    // ---------- serialVersionUID check? Not required ----------

    // additional boundary tests for between(ReadablePartial) with different field types
    @Test
    public void testBetweenPartialSameFieldTypesDifferentOrder() {
        // use MonthDay (month, day) vs YearMonthDay... no, YearMonth (year, month) vs MonthDay (month, day) same size but different types. Already tested.
    }

    // Ensure standardPeriodIn handles milliseconds correctly
    @Test
    public void testStandardPeriodInWithMilliseconds() {
        Period p = Period.millis(2000);
        assertEquals(1, BaseSingleFieldPeriod.standardPeriodIn(p, 1500L)); // 2000/1500 = 1
    }

    @Test
    public void testStandardPeriodInNegativeValues() {
        Period p = Period.hours(-10);
        assertEquals(-10, BaseSingleFieldPeriod.standardPeriodIn(p, 3600000L));
    }

}
