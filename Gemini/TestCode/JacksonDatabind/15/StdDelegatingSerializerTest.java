package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;

public class StdDelegatingSerializerTest {

    static class CustomConverter extends StdConverter<Integer, String> {
        @Override
        public String convert(Integer value) {
            return value == null ? null : "Value: " + value;
        }
    }

    static class SubDelegatingSerializerNoOverride extends StdDelegatingSerializer {
        public SubDelegatingSerializerNoOverride(Converter<?, ?> converter) {
            super(converter);
        }
    }

    static class SubDelegatingSerializerWithOverride extends StdDelegatingSerializer {
        public SubDelegatingSerializerWithOverride(Converter<?, ?> converter) {
            super(converter);
        }

        public SubDelegatingSerializerWithOverride(Converter<Object, ?> converter,
                                                   JavaType delegateType,
                                                   JsonSerializer<?> delegateSerializer) {
            super(converter, delegateType, delegateSerializer);
        }

        @Override
        protected StdDelegatingSerializer withDelegate(Converter<Object, ?> converter,
                                                       JavaType delegateType,
                                                       JsonSerializer<?> delegateSerializer) {
            return new SubDelegatingSerializerWithOverride(converter, delegateType, delegateSerializer);
        }
    }

    static abstract class MockResolvableContextualSchemaAwareSerializer<T>
            extends JsonSerializer<T>
            implements ResolvableSerializer, ContextualSerializer, SchemaAware {
    }

    @Test
    public void testConstructorsAndAccessors() {
        Converter<Integer, String> conv = new CustomConverter();
        StdDelegatingSerializer ser1 = new StdDelegatingSerializer(conv);
        Assert.assertSame(conv, ser1.getConverter());
        Assert.assertNull(ser1.getDelegatee());
        Assert.assertEquals(Object.class, ser1.handledType());

        StdDelegatingSerializer ser2 = new StdDelegatingSerializer(Integer.class, conv);
        Assert.assertSame(conv, ser2.getConverter());
        Assert.assertNull(ser2.getDelegatee());
        Assert.assertEquals(Integer.class, ser2.handledType());

        ObjectMapper mapper = new ObjectMapper();
        JavaType strType = mapper.constructType(String.class);
        JsonSerializer<Object> delSer = mapper.getSerializerProviderInstance().findValueSerializer(String.class);
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;

        StdDelegatingSerializer ser3 = new StdDelegatingSerializer(objConv, strType, delSer);
        Assert.assertSame(objConv, ser3.getConverter());
        Assert.assertSame(delSer, ser3.getDelegatee());
        Assert.assertEquals(String.class, ser3.handledType());
    }

    @Test
    public void testWithDelegateSubclassing() {
        Converter<Integer, String> conv = new CustomConverter();
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<?> mockSer = Mockito.mock(JsonSerializer.class);

        StdDelegatingSerializer base = new StdDelegatingSerializer(conv);
        StdDelegatingSerializer withDel = base.withDelegate(objConv, strType, mockSer);
        Assert.assertNotNull(withDel);
        Assert.assertSame(mockSer, withDel.getDelegatee());

        SubDelegatingSerializerNoOverride subNoOverride = new SubDelegatingSerializerNoOverride(conv);
        try {
            subNoOverride.withDelegate(objConv, strType, mockSer);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("must override 'withDelegate'"));
        }

        SubDelegatingSerializerWithOverride subWithOverride = new SubDelegatingSerializerWithOverride(conv);
        StdDelegatingSerializer created = subWithOverride.withDelegate(objConv, strType, mockSer);
        Assert.assertTrue(created instanceof SubDelegatingSerializerWithOverride);
        Assert.assertSame(mockSer, created.getDelegatee());
    }

    @Test
    public void testResolve() throws Exception {
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Converter<Object, ?> conv = Mockito.mock(Converter.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        // When delegate is null
        StdDelegatingSerializer serNullDel = new StdDelegatingSerializer(conv);
        serNullDel.resolve(provider);

        // When delegate is non-resolvable
        JsonSerializer<?> nonResolvable = Mockito.mock(JsonSerializer.class);
        StdDelegatingSerializer serNonRes = new StdDelegatingSerializer(conv, strType, nonResolvable);
        serNonRes.resolve(provider);

        // When delegate is resolvable
        MockResolvableContextualSchemaAwareSerializer<?> resolvable =
                Mockito.mock(MockResolvableContextualSchemaAwareSerializer.class);
        StdDelegatingSerializer serRes = new StdDelegatingSerializer(conv, strType, resolvable);
        serRes.resolve(provider);
        Mockito.verify((ResolvableSerializer) resolvable, Mockito.times(1)).resolve(provider);
    }

    @Test
    public void testCreateContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        Converter<Integer, String> conv = new CustomConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);

        // delSer == null, delegateType == null -> resolves type and serializer
        JsonSerializer<?> contextual = ser.createContextual(provider, prop);
        Assert.assertNotNull(contextual);
        Assert.assertTrue(contextual instanceof StdDelegatingSerializer);
        Assert.assertNotNull(contextual.getDelegatee());

        // createContextual again on resolved instance where delSer is already set and not modified
        JsonSerializer<?> contextual2 = contextual.createContextual(provider, prop);
        Assert.assertSame(contextual, contextual2);
    }

    @Test
    public void testCreateContextualWithContextualDelegate() throws Exception {
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JavaType delegateType = TypeFactory.defaultInstance().constructType(String.class);
        Converter<Object, Object> conv = Mockito.mock(Converter.class);
        Mockito.when(conv.getOutputType(Mockito.any())).thenReturn(delegateType);

        MockResolvableContextualSchemaAwareSerializer<Object> delSer =
                Mockito.mock(MockResolvableContextualSchemaAwareSerializer.class);
        JsonSerializer<Object> contextualDelSer = Mockito.mock(JsonSerializer.class);

        Mockito.when(provider.findValueSerializer(delegateType)).thenReturn((JsonSerializer) delSer);
        Mockito.when(provider.handleSecondaryContextualization(delSer, prop)).thenReturn((JsonSerializer) contextualDelSer);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);
        JsonSerializer<?> result = ser.createContextual(provider, prop);

        Assert.assertNotSame(ser, result);
        Assert.assertSame(contextualDelSer, result.getDelegatee());
    }

    @Test
    public void testSerializeNonNullAndNull() throws Exception {
        Converter<Integer, String> conv = new CustomConverter();
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        JsonSerializer<Object> delSer = Mockito.mock(JsonSerializer.class);
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(objConv, strType, delSer);

        // Serialize non-null
        ser.serialize(42, gen, provider);
        Mockito.verify(delSer, Mockito.times(1)).serialize("Value: 42", gen, provider);

        // Serialize null converted value
        ser.serialize(null, gen, provider);
        Mockito.verify(provider, Mockito.times(1)).defaultSerializeNull(gen);
    }

    @Test
    public void testSerializeWithType() throws Exception {
        Converter<Integer, String> conv = new CustomConverter();
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        JsonSerializer<Object> delSer = Mockito.mock(JsonSerializer.class);
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        TypeSerializer typeSer = Mockito.mock(TypeSerializer.class);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(objConv, strType, delSer);
        ser.serializeWithType(100, gen, provider, typeSer);
        Mockito.verify(delSer, Mockito.times(1)).serializeWithType("Value: 100", gen, provider, typeSer);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsEmptyDeprecated() {
        Converter<Integer, String> conv = new CustomConverter();
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        JsonSerializer<Object> delSer = Mockito.mock(JsonSerializer.class);
        Mockito.when(delSer.isEmpty("Value: 0")).thenReturn(true);
        Mockito.when(delSer.isEmpty("Value: 1")).thenReturn(false);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(objConv, strType, delSer);
        Assert.assertTrue(ser.isEmpty(0));
        Assert.assertFalse(ser.isEmpty(1));
    }

    @Test
    public void testIsEmptyWithProvider() {
        Converter<Integer, String> conv = new CustomConverter();
        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConv = (Converter<Object, ?>) (Converter<?, ?>) conv;
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        JsonSerializer<Object> delSer = Mockito.mock(JsonSerializer.class);
        Mockito.when(delSer.isEmpty(provider, "Value: 0")).thenReturn(true);
        Mockito.when(delSer.isEmpty(provider, "Value: 1")).thenReturn(false);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(objConv, strType, delSer);
        Assert.assertTrue(ser.isEmpty(provider, 0));
        Assert.assertFalse(ser.isEmpty(provider, 1));
    }

    @Test
    public void testGetSchemaTwoParams() throws Exception {
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Type typeHint = String.class;
        Converter<Object, ?> conv = Mockito.mock(Converter.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        // SchemaAware delegate
        MockResolvableContextualSchemaAwareSerializer<Object> schemaSer =
                Mockito.mock(MockResolvableContextualSchemaAwareSerializer.class);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        Mockito.when(schemaSer.getSchema(provider, typeHint)).thenReturn(schemaNode);

        StdDelegatingSerializer serWithSchema = new StdDelegatingSerializer(conv, strType, schemaSer);
        Assert.assertSame(schemaNode, serWithSchema.getSchema(provider, typeHint));

        // Non-SchemaAware delegate
        JsonSerializer<Object> plainSer = Mockito.mock(JsonSerializer.class);
        StdDelegatingSerializer serPlain = new StdDelegatingSerializer(conv, strType, plainSer);
        JsonNode defaultSchema = serPlain.getSchema(provider, typeHint);
        Assert.assertNotNull(defaultSchema);
        Assert.assertEquals("string", defaultSchema.get("type").asText());
    }

    @Test
    public void testGetSchemaThreeParams() throws Exception {
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Type typeHint = String.class;
        Converter<Object, ?> conv = Mockito.mock(Converter.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        // SchemaAware delegate
        MockResolvableContextualSchemaAwareSerializer<Object> schemaSer =
                Mockito.mock(MockResolvableContextualSchemaAwareSerializer.class);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        Mockito.when(schemaSer.getSchema(provider, typeHint, true)).thenReturn(schemaNode);

        StdDelegatingSerializer serWithSchema = new StdDelegatingSerializer(conv, strType, schemaSer);
        Assert.assertSame(schemaNode, serWithSchema.getSchema(provider, typeHint, true));

        // Non-SchemaAware delegate
        JsonSerializer<Object> plainSer = Mockito.mock(JsonSerializer.class);
        StdDelegatingSerializer serPlain = new StdDelegatingSerializer(conv, strType, plainSer);
        JsonNode defaultSchema = serPlain.getSchema(provider, typeHint, true);
        Assert.assertNotNull(defaultSchema);
        Assert.assertEquals("string", defaultSchema.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> delSer = Mockito.mock(JsonSerializer.class);
        Converter<Object, ?> conv = Mockito.mock(Converter.class);

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, typeHint, delSer);
        ser.acceptJsonFormatVisitor(visitor, typeHint);

        Mockito.verify(delSer, Mockito.times(1)).acceptJsonFormatVisitor(visitor, typeHint);
    }

    @Test
    public void testFullIntegrationSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Converter<Integer, String> conv = new CustomConverter();
        StdDelegatingSerializer delegatingSer = new StdDelegatingSerializer(Integer.class, conv);

        com.fasterxml.jackson.databind.module.SimpleModule module =
                new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addSerializer(Integer.class, delegatingSer);
        mapper.registerModule(module);

        String json = mapper.writeValueAsString(123);
        Assert.assertEquals("\"Value: 123\"", json);

        String jsonNull = mapper.writeValueAsString(new Object[] { null });
        Assert.assertEquals("[null]", jsonNull);
    }
}
