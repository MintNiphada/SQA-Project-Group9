package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import org.junit.Assert;
import org.junit.Test;

public class ParserMinimalBaseTest {

    static class MinimalTestParser extends ParserMinimalBase {
        private final List<JsonToken> tokens = new LinkedList<>();
        private String currentText = "";
        private String currentName = "";
        private Object embeddedObject = null;
        private int intVal = 0;
        private long longVal = 0L;
        private double doubleVal = 0.0;
        private boolean closed = false;

        public MinimalTestParser() {
            super();
        }

        public MinimalTestParser(int features) {
            super(features);
        }

        public MinimalTestParser addTokens(JsonToken... tList) {
            tokens.addAll(Arrays.asList(tList));
            return this;
        }

        public void setCurrToken(JsonToken token) {
            this._currToken = token;
        }

        public void setText(String text) {
            this.currentText = text;
        }

        public void setCurrentNameValue(String name) {
            this.currentName = name;
        }

        public void setEmbeddedObject(Object obj) {
            this.embeddedObject = obj;
        }

        public void setIntValue(int val) {
            this.intVal = val;
        }

        public void setLongValue(long val) {
            this.longVal = val;
        }

        public void setDoubleValue(double val) {
            this.doubleVal = val;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            if (tokens.isEmpty()) {
                _currToken = null;
            } else {
                _currToken = tokens.remove(0);
            }
            return _currToken;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF();
        }

        @Override
        public String getCurrentName() throws IOException {
            return currentName;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public void overrideCurrentName(String name) {
            this.currentName = name;
        }

        @Override
        public String getText() throws IOException {
            return currentText;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return currentText != null ? currentText.toCharArray() : null;
        }

        @Override
        public boolean hasTextCharacters() {
            return currentText != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return currentText != null ? currentText.length() : 0;
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return new byte[0];
        }

        @Override
        public int getIntValue() throws IOException {
            return intVal;
        }

        @Override
        public long getLongValue() throws IOException {
            return longVal;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return doubleVal;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return intVal;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return embeddedObject;
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public void setCodec(ObjectCodec c) { }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonLocation getTokenLocation() {
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return JsonLocation.NA;
        }
    }

    @Test
    public void testTokenInspectionAndState() {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertNull(p.currentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, p.currentTokenId());
        Assert.assertNull(p.getCurrentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        Assert.assertFalse(p.hasCurrentToken());
        Assert.assertTrue(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertFalse(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertTrue(p.hasToken(null));
        Assert.assertFalse(p.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(p.isExpectedStartArrayToken());
        Assert.assertFalse(p.isExpectedStartObjectToken());

        p.setCurrToken(JsonToken.START_OBJECT);
        Assert.assertSame(JsonToken.START_OBJECT, p.currentToken());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, p.currentTokenId());
        Assert.assertSame(JsonToken.START_OBJECT, p.getCurrentToken());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, p.getCurrentTokenId());
        Assert.assertTrue(p.hasCurrentToken());
        Assert.assertTrue(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertFalse(p.hasTokenId(JsonTokenId.ID_START_ARRAY));
        Assert.assertTrue(p.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(p.isExpectedStartArrayToken());
        Assert.assertTrue(p.isExpectedStartObjectToken());

        p.setCurrToken(JsonToken.START_ARRAY);
        Assert.assertTrue(p.isExpectedStartArrayToken());
        Assert.assertFalse(p.isExpectedStartObjectToken());

        p.clearCurrentToken();
        Assert.assertNull(p.currentToken());
        Assert.assertSame(JsonToken.START_ARRAY, p.getLastClearedToken());

        // Clearing again when null does not overwrite last cleared
        p.clearCurrentToken();
        Assert.assertSame(JsonToken.START_ARRAY, p.getLastClearedToken());
    }

    @Test
    public void testNextValue() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        p.addTokens(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.VALUE_NUMBER_INT);

        JsonToken t1 = p.nextValue();
        Assert.assertSame(JsonToken.VALUE_STRING, t1);

        JsonToken t2 = p.nextValue();
        Assert.assertSame(JsonToken.VALUE_NUMBER_INT, t2);

        JsonToken t3 = p.nextValue();
        Assert.assertNull(t3);
    }

    @Test
    public void testSkipChildrenNonStruct() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        p.setCurrToken(JsonToken.VALUE_STRING);
        Assert.assertSame(p, p.skipChildren());
    }

    @Test
    public void testSkipChildrenObjectAndArray() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        p.setCurrToken(JsonToken.START_OBJECT);
        p.addTokens(JsonToken.FIELD_NAME, JsonToken.START_ARRAY, JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_ARRAY, JsonToken.END_OBJECT, JsonToken.VALUE_TRUE);

        Assert.assertSame(p, p.skipChildren());
        Assert.assertSame(JsonToken.END_OBJECT, p.currentToken());
    }

    @Test
    public void testSkipChildrenEOF() {
        MinimalTestParser p = new MinimalTestParser();
        p.setCurrToken(JsonToken.START_OBJECT);
        p.addTokens(JsonToken.FIELD_NAME);

        try {
            p.skipChildren();
            Assert.fail("Expected JsonEOFException on EOF during skipChildren");
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
    }

    @Test
    public void testSkipChildrenNotAvailable() {
        MinimalTestParser p = new MinimalTestParser();
        p.setCurrToken(JsonToken.START_ARRAY);
        p.addTokens(JsonToken.NOT_AVAILABLE);

        try {
            p.skipChildren();
            Assert.fail("Expected JsonParseException on NOT_AVAILABLE during skipChildren");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Not enough content available for `skipChildren()`"));
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
    }

    @Test
    public void testGetValueAsBoolean() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertTrue(p.getValueAsBoolean(true));
        Assert.assertFalse(p.getValueAsBoolean(false));

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertTrue(p.getValueAsBoolean(false));

        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(1);
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setIntValue(0);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("true");
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setText("  false  ");
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setText("null");
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setText("non-boolean");
        Assert.assertTrue(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Boolean.TRUE);
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setEmbeddedObject(Boolean.FALSE);
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setEmbeddedObject("other");
        Assert.assertTrue(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.START_OBJECT);
        Assert.assertTrue(p.getValueAsBoolean(true));
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertEquals(42, p.getValueAsInt(42));
        Assert.assertEquals(0, p.getValueAsInt());

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(123);
        Assert.assertEquals(123, p.getValueAsInt());
        Assert.assertEquals(123, p.getValueAsInt(99));

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setIntValue(456);
        Assert.assertEquals(456, p.getValueAsInt());

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1, p.getValueAsInt(0));

        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0, p.getValueAsInt(1));

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0, p.getValueAsInt(1));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0, p.getValueAsInt(7));
        p.setText("789");
        Assert.assertEquals(789, p.getValueAsInt(0));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Integer.valueOf(321));
        Assert.assertEquals(321, p.getValueAsInt(0));
        p.setEmbeddedObject("not a number");
        Assert.assertEquals(55, p.getValueAsInt(55));

        p.setCurrToken(JsonToken.START_ARRAY);
        Assert.assertEquals(11, p.getValueAsInt(11));
    }

    @Test
    public void testGetValueAsLong() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertEquals(42L, p.getValueAsLong(42L));
        Assert.assertEquals(0L, p.getValueAsLong());

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setLongValue(1234567890123L);
        Assert.assertEquals(1234567890123L, p.getValueAsLong());
        Assert.assertEquals(1234567890123L, p.getValueAsLong(99L));

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setLongValue(9876543210987L);
        Assert.assertEquals(9876543210987L, p.getValueAsLong());

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1L, p.getValueAsLong(0L));

        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0L, p.getValueAsLong(1L));

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0L, p.getValueAsLong(1L));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0L, p.getValueAsLong(7L));
        p.setText("123456789");
        Assert.assertEquals(123456789L, p.getValueAsLong(0L));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Long.valueOf(55555L));
        Assert.assertEquals(55555L, p.getValueAsLong(0L));
        p.setEmbeddedObject("not a number");
        Assert.assertEquals(77L, p.getValueAsLong(77L));

        p.setCurrToken(JsonToken.START_ARRAY);
        Assert.assertEquals(88L, p.getValueAsLong(88L));
    }

    @Test
    public void testGetValueAsDouble() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertEquals(4.2, p.getValueAsDouble(4.2), 0.0001);

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setDoubleValue(12.5);
        Assert.assertEquals(12.5, p.getValueAsDouble(0.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setDoubleValue(34.5);
        Assert.assertEquals(34.5, p.getValueAsDouble(0.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1.0, p.getValueAsDouble(0.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0.0, p.getValueAsDouble(1.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0.0, p.getValueAsDouble(1.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0.0, p.getValueAsDouble(5.5), 0.0001);
        p.setText("123.456");
        Assert.assertEquals(123.456, p.getValueAsDouble(0.0), 0.0001);

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Double.valueOf(78.9));
        Assert.assertEquals(78.9, p.getValueAsDouble(0.0), 0.0001);
        p.setEmbeddedObject("not a number");
        Assert.assertEquals(10.5, p.getValueAsDouble(10.5), 0.0001);

        p.setCurrToken(JsonToken.START_OBJECT);
        Assert.assertEquals(33.3, p.getValueAsDouble(33.3), 0.0001);
    }

    @Test
    public void testGetValueAsString() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertNull(p.getValueAsString());
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("hello");
        Assert.assertEquals("hello", p.getValueAsString());
        Assert.assertEquals("hello", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.FIELD_NAME);
        p.setCurrentNameValue("fieldA");
        Assert.assertEquals("fieldA", p.getValueAsString());
        Assert.assertEquals("fieldA", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertNull(p.getValueAsString());
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.START_OBJECT);
        Assert.assertNull(p.getValueAsString());
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("100");
        Assert.assertEquals("100", p.getValueAsString());
        Assert.assertEquals("100", p.getValueAsString("def"));
    }

    @Test
    public void testBase64Decoding() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        p._decodeBase64("SGVsbG8gV29ybGQ=", builder, Base64Variants.MIME);
        byte[] bytes = builder.toByteArray();
        Assert.assertEquals("Hello World", new String(bytes, "UTF-8"));

        try {
            ByteArrayBuilder badBuilder = new ByteArrayBuilder();
            p._decodeBase64("!NotBase64!", badBuilder, Base64Variants.MIME);
            Assert.fail("Expected JsonParseException on invalid Base64 input");
        } catch (JsonParseException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testLongIntegerAndNumberDesc() {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertEquals("123", p._longIntegerDesc("123"));
        Assert.assertEquals("123.45", p._longNumberDesc("123.45"));

        StringBuilder sbLongPos = new StringBuilder();
        for (int i = 0; i < 1005; i++) {
            sbLongPos.append("1");
        }
        Assert.assertEquals("[Integer with 1005 digits]", p._longIntegerDesc(sbLongPos.toString()));
        Assert.assertEquals("[number with 1005 characters]", p._longNumberDesc(sbLongPos.toString()));

        StringBuilder sbLongNeg = new StringBuilder("-");
        for (int i = 0; i < 1005; i++) {
            sbLongNeg.append("2");
        }
        Assert.assertEquals("[Integer with 1005 digits]", p._longIntegerDesc(sbLongNeg.toString()));
        Assert.assertEquals("[number with 1005 characters]", p._longNumberDesc(sbLongNeg.toString()));
    }

    @Test
    public void testGetCharDesc() {
        Assert.assertEquals("(CTRL-CHAR, code 0)", ParserMinimalBase._getCharDesc(0));
        Assert.assertEquals("(CTRL-CHAR, code 10)", ParserMinimalBase._getCharDesc('\n'));
        Assert.assertEquals("'a' (code 97)", ParserMinimalBase._getCharDesc('a'));
        Assert.assertEquals("'\u0100' (code 256 / 0x100)", ParserMinimalBase._getCharDesc(256));
    }

    @Test
    public void testErrorReportingMethods() throws IOException {
        MinimalTestParser p = new MinimalTestParser(0);

        try {
            p.reportUnexpectedNumberChar('x', "extra info");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected character ('x' (code 120)) in numeric value: extra info"));
        }

        try {
            p.reportUnexpectedNumberChar('y', null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected character ('y' (code 121)) in numeric value"));
        }

        try {
            p.reportInvalidNumber("corrupt");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid numeric value: corrupt"));
        }

        p.setText("999999999999");
        try {
            p.reportOverflowInt();
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("out of range of int"));
        }

        try {
            p.reportOverflowInt("1234567890123");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("out of range of int"));
        }

        try {
            p.reportOverflowLong();
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("out of range of long"));
        }

        try {
            p.reportOverflowLong("99999999999999999999");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("out of range of long"));
        }

        try {
            p._reportUnexpectedChar('z', "some comment");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected character ('z' (code 122)): some comment"));
        }

        try {
            p._reportUnexpectedChar('w', null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected character ('w' (code 119))"));
        }

        try {
            p._reportUnexpectedChar(-1, null);
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }

        try {
            p._reportInvalidEOF();
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }

        try {
            p._reportInvalidEOFInValue(JsonToken.VALUE_STRING);
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("in a String value"));
        }

        try {
            p._reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_INT);
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("in a Number value"));
        }

        try {
            p._reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_FLOAT);
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("in a Number value"));
        }

        try {
            p._reportInvalidEOFInValue(JsonToken.VALUE_TRUE);
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("in a value"));
        }

        try {
            p._reportInvalidEOFInValue();
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("in a value"));
        }

        try {
            p._reportInvalidEOF(" custom eof");
            Assert.fail();
        } catch (JsonEOFException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input custom eof"));
        }

        try {
            p._reportMissingRootWS('?');
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Expected space separating root-level values"));
        }

        try {
            p._throwInvalidSpace(160);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("only regular white space"));
        }

        try {
            p._reportError("Format error %s", "arg1");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertEquals("Format error arg1", e.getOriginalMessage());
        }

        try {
            p._reportError("Format error %s %d", "arg1", 2);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertEquals("Format error arg1 2", e.getOriginalMessage());
        }

        try {
            p._wrapError("Wrapped error", new IOException("cause"));
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertEquals("Wrapped error", e.getOriginalMessage());
            Assert.assertEquals("cause", e.getCause().getMessage());
        }

        try {
            p._reportInputCoercion("Cannot coerce", JsonToken.VALUE_STRING, Integer.class);
            Assert.fail();
        } catch (InputCoercionException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot coerce"));
            Assert.assertSame(JsonToken.VALUE_STRING, e.getInputType());
            Assert.assertSame(Integer.class, e.getTargetType());
        }
    }

    @Test(expected = RuntimeException.class)
    public void testThrowInternal() {
        MinimalTestParser p = new MinimalTestParser();
        p._throwInternal();
    }

    @Test
    public void testAsciiConversionHelpers() {
        String original = "Hello ASCII 123";
        byte[] bytes = ParserMinimalBase._asciiBytes(original);
        Assert.assertArrayEquals(new byte[] { 'H', 'e', 'l', 'l', 'o', ' ', 'A', 'S', 'C', 'I', 'I', ' ', '1', '2', '3' }, bytes);
        String converted = ParserMinimalBase._ascii(bytes);
        Assert.assertEquals(original, converted);
    }

    @Test
    public void testConstantsAndConstructors() {
        Assert.assertEquals(0, ParserMinimalBase.NO_BYTES.length);
        Assert.assertEquals(0, ParserMinimalBase.NO_INTS.length);
        Assert.assertEquals(256, ParserMinimalBase.MAX_ERROR_TOKEN_LENGTH);
        Assert.assertEquals(BigInteger.valueOf(Integer.MIN_VALUE), ParserMinimalBase.BI_MIN_INT);
        Assert.assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), ParserMinimalBase.BI_MAX_INT);
        Assert.assertEquals(BigInteger.valueOf(Long.MIN_VALUE), ParserMinimalBase.BI_MIN_LONG);
        Assert.assertEquals(BigInteger.valueOf(Long.MAX_VALUE), ParserMinimalBase.BI_MAX_LONG);
        Assert.assertEquals(new BigDecimal(ParserMinimalBase.BI_MIN_INT), ParserMinimalBase.BD_MIN_INT);
        Assert.assertEquals(new BigDecimal(ParserMinimalBase.BI_MAX_INT), ParserMinimalBase.BD_MAX_INT);
        Assert.assertEquals(new BigDecimal(ParserMinimalBase.BI_MIN_LONG), ParserMinimalBase.BD_MIN_LONG);
        Assert.assertEquals(new BigDecimal(ParserMinimalBase.BI_MAX_LONG), ParserMinimalBase.BD_MAX_LONG);
        Assert.assertEquals((long) Integer.MIN_VALUE, ParserMinimalBase.MIN_INT_L);
        Assert.assertEquals((long) Integer.MAX_VALUE, ParserMinimalBase.MAX_INT_L);
        Assert.assertEquals((double) Integer.MIN_VALUE, ParserMinimalBase.MIN_INT_D, 0.0);
        Assert.assertEquals((double) Integer.MAX_VALUE, ParserMinimalBase.MAX_INT_D, 0.0);
        Assert.assertEquals((double) Long.MIN_VALUE, ParserMinimalBase.MIN_LONG_D, 0.0);
        Assert.assertEquals((double) Long.MAX_VALUE, ParserMinimalBase.MAX_LONG_D, 0.0);
    }
}
