package org.joda.time.field;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;

public class UnsupportedDurationFieldTest {

    private DurationFieldType millisType;
    private DurationFieldType secondsType;

    @Before
    public void setUp() {
        millisType = DurationFieldType.millis();
        secondsType = DurationFieldType.seconds();
    }

    // Test getInstance caching and null type
    @Test
    public void testGetInstanceCaching() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(millisType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(millisType);
        assertSame("Same type should return cached instance", field1, field2);

        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(secondsType);
        assertNotSame("Different type should return different instance", field1, field3);
    }

    @Test
    public void testGetInstanceWithNullType() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(null);
        assertNotNull(field);
        assertNull(field.getType());
    }

    @Test
    public void testGetType() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertEquals(millisType, field.getType());
    }

    @Test
    public void testGetName() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertEquals("millis", field.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNameWithNullType() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(null);
        field.getName();
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertFalse(field.isSupported());
    }

    @Test
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertTrue(field.isPrecise());
    }

    // Test all methods that throw UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueLong() {
        UnsupportedDurationField.getInstance(millisType).getValue(0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongLong() {
        UnsupportedDurationField.getInstance(millisType).getValueAsLong(0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueLongLong() {
        UnsupportedDurationField.getInstance(millisType).getValue(0L, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongLongLong() {
        UnsupportedDurationField.getInstance(millisType).getValueAsLong(0L, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisInt() {
        UnsupportedDurationField.getInstance(millisType).getMillis(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLong() {
        UnsupportedDurationField.getInstance(millisType).getMillis(0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisIntLong() {
        UnsupportedDurationField.getInstance(millisType).getMillis(0, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLongLong() {
        UnsupportedDurationField.getInstance(millisType).getMillis(0L, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongInt() {
        UnsupportedDurationField.getInstance(millisType).add(0L, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongLong() {
        UnsupportedDurationField.getInstance(millisType).add(0L, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference() {
        UnsupportedDurationField.getInstance(millisType).getDifference(0L, 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong() {
        UnsupportedDurationField.getInstance(millisType).getDifferenceAsLong(0L, 0L);
    }

    @Test
    public void testGetUnitMillis() {
        assertEquals(0, UnsupportedDurationField.getInstance(millisType).getUnitMillis());
    }

    @Test
    public void testCompareTo() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        // compareTo should return 0 for any DurationField
        assertEquals(0, field.compareTo(field));
        assertEquals(0, field.compareTo(UnsupportedDurationField.getInstance(secondsType)));
        // test with a mock DurationField (anonymous)
        assertEquals(0, field.compareTo(new DurationField() {
            @Override
            public DurationFieldType getType() { return null; }
            @Override
            public String getName() { return null; }
            @Override
            public boolean isSupported() { return false; }
            @Override
            public boolean isPrecise() { return false; }
            @Override
            public long getUnitMillis() { return 0; }
            @Override
            public int getValue(long duration) { return 0; }
            @Override
            public long getValueAsLong(long duration) { return 0; }
            @Override
            public int getValue(long duration, long instant) { return 0; }
            @Override
            public long getValueAsLong(long duration, long instant) { return 0; }
            @Override
            public long getMillis(int value) { return 0; }
            @Override
            public long getMillis(long value) { return 0; }
            @Override
            public long getMillis(int value, long instant) { return 0; }
            @Override
            public long getMillis(long value, long instant) { return 0; }
            @Override
            public long add(long instant, int value) { return 0; }
            @Override
            public long add(long instant, long value) { return 0; }
            @Override
            public int getDifference(long minuendInstant, long subtrahendInstant) { return 0; }
            @Override
            public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) { return 0; }
            @Override
            public int compareTo(DurationField other) { return 0; }
        }));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        UnsupportedDurationField.getInstance(millisType).compareTo(null);
    }

    // equals tests
    @Test
    public void testEqualsSameObject() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertTrue(field.equals(field));
    }

    @Test
    public void testEqualsSameName() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(millisType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(millisType);
        assertTrue(field1.equals(field2));
    }

    @Test
    public void testEqualsDifferentName() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(millisType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(secondsType);
        assertFalse(field1.equals(field2));
    }

    @Test
    public void testEqualsWithNullNames() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(null);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(null);
        assertTrue(field1.equals(field2));
    }

    @Test
    public void testEqualsOneNullName() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(millisType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(null);
        assertFalse(field1.equals(field2));
        assertFalse(field2.equals(field1));
    }

    @Test
    public void testEqualsNonUnsupportedDurationField() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertFalse(field.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertEquals("millis".hashCode(), field.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testHashCodeWithNullType() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(null);
        field.hashCode();
    }

    @Test
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(millisType);
        assertEquals("UnsupportedDurationField[millis]", field.toString());
    }

    @Test
    public void testToStringWithNullType() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(null);
        assertEquals("UnsupportedDurationField[null]", field.toString());
    }

    @Test
    public void testExceptionMessage() {
        try {
            UnsupportedDurationField.getInstance(millisType).getValue(0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("millis"));
            assertTrue(e.getMessage().contains("unsupported"));
        }
    }

    // Test serialization and readResolve
    @Test
    public void testSerializationReadResolve() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(millisType);
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnsupportedDurationField deserialized = (UnsupportedDurationField) ois.readObject();
        ois.close();
        // readResolve should return the cached instance, which is the same as original
        assertSame(original, deserialized);
    }

    // Test that cache can be cleared and new instance created (optional, but covers getInstance branches)
    @Test
    public void testGetInstanceAfterCacheClear() throws Exception {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(millisType);
        // Clear cache via reflection
        Field cacheField = UnsupportedDurationField.class.getDeclaredField("cCache");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(millisType);
        assertNotSame("After cache clear, new instance should be created", field1, field2);
    }
}
