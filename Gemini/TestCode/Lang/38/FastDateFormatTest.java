package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class FastDateFormatTest {

    @Test
    public void testGetInstance() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        Assert.assertSame(fdf1, fdf2);
        Assert.assertNotNull(fdf1.getPattern());

        FastDateFormat fdfPattern = FastDateFormat.getInstance("yyyy-MM-dd");
        Assert.assertEquals("yyyy-MM-dd", fdfPattern.getPattern());
        Assert.assertEquals(TimeZone.getDefault(), fdfPattern.getTimeZone());
        Assert.assertEquals(Locale.getDefault(), fdfPattern.getLocale());
        Assert.assertFalse(fdfPattern.getTimeZoneOverridesCalendar());

        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdfTz = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        Assert.assertEquals(tz, fdfTz.getTimeZone());
        Assert.assertTrue(fdfTz.getTimeZoneOverridesCalendar());

        Locale loc = Locale.GERMAN;
        FastDateFormat fdfLoc = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        Assert.assertEquals(loc, fdfLoc.getLocale());

        FastDateFormat fdfAll = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        Assert.assertEquals("yyyy-MM-dd", fdfAll.getPattern());
        Assert.assertEquals(tz, fdfAll.getTimeZone());
        Assert.assertEquals(loc, fdfAll.getLocale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNullPattern() {
        FastDateFormat.getInstance(null, TimeZone.getDefault(), Locale.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceInvalidPattern() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        Assert.assertNotNull(fdf1);

        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.US);
        Assert.assertNotNull(fdf2);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf3 = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        Assert.assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.UK);
        Assert.assertEquals(tz, fdf4.getTimeZone());
        Assert.assertEquals(Locale.UK, fdf4.getLocale());
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        Assert.assertNotNull(fdf1);

        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.US);
        Assert.assertNotNull(fdf2);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf3 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, tz);
        Assert.assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, Locale.UK);
        Assert.assertEquals(tz, fdf4.getTimeZone());
        Assert.assertEquals(Locale.UK, fdf4.getLocale());
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        Assert.assertNotNull(fdf1);

        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG, Locale.US);
        Assert.assertNotNull(fdf2);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf3 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT, tz);
        Assert.assertEquals(tz, fdf3.getTimeZone());

        FastDateFormat fdf4 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, Locale.UK);
        Assert.assertEquals(tz, fdf4.getTimeZone());
        Assert.assertEquals(Locale.UK, fdf4.getLocale());
    }

    @Test
    public void testFormatPatternsAllRules() {
        Calendar cal = new GregorianCalendar(2004, Calendar.DECEMBER, 31, 0, 5, 9);
        cal.set(Calendar.MILLISECOND, 7);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy yy MMMM MMM MM M d h H m s S EEEE E D F w W a k K z zzzz Z ZZ '' 'test' 'x'", TimeZone.getTimeZone("GMT"), Locale.US);
        String formatted = fdf.format(cal);
        Assert.assertTrue(formatted.contains("AD"));
        Assert.assertTrue(formatted.contains("2004"));
        Assert.assertTrue(formatted.contains("04"));
        Assert.assertTrue(formatted.contains("December"));
        Assert.assertTrue(formatted.contains("Dec"));
        Assert.assertTrue(formatted.contains("12"));
        Assert.assertTrue(formatted.contains("31"));
        Assert.assertTrue(formatted.contains("12"));
        Assert.assertTrue(formatted.contains("00"));
        Assert.assertTrue(formatted.contains("05"));
        Assert.assertTrue(formatted.contains("09"));
        Assert.assertTrue(formatted.contains("007") || formatted.contains("7"));
        Assert.assertTrue(formatted.contains("Friday"));
        Assert.assertTrue(formatted.contains("Fri"));
        Assert.assertTrue(formatted.contains("AM"));
        Assert.assertTrue(formatted.contains("24"));
        Assert.assertTrue(formatted.contains("' test x"));
        Assert.assertTrue(formatted.contains("+0000"));
        Assert.assertTrue(formatted.contains("+00:00"));
    }

    @Test
    public void testNumberRulesAndPadding() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyy-yyyy-yy-y-d-dd-ddd-dddd-H-HH-HHH-S-SS-SSS-SSSS", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(2023, Calendar.JANUARY, 5, 8, 9, 2);
        cal.set(Calendar.MILLISECOND, 45);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        String res = fdf.format(cal);
        Assert.assertEquals("02023-2023-23-23-5-05-005-0005-8-08-008-45-45-045-0045", res);

        cal.set(Calendar.DAY_OF_MONTH, 25);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MILLISECOND, 500);
        String res2 = fdf.format(cal);
        Assert.assertEquals("02023-2023-23-23-25-25-025-0025-18-18-018-500-500-500-0500", res2);
    }

    @Test
    public void testTwelveHourAndTwentyFourHourField() {
        FastDateFormat fdf = FastDateFormat.getInstance("h-H-k-K", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(2023, Calendar.JANUARY, 1, 0, 0, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        Assert.assertEquals("12-0-24-0", fdf.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12);
        Assert.assertEquals("12-12-12-0", fdf.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 13);
        Assert.assertEquals("1-13-13-1", fdf.format(cal));
    }

    @Test
    public void testTimeZoneRuleOutputs() {
        SimpleTimeZone negTz = new SimpleTimeZone(-5 * 3600 * 1000, "EST");
        FastDateFormat fdfNeg = FastDateFormat.getInstance("Z ZZ z zzzz", negTz, Locale.US);
        Calendar cal = new GregorianCalendar(negTz);
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        String res = fdfNeg.format(cal);
        Assert.assertTrue(res.startsWith("-0500 -05:00"));

        FastDateFormat fdfUnforced = FastDateFormat.getInstance("z zzzz");
        Calendar calDefault = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calDefault.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        String resUnforced = fdfUnforced.format(calDefault);
        Assert.assertNotNull(resUnforced);

        SimpleTimeZone dstTz = new SimpleTimeZone(0, "DST-TEST", Calendar.MARCH, 1, 0, 0, Calendar.OCTOBER, 1, 0, 0, 3600 * 1000);
        FastDateFormat fdfDstForced = FastDateFormat.getInstance("z zzzz", dstTz, Locale.US);
        Calendar calDst = new GregorianCalendar(dstTz, Locale.US);
        calDst.set(2023, Calendar.JULY, 1, 12, 0, 0);
        String dstForcedRes = fdfDstForced.format(calDst);
        Assert.assertNotNull(dstForcedRes);

        FastDateFormat fdfDstUnforced = FastDateFormat.getInstance("z zzzz");
        String dstUnforcedRes = fdfDstUnforced.format(calDst);
        Assert.assertNotNull(dstUnforcedRes);
    }

    @Test
    public void testVariousFormatSignatures() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        Date now = new Date(1672531199000L);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(now);

        String expected = "2022-12-31 23:59:59";
        Assert.assertEquals(expected, fdf.format(1672531199000L));
        Assert.assertEquals(expected, fdf.format(now));
        Assert.assertEquals(expected, fdf.format(cal));

        StringBuffer sb1 = new StringBuffer();
        fdf.format(1672531199000L, sb1);
        Assert.assertEquals(expected, sb1.toString());

        StringBuffer sb2 = new StringBuffer();
        fdf.format(now, sb2);
        Assert.assertEquals(expected, sb2.toString());

        StringBuffer sb3 = new StringBuffer();
        fdf.format(cal, sb3);
        Assert.assertEquals(expected, sb3.toString());

        StringBuffer sb4 = new StringBuffer();
        fdf.format((Object) now, sb4, new FieldPosition(0));
        Assert.assertEquals(expected, sb4.toString());

        StringBuffer sb5 = new StringBuffer();
        fdf.format((Object) cal, sb5, new FieldPosition(0));
        Assert.assertEquals(expected, sb5.toString());

        StringBuffer sb6 = new StringBuffer();
        fdf.format(Long.valueOf(1672531199000L), sb6, new FieldPosition(0));
        Assert.assertEquals(expected, sb6.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectInvalid() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("2023", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testParseObjectNotSupported() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(10);
        pos.setErrorIndex(5);
        Object obj = fdf.parseObject("2023-01-01", pos);
        Assert.assertNull(obj);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.UK);
        FastDateFormat fdf5 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfUnforced = FastDateFormat.getInstance("yyyy-MM-dd");

        Assert.assertEquals(fdf1, fdf2);
        Assert.assertEquals(fdf1.hashCode(), fdf2.hashCode());
        Assert.assertEquals(fdf1, fdf1);
        Assert.assertNotEquals(fdf1, fdf3);
        Assert.assertNotEquals(fdf1, fdf4);
        Assert.assertNotEquals(fdf1, fdf5);
        Assert.assertNotEquals(fdf1, fdfUnforced);
        Assert.assertNotEquals(fdf1, "some string");
        Assert.assertNotEquals(fdf1, null);
    }

    @Test
    public void testToStringAndMaxLengthEstimate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Assert.assertEquals("FastDateFormat[yyyy-MM-dd HH:mm:ss]", fdf.toString());
        Assert.assertTrue(fdf.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testSerialization() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        Assert.assertEquals(fdf, deserialized);
        Assert.assertEquals(fdf.format(1672531199000L), deserialized.format(1672531199000L));
    }

    @Test
    public void testTimeZoneDisplayKeyAndCache() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        String name1 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.LONG, Locale.US);
        String name2 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.LONG, Locale.US);
        Assert.assertEquals(name1, name2);
        String name3 = FastDateFormat.getTimeZoneDisplay(tz, true, TimeZone.LONG, Locale.US);
        Assert.assertNotNull(name3);
    }

    @Test
    public void testParseTokenLiteralEscapes() {
        FastDateFormat fdf = FastDateFormat.getInstance("''yyyy'' '''' 'o''clock'");
        Calendar cal = new GregorianCalendar(2023, Calendar.JANUARY, 1);
        String result = fdf.format(cal);
        Assert.assertEquals("'2023' '' o'clock", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPaddedNumberFieldIllegal() {
        FastDateFormat fdf = new FastDateFormat("yyyy", null, null) {
            private static final long serialVersionUID = 1L;
            {
                selectNumberRule(Calendar.YEAR, 2);
                selectNumberRule(Calendar.YEAR, 1);
                selectNumberRule(Calendar.YEAR, 0);
            }
        };
        Assert.assertNotNull(fdf);
    }
}
