package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.internal.util.ObjectMethodsGuru;
import org.mockito.internal.util.MockUtil;
import org.mockito.mock.MockName;
import org.mockito.mock.MockSettings;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReturnsEmptyValuesTest {

    @Mock
    private ObjectMethodsGuru methodsGuru;
    @Mock
    private MockUtil mockUtil;

    private ReturnsEmptyValues returnsEmptyValues;

    private interface TestMethods {
        int intMethod();
        List listMethod();
        String stringMethod();
    }

    @Before
    public void setUp() throws Exception {
        returnsEmptyValues = new ReturnsEmptyValues();
        Field guruField = ReturnsEmptyValues.class.getDeclaredField("methodsGuru");
        guruField.setAccessible(true);
        guruField.set(returnsEmptyValues, methodsGuru);
        Field mockUtilField = ReturnsEmptyValues.class.getDeclaredField("mockUtil");
        mockUtilField.setAccessible(true);
        mockUtilField.set(returnsEmptyValues, mockUtil);
    }

    // Tests for returnValueFor with primitives
    @Test
    public void testReturnValueForPrimitiveBoolean() {
        assertEquals(false, returnsEmptyValues.returnValueFor(boolean.class));
    }

    @Test
    public void testReturnValueForPrimitiveByte() {
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(byte.class));
    }

    @Test
    public void testReturnValueForPrimitiveShort() {
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(short.class));
    }

    @Test
    public void testReturnValueForPrimitiveInt() {
        assertEquals(0, returnsEmptyValues.returnValueFor(int.class));
    }

    @Test
    public void testReturnValueForPrimitiveLong() {
        assertEquals(0L, returnsEmptyValues.returnValueFor(long.class));
    }

    @Test
    public void testReturnValueForPrimitiveFloat() {
        assertEquals(0.0f, returnsEmptyValues.returnValueFor(float.class));
    }

    @Test
    public void testReturnValueForPrimitiveDouble() {
        assertEquals(0.0d, returnsEmptyValues.returnValueFor(double.class));
    }

    @Test
    public void testReturnValueForPrimitiveChar() {
        assertEquals('\0', returnsEmptyValues.returnValueFor(char.class));
    }

    // Tests for returnValueFor with wrapper types
    @Test
    public void testReturnValueForWrapperBoolean() {
        assertEquals(false, returnsEmptyValues.returnValueFor(Boolean.class));
    }

    @Test
    public void testReturnValueForWrapperByte() {
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(Byte.class));
    }

    @Test
    public void testReturnValueForWrapperShort() {
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(Short.class));
    }

    @Test
    public void testReturnValueForWrapperInteger() {
        assertEquals(0, returnsEmptyValues.returnValueFor(Integer.class));
    }

    @Test
    public void testReturnValueForWrapperLong() {
        assertEquals(0L, returnsEmptyValues.returnValueFor(Long.class));
    }

    @Test
    public void testReturnValueForWrapperFloat() {
        assertEquals(0.0f, returnsEmptyValues.returnValueFor(Float.class));
    }

    @Test
    public void testReturnValueForWrapperDouble() {
        assertEquals(0.0d, returnsEmptyValues.returnValueFor(Double.class));
    }

    @Test
    public void testReturnValueForWrapperCharacter() {
        assertEquals('\0', returnsEmptyValues.returnValueFor(Character.class));
    }

    // Tests for returnValueFor with collection types
    @Test
    public void testReturnValueForCollection() {
        assertTrue(returnsEmptyValues.returnValueFor(Collection.class) instanceof LinkedList);
    }

    @Test
    public void testReturnValueForSet() {
        assertTrue(returnsEmptyValues.returnValueFor(Set.class) instanceof HashSet);
    }

    @Test
    public void testReturnValueForHashSet() {
        assertTrue(returnsEmptyValues.returnValueFor(HashSet.class) instanceof HashSet);
    }

    @Test
    public void testReturnValueForSortedSet() {
        assertTrue(returnsEmptyValues.returnValueFor(SortedSet.class) instanceof TreeSet);
    }

    @Test
    public void testReturnValueForTreeSet() {
        assertTrue(returnsEmptyValues.returnValueFor(TreeSet.class) instanceof TreeSet);
    }

    @Test
    public void testReturnValueForLinkedHashSet() {
        assertTrue(returnsEmptyValues.returnValueFor(LinkedHashSet.class) instanceof LinkedHashSet);
    }

    @Test
    public void testReturnValueForList() {
        assertTrue(returnsEmptyValues.returnValueFor(List.class) instanceof LinkedList);
    }

    @Test
    public void testReturnValueForLinkedList() {
        assertTrue(returnsEmptyValues.returnValueFor(LinkedList.class) instanceof LinkedList);
    }

    @Test
    public void testReturnValueForArrayList() {
        assertTrue(returnsEmptyValues.returnValueFor(ArrayList.class) instanceof ArrayList);
    }

    @Test
    public void testReturnValueForMap() {
        assertTrue(returnsEmptyValues.returnValueFor(Map.class) instanceof HashMap);
    }

    @Test
    public void testReturnValueForHashMap() {
        assertTrue(returnsEmptyValues.returnValueFor(HashMap.class) instanceof HashMap);
    }

    @Test
    public void testReturnValueForSortedMap() {
        assertTrue(returnsEmptyValues.returnValueFor(SortedMap.class) instanceof TreeMap);
    }

    @Test
    public void testReturnValueForTreeMap() {
        assertTrue(returnsEmptyValues.returnValueFor(TreeMap.class) instanceof TreeMap);
    }

    @Test
    public void testReturnValueForLinkedHashMap() {
        assertTrue(returnsEmptyValues.returnValueFor(LinkedHashMap.class) instanceof LinkedHashMap);
    }

    @Test
    public void testReturnValueForOtherType() {
        assertNull(returnsEmptyValues.returnValueFor(String.class));
    }

    @Test
    public void testReturnValueForNullType() {
        assertNull(returnsEmptyValues.returnValueFor(null));
    }

    // Tests for answer method
    @Test
    public void testAnswerToStringDefaultName() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Object mockObject = mock(Object.class);
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(methodsGuru.isToString(toStringMethod)).thenReturn(true);

        MockName mockName = mock(MockName.class);
        when(mockName.isDefault()).thenReturn(true);
        when(mockUtil.getMockName(mockObject)).thenReturn(mockName);

        MockSettings mockSettings = mock(MockSettings.class);
        when(mockSettings.getTypeToMock()).thenReturn(TestMethods.class);
        when(mockUtil.getMockSettings(mockObject)).thenReturn(mockSettings);
        when(mockObject.hashCode()).thenReturn(12345);

        String result = (String) returnsEmptyValues.answer(invocation);
        assertEquals("Mock for TestMethods, hashCode: 12345", result);
    }

    @Test
    public void testAnswerToStringNonDefaultName() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Object mockObject = mock(Object.class);
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(methodsGuru.isToString(toStringMethod)).thenReturn(true);

        MockName mockName = mock(MockName.class);
        when(mockName.isDefault()).thenReturn(false);
        when(mockName.toString()).thenReturn("customName");
        when(mockUtil.getMockName(mockObject)).thenReturn(mockName);

        String result = (String) returnsEmptyValues.answer(invocation);
        assertEquals("customName", result);
    }

    @Test
    public void testAnswerCompareToSameReference() throws Exception {
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Object mockObject = mock(Object.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(invocation.getArguments()).thenReturn(new Object[]{mockObject});
        when(methodsGuru.isToString(compareToMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(compareToMethod)).thenReturn(true);

        assertEquals(0, returnsEmptyValues.answer(invocation));
    }

    @Test
    public void testAnswerCompareToDifferentReference() throws Exception {
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Object mockObject = mock(Object.class);
        Object otherObject = mock(Object.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(invocation.getArguments()).thenReturn(new Object[]{otherObject});
        when(methodsGuru.isToString(compareToMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(compareToMethod)).thenReturn(true);

        assertEquals(1, returnsEmptyValues.answer(invocation));
    }

    @Test
    public void testAnswerOtherReturnsPrimitive() throws Exception {
        Method intMethod = TestMethods.class.getMethod("intMethod");
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(intMethod);
        when(methodsGuru.isToString(intMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(intMethod)).thenReturn(false);

        assertEquals(0, returnsEmptyValues.answer(invocation));
    }

    @Test
    public void testAnswerOtherReturnsCollection() throws Exception {
        Method listMethod = TestMethods.class.getMethod("listMethod");
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(listMethod);
        when(methodsGuru.isToString(listMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(listMethod)).thenReturn(false);

        assertTrue(returnsEmptyValues.answer(invocation) instanceof LinkedList);
    }

    @Test
    public void testAnswerOtherReturnsNull() throws Exception {
        Method stringMethod = TestMethods.class.getMethod("stringMethod");
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(stringMethod);
        when(methodsGuru.isToString(stringMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(stringMethod)).thenReturn(false);

        assertNull(returnsEmptyValues.answer(invocation));
    }

    @Test(expected = NullPointerException.class)
    public void testAnswerWithNullMethod() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(null);
        returnsEmptyValues.answer(invocation);
    }
}
