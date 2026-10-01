package org.apache.commons.lang3.reflect;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TypeUtilsTest<T extends Number & Comparable<T>> {

    public List<String> listString;
    public List<Number> listNumber;
    public List<? extends Number> listExtendsNumber;
    public List<? extends Integer> listExtendsInteger;
    public List<? super Number> listSuperNumber;
    public List<? super Integer> listSuperInteger;
    public List<?> listWildcard;
    public List<String>[] listStringArray;
    public T[] typeVarArray;
    public T typeVar;
    public Comparable<T> compT;
    public String[] stringArray;
    public int[] intArray;
    public int primitiveInt;
    public Map<String, Integer> mapStringInteger;

    public static class AAA<K, V> {}
    public static class BBB<K, V> extends AAA<K, V> {}
    public static class CCC<V> extends BBB<String, V> {}
    public static class DDD extends CCC<Integer> {}

    public static class Outer<K, V> {
        public class Inner<T> {
            public class DeepInner<E> {}
        }
    }

    public Outer<String, Integer>.Inner<Long>.DeepInner<Double> deepInnerField;

    public interface InterfaceA<T> {}
    public interface InterfaceB<T> extends InterfaceA<T> {}
    public static class ClassImpl implements InterfaceB<String> {}

    public static class BoundedClass<S extends Number, U extends Comparable<U>> {}

    private Type getFieldType(String name) throws Exception {
        return getClass().getField(name).getGenericType();
    }

    @Test
    public void testConstructor() {
        TypeUtils utils = new TypeUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testIsArrayTypeAndGetArrayComponentType() throws Exception {
        Type strArr = getFieldType("stringArray");
        Type intArr = getFieldType("intArray");
        Type genArr = getFieldType("listStringArray");
        Type tvArr = getFieldType("typeVarArray");
        Type strList = getFieldType("listString");

        Assert.assertTrue(TypeUtils.isArrayType(strArr));
        Assert.assertTrue(TypeUtils.isArrayType(intArr));
        Assert.assertTrue(TypeUtils.isArrayType(genArr));
        Assert.assertTrue(TypeUtils.isArrayType(tvArr));
        Assert.assertFalse(TypeUtils.isArrayType(strList));
        Assert.assertFalse(TypeUtils.isArrayType(null));

        Assert.assertEquals(String.class, TypeUtils.getArrayComponentType(strArr));
        Assert.assertEquals(int.class, TypeUtils.getArrayComponentType(intArr));
        Assert.assertEquals(getFieldType("listString"), TypeUtils.getArrayComponentType(genArr));
        Assert.assertEquals(getFieldType("typeVar"), TypeUtils.getArrayComponentType(tvArr));
        Assert.assertNull(TypeUtils.getArrayComponentType(strList));
        Assert.assertNull(TypeUtils.getArrayComponentType(null));
    }

    @Test
    public void testIsAssignableNullAndClass() throws Exception {
        Assert.assertTrue(TypeUtils.isAssignable(null, Object.class));
        Assert.assertTrue(TypeUtils.isAssignable(null, (Type) null));
        Assert.assertFalse(TypeUtils.isAssignable(null, int.class));
        Assert.assertFalse(TypeUtils.isAssignable(String.class, (Class<?>) null));

        Assert.assertTrue(TypeUtils.isAssignable(String.class, String.class));
        Assert.assertTrue(TypeUtils.isAssignable(String.class, Object.class));
        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, Number.class));
        Assert.assertFalse(TypeUtils.isAssignable(Number.class, Integer.class));
    }

    @Test
    public void testIsAssignableParameterizedTypeToClass() throws Exception {
        Type strList = getFieldType("listString");
        Assert.assertTrue(TypeUtils.isAssignable(strList, List.class));
        Assert.assertTrue(TypeUtils.isAssignable(strList, Collection.class));
        Assert.assertTrue(TypeUtils.isAssignable(strList, Object.class));
        Assert.assertFalse(TypeUtils.isAssignable(strList, Set.class));
    }

    @Test
    public void testIsAssignableTypeVariableToClass() throws Exception {
        Type tv = getFieldType("typeVar");
        Assert.assertTrue(TypeUtils.isAssignable(tv, Number.class));
        Assert.assertTrue(TypeUtils.isAssignable(tv, Comparable.class));
        Assert.assertTrue(TypeUtils.isAssignable(tv, Object.class));
        Assert.assertFalse(TypeUtils.isAssignable(tv, String.class));
    }

    @Test
    public void testIsAssignableGenericArrayTypeToClass() throws Exception {
        Type genArr = getFieldType("listStringArray");
        Assert.assertTrue(TypeUtils.isAssignable(genArr, Object.class));
        Assert.assertTrue(TypeUtils.isAssignable(genArr, Object[].class));
        Assert.assertTrue(TypeUtils.isAssignable(genArr, List[].class));
        Assert.assertFalse(TypeUtils.isAssignable(genArr, String[].class));
        Assert.assertFalse(TypeUtils.isAssignable(genArr, List.class));
    }

    @Test
    public void testIsAssignableWildcardToClass() throws Exception {
        Type wild = ((ParameterizedType) getFieldType("listExtendsNumber")).getActualTypeArguments()[0];
        Assert.assertFalse(TypeUtils.isAssignable(wild, Number.class));
        Assert.assertFalse(TypeUtils.isAssignable(wild, Object.class));
    }

    @Test
    public void testIsAssignableParameterizedToParameterized() throws Exception {
        Type strList = getFieldType("listString");
        Type numList = getFieldType("listNumber");
        Type extNumList = getFieldType("listExtendsNumber");
        Type extIntList = getFieldType("listExtendsInteger");
        Type supNumList = getFieldType("listSuperNumber");
        Type supIntList = getFieldType("listSuperInteger");
        Type wildList = getFieldType("listWildcard");

        Assert.assertTrue(TypeUtils.isAssignable(null, strList));
        Assert.assertFalse(TypeUtils.isAssignable(strList, (ParameterizedType) null));
        Assert.assertTrue(TypeUtils.isAssignable(strList, strList));
        Assert.assertFalse(TypeUtils.isAssignable(strList, numList));

        Assert.assertTrue(TypeUtils.isAssignable(extIntList, extNumList));
        Assert.assertFalse(TypeUtils.isAssignable(extNumList, extIntList));

        Assert.assertTrue(TypeUtils.isAssignable(supNumList, supIntList));
        Assert.assertFalse(TypeUtils.isAssignable(supIntList, supNumList));

        Assert.assertTrue(TypeUtils.isAssignable(strList, wildList));
        Assert.assertTrue(TypeUtils.isAssignable(numList, wildList));

        Assert.assertTrue(TypeUtils.isAssignable(ArrayList.class, wildList));
        Assert.assertFalse(TypeUtils.isAssignable(Set.class, wildList));
    }

    @Test
    public void testIsAssignableGenericArray() throws Exception {
        Type genArr = getFieldType("listStringArray");
        Type strArr = getFieldType("stringArray");
        Type strList = getFieldType("listString");
        Type tvArr = getFieldType("typeVarArray");

        Assert.assertTrue(TypeUtils.isAssignable(null, genArr));
        Assert.assertFalse(TypeUtils.isAssignable(genArr, (GenericArrayType) null));
        Assert.assertTrue(TypeUtils.isAssignable(genArr, genArr));
        Assert.assertFalse(TypeUtils.isAssignable(strArr, genArr));
        Assert.assertFalse(TypeUtils.isAssignable(strList, genArr));
    }

    @Test
    public void testIsAssignableWildcardType() throws Exception {
        WildcardType extNum = (WildcardType) ((ParameterizedType) getFieldType("listExtendsNumber")).getActualTypeArguments()[0];
        WildcardType extInt = (WildcardType) ((ParameterizedType) getFieldType("listExtendsInteger")).getActualTypeArguments()[0];
        WildcardType supNum = (WildcardType) ((ParameterizedType) getFieldType("listSuperNumber")).getActualTypeArguments()[0];
        WildcardType supInt = (WildcardType) ((ParameterizedType) getFieldType("listSuperInteger")).getActualTypeArguments()[0];

        Assert.assertTrue(TypeUtils.isAssignable(null, extNum));
        Assert.assertFalse(TypeUtils.isAssignable(extNum, (WildcardType) null));
        Assert.assertTrue(TypeUtils.isAssignable(extNum, extNum));

        Assert.assertTrue(TypeUtils.isAssignable(extInt, extNum));
        Assert.assertFalse(TypeUtils.isAssignable(extNum, extInt));

        Assert.assertTrue(TypeUtils.isAssignable(supNum, supInt));
        Assert.assertFalse(TypeUtils.isAssignable(supInt, supNum));

        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, extNum));
        Assert.assertFalse(TypeUtils.isAssignable(String.class, extNum));
        Assert.assertTrue(TypeUtils.isAssignable(Object.class, supNum));
        Assert.assertFalse(TypeUtils.isAssignable(Integer.class, supNum));
    }

    @Test
    public void testIsAssignableTypeVariable() throws Exception {
        TypeVariable<?> tv = (TypeVariable<?>) getFieldType("typeVar");

        Assert.assertTrue(TypeUtils.isAssignable(null, tv));
        Assert.assertFalse(TypeUtils.isAssignable(tv, (TypeVariable<?>) null));
        Assert.assertTrue(TypeUtils.isAssignable(tv, tv));
        Assert.assertFalse(TypeUtils.isAssignable(String.class, tv));
        Assert.assertFalse(TypeUtils.isAssignable(getFieldType("listString"), tv));
        Assert.assertFalse(TypeUtils.isAssignable(getFieldType("listStringArray"), tv));
        Type wild = ((ParameterizedType) getFieldType("listWildcard")).getActualTypeArguments()[0];
        Assert.assertFalse(TypeUtils.isAssignable(wild, tv));
    }

    @Test
    public void testGetTypeArguments() throws Exception {
        ParameterizedType strListType = (ParameterizedType) getFieldType("listString");
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(strListType);
        Assert.assertEquals(1, typeArgs.size());
        Assert.assertEquals(String.class, typeArgs.values().iterator().next());

        Map<TypeVariable<?>, Type> dddArgs = TypeUtils.getTypeArguments(DDD.class, AAA.class);
        Assert.assertNotNull(dddArgs);
        Assert.assertEquals(2, dddArgs.size());
        Assert.assertTrue(dddArgs.containsValue(String.class));
        Assert.assertTrue(dddArgs.containsValue(Integer.class));

        Map<TypeVariable<?>, Type> classImplArgs = TypeUtils.getTypeArguments(ClassImpl.class, InterfaceA.class);
        Assert.assertNotNull(classImplArgs);
        Assert.assertEquals(1, classImplArgs.size());
        Assert.assertEquals(String.class, classImplArgs.values().iterator().next());

        Assert.assertNull(TypeUtils.getTypeArguments(String.class, List.class));
        Assert.assertNotNull(TypeUtils.getTypeArguments(int.class, int.class));
        Assert.assertNotNull(TypeUtils.getTypeArguments(int.class, Integer.class));

        ParameterizedType deepInner = (ParameterizedType) getFieldType("deepInnerField");
        Map<TypeVariable<?>, Type> innerArgs = TypeUtils.getTypeArguments(deepInner);
        Assert.assertNotNull(innerArgs);

        Type genArr = getFieldType("listStringArray");
        Map<TypeVariable<?>, Type> genArrArgs = TypeUtils.getTypeArguments(genArr, List[].class);
        Assert.assertNotNull(genArrArgs);
        Assert.assertEquals(String.class, genArrArgs.values().iterator().next());

        Type wild = getFieldType("listExtendsNumber");
        WildcardType wt = (WildcardType) ((ParameterizedType) wild).getActualTypeArguments()[0];
        Map<TypeVariable<?>, Type> wtArgs = TypeUtils.getTypeArguments(wt, Number.class);
        Assert.assertNotNull(wtArgs);

        Type tv = getFieldType("typeVar");
        Map<TypeVariable<?>, Type> tvArgs = TypeUtils.getTypeArguments(tv, Number.class);
        Assert.assertNotNull(tvArgs);
    }

    @Test
    public void testDetermineTypeArguments() throws Exception {
        ParameterizedType dddSuper = (ParameterizedType) DDD.class.getGenericSuperclass();
        Map<TypeVariable<?>, Type> res = TypeUtils.determineTypeArguments(DDD.class, dddSuper);
        Assert.assertNotNull(res);

        ParameterizedType treeSetIterable = (ParameterizedType) TreeSet.class.getMethod("iterator").getGenericReturnType();
        Map<TypeVariable<?>, Type> treeSetRes = TypeUtils.determineTypeArguments(TreeSet.class, (ParameterizedType) getFieldType("listString"));
        Assert.assertNull(treeSetRes);

        Map<TypeVariable<?>, Type> self = TypeUtils.determineTypeArguments(List.class, (ParameterizedType) getFieldType("listString"));
        Assert.assertNotNull(self);
        Assert.assertEquals(String.class, self.values().iterator().next());
    }

    @Test
    public void testIsInstance() throws Exception {
        Type strListType = getFieldType("listString");
        List<String> list = new ArrayList<String>();

        Assert.assertFalse(TypeUtils.isInstance("test", null));
        Assert.assertTrue(TypeUtils.isInstance(null, String.class));
        Assert.assertFalse(TypeUtils.isInstance(null, int.class));
        Assert.assertTrue(TypeUtils.isInstance("test", String.class));
        Assert.assertFalse(TypeUtils.isInstance(123, String.class));
        Assert.assertTrue(TypeUtils.isInstance(list, strListType));
        Assert.assertTrue(TypeUtils.isInstance(new String[0], getFieldType("stringArray")));
    }

    @Test
    public void testNormalizeUpperBounds() {
        Type[] empty = new Type[0];
        Assert.assertSame(empty, TypeUtils.normalizeUpperBounds(empty));

        Type[] single = new Type[] { Number.class };
        Assert.assertSame(single, TypeUtils.normalizeUpperBounds(single));

        Type[] redundant = new Type[] { Number.class, Integer.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(redundant);
        Assert.assertEquals(1, normalized.length);
        Assert.assertEquals(Integer.class, normalized[0]);

        Type[] unrelated = new Type[] { Comparable.class, Serializable.class };
        Type[] normUnrelated = TypeUtils.normalizeUpperBounds(unrelated);
        Assert.assertEquals(2, normUnrelated.length);
    }

    @Test
    public void testGetImplicitBounds() throws Exception {
        TypeVariable<?> tv = (TypeVariable<?>) getFieldType("typeVar");
        Type[] bounds = TypeUtils.getImplicitBounds(tv);
        Assert.assertEquals(2, bounds.length);

        WildcardType extNum = (WildcardType) ((ParameterizedType) getFieldType("listExtendsNumber")).getActualTypeArguments()[0];
        Type[] upper = TypeUtils.getImplicitUpperBounds(extNum);
        Assert.assertEquals(1, upper.length);
        Assert.assertEquals(Number.class, upper[0]);

        WildcardType supNum = (WildcardType) ((ParameterizedType) getFieldType("listSuperNumber")).getActualTypeArguments()[0];
        Type[] lower = TypeUtils.getImplicitLowerBounds(supNum);
        Assert.assertEquals(1, lower.length);
        Assert.assertEquals(Number.class, lower[0]);

        Type[] lowerEmpty = TypeUtils.getImplicitLowerBounds(extNum);
        Assert.assertEquals(1, lowerEmpty.length);
        Assert.assertNull(lowerEmpty[0]);
    }

    @Test
    public void testTypesSatisfyVariables() throws Exception {
        TypeVariable<?>[] typeParams = BoundedClass.class.getTypeParameters();
        TypeVariable<?> s = typeParams[0];
        TypeVariable<?> u = typeParams[1];

        Map<TypeVariable<?>, Type> mapValid = new HashMap<TypeVariable<?>, Type>();
        mapValid.put(s, Integer.class);
        mapValid.put(u, String.class);
        Assert.assertTrue(TypeUtils.typesSatisfyVariables(mapValid));

        Map<TypeVariable<?>, Type> mapInvalid = new HashMap<TypeVariable<?>, Type>();
        mapInvalid.put(s, String.class);
        mapInvalid.put(u, String.class);
        Assert.assertFalse(TypeUtils.typesSatisfyVariables(mapInvalid));
    }

    @Test
    public void testGetRawType() throws Exception {
        Assert.assertEquals(String.class, TypeUtils.getRawType(String.class, null));
        Assert.assertEquals(List.class, TypeUtils.getRawType(getFieldType("listString"), null));

        Type tv = getFieldType("typeVar");
        Assert.assertNull(TypeUtils.getRawType(tv, null));
        Assert.assertNull(TypeUtils.getRawType(tv, String.class));

        Type genArr = getFieldType("listStringArray");
        Assert.assertEquals(List[].class, TypeUtils.getRawType(genArr, null));

        Type wild = ((ParameterizedType) getFieldType("listWildcard")).getActualTypeArguments()[0];
        Assert.assertNull(TypeUtils.getRawType(wild, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawTypeUnknown() {
        Type customType = new Type() {
            @Override
            public String getTypeName() {
                return "custom";
            }
        };
        TypeUtils.getRawType(customType, null);
    }

    private static class DummyCustomType implements Type {}

    @Test(expected = IllegalStateException.class)
    public void testIsAssignableUnhandledType() {
        TypeUtils.isAssignable(new DummyCustomType(), String.class);
    }
}
