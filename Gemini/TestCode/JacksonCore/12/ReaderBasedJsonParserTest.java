package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class ReaderBasedJsonParserTest {

    private ReaderBasedJsonParser createParser(String doc) {
        return createParser(doc, 0);
    }

    private ReaderBasedJsonParser createParser(String doc, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), doc, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        StringReader reader = new StringReader(doc);
        return new ReaderBasedJsonParser(ctxt, features, reader, null, sym.makeChild(JsonFactory.Feature.collectDefaults()));
    }

    private ReaderBasedJsonParser createParserWithBuffer(String doc, int features, boolean recyclable) {
        IOContext ctxt = new IOContext(new BufferRecycler(), doc, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        char[] buf = doc.toCharArray();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null,
                sym.makeChild(JsonFactory.Feature.collectDefaults()), buf, 0, buf.length, recyclable);
    }

    @Test
    public void testBasicTokensAndNavigation() throws Exception {
        String json = "{\"name\":\"John\", \"age\":30, \"active\":true, \"score\":null, \"tags\":[\"admin\", 12.5]}";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertNull(parser.getCurrentToken());
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals("name", parser.getCurrentName());
            Assert.assertEquals("name", parser.getText());
            Assert.assertEquals("name", parser.getValueAsString());
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals("John", parser.getText());
            Assert.assertEquals("John", parser.getValueAsString());
            Assert.assertNotNull(parser.getTextCharacters());
            Assert.assertEquals(4, parser.getTextLength());
            Assert.assertEquals(0, parser.getTextOffset());

            Assert.assertEquals("age", parser.nextFieldName());
            Assert.assertEquals(30, parser.nextIntValue(0));
            Assert.assertEquals(30, parser.getIntValue());
            Assert.assertEquals(30L, parser.getLongValue());
            Assert.assertEquals("30", parser.getText());

            Assert.assertTrue(parser.nextFieldName(new SerializedString("active")));
            Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

            Assert.assertEquals("score", parser.nextFieldName());
            Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
            Assert.assertEquals("null", parser.getText());

            Assert.assertEquals("tags", parser.nextFieldName());
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            Assert.assertEquals("admin", parser.nextTextValue());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(12.5, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            Assert.assertNull(parser.nextToken());
        }
    }

    @Test
    public void testNextValuesOnDirectCalls() throws Exception {
        String json = "\"sample\" 100 200 true false null";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals("sample", parser.nextTextValue());
            Assert.assertEquals(100, parser.nextIntValue(0));
            Assert.assertEquals(200L, parser.nextLongValue(0L));
            Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());
            Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());
            Assert.assertNull(parser.nextBooleanValue()); // for null token
        }
    }

    @Test
    public void testCodecAndInputSource() throws Exception {
        try (ReaderBasedJsonParser parser = createParser("{}")) {
            Assert.assertNull(parser.getCodec());
            parser.setCodec(null);
            Assert.assertNotNull(parser.getInputSource());
        }
    }

    @Test
    public void testReleaseBuffered() throws Exception {
        String doc = "abcdef";
        IOContext ctxt = new IOContext(new BufferRecycler(), doc, false);
        char[] buf = doc.toCharArray();
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, null, null, sym, buf, 0, buf.length, false);
        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        Assert.assertEquals(6, released);
        Assert.assertEquals("abcdef", sw.toString());
        Assert.assertEquals(0, parser.releaseBuffered(sw));
    }

    @Test
    public void testLocations() throws Exception {
        String json = "{\n  \"a\" : 1\n}";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            JsonLocation loc = parser.getTokenLocation();
            Assert.assertEquals(1, loc.getLineNr());

            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            loc = parser.getTokenLocation();
            Assert.assertEquals(2, loc.getLineNr());

            JsonLocation curLoc = parser.getCurrentLocation();
            Assert.assertTrue(curLoc.getLineNr() >= 1);
        }
    }

    @Test
    public void testNumberParsingFormats() throws Exception {
        String json = "[ 0, 12345, -12345, 0.5, -0.5, 1.25e2, 1.25E+2, -1.25e-2, 10000000000 ]";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(0, parser.getIntValue());

            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(12345, parser.getIntValue());

            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(-12345, parser.getIntValue());

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(0.5, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(-0.5, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(125.0, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(125.0, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(-0.0125, parser.getDoubleValue(), 0.0001);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(10000000000L, parser.getLongValue());

            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
    }

    @Test
    public void testNonNumericNumbersAllowed() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[ NaN, Infinity, +Infinity, -Infinity, +INF, -INF ]";
        try (ReaderBasedJsonParser parser = createParser(json, feat)) {
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertTrue(Double.isNaN(parser.getDoubleValue()));

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

            Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericNumbersDisallowed() throws Exception {
        try (ReaderBasedJsonParser parser = createParser("[ NaN ]", 0)) {
            parser.nextToken();
            parser.nextToken();
        }
    }

    @Test
    public void testLeadingZerosAllowed() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        try (ReaderBasedJsonParser parser = createParser("[ 007, -007 ]", feat)) {
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(7, parser.getIntValue());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(-7, parser.getIntValue());
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZerosDisallowed() throws Exception {
        try (ReaderBasedJsonParser parser = createParser("[ 007 ]", 0)) {
            parser.nextToken();
            parser.nextToken();
        }
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws Exception {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask() | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{ foo : 'bar', 'baz' : 'qux\\n\\t\\u0041' }";
        try (ReaderBasedJsonParser parser = createParser(json, feat)) {
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals("foo", parser.getCurrentName());
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals("bar", parser.getText());

            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals("baz", parser.getCurrentName());
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals("qux\n\tA", parser.getText());
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    @Test
    public void testComments() throws Exception {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask() | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* header comment */\n{\n // line comment\n \"key\": /* mid */ 123 # yaml comment\n}";
        try (ReaderBasedJsonParser parser = createParser(json, feat)) {
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals("key", parser.getCurrentName());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(123, parser.getIntValue());
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    @Test
    public void testEscapesInStrings() throws Exception {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0020\"";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals("\" \\ / \b \f \n \r \t  ", parser.getText());
        }
    }

    @Test
    public void testBinaryValuesBase64() throws Exception {
        // "SGVsbG8gV29ybGQ=" is base64 for "Hello World"
        String json = "[\"SGVsbG8gV29ybGQ=\", \"SGVsbG8=\"]";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            byte[] bytes = parser.getBinaryValue(Base64Variants.MIME);
            Assert.assertEquals("Hello World", new String(bytes, "UTF-8"));

            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read = parser.readBinaryValue(Base64Variants.MIME, out);
            Assert.assertEquals(5, read);
            Assert.assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));

            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
    }

    @Test
    public void testBinaryValueErrors() throws Exception {
        String json = "123";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            parser.nextToken();
            try {
                parser.getBinaryValue(Base64Variants.MIME);
                Assert.fail("Expected exception for non-string binary access");
            } catch (JsonParseException e) {
                Assert.assertTrue(e.getMessage().contains("not VALUE_STRING"));
            }
        }
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerArray() throws Exception {
        try (ReaderBasedJsonParser parser = createParser("}")) {
            parser.nextToken();
        }
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarkerObject() throws Exception {
        try (ReaderBasedJsonParser parser = createParser("]")) {
            parser.nextToken();
        }
    }

    @Test
    public void testSkipString() throws Exception {
        String json = "[\"skip this string\", 42]";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            // nextToken calls nextToken which skips string if incomplete
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            Assert.assertEquals(42, parser.getIntValue());
        }
    }

    @Test
    public void testGetValueAsStringWithDefault() throws Exception {
        String json = "{\"a\":\"foo\", \"b\":null, \"c\":true}";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals("a", parser.getValueAsString("def"));

            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            Assert.assertEquals("foo", parser.getValueAsString("def"));

            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
            Assert.assertEquals("def", parser.getValueAsString("def"));

            Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
            Assert.assertEquals("true", parser.getValueAsString("def"));
        }
    }

    @Test
    public void testConstructorsAndBufferRecycling() throws Exception {
        ReaderBasedJsonParser p1 = createParserWithBuffer("{\"x\":1}", 0, true);
        Assert.assertEquals(JsonToken.START_OBJECT, p1.nextToken());
        p1.close();

        ReaderBasedJsonParser p2 = createParserWithBuffer("{\"x\":1}", 0, false);
        Assert.assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        p2.close();
    }

    @Test
    public void testNextFieldNameMatching() throws Exception {
        String json = "{\"id\":10, \"name\":\"test\", \"flag\":false}";
        try (ReaderBasedJsonParser parser = createParser(json)) {
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

            Assert.assertTrue(parser.nextFieldName(new SerializedString("id")));
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

            Assert.assertFalse(parser.nextFieldName(new SerializedString("wrong")));
            Assert.assertEquals("name", parser.getCurrentName());
            Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

            Assert.assertTrue(parser.nextFieldName(new SerializedString("flag")));
            Assert.assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        }
    }
}
