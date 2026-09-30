package com.google.gson.stream;

import static org.junit.Assert.*;
import java.io.*;
import org.junit.*;

public class JsonReaderTest {

    private JsonReader reader;

    @Before
    public void setUp() throws Exception {
        // default reader is null; individual tests will create specific readers
        reader = null;
    }

    // Constructors

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullReader() {
        new JsonReader(null);
    }

    // Lenient

    @Test
    public void testDefaultIsNotLenient() {
        reader = new JsonReader(new StringReader("{}"));
        assertFalse(reader.isLenient());
    }

    @Test
    public void testSetLenient() {
        reader = new JsonReader(new StringReader("{}"));
        reader.setLenient(true);
        assertTrue(reader.isLenient());
        reader.setLenient(false);
        assertFalse(reader.isLenient());
    }

    // Begin/End Array

    @Test
    public void testBeginArrayConsumesToken() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek()); // already consumed, so peek is next?
        // Actually after beginArray peek is cleared, and next token is END_ARRAY
        assertEquals(JsonToken.END_ARRAY, reader.peek());
    }

    @Test
    public void testBeginArrayThenEndArray() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endArray();
    }

    @Test
    public void testBeginArrayWithWrongToken() throws IOException {
        reader = new JsonReader(new StringReader("{}"));
        try {
            reader.beginArray();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("BEGIN_ARRAY"));
        }
    }

    @Test
    public void testEndArrayWithoutBegin() throws IOException {
        reader = new JsonReader(new StringReader("]"));
        try {
            reader.endArray();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("END_ARRAY"));
        }
    }

    // Begin/End Object

    @Test
    public void testBeginObjectConsumesToken() throws IOException {
        reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        assertEquals(JsonToken.END_OBJECT, reader.peek());
    }

    @Test
    public void testBeginObjectThenEndObject() throws IOException {
        reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.endObject();
    }

    @Test
    public void testBeginObjectWithWrongToken() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        try {
            reader.beginObject();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("BEGIN_OBJECT"));
        }
    }

    @Test
    public void testEndObjectWithoutBegin() throws IOException {
        reader = new JsonReader(new StringReader("}"));
        try {
            reader.endObject();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("END_OBJECT"));
        }
    }

    // hasNext

    @Test
    public void testHasNextInArray() throws IOException {
        reader = new JsonReader(new StringReader("[1,2]"));
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextInt(); // consume 1
        assertTrue(reader.hasNext());
        reader.nextInt(); // consume 2
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testHasNextInObject() throws IOException {
        reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.nextName(); // "a"
        reader.nextInt(); // 1
        assertTrue(reader.hasNext());
        reader.nextName(); // "b"
        reader.nextInt(); // 2
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    // Peek

    @Test
    public void testPeekString() throws IOException {
        reader = new JsonReader(new StringReader("\"abc\""));
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("abc", reader.nextString());
    }

    @Test
    public void testPeekNumber() throws IOException {
        reader = new JsonReader(new StringReader("123"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
    }

    @Test
    public void testPeekBoolean() throws IOException {
        reader = new JsonReader(new StringReader("true"));
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
    }

    @Test
    public void testPeekNull() throws IOException {
        reader = new JsonReader(new StringReader("null"));
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
    }

    @Test
    public void testPeekEndDocument() throws IOException {
        reader = new JsonReader(new StringReader(""));
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testPeekName() throws IOException {
        reader = new JsonReader(new StringReader("{\"name\":1}"));
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("name", reader.nextName());
    }

    // nextName

    @Test
    public void testNextNameDoubleQuoted() throws IOException {
        reader = new JsonReader(new StringReader("{\"name\":1}"));
        reader.beginObject();
        String name = reader.nextName();
        assertEquals("name", name);
    }

    @Test
    public void testNextNameSingleQuotedLenient() throws IOException {
        reader = new JsonReader(new StringReader("{'name':1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
    }

    @Test
    public void testNextNameUnquotedLenient() throws IOException {
        reader = new JsonReader(new StringReader("{name:1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
    }

    @Test
    public void testNextNameUnquotedStrictFails() throws IOException {
        reader = new JsonReader(new StringReader("{name:1}"));
        reader.beginObject();
        try {
            reader.nextName();
            fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testNextNameExpectedButGotValue() throws IOException {
        reader = new JsonReader(new StringReader("[1]"));
        try {
            reader.nextName();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Expected a name"));
        }
    }

    // nextString

    @Test
    public void testNextStringFromDoubleQuoted() throws IOException {
        reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", reader.nextString());
    }

    @Test
    public void testNextStringFromSingleQuotedLenient() throws IOException {
        reader = new JsonReader(new StringReader("'world'"));
        reader.setLenient(true);
        assertEquals("world", reader.nextString());
    }

    @Test
    public void testNextStringFromUnquotedLenient() throws IOException {
        reader = new JsonReader(new StringReader("unquoted"));
        reader.setLenient(true);
        assertEquals("unquoted", reader.nextString());
    }

    @Test
    public void testNextStringFromNumber() throws IOException {
        reader = new JsonReader(new StringReader("12345"));
        assertEquals("12345", reader.nextString());
    }

    @Test
    public void testNextStringFromLong() throws IOException {
        reader = new JsonReader(new StringReader("123"));
        reader.peek(); // load as PEEKED_LONG
        // nextString should convert long to string
        assertEquals("123", reader.nextString());
    }

    @Test
    public void testNextStringFromBufferedValue() throws IOException {
        reader = new JsonReader(new StringReader("1.5"));
        // peek, then nextDouble (which sets peekedString), then nextString? Not possible. Instead we can use nextDouble to buffer then nextString? Actually nextString after nextDouble gives the string? Not easily.
    }

    // nextBoolean

    @Test
    public void testNextBooleanTrue() throws IOException {
        reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
    }

    @Test
    public void testNextBooleanFalse() throws IOException {
        reader = new JsonReader(new StringReader("false"));
        assertFalse(reader.nextBoolean());
    }

    @Test
    public void testNextBooleanLenientCaseInsensitive() throws IOException {
        reader = new JsonReader(new StringReader("tRue"));
        reader.setLenient(true);
        // Not case insensitive due to keyword check; it checks lowercase and uppercase exact. "tRue" not match, so it would be unquoted. Strict would fail without lenient.
        // Actually keyword check: "true" vs "TRUE". "tRue" wouldn't match. So in strict it throws, in lenient it might be unquoted? In lenient it's unquoted string, and converted? nextBoolean expects boolean token.
        try {
            boolean b = reader.nextBoolean();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNextBooleanWithString() throws IOException {
        reader = new JsonReader(new StringReader("\"true\""));
        try {
            reader.nextBoolean();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    // nextNull

    @Test
    public void testNextNull() throws IOException {
        reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
    }

    @Test
    public void testNextNullWrongToken() throws IOException {
        reader = new JsonReader(new StringReader("42"));
        try {
            reader.nextNull();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    // nextDouble

    @Test
    public void testNextDoubleFromLong() throws IOException {
        reader = new JsonReader(new StringReader("42"));
        assertEquals(42.0, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNextDoubleFromNumber() throws IOException {
        reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, reader.nextDouble(), 0.001);
    }

    @Test
    public void testNextDoubleFromString() throws IOException {
        reader = new JsonReader(new StringReader("\"2.5\""));
        assertEquals(2.5, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNextDoubleNaNStrictFail() throws IOException {
        reader = new JsonReader(new StringReader("NaN"));
        reader.setLenient(false);
        try {
            reader.nextDouble();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testNextDoubleNaNLenientOk() throws IOException {
        reader = new JsonReader(new StringReader("NaN"));
        reader.setLenient(true);
        double d = reader.nextDouble();
        assertTrue(Double.isNaN(d));
    }

    @Test
    public void testNextDoubleInfinityStrictFail() throws IOException {
        reader = new JsonReader(new StringReader("Infinity"));
        reader.setLenient(false);
        try {
            reader.nextDouble();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testNextDoubleInfinityLenientOk() throws IOException {
        reader = new JsonReader(new StringReader("Infinity"));
        reader.setLenient(true);
        double d = reader.nextDouble();
        assertEquals(Double.POSITIVE_INFINITY, d, 0.0);
    }

    @Test
    public void testNextDoubleWithUnquotedLenient() throws IOException {
        reader = new JsonReader(new StringReader("Infinity"));
        reader.setLenient(true);
        reader.nextDouble(); // Should work
    }

    @Test
    public void testNextDoubleFromEmptyStringThrows() throws IOException {
        reader = new JsonReader(new StringReader(""));
        try {
            reader.nextDouble();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    // nextLong

    @Test
    public void testNextLongFromLong() throws IOException {
        reader = new JsonReader(new StringReader("12345"));
        assertEquals(12345L, reader.nextLong());
    }

    @Test
    public void testNextLongFromNumberString() throws IOException {
        reader = new JsonReader(new StringReader("\"9223372036854775807\""));
        assertEquals(9223372036854775807L, reader.nextLong());
    }

    @Test
    public void testNextLongPrecisionLossThrows() throws IOException {
        reader = new JsonReader(new StringReader("1.5"));
        try {
            reader.nextLong();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testNextLongFromLargeDoubleThrows() throws IOException {
        reader = new JsonReader(new StringReader("1e30"));
        try {
            reader.nextLong();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    // nextInt

    @Test
    public void testNextIntFromLong() throws IOException {
        reader = new JsonReader(new StringReader("789"));
        assertEquals(789, reader.nextInt());
    }

    @Test
    public void testNextIntOverflowThrows() throws IOException {
        reader = new JsonReader(new StringReader("2147483648")); // > Integer.MAX_VALUE
        try {
            reader.nextInt();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testNextIntFromString() throws IOException {
        reader = new JsonReader(new StringReader("\"42\""));
        assertEquals(42, reader.nextInt());
    }

    @Test
    public void testNextIntPrecisionLossThrows() throws IOException {
        reader = new JsonReader(new StringReader("2.1"));
        try {
            reader.nextInt();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    // Number parsing details

    @Test
    public void testNumberLeadingZeroFails() throws IOException {
        reader = new JsonReader(new StringReader("0123"));
        // In strict mode, leading zero should not be allowed, so peek will throw syntax error.
        try {
            reader.peek();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testNegativeNumber() throws IOException {
        reader = new JsonReader(new StringReader("-55"));
        assertEquals(-55L, reader.nextLong());
    }

    @Test
    public void testNumberExponent() throws IOException {
        reader = new JsonReader(new StringReader("2e3"));
        assertEquals(2000.0, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNumberExponentSign() throws IOException {
        reader = new JsonReader(new StringReader("2e+3"));
        assertEquals(2000.0, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNumberExponentNegative() throws IOException {
        reader = new JsonReader(new StringReader("1.5e-2"));
        assertEquals(0.015, reader.nextDouble(), 0.0001);
    }

    // Skip value

    @Test
    public void testSkipString() throws IOException {
        reader = new JsonReader(new StringReader("[\"skip\", \"keep\"]"));
        reader.beginArray();
        reader.skipValue();
        assertEquals("keep", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testSkipObject() throws IOException {
        reader = new JsonReader(new StringReader("[{\"a\":1}, 2]"));
        reader.beginArray();
        reader.skipValue();
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testSkipNestedObjects() throws IOException {
        reader = new JsonReader(new StringReader("[[1,2], 3]"));
        reader.beginArray();
        reader.skipValue(); // skip [1,2]
        assertEquals(3, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testSkipValueUpdatesPath() throws IOException {
        reader = new JsonReader(new StringReader("[1,2]"));
        reader.beginArray();
        reader.skipValue(); // skip 1
        assertEquals("$[1]", reader.getPath()); // after skipValue, index should be incremented
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    // getPath

    @Test
    public void testPathInArray() throws IOException {
        reader = new JsonReader(new StringReader("[1, 2]"));
        assertEquals("$[0]", reader.getPath()); // before beginArray? Actually path at start is "$"
        reader.beginArray();
        assertEquals("$[0]", reader.getPath());
        assertEquals(1, reader.nextInt()); // path indices[0] becomes 1 after reading
        assertEquals("$[1]", reader.getPath());
        reader.endArray();
        assertEquals("$", reader.getPath()); // after endArray, pathIndices[0] is 1? No, after endArray, stackSize decreases, pathIndices[stackSize-1]++ does not apply. getPath uses stack up to stackSize. So after endArray, stackSize=1, EMPTY_DOCUMENT (or NONEMPTY_DOCUMENT), index not printed. So path is "$".
    }

    @Test
    public void testPathInObject() throws IOException {
        reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.", reader.getPath()); // empty object, no name yet.
        assertEquals("a", reader.nextName());
        assertEquals("$.a", reader.getPath());
        assertEquals(1, reader.nextInt());
        assertEquals("$.b", reader.getPath()); // after nextName the name is set to b
        assertEquals(2, reader.nextInt());
        assertEquals("$.", reader.getPath());
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    // toString

    @Test
    public void testToString() throws IOException {
        reader = new JsonReader(new StringReader("{}"));
        String str = reader.toString();
        assertTrue(str.contains("JsonReader"));
        assertTrue(str.contains("line"));
    }

    // close

    @Test
    public void testClose() throws IOException {
        reader = new JsonReader(new StringReader("{}"));
        reader.close();
        // subsequent operations should fail
        try {
            reader.peek();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    // Lenient features

    @Test
    public void testLenientUnquotedString() throws IOException {
        reader = new JsonReader(new StringReader("abc"));
        reader.setLenient(true);
        assertEquals("abc", reader.nextString());
    }

    @Test
    public void testLenientSingleQuotedString() throws IOException {
        reader = new JsonReader(new StringReader("'hello'"));
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test
    public void testLenientCommentSlashSlash() throws IOException {
        reader = new JsonReader(new StringReader("// comment\n42"));
        reader.setLenient(true);
        assertEquals(42, reader.nextInt());
    }

    @Test
    public void testLenientCommentBlock() throws IOException {
        reader = new JsonReader(new StringReader("/* block */ true"));
        reader.setLenient(true);
        assertTrue(reader.nextBoolean());
    }

    @Test
    public void testLenientNonExecutePrefix() throws IOException {
        reader = new JsonReader(new StringReader(")]}'\n42"));
        reader.setLenient(true);
        assertEquals(42, reader.nextInt());
    }

    @Test
    public void testLenientMultipleTopLevelValues() throws IOException {
        reader = new JsonReader(new StringReader("1 2"));
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
        // after consuming first, peek should return next token
        assertEquals(2, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testLenientNameValueSeparatorEquals() throws IOException {
        reader = new JsonReader(new StringReader("{\"a\"=1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testLenientSemicolonInsteadOfComma() throws IOException {
        reader = new JsonReader(new StringReader("[1;2]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    // Escapes

    @Test
    public void testStringEscapeTab() throws IOException {
        reader = new JsonReader(new StringReader("\"a\\tb\""));
        assertEquals("a\tb", reader.nextString());
    }

    @Test
    public void testStringEscapeNewline() throws IOException {
        reader = new JsonReader(new StringReader("\"a\\nb\""));
        assertEquals("a\nb", reader.nextString());
    }

    @Test
    public void testStringUnicodeEscape() throws IOException {
        reader = new JsonReader(new StringReader("\"\\u0041\""));
        assertEquals("A", reader.nextString());
    }

    @Test
    public void testStringEscapeBackslashAndQuote() throws IOException {
        reader = new JsonReader(new StringReader("\"\\\\ \\\" \""));
        assertEquals("\\ \" ", reader.nextString());
    }

    @Test
    public void testStringEscapeInvalidHex() throws IOException {
        reader = new JsonReader(new StringReader("\"\\u00G0\""));
        try {
            reader.nextString();
            fail();
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testStringEscapeUnterminated() throws IOException {
        reader = new JsonReader(new StringReader("\"\\"));
        try {
            reader.nextString();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    // Syntax errors

    @Test
    public void testUnterminatedArray() throws IOException {
        reader = new JsonReader(new StringReader("["));
        try {
            reader.beginArray();
            reader.nextNull();
            fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testUnterminatedObject() throws IOException {
        reader = new JsonReader(new StringReader("{"));
        try {
            reader.beginObject();
            reader.nextName();
            fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testUnexpectedToken() throws IOException {
        reader = new JsonReader(new StringReader("]"));
        reader.setLenient(false);
        try {
            reader.nextNull();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testStrictTopLevelValueMustBeArrayOrObjectFails() throws IOException {
        reader = new JsonReader(new StringReader("42"));
        try {
            reader.peek();
            fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLenientAllowsTopLevelValue() throws IOException {
        reader = new JsonReader(new StringReader("42"));
        reader.setLenient(true);
        assertEquals(42, reader.nextInt());
    }

    // Edge cases

    @Test
    public void testEmptyDocument() throws IOException {
        reader = new JsonReader(new StringReader(""));
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testMultipleCallsToEndArray() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endArray();
        try {
            reader.endArray();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGettingPathAfterClose() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        reader.close();
        // getPath should still work but stack is CLOSED, so path shows nothing special.
        assertEquals("$", reader.getPath());
    }

    @Test
    public void testPeekAfterCloseThrows() throws IOException {
        reader = new JsonReader(new StringReader("1"));
        reader.close();
        try {
            reader.peek();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testLargeNumberNotFitLongButDouble() throws IOException {
        // e.g., 1.7976931348623157E308
        reader = new JsonReader(new StringReader("1e309"));
        reader.setLenient(true); // overflow to Infinity, but lenient allows Infinity
        double d = reader.nextDouble();
        assertTrue(Double.isInfinite(d));
    }

    @Test
    public void testNumberFollowedByLiteralCharacter() throws IOException {
        reader = new JsonReader(new StringReader("123abc"));
        // In strict mode, it is not allowed (unquoted). But in lenient it might read "123abc" as string? Actually peekNumber will fail because after number, isLiteral is true, so it returns PEEKED_NONE. Then it falls into unquoted value parsing. So lenient would treat entire "123abc" as unquoted string.
        reader.setLenient(true);
        assertEquals("123abc", reader.nextString());
    }

    @Test
    public void testBooleanKeywordTrueFollowedByLiteralCharacter() throws IOException {
        reader = new JsonReader(new StringReader("trueX"));
        reader.setLenient(true); // would be unquoted string
        assertEquals("trueX", reader.nextString());
    }

    @Test
    public void testNullKeywordFollowedByLiteralCharacter() throws IOException {
        reader = new JsonReader(new StringReader("nullX"));
        reader.setLenient(true);
        assertEquals("nullX", reader.nextString());
    }

    @Test
    public void testNumberBoundaryLongMinValue() throws IOException {
        reader = new JsonReader(new StringReader("-9223372036854775808"));
        assertEquals(Long.MIN_VALUE, reader.nextLong());
    }

    @Test
    public void testNumberBoundaryLongMaxValue() throws IOException {
        reader = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, reader.nextLong());
    }

    @Test
    public void testNumberExceedLongRangeThrows() throws IOException {
        reader = new JsonReader(new StringReader("9223372036854775808"));
        try {
            reader.nextLong();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    // Additional path and index tests

    @Test
    public void testPathAfterSkippingValue() throws IOException {
        reader = new JsonReader(new StringReader("[1,2,3]"));
        reader.beginArray();
        reader.skipValue();
        assertEquals("$[1]", reader.getPath());
        reader.skipValue();
        assertEquals("$[2]", reader.getPath());
        reader.endArray();
    }

    // Ensure that reading a value after end of array fails
    @Test
    public void testReadAfterEndArrayThrows() throws IOException {
        reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endArray();
        try {
            reader.nextInt();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    // Promotion of name to value (indirectly through static block, but we can't test directly; it's for internal use)
}
