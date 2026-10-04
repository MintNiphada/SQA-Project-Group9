package org.apache.commons.lang.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    @Test
    public void testFactoryGetInstances() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        Assert.assertSame(fdf1, fdf2);
        Assert.assertNotNull(fdf1.getPattern());

        FastDateFormat fdfPattern = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdfPatternTz = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"));
        FastDateFormat fdfPatternLoc = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        FastDateFormat fdfPatternAll = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);

        Assert.assertEquals("yyyy-MM-dd", fdfPattern.getPattern());
        Assert.assertEquals(TimeZone.getTimeZone("GMT"), fdfPatternTz.getTimeZone());
        Assert.assertEquals(Locale.US, fdfPatternLoc.getLocale());
        Assert.assertEquals(TimeZone.getTimeZone("GMT"), fdfPatternAll.getTimeZone());
        Assert.assertEquals(Locale.US, fdfPatternAll.getLocale());
    }

    @Test
    public void testDateInstances() {
        FastDateFormat f1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT"));
        FastDateFormat f4 = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);

        Assert.assertNotNull(f1);
        Assert.assertNotNull(f2);
        Assert.assertNotNull(f3);
        Assert.assertNotNull(f4);
    }

    @Test
    public void testTimeInstances() {
        FastDateFormat f1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.US);
        FastDateFormat f3 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT"));
        FastDateFormat f4 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);

        Assert.assertNotNull(f1);
        Assert.assertNotNull(f2);
        Assert.assertNotNull(f3);
        Assert.assertNotNull(f4);
    }

    @Test
    public void testDateTimeInstances() {
        FastDateFormat f1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, TimeZone.getTimeZone("GMT"));
        FastDateFormat f4 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);

        Assert.assertNotNull(f1);
        Assert.assertNotNull(f2);
        Assert.assertNotNull(f3);
        Assert.assertNotNull(f4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPatternComponent() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    @Test
    public void testPatternFormattingAllRules() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 7);
        cal.set(Calendar.SECOND, 9);
        cal.set(Calendar.MILLISECOND, 23);

        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy yy MMMM MMM MM M d h H m s SSSS EEEE E D F w W a k K z zzzz Z ZZ '' 'text'", TimeZone.getTimeZone("GMT"), Locale.US);
        String formatted = fdf.format(cal);

        Assert.assertTrue(formatted.contains("AD"));
        Assert.assertTrue(formatted.contains("2008"));
        Assert.assertTrue(formatted.contains("08"));
        Assert.assertTrue(formatted.contains("February"));
        Assert.assertTrue(formatted.contains("Feb"));
        Assert.assertTrue(formatted.contains("02"));
        Assert.assertTrue(formatted.contains(" 2 "));
        Assert.assertTrue(formatted.contains("5"));
        Assert.assertTrue(formatted.contains("12"));
        Assert.assertTrue(formatted.contains("00"));
        Assert.assertTrue(formatted.contains("07"));
        Assert.assertTrue(formatted.contains("09"));
        Assert.assertTrue(formatted.contains("0023"));
        Assert.assertTrue(formatted.contains("Tuesday"));
        Assert.assertTrue(formatted.contains("Tue"));
        Assert.assertTrue(formatted.contains("AM"));
        Assert.assertTrue(formatted.contains("24"));
        Assert.assertTrue(formatted.contains("0"));
        Assert.assertTrue(formatted.contains("GMT"));
        Assert.assertTrue(formatted.contains("+0000"));
        Assert.assertTrue(formatted.contains("+00:00"));
        Assert.assertTrue(formatted.contains("'"));
        Assert.assertTrue(formatted.contains("text"));
    }

    @Test
    public void testNumericPaddingBranches() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2008, Calendar.DECEMBER, 25, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 8);

        FastDateFormat fdf1 = FastDateFormat.getInstance("yyy d H m s S SS SSS SSSS SSSSS", TimeZone.getTimeZone("GMT"), Locale.US);
        String res1 = fdf1.format(cal);
        Assert.assertTrue(res1.contains("25"));
        Assert.assertTrue(res1.contains("08"));
        Assert.assertTrue(res1.contains("008"));
        Assert.assertTrue(res1.contains("0008"));
        Assert.assertTrue(res1.contains("00008"));

        cal.set(Calendar.MILLISECOND, 999);
        FastDateFormat fdf2 = FastDateFormat.getInstance("SS SSS SSSS", TimeZone.getTimeZone("GMT"), Locale.US);
        String res2 = fdf2.format(cal);
        Assert.assertTrue(res2.contains("999"));
        Assert.assertTrue(res2.contains("0999"));

        cal.set(Calendar.DAY_OF_YEAR, 366);
        FastDateFormat fdf3 = FastDateFormat.getInstance("DDDD", TimeZone.getTimeZone("GMT"), Locale.US);
        String res3 = fdf3.format(cal);
        Assert.assertEquals("0366", res3);
    }

    @Test
    public void testHourVariations() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        FastDateFormat fdf = FastDateFormat.getInstance("h K H k", TimeZone.getTimeZone("GMT"), Locale.US);
        Assert.assertEquals("12 0 12 12", fdf.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 0);
        Assert.assertEquals("12 0 0 24", fdf.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 23);
        Assert.assertEquals("11 11 23 23", fdf.format(cal));
    }

    @Test
    public void testTimeZoneOffsets() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"), Locale.US);
        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);

        FastDateFormat fdfNoColon = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("GMT+08:00"), Locale.US);
        FastDateFormat fdfColon = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT+08:00"), Locale.US);
        Assert.assertEquals("+0800", fdfNoColon.format(cal1));
        Assert.assertEquals("+08:00", fdfColon.format(cal1));

        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT-05:30"), Locale.US);
        cal2.set(2020, Calendar.JANUARY, 1, 0, 0, 0);

        FastDateFormat fdfNegativeNoColon = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("GMT-05:30"), Locale.US);
        FastDateFormat fdfNegativeColon = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT-05:30"), Locale.US);
        Assert.assertEquals("-0530", fdfNegativeNoColon.format(cal2));
        Assert.assertEquals("-05:30", fdfNegativeColon.format(cal2));
    }

    @Test
    public void testTimeZoneNameDaylight() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Calendar calStandard = Calendar.getInstance(tz, Locale.US);
        calStandard.set(2020, Calendar.JANUARY, 1, 12, 0, 0);

        Calendar calDaylight = Calendar.getInstance(tz, Locale.US);
        calDaylight.set(2020, Calendar.JULY, 1, 12, 0, 0);

        FastDateFormat fdfShort = FastDateFormat.getInstance("z", tz, Locale.US);
        FastDateFormat fdfLong = FastDateFormat.getInstance("zzzz", tz, Locale.US);

        Assert.assertEquals("EST", fdfShort.format(calStandard));
        Assert.assertEquals("EDT", fdfShort.format(calDaylight));
        Assert.assertEquals("Eastern Standard Time", fdfLong.format(calStandard));
        Assert.assertEquals("Eastern Daylight Time", fdfLong.format(calDaylight));

        FastDateFormat fdfUnforced = FastDateFormat.getInstance("z");
        Assert.assertFalse(fdfUnforced.getTimeZoneOverridesCalendar());
        Assert.assertEquals("EST", fdfUnforced.format(calStandard));
        Assert.assertEquals("EDT", fdfUnforced.format(calDaylight));
    }

    @Test
    public void testFormatOverloads() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        Date date = new Date(1577836800000L);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);

        Assert.assertEquals("2020-01-01 00:00:00", fdf.format(date));
        Assert.assertEquals("2020-01-01 00:00:00", fdf.format(1577836800000L));
        Assert.assertEquals("2020-01-01 00:00:00", fdf.format(cal));

        StringBuffer sb1 = new StringBuffer("Prefix: ");
        Assert.assertEquals("Prefix: 2020-01-01 00:00:00", fdf.format(date, sb1).toString());

        StringBuffer sb2 = new StringBuffer("Prefix: ");
        Assert.assertEquals("Prefix: 2020-01-01 00:00:00", fdf.format(1577836800000L, sb2).toString());

        StringBuffer sb3 = new StringBuffer("Prefix: ");
        Assert.assertEquals("Prefix: 2020-01-01 00:00:00", fdf.format(cal, sb3).toString());

        StringBuffer sb4 = new StringBuffer();
        Assert.assertEquals("2020-01-01 00:00:00", fdf.format((Object) date, sb4, new FieldPosition(0)).toString());

        StringBuffer sb5 = new StringBuffer();
        Assert.assertEquals("2020-01-01 00:00:00", fdf.format((Object) cal, sb5, new FieldPosition(0)).toString());

        StringBuffer sb6 = new StringBuffer();
        Assert.assertEquals("2020-01-01 00:00:00", fdf.format((Object) Long.valueOf(1577836800000L), sb6, new FieldPosition(0)).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnknownObjectClass() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("2020", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNullObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testParseObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(10);
        Object res = fdf.parseObject("2020-01-01", pos);
        Assert.assertNull(res);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testEqualsAndHashCodeAndToString() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f3 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat f5 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.GERMANY);

        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());
        Assert.assertNotEquals(f1, f3);
        Assert.assertNotEquals(f1, f4);
        Assert.assertNotEquals(f1, f5);
        Assert.assertNotEquals(f1, "NotAFastDateFormat");
        Assert.assertNotEquals(f1, null);

        Assert.assertEquals("FastDateFormat[yyyy-MM-dd]", f1.toString());
        Assert.assertTrue(f1.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testSerialization() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"), Locale.US);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        Assert.assertEquals(fdf, deserialized);
        Date date = new Date(1577836800000L);
        Assert.assertEquals(fdf.format(date), deserialized.format(date));
    }

    @Test
    public void testQuoteHandlingInPattern() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("''yyyy''", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(Calendar.YEAR, 2020);
        Assert.assertEquals("'2020'", fdf1.format(cal));

        FastDateFormat fdf2 = FastDateFormat.getInstance("'Year: 'yyyy", TimeZone.getTimeZone("GMT"), Locale.US);
        Assert.assertEquals("Year: 2020", fdf2.format(cal));
    }
}
