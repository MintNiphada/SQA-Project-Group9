package com.fasterxml.jackson.core.json.async;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class NonBlockingJsonParserTest {

    private IOContext _ioContext;
    private ByteQuadsCanonicalizer _symbols;

    @Before
    public void setUp() {
        _ioContext = new IOContext(new BufferRecycler(), null, false);
        _symbols = ByteQuadsCanonicalizer.createRoot();
    }

    private NonBlockingJsonParser createParser(int features) {
        return new NonBlockingJsonParser(_ioContext, features, _symbols);
    }

    private NonBlockingJsonParser createParser() {
        return createParser(0);
    }

    private void feed(NonBlockingJsonParser p, String input) throws IOException {
        byte[] bytes = input.getBytes("UTF-8");
        p.feedInput(bytes, 0, bytes.length);
    }

    private void feedPartial(NonBlockingJsonParser p, String input, int start, int end) throws IOException {
        byte[] bytes = input.getBytes("UTF-8");
        p.feedInput(bytes, start, end);
    }

    @Test
    public void testEmptyObject() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testSimpleObject() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testArray() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "[1,2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testString() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testTrue() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "true");
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testFalse() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "false");
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNull() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "null");
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberInteger() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberNegative() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "-123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberFloat() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1.23");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.23, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberExponent() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1e5");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(100000.0, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberLeadingZerosAllowed() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "001");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testNumberLeadingZerosDisallowed() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "001");
        p.nextToken();
    }

    @Test
    public void testTrailingCommaArray() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_TRAILING_COMMA.getMask());
        feed(p, "[1,]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testTrailingCommaObject() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_TRAILING_COMMA.getMask());
        feed(p, "{\"a\":1,}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testMissingValues() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_MISSING_VALUES.getMask());
        feed(p, "[1,,2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testSingleQuotes() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_SINGLE_QUOTES.getMask());
        feed(p, "'value'");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testSingleQuotesFieldName() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_SINGLE_QUOTES.getMask());
        feed(p, "{'a':1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testUnquotedNames() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        feed(p, "{a:1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testCComment() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_COMMENTS.getMask());
        feed(p, "/* comment */1");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testCppComment() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_COMMENTS.getMask());
        feed(p, "// comment\n1");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testYamlComment() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_YAML_COMMENTS.getMask());
        feed(p, "# comment\n1");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testEscapedCharsInString() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"\\n\\t\\\\\\\"\\/\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\n\t\\\"/", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testUnicodeEscape() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"\\u0041\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("A", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testUTF8String() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"\u00e9\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u00e9", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testBOM() throws IOException {
        NonBlockingJsonParser p = createParser();
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        p.feedInput(bom, 0, 3);
        feed(p, "1");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNonStandardNaN() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        feed(p, "NaN");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        assertNull(p.nextToken());
    }

    @Test
    public void testNonStandardInfinity() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        feed(p, "Infinity");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNonStandardPlusInfinity() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        feed(p, "+Infinity");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNonStandardMinusInfinity() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        feed(p, "-Infinity");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testUnexpectedChar() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "x");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumber() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "0a");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testEOFIncompleteToken() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"abc");
        p.endOfInput();
        p.nextToken();
    }

    @Test
    public void testFeedInputErrors() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1");
        p.nextToken();
        try {
            p.feedInput(new byte[] { 2 }, 0, 1);
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testFeedInputEndBeforeStart() throws IOException {
        NonBlockingJsonParser p = createParser();
        try {
            p.feedInput(new byte[] { 1 }, 1, 0);
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testFeedInputAfterEndOfInput() throws IOException {
        NonBlockingJsonParser p = createParser();
        p.endOfInput();
        try {
            p.feedInput(new byte[] { 1 }, 0, 1);
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "123");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        assertEquals(3, released);
        assertArrayEquals("123".getBytes("UTF-8"), out.toByteArray());
    }

    @Test
    public void testNonBlockingPartialFeed() throws IOException {
        NonBlockingJsonParser p = createParser();
        feedPartial(p, "{\"a\":1}", 0, 4);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        feedPartial(p, "{\"a\":1}", 4, 7);
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        feedPartial(p, "{\"a\":1}", 7, 7);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNeedMoreInput() throws IOException {
        NonBlockingJsonParser p = createParser();
        assertTrue(p.needMoreInput());
        feed(p, "1");
        assertFalse(p.needMoreInput());
        p.nextToken();
        assertTrue(p.needMoreInput());
        p.endOfInput();
        assertFalse(p.needMoreInput());
    }

    @Test
    public void testEndOfInput() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1");
        p.nextToken();
        p.endOfInput();
        assertNull(p.nextToken());
    }

    @Test
    public void testWhitespaceSkipping() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, " \t\n\r 1");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testEmptyString() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testFieldNameWithEscape() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "{\"a\\nb\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a\nb", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testLongFieldName() throws IOException {
        NonBlockingJsonParser p = createParser();
        String name = "abcdefghijklmnop";
        feed(p, "{\"" + name + "\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(name, p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberZero() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberNegativeZero() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "-0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberFloatNegativeExponent() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1.5e-2");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.015, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberFloatPositiveExponentWithPlus() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1.5e+2");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(150.0, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testMultipleTokens() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "1 2 3");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNestedStructures() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "{\"a\":[1,2],\"b\":{\"c\":3}}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getText());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testCommentInMiddle() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_COMMENTS.getMask());
        feed(p, "[1,/* comment */2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testYamlCommentInMiddle() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_YAML_COMMENTS.getMask());
        feed(p, "[1,# comment\n2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testCppCommentInMiddle() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_COMMENTS.getMask());
        feed(p, "[1,// comment\n2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testUnquotedNameWithDigits() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        feed(p, "{a1:1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a1", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testAposStringWithEscape() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_SINGLE_QUOTES.getMask());
        feed(p, "'a\\'b'");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a'b", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testAposFieldNameWithEscape() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_SINGLE_QUOTES.getMask());
        feed(p, "{'a\\'b':1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a'b", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberWithLeadingZerosAndFloat() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "00.1");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.1, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberWithLeadingZerosAndExponent() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "00e1");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberNegativeLeadingZeros() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "-001");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-1, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberNegativeLeadingZerosFloat() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "-00.1");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.1, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testNumberNegativeLeadingZerosExponent() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        feed(p, "-00e1");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.0);
        assertNull(p.nextToken());
    }

    @Test
    public void testTrailingCommaInArrayWithSpace() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_TRAILING_COMMA.getMask());
        feed(p, "[1 , ]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testTrailingCommaInObjectWithSpace() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_TRAILING_COMMA.getMask());
        feed(p, "{\"a\":1 , }");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testMissingValueInObject() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_MISSING_VALUES.getMask());
        feed(p, "{\"a\":1,,\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testMissingValueAtEnd() throws IOException {
        NonBlockingJsonParser p = createParser(Feature.ALLOW_MISSING_VALUES.getMask());
        feed(p, "[1,]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNonStandardTokenError() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "NaN");
        try {
            p.nextToken();
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testInvalidEscape() throws IOException {
        NonBlockingJsonParser p = createParser();
        feed(p, "\"\\x\"");
        try {
            p.nextToken();
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testInvalidUTF8() throws IOException {
        NonBlockingJsonParser p = createParser();
        byte[] bytes = new byte[] { (byte) 0xC0, (byte) 0x80 };
        p.feedInput(bytes, 0, 2);
        try {
            p.nextToken();
            fail("Should have thrown");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testClose() throws IOException {
        NonBlockingJsonParser p = createParser();
        p.close();
        assertNull(p.nextToken());
    }

    @Test
    public void testGetNonBlockingInputFeeder() {
        NonBlockingJsonParser p = createParser();
        assertSame(p, p.getNonBlockingInputFeeder());
    }

    @Test
    public void testDecodeEscaped() {
        NonBlockingJsonParser p = createParser();
        try {
            p._decodeEscaped();
            fail("Should have thrown");
        } catch (RuntimeException e) {
        }
    }
}
