package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DefaultDateTypeAdapterTest {

  @Test
  public void testConstructorWithInvalidDateType() {
    try {
      new DefaultDateTypeAdapter(DateSubclass.class);
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      Assert.assertTrue(expected.getMessage().contains("Date type must be one of"));
    }
  }

  @Test
  public void testConstructors() {
    DefaultDateTypeAdapter adapter1 = new DefaultDateTypeAdapter(Date.class);
    Assert.assertNotNull(adapter1);

    DefaultDateTypeAdapter adapter2 = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd");
    Assert.assertNotNull(adapter2);

    DefaultDateTypeAdapter adapter3 = new DefaultDateTypeAdapter(java.sql.Date.class, DateFormat.SHORT);
    Assert.assertNotNull(adapter3);

    DefaultDateTypeAdapter adapter4 = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.LONG);
    Assert.assertNotNull(adapter4);

    DefaultDateTypeAdapter adapter5 = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT, DateFormat.LONG);
    Assert.assertNotNull(adapter5);

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    DefaultDateTypeAdapter adapter6 = new DefaultDateTypeAdapter(Date.class, format, format);
    Assert.assertNotNull(adapter6);
  }

  @Test
  public void testWriteNull() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, null);
    jsonWriter.flush();
    Assert.assertEquals("null", stringWriter.toString());
  }

  @Test
  public void testWriteAndReadDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd HH:mm:ss");
    Date now = new Date(1609459200000L); // 2021-01-01 00:00:00 UTC approximately

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, now);
    jsonWriter.flush();

    String json = stringWriter.toString();
    Assert.assertTrue(json.startsWith("\""));
    Assert.assertTrue(json.endsWith("\""));

    JsonReader jsonReader = new JsonReader(new StringReader(json));
    Date parsedDate = adapter.read(jsonReader);
    Assert.assertEquals(Date.class, parsedDate.getClass());

    // Compare formatted output rather than raw millis due to second-level precision
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    Assert.assertEquals(format.format(now), format.format(parsedDate));
  }

  @Test
  public void testWriteAndReadTimestamp() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd HH:mm:ss");
    Timestamp timestamp = new Timestamp(1609459200000L);

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, timestamp);
    jsonWriter.flush();

    JsonReader jsonReader = new JsonReader(new StringReader(stringWriter.toString()));
    Date parsed = adapter.read(jsonReader);
    Assert.assertTrue(parsed instanceof Timestamp);
    Assert.assertEquals(timestamp.getTime(), parsed.getTime());
  }

  @Test
  public void testWriteAndReadSqlDate() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
    java.sql.Date sqlDate = new java.sql.Date(1609459200000L);

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, sqlDate);
    jsonWriter.flush();

    JsonReader jsonReader = new JsonReader(new StringReader(stringWriter.toString()));
    Date parsed = adapter.read(jsonReader);
    Assert.assertTrue(parsed instanceof java.sql.Date);

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Assert.assertEquals(format.format(sqlDate), format.format(parsed));
  }

  @Test
  public void testReadIso8601Date() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String iso8601 = "\"1970-01-01T00:00:00.000Z\"";
    JsonReader jsonReader = new JsonReader(new StringReader(iso8601));
    Date parsed = adapter.read(jsonReader);
    Assert.assertEquals(0L, parsed.getTime());
  }

  @Test
  public void testReadEnUsFallback() throws IOException {
    DateFormat enUs = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
    DateFormat local = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE);
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, enUs, local);

    Date expected = new Date(1609459200000L);
    String enUsDateString = "\"" + enUs.format(expected) + "\"";

    JsonReader jsonReader = new JsonReader(new StringReader(enUsDateString));
    Date parsed = adapter.read(jsonReader);
    Assert.assertNotNull(parsed);
  }

  @Test
  public void testReadInvalidToken() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader jsonReader = new JsonReader(new StringReader("12345"));
    try {
      adapter.read(jsonReader);
      Assert.fail("Expected JsonParseException");
    } catch (JsonParseException expected) {
      Assert.assertEquals("The date should be a string value", expected.getMessage());
    }
  }

  @Test
  public void testReadInvalidDateString() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    JsonReader jsonReader = new JsonReader(new StringReader("\"not-a-valid-date\""));
    try {
      adapter.read(jsonReader);
      Assert.fail("Expected JsonSyntaxException");
    } catch (JsonSyntaxException expected) {
      Assert.assertTrue(expected.getCause() instanceof java.text.ParseException);
    }
  }

  @Test
  public void testToString() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String str = adapter.toString();
    Assert.assertEquals("DefaultDateTypeAdapter(SimpleDateFormat)", str);
  }

  private static class DateSubclass extends Date {
    private static final long serialVersionUID = 1L;
  }
}
