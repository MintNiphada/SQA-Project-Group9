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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

    // Helper methods to create JsonReader and JsonWriter
    private JsonReader reader(String json) {
        return new JsonReader(new StringReader(json));
    }

    private String write(Object value, TypeAdapter adapter) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        adapter.write(jsonWriter, value);
        jsonWriter.flush();
        return stringWriter.toString();
    }

    // CLASS adapter tests
    @Test
    public void testClassWriteNull() throws IOException {
        String result = write(null, TypeAdapters.CLASS);
        Assert.assertEquals("null", result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClassWriteNonNull() throws IOException {
        TypeAdapters.CLASS.write(new JsonWriter(new StringWriter()), String.class);
    }

    @Test
    public void testClassReadNull() throws IOException {
        JsonReader reader = reader("null");
        Assert.assertNull(TypeAdapters.CLASS.read(reader));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClassReadNonNull() throws IOException {
        TypeAdapters.CLASS.read(reader("\"java.lang.String\""));
    }

    @Test
    public void testClassFactory() {
        TypeAdapterFactory factory = TypeAdapters.CLASS_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Class.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // BIT_SET adapter tests
    @Test
    public void testBitSetReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BIT_SET.read(reader("null")));
    }

    @Test
    public void testBitSetReadEmptyArray() throws IOException {
        BitSet bitset = TypeAdapters.BIT_SET.read(reader("[]"));
        Assert.assertTrue(bitset.isEmpty());
    }

    @Test
    public void testBitSetReadNumbers() throws IOException {
        BitSet bitset = TypeAdapters.BIT_SET.read(reader("[1,0,1]"));
        Assert.assertTrue(bitset.get(0));
        Assert.assertFalse(bitset.get(1));
        Assert.assertTrue(bitset.get(2));
    }

    @Test
    public void testBitSetReadBooleans() throws IOException {
        BitSet bitset = TypeAdapters.BIT_SET.read(reader("[true,false,true]"));
        Assert.assertTrue(bitset.get(0));
        Assert.assertFalse(bitset.get(1));
        Assert.assertTrue(bitset.get(2));
    }

    @Test
    public void testBitSetReadStrings() throws IOException {
        BitSet bitset = TypeAdapters.BIT_SET.read(reader("[\"1\",\"0\",\"1\"]"));
        Assert.assertTrue(bitset.get(0));
        Assert.assertFalse(bitset.get(1));
        Assert.assertTrue(bitset.get(2));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testBitSetReadInvalidString() throws IOException {
        TypeAdapters.BIT_SET.read(reader("[\"abc\"]"));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testBitSetReadInvalidType() throws IOException {
        TypeAdapters.BIT_SET.read(reader("[{}]"));
    }

    @Test
    public void testBitSetWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.BIT_SET));
    }

    @Test
    public void testBitSetWrite() throws IOException {
        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(2);
        Assert.assertEquals("[1,0,1]", write(bitset, TypeAdapters.BIT_SET));
    }

    @Test
    public void testBitSetFactory() {
        TypeAdapterFactory factory = TypeAdapters.BIT_SET_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(BitSet.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // BOOLEAN adapter tests
    @Test
    public void testBooleanReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BOOLEAN.read(reader("null")));
    }

    @Test
    public void testBooleanReadStringTrue() throws IOException {
        Assert.assertTrue(TypeAdapters.BOOLEAN.read(reader("\"true\"")));
    }

    @Test
    public void testBooleanReadStringFalse() throws IOException {
        Assert.assertFalse(TypeAdapters.BOOLEAN.read(reader("\"false\"")));
    }

    @Test
    public void testBooleanReadBoolean() throws IOException {
        Assert.assertTrue(TypeAdapters.BOOLEAN.read(reader("true")));
        Assert.assertFalse(TypeAdapters.BOOLEAN.read(reader("false")));
    }

    @Test
    public void testBooleanWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.BOOLEAN));
    }

    @Test
    public void testBooleanWrite() throws IOException {
        Assert.assertEquals("true", write(true, TypeAdapters.BOOLEAN));
        Assert.assertEquals("false", write(false, TypeAdapters.BOOLEAN));
    }

    @Test
    public void testBooleanAsStringReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BOOLEAN_AS_STRING.read(reader("null")));
    }

    @Test
    public void testBooleanAsStringRead() throws IOException {
        Assert.assertTrue(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"true\"")));
        Assert.assertFalse(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"false\"")));
    }

    @Test
    public void testBooleanAsStringWriteNull() throws IOException {
        Assert.assertEquals("\"null\"", write(null, TypeAdapters.BOOLEAN_AS_STRING));
    }

    @Test
    public void testBooleanAsStringWrite() throws IOException {
        Assert.assertEquals("\"true\"", write(true, TypeAdapters.BOOLEAN_AS_STRING));
        Assert.assertEquals("\"false\"", write(false, TypeAdapters.BOOLEAN_AS_STRING));
    }

    @Test
    public void testBooleanFactory() {
        TypeAdapterFactory factory = TypeAdapters.BOOLEAN_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Boolean.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(boolean.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // BYTE adapter tests
    @Test
    public void testByteReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BYTE.read(reader("null")));
    }

    @Test
    public void testByteRead() throws IOException {
        Assert.assertEquals((byte) 42, TypeAdapters.BYTE.read(reader("42")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testByteReadInvalid() throws IOException {
        TypeAdapters.BYTE.read(reader("\"abc\""));
    }

    @Test
    public void testByteWrite() throws IOException {
        Assert.assertEquals("42", write((byte) 42, TypeAdapters.BYTE));
    }

    @Test
    public void testByteFactory() {
        TypeAdapterFactory factory = TypeAdapters.BYTE_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Byte.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(byte.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // SHORT adapter tests
    @Test
    public void testShortReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.SHORT.read(reader("null")));
    }

    @Test
    public void testShortRead() throws IOException {
        Assert.assertEquals((short) 42, TypeAdapters.SHORT.read(reader("42")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testShortReadInvalid() throws IOException {
        TypeAdapters.SHORT.read(reader("\"abc\""));
    }

    @Test
    public void testShortWrite() throws IOException {
        Assert.assertEquals("42", write((short) 42, TypeAdapters.SHORT));
    }

    @Test
    public void testShortFactory() {
        TypeAdapterFactory factory = TypeAdapters.SHORT_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Short.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(short.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // INTEGER adapter tests
    @Test
    public void testIntegerReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.INTEGER.read(reader("null")));
    }

    @Test
    public void testIntegerRead() throws IOException {
        Assert.assertEquals(42, TypeAdapters.INTEGER.read(reader("42")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testIntegerReadInvalid() throws IOException {
        TypeAdapters.INTEGER.read(reader("\"abc\""));
    }

    @Test
    public void testIntegerWrite() throws IOException {
        Assert.assertEquals("42", write(42, TypeAdapters.INTEGER));
    }

    @Test
    public void testIntegerFactory() {
        TypeAdapterFactory factory = TypeAdapters.INTEGER_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(int.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // LONG adapter tests
    @Test
    public void testLongReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.LONG.read(reader("null")));
    }

    @Test
    public void testLongRead() throws IOException {
        Assert.assertEquals(42L, TypeAdapters.LONG.read(reader("42")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testLongReadInvalid() throws IOException {
        TypeAdapters.LONG.read(reader("\"abc\""));
    }

    @Test
    public void testLongWrite() throws IOException {
        Assert.assertEquals("42", write(42L, TypeAdapters.LONG));
    }

    // FLOAT adapter tests
    @Test
    public void testFloatReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.FLOAT.read(reader("null")));
    }

    @Test
    public void testFloatRead() throws IOException {
        Assert.assertEquals(3.14f, TypeAdapters.FLOAT.read(reader("3.14")));
    }

    @Test
    public void testFloatWrite() throws IOException {
        Assert.assertEquals("3.14", write(3.14f, TypeAdapters.FLOAT));
    }

    // DOUBLE adapter tests
    @Test
    public void testDoubleReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.DOUBLE.read(reader("null")));
    }

    @Test
    public void testDoubleRead() throws IOException {
        Assert.assertEquals(3.14, TypeAdapters.DOUBLE.read(reader("3.14")));
    }

    @Test
    public void testDoubleWrite() throws IOException {
        Assert.assertEquals("3.14", write(3.14, TypeAdapters.DOUBLE));
    }

    // NUMBER adapter tests
    @Test
    public void testNumberReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.NUMBER.read(reader("null")));
    }

    @Test
    public void testNumberRead() throws IOException {
        Assert.assertEquals(42, TypeAdapters.NUMBER.read(reader("42")).intValue());
    }

    @Test(expected = JsonSyntaxException.class)
    public void testNumberReadInvalid() throws IOException {
        TypeAdapters.NUMBER.read(reader("\"abc\""));
    }

    @Test
    public void testNumberWrite() throws IOException {
        Assert.assertEquals("42", write(42, TypeAdapters.NUMBER));
    }

    @Test
    public void testNumberFactory() {
        TypeAdapterFactory factory = TypeAdapters.NUMBER_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Number.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // CHARACTER adapter tests
    @Test
    public void testCharacterReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.CHARACTER.read(reader("null")));
    }

    @Test
    public void testCharacterRead() throws IOException {
        Assert.assertEquals('a', (char) TypeAdapters.CHARACTER.read(reader("\"a\"")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testCharacterReadInvalidLength() throws IOException {
        TypeAdapters.CHARACTER.read(reader("\"ab\""));
    }

    @Test
    public void testCharacterWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.CHARACTER));
    }

    @Test
    public void testCharacterWrite() throws IOException {
        Assert.assertEquals("\"a\"", write('a', TypeAdapters.CHARACTER));
    }

    @Test
    public void testCharacterFactory() {
        TypeAdapterFactory factory = TypeAdapters.CHARACTER_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Character.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(char.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // STRING adapter tests
    @Test
    public void testStringReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.STRING.read(reader("null")));
    }

    @Test
    public void testStringRead() throws IOException {
        Assert.assertEquals("abc", TypeAdapters.STRING.read(reader("\"abc\"")));
    }

    @Test
    public void testStringReadBoolean() throws IOException {
        Assert.assertEquals("true", TypeAdapters.STRING.read(reader("true")));
        Assert.assertEquals("false", TypeAdapters.STRING.read(reader("false")));
    }

    @Test
    public void testStringWrite() throws IOException {
        Assert.assertEquals("\"abc\"", write("abc", TypeAdapters.STRING));
    }

    @Test
    public void testStringFactory() {
        TypeAdapterFactory factory = TypeAdapters.STRING_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    }

    // BIG_DECIMAL adapter tests
    @Test
    public void testBigDecimalReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BIG_DECIMAL.read(reader("null")));
    }

    @Test
    public void testBigDecimalRead() throws IOException {
        Assert.assertEquals(new BigDecimal("3.14"), TypeAdapters.BIG_DECIMAL.read(reader("\"3.14\"")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testBigDecimalReadInvalid() throws IOException {
        TypeAdapters.BIG_DECIMAL.read(reader("\"abc\""));
    }

    @Test
    public void testBigDecimalWrite() throws IOException {
        Assert.assertEquals("3.14", write(new BigDecimal("3.14"), TypeAdapters.BIG_DECIMAL));
    }

    // BIG_INTEGER adapter tests
    @Test
    public void testBigIntegerReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.BIG_INTEGER.read(reader("null")));
    }

    @Test
    public void testBigIntegerRead() throws IOException {
        Assert.assertEquals(new BigInteger("42"), TypeAdapters.BIG_INTEGER.read(reader("\"42\"")));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testBigIntegerReadInvalid() throws IOException {
        TypeAdapters.BIG_INTEGER.read(reader("\"abc\""));
    }

    @Test
    public void testBigIntegerWrite() throws IOException {
        Assert.assertEquals("42", write(new BigInteger("42"), TypeAdapters.BIG_INTEGER));
    }

    // STRING_BUILDER adapter tests
    @Test
    public void testStringBuilderReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.STRING_BUILDER.read(reader("null")));
    }

    @Test
    public void testStringBuilderRead() throws IOException {
        Assert.assertEquals(new StringBuilder("abc"), TypeAdapters.STRING_BUILDER.read(reader("\"abc\"")));
    }

    @Test
    public void testStringBuilderWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.STRING_BUILDER));
    }

    @Test
    public void testStringBuilderWrite() throws IOException {
        Assert.assertEquals("\"abc\"", write(new StringBuilder("abc"), TypeAdapters.STRING_BUILDER));
    }

    @Test
    public void testStringBuilderFactory() {
        TypeAdapterFactory factory = TypeAdapters.STRING_BUILDER_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(StringBuilder.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // STRING_BUFFER adapter tests
    @Test
    public void testStringBufferReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.STRING_BUFFER.read(reader("null")));
    }

    @Test
    public void testStringBufferRead() throws IOException {
        Assert.assertEquals(new StringBuffer("abc"), TypeAdapters.STRING_BUFFER.read(reader("\"abc\"")));
    }

    @Test
    public void testStringBufferWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.STRING_BUFFER));
    }

    @Test
    public void testStringBufferWrite() throws IOException {
        Assert.assertEquals("\"abc\"", write(new StringBuffer("abc"), TypeAdapters.STRING_BUFFER));
    }

    @Test
    public void testStringBufferFactory() {
        TypeAdapterFactory factory = TypeAdapters.STRING_BUFFER_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(StringBuffer.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // URL adapter tests
    @Test
    public void testUrlReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.URL.read(reader("null")));
    }

    @Test
    public void testUrlReadNullString() throws IOException {
        Assert.assertNull(TypeAdapters.URL.read(reader("\"null\"")));
    }

    @Test
    public void testUrlRead() throws IOException {
        Assert.assertEquals(new URL("http://example.com"), TypeAdapters.URL.read(reader("\"http://example.com\"")));
    }

    @Test(expected = IOException.class)
    public void testUrlReadInvalid() throws IOException {
        TypeAdapters.URL.read(reader("\"invalid url\""));
    }

    @Test
    public void testUrlWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.URL));
    }

    @Test
    public void testUrlWrite() throws IOException {
        Assert.assertEquals("\"http://example.com\"", write(new URL("http://example.com"), TypeAdapters.URL));
    }

    @Test
    public void testUrlFactory() {
        TypeAdapterFactory factory = TypeAdapters.URL_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(URL.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // URI adapter tests
    @Test
    public void testUriReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.URI.read(reader("null")));
    }

    @Test
    public void testUriReadNullString() throws IOException {
        Assert.assertNull(TypeAdapters.URI.read(reader("\"null\"")));
    }

    @Test
    public void testUriRead() throws IOException {
        Assert.assertEquals(new URI("http://example.com"), TypeAdapters.URI.read(reader("\"http://example.com\"")));
    }

    @Test(expected = JsonIOException.class)
    public void testUriReadInvalid() throws IOException {
        TypeAdapters.URI.read(reader("\"invalid uri\""));
    }

    @Test
    public void testUriWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.URI));
    }

    @Test
    public void testUriWrite() throws IOException {
        Assert.assertEquals("\"http://example.com\"", write(new URI("http://example.com"), TypeAdapters.URI));
    }

    @Test
    public void testUriFactory() {
        TypeAdapterFactory factory = TypeAdapters.URI_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(URI.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // INET_ADDRESS adapter tests
    @Test
    public void testInetAddressReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.INET_ADDRESS.read(reader("null")));
    }

    @Test
    public void testInetAddressRead() throws IOException {
        Assert.assertEquals(InetAddress.getByName("127.0.0.1"), TypeAdapters.INET_ADDRESS.read(reader("\"127.0.0.1\"")));
    }

    @Test
    public void testInetAddressWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.INET_ADDRESS));
    }

    @Test
    public void testInetAddressWrite() throws IOException {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        Assert.assertEquals("\"127.0.0.1\"", write(address, TypeAdapters.INET_ADDRESS));
    }

    @Test
    public void testInetAddressFactory() {
        TypeAdapterFactory factory = TypeAdapters.INET_ADDRESS_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(InetAddress.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // UUID adapter tests
    @Test
    public void testUuidReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.UUID.read(reader("null")));
    }

    @Test
    public void testUuidRead() throws IOException {
        UUID uuid = UUID.randomUUID();
        Assert.assertEquals(uuid, TypeAdapters.UUID.read(reader("\"" + uuid.toString() + "\"")));
    }

    @Test
    public void testUuidWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.UUID));
    }

    @Test
    public void testUuidWrite() throws IOException {
        UUID uuid = UUID.randomUUID();
        Assert.assertEquals("\"" + uuid.toString() + "\"", write(uuid, TypeAdapters.UUID));
    }

    @Test
    public void testUuidFactory() {
        TypeAdapterFactory factory = TypeAdapters.UUID_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(UUID.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // TIMESTAMP adapter tests
    @Test
    public void testTimestampFactory() {
        TypeAdapterFactory factory = TypeAdapters.TIMESTAMP_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Timestamp.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(Date.class)));
    }

    @Test
    public void testTimestampReadNull() throws IOException {
        TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(new Gson(), TypeToken.get(Timestamp.class));
        Assert.assertNull(adapter.read(reader("null")));
    }

    @Test
    public void testTimestampRead() throws IOException {
        TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(new Gson(), TypeToken.get(Timestamp.class));
        Timestamp timestamp = adapter.read(reader("\"2020-01-01T00:00:00Z\""));
        Assert.assertNotNull(timestamp);
    }

    @Test
    public void testTimestampWriteNull() throws IOException {
        TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(new Gson(), TypeToken.get(Timestamp.class));
        Assert.assertEquals("null", write(null, adapter));
    }

    // CALENDAR adapter tests
    @Test
    public void testCalendarReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.CALENDAR.read(reader("null")));
    }

    @Test
    public void testCalendarRead() throws IOException {
        Calendar calendar = TypeAdapters.CALENDAR.read(reader("{\"year\":2020,\"month\":0,\"dayOfMonth\":1,\"hourOfDay\":0,\"minute\":0,\"second\":0}"));
        Assert.assertEquals(2020, calendar.get(Calendar.YEAR));
        Assert.assertEquals(0, calendar.get(Calendar.MONTH));
        Assert.assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCalendarWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.CALENDAR));
    }

    @Test
    public void testCalendarWrite() throws IOException {
        Calendar calendar = new GregorianCalendar(2020, 0, 1, 0, 0, 0);
        String json = write(calendar, TypeAdapters.CALENDAR);
        Assert.assertTrue(json.contains("\"year\":2020"));
        Assert.assertTrue(json.contains("\"month\":0"));
        Assert.assertTrue(json.contains("\"dayOfMonth\":1"));
    }

    @Test
    public void testCalendarFactory() {
        TypeAdapterFactory factory = TypeAdapters.CALENDAR_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Calendar.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(GregorianCalendar.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // LOCALE adapter tests
    @Test
    public void testLocaleReadNull() throws IOException {
        Assert.assertNull(TypeAdapters.LOCALE.read(reader("null")));
    }

    @Test
    public void testLocaleReadLanguageOnly() throws IOException {
        Assert.assertEquals(new Locale("en"), TypeAdapters.LOCALE.read(reader("\"en\"")));
    }

    @Test
    public void testLocaleReadLanguageCountry() throws IOException {
        Assert.assertEquals(new Locale("en", "US"), TypeAdapters.LOCALE.read(reader("\"en_US\"")));
    }

    @Test
    public void testLocaleReadLanguageCountryVariant() throws IOException {
        Assert.assertEquals(new Locale("en", "US", "WIN"), TypeAdapters.LOCALE.read(reader("\"en_US_WIN\"")));
    }

    @Test
    public void testLocaleWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.LOCALE));
    }

    @Test
    public void testLocaleWrite() throws IOException {
        Assert.assertEquals("\"en_US\"", write(new Locale("en", "US"), TypeAdapters.LOCALE));
    }

    @Test
    public void testLocaleFactory() {
        TypeAdapterFactory factory = TypeAdapters.LOCALE_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Locale.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // JSON_ELEMENT adapter tests
    @Test
    public void testJsonElementReadString() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("\"abc\""));
        Assert.assertTrue(element.isJsonPrimitive());
        Assert.assertEquals("abc", element.getAsString());
    }

    @Test
    public void testJsonElementReadNumber() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("42"));
        Assert.assertTrue(element.isJsonPrimitive());
        Assert.assertEquals(42, element.getAsInt());
    }

    @Test
    public void testJsonElementReadBoolean() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("true"));
        Assert.assertTrue(element.isJsonPrimitive());
        Assert.assertTrue(element.getAsBoolean());
    }

    @Test
    public void testJsonElementReadNull() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("null"));
        Assert.assertTrue(element.isJsonNull());
    }

    @Test
    public void testJsonElementReadArray() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("[1,2,3]"));
        Assert.assertTrue(element.isJsonArray());
        Assert.assertEquals(3, element.getAsJsonArray().size());
    }

    @Test
    public void testJsonElementReadObject() throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader("{\"key\":\"value\"}"));
        Assert.assertTrue(element.isJsonObject());
        Assert.assertEquals("value", element.getAsJsonObject().get("key").getAsString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonElementReadInvalid() throws IOException {
        TypeAdapters.JSON_ELEMENT.read(reader(""));
    }

    @Test
    public void testJsonElementWriteNull() throws IOException {
        Assert.assertEquals("null", write(null, TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWriteJsonNull() throws IOException {
        Assert.assertEquals("null", write(JsonNull.INSTANCE, TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWritePrimitiveString() throws IOException {
        Assert.assertEquals("\"abc\"", write(new JsonPrimitive("abc"), TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWritePrimitiveNumber() throws IOException {
        Assert.assertEquals("42", write(new JsonPrimitive(42), TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWritePrimitiveBoolean() throws IOException {
        Assert.assertEquals("true", write(new JsonPrimitive(true), TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWriteArray() throws IOException {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(1));
        array.add(new JsonPrimitive(2));
        Assert.assertEquals("[1,2]", write(array, TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementWriteObject() throws IOException {
        JsonObject object = new JsonObject();
        object.addProperty("key", "value");
        Assert.assertEquals("{\"key\":\"value\"}", write(object, TypeAdapters.JSON_ELEMENT));
    }

    @Test
    public void testJsonElementFactory() {
        TypeAdapterFactory factory = TypeAdapters.JSON_ELEMENT_FACTORY;
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(JsonElement.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(JsonObject.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    // ENUM adapter tests
    enum TestEnum {
        VALUE1, VALUE2
    }

    @Test
    public void testEnumReadNull() throws IOException {
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        Assert.assertNull(adapter.read(reader("null")));
    }

    @Test
    public void testEnumRead() throws IOException {
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        Assert.assertEquals(TestEnum.VALUE1, adapter.read(reader("\"VALUE1\"")));
    }

    @Test
    public void testEnumWriteNull() throws IOException {
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        Assert.assertEquals("null", write(null, adapter));
    }

    @Test
    public void testEnumWrite() throws IOException {
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        Assert.assertEquals("\"VALUE1\"", write(TestEnum.VALUE1, adapter));
    }

    @Test
    public void testEnumFactoryNonEnum() {
        Assert.assertNull(TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(String.class)));
    }

    // Factory method tests
    @Test
    public void testNewFactoryWithTypeToken() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(TypeToken.get(String.class), TypeAdapters.STRING);
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    }

    @Test
    public void testNewFactoryWithClass() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
        Assert.assertTrue(factory.toString().contains("Factory[type="));
    }

    @Test
    public void testNewFactoryWithUnboxedAndBoxed() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(int.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertTrue(factory.toString().contains("Factory[type="));
    }

    @Test
    public void testNewFactoryForMultipleTypes() {
        TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(Calendar.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(GregorianCalendar.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertTrue(factory.toString().contains("Factory[type="));
    }

    @Test
    public void testNewTypeHierarchyFactory() {
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(JsonElement.class, TypeAdapters.JSON_ELEMENT);
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(JsonElement.class)));
        Assert.assertNotNull(factory.create(new Gson(), TypeToken.get(JsonObject.class)));
        Assert.assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
        Assert.assertTrue(factory.toString().contains("Factory[typeHierarchy="));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<TypeAdapters> constructor = TypeAdapters.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
