package com.google.gson.internal.bind.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class ISO8601UtilsTest {

    @Test
    public void testConstructor() {
        ISO8601Utils utils = new ISO8601Utils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testFormatDefaultUtc() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.set(2023, Calendar.MAY, 17, 14, 30, 45);
        calendar.set(Calendar.MILLISECOND, 123);
        Date date = calendar.getTime();

        String formatted = ISO8601Utils.format(date);
        Assert.assertEquals("2023-05-17T14:30:45Z", formatted);
    }

    @Test
    public void testFormatWithMillis() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.set(2023, Calendar.JANUARY, 5, 8, 9, 3);
        calendar.set(Calendar.MILLISECOND, 7);
        Date date = calendar.getTime();

        String formatted = ISO8601Utils.format(date, true);
        Assert.assertEquals("2023-01-05T08:09:03.007Z", formatted);
    }

    @Test
    public void testFormatWithCustomTimeZonePositiveOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date date = calendar.getTime();

        String formatted = ISO8601Utils.format(date, true, tz);
        Assert.assertEquals("2023-12-31T23:59:59.999+02:00", formatted);
    }

    @Test
    public void testFormatWithCustomTimeZoneNegativeOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(2023, Calendar.JULY, 4, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();

        String formatted = ISO8601Utils.format(date, false, tz);
        Assert.assertEquals("2023-07-04T12:00:00-05:30", formatted);
    }

    @Test
    public void testFormatWithZeroOffsetNonUtc() {
        TimeZone tz = new SimpleTimeZone(0, "CustomZero");
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(2023, Calendar.AUGUST, 15, 10, 20, 30);
        Date date = calendar.getTime();

        String formatted = ISO8601Utils.format(date, false, tz);
        Assert.assertEquals("2023-08-15T10:20:30Z", formatted);
    }

    @Test
    public void testParseDateOnlyWithHyphens() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17", pos);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Assert.assertEquals(2023, calendar.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(10, pos.getIndex());
    }

    @Test
    public void testParseDateOnlyWithoutHyphens() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("20230517", pos);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Assert.assertEquals(2023, calendar.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(8, pos.getIndex());
    }

    @Test
    public void testParseFullIsoUtc() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(2023, calendar.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(45, calendar.get(Calendar.SECOND));
        Assert.assertEquals(0, calendar.get(Calendar.MILLISECOND));
        Assert.assertEquals("2023-05-17T14:30:45Z".length(), pos.getIndex());
    }

    @Test
    public void testParseFullIsoNoSeparators() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("20230517T143045Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(2023, calendar.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(45, calendar.get(Calendar.SECOND));
        Assert.assertEquals("20230517T143045Z".length(), pos.getIndex());
    }

    @Test
    public void testParseTimeWithoutSeconds() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(0, calendar.get(Calendar.SECOND));
    }

    @Test
    public void testParseWith1DigitMillis() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45.5Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(500, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseWith2DigitMillis() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45.56Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(560, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseWith3DigitMillis() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45.567Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(567, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseWithMoreThan3DigitMillis() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45.56789Z", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Assert.assertEquals(567, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseLeapSeconds() throws ParseException {
        ParsePosition pos60 = new ParsePosition(0);
        Date date60 = ISO8601Utils.parse("2023-05-17T14:30:60Z", pos60);
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date60);
        Assert.assertEquals(59, calendar.get(Calendar.SECOND));

        ParsePosition pos61 = new ParsePosition(0);
        Date date61 = ISO8601Utils.parse("2023-05-17T14:30:61Z", pos61);
        calendar.setTime(date61);
        Assert.assertEquals(59, calendar.get(Calendar.SECOND));

        ParsePosition pos62 = new ParsePosition(0);
        Date date62 = ISO8601Utils.parse("2023-05-17T14:30:62Z", pos62);
        calendar.setTime(date62);
        Assert.assertEquals(59, calendar.get(Calendar.SECOND));
    }

    @Test
    public void testParsePositiveZeroOffsetFormats() throws ParseException {
        ParsePosition pos1 = new ParsePosition(0);
        Date date1 = ISO8601Utils.parse("2023-05-17T14:30:45+0000", pos1);
        Assert.assertNotNull(date1);

        ParsePosition pos2 = new ParsePosition(0);
        Date date2 = ISO8601Utils.parse("2023-05-17T14:30:45+00:00", pos2);
        Assert.assertEquals(date1, date2);
    }

    @Test
    public void testParsePositiveOffsetWithColon() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45+02:00", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+02:00"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(45, calendar.get(Calendar.SECOND));
    }

    @Test
    public void testParsePositiveOffsetWithoutColon() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45+0200", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+02:00"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseNegativeOffsetWithColon() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45-05:00", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT-05:00"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseNegativeOffsetWithoutColon() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30:45-0500", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT-05:00"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseWithNonZeroInitialParsePosition() throws ParseException {
        String prefix = "PREFIX_DATA:";
        String fullString = prefix + "2023-05-17T14:30:45Z";
        ParsePosition pos = new ParsePosition(prefix.length());

        Date date = ISO8601Utils.parse(fullString, pos);
        Assert.assertNotNull(date);
        Assert.assertEquals(fullString.length(), pos.getIndex());
    }

    @Test
    public void testParseMissingTimeZoneIndicator() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-05-17T14:30:45", pos);
            Assert.fail("Expected ParseException for missing timezone indicator");
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains("No time zone indicator"));
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
            Assert.assertEquals(0, e.getErrorOffset());
        }
    }

    @Test
    public void testParseInvalidTimeZoneIndicator() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-05-17T14:30:45X", pos);
            Assert.fail("Expected ParseException for invalid timezone indicator");
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid time zone indicator 'X'"));
            Assert.assertTrue(e.getCause() instanceof IndexOutOfBoundsException);
        }
    }

    @Test
    public void testParseMismatchingTimeZoneOffset() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-05-17T14:30:45+99:99", pos);
            Assert.fail("Expected ParseException for mismatching timezone indicator");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IndexOutOfBoundsException);
            Assert.assertTrue(e.getMessage().contains("Mismatching time zone indicator"));
        }
    }

    @Test
    public void testParseNonNumericCharacters() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("202A-05-17T14:30:45Z", pos);
            Assert.fail("Expected ParseException for non-numeric characters");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof NumberFormatException);
            Assert.assertTrue(e.getMessage().contains("Invalid number"));
        }
    }

    @Test
    public void testParseNonDigitAtFirstPosition() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("YYYY-05-17T14:30:45Z", pos);
            Assert.fail("Expected ParseException for non-digit");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testParseStringTooShort() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("202", pos);
            Assert.fail("Expected ParseException for short string");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testParseEmptyString() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("", pos);
            Assert.fail("Expected ParseException for empty string");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testParseNullString() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse(null, pos);
            Assert.fail("Expected ParseException for null string");
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to parse date [null]"));
        }
    }

    @Test
    public void testParseInvalidDateStrictCheck() {
        ParsePosition pos = new ParsePosition(0);
        try {
            // Day 32 of January is invalid in non-lenient calendar
            ISO8601Utils.parse("2023-01-32T14:30:45Z", pos);
            Assert.fail("Expected ParseException for non-lenient calendar error");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseInvalidMonthStrictCheck() {
        ParsePosition pos = new ParsePosition(0);
        try {
            // Month 13 is invalid
            ISO8601Utils.parse("2023-13-01T14:30:45Z", pos);
            Assert.fail("Expected ParseException for invalid month");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseInvalidHourStrictCheck() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-01-01T25:00:00Z", pos);
            Assert.fail("Expected ParseException for invalid hour");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseInvalidMinuteStrictCheck() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-01-01T00:65:00Z", pos);
            Assert.fail("Expected ParseException for invalid minute");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseInvalidSecondStrictCheck() {
        ParsePosition pos = new ParsePosition(0);
        try {
            ISO8601Utils.parse("2023-01-01T00:00:65Z", pos);
            Assert.fail("Expected ParseException for invalid second");
        } catch (ParseException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseTimeOnlyWithoutSecondsAndWithOffset() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse("2023-05-17T14:30+02:00", pos);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+02:00"));
        calendar.setTime(date);
        Assert.assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(0, calendar.get(Calendar.SECOND));
    }
}
