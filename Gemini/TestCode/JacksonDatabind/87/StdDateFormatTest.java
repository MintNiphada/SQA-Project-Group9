package com.fasterxml.jackson.databind.util;

import org.junit.Assert;
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

    @Test
    public void testConstructorsAndDefaults() {
        StdDateFormat df = new StdDateFormat();
        Assert.assertNull(df.getTimeZone());
        Assert.assertTrue(df.isLenient());

        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale loc = Locale.GERMANY;
        StdDateFormat df2 = new StdDateFormat(tz, loc);
        Assert.assertEquals(tz, df2.getTimeZone());
        Assert.assertTrue(df2.isLenient());

        StdDateFormat df3 = new StdDateFormat(tz, loc, Boolean.FALSE);
        Assert.assertEquals(tz, df3.getTimeZone());
        Assert.assertFalse(df3.isLenient());

        Assert.assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
    }

    @Test
    public void testWithTimeZone() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tzPST = TimeZone.getTimeZone("PST");

        StdDateFormat dfPST = df.withTimeZone(tzPST);
        Assert.assertNotSame(df, dfPST);
        Assert.assertEquals(tzPST, dfPST.getTimeZone());

        StdDateFormat dfPST2 = dfPST.withTimeZone(tzPST);
        Assert.assertSame(dfPST, dfPST2);

        StdDateFormat dfUTC = df.withTimeZone(null);
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), dfUTC.getTimeZone());
    }

    @Test
    public void testWithLocale() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat dfSame = df.withLocale(Locale.US);
        Assert.assertSame(df, dfSame);

        StdDateFormat dfFr = df.withLocale(Locale.FRANCE);
        Assert.assertNotSame(df, dfFr);
        Assert.assertTrue(dfFr.toString().contains("fr_FR"));
    }

    @Test
    public void testClone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat df = new StdDateFormat(tz, Locale.ITALY, Boolean.FALSE);
        StdDateFormat clone = df.clone();
        Assert.assertNotSame(df, clone);
        Assert.assertEquals(df.getTimeZone(), clone.getTimeZone());
        Assert.assertEquals(df.isLenient(), clone.isLenient());
    }

    @Test
    public void testSetTimeZoneAndLenient() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tz1 = TimeZone.getTimeZone("GMT+1");
        df.setTimeZone(tz1);
        Assert.assertEquals(tz1, df.getTimeZone());

        df.setTimeZone(tz1);
        Assert.assertEquals(tz1, df.getTimeZone());

        df.setLenient(false);
        Assert.assertFalse(df.isLenient());
        df.setLenient(false);
        Assert.assertFalse(df.isLenient());
        df.setLenient(true);
        Assert.assertTrue(df.isLenient());
    }

    @Test
    public void testToStringEqualsHashCode() {
        StdDateFormat df1 = new StdDateFormat();
        Assert.assertEquals(df1, df1);
        Assert.assertNotEquals(df1, new StdDateFormat());
        Assert.assertNotEquals(df1, "not a StdDateFormat");
        Assert.assertEquals(System.identityHashCode(df1), df1.hashCode());

        String str1 = df1.toString();
        Assert.assertTrue(str1.contains("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat"));
        Assert.assertTrue(str1.contains("en_US"));

        StdDateFormat df2 = df1.withTimeZone(TimeZone.getTimeZone("UTC"));
        String str2 = df2.toString();
        Assert.assertTrue(str2.contains("timezone:"));
    }

    @Test
    public void testStaticGetters() {
        TimeZone tz = TimeZone.getTimeZone("GMT+3");
        DateFormat iso1 = StdDateFormat.getISO8601Format(tz);
        Assert.assertNotNull(iso1);
        Assert.assertEquals(tz, iso1.getTimeZone());

        DateFormat iso2 = StdDateFormat.getISO8601Format(tz, Locale.GERMANY);
        Assert.assertNotNull(iso2);

        DateFormat rfc1 = StdDateFormat.getRFC1123Format(tz);
        Assert.assertNotNull(rfc1);
        Assert.assertEquals(tz, rfc1.getTimeZone());

        DateFormat rfc2 = StdDateFormat.getRFC1123Format(tz, Locale.GERMANY);
        Assert.assertNotNull(rfc2);
    }

    @Test
    public void testFormat() {
        StdDateFormat df = StdDateFormat.instance.withTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(1589538030123L);
        String formatted = df.format(date);
        Assert.assertEquals("2020-05-15T10:20:30.123+0000", formatted);

        StringBuffer sb = new StringBuffer();
        df.format(date, sb, new FieldPosition(0));
        Assert.assertEquals("2020-05-15T10:20:30.123+0000", sb.toString());
    }

    @Test
    public void testLooksLikeISO8601() {
        StdDateFormat df = new StdDateFormat();
        Assert.assertFalse(df.looksLikeISO8601("202"));
        Assert.assertFalse(df.looksLikeISO8601("a020-05"));
        Assert.assertFalse(df.looksLikeISO8601("202a-05"));
        Assert.assertFalse(df.looksLikeISO8601("2020/05"));
        Assert.assertTrue(df.looksLikeISO8601("2020-05-15"));
    }

    @Test
    public void testParsePlainDate() throws Exception {
        StdDateFormat df = StdDateFormat.instance.withTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = df.parse("2020-05-15");
        Assert.assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        Assert.assertEquals(2020, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));

        Date date2 = df.parse("2020-05-15", new ParsePosition(0));
        Assert.assertEquals(date, date2);
    }

    @Test
    public void testParseISO8601Zulu() throws Exception {
        StdDateFormat df = StdDateFormat.instance;
        Date d1 = df.parse("2020-05-15T10:20:30.123Z");
        Assert.assertEquals(1589538030123L, d1.getTime());

        Date d2 = df.parse("2020-05-15T10:20:30Z");
        Assert.assertEquals(1589538030000L, d2.getTime());
    }

    @Test
    public void testParseISO8601WithTimezoneOffsets() throws Exception {
        StdDateFormat df = StdDateFormat.instance.withTimeZone(TimeZone.getTimeZone("UTC"));

        Date d1 = df.parse("2020-05-15T12:20:30.123+02:00");
        Assert.assertEquals(1589538030123L, d1.getTime());

        Date d2 = df.parse("2020-05-15T12:20:30.123+0200");
        Assert.assertEquals(1589538030123L, d2.getTime());

        Date d3 = df.parse("2020-05-15T12:20:30.123+02");
        Assert.assertEquals(1589538030123L, d3.getTime());

        Date d4 = df.parse("2020-05-15T12:20:30.12+0200");
        Assert.assertEquals(1589538030120L, d4.getTime());

        Date d5 = df.parse("2020-05-15T12:20:30.1+0200");
        Assert.assertEquals(1589538030100L, d5.getTime());

        Date d6 = df.parse("2020-05-15T12:20:30.+0200");
        Assert.assertEquals(1589538030000L, d6.getTime());

        Date d7 = df.parse("2020-05-15T12:20:30+0200");
        Assert.assertEquals(1589538030000L, d7.getTime());

        Date d8 = df.parse("2020-05-15T12:20+0200");
        Assert.assertEquals(1589538000000L, d8.getTime());

        Date d9 = df.parse("2020-05-15T08:20:30.123-02:00");
        Assert.assertEquals(1589538030123L, d9.getTime());
    }

    @Test
    public void testParseISO8601WithoutTimezone() throws Exception {
        StdDateFormat df = StdDateFormat.instance.withTimeZone(TimeZone.getTimeZone("UTC"));

        Date d1 = df.parse("2020-05-15T10:20:30.123");
        Assert.assertEquals(1589538030123L, d1.getTime());

        Date d2 = df.parse("2020-05-15T10:20:30.12");
        Assert.assertEquals(1589538030120L, d2.getTime());

        Date d3 = df.parse("2020-05-15T10:20:30.1");
        Assert.assertEquals(1589538030100L, d3.getTime());

        Date d4 = df.parse("2020-05-15T10:20:30.");
        Assert.assertEquals(1589538030000L, d4.getTime());

        Date d5 = df.parse("2020-05-15T10:20:30");
        Assert.assertEquals(1589538030000L, d5.getTime());
    }

    @Test
    public void testParseRFC1123() throws Exception {
        StdDateFormat df = StdDateFormat.instance.withTimeZone(TimeZone.getTimeZone("UTC"));
        Date d1 = df.parse("Fri, 15 May 2020 10:20:30 GMT");
        Assert.assertEquals(1589538030000L, d1.getTime());

        ParsePosition pos = new ParsePosition(0);
        Date d2 = df.parse("Fri, 15 May 2020 10:20:30 GMT", pos);
        Assert.assertEquals(1589538030000L, d2.getTime());
    }

    @Test
    public void testParseNumericTimestamps() throws Exception {
        StdDateFormat df = new StdDateFormat();

        Date d1 = df.parse("1589538030123");
        Assert.assertEquals(1589538030123L, d1.getTime());

        Date d2 = df.parse("-1589538030123");
        Assert.assertEquals(-1589538030123L, d2.getTime());

        Date d3 = df.parse("   123456   ");
        Assert.assertEquals(123456L, d3.getTime());

        ParsePosition pos = new ParsePosition(0);
        Date d4 = df.parse("1589538030123", pos);
        Assert.assertEquals(1589538030123L, d4.getTime());

        Date d5 = df.parse("-1589538030123", new ParsePosition(0));
        Assert.assertEquals(-1589538030123L, d5.getTime());
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidDateThrowsException() throws Exception {
        StdDateFormat df = new StdDateFormat();
        df.parse("not-a-valid-date");
    }

    @Test
    public void testParseWithPositionFailure() {
        StdDateFormat df = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("not-a-valid-date", pos);
        Assert.assertNull(d);

        ParsePosition pos2 = new ParsePosition(0);
        Date d2 = df.parse("2020-99-99T99:99:99", pos2);
        Assert.assertNull(d2);
    }

    @Test(expected = ParseException.class)
    public void testNonLenientParsingFailure() throws Exception {
        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, Boolean.FALSE);
        df.parse("2020-02-31T10:20:30.000Z");
    }

    @Test
    public void testClearFormats() {
        StdDateFormat df = new StdDateFormat();
        df.format(new Date());
        df.parseAsRFC1123("Fri, 15 May 2020 10:20:30 GMT", new ParsePosition(0));
        df._clearFormats();
        Assert.assertNull(df._formatISO8601);
        Assert.assertNull(df._formatRFC1123);
        Assert.assertNull(df._formatISO8601_z);
        Assert.assertNull(df._formatPlain);
    }
}
