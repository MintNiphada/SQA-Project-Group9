package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReturnsDeepStubsTest {

    interface Address {
        City getCity();
        String getStreet();
        int getZipCode();
    }

    interface City {
        String getName();
        Coordinates getCoordinates();
    }

    interface Coordinates {
        double getLatitude();
        double getLongitude();
    }

    interface Person {
        Address getAddress();
        String getName();
        int getAge();
        boolean isActive();
        final class FinalClass {}
        FinalClass getFinal();
    }

    interface Container<T> {
        T getItem();
    }

    interface AddressContainer extends Container<Address> {
    }

    interface TwoBounds<T extends List<?> & Cloneable> {
        T getBounded();
    }

    @Test
    public void should_create_deep_stub_mock() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        assertNotNull(person.getAddress());
        assertNotNull(person.getAddress().getCity());
        assertNotNull(person.getAddress().getCity().getCoordinates());
    }

    @Test
    public void should_return_empty_value_for_unmockable_types() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        assertEquals("", person.getName());
        assertEquals(0, person.getAge());
        assertFalse(person.isActive());
        assertEquals("", person.getAddress().getStreet());
        assertEquals(0, person.getAddress().getZipCode());
        assertEquals(0.0, person.getAddress().getCity().getCoordinates().getLatitude(), 0.0001);
    }

    @Test
    public void should_return_same_mock_instance_on_repeated_calls() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        Address address1 = person.getAddress();
        Address address2 = person.getAddress();
        assertSame(address1, address2);

        City city1 = person.getAddress().getCity();
        City city2 = person.getAddress().getCity();
        assertSame(city1, city2);
    }

    @Test
    public void should_support_overriding_stubbed_value_at_leaf() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        when(person.getAddress().getCity().getName()).thenReturn("New York");

        assertEquals("New York", person.getAddress().getCity().getName());
    }

    @Test
    public void should_support_overriding_stubbed_value_at_intermediate_level() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        City customCity = mock(City.class);
        when(customCity.getName()).thenReturn("San Francisco");
        when(person.getAddress().getCity()).thenReturn(customCity);

        assertSame(customCity, person.getAddress().getCity());
        assertEquals("San Francisco", person.getAddress().getCity().getName());
    }

    @Test
    public void should_handle_generic_return_types_correctly() {
        AddressContainer container = mock(AddressContainer.class, Mockito.RETURNS_DEEP_STUBS);
        Address address = container.getItem();
        assertNotNull(address);
        assertNotNull(address.getCity());
    }

    @Test
    public void should_handle_nested_generics() {
        Map<String, List<Set<String>>> map = mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
        List<Set<String>> list = map.get("key");
        assertNotNull(list);
        Set<String> set = list.get(0);
        assertNotNull(set);
        Iterator<String> iterator = set.iterator();
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void should_handle_extra_interface_bounds() {
        TwoBounds<?> mock = mock(TwoBounds.class, Mockito.RETURNS_DEEP_STUBS);
        Object bounded = mock.getBounded();
        assertNotNull(bounded);
        assertTrue(bounded instanceof List);
        assertTrue(bounded instanceof Cloneable);
    }

    @Test
    public void should_return_null_for_final_class_return_type_if_unmockable() {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        Person.FinalClass result = person.getFinal();
        assertNull(result);
    }

    @Test
    public void should_be_serializable() throws Exception {
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);
        Address address = person.getAddress();
        assertNotNull(address);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Person deserializedPerson = (Person) ois.readObject();

        assertNotNull(deserializedPerson);
        assertNotNull(deserializedPerson.getAddress());
        assertNotNull(deserializedPerson.getAddress().getCity());
    }

    @Test
    public void test_actualParameterizedType_method() throws Throwable {
        ReturnsDeepStubs returnsDeepStubs = new ReturnsDeepStubs();
        Person person = mock(Person.class, Mockito.RETURNS_DEEP_STUBS);

        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(person);
        assertNotNull(metadata);
        assertEquals(Person.class, metadata.rawType());
    }

    @Test
    public void test_serialization_of_ReturnsDeepStubs_instance() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(answer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsDeepStubs);
    }

    @Test
    public void test_deep_stub_delegation_when_type_not_mockable() throws Throwable {
        ReturnsDeepStubs returnsDeepStubs = new ReturnsDeepStubs();
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Person personMock = mock(Person.class);

        Method getNameMethod = Person.class.getMethod("getName");
        when(invocation.getMock()).thenReturn(personMock);
        when(invocation.getMethod()).thenReturn(getNameMethod);

        Object result = returnsDeepStubs.answer(invocation);
        assertEquals("", result);
    }

    @Test
    public void test_deep_stub_delegation_for_primitive_type() throws Throwable {
        ReturnsDeepStubs returnsDeepStubs = new ReturnsDeepStubs();
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Person personMock = mock(Person.class);

        Method getAgeMethod = Person.class.getMethod("getAge");
        when(invocation.getMock()).thenReturn(personMock);
        when(invocation.getMethod()).thenReturn(getAgeMethod);

        Object result = returnsDeepStubs.answer(invocation);
        assertEquals(0, result);
    }
}
