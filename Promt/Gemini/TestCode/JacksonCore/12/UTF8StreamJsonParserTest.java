package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class UTF8StreamJsonParserTest {

    private UTF8StreamJsonParser createParser(String json) {
        return createParser(json.getBytes(StandardCharsets.UTF_8), 0, true);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int features, boolean autoClose) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test-source", autoClose);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1)
                .makeChild(JsonFactory.Feature.collectDefaults());
        InputStream in = new ByteArrayInputStream(bytes);
        byte[] buf = new byte[Math.max(16, bytes.length)];
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, buf, 0, 0, true);
    }

    private UTF8StreamJsonParser createBufferedParser(byte[] bytes, int features) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test-source", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1)
                .makeChild(JsonFactory.Feature.collectDefaults());
        // Pre-loaded buffer with no active input stream
        return new UTF8StreamJsonParser(ctxt, features, null, null, sym, bytes, 0, bytes.length, false);
    }

    private UTF8StreamJsonParser createTinyBufferParser(String json, int features) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test-source", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1)
                .makeChild(JsonFactory.Feature.collectDefaults());
        InputStream in = new ByteArrayInputStream(bytes);
        byte[] buf = new byte[4]; // small buffer forces frequent loadMore() calls
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, buf, 0, 0, false);
    }

    @Test
    public void testBasicParsingStructure() throws IOException {
        String json = "{\"name\":\"Jackson\",\"age\":10,\"pi\":3.1415,\"flag\":true,\"other\":null,\"empty\":{}}";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertNull(parser.currentToken());
        Assert.assertNull(parser.getText());
        Assert.assertEquals(0, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("{", parser.getText());
        Assert.assertNotNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("name", parser.getCurrentName());
        Assert.assertEquals("name", parser.getText());
        Assert.assertEquals(4, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertEquals("name", parser.getValueAsString());
        Assert.assertEquals("name", parser.getValueAsString("default"));
        Assert.assertNotNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("Jackson", parser.getText());
        Assert.assertEquals("Jackson", parser.getValueAsString());
        Assert.assertEquals(7, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("age", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(10, parser.getIntValue());
        Assert.assertEquals(10, parser.getValueAsInt());
        Assert.assertEquals(10, parser.getValueAsInt(5));
        Assert.assertEquals(10L, parser.getLongValue());
        Assert.assertEquals(10.0, parser.getDoubleValue(), 0.001);
        Assert.assertEquals("10", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("pi", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(3.1415, parser.getDoubleValue(), 0.00001);
        Assert.assertEquals(3, parser.getValueAsInt());
        Assert.assertEquals("3.1415", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("flag", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertTrue(parser.getBooleanValue());
        Assert.assertEquals("true", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("other", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals("null", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("empty", parser.getCurrentName());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testCodecAndSourceAccess() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}");
        Assert.assertNull(parser.getCodec());
        parser.setCodec(null);
        Assert.assertNotNull(parser.getInputSource());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = parser.releaseBuffered(out);
        Assert.assertEquals(0, released);

        byte[] data = "   {}".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser preloaded = createBufferedParser(data, 0);
        released = preloaded.releaseBuffered(out);
        Assert.assertEquals(data.length, released);
        Assert.assertArrayEquals(data, out.toByteArray());

        preloaded.close();
        parser.close();
    }

    @Test
    public void testArrayAndNextValues() throws IOException {
        String json = "{\"arr\":[100,20000000000,true,false,\"str\",null]}";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertTrue(parser.nextFieldName(new SerializedString("arr")));
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(100, parser.nextIntValue(0));
        Assert.assertEquals(20000000000L, parser.nextLongValue(0L));
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        Assert.assertEquals("str", parser.nextTextValue());

        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertNull(parser.nextBooleanValue());

        parser.close();
    }

    @Test
    public void testFastFieldMatchingAndVariants() throws IOException {
        String json = "{\n  \"a\": 1, \t\"longFieldNameThatSpans\": 2, \"escaped\\\"name\": 3, \"x\": \"val\"\n}";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        SerializedString aStr = new SerializedString("a");
        Assert.assertTrue(parser.nextFieldName(aStr));
        Assert.assertEquals(1, parser.nextIntValue(0));

        SerializedString longStr = new SerializedString("longFieldNameThatSpans");
        Assert.assertTrue(parser.nextFieldName(longStr));
        Assert.assertEquals(2, parser.nextIntValue(0));

        SerializedString mismatch = new SerializedString("wrong");
        Assert.assertFalse(parser.nextFieldName(mismatch));
        Assert.assertEquals("escaped\"name", parser.getCurrentName());
        Assert.assertEquals(3, parser.nextIntValue(0));

        Assert.assertEquals("x", parser.nextFieldName());
        Assert.assertEquals("val", parser.nextTextValue());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextFieldName());
        Assert.assertNull(parser.nextToken());

        parser.close();
    }

    @Test
    public void testFieldNameLengthVariants() throws IOException {
        // Test names with 1 to 16 characters (1, 2, 3, 4 bytes, medium, medium2, and long)
        StringBuilder sb = new StringBuilder("{");
        for (int i = 1; i <= 20; i++) {
            StringBuilder name = new StringBuilder();
            for (int j = 0; j < i; j++) {
                name.append((char) ('a' + (j % 26)));
            }
            sb.append("\"").append(name.toString()).append("\":").append(i);
            if (i < 20) sb.append(",");
        }
        sb.append("}");

        UTF8StreamJsonParser parser = createParser(sb.toString());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        for (int i = 1; i <= 20; i++) {
            Assert.assertNotNull(parser.nextFieldName());
            Assert.assertEquals(i, parser.nextIntValue(0));
        }

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testEscapedAndUtf8Names() throws IOException {
        String json = "{\"\\u0041\\n\\t\\r\\b\\f\\\\\\/\\\"\": 1, \"\\u00e9l\\u00e8ve\": 2, \"\\u4e16\\u754c\": 3, \"\\uD83D\\uDE00\": 4, \"\": 5}";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("A\n\t\r\b\f\\/\"", parser.nextFieldName());
        Assert.assertEquals(1, parser.nextIntValue(0));

        Assert.assertEquals("\u00e9l\u00e8ve", parser.nextFieldName());
        Assert.assertEquals(2, parser.nextIntValue(0));

        Assert.assertEquals("\u4e16\u754c", parser.nextFieldName());
        Assert.assertEquals(3, parser.nextIntValue(0));

        Assert.assertEquals("\uD83D\uDE00", parser.nextFieldName());
        Assert.assertEquals(4, parser.nextIntValue(0));

        Assert.assertEquals("", parser.nextFieldName());
        Assert.assertEquals(5, parser.nextIntValue(0));

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNumbersPositiveAndNegativeAndFloats() throws IOException {
        String json = "[0, -0, 123456789, -987654321, 0.001, -0.5, 1.25e3, -2.5E-2, 1e+2, -0.123e4]";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals(0, parser.getValueAsInt());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123456789, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-987654321, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.001, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.5, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(1250.0, parser.getDoubleValue(), 0.1);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.025, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(100.0, parser.getDoubleValue(), 0.1);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-1230.0, parser.getDoubleValue(), 0.1);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNumbersAcrossBufferBoundaries() throws IOException {
        String json = "[-123456789, 987654321, -12.3456e+2, 0.987654321]";
        UTF8StreamJsonParser parser = createTinyBufferParser(json, 0);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-123456789, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(987654321, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-1234.56, parser.getDoubleValue(), 0.01);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.987654321, parser.getDoubleValue(), 0.000000001);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testLeadingZeroesFeature() throws IOException {
        String json = "[007, -012]";
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser parser = createParser(json.getBytes(StandardCharsets.UTF_8), feat, false);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(7, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-12, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZeroesDisabled() throws IOException {
        String json = "[007]";
        UTF8StreamJsonParser parser = createParser(json);
        parser.nextToken();
        parser.nextToken();
    }

    @Test
    public void testAllowNonNumericNumbers() throws IOException {
        String json = "[NaN, -Infinity, +Infinity, Infinity, -INF, +INF]";
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        UTF8StreamJsonParser parser = createParser(json.getBytes(StandardCharsets.UTF_8), feat, false);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertTrue(Double.isNaN(parser.getDoubleValue()));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testCommentsAndWhitespace() throws IOException {
        String json = "// Single line comment\n"
                + "/* Multi-line\n comment */"
                + "{\n"
                + "  # Yaml comment\r\n"
                + "  \"key\" /* inline */ : // comment\r\n"
                + "  \"value\"\n"
                + "}";
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        UTF8StreamJsonParser parser = createParser(json.getBytes(StandardCharsets.UTF_8), feat, false);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("key", parser.nextFieldName());
        Assert.assertEquals("value", parser.nextTextValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws IOException {
        String json = "{ unquoted: 'single quoted string', 'singleQuotedKey': 123 }";
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        UTF8StreamJsonParser parser = createParser(json.getBytes(StandardCharsets.UTF_8), feat, false);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("unquoted", parser.nextFieldName());
        Assert.assertEquals("single quoted string", parser.nextTextValue());

        Assert.assertEquals("singleQuotedKey", parser.nextFieldName());
        Assert.assertEquals(123, parser.nextIntValue(0));

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testUtf8MultiByteInStrings() throws IOException {
        // 2-byte, 3-byte, and 4-byte UTF-8 sequences
        String json = "[\"\\u00A2\", \"\\u20AC\", \"\\uD83D\\uDE00\", \"plain text\"]";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\u00A2", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\u20AC", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\uD83D\uDE00", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("plain text", parser.getText());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSkipStringMultiByteAndEscapes() throws IOException {
        String json = "[\"\\u00A2\\u20AC\\uD83D\\uDE00\\n\\t\\\\\", 42]";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Skip string by directly calling nextToken() without getText()
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        parser.close();
    }

    @Test
    public void testBase64BinaryParsing() throws IOException {
        byte[] original = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        String base64Str = Base64Variants.MIME.encode(original);
        String json = "{\"data\":\"" + base64Str + "\"}";

        UTF8StreamJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("data", parser.nextFieldName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(original, decoded);

        // Repeated getBinaryValue when not incomplete
        byte[] decoded2 = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(original, decoded2);

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testReadBinaryValueIncremental() throws IOException {
        byte[] original = "Hello, Base64 Incremental Binary Streaming!".getBytes(StandardCharsets.UTF_8);
        String base64Str = Base64Variants.MIME.encode(original);
        String json = "[\"" + base64Str + "\"]";

        UTF8StreamJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(original.length, bytesRead);
        Assert.assertArrayEquals(original, out.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testLocations() throws IOException {
        String json = "{\n  \"field\": 123\n}";
        UTF8StreamJsonParser parser = createParser(json);

        JsonLocation loc0 = parser.getCurrentLocation();
        Assert.assertEquals(1, loc0.getLineNr());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("field", parser.nextFieldName());

        JsonLocation fieldLoc = parser.getTokenLocation();
        Assert.assertEquals(2, fieldLoc.getLineNr());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        JsonLocation valLoc = parser.getTokenLocation();
        Assert.assertEquals(2, valLoc.getLineNr());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testGrowArrayBy() {
        int[] original = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(original, 5);
        Assert.assertEquals(8, grown.length);
        Assert.assertEquals(1, grown[0]);
        Assert.assertEquals(2, grown[1]);
        Assert.assertEquals(3, grown[2]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        Assert.assertEquals(4, fromNull.length);
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("{ ]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("[ }");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnexpectedCharacterInValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("?");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("trueish");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"unclosed string");
        parser.nextToken();
        parser.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedComment() throws IOException {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        UTF8StreamJsonParser parser = createParser("/* unclosed comment ".getBytes(StandardCharsets.UTF_8), feat, false);
        parser.nextToken();
    }

    @Test
    public void testRootSpaceSeparators() throws IOException {
        String json = "123 \t\r\n456";
        UTF8StreamJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(456, parser.getIntValue());

        Assert.assertNull(parser.nextToken());
        parser.close();
    }
}
