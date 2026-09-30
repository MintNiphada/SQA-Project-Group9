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
    JsonWriter writer = new JsonWriter(stringWriter);
    Assert.assertFalse(writer.isLenient());
    Assert.assertFalse(writer.isHtmlSafe());
    Assert.assertTrue(writer.getSerializeNulls());
  }

  @Test
  public void testSetLenient() {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setLenient(true);
    Assert.assertTrue(writer.isLenient());
    writer.setLenient(false);
    Assert.assertFalse(writer.isLenient());
  }

  @Test
  public void testSetHtmlSafe() {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setHtmlSafe(true);
    Assert.assertTrue(writer.isHtmlSafe());
    writer.setHtmlSafe(false);
    Assert.assertFalse(writer.isHtmlSafe());
  }

  @Test
  public void testSetSerializeNulls() {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setSerializeNulls(false);
    Assert.assertFalse(writer.getSerializeNulls());
    writer.setSerializeNulls(true);
    Assert.assertTrue(writer.getSerializeNulls());
  }

  @Test
  public void testSetIndent() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("a").value("b");
    writer.endObject();
    Assert.assertEquals("{\n  \"a\": \"b\"\n}", stringWriter.toString());

    stringWriter = new StringWriter();
    writer = new JsonWriter(stringWriter);
    writer.setIndent("");
    writer.beginObject();
    writer.name("a").value("b");
    writer.endObject();
    Assert.assertEquals("{\"a\":\"b\"}", stringWriter.toString());
  }

  @Test
  public void testEmptyArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    Assert.assertEquals("[]", stringWriter.toString());
  }

  @Test
  public void testEmptyObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.endObject();
    Assert.assertEquals("{}", stringWriter.toString());
  }

  @Test
  public void testArrayWithValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.value(true);
    writer.value(false);
    writer.value(5.0);
    writer.value(10L);
    writer.value("hello");
    writer.nullValue();
    writer.endArray();
    Assert.assertEquals("[true,false,5.0,10,\"hello\",null]", stringWriter.toString());
  }

  @Test
  public void testObjectWithValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name("boolTrue").value(true);
    writer.name("boolFalse").value(false);
    writer.name("number").value(123.45);
    writer.name("long").value(9876543210L);
    writer.name("str").value("test");
    writer.name("nullVal").nullValue();
    writer.endObject();
    Assert.assertEquals(
        "{\"boolTrue\":true,\"boolFalse\":false,\"number\":123.45,\"long\":9876543210,\"str\":\"test\",\"nullVal\":null}",
        stringWriter.toString());
  }

  @Test
  public void testNestedStructures() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name("array").beginArray();
    writer.beginObject();
    writer.name("inner").value(1);
    writer.endObject();
    writer.endArray();
    writer.endObject();
    Assert.assertEquals("{\"array\":[{\"inner\":1}]}", stringWriter.toString());
  }

  @Test
  public void testNestedStructuresWithIndentation() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setIndent("  ");
    writer.beginArray();
    writer.beginObject();
    writer.name("key").value("value");
    writer.endObject();
    writer.endArray();
    String expected = "[\n  {\n    \"key\": \"value\"\n  }\n]";
    Assert.assertEquals(expected, stringWriter.toString());
  }

  @Test
  public void testDeepNestingStackGrowth() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    for (int i = 0; i < 40; i++) {
      writer.beginArray();
    }
    writer.value("deep");
    for (int i = 0; i < 40; i++) {
      writer.endArray();
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

  @Test
  public void testValueNullString() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.value((String) null);
    writer.endArray();
    Assert.assertEquals("[null]", stringWriter.toString());
  }

  @Test
  public void testValueNumber() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.value((Number) null);
    writer.value(new BigDecimal("12345.6789"));
    writer.value(new BigInteger("999999999999999999999999"));
    writer.value(Integer.valueOf(42));
    writer.endArray();
    Assert.assertEquals("[null,12345.6789,999999999999999999999999,42]", stringWriter.toString());
  }

  @Test
  public void testJsonValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name("raw").jsonValue("{\"a\":1,\"b\":true}");
    writer.name("rawNull").jsonValue(null);
    writer.endObject();
    Assert.assertEquals("{\"raw\":{\"a\":1,\"b\":true},\"rawNull\":null}", stringWriter.toString());
  }

  @Test
  public void testSerializeNullsFalse() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("skipMe").nullValue();
    writer.name("skipMeToo").value((String) null);
    writer.name("keepMe").value("val");
    writer.endObject();
    Assert.assertEquals("{\"keepMe\":\"val\"}", stringWriter.toString());
  }

  @Test
  public void testSerializeNullsFalseInArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setSerializeNulls(false);
    writer.beginArray();
    writer.nullValue();
    writer.value((String) null);
    writer.value((Number) null);
    writer.endArray();
    Assert.assertEquals("[null,null,null]", stringWriter.toString());
  }

  @Test
  public void testStringEscapes() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.value("\" \\ \t \b \n \r \f");
    writer.value("\u0000\u001f\u007f");
    writer.value("\u2028\u2029");
    writer.value("plain text without escapes");
    writer.endArray();

    String expected = "[\"\\\" \\\\ \\t \\b \\n \\r \\f\","
        + "\"\\u0000\\u001f\u007f\","
        + "\"\\u2028\\u2029\","
        + "\"plain text without escapes\"]";
    Assert.assertEquals(expected, stringWriter.toString());
  }

  @Test
  public void testHtmlSafeEscapes() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setHtmlSafe(true);
    writer.beginArray();
    writer.value("<tag> & 'quoted' = \"val\"");
    writer.endArray();
    Assert.assertEquals("[\"\\u003ctag\\u003e \\u0026 \\u0027quoted\\u0027 \\u003d \\\"val\\\"]", stringWriter.toString());
  }

  @Test
  public void testHtmlSafeFalseEscapes() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setHtmlSafe(false);
    writer.beginArray();
    writer.value("<tag> & 'quoted' = \"val\"");
    writer.endArray();
    Assert.assertEquals("[\"<tag> & 'quoted' = \\\"val\\\"]", stringWriter.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueDoubleNaN() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.NaN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueDoublePositiveInfinity() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.POSITIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueDoubleNegativeInfinity() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.NEGATIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueNumberNaNStrict() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.valueOf(Double.NaN));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueNumberInfinityStrict() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueNumberNegativeInfinityStrict() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testValueNumberNaNLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setLenient(true);
    writer.beginArray();
    writer.value(Double.valueOf(Double.NaN));
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    writer.endArray();
    Assert.assertEquals("[NaN,Infinity,-Infinity]", stringWriter.toString());
  }

  @Test
  public void testTopLevelValuesLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setLenient(true);
    writer.value("first");
    writer.value("second");
    writer.value(123);
    Assert.assertEquals("\"first\"\"second\"123", stringWriter.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testMultipleTopLevelValuesStrict() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.value("first");
    writer.value("second");
  }

  @Test(expected = NullPointerException.class)
  public void testNameNull() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testDuplicateNameWithoutValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name("a");
    writer.name("b");
  }

  @Test(expected = IllegalStateException.class)
  public void testNameInArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.name("a");
    writer.value("b");
  }

  @Test(expected = IllegalStateException.class)
  public void testNameAtTopLevel() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.name("a");
    writer.value("b");
  }

  @Test(expected = IllegalStateException.class)
  public void testValueInObjectWithoutName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.value("value");
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectInArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayInObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectWithDanglingName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginObject();
    writer.name("dangling");
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayEmpty() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectEmpty() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.endObject();
  }

  @Test
  public void testFlush() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.value(1);
    writer.flush();
    Assert.assertEquals("[1", stringWriter.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testFlushClosed() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    writer.flush();
  }

  @Test(expected = IllegalStateException.class)
  public void testNameOnClosedWriter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    writer.name("a");
  }

  @Test(expected = IOException.class)
  public void testCloseIncompleteDocument() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.close();
  }

  @Test(expected = IOException.class)
  public void testCloseEmptyDocument() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.close();
  }

  @Test
  public void testCloseCompleteDocument() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    Assert.assertEquals("[]", stringWriter.toString());
  }

  @Test
  public void testCloseClosesUnderlyingWriter() throws IOException {
    class MockWriter extends Writer {
      boolean closed = false;
      @Override public void write(char[] cbuf, int off, int len) {}
      @Override public void flush() {}
      @Override public void close() {
        closed = true;
      }
    }
    MockWriter mockWriter = new MockWriter();
    JsonWriter writer = new JsonWriter(mockWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    Assert.assertTrue(mockWriter.closed);
  }
}
