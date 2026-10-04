package org.apache.commons.lang.time;

import org.junit.Test;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDateFormatTest {

    @Test
    public void testGetInstanceDefaults() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertNotNull(fdf1);
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertNotNull(fdf1.getPattern());
        assertEquals(TimeZone.getDefault(), fdf1.getTimeZone());
        assertEquals(Locale.getDefault(), fdf1.getLocale());
        assertFalse(fdf1.getTimeZoneOverridesCalendar());
    }

    @Test
    public void testGetInstanceWithParameters() {
        TimeZone tz = TimeZone.getTimeZone("GMT-5");
        Locale loc = Locale.GERMANY;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        assertEquals("yyyy-MM-dd", fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(loc, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());

        FastDateFormat fdfCached = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        assertTrue(fdf == fdfCached);

        FastDateFormat fdfPatternOnly = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(fdfPatternOnly);

        FastDateFormat fdfPatternTz = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertEquals(tz, fdfPatternTz.getTimeZone());

        FastDateFormat fdfPatternLoc = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        assertEquals(loc, fdfPatternLoc.getLocale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceInvalidPattern() {
        FastDateFormat.getInstance("invalidPattern");
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertNotNull(fdf1);

        Locale loc = Locale.FRENCH;
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, loc);
        assertEquals(loc, fdf2.getLocale());

        FastDateFormat fdf3 = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, loc);
        assertEquals(tz, fdf4.getTimeZone());
        assertEquals(loc, fdf4.getLocale());

        FastDateFormat fdfCached = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, loc);
        assertTrue(fdf4 == fdfCached);
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertNotNull(fdf1);

        Locale loc = Locale.JAPAN;
        TimeZone tz = TimeZone.getTimeZone("GMT+9");
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, loc);
        assertEquals(loc, fdf2.getLocale());

        FastDateFormat fdf3 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, tz);
        assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, loc);
        assertEquals(tz, fdf4.getTimeZone());
        assertEquals(loc, fdf4.getLocale());

        FastDateFormat fdfCached = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, loc);
        assertTrue(fdf4 == fdfCached);
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertNotNull(fdf1);

        Locale loc = Locale.UK;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG, loc);
        assertEquals(loc, fdf2.getLocale());

        FastDateFormat fdf3 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT, tz);
        assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, loc);
        assertEquals(tz, fdf4.getTimeZone());
        assertEquals(loc, fdf4.getLocale());

        FastDateFormat fdfCached = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, loc);
        assertTrue(fdf4 == fdfCached);
    }

    @Test
    public void testFormatTokens() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz, Locale.US);
        cal.clear();
        cal.set(2004, Calendar.FEBRUARY, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);

        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy yy M MMM MMMM d h H m s S E EEEE D F w W a k K z zzzz Z ZZ '' 'literal'", tz, Locale.US);
        String formatted = fdf.format(cal);
        assertTrue(formatted.contains("AD"));
        assertTrue(formatted.contains("2004"));
        assertTrue(formatted.contains("04"));
        assertTrue(formatted.contains("Feb"));
        assertTrue(formatted.contains("February"));
        assertTrue(formatted.contains("Tue"));
        assertTrue(formatted.contains("Tuesday"));
        assertTrue(formatted.contains("literal"));
        assertTrue(formatted.contains("'"));
    }

    @Test
    public void testNumberRulePadding() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);
        cal.clear();
        cal.set(Calendar.YEAR, 5);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 5);

        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertEquals("0005-01-05", fdf.format(cal));

        cal.set(Calendar.YEAR, 2023);
        fdf = FastDateFormat.getInstance("yyyyy-yyyy-yyy-yy-y", tz);
        assertEquals("02023-2023-2023-23-23", fdf.format(cal));

        cal.set(Calendar.YEAR, 8);
        fdf = FastDateFormat.getInstance("yyyyy-yyyy-yyy-yy-y", tz);
        assertEquals("00008-0008-008-08-08", fdf.format(cal));

        cal.set(Calendar.YEAR, 88);
        fdf = FastDateFormat.getInstance("yyyyy-yyyy-yyy-yy-y", tz);
        assertEquals("00088-0088-088-88-88", fdf.format(cal));

        cal.set(Calendar.YEAR, 888);
        fdf = FastDateFormat.getInstance("yyyyy-yyyy-yyy-yy-y", tz);
        assertEquals("00888-0888-888-88-88", fdf.format(cal));
    }

    @Test
    public void testHourFieldsMidnightAndNoon() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        FastDateFormat fdf = FastDateFormat.getInstance("h-H-k-K", tz);
        assertEquals("12-0-24-0", fdf.format(cal));

        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("12-12-12-0", fdf.format(cal));
    }

    @Test
    public void testTimeZoneTokens() {
        SimpleTimeZone customTz = new SimpleTimeZone(
                -5 * 3600000, "Custom",
                Calendar.MARCH, 8, -Calendar.SUNDAY, 2 * 3600000,
                Calendar.NOVEMBER, 1, -Calendar.SUNDAY, 2 * 3600000,
                3600000
        );

        Calendar cal = Calendar.getInstance(customTz);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        FastDateFormat fdfZ = FastDateFormat.getInstance("Z ZZ z zzzz", customTz, Locale.US);
        assertEquals("-0500 -05:00 GMT-05:00 GMT-05:00", fdfZ.format(cal));

        cal.set(2023, Calendar.JULY, 1, 12, 0, 0);
        assertEquals("-0400 -04:00 GMT-04:00 GMT-04:00", fdfZ.format(cal));

        FastDateFormat fdfUnforced = FastDateFormat.getInstance("z zzzz Z ZZ", Locale.US);
        String formatted = fdfUnforced.format(cal);
        assertNotNull(formatted);
    }

    @Test
    public void testFormatMethods() {
        Date date = new Date(100000000000L);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        FastDateFormat fdf = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss");
        assertEquals(fdf.format(date), fdf.format(100000000000L));
        assertEquals(fdf.format(date), fdf.format((Object) date, new StringBuffer(), new FieldPosition(0)).toString());
        assertEquals(fdf.format(cal), fdf.format((Object) cal, new StringBuffer(), new FieldPosition(0)).toString());
        assertEquals(fdf.format(100000000000L), fdf.format((Object) new Long(100000000000L), new StringBuffer(), new FieldPosition(0)).toString());

        StringBuffer sb = new StringBuffer();
        fdf.format(100000000000L, sb);
        assertEquals(fdf.format(date), sb.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatInvalidObject() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format("invalid", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNullObject() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format(null, new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testParseObject() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        ParsePosition pos = new ParsePosition(5);
        Object result = fdf.parseObject("2023-01-01", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("EST");
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy", tz1, Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy", tz1, Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy", tz2, Locale.US);
        FastDateFormat fdf4 = FastDateFormat.getInstance("MM", tz1, Locale.US);
        FastDateFormat fdf5 = FastDateFormat.getInstance("yyyy", tz1, Locale.GERMANY);
        FastDateFormat fdf6 = FastDateFormat.getInstance("yyyy");

        assertTrue(fdf1.equals(fdf1));
        assertTrue(fdf1.equals(fdf2));
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("string"));
        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(fdf4));
        assertFalse(fdf1.equals(fdf5));
        assertFalse(fdf1.equals(fdf6));
    }

    @Test
    public void testToStringAndMaxLengthEstimate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf.toString());
        assertTrue(fdf.getMaxLengthEstimate() >= 10);
    }

    @Test
    public void testSingleQuoteInPattern() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("''");
        assertEquals("'", fdf1.format(new Date()));

        FastDateFormat fdf2 = FastDateFormat.getInstance("'h'");
        assertEquals("h", fdf2.format(new Date()));

        FastDateFormat fdf3 = FastDateFormat.getInstance("'hello'");
        assertEquals("hello", fdf3.format(new Date()));
    }
}
