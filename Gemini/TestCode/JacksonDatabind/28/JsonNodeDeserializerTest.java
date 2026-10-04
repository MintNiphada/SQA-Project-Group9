package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonNodeDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testGetDeserializerFactoryMethod() {
        JsonDeserializer<? extends JsonNode> objDeser = JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        Assert.assertTrue(objDeser instanceof JsonNodeDeserializer.ObjectDeserializer);
        Assert.assertSame(JsonNodeDeserializer.ObjectDeserializer.getInstance(), objDeser);

        JsonDeserializer<? extends JsonNode> arrDeser = JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        Assert.assertTrue(arrDeser instanceof JsonNodeDeserializer.ArrayDeserializer);
        Assert.assertSame(JsonNodeDeserializer.ArrayDeserializer.getInstance(), arrDeser);

        JsonDeserializer<? extends JsonNode> genericDeser1 = JsonNodeDeserializer.getDeserializer(JsonNode.class);
        Assert.assertTrue(genericDeser1 instanceof JsonNodeDeserializer);

        JsonDeserializer<? extends JsonNode> genericDeser2 = JsonNodeDeserializer.getDeserializer(ValueNode.class);
        Assert.assertSame(genericDeser1, genericDeser2);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testNullValues() {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        Assert.assertSame(NullNode.getInstance(), deser.getNullValue());
        Assert.assertSame(NullNode.getInstance(), deser.getNullValue(mapper.getDeserializationContext()));
        Assert.assertTrue(deser.isCachable());
    }

    @Test
    public void testDeserializeScalars() throws Exception {
        JsonNode nodeStr = mapper.readValue("\"hello world\"", JsonNode.class);
        Assert.assertTrue(nodeStr.isTextual());
        Assert.assertEquals("hello world", nodeStr.textValue());

        JsonNode nodeTrue = mapper.readValue("true", JsonNode.class);
        Assert.assertTrue(nodeTrue.isBoolean());
        Assert.assertTrue(nodeTrue.booleanValue());

        JsonNode nodeFalse = mapper.readValue("false", JsonNode.class);
        Assert.assertTrue(nodeFalse.isBoolean());
        Assert.assertFalse(nodeFalse.booleanValue());

        JsonNode nodeNull = mapper.readValue("null", JsonNode.class);
        Assert.assertTrue(nodeNull.isNull());
    }

    @Test
    public void testDeserializeIntegersAndCoercion() throws Exception {
        JsonNode nodeInt = mapper.readValue("1234", JsonNode.class);
        Assert.assertTrue(nodeInt.isInt());
        Assert.assertEquals(1234, nodeInt.intValue());

        JsonNode nodeLong = mapper.readValue("99999999999999", JsonNode.class);
        Assert.assertTrue(nodeLong.isLong());
        Assert.assertEquals(99999999999999L, nodeLong.longValue());

        BigInteger bigIntVal = new BigInteger("123456789012345678901234567890");
        JsonNode nodeBigInt = mapper.readValue(bigIntVal.toString(), JsonNode.class);
        Assert.assertTrue(nodeBigInt.isBigInteger());
        Assert.assertEquals(bigIntVal, nodeBigInt.bigIntegerValue());

        ObjectMapper mapperUseBigInt = new ObjectMapper();
        mapperUseBigInt.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonNode nodeIntAsBig = mapperUseBigInt.readValue("42", JsonNode.class);
        Assert.assertTrue(nodeIntAsBig instanceof BigIntegerNode);
        Assert.assertEquals(BigInteger.valueOf(42), nodeIntAsBig.bigIntegerValue());

        ObjectMapper mapperUseLong = new ObjectMapper();
        mapperUseLong.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        JsonNode nodeIntAsLong = mapperUseLong.readValue("42", JsonNode.class);
        Assert.assertTrue(nodeIntAsLong instanceof LongNode);
        Assert.assertEquals(42L, nodeIntAsLong.longValue());
    }

    @Test
    public void testDeserializeFloatsAndCoercion() throws Exception {
        JsonNode nodeDouble = mapper.readValue("12.34", JsonNode.class);
        Assert.assertTrue(nodeDouble.isDouble());
        Assert.assertEquals(12.34, nodeDouble.doubleValue(), 0.00001);

        ObjectMapper mapperUseBigDecimal = new ObjectMapper();
        mapperUseBigDecimal.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonNode nodeBigDecimal = mapperUseBigDecimal.readValue("12.34", JsonNode.class);
        Assert.assertTrue(nodeBigDecimal.isBigDecimal());
        Assert.assertEquals(new BigDecimal("12.34"), nodeBigDecimal.decimalValue());
    }

    @Test
    public void testDeserializeObjectAndNestedElements() throws Exception {
        String json = "{"
                + "\"str\": \"value\","
                + "\"numInt\": 12,"
                + "\"numFloat\": 34.5,"
                + "\"boolT\": true,"
                + "\"boolF\": false,"
                + "\"nullVal\": null,"
                + "\"childObj\": { \"nestedKey\": \"nestedVal\" },"
                + "\"childArr\": [ 1, true, \"abc\" ]"
                + "}";

        JsonNode root = mapper.readValue(json, JsonNode.class);
        Assert.assertTrue(root.isObject());
        ObjectNode obj = (ObjectNode) root;

        Assert.assertEquals("value", obj.get("str").textValue());
        Assert.assertEquals(12, obj.get("numInt").intValue());
        Assert.assertEquals(34.5, obj.get("numFloat").doubleValue(), 0.001);
        Assert.assertTrue(obj.get("boolT").booleanValue());
        Assert.assertFalse(obj.get("boolF").booleanValue());
        Assert.assertTrue(obj.get("nullVal").isNull());

        JsonNode childObj = obj.get("childObj");
        Assert.assertTrue(childObj.isObject());
        Assert.assertEquals("nestedVal", childObj.get("nestedKey").textValue());

        JsonNode childArr = obj.get("childArr");
        Assert.assertTrue(childArr.isArray());
        Assert.assertEquals(3, childArr.size());
        Assert.assertEquals(1, childArr.get(0).intValue());
        Assert.assertTrue(childArr.get(1).booleanValue());
        Assert.assertEquals("abc", childArr.get(2).textValue());
    }

    @Test
    public void testDeserializeEmptyObject() throws Exception {
        JsonNode node = mapper.readValue("{}", JsonNode.class);
        Assert.assertTrue(node.isObject());
        Assert.assertEquals(0, node.size());

        ObjectNode objNode = mapper.readValue("{}", ObjectNode.class);
        Assert.assertEquals(0, objNode.size());
    }

    @Test
    public void testDeserializeArrayAndNestedElements() throws Exception {
        String json = "[ \"text\", 10, true, false, null, { \"k\": \"v\" }, [ 1, 2 ] ]";
        JsonNode root = mapper.readValue(json, JsonNode.class);
        Assert.assertTrue(root.isArray());
        ArrayNode arr = (ArrayNode) root;

        Assert.assertEquals(7, arr.size());
        Assert.assertEquals("text", arr.get(0).textValue());
        Assert.assertEquals(10, arr.get(1).intValue());
        Assert.assertTrue(arr.get(2).booleanValue());
        Assert.assertFalse(arr.get(3).booleanValue());
        Assert.assertTrue(arr.get(4).isNull());
        Assert.assertTrue(arr.get(5).isObject());
        Assert.assertEquals("v", arr.get(5).get("k").textValue());
        Assert.assertTrue(arr.get(6).isArray());
        Assert.assertEquals(2, arr.get(6).size());
    }

    @Test
    public void testDeserializeEmptyArray() throws Exception {
        JsonNode node = mapper.readValue("[]", JsonNode.class);
        Assert.assertTrue(node.isArray());
        Assert.assertEquals(0, node.size());

        ArrayNode arrNode = mapper.readValue("[]", ArrayNode.class);
        Assert.assertEquals(0, arrNode.size());
    }

    @Test
    public void testDuplicateFieldHandling() throws Exception {
        String json = "{\"dup\": 1, \"dup\": 2}";

        // Default behavior: last one replaces previous
        JsonNode node = mapper.readValue(json, JsonNode.class);
        Assert.assertEquals(2, node.get("dup").intValue());

        // Configured to fail
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        try {
            strictMapper.readValue(json, JsonNode.class);
            Assert.fail("Expected JsonMappingException on duplicate field");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Duplicate field 'dup'"));
        }
    }

    @Test
    public void testObjectDeserializerSpecific() throws Exception {
        ObjectNode node = mapper.readValue("{\"name\":\"test\"}", ObjectNode.class);
        Assert.assertNotNull(node);
        Assert.assertEquals("test", node.get("name").textValue());

        // Attempting to deserialize array into ObjectNode should fail
        try {
            mapper.readValue("[1, 2]", ObjectNode.class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // Test with parser pointing at FIELD_NAME token directly
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ObjectNode res = JsonNodeDeserializer.ObjectDeserializer.getInstance().deserialize(p, ctxt);
        Assert.assertEquals(1, res.get("a").intValue());
        p.close();
    }

    @Test
    public void testArrayDeserializerSpecific() throws Exception {
        ArrayNode arr = mapper.readValue("[1, 2, 3]", ArrayNode.class);
        Assert.assertNotNull(arr);
        Assert.assertEquals(3, arr.size());

        // Attempting to deserialize object into ArrayNode should fail
        try {
            mapper.readValue("{\"name\":\"test\"}", ArrayNode.class);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEmbeddedObjectsInTokenBuffer() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);

        // 1. Embedded null
        tb.writeStartObject();
        tb.writeFieldName("nullEmbed");
        tb.writeEmbeddedObject(null);

        // 2. Embedded byte array
        byte[] rawBytes = new byte[] { 1, 2, 3, 4 };
        tb.writeFieldName("bytes");
        tb.writeEmbeddedObject(rawBytes);

        // 3. Embedded RawValue
        RawValue rawValue = new RawValue("{\"raw\":true}");
        tb.writeFieldName("raw");
        tb.writeEmbeddedObject(rawValue);

        // 4. Embedded JsonNode
        JsonNode innerNode = IntNode.valueOf(789);
        tb.writeFieldName("node");
        tb.writeEmbeddedObject(innerNode);

        // 5. Embedded POJO
        Object pojo = new Object() {
            @Override
            public String toString() { return "customPOJO"; }
        };
        tb.writeFieldName("pojo");
        tb.writeEmbeddedObject(pojo);

        tb.writeEndObject();

        JsonParser parser = tb.asParser(mapper);
        parser.nextToken(); // Point to START_OBJECT

        JsonNode result = mapper.readTree(parser);
        Assert.assertTrue(result.isObject());
        Assert.assertTrue(result.get("nullEmbed").isNull());
        Assert.assertTrue(result.get("bytes") instanceof BinaryNode);
        Assert.assertArrayEquals(rawBytes, ((BinaryNode) result.get("bytes")).binaryValue());
        Assert.assertTrue(result.get("raw") instanceof com.fasterxml.jackson.databind.node.ValueNode);
        Assert.assertEquals(789, result.get("node").intValue());
        Assert.assertTrue(result.get("pojo") instanceof POJONode);
        Assert.assertSame(pojo, ((POJONode) result.get("pojo")).getPojo());
        parser.close();
        tb.close();
    }

    @Test
    public void testEmbeddedObjectsInArray() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeStartArray();
        tb.writeEmbeddedObject(new byte[] { 42 });
        tb.writeEmbeddedObject(new RawValue("123"));
        tb.writeEndArray();

        JsonParser parser = tb.asParser(mapper);
        ArrayNode arr = mapper.readValue(parser, ArrayNode.class);
        Assert.assertEquals(2, arr.size());
        Assert.assertTrue(arr.get(0) instanceof BinaryNode);
        parser.close();
        tb.close();
    }

    @Test
    public void testDeserializeAnyDirectly() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeEmbeddedObject(new byte[] { 9, 8 });
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // Points to EMBEDDED_OBJECT

        JsonNode node = mapper.getDeserializationContext().getNodeFactory().nullNode();
        JsonDeserializer<JsonNode> deser = (JsonDeserializer<JsonNode>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonNode res = deser.deserialize(p, mapper.getDeserializationContext());
        Assert.assertTrue(res instanceof BinaryNode);
        p.close();
        tb.close();
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeString("some text");
        JsonParser p = tb.asParser(mapper);
        p.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = mapper.getTypeFactory()
                .constructType(JsonNode.class)
                .getTypeHandler();

        // When TypeDeserializer is null or mock, verify delegation
        TypeDeserializer mockTypeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new TextNode(p.getText());
            }
        };

        Object output = deser.deserializeWithType(p, ctxt, mockTypeDeser);
        Assert.assertTrue(output instanceof TextNode);
        Assert.assertEquals("some text", ((TextNode) output).textValue());
        p.close();
        tb.close();
    }

    @Test
    public void testCustomReportProblem() {
        BaseNodeDeserializer<JsonNode> deser = new BaseNodeDeserializer<JsonNode>(JsonNode.class) {};
        TokenBuffer tb = new TokenBuffer(mapper, false);
        JsonParser p = tb.asParser(mapper);

        try {
            deser._reportProblem(p, "Custom error message");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Custom error message"));
        }
    }

    @Test
    public void testUnexpectedEndOfArrayInput() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeStartArray();
        // Do not write end array
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // START_ARRAY

        ArrayNode node = mapper.createArrayNode();
        try {
            JsonNodeDeserializer.ArrayDeserializer.getInstance().deserialize(p, mapper.getDeserializationContext());
            Assert.fail("Expected mappingException for unexpected end-of-input");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }
        p.close();
        tb.close();
    }

    @Test
    public void testDeserializeAnyInvalidToken() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeEndObject(); // write stray END_OBJECT
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // pointing to END_OBJECT

        // deserializeAny handles END_OBJECT as empty object, so test with NOT_AVAILABLE or END_ARRAY
        TokenBuffer tb2 = new TokenBuffer(mapper, false);
        tb2.writeStartArray();
        tb2.writeEndArray();
        JsonParser p2 = tb2.asParser(mapper);
        p2.nextToken(); // START_ARRAY
        p2.nextToken(); // END_ARRAY

        JsonDeserializer<JsonNode> deser = (JsonDeserializer<JsonNode>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        try {
            deser.deserialize(p2, mapper.getDeserializationContext());
            Assert.fail("Expected JsonMappingException on invalid token");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
        p.close();
        tb.close();
        p2.close();
        tb2.close();
    }

    @Test
    public void testObjectDeserializerInvalidStartToken() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeNumber(123);
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // VALUE_NUMBER_INT

        try {
            JsonNodeDeserializer.ObjectDeserializer.getInstance().deserialize(p, mapper.getDeserializationContext());
            Assert.fail("Expected JsonMappingException on non-object token");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
        p.close();
        tb.close();
    }

    @Test
    public void testArrayDeserializerInvalidStartToken() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeString("abc");
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // VALUE_STRING

        try {
            JsonNodeDeserializer.ArrayDeserializer.getInstance().deserialize(p, mapper.getDeserializationContext());
            Assert.fail("Expected JsonMappingException on non-array token");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
        p.close();
        tb.close();
    }

    @Test
    public void testDeserializeObjectWhenTokenIsNotExpectedStartObject() throws Exception {
        // BaseNodeDeserializer.deserializeObject when parser is at END_OBJECT
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeStartObject();
        tb.writeEndObject();
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT

        BaseNodeDeserializer<JsonNode> deser = new BaseNodeDeserializer<JsonNode>(JsonNode.class) {};
        ObjectNode result = deser.deserializeObject(p, mapper.getDeserializationContext(), mapper.getNodeFactory());
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
        p.close();
        tb.close();
    }

    @Test
    public void testDeserializeObjectWhenTokenIsInvalid() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeNumber(999);
        JsonParser p = tb.asParser(mapper);
        p.nextToken(); // VALUE_NUMBER_INT

        BaseNodeDeserializer<JsonNode> deser = new BaseNodeDeserializer<JsonNode>(JsonNode.class) {};
        try {
            deser.deserializeObject(p, mapper.getDeserializationContext(), mapper.getNodeFactory());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
        p.close();
        tb.close();
    }
}
