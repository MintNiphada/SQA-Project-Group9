package com.fasterxml.jackson.core.json.async;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NonBlockingJsonParserTest {

    private NonBlockingJsonParser createParser(int features) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        return new NonBlockingJsonParser(ctxt, features, sym);
    }

    private NonBlockingJsonParser createDefaultParser() {
        return createParser(JsonParser.Feature.collectDefaults());
    }

    private void feedAll(NonBlockingJsonParser p, String doc) throws IOException {
        byte[] bytes = doc.getBytes(StandardCharsets.UTF_8);
        p.feedInput(bytes, 0, bytes.length);
        p.endOfInput();
    }

    @Test
    public void testBasicFeederAndBufferMethods() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        Assert.assertSame(p, p.getNonBlockingInputFeeder());
        Assert.assertTrue(p.needMoreInput());

        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        Assert.assertFalse(p.needMoreInput());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        Assert.assertEquals(3, released);
        Assert.assertArrayEquals(data, out.toByteArray());

        // Feeding again now that input is consumed
        p.feedInput(data, 0, data.length);
        p.endOfInput();
        Assert.assertFalse(p.needMoreInput());
    }

    @Test(expected = IOException.class)
    public void testFeedInputWhenUndecodedBytesRemain() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        // Feeding without reading remaining bytes should throw
        p.feedInput(data, 0, data.length);
    }

    @Test(expected = IOException.class)
    public void testFeedInputEndBeforeStart() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 2, 1);
    }

    @Test(expected = IOException.class)
    public void testFeedInputAfterEndOfInput() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        p.endOfInput();
        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
    }

    @Test(expected = RuntimeException.class)
    public void testDecodeEscapedThrowsInternal() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        p._decodeEscaped();
    }

    @Test
    public void testSimpleValuesChunkByChunk() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "true false null".getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < data.length; i++) {
            p.feedInput(data, i, i + 1);
            while (true) {
                JsonToken t = p.nextToken();
                if (t == JsonToken.NOT_AVAILABLE) {
                    break;
                }
            }
        }
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testFastLiterals() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "[true,false,null]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testBOMHandling() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, (byte) '1'};
        p.feedInput(bom, 0, bom.length);
        p.endOfInput();

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testBOMSplitAcrossFeeds() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] b1 = new byte[]{(byte) 0xEF};
        byte[] b2 = new byte[]{(byte) 0xBB};
        byte[] b3 = new byte[]{(byte) 0xBF, (byte) '\"', (byte) 'o', (byte) 'k', (byte) '\"'};

        p.feedInput(b1, 0, 1);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());

        p.feedInput(b2, 0, 1);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());

        p.feedInput(b3, 0, b3.length);
        p.endOfInput();

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("ok", p.getText());
        Assert.assertNull(p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testCorruptBOMSecondByte() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0x00};
        p.feedInput(bom, 0, bom.length);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testCorruptBOMThirdByte() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0x00};
        p.feedInput(bom, 0, bom.length);
        p.nextToken();
    }

    @Test
    public void testWhitespaceAndNewlines() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, " \r\n\t {\r \n \"a\" : \r\n 123 \r } \n ");

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testNumbersVariousForms() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "[0, -0, 12345, -67890, 0.5, -0.25, 12.34e5, 56.78E-2, -0.0e+1, 1e3]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(12345, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-67890, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.5, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-0.25, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(12.34e5, p.getDoubleValue(), 1.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(56.78e-2, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(1000.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testNumberSplitAcrossFeeds() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] doc = "-12345.678e+9".getBytes(StandardCharsets.UTF_8);

        for (byte b : doc) {
            p.feedInput(new byte[]{b}, 0, 1);
            p.nextToken();
        }
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-12345.678e9, p.getDoubleValue(), 1000.0);
    }

    @Test
    public void testLeadingZerosWhenAllowed() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "[0123, -0456, 007]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-456, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(7, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZerosWhenNotAllowed() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "0123");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZerosNegativeWhenNotAllowed() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "-0123");
        p.nextToken();
    }

    @Test
    public void testNonStandardNumbers() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "[NaN, Infinity, +Infinity, -Infinity]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertTrue(Double.isNaN(p.getDoubleValue()));

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testStringEscapes() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\\u0020z\"]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("\"\\/\b\f\n\r\tA z", p.getText());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testMultiByteUtf8InStrings() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        // 2-byte: 'ä' (\u00E4), 3-byte: '€' (\u20AC), 4-byte: '\uD83D\uDE00' (emoji)
        String unicodeStr = "ä € \uD83D\uDE00";
        feedAll(p, "[\"" + unicodeStr + "\"]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals(unicodeStr, p.getText());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testMultiByteUtf8SplitAcrossFeeds() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        String json = "[\"ä€\uD83D\uDE00\"]";
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        for (byte b : data) {
            p.feedInput(new byte[]{b}, 0, 1);
            p.nextToken();
        }
        p.endOfInput();

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testSingleQuotedStringsAndKeys() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "{'key\\'1': 'val\\'1', 'key\\u0041': 'val\\n2', 'k3': 'ä€\uD83D\uDE00'}");

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("key'1", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val'1", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("keyA", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val\n2", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("k3", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("ä€\uD83D\uDE00", p.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testUnquotedFieldNames() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "{foo: 1, bar_baz$: 2, $id: 3}");

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("foo", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("bar_baz$", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(2, p.getIntValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("$id", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(3, p.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testObjectFieldNamesOfVariousLengths() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        // 1 byte: "a", 4 bytes: "abcd", 8 bytes: "abcdefgh", 12 bytes: "abcdefghijkl", 15 bytes: "abcdefghijklmno"
        feedAll(p, "{\"a\":1, \"abcd\":2, \"abcdefgh\":3, \"abcdefghijkl\":4, \"abcdefghijklmno\":5, \"escaped\\nname\":6, \"\":7}");

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcd", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefgh", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefghijkl", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("abcdefghijklmno", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("escaped\nname", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testTrailingCommasWhenAllowed() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "{\"a\": 1, }");

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());

        NonBlockingJsonParser p2 = createParser(feats);
        feedAll(p2, "[1, 2, ]");
        Assert.assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, p2.nextToken());
    }

    @Test
    public void testMissingValuesWhenAllowed() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_MISSING_VALUES.getMask();
        NonBlockingJsonParser p = createParser(feats);
        feedAll(p, "[1, , 3]");

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(1, p.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(3, p.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testJavaAndYamlComments() throws IOException {
        int feats = JsonParser.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        NonBlockingJsonParser p = createParser(feats);
        String doc = "/* comment */\n"
                + "{\n"
                + "// line comment\n"
                + "# yaml comment\n"
                + "\"key\": /* c-comment */ 123 // trailing\n"
                + "/* block */ , # yaml\n"
                + "\"key2\": 456\n"
                + "}\n"
                + "// final comment";
        feedAll(p, doc);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("key", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("key2", p.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(456, p.getIntValue());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testJavaCommentsWhenDisabled() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "/* comment */ 123");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testYamlCommentsWhenDisabled() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "# comment\n 123");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnrecognizedToken() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "trux");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberExponentWithoutDigit() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "1e");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberDecimalWithoutDigit() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "1.");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberMinusWithoutDigit() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        feedAll(p, "-");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnterminatedCComment() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_COMMENTS.getMask();
        NonBlockingJsonParser p = createParser(feats);
        byte[] data = "/* unclosed".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        p.nextToken();
        p.endOfInput();
        p.nextToken();
    }

    @Test
    public void testEofHandlingOnValidRootNumber() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testEofHandlingOnValidRootZero() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "0".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getIntValue());
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testEofHandlingOnValidRootFloat() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "12.34".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(12.34, p.getDoubleValue(), 0.0001);
        Assert.assertNull(p.nextToken());
    }

    @Test
    public void testEofHandlingOnValidRootKeywords() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        byte[] data = "true".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        p.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertNull(p.nextToken());

        NonBlockingJsonParser p2 = createDefaultParser();
        byte[] data2 = "false".getBytes(StandardCharsets.UTF_8);
        p2.feedInput(data2, 0, data2.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p2.nextToken());
        p2.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_FALSE, p2.nextToken());
        Assert.assertNull(p2.nextToken());

        NonBlockingJsonParser p3 = createDefaultParser();
        byte[] data3 = "null".getBytes(StandardCharsets.UTF_8);
        p3.feedInput(data3, 0, data3.length);
        Assert.assertEquals(JsonToken.NOT_AVAILABLE, p3.nextToken());
        p3.endOfInput();
        Assert.assertEquals(JsonToken.VALUE_NULL, p3.nextToken());
        Assert.assertNull(p3.nextToken());
    }

    @Test
    public void testCloseParser() throws IOException {
        NonBlockingJsonParser p = createDefaultParser();
        Assert.assertFalse(p.isClosed());
        p.close();
        Assert.assertTrue(p.isClosed());
        Assert.assertNull(p.nextToken());
    }
}
