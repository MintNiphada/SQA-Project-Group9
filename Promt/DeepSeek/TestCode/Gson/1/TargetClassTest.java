package com.google.gson;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import org.junit.Test;

public class TypeInfoFactoryTest {

    // Supportive classes
    static class Simple {
        int intField;
    }

    static class Generic<T> {
        T value;
    }

    static class ParameterizedFieldHolder {
        List<String> stringList;
    }

    static class GenericArrayHolder<T> {
        T[] array;
    }

    static class WildcardHolder {
        List<? extends Number> numbers;
    }

    // Test getTypeInfoForArray
    @Test
    public void testGetTypeInfoForArray_ArrayType() {
        TypeInfoArray result = TypeInfoFactory.getTypeInfoForArray(String[].class);
        assertNotNull(result);
        assertTrue(result instanceof TypeInfoArray);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeInfoForArray_NonArrayType() {
        TypeInfoFactory.getTypeInfoForArray(String.class);
    }

    // Test getTypeInfoForField with simple non-generic field
    @Test
    public void testGetTypeInfoForField_SimpleField() throws Exception {
        Field f = Simple.class.getDeclaredField("intField");
        Type definingF = Simple.class;
        TypeInfo result = TypeInfoFactory.getTypeInfoForField(f, definingF);
        assertNotNull(result);
        assertEquals(int.class, result.getActualType());
    }

    // Test getTypeInfoForField with TypeVariable field and ParameterizedType parent (resolves)
    @Test
    public void testGetTypeInfoForField_GenericField_Resolved() throws Exception {
        Field field = Generic.class.getDeclaredField("value");
        // Create a ParameterizedType representing Generic<String>
        Type parent = new ParameterizedTypeImpl(Generic.class, new Type[]{String.class}, null);
        TypeInfo result = TypeInfoFactory.getTypeInfoForField(field, parent);
        assertNotNull(result);
        assertEquals(String.class, result.getActualType());
    }

    // Test getTypeInfoForField with TypeVariable field and non-ParameterizedType parent (UnsupportedOperationException)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetTypeInfoForField_GenericField_NonParameterizedParent() throws Exception {
        Field field = Generic.class.getDeclaredField("value");
        Type parent = Generic.class; // raw class, not ParameterizedType
        TypeInfoFactory.getTypeInfoForField(field, parent);
    }

    // Test getTypeInfoForField with ParameterizedType field
    @Test
    public void testGetTypeInfoForField_ParameterizedField() throws Exception {
        Field field = ParameterizedFieldHolder.class.getDeclaredField("stringList");
        Type parent = ParameterizedFieldHolder.class;
        TypeInfo result = TypeInfoFactory.getTypeInfoForField(field, parent);
        assertNotNull(result);
        Type actualType = result.getActualType();
        assertTrue(actualType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) actualType;
        assertEquals(List.class, pt.getRawType());
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    // Test getTypeInfoForField with GenericArrayType field
    @Test
    public void testGetTypeInfoForField_GenericArrayField() throws Exception {
        Field field = GenericArrayHolder.class.getDeclaredField("array");
        Type parent = new ParameterizedTypeImpl(GenericArrayHolder.class, new Type[]{Integer.class}, null);
        TypeInfo result = TypeInfoFactory.getTypeInfoForField(field, parent);
        assertNotNull(result);
        Type actualType = result.getActualType();
        // Expect Integer[] (Class array) since componentType resolved to Integer.class and wrapWithArray is used
        assertEquals(Integer[].class, actualType);
    }

    // Test getTypeInfoForField with WildcardType field
    @Test
    public void testGetTypeInfoForField_WildcardField() throws Exception {
        Field field = WildcardHolder.class.getDeclaredField("numbers");
        Type parent = WildcardHolder.class;
        TypeInfo result = TypeInfoFactory.getTypeInfoForField(field, parent);
        assertNotNull(result);
        Type actualType = result.getActualType();
        // Should resolve wildcard upper bound to Number
        assertTrue(actualType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) actualType;
        assertEquals(List.class, pt.getRawType());
        assertEquals(Number.class, pt.getActualTypeArguments()[0]);
    }

    // Test getActualType directly (private method) for Class branch
    @Test
    public void testGetActualType_ClassType() throws Exception {
        Type result = invokeGetActualType(String.class, Object.class, Object.class);
        assertEquals(String.class, result);
    }

    // Test getActualType with ParameterizedType branch (including recursive)
    @Test
    public void testGetActualType_ParameterizedType() throws Exception {
        // Create a ParameterizedType List<String>
        Type stringList = new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null);
        Type result = invokeGetActualType(stringList, Object.class, Object.class);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) result;
        assertEquals(List.class, pt.getRawType());
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    // Test getActualType with GenericArrayType where componentType changes vs not
    @Test
    public void testGetActualType_GenericArrayType_ComponentResolved() throws Exception {
        // Create a GenericArrayType with TypeVariable component, resolved to class
        TypeVariable<?> typeVar = Generic.class.getTypeParameters()[0]; // T
        Type genericArrayTypeWithVar = new GenericArrayTypeImpl(typeVar);
        // Parent: ParameterizedType Generic<Integer>
        Type parent = new ParameterizedTypeImpl(Generic.class, new Type[]{Integer.class}, null);
        Type result = invokeGetActualType(genericArrayTypeWithVar, parent, Generic.class);
        assertEquals(Integer[].class, result); // wrapWithArray
    }

    @Test
    public void testGetActualType_GenericArrayType_ComponentUnchanged() throws Exception {
        // Component is already a Class that doesn't change after getActualType
        Type stringArrayComponent = String.class;
        Type genericArrayTypeWithClass = new GenericArrayTypeImpl(stringArrayComponent);
        Type parent = Object.class;
        Type result = invokeGetActualType(genericArrayTypeWithClass, parent, Object.class);
        // Since componentType is Class and equals actualType (same), return castedType
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    // Test getActualType with TypeVariable branch, resolved
    @Test
    public void testGetActualType_TypeVariable_Resolved() throws Exception {
        TypeVariable<?> typeVar = Generic.class.getTypeParameters()[0];
        Type parent = new ParameterizedTypeImpl(Generic.class, new Type[]{Integer.class}, null);
        Type result = invokeGetActualType(typeVar, parent, Generic.class);
        assertEquals(Integer.class, result);
    }

    // Test getActualType with WildcardType
    @Test
    public void testGetActualType_WildcardType() throws Exception {
        // Obtain a wildcard type from a field
        Field field = WildcardHolder.class.getDeclaredField("numbers");
        ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
        Type wildcard = fieldType.getActualTypeArguments()[0];
        assertTrue(wildcard instanceof WildcardType);
        Type result = invokeGetActualType(wildcard, WildcardHolder.class, WildcardHolder.class);
        // Should resolve to Number
        assertEquals(Number.class, result);
    }

    // Test getActualType with unknown type -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetActualType_UnknownType() throws Exception {
        // Create a custom Type that is not Class, ParameterizedType, GenericArrayType, TypeVariable, or WildcardType
        Type unknown = new Type(){};
        invokeGetActualType(unknown, Object.class, Object.class);
    }

    // Test extractRealTypes with null array (implies NullPointerException from Preconditions.checkNotNull)
    @Test(expected = NullPointerException.class)
    public void testExtractRealTypes_Null() throws Exception {
        invokeExtractRealTypes(null, Object.class, Object.class);
    }

    // Test getIndex: correct index
    @Test
    public void testGetIndex_Present() throws Exception {
        TypeVariable<?>[] vars = Generic.class.getTypeParameters();
        TypeVariable<?> target = vars[0];
        int index = invokeGetIndex(vars, target);
        assertEquals(0, index);
    }

    // Test getIndex: not present (should throw IllegalStateException)
    @Test(expected = IllegalStateException.class)
    public void testGetIndex_NotPresent() throws Exception {
        TypeVariable<?>[] vars = Generic.class.getTypeParameters();
        // Use a different type variable from another class
        TypeVariable<?> other = Simple.class.getTypeParameters(); // empty, but we need a TypeVariable; can't get since Simple has none
        // Better: use another generic class
        class OtherGeneric<X> {}
        TypeVariable<?>[] otherVars = OtherGeneric.class.getTypeParameters();
        TypeVariable<?> other = otherVars[0];
        invokeGetIndex(vars, other);
    }

    // Helper methods to test private methods via reflection
    private Type invokeGetActualType(Type typeToEvaluate, Type parentType, Class<?> rawParentClass) throws Exception {
        Method method = TypeInfoFactory.class.getDeclaredMethod("getActualType", Type.class, Type.class, Class.class);
        method.setAccessible(true);
        return (Type) method.invoke(null, typeToEvaluate, parentType, rawParentClass);
    }

    private Type[] invokeExtractRealTypes(Type[] actualTypeArguments, Type parentType, Class<?> rawParentClass) throws Exception {
        Method method = TypeInfoFactory.class.getDeclaredMethod("extractRealTypes", Type[].class, Type.class, Class.class);
        method.setAccessible(true);
        return (Type[]) method.invoke(null, actualTypeArguments, parentType, rawParentClass);
    }

    private int invokeGetIndex(TypeVariable<?>[] types, TypeVariable<?> type) throws Exception {
        Method method = TypeInfoFactory.class.getDeclaredMethod("getIndex", TypeVariable[].class, TypeVariable.class);
        method.setAccessible(true);
        return (Integer) method.invoke(null, types, type);
    }
}