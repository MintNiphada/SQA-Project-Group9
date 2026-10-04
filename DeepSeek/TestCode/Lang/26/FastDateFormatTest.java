package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormatTest {

    private static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String PATTERN_SIMPLE = "yyyy-MM-dd";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone PST = TimeZone.getTimeZone("PST");
    private static final Locale US = Locale.US;
    private static final Locale GERMANY = Locale.GERMANY;

    private FastDateFormat isoFormat;
    private FastDateFormat simpleFormat;
    private Calendar testCalendar;

    @Before
    public void setUp() {
        isoFormat = FastDateFormat.getInstance(PATTERN_ISO, UTC, US);
        simpleFormat = FastDateFormat.getInstance(PATTERN_SIMPLE, null, null);
        testCalendar = new GregorianCalendar(UTC);
        testCalendar.set(2020, Calendar.JANUARY, 15, 10, 30, 45);
        testCalendar.set(Calendar.MILLISECOND, 123);
    }

    @Test
    public void testGetInstanceDefaultPattern() {
        FastDateFormat format = FastDateFormat.getInstance();
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetInstancePattern() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        Assert.assertNotNull(format);
        Assert.assertEquals("2020-01-15", format.format(testCalendar));
    }

    @Test
    public void testGetInstancePatternTimeZone() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", PST);
        Assert.assertNotNull(format);
        Calendar cal = Calendar.getInstance(PST);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        String result = format.format(cal);
        Assert.assertTrue(result.contains("10:30:45"));
    }

    @Test
    public void testGetInstancePatternLocale() {
        FastDateFormat format = FastDateFormat.getInstance("EEEE", GERMANY);
        Assert.assertNotNull(format);
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        String result = format.format(cal);
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetInstancePatternTimeZoneLocale() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd z", PST, GERMANY);
        Assert.assertNotNull(format);
        Calendar cal = new GregorianCalendar(PST);
        cal.set(2020, Calendar.JANUARY, 15, 10, 0, 0);
        String result = format.format(cal);
        Assert.assertTrue(result.contains("2020-01-15"));
    }

    @Test
    public void testGetInstanceCaching() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        Assert.assertSame(f1, f2);
    }

    @Test
    public void testGetDateInstanceStyle() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateInstanceStyleLocale() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.LONG, GERMANY);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateInstanceStyleTimeZone() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, PST);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateInstanceStyleTimeZoneLocale() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.SHORT, UTC, US);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetTimeInstanceStyle() {
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.FULL);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetTimeInstanceStyleLocale() {
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.LONG, GERMANY);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetTimeInstanceStyleTimeZone() {
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, PST);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetTimeInstanceStyleTimeZoneLocale() {
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, UTC, US);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateTimeInstanceLocale() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, GERMANY);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateTimeInstanceTimeZone() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, PST);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetDateTimeInstanceTimeZoneLocale() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, UTC, US);
        Assert.assertNotNull(format);
        String result = format.format(new Date(0));
        Assert.assertNotNull(result);
    }

    @Test
    public void testFormatObjectDate() {
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        Date date = testCalendar.getTime();
        StringBuffer result = isoFormat.format(date, buf, pos);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testFormatObjectCalendar() {
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = isoFormat.format(testCalendar, buf, pos);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testFormatObjectLong() {
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        Long millis = testCalendar.getTimeInMillis();
        StringBuffer result = isoFormat.format(millis, buf, pos);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectNull() {
        isoFormat.format(null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectUnknownClass() {
        isoFormat.format("invalid", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testFormatLong() {
        long millis = testCalendar.getTimeInMillis();
        String result = isoFormat.format(millis);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("2020"));
    }

    @Test
    public void testFormatDate() {
        Date date = testCalendar.getTime();
        String result = isoFormat.format(date);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("2020"));
    }

    @Test
    public void testFormatCalendar() {
        String result = isoFormat.format(testCalendar);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("2020"));
    }

    @Test
    public void testFormatLongStringBuffer() {
        long millis = testCalendar.getTimeInMillis();
        StringBuffer buf = new StringBuffer();
        StringBuffer result = isoFormat.format(millis, buf);
        Assert.assertSame(buf, result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testFormatDateStringBuffer() {
        Date date = testCalendar.getTime();
        StringBuffer buf = new StringBuffer();
        StringBuffer result = isoFormat.format(date, buf);
        Assert.assertSame(buf, result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testFormatCalendarStringBuffer() {
        StringBuffer buf = new StringBuffer();
        StringBuffer result = isoFormat.format(testCalendar, buf);
        Assert.assertSame(buf, result);
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testFormatCalendarForcedTimeZone() {
        FastDateFormat forcedFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss z", PST, US);
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        String result = forcedFormat.format(cal);
        Assert.assertTrue(result.contains("PST") || result.contains("PDT"));
    }

    @Test
    public void testParseObject() {
        ParsePosition pos = new ParsePosition(0);
        Object result = isoFormat.parseObject("2020-01-15", pos);
        Assert.assertNull(result);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testGetPattern() {
        Assert.assertEquals(PATTERN_ISO, isoFormat.getPattern());
    }

    @Test
    public void testGetTimeZone() {
        Assert.assertEquals(UTC, isoFormat.getTimeZone());
    }

    @Test
    public void testGetTimeZoneOverridesCalendar() {
        Assert.assertTrue(isoFormat.getTimeZoneOverridesCalendar());
        Assert.assertFalse(simpleFormat.getTimeZoneOverridesCalendar());
    }

    @Test
    public void testGetLocale() {
        Assert.assertEquals(US, isoFormat.getLocale());
    }

    @Test
    public void testGetMaxLengthEstimate() {
        Assert.assertTrue(isoFormat.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testEquals() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        FastDateFormat f3 = FastDateFormat.getInstance("yyyy-MM-dd HH", UTC, US);
        Assert.assertEquals(f1, f2);
        Assert.assertNotEquals(f1, f3);
        Assert.assertNotEquals(f1, null);
        Assert.assertNotEquals(f1, new Object());
    }

    @Test
    public void testHashCode() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString() {
        String str = isoFormat.toString();
        Assert.assertTrue(str.contains(PATTERN_ISO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPattern() {
        new FastDateFormat(null, null, null);
    }

    @Test
    public void testPatternLetterG() {
        FastDateFormat format = FastDateFormat.getInstance("G yyyy", US);
        String result = format.format(testCalendar);
        Assert.assertTrue(result.startsWith("AD"));
    }

    @Test
    public void testPatternLetterY() {
        FastDateFormat format = FastDateFormat.getInstance("yy");
        String result = format.format(testCalendar);
        Assert.assertEquals("20", result);
    }

    @Test
    public void testPatternLetterYLong() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        String result = format.format(testCalendar);
        Assert.assertEquals("2020", result);
    }

    @Test
    public void testPatternLetterM() {
        FastDateFormat format = FastDateFormat.getInstance("M");
        String result = format.format(testCalendar);
        Assert.assertEquals("1", result);
    }

    @Test
    public void testPatternLetterMM() {
        FastDateFormat format = FastDateFormat.getInstance("MM");
        String result = format.format(testCalendar);
        Assert.assertEquals("01", result);
    }

    @Test
    public void testPatternLetterMMM() {
        FastDateFormat format = FastDateFormat.getInstance("MMM", US);
        String result = format.format(testCalendar);
        Assert.assertEquals("Jan", result);
    }

    @Test
    public void testPatternLetterMMMM() {
        FastDateFormat format = FastDateFormat.getInstance("MMMM", US);
        String result = format.format(testCalendar);
        Assert.assertEquals("January", result);
    }

    @Test
    public void testPatternLetterD() {
        FastDateFormat format = FastDateFormat.getInstance("d");
        String result = format.format(testCalendar);
        Assert.assertEquals("15", result);
    }

    @Test
    public void testPatternLetterDD() {
        FastDateFormat format = FastDateFormat.getInstance("dd");
        String result = format.format(testCalendar);
        Assert.assertEquals("15", result);
    }

    @Test
    public void testPatternLetterH() {
        FastDateFormat format = FastDateFormat.getInstance("h");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("12", result);
    }

    @Test
    public void testPatternLetterHH() {
        FastDateFormat format = FastDateFormat.getInstance("hh");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 13, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("01", result);
    }

    @Test
    public void testPatternLetterH24() {
        FastDateFormat format = FastDateFormat.getInstance("H");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 13, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("13", result);
    }

    @Test
    public void testPatternLetterHH24() {
        FastDateFormat format = FastDateFormat.getInstance("HH");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 1, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("01", result);
    }

    @Test
    public void testPatternLetterM() {
        FastDateFormat format = FastDateFormat.getInstance("m");
        String result = format.format(testCalendar);
        Assert.assertEquals("30", result);
    }

    @Test
    public void testPatternLetterMM() {
        FastDateFormat format = FastDateFormat.getInstance("mm");
        String result = format.format(testCalendar);
        Assert.assertEquals("30", result);
    }

    @Test
    public void testPatternLetterS() {
        FastDateFormat format = FastDateFormat.getInstance("s");
        String result = format.format(testCalendar);
        Assert.assertEquals("45", result);
    }

    @Test
    public void testPatternLetterSS() {
        FastDateFormat format = FastDateFormat.getInstance("ss");
        String result = format.format(testCalendar);
        Assert.assertEquals("45", result);
    }

    @Test
    public void testPatternLetterS() {
        FastDateFormat format = FastDateFormat.getInstance("S");
        String result = format.format(testCalendar);
        Assert.assertEquals("123", result);
    }

    @Test
    public void testPatternLetterSS() {
        FastDateFormat format = FastDateFormat.getInstance("SS");
        String result = format.format(testCalendar);
        Assert.assertEquals("12", result);
    }

    @Test
    public void testPatternLetterSSS() {
        FastDateFormat format = FastDateFormat.getInstance("SSS");
        String result = format.format(testCalendar);
        Assert.assertEquals("123", result);
    }

    @Test
    public void testPatternLetterE() {
        FastDateFormat format = FastDateFormat.getInstance("E", US);
        String result = format.format(testCalendar);
        Assert.assertEquals("Wed", result);
    }

    @Test
    public void testPatternLetterEEEE() {
        FastDateFormat format = FastDateFormat.getInstance("EEEE", US);
        String result = format.format(testCalendar);
        Assert.assertEquals("Wednesday", result);
    }

    @Test
    public void testPatternLetterD() {
        FastDateFormat format = FastDateFormat.getInstance("D");
        String result = format.format(testCalendar);
        Assert.assertEquals("15", result);
    }

    @Test
    public void testPatternLetterF() {
        FastDateFormat format = FastDateFormat.getInstance("F");
        String result = format.format(testCalendar);
        Assert.assertEquals("3", result);
    }

    @Test
    public void testPatternLetterW() {
        FastDateFormat format = FastDateFormat.getInstance("w");
        String result = format.format(testCalendar);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPatternLetterW() {
        FastDateFormat format = FastDateFormat.getInstance("W");
        String result = format.format(testCalendar);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPatternLetterA() {
        FastDateFormat format = FastDateFormat.getInstance("a", US);
        String result = format.format(testCalendar);
        Assert.assertEquals("AM", result);
    }

    @Test
    public void testPatternLetterK() {
        FastDateFormat format = FastDateFormat.getInstance("k");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("24", result);
    }

    @Test
    public void testPatternLetterK() {
        FastDateFormat format = FastDateFormat.getInstance("K");
        Calendar cal = new GregorianCalendar(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0);
        String result = format.format(cal);
        Assert.assertEquals("0", result);
    }

    @Test
    public void testPatternLetterZ() {
        FastDateFormat format = FastDateFormat.getInstance("Z", UTC);
        String result = format.format(testCalendar);
        Assert.assertEquals("+0000", result);
    }

    @Test
    public void testPatternLetterZZ() {
        FastDateFormat format = FastDateFormat.getInstance("ZZ", UTC);
        String result = format.format(testCalendar);
        Assert.assertEquals("+00:00", result);
    }

    @Test
    public void testPatternLetterZShort() {
        FastDateFormat format = FastDateFormat.getInstance("z", UTC, US);
        String result = format.format(testCalendar);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPatternLetterZLong() {
        FastDateFormat format = FastDateFormat.getInstance("zzzz", UTC, US);
        String result = format.format(testCalendar);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPatternLiteralSingleQuote() {
        FastDateFormat format = FastDateFormat.getInstance("'T'HH:mm");
        String result = format.format(testCalendar);
        Assert.assertTrue(result.startsWith("T"));
    }

    @Test
    public void testPatternLiteralString() {
        FastDateFormat format = FastDateFormat.getInstance("'Time:'HH:mm");
        String result = format.format(testCalendar);
        Assert.assertTrue(result.startsWith("Time:"));
    }

    @Test
    public void testPatternEscapedQuote() {
        FastDateFormat format = FastDateFormat.getInstance("''yyyy''");
        String result = format.format(testCalendar);
        Assert.assertTrue(result.startsWith("'"));
        Assert.assertTrue(result.endsWith("'"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        FastDateFormat.getInstance("invalid#");
    }

    @Test
    public void testPairEqualsHashCode() {
        Object key1 = new Pair(Integer.valueOf(1), "test");
        Object key2 = new Pair(Integer.valueOf(1), "test");
        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());
        Assert.assertNotEquals(key1, new Pair(Integer.valueOf(2), "test"));
    }

    @Test
    public void testTimeZoneDisplayKeyEqualsHashCode() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Object key1 = new TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        Object key2 = new TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testFormatWithDifferentLocales() {
        FastDateFormat formatUS = FastDateFormat.getInstance("MMMM", US);
        FastDateFormat formatDE = FastDateFormat.getInstance("MMMM", GERMANY);
        String usResult = formatUS.format(testCalendar);
        String deResult = formatDE.format(testCalendar);
        Assert.assertNotEquals(usResult, deResult);
    }

    @Test
    public void testFormatWithDifferentTimeZones() {
        FastDateFormat formatUTC = FastDateFormat.getInstance("Z", UTC);
        FastDateFormat formatPST = FastDateFormat.getInstance("Z", PST);
        String utcResult = formatUTC.format(testCalendar);
        String pstResult = formatPST.format(testCalendar);
        Assert.assertNotEquals(utcResult, pstResult);
    }

    @Test
    public void testFormatWithDST() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        FastDateFormat format = FastDateFormat.getInstance("z", tz, US);
        Calendar summer = new GregorianCalendar(tz);
        summer.set(2020, Calendar.JULY, 15, 10, 0, 0);
        Calendar winter = new GregorianCalendar(tz);
        winter.set(2020, Calendar.JANUARY, 15, 10, 0, 0);
        String summerResult = format.format(summer);
        String winterResult = format.format(winter);
        Assert.assertNotEquals(summerResult, winterResult);
    }

    @Test
    public void testFormatCalendarNotForced() {
        FastDateFormat format = FastDateFormat.getInstance("z", null, US);
        Calendar cal = new GregorianCalendar(PST);
        cal.set(2020, Calendar.JANUARY, 15, 10, 0, 0);
        String result = format.format(cal);
        Assert.assertTrue(result.contains("PST") || result.contains("PDT"));
    }

    @Test
    public void testFormatCalendarForced() {
        FastDateFormat format = FastDateFormat.getInstance("z", UTC, US);
        Calendar cal = new GregorianCalendar(PST);
        cal.set(2020, Calendar.JANUARY, 15, 10, 0, 0);
        String result = format.format(cal);
        Assert.assertTrue(result.contains("UTC") || result.contains("GMT"));
    }

    @Test
    public void testMaxLengthEstimate() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS Z");
        Assert.assertTrue(format.getMaxLengthEstimate() >= 28);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd", UTC, US);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();
        Assert.assertEquals(original.getPattern(), deserialized.getPattern());
        Assert.assertEquals(original.getTimeZone(), deserialized.getTimeZone());
        Assert.assertEquals(original.getLocale(), deserialized.getLocale());
        String originalResult = original.format(testCalendar);
        String deserializedResult = deserialized.format(testCalendar);
        Assert.assertEquals(originalResult, deserializedResult);
    }
}
