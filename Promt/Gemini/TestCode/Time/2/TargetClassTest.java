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
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.days());

        Assert.assertNotNull(field1);
        Assert.assertSame(field1, field2);
        Assert.assertNotSame(field1, field3);
    }

    @Test
    public void testGetTypeAndName() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        Assert.assertEquals(DurationFieldType.years(), field.getType());
        Assert.assertEquals("years", field.getName());
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        Assert.assertFalse(field.isSupported());
    }

    @Test
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        Assert.assertTrue(field.isPrecise());
    }

    @Test
    public void testGetUnitMillis() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        Assert.assertEquals(0L, field.getUnitMillis());
    }

    @Test
    public void testCompareTo() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.days());
        Assert.assertEquals(0, field1.compareTo(field2));
        Assert.assertEquals(0, field1.compareTo(field1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueDuration() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getValue(1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongDuration() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getValueAsLong(1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueDurationInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getValue(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongDurationInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getValueAsLong(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisInt() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getMillis(10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getMillis(10L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisIntInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getMillis(10, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLongInstant() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getMillis(10L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddInstantInt() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).add(1000L, 5);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddInstantLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).add(1000L, 5L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getDifference(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong() {
        UnsupportedDurationField.getInstance(DurationFieldType.hours()).getDifferenceAsLong(1000L, 500L);
    }

    @Test
    public void testEqualsAndHashCode() {
        UnsupportedDurationField fieldHours1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField fieldHours2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField fieldDays = UnsupportedDurationField.getInstance(DurationFieldType.days());

        Assert.assertTrue(fieldHours1.equals(fieldHours1));
        Assert.assertTrue(fieldHours1.equals(fieldHours2));
        Assert.assertFalse(fieldHours1.equals(fieldDays));
        Assert.assertFalse(fieldHours1.equals(null));
        Assert.assertFalse(fieldHours1.equals("hours"));

        Assert.assertEquals(fieldHours1.hashCode(), fieldHours2.hashCode());
        Assert.assertEquals("hours".hashCode(), fieldHours1.hashCode());
    }

    @Test
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        Assert.assertEquals("UnsupportedDurationField[months]", field.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertSame(original, deserialized);
    }
}
