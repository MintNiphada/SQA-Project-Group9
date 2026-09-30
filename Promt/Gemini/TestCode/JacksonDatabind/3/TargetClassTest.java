package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class StringArrayDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "custom:" + p.getText();
        }

        @Override
        public String getNullValue() {
            return "custom:null";
        }
    }

    static class UpperCaseConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    static class ArrayWrapper {
        public String[] values;
    }

    static class CustomArrayWrapper {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public String[] values;
    }

    static class ConvertedArrayWrapper {
        @JsonDeserialize(contentConverter = UpperCaseConverter.class)
        public String[] values;
    }

    static class PolymorphicWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object data;
    }

    @Test
    public void testInstanceAndConstructors() {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        Assert.assertNotNull(deser);
        Assert.assertEquals(String[].class, deser.getValueClass());

        StringArrayDeserializer defaultDeser = new StringArrayDeserializer();
        Assert.assertNotNull(defaultDeser);
        Assert.assertNull(defaultDeser._elementDeserializer);

        CustomStringDeserializer custom = new CustomStringDeserializer();
        StringArrayDeserializer customDeser = new StringArrayDeserializer(custom);
        Assert.assertSame(custom, customDeser._elementDeserializer);
    }

    @Test
    public void testDefaultDeserializationEmptyArray() throws Exception {
        String json = "[]";
        String[] result = MAPPER.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testDefaultDeserializationStandardStrings() throws Exception {
        String json = "[\"a\", \"b\", \"c\"]";
        String[] result = MAPPER.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testDefaultDeserializationMixedTypes() throws Exception {
        String json = "[\"text\", 123, true, 45.67]";
        String[] result = MAPPER.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[]{"text", "123", "true", "45.67"}, result);
    }

    @Test
    public void testDefaultDeserializationLargeArray() throws Exception {
        int size = 5000;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"val").append(i).append("\"");
        }
        sb.append("]");

        String[] result = MAPPER.readValue(sb.toString(), String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(size, result.length);
        Assert.assertEquals("val0", result[0]);
        Assert.assertEquals("val4999", result[4999]);
    }

    @Test
    public void testCustomDeserializerInArray() throws Exception {
        String json = "{\"values\": [\"first\", \"second\"]}";
        CustomArrayWrapper wrapper = MAPPER.readValue(json, CustomArrayWrapper.class);
        Assert.assertNotNull(wrapper.values);
        Assert.assertArrayEquals(new String[]{"custom:first", "custom:second"}, wrapper.values);
    }

    @Test
    public void testCustomDeserializerWithNull() throws Exception {
        String json = "{\"values\": [\"first\", null, \"third\"]}";
        CustomArrayWrapper wrapper = MAPPER.readValue(json, CustomArrayWrapper.class);
        Assert.assertNotNull(wrapper.values);
        Assert.assertArrayEquals(new String[]{"custom:first", null, "custom:third"}, wrapper.values);
    }

    @Test
    public void testCustomDeserializerLargeArray() throws Exception {
        int size = 3000;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"values\": [");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            if (i % 2 == 0) {
                sb.append("\"item").append(i).append("\"");
            } else {
                sb.append("null");
            }
        }
        sb.append("]}");

        CustomArrayWrapper wrapper = MAPPER.readValue(sb.toString(), CustomArrayWrapper.class);
        Assert.assertNotNull(wrapper.values);
        Assert.assertEquals(size, wrapper.values.length);
        Assert.assertEquals("custom:item0", wrapper.values[0]);
        Assert.assertNull(wrapper.values[1]);
        Assert.assertEquals("custom:item2", wrapper.values[2]);
    }

    @Test
    public void testConverterSupport() throws Exception {
        String json = "{\"values\": [\"abc\", \"def\"]}";
        ConvertedArrayWrapper wrapper = MAPPER.readValue(json, ConvertedArrayWrapper.class);
        Assert.assertNotNull(wrapper.values);
        Assert.assertArrayEquals(new String[]{"ABC", "DEF"}, wrapper.values);
    }

    @Test
    public void testSingleValueAsArrayEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String[] result = mapper.readValue("\"single\"", String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[]{"single"}, result);

        String[] nullResult = mapper.readValue("null", String[].class);
        Assert.assertNull(nullResult);

        String[] numberResult = mapper.readValue("12345", String[].class);
        Assert.assertNotNull(numberResult);
        Assert.assertArrayEquals(new String[]{"12345"}, numberResult);
    }

    @Test
    public void testSingleValueAsArrayDisabledFailure() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        try {
            mapper.readValue("\"single\"", String[].class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("from String value"));
        }
    }

    @Test
    public void testAcceptEmptyStringAsNullObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String[] result = mapper.readValue("\"\"", String[].class);
        Assert.assertNull(result);

        try {
            mapper.readValue("\"non-empty\"", String[].class);
            Assert.fail("Expected JsonMappingException for non-empty string when single value as array is disabled");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("from String value"));
        }
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        PolymorphicWrapper wrapper = new PolymorphicWrapper();
        wrapper.data = new String[]{"hello", "world"};

        String json = MAPPER.writeValueAsString(wrapper);
        PolymorphicWrapper result = MAPPER.readValue(json, PolymorphicWrapper.class);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.data instanceof String[]);
        Assert.assertArrayEquals(new String[]{"hello", "world"}, (String[]) result.data);
    }

    @Test
    public void testDirectDeserializeWithTypeMethod() throws Exception {
        String json = "[\"a\", \"b\"]";
        JsonParser p = new JsonFactory().createParser(json);
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        TypeDeserializer typeDeserializer = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) {
                return this;
            }

            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() {
                return com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_ARRAY;
            }

            @Override
            public String getPropertyName() {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() {
                return null;
            }

            @Override
            public Class<?> getDefaultImpl() {
                return String[].class;
            }

            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new String[]{"mocked"};
            }

            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        StringArrayDeserializer deser = new StringArrayDeserializer();
        Object result = deser.deserializeWithType(p, ctxt, typeDeserializer);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof String[]);
        Assert.assertArrayEquals(new String[]{"mocked"}, (String[]) result);
        p.close();
    }

    @Test
    public void testCreateContextualBranches() throws Exception {
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        StringArrayDeserializer defaultDeser = new StringArrayDeserializer();

        // 1. Contextualize with default deserializer: should return same or instance with null deser
        JsonDeserializer<?> contextualized = defaultDeser.createContextual(ctxt, null);
        Assert.assertNotNull(contextualized);
        Assert.assertTrue(contextualized instanceof StringArrayDeserializer);
        Assert.assertNull(((StringArrayDeserializer) contextualized)._elementDeserializer);

        // 2. Contextualize with custom element deserializer
        CustomStringDeserializer customElem = new CustomStringDeserializer();
        StringArrayDeserializer withCustom = new StringArrayDeserializer(customElem);
        JsonDeserializer<?> contextualizedCustom = withCustom.createContextual(ctxt, null);
        Assert.assertNotNull(contextualizedCustom);
        Assert.assertTrue(contextualizedCustom instanceof StringArrayDeserializer);
        Assert.assertNotNull(((StringArrayDeserializer) contextualizedCustom)._elementDeserializer);

        // 3. Re-contextualizing with same custom deser when no changes are made should return this
        JsonDeserializer<?> sameResult = contextualizedCustom.createContextual(ctxt, null);
        Assert.assertSame(contextualizedCustom, sameResult);
    }

    @Test
    public void testHandleNonArrayNullTokenDirectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        // Simulating parsing of a non-array null token with ACCEPT_SINGLE_VALUE_AS_ARRAY
        String json = "null";
        JsonParser p = new JsonFactory().createParser(json);
        p.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();

        StringArrayDeserializer deser = new StringArrayDeserializer();
        String[] res = deser.deserialize(p, ctxt);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.length);
        Assert.assertNull(res[0]);
        p.close();
    }
}
