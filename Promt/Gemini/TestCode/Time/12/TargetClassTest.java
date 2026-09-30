package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LocalDateTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    private DateTimeZone originalZone;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        DateTimeZone.setDefault(LONDON);
        Locale.setDefault(Locale.UK);
        DateTimeUtils.setCurrentMillisFixed(1577836800000L); // 2020-01-01T00:00:00.000Z
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
        DateTimeZone.setDefault(originalZone);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testFactoryNow() {
        LocalDate now1 = LocalDate.now();
        Assert.assertEquals(2020, now1.getYear());
        Assert.assertEquals(1, now1.getMonthOfYear());
        Assert.assertEquals(1, now1.getDayOfMonth());

        LocalDate now2 = LocalDate.now(PARIS);
        Assert.assertNotNull(now2);

        LocalDate now3 = LocalDate.now(GJChronology.getInstance());
        Assert.assertNotNull(now3);
    }

    @Test(expected = NullPointerException.class)
    public void testFactoryNow_NullZone() {
        LocalDate.now((DateTimeZone) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFactoryNow_NullChrono() {
        LocalDate.now((Chronology) null);
    }

    @Test
    public void testFactoryParse() {
        LocalDate parsed = LocalDate.parse("2021-06-15");
        Assert.assertEquals(2021, parsed.getYear());
        Assert.assertEquals(6, parsed.getMonthOfYear());
        Assert.assertEquals(15, parsed.getDayOfMonth());

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
        LocalDate parsedCustom = LocalDate.parse("15/06/2021", dtf);
        Assert.assertEquals(parsed, parsedCustom);
    }

    @Test
    public void testFactoryFromCalendarFields() {
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.FEBRUARY, 28, 12, 30, 40);
        LocalDate dt = LocalDate.fromCalendarFields(cal);
        Assert.assertEquals(2015, dt.getYear());
        Assert.assertEquals(2, dt.getMonthOfYear());
        Assert.assertEquals(28, dt.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryFromCalendarFields_Null() {
        LocalDate.fromCalendarFields(null);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFactoryFromDateFields() {
        Date date = new Date(115, 1, 28, 12, 30, 40);
        LocalDate dt = LocalDate.fromDateFields(date);
        Assert.assertEquals(2015, dt.getYear());
        Assert.assertEquals(2, dt.getMonthOfYear());
        Assert.assertEquals(28, dt.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryFromDateFields_Null() {
        LocalDate.fromDateFields(null);
    }

    @Test
    public void testConstructors() {
        LocalDate test1 = new LocalDate();
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test1.getChronology());

        LocalDate test2 = new LocalDate(PARIS);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test2.getChronology());

        LocalDate test3 = new LocalDate((DateTimeZone) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test3.getChronology());

        LocalDate test4 = new LocalDate(CopticChronology.getInstance());
        Assert.assertEquals(CopticChronology.getInstanceUTC(), test4.getChronology());

        LocalDate test5 = new LocalDate((Chronology) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test5.getChronology());

        LocalDate test6 = new LocalDate(1577836800000L);
        Assert.assertEquals(2020, test6.getYear());

        LocalDate test7 = new LocalDate(1577836800000L, PARIS);
        Assert.assertEquals(2020, test7.getYear());

        LocalDate test8 = new LocalDate(1577836800000L, (DateTimeZone) null);
        Assert.assertEquals(2020, test8.getYear());

        LocalDate test9 = new LocalDate(1577836800000L, BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), test9.getChronology());

        LocalDate test10 = new LocalDate(1577836800000L, (Chronology) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test10.getChronology());

        LocalDate test11 = new LocalDate("2021-05-10");
        Assert.assertEquals(2021, test11.getYear());

        LocalDate test12 = new LocalDate("2021-05-10", PARIS);
        Assert.assertEquals(2021, test12.getYear());

        LocalDate test13 = new LocalDate("2021-05-10", (DateTimeZone) null);
        Assert.assertEquals(2021, test13.getYear());

        LocalDate test14 = new LocalDate("2021-05-10", BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), test14.getChronology());

        LocalDate test15 = new LocalDate("2021-05-10", (Chronology) null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test15.getChronology());

        LocalDate test16 = new LocalDate(2020, 5, 20);
        Assert.assertEquals(2020, test16.getYear());
        Assert.assertEquals(5, test16.getMonthOfYear());
        Assert.assertEquals(20, test16.getDayOfMonth());

        LocalDate test17 = new LocalDate(2020, 5, 20, BuddhistChronology.getInstanceUTC());
        Assert.assertEquals(BuddhistChronology.getInstanceUTC(), test17.getChronology());

        LocalDate test18 = new LocalDate(2020, 5, 20, null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), test18.getChronology());
    }

    @Test
    public void testGettersAndSize() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Assert.assertEquals(3, test.size());
        Assert.assertEquals(2021, test.getValue(0));
        Assert.assertEquals(6, test.getValue(1));
        Assert.assertEquals(15, test.getValue(2));

        Assert.assertEquals(ISOChronology.getInstanceUTC().year(), test.getField(0, ISOChronology.getInstanceUTC()));
        Assert.assertEquals(ISOChronology.getInstanceUTC().monthOfYear(), test.getField(1, ISOChronology.getInstanceUTC()));
        Assert.assertEquals(ISOChronology.getInstanceUTC().dayOfMonth(), test.getField(2, ISOChronology.getInstanceUTC()));

        Assert.assertEquals(1, test.getEra());
        Assert.assertEquals(20, test.getCenturyOfEra());
        Assert.assertEquals(2021, test.getYearOfEra());
        Assert.assertEquals(21, test.getYearOfCentury());
        Assert.assertEquals(2021, test.getYear());
        Assert.assertEquals(2021, test.getWeekyear());
        Assert.assertEquals(6, test.getMonthOfYear());
        Assert.assertEquals(24, test.getWeekOfWeekyear());
        Assert.assertEquals(166, test.getDayOfYear());
        Assert.assertEquals(15, test.getDayOfMonth());
        Assert.assertEquals(2, test.getDayOfWeek());

        Assert.assertEquals(2021, test.get(DateTimeFieldType.year()));
        Assert.assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        Assert.assertEquals(15, test.get(DateTimeFieldType.dayOfMonth()));
        Assert.assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetField_InvalidIndexLow() {
        new LocalDate().getField(-1, ISOChronology.getInstanceUTC());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetField_InvalidIndexHigh() {
        new LocalDate().getField(3, ISOChronology.getInstanceUTC());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_InvalidIndexLow() {
        new LocalDate().getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_InvalidIndexHigh() {
        new LocalDate().getValue(3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_NullFieldType() {
        new LocalDate().get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_UnsupportedFieldType() {
        new LocalDate().get(DateTimeFieldType.minuteOfDay());
    }

    @Test
    public void testIsSupported_DateTimeFieldType() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Assert.assertFalse(test.isSupported((DateTimeFieldType) null));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.year()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.monthOfYear()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.dayOfMonth()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.dayOfWeek()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.weekyear()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.centuryOfEra()));
        Assert.assertTrue(test.isSupported(DateTimeFieldType.era()));
        Assert.assertFalse(test.isSupported(DateTimeFieldType.hourOfDay()));
        Assert.assertFalse(test.isSupported(DateTimeFieldType.minuteOfHour()));
    }

    @Test
    public void testIsSupported_DurationFieldType() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Assert.assertFalse(test.isSupported((DurationFieldType) null));
        Assert.assertTrue(test.isSupported(DurationFieldType.days()));
        Assert.assertTrue(test.isSupported(DurationFieldType.weeks()));
        Assert.assertTrue(test.isSupported(DurationFieldType.months()));
        Assert.assertTrue(test.isSupported(DurationFieldType.weekyears()));
        Assert.assertTrue(test.isSupported(DurationFieldType.years()));
        Assert.assertTrue(test.isSupported(DurationFieldType.centuries()));
        Assert.assertTrue(test.isSupported(DurationFieldType.eras()));
        Assert.assertFalse(test.isSupported(DurationFieldType.hours()));
        Assert.assertFalse(test.isSupported(DurationFieldType.minutes()));
    }

    @Test
    public void testEqualsAndHashCode() {
        LocalDate test1 = new LocalDate(2021, 6, 15);
        LocalDate test2 = new LocalDate(2021, 6, 15);
        LocalDate test3 = new LocalDate(2021, 6, 16);
        LocalDate test4 = new LocalDate(2021, 6, 15, BuddhistChronology.getInstanceUTC());

        Assert.assertTrue(test1.equals(test1));
        Assert.assertTrue(test1.equals(test2));
        Assert.assertFalse(test1.equals(test3));
        Assert.assertFalse(test1.equals(test4));
        Assert.assertFalse(test1.equals(null));
        Assert.assertFalse(test1.equals("other"));

        Assert.assertEquals(test1.hashCode(), test2.hashCode());
        Assert.assertEquals(test1.hashCode(), test1.hashCode());
    }

    @Test
    public void testCompareTo() {
        LocalDate test1 = new LocalDate(2021, 6, 15);
        LocalDate test2 = new LocalDate(2021, 6, 15);
        LocalDate test3 = new LocalDate(2021, 6, 16);
        LocalDate test4 = new LocalDate(2021, 6, 14);

        Assert.assertEquals(0, test1.compareTo(test1));
        Assert.assertEquals(0, test1.compareTo(test2));
        Assert.assertTrue(test1.compareTo(test3) < 0);
        Assert.assertTrue(test1.compareTo(test4) > 0);

        YearMonthDay ymd = new YearMonthDay(2021, 6, 15);
        Assert.assertEquals(0, test1.compareTo(ymd));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testToDateTimeConversions() {
        LocalDate test = new LocalDate(2021, 6, 15);

        DateTime startOfDayDefault = test.toDateTimeAtStartOfDay();
        Assert.assertEquals(2021, startOfDayDefault.getYear());
        Assert.assertEquals(6, startOfDayDefault.getMonthOfYear());
        Assert.assertEquals(15, startOfDayDefault.getDayOfMonth());

        DateTime startOfDayZone = test.toDateTimeAtStartOfDay(PARIS);
        Assert.assertEquals(PARIS, startOfDayZone.getZone());

        DateTime atMidnightDefault = test.toDateTimeAtMidnight();
        Assert.assertEquals(0, atMidnightDefault.getHourOfDay());
        Assert.assertEquals(0, atMidnightDefault.getMinuteOfHour());

        DateTime atMidnightZone = test.toDateTimeAtMidnight(PARIS);
        Assert.assertEquals(PARIS, atMidnightZone.getZone());

        DateTime atCurrentTimeDefault = test.toDateTimeAtCurrentTime();
        Assert.assertEquals(2021, atCurrentTimeDefault.getYear());

        DateTime atCurrentTimeZone = test.toDateTimeAtCurrentTime(PARIS);
        Assert.assertEquals(PARIS, atCurrentTimeZone.getZone());

        DateMidnight dmDefault = test.toDateMidnight();
        Assert.assertEquals(2021, dmDefault.getYear());

        DateMidnight dmZone = test.toDateMidnight(PARIS);
        Assert.assertEquals(PARIS, dmZone.getZone());
    }

    @Test
    public void testToLocalDateTime() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalTime time = new LocalTime(14, 30, 45);
        LocalDateTime ldt = test.toLocalDateTime(time);

        Assert.assertEquals(2021, ldt.getYear());
        Assert.assertEquals(6, ldt.getMonthOfYear());
        Assert.assertEquals(15, ldt.getDayOfMonth());
        Assert.assertEquals(14, ldt.getHourOfDay());
        Assert.assertEquals(30, ldt.getMinuteOfHour());
        Assert.assertEquals(45, ldt.getSecondOfMinute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_Null() {
        new LocalDate(2021, 6, 15).toLocalDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_DifferentChronology() {
        LocalDate test = new LocalDate(2021, 6, 15, ISOChronology.getInstanceUTC());
        LocalTime time = new LocalTime(14, 30, BuddhistChronology.getInstanceUTC());
        test.toLocalDateTime(time);
    }

    @Test
    public void testToDateTime_LocalTime() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalTime time = new LocalTime(10, 20);

        DateTime dt1 = test.toDateTime(time);
        Assert.assertEquals(10, dt1.getHourOfDay());
        Assert.assertEquals(20, dt1.getMinuteOfHour());

        DateTime dt2 = test.toDateTime(time, PARIS);
        Assert.assertEquals(PARIS, dt2.getZone());
        Assert.assertEquals(10, dt2.getHourOfDay());

        DateTime dt3 = test.toDateTime((LocalTime) null, PARIS);
        Assert.assertEquals(PARIS, dt3.getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToDateTime_DifferentChronology() {
        LocalDate test = new LocalDate(2021, 6, 15, ISOChronology.getInstanceUTC());
        LocalTime time = new LocalTime(10, 20, BuddhistChronology.getInstanceUTC());
        test.toDateTime(time, PARIS);
    }

    @Test
    public void testToInterval() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Interval intervalDefault = test.toInterval();
        Assert.assertEquals(test.toDateTimeAtStartOfDay(), intervalDefault.getStart());
        Assert.assertEquals(test.plusDays(1).toDateTimeAtStartOfDay(), intervalDefault.getEnd());

        Interval intervalZone = test.toInterval(PARIS);
        Assert.assertEquals(test.toDateTimeAtStartOfDay(PARIS), intervalZone.getStart());
        Assert.assertEquals(test.plusDays(1).toDateTimeAtStartOfDay(PARIS), intervalZone.getEnd());
    }

    @Test
    public void testToDate() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Date date = test.toDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        Assert.assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testWithFields() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalDate updated = test.withFields(new YearMonthDay(2020, 2, 10));
        Assert.assertEquals(new LocalDate(2020, 2, 10), updated);

        Assert.assertSame(test, test.withFields(null));
    }

    @Test
    public void testWithField() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalDate modified = test.withField(DateTimeFieldType.year(), 2022);
        Assert.assertEquals(2022, modified.getYear());
        Assert.assertSame(test, test.withField(DateTimeFieldType.year(), 2021));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_Null() {
        new LocalDate(2021, 6, 15).withField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_Unsupported() {
        new LocalDate(2021, 6, 15).withField(DateTimeFieldType.hourOfDay(), 1);
    }

    @Test
    public void testWithFieldAdded() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalDate added = test.withFieldAdded(DurationFieldType.years(), 2);
        Assert.assertEquals(2023, added.getYear());

        Assert.assertSame(test, test.withFieldAdded(DurationFieldType.years(), 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_Null() {
        new LocalDate(2021, 6, 15).withFieldAdded(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_Unsupported() {
        new LocalDate(2021, 6, 15).withFieldAdded(DurationFieldType.hours(), 1);
    }

    @Test
    public void testWithPeriodAdded() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Period p = Period.days(5).withHours(12); // hours ignored
        LocalDate res = test.withPeriodAdded(p, 2);
        Assert.assertEquals(new LocalDate(2021, 6, 25), res);

        Assert.assertSame(test, test.withPeriodAdded(null, 1));
        Assert.assertSame(test, test.withPeriodAdded(p, 0));
    }

    @Test
    public void testPlusMinusPeriod() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Assert.assertEquals(new LocalDate(2021, 6, 18), test.plus(Period.days(3)));
        Assert.assertEquals(new LocalDate(2021, 6, 12), test.minus(Period.days(3)));
        Assert.assertSame(test, test.plus(null));
        Assert.assertSame(test, test.minus(null));
    }

    @Test
    public void testPlusMinusFields() {
        LocalDate test = new LocalDate(2021, 6, 15);

        Assert.assertEquals(new LocalDate(2023, 6, 15), test.plusYears(2));
        Assert.assertSame(test, test.plusYears(0));
        Assert.assertEquals(new LocalDate(2019, 6, 15), test.minusYears(2));
        Assert.assertSame(test, test.minusYears(0));

        Assert.assertEquals(new LocalDate(2021, 8, 15), test.plusMonths(2));
        Assert.assertSame(test, test.plusMonths(0));
        Assert.assertEquals(new LocalDate(2021, 4, 15), test.minusMonths(2));
        Assert.assertSame(test, test.minusMonths(0));

        Assert.assertEquals(new LocalDate(2021, 6, 29), test.plusWeeks(2));
        Assert.assertSame(test, test.plusWeeks(0));
        Assert.assertEquals(new LocalDate(2021, 6, 1), test.minusWeeks(2));
        Assert.assertSame(test, test.minusWeeks(0));

        Assert.assertEquals(new LocalDate(2021, 6, 17), test.plusDays(2));
        Assert.assertSame(test, test.plusDays(0));
        Assert.assertEquals(new LocalDate(2021, 6, 13), test.minusDays(2));
        Assert.assertSame(test, test.minusDays(0));
    }

    @Test
    public void testWithMethods() {
        LocalDate test = new LocalDate(2021, 6, 15);

        Assert.assertEquals(new LocalDate(2021, 6, 15), test.withEra(DateTimeConstants.CE));
        Assert.assertEquals(new LocalDate(2121, 6, 15), test.withCenturyOfEra(21));
        Assert.assertEquals(new LocalDate(2025, 6, 15), test.withYearOfEra(2025));
        Assert.assertEquals(new LocalDate(2050, 6, 15), test.withYearOfCentury(50));
        Assert.assertEquals(new LocalDate(2030, 6, 15), test.withYear(2030));
        Assert.assertEquals(new LocalDate(2030, 6, 15).getWeekyear(), test.withWeekyear(2030).getWeekyear());
        Assert.assertEquals(new LocalDate(2021, 11, 15), test.withMonthOfYear(11));
        Assert.assertEquals(10, test.withWeekOfWeekyear(10).getWeekOfWeekyear());
        Assert.assertEquals(100, test.withDayOfYear(100).getDayOfYear());
        Assert.assertEquals(new LocalDate(2021, 6, 25), test.withDayOfMonth(25));
        Assert.assertEquals(7, test.withDayOfWeek(7).getDayOfWeek());
    }

    @Test
    public void testProperties() {
        LocalDate test = new LocalDate(2021, 6, 15);

        Assert.assertEquals("era", test.era().getName());
        Assert.assertEquals("centuryOfEra", test.centuryOfEra().getName());
        Assert.assertEquals("yearOfCentury", test.yearOfCentury().getName());
        Assert.assertEquals("yearOfEra", test.yearOfEra().getName());
        Assert.assertEquals("year", test.year().getName());
        Assert.assertEquals("weekyear", test.weekyear().getName());
        Assert.assertEquals("monthOfYear", test.monthOfYear().getName());
        Assert.assertEquals("weekOfWeekyear", test.weekOfWeekyear().getName());
        Assert.assertEquals("dayOfYear", test.dayOfYear().getName());
        Assert.assertEquals("dayOfMonth", test.dayOfMonth().getName());
        Assert.assertEquals("dayOfWeek", test.dayOfWeek().getName());
        Assert.assertEquals("monthOfYear", test.property(DateTimeFieldType.monthOfYear()).getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_Null() {
        new LocalDate(2021, 6, 15).property(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_Unsupported() {
        new LocalDate(2021, 6, 15).property(DateTimeFieldType.hourOfDay());
    }

    @Test
    public void testPropertyMethods() {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalDate.Property monthProp = test.monthOfYear();

        Assert.assertEquals(test, monthProp.getLocalDate());
        Assert.assertEquals(test.getLocalMillis(), monthProp.getMillis());
        Assert.assertEquals(ISOChronology.getInstanceUTC(), monthProp.getChronology());
        Assert.assertEquals(ISOChronology.getInstanceUTC().monthOfYear(), monthProp.getField());

        LocalDate added = monthProp.addToCopy(2);
        Assert.assertEquals(8, added.getMonthOfYear());

        LocalDate wrapAdded = monthProp.addWrapFieldToCopy(8);
        Assert.assertEquals(2, wrapAdded.getMonthOfYear());

        LocalDate setVal = monthProp.setCopy(12);
        Assert.assertEquals(12, setVal.getMonthOfYear());

        LocalDate setText = monthProp.setCopy("July", Locale.UK);
        Assert.assertEquals(7, setText.getMonthOfYear());

        LocalDate setTextDefault = monthProp.setCopy("October");
        Assert.assertEquals(10, setTextDefault.getMonthOfYear());

        LocalDate maxMonth = test.dayOfMonth().withMaximumValue();
        Assert.assertEquals(30, maxMonth.getDayOfMonth());

        LocalDate minMonth = test.dayOfMonth().withMinimumValue();
        Assert.assertEquals(1, minMonth.getDayOfMonth());

        LocalDate roundFloor = test.monthOfYear().roundFloorCopy();
        Assert.assertEquals(1, roundFloor.getDayOfMonth());

        LocalDate roundCeiling = test.monthOfYear().roundCeilingCopy();
        Assert.assertEquals(1, roundCeiling.getDayOfMonth());

        LocalDate roundHalfFloor = test.monthOfYear().roundHalfFloorCopy();
        Assert.assertEquals(2021, roundHalfFloor.getYear());

        LocalDate roundHalfCeiling = test.monthOfYear().roundHalfCeilingCopy();
        Assert.assertEquals(2021, roundHalfCeiling.getYear());

        LocalDate roundHalfEven = test.monthOfYear().roundHalfEvenCopy();
        Assert.assertEquals(2021, roundHalfEven.getYear());
    }

    @Test
    public void testToString() {
        LocalDate test = new LocalDate(2021, 6, 15);
        Assert.assertEquals("2021-06-15", test.toString());
        Assert.assertEquals("15/06/2021", test.toString("dd/MM/yyyy"));
        Assert.assertEquals("2021-06-15", test.toString((String) null));

        Assert.assertEquals("15-Jun-2021", test.toString("dd-MMM-yyyy", Locale.ENGLISH));
        Assert.assertEquals("2021-06-15", test.toString(null, Locale.ENGLISH));
    }

    @Test
    public void testSerialization() throws Exception {
        LocalDate test = new LocalDate(2021, 6, 15, BuddhistChronology.getInstanceUTC());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate result = (LocalDate) ois.readObject();
        ois.close();

        Assert.assertEquals(test, result);
    }

    @Test
    public void testPropertySerialization() throws Exception {
        LocalDate test = new LocalDate(2021, 6, 15);
        LocalDate.Property prop = test.monthOfYear();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate.Property result = (LocalDate.Property) ois.readObject();
        ois.close();

        Assert.assertEquals(prop.getLocalDate(), result.getLocalDate());
        Assert.assertEquals(prop.getField().getType(), result.getField().getType());
    }
}
