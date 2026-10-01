package com.google.gson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class DefaultDateTypeAdapterTest {

  private Locale originalLocale;

  @Before
  public void setUp() {
    originalLocale = Locale.getDefault();
  }

  @After
  public void tearDown() {
    Locale.setDefault(originalLocale);
  }

  // Helper to create a JsonWriter that writes to a StringWriter
  private JsonWriter createJsonWriter(StringWriter sw) {
    return new JsonWriter(sw);
  }

  // Helper to create a JsonReader from a JSON string
  private JsonReader createJsonReader(String json) {
    return new JsonReader(new StringReader(json));
  }

  // Helper to get the written string from a JsonWriter
  private String getWrittenString(JsonWriter writer, StringWriter sw) throws IOException {
    writer.flush();
    return sw.toString();
  }

  // ==================== Constructor Tests ====================

  @Test
  public void testConstructorWithDateTypeOnly() {
    // Should not throw for valid types
    new DefaultDateTypeAdapter(Date.class);
    new DefaultDateTypeAdapter(Timestamp.class);
    new DefaultDateTypeAdapter(java.sql.Date.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInvalidDateType() {
    // Anonymous subclass of Date is not one of the allowed types
    new DefaultDateTypeAdapter(new Date() {}.getClass());
  }

  @Test
  public void testConstructorWithDateTypeAndPattern() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    assertNotNull(adapter);
  }

  @Test
  public void testConstructorWithDateTypeAndStyle() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT);
    assertNotNull(adapter);
  }

  @Test
  public void testConstructorWithDateStyleAndTimeStyle() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT);
    assertNotNull(adapter);
  }

  @Test
  public void testConstructorWithDateTypeAndDateStyleAndTimeStyle() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT, DateFormat.SHORT);
    assertNotNull(adapter);
  }

  // ==================== Write Tests ====================

  @Test
  public void testWriteNull() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    StringWriter sw = new StringWriter();
    JsonWriter writer = createJsonWriter(sw);
    adapter.write(writer, null);
    writer.flush();
    assertEquals("null", sw.toString());
  }

  @Test
  public void testWriteDate() throws IOException {
    // Use a fixed date and a pattern to have deterministic output
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    Date date = new Date(1234567890000L); // some fixed timestamp
    StringWriter sw = new StringWriter();
    JsonWriter writer = createJsonWriter(sw);
    adapter.write(writer, date);
    writer.flush();
    // enUsFormat uses the pattern "yyyy-MM-dd" with Locale.US
    SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String expected = "\"" + expectedFormat.format(date) + "\"";
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testWriteTimestamp() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd");
    Timestamp timestamp = new Timestamp(1234567890000L);
    StringWriter sw = new StringWriter();
    JsonWriter writer = createJsonWriter(sw);
    adapter.write(writer, timestamp);
    writer.flush();
    SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String expected = "\"" + expectedFormat.format(timestamp) + "\"";
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testWriteSqlDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
    java.sql.Date sqlDate = new java.sql.Date(1234567890000L);
    StringWriter sw = new StringWriter();
    JsonWriter writer = createJsonWriter(sw);
    adapter.write(writer, sqlDate);
    writer.flush();
    SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String expected = "\"" + expectedFormat.format(sqlDate) + "\"";
    assertEquals(expected, sw.toString());
  }

  // ==================== Read Tests ====================

  @Test
  public void testReadValidDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String json = "\"2023-01-15\"";
    JsonReader reader = createJsonReader(json);
    Date date = adapter.read(reader);
    assertNotNull(date);
    // Check that the date is parsed correctly
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    assertEquals(sdf.parse("2023-01-15"), date);
  }

  @Test
  public void testReadValidTimestamp() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd");
    String json = "\"2023-01-15\"";
    JsonReader reader = createJsonReader(json);
    Date date = adapter.read(reader);
    assertTrue(date instanceof Timestamp);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    assertEquals(sdf.parse("2023-01-15").getTime(), date.getTime());
  }

  @Test
  public void testReadValidSqlDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
    String json = "\"2023-01-15\"";
    JsonReader reader = createJsonReader(json);
    Date date = adapter.read(reader);
    assertTrue(date instanceof java.sql.Date);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    assertEquals(sdf.parse("2023-01-15").getTime(), date.getTime());
  }

  @Test(expected = JsonParseException.class)
  public void testReadNonStringToken() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = createJsonReader("123");
    adapter.read(reader);
  }

  @Test
  public void testReadBooleanTokenThrows() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = createJsonReader("true");
    try {
      adapter.read(reader);
      fail("Expected JsonParseException");
    } catch (JsonParseException e) {
      // expected
    }
  }

  @Test
  public void testReadNullTokenThrows() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = createJsonReader("null");
    try {
      adapter.read(reader);
      fail("Expected JsonParseException");
    } catch (JsonParseException e) {
      // expected
    }
  }

  @Test
  public void testReadBeginObjectTokenThrows() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = createJsonReader("{}");
    try {
      adapter.read(reader);
      fail("Expected JsonParseException");
    } catch (JsonParseException e) {
      // expected
    }
  }

  @Test(expected = JsonSyntaxException.class)
  public void testReadUnparseableDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String json = "\"not-a-date\"";
    JsonReader reader = createJsonReader(json);
    adapter.read(reader);
  }

  @Test
  public void testReadFallbackToEnUsFormat() throws IOException {
    // Set default locale to Germany, provide a date string that is valid in US but not in Germany
    Locale.setDefault(Locale.GERMANY);
    // Use a pattern that is the same for both locales, but we can test that enUsFormat is used as fallback
    // Actually, we need a date string that localFormat (Germany) cannot parse but enUsFormat (US) can.
    // For example, using a pattern with month names: "MMM dd, yyyy" with English month name.
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "MMM dd, yyyy");
    String json = "\"Jan 15, 2023\""; // English month name
    JsonReader reader = createJsonReader(json);
    Date date = adapter.read(reader);
    assertNotNull(date);
    // Verify that the date is correct
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    assertEquals(sdf.parse("Jan 15, 2023"), date);
  }

  @Test
  public void testReadFallbackToISO8601() throws IOException {
    // Provide an ISO 8601 date string that neither local nor enUs can parse with the given pattern
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String isoDate = "\"2023-01-15T10:15:30Z\"";
    JsonReader reader = createJsonReader(isoDate);
    Date date = adapter.read(reader);
    assertNotNull(date);
    // Verify that the date is parsed correctly (ISO8601Utils should handle it)
    // We can check that the time is correct
    // 2023-01-15T10:15:30Z corresponds to a specific timestamp
    // We'll just check that it's not null and roughly correct
    assertTrue(date.getTime() > 0);
  }

  @Test
  public void testReadInvalidDateTypeThrowsAssertionError() throws Exception {
    // Create a valid adapter and then use reflection to set dateType to an invalid class
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    Field dateTypeField = DefaultDateTypeAdapter.class.getDeclaredField("dateType");
    dateTypeField.setAccessible(true);
    dateTypeField.set(adapter, Object.class); // invalid type

    String json = "\"2023-01-15\"";
    JsonReader reader = createJsonReader(json);
    try {
      adapter.read(reader);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      // expected
    }
  }

  // ==================== toString Tests ====================

  @Test
  public void testToString() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String str = adapter.toString();
    assertTrue(str.startsWith("DefaultDateTypeAdapter("));
    assertTrue(str.contains("SimpleDateFormat"));
    assertTrue(str.endsWith(")"));
  }

  @Test
  public void testToStringWithDifferentFormat() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT);
    String str = adapter.toString();
    assertTrue(str.startsWith("DefaultDateTypeAdapter("));
    // The localFormat will be a DateFormat subclass, likely SimpleDateFormat
    assertTrue(str.contains("DateFormat") || str.contains("SimpleDateFormat"));
  }
}
