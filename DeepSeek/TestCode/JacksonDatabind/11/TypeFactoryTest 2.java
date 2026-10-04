package com.fasterxml.jackson.databind.type;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;

public class TypeFactoryTest {

    @Test
    public void testDefaultInstance() {
        Assert.assertNotNull(TypeFactory.defaultInstance());
    }

    @Test
    public void testUnknownType() {
        JavaType type = TypeFactory.unknownType();
        Assert.assertNotNull(type);
        Assert.assertEquals(Object.class, type.getRawClass());
    }

    @Test
    public void testRawClassWithClass() {
        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
    }

    @Test
    public void testRawClassWithParameterizedType() {
        Type type = new TypeReference<List<String>>() {}.getType();
        Assert.assertEquals(List.class, TypeFactory.rawClass(type));
    }

    @Test
    public void testConstructTypeFromClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String.class);
        Assert.assertNotNull(type);
        Assert.assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeFromJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType type = tf.constructType(stringType);
        Assert.assertSame(stringType, type);
    }

    @Test
    public void testConstructTypeFromTypeReference() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<List<String>>() {});
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(1, type.containedTypeCount());
        Assert.assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeWithContextClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(ArrayList.class.getTypeParameters()[0], ArrayList.class);
        Assert.assertNotNull(type);
    }

    @Test
    public void testConstructTypeWithContextJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType context = tf.constructType(new TypeReference<ArrayList<String>>() {});
        JavaType type = tf.constructType(ArrayList.class.getTypeParameters()[0], context);
        Assert.assertNotNull(type);
    }

    @Test
    public void testConstructArrayTypeWithClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType type = tf.constructArrayType(String.class);
        Assert.assertTrue(type.isArrayType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructArrayTypeWithJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elementType = tf.constructType(String.class);
        ArrayType type = tf.constructArrayType(elementType);
        Assert.assertTrue(type.isArrayType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionTypeWithClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType type = tf.constructCollectionType(List.class, String.class);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionTypeWithJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elementType = tf.constructType(Integer.class);
        CollectionType type = tf.constructCollectionType(Set.class, elementType);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(Set.class, type.getRawClass());
        Assert.assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeTypeWithClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionLikeType type = tf.constructCollectionLikeType(List.class, String.class);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeTypeWithJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elementType = tf.constructType(Long.class);
        CollectionLikeType type = tf.constructCollectionLikeType(Collection.class, elementType);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(Collection.class, type.getRawClass());
        Assert.assertEquals(Long.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapTypeWithJavaTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType keyType = tf.constructType(String.class);
        JavaType valueType = tf.constructType(Integer.class);
        MapType type = tf.constructMapType(Map.class, keyType, valueType);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(Map.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapTypeWithClasses() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType type = tf.constructMapType(HashMap.class, String.class, Integer.class);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(HashMap.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeTypeWithJavaTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType keyType = tf.constructType(String.class);
        JavaType valueType = tf.constructType(Boolean.class);
        MapLikeType type = tf.constructMapLikeType(Map.class, keyType, valueType);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(Map.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Boolean.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeTypeWithClasses() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapLikeType type = tf.constructMapLikeType(HashMap.class, String.class, Double.class);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(HashMap.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Double.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType[] params = new JavaType[]{ tf.constructType(String.class) };
        JavaType type = tf.constructSimpleType(Comparable.class, Comparable.class, params);
        Assert.assertEquals(Comparable.class, type.getRawClass());
        Assert.assertEquals(1, type.containedTypeCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSimpleTypeMismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType[] params = new JavaType[]{ tf.constructType(String.class), tf.constructType(Integer.class) };
        tf.constructSimpleType(Comparable.class, Comparable.class, params);
    }

    @Test
    public void testUncheckedSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.uncheckedSimpleType(String.class);
        Assert.assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructParametrizedTypeWithClasses() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(List.class, List.class, String.class);
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(1, type.containedTypeCount());
        Assert.assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructParametrizedTypeWithJavaTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType[] params = new JavaType[]{ tf.constructType(Integer.class) };
        JavaType type = tf.constructParametrizedType(Set.class, Set.class, params);
        Assert.assertEquals(Set.class, type.getRawClass());
        Assert.assertEquals(1, type.containedTypeCount());
        Assert.assertEquals(Integer.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructParametrizedTypeMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(Map.class, Map.class, String.class, Integer.class);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(Map.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametrizedTypeArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(String[].class, String[].class, String.class);
        Assert.assertTrue(type.isArrayType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeArrayWrongParamCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(String[].class, String[].class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeMapWrongParamCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(Map.class, Map.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeCollectionWrongParamCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(List.class, List.class);
    }

    @Test
    public void testConstructRawCollectionType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType type = tf.constructRawCollectionType(List.class);
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawCollectionLikeType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionLikeType type = tf.constructRawCollectionLikeType(Collection.class);
        Assert.assertEquals(Collection.class, type.getRawClass());
        Assert.assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawMapType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType type = tf.constructRawMapType(Map.class);
        Assert.assertEquals(Map.class, type.getRawClass());
        Assert.assertEquals(Object.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawMapLikeType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapLikeType type = tf.constructRawMapLikeType(Map.class);
        Assert.assertEquals(Map.class, type.getRawClass());
        Assert.assertEquals(Object.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructSpecializedTypeSameClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType base = tf.constructType(List.class);
        JavaType specialized = tf.constructSpecializedType(base, List.class);
        Assert.assertSame(base, specialized);
    }

    @Test
    public void testConstructSpecializedTypeSubclass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType base = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType specialized = tf.constructSpecializedType(base, HashMap.class);
        Assert.assertEquals(HashMap.class, specialized.getRawClass());
        Assert.assertEquals(String.class, specialized.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, specialized.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeNotSubtype() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType base = tf.constructType(String.class);
        tf.constructSpecializedType(base, Integer.class);
    }

    @Test
    public void testConstructSpecializedTypeWithHandlers() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType base = tf.constructType(new TypeReference<List<String>>() {});
        Object handler = new Object();
        base = base.withValueHandler(handler);
        base = base.withTypeHandler(handler);
        JavaType specialized = tf.constructSpecializedType(base, ArrayList.class);
        Assert.assertEquals(ArrayList.class, specialized.getRawClass());
        Assert.assertNotNull(specialized.getValueHandler());
        Assert.assertNotNull(specialized.getTypeHandler());
    }

    @Test
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructFromCanonical("Ljava.lang.String;");
        Assert.assertEquals(String.class, type.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructFromCanonical("invalid");
    }

    @Test
    public void testFindTypeParametersDirect() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType[] params = tf.findTypeParameters(type, Map.class);
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());
        Assert.assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test
    public void testFindTypeParametersIndirect() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType[] params = tf.findTypeParameters(HashMap.class, Map.class);
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNotSubtype() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.findTypeParameters(String.class, Map.class);
    }

    @Test
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t1 = tf.constructType(Number.class);
        JavaType t2 = tf.constructType(Integer.class);
        Assert.assertEquals(Integer.class, tf.moreSpecificType(t1, t2).getRawClass());
        Assert.assertEquals(Integer.class, tf.moreSpecificType(t2, t1).getRawClass());
    }

    @Test
    public void testMoreSpecificTypeNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(String.class);
        Assert.assertEquals(String.class, tf.moreSpecificType(null, t).getRawClass());
        Assert.assertEquals(String.class, tf.moreSpecificType(t, null).getRawClass());
    }

    @Test
    public void testMoreSpecificTypeSame() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(String.class);
        Assert.assertEquals(String.class, tf.moreSpecificType(t, t).getRawClass());
    }

    @Test
    public void testClearCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(String.class);
        tf.clearCache();
        // No exception means success
    }

    @Test
    public void testWithModifier() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeFactory modified = tf.withModifier(null);
        Assert.assertNotNull(modified);
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        modified = tf.withModifier(mod);
        Assert.assertNotNull(modified);
    }

    @Test
    public void testConstructTypeGenericArray() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m = TypeFactoryTest.class.getDeclaredMethod("genericArrayMethod");
        Type returnType = m.getGenericReturnType();
        JavaType type = tf.constructType(returnType);
        Assert.assertTrue(type.isArrayType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeTypeVariable() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m = TypeFactoryTest.class.getDeclaredMethod("typeVariableMethod", List.class);
        Type paramType = m.getGenericParameterTypes()[0];
        JavaType type = tf.constructType(paramType);
        Assert.assertEquals(List.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeWildcard() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m = TypeFactoryTest.class.getDeclaredMethod("wildcardMethod", List.class);
        Type paramType = m.getGenericParameterTypes()[0];
        JavaType type = tf.constructType(paramType);
        Assert.assertEquals(List.class, type.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeUnrecognized() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(new Type() {
            @Override
            public String getTypeName() {
                return "Unknown";
            }
        });
    }

    @Test
    public void testConstructTypeNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructType((Type) null);
            Assert.fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFromClassCoreTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertEquals(String.class, tf.constructType(String.class).getRawClass());
        Assert.assertEquals(Boolean.TYPE, tf.constructType(Boolean.TYPE).getRawClass());
        Assert.assertEquals(Integer.TYPE, tf.constructType(Integer.TYPE).getRawClass());
        Assert.assertEquals(Long.TYPE, tf.constructType(Long.TYPE).getRawClass());
    }

    @Test
    public void testFromClassArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String[].class);
        Assert.assertTrue(type.isArrayType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFromClassEnum() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(TimeUnit.class);
        Assert.assertTrue(type.isEnumType());
    }

    @Test
    public void testFromClassMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(HashMap.class);
        Assert.assertTrue(type.isMapLikeType());
    }

    @Test
    public void testFromClassCollection() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(ArrayList.class);
        Assert.assertTrue(type.isCollectionLikeType());
    }

    @Test
    public void testFromClassMapEntry() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(Map.Entry.class);
        Assert.assertEquals(Map.Entry.class, type.getRawClass());
    }

    @Test
    public void testFromParameterizedClassMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        List<JavaType> params = new ArrayList<JavaType>();
        params.add(tf.constructType(String.class));
        params.add(tf.constructType(Integer.class));
        JavaType type = tf._fromParameterizedClass(HashMap.class, params);
        Assert.assertTrue(type.isMapLikeType());
        Assert.assertEquals(String.class, type.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFromParameterizedClassCollection() {
        TypeFactory tf = TypeFactory.defaultInstance();
        List<JavaType> params = new ArrayList<JavaType>();
        params.add(tf.constructType(String.class));
        JavaType type = tf._fromParameterizedClass(ArrayList.class, params);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFromParameterizedClassSimple() {
        TypeFactory tf = TypeFactory.defaultInstance();
        List<JavaType> params = new ArrayList<JavaType>();
        params.add(tf.constructType(String.class));
        JavaType type = tf._fromParameterizedClass(Comparable.class, params);
        Assert.assertEquals(Comparable.class, type.getRawClass());
    }

    @Test
    public void testFromParamTypeMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<HashMap<String, Integer>>() {}.getType();
        JavaType jt = tf.constructType(type);
        Assert.assertTrue(jt.isMapLikeType());
        Assert.assertEquals(String.class, jt.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, jt.getContentType().getRawClass());
    }

    @Test
    public void testFromParamTypeCollection() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<ArrayList<Long>>() {}.getType();
        JavaType jt = tf.constructType(type);
        Assert.assertTrue(jt.isCollectionLikeType());
        Assert.assertEquals(Long.class, jt.getContentType().getRawClass());
    }

    @Test
    public void testFromParamTypeSimple() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<Comparable<String>>() {}.getType();
        JavaType jt = tf.constructType(type);
        Assert.assertEquals(Comparable.class, jt.getRawClass());
    }

    @Test
    public void testFindSuperTypeChainHashMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType ht = tf._findSuperTypeChain(HashMap.class, Map.class);
        Assert.assertNotNull(ht);
        Assert.assertEquals(HashMap.class, ht.getRawClass());
    }

    @Test
    public void testFindSuperTypeChainArrayList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType ht = tf._findSuperTypeChain(ArrayList.class, List.class);
        Assert.assertNotNull(ht);
        Assert.assertEquals(ArrayList.class, ht.getRawClass());
    }

    @Test
    public void testFindSuperTypeChainClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType ht = tf._findSuperTypeChain(Integer.class, Number.class);
        Assert.assertNotNull(ht);
        Assert.assertEquals(Integer.class, ht.getRawClass());
    }

    @Test
    public void testFindSuperTypeChainNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType ht = tf._findSuperTypeChain(String.class, Map.class);
        Assert.assertNull(ht);
    }

    @Test
    public void testResolveVariableViaSubTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType leaf = new HierarchicType(new TypeReference<HashMap<String, Integer>>() {}.getType());
        TypeBindings bindings = new TypeBindings(tf, HashMap.class);
        JavaType resolved = tf._resolveVariableViaSubTypes(leaf, "K", bindings);
        Assert.assertNotNull(resolved);
    }

    @Test
    public void testResolveVariableViaSubTypesNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings bindings = new TypeBindings(tf, String.class);
        JavaType resolved = tf._resolveVariableViaSubTypes(null, "T", bindings);
        Assert.assertEquals(Object.class, resolved.getRawClass());
    }

    // Helper methods for reflection tests
    public String[] genericArrayMethod() { return null; }
    public <T> void typeVariableMethod(List<T> param) {}
    public void wildcardMethod(List<?> param) {}
}
