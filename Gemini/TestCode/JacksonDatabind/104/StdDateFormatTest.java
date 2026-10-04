package com.fasterxml.jackson.databind.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    @Test
    public void testDefaultConstruction() {
        Assert.assertNull(stdDateFormat.getTimeZone());
        Assert.assertTrue(stdDateFormat.isLenient());
        Assert.assertFalse(stdDateFormat.isColonIncludedInTimeZone());
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedConstructor() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat df = new StdDateFormat(tz, Locale.GERMANY);
        Assert.assertEquals(tz, df.getTimeZone());
        Assert.assertTrue(df.isLenient());
    }

    @Test
    public void testWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat df = stdDateFormat.withTimeZone(tz);
        Assert.assertNotSame(stdDateFormat, df);
        Assert.assertEquals(tz, df.getTimeZone());

        StdDateFormat dfSame = df.withTimeZone(tz);
        Assert.assertSame(df, dfSame);

        StdDateFormat dfDefault = df.withTimeZone(null);
        Assert.assertEquals(StdDateFormat.getDefaultTimeZone(), dfDefault.getTimeZone());
    }

    @Test
    public void testWithLocale() {
        StdDateFormat df = stdDateFormat.withLocale(Locale.FRANCE);
        Assert.assertNotSame(stdDateFormat, df);

        StdDateFormat dfSame = df.withLocale(Locale.FRANCE);
        Assert.assertSame(df, dfSame);
    }

    @Test
    public void testWithLenient() {
        StdDateFormat df = stdDateFormat.withLenient(Boolean.FALSE);
        Assert.assertNotSame(stdDateFormat, df);
        Assert.assertFalse(df.isLenient());

        StdDateFormat dfSame = df.withLenient(Boolean.FALSE);
        Assert.assertSame(df, dfSame);

        StdDateFormat dfTrue = df.withLenient(Boolean.TRUE);
        Assert.assertTrue(dfTrue.isLenient());

        StdDateFormat dfNull = df.withLenient(null);
        Assert.assertTrue(dfNull.isLenient());
    }

    @Test
    public void testWithColonInTimeZone() {
        StdDateFormat df = stdDateFormat.withColonInTimeZone(true);
        Assert.assertNotSame(stdDateFormat, df);
        Assert.assertTrue(df.isColonIncludedInTimeZone());

        StdDateFormat dfSame = df.withColonInTimeZone(true);
        Assert.assertSame(df, dfSame);

        StdDateFormat dfFalse = df.withColonInTimeZone(false);
        Assert.assertFalse(dfFalse.isColonIncludedInTimeZone());
    }

    @Test
    public void testClone() {
        StdDateFormat clone = stdDateFormat.clone();
        Assert.assertNotNull(clone);
        Assert.assertNotSame(stdDateFormat, clone);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetISO8601Format() {
        DateFormat df1 = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("GMT+1"), Locale.US);
        Assert.assertNotNull(df1);
        DateFormat df2 = StdDateFormat.getISO8601Format(null, Locale.GERMANY);
        Assert.assertNotNull(df2);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetRFC1123Format() {
        DateFormat df1 = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("GMT+1"), Locale.US);
        Assert.assertNotNull(df1);
        DateFormat df2 = StdDateFormat.getRFC1123Format(null, Locale.GERMANY);
        Assert.assertNotNull(df2);
    }

    @Test
    public void testSetTimeZoneAndLenient() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+1");
        TimeZone tz2 = TimeZone.getTimeZone("GMT+2");

        stdDateFormat.setTimeZone(tz1);
        Assert.assertEquals(tz1, stdDateFormat.getTimeZone());

        stdDateFormat.setTimeZone(tz1);
        Assert.assertEquals(tz1, stdDateFormat.getTimeZone());

        stdDateFormat.setTimeZone(tz2);
        Assert.assertEquals(tz2, stdDateFormat.getTimeZone());

        stdDateFormat.setLenient(false);
        Assert.assertFalse(stdDateFormat.isLenient());
        stdDateFormat.setLenient(false);
        Assert.assertFalse(stdDateFormat.isLenient());
        stdDateFormat.setLenient(true);
        Assert.assertTrue(stdDateFormat.isLenient());
    }

    @Test
    public void testFormatWithDefaultUtcAndNoColon() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 5, 6, 7, 8);
        cal.set(Calendar.MILLISECOND, 9);
        Date date = cal.getTime();

        StringBuffer sb = new StringBuffer();
        stdDateFormat.format(date, sb, new FieldPosition(0));
        Assert.assertEquals("2023-01-05T06:07:08.009+0000", sb.toString());
    }

    @Test
    public void testFormatWithDefaultUtcAndColon() {
        StdDateFormat df = stdDateFormat.withColonInTimeZone(true);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.DECEMBER, 25, 15, 30, 45);
        cal.set(Calendar.MILLISECOND, 120);
        Date date = cal.getTime();

        String formatted = df.format(date);
        Assert.assertEquals("2023-12-25T15:30:45.120+00:00", formatted);
    }

    @Test
    public void testFormatWithPositiveOffsetAndColon() {
        StdDateFormat df = stdDateFormat.withTimeZone(TimeZone.getTimeZone("GMT+05:30")).withColonInTimeZone(true);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.FEBRUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 500);

        String formatted = df.format(cal.getTime());
        Assert.assertEquals("2023-02-01T05:30:00.500+05:30", formatted);
    }

    @Test
    public void testFormatWithNegativeOffsetNoColon() {
        StdDateFormat df = stdDateFormat.withTimeZone(TimeZone.getTimeZone("GMT-08:00")).withColonInTimeZone(false);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 10, 0, 0);

        String formatted = df.format(cal.getTime());
        Assert.assertEquals("2023-01-01T02:00:00.000-0800", formatted);
    }

    @Test
    public void testFormatPadHandling() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(5, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 5);

        String formatted = stdDateFormat.format(cal.getTime());
        Assert.assertEquals("0005-01-01T00:00:00.005+0000", formatted);
    }

    @Test
    public void testParsePlainIsoDate() throws Exception {
        Date d = stdDateFormat.parse("2023-05-18");
        Assert.assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseIso8601WithZ() throws Exception {
        Date d = stdDateFormat.parse("2023-05-18T10:15:30.123Z");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(15, cal.get(Calendar.MINUTE));
        Assert.assertEquals(30, cal.get(Calendar.SECOND));
        Assert.assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseIso8601WithoutSecondsOrMillis() throws Exception {
        Date d = stdDateFormat.parse("2023-05-18T10:15Z");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        Assert.assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(15, cal.get(Calendar.MINUTE));
        Assert.assertEquals(0, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseIso8601WithOffsets() throws Exception {
        Date d1 = stdDateFormat.parse("2023-05-18T10:15:30+02:00");
        Date d2 = stdDateFormat.parse("2023-05-18T10:15:30+0200");
        Date d3 = stdDateFormat.parse("2023-05-18T10:15:30+02");
        Assert.assertEquals(d1.getTime(), d2.getTime());
        Assert.assertEquals(d1.getTime(), d3.getTime());

        Date d4 = stdDateFormat.parse("2023-05-18T10:15:30-05:00");
        Assert.assertTrue(d4.getTime() > d1.getTime());
    }

    @Test
    public void testParseIso8601WithVaryingFractions() throws Exception {
        Date d1 = stdDateFormat.parse("2023-05-18T10:15:30.1Z");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d1);
        Assert.assertEquals(100, cal.get(Calendar.MILLISECOND));

        Date d2 = stdDateFormat.parse("2023-05-18T10:15:30.12Z");
        cal.setTime(d2);
        Assert.assertEquals(120, cal.get(Calendar.MILLISECOND));

        Date d3 = stdDateFormat.parse("2023-05-18T10:15:30.123456789Z");
        cal.setTime(d3);
        Assert.assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = ParseException.class)
    public void testParseIso8601FractionTooLong() throws Exception {
        stdDateFormat.parse("2023-05-18T10:15:30.1234567890Z");
    }

    @Test
    public void testParseTimestampString() throws Exception {
        long now = 1684400000000L;
        Date d = stdDateFormat.parse(String.valueOf(now));
        Assert.assertEquals(now, d.getTime());

        long neg = -1684400000000L;
        Date dNeg = stdDateFormat.parse(String.valueOf(neg));
        Assert.assertEquals(neg, dNeg.getTime());
    }

    @Test(expected = ParseException.class)
    public void testParseTimestampOutOfRange() throws Exception {
        stdDateFormat.parse("9999999999999999999999999999999999");
    }

    @Test
    public void testParseRfc1123() throws Exception {
        Date d = stdDateFormat.parse("Thu, 18 May 2023 10:15:30 GMT");
        Assert.assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(d);
        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidPattern() throws Exception {
        stdDateFormat.parse("not-a-date-at-all");
    }

    @Test
    public void testParseWithPositionSuccess() {
        ParsePosition pos = new ParsePosition(0);
        Date d = stdDateFormat.parse("2023-05-18", pos);
        Assert.assertNotNull(d);
        Assert.assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testParseWithPositionFailure() {
        ParsePosition pos = new ParsePosition(0);
        Date d = stdDateFormat.parse("invalid-date", pos);
        Assert.assertNull(d);
    }

    @Test
    public void testToStringAndToPattern() {
        String str = stdDateFormat.toString();
        Assert.assertTrue(str.contains("DateFormat"));

        String patternStrict = stdDateFormat.withLenient(Boolean.FALSE).toPattern();
        Assert.assertTrue(patternStrict.contains("strict"));

        String patternLenient = stdDateFormat.withLenient(Boolean.TRUE).toPattern();
        Assert.assertTrue(patternLenient.contains("lenient"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Assert.assertTrue(stdDateFormat.equals(stdDateFormat));
        Assert.assertFalse(stdDateFormat.equals(null));
        Assert.assertFalse(stdDateFormat.equals("other"));
        Assert.assertFalse(stdDateFormat.equals(new StdDateFormat()));
        Assert.assertEquals(System.identityHashCode(stdDateFormat), stdDateFormat.hashCode());
    }

    @Test
    public void testLooksLikeISO8601() {
        Assert.assertTrue(stdDateFormat.looksLikeISO8601("2023-01-01"));
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("2023/01/01"));
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("short"));
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("abcd-ef-gh"));
    }

    @Test
    public void testEqualsHelper() {
        Assert.assertTrue(StdDateFormat._equals(null, null));
        Assert.assertTrue(StdDateFormat._equals("a", "a"));
        Assert.assertFalse(StdDateFormat._equals("a", "b"));
        Assert.assertFalse(StdDateFormat._equals("a", null));
        Assert.assertFalse(StdDateFormat._equals(null, "b"));
    }
}
