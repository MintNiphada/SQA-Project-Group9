package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
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
    public void testFactoryMethodsAndDefaults() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        Assert.assertNotNull(fdf1);
        Assert.assertEquals(Locale.getDefault(), fdf1.getLocale());
        Assert.assertEquals(TimeZone.getDefault(), fdf1.getTimeZone());
        Assert.assertFalse(fdf1.getTimeZoneOverridesCalendar());

        FastDateFormat fdfPattern = FastDateFormat.getInstance("yyyy-MM-dd");
        Assert.assertEquals("yyyy-MM-dd", fdfPattern.getPattern());

        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.GERMAN;

        FastDateFormat fdfTz = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        Assert.assertEquals(tz, fdfTz.getTimeZone());
        Assert.assertTrue(fdfTz.getTimeZoneOverridesCalendar());

        FastDateFormat fdfLoc = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        Assert.assertEquals(loc, fdfLoc.getLocale());

        FastDateFormat fdfFull = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        Assert.assertEquals(tz, fdfFull.getTimeZone());
        Assert.assertEquals(loc, fdfFull.getLocale());
        Assert.assertTrue(fdfFull.getTimeZoneOverridesCalendar());

        // Cache hit
        FastDateFormat fdfFullCached = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        Assert.assertSame(fdfFull, fdfFullCached);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test
    public void testDateInstanceFactories() {
        FastDateFormat df1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        Assert.assertNotNull(df1);

        FastDateFormat df2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.US);
        Assert.assertEquals(Locale.US, df2.getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        FastDateFormat df3 = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        Assert.assertEquals(tz, df3.getTimeZone());

        FastDateFormat df4 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.UK);
        Assert.assertEquals(tz, df4.getTimeZone());
        Assert.assertEquals(Locale.UK, df4.getLocale());

        // Cache hit
        FastDateFormat df4Cached = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.UK);
        Assert.assertSame(df4, df4Cached);
    }

    @Test
    public void testTimeInstanceFactories() {
        FastDateFormat tf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        Assert.assertNotNull(tf1);

        FastDateFormat tf2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.FRANCE);
        Assert.assertEquals(Locale.FRANCE, tf2.getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT-5");
        FastDateFormat tf3 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, tz);
        Assert.assertEquals(tz, tf3.getTimeZone());

        FastDateFormat tf4 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, Locale.GERMAN);
        Assert.assertEquals(tz, tf4.getTimeZone());
        Assert.assertEquals(Locale.GERMAN, tf4.getLocale());

        FastDateFormat tf4Cached = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, Locale.GERMAN);
        Assert.assertSame(tf4, tf4Cached);
    }

    @Test
    public void testDateTimeInstanceFactories() {
        FastDateFormat dtf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        Assert.assertNotNull(dtf1);

        FastDateFormat dtf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, Locale.ITALY);
        Assert.assertEquals(Locale.ITALY, dtf2.getLocale());

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat dtf3 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, tz);
        Assert.assertEquals(tz, dtf3.getTimeZone());

        FastDateFormat dtf4 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, Locale.JAPAN);
        Assert.assertEquals(tz, dtf4.getTimeZone());
        Assert.assertEquals(Locale.JAPAN, dtf4.getLocale());

        FastDateFormat dtf4Cached = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, Locale.JAPAN);
        Assert.assertSame(dtf4, dtf4Cached);
    }

    @Test
    public void testFormattingDateAndCalendarAndLong() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", gmt, Locale.US);

        Calendar cal = Calendar.getInstance(gmt);
        cal.set(2023, Calendar.MARCH, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();
        long millis = date.getTime();

        String expected = "2023-03-15 13:45:30.123";

        Assert.assertEquals(expected, format.format(date));
        Assert.assertEquals(expected, format.format(cal));
        Assert.assertEquals(expected, format.format(millis));

        StringBuffer sbDate = new StringBuffer();
        Assert.assertSame(sbDate, format.format(date, sbDate));
        Assert.assertEquals(expected, sbDate.toString());

        StringBuffer sbCal = new StringBuffer();
        Assert.assertSame(sbCal, format.format(cal, sbCal));
        Assert.assertEquals(expected, sbCal.toString());

        StringBuffer sbMillis = new StringBuffer();
        Assert.assertSame(sbMillis, format.format(millis, sbMillis));
        Assert.assertEquals(expected, sbMillis.toString());

        // Object format
        StringBuffer sbObj1 = new StringBuffer();
        Assert.assertSame(sbObj1, format.format((Object) date, sbObj1, new FieldPosition(0)));
        Assert.assertEquals(expected, sbObj1.toString());

        StringBuffer sbObj2 = new StringBuffer();
        Assert.assertSame(sbObj2, format.format((Object) cal, sbObj2, new FieldPosition(0)));
        Assert.assertEquals(expected, sbObj2.toString());

        StringBuffer sbObj3 = new StringBuffer();
        Assert.assertSame(sbObj3, format.format(Long.valueOf(millis), sbObj3, new FieldPosition(0)));
        Assert.assertEquals(expected, sbObj3.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatInvalidObjectNull() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatInvalidObjectType() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format("not a date", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testFormatAllPatternRules() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        Calendar cal = Calendar.getInstance(tz, Locale.US);
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.JANUARY); // 0
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 0); // Midnight
        cal.set(Calendar.MINUTE, 7);
        cal.set(Calendar.SECOND, 9);
        cal.set(Calendar.MILLISECOND, 4);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

        // ERA 'G'
        FastDateFormat fG = FastDateFormat.getInstance("G", tz, Locale.US);
        Assert.assertEquals("AD", fG.format(cal));

        // Year 'y', 'yy', 'yyyy', 'yyyyy'
        FastDateFormat fy2 = FastDateFormat.getInstance("yy", tz, Locale.US);
        Assert.assertEquals("23", fy2.format(cal));
        FastDateFormat fy1 = FastDateFormat.getInstance("y", tz, Locale.US);
        Assert.assertEquals("23", fy1.format(cal)); // 1-3 digits fall back to TwoDigitYearField
        FastDateFormat fy4 = FastDateFormat.getInstance("yyyy", tz, Locale.US);
        Assert.assertEquals("2023", fy4.format(cal));
        FastDateFormat fy5 = FastDateFormat.getInstance("yyyyy", tz, Locale.US);
        Assert.assertEquals("02023", fy5.format(cal));

        // Month 'M', 'MM', 'MMM', 'MMMM'
        FastDateFormat fM1 = FastDateFormat.getInstance("M", tz, Locale.US);
        Assert.assertEquals("1", fM1.format(cal));
        FastDateFormat fM2 = FastDateFormat.getInstance("MM", tz, Locale.US);
        Assert.assertEquals("01", fM2.format(cal));
        FastDateFormat fM3 = FastDateFormat.getInstance("MMM", tz, Locale.US);
        Assert.assertEquals("Jan", fM3.format(cal));
        FastDateFormat fM4 = FastDateFormat.getInstance("MMMM", tz, Locale.US);
        Assert.assertEquals("January", fM4.format(cal));

        // Day of month 'd', 'dd'
        FastDateFormat fd1 = FastDateFormat.getInstance("d", tz, Locale.US);
        Assert.assertEquals("5", fd1.format(cal));
        FastDateFormat fd2 = FastDateFormat.getInstance("dd", tz, Locale.US);
        Assert.assertEquals("05", fd2.format(cal));

        // Hour 1-12 'h', 'hh' (at 00:07, 12-hour is 12)
        FastDateFormat fh1 = FastDateFormat.getInstance("h", tz, Locale.US);
        Assert.assertEquals("12", fh1.format(cal));
        FastDateFormat fh2 = FastDateFormat.getInstance("hh", tz, Locale.US);
        Assert.assertEquals("12", fh2.format(cal));

        // Hour 0-23 'H', 'HH'
        FastDateFormat fH1 = FastDateFormat.getInstance("H", tz, Locale.US);
        Assert.assertEquals("0", fH1.format(cal));
        FastDateFormat fH2 = FastDateFormat.getInstance("HH", tz, Locale.US);
        Assert.assertEquals("00", fH2.format(cal));

        // Hour 1-24 'k', 'kk' (at 00:07, 24-hour is 24)
        FastDateFormat fk1 = FastDateFormat.getInstance("k", tz, Locale.US);
        Assert.assertEquals("24", fk1.format(cal));
        FastDateFormat fk2 = FastDateFormat.getInstance("kk", tz, Locale.US);
        Assert.assertEquals("24", fk2.format(cal));

        // Hour 0-11 'K', 'KK' (at 00:07, 11-hour is 0)
        FastDateFormat fK1 = FastDateFormat.getInstance("K", tz, Locale.US);
        Assert.assertEquals("0", fK1.format(cal));
        FastDateFormat fK2 = FastDateFormat.getInstance("KK", tz, Locale.US);
        Assert.assertEquals("00", fK2.format(cal));

        // Minute 'm', 'mm'
        FastDateFormat fm1 = FastDateFormat.getInstance("m", tz, Locale.US);
        Assert.assertEquals("7", fm1.format(cal));
        FastDateFormat fm2 = FastDateFormat.getInstance("mm", tz, Locale.US);
        Assert.assertEquals("07", fm2.format(cal));

        // Second 's', 'ss'
        FastDateFormat fs1 = FastDateFormat.getInstance("s", tz, Locale.US);
        Assert.assertEquals("9", fs1.format(cal));
        FastDateFormat fs2 = FastDateFormat.getInstance("ss", tz, Locale.US);
        Assert.assertEquals("09", fs2.format(cal));

        // Millisecond 'S', 'SS', 'SSS'
        FastDateFormat fS1 = FastDateFormat.getInstance("S", tz, Locale.US);
        Assert.assertEquals("4", fS1.format(cal));
        FastDateFormat fS2 = FastDateFormat.getInstance("SS", tz, Locale.US);
        Assert.assertEquals("04", fS2.format(cal));
        FastDateFormat fS3 = FastDateFormat.getInstance("SSS", tz, Locale.US);
        Assert.assertEquals("004", fS3.format(cal));

        // Day of week 'E', 'EEEE'
        FastDateFormat fE1 = FastDateFormat.getInstance("E", tz, Locale.US);
        Assert.assertEquals("Thu", fE1.format(cal));
        FastDateFormat fE4 = FastDateFormat.getInstance("EEEE", tz, Locale.US);
        Assert.assertEquals("Thursday", fE4.format(cal));

        // Day in year 'D', 'DDD'
        FastDateFormat fD = FastDateFormat.getInstance("DDD", tz, Locale.US);
        Assert.assertEquals("005", fD.format(cal));

        // Day of week in month 'F'
        FastDateFormat fF = FastDateFormat.getInstance("F", tz, Locale.US);
        Assert.assertEquals("1", fF.format(cal));

        // Week in year 'w', 'ww'
        FastDateFormat fw = FastDateFormat.getInstance("ww", tz, Locale.US);
        Assert.assertEquals("01", fw.format(cal));

        // Week in month 'W'
        FastDateFormat fW = FastDateFormat.getInstance("W", tz, Locale.US);
        Assert.assertEquals("1", fW.format(cal));

        // AM/PM 'a'
        FastDateFormat fa = FastDateFormat.getInstance("a", tz, Locale.US);
        Assert.assertEquals("AM", fa.format(cal));

        // TimeZone 'z', 'zzzz'
        FastDateFormat fz1 = FastDateFormat.getInstance("z", tz, Locale.US);
        Assert.assertNotNull(fz1.format(cal));
        FastDateFormat fz4 = FastDateFormat.getInstance("zzzz", tz, Locale.US);
        Assert.assertNotNull(fz4.format(cal));

        // TimeZone 'Z', 'ZZ'
        FastDateFormat fZ1 = FastDateFormat.getInstance("Z", tz, Locale.US);
        Assert.assertEquals("-0500", fZ1.format(cal));
        FastDateFormat fZ2 = FastDateFormat.getInstance("ZZ", tz, Locale.US);
        Assert.assertEquals("-05:00", fZ2.format(cal));

        // Quotes and literals
        FastDateFormat fLiteral = FastDateFormat.getInstance("'Date: 'yyyy' / ''special'''", tz, Locale.US);
        Assert.assertEquals("Date: 2023 / 'special'", fLiteral.format(cal));
    }

    @Test
    public void testNumberRuleEdgeCases() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);

        // Test UnpaddedNumberField > 10 and >= 100
        cal.set(Calendar.DAY_OF_YEAR, 8);
        Assert.assertEquals("8", FastDateFormat.getInstance("D", tz).format(cal));
        cal.set(Calendar.DAY_OF_YEAR, 42);
        Assert.assertEquals("42", FastDateFormat.getInstance("D", tz).format(cal));
        cal.set(Calendar.DAY_OF_YEAR, 256);
        Assert.assertEquals("256", FastDateFormat.getInstance("D", tz).format(cal));

        // Test TwoDigitNumberField >= 100
        Assert.assertEquals("256", FastDateFormat.getInstance("DD", tz).format(cal));

        // Test PaddedNumberField < 100, 100..999, >= 1000
        FastDateFormat fPadded5 = FastDateFormat.getInstance("DDDDD", tz);
        cal.set(Calendar.DAY_OF_YEAR, 7);
        Assert.assertEquals("00007", fPadded5.format(cal));
        cal.set(Calendar.DAY_OF_YEAR, 85);
        Assert.assertEquals("00085", fPadded5.format(cal));
        cal.set(Calendar.DAY_OF_YEAR, 320);
        Assert.assertEquals("00320", fPadded5.format(cal));

        FastDateFormat fYear6 = FastDateFormat.getInstance("yyyyyy", tz);
        cal.set(Calendar.YEAR, 2024);
        Assert.assertEquals("002024", fYear6.format(cal));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternComponent() {
        FastDateFormat.getInstance("yyyy-MM-dd Q");
    }

    @Test
    public void testUnforcedTimeZoneRulesAndDST() {
        // Test TimeZoneNameRule when timeZoneForced is false
        FastDateFormat unforcedShort = FastDateFormat.getInstance("z");
        FastDateFormat unforcedLong = FastDateFormat.getInstance("zzzz");
        Assert.assertFalse(unforcedShort.getTimeZoneOverridesCalendar());

        // Construct standard and daylight time calendars
        SimpleTimeZone stz = new SimpleTimeZone(
                -5 * 3600000, "America/New_York",
                Calendar.MARCH, 2, Calendar.SUNDAY, 2 * 3600000,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 2 * 3600000,
                3600000);

        Calendar calStandard = Calendar.getInstance(stz, Locale.US);
        calStandard.set(2023, Calendar.JANUARY, 15, 12, 0, 0);

        Calendar calDaylight = Calendar.getInstance(stz, Locale.US);
        calDaylight.set(2023, Calendar.JUNE, 15, 12, 0, 0);

        Assert.assertNotNull(unforcedShort.format(calStandard));
        Assert.assertNotNull(unforcedShort.format(calDaylight));
        Assert.assertNotNull(unforcedLong.format(calStandard));
        Assert.assertNotNull(unforcedLong.format(calDaylight));

        // TimeZone forced with DST
        FastDateFormat forcedShort = FastDateFormat.getInstance("z", stz, Locale.US);
        FastDateFormat forcedLong = FastDateFormat.getInstance("zzzz", stz, Locale.US);
        Assert.assertNotNull(forcedShort.format(calStandard));
        Assert.assertNotNull(forcedShort.format(calDaylight));
        Assert.assertNotNull(forcedLong.format(calStandard));
        Assert.assertNotNull(forcedLong.format(calDaylight));

        // TimeZone positive offset in TimeZoneNumberRule
        TimeZone plusTz = TimeZone.getTimeZone("GMT+08:00");
        Calendar calPlus = Calendar.getInstance(plusTz, Locale.US);
        Assert.assertEquals("+0800", FastDateFormat.getInstance("Z", plusTz).format(calPlus));
        Assert.assertEquals("+08:00", FastDateFormat.getInstance("ZZ", plusTz).format(calPlus));
    }

    @Test
    public void testTwelveAndTwentyFourHourNonZero() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(Calendar.HOUR_OF_DAY, 15); // 3 PM
        FastDateFormat f12 = FastDateFormat.getInstance("h", tz);
        FastDateFormat f24 = FastDateFormat.getInstance("k", tz);
        Assert.assertEquals("3", f12.format(cal));
        Assert.assertEquals("15", f24.format(cal));
    }

    @Test
    public void testParseObjectNotSupported() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Object result = fdf.parseObject("2023", pos);
        Assert.assertNull(result);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(0, pos.getErrorIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f3 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat f4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.UK);
        FastDateFormat f5 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f6 = FastDateFormat.getInstance("yyyy-MM-dd");

        Assert.assertEquals(f1, f1);
        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("different object"));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));
        Assert.assertFalse(f1.equals(f5));
        Assert.assertFalse(f1.equals(f6)); // f6 has timeZoneForced = false

        Assert.assertTrue(f1.toString().contains("yyyy-MM-dd"));
        Assert.assertTrue(f1.getMaxLengthEstimate() > 0);
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
        Date now = new Date();
        Assert.assertEquals(fdf.format(now), deserialized.format(now));
    }
}
