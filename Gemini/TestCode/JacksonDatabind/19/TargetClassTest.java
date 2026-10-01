package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TypeFactoryTest {

    private TypeFactory tf;

    public enum SampleEnum { A, B }

    public static class CustomGeneric<T, U> {
        public T key;
        public U value;
    }

    public static class SubGeneric extends CustomGeneric<String, Integer> {}

    public static class CustomList<E> extends ArrayList<E> {}

    public static class CustomMap<K, V> extends HashMap<K, V> {}

    public static class CustomRef<T> extends AtomicReference<T> {}

    public static class CustomEntry<K, V> implements Map.Entry<K, V> {
        private K k;
        private V v;
        @Override public K getKey() { return k; }
        @Override public V getValue() { return v; }
        @Override public V setValue(V value) { this.v = value; return v; }
    }

    public static class RawRef extends AtomicReference {}

    public static class RawEntry implements Map.Entry {
        @Override public Object getKey() { return null; }
        @Override public Object getValue() { return null; }
        @Override public Object setValue(Object value) { return null; }
    }

    public static class FieldContainer<T extends Comparable<T>> {
        public List<String>[] arrayOfList;
        public List<? extends Number> wildcardList;
        public List<? super Integer> wildcardSuperList;
        public T boundedGeneric;
        public List<T> genericList;
    }

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
        tf.clearCache();
    }

    @Test
    public void testSingletonAndBasicInstances() {
        Assert.assertNotNull(tf);
        Assert.assertSame(tf, TypeFactory.defaultInstance());

        JavaType unknown = TypeFactory.unknownType();
        Assert.assertNotNull(unknown);
        Assert.assertEquals(Object.class, unknown.getRawClass());

        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType jt = tf.constructType(Integer.class);
        Assert.assertEquals(Integer.class, TypeFactory.rawClass(jt));
    }

    @Test
    public void testCorePrimitiveAndCachedTypes() {
        JavaType strType = tf.constructType(String.class);
        Assert.assertSame(TypeFactory.CORE_TYPE_STRING, strType);

        JavaType boolType = tf.constructType(Boolean.TYPE);
        Assert.assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType intType = tf.constructType(Integer.TYPE);
        Assert.assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType longType = tf.constructType(Long.TYPE);
        Assert.assertSame(TypeFactory.CORE_TYPE_LONG, longType);

        // Cache hit
        JavaType doubleType1 = tf.constructType(Double.class);
        JavaType doubleType2 = tf.constructType(Double.class);
        Assert.assertSame(doubleType1, doubleType2);

        tf.clearCache();
    }

    @Test
    public void testModifiers() {
        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == Integer.class) {
                    return typeFactory.constructType(Long.class);
                }
                return type;
            }
        };

        TypeFactory tfWithMod = tf.withModifier(mod1);
        Assert.assertNotSame(tf, tfWithMod);

        // withModifier(null) returns new instance with existing modifiers
        TypeFactory tfNullMod = tf.withModifier(null);
        Assert.assertNotNull(tfNullMod);

        // Multiple modifiers
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithTwoMods = tfWithMod.withModifier(mod2);
        Assert.assertNotNull(tfWithTwoMods);

        JavaType modified = tfWithMod.constructType(Integer.class);
        Assert.assertEquals(Long.class, modified.getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType baseMap = tf.constructType(Map.class);
        JavaType specializedMap = tf.constructSpecializedType(baseMap, HashMap.class);
        Assert.assertEquals(HashMap.class, specializedMap.getRawClass());

        // Same class optimization
        JavaType same = tf.constructSpecializedType(specializedMap, HashMap.class);
        Assert.assertSame(specializedMap, same);

        // Handlers preserved
        JavaType simpleBase = SimpleType.construct(Object.class).withValueHandler("valH").withTypeHandler("typeH");
        JavaType specializedWithHandlers = tf.constructSpecializedType(simpleBase, ArrayList.class);
        Assert.assertEquals(ArrayList.class, specializedWithHandlers.getRawClass());
        Assert.assertEquals("valH", specializedWithHandlers.getValueHandler());
        Assert.assertEquals("typeH", specializedWithHandlers.getTypeHandler());

        // Array specialization
        JavaType objBase = tf.constructType(Object.class);
        JavaType arraySpec = tf.constructSpecializedType(objBase, String[].class);
        Assert.assertTrue(arraySpec.isArrayType());

        // Non-SimpleType narrowing
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        JavaType subListType = tf.constructSpecializedType(listType, ArrayList.class);
        Assert.assertEquals(ArrayList.class, subListType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeIncompatible() {
        JavaType listType = tf.constructType(List.class);
        tf.constructSpecializedType(listType, HashMap.class);
    }

    @Test
    public void testConstructFromCanonical() {
        JavaType t = tf.constructFromCanonical("java.lang.String");
        Assert.assertEquals(String.class, t.getRawClass());

        JavaType listType = tf.constructFromCanonical("java.util.List<java.lang.Integer>");
        Assert.assertEquals(List.class, listType.getRawClass());
        Assert.assertEquals(Integer.class, listType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        tf.constructFromCanonical("unknown.Class.Name");
    }

    @Test
    public void testFindTypeParameters() {
        JavaType subGenType = tf.constructType(SubGeneric.class);
        JavaType[] params = tf.findTypeParameters(subGenType, CustomGeneric.class);
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());
        Assert.assertEquals(Integer.class, params[1].getRawClass());

        // Parameter source matches directly
        JavaType mapType = tf.constructMapType(Map.class, String.class, Long.class);
        JavaType[] mapParams = tf.findTypeParameters(mapType, Map.class);
        Assert.assertNotNull(mapParams);
        Assert.assertEquals(2, mapParams.length);
        Assert.assertEquals(String.class, mapParams[0].getRawClass());
        Assert.assertEquals(Long.class, mapParams[1].getRawClass());

        // No parameters source count == 0
        JavaType simpleObj = tf.constructType(Object.class);
        Assert.assertNull(tf.findTypeParameters(simpleObj, Object.class));

        // From class directly
        JavaType[] listParams = tf.findTypeParameters(CustomList.class, List.class);
        Assert.assertNotNull(listParams);
        Assert.assertEquals(1, listParams.length);

        // Non-generic target
        JavaType[] objParams = tf.findTypeParameters(String.class, Object.class);
        Assert.assertNull(objParams);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNotSubtype() {
        tf.findTypeParameters(String.class, List.class);
    }

    @Test
    public void testMoreSpecificType() {
        JavaType stringType = tf.constructType(String.class);
        JavaType objType = tf.constructType(Object.class);

        Assert.assertSame(stringType, tf.moreSpecificType(stringType, null));
        Assert.assertSame(stringType, tf.moreSpecificType(null, stringType));
        Assert.assertSame(stringType, tf.moreSpecificType(stringType, stringType));

        // Object is assignable from String, so String is more specific
        Assert.assertSame(stringType, tf.moreSpecificType(objType, stringType));
        Assert.assertSame(stringType, tf.moreSpecificType(stringType, objType));

        JavaType intType = tf.constructType(Integer.class);
        Assert.assertSame(stringType, tf.moreSpecificType(stringType, intType));
    }

    @Test
    public void testConstructTypeOverloads() {
        JavaType t1 = tf.constructType(new TypeReference<List<String>>() {});
        Assert.assertEquals(List.class, t1.getRawClass());
        Assert.assertEquals(String.class, t1.getContentType().getRawClass());

        JavaType t2 = tf.constructType(String.class, (Class<?>) null);
        Assert.assertEquals(String.class, t2.getRawClass());

        JavaType t3 = tf.constructType(String.class, String.class);
        Assert.assertEquals(String.class, t3.getRawClass());

        JavaType t4 = tf.constructType(String.class, (JavaType) null);
        Assert.assertEquals(String.class, t4.getRawClass());

        JavaType t5 = tf.constructType(String.class, t1);
        Assert.assertEquals(String.class, t5.getRawClass());

        TypeBindings bindings = new TypeBindings(tf, SubGeneric.class);
        JavaType t6 = tf.constructType(String.class, bindings);
        Assert.assertEquals(String.class, t6.getRawClass());

        // JavaType directly returned
        JavaType t7 = tf.constructType(t1);
        Assert.assertSame(t1, t7);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeUnrecognized() {
        tf._constructType(null, null);
    }

    @Test
    public void testDirectFactoryMethods() {
        ArrayType arr1 = tf.constructArrayType(String.class);
        Assert.assertEquals(String.class, arr1.getContentType().getRawClass());

        ArrayType arr2 = tf.constructArrayType(arr1.getContentType());
        Assert.assertEquals(String.class, arr2.getContentType().getRawClass());

        CollectionType col1 = tf.constructCollectionType(List.class, String.class);
        Assert.assertEquals(List.class, col1.getRawClass());
        Assert.assertEquals(String.class, col1.getContentType().getRawClass());

        CollectionType col2 = tf.constructCollectionType(Set.class, arr1.getContentType());
        Assert.assertEquals(Set.class, col2.getRawClass());

        CollectionLikeType colLike1 = tf.constructCollectionLikeType(List.class, Integer.class);
        Assert.assertEquals(List.class, colLike1.getRawClass());

        CollectionLikeType colLike2 = tf.constructCollectionLikeType(List.class, arr1.getContentType());
        Assert.assertEquals(List.class, colLike2.getRawClass());

        MapType map1 = tf.constructMapType(Map.class, String.class, Integer.class);
        Assert.assertEquals(String.class, map1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, map1.getContentType().getRawClass());

        MapType map2 = tf.constructMapType(Map.class, map1.getKeyType(), map1.getContentType());
        Assert.assertEquals(Map.class, map2.getRawClass());

        MapLikeType mapLike1 = tf.constructMapLikeType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, mapLike1.getRawClass());

        MapLikeType mapLike2 = tf.constructMapLikeType(Map.class, map1.getKeyType(), map1.getContentType());
        Assert.assertEquals(Map.class, mapLike2.getRawClass());

        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());

        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        Assert.assertEquals(String.class, unchecked.getRawClass());
    }

    @Test
    public void testConstructParametrizedTypes() {
        JavaType pt1 = tf.constructParametrizedType(CustomGeneric.class, CustomGeneric.class, String.class, Integer.class);
        Assert.assertEquals(CustomGeneric.class, pt1.getRawClass());
        Assert.assertEquals(2, pt1.containedTypeCount());

        @SuppressWarnings("deprecation")
        JavaType pt2 = tf.constructParametricType(CustomGeneric.class, String.class, Integer.class);
        Assert.assertEquals(CustomGeneric.class, pt2.getRawClass());

        JavaType pt3 = tf.constructParametrizedType(String[].class, String[].class, tf.constructType(String.class));
        Assert.assertTrue(pt3.isArrayType());

        JavaType pt4 = tf.constructParametrizedType(Map.class, Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertTrue(pt4.isMapLikeType());

        JavaType pt5 = tf.constructParametrizedType(List.class, List.class, tf.constructType(String.class));
        Assert.assertTrue(pt5.isContainerType());

        @SuppressWarnings("deprecation")
        JavaType pt6 = tf.constructParametricType(List.class, tf.constructType(String.class));
        Assert.assertTrue(pt6.isContainerType());

        @SuppressWarnings("deprecation")
        JavaType simpleDeprecated = tf.constructSimpleType(CustomGeneric.class, new JavaType[]{tf.constructType(String.class), tf.constructType(Integer.class)});
        Assert.assertEquals(CustomGeneric.class, simpleDeprecated.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeArrayInvalidArgs() {
        tf.constructParametrizedType(String[].class, String[].class, tf.constructType(String.class), tf.constructType(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeMapInvalidArgs() {
        tf.constructParametrizedType(Map.class, Map.class, tf.constructType(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeCollectionInvalidArgs() {
        tf.constructParametrizedType(List.class, List.class, tf.constructType(String.class), tf.constructType(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSimpleTypeMismatch() {
        tf.constructSimpleType(CustomGeneric.class, CustomGeneric.class, new JavaType[]{tf.constructType(String.class)});
    }

    @Test
    public void testRawFactoryMethods() {
        CollectionType rawCol = tf.constructRawCollectionType(ArrayList.class);
        Assert.assertEquals(ArrayList.class, rawCol.getRawClass());
        Assert.assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(ArrayList.class);
        Assert.assertEquals(ArrayList.class, rawColLike.getRawClass());

        MapType rawMap = tf.constructRawMapType(HashMap.class);
        Assert.assertEquals(HashMap.class, rawMap.getRawClass());
        Assert.assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(HashMap.class);
        Assert.assertEquals(HashMap.class, rawMapLike.getRawClass());
    }

    @Test
    public void testEnumAndAtomicReferenceAndMapEntryHandling() {
        JavaType enumType = tf.constructType(SampleEnum.class);
        Assert.assertTrue(enumType.isEnumType());

        JavaType refType = tf.constructType(AtomicReference.class);
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());

        JavaType customRefType = tf.constructType(CustomRef.class);
        Assert.assertEquals(CustomRef.class, customRefType.getRawClass());

        JavaType rawRefType = tf.constructType(RawRef.class);
        Assert.assertEquals(RawRef.class, rawRefType.getRawClass());

        JavaType mapEntryType = tf.constructType(Map.Entry.class);
        Assert.assertEquals(Map.Entry.class, mapEntryType.getRawClass());

        JavaType customEntryType = tf.constructType(CustomEntry.class);
        Assert.assertEquals(CustomEntry.class, customEntryType.getRawClass());

        JavaType rawEntryType = tf.constructType(RawEntry.class);
        Assert.assertEquals(RawEntry.class, rawEntryType.getRawClass());
    }

    @Test
    public void testFromParameterizedClass() {
        List<JavaType> listTypes = new ArrayList<JavaType>();
        listTypes.add(tf.constructType(String.class));

        JavaType arrFromParam = tf._fromParameterizedClass(String[].class, listTypes);
        Assert.assertTrue(arrFromParam.isArrayType());

        JavaType enumFromParam = tf._fromParameterizedClass(SampleEnum.class, listTypes);
        Assert.assertTrue(enumFromParam.isEnumType());

        JavaType colFromParam = tf._fromParameterizedClass(List.class, listTypes);
        Assert.assertTrue(colFromParam.isCollectionLikeType());

        JavaType rawColFromParam = tf._fromParameterizedClass(List.class, new ArrayList<JavaType>());
        Assert.assertTrue(rawColFromParam.isCollectionLikeType());

        List<JavaType> mapTypes = new ArrayList<JavaType>();
        mapTypes.add(tf.constructType(String.class));
        mapTypes.add(tf.constructType(Integer.class));

        JavaType mapFromParam = tf._fromParameterizedClass(Map.class, mapTypes);
        Assert.assertTrue(mapFromParam.isMapLikeType());

        List<JavaType> singleMapType = new ArrayList<JavaType>();
        singleMapType.add(tf.constructType(String.class));
        JavaType mapSingleFromParam = tf._fromParameterizedClass(Map.class, singleMapType);
        Assert.assertTrue(mapSingleFromParam.isMapLikeType());

        JavaType rawMapFromParam = tf._fromParameterizedClass(Map.class, new ArrayList<JavaType>());
        Assert.assertTrue(rawMapFromParam.isMapLikeType());

        JavaType simpleFromParam0 = tf._fromParameterizedClass(Object.class, new ArrayList<JavaType>());
        Assert.assertEquals(Object.class, simpleFromParam0.getRawClass());

        JavaType customFromParam = tf._fromParameterizedClass(CustomGeneric.class, mapTypes);
        Assert.assertEquals(CustomGeneric.class, customFromParam.getRawClass());
    }

    @Test
    public void testReflectionTypeResolution() throws Exception {
        Field arrayOfListField = FieldContainer.class.getField("arrayOfList");
        GenericArrayType gat = (GenericArrayType) arrayOfListField.getGenericType();
        JavaType gatType = tf.constructType(gat);
        Assert.assertTrue(gatType.isArrayType());

        Field wildcardListField = FieldContainer.class.getField("wildcardList");
        ParameterizedType ptWildcard = (ParameterizedType) wildcardListField.getGenericType();
        WildcardType wt = (WildcardType) ptWildcard.getActualTypeArguments()[0];
        JavaType wtType = tf._fromWildcard(wt, null);
        Assert.assertEquals(Number.class, wtType.getRawClass());

        Field boundedGenericField = FieldContainer.class.getField("boundedGeneric");
        TypeVariable<?> tv = (TypeVariable<?>) boundedGenericField.getGenericType();
        JavaType tvTypeNoContext = tf._fromVariable(tv, null);
        Assert.assertEquals(Comparable.class, tvTypeNoContext.getRawClass());

        TypeBindings bindings = new TypeBindings(tf, FieldContainer.class);
        bindings.addBinding("T", tf.constructType(String.class));
        JavaType tvTypeWithContext = tf._fromVariable(tv, bindings);
        Assert.assertEquals(String.class, tvTypeWithContext.getRawClass());
    }

    @Test
    public void testSuperInterfaceChainsAndCaching() {
        HierarchicType hmChain = tf._findSuperInterfaceChain(HashMap.class, Map.class);
        Assert.assertNotNull(hmChain);
        // Second call should exercise cached chain
        HierarchicType hmChainCached = tf._findSuperInterfaceChain(HashMap.class, Map.class);
        Assert.assertNotNull(hmChainCached);

        HierarchicType alChain = tf._findSuperInterfaceChain(ArrayList.class, List.class);
        Assert.assertNotNull(alChain);
        // Second call should exercise cached chain
        HierarchicType alChainCached = tf._findSuperInterfaceChain(ArrayList.class, List.class);
        Assert.assertNotNull(alChainCached);

        // Class inheritance chain
        HierarchicType classChain = tf._findSuperClassChain(ArrayList.class, Object.class);
        Assert.assertNotNull(classChain);
    }

    @Test
    public void testResolveVariableViaSubTypes() {
        HierarchicType leaf = tf._findSuperTypeChain(SubGeneric.class, CustomGeneric.class);
        JavaType resolvedT = tf._resolveVariableViaSubTypes(leaf, "T", new TypeBindings(tf, SubGeneric.class));
        Assert.assertEquals(String.class, resolvedT.getRawClass());

        JavaType resolvedUnknown = tf._resolveVariableViaSubTypes(null, "X", null);
        Assert.assertEquals(Object.class, resolvedUnknown.getRawClass());
    }
}
