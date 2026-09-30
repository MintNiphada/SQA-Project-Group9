package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import org.junit.Assert;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JsonReaderTest {

  @Test(expected = NullPointerException.class)
  public void testNullReaderThrowsException() {
    new JsonReader(null);
  }

  @Test
  public void testEmptyDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(""));
    Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    Assert.assertEquals("$", reader.getPath());
    Assert.assertEquals("JsonReader at line 1 column 1", reader.toString());
    reader.close();
  }

  @Test
  public void testBOMConsumption() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\ufeff[123]"));
    reader.beginArray();
    Assert.assertEquals(123, reader.nextInt());
    reader.endArray();
  }

  @Test
  public void testLenientNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
    reader.setLenient(true);
    Assert.assertTrue(reader.isLenient());
    reader.beginArray();
    Assert.assertEquals(1, reader.nextInt());
    reader.endArray();
  }

  @Test
  public void testStrictNonExecutePrefixFails() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
    try {
      reader.beginArray();
      Assert.fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testBooleansAndNull() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true, FALSE, True, false, null, NULL]"));
    reader.beginArray();
    Assert.assertTrue(reader.nextBoolean());
    Assert.assertFalse(reader.nextBoolean());
    Assert.assertTrue(reader.nextBoolean());
    Assert.assertFalse(reader.nextBoolean());
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextBooleanWrongType() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123"));
    reader.nextBoolean();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextNullWrongType() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123"));
    reader.nextNull();
  }

  @Test
  public void testNumbersParsing() throws IOException {
    String json = "["
        + "0, -0, 1, -1, 9223372036854775807, -9223372036854775808, "
        + "1.0, -1.0, 1e2, 1e+2, 1e-2, 1E2, 1E+2, 1E-2, 3.14159, "
        + "9223372036854775808, -9223372036854775809" // overflow longs handled as numbers
        + "]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    Assert.assertEquals(0L, reader.nextLong());
    Assert.assertEquals(0L, reader.nextLong());
    Assert.assertEquals(1L, reader.nextLong());
    Assert.assertEquals(-1L, reader.nextLong());
    Assert.assertEquals(Long.MAX_VALUE, reader.nextLong());
    Assert.assertEquals(Long.MIN_VALUE, reader.nextLong());

    Assert.assertEquals(1.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(-1.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(0.01, reader.nextDouble(), 0.0);
    Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(100.0, reader.nextDouble(), 0.0);
    Assert.assertEquals(0.01, reader.nextDouble(), 0.0);
    Assert.assertEquals(3.14159, reader.nextDouble(), 0.0);

    Assert.assertEquals(9.223372036854776E18, reader.nextDouble(), 1000.0);
    Assert.assertEquals(-9.223372036854776E18, reader.nextDouble(), 1000.0);
    reader.endArray();
  }

  @Test
  public void testStringsAndEscapes() throws IOException {
    String json = "[\"simple\", \"escapes: \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041 \\u00a0\", 'single quoted', \"multiline\nnewline\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertEquals("simple", reader.nextString());
    Assert.assertEquals("escapes: \" \\ / \b \f \n \r \t A \u00a0", reader.nextString());
    Assert.assertEquals("single quoted", reader.nextString());
    Assert.assertEquals("multiline\nnewline", reader.nextString());
    reader.endArray();
  }

  @Test
  public void testMalformedUnicodeEscape() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"\\u00G1\""));
    try {
      reader.nextString();
      Assert.fail();
    } catch (NumberFormatException expected) {
    }
  }

  @Test
  public void testUnterminatedUnicodeEscape() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"\\u00\""));
    try {
      reader.nextString();
      Assert.fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testUnterminatedEscapeSequenceAtBufferEnd() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"\\"));
    try {
      reader.nextString();
      Assert.fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testNumbersCoercionToString() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[123, 123.456]"));
    reader.beginArray();
    Assert.assertEquals("123", reader.nextString());
    Assert.assertEquals("123.456", reader.nextString());
    reader.endArray();
  }

  @Test
  public void testNumbersCoercionToIntAndLong() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"123\", \"123.0\", \"9223372036854775807\", '456']"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertEquals(123, reader.nextInt());
    Assert.assertEquals(123, reader.nextInt());
    Assert.assertEquals(9223372036854775807L, reader.nextLong());
    Assert.assertEquals(456L, reader.nextLong());
    reader.endArray();
  }

  @Test(expected = NumberFormatException.class)
  public void testIntLossOfPrecisionThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123.45"));
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class)
  public void testLongLossOfPrecisionThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123.45"));
    reader.nextLong();
  }

  @Test(expected = NumberFormatException.class)
  public void testIntOverflowFromLongThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("2147483648"));
    reader.nextInt();
  }

  @Test
  public void testDoubleParsingFromStringAndBuffered() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"1.23\", 4.56, '7.89']"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertEquals(1.23, reader.nextDouble(), 0.0001);
    Assert.assertEquals(4.56, reader.nextDouble(), 0.0001);
    Assert.assertEquals(7.89, reader.nextDouble(), 0.0001);
    reader.endArray();
  }

  @Test
  public void testNaNAndInfinitiesLenientVsStrict() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[NaN, Infinity, -Infinity]"));
    try {
      reader.beginArray();
      reader.nextDouble();
      Assert.fail();
    } catch (MalformedJsonException expected) {
    }

    reader = new JsonReader(new StringReader("[NaN, Infinity, -Infinity]"));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertTrue(Double.isNaN(reader.nextDouble()));
    Assert.assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
    Assert.assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
    reader.endArray();
  }

  @Test
  public void testLenientComments() throws IOException {
    String json = "// comment 1\n"
        + "{\n"
        + "  # comment 2\n"
        + "  \"key\": /* comment 3 */ \"value\" // comment 4\r\n"
        + "}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    Assert.assertEquals("key", reader.nextName());
    Assert.assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedCommentThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/* unterminated"));
    reader.setLenient(true);
    reader.peek();
  }

  @Test
  public void testLenientNameAndValueSeparators() throws IOException {
    String json = "{ a = 1; b => 2, 'c': 3; }";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    Assert.assertEquals("a", reader.nextName());
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals("b", reader.nextName());
    Assert.assertEquals(2, reader.nextInt());
    Assert.assertEquals("c", reader.nextName());
    Assert.assertEquals(3, reader.nextInt());
    reader.endObject();
  }

  @Test
  public void testLenientArraySeparatorsAndEmptyValues() throws IOException {
    String json = "[1; 2, ; 4,]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals(2, reader.nextInt());
    reader.nextNull();
    Assert.assertEquals(4, reader.nextInt());
    reader.nextNull();
    reader.endArray();
  }

  @Test
  public void testSkipValues() throws IOException {
    String json = "{"
        + "\"a\": [1, 2, {\"x\": \"y\"}],"
        + "\"b\": 'single',"
        + "\"c\": \"double\","
        + "\"d\": unquoted,"
        + "\"e\": 123.456,"
        + "\"f\": true"
        + "}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    while (reader.hasNext()) {
      reader.nextName();
      reader.skipValue();
    }
    reader.endObject();
  }

  @Test
  public void testPathTracking() throws IOException {
    String json = "{\"a\": [1, {\"b\": 2}], \"c\": 3}";
    JsonReader reader = new JsonReader(new StringReader(json));
    Assert.assertEquals("$", reader.getPath());
    reader.beginObject();
    Assert.assertEquals("$.", reader.getPath());
    Assert.assertEquals("a", reader.nextName());
    Assert.assertEquals("$.a", reader.getPath());
    reader.beginArray();
    Assert.assertEquals("$.a[0]", reader.getPath());
    Assert.assertEquals(1, reader.nextInt());
    Assert.assertEquals("$.a[1]", reader.getPath());
    reader.beginObject();
    Assert.assertEquals("$.a[1].", reader.getPath());
    Assert.assertEquals("b", reader.nextName());
    Assert.assertEquals("$.a[1].b", reader.getPath());
    Assert.assertEquals(2, reader.nextInt());
    reader.endObject();
    Assert.assertEquals("$.a[2]", reader.getPath());
    reader.endArray();
    Assert.assertEquals("$.a", reader.getPath());
    Assert.assertEquals("c", reader.nextName());
    Assert.assertEquals("$.c", reader.getPath());
    Assert.assertEquals(3, reader.nextInt());
    reader.endObject();
    Assert.assertEquals("$", reader.getPath());
  }

  @Test
  public void testDeepNestingStackGrowth() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      sb.append("[");
    }
    sb.append("1");
    for (int i = 0; i < 50; i++) {
      sb.append("]");
    }
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    for (int i = 0; i < 50; i++) {
      reader.beginArray();
    }
    Assert.assertEquals(1, reader.nextInt());
    for (int i = 0; i < 50; i++) {
      reader.endArray();
    }
  }

  @Test
  public void testBufferRefillingLongLiteral() throws IOException {
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < 2000; i++) {
      sb.append("a");
    }
    sb.append("\"");
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    String str = reader.nextString();
    Assert.assertEquals(2000, str.length());
  }

  @Test
  public void testLongUnquotedLiteralBufferRefilling() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 2000; i++) {
      sb.append("a");
    }
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    reader.setLenient(true);
    String str = reader.nextString();
    Assert.assertEquals(2000, str.length());
  }

  @Test
  public void testInternalAccessPromoteNameToValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"name\": \"value\"}"));
    reader.beginObject();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    Assert.assertEquals(JsonToken.STRING, reader.peek());
    Assert.assertEquals("name", reader.nextString());
    Assert.assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testInternalAccessPromoteNameToValueInvalidState() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, 2]"));
    reader.beginArray();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
  }

  @Test(expected = IllegalStateException.class)
  public void testClosedReaderThrowsOnPeek() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.close();
    reader.peek();
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
    JsonReader reader = new JsonReader(new StringReader("1 2"));
    reader.nextInt();
    reader.peek();
  }

  @Test(expected = EOFException.class)
  public void testUnterminatedArrayThrowsEOF() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, "));
    reader.beginArray();
    reader.nextInt();
    reader.hasNext();
  }

  @Test(expected = EOFException.class)
  public void testUnterminatedObjectThrowsEOF() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\": 1, "));
    reader.beginObject();
    reader.nextName();
    reader.nextInt();
    reader.hasNext();
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedQuotedStringThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"unterminated"));
    reader.nextString();
  }

  @Test(expected = MalformedJsonException.class)
  public void testUnterminatedSingleQuotedStringThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("'unterminated"));
    reader.setLenient(true);
    reader.nextString();
  }

  @Test(expected = IllegalStateException.class)
  public void testBeginArrayExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{}"));
    reader.beginArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndArrayExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{}"));
    reader.beginObject();
    reader.endArray();
  }

  @Test(expected = IllegalStateException.class)
  public void testBeginObjectExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testEndObjectExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginArray();
    reader.endObject();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextNameExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginArray();
    reader.nextName();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextDoubleExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.nextDouble();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextLongExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.nextLong();
  }

  @Test(expected = IllegalStateException.class)
  public void testNextIntExpectedMismatch() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.nextInt();
  }
}
