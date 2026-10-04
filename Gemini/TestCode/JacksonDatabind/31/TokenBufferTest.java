package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenBufferTest {

    @Test
    public void testConstructorsAndConfig() {
        TokenBuffer tb1 = new TokenBuffer((ObjectCodec) null);
        Assert.assertNull(tb1.getCodec());
        Assert.assertFalse(tb1.canWriteObjectId());
        Assert.assertFalse(tb1.canWriteTypeId());
        Assert.assertNotNull(tb1.version());
        Assert.assertTrue(tb1.canWriteBinaryNatively());
        Assert.assertNotNull(tb1.getOutputContext());

        TokenBuffer tb2 = new TokenBuffer(null, true);
        Assert.assertTrue(tb2.canWriteObjectId());
        Assert.assertTrue(tb2.canWriteTypeId());

        ObjectMapper mapper = new ObjectMapper();
        tb1.setCodec(mapper);
        Assert.assertSame(mapper, tb1.getCodec());

        tb1.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Assert.assertTrue(tb1.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        Assert.assertTrue((tb1.getFeatureMask() & JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask()) != 0);

        tb1.disable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Assert.assertFalse(tb1.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        tb1.setFeatureMask(0xFFFF);
        Assert.assertEquals(0xFFFF, tb1.getFeatureMask());

        Assert.assertSame(tb1, tb1.useDefaultPrettyPrinter());
        Assert.assertFalse(tb1.isClosed());
        try {
            tb1.flush();
            tb1.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(tb1.isClosed());
    }

    @Test
    public void testParserConstructorAndForceBigDecimal() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{\"a\":123.45}");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TokenBuffer tb = new TokenBuffer(p, ctxt);
        Assert.assertSame(mapper, tb.getCodec());
        tb.forceUseOfBigDecimal(true);

        TokenBuffer tbParserOnly = new TokenBuffer(p);
        Assert.assertNotNull(tbParserOnly);
        p.close();
    }

    @Test
    public void testBasicWriteAndParse() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        Assert.assertNull(tb.firstToken());

        tb.writeStartObject();
        tb.writeFieldName("numInt");
        tb.writeNumber(100);
        tb.writeFieldName(new SerializedString("numLong"));
        tb.writeNumber(10000000000L);
        tb.writeFieldName("numShort");
        tb.writeNumber((short) 5);
        tb.writeFieldName("numDouble");
        tb.writeNumber(12.34d);
        tb.writeFieldName("numFloat");
        tb.writeNumber(5.67f);
        tb.writeFieldName("numBigDec");
        tb.writeNumber(new BigDecimal("123.456"));
        tb.writeFieldName("numBigInt");
        tb.writeNumber(new BigInteger("9999999999999999999"));
        tb.writeFieldName("numString");
        tb.writeNumber("456.78");
        tb.writeFieldName("str");
        tb.writeString("hello");
        tb.writeFieldName("strChars");
        tb.writeString(new char[]{'w', 'o', 'r', 'l', 'd'}, 0, 5);
        tb.writeFieldName("strSerial");
        tb.writeString(new SerializedString("serialStr"));
        tb.writeFieldName("nullStr");
        tb.writeString((String) null);
        tb.writeFieldName("nullSerialStr");
        tb.writeString((SerializableString) null);
        tb.writeFieldName("boolTrue");
        tb.writeBoolean(true);
        tb.writeFieldName("boolFalse");
        tb.writeBoolean(false);
        tb.writeFieldName("nullVal");
        tb.writeNull();
        tb.writeFieldName("nullBigDec");
        tb.writeNumber((BigDecimal) null);
        tb.writeFieldName("nullBigInt");
        tb.writeNumber((BigInteger) null);
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        Assert.assertEquals(JsonToken.START_OBJECT, tb.firstToken());

        JsonParser jp = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        Assert.assertEquals("numInt", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        Assert.assertEquals(100, jp.getIntValue());
        Assert.assertEquals(JsonParser.NumberType.INT, jp.getNumberType());
        Assert.assertEquals("100", jp.getText());
        Assert.assertNotNull(jp.getTextCharacters());
        Assert.assertEquals(3, jp.getTextLength());
        Assert.assertEquals(0, jp.getTextOffset());
        Assert.assertFalse(jp.hasTextCharacters());

        Assert.assertEquals("numLong", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        Assert.assertEquals(10000000000L, jp.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());

        Assert.assertEquals("numShort", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        Assert.assertEquals(5, jp.getIntValue());

        Assert.assertEquals("numDouble", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        Assert.assertEquals(12.34, jp.getDoubleValue(), 0.001);
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, jp.getNumberType());

        Assert.assertEquals("numFloat", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        Assert.assertEquals(5.67f, jp.getFloatValue(), 0.001f);
        Assert.assertEquals(JsonParser.NumberType.FLOAT, jp.getNumberType());

        Assert.assertEquals("numBigDec", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        Assert.assertEquals(new BigDecimal("123.456"), jp.getDecimalValue());
        Assert.assertEquals(new BigInteger("123"), jp.getBigIntegerValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_DECIMAL, jp.getNumberType());

        Assert.assertEquals("numBigInt", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        Assert.assertEquals(new BigInteger("9999999999999999999"), jp.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal("9999999999999999999"), jp.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, jp.getNumberType());

        Assert.assertEquals("numString", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        Assert.assertEquals(456.78, jp.getDoubleValue(), 0.001);

        Assert.assertEquals("str", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        Assert.assertEquals("hello", jp.getText());

        Assert.assertEquals("strChars", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        Assert.assertEquals("world", jp.getText());

        Assert.assertEquals("strSerial", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        Assert.assertEquals("serialStr", jp.getText());

        Assert.assertEquals("nullStr", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        Assert.assertEquals("nullSerialStr", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        Assert.assertEquals("boolTrue", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());

        Assert.assertEquals("boolFalse", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());

        Assert.assertEquals("nullVal", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        Assert.assertEquals("nullBigDec", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        Assert.assertEquals("nullBigInt", jp.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        Assert.assertEquals("arr", jp.nextFieldName());
        Assert.assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        Assert.assertNull(jp.nextToken());
        jp.close();
    }

    @Test
    public void testSegmentBoundaryAndPeek() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        for (int i = 0; i < 35; i++) {
            tb.writeNumber(i);
        }
        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.peekNextToken());
        for (int i = 0; i < 35; i++) {
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            Assert.assertEquals(i, p.getIntValue());
        }
        Assert.assertNull(p.peekNextToken());
        Assert.assertNull(p.nextToken());
        p.close();
        Assert.assertNull(p.peekNextToken());
    }

    @Test
    public void testRawValuesAndObjects() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRawValue("raw1");
        tb.writeRawValue("prefix_raw2_suffix", 7, 4);
        tb.writeRawValue(new char[]{'r', 'a', 'w', '3'}, 0, 4);
        byte[] bdata = new byte[]{1, 2, 3};
        tb.writeObject(bdata);
        tb.writeObject(null);
        tb.writeTree(null);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertTrue(p.getEmbeddedObject() instanceof RawValue);
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals("raw3", p.getEmbeddedObject());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertArrayEquals(bdata, (byte[]) p.getEmbeddedObject());
        Assert.assertArrayEquals(bdata, p.getBinaryValue(Base64Variants.MIME));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Assert.assertEquals(3, p.readBinaryValue(Base64Variants.MIME, baos));
        Assert.assertArrayEquals(bdata, baos.toByteArray());

        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64BinaryString() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeBinary(Base64Variants.MIME, new byte[]{10, 20, 30, 40}, 0, 4);
        tb.writeString("AQIDBA==");

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, decoded);
        p.close();
    }

    @Test
    public void testNativeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        tb.writeObjectId("objId1");
        tb.writeTypeId("typeId1");
        tb.writeStartObject();
        tb.writeFieldName("field1");
        tb.writeObjectId("objId2");
        tb.writeNumber(123);
        tb.writeEndObject();

        TokenBuffer tb2 = new TokenBuffer(null, false);
        tb2.append(tb);

        JsonParser p = tb2.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertTrue(p.canReadObjectId());
        Assert.assertTrue(p.canReadTypeId());
        Assert.assertEquals("objId1", p.getObjectId());
        Assert.assertEquals("typeId1", p.getTypeId());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals("objId2", p.getObjectId());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();

        String str = tb2.toString();
        Assert.assertTrue(str.contains("TokenBuffer"));
        Assert.assertTrue(str.contains("objId1"));
    }

    @Test
    public void testSerialize() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName(new SerializedString("strField"));
        tb.writeString(new SerializedString("val"));
        tb.writeFieldName("int");
        tb.writeNumber(1);
        tb.writeFieldName("long");
        tb.writeNumber(2L);
        tb.writeFieldName("short");
        tb.writeNumber((short) 3);
        tb.writeFieldName("bigInt");
        tb.writeNumber(BigInteger.TEN);
        tb.writeFieldName("double");
        tb.writeNumber(4.5d);
        tb.writeFieldName("float");
        tb.writeNumber(5.5f);
        tb.writeFieldName("bigDec");
        tb.writeNumber(BigDecimal.valueOf(6.5));
        tb.writeFieldName("numStr");
        tb.writeNumber("7.5");
        tb.writeFieldName("boolT");
        tb.writeBoolean(true);
        tb.writeFieldName("boolF");
        tb.writeBoolean(false);
        tb.writeFieldName("null");
        tb.writeNull();
        tb.writeFieldName("raw");
        tb.writeRawValue("rawVal");
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer target = new TokenBuffer(null, false);
        tb.serialize(target);

        JsonParser p = target.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("strField", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val", p.getText());
        p.close();
    }

    @Test
    public void testCopyCurrentStructureAndEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":[1,{\"b\":\"c\"}],\"d\":true,\"e\":null,\"f\":1.23,\"g\":99999999999999999999999999}";
        JsonParser p = mapper.getFactory().createParser(json);
        TokenBuffer tb = new TokenBuffer(mapper);
        p.nextToken();
        tb.copyCurrentStructure(p);
        p.close();

        JsonParser p2 = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p2.nextToken());
        Assert.assertEquals("a", p2.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        Assert.assertEquals(1, p2.getIntValue());
        p2.close();
    }

    @Test
    public void testDeserializeSpecialCase() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"fieldName\":123}");
        p.nextToken();
        TokenBuffer tb = new TokenBuffer(mapper);
        tb.deserialize(p, mapper.getDeserializationContext());
        p.close();

        JsonParser p2 = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p2.nextToken());
        Assert.assertEquals("fieldName", p2.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        Assert.assertEquals(123, p2.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        p2.close();
    }

    @Test
    public void testParserContextAndNavigation() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName("rootField");
        tb.writeStartArray();
        tb.writeNumber(10);
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertNull(p.getCurrentName());
        Assert.assertNotNull(p.getParsingContext());
        Assert.assertNotNull(p.getCurrentLocation());
        Assert.assertNotNull(p.getTokenLocation());

        p.setLocation(new JsonLocation("src", 1L, 1, 1));
        Assert.assertEquals(1, p.getCurrentLocation().getLineNr());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("rootField", p.getCurrentName());
        p.overrideCurrentName("overridden");
        Assert.assertEquals("overridden", p.getCurrentName());

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals("overridden", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUnsupported1() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUnsupported2() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw("test", 0, 4);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUnsupported3() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw(new SerializedString("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUnsupported4() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw(new char[]{'a'}, 0, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUnsupported5() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw('a');
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8Unsupported1() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRawUTF8String(new byte[0], 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8Unsupported2() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeUTF8String(new byte[0], 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteBinaryStreamUnsupported() {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeBinary(Base64Variants.MIME, (InputStream) null, 0);
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericAccessThrows() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("abc");
        JsonParser p = tb.asParser();
        p.nextToken();
        p.getIntValue();
    }

    @Test
    public void testStringNumberParsing() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb._appendRaw(JsonToken.VALUE_NUMBER_INT.ordinal(), "12345");
        tb._appendRaw(JsonToken.VALUE_NUMBER_FLOAT.ordinal(), "123.45");

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(12345L, p.getNumberValue().longValue());
        Assert.assertEquals(12345L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(123.45d, p.getNumberValue().doubleValue(), 0.001);
        Assert.assertEquals(123.45d, p.getDoubleValue(), 0.001);
        p.close();
    }

    @Test
    public void testAsParserWithSrc() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser src = mapper.getFactory().createParser("123");
        TokenBuffer tb = new TokenBuffer(mapper);
        tb.writeNumber(456);
        JsonParser p = tb.asParser(src);
        Assert.assertSame(mapper, p.getCodec());
        p.setCodec(null);
        Assert.assertNull(p.getCodec());
        p.close();
        src.close();
    }
}
