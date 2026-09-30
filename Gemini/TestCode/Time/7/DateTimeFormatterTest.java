package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFormatterTest {

    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone OFFSET_P2 = DateTimeZone.forOffsetHours(2);

    private DateTimeZone originalDefaultZone;
    private Locale originalDefaultLocale;

    private DateTimeFormatter isoFormatter;
    private DateTimeFormatter patternFormatter;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        originalDefaultLocale = Locale.getDefault();
        DateTimeZone.setDefault(LONDON);
        Locale.setDefault(Locale.UK);

        isoFormatter = ISODateTimeFormat.dateTime();
        patternFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS Z");
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
        Locale.setDefault(originalDefaultLocale);
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testConstructorAndCapabilities() {
        DateTimeFormatter formatter = new DateTimeFormatter(isoFormatter.getPrinter(), isoFormatter.getParser());
        Assert.assertTrue(formatter.isPrinter());
        Assert.assertTrue(formatter.isParser());
        Assert.assertNotNull(formatter.getPrinter());
        Assert.assertNotNull(formatter.getParser());
        Assert.assertNull(formatter.getLocale());
        Assert.assertFalse(formatter.isOffsetParsed());
        Assert.assertNull(formatter.getChronology());
        Assert.assertNull(formatter.getChronolgy());
        Assert.assertNull(formatter.getZone());
        Assert.assertNull(formatter.getPivotYear());
        Assert.assertEquals(2000, formatter.getDefaultYear());
    }

    @Test
    public void testPrinterParserNullSupport() {
        DateTimeFormatter printerOnly = new DateTimeFormatter(isoFormatter.getPrinter(), null);
        Assert.assertTrue(printerOnly.isPrinter());
        Assert.assertFalse(printerOnly.isParser());
        Assert.assertNotNull(printerOnly.getPrinter());
        Assert.assertNull(printerOnly.getParser());

        DateTimeFormatter parserOnly = new DateTimeFormatter(null, isoFormatter.getParser());
        Assert.assertFalse(parserOnly.isPrinter());
        Assert.assertTrue(parserOnly.isParser());
        Assert.assertNull(parserOnly.getPrinter());
        Assert.assertNotNull(parserOnly.getParser());
    }

    @Test
    public void testWithLocale() {
        DateTimeFormatter base = DateTimeFormat.forPattern("MMMM dd, yyyy");
        Assert.assertNull(base.getLocale());

        DateTimeFormatter french = base.withLocale(Locale.FRENCH);
        Assert.assertEquals(Locale.FRENCH, french.getLocale());
        Assert.assertSame(french, french.withLocale(Locale.FRENCH));
        Assert.assertSame(french, french.withLocale(new Locale("fr")));

        DateTimeFormatter backToNull = french.withLocale(null);
        Assert.assertNull(backToNull.getLocale());
        Assert.assertSame(backToNull, backToNull.withLocale(null));

        DateTime dt = new DateTime(2020, 1, 15, 12, 0, 0, 0, DateTimeZone.UTC);
        Assert.assertEquals("janvier 15, 2020", french.print(dt));
    }

    @Test
    public void testWithOffsetParsed() {
        DateTimeFormatter base = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
        Assert.assertFalse(base.isOffsetParsed());

        DateTimeFormatter offsetParsed = base.withOffsetParsed();
        Assert.assertTrue(offsetParsed.isOffsetParsed());
        Assert.assertSame(offsetParsed, offsetParsed.withOffsetParsed());
        Assert.assertNull(offsetParsed.getZone());

        DateTimeFormatter withZone = offsetParsed.withZone(PARIS);
        Assert.assertFalse(withZone.isOffsetParsed());
        Assert.assertEquals(PARIS, withZone.getZone());

        DateTime parsed = offsetParsed.parseDateTime("2020-05-10 14:30:00 -0400");
        Assert.assertEquals(DateTimeZone.forOffsetHours(-4), parsed.getZone());
    }

    @Test
    public void testWithChronology() {
        Chronology coptic = CopticChronology.getInstanceUTC();
        DateTimeFormatter base = DateTimeFormat.forPattern("yyyy-MM-dd");
        Assert.assertNull(base.getChronology());

        DateTimeFormatter copticFormatter = base.withChronology(coptic);
        Assert.assertSame(coptic, copticFormatter.getChronology());
        Assert.assertSame(coptic, copticFormatter.getChronolgy());
        Assert.assertSame(copticFormatter, copticFormatter.withChronology(coptic));

        DateTimeFormatter cleared = copticFormatter.withChronology(null);
        Assert.assertNull(cleared.getChronology());
    }

    @Test
    public void testWithZoneAndWithZoneUTC() {
        DateTimeFormatter base = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Assert.assertNull(base.getZone());

        DateTimeFormatter paris = base.withZone(PARIS);
        Assert.assertEquals(PARIS, paris.getZone());
        Assert.assertSame(paris, paris.withZone(PARIS));

        DateTimeFormatter utc = paris.withZoneUTC();
        Assert.assertEquals(DateTimeZone.UTC, utc.getZone());
        Assert.assertSame(utc, utc.withZone(DateTimeZone.UTC));

        DateTimeFormatter cleared = utc.withZone(null);
        Assert.assertNull(cleared.getZone());
    }

    @Test
    public void testWithPivotYear() {
        DateTimeFormatter base = DateTimeFormat.forPattern("yy-MM-dd");
        Assert.assertNull(base.getPivotYear());

        DateTimeFormatter withPivot = base.withPivotYear(1950);
        Assert.assertEquals(Integer.valueOf(1950), withPivot.getPivotYear());
        Assert.assertSame(withPivot, withPivot.withPivotYear(1950));
        Assert.assertSame(withPivot, withPivot.withPivotYear(Integer.valueOf(1950)));

        DateTimeFormatter withPivotObj = base.withPivotYear(Integer.valueOf(2025));
        Assert.assertEquals(Integer.valueOf(2025), withPivotObj.getPivotYear());

        DateTimeFormatter cleared = withPivot.withPivotYear(null);
        Assert.assertNull(cleared.getPivotYear());
        Assert.assertSame(cleared, cleared.withPivotYear(null));

        DateTime parsed1950 = withPivot.withZoneUTC().parseDateTime("20-01-01");
        Assert.assertEquals(1920, parsed1950.getYear());

        DateTime parsed2025 = withPivotObj.withZoneUTC().parseDateTime("20-01-01");
        Assert.assertEquals(2020, parsed2025.getYear());
    }

    @Test
    public void testWithDefaultYear() {
        DateTimeFormatter base = DateTimeFormat.forPattern("MM-dd");
        Assert.assertEquals(2000, base.getDefaultYear());

        DateTimeFormatter changed = base.withDefaultYear(1996);
        Assert.assertEquals(1996, changed.getDefaultYear());

        DateTime dtLeap = changed.withZoneUTC().parseDateTime("02-29");
        Assert.assertEquals(1996, dtLeap.getYear());
        Assert.assertEquals(2, dtLeap.getMonthOfYear());
        Assert.assertEquals(29, dtLeap.getDayOfMonth());
    }

    @Test
    public void testPrintReadableInstant() throws IOException {
        DateTime dt = new DateTime(2021, 6, 15, 10, 30, 0, 0, DateTimeZone.UTC);

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        Assert.assertEquals("2021-06-15 10:30:00", f.print(dt));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, dt);
        Assert.assertEquals("2021-06-15 10:30:00", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, dt);
        Assert.assertEquals("2021-06-15 10:30:00", sw.toString());

        StringBuilder sBuilder = new StringBuilder();
        f.printTo((Appendable) sBuilder, dt);
        Assert.assertEquals("2021-06-15 10:30:00", sBuilder.toString());
    }

    @Test
    public void testPrintReadableInstantNullDefaultsToNow() throws IOException {
        DateTimeUtils.setCurrentMillisFixed(1577836800000L); // 2020-01-01T00:00:00Z
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();

        Assert.assertEquals("2020-01-01", f.print((ReadableInstant) null));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, (ReadableInstant) null);
        Assert.assertEquals("2020-01-01", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, (ReadableInstant) null);
        Assert.assertEquals("2020-01-01", sw.toString());

        StringBuilder app = new StringBuilder();
        f.printTo((Appendable) app, (ReadableInstant) null);
        Assert.assertEquals("2020-01-01", app.toString());
    }

    @Test
    public void testPrintLongMillis() throws IOException {
        long millis = new DateTime(2021, 6, 15, 10, 30, 0, 0, DateTimeZone.UTC).getMillis();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

        Assert.assertEquals("2021-06-15 10:30:00", f.print(millis));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, millis);
        Assert.assertEquals("2021-06-15 10:30:00", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, millis);
        Assert.assertEquals("2021-06-15 10:30:00", sw.toString());

        StringBuilder app = new StringBuilder();
        f.printTo((Appendable) app, millis);
        Assert.assertEquals("2021-06-15 10:30:00", app.toString());
    }

    @Test
    public void testPrintReadablePartial() throws IOException {
        LocalDate date = new LocalDate(2021, 6, 15);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy/MM/dd");

        Assert.assertEquals("2021/06/15", f.print(date));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, date);
        Assert.assertEquals("2021/06/15", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, date);
        Assert.assertEquals("2021/06/15", sw.toString());

        StringBuilder app = new StringBuilder();
        f.printTo((Appendable) app, date);
        Assert.assertEquals("2021/06/15", app.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintPartialNullStringBuffer() {
        DateTimeFormat.forPattern("yyyy").printTo((StringBuffer) null, (ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintPartialNullWriter() throws IOException {
        DateTimeFormat.forPattern("yyyy").printTo((Writer) new StringWriter(), (ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintPartialNullAppendable() throws IOException {
        DateTimeFormat.forPattern("yyyy").printTo((Appendable) new StringBuilder(), (ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintPartialNullString() {
        DateTimeFormat.forPattern("yyyy").print((ReadablePartial) null);
    }

    @Test
    public void testPrintOffsetArithmeticOverflow() {
        DateTimeZone largeOffsetZone = DateTimeZone.forOffsetHours(5);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy").withZone(largeOffsetZone);

        // Long.MAX_VALUE + positive offset overflows long
        String maxPrinted = f.print(Long.MAX_VALUE);
        Assert.assertNotNull(maxPrinted);

        // Long.MIN_VALUE with negative offset
        DateTimeZone negativeOffsetZone = DateTimeZone.forOffsetHours(-5);
        DateTimeFormatter f2 = DateTimeFormat.forPattern("yyyy").withZone(negativeOffsetZone);
        String minPrinted = f2.print(Long.MIN_VALUE);
        Assert.assertNotNull(minPrinted);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNull() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, isoFormatter.getParser());
        parserOnly.print(new DateTime());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNull() {
        DateTimeFormatter printerOnly = new DateTimeFormatter(isoFormatter.getPrinter(), null);
        printerOnly.parseMillis("2020-01-01");
    }

    @Test
    public void testParseInto() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);

        int result = f.parseInto(mdt, "2021-06-15 10:30:00 extra", 0);
        Assert.assertEquals(19, result);
        Assert.assertEquals(new DateTime(2021, 6, 15, 10, 30, 0, 0, DateTimeZone.UTC).getMillis(), mdt.getMillis());

        // Parse with failure
        int failResult = f.parseInto(mdt, "invalid-date", 0);
        Assert.assertTrue(failResult < 0);
    }

    @Test
    public void testParseIntoWithOffsetParsedAndZone() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z").withOffsetParsed();
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);

        int result = f.parseInto(mdt, "2021-06-15 10:30:00 +0200", 0);
        Assert.assertEquals(25, result);
        Assert.assertEquals(DateTimeZone.forOffsetHours(2), mdt.getZone());

        DateTimeFormatter fWithZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(NEW_YORK);
        mdt = new MutableDateTime(0L, DateTimeZone.UTC);
        fWithZone.parseInto(mdt, "2021-06-15 10:30:00", 0);
        Assert.assertEquals(NEW_YORK, mdt.getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIntoNullInstant() {
        DateTimeFormat.forPattern("yyyy").parseInto(null, "2020", 0);
    }

    @Test
    public void testParseMillis() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        long millis = f.parseMillis("2021-06-15 10:30:00");
        Assert.assertEquals(new DateTime(2021, 6, 15, 10, 30, 0, 0, DateTimeZone.UTC).getMillis(), millis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMillisIncomplete() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMillis("2021-06-15 extra text");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMillisInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMillis("not-a-date");
    }

    @Test
    public void testParseLocalDate() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate date = f.parseLocalDate("2021-06-15 10:30:00");
        Assert.assertEquals(new LocalDate(2021, 6, 15), date);
    }

    @Test
    public void testParseLocalTime() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        LocalTime time = f.parseLocalTime("2021-06-15 10:30:00");
        Assert.assertEquals(new LocalTime(10, 30, 0), time);
    }

    @Test
    public void testParseLocalDateTime() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = f.parseLocalDateTime("2021-06-15 10:30:00");
        Assert.assertEquals(new LocalDateTime(2021, 6, 15, 10, 30, 0), ldt);

        DateTimeFormatter fWithZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
        LocalDateTime ldt2 = fWithZone.parseLocalDateTime("2021-06-15 10:30:00 +0300");
        Assert.assertEquals(new LocalDateTime(2021, 6, 15, 10, 30, 0), ldt2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalDateTimeInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseLocalDateTime("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalDateTimeIncomplete() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseLocalDateTime("2021-06-15 trailing");
    }

    @Test
    public void testParseDateTime() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(PARIS);
        DateTime dt = f.parseDateTime("2021-06-15 10:30:00");
        Assert.assertEquals(2021, dt.getYear());
        Assert.assertEquals(6, dt.getMonthOfYear());
        Assert.assertEquals(15, dt.getDayOfMonth());
        Assert.assertEquals(10, dt.getHourOfDay());
        Assert.assertEquals(PARIS, dt.getZone());

        DateTimeFormatter fOffset = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z").withOffsetParsed();
        DateTime dtOffset = fOffset.parseDateTime("2021-06-15 10:30:00 -0500");
        Assert.assertEquals(DateTimeZone.forOffsetHours(-5), dtOffset.getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTimeInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseDateTime("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTimeIncomplete() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseDateTime("2021-06-15 remainder");
    }

    @Test
    public void testParseMutableDateTime() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(NEW_YORK);
        MutableDateTime mdt = f.parseMutableDateTime("2021-06-15 10:30:00");
        Assert.assertEquals(2021, mdt.getYear());
        Assert.assertEquals(6, mdt.getMonthOfYear());
        Assert.assertEquals(15, mdt.getDayOfMonth());
        Assert.assertEquals(10, mdt.getHourOfDay());
        Assert.assertEquals(NEW_YORK, mdt.getZone());

        DateTimeFormatter fOffset = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z").withOffsetParsed();
        MutableDateTime mdtOffset = fOffset.parseMutableDateTime("2021-06-15 10:30:00 +0400");
        Assert.assertEquals(DateTimeZone.forOffsetHours(4), mdtOffset.getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMutableDateTimeInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMutableDateTime("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMutableDateTimeIncomplete() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMutableDateTime("2021-06-15 extra");
    }

    @Test
    public void testChronoAndZoneInteraction() {
        GJChronology gj = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .withChronology(gj)
                .withZone(PARIS);

        DateTime dt = f.parseDateTime("2020-01-01 12:00:00");
        Assert.assertEquals(PARIS, dt.getZone());
        Assert.assertEquals(gj.withZone(PARIS), dt.getChronology());

        String printed = f.print(dt);
        Assert.assertEquals("2020-01-01 12:00:00", printed);
    }
}
