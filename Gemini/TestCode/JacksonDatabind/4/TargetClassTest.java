package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringArrayDeserializerTest {

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "CUSTOM:" + p.getText();
        }

        @Override
        public String getNullValue() {
            return "CUSTOM_NULL";
        }
    }

    static class WrapperWithCustomElementDeser {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public String[] values;
    }

    static class WrapperWithPolymorphicArray {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object data;
    }

    @Test
    public void testDefaultInstanceAndConstructors() {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        Assert.assertNotNull(deser);
        Assert.assertEquals(String[].class, deser.getValueClass());
        Assert.assertNull(deser._elementDeserializer);

        StringArrayDeserializer custom = new StringArrayDeserializer(new CustomStringDeserializer());
        Assert.assertNotNull(custom._elementDeserializer);
    }

    @Test
    public void testDeserializeEmptyArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String[] result = mapper.readValue("[]", String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testDeserializeStandardStringArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"apple\", \"banana\", \"cherry\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[] { "apple", "banana", "cherry" }, result);
    }

    @Test
    public void testDeserializeArrayWithNullAndMixedTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"text\", null, 123, true, 45.67]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[] { "text", null, "123", "true", "45.67" }, result);
    }

    @Test
    public void testDeserializeLargeArrayTriggeringBufferGrowth() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        int count = 1000;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(count, result.length);
        for (int i = 0; i < count; i++) {
            Assert.assertEquals("item" + i, result[i]);
        }
    }

    @Test
    public void testDeserializeWithCustomElementDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"values\": [\"one\", null, \"two\"]}";
        WrapperWithCustomElementDeser wrapper = mapper.readValue(json, WrapperWithCustomElementDeser.class);

        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.values);
        Assert.assertArrayEquals(new String[] { "CUSTOM:one", "CUSTOM_NULL", "CUSTOM:two" }, wrapper.values);
    }

    @Test
    public void testDeserializeLargeArrayWithCustomElementDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        int count = 1000;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"values\": [");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (i % 2 == 0) {
                sb.append("\"val").append(i).append("\"");
            } else {
                sb.append("null");
            }
        }
        sb.append("]}");

        WrapperWithCustomElementDeser wrapper = mapper.readValue(sb.toString(), WrapperWithCustomElementDeser.class);
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.values);
        Assert.assertEquals(count, wrapper.values.length);
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals("CUSTOM:val" + i, wrapper.values[i]);
            } else {
                Assert.assertEquals("CUSTOM_NULL", wrapper.values[i]);
            }
        }
    }

    @Test
    public void testSingleValueAsArrayEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String[] res1 = mapper.readValue("\"single\"", String[].class);
        Assert.assertArrayEquals(new String[] { "single" }, res1);

        String[] res2 = mapper.readValue("12345", String[].class);
        Assert.assertArrayEquals(new String[] { "12345" }, res2);

        String[] res3 = mapper.readValue("null", String[].class);
        Assert.assertNull(res3);
    }

    @Test
    public void testSingleValueAsArrayDisabledFailure() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        try {
            mapper.readValue("\"single\"", String[].class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected
        }

        try {
            mapper.readValue("123", String[].class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected
        }

        try {
            mapper.readValue("{\"key\":\"value\"}", String[].class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected
        }
    }

    @Test
    public void testAcceptEmptyStringAsNullObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String[] result = mapper.readValue("\"\"", String[].class);
        Assert.assertNull(result);

        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        try {
            mapper.readValue("\"\"", String[].class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected
        }
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WrapperWithPolymorphicArray wrapper = new WrapperWithPolymorphicArray();
        wrapper.data = new String[] { "a", "b", "c" };

        String json = mapper.writeValueAsString(wrapper);
        WrapperWithPolymorphicArray deserialized = mapper.readValue(json, WrapperWithPolymorphicArray.class);

        Assert.assertNotNull(deserialized);
        Assert.assertTrue(deserialized.data instanceof String[]);
        Assert.assertArrayEquals(new String[] { "a", "b", "c" }, (String[]) deserialized.data);
    }

    @Test
    public void testDirectDeserializeWithTypeInvocation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("[\"x\", \"y\"]");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(String[].class);
        TypeDeserializer typeDeser = mapper.getDeserializationConfig().findTypeDeserializer(type);

        if (typeDeser != null) {
            StringArrayDeserializer deser = new StringArrayDeserializer();
            Object res = deser.deserializeWithType(parser, ctxt, typeDeser);
            Assert.assertNotNull(res);
        }
        parser.close();
    }

    @Test
    public void testCreateContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        StringArrayDeserializer deser = new StringArrayDeserializer();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertNotNull(contextual);
        Assert.assertTrue(contextual instanceof StringArrayDeserializer);

        // When default deserializer is resolved, _elementDeserializer remains null
        StringArrayDeserializer sDeser = (StringArrayDeserializer) contextual;
        Assert.assertNull(sDeser._elementDeserializer);

        // Creating contextual from existing custom deserializer
        CustomStringDeserializer customElem = new CustomStringDeserializer();
        StringArrayDeserializer customArrayDeser = new StringArrayDeserializer(customElem);
        JsonDeserializer<?> contextualCustom = customArrayDeser.createContextual(ctxt, null);
        Assert.assertNotNull(contextualCustom);
        Assert.assertTrue(contextualCustom instanceof StringArrayDeserializer);
    }

    @Test
    public void testCustomModuleRegistration() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String[].class, new StringArrayDeserializer(new CustomStringDeserializer()));
        mapper.registerModule(module);

        String json = "[\"foo\", \"bar\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals(new String[] { "CUSTOM:foo", "CUSTOM:bar" }, result);
    }
}
