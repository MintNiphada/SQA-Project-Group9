package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.YearMonth;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GJChronologyTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");

    private DateTimeZone originalZone;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
    }

    @Test
    public void testFactoryGetInstances() {
        GJChronology defaultUtc = GJChronology.getInstanceUTC();
        Assert.assertNotNull(defaultUtc);
        Assert.assertEquals(DateTimeZone.UTC, defaultUtc.getZone());
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, defaultUtc.getGregorianCutover());
        Assert.assertEquals(4, defaultUtc.getMinimumDaysInFirstWeek());

        GJChronology defaultLocal = GJChronology.getInstance();
        Assert.assertEquals(DateTimeZone.getDefault(), defaultLocal.getZone());

        GJChronology parisChrono = GJChronology.getInstance(PARIS);
        Assert.assertEquals(PARIS, parisChrono.getZone());

        GJChronology nullZone = GJChronology.getInstance(null);
        Assert.assertEquals(DateTimeZone.getDefault(), nullZone.getZone());

        Instant cutover = new Instant(0L);
        GJChronology customCutover = GJChronology.getInstance(PARIS, cutover);
        Assert.assertEquals(cutover, customCutover.getGregorianCutover());
        Assert.assertEquals(4, customCutover.getMinimumDaysInFirstWeek());

        GJChronology customCutoverAndDays = GJChronology.getInstance(PARIS, cutover, 3);
        Assert.assertEquals(3, customCutoverAndDays.getMinimumDaysInFirstWeek());

        GJChronology cached = GJChronology.getInstance(PARIS, cutover, 3);
        Assert.assertSame(customCutoverAndDays, cached);

        GJChronology nullCutoverChrono = GJChronology.getInstance(PARIS, (org.joda.time.ReadableInstant) null, 5);
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, nullCutoverChrono.getGregorianCutover());
        Assert.assertEquals(5, nullCutoverChrono.getMinimumDaysInFirstWeek());

        GJChronology cutoverLongDefault = GJChronology.getInstance(PARIS, GJChronology.DEFAULT_CUTOVER.getMillis(), 4);
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER, cutoverLongDefault.getGregorianCutover());

        GJChronology cutoverLongCustom = GJChronology.getInstance(PARIS, 1000000L, 4);
        Assert.assertEquals(new Instant(1000000L), cutoverLongCustom.getGregorianCutover());
    }

    @Test
    public void testWithZoneAndWithUTC() {
        GJChronology chronoUtc = GJChronology.getInstanceUTC();
        Assert.assertSame(chronoUtc, chronoUtc.withUTC());
        Assert.assertSame(chronoUtc, chronoUtc.withZone(DateTimeZone.UTC));

        Chronology chronoParis = chronoUtc.withZone(PARIS);
        Assert.assertEquals(PARIS, chronoParis.getZone());

        Chronology chronoNull = chronoParis.withZone(null);
        Assert.assertEquals(DateTimeZone.getDefault(), chronoNull.getZone());
    }

    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstance(TOKYO, new Instant(1000L), 4);
        GJChronology chrono2 = GJChronology.getInstance(TOKYO, new Instant(1000L), 4);
        GJChronology chrono3 = GJChronology.getInstance(PARIS, new Instant(1000L), 4);
        GJChronology chrono4 = GJChronology.getInstance(TOKYO, new Instant(2000L), 4);
        GJChronology chrono5 = GJChronology.getInstance(TOKYO, new Instant(1000L), 5);

        Assert.assertTrue(chrono1.equals(chrono1));
        Assert.assertTrue(chrono1.equals(chrono2));
        Assert.assertEquals(chrono1.hashCode(), chrono2.hashCode());

        Assert.assertFalse(chrono1.equals(chrono3));
        Assert.assertFalse(chrono1.equals(chrono4));
        Assert.assertFalse(chrono1.equals(chrono5));
        Assert.assertFalse(chrono1.equals("NotAChronology"));
        Assert.assertFalse(chrono1.equals(null));
    }

    @Test
    public void testToString() {
        GJChronology utc = GJChronology.getInstanceUTC();
        Assert.assertEquals("GJChronology[UTC]", utc.toString());

        GJChronology custom1 = GJChronology.getInstance(PARIS, new Instant(0L), 4);
        Assert.assertTrue(custom1.toString().contains("Europe/Paris"));
        Assert.assertTrue(custom1.toString().contains("cutover=1970-01-01"));

        GJChronology custom2 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(3600000L), 3);
        Assert.assertTrue(custom2.toString().contains("cutover=1970-01-01T01:00:00.000Z"));
        Assert.assertTrue(custom2.toString().contains("mdfw=3"));
    }

    @Test
    public void testSerialization() throws Exception {
        GJChronology chrono = GJChronology.getInstance(LONDON, new Instant(123456789L), 5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(chrono);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        GJChronology deserialized = (GJChronology) ois.readObject();

        Assert.assertSame(chrono, deserialized);
    }

    @Test
    public void testGetDateTimeMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        long instant1 = chrono.getDateTimeMillis(1582, 10, 15, 0);
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER.getMillis(), instant1);

        long instant2 = chrono.getDateTimeMillis(1582, 10, 4, 0);
        Assert.assertEquals(GJChronology.DEFAULT_CUTOVER.getMillis() - 86400000L, instant2);

        long instant3 = chrono.getDateTimeMillis(2000, 1, 1, 12, 30, 45, 500);
        Assert.assertEquals(12 * 3600000L + 30 * 60000L + 45 * 1000L + 500L, chrono.millisOfDay().get(instant3));

        GJChronology zonedChrono = GJChronology.getInstance(PARIS);
        long instantZoned = zonedChrono.getDateTimeMillis(2000, 1, 1, 1000);
        Assert.assertTrue(instantZoned != 0);

        long instantZoned7 = zonedChrono.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        Assert.assertTrue(instantZoned7 != 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisCutoverGap4Param() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisCutoverGap7Param() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 10, 12, 0, 0, 0);
    }

    @Test
    public void testGetDateTimeMillisJulianFeb29() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 0, 0, 0, 0);
        Assert.assertEquals(2, chrono.monthOfYear().get(millis));
        Assert.assertEquals(29, chrono.dayOfMonth().get(millis));
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testGetDateTimeMillisGregorianNonLeapFeb29() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1900, 2, 29, 0, 0, 0, 0);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testGetDateTimeMillisInvalidGregorianFeb29PostCutover() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L));
        chrono.getDateTimeMillis(1900, 2, 29, 0, 0, 0, 0);
    }

    @Test
    public void testNonMidnightCutoverAssemble() {
        Instant nonMidnightCutover = new Instant(3600000L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, nonMidnightCutover, 4);

        Assert.assertNotNull(chrono.millisOfSecond());
        Assert.assertNotNull(chrono.millisOfDay());
        Assert.assertNotNull(chrono.secondOfMinute());
        Assert.assertNotNull(chrono.secondOfDay());
        Assert.assertNotNull(chrono.minuteOfHour());
        Assert.assertNotNull(chrono.minuteOfDay());
        Assert.assertNotNull(chrono.hourOfDay());
        Assert.assertNotNull(chrono.hourOfHalfday());
        Assert.assertNotNull(chrono.clockhourOfDay());
        Assert.assertNotNull(chrono.clockhourOfHalfday());
        Assert.assertNotNull(chrono.halfdayOfDay());

        long inst = nonMidnightCutover.getMillis() - 1000L;
        Assert.assertEquals(59, chrono.secondOfMinute().get(inst));
    }

    @Test
    public void testCutoverFieldMethods() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        Assert.assertFalse(dayOfMonth.isLenient());

        long preCutover = GJChronology.DEFAULT_CUTOVER.getMillis() - 86400000L;
        long postCutover = GJChronology.DEFAULT_CUTOVER.getMillis();

        Assert.assertEquals(4, dayOfMonth.get(preCutover));
        Assert.assertEquals(15, dayOfMonth.get(postCutover));

        Assert.assertEquals("4", dayOfMonth.getAsText(preCutover, Locale.ENGLISH));
        Assert.assertEquals("15", dayOfMonth.getAsText(postCutover, Locale.ENGLISH));
        Assert.assertEquals("4", dayOfMonth.getAsShortText(preCutover, Locale.ENGLISH));
        Assert.assertEquals("15", dayOfMonth.getAsShortText(postCutover, Locale.ENGLISH));
        Assert.assertEquals("10", dayOfMonth.getAsText(10, Locale.ENGLISH));
        Assert.assertEquals("10", dayOfMonth.getAsShortText(10, Locale.ENGLISH));

        Assert.assertTrue(dayOfMonth.getMaximumTextLength(Locale.ENGLISH) >= 2);
        Assert.assertTrue(dayOfMonth.getMaximumShortTextLength(Locale.ENGLISH) >= 2);

        Assert.assertTrue(chrono.dayOfYear().isLeap(chrono.getDateTimeMillis(1500, 1, 1, 0)));
        Assert.assertFalse(chrono.dayOfYear().isLeap(chrono.getDateTimeMillis(1900, 1, 1, 0)));
        Assert.assertEquals(1, chrono.dayOfYear().getLeapAmount(chrono.getDateTimeMillis(1500, 1, 1, 0)));
        Assert.assertEquals(0, chrono.dayOfYear().getLeapAmount(chrono.getDateTimeMillis(1900, 1, 1, 0)));
        Assert.assertNotNull(chrono.dayOfYear().getLeapDurationField());

        Assert.assertEquals(1, dayOfMonth.getMinimumValue());
        Assert.assertEquals(31, dayOfMonth.getMaximumValue());
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(preCutover));
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(postCutover));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(preCutover));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(postCutover));

        long oct1582 = chrono.getDateTimeMillis(1582, 10, 15, 0);
        Assert.assertEquals(15, dayOfMonth.getMinimumValue(oct1582));

        long preRound = chrono.getDateTimeMillis(1582, 10, 4, 12, 0, 0, 0);
        long roundedFloor = dayOfMonth.roundFloor(preRound);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0), roundedFloor);

        long postRound = chrono.getDateTimeMillis(1582, 10, 15, 12, 0, 0, 0);
        long roundedCeil = dayOfMonth.roundCeiling(postRound);
        Assert.assertEquals(chrono.getDateTimeMillis(1582, 10, 16, 0, 0, 0, 0), roundedCeil);

        long crossedSet = dayOfMonth.set(oct1582, 4);
        Assert.assertEquals(4, dayOfMonth.get(crossedSet));

        long setWithText = dayOfMonth.set(oct1582, "16", Locale.ENGLISH);
        Assert.assertEquals(16, dayOfMonth.get(setWithText));

        long preSet = chrono.getDateTimeMillis(1582, 10, 4, 0);
        long crossedForwardSet = dayOfMonth.set(preSet, 15);
        Assert.assertEquals(15, dayOfMonth.get(crossedForwardSet));

        long crossedForwardText = dayOfMonth.set(preSet, "16", Locale.ENGLISH);
        Assert.assertEquals(16, dayOfMonth.get(crossedForwardText));

        long added = dayOfMonth.add(preCutover, 1);
        Assert.assertTrue(added != 0);
        long addedLong = dayOfMonth.add(preCutover, 1L);
        Assert.assertEquals(added, addedLong);

        int diff = dayOfMonth.getDifference(postCutover, preCutover);
        long diffLong = dayOfMonth.getDifferenceAsLong(postCutover, preCutover);
        Assert.assertEquals((long) diff, diffLong);
    }

    @Test
    public void testPartialCutoverMethods() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        LocalDate date = new LocalDate(1582, 10, 15, chrono);
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(date));
        Assert.assertEquals(1, dayOfMonth.getMinimumValue(date, new int[] {1582, 10, 15}));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(date));
        Assert.assertEquals(31, dayOfMonth.getMaximumValue(date, new int[] {1582, 10, 15}));

        Partial partial = new Partial(DateTimeFieldType.year(), 1582)
                .with(DateTimeFieldType.monthOfYear(), 10)
                .with(DateTimeFieldType.dayOfMonth(), 4);
        int[] result = dayOfMonth.add(partial, 2, new int[] {1582, 10, 4}, 1);
        Assert.assertEquals(1582, result[0]);
        Assert.assertEquals(10, result[1]);
        Assert.assertEquals(15, result[2]);

        int[] noOp = dayOfMonth.add(partial, 2, new int[] {1582, 10, 4}, 0);
        Assert.assertSame(noOp, noOp);
    }

    @Test
    public void testImpreciseCutoverField() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField yearField = chrono.year();
        DateTimeField monthField = chrono.monthOfYear();
        DateTimeField weekyearField = chrono.weekyear();

        long preCutover = chrono.getDateTimeMillis(1581, 10, 15, 0);
        long postCutover = chrono.getDateTimeMillis(1583, 10, 15, 0);

        long addedYears = yearField.add(preCutover, 2);
        Assert.assertEquals(1583, yearField.get(addedYears));

        long addedYearsLong = yearField.add(preCutover, 2L);
        Assert.assertEquals(1583, yearField.get(addedYearsLong));

        long addedYearsBack = yearField.add(postCutover, -2);
        Assert.assertEquals(1581, yearField.get(addedYearsBack));

        long addedYearsBackLong = yearField.add(postCutover, -2L);
        Assert.assertEquals(1581, yearField.get(addedYearsBackLong));

        Assert.assertEquals(2, yearField.getDifference(postCutover, preCutover));
        Assert.assertEquals(2L, yearField.getDifferenceAsLong(postCutover, preCutover));
        Assert.assertEquals(-2, yearField.getDifference(preCutover, postCutover));
        Assert.assertEquals(-2L, yearField.getDifferenceAsLong(preCutover, postCutover));

        long postCutover2 = chrono.getDateTimeMillis(1584, 10, 15, 0);
        Assert.assertEquals(1, yearField.getDifference(postCutover2, postCutover));
        Assert.assertEquals(1L, yearField.getDifferenceAsLong(postCutover2, postCutover));

        long preCutover2 = chrono.getDateTimeMillis(1580, 10, 15, 0);
        Assert.assertEquals(1, yearField.getDifference(preCutover, preCutover2));
        Assert.assertEquals(1L, yearField.getDifferenceAsLong(preCutover, preCutover2));

        Assert.assertEquals(1, monthField.getMinimumValue(preCutover));
        Assert.assertEquals(1, monthField.getMinimumValue(postCutover));
        Assert.assertEquals(12, monthField.getMaximumValue(preCutover));
        Assert.assertEquals(12, monthField.getMaximumValue(postCutover));

        long weekAdded = weekyearField.add(preCutover, 2);
        Assert.assertEquals(1583, weekyearField.get(weekAdded));

        DurationField yearsDuration = chrono.years();
        Assert.assertNotNull(yearsDuration);
        Assert.assertEquals(addedYears, yearsDuration.add(preCutover, 2));
        Assert.assertEquals(addedYearsLong, yearsDuration.add(preCutover, 2L));
        Assert.assertEquals(2, yearsDuration.getDifference(postCutover, preCutover));
        Assert.assertEquals(2L, yearsDuration.getDifferenceAsLong(postCutover, preCutover));
    }

    @Test
    public void testWeekyearConversions() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long cutover = GJChronology.DEFAULT_CUTOVER.getMillis();

        long julianInst = cutover - 1000L;
        long gregFromJul = chrono.julianToGregorianByWeekyear(julianInst);
        long backToJul = chrono.gregorianToJulianByWeekyear(gregFromJul);
        Assert.assertEquals(julianInst, backToJul);

        long gregInst = cutover + 1000L;
        long julFromGreg = chrono.gregorianToJulianByWeekyear(gregInst);
        long backToGreg = chrono.julianToGregorianByWeekyear(julFromGreg);
        Assert.assertEquals(gregInst, backToGreg);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testCutoverFieldSetInvalidStuck() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();
        long preGap = chrono.getDateTimeMillis(1582, 10, 4, 0);
        dayOfMonth.set(preGap, 10);
    }

    @Test
    public void testYearMonthAdditionAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonth ym = new YearMonth(1582, 9, chrono);
        YearMonth next = ym.plusMonths(2);
        Assert.assertEquals(1582, next.getYear());
        Assert.assertEquals(11, next.getMonthOfYear());
    }

    @Test
    public void testRoundFloorAndCeilingTransitions() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfYear = chrono.dayOfYear();

        long instant = chrono.getDateTimeMillis(1582, 1, 1, 12, 0, 0, 0);
        long ceil = dayOfYear.roundCeiling(instant);
        Assert.assertTrue(ceil > instant);

        long postInstant = chrono.getDateTimeMillis(1583, 1, 1, 12, 0, 0, 0);
        long floor = dayOfYear.roundFloor(postInstant);
        Assert.assertTrue(floor < postInstant);
    }
}
