package org.joda.time.field;

import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.junit.Test;

import static org.junit.Assert.*;

public class FieldUtilsTest {

    // safeNegate tests
    @Test
    public void testSafeNegate() {
        assertEquals(-1, FieldUtils.safeNegate(1));
        assertEquals(0, FieldUtils.safeNegate(0));
        assertEquals(1, FieldUtils.safeNegate(-1));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeNegate(Integer.MIN_VALUE + 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeNegateMinValue() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    // safeAdd int tests
    @Test
    public void testSafeAddInt() {
        assertEquals(0, FieldUtils.safeAdd(0, 0));
        assertEquals(5, FieldUtils.safeAdd(2, 3));
        assertEquals(-5, FieldUtils.safeAdd(-2, -3));
        assertEquals(0, FieldUtils.safeAdd(Integer.MAX_VALUE, Integer.MIN_VALUE + 1));
        assertEquals(-1, FieldUtils.safeAdd(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddIntOverflowPositive() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddIntOverflowNegative() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    // safeAdd long tests
    @Test
    public void testSafeAddLong() {
        assertEquals(0L, FieldUtils.safeAdd(0L, 0L));
        assertEquals(5L, FieldUtils.safeAdd(2L, 3L));
        assertEquals(-5L, FieldUtils.safeAdd(-2L, -3L));
        assertEquals(0L, FieldUtils.safeAdd(Long.MAX_VALUE, Long.MIN_VALUE + 1));
        assertEquals(-1L, FieldUtils.safeAdd(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddLongOverflowPositive() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddLongOverflowNegative() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    // safeSubtract long tests
    @Test
    public void testSafeSubtractLong() {
        assertEquals(0L, FieldUtils.safeSubtract(0L, 0L));
        assertEquals(1L, FieldUtils.safeSubtract(3L, 2L));
        assertEquals(-1L, FieldUtils.safeSubtract(2L, 3L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE, 0L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLongOverflowPositive() {
        FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLongOverflowNegative() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    // safeMultiply int tests
    @Test
    public void testSafeMultiplyInt() {
        assertEquals(0, FieldUtils.safeMultiply(0, 0));
        assertEquals(6, FieldUtils.safeMultiply(2, 3));
        assertEquals(-6, FieldUtils.safeMultiply(2, -3));
        assertEquals(0, FieldUtils.safeMultiply(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntOverflowPositive() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntOverflowNegative() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, 2);
    }

    // safeMultiply long * int tests
    @Test
    public void testSafeMultiplyLongInt() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 0));
        assertEquals(6L, FieldUtils.safeMultiply(2L, 3));
        assertEquals(-6L, FieldUtils.safeMultiply(2L, -3));
        assertEquals(0L, FieldUtils.safeMultiply(Long.MAX_VALUE, 0));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1));
        assertEquals(-Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongIntOverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongIntOverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongIntOverflowMinValueTimesMinusOne() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
    }

    // safeMultiply long * long tests
    @Test
    public void testSafeMultiplyLongLong() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 0L));
        assertEquals(6L, FieldUtils.safeMultiply(2L, 3L));
        assertEquals(-6L, FieldUtils.safeMultiply(2L, -3L));
        assertEquals(0L, FieldUtils.safeMultiply(Long.MAX_VALUE, 0L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(1L, Long.MAX_VALUE));
        assertEquals(-Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1L));
        assertEquals(-Long.MAX_VALUE, FieldUtils.safeMultiply(-1L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(1L, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLongOverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLongOverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLongOverflowMinValueTimesMinusOne() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLongOverflowMinusOneTimesMinValue() {
        FieldUtils.safeMultiply(-1L, Long.MIN_VALUE);
    }

    // safeToInt tests
    @Test
    public void testSafeToInt() {
        assertEquals(0, FieldUtils.safeToInt(0L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeToInt((long) Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeToInt((long) Integer.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeToIntOverflowPositive() {
        FieldUtils.safeToInt((long) Integer.MAX_VALUE + 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeToIntOverflowNegative() {
        FieldUtils.safeToInt((long) Integer.MIN_VALUE - 1);
    }

    // safeMultiplyToInt tests
    @Test
    public void testSafeMultiplyToInt() {
        assertEquals(0, FieldUtils.safeMultiplyToInt(0L, 0L));
        assertEquals(6, FieldUtils.safeMultiplyToInt(2L, 3L));
        assertEquals(-6, FieldUtils.safeMultiplyToInt(2L, -3L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiplyToInt(1L, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiplyToInt(1L, Integer.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToIntOverflow() {
        FieldUtils.safeMultiplyToInt(2L, (long) Integer.MAX_VALUE);
    }

    // verifyValueBounds with DateTimeField tests
    @Test
    public void testVerifyValueBoundsDateTimeField() {
        DateTimeField field = new MockDateTimeField(DateTimeFieldType.year(), 0, 100);
        FieldUtils.verifyValueBounds(field, 50, 0, 100);
        FieldUtils.verifyValueBounds(field, 0, 0, 100);
        FieldUtils.verifyValueBounds(field, 100, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeFieldBelow() {
        DateTimeField field = new MockDateTimeField(DateTimeFieldType.year(), 0, 100);
        FieldUtils.verifyValueBounds(field, -1, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeFieldAbove() {
        DateTimeField field = new MockDateTimeField(DateTimeFieldType.year(), 0, 100);
        FieldUtils.verifyValueBounds(field, 101, 0, 100);
    }

    // verifyValueBounds with DateTimeFieldType tests
    @Test
    public void testVerifyValueBoundsDateTimeFieldType() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.year(), 50, 0, 100);
        FieldUtils.verifyValueBounds(DateTimeFieldType.year(), 0, 0, 100);
        FieldUtils.verifyValueBounds(DateTimeFieldType.year(), 100, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeFieldTypeBelow() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.year(), -1, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeFieldTypeAbove() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.year(), 101, 0, 100);
    }

    // verifyValueBounds with String tests
    @Test
    public void testVerifyValueBoundsString() {
        FieldUtils.verifyValueBounds("test", 50, 0, 100);
        FieldUtils.verifyValueBounds("test", 0, 0, 100);
        FieldUtils.verifyValueBounds("test", 100, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsStringBelow() {
        FieldUtils.verifyValueBounds("test", -1, 0, 100);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsStringAbove() {
        FieldUtils.verifyValueBounds("test", 101, 0, 100);
    }

    // getWrappedValue with wrapValue tests
    @Test
    public void testGetWrappedValueWithWrapValue() {
        assertEquals(5, FieldUtils.getWrappedValue(0, 5, 0, 10));
        assertEquals(0, FieldUtils.getWrappedValue(10, 1, 0, 10));
        assertEquals(10, FieldUtils.getWrappedValue(0, -1, 0, 10));
        assertEquals(0, FieldUtils.getWrappedValue(0, 0, 0, 10));
    }

    // getWrappedValue tests
    @Test
    public void testGetWrappedValue() {
        assertEquals(5, FieldUtils.getWrappedValue(5, 0, 10));
        assertEquals(0, FieldUtils.getWrappedValue(0, 0, 10));
        assertEquals(10, FieldUtils.getWrappedValue(10, 0, 10));
        assertEquals(0, FieldUtils.getWrappedValue(11, 0, 10));
        assertEquals(1, FieldUtils.getWrappedValue(12, 0, 10));
        assertEquals(10, FieldUtils.getWrappedValue(-1, 0, 10));
        assertEquals(9, FieldUtils.getWrappedValue(-2, 0, 10));
        assertEquals(0, FieldUtils.getWrappedValue(-11, 0, 10));
        assertEquals(1, FieldUtils.getWrappedValue(-10, 0, 10));
        assertEquals(5, FieldUtils.getWrappedValue(5, 5, 10));
        assertEquals(5, FieldUtils.getWrappedValue(0, 5, 10));
        assertEquals(10, FieldUtils.getWrappedValue(-1, 5, 10));
        assertEquals(5, FieldUtils.getWrappedValue(-6, 5, 10));
        assertEquals(10, FieldUtils.getWrappedValue(-7, 5, 10));
        assertEquals(0, FieldUtils.getWrappedValue(0, -5, 5));
        assertEquals(-5, FieldUtils.getWrappedValue(-5, -5, 5));
        assertEquals(5, FieldUtils.getWrappedValue(5, -5, 5));
        assertEquals(-5, FieldUtils.getWrappedValue(6, -5, 5));
        assertEquals(-4, FieldUtils.getWrappedValue(7, -5, 5));
        assertEquals(5, FieldUtils.getWrappedValue(-6, -5, 5));
        assertEquals(4, FieldUtils.getWrappedValue(-7, -5, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValueMinGreaterThanMax() {
        FieldUtils.getWrappedValue(0, 10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValueMinEqualsMax() {
        FieldUtils.getWrappedValue(0, 10, 10);
    }

    // equals tests
    @Test
    public void testEquals() {
        assertTrue(FieldUtils.equals(null, null));
        assertTrue(FieldUtils.equals("a", "a"));
        assertFalse(FieldUtils.equals(null, "a"));
        assertFalse(FieldUtils.equals("a", null));
        assertFalse(FieldUtils.equals("a", "b"));
        Object obj1 = new Object();
        Object obj2 = obj1;
        assertTrue(FieldUtils.equals(obj1, obj2));
    }

    // Mock DateTimeField for testing verifyValueBounds
    private static class MockDateTimeField extends DateTimeField {
        private final DateTimeFieldType type;
        private final int minValue;
        private final int maxValue;

        public MockDateTimeField(DateTimeFieldType type, int minValue, int maxValue) {
            super();
            this.type = type;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public DateTimeFieldType getType() {
            return type;
        }

        @Override
        public String getName() {
            return type.getName();
        }

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public int get(long instant) {
            return 0;
        }

        @Override
        public long set(long instant, int value) {
            return 0;
        }

        @Override
        public long set(long instant, String text, java.util.Locale locale) {
            return 0;
        }

        @Override
        public long roundFloor(long instant) {
            return 0;
        }

        @Override
        public long roundCeiling(long instant) {
            return 0;
        }

        @Override
        public long roundHalfFloor(long instant) {
            return 0;
        }

        @Override
        public long roundHalfCeiling(long instant) {
            return 0;
        }

        @Override
        public long roundHalfEven(long instant) {
            return 0;
        }

        @Override
        public long remainder(long instant) {
            return 0;
        }

        @Override
        public int getMinimumValue() {
            return minValue;
        }

        @Override
        public int getMaximumValue() {
            return maxValue;
        }

        @Override
        public String getAsText(long instant, java.util.Locale locale) {
            return null;
        }

        @Override
        public String getAsShortText(long instant, java.util.Locale locale) {
            return null;
        }

        @Override
        public long add(long instant, int value) {
            return 0;
        }

        @Override
        public long add(long instant, long value) {
            return 0;
        }

        @Override
        public int getDifference(long minuendInstant, long subtrahendInstant) {
            return 0;
        }

        @Override
        public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
            return 0;
        }

        @Override
        public boolean isLeap(long instant) {
            return false;
        }

        @Override
        public int getLeapAmount(long instant) {
            return 0;
        }

        @Override
        public DateTimeField getLeapDurationField() {
            return null;
        }

        @Override
        public int getMinimumValue(long instant) {
            return minValue;
        }

        @Override
        public int getMaximumValue(long instant) {
            return maxValue;
        }

        @Override
        public long setExtended(long instant, int value) {
            return 0;
        }
    }
}
