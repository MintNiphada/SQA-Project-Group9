package org.jfree.data.time;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test suite for {@link Week}.
 */
public class WeekTest {

    @Test
    public void testDefaultConstructor() {
        Week w = new Week();
        assertNotNull(w);
        assertTrue(w.getYearValue() >= 1900);
        assertTrue(w.getWeek() >= 1 && w.getWeek() <= 53);
    }

    @Test
    public void testConstructorWeekIntYearInt() {
        Week w = new Week(10, 2020);
        assertEquals(10, w.getWeek());
        assertEquals(2020, w.getYearValue());
        assertEquals(new Year(2020), w.getYear());

        // Boundary values
        Week wFirst = new Week(1, 1900);
        assertEquals(1, wFirst.getWeek());
        assertEquals(1900, wFirst.getYearValue());

        Week wLast = new Week(53, 9999);
        assertEquals(53, wLast.getWeek());
        assertEquals(9999, wLast.getYearValue());
    }

    @Test
    public void testConstructorWeekYearObject() {
        Year y = new Year(2015);
        Week w = new Week(25, y);
        assertEquals(25, w.getWeek());
        assertEquals(2015, w.getYearValue());
        assertEquals(y, w.getYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDate() {
        new Week(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullZone() {
        new Week(new Date(), null, Locale.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullLocale() {
        new Week(new Date(), TimeZone.getDefault(), null);
    }

    @Test
    public void testConstructorDateAndZoneDeprecated() {
        Date d = new Date(100000000000L);
        Week w = new Week(d, TimeZone.getTimeZone("UTC"));
        assertNotNull(w);
    }

    @Test
    public void testConstructorDateEndDecStartJan() {
        // Test week 1 in December (transition to next year)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        cal.clear();
        cal.set(2008, Calendar.DECEMBER, 29); // In ISO/UK, Dec 29 2008 is week 1 of 2009
        Week w1 = new Week(cal.getTime(), TimeZone.getTimeZone("UTC"), Locale.UK);
        assertEquals(1, w1.getWeek());
        assertEquals(2009, w1.getYearValue());

        // Test week 52/53 in early January (transition from previous year)
        cal.clear();
        cal.set(2010, Calendar.JANUARY, 1); // Jan 1 2010 is week 53 of 2009 in UK
        Week w2 = new Week(cal.getTime(), TimeZone.getTimeZone("UTC"), Locale.UK);
        assertTrue(w2.getWeek() >= 52);
        assertEquals(2009, w2.getYearValue());
    }

    @Test
    public void testGetFirstMillisecond() {
        Locale savedLocale = Locale.getDefault();
        TimeZone savedZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.UK);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Week w = new Week(1, 2020);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
            long expected = w.getFirstMillisecond(cal);
            assertEquals(expected, w.getFirstMillisecond());
        }
        finally {
            Locale.setDefault(savedLocale);
            TimeZone.setDefault(savedZone);
        }
    }

    @Test
    public void testGetLastMillisecond() {
        Locale savedLocale = Locale.getDefault();
        TimeZone savedZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.UK);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Week w = new Week(1, 2020);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
            long expected = w.getLastMillisecond(cal);
            assertEquals(expected, w.getLastMillisecond());
            assertEquals(expected, w.getFirstMillisecond() + (7L * 24 * 3600 * 1000) - 1);
        }
        finally {
            Locale.setDefault(savedLocale);
            TimeZone.setDefault(savedZone);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testGetFirstMillisecondNullCalendar() {
        Week w = new Week(1, 2020);
        w.getFirstMillisecond(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetLastMillisecondNullCalendar() {
        Week w = new Week(1, 2020);
        w.getLastMillisecond(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPegNullCalendar() {
        Week w = new Week(1, 2020);
        w.peg(null);
    }

    @Test
    public void testPrevious() {
        // Middle of the year
        Week w1 = new Week(20, 2020);
        Week p1 = (Week) w1.previous();
        assertEquals(19, p1.getWeek());
        assertEquals(2020, p1.getYearValue());

        // First week of regular year
        Week w2 = new Week(1, 2020);
        Week p2 = (Week) w2.previous();
        assertEquals(2019, p2.getYearValue());
        assertTrue(p2.getWeek() == 52 || p2.getWeek() == 53);

        // Lower bound year 1900
        Week w3 = new Week(1, 1900);
        assertNull(w3.previous());

        Week w4 = new Week(2, 1900);
        Week p4 = (Week) w4.previous();
        assertEquals(1, p4.getWeek());
        assertEquals(1900, p4.getYearValue());
    }

    @Test
    public void testNext() {
        // Middle of year
        Week w1 = new Week(20, 2020);
        Week n1 = (Week) w1.next();
        assertEquals(21, n1.getWeek());
        assertEquals(2020, n1.getYearValue());

        // Week 51 to 52
        Week w2 = new Week(51, 2020);
        Week n2 = (Week) w2.next();
        assertEquals(52, n2.getWeek());
        assertEquals(2020, n2.getYearValue());

        // Check roll-over from 52/53
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.DECEMBER, 31);
        int maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);

        if (maxWeeks == 53) {
            Week w52 = new Week(52, 2020);
            Week n52 = (Week) w52.next();
            assertEquals(53, n52.getWeek());
            assertEquals(2020, n52.getYearValue());

            Week w53 = new Week(53, 2020);
            Week n53 = (Week) w53.next();
            assertEquals(1, n53.getWeek());
            assertEquals(2021, n53.getYearValue());
        }
        else {
            Week w52 = new Week(52, 2020);
            Week n52 = (Week) w52.next();
            assertEquals(1, n52.getWeek());
            assertEquals(2021, n52.getYearValue());
        }

        // Upper bound year 9999
        Week wEnd = new Week(53, 9999);
        assertNull(wEnd.next());
    }

    @Test
    public void testGetSerialIndex() {
        Week w = new Week(10, 2020);
        assertEquals(2020 * 53L + 10, w.getSerialIndex());

        Week w1 = new Week(1, 1900);
        assertEquals(1900 * 53L + 1, w1.getSerialIndex());
    }

    @Test
    public void testToString() {
        Week w = new Week(9, 2002);
        assertEquals("Week 9, 2002", w.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Week w1 = new Week(15, 2020);
        Week w2 = new Week(15, 2020);
        Week w3 = new Week(16, 2020);
        Week w4 = new Week(15, 2021);

        // Reflexive
        assertTrue(w1.equals(w1));

        // Symmetric
        assertTrue(w1.equals(w2));
        assertTrue(w2.equals(w1));
        assertEquals(w1.hashCode(), w2.hashCode());

        // Inequality
        assertFalse(w1.equals(w3));
        assertFalse(w1.equals(w4));
        assertFalse(w1.equals(null));
        assertFalse(w1.equals("Not a Week"));
    }

    @Test
    public void testCompareTo() {
        Week w1 = new Week(10, 2020);
        Week w2 = new Week(10, 2020);
        Week wEarlierWeek = new Week(9, 2020);
        Week wLaterWeek = new Week(11, 2020);
        Week wEarlierYear = new Week(10, 2019);
        Week wLaterYear = new Week(10, 2021);

        assertEquals(0, w1.compareTo(w2));
        assertTrue(w1.compareTo(wEarlierWeek) > 0);
        assertTrue(w1.compareTo(wLaterWeek) < 0);
        assertTrue(w1.compareTo(wEarlierYear) > 0);
        assertTrue(w1.compareTo(wLaterYear) < 0);

        // Comparing to different RegularTimePeriod
        Year y = new Year(2020);
        assertEquals(0, w1.compareTo(y));

        // Comparing to non-RegularTimePeriod object
        assertTrue(w1.compareTo(new Object()) > 0);
    }

    @Test
    public void testParseWeekValidFormats() {
        // YYYY-Wnn format
        Week w1 = Week.parseWeek("2020-W15");
        assertEquals(15, w1.getWeek());
        assertEquals(2020, w1.getYearValue());

        // Wnn-YYYY format
        Week w2 = Week.parseWeek("W15-2020");
        assertEquals(15, w2.getWeek());
        assertEquals(2020, w2.getYearValue());

        // Separators: space, comma, dot
        Week w3 = Week.parseWeek("2020, 15");
        assertEquals(15, w3.getWeek());
        assertEquals(2020, w3.getYearValue());

        Week w4 = Week.parseWeek("2020 15");
        assertEquals(15, w4.getWeek());
        assertEquals(2020, w4.getYearValue());

        Week w5 = Week.parseWeek("2020.15");
        assertEquals(15, w5.getWeek());
        assertEquals(2020, w5.getYearValue());

        Week w6 = Week.parseWeek("15 2020");
        assertEquals(15, w6.getWeek());
        assertEquals(2020, w6.getYearValue());

        // Null string check
        assertNull(Week.parseWeek(null));
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekNoSeparator() {
        Week.parseWeek("2020W15");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekWithYearFirst() {
        Week.parseWeek("2020-99");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekWithWeekFirst() {
        Week.parseWeek("99-2020");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekNoYearFound() {
        Week.parseWeek("abc-def");
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeekNumberString() {
        Week.parseWeek("2020-abc");
    }

    @Test
    public void testSerialization() throws Exception {
        Week w1 = new Week(26, 2020);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(w1);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Week w2 = (Week) in.readObject();

        assertEquals(w1, w2);
        assertEquals(w1.getFirstMillisecond(), w2.getFirstMillisecond());
        assertEquals(w1.getLastMillisecond(), w2.getLastMillisecond());
    }

    @Test
    public void testPeg() {
        Week w = new Week(15, 2020);
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 1);
        w.peg(cal);
        assertTrue(w.getFirstMillisecond() != 0);
        assertTrue(w.getLastMillisecond() >= w.getFirstMillisecond());
    }
}