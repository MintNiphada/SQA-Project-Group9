package org.joda.time.format;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.MillisDurationField;
import org.joda.time.field.UnsupportedDateTimeField;
import org.joda.time.field.UnsupportedDurationField;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeParserBucketTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    private Locale originalLocale;

    @Before
    public void setUp() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() {
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testConstructorsAndGetters() {
        Chronology chrono = ISOChronology.getInstance(PARIS);
        DateTimeParserBucket bucket1 = new DateTimeParserBucket(1000L, chrono, Locale.FRANCE);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), bucket1.getChronology());
        Assert.assertEquals(Locale.FRANCE, bucket1.getLocale());
        Assert.assertEquals(PARIS, bucket1.getZone());
        Assert.assertNull(bucket1.getPivotYear());
        Assert.assertEquals(0, bucket1.getOffset());

        DateTimeParserBucket bucket2 = new DateTimeParserBucket(1000L, chrono, Locale.GERMANY, 2050);
        Assert.assertEquals(Integer.valueOf(2050), bucket2.getPivotYear());
        Assert.assertEquals(Locale.GERMANY, bucket2.getLocale());

        DateTimeParserBucket bucket3 = new DateTimeParserBucket(1000L, chrono, null, null, 1990);
        Assert.assertEquals(Locale.UK, bucket3.getLocale());
        Assert.assertNull(bucket3.getPivotYear());

        DateTimeParserBucket bucket4 = new DateTimeParserBucket(1000L, null, null);
        Assert.assertEquals(ISOChronology.getInstanceUTC(), bucket4.getChronology());
        Assert.assertEquals(Locale.UK, bucket4.getLocale());
    }

    @Test
    public void testZoneAndOffsetHandling() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        Assert.assertNull(bucket.getZone());
        Assert.assertEquals(0, bucket.getOffset());

        bucket.setZone(PARIS);
        Assert.assertEquals(PARIS, bucket.getZone());
        Assert.assertEquals(0, bucket.getOffset());

        bucket.setZone(DateTimeZone.UTC);
        Assert.assertNull(bucket.getZone());

        bucket.setOffset(3600000);
        Assert.assertNull(bucket.getZone());
        Assert.assertEquals(3600000, bucket.getOffset());

        bucket.setZone(NEW_YORK);
        Assert.assertEquals(NEW_YORK, bucket.getZone());
        Assert.assertEquals(0, bucket.getOffset());
    }

    @Test
    public void testPivotYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        Assert.assertNull(bucket.getPivotYear());

        bucket.setPivotYear(2025);
        Assert.assertEquals(Integer.valueOf(2025), bucket.getPivotYear());

        bucket.setPivotYear(null);
        Assert.assertNull(bucket.getPivotYear());
    }

    @Test
    public void testComputeMillisBasic() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 45);

        long computed = bucket.computeMillis(false);
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2020, utcChrono.year().get(computed));
        Assert.assertEquals(6, utcChrono.monthOfYear().get(computed));
        Assert.assertEquals(15, utcChrono.dayOfMonth().get(computed));
        Assert.assertEquals(12, utcChrono.hourOfDay().get(computed));
        Assert.assertEquals(30, utcChrono.minuteOfHour().get(computed));
        Assert.assertEquals(45, utcChrono.secondOfMinute().get(computed));
    }

    @Test
    public void testComputeMillisWithResetFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);

        long millis = bucket.computeMillis(true);
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2021, utcChrono.year().get(millis));
        Assert.assertEquals(5, utcChrono.monthOfYear().get(millis));
        Assert.assertEquals(10, utcChrono.dayOfMonth().get(millis));
        Assert.assertEquals(0, utcChrono.hourOfDay().get(millis));
        Assert.assertEquals(0, utcChrono.minuteOfHour().get(millis));
        Assert.assertEquals(0, utcChrono.secondOfMinute().get(millis));
        Assert.assertEquals(0, utcChrono.millisOfSecond().get(millis));
    }

    @Test
    public void testComputeMillisWithTextFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2018);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "March", Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 25);

        long computed = bucket.computeMillis(false);
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2018, utcChrono.year().get(computed));
        Assert.assertEquals(3, utcChrono.monthOfYear().get(computed));
        Assert.assertEquals(25, utcChrono.dayOfMonth().get(computed));
    }

    @Test
    public void testComputeMillisDefaultYearFallbackWhenFirstFieldIsMonthOrDay() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH, null, 2012);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 11);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 20);

        long millis = bucket.computeMillis(false, "11-20");
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2012, utcChrono.year().get(millis));
        Assert.assertEquals(11, utcChrono.monthOfYear().get(millis));
        Assert.assertEquals(20, utcChrono.dayOfMonth().get(millis));
    }

    @Test
    public void testComputeMillisDefaultYearFallbackDayOfYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH, null, 2008);
        bucket.saveField(DateTimeFieldType.dayOfYear(), 45);

        long millis = bucket.computeMillis();
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2008, utcChrono.year().get(millis));
        Assert.assertEquals(45, utcChrono.dayOfYear().get(millis));
    }

    @Test
    public void testComputeMillisWithOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 10);
        bucket.setOffset(3600000);

        long millis = bucket.computeMillis();
        Assert.assertEquals(946717200000L, millis);
    }

    @Test
    public void testComputeMillisWithZone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);
        bucket.setZone(NEW_YORK);

        long millis = bucket.computeMillis();
        long expected = ISOChronology.getInstance(NEW_YORK).getDateTimeMillis(2020, 1, 1, 12, 0, 0, 0);
        Assert.assertEquals(expected, millis);
    }

    @Test
    public void testComputeMillisFieldExceptionWithText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);

        try {
            bucket.computeMillis(false, "2020-13-01");
            Assert.fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot parse \"2020-13-01\""));
        }
    }

    @Test
    public void testComputeMillisFieldExceptionWithoutText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);

        try {
            bucket.computeMillis(false, null);
            Assert.fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            Assert.assertFalse(ex.getMessage().contains("Cannot parse"));
        }
    }

    @Test
    public void testComputeMillisZoneGapException() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.setZone(PARIS);
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 28);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);

        try {
            bucket.computeMillis(false, "2021-03-28 02:30");
            Assert.fail("Expected IllegalArgumentException due to DST gap");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot parse \"2021-03-28 02:30\": Illegal instant due to time zone offset transition"));
        }

        try {
            bucket.computeMillis(false, null);
            Assert.fail("Expected IllegalArgumentException due to DST gap");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Illegal instant due to time zone offset transition"));
        }
    }

    @Test
    public void testSaveStateAndRestoreState() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.setZone(LONDON);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);

        Object state1 = bucket.saveState();
        Assert.assertNotNull(state1);
        Assert.assertSame(state1, bucket.saveState());

        bucket.setZone(NEW_YORK);
        bucket.setOffset(7200000);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);

        Assert.assertEquals(7200000, bucket.getOffset());
        Assert.assertNull(bucket.getZone());

        boolean restored = bucket.restoreState(state1);
        Assert.assertTrue(restored);
        Assert.assertEquals(LONDON, bucket.getZone());
        Assert.assertEquals(0, bucket.getOffset());

        long millis = bucket.computeMillis();
        Assert.assertEquals(2010, ISOChronology.getInstance(LONDON).year().get(millis));
        Assert.assertEquals(5, ISOChronology.getInstance(LONDON).monthOfYear().get(millis));

        Assert.assertFalse(bucket.restoreState("InvalidStateObject"));
        Assert.assertFalse(bucket.restoreState(null));

        DateTimeParserBucket otherBucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        Assert.assertFalse(otherBucket.restoreState(state1));
    }

    @Test
    public void testSaveStateRestoredToLowerCountAndSharedArrayCloning() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2000);
        Object state1 = bucket.saveState();

        bucket.saveField(DateTimeFieldType.monthOfYear(), 10);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 12);

        bucket.restoreState(state1);

        bucket.saveField(DateTimeFieldType.monthOfYear(), 4);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 8);

        long millis = bucket.computeMillis();
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2000, utcChrono.year().get(millis));
        Assert.assertEquals(4, utcChrono.monthOfYear().get(millis));
        Assert.assertEquals(8, utcChrono.dayOfMonth().get(millis));
    }

    @Test
    public void testArrayExpansionAndSortMoreThan10Fields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.secondOfMinute(),
            DateTimeFieldType.millisOfSecond(),
            DateTimeFieldType.centuryOfEra(),
            DateTimeFieldType.yearOfEra(),
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfYear(),
            DateTimeFieldType.weekOfWeekyear()
        };

        for (int i = 0; i < types.length; i++) {
            DateTimeField field = types[i].getField(bucket.getChronology());
            bucket.saveField(field, 1);
        }

        bucket.saveField(DateTimeFieldType.year(), 2025);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 12);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 31);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 23);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 59);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 59);

        long millis = bucket.computeMillis(false);
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2025, utcChrono.year().get(millis));
        Assert.assertEquals(12, utcChrono.monthOfYear().get(millis));
        Assert.assertEquals(31, utcChrono.dayOfMonth().get(millis));
    }

    @Test
    public void testInsertionSortOrderSmallList() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 10);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 20);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 15);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 5);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 8);
        bucket.saveField(DateTimeFieldType.year(), 2019);

        long millis = bucket.computeMillis();
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2019, utcChrono.year().get(millis));
        Assert.assertEquals(8, utcChrono.monthOfYear().get(millis));
        Assert.assertEquals(5, utcChrono.dayOfMonth().get(millis));
        Assert.assertEquals(15, utcChrono.hourOfDay().get(millis));
        Assert.assertEquals(20, utcChrono.minuteOfHour().get(millis));
        Assert.assertEquals(10, utcChrono.secondOfMinute().get(millis));
    }

    @Test
    public void testCompareReverseMethod() {
        DurationField millis1 = MillisDurationField.INSTANCE;
        DurationField millis2 = MillisDurationField.INSTANCE;
        DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.days());

        Assert.assertEquals(0, DateTimeParserBucket.compareReverse(null, null));
        Assert.assertEquals(0, DateTimeParserBucket.compareReverse(unsupported, unsupported));
        Assert.assertEquals(0, DateTimeParserBucket.compareReverse(null, unsupported));
        Assert.assertEquals(0, DateTimeParserBucket.compareReverse(unsupported, null));

        Assert.assertEquals(-1, DateTimeParserBucket.compareReverse(null, millis1));
        Assert.assertEquals(-1, DateTimeParserBucket.compareReverse(unsupported, millis1));

        Assert.assertEquals(1, DateTimeParserBucket.compareReverse(millis1, null));
        Assert.assertEquals(1, DateTimeParserBucket.compareReverse(millis1, unsupported));

        Assert.assertEquals(0, DateTimeParserBucket.compareReverse(millis1, millis2));

        Chronology chrono = ISOChronology.getInstanceUTC();
        DurationField days = chrono.days();
        DurationField hours = chrono.hours();
        Assert.assertTrue(DateTimeParserBucket.compareReverse(days, hours) < 0);
        Assert.assertTrue(DateTimeParserBucket.compareReverse(hours, days) > 0);
    }

    @Test
    public void testSavedFieldCompareTo() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();
        DateTimeField monthOfYear = chrono.monthOfYear();
        DateTimeField dayOfWeek = chrono.dayOfWeek();

        DateTimeParserBucket.SavedField sfDayOfMonth = new DateTimeParserBucket.SavedField(dayOfMonth, 10);
        DateTimeParserBucket.SavedField sfMonthOfYear = new DateTimeParserBucket.SavedField(monthOfYear, 5);
        DateTimeParserBucket.SavedField sfDayOfWeek = new DateTimeParserBucket.SavedField(dayOfWeek, 2);

        Assert.assertTrue(sfMonthOfYear.compareTo(sfDayOfMonth) < 0);
        Assert.assertTrue(sfDayOfMonth.compareTo(sfMonthOfYear) > 0);

        Assert.assertTrue(sfDayOfMonth.compareTo(sfDayOfWeek) < 0);
        Assert.assertTrue(sfDayOfWeek.compareTo(sfDayOfMonth) > 0);

        DateTimeParserBucket.SavedField sfDayOfMonthText = new DateTimeParserBucket.SavedField(dayOfMonth, "10", Locale.ENGLISH);
        Assert.assertEquals(0, sfDayOfMonth.compareTo(sfDayOfMonthText));
    }

    @Test
    public void testSaveFieldDirectDateTimeField() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        DateTimeField yearField = ISOChronology.getInstanceUTC().year();
        bucket.saveField(yearField, 2030);

        long millis = bucket.computeMillis();
        Assert.assertEquals(2030, ISOChronology.getInstanceUTC().year().get(millis));
    }

    @Test
    public void testComputeMillisSharedFieldsCloning() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2005);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 4);

        Object state = bucket.saveState();
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);

        bucket.restoreState(state);
        long millis = bucket.computeMillis(false);
        Chronology utcChrono = ISOChronology.getInstanceUTC();
        Assert.assertEquals(2005, utcChrono.year().get(millis));
        Assert.assertEquals(4, utcChrono.monthOfYear().get(millis));
    }

    @Test
    public void testBuddhistAndGJChronologies() {
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, buddhist, Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2564);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long millis = bucket.computeMillis();
        Assert.assertEquals(2564, buddhist.year().get(millis));

        Chronology gj = GJChronology.getInstance(DateTimeZone.UTC);
        DateTimeParserBucket bucketGJ = new DateTimeParserBucket(0L, gj, Locale.ENGLISH);
        bucketGJ.saveField(DateTimeFieldType.year(), 1582);
        bucketGJ.saveField(DateTimeFieldType.monthOfYear(), 10);
        bucketGJ.saveField(DateTimeFieldType.dayOfMonth(), 4);
        long millisGJ = bucketGJ.computeMillis();
        Assert.assertEquals(1582, gj.year().get(millisGJ));
        Assert.assertEquals(10, gj.monthOfYear().get(millisGJ));
        Assert.assertEquals(4, gj.dayOfMonth().get(millisGJ));
    }
}
