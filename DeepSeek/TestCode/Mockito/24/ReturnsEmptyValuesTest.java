package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.ObjectMethodsGuru;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockName;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;
    private ObjectMethodsGuru methodsGuru;
    private MockUtil mockUtil;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
        methodsGuru = mock(ObjectMethodsGuru.class);
        mockUtil = mock(MockUtil.class);
        returnsEmptyValues.methodsGuru = methodsGuru;
        returnsEmptyValues.mockUtil = mockUtil;
    }

    @Test
    public void testAnswerToStringDefaultName() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(methodsGuru.isToString(toStringMethod)).thenReturn(true);
        Object mockObject = new Object();
        when(invocation.getMock()).thenReturn(mockObject);
        MockName mockName = mock(MockName.class);
        when(mockName.isDefault()).thenReturn(true);
        when(mockUtil.getMockName(mockObject)).thenReturn(mockName);
        org.mockito.mock.MockCreationSettings settings = mock(org.mockito.mock.MockCreationSettings.class);
        when(settings.getTypeToMock()).thenReturn((Class) Object.class);
        when(mockUtil.getMockSettings(mockObject)).thenReturn(settings);
        String result = (String) returnsEmptyValues.answer(invocation);
        assertTrue(result.startsWith("Mock for Object, hashCode: "));
    }

    @Test
    public void testAnswerToStringNonDefaultName() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(methodsGuru.isToString(toStringMethod)).thenReturn(true);
        Object mockObject = new Object();
        when(invocation.getMock()).thenReturn(mockObject);
        MockName mockName = mock(MockName.class);
        when(mockName.isDefault()).thenReturn(false);
        when(mockName.toString()).thenReturn("customName");
        when(mockUtil.getMockName(mockObject)).thenReturn(mockName);
        String result = (String) returnsEmptyValues.answer(invocation);
        assertEquals("customName", result);
    }

    @Test
    public void testAnswerCompareTo() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        when(methodsGuru.isCompareToMethod(compareToMethod)).thenReturn(true);
        Integer result = (Integer) returnsEmptyValues.answer(invocation);
        assertEquals(Integer.valueOf(1), result);
    }

    @Test
    public void testAnswerOtherDelegatesToReturnValueFor() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method someMethod = Object.class.getMethod("hashCode");
        when(invocation.getMethod()).thenReturn(someMethod);
        when(methodsGuru.isToString(someMethod)).thenReturn(false);
        when(methodsGuru.isCompareToMethod(someMethod)).thenReturn(false);
        Object result = returnsEmptyValues.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testReturnValueForPrimitiveInt() {
        assertEquals(0, returnsEmptyValues.returnValueFor(int.class));
    }

    @Test
    public void testReturnValueForPrimitiveBoolean() {
        assertEquals(false, returnsEmptyValues.returnValueFor(boolean.class));
    }

    @Test
    public void testReturnValueForWrapperInteger() {
        assertEquals(0, returnsEmptyValues.returnValueFor(Integer.class));
    }

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
}
