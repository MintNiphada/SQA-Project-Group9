package com.google.gson.internal.bind;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

public class TypeAdaptersTest {

  private enum SampleEnum {
    @SerializedName(value = "first_val", alternate = {"val_1", "v1"})
    FIRST,
    SECOND {
      @Override
      public String toString() {
        return "custom_second";
      }
    }
  }

  private static class CustomJsonElement extends JsonElement {
    @Override
    public JsonElement deepCopy() {
      return this;
    }
  }

  private static <T> String toJson(TypeAdapter<T> adapter, T value) throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);
    adapter.write(writer, value);
    return out.toString();
  }

  private static <T> T fromJson(TypeAdapter<T> adapter, String json) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    return adapter.read(reader);
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<TypeAdapters> constructor = TypeAdapters.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      Assert.fail("Expected UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      Assert.assertTrue(e.getCause() instanceof UnsupportedOperationException);
    }
  }

  @Test
  public void testClassAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.CLASS, "null"));
    Assert.assertEquals("null", toJson(TypeAdapters.CLASS, null));

    try {
      toJson(TypeAdapters.CLASS, String.class);
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }

    try {
      fromJson(TypeAdapters.CLASS, "\"java.lang.String\"");
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  public void testBitSetAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BIT_SET, "null"));
    Assert.assertEquals("null", toJson(TypeAdapters.BIT_SET, null));

    BitSet bitSet = new BitSet();
    bitSet.set(0);
    bitSet.set(2);
    bitSet.set(3);
    String json = toJson(TypeAdapters.BIT_SET, bitSet);
    Assert.assertEquals("[1,0,1,1]", json);

    BitSet parsed = fromJson(TypeAdapters.BIT_SET, "[1, 0, true, false, \"1\", \"0\"]");
    Assert.assertTrue(parsed.get(0));
    Assert.assertFalse(parsed.get(1));
    Assert.assertTrue(parsed.get(2));
    Assert.assertFalse(parsed.get(3));
    Assert.assertTrue(parsed.get(4));
    Assert.assertFalse(parsed.get(5));

    try {
      fromJson(TypeAdapters.BIT_SET, "[\"invalid\"]");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }

    try {
      fromJson(TypeAdapters.BIT_SET, "[{}]");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testBooleanAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BOOLEAN, "null"));
    Assert.assertTrue(fromJson(TypeAdapters.BOOLEAN, "true"));
    Assert.assertFalse(fromJson(TypeAdapters.BOOLEAN, "false"));
    Assert.assertTrue(fromJson(TypeAdapters.BOOLEAN, "\"true\""));
    Assert.assertFalse(fromJson(TypeAdapters.BOOLEAN, "\"false\""));
    Assert.assertEquals("true", toJson(TypeAdapters.BOOLEAN, true));
    Assert.assertEquals("null", toJson(TypeAdapters.BOOLEAN, null));
  }

  @Test
  public void testBooleanAsStringAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "null"));
    Assert.assertTrue(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"true\""));
    Assert.assertFalse(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"false\""));
    Assert.assertEquals("\"true\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, Boolean.TRUE));
    Assert.assertEquals("\"null\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, null));
  }

  @Test
  public void testByteAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BYTE, "null"));
    Assert.assertEquals((byte) 42, fromJson(TypeAdapters.BYTE, "42").byteValue());
    Assert.assertEquals("42", toJson(TypeAdapters.BYTE, (byte) 42));

    try {
      fromJson(TypeAdapters.BYTE, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testShortAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.SHORT, "null"));
    Assert.assertEquals((short) 1234, fromJson(TypeAdapters.SHORT, "1234").shortValue());
    Assert.assertEquals("1234", toJson(TypeAdapters.SHORT, (short) 1234));

    try {
      fromJson(TypeAdapters.SHORT, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testIntegerAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.INTEGER, "null"));
    Assert.assertEquals(123456, fromJson(TypeAdapters.INTEGER, "123456").intValue());
    Assert.assertEquals("123456", toJson(TypeAdapters.INTEGER, 123456));

    try {
      fromJson(TypeAdapters.INTEGER, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testAtomicIntegerAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.ATOMIC_INTEGER, "null"));
    AtomicInteger parsed = fromJson(TypeAdapters.ATOMIC_INTEGER, "123");
    Assert.assertEquals(123, parsed.get());
    Assert.assertEquals("123", toJson(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(123)));
    Assert.assertEquals("null", toJson(TypeAdapters.ATOMIC_INTEGER, null));

    try {
      fromJson(TypeAdapters.ATOMIC_INTEGER, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testAtomicBooleanAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.ATOMIC_BOOLEAN, "null"));
    AtomicBoolean parsed = fromJson(TypeAdapters.ATOMIC_BOOLEAN, "true");
    Assert.assertTrue(parsed.get());
    Assert.assertEquals("true", toJson(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true)));
    Assert.assertEquals("null", toJson(TypeAdapters.ATOMIC_BOOLEAN, null));
  }

  @Test
  public void testAtomicIntegerArrayAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "null"));
    AtomicIntegerArray array = fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "[10, 20, 30]");
    Assert.assertEquals(3, array.length());
    Assert.assertEquals(10, array.get(0));
    Assert.assertEquals(20, array.get(1));
    Assert.assertEquals(30, array.get(2));

    String json = toJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, array);
    Assert.assertEquals("[10,20,30]", json);
    Assert.assertEquals("null", toJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, null));

    try {
      fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "[10, \"abc\"]");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testLongAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.LONG, "null"));
    Assert.assertEquals(9876543210L, fromJson(TypeAdapters.LONG, "9876543210").longValue());
    Assert.assertEquals("9876543210", toJson(TypeAdapters.LONG, 9876543210L));

    try {
      fromJson(TypeAdapters.LONG, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testFloatAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.FLOAT, "null"));
    Assert.assertEquals(1.25f, fromJson(TypeAdapters.FLOAT, "1.25").floatValue(), 0.0001f);
    Assert.assertEquals("1.25", toJson(TypeAdapters.FLOAT, 1.25f));
  }

  @Test
  public void testDoubleAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.DOUBLE, "null"));
    Assert.assertEquals(1.234567, fromJson(TypeAdapters.DOUBLE, "1.234567").doubleValue(), 0.000001);
    Assert.assertEquals("1.234567", toJson(TypeAdapters.DOUBLE, 1.234567));
  }

  @Test
  public void testNumberAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.NUMBER, "null"));
    Number num = fromJson(TypeAdapters.NUMBER, "123.456");
    Assert.assertTrue(num instanceof LazilyParsedNumber);
    Assert.assertEquals("123.456", num.toString());
    Assert.assertEquals("123.456", toJson(TypeAdapters.NUMBER, num));

    try {
      fromJson(TypeAdapters.NUMBER, "true");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testCharacterAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.CHARACTER, "null"));
    Assert.assertEquals(Character.valueOf('a'), fromJson(TypeAdapters.CHARACTER, "\"a\""));
    Assert.assertEquals("\"a\"", toJson(TypeAdapters.CHARACTER, 'a'));
    Assert.assertEquals("null", toJson(TypeAdapters.CHARACTER, null));

    try {
      fromJson(TypeAdapters.CHARACTER, "\"abc\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testStringAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.STRING, "null"));
    Assert.assertEquals("hello", fromJson(TypeAdapters.STRING, "\"hello\""));
    Assert.assertEquals("true", fromJson(TypeAdapters.STRING, "true"));
    Assert.assertEquals("\"test\"", toJson(TypeAdapters.STRING, "test"));
  }

  @Test
  public void testBigDecimalAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BIG_DECIMAL, "null"));
    BigDecimal bd = fromJson(TypeAdapters.BIG_DECIMAL, "\"123456.789012\"");
    Assert.assertEquals(new BigDecimal("123456.789012"), bd);
    Assert.assertEquals("123456.789012", toJson(TypeAdapters.BIG_DECIMAL, bd));

    try {
      fromJson(TypeAdapters.BIG_DECIMAL, "\"invalid-number\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testBigIntegerAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.BIG_INTEGER, "null"));
    BigInteger bi = fromJson(TypeAdapters.BIG_INTEGER, "\"12345678901234567890\"");
    Assert.assertEquals(new BigInteger("12345678901234567890"), bi);
    Assert.assertEquals("12345678901234567890", toJson(TypeAdapters.BIG_INTEGER, bi));

    try {
      fromJson(TypeAdapters.BIG_INTEGER, "\"invalid-number\"");
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testStringBuilderAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.STRING_BUILDER, "null"));
    StringBuilder sb = fromJson(TypeAdapters.STRING_BUILDER, "\"builder\"");
    Assert.assertEquals("builder", sb.toString());
    Assert.assertEquals("\"builder\"", toJson(TypeAdapters.STRING_BUILDER, sb));
    Assert.assertEquals("null", toJson(TypeAdapters.STRING_BUILDER, null));
  }

  @Test
  public void testStringBufferAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.STRING_BUFFER, "null"));
    StringBuffer sb = fromJson(TypeAdapters.STRING_BUFFER, "\"buffer\"");
    Assert.assertEquals("buffer", sb.toString());
    Assert.assertEquals("\"buffer\"", toJson(TypeAdapters.STRING_BUFFER, sb));
    Assert.assertEquals("null", toJson(TypeAdapters.STRING_BUFFER, null));
  }

  @Test
  public void testUrlAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.URL, "null"));
    Assert.assertNull(fromJson(TypeAdapters.URL, "\"null\""));
    URL url = fromJson(TypeAdapters.URL, "\"https://www.google.com\"");
    Assert.assertEquals("https://www.google.com", url.toExternalForm());
    Assert.assertEquals("\"https://www.google.com\"", toJson(TypeAdapters.URL, url));
    Assert.assertEquals("null", toJson(TypeAdapters.URL, null));
  }

  @Test
  public void testUriAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.URI, "null"));
    Assert.assertNull(fromJson(TypeAdapters.URI, "\"null\""));
    URI uri = fromJson(TypeAdapters.URI, "\"https://www.google.com/test\"");
    Assert.assertEquals("https://www.google.com/test", uri.toString());
    Assert.assertEquals("\"https://www.google.com/test\"", toJson(TypeAdapters.URI, uri));
    Assert.assertEquals("null", toJson(TypeAdapters.URI, null));

    try {
      fromJson(TypeAdapters.URI, "\"some invalid uri: \\\\\"");
      Assert.fail("Expected JsonIOException");
    } catch (JsonIOException expected) {
    }
  }

  @Test
  public void testInetAddressAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.INET_ADDRESS, "null"));
    InetAddress address = fromJson(TypeAdapters.INET_ADDRESS, "\"127.0.0.1\"");
    Assert.assertEquals("127.0.0.1", address.getHostAddress());
    Assert.assertEquals("\"127.0.0.1\"", toJson(TypeAdapters.INET_ADDRESS, address));
    Assert.assertEquals("null", toJson(TypeAdapters.INET_ADDRESS, null));
  }

  @Test
  public void testUuidAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.UUID, "null"));
    UUID uuid = UUID.randomUUID();
    UUID parsed = fromJson(TypeAdapters.UUID, "\"" + uuid.toString() + "\"");
    Assert.assertEquals(uuid, parsed);
    Assert.assertEquals("\"" + uuid.toString() + "\"", toJson(TypeAdapters.UUID, uuid));
    Assert.assertEquals("null", toJson(TypeAdapters.UUID, null));
  }

  @Test
  public void testCurrencyAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.CURRENCY, "null"));
    Currency currency = fromJson(TypeAdapters.CURRENCY, "\"USD\"");
    Assert.assertEquals("USD", currency.getCurrencyCode());
    Assert.assertEquals("\"USD\"", toJson(TypeAdapters.CURRENCY, currency));
    Assert.assertEquals("null", toJson(TypeAdapters.CURRENCY, null));
  }

  @Test
  public void testTimestampFactory() throws IOException {
    Gson gson = new Gson();
    TypeAdapter<Timestamp> adapter = gson.getAdapter(Timestamp.class);
    Assert.assertNotNull(adapter);
    Assert.assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class)));

    long time = 123456789000L;
    Timestamp timestamp = new Timestamp(time);
    String json = gson.toJson(timestamp);
    Timestamp deserialized = gson.fromJson(json, Timestamp.class);
    Assert.assertEquals(timestamp.getTime(), deserialized.getTime());

    Assert.assertNull(gson.fromJson("null", Timestamp.class));
    Assert.assertEquals("null", gson.toJson(null, Timestamp.class));
  }

  @Test
  public void testCalendarAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.CALENDAR, "null"));
    Assert.assertEquals("null", toJson(TypeAdapters.CALENDAR, null));

    Calendar cal = new GregorianCalendar(2023, 5, 15, 10, 30, 45);
    String json = toJson(TypeAdapters.CALENDAR, cal);
    Calendar parsed = fromJson(TypeAdapters.CALENDAR, json);

    Assert.assertEquals(2023, parsed.get(Calendar.YEAR));
    Assert.assertEquals(5, parsed.get(Calendar.MONTH));
    Assert.assertEquals(15, parsed.get(Calendar.DAY_OF_MONTH));
    Assert.assertEquals(10, parsed.get(Calendar.HOUR_OF_DAY));
    Assert.assertEquals(30, parsed.get(Calendar.MINUTE));
    Assert.assertEquals(45, parsed.get(Calendar.SECOND));
  }

  @Test
  public void testLocaleAdapter() throws IOException {
    Assert.assertNull(fromJson(TypeAdapters.LOCALE, "null"));
    Assert.assertEquals("null", toJson(TypeAdapters.LOCALE, null));

    Locale l1 = fromJson(TypeAdapters.LOCALE, "\"en\"");
    Assert.assertEquals(new Locale("en"), l1);

    Locale l2 = fromJson(TypeAdapters.LOCALE, "\"en_US\"");
    Assert.assertEquals(new Locale("en", "US"), l2);

    Locale l3 = fromJson(TypeAdapters.LOCALE, "\"en_US_POSIX\"");
    Assert.assertEquals(new Locale("en", "US", "POSIX"), l3);

    Assert.assertEquals("\"en_US\"", toJson(TypeAdapters.LOCALE, Locale.US));
  }

  @Test
  public void testJsonElementAdapter() throws IOException {
    Assert.assertEquals(JsonNull.INSTANCE, fromJson(TypeAdapters.JSON_ELEMENT, "null"));
    Assert.assertEquals(new JsonPrimitive("test"), fromJson(TypeAdapters.JSON_ELEMENT, "\"test\""));
    Assert.assertEquals(new JsonPrimitive(123), fromJson(TypeAdapters.JSON_ELEMENT, "123"));
    Assert.assertEquals(new JsonPrimitive(true), fromJson(TypeAdapters.JSON_ELEMENT, "true"));

    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("a"));
    array.add(new JsonPrimitive(1));
    Assert.assertEquals(array, fromJson(TypeAdapters.JSON_ELEMENT, "[\"a\",1]"));

    JsonObject object = new JsonObject();
    object.addProperty("key", "value");
    Assert.assertEquals(object, fromJson(TypeAdapters.JSON_ELEMENT, "{\"key\":\"value\"}"));

    Assert.assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, null));
    Assert.assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, JsonNull.INSTANCE));
    Assert.assertEquals("100", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive(100)));
    Assert.assertEquals("true", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive(true)));
    Assert.assertEquals("\"str\"", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive("str")));
    Assert.assertEquals("[\"a\",1]", toJson(TypeAdapters.JSON_ELEMENT, array));
    Assert.assertEquals("{\"key\":\"value\"}", toJson(TypeAdapters.JSON_ELEMENT, object));

    try {
      toJson(TypeAdapters.JSON_ELEMENT, new CustomJsonElement());
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testEnumAdapter() throws IOException {
    Gson gson = new Gson();
    TypeAdapter<SampleEnum> adapter = gson.getAdapter(SampleEnum.class);

    Assert.assertNull(adapter.fromJson("null"));
    Assert.assertEquals(SampleEnum.FIRST, adapter.fromJson("\"first_val\""));
    Assert.assertEquals(SampleEnum.FIRST, adapter.fromJson("\"val_1\""));
    Assert.assertEquals(SampleEnum.FIRST, adapter.fromJson("\"v1\""));
    Assert.assertEquals(SampleEnum.SECOND, adapter.fromJson("\"SECOND\""));
    Assert.assertNull(adapter.fromJson("\"UNKNOWN\""));

    Assert.assertEquals("\"first_val\"", adapter.toJson(SampleEnum.FIRST));
    Assert.assertEquals("\"SECOND\"", adapter.toJson(SampleEnum.SECOND));
    Assert.assertEquals("null", adapter.toJson(null));

    Assert.assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(String.class)));
    Assert.assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(Enum.class)));
  }

  @Test
  public void testFactoriesToStringAndMatching() {
    Gson gson = new Gson();

    TypeAdapterFactory typeTokenFactory = TypeAdapters.newFactory(TypeToken.get(String.class), TypeAdapters.STRING);
    Assert.assertNotNull(typeTokenFactory.create(gson, TypeToken.get(String.class)));
    Assert.assertNull(typeTokenFactory.create(gson, TypeToken.get(Integer.class)));

    Assert.assertNotNull(TypeAdapters.CLASS_FACTORY.create(gson, TypeToken.get(Class.class)));
    Assert.assertNull(TypeAdapters.CLASS_FACTORY.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(TypeAdapters.CLASS_FACTORY.toString().contains("Factory[type="));

    Assert.assertNotNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(boolean.class)));
    Assert.assertNotNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(Boolean.class)));
    Assert.assertNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(Integer.class)));
    Assert.assertTrue(TypeAdapters.BOOLEAN_FACTORY.toString().contains("Factory[type="));

    Assert.assertNotNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(Calendar.class)));
    Assert.assertNotNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(GregorianCalendar.class)));
    Assert.assertNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(TypeAdapters.CALENDAR_FACTORY.toString().contains("Factory[type="));

    Assert.assertNotNull(TypeAdapters.INET_ADDRESS_FACTORY.create(gson, TypeToken.get(InetAddress.class)));
    Assert.assertNull(TypeAdapters.INET_ADDRESS_FACTORY.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(TypeAdapters.INET_ADDRESS_FACTORY.toString().contains("Factory[typeHierarchy="));
  }

  @Test
  public void testTypeHierarchyFactoryTypeMismatch() throws IOException {
    Gson gson = new Gson();
    TypeAdapterFactory hierarchyFactory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
    TypeAdapter<Integer> adapter = hierarchyFactory.create(gson, TypeToken.get(Integer.class));
    Assert.assertNotNull(adapter);

    try {
      adapter.fromJson("123");
      Assert.fail("Expected JsonSyntaxException because LazilyParsedNumber is not an Integer");
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testAllFactoriesCoverage() {
    Gson gson = new Gson();
    Assert.assertNotNull(TypeAdapters.BIT_SET_FACTORY.create(gson, TypeToken.get(BitSet.class)));
    Assert.assertNotNull(TypeAdapters.BYTE_FACTORY.create(gson, TypeToken.get(byte.class)));
    Assert.assertNotNull(TypeAdapters.SHORT_FACTORY.create(gson, TypeToken.get(short.class)));
    Assert.assertNotNull(TypeAdapters.INTEGER_FACTORY.create(gson, TypeToken.get(int.class)));
    Assert.assertNotNull(TypeAdapters.ATOMIC_INTEGER_FACTORY.create(gson, TypeToken.get(AtomicInteger.class)));
    Assert.assertNotNull(TypeAdapters.ATOMIC_BOOLEAN_FACTORY.create(gson, TypeToken.get(AtomicBoolean.class)));
    Assert.assertNotNull(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY.create(gson, TypeToken.get(AtomicIntegerArray.class)));
    Assert.assertNotNull(TypeAdapters.NUMBER_FACTORY.create(gson, TypeToken.get(Number.class)));
    Assert.assertNotNull(TypeAdapters.CHARACTER_FACTORY.create(gson, TypeToken.get(char.class)));
    Assert.assertNotNull(TypeAdapters.STRING_FACTORY.create(gson, TypeToken.get(String.class)));
    Assert.assertNotNull(TypeAdapters.STRING_BUILDER_FACTORY.create(gson, TypeToken.get(StringBuilder.class)));
    Assert.assertNotNull(TypeAdapters.STRING_BUFFER_FACTORY.create(gson, TypeToken.get(StringBuffer.class)));
    Assert.assertNotNull(TypeAdapters.URL_FACTORY.create(gson, TypeToken.get(URL.class)));
    Assert.assertNotNull(TypeAdapters.URI_FACTORY.create(gson, TypeToken.get(URI.class)));
    Assert.assertNotNull(TypeAdapters.UUID_FACTORY.create(gson, TypeToken.get(UUID.class)));
    Assert.assertNotNull(TypeAdapters.CURRENCY_FACTORY.create(gson, TypeToken.get(Currency.class)));
    Assert.assertNotNull(TypeAdapters.LOCALE_FACTORY.create(gson, TypeToken.get(Locale.class)));
    Assert.assertNotNull(TypeAdapters.JSON_ELEMENT_FACTORY.create(gson, TypeToken.get(JsonElement.class)));
  }
}
