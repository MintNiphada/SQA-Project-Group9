package com.fasterxml.jackson.databind;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeTest {

    private static class DummyJavaType extends JavaType {
        private static final long serialVersionUID = 1L;

        private final JavaType _contained;
        private final String _containedName;

        public DummyJavaType(Class<?> raw) {
            this(raw, 0, null, null, false, null, null);
        }

        public DummyJavaType(Class<?> raw, int additionalHash, Object valueHandler, Object typeHandler, boolean asStatic) {
            this(raw, additionalHash, valueHandler, typeHandler, asStatic, null, null);
        }

        public DummyJavaType(Class<?> raw, int additionalHash, Object valueHandler, Object typeHandler,
                             boolean asStatic, JavaType contained, String containedName) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
            this._contained = contained;
            this._containedName = containedName;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), _valueHandler, h, _asStatic, _contained, _containedName);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), h, _typeHandler, _asStatic, _contained, _containedName);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), _valueHandler, _typeHandler, true, _contained, _containedName);
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new DummyJavaType(subclass, _hash - _class.getName().hashCode(), null, null, _asStatic, _contained, _containedName);
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
            return false;
        }

        @Override
        public Class<?> getParameterSource() {
            return _class;
        }

        @Override
        public int containedTypeCount() {
            return _contained != null ? 1 : 0;
        }

        @Override
        public JavaType containedType(int index) {
            return (index == 0) ? _contained : null;
        }

        @Override
        public String containedTypeName(int index) {
            return (index == 0) ? _containedName : null;
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append("L").append(_class.getName().replace('.', '/')).append(";");
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append("L").append(_class.getName().replace('.', '/')).append(";");
            return sb;
        }

        @Override
        public String toString() {
            return "[DummyJavaType: " + _class.getName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            DummyJavaType other = (DummyJavaType) o;
            return other._class == this._class;
        }
    }

    private enum TestEnum { A, B }

    private abstract static class AbstractTestClass {}

    @Test
    public void testConstructorAndGetters() {
        Object valHandler = "valHandler";
        Object typeHandler = "typeHandler";
        DummyJavaType type = new DummyJavaType(String.class, 123, valHandler, typeHandler, true);

        Assert.assertSame(String.class, type.getRawClass());
        Assert.assertTrue(type.hasRawClass(String.class));
        Assert.assertFalse(type.hasRawClass(Integer.class));
        Assert.assertEquals(String.class.getName().hashCode() + 123, type.hashCode());
        Assert.assertEquals(valHandler, type.getValueHandler());
        Assert.assertEquals(typeHandler, type.getTypeHandler());
        Assert.assertTrue(type.useStaticType());
    }

    @Test
    public void testClassCharacteristics() {
        DummyJavaType strType = new DummyJavaType(String.class);
        Assert.assertTrue(strType.isConcrete());
        Assert.assertFalse(strType.isAbstract());
        Assert.assertFalse(strType.isPrimitive());
        Assert.assertFalse(strType.isInterface());
        Assert.assertFalse(strType.isEnumType());
        Assert.assertFalse(strType.isThrowable());
        Assert.assertTrue(strType.isFinal());
        Assert.assertFalse(strType.isArrayType());
        Assert.assertFalse(strType.isCollectionLikeType());
        Assert.assertFalse(strType.isMapLikeType());

        DummyJavaType primType = new DummyJavaType(int.class);
        Assert.assertTrue(primType.isPrimitive());
        Assert.assertTrue(primType.isConcrete());
        Assert.assertFalse(primType.isAbstract());
        Assert.assertTrue(primType.isFinal());

        DummyJavaType ifaceType = new DummyJavaType(List.class);
        Assert.assertTrue(ifaceType.isInterface());
        Assert.assertTrue(ifaceType.isAbstract());
        Assert.assertFalse(ifaceType.isConcrete());
        Assert.assertFalse(ifaceType.isFinal());

        DummyJavaType abstractType = new DummyJavaType(AbstractTestClass.class);
        Assert.assertTrue(abstractType.isAbstract());
        Assert.assertFalse(abstractType.isConcrete());
        Assert.assertFalse(abstractType.isInterface());

        DummyJavaType enumType = new DummyJavaType(TestEnum.class);
        Assert.assertTrue(enumType.isEnumType());
        Assert.assertTrue(enumType.isConcrete());

        DummyJavaType exType = new DummyJavaType(Exception.class);
        Assert.assertTrue(exType.isThrowable());
    }

    @Test
    public void testNarrowBySameClass() {
        DummyJavaType type = new DummyJavaType(Number.class);
        JavaType narrowed = type.narrowBy(Number.class);
        Assert.assertSame(type, narrowed);
    }

    @Test
    public void testNarrowBySubclassWithHandlers() {
        Object valHandler = "valH";
        Object typeHandler = "typeH";
        DummyJavaType type = new DummyJavaType(Number.class, 0, valHandler, typeHandler, false);
        JavaType narrowed = type.narrowBy(Integer.class);

        Assert.assertNotSame(type, narrowed);
        Assert.assertSame(Integer.class, narrowed.getRawClass());
        Assert.assertEquals(valHandler, narrowed.getValueHandler());
        Assert.assertEquals(typeHandler, narrowed.getTypeHandler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNarrowByInvalidSubclassThrows() {
        DummyJavaType type = new DummyJavaType(Number.class);
        type.narrowBy(String.class);
    }

    @Test
    public void testForcedNarrowBySameClass() {
        DummyJavaType type = new DummyJavaType(Number.class);
        JavaType narrowed = type.forcedNarrowBy(Number.class);
        Assert.assertSame(type, narrowed);
    }

    @Test
    public void testForcedNarrowBySubclass() {
        Object valHandler = "valH";
        Object typeHandler = "typeH";
        DummyJavaType type = new DummyJavaType(Number.class, 0, valHandler, typeHandler, false);
        JavaType narrowed = type.forcedNarrowBy(Integer.class);

        Assert.assertNotSame(type, narrowed);
        Assert.assertSame(Integer.class, narrowed.getRawClass());
        Assert.assertEquals(valHandler, narrowed.getValueHandler());
        Assert.assertEquals(typeHandler, narrowed.getTypeHandler());
    }

    @Test
    public void testWidenBySameClass() {
        DummyJavaType type = new DummyJavaType(ArrayList.class);
        JavaType widened = type.widenBy(ArrayList.class);
        Assert.assertSame(type, widened);
    }

    @Test
    public void testWidenBySuperclass() {
        DummyJavaType type = new DummyJavaType(ArrayList.class);
        JavaType widened = type.widenBy(AbstractList.class);
        Assert.assertSame(AbstractList.class, widened.getRawClass());

        JavaType widenedToInterface = type.widenBy(List.class);
        Assert.assertSame(List.class, widenedToInterface.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWidenByInvalidSuperclassThrows() {
        DummyJavaType type = new DummyJavaType(ArrayList.class);
        type.widenBy(String.class);
    }

    @Test
    public void testTypeParametersDefaults() {
        DummyJavaType type = new DummyJavaType(String.class);
        Assert.assertFalse(type.hasGenericTypes());
        Assert.assertNull(type.getKeyType());
        Assert.assertNull(type.getContentType());
        Assert.assertEquals(0, type.containedTypeCount());
        Assert.assertNull(type.containedType(0));
        Assert.assertNull(type.containedTypeName(0));

        JavaType unknown = type.containedTypeOrUnknown(0);
        Assert.assertNotNull(unknown);
        Assert.assertSame(TypeFactory.unknownType().getRawClass(), unknown.getRawClass());
    }

    @Test
    public void testTypeParametersPresent() {
        DummyJavaType elemType = new DummyJavaType(String.class);
        DummyJavaType type = new DummyJavaType(List.class, 0, null, null, false, elemType, "E");

        Assert.assertTrue(type.hasGenericTypes());
        Assert.assertEquals(1, type.containedTypeCount());
        Assert.assertSame(elemType, type.containedType(0));
        Assert.assertNull(type.containedType(1));
        Assert.assertEquals("E", type.containedTypeName(0));
        Assert.assertNull(type.containedTypeName(1));

        JavaType resolved = type.containedTypeOrUnknown(0);
        Assert.assertSame(elemType, resolved);
        JavaType unknown = type.containedTypeOrUnknown(1);
        Assert.assertNotNull(unknown);
        Assert.assertSame(TypeFactory.unknownType().getRawClass(), unknown.getRawClass());
    }

    @Test
    public void testSignatures() {
        DummyJavaType type = new DummyJavaType(String.class);
        String genSig = type.getGenericSignature();
        String erasedSig = type.getErasedSignature();

        Assert.assertEquals("Ljava/lang/String;", genSig);
        Assert.assertEquals("Ljava/lang/String;", erasedSig);
    }
}
