package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTypeTest {

    private static class SubSimpleType extends SimpleType {
        private static final long serialVersionUID = 1L;

        public SubSimpleType(Class<?> cls) {
            super(cls);
        }

        public SubSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts) {
            super(cls, bindings, superClass, superInts);
        }

        public SubSimpleType(TypeBase base) {
            super(base);
        }

        public SubSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts,
                             Object valueHandler, Object typeHandler, boolean asStatic) {
            super(cls, bindings, superClass, superInts, valueHandler, typeHandler, asStatic);
        }

        public SubSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts,
                             int extraHash, Object valueHandler, Object typeHandler, boolean asStatic) {
            super(cls, bindings, superClass, superInts, extraHash, valueHandler, typeHandler, asStatic);
        }

        public JavaType narrow(Class<?> subclass) {
            return _narrow(subclass);
        }
    }

    @Test
    public void testConstruct() {
        SimpleType type = SimpleType.construct(String.class);
        Assert.assertNotNull(type);
        Assert.assertEquals(String.class, type.getRawClass());
        Assert.assertFalse(type.isContainerType());
        Assert.assertNotNull(type.getSuperClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructMapException() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructCollectionException() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructArrayException() {
        SimpleType.construct(String[].class);
    }

    @Test
    public void testConstructUnsafe() {
        SimpleType type = SimpleType.constructUnsafe(Object.class);
        Assert.assertNotNull(type);
        Assert.assertEquals(Object.class, type.getRawClass());
        Assert.assertNull(type.getSuperClass());
    }

    @Test
    public void testSubclassConstructors() {
        SubSimpleType st1 = new SubSimpleType(String.class);
        Assert.assertEquals(String.class, st1.getRawClass());

        SubSimpleType st2 = new SubSimpleType(String.class, TypeBindings.emptyBindings(), null, null);
        Assert.assertEquals(String.class, st2.getRawClass());

        SubSimpleType st3 = new SubSimpleType(st1);
        Assert.assertEquals(String.class, st3.getRawClass());

        SubSimpleType st4 = new SubSimpleType(String.class, TypeBindings.emptyBindings(), null, null, "vh", "th", true);
        Assert.assertEquals("vh", st4.getValueHandler());
        Assert.assertEquals("th", st4.getTypeHandler());
        Assert.assertTrue(st4.useStaticType());

        SubSimpleType st5 = new SubSimpleType(String.class, TypeBindings.emptyBindings(), null, null, 123, "vh", "th", false);
        Assert.assertEquals("vh", st5.getValueHandler());
        Assert.assertEquals("th", st5.getTypeHandler());
        Assert.assertFalse(st5.useStaticType());
    }

    @Test
    public void testNarrow() {
        SubSimpleType parent = new SubSimpleType(CharSequence.class);
        JavaType same = parent.narrow(CharSequence.class);
        Assert.assertSame(parent, same);

        JavaType narrowed = parent.narrow(String.class);
        Assert.assertNotSame(parent, narrowed);
        Assert.assertEquals(String.class, narrowed.getRawClass());
        Assert.assertSame(parent, narrowed.getSuperClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentType(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentValueHandler("handler");
    }

    @Test
    public void testWithTypeHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Assert.assertNull(type.getTypeHandler());

        SimpleType typeWithHandler = type.withTypeHandler("handler1");
        Assert.assertEquals("handler1", typeWithHandler.getTypeHandler());
        Assert.assertNotSame(type, typeWithHandler);

        SimpleType sameHandler = typeWithHandler.withTypeHandler("handler1");
        Assert.assertSame(typeWithHandler, sameHandler);
    }

    @Test
    public void testWithValueHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Assert.assertNull(type.getValueHandler());

        SimpleType typeWithHandler = type.withValueHandler("valHandler1");
        Assert.assertEquals("valHandler1", typeWithHandler.getValueHandler());
        Assert.assertNotSame(type, typeWithHandler);

        SimpleType sameHandler = typeWithHandler.withValueHandler("valHandler1");
        Assert.assertSame(typeWithHandler, sameHandler);
    }

    @Test
    public void testWithStaticTyping() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Assert.assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        Assert.assertTrue(staticType.useStaticType());
        Assert.assertNotSame(type, staticType);

        SimpleType sameStatic = staticType.withStaticTyping();
        Assert.assertSame(staticType, sameStatic);
    }

    @Test
    public void testRefine() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        Assert.assertNull(refined);
    }

    @Test
    public void testSignaturesAndCanonicalWithoutBindings() {
        SimpleType type = SimpleType.constructUnsafe(String.class);

        StringBuilder sbErased = new StringBuilder();
        type.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/lang/String;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        type.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/lang/String;;", sbGeneric.toString());

        Assert.assertEquals("java.lang.String", type.toCanonical());
        Assert.assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    @Test
    public void testSignaturesAndCanonicalWithBindings() {
        SimpleType paramType1 = SimpleType.constructUnsafe(String.class);
        SimpleType paramType2 = SimpleType.constructUnsafe(Integer.class);
        TypeBindings bindings = TypeBindings.create(List.class, new JavaType[]{paramType1, paramType2});

        SimpleType type = new SimpleType(List.class, bindings, null, null, null, null, false);

        StringBuilder sbGeneric = new StringBuilder();
        type.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/util/List<Ljava/lang/String;;Ljava/lang/Integer;;>;", sbGeneric.toString());

        Assert.assertEquals("java.util.List<java.lang.String,java.lang.Integer>", type.toCanonical());
        Assert.assertEquals("[simple type, class java.util.List<java.lang.String,java.lang.Integer>]", type.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        SimpleType st1 = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = SimpleType.constructUnsafe(String.class);
        SimpleType st3 = SimpleType.constructUnsafe(Integer.class);

        Assert.assertTrue(st1.equals(st1));
        Assert.assertFalse(st1.equals(null));
        Assert.assertFalse(st1.equals("some string"));
        Assert.assertTrue(st1.equals(st2));
        Assert.assertTrue(st2.equals(st1));
        Assert.assertFalse(st1.equals(st3));

        TypeBindings bindings1 = TypeBindings.create(List.class, new JavaType[]{st1});
        TypeBindings bindings2 = TypeBindings.create(List.class, new JavaType[]{st3});
        SimpleType bound1 = new SimpleType(List.class, bindings1, null, null);
        SimpleType bound1Same = new SimpleType(List.class, bindings1, null, null);
        SimpleType bound2 = new SimpleType(List.class, bindings2, null, null);

        Assert.assertTrue(bound1.equals(bound1Same));
        Assert.assertFalse(bound1.equals(bound2));
    }
}
