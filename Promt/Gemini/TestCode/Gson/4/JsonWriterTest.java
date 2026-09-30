package com.google.gson.stream;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonWriterTest {

  @Test(expected = NullPointerException.class)
  public void testConstructorNullWriter() {
    new JsonWriter(null);
  }

  @Test
  public void testDefaultSettings() {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    Assert.assertFalse(jsonWriter.isLenient());
    Assert.assertFalse(jsonWriter.isHtmlSafe());
    Assert.assertTrue(jsonWriter.getSerializeNulls());
  }

  @Test
  public void testSetIndentAndFormatting() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.setIndent("  ");
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.beginArray();
    jsonWriter.value(1);
    jsonWriter.value(2);
    jsonWriter.endArray();
    jsonWriter.endObject();

    String expected = "{\n  \"a\": [\n    1,\n    2\n  ]\n}";
    Assert.assertEquals(expected, stringWriter.toString());

    // Reset indent to empty (compact format)
    stringWriter = new StringWriter();
    jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setIndent("");
    jsonWriter.beginObject();
    jsonWriter.name("a").value(1);
    jsonWriter.endObject();
    Assert.assertEquals("{\"a\":1}", stringWriter.toString());
  }

  @Test
  public void testLenientTopLevelLiterals() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    Assert.assertTrue(jsonWriter.isLenient());

    jsonWriter.value("hello");
    jsonWriter.value(123);
    jsonWriter.value(true);
    jsonWriter.nullValue();
    jsonWriter.close();

    Assert.assertEquals("\"hello\"123truenull", stringWriter.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testStrictTopLevelValueThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.value("string");
  }

  @Test(expected = IllegalStateException.class)
  public void testStrictMultipleTopLevelValuesThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray().endArray();
    jsonWriter.beginArray().endArray();
  }

  @Test
  public void testHtmlSafe() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setHtmlSafe(true);
    Assert.assertTrue(jsonWriter.isHtmlSafe());

    jsonWriter.beginArray();
    jsonWriter.value("<tag> & 'foo' = \"bar\"");
    jsonWriter.endArray();

    Assert.assertEquals("[\"\\u003ctag\\u003e \\u0026 \\u0027foo\\u0027 \\u003d \\\"bar\\\"]", stringWriter.toString());
  }

  @Test
  public void testEscapeSpecialCharacters() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginArray();
    jsonWriter.value("\"\\\t\b\n\r\f\u0000\u001f\u2028\u2029abc");
    jsonWriter.endArray();

    String expected = "[\"\\\"\\\\\\t\\b\\n\\r\\f\\u0000\\u001f\\u2028\\u2029abc\"]";
    Assert.assertEquals(expected, stringWriter.toString());
  }

  @Test
  public void testSerializeNullsFalse() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setSerializeNulls(false);
    Assert.assertFalse(jsonWriter.getSerializeNulls());

    jsonWriter.beginObject();
    jsonWriter.name("a").value("A");
    jsonWriter.name("b").nullValue();
    jsonWriter.name("c").value((String) null);
    jsonWriter.name("d").value((Number) null);
    jsonWriter.name("e").jsonValue(null);
    jsonWriter.endObject();

    Assert.assertEquals("{\"a\":\"A\"}", stringWriter.toString());
  }

  @Test
  public void testSerializeNullsTrue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setSerializeNulls(true);

    jsonWriter.beginObject();
    jsonWriter.name("a").nullValue();
    jsonWriter.name("b").value((String) null);
    jsonWriter.name("c").value((Number) null);
    jsonWriter.name("d").jsonValue(null);
    jsonWriter.endObject();

    Assert.assertEquals("{\"a\":null,\"b\":null,\"c\":null,\"d\":null}", stringWriter.toString());
  }

  @Test
  public void testJsonValueDirectInsertion() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginObject();
    jsonWriter.name("raw");
    jsonWriter.jsonValue("{\"inner\":true}");
    jsonWriter.endObject();

    Assert.assertEquals("{\"raw\":{\"inner\":true}}", stringWriter.toString());
  }

  @Test
  public void testNumericValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginArray();
    jsonWriter.value(123456789012345L);
    jsonWriter.value(123.456);
    jsonWriter.value(new BigDecimal("123456789.987654321"));
    jsonWriter.value(new BigInteger("999999999999999999999999"));
    jsonWriter.endArray();

    Assert.assertEquals("[123456789012345,123.456,123456789.987654321,999999999999999999999999]", stringWriter.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleNaNStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.NaN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleInfiniteStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.POSITIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleNegativeInfiniteStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.NEGATIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNumberNaNStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NaN));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNumberInfinityStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNumberNegativeInfinityStrictThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testNumberLenientNaNAndInfinities() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);

    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NaN));
    jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
    jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    jsonWriter.endArray();

    Assert.assertEquals("[NaN,Infinity,-Infinity]", stringWriter.toString());
  }

  @Test
  public void testDeepNestingAndStackGrowth() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    for (int i = 0; i < 40; i++) {
      jsonWriter.beginArray();
    }
    jsonWriter.value("deep");
    for (int i = 0; i < 40; i++) {
      jsonWriter.endArray();
    }

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      expected.append("[");
    }
    expected.append("\"deep\"");
    for (int i = 0; i < 40; i++) {
      expected.append("]");
    }

    Assert.assertEquals(expected.toString(), stringWriter.toString());
  }

  @Test(expected = NullPointerException.class)
  public void testNameNullThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testConsecutiveNamesThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.name("b");
  }

  @Test(expected = IllegalStateException.class)
  public void testNameOutsideObjectThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.name("a").value("value");
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayWhenObjectExpected() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectWhenArrayExpected() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testDanglingNameAtEndObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.endObject();
  }

  @Test(expected = IOException.class)
  public void testCloseIncompleteArrayThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.close();
  }

  @Test(expected = IOException.class)
  public void testCloseIncompleteObjectThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.close();
  }

  @Test(expected = IOException.class)
  public void testCloseEmptyThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.close();
  }

  @Test
  public void testFlushAndClose() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(true);
    jsonWriter.value(false);
    jsonWriter.endArray();
    jsonWriter.flush();
    jsonWriter.close();

    Assert.assertEquals("[true,false]", stringWriter.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testFlushAfterCloseThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray().endArray();
    jsonWriter.close();
    jsonWriter.flush();
  }

  @Test(expected = IllegalStateException.class)
  public void testNameAfterCloseThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray().endArray();
    jsonWriter.close();
    jsonWriter.name("test");
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectOnClosedWriterThrows() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject().endObject();
    jsonWriter.close();
    jsonWriter.endObject();
  }

  @Test
  public void testComplexNestedDocument() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginObject();
    jsonWriter.name("items");
    jsonWriter.beginArray();
    jsonWriter.beginObject();
    jsonWriter.name("id").value(1);
    jsonWriter.name("valid").value(true);
    jsonWriter.name("comment").value("some\ntext");
    jsonWriter.endObject();
    jsonWriter.endArray();
    jsonWriter.name("emptyObj").beginObject().endObject();
    jsonWriter.name("emptyArr").beginArray().endArray();
    jsonWriter.endObject();

    String expected = "{\"items\":[{\"id\":1,\"valid\":true,\"comment\":\"some\\ntext\"}],\"emptyObj\":{},\"emptyArr\":[]}";
    Assert.assertEquals(expected, stringWriter.toString());
  }
}
