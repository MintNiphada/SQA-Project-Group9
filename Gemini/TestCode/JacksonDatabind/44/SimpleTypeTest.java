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
        SimpleType type = SimpleType.construct(String.class);
        Assert.assertEquals(String.class, type.getRawClass());
        Assert.assertFalse(type.isContainerType());
        Assert.assertNull(type.getValueHandler());
        Assert.assertNull(type.getTypeHandler());
        Assert.assertFalse(type.useStaticType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructMapFails() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructCollectionFails() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructArrayFails() {
        SimpleType.construct(String[].class);
    }

    @Test
    public void testConstructUnsafe() {
        SimpleType type = SimpleType.constructUnsafe(Integer.class);
        Assert.assertEquals(Integer.class, type.getRawClass());
    }

    @Test
    public void testProtectedConstructors() {
        SimpleType t1 = new SimpleType(Long.class);
        Assert.assertEquals(Long.class, t1.getRawClass());

        SimpleType t2 = new SimpleType(Long.class, TypeBindings.emptyBindings(), null, null);
        Assert.assertEquals(Long.class, t2.getRawClass());

        SimpleType t3 = new SimpleType(t1);
        Assert.assertEquals(Long.class, t3.getRawClass());

        SimpleType t4 = new SimpleType(Long.class, TypeBindings.emptyBindings(), null, null, 123, "vh", "th", true);
        Assert.assertEquals("vh", t4.getValueHandler());
        Assert.assertEquals("th", t4.getTypeHandler());
        Assert.assertTrue(t4.useStaticType());
    }

    @Test
    public void testNarrow() {
        SimpleType parent = SimpleType.construct(Number.class);
        JavaType narrowed = parent._narrow(Integer.class);
        Assert.assertEquals(Integer.class, narrowed.getRawClass());

        JavaType same = parent._narrow(Number.class);
        Assert.assertSame(parent, same);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeFails() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentType(SimpleType.construct(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandlerFails() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandlerFails() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentValueHandler("handler");
    }

    @Test
    public void testWithTypeHandler() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType withH = type.withTypeHandler("handler");
        Assert.assertEquals("handler", withH.getTypeHandler());

        SimpleType sameH = withH.withTypeHandler("handler");
        Assert.assertSame(withH, sameH);
    }

    @Test
    public void testWithValueHandler() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType withH = type.withValueHandler("handler");
        Assert.assertEquals("handler", withH.getValueHandler());

        SimpleType sameH = withH.withValueHandler("handler");
        Assert.assertSame(withH, sameH);
    }

    @Test
    public void testWithStaticTyping() {
        SimpleType type = SimpleType.construct(String.class);
        Assert.assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        Assert.assertTrue(staticType.useStaticType());

        SimpleType sameStatic = staticType.withStaticTyping();
        Assert.assertSame(staticType, sameStatic);
    }

    @Test
    public void testRefine() {
        SimpleType type = SimpleType.construct(String.class);
        Assert.assertNull(type.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    @Test
    public void testSignaturesAndCanonical() {
        TypeBindings bindings = TypeBindings.create(List.class, new JavaType[]{SimpleType.construct(String.class)});
        SimpleType boundType = new SimpleType(List.class, bindings, null, null, null, null, false);

        String canonical = boundType.toCanonical();
        Assert.assertEquals("java.util.List<java.lang.String>", canonical);

        StringBuilder sb = new StringBuilder();
        boundType.getErasedSignature(sb);
        Assert.assertEquals("Ljava/util/List;", sb.toString());

        StringBuilder sbGen = new StringBuilder();
        boundType.getGenericSignature(sbGen);
        Assert.assertEquals("Ljava/util/List<Ljava/lang/String;>;", sbGen.toString());
    }

    @Test
    public void testToString() {
        SimpleType type = SimpleType.construct(String.class);
        Assert.assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        SimpleType t1 = SimpleType.construct(String.class);
        SimpleType t2 = SimpleType.construct(String.class);
        SimpleType t3 = SimpleType.construct(Integer.class);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals("string"));

        TypeBindings b1 = TypeBindings.create(List.class, new JavaType[]{SimpleType.construct(String.class)});
        TypeBindings b2 = TypeBindings.create(List.class, new JavaType[]{SimpleType.construct(Integer.class)});
        SimpleType bound1 = new SimpleType(List.class, b1, null, null);
        SimpleType bound2 = new SimpleType(List.class, b2, null, null);
        Assert.assertFalse(bound1.equals(bound2));
    }

    @Test
    public void testBuildSuperClassHierarchy() {
        SimpleType custom = SimpleType.construct(SubTestClass.class);
        Assert.assertNotNull(custom.getSuperClass());
        Assert.assertEquals(BaseTestClass.class, custom.getSuperClass().getRawClass());
    }

    static class BaseTestClass {}
    static class SubTestClass extends BaseTestClass {}
}
