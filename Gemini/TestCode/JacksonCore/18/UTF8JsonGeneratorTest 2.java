package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.BufferRecycler;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class UTF8JsonGeneratorTest {

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, features, null, out);
    }

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out) {
        return createGenerator(out, 0);
    }

    private UTF8JsonGenerator createGeneratorWithBuffer(ByteArrayOutputStream out, byte[] buf, int offset, boolean recyclable) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, 0, null, out, buf, offset, recyclable);
    }

    @Test
    public void testBasicObjectAndArrayWriting() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        Assert.assertSame(out, gen.getOutputTarget());
        Assert.assertEquals(0, gen.getOutputBuffered());

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(123);
        gen.writeFieldName(new SerializedString("b"));
        gen.writeBoolean(true);
        gen.writeFieldName("c");
        gen.writeBoolean(false);
        gen.writeFieldName("d");
        gen.writeNull();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeString("str");
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertEquals("{\"a\":123,\"b\":true,\"c\":false,\"d\":null,\"arr\":[\"str\"]}", json);
    }

    @Test
    public void testNumbers() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber((short) -12);
        gen.writeNumber(12345);
        gen.writeNumber(-9876543210L);
        gen.writeNumber(new BigInteger("12345678901234567890"));
        gen.writeNumber((BigInteger) null);
        gen.writeNumber(123.456d);
        gen.writeNumber(78.9f);
        gen.writeNumber(new BigDecimal("9999.9999"));
        gen.writeNumber((BigDecimal) null);
        gen.writeNumber("42");
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.startsWith("[-12,12345,-9876543210,12345678901234567890,null,123.456,78.9,9999.9999,null,42]"));
    }

    @Test
    public void testNumbersAsStrings() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeNumber((short) 5);
        gen.writeNumber(100);
        gen.writeNumber(20000000000L);
        gen.writeNumber(new BigInteger("12345"));
        gen.writeNumber(new BigDecimal("1E+2"));
        gen.writeNumber("99");
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertEquals("[\"5\",\"100\",\"20000000000\",\"12345\",\"100\",\"99\"]", json);
    }

    @Test
    public void testSpecialFloatingPointValues() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Double.NEGATIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.POSITIVE_INFINITY);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertEquals("[\"NaN\",\"Infinity\",\"-Infinity\",\"NaN\",\"Infinity\",\"-Infinity\"]", json);
    }

    @Test
    public void testUnquotedFieldNames() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);

        gen.writeStartObject();
        gen.writeFieldName("unquotedKey");
        gen.writeString("val1");
        gen.writeFieldName(new SerializedString("key2"));
        gen.writeString("val2");
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertEquals("{unquotedKey:\"val1\",key2:\"val2\"}", json);
    }

    @Test
    public void testPrettyPrinterOutput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        gen.setPrettyPrinter(pp);

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeString("b");
        gen.writeFieldName(new SerializedString("c"));
        gen.writeNumber(1);
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(2);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.contains("{\n"));
        Assert.assertTrue(json.contains("\"a\" : \"b\""));
    }

    @Test
    public void testPrettyPrinterUnquotedFieldNames() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("foo");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("bar"));
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.contains("foo : 1"));
        Assert.assertTrue(json.contains("bar : 2"));
    }

    @Test
    public void testVariousStringWrites() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeString((String) null);
        gen.writeString("hello \"world\" \n \t \r \b \f \\ \u0000 \u001F");
        char[] chars = "abcdef".toCharArray();
        gen.writeString(chars, 1, 3); // "bcd"
        gen.writeString(new SerializedString("serializable"));
        byte[] utf8Bytes = "utf8text".getBytes("UTF-8");
        gen.writeRawUTF8String(utf8Bytes, 0, utf8Bytes.length);
        gen.writeUTF8String(utf8Bytes, 0, utf8Bytes.length);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.contains("null"));
        Assert.assertTrue(json.contains("\"bcd\""));
        Assert.assertTrue(json.contains("\"serializable\""));
        Assert.assertTrue(json.contains("\"utf8text\""));
    }

    @Test
    public void testRawWrites() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeRaw("123");
        gen.writeRaw("456789", 1, 3);
        gen.writeRaw(new SerializedString(",999"));
        gen.writeRawValue(new SerializedString(",1000"));
        gen.writeRaw(',');
        gen.writeRaw('A');
        gen.writeRaw('\u00E9'); // 2-byte UTF-8
        gen.writeRaw('\u4E16'); // 3-byte UTF-8
        char[] cbuf = ",true".toCharArray();
        gen.writeRaw(cbuf, 0, cbuf.length);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.startsWith("[123567,999,1000,A\u00E9\u4E16,true]"));
    }

    @Test
    public void testSurrogatesAndEscapes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeString("Text with \u00E9 and \u4E16 and high ascii \u0085");
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.contains("\\u00e9"));
        Assert.assertTrue(json.contains("\\u4e16"));
    }

    @Test
    public void testCustomEscapes() throws IOException {
        CharacterEscapes custom = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = CharacterEscapes.standardAsciiEscapesForJSON();
                ascii['a'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("[custom-a]");
                }
                return null;
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setCharacterEscapes(custom);

        gen.writeStartArray();
        gen.writeString("abc");
        char[] chars = "bac".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.contains("[custom-a]bc"));
        Assert.assertTrue(json.contains("b[custom-a]c"));
    }

    @Test
    public void testCustomEscapesMissingDefinitionThrowsException() throws IOException {
        CharacterEscapes custom = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = CharacterEscapes.standardAsciiEscapesForJSON();
                ascii['x'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null; // missing definition
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setCharacterEscapes(custom);

        try {
            gen.writeString("x");
            Assert.fail("Expected JsonGenerationException for missing custom escape");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid custom escape definitions"));
        }
    }

    @Test
    public void testBinaryWrites() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);
        InputStream in = new ByteArrayInputStream(data);
        int written = gen.writeBinary(Base64Variants.MIME, in, data.length);
        Assert.assertEquals(data.length, written);

        InputStream inUnknown = new ByteArrayInputStream(data);
        int writtenUnknown = gen.writeBinary(Base64Variants.MIME, inUnknown, -1);
        Assert.assertEquals(data.length, writtenUnknown);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.startsWith("[\""));
        Assert.assertTrue(json.endsWith("\"]"));
    }

    @Test(expected = JsonGenerationException.class)
    public void testBinaryTooFewBytesThrowsException() throws IOException {
        byte[] data = new byte[] { 1, 2 };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        InputStream in = new ByteArrayInputStream(data);
        gen.writeBinary(Base64Variants.MIME, in, 10);
    }

    @Test
    public void testBufferFlushingAndAutoClose() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask()
                | JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask()
                | JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.flush();
        // Closing should auto-close array and object
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertEquals("{\"arr\":[1]}", json);
    }

    @Test(expected = JsonGenerationException.class)
    public void testInvalidContextEndObject() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.writeStartArray();
        gen.writeEndObject(); // Error: expected EndArray
    }

    @Test(expected = JsonGenerationException.class)
    public void testInvalidContextEndArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.writeStartObject();
        gen.writeEndArray(); // Error: expected EndObject
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameInArrayThrowsException() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.writeStartArray();
        gen.writeFieldName("a");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteValueExpectingFieldNameThrowsException() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.writeStartObject();
        gen.writeNumber(123); // Expecting field name
    }

    @Test
    public void testLongStringsForcingBufferFlushes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[32]; // Small buffer to force segmented processing
        UTF8JsonGenerator gen = createGeneratorWithBuffer(out, buf, 0, false);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("long_string_segment_");
        }
        String longStr = sb.toString();

        gen.writeStartArray();
        gen.writeString(longStr);
        gen.writeRaw(longStr);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        Assert.assertTrue(json.startsWith("[\"" + longStr + "\""));
    }

    @Test
    public void testSurrogatePairsInRaw() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        // Emoji character (U+1F600: High surrogate \uD83D, Low surrogate \uDE00)
        char[] surrs = new char[] { '\uD83D', '\uDE00' };
        gen.writeRaw(surrs, 0, 2);
        gen.writeEndArray();
        gen.close();

        byte[] bytes = out.toByteArray();
        Assert.assertTrue(bytes.length > 2);
    }

    @Test(expected = JsonGenerationException.class)
    public void testSplitSurrogateThrowsException() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.writeRaw('\uD83D'); // Lone high surrogate
    }
}
