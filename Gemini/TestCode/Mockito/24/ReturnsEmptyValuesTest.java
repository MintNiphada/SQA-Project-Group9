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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    interface SampleMethods {
        boolean getBoolean();
        Boolean getBooleanWrapper();
        byte getByte();
        Byte getByteWrapper();
        short getShort();
        Short getShortWrapper();
        char getChar();
        Character getCharWrapper();
        int getInt();
        Integer getIntWrapper();
        long getLong();
        Long getLongWrapper();
        float getFloat();
        Float getFloatWrapper();
        double getDouble();
        Double getDoubleWrapper();
        Collection<Object> getCollection();
        Set<Object> getSet();
        HashSet<Object> getHashSet();
        SortedSet<Object> getSortedSet();
        TreeSet<Object> getTreeSet();
        LinkedHashSet<Object> getLinkedHashSet();
        List<Object> getList();
        LinkedList<Object> getLinkedList();
        ArrayList<Object> getArrayList();
        Map<Object, Object> getMap();
        HashMap<Object, Object> getHashMap();
        SortedMap<Object, Object> getSortedMap();
        TreeMap<Object, Object> getTreeMap();
        LinkedHashMap<Object, Object> getLinkedHashMap();
        String getString();
        Object getObject();
    }

    interface ComparableInterface extends Comparable<ComparableInterface> {
    }

    @Test
    public void shouldReturnDefaultPrimitiveValues() {
        assertEquals(false, returnsEmptyValues.returnValueFor(boolean.class));
        assertEquals(false, returnsEmptyValues.returnValueFor(Boolean.class));
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(byte.class));
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(Byte.class));
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(short.class));
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(Short.class));
        assertEquals((char) 0, returnsEmptyValues.returnValueFor(char.class));
        assertEquals((char) 0, returnsEmptyValues.returnValueFor(Character.class));
        assertEquals(0, returnsEmptyValues.returnValueFor(int.class));
        assertEquals(0, returnsEmptyValues.returnValueFor(Integer.class));
        assertEquals(0L, returnsEmptyValues.returnValueFor(long.class));
        assertEquals(0L, returnsEmptyValues.returnValueFor(Long.class));
        assertEquals(0.0F, returnsEmptyValues.returnValueFor(float.class));
        assertEquals(0.0F, returnsEmptyValues.returnValueFor(Float.class));
        assertEquals(0.0D, returnsEmptyValues.returnValueFor(double.class));
        assertEquals(0.0D, returnsEmptyValues.returnValueFor(Double.class));
    }

    @Test
    public void shouldReturnEmptyCollections() {
        Object collection = returnsEmptyValues.returnValueFor(Collection.class);
        assertTrue(collection instanceof LinkedList);
        assertTrue(((Collection<?>) collection).isEmpty());

        Object set = returnsEmptyValues.returnValueFor(Set.class);
        assertTrue(set instanceof HashSet);
        assertTrue(((Set<?>) set).isEmpty());

        Object hashSet = returnsEmptyValues.returnValueFor(HashSet.class);
        assertTrue(hashSet instanceof HashSet);
        assertTrue(((HashSet<?>) hashSet).isEmpty());

        Object sortedSet = returnsEmptyValues.returnValueFor(SortedSet.class);
        assertTrue(sortedSet instanceof TreeSet);
        assertTrue(((SortedSet<?>) sortedSet).isEmpty());

        Object treeSet = returnsEmptyValues.returnValueFor(TreeSet.class);
        assertTrue(treeSet instanceof TreeSet);
        assertTrue(((TreeSet<?>) treeSet).isEmpty());

        Object linkedHashSet = returnsEmptyValues.returnValueFor(LinkedHashSet.class);
        assertTrue(linkedHashSet instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) linkedHashSet).isEmpty());

        Object list = returnsEmptyValues.returnValueFor(List.class);
        assertTrue(list instanceof LinkedList);
        assertTrue(((List<?>) list).isEmpty());

        Object linkedList = returnsEmptyValues.returnValueFor(LinkedList.class);
        assertTrue(linkedList instanceof LinkedList);
        assertTrue(((LinkedList<?>) linkedList).isEmpty());

        Object arrayList = returnsEmptyValues.returnValueFor(ArrayList.class);
        assertTrue(arrayList instanceof ArrayList);
        assertTrue(((ArrayList<?>) arrayList).isEmpty());
    }

    @Test
    public void shouldReturnEmptyMaps() {
        Object map = returnsEmptyValues.returnValueFor(Map.class);
        assertTrue(map instanceof HashMap);
        assertTrue(((Map<?, ?>) map).isEmpty());

        Object hashMap = returnsEmptyValues.returnValueFor(HashMap.class);
        assertTrue(hashMap instanceof HashMap);
        assertTrue(((HashMap<?, ?>) hashMap).isEmpty());

        Object sortedMap = returnsEmptyValues.returnValueFor(SortedMap.class);
        assertTrue(sortedMap instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) sortedMap).isEmpty());

        Object treeMap = returnsEmptyValues.returnValueFor(TreeMap.class);
        assertTrue(treeMap instanceof TreeMap);
        assertTrue(((TreeMap<?, ?>) treeMap).isEmpty());

        Object linkedHashMap = returnsEmptyValues.returnValueFor(LinkedHashMap.class);
        assertTrue(linkedHashMap instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) linkedHashMap).isEmpty());
    }

    @Test
    public void shouldReturnMutableCollections() {
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) returnsEmptyValues.returnValueFor(List.class);
        list.add("item");
        assertEquals(1, list.size());

        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) returnsEmptyValues.returnValueFor(Map.class);
        map.put("key", "value");
        assertEquals(1, map.size());
    }

    @Test
    public void shouldReturnNullForOtherTypes() {
        assertNull(returnsEmptyValues.returnValueFor(String.class));
        assertNull(returnsEmptyValues.returnValueFor(Object.class));
        assertNull(returnsEmptyValues.returnValueFor(SampleMethods.class));
        assertNull(returnsEmptyValues.returnValueFor(Void.TYPE));
    }

    @Test
    public void shouldReturnToStringForDefaultMock() {
        SampleMethods mock = mock(SampleMethods.class, returnsEmptyValues);
        String toString = mock.toString();
        assertNotNull(toString);
        assertTrue(toString.startsWith("Mock for SampleMethods, hashCode: "));
    }

    @Test
    public void shouldReturnToStringForCustomNamedMock() {
        SampleMethods mock = mock(SampleMethods.class, withSettings().name("customMock").defaultAnswer(returnsEmptyValues));
        assertEquals("customMock", mock.toString());
    }

    @Test
    public void shouldReturnOneForCompareTo() {
        ComparableInterface mock1 = mock(ComparableInterface.class, returnsEmptyValues);
        ComparableInterface mock2 = mock(ComparableInterface.class, returnsEmptyValues);
        int result = mock1.compareTo(mock2);
        assertEquals(1, result);
    }

    @Test
    public void shouldAnswerInvocationsCorrectlyViaMock() {
        SampleMethods mock = mock(SampleMethods.class, returnsEmptyValues);

        assertFalse(mock.getBoolean());
        assertEquals(Boolean.FALSE, mock.getBooleanWrapper());
        assertEquals((byte) 0, mock.getByte());
        assertEquals(Byte.valueOf((byte) 0), mock.getByteWrapper());
        assertEquals((short) 0, mock.getShort());
        assertEquals(Short.valueOf((short) 0), mock.getShortWrapper());
        assertEquals((char) 0, mock.getChar());
        assertEquals(Character.valueOf((char) 0), mock.getCharWrapper());
        assertEquals(0, mock.getInt());
        assertEquals(Integer.valueOf(0), mock.getIntWrapper());
        assertEquals(0L, mock.getLong());
        assertEquals(Long.valueOf(0L), mock.getLongWrapper());
        assertEquals(0.0F, mock.getFloat(), 0.0001F);
        assertEquals(Float.valueOf(0.0F), mock.getFloatWrapper());
        assertEquals(0.0D, mock.getDouble(), 0.0001D);
        assertEquals(Double.valueOf(0.0D), mock.getDoubleWrapper());

        assertNotNull(mock.getCollection());
        assertNotNull(mock.getSet());
        assertNotNull(mock.getHashSet());
        assertNotNull(mock.getSortedSet());
        assertNotNull(mock.getTreeSet());
        assertNotNull(mock.getLinkedHashSet());
        assertNotNull(mock.getList());
        assertNotNull(mock.getLinkedList());
        assertNotNull(mock.getArrayList());
        assertNotNull(mock.getMap());
        assertNotNull(mock.getHashMap());
        assertNotNull(mock.getSortedMap());
        assertNotNull(mock.getTreeMap());
        assertNotNull(mock.getLinkedHashMap());

        assertNull(mock.getString());
        assertNull(mock.getObject());
    }

    @Test
    public void shouldHandleInvocationDirectly() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method method = SampleMethods.class.getMethod("getInt");
        when(invocation.getMethod()).thenReturn(method);

        Object result = returnsEmptyValues.answer(invocation);
        assertEquals(0, result);
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsEmptyValues);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertTrue(deserialized instanceof ReturnsEmptyValues);
        assertEquals(0, ((ReturnsEmptyValues) deserialized).returnValueFor(int.class));
    }
}
