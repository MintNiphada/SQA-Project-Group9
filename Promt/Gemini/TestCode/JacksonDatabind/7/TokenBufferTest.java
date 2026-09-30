package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
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
    public void testConstructorsAndCapabilities() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb1 = new TokenBuffer(mapper);
        Assert.assertSame(mapper, tb1.getCodec());
        Assert.assertFalse(tb1.canWriteTypeId());
        Assert.assertFalse(tb1.canWriteObjectId());
        Assert.assertTrue(tb1.canWriteBinaryNatively());
        Assert.assertNotNull(tb1.version());
        Assert.assertNull(tb1.firstToken());
        Assert.assertFalse(tb1.isClosed());
        tb1.flush();
        tb1.close();
        Assert.assertTrue(tb1.isClosed());

        TokenBuffer tb2 = new TokenBuffer(mapper, true);
        Assert.assertTrue(tb2.canWriteTypeId());
        Assert.assertTrue(tb2.canWriteObjectId());

        JsonParser parser = mapper.getFactory().createParser("{\"a\":1}");
        TokenBuffer tb3 = new TokenBuffer(parser);
        Assert.assertNotNull(tb3.getCodec());
        parser.close();

        TokenBuffer tb4 = new TokenBuffer(null, false);
        Assert.assertNull(tb4.getCodec());
        tb4.setCodec(mapper);
        Assert.assertSame(mapper, tb4.getCodec());
    }

    @Test
    public void testFeatures() {
        TokenBuffer tb = new TokenBuffer(null);
        int defaultFeatures = tb.getFeatureMask();
        Assert.assertEquals(TokenBuffer.DEFAULT_GENERATOR_FEATURES, defaultFeatures);

        tb.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        Assert.assertTrue(tb.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        tb.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        Assert.assertFalse(tb.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        tb.setFeatureMask(12345);
        Assert.assertEquals(12345, tb.getFeatureMask());

        Assert.assertSame(tb, tb.useDefaultPrettyPrinter());
    }

    @Test
    public void testStructuralWritesAndContext() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        Assert.assertTrue(tb.getOutputContext().inRoot());

        tb.writeStartObject();
        Assert.assertTrue(tb.getOutputContext().inObject());
        tb.writeFieldName("field1");
        tb.writeString("val1");

        tb.writeFieldName(new SerializedString("field2"));
        tb.writeStartArray();
        Assert.assertTrue(tb.getOutputContext().inArray());

        tb.writeNumber((short) 1);
        tb.writeNumber(2);
        tb.writeNumber(3L);
        tb.writeNumber(4.0d);
        tb.writeNumber(5.0f);
        tb.writeNumber(new BigDecimal("6.5"));
        tb.writeNumber(new BigInteger("7"));
        tb.writeNumber("8.9");
        tb.writeBoolean(true);
        tb.writeBoolean(false);
        tb.writeNull();

        tb.writeEndArray();
        Assert.assertTrue(tb.getOutputContext().inObject());

        tb.writeEndObject();
        Assert.assertTrue(tb.getOutputContext().inRoot());

        // Unbalanced end should not crash
        tb.writeEndObject();
        tb.writeEndArray();

        Assert.assertEquals(JsonToken.START_OBJECT, tb.firstToken());
    }

    @Test
    public void testSegmentBoundaryCrossing() throws IOException {
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
    public void testWriteValuesWithNulls() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString((String) null);
        tb.writeString((SerializableString) null);
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);
        tb.writeObject(null);
        tb.writeTree(null);

        JsonParser p = tb.asParser();
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
            Assert.assertEquals("null", p.getText());
        }
        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testWriteObjectAndTree() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        tb.writeObject("stringObj");
        tb.writeTree(IntNode.valueOf(42));
        byte[] raw = new byte[]{1, 2, 3};
        tb.writeObject(raw);

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("stringObj", p.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(42, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertArrayEquals(raw, (byte[]) p.getEmbeddedObject());

        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testWriteObjectWithoutCodec() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeObject("embedded");
        tb.writeTree(IntNode.valueOf(10));

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertEquals("embedded", p.getEmbeddedObject());

        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertTrue(p.getEmbeddedObject() instanceof IntNode);

        Assert.assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testStringVariantsAndCharArray() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        char[] chars = "hello world".toCharArray();
        tb.writeString(chars, 0, 5);
        tb.writeString(new SerializedString("world"));

        JsonParser p = tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("hello", p.getText());
        Assert.assertFalse(p.hasTextCharacters());
        Assert.assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        Assert.assertEquals(5, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("world", p.getText());
        p.close();
    }

    @Test
    public void testUnsupportedOperations() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        try {
            tb.writeRawUTF8String(new byte[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeUTF8String(new byte[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw("test");
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw("test", 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw(new SerializedString("test"));
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw(new char[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw('x');
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRawValue("test");
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRawValue("test", 0, 1);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRawValue(new char[0], 0, 0);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(new byte[0]), 0);
            Assert.fail();
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void testBinaryHandling() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        byte[] src = new byte[]{10, 20, 30, 40, 50};
        tb.writeBinary(Base64Variants.MIME, src, 1, 3);

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Assert.assertArrayEquals(new byte[]{20, 30, 40}, (byte[]) p.getEmbeddedObject());
        Assert.assertArrayEquals(new byte[]{20, 30, 40}, p.getBinaryValue(Base64Variants.MIME));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = p.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(3, read);
        Assert.assertArrayEquals(new byte[]{20, 30, 40}, out.toByteArray());

        // Base64 string decoding path
        TokenBuffer tb2 = new TokenBuffer(null);
        String base64Str = Base64Variants.MIME.encode(src);
        tb2.writeString(base64Str);
        TokenBuffer.Parser p2 = (TokenBuffer.Parser) tb2.asParser();
        Assert.assertEquals(JsonToken.VALUE_STRING, p2.nextToken());
        Assert.assertArrayEquals(src, p2.getBinaryValue(Base64Variants.MIME));
        // Reset and reuse byte builder
        Assert.assertArrayEquals(src, p2.getBinaryValue(Base64Variants.MIME));

        p.close();
        p2.close();
    }

    @Test
    public void testParserNumbers() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeNumber((short) 12);
        tb.writeNumber(12345);
        tb.writeNumber(9876543210L);
        tb.writeNumber(1.25f);
        tb.writeNumber(3.1415926535);
        tb.writeNumber(new BigDecimal("123456.7890123456789"));
        tb.writeNumber(new BigInteger("999999999999999999999"));
        tb.writeNumber("456.78");
        tb.writeNumber("123");

        JsonParser p = tb.asParser();

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        Assert.assertEquals(12, p.getIntValue());
        Assert.assertEquals(12L, p.getLongValue());
        Assert.assertEquals(12.0f, p.getFloatValue(), 0.001f);
        Assert.assertEquals(12.0d, p.getDoubleValue(), 0.001d);
        Assert.assertEquals(BigInteger.valueOf(12), p.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal(12), p.getDecimalValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        Assert.assertEquals(12345, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        Assert.assertEquals(9876543210L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());
        Assert.assertEquals(1.25f, p.getFloatValue(), 0.0001f);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        Assert.assertEquals(3.1415926535, p.getDoubleValue(), 0.000000001d);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        Assert.assertEquals(new BigDecimal("123456.7890123456789"), p.getDecimalValue());
        Assert.assertEquals(new BigInteger("123456"), p.getBigIntegerValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
        Assert.assertEquals(new BigInteger("999999999999999999999"), p.getBigIntegerValue());
        Assert.assertEquals(new BigDecimal(new BigInteger("999999999999999999999")), p.getDecimalValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(456.78, p.getDoubleValue(), 0.001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(123L, p.getLongValue());

        p.close();
    }

    @Test
    public void testParserPeekAndNavigation() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("k");
        tb.writeString("v");
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertNull(p.getCurrentToken());
        Assert.assertNull(p.getText());

        Assert.assertEquals(JsonToken.START_OBJECT, p.peekNextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertNotNull(p.getParsingContext());
        Assert.assertEquals(JsonLocation.NA, p.getCurrentLocation());
        Assert.assertEquals(JsonLocation.NA, p.getTokenLocation());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.peekNextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("k", p.getCurrentName());
        Assert.assertEquals("k", p.getText());

        p.overrideCurrentName("newK");
        Assert.assertEquals("newK", p.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("v", p.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.peekNextToken());
        Assert.assertNull(p.nextToken());

        p.close();
        Assert.assertTrue(p.isClosed());
        Assert.assertNull(p.peekNextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testNativeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        tb.writeTypeId("Type123");
        tb.writeObjectId("Obj456");
        tb.writeStartObject();
        tb.writeFieldName("prop");
        tb.writeTypeId("StringType");
        tb.writeString("val");
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        Assert.assertTrue(p.canReadTypeId());
        Assert.assertTrue(p.canReadObjectId());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("Type123", p.getTypeId());
        Assert.assertEquals("Obj456", p.getObjectId());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("StringType", p.getTypeId());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("a");
        tb.writeNumber(100);
        tb.writeFieldName("b");
        tb.writeNumber(200L);
        tb.writeFieldName("c");
        tb.writeNumber((short) 1);
        tb.writeFieldName("d");
        tb.writeNumber(new BigInteger("999"));
        tb.writeFieldName("e");
        tb.writeNumber(3.5d);
        tb.writeFieldName("f");
        tb.writeNumber(4.5f);
        tb.writeFieldName("g");
        tb.writeNumber(new BigDecimal("10.25"));
        tb.writeFieldName("h");
        tb.writeNumber("12.34");
        tb.writeFieldName("i");
        tb.writeBoolean(true);
        tb.writeFieldName("j");
        tb.writeBoolean(false);
        tb.writeFieldName("k");
        tb.writeNull();
        tb.writeFieldName("l");
        tb.writeObject("embeddedVal");
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeString(new SerializedString("sStr"));
        tb.writeEndArray();
        tb.writeEndObject();

        TokenBuffer target = new TokenBuffer(null);
        tb.serialize(target);

        TokenBuffer appendTarget = new TokenBuffer(null);
        appendTarget.append(tb);

        String str = tb.toString();
        Assert.assertTrue(str.contains("START_OBJECT"));
        Assert.assertTrue(str.contains("FIELD_NAME(a)"));

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{\"deser\":123}");
        TokenBuffer deserTb = new TokenBuffer(mapper);
        p.nextToken();
        deserTb.deserialize(p, (DeserializationContext) null);
        Assert.assertEquals(JsonToken.START_OBJECT, deserTb.firstToken());
        p.close();
    }

    @Test
    public void testCopyCurrentStructureAndEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"obj\":{\"num\":123,\"arr\":[true,false,null,\"str\",4.5]}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();

        TokenBuffer tb = new TokenBuffer(mapper);
        tb.copyCurrentStructure(p);

        JsonParser reader = tb.asParser(p);
        Assert.assertNotNull(reader.getCodec());
        Assert.assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
        Assert.assertEquals("obj", reader.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
        Assert.assertEquals("num", reader.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, reader.nextToken());
        Assert.assertEquals(123, reader.getIntValue());
        Assert.assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
        Assert.assertEquals("arr", reader.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, reader.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, reader.nextToken());
        Assert.assertEquals("str", reader.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, reader.nextToken());
        Assert.assertEquals(4.5, reader.getDoubleValue(), 0.001);
        Assert.assertEquals(JsonToken.END_ARRAY, reader.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        Assert.assertNull(reader.nextToken());

        p.close();
        reader.close();
    }

    @Test
    public void testSegmentDirectOperations() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        Assert.assertNull(seg.next());
        Assert.assertFalse(seg.hasIds());
        Assert.assertNull(seg.findObjectId(0));
        Assert.assertNull(seg.findTypeId(0));
        Assert.assertNull(seg.get(0));
        Assert.assertNull(seg.type(0));
        Assert.assertEquals(0, seg.rawType(0));

        for (int i = 0; i < 16; i++) {
            Assert.assertNull(seg.append(i, JsonToken.START_OBJECT));
        }
        TokenBuffer.Segment nextSeg = seg.append(16, JsonToken.END_OBJECT);
        Assert.assertNotNull(nextSeg);

        TokenBuffer.Segment seg2 = new TokenBuffer.Segment();
        for (int i = 0; i < 16; i++) {
            Assert.assertNull(seg2.append(i, JsonToken.VALUE_STRING, "val" + i, "obj" + i, "type" + i));
        }
        Assert.assertTrue(seg2.hasIds());
        Assert.assertEquals("obj0", seg2.findObjectId(0));
        Assert.assertEquals("type0", seg2.findTypeId(0));
        Assert.assertEquals("val0", seg2.get(0));
        TokenBuffer.Segment nextSeg2 = seg2.append(16, JsonToken.VALUE_STRING, "val16", "obj16", "type16");
        Assert.assertNotNull(nextSeg2);
        Assert.assertEquals("obj16", nextSeg2.findObjectId(0));

        TokenBuffer.Segment seg3 = new TokenBuffer.Segment();
        for (int i = 0; i < 16; i++) {
            Assert.assertNull(seg3.append(i, JsonToken.START_ARRAY, "o" + i, "t" + i));
        }
        TokenBuffer.Segment nextSeg3 = seg3.append(16, JsonToken.START_ARRAY, "o16", "t16");
        Assert.assertNotNull(nextSeg3);

        TokenBuffer.Segment seg4 = new TokenBuffer.Segment();
        for (int i = 0; i < 16; i++) {
            Assert.assertNull(seg4.appendRaw(i, 1, "raw" + i));
        }
        Assert.assertEquals(1, seg4.rawType(0));
        TokenBuffer.Segment nextSeg4 = seg4.appendRaw(16, 2, "raw16");
        Assert.assertNotNull(nextSeg4);

        TokenBuffer.Segment seg5 = new TokenBuffer.Segment();
        for (int i = 0; i < 16; i++) {
            Assert.assertNull(seg5.appendRaw(i, 1, "raw" + i, "oid" + i, "tid" + i));
        }
        TokenBuffer.Segment nextSeg5 = seg5.appendRaw(16, 2, "raw16", "oid16", "tid16");
        Assert.assertNotNull(nextSeg5);
    }

    @Test
    public void testParserErrorConditions() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("notANumber");
        tb.writeBoolean(true);

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        p.setCodec(null);
        Assert.assertNull(p.getCodec());
        Assert.assertNotNull(p.version());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        try {
            p.getIntValue();
            Assert.fail();
        } catch (JsonParseException expected) {}

        try {
            p.getBigIntegerValue();
            Assert.fail();
        } catch (JsonParseException expected) {}

        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        try {
            p.getBinaryValue(Base64Variants.MIME);
            Assert.fail();
        } catch (JsonParseException expected) {}

        p.close();
    }
}
