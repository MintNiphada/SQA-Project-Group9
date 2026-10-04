package org.apache.commons.lang.time;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class DurationFormatUtilsTest {

    @Test
    public void testConstructor() {
        DurationFormatUtils utils = new DurationFormatUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testFormatDurationHMS() {
        long duration = 1000 * (3600 * 5 + 60 * 4 + 3) + 21;
        String result = DurationFormatUtils.formatDurationHMS(duration);
        Assert.assertEquals("5:04:03.021", result);
    }

    @Test
    public void testFormatDurationISO() {
        long duration = 1000 * (86400 * 7 + 3600 * 6 + 60 * 5 + 4) + 321;
        String result = DurationFormatUtils.formatDurationISO(duration);
        Assert.assertEquals("P0000Y0M7DT6H5M4.321S", result);
    }

    @Test
    public void testFormatDurationWithFormat() {
        long duration = 1000 * (86400 * 2 + 3600 * 3 + 60 * 4 + 5) + 6;
        Assert.assertEquals("02 03 04 05 006", DurationFormatUtils.formatDuration(duration, "dd HH mm ss SSS"));
        Assert.assertEquals("2 3 4 5 6", DurationFormatUtils.formatDuration(duration, "d H m s S", false));
    }

    @Test
    public void testFormatDurationMillisecondsAfterSeconds() {
        long duration = 1506;
        Assert.assertEquals("1 506", DurationFormatUtils.formatDuration(duration, "s SSS", true));
        Assert.assertEquals("1 506", DurationFormatUtils.formatDuration(duration, "s S", false));
        Assert.assertEquals("1506", DurationFormatUtils.formatDuration(duration, "S", false));
        Assert.assertEquals("01506", DurationFormatUtils.formatDuration(duration, "SSSSS", true));
    }

    @Test
    public void testFormatDurationLiterals() {
        long duration = 1000 * 60;
        Assert.assertEquals("Time: 1 mins", DurationFormatUtils.formatDuration(duration, "'Time: 'm' mins'"));
    }

    @Test
    public void testFormatDurationWordsAllTrue() {
        long duration = 0;
        Assert.assertEquals("", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000;
        Assert.assertEquals("1 second", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000 * 60;
        Assert.assertEquals("1 minute", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000 * 3600;
        Assert.assertEquals("1 hour", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000 * 86400;
        Assert.assertEquals("1 day", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000 * (86400 * 2 + 3600 * 2 + 60 * 2 + 2);
        Assert.assertEquals("2 days 2 hours 2 minutes 2 seconds", DurationFormatUtils.formatDurationWords(duration, true, true));
    }

    @Test
    public void testFormatDurationWordsAllFalse() {
        long duration = 0;
        Assert.assertEquals("0 days 0 hours 0 minutes 0 seconds", DurationFormatUtils.formatDurationWords(duration, false, false));

        duration = 1000 * (86400 + 3600 + 60 + 1);
        Assert.assertEquals("1 day 1 hour 1 minute 1 second", DurationFormatUtils.formatDurationWords(duration, false, false));
    }

    @Test
    public void testFormatDurationWordsSuppressLeadingOnly() {
        long duration = 1000 * 60;
        Assert.assertEquals("1 minute 0 seconds", DurationFormatUtils.formatDurationWords(duration, true, false));

        duration = 1000 * 3600;
        Assert.assertEquals("1 hour 0 minutes 0 seconds", DurationFormatUtils.formatDurationWords(duration, true, false));

        duration = 1000 * 86400;
        Assert.assertEquals("1 day 0 hours 0 minutes 0 seconds", DurationFormatUtils.formatDurationWords(duration, true, false));
    }

    @Test
    public void testFormatDurationWordsSuppressTrailingOnly() {
        long duration = 1000 * 86400;
        Assert.assertEquals("1 day", DurationFormatUtils.formatDurationWords(duration, false, true));

        duration = 1000 * (86400 * 2 + 3600 * 3);
        Assert.assertEquals("2 days 3 hours", DurationFormatUtils.formatDurationWords(duration, false, true));

        duration = 1000 * (86400 * 2 + 3600 * 3 + 60 * 4);
        Assert.assertEquals("2 days 3 hours 4 minutes", DurationFormatUtils.formatDurationWords(duration, false, true));
    }

    @Test
    public void testFormatPeriodUnder28Days() {
        long start = 1000000L;
        long end = start + (1000 * (86400 * 5 + 3600 * 4 + 60 * 3 + 2) + 1);
        Assert.assertEquals("5 4 3 2 1", DurationFormatUtils.formatPeriod(start, end, "d H m s S", false, TimeZone.getTimeZone("UTC")));
        Assert.assertEquals("05 04 03 02 001", DurationFormatUtils.formatPeriod(start, end, "dd HH mm ss SSS"));
    }

    @Test
    public void testFormatPeriodISO() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal2.set(2021, Calendar.FEBRUARY, 3, 4, 5, 6);
        cal2.set(Calendar.MILLISECOND, 7);

        String result = DurationFormatUtils.formatPeriodISO(cal1.getTimeInMillis(), cal2.getTimeInMillis());
        Assert.assertEquals("P1Y1M2DT4H5M6.007S", result);
    }

    @Test
    public void testFormatPeriodNegativeDifferencesAdjustment() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2020, Calendar.MARCH, 15, 10, 30, 45);
        cal1.set(Calendar.MILLISECOND, 500);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2021, Calendar.FEBRUARY, 10, 8, 20, 15);
        cal2.set(Calendar.MILLISECOND, 200);

        String result = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "y M d H m s S", false, tz);
        Assert.assertNotNull(result);
    }

    @Test
    public void testFormatPeriodWithoutTokens() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2022, Calendar.MARCH, 5, 6, 7, 8);
        cal2.set(Calendar.MILLISECOND, 9);

        long start = cal1.getTimeInMillis();
        long end = cal2.getTimeInMillis();

        String noY = DurationFormatUtils.formatPeriod(start, end, "M d H m s", false, tz);
        Assert.assertFalse(noY.contains("y"));

        String noM = DurationFormatUtils.formatPeriod(start, end, "y d H m s", false, tz);
        Assert.assertFalse(noM.contains("M"));

        String noD = DurationFormatUtils.formatPeriod(start, end, "y M H m s", false, tz);
        Assert.assertFalse(noD.contains("d"));

        String noH = DurationFormatUtils.formatPeriod(start, end, "y M d m s", false, tz);
        Assert.assertFalse(noH.contains("H"));

        String noMin = DurationFormatUtils.formatPeriod(start, end, "y M d H s", false, tz);
        Assert.assertFalse(noMin.contains("m"));

        String noS = DurationFormatUtils.formatPeriod(start, end, "y M d H m S", false, tz);
        Assert.assertFalse(noS.contains("s"));

        String noYNoM = DurationFormatUtils.formatPeriod(start, end, "d H m s", false, tz);
        Assert.assertNotNull(noYNoM);
    }

    @Test
    public void testFormatPeriodDefaultTimezone() {
        long start = System.currentTimeMillis();
        long end = start + (35L * DateUtils.MILLIS_PER_DAY);
        String formatted = DurationFormatUtils.formatPeriod(start, end, "d 'days'");
        Assert.assertTrue(formatted.endsWith("days"));
    }

    @Test
    public void testReduceAndCorrectBranch() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        start.set(2020, Calendar.JANUARY, 10, 0, 0, 0);
        end.set(2020, Calendar.JANUARY, 5, 0, 0, 0);

        int diff = DurationFormatUtils.reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, 0);
        Assert.assertEquals(5, diff);

        start.set(2020, Calendar.JANUARY, 5, 0, 0, 0);
        end.set(2020, Calendar.JANUARY, 10, 0, 0, 0);
        diff = DurationFormatUtils.reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, 0);
        Assert.assertEquals(0, diff);
    }

    @Test
    public void testLexxEdgeCases() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("''yy''MM''dd''HH''mm''ss''SS''");
        Assert.assertTrue(tokens.length > 0);

        tokens = DurationFormatUtils.lexx("'literal'yyyy");
        Assert.assertEquals(2, tokens.length);
        Assert.assertEquals("literal", tokens[0].getValue().toString());
        Assert.assertEquals("y", tokens[1].getValue());
        Assert.assertEquals(4, tokens[1].getCount());
    }

    @Test
    public void testTokenMethods() {
        DurationFormatUtils.Token token1 = new DurationFormatUtils.Token("y");
        Assert.assertEquals("y", token1.getValue());
        Assert.assertEquals(1, token1.getCount());

        token1.increment();
        Assert.assertEquals(2, token1.getCount());

        DurationFormatUtils.Token token2 = new DurationFormatUtils.Token("y", 2);
        Assert.assertEquals(token1, token2);
        Assert.assertEquals(token1.hashCode(), token2.hashCode());
        Assert.assertEquals("yy", token1.toString());

        DurationFormatUtils.Token token3 = new DurationFormatUtils.Token("y", 3);
        Assert.assertNotEquals(token1, token3);

        DurationFormatUtils.Token tokenNumber1 = new DurationFormatUtils.Token(10, 1);
        DurationFormatUtils.Token tokenNumber2 = new DurationFormatUtils.Token(10, 1);
        DurationFormatUtils.Token tokenNumber3 = new DurationFormatUtils.Token(20, 1);
        Assert.assertEquals(tokenNumber1, tokenNumber2);
        Assert.assertNotEquals(tokenNumber1, tokenNumber3);

        StringBuffer sb1 = new StringBuffer("test");
        StringBuffer sb2 = new StringBuffer("test");
        StringBuffer sb3 = new StringBuffer("other");
        DurationFormatUtils.Token tokenSb1 = new DurationFormatUtils.Token(sb1, 1);
        DurationFormatUtils.Token tokenSb2 = new DurationFormatUtils.Token(sb2, 1);
        DurationFormatUtils.Token tokenSb3 = new DurationFormatUtils.Token(sb3, 1);
        Assert.assertEquals(tokenSb1, tokenSb2);
        Assert.assertNotEquals(tokenSb1, tokenSb3);

        Assert.assertNotEquals(token1, tokenSb1);
        Assert.assertNotEquals(token1, null);
        Assert.assertNotEquals(token1, "y");

        DurationFormatUtils.Token[] array = new DurationFormatUtils.Token[] { token1, tokenSb1 };
        Assert.assertTrue(DurationFormatUtils.Token.containsTokenWithValue(array, "y"));
        Assert.assertFalse(DurationFormatUtils.Token.containsTokenWithValue(array, "nonexistent"));
    }

    @Test
    public void testFormatWithCustomTokens() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("y M d H m s S 'end'");
        String resNoPad = DurationFormatUtils.format(tokens, 1, 2, 3, 4, 5, 6, 7, false);
        Assert.assertEquals("1 2 3 4 5 6 7 end", resNoPad);

        String resPad = DurationFormatUtils.format(tokens, 1, 2, 3, 4, 5, 6, 7, true);
        Assert.assertEquals("1 2 3 4 5 6 7 end", resPad);
    }
}
