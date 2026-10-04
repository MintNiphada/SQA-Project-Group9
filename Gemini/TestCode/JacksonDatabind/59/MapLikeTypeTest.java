package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MapLikeTypeTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();

    @Test
    public void testConstructAndGetters() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType mapType = MapLikeType.construct(Map.class, keyT, valT);

        Assert.assertEquals(Map.class, mapType.getRawClass());
        Assert.assertEquals(keyT, mapType.getKeyType());
        Assert.assertEquals(valT, mapType.getContentType());
        Assert.assertTrue(mapType.isContainerType());
        Assert.assertTrue(mapType.isMapLikeType());
        Assert.assertTrue(mapType.isTrueMapType());
        Assert.assertNull(mapType.getContentValueHandler());
        Assert.assertNull(mapType.getContentTypeHandler());
        Assert.assertFalse(mapType.hasHandlers());
    }

    @Test
    public void testConstructWithNonMapClass() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Long.class);
        MapLikeType customType = MapLikeType.construct(String.class, keyT, valT);

        Assert.assertFalse(customType.isTrueMapType());
        Assert.assertEquals(String.class, customType.getRawClass());
    }

    @Test
    public void testUpgradeFrom() {
        JavaType baseType = _typeFactory.constructType(HashMap.class);
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Object.class);

        MapLikeType upgraded = MapLikeType.upgradeFrom(baseType, keyT, valT);
        Assert.assertEquals(HashMap.class, upgraded.getRawClass());
        Assert.assertEquals(keyT, upgraded.getKeyType());
        Assert.assertEquals(valT, upgraded.getContentType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFromInvalid() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Object.class);
        JavaType fakeBase = new SimpleType(String.class) {
            private static final long serialVersionUID = 1L;
        };
        JavaType notTypeBase = new JavaType(fakeBase) {
            private static final long serialVersionUID = 1L;
            @Override public JavaType withContentType(JavaType ct) { return this; }
            @Override public JavaType withTypeHandler(Object h) { return this; }
            @Override public JavaType withContentTypeHandler(Object h) { return this; }
            @Override public JavaType withValueHandler(Object h) { return this; }
            @Override public JavaType withContentValueHandler(Object h) { return this; }
            @Override public JavaType withStaticTyping() { return this; }
            @Override public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override public boolean isContainerType() { return false; }
            @Override public String toString() { return ""; }
            @Override public boolean equals(Object o) { return false; }
            @Override public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
        };

        MapLikeType.upgradeFrom(notTypeBase, keyT, valT);
    }

    @Test
    public void testWithKeyType() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        MapLikeType same = type.withKeyType(keyT);
        Assert.assertSame(type, same);

        JavaType newKeyT = _typeFactory.constructType(Long.class);
        MapLikeType changed = type.withKeyType(newKeyT);
        Assert.assertNotSame(type, changed);
        Assert.assertEquals(newKeyT, changed.getKeyType());
    }

    @Test
    public void testWithContentType() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        JavaType same = type.withContentType(valT);
        Assert.assertSame(type, same);

        JavaType newValT = _typeFactory.constructType(Double.class);
        JavaType changed = type.withContentType(newValT);
        Assert.assertNotSame(type, changed);
        Assert.assertEquals(newValT, changed.getContentType());
    }

    @Test
    public void testWithHandlers() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        Object typeH = "typeHandler";
        Object valH = "valHandler";
        Object keyTypeH = "keyTypeHandler";
        Object keyValH = "keyValHandler";
        Object contentTypeH = "contentTypeHandler";
        Object contentValH = "contentValHandler";

        MapLikeType t1 = type.withTypeHandler(typeH);
        Assert.assertEquals(typeH, t1.getTypeHandler());
        Assert.assertTrue(t1.hasHandlers());

        MapLikeType t2 = type.withValueHandler(valH);
        Assert.assertEquals(valH, t2.getValueHandler());
        Assert.assertTrue(t2.hasHandlers());

        MapLikeType t3 = type.withKeyTypeHandler(keyTypeH);
        Assert.assertEquals(keyTypeH, t3.getKeyType().getTypeHandler());
        Assert.assertTrue(t3.hasHandlers());

        MapLikeType t4 = type.withKeyValueHandler(keyValH);
        Assert.assertEquals(keyValH, t4.getKeyType().getValueHandler());
        Assert.assertTrue(t4.hasHandlers());

        MapLikeType t5 = type.withContentTypeHandler(contentTypeH);
        Assert.assertEquals(contentTypeH, t5.getContentTypeHandler());
        Assert.assertTrue(t5.hasHandlers());

        MapLikeType t6 = type.withContentValueHandler(contentValH);
        Assert.assertEquals(contentValH, t6.getContentValueHandler());
        Assert.assertTrue(t6.hasHandlers());
    }

    @Test
    public void testWithStaticTyping() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        Assert.assertFalse(type.useStaticType());
        MapLikeType staticType = type.withStaticTyping();
        Assert.assertTrue(staticType.useStaticType());
        Assert.assertSame(staticType, staticType.withStaticTyping());
    }

    @Test
    public void testNarrowAndRefine() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        JavaType narrowed = type._narrow(HashMap.class);
        Assert.assertEquals(HashMap.class, narrowed.getRawClass());
        Assert.assertEquals(keyT, ((MapLikeType) narrowed).getKeyType());

        TypeBindings bindings = TypeBindings.create(HashMap.class, keyT, valT);
        JavaType refined = type.refine(HashMap.class, bindings, null, null);
        Assert.assertEquals(HashMap.class, refined.getRawClass());
        Assert.assertEquals(bindings, refined.getBindings());
    }

    @Test
    public void testSignaturesAndCanonical() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        String canonical = type.toCanonical();
        Assert.assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", canonical);

        StringBuilder sbErased = new StringBuilder();
        type.getErasedSignature(sbErased);
        Assert.assertEquals("Ljava/util/Map;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        type.getGenericSignature(sbGeneric);
        Assert.assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;", sbGeneric.toString());
    }

    @Test
    public void testToString() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type = MapLikeType.construct(Map.class, keyT, valT);

        String str = type.toString();
        Assert.assertTrue(str.startsWith("[map-like type; class java.util.Map,"));
        Assert.assertTrue(str.contains("->"));
    }

    @Test
    public void testEquals() {
        JavaType keyT = _typeFactory.constructType(String.class);
        JavaType valT = _typeFactory.constructType(Integer.class);
        MapLikeType type1 = MapLikeType.construct(Map.class, keyT, valT);
        MapLikeType type2 = MapLikeType.construct(Map.class, keyT, valT);

        Assert.assertTrue(type1.equals(type1));
        Assert.assertTrue(type1.equals(type2));
        Assert.assertFalse(type1.equals(null));
        Assert.assertFalse(type1.equals("not a type"));

        JavaType diffVal = _typeFactory.constructType(Long.class);
        MapLikeType type3 = MapLikeType.construct(Map.class, keyT, diffVal);
        Assert.assertFalse(type1.equals(type3));

        JavaType diffKey = _typeFactory.constructType(Object.class);
        MapLikeType type4 = MapLikeType.construct(Map.class, diffKey, valT);
        Assert.assertFalse(type1.equals(type4));

        MapLikeType type5 = MapLikeType.construct(HashMap.class, keyT, valT);
        Assert.assertFalse(type1.equals(type5));
    }
}
