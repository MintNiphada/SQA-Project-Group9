package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class UTF8StreamJsonParserTest {

    private UTF8StreamJsonParser createParser(String json) {
        return createParser(json.getBytes(StandardCharsets.UTF_8), 0);
    }

    private UTF8StreamJsonParser createParser(String json, int features) {
        return createParser(json.getBytes(StandardCharsets.UTF_8), features);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, features, new ByteArrayInputStream(bytes), null, sym,
                new byte[bytes.length > 0 ? bytes.length : 1], 0, 0, true);
    }

    private UTF8StreamJsonParser createParserWithChunkedStream(byte[] bytes, int chunkSize, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        InputStream in = new InputStream() {
            private int ptr = 0;

            @Override
            public int read() {
                if (ptr >= bytes.length) return -1;
                return bytes[ptr++] & 0xFF;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (ptr >= bytes.length) return -1;
                int toRead = Math.min(len, Math.min(chunkSize, bytes.length - ptr));
                System.arraycopy(bytes, ptr, b, off, toRead);
                ptr += toRead;
                return toRead;
            }
        };
        byte[] inputBuf = new byte[chunkSize < 16 ? 16 : chunkSize];
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, inputBuf, 0, 0, true);
    }

    private int enableFeature(JsonParser.Feature... feats) {
        int mask = 0;
        for (JsonParser.Feature f : feats) {
            mask |= f.getMask();
        }
        return mask;
    }

    @Test
    public void testLifecycleAndCodec() throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(0);
        byte[] buf = new byte[100];
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ '1' });
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, 0, in, null, sym, buf, 0, 0, true);

        Assert.assertNull(p.getCodec());
        p.setCodec(null);
        Assert.assertNull(p.getCodec());
        Assert.assertSame(in, p.getInputSource());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Assert.assertEquals(0, p.releaseBuffered(out));

        p.close();
        Assert.assertTrue(p.isClosed());
        p._closeInput();
        p._releaseBuffers();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        byte[] data = "123456789".getBytes(StandardCharsets.UTF_8);
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(0);
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(data), null, sym, data, 2, 7, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        Assert.assertEquals(5, released);
        Assert.assertArrayEquals("34567".getBytes(StandardCharsets.UTF_8), out.toByteArray());
        p.close();
    }

    @Test
    public void testBasicTokensAndLocations() throws IOException {
        String doc = "{\"a\": [true, false, null, 123, -456, 78.9, 1e2, -0.5e-2]}";
        UTF8StreamJsonParser p = createParser(doc);

        Assert.assertNull(p.getCurrentToken());
        Assert.assertNull(p.getTextCharacters());
        Assert.assertEquals(0, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("{", p.getText());
        JsonLocation loc = p.getTokenLocation();
        Assert.assertEquals(1, loc.getLineNr());
        JsonLocation curLoc = p.getCurrentLocation();
        Assert.assertTrue(curLoc.getLineNr() >= 1);

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getText());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertNotNull(p.getTextCharacters());
        Assert.assertEquals(1, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals("[", p.getText());

        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertTrue(p.getBooleanValue());
        Assert.assertEquals("true", p.getText());
        Assert.assertArrayEquals("true".toCharArray(), p.getTextCharacters());

        Assert.assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        Assert.assertFalse(p.getBooleanValue());

        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("null", p.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(123, p.getValueAsInt());
        Assert.assertEquals(123, p.getValueAsInt(99));
        Assert.assertEquals("123", p.getValueAsString());
        Assert.assertEquals("123", p.getValueAsString("def"));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-456, p.getIntValue());
        Assert.assertEquals(-456L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(78.9, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(78, p.getValueAsInt());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(100.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-0.005, p.getDoubleValue(), 0.000001);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());

        p.close();
    }

    @Test
    public void testNextMethods() throws IOException {
        String doc = "{\"name\": \"value\", \"num\": 42, \"flag\": true, \"arr\": [1]}";
        UTF8StreamJsonParser p = createParser(doc);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertTrue(p.nextFieldName(new SerializedString("name")));
        Assert.assertEquals("value", p.nextTextValue());

        Assert.assertTrue(p.nextFieldName(new SerializedString("num")));
        Assert.assertEquals(42, p.nextIntValue(0));

        Assert.assertTrue(p.nextFieldName(new SerializedString("flag")));
        Assert.assertEquals(Boolean.TRUE, p.nextBooleanValue());

        Assert.assertEquals("arr", p.nextFieldName());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(1L, p.nextLongValue(0L));
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextFieldName());
        p.close();
    }

    @Test
    public void testNextFieldNameVariations() throws IOException {
        String doc = "{\"k1\":\"v1\", \"k2\":2, \"k3\":false, \"k4\":null, \"k5\":-10, \"k6\":[1], \"k7\":{}}";
        UTF8StreamJsonParser p = createParser(doc);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertFalse(p.nextFieldName(new SerializedString("wrong")));
        Assert.assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        Assert.assertEquals("k1", p.getCurrentName());
        Assert.assertEquals("v1", p.nextTextValue());

        Assert.assertEquals("k2", p.nextFieldName());
        Assert.assertEquals(2, p.nextIntValue(-1));

        Assert.assertTrue(p.nextFieldName(new SerializedString("k3")));
        Assert.assertEquals(Boolean.FALSE, p.nextBooleanValue());

        Assert.assertEquals("k4", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        Assert.assertTrue(p.nextFieldName(new SerializedString("k5")));
        Assert.assertEquals(-10L, p.nextLongValue(0L));

        Assert.assertEquals("k6", p.nextFieldName());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());

        Assert.assertTrue(p.nextFieldName(new SerializedString("k7")));
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertFalse(p.nextFieldName(new SerializedString("none")));

        p.close();
    }

    @Test
    public void testVariousLengthFieldNames() throws IOException {
        String doc = "{"
                + "\"a\":1,"
                + "\"ab\":2,"
                + "\"abc\":3,"
                + "\"abcd\":4,"
                + "\"abcde\":5,"
                + "\"abcdef\":6,"
                + "\"abcdefg\":7,"
                + "\"abcdefgh\":8,"
                + "\"abcdefghi\":9,"
                + "\"abcdefghij\":10,"
                + "\"abcdefghijk\":11,"
                + "\"abcdefghijkl\":12,"
                + "\"abcdefghijklm\":13,"
                + "\"this_is_a_very_long_field_name_that_exceeds_quad_buffer_capacity_1234567890_abcdefghij\":14,"
                + "\"\":15"
                + "}";
        UTF8StreamJsonParser p = createParser(doc);
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        String[] expectedNames = new String[]{
                "a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg", "abcdefgh",
                "abcdefghi", "abcdefghij", "abcdefghijk", "abcdefghijkl", "abcdefghijklm",
                "this_is_a_very_long_field_name_that_exceeds_quad_buffer_capacity_1234567890_abcdefghij",
                ""
        };

        for (int i = 0; i < expectedNames.length; i++) {
            Assert.assertEquals(expectedNames[i], p.nextFieldName());
            Assert.assertEquals(i + 1, p.nextIntValue(-1));
        }
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testEscapedFieldNamesAndMultiByte() throws IOException {
        String doc = "{\"a\\\"b\\\\c\\/d\\b\\f\\n\\r\\t\\u0041\": 1, \"\\u00e9\": 2, \"\\u4e16\\u754c\": 3, \"\uD83D\uDE00\": 4}";
        UTF8StreamJsonParser p = createParser(doc);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("a\"b\\c/d\b\f\n\r\tA", p.nextFieldName());
        Assert.assertEquals(1, p.nextIntValue(0));

        Assert.assertEquals("\u00e9", p.nextFieldName());
        Assert.assertEquals(2, p.nextIntValue(0));

        Assert.assertEquals("\u4e16\u754c", p.nextFieldName());
        Assert.assertEquals(3, p.nextIntValue(0));

        Assert.assertEquals("\uD83D\uDE00", p.nextFieldName());
        Assert.assertEquals(4, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testNonStandardFeatures() throws IOException {
        int feats = enableFeature(
                JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
                JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_YAML_COMMENTS,
                JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS,
                JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS
        );

        String doc = "/* comment */ { // line comment\n"
                + "# yaml comment\n"
                + "unquoted_key: 'single quoted \\' val',\n"
                + "'single_key': 'val2',\n"
                + "'nan': NaN,\n"
                + "'inf': +Infinity,\n"
                + "'ninf': -Infinity,\n"
                + "'leadZero': 007\n"
                + "}";
        UTF8StreamJsonParser p = createParser(doc, feats);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals("unquoted_key", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("single quoted ' val", p.getText());

        Assert.assertEquals("single_key", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val2", p.getText());

        Assert.assertEquals("nan", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertTrue(Double.isNaN(p.getDoubleValue()));

        Assert.assertEquals("inf", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals("ninf", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals("leadZero", p.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(7, p.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testChunkedStreamStringAndNumbers() throws IOException {
        String doc = "[\"abcdefghijklmnopqrstuvwxyz0123456789\", 123456789012345, -987654321012345, 123.456e+2, \"\u00A2\u20AC\uD83D\uDE00\"]";
        byte[] bytes = doc.getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParserWithChunkedStream(bytes, 4, 0);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("abcdefghijklmnopqrstuvwxyz0123456789", p.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123456789012345L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-987654321012345L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(12345.6, p.getDoubleValue(), 0.01);

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("\u00A2\u20AC\uD83D\uDE00", p.getText());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64Decoding() throws IOException {
        byte[] raw = "Jackson JSON Base64 Parsing Test! 1234567890".getBytes(StandardCharsets.UTF_8);
        Base64Variant b64 = Base64Variants.MIME;
        String encoded = b64.encode(raw);

        String doc = "[\"" + encoded + "\", \"" + encoded + "\"]";
        UTF8StreamJsonParser p = createParser(doc);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded1 = p.getBinaryValue(b64);
        Assert.assertArrayEquals(raw, decoded1);

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(b64, out);
        Assert.assertEquals(raw.length, count);
        Assert.assertArrayEquals(raw, out.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64WithoutPadding() throws IOException {
        byte[] raw = "test".getBytes(StandardCharsets.UTF_8);
        Base64Variant b64 = Base64Variants.MODIFIED_FOR_URL;
        String encoded = b64.encode(raw, false);

        String doc = "[\"" + encoded + "\"]";
        UTF8StreamJsonParser p = createParser(doc);
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(b64);
        Assert.assertArrayEquals(raw, decoded);
        p.close();
    }

    @Test
    public void testSkipStringAndComments() throws IOException {
        String doc = "/* multi \n line \n comment */ // single line\n [\"first string to skip\", \"second\"]";
        UTF8StreamJsonParser p = createParser(doc, enableFeature(JsonParser.Feature.ALLOW_COMMENTS));

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("second", p.getText());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testGrowArrayBy() {
        int[] arr = new int[]{ 1, 2, 3 };
        int[] grown = UTF8StreamJsonParser.growArrayBy(arr, 2);
        Assert.assertEquals(5, grown.length);
        Assert.assertEquals(1, grown[0]);
        Assert.assertEquals(2, grown[1]);
        Assert.assertEquals(3, grown[2]);
        Assert.assertEquals(0, grown[3]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        Assert.assertEquals(4, fromNull.length);
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndArray() throws IOException {
        UTF8StreamJsonParser p = createParser("}");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndObject() throws IOException {
        UTF8StreamJsonParser p = createParser("]");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"key\" \"val\"}");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingComma() throws IOException {
        UTF8StreamJsonParser p = createParser("[1 2]");
        p.nextToken();
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberFollowsMinus() throws IOException {
        UTF8StreamJsonParser p = createParser("[-a]");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZeroesNotAllowedByDefault() throws IOException {
        UTF8StreamJsonParser p = createParser("[0123]");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnquotedFieldNotAllowedByDefault() throws IOException {
        UTF8StreamJsonParser p = createParser("{abc: 1}");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testCommentsNotAllowedByDefault() throws IOException {
        UTF8StreamJsonParser p = createParser("/- comment */ 123");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"unclosed string");
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidEscapeHex() throws IOException {
        UTF8StreamJsonParser p = createParser("\"\\u00AG\"");
        p.nextToken();
        p.getText();
    }

    @Test
    public void testEmptyAndWhitespaceInput() throws IOException {
        UTF8StreamJsonParser p1 = createParser("");
        Assert.assertNull(p1.nextToken());
        p1.close();

        UTF8StreamJsonParser p2 = createParser("   \t\r\n   ");
        Assert.assertNull(p2.nextToken());
        p2.close();
    }
}
