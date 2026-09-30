package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class TargetClassTest {

    private ReaderBasedJsonParser createParser(String doc) throws IOException {
        JsonFactory f = new JsonFactory();
        return (ReaderBasedJsonParser) f.createParser(new StringReader(doc));
    }

    private ReaderBasedJsonParser createParser(String doc, JsonFactory f) throws IOException {
        return (ReaderBasedJsonParser) f.createParser(new StringReader(doc));
    }

    private ReaderBasedJsonParser createParserDirect(Reader r, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot().makeChild(true, true);
        return new ReaderBasedJsonParser(ctxt, features, r, null, sym);
    }

    @Test
    public void testBasicsAndCodec() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":\"value\"}");
        Assert.assertNull(parser.getCodec());
        parser.setCodec(null);
        Assert.assertNull(parser.getCodec());
        Assert.assertNotNull(parser.getInputSource());

        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        Assert.assertEquals(0, released);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getText());
        Assert.assertEquals("key", parser.getValueAsString());
        Assert.assertEquals("key", parser.getValueAsString("default"));
        Assert.assertArrayEquals("key".toCharArray(), parser.getTextCharacters());
        Assert.assertEquals(3, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("value", parser.getText());
        Assert.assertEquals("value", parser.getValueAsString());
        Assert.assertEquals("value", parser.getValueAsString("default"));
        Assert.assertEquals(5, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getTextCharacters());
        Assert.assertEquals(0, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testNumberParsing() throws IOException {
        String json = "[ 0, 123, -456, 0.5, -0.25, 1.25e2, 1.25E-2, 1.25e+2, -12.5E2 ]";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals("0", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(123L, parser.getLongValue());
        Assert.assertEquals("123", parser.getText());
        Assert.assertEquals(3, parser.getTextLength());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-456, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.5, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.25, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(125.0, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.0125, parser.getDoubleValue(), 0.00001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(125.0, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-1250.0, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testLeadingZeroes() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        ReaderBasedJsonParser p = createParser("[ 007, 0123 ]", f);
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(7, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        // Failure without feature
        ReaderBasedJsonParser p2 = createParser("[ 007 ]");
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        try {
            p2.nextToken();
            Assert.fail("Expected failure for leading zeroes");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
        p2.close();
    }

    @Test
    public void testNonNumericNumbers() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser p = createParser("[ NaN, Infinity, +Infinity, -Infinity, +INF, -INF ]", f);

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

        // Failure without feature enabled
        ReaderBasedJsonParser p2 = createParser("[ NaN ]");
        p2.nextToken();
        try {
            p2.nextToken();
            Assert.fail("Expected exception for NaN without feature");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Non-standard token 'NaN'"));
        }
        p2.close();

        ReaderBasedJsonParser p3 = createParser("[ Infinity ]");
        p3.nextToken();
        try {
            p3.nextToken();
            Assert.fail("Expected exception for Infinity without feature");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Non-standard token 'Infinity'"));
        }
        p3.close();
    }

    @Test
    public void testComments() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);

        String json = "/* header comment */\n"
                + "{\n"
                + "  // single line comment\n"
                + "  \"a\": 1, \n"
                + "  # yaml comment\n"
                + "  \"b\": 2\n"
                + "/* block \n * comment \r\n */ }";

        ReaderBasedJsonParser parser = createParser(json, f);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("a", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(2, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testCommentsDisabled() throws IOException {
        ReaderBasedJsonParser p = createParser("/- invalid comment");
        try {
            p.nextToken();
            Assert.fail("Expected comment error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("maybe a (non-standard) comment?"));
        }
        p.close();

        JsonFactory f = new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser p2 = createParser("/- invalid comment", f);
        try {
            p2.nextToken();
            Assert.fail("Expected comment format error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("was expecting either '*' or '/'"));
        }
        p2.close();
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        String json = "{ 'foo' : 'bar\\'s', unquoted : 'val', _test$123 : 456 }";
        ReaderBasedJsonParser parser = createParser(json, f);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("foo", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("bar's", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("unquoted", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("val", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("_test$123", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(456, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testUnquotedFieldNameDisabled() throws IOException {
        ReaderBasedJsonParser p = createParser("{ unquoted: 1 }");
        p.nextToken();
        try {
            p.nextToken();
            Assert.fail("Should fail on unquoted field name");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("was expecting double-quote to start field name"));
        }
        p.close();
    }

    @Test
    public void testEscapeSequences() throws IOException {
        String json = "[\"\\\"\", \"\\\\\", \"\\/\", \"\\b\", \"\\f\", \"\\n\", \"\\r\", \"\\t\", \"\\u0041\\u0020\"]";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\"", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\\", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("/", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\b", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\f", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\n", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\r", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("\t", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("A ", parser.getText());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBooleanAndNullLiterals() throws IOException {
        String json = "[ true, false, null ]";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextValueMethods() throws IOException {
        String json = "{\"s\":\"str\", \"i\":100, \"l\":200, \"t\":true, \"f\":false, \"arr\":[1], \"obj\":{}}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("s", parser.getCurrentName());
        Assert.assertEquals("str", parser.nextTextValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(100, parser.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(200L, parser.nextLongValue(0L));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertNull(parser.nextTextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(42, parser.nextIntValue(42));
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();

        // Directly test next*Value when not at field name
        ReaderBasedJsonParser p2 = createParser("[\"hello\", 5, 9999999999, true, false]");
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        Assert.assertEquals("hello", p2.nextTextValue());
        Assert.assertEquals(5, p2.nextIntValue(0));
        Assert.assertEquals(9999999999L, p2.nextLongValue(0L));
        Assert.assertEquals(Boolean.TRUE, p2.nextBooleanValue());
        Assert.assertEquals(Boolean.FALSE, p2.nextBooleanValue());
        p2.close();
    }

    @Test
    public void testBase64Decoding() throws IOException {
        String base64Str = "SGVsbG8gV29ybGQ="; // "Hello World"
        String json = "[\"" + base64Str + "\"]";

        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertEquals("Hello World", new String(binary, "UTF-8"));
        // Access second time to hit cached binaryValue
        Assert.assertArrayEquals(binary, parser.getBinaryValue(Base64Variants.MIME));

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();

        // Streaming binary read
        ReaderBasedJsonParser p2 = createParser(json);
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p2.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p2.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(11, count);
        Assert.assertEquals("Hello World", new String(out.toByteArray(), "UTF-8"));
        p2.close();
    }

    @Test
    public void testSkipString() throws IOException {
        String json = "{\"a\":\"skip me with \\\"escapes\\\" and \\n newlines\", \"b\":123}";
        ReaderBasedJsonParser parser = createParser(json);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // do NOT call getText(), let nextToken() call _skipString()
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testMismatchedEndMarkers() throws IOException {
        ReaderBasedJsonParser p1 = createParser("{ ]");
        p1.nextToken();
        try {
            p1.nextToken();
            Assert.fail("Expected mismatch error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected close marker ']'"));
        }
        p1.close();

        ReaderBasedJsonParser p2 = createParser("[ }");
        p2.nextToken();
        try {
            p2.nextToken();
            Assert.fail("Expected mismatch error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected close marker '}'"));
        }
        p2.close();
    }

    @Test
    public void testInvalidTokensAndValues() throws IOException {
        ReaderBasedJsonParser p1 = createParser("truth");
        try {
            p1.nextToken();
            Assert.fail("Expected invalid token error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized token 'truth'"));
        }
        p1.close();

        ReaderBasedJsonParser p2 = createParser("+invalid");
        try {
            p2.nextToken();
            Assert.fail("Expected unexpected char error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected digit"));
        }
        p2.close();

        ReaderBasedJsonParser p3 = createParser("@error");
        try {
            p3.nextToken();
            Assert.fail("Expected unexpected char error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected a valid value"));
        }
        p3.close();
    }

    @Test
    public void testInvalidNumberFormats() throws IOException {
        ReaderBasedJsonParser p1 = createParser("1.");
        try {
            p1.nextToken();
            Assert.fail("Expected decimal error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Decimal point not followed by a digit"));
        }
        p1.close();

        ReaderBasedJsonParser p2 = createParser("1e");
        try {
            p2.nextToken();
            Assert.fail("Expected exponent error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Exponent indicator not followed by a digit"));
        }
        p2.close();

        ReaderBasedJsonParser p3 = createParser("-");
        try {
            p3.nextToken();
            Assert.fail("Expected number start error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected digit"));
        }
        p3.close();
    }

    @Test
    public void testInvalidHexEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u00G0\"");
        try {
            parser.nextToken();
            parser.getText();
            Assert.fail("Expected hex escape error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("expected a hex-digit"));
        }
        parser.close();
    }

    @Test
    public void testInvalidEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\z\"");
        try {
            parser.nextToken();
            parser.getText();
            Assert.fail("Expected unrecognized escape error");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized character escape 'z'"));
        }
        parser.close();
    }

    @Test
    public void testDirectCreationAndReleaseBuffers() throws IOException {
        StringReader reader = new StringReader("{\"a\":1}");
        ReaderBasedJsonParser parser = createParserDirect(reader, 0);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.close();
        // Closing again should be safe
        parser.close();
    }
}
