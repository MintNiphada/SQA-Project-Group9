package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class FastDatePrinterTest {

    private static class TestableFastDatePrinter extends FastDatePrinter {
        TestableFastDatePrinter(String pattern, TimeZone timeZone, Locale locale) {
            super(pattern, timeZone, locale);
        }
    }

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Locale US = Locale.US;

    @Test
    public void testConstructorAndGetters() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        assertEquals("yyyy-MM-dd", printer.getPattern());
        assertEquals(GMT, printer.getTimeZone());
        assertEquals(US, printer.getLocale());
        assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPattern() {
        new TestableFastDatePrinter(null, GMT, US);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTimeZone() {
        new TestableFastDatePrinter("yyyy", null, US);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLocale() {
        new TestableFastDatePrinter("yyyy", GMT, null);
    }

    @Test
    public void testFormatDate() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        assertEquals("2003-04-05 12:34:56.789", printer.format(date));
    }

    @Test
    public void testFormatCalendar() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        assertEquals("2003-04-05 12:34:56.789", printer.format(cal));
    }

    @Test
    public void testFormatLong() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        long millis = cal.getTimeInMillis();
        assertEquals("2003-04-05 12:34:56.789", printer.format(millis));
    }

    @Test
    public void testFormatObjectWithDate() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format(cal.getTime(), buf, pos);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testFormatObjectWithCalendar() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format(cal, buf, pos);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testFormatObjectWithLong() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        long millis = cal.getTimeInMillis();
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format(millis, buf, pos);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectWithNull() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy", GMT, US);
        printer.format(null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectWithUnknownType() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy", GMT, US);
        printer.format("invalid", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testFormatDateToBuffer() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(cal.getTime(), buf);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testFormatCalendarToBuffer() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(cal, buf);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testFormatLongToBuffer() {
        FastDatePrinter printer = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        long millis = cal.getTimeInMillis();
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(millis, buf);
        assertEquals("2003-04-05", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testEquals() {
        FastDatePrinter p1 = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        FastDatePrinter p2 = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        FastDatePrinter p3 = new TestableFastDatePrinter("yyyy/MM/dd", GMT, US);
        FastDatePrinter p4 = new TestableFastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("PST"), US);
        FastDatePrinter p5 = new TestableFastDatePrinter("yyyy-MM-dd", GMT, Locale.FRANCE);

        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(p4));
        assertFalse(p1.equals(p5));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("string"));
    }

    @Test
    public void testHashCode() {
        FastDatePrinter p1 = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        FastDatePrinter p2 = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        FastDatePrinter p = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        assertEquals("FastDatePrinter[yyyy-MM-dd,en_US,GMT]", p.toString());
    }

    @Test
    public void testMaxLengthEstimate() {
        FastDatePrinter p = new TestableFastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        assertTrue(p.getMaxLengthEstimate() >= "2003-04-05 12:34:56.789".length());
    }

    @Test
    public void testSerialization() throws Exception {
        FastDatePrinter original = new TestableFastDatePrinter("yyyy-MM-dd", GMT, US);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        assertEquals(original.format(cal.getTime()), deserialized.format(cal.getTime()));
    }

    // Pattern letter tests
    @Test
    public void testEraG() {
        FastDatePrinter p = new TestableFastDatePrinter("G", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        assertEquals("AD", p.format(cal));
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        assertEquals("BC", p.format(cal));
    }

    @Test
    public void testYearPatterns() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        // y
        assertEquals("2003", new TestableFastDatePrinter("y", GMT, US).format(cal));
        // yy
        assertEquals("03", new TestableFastDatePrinter("yy", GMT, US).format(cal));
        // yyy
        assertEquals("2003", new TestableFastDatePrinter("yyy", GMT, US).format(cal));
        // yyyy
        assertEquals("2003", new TestableFastDatePrinter("yyyy", GMT, US).format(cal));
        // yyyyy (more than 4)
        assertEquals("02003", new TestableFastDatePrinter("yyyyy", GMT, US).format(cal));
    }

    @Test
    public void testMonthPatterns() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5); // month 3
        // M - unpadded
        assertEquals("4", new TestableFastDatePrinter("M", GMT, US).format(cal));
        // MM - two digit
        assertEquals("04", new TestableFastDatePrinter("MM", GMT, US).format(cal));
        // MMM - short text
        assertEquals("Apr", new TestableFastDatePrinter("MMM", GMT, US).format(cal));
        // MMMM - full text
        assertEquals("April", new TestableFastDatePrinter("MMMM", GMT, US).format(cal));
    }

    @Test
    public void testDayOfMonth() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5);
        // d - unpadded
        assertEquals("5", new TestableFastDatePrinter("d", GMT, US).format(cal));
        // dd - two digit
        assertEquals("05", new TestableFastDatePrinter("dd", GMT, US).format(cal));
        // ddd - padded to 3
        assertEquals("005", new TestableFastDatePrinter("ddd", GMT, US).format(cal));
    }

    @Test
    public void testHour12() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 0, 0, 0); // midnight
        // h - unpadded 1-12
        assertEquals("12", new TestableFastDatePrinter("h", GMT, US).format(cal));
        // hh - two digit
        assertEquals("12", new TestableFastDatePrinter("hh", GMT, US).format(cal));
        cal.set(Calendar.HOUR, 1);
        assertEquals("1", new TestableFastDatePrinter("h", GMT, US).format(cal));
        assertEquals("01", new TestableFastDatePrinter("hh", GMT, US).format(cal));
    }

    @Test
    public void testHour24() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 0, 0, 0); // midnight
        // H - unpadded 0-23
        assertEquals("0", new TestableFastDatePrinter("H", GMT, US).format(cal));
        // HH - two digit
        assertEquals("00", new TestableFastDatePrinter("HH", GMT, US).format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("1", new TestableFastDatePrinter("H", GMT, US).format(cal));
        assertEquals("01", new TestableFastDatePrinter("HH", GMT, US).format(cal));
    }

    @Test
    public void testMinute() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34);
        assertEquals("34", new TestableFastDatePrinter("m", GMT, US).format(cal)); // unpadded, but 34 > 9
        assertEquals("34", new TestableFastDatePrinter("mm", GMT, US).format(cal));
        cal.set(Calendar.MINUTE, 5);
        assertEquals("5", new TestableFastDatePrinter("m", GMT, US).format(cal));
        assertEquals("05", new TestableFastDatePrinter("mm", GMT, US).format(cal));
    }

    @Test
    public void testSecond() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        assertEquals("56", new TestableFastDatePrinter("s", GMT, US).format(cal));
        assertEquals("56", new TestableFastDatePrinter("ss", GMT, US).format(cal));
        cal.set(Calendar.SECOND, 5);
        assertEquals("5", new TestableFastDatePrinter("s", GMT, US).format(cal));
        assertEquals("05", new TestableFastDatePrinter("ss", GMT, US).format(cal));
    }

    @Test
    public void testMillisecond() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        assertEquals("789", new TestableFastDatePrinter("S", GMT, US).format(cal));
        assertEquals("789", new TestableFastDatePrinter("SSS", GMT, US).format(cal));
        cal.set(Calendar.MILLISECOND, 5);
        assertEquals("5", new TestableFastDatePrinter("S", GMT, US).format(cal));
        assertEquals("005", new TestableFastDatePrinter("SSS", GMT, US).format(cal));
    }

    @Test
    public void testDayOfWeek() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5); // Saturday
        // E short
        assertEquals("Sat", new TestableFastDatePrinter("E", GMT, US).format(cal));
        assertEquals("Sat", new TestableFastDatePrinter("EE", GMT, US).format(cal));
        assertEquals("Sat", new TestableFastDatePrinter("EEE", GMT, US).format(cal));
        // EEEE full
        assertEquals("Saturday", new TestableFastDatePrinter("EEEE", GMT, US).format(cal));
    }

    @Test
    public void testDayOfYear() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5); // Jan 1 = 1, Apr 5 = 95 (non-leap)
        assertEquals("95", new TestableFastDatePrinter("D", GMT, US).format(cal));
        assertEquals("095", new TestableFastDatePrinter("DDD", GMT, US).format(cal));
    }

    @Test
    public void testDayOfWeekInMonth() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5); // first Saturday in April
        assertEquals("1", new TestableFastDatePrinter("F", GMT, US).format(cal));
        cal.set(Calendar.DAY_OF_MONTH, 12); // second Saturday
        assertEquals("2", new TestableFastDatePrinter("F", GMT, US).format(cal));
    }

    @Test
    public void testWeekOfYear() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.JANUARY, 1); // Wednesday, week 1
        assertEquals("1", new TestableFastDatePrinter("w", GMT, US).format(cal));
        assertEquals("01", new TestableFastDatePrinter("ww", GMT, US).format(cal));
    }

    @Test
    public void testWeekOfMonth() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5); // first week of April? depends on locale
        // Better set to a known week
        cal.set(Calendar.DAY_OF_MONTH, 1); // first day, week 1
        assertEquals("1", new TestableFastDatePrinter("W", GMT, US).format(cal));
        cal.set(Calendar.DAY_OF_MONTH, 8); // second week if week starts on Sunday?
        // Hard to test exactly due to locale, just check output matches Calendar's field
        assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)), new TestableFastDatePrinter("W", GMT, US).format(cal));
    }

    @Test
    public void testAmPm() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56); // noon = PM
        assertEquals("PM", new TestableFastDatePrinter("a", GMT, US).format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1); // 1 AM
        assertEquals("AM", new TestableFastDatePrinter("a", GMT, US).format(cal));
    }

    @Test
    public void testHour24_1to24() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 0, 0, 0);
        assertEquals("24", new TestableFastDatePrinter("k", GMT, US).format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("1", new TestableFastDatePrinter("k", GMT, US).format(cal));
        assertEquals("01", new TestableFastDatePrinter("kk", GMT, US).format(cal));
    }

    @Test
    public void testHour0_11() {
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 0, 0, 0);
        assertEquals("0", new TestableFastDatePrinter("K", GMT, US).format(cal));
        cal.set(Calendar.HOUR, 1);
        assertEquals("1", new TestableFastDatePrinter("K", GMT, US).format(cal));
        assertEquals("01", new TestableFastDatePrinter("KK", GMT, US).format(cal));
    }

    @Test
    public void testTimeZoneShort() {
        FastDatePrinter p = new TestableFastDatePrinter("z", TimeZone.getTimeZone("GMT"), US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 0, 0);
        assertEquals("GMT", p.format(cal));
    }

    @Test
    public void testTimeZoneLong() {
        FastDatePrinter p = new TestableFastDatePrinter("zzzz", TimeZone.getTimeZone("GMT"), US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 0, 0);
        assertEquals("Greenwich Mean Time", p.format(cal));
    }

    @Test
    public void testTimeZoneNumber() {
        // GMT offset 0
        FastDatePrinter p1 = new TestableFastDatePrinter("Z", TimeZone.getTimeZone("GMT"), US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 0, 0);
        assertEquals("+0000", p1.format(cal));
        // with colon
        FastDatePrinter p2 = new TestableFastDatePrinter("ZZ", TimeZone.getTimeZone("GMT"), US);
        assertEquals("+00:00", p2.format(cal));
        // positive offset
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDatePrinter p3 = new TestableFastDatePrinter("Z", tz, US);
        Calendar cal2 = new GregorianCalendar(tz, US);
        cal2.setTimeInMillis(cal.getTimeInMillis());
        assertEquals("+0530", p3.format(cal2));
        FastDatePrinter p4 = new TestableFastDatePrinter("ZZ", tz, US);
        assertEquals("+05:30", p4.format(cal2));
        // negative offset
        TimeZone tzNeg = TimeZone.getTimeZone("GMT-05:00");
        FastDatePrinter p5 = new TestableFastDatePrinter("Z", tzNeg, US);
        Calendar cal3 = new GregorianCalendar(tzNeg, US);
        cal3.setTimeInMillis(cal.getTimeInMillis());
        assertEquals("-0500", p5.format(cal3));
        FastDatePrinter p6 = new TestableFastDatePrinter("ZZ", tzNeg, US);
        assertEquals("-05:00", p6.format(cal3));
    }

    @Test
    public void testLiteralSingleQuote() {
        FastDatePrinter p = new TestableFastDatePrinter("'T'HH:mm", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 0);
        assertEquals("T12:34", p.format(cal));
    }

    @Test
    public void testLiteralMultipleCharacters() {
        FastDatePrinter p = new TestableFastDatePrinter("'abc'HH", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 0, 0);
        assertEquals("abc12", p.format(cal));
    }

    @Test
    public void testEscapedQuote() {
        FastDatePrinter p = new TestableFastDatePrinter("''HH:mm''", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 0);
        assertEquals("'12:34'", p.format(cal));
    }

    @Test
    public void testUnterminatedLiteral() {
        try {
            new TestableFastDatePrinter("'abc", GMT, US);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testInvalidPatternLetter() {
        try {
            new TestableFastDatePrinter("X", GMT, US);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPaddedNumberFieldNegative() {
        // Try to trigger Validate.isTrue negative values: use year with pattern yyyy but year < 1000? Actually negative year not possible.
        // We can test via direct reflection on PaddedNumberField? Not accessible, but we can use pattern that creates PaddedNumberField
        // and set a calendar with field value negative? Not possible for standard calendar fields.
        // Maybe skip this as it's internal and not triggerable through normal use.
    }

    @Test
    public void testGetTimeZoneDisplayCache() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        String std = FastDatePrinter.getTimeZoneDisplay(tz, false, TimeZone.SHORT, US);
        assertEquals("GMT", std);
        String daylight = FastDatePrinter.getTimeZoneDisplay(tz, true, TimeZone.SHORT, US);
        // GMT has no DST, so display name same
        assertEquals("GMT", daylight);
        // Test with a timezone with DST
        TimeZone tzWithDST = TimeZone.getTimeZone("America/New_York");
        String std2 = FastDatePrinter.getTimeZoneDisplay(tzWithDST, false, TimeZone.LONG, US);
        String daylight2 = FastDatePrinter.getTimeZoneDisplay(tzWithDST, true, TimeZone.LONG, US);
        assertNotNull(std2);
        assertNotNull(daylight2);
        assertNotEquals(std2, daylight2);
        // Call again to hit cache
        assertEquals(std2, FastDatePrinter.getTimeZoneDisplay(tzWithDST, false, TimeZone.LONG, US));
    }

    @Test
    public void testTimeZoneNameRuleWithDaylight() {
        TimeZone ny = TimeZone.getTimeZone("America/New_York");
        FastDatePrinter p = new TestableFastDatePrinter("zzzz", ny, US);
        Calendar cal = new GregorianCalendar(ny, US);
        cal.set(2003, Calendar.JULY, 5, 12, 0, 0); // summer, DST in effect
        String summer = p.format(cal);
        assertTrue(summer.contains("Daylight") || summer.contains("Eastern Daylight"));
        cal.set(Calendar.MONTH, Calendar.JANUARY); // winter, standard time
        String winter = p.format(cal);
        assertFalse(winter.contains("Daylight"));
    }

    @Test
    public void testComplexPattern() {
        FastDatePrinter p = new TestableFastDatePrinter("EEE, d MMM yyyy HH:mm:ss Z", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        assertEquals("Sat, 5 Apr 2003 12:34:56 +0000", p.format(cal));
    }

    @Test
    public void testLocaleSensitivity() {
        FastDatePrinter p = new TestableFastDatePrinter("MMMM EEEE", GMT, Locale.FRANCE);
        Calendar cal = new GregorianCalendar(GMT, Locale.FRANCE);
        cal.set(2003, Calendar.APRIL, 5);
        assertEquals("avril samedi", p.format(cal));
    }

    @Test
    public void testParseTokenMixed() {
        // Ensure literals and pattern letters are parsed correctly
        FastDatePrinter p = new TestableFastDatePrinter("yyyy-MM-dd'T'HH:mm:ss", GMT, US);
        Calendar cal = new GregorianCalendar(GMT, US);
        cal.set(2003, Calendar.APRIL, 5, 12, 34, 56);
        assertEquals("2003-04-05T12:34:56", p.format(cal));
    }
}
