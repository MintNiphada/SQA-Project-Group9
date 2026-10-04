package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ReferenceTypeTest {

    @Test
    public void testUpgradeFromSuccess() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.upgradeFrom(baseType, refdType);

        Assert.assertNotNull(refType);
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());
        Assert.assertEquals(refdType, refType.getContentType());
        Assert.assertEquals(refdType, refType.getReferencedType());
        Assert.assertTrue(refType.hasContentType());
        Assert.assertTrue(refType.isReferenceType());
        Assert.assertTrue(refType.isAnchorType());
        Assert.assertSame(refType, refType.getAnchorType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFromNullRefdType() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType.upgradeFrom(baseType, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFromInvalidBaseType() {
        JavaType customType = new JavaType(AtomicReference.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;
            @Override public JavaType withContentType(JavaType contentType) { return this; }
            @Override public JavaType withTypeHandler(Object h) { return this; }
            @Override public JavaType withContentTypeHandler(Object h) { return this; }
            @Override public JavaType withValueHandler(Object h) { return this; }
            @Override public JavaType withContentValueHandler(Object h) { return this; }
            @Override public JavaType withStaticTyping() { return this; }
            @Override public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override protected String buildCanonicalName() { return ""; }
            @Override public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override public JavaType getContentType() { return null; }
            @Override public boolean isContainerType() { return false; }
            @Override public String toString() { return ""; }
            @Override public boolean equals(Object o) { return false; }
        };
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType.upgradeFrom(customType, refdType);
    }

    @Test
    public void testConstructors() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt1 = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertNotNull(rt1);
        Assert.assertEquals(AtomicReference.class, rt1.getRawClass());
        Assert.assertEquals(refdType, rt1.getContentType());
        Assert.assertTrue(rt1.isAnchorType());

        @SuppressWarnings("deprecation")
        ReferenceType rt2 = ReferenceType.construct(AtomicReference.class, refdType);
        Assert.assertNotNull(rt2);
        Assert.assertEquals(AtomicReference.class, rt2.getRawClass());
        Assert.assertNull(rt2.getContentType());
    }

    @Test
    public void testWithContentType() {
        JavaType refdType1 = SimpleType.constructUnsafe(String.class);
        JavaType refdType2 = SimpleType.constructUnsafe(Integer.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);

        ReferenceType rt = ReferenceType.upgradeFrom(baseType, refdType1);
        Assert.assertSame(rt, rt.withContentType(refdType1));

        JavaType rt2 = rt.withContentType(refdType2);
        Assert.assertNotSame(rt, rt2);
        Assert.assertEquals(refdType2, rt2.getContentType());
    }

    @Test
    public void testWithTypeHandler() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertSame(rt, rt.withTypeHandler(null));

        Object handler = "typeHandler";
        ReferenceType rt2 = rt.withTypeHandler(handler);
        Assert.assertNotSame(rt, rt2);
        Assert.assertSame(handler, rt2.getTypeHandler());
        Assert.assertSame(rt2, rt2.withTypeHandler(handler));
    }

    @Test
    public void testWithContentTypeHandler() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertSame(rt, rt.withContentTypeHandler(null));

        Object handler = "contentTypeHandler";
        ReferenceType rt2 = rt.withContentTypeHandler(handler);
        Assert.assertNotSame(rt, rt2);
        Assert.assertSame(handler, rt2.getContentType().getTypeHandler());
        Assert.assertSame(rt2, rt2.withContentTypeHandler(handler));
    }

    @Test
    public void testWithValueHandler() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertSame(rt, rt.withValueHandler(null));

        Object handler = "valHandler";
        ReferenceType rt2 = rt.withValueHandler(handler);
        Assert.assertNotSame(rt, rt2);
        Assert.assertSame(handler, rt2.getValueHandler());
        Assert.assertSame(rt2, rt2.withValueHandler(handler));
    }

    @Test
    public void testWithContentValueHandler() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertSame(rt, rt.withContentValueHandler(null));

        Object handler = "contentValHandler";
        ReferenceType rt2 = rt.withContentValueHandler(handler);
        Assert.assertNotSame(rt, rt2);
        Assert.assertSame(handler, rt2.getContentType().getValueHandler());
        Assert.assertSame(rt2, rt2.withContentValueHandler(handler));
    }

    @Test
    public void testWithStaticTyping() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, TypeBindings.emptyBindings(), null, null, refdType);
        Assert.assertFalse(rt.useStaticType());

        ReferenceType staticRt = rt.withStaticTyping();
        Assert.assertNotSame(rt, staticRt);
        Assert.assertTrue(staticRt.useStaticType());
        Assert.assertSame(staticRt, staticRt.withStaticTyping());
    }

    @Test
    public void testRefineAndNarrow() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType rt = ReferenceType.upgradeFrom(baseType, refdType);

        JavaType refined = rt.refine(AtomicReference.class, TypeBindings.emptyBindings(), null, new JavaType[0]);
        Assert.assertNotNull(refined);
        Assert.assertEquals(AtomicReference.class, refined.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType narrowed = rt._narrow(AtomicReference.class);
        Assert.assertNotNull(narrowed);
        Assert.assertEquals(AtomicReference.class, narrowed.getRawClass());
    }

    @Test
    public void testSignaturesAndCanonical() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType rt = ReferenceType.upgradeFrom(baseType, refdType);

        StringBuilder sbErased = new StringBuilder();
        rt.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        rt.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", sbGeneric.toString());

        String canonical = rt.toCanonical();
        Assert.assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String>", canonical);

        String str = rt.toString();
        Assert.assertTrue(str.startsWith("[reference type, class "));
        Assert.assertTrue(str.contains(canonical));
    }

    @Test
    public void testAnchorType() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType anchor = ReferenceType.upgradeFrom(baseType, refdType);

        ReferenceType child = new ReferenceType(AtomicReference.class, TypeBindings.emptyBindings(),
                null, null, refdType, anchor, null, null, false);

        Assert.assertTrue(anchor.isAnchorType());
        Assert.assertFalse(child.isAnchorType());
        Assert.assertSame(anchor, child.getAnchorType());
    }

    @Test
    public void testEquals() {
        JavaType refdStr = SimpleType.constructUnsafe(String.class);
        JavaType refdInt = SimpleType.constructUnsafe(Integer.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);

        ReferenceType rt1 = ReferenceType.upgradeFrom(baseType, refdStr);
        ReferenceType rt2 = ReferenceType.upgradeFrom(baseType, refdStr);
        ReferenceType rt3 = ReferenceType.upgradeFrom(baseType, refdInt);
        ReferenceType rt4 = ReferenceType.construct(Object.class, TypeBindings.emptyBindings(), null, null, refdStr);

        Assert.assertTrue(rt1.equals(rt1));
        Assert.assertTrue(rt1.equals(rt2));
        Assert.assertFalse(rt1.equals(null));
        Assert.assertFalse(rt1.equals("some string"));
        Assert.assertFalse(rt1.equals(rt3));
        Assert.assertFalse(rt1.equals(rt4));
    }
}
