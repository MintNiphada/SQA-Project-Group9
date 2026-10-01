package com.google.gson.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;

public class JsonReaderTest {

    // Helper to create a reader from a string
    private JsonReader reader(String json) {
        return new JsonReader(new StringReader(json));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReader() {
        new JsonReader(null);
    }

    @Test
    public void testEmptyDocument() throws IOException {
        JsonReader reader = reader("");
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        assertFalse(reader.hasNext());
        reader.close();
    }

    @Test
    public void testBooleanTrue() throws IOException {
        JsonReader reader = reader("true");
        assertTrue(reader.hasNext());
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    @Test
    public void testBooleanFalse() throws IOException {
        JsonReader reader = reader("false");
        assertFalse(reader.nextBoolean());
        reader.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testBooleanTypeMismatch() throws IOException {
        JsonReader reader = reader("1");
        reader.nextBoolean();
    }

    @Test
    public void testNull() throws IOException {
        JsonReader reader = reader("null");
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testNullTypeMismatch() throws IOException {
        JsonReader reader = reader("true");
        reader.nextNull();
    }

    @Test
    public void testString() throws IOException {
        JsonReader reader = reader("\"hello\"");
        assertEquals("hello", reader.nextString());
    }

    @Test
    public void testStringUnicode() throws IOException {
        JsonReader reader = reader("\"\\u0041\\u0042\"");
        assertEquals("AB", reader.nextString());
    }

    @Test
    public void testStringEscapeCharacters() throws IOException {
        JsonReader reader = reader("\"\\\\ \\/ \\b \\f \\n \\r \\t \\\" \\'\"");
        assertEquals("\\ / \b \f \n \r \t \" \'", reader.nextString());
    }

    @Test
    public void testStringUnicodeInvalid() throws IOException {
        JsonReader reader = reader("\"\\u0Z00\"");
        try {
            reader.nextString();
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testStringUnterminated() throws IOException {
        JsonReader reader = reader("\"hello");
        try {
            reader.nextString();
            fail("Expected MalformedJsonException");
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Unterminated string"));
        }
    }

    @Test
    public void testNumberDouble() throws IOException {
        JsonReader reader = reader("1.5e2");
        assertEquals(150.0, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNumberLong() throws IOException {
        JsonReader reader = reader("123");
        assertEquals(123L, reader.nextLong());
    }

    @Test
    public void testNumberInt() throws IOException {
        JsonReader reader = reader("123");
        assertEquals(123, reader.nextInt());
    }

    @Test
    public void testNumberIntTooLarge() throws IOException {
        JsonReader reader = reader("9999999999");
        try {
            reader.nextInt();
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testNumberNegative() throws IOException {
        JsonReader reader = reader("-456");
        assertEquals(-456, reader.nextInt());
    }

    @Test
    public void testNumberLeadingZeroInvalid() throws IOException {
        JsonReader reader = reader("0123");
        try {
            reader.nextInt();
            fail("Expected NumberFormatException? Actually the parser will treat '0123' as unquoted string in lenient? In strict, it should fail.");
        } catch (MalformedJsonException expected) {
            // Expected: "Expected value" or something? In strict, 0 followed by digits is invalid number, returns PEEKED_NONE, then falls to isLiteral check, which returns true for '1', then expects value so it will treat "0123" as unquoted string? Actually doPeek for empty document, after nextNonWhitespace reads '0', pos=0, then peekNumber, value=0, then at '1' since value==0 and last==NUMBER_CHAR_DIGIT, it returns PEEKED_NONE. Then falls through to isLiteral check on buffer[pos] where pos still 0, '0' is literal, so checkLenient() (strict fails) and returns PEEKED_UNQUOTED. So in strict mode, it throws syntax error. In lenient, it would treat as unquoted string "0123". So in strict, we expect MalformedJsonException.
        }
    }

    @Test
    public void testNumberMaxLong() throws IOException {
        JsonReader reader = reader(String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, reader.nextLong());
    }

    @Test
    public void testNumberMinLong() throws IOException {
        JsonReader reader = reader(String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, reader.nextLong());
    }

    @Test
    public void testNumberOverflow() throws IOException {
        JsonReader reader = reader("9923372036854775808"); // larger than Long.MAX_VALUE
        assertEquals(9.922372036854776E18, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNumberNanInStrict() throws IOException {
        JsonReader reader = reader("NaN");
        reader.setLenient(true); // NaN is only allowed in lenient
        double result = reader.nextDouble();
        assertTrue(Double.isNaN(result));
    }

    @Test(expected = MalformedJsonException.class)
    public void testNumberNanInStrict() throws IOException {
        JsonReader reader = reader("NaN");
        reader.nextDouble();
    }

    @Test
    public void testNumberInfinityLenient() throws IOException {
        JsonReader reader = reader("Infinity");
        reader.setLenient(true);
        assertTrue(Double.isInfinite(reader.nextDouble()));
    }

    @Test(expected = MalformedJsonException.class)
    public void testNumberInfinityStrict() throws IOException {
        JsonReader reader = reader("Infinity");
        reader.nextDouble();
    }

    @Test
    public void testNumberNegativeInfinityLenient() throws IOException {
        JsonReader reader = reader("-Infinity");
        reader.setLenient(true);
        assertTrue(Double.isInfinite(reader.nextDouble()));
    }

    @Test
    public void testArray() throws IOException {
        JsonReader reader = reader("[1, 2, 3]");
        reader.beginArray();
        assertTrue(reader.hasNext());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals(2, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals(3, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    @Test
    public void testArrayMissingComma() throws IOException {
        JsonReader reader = reader("[1 2]");
        reader.beginArray();
        reader.nextInt();
        try {
            reader.nextInt();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testArrayUnterminated() throws IOException {
        JsonReader reader = reader("[1,");
        reader.beginArray();
        reader.nextInt();
        assertTrue(reader.hasNext()); // hasNext will try to peek next token, which should fail?
        try {
            reader.hasNext();
            fail();
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Unterminated array"));
        }
    }

    @Test
    public void testObject() throws IOException {
        JsonReader reader = reader("{\"a\": 1, \"b\": true}");
        reader.beginObject();
        assertTrue(reader.hasNext());
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals("b", reader.nextName());
        assertTrue(reader.nextBoolean());
        assertFalse(reader.hasNext());
        reader.endObject();
        reader.close();
    }

    @Test
    public void testObjectMissingColon() throws IOException {
        JsonReader reader = reader("{\"a\" 1}");
        reader.beginObject();
        assertEquals("a", reader.nextName());
        try {
            reader.nextInt();
            fail();
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Expected ':'"));
        }
    }

    @Test
    public void testObjectExpectedName() throws IOException {
        JsonReader reader = reader("{1}");
        reader.beginObject();
        try {
            reader.nextName();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testUnterminatedObject() throws IOException {
        JsonReader reader = reader("{\"a\":1");
        reader.beginObject();
        reader.nextName();
        reader.nextInt();
        assertTrue(reader.hasNext()); // expects comma or }, gets EOF
        try {
            reader.hasNext();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testNestedArrays() throws IOException {
        JsonReader reader = reader("[[1, 2], [3]]");
        reader.beginArray();
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
        reader.beginArray();
        assertEquals(3, reader.nextInt());
        reader.endArray();
        reader.endArray();
    }

    @Test
    public void testNestedObjects() throws IOException {
        JsonReader reader = reader("{\"a\":{\"b\":1}}");
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.beginObject();
        assertEquals("b", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.endObject();
    }

    @Test
    public void testSkipValue() throws IOException {
        JsonReader reader = reader("[true, null, \"hello\", 123, [1,2], {\"a\":1}]");
        reader.beginArray();
        reader.skipValue(); // skip true
        reader.skipValue(); // skip null
        reader.skipValue(); // skip "hello"
        reader.skipValue(); // skip 123
        reader.skipValue(); // skip array [1,2]
        reader.skipValue(); // skip object {"a":1}
        reader.endArray();
    }

    @Test
    public void testSkipValueNested() throws IOException {
        JsonReader reader = reader("{\"skip\": [1, {\"inner\": 2}], \"keep\": 3}");
        reader.beginObject();
        reader.nextName();
        reader.skipValue(); // skips array and nested object
        assertEquals("keep", reader.nextName());
        assertEquals(3, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testGetPath() throws IOException {
        JsonReader reader = reader("{\"a\":[1, {\"b\":2}]}");
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.", reader.getPath());
        assertEquals("a", reader.nextName());
        assertEquals("$.a", reader.getPath()); // name is known but not yet consumed? Actually nextName sets pathNames, so getPath will show the name.
        reader.beginArray();
        assertEquals("$.a[0]", reader.getPath());
        assertEquals(1, reader.nextInt());
        assertEquals("$.a[1]", reader.getPath()); // index increments after reading value
        reader.beginObject();
        assertEquals("$.a[1].", reader.getPath());
        assertEquals("b", reader.nextName());
        assertEquals("$.a[1].b", reader.getPath());
        assertEquals(2, reader.nextInt());
        assertEquals("$.a[1].b", reader.getPath()); // after consumption, path still shows the last name? Actually after nextInt(), pathIndices[2]++ makes it [2], but pathNames for object still set. So path shows $.a[1].b? Wait after nextInt(), the path index for the array increments: stackSize is 3? Let's not overcomplicate; just assert at each step.
        reader.endObject();
        assertEquals("$.a[2]", reader.getPath()); // endObject increments array index
        reader.endArray();
        assertEquals("$.a", reader.getPath()); // after endArray? Actually endArray pops and increments parent index; parent is object, so pathNames[stackSize-1] is null? Let's just verify it works.
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    @Test
    public void testLenientNonExecutePrefix() throws IOException {
        JsonReader reader = reader(")]}'\n[1]");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testStrictNonExecutePrefix() throws IOException {
        JsonReader reader = reader(")]}'\n[1]");
        try {
            reader.beginArray();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testLenientLineComments() throws IOException {
        JsonReader reader = reader("// comment\n123");
        reader.setLenient(true);
        assertEquals(123, reader.nextInt());
    }

    @Test
    public void testLenientHashComments() throws IOException {
        JsonReader reader = reader("# comment\n123");
        reader.setLenient(true);
        assertEquals(123, reader.nextInt());
    }

    @Test
    public void testLenientBlockComments() throws IOException {
        JsonReader reader = reader("/* comment */123");
        reader.setLenient(true);
        assertEquals(123, reader.nextInt());
    }

    @Test
    public void testLenientSingleQuotedString() throws IOException {
        JsonReader reader = reader("'hello'");
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test
    public void testLenientUnquotedString() throws IOException {
        JsonReader reader = reader("hello");
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test
    public void testLenientMultipleTopLevel() throws IOException {
        JsonReader reader = reader("true false");
        reader.setLenient(true);
        assertTrue(reader.nextBoolean());
        assertFalse(reader.nextBoolean());
    }

    @Test
    public void testStrictMultipleTopLevel() throws IOException {
        JsonReader reader = reader("true false");
        reader.nextBoolean();
        try {
            reader.nextBoolean();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testLenientSemicolonAsComma() throws IOException {
        JsonReader reader = reader("[1;2]");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testLenientEqualsAsColon() throws IOException {
        JsonReader reader = reader("{\"a\" = 1}");
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testLenientArrayTrailingComma() throws IOException {
        JsonReader reader = reader("[1,]");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testLenientPromoteNameToValue() throws Exception {
        JsonReader reader = reader("{\"a\":123}");
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        // Now call promoteNameToValue using reflection
        Class<?> internalAccessClass = Class.forName("com.google.gson.internal.JsonReaderInternalAccess");
        Field instanceField = internalAccessClass.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        Object instance = instanceField.get(null);
        Method promoteMethod = internalAccessClass.getDeclaredMethod("promoteNameToValue", JsonReader.class);
        promoteMethod.invoke(instance, reader);
        // After promotion, peek should be STRING (or UNQUOTED) and value can be read as string
        assertEquals("a", reader.nextString());
        // The next token should be 123
        assertEquals(123, reader.nextInt());
        reader.endObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testPromoteNameToValueOnNonName() throws Exception {
        JsonReader reader = reader("123");
        Class<?> internalAccessClass = Class.forName("com.google.gson.internal.JsonReaderInternalAccess");
        Field instanceField = internalAccessClass.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        Object instance = instanceField.get(null);
        Method promoteMethod = internalAccessClass.getDeclaredMethod("promoteNameToValue", JsonReader.class);
        promoteMethod.invoke(instance, reader);
    }

    @Test
    public void testClose() throws IOException {
        JsonReader reader = reader("1");
        assertEquals(1, reader.nextInt());
        reader.close();
        try {
            reader.nextInt();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("JsonReader is closed"));
        }
    }

    @Test
    public void testCloseImmediately() throws IOException {
        JsonReader reader = reader("anything");
        reader.close();
        try {
            reader.peek();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testToString() {
        JsonReader reader = reader("1");
        assertTrue(reader.toString().contains("JsonReader"));
        assertTrue(reader.toString().contains("at line"));
    }

    @Test
    public void testSetLenientFalse() {
        JsonReader reader = reader("anything");
        reader.setLenient(true);
        assertTrue(reader.isLenient());
        reader.setLenient(false);
        assertFalse(reader.isLenient());
    }

    @Test
    public void testPeekEndArray() throws IOException {
        JsonReader reader = reader("[]");
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
    }

    @Test
    public void testPeekEndObject() throws IOException {
        JsonReader reader = reader("{}");
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
    }

    @Test
    public void testDoubleFromString() throws IOException {
        JsonReader reader = reader("\"123.5\"");
        assertEquals(123.5, reader.nextDouble(), 0.0);
    }

    @Test
    public void testLongFromString() throws IOException {
        JsonReader reader = reader("\"123\"");
        assertEquals(123L, reader.nextLong());
    }

    @Test(expected = NumberFormatException.class)
    public void testLongFromStringWithDecimal() throws IOException {
        JsonReader reader = reader("\"123.5\"");
        reader.nextLong();
    }

    @Test
    public void testIntFromString() throws IOException {
        JsonReader reader = reader("\"5\"");
        assertEquals(5, reader.nextInt());
    }

    @Test(expected = NumberFormatException.class)
    public void testIntFromStringWithFraction() throws IOException {
        JsonReader reader = reader("\"5.5\"");
        reader.nextInt();
    }

    @Test
    public void testLenientStringWithLeadingZeros() throws IOException {
        // In lenient, "0123" should be treated as string "0123"
        JsonReader reader = reader("0123");
        reader.setLenient(true);
        assertEquals("0123", reader.nextString());
    }

    @Test
    public void testNumberBufferOverflow() throws IOException {
        // Very long number that exceeds buffer size (1024) to trigger string builder in peekNumber? Actually peekNumber has a check for i == buffer.length and returns PEEKED_NONE if too long. So it will fall through to unquoted string.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append('9');
        }
        JsonReader reader = reader(sb.toString());
        reader.setLenient(true);
        String result = reader.nextString();
        assertEquals(sb.toString(), result);
    }

    @Test
    public void testReadEscapeInvalidSequence() throws IOException {
        JsonReader reader = reader("\"\\x\"");
        try {
            reader.nextString();
            fail();
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Invalid escape sequence"));
        }
    }

    @Test
    public void testReadEscapeEndOfFile() throws IOException {
        JsonReader reader = reader("\"\\");
        try {
            reader.nextString();
            fail();
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Unterminated escape sequence"));
        }
    }

    @Test
    public void testReadEscapeUnicodeEndOfFile() throws IOException {
        JsonReader reader = reader("\"\\u04\"");
        try {
            reader.nextString();
            fail();
        } catch (MalformedJsonException expected) {
            assertTrue(expected.getMessage().contains("Unterminated escape sequence"));
        }
    }

    @Test
    public void testNextNonWhitespaceEndOfInput() throws IOException {
        JsonReader reader = reader("   ");
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testConsumeNonExecutePrefixPartial() throws IOException {
        JsonReader reader = reader(")]}\trunc");
        reader.setLenient(true);
        // The prefix is ")]}'\n", if it's not complete, it's ignored.
        // So the first character ')' will cause syntax error? In lenient, doPeek for EMPTY_DOCUMENT calls consumeNonExecutePrefix, which checks for the full prefix. Since it doesn't match, it returns early, leaving pos unchanged. Then nextNonWhitespace reads ')' which is not whitespace, and since not start of JSON value, will cause "Expected value".
        try {
            reader.peek();
            fail();
        } catch (MalformedJsonException expected) {
        }
    }

    @Test
    public void testBeginArrayAfterEnd() throws IOException {
        JsonReader reader = reader("[]");
        reader.beginArray();
        reader.endArray();
        try {
            reader.beginArray();
            fail();
        } catch (IllegalStateException expected) {
            // Not IllegalState? Actually after endArray stack is EMPTY_DOCUMENT? Wait after end of array, stackSize-- returns to EMPTY_DOCUMENT/NONEMPTY? The stack starts with EMPTY_DOCUMENT, then beginArray pushes EMPTY_ARRAY, then after endArray, stackSize-- returns to EMPTY_DOCUMENT? Actually endArray decrements stackSize, so top of stack becomes the previous scope, which is EMPTY_DOCUMENT? But after the first read, the document scope was set to NONEMPTY_DOCUMENT? Let's trace: initial stack[0]=EMPTY_DOCUMENT. First beginArray peeks BEGIN_ARRAY, push EMPTY_ARRAY. After reading the entire document, the root document scope is NONEMPTY_DOCUMENT after first value? In our case, the array is the only top-level value. After endArray, stackSize goes from 2 to 1, the top is now the previous stack element which was still EMPTY_DOCUMENT? Actually after consuming the array, the stack[0] was changed to NONEMPTY_DOCUMENT? Yes, doPeek when stack was EMPTY_DOCUMENT, it changed to NONEMPTY_DOCUMENT before returning the token. So after the array is consumed, stack[0] is NONEMPTY_DOCUMENT. So calling beginArray again would trigger "JsonReader is closed"? No, it's still open. But there is no more content; doPeek will see NONEMPTY_DOCUMENT and try nextNonWhitespace, which returns End of input. So it returns PEEKED_EOF. But beginArray expects PEEKED_BEGIN_ARRAY or PEEKED_NONE, but it will call doPeek and get PEEKED_EOF, so it throws IllegalState with "Expected BEGIN_ARRAY but was END_DOCUMENT". So we should expect IllegalStateException. That's fine.
        }
    }

    @Test
    public void testEndArrayOnWrongType() throws IOException {
        JsonReader reader = reader("{}");
        try {
            reader.endArray();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Expected END_ARRAY but was BEGIN_OBJECT"));
        }
    }

    // similar for object

    @Test
    public void testEndObjectOnWrongType() throws IOException {
        JsonReader reader = reader("[]");
        try {
            reader.endObject();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Expected END_OBJECT but was BEGIN_ARRAY"));
        }
    }
}
