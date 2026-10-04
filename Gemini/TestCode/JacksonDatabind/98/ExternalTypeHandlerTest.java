package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ExternalTypeHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBuilderAndStart() {
        JavaType javaType = TypeFactory.defaultInstance().constructType(String.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);
        Assert.assertNotNull(builder);

        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap());
        ExternalTypeHandler handler = builder.build(propMap);
        Assert.assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        Assert.assertNotNull(started);
        Assert.assertNotSame(handler, started);
    }

    @Test
    public void testBuilderAddExternalMultiple() {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop1 = mock(SettableBeanProperty.class);
        when(prop1.getName()).thenReturn("data");
        TypeDeserializer typeDeser1 = mock(TypeDeserializer.class);
        when(typeDeser1.getPropertyName()).thenReturn("type");

        SettableBeanProperty prop2 = mock(SettableBeanProperty.class);
        when(prop2.getName()).thenReturn("data2");
        TypeDeserializer typeDeser2 = mock(TypeDeserializer.class);
        when(typeDeser2.getPropertyName()).thenReturn("type");

        SettableBeanProperty prop3 = mock(SettableBeanProperty.class);
        when(prop3.getName()).thenReturn("data");
        TypeDeserializer typeDeser3 = mock(TypeDeserializer.class);
        when(typeDeser3.getPropertyName()).thenReturn("type3");

        builder.addExternal(prop1, typeDeser1);
        builder.addExternal(prop2, typeDeser2);
        builder.addExternal(prop3, typeDeser3);

        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap());
        ExternalTypeHandler handler = builder.build(propMap);
        Assert.assertNotNull(handler);
    }

    @Test
    public void testHandleTypePropertyValueUnknown() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler handler = ExternalTypeHandler.builder(javaType).build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        JsonParser parser = mapper.createParser("\"test\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknown", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandlePropertyValueUnknown() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler handler = ExternalTypeHandler.builder(javaType).build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        JsonParser parser = mapper.createParser("\"test\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknown", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandlePropertyValueSingleAndDeserialize() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("value");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("extType");

        builder.addExternal(prop, typeDeser);
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap());
        ExternalTypeHandler handler = builder.build(propMap).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object targetBean = new Object();

        JsonParser p1 = mapper.createParser("\"myType\"");
        p1.nextToken();
        boolean handledType = handler.handlePropertyValue(p1, ctxt, "extType", targetBean);
        Assert.assertTrue(handledType);

        JsonParser p2 = mapper.createParser("{\"k\":\"v\"}");
        p2.nextToken();
        boolean handledValue = handler.handlePropertyValue(p2, ctxt, "value", targetBean);
        Assert.assertTrue(handledValue);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(targetBean));
    }

    @Test
    public void testHandlePropertyValueListTypeAndValues() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop1 = mock(SettableBeanProperty.class);
        when(prop1.getName()).thenReturn("val1");
        TypeDeserializer typeDeser1 = mock(TypeDeserializer.class);
        when(typeDeser1.getPropertyName()).thenReturn("type");

        SettableBeanProperty prop2 = mock(SettableBeanProperty.class);
        when(prop2.getName()).thenReturn("val2");
        TypeDeserializer typeDeser2 = mock(TypeDeserializer.class);
        when(typeDeser2.getPropertyName()).thenReturn("type");

        builder.addExternal(prop1, typeDeser1);
        builder.addExternal(prop2, typeDeser2);

        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object targetBean = new Object();

        JsonParser pType = mapper.createParser("\"commonType\"");
        pType.nextToken();
        boolean handledType = handler.handlePropertyValue(pType, ctxt, "type", targetBean);
        Assert.assertTrue(handledType);

        JsonParser pVal1 = mapper.createParser("{\"a\":1}");
        pVal1.nextToken();
        boolean handledVal1 = handler.handlePropertyValue(pVal1, ctxt, "val1", targetBean);
        Assert.assertTrue(handledVal1);

        verify(prop1, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(targetBean));
    }

    @Test
    public void testHandleTypePropertyValueList() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop1 = mock(SettableBeanProperty.class);
        when(prop1.getName()).thenReturn("val1");
        TypeDeserializer typeDeser1 = mock(TypeDeserializer.class);
        when(typeDeser1.getPropertyName()).thenReturn("type");

        SettableBeanProperty prop2 = mock(SettableBeanProperty.class);
        when(prop2.getName()).thenReturn("val2");
        TypeDeserializer typeDeser2 = mock(TypeDeserializer.class);
        when(typeDeser2.getPropertyName()).thenReturn("type");

        builder.addExternal(prop1, typeDeser1);
        builder.addExternal(prop2, typeDeser2);

        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pVal1 = mapper.createParser("123");
        pVal1.nextToken();
        handler.handlePropertyValue(pVal1, ctxt, "val1", null);

        JsonParser pType = mapper.createParser("\"theType\"");
        pType.nextToken();
        Object targetBean = new Object();
        boolean handled = handler.handleTypePropertyValue(pType, ctxt, "type", targetBean);
        Assert.assertTrue(handled);

        verify(prop1, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(targetBean));
    }

    @Test
    public void testHandleTypePropertyValueSingle() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pVal = mapper.createParser("\"data\"");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        JsonParser pType = mapper.createParser("\"customType\"");
        pType.nextToken();
        Object targetBean = new Object();
        boolean handled = handler.handleTypePropertyValue(pType, ctxt, "type", targetBean);
        Assert.assertTrue(handled);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(targetBean));
    }

    @Test
    public void testCompleteMissingBoth() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        JsonParser parser = mapper.createParser("{}");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object bean = new Object();

        Object res = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, res);
    }

    @Test
    public void testCompleteMissingTokensWithoutFailFeature() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.isRequired()).thenReturn(false);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)).thenReturn(false);

        JsonParser pType = mapper.createParser("\"myType\"");
        pType.nextToken();
        handler.handlePropertyValue(pType, ctxt, "type", null);

        JsonParser parser = mapper.createParser("{}");
        Object bean = new Object();
        Object res = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, res);
    }

    @Test
    public void testCompleteMissingTokensWithFailFeature() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.isRequired()).thenReturn(true);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mock(DeserializationContext.class);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)).thenReturn(true);

        JsonParser pType = mapper.createParser("\"myType\"");
        pType.nextToken();
        handler.handlePropertyValue(pType, ctxt, "type", null);

        JsonParser parser = mapper.createParser("{}");
        Object bean = new Object();
        handler.complete(parser, ctxt, bean);

        verify(ctxt, times(1)).reportInputMismatch(eq(Object.class), any(String.class), eq("val"), eq("type"));
    }

    @Test
    public void testCompleteWithDefaultImpl() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getType()).thenReturn(TypeFactory.defaultInstance().constructType(String.class));
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");
        doReturn(String.class).when(typeDeser).getDefaultImpl();
        TypeIdResolver idResolver = mock(TypeIdResolver.class);
        when(idResolver.idFromValueAndType(null, String.class)).thenReturn("defaultType");
        when(typeDeser.getTypeIdResolver()).thenReturn(idResolver);

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser pVal = mapper.createParser("{\"k\":\"v\"}");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        JsonParser parser = mapper.createParser("{}");
        Object bean = new Object();
        handler.complete(parser, ctxt, bean);

        verify(prop, times(1)).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testCompleteMissingDefaultTypeReportsMismatch() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getType()).thenReturn(TypeFactory.defaultInstance().constructType(Object.class));
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");
        when(typeDeser.getDefaultImpl()).thenReturn(null);

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mock(DeserializationContext.class);
        JsonParser pVal = mapper.createParser("\"plainString\"");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        JsonParser parser = mapper.createParser("{}");
        Object bean = new Object();
        handler.complete(parser, ctxt, bean);

        verify(ctxt, times(1)).reportInputMismatch(eq(Object.class), any(String.class), eq("type"));
    }

    @Test
    public void testCompleteCreatorMissingTokens() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getCreatorIndex()).thenReturn(0);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mock(DeserializationContext.class);
        JsonParser pType = mapper.createParser("\"myType\"");
        pType.nextToken();
        handler.handlePropertyValue(pType, ctxt, "type", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        when(creator.build(ctxt, buffer)).thenReturn(new Object());

        JsonParser parser = mapper.createParser("{}");
        handler.complete(parser, ctxt, buffer, creator);

        verify(ctxt, times(1)).reportInputMismatch(eq(javaType), any(String.class), eq("val"), eq("type"));
    }

    @Test
    public void testCompleteCreatorMissingBoth() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getCreatorIndex()).thenReturn(-1);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object expectedBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(expectedBean);

        JsonParser parser = mapper.createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);
        Assert.assertSame(expectedBean, result);
    }

    @Test
    public void testCompleteCreatorWithProperties() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getCreatorIndex()).thenReturn(0);
        when(prop.deserialize(any(JsonParser.class), any(DeserializationContext.class))).thenReturn("deserializedValue");

        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        SettableBeanProperty typeProp = mock(SettableBeanProperty.class);
        when(typeProp.getName()).thenReturn("type");
        when(typeProp.getCreatorIndex()).thenReturn(1);

        builder.addExternal(prop, typeDeser);

        BeanPropertyMap propMap = mock(BeanPropertyMap.class);
        when(propMap.find("type")).thenReturn(typeProp);

        ExternalTypeHandler handler = builder.build(propMap).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pType = mapper.createParser("\"type123\"");
        pType.nextToken();
        handler.handlePropertyValue(pType, ctxt, "type", null);

        JsonParser pVal = mapper.createParser("{\"foo\":\"bar\"}");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object expectedBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(expectedBean);

        JsonParser parser = mapper.createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);

        Assert.assertSame(expectedBean, result);
        verify(buffer, times(1)).assignParameter(prop, "deserializedValue");
        verify(buffer, times(1)).assignParameter(typeProp, "type123");
    }

    @Test
    public void testCompleteCreatorMissingTypeWithDefault() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getCreatorIndex()).thenReturn(-1);
        when(prop.deserialize(any(JsonParser.class), any(DeserializationContext.class))).thenReturn("defaultVal");

        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");
        doReturn(String.class).when(typeDeser).getDefaultImpl();
        TypeIdResolver idResolver = mock(TypeIdResolver.class);
        when(idResolver.idFromValueAndType(null, String.class)).thenReturn("defType");
        when(typeDeser.getTypeIdResolver()).thenReturn(idResolver);

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pVal = mapper.createParser("123");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object expectedBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(expectedBean);

        JsonParser parser = mapper.createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);

        Assert.assertSame(expectedBean, result);
        verify(prop, times(1)).set(expectedBean, "defaultVal");
    }

    @Test
    public void testCompleteCreatorMissingTypeWithoutDefault() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        when(prop.getCreatorIndex()).thenReturn(0);

        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");
        when(typeDeser.getDefaultImpl()).thenReturn(null);

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mock(DeserializationContext.class);

        JsonParser pVal = mapper.createParser("123");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        when(creator.build(ctxt, buffer)).thenReturn(new Object());

        JsonParser parser = mapper.createParser("{}");
        handler.complete(parser, ctxt, buffer, creator);

        verify(ctxt, times(1)).reportInputMismatch(eq(javaType), any(String.class), eq("type"));
    }

    @Test
    public void testDeserializeNullToken() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Object.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);

        SettableBeanProperty prop = mock(SettableBeanProperty.class);
        when(prop.getName()).thenReturn("val");
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        when(typeDeser.getPropertyName()).thenReturn("type");

        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, java.util.List<com.fasterxml.jackson.databind.PropertyName>>emptyMap())).start();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object bean = new Object();

        JsonParser pType = mapper.createParser("\"type1\"");
        pType.nextToken();
        handler.handlePropertyValue(pType, ctxt, "type", bean);

        JsonParser pVal = mapper.createParser("null");
        pVal.nextToken();
        handler.handlePropertyValue(pVal, ctxt, "val", bean);

        verify(prop, times(1)).set(bean, null);
    }
}
