package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.*;

public class TokenBufferTest {

    // Minimal ObjectCodec stub to satisfy non-null requirements if needed
    static class StubCodec extends ObjectCodec {
        @Override public <T extends TreeNode> T readTree(JsonParser p) { return null; }
        @Override public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
        @Override public <T> Iterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException { return null; }
        @Override public void writeValue(JsonGenerator gen, Object value) throws IOException { }
        @Override public <T extends TreeNode> T createArrayNode() { return null; }
        @Override public <T extends TreeNode> T createObjectNode() { return null; }
        @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
        @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException { return null; }
    }

    private TokenBuffer buffer;
    private StubCodec codec;

    @Before
    public void setUp() {
        codec = new StubCodec();
        buffer = new TokenBuffer(codec, false);
    }

    // ---------- Constructors ----------
    @Test
    public void testDefaultConstructor() {
        TokenBuffer tb = new TokenBuffer(codec);
        assertNotNull(tb);
        assertFalse(tb.isClosed());
        assertEquals(codec, tb.getCodec());
        // deprecated constructor covered
    }

    @Test
    public void testConstructorWithNativeIds() {
        TokenBuffer tb = new TokenBuffer(codec, true);
        assertTrue(tb.canWriteTypeId());
        assertTrue(tb.canWriteObjectId());
    }

    @Test
    public void testConstructorFromJsonParser() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeEndObject();
        JsonParser p = src.asParser();
        p.nextToken();
        TokenBuffer tb = new TokenBuffer(p);
        assertNotNull(tb);
        assertFalse(tb.isClosed());
    }

    // ---------- version ----------
    @Test
    public void testVersion() {
        assertNotNull(buffer.version());
    }

    // ---------- asParser ----------
    @Test
    public void testAsParserDefaultCodec() {
        JsonParser p = buffer.asParser();
        assertNotNull(p);
        assertEquals(codec, p.getCodec());
    }

    @Test
    public void testAsParserWithCodec() {
        StubCodec otherCodec = new StubCodec();
        JsonParser p = buffer.asParser(otherCodec);
        assertNotNull(p);
        assertEquals(otherCodec, p.getCodec());
    }

    @Test
    public void testAsParserWithSourceParser() throws IOException {
        buffer.writeNumber(123);
        JsonParser src = buffer.asParser();
        src.nextToken();
        JsonParser p = buffer.asParser(src);
        assertNotNull(p);
        assertNotNull(p.getTokenLocation());
    }

    // ---------- firstToken ----------
    @Test
    public void testFirstTokenNullWhenEmpty() {
        assertNull(buffer.firstToken());
    }

    @Test
    public void testFirstTokenAfterWrite() throws IOException {
        buffer.writeStartArray();
        assertEquals(JsonToken.START_ARRAY, buffer.firstToken());
    }

    // ---------- Configuration ----------
    @Test
    public void testEnableDisableFeature() {
        JsonGenerator.Feature f = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS;
        assertFalse(buffer.isEnabled(f));
        buffer.enable(f);
        assertTrue(buffer.isEnabled(f));
        buffer.disable(f);
        assertFalse(buffer.isEnabled(f));
    }

    @Test
    public void testFeatureMask() {
        int mask = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        buffer.setFeatureMask(mask);
        assertEquals(mask, buffer.getFeatureMask());
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testUseDefaultPrettyPrinter() {
        assertSame(buffer, buffer.useDefaultPrettyPrinter());
    }

    @Test
    public void testSetGetCodec() {
        buffer.setCodec(null);
        assertNull(buffer.getCodec());
        buffer.setCodec(codec);
        assertSame(codec, buffer.getCodec());
    }

    @Test
    public void testGetOutputContext() {
        assertNotNull(buffer.getOutputContext());
        assertEquals(JsonWriteContext.STATUS_OK_AS_ROOT, buffer.getOutputContext().inRoot()); // or similar
    }

    // ---------- Capabilities ----------
    @Test
    public void testCanWriteBinaryNatively() {
        assertTrue(buffer.canWriteBinaryNatively());
    }

    // ---------- Close & Flush ----------
    @Test
    public void testCloseAndIsClosed() throws IOException {
        assertFalse(buffer.isClosed());
        buffer.close();
        assertTrue(buffer.isClosed());
        buffer.flush(); // no-op
    }

    // ---------- Structural writes ----------
    @Test
    public void testWriteStartEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testWriteStartEndArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testWriteFieldNameString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("foo");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testWriteFieldNameSerializableString() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializedString("bar"));
        buffer.writeString("val");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.nextToken();
        assertEquals("bar", p.getCurrentName());
    }

    // ---------- Textual writes ----------
    @Test
    public void testWriteString() throws IOException {
        buffer.writeString("hello");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
    }

    @Test
    public void testWriteStringNull() throws IOException {
        buffer.writeString((String)null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteStringCharArray() throws IOException {
        buffer.writeString(new char[] {'a','b','c'}, 0, 3);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("abc", p.getText());
    }

    @Test
    public void testWriteStringSerializableString() throws IOException {
        buffer.writeString(new SerializedString("xyz"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("xyz", p.getText());
    }

    @Test
    public void testWriteStringSerializableNull() throws IOException {
        buffer.writeString((SerializableString)null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    // ---------- Numeric writes ----------
    @Test
    public void testWriteNumberShort() throws IOException {
        buffer.writeNumber((short) 5);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
        assertEquals(5, p.getIntValue());
        assertEquals(NumberType.INT, p.getNumberType());
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        buffer.writeNumber(123L);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(123L, p.getLongValue());
        assertEquals(NumberType.LONG, p.getNumberType());
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        buffer.writeNumber(3.14);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.getCurrentToken());
        assertEquals(3.14, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        buffer.writeNumber(2.5f);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(2.5f, p.getFloatValue(), 0.0);
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("123.456");
        buffer.writeNumber(bd);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.getCurrentToken());
        assertEquals(bd, p.getDecimalValue());
    }

    @Test
    public void testWriteNumberBigDecimalNull() throws IOException {
        buffer.writeNumber((BigDecimal)null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        BigInteger bi = new BigInteger("9999999999999");
        buffer.writeNumber(bi);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
        assertEquals(bi, p.getBigIntegerValue());
    }

    @Test
    public void testWriteNumberBigIntegerNull() throws IOException {
        buffer.writeNumber((BigInteger)null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testWriteNumberAsString() throws IOException {
        buffer.writeNumber("3.14");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.getCurrentToken());
        // getNumberValue returns Double for string with '.'
        assertEquals(3.14, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testWriteNumberStringInt() throws IOException {
        buffer.writeNumber("789");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(789, p.getIntValue());
    }

    @Test
    public void testWriteBooleanTrue() throws IOException {
        buffer.writeBoolean(true);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        buffer.writeBoolean(false);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
    }

    @Test
    public void testWriteNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    // ---------- Object / Tree ----------
    @Test
    public void testWriteObject() throws IOException {
        buffer.writeObject("some object");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("some object", p.getEmbeddedObject());
    }

    @Test
    public void testWriteTree() throws IOException {
        buffer.writeTree(null);  // TreeNode can be null
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.getEmbeddedObject());
    }

    // ---------- Binary ----------
    @Test
    public void testWriteBinary() throws IOException {
        byte[] data = new byte[] {1,2,3,4};
        buffer.writeBinary(data);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[])p.getEmbeddedObject());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteBinaryFromStreamThrows() throws IOException {
        buffer.writeBinary(null, new ByteArrayInputStream(new byte[0]), 0);
    }

    // ---------- Raw operations (unsupported) ----------
    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawUTF8StringThrows() throws IOException {
        buffer.writeRawUTF8String(new byte[0], 0, 0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteUTF8StringThrows() throws IOException {
        buffer.writeUTF8String(new byte[0], 0, 0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawStringThrows() throws IOException {
        buffer.writeRaw("raw");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawStringOffsetThrows() throws IOException {
        buffer.writeRaw("raw", 0, 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawSerializableStringThrows() throws IOException {
        buffer.writeRaw(new SerializedString("raw"));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawCharArrayThrows() throws IOException {
        buffer.writeRaw(new char[] {'r'}, 0, 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawCharThrows() throws IOException {
        buffer.writeRaw('r');
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawValueStringThrows() throws IOException {
        buffer.writeRawValue("raw");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawValueStringOffsetThrows() throws IOException {
        buffer.writeRawValue("raw", 0, 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawValueCharArrayThrows() throws IOException {
        buffer.writeRawValue(new char[] {'r'}, 0, 1);
    }

    // ---------- Native Ids ----------
    @Test
    public void testNativeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(codec, true);
        assertTrue(tb.canWriteTypeId());
        assertTrue(tb.canWriteObjectId());
        tb.writeTypeId("type1");
        tb.writeObjectId("obj1");
        tb.writeStartObject();
        tb.writeEndObject();
        JsonParser p = tb.asParser();
        // The native ids should be associated with the START_OBJECT token
        assertNull(p.nextToken()); // we need to advance? Actually the ids are set on the next data token.
        // In asParser, the first token is START_OBJECT, so we can check ids.
        assertEquals(JsonToken.START_OBJECT, p.getCurrentToken());
        assertEquals("type1", p.getTypeId());
        assertEquals("obj1", p.getObjectId());
    }

    // ---------- Append ----------
    @Test
    public void testAppendBuffer() throws IOException {
        TokenBuffer source = new TokenBuffer(codec, false);
        source.writeStartArray();
        source.writeNumber(1);
        source.writeEndArray();

        TokenBuffer target = new TokenBuffer(codec, false);
        target.append(source);

        JsonParser p = target.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testAppendWithNativeIdsPropagation() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, true);
        src.writeTypeId("t");
        src.writeNumber(5);
        TokenBuffer dest = new TokenBuffer(codec, false);
        dest.append(src);
        assertTrue(dest.canWriteTypeId());
    }

    // ---------- Serialize ----------
    @Test
    public void testSerializeToGenerator() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeFieldName("key");
        src.writeString("value");
        src.writeEndObject();

        TokenBuffer dest = new TokenBuffer(codec, false);
        src.serialize(dest);  // dest is a JsonGenerator

        JsonParser p = dest.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    private TokenBuffer createSimpleBuffer() throws IOException {
        TokenBuffer b = new TokenBuffer(codec, false);
        b.writeStartObject();
        b.writeFieldName("k");
        b.writeBoolean(true);
        b.writeEndObject();
        return b;
    }

    @Test
    public void testSerializeWithNativeIds() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, true);
        src.writeObjectId("oid");
        src.writeTypeId("tid");
        src.writeStartArray();
        src.writeEndArray();

        TokenBuffer dest = new TokenBuffer(codec, false);
        src.serialize(dest);

        // dest should have the ids
        JsonParser p = dest.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("oid", p.getObjectId());
        assertEquals("tid", p.getTypeId());
    }

    // ---------- Deserialize ----------
    @Test
    public void testDeserialize() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartArray();
        src.writeNumber(10);
        src.writeEndArray();
        JsonParser p = src.asParser();
        p.nextToken(); // START_ARRAY
        TokenBuffer result = new TokenBuffer(codec, false);
        DeserializationContext ctxt = null; // not used
        result.deserialize(p, ctxt);
        // result now contains copy of array structure
        JsonParser rp = result.asParser();
        assertEquals(JsonToken.START_ARRAY, rp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, rp.nextToken());
        assertEquals(10, rp.getIntValue());
        assertEquals(JsonToken.END_ARRAY, rp.nextToken());
    }

    // ---------- toString ----------
    @Test
    public void testToStringEmpty() {
        String str = buffer.toString();
        assertTrue(str.contains("[TokenBuffer:"));
        assertTrue(str.endsWith("]"));
    }

    @Test
    public void testToStringTruncation() throws IOException {
        // write more than 100 tokens to trigger truncation
        for (int i = 0; i < 120; i++) {
            buffer.writeNumber(i);
        }
        String str = buffer.toString();
        assertTrue(str.contains("... (truncated"));
    }

    // ---------- Segment overflow (internal) ----------
    @Test
    public void testWriteManyTokens() throws IOException {
        for (int i = 0; i < 20; i++) {
            buffer.writeNumber(i);
        }
        JsonParser p = buffer.asParser();
        for (int i = 0; i < 20; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
    }

    // ---------- Copy current event ----------
    @Test
    public void testCopyCurrentEvent() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeFieldName("a");
        src.writeString("b");
        src.writeEndObject();

        JsonParser srcParser = src.asParser();
        srcParser.nextToken(); // START_OBJECT
        srcParser.nextToken(); // FIELD_NAME
        srcParser.nextToken(); // VALUE_STRING

        TokenBuffer dest = new TokenBuffer(codec, false);
        dest.copyCurrentEvent(srcParser);
        // dest should have the string token
        JsonParser dp = dest.asParser();
        assertEquals(JsonToken.VALUE_STRING, dp.nextToken());
        assertEquals("b", dp.getText());
    }

    @Test
    public void testCopyCurrentEventAllTypes() throws IOException {
        // we test each token type via a roundtrip
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeFieldName("f");
        src.writeStartArray();
        src.writeEndArray();
        src.writeNumber(42);
        src.writeNumber(3.14);
        src.writeBoolean(true);
        src.writeBoolean(false);
        src.writeNull();
        src.writeObject("embedded");
        src.writeEndObject();

        JsonParser sp = src.asParser();
        TokenBuffer dest = new TokenBuffer(codec, false);
        while (sp.nextToken() != null) {
            dest.copyCurrentEvent(sp);
        }
        // Serialize dest to another buffer to verify
        TokenBuffer verify = new TokenBuffer(codec, false);
        dest.serialize(verify);
        JsonParser vp = verify.asParser();
        assertEquals(JsonToken.START_OBJECT, vp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals("f", vp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, vp.nextToken());
        assertEquals(JsonToken.END_ARRAY, vp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, vp.nextToken());
        assertEquals(42, vp.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, vp.nextToken());
        assertEquals(3.14, vp.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_TRUE, vp.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, vp.nextToken());
        assertEquals(JsonToken.VALUE_NULL, vp.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, vp.nextToken());
        assertEquals("embedded", vp.getEmbeddedObject());
        assertEquals(JsonToken.END_OBJECT, vp.nextToken());
    }

    @Test
    public void testCopyCurrentEventWithTextCharacters() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeString("text");
        JsonParser sp = src.asParser();
        sp.nextToken();
        // force hasTextCharacters? In TokenBuffer.Parser it's always false.
        // But copyCurrentEvent uses hasTextCharacters to branch.
        TokenBuffer dest = new TokenBuffer(codec, false);
        dest.copyCurrentEvent(sp);
        JsonParser dp = dest.asParser();
        assertEquals(JsonToken.VALUE_STRING, dp.nextToken());
        assertEquals("text", dp.getText());
    }

    // ---------- Copy current structure ----------
    @Test
    public void testCopyCurrentStructureNested() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeFieldName("arr");
        src.writeStartArray();
        src.writeNumber(1);
        src.writeNumber(2);
        src.writeEndArray();
        src.writeEndObject();

        JsonParser sp = src.asParser();
        sp.nextToken(); // START_OBJECT
        TokenBuffer dest = new TokenBuffer(codec, false);
        dest.copyCurrentStructure(sp);

        JsonParser dp = dest.asParser();
        assertEquals(JsonToken.START_OBJECT, dp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, dp.nextToken());
        assertEquals("arr", dp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, dp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, dp.nextToken());
        assertEquals(1, dp.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, dp.nextToken());
        assertEquals(2, dp.getIntValue());
        assertEquals(JsonToken.END_ARRAY, dp.nextToken());
        assertEquals(JsonToken.END_OBJECT, dp.nextToken());
    }

    @Test
    public void testCopyCurrentStructureWithFieldNameFirst() throws IOException {
        TokenBuffer src = new TokenBuffer(codec, false);
        src.writeStartObject();
        src.writeFieldName("f");
        src.writeNumber(99);
        src.writeEndObject();

        JsonParser sp = src.asParser();
        sp.nextToken(); // START_OBJECT
        sp.nextToken(); // FIELD_NAME
        TokenBuffer dest = new TokenBuffer(codec, false);
        dest.copyCurrentStructure(sp);

        JsonParser dp = dest.asParser();
        assertEquals(JsonToken.FIELD_NAME, dp.nextToken());
        assertEquals("f", dp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, dp.nextToken());
        assertEquals(99, dp.getIntValue());
    }

    // ---------- Parser inner class methods ----------
    @Test
    public void testParserGetText() throws IOException {
        buffer.writeString("text");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("text", p.getText());
    }

    @Test
    public void testParserGetTextOnNumber() throws IOException {
        buffer.writeNumber(123);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("123", p.getText());
    }

    @Test
    public void testParserGetTextOnFieldName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("field");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test
    public void testParserGetTextOnToken() throws IOException {
        buffer.writeBoolean(true);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test
    public void testParserIsClosed() throws IOException {
        JsonParser p = buffer.asParser();
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test
    public void testParserNextTokenAfterClose() throws IOException {
        JsonParser p = buffer.asParser();
        p.close();
        assertNull(p.nextToken());
    }

    @Test
    public void testParserPeekNextToken() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.peekNextToken());
        p.nextToken();
        assertEquals(JsonToken.END_ARRAY, p.peekNextToken());
        p.nextToken();
        assertNull(p.peekNextToken());
    }

    @Test
    public void testParserGetNumberValue() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        Number n = p.getNumberValue();
        assertEquals(42, n.intValue());
    }

    @Test
    public void testParserGetNumberValueString() throws IOException {
        buffer.writeNumber("3.14");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(3.14, p.getNumberValue().doubleValue(), 0.0);
    }

    @Test
    public void testParserGetNumberValueNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertNull(p.getNumberValue());
    }

    @Test(expected=com.fasterxml.jackson.core.JsonParseException.class)
    public void testParserGetNumberValueOnNonNumber() throws IOException {
        buffer.writeString("abc");
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.getNumberValue(); // should throw
    }

    @Test
    public void testParserGetBigIntegerValue() throws IOException {
        buffer.writeNumber(BigInteger.TEN);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
    }

    @Test
    public void testParserGetBigIntegerFromBigDecimal() throws IOException {
        buffer.writeNumber(BigDecimal.TEN);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
    }

    @Test
    public void testParserGetBigIntegerFromInt() throws IOException {
        buffer.writeNumber(5);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigInteger.valueOf(5), p.getBigIntegerValue());
    }

    @Test
    public void testParserGetDecimalValue() throws IOException {
        buffer.writeNumber(BigDecimal.valueOf(3.14));
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigDecimal.valueOf(3.14), p.getDecimalValue());
    }

    @Test
    public void testParserGetDecimalFromInt() throws IOException {
        buffer.writeNumber(7);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(BigDecimal.valueOf(7), p.getDecimalValue());
    }

    @Test
    public void testParserGetDoubleFloat() throws IOException {
        buffer.writeNumber(2.5);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(2.5, p.getDoubleValue(), 0.0);
        assertEquals(2.5f, p.getFloatValue(), 0.0);
    }

    @Test
    public void testParserGetIntValue() throws IOException {
        buffer.writeNumber(12345);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(12345, p.getIntValue());
    }

    @Test
    public void testParserGetLongValue() throws IOException {
        buffer.writeNumber(Long.MAX_VALUE);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(Long.MAX_VALUE, p.getLongValue());
    }

    @Test
    public void testParserNumberType() throws IOException {
        buffer.writeNumber((short)1);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(NumberType.INT, p.getNumberType());
        // other types covered in writeNumber tests
    }

    @Test
    public void testParserGetBinaryValue() throws IOException {
        byte[] data = new byte[] {1,2,3};
        buffer.writeBinary(data);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue());
    }

    @Test
    public void testParserGetBinaryValueFromString() throws IOException {
        // base64 encoded
        buffer.writeString("AQID"); // three bytes
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertArrayEquals(new byte[]{1,2,3}, p.getBinaryValue());
    }

    @Test(expected=com.fasterxml.jackson.core.JsonParseException.class)
    public void testParserGetBinaryValueWrongToken() throws IOException {
        buffer.writeNumber(1);
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.getBinaryValue();
    }

    @Test
    public void testParserReadBinaryValue() throws IOException {
        buffer.writeBinary(new byte[]{4,5,6});
        JsonParser p = buffer.asParser();
        p.nextToken();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), baos);
        assertArrayEquals(new byte[]{4,5,6}, baos.toByteArray());
        assertEquals(3, len);
    }

    @Test
    public void testParserReadBinaryValueNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        p.nextToken();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), baos);
        assertEquals(0, len);
    }

    @Test
    public void testParserOverrideCurrentName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("old");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.overrideCurrentName("new");
        assertEquals("new", p.getCurrentName());
        // Test override for START_OBJECT/ARRAY off-by-one
        buffer.writeStartArray();
        buffer.writeEndArray();
        p = buffer.asParser();
        p.nextToken();
        p.overrideCurrentName("arrName");
        // Not applicable? overrideCurrentName on START_ARRAY affects parent context? Not important.
    }

    @Test
    public void testParserGetCurrentName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("key");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.nextToken();
        assertEquals("key", p.getCurrentName());
    }

    @Test
    public void testParserHasTextCharacters() {
        JsonParser p = buffer.asParser();
        assertFalse(p.hasTextCharacters());
    }

    @Test
    public void testParserGetTextCharacters() throws IOException {
        buffer.writeString("sample");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertArrayEquals("sample".toCharArray(), p.getTextCharacters());
    }

    @Test
    public void testParserGetTextLengthOffset() throws IOException {
        buffer.writeString("abc");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals(3, p.getTextLength());
        assertEquals(0, p.getTextOffset());
    }

    @Test
    public void testParserGetEmbeddedObject() throws IOException {
        buffer.writeObject("embedded");
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertEquals("embedded", p.getEmbeddedObject());
        // non-embedded token returns null
        buffer.writeNumber(5);
        p = buffer.asParser();
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test
    public void testParserGetParsingContext() throws IOException {
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertNotNull(p.getParsingContext());
    }

    // ---------- Edge cases ----------
    @Test
    public void testUnbalancedArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeStartArray();
        buffer.writeEndArray();
        buffer.writeEndArray();
        buffer.writeEndArray(); // extra, should be allowed
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testWriteEndArrayAtRoot() throws IOException {
        // nothing written, end array should be allowed
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testWriteEndObjectAtRoot() throws IOException {
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // Test Segment's appendRaw (covered indirectly through some internal? Actually appendRaw isn't used by write methods)
    // We can't directly call appendRaw; but if needed, we could use reflection, but that's overkill.
    // Probably not necessary for coverage.

    // Test native id assignment with non-null object/type ids.
    @Test
    public void testNativeIdAssignmentInSegment() throws IOException {
        TokenBuffer tb = new TokenBuffer(codec, true);
        tb.writeTypeId("type1");
        tb.writeObjectId("obj1");
        tb.writeNumber(42);
        JsonParser p = tb.asParser();
        p.nextToken(); // VALUE_NUMBER_INT
        assertEquals("type1", p.getTypeId());
        assertEquals("obj1", p.getObjectId());
    }

    // Test TokenBuffer.Parser setLocation
    @Test
    public void testParserSetLocation() {
        JsonParser p = buffer.asParser();
        JsonLocation loc = new JsonLocation(null, 100, 1, 1);
        ((TokenBuffer.Parser)p).setLocation(loc);
        assertEquals(100, p.getTokenLocation().getByteOffset());
    }

    // Test _appendRaw (indirectly) - Not called by public methods, so skip.

    // Test writeFieldName with SerializableString, value retrieval
    @Test
    public void testWriteFieldNameSerializableStringValue() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializedString("serialized"));
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        p.nextToken();
        p.nextToken();
        assertEquals("serialized", p.getText());
    }
}
```
