package com.fasterxml.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class WriterBasedJsonGeneratorTest {

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), "test", false);
    }

    private WriterBasedJsonGenerator createGenerator(Writer w, int features) {
        return new WriterBasedJsonGenerator(createIOContext(), features, null, w);
    }

    private WriterBasedJsonGenerator createGenerator(Writer w) {
        return createGenerator(w, 0);
    }

    @Test
    public void testGetOutputTargetAndBuffered() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        Assert.assertSame(sw, gen.getOutputTarget());
        Assert.assertEquals(0, gen.getOutputBuffered());

        gen.writeRaw("123");
        Assert.assertEquals(3, gen.getOutputBuffered());
        gen.flush();
        Assert.assertEquals(0, gen.getOutputBuffered());
        gen.close();
    }

    @Test
    public void testBasicJsonStructure() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("value");
        gen.writeFieldName(new SerializedString("count"));
        gen.writeNumber(42);
        gen.writeFieldName("items");
        gen.writeStartArray();
        gen.writeBoolean(true);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{\"name\":\"value\",\"count\":42,\"items\":[true,false,null]}", sw.toString());
    }

    @Test
    public void testUnquotedFieldNames() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask());
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("unquoted");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("unquoted2"));
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{unquoted:1,unquoted2:2}", sw.toString());
    }

    @Test
    public void testPrettyPrinterOutput() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("b"));
        gen.writeStartArray();
        gen.writeString("test");
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String result = sw.toString();
        Assert.assertTrue(result.contains("\"a\" : 1"));
        Assert.assertTrue(result.contains("\"b\" : ["));
    }

    @Test
    public void testPrettyPrinterUnquotedFieldNames() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("b"));
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        String result = sw.toString();
        Assert.assertTrue(result.contains("a : 1"));
        Assert.assertTrue(result.contains("b : 2"));
    }

    @Test(expected = JsonGenerationException.class)
    public void testInvalidWriteEndArray() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeStartObject();
        gen.writeEndArray();
    }

    @Test(expected = JsonGenerationException.class)
    public void testInvalidWriteEndObject() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeStartArray();
        gen.writeEndObject();
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameInRoot() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeFieldName("field");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteSerializableFieldNameInRoot() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeFieldName(new SerializedString("field"));
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteValueWhenExpectingFieldName() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeStartObject();
        gen.writeString("valueWithoutField");
    }

    @Test
    public void testStringVariations() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeString((String) null);
        gen.writeString("simple");
        char[] chars = "charArray".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeString(new SerializedString("serializable"));
        gen.writeString(new SerializedString("this is a longer serializable string that exceeds short write threshold"));
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[null,\"simple\",\"charArray\",\"serializable\",\"this is a longer serializable string that exceeds short write threshold\"]", sw.toString());
    }

    @Test
    public void testRawWrites() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeRaw("raw1");
        gen.writeRaw("raw2", 0, 4);
        gen.writeRaw(new SerializedString("raw3"));
        char[] rawChars = "raw4".toCharArray();
        gen.writeRaw(rawChars, 0, rawChars.length);
        char[] longRawChars = new char[50];
        Arrays.fill(longRawChars, 'x');
        gen.writeRaw(longRawChars, 0, longRawChars.length);
        gen.writeRaw('!');
        gen.close();

        String expected = "raw1raw2raw3raw4" + new String(longRawChars) + "!";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testRawLongString() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("abcdefghij");
        }
        String big = sb.toString();
        gen.writeRaw(big);
        gen.close();

        Assert.assertEquals(big, sw.toString());
    }

    @Test
    public void testNumbers() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeNumber((short) 12);
        gen.writeNumber(12345);
        gen.writeNumber(1234567890123L);
        gen.writeNumber(new BigInteger("98765432109876543210"));
        gen.writeNumber((BigInteger) null);
        gen.writeNumber(3.14159);
        gen.writeNumber(2.71828f);
        gen.writeNumber(new BigDecimal("1234.5678"));
        gen.writeNumber((BigDecimal) null);
        gen.writeNumber("9999");
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[12,12345,1234567890123,98765432109876543210,null,3.14159,2.71828,1234.5678,null,9999]", sw.toString());
    }

    @Test
    public void testNumbersAsStrings() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);

        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(2);
        gen.writeNumber(3L);
        gen.writeNumber(new BigInteger("4"));
        gen.writeNumber(5.5);
        gen.writeNumber(6.5f);
        gen.writeNumber(new BigDecimal("7.5"));
        gen.writeNumber("8.5");
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"1\",\"2\",\"3\",\"4\",\"5.5\",\"6.5\",\"7.5\",\"8.5\"]", sw.toString());
    }

    @Test
    public void testNonNumericNumbersAndBigDecimalPlain() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeNumber(new BigDecimal("1E+2"));
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"NaN\",\"Infinity\",\"NaN\",\"-Infinity\",100]", sw.toString());
    }

    @Test
    public void testCharacterEscapesStandard() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeString("Hello\nWorld\t\"Escaped\"\0\u001F");
        gen.close();

        Assert.assertEquals("\"Hello\\nWorld\\t\\\"Escaped\\\"\\u0000\\u001f\"", sw.toString());
    }

    @Test
    public void testCharacterEscapesAscii() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setHighestNonEscapedChar(127);

        gen.writeStartArray();
        gen.writeString("ASCII \u00C5 \u4E16\u754C");
        char[] chars = "\u00E9\u00E8\u00EA".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"ASCII \\u00c5 \\u4e16\\u754c\",\"\\u00e9\\u00e8\\u00ea\"]", sw.toString());
    }

    @Test
    public void testCustomCharacterEscapes() throws Exception {
        CharacterEscapes customEscapes = new CharacterEscapes() {
            private final int[] ascii = CharacterEscapes.standardAsciiEscapesForJSON();
            @Override
            public int[] getEscapeCodesForAscii() {
                return ascii;
            }
            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'x') {
                    return new SerializedString("[X]");
                }
                return null;
            }
        };

        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setCharacterEscapes(customEscapes);

        gen.writeStartArray();
        gen.writeString("text with x in it");
        char[] chars = "extra x test".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"text with [X] in it\",\"e[X]tra [X] test\"]", sw.toString());
    }

    @Test
    public void testLongEscapedString() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("A\"B\nC\u0001D");
        }
        gen.writeString(sb.toString());
        gen.close();

        String result = sw.toString();
        Assert.assertTrue(result.startsWith("\"A\\\"B\\nC\\u0001D"));
        Assert.assertTrue(result.endsWith("\""));
    }

    @Test
    public void testBinaryByteArray() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] data = "Hello World Jackson Base64".getBytes("UTF-8");
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);
        gen.close();

        Assert.assertEquals("\"SGVsbG8gV29ybGQgSmFja3NvbiBCYXNlNjQ=\"", sw.toString());
    }

    @Test
    public void testBinaryInputStreamKnownLength() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] data = "Streaming Binary Test Data 123456789".getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        int written = gen.writeBinary(Base64Variants.MIME, bais, data.length);
        gen.close();

        Assert.assertEquals(data.length, written);
        Assert.assertEquals("\"U3RyZWFtaW5nIEJpbmFyeSBUZXN0IERhdGEgMTIzNDU2Nzg5\"", sw.toString());
    }

    @Test
    public void testBinaryInputStreamUnknownLength() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] data = "Unknown length binary data test stream chunk".getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        int written = gen.writeBinary(Base64Variants.MIME, bais, -1);
        gen.close();

        Assert.assertEquals(data.length, written);
        Assert.assertTrue(sw.toString().startsWith("\""));
        Assert.assertTrue(sw.toString().endsWith("\""));
    }

    @Test(expected = JsonGenerationException.class)
    public void testBinaryInputStreamTooFewBytes() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] data = new byte[]{1, 2};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        gen.writeBinary(Base64Variants.MIME, bais, 10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeRawUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test
    public void testAutoCloseJsonContent() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask());
        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.close();

        Assert.assertEquals("{\"arr\":[1]}", sw.toString());
    }

    @Test
    public void testAutoCloseTarget() throws Exception {
        final boolean[] closed = new boolean[]{false};
        Writer customWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {}
            @Override
            public void flush() {}
            @Override
            public void close() {
                closed[0] = true;
            }
        };

        WriterBasedJsonGenerator gen = createGenerator(customWriter, JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask());
        gen.writeNumber(123);
        gen.close();

        Assert.assertTrue(closed[0]);
    }

    @Test
    public void testFlushPassedToStream() throws Exception {
        final boolean[] flushed = new boolean[]{false};
        Writer customWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {}
            @Override
            public void flush() {
                flushed[0] = true;
            }
            @Override
            public void close() {}
        };

        WriterBasedJsonGenerator gen = createGenerator(customWriter, JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.getMask());
        gen.writeNumber(123);
        gen.flush();

        Assert.assertTrue(flushed[0]);
        gen.close();
    }

    @Test
    public void testRootValueSeparator() throws Exception {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setRootValueSeparator(new SerializedString(" "));

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeNumber(3);
        gen.close();

        Assert.assertEquals("1 2 3", sw.toString());
    }
}
