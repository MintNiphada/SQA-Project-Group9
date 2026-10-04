package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {

    @Test
    public void testPrimitiveTypeOfWithPrimitive() {
        assertEquals(int.class, Primitives.primitiveTypeOf(int.class));
        assertEquals(boolean.class, Primitives.primitiveTypeOf(boolean.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(double.class));
    }

    @Test
    public void testPrimitiveTypeOfWithWrapper() {
        assertEquals(int.class, Primitives.primitiveTypeOf(Integer.class));
        assertEquals(boolean.class, Primitives.primitiveTypeOf(Boolean.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(Double.class));
    }

    @Test
    public void testPrimitiveTypeOfWithNonPrimitiveNonWrapper() {
        assertNull(Primitives.primitiveTypeOf(String.class));
        assertNull(Primitives.primitiveTypeOf(Object.class));
    }

    @Test(expected = NullPointerException.class)
    public void testPrimitiveTypeOfWithNull() {
        Primitives.primitiveTypeOf(null);
    }

    @Test
    public void testIsPrimitiveWrapperWithWrapper() {
        assertTrue(Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue(Primitives.isPrimitiveWrapper(Character.class));
    }

    @Test
    public void testIsPrimitiveWrapperWithPrimitive() {
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
    }

    @Test
    public void testIsPrimitiveWrapperWithNonWrapper() {
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(Object.class));
    }

    @Test
    public void testIsPrimitiveWrapperWithNull() {
        assertFalse(Primitives.isPrimitiveWrapper(null));
    }

    @Test
    public void testPrimitiveWrapperOf() {
        assertEquals(Boolean.FALSE, Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveWrapperOf(Character.class));
        assertEquals(Byte.valueOf((byte)0), Primitives.primitiveWrapperOf(Byte.class));
        assertEquals(Short.valueOf((short)0), Primitives.primitiveWrapperOf(Short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(Long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveWrapperOf(Float.class));
        assertEquals(Double.valueOf(0D), Primitives.primitiveWrapperOf(Double.class));
    }

    @Test
    public void testPrimitiveWrapperOfWithNonWrapper() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
        assertNull(Primitives.primitiveWrapperOf(Object.class));
    }

    @Test
    public void testPrimitiveWrapperOfWithNull() {
        assertNull(Primitives.primitiveWrapperOf(null));
    }

    @Test
    public void testPrimitiveValueOrNullFor() {
        assertEquals(false, Primitives.primitiveValueOrNullFor(boolean.class));
        assertEquals('\u0000', Primitives.primitiveValueOrNullFor(char.class));
        assertEquals((byte)0, Primitives.primitiveValueOrNullFor(byte.class));
        assertEquals((short)0, Primitives.primitiveValueOrNullFor(short.class));
        assertEquals(0, Primitives.primitiveValueOrNullFor(int.class));
        assertEquals(0L, Primitives.primitiveValueOrNullFor(long.class));
        assertEquals(0F, Primitives.primitiveValueOrNullFor(float.class));
        assertEquals(0D, Primitives.primitiveValueOrNullFor(double.class));
    }

    @Test
    public void testPrimitiveValueOrNullForWithNonPrimitive() {
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
        assertNull(Primitives.primitiveValueOrNullFor(Integer.class));
    }

    @Test
    public void testPrimitiveValueOrNullForWithNull() {
        assertNull(Primitives.primitiveValueOrNullFor(null));
    }
}
