package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
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

public class ObjectIdValuePropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
    }

    private JavaType _idType;
    private ObjectIdGenerator<?> _generator;
    private ObjectIdResolver _resolver;
    private JsonDeserializer<Object> _deserializer;
    private SettableBeanProperty _idProperty;
    private ObjectIdReader _objectIdReaderWithProp;
    private ObjectIdReader _objectIdReaderNoProp;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        _idType = TypeFactory.defaultInstance().constructType(String.class);
        _generator = Mockito.mock(ObjectIdGenerator.class);
        _resolver = new SimpleObjectIdResolver();
        _deserializer = Mockito.mock(JsonDeserializer.class);
        _idProperty = Mockito.mock(SettableBeanProperty.class);

        _objectIdReaderWithProp = ObjectIdReader.construct(
                _idType,
                new PropertyName("idProp"),
                _generator,
                _deserializer,
                _idProperty,
                _resolver
        );

        _objectIdReaderNoProp = ObjectIdReader.construct(
                _idType,
                new PropertyName("idNoProp"),
                _generator,
                _deserializer,
                null,
                _resolver
        );
    }

    @Test
    public void testConstructorAndBasicGetters() {
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, metadata);

        Assert.assertEquals("idProp", prop.getName());
        Assert.assertEquals(_idType, prop.getType());
        Assert.assertEquals(metadata, prop.getMetadata());
        Assert.assertEquals(_deserializer, prop.getValueDeserializer());
        Assert.assertNull(prop.getAnnotation(TestAnnotation.class));
        Assert.assertNull(prop.getMember());
    }

    @Test
    public void testWithNameAndConstructors() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);

        PropertyName newName = new PropertyName("newName");
        ObjectIdValueProperty propWithName = prop.withName(newName);
        Assert.assertEquals("newName", propWithName.getName());
        Assert.assertSame(prop.getValueDeserializer(), propWithName.getValueDeserializer());

        // Test deprecated String constructor
        ObjectIdValueProperty propWithString = new ObjectIdValueProperty(prop, "stringName");
        Assert.assertEquals("stringName", propWithString.getName());

        // Test deprecated PropertyName constructor
        ObjectIdValueProperty propWithPropName = new ObjectIdValueProperty(prop, new PropertyName("propName"));
        Assert.assertEquals("propName", propWithPropName.getName());
    }

    @Test
    public void testWithValueDeserializer() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> newDeser = Mockito.mock(JsonDeserializer.class);

        ObjectIdValueProperty propWithNewDeser = prop.withValueDeserializer(newDeser);
        Assert.assertSame(newDeser, propWithNewDeser.getValueDeserializer());
        Assert.assertEquals(prop.getName(), propWithNewNewName(propWithNewDeser).getName());
    }

    private ObjectIdValueProperty propWithNewNewName(ObjectIdValueProperty prop) {
        return prop;
    }

    @Test
    public void testDeserializeAndSetWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object instance = new Object();
        Object id = "generated-id-123";
        Object updatedInstance = new Object();

        Mockito.when(_deserializer.deserialize(parser, ctxt)).thenReturn(id);
        Mockito.when(ctxt.findObjectId(id, _generator, _resolver)).thenReturn(roid);
        Mockito.when(_idProperty.setAndReturn(instance, id)).thenReturn(updatedInstance);

        prop.deserializeAndSet(parser, ctxt, instance);

        Mockito.verify(roid).bindItem(instance);
        Mockito.verify(_idProperty).setAndReturn(instance, id);
    }

    @Test
    public void testDeserializeSetAndReturnWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object instance = new Object();
        Object id = "test-id";
        Object returnedInstance = new Object();

        Mockito.when(_deserializer.deserialize(parser, ctxt)).thenReturn(id);
        Mockito.when(ctxt.findObjectId(id, _generator, _resolver)).thenReturn(roid);
        Mockito.when(_idProperty.setAndReturn(instance, id)).thenReturn(returnedInstance);

        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        Mockito.verify(roid).bindItem(instance);
        Mockito.verify(_idProperty).setAndReturn(instance, id);
        Assert.assertSame(returnedInstance, result);
    }

    @Test
    public void testDeserializeSetAndReturnWithoutIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderNoProp, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);

        Object instance = new Object();
        Object id = "test-id-no-prop";

        Mockito.when(_deserializer.deserialize(parser, ctxt)).thenReturn(id);
        Mockito.when(ctxt.findObjectId(id, _generator, _resolver)).thenReturn(roid);

        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        Mockito.verify(roid).bindItem(instance);
        Assert.assertSame(instance, result);
    }

    @Test
    public void testSetWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);

        Object instance = new Object();
        Object value = "value-to-set";

        prop.set(instance, value);

        Mockito.verify(_idProperty).setAndReturn(instance, value);
    }

    @Test
    public void testSetAndReturnWithIdProperty() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderWithProp, PropertyMetadata.STD_OPTIONAL);

        Object instance = new Object();
        Object value = "value-to-set";
        Object updatedInstance = new Object();

        Mockito.when(_idProperty.setAndReturn(instance, value)).thenReturn(updatedInstance);

        Object result = prop.setAndReturn(instance, value);

        Mockito.verify(_idProperty).setAndReturn(instance, value);
        Assert.assertSame(updatedInstance, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetWithoutIdPropertyThrowsException() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderNoProp, PropertyMetadata.STD_OPTIONAL);
        prop.set(new Object(), "val");
    }

    @Test
    public void testSetAndReturnWithoutIdPropertyThrowsException() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReaderNoProp, PropertyMetadata.STD_OPTIONAL);
        try {
            prop.setAndReturn(new Object(), "val");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Should not call set() on ObjectIdProperty that has no SettableBeanProperty"));
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }
}
