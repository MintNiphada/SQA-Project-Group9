package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TypeFactoryTest {

    static class GenericHolder<T> {
        public T value;
        public List<T> list;
        public T[] array;
    }

    static class MultiBound<T extends Number & Comparable<T>> {}
    static class Recursive<T extends Recursive<T>> {}
    static class SelfRef {
        public SelfRef child;
    }
    static class CustomList<E> extends ArrayList<E> {}
    static class CustomMap<K, V> extends HashMap<K, V> {}
    static class CustomCollectionLike<E> {}
    static class CustomMapLike<K, V> {}
    static abstract class AbstractCustomClass<T> {}
    static class ConcreteSubclass<T> extends AbstractCustomClass<T> {}

    @Test
    public void testDefaultInstanceAndCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertNotNull(tf);
        Assert.assertNull(tf.getClassLoader());

        tf.clearCache();
        TypeFactory withCache = tf.withCache(new LRUMap<Object, JavaType>(10, 50));
        Assert.assertNotNull(withCache);

        ClassLoader cl = getClass().getClassLoader();
        TypeFactory withCl = tf.withClassLoader(cl);
        Assert.assertSame(cl, withCl.getClassLoader());
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
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory tf2 = tf.withModifier(mod1);
        Assert.assertNotNull(tf2);
        TypeFactory tf3 = tf2.withModifier(mod2);
        Assert.assertNotNull(tf3);
        TypeFactory tf4 = tf3.withModifier(null);
        Assert.assertNotNull(tf4);
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
    public void testStaticUnknownTypeAndRawClass() {
        JavaType unknown = TypeFactory.unknownType();
        Assert.assertNotNull(unknown);
        Assert.assertSame(Object.class, unknown.getRawClass());

        Assert.assertSame(String.class, TypeFactory.rawClass(String.class));
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        Assert.assertSame(List.class, TypeFactory.rawClass(listType));
    }

    @Test
    public void testFindClassPrimitives() throws ClassNotFoundException {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertSame(int.class, tf.findClass("int"));
        Assert.assertSame(long.class, tf.findClass("long"));
        Assert.assertSame(float.class, tf.findClass("float"));
        Assert.assertSame(double.class, tf.findClass("double"));
        Assert.assertSame(boolean.class, tf.findClass("boolean"));
        Assert.assertSame(byte.class, tf.findClass("byte"));
        Assert.assertSame(char.class, tf.findClass("char"));
        Assert.assertSame(short.class, tf.findClass("short"));
        Assert.assertSame(void.class, tf.findClass("void"));
    }

    @Test
    public void testFindClassObjects() throws ClassNotFoundException {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertSame(String.class, tf.findClass("java.lang.String"));
        Assert.assertSame(TypeFactoryTest.class, tf.findClass(TypeFactoryTest.class.getName()));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClassNotFound() throws ClassNotFoundException {
        TypeFactory.defaultInstance().findClass("com.nonexistent.Class123");
    }

    @Test
    public void testWellKnownTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Assert.assertSame(String.class, tf.constructType(String.class).getRawClass());
        Assert.assertSame(Object.class, tf.constructType(Object.class).getRawClass());
        Assert.assertSame(int.class, tf.constructType(int.class).getRawClass());
        Assert.assertSame(long.class, tf.constructType(long.class).getRawClass());
        Assert.assertSame(boolean.class, tf.constructType(boolean.class).getRawClass());
        Assert.assertSame(Comparable.class, tf.constructType(Comparable.class).getRawClass());
        Assert.assertSame(Enum.class, tf.constructType(Enum.class).getRawClass());
        Assert.assertSame(Class.class, tf.constructType(Class.class).getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType at1 = tf.constructArrayType(String.class);
        Assert.assertSame(String[].class, at1.getRawClass());
        Assert.assertSame(String.class, at1.getContentType().getRawClass());

        JavaType intType = tf.constructType(int.class);
        ArrayType at2 = tf.constructArrayType(intType);
        Assert.assertSame(int[].class, at2.getRawClass());
        Assert.assertSame(int.class, at2.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType ct1 = tf.constructCollectionType(ArrayList.class, String.class);
        Assert.assertSame(ArrayList.class, ct1.getRawClass());
        Assert.assertSame(String.class, ct1.getContentType().getRawClass());

        CollectionType ct2 = tf.constructCollectionType(List.class, tf.constructType(Integer.class));
        Assert.assertSame(List.class, ct2.getRawClass());
        Assert.assertSame(Integer.class, ct2.getContentType().getRawClass());

        CollectionType rawCt = tf.constructRawCollectionType(ArrayList.class);
        Assert.assertSame(Object.class, rawCt.getContentType().getRawClass());

        CollectionLikeType clt1 = tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        Assert.assertSame(CustomCollectionLike.class, clt1.getRawClass());
        Assert.assertSame(String.class, clt1.getContentType().getRawClass());

        CollectionLikeType clt2 = tf.constructCollectionLikeType(CustomCollectionLike.class, tf.constructType(Integer.class));
        Assert.assertSame(Integer.class, clt2.getContentType().getRawClass());

        CollectionLikeType rawClt = tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        Assert.assertSame(Object.class, rawClt.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType mt1 = tf.constructMapType(HashMap.class, String.class, Integer.class);
        Assert.assertSame(HashMap.class, mt1.getRawClass());
        Assert.assertSame(String.class, mt1.getKeyType().getRawClass());
        Assert.assertSame(Integer.class, mt1.getContentType().getRawClass());

        MapType mt2 = tf.constructMapType(Map.class, tf.constructType(String.class), tf.constructType(Long.class));
        Assert.assertSame(String.class, mt2.getKeyType().getRawClass());
        Assert.assertSame(Long.class, mt2.getContentType().getRawClass());

        MapType propMap = tf.constructMapType(Properties.class, Object.class, Object.class);
        Assert.assertSame(String.class, propMap.getKeyType().getRawClass());
        Assert.assertSame(String.class, propMap.getContentType().getRawClass());

        MapType rawMt = tf.constructRawMapType(HashMap.class);
        Assert.assertSame(Object.class, rawMt.getKeyType().getRawClass());
        Assert.assertSame(Object.class, rawMt.getContentType().getRawClass());

        MapLikeType mlt1 = tf.constructMapLikeType(CustomMapLike.class, String.class, Integer.class);
        Assert.assertSame(CustomMapLike.class, mlt1.getRawClass());
        Assert.assertSame(String.class, mlt1.getKeyType().getRawClass());

        MapLikeType mlt2 = tf.constructMapLikeType(CustomMapLike.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertSame(Integer.class, mlt2.getContentType().getRawClass());

        MapLikeType rawMlt = tf.constructRawMapLikeType(CustomMapLike.class);
        Assert.assertSame(Object.class, rawMlt.getKeyType().getRawClass());
    }

    @Test
    public void testConstructReferenceType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        Assert.assertTrue(refType.isReferenceType());
        Assert.assertSame(String.class, refType.getContentType().getRawClass());

        JavaType directRef = tf.constructType(new TypeReference<AtomicReference<Long>>() {});
        Assert.assertTrue(directRef.isReferenceType());
        Assert.assertSame(Long.class, directRef.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType pt1 = tf.constructParametricType(GenericHolder.class, String.class);
        Assert.assertSame(GenericHolder.class, pt1.getRawClass());
        Assert.assertSame(String.class, pt1.getBindings().getBoundType(0).getRawClass());

        JavaType pt2 = tf.constructParametricType(GenericHolder.class, tf.constructType(Integer.class));
        Assert.assertSame(Integer.class, pt2.getBindings().getBoundType(0).getRawClass());

        JavaType pt3 = tf.constructParametrizedType(GenericHolder.class, GenericHolder.class, String.class);
        Assert.assertSame(GenericHolder.class, pt3.getRawClass());

        JavaType pt4 = tf.constructParametrizedType(GenericHolder.class, GenericHolder.class, tf.constructType(Double.class));
        Assert.assertSame(Double.class, pt4.getBindings().getBoundType(0).getRawClass());

        JavaType simpleParam = tf.constructSimpleType(GenericHolder.class, new JavaType[] { tf.constructType(Float.class) });
        Assert.assertSame(Float.class, simpleParam.getBindings().getBoundType(0).getRawClass());

        @SuppressWarnings("deprecation")
        JavaType simpleDep = tf.constructSimpleType(GenericHolder.class, GenericHolder.class, new JavaType[] { tf.constructType(Float.class) });
        Assert.assertSame(Float.class, simpleDep.getBindings().getBoundType(0).getRawClass());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethods() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType unchk = tf.uncheckedSimpleType(String.class);
        Assert.assertSame(String.class, unchk.getRawClass());

        JavaType t1 = tf.constructType(String.class, Object.class);
        Assert.assertSame(String.class, t1.getRawClass());

        JavaType t2 = tf.constructType(String.class, tf.constructType(Object.class));
        Assert.assertSame(String.class, t2.getRawClass());

        JavaType[] params1 = tf.findTypeParameters(ArrayList.class, List.class);
        Assert.assertEquals(1, params1.length);

        JavaType[] params2 = tf.findTypeParameters(ArrayList.class, List.class, TypeBindings.emptyBindings());
        Assert.assertEquals(1, params2.length);
    }

    @Test
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        Assert.assertEquals(2, params.length);
        Assert.assertSame(String.class, params[0].getRawClass());
        Assert.assertSame(Integer.class, params[1].getRawClass());

        JavaType[] noParams = tf.findTypeParameters(mapType, List.class);
        Assert.assertEquals(0, noParams.length);
    }

    @Test
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType str = tf.constructType(String.class);
        JavaType obj = tf.constructType(Object.class);
        JavaType num = tf.constructType(Number.class);

        Assert.assertSame(str, tf.moreSpecificType(null, str));
        Assert.assertSame(str, tf.moreSpecificType(str, null));
        Assert.assertSame(str, tf.moreSpecificType(str, str));
        Assert.assertSame(str, tf.moreSpecificType(obj, str));
        Assert.assertSame(str, tf.moreSpecificType(str, obj));
        Assert.assertSame(str, tf.moreSpecificType(str, num));
    }

    @Test
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructFromCanonical("java.util.List<java.lang.String>");
        Assert.assertSame(List.class, t.getRawClass());
        Assert.assertSame(String.class, t.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        TypeFactory.defaultInstance().constructFromCanonical("java.util.List<");
    }

    @Test
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructMapType(Map.class, String.class, Integer.class);

        JavaType specializedSame = tf.constructSpecializedType(mapType, Map.class);
        Assert.assertSame(mapType, specializedSame);

        JavaType specializedHashMap = tf.constructSpecializedType(mapType, HashMap.class);
        Assert.assertSame(HashMap.class, specializedHashMap.getRawClass());
        Assert.assertSame(String.class, specializedHashMap.getKeyType().getRawClass());
        Assert.assertSame(Integer.class, specializedHashMap.getContentType().getRawClass());

        JavaType specializedLinkedMap = tf.constructSpecializedType(mapType, LinkedHashMap.class);
        Assert.assertSame(LinkedHashMap.class, specializedLinkedMap.getRawClass());

        JavaType specializedTreeMap = tf.constructSpecializedType(mapType, TreeMap.class);
        Assert.assertSame(TreeMap.class, specializedTreeMap.getRawClass());

        JavaType specializedEnumMap = tf.constructSpecializedType(mapType, EnumMap.class);
        Assert.assertSame(EnumMap.class, specializedEnumMap.getRawClass());

        JavaType collType = tf.constructCollectionType(List.class, String.class);
        JavaType specializedArrayList = tf.constructSpecializedType(collType, ArrayList.class);
        Assert.assertSame(ArrayList.class, specializedArrayList.getRawClass());

        JavaType specializedLinkedList = tf.constructSpecializedType(collType, LinkedList.class);
        Assert.assertSame(LinkedList.class, specializedLinkedList.getRawClass());

        JavaType setType = tf.constructCollectionType(Set.class, String.class);
        JavaType specializedHashSet = tf.constructSpecializedType(setType, HashSet.class);
        Assert.assertSame(HashSet.class, specializedHashSet.getRawClass());

        JavaType specializedTreeSet = tf.constructSpecializedType(setType, TreeSet.class);
        Assert.assertSame(TreeSet.class, specializedTreeSet.getRawClass());

        JavaType rawObj = tf.constructType(Object.class);
        JavaType specializedFromObj = tf.constructSpecializedType(rawObj, String.class);
        Assert.assertSame(String.class, specializedFromObj.getRawClass());

        JavaType unparam = tf.constructType(String.class);
        JavaType specializedUnparam = tf.constructSpecializedType(unparam, String.class);
        Assert.assertSame(String.class, specializedUnparam.getRawClass());

        JavaType customBase = tf.constructParametricType(AbstractCustomClass.class, String.class);
        JavaType specializedCustom = tf.constructSpecializedType(customBase, ConcreteSubclass.class);
        Assert.assertSame(ConcreteSubclass.class, specializedCustom.getRawClass());
        Assert.assertSame(String.class, specializedCustom.getBindings().getBoundType(0).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeInvalid() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructSpecializedType(listType, Set.class);
    }

    @Test
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);

        JavaType generalizedSame = tf.constructGeneralizedType(arrayListType, ArrayList.class);
        Assert.assertSame(arrayListType, generalizedSame);

        JavaType generalizedList = tf.constructGeneralizedType(arrayListType, List.class);
        Assert.assertSame(List.class, generalizedList.getRawClass());
        Assert.assertSame(String.class, generalizedList.getContentType().getRawClass());

        JavaType generalizedColl = tf.constructGeneralizedType(arrayListType, Collection.class);
        Assert.assertSame(Collection.class, generalizedColl.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedTypeInvalid() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructGeneralizedType(listType, Set.class);
    }

    @Test
    public void testGenericFieldResolutions() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field listField = GenericHolder.class.getField("list");
        JavaType boundHolder = tf.constructParametricType(GenericHolder.class, String.class);
        JavaType listType = tf.constructType(listField.getGenericType(), boundHolder);
        Assert.assertSame(List.class, listType.getRawClass());
        Assert.assertSame(String.class, listType.getContentType().getRawClass());

        Field arrayField = GenericHolder.class.getField("array");
        JavaType arrayType = tf.constructType(arrayField.getGenericType(), boundHolder);
        Assert.assertSame(String[].class, arrayType.getRawClass());

        Field valueField = GenericHolder.class.getField("value");
        JavaType valType = tf.constructType(valueField.getGenericType(), boundHolder);
        Assert.assertSame(String.class, valType.getRawClass());
    }

    @Test
    public void testRecursiveTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType selfRef = tf.constructType(SelfRef.class);
        Assert.assertSame(SelfRef.class, selfRef.getRawClass());

        JavaType recursiveParam = tf.constructType(Recursive.class);
        Assert.assertSame(Recursive.class, recursiveParam.getRawClass());
    }

    @Test
    public void testWildcardAndUnboundResolutions() throws Exception {
        class WildcardTestClass {
            public List<?> wildcardList;
            public List<? extends Number> numberList;
        }

        TypeFactory tf = TypeFactory.defaultInstance();
        Field wildcardField = WildcardTestClass.class.getField("wildcardList");
        JavaType wType = tf.constructType(wildcardField.getGenericType());
        Assert.assertSame(Object.class, wType.getContentType().getRawClass());

        Field numberField = WildcardTestClass.class.getField("numberList");
        JavaType nType = tf.constructType(numberField.getGenericType());
        Assert.assertSame(Number.class, nType.getContentType().getRawClass());

        JavaType multiBound = tf.constructType(MultiBound.class);
        Assert.assertSame(MultiBound.class, multiBound.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTypeException() {
        TypeFactory.defaultInstance().constructType((Type) null);
    }

    @Test
    public void testPropertiesTypeConstruct() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType propType = tf.constructType(Properties.class);
        Assert.assertSame(Properties.class, propType.getRawClass());
        Assert.assertSame(String.class, propType.getKeyType().getRawClass());
        Assert.assertSame(String.class, propType.getContentType().getRawClass());
    }
}
