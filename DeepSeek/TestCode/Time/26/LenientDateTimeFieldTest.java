package org.joda.time.field;

import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import static org.junit.Assert.*;

public class LenientDateTimeFieldTest {

    @Test
    public void testGetInstance_nullField_returnsNull() {
        assertNull(LenientDateTimeField.getInstance(null, ISOChronology.getInstanceUTC()));
    }

    @Test
    public void testGetInstance_alreadyLenient_returnsSame() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        assertTrue(lenient.isLenient());
        assertSame(lenient, LenientDateTimeField.getInstance(lenient, base));
    }

    @Test
    public void testGetInstance_strictField_returnsLenientWithWrappedField() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField original = base.dayOfMonth();
        DateTimeField strict = StrictDateTimeField.getInstance(original);
        DateTimeField result = LenientDateTimeField.getInstance(strict, base);
        assertTrue(result.isLenient());
        assertNotSame(strict, result);
        assertEquals(original.getType(), result.getType());
    }

    @Test
    public void testGetInstance_nonLenient_returnsNewLenientField() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField result = LenientDateTimeField.getInstance(field, base);
        assertTrue(result.isLenient());
        assertNotSame(field, result);
        assertEquals(field.getType(), result.getType());
    }

    @Test
    public void testIsLenient_returnsTrue() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        assertTrue(lenient.isLenient());
    }

    @Test
    public void testSet_withinBounds_noChange() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 6, 15, 0, 0, 0, 0);
        long result = lenient.set(instant, 15);
        assertEquals(instant, result);
    }

    @Test
    public void testSet_outOfBounds_positiveOverflow_addsDifference() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 6, 15, 0, 0, 0, 0);
        long expected = base.getDateTimeMillis(2020, 6, 25, 0, 0, 0, 0);
        long result = lenient.set(instant, 25);
        assertEquals(expected, result);
    }

    @Test
    public void testSet_outOfBounds_negativeOverflow_subtractsDifference() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 6, 15, 0, 0, 0, 0);
        long expected = base.getDateTimeMillis(2020, 5, 31, 0, 0, 0, 0);
        long result = lenient.set(instant, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testSet_timeZoneConversion() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology base = ISOChronology.getInstance(zone);
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 6, 15, 12, 0, 0, 0);
        long result = lenient.set(instant, 25);
        long expected = base.getDateTimeMillis(2020, 6, 25, 12, 0, 0, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testSet_valueSameAsCurrent_noChange() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 6, 15, 0, 0, 0, 0);
        long result = lenient.set(instant, 15);
        assertEquals(instant, result);
    }

    @Test
    public void testSet_largeOverflow() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 1, 1, 0, 0, 0, 0);
        long result = lenient.set(instant, 400);
        long expected = base.getDateTimeMillis(2021, 2, 4, 0, 0, 0, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testSet_negativeLargeOverflow() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeField field = base.dayOfMonth();
        DateTimeField lenient = LenientDateTimeField.getInstance(field, base);
        long instant = base.getDateTimeMillis(2020, 12, 31, 0, 0, 0, 0);
        long result = lenient.set(instant, -100);
        long expected = base.getDateTimeMillis(2019, 9, 22, 0, 0, 0, 0);
        assertEquals(expected, result);
    }
}
