package org.joda.time.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.chrono.ISOChronology;
import org.junit.Assert;
import org.junit.Test;

public class FieldUtilsTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<FieldUtils> constructor = FieldUtils.class.getDeclaredConstructor();
        Assert.assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        FieldUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    //-----------------------------------------------------------------------
    // safeNegate(int)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeNegate() {
        Assert.assertEquals(0, FieldUtils.safeNegate(0));
        Assert.assertEquals(-1, FieldUtils.safeNegate(1));
        Assert.assertEquals(1, FieldUtils.safeNegate(-1));
        Assert.assertEquals(-Integer.MAX_VALUE, FieldUtils.safeNegate(Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, FieldUtils.safeNegate(-Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeNegateOverflow() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // safeAdd(int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeAddInt() {
        Assert.assertEquals(0, FieldUtils.safeAdd(0, 0));
        Assert.assertEquals(5, FieldUtils.safeAdd(2, 3));
        Assert.assertEquals(-1, FieldUtils.safeAdd(2, -3));
        Assert.assertEquals(1, FieldUtils.safeAdd(-2, 3));
        Assert.assertEquals(-5, FieldUtils.safeAdd(-2, -3));
        Assert.assertEquals(Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE - 1, 1));
        Assert.assertEquals(Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE + 1, -1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddIntOverflowPositive() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddIntOverflowNegative() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    //-----------------------------------------------------------------------
    // safeAdd(long, long)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeAddLong() {
        Assert.assertEquals(0L, FieldUtils.safeAdd(0L, 0L));
        Assert.assertEquals(5L, FieldUtils.safeAdd(2L, 3L));
        Assert.assertEquals(-1L, FieldUtils.safeAdd(2L, -3L));
        Assert.assertEquals(1L, FieldUtils.safeAdd(-2L, 3L));
        Assert.assertEquals(-5L, FieldUtils.safeAdd(-2L, -3L));
        Assert.assertEquals(Long.MAX_VALUE, FieldUtils.safeAdd(Long.MAX_VALUE - 1L, 1L));
        Assert.assertEquals(Long.MIN_VALUE, FieldUtils.safeAdd(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddLongOverflowPositive() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddLongOverflowNegative() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    //-----------------------------------------------------------------------
    // safeSubtract(long, long)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeSubtractLong() {
        Assert.assertEquals(0L, FieldUtils.safeSubtract(0L, 0L));
        Assert.assertEquals(-1L, FieldUtils.safeSubtract(2L, 3L));
        Assert.assertEquals(5L, FieldUtils.safeSubtract(2L, -3L));
        Assert.assertEquals(-5L, FieldUtils.safeSubtract(-2L, 3L));
        Assert.assertEquals(1L, FieldUtils.safeSubtract(-2L, -3L));
        Assert.assertEquals(Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE + 1L, 1L));
        Assert.assertEquals(Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE - 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLongOverflowPositive() {
        FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLongOverflowNegative() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    //-----------------------------------------------------------------------
    // safeMultiply(int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeMultiplyInt() {
        Assert.assertEquals(0, FieldUtils.safeMultiply(0, 0));
        Assert.assertEquals(0, FieldUtils.safeMultiply(0, 5));
        Assert.assertEquals(0, FieldUtils.safeMultiply(5, 0));
        Assert.assertEquals(6, FieldUtils.safeMultiply(2, 3));
        Assert.assertEquals(-6, FieldUtils.safeMultiply(2, -3));
        Assert.assertEquals(-6, FieldUtils.safeMultiply(-2, 3));
        Assert.assertEquals(6, FieldUtils.safeMultiply(-2, -3));
        Assert.assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, 1));
        Assert.assertEquals(-Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, -1));
        Assert.assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntOverflowPositive() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntOverflowNegative() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntOverflowMinValTimesMinusOne() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, -1);
    }

    //-----------------------------------------------------------------------
    // safeMultiply(long, int)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeMultiplyLongInt() {
        Assert.assertEquals(0L, FieldUtils.safeMultiply(0L, 0));
        Assert.assertEquals(0L, FieldUtils.safeMultiply(10L, 0));
        Assert.assertEquals(10L, FieldUtils.safeMultiply(10L, 1));
        Assert.assertEquals(-10L, FieldUtils.safeMultiply(10L, -1));
        Assert.assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1));
        Assert.assertEquals(-Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1));
        Assert.assertEquals(6L, FieldUtils.safeMultiply(2L, 3));
        Assert.assertEquals(-6L, FieldUtils.safeMultiply(2L, -3));
        Assert.assertEquals(-6L, FieldUtils.safeMultiply(-2L, 3));
        Assert.assertEquals(6L, FieldUtils.safeMultiply(-2L, -3));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongIntOverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongIntOverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
    }

    //-----------------------------------------------------------------------
    // safeMultiply(long, long)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeMultiplyLongLong() {
        Assert.assertEquals(0L, FieldUtils.safeMultiply(0L, 0L));
        Assert.assertEquals(0L, FieldUtils.safeMultiply(0L, 10L));
        Assert.assertEquals(0L, FieldUtils.safeMultiply(10L, 0L));
        Assert.assertEquals(10L, FieldUtils.safeMultiply(10L, 1L));
        Assert.assertEquals(10L, FieldUtils.safeMultiply(1L, 10L));
        Assert.assertEquals(-10L, FieldUtils.safeMultiply(10L, -1L));
        Assert.assertEquals(-10L, FieldUtils.safeMultiply(-1L, 10L));
        Assert.assertEquals(6L, FieldUtils.safeMultiply(2L, 3L));
        Assert.assertEquals(-6L, FieldUtils.safeMultiply(2L, -3L));
        Assert.assertEquals(-6L, FieldUtils.safeMultiply(-2L, 3L));
        Assert.assertEquals(6L, FieldUtils.safeMultiply(-2L, -3L));
        Assert.assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1L));
        Assert.assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(1L, Long.MAX_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1L));
        Assert.assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(1L, Long.MIN_VALUE));
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
    public void testSafeMultiplyLongLongOverflowMinValTimesMinusOne() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLongOverflowMinusOneTimesMinVal() {
        FieldUtils.safeMultiply(-1L, Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // safeToInt(long)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeToInt() {
        Assert.assertEquals(0, FieldUtils.safeToInt(0L));
        Assert.assertEquals(12345, FieldUtils.safeToInt(12345L));
        Assert.assertEquals(-12345, FieldUtils.safeToInt(-12345L));
        Assert.assertEquals(Integer.MAX_VALUE, FieldUtils.safeToInt((long) Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE, FieldUtils.safeToInt((long) Integer.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeToIntOverflowUpper() {
        FieldUtils.safeToInt((long) Integer.MAX_VALUE + 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeToIntOverflowLower() {
        FieldUtils.safeToInt((long) Integer.MIN_VALUE - 1L);
    }

    //-----------------------------------------------------------------------
    // safeMultiplyToInt(long, long)
    //-----------------------------------------------------------------------
    @Test
    public void testSafeMultiplyToInt() {
        Assert.assertEquals(0, FieldUtils.safeMultiplyToInt(0L, 0L));
        Assert.assertEquals(6, FieldUtils.safeMultiplyToInt(2L, 3L));
        Assert.assertEquals(-6, FieldUtils.safeMultiplyToInt(2L, -3L));
        Assert.assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiplyToInt(Integer.MAX_VALUE, 1L));
        Assert.assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiplyToInt(Integer.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToIntOverflowMultiply() {
        FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToIntOverflowIntCast() {
        FieldUtils.safeMultiplyToInt(Integer.MAX_VALUE, 2L);
    }

    //-----------------------------------------------------------------------
    // verifyValueBounds(DateTimeField, int, int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testVerifyValueBoundsDateTimeField() {
        DateTimeField field = ISOChronology.getInstanceUTC().monthOfYear();
        FieldUtils.verifyValueBounds(field, 1, 1, 12);
        FieldUtils.verifyValueBounds(field, 6, 1, 12);
        FieldUtils.verifyValueBounds(field, 12, 1, 12);

        try {
            FieldUtils.verifyValueBounds(field, 0, 1, 12);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals(DateTimeFieldType.monthOfYear(), ex.getDateTimeFieldType());
            Assert.assertEquals(Integer.valueOf(0), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(1), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(12), ex.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds(field, 13, 1, 12);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals(DateTimeFieldType.monthOfYear(), ex.getDateTimeFieldType());
            Assert.assertEquals(Integer.valueOf(13), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(1), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(12), ex.getUpperBound());
        }
    }

    //-----------------------------------------------------------------------
    // verifyValueBounds(DateTimeFieldType, int, int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testVerifyValueBoundsDateTimeFieldType() {
        DateTimeFieldType fieldType = DateTimeFieldType.dayOfMonth();
        FieldUtils.verifyValueBounds(fieldType, 1, 1, 31);
        FieldUtils.verifyValueBounds(fieldType, 15, 1, 31);
        FieldUtils.verifyValueBounds(fieldType, 31, 1, 31);

        try {
            FieldUtils.verifyValueBounds(fieldType, 0, 1, 31);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals(DateTimeFieldType.dayOfMonth(), ex.getDateTimeFieldType());
            Assert.assertEquals(Integer.valueOf(0), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(1), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(31), ex.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds(fieldType, 32, 1, 31);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals(DateTimeFieldType.dayOfMonth(), ex.getDateTimeFieldType());
            Assert.assertEquals(Integer.valueOf(32), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(1), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(31), ex.getUpperBound());
        }
    }

    //-----------------------------------------------------------------------
    // verifyValueBounds(String, int, int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testVerifyValueBoundsString() {
        String fieldName = "hourOfDay";
        FieldUtils.verifyValueBounds(fieldName, 0, 0, 23);
        FieldUtils.verifyValueBounds(fieldName, 12, 0, 23);
        FieldUtils.verifyValueBounds(fieldName, 23, 0, 23);

        try {
            FieldUtils.verifyValueBounds(fieldName, -1, 0, 23);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals("hourOfDay", ex.getFieldName());
            Assert.assertEquals(Integer.valueOf(-1), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(0), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(23), ex.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds(fieldName, 24, 0, 23);
            Assert.fail();
        } catch (IllegalFieldValueException ex) {
            Assert.assertEquals("hourOfDay", ex.getFieldName());
            Assert.assertEquals(Integer.valueOf(24), ex.getIllegalNumberValue());
            Assert.assertEquals(Integer.valueOf(0), ex.getLowerBound());
            Assert.assertEquals(Integer.valueOf(23), ex.getUpperBound());
        }
    }

    //-----------------------------------------------------------------------
    // getWrappedValue(int, int, int, int) and getWrappedValue(int, int, int)
    //-----------------------------------------------------------------------
    @Test
    public void testGetWrappedValueFourArgs() {
        Assert.assertEquals(1, FieldUtils.getWrappedValue(0, 1, 1, 12));
        Assert.assertEquals(12, FieldUtils.getWrappedValue(0, 12, 1, 12));
        Assert.assertEquals(1, FieldUtils.getWrappedValue(12, 1, 1, 12));
        Assert.assertEquals(11, FieldUtils.getWrappedValue(1, -2, 1, 12));
    }

    @Test
    public void testGetWrappedValueThreeArgs() {
        Assert.assertEquals(1, FieldUtils.getWrappedValue(1, 1, 12));
        Assert.assertEquals(12, FieldUtils.getWrappedValue(12, 1, 12));
        Assert.assertEquals(1, FieldUtils.getWrappedValue(13, 1, 12));
        Assert.assertEquals(2, FieldUtils.getWrappedValue(14, 1, 12));
        Assert.assertEquals(12, FieldUtils.getWrappedValue(0, 1, 12));
        Assert.assertEquals(11, FieldUtils.getWrappedValue(-1, 1, 12));
        Assert.assertEquals(1, FieldUtils.getWrappedValue(-11, 1, 12));
        Assert.assertEquals(12, FieldUtils.getWrappedValue(-12, 1, 12));
        Assert.assertEquals(11, FieldUtils.getWrappedValue(-13, 1, 12));

        // Range where min is negative
        Assert.assertEquals(-5, FieldUtils.getWrappedValue(-5, -5, 5));
        Assert.assertEquals(5, FieldUtils.getWrappedValue(5, -5, 5));
        Assert.assertEquals(-5, FieldUtils.getWrappedValue(6, -5, 5));
        Assert.assertEquals(5, FieldUtils.getWrappedValue(-6, -5, 5));
        Assert.assertEquals(-4, FieldUtils.getWrappedValue(7, -5, 5));
        Assert.assertEquals(4, FieldUtils.getWrappedValue(-7, -5, 5));

        // Exact wrap multiples when value < minValue
        Assert.assertEquals(1, FieldUtils.getWrappedValue(1 - 24, 1, 12));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValueMinEqualMax() {
        FieldUtils.getWrappedValue(5, 10, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValueMinGreaterThanMax() {
        FieldUtils.getWrappedValue(5, 12, 10);
    }

    //-----------------------------------------------------------------------
    // equals(Object, Object)
    //-----------------------------------------------------------------------
    @Test
    public void testEquals() {
        Assert.assertTrue(FieldUtils.equals(null, null));
        Assert.assertFalse(FieldUtils.equals("a", null));
        Assert.assertFalse(FieldUtils.equals(null, "a"));
        Assert.assertTrue(FieldUtils.equals("a", "a"));
        Assert.assertFalse(FieldUtils.equals("a", "b"));
        Assert.assertFalse(FieldUtils.equals(Integer.valueOf(1), Long.valueOf(1)));
        
        String str1 = new String("hello");
        String str2 = new String("hello");
        Assert.assertTrue(FieldUtils.equals(str1, str2));
    }
}
