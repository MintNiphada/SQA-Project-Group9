package com.fasterxml.jackson.databind.type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryTest {

    static class GenericHolder<T> {
        public T single;
        public T[] genericArray;
        public List<T> list;
        public Map<String, T> map;
        public List<? extends Number> wildcardExtends;
        public List<? super Integer> wildcardSuper;
    }

    static class MultiHolder<K, V> {
        public Map<K, V> map;
    }

    static class BoundedHolder<T extends Comparable<T>> {
        public T bounded;
    }

    static class RecursiveClass<T> {
        public RecursiveClass<T> next;
    }

    static class SelfReferential<T extends SelfReferential<T>> {
        public T self;
    }

    static class NonGenericList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    static class SingleArgGeneric<T> extends ArrayList<T> {
        private static final long serialVersionUID = 1L;
    }

    static class DoubleArgGeneric<A, B> extends HashMap<A, B> {
        private static final long serialVersionUID = 1L;
    }

    interface CustomInterface<T> {
    }

    static class CustomInterfaceImpl<T> implements CustomInterface<T> {
    }

    enum TestEnum {
        A, B
    }

    @Test
    public void testDefaultInstanceAndCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertNotNull(tf);
        Assert.assertSame(tf, TypeFactory.defaultInstance());
        tf.clearCache();
        Assert.assertNull(tf.getClassLoader());
    }

    @Test
    public void testWithClassLoader() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tf2 = tf.withClassLoader(cl);
        Assert.assertNotSame(tf, tf2);
        Assert.assertSame(cl, tf2.getClassLoader());
        Class<?> cls = tf2.findClass("java.lang.String");
        Assert.assertEquals(String.class, cls);
    }

    @Test
    public void testWithModifier() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier modNull = null;
        TypeFactory tfNull = tf.withModifier(modNull);
        Assert.assertNotNull(tfNull);

        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithMod = tf.withModifier(mod1);
        Assert.assertNotNull(tfWithMod);

        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithBoth = tfWithMod.withModifier(mod2);
        Assert.assertNotNull(tfWithBoth);
    }

    @Test(expected = IllegalStateException.class)
    public void testModifierReturningNullThrowsException() {
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
        JavaType unk = TypeFactory.unknownType();
        Assert.assertEquals(Object.class, unk.getRawClass());
        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType jt = TypeFactory.defaultInstance().constructType(Integer.class);
        Assert.assertEquals(Integer.class, TypeFactory.rawClass(jt));
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
        Assert.assertEquals(int[].class, tf.findClass("[I"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClassNotFound() throws Exception {
        TypeFactory.defaultInstance().findClass("com.nonexistent.NoSuchClass12345");
    }

    @Test
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseList = tf.constructCollectionType(List.class, String.class);
        Assert.assertSame(baseList, tf.constructSpecializedType(baseList, List.class));

        JavaType objType = tf.constructType(Object.class);
        JavaType specializedFromObj = tf.constructSpecializedType(objType, String.class);
        Assert.assertEquals(String.class, specializedFromObj.getRawClass());

        JavaType arrayListType = tf.constructSpecializedType(baseList, ArrayList.class);
        Assert.assertEquals(ArrayList.class, arrayListType.getRawClass());
        Assert.assertEquals(String.class, arrayListType.getContentType().getRawClass());

        JavaType baseMap = tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType hashMapType = tf.constructSpecializedType(baseMap, HashMap.class);
        Assert.assertEquals(HashMap.class, hashMapType.getRawClass());
        Assert.assertEquals(String.class, hashMapType.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, hashMapType.getContentType().getRawClass());

        JavaType linkedHashMapType = tf.constructSpecializedType(baseMap, LinkedHashMap.class);
        Assert.assertEquals(LinkedHashMap.class, linkedHashMapType.getRawClass());

        JavaType treeMapType = tf.constructSpecializedType(baseMap, TreeMap.class);
        Assert.assertEquals(TreeMap.class, treeMapType.getRawClass());

        JavaType baseEnumMap = tf.constructMapType(Map.class, TestEnum.class, Integer.class);
        JavaType enumMapType = tf.constructSpecializedType(baseEnumMap, EnumMap.class);
        Assert.assertEquals(EnumMap.class, enumMapType.getRawClass());

        JavaType linkedListType = tf.constructSpecializedType(baseList, LinkedList.class);
        Assert.assertEquals(LinkedList.class, linkedListType.getRawClass());

        JavaType baseSet = tf.constructCollectionType(Set.class, String.class);
        JavaType hashSetType = tf.constructSpecializedType(baseSet, HashSet.class);
        Assert.assertEquals(HashSet.class, hashSetType.getRawClass());

        JavaType treeSetType = tf.constructSpecializedType(baseSet, TreeSet.class);
        Assert.assertEquals(TreeSet.class, treeSetType.getRawClass());

        JavaType nonGenType = tf.constructSpecializedType(baseList, NonGenericList.class);
        Assert.assertEquals(NonGenericList.class, nonGenType.getRawClass());

        JavaType rawList = tf.constructRawCollectionType(List.class);
        JavaType rawArrayList = tf.constructSpecializedType(rawList, ArrayList.class);
        Assert.assertEquals(ArrayList.class, rawArrayList.getRawClass());

        JavaType customMapType = tf.constructSpecializedType(baseMap, CustomMap.class);
        Assert.assertEquals(CustomMap.class, customMapType.getRawClass());

        JavaType customIfType = tf.constructType(CustomInterface.class);
        JavaType customImplType = tf.constructSpecializedType(customIfType, CustomInterfaceImpl.class);
        Assert.assertEquals(CustomInterfaceImpl.class, customImplType.getRawClass());

        JavaType singleType = tf.constructParametricType(SingleArgGeneric.class, String.class);
        JavaType specializedSingle = tf.constructSpecializedType(baseList, SingleArgGeneric.class);
        Assert.assertEquals(SingleArgGeneric.class, specializedSingle.getRawClass());

        JavaType doubleType = tf.constructParametricType(DoubleArgGeneric.class, String.class, Integer.class);
        JavaType specializedDouble = tf.constructSpecializedType(baseMap, DoubleArgGeneric.class);
        Assert.assertEquals(DoubleArgGeneric.class, specializedDouble.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeInvalidSubclass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructSpecializedType(listType, Set.class);
    }

    @Test
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);
        Assert.assertSame(arrayListType, tf.constructGeneralizedType(arrayListType, ArrayList.class));

        JavaType listType = tf.constructGeneralizedType(arrayListType, List.class);
        Assert.assertEquals(List.class, listType.getRawClass());
        Assert.assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType collType = tf.constructGeneralizedType(arrayListType, Collection.class);
        Assert.assertEquals(Collection.class, collType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedTypeNotSuperType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructGeneralizedType(listType, Map.class);
    }

    @Test
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType jt = tf.constructFromCanonical("java.util.List<java.lang.String>");
        Assert.assertTrue(jt.isCollectionLikeType());
        Assert.assertEquals(List.class, jt.getRawClass());
        Assert.assertEquals(String.class, jt.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        TypeFactory.defaultInstance().constructFromCanonical("unknown.package.InvalidClass<foo>");
    }

    @Test
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);
        JavaType[] params = tf.findTypeParameters(arrayListType, Collection.class);
        Assert.assertEquals(1, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());

        JavaType[] notFound = tf.findTypeParameters(arrayListType, Map.class);
        Assert.assertEquals(0, notFound.length);

        @SuppressWarnings("deprecation")
        JavaType[] depParams1 = tf.findTypeParameters(ArrayList.class, Collection.class);
        Assert.assertNotNull(depParams1);

        @SuppressWarnings("deprecation")
        JavaType[] depParams2 = tf.findTypeParameters(ArrayList.class, Collection.class, TypeBindings.emptyBindings());
        Assert.assertNotNull(depParams2);
    }

    @Test
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType objType = tf.constructType(Object.class);
        JavaType strType = tf.constructType(String.class);
        JavaType numType = tf.constructType(Number.class);
        JavaType intType = tf.constructType(Integer.class);

        Assert.assertNull(tf.moreSpecificType(null, null));
        Assert.assertSame(strType, tf.moreSpecificType(strType, null));
        Assert.assertSame(strType, tf.moreSpecificType(null, strType));
        Assert.assertSame(strType, tf.moreSpecificType(strType, strType));
        Assert.assertSame(intType, tf.moreSpecificType(numType, intType));
        Assert.assertSame(intType, tf.moreSpecificType(intType, numType));
        Assert.assertSame(strType, tf.moreSpecificType(strType, numType));
    }

    @Test
    public void testConstructTypeReferences() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
        JavaType jt = tf.constructType(ref);
        Assert.assertEquals(List.class, jt.getRawClass());
        Assert.assertEquals(String.class, jt.getContentType().getRawClass());

        @SuppressWarnings("deprecation")
        JavaType jtClassCtx = tf.constructType(String.class, (Class<?>) null);
        Assert.assertEquals(String.class, jtClassCtx.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType jtJavaTypeCtx = tf.constructType(String.class, (JavaType) null);
        Assert.assertEquals(String.class, jtJavaTypeCtx.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType jtClassCtx2 = tf.constructType(String.class, ArrayList.class);
        Assert.assertEquals(String.class, jtClassCtx2.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType jtJavaTypeCtx2 = tf.constructType(String.class, jt);
        Assert.assertEquals(String.class, jtJavaTypeCtx2.getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType at1 = tf.constructArrayType(String.class);
        Assert.assertEquals(String[].class, at1.getRawClass());
        Assert.assertEquals(String.class, at1.getContentType().getRawClass());

        ArrayType at2 = tf.constructArrayType(tf.constructType(Integer.class));
        Assert.assertEquals(Integer[].class, at2.getRawClass());
        Assert.assertEquals(Integer.class, at2.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType ct1 = tf.constructCollectionType(ArrayList.class, String.class);
        Assert.assertEquals(ArrayList.class, ct1.getRawClass());
        Assert.assertEquals(String.class, ct1.getContentType().getRawClass());

        CollectionType ct2 = tf.constructCollectionType(LinkedList.class, tf.constructType(Integer.class));
        Assert.assertEquals(LinkedList.class, ct2.getRawClass());
        Assert.assertEquals(Integer.class, ct2.getContentType().getRawClass());

        CollectionLikeType clt1 = tf.constructCollectionLikeType(List.class, Double.class);
        Assert.assertEquals(List.class, clt1.getRawClass());

        CollectionLikeType clt2 = tf.constructCollectionLikeType(String.class, tf.constructType(Character.class));
        Assert.assertEquals(String.class, clt2.getRawClass());

        CollectionType rawColl = tf.constructRawCollectionType(List.class);
        Assert.assertEquals(List.class, rawColl.getRawClass());
        Assert.assertEquals(Object.class, rawColl.getContentType().getRawClass());

        CollectionLikeType rawCollLike = tf.constructRawCollectionLikeType(List.class);
        Assert.assertEquals(List.class, rawCollLike.getRawClass());
        Assert.assertEquals(Object.class, rawCollLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType mt1 = tf.constructMapType(HashMap.class, String.class, Integer.class);
        Assert.assertEquals(HashMap.class, mt1.getRawClass());
        Assert.assertEquals(String.class, mt1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, mt1.getContentType().getRawClass());

        MapType mt2 = tf.constructMapType(Properties.class, Object.class, Object.class);
        Assert.assertEquals(Properties.class, mt2.getRawClass());
        Assert.assertEquals(String.class, mt2.getKeyType().getRawClass());
        Assert.assertEquals(String.class, mt2.getContentType().getRawClass());

        MapType mt3 = tf.constructMapType(LinkedHashMap.class, tf.constructType(String.class), tf.constructType(Long.class));
        Assert.assertEquals(LinkedHashMap.class, mt3.getRawClass());

        MapLikeType mlt1 = tf.constructMapLikeType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, mlt1.getRawClass());

        MapLikeType mlt2 = tf.constructMapLikeType(String.class, tf.constructType(Integer.class), tf.constructType(Boolean.class));
        Assert.assertEquals(String.class, mlt2.getRawClass());

        MapType rawMap = tf.constructRawMapType(Map.class);
        Assert.assertEquals(Map.class, rawMap.getRawClass());
        Assert.assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(Map.class);
        Assert.assertEquals(Map.class, rawMapLike.getRawClass());
        Assert.assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructSimpleAndParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st1 = tf.constructSimpleType(String.class, new JavaType[0]);
        Assert.assertEquals(String.class, st1.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType st2 = tf.constructSimpleType(String.class, String.class, new JavaType[0]);
        Assert.assertEquals(String.class, st2.getRawClass());

        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        Assert.assertEquals(AtomicReference.class, refType.getRawClass());
        Assert.assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType unchk = tf.uncheckedSimpleType(Integer.class);
        Assert.assertEquals(Integer.class, unchk.getRawClass());

        JavaType pt1 = tf.constructParametricType(List.class, String.class);
        Assert.assertEquals(List.class, pt1.getRawClass());

        JavaType pt2 = tf.constructParametricType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, pt2.getRawClass());

        JavaType pt3 = tf.constructParametrizedType(List.class, List.class, String.class);
        Assert.assertEquals(List.class, pt3.getRawClass());

        JavaType pt4 = tf.constructParametrizedType(Map.class, Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, pt4.getRawClass());
    }

    @Test
    public void testFromAnyReflectionTypes() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field singleField = GenericHolder.class.getField("single");
        JavaType singleType = tf.constructType(singleField.getGenericType());
        Assert.assertEquals(Object.class, singleType.getRawClass());

        Field arrayField = GenericHolder.class.getField("genericArray");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        Assert.assertTrue(arrayType.isArrayType());

        Field listField = GenericHolder.class.getField("list");
        JavaType listType = tf.constructType(listField.getGenericType());
        Assert.assertTrue(listType.isCollectionLikeType());

        Field mapField = GenericHolder.class.getField("map");
        JavaType mapType = tf.constructType(mapField.getGenericType());
        Assert.assertTrue(mapType.isMapLikeType());

        Field wildcardExtField = GenericHolder.class.getField("wildcardExtends");
        JavaType wildcardExtType = tf.constructType(wildcardExtField.getGenericType());
        Assert.assertEquals(Number.class, wildcardExtType.getContentType().getRawClass());

        Field wildcardSupField = GenericHolder.class.getField("wildcardSuper");
        JavaType wildcardSupType = tf.constructType(wildcardSupField.getGenericType());
        Assert.assertEquals(Object.class, wildcardSupType.getContentType().getRawClass());

        Field boundedField = BoundedHolder.class.getField("bounded");
        JavaType boundedType = tf.constructType(boundedField.getGenericType());
        Assert.assertEquals(Comparable.class, boundedType.getRawClass());

        Field multiMapField = MultiHolder.class.getField("map");
        JavaType multiMapType = tf.constructType(multiMapField.getGenericType());
        Assert.assertEquals(Map.class, multiMapType.getRawClass());
    }

    @Test
    public void testSpecialParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType enumType = tf.constructParametricType(Enum.class, TestEnum.class);
        Assert.assertSame(TypeFactory.CORE_TYPE_ENUM, enumType);

        JavaType compType = tf.constructParametricType(Comparable.class, String.class);
        Assert.assertSame(TypeFactory.CORE_TYPE_COMPARABLE, compType);

        JavaType classType = tf.constructParametricType(Class.class, String.class);
        Assert.assertSame(TypeFactory.CORE_TYPE_CLASS, classType);
    }

    @Test
    public void testWellKnownTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertSame(TypeFactory.CORE_TYPE_BOOL, tf.constructType(boolean.class));
        Assert.assertSame(TypeFactory.CORE_TYPE_INT, tf.constructType(int.class));
        Assert.assertSame(TypeFactory.CORE_TYPE_LONG, tf.constructType(long.class));
        Assert.assertSame(TypeFactory.CORE_TYPE_STRING, tf.constructType(String.class));
        Assert.assertSame(TypeFactory.CORE_TYPE_OBJECT, tf.constructType(Object.class));
    }

    @Test
    public void testAtomicReferenceResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(new TypeReference<AtomicReference<String>>() {});
        Assert.assertTrue(refType.isReferenceType());
        Assert.assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType rawRef = tf.constructType(AtomicReference.class);
        Assert.assertTrue(rawRef.isReferenceType());
        Assert.assertEquals(Object.class, rawRef.getContentType().getRawClass());
    }

    @Test
    public void testRecursiveTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType recType = tf.constructType(RecursiveClass.class);
        Assert.assertNotNull(recType);
        Assert.assertEquals(RecursiveClass.class, recType.getRawClass());

        JavaType selfRef = tf.constructType(SelfReferential.class);
        Assert.assertNotNull(selfRef);
        Assert.assertEquals(SelfReferential.class, selfRef.getRawClass());
    }

    @Test
    public void testPropertiesType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType propType = tf.constructType(Properties.class);
        Assert.assertTrue(propType.isMapLikeType());
        Assert.assertEquals(String.class, propType.getKeyType().getRawClass());
        Assert.assertEquals(String.class, propType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnrecognizedTypeThrows() {
        TypeFactory.defaultInstance()._fromAny(null, null, TypeBindings.emptyBindings());
    }

    @Test
    public void testClassStackSelfReferenceCycle() {
        ClassStack stack = new ClassStack(String.class);
        ClassStack child = stack.child(Integer.class);
        Assert.assertNull(child.find(Double.class));
        Assert.assertSame(stack, child.find(String.class));
        Assert.assertSame(child, child.find(Integer.class));
    }
}
