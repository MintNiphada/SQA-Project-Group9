package org.joda.time.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.MutablePeriod;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.YearMonth;
import org.joda.time.chrono.ISOChronology;
import org.junit.Assert;
import org.junit.Test;

public class BaseSingleFieldPeriodTest {

    private static class MockSingleFieldPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        MockSingleFieldPeriod(int period) {
            super(period);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.hours();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.hours();
        }

        public void setValuePublic(int value) {
            super.setValue(value);
        }

        public int getValuePublic() {
            return super.getValue();
        }
    }

    private static class MockDaysSingleFieldPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        MockDaysSingleFieldPeriod(int period) {
            super(period);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }
    }

    //-----------------------------------------------------------------------
    // between(ReadableInstant, ReadableInstant, DurationFieldType)
    //-----------------------------------------------------------------------
    @Test
    public void testBetween_ReadableInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0);
        DateTime end = new DateTime(2006, 6, 9, 15, 0, 0, 0);
        int result = BaseSingleFieldPeriod.between(start, end, DurationFieldType.hours());
        Assert.assertEquals(3, result);

        result = BaseSingleFieldPeriod.between(end, start, DurationFieldType.hours());
        Assert.assertEquals(-3, result);

        result = BaseSingleFieldPeriod.between(start, start, DurationFieldType.hours());
        Assert.assertEquals(0, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadableInstant_NullStart() {
        DateTime end = new DateTime(2006, 6, 9, 15, 0, 0, 0);
        BaseSingleFieldPeriod.between((ReadableInstant) null, end, DurationFieldType.hours());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadableInstant_NullEnd() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0);
        BaseSingleFieldPeriod.between(start, (ReadableInstant) null, DurationFieldType.hours());
    }

    //-----------------------------------------------------------------------
    // between(ReadablePartial, ReadablePartial, ReadablePeriod)
    //-----------------------------------------------------------------------
    @Test
    public void testBetween_ReadablePartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end = new LocalDate(2006, 6, 12);
        int result = BaseSingleFieldPeriod.between(start, end, Period.days(0));
        Assert.assertEquals(3, result);

        result = BaseSingleFieldPeriod.between(end, start, Period.days(0));
        Assert.assertEquals(-3, result);

        result = BaseSingleFieldPeriod.between(start, start, Period.days(0));
        Assert.assertEquals(0, result);
    }

    @Test
    public void testBetween_ReadablePartial_Time() {
        LocalTime start = new LocalTime(12, 30, 0);
        LocalTime end = new LocalTime(15, 30, 0);
        int result = BaseSingleFieldPeriod.between(start, end, Period.hours(0));
        Assert.assertEquals(3, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadablePartial_NullStart() {
        LocalDate end = new LocalDate(2006, 6, 12);
        BaseSingleFieldPeriod.between((ReadablePartial) null, end, Period.days(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadablePartial_NullEnd() {
        LocalDate start = new LocalDate(2006, 6, 9);
        BaseSingleFieldPeriod.between(start, (ReadablePartial) null, Period.days(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadablePartial_DifferentSize() {
        LocalDate start = new LocalDate(2006, 6, 9);
        YearMonth end = new YearMonth(2006, 6);
        BaseSingleFieldPeriod.between(start, end, Period.days(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadablePartial_DifferentFields() {
        Partial start = new Partial(DateTimeFieldType.dayOfMonth(), 9);
        Partial end = new Partial(DateTimeFieldType.monthOfYear(), 6);
        BaseSingleFieldPeriod.between(start, end, Period.days(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetween_ReadablePartial_NonContiguous() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.hourOfDay() };
        int[] values = new int[] { 2006, 5 };
        Partial start = new Partial(types, values);
        Partial end = new Partial(types, values);
        BaseSingleFieldPeriod.between(start, end, Period.hours(0));
    }

    //-----------------------------------------------------------------------
    // standardPeriodIn(ReadablePeriod, long)
    //-----------------------------------------------------------------------
    @Test
    public void testStandardPeriodIn_Null() {
        int result = BaseSingleFieldPeriod.standardPeriodIn(null, DateTimeConstants.MILLIS_PER_HOUR);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testStandardPeriodIn_Precise() {
        Period p = new Period(0, 0, 1, 2, 3, 4, 5, 6); // 1 week, 2 days, 3 hours, 4 mins, 5 secs, 6 ms
        long totalMillis = (7L * 24 + 2 * 24 + 3) * 3600000L + 4 * 60000L + 5 * 1000L + 6;
        int hours = BaseSingleFieldPeriod.standardPeriodIn(p, DateTimeConstants.MILLIS_PER_HOUR);
        Assert.assertEquals((int) (totalMillis / 3600000L), hours);

        int days = BaseSingleFieldPeriod.standardPeriodIn(p, DateTimeConstants.MILLIS_PER_DAY);
        Assert.assertEquals((int) (totalMillis / 86400000L), days);
    }

    @Test
    public void testStandardPeriodIn_Zero() {
        Period p = Period.ZERO;
        int hours = BaseSingleFieldPeriod.standardPeriodIn(p, DateTimeConstants.MILLIS_PER_HOUR);
        Assert.assertEquals(0, hours);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodIn_ImpreciseMonth() {
        Period p = Period.months(1);
        BaseSingleFieldPeriod.standardPeriodIn(p, DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodIn_ImpreciseYear() {
        Period p = Period.years(1);
        BaseSingleFieldPeriod.standardPeriodIn(p, DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test(expected = ArithmeticException.class)
    public void testStandardPeriodIn_Overflow() {
        Period p = Period.days(Integer.MAX_VALUE);
        BaseSingleFieldPeriod.standardPeriodIn(p, 1);
    }

    //-----------------------------------------------------------------------
    // Instance methods: size, getFieldType, getValue, get, isSupported
    //-----------------------------------------------------------------------
    @Test
    public void testSize() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertEquals(1, test.size());
    }

    @Test
    public void testGetFieldType_Int() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertEquals(DurationFieldType.hours(), test.getFieldType(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_Int_InvalidIndexPositive() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        test.getFieldType(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_Int_InvalidIndexNegative() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        test.getFieldType(-1);
    }

    @Test
    public void testGetValue_Int() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertEquals(5, test.getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_Int_InvalidIndexPositive() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        test.getValue(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_Int_InvalidIndexNegative() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        test.getValue(-1);
    }

    @Test
    public void testGet_DurationFieldType() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertEquals(5, test.get(DurationFieldType.hours()));
        Assert.assertEquals(0, test.get(DurationFieldType.days()));
        Assert.assertEquals(0, test.get(null));
    }

    @Test
    public void testIsSupported() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertTrue(test.isSupported(DurationFieldType.hours()));
        Assert.assertFalse(test.isSupported(DurationFieldType.days()));
        Assert.assertFalse(test.isSupported(null));
    }

    @Test
    public void testGetSetValue() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Assert.assertEquals(5, test.getValuePublic());
        test.setValuePublic(10);
        Assert.assertEquals(10, test.getValuePublic());
    }

    //-----------------------------------------------------------------------
    // toPeriod & toMutablePeriod
    //-----------------------------------------------------------------------
    @Test
    public void testToPeriod() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        Period expected = Period.hours(5);
        Assert.assertEquals(expected, test.toPeriod());
    }

    @Test
    public void testToMutablePeriod() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        MutablePeriod expected = new MutablePeriod(Period.hours(5));
        Assert.assertEquals(expected, test.toMutablePeriod());
    }

    //-----------------------------------------------------------------------
    // equals & hashCode
    //-----------------------------------------------------------------------
    @Test
    public void testEqualsAndHashCode() {
        MockSingleFieldPeriod test1 = new MockSingleFieldPeriod(5);
        MockSingleFieldPeriod test2 = new MockSingleFieldPeriod(5);
        MockSingleFieldPeriod test3 = new MockSingleFieldPeriod(6);
        MockDaysSingleFieldPeriod testDays = new MockDaysSingleFieldPeriod(5);

        Assert.assertTrue(test1.equals(test1));
        Assert.assertTrue(test1.equals(test2));
        Assert.assertTrue(test2.equals(test1));
        Assert.assertEquals(test1.hashCode(), test2.hashCode());

        Assert.assertFalse(test1.equals(test3));
        Assert.assertFalse(test1.equals(testDays));
        Assert.assertFalse(test1.equals(null));
        Assert.assertFalse(test1.equals("Not a period"));

        // Period with matching type and value
        Hours hours5 = Hours.hours(5);
        Assert.assertTrue(test1.equals(hours5));

        Hours hours6 = Hours.hours(6);
        Assert.assertFalse(test1.equals(hours6));
    }

    @Test
    public void testHashCodeCalculation() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        int expected = 17;
        expected = 27 * expected + 5;
        expected = 27 * expected + DurationFieldType.hours().hashCode();
        Assert.assertEquals(expected, test.hashCode());
    }

    //-----------------------------------------------------------------------
    // compareTo
    //-----------------------------------------------------------------------
    @Test
    public void testCompareTo() {
        MockSingleFieldPeriod test1 = new MockSingleFieldPeriod(5);
        MockSingleFieldPeriod test2 = new MockSingleFieldPeriod(5);
        MockSingleFieldPeriod test3 = new MockSingleFieldPeriod(6);
        MockSingleFieldPeriod test4 = new MockSingleFieldPeriod(4);

        Assert.assertEquals(0, test1.compareTo(test2));
        Assert.assertTrue(test1.compareTo(test3) < 0);
        Assert.assertTrue(test1.compareTo(test4) > 0);
        Assert.assertEquals(-1, test1.compareTo(test3));
        Assert.assertEquals(1, test1.compareTo(test4));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_Null() {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);
        test.compareTo(null);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareTo_DifferentClass() {
        MockSingleFieldPeriod testHours = new MockSingleFieldPeriod(5);
        MockDaysSingleFieldPeriod testDays = new MockDaysSingleFieldPeriod(5);
        testHours.compareTo(testDays);
    }

    //-----------------------------------------------------------------------
    // Serialization
    //-----------------------------------------------------------------------
    @Test
    public void testSerialization() throws Exception {
        MockSingleFieldPeriod test = new MockSingleFieldPeriod(5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MockSingleFieldPeriod result = (MockSingleFieldPeriod) ois.readObject();
        ois.close();

        Assert.assertEquals(test, result);
        Assert.assertEquals(test.getValue(), result.getValue());
    }
}
