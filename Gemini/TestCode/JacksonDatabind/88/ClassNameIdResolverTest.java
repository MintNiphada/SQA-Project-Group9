package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClassNameIdResolverTest {

    private TypeFactory typeFactory;
    private ObjectMapper mapper;

    enum TestEnum {
        A {
            @Override
            public String toString() {
                return "A";
            }
        },
        B
    }

    class NonStaticInner {
    }

    static class StaticInner {
    }

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
        mapper = new ObjectMapper();
    }

    @Test
    public void testGetMechanismAndDescAndRegister() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        Assert.assertEquals(JsonTypeInfo.Id.CLASS, resolver.getMechanism());
        Assert.assertEquals("class name used as type id", resolver.getDescForKnownTypeIds());
        resolver.registerSubtype(String.class, "string");
    }

    @Test
    public void testIdFromValueBasic() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        String id = resolver.idFromValue("test string");
        Assert.assertEquals(String.class.getName(), id);
    }

    @Test
    public void testIdFromValueAndType() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        String id = resolver.idFromValueAndType("test string", CharSequence.class);
        Assert.assertEquals(CharSequence.class.getName(), id);
    }

    @Test
    public void testIdFromEnumSubclass() {
        JavaType baseType = typeFactory.constructType(TestEnum.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        String idA = resolver.idFromValue(TestEnum.A);
        Assert.assertEquals(TestEnum.class.getName(), idA);

        String idB = resolver.idFromValue(TestEnum.B);
        Assert.assertEquals(TestEnum.class.getName(), idB);
    }

    @Test
    public void testIdFromEnumSetAndEnumMap() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        EnumSet<TestEnum> set = EnumSet.of(TestEnum.A);
        String setId = resolver.idFromValue(set);
        JavaType expectedSetType = typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        Assert.assertEquals(expectedSetType.toCanonical(), setId);

        EnumMap<TestEnum, String> map = new EnumMap<>(TestEnum.class);
        map.put(TestEnum.A, "value");
        String mapId = resolver.idFromValue(map);
        JavaType expectedMapType = typeFactory.constructMapType(EnumMap.class, TestEnum.class, Object.class);
        Assert.assertEquals(expectedMapType.toCanonical(), mapId);
    }

    @Test
    public void testIdFromJavaUtilCollectionsAndArraysWrappers() {
        JavaType baseType = typeFactory.constructType(List.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        List<String> asList = Arrays.asList("1", "2");
        String asListId = resolver.idFromValue(asList);
        Assert.assertEquals("java.util.ArrayList", asListId);

        List<String> unmodList = Collections.unmodifiableList(new ArrayList<>());
        String unmodListId = resolver.idFromValue(unmodList);
        Assert.assertEquals("java.util.ArrayList", unmodListId);

        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        String syncListId = resolver.idFromValue(syncList);
        Assert.assertEquals("java.util.ArrayList", syncListId);

        HashMap<String, String> plainMap = new HashMap<>();
        String mapId = resolver.idFromValue(plainMap);
        Assert.assertEquals(HashMap.class.getName(), mapId);
    }

    @Test
    public void testIdFromInnerClass() {
        JavaType topLevelBase = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(topLevelBase, typeFactory);

        NonStaticInner innerObj = new NonStaticInner();
        String innerId = resolver.idFromValue(innerObj);
        Assert.assertEquals(Object.class.getName(), innerId);

        StaticInner staticInnerObj = new StaticInner();
        String staticId = resolver.idFromValue(staticInnerObj);
        Assert.assertEquals(StaticInner.class.getName(), staticId);

        JavaType innerBase = typeFactory.constructType(NonStaticInner.class);
        ClassNameIdResolver innerResolver = new ClassNameIdResolver(innerBase, typeFactory);
        String nonStaticPreservedId = innerResolver.idFromValue(innerObj);
        Assert.assertEquals(NonStaticInner.class.getName(), nonStaticPreservedId);
    }

    @Test
    public void testTypeFromIdGeneric() throws IOException {
        JavaType baseType = typeFactory.constructType(List.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType expectedType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        JavaType resolvedType = resolver.typeFromId(ctxt, expectedType.toCanonical());

        Assert.assertNotNull(resolvedType);
        Assert.assertEquals(ArrayList.class, resolvedType.getRawClass());
        Assert.assertEquals(String.class, resolvedType.getContentType().getRawClass());
    }

    @Test
    public void testTypeFromIdConcreteSpecialized() throws IOException {
        JavaType baseType = typeFactory.constructType(CharSequence.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType resolvedType = resolver.typeFromId(ctxt, String.class.getName());

        Assert.assertNotNull(resolvedType);
        Assert.assertEquals(String.class, resolvedType.getRawClass());
    }

    @Test
    public void testTypeFromIdClassNotFoundInDeserializationContext() throws IOException {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            resolver.typeFromId(ctxt, "com.nonexistent.NoSuchClass");
            Assert.fail("Expected exception for unknown type id");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("no such class found") || e.getMessage().contains("NoSuchClass") || e.getMessage().contains("Could not resolve"));
        }
    }

    @Test
    public void testTypeFromIdClassNotFoundInCustomContext() throws IOException {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        DatabindContext customContext = new DatabindContext() {
            @Override
            public JavaType constructType(java.lang.reflect.Type type) {
                return typeFactory.constructType(type);
            }

            @Override
            public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
                return typeFactory.constructSpecializedType(baseType, subclass);
            }

            @Override
            public com.fasterxml.jackson.databind.cfg.MapperConfig<?> getConfig() {
                return null;
            }

            @Override
            public TypeFactory getTypeFactory() {
                return typeFactory;
            }
        };

        JavaType result = resolver.typeFromId(customContext, "com.nonexistent.NoSuchClass");
        Assert.assertNull(result);
    }

    @Test
    public void testTypeFromIdInvalidId() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, typeFactory);

        DatabindContext customContext = new DatabindContext() {
            @Override
            public JavaType constructType(java.lang.reflect.Type type) {
                return typeFactory.constructType(type);
            }

            @Override
            public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
                return typeFactory.constructSpecializedType(baseType, subclass);
            }

            @Override
            public com.fasterxml.jackson.databind.cfg.MapperConfig<?> getConfig() {
                return null;
            }

            @Override
            public TypeFactory getTypeFactory() {
                return new TypeFactory(null) {
                    @Override
                    public Class<?> findClass(String className) throws ClassNotFoundException {
                        throw new RuntimeException("Simulated error");
                    }
                };
            }
        };

        try {
            resolver.typeFromId(customContext, "any.Class");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid type id 'any.Class'"));
            Assert.assertTrue(e.getMessage().contains("Simulated error"));
        } catch (IOException e) {
            Assert.fail("Expected IllegalArgumentException but got IOException");
        }
    }
}
