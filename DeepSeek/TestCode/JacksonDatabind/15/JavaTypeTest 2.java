package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;
import org.junit.Test;

public class JavaTypeTest {

    // Concrete subclass for testing
    static class TestJavaType extends JavaType {
        private final Class<?> raw;
        private final int additionalHash;
        private final Object valueHandler;
        private final Object typeHandler;
        private final boolean asStatic;
        private boolean containerType;

        public TestJavaType(Class<?> raw, int additionalHash, Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
            this.raw = raw;
            this.additionalHash = additionalHash;
            this.valueHandler = valueHandler;
            this.typeHandler = typeHandler;
            this.asStatic = asStatic;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new TestJavaType(raw, additionalHash, valueHandler, h, asStatic);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new TestJavaType(raw, additionalHash, h, typeHandler, asStatic);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return new TestJavaType(raw, additionalHash, valueHandler, typeHandler, true);
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new TestJavaType(subclass, 0, null, null, asStatic);
        }

        @Override
        public JavaType narrowContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public JavaType widenContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public boolean isContainerType() {
            return containerType;
        }

        public void setContainerType(boolean containerType) {
            this.containerType = containerType;
        }

        @Override
        public Class<?> getParameterSource() {
            return null;
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append("generic");
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append("erased");
            return sb;
        }

        @Override
        public String toString() {
            return "TestJavaType";
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TestJavaType) {
                TestJavaType other = (TestJavaType) o;
                return _class == other._class &&
                        _valueHandler == other._valueHandler &&
                        _typeHandler == other._typeHandler &&
                        _asStatic == other._asStatic;
            }
            return false;
        }

        // Expose protected method for testing
        public void assertSubclass(Class<?> subclass, Class<?> superClass) {
            _assertSubclass(subclass, superClass);
        }
    }

    // Helper to create a default TestJavaType
    private TestJavaType createType(Class<?> raw) {
        return new TestJavaType(raw, 0, null, null, false);
    }

    @Test
    public void testConstructorAndHash() {
        TestJavaType type = new TestJavaType(String.class, 5, "vh", "th", true);
        assertEquals(String.class, type.getRawClass());
        assertEquals("vh", type.getValueHandler());
        assertEquals("th", type.getTypeHandler());
        assertTrue(type.useStaticType());
        int expectedHash = String.class.getName().hashCode() + 5;
        assertEquals(expectedHash, type.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullRaw() {
        new TestJavaType(null, 0, null, null, false);
    }

    @Test
    public void testGetRawClass() {
        TestJavaType type = createType(Integer.class);
        assertEquals(Integer.class, type.getRawClass());
    }

    @Test
    public void testHasRawClass() {
        TestJavaType type = createType(String.class);
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Integer.class));
    }

    @Test
    public void testIsAbstract() {
        assertTrue(createType(java.util.AbstractList.class).isAbstract());
        assertTrue(createType(java.util.List.class).isAbstract()); // interface
        assertFalse(createType(java.util.ArrayList.class).isAbstract());
        assertTrue(createType(int.class).isAbstract()); // primitive has abstract flag
    }

    @Test
    public void testIsConcrete() {
        assertTrue(createType(java.util.ArrayList.class).isConcrete());
        assertFalse(createType(java.util.AbstractList.class).isConcrete());
        assertFalse(createType(java.util.List.class).isConcrete());
        assertTrue(createType(int.class).isConcrete()); // primitive is concrete despite abstract flag
    }

    @Test
    public void testIsThrowable() {
        assertTrue(createType(Exception.class).isThrowable());
        assertTrue(createType(Error.class).isThrowable());
        assertFalse(createType(Object.class).isThrowable());
    }

    @Test
    public void testIsArrayType() {
        assertFalse(createType(String.class).isArrayType());
    }

    @Test
    public void testIsEnumType() {
        assertTrue(createType(java.lang.annotation.RetentionPolicy.class).isEnumType());
        assertFalse(createType(String.class).isEnumType());
    }

    @Test
    public void testIsInterface() {
        assertTrue(createType(java.util.List.class).isInterface());
        assertFalse(createType(java.util.ArrayList.class).isInterface());
    }

    @Test
    public void testIsPrimitive() {
        assertTrue(createType(int.class).isPrimitive());
        assertFalse(createType(Integer.class).isPrimitive());
    }

    @Test
    public void testIsFinal() {
        assertTrue(createType(String.class).isFinal());
        assertFalse(createType(java.util.ArrayList.class).isFinal());
    }

    @Test
    public void testIsContainerType() {
        TestJavaType type = createType(String.class);
        assertFalse(type.isContainerType());
        type.setContainerType(true);
        assertTrue(type.isContainerType());
    }

    @Test
    public void testIsCollectionLikeType() {
        assertFalse(createType(String.class).isCollectionLikeType());
    }

    @Test
    public void testIsMapLikeType() {
        assertFalse(createType(String.class).isMapLikeType());
    }

    @Test
    public void testUseStaticType() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, true);
        assertTrue(type.useStaticType());
        type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.useStaticType());
    }

    @Test
    public void testHasGenericTypes() {
        TestJavaType type = createType(String.class);
        assertFalse(type.hasGenericTypes());
        // Override containedTypeCount to return >0
        JavaType custom = new TestJavaType(String.class, 0, null, null, false) {
            @Override
            public int containedTypeCount() { return 1; }
        };
        assertTrue(custom.hasGenericTypes());
    }

    @Test
    public void testGetKeyType() {
        assertNull(createType(String.class).getKeyType());
    }

    @Test
    public void testGetContentType() {
        assertNull(createType(String.class).getContentType());
    }

    @Test
    public void testContainedTypeCount() {
        assertEquals(0, createType(String.class).containedTypeCount());
    }

    @Test
    public void testContainedType() {
        assertNull(createType(String.class).containedType(0));
    }

    @Test
    public void testContainedTypeName() {
        assertNull(createType(String.class).containedTypeName(0));
    }

    @Test
    public void testContainedTypeOrUnknown() {
        TestJavaType type = createType(String.class);
        JavaType unknown = type.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        // When containedType returns non-null
        final JavaType customType = createType(Integer.class);
        JavaType custom = new TestJavaType(String.class, 0, null, null, false) {
            @Override
            public JavaType containedType(int index) {
                return customType;
            }
        };
        assertSame(customType, custom.containedTypeOrUnknown(0));
    }

    @Test
    public void testGetValueHandler() {
        Object handler = new Object();
        TestJavaType type = new TestJavaType(String.class, 0, handler, null, false);
        assertSame(handler, type.getValueHandler());
    }

    @Test
    public void testGetTypeHandler() {
        Object handler = new Object();
        TestJavaType type = new TestJavaType(String.class, 0, null, handler, false);
        assertSame(handler, type.getTypeHandler());
    }

    @Test
    public void testGetGenericSignature() {
        TestJavaType type = createType(String.class);
        assertEquals("generic", type.getGenericSignature());
        StringBuilder sb = new StringBuilder();
        type.getGenericSignature(sb);
        assertEquals("generic", sb.toString());
    }

    @Test
    public void testGetErasedSignature() {
        TestJavaType type = createType(String.class);
        assertEquals("erased", type.getErasedSignature());
        StringBuilder sb = new StringBuilder();
        type.getErasedSignature(sb);
        assertEquals("erased", sb.toString());
    }

    @Test
    public void testHashCode() {
        TestJavaType type = new TestJavaType(String.class, 10, null, null, false);
        assertEquals(String.class.getName().hashCode() + 10, type.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("TestJavaType", createType(String.class).toString());
    }

    @Test
    public void testEquals() {
        TestJavaType type1 = new TestJavaType(String.class, 0, "vh", "th", true);
        TestJavaType type2 = new TestJavaType(String.class, 0, "vh", "th", true);
        TestJavaType type3 = new TestJavaType(String.class, 0, "vh", "th", false);
        TestJavaType type4 = new TestJavaType(Integer.class, 0, "vh", "th", true);
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(type3));
        assertFalse(type1.equals(type4));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("string"));
    }

    @Test
    public void testAssertSubclassValid() {
        TestJavaType type = createType(Number.class);
        type.assertSubclass(Integer.class, Number.class); // no exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssertSubclassInvalid() {
        TestJavaType type = createType(Number.class);
        type.assertSubclass(String.class, Number.class);
    }

    @Test
    public void testNarrowBySameClass() {
        TestJavaType type = createType(String.class);
        assertSame(type, type.narrowBy(String.class));
    }

    @Test
    public void testNarrowByValidSubclass() {
        TestJavaType type = new TestJavaType(Number.class, 0, "vh", "th", false);
        JavaType narrowed = type.narrowBy(Integer.class);
        assertNotSame(type, narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals("vh", narrowed.getValueHandler());
        assertEquals("th", narrowed.getTypeHandler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNarrowByInvalidSubclass() {
        TestJavaType type = createType(Number.class);
        type.narrowBy(String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testNarrowByNull() {
        TestJavaType type = createType(Number.class);
        type.narrowBy(null);
    }

    @Test
    public void testForcedNarrowBySameClass() {
        TestJavaType type = createType(String.class);
        assertSame(type, type.forcedNarrowBy(String.class));
    }

    @Test
    public void testForcedNarrowByDifferentClass() {
        TestJavaType type = new TestJavaType(Number.class, 0, "vh", "th", false);
        JavaType narrowed = type.forcedNarrowBy(Integer.class);
        assertNotSame(type, narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals("vh", narrowed.getValueHandler());
        assertEquals("th", narrowed.getTypeHandler());
    }

    @Test
    public void testForcedNarrowByNonAssignable() {
        // forcedNarrowBy does not check assignability, so no exception
        TestJavaType type = new TestJavaType(Number.class, 0, "vh", "th", false);
        JavaType narrowed = type.forcedNarrowBy(String.class);
        assertEquals(String.class, narrowed.getRawClass());
        assertEquals("vh", narrowed.getValueHandler());
        assertEquals("th", narrowed.getTypeHandler());
    }

    @Test
    public void testWidenBySameClass() {
        TestJavaType type = createType(String.class);
        assertSame(type, type.widenBy(String.class));
    }

    @Test
    public void testWidenByValidSuperclass() {
        TestJavaType type = createType(Integer.class);
        JavaType widened = type.widenBy(Number.class);
        assertNotSame(type, widened);
        assertEquals(Number.class, widened.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWidenByInvalidSuperclass() {
        TestJavaType type = createType(Number.class);
        type.widenBy(String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testWidenByNull() {
        TestJavaType type = createType(Number.class);
        type.widenBy(null);
    }

    @Test
    public void testNarrowContentsBy() {
        TestJavaType type = createType(String.class);
        assertSame(type, type.narrowContentsBy(Integer.class));
    }

    @Test
    public void testWidenContentsBy() {
        TestJavaType type = createType(String.class);
        assertSame(type, type.widenContentsBy(Integer.class));
    }
}
