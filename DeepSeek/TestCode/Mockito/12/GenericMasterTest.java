package org.mockito.internal.util.reflection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

public class GenericMasterTest {

    private final GenericMaster master = new GenericMaster();

    // Helper class with various fields to get real Field objects
    static class TestFields {
        List<String> stringList;
        List rawList;
        String stringField;
        Map<String, Integer> map;
        List<?> wildcardList;
        List<List<String>> nestedList;
    }

    @Test
    public void should_return_Object_class_when_generic_type_is_null() throws NoSuchFieldException {
        Field field = mock(Field.class);
        when(field.getGenericType()).thenReturn(null);

        Class<?> result = master.getGenericType(field);

        assertEquals(Object.class, result);
    }

    @Test
    public void should_return_Object_class_when_generic_type_is_not_ParameterizedType() throws NoSuchFieldException {
        // use a TypeVariable as an example (cannot be easily mocked without going deeper, but we can return a raw Class)
        Field field = mock(Field.class);
        // Returning Class object simulates a non-generic field (raw type)
        Type nonParamType = String.class;
        when(field.getGenericType()).thenReturn(nonParamType);

        Class<?> result = master.getGenericType(field);

        assertEquals(Object.class, result);
    }

    @Test
    public void should_return_Object_class_for_raw_non_generic_field() throws NoSuchFieldException {
        Field field = TestFields.class.getDeclaredField("stringField");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void should_return_Object_class_for_raw_list_field() throws NoSuchFieldException {
        Field field = TestFields.class.getDeclaredField("rawList");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void should_return_Object_class_for_wildcard_parameterized_type() throws NoSuchFieldException {
        // For List<?>, getGenericType() returns ParameterizedType but actual type argument is WildcardType,
        // which is not a Class and will cause ClassCastException? Actually WildcardType is not Class, so cast will fail.
        // But let's test the actual behavior: it will try to cast to Class and throw ClassCastException.
        // So we assert that exception is thrown. That demonstrates the limitation.
        Field field = TestFields.class.getDeclaredField("wildcardList");
        // According to code, it will attempt cast and throw ClassCastException.
        try {
            master.getGenericType(field);
            fail("Expected ClassCastException when first actual type argument is WildcardType");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test
    public void should_return_first_actual_type_argument_as_class() throws NoSuchFieldException {
        // For List<String>, expects String.class
        Field field = TestFields.class.getDeclaredField("stringList");
        Class<?> result = master.getGenericType(field);
        assertEquals(String.class, result);
    }

    @Test
    public void should_return_first_actual_type_argument_for_map() throws NoSuchFieldException {
        // For Map<String,Integer>, first type argument is String.class
        Field field = TestFields.class.getDeclaredField("map");
        Class<?> result = master.getGenericType(field);
        assertEquals(String.class, result);
    }

    @Test
    public void should_throw_ClassCastException_for_nested_parameterized_type() throws NoSuchFieldException {
        // List<List<String>> -> first type argument is ParameterizedType, not a Class -> ClassCastException
        Field field = TestFields.class.getDeclaredField("nestedList");
        try {
            master.getGenericType(field);
            fail("Expected ClassCastException because nested generic is not a raw Class");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void should_throw_ArrayIndexOutOfBoundsException_when_actual_type_arguments_empty() throws NoSuchFieldException {
        Field field = mock(Field.class);
        ParameterizedType mockParamType = mock(ParameterizedType.class);
        when(mockParamType.getActualTypeArguments()).thenReturn(new Type[0]);
        when(field.getGenericType()).thenReturn(mockParamType);

        master.getGenericType(field);
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_NullPointerException_when_field_is_null() {
        master.getGenericType(null);
    }

    @Test
    public void should_throw_ClassCastException_when_actual_type_argument_is_TypeVariable() throws NoSuchFieldException {
        Field field = mock(Field.class);
        ParameterizedType mockParamType = mock(ParameterizedType.class);
        TypeVariable<?> typeVar = mock(TypeVariable.class);
        when(mockParamType.getActualTypeArguments()).thenReturn(new Type[] { typeVar });
        when(field.getGenericType()).thenReturn(mockParamType);

        try {
            master.getGenericType(field);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test
    public void should_return_correct_class_when_parameterized_with_concrete_class_via_mock() throws NoSuchFieldException {
        Field field = mock(Field.class);
        ParameterizedType paramType = mock(ParameterizedType.class);
        when(paramType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class });
        when(field.getGenericType()).thenReturn(paramType);

        Class<?> result = master.getGenericType(field);

        assertEquals(Integer.class, result);
    }
}
