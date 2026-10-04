package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class JavaTypeTest {

    private static class DummyType extends JavaType {
        private static final long serialVersionUID = 1L;
        private final JavaType _contained;

        protected DummyType(Class<?> raw) {
            super(raw, 0, null, null, false);
            _contained = null;
        }

        protected DummyType(Class<?> raw, int hash, Object valH, Object typeH, boolean asStatic) {
            super(raw, hash, valH, typeH, asStatic);
            _contained = null;
        }

        protected DummyType(Class<?> raw, JavaType contained) {
            super(raw, 0, null, null, false);
            _contained = contained;
        }

        protected DummyType(DummyType base) {
            super(base);
            _contained = base._contained;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new DummyType(_class, _hash - _class.getName().hashCode(), _valueHandler, h, _asStatic);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new DummyType(_class, _hash - _class.getName().hashCode(), h, _typeHandler, _asStatic);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return new DummyType(_class, contentType);
        }

        @Override
        public JavaType withStaticTyping() {
            return new DummyType(_class, _hash - _class.getName().hashCode(), _valueHandler, _typeHandler, true);
        }

        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
            return this;
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new DummyType(subclass);
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public int containedTypeCount() {
            return _contained == null ? 0 : 1;
        }

        @Override
        public JavaType containedType(int index) {
            return (index == 0) ? _contained : null;
        }

        @Override
        public String containedTypeName(int index) {
            return null;
        }

        @Override
        public TypeBindings getBindings() {
            return TypeBindings.emptyBindings();
        }

        @Override
        public JavaType findSuperType(Class<?> erasedTarget) {
            return null;
        }

        @Override
        public JavaType getSuperClass() {
            return null;
        }

        @Override
        public List<JavaType> getInterfaces() {
            return Collections.emptyList();
        }

        @Override
        public JavaType[] findTypeParameters(Class<?> expType) {
            return new JavaType[0];
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            return sb.append(_class.getName());
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            return sb.append(_class.getName());
        }

        @Override
        public String toString() {
            return "[DummyType " + _class.getName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            DummyType other = (DummyType) o;
            return other._class == _class;
        }
    }

    private enum TestEnum { A, B }
    private interface TestInterface {}
    private static abstract class AbstractClass {}
    private static class ConcreteClass extends AbstractClass {}

    @Test
    public void testBaseConstructorsAndAccessors() {
        DummyType t1 = new DummyType(String.class, 123, "valH", "typeH", true);
        Assert.assertEquals(String.class, t1.getRawClass());
        Assert.assertTrue(t1.hasRawClass(String.class));
        Assert.assertFalse(t1.hasRawClass(Integer.class));
        Assert.assertEquals("valH", t1.getValueHandler());
        Assert.assertEquals("typeH", t1.getTypeHandler());
        Assert.assertTrue(t1.useStaticType());
        Assert.assertTrue(t1.hasValueHandler());
        Assert.assertTrue(t1.hasHandlers());
        Assert.assertEquals(String.class.getName().hashCode() + 123, t1.hashCode());

        DummyType copy = new DummyType(t1);
        Assert.assertEquals(t1.getRawClass(), copy.getRawClass());
        Assert.assertEquals(t1.getValueHandler(), copy.getValueHandler());
        Assert.assertEquals(t1.getTypeHandler(), copy.getTypeHandler());
        Assert.assertEquals(t1.useStaticType(), copy.useStaticType());
        Assert.assertEquals(t1.hashCode(), copy.hashCode());
    }

    @Test
    public void testTypeClassifications() {
        DummyType stringType = new DummyType(String.class);
        Assert.assertTrue(stringType.isConcrete());
        Assert.assertFalse(stringType.isAbstract());
        Assert.assertFalse(stringType.isThrowable());
        Assert.assertFalse(stringType.isArrayType());
        Assert.assertFalse(stringType.isEnumType());
        Assert.assertFalse(stringType.isInterface());
        Assert.assertFalse(stringType.isPrimitive());
        Assert.assertTrue(stringType.isFinal());
        Assert.assertFalse(stringType.isJavaLangObject());

        DummyType intType = new DummyType(int.class);
        Assert.assertTrue(intType.isPrimitive());
        Assert.assertTrue(intType.isConcrete());
        Assert.assertFalse(intType.isAbstract());

        DummyType objType = new DummyType(Object.class);
        Assert.assertTrue(objType.isJavaLangObject());

        DummyType exType = new DummyType(Exception.class);
        Assert.assertTrue(exType.isThrowable());

        DummyType enumType = new DummyType(TestEnum.class);
        Assert.assertTrue(enumType.isEnumType());

        DummyType ifaceType = new DummyType(TestInterface.class);
        Assert.assertTrue(ifaceType.isInterface());
        Assert.assertTrue(ifaceType.isAbstract());
        Assert.assertFalse(ifaceType.isConcrete());

        DummyType absType = new DummyType(AbstractClass.class);
        Assert.assertTrue(absType.isAbstract());
        Assert.assertFalse(absType.isConcrete());
    }

    @Test
    public void testIsTypeOrSubTypeOf() {
        DummyType sub = new DummyType(ConcreteClass.class);
        Assert.assertTrue(sub.isTypeOrSubTypeOf(ConcreteClass.class));
        Assert.assertTrue(sub.isTypeOrSubTypeOf(AbstractClass.class));
        Assert.assertTrue(sub.isTypeOrSubTypeOf(Object.class));
        Assert.assertFalse(sub.isTypeOrSubTypeOf(String.class));
    }

    @Test
    public void testDefaultMethodImplementations() {
        DummyType t = new DummyType(String.class);
        Assert.assertTrue(t.hasContentType());
        Assert.assertFalse(t.isCollectionLikeType());
        Assert.assertFalse(t.isMapLikeType());
        Assert.assertNull(t.getKeyType());
        Assert.assertNull(t.getContentType());
        Assert.assertNull(t.getReferencedType());
        Assert.assertNull(t.getContentValueHandler());
        Assert.assertNull(t.getContentTypeHandler());
        Assert.assertNull(t.getParameterSource());
        Assert.assertFalse(t.hasHandlers());
        Assert.assertFalse(t.hasGenericTypes());
    }

    @Test
    public void testGenericAndErasedSignatures() {
        DummyType t = new DummyType(String.class);
        Assert.assertEquals("java.lang.String", t.getGenericSignature());
        Assert.assertEquals("java.lang.String", t.getErasedSignature());
    }

    @Test
    public void testContainedTypes() {
        DummyType inner = new DummyType(Long.class);
        DummyType outer = new DummyType(List.class, inner);
        Assert.assertTrue(outer.hasGenericTypes());
        Assert.assertEquals(1, outer.containedTypeCount());
        Assert.assertSame(inner, outer.containedType(0));
        Assert.assertNull(outer.containedType(1));
        Assert.assertSame(inner, outer.containedTypeOrUnknown(0));
        
        JavaType unknown = outer.containedTypeOrUnknown(1);
        Assert.assertNotNull(unknown);
        Assert.assertEquals(TypeFactory.unknownType(), unknown);
    }

    @Test
    public void testForcedNarrowBy() {
        DummyType parent = new DummyType(Number.class, 0, "valH", "typeH", false);
        JavaType same = parent.forcedNarrowBy(Number.class);
        Assert.assertSame(parent, same);

        JavaType narrowed = parent.forcedNarrowBy(Integer.class);
        Assert.assertEquals(Integer.class, narrowed.getRawClass());
        Assert.assertEquals("valH", narrowed.getValueHandler());
        Assert.assertEquals("typeH", narrowed.getTypeHandler());
    }

    @Test
    public void testHandlersCheck() {
        DummyType tNoH = new DummyType(String.class, 0, null, null, false);
        Assert.assertFalse(tNoH.hasHandlers());
        Assert.assertFalse(tNoH.hasValueHandler());

        DummyType tValH = new DummyType(String.class, 0, "h", null, false);
        Assert.assertTrue(tValH.hasHandlers());
        Assert.assertTrue(tValH.hasValueHandler());

        DummyType tTypeH = new DummyType(String.class, 0, null, "h", false);
        Assert.assertTrue(tTypeH.hasHandlers());
        Assert.assertFalse(tTypeH.hasValueHandler());
    }
}
