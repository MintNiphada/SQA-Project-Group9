package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.After;
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
import java.util.*;

public class TypeFactoryTest {

    private TypeFactory tf;

    public enum TestEnum {
        A, B
    }

    public static class CustomList<E> extends ArrayList<E> {}
    public static class CustomMap<K, V> extends HashMap<K, V> {}
    public static class StringIntMap extends HashMap<String, Integer> {}
    public static class RawMap extends HashMap {}
    public static class RawCollection extends ArrayList {}

    public static class GenericClass<T, U> {
        public T fieldT;
        public U fieldU;
        public List<T>[] genericArrayField;
        public List<? extends Number> wildcardField;
        public List<?> unboundWildcardField;
        public <C extends Comparable<C>> C recursiveMethod(C param) { return param; }
    }

    public static class SubGenericClass extends GenericClass<String, Long> {}

    public static class CustomEntry<K, V> implements Map.Entry<K, V> {
        private K k;
        private V v;
        @Override public K getKey() { return k; }
        @Override public V getValue() { return v; }
        @Override public V setValue(V value) { this.v = value; return value; }
    }

    public static class RawEntry implements Map.Entry {
        @Override public Object getKey() { return null; }
        @Override public Object getValue() { return null; }
        @Override public Object setValue(Object value) { return null; }
    }

    public static class IntermediateEntry<V> implements Map.Entry<String, V> {
        @Override public String getKey() { return null; }
        @Override public V getValue() { return null; }
        @Override public V setValue(V value) { return null; }
    }

    public static class FinalEntry extends IntermediateEntry<Integer> {}

    public interface MyInterface<T> {}
    public static class MyBase<T> implements MyInterface<T> {}
    public static class MySub extends MyBase<String> {}
    public static class NonGenericBase {}

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
        tf.clearCache();
    }

    @After
    public void tearDown() {
        tf.clearCache();
    }

    @Test
    public void testCoreAndBasicTypes() {
        JavaType strType = tf.constructType(String.class);
        Assert.assertEquals(String.class, strType.getRawClass());
        Assert.assertSame(TypeFactory.CORE_TYPE_STRING, strType);

        JavaType boolType = tf.constructType(Boolean.TYPE);
        Assert.assertEquals(Boolean.TYPE, boolType.getRawClass());
        Assert.assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType intType = tf.constructType(Integer.TYPE);
        Assert.assertEquals(Integer.TYPE, intType.getRawClass());
        Assert.assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType longType = tf.constructType(Long.TYPE);
        Assert.assertEquals(Long.TYPE, longType.getRawClass());
        Assert.assertSame(TypeFactory.CORE_TYPE_LONG, longType);
    }

    @Test
    public void testUnknownTypeAndRawClass() {
        JavaType unknown = TypeFactory.unknownType();
        Assert.assertNotNull(unknown);
        Assert.assertEquals(Object.class, unknown.getRawClass());

        Assert.assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType jt = tf.constructType(Integer.class);
        Assert.assertEquals(Integer.class, TypeFactory.rawClass(jt));
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

        TypeFactory modifiedTf = tf.withModifier(null);
        Assert.assertNotNull(modifiedTf);

        modifiedTf = tf.withModifier(mod1);
        JavaType res = modifiedTf.constructType(Integer.class);
        Assert.assertEquals(Long.class, res.getRawClass());

        // Test chaining modifiers
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == Long.class) {
                    return typeFactory.constructType(Double.class);
                }
                return type;
            }
        };

        TypeFactory chainedTf = modifiedTf.withModifier(mod2);
        JavaType res2 = chainedTf.constructType(Integer.class);
        Assert.assertEquals(Double.class, res2.getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType baseType = tf.constructType(Map.class);
        JavaType specialized = tf.constructSpecializedType(baseType, Map.class);
        Assert.assertSame(baseType, specialized);

        JavaType mapType = tf.constructType(Map.class);
        JavaType hashMapType = tf.constructSpecializedType(mapType, HashMap.class);
        Assert.assertEquals(HashMap.class, hashMapType.getRawClass());

        JavaType colType = tf.constructType(Collection.class);
        JavaType listType = tf.constructSpecializedType(colType, ArrayList.class);
        Assert.assertEquals(ArrayList.class, listType.getRawClass());

        JavaType objType = tf.constructType(Object.class);
        JavaType arrType = tf.constructSpecializedType(objType, String[].class);
        Assert.assertEquals(String[].class, arrType.getRawClass());

        // Test handlers preserved
        JavaType withHandlers = objType.withValueHandler("valH").withTypeHandler("typeH");
        JavaType specWithHandlers = tf.constructSpecializedType(withHandlers, ArrayList.class);
        Assert.assertEquals("valH", specWithHandlers.getValueHandler());
        Assert.assertEquals("typeH", specWithHandlers.getTypeHandler());

        // Non-SimpleType narrowing
        JavaType listGeneric = tf.constructCollectionType(List.class, String.class);
        JavaType specializedList = tf.constructSpecializedType(listGeneric, ArrayList.class);
        Assert.assertEquals(ArrayList.class, specializedList.getRawClass());
        Assert.assertEquals(String.class, specializedList.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeIncompatible() {
        JavaType stringType = tf.constructType(String.class);
        tf.constructSpecializedType(stringType, ArrayList.class);
    }

    @Test
    public void testConstructFromCanonical() {
        JavaType type = tf.constructFromCanonical("java.util.List<java.lang.String>");
        Assert.assertNotNull(type);
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        tf.constructFromCanonical("com.nonexisting.ClassDoesNotExist");
    }

    @Test
    public void testMoreSpecificType() {
        JavaType stringType = tf.constructType(String.class);
        JavaType objType = tf.constructType(Object.class);
        JavaType numType = tf.constructType(Number.class);
        JavaType intType = tf.constructType(Integer.class);

        Assert.assertSame(stringType, tf.moreSpecificType(null, stringType));
        Assert.assertSame(stringType, tf.moreSpecificType(stringType, null));
        Assert.assertSame(stringType, tf.moreSpecificType(stringType, stringType));

        Assert.assertSame(intType, tf.moreSpecificType(numType, intType));
        Assert.assertSame(intType, tf.moreSpecificType(intType, numType));

        Assert.assertSame(stringType, tf.moreSpecificType(stringType, numType));
    }

    @Test
    public void testFindTypeParameters() {
        JavaType strIntMapType = tf.constructType(StringIntMap.class);
        JavaType[] params = tf.findTypeParameters(strIntMapType, Map.class);
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(String.class, params[0].getRawClass());
        Assert.assertEquals(Integer.class, params[1].getRawClass());

        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] mapParams = tf.findTypeParameters(mapType, HashMap.class);
        Assert.assertNotNull(mapParams);
        Assert.assertEquals(2, mapParams.length);
        Assert.assertEquals(String.class, mapParams[0].getRawClass());
        Assert.assertEquals(Integer.class, mapParams[1].getRawClass());

        JavaType simpleType = tf.constructType(String.class);
        JavaType[] emptyParams = tf.findTypeParameters(simpleType, String.class);
        Assert.assertNull(emptyParams);

        JavaType[] subParams = tf.findTypeParameters(SubGenericClass.class, GenericClass.class);
        Assert.assertNotNull(subParams);
        Assert.assertEquals(2, subParams.length);
        Assert.assertEquals(String.class, subParams[0].getRawClass());
        Assert.assertEquals(Long.class, subParams[1].getRawClass());

        JavaType[] nonGenParams = tf.findTypeParameters(NonGenericBase.class, Object.class);
        Assert.assertNull(nonGenParams);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNotSubtype() {
        tf.findTypeParameters(String.class, List.class);
    }

    @Test
    public void testConstructTypeVariants() throws Exception {
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
        JavaType fromRef = tf.constructType(ref);
        Assert.assertEquals(List.class, fromRef.getRawClass());
        Assert.assertEquals(String.class, fromRef.getContentType().getRawClass());

        JavaType fromContextClass = tf.constructType(List.class, (Class<?>) null);
        Assert.assertEquals(List.class, fromContextClass.getRawClass());

        JavaType fromContextType = tf.constructType(List.class, (JavaType) null);
        Assert.assertEquals(List.class, fromContextType.getRawClass());

        JavaType directJt = tf.constructType(fromRef);
        Assert.assertSame(fromRef, directJt);

        Field arrayField = GenericClass.class.getField("genericArrayField");
        JavaType arrayType = tf.constructType(arrayField.getGenericType(), new TypeBindings(tf, SubGenericClass.class));
        Assert.assertTrue(arrayType.isArrayType());

        Field wildcardField = GenericClass.class.getField("wildcardField");
        JavaType wildcardType = tf.constructType(wildcardField.getGenericType(), new TypeBindings(tf, SubGenericClass.class));
        Assert.assertEquals(List.class, wildcardType.getRawClass());

        Field unboundWildcard = GenericClass.class.getField("unboundWildcardField");
        JavaType unboundType = tf.constructType(unboundWildcard.getGenericType(), new TypeBindings(tf, SubGenericClass.class));
        Assert.assertEquals(List.class, unboundType.getRawClass());

        Method recMethod = GenericClass.class.getMethod("recursiveMethod", Comparable.class);
        JavaType recType = tf.constructType(recMethod.getGenericReturnType(), new TypeBindings(tf, GenericClass.class));
        Assert.assertNotNull(recType);

        TypeVariable<?> tv = GenericClass.class.getTypeParameters()[0];
        JavaType unboundVar = tf.constructType(tv);
        Assert.assertEquals(Object.class, unboundVar.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeNull() {
        tf.constructType((Type) null);
    }

    @Test
    public void testDirectFactoryMethods() {
        ArrayType at1 = tf.constructArrayType(String.class);
        Assert.assertEquals(String[].class, at1.getRawClass());

        ArrayType at2 = tf.constructArrayType(tf.constructType(Integer.class));
        Assert.assertEquals(Integer[].class, at2.getRawClass());

        CollectionType ct1 = tf.constructCollectionType(List.class, String.class);
        Assert.assertEquals(List.class, ct1.getRawClass());
        Assert.assertEquals(String.class, ct1.getContentType().getRawClass());

        CollectionType ct2 = tf.constructCollectionType(List.class, tf.constructType(String.class));
        Assert.assertEquals(List.class, ct2.getRawClass());

        CollectionLikeType clt1 = tf.constructCollectionLikeType(List.class, String.class);
        Assert.assertEquals(List.class, clt1.getRawClass());

        CollectionLikeType clt2 = tf.constructCollectionLikeType(List.class, tf.constructType(String.class));
        Assert.assertEquals(List.class, clt2.getRawClass());

        MapType mt1 = tf.constructMapType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, mt1.getRawClass());
        Assert.assertEquals(String.class, mt1.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, mt1.getContentType().getRawClass());

        MapType mt2 = tf.constructMapType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, mt2.getRawClass());

        MapLikeType mlt1 = tf.constructMapLikeType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, mlt1.getRawClass());

        MapLikeType mlt2 = tf.constructMapLikeType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        Assert.assertEquals(Map.class, mlt2.getRawClass());

        JavaType st = tf.constructSimpleType(GenericClass.class, new JavaType[]{tf.constructType(String.class), tf.constructType(Integer.class)});
        Assert.assertEquals(GenericClass.class, st.getRawClass());
        Assert.assertEquals(2, st.containedTypeCount());

        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        Assert.assertEquals(String.class, unchecked.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSimpleTypeMismatch() {
        tf.constructSimpleType(GenericClass.class, GenericClass.class, new JavaType[]{tf.constructType(String.class)});
    }

    @Test
    public void testParametricTypes() {
        JavaType pt1 = tf.constructParametricType(List.class, String.class);
        Assert.assertEquals(List.class, pt1.getRawClass());
        Assert.assertEquals(String.class, pt1.getContentType().getRawClass());

        JavaType pt2 = tf.constructParametricType(Map.class, String.class, Integer.class);
        Assert.assertEquals(Map.class, pt2.getRawClass());

        JavaType pt3 = tf.constructParametricType(String[].class, String.class);
        Assert.assertTrue(pt3.isArrayType());

        JavaType pt4 = tf.constructParametrizedType(GenericClass.class, GenericClass.class, String.class, Integer.class);
        Assert.assertEquals(GenericClass.class, pt4.getRawClass());
        Assert.assertEquals(String.class, pt4.containedType(0).getRawClass());
        Assert.assertEquals(Integer.class, pt4.containedType(1).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParametricTypeArrayMismatch() {
        tf.constructParametricType(String[].class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParametricTypeMapMismatch() {
        tf.constructParametricType(Map.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParametricTypeCollectionMismatch() {
        tf.constructParametricType(List.class, String.class, Integer.class);
    }

    @Test
    public void testRawTypes() {
        CollectionType rct = tf.constructRawCollectionType(ArrayList.class);
        Assert.assertEquals(ArrayList.class, rct.getRawClass());
        Assert.assertEquals(Object.class, rct.getContentType().getRawClass());

        CollectionLikeType rclt = tf.constructRawCollectionLikeType(ArrayList.class);
        Assert.assertEquals(ArrayList.class, rclt.getRawClass());
        Assert.assertEquals(Object.class, rclt.getContentType().getRawClass());

        MapType rmt = tf.constructRawMapType(HashMap.class);
        Assert.assertEquals(HashMap.class, rmt.getRawClass());
        Assert.assertEquals(Object.class, rmt.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rmt.getContentType().getRawClass());

        MapLikeType rmlt = tf.constructRawMapLikeType(HashMap.class);
        Assert.assertEquals(HashMap.class, rmlt.getRawClass());
        Assert.assertEquals(Object.class, rmlt.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, rmlt.getContentType().getRawClass());
    }

    @Test
    public void testFromClassBranches() {
        JavaType enumType = tf.constructType(TestEnum.class);
        Assert.assertTrue(enumType.isEnumType());

        JavaType rawMapType = tf.constructType(RawMap.class);
        Assert.assertTrue(rawMapType.isMapLikeType());
        Assert.assertEquals(Object.class, rawMapType.getKeyType().getRawClass());

        JavaType rawColType = tf.constructType(RawCollection.class);
        Assert.assertTrue(rawColType.isCollectionLikeType());
        Assert.assertEquals(Object.class, rawColType.getContentType().getRawClass());

        JavaType entryType = tf.constructType(CustomEntry.class);
        Assert.assertEquals(CustomEntry.class, entryType.getRawClass());

        JavaType rawEntryType = tf.constructType(RawEntry.class);
        Assert.assertEquals(RawEntry.class, rawEntryType.getRawClass());
        Assert.assertEquals(Object.class, rawEntryType.containedType(0).getRawClass());
        Assert.assertEquals(Object.class, rawEntryType.containedType(1).getRawClass());

        JavaType finalEntryType = tf.constructType(FinalEntry.class);
        Assert.assertEquals(FinalEntry.class, finalEntryType.getRawClass());
        Assert.assertEquals(String.class, finalEntryType.containedType(0).getRawClass());
        Assert.assertEquals(Integer.class, finalEntryType.containedType(1).getRawClass());
    }

    @Test
    public void testFromParameterizedClass() {
        JavaType arr = tf._fromParameterizedClass(String[].class, Collections.<JavaType>emptyList());
        Assert.assertTrue(arr.isArrayType());

        JavaType en = tf._fromParameterizedClass(TestEnum.class, Collections.<JavaType>emptyList());
        Assert.assertTrue(en.isEnumType());

        JavaType map0 = tf._fromParameterizedClass(HashMap.class, Collections.<JavaType>emptyList());
        Assert.assertTrue(map0.isMapLikeType());

        JavaType map1 = tf._fromParameterizedClass(HashMap.class, Collections.singletonList(tf.constructType(String.class)));
        Assert.assertTrue(map1.isMapLikeType());
        Assert.assertEquals(String.class, map1.getKeyType().getRawClass());
        Assert.assertEquals(Object.class, map1.getContentType().getRawClass());

        JavaType map2 = tf._fromParameterizedClass(HashMap.class, Arrays.asList(tf.constructType(String.class), tf.constructType(Integer.class)));
        Assert.assertTrue(map2.isMapLikeType());
        Assert.assertEquals(String.class, map2.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, map2.getContentType().getRawClass());

        JavaType col0 = tf._fromParameterizedClass(ArrayList.class, Collections.<JavaType>emptyList());
        Assert.assertTrue(col0.isCollectionLikeType());

        JavaType col1 = tf._fromParameterizedClass(ArrayList.class, Collections.singletonList(tf.constructType(String.class)));
        Assert.assertTrue(col1.isCollectionLikeType());
        Assert.assertEquals(String.class, col1.getContentType().getRawClass());

        JavaType simple0 = tf._fromParameterizedClass(NonGenericBase.class, Collections.<JavaType>emptyList());
        Assert.assertFalse(simple0.isContainerType());

        JavaType simple2 = tf._fromParameterizedClass(GenericClass.class, Arrays.asList(tf.constructType(String.class), tf.constructType(Integer.class)));
        Assert.assertEquals(2, simple2.containedTypeCount());
    }

    @Test
    public void testResolveVariableViaSubTypes() {
        HierarchicType sub = tf._findSuperTypeChain(SubGenericClass.class, GenericClass.class);
        HierarchicType base = sub;
        while (base.getSuperType() != null) {
            base = base.getSuperType();
        }
        JavaType resT = tf._resolveVariableViaSubTypes(sub, "T", new TypeBindings(tf, SubGenericClass.class));
        Assert.assertEquals(String.class, resT.getRawClass());

        JavaType resUnknown = tf._resolveVariableViaSubTypes(null, "UNKNOWN", new TypeBindings(tf, SubGenericClass.class));
        Assert.assertEquals(Object.class, resUnknown.getRawClass());
    }

    @Test
    public void testHierarchyChainCaching() {
        HierarchicType htMap1 = tf._findSuperTypeChain(HashMap.class, Map.class);
        HierarchicType htMap2 = tf._findSuperTypeChain(HashMap.class, Map.class);
        Assert.assertNotNull(htMap1);
        Assert.assertNotNull(htMap2);

        HierarchicType htList1 = tf._findSuperTypeChain(ArrayList.class, List.class);
        HierarchicType htList2 = tf._findSuperTypeChain(ArrayList.class, List.class);
        Assert.assertNotNull(htList1);
        Assert.assertNotNull(htList2);
    }
}
