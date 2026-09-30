package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;

    @Test
    public void testBasicFormatPatternParsing() throws Exception {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        FastDateParser parser = new FastDateParser(pattern, GMT, US);

        Assert.assertEquals(pattern, parser.getPattern());
        Assert.assertEquals(GMT, parser.getTimeZone());
        Assert.assertEquals(US, parser.getLocale());
        Assert.assertNotNull(parser.getParsePattern());

        Date parsed = parser.parse("2023-11-25 14:30:45.123");
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(parsed);

        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        Assert.assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(45, cal.get(Calendar.SECOND));
        Assert.assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testAllDateFields() throws Exception {
        String pattern = "G yyyy M d D E F w W a k K h H m s S z Z";
        FastDateParser parser = new FastDateParser(pattern, GMT, US);

        Date date = parser.parse("AD 2023 11 25 329 Sat 4 47 4 PM 14 2 2 14 30 45 123 GMT -0500");
        Assert.assertNotNull(date);

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        Assert.assertEquals(Calendar.AD, cal.get(Calendar.ERA));
        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
    }

    @Test
    public void testMonthTextAndNumber() throws Exception {
        FastDateParser parserLongMonth = new FastDateParser("MMMM dd, yyyy", GMT, US);
        Date d1 = parserLongMonth.parse("November 25, 2023");
        Calendar cal1 = Calendar.getInstance(GMT, US);
        cal1.setTime(d1);
        Assert.assertEquals(Calendar.NOVEMBER, cal1.get(Calendar.MONTH));

        FastDateParser parserShortMonth = new FastDateParser("MMM dd, yyyy", GMT, US);
        Date d2 = parserShortMonth.parse("Nov 25, 2023");
        Calendar cal2 = Calendar.getInstance(GMT, US);
        cal2.setTime(d2);
        Assert.assertEquals(Calendar.NOVEMBER, cal2.get(Calendar.MONTH));

        FastDateParser parserNumMonth = new FastDateParser("MM dd, yyyy", GMT, US);
        Date d3 = parserNumMonth.parse("11 25, 2023");
        Calendar cal3 = Calendar.getInstance(GMT, US);
        cal3.setTime(d3);
        Assert.assertEquals(Calendar.NOVEMBER, cal3.get(Calendar.MONTH));
    }

    @Test
    public void testTwoDigitYearAdjustment() throws Exception {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Calendar currentCal = Calendar.getInstance(GMT, US);
        int currentYear = currentCal.get(Calendar.YEAR);
        int currentCentury = currentYear - (currentYear % 100);

        int twoDigitFuture = (currentYear + 10) % 100;
        Date parsedFuture = parser.parse(String.format("%02d-01-01", twoDigitFuture));
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(parsedFuture);
        Assert.assertEquals(currentCentury + twoDigitFuture, cal.get(Calendar.YEAR));

        int twoDigitPast = (currentYear - 50 + 100) % 100;
        Date parsedPast = parser.parse(String.format("%02d-01-01", twoDigitPast));
        cal.setTime(parsedPast);
        int expectedPastYear = currentCentury + twoDigitPast;
        if (expectedPastYear >= currentYear + 20) {
            expectedPastYear -= 100;
        }
        Assert.assertEquals(expectedPastYear, cal.get(Calendar.YEAR));
    }

    @Test
    public void testAdjacentNumbersParsing() throws Exception {
        FastDateParser parser = new FastDateParser("yyyyMMddHHmmss", GMT, US);
        Date date = parser.parse("20231125143045");
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        Assert.assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(45, cal.get(Calendar.SECOND));
    }

    @Test
    public void testQuotedLiteralsAndSpecialChars() throws Exception {
        String pattern = "'Date: 'yyyy-MM-dd 'Time: 'HH:mm:ss 'Zone: ''Special''' [?]{}()\\|*+^$.";
        FastDateParser parser = new FastDateParser(pattern, GMT, US);
        Date date = parser.parse("Date: 2023-11-25 Time: 14:30:45 Zone: 'Special' [?]{}()\\|*+^$.");
        Assert.assertNotNull(date);

        FastDateParser parserQuotesOnly = new FastDateParser("''yyyy''", GMT, US);
        Date dateQuotes = parserQuotesOnly.parse("'2023'");
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(dateQuotes);
        Assert.assertEquals(2023, cal.get(Calendar.YEAR));
    }

    @Test
    public void testQuotesAtEnd() {
        FastDateParser parser = new FastDateParser("yyyy'T'", GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date d = parser.parse("2023T", pos);
        Assert.assertNotNull(d);
        Assert.assertEquals(5, pos.getIndex());
    }

    @Test
    public void testTimeZoneParsingOffsets() throws Exception {
        FastDateParser parserZ = new FastDateParser("yyyy-MM-dd HH:mm:ss Z", GMT, US);

        Date d1 = parserZ.parse("2023-11-25 12:00:00 +0000");
        Date d2 = parserZ.parse("2023-11-25 12:00:00 +00:00");
        Date d3 = parserZ.parse("2023-11-25 12:00:00 GMT+00:00");
        Date d4 = parserZ.parse("2023-11-25 12:00:00 -0500");

        Assert.assertEquals(d1.getTime(), d2.getTime());
        Assert.assertEquals(d1.getTime(), d3.getTime());
        Assert.assertEquals(d1.getTime() + 5 * 3600 * 1000L, d4.getTime());
    }

    @Test
    public void testTimeZoneNames() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd z", GMT, US);
        Date d1 = parser.parse("2023-01-01 EST");
        Date d2 = parser.parse("2023-01-01 GMT");
        Assert.assertTrue(d1.getTime() > d2.getTime());
    }

    @Test
    public void testParseWithPosition() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String prefix = "Prefix text: 2023-11-25 suffix";
        ParsePosition pos = new ParsePosition(13);
        Date date = parser.parse(prefix, pos);
        Assert.assertNotNull(date);
        Assert.assertEquals(23, pos.getIndex());

        ParsePosition invalidPos = new ParsePosition(0);
        Date failed = parser.parse(prefix, invalidPos);
        Assert.assertNull(failed);
        Assert.assertEquals(0, invalidPos.getIndex());
    }

    @Test
    public void testParseObject() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj1 = parser.parseObject("2023-11-25");
        Assert.assertTrue(obj1 instanceof Date);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2023-11-25", pos);
        Assert.assertTrue(obj2 instanceof Date);
        Assert.assertEquals(10, pos.getIndex());
    }

    @Test(expected = ParseException.class)
    public void testParseException() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("invalid-date");
    }

    @Test
    public void testJapaneseImperialLocaleException() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("invalid-date");
            Assert.fail("Expected ParseException");
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains("does not support dates before 1868 AD"));
        }
    }

    @Test
    public void testJapaneseImperialEra() throws Exception {
        FastDateParser parser = new FastDateParser("GGGG yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        Date date = parser.parse("Heisei 01-01-08");
        Assert.assertNotNull(date);
    }

    @Test
    public void testEqualsAndHashCodeAndToString() {
        FastDateParser p1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser p2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser p3 = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser p4 = new FastDateParser("yyyy-MM-dd", EST, US);
        FastDateParser p5 = new FastDateParser("yyyy-MM-dd", GMT, Locale.GERMANY);

        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        Assert.assertNotEquals(p1, null);
        Assert.assertNotEquals(p1, "some string");
        Assert.assertNotEquals(p1, p3);
        Assert.assertNotEquals(p1, p4);
        Assert.assertNotEquals(p1, p5);

        String str = p1.toString();
        Assert.assertTrue(str.contains("FastDateParser[yyyy-MM-dd,en_US,GMT]"));
    }

    @Test
    public void testSerialization() throws Exception {
        FastDateParser original = new FastDateParser("yyyy-MM-dd HH:mm:ss", EST, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateParser deserialized = (FastDateParser) ois.readObject();

        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.getPattern(), deserialized.getPattern());
        Assert.assertEquals(original.getTimeZone(), deserialized.getTimeZone());
        Assert.assertEquals(original.getLocale(), deserialized.getLocale());

        Date d1 = original.parse("2023-11-25 14:30:45");
        Date d2 = deserialized.parse("2023-11-25 14:30:45");
        Assert.assertEquals(d1, d2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFieldInGetDisplayNames() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.getDisplayNames(Calendar.ZONE_OFFSET);
    }

    @Test
    public void testHourStrategiesModifiers() throws Exception {
        FastDateParser parserH = new FastDateParser("H", GMT, US);
        Date dH = parserH.parse("24");
        Calendar calH = Calendar.getInstance(GMT, US);
        calH.setTime(dH);
        Assert.assertEquals(0, calH.get(Calendar.HOUR_OF_DAY));

        FastDateParser parserh = new FastDateParser("h", GMT, US);
        Date dh = parserh.parse("12");
        Calendar calh = Calendar.getInstance(GMT, US);
        calh.setTime(dh);
        Assert.assertEquals(0, calh.get(Calendar.HOUR));

        FastDateParser parserk = new FastDateParser("k", GMT, US);
        Date dk = parserk.parse("24");
        Calendar calk = Calendar.getInstance(GMT, US);
        calk.setTime(dk);
        Assert.assertEquals(0, calk.get(Calendar.HOUR_OF_DAY));

        FastDateParser parserK = new FastDateParser("K", GMT, US);
        Date dK = parserK.parse("0");
        Calendar calK = Calendar.getInstance(GMT, US);
        calK.setTime(dK);
        Assert.assertEquals(0, calK.get(Calendar.HOUR));
    }

    @Test
    public void testDayOfWeekStrategy() throws Exception {
        FastDateParser parser = new FastDateParser("EEEE", GMT, US);
        Date d1 = parser.parse("Monday");
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(d1);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        FastDateParser parserShort = new FastDateParser("E", GMT, US);
        Date d2 = parserShort.parse("Tue");
        cal.setTime(d2);
        Assert.assertEquals(Calendar.TUESDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testAmPmStrategy() throws Exception {
        FastDateParser parser = new FastDateParser("hh a", GMT, US);
        Date am = parser.parse("08 AM");
        Calendar calAm = Calendar.getInstance(GMT, US);
        calAm.setTime(am);
        Assert.assertEquals(Calendar.AM, calAm.get(Calendar.AM_PM));

        Date pm = parser.parse("08 PM");
        Calendar calPm = Calendar.getInstance(GMT, US);
        calPm.setTime(pm);
        Assert.assertEquals(Calendar.PM, calPm.get(Calendar.AM_PM));
    }
}
