package com.fasterxml.jackson.databind.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

public class StdDateFormatTest {

    private final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private final TimeZone PST = TimeZone.getTimeZone("PST");
    private final Locale US = Locale.US;
    private final Locale FRANCE = Locale.FRANCE;

    @Test
    public void testDefaultConstructor() {
        StdDateFormat df = new StdDateFormat();
        assertNull(df._timezone);
        assertEquals(US, df._locale);
        assertNotNull(df.toString());
    }

    @Test
    public void testConstructorWithTimeZone() {
        StdDateFormat df = new StdDateFormat(PST);
        assertEquals(PST, df._timezone);
        assertEquals(US, df._locale);
    }

    @Test
    public void testConstructorWithTimeZoneAndLocale() {
        StdDateFormat df = new StdDateFormat(PST, FRANCE);
        assertEquals(PST, df._timezone);
        assertEquals(FRANCE, df._locale);
    }

    @Test
    public void testWithTimeZoneNull() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat df2 = df.withTimeZone(null);
        assertNotNull(df2);
        assertEquals(StdDateFormat.getDefaultTimeZone(), df2._timezone);
    }

    @Test
    public void testWithTimeZoneSame() {
        StdDateFormat df = new StdDateFormat(PST, US);
        StdDateFormat df2 = df.withTimeZone(PST);
        assertSame(df, df2);
    }

    @Test
    public void testWithTimeZoneDifferent() {
        StdDateFormat df = new StdDateFormat(PST, US);
        StdDateFormat df2 = df.withTimeZone(GMT);
        assertNotSame(df, df2);
        assertEquals(GMT, df2._timezone);
        assertEquals(US, df2._locale);
    }

    @Test
    public void testWithLocaleSame() {
        StdDateFormat df = new StdDateFormat(PST, US);
        StdDateFormat df2 = df.withLocale(US);
        assertSame(df, df2);
    }

    @Test
    public void testWithLocaleDifferent() {
        StdDateFormat df = new StdDateFormat(PST, US);
        StdDateFormat df2 = df.withLocale(FRANCE);
        assertNotSame(df, df2);
        assertEquals(PST, df2._timezone);
        assertEquals(FRANCE, df2._locale);
    }

    @Test
    public void testClone() {
        StdDateFormat df = new StdDateFormat(PST, FRANCE);
        StdDateFormat clone = df.clone();
        assertNotSame(df, clone);
        assertEquals(df._timezone, clone._timezone);
        assertEquals(df._locale, clone._locale);
    }

    @Test
    public void testGetBlueprintISO8601Format() {
        DateFormat f = StdDateFormat.getBlueprintISO8601Format();
        assertNotNull(f);
        assertTrue(f instanceof SimpleDateFormat);
    }

    @Test
    public void testGetBlueprintRFC1123Format() {
        DateFormat f = StdDateFormat.getBlueprintRFC1123Format();
        assertNotNull(f);
        assertTrue(f instanceof SimpleDateFormat);
    }

    @Test
    public void testGetISO8601FormatWithTimeZone() {
        DateFormat f = StdDateFormat.getISO8601Format(PST);
        assertNotNull(f);
        assertEquals(PST, f.getTimeZone());
    }

    @Test
    public void testGetISO8601FormatWithTimeZoneAndLocale() {
        DateFormat f = StdDateFormat.getISO8601Format(PST, FRANCE);
        assertNotNull(f);
        assertEquals(PST, f.getTimeZone());
    }

    @Test
    public void testGetRFC1123FormatWithTimeZone() {
        DateFormat f = StdDateFormat.getRFC1123Format(PST);
        assertNotNull(f);
        assertEquals(PST, f.getTimeZone());
    }

    @Test
    public void testGetRFC1123FormatWithTimeZoneAndLocale() {
        DateFormat f = StdDateFormat.getRFC1123Format(PST, FRANCE);
        assertNotNull(f);
        assertEquals(PST, f.getTimeZone());
    }

    @Test
    public void testSetTimeZoneResetsFormats() throws Exception {
        StdDateFormat df = new StdDateFormat(PST, US);
        // trigger lazy init
        df.format(new Date());
        assertNotNull(df._formatISO8601);
        df.setTimeZone(GMT);
        assertNull(df._formatISO8601);
        assertNull(df._formatRFC1123);
        assertNull(df._formatISO8601_z);
        assertNull(df._formatPlain);
        assertEquals(GMT, df._timezone);
    }

    @Test
    public void testParseStringISO8601Z() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("2015-03-17T12:30:00.000Z");
        assertNotNull(d);
    }

    @Test
    public void testParseStringISO8601Offset() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("2015-03-17T12:30:00.000+0000");
        assertNotNull(d);
    }

    @Test
    public void testParseStringPlainDate() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("2015-03-17");
        assertNotNull(d);
    }

    @Test
    public void testParseStringRFC1123() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("Tue, 17 Mar 2015 12:30:00 GMT");
        assertNotNull(d);
    }

    @Test
    public void testParseStringNumericTimestamp() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("1426595400000");
        assertNotNull(d);
        assertEquals(1426595400000L, d.getTime());
    }

    @Test
    public void testParseStringNegativeTimestamp() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("-100000");
        assertNotNull(d);
        assertEquals(-100000L, d.getTime());
    }

    @Test(expected = ParseException.class)
    public void testParseStringInvalid() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        df.parse("not-a-date");
    }

    @Test
    public void testParseStringInvalidMessage() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        try {
            df.parse("not-a-date");
            fail("Expected ParseException");
        } catch (ParseException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("not-a-date"));
            assertTrue(msg.contains("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            assertTrue(msg.contains("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            assertTrue(msg.contains("EEE, dd MMM yyyy HH:mm:ss zzz"));
            assertTrue(msg.contains("yyyy-MM-dd"));
        }
    }

    @Test
    public void testParseWithParsePositionISO8601() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("2015-03-17T12:30:00.000Z", pos);
        assertNotNull(d);
        assertTrue(pos.getIndex() > 0);
    }

    @Test
    public void testParseWithParsePositionRFC1123() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("Tue, 17 Mar 2015 12:30:00 GMT", pos);
        assertNotNull(d);
        assertTrue(pos.getIndex() > 0);
    }

    @Test
    public void testParseWithParsePositionNumeric() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("1426595400000", pos);
        assertNotNull(d);
        assertEquals(1426595400000L, d.getTime());
    }

    @Test
    public void testParseWithParsePositionInvalid() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("not-a-date", pos);
        assertNull(d);
        assertTrue(pos.getErrorIndex() >= 0);
    }

    @Test
    public void testFormat() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date date = new Date(1426595400000L);
        String formatted = df.format(date);
        assertNotNull(formatted);
        assertTrue(formatted.contains("2015-03-17"));
    }

    @Test
    public void testToStringNoTimezone() {
        StdDateFormat df = new StdDateFormat();
        String s = df.toString();
        assertTrue(s.contains("DateFormat"));
        assertTrue(s.contains("locale: " + US));
        assertFalse(s.contains("timezone:"));
    }

    @Test
    public void testToStringWithTimezone() {
        StdDateFormat df = new StdDateFormat(PST, US);
        String s = df.toString();
        assertTrue(s.contains("timezone:"));
        assertTrue(s.contains(PST.getDisplayName()));
    }

    @Test
    public void testLooksLikeISO8601True() {
        StdDateFormat df = new StdDateFormat();
        assertTrue(df.looksLikeISO8601("2015-03-17"));
        assertTrue(df.looksLikeISO8601("2015-03-17T12:30:00.000Z"));
    }

    @Test
    public void testLooksLikeISO8601False() {
        StdDateFormat df = new StdDateFormat();
        assertFalse(df.looksLikeISO8601("Tue, 17 Mar 2015 12:30:00 GMT"));
        assertFalse(df.looksLikeISO8601("12345"));
        assertFalse(df.looksLikeISO8601("2015/03/17"));
    }

    @Test
    public void testParseAsISO8601PlainDate() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601Zulu() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000Z", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601ZuluMissingMilliseconds() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00Z", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneColon() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000+05:00", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneNoColon() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000+0500", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneMissingMinutes() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000+05", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601NoTimeZone() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601NoTimeZoneMissingMilliseconds() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsRFC1123() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsRFC1123("Tue, 17 Mar 2015 12:30:00 GMT", pos);
        assertNotNull(d);
    }

    @Test
    public void testHasTimeZoneTrue() {
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+0500"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+05:00"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+05"));
    }

    @Test
    public void testHasTimeZoneFalse() {
        assertFalse(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000"));
        assertFalse(StdDateFormat.hasTimeZone("2015-03-17"));
        assertFalse(StdDateFormat.hasTimeZone("12345"));
    }

    @Test
    public void testCloneFormatDefaultLocale() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        DateFormat cloned = StdDateFormat._cloneFormat(df, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", null, US);
        assertNotNull(cloned);
        assertEquals(GMT, cloned.getTimeZone());
    }

    @Test
    public void testCloneFormatNonDefaultLocale() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        DateFormat cloned = StdDateFormat._cloneFormat(df, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", PST, FRANCE);
        assertNotNull(cloned);
        assertEquals(PST, cloned.getTimeZone());
    }

    @Test
    public void testCloneFormatWithNullTimezone() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        DateFormat cloned = StdDateFormat._cloneFormat(df, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", null, US);
        assertEquals(GMT, cloned.getTimeZone());
    }

    @Test
    public void testParseStringTrimsInput() throws Exception {
        StdDateFormat df = new StdDateFormat(GMT, US);
        Date d = df.parse("  2015-03-17T12:30:00.000Z  ");
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601LazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatISO8601);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000+0500", pos);
        assertNotNull(d);
        assertNotNull(df._formatISO8601);
    }

    @Test
    public void testParseAsRFC1123LazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatRFC1123);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsRFC1123("Tue, 17 Mar 2015 12:30:00 PST", pos);
        assertNotNull(d);
        assertNotNull(df._formatRFC1123);
    }

    @Test
    public void testFormatLazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatISO8601);
        String s = df.format(new Date());
        assertNotNull(s);
        assertNotNull(df._formatISO8601);
    }

    @Test
    public void testParseNumericTooLarge() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("99999999999999999999999999999999999999999999999999", pos);
        assertNull(d);
    }

    @Test
    public void testParseNumericNegativeButNotAllDigits() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("-123abc", pos);
        assertNull(d);
    }

    @Test
    public void testParseNumericPositiveNotInLongRange() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("99999999999999999999999999999999999999999999999999", pos);
        assertNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneMissingMillisecondsAndSeconds() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30+0500", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneMissingMillisecondsAndSecondsAndColon() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30+05:00", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneMissingMillisecondsAndSecondsAndMissingMinutes() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30+05", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601ZuluWithColonInTime() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00Z", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601ZuluWithColonAndMilliseconds() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.123Z", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601ZuluWithColonMissingMilliseconds() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00Z", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseAsISO8601PlainDateLazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatPlain);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17", pos);
        assertNotNull(d);
        assertNotNull(df._formatPlain);
    }

    @Test
    public void testParseAsISO8601ZuluLazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatISO8601_z);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000Z", pos);
        assertNotNull(d);
        assertNotNull(df._formatISO8601_z);
    }

    @Test
    public void testParseAsISO8601NoTimeZoneLazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatISO8601_z);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00.000", pos);
        assertNotNull(d);
        assertNotNull(df._formatISO8601_z);
    }

    @Test
    public void testParseAsISO8601WithTimeZoneMissingMillisecondsLazyInit() {
        StdDateFormat df = new StdDateFormat(PST, US);
        assertNull(df._formatISO8601);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parseAsISO8601("2015-03-17T12:30:00+0500", pos);
        assertNotNull(d);
        assertNotNull(df._formatISO8601);
    }

    @Test
    public void testHasTimeZoneBoundary() {
        assertFalse(StdDateFormat.hasTimeZone("12345"));
        assertFalse(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+05"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+0500"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000+05:00"));
        assertTrue(StdDateFormat.hasTimeZone("2015-03-17T12:30:00.000-05:00"));
    }

    @Test
    public void testParseStringWithParsePositionAllDigits() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("1234567890", pos);
        assertNotNull(d);
        assertEquals(1234567890L, d.getTime());
    }

    @Test
    public void testParseStringWithParsePositionNegativeAllDigits() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("-1234567890", pos);
        assertNotNull(d);
        assertEquals(-1234567890L, d.getTime());
    }

    @Test
    public void testParseStringWithParsePositionAllDigitsButNotInLongRange() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("99999999999999999999999999999999999999999999999999", pos);
        assertNull(d);
    }

    @Test
    public void testParseStringWithParsePositionNegativeNotAllDigits() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("-123abc", pos);
        assertNull(d);
    }

    @Test
    public void testParseStringWithParsePositionRFC1123Fallback() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("Tue, 17 Mar 2015 12:30:00 GMT", pos);
        assertNotNull(d);
    }

    @Test
    public void testParseStringWithParsePositionInvalidRFC1123() {
        StdDateFormat df = new StdDateFormat(GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("Invalid Date String", pos);
        assertNull(d);
    }

    @Test
    public void testFormatWithDifferentTimezone() {
        StdDateFormat df = new StdDateFormat(PST, US);
        Date date = new Date(1426595400000L);
        String formatted = df.format(date);
        assertNotNull(formatted);
        // Should be in PST, but format uses ISO8601 with timezone offset
        assertTrue(formatted.contains("-08:00") || formatted.contains("-0700"));
    }

    @Test
    public void testCloneFormatWithNullTimezoneAndNonDefaultLocale() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        DateFormat cloned = StdDateFormat._cloneFormat(df, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", null, FRANCE);
        assertNotNull(cloned);
        assertEquals(GMT, cloned.getTimeZone());
    }

    @Test
    public void testCloneFormatWithTimezoneAndDefaultLocale() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        DateFormat cloned = StdDateFormat._cloneFormat(df, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", PST, US);
        assertNotNull(cloned);
        assertEquals(PST, cloned.getTimeZone());
    }

    @Test
    public void testGetDefaultTimeZone() {
        assertEquals(GMT, StdDateFormat.getDefaultTimeZone());
    }

    @Test
    public void testInstance() {
        assertNotNull(StdDateFormat.instance);
        assertTrue(StdDateFormat.instance instanceof StdDateFormat);
    }
}
