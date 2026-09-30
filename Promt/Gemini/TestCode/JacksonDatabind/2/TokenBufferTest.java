package com.fasterxml.jackson.databind.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;

public class TokenBufferTest {

    @Test
    public void testConstructorsAndConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer buf1 = new TokenBuffer(mapper);
        Assert.assertSame(mapper, buf1.getCodec());
        Assert.assertFalse(buf1.canWriteObjectId());
        Assert.assertFalse(buf1.canWriteTypeId());
        Assert.assertTrue(buf1.canWriteBinaryNatively());
        Assert.assertNotNull(buf1.version());
        Assert.assertFalse(buf1.isClosed());
        Assert.assertNotNull(buf1.getOutputContext());

        TokenBuffer buf2 = new TokenBuffer(mapper, true);
        Assert.assertTrue(buf2.canWriteObjectId());
        Assert.assertTrue(buf2.canWriteTypeId());

        JsonParser parser = mapper.getFactory().createParser("{\"a\":1}");
        TokenBuffer buf3 = new TokenBuffer(parser);
        Assert.assertNotNull(buf3);
        parser.close();

        // Features
        buf1.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Assert.assertTrue(buf1.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        buf1.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Assert.assertFalse(buf1.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        int mask = buf1.getFeatureMask();
        buf1.setFeatureMask(mask);
        Assert.assertEquals(mask, buf1.getFeatureMask());

        buf1.useDefaultPrettyPrinter();
        buf1.setCodec(null);
        Assert.assertNull(buf1.getCodec());
        buf1.flush();
        buf1.close();
        Assert.assertTrue(buf1.isClosed());
    }

    @Test
    public void testBasicWritingAndParsing() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        Assert.assertNull(tb.firstToken());

        tb.writeStartObject();
        Assert.assertEquals(JsonToken.START_OBJECT, tb.firstToken());
        tb.writeFieldName("numInt");
        tb.writeNumber((short) 1);
        tb.writeFieldName("numLong");
        tb.writeNumber(1234567890123L);
        tb.writeFieldName("numDouble");
        tb.writeNumber(12.34);
        tb.writeFieldName("numFloat");
        tb.writeNumber(1.5f);
        tb.writeFieldName("numBigDec");
        tb.writeNumber(new BigDecimal("999.999"));
        tb.writeFieldName("numBigDecNull");
        tb.writeNumber((BigDecimal) null);
        tb.writeFieldName("numBigInt");
        tb.writeNumber(new BigInteger("12345678901234567890"));
        tb.writeFieldName("numBigIntNull");
        tb.writeNumber((BigInteger) null);
        tb.writeFieldName("numStr");
        tb.writeNumber("42.5");
        tb.writeFieldName("boolTrue");
        tb.writeBoolean(true);
        tb.writeFieldName("boolFalse");
        tb.writeBoolean(false);
        tb.writeFieldName("nullVal");
        tb.writeNull();
        tb.writeFieldName("strVal");
        tb.writeString("hello");
        tb.writeFieldName("strNull");
        tb.writeString((String) null);
        tb.writeFieldName(new SerializedString("strSer"));
        tb.writeString(new SerializedString("world"));
        tb.writeFieldName("strChars");
        tb.writeString(new char[]{'a', 'b', 'c'}, 0, 3);
        tb.writeFieldName("embedded");
        tb.writeObject("embeddedObj");
        tb.writeFieldName("tree");
        tb.writeTree(new IntNode(100));
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        Assert.assertFalse(p.isClosed());
        Assert.assertNull(p.getCurrentToken());
        Assert.assertEquals(JsonToken.START_OBJECT, p.peekNextToken());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("numInt", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertEquals(1L, p.getLongValue());
        Assert.assertEquals(1.0, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(1.0f, p.getFloatValue(), 0.0001f);
        Assert.assertEquals(BigInteger.valueOf(1), p.getBigIntegerValue());
        Assert.assertEquals(BigDecimal.valueOf(1), p.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        Assert.assertEquals(1, ((Number) p.getNumberValue()).intValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("numLong", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1234567890123L, p.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, p.getNumberType());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(12.34, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(1.5f, p.getFloatValue(), 0.0001f);
        Assert.assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(new BigDecimal("999.999"), p.getDecimalValue());
        Assert.assertEquals(new BigInteger("999"), p.getBigIntegerValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal(new BigInteger("12345678901234567890")), p.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(42.5, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(42L, p.getLongValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals("true", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        Assert.assertEquals("false", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("null", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("hello", p.getText());
        Assert.assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        Assert.assertEquals(5, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());
        Assert.assertFalse(p.hasTextCharacters());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("world", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("abc", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals("embeddedObj", p.getEmbeddedObject());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertTrue(p.getEmbeddedObject() instanceof IntNode);

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        Assert.assertNull(p.peekNextToken());

        p.close();
        Assert.assertTrue(p.isClosed());
        Assert.assertNull(p.nextToken());
        Assert.assertNull(p.peekNextToken());
    }

    @Test
    public void testSegmentBoundaryAndSpanning() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        for (int i = 0; i < 40; ++i) {
            tb.writeNumber(i);
        }
        JsonParser p = tb.asParser();
        for (int i = 0; i < 40; ++i) {
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            Assert.assertEquals(i, p.getIntValue());
        }
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testNativeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        tb.writeObjectId("obj123");
        tb.writeTypeId("typeXYZ");
        tb.writeStartObject();
        tb.writeFieldName("f");
        tb.writeString("v");
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        Assert.assertTrue(p.canReadObjectId());
        Assert.assertTrue(p.canReadTypeId());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("obj123", p.getObjectId());
        Assert.assertEquals("typeXYZ", p.getTypeId());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());

        String str = tb.toString();
        Assert.assertTrue(str.contains("objectId=obj123"));
        Assert.assertTrue(str.contains("typeXYZ"));
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        TokenBuffer tb = new TokenBuffer(new ObjectMapper(), true);
        tb.writeObjectId("id1");
        tb.writeTypeId("type1");
        tb.writeStartObject();
        tb.writeFieldName("str");
        tb.writeString("val");
        tb.writeFieldName("numInt");
        tb.writeNumber(10);
        tb.writeFieldName("numLong");
        tb.writeNumber(20L);
        tb.writeFieldName("numShort");
        tb.writeNumber((short) 5);
        tb.writeFieldName("numBigInt");
        tb.writeNumber(BigInteger.valueOf(30));
        tb.writeFieldName("numDouble");
        tb.writeNumber(1.23);
        tb.writeFieldName("numFloat");
        tb.writeNumber(4.56f);
        tb.writeFieldName("numBigDec");
        tb.writeNumber(new BigDecimal("7.89"));
        tb.writeFieldName("numStr");
        tb.writeNumber("123.456");
        tb.writeFieldName("boolTrue");
        tb.writeBoolean(true);
        tb.writeFieldName("boolFalse");
        tb.writeBoolean(false);
        tb.writeFieldName("nullVal");
        tb.writeNull();
        tb.writeFieldName("embedded");
        tb.writeObject("testEmbed");
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer target = new TokenBuffer(null, true);
        tb.serialize(target);

        JsonParser p = target.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("id1", p.getObjectId());
        Assert.assertEquals("type1", p.getTypeId());

        TokenBuffer deserBuf = new TokenBuffer(null, false);
        deserBuf.deserialize(target.asParser(), null);
        Assert.assertNotNull(deserBuf.firstToken());
    }

    @Test
    public void testAppend() throws IOException {
        TokenBuffer tb1 = new TokenBuffer(null, false);
        tb1.writeStartArray();
        tb1.writeNumber(1);
        tb1.writeEndArray();

        TokenBuffer tb2 = new TokenBuffer(null, true);
        tb2.writeObjectId("id2");
        tb2.writeStartArray();
        tb2.writeNumber(2);
        tb2.writeEndArray();

        tb1.append(tb2);
        JsonParser p = tb1.asParser();
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(2, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testBinaryHandling() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        tb.writeBinary(Base64Variants.MIME, data, 0, data.length);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] read = p.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(data, read);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int written = p.readBinaryValue(Base64Variants.MIME, baos);
        Assert.assertEquals(5, written);
        Assert.assertArrayEquals(data, baos.toByteArray());

        TokenBuffer tbStr = new TokenBuffer(null);
        tbStr.writeString(Base64Variants.MIME.encode(data));
        JsonParser pStr = tbStr.asParser();
        Assert.assertEquals(JsonToken.VALUE_STRING, pStr.nextToken());
        byte[] readStr = pStr.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(data, readStr);

        try {
            tb.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(data), data.length);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void testParserLocationsAndContext() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("a");
        tb.writeStartArray();
        tb.writeNumber(1);
        tb.writeEndArray();
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        TokenBuffer.Parser tbp = (TokenBuffer.Parser) p;
        Assert.assertNotNull(tbp.version());

        Assert.assertEquals(JsonLocation.NA, p.getCurrentLocation());
        Assert.assertEquals(JsonLocation.NA, p.getTokenLocation());
        JsonLocation customLoc = new JsonLocation("src", 100L, 1, 1);
        tbp.setLocation(customLoc);
        Assert.assertEquals(customLoc, p.getCurrentLocation());
        Assert.assertEquals(customLoc, p.getTokenLocation());

        JsonStreamContext ctx = p.getParsingContext();
        Assert.assertNotNull(ctx);
        Assert.assertTrue(ctx.inRoot());

        p.nextToken(); // START_OBJECT
        p.overrideCurrentName("customRoot");
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        p.overrideCurrentName("renamedA");
        Assert.assertEquals("renamedA", p.getCurrentName());

        p.nextToken(); // START_ARRAY
        p.nextToken(); // 1
        p.nextToken(); // END_ARRAY
        p.nextToken(); // END_OBJECT
    }

    @Test
    public void testParserNumberConversions() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartArray();
        tb.writeNumber("123");
        tb.writeNumber("123.456");
        tb.writeNumber(100);
        tb.writeNumber(200L);
        tb.writeNumber(3.14);
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        p.nextToken(); // START_ARRAY

        p.nextToken(); // "123"
        Assert.assertEquals(123L, p.getLongValue());
        Assert.assertEquals(123, p.getIntValue());

        p.nextToken(); // "123.456"
        Assert.assertEquals(123.456, p.getDoubleValue(), 0.0001);

        p.nextToken(); // 100
        Assert.assertEquals(100, p.getIntValue());
        Assert.assertEquals(100L, p.getLongValue());

        p.nextToken(); // 200L
        Assert.assertEquals(200L, p.getLongValue());
        Assert.assertEquals(BigDecimal.valueOf(200L), p.getDecimalValue());

        p.nextToken(); // 3.14
        Assert.assertEquals(3, p.getIntValue());
        Assert.assertEquals(3L, p.getLongValue());
        Assert.assertEquals(BigInteger.valueOf(3), p.getBigIntegerValue());

        p.nextToken(); // END_ARRAY
    }

    @Test
    public void testParserCodecAndAsParserVariants() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeNumber(42);

        JsonParser srcParser = mapper.getFactory().createParser("123");
        JsonParser p1 = tb.asParser(srcParser);
        Assert.assertNotNull(p1);
        srcParser.close();

        TokenBuffer.Parser p2 = (TokenBuffer.Parser) tb.asParser((ObjectCodec) null);
        p2.setCodec(mapper);
        Assert.assertSame(mapper, p2.getCodec());
    }

    @Test
    public void testToStringAndTruncation() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        for (int i = 0; i < 110; ++i) {
            tb.writeNumber(i);
        }
        String str = tb.toString();
        Assert.assertTrue(str.startsWith("[TokenBuffer: "));
        Assert.assertTrue(str.contains("truncated 10 entries"));
    }

    @Test
    public void testCopyCurrentStructureAndEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{\"a\":[1,{\"b\":true}],\"c\":\"str\",\"d\":null,\"e\":1.5}");
        jp.nextToken(); // START_OBJECT

        TokenBuffer tb = new TokenBuffer(mapper);
        tb.copyCurrentStructure(jp);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("b", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("c", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("d", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("e", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        jp.close();
    }

    @Test
    public void testUnsupportedOperations() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        try {
            tb.writeRawUTF8String(new byte[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeUTF8String(new byte[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRaw("test");
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRaw("test", 0, 4);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRaw(new SerializedString("test"));
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRaw(new char[]{'a'}, 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRaw('a');
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRawValue("test");
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRawValue("test", 0, 4);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}

        try {
            tb.writeRawValue(new char[]{'a'}, 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {}
    }

    @Test
    public void testParserExceptionOnNonNumber() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("notANumber");
        JsonParser p = tb.asParser();
        p.nextToken();
        try {
            p.getIntValue();
            Assert.fail();
        } catch (JsonParseException e) {}

        try {
            p.getNumberType();
            Assert.fail();
        } catch (JsonParseException e) {}

        try {
            p.getBinaryValue(Base64Variants.MIME);
            Assert.fail();
        } catch (JsonParseException e) {}
    }

    @Test
    public void testSegmentDirectOperations() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        Assert.assertFalse(seg.hasIds());
        Assert.assertNull(seg.next());
        Assert.assertNull(seg.type(0));
        Assert.assertEquals(0, seg.rawType(0));
        Assert.assertNull(seg.get(0));

        TokenBuffer.Segment nextSeg = seg.appendRaw(TokenBuffer.Segment.TOKENS_PER_SEGMENT, 1, "test");
        Assert.assertNotNull(nextSeg);
        Assert.assertSame(nextSeg, seg.next());
        Assert.assertEquals(1, nextSeg.rawType(0));
        Assert.assertEquals("test", nextSeg.get(0));

        TokenBuffer.Segment nextSegWithIds = nextSeg.appendRaw(TokenBuffer.Segment.TOKENS_PER_SEGMENT, 2, "test2", "objId", "typeId");
        Assert.assertNotNull(nextSegWithIds);
        Assert.assertTrue(nextSegWithIds.hasIds());
        Assert.assertEquals("objId", nextSegWithIds.findObjectId(0));
        Assert.assertEquals("typeId", nextSegWithIds.findTypeId(0));
    }

    @Test
    public void testSerializeUnrecognizedFloat() throws IOException {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.VALUE_NUMBER_FLOAT, new Object());
        TokenBuffer tb = new TokenBuffer(null);
        tb._first = tb._last = seg;
        try {
            tb.serialize(new TokenBuffer(null));
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized value type for VALUE_NUMBER_FLOAT"));
        }
    }

    @Test
    public void testNullTextParser() throws IOException {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.VALUE_STRING, null);
        TokenBuffer tb = new TokenBuffer(null);
        tb._first = tb._last = seg;
        JsonParser p = tb.asParser();
        p.nextToken();
        Assert.assertNull(p.getText());
        Assert.assertNull(p.getTextCharacters());
        Assert.assertEquals(0, p.getTextLength());
        Assert.assertNull(p.getBinaryValue(Base64Variants.MIME));
    }
}
