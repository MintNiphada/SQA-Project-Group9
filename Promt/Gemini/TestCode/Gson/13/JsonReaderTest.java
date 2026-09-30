package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import org.junit.Assert;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class JsonReaderTest {

    @Test(expected = NullPointerException.class)
    public void testNullReaderThrowsException() {
        new JsonReader(null);
    }

    @Test
    public void testLenientGetterSetter() {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        Assert.assertFalse(reader.isLenient());
        reader.setLenient(true);
        Assert.assertTrue(reader.isLenient());
    }

    @Test
    public void testEmptyDocument() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(""));
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testCustomNonExecutePrefix() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(")]}'\n{\"key\":\"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("key", reader.nextName());
        Assert.assertEquals("value", reader.nextString());
        reader.endObject();
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testNonExecutePrefixPartial() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(")]}"));
        reader.setLenient(true);
        try {
            reader.peek();
            Assert.fail();
        } catch (MalformedJsonException expected) {
            // Expected syntax error since prefix didn't match and was unquoted
        }
    }

    @Test
    public void testBomStripping() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\ufeff[true]"));
        reader.beginArray();
        Assert.assertTrue(reader.nextBoolean());
        reader.endArray();
    }

    @Test
    public void testBooleans() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[true, false, TRUE, FALSE]"));
        reader.beginArray();
        Assert.assertTrue(reader.nextBoolean());
        Assert.assertFalse(reader.nextBoolean());
        Assert.assertTrue(reader.nextBoolean());
        Assert.assertFalse(reader.nextBoolean());
        reader.endArray();
    }

    @Test
    public void testNullLiteral() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[null, NULL]"));
        reader.beginArray();
        Assert.assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        Assert.assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        reader.endArray();
    }

    @Test
    public void testKeywordsNotExact() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[trues, falses, nulls]"));
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertEquals("trues", reader.nextString());
        Assert.assertEquals("falses", reader.nextString());
        Assert.assertEquals("nulls", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testNumbers() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[0, -0, 123, -123, 9223372036854775807, -9223372036854775808, 1.25, -1.25, 1e2, 1e+2, 1e-2, 1E2]"));
        reader.beginArray();
        Assert.assertEquals(0, reader.nextInt());
        Assert.assertEquals(0, reader.nextInt());
        Assert.assertEquals(123, reader.nextInt());
        Assert.assertEquals(-123, reader.nextInt());
        Assert.assertEquals(9223372036854775807L, reader.nextLong());
        Assert.assertEquals(-9223372036854775808L, reader.nextLong());
        Assert.assertEquals(1.25, reader.nextDouble(), 0.0);
        Assert.assertEquals(-1.25, reader.nextDouble(), 0.0);
        Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
        Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
        Assert.assertEquals(0.01, reader.nextDouble(), 0.0);
        Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test
    public void testNumberOverflowFitsInLong() throws IOException {
        // Exceeds Long.MAX_VALUE but parsed as double/string in lenient mode
        JsonReader reader = new JsonReader(new StringReader("[9223372036854775808]"));
        reader.beginArray();
        Assert.assertEquals(JsonToken.NUMBER, reader.peek());
        Assert.assertEquals(9223372036854775808d, reader.nextDouble(), 1.0);
        reader.endArray();
    }

    @Test
    public void testLeadingZeroNumberDisallowedInStrict() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[0123]"));
        reader.beginArray();
        try {
            reader.nextInt();
            Assert.fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testStringsWithEscapes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"\\\"\", \"\\\\\", \"\\/\", \"\\b\", \"\\f\", \"\\n\", \"\\r\", \"\\t\", \"\\u0041\", \"\\u0061\", \"\\u000a\"]"));
        reader.beginArray();
        Assert.assertEquals("\"", reader.nextString());
        Assert.assertEquals("\\", reader.nextString());
        Assert.assertEquals("/", reader.nextString());
        Assert.assertEquals("\b", reader.nextString());
        Assert.assertEquals("\f", reader.nextString());
        Assert.assertEquals("\n", reader.nextString());
        Assert.assertEquals("\r", reader.nextString());
        Assert.assertEquals("\t", reader.nextString());
        Assert.assertEquals("A", reader.nextString());
        Assert.assertEquals("a", reader.nextString());
        Assert.assertEquals("\n", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testSingleQuotedStrings() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("['hello', 'world \\' escaped']"));
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertEquals("hello", reader.nextString());
        Assert.assertEquals("world ' escaped", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testUnquotedStringsAndNames() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{name: value, unquoted_key: 123}"));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("name", reader.nextName());
        Assert.assertEquals("value", reader.nextString());
        Assert.assertEquals("unquoted_key", reader.nextName());
        Assert.assertEquals(123, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testSingleQuotedNames() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{'key': 'value'}"));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("key", reader.nextName());
        Assert.assertEquals("value", reader.nextString());
        reader.endObject();
    }

    @Test
    public void testNameSeparatorsLenient() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\" = 1; \"b\" => 2}"));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("a", reader.nextName());
        Assert.assertEquals(1, reader.nextInt());
        Assert.assertEquals("b", reader.nextName());
        Assert.assertEquals(2, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testArraySeparatorsLenient() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1; 2, 3;]"));
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertEquals(1, reader.nextInt());
        Assert.assertEquals(2, reader.nextInt());
        Assert.assertEquals(3, reader.nextInt());
        Assert.assertNull(reader.nextString()); // empty element becomes null
        reader.endArray();
    }

    @Test
    public void testComments() throws IOException {
        String json = "// comment line\n"
                + "# hash comment\n"
                + "{\n"
                + "  /* multi\n"
                + "     line\n"
                + "     comment */\n"
                + "  \"key\": /* comment */ \"value\" // end line\n"
                + "}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("key", reader.nextName());
        Assert.assertEquals("value", reader.nextString());
        reader.endObject();
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testPathTracking() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\": [1, {\"b\": 2}], \"c\": 3}"));
        Assert.assertEquals("$", reader.getPath());
        reader.beginObject();
        Assert.assertEquals("$.", reader.getPath());
        Assert.assertEquals("a", reader.nextName());
        Assert.assertEquals("$.a", reader.getPath());
        reader.beginArray();
        Assert.assertEquals("$.a[0]", reader.getPath());
        Assert.assertEquals(1, reader.nextInt());
        Assert.assertEquals("$.a[1]", reader.getPath());
        reader.beginObject();
        Assert.assertEquals("$.a[1].", reader.getPath());
        Assert.assertEquals("b", reader.nextName());
        Assert.assertEquals("$.a[1].b", reader.getPath());
        Assert.assertEquals(2, reader.nextInt());
        reader.endObject();
        Assert.assertEquals("$.a[2]", reader.getPath());
        reader.endArray();
        Assert.assertEquals("$.a", reader.getPath());
        Assert.assertEquals("c", reader.nextName());
        Assert.assertEquals("$.c", reader.getPath());
        Assert.assertEquals(3, reader.nextInt());
        reader.endObject();
        Assert.assertEquals("$", reader.getPath());
    }

    @Test
    public void testSkipValueScalarsAndStructures() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"skip1\": [1, 2, {\"k\": \"v\"}], \"skip2\": \"str\", \"skip3\": 123, \"keep\": true}"));
        reader.beginObject();
        Assert.assertEquals("skip1", reader.nextName());
        reader.skipValue();
        Assert.assertEquals("skip2", reader.nextName());
        reader.skipValue();
        Assert.assertEquals("skip3", reader.nextName());
        reader.skipValue();
        Assert.assertEquals("keep", reader.nextName());
        Assert.assertTrue(reader.nextBoolean());
        reader.endObject();
    }

    @Test
    public void testSkipValueSingleQuotedAndUnquoted() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{k1: 'v1', k2: unquotedVal}"));
        reader.setLenient(true);
        reader.beginObject();
        Assert.assertEquals("k1", reader.nextName());
        reader.skipValue();
        Assert.assertEquals("k2", reader.nextName());
        reader.skipValue();
        reader.endObject();
    }

    @Test
    public void testNextIntAndLongFromDoubleString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"123\", \"456.0\", \"-789.0\"]"));
        reader.beginArray();
        Assert.assertEquals(123, reader.nextInt());
        Assert.assertEquals(456L, reader.nextLong());
        Assert.assertEquals(-789, reader.nextInt());
        reader.endArray();
    }

    @Test(expected = NumberFormatException.class)
    public void testNextIntPrecisionLoss() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[123.45]"));
        reader.beginArray();
        reader.nextInt();
    }

    @Test(expected = NumberFormatException.class)
    public void testNextLongPrecisionLoss() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[123.45]"));
        reader.beginArray();
        reader.nextLong();
    }

    @Test(expected = MalformedJsonException.class)
    public void testStrictNanDoubleThrows() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[NaN]"));
        reader.beginArray();
        reader.nextDouble();
    }

    @Test
    public void testLenientNanDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[NaN, Infinity, -Infinity]"));
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertTrue(Double.isNaN(reader.nextDouble()));
        Assert.assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test
    public void testDeepNestingStackGrowth() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append("[");
        }
        sb.append("1");
        for (int i = 0; i < 40; i++) {
            sb.append("]");
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        for (int i = 0; i < 40; i++) {
            reader.beginArray();
        }
        Assert.assertEquals(1, reader.nextInt());
        for (int i = 0; i < 40; i++) {
            reader.endArray();
        }
    }

    @Test
    public void testBufferSpanLongUnquotedString() throws IOException {
        char[] large = new char[2048];
        Arrays.fill(large, 'a');
        String longStr = new String(large);
        JsonReader reader = new JsonReader(new StringReader("[" + longStr + "]"));
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertEquals(longStr, reader.nextString());
        reader.endArray();
    }

    @Test
    public void testBufferSpanLongQuotedString() throws IOException {
        char[] large = new char[2048];
        Arrays.fill(large, 'b');
        String longStr = new String(large);
        JsonReader reader = new JsonReader(new StringReader("[\"" + longStr + "\"]"));
        reader.beginArray();
        Assert.assertEquals(longStr, reader.nextString());
        reader.endArray();
    }

    @Test(expected = MalformedJsonException.class)
    public void testUnterminatedQuotedStringThrows() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"unfinished"));
        reader.beginArray();
        reader.nextString();
    }

    @Test(expected = MalformedJsonException.class)
    public void testInvalidEscapeSequence() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"\\q\"]"));
        reader.beginArray();
        reader.nextString();
    }

    @Test(expected = NumberFormatException.class)
    public void testMalformedUnicodeEscape() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"\\u12G4\"]"));
        reader.beginArray();
        reader.nextString();
    }

    @Test(expected = IllegalStateException.class)
    public void testClosedReaderThrowsOnAction() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.close();
        reader.peek();
    }

    @Test
    public void testPromoteNameToValueInternalAccess() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"key1\": \"value1\", 'key2': 2, key3: 3}"));
        reader.setLenient(true);
        reader.beginObject();

        JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
        Assert.assertEquals(JsonToken.STRING, reader.peek());
        Assert.assertEquals("key1", reader.nextString());
        Assert.assertEquals("value1", reader.nextString());

        JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
        Assert.assertEquals(JsonToken.STRING, reader.peek());
        Assert.assertEquals("key2", reader.nextString());
        Assert.assertEquals(2, reader.nextInt());

        JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
        Assert.assertEquals(JsonToken.STRING, reader.peek());
        Assert.assertEquals("key3", reader.nextString());
        Assert.assertEquals(3, reader.nextInt());

        reader.endObject();
    }

    @Test
    public void testToStringAndLocation() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\n  123\n]"));
        Assert.assertTrue(reader.toString().contains("JsonReader"));
        reader.beginArray();
        Assert.assertEquals(123, reader.nextInt());
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedBeginArrayButObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedBeginObjectButArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedEndArrayButObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedEndObjectButArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedBooleanButNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[123]"));
        reader.beginArray();
        reader.nextBoolean();
    }

    @Test(expected = IllegalStateException.class)
    public void testExpectedNullButString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"hello\"]"));
        reader.beginArray();
        reader.nextNull();
    }

    @Test(expected = EOFException.class)
    public void testNextNonWhitespaceEof() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("   "));
        reader.beginArray();
    }

    @Test(expected = MalformedJsonException.class)
    public void testUnterminatedComment() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("/* comment without end"));
        reader.setLenient(true);
        reader.peek();
    }

    @Test
    public void testMultipleTopLevelLenient() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1 2 3"));
        reader.setLenient(true);
        Assert.assertEquals(1, reader.nextInt());
        Assert.assertEquals(2, reader.nextInt());
        Assert.assertEquals(3, reader.nextInt());
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(expected = MalformedJsonException.class)
    public void testMultipleTopLevelStrictFails() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1 2"));
        Assert.assertEquals(1, reader.nextInt());
        reader.nextInt();
    }

    @Test
    public void testSingleSlashNotComment() throws IOException {
        // Reader encounters '/' but not followed by '/' or '*'
        Reader r = new Reader() {
            private final String data = "[/a]";
            private int index = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (index >= data.length()) return -1;
                cbuf[off] = data.charAt(index++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        JsonReader reader = new JsonReader(r);
        reader.setLenient(true);
        reader.beginArray();
        Assert.assertEquals("/a", reader.nextString());
        reader.endArray();
    }
}
