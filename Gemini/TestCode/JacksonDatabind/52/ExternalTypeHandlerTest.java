package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalTypeHandlerTest {

    private SettableBeanProperty prop;
    private TypeDeserializer typeDeser;
    private TypeIdResolver idResolver;
    private DeserializationContext ctxt;
    private JsonParser parser;

    @Before
    public void setUp() {
        prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("dataProp");
        when(prop.getCreatorIndex()).thenReturn(-1);

        typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");

        idResolver = mock(TypeIdResolver.class);
        when(typeDeser.getTypeIdResolver()).thenReturn(idResolver);

        ctxt = mock(DeserializationContext.class);
        parser = mock(JsonParser.class);
    }

    @Test
    public void testBuilderAndStart() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();
        Assert.assertNotNull(handler);

        ExternalTypeHandler copy = handler.start();
        Assert.assertNotNull(copy);
        Assert.assertNotSame(handler, copy);
    }

    @Test
    public void testHandleTypePropertyValueUnknownProp() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknownProp", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandleTypePropertyValueMatchingDataPropName() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "dataProp", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandleTypePropertyValueWhenNoTokensBuffered() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getText()).thenReturn("extTypeId");
        Object bean = new Object();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "typeProp", bean);
        Assert.assertTrue(handled);
    }

    @Test
    public void testHandleTypePropertyValueWhenTokensAlreadyBuffered() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("payloadValue");
        Object bean = new Object();

        handler.handlePropertyValue(parser, ctxt, "dataProp", bean);

        when(parser.getText()).thenReturn("typeIdVal");
        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "typeProp", bean);
        Assert.assertTrue(handled);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testHandlePropertyValueUnknownProp() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknownProp", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandlePropertyValueTypePropertyFirst() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getText()).thenReturn("typeIdVal");
        boolean handled = handler.handlePropertyValue(parser, ctxt, "typeProp", new Object());
        Assert.assertTrue(handled);
        verify(parser, times(1)).skipChildren();
    }

    @Test
    public void testHandlePropertyValueDataPropertyFirstThenTypeProperty() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        Object bean = new Object();
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("data");

        boolean handled1 = handler.handlePropertyValue(parser, ctxt, "dataProp", bean);
        Assert.assertTrue(handled1);

        when(parser.getText()).thenReturn("typeId");
        boolean handled2 = handler.handlePropertyValue(parser, ctxt, "typeProp", bean);
        Assert.assertTrue(handled2);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testHandlePropertyValueTypePropertyFirstThenDataProperty() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        Object bean = new Object();
        when(parser.getText()).thenReturn("typeId");
        handler.handlePropertyValue(parser, ctxt, "typeProp", bean);

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(parser.getIntValue()).thenReturn(123);
        boolean handled = handler.handlePropertyValue(parser, ctxt, "dataProp", bean);
        Assert.assertTrue(handled);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testCompleteBothNull() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        Object bean = new Object();
        Object result = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, result);
        verify(prop, never()).deserializeAndSet(any(JsonParser.class), any(DeserializationContext.class), any());
    }

    @Test
    public void testCompleteMissingPropertyForExternalTypeId() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getText()).thenReturn("typeIdVal");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        doAnswer(invocation -> {
            throw new JsonMappingException("Missing property");
        }).when(ctxt).reportMappingException(anyString(), any(), any());

        try {
            handler.complete(parser, ctxt, new Object());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            verify(ctxt, times(1)).reportMappingException(eq("Missing property '%s' for external type id '%s'"), eq("dataProp"), eq("typeProp"));
        }
    }

    @Test
    public void testCompleteMissingTypeIdWithoutDefaultImpl() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("data");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(prop.getType()).thenReturn(TypeFactory.defaultInstance().constructType(Object.class));
        when(typeDeser.getDefaultImpl()).thenReturn(null);

        doAnswer(invocation -> {
            throw new JsonMappingException("Missing external type id property");
        }).when(ctxt).reportMappingException(anyString(), any());

        try {
            handler.complete(parser, ctxt, new Object());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            verify(ctxt, times(1)).reportMappingException(eq("Missing external type id property '%s'"), eq("typeProp"));
        }
    }

    @Test
    public void testCompleteMissingTypeIdWithDefaultImpl() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("someText");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        Class rawCls = String.class;
        when(prop.getType()).thenReturn(TypeFactory.defaultInstance().constructType(rawCls));
        when(typeDeser.getDefaultImpl()).thenReturn(rawCls);
        when(idResolver.idFromValueAndType(null, rawCls)).thenReturn("stringType");

        Object bean = new Object();
        Object result = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, result);
        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testCompleteWithNaturalType() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("naturalString");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        when(prop.getType()).thenReturn(stringType);

        Object bean = new Object();
        Object result = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, result);
        verify(prop, times(1)).set(eq(bean), eq("naturalString"));
    }

    @Test
    public void testDeserializeAndSetWithNullToken() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(parser.getText()).thenReturn("typeId");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        Object bean = new Object();
        handler.complete(parser, ctxt, bean);
        verify(prop, times(1)).set(eq(bean), Mockito.isNull());
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorBothNull() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object createdBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(createdBean);

        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(createdBean, result);
        verify(creator, times(1)).build(ctxt, buffer);
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorMissingTypeIdNoDefault() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("data");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(typeDeser.getDefaultImpl()).thenReturn(null);
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);

        doAnswer(invocation -> {
            throw new JsonMappingException("Missing external type id property");
        }).when(ctxt).reportMappingException(anyString(), any());

        try {
            handler.complete(parser, ctxt, buffer, creator);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            verify(ctxt, times(1)).reportMappingException(eq("Missing external type id property '%s'"), eq("typeProp"));
        }
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorMissingProperty() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getText()).thenReturn("typeIdVal");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);

        doAnswer(invocation -> {
            throw new JsonMappingException("Missing property");
        }).when(ctxt).reportMappingException(anyString(), any(), any());

        try {
            handler.complete(parser, ctxt, buffer, creator);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            verify(ctxt, times(1)).reportMappingException(eq("Missing property '%s' for external type id '%s'"), eq("dataProp"), eq("typeProp"));
        }
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorCreatorProperty() throws IOException {
        when(prop.getCreatorIndex()).thenReturn(0);
        when(prop.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn("deserializedVal");

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("dataValue");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(parser.getText()).thenReturn("typeIdVal");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object createdBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(createdBean);

        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(createdBean, result);

        verify(buffer, times(1)).assignParameter(eq(prop), eq("deserializedVal"));
        verify(prop, never()).set(any(), any());
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorNonCreatorProperty() throws IOException {
        when(prop.getCreatorIndex()).thenReturn(-1);
        when(prop.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn("deserializedVal");

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("dataValue");
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(parser.getText()).thenReturn("typeIdVal");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object createdBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(createdBean);

        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(createdBean, result);

        verify(buffer, never()).assignParameter(eq(prop), any());
        verify(prop, times(1)).set(eq(createdBean), eq("deserializedVal"));
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorAndNullToken() throws IOException {
        when(prop.getCreatorIndex()).thenReturn(0);

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        when(parser.getText()).thenReturn("typeIdVal");
        handler.handlePropertyValue(parser, ctxt, "typeProp", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object createdBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(createdBean);

        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(createdBean, result);
        verify(buffer, times(1)).assignParameter(eq(prop), Mockito.isNull());
    }

    @Test
    public void testCompleteWithPropertyBasedCreatorDefaultTypeId() throws IOException {
        when(prop.getCreatorIndex()).thenReturn(0);
        Class rawCls = Integer.class;
        when(typeDeser.getDefaultImpl()).thenReturn(rawCls);
        when(idResolver.idFromValueAndType(null, rawCls)).thenReturn("intType");
        when(prop.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn(42);

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build().start();

        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(parser.getIntValue()).thenReturn(42);
        handler.handlePropertyValue(parser, ctxt, "dataProp", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object createdBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(createdBean);

        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(createdBean, result);
        verify(buffer, times(1)).assignParameter(eq(prop), eq(42));
    }
}
