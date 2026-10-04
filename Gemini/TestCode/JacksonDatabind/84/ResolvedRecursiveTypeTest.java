package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

public class ResolvedRecursiveTypeTest {

    @Test
    public void testConstructorAndGetSelfReferencedType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        Assert.assertEquals(String.class, type.getRawClass());
        Assert.assertNull(type.getSelfReferencedType());
    }

    @Test
    public void testSetReferenceSuccess() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, bindings);
        SimpleType refType = SimpleType.constructUnsafe(Object.class);
        type.setReference(refType);
        Assert.assertSame(refType, type.getSelfReferencedType());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetReferenceAlreadySet() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, bindings);
        SimpleType refType1 = SimpleType.constructUnsafe(Object.class);
        SimpleType refType2 = SimpleType.constructUnsafe(String.class);
        type.setReference(refType1);
        type.setReference(refType2);
    }

    @Test
    public void testSignaturesDelegation() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        SimpleType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);

        StringBuilder sbGeneric = new StringBuilder();
        StringBuilder resGeneric = type.getGenericSignature(sbGeneric);
        Assert.assertSame(sbGeneric, resGeneric);
        Assert.assertEquals("Ljava/lang/String;", sbGeneric.toString());

        StringBuilder sbErased = new StringBuilder();
        StringBuilder resErased = type.getErasedSignature(sbErased);
        Assert.assertSame(sbErased, resErased);
        Assert.assertEquals("Ljava/lang/String;", sbErased.toString());
    }

    @Test
    public void testWithMethodsReturnThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, bindings);
        SimpleType dummyType = SimpleType.constructUnsafe(String.class);

        Assert.assertSame(type, type.withContentType(dummyType));
        Assert.assertSame(type, type.withTypeHandler("handler"));
        Assert.assertSame(type, type.withContentTypeHandler("handler"));
        Assert.assertSame(type, type.withValueHandler("handler"));
        Assert.assertSame(type, type.withContentValueHandler("handler"));
        Assert.assertSame(type, type.withStaticTyping());
        Assert.assertSame(type, type._narrow(Object.class));
    }

    @Test
    public void testRefineAndIsContainerType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, bindings);

        Assert.assertNull(type.refine(Object.class, bindings, null, null));
        Assert.assertFalse(type.isContainerType());
    }

    @Test
    public void testToStringUnresolvedAndResolved() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        Assert.assertEquals("[recursive type; UNRESOLVED", type.toString());

        SimpleType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);
        Assert.assertEquals("[recursive type; java.lang.String", type.toString());
    }

    @Test
    public void testEquals() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings);

        Assert.assertTrue(type1.equals(type1));
        Assert.assertFalse(type1.equals(null));
        Assert.assertFalse(type1.equals("differentClass"));
        Assert.assertFalse(type1.equals(type2));

        SimpleType refType1 = SimpleType.constructUnsafe(String.class);
        SimpleType refType2 = SimpleType.constructUnsafe(String.class);
        SimpleType refType3 = SimpleType.constructUnsafe(Integer.class);

        type1.setReference(refType1);
        Assert.assertFalse(type1.equals(type2));

        type2.setReference(refType2);
        Assert.assertTrue(type1.equals(type2));

        ResolvedRecursiveType type3 = new ResolvedRecursiveType(Integer.class, bindings);
        type3.setReference(refType3);
        Assert.assertFalse(type1.equals(type3));
    }
}
