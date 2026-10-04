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
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref = SimpleType.constructUnsafe(String.class);
        type.setReference(ref);
        Assert.assertSame(ref, type.getSelfReferencedType());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetReferenceAlreadySetThrowsException() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref1 = SimpleType.constructUnsafe(String.class);
        JavaType ref2 = SimpleType.constructUnsafe(Integer.class);
        type.setReference(ref1);
        type.setReference(ref2);
    }

    @Test
    public void testSignaturesDelegation() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref = SimpleType.constructUnsafe(String.class);
        type.setReference(ref);

        StringBuilder sbGen = new StringBuilder();
        StringBuilder resGen = type.getGenericSignature(sbGen);
        Assert.assertSame(sbGen, resGen);
        Assert.assertEquals("Ljava/lang/String;", resGen.toString());

        StringBuilder sbErase = new StringBuilder();
        StringBuilder resErase = type.getErasedSignature(sbErase);
        Assert.assertSame(sbErase, resErase);
        Assert.assertEquals("Ljava/lang/String;", resErase.toString());
    }

    @Test
    public void testFluentModifierMethodsReturnThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType dummy = SimpleType.constructUnsafe(Integer.class);

        Assert.assertSame(type, type.withContentType(dummy));
        Assert.assertSame(type, type.withTypeHandler("handler"));
        Assert.assertSame(type, type.withContentTypeHandler("handler"));
        Assert.assertSame(type, type.withValueHandler("handler"));
        Assert.assertSame(type, type.withContentValueHandler("handler"));
        Assert.assertSame(type, type.withStaticTyping());
        Assert.assertSame(type, type._narrow(Object.class));
    }

    @Test
    public void testRefineReturnsNull() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType refined = type.refine(String.class, bindings, null, null);
        Assert.assertNull(refined);
    }

    @Test
    public void testIsContainerType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        Assert.assertFalse(type.isContainerType());
    }

    @Test
    public void testToStringUnresolvedAndResolved() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        Assert.assertEquals("[recursive type; UNRESOLVED", type.toString());

        JavaType ref = SimpleType.constructUnsafe(String.class);
        type.setReference(ref);
        Assert.assertEquals("[recursive type; java.lang.String", type.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings);
        JavaType refString = SimpleType.constructUnsafe(String.class);
        JavaType refInt = SimpleType.constructUnsafe(Integer.class);

        Assert.assertTrue(type1.equals(type1));
        Assert.assertFalse(type1.equals(null));
        Assert.assertFalse(type1.equals("a string"));

        Assert.assertFalse(type1.equals(type2));

        type1.setReference(refString);
        Assert.assertFalse(type1.equals(type2));

        type2.setReference(refInt);
        Assert.assertFalse(type1.equals(type2));

        ResolvedRecursiveType type3 = new ResolvedRecursiveType(String.class, bindings);
        type3.setReference(refString);
        Assert.assertTrue(type1.equals(type3));
    }
}
