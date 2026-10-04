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
        StdDateFormat std = new StdDateFormat();
        Assert.assertNull(std.getTimeZone());
        Assert.assertTrue(std.isLenient());

        StdDateFormat std2 = new StdDateFormat(TimeZone.getTimeZone("GMT+2"), Locale.GERMANY);
        Assert.assertEquals(TimeZone.getTimeZone("GMT+2"), std2.getTimeZone());

        StdDateFormat std3 = new StdDateFormat(TimeZone.getTimeZone("GMT+1"), Locale.FRANCE, Boolean.FALSE);
        Assert.assertEquals(TimeZone.getTimeZone("GMT+1"), std3.getTimeZone());
        Assert.assertFalse(std3.isLenient());
    }

    @Test
    public void testGetDefaultTimeZone() {
        TimeZone tz = StdDateFormat.getDefaultTimeZone();
        Assert.assertEquals("UTC", tz.getID());
    }

    @Test
    public void testWithTimeZone() {
        StdDateFormat std = new StdDateFormat();
        StdDateFormat customTz = std.withTimeZone(TimeZone.getTimeZone("PST"));
        Assert.assertEquals(TimeZone.getTimeZone("PST"), customTz.getTimeZone());

        StdDateFormat sameTz = customTz.withTimeZone(TimeZone.getTimeZone("PST"));
        Assert.assertSame(customTz, sameTz);

        StdDateFormat nullTz = customTz.withTimeZone(null);
        Assert.assertEquals(StdDateFormat.getDefaultTimeZone(), nullTz.getTimeZone());
    }

    @Test
    public void testWithLocale() {
        StdDateFormat std = new StdDateFormat();
        StdDateFormat customLoc = std.withLocale(Locale.GERMAN);
        Assert.assertNotSame(std, customLoc);

        StdDateFormat sameLoc = customLoc.withLocale(Locale.GERMAN);
        Assert.assertSame(customLoc, sameLoc);
    }

    @Test
    public void testClone() {
        StdDateFormat std = new StdDateFormat(TimeZone.getTimeZone("GMT+3"), Locale.ITALY, Boolean.FALSE);
        StdDateFormat cloned = std.clone();
        Assert.assertNotSame(std, cloned);
        Assert.assertEquals(std.getTimeZone(), cloned.getTimeZone());
        Assert.assertEquals(std.isLenient(), cloned.isLenient());
    }

    @Test
    public void testStaticGetFormatMethods() {
        DateFormat df1 = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("GMT"));
        Assert.assertNotNull(df1);
        DateFormat df2 = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("GMT"), Locale.GERMAN);
        Assert.assertNotNull(df2);

        DateFormat df3 = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("GMT"));
        Assert.assertNotNull(df3);
        DateFormat df4 = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("GMT"), Locale.GERMAN);
        Assert.assertNotNull(df4);
    }

    @Test
    public void testSetTimeZone() {
        StdDateFormat std = new StdDateFormat();
        std.format(new Date());
        std.setTimeZone(TimeZone.getTimeZone("EST"));
        Assert.assertEquals(TimeZone.getTimeZone("EST"), std.getTimeZone());
        
        std.setTimeZone(TimeZone.getTimeZone("EST"));
        Assert.assertEquals(TimeZone.getTimeZone("EST"), std.getTimeZone());
    }

    @Test
    public void testToString() {
        StdDateFormat std = new StdDateFormat();
        String str = std.toString();
        Assert.assertTrue(str.contains("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat"));

        StdDateFormat custom = std.withTimeZone(TimeZone.getTimeZone("UTC"));
        String strCustom = custom.toString();
        Assert.assertTrue(strCustom.contains("timezone:"));
    }

    @Test
    public void testParsePlainDate() throws ParseException {
        StdDateFormat std = new StdDateFormat();
        Date d = std.parse("2020-05-12");
        Assert.assertNotNull(d);

        ParsePosition pos = new ParsePosition(0);
        Date d2 = std.parse("2020-05-12", pos);
        Assert.assertNotNull(d2);
        Assert.assertEquals(d, d2);
    }

    @Test
    public void testParseISO8601Zulu() throws ParseException {
        StdDateFormat std = new StdDateFormat();
        Date d1 = std.parse("2020-05-12T10:15:30.123Z");
        Assert.assertNotNull(d1);

        Date d2 = std.parse("2020-05-12T10:15:30Z");
        Assert.assertNotNull(d2);

        ParsePosition pos = new ParsePosition(0);
        Date d3 = std.parse("2020-05-12T10:15:30.123Z", pos);
        Assert.assertEquals(d1, d3);
    }

    @Test
    public void testParseISO8601WithTimezones() throws ParseException {
        StdDateFormat std = new StdDateFormat();

        Date d1 = std.parse("2020-05-12T10:15:30.123+02:00");
        Assert.assertNotNull(d1);

        Date d2 = std.parse("2020-05-12T10:15:30.123+0200");
        Assert.assertNotNull(d2);
        Assert.assertEquals(d1, d2);

        Date d3 = std.parse("2020-05-12T10:15:30.123+02");
        Assert.assertNotNull(d3);
        Assert.assertEquals(d1, d3);

        Date d4 = std.parse("2020-05-12T10:15:30.12-05:00");
        Assert.assertNotNull(d4);

        Date d5 = std.parse("2020-05-12T10:15:30.1-05:00");
        Assert.assertNotNull(d5);

        Date d6 = std.parse("2020-05-12T10:15:30.-05:00");
        Assert.assertNotNull(d6);

        Date d7 = std.parse("2020-05-12T10:15:30-05:00");
        Assert.assertNotNull(d7);

        Date d8 = std.parse("2020-05-12T10:15:3-05:00");
        Assert.assertNotNull(d8);

        Date d9 = std.parse("2020-05-12T10:15-05:00");
        Assert.assertNotNull(d9);

        Date d10 = std.parse("2020-05-12T10:1-05:00");
        Assert.assertNotNull(d10);
    }

    @Test
    public void testParseISO8601NoTimezone() throws ParseException {
        StdDateFormat std = new StdDateFormat();

        Date d1 = std.parse("2020-05-12T10:15:30.123");
        Assert.assertNotNull(d1);

        Date d2 = std.parse("2020-05-12T10:15:30.12");
        Assert.assertNotNull(d2);

        Date d3 = std.parse("2020-05-12T10:15:30.1");
        Assert.assertNotNull(d3);

        Date d4 = std.parse("2020-05-12T10:15:30");
        Assert.assertNotNull(d4);
    }

    @Test
    public void testParseTimestampNumeric() throws ParseException {
        StdDateFormat std = new StdDateFormat();
        Date now = new Date(1589278530000L);

        Date d1 = std.parse("1589278530000");
        Assert.assertEquals(now, d1);

        Date d2 = std.parse("-1589278530000");
        Assert.assertEquals(new Date(-1589278530000L), d2);

        ParsePosition pos = new ParsePosition(0);
        Date d3 = std.parse("1589278530000", pos);
        Assert.assertEquals(now, d3);

        ParsePosition posNeg = new ParsePosition(0);
        Date d4 = std.parse("-1589278530000", posNeg);
        Assert.assertEquals(new Date(-1589278530000L), d4);
    }

    @Test
    public void testParseRFC1123() throws ParseException {
        StdDateFormat std = new StdDateFormat();
        String rfcStr = "Tue, 12 May 2020 10:15:30 GMT";
        Date d = std.parse(rfcStr);
        Assert.assertNotNull(d);

        ParsePosition pos = new ParsePosition(0);
        Date d2 = std.parse(rfcStr, pos);
        Assert.assertEquals(d, d2);
    }

    @Test
    public void testFormat() {
        StdDateFormat std = new StdDateFormat().withTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2020, Calendar.MAY, 12, 10, 15, 30);
        cal.set(Calendar.MILLISECOND, 123);
        String formatted = std.format(cal.getTime());
        Assert.assertEquals("2020-05-12T10:15:30.123+0000", formatted);

        StringBuffer sb = new StringBuffer();
        StringBuffer res = std.format(cal.getTime(), sb, new FieldPosition(0));
        Assert.assertEquals("2020-05-12T10:15:30.123+0000", res.toString());
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidDateThrowsException() throws ParseException {
        StdDateFormat std = new StdDateFormat();
        std.parse("invalid-date-string");
    }

    @Test(expected = ParseException.class)
    public void testParseNonLenientThrowsExceptionOnInvalid() throws ParseException {
        StdDateFormat std = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, Boolean.FALSE);
        std.parse("2020-02-31T10:15:30.000Z");
    }

    @Test
    public void testParsePosReturnsNullOnInvalidIso() {
        StdDateFormat std = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, Boolean.FALSE);
        ParsePosition pos = new ParsePosition(0);
        Date d = std.parse("2020-02-31T10:15:30.000Z", pos);
        Assert.assertNull(d);
    }

    @Test
    public void testLooksLikeISO8601() {
        StdDateFormat std = new StdDateFormat();
        Assert.assertTrue(std.looksLikeISO8601("2020-01-01"));
        Assert.assertFalse(std.looksLikeISO8601("202-01-01"));
        Assert.assertFalse(std.looksLikeISO8601("2020/01/01"));
        Assert.assertFalse(std.looksLikeISO8601("abcd-ef-gh"));
    }
}
