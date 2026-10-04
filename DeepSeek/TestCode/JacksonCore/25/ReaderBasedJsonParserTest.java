package com.fasterxml.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private static final IOContext SHARED_CTXT = new IOContext(null, null, false);
    private static final CharsToNameCanonicalizer SHARED_SYMS = CharsToNameCanonicalizer.createRoot();

    private ReaderBasedJsonParser parser;

    private void setup(String doc) throws IOException {
        setup(doc, false);
    }

    private void setup(String doc, boolean allowComments) throws IOException {
        int features = JsonFactory.Feature.collectDefaults();
        if (allowComments) {
            features |= Feature.ALLOW_COMMENTS.getMask();
        }
        parser = new ReaderBasedJsonParser(SHARED_CTXT, features, new StringReader(doc),
                null, SHARED_SYMS.makeChild(JsonFactory.Feature.collectDefaults()));
    }

    private void setupWithFeatures(String doc, int features) throws IOException {
        parser = new ReaderBasedJsonParser(SHARED_CTXT, features, new StringReader(doc),
                null, SHARED_SYMS.makeChild(JsonFactory.Feature.collectDefaults()));
    }

    @Test
    public void testGetCodecInitiallyNull() throws IOException {
        setup("{}");
        assertNull(parser.getCodec());
    }

    @Test
    public void testSetAndGetCodec() throws IOException {
        setup("{}");
        ObjectCodec codec = new ObjectCodec() {};
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());
    }

    @Test
    public void testReleaseBufferedReturnsZeroWhenEmpty() throws IOException {
        setup("{}");
        assertEquals(0, parser.releaseBuffered(new StringWriter()));
    }

    @Test
    public void testReleaseBufferedAfterReadingFirstToken() throws IOException {
        setup("42");
        parser.nextToken();
        StringWriter w = new StringWriter();
        int count = parser.releaseBuffered(w);
        assertTrue(count > 0);
        assertEquals("42".substring("42".length() - count), w.toString());
    }

    @Test
    public void testGetInputSourceReturnsReader() throws IOException {
        StringReader r = new StringReader("1");
        parser = new ReaderBasedJsonParser(SHARED_CTXT, 0, r, null, SHARED_SYMS);
        assertSame(r, parser.getInputSource());
    }

    @Test(expected = JsonParseException.class)
    public void testNextCharEofThrows() throws IOException {
        setup("");
        parser.nextToken();
    }

    @Test
    public void testGetTextStringSimple() throws IOException {
        setup("\"abc\"");
        parser.nextToken();
        assertEquals("abc", parser.getText());
    }

    @Test
    public void testGetTextNumeric() throws IOException {
        setup("123");
        parser.nextToken();
        assertEquals("123", parser.getText());
    }

    @Test
    public void testGetTextFieldName() throws IOException {
        setup("{\"key\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals("key", parser.getText());
    }

    @Test
    public void testGetTextBoolean() throws IOException {
        setup("true");
        parser.nextToken();
        assertEquals("true", parser.getText());
    }

    @Test
    public void testGetTextNullToken() throws IOException {
        setup("null");
        parser.nextToken();
        assertEquals("null", parser.getText());
    }

    @Test
    public void testGetTextCharactersFieldName() throws IOException {
        setup("{\"key\":1}");
        parser.nextToken();
        parser.nextToken();
        assertArrayEquals("key".toCharArray(), parser.getTextCharacters());
    }

    @Test
    public void testGetTextCharactersString() throws IOException {
        setup("\"abc\"");
        parser.nextToken();
        assertArrayEquals("abc".toCharArray(), parser.getTextCharacters());
    }

    @Test
    public void testGetTextCharactersNumber() throws IOException {
        setup("123");
        parser.nextToken();
        assertArrayEquals("123".toCharArray(), parser.getTextCharacters());
    }

    @Test
    public void testGetTextLengthFieldName() throws IOException {
        setup("{\"abc\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals(3, parser.getTextLength());
    }

    @Test
    public void testGetTextLengthString() throws IOException {
        setup("\"abcd\"");
        parser.nextToken();
        assertEquals(4, parser.getTextLength());
    }

    @Test
    public void testGetTextOffsetFieldName() throws IOException {
        setup("{\"abc\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals(0, parser.getTextOffset());
    }

    @Test
    public void testGetValueAsStringString() throws IOException {
        setup("\"xyz\"");
        parser.nextToken();
        assertEquals("xyz", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsStringFromFieldName() throws IOException {
        setup("{\"name\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals("name", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsStringNullDefault() throws IOException {
        setup("null");
        parser.nextToken();
        assertEquals("null", parser.getValueAsString("def"));
    }

    @Test
    public void testGetValueAsStringDefaultUsed() throws IOException {
        setup("[]");
        parser.nextToken();
        assertEquals("def", parser.getValueAsString("def"));
    }

    @Test
    public void testNextTokenEndOfInputAfterClose() throws IOException {
        setup("1");
        parser.nextToken();
        parser.close();
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextTokenStartObject() throws IOException {
        setup("{}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    }

    @Test
    public void testNextTokenStartArray() throws IOException {
        setup("[]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
    }

    @Test
    public void testNextTokenTrue() throws IOException {
        setup("true");
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    @Test
    public void testNextTokenFalse() throws IOException {
        setup("false");
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
    }

    @Test
    public void testNextTokenNull() throws IOException {
        setup("null");
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    @Test
    public void testNextTokenString() throws IOException {
        setup("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
    }

    @Test
    public void testNextTokenInteger() throws IOException {
        setup("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testNextTokenNegativeInteger() throws IOException {
        setup("-17");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testNextTokenFloat() throws IOException {
        setup("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
    }

    @Test
    public void testNextTokenExponent() throws IOException {
        setup("2e5");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
    }

    @Test
    public void testNextTokenNegativeFloat() throws IOException {
        setup("-0.5");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
    }

    @Test
    public void testNextTokenObjectWithField() throws IOException {
        setup("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testNextTokenNestedObject() throws IOException {
        setup("{\"outer\":{}}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testNextTokenArrayWithValues() throws IOException {
        setup("[1,2]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test
    public void testNextTokenLeadingZeroError() throws IOException {
        setup("01", false);
        try {
            parser.nextToken();
            fail("Expected exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
    }

    @Test
    public void testAllowLeadingZeros() throws IOException {
        setupWithFeatures("01", Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("1", parser.getText());
    }

    @Test
    public void testTrailingCommaInObject() throws IOException {
        setupWithFeatures("{\"a\":1,}", Feature.ALLOW_TRAILING_COMMA.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testTrailingCommaInArray() throws IOException {
        setupWithFeatures("[1,]", Feature.ALLOW_TRAILING_COMMA.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test
    public void testSingleQuotedString() throws IOException {
        setupWithFeatures("'abc'", Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("abc", parser.getText());
    }

    @Test
    public void testSingleQuotedFieldName() throws IOException {
        setupWithFeatures("{'key':1}", Feature.ALLOW_SINGLE_QUOTES.getMask());
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
    }

    @Test
    public void testUnquotedFieldName() throws IOException {
        setupWithFeatures("{abc:1}", Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
    }

    @Test
    public void testAllowMissingValuesInArray() throws IOException {
        setupWithFeatures("[1,,2]", Feature.ALLOW_MISSING_VALUES.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test
    public void testAllowCommentsSlashSlash() throws IOException {
        setup("// comment\n1", true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testAllowCommentsSlashStar() throws IOException {
        setup("/* comment */ 1", true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testAllowYamlComments() throws IOException {
        int f = Feature.ALLOW_YAML_COMMENTS.getMask();
        setupWithFeatures("# yaml\n1", f);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testNonStandardNaN() throws IOException {
        setupWithFeatures("NaN", Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
    }

    @Test
    public void testNonStandardInfinity() throws IOException {
        setupWithFeatures("Infinity", Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertFalse(parser.getDoubleValue() < 0);
    }

    @Test
    public void testNonStandardNegativeInfinity() throws IOException {
        setupWithFeatures("-Infinity", Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(parser.getDoubleValue() == Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testFinishToken() throws IOException {
        setup("\"abc\"");
        parser.nextToken();
        parser.finishToken();
        assertEquals("abc", parser.getText());
    }

    @Test
    public void testNextFieldNameSimple() throws IOException {
        setup("{\"f\":1}");
        parser.nextToken();
        assertEquals("f", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testNextFieldNameMatchingSerializable() throws IOException {
        setup("{\"simple\":1}");
        parser.nextToken();
        SerializableString sstr = new SerializableString() {
            @Override public String getValue() { return "simple"; }
            @Override public int charLength() { return 6; }
            @Override public char[] asQuotedChars() { return "\"simple\"".toCharArray(); }
            @Override public byte[] asUnquotedUTF8() { return null; }
            @Override public byte[] asQuotedUTF8() { return null; }
        };
        assertTrue(parser.nextFieldName(sstr));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test
    public void testNextTextValue() throws IOException {
        setup("\"hello\"");
        assertEquals("hello", parser.nextTextValue());
    }

    @Test
    public void testNextTextValueFromField() throws IOException {
        setup("{\"key\":\"val\"}");
        parser.nextToken();
        parser.nextToken();
        assertEquals("val", parser.nextTextValue());
    }

    @Test
    public void testNextIntValue() throws IOException {
        setup("123");
        assertEquals(123, parser.nextIntValue(-1));
    }

    @Test
    public void testNextIntValueDefault() throws IOException {
        setup("\"abc\"");
        assertEquals(-1, parser.nextIntValue(-1));
    }

    @Test
    public void testNextLongValue() throws IOException {
        setup("9999999999");
        assertEquals(9999999999L, parser.nextLongValue(-1L));
    }

    @Test
    public void testNextLongValueDefault() throws IOException {
        setup("{}");
        assertEquals(-1L, parser.nextLongValue(-1L));
    }

    @Test
    public void testNextBooleanValueTrue() throws IOException {
        setup("true");
        assertTrue(parser.nextBooleanValue());
    }

    @Test
    public void testNextBooleanValueFalse() throws IOException {
        setup("false");
        assertFalse(parser.nextBooleanValue());
    }

    @Test
    public void testNextBooleanValueNullForOther() throws IOException {
        setup("123");
        assertNull(parser.nextBooleanValue());
    }

    @Test
    public void testGetBinaryValueString() throws IOException {
        setup("\"dGVzdA==\"");
        parser.nextToken();
        byte[] bin = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("test", new String(bin, "ASCII"));
    }

    @Test
    public void testGetBinaryValueNotString() throws IOException {
        setup("123");
        parser.nextToken();
        try {
            parser.getBinaryValue(Base64Variants.getDefaultVariant());
            fail("Expected exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not VALUE_STRING"));
        }
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        setup("\"dGVzdA==\"");
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals("test", new String(out.toByteArray(), "ASCII"));
        assertEquals(4, len);
    }

    @Test
    public void testEscapedString() throws IOException {
        setup("\"a\\n\\t\\\"\\\\\"");
        parser.nextToken();
        assertEquals("a\n\t\"\\", parser.getText());
    }

    @Test
    public void testUnicodeEscape() throws IOException {
        setup("\"\\u0041\"");
        parser.nextToken();
        assertEquals("A", parser.getText());
    }

    @Test
    public void testUnicodeMultiple() throws IOException {
        setup("\"\\u0041\\u0042\"");
        parser.nextToken();
        assertEquals("AB", parser.getText());
    }

    @Test(expected = JsonParseException.class)
    public void testBadUnicodeEscape() throws IOException {
        setup("\"\\u00XY\"");
        parser.nextToken();
        parser.getText();
    }

    @Test
    public void testGetTokenLocation() throws IOException {
        setup("  42");
        parser.nextToken();
        JsonLocation loc = parser.getTokenLocation();
        assertEquals(2, loc.getColumnNr());
        assertEquals(1, loc.getLineNr());
    }

    @Test
    public void testGetTokenLocationFieldName() throws IOException {
        setup("{\"name\":1}");
        parser.nextToken();
        parser.nextToken();
        JsonLocation loc = parser.getTokenLocation();
        assertEquals(2, loc.getColumnNr());
    }

    @Test
    public void testGetCurrentLocation() throws IOException {
        setup("{\"a\":1}");
        parser.nextToken();
        JsonLocation loc = parser.getCurrentLocation();
        assertEquals(1, loc.getLineNr());
        assertTrue(loc.getColumnNr() > 0);
    }

    @Test
    public void testNumberInt() throws IOException {
        setup("123");
        parser.nextToken();
        assertEquals(123, parser.getIntValue());
    }

    @Test
    public void testNumberLong() throws IOException {
        setup("9999999999");
        parser.nextToken();
        assertEquals(9999999999L, parser.getLongValue());
    }

    @Test
    public void testNumberFloat() throws IOException {
        setup("3.25");
        parser.nextToken();
        assertEquals(3.25f, parser.getFloatValue(), 0.0);
    }

    @Test
    public void testNumberDouble() throws IOException {
        setup("2.5e2");
        parser.nextToken();
        assertEquals(250.0, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testNumberBigInteger() throws IOException {
        setup("12345678901234567890");
        parser.nextToken();
        assertEquals(new BigInteger("12345678901234567890"), parser.getBigIntegerValue());
    }

    @Test
    public void testNumberBigDecimal() throws IOException {
        setup("3.14159265358979323846");
        parser.nextToken();
        assertEquals(new BigDecimal("3.14159265358979323846"), parser.getDecimalValue());
    }

    @Test
    public void testNumberLeadingZeroWithFeature() throws IOException {
        setupWithFeatures("07", Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        parser.nextToken();
        assertEquals(7, parser.getIntValue());
    }

    @Test
    public void testParserCloseWithoutOwningReader() throws IOException {
        StringReader r = new StringReader("{}");
        parser = new ReaderBasedJsonParser(SHARED_CTXT, 0, r, null, SHARED_SYMS);
        parser.close();
    }

    @Test
    public void testReleaseBuffersRecyclesBuffer() throws IOException {
        parser = new ReaderBasedJsonParser(SHARED_CTXT, 0, new StringReader("{}"), null, SHARED_SYMS);
        parser.close();
    }

    @Test
    public void testTextFromWriterString() throws IOException {
        setup("\"hello\"");
        parser.nextToken();
        StringWriter w = new StringWriter();
        parser.getText(w);
        assertEquals("hello", w.toString());
    }

    @Test
    public void testTextFromWriterFieldName() throws IOException {
        setup("{\"key\":1}");
        parser.nextToken();
        parser.nextToken();
        StringWriter w = new StringWriter();
        parser.getText(w);
        assertEquals("key", w.toString());
    }

    @Test
    public void testTextFromWriterNumber() throws IOException {
        setup("42");
        parser.nextToken();
        StringWriter w = new StringWriter();
        parser.getText(w);
        assertEquals("42", w.toString());
    }

    @Test
    public void testTextFromWriterTokenNull() throws IOException {
        setup("null");
        parser.nextToken();
        StringWriter w = new StringWriter();
        parser.getText(w);
        assertEquals("null", w.toString());
    }

    @Test
    public void testNumberPosOverflowToFloat() throws IOException {
        setup("1e309");
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
    }

    @Test
    public void testNumberNegOverflow() throws IOException {
        setup("-1e309");
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
    }

    @Test
    public void testEmptyObjectAfterField() throws IOException {
        setup("{\"a\":{}}");
        parser.nextToken();
        parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testSkipChildren() throws IOException {
        setup("{\"a\":[1,2]}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testHasTextCharacters() throws IOException {
        setup("\"abc\"");
        parser.nextToken();
        assertTrue(parser.hasTextCharacters());
    }

    @Test
    public void testIsClosed() throws IOException {
        setup("{}");
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testVersion() throws IOException {
        setup("{}");
        assertNotNull(parser.version());
    }

    @Test
    public void testCanReadObjectId() throws IOException {
        setup("{}");
        assertFalse(parser.canReadObjectId());
    }

    @Test
    public void testCanReadTypeId() throws IOException {
        setup("{}");
        assertFalse(parser.canReadTypeId());
    }

    @Test
    public void testEmbeddedObject() throws IOException {
        setup("{}");
        assertNull(parser.getEmbeddedObject());
    }

    @Test
    public void testCurrentToken() throws IOException {
        setup("123");
        assertNull(parser.currentToken());
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.currentToken());
    }

    @Test
    public void testCurrentTokenId() throws IOException {
        setup("null");
        parser.nextToken();
        assertEquals(JsonTokenId.ID_NULL, parser.currentTokenId());
    }

    @Test
    public void testIsExpectedStartArrayToken() throws IOException {
        setup("[]");
        assertTrue(parser.isExpectedStartArrayToken());
    }

    @Test
    public void testIsExpectedStartObjectToken() throws IOException {
        setup("{}");
        assertTrue(parser.isExpectedStartObjectToken());
    }

    @Test
    public void testClearCurrentToken() throws IOException {
        setup("123");
        parser.nextToken();
        parser.clearCurrentToken();
        assertNull(parser.currentToken());
    }

    @Test
    public void testGetLastClearedToken() throws IOException {
        setup("123");
        parser.nextToken();
        parser.clearCurrentToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getLastClearedToken());
    }

    @Test
    public void testOverrideCurrentName() throws IOException {
        setup("{\"a\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals("a", parser.getCurrentName());
        parser.overrideCurrentName("b");
        assertEquals("b", parser.getCurrentName());
    }

    @Test
    public void testReadValueAsTree() throws IOException {
        setup("{}");
        assertNull(parser.getCodec());
        try {
            parser.readValueAsTree();
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEOFAfterWhitespace() throws IOException {
        setup("   ");
        try {
            parser.nextToken();
            fail("Expected exception");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testStringWithEscapedSlash() throws IOException {
        setup("\"a\\/b\"");
        parser.nextToken();
        assertEquals("a/b", parser.getText());
    }

    @Test
    public void testStringWithEscapedBackslash() throws IOException {
        setup("\"a\\\\b\"");
        parser.nextToken();
        assertEquals("a\\b", parser.getText());
    }

    @Test
    public void testNameWithEscape() throws IOException {
        setup("{\"a\\u0062c\":1}");
        parser.nextToken();
        parser.nextToken();
        assertEquals("abc", parser.getCurrentName());
    }

    @Test
    public void testUnexpectedCharInName() throws IOException {
        setup("{\"a\\tb\":1}");
        parser.nextToken();
        assertEquals("a\tb", parser.nextToken());
    }

    @Test
    public void testSkipChildrenOverStruct() throws IOException {
        setup("[1,{\"a\":2},3]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.skipChildren();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }
}
