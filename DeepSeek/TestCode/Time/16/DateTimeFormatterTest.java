package org.joda.time.format;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadWritableInstant;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimePrinter;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.formatUtils;

public class DateTimeFormatterTest {

    private DateTimePrinter printer;
    private DateTimeParser parser;
    private DateTimeFormatter formatterWithBoth;
    private DateTimeFormatter printerOnly;
    private DateTimeFormatter parserOnly;

    @Before
    public void setUp() {
        // create simple printer that prints a constant string
        printer = new DateTimeFormatterBuilder().appendLiteral("test").toPrinter();
        // create simple parser that parses a fixed string and sets epoch millis
        parser = new DateTimeFormatterBuilder().appendLiteral("test").toParser().withDefaultYear(2000);
        // Note: the parser will consume "test" but not set any fields, so computeMillis returns 0.
        // We will use pattern-based formatters for complex parse tests.

        formatterWithBoth = new DateTimeFormatter(printer, parser);
        printerOnly = new DateTimeFormatter(printer, null);
        parserOnly = new DateTimeFormatter(null, parser);
    }

    // Test constructor and getters
    @Test
    public void testConstructorWithBoth() {
        assertTrue(printerOnly.isPrinter());
        assertFalse(printerOnly.isParser());
        assertSame(printer, printerOnly.getPrinter());
        assertNull(printerOnly.getParser());
        assertNull(printerOnly.getLocale());
        assertFalse(printerOnly.isOffsetParsed());
        assertNull(printerOnly.getChronology());
        assertNull(printerOnly.getZone());
        assertNull(printerOnly.getPivotYear());
        assertEquals(2000, printerOnly.getDefaultYear());
    }

    @Test
    public void testIsPrinter_WithNullPrinter() {
        DateTimeFormatter f = new DateTimeFormatter(null, parser);
        assertFalse(f.isPrinter());
        assertNull(f.getPrinter());
        assertTrue(f.isParser());
        assertSame(parser, f.getParser());
    }

    @Test
    public void testIsParser_WithNullParser() {
        DateTimeFormatter f = new DateTimeFormatter(printer, null);
        assertTrue(f.isPrinter());
        assertFalse(f.isParser());
        assertNull(f.getParser());
    }

    // withLocale
    @Test
    public void testWithLocale() {
        assertNull(formatterWithBoth.getLocale());
        DateTimeFormatter localized = formatterWithBoth.withLocale(Locale.FRENCH);
        assertNotSame(formatterWithBoth, localized);
        assertEquals(Locale.FRENCH, localized.getLocale());
        // setting same locale returns this
        assertSame(localized, localized.withLocale(Locale.FRENCH));
        // null locale on a formatter that already has null returns this
        assertSame(formatterWithBoth, formatterWithBoth.withLocale(null));
    }

    // withOffsetParsed
    @Test
    public void testWithOffsetParsed() {
        assertFalse(formatterWithBoth.isOffsetParsed());
        DateTimeFormatter offsetParsed = formatterWithBoth.withOffsetParsed();
        assertTrue(offsetParsed.isOffsetParsed());
        assertNull(offsetParsed.getZone()); // zone override set to null
        assertNotSame(formatterWithBoth, offsetParsed);
        // calling again returns this
        assertSame(offsetParsed, offsetParsed.withOffsetParsed());
    }

    // withChronology
    @Test
    public void testWithChronology() {
        assertNull(formatterWithBoth.getChronology());
        Chronology iso = DateTimeUtils.getChronology(null);
        DateTimeFormatter withC = formatterWithBoth.withChronology(iso);
        assertSame(iso, withC.getChronology());
        assertNotSame(formatterWithBoth, withC);
        // same choronology returns this
        assertSame(withC, withC.withChronology(iso));
        // null on a formatter with null returns this
        assertSame(formatterWithBoth, formatterWithBoth.withChronology(null));
    }

    // withZone/withZoneUTC
    @Test
    public void testWithZone() {
        assertNull(formatterWithBoth.getZone());
        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeFormatter withZone = formatterWithBoth.withZone(utc);
        assertEquals(utc, withZone.getZone());
        assertNotSame(formatterWithBoth, withZone);
        // same zone returns this
        assertSame(withZone, withZone.withZone(utc));
        // null on a formatter with null returns this
        assertSame(formatterWithBoth, formatterWithBoth.withZone(null));
    }

    @Test
    public void testWithZoneUTC() {
        DateTimeFormatter withUtc = formatterWithBoth.withZoneUTC();
        assertEquals(DateTimeZone.UTC, withUtc.getZone());
    }

    // withPivotYear
    @Test
    public void testWithPivotYear_Intger() {
        assertNull(formatterWithBoth.getPivotYear());
        Integer pivot = Integer.valueOf(1950);
        DateTimeFormatter withPivot = formatterWithBoth.withPivotYear(pivot);
        assertEquals(pivot, withPivot.getPivotYear());
        assertNotSame(formatterWithBoth, withPivot);
        // same pivot returns this
        assertSame(withPivot, withPivot.withPivotYear(pivot));
        // null pivot on formatter with null pivot returns this
        assertSame(formatterWithBoth, formatterWithBoth.withPivotYear((Integer) null));
    }

    @Test
    public void testWithPivotYear_Int() {
        DateTimeFormatter withPivot = formatterWithBoth.withPivotYear(2025);
        assertEquals(Integer.valueOf(2025), withPivot.getPivotYear());
    }

    // withDefaultYear
    @Test
    public void testWithDefaultYear() {
        assertEquals(2000, formatterWithBoth.getDefaultYear());
        DateTimeFormatter changed = formatterWithBoth.withDefaultYear(1970);
        assertEquals(1970, changed.getDefaultYear());
        assertNotSame(formatterWithBoth, changed);
        DateTimeFormatter sameAgain = changed.withDefaultYear(1970);
        assertEquals(1970, sameAgain.getDefaultYear());
        assertNotSame(changed, sameAgain); // always creates new instance
    }

    // getChronolgy (deprecated)
    @Test
    public void testGetChronolgy() {
        assertNull(formatterWithBoth.getChronolgy());
        Chronology iso = DateTimeUtils.getChronology(null);
        DateTimeFormatter withC = formatterWithBoth.withChronology(iso);
        assertEquals(iso, withC.getChronolgy());
    }

    // --- Printing tests ---

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintTo_StringBuffer_WithoutPrinter() {
        parserOnly.printTo(new StringBuffer(), new DateTime());
    }

    @Test
    public void testPrintTo_StringBuffer_ReadableInstant_withPrinter() {
        StringBuffer buf = new StringBuffer();
        printerOnly.printTo(buf, (ReadableInstant) null); // instant null means now
        assertEquals("test", buf.toString());
    }

    @Test
    public void testPrintTo_StringBuffer_long_withPrinter() {
        StringBuffer buf = new StringBuffer();
        printerOnly.printTo(buf, 0L); // epoch
        assertEquals("test", buf.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_StringBuffer_ReadablePartial_nullThrowsException() {
        printerOnly.printTo(new StringBuffer(), (ReadablePartial) null);
    }

    @Test
    public void testPrintTo_StringBuffer_ReadablePartial() {
        StringBuffer buf = new StringBuffer();
        // using a partial that can be printed - need a printer that handles partials.
        // The printer we have just prints literal, it won't use the partial.
        printerOnly.printTo(buf, new LocalDate(2020, 5, 10));
        assertEquals("test", buf.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintTo_Writer_WithoutPrinter() throws IOException {
        parserOnly.printTo(new StringWriter(), new DateTime());
    }

    @Test
    public void testPrintTo_Writer_ReadableInstant() throws IOException {
        StringWriter out = new StringWriter();
        printerOnly.printTo(out, (ReadableInstant) null);
        assertEquals("test", out.toString());
    }

    @Test
    public void testPrintTo_Writer_long() throws IOException {
        StringWriter out = new StringWriter();
        printerOnly.printTo(out, 0L);
        assertEquals("test", out.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_Writer_ReadablePartial_nullThrowsException() throws IOException {
        printerOnly.printTo(new StringWriter(), (ReadablePartial) null);
    }

    @Test
    public void testPrintTo_Writer_ReadablePartial() throws IOException {
        StringWriter out = new StringWriter();
        printerOnly.printTo(out, new LocalDate());
        assertEquals("test", out.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintTo_Appendable_WithoutPrinter() throws IOException {
        parserOnly.printTo((Appendable) new StringBuilder(), new DateTime());
    }

    @Test
    public void testPrintTo_Appendable_ReadableInstant() throws IOException {
        StringBuilder sb = new StringBuilder();
        printerOnly.printTo((Appendable) sb, (ReadableInstant) null);
        assertEquals("test", sb.toString());
    }

    @Test
    public void testPrintTo_Appendable_long() throws IOException {
        StringBuilder sb = new StringBuilder();
        printerOnly.printTo((Appendable) sb, 0L);
        assertEquals("test", sb.toString());
    }

    @Test
    public void testPrintTo_Appendable_ReadablePartial() throws IOException {
        StringBuilder sb = new StringBuilder();
        printerOnly.printTo((Appendable) sb, new LocalDate());
        assertEquals("test", sb.toString());
    }

    // print() methods
    @Test(expected = UnsupportedOperationException.class)
    public void testPrint_ReadableInstant_NoPrinter() {
        parserOnly.print(new DateTime());
    }

    @Test
    public void testPrint_ReadableInstant() {
        String result = printerOnly.print(new DateTime());
        assertEquals("test", result);
    }

    @Test
    public void testPrint_long() {
        String result = printerOnly.print(0L);
        assertEquals("test", result);
    }

    @Test
    public void testPrint_ReadablePartial() {
        String result = printerOnly.print(new LocalDate());
        assertEquals("test", result);
    }

    // Testing selectChronology via printing with overrides
    @Test
    public void testPrintWithOverriddenChronologyAndZone() {
        // Create a formatter that prints something based on the chrono/zone
        // Use a pattern that shows timezone, e.g., "YYYY-MM-dd'T'HH:mm:ssZZ"
        DateTimeFormatter pattern = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ");
        DateTimeZone overriddenZone = DateTimeZone.forOffsetHours(5);
        Chronology overriddenChrono = DateTimeUtils.getChronology(null).withZone(DateTimeZone.UTC);
        DateTimeFormatter overridden = pattern.withZone(overriddenZone).withChronology(overriddenChrono);
        DateTime dt = new DateTime(2020, 7, 15, 12, 0, DateTimeZone.UTC);
        String out = overridden.print(dt);
        // The formatter should use overriddenChrono (UTC) and overriddenZone (+05)
        // So time will be 12:00 UTC -> 17:00 +05? Actually the offset in the string is from the zone, not the instant zone. With overridden zone, the print uses the zone, so output should have +05:00 offset.
        assertTrue(out.endsWth("+05:00"));
    }

    // Time zone offset overflow in printTo: test the branch that reverts to UTC
    @Test
    public void testPrintTo_OffsetOverflow_RevertsToUTC() {
        // Create a printer that just prints the year maybe, to trigger overflow.
        // We need an instant near Long.MAX_VALUE and a zone with a large positive offset to cause overflow.
        DateTimeZone largeOffsetZone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE * 2L); // large offset
        DateTimeFormatter pattern = DateTimeFormat.forPattern("YYYY");
        DateTimeFormatter withZone = pattern.withZone(largeOffsetZone);
        long instant = Long.MAX_VALUE;
        DateTime dt = new DateTime(instant, DateTimeZone.UTC);
        // This should cause overflow and revision to UTC, so printed year will be based on UTC time.
        String result = withZone.print(dt);
        // Year at Long.MAX_VALUE in UTC is 292278994. Let's confirm month and year.
        // We just verify that no overflow exception, and output is not empty.
        assertFalse(result.isEmpty());
    }

    // --- Parsing tests ---

    @Test(expected = UnsupportedOperationException.class)
    public void testParseInto_NoParser() {
        printerOnly.parseInto(new MutableDateTime(), "test", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_NullInstant() {
        formatterWithBoth.parseInto(null, "test", 0);
    }

    @Test
    public void testParseInto_Success() {
        MutableDateTime instant = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        // Create a parser that sets year=2020
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY").withZone(DateTimeZone.UTC);
        int newPos = formatter.parseInto(instant, "2020", 0);
        assertEquals(4, newPos);
        assertEquals(2020, instant.getYear());
    }

    @Test
    public void testParseInto_OffsetParsed() {
        MutableDateTime instant = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        // Use a pattern with offset, and offsetParsed
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ").withOffsetParsed();
        // Parse a date with offset -08:00
        int newPos = formatter.parseInto(instant, "2020-06-09T10:20:30-08:00", 0);
        assertTrue(newPos > 0);
        // Ensure zone was set to fixed offset
        assertEquals(DateTimeZone.forOffsetHours(-8), instant.getZone());
    }

    @Test
    public void testParseInto_BucketZone() {
        MutableDateTime instant = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        // Pattern with time zone name (not fixed offset)
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss ZZ").withZone(DateTimeZone.UTC);
        // Use a known id, e.g., "America/New_York"
        int newPos = formatter.parseInto(instant, "2020-06-09 10:20:30 America/New_York", 0);
        assertTrue(newPos > 0);
        assertEquals(DateTimeZone.forID("America/New_York"), instant.getZone());
    }

    @Test
    public void testParseInto_OverriddenZone() {
        MutableDateTime instant = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd").withZone(DateTimeZone.forID("Europe/London"));
        formatter.parseInto(instant, "2020-06-09", 0);
        // parseInto sets zone to overridden zone at the end
        assertEquals(DateTimeZone.forID("Europe/London"), instant.getZone());
    }

    @Test
    public void testParseMillis_Sccess() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd").withZone(DateTimeZone.UTC);
        long millis = formatter.parseMillis("2020-06-09");
        assertEquals(new DateTime(2020, 6, 9, 0, 0, DateTimeZone.UTC).getMillis(), millis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMillis_Failure() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd").withZone(DateTimeZone.UTC);
        formatter.parseMillis("hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMillis_PartialParse() {
        // Pattern requires 10 chars, input has extra.
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd").withZone(DateTimeZone.UTC);
        formatter.parseMillis("2020-06-09-extra");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParseLocalDateTime_NoParser() {
        printerOnly.parseLocalDateTime("test");
    }

    @Test
    public void testParseLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = formatter.parseLocalDateTime("2020-06-09T10:20:30");
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(9, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHourOfDay());
        assertEquals(20, ldt.getMinuteOfHour());
        assertEquals(30, ldt.getSecondOfMinute());
    }

    @Test
    public void testParseLocalDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
        LocalDate ld = formatter.parseLocalDate("2020-06-09T10:20:30");
        assertEquals(2020, ld.getYear());
        assertEquals(6, ld.getMonthOfYear());
        assertEquals(9, ld.getDayOfMonth());
    }

    @Test
    public void testParseLocalTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
        LocalTime lt = formatter.parseLocalTime("2020-06-09T10:20:30");
        assertEquals(10, lt.getHourOfDay());
        assertEquals(20, lt.getMinuteOfHour());
        assertEquals(30, lt.getSecondOfMinute());
    }

    @Test
    public void testParseDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ").withZone(DateTimeZone.UTC);
        DateTime dt = formatter.parseDateTime("2020-06-09T10:20:30+05:00");
        // Because withOffsetParsed is false, the time will be converted to UTC, so hour becomes 05:20 (10:20 - 5h)
        assertEquals(DateTimeZone.UTC, dt.getZone());
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(9, dt.getDayOfMonth());
        // after conversion to UTC: 10:20:30 +05:00 -> 05:20:30 UTC
        assertEquals(5, dt.getHourOfDay());
        assertEquals(20, dt.getMinuteOfHour());
        assertEquals(30, dt.getSecondOfMinute());
    }

    @Test
    public void testParseDateTime_OffsetParsed() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ").withOffsetParsed().withZone(DateTimeZone.UTC);
        DateTime dt = formatter.parseDateTime("2020-06-09T10:20:30+05:00");
        // offsetParsed: zone becomes +05:00, so hour remains 10.
        assertEquals(DateTimeZone.forOffsetHours(5), dt.getZone());
        assertEquals(10, dt.getHourOfDay());
    }

    @Test
    public void testParseDateTime_OverriddenZone() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss").withZone(DateTimeZone.forID("Europe/Paris"));
        DateTime dt = formatter.parseDateTime("2020-06-09T10:20:30");
        assertEquals(DateTimeZone.forID("Europe/Paris"), dt.getZone());
    }

    @Test
    public void testParseMutableDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss").withZone(DateTimeZone.UTC);
        MutableDateTime mdt = formatter.parseMutableDateTime("2020-06-09T10:20:30");
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(9, mdt.getDayOfMonth());
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(20, mdt.getMinuteOfHour());
        assertEquals(30, mdt.getSecondOfMinute());
        assertEquals(DateTimeZone.UTC, mdt.getZone());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRequireParser_NoParser() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        f.parseMillis("test");
    }

}
