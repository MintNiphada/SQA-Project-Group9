package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

import org.junit.Assert;
import org.junit.Test;

public class UTF8StreamJsonParserTest {

    private UTF8StreamJsonParser createParser(String json, int features) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return createParser(bytes, 0, bytes.length, features, true);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int offset, int length, int features, boolean recyclable) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "sourceRef", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(0).makeChild(JsonParser.Feature.collectDefaults());
        ByteArrayInputStream in = new ByteArrayInputStream(bytes, offset, length);
        byte[] buf = new byte[Math.max(1024, bytes.length)];
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, buf, 0, 0, recyclable);
    }

    private UTF8StreamJsonParser createParserWithInitialBuffer(byte[] bytes, int offset, int length, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "sourceRef", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(0).makeChild(JsonParser.Feature.collectDefaults());
        ByteArrayInputStream in = new ByteArrayInputStream(bytes, offset, length);
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, bytes, offset, length, false);
    }

    @Test
    public void testBasicsAndCodec() throws IOException {
        String json = "{\"key\":\"value\"}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertNull(p.getCodec());
        p.setCodec(null);
        Assert.assertNull(p.getCodec());
        Assert.assertNotNull(p.getInputSource());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("key", p.getText());
        Assert.assertEquals("key", p.getValueAsString());
        Assert.assertEquals("key", p.getValueAsString("def"));
        Assert.assertEquals(0, p.getTextOffset());
        Assert.assertEquals(3, p.getTextLength());
        Assert.assertNotNull(p.getTextCharacters());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("value", p.getText());
        Assert.assertEquals("value", p.getValueAsString());
        Assert.assertEquals(5, p.getTextLength());
        Assert.assertNotNull(p.getTextCharacters());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        Assert.assertNull(p.nextToken());
        Assert.assertNull(p.getText());
        Assert.assertNull(p.getTextCharacters());
        Assert.assertEquals(0, p.getTextLength());
        Assert.assertEquals(0, p.getTextOffset());
        p.close();
    }

    @Test
    public void testNumbersParsing() throws IOException {
        String json = "[ 0, 12345, -6789, 0.125, -12.5e-2, 1E+3, 1e2 ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());

        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(0, p.getValueAsInt());
        Assert.assertEquals(0, p.getIntValue());
        Assert.assertEquals(0L, p.getLongValue());
        Assert.assertEquals("0", p.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(12345, p.getValueAsInt(10));
        Assert.assertEquals(12345, p.getIntValue());
        Assert.assertEquals(12345L, p.getLongValue());
        Assert.assertEquals("12345", p.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-6789, p.getIntValue());
        Assert.assertEquals(-6789L, p.getLongValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(0.125, p.getDoubleValue(), 0.0001);
        Assert.assertEquals(0, p.getValueAsInt());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(-0.125, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(1000.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        Assert.assertEquals(100.0, p.getDoubleValue(), 0.0001);

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testBooleanAndNull() throws IOException {
        String json = "[ true, false, null ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        Assert.assertEquals(Boolean.TRUE, p.nextBooleanValue());

        Assert.assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        Assert.assertEquals("null", p.getText());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testNextMethodsOnFields() throws IOException {
        String json = "{\"s\":\"str\", \"i\":100, \"l\":200, \"b\":true, \"f\":false, \"arr\":[], \"obj\":{}}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals("s", p.nextFieldName());
        Assert.assertEquals("str", p.nextTextValue());

        Assert.assertEquals("i", p.nextFieldName());
        Assert.assertEquals(100, p.nextIntValue(0));

        Assert.assertEquals("l", p.nextFieldName());
        Assert.assertEquals(200L, p.nextLongValue(0L));

        Assert.assertEquals("b", p.nextFieldName());
        Assert.assertEquals(Boolean.TRUE, p.nextBooleanValue());

        Assert.assertEquals("f", p.nextFieldName());
        Assert.assertEquals(Boolean.FALSE, p.nextBooleanValue());

        Assert.assertEquals("arr", p.nextFieldName());
        Assert.assertNull(p.nextTextValue());
        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());

        Assert.assertEquals("obj", p.nextFieldName());
        Assert.assertNull(p.nextBooleanValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testNextFieldNameSerializableString() throws IOException {
        String json = "{\"short\":\"a\", \"mediumName\":\"b\", \"mediumNameNine\":\"c\", \"veryVeryLongFieldNameExceedingTwelveBytes\":\"d\"}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertTrue(p.nextFieldName(new SerializedString("short")));
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("a", p.getText());

        Assert.assertTrue(p.nextFieldName(new SerializedString("mediumName")));
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("b", p.getText());

        Assert.assertTrue(p.nextFieldName(new SerializedString("mediumNameNine")));
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("c", p.getText());

        Assert.assertTrue(p.nextFieldName(new SerializedString("veryVeryLongFieldNameExceedingTwelveBytes")));
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("d", p.getText());

        Assert.assertFalse(p.nextFieldName(new SerializedString("missing")));
        Assert.assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        p.close();
    }

    @Test
    public void testFieldNamesOfDifferentLengths() throws IOException {
        String json = "{"
                + "\"a\":1,"
                + "\"ab\":2,"
                + "\"abc\":3,"
                + "\"abcd\":4,"
                + "\"abcde\":5,"
                + "\"abcdef\":6,"
                + "\"abcdefg\":7,"
                + "\"abcdefgh\":8,"
                + "\"abcdefghi\":9,"
                + "\"abcdefghij\":10,"
                + "\"abcdefghijk\":11,"
                + "\"abcdefghijkl\":12,"
                + "\"abcdefghijklm\":13,"
                + "\"abcdefghijklmnopq\":14"
                + "}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        for (int i = 1; i <= 14; i++) {
            Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            Assert.assertEquals(i, p.getIntValue());
        }
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testEscapesInStringsAndFieldNames() throws IOException {
        String json = "{\"\\\"escaped\\\"\\n\\t\\r\\b\\f\\/\\\\\\u0041\": \"Hello\\n\\tWorld\\u0021\"}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("\"escaped\"\n\t\r\b\f/\\A", p.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("Hello\n\tWorld!", p.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testUtf8MultiByteInStringsAndNames() throws IOException {
        // 2-byte (¢ = \u00A2), 3-byte (€ = \u20AC), 4-byte (𐍈 = \uD800\uDF48 / U+10348)
        String json = "{\"¢_€_𐍈\": \"value_¢_€_𐍈\"}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("¢_€_𐍈", p.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("value_¢_€_𐍈", p.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testSingleQuotesAndUnquotedNames() throws IOException {
        int feats = JsonParser.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();

        String json = "{ unquoted: 'single quoted', 'quotedKey': 'val\\'' }";
        UTF8StreamJsonParser p = createParser(json, feats);
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("unquoted", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("single quoted", p.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("quotedKey", p.getText());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        Assert.assertEquals("val'", p.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testComments() throws IOException {
        int feats = JsonParser.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();

        String json = "/* comment */\n{\n// line comment\n# yaml comment\n\"a\" /* inline */ : /* val */ 123\n}";
        UTF8StreamJsonParser p = createParser(json, feats);
        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        Assert.assertEquals("a", p.getText());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(123, p.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testAllowNonNumericNumbers() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[ NaN, Infinity, +Infinity, -Infinity, +INF, -INF ]";
        UTF8StreamJsonParser p = createParser(json, feats);

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
    }

    @Test
    public void testLeadingZeroesFeature() throws IOException {
        int feats = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        String json = "[ 007, -008 ]";
        UTF8StreamJsonParser p = createParser(json, feats);
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(7, p.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(-8, p.getIntValue());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testBase64Decoding() throws IOException {
        byte[] raw = "Jackson base64 test payload 1234567890".getBytes(StandardCharsets.UTF_8);
        String base64Str = Base64Variants.MIME.encode(raw);
        String json = "[\"" + base64Str + "\"]";

        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(raw, decoded);

        // readBinaryValue
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(raw.length, count);
        Assert.assertArrayEquals(raw, out.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testReadBinaryValueIncremental() throws IOException {
        byte[] raw = new byte[250];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) i;
        }
        String base64Str = Base64Variants.MIME.encode(raw);
        String json = "[\"" + base64Str + "\"]";

        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(raw.length, count);
        Assert.assertArrayEquals(raw, out.toByteArray());

        Assert.assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        String json = "123456789";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParserWithInitialBuffer(bytes, 0, bytes.length, JsonParser.Feature.collectDefaults());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.releaseBuffered(out);
        Assert.assertEquals(bytes.length, count);
        Assert.assertArrayEquals(bytes, out.toByteArray());
        p.close();
    }

    @Test
    public void testLocations() throws IOException {
        String json = "{\n  \"name\": 123\n}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
        JsonLocation loc = p.getCurrentLocation();
        Assert.assertEquals(1, loc.getLineNr());

        Assert.assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        JsonLocation tokenLoc = p.getTokenLocation();
        Assert.assertEquals(2, tokenLoc.getLineNr());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedEndMarker() throws IOException {
        String json = "{\"a\": 1 ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        p.nextToken();
        p.nextToken();
        p.nextToken();
        p.nextToken(); // should throw mismatched closing bracket
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        String json = "{\"a\" 1}";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        p.nextToken();
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberLeadingZero() throws IOException {
        String json = "[ 0123 ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberExponent() throws IOException {
        String json = "[ 1e ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberDecimal() throws IOException {
        String json = "[ 1. ]";
        UTF8StreamJsonParser p = createParser(json, JsonParser.Feature.collectDefaults());
        p.nextToken();
        p.nextToken();
    }

    @Test
    public void testGrowArrayBy() {
        int[] arr = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(arr, 2);
        Assert.assertEquals(5, grown.length);
        Assert.assertEquals(1, grown[0]);
        Assert.assertEquals(2, grown[1]);
        Assert.assertEquals(3, grown[2]);
        Assert.assertEquals(0, grown[3]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        Assert.assertEquals(4, fromNull.length);
    }
}
