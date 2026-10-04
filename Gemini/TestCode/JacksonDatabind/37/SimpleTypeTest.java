package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTypeTest {

    @Test
    public void testConstruct() {
        SimpleType st = SimpleType.construct(String.class);
        Assert.assertNotNull(st);
        Assert.assertEquals(String.class, st.getRawClass());
        Assert.assertFalse(st.isContainerType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMapFails() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithCollectionFails() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithArrayFails() {
        SimpleType.construct(String[].class);
    }

    @Test
    public void testConstructUnsafe() {
        SimpleType st = SimpleType.constructUnsafe(Object.class);
        Assert.assertNotNull(st);
        Assert.assertEquals(Object.class, st.getRawClass());
    }

    @Test
    public void testNarrow() {
        SimpleType st = SimpleType.construct(Number.class);
        JavaType same = st._narrow(Number.class);
        Assert.assertSame(st, same);

        JavaType narrowed = st._narrow(Integer.class);
        Assert.assertNotSame(st, narrowed);
        Assert.assertEquals(Integer.class, narrowed.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeFails() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentType(SimpleType.construct(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandlerFails() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandlerFails() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentValueHandler("handler");
    }

    @Test
    public void testWithTypeHandler() {
        SimpleType st = SimpleType.construct(String.class);
        Assert.assertNull(st.getTypeHandler());

        SimpleType st2 = st.withTypeHandler("handler1");
        Assert.assertNotSame(st, st2);
        Assert.assertEquals("handler1", st2.getTypeHandler());

        SimpleType st3 = st2.withTypeHandler("handler1");
        Assert.assertSame(st2, st3);
    }

    @Test
    public void testWithValueHandler() {
        SimpleType st = SimpleType.construct(String.class);
        Assert.assertNull(st.getValueHandler());

        SimpleType st2 = st.withValueHandler("handler2");
        Assert.assertNotSame(st, st2);
        Assert.assertEquals("handler2", st2.getValueHandler());

        SimpleType st3 = st2.withValueHandler("handler2");
        Assert.assertSame(st2, st3);
    }

    @Test
    public void testWithStaticTyping() {
        SimpleType st = SimpleType.construct(String.class);
        Assert.assertFalse(st.useStaticType());

        SimpleType st2 = st.withStaticTyping();
        Assert.assertTrue(st2.useStaticType());

        SimpleType st3 = st2.withStaticTyping();
        Assert.assertSame(st2, st3);
    }

    @Test
    public void testRefine() {
        SimpleType st = SimpleType.construct(String.class);
        JavaType refined = st.refine(String.class, TypeBindings.emptyBindings(), null, null);
        Assert.assertNull(refined);
    }

    @Test
    public void testCanonicalNameAndSignaturesWithoutGenerics() {
        SimpleType st = SimpleType.construct(String.class);
        Assert.assertEquals("java.lang.String", st.toCanonical());
        Assert.assertEquals("[simple type, class java.lang.String]", st.toString());

        StringBuilder sbErased = new StringBuilder();
        st.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/lang/String;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        st.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/lang/String;;", sbGeneric.toString());
    }

    @Test
    public void testSignaturesWithGenerics() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[]{ SimpleType.construct(Integer.class), SimpleType.construct(Boolean.class) });
        SimpleType st = new SimpleType(String.class, bindings, null, null);

        Assert.assertEquals("java.lang.String<java.lang.Integer,java.lang.Boolean>", st.toCanonical());
        Assert.assertEquals("[simple type, class java.lang.String<java.lang.Integer,java.lang.Boolean>]", st.toString());

        StringBuilder sbGeneric = new StringBuilder();
        st.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/lang/String<Ljava/lang/Integer;;Ljava/lang/Boolean;;>;", sbGeneric.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        SimpleType st1 = SimpleType.construct(String.class);
        SimpleType st2 = SimpleType.construct(String.class);
        SimpleType st3 = SimpleType.construct(Integer.class);

        Assert.assertTrue(st1.equals(st1));
        Assert.assertFalse(st1.equals(null));
        Assert.assertFalse(st1.equals("not a type"));
        Assert.assertTrue(st1.equals(st2));
        Assert.assertTrue(st2.equals(st1));
        Assert.assertFalse(st1.equals(st3));

        TypeBindings bindings1 = TypeBindings.create(String.class, new JavaType[]{ SimpleType.construct(Integer.class) });
        TypeBindings bindings2 = TypeBindings.create(String.class, new JavaType[]{ SimpleType.construct(Boolean.class) });
        SimpleType stWithB1 = new SimpleType(String.class, bindings1, null, null);
        SimpleType stWithB2 = new SimpleType(String.class, bindings2, null, null);
        SimpleType stWithB1Clone = new SimpleType(String.class, bindings1, null, null);

        Assert.assertTrue(stWithB1.equals(stWithB1Clone));
        Assert.assertFalse(stWithB1.equals(stWithB2));
        Assert.assertFalse(st1.equals(stWithB1));
    }

    @Test
    public void testProtectedConstructors() {
        SimpleType base = SimpleType.construct(String.class);
        SimpleType copy = new SimpleType(base);
        Assert.assertEquals(base, copy);

        SimpleType custom = new SimpleType(String.class, TypeBindings.emptyBindings(), null, null, 123, "val", "type", true);
        Assert.assertEquals("val", custom.getValueHandler());
        Assert.assertEquals("type", custom.getTypeHandler());
        Assert.assertTrue(custom.useStaticType());
    }
}
