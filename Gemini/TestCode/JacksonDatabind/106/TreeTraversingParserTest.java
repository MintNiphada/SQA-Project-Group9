package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TreeTraversingParserTest {

    @Test
    public void testCodecAndVersion() {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        JsonNode node = factory.textNode("test");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        Assert.assertNull(parser.getCodec());

        ObjectCodec codec = new ObjectMapper();
        parser.setCodec(codec);
        Assert.assertSame(codec, parser.getCodec());

        Version v = parser.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknownVersion());
    }

    @Test
    public void testValueNodeTraversal() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        TreeTraversingParser parser = new TreeTraversingParser(factory.textNode("hello"));

        Assert.assertFalse(parser.isClosed());
        Assert.assertNull(parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("hello", parser.getText());
        Assert.assertEquals(5, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertArrayEquals("hello".toCharArray(), parser.getTextCharacters());
        Assert.assertFalse(parser.hasTextCharacters());

        Assert.assertNull(parser.nextToken());
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testObjectTraversal() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode obj = factory.objectNode();
        obj.put("intVal", 42);
        obj.put("boolVal", true);
        obj.putNull("nullVal");

        TreeTraversingParser parser = new TreeTraversingParser(obj);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("intVal", parser.getCurrentName());
        Assert.assertEquals("intVal", parser.getText());

        parser.overrideCurrentName("renamedInt");
        Assert.assertEquals("renamedInt", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals("42", parser.getText());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(42L, parser.getLongValue());
        Assert.assertEquals(42.0, parser.getDoubleValue(), 0.001);
        Assert.assertEquals(42.0f, parser.getFloatValue(), 0.001f);
        Assert.assertEquals(BigInteger.valueOf(42), parser.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal("42"), parser.getDecimalValue());
        Assert.assertEquals(42, parser.getNumberValue().intValue());
        Assert.assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        Assert.assertFalse(parser.isNaN());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertEquals("true", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals("null", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testArrayTraversal() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode arr = factory.arrayNode();
        arr.add(10.5);
        arr.add(Double.NaN);

        TreeTraversingParser parser = new TreeTraversingParser(arr);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals("10.5", parser.getText());
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        Assert.assertEquals(10.5, parser.getDoubleValue(), 0.001);
        Assert.assertFalse(parser.isNaN());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertTrue(parser.isNaN());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
    }

    @Test
    public void testEmptyContainers() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        TreeTraversingParser p1 = new TreeTraversingParser(factory.objectNode());
        Assert.assertEquals(JsonToken.START_OBJECT, p1.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p1.nextToken());
        Assert.assertNull(p1.nextToken());

        TreeTraversingParser p2 = new TreeTraversingParser(factory.arrayNode());
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p2.nextToken());
        Assert.assertNull(p2.nextToken());
    }

    @Test
    public void testSkipChildren() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode obj = factory.objectNode();
        obj.put("field", "val");
        TreeTraversingParser p1 = new TreeTraversingParser(obj);
        Assert.assertEquals(JsonToken.START_OBJECT, p1.nextToken());
        p1.skipChildren();
        Assert.assertEquals(JsonToken.END_OBJECT, p1.getCurrentToken());
        Assert.assertNull(p1.nextToken());

        ArrayNode arr = factory.arrayNode();
        arr.add("val");
        TreeTraversingParser p2 = new TreeTraversingParser(arr);
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        p2.skipChildren();
        Assert.assertEquals(JsonToken.END_ARRAY, p2.getCurrentToken());
        Assert.assertNull(p2.nextToken());

        TreeTraversingParser p3 = new TreeTraversingParser(factory.textNode("abc"));
        p3.nextToken();
        p3.skipChildren();
        Assert.assertEquals(JsonToken.VALUE_STRING, p3.getCurrentToken());
    }

    @Test
    public void testEmbeddedObjectsAndBinary() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        byte[] data = new byte[]{1, 2, 3, 4};
        BinaryNode binNode = factory.binaryNode(data);

        TreeTraversingParser pBin = new TreeTraversingParser(binNode);
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, pBin.nextToken());
        Assert.assertArrayEquals(data, (byte[]) pBin.getEmbeddedObject());
        Assert.assertNotNull(pBin.getText());
        Assert.assertArrayEquals(data, pBin.getBinaryValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = pBin.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(4, bytesRead);
        Assert.assertArrayEquals(data, out.toByteArray());

        Object pojo = new Object();
        POJONode pojoNode = factory.pojoNode(pojo);
        TreeTraversingParser pPojo = new TreeTraversingParser(pojoNode);
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, pPojo.nextToken());
        Assert.assertSame(pojo, pPojo.getEmbeddedObject());

        TextNode b64Text = factory.textNode("AQIDBA==");
        TreeTraversingParser pText = new TreeTraversingParser(b64Text);
        pText.nextToken();
        Assert.assertArrayEquals(data, pText.getBinaryValue(Base64Variants.MIME));
    }

    @Test
    public void testCloseAndLocations() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(123));

        Assert.assertNotNull(parser.getParsingContext());
        Assert.assertEquals(JsonLocation.NA, parser.getTokenLocation());
        Assert.assertEquals(JsonLocation.NA, parser.getCurrentLocation());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getEmbeddedObject());
        Assert.assertFalse(parser.isNaN());
        Assert.assertNull(parser.getCurrentName());
        Assert.assertNull(parser.getBinaryValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Assert.assertEquals(0, parser.readBinaryValue(Base64Variants.MIME, out));

        parser.overrideCurrentName("ignored");
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericNodeThrows() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        TreeTraversingParser parser = new TreeTraversingParser(factory.textNode("not a number"));
        parser.nextToken();
        parser.getIntValue();
    }

    @Test(expected = RuntimeException.class)
    public void testHandleEOF() throws JsonParseException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode());
        parser._handleEOF();
    }

    @Test
    public void testNestedStructures() throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode root = factory.objectNode();
        ArrayNode childArr = root.putArray("arr");
        ObjectNode innerObj = childArr.addObject();
        innerObj.put("key", "val");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("arr", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
    }
}
