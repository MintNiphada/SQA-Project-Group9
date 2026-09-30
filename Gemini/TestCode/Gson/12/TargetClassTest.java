package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonTreeReaderTest {

  @Test
  public void testEmptyDocument() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(JsonNull.INSTANCE);
    reader.nextNull();
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    Assert.assertEquals("$", reader.getPath());
  }

  @Test
  public void testPrimitives() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("hello"));
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals("hello", reader.nextString());
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());

    reader = new JsonTreeReader(new JsonPrimitive(true));
    Assert.assertEquals(JsonToken.BOOLEAN, reader.peek());
    Assert.assertTrue(reader.nextBoolean());

    reader = new JsonTreeReader(new JsonPrimitive(false));
    Assert.assertEquals(JsonToken.BOOLEAN, reader.peek());
    Assert.assertFalse(reader.nextBoolean());

    reader = new JsonTreeReader(new JsonPrimitive(123));
    Assert.assertEquals(JsonToken.NUMBER, reader.peek());
    Assert.assertEquals(123, reader.nextInt());

    reader = new JsonTreeReader(new JsonPrimitive(1234567890123L));
    Assert.assertEquals(JsonToken.NUMBER, reader.peek());
    Assert.assertEquals(1234567890123L, reader.nextLong());

    reader = new JsonTreeReader(new JsonPrimitive(123.456));
    Assert.assertEquals(JsonToken.NUMBER, reader.peek());
    Assert.assertEquals(123.456, reader.nextDouble(), 0.0001);

    reader = new JsonTreeReader(JsonNull.INSTANCE);
    Assert.assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();
  }

  @Test
  public void testNumberStringInterchangeability() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("123"));
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals(123, reader.nextInt());

    reader = new JsonTreeReader(new JsonPrimitive("456"));
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals(456L, reader.nextLong());

    reader = new JsonTreeReader(new JsonPrimitive("789.0"));
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals(789.0, reader.nextDouble(), 0.0001);

    reader = new JsonTreeReader(new JsonPrimitive(123));
    Assert.assertEquals(JsonToken.NUMBER, reader.peek());
    Assert.assertEquals("123", reader.nextString());
  }

  @Test
  public void testArray() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("first"));
    array.add(new JsonPrimitive(2));
    array.add(JsonNull.INSTANCE);

    JsonTreeReader reader = new JsonTreeReader(array);
    Assert.assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    Assert.assertTrue(reader.hasNext());
    reader.beginArray();
    Assert.assertEquals("$[0]", reader.getPath());

    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals("first", reader.nextString());
    Assert.assertEquals("$[1]", reader.getPath());

    Assert.assertEquals(JsonToken.NUMBER, reader.peek());
    Assert.assertEquals(2, reader.nextInt());
    Assert.assertEquals("$[2]", reader.getPath());

    Assert.assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();
    Assert.assertEquals("$[3]", reader.getPath());

    Assert.assertFalse(reader.hasNext());
    Assert.assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    Assert.assertEquals("$", reader.getPath());
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testObject() throws IOException {
    JsonObject object = new JsonObject();
    object.addProperty("key1", "val1");
    object.addProperty("key2", true);

    JsonTreeReader reader = new JsonTreeReader(object);
    Assert.assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    Assert.assertTrue(reader.hasNext());
    reader.beginObject();

    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals(JsonToken.NAME, reader.peek());
    Assert.assertEquals("key1", reader.nextName());
    Assert.assertEquals("$.key1", reader.getPath());
    Assert.assertEquals("val1", reader.nextString());

    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals(JsonToken.NAME, reader.peek());
    Assert.assertEquals("key2", reader.nextName());
    Assert.assertEquals("$.key2", reader.getPath());
    Assert.assertTrue(reader.nextBoolean());

    Assert.assertFalse(reader.hasNext());
    Assert.assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    Assert.assertEquals("$", reader.getPath());
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testNestedStructuresAndPaths() throws IOException {
    JsonObject root = new JsonObject();
    JsonArray array = new JsonArray();
    JsonObject nested = new JsonObject();
    nested.addProperty("a", 1);
    array.add(nested);
    root.add("arr", array);

    JsonTreeReader reader = new JsonTreeReader(root);
    reader.beginObject();
    Assert.assertEquals("arr", reader.nextName());
    Assert.assertEquals("$.arr", reader.getPath());

    reader.beginArray();
    Assert.assertEquals("$.arr[0]", reader.getPath());

    reader.beginObject();
    Assert.assertEquals("a", reader.nextName());
    Assert.assertEquals("$.arr[0].a", reader.getPath());
    Assert.assertEquals(1, reader.nextInt());
    reader.endObject();

    reader.endArray();
    reader.endObject();
    Assert.assertEquals("$", reader.getPath());
  }

  @Test
  public void testSkipValue() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("skipMe", "val");
    obj.addProperty("keepMe", 42);

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();
    reader.skipValue(); // skips the name "skipMe"
    reader.skipValue(); // skips the string "val"
    Assert.assertEquals("keepMe", reader.nextName());
    Assert.assertEquals(42, reader.nextInt());
    reader.endObject();
  }

  @Test
  public void testPromoteNameToValue() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("key1", "val1");

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();
    reader.promoteNameToValue();
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals("key1", reader.nextString());
    Assert.assertEquals("val1", reader.nextString());
    reader.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testPromoteNameToValueInvalid() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("notAnObject"));
    reader.promoteNameToValue();
  }

  @Test
  public void testClose() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("test"));
    reader.close();
    try {
      reader.peek();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("JsonReader is closed", expected.getMessage());
    }
  }

  @Test
  public void testDoubleLenientNaNAndInfinity() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(Double.NaN));
    try {
      reader.nextDouble();
      Assert.fail();
    } catch (NumberFormatException expected) {
      // Expected since not lenient
    }

    reader = new JsonTreeReader(new JsonPrimitive(Double.NaN));
    reader.setLenient(true);
    Assert.assertTrue(Double.isNaN(reader.nextDouble()));

    reader = new JsonTreeReader(new JsonPrimitive(Double.POSITIVE_INFINITY));
    try {
      reader.nextDouble();
      Assert.fail();
    } catch (NumberFormatException expected) {
      // Expected
    }

    reader = new JsonTreeReader(new JsonPrimitive(Double.POSITIVE_INFINITY));
    reader.setLenient(true);
    Assert.assertTrue(Double.isInfinite(reader.nextDouble()));
  }

  @Test
  public void testStackExpansion() throws IOException {
    // Create deep nesting (> 32 levels) to trigger stack resizing
    JsonArray current = new JsonArray();
    JsonArray root = current;
    for (int i = 0; i < 40; i++) {
      JsonArray next = new JsonArray();
      current.add(next);
      current = next;
    }
    current.add(new JsonPrimitive("deep"));

    JsonTreeReader reader = new JsonTreeReader(root);
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    reader.beginArray();
    Assert.assertEquals("deep", reader.nextString());
    reader.endArray();
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
  }

  @Test
  public void testToString() {
    JsonTreeReader reader = new JsonTreeReader(JsonNull.INSTANCE);
    Assert.assertEquals("JsonTreeReader", reader.toString());
  }

  @Test
  public void testExpectMismatchThrowsIllegalStateException() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("hello"));
    try {
      reader.beginArray();
      Assert.fail();
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Expected BEGIN_ARRAY but was STRING"));
    }

    try {
      reader.beginObject();
      Assert.fail();
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Expected BEGIN_OBJECT but was STRING"));
    }

    try {
      reader.nextBoolean();
      Assert.fail();
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Expected BOOLEAN but was STRING"));
    }

    try {
      reader.nextNull();
      Assert.fail();
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Expected NULL but was STRING"));
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testNextStringOnBooleanThrows() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextString();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextIntOnBooleanThrows() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextInt();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextLongOnBooleanThrows() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextLong();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextDoubleOnBooleanThrows() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextDouble();
  }

  @Test(expected = AssertionError.class)
  public void testInvalidElementInStack() throws IOException {
    JsonElement customElement = new JsonElement() {
      @Override
      public JsonElement deepCopy() {
        return this;
      }
    };
    JsonTreeReader reader = new JsonTreeReader(customElement);
    reader.peek();
  }
}
