package com.google.gson.internal.bind;

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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

public class TypeAdaptersTest {

  private final Gson gson = new Gson();

  private enum SampleEnum {
    @SerializedName(value = "FIRST", alternate = {"first", "1st"})
    FIRST,
    @SerializedName("SECOND")
    SECOND,
    THIRD {
      @Override
      public String toString() {
        return "CustomThird";
      }
    }
  }

  private <T> String toJson(TypeAdapter<T> adapter, T value) throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, value);
    return stringWriter.toString();
  }

  private <T> T fromJson(TypeAdapter<T> adapter, String json) throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    return adapter.read(jsonReader);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testConstructorIsPrivate() throws Throwable {
    Constructor<TypeAdapters> constructor = TypeAdapters.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testClassAdapter() throws IOException {
    Assert.assertEquals("null", toJson(TypeAdapters.CLASS, null));
    Assert.assertNull(fromJson(TypeAdapters.CLASS, "null"));

    try {
      toJson(TypeAdapters.CLASS, String.class);
      Assert.fail();
    } catch (UnsupportedOperationException expected) {}

    try {
      fromJson(TypeAdapters.CLASS, "\"java.lang.String\"");
      Assert.fail();
    } catch (UnsupportedOperationException expected) {}

    Assert.assertNotNull(TypeAdapters.CLASS_FACTORY.create(gson, TypeToken.get(Class.class)));
    Assert.assertNull(TypeAdapters.CLASS_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  @Test
  public void testBitSetAdapter() throws IOException {
    BitSet bitSet = new BitSet();
    bitSet.set(0);
    bitSet.set(2);
    bitSet.set(3);

    String json = toJson(TypeAdapters.BIT_SET, bitSet);
    Assert.assertEquals("[1,0,1,1]", json);

    BitSet readBitSet = fromJson(TypeAdapters.BIT_SET, "[1,0,1,1]");
    Assert.assertEquals(bitSet, readBitSet);

    BitSet readBooleanBitSet = fromJson(TypeAdapters.BIT_SET, "[true,false,true,true]");
    Assert.assertEquals(bitSet, readBooleanBitSet);

    BitSet readStringBitSet = fromJson(TypeAdapters.BIT_SET, "[\"1\",\"0\",\"1\",\"1\"]");
    Assert.assertEquals(bitSet, readStringBitSet);

    Assert.assertNull(fromJson(TypeAdapters.BIT_SET, "null"));
    Assert.assertEquals("null", toJson(TypeAdapters.BIT_SET, null));

    Assert.assertNotNull(TypeAdapters.BIT_SET_FACTORY.create(gson, TypeToken.get(BitSet.class)));
    Assert.assertNull(TypeAdapters.BIT_SET_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBitSetInvalidStringValue() throws IOException {
    fromJson(TypeAdapters.BIT_SET, "[\"not_a_number\"]");
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBitSetInvalidTokenType() throws IOException {
    fromJson(TypeAdapters.BIT_SET, "[{}]");
  }

  @Test
  public void testBooleanAdapters() throws IOException {
    Assert.assertEquals("true", toJson(TypeAdapters.BOOLEAN, true));
    Assert.assertEquals("false", toJson(TypeAdapters.BOOLEAN, false));
    Assert.assertEquals("null", toJson(TypeAdapters.BOOLEAN, null));

    Assert.assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN, "true"));
    Assert.assertEquals(Boolean.FALSE, fromJson(TypeAdapters.BOOLEAN, "false"));
    Assert.assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN, "\"true\""));
    Assert.assertNull(fromJson(TypeAdapters.BOOLEAN, "null"));

    Assert.assertEquals("\"true\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, true));
    Assert.assertEquals("\"null\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, null));
    Assert.assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"true\""));
    Assert.assertNull(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "null"));

    Assert.assertNotNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(boolean.class)));
    Assert.assertNotNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(Boolean.class)));
    Assert.assertNull(TypeAdapters.BOOLEAN_FACTORY.create(gson, TypeToken.get(Integer.class)));
  }

  @Test
  public void testByteAdapter() throws IOException {
    Assert.assertEquals("123", toJson(TypeAdapters.BYTE, (byte) 123));
    Assert.assertEquals("null", toJson(TypeAdapters.BYTE, null));
    Assert.assertEquals(Byte.valueOf((byte) 123), fromJson(TypeAdapters.BYTE, "123"));
    Assert.assertNull(fromJson(TypeAdapters.BYTE, "null"));

    Assert.assertNotNull(TypeAdapters.BYTE_FACTORY.create(gson, TypeToken.get(byte.class)));
    Assert.assertNotNull(TypeAdapters.BYTE_FACTORY.create(gson, TypeToken.get(Byte.class)));
    Assert.assertNull(TypeAdapters.BYTE_FACTORY.create(gson, TypeToken.get(Short.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testByteAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.BYTE, "\"invalid\"");
  }

  @Test
  public void testShortAdapter() throws IOException {
    Assert.assertEquals("1234", toJson(TypeAdapters.SHORT, (short) 1234));
    Assert.assertEquals("null", toJson(TypeAdapters.SHORT, null));
    Assert.assertEquals(Short.valueOf((short) 1234), fromJson(TypeAdapters.SHORT, "1234"));
    Assert.assertNull(fromJson(TypeAdapters.SHORT, "null"));

    Assert.assertNotNull(TypeAdapters.SHORT_FACTORY.create(gson, TypeToken.get(short.class)));
    Assert.assertNotNull(TypeAdapters.SHORT_FACTORY.create(gson, TypeToken.get(Short.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testShortAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.SHORT, "\"invalid\"");
  }

  @Test
  public void testIntegerAdapter() throws IOException {
    Assert.assertEquals("12345", toJson(TypeAdapters.INTEGER, 12345));
    Assert.assertEquals("null", toJson(TypeAdapters.INTEGER, null));
    Assert.assertEquals(Integer.valueOf(12345), fromJson(TypeAdapters.INTEGER, "12345"));
    Assert.assertNull(fromJson(TypeAdapters.INTEGER, "null"));

    Assert.assertNotNull(TypeAdapters.INTEGER_FACTORY.create(gson, TypeToken.get(int.class)));
    Assert.assertNotNull(TypeAdapters.INTEGER_FACTORY.create(gson, TypeToken.get(Integer.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testIntegerAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.INTEGER, "\"invalid\"");
  }

  @Test
  public void testLongAdapter() throws IOException {
    Assert.assertEquals("1234567890", toJson(TypeAdapters.LONG, 1234567890L));
    Assert.assertEquals("null", toJson(TypeAdapters.LONG, null));
    Assert.assertEquals(Long.valueOf(1234567890L), fromJson(TypeAdapters.LONG, "1234567890"));
    Assert.assertNull(fromJson(TypeAdapters.LONG, "null"));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testLongAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.LONG, "\"invalid\"");
  }

  @Test
  public void testFloatAdapter() throws IOException {
    Assert.assertEquals("12.34", toJson(TypeAdapters.FLOAT, 12.34f));
    Assert.assertEquals("null", toJson(TypeAdapters.FLOAT, null));
    Assert.assertEquals(Float.valueOf(12.34f), fromJson(TypeAdapters.FLOAT, "12.34"));
    Assert.assertNull(fromJson(TypeAdapters.FLOAT, "null"));
  }

  @Test
  public void testDoubleAdapter() throws IOException {
    Assert.assertEquals("12.3456", toJson(TypeAdapters.DOUBLE, 12.3456d));
    Assert.assertEquals("null", toJson(TypeAdapters.DOUBLE, null));
    Assert.assertEquals(Double.valueOf(12.3456d), fromJson(TypeAdapters.DOUBLE, "12.3456"));
    Assert.assertNull(fromJson(TypeAdapters.DOUBLE, "null"));
  }

  @Test
  public void testNumberAdapter() throws IOException {
    Assert.assertEquals("123", toJson(TypeAdapters.NUMBER, 123));
    Assert.assertEquals("null", toJson(TypeAdapters.NUMBER, null));
    Number number = fromJson(TypeAdapters.NUMBER, "1234.56");
    Assert.assertEquals(1234.56d, number.doubleValue(), 0.0001);
    Assert.assertNull(fromJson(TypeAdapters.NUMBER, "null"));

    Assert.assertNotNull(TypeAdapters.NUMBER_FACTORY.create(gson, TypeToken.get(Number.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testNumberAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.NUMBER, "\"not_a_number\"");
  }

  @Test
  public void testCharacterAdapter() throws IOException {
    Assert.assertEquals("\"a\"", toJson(TypeAdapters.CHARACTER, 'a'));
    Assert.assertEquals("null", toJson(TypeAdapters.CHARACTER, null));
    Assert.assertEquals(Character.valueOf('z'), fromJson(TypeAdapters.CHARACTER, "\"z\""));
    Assert.assertNull(fromJson(TypeAdapters.CHARACTER, "null"));

    Assert.assertNotNull(TypeAdapters.CHARACTER_FACTORY.create(gson, TypeToken.get(char.class)));
    Assert.assertNotNull(TypeAdapters.CHARACTER_FACTORY.create(gson, TypeToken.get(Character.class)));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testCharacterAdapterMultiChar() throws IOException {
    fromJson(TypeAdapters.CHARACTER, "\"abc\"");
  }

  @Test
  public void testStringAdapter() throws IOException {
    Assert.assertEquals("\"hello\"", toJson(TypeAdapters.STRING, "hello"));
    Assert.assertEquals("null", toJson(TypeAdapters.STRING, null));
    Assert.assertEquals("hello", fromJson(TypeAdapters.STRING, "\"hello\""));
    Assert.assertEquals("true", fromJson(TypeAdapters.STRING, "true"));
    Assert.assertNull(fromJson(TypeAdapters.STRING, "null"));

    Assert.assertNotNull(TypeAdapters.STRING_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  @Test
  public void testBigDecimalAdapter() throws IOException {
    BigDecimal bd = new BigDecimal("12345.6789");
    Assert.assertEquals("12345.6789", toJson(TypeAdapters.BIG_DECIMAL, bd));
    Assert.assertEquals("null", toJson(TypeAdapters.BIG_DECIMAL, null));
    Assert.assertEquals(bd, fromJson(TypeAdapters.BIG_DECIMAL, "12345.6789"));
    Assert.assertNull(fromJson(TypeAdapters.BIG_DECIMAL, "null"));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBigDecimalAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.BIG_DECIMAL, "\"invalid_number\"");
  }

  @Test
  public void testBigIntegerAdapter() throws IOException {
    BigInteger bi = new BigInteger("12345678901234567890");
    Assert.assertEquals("12345678901234567890", toJson(TypeAdapters.BIG_INTEGER, bi));
    Assert.assertEquals("null", toJson(TypeAdapters.BIG_INTEGER, null));
    Assert.assertEquals(bi, fromJson(TypeAdapters.BIG_INTEGER, "\"12345678901234567890\""));
    Assert.assertNull(fromJson(TypeAdapters.BIG_INTEGER, "null"));
  }

  @Test(expected = JsonSyntaxException.class)
  public void testBigIntegerAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.BIG_INTEGER, "\"invalid_big_integer\"");
  }

  @Test
  public void testStringBuilderAdapter() throws IOException {
    Assert.assertEquals("\"test\"", toJson(TypeAdapters.STRING_BUILDER, new StringBuilder("test")));
    Assert.assertEquals("null", toJson(TypeAdapters.STRING_BUILDER, null));
    StringBuilder sb = fromJson(TypeAdapters.STRING_BUILDER, "\"test\"");
    Assert.assertEquals("test", sb.toString());
    Assert.assertNull(fromJson(TypeAdapters.STRING_BUILDER, "null"));

    Assert.assertNotNull(TypeAdapters.STRING_BUILDER_FACTORY.create(gson, TypeToken.get(StringBuilder.class)));
  }

  @Test
  public void testStringBufferAdapter() throws IOException {
    Assert.assertEquals("\"test\"", toJson(TypeAdapters.STRING_BUFFER, new StringBuffer("test")));
    Assert.assertEquals("null", toJson(TypeAdapters.STRING_BUFFER, null));
    StringBuffer sb = fromJson(TypeAdapters.STRING_BUFFER, "\"test\"");
    Assert.assertEquals("test", sb.toString());
    Assert.assertNull(fromJson(TypeAdapters.STRING_BUFFER, "null"));

    Assert.assertNotNull(TypeAdapters.STRING_BUFFER_FACTORY.create(gson, TypeToken.get(StringBuffer.class)));
  }

  @Test
  public void testUrlAdapter() throws IOException {
    URL url = new URL("http://google.com");
    Assert.assertEquals("\"http://google.com\"", toJson(TypeAdapters.URL, url));
    Assert.assertEquals("null", toJson(TypeAdapters.URL, null));
    Assert.assertEquals(url, fromJson(TypeAdapters.URL, "\"http://google.com\""));
    Assert.assertNull(fromJson(TypeAdapters.URL, "null"));
    Assert.assertNull(fromJson(TypeAdapters.URL, "\"null\""));

    Assert.assertNotNull(TypeAdapters.URL_FACTORY.create(gson, TypeToken.get(URL.class)));
  }

  @Test
  public void testUriAdapter() throws IOException {
    URI uri = URI.create("http://google.com/path");
    Assert.assertEquals("\"http://google.com/path\"", toJson(TypeAdapters.URI, uri));
    Assert.assertEquals("null", toJson(TypeAdapters.URI, null));
    Assert.assertEquals(uri, fromJson(TypeAdapters.URI, "\"http://google.com/path\""));
    Assert.assertNull(fromJson(TypeAdapters.URI, "null"));
    Assert.assertNull(fromJson(TypeAdapters.URI, "\"null\""));

    Assert.assertNotNull(TypeAdapters.URI_FACTORY.create(gson, TypeToken.get(URI.class)));
  }

  @Test(expected = JsonIOException.class)
  public void testUriAdapterInvalid() throws IOException {
    fromJson(TypeAdapters.URI, "\"some : invalid : uri\"");
  }

  @Test
  public void testInetAddressAdapter() throws IOException {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    Assert.assertEquals("\"127.0.0.1\"", toJson(TypeAdapters.INET_ADDRESS, address));
    Assert.assertEquals("null", toJson(TypeAdapters.INET_ADDRESS, null));
    InetAddress parsed = fromJson(TypeAdapters.INET_ADDRESS, "\"127.0.0.1\"");
    Assert.assertEquals(address, parsed);
    Assert.assertNull(fromJson(TypeAdapters.INET_ADDRESS, "null"));

    Assert.assertNotNull(TypeAdapters.INET_ADDRESS_FACTORY.create(gson, TypeToken.get(InetAddress.class)));
  }

  @Test
  public void testUuidAdapter() throws IOException {
    UUID uuid = UUID.randomUUID();
    Assert.assertEquals("\"" + uuid.toString() + "\"", toJson(TypeAdapters.UUID, uuid));
    Assert.assertEquals("null", toJson(TypeAdapters.UUID, null));
    Assert.assertEquals(uuid, fromJson(TypeAdapters.UUID, "\"" + uuid.toString() + "\""));
    Assert.assertNull(fromJson(TypeAdapters.UUID, "null"));

    Assert.assertNotNull(TypeAdapters.UUID_FACTORY.create(gson, TypeToken.get(UUID.class)));
  }

  @Test
  public void testTimestampFactory() throws IOException {
    TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(Timestamp.class));
    Assert.assertNotNull(adapter);
    Assert.assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class)));

    long now = 1600000000000L;
    Timestamp ts = new Timestamp(now);
    String json = toJson(adapter, ts);
    Timestamp read = fromJson(adapter, json);
    Assert.assertNotNull(read);
    Assert.assertEquals(ts.getTime(), read.getTime());

    Assert.assertEquals("null", toJson(adapter, null));
    Assert.assertNull(fromJson(adapter, "null"));
  }

  @Test
  public void testCalendarAdapter() throws IOException {
    Calendar cal = new GregorianCalendar(2023, 5, 15, 10, 20, 30);
    String json = toJson(TypeAdapters.CALENDAR, cal);
    Assert.assertTrue(json.contains("\"year\":2023"));
    Assert.assertTrue(json.contains("\"month\":5"));
    Assert.assertTrue(json.contains("\"dayOfMonth\":15"));
    Assert.assertTrue(json.contains("\"hourOfDay\":10"));
    Assert.assertTrue(json.contains("\"minute\":20"));
    Assert.assertTrue(json.contains("\"second\":30"));

    Calendar parsed = fromJson(TypeAdapters.CALENDAR,
        "{\"year\":2023,\"month\":5,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":20,\"second\":30}");
    Assert.assertEquals(2023, parsed.get(Calendar.YEAR));
    Assert.assertEquals(5, parsed.get(Calendar.MONTH));
    Assert.assertEquals(15, parsed.get(Calendar.DAY_OF_MONTH));
    Assert.assertEquals(10, parsed.get(Calendar.HOUR_OF_DAY));
    Assert.assertEquals(20, parsed.get(Calendar.MINUTE));
    Assert.assertEquals(30, parsed.get(Calendar.SECOND));

    Assert.assertEquals("null", toJson(TypeAdapters.CALENDAR, null));
    Assert.assertNull(fromJson(TypeAdapters.CALENDAR, "null"));

    Assert.assertNotNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(Calendar.class)));
    Assert.assertNotNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(GregorianCalendar.class)));
    Assert.assertNull(TypeAdapters.CALENDAR_FACTORY.create(gson, TypeToken.get(Date.class)));
  }

  @Test
  public void testLocaleAdapter() throws IOException {
    Assert.assertEquals("\"en\"", toJson(TypeAdapters.LOCALE, new Locale("en")));
    Assert.assertEquals("\"en_US\"", toJson(TypeAdapters.LOCALE, new Locale("en", "US")));
    Assert.assertEquals("\"en_US_WIN\"", toJson(TypeAdapters.LOCALE, new Locale("en", "US", "WIN")));
    Assert.assertEquals("null", toJson(TypeAdapters.LOCALE, null));

    Assert.assertEquals(new Locale("en"), fromJson(TypeAdapters.LOCALE, "\"en\""));
    Assert.assertEquals(new Locale("en", "US"), fromJson(TypeAdapters.LOCALE, "\"en_US\""));
    Assert.assertEquals(new Locale("en", "US", "WIN"), fromJson(TypeAdapters.LOCALE, "\"en_US_WIN\""));
    Assert.assertNull(fromJson(TypeAdapters.LOCALE, "null"));

    Assert.assertNotNull(TypeAdapters.LOCALE_FACTORY.create(gson, TypeToken.get(Locale.class)));
  }

  @Test
  public void testJsonElementAdapter() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("str", "value");
    obj.addProperty("num", 42);
    obj.addProperty("bool", true);
    obj.add("nullVal", JsonNull.INSTANCE);
    JsonArray arr = new JsonArray();
    arr.add(new JsonPrimitive(1));
    arr.add(new JsonPrimitive(2));
    obj.add("arr", arr);

    String json = toJson(TypeAdapters.JSON_ELEMENT, obj);
    JsonElement parsed = fromJson(TypeAdapters.JSON_ELEMENT, json);
    Assert.assertEquals(obj, parsed);

    Assert.assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, null));
    Assert.assertEquals(JsonNull.INSTANCE, fromJson(TypeAdapters.JSON_ELEMENT, "null"));

    JsonPrimitive numPrim = (JsonPrimitive) fromJson(TypeAdapters.JSON_ELEMENT, "123.45");
    Assert.assertTrue(numPrim.isNumber());
    Assert.assertEquals(123.45, numPrim.getAsDouble(), 0.001);

    JsonPrimitive boolPrim = (JsonPrimitive) fromJson(TypeAdapters.JSON_ELEMENT, "true");
    Assert.assertTrue(boolPrim.getAsBoolean());

    Assert.assertNotNull(TypeAdapters.JSON_ELEMENT_FACTORY.create(gson, TypeToken.get(JsonElement.class)));
    Assert.assertNotNull(TypeAdapters.JSON_ELEMENT_FACTORY.create(gson, TypeToken.get(JsonObject.class)));
    Assert.assertNull(TypeAdapters.JSON_ELEMENT_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJsonElementAdapterInvalidClass() throws IOException {
    JsonElement customElement = new JsonElement() {
      @Override
      public JsonElement deepCopy() {
        return this;
      }
    };
    toJson(TypeAdapters.JSON_ELEMENT, customElement);
  }

  @Test
  public void testEnumAdapter() throws IOException {
    TypeAdapter<SampleEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(SampleEnum.class));
    Assert.assertNotNull(adapter);

    Assert.assertEquals("\"FIRST\"", toJson(adapter, SampleEnum.FIRST));
    Assert.assertEquals("\"SECOND\"", toJson(adapter, SampleEnum.SECOND));
    Assert.assertEquals("\"THIRD\"", toJson(adapter, SampleEnum.THIRD));
    Assert.assertEquals("null", toJson(adapter, null));

    Assert.assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"FIRST\""));
    Assert.assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"first\""));
    Assert.assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"1st\""));
    Assert.assertEquals(SampleEnum.SECOND, fromJson(adapter, "\"SECOND\""));
    Assert.assertEquals(SampleEnum.THIRD, fromJson(adapter, "\"THIRD\""));
    Assert.assertNull(fromJson(adapter, "null"));

    TypeAdapter<SampleEnum> anonAdapter = TypeAdapters.ENUM_FACTORY.create(gson,
        (TypeToken<SampleEnum>) TypeToken.get(SampleEnum.THIRD.getClass()));
    Assert.assertNotNull(anonAdapter);
    Assert.assertEquals(SampleEnum.THIRD, fromJson(anonAdapter, "\"THIRD\""));

    Assert.assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(Enum.class)));
    Assert.assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  @Test
  public void testFactoryHelpers() {
    TypeToken<String> token = TypeToken.get(String.class);
    TypeAdapterFactory f1 = TypeAdapters.newFactory(token, TypeAdapters.STRING);
    Assert.assertNotNull(f1.create(gson, token));
    Assert.assertNull(f1.create(gson, TypeToken.get(Integer.class)));

    TypeAdapterFactory f2 = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
    Assert.assertNotNull(f2.create(gson, token));
    Assert.assertNull(f2.create(gson, TypeToken.get(Integer.class)));
    Assert.assertTrue(f2.toString().contains("Factory[type=java.lang.String"));

    TypeAdapterFactory f3 = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
    Assert.assertNotNull(f3.create(gson, TypeToken.get(int.class)));
    Assert.assertNotNull(f3.create(gson, TypeToken.get(Integer.class)));
    Assert.assertNull(f3.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(f3.toString().contains("Factory[type=java.lang.Integer+int"));

    TypeAdapterFactory f4 = TypeAdapters.newFactoryForMultipleTypes(Number.class, Integer.class, TypeAdapters.INTEGER);
    Assert.assertNotNull(f4.create(gson, TypeToken.get(Number.class)));
    Assert.assertNotNull(f4.create(gson, TypeToken.get(Integer.class)));
    Assert.assertNull(f4.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(f4.toString().contains("Factory[type=java.lang.Number+java.lang.Integer"));

    TypeAdapterFactory f5 = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
    Assert.assertNotNull(f5.create(gson, TypeToken.get(Number.class)));
    Assert.assertNotNull(f5.create(gson, TypeToken.get(Integer.class)));
    Assert.assertNotNull(f5.create(gson, TypeToken.get(Double.class)));
    Assert.assertNull(f5.create(gson, TypeToken.get(String.class)));
    Assert.assertTrue(f5.toString().contains("Factory[typeHierarchy=java.lang.Number"));
  }
}
