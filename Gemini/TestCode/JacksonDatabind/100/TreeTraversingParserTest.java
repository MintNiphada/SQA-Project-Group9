package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class TreeTraversingParserTest {

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    @Test
    public void testCodecAndVersion() {
        ObjectCodec codec = new ObjectMapper();
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.nullNode(), codec);
        Assert.assertSame(codec, parser.getCodec());
        parser.setCodec(null);
        Assert.assertNull(parser.getCodec());
        Assert.assertNotNull(parser.version());
    }

    @Test
    public void testLocationsAndContext() throws Exception {
        ObjectNode root = nodeFactory.objectNode();
        root.put("field", "value");
        TreeTraversingParser parser = new TreeTraversingParser(root);
        Assert.assertEquals(JsonLocation.NA, parser.getTokenLocation());
        Assert.assertEquals(JsonLocation.NA, parser.getCurrentLocation());
        Assert.assertNotNull(parser.getParsingContext());
        Assert.assertFalse(parser.hasTextCharacters());
        parser.close();
        Assert.assertNull(parser.getParsingContext());
    }

    @Test
    public void testCloseAndIsClosed() throws Exception {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("test"));
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        Assert.assertNull(parser.nextToken());
        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getEmbeddedObject());
        Assert.assertFalse(parser.isNaN());
    }

    @Test
    public void testTraverseSingleValueNode() throws Exception {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("hello"));
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("hello", parser.getText());
        Assert.assertArrayEquals("hello".toCharArray(), parser.getTextCharacters());
        Assert.assertEquals(5, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNull(parser.nextToken());
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testEmptyContainers() throws Exception {
        ObjectNode emptyObj = nodeFactory.objectNode();
        TreeTraversingParser parserObj = new TreeTraversingParser(emptyObj);
        Assert.assertEquals(JsonToken.START_OBJECT, parserObj.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parserObj.nextToken());
        Assert.assertNull(parserObj.nextToken());

        ArrayNode emptyArr = nodeFactory.arrayNode();
        TreeTraversingParser parserArr = new TreeTraversingParser(emptyArr);
        Assert.assertEquals(JsonToken.START_ARRAY, parserArr.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parserArr.nextToken());
        Assert.assertNull(parserArr.nextToken());
    }

    @Test
    public void testObjectTraversalAndNameOverride() throws Exception {
        ObjectNode root = nodeFactory.objectNode();
        root.put("name", "John");
        root.put("age", 30);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("name", parser.getCurrentName());
        Assert.assertEquals("name", parser.getText());

        parser.overrideCurrentName("customName");
        Assert.assertEquals("customName", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("John", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("age", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(30, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.overrideCurrentName("afterClosed");
        Assert.assertNull(parser.getCurrentName());
    }

    @Test
    public void testArrayTraversalAndSkipChildren() throws Exception {
        ArrayNode root = nodeFactory.arrayNode();
        root.add(10);
        root.add(nodeFactory.arrayNode().add("nested"));
        root.add(nodeFactory.objectNode().put("k", "v"));

        TreeTraversingParser parser = new TreeTraversingParser(root);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren();
        Assert.assertEquals(JsonToken.END_ARRAY, parser.currentToken());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren();
        Assert.assertEquals(JsonToken.END_OBJECT, parser.currentToken());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
    }

    @Test
    public void testNumericAccessors() throws Exception {
        TreeTraversingParser pInt = new TreeTraversingParser(nodeFactory.numberNode(123));
        pInt.nextToken();
        Assert.assertEquals(JsonParser.NumberType.INT, pInt.getNumberType());
        Assert.assertEquals(123, pInt.getIntValue());
        Assert.assertEquals(123L, pInt.getLongValue());
        Assert.assertEquals(123.0f, pInt.getFloatValue(), 0.001f);
        Assert.assertEquals(123.0, pInt.getDoubleValue(), 0.001);
        Assert.assertEquals(BigInteger.valueOf(123), pInt.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal("123"), pInt.getDecimalValue());
        Assert.assertEquals(123, pInt.getNumberValue().intValue());
        Assert.assertEquals("123", pInt.getText());

        TreeTraversingParser pFloat = new TreeTraversingParser(nodeFactory.numberNode(12.34));
        pFloat.nextToken();
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, pFloat.getNumberType());
        Assert.assertEquals(12.34, pFloat.getDoubleValue(), 0.0001);
        Assert.assertEquals(12.34f, pFloat.getFloatValue(), 0.0001f);
        Assert.assertEquals("12.34", pFloat.getText());

        TreeTraversingParser pNaN = new TreeTraversingParser(DoubleNode.valueOf(Double.NaN));
        pNaN.nextToken();
        Assert.assertTrue(pNaN.isNaN());

        TreeTraversingParser pNotNaN = new TreeTraversingParser(nodeFactory.numberNode(10));
        pNotNaN.nextToken();
        Assert.assertFalse(pNotNaN.isNaN());
    }

    @Test
    public void testNumericAccessorsOnNonNumericThrowException() throws Exception {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("notANumber"));
        parser.nextToken();
        try {
            parser.getIntValue();
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("not numeric"));
        }
    }

    @Test
    public void testEmbeddedObjectAndBinary() throws Exception {
        byte[] rawData = new byte[]{1, 2, 3, 4};
        BinaryNode binaryNode = nodeFactory.binaryNode(rawData);
        TreeTraversingParser pBinary = new TreeTraversingParser(binaryNode);
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, pBinary.nextToken());
        Assert.assertArrayEquals(rawData, (byte[]) pBinary.getEmbeddedObject());
        Assert.assertArrayEquals(rawData, pBinary.getBinaryValue(Base64Variants.getDefaultVariant()));
        Assert.assertNotNull(pBinary.getText());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesWritten = pBinary.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        Assert.assertEquals(rawData.length, bytesWritten);
        Assert.assertArrayEquals(rawData, out.toByteArray());

        POJONode pojoByteArray = nodeFactory.pojoNode(rawData);
        TreeTraversingParser pPojoBytes = new TreeTraversingParser(pojoByteArray);
        pPojoBytes.nextToken();
        Assert.assertArrayEquals(rawData, pPojoBytes.getBinaryValue(Base64Variants.getDefaultVariant()));
        Assert.assertSame(rawData, pPojoBytes.getEmbeddedObject());

        POJONode pojoOther = nodeFactory.pojoNode("stringPojo");
        TreeTraversingParser pPojoOther = new TreeTraversingParser(pojoOther);
        pPojoOther.nextToken();
        Assert.assertEquals("stringPojo", pPojoOther.getEmbeddedObject());
        Assert.assertNull(pPojoOther.getBinaryValue(Base64Variants.getDefaultVariant()));

        ByteArrayOutputStream emptyOut = new ByteArrayOutputStream();
        Assert.assertEquals(0, pPojoOther.readBinaryValue(Base64Variants.getDefaultVariant(), emptyOut));

        TreeTraversingParser pNullNode = new TreeTraversingParser(nodeFactory.nullNode());
        pNullNode.nextToken();
        Assert.assertNull(pNullNode.getEmbeddedObject());
        Assert.assertNull(pNullNode.getBinaryValue(Base64Variants.getDefaultVariant()));
        Assert.assertFalse(pNullNode.isNaN());
    }

    @Test
    public void testTextForDefaultTokens() throws Exception {
        TreeTraversingParser pTrue = new TreeTraversingParser(BooleanNode.TRUE);
        pTrue.nextToken();
        Assert.assertEquals("true", pTrue.getText());

        TreeTraversingParser pFalse = new TreeTraversingParser(BooleanNode.FALSE);
        pFalse.nextToken();
        Assert.assertEquals("false", pFalse.getText());

        TreeTraversingParser pNull = new TreeTraversingParser(nodeFactory.nullNode());
        pNull.nextToken();
        Assert.assertEquals("null", pNull.getText());
    }

    @Test
    public void testHandleEOF() {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.nullNode());
        try {
            parser._handleEOF();
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Internal error"));
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
        }
    }
}
