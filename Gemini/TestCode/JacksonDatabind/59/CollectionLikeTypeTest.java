package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CollectionLikeTypeTest {

    @Test
    public void testConstructAndGetters() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        TypeBindings bindings = TypeBindings.create(ArrayList.class, elemT);
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInts = new JavaType[]{SimpleType.constructUnsafe(Cloneable.class)};

        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, bindings, superClass, superInts, elemT);

        Assert.assertNotNull(type);
        Assert.assertEquals(ArrayList.class, type.getRawClass());
        Assert.assertEquals(elemT, type.getContentType());
        Assert.assertTrue(type.isContainerType());
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertTrue(type.isTrueCollectionType());
        Assert.assertNull(type.getContentValueHandler());
        Assert.assertNull(type.getContentTypeHandler());
        Assert.assertFalse(type.hasHandlers());
    }

    @Test
    public void testDeprecatedConstructSingleTypeParam() {
        JavaType elemT = SimpleType.constructUnsafe(Integer.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemT);

        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(elemT, type.getContentType());
        Assert.assertEquals(1, type.getBindings().size());
        Assert.assertEquals(elemT, type.getBindings().getBoundType(0));
    }

    @Test
    public void testDeprecatedConstructNoTypeParams() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(String.class, elemT);

        Assert.assertEquals(String.class, type.getRawClass());
        Assert.assertEquals(elemT, type.getContentType());
        Assert.assertTrue(type.getBindings().isEmpty());
        Assert.assertFalse(type.isTrueCollectionType());
    }

    @Test
    public void testUpgradeFromSuccess() {
        JavaType baseType = SimpleType.constructUnsafe(ArrayList.class);
        JavaType elemT = SimpleType.constructUnsafe(Long.class);
        CollectionLikeType upgraded = CollectionLikeType.upgradeFrom(baseType, elemT);

        Assert.assertEquals(ArrayList.class, upgraded.getRawClass());
        Assert.assertEquals(elemT, upgraded.getContentType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFromFailure() {
        JavaType customNonBaseType = new JavaType(String.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;
            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withStaticTyping() { return this; }
            @Override
            public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override
            public boolean isContainerType() { return false; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override
            public String toString() { return ""; }
            @Override
            public boolean equals(Object o) { return false; }
        };

        CollectionLikeType.upgradeFrom(customNonBaseType, SimpleType.constructUnsafe(String.class));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testNarrow() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemT);
        JavaType narrowed = type._narrow(LinkedList.class);

        Assert.assertEquals(LinkedList.class, narrowed.getRawClass());
        Assert.assertEquals(elemT, narrowed.getContentType());
    }

    @Test
    public void testWithContentType() {
        JavaType elemT1 = SimpleType.constructUnsafe(String.class);
        JavaType elemT2 = SimpleType.constructUnsafe(Integer.class);
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, elemT1);

        JavaType sameType = type.withContentType(elemT1);
        Assert.assertSame(type, sameType);

        JavaType diffType = type.withContentType(elemT2);
        Assert.assertNotSame(type, diffType);
        Assert.assertEquals(elemT2, diffType.getContentType());
    }

    @Test
    public void testWithHandlers() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, elemT);

        Object valHandler = "valueHandler";
        Object typeHandler = "typeHandler";
        Object contentValHandler = "contentValueHandler";
        Object contentTypeHandler = "contentTypeHandler";

        CollectionLikeType withValH = type.withValueHandler(valHandler);
        Assert.assertEquals(valHandler, withValH.getValueHandler());
        Assert.assertTrue(withValH.hasHandlers());

        CollectionLikeType withTypeH = type.withTypeHandler(typeHandler);
        Assert.assertEquals(typeHandler, withTypeH.getTypeHandler());
        Assert.assertTrue(withTypeH.hasHandlers());

        CollectionLikeType withContValH = type.withContentValueHandler(contentValHandler);
        Assert.assertEquals(contentValHandler, withContValH.getContentValueHandler());
        Assert.assertTrue(withContValH.hasHandlers());

        CollectionLikeType withContTypeH = type.withContentTypeHandler(contentTypeHandler);
        Assert.assertEquals(contentTypeHandler, withContTypeH.getContentTypeHandler());
        Assert.assertTrue(withContTypeH.hasHandlers());
    }

    @Test
    public void testWithStaticTyping() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, elemT);

        Assert.assertFalse(type.useStaticType());

        CollectionLikeType staticType = type.withStaticTyping();
        Assert.assertTrue(staticType.useStaticType());
        Assert.assertTrue(staticType.getContentType().useStaticType());

        CollectionLikeType staticType2 = staticType.withStaticTyping();
        Assert.assertSame(staticType, staticType2);
    }

    @Test
    public void testRefine() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(Collection.class, elemT);

        TypeBindings newBindings = TypeBindings.create(ArrayList.class, elemT);
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[]{SimpleType.constructUnsafe(List.class)};

        JavaType refined = type.refine(ArrayList.class, newBindings, superClass, superInterfaces);
        Assert.assertEquals(ArrayList.class, refined.getRawClass());
        Assert.assertEquals(superClass, refined.getSuperClass());
        Assert.assertEquals(1, refined.getInterfaces().length);
    }

    @Test
    public void testSignaturesAndCanonical() {
        JavaType elemT = SimpleType.constructUnsafe(String.class);
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, elemT);

        StringBuilder sbErased = new StringBuilder();
        type.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/util/ArrayList;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        type.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/util/ArrayList<Ljava/lang/String;>;", sbGeneric.toString());

        String canonical = type.toCanonical();
        Assert.assertEquals("java.util.ArrayList<java.lang.String>", canonical);
    }

    @Test
    public void testEqualsAndToString() {
        JavaType elemT1 = SimpleType.constructUnsafe(String.class);
        JavaType elemT2 = SimpleType.constructUnsafe(Integer.class);

        CollectionLikeType type1 = CollectionLikeType.construct(ArrayList.class, elemT1);
        CollectionLikeType type2 = CollectionLikeType.construct(ArrayList.class, elemT1);
        CollectionLikeType typeDiffElem = CollectionLikeType.construct(ArrayList.class, elemT2);
        CollectionLikeType typeDiffClass = CollectionLikeType.construct(LinkedList.class, elemT1);

        Assert.assertTrue(type1.equals(type1));
        Assert.assertTrue(type1.equals(type2));
        Assert.assertTrue(type2.equals(type1));

        Assert.assertFalse(type1.equals(null));
        Assert.assertFalse(type1.equals("string"));
        Assert.assertFalse(type1.equals(typeDiffElem));
        Assert.assertFalse(type1.equals(typeDiffClass));

        String str = type1.toString();
        Assert.assertTrue(str.contains("collection-like type"));
        Assert.assertTrue(str.contains("java.util.ArrayList"));
        Assert.assertTrue(str.contains("java.lang.String"));
    }
}
