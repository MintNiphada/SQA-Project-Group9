package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ReferenceTypeTest {

    @Test
    public void testConstructAndGetters() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Assert.assertNotNull(rt);
        Assert.assertEquals(AtomicReference.class, rt.getRawClass());
        Assert.assertSame(refType, rt.getReferencedType());
        Assert.assertTrue(rt.isReferenceType());
        Assert.assertEquals(1, rt.containedTypeCount());
        Assert.assertSame(refType, rt.containedType(0));
        Assert.assertNull(rt.containedType(1));
        Assert.assertNull(rt.containedType(-1));
        Assert.assertEquals("T", rt.containedTypeName(0));
        Assert.assertNull(rt.containedTypeName(1));
        Assert.assertNull(rt.containedTypeName(-1));
        Assert.assertEquals(AtomicReference.class, rt.getParameterSource());
    }

    @Test
    public void testWithTypeHandler() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Object handler = "typeHandler";
        ReferenceType modified = rt.withTypeHandler(handler);

        Assert.assertNotSame(rt, modified);
        Assert.assertSame(handler, modified.getTypeHandler());
        Assert.assertSame(modified, modified.withTypeHandler(handler));
    }

    @Test
    public void testWithValueHandler() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Object handler = "valueHandler";
        ReferenceType modified = rt.withValueHandler(handler);

        Assert.assertNotSame(rt, modified);
        Assert.assertSame(handler, modified.getValueHandler());
        Assert.assertSame(modified, modified.withValueHandler(handler));
    }

    @Test
    public void testWithContentTypeHandler() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Object handler = "contentTypeHandler";
        ReferenceType modified = rt.withContentTypeHandler(handler);

        Assert.assertNotSame(rt, modified);
        Assert.assertSame(handler, modified.getReferencedType().getTypeHandler());
        Assert.assertSame(modified, modified.withContentTypeHandler(handler));
    }

    @Test
    public void testWithContentValueHandler() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Object handler = "contentValueHandler";
        ReferenceType modified = rt.withContentValueHandler(handler);

        Assert.assertNotSame(rt, modified);
        Assert.assertSame(handler, modified.getReferencedType().getValueHandler());
        Assert.assertSame(modified, modified.withContentValueHandler(handler));
    }

    @Test
    public void testWithStaticTyping() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        Assert.assertFalse(rt.useStaticType());
        ReferenceType staticRt = rt.withStaticTyping();

        Assert.assertNotSame(rt, staticRt);
        Assert.assertTrue(staticRt.useStaticType());
        Assert.assertSame(staticRt, staticRt.withStaticTyping());
    }

    @Test
    public void testNarrow() {
        JavaType refType = SimpleType.constructUnsafe(Object.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        JavaType narrowed = rt._narrow(AtomicReference.class);
        Assert.assertNotNull(narrowed);
        Assert.assertTrue(narrowed instanceof ReferenceType);
        Assert.assertEquals(AtomicReference.class, narrowed.getRawClass());
        Assert.assertSame(refType, ((ReferenceType) narrowed).getReferencedType());
    }

    @Test
    public void testCanonicalNameAndToString() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        String canonical = rt.toCanonical();
        Assert.assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String>", canonical);

        String str = rt.toString();
        Assert.assertTrue(str.startsWith("[reference type, class " + canonical + "<[simple type, class java.lang.String]>]"));
    }

    @Test
    public void testSignatures() {
        JavaType refType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, refType, null, null);

        StringBuilder sbErased = new StringBuilder();
        rt.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        rt.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;;", sbGeneric.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        JavaType refType1 = SimpleType.constructUnsafe(String.class);
        JavaType refType2 = SimpleType.constructUnsafe(Integer.class);

        ReferenceType rt1 = ReferenceType.construct(AtomicReference.class, refType1, null, null);
        ReferenceType rt2 = ReferenceType.construct(AtomicReference.class, refType1, null, null);
        ReferenceType rt3 = ReferenceType.construct(AtomicReference.class, refType2, null, null);
        ReferenceType rt4 = ReferenceType.construct(Object.class, refType1, null, null);

        Assert.assertTrue(rt1.equals(rt1));
        Assert.assertTrue(rt1.equals(rt2));
        Assert.assertTrue(rt2.equals(rt1));
        Assert.assertFalse(rt1.equals(rt3));
        Assert.assertFalse(rt1.equals(rt4));
        Assert.assertFalse(rt1.equals(null));
        Assert.assertFalse(rt1.equals("some string"));
        Assert.assertEquals(rt1.hashCode(), rt2.hashCode());
    }
}
