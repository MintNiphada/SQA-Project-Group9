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
  public void testNullWriterConstructor() {
    new JsonWriter(null);
  }

  @Test
  public void testDefaultStateAndProperties() {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    Assert.assertFalse(writer.isLenient());
    Assert.assertFalse(writer.isHtmlSafe());
    Assert.assertTrue(writer.getSerializeNulls());

    writer.setLenient(true);
    Assert.assertTrue(writer.isLenient());
    writer.setLenient(false);
    Assert.assertFalse(writer.isLenient());

    writer.setHtmlSafe(true);
    Assert.assertTrue(writer.isHtmlSafe());
    writer.setHtmlSafe(false);
    Assert.assertFalse(writer.isHtmlSafe());

    writer.setSerializeNulls(false);
    Assert.assertFalse(writer.getSerializeNulls());
    writer.setSerializeNulls(true);
    Assert.assertTrue(writer.getSerializeNulls());
  }

  @Test
  public void testIndentAndFormatting() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("a").value(1);
    writer.name("b").value(2);
    writer.beginArray();
    writer.value(3);
    writer.value(4);
    writer.endArray();
    writer.endObject();
    writer.close();

    String expected = "{\n" +
        "  \"a\": 1,\n" +
        "  \"b\": 2,\n" +
        "  \"c\": [\n".replace("\"c\":", "") +
        "  [\n" +
        "    3,\n" +
        "    4\n" +
        "  ]\n" +
        "}";
    Assert.assertEquals("{\n  \"a\": 1,\n  \"b\": 2,\n  [\n    3,\n    4\n  ]\n}", out.toString());

    // Reset indent to empty (compact)
    StringWriter out2 = new StringWriter();
    JsonWriter writer2 = new JsonWriter(out2);
    writer2.setIndent("  ");
    writer2.setIndent("");
    writer2.beginObject();
    writer2.name("a").value(1);
    writer2.endObject();
    writer2.close();
    Assert.assertEquals("{\"a\":1}", out2.toString());
  }

  @Test
  public void testTopLevelValuesStrictVsLenient() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value("first");
    try {
      writer.value("second");
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("JSON must have only one top-level value.", expected.getMessage());
    }

    StringWriter outLenient = new StringWriter();
    JsonWriter writerLenient = new JsonWriter(outLenient);
    writerLenient.setLenient(true);
    writerLenient.value("first");
    writerLenient.value("second");
    writerLenient.close();
    Assert.assertEquals("\"first\"\"second\"", outLenient.toString());
  }

  @Test
  public void testNumbersStrictVsLenient() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    try {
      writer.value(Double.NaN);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      writer.value(Double.NEGATIVE_INFINITY);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      writer.value(Double.POSITIVE_INFINITY);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      writer.value(Double.valueOf(Double.NaN));
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }

    StringWriter outLenient = new StringWriter();
    JsonWriter writerLenient = new JsonWriter(outLenient);
    writerLenient.setLenient(true);
    writerLenient.beginArray();
    writerLenient.value(Double.NaN);
    writerLenient.value(Double.NEGATIVE_INFINITY);
    writerLenient.value(Double.POSITIVE_INFINITY);
    writerLenient.value((Number) Double.valueOf(Double.NaN));
    writerLenient.value((Number) Double.valueOf(Double.NEGATIVE_INFINITY));
    writerLenient.value((Number) Double.valueOf(Double.POSITIVE_INFINITY));
    writerLenient.endArray();
    writerLenient.close();
    Assert.assertEquals("[NaN,-Infinity,Infinity,NaN,-Infinity,Infinity]", outLenient.toString());
  }

  @Test
  public void testNumericValues() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(123.456);
    writer.value(123456789012345L);
    writer.value(Long.MIN_VALUE);
    writer.value(Long.MAX_VALUE);
    writer.value(new BigDecimal("12345678901234567890.1234567890"));
    writer.value(new BigInteger("999999999999999999999999999999"));
    writer.value((Number) null);
    writer.endArray();
    writer.close();

    Assert.assertEquals("[123.456,123456789012345,-9223372036854775808,9223372036854775807," +
        "12345678901234567890.1234567890,999999999999999999999999999999,null]", out.toString());
  }

  @Test
  public void testBooleans() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(true);
    writer.value(false);
    writer.value(Boolean.TRUE);
    writer.value(Boolean.FALSE);
    writer.value((Boolean) null);
    writer.endArray();
    writer.close();

    Assert.assertEquals("[true,false,true,false,null]", out.toString());
  }

  @Test
  public void testJsonValue() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.jsonValue("{\"raw\":true}");
    writer.jsonValue(null);
    writer.endArray();
    writer.close();

    Assert.assertEquals("[{\"raw\":true},null]", out.toString());
  }

  @Test
  public void testNullSerialization() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("a").value((String) null);
    writer.name("b").nullValue();
    writer.endObject();
    writer.close();
    Assert.assertEquals("{\"a\":null,\"b\":null}", out.toString());

    StringWriter outOmit = new StringWriter();
    JsonWriter writerOmit = new JsonWriter(outOmit);
    writerOmit.setSerializeNulls(false);
    writerOmit.beginObject();
    writerOmit.name("a").value((String) null);
    writerOmit.name("b").nullValue();
    writerOmit.name("c").value("val");
    writerOmit.endObject();
    writerOmit.close();
    Assert.assertEquals("{\"c\":\"val\"}", outOmit.toString());
  }

  @Test
  public void testStringEscaping() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    // Test control chars and standard escapes
    writer.value("\"\\\t\b\n\r\f");
    // Test U+0000 through U+001F (e.g. \u0001, \u001f)
    writer.value("\u0000\u0001\u001f");
    // Test JS newline chars
    writer.value("\u2028\u2029");
    // Test normal unicode char
    writer.value("Hello World! \u0080 \u1234");
    writer.endArray();
    writer.close();

    Assert.assertEquals("[\"\\\"\\\\\\t\\b\\n\\r\\f\"," +
        "\"\\u0000\\u0001\\u001f\"," +
        "\"\\u2028\\u2029\"," +
        "\"Hello World! \u0080 \u1234\"]", out.toString());
  }

  @Test
  public void testHtmlSafeEscaping() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setHtmlSafe(true);
    writer.beginArray();
    writer.value("<tag> & 'test' = true");
    writer.endArray();
    writer.close();

    Assert.assertEquals("[\"\\u003ctag\\u003e \\u0026 \\u0027test\\u0027 \\u003d true\"]", out.toString());
  }

  @Test
  public void testDeepNestingStackResize() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    int depth = 40;
    for (int i = 0; i < depth; i++) {
      writer.beginArray();
    }
    writer.value("deep");
    for (int i = 0; i < depth; i++) {
      writer.endArray();
    }
    writer.close();

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      expected.append("[");
    }
    expected.append("\"deep\"");
    for (int i = 0; i < depth; i++) {
      expected.append("]");
    }
    Assert.assertEquals(expected.toString(), out.toString());
  }

  @Test
  public void testNullNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    try {
      writer.name(null);
      Assert.fail();
    } catch (NullPointerException expected) {
      Assert.assertEquals("name == null", expected.getMessage());
    }
  }

  @Test
  public void testDuplicateNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("a");
    try {
      writer.name("b");
      Assert.fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testNameWithoutObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    try {
      writer.name("a").value(1);
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Nesting problem.", expected.getMessage());
    }
  }

  @Test
  public void testNameInEmptyDocumentThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    try {
      writer.name("a").value(1);
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Nesting problem.", expected.getMessage());
    }
  }

  @Test
  public void testNameWhenClosedThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray().close();
    try {
      writer.name("a");
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("JsonWriter is closed.", expected.getMessage());
    }
  }

  @Test
  public void testFlushWhenClosedThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray().close();
    try {
      writer.flush();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("JsonWriter is closed.", expected.getMessage());
    }
  }

  @Test
  public void testValueWhenClosedThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray().close();
    try {
      writer.value("a");
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("JsonWriter is closed.", expected.getMessage());
    }
  }

  @Test
  public void testEndArrayWhenInObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    try {
      writer.endArray();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Nesting problem.", expected.getMessage());
    }
  }

  @Test
  public void testEndObjectWhenInArrayThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    try {
      writer.endObject();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Nesting problem.", expected.getMessage());
    }
  }

  @Test
  public void testEndObjectWithDanglingNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("a");
    try {
      writer.endObject();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Dangling name: a", expected.getMessage());
    }
  }

  @Test
  public void testEndArrayWithDanglingNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("a");
    try {
      writer.endArray();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertEquals("Nesting problem.", expected.getMessage());
    }
  }

  @Test
  public void testIncompleteDocumentThrowsOnClose() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    try {
      writer.close();
      Assert.fail();
    } catch (IOException expected) {
      Assert.assertEquals("Incomplete document", expected.getMessage());
    }

    StringWriter out2 = new StringWriter();
    JsonWriter writer2 = new JsonWriter(out2);
    try {
      writer2.close();
      Assert.fail();
    } catch (IOException expected) {
      Assert.assertEquals("Incomplete document", expected.getMessage());
    }
  }

  @Test
  public void testFlush() throws IOException {
    final boolean[] flushed = new boolean[1];
    Writer customWriter = new Writer() {
      @Override
      public void write(char[] cbuf, int off, int len) {
      }

      @Override
      public void flush() {
        flushed[0] = true;
      }

      @Override
      public void close() {
      }
    };
    JsonWriter writer = new JsonWriter(customWriter);
    writer.beginArray();
    writer.flush();
    Assert.assertTrue(flushed[0]);
  }

  @Test
  public void testCompleteObjectWithWhitespaceAndIndentation() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("\t");
    writer.beginObject();
    writer.name("emptyArray").beginArray().endArray();
    writer.name("emptyObject").beginObject().endObject();
    writer.name("nonEmptyArray").beginArray().value(1).value(2).endArray();
    writer.name("nonEmptyObject").beginObject().name("k1").value("v1").name("k2").value("v2").endObject();
    writer.endObject();
    writer.close();

    String expected = "{\n" +
        "\t\"emptyArray\": [],\n" +
        "\t\"emptyObject\": {},\n" +
        "\t\"nonEmptyArray\": [\n" +
        "\t\t1,\n" +
        "\t\t2\n" +
        "\t],\n" +
        "\t\"nonEmptyObject\": {\n" +
        "\t\t\"k1\": \"v1\",\n" +
        "\t\t\"k2\": \"v2\"\n" +
        "\t}\n" +
        "}";
    Assert.assertEquals(expected, out.toString());
  }

  @Test
  public void testMultipleClosesAreAllowedOnDelegate() throws IOException {
    final int[] closeCount = new int[1];
    Writer customWriter = new Writer() {
      @Override
      public void write(char[] cbuf, int off, int len) {
      }

      @Override
      public void flush() {
      }

      @Override
      public void close() {
        closeCount[0]++;
      }
    };

    JsonWriter writer = new JsonWriter(customWriter);
    writer.beginArray().endArray();
    writer.close();
    Assert.assertEquals(1, closeCount[0]);

    writer.close();
    Assert.assertEquals(2, closeCount[0]);
  }
}
