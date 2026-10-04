package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReturnsDeepStubsTest {

    interface City {
        String getZipCode();
    }

    interface Address {
        City getCity();
        String getStreet();
    }

    interface MultiBoundInterface<T extends List<?> & Comparable<?>> {
        T getBounded();
    }

    interface Person {
        Address getAddress();
        int getAge();
        boolean isActive();
        String getName();
        <T extends List<?> & Comparable<?>> T getBounded();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
    }

    interface SimpleGeneric<T> {
        T getValue();
    }

    @Test
    public void should_deep_stub_chained_calls() {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Person person = mock(Person.class, answer);

        Address address = person.getAddress();
        assertNotNull(address);

        City city = address.getCity();
        assertNotNull(city);
    }

    @Test
    public void should_return_same_mock_instance_on_consecutive_calls() {
        Person person = mock(Person.class, new ReturnsDeepStubs());

        Address firstCall = person.getAddress();
        Address secondCall = person.getAddress();

        assertNotNull(firstCall);
        assertSame("Consecutive calls to deep stub should return the same mock instance", firstCall, secondCall);
    }

    @Test
    public void should_return_empty_values_for_unmockable_primitive_types() {
        Person person = mock(Person.class, new ReturnsDeepStubs());

        assertEquals(0, person.getAge());
        assertFalse(person.isActive());
    }

    @Test
    public void should_return_empty_value_for_non_mockable_types() {
        Person person = mock(Person.class, new ReturnsDeepStubs());

        assertEquals("", person.getName());
    }

    @Test
    public void should_handle_generic_bounds_with_extra_interfaces() {
        MultiBoundInterface<?> mock = mock(MultiBoundInterface.class, new ReturnsDeepStubs());

        Object bounded = mock.getBounded();
        assertNotNull(bounded);
        assertTrue("Mock should implement primary bound", bounded instanceof List);
        assertTrue("Mock should implement secondary bound", bounded instanceof Comparable);
    }

    @Test
    public void should_handle_nested_generics_metadata() {
        GenericsNest<?> mock = mock(GenericsNest.class, new ReturnsDeepStubs());

        Set<Number> set = mock.entrySet().iterator().next().getValue();
        assertNotNull(set);

        Number number = set.iterator().next();
        assertNotNull(number);
    }

    @Test
    public void should_allow_overriding_deep_stub_with_explicit_stubbing() {
        Person person = mock(Person.class, new ReturnsDeepStubs());
        Address customAddress = mock(Address.class);
        City customCity = mock(City.class);

        when(person.getAddress()).thenReturn(customAddress);
        when(customAddress.getCity()).thenReturn(customCity);

        assertSame(customAddress, person.getAddress());
        assertSame(customCity, person.getAddress().getCity());
    }

    @Test
    public void should_infer_actual_parameterized_type_correctly() {
        ReturnsDeepStubs returnsDeepStubs = new ReturnsDeepStubs();
        Person person = mock(Person.class, returnsDeepStubs);

        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(person);
        assertNotNull(metadata);
        assertEquals(Person.class, metadata.rawType());
    }

    @Test
    public void should_be_serializable() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(answer);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsDeepStubs);
    }

    @Test
    public void should_work_with_custom_generic_interface() {
        SimpleGeneric<Address> mock = mock(SimpleGeneric.class, new ReturnsDeepStubs());
        Address address = mock.getValue();
        assertNotNull(address);
        assertNotNull(address.getCity());
    }
}
