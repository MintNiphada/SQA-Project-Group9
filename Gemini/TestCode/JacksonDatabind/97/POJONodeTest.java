package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.RawValue;
import org.junit.Assert;
import org.junit.Test;

public class POJONodeTest {

    private static class DummyGenerator extends JsonGenerator {
        Object writtenObject;
        @Override public JsonGenerator setCodec(ObjectCodec oc) { return this; }
        @Override public ObjectCodec getCodec() { return null; }
        @Override public Version version() { return Version.unknownVersion(); }
        @Override public JsonGenerator enable(Feature f) { return this; }
        @Override public JsonGenerator disable(Feature f) { return this; }
        @Override public boolean isEnabled(Feature f) { return false; }
        @Override public int getFeatureMask() { return 0; }
        @Deprecated @Override public JsonGenerator setFeatureMask(int values) { return this; }
        @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
        @Override public void writeStartArray() throws IOException {}
        @Override public void writeEndArray() throws IOException {}
        @Override public void writeStartObject() throws IOException {}
        @Override public void writeEndObject() throws IOException {}
        @Override public void writeFieldName(String name) throws IOException {}
        @Override public void writeFieldName(SerializableString name) throws IOException {}
        @Override public void writeString(String text) throws IOException {}
        @Override public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override public void writeString(SerializableString text) throws IOException {}
        @Override public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override public void writeRaw(String text) throws IOException {}
        @Override public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char c) throws IOException {}
        @Override public void writeRawValue(String text) throws IOException {}
        @Override public void writeRawValue(String text, int offset, int len) throws IOException {}
        @Override public void writeRawValue(char[] text, int offset, int len) throws IOException {}
        @Override public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
        @Override public int writeBinary(Base64Variant bv, java.io.InputStream data, int dataLength) throws IOException { return 0; }
        @Override public void writeNumber(int v) throws IOException {}
        @Override public void writeNumber(long v) throws IOException {}
        @Override public void writeNumber(BigInteger v) throws IOException {}
        @Override public void writeNumber(double v) throws IOException {}
        @Override public void writeNumber(float v) throws IOException {}
        @Override public void writeNumber(BigDecimal v) throws IOException {}
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException {}
        @Override public void writeNull() throws IOException {}
        @Override public void writeObject(Object pojo) throws IOException { this.writtenObject = pojo; }
        @Override public void writeTree(TreeNode rootNode) throws IOException {}
        @Override public JsonStreamContext getOutputContext() { return null; }
        @Override public void flush() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() throws IOException {}
    }

    private static class DummyProvider extends SerializerProvider {
        boolean nullSerialized = false;
        public DummyProvider() { super(); }
        @Override public JsonSerializer<Object> serializerInstance(com.fasterxml.jackson.databind.introspect.Annotated annotated, Object serDef) { return null; }
        @Override public Object findFilterId(SerializationConfig config, com.fasterxml.jackson.databind.BeanDescription beanDesc) { return null; }
        @Override public void defaultSerializeNull(JsonGenerator jgen) throws IOException { this.nullSerialized = true; }
    }

    @Test
    public void testGetNodeTypeAndToken() {
        POJONode node = new POJONode("test");
        Assert.assertEquals(JsonNodeType.POJO, node.getNodeType());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
        Assert.assertEquals("test", node.getPojo());
    }

    @Test
    public void testBinaryValue() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        POJONode byteNode = new POJONode(data);
        Assert.assertArrayEquals(data, byteNode.binaryValue());

        POJONode stringNode = new POJONode("not bytes");
        Assert.assertNull(stringNode.binaryValue());

        POJONode nullNode = new POJONode(null);
        Assert.assertNull(nullNode.binaryValue());
    }

    @Test
    public void testAsText() {
        POJONode node = new POJONode("hello");
        Assert.assertEquals("hello", node.asText());
        Assert.assertEquals("hello", node.asText("def"));

        POJONode nullNode = new POJONode(null);
        Assert.assertEquals("null", nullNode.asText());
        Assert.assertEquals("def", nullNode.asText("def"));
        Assert.assertNull(nullNode.asText(null));
    }

    @Test
    public void testAsBoolean() {
        POJONode trueNode = new POJONode(Boolean.TRUE);
        Assert.assertTrue(trueNode.asBoolean(false));

        POJONode falseNode = new POJONode(Boolean.FALSE);
        Assert.assertFalse(falseNode.asBoolean(true));

        POJONode nonBool = new POJONode("true");
        Assert.assertTrue(nonBool.asBoolean(true));
        Assert.assertFalse(nonBool.asBoolean(false));

        POJONode nullNode = new POJONode(null);
        Assert.assertTrue(nullNode.asBoolean(true));
        Assert.assertFalse(nullNode.asBoolean(false));
    }

    @Test
    public void testAsInt() {
        POJONode intNode = new POJONode(Integer.valueOf(42));
        Assert.assertEquals(42, intNode.asInt(0));

        POJONode longNode = new POJONode(Long.valueOf(100L));
        Assert.assertEquals(100, longNode.asInt(0));

        POJONode nonNumber = new POJONode("42");
        Assert.assertEquals(99, nonNumber.asInt(99));

        POJONode nullNode = new POJONode(null);
        Assert.assertEquals(123, nullNode.asInt(123));
    }

    @Test
    public void testAsLong() {
        POJONode longNode = new POJONode(Long.valueOf(9876543210L));
        Assert.assertEquals(9876543210L, longNode.asLong(0L));

        POJONode intNode = new POJONode(Integer.valueOf(123));
        Assert.assertEquals(123L, intNode.asLong(0L));

        POJONode nonNumber = new POJONode("abc");
        Assert.assertEquals(555L, nonNumber.asLong(555L));

        POJONode nullNode = new POJONode(null);
        Assert.assertEquals(777L, nullNode.asLong(777L));
    }

    @Test
    public void testAsDouble() {
        POJONode doubleNode = new POJONode(Double.valueOf(3.1415));
        Assert.assertEquals(3.1415, doubleNode.asDouble(0.0), 0.00001);

        POJONode intNode = new POJONode(Integer.valueOf(7));
        Assert.assertEquals(7.0, intNode.asDouble(0.0), 0.00001);

        POJONode nonNumber = new POJONode("3.14");
        Assert.assertEquals(1.23, nonNumber.asDouble(1.23), 0.00001);

        POJONode nullNode = new POJONode(null);
        Assert.assertEquals(4.56, nullNode.asDouble(4.56), 0.00001);
    }

    @Test
    public void testSerializeNull() throws IOException {
        POJONode nullNode = new POJONode(null);
        DummyGenerator gen = new DummyGenerator();
        DummyProvider provider = new DummyProvider();
        nullNode.serialize(gen, provider);
        Assert.assertTrue(provider.nullSerialized);
        Assert.assertNull(gen.writtenObject);
    }

    @Test
    public void testSerializeJsonSerializable() throws IOException {
        final boolean[] called = new boolean[]{false};
        JsonSerializable serializable = new JsonSerializable() {
            @Override
            public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
                called[0] = true;
            }
            @Override
            public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {}
        };
        POJONode node = new POJONode(serializable);
        DummyGenerator gen = new DummyGenerator();
        DummyProvider provider = new DummyProvider();
        node.serialize(gen, provider);
        Assert.assertTrue(called[0]);
    }

    @Test
    public void testSerializeStandardObject() throws IOException {
        String data = "serializeMe";
        POJONode node = new POJONode(data);
        DummyGenerator gen = new DummyGenerator();
        DummyProvider provider = new DummyProvider();
        node.serialize(gen, provider);
        Assert.assertEquals(data, gen.writtenObject);
    }

    @Test
    public void testEqualsAndHashCode() {
        POJONode n1 = new POJONode("same");
        POJONode n2 = new POJONode("same");
        POJONode n3 = new POJONode("different");
        POJONode null1 = new POJONode(null);
        POJONode null2 = new POJONode(null);

        Assert.assertTrue(n1.equals(n1));
        Assert.assertTrue(n1.equals(n2));
        Assert.assertTrue(n2.equals(n1));
        Assert.assertEquals(n1.hashCode(), n2.hashCode());

        Assert.assertFalse(n1.equals(n3));
        Assert.assertFalse(n1.equals(null1));
        Assert.assertFalse(null1.equals(n1));

        Assert.assertTrue(null1.equals(null2));
        Assert.assertFalse(n1.equals(null));
        Assert.assertFalse(n1.equals("same"));
        Assert.assertFalse(n1.equals(new Object()));
    }

    @Test(expected = NullPointerException.class)
    public void testHashCodeWithNullThrowsNpe() {
        POJONode nullNode = new POJONode(null);
        nullNode.hashCode();
    }

    @Test
    public void testToString() {
        byte[] bytes = new byte[]{1, 2, 3, 4};
        POJONode byteNode = new POJONode(bytes);
        Assert.assertEquals("(binary value of 4 bytes)", byteNode.toString());

        RawValue raw = new RawValue("{\"k\":\"v\"}");
        POJONode rawNode = new POJONode(raw);
        Assert.assertEquals("(raw value '{\"k\":\"v\"}')", rawNode.toString());

        POJONode stringNode = new POJONode("test-val");
        Assert.assertEquals("test-val", stringNode.toString());

        POJONode nullNode = new POJONode(null);
        Assert.assertEquals("null", nullNode.toString());
    }
}
