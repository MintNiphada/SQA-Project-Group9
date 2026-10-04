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
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, doc, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym.makeChild(JsonFactory.Feature.collectDefaults()));
    }

    private ReaderBasedJsonParser createParserWithBuffer(String doc, int features, boolean recyclable) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, doc, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        char[] buf = doc.toCharArray();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym.makeChild(JsonFactory.Feature.collectDefaults()), buf, 0, buf.length, recyclable);
    }

    @Test
    public void testCodecAndInputSource() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        Assert.assertNull(parser.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            @Override public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
            @Override public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, Class<T> valueType) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
            @Override public void writeValue(JsonGenerator gen, Object value) {}
            @Override public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override public TreeNode createObjectNode() { return null; }
            @Override public TreeNode createArrayNode() { return null; }
            @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        parser.setCodec(codec);
        Assert.assertSame(codec, parser.getCodec());
        Assert.assertNotNull(parser.getInputSource());
        parser.close();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser parser = createParserWithBuffer("{\"k\":\"v\"}", 0, false);
        StringWriter sw = new StringWriter();
        int count = parser.releaseBuffered(sw);
        Assert.assertEquals(9, count);
        Assert.assertEquals("{\"k\":\"v\"}", sw.toString());

        StringWriter sw2 = new StringWriter();
        Assert.assertEquals(0, parser.releaseBuffered(sw2));
        parser.close();
    }

    @Test
    public void testBasicObjectAndArrayParsing() throws IOException {
        String json = "{\"name\":\"John\", \"age\":30, \"active\":true, \"data\":null, \"tags\":[\"a\", false]}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getTextCharacters());
        Assert.assertEquals(0, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("{", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("name", parser.getCurrentName());
        Assert.assertEquals("name", parser.getText());
        Assert.assertEquals("name", parser.getValueAsString());
        Assert.assertEquals(4, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNotNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("John", parser.getText());
        Assert.assertEquals("John", parser.getValueAsString());
        Assert.assertEquals("John", parser.getValueAsString("def"));
        Assert.assertEquals(4, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("age", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals("30", parser.getText());
        Assert.assertEquals(30, parser.getIntValue());
        Assert.assertEquals(2, parser.getTextLength());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertEquals("true", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals("null", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals("[", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("a", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        Assert.assertEquals("false", parser.getText());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals("]", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals("}", parser.getText());

        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testStringEscapes() throws IOException {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041\"";
        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\" \\ / \b \f \n \r \t A", parser.getText());
        parser.close();
    }

    @Test
    public void testSkipString() throws IOException {
        String json = "[\"string to skip \\\"with escape\\\"\", 42]";
        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Do not call getText(), call nextToken directly to trigger _skipString
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSingleQuoteStringsAndNames() throws IOException {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        String json = "{'first':'O\\'Reilly', 'second':'test'}";
        ReaderBasedJsonParser parser = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("first", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("O'Reilly", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("second", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("test", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testUnquotedFieldNames() throws IOException {
        int feat = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{foo:123, _bar:456, $baz:789}";
        ReaderBasedJsonParser parser = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("foo", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("_bar", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(456, parser.getIntValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("$baz", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(789, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNumberParsing() throws IOException {
        String[] validNumbers = new String[]{
                "0", "-0", "123", "-456", "0.5", "-0.25", "12.34", "-56.78",
                "1e3", "1E3", "1e+3", "1e-3", "-2.5e2", "-2.5E-2"
        };
        for (String num : validNumbers) {
            ReaderBasedJsonParser parser = createParser(num);
            JsonToken t = parser.nextToken();
            Assert.assertTrue(t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT);
            Assert.assertEquals(num, parser.getText());
            parser.close();
        }
    }

    @Test
    public void testLeadingZeroes() throws IOException {
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        ReaderBasedJsonParser parser = createParser("007", feat);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(7, parser.getIntValue());
        parser.close();

        ReaderBasedJsonParser parser2 = createParser("007", 0);
        try {
            parser2.nextToken();
            Assert.fail("Leading zeroes without feature should fail");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
        parser2.close();
    }

    @Test
    public void testNonNumericNumbers() throws IOException {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[NaN, Infinity, +Infinity, -Infinity, -INF, +INF]";
        ReaderBasedJsonParser parser = createParser(json, feat);

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
        Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testComments() throws IOException {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask() | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* header comment */\n"
                + "{\n"
                + "  // single line comment\n"
                + "  \"key\": /* inline */ \"value\",\n"
                + "  # yaml comment\n"
                + "  \"num\": 1\n"
                + "}";
        ReaderBasedJsonParser parser = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("value", parser.getText());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("num", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBase64Decoding() throws IOException {
        String base64 = "SGVsbG8gV29ybGQh"; // "Hello World!"
        ReaderBasedJsonParser parser = createParser("\"" + base64 + "\"");
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded = parser.getBinaryValue();
        Assert.assertEquals("Hello World!", new String(decoded, "UTF-8"));

        // readBinaryValue
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = parser.readBinaryValue(baos);
        Assert.assertEquals(12, read);
        Assert.assertArrayEquals(decoded, baos.toByteArray());
        parser.close();
    }

    @Test
    public void testReadBinaryValueStreaming() throws IOException {
        String base64 = "SGVsbG8gV29ybGQh";
        ReaderBasedJsonParser parser = createParser("\"" + base64 + "\"");
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = parser.readBinaryValue(Base64Variants.MIME, baos);
        Assert.assertEquals(12, read);
        Assert.assertEquals("Hello World!", new String(baos.toByteArray(), "UTF-8"));
        parser.close();
    }

    @Test
    public void testNextFieldNameAndOptimizedMethods() throws IOException {
        String json = "{\"str\":\"hello\", \"num\":123, \"long\":9876543210, \"bool\":true, \"falseBool\":false, \"arr\":[1], \"obj\":{}}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("str")));
        Assert.assertEquals("hello", parser.nextTextValue());

        Assert.assertEquals("num", parser.nextFieldName());
        Assert.assertEquals(123, parser.nextIntValue(-1));

        Assert.assertEquals("long", parser.nextFieldName());
        Assert.assertEquals(9876543210L, parser.nextLongValue(-1L));

        Assert.assertEquals("bool", parser.nextFieldName());
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        Assert.assertEquals("falseBool", parser.nextFieldName());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        Assert.assertEquals("arr", parser.nextFieldName());
        Assert.assertNull(parser.nextTextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        Assert.assertEquals("obj", parser.nextFieldName());
        Assert.assertEquals(-1, parser.nextIntValue(-1));
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextFieldName());
        parser.close();
    }

    @Test
    public void testNextFieldNameWithDirectValues() throws IOException {
        String json = "{\"a\":\"val\", \"b\":-42, \"c\":100, \"d\":true, \"e\":false, \"f\":null, \"g\":[1], \"h\":{\"sub\":2}}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.nextFieldName(new SerializedString("a")));
        Assert.assertEquals("val", parser.nextTextValue());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("b")));
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-42, parser.getIntValue());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("c")));
        Assert.assertEquals(100, parser.nextIntValue(-1));

        Assert.assertTrue(parser.nextFieldName(new SerializedString("d")));
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("e")));
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("f")));
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("g")));
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        Assert.assertTrue(parser.nextFieldName(new SerializedString("h")));
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testLocations() throws IOException {
        String json = "{\n  \"name\": \"test\"\n}";
        ReaderBasedJsonParser parser = createParser(json);

        JsonLocation loc = parser.getCurrentLocation();
        Assert.assertEquals(1, loc.getLineNr());

        parser.nextToken(); // {
        parser.nextToken(); // name
        JsonLocation tokenLoc = parser.getTokenLocation();
        Assert.assertEquals(2, tokenLoc.getLineNr());

        parser.nextToken(); // "test"
        tokenLoc = parser.getTokenLocation();
        Assert.assertEquals(2, tokenLoc.getLineNr());

        parser.nextToken(); // }
        parser.close();
    }

    @Test
    public void testMismatchedBrackets() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\": [1, 2}}");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.nextToken();
            Assert.fail("Expected mismatched bracket exception");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Mismatched"));
        }
        parser.close();
    }

    @Test
    public void testInvalidNumberStart() throws IOException {
        ReaderBasedJsonParser parser = createParser("-abc");
        try {
            parser.nextToken();
            Assert.fail("Expected invalid number error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected digit (0-9) to follow minus sign"));
        }
        parser.close();
    }

    @Test
    public void testInvalidHexEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u123Z\"");
        try {
            parser.nextToken();
            parser.getText();
            Assert.fail("Expected invalid hex escape error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected a hex-digit"));
        }
        parser.close();
    }

    @Test
    public void testInvalidUnquotedSpaceInString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\n\"");
        try {
            parser.nextToken();
            parser.getText();
            Assert.fail("Expected unquoted space / control char error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }
        parser.close();
    }

    @Test
    public void testUnexpectedEndInArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,");
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.nextToken();
            Assert.fail("Expected unexpected end error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }
        parser.close();
    }

    @Test
    public void testUnrecognizedToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("unknownToken");
        try {
            parser.nextToken();
            Assert.fail("Expected unrecognized token error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized token 'unknownToken'"));
        }
        parser.close();
    }

    @Test
    public void testRootValuesSpacing() throws IOException {
        ReaderBasedJsonParser parser = createParser("123\n456\r\n789\t0");
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(456, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(789, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testSmallBufferSplit() throws IOException {
        // Feed input through a 1-character reading reader to hit loadMore branches
        Reader customReader = new Reader() {
            private final String data = "{\"key\":\"value with some length to cross chunks\", \"num\": 123456.789e2}";
            private int idx = 0;

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (idx >= data.length()) return -1;
                cbuf[off] = data.charAt(idx++);
                return 1;
            }

            @Override
            public void close() throws IOException {}
        };

        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, customReader, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(0);
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, customReader, null, sym.makeChild(JsonFactory.Feature.collectDefaults()));

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("value with some length to cross chunks", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("num", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(123456.789e2, parser.getDoubleValue(), 0.001);

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }
}
