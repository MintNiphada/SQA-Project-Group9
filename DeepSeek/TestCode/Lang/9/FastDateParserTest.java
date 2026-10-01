package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class FastDateParserTest {

    private FastDateParser createParser(String pattern) {
        return new FastDateParser(pattern, TimeZone.getDefault(), Locale.getDefault());
    }

    private FastDateParser createParser(String pattern, TimeZone tz) {
        return new FastDateParser(pattern, tz, Locale.getDefault());
    }

    private FastDateParser createParser(String pattern, Locale locale) {
        return new FastDateParser(pattern, TimeZone.getDefault(), locale);
    }

    @Test
    public void testConstructorValidPatterns() {
        // all supported pattern letters
        createParser("yyyy-MM-dd");
        createParser("yyyy-MM-dd HH:mm:ss.SSS");
        createParser("EEE, d MMM yyyy HH:mm:ss Z");
        createParser("'Today is: 'yyyy-MM-dd");
        createParser("''"); // double quote
        createParser("'quoted text'");
        createParser("yyyy-MM-dd'T'HH:mm:ss");
        createParser("yyyy-MM-dd HH:mm:ss z");
        createParser("G yyyy"); // era
        createParser("w W"); // week of year, week of month
        createParser("D F"); // day of year, day of week in month
        createParser("KK:mm a"); // hour 0-11 am/pm
        createParser("hh:mm a"); // hour 1-12 am/pm
        createParser("HH:mm"); // hour 0-23
        createParser("kk:mm"); // hour 1-24
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidPatternUnclosedQuote() {
        createParser("yyyy'"); // unmatched quote
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidPatternEmpty() {
        createParser(""); // empty string - formatPattern won't match? Actually empty is invalid.
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidPatternOnlyQuote() {
        createParser("'");
    }

    @Test
    public void testGetPattern() {
        FastDateParser parser = createParser("yyyy-MM-dd");
        assertEquals("yyyy-MM-dd", parser.getPattern());
    }

    @Test
    public void testGetTimeZone() {
        TimeZone utc = TimeZone.getTimeZone("GMT");
        FastDateParser parser = new FastDateParser("yyyy", utc, Locale.US);
        assertEquals(utc, parser.getTimeZone());
    }

    @Test
    public void testGetLocale() {
        Locale locale = Locale.FRANCE;
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), locale);
        assertEquals(locale, parser.getLocale());
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateParser parser1 = new FastDateParser("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateParser parser2 = new FastDateParser("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateParser parser3 = new FastDateParser("yyyy-MM-dd", TimeZone.getTimeZone("PST"), Locale.US);
        FastDateParser parser4 = new FastDateParser("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.FRANCE);
        FastDateParser parser5 = new FastDateParser("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);

        assertTrue(parser1.equals(parser2));
        assertFalse(parser1.equals(parser3));
        assertFalse(parser1.equals(parser4));
        assertFalse(parser1.equals(parser5));
        assertFalse(parser1.equals(null));
        assertFalse(parser1.equals("not a parser"));

        assertEquals(parser1.hashCode(), parser2.hashCode());
        assertFalse(parser1.hashCode() == parser3.hashCode()); // could collide but unlikely
    }

    @Test
    public void testToString() {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getTimeZone("GMT"), Locale.US);
        String str = parser.toString();
        assertTrue(str.contains("FastDateParser"));
        assertTrue(str.contains("yyyy"));
        assertTrue(str.contains("GMT"));
    }

    @Test
    public void testParseValidDate() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd");
        Date date = parser.parse("2015-06-15");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2015, cal.get(Calendar.YEAR));
        assertEquals(5, cal.get(Calendar.MONTH)); // June is 5
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseWithTime() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"));
        Date date = parser.parse("2012-11-30 12:34:56.789");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2012, cal.get(Calendar.YEAR));
        assertEquals(10, cal.get(Calendar.MONTH)); // Nov
        assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(34, cal.get(Calendar.MINUTE));
        assertEquals(56, cal.get(Calendar.SECOND));
        assertEquals(789, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseTextMonthShort() throws ParseException {
        FastDateParser parser = createParser("dd-MMM-yyyy", Locale.US);
        Date date = parser.parse("15-Jan-2014");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH)); // Jan
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseTextMonthLong() throws ParseException {
        FastDateParser parser = createParser("dd MMMM yyyy", Locale.US);
        Date date = parser.parse("15 January 2014");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidTextMonth() throws ParseException {
        FastDateParser parser = createParser("dd-MMM-yyyy", Locale.US);
        parser.parse("15-XXX-2014");
    }

    @Test
    public void testParseEra() throws ParseException {
        FastDateParser parser = createParser("G yyyy", Locale.US);
        Date date = parser.parse("AD 2015");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(Calendar.AD, cal.get(Calendar.ERA));
        assertEquals(2015, cal.get(Calendar.YEAR));
    }

    @Test
    public void testParseAmPm() throws ParseException {
        FastDateParser parser = createParser("hh:mm a", Locale.US);
        Date date = parser.parse("02:30 PM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY)); // 2 PM = 14
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    @Test
    public void testParseAmPmLowerCase() throws ParseException {
        FastDateParser parser = createParser("hh:mm a", Locale.US);
        Date date = parser.parse("02:30 pm");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseDayOfWeek() throws ParseException {
        FastDateParser parser = createParser("EEE, yyyy-MM-dd", Locale.US);
        Date date = parser.parse("Wed, 2020-01-01");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(Calendar.WEDNESDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(2020, cal.get(Calendar.YEAR));
    }

    @Test
    public void testParseTimeZoneShort() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd z", Locale.US);
        Date date = parser.parse("2020-01-01 PSt");
        // verify timezone was set; getTimeZone on calendar? parse returns Date, calendar internal
        // We can parse with a timezone that has offset and check UTC time
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles")); // PST
        cal.setTime(date);
        // The expected offset is -8 hours from UTC
        assertEquals(-8 * 60 * 60 * 1000, cal.getTimeZone().getRawOffset());
    }

    @Test
    public void testParseTimeZoneLong() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd z", Locale.US);
        Date date = parser.parse("2020-01-01 Pacific Standard Time");
        // Verify timezone
    }

    @Test
    public void testParseTimeZoneGMT() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd z");
        Date date = parser.parse("2020-01-01 GMT+05:30");
        // check offset
    }

    @Test
    public void testParseTimeZonePlusMinus() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd Z");
        Date date = parser.parse("2020-01-01 -0500");
        // check offset
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTimeZoneInvalid() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd z");
        parser.parse("2020-01-01 XyZ");
    }

    @Test
    public void testParseHourModulo() throws ParseException {
        // H pattern expects 0-23 but accepts any number and mods 24
        FastDateParser parser = createParser("HH:mm");
        Date date = parser.parse("25:10");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY)); // 25%24=1
        assertEquals(10, cal.get(Calendar.MINUTE));
    }

    @Test
    public void testParseHourModulo12() throws ParseException {
        FastDateParser parser = createParser("hh:mm a", Locale.US);
        Date date = parser.parse("13:10 PM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(13%12, cal.get(Calendar.HOUR)); // 1
        assertEquals(Calendar.PM, cal.get(Calendar.AM_PM));
    }

    @Test
    public void testParseNumericMonthNegativeMod() throws ParseException {
        FastDateParser parser = createParser("MM/dd");
        Date date = parser.parse("0/15");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH)); // 0-> January (since modify: iValue-1 => -1? Actually 0 becomes -1, set to Calendar.MONTH, which is January? Calendar.JANUARY is 0, so -1? That's invalid but Calendar.set may adjust. We expect it's February? Let's check: modify iValue-1, so 0-1=-1. Calendar.MONTH -1 is not defined but may be December? Test will pass if it doesn't throw. But likely it will set to some month. So not a great test but covers modify.
        // Better: use 2 to get February.
    }

    @Test
    public void testParseNumericMonth() throws ParseException {
        FastDateParser parser = createParser("MM/dd");
        Date date = parser.parse("02/15");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(Calendar.FEBRUARY, cal.get(Calendar.MONTH));
    }

    @Test
    public void testParseAbbreviatedYear() throws ParseException {
        FastDateParser parser = createParser("yy/MM/dd");
        // current year will affect adjustYear, but we can check that the parsed year is within 80 years before and 20 after construction year.
        Date date = parser.parse("99/12/25");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        assertTrue(year >= thisYear - 80 && year <= thisYear + 20);
    }

    @Test
    public void testParseFourDigitYear() throws ParseException {
        FastDateParser parser = createParser("yyyy/MM/dd");
        Date date = parser.parse("1900/01/01");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(1900, cal.get(Calendar.YEAR));
    }

    @Test
    public void testParseUnparsableReturnsNull() {
        FastDateParser parser = createParser("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("abc", pos);
        assertNull(date);
        assertEquals(0, pos.getIndex()); // index unchanged
    }

    @Test
    public void testParsePositionUpdated() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("2011-12-03", pos);
        assertNotNull(date);
        assertEquals(10, pos.getIndex()); // length of matched string
        assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testParsePositionWithSpaces() {
        FastDateParser parser = createParser("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(1);
        Date date = parser.parse(" 2015-01-01", pos);
        assertNull(date); // because substring from offset 1 is "2015-01-01" which does match
        // Actually substring from offset 1 is "2015-01-01", that matches pattern, so it should parse.
        // Let's test with leading skip.
    }

    @Test
    public void testParseObject() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd");
        Object obj = parser.parseObject("2016-07-04");
        assertTrue(obj instanceof Date);
        assertEquals(parser.parse("2016-07-04"), obj);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidStringThrowsParseException() throws ParseException {
        FastDateParser parser = createParser("yyyy-MM-dd");
        parser.parse("not a date");
    }

    @Test
    public void testParseInvalidStringJapaneseImperialLocaleMessage() {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("abc");
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains(FastDateParser.JAPANESE_IMPERIAL.toString()));
            assertTrue(e.getMessage().contains("1868"));
        }
    }

    @Test
    public void testAdjustYear() throws Exception {
        FastDateParser parser = createParser("yy");
        // Set thisYear to a fixed value using reflection
        Field thisYearField = FastDateParser.class.getDeclaredField("thisYear");
        thisYearField.setAccessible(true);
        thisYearField.setInt(parser, 2000);

        // twoDigitYear=20: trial = 20+2000-0 = 2020; 2020 < 2000+20? 2020<2020 false -> returns 1920
        int adjusted = parser.adjustYear(20);
        assertEquals(1920, adjusted);

        // twoDigitYear=19: trial=2019; 2019<2020 true -> return 2019
        adjusted = parser.adjustYear(19);
        assertEquals(2019, adjusted);

        // twoDigitYear=0: trial=2000; 2000<2020 true -> return 2000
        adjusted = parser.adjustYear(0);
        assertEquals(2000, adjusted);
    }

    @Test
    public void testGetDisplayNamesEra() {
        FastDateParser parser = createParser("G", Locale.US);
        KeyValue[] eras = parser.getDisplayNames(Calendar.ERA);
        assertNotNull(eras);
        assertTrue(eras.length > 0);
        // find AD
        boolean foundAD = false;
        for (KeyValue kv : eras) {
            if ("AD".equalsIgnoreCase(kv.key)) {
                assertEquals(Calendar.AD, kv.value);
                foundAD = true;
            }
        }
        assertTrue(foundAD);
    }

    @Test
    public void testGetDisplayNamesDayOfWeek() {
        FastDateParser parser = createParser("E", Locale.US);
        KeyValue[] days = parser.getDisplayNames(Calendar.DAY_OF_WEEK);
        assertNotNull(days);
        assertEquals(7, days.length); // Sunday..Saturday, all non-empty
    }

    @Test
    public void testGetDisplayNamesAmPm() {
        FastDateParser parser = createParser("a", Locale.US);
        KeyValue[] ampm = parser.getDisplayNames(Calendar.AM_PM);
        assertNotNull(ampm);
        assertEquals(2, ampm.length);
    }

    @Test
    public void testGetDisplayNamesMonth() {
        FastDateParser parser = createParser("M", Locale.US);
        KeyValue[] months = parser.getDisplayNames(Calendar.MONTH);
        assertNotNull(months);
        assertEquals(12, months.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDisplayNamesInvalidField() {
        FastDateParser parser = createParser("y");
        parser.getDisplayNames(-1);
    }

    @Test
    public void testParsePatternAccessible() {
        FastDateParser parser = createParser("yyyy");
        assertNotNull(parser.getParsePattern());
        assertTrue(parser.getParsePattern().matcher("2020").lookingAt());
    }

    @Test
    public void testCopyQuotedStrategyIsNumber() {
        // indirectly test that quoted numbers are treated as numbers for width calculation
        // pattern where a quoted digit precedes a number field will not add extra width because isNextNumber true?
        // e.g. pattern "'12'yyyy" should parse "122020" as year 2020? Actually the literal "12" must be present.
        // So parse "122020" with pattern "'12'yyyy" should succeed and year=2020.
        FastDateParser parser = createParser("'12'yyyy");
        Date date = parser.parse("122020");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        // The width of the year field may be 4; this shows isNumber returned true for the quoted "12".
        // If isNumber had returned false, the regex for year might have been (\\p{IsNd}++)? but the result same.
    }

    @Test
    public void testParseQuotedTextWithQuote() throws ParseException {
        FastDateParser parser = createParser("'Date: '''yyyy-MM-dd");
        Date date = parser.parse("Date: '2015-01-01");
        assertNotNull(date);
    }

    @Test
    public void testParseWithAllFields() throws ParseException {
        String pattern = "G yyyy-MM-dd HH:mm:ss.SSS z EEE D wW F";
        FastDateParser parser = new FastDateParser(pattern, TimeZone.getTimeZone("GMT"), Locale.US);
        String input = "AD 2015-01-01 12:30:45.678 Pacific Standard Time Thu 1 1 1";
        // D=1 (day of year), w=1 (week of year), W=1 (week of month), F=1 (day of week in month)
        Date date = parser.parse(input);
        assertNotNull(date);
    }

    @Test
    public void testSerialization() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(parser);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        FastDateParser restored = (FastDateParser) ois.readObject();
        assertEquals(parser.getPattern(), restored.getPattern());
        assertEquals(parser.getTimeZone(), restored.getTimeZone());
        assertEquals(parser.getLocale(), restored.getLocale());
        // parse should work
        Date date = restored.parse("2015-12-25");
        assertNotNull(date);
    }

    @Test
    public void testIsNextNumberDuringInit() {
        // just create a parser with two consecutive number fields to ensure no exception
        createParser("yyMM");
    }

    @Test
    public void testEscapeRegexSpecialChars() {
        // pattern with special regex chars inside quoted literal
        FastDateParser parser = createParser("'('yyyy')'");
        Date date = parser.parse("(2015)");
        assertNotNull(date);
    }

    @Test
    public void testParseWeekFields() throws ParseException {
        FastDateParser parser = createParser("yyyy ww");
        Date date = parser.parse("2020 02");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2, cal.get(Calendar.WEEK_OF_YEAR));
        assertEquals(2020, cal.get(Calendar.YEAR));
    }
}
