package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;

public class BooleanUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new BooleanUtils());
    }

    @Test
    public void testNegate() {
        Assert.assertNull(BooleanUtils.negate(null));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
    }

    @Test
    public void testIsTrue() {
        Assert.assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
        Assert.assertFalse(BooleanUtils.isTrue(Boolean.FALSE));
        Assert.assertFalse(BooleanUtils.isTrue(null));
    }

    @Test
    public void testIsNotTrue() {
        Assert.assertFalse(BooleanUtils.isNotTrue(Boolean.TRUE));
        Assert.assertTrue(BooleanUtils.isNotTrue(Boolean.FALSE));
        Assert.assertTrue(BooleanUtils.isNotTrue(null));
    }

    @Test
    public void testIsFalse() {
        Assert.assertFalse(BooleanUtils.isFalse(Boolean.TRUE));
        Assert.assertTrue(BooleanUtils.isFalse(Boolean.FALSE));
        Assert.assertFalse(BooleanUtils.isFalse(null));
    }

    @Test
    public void testIsNotFalse() {
        Assert.assertTrue(BooleanUtils.isNotFalse(Boolean.TRUE));
        Assert.assertFalse(BooleanUtils.isNotFalse(Boolean.FALSE));
        Assert.assertTrue(BooleanUtils.isNotFalse(null));
    }

    @Test
    public void testToBooleanObject_boolean() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

    @Test
    public void testToBoolean_Boolean() {
        Assert.assertTrue(BooleanUtils.toBoolean(Boolean.TRUE));
        Assert.assertFalse(BooleanUtils.toBoolean(Boolean.FALSE));
        Assert.assertFalse(BooleanUtils.toBoolean((Boolean) null));
    }

    @Test
    public void testToBooleanDefaultIfNull() {
        Assert.assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        Assert.assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        Assert.assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        Assert.assertFalse(BooleanUtils.toBooleanDefaultIfNull(null, false));
    }

    @Test
    public void testToBoolean_int() {
        Assert.assertTrue(BooleanUtils.toBoolean(1));
        Assert.assertTrue(BooleanUtils.toBoolean(-1));
        Assert.assertFalse(BooleanUtils.toBoolean(0));
    }

    @Test
    public void testToBooleanObject_int() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

    @Test
    public void testToBooleanObject_Integer() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        Assert.assertNull(BooleanUtils.toBooleanObject((Integer) null));
    }

    @Test
    public void testToBoolean_int_int_int() {
        Assert.assertTrue(BooleanUtils.toBoolean(1, 1, 2));
        Assert.assertFalse(BooleanUtils.toBoolean(2, 1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_int_int_int_noMatch() {
        BooleanUtils.toBoolean(3, 1, 2);
    }

    @Test
    public void testToBoolean_Integer_Integer_Integer() {
        Assert.assertTrue(BooleanUtils.toBoolean((Integer) null, (Integer) null, new Integer(1)));
        Assert.assertFalse(BooleanUtils.toBoolean((Integer) null, new Integer(1), (Integer) null));
        Assert.assertTrue(BooleanUtils.toBoolean(new Integer(1), new Integer(1), new Integer(2)));
        Assert.assertFalse(BooleanUtils.toBoolean(new Integer(2), new Integer(1), new Integer(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_Integer_Integer_Integer_nullMatchFail() {
        BooleanUtils.toBoolean((Integer) null, new Integer(1), new Integer(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_Integer_Integer_Integer_noMatch() {
        BooleanUtils.toBoolean(new Integer(3), new Integer(1), new Integer(2));
    }

    @Test
    public void testToBooleanObject_int_int_int_int() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 2, 3));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(2, 1, 2, 3));
        Assert.assertNull(BooleanUtils.toBooleanObject(3, 1, 2, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_int_int_int_int_noMatch() {
        BooleanUtils.toBooleanObject(4, 1, 2, 3);
    }

    @Test
    public void testToBooleanObject_Integer_Integer_Integer_Integer() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject((Integer) null, (Integer) null, new Integer(2), new Integer(3)));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject((Integer) null, new Integer(1), (Integer) null, new Integer(3)));
        Assert.assertNull(BooleanUtils.toBooleanObject((Integer) null, new Integer(1), new Integer(2), (Integer) null));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1), new Integer(1), new Integer(2), new Integer(3)));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(2), new Integer(1), new Integer(2), new Integer(3)));
        Assert.assertNull(BooleanUtils.toBooleanObject(new Integer(3), new Integer(1), new Integer(2), new Integer(3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_Integer_Integer_Integer_Integer_nullMatchFail() {
        BooleanUtils.toBooleanObject((Integer) null, new Integer(1), new Integer(2), new Integer(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_Integer_Integer_Integer_Integer_noMatch() {
        BooleanUtils.toBooleanObject(new Integer(4), new Integer(1), new Integer(2), new Integer(3));
    }

    @Test
    public void testToInteger_boolean() {
        Assert.assertEquals(1, BooleanUtils.toInteger(true));
        Assert.assertEquals(0, BooleanUtils.toInteger(false));
    }

    @Test
    public void testToIntegerObject_boolean() {
        Assert.assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        Assert.assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }

    @Test
    public void testToIntegerObject_Boolean() {
        Assert.assertNull(BooleanUtils.toIntegerObject((Boolean) null));
        Assert.assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        Assert.assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
    }

    @Test
    public void testToInteger_boolean_int_int() {
        Assert.assertEquals(10, BooleanUtils.toInteger(true, 10, 20));
        Assert.assertEquals(20, BooleanUtils.toInteger(false, 10, 20));
    }

    @Test
    public void testToInteger_Boolean_int_int_int() {
        Assert.assertEquals(10, BooleanUtils.toInteger(Boolean.TRUE, 10, 20, 30));
        Assert.assertEquals(20, BooleanUtils.toInteger(Boolean.FALSE, 10, 20, 30));
        Assert.assertEquals(30, BooleanUtils.toInteger(null, 10, 20, 30));
    }

    @Test
    public void testToIntegerObject_boolean_Integer_Integer() {
        Assert.assertEquals(new Integer(10), BooleanUtils.toIntegerObject(true, new Integer(10), new Integer(20)));
        Assert.assertEquals(new Integer(20), BooleanUtils.toIntegerObject(false, new Integer(10), new Integer(20)));
    }

    @Test
    public void testToIntegerObject_Boolean_Integer_Integer_Integer() {
        Assert.assertEquals(new Integer(10), BooleanUtils.toIntegerObject(Boolean.TRUE, new Integer(10), new Integer(20), new Integer(30)));
        Assert.assertEquals(new Integer(20), BooleanUtils.toIntegerObject(Boolean.FALSE, new Integer(10), new Integer(20), new Integer(30)));
        Assert.assertEquals(new Integer(30), BooleanUtils.toIntegerObject(null, new Integer(10), new Integer(20), new Integer(30)));
    }

    @Test
    public void testToBooleanObject_String() {
        Assert.assertNull(BooleanUtils.toBooleanObject((String) null));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("True"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        Assert.assertNull(BooleanUtils.toBooleanObject("other"));
    }

    @Test
    public void testToBooleanObject_String_String_String_String() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject((String) null, null, "false", "null"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject((String) null, "true", null, "null"));
        Assert.assertNull(BooleanUtils.toBooleanObject((String) null, "true", "false", null));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true", "true", "false", "null"));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false", "true", "false", "null"));
        Assert.assertNull(BooleanUtils.toBooleanObject("null", "true", "false", "null"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_String_String_String_String_nullMatchFail() {
        BooleanUtils.toBooleanObject(null, "true", "false", "null");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_String_String_String_String_noMatch() {
        BooleanUtils.toBooleanObject("other", "true", "false", "null");
    }

    @Test
    public void testToBoolean_String() {
        Assert.assertTrue(BooleanUtils.toBoolean("true"));
        Assert.assertTrue(BooleanUtils.toBoolean(new String(new char[]{'t', 'r', 'u', 'e'})));
        Assert.assertTrue(BooleanUtils.toBoolean("TRUE"));
        Assert.assertTrue(BooleanUtils.toBoolean("tRuE"));
        Assert.assertTrue(BooleanUtils.toBoolean("TrUe"));
        Assert.assertFalse(BooleanUtils.toBoolean("trux"));
        Assert.assertFalse(BooleanUtils.toBoolean("TRUX"));
        Assert.assertFalse(BooleanUtils.toBoolean("trxe"));
        Assert.assertFalse(BooleanUtils.toBoolean("TRXE"));
        Assert.assertFalse(BooleanUtils.toBoolean("txue"));
        Assert.assertFalse(BooleanUtils.toBoolean("TXUE"));
        Assert.assertFalse(BooleanUtils.toBoolean("xrue"));

        Assert.assertTrue(BooleanUtils.toBoolean("on"));
        Assert.assertTrue(BooleanUtils.toBoolean("ON"));
        Assert.assertTrue(BooleanUtils.toBoolean("oN"));
        Assert.assertTrue(BooleanUtils.toBoolean("On"));
        Assert.assertFalse(BooleanUtils.toBoolean("ox"));
        Assert.assertFalse(BooleanUtils.toBoolean("xn"));

        Assert.assertTrue(BooleanUtils.toBoolean("yes"));
        Assert.assertTrue(BooleanUtils.toBoolean("YES"));
        Assert.assertTrue(BooleanUtils.toBoolean("yEs"));
        Assert.assertTrue(BooleanUtils.toBoolean("YeS"));
        Assert.assertFalse(BooleanUtils.toBoolean("yex"));
        Assert.assertFalse(BooleanUtils.toBoolean("YEX"));
        Assert.assertFalse(BooleanUtils.toBoolean("yxs"));
        Assert.assertFalse(BooleanUtils.toBoolean("YXS"));
        Assert.assertFalse(BooleanUtils.toBoolean("xes"));

        Assert.assertFalse(BooleanUtils.toBoolean((String) null));
        Assert.assertFalse(BooleanUtils.toBoolean(""));
        Assert.assertFalse(BooleanUtils.toBoolean("a"));
        Assert.assertFalse(BooleanUtils.toBoolean("false"));
        Assert.assertFalse(BooleanUtils.toBoolean("xyz123"));
    }

    @Test
    public void testToBoolean_String_String_String() {
        Assert.assertTrue(BooleanUtils.toBoolean((String) null, null, "false"));
        Assert.assertFalse(BooleanUtils.toBoolean((String) null, "true", null));
        Assert.assertTrue(BooleanUtils.toBoolean("true", "true", "false"));
        Assert.assertFalse(BooleanUtils.toBoolean("false", "true", "false"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_String_String_String_nullMatchFail() {
        BooleanUtils.toBoolean((String) null, "true", "false");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_String_String_String_noMatch() {
        BooleanUtils.toBoolean("other", "true", "false");
    }

    @Test
    public void testToStringTrueFalse_Boolean() {
        Assert.assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        Assert.assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
        Assert.assertNull(BooleanUtils.toStringTrueFalse((Boolean) null));
    }

    @Test
    public void testToStringOnOff_Boolean() {
        Assert.assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        Assert.assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        Assert.assertNull(BooleanUtils.toStringOnOff((Boolean) null));
    }

    @Test
    public void testToStringYesNo_Boolean() {
        Assert.assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        Assert.assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
        Assert.assertNull(BooleanUtils.toStringYesNo((Boolean) null));
    }

    @Test
    public void testToString_Boolean_String_String_String() {
        Assert.assertEquals("T", BooleanUtils.toString(Boolean.TRUE, "T", "F", "N"));
        Assert.assertEquals("F", BooleanUtils.toString(Boolean.FALSE, "T", "F", "N"));
        Assert.assertEquals("N", BooleanUtils.toString((Boolean) null, "T", "F", "N"));
    }

    @Test
    public void testToStringTrueFalse_boolean() {
        Assert.assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        Assert.assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

    @Test
    public void testToStringOnOff_boolean() {
        Assert.assertEquals("on", BooleanUtils.toStringOnOff(true));
        Assert.assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

    @Test
    public void testToStringYesNo_boolean() {
        Assert.assertEquals("yes", BooleanUtils.toStringYesNo(true));
        Assert.assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

    @Test
    public void testToString_boolean_String_String() {
        Assert.assertEquals("T", BooleanUtils.toString(true, "T", "F"));
        Assert.assertEquals("F", BooleanUtils.toString(false, "T", "F"));
    }

    @Test
    public void testXor_booleanArray() {
        Assert.assertTrue(BooleanUtils.xor(new boolean[]{true}));
        Assert.assertFalse(BooleanUtils.xor(new boolean[]{false}));
        Assert.assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        Assert.assertTrue(BooleanUtils.xor(new boolean[]{false, true}));
        Assert.assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        Assert.assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
        Assert.assertTrue(BooleanUtils.xor(new boolean[]{false, true, false}));
        Assert.assertFalse(BooleanUtils.xor(new boolean[]{true, true, false}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_booleanArray_null() {
        BooleanUtils.xor((boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_booleanArray_empty() {
        BooleanUtils.xor(new boolean[0]);
    }

    @Test
    public void testXor_BooleanArray() {
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE}));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE}));
        Assert.assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE}));
        Assert.assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.FALSE}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_BooleanArray_null() {
        BooleanUtils.xor((Boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_BooleanArray_empty() {
        BooleanUtils.xor(new Boolean[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_BooleanArray_containsNull() {
        BooleanUtils.xor(new Boolean[]{Boolean.TRUE, null});
    }
}
