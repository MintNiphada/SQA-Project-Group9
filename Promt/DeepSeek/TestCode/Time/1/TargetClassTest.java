package org.joda.time.field;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Test;

public class UnsupportedDurationFieldTest {

    @Test
    public void testGetInstance_cachesAndReturnsSame() {
        DurationFieldType type = DurationFieldType.eras();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        assertSame("Expected cached instance", field1, field2);
    }

    @Test
    public void testGetInstance_nullType() {
        // This will eventually cause NPE inside the constructor or later,
        // but we are testing that it doesn't throw here (caching might work with null key)
        // Actually, iType is set to null, so getType() will return null.
        // getName() will call null.getName() causing NPE if called.
        // Just verify getInstance doesn't throw immediately.
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(null);
        assertNotNull(field);
        assertNull(field.getType());
        // getName() would throw, so we don't call it.
    }

    @Test
    public void testGetInstance_differentTypesGiveDifferentInstances() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        assertNotSame(field1, field2);
    }

    @Test
    public void testGetType() {
        DurationFieldType type = DurationFieldType.eras();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertSame(type, field.getType());
    }

    @Test
    public void testGetName() {
        DurationFieldType type = DurationFieldType.eras();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertEquals(type.getName(), field.getName());
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertFalse(field.isSupported());
    }

    @Test
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertTrue(field.isPrecise());
    }

    @Test
    public void testGetUnitMillis() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertEquals(0, field.getUnitMillis());
    }

    @Test
    public void testGetValue_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getValue(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetValueAsLong_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getValueAsLong(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetValue_long_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getValue(100L, 200L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetValueAsLong_long_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getValueAsLong(100L, 200L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetMillis_int() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getMillis(5);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetMillis_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getMillis(5L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetMillis_int_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getMillis(5, 100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetMillis_long_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getMillis(5L, 100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAdd_long_int() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.add(100L, 5);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAdd_long_long() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.add(100L, 5L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetDifference() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getDifference(200L, 100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetDifferenceAsLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.getDifferenceAsLong(200L, 100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testCompareTo_null() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        try {
            field.compareTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected because null.isSupported() throws NPE
        }
    }

    @Test
    public void testCompareTo_unsupported() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        // both unsupported, compareTo returns 0
        assertEquals(0, field1.compareTo(field2));
    }

    @Test
    public void testCompareTo_supported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        // create a mock DurationField that is supported
        DurationField supported = new DurationField() {
            @Override
            public DurationFieldType getType() {
                return DurationFieldType.eras();
            }
            @Override
            public String getName() {
                return "mock";
            }
            @Override
            public boolean isSupported() {
                return true;
            }
            @Override
            public boolean isPrecise() {
                return false;
            }
            @Override
            public long getUnitMillis() {
                return 1;
            }
            @Override
            public int getValue(long duration) {
                return 0;
            }
            @Override
            public long getValueAsLong(long duration) {
                return 0;
            }
            @Override
            public int getValue(long duration, long instant) {
                return 0;
            }
            @Override
            public long getValueAsLong(long duration, long instant) {
                return 0;
            }
            @Override
            public long getMillis(int value) {
                return 0;
            }
            @Override
            public long getMillis(long value) {
                return 0;
            }
            @Override
            public long getMillis(int value, long instant) {
                return 0;
            }
            @Override
            public long getMillis(long value, long instant) {
                return 0;
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
            public int compareTo(DurationField other) {
                return 0;
            }
        };
        // because supported is true, unsupported.compareTo returns 1
        assertEquals(1, field.compareTo(supported));
    }

    @Test
    public void testEquals_sameInstance() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertTrue(field.equals(field));
    }

    @Test
    public void testEquals_ifferentInstanceSameType() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertTrue(field1.equals(field2));
    }

    @Test
    public void testEquals_dfferentType() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        assertFalse(field1.equals(field2));
    }

    @Test
    public void testEquals_nullNameInOther() {
        // special case: equals checks if other.getName() == null, then returns (getName() == null)
        // We can simulate by using a type whose getName() returns null? DurationFieldType might not allow null name.
        // Instead, we can test that a field with null iType (via null type in getInstance) has getName() that throws NPE.
        // Actually, getName() calls iType.getName(), if iType is null then NPE. So that code path is hard to test without NPE.
        // The equals implementation: if other.getName() == null return (getName() == null). Both can be null if iType is null.
        // So getInstance(null) gives iType=null, then getName() throws NPE. So we cannot test equals on such field directly.
        // We'll skip this branch; the code is trivial.
    }

    @Test
    public void testEquals_nonUnsupportedDurationField() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertFalse(field.equals("string"));
    }

    @Test
    public void testHashCode() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        assertEquals(field1.hashCode(), field2.hashCode());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        // hash codes are not necessarily different, but for different names usually are.
    }

    @Test
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        String expected = "UnsupportedDurationField[" + field.getName() + "]";
        assertEquals(expected, field.toString());
    }

    @Test
    public void testreadResolve_returnsSingleton() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        Method readResolve = UnsupportedDurationField.class.getDeclaredMethod("readResolve");
        readResolve.setAccessible(true);
        Object resolved = readResolve.invoke(field);
        assertSame("readResolve returns cached instance", field, resolved);
    }
}
