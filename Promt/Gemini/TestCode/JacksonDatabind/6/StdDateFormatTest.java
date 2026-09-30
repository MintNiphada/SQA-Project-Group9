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
    public void testConstructorsAndDefaults() {
        Assert.assertNotNull(StdDateFormat.instance);
        Assert.assertEquals(TimeZone.getTimeZone("GMT"), StdDateFormat.getDefaultTimeZone());

        StdDateFormat dfDefault = new StdDateFormat();
        Assert.assertNotNull(dfDefault);

        TimeZone tzPST = TimeZone.getTimeZone("PST");
        StdDateFormat dfWithTz = new StdDateFormat(tzPST);
        Assert.assertEquals("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat (timezone: " + tzPST + ")(locale: en_US)", dfWithTz.toString());

        StdDateFormat dfWithTzAndLoc = new StdDateFormat(tzPST, Locale.GERMANY);
        Assert.assertEquals("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat (timezone: " + tzPST + ")(locale: de_DE)", dfWithTzAndLoc.toString());
    }

    @Test
    public void testWithTimeZone() {
        TimeZone tzGMT = TimeZone.getTimeZone("GMT");
        TimeZone tzEST = TimeZone.getTimeZone("EST");

        StdDateFormat df = new StdDateFormat(tzGMT, Locale.US);
        
        // Same timezone returns same instance
        StdDateFormat sameDf = df.withTimeZone(tzGMT);
        Assert.assertSame(df, sameDf);

        // Null timezone defaults to GMT
        StdDateFormat nullTzDf = df.withTimeZone(null);
        Assert.assertSame(df, nullTzDf);

        // Different timezone returns new instance
        StdDateFormat differentDf = df.withTimeZone(tzEST);
        Assert.assertNotSame(df, differentDf);

        StdDateFormat defaultInst = new StdDateFormat();
        StdDateFormat updatedTz = defaultInst.withTimeZone(tzEST);
        Assert.assertNotSame(defaultInst, updatedTz);
    }

    @Test
    public void testWithLocale() {
        Locale locUS = Locale.US;
        Locale locFR = Locale.FRANCE;

        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("GMT"), locUS);
        
        // Same locale returns same instance
        StdDateFormat sameDf = df.withLocale(locUS);
        Assert.assertSame(df, sameDf);

        // Different locale returns new instance
        StdDateFormat differentDf = df.withLocale(locFR);
        Assert.assertNotSame(df, differentDf);
    }

    @Test
    public void testClone() {
        TimeZone tzEST = TimeZone.getTimeZone("EST");
        StdDateFormat df = new StdDateFormat(tzEST, Locale.GERMAN);
        StdDateFormat cloned = df.clone();

        Assert.assertNotSame(df, cloned);
        Assert.assertEquals(df.toString(), cloned.toString());
    }

    @Test
    public void testDeprecatedAndStaticBlueprintMethods() {
        Assert.assertNotNull(StdDateFormat.getBlueprintISO8601Format());
        Assert.assertNotNull(StdDateFormat.getBlueprintRFC1123Format());

        TimeZone tz = TimeZone.getTimeZone("PST");
        DateFormat iso1 = StdDateFormat.getISO8601Format(tz);
        Assert.assertNotNull(iso1);
        Assert.assertEquals(tz, iso1.getTimeZone());

        DateFormat iso2 = StdDateFormat.getISO8601Format(tz, Locale.FRANCE);
        Assert.assertNotNull(iso2);
        Assert.assertEquals(tz, iso2.getTimeZone());

        DateFormat iso3 = StdDateFormat.getISO8601Format(null, Locale.FRANCE);
        Assert.assertNotNull(iso3);
        Assert.assertEquals(StdDateFormat.getDefaultTimeZone(), iso3.getTimeZone());

        DateFormat rfc1 = StdDateFormat.getRFC1123Format(tz);
        Assert.assertNotNull(rfc1);
        Assert.assertEquals(tz, rfc1.getTimeZone());

        DateFormat rfc2 = StdDateFormat.getRFC1123Format(tz, Locale.GERMANY);
        Assert.assertNotNull(rfc2);
        Assert.assertEquals(tz, rfc2.getTimeZone());
    }

    @Test
    public void testSetTimeZone() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("PST");

        StdDateFormat df = new StdDateFormat(tz1, Locale.US);
        
        // Trigger lazy initialization of internal formats
        df.format(new Date(0L));

        // Setting a different timezone resets internal formats
        df.setTimeZone(tz2);
        Assert.assertTrue(df.toString().contains("PST"));

        // Setting the exact same timezone does not alter anything
        df.setTimeZone(tz2);
        Assert.assertTrue(df.toString().contains("PST"));
    }

    @Test
    public void testFormatDate() {
        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.US);
        Date date = new Date(0L); // 1970-01-01T00:00:00.000+0000

        StringBuffer sb = new StringBuffer();
        df.format(date, sb, new FieldPosition(0));
        Assert.assertEquals("1970-01-01T00:00:00.000+0000", sb.toString());
    }

    @Test
    public void testParsePlainDate() throws Exception {
        Date date = stdDateFormat.parse("2021-05-18");
        Assert.assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseISO8601Zulu() throws Exception {
        // Without milliseconds
        Date date1 = stdDateFormat.parse("2021-05-18T12:30:45Z");
        Assert.assertNotNull(date1);

        // With milliseconds
        Date date2 = stdDateFormat.parse("2021-05-18T12:30:45.123Z");
        Assert.assertNotNull(date2);
        Assert.assertEquals(date1.getTime() + 123L, date2.getTime());
    }

    @Test
    public void testParseISO8601WithColonTimezone() throws Exception {
        // "+00:00" timezone offset
        Date date1 = stdDateFormat.parse("2021-05-18T12:30:45.123+00:00");
        Date date2 = stdDateFormat.parse("2021-05-18T12:30:45.123+0000");
        Assert.assertEquals(date1, date2);

        // Missing milliseconds with colon timezone: len-9 digit check
        Date date3 = stdDateFormat.parse("2021-05-18T12:30:45+02:00");
        Assert.assertNotNull(date3);
    }

    @Test
    public void testParseISO8601WithShortTimezone() throws Exception {
        // Timezone with 2 digits (+02)
        Date date1 = stdDateFormat.parse("2021-05-18T12:30:45.123+02");
        Date date2 = stdDateFormat.parse("2021-05-18T12:30:45.123+0200");
        Assert.assertEquals(date1, date2);

        Date date3 = stdDateFormat.parse("2021-05-18T12:30:45.123-05");
        Date date4 = stdDateFormat.parse("2021-05-18T12:30:45.123-0500");
        Assert.assertEquals(date3, date4);
    }

    @Test
    public void testParseISO8601WithoutTimezone() throws Exception {
        // Plain date-time without timezone and without millis: timeLen <= 8
        Date date1 = stdDateFormat.parse("2021-05-18T12:30:45");
        Date date2 = stdDateFormat.parse("2021-05-18T12:30:45.000Z");
        Assert.assertEquals(date2, date1);

        // Plain date-time without timezone with millis: timeLen > 8
        Date date3 = stdDateFormat.parse("2021-05-18T12:30:45.500");
        Date date4 = stdDateFormat.parse("2021-05-18T12:30:45.500Z");
        Assert.assertEquals(date4, date3);
    }

    @Test
    public void testParseNumericTimestamp() throws Exception {
        long now = 1621340000000L;
        Date datePositive = stdDateFormat.parse(String.valueOf(now));
        Assert.assertEquals(now, datePositive.getTime());

        long negativeTimestamp = -500000L;
        Date dateNegative = stdDateFormat.parse(String.valueOf(negativeTimestamp));
        Assert.assertEquals(negativeTimestamp, dateNegative.getTime());
    }

    @Test
    public void testParseRFC1123() throws Exception {
        String rfcStr = "Tue, 18 May 2021 12:30:45 GMT";
        Date date = stdDateFormat.parse(rfcStr);
        Assert.assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(45, cal.get(Calendar.SECOND));
    }

    @Test
    public void testParseInvalidDateThrowsParseException() {
        try {
            stdDateFormat.parse("not-a-valid-date-string");
            Assert.fail("Expected ParseException");
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains("Can not parse date \"not-a-valid-date-string\""));
            Assert.assertTrue(e.getMessage().contains("not compatible with any of standard forms"));
        }
    }

    @Test
    public void testLooksLikeISO8601() {
        Assert.assertTrue(stdDateFormat.looksLikeISO8601("2021-05-18"));
        Assert.assertTrue(stdDateFormat.looksLikeISO8601("2021-05-18T10:00:00Z"));

        // Length < 5
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("202"));
        // Char 0 not digit
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("a021-05"));
        // Char 3 not digit
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("202a-05"));
        // Char 4 not '-'
        Assert.assertFalse(stdDateFormat.looksLikeISO8601("2021/05"));
    }

    @Test
    public void testParseWithNonDefaultLocale() throws Exception {
        StdDateFormat dfFR = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.FRANCE);
        Date date = dfFR.parse("2021-05-18");
        Assert.assertNotNull(date);

        Date rfcDate = dfFR.parse("mar., 18 mai 2021 12:30:45 GMT");
        Assert.assertNotNull(rfcDate);
    }

    @Test
    public void testNumericBoundaryAndNonNumericBranch() {
        ParsePosition pos = new ParsePosition(0);
        
        // String that has non-digit in the middle of digits (e.g. "123a456")
        Date res1 = stdDateFormat.parse("123a456", pos);
        Assert.assertNull(res1);

        // String with negative sign not at index 0 (e.g. "12-34")
        pos.setIndex(0);
        Date res2 = stdDateFormat.parse("12-34", pos);
        Assert.assertNull(res2);
    }
}
