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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFormatterTest {

    private static final DateTimeZone UTC = DateTimeZone.UTC;
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");
    private static final DateTimeZone OFFSET_P02 = DateTimeZone.forOffsetHours(2);

    private DateTimeZone originalDateTimeZone = null;
    private Locale originalLocale = null;

    @Before
    public void setUp() {
        originalDateTimeZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        DateTimeZone.setDefault(LONDON);
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDateTimeZone);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testConstructorAndGetters() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        Assert.assertTrue(f.isPrinter());
        Assert.assertTrue(f.isParser());
        Assert.assertNotNull(f.getPrinter());
        Assert.assertNotNull(f.getParser());
        Assert.assertNull(f.getLocale());
        Assert.assertFalse(f.isOffsetParsed());
        Assert.assertNull(f.getChronology());
        Assert.assertNull(f.getChronolgy());
        Assert.assertNull(f.getZone());
        Assert.assertNull(f.getPivotYear());
        Assert.assertEquals(2000, f.getDefaultYear());
    }

    @Test
    public void testPrinterParserNullCheck() {
        DateTimeFormatter printerOnly = new DateTimeFormatter(ISODateTimeFormat.dateTime().getPrinter(), null);
        Assert.assertTrue(printerOnly.isPrinter());
        Assert.assertFalse(printerOnly.isParser());
        Assert.assertNull(printerOnly.getParser());

        try {
            printerOnly.parseMillis("2020-01-01T00:00:00Z");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Parsing not supported", ex.getMessage());
        }

        DateTimeFormatter parserOnly = new DateTimeFormatter(null, ISODateTimeFormat.dateTime().getParser());
        Assert.assertFalse(parserOnly.isPrinter());
        Assert.assertTrue(parserOnly.isParser());
        Assert.assertNull(parserOnly.getPrinter());

        try {
            parserOnly.print(0L);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Printing not supported", ex.getMessage());
        }
    }

    @Test
    public void testWithLocale() {
        DateTimeFormatter f = DateTimeFormat.forPattern("MMMM dd, yyyy");
        Assert.assertNull(f.getLocale());

        DateTimeFormatter fFr = f.withLocale(Locale.FRENCH);
        Assert.assertEquals(Locale.FRENCH, fFr.getLocale());
        Assert.assertSame(fFr, fFr.withLocale(Locale.FRENCH));
        Assert.assertSame(fFr, fFr.withLocale(new Locale("fr")));

        DateTimeFormatter fNull = fFr.withLocale(null);
        Assert.assertNull(fNull.getLocale());
        Assert.assertSame(f, f.withLocale(null));

        DateTime dt = new DateTime(2020, 1, 15, 12, 0, UTC);
        Assert.assertEquals("January 15, 2020", f.withZone(UTC).print(dt));
        Assert.assertEquals("janvier 15, 2020", fFr.withZone(UTC).print(dt));
    }

    @Test
    public void testWithOffsetParsed() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        Assert.assertFalse(f.isOffsetParsed());

        DateTimeFormatter fOffset = f.withOffsetParsed();
        Assert.assertTrue(fOffset.isOffsetParsed());
        Assert.assertSame(fOffset, fOffset.withOffsetParsed());

        // withZone resets offsetParsed
        DateTimeFormatter fZone = fOffset.withZone(PARIS);
        Assert.assertFalse(fZone.isOffsetParsed());
        Assert.assertEquals(PARIS, fZone.getZone());

        // withOffsetParsed resets zone override
        DateTimeFormatter fOffset2 = fZone.withOffsetParsed();
        Assert.assertTrue(fOffset2.isOffsetParsed());
        Assert.assertNull(fOffset2.getZone());
    }

    @Test
    public void testWithChronology() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        Assert.assertNull(f.getChronology());

        Chronology gj = GJChronology.getInstance();
        DateTimeFormatter fGj = f.withChronology(gj);
        Assert.assertSame(gj, fGj.getChronology());
        Assert.assertSame(gj, fGj.getChronolgy());
        Assert.assertSame(fGj, fGj.withChronology(gj));

        DateTimeFormatter fIso = fGj.withChronology(null);
        Assert.assertNull(fIso.getChronology());
    }

    @Test
    public void testWithZoneAndWithZoneUTC() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        Assert.assertNull(f.getZone());

        DateTimeFormatter fParis = f.withZone(PARIS);
        Assert.assertEquals(PARIS, fParis.getZone());
        Assert.assertSame(fParis, fParis.withZone(PARIS));

        DateTimeFormatter fNull = fParis.withZone(null);
        Assert.assertNull(fNull.getZone());

        DateTimeFormatter fUtc = f.withZoneUTC();
        Assert.assertEquals(UTC, fUtc.getZone());
    }

    @Test
    public void testWithPivotYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yy-MM-dd");
        Assert.assertNull(f.getPivotYear());

        DateTimeFormatter fPivot = f.withPivotYear(1950);
        Assert.assertEquals(Integer.valueOf(1950), fPivot.getPivotYear());
        Assert.assertSame(fPivot, fPivot.withPivotYear(1950));
        Assert.assertSame(fPivot, fPivot.withPivotYear(Integer.valueOf(1950)));

        DateTimeFormatter fNull = fPivot.withPivotYear((Integer) null);
        Assert.assertNull(fNull.getPivotYear());
        Assert.assertSame(f, f.withPivotYear((Integer) null));

        DateTime dt1 = fPivot.parseDateTime("20-01-01");
        Assert.assertEquals(1920, dt1.getYear());

        DateTimeFormatter fPivot2000 = f.withPivotYear(2000);
        DateTime dt2 = fPivot2000.parseDateTime("20-01-01");
        Assert.assertEquals(2020, dt2.getYear());
    }

    @Test
    public void testWithDefaultYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("MM-dd");
        Assert.assertEquals(2000, f.getDefaultYear());

        DateTimeFormatter f1996 = f.withDefaultYear(1996);
        Assert.assertEquals(1996, f1996.getDefaultYear());

        DateTime dt = f1996.withZoneUTC().parseDateTime("02-29");
        Assert.assertEquals(1996, dt.getYear());
        Assert.assertEquals(2, dt.getMonthOfYear());
        Assert.assertEquals(29, dt.getDayOfMonth());
    }

    @Test
    public void testPrintReadableInstant() throws IOException {
        DateTimeFormatter f = ISODateTimeFormat.dateTime().withZone(UTC);
        DateTime dt = new DateTime(2021, 5, 20, 10, 30, 45, 123, UTC);

        Assert.assertEquals("2021-05-20T10:30:45.123Z", f.print(dt));

        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", buf.toString());

        StringWriter writer = new StringWriter();
        f.printTo(writer, dt);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", writer.toString());

        StringBuilder sb = new StringBuilder();
        f.printTo((Appendable) sb, dt);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", sb.toString());

        // null instant means now
        long now = DateTimeUtils.currentTimeMillis();
        String printNow = f.print((ReadableInstant) null);
        Assert.assertNotNull(printNow);
    }

    @Test
    public void testPrintLongMillis() throws IOException {
        DateTimeFormatter f = ISODateTimeFormat.dateTime().withZone(UTC);
        long millis = new DateTime(2021, 5, 20, 10, 30, 45, 123, UTC).getMillis();

        Assert.assertEquals("2021-05-20T10:30:45.123Z", f.print(millis));

        StringBuffer buf = new StringBuffer();
        f.printTo(buf, millis);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", buf.toString());

        StringWriter writer = new StringWriter();
        f.printTo(writer, millis);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", writer.toString());

        StringBuilder sb = new StringBuilder();
        f.printTo((Appendable) sb, millis);
        Assert.assertEquals("2021-05-20T10:30:45.123Z", sb.toString());
    }

    @Test
    public void testPrintReadablePartial() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = new LocalDate(2021, 5, 20);

        Assert.assertEquals("2021-05-20", f.print(date));

        StringBuffer buf = new StringBuffer();
        f.printTo(buf, date);
        Assert.assertEquals("2021-05-20", buf.toString());

        StringWriter writer = new StringWriter();
        f.printTo(writer, date);
        Assert.assertEquals("2021-05-20", writer.toString());

        StringBuilder sb = new StringBuilder();
        f.printTo((Appendable) sb, date);
        Assert.assertEquals("2021-05-20", sb.toString());

        try {
            f.printTo((StringBuffer) null, (ReadablePartial) null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("The partial must not be null", ex.getMessage());
        }

        try {
            f.printTo((Writer) null, (ReadablePartial) null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("The partial must not be null", ex.getMessage());
        }
    }

    @Test
    public void testPrintTimezoneOffsetOverflow() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(TOKYO);
        long maxInstant = Long.MAX_VALUE - 1000L;
        String sMax = f.print(maxInstant);
        Assert.assertNotNull(sMax);

        long minInstant = Long.MIN_VALUE + 1000L;
        DateTimeFormatter fNeg = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forOffsetHours(-5));
        String sMin = fNeg.print(minInstant);
        Assert.assertNotNull(sMin);

        // Also test writer branch with overflow
        StringWriter sw = new StringWriter();
        try {
            f.printTo(sw, maxInstant);
            Assert.assertFalse(sw.toString().isEmpty());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testParseInto() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, UTC);

        int pos = f.parseInto(mdt, "2023-11-25 15:30:45 extra", 0);
        Assert.assertEquals(19, pos);
        Assert.assertEquals(2023, mdt.getYear());
        Assert.assertEquals(11, mdt.getMonthOfYear());
        Assert.assertEquals(25, mdt.getDayOfMonth());
        Assert.assertEquals(15, mdt.getHourOfDay());
        Assert.assertEquals(30, mdt.getMinuteOfHour());
        Assert.assertEquals(45, mdt.getSecondOfMinute());

        try {
            f.parseInto(null, "2023-11-25", 0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Instant must not be null", ex.getMessage());
        }

        DateTimeFormatter fOffset = DateTimeFormat.forPattern("yyyy-MM-dd Z").withOffsetParsed();
        MutableDateTime mdtOffset = new MutableDateTime(0L, UTC);
        fOffset.parseInto(mdtOffset, "2020-01-01 +0200", 0);
        Assert.assertEquals(OFFSET_P02, mdtOffset.getZone());

        DateTimeFormatter fZone = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(PARIS);
        MutableDateTime mdtZone = new MutableDateTime(0L, UTC);
        fZone.parseInto(mdtZone, "2020-01-01", 0);
        Assert.assertEquals(PARIS, mdtZone.getZone());

        DateTimeFormatter fZoneParser = DateTimeFormat.forPattern("yyyy-MM-dd zzz");
        MutableDateTime mdtZoneParser = new MutableDateTime(0L, UTC);
        fZoneParser.parseInto(mdtZoneParser, "2020-01-01 UTC", 0);
        Assert.assertEquals(UTC, mdtZoneParser.getZone());
    }

    @Test
    public void testParseMillis() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime().withZone(UTC);
        long millis = f.parseMillis("2021-05-20T10:30:45.123Z");
        Assert.assertEquals(new DateTime(2021, 5, 20, 10, 30, 45, 123, UTC).getMillis(), millis);

        try {
            f.parseMillis("invalid-date");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }

        try {
            f.parseMillis("2021-05-20T10:30:45.123Z extra text");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }
    }

    @Test
    public void testParseLocalDate() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        LocalDate date = f.parseLocalDate("2021-05-20T10:30:45.123+03:00");
        Assert.assertEquals(new LocalDate(2021, 5, 20), date);
    }

    @Test
    public void testParseLocalTime() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        LocalTime time = f.parseLocalTime("2021-05-20T10:30:45.123+03:00");
        Assert.assertEquals(new LocalTime(10, 30, 45, 123), time);
    }

    @Test
    public void testParseLocalDateTime() {
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        LocalDateTime ldt = f.parseLocalDateTime("2021-05-20T10:30:45.123");
        Assert.assertEquals(new LocalDateTime(2021, 5, 20, 10, 30, 45, 123), ldt);

        DateTimeFormatter fOffset = ISODateTimeFormat.dateTime();
        LocalDateTime ldtOffset = fOffset.parseLocalDateTime("2021-05-20T10:30:45.123+02:00");
        Assert.assertEquals(new LocalDateTime(2021, 5, 20, 10, 30, 45, 123), ldtOffset);

        DateTimeFormatter fZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss zzz");
        LocalDateTime ldtZone = fZone.parseLocalDateTime("2021-05-20 10:30:45 UTC");
        Assert.assertEquals(new LocalDateTime(2021, 5, 20, 10, 30, 45, 0), ldtZone);

        try {
            f.parseLocalDateTime("2021-05-20T10:30:45.123 trailing");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }

        try {
            f.parseLocalDateTime("bad text");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }
    }

    @Test
    public void testParseDateTime() {
        DateTimeFormatter f = ISODateTimeFormat.dateTimeParser();
        DateTime dt = f.parseDateTime("2021-05-20T10:30:45.123Z");
        Assert.assertEquals(2021, dt.getYear());
        Assert.assertEquals(5, dt.getMonthOfYear());
        Assert.assertEquals(20, dt.getDayOfMonth());

        DateTimeFormatter fOffset = f.withOffsetParsed();
        DateTime dtOffset = fOffset.parseDateTime("2021-05-20T10:30:45.123+02:00");
        Assert.assertEquals(OFFSET_P02, dtOffset.getZone());

        DateTimeFormatter fZoneOverride = f.withZone(PARIS);
        DateTime dtZone = fZoneOverride.parseDateTime("2021-05-20T10:30:45.123Z");
        Assert.assertEquals(PARIS, dtZone.getZone());

        DateTimeFormatter fParsedZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss zzz");
        DateTime dtParsedZone = fParsedZone.parseDateTime("2021-05-20 10:30:45 UTC");
        Assert.assertEquals(UTC, dtParsedZone.getZone());

        try {
            f.parseDateTime("2021-05-20T10:30:45.123Z trailing");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }

        try {
            f.parseDateTime("invalid");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }
    }

    @Test
    public void testParseMutableDateTime() {
        DateTimeFormatter f = ISODateTimeFormat.dateTimeParser();
        MutableDateTime mdt = f.parseMutableDateTime("2021-05-20T10:30:45.123Z");
        Assert.assertEquals(2021, mdt.getYear());
        Assert.assertEquals(5, mdt.getMonthOfYear());
        Assert.assertEquals(20, mdt.getDayOfMonth());

        DateTimeFormatter fOffset = f.withOffsetParsed();
        MutableDateTime mdtOffset = fOffset.parseMutableDateTime("2021-05-20T10:30:45.123+02:00");
        Assert.assertEquals(OFFSET_P02, mdtOffset.getZone());

        DateTimeFormatter fZoneOverride = f.withZone(PARIS);
        MutableDateTime mdtZone = fZoneOverride.parseMutableDateTime("2021-05-20T10:30:45.123Z");
        Assert.assertEquals(PARIS, mdtZone.getZone());

        DateTimeFormatter fParsedZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss zzz");
        MutableDateTime mdtParsedZone = fParsedZone.parseMutableDateTime("2021-05-20 10:30:45 UTC");
        Assert.assertEquals(UTC, mdtParsedZone.getZone());

        try {
            f.parseMutableDateTime("2021-05-20T10:30:45.123Z trailing");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }

        try {
            f.parseMutableDateTime("invalid");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid format"));
        }
    }

    @Test
    public void testSelectChronologyPrecedence() {
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTimeFormatter f = ISODateTimeFormat.dateTime().withChronology(buddhist).withZone(PARIS);
        DateTime dt = f.parseDateTime("2564-05-20T10:30:45.123+02:00");
        Assert.assertEquals(PARIS, dt.getZone());
        Assert.assertEquals(BuddhistChronology.getInstance(PARIS), dt.getChronology());
    }
}
