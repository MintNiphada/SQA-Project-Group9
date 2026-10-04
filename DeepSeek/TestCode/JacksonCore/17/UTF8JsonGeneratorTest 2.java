package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class UTF8JsonGeneratorTest {
    private static final byte[] NULL_BYTES = "null".getBytes();
    private static final byte[] TRUE_BYTES = "true".getBytes();
    private static final byte[] FALSE_BYTES = "false".getBytes();
    private static final byte[] EMPTY_ARRAY = "[]".getBytes();
    private static final byte[] EMPTY_OBJECT = "{}".getBytes();

    private ByteArrayOutputStream out;
    private IOContext ctxt;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
        BufferRecycler recycler = new BufferRecycler();
        ctxt = new IOContext(recycler, "test", false);
    }

    /* Helper methods */

    private UTF8JsonGenerator createGenerator(int features, int outputBufferSize) {
        byte[] buf = new byte[outputBufferSize];
        return new UTF8JsonGenerator(ctxt, features, null, out, buf, 0, false);
    }

    private UTF8JsonGenerator createGenerator(int features) {
        // default buffer size big enough to avoid flush in many cases
        return new UTF8JsonGenerator(ctxt, features, null, out);
    }

    private byte[] getOutputBytes() {
        return out.toByteArray();
    }

    private String getOutputString() {
        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private void assertOutputEquals(byte[] expected) {
        assertArrayEquals(expected, out.toByteArray());
    }

    private void assertOutputEquals(String expected) {
        assertEquals(expected, getOutputString());
    }

    @Test
    public void testWriteStartArray() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.close();
        assertOutputEquals(EMPTY_ARRAY);
    }

    @Test
    public void testWriteEndArrayNoContext() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        assertOutputEquals(EMPTY_ARRAY);
    }

    @Test
    public void testWriteEndArrayNotInArray() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        try {
            gen.writeEndArray();
            fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            // expected
        }
    }

    @Test
    public void testWriteStartObject() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.close();
        assertOutputEquals(EMPTY_OBJECT);
    }

    @Test
    public void testWriteEndObject() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        assertOutputEquals(EMPTY_OBJECT);
    }

    @Test
    public void testWriteEndObjectNotInObject() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        try {
            gen.writeEndObject();
            fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            // expected
        }
    }

    @Test
    public void testWriteBooleanTrue() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeBoolean(true);
        gen.close();
        assertOutputEquals(TRUE_BYTES);
    }

    @Test
    public void testWriteBooleanFalse() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeBoolean(false);
        gen.close();
        assertOutputEquals(FALSE_BYTES);
    }

    @Test
    public void testWriteNull() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeNull();
        gen.close();
        assertOutputEquals(NULL_BYTES);
    }

    @Test
    public void testWriteStringNull() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString((String) null);
        gen.close();
        assertOutputEquals(NULL_BYTES);
    }

    @Test
    public void testWriteStringEmpty() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString("");
        gen.close();
        assertOutputEquals("\"\"" .getBytes()));
    }

    @Test
    public void testWriteStringSimple() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString("hello");
        gen.close();
        assertOutputEquals("\"hello\"".getBytes());
    }

    @Test
    public void testWriteStringWithEscapes() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString("hello\nworld");
        gen.close();
        assertOutputEquals("\"hello\\nworld\"".getBytes());
    }

    @Test
    public void testWriteStringNonAscii() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString("\u00e9"); // é
        gen.close();
        assertOutputEquals("\"\u00e9\"".getBytes());
    }

    @Test
    public void testWriteStringLongSegmented() throws Exception {
        // use small buffer to force segmentation
        UTF8JsonGenerator gen = createGenerator(0, 10);
        StringBuilder sb = new StringBuilder(100);
        for (int i=0; i<100; i++) sb.append('a');
        gen.writeString(sb.toString());
        gen.close();
        assertEquals('"' + sb.toString() + '"', new String(getOutputBytes()));
    }

    @Test
    public void testWriteStringCharArray() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeString(new char[]{'f','o','o'}, 0, 3);
        gen.close();
        assertOutputEquals("\"foo\"".getBytes());
    }

    @Test
    public void testWriteStringSerializableString() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        SerializableString s = SerializableStringImpl.forValue("bar");
        gen.writeString(s);
        gen.close();
        assertOutputEquals("\"bar\"".getBytes());
    }

    @Test
    public void testWriteFieldNameSimple() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeFieldName("field");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        assertOutputEquals("{\"field\":\"value\"}".getBytes());
    }

    @Test
    public void testWriteFieldNameUnquoted() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        gen.writeStartObject();
        gen.writeFieldName("field");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        assertOutputEquals("{field:\"value\"}".getBytes());
    }

    @Test
    public void testWriteFieldNameSerializable() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        SerializableString name = SerializableStringImpl.forValue("field");
        gen.writeFieldName(name);
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        assertOutputEquals("{\"field\":\"value\"}".getBytes());
    }

    @Test
    public void testWriteFieldNameWithPrettyPrinter() throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());
        gen.writeStartObject();
        gen.writeFieldName("field");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        String result = getOutputString();
        assertTrue(result.contains("\"field\" : \"value\""));
    }

    @Test
    public void testWriteNumbersAsStrings( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        gen.writeNumber(123);
        gen.close();
        assertOutputEquals("\"123\"".getBytes());
    }

    @Test
    public void testWriteLongNumber ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber(Long.MAX_VALUE);
        gen.close();
        assertEquals(Long.toString(Long.MAX_VALUE), getOutputString());
    }

    @Test
    public void testWriteBigInteger ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber(BigInteger.TEN);
        gen.close();
        assertEquals("10", getOutputString());
    }

    @Test
    public void testWriteBigDecimal ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber(BigDecimal.valueOf(1234.5678));
        gen.close();
        assertEquals("1234.5678", getOutputString());
    }

    @Test
    public void testWriteFloatNaNQuoting ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        gen.writeNumber(Float.NaN);
        gen.close();
        assertEquals("\"NaN\"", getOutputString());
    }

    @Test
    public void testWriteDoubleInfinityQuoting ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.close();
        assertEquals("\"Infinit\"", getOutputString());
    }

    @Test
    public void testWriteRawString ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeRaw("12345");
        gen.close();
        assertEquals("12345", getOutputString());
    }

    @Test
    public void testWriteRawChar ( ) throws Exception {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeRaw('A');
        gen.close();
        assertEquals("A", getOutputString());
    }

    @Test
    public void testWriteRawCharArraySegment ( ) throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeRaw(new char[]{'x','y','z'}, 1, 2);
        gen.close();
        assertEquals("yz", getOutputString());
    }

    @Test
    public void testWriteRawSegmented ( ) throws Exception {
        TF8JsonGenerator gen = createGenerator(0, 5); // small buffer to force segmented raw
        char[] buf = new char[20];
        for (int i=0;i<20;i++) buf[i] = 'a';
        gen.writeRaw(buf, 0, 20);
        gen.close();
        assertEquals("aaaaaaaaaaaaaaaaaaaa", getOutputString());
    }

    @Test
    public void testWriteRawUTF8String ( ) throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeRawUTF8String("hello".getBytes(), 0, 5);
        gen.close();
        assertEquals("\"hello\"", getOutputString());
    }

    @Test
    public void testWriteUTF8StringShort ( ) throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeUTF8String("world".getBytes(), 0, 5);
        gen.close();
        assertEquals("\"world\"", getOutputString());
    }

    @Test
    public void testWriteBinary Byte Array() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        Base64Variant b64 = Base64Variants.MIME_NO_LINEFEEDS;
        byte[] data = new byte[]{0x1,0x2,0x3};
        gen.writeBinary(b64, data, 0, data.length);
        gen.close();
        String expected = "\"" + Base64Variants.MIME_NO_LINEFEEDS.encode(data) + "\"";
        assertEquals(expected, getOutputString());
    }

    @Test
    public void testWriteBinary InputStream() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        Base64Variant b64 = Base64Variants.MIME_NO_LINEFEEDS;
        byte[] data = new byte[]{0x41,0x42,0x43};
        InputStream in = new ByteArrayInputStream(data);
        gen.writeBinary(b64, in, data.length);
        gen.close();
        String expected = "\"" + Base64Variants.MIME_NO_LINEFEEDS.encode(data) + "\"";
        assertEquals(expected, getOutputString());
    }

    @Test
    public void testWriteBinaryLinefeeds() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        Base64Variant b64 = Base64Variants.MIME;
        byte[] data = new byte[100]; // large enough to trigger linefeeds
        gen.writeBinary(b64, data, 0, data.length);
        gen.close();
        String result = getOutputString();
        assertTrue(result.startsWith("\""));
        assertTrue(result.endsWith("\""));
        assertTrue(result.contains("\\n")); // JSON escaped newline
    }

    @Test
    public void testFlush() throws Exception {
        TF8JsonGenerator gen = createGenerator(0, 10);
        gen.writeRaw("abc");
        gen.flush();
        assertEqual("abc", getOutputString());
        gen.close();
    }

    @Test
    public void testCloseWithAutoCloseJsonContent() throws Exception {
        TF8JsonGenerator gen = createGenerator(JsonGenerator.Feature.AUTO_CLOSE_JSON_COTENT.getMask());
        gen.writeStartArray();
        gen.close();
        assertArrayEquals("[]".getBytes(), getOutputBytes());
    }

    @Test
    public void testCloseWithOpenObject() throws Exception {
        TF8JsonGenerator gen = createGenerator(JsonGenerator.Feature.AUTO_CLOSE_JSON_COTENT.getMask());
        gen.writeStartObject();
        gen.close();
        assertArrayEquals("{}".getBytes(), getOutputBytes());
    }

    @Test
    public void testWriteFieldNameExpectingValueError() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        try {
            gen.writeFieldName("x"); // no preceding start object
            fail();
        } catch (JsonGenerationException e) {
            // expected
        }
    }

    @Test
    public void testWriteValueExpectingName() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        try {
            gen.writeNumber(1);
            fnail();
        } catch (JsonGenerationException e) {
            // expected
        }
    }

    @Test
    public void testWriteStringWithCustomEscapes() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        CharacterEscapes custom = new CharacterEscapes() {
            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == '@') return SerializableStringImpl.forValue("$$");
                return null;
            }
            @Override
            public int[] getEscapeCodesForAsii() {
                int[] esc = CharacterEscapes.standardAsiiEscapecsForJSON();
                esc['@'] = CharacterEscapes.ESCAPE_CUSTOM;
                return esc;
            }
        };
        gen.setCharacterEscapes(custom);
        gen.writeString("a@b");
        gen.close();
        assertEquals("\"a\\$$b\"", getOutputString());
    }

    @Test
    public void testWriteRawWithSurrogate() throws Exception {
        TF8JsonGenerator gen = createGenerator(0, 100);
        String surr = "\ud835\udd00"; // surrogate pair for
        gen.writeRaw(surr);
        gen.close();
        assertEquals("\ud835\udd00", getOutputString());
    }

    @Test
    public void testWriteRawCharNonAscii() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeRaw('é');
        gen.close();
        assertEquals("é", getOutputString());
    }

    @Test
    public void testGetOutputTarget() {
        TF8JsonGenerator gen = createGenerator(0);
        assertSame(out, gen.getOutputTarget());
    }

    @Test
    public void testGetOutputBuffered() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeRaw("abc");
        int buffered = gen.getOutputBuffered();
        assertTrue(buffered > 0);
        gen.close();
    }

    @Test
    public void testWriteNumberShort() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber((short) 42);
        gen.close();
        assertEquals("42", getOutputString());
    }

    @Test
    public void testWriteNumberFloat() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber(3.14f);
        gen.close();
        assertEquals("3.14", getOutputString());
    }

    @Test
    public void testWriteNumberStringEncoded() throws Exception {
        TF8JsonGenerator gen = createGenerator(0);
        gen.writeNumber("12345");
        gen.close();
        assertEquals("12345", getOutputString());
    }
}
