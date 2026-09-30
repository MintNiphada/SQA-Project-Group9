package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonTreeWriterTest {

  @Test
  public void testInitialState() {
    JsonTreeWriter writer = new JsonTreeWriter();
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonNull());
  }

  @Test
  public void testWriteSingleString() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("hello");
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertEquals("hello", element.getAsString());
  }

  @Test
  public void testWriteSingleNullString() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((String) null);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonNull());
  }

  @Test
  public void testWriteNullValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.nullValue();
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonNull());
  }

  @Test
  public void testWriteBoolean() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(true);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertTrue(element.getAsBoolean());
  }

  @Test
  public void testWriteLong() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(123456789L);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertEquals(123456789L, element.getAsLong());
  }

  @Test
  public void testWriteDouble() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(123.456);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertEquals(123.456, element.getAsDouble(), 0.00001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWriteDoubleNaNNotLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Double.NaN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWriteDoubleInfiniteNotLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Double.POSITIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWriteDoubleNegativeInfiniteNotLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Double.NEGATIVE_INFINITY);
  }

  @Test
  public void testWriteDoubleNaNLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.NaN);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertTrue(Double.isNaN(element.getAsDouble()));
  }

  @Test
  public void testWriteDoubleInfiniteLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.POSITIVE_INFINITY);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertTrue(Double.isInfinite(element.getAsDouble()));
  }

  @Test
  public void testWriteNumber() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Integer.valueOf(42));
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertEquals(42, element.getAsInt());
  }

  @Test
  public void testWriteNumberNull() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((Number) null);
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonNull());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWriteNumberNaNNotLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Float.valueOf(Float.NaN));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWriteNumberInfiniteNotLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Float.valueOf(Float.POSITIVE_INFINITY));
  }

  @Test
  public void testWriteNumberNaNLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Float.valueOf(Float.NaN));
    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonPrimitive());
    Assert.assertTrue(Double.isNaN(element.getAsDouble()));
  }

  @Test
  public void testArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value("a");
    writer.value(1);
    writer.value(true);
    writer.nullValue();
    writer.endArray();

    JsonElement element = writer.get();
    Assert.assertTrue(element.isJsonArray());
    JsonArray array = element.getAsJsonArray();
    Assert.assertEquals(4, array.size());
    Assert.assertEquals("a", array.get(0).getAsString());
    Assert.assertEquals(1, array.get(1).getAsInt());
    Assert.assertTrue(array.get(2).getAsBoolean());
    Assert.assertTrue(array.get(3).isJsonNull());
  }

  @Test
  public void testNestedArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.beginArray();
    writer.value(10);
    writer.endArray();
    writer.endArray();

    JsonArray root = writer.get().getAsJsonArray();
    Assert.assertEquals(1, root.size());
    JsonArray inner = root.get(0).getAsJsonArray();
    Assert.assertEquals(1, inner.size());
    Assert.assertEquals(10, inner.get(0).getAsInt());
  }

  @Test
  public void testObjectWithoutSerializeNulls() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("a").value("alpha");
    writer.name("b").nullValue();
    writer.name("c").value((String) null);
    writer.name("d").value(123);
    writer.endObject();

    JsonObject object = writer.get().getAsJsonObject();
    Assert.assertEquals(2, object.size());
    Assert.assertEquals("alpha", object.get("a").getAsString());
    Assert.assertEquals(123, object.get("d").getAsInt());
    Assert.assertFalse(object.has("b"));
    Assert.assertFalse(object.has("c"));
  }

  @Test
  public void testObjectWithSerializeNulls() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("a").value("alpha");
    writer.name("b").nullValue();
    writer.name("c").value((String) null);
    writer.endObject();

    JsonObject object = writer.get().getAsJsonObject();
    Assert.assertEquals(3, object.size());
    Assert.assertEquals("alpha", object.get("a").getAsString());
    Assert.assertTrue(object.get("b").isJsonNull());
    Assert.assertTrue(object.get("c").isJsonNull());
  }

  @Test
  public void testComplexStructure() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("items").beginArray();
    writer.beginObject().name("id").value(1).endObject();
    writer.beginObject().name("id").value(2).endObject();
    writer.endArray();
    writer.name("count").value(2);
    writer.endObject();

    JsonObject root = writer.get().getAsJsonObject();
    Assert.assertEquals(2, root.get("count").getAsInt());
    JsonArray items = root.getAsJsonArray("items");
    Assert.assertEquals(2, items.size());
    Assert.assertEquals(1, items.get(0).getAsJsonObject().get("id").getAsInt());
    Assert.assertEquals(2, items.get(1).getAsJsonObject().get("id").getAsInt());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetUnfinishedThrows() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.get();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayWhenObjectExpected() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectWhenArrayExpected() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testNameOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.name("a");
  }

  @Test(expected = IllegalStateException.class)
  public void testNameWhenArrayOnStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.name("a");
  }

  @Test(expected = IllegalStateException.class)
  public void testDoubleName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("a");
    writer.name("b");
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectWithPendingName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("a");
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayWithPendingName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("a");
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testValueWithoutNameInObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.value("orphan");
  }

  @Test
  public void testFlushDoesNotThrow() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.flush();
    writer.value("test");
    writer.flush();
    Assert.assertEquals("test", writer.get().getAsString());
  }

  @Test
  public void testCloseSuccessfully() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("finished");
    writer.close();
    try {
      writer.get();
      Assert.fail("Expected IllegalStateException after close");
    } catch (IllegalStateException expected) {
    }
  }

  @Test(expected = IOException.class)
  public void testCloseIncompleteDocument() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.close();
  }

  @Test(expected = IllegalStateException.class)
  public void testWriteAfterClose() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("done");
    writer.close();
    writer.value("another");
  }
}
