package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.*;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.MergedStream;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.*;

public class UTF8StreamJsonParserTest {

    private IOContext _context;
    private ByteQuadsCanonicalizer _symbols;

    @Before
    public void setUp() {
        _context = IOContext.createInitContext(new BufferRecycler());
        _symbols = ByteQuadsCanonicalizer.createRoot();
    }

    private UTF8StreamJsonParser parser(String input) throws IOException {
        return parser(input, false);
    }

    private UTF8StreamJsonParser parser(String input, boolean bufferRecyclable) throws IOException {
        byte[] data = input.getBytes("UTF-8");
        return new UTF8StreamJsonParser(_context, Feature.collectDefaults(),
                new ByteArrayInputStream(data), null, _symbols,
                data, 0, data.length, bufferRecyclable);
    }

    private UTF8StreamJsonParser parser(byte[] data) throws IOException {
        return new UTF8StreamJsonParser(_context, Feature.collectDefaults(),
                new ByteArrayInputStream(data), null, _symbols,
                data, 0, data.length, false);
    }

    private UTF8StreamJsonParser parser(String input, int features, boolean bufferRecyclable) throws IOException {
        byte[] data = input.getBytes("UTF-8");
        return new UTF8StreamJsonParser(_context, features,
                new ByteArrayInputStream(data), null, _symbols,
                data, 0, data.length, bufferRecyclable);
    }

    @Test
    public void testGetCodecAndSetCodec() throws IOException {
        UTF8StreamJsonParser p = parser("true");
        assertNull(p.getCodec());
        ObjectCodec oc = null;
        p.setCodec(oc);
        assertNull(p.getCodec());
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        UTF8StreamJsonParser p = parser("123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        // release should give 0 because input stream consumed
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int rel = p.releaseBuffered(bos);
        assertTrue(rel >= 0);
        // subsequent getText etc.
        assertEquals("123", p.getText());
    }

    @Test
    public void testGetInputSource() throws IOException {
        InputStream in = new ByteArrayInputStream("null".getBytes("UTF-8"));
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(_context, Feature.collectDefaults(),
                in, null, _symbols,
                "null".getBytes("UTF-8"), 0, 4, false);
        assertSame(in, p.getInputSource());
    }

    @Test
    public void testLoadMoreReturnsFalseOnClosed() throws IOException {
        // simulate closed state: _inputStream null
        UTF8StreamJsonParser p = parser("1");
        p.close();
        try {
            // can't test protected loadMore directly but nextToken after close returns null
            assertNull(p.nextToken());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testLoadToHaveAtLeast() throws IOException {
        // protected method indirectly exercised by long name parsing that requires multiple loads
        StringBuilder sb = new StringBuilder();
        sb.append("{\""");
        for (int i =0; i <200; i++) {sb.append('a');}
        sb.append("":1}");
        UTF8StreamJsonParser p = parser(sb.toString());
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        String name = p.nextFieldName();
        assertNotNull(name);
        assertTrue(name.length() > 200);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertTrue(p.nextToken() == JsonToken.END_OBJECT);
    }

    @Test
    public void testCloseInput() throws IOException {
        UTF8StreamJsonParser p = parser("{}");
        p.close();
        // should release resources
        assertNull(p.getInputSource()); // getInputStream cleared
        try {
            p.nextToken();
            fail("Should be closed");
        } catch (IOException e) {
            // expected if closed properly
        }
    }

    @Test
    public void testReleaseBuffers() throws IOException {
        UTF8StreamJsonParser p = parser("{}");
        p.close();
        // just no exception
    }

    @Test
    public void testGetTextValuesting() throws IOException {
        UTF8StreamJsonParser p = parser("\"hello\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        assertEquals("hello", p.getText());
        assertTrue(p.nextToken() == null);
    }

    @Test
    public void testGetTextIncompleteString() throws IOException {
        // partial string token (_tokenIncomplete = true)
        UTF8StreamJsonParser p = parser("{\"key\":\"hello\"}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        // At this point the string may be incomplete if parsed field name with _tokenIncomplete=true?
        // Actually after nextToken it's set. But we can simulate by forcibly setting incomplete.
        p._tokenIncomplete = true;
        assertEquals("hello", p.getText());
        assertFalse(p._tokenIncomplete);
    }

    @Test
    public void testGetValueAsString() throws IOException {
        UTF8StreamJsonParser p = parser("\"test\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        assertEquals("test", p.getValueAsString());
    }

    @Test
    public void testGetValueAsStringDefValue() throws IOException {
        UTF8StreamJsonParser p = parser("123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals("123", p.getValueAsString("default"));
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        UTF8StreamJsonParser p = parser("789");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(789, p.getValueAsInt());
        assertEquals(789, p.getValueAsInt(0));
    }

    @Test
    public void testGetValueAsIntWithDefault() throws IOException {
        UTF8StreamJsonParser p = parser("true");
        assertTrue(p.nextToken() == JsonToken.VALUE_TUE);
        assertEquals(0, p.getValueAsInt(0));
    }

    @Test
    public void testGetTextCharactersFieldName() throws IOException {
        UTF8StreamJsonParser p = parser("{\"myName\":1}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("myName", p.nextFieldName());
        char[] chars = p.getTextCharacters();
        assertNotNull(chars);
        assertEquals("myName", new String(chars, 0, p.getTextLength()));
    }

    @Test
    public void testGetTextLength() throws IOException {
        UTF8StreamJsonParser p = parser("123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(3, p.getTextLength());
    }

    @Test
    public void testGetTextOffset() throws IOException {
        UTF8StreamJsonParser p = parser("123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(0, p.getTextOffset());
    }

    @Test
    public void testGetBinaryValue() throws IOException {
        UTF8StreamJsonParser p = parser("\"VG8=\""); // base64 for "To"
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        Base64Variant bv = new Base64Variant();
        byte[] bin = p.getBinaryValue(bv);
        assertArrayEquals("To".getBytes("UTF-8"), bin);
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        UTF8StreamJsonParser p = parser("\"VG8=\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        Base64Variant bv = new Base64Variant();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = p.readBinaryValue(bv, bos);
        assertArrayEquals("To".getBytes("UTF-8"), bos.toByteArray());
        assertEquals(2, len);
    }

    @Test
    public void testGetTokenLocation() throws IOException {
        UTF8StreamJsonParser p = parser(" [ ] ");
        assertTrue(p.nextToken() == JsonToken.SART_ARRAY);
        JsonLocation loc = p.getTokenLocation();
        assertNotNull(loc);
        assertTrue(loc.getCharOffset() >= 0);
    }

    @Test
    public void testGetCurrentLocation() throws IOException {
        UTF8StreamJsonParser p = parser("{}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        JsonLocation loc = p.getCurrentLocation();
        assertNotNull(loc);
    }

    @Test
    public void testNextTokenEndOfInput() throws IOException {
        UTF8StreamJsonParser p = parser("   ");
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenWhitespaceAndClose() throws IOException {
        UTF8StreamJsonParser p = parser("{}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertTrue(p.nextToken() == JsonToken.END_OBJECT);
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenArray() throws IOException {
        UTF8StreamJsonParser p = parser("[]");
        assertTrue(p.nextToken() == JsonToken.SART_ARRAY);
        assertTrue(p.nextToken() == JsonToken.END_ARRAY);
    }

    @Test
    public void testNextTokenObjectWithValues() throws IOException {
        UTF8StreamJsonParser p = parser("{\"key\":123}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertTrue(p.nextToken() == JsonToken.END_OBJECT);
    }

    @Test
    public void testNextTokenBooleans() throws IOException {
        UTF8StreamJsonParser p = parser("true false");
        assertTrue(p.nextToken() == JsonToken.VALUE_TUE);
        assertTrue(p.nextToken() == JsonToken.VALUE_FALSE);
    }

    @Test
    public void testNextTokenNull() throws IOException {
        UTF8StreamJsonParser p = parser("null");
        assertTrue(p.nextToken() == JsonToken.VALUE_ULL);
    }

    @Test
    public void testNextTokenNumberPositive() throws IOException {
        UTF8StreamJsonParser p = parser("123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(123, p.getIntValue());
    }

    @Test
    public void testNextTokenNegativeNumber() throws IOException {
        UTF8StreamJsonParser p = parser("-456");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(-456, p.getIntValue());
    }

    @Test
    public void testNextTokenFloat() throws IOException {
        UTF8StreamJsonParser p = parser("12.34");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(12.34, p.getDoubleValue(), 0.0001);
    }

    @Test
    public void testNextTokenExponent() throws IOException {
        UTF8StreamJsonParser p = parser("1e3");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(1000.0, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testNextTokenMultipleRootValuesWithSpaces() throws IOException {
        UTF8StreamJsonParser p = parser("123 456");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(123, p.getIntValue());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(456, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenRootSpaceValidation() throws IOException {
        // root values must have space between
        UTF8StreamJsonParser p = parser("123  ");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertNull(p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testNextTokenRootWithoutSpace() throws IOException {
        UTF8StreamJsonParser p = parser("123true");
        p.nextToken();
        p.nextToken(); // should fail
    }

    @Test
    public void testNextAfterName() throws IOException {
        UTF8StreamJsonParser p = parser("{\"x\":[1]}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("x", p.nextFieldName());
        // next token should be start array
        assertEquals(JsonToken.SART_ARRAY, p.nextToken());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertTrue(p.nextToken() == JsonToken.END_ARRAY);
        assertTrue(p.nextToken() == JsonToken.END_OBJECT);
    }

    @Test
    public void testNextFieldName() throws IOException {
        UTF8StreamJsonParser p = parser("{\"name\":\"value\"}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("name", p.nextFieldName());
        assertEquals("value", p.getText());
    }

    @Test
    public void testNextFieldNameSerializableString() throws IOException {
        UTF8StreamJsonParser p = parser("{\"myField\":123}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        SerializableString ss = new SerializableString("myField");
        assertTrue(p.nextFieldName(ss));
        assertEquals("myField", p.getText());
    }

    @Test
    public void testNextTextValue() throws IOException {
        UTF8StreamJsonParser p = parser("{\"key\":\"text\"}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertEquals("text", p.nextTextValue());
    }

    @Test
    public void testNextIntValue() throws IOException {
        UTF8StreamJsonParser p = parser("{\"key\":123}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertEquals(123, p.nextIntValue(0));
    }

    @Test
    public void testNextLongValue() throws IOException {
        UTF8StreamJsonParser p = parser("{\"key\":123456789}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertEquals(123456789L, p.nextLongValue(0));
    }

    @Test
    public void testNextBooleanValue() throws IOException {
        UTF8StreamJsonParser p = parser("{\"key\":true}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("key", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
    }

    @Test
    public void testParsePosNumber() throws IOException {
        UTF8StreamJsonParser p = parser("0");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(0, p.getIntValue());
    }

    @Test
    public void testParseNegNumber() throws IOException {
        UTF8StreamJsonParser p = parser("-0");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(0, p.getIntValue());
    }

    @Test
    public void testVerifyNoLeadingZeroes() throws IOException {
        UTF8StreamJsonParser p = parser("001");
        try {
            p.nextToken();
            fail("Should throw");
        } catch (JsonParseException e) {
            // expected
        }
        // with feature ALLOW_NUMERIC_LEADING_ZEROS
        p = parser("001", Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask(), false);
        JsonToken t = p.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(1, p.getIntValue());
    }

    @Test
    public void testParseFloatDecimal() throws IOException {
        UTF8StreamJsonParser p = parser("0.5");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(0.5, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseFloatExponent() throws IOException {
        UTF8StreamJsonParser p = parser("1e2");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(100.0, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseFloatExponentSign() throws IOException {
        UTF8StreamJsonParser p = parser("1e+2");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(100.0, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseFloatExponentNegative() throws IOException {
        UTF8StreamJsonParser p = parser("1e-2");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(0.01, p.getDoubleValue(), 0.0001);
    }

    @Test
    public void testVerifyRootSpaceCRLF() throws IOException {
        UTF8StreamJsonParser p = parser("123\r\n456");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
    }

    @Test
    public void testNameParseShort() throws IOException {
        UTF8StreamJsonParser p = parser("{\"a\":1}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("a", p.nextFieldName());
    }

    @Test
    public void testNameParse2Bytes() throws IOException {
        UTF8StreamJsonParser p = parser("{\"ab\":2}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("ab", p.nextFieldName());
    }

    @Test
    public void testNameParse3Bytes() throws IOException {
        UTF8StreamJsonParser p = parser("{\"abc\":3}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("abc", p.nextFieldName());
    }

    @Test
    public void testNameParse4Bytes() throws IOException {
        UTF8StreamJsonParser p = parser("{\"abcd\":4}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("abcd", p.nextFieldName());
    }

    @Test
    public void testNameParse12Bytes() throws IOException {
        UTF8StreamJsonParser p = parser("{\"1234567890ab\":12}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        String name = p.nextFieldName();
        assertEquals(12, name.length());
        assertEquals("1234567890ab", name);
    }

    @Test
    public void testNameParseLong() throws IOException {
        StringBuilder sb = new StringBuilder("\"");
        sb.append("{\"");
        for (int i =0; i<200; i++) sb.append('x');
        sb.append("\":1}");
        String input = sb.toString();
        UTF8StreamJsonParser p = parser(input);
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals(200, p.nextFieldName().length());
    }

    @Test
    public void testNameParseEscaped() throws IOException {
        UTF8StreamJsonParser p = parser("{\"a\\n\":1}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("a\n", p.nextFieldName());
    }

    @Test
    public void testNameParseUnquotedWithFeature() throws IOException {
        int feat = Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        UTF8StreamJsonParser p = parser("{abc:1}", feat, false);
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("abc", p.nextFieldName());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
    }

    @Test
    public void testNameParseSingleQuoted() throws IOException {
        int feat = Feature.ALLOW_SINGLE_QUOTES.getMask();
        UTF8StreamJsonParser p = parser("{'def':2}", feat, false);
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("def", p.nextFieldName());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
    }

    @Test
    public void testFinishString() throws IOException {
        UTF8StreamJsonParser p = parser("\"simple\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        p._tokenIncomplete = true; // force incomplete
        assertEquals("simple", p.getText());
    }

    @Test
    public void testFinishStringWithUtf8() throws IOException {
        // Unicode escape or multi-byte
        String unicode = "\"\\u00E9\""; // é
        UTF8StreamJsonParser p = parser(unicode);
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        assertEquals("é", p.getText());
    }

    @Test
    public void testSkipString() throws IOException {
        UTF8StreamJsonParser p = parser("{\"skip\":\"ignored\"}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        p.nextFieldName();
        p._skipString(); // skip value
        assertTrue(p.nextToken() == JsonToken.END_OBJECT);
    }

    @Test
    public void testHandleUnexpectedValue() throws IOException {
        // test tokens like NaN, Infinity
        int feat = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        UTF8StreamJsonParser p = parser("NaN", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertTrue(Double.isNaN(p.getDoubleValue()));
        p = parser("Infinity", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_FLOAT);
        assertTrue(Double.isInfinite(p.getDoubleValue()));
    }

    @Test(expected = JsonParseException.class)
    public void testUnexpectedValueNoFeatureNan() throws IOException {
        UTF8StreamJsonParser p = parser("NaN");
        p.nextToken();
    }

    @Test
    public void testHandleApos() throws IOException {
        int feat = Feature.ALLOW_SINGLE_QUOTES.getMask();
        UTF8StreamJsonParser p = parser("'value'", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        assertEquals("value", p.getText());
    }

    @Test
    public void testMatchToken() throws IOException {
        UTF8StreamJsonParser p = parser("tru");
        try {
            p._matchToken("true", 1);
            fail("Should throw");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testCheckMatchEnd() throws IOException {
        UTF8StreamJsonParser p = parser("truetoken");
        try {
            // forcing match end check after "true"
            p._inputPtr =0;
            p._matchToken("true", 1);
            fail("Should throw");
        } catch (JsonParseException e) {
        }
    }

    @Test
    public void testSkipWS() throws IOException {
        UTF8StreamJsonParser p = parser("  \t\n\r 123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(123, p.getIntValue());
    }

    @Test
    public void testSkipColon() throws IOException {
        UTF8StreamJsonParser p = parser("{\"a\" : 1}");
        assertTrue(p.nextToken() == JsonToken.SART_OBJECT);
        assertEquals("a", p.nextFieldName());
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
    }

    @Test
    public void testSkipComment() throws IOException {
        int feat = Feature.ALLOW_COMMENTS.getMask();
        UTF8StreamJsonParser p = parser("/* comment */1", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(1, p.getIntValue());
    }

    @Test
    public void testSkipLineComment() throws IOException {
        int feat = Feature.ALLOW_COMMENTS.getMask();
        UTF8StreamJsonParser p = parser("// line comment\n2", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(2, p.getIntValue());
    }

    @Test
    public void testSkipYAMLComment() throws IOException {
        int feat = Feature.ALLOW_AML_COMMENTS.getMask();
        UTF8StreamJsonParser p = parser("# yaml comment\n3", feat, false);
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
        assertEquals(3, p.getIntValue());
    }

    @Test
    public void testDecodeEscaped() throws IOException {
        UTF8StreamJsonParser p = parser("\"\\u0041\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        assertEquals("A", p.getText());
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidUtf8Start() throws IOException {
        byte[] bad = new byte[] { (byte) 0x80 }; // invalid start byte
        UTF8StreamJsonParser p = parser(bad);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidUtf8Middle() throws IOException {
        byte[] bad = new byte[] { (byte) 0xC0, 0x20 }; // 0x20 not 10xx xxxx
        UTF8StreamJsonParser p = parser(bad);
        p.nextToken();
    }

    @Test
    public void testGrowArrayBy() throws IOException {
        int[] arr = null;
        arr = UTF8StreamJsonParser.growArrayBy(arr, 10);
        assertArrayEquals(new int[10], arr);
        arr = UTF8StreamJsonParser.growArrayBy(arr, 2);
        assertEquals(12, arr.length);
    }

    @Test
    public void testPad() throws IOException {
        assertEquals(-1 << 8, UTF8StreamJsonParser.pad(0, 1) & 0xFFFFFF00);
        assertEquals(-1, UTF8StreamJsonParser.pad(0, 4));
    }

    @Test
    public void testDecodeBase64() throws IOException {
        // bases64 encoded "hello"
        String encoded = "\"aGVsbG8=\""; // "hello"
        UTF8StreamJsonParser p = parser(encoded);
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        Base64Variant bv = new Base64Variant();
        // should call _decodeBase64 indirectly via getBinaryValue
        byte[] bin = p.getBinaryValue(bv);
        assertEquals("hello", new String(bin, "UTF-8"));
    }

    @Test
    public void testSkipCR() throws IOException {
        UTF8StreamJsonParser p = parser("\r\n 123");
        assertTrue(p.nextToken() == JsonToken.VALUE_NUMBER_INT);
    }

    @Test
    public void testErrorReportingInvalidToken() throws IOException {
        try {
            UTF8StreamJsonParser p = parser("naX");
            p.nextToken();
            fail();
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("naX"));
        }
    }

    @Test
    public void testDecodeCharForError() throws IOException {
        UTF8StreamJsonParser p = parser("\"\\u0041\"");
        assertTrue(p.nextToken() == JsonToken.VALUE_STRING);
        // fine
        // now test error
        byte[] bad = { (byte)0xC0, 0x20};
        p = parser(bad);
        try {
            p.nextToken();
            fail();
        } catch (IOException e) {
            // expected
        }
    }

    private static class Base64Variant extends com.fasterxml.jackson.core.Base64Variant {
        public Base64Variant() {
            super("DEFAULT", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", true, '=', 3);
        }
    }

    private static class SerializableString implements com.fasterxml.jackson.core.SerializableString {
        private final String _value;
        public SerializableString(String v) { _value = v; }
        @Override public String getValue() { return _value; }
        @Override public int charLength() { return _value.length(); }
        @Override public char[] asQuotedChars() { return ("\"" + _value + "\"").toCharArray(); }
        @Override public byte[] asQuotedUTF8() { try { return ("\"" + _value + "\"").getBytes("UTF-8"); } catch (Exception e) { return null; } }
        @Override public byte[] asUnquotedUTF8() { try { return _value.getBytes("UTF-8"); } catch (Exception e) { return null; } }
    }
}
