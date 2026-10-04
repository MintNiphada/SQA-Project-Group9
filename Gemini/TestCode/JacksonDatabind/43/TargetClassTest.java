package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ObjectIdValuePropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface CustomAnnotation {
    }

    private ObjectIdReader _reader;
    private SettableBeanProperty _idProperty;
    private JsonDeserializer<Object> _deser;
    private ObjectIdGenerator<?> _generator;
    private ObjectIdResolver _resolver;
    private JavaType _type;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        _type = TypeFactory.defaultInstance().constructType(String.class);
        _deser = Mockito.mock(JsonDeserializer.class);
        _generator = new ObjectIdGenerators.IntSequenceGenerator();
        _resolver = new SimpleObjectIdResolver();
        _idProperty = Mockito.mock(SettableBeanProperty.class);
        _reader = ObjectIdReader.construct(
                _type,
                new PropertyName("idProp"),
                _generator,
                _deser,
                _idProperty,
                _resolver
        );
    }

    @Test
    public void testConstructorAndGetters() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_REQUIRED);
        Assert.assertEquals("idProp", prop.getName());
        Assert.assertEquals(_type, prop.getType());
        Assert.assertNull(prop.getAnnotation(CustomAnnotation.class));
        Assert.assertNull(prop.getMember());
    }

    @Test
    public void testWithName() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        ObjectIdValueProperty propWithNewName = prop.withName(new PropertyName("renamedProp"));
        Assert.assertNotSame(prop, propWithNewName);
        Assert.assertEquals("renamedProp", propWithNewName.getName());
    }

    @Test
    public void testWithValueDeserializer() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> newDeser = Mockito.mock(JsonDeserializer.class);
        ObjectIdValueProperty propWithNewDeser = prop.withValueDeserializer(newDeser);
        Assert.assertNotSame(prop, propWithNewDeser);
        Assert.assertSame(newDeser, propWithNewDeser.getValueDeserializer());
    }

    @Test
    public void testDeserializeSetAndReturnWhenDeserializedIdIsNull() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(_deser.deserialize(parser, ctxt)).thenReturn(null);

        Object result = prop.deserializeSetAndReturn(parser, ctxt, new Object());
        Assert.assertNull(result);
    }

    @Test
    public void testDeserializeSetAndReturnWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object idValue = 123;
        Object targetInstance = new Object();
        Object updatedInstance = new Object();

        Mockito.when(_deser.deserialize(parser, ctxt)).thenReturn(idValue);
        Mockito.when(ctxt.findObjectId(idValue, _generator, _resolver)).thenReturn(roid);
        Mockito.when(_idProperty.setAndReturn(targetInstance, idValue)).thenReturn(updatedInstance);

        Object result = prop.deserializeSetAndReturn(parser, ctxt, targetInstance);

        Mockito.verify(roid).bindItem(targetInstance);
        Mockito.verify(_idProperty).setAndReturn(targetInstance, idValue);
        Assert.assertSame(updatedInstance, result);
    }

    @Test
    public void testDeserializeSetAndReturnWithoutIdProperty() throws IOException {
        ObjectIdReader readerWithoutIdProp = ObjectIdReader.construct(
                _type,
                new PropertyName("idProp"),
                _generator,
                _deser,
                null,
                _resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(readerWithoutIdProp, PropertyMetadata.STD_OPTIONAL);
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object idValue = "abc";
        Object targetInstance = new Object();

        Mockito.when(_deser.deserialize(parser, ctxt)).thenReturn(idValue);
        Mockito.when(ctxt.findObjectId(idValue, _generator, _resolver)).thenReturn(roid);

        Object result = prop.deserializeSetAndReturn(parser, ctxt, targetInstance);

        Mockito.verify(roid).bindItem(targetInstance);
        Assert.assertSame(targetInstance, result);
    }

    @Test
    public void testDeserializeAndSetDelegatesToDeserializeSetAndReturn() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object idValue = 456;
        Object targetInstance = new Object();

        Mockito.when(_deser.deserialize(parser, ctxt)).thenReturn(idValue);
        Mockito.when(ctxt.findObjectId(idValue, _generator, _resolver)).thenReturn(roid);

        prop.deserializeAndSet(parser, ctxt, targetInstance);

        Mockito.verify(roid).bindItem(targetInstance);
        Mockito.verify(_idProperty).setAndReturn(targetInstance, idValue);
    }

    @Test
    public void testSetAndReturnWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        Object targetInstance = new Object();
        Object value = "someValue";
        Object returnedInstance = new Object();

        Mockito.when(_idProperty.setAndReturn(targetInstance, value)).thenReturn(returnedInstance);

        Object result = prop.setAndReturn(targetInstance, value);
        Assert.assertSame(returnedInstance, result);
        Mockito.verify(_idProperty).setAndReturn(targetInstance, value);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturnWithoutIdPropertyThrowsException() throws IOException {
        ObjectIdReader readerWithoutIdProp = ObjectIdReader.construct(
                _type,
                new PropertyName("idProp"),
                _generator,
                _deser,
                null,
                _resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(readerWithoutIdProp, PropertyMetadata.STD_OPTIONAL);
        prop.setAndReturn(new Object(), "val");
    }

    @Test
    public void testSetDelegatesToSetAndReturn() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_reader, PropertyMetadata.STD_OPTIONAL);
        Object targetInstance = new Object();
        Object value = "someValue";

        prop.set(targetInstance, value);
        Mockito.verify(_idProperty).setAndReturn(targetInstance, value);
    }
}
