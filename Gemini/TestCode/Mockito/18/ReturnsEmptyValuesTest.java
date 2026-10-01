package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues answer;

    interface SampleComparable extends Comparable<SampleComparable> {
        List<String> getList();
        String getCustomObject();
    }

    @Before
    public void setUp() {
        answer = new ReturnsEmptyValues();
    }

    @Test
    public void testReturnValueForPrimitives() {
        assertEquals(false, answer.returnValueFor(boolean.class));
        assertEquals((byte) 0, answer.returnValueFor(byte.class));
        assertEquals((char) 0, answer.returnValueFor(char.class));
        assertEquals((short) 0, answer.returnValueFor(short.class));
        assertEquals(0, answer.returnValueFor(int.class));
        assertEquals(0L, answer.returnValueFor(long.class));
        assertEquals(0.0f, answer.returnValueFor(float.class));
        assertEquals(0.0d, answer.returnValueFor(double.class));
    }

    @Test
    public void testReturnValueForPrimitiveWrappers() {
        assertEquals(Boolean.FALSE, answer.returnValueFor(Boolean.class));
        assertEquals(Byte.valueOf((byte) 0), answer.returnValueFor(Byte.class));
        assertEquals(Character.valueOf((char) 0), answer.returnValueFor(Character.class));
        assertEquals(Short.valueOf((short) 0), answer.returnValueFor(Short.class));
        assertEquals(Integer.valueOf(0), answer.returnValueFor(Integer.class));
        assertEquals(Long.valueOf(0L), answer.returnValueFor(Long.class));
        assertEquals(Float.valueOf(0.0f), answer.returnValueFor(Float.class));
        assertEquals(Double.valueOf(0.0d), answer.returnValueFor(Double.class));
    }

    @Test
    public void testReturnValueForCollections() {
        Object collection = answer.returnValueFor(Collection.class);
        assertTrue(collection instanceof LinkedList);
        assertTrue(((Collection<?>) collection).isEmpty());

        Object set = answer.returnValueFor(Set.class);
        assertTrue(set instanceof HashSet);
        assertTrue(((Set<?>) set).isEmpty());

        Object hashSet = answer.returnValueFor(HashSet.class);
        assertTrue(hashSet instanceof HashSet);
        assertTrue(((HashSet<?>) hashSet).isEmpty());

        Object sortedSet = answer.returnValueFor(SortedSet.class);
        assertTrue(sortedSet instanceof TreeSet);
        assertTrue(((SortedSet<?>) sortedSet).isEmpty());

        Object treeSet = answer.returnValueFor(TreeSet.class);
        assertTrue(treeSet instanceof TreeSet);
        assertTrue(((TreeSet<?>) treeSet).isEmpty());

        Object linkedHashSet = answer.returnValueFor(LinkedHashSet.class);
        assertTrue(linkedHashSet instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) linkedHashSet).isEmpty());

        Object list = answer.returnValueFor(List.class);
        assertTrue(list instanceof LinkedList);
        assertTrue(((List<?>) list).isEmpty());

        Object linkedList = answer.returnValueFor(LinkedList.class);
        assertTrue(linkedList instanceof LinkedList);
        assertTrue(((LinkedList<?>) linkedList).isEmpty());

        Object arrayList = answer.returnValueFor(ArrayList.class);
        assertTrue(arrayList instanceof ArrayList);
        assertTrue(((ArrayList<?>) arrayList).isEmpty());
    }

    @Test
    public void testReturnValueForMaps() {
        Object map = answer.returnValueFor(Map.class);
        assertTrue(map instanceof HashMap);
        assertTrue(((Map<?, ?>) map).isEmpty());

        Object hashMap = answer.returnValueFor(HashMap.class);
        assertTrue(hashMap instanceof HashMap);
        assertTrue(((HashMap<?, ?>) hashMap).isEmpty());

        Object sortedMap = answer.returnValueFor(SortedMap.class);
        assertTrue(sortedMap instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) sortedMap).isEmpty());

        Object treeMap = answer.returnValueFor(TreeMap.class);
        assertTrue(treeMap instanceof TreeMap);
        assertTrue(((TreeMap<?, ?>) treeMap).isEmpty());

        Object linkedHashMap = answer.returnValueFor(LinkedHashMap.class);
        assertTrue(linkedHashMap instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) linkedHashMap).isEmpty());
    }

    @Test
    public void testReturnValueForOtherTypesReturnsNull() {
        assertNull(answer.returnValueFor(String.class));
        assertNull(answer.returnValueFor(Object.class));
        assertNull(answer.returnValueFor(ReturnsEmptyValues.class));
    }

    @Test
    public void testAnswerForToStringDefaultMockName() throws Throwable {
        List<?> mockList = Mockito.mock(List.class);
        Method toStringMethod = Object.class.getMethod("toString");
        
        InvocationOnMock invocation = createInvocation(mockList, toStringMethod, new Object[0]);
        Object result = answer.answer(invocation);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String strResult = (String) result;
        assertTrue(strResult.startsWith("Mock for List, hashCode: "));
        assertTrue(strResult.contains(String.valueOf(mockList.hashCode())));
    }

    @Test
    public void testAnswerForToStringCustomMockName() throws Throwable {
        List<?> mockList = Mockito.mock(List.class, Mockito.withSettings().name("customMockList"));
        Method toStringMethod = Object.class.getMethod("toString");
        
        InvocationOnMock invocation = createInvocation(mockList, toStringMethod, new Object[0]);
        Object result = answer.answer(invocation);
        
        assertEquals("customMockList", result);
    }

    @Test
    public void testAnswerForCompareToSameMock() throws Throwable {
        SampleComparable mock1 = Mockito.mock(SampleComparable.class);
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        
        InvocationOnMock invocation = createInvocation(mock1, compareToMethod, new Object[]{mock1});
        Object result = answer.answer(invocation);
        
        assertEquals(0, result);
    }

    @Test
    public void testAnswerForCompareToDifferentMock() throws Throwable {
        SampleComparable mock1 = Mockito.mock(SampleComparable.class);
        SampleComparable mock2 = Mockito.mock(SampleComparable.class);
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        
        InvocationOnMock invocation = createInvocation(mock1, compareToMethod, new Object[]{mock2});
        Object result = answer.answer(invocation);
        
        assertEquals(1, result);
    }

    @Test
    public void testAnswerForNonToStringNonCompareToMethod() throws Throwable {
        SampleComparable mock = Mockito.mock(SampleComparable.class);
        Method getListMethod = SampleComparable.class.getMethod("getList");
        
        InvocationOnMock invocation = createInvocation(mock, getListMethod, new Object[0]);
        Object result = answer.answer(invocation);
        
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
    }

    @Test
    public void testAnswerReturnsNullForUnrecognizedType() throws Throwable {
        SampleComparable mock = Mockito.mock(SampleComparable.class);
        Method getCustomObjectMethod = SampleComparable.class.getMethod("getCustomObject");
        
        InvocationOnMock invocation = createInvocation(mock, getCustomObjectMethod, new Object[0]);
        Object result = answer.answer(invocation);
        
        assertNull(result);
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(answer);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsEmptyValues);
    }

    private InvocationOnMock createInvocation(final Object mock, final Method method, final Object[] arguments) {
        return new InvocationOnMock() {
            public Object getMock() {
                return mock;
            }

            public Method getMethod() {
                return method;
            }

            public Object[] getArguments() {
                return arguments;
            }

            public Object getArgument(int index) {
                return arguments[index];
            }

            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return clazz.cast(arguments[index]);
            }

            public Object callRealMethod() throws Throwable {
                return null;
            }
        };
    }
}
