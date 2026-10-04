package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenBufferTest {

    @Test
    public void testConstructorsAndVersion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb1 = new TokenBuffer(mapper);
        Assert.assertNotNull(tb1.version());
        Assert.assertSame(mapper, tb1.getCodec());
        Assert.assertFalse(tb1.isClosed());
        Assert.assertTrue(tb1.canWriteBinaryNatively());
        Assert.assertNull(tb1.firstToken());

        TokenBuffer tb2 = new TokenBuffer(mapper, true);
        Assert.assertTrue(tb2.canWriteTypeId());
        Assert.assertTrue(tb2.canWriteObjectId());

        JsonParser jp = mapper.getFactory().createParser("{\"a\":123}");
        TokenBuffer tb3 = new TokenBuffer(jp);
        Assert.assertNotNull(tb3.getCodec());

        DeserializationContext ctxt = mapper.getDeserializationContext();
        TokenBuffer tb4 = new TokenBuffer(jp, ctxt);
        Assert.assertNotNull(tb4);
        jp.close();
    }

    @Test
    public void testGeneratorConfiguration() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        Assert.assertNull(tb.getCodec());
        ObjectMapper mapper = new ObjectMapper();
        tb.setCodec(mapper);
        Assert.assertSame(mapper, tb.getCodec());

        int mask = tb.getFeatureMask();
        Assert.assertTrue(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET) == ((mask & JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask()) != 0));

        tb.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertTrue(tb.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        tb.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertFalse(tb.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));

        tb.setFeatureMask(0);
        Assert.assertEquals(0, tb.getFeatureMask());

        tb.useDefaultPrettyPrinter();
        tb.flush();
        Assert.assertFalse(tb.isClosed());
        tb.close();
        Assert.assertTrue(tb.isClosed());
    }

    @Test
    public void testSimpleWriteAndReadObject() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName("str");
        tb.writeString("value");
        tb.writeFieldName(new SerializedString("num"));
        tb.writeNumber(42);
        tb.writeFieldName("boolTrue");
        tb.writeBoolean(true);
        tb.writeFieldName("boolFalse");
        tb.writeBoolean(false);
        tb.writeFieldName("nullVal");
        tb.writeNull();
        tb.writeFieldName("strNull");
        tb.writeString((String) null);
        tb.writeFieldName("serialNull");
        tb.writeString((SerializableString) null);
        tb.writeEndObject();

        Assert.assertEquals(JsonToken.START_OBJECT, tb.firstToken());
        Assert.assertNotNull(tb.getOutputContext());

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("str", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("value", p.getText());
        Assert.assertEquals("num", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(42, p.getIntValue());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("boolTrue", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals("boolFalse", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        Assert.assertEquals("nullVal", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("strNull", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("serialNull", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testArrayAndNumbers() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartArray();
        tb.writeNumber((short) 1);
        tb.writeNumber(2);
        tb.writeNumber(3L);
        tb.writeNumber(4.5f);
        tb.writeNumber(5.5d);
        tb.writeNumber(new BigDecimal("6.75"));
        tb.writeNumber(new BigInteger("12345678901234567890"));
        tb.writeNumber("99.99");
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertEquals(JsonParser.NumberType.INT, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(2, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(3L, p.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(4.5f, p.getFloatValue(), 0.0001);
        Assert.assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(5.5d, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(new BigDecimal("6.75"), p.getDecimalValue());
        Assert.assertEquals(BigInteger.valueOf(6), p.getBigIntegerValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal("12345678901234567890"), p.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(99.99d, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testSegmentOverflow() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        for (int i = 0; i < 40; i++) {
            tb.writeNumber(i);
        }
        JsonParser p = tb.asParser();
        for (int i = 0; i < 40; i++) {
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            Assert.assertEquals(i, p.getIntValue());
        }
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testRawAndEmbeddedValues() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRawValue("123");
        tb.writeRawValue("abc456def", 3, 3);
        char[] chars = "xyz789".toCharArray();
        tb.writeRawValue(chars, 3, 3);
        byte[] data = new byte[]{1, 2, 3};
        tb.writeBinary(Base64Variants.MIME, data, 0, data.length);
        tb.writeObject(null);
        tb.writeObject(new RawValue("raw"));

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertTrue(p.getEmbeddedObject() instanceof RawValue);

        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
        Assert.assertArrayEquals(data, p.getBinaryValue(Base64Variants.MIME));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Assert.assertEquals(3, p.readBinaryValue(Base64Variants.MIME, baos));
        Assert.assertArrayEquals(data, baos.toByteArray());

        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testBinaryFromString() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("AQID");
        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] expected = new byte[]{1, 2, 3};
        Assert.assertArrayEquals(expected, p.getBinaryValue(Base64Variants.MIME));
        p.close();
    }

    @Test
    public void testWriteObjectAndTreeWithCodec() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        tb.writeObject(12345);
        tb.writeTree(new IntNode(999));
        tb.writeTree(null);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(12345, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(999, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testNativeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        tb.writeTypeId("myTypeId");
        tb.writeObjectId("myObjectId");
        tb.writeStartObject();
        tb.writeTypeId("nestedType");
        tb.writeFieldName("f");
        tb.writeString("v");
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        Assert.assertTrue(p.canReadTypeId());
        Assert.assertTrue(p.canReadObjectId());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("myTypeId", p.getTypeId());
        Assert.assertEquals("myObjectId", p.getObjectId());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("nestedType", p.getTypeId());
        p.close();
    }

    @Test
    public void testAppendAndToString() throws IOException {
        TokenBuffer tb1 = new TokenBuffer(null, true);
        tb1.writeStartObject();
        tb1.writeFieldName("a");
        tb1.writeString("1");
        tb1.writeEndObject();

        TokenBuffer tb2 = new TokenBuffer(null, false);
        tb2.writeStartObject();
        tb2.writeFieldName("b");
        tb2.writeNumber(2);
        tb2.writeEndObject();

        tb1.append(tb2);

        String str = tb1.toString();
        Assert.assertTrue(str.startsWith("[TokenBuffer:"));
        Assert.assertTrue(str.contains("START_OBJECT"));
        Assert.assertTrue(str.contains("FIELD_NAME(a)"));
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        tb.writeStartObject();
        tb.writeFieldName("numInt");
        tb.writeNumber(10);
        tb.writeFieldName("numLong");
        tb.writeNumber(100L);
        tb.writeFieldName("numShort");
        tb.writeNumber((short) 5);
        tb.writeFieldName("numBigInt");
        tb.writeNumber(BigInteger.TEN);
        tb.writeFieldName("numDouble");
        tb.writeNumber(1.25);
        tb.writeFieldName("numFloat");
        tb.writeNumber(2.5f);
        tb.writeFieldName("numBigDec");
        tb.writeNumber(BigDecimal.valueOf(3.75));
        tb.writeFieldName("numStr");
        tb.writeNumber("4.5");
        tb.writeFieldName("boolT");
        tb.writeBoolean(true);
        tb.writeFieldName("boolF");
        tb.writeBoolean(false);
        tb.writeFieldName("nullV");
        tb.writeNull();
        tb.writeFieldName("rawV");
        tb.writeRawValue("{\"x\":1}");
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer target = new TokenBuffer(mapper);
        tb.serialize(target);

        JsonParser p = target.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        while (p.nextToken() != JsonToken.END_OBJECT) {
        }
        p.close();

        JsonParser parser = mapper.getFactory().createParser("{\"k\":\"v\"}");
        TokenBuffer tb3 = new TokenBuffer(mapper);
        tb3.deserialize(parser, mapper.getDeserializationContext());
        JsonParser p3 = tb3.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p3.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p3.nextToken());
        Assert.assertEquals("k", p3.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p3.nextToken());
        Assert.assertEquals("v", p3.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p3.nextToken());
        p3.close();
    }

    @Test
    public void testDeserializeStartingFromFieldName() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{\"k1\":\"v1\",\"k2\":2}");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());

        TokenBuffer tb = new TokenBuffer(mapper);
        tb.deserialize(parser, mapper.getDeserializationContext());

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("k1", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("k2", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testCopyCurrentStructureAndEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"obj\":{\"a\":1},\"arr\":[10,11.5,true,false,null],\"s\":\"txt\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();

        TokenBuffer tb = new TokenBuffer(mapper);
        tb.copyCurrentStructure(p);

        JsonParser reader = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        Assert.assertEquals("obj", reader.nextFieldName());
        Assert.assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        Assert.assertEquals("a", reader.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, reader.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        Assert.assertEquals("arr", reader.nextFieldName());
        Assert.assertEquals(JsonToken.START_ARRAY, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, reader.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, reader.nextToken());
        Assert.assertEquals("s", reader.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, reader.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        Assert.assertNull(reader.nextToken());
        reader.close();
    }

    @Test
    public void testParserNavigationAndOverrides() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("rootProp");
        tb.writeStartArray();
        tb.writeString("item1");
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertNull(p.getText());
        Assert.assertNull(p.getTextCharacters());
        Assert.assertEquals(0, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());
        Assert.assertFalse(p.hasTextCharacters());

        Assert.assertEquals(JsonToken.START_OBJECT, p.peekNextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.peekNextToken());
        Assert.assertEquals("rootProp", p.nextFieldName());
        p.overrideCurrentName("overriddenProp");
        Assert.assertEquals("overriddenProp", p.getCurrentName());

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals("overriddenProp", p.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("item1", p.getText());
        Assert.assertEquals(5, p.getTextLength());
        Assert.assertNotNull(p.getTextCharacters());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        Assert.assertNull(p.peekNextToken());

        p.close();
        Assert.assertTrue(p.isClosed());
        Assert.assertNull(p.nextToken());
        Assert.assertNull(p.peekNextToken());
        Assert.assertNull(p.nextFieldName());
    }

    @Test
    public void testNumericConversionsAndParsingInParser() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeNumber("123.45");
        tb.writeNumber("9876543210");
        tb.writeNumber(100);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(123.45d, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(123L, p.getLongValue());
        Assert.assertEquals(123.45f, p.getFloatValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(9876543210L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(100, p.getIntValue());
        Assert.assertEquals(100L, p.getLongValue());
        Assert.assertEquals(100.0, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(BigInteger.valueOf(100), p.getBigIntegerValue());
        Assert.assertEquals(BigDecimal.valueOf(100), p.getDecimalValue());
        p.close();
    }

    @Test
    public void testLocationAndContext() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser srcParser = mapper.getFactory().createParser("{\"a\": 1}");
        srcParser.nextToken();

        TokenBuffer tb = new TokenBuffer(mapper);
        tb.writeStartObject();
        tb.writeFieldName("a");
        tb.writeNumber(1);
        tb.writeEndObject();

        JsonParser p = tb.asParser(srcParser);
        Assert.assertNotNull(p.getTokenLocation());
        Assert.assertNotNull(p.getCurrentLocation());
        Assert.assertNotNull(p.getParsingContext());
        srcParser.close();
        p.close();
    }

    @Test
    public void testUnsupportedOperations() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        try {
            tb.writeRaw("test");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeRaw("test", 0, 4);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeRaw(new SerializedString("test"));
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeRaw(new char[]{'a'}, 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeRaw('c');
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeRawUTF8String(new byte[]{1}, 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeUTF8String(new byte[]{1}, 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            tb.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(new byte[1]), 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testParserExceptionOnNonNumeric() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("notANumber");
        JsonParser p = tb.asParser();
        p.nextToken();
        try {
            p.getIntValue();
            Assert.fail();
        } catch (JsonParseException e) {
        }
        p.close();
    }
}
