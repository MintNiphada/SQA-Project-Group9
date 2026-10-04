package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.databind.util.TokenBuffer.Parser;
import com.fasterxml.jackson.databind.util.TokenBuffer.Segment;

import org.junit.Before;
import org.junit.Test;

public class TokenBufferTest extends BaseTest
{
    private ObjectMapper mapper;
    private TokenBuffer buffer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        buffer = new TokenBuffer(mapper, false);
    }

    @Test
    public void testConstructionWithCodec() {
        TokenBuffer buf = new TokenBuffer(mapper);
        assertNotNull(buf);
        assertFalse(buf.isClosed());
        assertNotNull(buf.getOutputContext());
    }

    @Test
    public void testConstructionWithCodecAndHasNativeIds() {
        TokenBuffer buf = new TokenBuffer(mapper, true);
        assertTrue(buf.canWriteTypeId());
        assertTrue(buf.canWriteObjectId());
    }

    @Test
    public void testConstructionWithJsonParser() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{}");
        TokenBuffer buf = new TokenBuffer(p);
        assertNotNull(buf);
        assertFalse(buf.canWriteTypeId());
        p.close();
    }

    @Test
    public void testConstructionWithJsonParserAndDeserializationContext() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TokenBuffer buf = new TokenBuffer(p, ctxt);
        assertNotNull(buf);
        p.close();
    }

    @Test
    public void testForceUseOfBigDecimal() {
        TokenBuffer buf = buffer.forceUseOfBigDecimal(true);
        assertNotNull(buf);
    }

    @Test
    public void testVersion() {
        assertNotNull(buffer.version());
    }

    @Test
    public void testAsParser() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertNotNull(p);
        p.close();
    }

    @Test
    public void testAsParserWithCodec() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser(mapper);
        assertNotNull(p);
        assertEquals(mapper, p.getCodec());
        p.close();
    }

    @Test
    public void testAsParserWithJsonParser() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser src = mapper.getFactory().createParser("{}");
        JsonParser p = buffer.asParser(src);
        assertNotNull(p);
        src.close();
        p.close();
    }

    @Test
    public void testFirstToken() {
        assertNull(buffer.firstToken());
        buffer.writeStartArray();
        assertEquals(JsonToken.START_ARRAY, buffer.firstToken());
    }

    @Test
    public void testFirstTokenNullWhenFirstNull() {
        buffer = new TokenBuffer(mapper);
        assertNull(buffer.firstToken());
    }

    @Test
    public void testAppendBuffer() throws IOException {
        TokenBuffer other = new TokenBuffer(mapper);
        other.writeNumber(123);
        buffer.append(other);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test
    public void testSerialize() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JsonGenerator gen = mapper.getFactory().createGenerator(bout);
        buffer.serialize(gen);
        gen.flush();
        assertEquals("{\"a\":1}", bout.toString());
        gen.close();
    }

    @Test
    public void testDeserializeFieldNameStart() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        p.nextToken();
        buffer.deserialize(p, mapper.getDeserializationContext());
        p.close();
        JsonParser out = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, out.nextToken());
        assertToken(JsonToken.FIELD_NAME, out.nextToken());
        assertEquals("a", out.getCurrentName());
        out.close();
    }

    @Test
    public void testDeserializeNonFieldNameStart() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        buffer.deserialize(p, mapper.getDeserializationContext());
        p.close();
        JsonParser out = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, out.nextToken());
        out.close();
    }

    @Test
    public void testToString() {
        buffer.writeStartObject();
        String str = buffer.toString();
        assertTrue(str.contains("TokenBuffer"));
        assertTrue(str.contains("START_OBJECT"));
    }

    @Test
    public void testToStringWithManyTokens() {
        for (int i = 0; i < 120; i++) {
            buffer.writeNumber(i);
        }
        String str = buffer.toString();
        assertTrue(str.contains("truncated"));
        assertTrue(str.contains("20"));
    }

    @Test
    public void testEnableDisableFeature() {
        buffer.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        buffer.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test
    public void testGetSetFeatureMask() {
        int mask = buffer.getFeatureMask();
        buffer.setFeatureMask(0);
        assertEquals(0, buffer.getFeatureMask());
        buffer.setFeatureMask(mask);
    }

    @Test
    public void testUseDefaultPrettyPrinter() {
        assertSame(buffer, buffer.useDefaultPrettyPrinter());
    }

    @Test
    public void testSetGetCodec() {
        buffer.setCodec(mapper);
        assertSame(mapper, buffer.getCodec());
    }

    @Test
    public void testGetOutputContext() {
        assertNotNull(buffer.getOutputContext());
    }

    @Test
    public void testCanWriteBinaryNatively() {
        assertTrue(buffer.canWriteBinaryNatively());
    }

    @Test
    public void testFlushDoesNothing() throws IOException {
        buffer.flush();
    }

    @Test
    public void testClose() throws IOException {
        buffer.close();
        assertTrue(buffer.isClosed());
    }

    @Test
    public void testIsClosedInitially() {
        assertFalse(buffer.isClosed());
    }

    @Test
    public void testWriteStartArray() throws IOException {
        buffer.writeStartArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteEndArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteEndArrayUnbalanced() throws IOException {
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteStartObject() throws IOException {
        buffer.writeStartObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteEndObjectUnbalanced() throws IOException {
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteFieldNameString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("field");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field", p.getCurrentName());
        p.close();
    }

    @Test
    public void testWriteFieldNameSerializableString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializedString("serField"));
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("serField", p.getCurrentName());
        p.close();
    }

    @Test
    public void testWriteString() throws IOException {
        buffer.writeString("test");
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("test", p.getText());
        p.close();
    }

    @Test
    public void testWriteStringNull() throws IOException {
        buffer.writeString((String)null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteStringCharArray() throws IOException {
        buffer.writeString("hello".toCharArray(), 0, 5);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        p.close();
    }

    @Test
    public void testWriteStringSerializableStringNull() throws IOException {
        buffer.writeString((SerializableString)null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteRawUTF8StringThrows() {
        try {
            buffer.writeRawUTF8String(new byte[1], 0, 1);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteUTF8StringThrows() {
        try {
            buffer.writeUTF8String(new byte[1], 0, 1);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawTextThrows() {
        try {
            buffer.writeRaw("raw");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawTextOffsetThrows() {
        try {
            buffer.writeRaw("raw", 0, 3);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawSerializableThrows() {
        try {
            buffer.writeRaw(new SerializedString("raw"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawCharsThrows() {
        try {
            buffer.writeRaw(new char[1], 0, 1);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawCharThrows() {
        try {
            buffer.writeRaw('a');
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testWriteRawValueText() throws IOException {
        buffer.writeRawValue("raw");
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("raw", p.getEmbeddedObject().toString());
        p.close();
    }

    @Test
    public void testWriteRawValueTextOffset() throws IOException {
        buffer.writeRawValue("raw0123", 0, 3);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("raw", p.getEmbeddedObject().toString());
        p.close();
    }

    @Test
    public void testWriteRawValueChars() throws IOException {
        buffer.writeRawValue("raw".toCharArray(), 0, 3);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("raw", p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteNumberShort() throws IOException {
        buffer.writeNumber((short)5);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(5, p.getIntValue());
        p.close();
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        buffer.writeNumber(123);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        buffer.writeNumber(9999999999L);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(9999999999L, p.getLongValue());
        p.close();
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        buffer.writeNumber(3.14);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        buffer.writeNumber(1.5f);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.5f, p.getFloatValue(), 0.0);
        p.close();
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        buffer.writeNumber(new BigDecimal("2.5"));
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("2.5"), p.getDecimalValue());
        p.close();
    }

    @Test
    public void testWriteNumberBigDecimalNull() throws IOException {
        buffer.writeNumber((BigDecimal)null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        buffer.writeNumber(BigInteger.TEN);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
        p.close();
    }

    @Test
    public void testWriteNumberBigIntegerNull() throws IOException {
        buffer.writeNumber((BigInteger)null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteNumberString() throws IOException {
        buffer.writeNumber("123");
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(123L, p.getLongValue());
        p.close();
    }

    @Test
    public void testWriteBooleanTrue() throws IOException {
        buffer.writeBoolean(true);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        buffer.writeBoolean(false);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        buffer.writeObject(null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteObjectByteArray() throws IOException {
        byte[] data = new byte[] { 1, 2, 3 };
        buffer.writeObject(data);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[])p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteObjectRawValue() throws IOException {
        RawValue raw = new RawValue("raw");
        buffer.writeObject(raw);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertSame(raw, p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteObjectWithCodec() throws IOException {
        buffer.setCodec(mapper);
        buffer.writeObject(Integer.valueOf(123));
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test
    public void testWriteObjectNoCodec() throws IOException {
        buffer.setCodec(null);
        buffer.writeObject("test");
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("test", p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteTreeNull() throws IOException {
        buffer.writeTree(null);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteTreeWithCodec() throws IOException {
        buffer.setCodec(mapper);
        JsonNode node = mapper.createObjectNode().put("a", 1);
        buffer.writeTree(node);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteTreeNoCodec() throws IOException {
        buffer.setCodec(null);
        JsonNode node = mapper.createObjectNode().put("a", 1);
        buffer.writeTree(node);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertSame(node, p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteBinary() throws IOException {
        byte[] data = new byte[] { 1, 2, 3 };
        buffer.writeBinary(Base64Variants.getDefaultVariant(), data, 0, 3);
        JsonParser p = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[])p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testWriteBinaryInputStreamThrows() {
        try {
            buffer.writeBinary(Base64Variants.getDefaultVariant(), new ByteArrayInputStream(new byte[1]), 1);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) { }
    }

    @Test
    public void testCanWriteTypeId() {
        TokenBuffer buf = new TokenBuffer(mapper, true);
        assertTrue(buf.canWriteTypeId());
    }

    @Test
    public void testCanWriteObjectId() {
        TokenBuffer buf = new TokenBuffer(mapper, true);
        assertTrue(buf.canWriteObjectId());
    }

    @Test
    public void testWriteTypeAndObjectId() {
        buffer = new TokenBuffer(mapper, true);
        buffer.writeTypeId("type");
        buffer.writeObjectId("obj");
        buffer.writeStartObject();
        JsonParser p = buffer.asParser();
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());
        p.close();
    }

    @Test
    public void testCopyCurrentEventStartObject() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, out.nextToken());
        out.close();
    }

    @Test
    public void testCopyCurrentEventEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        TokenBuffer buf2 = new TokenBuffer(mapper);
        buf2.copyCurrentEvent(p);
        p.nextToken();
        buf2.copyCurrentEvent(p);
        p.close();
        JsonParser out = buf2.asParser();
        assertEquals(JsonToken.START_OBJECT, out.nextToken());
        assertEquals(JsonToken.END_OBJECT, out.nextToken());
        out.close();
    }

    @Test
    public void testCopyCurrentEventStartArray() throws IOException {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, out.nextToken());
        out.close();
    }

    @Test
    public void testCopyCurrentEventFieldName() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.FIELD_NAME, out.nextToken());
        assertEquals("a", out.getCurrentName());
        out.close();
    }

    @Test
    public void testCopyCurrentEventVALUE_STRING() throws IOException {
        JsonParser p = mapper.getFactory().createParser("\"text\"");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, out.nextToken());
        assertEquals("text", out.getText());
        out.close();
    }

    @Test
    public void testCopyCurrentEventVALUE_NUMBER_INT() throws IOException {
        JsonParser p = mapper.getFactory().createParser("123");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, out.nextToken());
        assertEquals(123, out.getIntValue());
        out.close();
    }

    @Test
    public void testCopyCurrentEventVALUE_NUMBER_FLOAT() throws IOException {
        JsonParser p = mapper.getFactory().createParser("1.5");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, out.nextToken());
        assertEquals(1.5, out.getDoubleValue(), 0.0);
        out.close();
    }

    @Test
    public void testCopyCurrentEventVALUE_NUMBER_FLOAT_BigDecimal() throws IOException {
        buffer = new TokenBuffer(mapper, false);
        buffer.forceUseOfBigDecimal(true);
        JsonParser p = mapper.getFactory().createParser("1.5");
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, out.nextToken());
        assertEquals(new BigDecimal("1.5"), out.getDecimalValue());
        out.close();
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{\"a\":[1,2]}");
        p.nextToken();
        buffer.copyCurrentStructure(p);
        p.close();
        JsonParser out = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, out.nextToken());
        assertEquals(JsonToken.FIELD_NAME, out.nextToken());
        assertEquals("a", out.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, out.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, out.nextToken());
        assertEquals(1, out.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, out.nextToken());
        assertEquals(2, out.getIntValue());
        assertEquals(JsonToken.END_ARRAY, out.nextToken());
        assertEquals(JsonToken.END_OBJECT, out.nextToken());
        out.close();
    }

    @Test
    public void testParserNextTokenFieldsAndContext() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("f");
        buffer.writeNumber(5);
        buffer.writeEndObject();
        Parser p = (Parser) buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("f", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testParserNextFieldName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeFieldName("b");
        buffer.writeNumber(2);
        buffer.writeEndObject();
        Parser p = (Parser) buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("a", p.nextFieldName());
        assertEquals(1, p.getIntValue());
        assertEquals("b", p.nextFieldName());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testParserIsClosed() throws IOException {
        Parser p = (Parser) buffer.asParser();
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test
    public void testParserGetParsingContext() throws IOException {
        Parser p = (Parser) buffer.asParser();
        assertNotNull(p.getParsingContext());
        p.close();
    }

    @Test
    public void testParserGetText() throws IOException {
        buffer.writeString("hello");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals("hello", p.getText());
        p.close();
    }

    @Test
    public void testParserGetTextNumber() throws IOException {
        buffer.writeNumber(123);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals("123", p.getText());
        p.close();
    }

    @Test
    public void testParserGetTextCharactersAndLengthAndOffset() throws IOException {
        buffer.writeString("hello");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        p.close();
    }

    @Test
    public void testParserHasTextCharacters() throws IOException {
        Parser p = (Parser) buffer.asParser();
        assertFalse(p.hasTextCharacters());
        p.close();
    }

    @Test
    public void testParserGetBigIntegerValue() throws IOException {
        buffer.writeNumber(BigInteger.valueOf(123));
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(BigInteger.valueOf(123), p.getBigIntegerValue());
        p.close();
    }

    @Test
    public void testParserGetDecimalValue() throws IOException {
        buffer.writeNumber(new BigDecimal("2.5"));
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(new BigDecimal("2.5"), p.getDecimalValue());
        p.close();
    }

    @Test
    public void testParserGetDoubleValue() throws IOException {
        buffer.writeNumber(3.14);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(3.14, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test
    public void testParserGetFloatValue() throws IOException {
        buffer.writeNumber(1.5f);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(1.5f, p.getFloatValue(), 0.0);
        p.close();
    }

    @Test
    public void testParserGetIntValue() throws IOException {
        buffer.writeNumber(999);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(999, p.getIntValue());
        p.close();
    }

    @Test
    public void testParserGetLongValue() throws IOException {
        buffer.writeNumber(9999999999L);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(9999999999L, p.getLongValue());
        p.close();
    }

    @Test
    public void testParserGetNumberType() throws IOException {
        buffer.writeNumber(123);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(NumberType.INT, p.getNumberType());
        p.close();
    }

    @Test
    public void testParserGetNumberValue() throws IOException {
        buffer.writeNumber(123);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertEquals(123, p.getNumberValue().intValue());
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testParserGetNumberValueNonNumeric() throws IOException {
        buffer.writeString("text");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        p.getNumberValue();
    }

    @Test
    public void testParserGetEmbeddedObject() throws IOException {
        buffer.writeObject(new byte[] { 1, 3 });
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertNotNull(p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testParserGetEmbeddedObjectNull() throws IOException {
        buffer.writeString("text");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertNull(p.getEmbeddedObject());
        p.close();
    }

    @Test
    public void testParserGetBinaryValueFromEmbedded() throws IOException {
        byte[] data = new byte[] { 1, 2 };
        buffer.writeObject(data);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
        p.close();
    }

    @Test
    public void testParserGetBinaryValueFromString() throws IOException {
        buffer.writeString("hello");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testParserGetBinaryValueError() throws IOException {
        buffer.writeNumber(123);
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        p.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    @Test
    public void testParserReadBinaryValue() throws IOException {
        buffer.writeString("hello");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), bos);
        assertTrue(len > 0);
        p.close();
    }

    @Test
    public void testParserOverrideCurrentName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("f");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        p.nextToken();
        p.overrideCurrentName("newName");
        assertEquals("newName", p.getCurrentName());
        p.close();
    }

    @Test
    public void testSegmentAppendAndNext() throws IOException {
        Segment seg = new Segment();
        for (int i = 0; i < Segment.TOKENS_PER_SEGMENT; i++) {
            assertNull(seg.append(i, JsonToken.VALUE_NULL));
        }
        Segment next = seg.append(Segment.TOKENS_PER_SEGMENT, JsonToken.VALUE_TRUE);
        assertNotNull(next);
        assertEquals(JsonToken.VALUE_TRUE, next.type(0));
        assertSame(next, seg.next());
    }

    @Test
    public void testSegmentAppendWithIds() {
        Segment seg = new Segment();
        assertNull(seg.append(0, JsonToken.VALUE_TRUE, "objId", "typeId"));
        assertEquals(JsonToken.VALUE_TRUE, seg.type(0));
        assertNull(seg.findObjectId(0));
        assertNull(seg.findTypeId(0));
    }

    @Test
    public void testSegmentAppendValue() {
        Segment seg = new Segment();
        assertNull(seg.append(0, JsonToken.VALUE_STRING, "value"));
        assertEquals(JsonToken.VALUE_STRING, seg.type(0));
        assertEquals("value", seg.get(0));
    }

    @Test
    public void testSegmentAppendValueWithIds() {
        Segment seg = new Segment();
        assertNull(seg.append(0, JsonToken.VALUE_STRING, "value", "objId", "typeId"));
        assertEquals(JsonToken.VALUE_STRING, seg.type(0));
        assertEquals("value", seg.get(0));
    }

    @Test
    public void testSegmentAppendRaw() {
        Segment seg = new Segment();
        assertNull(seg.appendRaw(0, 3, "raw"));
        assertEquals(3, seg.rawType(0));
        assertEquals("raw", seg.get(0));
    }

    @Test
    public void testSegmentAppendRawWithIds() {
        Segment seg = new Segment();
        assertNull(seg.appendRaw(0, 3, "raw", "objId", "typeId"));
        assertEquals(3, seg.rawType(0));
        assertEquals("raw", seg.get(0));
    }

    @Test
    public void testSegmentHasIdsFalse() {
        Segment seg = new Segment();
        assertFalse(seg.hasIds());
    }

    @Test
    public void testSegmentFindObjectIdAndTypeIdNull() {
        Segment seg = new Segment();
        assertNull(seg.findObjectId(0));
        assertNull(seg.findTypeId(0));
    }

    @Test
    public void testSegmentTypeAndRawTypeWithIndex() {
        Segment seg = new Segment();
        seg.append(1, JsonToken.VALUE_TRUE);
        assertEquals(JsonToken.VALUE_TRUE, seg.type(1));
        assertNotEquals(0, seg.rawType(1));
    }

    @Test
    public void testAppendRawUsedByInvalidCalled() throws IOException {
        try {
            buffer._appendRaw(1, "illegal");
            fail("Should have thrown exception because of invalid raw type");
        } catch (Exception e) { }
    }

    @Test
    public void testParserGetCurrentNameForStartObjectAndArray() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("f");
        buffer.writeStartArray();
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("f", p.getCurrentName());
        p.close();
    }

    @Test
    public void testParserPeekNextToken() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        Parser p = (Parser) buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.peekNextToken());
        p.close();
    }

    @Test
    public void testParserPeekNextTokenAfterClose() throws IOException {
        Parser p = (Parser) buffer.asParser();
        p.close();
        assertNull(p.peekNextToken());
    }

    @Test
    public void testParserNextTokenAfterClose() throws IOException {
        Parser p = (Parser) buffer.asParser();
        p.close();
        assertNull(p.nextToken());
    }

    @Test
    public void testSegmentNextWhenNull() {
        Segment seg = new Segment();
        assertNull(seg.next());
    }

    @Test
    public void testParserGetTextNullToken() throws IOException {
        Parser p = (Parser) buffer.asParser();
        assertNull(p.getText());
        p.close();
    }

    @Test
    public void testParserGetTextCharactersNull() throws IOException {
        Parser p = (Parser) buffer.asParser();
        assertNull(p.getTextCharacters());
        p.close();
    }

    @Test
    public void testParserGetNumberValueStringWithDot() throws IOException {
        buffer.writeNumber("1.5");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertTrue(p.getNumberValue() instanceof Double);
        assertEquals(1.5, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test
    public void testParserGetNumberValueStringWithoutDot() throws IOException {
        buffer.writeNumber("123");
        Parser p = (Parser) buffer.asParser();
        p.nextToken();
        assertTrue(p.getNumberValue() instanceof Long);
        assertEquals(123L, p.getLongValue());
        p.close();
    }
}
