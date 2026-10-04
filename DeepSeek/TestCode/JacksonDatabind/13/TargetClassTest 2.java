package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;

@RunWith(MockitoJUnitRunner.class)
public class ObjectIdValuePropertyTest {

    @Mock
    private ObjectIdReader objectIdReader;
    @Mock
    private PropertyMetadata metadata;
    @Mock
    private JsonDeserializer<Object> valueDeserializer;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private ReadableObjectId roid;
    @Mock
    private SettableBeanProperty idProperty;

    private ObjectIdValueProperty property;

    @Before
    public void setUp() throws Exception {
        when(objectIdReader.propertyName).thenReturn(new PropertyName("id")));
        when(objectIdReader.getIdType()).thenReturn(null);
        when(objectIdReader.getDeserializer()).thenReturn(valueDeserialzer);
        when(objectIdReader.generator).thenReturn(null);
        when(objectIdReader.resolver).thenReturn(null);
        when(objectIdReader.idProperty).thenReturn(idProperty);
        property = new ObjectIdValueProperty(objectIdReader, metadata);
    }

    @Test
    public void testConstruct_WithReaderAndMetadata() {
        assertNotNull(property);
    }

    @Test
    public void test_withName_ReturnsNewInstance() {
        PropertyName newName = new PropertyName("newname");
        ObjectIdValueProperty result = property.withName(newName);
        assertNotNull(result);
        assertNotSame(property, result);
    }

    @Test
    public void test_withValueDeserializer_ReturnsNewInstance() {
        JsonDeserialzer<?> newDeser = mock(JsonDeserialzer.class);
        ObjectIdValueProperty result = property.withValueDeserialzer(newDeser);
        assertNotNull(result);
        assertNotSame(property, result);
    }

    @Test
    public void test_getAnnotation_ReturnsNull() {
        assertNull(property.getAnnotation(Override.class));
    }

    @Test
    public void test_getMember_ReturnsNull() {
        assertNull(property.getMember());
    }

    @Test
    public void test_deserializeAndSet_callsDeserializeSetAndReturn() throws IOException {
        when(valueDeserializer.deserialize(jsonParser, ctxt)).thenReturn("idValue");
        when(ctxt.findObjectId(any(), any(), any())).thenReturn(roid);
        Object instance = new Object();
        property.deserializeAndSet(jsonParser, ctxt, instance);
        verify(valueDeserializer).deserialize(jsonParser, ctxt);
        verify(roid).bindItem(instance);
    }

    @Test
    public void test_deserializeSetAndReturn_idPropNull_returnsInstance() throws IOException {
        when(objectIdReader.idProperty).thenReturn(null);
        String id = "idValue";
        when(valueDeserializer.deserialize(jsonParser, ctxt)).thenReturn(id);
        when(ctxt.findObjectId(id, null, null)).thenReturn(roid);
        Object instance = new Object();
        Object result = property.deserializeSetAndReturn(jsonParser, ctxt, instance);
        assertSame(instance, result);
        verify(roid).bindItem(instance);
        verify(idProperty, never()).setAndReturn(any(), any());
    }

    @Test
    public void test_deserializeSetAndReturn_idPropNotNull_callsSetAndReturn() throws IOException {
        String id = "idValue";
        when(valueDeserializer.deserialize(jsonParser, cxt)).thenReturn(id);
        when(ctxt.findObjectId(id, null, null)).thenReturn(roid);
        Object instance = new Object();
        when(idProperty.setAndReturn(instance, id)).thenReturn(id);
        Object result = property.deserializeSetAndReturn(jsonParser, ctxt, instance);
        assertSame(id, result);
        verify(roid).bindItem(instance);
        verify(idProperty).setAndReturn(instance, id);
    }

    @Test
    public void test_deserializeSetAndReturn_withNullId() throws IOException {
        when(valueDeserializer.deserialize(jsonParser, ctxt)).thenReturn(null);
        when(ctxt.findObjectId(eq(null), any(), any())).thenReturn(roid);
        Object instance = new Object();
        Object result = property.deserializeSetAndReturn(jsonParser, ctxt, instance);
        assertSame(instance, result);
        verify(roid).bindItem(instance);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setAndReturn_idPropNull_throwsException() throws IOException {
        when(objectIdReader.idProperty).thenReturn(null);
        property.setAndReturn(new Object(), "value");
    }

    @Test
    public void test_setAndReturn_idPropNotNull_delegates() throws IOException {
        Object instance = new Object();
        String value = "value";
        when(idProperty.setAndReturn(instance, value)).thenReturn(value);
        Object result = property.setAndReturn(instance, value);
        assertSame(value, result);
        verify(idProperty).setAndReturn(instance, value);
    }

    @Test
    public void test_set_callsSetAndReturn() throws IOException {
        Object instance = new Object();
        String value = "value";
        property.set(instance, value);
        verify(idProperty).setAndReturn(instance, value);
    }

    @Test
    public void test_deprecatedConstructor_WithPropertyName() throws Exception {
        Constructor<ObjectIdValueProperty> ctor =
                ObjectIdValueProperty.class.getDeclaredConstructor(
                        ObjectIdValueProperty.class, PropertyName.class);
        ctor.setAccessible(true);
        PropertyName newName = new PropertyName("deprecated");
        ObjectIdValueProperty result = ctor.newInstance(property, newName);
        assertNotNull(result);
        Field readerField = ObjectIdValueProperty.class.getDeclaredField("_objectIdReader");
        readerField.setAccessible(true);
        assertSame(objectIdReader, readerField.get(result));
    }

    @Test
    public void test_deprecatedConstructor_WithString() throws Exception {
        Constructor<ObjectIdValueProperty> ctor =
                ObjectIdValueProperty.class.getDeclaredConstructor(
                        ObjectIdValueProperty.class, String.class);
        ctor.setAccessible(true);
        ObjectIdValueProperty result = ctor.newInstance(property, "deprecated");
        assertNotNull(result);
        Field readerField = ObjectIdValueProperty.class.getDeclaredField("_objectIdReader");
        readerField.setAccessible(true);
        assertSame(objectIdReader, readerField.get(result));
    }
}
