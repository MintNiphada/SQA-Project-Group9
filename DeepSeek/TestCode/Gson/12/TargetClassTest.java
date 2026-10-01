package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonTreeReaderTest {

    @Test
    public void testRootPrimitiveString() throws IOException {
        JsonElement element = new JsonPrimitive("hello");
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootPrimitiveBooleanTrue() throws IOException {
        JsonElement element = new JsonPrimitive(true);
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootPrimitiveBooleanFalse() throws IOException {
        JsonElement element = new JsonPrimitive(false);
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertFalse(reader.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootPrimitiveNumber() throws IOException {
        JsonElement element = new JsonPrimitive(42);
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(42, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootPrimitiveNumberFromString() throws IOException {
        JsonElement element = new JsonPrimitive("123");
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals(123, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootPrimitiveNumberAsString() throws IOException {
        JsonElement element = new JsonPrimitive(3.14);
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.NUMBER, reader.peek());
        String s = reader.nextString();
        assertTrue(s.equals("3.14") || s.equals("3.1400001049041748")); // float representation, but we compare to string representation of the number
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootNull() throws IOException {
        JsonElement element = JsonNull.INSTANCE;
        JsonTreeReader reader = new JsonTreeReader(element);
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootObjectEmpty() throws IOException {
        JsonObject obj = new JsonObject();
        JsonTreeReader reader = new JsonTreeReader(obj);
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootObjectSingleBoolean() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("flag", new JsonPrimitive(true));
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertTrue(reader.hasNext());
        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("flag", reader.nextName());
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        assertFalse(reader.hasNext());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootObjectNested() throws IOException {
        JsonObject inner = new JsonObject();
        inner.add("a", new JsonPrimitive(1));
        JsonObject outer = new JsonObject();
        outer.add("inner", inner);
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootArrayEmpty() throws IOException {
        JsonArray arr = new JsonArray();
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testRootArrayWithNumbers() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive(2));
        arr.add(new JsonPrimitive(3));
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        assertTrue(reader.hasNext());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals(2, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals(3, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testHasNext() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("key", new JsonPrimitive("val"));
        JsonTreeReader reader = new JsonTreeReader(obj);
        assertTrue(reader.hasNext());
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test
    public void testGetPathObject() throws IOException {
        JsonObject obj = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.add("b", new JsonPrimitive(2));
        obj.add("a", inner);
        JsonTreeReader reader = new JsonTreeReader(obj);
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.", reader.getPath());
        assertEquals("a", reader.nextName());
        assertEquals("$.a", reader.getPath());
        reader.beginObject();
        assertEquals("$.a.", reader.getPath());
        assertEquals("b", reader.nextName());
        assertEquals("$.a.b", reader.getPath());
        reader.nextInt();
        assertEquals("$.a.b", reader.getPath());
        reader.endObject();
        assertEquals("$.a", reader.getPath());
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    @Test
    public void testGetPathArray() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(10));
        arr.add(new JsonPrimitive(20));
        JsonTreeReader reader = new JsonTreeReader(arr);
        assertEquals("$", reader.getPath());
        reader.beginArray();
        assertEquals("$[0]", reader.getPath()); // after beginArray, pathIndices was set to 0? Actually beginArray pushes iterator, then sets pathIndices[stackSize-1]=0.
        assertEquals(10, reader.nextInt());
        assertEquals("$[1]", reader.getPath()); // after nextInt, incremented to 1.
        assertEquals(20, reader.nextInt());
        assertEquals("$[2]", reader.getPath());
        reader.endArray();
        assertEquals("$", reader.getPath());
    }

    @Test
    public void testGetPathNestedMix() throws IOException {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive("x"));
        obj.add("list", arr);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals("list", reader.nextName());
        assertEquals("$.list", reader.getPath());
        reader.beginArray();
        assertEquals("$.list[0]", reader.getPath());
        reader.nextString();
        assertEquals("$.list[1]", reader.getPath());
        reader.endArray();
        assertEquals("$.list", reader.getPath());
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    @Test
    public void testStackExpansion() throws IOException {
        // Create deeply nested structure: array containing 40 elements
        JsonArray arr = new JsonArray();
        for (int i = 0; i < 40; i++) {
            arr.add(new JsonPrimitive(i));
        }
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        for (int i = 0; i < 40; i++) {
            assertEquals(i, reader.nextInt());
        }
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testClosedReaderThrowsOnPeek() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("x"));
        reader.close();
        try {
            reader.peek();
            fail("Expected IllegalStateEx");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("JsonReader is closed"));
        }
    }

    @Test
    public void testClosedReaderThrowsOnNextName() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        reader.close();
        try {
            reader.nextName();
            fail();
        } catch (IllegalStateException e) { }
    }

    @Test
    public void testClosedReaderThrowsOnNextString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("a"));
        reader.close();
        try {
            reader.nextString();
            fail();
        } catch (IllegalStateException e) { }
    }

    @Test(expected = IllegalStateException.class)
    public void testBeginArrayOnWrongToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(1));
        reader.beginArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testBeginObjectOnWrongToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
        reader.beginObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndArrayOnWrongToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndObjectOnWrongToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonArray());
        reader.endObject();
    }

    @Test
    public void testNextDoubleOnStringNaNNonLenient() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("NaN"));
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().contains("param "));
        }
    }

    @Test
    public void testNextDoubleOnStringInfinityNonLenient() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("Infinity"));
        try {
            reader.nextDouble();
            fail();
        } catch (NumberFormatException e) { }
    }

    @Test
    public void testNextDoubleOnStringNaNLenient() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("NaN"));
        reader.setLenient(true);
        double val = reader.nextDouble();
        assertTrue(Double.isNaN(val));
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testNextDoubleOnStringInfinityLenient() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("Infinity"));
        reader.setLenient(true);
        double val = reader.nextDouble();
        assertTrue(Double.isInfinite(val) && val > 0);
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testNextDoubleOnStringNegativeInfinityLenient() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("-Infinity"));
        reader.setLenient(true);
        double val = reader.nextDouble();
        assertTrue(Double.isInfinite(val) && val < 0);
    }

    @Test
    public void testNextDoubleOnNumberToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(2.718));
        double val = reader.nextDouble();
        assertEquals(2.718, val, 0.0);
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testNextLongFromString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("123456789"));
        long val = reader.nextLong();
        assertEquals(123456789L, val);
    }

    @Test
    public void testNextIntFromString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("42"));
        int val = reader.nextInt();
        assertEquals(42, val);
    }

    @Test(expected = IllegalStateException.class)
    public void testNextNameOnNumberToken() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(1));
        reader.nextName();
    }

    @Test
    public void testSkipValueOnNameInObject() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("skipMe", new JsonPrimitive(true));
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test
    public void testSkipValueOnRootPrimiveCausesCrash() {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(1));
        try {
            reader.skipValue();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // known bug: skipValue when stackSize becomes 0
        } catch (IOException e) {
            fail("Should have thrown ArrayIndexOutOfBounds, got IOException");
        }
    }

    @Test
    public void testPromoteNameToValue() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("key", new JsonPrimitive("value"));
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        reader.promoteNameToValue();
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("key", reader.nextString());
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("value", reader.nextString());
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test
    public void testPeekOnClosedReader() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        reader.close();
        try {
            reader.peek();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("closed"));
        }
    }

    @Test
    public void testLocationStringInException() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("name", new JsonArray());
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.nextName(); // now at BEGIN_ARRAY
        try {
            reader.nextBoolean(); // wrong token
            fail();
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Expected BOCLEAN but was BEGIN_ARRAY"));
            assertTrue(msg.contains(" at path $.name"));
        }
    }
}
