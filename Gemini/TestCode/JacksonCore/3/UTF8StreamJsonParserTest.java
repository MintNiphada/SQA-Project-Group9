package com.fasterxml.jackson.core.json;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class UTF8StreamJsonParserTest {

    private UTF8StreamJsonParser createParser(String doc) {
        return createParser(doc.getBytes(StandardCharsets.UTF_8), 0);
    }

    private UTF8StreamJsonParser createParser(String doc, int features) {
        return createParser(doc.getBytes(StandardCharsets.UTF_8), features);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int features) {
        return createParser(new ByteArrayInputStream(bytes), features, bytes.length);
    }

    private UTF8StreamJsonParser createParser(InputStream in, int features, int bufSize) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, in, false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1234);
        BytesToNameCanonicalizer childSym = sym.makeChild(JsonFactory.Feature.collectDefaults());
        byte[] buf = new byte[Math.max(bufSize, 16)];
        return new UTF8StreamJsonParser(ctxt, features, in, null, childSym, buf, 0, 0, true);
    }

    private UTF8StreamJsonParser createParserPreloaded(byte[] bytes, int features) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, null, false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1234);
        BytesToNameCanonicalizer childSym = sym.makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, features, null, null, childSym, bytes, 0, bytes.length, false);
    }

    @Test
    public void testBasicParsingAndTypes() throws Exception {
        String json = "{\"a\": 123, \"b\": true, \"c\": false, \"d\": null, \"e\": \"hello\", \"f\": [1.5, -2, -0.5e+2]}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertNull(p.getCodec());
        p.setCodec(null);
        Assert.assertNull(p.getCodec());

        Assert.assertNotNull(p.getInputSource());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals("{", p.getText());

        Assert.assertTrue(p.nextFieldName(new SerializedString("a")));
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        Assert.assertEquals(123, p.nextIntValue(0));

        Assert.assertTrue(p.nextFieldName(new SerializedString("b")));
        Assert.assertEquals(Boolean.TRUE, p.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("c", p.getText());
        Assert.assertEquals(Boolean.FALSE, p.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("d", p.getText());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("null", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("e", p.getText());
        Assert.assertEquals("hello", p.nextTextValue());
        Assert.assertEquals("hello", p.getValueAsString());
        Assert.assertEquals("hello", p.getValueAsString("default"));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("f", p.getText());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(1.5, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(-2L, p.nextLongValue(0L));
        Assert.assertEquals(-2, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-50.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertEquals("]", p.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertEquals("}", p.getText());

        Assert.assertNull(p.nextToken());
        p.close();
        Assert.assertTrue(p.isClosed());
    }

    @Test
    public void testTextCharactersAndOffsets() throws Exception {
        String json = "{\"field\": \"value\", \"num\": 456}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertNull(p.getTextCharacters());
        Assert.assertEquals(0, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());

        p.nextToken(); // {
        Assert.assertArrayEquals(new char[]{'{'}, p.getTextCharacters());

        p.nextToken(); // field
        char[] chars = p.getTextCharacters();
        Assert.assertEquals("field", new String(chars, 0, p.getTextLength()));
        Assert.assertEquals(0, p.getTextOffset());
        // verify cached name copy buffer
        Assert.assertArrayEquals(chars, p.getTextCharacters());

        p.nextToken(); // "value"
        Assert.assertEquals("value", new String(p.getTextCharacters(), p.getTextOffset(), p.getTextLength()));

        p.nextToken(); // num
        Assert.assertEquals("num", p.getText());

        p.nextToken(); // 456
        Assert.assertEquals("456", new String(p.getTextCharacters(), p.getTextOffset(), p.getTextLength()));

        p.nextToken(); // }
        p.close();
    }

    @Test
    public void testReleaseBuffered() throws Exception {
        byte[] data = "{\"key\": 123}".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParserPreloaded(data, 0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        Assert.assertEquals(data.length, released);
        Assert.assertArrayEquals(data, out.toByteArray());

        // second release should yield 0
        Assert.assertEquals(0, p.releaseBuffered(out));
        p.close();
    }

    @Test
    public void testNumberParsingVariations() throws Exception {
        String json = "[0, -0, 100, -100, 0.123, -0.123, 1.25e2, 1.25E-2, -1.25e+2, 0e5]";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(100, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-100, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.123, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-0.123, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(125.0, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.0125, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-125.0, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.0, p.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testLeadingZeroesFeature() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser p = createParser("[007, 0123, 00]", feat);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(7, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZeroesDisallowed() throws Exception {
        UTF8StreamJsonParser p = createParser("[007]");
        p.nextToken();
        p.nextToken();
    }

    @Test
    public void testNonNumericNumbers() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        UTF8StreamJsonParser p = createParser("[NaN, Infinity, +Infinity, -Infinity, +INF, -INF]", feat);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertTrue(Double.isNaN(p.getDoubleValue()));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericNumbersDisallowed() throws Exception {
        UTF8StreamJsonParser p = createParser("[NaN]");
        p.nextToken();
        p.nextToken();
    }

    @Test
    public void testSingleQuotesAndUnquotedFieldNames() throws Exception {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{ unquoted: 'single quoted string', 'singleQuotedKey': 'val\\'ue' }";
        UTF8StreamJsonParser p = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("unquoted", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("single quoted string", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("singleQuotedKey", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val'ue", p.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testComments() throws Exception {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* C-style comment */\n"
                + "{\n"
                + "  // C++ style comment\n"
                + "  # YAML style comment\n"
                + "  \"key\": \"value\" /* trailing */\n"
                + "}";
        UTF8StreamJsonParser p = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("key", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("value", p.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testCommentsDisallowed() throws Exception {
        UTF8StreamJsonParser p = createParser("// comment\n{}");
        p.nextToken();
    }

    @Test
    public void testEscapeSequencesInString() throws Exception {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041\\u0042\\u0043\"";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("\" \\ / \b \f \n \r \t ABC", p.getText());
        p.close();
    }

    @Test
    public void testUtf8MultiByteCharacters() throws Exception {
        // 2-byte: \u00a2 (¢), 3-byte: \u20ac (€), 4-byte: \uD83D\uDE00 (😀)
        String unicodeStr = "¢ € \uD83D\uDE00";
        String json = "{\"name_\u00a2_\u20ac_\uD83D\uDE00\": \"" + unicodeStr + "\"}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("name_\u00a2_\u20ac_\uD83D\uDE00", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals(unicodeStr, p.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testVariousFieldNameLengths() throws Exception {
        // 1-4 bytes, 5-8 bytes, >8 bytes, long field names
        String json = "{"
                + "\"a\": 1,"
                + "\"ab\": 2,"
                + "\"abc\": 3,"
                + "\"abcd\": 4,"
                + "\"abcde\": 5,"
                + "\"abcdef\": 6,"
                + "\"abcdefg\": 7,"
                + "\"abcdefgh\": 8,"
                + "\"abcdefghi\": 9,"
                + "\"veryLongFieldNameExceedingQuadBufferCapacity12345678901234567890\": 10,"
                + "\"escaped\\nName\\t\": 11,"
                + "\"escapedUnicode\\u0041\\u0042\": 12"
                + "}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(1, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("ab", p.getCurrentName());
        Assert.assertEquals(2, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abc", p.getCurrentName());
        Assert.assertEquals(3, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcd", p.getCurrentName());
        Assert.assertEquals(4, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcde", p.getCurrentName());
        Assert.assertEquals(5, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdef", p.getCurrentName());
        Assert.assertEquals(6, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefg", p.getCurrentName());
        Assert.assertEquals(7, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefgh", p.getCurrentName());
        Assert.assertEquals(8, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefghi", p.getCurrentName());
        Assert.assertEquals(9, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("veryLongFieldNameExceedingQuadBufferCapacity12345678901234567890", p.getCurrentName());
        Assert.assertEquals(10, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("escaped\nName\t", p.getCurrentName());
        Assert.assertEquals(11, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("escapedUnicodeAB", p.getCurrentName());
        Assert.assertEquals(12, p.nextIntValue(0));

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64DecodingAndStreaming() throws Exception {
        byte[] original = "Hello Base64 World! Testing 1 2 3.".getBytes(StandardCharsets.UTF_8);
        Base64Variant variant = Base64Variants.MIME;
        String encoded = variant.encode(original);

        String json = "{\"bin\": \"" + encoded + "\"}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(variant, out);
        Assert.assertEquals(original.length, count);
        Assert.assertArrayEquals(original, out.toByteArray());

        // second read when complete
        byte[] decoded2 = p.getBinaryValue(variant);
        Assert.assertArrayEquals(original, decoded2);

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64PaddingVariations() throws Exception {
        byte[] b1 = new byte[]{1};
        byte[] b2 = new byte[]{1, 2};
        byte[] b3 = new byte[]{1, 2, 3};

        Base64Variant variant = Base64Variants.MIME;
        String json = "[\"" + variant.encode(b1) + "\", \"" + variant.encode(b2) + "\", \"" + variant.encode(b3) + "\"]";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        p.nextToken();
        Assert.assertArrayEquals(b1, p.getBinaryValue(variant));

        p.nextToken();
        Assert.assertArrayEquals(b2, p.getBinaryValue(variant));

        p.nextToken();
        Assert.assertArrayEquals(b3, p.getBinaryValue(variant));

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testLocations() throws Exception {
        String json = "{\n  \"a\": 1\n}";
        UTF8StreamJsonParser p = createParser(json);

        JsonLocation loc1 = p.getTokenLocation();
        Assert.assertNotNull(loc1);

        p.nextToken(); // {
        JsonLocation loc2 = p.getCurrentLocation();
        Assert.assertEquals(1, loc2.getLineNr());

        p.nextToken(); // a
        JsonLocation loc3 = p.getTokenLocation();
        Assert.assertEquals(2, loc3.getLineNr());

        p.close();
    }

    @Test
    public void testNextOptimizedMethods() throws Exception {
        String json = "{\"txt\":\"hello\",\"n\":10,\"l\":20,\"b1\":true,\"b2\":false,\"arr\":[],\"obj\":{}}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("hello", p.nextTextValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(10, p.nextIntValue(99));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(20L, p.nextLongValue(99L));

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(Boolean.TRUE, p.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals(Boolean.FALSE, p.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertNull(p.nextTextValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertNull(p.nextBooleanValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testGrowArrayBy() {
        int[] arr = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(arr, 5);
        Assert.assertEquals(8, grown.length);
        Assert.assertEquals(1, grown[0]);
        Assert.assertEquals(2, grown[1]);
        Assert.assertEquals(3, grown[2]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        Assert.assertEquals(4, fromNull.length);
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerObjectInArray() throws Exception {
        UTF8StreamJsonParser p = createParser("[}");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerArrayInObject() throws Exception {
        UTF8StreamJsonParser p = createParser("{]");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"key\" 123}");
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnexpectedEndInString() throws Exception {
        UTF8StreamJsonParser p = createParser("\"unfinished string");
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidEscapeSequence() throws Exception {
        UTF8StreamJsonParser p = createParser("\"\\x\"");
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidHexEscapeSequence() throws Exception {
        UTF8StreamJsonParser p = createParser("\"\\u004Z\"");
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testUnrecognizedToken() throws Exception {
        UTF8StreamJsonParser p = createParser("truth");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNegativeNumber() throws Exception {
        UTF8StreamJsonParser p = createParser("-a");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testFractionWithoutDigits() throws Exception {
        UTF8StreamJsonParser p = createParser("1.");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testExponentWithoutDigits() throws Exception {
        UTF8StreamJsonParser p = createParser("1.0e");
        p.nextToken();
    }

    @Test
    public void testSkipString() throws Exception {
        String json = "[\"skipped string with \\\"escapes\\\" and unicode \u00a2 \u20ac\", 123]";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        // do not call getText(), call nextToken() directly to trigger _skipString()
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testEmptyStringsAndObjects() throws Exception {
        String json = "{\"\" : \"\"}";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("", p.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testMultipleTokensSeparatedBySpaces() throws Exception {
        String json = "123  \"str\"  true  false  null";
        UTF8StreamJsonParser p = createParser(json);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("str", p.getText());

        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertNull(p.nextToken());
        p.close();
    }
}
