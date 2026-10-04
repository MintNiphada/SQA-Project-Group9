package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

public class ParserBaseTest {

    static class TestParser extends ParserBase {
        public String lastError;
        public boolean throwOnError = true;
        public int closeInputCalled;
        public int releaseBuffersCalled;
        public boolean loadMoreReturn;
        public int decodeEscapedReturn = 'A';

        public TestParser(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        @Override
        protected void _closeInput() throws IOException {
            closeInputCalled++;
        }

        @Override
        protected void _releaseBuffers() throws IOException {
            releaseBuffersCalled++;
            super._releaseBuffers();
        }

        @Override
        protected void _reportError(String msg) throws JsonParseException {
            lastError = msg;
            if (throwOnError) {
                super._reportError(msg);
            }
        }

        @Override
        protected void _reportInvalidEOF() throws JsonParseException {
            lastError = "EOF";
            if (throwOnError) {
                super._reportInvalidEOF();
            }
        }

        @Override
        protected void _reportInvalidEOF(String msg) throws JsonParseException {
            lastError = msg;
            if (throwOnError) {
                super._reportInvalidEOF(msg);
            }
        }

        @Override
        protected void _reportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
            lastError = "mismatch";
            if (throwOnError) {
                super._reportMismatchedEndMarker(actCh, expCh);
            }
        }

        @Override
        protected char _decodeEscaped() throws IOException {
            return (char) decodeEscapedReturn;
        }

        @Override
        public String getText() throws IOException {
            return _textBuffer.contentsAsString();
        }
    }

    private TestParser parser;
    private IOContext ioContext;
    private TextBuffer textBuffer;

    @Before
    public void setUp() throws Exception {
        BufferRecycler recycler = new BufferRecycler();
        textBuffer = new TextBuffer(recycler);
        ioContext = new IOContext(recycler, null, false) {
            @Override
            public TextBuffer constructTextBuffer() {
                return textBuffer;
            }
            @Override
            public Object getSourceReference() {
                return "source";
            }
        };
        parser = new TestParser(ioContext, 0);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = ParserBase.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = ParserBase.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void testConstructorDefault() throws Exception {
        Assert.assertNotNull(parser._ioContext);
        Assert.assertNotNull(parser._textBuffer);
        Assert.assertNotNull(parser._parsingContext);
        Assert.assertFalse(parser._closed);
        Assert.assertEquals(0, parser._features);
    }

    @Test
    public void testConstructorWithStrictDuplicateDetection() throws Exception {
        TestParser p = new TestParser(ioContext, JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(p._parsingContext.getDupDetector());
    }

    @Test
    public void testVersion() {
        Assert.assertNotNull(parser.version());
    }

    @Test
    public void testGetCurrentValue() throws Exception {
        parser._parsingContext.setCurrentValue("val");
        Assert.assertEquals("val", parser.getCurrentValue());
    }

    @Test
    public void testSetCurrentValue() throws Exception {
        parser.setCurrentValue("newVal");
        Assert.assertEquals("newVal", parser._parsingContext.getCurrentValue());
    }

    @Test
    public void testEnableFeatureStrictDuplicateDetection() throws Exception {
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNotNull(parser._parsingContext.getDupDetector());
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNotNull(parser._parsingContext.getDupDetector());
    }

    @Test
    public void testDisableFeatureStrictDuplicateDetection() throws Exception {
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNull(parser._parsingContext.getDupDetector());
        parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNull(parser._parsingContext.getDupDetector());
    }

    @Test
    public void testSetFeatureMask() throws Exception {
        parser.setFeatureMask(JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(parser._parsingContext.getDupDetector());
        parser.setFeatureMask(0);
        Assert.assertNull(parser._parsingContext.getDupDetector());
    }

    @Test
    public void testOverrideStdFeatures() throws Exception {
        parser.overrideStdFeatures(JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask(),
                JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(parser._parsingContext.getDupDetector());
        parser.overrideStdFeatures(0, JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNull(parser._parsingContext.getDupDetector());
    }

    @Test
    public void testCheckStdFeatureChanges() throws Exception {
        parser._checkStdFeatureChanges(JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask(), 0);
        Assert.assertNull(parser._parsingContext.getDupDetector());
        parser._checkStdFeatureChanges(0, JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNull(parser._parsingContext.getDupDetector());
        parser._checkStdFeatureChanges(JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask(),
                JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(parser._parsingContext.getDupDetector());
    }

    @Test
    public void testGetCurrentNameFieldName() throws Exception {
        setField(parser, "_currToken", JsonToken.FIELD_NAME);
        parser._parsingContext.setCurrentName("field");
        Assert.assertEquals("field", parser.getCurrentName());
    }

    @Test
    public void testGetCurrentNameStartObjectWithParent() throws Exception {
        setField(parser, "_currToken", JsonToken.START_OBJECT);
        JsonReadContext parent = JsonReadContext.createRootContext(null);
        parent.setCurrentName("parentName");
        parser._parsingContext = parser._parsingContext.withParent(parent);
        Assert.assertEquals("parentName", parser.getCurrentName());
    }

    @Test
    public void testGetCurrentNameStartArrayWithParent() throws Exception {
        setField(parser, "_currToken", JsonToken.START_ARRAY);
        JsonReadContext parent = JsonReadContext.createRootContext(null);
        parent.setCurrentName("parentName");
        parser._parsingContext = parser._parsingContext.withParent(parent);
        Assert.assertEquals("parentName", parser.getCurrentName());
    }

    @Test
    public void testGetCurrentNameStartObjectNoParent() throws Exception {
        setField(parser, "_currToken", JsonToken.START_OBJECT);
        Assert.assertNull(parser.getCurrentName());
    }

    @Test
    public void testOverrideCurrentNameFieldName() throws Exception {
        setField(parser, "_currToken", JsonToken.FIELD_NAME);
        parser.overrideCurrentName("newName");
        Assert.assertEquals("newName", parser._parsingContext.getCurrentName());
    }

    @Test
    public void testOverrideCurrentNameStartObject() throws Exception {
        setField(parser, "_currToken", JsonToken.START_OBJECT);
        JsonReadContext parent = JsonReadContext.createRootContext(null);
        parser._parsingContext = parser._parsingContext.withParent(parent);
        parser.overrideCurrentName("newName");
        Assert.assertEquals("newName", parent.getCurrentName());
    }

    @Test
    public void testClose() throws Exception {
        parser.close();
        Assert.assertTrue(parser._closed);
        Assert.assertEquals(1, parser.closeInputCalled);
        Assert.assertEquals(1, parser.releaseBuffersCalled);
        Assert.assertEquals(parser._inputEnd, parser._inputPtr);
    }

    @Test
    public void testIsClosed() throws Exception {
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testGetParsingContext() {
        Assert.assertSame(parser._parsingContext, parser.getParsingContext());
    }

    @Test
    public void testGetTokenLocation() throws Exception {
        setField(parser, "_tokenInputTotal", 100L);
        setField(parser, "_tokenInputRow", 2);
        setField(parser, "_tokenInputCol", 5);
        JsonLocation loc = parser.getTokenLocation();
        Assert.assertEquals(100L, loc.getCharOffset());
        Assert.assertEquals(2, loc.getLineNr());
        Assert.assertEquals(6, loc.getColumnNr());
    }

    @Test
    public void testGetCurrentLocation() throws Exception {
        setField(parser, "_inputPtr", 10);
        setField(parser, "_currInputRowStart", 5);
        setField(parser, "_currInputProcessed", 200L);
        setField(parser, "_currInputRow", 3);
        JsonLocation loc = parser.getCurrentLocation();
        Assert.assertEquals(210L, loc.getCharOffset());
        Assert.assertEquals(3, loc.getLineNr());
        Assert.assertEquals(6, loc.getColumnNr());
    }

    @Test
    public void testHasTextCharactersValueString() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_STRING);
        Assert.assertTrue(parser.hasTextCharacters());
    }

    @Test
    public void testHasTextCharactersFieldNameCopied() throws Exception {
        setField(parser, "_currToken", JsonToken.FIELD_NAME);
        setField(parser, "_nameCopied", true);
        Assert.assertTrue(parser.hasTextCharacters());
    }

    @Test
    public void testHasTextCharactersFieldNameNotCopied() throws Exception {
        setField(parser, "_currToken", JsonToken.FIELD_NAME);
        setField(parser, "_nameCopied", false);
        Assert.assertFalse(parser.hasTextCharacters());
    }

    @Test
    public void testHasTextCharactersOtherToken() throws Exception {
        setField(parser, "_currToken", JsonToken.START_OBJECT);
        Assert.assertFalse(parser.hasTextCharacters());
    }

    @Test
    public void testGetBinaryValueFirstCall() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_STRING);
        textBuffer.resetWithString("dGVzdA==");
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        byte[] result = parser.getBinaryValue(b64);
        Assert.assertNotNull(result);
        Assert.assertArrayEquals("test".getBytes("UTF-8"), result);
        Assert.assertNotNull(parser._binaryValue);
    }

    @Test
    public void testGetBinaryValueCached() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_STRING);
        byte[] cached = new byte[] { 1, 2, 3 };
        setField(parser, "_binaryValue", cached);
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        byte[] result = parser.getBinaryValue(b64);
        Assert.assertSame(cached, result);
    }

    @Test(expected = JsonParseException.class)
    public void testGetBinaryValueWrongToken() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    @Test
    public void testGetTokenCharacterOffset() throws Exception {
        setField(parser, "_tokenInputTotal", 123L);
        Assert.assertEquals(123L, parser.getTokenCharacterOffset());
    }

    @Test
    public void testGetTokenLineNr() throws Exception {
        setField(parser, "_tokenInputRow", 7);
        Assert.assertEquals(7, parser.getTokenLineNr());
    }

    @Test
    public void testGetTokenColumnNrPositive() throws Exception {
        setField(parser, "_tokenInputCol", 3);
        Assert.assertEquals(4, parser.getTokenColumnNr());
    }

    @Test
    public void testGetTokenColumnNrNegative() throws Exception {
        setField(parser, "_tokenInputCol", -1);
        Assert.assertEquals(-1, parser.getTokenColumnNr());
    }

    @Test
    public void testHandleEOFInRoot() throws Exception {
        parser._parsingContext = JsonReadContext.createRootContext(null);
        parser._handleEOF();
    }

    @Test(expected = JsonParseException.class)
    public void testHandleEOFInArray() throws Exception {
        parser._parsingContext = JsonReadContext.createRootContext(null).createChildArrayContext();
        parser._handleEOF();
    }

    @Test(expected = JsonParseException.class)
    public void testHandleEOFInObject() throws Exception {
        parser._parsingContext = JsonReadContext.createRootContext(null).createChildObjectContext();
        parser._handleEOF();
    }

    @Test
    public void testEofAsNextChar() throws Exception {
        parser._parsingContext = JsonReadContext.createRootContext(null);
        Assert.assertEquals(-1, parser._eofAsNextChar());
    }

    @Test
    public void testGetByteArrayBuilderFirstCall() {
        Assert.assertNull(parser._byteArrayBuilder);
        Assert.assertNotNull(parser._getByteArrayBuilder());
        Assert.assertNotNull(parser._byteArrayBuilder);
    }

    @Test
    public void testGetByteArrayBuilderReset() {
        parser._getByteArrayBuilder();
        parser._byteArrayBuilder.append(1);
        parser._getByteArrayBuilder();
        Assert.assertEquals(0, parser._byteArrayBuilder.size());
    }

    @Test
    public void testResetInt() throws Exception {
        JsonToken token = parser.reset(false, 5, 0, 0);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        Assert.assertFalse(parser._numberNegative);
        Assert.assertEquals(5, parser._intLength);
        Assert.assertEquals(0, parser._fractLength);
        Assert.assertEquals(0, parser._expLength);
        Assert.assertEquals(ParserMinimalBase.NR_UNKNOWN, parser._numTypesValid);
    }

    @Test
    public void testResetFloat() throws Exception {
        JsonToken token = parser.reset(true, 3, 2, 1);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, token);
        Assert.assertTrue(parser._numberNegative);
        Assert.assertEquals(3, parser._intLength);
        Assert.assertEquals(2, parser._fractLength);
        Assert.assertEquals(1, parser._expLength);
        Assert.assertEquals(ParserMinimalBase.NR_UNKNOWN, parser._numTypesValid);
    }

    @Test
    public void testResetAsNaN() throws Exception {
        JsonToken token = parser.resetAsNaN("NaN", Double.NaN);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, token);
        Assert.assertEquals("NaN", parser._textBuffer.contentsAsString());
        Assert.assertEquals(Double.NaN, parser._numberDouble, 0.0);
        Assert.assertEquals(ParserMinimalBase.NR_DOUBLE, parser._numTypesValid);
    }

    @Test
    public void testIsNaNTrueNaN() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", Double.NaN);
        Assert.assertTrue(parser.isNaN());
    }

    @Test
    public void testIsNaNTrueInfinite() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", Double.POSITIVE_INFINITY);
        Assert.assertTrue(parser.isNaN());
    }

    @Test
    public void testIsNaNFalse() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", 1.0);
        Assert.assertFalse(parser.isNaN());
    }

    @Test
    public void testIsNaNNotFloat() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        Assert.assertFalse(parser.isNaN());
    }

    @Test
    public void testGetNumberValueInt() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_INT);
        setField(parser, "_numberInt", 42);
        Assert.assertEquals(Integer.valueOf(42), parser.getNumberValue());
    }

    @Test
    public void testGetNumberValueLong() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        setField(parser, "_numberLong", 123L);
        Assert.assertEquals(Long.valueOf(123L), parser.getNumberValue());
    }

    @Test
    public void testGetNumberValueBigInt() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGINT);
        BigInteger bi = new BigInteger("12345678901234567890");
        setField(parser, "_numberBigInt", bi);
        Assert.assertEquals(bi, parser.getNumberValue());
    }

    @Test
    public void testGetNumberValueBigDecimal() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGDECIMAL);
        BigDecimal bd = new BigDecimal("123.45");
        setField(parser, "_numberBigDecimal", bd);
        Assert.assertEquals(bd, parser.getNumberValue());
    }

    @Test
    public void testGetNumberValueDouble() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", 3.14);
        Assert.assertEquals(Double.valueOf(3.14), parser.getNumberValue());
    }

    @Test
    public void testGetNumberTypeInt() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_INT);
        Assert.assertEquals(com.fasterxml.jackson.core.NumberType.INT, parser.getNumberType());
    }

    @Test
    public void testGetNumberTypeLong() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        Assert.assertEquals(com.fasterxml.jackson.core.NumberType.LONG, parser.getNumberType());
    }

    @Test
    public void testGetNumberTypeBigInt() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(com.fasterxml.jackson.core.NumberType.BIG_INTEGER, parser.getNumberType());
    }

    @Test
    public void testGetNumberTypeBigDecimal() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGDECIMAL);
        Assert.assertEquals(com.fasterxml.jackson.core.NumberType.BIG_DECIMAL, parser.getNumberType());
    }

    @Test
    public void testGetNumberTypeDouble() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(com.fasterxml.jackson.core.NumberType.DOUBLE, parser.getNumberType());
    }

    @Test
    public void testGetIntValueAlreadyInt() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_INT);
        setField(parser, "_numberInt", 10);
        Assert.assertEquals(10, parser.getIntValue());
    }

    @Test
    public void testGetLongValueAlreadyLong() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        setField(parser, "_numberLong", 100L);
        Assert.assertEquals(100L, parser.getLongValue());
    }

    @Test
    public void testGetBigIntegerValueAlreadyBigInt() throws Exception {
        BigInteger bi = new BigInteger("999");
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGINT);
        setField(parser, "_numberBigInt", bi);
        Assert.assertEquals(bi, parser.getBigIntegerValue());
    }

    @Test
    public void testGetFloatValue() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", 2.5);
        Assert.assertEquals(2.5f, parser.getFloatValue(), 0.0f);
    }

    @Test
    public void testGetDoubleValueAlreadyDouble() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", 3.14);
        Assert.assertEquals(3.14, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testGetDecimalValueAlreadyBigDecimal() throws Exception {
        BigDecimal bd = new BigDecimal("1.23");
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_BIGDECIMAL);
        setField(parser, "_numberBigDecimal", bd);
        Assert.assertEquals(bd, parser.getDecimalValue());
    }

    @Test
    public void testParseNumericValueIntShort() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_intLength", 5);
        setField(parser, "_numberNegative", false);
        textBuffer.resetWithString("12345");
        parser._parseNumericValue(ParserMinimalBase.NR_UNKNOWN);
        Assert.assertEquals(ParserMinimalBase.NR_INT, parser._numTypesValid);
        Assert.assertEquals(12345, parser._numberInt);
    }

    @Test
    public void testParseNumericValueIntLong() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_intLength", 15);
        setField(parser, "_numberNegative", false);
        textBuffer.resetWithString("123456789012345");
        parser._parseNumericValue(ParserMinimalBase.NR_UNKNOWN);
        Assert.assertEquals(ParserMinimalBase.NR_LONG, parser._numTypesValid);
        Assert.assertEquals(123456789012345L, parser._numberLong);
    }

    @Test
    public void testParseNumericValueIntBig() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_intLength", 20);
        setField(parser, "_numberNegative", false);
        textBuffer.resetWithString("12345678901234567890");
        parser._parseNumericValue(ParserMinimalBase.NR_BIGINT);
        Assert.assertEquals(ParserMinimalBase.NR_BIGINT, parser._numTypesValid);
        Assert.assertEquals(new BigInteger("12345678901234567890"), parser._numberBigInt);
    }

    @Test
    public void testParseNumericValueFloat() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_FLOAT);
        setField(parser, "_intLength", 1);
        setField(parser, "_fractLength", 2);
        setField(parser, "_expLength", 0);
        setField(parser, "_numberNegative", false);
        textBuffer.resetWithString("1.23");
        parser._parseNumericValue(ParserMinimalBase.NR_DOUBLE);
        Assert.assertEquals(ParserMinimalBase.NR_DOUBLE, parser._numTypesValid);
        Assert.assertEquals(1.23, parser._numberDouble, 0.0);
    }

    @Test(expected = JsonParseException.class)
    public void testParseNumericValueNonNumeric() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_STRING);
        parser._parseNumericValue(ParserMinimalBase.NR_UNKNOWN);
    }

    @Test
    public void testParseIntValueShort() throws Exception {
        setField(parser, "_currToken", JsonToken.VALUE_NUMBER_INT);
        setField(parser, "_intLength", 3);
        setField(parser, "_numberNegative", false);
        textBuffer.resetWithString("123");
        int val = parser._parseIntValue();
        Assert.assertEquals(123, val);
        Assert.assertEquals(ParserMinimalBase.NR_INT, parser._numTypesValid);
    }

    @Test
    public void testConvertNumberToIntFromLong() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        setField(parser, "_numberLong", 100L);
        parser.convertNumberToInt();
        Assert.assertEquals(100, parser._numberInt);
        Assert.assertTrue((parser._numTypesValid & ParserMinimalBase.NR_INT) != 0);
    }

    @Test(expected = JsonParseException.class)
    public void testConvertNumberToIntFromLongOverflow() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        setField(parser, "_numberLong", Long.MAX_VALUE);
        parser.convertNumberToInt();
    }

    @Test
    public void testConvertNumberToLongFromInt() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_INT);
        setField(parser, "_numberInt", 50);
        parser.convertNumberToLong();
        Assert.assertEquals(50L, parser._numberLong);
        Assert.assertTrue((parser._numTypesValid & ParserMinimalBase.NR_LONG) != 0);
    }

    @Test
    public void testConvertNumberToBigIntegerFromLong() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_LONG);
        setField(parser, "_numberLong", 123L);
        parser.convertNumberToBigInteger();
        Assert.assertEquals(BigInteger.valueOf(123L), parser._numberBigInt);
        Assert.assertTrue((parser._numTypesValid & ParserMinimalBase.NR_BIGINT) != 0);
    }

    @Test
    public void testConvertNumberToDoubleFromInt() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_INT);
        setField(parser, "_numberInt", 7);
        parser.convertNumberToDouble();
        Assert.assertEquals(7.0, parser._numberDouble, 0.0);
        Assert.assertTrue((parser._numTypesValid & ParserMinimalBase.NR_DOUBLE) != 0);
    }

    @Test
    public void testConvertNumberToBigDecimalFromDouble() throws Exception {
        setField(parser, "_numTypesValid", ParserMinimalBase.NR_DOUBLE);
        setField(parser, "_numberDouble", 2.5);
        textBuffer.resetWithString("2.5");
        parser.convertNumberToBigDecimal();
        Assert.assertEquals(new BigDecimal("2.5"), parser._numberBigDecimal);
        Assert.assertTrue((parser._numTypesValid & ParserMinimalBase.NR_BIGDECIMAL) != 0);
    }

    @Test
    public void testReportMismatchedEndMarker() throws Exception {
        parser.throwOnError = false;
        parser._reportMismatchedEndMarker(']', '}');
        Assert.assertNotNull(parser.lastError);
        Assert.assertTrue(parser.lastError.contains("Unexpected close marker"));
    }

    @Test
    public void testHandleUnrecognizedCharacterEscapeAllowed() throws Exception {
        parser.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        Assert.assertEquals('x', parser._handleUnrecognizedCharacterEscape('x'));
    }

    @Test
    public void testHandleUnrecognizedCharacterEscapeSingleQuote() throws Exception {
        parser.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Assert.assertEquals('\'', parser._handleUnrecognizedCharacterEscape('\''));
    }

    @Test(expected = JsonParseException.class)
    public void testHandleUnrecognizedCharacterEscapeError() throws Exception {
        parser._handleUnrecognizedCharacterEscape('z');
    }

    @Test
    public void testThrowUnquotedSpaceAllowed() throws Exception {
        parser.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        parser._throwUnquotedSpace(0x20, "test");
    }

    @Test(expected = JsonParseException.class)
    public void testThrowUnquotedSpaceDisallowed() throws Exception {
        parser._throwUnquotedSpace(0x01, "test");
    }

    @Test
    public void testDecodeBase64EscapeIntNoBackslash() throws Exception {
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        try {
            parser._decodeBase64Escape(b64, 'A', 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDecodeBase64EscapeIntWhitespaceIndex0() throws Exception {
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        parser.decodeEscapedReturn = ' ';
        Assert.assertEquals(-1, parser._decodeBase64Escape(b64, '\\', 0));
    }

    @Test
    public void testDecodeBase64EscapeCharNoBackslash() throws Exception {
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        try {
            parser._decodeBase64Escape(b64, 'A', 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testReportInvalidBase64Char() {
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        try {
            parser.reportInvalidBase64Char(b64, ' ', 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("white space"));
        }
    }

    @Test
    public void testHandleBase64MissingPadding() throws Exception {
        parser.throwOnError = false;
        Base64Variant b64 = Base64Variants.getDefaultVariant();
        parser._handleBase64MissingPadding(b64);
        Assert.assertNotNull(parser.lastError);
    }

    @Test
    public void testGetSourceReferenceEnabled() throws Exception {
        parser.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        Assert.assertEquals("source", parser._getSourceReference());
    }

    @Test
    public void testGetSourceReferenceDisabled() {
        Assert.assertNull(parser._getSourceReference());
    }

    @Test
    public void testGrowArrayByNull() {
        int[] result = ParserBase.growArrayBy(null, 5);
        Assert.assertArrayEquals(new int[5], result);
    }

    @Test
    public void testGrowArrayByExisting() {
        int[] arr = new int[] { 1, 2 };
        int[] result = ParserBase.growArrayBy(arr, 3);
        Assert.assertEquals(5, result.length);
        Assert.assertEquals(1, result[0]);
        Assert.assertEquals(2, result[1]);
    }

    @Test
    public void testLoadMoreGuaranteedSuccess() throws Exception {
        parser.loadMoreReturn = true;
        parser.loadMoreGuaranteed();
    }

    @Test(expected = JsonParseException.class)
    public void testLoadMoreGuaranteedFailure() throws Exception {
        parser.loadMoreReturn = false;
        parser.loadMoreGuaranteed();
    }

    @Test
    public void testLoadMoreDefault() throws Exception {
        Assert.assertFalse(parser.loadMore());
    }

    @Test
    public void testFinishString() throws Exception {
        parser._finishString();
    }
}
