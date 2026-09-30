package org.joda.time.format;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Locale;
import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.tz.*;

public class DateTimeFormatterTest {

    private DateTimeFormatter formatter;
    private DateTimeFormatter printerOnly;
    private DateTimeFormatter parserOnly;
    private DateTimeFormatter emptyFormatter;

    @Before
    public void setUp() {
        formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        printerOnly = new DateTimeFormatter(new DateTimePrinter() {
            public int estimatePrintedLength() { return 10; }
            public void printTo(StringBuffer buf, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) {
                buf.append("PRINTED");
            }
            public void printTo(Writer out, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) throws IOException {
                out.write("PRINTED");
            }
            public void printTo(StringBuffer buf, ReadablePartial partial, Locale locale) {
                buf.append("PARTIAL");
            }
            public void printTo(Writer out, ReadablePartial partial, Locale locale) throws IOException {
                out.write("PARTIAL");
            }
        }, null);
        parserOnly = new DateTimeFormatter(null, new DateTimeParser() {
            public int estimateParsedLength() { return 10; }
            public int parseInto(DateTimeParserBucket bucket, String text, int position) {
                bucket.saveField(DateTimeFieldType.year(), 2020);
                bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
                bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
                return text.length();
            }
        });
        emptyFormatter = new DateTimeFormatter(null, null);
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    //-----------------------------------------------------------------------
    // Constructor and basic getters
    @Test
    public void testConstructorWithPrinterAndParser() {
        DateTimePrinter printer = new MockDateTimePrinter();
        DateTimeParser parser = new MockDateTimeParser();
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
        assertSame(printer, f.getPrinter());
        assertSame(parser, f.getParser());
        assertNull(f.getLocale());
        assertFalse(f.isOffsetParsed());
        assertNull(f.getChronology());
        assertNull(f.getZone());
        assertNull(f.getPivotYear());
        assertEquals(2000, f.getDefaultYear());
    }

    @Test
    public void testConstructorWithNullPrinterAndParser() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertFalse(f.isPrinter());
        assertFalse(f.isParser());
        assertNull(f.getPrinter());
        assertNull(f.getParser());
    }

    @Test
    public void testIsPrinter() {
        assertTrue(formatter.isPrinter());
        assertTrue(printerOnly.isPrinter());
        assertFalse(parserOnly.isPrinter());
        assertFalse(emptyFormatter.isPrinter());
    }

    @Test
    public void testGetPrinter() {
        assertNotNull(formatter.getPrinter());
        assertNotNull(printerOnly.getPrinter());
        assertNull(parserOnly.getPrinter());
        assertNull(emptyFormatter.getPrinter());
    }

    @Test
    public void testIsParser() {
        assertTrue(formatter.isParser());
        assertFalse(printerOnly.isParser());
        assertTrue(parserOnly.isParser());
        assertFalse(emptyFormatter.isParser());
    }

    @Test
    public void testGetParser() {
        assertNotNull(formatter.getParser());
        assertNull(printerOnly.getParser());
        assertNotNull(parserOnly.getParser());
        assertNull(emptyFormatter.getParser());
    }

    //-----------------------------------------------------------------------
    // withLocale and getLocale
    @Test
    public void testWithLocale() {
        Locale locale = Locale.FRENCH;
        DateTimeFormatter f = formatter.withLocale(locale);
        assertNotSame(formatter, f);
        assertEquals(locale, f.getLocale());
        assertSame(formatter, formatter.withLocale(null));
        assertSame(f, f.withLocale(locale));
        assertSame(f, f.withLocale(Locale.FRENCH));
    }

    @Test
    public void testWithLocaleNull() {
        DateTimeFormatter f = formatter.withLocale(Locale.FRENCH);
        DateTimeFormatter f2 = f.withLocale(null);
        assertNotSame(f, f2);
        assertNull(f2.getLocale());
    }

    @Test
    public void testGetLocale() {
        assertNull(formatter.getLocale());
        DateTimeFormatter f = formatter.withLocale(Locale.US);
        assertEquals(Locale.US, f.getLocale());
    }

    //-----------------------------------------------------------------------
    // withOffsetParsed and isOffsetParsed
    @Test
    public void testWithOffsetParsed() {
        DateTimeFormatter f = formatter.withOffsetParsed();
        assertNotSame(formatter, f);
        assertTrue(f.isOffsetParsed());
        assertSame(f, f.withOffsetParsed());
        assertNull(f.getZone());
    }

    @Test
    public void testIsOffsetParsed() {
        assertFalse(formatter.isOffsetParsed());
        assertTrue(formatter.withOffsetParsed().isOffsetParsed());
    }

    //-----------------------------------------------------------------------
    // withChronology and getChronology/getChronolgy
    @Test
    public void testWithChronology() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeFormatter f = formatter.withChronology(chrono);
        assertNotSame(formatter, f);
        assertEquals(chrono, f.getChronology());
        assertSame(f, f.withChronology(chrono));
        assertSame(formatter, formatter.withChronology(null));
    }

    @Test
    public void testGetChronology() {
        assertNull(formatter.getChronology());
        Chronology chrono = ISOChronology.getInstanceUTC();
        assertEquals(chrono, formatter.withChronology(chrono).getChronology());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetChronolgyDeprecated() {
        assertNull(formatter.getChronolgy());
        Chronology chrono = ISOChronology.getInstanceUTC();
        assertEquals(chrono, formatter.withChronology(chrono).getChronolgy());
    }

    //-----------------------------------------------------------------------
    // withZoneUTC, withZone, getZone
    @Test
    public void testWithZoneUTC() {
        DateTimeFormatter f = formatter.withZoneUTC();
        assertNotSame(formatter, f);
        assertEquals(DateTimeZone.UTC, f.getZone());
        assertFalse(f.isOffsetParsed());
    }

    @Test
    public void testWithZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter f = formatter.withZone(zone);
        assertNotSame(formatter, f);
        assertEquals(zone, f.getZone());
        assertSame(f, f.withZone(zone));
        assertSame(formatter, formatter.withZone(null));
        assertFalse(f.isOffsetParsed());
    }

    @Test
    public void testGetZone() {
        assertNull(formatter.getZone());
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals(zone, formatter.withZone(zone).getZone());
    }

    //-----------------------------------------------------------------------
    // withPivotYear and getPivotYear
    @Test
    public void testWithPivotYearInteger() {
        Integer pivot = Integer.valueOf(1950);
        DateTimeFormatter f = formatter.withPivotYear(pivot);
        assertNotSame(formatter, f);
        assertEquals(pivot, f.getPivotYear());
        assertSame(f, f.withPivotYear(pivot));
        assertSame(f, f.withPivotYear(Integer.valueOf(1950)));
        assertSame(formatter, formatter.withPivotYear((Integer) null));
    }

    @Test
    public void testWithPivotYearInt() {
        DateTimeFormatter f = formatter.withPivotYear(1950);
        assertEquals(Integer.valueOf(1950), f.getPivotYear());
        assertSame(f, f.withPivotYear(1950));
    }

    @Test
    public void testGetPivotYear() {
        assertNull(formatter.getPivotYear());
        assertEquals(Integer.valueOf(2000), formatter.withPivotYear(2000).getPivotYear());
    }

    //-----------------------------------------------------------------------
    // withDefaultYear and getDefaultYear
    @Test
    public void testWithDefaultYear() {
        DateTimeFormatter f = formatter.withDefaultYear(2024);
        assertNotSame(formatter, f);
        assertEquals(2024, f.getDefaultYear());
        assertEquals(2000, formatter.getDefaultYear());
    }

    @Test
    public void testGetDefaultYear() {
        assertEquals(2000, formatter.getDefaultYear());
        assertEquals(1990, formatter.withDefaultYear(1990).getDefaultYear());
    }

    //-----------------------------------------------------------------------
    // printTo(StringBuffer, ReadableInstant)
    @Test
    public void testPrintToStringBufferReadableInstant() {
        StringBuffer buf = new StringBuffer();
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        formatter.printTo(buf, dt);
        assertEquals("2020-06-15 10:30:00", buf.toString());
    }

    @Test
    public void testPrintToStringBufferReadableInstantNull() {
        StringBuffer buf = new StringBuffer();
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
        formatter.printTo(buf, (ReadableInstant) null);
        assertEquals("2020-06-15 10:30:00", buf.toString());
    }

    @Test
    public void testPrintToStringBufferReadableInstantNoPrinter() {
        StringBuffer buf = new StringBuffer();
        try {
            parserOnly.printTo(buf, new DateTime());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Writer, ReadableInstant)
    @Test
    public void testPrintToWriterReadableInstant() throws IOException {
        StringWriter out = new StringWriter();
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        formatter.printTo(out, dt);
        assertEquals("2020-06-15 10:30:00", out.toString());
    }

    @Test
    public void testPrintToWriterReadableInstantNull() throws IOException {
        StringWriter out = new StringWriter();
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
        formatter.printTo(out, (ReadableInstant) null);
        assertEquals("2020-06-15 10:30:00", out.toString());
    }

    @Test
    public void testPrintToWriterReadableInstantNoPrinter() throws IOException {
        StringWriter out = new StringWriter();
        try {
            parserOnly.printTo(out, new DateTime());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Appendable, ReadableInstant)
    @Test
    public void testPrintToAppendableReadableInstant() throws IOException {
        StringBuilder sb = new StringBuilder();
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        formatter.printTo(sb, dt);
        assertEquals("2020-06-15 10:30:00", sb.toString());
    }

    @Test
    public void testPrintToAppendableReadableInstantNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
        formatter.printTo(sb, (ReadableInstant) null);
        assertEquals("2020-06-15 10:30:00", sb.toString());
    }

    @Test
    public void testPrintToAppendableReadableInstantNoPrinter() throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            parserOnly.printTo(sb, new DateTime());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(StringBuffer, long)
    @Test
    public void testPrintToStringBufferLong() {
        StringBuffer buf = new StringBuffer();
        long millis = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis();
        formatter.printTo(buf, millis);
        assertEquals("2020-06-15 10:30:00", buf.toString());
    }

    @Test
    public void testPrintToStringBufferLongNoPrinter() {
        StringBuffer buf = new StringBuffer();
        try {
            parserOnly.printTo(buf, 0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Writer, long)
    @Test
    public void testPrintToWriterLong() throws IOException {
        StringWriter out = new StringWriter();
        long millis = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis();
        formatter.printTo(out, millis);
        assertEquals("2020-06-15 10:30:00", out.toString());
    }

    @Test
    public void testPrintToWriterLongNoPrinter() throws IOException {
        StringWriter out = new StringWriter();
        try {
            parserOnly.printTo(out, 0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Appendable, long)
    @Test
    public void testPrintToAppendableLong() throws IOException {
        StringBuilder sb = new StringBuilder();
        long millis = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis();
        formatter.printTo(sb, millis);
        assertEquals("2020-06-15 10:30:00", sb.toString());
    }

    @Test
    public void testPrintToAppendableLongNoPrinter() throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            parserOnly.printTo(sb, 0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(StringBuffer, ReadablePartial)
    @Test
    public void testPrintToStringBufferReadablePartial() {
        StringBuffer buf = new StringBuffer();
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.printTo(buf, date);
        assertEquals("2020-06-15", buf.toString());
    }

    @Test
    public void testPrintToStringBufferReadablePartialNull() {
        StringBuffer buf = new StringBuffer();
        try {
            formatter.printTo(buf, (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The partial must not be null", e.getMessage());
        }
    }

    @Test
    public void testPrintToStringBufferReadablePartialNoPrinter() {
        StringBuffer buf = new StringBuffer();
        try {
            parserOnly.printTo(buf, new LocalDate());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Writer, ReadablePartial)
    @Test
    public void testPrintToWriterReadablePartial() throws IOException {
        StringWriter out = new StringWriter();
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.printTo(out, date);
        assertEquals("2020-06-15", out.toString());
    }

    @Test
    public void testPrintToWriterReadablePartialNull() throws IOException {
        StringWriter out = new StringWriter();
        try {
            formatter.printTo(out, (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The partial must not be null", e.getMessage());
        }
    }

    @Test
    public void testPrintToWriterReadablePartialNoPrinter() throws IOException {
        StringWriter out = new StringWriter();
        try {
            parserOnly.printTo(out, new LocalDate());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // printTo(Appendable, ReadablePartial)
    @Test
    public void testPrintToAppendableReadablePartial() throws IOException {
        StringBuilder sb = new StringBuilder();
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.printTo(sb, date);
        assertEquals("2020-06-15", sb.toString());
    }

    @Test
    public void testPrintToAppendableReadablePartialNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            formatter.printTo(sb, (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The partial must not be null", e.getMessage());
        }
    }

    @Test
    public void testPrintToAppendableReadablePartialNoPrinter() throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            parserOnly.printTo(sb, new LocalDate());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // print(ReadableInstant)
    @Test
    public void testPrintReadableInstant() {
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        assertEquals("2020-06-15 10:30:00", formatter.print(dt));
    }

    @Test
    public void testPrintReadableInstantNull() {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
        assertEquals("2020-06-15 10:30:00", formatter.print((ReadableInstant) null));
    }

    @Test
    public void testPrintReadableInstantNoPrinter() {
        try {
            parserOnly.print(new DateTime());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // print(long)
    @Test
    public void testPrintLong() {
        long millis = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.UTC).getMillis();
        assertEquals("2020-06-15 10:30:00", formatter.print(millis));
    }

    @Test
    public void testPrintLongNoPrinter() {
        try {
            parserOnly.print(0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // print(ReadablePartial)
    @Test
    public void testPrintReadablePartial() {
        LocalDate date = new LocalDate(2020, 6, 15);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertEquals("2020-06-15", f.print(date));
    }

    @Test
    public void testPrintReadablePartialNull() {
        try {
            formatter.print((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The partial must not be null", e.getMessage());
        }
    }

    @Test
    public void testPrintReadablePartialNoPrinter() {
        try {
            parserOnly.print(new LocalDate());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Printing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // parseInto
    @Test
    public void testParseInto() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int newPos = formatter.parseInto(mdt, "2020-06-15 10:30:00", 0);
        assertEquals(19, newPos);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(0, mdt.getSecondOfMinute());
    }

    @Test
    public void testParseIntoNullInstant() {
        try {
            formatter.parseInto(null, "2020-06-15", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Instant must not be null", e.getMessage());
        }
    }

    @Test
    public void testParseIntoNoParser() {
        MutableDateTime mdt = new MutableDateTime();
        try {
            printerOnly.parseInto(mdt, "2020-06-15", 0);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    @Test
    public void testParseIntoWithOffsetParsed() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").withOffsetParsed();
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int newPos = f.parseInto(mdt, "2020-06-15T10:30:00+02:00", 0);
        assertEquals(25, newPos);
        assertEquals(DateTimeZone.forOffsetHours(2), mdt.getZone());
    }

    @Test
    public void testParseIntoWithZone() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("Europe/Paris"));
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        f.parseInto(mdt, "2020-06-15 10:30:00", 0);
        assertEquals(DateTimeZone.forID("Europe/Paris"), mdt.getZone());
    }

    @Test
    public void testParseIntoWithChronology() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(GregorianChronology.getInstanceUTC());
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        f.parseInto(mdt, "2020-06-15 10:30:00", 0);
        assertEquals(GregorianChronology.getInstanceUTC(), mdt.getChronology());
    }

    //-----------------------------------------------------------------------
    // parseMillis
    @Test
    public void testParseMillis() {
        long millis = formatter.parseMillis("2020-06-15 10:30:00");
        DateTime dt = new DateTime(millis, DateTimeZone.UTC);
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
    }

    @Test
    public void testParseMillisInvalid() {
        try {
            formatter.parseMillis("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseMillisNoParser() {
        try {
            printerOnly.parseMillis("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    @Test
    public void testParseMillisWithChronology() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(GregorianChronology.getInstanceUTC());
        long millis = f.parseMillis("2020-06-15 10:30:00");
        DateTime dt = new DateTime(millis, GregorianChronology.getInstanceUTC());
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    // parseLocalDate
    @Test
    public void testParseLocalDate() {
        LocalDate date = formatter.parseLocalDate("2020-06-15 10:30:00");
        assertEquals(2020, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testParseLocalDateInvalid() {
        try {
            formatter.parseLocalDate("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseLocalDateNoParser() {
        try {
            printerOnly.parseLocalDate("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // parseLocalTime
    @Test
    public void testParseLocalTime() {
        LocalTime time = formatter.parseLocalTime("2020-06-15 10:30:00");
        assertEquals(10, time.getHourOfDay());
        assertEquals(30, time.getMinuteOfHour());
        assertEquals(0, time.getSecondOfMinute());
    }

    @Test
    public void testParseLocalTimeInvalid() {
        try {
            formatter.parseLocalTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseLocalTimeNoParser() {
        try {
            printerOnly.parseLocalTime("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    // parseLocalDateTime
    @Test
    public void testParseLocalDateTime() {
        LocalDateTime ldt = formatter.parseLocalDateTime("2020-06-15 10:30:00");
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(0, ldt.getSecondOfMinute());
    }

    @Test
    public void testParseLocalDateTimeInvalid() {
        try {
            formatter.parseLocalDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseLocalDateTimeNoParser() {
        try {
            printerOnly.parseLocalDateTime("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    @Test
    public void testParseLocalDateTimeWithOffset() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        LocalDateTime ldt = f.parseLocalDateTime("2020-06-15T10:30:00+02:00");
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
    }

    //-----------------------------------------------------------------------
    // parseDateTime
    @Test
    public void testParseDateTime() {
        DateTime dt = formatter.parseDateTime("2020-06-15 10:30:00");
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
    }

    @Test
    public void testParseDateTimeInvalid() {
        try {
            formatter.parseDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseDateTimeNoParser() {
        try {
            printerOnly.parseDateTime("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    @Test
    public void testParseDateTimeWithOffsetParsed() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").withOffsetParsed();
        DateTime dt = f.parseDateTime("2020-06-15T10:30:00+02:00");
        assertEquals(DateTimeZone.forOffsetHours(2), dt.getZone());
    }

    @Test
    public void testParseDateTimeWithZone() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("Europe/Paris"));
        DateTime dt = f.parseDateTime("2020-06-15 10:30:00");
        assertEquals(DateTimeZone.forID("Europe/Paris"), dt.getZone());
    }

    @Test
    public void testParseDateTimeWithChronology() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(GregorianChronology.getInstanceUTC());
        DateTime dt = f.parseDateTime("2020-06-15 10:30:00");
        assertEquals(GregorianChronology.getInstanceUTC(), dt.getChronology());
    }

    //-----------------------------------------------------------------------
    // parseMutableDateTime
    @Test
    public void testParseMutableDateTime() {
        MutableDateTime mdt = formatter.parseMutableDateTime("2020-06-15 10:30:00");
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(0, mdt.getSecondOfMinute());
    }

    @Test
    public void testParseMutableDateTimeInvalid() {
        try {
            formatter.parseMutableDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseMutableDateTimeNoParser() {
        try {
            printerOnly.parseMutableDateTime("2020-06-15");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("Parsing not supported", e.getMessage());
        }
    }

    @Test
    public void testParseMutableDateTimeWithOffsetParsed() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").withOffsetParsed();
        MutableDateTime mdt = f.parseMutableDateTime("2020-06-15T10:30:00+02:00");
        assertEquals(DateTimeZone.forOffsetHours(2), mdt.getZone());
    }

    @Test
    public void testParseMutableDateTimeWithZone() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("Europe/Paris"));
        MutableDateTime mdt = f.parseMutableDateTime("2020-06-15 10:30:00");
        assertEquals(DateTimeZone.forID("Europe/Paris"), mdt.getZone());
    }

    @Test
    public void testParseMutableDateTimeWithChronology() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(GregorianChronology.getInstanceUTC());
        MutableDateTime mdt = f.parseMutableDateTime("2020-06-15 10:30:00");
        assertEquals(GregorianChronology.getInstanceUTC(), mdt.getChronology());
    }

    //-----------------------------------------------------------------------
    // selectChronology via printing with overrides
    @Test
    public void testPrintWithChronologyOverride() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(GregorianChronology.getInstanceUTC());
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, ISOChronology.getInstanceUTC());
        String result = f.print(dt);
        assertEquals("2020-06-15 10:30:00", result);
    }

    @Test
    public void testPrintWithZoneOverride() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC);
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.forID("Europe/Paris"));
        String result = f.print(dt);
        assertEquals("2020-06-15 08:30:00", result);
    }

    @Test
    public void testPrintWithChronologyAndZoneOverride() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            .withChronology(GregorianChronology.getInstanceUTC())
            .withZone(DateTimeZone.UTC);
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, DateTimeZone.forID("Europe/Paris"));
        String result = f.print(dt);
        assertEquals("2020-06-15 08:30:00", result);
    }

    //-----------------------------------------------------------------------
    // Edge cases for printTo with overflow
    @Test
    public void testPrintToOverflow() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, Long.MAX_VALUE);
        assertNotNull(buf.toString());
    }

    @Test
    public void testPrintToWriterOverflow() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        StringWriter out = new StringWriter();
        f.printTo(out, Long.MAX_VALUE);
        assertNotNull(out.toString());
    }

    //-----------------------------------------------------------------------
    // Mock classes
    private static class MockDateTimePrinter implements DateTimePrinter {
        public int estimatePrintedLength() { return 10; }
        public void printTo(StringBuffer buf, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) {
            buf.append("MOCK");
        }
        public void printTo(Writer out, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) throws IOException {
            out.write("MOCK");
        }
        public void printTo(StringBuffer buf, ReadablePartial partial, Locale locale) {
            buf.append("MOCK");
        }
        public void printTo(Writer out, ReadablePartial partial, Locale locale) throws IOException {
            out.write("MOCK");
        }
    }

    private static class MockDateTimeParser implements DateTimeParser {
        public int estimateParsedLength() { return 10; }
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            return position;
        }
    }
}
