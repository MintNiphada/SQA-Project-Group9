package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import org.junit.Assert;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class JsonReaderTest {

  @Test(expected = NullPointerException.class)
  public void testNullReaderConstructor() {
    new JsonReader(null);
  }

  @Test
  public void testEmptyDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(""));
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    Assert.assertFalse(reader.hasNext());
    Assert.assertEquals("$", reader.getPath());
  }

  @Test
  public void testReadArray() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, 2, 3]"));
    Assert.assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    Assert.assertEquals("$[0]", reader.getPath());
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals("$[1]", reader.getPath());
    Assert.assertEquals(2L, reader.nextLong());
    Assert.assertEquals(3.0, reader.nextDouble(), 0.0001);
    Assert.assertFalse(reader.hasNext());
    Assert.assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testReadObject() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\": true, \"b\": false, \"c\": null, \"d\": \"hello\"}"));
    Assert.assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    Assert.assertEquals("$.", reader.getPath());
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals("a", reader.nextName());
    Assert.assertEquals("$.a", reader.getPath());
    Assert.assertTrue(reader.nextBoolean());
    Assert.assertEquals("b", reader.nextName());
    Assert.assertFalse(reader.nextBoolean());
    Assert.assertEquals("c", reader.nextName());
    reader.nextNull();
    Assert.assertEquals("d", reader.nextName());
    Assert.assertEquals("hello", reader.nextString());
    Assert.assertFalse(reader.hasNext());
    reader.endObject();
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testLenientTokensAndComments() throws IOException {
    String json = ")]}'\n"
        + "{\n"
        + "  // single line comment\n"
        + "  # hash comment\n"
        + "  /* multi\nline\ncomment */\n"
        + "  'single': 'value',\n"
        + "  unquoted: unquotedVal,\n"
        + "  num = 123;\n"
        + "  arrow => 456,\n"
        + "  arr: [1; 2, ; 3,]\n"
        + "}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    Assert.assertTrue(reader.isLenient());

    reader.beginObject();
    Assert.assertEquals("single", reader.nextName());
    Assert.assertEquals("value", reader.nextString());

    Assert.assertEquals("unquoted", reader.nextName());
    Assert.assertEquals("unquotedVal", reader.nextString());

    Assert.assertEquals("num", reader.nextName());
    Assert.assertEquals(123, reader.nextInt());

    Assert.assertEquals("arrow", reader.nextName());
    Assert.assertEquals(456, reader.nextInt());

    Assert.assertEquals("arr", reader.nextName());
    reader.beginArray();
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals(2, reader.nextInt());
    reader.nextNull(); // empty element
    Assert.assertEquals(3, reader.nextInt());
    reader.endArray();

    reader.endObject();
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testNumbersParsing() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[0, -0, 9223372036854775807, -9223372036854775808, 9223372036854775808, -9223372036854775809, 3.14159, -1.5e+2, 1e-3, 0.5]"));
    reader.beginArray();

    Assert.assertEquals(0, reader.nextInt());
    Assert.assertEquals(0L, reader.nextLong());
    Assert.assertEquals(Long.MAX_VALUE, reader.nextLong());
    Assert.assertEquals(Long.MIN_VALUE, reader.nextLong());

    Assert.assertEquals("9223372036854775808", reader.nextString());
    Assert.assertEquals("-9223372036854775809", reader.nextString());

    Assert.assertEquals(3.14159, reader.nextDouble(), 1e-6);
    Assert.assertEquals(-150.0, reader.nextDouble(), 1e-6);
    Assert.assertEquals(0.001, reader.nextDouble(), 1e-6);
    Assert.assertEquals(0.5, reader.nextDouble(), 1e-6);

    reader.endArray();
  }

  @Test
  public void testNumericConversionsAndBoundaries() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"123\", \"-456\", \"3.0\", \"1e2\"]"));
    reader.beginArray();
    Assert.assertEquals(123, reader.nextInt());
    Assert.assertEquals(-456L, reader.nextLong());
    Assert.assertEquals(3, reader.nextInt());
    Assert.assertEquals(100.0, reader.nextDouble(), 1e-6);
    reader.endArray();
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidIntConversionThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"3.14\"]"));
    reader.beginArray();
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class)
  public void testIntOverflowThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[2147483648]"));
    reader.beginArray();
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidLongConversionThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"3.14\"]"));
    reader.beginArray();
    reader.nextLong();
  }

  @Test
  public void testStringEscapes() throws IOException {
    String json = "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\\u000a\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    Assert.assertEquals("\"\\/\b\f\n\r\tA\n", reader.nextString());
    reader.endArray();
  }

  @Test(expected = NumberFormatException.class)
  public void testMalformedUnicodeEscape() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"\\u12G4\"]"));
    reader.beginArray();
    reader.nextString();
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedEscapeSequence() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"\\u12"));
    reader.beginArray();
    reader.nextString();
  }

  @Test
  public void testSkipValue() throws IOException {
    String json = "{\"skipArray\": [1, 2, {\"nested\": true}], \"skipObj\": {\"k\": \"v\"}, \"skipStr\": 'single', \"skipDbl\": \"double\", \"skipNum\": 123.456, \"skipUnq\": unquoted, \"keep\": 42}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();

    Assert.assertEquals("skipArray", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("skipObj", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("skipStr", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("skipDbl", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("skipNum", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("skipUnq", reader.nextName());
    reader.skipValue();

    Assert.assertEquals("keep", reader.nextName());
    Assert.assertEquals(42, reader.nextInt());

    reader.endObject();
  }

  @Test
  public void testPromoteNameToValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"key\":\"value\", 'k2':'v2', unquoted:1}"));
    reader.setLenient(true);
    reader.beginObject();

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    Assert.assertEquals("key", reader.nextString());
    Assert.assertEquals("value", reader.nextString());

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    Assert.assertEquals("k2", reader.nextString());
    Assert.assertEquals("v2", reader.nextString());

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    Assert.assertEquals("unquoted", reader.nextString());
    Assert.assertEquals(1, reader.nextInt());

    reader.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testPromoteNameToValueInvalidState() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginArray();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
  }

  @Test
  public void testBufferExtensionAndLongLiterals() throws IOException {
    char[] large = new char[2048];
    Arrays.fill(large, 'a');
    String longString = new String(large);
    JsonReader reader = new JsonReader(new StringReader("[\"" + longString + "\", " + longString + "]"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertEquals(longString, reader.nextString());
    Assert.assertEquals(longString, reader.nextString());
    reader.endArray();
  }

  @Test
  public void testBomStripping() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\ufeff[1]"));
    reader.beginArray();
    Assert.assertEquals(1, reader.nextInt());
    reader.endArray();
  }

  @Test
  public void testStackGrowth() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append("{\"a\":[");
    }
    sb.append("1");
    for (int i = 0; i < 40; i++) {
      sb.append("]}");
    }
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    for (int i = 0; i < 40; i++) {
      reader.beginObject();
      Assert.assertEquals("a", reader.nextName());
      reader.beginArray();
    }
    Assert.assertEquals(1, reader.nextInt());
    for (int i = 0; i < 40; i++) {
      reader.endArray();
      reader.endObject();
    }
  }

  @Test
  public void testLenientNaNDouble() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[NaN, Infinity, -Infinity]"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertTrue(Double.isNaN(reader.nextDouble()));
    Assert.assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
    Assert.assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
    reader.endArray();
  }

  @Test(expected = MalformedJsonException.class)
  public void testStrictNaNThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[NaN]"));
    reader.beginArray();
    reader.nextDouble();
  }

  @Test
  public void testClose() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.close();
    try {
      reader.peek();
      Assert.fail();
    } catch (IllegalStateException expected) {
      Assert.assertTrue(expected.getMessage().contains("closed"));
    }
  }

  @Test
  public void testToStringAndLocation() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\n  1,\n  2\n]"));
    reader.beginArray();
    reader.nextInt();
    String str = reader.toString();
    Assert.assertTrue(str.contains("JsonReader at line 2 column 5"));
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedCommentThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/* comment"));
    reader.setLenient(true);
    reader.peek();
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedStringThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"unterminated"));
    reader.peek();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedBeginArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{}"));
    reader.beginArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedEndArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{}"));
    reader.beginObject();
    reader.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedBeginObjectThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedEndObjectThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginArray();
    reader.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedNextBooleanThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.beginArray();
    reader.nextBoolean();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedNextNullThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.beginArray();
    reader.nextNull();
  }

  @Test(expected = IllegalStateException.class)
  public void testMismatchedNextNameThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.beginArray();
    reader.nextName();
  }

  @Test
  public void testKeywordsCaseInsensitiveLenient() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[TRUE, FALSE, NULL]"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertTrue(reader.nextBoolean());
    Assert.assertFalse(reader.nextBoolean());
    reader.nextNull();
    reader.endArray();
  }

  @Test
  public void testMultipleTopLevelValuesLenient() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("1 2 3"));
    reader.setLenient(true);
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals(2, reader.nextInt());
    Assert.assertEquals(3, reader.nextInt());
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(expected = MalformedJsonException.class)
  public void testMultipleTopLevelValuesStrictFails() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[] []"));
    reader.beginArray();
    reader.endArray();
    reader.peek();
  }

  @Test(expected = MalformedJsonException.class)
  public void testTopLevelLiteralStrictFails() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123"));
    reader.peek();
  }
}
