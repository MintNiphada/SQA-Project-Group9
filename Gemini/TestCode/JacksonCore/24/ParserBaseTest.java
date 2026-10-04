package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserBaseTest {

    private static class TestParserBase extends ParserBase {
        private String _text;
        private boolean _closedInputCalled = false;
        private char _escapedChar = 'n';

        public TestParserBase(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        @Override
        protected void _closeInput() throws IOException {
            _closedInputCalled = true;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            return _currToken;
        }

        @Override
        public String getText() throws IOException {
            if (_text != null) {
                return _text;
            }
            return _textBuffer.contentsAsString();
        }

        public void setText(String text) {
            this._text = text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _textBuffer.getTextBuffer();
        }

        @Override
        public int getTextLength() throws IOException {
            return _textBuffer.size();
        }

        @Override
        public int getTextOffset() throws IOException {
            return _textBuffer.getTextOffset();
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public void setCodec(ObjectCodec c) {
        }

        public void setCurrToken(JsonToken token) {
            this._currToken = token;
        }

        public void setParsingContext(JsonReadContext ctxt) {
            this._parsingContext = ctxt;
        }

        public void setNameCopied(boolean copied) {
            this._nameCopied = copied;
        }

        public void setNameCopyBuffer(char[] buf) {
            this._nameCopyBuffer = buf;
        }

        public void setNumberInt(int val) {
            this._numberInt = val;
        }

        public void setNumberLong(long val) {
            this._numberLong = val;
        }

        public void setNumberDouble(double val) {
            this._numberDouble = val;
        }

        public void setNumberBigInt(BigInteger val) {
            this._numberBigInt = val;
        }

        public void setNumberBigDecimal(BigDecimal val) {
            this._numberBigDecimal = val;
        }

        public void setNumTypesValid(int mask) {
            this._numTypesValid = mask;
        }

        public int getNumTypesValid() {
            return this._numTypesValid;
        }

        public void setEscapedChar(char ch) {
            this._escapedChar = ch;
        }

        @Override
        protected char _decodeEscaped() throws IOException {
            return _escapedChar;
        }
    }

    private IOContext ioContext;
    private TestParserBase parser;

    @Before
    public void setUp() {
        ioContext = new IOContext(new BufferRecycler(), "sourceRef", false);
        parser = new TestParserBase(ioContext, 0);
    }

    @Test
    public void testVersionAndCurrentValue() {
        Assert.assertNotNull(parser.version());
        Assert.assertNull(parser.getCurrentValue());

        Object testObj = new Object();
        parser.setCurrentValue(testObj);
        Assert.assertSame(testObj, parser.getCurrentValue());
    }

    @Test
    public void testFeatureHandling() {
        Assert.assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNull(parser.getParsingContext().getDupDetector());

        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNotNull(parser.getParsingContext().getDupDetector());

        // Enable again should keep dup detector
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNotNull(parser.getParsingContext().getDupDetector());

        parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNull(parser.getParsingContext().getDupDetector());

        // test setFeatureMask
        int strictMask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        parser.setFeatureMask(strictMask);
        Assert.assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNotNull(parser.getParsingContext().getDupDetector());

        parser.setFeatureMask(0);
        Assert.assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));

        // test overrideStdFeatures
        parser.overrideStdFeatures(strictMask, strictMask);
        Assert.assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        parser.overrideStdFeatures(0, strictMask);
        Assert.assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
    }

    @Test
    public void testCheckStdFeatureChanges() {
        int dupMask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        
        // Enabling dup detector
        parser._checkStdFeatureChanges(dupMask, dupMask);
        Assert.assertNotNull(parser.getParsingContext().getDupDetector());

        // Disabling dup detector
        parser._checkStdFeatureChanges(0, dupMask);
        Assert.assertNull(parser.getParsingContext().getDupDetector());

        // No changes
        parser._checkStdFeatureChanges(0, 0);
        Assert.assertNull(parser.getParsingContext().getDupDetector());
    }

    @Test
    public void testStrictDuplicateDetectionInConstructor() {
        int features = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParserBase dupParser = new TestParserBase(ioContext, features);
        Assert.assertNotNull(dupParser.getParsingContext().getDupDetector());
    }

    @Test
    public void testGetCurrentNameAndOverrideCurrentName() throws IOException {
        JsonReadContext root = parser.getParsingContext();
        JsonReadContext child = root.createChildObjectContext(1, 1);
        child.setCurrentName("childField");
        parser.setParsingContext(child);

        parser.setCurrToken(JsonToken.START_OBJECT);
        // Under START_OBJECT, should query parent context's name
        Assert.assertNull(parser.getCurrentName());

        root.setCurrentName("parentField");
        Assert.assertEquals("parentField", parser.getCurrentName());

        // Under regular field or value token
        parser.setCurrToken(JsonToken.VALUE_STRING);
        Assert.assertEquals("childField", parser.getCurrentName());

        // Override current name for START_OBJECT (targets parent)
        parser.setCurrToken(JsonToken.START_OBJECT);
        parser.overrideCurrentName("overriddenParent");
        Assert.assertEquals("overriddenParent", root.getCurrentName());

        // Override current name for VALUE_STRING (targets child)
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.overrideCurrentName("overriddenChild");
        Assert.assertEquals("overriddenChild", child.getCurrentName());

        // Test START_ARRAY
        parser.setCurrToken(JsonToken.START_ARRAY);
        Assert.assertEquals("overriddenParent", parser.getCurrentName());
    }

    @Test
    public void testCloseAndReleaseBuffers() throws IOException {
        parser.setNameCopyBuffer(new char[10]);
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        Assert.assertTrue(parser._closedInputCalled);

        // Calling close again should be a no-op
        parser._closedInputCalled = false;
        parser.close();
        Assert.assertFalse(parser._closedInputCalled);
    }

    @Test
    public void testLocationMethods() {
        parser._currInputRow = 5;
        parser._currInputRowStart = 10;
        parser._inputPtr = 15;
        parser._currInputProcessed = 100;
        parser._tokenInputTotal = 80;
        parser._tokenInputRow = 4;
        parser._tokenInputCol = 2; // 0-based, converted to 3

        JsonLocation currLoc = parser.getCurrentLocation();
        Assert.assertEquals(5, currLoc.getLineNr());
        Assert.assertEquals(6, currLoc.getColumnNr()); // 15 - 10 + 1 = 6
        Assert.assertEquals(115, currLoc.getCharOffset());

        JsonLocation tokenLoc = parser.getTokenLocation();
        Assert.assertEquals(4, tokenLoc.getLineNr());
        Assert.assertEquals(3, tokenLoc.getColumnNr());
        Assert.assertEquals(80, tokenLoc.getCharOffset());

        Assert.assertEquals(80L, parser.getTokenCharacterOffset());
        Assert.assertEquals(4, parser.getTokenLineNr());
        Assert.assertEquals(3, parser.getTokenColumnNr());

        parser._tokenInputCol = -1;
        Assert.assertEquals(-1, parser.getTokenColumnNr());
    }

    @Test
    public void testSourceReferenceInLocation() {
        Assert.assertNull(parser._getSourceReference());
        parser.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        Assert.assertEquals("sourceRef", parser._getSourceReference());
    }

    @Test
    public void testHasTextCharacters() {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        Assert.assertTrue(parser.hasTextCharacters());

        parser.setCurrToken(JsonToken.FIELD_NAME);
        parser.setNameCopied(false);
        Assert.assertFalse(parser.hasTextCharacters());
        parser.setNameCopied(true);
        Assert.assertTrue(parser.hasTextCharacters());

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        Assert.assertFalse(parser.hasTextCharacters());
    }

    @Test
    public void testBinaryValue() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser._textBuffer.resetWithShared(new char[]{'A', 'B', 'C', 'D'}, 0, 4);

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertNotNull(binary);
        Assert.assertEquals(3, binary.length);

        // Cached binary test
        byte[] binary2 = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertSame(binary, binary2);

        // Invalid token for binary
        TestParserBase nonStrParser = new TestParserBase(ioContext, 0);
        nonStrParser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        try {
            nonStrParser.getBinaryValue(Base64Variants.MIME);
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("not VALUE_STRING"));
        }
    }

    @Test
    public void testByteArrayBuilderReuse() {
        ByteArrayBuilder b1 = parser._getByteArrayBuilder();
        Assert.assertNotNull(b1);
        ByteArrayBuilder b2 = parser._getByteArrayBuilder();
        Assert.assertSame(b1, b2);
    }

    @Test
    public void testResetAndNaN() {
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.reset(false, 5, 0, 0));
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.reset(true, 5, 2, 1));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.resetAsNaN("NaN", Double.NaN));
        Assert.assertTrue(parser.isNaN());

        parser.resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
        Assert.assertTrue(parser.isNaN());

        parser.resetAsNaN("123.45", 123.45);
        Assert.assertFalse(parser.isNaN());

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        Assert.assertFalse(parser.isNaN());
    }

    @Test
    public void testParseNumericIntFastPaths() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // <= 9 digits int
        parser._textBuffer.resetWithString("12345");
        parser.resetInt(false, 5);
        Assert.assertEquals(12345, parser.getIntValue());
        Assert.assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        Assert.assertEquals(12345, parser.getNumberValue());

        // Negative <= 9 digits int
        parser._textBuffer.resetWithString("-9876");
        parser.resetInt(true, 4);
        Assert.assertEquals(-9876, parser.getIntValue());

        // 10 digits int within range (positive)
        parser._textBuffer.resetWithString("2147483647");
        parser.resetInt(false, 10);
        Assert.assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        Assert.assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        // 10 digits int within range (negative)
        parser._textBuffer.resetWithString("-2147483648");
        parser.resetInt(true, 10);
        Assert.assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        Assert.assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        // 10 digits exceeding int max (positive)
        parser._textBuffer.resetWithString("2147483648");
        parser.resetInt(false, 10);
        Assert.assertEquals(2147483648L, parser.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        // 10 digits exceeding int min (negative)
        parser._textBuffer.resetWithString("-2147483649");
        parser.resetInt(true, 10);
        Assert.assertEquals(-2147483649L, parser.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        // 11-18 digits long
        parser._textBuffer.resetWithString("123456789012345");
        parser.resetInt(false, 15);
        Assert.assertEquals(123456789012345L, parser.getLongValue());
        Assert.assertEquals(123456789012345L, parser.getNumberValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
    }

    @Test
    public void testParseNumericIntSlowPaths() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // 19 digits within Long range
        String maxLong = String.valueOf(Long.MAX_VALUE);
        parser._textBuffer.resetWithString(maxLong);
        parser.resetInt(false, maxLong.length());
        Assert.assertEquals(Long.MAX_VALUE, parser.getLongValue());
        Assert.assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        // Negative 19 digits within Long range
        String minLong = String.valueOf(Long.MIN_VALUE);
        parser._textBuffer.resetWithString(minLong);
        parser.resetInt(true, minLong.length() - 1);
        Assert.assertEquals(Long.MIN_VALUE, parser.getLongValue());

        // Beyond Long range -> BigInteger
        String bigIntStr = "123456789012345678901234567890";
        parser._textBuffer.resetWithString(bigIntStr);
        parser.resetInt(false, bigIntStr.length());
        Assert.assertEquals(new BigInteger(bigIntStr), parser.getBigIntegerValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());
        Assert.assertEquals(new BigInteger(bigIntStr), parser.getNumberValue());

        // Exceeding long range when requested as INT/LONG should fail
        TestParserBase overflowParser = new TestParserBase(ioContext, 0);
        overflowParser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        overflowParser._textBuffer.resetWithString(bigIntStr);
        overflowParser.resetInt(false, bigIntStr.length());
        try {
            overflowParser.getIntValue();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("out of range"));
        }
    }

    @Test
    public void testParseNumericFloatPaths() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);

        parser._textBuffer.resetWithString("123.456");
        parser.resetFloat(false, 3, 3, 0);
        Assert.assertEquals(123.456, parser.getDoubleValue(), 0.00001);
        Assert.assertEquals(123.456f, parser.getFloatValue(), 0.00001f);
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        Assert.assertEquals(123.456, ((Double) parser.getNumberValue()).doubleValue(), 0.00001);

        // Request as BigDecimal directly
        TestParserBase bdParser = new TestParserBase(ioContext, 0);
        bdParser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        bdParser._textBuffer.resetWithString("987.654321");
        bdParser.resetFloat(false, 3, 6, 0);
        Assert.assertEquals(new BigDecimal("987.654321"), bdParser.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.BIG_DECIMAL, bdParser.getNumberType());
        Assert.assertEquals(new BigDecimal("987.654321"), bdParser.getNumberValue());
    }

    @Test
    public void testParseNumericOnNonNumericToken() {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        try {
            parser.getIntValue();
            Assert.fail("Expected JsonParseException");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("not numeric"));
        }
    }

    @Test
    public void testConvertNumberToInt() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // From Long: valid
        parser.setNumberLong(500L);
        parser.setNumTypesValid(ParserMinimalBase.NR_LONG);
        Assert.assertEquals(500, parser.getIntValue());

        // From Long: overflow
        parser.setNumberLong(Long.MAX_VALUE);
        parser.setNumTypesValid(ParserMinimalBase.NR_LONG);
        try {
            parser.convertNumberToInt();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("out of range of int"));
        }

        // From BigInt: valid
        parser.setNumberBigInt(BigInteger.valueOf(100));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(100, parser.getIntValue());

        // From BigInt: overflow
        parser.setNumberBigInt(BigInteger.valueOf(Long.MAX_VALUE));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        try {
            parser.convertNumberToInt();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }

        // From Double: valid
        parser.setNumberDouble(42.5);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(42, parser.getIntValue());

        // From Double: overflow
        parser.setNumberDouble(1e12);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        try {
            parser.convertNumberToInt();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }

        // From BigDecimal: valid
        parser.setNumberBigDecimal(new BigDecimal("99.9"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        Assert.assertEquals(99, parser.getIntValue());

        // From BigDecimal: overflow
        parser.setNumberBigDecimal(new BigDecimal("1e15"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        try {
            parser.convertNumberToInt();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }
    }

    @Test
    public void testConvertNumberToLong() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // From Int
        parser.setNumberInt(1234);
        parser.setNumTypesValid(ParserMinimalBase.NR_INT);
        Assert.assertEquals(1234L, parser.getLongValue());

        // From BigInt: valid
        parser.setNumberBigInt(BigInteger.valueOf(9876543210L));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(9876543210L, parser.getLongValue());

        // From BigInt: overflow
        parser.setNumberBigInt(new BigInteger("123456789012345678901234567890"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        try {
            parser.convertNumberToLong();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }

        // From Double: valid
        parser.setNumberDouble(100.0);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(100L, parser.getLongValue());

        // From Double: overflow
        parser.setNumberDouble(1e25);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        try {
            parser.convertNumberToLong();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }

        // From BigDecimal: valid
        parser.setNumberBigDecimal(new BigDecimal("123456789.5"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        Assert.assertEquals(123456789L, parser.getLongValue());

        // From BigDecimal: overflow
        parser.setNumberBigDecimal(new BigDecimal("1e25"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        try {
            parser.convertNumberToLong();
            Assert.fail("Expected overflow exception");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Overflow"));
        }
    }

    @Test
    public void testConvertNumberToBigInteger() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // From Int
        parser.setNumberInt(55);
        parser.setNumTypesValid(ParserMinimalBase.NR_INT);
        Assert.assertEquals(BigInteger.valueOf(55), parser.getBigIntegerValue());

        // From Long
        parser.setNumberLong(5555555555L);
        parser.setNumTypesValid(ParserMinimalBase.NR_LONG);
        Assert.assertEquals(BigInteger.valueOf(5555555555L), parser.getBigIntegerValue());

        // From Double
        parser.setNumberDouble(12.34);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(BigInteger.valueOf(12), parser.getBigIntegerValue());

        // From BigDecimal
        parser.setNumberBigDecimal(new BigDecimal("987654321.123"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        Assert.assertEquals(new BigInteger("987654321"), parser.getBigIntegerValue());
    }

    @Test
    public void testConvertNumberToDouble() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // From Int
        parser.setNumberInt(77);
        parser.setNumTypesValid(ParserMinimalBase.NR_INT);
        Assert.assertEquals(77.0, parser.getDoubleValue(), 0.001);

        // From Long
        parser.setNumberLong(7777777L);
        parser.setNumTypesValid(ParserMinimalBase.NR_LONG);
        Assert.assertEquals(7777777.0, parser.getDoubleValue(), 0.001);

        // From BigInt
        parser.setNumberBigInt(BigInteger.valueOf(88888888L));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(88888888.0, parser.getDoubleValue(), 0.001);

        // From BigDecimal
        parser.setNumberBigDecimal(new BigDecimal("99.99"));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGDECIMAL);
        Assert.assertEquals(99.99, parser.getDoubleValue(), 0.001);
    }

    @Test
    public void testConvertNumberToBigDecimal() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);

        // From Int
        parser.setNumberInt(11);
        parser.setNumTypesValid(ParserMinimalBase.NR_INT);
        Assert.assertEquals(BigDecimal.valueOf(11), parser.getDecimalValue());

        // From Long
        parser.setNumberLong(222222L);
        parser.setNumTypesValid(ParserMinimalBase.NR_LONG);
        Assert.assertEquals(BigDecimal.valueOf(222222L), parser.getDecimalValue());

        // From BigInt
        parser.setNumberBigInt(BigInteger.valueOf(333333333L));
        parser.setNumTypesValid(ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(new BigDecimal(BigInteger.valueOf(333333333L)), parser.getDecimalValue());

        // From Double
        parser._textBuffer.resetWithString("123.456");
        parser.setNumberDouble(123.456);
        parser.setNumTypesValid(ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(new BigDecimal("123.456"), parser.getDecimalValue());
    }

    @Test
    public void testEOFHandling() {
        // In root context: valid EOF
        try {
            Assert.assertEquals(-1, parser._eofAsNextChar());
        } catch (JsonParseException e) {
            Assert.fail("Root context EOF should be valid");
        }

        // In array context: invalid EOF
        JsonReadContext arrContext = parser.getParsingContext().createChildArrayContext(1, 1);
        parser.setParsingContext(arrContext);
        try {
            parser._eofAsNextChar();
            Assert.fail("Expected JsonParseException in open array");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("expected close marker for Array"));
        }

        // In object context: invalid EOF
        JsonReadContext objContext = parser.getParsingContext().getParent().createChildObjectContext(1, 1);
        parser.setParsingContext(objContext);
        try {
            parser._eofAsNextChar();
            Assert.fail("Expected JsonParseException in open object");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("expected close marker for Object"));
        }
    }

    @Test
    public void testErrorReportingMethods() {
        // Mismatched end marker
        try {
            parser._reportMismatchedEndMarker(']', '}');
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Unexpected close marker ']'"));
        }

        // Unrecognized character escape
        try {
            parser._handleUnrecognizedCharacterEscape('z');
            Assert.fail("Expected JsonProcessingException");
        } catch (JsonProcessingException expected) {
            Assert.assertTrue(expected.getMessage().contains("Unrecognized character escape"));
        }

        // Backslash escaping any char enabled
        parser.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        try {
            Assert.assertEquals('z', parser._handleUnrecognizedCharacterEscape('z'));
        } catch (JsonProcessingException e) {
            Assert.fail("Escape should be allowed");
        }
        parser.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);

        // Single quote allowed
        parser.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        try {
            Assert.assertEquals('\'', parser._handleUnrecognizedCharacterEscape('\''));
        } catch (JsonProcessingException e) {
            Assert.fail("Single quote escape should be allowed");
        }

        // Unquoted space reporting
        try {
            parser._throwUnquotedSpace(10, "string value");
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Illegal unquoted character"));
        }

        parser.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        try {
            parser._throwUnquotedSpace(10, "string value"); // 10 <= INT_SPACE (32), should not throw
        } catch (JsonParseException e) {
            Assert.fail("Unquoted control chars allowed");
        }
    }

    @Test
    public void testBase64EscapeDecoding() throws IOException {
        Base64Variant b64 = Base64Variants.MIME;

        // Valid escaped char
        parser.setEscapedChar('A'); // 'A' decoded in MIME = 0
        int decoded = parser._decodeBase64Escape(b64, '\\', 0);
        Assert.assertEquals(0, decoded);

        // Valid escaped char as char overload
        decoded = parser._decodeBase64Escape(b64, '\\', 0);
        Assert.assertEquals(0, decoded);

        // Escape space at start (index 0) returns -1 to skip
        parser.setEscapedChar(' ');
        decoded = parser._decodeBase64Escape(b64, '\\', 0);
        Assert.assertEquals(-1, decoded);

        // Non-backslash character passed
        try {
            parser._decodeBase64Escape(b64, 'X', 0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assert.assertTrue(expected.getMessage().contains("Illegal character 'X'"));
        }

        // Base64 padding at invalid index 0
        parser.setEscapedChar('=');
        try {
            parser._decodeBase64Escape(b64, '\\', 0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assert.assertTrue(expected.getMessage().contains("Unexpected padding"));
        }

        // Base64 padding at valid index 2
        decoded = parser._decodeBase64Escape(b64, '\\', 2);
        Assert.assertEquals(Base64Variant.BASE64_VALUE_PADDING, decoded);
    }

    @Test
    public void testReportInvalidBase64Char() {
        Base64Variant b64 = Base64Variants.MIME;

        IllegalArgumentException ex1 = parser.reportInvalidBase64Char(b64, ' ', 0);
        Assert.assertTrue(ex1.getMessage().contains("Illegal white space character"));

        IllegalArgumentException ex2 = parser.reportInvalidBase64Char(b64, '=', 0);
        Assert.assertTrue(ex2.getMessage().contains("Unexpected padding character"));

        IllegalArgumentException ex3 = parser.reportInvalidBase64Char(b64, 0x01, 0);
        Assert.assertTrue(ex3.getMessage().contains("Illegal character (code 0x1)"));

        IllegalArgumentException ex4 = parser.reportInvalidBase64Char(b64, '$', 0, "extra message");
        Assert.assertTrue(ex4.getMessage().contains("extra message"));
    }

    @Test
    public void testHandleBase64MissingPadding() {
        try {
            parser._handleBase64MissingPadding(Base64Variants.MIME);
            Assert.fail("Expected JsonParseException");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("padding"));
        }
    }

    @Test
    public void testGrowArrayBy() {
        int[] result = ParserBase.growArrayBy(null, 5);
        Assert.assertNotNull(result);
        Assert.assertEquals(5, result.length);

        int[] original = new int[]{1, 2, 3};
        result = ParserBase.growArrayBy(original, 2);
        Assert.assertEquals(5, result.length);
        Assert.assertEquals(1, result[0]);
        Assert.assertEquals(2, result[1]);
        Assert.assertEquals(3, result[2]);
        Assert.assertEquals(0, result[3]);
    }

    @Test
    public void testDeprecatedMethods() throws IOException {
        Assert.assertFalse(parser.loadMore());
        parser._finishString();
        try {
            parser.loadMoreGuaranteed();
            Assert.fail("Expected JsonParseException on loadMoreGuaranteed failing");
        } catch (JsonParseException expected) {
            Assert.assertTrue(expected.getMessage().contains("Unexpected end-of-input"));
        }
    }
}
