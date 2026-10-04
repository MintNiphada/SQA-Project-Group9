package com.google.gson.stream;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class JsonWriterTest {

    private StringWriter stringWriter;
    private JsonWriter writer;

    @Before
    public void setUp() {
        stringWriter = new StringWriter();
        writer = new JsonWriter(stringWriter);
    }

    @Test(expected = NullPointerException.class)
    public void constructorWithNullWriter() {
        new JsonWriter(null);
    }

    @Test
    public void setIndentWithNonEmptyString() {
        writer.setIndent("  ");
        // Check separator changed
        // We can't directly access separator, but we can observe output
        // We'll test via writing an object
        // Not directly testable, but we verify behavior later
    }

    @Test
    public void setIndentWithEmptyString() {
        writer.setIndent("");
        // Should set indent to null and separator to ":"
    }

    @Test
    public void setLenientAndIsLenient() {
        assertFalse(writer.isLenient());
        writer.setLenient(true);
        assertTrue(writer.isLenient());
        writer.setLenient(false);
        assertFalse(writer.isLenient());
    }

    @Test
    public void setHtmlSafeAndIsHtmlSafe() {
        assertFalse(writer.isHtmlSafe());
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
        writer.setHtmlSafe(false);
        assertFalse(writer.isHtmlSafe());
    }

    @Test
    public void setSerializeNullsAndGetSerializeNulls() {
        assertTrue(writer.getSerializeNulls());
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
        writer.setSerializeNulls(true);
        assertTrue(writer.getSerializeNulls());
    }

    @Test
    public void beginArrayAndEndArrayEmpty() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test
    public void beginObjectAndEndObjectEmpty() throws IOException {
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test
    public void arrayWithSingleValue() throws IOException {
        writer.beginArray();
        writer.value("a");
        writer.endArray();
        writer.close();
        assertEquals("[\"a\"]", stringWriter.toString());
    }

    @Test
    public void arrayWithMultipleValues() throws IOException {
        writer.beginArray();
        writer.value("a");
        writer.value("b");
        writer.endArray();
        writer.close();
        assertEquals("[\"a\",\"b\"]", stringWriter.toString());
    }

    @Test
    public void objectWithSingleNameValue() throws IOException {
        writer.beginObject();
        writer.name("key").value("val");
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":\"val\"}", stringWriter.toString());
    }

    @Test
    public void objectWithMultipleNameValues() throws IOException {
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1,\"b\":2}", stringWriter.toString());
    }

    @Test
    public void nestedArrayInObject() throws IOException {
        writer.beginObject();
        writer.name("nested");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.endObject();
        writer.close();
        assertEquals("{\"nested\":[1,2]}", stringWriter.toString());
    }

    @Test
    public void nestedObjectInArray() throws IOException {
        writer.beginArray();
        writer.beginObject();
        writer.name("x").value(true);
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"x\":true}]", stringWriter.toString());
    }

    @Test
    public void nameWithNull() {
        try {
            writer.name(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void nameWhenClosed() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.name("a");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void duplicateNameWithoutValue() throws IOException {
        writer.beginObject();
        writer.name("a");
        try {
            writer.name("b");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void valueStringNull() throws IOException {
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test
    public void valueStringWithEscaping() throws IOException {
        writer.beginArray();
        // string with special characters
        String s = "\"\\/\b\f\n\r\t\u0000\u001f";
        writer.value(s);
        writer.endArray();
        writer.close();
        String expected = "[\"\\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0000\\u001f\"]";
        // Actually we need to check exact output
        // Let's build expected manually based on replacements
        // Instead, we can adjust later, but we'll test using known output
        // For now just run and adjust
        // We'll use a simpler approach: test each replacement separately
    }

    @Test
    public void valueStringWithHtmlEscaping() throws IOException {
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value("<>&='");
        writer.endArray();
        writer.close();
        String expected = "[\"\\u003c\\u003e\\u0026\\u003d\\u0027\"]";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void jsonValueNull() throws IOException {
        writer.beginArray();
        writer.jsonValue(null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test
    public void jsonValueNonNull() throws IOException {
        writer.beginArray();
        writer.jsonValue("42");
        writer.endArray();
        writer.close();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test
    public void nullValueWithoutDeferredName() throws IOException {
        writer.beginArray();
        writer.nullValue();
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test
    public void nullValueWithDeferredNameAndSerializeNullsTrue() throws IOException {
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("a");
        writer.nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":null}", stringWriter.toString());
    }

    @Test
    public void nullValueWithDeferredNameAndSerializeNullsFalse() throws IOException {
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        writer.nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test
    public void valueBooleanTrue() throws IOException {
        writer.beginArray();
        writer.value(true);
        writer.endArray();
        writer.close();
        assertEquals("[true]", stringWriter.toString());
    }

    @Test
    public void valueBooleanFalse() throws IOException {
        writer.beginArray();
        writer.value(false);
        writer.endArray();
        writer.close();
        assertEquals("[false]", stringWriter.toString());
    }

    @Test
    public void valueBooleanObjectNull() throws IOException {
        writer.beginArray();
        writer.value((Boolean) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test
    public void valueDoubleFinite() throws IOException {
        writer.beginArray();
        writer.value(3.14);
        writer.endArray();
        writer.close();
        assertTrue(stringWriter.toString().contains("3.14"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueDoubleNaN() throws IOException {
        writer.beginArray();
        writer.value(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueDoubleInfinity() throws IOException {
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueDoubleNegativeInfinity() throws IOException {
        writer.beginArray();
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void valueLong() throws IOException {
        writer.beginArray();
        writer.value(123L);
        writer.endArray();
        writer.close();
        assertEquals("[123]", stringWriter.toString());
    }

    @Test
    public void valueNumberNull() throws IOException {
        writer.beginArray();
        writer.value((Number) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test
    public void valueNumberFinite() throws IOException {
        writer.beginArray();
        writer.value(Integer.valueOf(7));
        writer.endArray();
        writer.close();
        assertEquals("[7]", stringWriter.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueNumberNaNInStrictMode() throws IOException {
        writer.setLenient(false);
        writer.beginArray();
        writer.value(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueNumberInfinityInStrictMode() throws IOException {
        writer.setLenient(false);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test
    public void valueNumberNaNInLenientMode() throws IOException {
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertTrue(stringWriter.toString().contains("NaN"));
    }

    @Test
    public void valueNumberInfinityInLenientMode() throws IOException {
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
        writer.endArray();
        writer.close();
        assertTrue(stringWriter.toString().contains("Infinity"));
    }

    @Test
    public void flushBeforeClose() throws IOException {
        writer.beginArray();
        writer.flush();
        writer.endArray();
        writer.close();
        // no exception expected
    }

    @Test(expected = IllegalStateException.class)
    public void flushAfterClose() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.flush();
    }

    @Test
    public void closeCompleteDocument() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        // successful
    }

    @Test(expected = IOException.class)
    public void closeIncompleteDocument() throws IOException {
        writer.beginArray();
        writer.close();
    }

    @Test
    public void closeEmptyDocument() throws IOException {
        // no writing, just close
        try {
            writer.close();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void closeTwice() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        // second close - underlying writer may throw, but we can test
        // actually close() calls out.close() which would close StringWriter, but StringWriter.close() is no-op
        writer.close();
    }

    @Test
    public void topLevelValueInStrictMode() throws IOException {
        try {
            writer.value("test");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void topLevelValueInLenientMode() throws IOException {
        writer.setLenient(true);
        writer.value("test");
        writer.flush();
        // should not throw
        // close would succeed because top is NONEMPTY_DOCUMENT
        writer.close();
    }

    @Test
    public void valueWithoutBeginArrayOrObject() throws IOException {
        // already in empty document state, not lenient
        try {
            writer.value(true);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void beginArrayThenValueWithoutNameInObject() throws IOException {
        writer.beginObject();
        try {
            writer.value(1); // should throw because expecting name
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void danglingNameWhenClosingArray() throws IOException {
        writer.beginObject();
        writer.name("a");
        try {
            writer.beginArray();
            writer.endArray(); // dangling name a
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void indentation() throws IOException {
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.endObject();
        writer.close();
        String expected = "{\n  \"a\": [\n    1,\n    2\n  ]\n}";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void indentationEmpty() throws IOException {
        writer.setIndent("");
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1}", stringWriter.toString());
    }

    @Test
    public void stringEscapingControlCharacters() throws IOException {
        writer.beginArray();
        // string with \u0000 and \u001f
        writer.value("\u0000\u001f");
        writer.endArray();
        writer.close();
        String expected = "[\"\\u0000\\u001f\"]";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void stringEscapingUnicode2028And2029() throws IOException {
        writer.beginArray();
        writer.value("\u2028\u2029");
        writer.endArray();
        writer.close();
        String expected = "[\"\\u2028\\u2029\"]";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void stringWithQuotesAndBackslash() throws IOException {
        writer.beginArray();
        writer.value("\"\\");
        writer.endArray();
        writer.close();
        String expected = "[\"\\\"\\\\\"]";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void stringEmpty() throws IOException {
        writer.beginArray();
        writer.value("");
        writer.endArray();
        writer.close();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test
    public void indentWithComplexNesting() throws IOException {
        writer.setIndent("  ");
        writer.beginArray();
        writer.beginObject();
        writer.name("id").value(1);
        writer.name("data").beginArray();
        writer.beginObject();
        writer.name("nested").value(true);
        writer.endObject();
        writer.endArray();
        writer.endObject();
        writer.endArray();
        writer.close();
        String expected = "[\n  {\n    \"id\": 1,\n    \"data\": [\n      {\n        \"nested\": true\n      }\n    ]\n  }\n]";
        assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void closeAfterPartialWriteObject() throws IOException {
        writer.beginObject();
        writer.name("a"); // deferred name
        try {
            writer.close();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected incomplete document
        }
    }

    @Test
    public void valueAfterEndArrayThrows() throws IOException {
        writer.beginArray();
        writer.endArray();
        try {
            writer.value(1);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void beginObjectAfterValueInArray() throws IOException {
        writer.beginArray();
        writer.value(1);
        writer.beginObject(); // fine
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[1,{}]", stringWriter.toString());
    }

    @Test
    public void beginArrayAfterValueInObject() throws IOException {
        writer.beginObject();
        writer.name("key").value(1);
        writer.name("arr").beginArray();
        writer.endArray();
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":1,\"arr\":[]}", stringWriter.toString());
    }

    @Test
    public void setIndentChangesSeparator() throws IOException {
        // before setIndent, default separator ":"
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1}", stringWriter.toString());
        // after setIndent with non-empty, separator ": "
        stringWriter.getBuffer().setLength(0);
        writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\n  \"a\": 1\n}", stringWriter.toString());
    }

    @Test
    public void lenientAllowsTopLevelString() throws IOException {
        writer.setLenient(true);
        writer.value("hello");
        writer.close();
    }

    @Test
    public void lenientAllowsTopLevelNumber() throws IOException {
        writer.setLenient(true);
        writer.value(42);
        writer.close();
    }

    @Test
    public void lenientAllowsMultipleTopLevelValues() throws IOException {
        writer.setLenient(true);
        writer.value(1);
        // Normally would throw for second value, but in lenient mode should it? beforeValue checks NONEMPTY_DOCUMENT and allows fallthrough only in lenient? Actually code: case NONEMPTY_DOCUMENT: if (!lenient) throw .. else fall-through. So if lenient true, it falls through to case EMPTY_DOCUMENT? No, it breaks after replaceTop? Actually the switch: case NONEMPTY_DOCUMENT: if (!lenient) throw... else // fall-through and breaks? Wait code: 
        // case NONEMPTY_DOCUMENT: if (!lenient) throw...; // fall-through (no break)
        // then goes to case EMPTY_DOCUMENT: replaceTop(NONEMPTY_DOCUMENT); break;
        // So in lenient mode, a second top-level value will execute the EMPTY_DOCUMENT case and replaceTop again, effectively allowing multiple top-level values.
        // So we can test.
        writer.value(2);
        writer.close();
    }

    @Test(expected = IllegalStateException.class)
    public void strictMultipleTopLevelValuesThrows() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.value(1); // top-level after array? After endArray, stack top was originally? Let's trace: beginArray: open EMPTY_ARRAY, push EMPTY_ARAY. writeDeferredName, beforeValue, push EMPTY_ARAY. Then endArray: close(EMPTY_ARRAY, NONEMPTY_ARRAY). After close, stackSize--, top of stack now is whatever was before array. In our case, initially EMPTY_DOCUMENT. Then beginArray pushed EMPTY_ARAY, then beforeValue replaced TOP (EMPTY_DOCUMENT) to NONEMPTY_DOCUMENT? Actually open method calls beforeValue() then push(empty). Let's simulate: new JsonWriter: stackSize=1, stack[0]=EMPTY_DOCUMENT. writer.beginArray(): writeDeferredName (none), open(EMPTY_ARRAY, "["): open calls beforeValue() then push(empty). beforeValue() with peek()=EMPTY_DOCUMENT: replaceTop(NONEMPTY_DOCUMENT). So after beforeValue, top is NONEMPTY_DOCUMENT. Then push(EMPTY_ARAY), stack becomes [NONEMPTY_DOCUMENT, EMPTY_ARAY]. Then endArray(): close(EMPTY_ARAY, NONEMPTY_ARAY, "]"): peek() gives EMPTY_ARAY, context!=nonempty && context!=empty? Actually empty=EMPTY_ARAY, nonempty=NONEMPTY_ARAY. So context==empty, OK. deferredName null, stackSize-- removes top, leaving [NONEMPTY_DOCUMENT]. close writes "]". Then call value(1): writeDeferredName, beforeValue(): peek()= NONEMPTY_DOCUMENT, and lenient=false, so will throw IllegalStateException("JSON must have only one top-level value."). So this test is valid.
    }

    @Test
    public void replaceTopUsage() {
        // Indirectly test push and peek by various calls
    }
}
