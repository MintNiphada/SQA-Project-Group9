package org.mockito.internal.util.reflection;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GenericMasterTest {

    private GenericMaster genericMaster;

    // Test fields for reflection
    public List<String> stringList;
    public List<Integer> integerList;
    public Set<Double> doubleSet;
    public Map<Byte, Short> byteShortMap;
    public String nonGenericString;
    public int primitiveInt;
    public Object objectField;
    public String[] stringArray;
    @SuppressWarnings("rawtypes")
    public List rawList;
    public List<List<String>> nestedList;
    public List<?> wildcardList;

    @Before
    public void setUp() {
        genericMaster = new GenericMaster();
    }

    @Test
    public void shouldReturnCorrectTypeForSingleTypeParameter() throws Exception {
        Field field = GenericMasterTest.class.getField("stringList");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(String.class, result);
    }

    @Test
    public void shouldReturnCorrectTypeForIntegerList() throws Exception {
        Field field = GenericMasterTest.class.getField("integerList");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Integer.class, result);
    }

    @Test
    public void shouldReturnCorrectTypeForSet() throws Exception {
        Field field = GenericMasterTest.class.getField("doubleSet");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Double.class, result);
    }

    @Test
    public void shouldReturnFirstTypeParameterForMap() throws Exception {
        Field field = GenericMasterTest.class.getField("byteShortMap");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Byte.class, result);
    }

    @Test
    public void shouldReturnObjectClassForNonGenericField() throws Exception {
        Field field = GenericMasterTest.class.getField("nonGenericString");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void shouldReturnObjectClassForPrimitiveField() throws Exception {
        Field field = GenericMasterTest.class.getField("primitiveInt");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void shouldReturnObjectClassForObjectField() throws Exception {
        Field field = GenericMasterTest.class.getField("objectField");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void shouldReturnObjectClassForArrayField() throws Exception {
        Field field = GenericMasterTest.class.getField("stringArray");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test
    public void shouldReturnObjectClassForRawList() throws Exception {
        Field field = GenericMasterTest.class.getField("rawList");
        Class<?> result = genericMaster.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test(expected = ClassCastException.class)
    public void shouldThrowClassCastExceptionForNestedGenerics() throws Exception {
        Field field = GenericMasterTest.class.getField("nestedList");
        genericMaster.getGenericType(field);
    }

    @Test(expected = ClassCastException.class)
    public void shouldThrowClassCastExceptionForWildcardGenerics() throws Exception {
        Field field = GenericMasterTest.class.getField("wildcardList");
        genericMaster.getGenericType(field);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionForNullField() {
        genericMaster.getGenericType(null);
    }

    @Test
    public void shouldInstantiateGenericMaster() {
        GenericMaster instance = new GenericMaster();
        assertNotNull(instance);
    }
}
