package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class FastDatePrinterTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");

    @Test
    public void testConstructorsAndGetters() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", GMT, Locale.US);
        Assert.assertEquals("yyyy-MM-dd", printer.getPattern());
        Assert.assertEquals(GMT, printer.getTimeZone());
        Assert.assertEquals(Locale.US, printer.getLocale());
        Assert.assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testFormatMethods() {
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.set(2023, Calendar.JANUARY, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();
        long millis = date.getTime();

        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", GMT, Locale.US);

        String expected = "2023-01-15 13:45:30.123";
        Assert.assertEquals(expected, printer.format(millis));
        Assert.assertEquals(expected, printer.format(date));
        Assert.assertEquals(expected, printer.format(cal));

        StringBuffer buf1 = new StringBuffer();
        Assert.assertSame(buf1, printer.format(millis, buf1));
        Assert.assertEquals(expected, buf1.toString());

        StringBuffer buf2 = new StringBuffer();
        Assert.assertSame(buf2, printer.format(date, buf2));
        Assert.assertEquals(expected, buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        Assert.assertSame(buf3, printer.format(cal, buf3));
        Assert.assertEquals(expected, buf3.toString());

        StringBuffer buf4 = new StringBuffer();
        Assert.assertSame(buf4, printer.format((Object) date, buf4, new FieldPosition(0)));
        Assert.assertEquals(expected, buf4.toString());

        StringBuffer buf5 = new StringBuffer();
        Assert.assertSame(buf5, printer.format((Object) cal, buf5, new FieldPosition(0)));
        Assert.assertEquals(expected, buf5.toString());

        StringBuffer buf6 = new StringBuffer();
        Assert.assertSame(buf6, printer.format((Object) Long.valueOf(millis), buf6, new FieldPosition(0)));
        Assert.assertEquals(expected, buf6.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnknownObjectClass() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", GMT, Locale.US);
        printer.format("2023-01-01", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNullObject() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", GMT, Locale.US);
        printer.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testAllPatternTokens() {
        // G, y (2, 4, other), M (1, 2, 3, 4), d, h, H, m, s, S, E (short, full), D, F, w, W, a, k, K, z (short, full), Z (1, 2), literal '', 'text'
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.set(2023, Calendar.MARCH, 8, 5, 6, 7);
        cal.set(Calendar.MILLISECOND, 9);
        Date date = cal.getTime();

        FastDatePrinter pEra = new FastDatePrinter("G", GMT, Locale.US);
        Assert.assertEquals("AD", pEra.format(date));

        FastDatePrinter pYear2 = new FastDatePrinter("yy", GMT, Locale.US);
        Assert.assertEquals("23", pYear2.format(date));

        FastDatePrinter pYear4 = new FastDatePrinter("yyyy", GMT, Locale.US);
        Assert.assertEquals("2023", pYear4.format(date));

        FastDatePrinter pYear1 = new FastDatePrinter("y", GMT, Locale.US);
        Assert.assertEquals("2023", pYear1.format(date));

        FastDatePrinter pYear5 = new FastDatePrinter("yyyyy", GMT, Locale.US);
        Assert.assertEquals("02023", pYear5.format(date));

        FastDatePrinter pMonth1 = new FastDatePrinter("M", GMT, Locale.US);
        Assert.assertEquals("3", pMonth1.format(date));

        FastDatePrinter pMonth2 = new FastDatePrinter("MM", GMT, Locale.US);
        Assert.assertEquals("03", pMonth2.format(date));

        FastDatePrinter pMonth3 = new FastDatePrinter("MMM", GMT, Locale.US);
        Assert.assertEquals("Mar", pMonth3.format(date));

        FastDatePrinter pMonth4 = new FastDatePrinter("MMMM", GMT, Locale.US);
        Assert.assertEquals("March", pMonth4.format(date));

        FastDatePrinter pDay1 = new FastDatePrinter("d", GMT, Locale.US);
        Assert.assertEquals("8", pDay1.format(date));

        FastDatePrinter pDay2 = new FastDatePrinter("dd", GMT, Locale.US);
        Assert.assertEquals("08", pDay2.format(date));

        FastDatePrinter pDay3 = new FastDatePrinter("ddd", GMT, Locale.US);
        Assert.assertEquals("008", pDay3.format(date));

        FastDatePrinter pHour12_1 = new FastDatePrinter("h", GMT, Locale.US);
        Assert.assertEquals("5", pHour12_1.format(date));

        FastDatePrinter pHour12_2 = new FastDatePrinter("hh", GMT, Locale.US);
        Assert.assertEquals("05", pHour12_2.format(date));

        FastDatePrinter pHour24_1 = new FastDatePrinter("H", GMT, Locale.US);
        Assert.assertEquals("5", pHour24_1.format(date));

        FastDatePrinter pHour24_2 = new FastDatePrinter("HH", GMT, Locale.US);
        Assert.assertEquals("05", pHour24_2.format(date));

        FastDatePrinter pMin1 = new FastDatePrinter("m", GMT, Locale.US);
        Assert.assertEquals("6", pMin1.format(date));

        FastDatePrinter pMin2 = new FastDatePrinter("mm", GMT, Locale.US);
        Assert.assertEquals("06", pMin2.format(date));

        FastDatePrinter pSec1 = new FastDatePrinter("s", GMT, Locale.US);
        Assert.assertEquals("7", pSec1.format(date));

        FastDatePrinter pSec2 = new FastDatePrinter("ss", GMT, Locale.US);
        Assert.assertEquals("07", pSec2.format(date));

        FastDatePrinter pMs1 = new FastDatePrinter("S", GMT, Locale.US);
        Assert.assertEquals("9", pMs1.format(date));

        FastDatePrinter pMs2 = new FastDatePrinter("SS", GMT, Locale.US);
        Assert.assertEquals("09", pMs2.format(date));

        FastDatePrinter pMs3 = new FastDatePrinter("SSS", GMT, Locale.US);
        Assert.assertEquals("009", pMs3.format(date));

        FastDatePrinter pMs4 = new FastDatePrinter("SSSS", GMT, Locale.US);
        Assert.assertEquals("0009", pMs4.format(date));

        FastDatePrinter pDayOfWeekShort = new FastDatePrinter("EEE", GMT, Locale.US);
        Assert.assertEquals("Wed", pDayOfWeekShort.format(date));

        FastDatePrinter pDayOfWeekFull = new FastDatePrinter("EEEE", GMT, Locale.US);
        Assert.assertEquals("Wednesday", pDayOfWeekFull.format(date));

        FastDatePrinter pDayOfYear = new FastDatePrinter("D", GMT, Locale.US);
        Assert.assertEquals(String.valueOf(cal.get(Calendar.DAY_OF_YEAR)), pDayOfYear.format(date));

        FastDatePrinter pDayOfWeekInMonth = new FastDatePrinter("F", GMT, Locale.US);
        Assert.assertEquals(String.valueOf(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)), pDayOfWeekInMonth.format(date));

        FastDatePrinter pWeekOfYear = new FastDatePrinter("w", GMT, Locale.US);
        Assert.assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)), pWeekOfYear.format(date));

        FastDatePrinter pWeekOfMonth = new FastDatePrinter("W", GMT, Locale.US);
        Assert.assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)), pWeekOfMonth.format(date));

        FastDatePrinter pAmPm = new FastDatePrinter("a", GMT, Locale.US);
        Assert.assertEquals("AM", pAmPm.format(date));

        FastDatePrinter pHourInDay1_24 = new FastDatePrinter("k", GMT, Locale.US);
        Assert.assertEquals("5", pHourInDay1_24.format(date));

        FastDatePrinter pHourInAmPm0_11 = new FastDatePrinter("K", GMT, Locale.US);
        Assert.assertEquals("5", pHourInAmPm0_11.format(date));

        FastDatePrinter pTzShort = new FastDatePrinter("z", GMT, Locale.US);
        Assert.assertEquals("GMT", pTzShort.format(date));

        FastDatePrinter pTzLong = new FastDatePrinter("zzzz", GMT, Locale.US);
        Assert.assertEquals("Greenwich Mean Time", pTzLong.format(date));

        FastDatePrinter pTzRfc = new FastDatePrinter("Z", GMT, Locale.US);
        Assert.assertEquals("+0000", pTzRfc.format(date));

        FastDatePrinter pTzIso = new FastDatePrinter("ZZ", GMT, Locale.US);
        Assert.assertEquals("+00:00", pTzIso.format(date));
    }

    @Test
    public void testHourBoundaries() {
        Calendar calMidnight = new GregorianCalendar(GMT, Locale.US);
        calMidnight.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        FastDatePrinter pHour12 = new FastDatePrinter("h", GMT, Locale.US);
        Assert.assertEquals("12", pHour12.format(calMidnight));

        FastDatePrinter pHour24k = new FastDatePrinter("k", GMT, Locale.US);
        Assert.assertEquals("24", pHour24k.format(calMidnight));

        FastDatePrinter pHour24H = new FastDatePrinter("H", GMT, Locale.US);
        Assert.assertEquals("0", pHour24H.format(calMidnight));

        FastDatePrinter pHourK = new FastDatePrinter("K", GMT, Locale.US);
        Assert.assertEquals("0", pHourK.format(calMidnight));

        Calendar calNoon = new GregorianCalendar(GMT, Locale.US);
        calNoon.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        Assert.assertEquals("12", pHour12.format(calNoon));
        Assert.assertEquals("12", pHour24k.format(calNoon));
        Assert.assertEquals("12", pHour24H.format(calNoon));
        Assert.assertEquals("0", pHourK.format(calNoon));
    }

    @Test
    public void testNumberRulePaddingBranches() {
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.set(Calendar.MILLISECOND, 50);
        FastDatePrinter pUnpadded = new FastDatePrinter("S", GMT, Locale.US);
        Assert.assertEquals("50", pUnpadded.format(cal));

        cal.set(Calendar.MILLISECOND, 500);
        Assert.assertEquals("500", pUnpadded.format(cal));

        cal.set(Calendar.MILLISECOND, 5);
        FastDatePrinter pPadded3 = new FastDatePrinter("SSS", GMT, Locale.US);
        Assert.assertEquals("005", pPadded3.format(cal));

        cal.set(Calendar.MILLISECOND, 50);
        Assert.assertEquals("050", pPadded3.format(cal));

        cal.set(Calendar.MILLISECOND, 500);
        Assert.assertEquals("500", pPadded3.format(cal));

        FastDatePrinter pPadded5 = new FastDatePrinter("SSSSS", GMT, Locale.US);
        cal.set(Calendar.MILLISECOND, 50);
        Assert.assertEquals("00050", pPadded5.format(cal));

        cal.set(Calendar.MILLISECOND, 500);
        Assert.assertEquals("00500", pPadded5.format(cal));

        cal.set(2023, Calendar.OCTOBER, 15);
        FastDatePrinter pTwoDigitMonth = new FastDatePrinter("MM", GMT, Locale.US);
        Assert.assertEquals("10", pTwoDigitMonth.format(cal));

        FastDatePrinter pUnpaddedMonth = new FastDatePrinter("M", GMT, Locale.US);
        Assert.assertEquals("10", pUnpaddedMonth.format(cal));

        FastDatePrinter pTwoDigitNumber = new FastDatePrinter("dd", GMT, Locale.US);
        cal.set(Calendar.DAY_OF_MONTH, 105); // Simulated large value
        Assert.assertEquals("105", pTwoDigitNumber.format(cal));
    }

    @Test
    public void testLiteralsAndEscapes() {
        FastDatePrinter p1 = new FastDatePrinter("'Date:' yyyy''MM''dd 'o''clock' ''", GMT, Locale.US);
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.set(2023, Calendar.JANUARY, 15);
        Assert.assertEquals("Date: 2023'01'15 o'clock '", p1.format(cal));

        FastDatePrinter pCharLiteral = new FastDatePrinter("'T'", GMT, Locale.US);
        Assert.assertEquals("T", pCharLiteral.format(cal));

        FastDatePrinter pSpecialChars = new FastDatePrinter("yyyy-MM-dd'T'HH:mm:ss", GMT, Locale.US);
        cal.set(2023, Calendar.JANUARY, 15, 10, 20, 30);
        Assert.assertEquals("2023-01-15T10:20:30", pSpecialChars.format(cal));
    }

    @Test
    public void testTimeZoneFormattingAndOffsets() {
        TimeZone tzNegative = TimeZone.getTimeZone("America/New_York"); // typically -05:00 or -04:00
        Calendar cal = new GregorianCalendar(tzNegative, Locale.US);
        cal.set(2023, Calendar.JANUARY, 15, 12, 0, 0);

        FastDatePrinter pNoColon = new FastDatePrinter("Z", tzNegative, Locale.US);
        Assert.assertEquals("-0500", pNoColon.format(cal));

        FastDatePrinter pColon = new FastDatePrinter("ZZ", tzNegative, Locale.US);
        Assert.assertEquals("-05:00", pColon.format(cal));

        TimeZone tzPositive = TimeZone.getTimeZone("Asia/Kolkata"); // +05:30
        Calendar calPos = new GregorianCalendar(tzPositive, Locale.US);
        calPos.set(2023, Calendar.JANUARY, 15, 12, 0, 0);

        FastDatePrinter pPosNoColon = new FastDatePrinter("Z", tzPositive, Locale.US);
        Assert.assertEquals("+0530", pPosNoColon.format(calPos));

        FastDatePrinter pPosColon = new FastDatePrinter("ZZ", tzPositive, Locale.US);
        Assert.assertEquals("+05:30", pPosColon.format(calPos));
    }

    @Test
    public void testDaylightSavingsTimeZoneName() {
        SimpleTimeZone tz = new SimpleTimeZone(
                -5 * 3600000,
                "America/New_York",
                Calendar.MARCH, 2, Calendar.SUNDAY, 2 * 3600000,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 2 * 3600000
        );

        Calendar calWinter = new GregorianCalendar(tz, Locale.US);
        calWinter.set(2023, Calendar.JANUARY, 15, 12, 0, 0);

        Calendar calSummer = new GregorianCalendar(tz, Locale.US);
        calSummer.set(2023, Calendar.JULY, 15, 12, 0, 0);

        FastDatePrinter pShort = new FastDatePrinter("z", tz, Locale.US);
        Assert.assertEquals("EST", pShort.format(calWinter));
        Assert.assertEquals("EDT", pShort.format(calSummer));

        FastDatePrinter pLong = new FastDatePrinter("zzzz", tz, Locale.US);
        Assert.assertEquals("Eastern Standard Time", pLong.format(calWinter));
        Assert.assertEquals("Eastern Daylight Time", pLong.format(calSummer));
    }

    @Test
    public void testTimeZoneDisplayKeyCoverage() {
        String name1 = FastDatePrinter.getTimeZoneDisplay(EST, false, TimeZone.SHORT, Locale.US);
        String name2 = FastDatePrinter.getTimeZoneDisplay(EST, false, TimeZone.SHORT, Locale.US);
        Assert.assertEquals(name1, name2);

        String name3 = FastDatePrinter.getTimeZoneDisplay(EST, true, TimeZone.SHORT, Locale.US);
        Assert.assertNotNull(name3);
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", GMT, Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy-MM-dd", GMT, Locale.US);
        FastDatePrinter p3 = new FastDatePrinter("yyyy-MM-dd HH:mm", GMT, Locale.US);
        FastDatePrinter p4 = new FastDatePrinter("yyyy-MM-dd", EST, Locale.US);
        FastDatePrinter p5 = new FastDatePrinter("yyyy-MM-dd", GMT, Locale.GERMANY);

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("String Object"));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(p4));
        Assert.assertFalse(p1.equals(p5));

        Assert.assertEquals("FastDatePrinter[yyyy-MM-dd,en_US,GMT]", p1.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss", GMT, Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(printer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();

        Assert.assertEquals(printer, deserialized);
        Assert.assertEquals(printer.getPattern(), deserialized.getPattern());
        Assert.assertEquals(printer.getTimeZone(), deserialized.getTimeZone());
        Assert.assertEquals(printer.getLocale(), deserialized.getLocale());

        Date now = new Date();
        Assert.assertEquals(printer.format(now), deserialized.format(now));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternToken() {
        new FastDatePrinter("yyyy-MM-dd X", GMT, Locale.US);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPaddedNumberFieldInvalidSize() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", GMT, Locale.US);
        printer.selectNumberRule(Calendar.YEAR, 2);
        // Direct testing of constructor through overridden/custom invocation
        new FastDatePrinter("y", GMT, Locale.US) {
            {
                selectNumberRule(Calendar.YEAR, 1);
            }
        };
        // Calling directly with size < 3 on selectNumberRule fallback:
        // PaddedNumberField requires size >= 3.
        printer.selectNumberRule(Calendar.YEAR, 0); // throws IllegalArgumentException
    }
}
