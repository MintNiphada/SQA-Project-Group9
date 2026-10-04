package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.util.RawValue;

@RunWith(MockitoJUnitRunner.class)
public class JsonNodeDeserializerTest {

    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private JsonNodeFactory nodeFactory;
    @Mock
    private ObjectNode objectNode;
    @Mock
    private ArrayNode arrayNode;
    @Mock
    private JsonNode jsonNode;
    @Mock
    private TypeDeserializer typeDeserializer;

    private JsonNodeDeserializer deserializer;

    @Before
    public void setUp() {
        deserializer = new JsonNodeDeserializer();
        when(ctxt.getNodeFactory()).thenReturn(nodeFactory);
    }

    @Test
    public void testGetDeserializerObjectNode() {
        assertTrue(JsonNodeDeserializer.getDeserializer(ObjectNode.class) instanceof JsonNodeDeserializer.ObjectDeserializer);
    }

    @Test
    public void testGetDeserializerArrayNode() {
        assertTrue(JsonNodeDeserializer.getDeserializer(ArrayNode.class) instanceof JsonNodeDeserializer.ArrayDeserializer);
    }

    @Test
    public void testGetDeserializerOther() {
        assertSame(JsonNodeDeserializer.getDeserializer(JsonNode.class), JsonNodeDeserializer.getDeserializer(ValueNode.class));
    }

    @Test
    public void testGetNullValueWithContext() {
        assertSame(NullNode.getInstance(), deserializer.getNullValue(ctxt));
    }

    @Test
    public void testGetNullValueDeprecated() {
        assertSame(NullNode.getInstance(), deserializer.getNullValue());
    }

    @Test
    public void testDeserializeStartObject() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn(null);
        JsonNode result = deserializer.deserialize(parser, ctxt);
        assertSame(objectNode, result);
    }

    @Test
    public void testDeserializeStartArray() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_ARRAY);
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(null);
        try {
            deserializer.deserialize(parser, ctxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testDeserializeDefault() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_STRING);
        when(parser.getText()).thenReturn("text");
        when(nodeFactory.textNode("text")).thenReturn(jsonNode);
        JsonNode result = deserializer.deserialize(parser, ctxt);
        assertSame(jsonNode, result);
    }

    @Test
    public void testObjectDeserializerStartObject() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer objDeser = new JsonNodeDeserializer.ObjectDeserializer();
        when(parser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(parser.nextToken()).thenReturn(JsonToken.END_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        JsonNode result = objDeser.deserialize(parser, ctxt);
        assertSame(objectNode, result);
    }

    @Test
    public void testObjectDeserializerFieldName() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer objDeser = new JsonNodeDeserializer.ObjectDeserializer();
        when(parser.getCurrentToken()).thenReturn(JsonToken.FIELD_NAME);
        when(parser.isExpectedStartObjectToken()).thenReturn(false);
        when(parser.getCurrentName()).thenReturn("field");
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("value");
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(nodeFactory.textNode("value")).thenReturn(jsonNode);
        when(objectNode.replace("field", jsonNode)).thenReturn(null);
        when(parser.nextFieldName()).thenReturn(null);
        JsonNode result = objDeser.deserialize(parser, ctxt);
        assertSame(objectNode, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testObjectDeserializerInvalidToken() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer objDeser = new JsonNodeDeserializer.ObjectDeserializer();
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(ctxt.mappingException(ObjectNode.class)).thenReturn(new JsonMappingException("test"));
        objDeser.deserialize(parser, ctxt);
    }

    @Test
    public void testArrayDeserializerStartArray() throws IOException {
        JsonNodeDeserializer.ArrayDeserializer arrDeser = new JsonNodeDeserializer.ArrayDeserializer();
        when(parser.isExpectedStartArrayToken()).thenReturn(true);
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.END_ARRAY);
        JsonNode result = arrDeser.deserialize(parser, ctxt);
        assertSame(arrayNode, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testArrayDeserializerInvalidToken() throws IOException {
        JsonNodeDeserializer.ArrayDeserializer arrDeser = new JsonNodeDeserializer.ArrayDeserializer();
        when(parser.isExpectedStartArrayToken()).thenReturn(false);
        when(ctxt.mappingException(ArrayNode.class)).thenReturn(new JsonMappingException("test"));
        arrDeser.deserialize(parser, ctxt);
    }

    @Test
    public void testDeserializeWithType() throws IOException {
        when(typeDeserializer.deserializeTypedFromAny(parser, ctxt)).thenReturn(jsonNode);
        Object result = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);
        assertSame(jsonNode, result);
    }

    @Test
    public void testIsCachable() {
        assertTrue(deserializer.isCachable());
    }

    @Test
    public void testHandleDuplicateFieldFeatureDisabled() throws IOException {
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)).thenReturn(false);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleDuplicateFieldFeatureEnabled() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("dup", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING, JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("first", "second");
        when(nodeFactory.textNode("first")).thenReturn(jsonNode);
        when(nodeFactory.textNode("second")).thenReturn(mock(JsonNode.class));
        when(objectNode.replace("dup", jsonNode)).thenReturn(null);
        when(objectNode.replace("dup", any(JsonNode.class))).thenReturn(jsonNode);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)).thenReturn(true);
        when(parser.getTokenLocation()).thenReturn(mock(JsonLocation.class));
        deserializer.deserialize(parser, ctxt);
    }

    @Test
    public void testDeserializeObjectEmpty() throws IOException {
        when(parser.getCurrentToken()).thenReturn(JsonToken.END_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        JsonNode result = deserializer.deserializeObject(parser, ctxt, nodeFactory);
        assertSame(objectNode, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeObjectInvalidFirstToken() throws IOException {
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(parser.isExpectedStartObjectToken()).thenReturn(false);
        when(ctxt.mappingException(any(Class.class), eq(JsonToken.VALUE_NUMBER_INT))).thenReturn(new JsonMappingException("test"));
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithNestedObject() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode, mock(ObjectNode.class));
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("nested", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.END_OBJECT);
        when(objectNode.replace(eq("nested"), any(ObjectNode.class))).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithArray() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("arr", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.START_ARRAY, JsonToken.END_ARRAY);
        when(objectNode.replace(eq("arr"), eq(arrayNode))).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithEmbedded() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("emb", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT);
        when(parser.getEmbeddedObject()).thenReturn(new byte[0]);
        when(nodeFactory.binaryNode(any(byte[].class))).thenReturn(jsonNode);
        when(objectNode.replace("emb", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithString() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("str", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("value");
        when(nodeFactory.textNode("value")).thenReturn(jsonNode);
        when(objectNode.replace("str", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithInt() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("int", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        when(parser.getIntValue()).thenReturn(42);
        when(nodeFactory.numberNode(42)).thenReturn(jsonNode);
        when(objectNode.replace("int", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithBooleanTrue() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("bool", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_TRUE);
        when(nodeFactory.booleanNode(true)).thenReturn(jsonNode);
        when(objectNode.replace("bool", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithBooleanFalse() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("bool", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_FALSE);
        when(nodeFactory.booleanNode(false)).thenReturn(jsonNode);
        when(objectNode.replace("bool", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeObjectWithNull() throws IOException {
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn("null", (String) null);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_NULL);
        when(nodeFactory.nullNode()).thenReturn(jsonNode);
        when(objectNode.replace("null", jsonNode)).thenReturn(null);
        deserializer.deserializeObject(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeArrayWithElements() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING, JsonToken.END_ARRAY);
        when(parser.getText()).thenReturn("elem");
        when(nodeFactory.textNode("elem")).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeArrayUnexpectedEnd() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(null);
        when(ctxt.mappingException(anyString())).thenReturn(new JsonMappingException("test"));
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
    }

    @Test
    public void testDeserializeArrayWithObject() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.END_ARRAY);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn(null);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(objectNode);
    }

    @Test
    public void testDeserializeArrayWithArray() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode, mock(ArrayNode.class));
        when(parser.nextToken()).thenReturn(JsonToken.START_ARRAY, JsonToken.END_ARRAY, JsonToken.END_ARRAY);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(any(ArrayNode.class));
    }

    @Test
    public void testDeserializeArrayWithEmbedded() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT, JsonToken.END_ARRAY);
        when(parser.getEmbeddedObject()).thenReturn(new byte[0]);
        when(nodeFactory.binaryNode(any(byte[].class))).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test
    public void testDeserializeArrayWithInt() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_NUMBER_INT, JsonToken.END_ARRAY);
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        when(parser.getIntValue()).thenReturn(10);
        when(nodeFactory.numberNode(10)).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test
    public void testDeserializeArrayWithTrue() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_TRUE, JsonToken.END_ARRAY);
        when(nodeFactory.booleanNode(true)).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test
    public void testDeserializeArrayWithFalse() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_FALSE, JsonToken.END_ARRAY);
        when(nodeFactory.booleanNode(false)).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test
    public void testDeserializeArrayWithNull() throws IOException {
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_NULL, JsonToken.END_ARRAY);
        when(nodeFactory.nullNode()).thenReturn(jsonNode);
        deserializer.deserializeArray(parser, ctxt, nodeFactory);
        verify(arrayNode).add(jsonNode);
    }

    @Test
    public void testDeserializeAnyStartObject() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(true);
        when(parser.nextFieldName()).thenReturn(null);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(objectNode, result);
    }

    @Test
    public void testDeserializeAnyEndObject() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_END_OBJECT);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.getCurrentToken()).thenReturn(JsonToken.END_OBJECT);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(objectNode, result);
    }

    @Test
    public void testDeserializeAnyStartArray() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_ARRAY);
        when(nodeFactory.arrayNode()).thenReturn(arrayNode);
        when(parser.nextToken()).thenReturn(JsonToken.END_ARRAY);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(arrayNode, result);
    }

    @Test
    public void testDeserializeAnyFieldName() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_FIELD_NAME);
        when(nodeFactory.objectNode()).thenReturn(objectNode);
        when(parser.isExpectedStartObjectToken()).thenReturn(false);
        when(parser.getCurrentToken()).thenReturn(JsonToken.FIELD_NAME);
        when(parser.getCurrentName()).thenReturn("f");
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_NULL);
        when(nodeFactory.nullNode()).thenReturn(jsonNode);
        when(objectNode.replace("f", jsonNode)).thenReturn(null);
        when(parser.nextFieldName()).thenReturn(null);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(objectNode, result);
    }

    @Test
    public void testDeserializeAnyEmbedded() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_EMBEDDED_OBJECT);
        when(parser.getEmbeddedObject()).thenReturn(new byte[0]);
        when(nodeFactory.binaryNode(any(byte[].class))).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyString() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_STRING);
        when(parser.getText()).thenReturn("str");
        when(nodeFactory.textNode("str")).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyNumberInt() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NUMBER_INT);
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        when(parser.getIntValue()).thenReturn(5);
        when(nodeFactory.numberNode(5)).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyNumberFloat() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NUMBER_FLOAT);
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.DOUBLE);
        when(parser.getDoubleValue()).thenReturn(3.14);
        when(nodeFactory.numberNode(3.14)).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyTrue() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_TRUE);
        when(nodeFactory.booleanNode(true)).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyFalse() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_FALSE);
        when(nodeFactory.booleanNode(false)).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testDeserializeAnyNull() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NULL);
        when(nodeFactory.nullNode()).thenReturn(jsonNode);
        JsonNode result = deserializer.deserializeAny(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeAnyInvalid() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NO_TOKEN);
        when(ctxt.mappingException(any(Class.class))).thenReturn(new JsonMappingException("test"));
        deserializer.deserializeAny(parser, ctxt, nodeFactory);
    }

    @Test
    public void testFromIntNormal() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        when(parser.getIntValue()).thenReturn(100);
        when(nodeFactory.numberNode(100)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromInt(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromIntLong() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.LONG);
        when(parser.getLongValue()).thenReturn(100L);
        when(nodeFactory.numberNode(100L)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromInt(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromIntBigInteger() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.BIG_INTEGER);
        BigInteger bi = BigInteger.valueOf(123);
        when(parser.getBigIntegerValue()).thenReturn(bi);
        when(nodeFactory.numberNode(bi)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromInt(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromIntCoercionBigInteger() throws IOException {
        when(ctxt.getDeserializationFeatures()).thenReturn(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.getMask());
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        BigInteger bi = BigInteger.valueOf(42);
        when(parser.getBigIntegerValue()).thenReturn(bi);
        when(nodeFactory.numberNode(bi)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromInt(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromIntCoercionLong() throws IOException {
        when(ctxt.getDeserializationFeatures()).thenReturn(DeserializationFeature.USE_LONG_FOR_INTS.getMask());
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.INT);
        when(parser.getLongValue()).thenReturn(42L);
        when(nodeFactory.numberNode(42L)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromInt(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromFloatBigDecimal() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.BIG_DECIMAL);
        BigDecimal bd = new BigDecimal("1.5");
        when(parser.getDecimalValue()).thenReturn(bd);
        when(nodeFactory.numberNode(bd)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromFloat(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromFloatDoubleWithFeature() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.DOUBLE);
        when(ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)).thenReturn(true);
        BigDecimal bd = new BigDecimal("2.5");
        when(parser.getDecimalValue()).thenReturn(bd);
        when(nodeFactory.numberNode(bd)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromFloat(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromFloatDouble() throws IOException {
        when(parser.getNumberType()).thenReturn(JsonParser.NumberType.DOUBLE);
        when(parser.getDoubleValue()).thenReturn(2.5);
        when(nodeFactory.numberNode(2.5)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromFloat(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromEmbeddedNull() throws IOException {
        when(parser.getEmbeddedObject()).thenReturn(null);
        when(nodeFactory.nullNode()).thenReturn(jsonNode);
        JsonNode result = deserializer._fromEmbedded(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromEmbeddedByteArray() throws IOException {
        byte[] data = new byte[]{1,2,3};
        when(parser.getEmbeddedObject()).thenReturn(data);
        when(nodeFactory.binaryNode(data)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromEmbedded(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromEmbeddedRawValue() throws IOException {
        RawValue raw = new RawValue("raw");
        when(parser.getEmbeddedObject()).thenReturn(raw);
        when(nodeFactory.rawValueNode(raw)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromEmbedded(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromEmbeddedJsonNode() throws IOException {
        when(parser.getEmbeddedObject()).thenReturn(jsonNode);
        JsonNode result = deserializer._fromEmbedded(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }

    @Test
    public void testFromEmbeddedPojo() throws IOException {
        Object pojo = new Object();
        when(parser.getEmbeddedObject()).thenReturn(pojo);
        when(nodeFactory.pojoNode(pojo)).thenReturn(jsonNode);
        JsonNode result = deserializer._fromEmbedded(parser, ctxt, nodeFactory);
        assertSame(jsonNode, result);
    }
}
