package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TypeFactoryTest {

    static class CustomList<E> extends ArrayList<E> {}
    static class StringLongMap extends HashMap<String, Long> {}
    static class CustomMap<K, V> extends HashMap<K, V> {}
    static class SingleParam<T> { public T value; }
    static class TwoParam<A, B> { public A a; public B b; }
    static class ThreeParam<A, B, C> { public A a; public B b; public C c; }
    static class NonGenericSubclass extends SingleParam<String> {}
    static class Holder {
        public List<?> wildcardList;
        public List<? extends Number> wildcardExtends;
        public List<String>[] genericArray;
        public <T> T genericMethod(T arg) { return arg; }
    }
    static class SelfRef {
        public SelfRef next;
    }
    static class GenericSelfRef<T extends GenericSelfRef<T>> {
        public T next;
    }
    static class DummyNonContainer {}
    interface CustomInterface<T> {}
    static class CustomInterfaceImpl implements CustomInterface<String> {}

    @Test
    public void testDefaultInstanceAndCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertNotNull(tf);
        tf.clearCache();

        TypeFactory withCache = tf.withCache(new LRUMap<Object, JavaType>(10, 50));
        Assert.assertNotNull(withCache);
        withCache.clearCache();
    }

    @Test
    public void testWithClassLoader() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory customLoader = tf.withClassLoader(cl);
        Assert.assertSame(cl, customLoader.getClassLoader());
    }

    @Test
    public void testWithModifier() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tf1 = tf.withModifier(mod1);
        Assert.assertNotNull(tf1);

        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tf2 = tf1.withModifier(mod2);
        Assert.assertNotNull(tf2);

        TypeFactory tfNull = tf2.withModifier(null);
        Assert.assertNotNull(tfNull);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithModifierReturningNull() {
        TypeModifier badMod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return null;
            }
        };
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(badMod);
        tf.constructType(String.class);
    }

    @Test
    public void testUnknownTypeAndRawClass() {
        JavaType unknown = TypeFactory.unknownType();
        Assert.assertEquals(Object.class, unknown.getRawClass());
        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
        Assert.assertEquals(List.class, TypeFactory.rawClass(new TypeReference<List<String>>() {}.getType()));
    }

    @Test
    public void testFindClassPrimitives() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertEquals(int.class, tf.findClass("int"));
        Assert.assertEquals(long.class, tf.findClass("long"));
        Assert.assertEquals(float.class, tf.findClass("float"));
        Assert.assertEquals(double.class, tf.findClass("double"));
        Assert.assertEquals(boolean.class, tf.findClass("boolean"));
        Assert.assertEquals(byte.class, tf.findClass("byte"));
        Assert.assertEquals(char.class, tf.findClass("char"));
        Assert.assertEquals(short.class, tf.findClass("short"));
        Assert.assertEquals(void.class, tf.findClass("void"));
    }

    @Test
    public void testFindClassStandard() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertEquals(String.class, tf.findClass("java.lang.String"));
        Assert.assertEquals(TypeFactoryTest.class, tf.findClass(TypeFactoryTest.class.getName()));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClassNotFound() throws Exception {
        TypeFactory.defaultInstance().findClass("com.invalid.NonExistingClass12345");
    }

    @Test
    public void testWellKnownTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertEquals(boolean.class, tf.constructType(boolean.class).getRawClass());
        Assert.assertEquals(int.class, tf.constructType(int.class).getRawClass());
        Assert.assertEquals(long.class, tf.constructType(long.class).getRawClass());
        Assert.assertEquals(String.class, tf.constructType(String.class).getRawClass());
        Assert.assertEquals(Object.class, tf.constructType(Object.class).getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);

        JavaType same = tf.constructSpecializedType(listType, List.class);
        Assert.assertSame(listType, same);

        JavaType arrayListType = tf.constructSpecializedType(listType, ArrayList.class);
        Assert.assertEquals(ArrayList.class, arrayListType.getRawClass());
        Assert.assertEquals(String.class, arrayListType.getContentType().getRawClass());

        JavaType linkedListType = tf.constructSpecializedType(listType, LinkedList.class);
        Assert.assertEquals(LinkedList.class, linkedListType.getRawClass());

        JavaType setType = tf.constructCollectionType(Set.class, String.class);
        JavaType hashSetType = tf.constructSpecializedType(setType, HashSet.class);
        Assert.assertEquals(HashSet.class, hashSetType.getRawClass());

        JavaType treeSetType = tf.constructSpecializedType(setType, TreeSet.class);
        Assert.assertEquals(TreeSet.class, treeSetType.getRawClass());

        JavaType enumSetType = tf.constructCollectionType(EnumSet.class, SimpleEnum.class);
        JavaType specializedEnumSet = tf.constructSpecializedType(enumSetType, EnumSet.class);
        Assert.assertSame(enumSetType, specializedEnumSet);

        JavaType mapType = tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType hashMapType = tf.constructSpecializedType(mapType, HashMap.class);
        Assert.assertEquals(HashMap.class, hashMapType.getRawClass());
        Assert.assertEquals(String.class, hashMapType.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, hashMapType.getContentType().getRawClass());

        JavaType linkedMapType = tf.constructSpecializedType(mapType, LinkedHashMap.class);
        Assert.assertEquals(LinkedHashMap.class, linkedMapType.getRawClass());

        JavaType treeMapType = tf.constructSpecializedType(mapType, TreeMap.class);
        Assert.assertEquals(TreeMap.class, treeMapType.getRawClass());

        JavaType enumMapType = tf.constructSpecializedType(mapType, EnumMap.class);
        Assert.assertEquals(EnumMap.class, enumMapType.getRawClass());

        JavaType objType = tf.constructType(Object.class);
        JavaType specializedFromObj = tf.constructSpecializedType(objType, String.class);
        Assert.assertEquals(String.class, specializedFromObj.getRawClass());

        JavaType rawList = tf.constructRawCollectionType(List.class);
        JavaType customList = tf.constructSpecializedType(rawList, CustomList.class);
        Assert.assertEquals(CustomList.class, customList.getRawClass());

        JavaType singleParamType = tf.constructParametricType(SingleParam.class, String.class);
        JavaType nonGenSub = tf.constructSpecializedType(singleParamType, NonGenericSubclass.class);
        Assert.assertEquals(NonGenericSubclass.class, nonGenSub.getRawClass());

        JavaType twoParamType = tf.constructParametricType(TwoParam.class, String.class, Integer.class);
        JavaType specializedTwo = tf.constructSpecializedType(twoParamType, TwoParam.class);
        Assert.assertEquals(TwoParam.class, specializedTwo.getRawClass());

        JavaType threeParamType = tf.constructParametricType(ThreeParam.class, String.class, Integer.class, Boolean.class);
        JavaType specializedThree = tf.constructSpecializedType(threeParamType, ThreeParam.class);
        Assert.assertEquals(ThreeParam.class, specializedThree.getRawClass());

        JavaType customInterfaceType = tf.constructParametricType(CustomInterface.class, String.class);
        JavaType specializedInterface = tf.constructSpecializedType(customInterfaceType, CustomInterfaceImpl.class);
        Assert.assertEquals(CustomInterfaceImpl.class, specializedInterface.getRawClass());
    }

    enum SimpleEnum { A, B }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeInvalid() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructSpecializedType(listType, Map.class);
    }

    @Test
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);

        JavaType same = tf.constructGeneralizedType(arrayListType, ArrayList.class);
        Assert.assertSame(arrayListType, same);

        JavaType listType = tf.constructGeneralizedType(arrayListType, List.class);
        Assert.assertEquals(List.class, listType.getRawClass());
        Assert.assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType collectionType = tf.constructGeneralizedType(arrayListType, Collection.class);
        Assert.assertEquals(Collection.class, collectionType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedTypeInvalid() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructGeneralizedType(listType, Set.class);
    }

    @Test
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructFromCanonical("java.util.List<java.lang.String>");
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringLongMap = tf.constructType(StringLongMap.class);
        JavaType[] params = tf.findTypeParameters(stringLongMap, Map.class);
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());
        Assert.assertEquals(Long.class, params[1].getRawClass());

        JavaType[] notFound = tf.findTypeParameters(stringLongMap, List.class);
        Assert.assertEquals(0, notFound.length);

        @SuppressWarnings("deprecation")
        JavaType[] deprecated1 = tf.findTypeParameters(StringLongMap.class, Map.class, TypeBindings.emptyBindings());
        Assert.assertEquals(2, deprecated1.length);

        @SuppressWarnings("deprecation")
        JavaType[] deprecated2 = tf.findTypeParameters(StringLongMap.class, Map.class);
        Assert.assertEquals(2, deprecated2.length);
    }

    @Test
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);
        JavaType mapType = tf.constructMapType(Map.class, String.class, String.class);

        Assert.assertSame(listType, tf.moreSpecificType(listType, null));
        Assert.assertSame(listType, tf.moreSpecificType(null, listType));
        Assert.assertSame(listType, tf.moreSpecificType(listType, listType));
        Assert.assertSame(arrayListType, tf.moreSpecificType(listType, arrayListType));
        Assert.assertSame(arrayListType, tf.moreSpecificType(arrayListType, listType));
        Assert.assertSame(listType, tf.moreSpecificType(listType, mapType));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testConstructTypeVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType typeRef = tf.constructType(new TypeReference<List<String>>() {});
        Assert.assertEquals(List.class, typeRef.getRawClass());
        Assert.assertEquals(String.class, typeRef.getContentType().getRawClass());

        JavaType fromClassContext = tf.constructType(List.class, StringLongMap.class);
        Assert.assertEquals(List.class, fromClassContext.getRawClass());

        JavaType fromNullClassContext = tf.constructType(String.class, (Class<?>) null);
        Assert.assertEquals(String.class, fromNullClassContext.getRawClass());

        JavaType fromJavaTypeContext = tf.constructType(List.class, typeRef);
        Assert.assertEquals(List.class, fromJavaTypeContext.getRawClass());

        JavaType fromNullJavaTypeContext = tf.constructType(String.class, (JavaType) null);
        Assert.assertEquals(String.class, fromNullJavaTypeContext.getRawClass());

        JavaType javaTypeInput = tf.constructType(typeRef);
        Assert.assertSame(typeRef, javaTypeInput);
    }

    @Test
    public void testConstructArrays() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType arr1 = tf.constructArrayType(String.class);
        Assert.assertEquals(String[].class, arr1.getRawClass());
        Assert.assertEquals(String.class, arr1.getContentType().getRawClass());

        ArrayType arr2 = tf.constructArrayType(tf.constructType(Integer.class));
        Assert.assertEquals(Integer[].class, arr2.getRawClass());
        Assert.assertEquals(Integer.class, arr2.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollections() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType col1 = tf.constructCollectionType(List.class, String.class);
        Assert.assertEquals(List.class, col1.getRawClass());
        Assert.assertEquals(String.class, col1.getContentType().getRawClass());

        CollectionType col2 = tf.constructCollectionType(ArrayList.class, tf.constructType(Integer.class));
        Assert.assertEquals(ArrayList.class, col2.getRawClass());
        Assert.assertEquals(Integer.class, col2.getContentType().getRawClass());

        CollectionLikeType colLike1 = tf.constructCollectionLikeType(DummyNonContainer.class, String.class);
        Assert.assertEquals(DummyNonContainer.class, colLike1.getRawClass());
        Assert.assertEquals(String.class, colLike1.getContentType().getRawClass());

        CollectionLikeType colLike2 = tf.constructCollectionLikeType(List.class, tf.constructType(String.class));
        Assert.assertEquals(List.class, colLike2.getRawClass());

        CollectionType rawCol = tf.constructRawCollectionType(List.class);
        Assert.assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(DummyNonContainer.class);
        Assert.assertEquals(Object.class, rawColLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructMaps() {
        TypeFactory tf = TypeFactory.defaultInstance();

        MapType map1 = tf.constructMapType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, map1.getRawClass());
        Assert.assertEquals(String.class, map1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, map1.getContentType().getRawClass());

        MapType map2 = tf.constructMapType(HashMap.class, tf.constructType(String.class), tf.constructType(Long.class));
        Assert.assertEquals(HashMap.class, map2.getRawClass());

        MapType propMap = tf.constructMapType(Properties.class, Object.class, Object.class);
        Assert.assertEquals(Properties.class, propMap.getRawClass());
        Assert.assertEquals(String.class, propMap.getKeyType().getRawClass());
        Assert.assertEquals(String.class, propMap.getContentType().getRawClass());

        MapLikeType mapLike1 = tf.constructMapLikeType(DummyNonContainer.class, String.class, Integer.class);
        Assert.assertEquals(DummyNonContainer.class, mapLike1.getRawClass());
        Assert.assertEquals(String.class, mapLike1.getKeyType().getRawClass());

        MapLikeType mapLike2 = tf.constructMapLikeType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, mapLike2.getRawClass());

        MapType rawMap = tf.constructRawMapType(Map.class);
        Assert.assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(DummyNonContainer.class);
        Assert.assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testConstructSimpleAndParametric() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType simple1 = tf.constructSimpleType(String.class, new JavaType[0]);
        Assert.assertEquals(String.class, simple1.getRawClass());

        JavaType simple2 = tf.constructSimpleType(String.class, String.class, new JavaType[0]);
        Assert.assertEquals(String.class, simple2.getRawClass());

        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        Assert.assertEquals(String.class, unchecked.getRawClass());

        JavaType param1 = tf.constructParametricType(List.class, String.class);
        Assert.assertEquals(List.class, param1.getRawClass());

        JavaType param2 = tf.constructParametrizedType(List.class, List.class, String.class);
        Assert.assertEquals(List.class, param2.getRawClass());

        JavaType param3 = tf.constructParametrizedType(List.class, List.class, tf.constructType(String.class));
        Assert.assertEquals(List.class, param3.getRawClass());
    }

    @Test
    public void testConstructReferenceType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());
        Assert.assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType atomicType = tf.constructType(AtomicReference.class);
        Assert.assertEquals(AtomicReference.class, atomicType.getRawClass());
    }

    @Test
    public void testReflectionFieldTypes() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field wildcardList = Holder.class.getField("wildcardList");
        JavaType t1 = tf.constructType(wildcardList.getGenericType());
        Assert.assertEquals(List.class, t1.getRawClass());

        Field wildcardExtends = Holder.class.getField("wildcardExtends");
        JavaType t2 = tf.constructType(wildcardExtends.getGenericType());
        Assert.assertEquals(List.class, t2.getRawClass());
        Assert.assertEquals(Number.class, t2.getContentType().getRawClass());

        Field genericArray = Holder.class.getField("genericArray");
        JavaType t3 = tf.constructType(genericArray.getGenericType());
        Assert.assertTrue(t3.isArrayType());
        Assert.assertEquals(List.class, t3.getContentType().getRawClass());

        Type methodType = Holder.class.getMethod("genericMethod", Object.class).getGenericReturnType();
        JavaType t4 = tf.constructType(methodType);
        Assert.assertEquals(Object.class, t4.getRawClass());
    }

    @Test
    public void testSelfReferencingType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType selfRef = tf.constructType(SelfRef.class);
        Assert.assertEquals(SelfRef.class, selfRef.getRawClass());

        JavaType genSelfRef = tf.constructType(GenericSelfRef.class);
        Assert.assertEquals(GenericSelfRef.class, genSelfRef.getRawClass());
    }

    @Test
    public void testSpecialParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType compType = tf.constructType(new TypeReference<Comparable<String>>() {}.getType());
        Assert.assertEquals(Comparable.class, compType.getRawClass());

        JavaType enumType = tf.constructType(new TypeReference<Enum<SimpleEnum>>() {}.getType());
        Assert.assertEquals(Enum.class, enumType.getRawClass());

        JavaType classType = tf.constructType(new TypeReference<Class<String>>() {}.getType());
        Assert.assertEquals(Class.class, classType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnrecognizedTypeThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType((Type) null);
    }
}
