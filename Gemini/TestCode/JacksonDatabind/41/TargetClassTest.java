package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TypeFactoryTest {

    static class GenericHolder<T extends Number, U extends List<T>> {
        public T valT;
        public U valU;
        public T[] arrayT;
        public List<?> wildcardList;
        public List<? extends CharSequence> wildcardExtends;
        public List<String> stringList;
        public Map<String, Integer> stringIntMap;
    }

    static class RecursiveClass<T extends RecursiveClass<T>> {
        public T selfRef;
    }

    static class CustomMapLike<K, V> {}
    static class CustomCollectionLike<E> {}

    @Test
    public void testDefaultInstanceAndCoreTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertNotNull(tf);
        Assert.assertSame(tf, TypeFactory.defaultInstance());

        JavaType objType = TypeFactory.unknownType();
        Assert.assertEquals(Object.class, objType.getRawClass());

        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
        Assert.assertEquals(List.class, TypeFactory.rawClass(new TypeReference<List<String>>() {}.getType()));
    }

    @Test
    public void testCacheAndClear() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t1 = tf.constructType(ArrayList.class);
        JavaType t2 = tf.constructType(ArrayList.class);
        Assert.assertSame(t1, t2);
        tf.clearCache();
        JavaType t3 = tf.constructType(ArrayList.class);
        Assert.assertEquals(t1, t3);
    }

    @Test
    public void testModifiersAndClassLoader() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertNull(tf.getClassLoader());

        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tfCl = tf.withClassLoader(cl);
        Assert.assertSame(cl, tfCl.getClassLoader());

        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory tfMod = tf.withModifier(null);
        Assert.assertNotNull(tfMod);

        tfMod = tf.withModifier(mod1);
        Assert.assertNotNull(tfMod);
        TypeFactory tfMod2 = tfMod.withModifier(mod1);
        Assert.assertNotNull(tfMod2);

        JavaType modified = tfMod.constructType(String.class);
        Assert.assertEquals(String.class, modified.getRawClass());
    }

    @Test
    public void testFindClass() throws ClassNotFoundException {
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

        Assert.assertEquals(String.class, tf.findClass("java.lang.String"));

        try {
            tf.findClass("non.existent.Class");
            Assert.fail();
        } catch (ClassNotFoundException e) {
            Assert.assertNotNull(e.getMessage());
        }

        TypeFactory customLoaderTf = tf.withClassLoader(getClass().getClassLoader());
        Assert.assertEquals(String.class, customLoaderTf.findClass("java.lang.String"));
    }

    @Test
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseMap = tf.constructMapType(Map.class, String.class, Integer.class);

        Assert.assertSame(baseMap, tf.constructSpecializedType(baseMap, Map.class));

        JavaType subHashMap = tf.constructSpecializedType(baseMap, HashMap.class);
        Assert.assertEquals(HashMap.class, subHashMap.getRawClass());
        Assert.assertEquals(String.class, subHashMap.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, subHashMap.getContentType().getRawClass());

        JavaType subLinked = tf.constructSpecializedType(baseMap, LinkedHashMap.class);
        Assert.assertEquals(LinkedHashMap.class, subLinked.getRawClass());

        JavaType subTree = tf.constructSpecializedType(baseMap, TreeMap.class);
        Assert.assertEquals(TreeMap.class, subTree.getRawClass());

        JavaType subEnumMap = tf.constructSpecializedType(tf.constructMapType(Map.class, Thread.State.class, String.class), EnumMap.class);
        Assert.assertEquals(EnumMap.class, subEnumMap.getRawClass());

        JavaType baseCol = tf.constructCollectionType(Collection.class, String.class);
        JavaType subArrayList = tf.constructSpecializedType(baseCol, ArrayList.class);
        Assert.assertEquals(ArrayList.class, subArrayList.getRawClass());
        Assert.assertEquals(String.class, subArrayList.getContentType().getRawClass());

        JavaType subLinkedList = tf.constructSpecializedType(baseCol, LinkedList.class);
        Assert.assertEquals(LinkedList.class, subLinkedList.getRawClass());

        JavaType subHashSet = tf.constructSpecializedType(baseCol, HashSet.class);
        Assert.assertEquals(HashSet.class, subHashSet.getRawClass());

        JavaType subTreeSet = tf.constructSpecializedType(baseCol, TreeSet.class);
        Assert.assertEquals(TreeSet.class, subTreeSet.getRawClass());

        JavaType enumSetBase = tf.constructCollectionType(EnumSet.class, Thread.State.class);
        JavaType enumSetSpec = tf.constructSpecializedType(enumSetBase, EnumSet.class);
        Assert.assertSame(enumSetBase, enumSetSpec);

        JavaType objType = tf.constructType(Object.class);
        JavaType specFromObj = tf.constructSpecializedType(objType, String.class);
        Assert.assertEquals(String.class, specFromObj.getRawClass());

        JavaType nonGeneric = tf.constructType(String.class);
        JavaType specNonGen = tf.constructSpecializedType(nonGeneric, String.class);
        Assert.assertEquals(String.class, specNonGen.getRawClass());

        JavaType ifaceBase = tf.constructType(Comparable.class);
        JavaType specString = tf.constructSpecializedType(ifaceBase, String.class);
        Assert.assertEquals(String.class, specString.getRawClass());

        JavaType classBase = tf.constructType(Number.class);
        JavaType specLong = tf.constructSpecializedType(classBase, Long.class);
        Assert.assertEquals(Long.class, specLong.getRawClass());

        try {
            tf.constructSpecializedType(baseMap, List.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("not subtype of"));
        }
    }

    @Test
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType subHashMap = tf.constructMapType(HashMap.class, String.class, Integer.class);

        Assert.assertSame(subHashMap, tf.constructGeneralizedType(subHashMap, HashMap.class));

        JavaType genMap = tf.constructGeneralizedType(subHashMap, Map.class);
        Assert.assertEquals(Map.class, genMap.getRawClass());
        Assert.assertEquals(String.class, genMap.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, genMap.getContentType().getRawClass());

        try {
            tf.constructGeneralizedType(subHashMap, List.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("not a super-type of"));
        }
    }

    @Test
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType parsed = tf.constructFromCanonical("java.util.List<java.lang.String>");
        Assert.assertEquals(List.class, parsed.getRawClass());
        Assert.assertEquals(String.class, parsed.getContentType().getRawClass());

        try {
            tf.constructFromCanonical("com.nonexisting.Type<invalid");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());
        Assert.assertEquals(Integer.class, params[1].getRawClass());

        JavaType[] noParams = tf.findTypeParameters(mapType, List.class);
        Assert.assertEquals(0, noParams.length);

        JavaType[] depParams1 = tf.findTypeParameters(HashMap.class, Map.class, TypeBindings.emptyBindings());
        Assert.assertNotNull(depParams1);

        JavaType[] depParams2 = tf.findTypeParameters(HashMap.class, Map.class);
        Assert.assertNotNull(depParams2);
    }

    @Test
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType numType = tf.constructType(Number.class);
        JavaType intType = tf.constructType(Integer.class);
        JavaType strType = tf.constructType(String.class);

        Assert.assertSame(intType, tf.moreSpecificType(null, intType));
        Assert.assertSame(numType, tf.moreSpecificType(numType, null));
        Assert.assertSame(numType, tf.moreSpecificType(numType, numType));
        Assert.assertSame(intType, tf.moreSpecificType(numType, intType));
        Assert.assertSame(intType, tf.moreSpecificType(intType, numType));
        Assert.assertSame(strType, tf.moreSpecificType(strType, numType));
    }

    @Test
    public void testDirectFactoryConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        ArrayType arrCls = tf.constructArrayType(String.class);
        Assert.assertEquals(String.class, arrCls.getContentType().getRawClass());

        ArrayType arrType = tf.constructArrayType(tf.constructType(Integer.class));
        Assert.assertEquals(Integer.class, arrType.getContentType().getRawClass());

        CollectionType col1 = tf.constructCollectionType(List.class, String.class);
        Assert.assertEquals(String.class, col1.getContentType().getRawClass());

        CollectionType col2 = tf.constructCollectionType(List.class, tf.constructType(String.class));
        Assert.assertEquals(String.class, col2.getContentType().getRawClass());

        CollectionLikeType clt1 = tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        Assert.assertEquals(String.class, clt1.getContentType().getRawClass());

        CollectionLikeType clt2 = tf.constructCollectionLikeType(CustomCollectionLike.class, tf.constructType(String.class));
        Assert.assertEquals(String.class, clt2.getContentType().getRawClass());

        MapType map1 = tf.constructMapType(Map.class, String.class, Integer.class);
        Assert.assertEquals(String.class, map1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, map1.getContentType().getRawClass());

        MapType map2 = tf.constructMapType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(String.class, map2.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, map2.getContentType().getRawClass());

        MapType propMap = tf.constructMapType(Properties.class, Object.class, Object.class);
        Assert.assertEquals(String.class, propMap.getKeyType().getRawClass());
        Assert.assertEquals(String.class, propMap.getContentType().getRawClass());

        MapLikeType mlt1 = tf.constructMapLikeType(CustomMapLike.class, String.class, Integer.class);
        Assert.assertEquals(String.class, mlt1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, mlt1.getContentType().getRawClass());

        MapLikeType mlt2 = tf.constructMapLikeType(CustomMapLike.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(String.class, mlt2.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, mlt2.getContentType().getRawClass());

        JavaType simple = tf.constructSimpleType(String.class, new JavaType[0]);
        Assert.assertEquals(String.class, simple.getRawClass());

        JavaType simpleDep = tf.constructSimpleType(String.class, String.class, new JavaType[0]);
        Assert.assertEquals(String.class, simpleDep.getRawClass());

        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());
        Assert.assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        Assert.assertEquals(String.class, unchecked.getRawClass());
    }

    @Test
    public void testParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType param1 = tf.constructParametricType(List.class, String.class);
        Assert.assertEquals(List.class, param1.getRawClass());
        Assert.assertEquals(String.class, param1.getContentType().getRawClass());

        JavaType param2 = tf.constructParametricType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, param2.getRawClass());
        Assert.assertEquals(String.class, param2.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, param2.getContentType().getRawClass());

        JavaType param3 = tf.constructParametrizedType(List.class, List.class, tf.constructType(String.class));
        Assert.assertEquals(List.class, param3.getRawClass());

        JavaType param4 = tf.constructParametrizedType(List.class, List.class, String.class);
        Assert.assertEquals(List.class, param4.getRawClass());
    }

    @Test
    public void testRawVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType rawCol = tf.constructRawCollectionType(ArrayList.class);
        Assert.assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        Assert.assertEquals(Object.class, rawColLike.getContentType().getRawClass());

        MapType rawMap = tf.constructRawMapType(HashMap.class);
        Assert.assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(CustomMapLike.class);
        Assert.assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test
    public void testReflectionAndGenericResolution() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field fValT = GenericHolder.class.getField("valT");
        JavaType jtT = tf.constructType(fValT.getGenericType());
        Assert.assertEquals(Number.class, jtT.getRawClass());

        Field fValU = GenericHolder.class.getField("valU");
        JavaType jtU = tf.constructType(fValU.getGenericType());
        Assert.assertEquals(List.class, jtU.getRawClass());

        Field fArrayT = GenericHolder.class.getField("arrayT");
        JavaType jtArrT = tf.constructType(fArrayT.getGenericType());
        Assert.assertTrue(jtArrT.isArrayType());
        Assert.assertEquals(Number.class, jtArrT.getContentType().getRawClass());

        Field fWildcard = GenericHolder.class.getField("wildcardList");
        JavaType jtWild = tf.constructType(fWildcard.getGenericType());
        Assert.assertEquals(Object.class, jtWild.getContentType().getRawClass());

        Field fWildcardExt = GenericHolder.class.getField("wildcardExtends");
        JavaType jtWildExt = tf.constructType(fWildcardExt.getGenericType());
        Assert.assertEquals(CharSequence.class, jtWildExt.getContentType().getRawClass());

        Field fStrList = GenericHolder.class.getField("stringList");
        JavaType jtStrList = tf.constructType(fStrList.getGenericType());
        Assert.assertEquals(List.class, jtStrList.getRawClass());
        Assert.assertEquals(String.class, jtStrList.getContentType().getRawClass());

        Field fStrIntMap = GenericHolder.class.getField("stringIntMap");
        JavaType jtStrIntMap = tf.constructType(fStrIntMap.getGenericType());
        Assert.assertEquals(Map.class, jtStrIntMap.getRawClass());
        Assert.assertEquals(String.class, jtStrIntMap.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, jtStrIntMap.getContentType().getRawClass());

        JavaType javaTypeInput = tf.constructType(String.class);
        Assert.assertSame(javaTypeInput, tf.constructType(javaTypeInput));

        JavaType recursiveType = tf.constructType(RecursiveClass.class);
        Assert.assertEquals(RecursiveClass.class, recursiveType.getRawClass());
    }

    @Test
    public void testDeprecatedConstructType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType jt1 = tf.constructType(String.class, String.class);
        Assert.assertEquals(String.class, jt1.getRawClass());

        JavaType jt2 = tf.constructType(String.class, tf.constructType(String.class));
        Assert.assertEquals(String.class, jt2.getRawClass());
    }

    @Test
    public void testWellKnownParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType enumType = tf.constructType(new TypeReference<Enum<Thread.State>>() {}.getType());
        Assert.assertEquals(Enum.class, enumType.getRawClass());

        JavaType compType = tf.constructType(new TypeReference<Comparable<String>>() {}.getType());
        Assert.assertEquals(Comparable.class, compType.getRawClass());

        JavaType classType = tf.constructType(new TypeReference<Class<String>>() {}.getType());
        Assert.assertEquals(Class.class, classType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnrecognizedTypeThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType((Type) null);
    }
}
