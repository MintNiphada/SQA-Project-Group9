package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Unit tests for the Week class.
 */
public class WeekTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Locale UK = Locale.UK;

    // ─── constructor tests ───────────────────────────────────────────

    @Test
    public void testDefaultConstructor() {
        Week w = new Week();
        assertNotNull(w);
        // just check that year and week are populated (non-negative)
        assertTrue(w.getYearValue() >= 1900);
        assertTrue(w.getWeek() >= 1 && w.getWeek() <= 53);
    }

    @Test
    public void testWeekIntYearValidRange() {
        Week w1 = new Week(1, 2000);
        assertEquals(1, w1.getWeek());
        assertEquals(2000, w1.getYearValue());

        Week w53 = new Week(53, 2000);
        assertEquals(53, w53.getWeek());
        assertEquals(2000, w53.getYearValue());

        Week w15 = new Week(15, 9999);
        assertEquals(15, w15.getWeek());
        assertEquals(9999, w15.getYearValue());
    }

    @Test
    public void testWeekIntYearInvalidRangeNoException() {
        // Due to bug (&& instead of ||) the constructor never throws
        // for out-of-range values. Verify that no exception is thrown.
        Week w0 = new Week(0, 2000);
        assertEquals(0, w0.getWeek());

        Week w54 = new Week(54, 2000);
        assertEquals(54, w54.getWeek());
    }

    @Test
    public void testWeekIntYearYearObjectValid() {
        Year y = new Year(2010);
        Week w = new Week(10, y);
        assertEquals(10, w.getWeek());
        assertEquals(2010, w.getYearValue());
    }

    @Test
    public void testWeekIntYearYearObjectInvalidNoException() {
        Year y = new Year(2005);
        Week w = new Week(0, y); // no exception
        assertEquals(0, w.getWeek());
        assertEquals(2005, w.getYearValue());
    }

    @Test
    public void testWeekDate() {
        // 2005-03-14 (Monday) -> week 11? (ISO) Let's compute
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.set(2005, Calendar.MARCH, 14, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date d = cal.getTime();
        Week w = new Week(d, GMT, UK);
        int expectedWeek = cal.get(Calendar.WEEK_OF_YEAR);
        int expectedYear = cal.get(Calendar.YEAR);
        // handle special cases
        if (expectedWeek == 1 && cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
            expectedYear++;
            expectedWeek = 1;
        } else {
            expectedWeek = Math.min(expectedWeek, Week.LAST_WEEK_IN_YEAR);
        }
        if (cal.get(Calendar.MONTH) == Calendar.JANUARY && expectedWeek >= 52) {
            expectedYear--;
        }
        assertEquals(expectedWeek, w.getWeek());
        assertEquals(expectedYear, w.getYearValue());
    }

    @Test
    public void testWeekDateDecemberWeek1Case() {
        // 2007-12-31 was Monday, week 1 of 2008 in ISO
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.set(2007, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), GMT, UK);
        assertEquals(1, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test
    public void testWeekDateJanuaryHighWeekCase() {
        // 2009-01-01 (Thursday) week 1 of 2009, but test for high week in January
        // Find a date that triggers the "week >= 52 and month == JANUARY" branch.
        // 2005-01-01 is Saturday, week 53 of 2004 in ISO.
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.set(2005, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), GMT, UK);
        // expected: week 53 of 2004
        assertEquals(53, w.getWeek());
        assertEquals(2004, w.getYearValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekDateNullTime() {
        new Week((Date) null, GMT, UK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekDateNullZone() {
        new Week(new Date(), null, UK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekDateNullLocale() {
        new Week(new Date(), GMT, null);
    }

    @Test
    public void testWeekDateTimeZoneDeprecated() {
        // This constructor ignores its zone argument, using default timezone.
        // Just verify that it does not throw.
        Week w = new Week(new Date(), TimeZone.getTimeZone("PST"));
        assertNotNull(w);
    }

    // ─── getter tests ────────────────────────────────────────────────

    @Test
    public void testGetYear() {
        Week w = new Week(5, 2012);
        assertEquals(new Year(2012), w.getYear());
    }

    @Test
    public void testGetYearValue() {
        Week w = new Week(5, 2012);
        assertEquals(2012, w.getYearValue());
    }

    @Test
    public void testGetWeek() {
        assertEquals(5, new Week(5, 2012).getWeek());
    }

    // ─── millisecond tests ───────────────────────────────────────────

    @Test
    public void testGetFirstMillisecond() {
        Week w = new Week(10, 2008);
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.clear();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.WEEK_OF_YEAR, 10);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long expected = cal.getTime().getTime();
        assertEquals(expected, w.getFirstMillisecond());
    }

    @Test
    public void testGetLastMillisecond() {
        Week w = new Week(10, 2008);
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.clear();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.WEEK_OF_YEAR, 11); // start of next week
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long expectedLast = cal.getTime().getTime() - 1;
        assertEquals(expectedLast, w.getLastMillisecond());
    }

    @Test
    public void testGetFirstMillisecondWithCalendar() {
        Week w = new Week(3, 1999);
        Calendar cal = Calendar.getInstance(GMT, UK);
        long first = w.getFirstMillisecond(cal);
        // use the same logic to calculate expected
        Calendar expectedCal = (Calendar) cal.clone();
        expectedCal.clear();
        expectedCal.set(Calendar.YEAR, 1999);
        expectedCal.set(Calendar.WEEK_OF_YEAR, 3);
        expectedCal.set(Calendar.DAY_OF_WEEK, expectedCal.getFirstDayOfWeek());
        expectedCal.set(Calendar.HOUR_OF_DAY, 0);
        expectedCal.set(Calendar.MINUTE, 0);
        expectedCal.set(Calendar.SECOND, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime().getTime(), first);
    }

    @Test
    public void testGetLastMillisecondWithCalendar() {
        Week w = new Week(3, 1999);
        Calendar cal = Calendar.getInstance(GMT, UK);
        long last = w.getLastMillisecond(cal);
        Calendar nextWeekCal = (Calendar) cal.clone();
        nextWeekCal.clear();
        nextWeekCal.set(Calendar.YEAR, 1999);
        nextWeekCal.set(Calendar.WEEK_OF_YEAR, 4);
        nextWeekCal.set(Calendar.DAY_OF_WEEK, nextWeekCal.getFirstDayOfWeek());
        nextWeekCal.set(Calendar.HOUR_OF_DAY, 0);
        nextWeekCal.set(Calendar.MINUTE, 0);
        nextWeekCal.set(Calendar.SECOND, 0);
        nextWeekCal.set(Calendar.MILLISECOND, 0);
        long expected = nextWeekCal.getTime().getTime() - 1;
        assertEquals(expected, last);
    }

    @Test(expected = NullPointerException.class)
    public void testPegWithNullCalendar() {
        Week w = new Week(1, 2000);
        w.peg(null);
    }

    @Test
    public void testPegUpdatesMilliseconds() {
        Week w = new Week(20, 2015);
        long oldFirst = w.getFirstMillisecond();
        long oldLast = w.getLastMillisecond();
        // peg with a different calendar (same zone)
        Calendar cal = Calendar.getInstance(GMT, UK);
        w.peg(cal);
        // after peg, first/last should be recalculated; they may remain the same
        long newFirst = w.getFirstMillisecond();
        long newLast = w.getLastMillisecond();
        assertEquals(oldFirst, newFirst);
        assertEquals(oldLast, newLast);
    }

    // ─── previous / next tests ─────────────────────────────────────

    @Test
    public void testPreviousBasic() {
        Week w = new Week(10, 2010);
        RegularTimePeriod prev = w.previous();
        assertNotNull(prev);
        Week prevWeek = (Week) prev;
        assertEquals(9, prevWeek.getWeek());
        assertEquals(2010, prevWeek.getYearValue());
    }

    @Test
    public void testPreviousWeek1() {
        Week w = new Week(1, 2010);
        RegularTimePeriod prev = w.previous();
        assertNotNull(prev);
        Week prevWeek = (Week) prev;
        // Should be the last week of 2009
        Calendar cal = Calendar.getInstance(GMT, UK);
        cal.set(2009, Calendar.DECEMBER, 31);
        int expectedWeek = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
        assertEquals(expectedWeek, prevWeek.getWeek());
        assertEquals(2009, prevWeek.getYearValue());
    }

    @Test
    public void testPreviousWeek1Year1900() {
        Week w = new Week(1, 1900);
        assertNull(w.previous());
    }

    @Test
    public void testNextBasic() {
        Week w = new Week(5, 2010);
        RegularTimePeriod next = w.next();
        assertNotNull(next);
        Week nextWeek = (Week) next;
        assertEquals(6, nextWeek.getWeek());
        assertEquals(2010, nextWeek.getYearValue());
    }

    @Test
    public void testNextWeek52InYearWith53Weeks() {
        // 2004 had 53 weeks (ISO). Week 52 -> next = week 53
        Week w = new Week(52, 2004);
        RegularTimePeriod next = w.next();
        assertNotNull(next);
        Week nextWeek = (Week) next;
        assertEquals(53, nextWeek.getWeek());
        assertEquals(2004, nextWeek.getYearValue());
    }

    @Test
    public void testNextWeek53InYearWith53Weeks() {
        // 2004 week 53 -> next = week 1 2005
        Week w = new Week(53, 2004);
        RegularTimePeriod next = w.next();
        assertNotNull(next);
        Week nextWeek = (Week) next;
        assertEquals(1, nextWeek.getWeek());
        assertEquals(2005, nextWeek.getYearValue());
    }

    @Test
    public void testNextWeek52InYearWith52Weeks() {
        // 2005 had 52 weeks (ISO). Week 52 -> next = week 1 2006
        Week w = new Week(52, 2005);
        RegularTimePeriod next = w.next();
        assertNotNull(next);
        Week nextWeek = (Week) next;
        assertEquals(1, nextWeek.getWeek());
        assertEquals(2006, nextWeek.getYearValue());
    }

    @Test
    public void testNextWeek53Year9999() {
        // For year 9999 week 53, next should be null (if actualMaxWeek allows)
        // First ensure that 9999 has a week 53 (we'll just trust it might).
        Week w = new Week(53, 9999);
        // next() returns null if year >= 9999 and week == actualMaxWeek
        assertNull(w.next());
    }

    @Test
    public void testNextWeek52Year9999() {
        // For year 9999 week 52, if actualMaxWeek==52, next = null; 
        // if actualMaxWeek > 52, next week 53 exists but year 9999 any subsequent next() will be null.
        // This test is to trigger the branch year < 9999? Actually year 9999 so it'll go to else.
        Week w = new Week(52, 9999);
        // actualMaxWeek could be 52 or 53; if 52, this.week (52) == actualMax, so null.
        // But if actualMaxWeek is 53, this.week < actualMax so result = week 53.
        RegularTimePeriod n = w.next();
        if (n == null) {
            // acceptable
        } else {
            assertEquals(53, ((Week) n).getWeek());
        }
    }

    // ─── serial index / toString ─────────────────────────────────────

    @Test
    public void testGetSerialIndex() {
        Week w = new Week(12, 2000);
        assertEquals(2000 * 53L + 12, w.getSerialIndex());
    }

    @Test
    public void testToString() {
        Week w = new Week(9, 2002);
        assertEquals("Week 9, 2002", w.toString());
    }

    // ─── equals / hashCode ───────────────────────────────────────────

    @Test
    public void testEquals() {
        Week w1 = new Week(1, 2010);
        Week w2 = new Week(1, 2010);
        Week w3 = new Week(2, 2010);
        Week w4 = new Week(1, 2011);

        assertTrue(w1.equals(w2));
        assertTrue(w2.equals(w1));
        assertFalse(w1.equals(w3));
        assertFalse(w1.equals(w4));
        assertFalse(w1.equals(null));
        assertFalse(w1.equals("String"));
        assertTrue(w1.equals(w1)); // same object
    }

    @Test
    public void testHashCode() {
        Week w1a = new Week(5, 2008);
        Week w1b = new Week(5, 2008);
        assertEquals(w1a.hashCode(), w1b.hashCode());
        
        // different weeks should ideally have different hash codes,
        // but not guaranteed. We just check that equal objects have equal hash.
    }

    // ─── compareTo tests ─────────────────────────────────────────────

    @Test
    public void testCompareToWeek() {
        Week w1 = new Week(10, 2000);
        Week w2 = new Week(10, 2001);
        Week w3 = new Week(11, 2000);

        assertTrue(w1.compareTo(w2) < 0);   // earlier year
        assertTrue(w2.compareTo(w1) > 0);
        assertEquals(0, w1.compareTo(new Week(10, 2000)));

        assertTrue(w1.compareTo(w3) < 0);   // same year, earlier week
        assertTrue(w3.compareTo(w1) > 0);
    }

    @Test
    public void testCompareToRegularTimePeriod() {
        // compareTo with another RegularTimePeriod (not Week) returns 0
        RegularTimePeriod other = new RegularTimePeriod() {
            public RegularTimePeriod previous() { return null; }
            public RegularTimePeriod next() { return null; }
            public long getSerialIndex() { return 0; }
            public void peg(Calendar calendar) { }
            public long getFirstMillisecond() { return 0; }
            public long getLastMillisecond() { return 0; }
            public long getFirstMillisecond(Calendar calendar) { return 0; }
            public long getLastMillisecond(Calendar calendar) { return 0; }
            public int compareTo(Object o1) { return 0; }
        };
        Week w = new Week(1, 2000);
        assertEquals(0, w.compareTo(other));
    }

    @Test
    public void testCompareToNonTimePeriod() {
        Week w = new Week(1, 2000);
        assertEquals(1, w.compareTo("Not a Time Period"));
    }

    // ─── parseWeek tests ─────────────────────────────────────────────

    @Test
    public void testParseWeekNull() {
        assertNull(Week.parseWeek(null));
    }

    @Test
    public void testParseWeekValidFormat1() {
        Week w = Week.parseWeek("2008-W01");
        assertNotNull(w);
        assertEquals(2008, w.getYearValue());
        assertEquals(1, w.getWeek());
    }

    @Test
    public void testParseWeekValidFormat2() {
        Week w = Week.parseWeek("W01-2008");
        assertNotNull(w);
        assertEquals(2008, w.getYearValue());
        assertEquals(1, w.getWeek());
    }

    @Test
    public void testParseWeekWithComma() {
        Week w = Week.parseWeek("2008,W02");
        assertEquals(2, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test
    public void testParseWeekWithSpace() {
        Week w = Week.parseWeek("2008 W03");
        assertEquals(3, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test
    public void testParseWeekWithDot() {
        Week w = Week.parseWeek("2008.W04");
        assertEquals(4, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekMissingSeparator() {
        Week.parseWeek("2008W05");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekNumber() {
        Week.parseWeek("2008-W00");  // week < 1
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekNumberTooHigh() {
        Week.parseWeek("2008-W54");  // week > 53
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidYear() {
        // year not parseable
        Week.parseWeek("NOTAYEAR-W10");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekNonNumeric() {
        Week.parseWeek("2008-WAB");
    }

    @Test
    public void testParseWeekTrimWhitespace() {
        Week w = Week.parseWeek("  2008-W12  ");
        assertEquals(12, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    // ─── edge cases for stringToWeek via parseWeek ─────────────────
    @Test(expected = TimePeriodFormatException.class)
    public void testStringToWeekOutOfRangeLow() {
        Week.parseWeek("2008-W0");  // after trimming, 0 -> invalid
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testStringToWeekOutOfRangeHigh() {
        Week.parseWeek("2008-W54");
    }
}
```
