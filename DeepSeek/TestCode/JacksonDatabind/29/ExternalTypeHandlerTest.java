package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class ExternalTypeHandlerTest {

    @Test
    public void testStartReturnsNewHandler() {
        ExtTypedProperty[] props = new ExtTypedProperty[0];
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, null, null);
        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    @Test
    public void testHandleTypePropertyValueUnknownProperty() throws IOException {
        ExtTypedProperty[] props = new ExtTypedProperty[0];
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[0], new TokenBuffer[0]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertFalse(handler.handleTypePropertyValue(jp, ctxt, "unknown", new Object()));
    }

    @Test
    public void testHandleTypePropertyValueNotTypeProperty() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("someProp", 0);
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[1]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertFalse(handler.handleTypePropertyValue(jp, ctxt, "someProp", new Object()));
    }

    @Test
    public void testHandleTypePropertyValueTypePropertyNoBean() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("typeProp", 0);
        String[] typeIds = new String[1];
        TokenBuffer[] tokens = new TokenBuffer[1];
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokens);
        JsonParser jp = mock(JsonParser.class);
        when(jp.getText()).thenReturn("typeId");
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertTrue(handler.handleTypePropertyValue(jp, ctxt, "typeProp", null));
        assertEquals("typeId", typeIds[0]);
    }

    @Test
    public void testHandleTypePropertyValueTypePropertyWithBeanAndToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("typeProp", 0);
        String[] typeIds = new String[1];
        TokenBuffer tokens = mock(TokenBuffer.class);
        TokenBuffer[] tokenArr = new TokenBuffer[]{tokens};
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokenArr);
        JsonParser jp = mock(JsonParser.class);
        when(jp.getText()).thenReturn("typeId");
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        assertTrue(handler.handleTypePropertyValue(jp, ctxt, "typeProp", bean));
        assertNull(tokenArr[0]);
    }

    @Test
    public void testHandlePropertyValueUnknownProperty() throws IOException {
        ExtTypedProperty[] props = new ExtTypedProperty[0];
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[0], new TokenBuffer[0]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertFalse(handler.handlePropertyValue(jp, ctxt, "unknown", new Object()));
    }

    @Test
    public void testHandlePropertyValueTypeProperty() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("typeProp", 0);
        String[] typeIds = new String[1];
        TokenBuffer[] tokens = new TokenBuffer[1];
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokens);
        JsonParser jp = mock(JsonParser.class);
        when(jp.getText()).thenReturn("typeId");
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertTrue(handler.handlePropertyValue(jp, ctxt, "typeProp", null));
        assertEquals("typeId", typeIds[0]);
    }

    @Test
    public void testHandlePropertyValueNonTypeProperty() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("valueProp", 0);
        String[] typeIds = new String[1];
        TokenBuffer[] tokens = new TokenBuffer[1];
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokens);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        assertTrue(handler.handlePropertyValue(jp, ctxt, "valueProp", null));
        assertNotNull(tokens[0]);
    }

    @Test
    public void testHandlePropertyValueTypePropertyWithBeanAndToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("typeProp", 0);
        String[] typeIds = new String[1];
        TokenBuffer tokens = mock(TokenBuffer.class);
        TokenBuffer[] tokenArr = new TokenBuffer[]{tokens};
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokenArr);
        JsonParser jp = mock(JsonParser.class);
        when(jp.getText()).thenReturn("typeId");
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        assertTrue(handler.handlePropertyValue(jp, ctxt, "typeProp", bean));
        assertNull(typeIds[0]);
        assertNull(tokenArr[0]);
    }

    @Test
    public void testHandlePropertyValueNonTypePropertyWithBeanAndTypeId() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("valueProp", 0);
        String[] typeIds = new String[]{"typeId"};
        TokenBuffer[] tokens = new TokenBuffer[1];
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokens);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        assertTrue(handler.handlePropertyValue(jp, ctxt, "valueProp", bean));
        assertNull(typeIds[0]);
        assertNull(tokens[0]);
    }

    @Test
    public void testCompleteMissingBothTypeAndToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[1]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        Object result = handler.complete(jp, ctxt, bean);
        assertSame(bean, result);
    }

    @Test
    public void testCompleteMissingTypeIdWithScalarToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        TokenBuffer tokens = mock(TokenBuffer.class);
        when(tokens.firstToken()).thenReturn(JsonToken.VALUE_STRING);
        JsonParser bufferedParser = mock(JsonParser.class);
        when(tokens.asParser(any(JsonParser.class))).thenReturn(bufferedParser);
        when(bufferedParser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(prop.getType()).thenReturn(String.class);
        Object resultObj = "result";
        when(TypeDeserializer.deserializeIfNatural(eq(bufferedParser), any(DeserializationContext.class), eq(String.class))).thenReturn(resultObj);
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        Object result = handler.complete(jp, ctxt, bean);
        assertSame(bean, result);
        verify(prop).set(bean, resultObj);
    }

    @Test
    public void testCompleteMissingTypeIdWithScalarTokenNaturalNull() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        TokenBuffer tokens = mock(TokenBuffer.class);
        when(tokens.firstToken()).thenReturn(JsonToken.VALUE_STRING);
        JsonParser bufferedParser = mock(JsonParser.class);
        when(tokens.asParser(any(JsonParser.class))).thenReturn(bufferedParser);
        when(bufferedParser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(prop.getType()).thenReturn(String.class);
        when(TypeDeserializer.deserializeIfNatural(eq(bufferedParser), any(DeserializationContext.class), eq(String.class))).thenReturn(null);
        when(extProp.hasDefaultType()).thenReturn(false);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.mappingException(anyString(), anyString())).thenReturn(new JsonMappingException(null, "test"));
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        Object bean = new Object();
        try {
            handler.complete(jp, ctxt, bean);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCompleteMissingTypeIdWithScalarTokenDefaultType() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        TokenBuffer tokens = mock(TokenBuffer.class);
        when(tokens.firstToken()).thenReturn(JsonToken.VALUE_STRING);
        JsonParser bufferedParser = mock(JsonParser.class);
        when(tokens.asParser(any(JsonParser.class))).thenReturn(bufferedParser);
        when(bufferedParser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(prop.getType()).thenReturn(String.class);
        when(TypeDeserializer.deserializeIfNatural(eq(bufferedParser), any(DeserializationContext.class), eq(String.class))).thenReturn(null);
        when(extProp.hasDefaultType()).thenReturn(true);
        when(extProp.getDefaultTypeId()).thenReturn("defaultType");
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        Object result = handler.complete(jp, ctxt, bean);
        assertSame(bean, result);
    }

    @Test
    public void testCompleteMissingTokenWithTypeId() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("propName");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] typeIds = new String[]{"typeId"};
        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.mappingException(anyString(), anyString(), anyString())).thenReturn(new JsonMappingException(null, "test"));
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, new TokenBuffer[1]);
        JsonParser jp = mock(JsonParser.class);
        Object bean = new Object();
        try {
            handler.complete(jp, ctxt, bean);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCompleteWithTypeIdAndToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] typeIds = new String[]{"typeId"};
        TokenBuffer tokens = mock(TokenBuffer.class);
        TokenBuffer[] tokenArr = new TokenBuffer[]{tokens};
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, tokenArr);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object bean = new Object();
        Object result = handler.complete(jp, ctxt, bean);
        assertSame(bean, result);
    }

    @Test
    public void testCompletePropertyBasedCreatorMissingBoth() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[1]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        when(creator.build(ctxt, buffer)).thenReturn(new Object());
        Object result = handler.complete(jp, ctxt, buffer, creator);
        assertNotNull(result);
    }

    @Test
    public void testCompletePropertyBasedCreatorMissingTypeIdNoDefault() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        when(extProp.hasDefaultType()).thenReturn(false);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        TokenBuffer tokens = mock(TokenBuffer.class);
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.mappingException(anyString(), anyString())).thenReturn(new JsonMappingException(null, "test"));
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        try {
            handler.complete(jp, ctxt, buffer, creator);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCompletePropertyBasedCreatorMissingTypeIdWithDefault() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        when(extProp.hasDefaultType()).thenReturn(true);
        when(extProp.getDefaultTypeId()).thenReturn("defaultType");
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        TokenBuffer tokens = mock(TokenBuffer.class);
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, new String[1], new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        when(creator.build(ctxt, buffer)).thenReturn(new Object());
        Object result = handler.complete(jp, ctxt, buffer, creator);
        assertNotNull(result);
    }

    @Test
    public void testCompletePropertyBasedCreatorMissingTokenWithTypeId() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("propName");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] typeIds = new String[]{"typeId"};
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, new TokenBuffer[1]);
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.mappingException(anyString(), anyString(), anyString())).thenReturn(new JsonMappingException(null, "test"));
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        try {
            handler.complete(jp, ctxt, buffer, creator);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCompletePropertyBasedCreatorWithTypeIdAndToken() throws IOException {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("propName");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExtTypedProperty extProp = new ExtTypedProperty(prop, typeDeser);
        ExtTypedProperty[] props = new ExtTypedProperty[]{extProp};
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] typeIds = new String[]{"typeId"};
        TokenBuffer tokens = mock(TokenBuffer.class);
        ExternalTypeHandler handler = new ExternalTypeHandler(props, map, typeIds, new TokenBuffer[]{tokens});
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        when(creator.findCreatorProperty("propName")).thenReturn(null);
        when(creator.build(ctxt, buffer)).thenReturn(new Object());
        Object result = handler.complete(jp, ctxt, buffer, creator);
        assertNotNull(result);
    }

    @Test
    public void testBuilderAddExternalAndBuild() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("propName");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);
    }

    @Test
    public void testExtTypedPropertyHasTypePropertyName() {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertTrue(extProp.hasTypePropertyName("typeProp"));
        assertFalse(extProp.hasTypePropertyName("other"));
    }

    @Test
    public void testExtTypedPropertyHasDefaultType() {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getDefaultImpl()).thenReturn(null);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertFalse(extProp.hasDefaultType());
        when(typeDeser.getDefaultImpl()).thenReturn(Object.class);
        assertTrue(extProp.hasDefaultType());
    }

    @Test
    public void testExtTypedPropertyGetDefaultTypeId() {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getDefaultImpl()).thenReturn(null);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertNull(extProp.getDefaultTypeId());
        when(typeDeser.getDefaultImpl()).thenReturn(String.class);
        TypeIdResolver idResolver = mock(TypeIdResolver.class);
        when(typeDeser.getTypeIdResolver()).thenReturn(idResolver);
        when(idResolver.idFromValueAndType(null, String.class)).thenReturn("stringType");
        assertEquals("stringType", extProp.getDefaultTypeId());
    }

    @Test
    public void testExtTypedPropertyGetTypePropertyName() {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("typeProp");
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertEquals("typeProp", extProp.getTypePropertyName());
    }

    @Test
    public void testExtTypedPropertyGetProperty() {
        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertSame(prop, extProp.getProperty());
    }
}
