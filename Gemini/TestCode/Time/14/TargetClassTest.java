package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MonthDay;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;
import org.joda.time.YearMonth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicMonthOfYearDateTimeFieldTest {

    private GregorianChronology iChronology;
    private BasicMonthOfYearDateTimeField iField;

    @Before
    public void setUp() {
        iChronology = GregorianChronology.getInstanceUTC();
        iField = new BasicMonthOfYearDateTimeField(iChronology, DateTimeConstants.FEBRUARY);
    }

    @Test
    public void testIsLenient() {
        Assert.assertFalse(iField.isLenient());
    }

    @Test
    public void testGet() {
        long millis = new DateTime(2023, 6, 15, 12, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(6, iField.get(millis));
    }

    @Test
    public void testGetMinimumValueAndMaximumValue() {
        Assert.assertEquals(DateTimeConstants.JANUARY, iField.getMinimumValue());
        Assert.assertEquals(12, iField.getMaximumValue());
    }

    @Test
    public void testGetRangeDurationField() {
        DurationField rangeField = iField.getRangeDurationField();
        Assert.assertEquals(iChronology.years(), rangeField);
    }

    @Test
    public void testGetLeapDurationField() {
        DurationField leapField = iField.getLeapDurationField();
        Assert.assertEquals(iChronology.days(), leapField);
    }

    @Test
    public void testIsLeapAndGetLeapAmount() {
        long leapYearFeb = new DateTime(2020, 2, 10, 0, 0, 0, 0, iChronology).getMillis();
        Assert.assertTrue(iField.isLeap(leapYearFeb));
        Assert.assertEquals(1, iField.getLeapAmount(leapYearFeb));

        long leapYearMar = new DateTime(2020, 3, 10, 0, 0, 0, 0, iChronology).getMillis();
        Assert.assertFalse(iField.isLeap(leapYearMar));
        Assert.assertEquals(0, iField.getLeapAmount(leapYearMar));

        long nonLeapYearFeb = new DateTime(2021, 2, 10, 0, 0, 0, 0, iChronology).getMillis();
        Assert.assertFalse(iField.isLeap(nonLeapYearFeb));
        Assert.assertEquals(0, iField.getLeapAmount(nonLeapYearFeb));
    }

    @Test
    public void testRoundFloorAndRemainder() {
        long instant = new DateTime(2023, 5, 20, 15, 30, 45, 123, iChronology).getMillis();
        long expectedFloor = new DateTime(2023, 5, 1, 0, 0, 0, 0, iChronology).getMillis();

        long floor = iField.roundFloor(instant);
        Assert.assertEquals(expectedFloor, floor);

        long remainder = iField.remainder(instant);
        Assert.assertEquals(instant - expectedFloor, remainder);
    }

    @Test
    public void testSet() {
        long instant = new DateTime(2023, 1, 15, 10, 0, 0, 0, iChronology).getMillis();
        long result = iField.set(instant, 8);
        long expected = new DateTime(2023, 8, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expected, result);

        long jan31 = new DateTime(2023, 1, 31, 5, 0, 0, 0, iChronology).getMillis();
        long toFeb = iField.set(jan31, 2);
        long expectedFeb = new DateTime(2023, 2, 28, 5, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedFeb, toFeb);

        long leapJan31 = new DateTime(2024, 1, 31, 5, 0, 0, 0, iChronology).getMillis();
        long toLeapFeb = iField.set(leapJan31, 2);
        long expectedLeapFeb = new DateTime(2024, 2, 29, 5, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedLeapFeb, toLeapFeb);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testSetBelowMin() {
        long instant = new DateTime(2023, 1, 15, 0, 0, 0, 0, iChronology).getMillis();
        iField.set(instant, 0);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testSetAboveMax() {
        long instant = new DateTime(2023, 1, 15, 0, 0, 0, 0, iChronology).getMillis();
        iField.set(instant, 13);
    }

    @Test
    public void testAddIntZero() {
        long instant = new DateTime(2023, 5, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(instant, iField.add(instant, 0));
    }

    @Test
    public void testAddIntPositive() {
        long instant = new DateTime(2023, 5, 15, 10, 0, 0, 0, iChronology).getMillis();
        long result = iField.add(instant, 3);
        long expected = new DateTime(2023, 8, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expected, result);

        long resultNextYear = iField.add(instant, 10);
        long expectedNextYear = new DateTime(2024, 3, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedNextYear, resultNextYear);
    }

    @Test
    public void testAddIntNegative() {
        long instant = new DateTime(2023, 5, 15, 10, 0, 0, 0, iChronology).getMillis();
        long result = iField.add(instant, -2);
        long expected = new DateTime(2023, 3, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expected, result);

        long resultPrevYear = iField.add(instant, -6);
        long expectedPrevYear = new DateTime(2022, 11, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedPrevYear, resultPrevYear);

        long jan = new DateTime(2023, 1, 15, 10, 0, 0, 0, iChronology).getMillis();
        long minus12 = iField.add(jan, -12);
        long expectedMinus12 = new DateTime(2022, 1, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedMinus12, minus12);

        long minus24 = iField.add(jan, -24);
        long expectedMinus24 = new DateTime(2021, 1, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedMinus24, minus24);

        long minus1 = iField.add(jan, -1);
        long expectedMinus1 = new DateTime(2022, 12, 15, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedMinus1, minus1);
    }

    @Test
    public void testAddIntCoerceDayOfMonth() {
        long jan31 = new DateTime(2023, 1, 31, 0, 0, 0, 0, iChronology).getMillis();
        long toFeb = iField.add(jan31, 1);
        long expectedFeb = new DateTime(2023, 2, 28, 0, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedFeb, toFeb);

        long mar31 = new DateTime(2024, 3, 31, 0, 0, 0, 0, iChronology).getMillis();
        long toLeapFeb = iField.add(mar31, -1);
        long expectedLeapFeb = new DateTime(2024, 2, 29, 0, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedLeapFeb, toLeapFeb);
    }

    @Test
    public void testAddLongWithinIntRange() {
        long instant = new DateTime(2023, 5, 15, 10, 0, 0, 0, iChronology).getMillis();
        long resInt = iField.add(instant, 5);
        long resLong = iField.add(instant, 5L);
        Assert.assertEquals(resInt, resLong);
    }

    @Test
    public void testAddLongLargeAmounts() {
        long instant = new DateTime(2000, 1, 15, 10, 0, 0, 0, iChronology).getMillis();
        long largeMonths = 3000000L * 12L;
        long result = iField.add(instant, largeMonths);
        long expectedYear = 2000 + 3000000L;
        Assert.assertEquals(expectedYear, iChronology.getYear(result));
        Assert.assertEquals(1, iChronology.getMonthOfYear(result));

        long negativeLargeMonths = -1000000L * 12L;
        long negResult = iField.add(instant, negativeLargeMonths);
        Assert.assertEquals(2000 - 1000000L, iChronology.getYear(negResult));
        Assert.assertEquals(1, iChronology.getMonthOfYear(negResult));

        long negNonExactYear = -1000000L * 12L - 1L;
        long negNonExactRes = iField.add(instant, negNonExactYear);
        Assert.assertEquals(2000 - 1000000L - 1, iChronology.getYear(negNonExactRes));
        Assert.assertEquals(12, iChronology.getMonthOfYear(negNonExactRes));

        long jan31 = new DateTime(2000, 1, 31, 10, 0, 0, 0, iChronology).getMillis();
        long addLargeToFeb = iField.add(jan31, 3000000L * 12L + 1L);
        Assert.assertEquals(2, iChronology.getMonthOfYear(addLargeToFeb));
        int daysInFeb = iChronology.getDaysInYearMonth(2000 + 3000000, 2);
        Assert.assertEquals(daysInFeb, iChronology.getDayOfMonth(addLargeToFeb));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLongTooLargePositive() {
        long instant = new DateTime(2000, 1, 1, 0, 0, 0, 0, iChronology).getMillis();
        iField.add(instant, Long.MAX_VALUE / 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLongTooLargeNegative() {
        long instant = new DateTime(2000, 1, 1, 0, 0, 0, 0, iChronology).getMillis();
        iField.add(instant, Long.MIN_VALUE / 2);
    }

    @Test
    public void testAddReadablePartialContiguous() {
        YearMonth ym = new YearMonth(2023, 10, iChronology);
        int[] values = ym.getValues();
        int[] result = iField.add(ym, 1, values, 5);
        Assert.assertEquals(2024, result[0]);
        Assert.assertEquals(3, result[1]);

        int[] zeroResult = iField.add(ym, 1, values, 0);
        Assert.assertArrayEquals(values, zeroResult);

        MonthDay md = new MonthDay(2, 29, iChronology);
        int[] mdValues = md.getValues();
        int[] mdResult = iField.add(md, 0, mdValues, 12);
        Assert.assertEquals(2, mdResult[0]);
        Assert.assertEquals(28, mdResult[1]);
    }

    @Test
    public void testAddReadablePartialNonContiguous() {
        Partial nonContiguous = new Partial()
            .with(DateTimeFieldType.monthOfYear(), 5)
            .with(DateTimeFieldType.minuteOfHour(), 30);
        int[] values = nonContiguous.getValues();
        int[] result = iField.add(nonContiguous, 0, values, 3);
        Assert.assertEquals(8, result[0]);
        Assert.assertEquals(30, result[1]);
    }

    @Test
    public void testAddWrapField() {
        long instant = new DateTime(2023, 10, 15, 12, 0, 0, 0, iChronology).getMillis();
        long result = iField.addWrapField(instant, 4);
        long expected = new DateTime(2023, 2, 15, 12, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expected, result);

        long jan31 = new DateTime(2023, 1, 31, 12, 0, 0, 0, iChronology).getMillis();
        long wrapToFeb = iField.addWrapField(jan31, 1);
        long expectedFeb = new DateTime(2023, 2, 28, 12, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(expectedFeb, wrapToFeb);
    }

    @Test
    public void testGetDifferenceAsLong() {
        long start = new DateTime(2023, 1, 15, 12, 0, 0, 0, iChronology).getMillis();
        long end = new DateTime(2024, 6, 15, 12, 0, 0, 0, iChronology).getMillis();

        Assert.assertEquals(17L, iField.getDifferenceAsLong(end, start));
        Assert.assertEquals(-17L, iField.getDifferenceAsLong(start, end));

        long endEarlierDay = new DateTime(2024, 6, 14, 12, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(16L, iField.getDifferenceAsLong(endEarlierDay, start));

        long endEarlierTime = new DateTime(2024, 6, 15, 11, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(16L, iField.getDifferenceAsLong(endEarlierTime, start));

        long endSame = new DateTime(2023, 1, 15, 12, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(0L, iField.getDifferenceAsLong(endSame, start));

        long feb28 = new DateTime(2023, 2, 28, 10, 0, 0, 0, iChronology).getMillis();
        long mar31 = new DateTime(2023, 3, 31, 10, 0, 0, 0, iChronology).getMillis();
        Assert.assertEquals(1L, iField.getDifferenceAsLong(mar31, feb28));
        Assert.assertEquals(-1L, iField.getDifferenceAsLong(feb28, mar31));
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(iField);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertSame(iChronology.monthOfYear(), deserialized);
    }
}
