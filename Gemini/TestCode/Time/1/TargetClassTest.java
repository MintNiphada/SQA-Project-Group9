package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Assert;
import org.junit.Test;

public class UnsupportedDurationFieldTest {

    @Test
    public void testGetInstance() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());

        Assert.assertNotNull(field1);
        Assert.assertSame(field1, field2);
        Assert.assertNotSame(field1, field3);
        Assert.assertEquals(DurationFieldType.seconds(), field1.getType());
        Assert.assertEquals(DurationFieldType.minutes(), field3.getType());
    }

    @Test
    public void testGetName() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        Assert.assertEquals("hours", field.getName());
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        Assert.assertFalse(field.isSupported());
    }

    @Test
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        Assert.assertTrue(field.isPrecise());
    }

    @Test
    public void testGetUnitMillis() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        Assert.assertEquals(0L, field.getUnitMillis());
    }

    @Test
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        Assert.assertEquals("UnsupportedDurationField[months]", field.toString());
    }

    @Test
    public void testCompareTo() {
        UnsupportedDurationField unsupp1 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        UnsupportedDurationField unsupp2 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());

        Assert.assertEquals(0, unsupp1.compareTo(unsupp2));

        DurationField supportedField = MillisDurationField.INSTANCE;
        Assert.assertEquals(1, unsupp1.compareTo(supportedField));
    }

    @Test
    public void testEqualsAndHashCode() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.months());

        Assert.assertTrue(field1.equals(field1));
        Assert.assertTrue(field1.equals(field2));
        Assert.assertFalse(field1.equals(field3));
        Assert.assertFalse(field1.equals(null));
        Assert.assertFalse(field1.equals("years"));

        Assert.assertEquals(field1.hashCode(), field2.hashCode());
        Assert.assertEquals("years".hashCode(), field1.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertSame(field, deserialized);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueDuration() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getValue(100L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongDuration() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getValueAsLong(100L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueDurationInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getValue(100L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongDurationInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getValueAsLong(100L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisInt() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(10L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisIntInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(10, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLongInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(10L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddInstantInt() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).add(500L, 10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddInstantLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).add(500L, 10L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getDifference(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getDifferenceAsLong(1000L, 500L);
    }

    @Test
    public void testUnsupportedExceptionMessage() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        try {
            field.getValue(1234L);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("halfdays field is unsupported", ex.getMessage());
        }
    }
}
