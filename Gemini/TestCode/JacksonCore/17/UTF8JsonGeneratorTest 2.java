package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.BufferRecycler;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class UTF8JsonGeneratorTest {

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream bytesOut, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, features, null, bytesOut);
    }

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream bytesOut) {
        return createGenerator(bytesOut, 0);
    }

    @Test
    public void testBasicObjectAndArrayWriting() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("Jackson");
        gen.writeFieldName("age");
        gen.writeNumber(10);
        gen.writeFieldName("items");
        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(2L);
        gen.writeBoolean(true);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertEquals("{\"name\":\"Jackson\",\"age\":10,\"items\":[1,2,true,false,null]}", json);
    }

    @Test
    public void testSerializableStringFieldNameAndValue() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        gen.writeStartObject();
        SerializableString fieldName = new SerializedString("key");
        SerializableString fieldValue = new SerializedString("val");
        gen.writeFieldName(fieldName);
        gen.writeString(fieldValue);
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{\"key\":\"val\"}", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testNumbers() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        gen.writeStartArray();
        gen.writeNumber((short) 123);
        gen.writeNumber(-456);
        gen.writeNumber(123456789012345L);
        gen.writeNumber(3.14159);
        gen.writeNumber(2.71828f);
        gen.writeNumber(new BigInteger("999999999999999999999999999999"));
        gen.writeNumber(new BigDecimal("123.456e2"));
        gen.writeNumber((BigInteger) null);
        gen.writeNumber((BigDecimal) null);
        gen.writeNumber("42");
        gen.writeEndArray();
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.contains("123"));
        Assert.assertTrue(json.contains("-456"));
        Assert.assertTrue(json.contains("123456789012345"));
        Assert.assertTrue(json.contains("3.14159"));
        Assert.assertTrue(json.contains("999999999999999999999999999999"));
        Assert.assertTrue(json.contains("null"));
    }

    @Test
    public void testNumbersAsStrings() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = createGenerator(bytesOut, features);

        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(2);
        gen.writeNumber(3L);
        gen.writeNumber(4.5);
        gen.writeNumber(6.7f);
        gen.writeNumber(BigInteger.valueOf(8));
        gen.writeNumber(new BigDecimal("9.9"));
        gen.writeNumber("10");
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"1\",\"2\",\"3\",\"4.5\",\"6.7\",\"8\",\"9.9\",\"10\"]", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testBigDecimalAsPlain() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        UTF8JsonGenerator gen = createGenerator(bytesOut, features);

        gen.writeStartArray();
        gen.writeNumber(new BigDecimal("1e-5"));
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[0.00001]", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testUnquotedFieldNames() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        // Disabling quote field names
        UTF8JsonGenerator gen = createGenerator(bytesOut, 0);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("unquotedKey");
        gen.writeString("value");
        gen.writeFieldName(new SerializedString("unquotedKey2"));
        gen.writeString("value2");
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{unquotedKey:\"value\",unquotedKey2:\"value2\"}", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testPrettyPrinter() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeString("b");
        gen.writeFieldName(new SerializedString("c"));
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.contains("\n"));
        Assert.assertTrue(json.contains("\"a\" : \"b\"") || json.contains("\"a\": \"b\"") || json.contains("\"a\" :"));
    }

    @Test
    public void testEscapeNonAsciiFeature() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = createGenerator(bytesOut, features);

        gen.writeString("Hello \u00E9 \u4e16\u754c");
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.contains("\\u00e9") || json.contains("\\u00E9"));
        Assert.assertTrue(json.contains("\\u4e16") || json.contains("\\u4E16"));
    }

    @Test
    public void testCustomCharacterEscapes() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        CharacterEscapes custom = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['a'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("[esc-a]");
                }
                if (ch == 0x100) {
                    return new SerializedString("[esc-100]");
                }
                return null;
            }
        };

        gen.setCharacterEscapes(custom);
        gen.writeString("abc \u0100");
        gen.close();

        Assert.assertEquals("\"[esc-a]bc [esc-100]\"", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testCustomCharacterEscapesLongBuffer() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        final String longEscape = "[VERY_LONG_CUSTOM_ESCAPE_SEQUENCE_EXCEEDING_SIX_BYTES]";
        CharacterEscapes custom = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['#'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == '#') {
                    return new SerializedString(longEscape);
                }
                return null;
            }
        };

        gen.setCharacterEscapes(custom);
        gen.writeString("x#y");
        gen.close();

        Assert.assertEquals("\"x" + longEscape + "y\"", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testWriteRawMethods() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        gen.writeRaw("raw1");
        gen.writeRaw("raw2", 1, 3); // "aw2"
        gen.writeRaw(new SerializedString("raw3"));
        gen.writeRaw('X');
        gen.writeRaw('\u00E9'); // 2-byte utf-8
        gen.writeRaw('\u4E16'); // 3-byte utf-8
        char[] cbuf = new char[] { 'a', 'b', '\u00E9', '\u4E16', 'c' };
        gen.writeRaw(cbuf, 0, cbuf.length);

        gen.close();
        String result = bytesOut.toString("UTF-8");
        Assert.assertEquals("raw1aw2raw3X\u00E9\u4E16ab\u00E9\u4E16c", result);
    }

    @Test
    public void testWriteRawSurrogates() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        // UTF-16 surrogate pair for U+1F600 (Grinning Face): \uD83D\uDE00
        char[] chars = new char[] { '\uD83D', '\uDE00' };
        gen.writeRaw(chars, 0, 2);
        gen.close();

        byte[] expected = "\uD83D\uDE00".getBytes(StandardCharsets.UTF_8);
        Assert.assertArrayEquals(expected, bytesOut.toByteArray());
    }

    @Test(expected = IOException.class)
    public void testWriteRawSplitSurrogateThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        char[] chars = new char[] { '\uD83D' };
        gen.writeRaw(chars, 0, 1);
    }

    @Test
    public void testWriteBinaryByteArray() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        byte[] data = "Testing Base64 encoding functionality".getBytes(StandardCharsets.UTF_8);
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.startsWith("\""));
        Assert.assertTrue(json.endsWith("\""));
        Assert.assertTrue(json.contains("VGVzdGluZyBCYXNlNjQgZW5jb2Rpbmc"));
    }

    @Test
    public void testWriteBinaryInputStreamKnownLength() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        byte[] data = new byte[250];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        int written = gen.writeBinary(Base64Variants.MIME, in, data.length);
        gen.close();

        Assert.assertEquals(data.length, written);
    }

    @Test
    public void testWriteBinaryInputStreamUnknownLength() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 2);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        int written = gen.writeBinary(Base64Variants.MIME, in, -1);
        gen.close();

        Assert.assertEquals(data.length, written);
    }

    @Test(expected = IOException.class)
    public void testWriteBinaryMissingDataThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        byte[] data = new byte[10];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        gen.writeBinary(Base64Variants.MIME, in, 20); // Expecting 20, only 10 available
    }

    @Test
    public void testWriteUTF8StringAndRawUTF8String() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        byte[] utf8 = "hello \"world\" \n test".getBytes(StandardCharsets.UTF_8);
        gen.writeStartArray();
        gen.writeUTF8String(utf8, 0, utf8.length);

        byte[] rawUtf8 = "alreadyEscaped".getBytes(StandardCharsets.UTF_8);
        gen.writeRawUTF8String(rawUtf8, 0, rawUtf8.length);
        gen.writeEndArray();
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.contains("\"hello \\\"world\\\" \\n test\""));
        Assert.assertTrue(json.contains("\"alreadyEscaped\""));
    }

    @Test
    public void testLongStringSegmentWrites() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append("a");
        }
        sb.append(" special: \" \t \n \u00E9 \u4E16 \uD83D\uDE00 ");
        for (int i = 0; i < 4000; i++) {
            sb.append("z");
        }
        String large = sb.toString();

        gen.writeStartArray();
        gen.writeString(large);
        char[] charArr = large.toCharArray();
        gen.writeString(charArr, 0, charArr.length);
        gen.writeEndArray();
        gen.close();

        String json = bytesOut.toString("UTF-8");
        Assert.assertTrue(json.startsWith("[\"aaa"));
        Assert.assertTrue(json.endsWith("zzz\"]"));
    }

    @Test
    public void testLargeRawSegments() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8000; i++) {
            sb.append("x\u00E9");
        }
        gen.writeRaw(sb.toString());
        gen.close();

        String res = bytesOut.toString("UTF-8");
        Assert.assertEquals(sb.toString(), res);
    }

    @Test
    public void testNaNAndInfinityDoublesAndFloats() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        UTF8JsonGenerator gen = createGenerator(bytesOut, features);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[\"NaN\",\"Infinity\",\"NaN\",\"-Infinity\"]", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testAutoCloseJsonContent() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask();
        UTF8JsonGenerator gen = createGenerator(bytesOut, features);

        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.close(); // Should automatically write ']' and '}'

        Assert.assertEquals("{\"arr\":[1]}", bytesOut.toString("UTF-8"));
    }

    @Test(expected = IOException.class)
    public void testWriteEndArrayWithoutStartThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        gen.writeEndArray();
    }

    @Test(expected = IOException.class)
    public void testWriteEndObjectWithoutStartThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        gen.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testFieldNameInRootThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        gen.writeFieldName("rootField");
    }

    @Test(expected = IOException.class)
    public void testValueExpectingFieldNameThrows() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);
        gen.writeStartObject();
        gen.writeNumber(123); // Missing field name
    }

    @Test
    public void testCustomBufferConstructor() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        byte[] buffer = new byte[1024];
        UTF8JsonGenerator gen = new UTF8JsonGenerator(ctxt, 0, null, bytesOut, buffer, 0, false);

        Assert.assertSame(bytesOut, gen.getOutputTarget());
        Assert.assertEquals(0, gen.getOutputBuffered());

        gen.writeString("test");
        Assert.assertTrue(gen.getOutputBuffered() > 0);
        gen.flush();
        Assert.assertEquals(0, gen.getOutputBuffered());
        gen.close();
        Assert.assertEquals("\"test\"", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testWriteRawValueSerializableString() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        gen.writeStartArray();
        gen.writeRawValue(new SerializedString("{\"key\":\"value\"}"));
        gen.writeEndArray();
        gen.close();

        Assert.assertEquals("[{\"key\":\"value\"}]", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testControlCharacterEscaping() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        // Control characters: \u0001, \u001F
        gen.writeString("test\u0001value\u001Fend");
        gen.close();

        Assert.assertEquals("\"test\\u0001value\\u001fend\"", bytesOut.toString("UTF-8"));
    }

    @Test
    public void testLongCustomEscapeFlushing() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bytesOut);

        StringBuilder longEscSeq = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longEscSeq.append("E");
        }
        final String escStr = longEscSeq.toString();

        CharacterEscapes custom = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['@'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == '@') {
                    return new SerializedString(escStr);
                }
                return null;
            }
        };

        gen.setCharacterEscapes(custom);
        gen.writeString("@");
        gen.close();

        Assert.assertEquals("\"" + escStr + "\"", bytesOut.toString("UTF-8"));
    }
}
