package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class TypeAdaptersTest {

  private JsonReader reader(String json) {
    return new JsonReader(new StringReader(json));
  }

  private String write(Object adapter, Object value) throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    ((TypeAdapter) adapter).write(jw, value);
    jw.close();
    return sw.toString();
  }

  @Test
  public void testClassWriteNull() throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    TypeAdapters.CLASS.write(jw, null);
    jw.close();
    assertEquals("null", sw.toString());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testClassWriteNonNull() throws IOException {
    TypeAdapters.CLASS.write(new JsonWriter(new StringWriter()), String.class);
  }

  @Test
  public void testClassReadNull() throws IOException {
    JsonReader jr = reader("null");
    assertNull(TypeAdapters.CLASS.read(jr));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testClassReadNonNull() throws IOException {
    TypeAdapters.CLASS.read(reader("\"java.lang.String\""));
  }

  // BIT_SET tests
  @Test
  public void testBitSetReadNull() throws IOException {
    assertEquals(null, TypeAdapters.BIT_SET.read(reader("null")));
  }

  @Test
  public void testBitSetReadEmptyArray() throws IOException {
    BitSet bs = TypeAdapters.BIT_SET.read(reader("[]"));
    assertTrue(bs.isEmpty());
  }

  @Test
  public void testBitSetReadNumbers() throws IOException {
    BitSet bs = TypeAdapters.BIT_SET.read(reader("[1,0,1,0]"));
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertTrue(bs.get(2));
    assertFalse(bs.get(3));
    assertEquals(4, bs.length());
  }

  @Test
  public void testBitSetReadBooleans() throws IOException {
    BitSet bs = TypeAdapters.BIT_SET.read(reader("[true,false,true]"));
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertTrue(bs.get(2));
  }

  @Test
  public void testBitSetReadStrings() throws IOException {
    BitSet bs = TypeAdapters.BIT_SET.read(reader("[\"1\",\"0\",\"1\"]"));
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertTrue(bs.get(2));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBitSetReadInvalidString() throws IOException {
    TypeAdapters.BIT_SET.read(reader("[\"abc\"]"));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBitSetReadInvalidToken() throws IOException {
    TypeAdapters.BIT_SET.read(reader("[\"invalid\", null]"));
  }

  @Test
  public void testBitSetWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.BIT_SET, null));
  }

  @Test
  public void testBitSetWrite() throws IOException {
    BitSet bs = new BitSet();
    bs.set(0);
    bs.set(2);
    assertEquals("[1,0,1]", write(TypeAdapters.BIT_SET, bs));
  }

  // BOOLEAN
  @Test
  public void testBooleanReadNull() throws IOException {
    assertNull(TypeAdapters.BOOLEAN.read(reader("null")));
  }

  @Test
  public void testBooleanReadTrue() throws IOException {
    assertTrue(TypeAdapters.BOOLEAN.read(reader("true")));
  }

  @Test
  public void testBooleanReadFalse() throws IOException {
    assertFalse(TypeAdapters.BOOLEAN.read(reader("false")));
  }

  @Test
  public void testBooleanReadStringTrue() throws IOException {
    assertTrue(TypeAdapters.BOOLEAN.read(reader("\"true\"")));
  }

  @Test
  public void testBooleanReadStringFalse() throws IOException {
    assertFalse(TypeAdapters.BOOLEAN.read(reader("\"false\"")));
  }

  @Test
  public void testBooleanReadStringArbitrary() throws IOException {
    assertFalse(TypeAdapters.BOOLEAN.read(reader("\"foo\"")));
  }

  @Test
  public void testBooleanWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.BOOLEAN, null));
  }

  @Test
  public void testBooleanWriteTrue() throws IOException {
    assertEquals("true", write(TypeAdapters.BOOLEAN, true));
  }

  // BOOLEAN_AS_STRING
  @Test
  public void testBooleanAsStringReadNull() throws IOException {
    assertNull(TypeAdapters.BOOLEAN_AS_STRING.read(reader("null")));
  }

  @Test
  public void testBooleanAsStringReadTrue() throws IOException {
    assertTrue(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"true\"")));
  }

  @Test
  public void testBooleanAsStringReadFalse() throws IOException {
    assertFalse(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"false\"")));
  }

  @Test
  public void testBooleanAsStringWriteNull() throws IOException {
    assertEquals("\"null\"", write(TypeAdapters.BOOLEAN_AS_STRING, null));
  }

  @Test
  public void testBooleanAsStringWriteTrue() throws IOException {
    assertEquals("\"true\"", write(TypeAdapters.BOOLEAN_AS_STRING, true));
  }

  // BYTE
  @Test
  public void testByteReadNull() throws IOException {
    assertNull(TypeAdapters.BYTE.read(reader("null")));
  }

  @Test
  public void testByteReadNormal() throws IOException {
    assertEquals((byte) 42, TypeAdapters.BYTE.read(reader("42")));
  }

  @Test
  public void testByteReadNegative() throws IOException {
    assertEquals((byte) -7, TypeAdapters.BYTE.read(reader("-7")));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testByteReadInvalid() throws IOException {
    TypeAdapters.BYTE.read(reader("\"abc\""));
  }

  @Test
  public void testByteWrite() throws IOException {
    assertEquals("10", write(TypeAdapters.BYTE, 10));
  }

  // SHORT
  @Test
  public void testShortReadNull() throws IOException {
    assertNull(TypeAdapters.SHORT.read(reader("null")));
  }

  @Test
  public void testShortReadNormal() throws IOException {
    assertEquals((short) 123, TypeAdapters.SHORT.read(reader("123")));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testShortReadInvalid() throws IOException {
    TypeAdapters.SHORT.read(reader("\"abc\""));
  }

  @Test
  public void testShortWrite() throws IOException {
    assertEquals("123", write(TypeAdapters.SHORT, (short) 123));
  }

  // INTEGER
  @Test
  public void testIntegerReadNull() throws IOException {
    assertNull(TypeAdapters.INTEGER.read(reader("null")));
  }

  @Test
  public void testIntegerReadNormal() throws IOException {
    assertEquals(123, TypeAdapters.INTEGER.read(reader("123")));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testIntegerReadInvalid() throws IOException {
    TypeAdapters.INTEGER.read(reader("\"abc\""));
  }

  @Test
  public void testIntegerWrite() throws IOException {
    assertEquals("123", write(TypeAdapters.INTEGER, 123));
  }

  // ATOMIC_INTEGER
  @Test
  public void testAtomicIntegerRead() throws IOException {
    AtomicInteger ai = TypeAdapters.ATOMIC_INTEGER.read(reader("42"));
    assertEquals(42, ai.get());
  }

  @Test(expected = JsonSyntaxException.class)
  public void testAtomicIntegerReadInvalid() throws IOException {
    TypeAdapters.ATOMIC_INTEGER.read(reader("\"abc\""));
  }

  @Test
  public void testAtomicIntegerWrite() throws IOException {
    assertEquals("7", write(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(7)));
  }

  // ATOMIC_BOOLEAN
  @Test
  public void testAtomicBooleanRead() throws IOException {
    AtomicBoolean ab = TypeAdapters.ATOMIC_BOOLEAN.read(reader("true"));
    assertTrue(ab.get());
  }

  @Test
  public void testAtomicBooleanWrite() throws IOException {
    assertEquals("true", write(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true)));
  }

  // ATOMIC_INTEGER_ARRAY
  @Test
  public void testAtomicIntegerArrayRead() throws IOException {
    AtomicIntegerArray aia = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(reader("[1,2,3]"));
    assertEquals(3, aia.length());
    assertEquals(1, aia.get(0));
    assertEquals(2, aia.get(1));
    assertEquals(3, aia.get(2));
  }

  @Test
  public void testAtomicIntegerArrayReadEmpty() throws IOException {
    AtomicIntegerArray aia = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(reader("[]"));
    assertEquals(0, aia.length());
  }

  @Test(expected = JsonSyntaxException.class)
  public void testAtomicIntegerArrayReadInvalid() throws IOException {
    TypeAdapters.ATOMIC_INTEGER_ARRAY.read(reader("[\"abc\"]"));
  }

  @Test
  public void testAtomicIntegerArrayWrite() throws IOException {
    AtomicIntegerArray aia = new AtomicIntegerArray(new int[]{5,7,9});
    assertEquals("[5,7,9]", write(TypeAdapters.ATOMIC_INTEGER_ARRAY, aia));
  }

  // LONG
  @Test
  public void testLongReadNull() throws IOException {
    assertNull(TypeAdapters.LONG.read(reader("null")));
  }

  @Test
  public void testLongReadNormal() throws IOException {
    assertEquals(1234567890123L, TypeAdapters.LONG.read(reader("1234567890123")));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testLongReadInvalid() throws IOException {
    TypeAdapters.LONG.read(reader("\"abc\""));
  }

  @Test
  public void testLongWrite() throws IOException {
    assertEquals("1234567890123", write(TypeAdapters.LONG, 1234567890123L));
  }

  // FLOAT
  @Test
  public void testFloatReadNull() throws IOException {
    assertNull(TypeAdapters.FLOAT.read(reader("null")));
  }

  @Test  public void testFloatReadNormal() throws IOException {
    assertEquals(3.14f, TypeAdapters.FLOAT.read(reader("3.14")));
  }

  @Test  public void testFloatWrite() throws IOException {
    assertEquals("3.14", write(TypeAdapters.FLOAT, 3.14f));
  }

  // DOUBLE
  @Test  public void testDoubleReadNull() throws IOException {
    assertNull(TypeAdapters.DOUBLE.read(reader("null")));
  }

  @Test  public void testDoubleReadNormal() throws IOException {
    assertEquals(2.718, TypeAdapters.DOUBLE.read(reader("2.718")));
  }

  @Test  public void testDoubleWrite() throws IOException {
    assertEquals("2.718", write(TypeAdapters.DOUBLE, 2.718));
  }

  // NUMBER
  @Test
  public void testNumberReadNull() throws IOException {
    assertNull(TypeAdapters.NUMBER.read(reader("null")));
  }

  @Test
  public void testNumberReadNumber() throws IOException {
    Number n = TypeAdapters.NUMBER.read(reader("42"));
    assertTrue(n instanceof com.google.gson.internal.LazilyParsedNumber);
    assertEquals(42, n.intValue());
  }

  @Test(expected = JsonSyntaxException.class)
  public void testNumberReadInvalid() throws IOException {
    TypeAdapters.NUMBER.read(reader("\"abc\""));
  }

  @Test
  public void testNumberWrite() throws IOException {
    assertEquals("42", write(TypeAdapters.NUMBER, 42));
  }

  // CHARACTER
  @Test
 public void testCharacterReadNull() throws IOException {
    assertNull(TypeAdapters.CHARACTER.read(reader("null")));
  }

  @Test
  public void testCharacterReadValid() throws IOException {
    assertEquals('A', (char) TypeAdapters.CHARACTER.read(reader("\"A\"")));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testCharacterReadInvalidLength() throws IOException {
    TypeAdapters.CHARACTER.read(reader("\"AB\""));
  }

  @Test
  public void testCharacterWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.CHARACTER, null));
  }

  @Test
  public void testCharacterWriteValid() throws IOException {
    assertEquals("\"A\"", write(TypeAdapters.CHARACTER, 'A'));
  }

  // STRING
  @Test
  public void testStringReadNull() throws IOException {
    assertNull(TypeAdapters.STRING.read(reader("null")));
  }

  @Test
  public void testStringReadString() throws IOException {
    assertEquals("hello", TypeAdapters.STRING.read(reader("\"hello\"")));
  }

  @Test
  public void testStringReadBoolean() throws IOException {
    assertEquals("true", TypeAdapters.STRING.read(reader("true")));
  }

  @Test
  public void testStringWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.STRING, null));
  }

  @Test
  public void testStringWrite() throws IOException {
    assertEquals("\"hello\"", write(TypeAdapters.STRING, "hello"));
  }

  // BIG_DECIMAL
  @Test
  public void testBigDecimalReadNull() throws IOException {
    assertNull(TypeAdapters.BIG_DECIMAL.read(reader("null")));
  }

  @Test
  public void testBigDecimalReadNormal() throws IOException {
    BigDecimal bd = TypeAdapters.BIG_DECIMAL.read(reader("\"123.45\""));
    assertEquals(new BigDecimal("123.45"), bd);
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBigDecimalReadInvalid() throws IOException {
    TypeAdapters.BIG_DECIMAL.read(reader("\"abc\""));
  }

  @Test
  public void testBigDecimalWrite() throws IOException {
    assertEquals("123.45", write(TypeAdapters.BIG_DECIMAL, new BigDecimal("123.45")));
  }

  // BIG_INTEGER
  @Test
  public void testBigIntegerReadNull() throws IOException {
    assertNull(TypeAdapters.BIG_INTEGER.read(reader("null")));
  }

  @Test
  public void testBigIntegerReadNormal() throws IOException {
    BigInteger bi = TypeAdapters.BIG_INTEGER.read(reader("\"12345678901234567890\""));
    assertEquals(new BigInteger("12345678901234567890"), bi);
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBigIntegerReadInvalid() throws IOException {
    TypeAdapters.BIG_INTEGER.read(reader("\"abc\""));
  }

  @Test
  public void testBigIntegerWrite() throws IOException {
    assertEquals("12345678901234567890", write(TypeAdapters.BIG_INTEGER, new BigInteger("12345678901234567890")));
  }

  // STRING_BUILDER
  @Test
  public void testStringBuilderReadNull() throws IOException {
    assertNull(TypeAdapters.STRING_BUILDER.read(reader("null")));
  }

  @Test
  public void testStringBuilderRead() throws IOException {
    StringBuilder sb = TypeAdapters.STRING_BUILDER.read(reader("\"hello\""));
    assertEquals("hello", sb.toString());
  }

  @Test
  public void testStringBuilderWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.STRING_BUILDER, null));
  }

  @Test
  public void testStringBuilderWrite() throws IOException {
    assertEquals("\"hello\"", write(TypeAdapters.STRING_BUILDER, new StringBuilder("hello")));
  }

  // STRING_BUFFER
  @Test
  public void testStringBufferReadNull() throws IOException {
    assertNull(TypeAdapters.STRING_BUFFER.read(reader("null")));
  }

  @Test
  public void testStringBufferRead() throws IOException {
    StringBuffer sb = TypeAdapters.STRING_BUFFER.read(reader("\"world\""));
    assertEquals("world", sb.toString());
  }

  @Test
  public void testStringBufferWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.STRING_BUFFER, null));
  }

  @Test
  public void testStringBufferWrite() throws IOException {
    assertEquals("\"world\"", write(TypeAdapters.STRING_BUFFER, new StringBuffer("world")));
  }

  // URL
  @Test
  public void testUrlReadNull() throws IOException {
    assertNull(TypeAdapters.URL.read(reader("null")));
  }

  @Test
  public void testUrlReadNullString() throws IOException {
    assertNull(TypeAdapters.URL.read(reader("\"null\"")));
  }

  @Test
  public void testUrlReadValid() throws IOException {
    URL url = TypeAdapters.URL.read(reader("\"http://example.com\""));
    assertEquals("http://example.com", url.toString());
  }

  @Test
  public void testUrlWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.URL, null));
  }

  @Test
  public void testUrlWrite() throws IOException {
    URL url = new URL("http://example.com");
    assertEquals("\"http://example.com\"", write(TypeAdapters.URL, url));
  }

  // URI
  @Test
  public void testUriReadNull() throws IOException {
    assertNull(TypeAdapters.URI.read(reader("null")));
  }

  @Test
  public void testUriReadNullString() throws IOException {
    assertNull(TypeAdapters.URI.read(reader("\"null\"")));
  }

  @Test
  public void testUriReadValid() throws IOException {
    URI uri = TypeAdapters.URI.read(reader("\"http://example.com\""));
    assertEquals(new URI("http://example.com"), uri);
  }

  @Test  public void testUriWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.URI, null));
  }

  @Test  public void testUriWrite() throws IOException {
    URI uri = new URI("http://example.com");
    assertEquals("\"http://example.com\"", write(TypeAdapters.URI, uri));
  }

  // INET_ADDRESS
  @Test
  public void testInetAddressReadNull() throws IOException {
    assertNull(TypeAdapters.INET_ADDRESS.read(reader("null")));
  }

  @Test
  public void testInetAddressRead() throws IOException {
    java.net.InetAddress ia = TypeAdapters.INET_ADDRESS.read(reader("\"127.0.0.1\""));
    assertEquals("127.0.0.1", ia.getHostAddress()));
  }

  @Test
  public void testInetAddressWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.INET_ADDRESS, null));
  }

  @Test
  public void testInetAddressWrite() throws IOException {
    java.net.InetAddress ia = java.net.InetAddress.getByName("127.0.0.1");
    assertEquals("\"127.0.0.1\"", write(TypeAdapters.INET_ADDRESS, ia));
  }

  // UUID
  @Test
  public void testUuidReadNull() throws IOException {
    assertNull(TypeAdapters.UUID.read(reader("null")));
  }

  @Test
  public void testUuidReadValid() throws IOException {
    UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    assertEquals(uuid, TypeAdapters.UUID.read(reader("\"" + uuid.toString() + "\"")));
  }

  @Test
  public void testUuidWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.UUID, null));
  }

  @Test
  public void testUuidWrite() throws IOException {
    UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    assertEquals("\"550e8400-e29b-41d4-a716-446655440000\"", write(TypeAdapters.UUID, uuid));
  }

  // CURRENCY
  @Test
  public void testCurrencyRead() throws IOException {
    Currency c = TypeAdapters.CURRENCY.read(reader("\"USD\""));
    assertEquals("USD", c.getCurrencyCode());
  }

  @Test
  public void testCurrencyWrite() throws IOException {
    assertEquals("\"USD\"", write(TypeAdapters.CURRENCY, Currency.getInstance("USD")));
  }

  // TIMESTAMP_FACTORY
  @Test
  public void testTimestampFactoryCreate() {
    Gson gson = new Gson();
    assertNotNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, com.google.gson.reflect.TypeToken.get(Timestamp.class)));
    assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, com.google.gson.reflect.TypeToken.get(java.util.Date.class)));
  }

  @Test
  public void testTimestampAdapterReadWrite() throws IOException {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    TypeAdapter<Timestamp> adapter = (TypeAdapter<Timestamp>) TypeAdapters.TIMESTAMP_FACTORY.create(gson, com.google.gson.reflect.TypeToken.get(Timestamp.class));
    assertNotNull(adapter);
    String json = "\"2011-06-30T12:34:56.789+0000\"";
    Timestamp ts = adapter.read(reader(json));
    assertEquals(1309437296789L, ts.getTime());
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, new Timestamp(1309437296789L));
    jw.close();
    assertEquals(json, sw.toString());
  }

  // CALENDAR
  @Test
  public void testCalendarReadNull() throws IOException {
    assertNull(TypeAdapters.CALENDAR.read(reader("null")));
  }

  @Test
  public void testCalendarReadNormal() throws IOException {
    String json = "{\"year\":2011,\"month\":6,\"dayOfMonth\":30,\"hourOfDay\":12,\"minute\":34,\"second\":56}";
    Calendar cal = TypeAdapters.CALENDAR.read(reader(json));
    assertEquals(2011, cal.get(Calendar.YEAR));
    assertEquals(6, cal.get(Calendar.MONTH));
    assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(34, cal.get(Calendar.MINUTE));
    assertEquals(56, cal.get(Calendar.SECOND));
  }

  @Test
  public void testCalendarReadPartial() throws IOException {
    String json = "{\"year\":2011,\"month\":2}";
    Calendar cal = TypeAdapters.CALENDAR.read(reader(json));
    assertEquals(2011, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(0, cal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testCalendarWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.CALENDAR, null));
  }

  @Test
  public void testCalendarWrite() throws IOException {
    Calendar cal = new GregorianCalendar(2011, 6, 30, 12, 34, 56);
    String json = write(TypeAdapters.CALENDAR, cal);
    assertTrue(json.contains("\"year\":2011"\));
    assertTrue(json.contains("\"month\":6"));
    assertTrue(json.contains("\"dayOfMonth\":30"));
  }

  // LOCALE
  @Test
  public void testLocaleReadNull() throws IOException {
    assertNull(TypeAdapters.LOCALE.read(reader("null")));
  }

  @Test
  public void testLocaleReadLanguageOnly() throws IOException {
    Locale l = TypeAdapters.LOCALE.read(reader("\"en\""));
    assertEquals(new Locale("en"), l);
  }

  @Test
  public void testLocaleReadLanguageCountry() throws IOException {
    Locale l = TypeAdapters.LOCALE.read(reader("\"en_US\""));
    assertEquals(new Locale("en", "US"), l);
  }

  @Test
  public void testLocaleReadFull() throws IOException {
    Locale l = TypeAdapters.LOCALE.read(reader("\"en_US_CA\""));
    assertEquals(new Locale("en", "US", "CA"), l);
  }

  @Test
  public void testLocaleWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.LOCALE, null));
  }

  @Test
  public void testLocaleWrite() throws IOException {
    Locale l = new Locale("en", "US", "CA");
    assertEquals("\"en_US_CA\"", write(TypeAdapters.LOCALE, l));
  }

  // JSON_ELEMENT
  @Test
  public void testJsonElementReadNull() throws IOException {
    assertEquals(com.google.gson.JsonNull.INSTANCE, TypeAdapters.JSON_ELEMENT.read(reader("null")));
  }

  @Test
  public void testJsonElementReadString() throws IOException {
    assertEquals(new com.google.gson.JsonPrimitive("hello"), TypeAdapters.JSON_ELEMENT.read(reader("\"hello\"")));
  }

  @Test
  public void testJsonElementReadNumber() throws IOException {
    assertEquals(new com.google.gson.JsonPrimitive(42), TypeAdapters.JSON_ELEMENT.read(reader("42")));
  }

  @Test
  public void testJsonElementReadBoolean() throws IOException {
    assertEquals(new com.google.gson.JsonPrimitive(true), TypeAdapters.JSON_ELEMENT.read(reader("true")));
  }

  @Test
  public void testJsonElementReadArray() throws IOException {
    JsonElement el = TypeAdapters.JSON_ELEMENT.read(reader("[1,\"a\",true]"));
    assertTrue(el.isJsonArray());
    assertEquals(3, el.getAsJsonArray().size());
  }

  @Test
  public void testJsonElementReadObject() throws IOException {
    JsonElement el = TypeAdapters.JSON_ELEMENT.read(reader("{\"key\":\"value\"}"));
    assertTrue(el.isJsonObject());
    assertEquals("value", el.getAsJsonObject().get("key").getAsString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJsonElementReadInvalid() throws IOException {
    TypeAdapters.JSON_ELEMENT.read(reader(""));
  }

  @Test
  public void testJsonElementWriteNull() throws IOException {
    assertEquals("null", write(TypeAdapters.JSON_ELEMENT, null));
  }

  @Test
  public void testJsonElementWritePrimitiveNumber() throws IOException {
    assertEquals("42", write(TypeAdapters.JSON_ELEMENT, new com.google.gson.JsonPrimitive(42)));
  }

  @Test
  public void testJsonElementWritePrimitiveBoolean() throws IOException {
    assertEquals("true", write(TypeAdapters.JSON_ELEMENT, new com.google.gson.JsonPrimitive(true)));
  }

  @Test
  public void testJsonElementWritePrimitiveString() throws IOException {
    assertEquals("\"hello\"", write(TypeAdapters.JSON_ELEMENT, new com.google.gson.JsonPrimitive("hello")));
  }

  @Test
  public void testJsonElementWriteArray() throws IOException {
    JsonArray array = new JsonArray();
    array.add(1);
    array.add("a");
    assertEquals("[1,\"a\"]", write(TypeAdapters.JSON_ELEMENT, array));
  }

  @Test
  public void testJsonElementWriteObject() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("key", "value");
    assertEquals("{\"key\":\"value\"}", write(TypeAdapters.JSON_ELEMENT, obj));
  }

  // ENUM FACTORY
  enum TestEnum { FOO, BAR; }

  @Test
  public void testEnumFactoryCreateEnum() {
    TypeAdapterFactory f = TypeAdapters.ENUM_FACTORY;
    assertNotNull(f.create(new Gson(), com.google.gson.reflect.TypeToken.get(TestEnum.class));
  }

  @Test
  public void testEnumFactoryCreateNonEnum() {
    assertNull(TypeAdapters.ENUM_FACTORY.create(new Gson(), com.google.gson.reflect.TypeToken.get(String.class)));
  }

  @Test
  public void testEnumAdapterReadWrite() throws IOException {
    TypeAdapter<TestEnum> adapter = (TypeAdapter<TestEnum>) TypeAdapters.ENUM_FACTORY.create(new Gson(), com.google.gson.reflect.TypeToken.get(TestEnum.class));
    assertNotNull(adapter);
    assertEquals("FOO", write(adapter, TestEnum.FOO));
    assertEquals(TestEnum.FOO, adapter.read(reader("\"FOO\"")));
    // null handling
    assertNull(adapter.read(reader("null")));
    assertEquals("null", write(adapter, null));
  }

  // Factory methods test
  @Test
  public void testNewFactoryType() {
    TypeAdapter<String> adapter = TypeAdapters.STRING;
    TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, adapter);
    assertNotNull(factory);
    assertEquals(adapter, factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(String.class)));
    assertNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(Integer.class)));
  }

  @Test
  public void testNewFactoryUnboxedBoxed() {
    TypeAdapter<Number> adapter = TypeAdapters.INTEGER;
    TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, adapter);
    assertNotNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(int.class)));
    assertNotNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(Integer.class)));
    assertNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(Number.class)));
  }

  @Test
  public void testNewFactoryForMultipleTypes() {
    TypeAdapter<?> adapter = TypeAdapters.CALENDAR;
    TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, adapter);
    assertNotNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(Calendar.class)));
    assertNotNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(GregorianCalendar.class)));
    assertNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(java.util.Date.class)));
  }

  @Test
  public void testNewTypeHierarchyFactory() {
    TypeAdapter<java.net.InetAddress> adapter = TypeAdapters.INET_ADDRESS;
    TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(java.net.InetAddress.class, adapter);
    assertNotNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(java.net.InetAddress.class)));

    // sublass, if any? not needed
    assertNull(factory.create(new Gson(), com.google.gson.reflect.TypeToken.get(String.class)));
  }

  // Test toString of factories
  @Test
  public void testFactoryToString() {
    TypeAdapterFactory f = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
    assertTrue(f.toString().contains("Factory[type="));
    assertTrue(f.toString().contains("java.lang.String"));
  }
}
```
