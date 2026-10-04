package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectCodec;

@RunWith(MockitoJUnitRunner.class)
public class TokenBufferTest {

    @Mock
    private ObjectCodec codec;
    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private JsonGenerator gen;

    private TokenBuffer buffer;

    @Before
    public void setUp() {
        buffer = new TokenBuffer(codec, false);
    }

    @Test
    public void testConstructorWithCodec() {
        TokenBuffer b = new TokenBuffer(codec);
        assertNotNull(b);
        assertFalse(b.isClosed());
        assertNull(b.firstToken());
        assertEquals(JsonWriteContext.createRootContext(null).getClass(), b.getOutputContext().getClass());
    }

    @Test
    public void testConstructorWithCodecAndNativeIds() {
        TokenBuffer b = new TokenBuffer(codec, true);
        assertTrue(b.canWriteTypeId());
        assertTrue(b.canWriteObjectId());
    }

    @Test
    public void testConstructorWithJsonParser() {
        when(parser.getCodec()).thenReturn(codec);
        when(parser.canReadTypeId()).thenReturn(true);
        when(parser.canReadObjectId()).thenReturn(false);
        TokenBuffer b = new TokenBuffer(parser);
        assertTrue(b.canWriteTypeId());
        assertFalse(b.canWriteObjectId());
    }

    @Test
    public void testConstructorWithJsonParserAndDeserializationContext() {
        when(parser.getCodec()).thenReturn(codec);
        when(parser.canReadTypeId()).thenReturn(false);
        when(parser.canReadObjectId()).thenReturn(true);
        TokenBuffer b = new TokenBuffer(parser, ctxt);
        assertFalse(b.canWriteTypeId());
        assertTrue(b.canWriteObjectId());
    }

    @Test
    public void testAsParser() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertNotNull(p);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testAsParserWithCodec() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser(codec);
        assertEquals(codec, p.getCodec());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    }

    @Test
    public void testAsParserWithJsonParser() throws IOException {
        buffer.writeBoolean(true);
        JsonLocation loc = new JsonLocation(null, 100, 1, 1);
        when(parser.getTokenLocation()).thenReturn(loc);
        when(parser.getCodec()).thenReturn(codec);
        JsonParser p = buffer.asParser(parser);
        assertEquals(loc, p.getCurrentLocation());
    }

    @Test
    public void testFirstToken() throws IOException {
        assertNull(buffer.firstToken());
        buffer.writeNull();
        assertEquals(JsonToken.VALUE_NULL, buffer.firstToken());
    }

    @Test
    public void testAppend() throws IOException {
        TokenBuffer other = new TokenBuffer(codec, false);
        other.writeNumber(1);
        other.writeNumber(2);
        buffer.append(other);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testAppendPropagatesNativeIds() throws IOException {
        TokenBuffer other = new TokenBuffer(codec, true);
        other.writeTypeId("type1");
        other.writeNumber(1);
        buffer.append(other);
        assertTrue(buffer.canWriteTypeId());
        assertTrue(buffer.canWriteObjectId());
    }

    @Test
    public void testSerialize() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("field");
        buffer.writeString("value");
        buffer.writeEndObject();
        buffer.serialize(gen);
        verify(gen).writeStartObject();
        verify(gen).writeFieldName("field");
        verify(gen).writeString("value");
        verify(gen).writeEndObject();
    }

    @Test
    public void testSerializeWithNativeIds() throws IOException {
        buffer = new TokenBuffer(codec, true);
        buffer.writeObjectId("objId");
        buffer.writeTypeId("typeId");
        buffer.writeNumber(1);
        buffer.serialize(gen);
        verify(gen).writeObjectId("objId");
        verify(gen).writeTypeId("typeId");
        verify(gen).writeNumber(1);
    }

    @Test
    public void testSerializeWithSerializableString() throws IOException {
        buffer.writeFieldName(new SerializedString("f"));
        buffer.writeString(new SerializedString("v"));
        buffer.serialize(gen);
        verify(gen).writeFieldName(any(SerializableString.class));
        verify(gen).writeString(any(SerializableString.class));
    }

    @Test
    public void testSerializeWithRawValue() throws IOException {
        buffer.writeRawValue("raw");
        buffer.serialize(gen);
        verify(gen).writeObject(any(RawValue.class));
    }

    @Test
    public void testSerializeNumberIntTypes() throws IOException {
        buffer.writeNumber((short) 1);
        buffer.writeNumber(2);
        buffer.writeNumber(3L);
        buffer.writeNumber(BigInteger.TEN);
        buffer.serialize(gen);
        verify(gen).writeNumber((short) 1);
        verify(gen).writeNumber(2);
        verify(gen).writeNumber(3L);
        verify(gen).writeNumber(BigInteger.TEN);
    }

    @Test
    public void testSerializeNumberFloatTypes() throws IOException {
        buffer.writeNumber(1.0);
        buffer.writeNumber(2.0f);
        buffer.writeNumber(BigDecimal.ONE);
        buffer.writeNumber("3.14");
        buffer.serialize(gen);
        verify(gen).writeNumber(1.0);
        verify(gen).writeNumber(2.0f);
        verify(gen).writeNumber(BigDecimal.ONE);
        verify(gen).writeNumber("3.14");
    }

    @Test
    public void testSerializeBooleanAndNull() throws IOException {
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        buffer.writeNull();
        buffer.serialize(gen);
        verify(gen).writeBoolean(true);
        verify(gen).writeBoolean(false);
        verify(gen).writeNull();
    }

    @Test
    public void testSerializeEmbeddedObject() throws IOException {
        buffer.writeObject(new byte[]{1,2,3});
        buffer.serialize(gen);
        verify(gen).writeObject(any(byte[].class));
    }

    @Test
    public void testDeserializeFieldNameStart() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonToken.FIELD_NAME.id());
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING, JsonToken.END_OBJECT, null);
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getText()).thenReturn("val");
        buffer.deserialize(parser, ctxt);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testDeserializeNotFieldName() throws IOException {
        when(parser.getCurrentTokenId()).thenReturn(JsonToken.START_OBJECT.id());
        when(parser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(parser.nextToken()).thenReturn(JsonToken.END_OBJECT, null);
        buffer.deserialize(parser, ctxt);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testToString() throws IOException {
        buffer.writeStartArray();
        buffer.writeNumber(1);
        buffer.writeEndArray();
        String str = buffer.toString();
        assertTrue(str.contains("START_ARRAY"));
        assertTrue(str.contains("VALUE_NUMBER_INT"));
        assertTrue(str.contains("END_ARRAY"));
    }

    @Test
    public void testFeatureManagement() {
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        buffer.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        buffer.disable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        int mask = buffer.getFeatureMask();
        buffer.setFeatureMask(mask | JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask());
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
    }

    @Test
    public void testUseDefaultPrettyPrinter() {
        assertSame(buffer, buffer.useDefaultPrettyPrinter());
    }

    @Test
    public void testSetCodecGetCodec() {
        buffer.setCodec(codec);
        assertSame(codec, buffer.getCodec());
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
    public void testFlushCloseIsClosed() throws IOException {
        assertFalse(buffer.isClosed());
        buffer.flush();
        assertFalse(buffer.isClosed());
        buffer.close();
        assertTrue(buffer.isClosed());
    }

    @Test
    public void testWriteStartArrayEndArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testWriteStartObjectEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testWriteFieldNameString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("name", p.getCurrentName());
    }

    @Test
    public void testWriteFieldNameSerializableString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializedString("name"));
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("name", p.getCurrentName());
    }

    @Test
    public void testWriteString() throws IOException {
        buffer.writeString("text");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
    }

    @Test
    public void testWriteStringNull() throws IOException {
        buffer.writeString((String) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteStringCharArray() throws IOException {
        buffer.writeString("text".toCharArray(), 0, 4);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
    }

    @Test
    public void testWriteStringSerializableString() throws IOException {
        buffer.writeString(new SerializedString("text"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
    }

    @Test
    public void testWriteStringSerializableStringNull() throws IOException {
        buffer.writeString((SerializableString) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String() throws IOException {
        buffer.writeRawUTF8String(new byte[0], 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String() throws IOException {
        buffer.writeUTF8String(new byte[0], 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawString() throws IOException {
        buffer.writeRaw("raw");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawStringOffset() throws IOException {
        buffer.writeRaw("raw", 0, 3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawSerializableString() throws IOException {
        buffer.writeRaw(new SerializedString("raw"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawCharArray() throws IOException {
        buffer.writeRaw("raw".toCharArray(), 0, 3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawChar() throws IOException {
        buffer.writeRaw('r');
    }

    @Test
    public void testWriteRawValue() throws IOException {
        buffer.writeRawValue("raw");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertTrue(p.getEmbeddedObject() instanceof RawValue);
    }

    @Test
    public void testWriteRawValueSubstring() throws IOException {
        buffer.writeRawValue("raw", 1, 2);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("aw", ((RawValue) p.getEmbeddedObject()).rawValue());
    }

    @Test
    public void testWriteRawValueCharArray() throws IOException {
        buffer.writeRawValue("raw".toCharArray(), 0, 3);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("raw", p.getEmbeddedObject());
    }

    @Test
    public void testWriteNumberShort() throws IOException {
        buffer.writeNumber((short) 42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        buffer.writeNumber(42L);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42L, p.getLongValue());
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        buffer.writeNumber(3.14);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        buffer.writeNumber(3.14f);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14f, p.getFloatValue(), 0.0);
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        buffer.writeNumber(new BigDecimal("3.14"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("3.14"), p.getDecimalValue());
    }

    @Test
    public void testWriteNumberBigDecimalNull() throws IOException {
        buffer.writeNumber((BigDecimal) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        buffer.writeNumber(BigInteger.TEN);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
    }

    @Test
    public void testWriteNumberBigIntegerNull() throws IOException {
        buffer.writeNumber((BigInteger) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteNumberString() throws IOException {
        buffer.writeNumber("42");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("42", p.getText());
    }

    @Test
    public void testWriteBoolean() throws IOException {
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
    }

    @Test
    public void testWriteNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        buffer.writeObject(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteObjectByteArray() throws IOException {
        byte[] data = {1,2,3};
        buffer.writeObject(data);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
    }

    @Test
    public void testWriteObjectRawValue() throws IOException {
        RawValue raw = new RawValue("raw");
        buffer.writeObject(raw);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertSame(raw, p.getEmbeddedObject());
    }

    @Test
    public void testWriteObjectWithCodec() throws IOException {
        buffer.setCodec(codec);
        Object value = new Object();
        buffer.writeObject(value);
        verify(codec).writeValue(buffer, value);
    }

    @Test
    public void testWriteObjectWithoutCodec() throws IOException {
        buffer = new TokenBuffer(null, false);
        Object value = "test";
        buffer.writeObject(value);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(value, p.getEmbeddedObject());
    }

    @Test
    public void testWriteTreeNull() throws IOException {
        buffer.writeTree(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteTreeWithCodec() throws IOException {
        buffer.setCodec(codec);
        TreeNode node = mock(TreeNode.class);
        buffer.writeTree(node);
        verify(codec).writeTree(buffer, node);
    }

    @Test
    public void testWriteTreeWithoutCodec() throws IOException {
        buffer = new TokenBuffer(null, false);
        TreeNode node = mock(TreeNode.class);
        buffer.writeTree(node);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertSame(node, p.getEmbeddedObject());
    }

    @Test
    public void testWriteBinaryByteArray() throws IOException {
        byte[] data = {1,2,3};
        buffer.writeBinary(data);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteBinaryInputStream() throws IOException {
        buffer.writeBinary(Base64Variants.getDefaultVariant(), mock(InputStream.class), 0);
    }

    @Test
    public void testCanWriteTypeIdObjectId() {
        assertFalse(buffer.canWriteTypeId());
        assertFalse(buffer.canWriteObjectId());
        buffer = new TokenBuffer(codec, true);
        assertTrue(buffer.canWriteTypeId());
        assertTrue(buffer.canWriteObjectId());
    }

    @Test
    public void testWriteTypeIdObjectId() throws IOException {
        buffer = new TokenBuffer(codec, true);
        buffer.writeTypeId("type");
        buffer.writeObjectId("obj");
        buffer.writeNumber(1);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("type", p.getTypeId());
        assertEquals("obj", p.getObjectId());
    }

    @Test
    public void testCopyCurrentEvent() throws IOException {
        when(parser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        buffer.copyCurrentEvent(parser);
        assertEquals(JsonToken.START_OBJECT, buffer.firstToken());
    }

    @Test
    public void testCopyCurrentEventWithNativeIds() throws IOException {
        buffer = new TokenBuffer(codec, true);
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.getTypeId()).thenReturn("type");
        when(parser.getObjectId()).thenReturn("obj");
        when(parser.hasTextCharacters()).thenReturn(false);
        when(parser.getText()).thenReturn("text");
        buffer.copyCurrentEvent(parser);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("type", p.getTypeId());
        assertEquals("obj", p.getObjectId());
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        when(parser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(parser.nextToken()).thenReturn(JsonToken.END_OBJECT, null);
        buffer.copyCurrentStructure(parser);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testCopyCurrentStructureFieldName() throws IOException {
        when(parser.getCurrentToken()).thenReturn(JsonToken.FIELD_NAME);
        when(parser.getCurrentName()).thenReturn("field");
        when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING, null);
        when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(parser.hasTextCharacters()).thenReturn(false);
        when(parser.getText()).thenReturn("value");
        buffer.copyCurrentStructure(parser);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
    }

    @Test
    public void testParserNextToken() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testParserNextFieldName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("f");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("f", p.nextFieldName());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testParserGetText() throws IOException {
        buffer.writeString("text");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("text", p.getText());
    }

    @Test
    public void testParserGetNumberValueInt() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(42, p.getNumberValue().intValue());
    }

    @Test
    public void testParserGetNumberValueString() throws IOException {
        buffer.writeNumber("42");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(42L, p.getNumberValue().longValue());
    }

    @Test
    public void testParserGetBigIntegerValue() throws IOException {
        buffer.writeNumber(BigInteger.TEN);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
    }

    @Test
    public void testParserGetDecimalValue() throws IOException {
        buffer.writeNumber(new BigDecimal("3.14"));
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(new BigDecimal("3.14"), p.getDecimalValue());
    }

    @Test
    public void testParserGetDoubleValue() throws IOException {
        buffer.writeNumber(3.14);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(3.14, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testParserGetFloatValue() throws IOException {
        buffer.writeNumber(3.14f);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(3.14f, p.getFloatValue(), 0.0);
    }

    @Test
    public void testParserGetIntValue() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testParserGetLongValue() throws IOException {
        buffer.writeNumber(42L);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(42L, p.getLongValue());
    }

    @Test
    public void testParserGetNumberType() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(NumberType.INT, p.getNumberType());
    }

    @Test
    public void testParserGetEmbeddedObject() throws IOException {
        buffer.writeObject(new byte[]{1});
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertArrayEquals(new byte[]{1}, (byte[]) p.getEmbeddedObject());
    }

    @Test
    public void testParserGetBinaryValueFromString() throws IOException {
        buffer.writeString("dGVzdA==");
        JsonParser p = buffer.asParser();
        p.nextToken();
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("test", new String(result));
    }

    @Test
    public void testParserGetBinaryValueFromEmbedded() throws IOException {
        byte[] data = {1,2,3};
        buffer.writeObject(data);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test
    public void testParserReadBinaryValue() throws IOException {
        buffer.writeString("dGVzdA==");
        JsonParser p = buffer.asParser();
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(4, len);
        assertEquals("test", out.toString());
    }

    @Test
    public void testParserCanReadObjectIdTypeId() {
        buffer = new TokenBuffer(codec, true);
        JsonParser p = buffer.asParser();
        assertTrue(p.canReadObjectId());
        assertTrue(p.canReadTypeId());
    }

    @Test
    public void testParserGetTypeIdObjectId() throws IOException {
        buffer = new TokenBuffer(codec, true);
        buffer.writeTypeId("type");
        buffer.writeObjectId("obj");
        buffer.writeNumber(1);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("type", p.getTypeId());
        assertEquals("obj", p.getObjectId());
    }

    @Test
    public void testParserGetCurrentName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("field");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertNull(p.getCurrentName());
        p.nextToken();
        assertEquals("field", p.getCurrentName());
    }

    @Test
    public void testParserOverrideCurrentName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("old");
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.nextToken();
        p.overrideCurrentName("new");
        assertEquals("new", p.getCurrentName());
    }

    @Test
    public void testParserGetParsingContext() throws IOException {
        buffer.writeStartArray();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test
    public void testParserGetTokenLocation() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonLocation.NA, p.getTokenLocation());
    }

    @Test
    public void testParserCloseIsClosed() throws IOException {
        JsonParser p = buffer.asParser();
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test
    public void testParserPeekNextToken() throws IOException {
        buffer.writeNumber(1);
        buffer.writeNumber(2);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.peekNextToken());
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.peekNextToken());
    }

    @Test
    public void testSegmentAppend() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        for (int i = 0; i < 16; i++) {
            assertNull(seg.append(i, JsonToken.VALUE_NULL));
        }
        TokenBuffer.Segment next = seg.append(16, JsonToken.VALUE_NULL);
        assertNotNull(next);
        assertEquals(JsonToken.VALUE_NULL, next.type(0));
    }

    @Test
    public void testSegmentType() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.START_OBJECT);
        assertEquals(JsonToken.START_OBJECT, seg.type(0));
    }

    @Test
    public void testSegmentRawType() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.appendRaw(0, 5, "value");
        assertEquals(5, seg.rawType(0));
    }

    @Test
    public void testSegmentGet() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.VALUE_STRING, "text");
        assertEquals("text", seg.get(0));
    }

    @Test
    public void testSegmentNext() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        assertNull(seg.next());
        seg.append(16, JsonToken.VALUE_NULL);
        assertNotNull(seg.next());
    }

    @Test
    public void testSegmentHasIds() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        assertFalse(seg.hasIds());
        seg.append(0, JsonToken.VALUE_NULL, "obj", "type");
        assertTrue(seg.hasIds());
    }

    @Test
    public void testSegmentFindObjectIdTypeId() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.VALUE_NULL, "obj", "type");
        assertEquals("obj", seg.findObjectId(0));
        assertEquals("type", seg.findTypeId(0));
    }

    @Test
    public void testSegmentAssignNativeIds() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        seg.append(0, JsonToken.VALUE_NULL, "obj", "type");
        assertEquals("obj", seg.findObjectId(0));
        assertEquals("type", seg.findTypeId(0));
    }
}
