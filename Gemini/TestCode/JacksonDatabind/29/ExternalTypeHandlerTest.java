package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalTypeHandlerTest {

    private ObjectMapper mapper;
    private SettableBeanProperty prop1;
    private SettableBeanProperty prop2;
    private TypeDeserializer typeDeser1;
    private TypeDeserializer typeDeser2;
    private TypeIdResolver typeIdResolver1;
    private DeserializationContext ctxt;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        prop1 = mock(SettableBeanProperty.class);
        prop2 = mock(SettableBeanProperty.class);
        typeDeser1 = mock(TypeDeserializer.class);
        typeDeser2 = mock(TypeDeserializer.class);
        typeIdResolver1 = mock(TypeIdResolver.class);
        ctxt = mock(DeserializationContext.class);

        when(prop1.getName()).thenReturn("valueProp1");
        when(typeDeser1.getPropertyName()).thenReturn("typeProp1");
        when(typeDeser1.getTypeIdResolver()).thenReturn(typeIdResolver1);

        when(prop2.getName()).thenReturn("valueProp2");
        when(typeDeser2.getPropertyName()).thenReturn("typeProp2");

        when(ctxt.mappingException(anyString(), any(), any())).thenAnswer(inv -> {
            String msg = String.format((String) inv.getArgument(0), inv.getArgument(1), inv.getArgument(2));
            return new JsonMappingException(msg);
        });
        when(ctxt.mappingException(anyString(), any())).thenAnswer(inv -> {
            String msg = String.format((String) inv.getArgument(0), inv.getArgument(1));
            return new JsonMappingException(msg);
        });
    }

    @Test
    public void testBuilderAndStart() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build();
        Assert.assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        Assert.assertNotNull(started);
        Assert.assertNotSame(handler, started);
    }

    @Test
    public void testHandleTypePropertyValueUnknown() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("\"typeA\"");
        parser.nextToken();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknownProp", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandleTypePropertyValueNonTypePropName() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("\"someVal\"");
        parser.nextToken();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "valueProp1", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandleTypePropertyValueBeanNull() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("\"typeA\"");
        parser.nextToken();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "typeProp1", null);
        Assert.assertTrue(handled);
    }

    @Test
    public void testHandlePropertyValueUnknown() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("\"val\"");
        parser.nextToken();

        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknownProp", new Object());
        Assert.assertFalse(handled);
    }

    @Test
    public void testHandlePropertyValueTypePropertyFirstThenValueProperty() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        Object bean = new Object();

        JsonParser typeParser = mapper.getFactory().createParser("\"typeA\"");
        typeParser.nextToken();
        boolean handledType = handler.handlePropertyValue(typeParser, ctxt, "typeProp1", bean);
        Assert.assertTrue(handledType);

        JsonParser valParser = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        valParser.nextToken();
        boolean handledVal = handler.handlePropertyValue(valParser, ctxt, "valueProp1", bean);
        Assert.assertTrue(handledVal);

        verify(prop1).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testHandlePropertyValueValuePropertyFirstThenTypePropertyValue() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        Object bean = new Object();

        JsonParser valParser = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        valParser.nextToken();
        boolean handledVal = handler.handlePropertyValue(valParser, ctxt, "valueProp1", bean);
        Assert.assertTrue(handledVal);

        JsonParser typeParser = mapper.getFactory().createParser("\"typeA\"");
        typeParser.nextToken();
        boolean handledType = handler.handleTypePropertyValue(typeParser, ctxt, "typeProp1", bean);
        Assert.assertTrue(handledType);

        verify(prop1).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testCompleteBothNull() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object bean = new Object();
        Object result = handler.complete(parser, ctxt, bean);
        Assert.assertSame(bean, result);
        verify(prop1, never()).deserializeAndSet(any(), any(), any());
    }

    @Test
    public void testCompleteMissingPropertyForTypeId() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser typeParser = mapper.getFactory().createParser("\"typeA\"");
        typeParser.nextToken();
        handler.handlePropertyValue(typeParser, ctxt, "typeProp1", null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        try {
            handler.complete(parser, ctxt, new Object());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Missing property"));
        }
    }

    @Test
    public void testCompleteMissingTypeIdWithoutDefault() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser valParser = mapper.getFactory().createParser("{\"k\":\"v\"}");
        valParser.nextToken();
        handler.handlePropertyValue(valParser, ctxt, "valueProp1", null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        try {
            handler.complete(parser, ctxt, new Object());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Missing external type id"));
        }
    }

    @Test
    public void testCompleteMissingTypeIdWithDefault() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        Mockito.doReturn(String.class).when(typeDeser1).getDefaultImpl();
        when(typeIdResolver1.idFromValueAndType(null, String.class)).thenReturn("defaultType");

        JsonParser valParser = mapper.getFactory().createParser("123");
        valParser.nextToken();
        handler.handlePropertyValue(valParser, ctxt, "valueProp1", null);

        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        when(prop1.getType()).thenReturn(intType);

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object bean = new Object();
        handler.complete(parser, ctxt, bean);

        verify(prop1).deserializeAndSet(any(JsonParser.class), eq(ctxt), eq(bean));
    }

    @Test
    public void testCompleteNaturalTypeMatching() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        when(prop1.getType()).thenReturn(strType);

        JsonParser valParser = mapper.getFactory().createParser("\"naturalString\"");
        valParser.nextToken();
        handler.handlePropertyValue(valParser, ctxt, "valueProp1", null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object bean = new Object();
        handler.complete(parser, ctxt, bean);

        verify(prop1).set(eq(bean), eq("naturalString"));
    }

    @Test
    public void testCompleteCreatorBothMissing() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object bean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(bean);

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);

        Assert.assertSame(bean, result);
    }

    @Test
    public void testCompleteCreatorMissingPropertyError() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser typeParser = mapper.getFactory().createParser("\"typeA\"");
        typeParser.nextToken();
        handler.handlePropertyValue(typeParser, ctxt, "typeProp1", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);

        JsonParser parser = mapper.getFactory().createParser("{}");
        try {
            handler.complete(parser, ctxt, buffer, creator);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Missing property"));
        }
    }

    @Test
    public void testCompleteCreatorMissingTypeIdError() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        JsonParser valParser = mapper.getFactory().createParser("{\"k\":\"v\"}");
        valParser.nextToken();
        handler.handlePropertyValue(valParser, ctxt, "valueProp1", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);

        JsonParser parser = mapper.getFactory().createParser("{}");
        try {
            handler.complete(parser, ctxt, buffer, creator);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Missing external type id"));
        }
    }

    @Test
    public void testCompleteCreatorWithCreatorAndNonCreatorProps() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        builder.addExternal(prop2, typeDeser2);
        ExternalTypeHandler handler = builder.build().start();

        when(prop1.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn("val1");
        when(prop2.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn("val2");

        JsonParser p1Type = mapper.getFactory().createParser("\"type1\"");
        p1Type.nextToken();
        handler.handlePropertyValue(p1Type, ctxt, "typeProp1", null);

        JsonParser p1Val = mapper.getFactory().createParser("\"v1\"");
        p1Val.nextToken();
        handler.handlePropertyValue(p1Val, ctxt, "valueProp1", null);

        JsonParser p2Type = mapper.getFactory().createParser("\"type2\"");
        p2Type.nextToken();
        handler.handlePropertyValue(p2Type, ctxt, "typeProp2", null);

        JsonParser p2Val = mapper.getFactory().createParser("\"v2\"");
        p2Val.nextToken();
        handler.handlePropertyValue(p2Val, ctxt, "valueProp2", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);

        SettableBeanProperty creatorProp = mock(SettableBeanProperty.class);
        when(creator.findCreatorProperty("valueProp1")).thenReturn(creatorProp);
        when(creator.findCreatorProperty("valueProp2")).thenReturn(null);

        Object builtBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(builtBean);

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);

        Assert.assertSame(builtBean, result);
        verify(buffer).assignParameter(prop1, "val1");
        verify(prop2).set(builtBean, "val2");
    }

    @Test
    public void testCompleteCreatorWithDefaultType() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop1, typeDeser1);
        ExternalTypeHandler handler = builder.build().start();

        Mockito.doReturn(String.class).when(typeDeser1).getDefaultImpl();
        when(typeIdResolver1.idFromValueAndType(null, String.class)).thenReturn("defaultType");
        when(prop1.deserialize(any(JsonParser.class), eq(ctxt))).thenReturn("valDefault");

        JsonParser valParser = mapper.getFactory().createParser("\"val\"");
        valParser.nextToken();
        handler.handlePropertyValue(valParser, ctxt, "valueProp1", null);

        PropertyValueBuffer buffer = mock(PropertyValueBuffer.class);
        PropertyBasedCreator creator = mock(PropertyBasedCreator.class);
        Object builtBean = new Object();
        when(creator.build(ctxt, buffer)).thenReturn(builtBean);
        when(creator.findCreatorProperty("valueProp1")).thenReturn(null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        Object result = handler.complete(parser, ctxt, buffer, creator);

        Assert.assertSame(builtBean, result);
        verify(prop1).set(builtBean, "valDefault");
    }
}
