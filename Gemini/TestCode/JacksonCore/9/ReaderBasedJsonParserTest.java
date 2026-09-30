package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class ReaderBasedJsonParserTest {

    private ReaderBasedJsonParser createParser(String doc) {
        return createParser(doc, 0);
    }

    private ReaderBasedJsonParser createParser(String doc, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), doc, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, symbols);
    }

    private ReaderBasedJsonParser createParserWithBuffer(String doc, int features, boolean recyclable) {
        IOContext ctxt = new IOContext(new BufferRecycler(), doc, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        char[] buf = doc.toCharArray();
        return new ReaderBasedJsonParser(ctxt, features, null, null, symbols, buf, 0, buf.length, recyclable);
    }

    @Test
    public void testConstructorsAndCodecs() throws Exception {
        ReaderBasedJsonParser parser = createParser("{}");
        Assert.assertNull(parser.getCodec());
        parser.setCodec(null);
        Assert.assertNull(parser.getCodec());
        Assert.assertNotNull(parser.getInputSource());
        parser.close();

        ReaderBasedJsonParser parser2 = createParserWithBuffer("{}", 0, false);
        Assert.assertNull(parser2.getInputSource());
        parser2.close();
    }

    @Test
    public void testReleaseBuffered() throws Exception {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        char[] buf = "12345".toCharArray();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, null, null, symbols, buf, 1, 4, false);

        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        Assert.assertEquals(3, released);
        Assert.assertEquals("234", sw.toString());

        StringWriter sw2 = new StringWriter();
        Assert.assertEquals(0, parser.releaseBuffered(sw2));
        parser.close();
    }

    @Test
    public void testBasicTokensAndStructure() throws Exception {
        String json = "{\"name\":\"test\", \"val\":123, \"flag\":true, \"flag2\":false, \"nil\":null, \"arr\":[1,2]}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getTextCharacters());
        Assert.assertEquals(0, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNull(parser.getValueAsString());
        Assert.assertEquals("default", parser.getValueAsString("default"));

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals("{", parser.getText());
        Assert.assertArrayEquals(new char[]{'{'}, parser.getTextCharacters());
        Assert.assertEquals(1, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("name", parser.getCurrentName());
        Assert.assertEquals("name", parser.getText());
        char[] nameChars = parser.getTextCharacters();
        Assert.assertEquals("name", new String(nameChars, 0, parser.getTextLength()));

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("test", parser.getText());
        Assert.assertEquals("test", parser.getValueAsString());
        Assert.assertEquals("test", parser.getValueAsString("def"));
        Assert.assertEquals(4, parser.getTextLength());
        Assert.assertNotNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals("123", parser.getText());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(3, parser.getTextLength());
        Assert.assertTrue(parser.getTextOffset() >= 0);

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertEquals("true", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        Assert.assertEquals("false", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals("null", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();
    }

    @Test
    public void testLookaheadValueMethods() throws Exception {
        String json = "{\"text\":\"hello\",\"num\":42,\"big\":9876543210123,\"t\":true,\"f\":false,\"arr\":[],\"obj\":{}}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("hello", parser.nextTextValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(42, parser.nextIntValue(-1));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(9876543210123L, parser.nextLongValue(-1L));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertNull(parser.nextTextValue());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(-1, parser.nextIntValue(-1));
        Assert.assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();

        // Direct lookahead when not on field name
        ReaderBasedJsonParser parser2 = createParser("\"val\" 10 20L true false");
        Assert.assertEquals("val", parser2.nextTextValue());
        Assert.assertEquals(10, parser2.nextIntValue(0));
        Assert.assertEquals(20L, parser2.nextLongValue(0L));
        Assert.assertEquals(Boolean.TRUE, parser2.nextBooleanValue());
        Assert.assertEquals(Boolean.FALSE, parser2.nextBooleanValue());
        parser2.close();
    }

    @Test
    public void testNumberParsingFormats() throws Exception {
        String doc = "0 -0 123 -123 0.5 -0.5 12.34e5 12.34E-5 -12.34e+5";
        ReaderBasedJsonParser parser = createParser(doc);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals("0", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals("-0", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-123, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.5, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.5, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(12.34e5, parser.getDoubleValue(), 1.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(12.34e-5, parser.getDoubleValue(), 0.000001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-12.34e+5, parser.getDoubleValue(), 1.0);

        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testLeadingZeroes() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        ReaderBasedJsonParser parser = createParser("007 0123", feat);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(7, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        parser.close();

        ReaderBasedJsonParser parserFail = createParser("007", 0);
        try {
            parserFail.nextToken();
            Assert.fail("Should have failed for leading zero");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
        parserFail.close();
    }

    @Test
    public void testNonStandardNumbers() throws Exception {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser parser = createParser("NaN Infinity -Infinity +Infinity +INF -INF", feat);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertTrue(Double.isNaN(parser.getDoubleValue()));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        parser.close();
    }

    @Test
    public void testStringEscapes() throws Exception {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041\\u0020\"";
        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\" \\ / \b \f \n \r \t A ", parser.getText());
        parser.close();
    }

    @Test
    public void testSkipString() throws Exception {
        String json = "[\"skipped string with \\\" escapes \", 42]";
        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        // Next token is string, but we don't call getText(), just call nextToken() directly to exercise _skipString
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws Exception {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{'a': 'single \\' quote', b: 'unquoted key'}";
        ReaderBasedJsonParser parser = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("a", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("single ' quote", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("unquoted key", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testComments() throws Exception {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* block comment */\n"
                + "{\n"
                + "// line comment\n"
                + "# yaml comment\n"
                + "\"key\" /* comment */ : /* comment */ 123 // trailing\n"
                + "}";
        ReaderBasedJsonParser parser = createParser(json, feat);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();
    }

    @Test
    public void testBase64BinaryParsing() throws Exception {
        byte[] original = "Jackson JSON Parser Unit Test 1234567890".getBytes("UTF-8");
        String b64 = Base64Variants.MIME.encode(original);
        String json = "[\"" + b64 + "\", \"" + b64 + "\"]";

        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded1 = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(original, decoded1);

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(original.length, bytesRead);
        Assert.assertArrayEquals(original, out.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBase64WithMissingPaddingAndEscapes() throws Exception {
        byte[] original = "Jackson".getBytes("UTF-8");
        Base64Variant noPadding = Base64Variants.MODIFIED_FOR_URL;
        String b64 = noPadding.encode(original);

        ReaderBasedJsonParser parser = createParser("\"" + b64 + "\"");
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded = parser.getBinaryValue(noPadding);
        Assert.assertArrayEquals(original, decoded);
        parser.close();
    }

    @Test
    public void testMismatchedClosingTokens() throws Exception {
        ReaderBasedJsonParser parser = createParser("}");
        try {
            parser.nextToken();
            Assert.fail("Expected failure on mismatched end marker");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("mismatched"));
        }
        parser.close();

        ReaderBasedJsonParser parser2 = createParser("]");
        try {
            parser2.nextToken();
            Assert.fail("Expected failure on mismatched end marker");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("mismatched"));
        }
        parser2.close();
    }

    @Test
    public void testInvalidNumberFormats() throws Exception {
        String[] invalidNumbers = {"-", "1.", "1.e2", "1e", "1e+", "+123", "0123"};
        for (String num : invalidNumbers) {
            ReaderBasedJsonParser p = createParser(num);
            try {
                p.nextToken();
                Assert.fail("Should have thrown exception for: " + num);
            } catch (JsonParseException e) {
                Assert.assertNotNull(e.getMessage());
            } finally {
                p.close();
            }
        }
    }

    @Test
    public void testInvalidEscapeSequence() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"\\q\"");
        try {
            parser.nextToken();
            parser.getText();
            Assert.fail("Should have failed for invalid escape sequence");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("character escape sequence"));
        }
        parser.close();

        ReaderBasedJsonParser parserHex = createParser("\"\\u00G1\"");
        try {
            parserHex.nextToken();
            parserHex.getText();
            Assert.fail("Should have failed for invalid hex escape");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("hex-digit"));
        }
        parserHex.close();
    }

    @Test
    public void testInvalidTokens() throws Exception {
        ReaderBasedJsonParser parser = createParser("truth");
        try {
            parser.nextToken();
            Assert.fail("Expected error on invalid token");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized token"));
        }
        parser.close();
    }

    @Test
    public void testUnexpectedSeparators() throws Exception {
        ReaderBasedJsonParser parser = createParser("{\"a\" 123}");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        try {
            parser.nextToken();
            Assert.fail("Expected error for missing colon");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expecting a colon"));
        }
        parser.close();

        ReaderBasedJsonParser parser2 = createParser("[1 2]");
        Assert.assertEquals(JsonToken.START_ARRAY, parser2.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser2.nextToken());
        try {
            parser2.nextToken();
            Assert.fail("Expected error for missing comma");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expecting comma"));
        }
        parser2.close();
    }

    @Test
    public void testBufferBoundaryNumberAndStringParsing() throws Exception {
        // Construct a reader that reads in tiny 2-character chunks to force split-buffer execution paths
        final String json = "{\"longKey123456\": 123456789.987654321e-10, \"str\": \"a very long string that spans across multiple buffers easily\"}";
        Reader tinyReader = new Reader() {
            private final Reader in = new StringReader(json);
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return in.read(cbuf, off, Math.min(len, 2));
            }
            @Override
            public void close() throws IOException {
                in.close();
            }
        };

        IOContext ctxt = new IOContext(new BufferRecycler(), json, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, tinyReader, null, symbols);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("longKey123456", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(123456789.987654321e-10, parser.getDoubleValue(), 1e-15);

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("str", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("a very long string that spans across multiple buffers easily", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }
}
