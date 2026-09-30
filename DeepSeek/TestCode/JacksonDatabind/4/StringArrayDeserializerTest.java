package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class StringArrayDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserializeSimpleArray() throws Exception {
        String json = "[\"a\", \"b\", \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testDeserializeEmptyArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testDeserializeArrayWithNulls() throws Exception {
        String json = "[\"a\", null, \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"a", null, "c"}, result);
    }

    @Test
    public void testDeserializeArrayWithNumbers() throws Exception {
        String json = "[1, 2, 3]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"1", "2", "3"}, result);
    }

    @Test
    public void testDeserializeArrayWithBooleans() throws Exception {
        String json = "[true, false]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"true", "false"}, result);
    }

    @Test
    public void testDeserializeSingleValueAsArrayEnabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"single\"";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"single"}, result);
    }

    @Test
    public void testDeserializeSingleNullAsArrayEnabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "null";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{null}, result);
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testDeserializeSingleValueAsArrayDisabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"single\"";
        customMapper.readValue(json, String[].class);
    }

    @Test
    public void testDeserializeEmptyStringAsNullEnabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertNull(result);
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testDeserializeEmptyStringAsNullDisabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        customMapper.readValue(json, String[].class);
    }

    @Test
    public void testDeserializeWithCustomElementDeserializer() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        StringArrayDeserializer deser = new StringArrayDeserializer(new UpperCaseStringDeserializer());
        customMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
            .addDeserializer(String[].class, deser));
        String json = "[\"a\", \"b\"]";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"A", "B"}, result);
    }

    @Test
    public void testDeserializeWithCustomElementDeserializerAndNulls() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        StringArrayDeserializer deser = new StringArrayDeserializer(new UpperCaseStringDeserializer());
        customMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
            .addDeserializer(String[].class, deser));
        String json = "[\"a\", null, \"c\"]";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"A", null, "C"}, result);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        String json = "[\"a\", \"b\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateContextualDefaultDeserializer() throws Exception {
        StringArrayDeserializer deser = new StringArrayDeserializer();
        JsonDeserializer<?> result = deser.createContextual(mapper.getDeserializationContext(), null);
        Assert.assertSame(deser, result);
    }

    @Test
    public void testCreateContextualWithCustomDeserializer() throws Exception {
        StringArrayDeserializer deser = new StringArrayDeserializer(new UpperCaseStringDeserializer());
        JsonDeserializer<?> result = deser.createContextual(mapper.getDeserializationContext(), null);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof StringArrayDeserializer);
    }

    @Test
    public void testHandleNonArrayWithSingleValueEnabled() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "123";
        String[] result = customMapper.readValue(json, String[].class);
        Assert.assertArrayEquals(new String[]{"123"}, result);
    }

    @Test
    public void testLargeArray() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"value").append(i).append("\"");
        }
        sb.append("]");
        String[] result = mapper.readValue(sb.toString(), String[].class);
        Assert.assertEquals(1000, result.length);
        Assert.assertEquals("value0", result[0]);
        Assert.assertEquals("value999", result[999]);
    }

    static class UpperCaseStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText().toUpperCase();
        }

        @Override
        public String getNullValue() {
            return null;
        }
    }
}
