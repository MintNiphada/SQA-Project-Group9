package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class JsonTreeWriterTest {

    @Test
    public void testInitialProductIsJsonNull() {
        JsonTreeWriter writer = new JsonTreeWriter();
        Assert.assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testGetAfterSimpleValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("test");
        JsonElement result = writer.get();
        Assert.assertTrue(result.isJsonPrimitive());
        Assert.assertEquals("test", result.getAsString());
    }

    @Test
    public void testGetWithStackNotEmptyThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.get();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
        writer.endArray();
        // now should succeed
        Assert.assertTrue(writer.get().isJsonArray());
    }

    @Test
    public void testGetAfterCloseThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        try {
            writer.get();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testBeginArrayEndArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.endArray();
        JsonElement result = writer.get();
        Assert.assertTrue(result.isJsonArray());
        Assert.assertEquals(0, result.getAsJsonArray().size());
    }

    @Test
    public void testBeginObjectEndObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.endObject();
        JsonElement result = writer.get();
        Assert.assertTrue(result.isJsonObject());
        Assert.assertEquals(0, result.getAsJsonObject().entrySet().size());
    }

    @Test
    public void testNestedStructure() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("a");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.name("b");
        writer.value("text");
        writer.endObject();
        JsonObject obj = writer.get().getAsJsonObject();
        Assert.assertEquals(2, obj.entrySet().size());
        Assert.assertTrue(obj.get("a").isJsonArray());
        Assert.assertEquals(2, obj.getAsJsonArray("a").size());
        Assert.assertEquals(1, obj.getAsJsonArray("a").get(0).getAsInt());
        Assert.assertEquals("text", obj.get("b").getAsString());
    }

    @Test
    public void testEndArrayTypeMismatch() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.endArray();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndObjectTypeMismatch() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.endObject();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testEndArrayWithEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndObjectWithEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testNameOnEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.name("x");
    }

    @Test
    public void testNameWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("first");
        try {
            writer.name("second");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNameOnArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.name("x");
    }

    @Test
    public void testValueString() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("hello");
        Assert.assertEquals("hello", writer.get().getAsString());
    }

    @Test
    public void testValueStringNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((String) null);
        Assert.assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testNullValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.nullValue();
        Assert.assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testValueBooleanTrue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(true);
        Assert.assertTrue(writer.get().getAsBoolean());
    }

    @Test
    public void testValueBooleanFalse() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(false);
        Assert.assertFalse(writer.get().getAsBoolean());
    }

    @Test
    public void testValueDoubleNormal() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(3.14);
        Assert.assertEquals(3.14, writer.get().getAsDouble(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueDoubleNaNThrowsWhenNotLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueDoublePositiveInfinityThrowsWhenNotLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueDoubleNegativeInfinityThrowsWhenNotLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testValueDoubleNaNWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NaN);
        Assert.assertTrue(Double.isNaN(writer.get().getAsDouble()));
    }

    @Test
    public void testValueDoubleInfinityWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, writer.get().getAsDouble(), 0.0);
    }

    @Test
    public void testValueLong() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        long val = 123456789012345L;
        writer.value(val);
        Assert.assertEquals(val, writer.get().getAsLong());
    }

    @Test
    public void testValueNumberNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) null);
        Assert.assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testValueNumberInteger() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Integer.valueOf(42));
        Assert.assertEquals(42, writer.get().getAsInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueNumberNaNThrowsWhenNotLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.valueOf(Double.NaN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueNumberPositiveInfinityThrowsWhenNotLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testValueNumberNaNWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.valueOf(Double.NaN));
        Assert.assertTrue(Double.isNaN(writer.get().getAsDouble()));
    }

    @Test
    public void testValueNumberInfinityWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Double.POSITIVE_INFINITY, writer.get().getAsDouble(), 0.0);
    }

    @Test
    public void testFlushDoesNothing() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.flush(); // no exception
    }

    @Test
    public void testCloseOnEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        // Should be able to close; subsequent writes fail
        try {
            writer.value(1);
            Assert.fail("Expected IllegalStateException after close");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(expected = IOException.class)
    public void testCloseWithIncompleteDocument() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.close();
    }

    @Test(expected = IOException.class)
    public void testCloseAfterClose() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        writer.close(); // stack already has SENTINEL_CLOSED, not empty
    }

    @Test
    public void testTopLevelValuesOverride() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("first");
        writer.value("second");
        Assert.assertEquals("second", writer.get().getAsString());
    }

    @Test
    public void testPutValueWithoutNameInObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.value("x");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNullValueInObjectWithSerializeNullsFalse() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key");
        writer.nullValue();
        writer.endObject();
        JsonObject obj = writer.get().getAsJsonObject();
        Assert.assertFalse(obj.has("key"));
    }

    @Test
    public void testNullValueInObjectWithSerializeNullsTrue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("key");
        writer.nullValue();
        writer.endObject();
        JsonObject obj = writer.get().getAsJsonObject();
        Assert.assertTrue(obj.has("key"));
        Assert.assertSame(JsonNull.INSTANCE, obj.get("key"));
    }

    @Test
    public void testEndArrayWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("x");
        try {
            writer.endArray();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndObjectWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("x");
        try {
            writer.endObject();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testBeginArrayWithinObjectWithoutName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.beginArray();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testBeginArrayAfterClose() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        try {
            writer.beginArray();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testMixedArrayObjectDepth() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.beginObject();
        writer.name("x");
        writer.value(1);
        writer.endObject();
        writer.endArray();
        JsonArray arr = writer.get().getAsJsonArray();
        Assert.assertEquals(1, arr.size());
        Assert.assertEquals(1, arr.get(0).getAsJsonObject().get("x").getAsInt());
    }
}
