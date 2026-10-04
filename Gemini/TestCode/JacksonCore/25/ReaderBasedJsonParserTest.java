package com.fasterxml.jackson.core.json;

import java.io.*;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ReaderBasedJsonParserTest {

    private ReaderBasedJsonParser createParser(String doc) {
        return createParser(doc, 0);
    }

    private ReaderBasedJsonParser createParser(String doc, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(12345);
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym);
    }

    private ReaderBasedJsonParser createParserWithBuffer(String doc, int features, int bufSize) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(12345);
        char[] buf = new char[bufSize];
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym, buf, 0, 0, true);
    }

    @Test
    public void testBasicsAndLifecycle() throws Exception {
        String json = "{\"a\": 123}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertNull(parser.getCodec());
        parser.setCodec(null);
        Assert.assertNotNull(parser.getInputSource());

        StringWriter sw = new StringWriter();
        Assert.assertEquals(0, parser.releaseBuffered(sw));

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("a", parser.getCurrentName());
        Assert.assertEquals("a", parser.getText());
        Assert.assertEquals("a", parser.getValueAsString());
        Assert.assertEquals("a", parser.getValueAsString("def"));
        Assert.assertEquals(1, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNotNull(parser.getTextCharacters());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals("123", parser.getText());
        Assert.assertEquals(3, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNotNull(parser.getTextCharacters());

        StringWriter numSw = new StringWriter();
        int written = parser.getText(numSw);
        Assert.assertEquals(3, written);
        Assert.assertEquals("123", numSw.toString());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testFieldAndTokenLocations() throws Exception {
        String json = "{\n  \"field\": true\n}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        JsonLocation startLoc = parser.getTokenLocation();
        Assert.assertNotNull(startLoc);

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        JsonLocation fieldLoc = parser.getTokenLocation();
        Assert.assertNotNull(fieldLoc);
        Assert.assertEquals(2, fieldLoc.getLineNr());

        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        JsonLocation boolLoc = parser.getTokenLocation();
        Assert.assertNotNull(boolLoc);

        JsonLocation curLoc = parser.getCurrentLocation();
        Assert.assertNotNull(curLoc);

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testFastPathNextFieldName() throws Exception {
        String json = "{\"first\": 1, \"second\": 2, \"third\": [true]}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        SerializedString firstField = new SerializedString("first");
        Assert.assertTrue(parser.nextFieldName(firstField));
        Assert.assertEquals("first", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());

        SerializedString wrongField = new SerializedString("wrong");
        Assert.assertFalse(parser.nextFieldName(wrongField));
        Assert.assertEquals("second", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(2, parser.getIntValue());

        String name = parser.nextFieldName();
        Assert.assertEquals("third", name);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextFieldName());
        parser.close();
    }

    @Test
    public void testNextOptimizedValues() throws Exception {
        String json = "{\"str\":\"hello\", \"int\":42, \"long\":9876543210123, \"boolT\":true, \"boolF\":false, \"arr\":[]}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("hello", parser.nextTextValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(42, parser.nextIntValue(0));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(9876543210123L, parser.nextLongValue(0L));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertNull(parser.nextTextValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextValuesDirectly() throws Exception {
        String json = "[\"text\", 123, 456, true, false]";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals("text", parser.nextTextValue());
        Assert.assertEquals(123, parser.nextIntValue(-1));
        Assert.assertEquals(456L, parser.nextLongValue(-1L));
        Assert.assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        Assert.assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextBooleanValue());
        parser.close();
    }

    @Test
    public void testStringEscapesAndBufferSplits() throws Exception {
        String json = "\"Quote: \\\" Backslash: \\\\ Slash: \\/ Tab: \\t Bell: \\b LF: \\n CR: \\r FF: \\f Hex: \\u0041\"";
        ReaderBasedJsonParser parser = createParserWithBuffer(json, 0, 8);

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String expected = "Quote: \" Backslash: \\ Slash: / Tab: \t Bell: \b LF: \n CR: \r FF: \f Hex: A";
        Assert.assertEquals(expected, parser.getText());

        StringWriter sw = new StringWriter();
        parser.getText(sw);
        Assert.assertEquals(expected, sw.toString());

        parser.finishToken();
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testNumbersParsing() throws Exception {
        String json = "[ 0, 12345, -6789, 0.5, -0.25, 1.25e2, -2.5E-1, 100e+2 ]";
        ReaderBasedJsonParser parser = createParserWithBuffer(json, 0, 6);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(0, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(12345, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-6789, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(0.5, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.25, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(125.0, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(-0.25, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        Assert.assertEquals(10000.0, parser.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNonNumericNumbers() throws Exception {
        int feats = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[ NaN, Infinity, +Infinity, -Infinity, +INF, -INF ]";
        ReaderBasedJsonParser parser = createParser(json, feats);

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
        parser.close();
    }

    @Test
    public void testLeadingZeroesFeature() throws Exception {
        int feats = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        String json = "[ 0123, 0007, -012 ]";
        ReaderBasedJsonParser parser = createParserWithBuffer(json, feats, 4);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(7, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(-12, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws Exception {
        int feats = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                  | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{ unquoted: 'single quoted', 'quotedKey': 'val\\n2' }";
        ReaderBasedJsonParser parser = createParser(json, feats);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("unquoted", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("single quoted", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("quotedKey", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("val\n2", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testComments() throws Exception {
        int feats = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                  | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* C-comment */\n"
                    + "{\n"
                    + "  // C++ comment\n"
                    + "  # YAML comment\n"
                    + "  \"key\": /* inline */ \"value\"\n"
                    + "}";
        ReaderBasedJsonParser parser = createParserWithBuffer(json, feats, 5);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("key", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("value", parser.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testTrailingCommaAndMissingValues() throws Exception {
        int feats = JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask()
                  | JsonParser.Feature.ALLOW_MISSING_VALUES.getMask();
        String json = "{\"a\": 1, } [ 1, , 2, ]";
        ReaderBasedJsonParser parser = createParser(json, feats);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(2, parser.getIntValue());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBinaryValues() throws Exception {
        byte[] original = "Jackson JSON parser binary test string 1234567890".getBytes("UTF-8");
        String b64 = Base64Variants.MIME.encode(original);
        String json = "[\"" + b64 + "\", \"" + b64 + "\"]";

        ReaderBasedJsonParser parser = createParserWithBuffer(json, 0, 7);
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded1 = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(original, decoded1);

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = parser.readBinaryValue(Base64Variants.MIME, baos);
        Assert.assertEquals(original.length, read);
        Assert.assertArrayEquals(original, baos.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSkipStringOnTokenSkip() throws Exception {
        String json = "[\"very long string with \\\" escapes to be skipped\", 42]";
        ReaderBasedJsonParser parser = createParserWithBuffer(json, 0, 5);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Next token without reading text from previous string
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testGetTextWriterForNonStrings() throws Exception {
        String json = "{\"f\": true, \"arr\": null}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());

        StringWriter swName = new StringWriter();
        Assert.assertEquals(1, parser.getText(swName));
        Assert.assertEquals("f", swName.toString());

        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        StringWriter swBool = new StringWriter();
        Assert.assertEquals(4, parser.getText(swBool));
        Assert.assertEquals("true", swBool.toString());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        StringWriter swNull = new StringWriter();
        Assert.assertEquals(4, parser.getText(swNull));
        Assert.assertEquals("null", swNull.toString());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        StringWriter swEnd = new StringWriter();
        Assert.assertEquals(1, parser.getText(swEnd));
        Assert.assertEquals("}", swEnd.toString());
        parser.close();
    }

    @Test
    public void testCRLFLineCounting() throws Exception {
        String json = "{\r\n\"a\": 1,\r\"b\": 2\n}";
        ReaderBasedJsonParser parser = createParser(json);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedClosingScopeArray() throws Exception {
        ReaderBasedJsonParser parser = createParser("[ 1, 2 }");
        parser.nextToken(); // [
        parser.nextToken(); // 1
        parser.nextToken(); // 2
        parser.nextToken(); // error on }
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedClosingScopeObject() throws Exception {
        ReaderBasedJsonParser parser = createParser("{ \"a\": 1 ]");
        parser.nextToken(); // {
        parser.nextToken(); // field
        parser.nextToken(); // 1
        parser.nextToken(); // error on ]
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberStart() throws Exception {
        ReaderBasedJsonParser parser = createParser("-x");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedComment() throws Exception {
        int feats = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        ReaderBasedJsonParser parser = createParser("/* unclosed comment", feats);
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedString() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"unclosed string");
        parser.nextToken();
        parser.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidEscapeSequence() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"invalid escape: \\q \"");
        parser.nextToken();
        parser.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidHexEscape() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"invalid hex: \\u00AZ \"");
        parser.nextToken();
        parser.getText();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidToken() throws Exception {
        ReaderBasedJsonParser parser = createParser("truth");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testRootSpaceMissing() throws Exception {
        ReaderBasedJsonParser parser = createParser("123abc");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws Exception {
        ReaderBasedJsonParser parser = createParser("{ \"key\" 123 }");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingComma() throws Exception {
        ReaderBasedJsonParser parser = createParser("[ 1 2 ]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }
}
