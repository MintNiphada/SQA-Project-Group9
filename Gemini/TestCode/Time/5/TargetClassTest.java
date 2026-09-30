package org.joda.time;

import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.junit.Assert;
import org.junit.Test;

public class PeriodTest {

    @Test
    public void testConstantsAndZero() {
        Assert.assertNotNull(Period.ZERO);
        Assert.assertEquals(0, Period.ZERO.getYears());
        Assert.assertEquals(0, Period.ZERO.getMonths());
        Assert.assertEquals(0, Period.ZERO.getWeeks());
        Assert.assertEquals(0, Period.ZERO.getDays());
        Assert.assertEquals(0, Period.ZERO.getHours());
        Assert.assertEquals(0, Period.ZERO.getMinutes());
        Assert.assertEquals(0, Period.ZERO.getSeconds());
        Assert.assertEquals(0, Period.ZERO.getMillis());
        Assert.assertEquals(PeriodType.standard(), Period.ZERO.getPeriodType());
    }

    @Test
    public void testFactoryMethods() {
        Period py = Period.years(5);
        Assert.assertEquals(5, py.getYears());
        Assert.assertEquals(0, py.getMonths());

        Period pm = Period.months(6);
        Assert.assertEquals(6, pm.getMonths());

        Period pw = Period.weeks(7);
        Assert.assertEquals(7, pw.getWeeks());

        Period pd = Period.days(8);
        Assert.assertEquals(8, pd.getDays());

        Period ph = Period.hours(9);
        Assert.assertEquals(9, ph.getHours());

        Period pmin = Period.minutes(10);
        Assert.assertEquals(10, pmin.getMinutes());

        Period ps = Period.seconds(11);
        Assert.assertEquals(11, ps.getSeconds());

        Period pms = Period.millis(12);
        Assert.assertEquals(12, pms.getMillis());
    }

    @Test
    public void testParse() {
        Period p = Period.parse("P1Y2M3W4DT5H6M7.008S");
        Assert.assertEquals(1, p.getYears());
        Assert.assertEquals(2, p.getMonths());
        Assert.assertEquals(3, p.getWeeks());
        Assert.assertEquals(4, p.getDays());
        Assert.assertEquals(5, p.getHours());
        Assert.assertEquals(6, p.getMinutes());
        Assert.assertEquals(7, p.getSeconds());
        Assert.assertEquals(8, p.getMillis());

        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p2 = Period.parse("PT1H", formatter);
        Assert.assertEquals(1, p2.getHours());
    }

    @Test
    public void testFieldDifferenceSuccess() {
        LocalDate start = new LocalDate(2005, 6, 9);
        LocalDate end = new LocalDate(2007, 4, 12);
        Period diff = Period.fieldDifference(start, end);
        Assert.assertEquals(2, diff.getYears());
        Assert.assertEquals(-2, diff.getMonths());
        Assert.assertEquals(3, diff.getDays());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifferenceNullStart() {
        Period.fieldDifference(null, new LocalDate(2007, 4, 12));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifferenceNullEnd() {
        Period.fieldDifference(new LocalDate(2005, 6, 9), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifferenceDifferentSizes() {
        Period.fieldDifference(new LocalDate(2005, 6, 9), new YearMonth(2007, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifferenceDifferentTypes() {
        Partial p1 = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial p2 = new Partial(DateTimeFieldType.minuteOfHour(), 10);
        Period.fieldDifference(p1, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifferenceOverlappingFields() {
        Partial p1 = new Partial()
                .with(DateTimeFieldType.dayOfMonth(), 5)
                .with(DateTimeFieldType.dayOfWeek(), 2);
        Partial p2 = new Partial()
                .with(DateTimeFieldType.dayOfMonth(), 10)
                .with(DateTimeFieldType.dayOfWeek(), 4);
        Period.fieldDifference(p1, p2);
    }

    @Test
    public void testConstructors() {
        Period p0 = new Period();
        Assert.assertEquals(0, p0.getDays());

        Period p4 = new Period(1, 2, 3, 4);
        Assert.assertEquals(1, p4.getHours());
        Assert.assertEquals(2, p4.getMinutes());
        Assert.assertEquals(3, p4.getSeconds());
        Assert.assertEquals(4, p4.getMillis());

        Period p8 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Assert.assertEquals(1, p8.getYears());
        Assert.assertEquals(2, p8.getMonths());
        Assert.assertEquals(3, p8.getWeeks());
        Assert.assertEquals(4, p8.getDays());
        Assert.assertEquals(5, p8.getHours());
        Assert.assertEquals(6, p8.getMinutes());
        Assert.assertEquals(7, p8.getSeconds());
        Assert.assertEquals(8, p8.getMillis());

        Period p8Typed = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        Assert.assertEquals(8, p8Typed.getMillis());

        Period pDur1 = new Period(1000L);
        Assert.assertEquals(1, pDur1.getSeconds());

        Period pDur2 = new Period(1000L, PeriodType.seconds());
        Assert.assertEquals(1, pDur2.getSeconds());

        Period pDur3 = new Period(1000L, ISOChronology.getInstanceUTC());
        Assert.assertEquals(1, pDur3.getSeconds());

        Period pDur4 = new Period(1000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        Assert.assertEquals(1, pDur4.getSeconds());

        Period pInstants1 = new Period(0L, 5000L);
        Assert.assertEquals(5, pInstants1.getSeconds());

        Period pInstants2 = new Period(0L, 5000L, PeriodType.standard());
        Assert.assertEquals(5, pInstants2.getSeconds());

        Period pInstants3 = new Period(0L, 5000L, ISOChronology.getInstanceUTC());
        Assert.assertEquals(5, pInstants3.getSeconds());

        Period pInstants4 = new Period(0L, 5000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        Assert.assertEquals(5, pInstants4.getSeconds());

        DateTime dt1 = new DateTime(2000, 1, 1, 0, 0, ISOChronology.getInstanceUTC());
        DateTime dt2 = new DateTime(2000, 1, 2, 0, 0, ISOChronology.getInstanceUTC());
        Period pReadInst1 = new Period(dt1, dt2);
        Assert.assertEquals(1, pReadInst1.getDays());

        Period pReadInst2 = new Period(dt1, dt2, PeriodType.hours());
        Assert.assertEquals(24, pReadInst2.getHours());

        LocalDate ld1 = new LocalDate(2000, 1, 1);
        LocalDate ld2 = new LocalDate(2000, 1, 2);
        Period pPart1 = new Period(ld1, ld2);
        Assert.assertEquals(1, pPart1.getDays());

        Period pPart2 = new Period(ld1, ld2, PeriodType.days());
        Assert.assertEquals(1, pPart2.getDays());

        Duration dur = new Duration(60000L);
        Period pDurInst1 = new Period(dt1, dur);
        Assert.assertEquals(1, pDurInst1.getMinutes());

        Period pDurInst2 = new Period(dt1, dur, PeriodType.standard());
        Assert.assertEquals(1, pDurInst2.getMinutes());

        Period pDurInst3 = new Period(dur, dt2);
        Assert.assertEquals(1, pDurInst3.getMinutes());

        Period pDurInst4 = new Period(dur, dt2, PeriodType.standard());
        Assert.assertEquals(1, pDurInst4.getMinutes());

        Period pObj1 = new Period("PT15M");
        Assert.assertEquals(15, pObj1.getMinutes());

        Period pObj2 = new Period("PT15M", PeriodType.standard());
        Assert.assertEquals(15, pObj2.getMinutes());

        Period pObj3 = new Period("PT15M", ISOChronology.getInstanceUTC());
        Assert.assertEquals(15, pObj3.getMinutes());

        Period pObj4 = new Period("PT15M", PeriodType.standard(), ISOChronology.getInstanceUTC());
        Assert.assertEquals(15, pObj4.getMinutes());
    }

    @Test
    public void testToPeriod() {
        Period p = Period.days(3);
        Assert.assertSame(p, p.toPeriod());
    }

    @Test
    public void testWithPeriodType() {
        Period p = new Period(1, 2, 0, 4, 0, 0, 0, 0);
        Period same = p.withPeriodType(PeriodType.standard());
        Assert.assertSame(p, same);

        Period pYearMonth = p.withPeriodType(PeriodType.yearMonthDayTime());
        Assert.assertEquals(PeriodType.yearMonthDayTime(), pYearMonth.getPeriodType());
        Assert.assertEquals(1, pYearMonth.getYears());
        Assert.assertEquals(2, pYearMonth.getMonths());
        Assert.assertEquals(4, pYearMonth.getDays());

        Period pNull = p.withPeriodType(null);
        Assert.assertEquals(PeriodType.standard(), pNull.getPeriodType());
    }

    @Test
    public void testWithFields() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Assert.assertSame(p, p.withFields(null));

        Period update = new Period(10, 0, 0, 0, 0, 0, 0, 0);
        Period result = p.withFields(update);
        Assert.assertEquals(10, result.getYears());
        Assert.assertEquals(2, result.getMonths());
    }

    @Test
    public void testWithField() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period modified = p.withField(DurationFieldType.years(), 20);
        Assert.assertEquals(20, modified.getYears());
        Assert.assertEquals(2, modified.getMonths());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldNull() {
        Period p = Period.days(1);
        p.withField(null, 5);
    }

    @Test
    public void testWithFieldAdded() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Assert.assertSame(p, p.withFieldAdded(DurationFieldType.years(), 0));

        Period result = p.withFieldAdded(DurationFieldType.years(), 5);
        Assert.assertEquals(6, result.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddedNull() {
        Period p = Period.days(1);
        p.withFieldAdded(null, 5);
    }

    @Test
    public void testWithXxxMethods() {
        Period p = new Period();
        p = p.withYears(1)
             .withMonths(2)
             .withWeeks(3)
             .withDays(4)
             .withHours(5)
             .withMinutes(6)
             .withSeconds(7)
             .withMillis(8);

        Assert.assertEquals(1, p.getYears());
        Assert.assertEquals(2, p.getMonths());
        Assert.assertEquals(3, p.getWeeks());
        Assert.assertEquals(4, p.getDays());
        Assert.assertEquals(5, p.getHours());
        Assert.assertEquals(6, p.getMinutes());
        Assert.assertEquals(7, p.getSeconds());
        Assert.assertEquals(8, p.getMillis());
    }

    @Test
    public void testPlusReadablePeriod() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Assert.assertSame(p1, p1.plus(null));

        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period sum = p1.plus(p2);
        Assert.assertEquals(2, sum.getYears());
        Assert.assertEquals(3, sum.getMonths());
        Assert.assertEquals(4, sum.getWeeks());
        Assert.assertEquals(5, sum.getDays());
        Assert.assertEquals(6, sum.getHours());
        Assert.assertEquals(7, sum.getMinutes());
        Assert.assertEquals(8, sum.getSeconds());
        Assert.assertEquals(9, sum.getMillis());
    }

    @Test
    public void testPlusXxxMethods() {
        Period p = new Period();
        Assert.assertSame(p, p.plusYears(0));
        Assert.assertSame(p, p.plusMonths(0));
        Assert.assertSame(p, p.plusWeeks(0));
        Assert.assertSame(p, p.plusDays(0));
        Assert.assertSame(p, p.plusHours(0));
        Assert.assertSame(p, p.plusMinutes(0));
        Assert.assertSame(p, p.plusSeconds(0));
        Assert.assertSame(p, p.plusMillis(0));

        p = p.plusYears(1)
             .plusMonths(2)
             .plusWeeks(3)
             .plusDays(4)
             .plusHours(5)
             .plusMinutes(6)
             .plusSeconds(7)
             .plusMillis(8);

        Assert.assertEquals(1, p.getYears());
        Assert.assertEquals(2, p.getMonths());
        Assert.assertEquals(3, p.getWeeks());
        Assert.assertEquals(4, p.getDays());
        Assert.assertEquals(5, p.getHours());
        Assert.assertEquals(6, p.getMinutes());
        Assert.assertEquals(7, p.getSeconds());
        Assert.assertEquals(8, p.getMillis());
    }

    @Test
    public void testMinusReadablePeriod() {
        Period p1 = new Period(2, 3, 4, 5, 6, 7, 8, 9);
        Assert.assertSame(p1, p1.minus(null));

        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period diff = p1.minus(p2);
        Assert.assertEquals(1, diff.getYears());
        Assert.assertEquals(2, diff.getMonths());
        Assert.assertEquals(3, diff.getWeeks());
        Assert.assertEquals(4, diff.getDays());
        Assert.assertEquals(5, diff.getHours());
        Assert.assertEquals(6, diff.getMinutes());
        Assert.assertEquals(7, diff.getSeconds());
        Assert.assertEquals(8, diff.getMillis());
    }

    @Test
    public void testMinusXxxMethods() {
        Period p = new Period(10, 10, 10, 10, 10, 10, 10, 10);
        p = p.minusYears(1)
             .minusMonths(2)
             .minusWeeks(3)
             .minusDays(4)
             .minusHours(5)
             .minusMinutes(6)
             .minusSeconds(7)
             .minusMillis(8);

        Assert.assertEquals(9, p.getYears());
        Assert.assertEquals(8, p.getMonths());
        Assert.assertEquals(7, p.getWeeks());
        Assert.assertEquals(6, p.getDays());
        Assert.assertEquals(5, p.getHours());
        Assert.assertEquals(4, p.getMinutes());
        Assert.assertEquals(3, p.getSeconds());
        Assert.assertEquals(2, p.getMillis());
    }

    @Test
    public void testMultipliedByAndNegated() {
        Period zero = Period.ZERO;
        Assert.assertSame(zero, zero.multipliedBy(5));

        Period p = new Period(1, -2, 3, 4, 5, 6, 7, 8);
        Assert.assertSame(p, p.multipliedBy(1));

        Period mult = p.multipliedBy(2);
        Assert.assertEquals(2, mult.getYears());
        Assert.assertEquals(-4, mult.getMonths());
        Assert.assertEquals(6, mult.getWeeks());
        Assert.assertEquals(8, mult.getDays());
        Assert.assertEquals(10, mult.getHours());
        Assert.assertEquals(12, mult.getMinutes());
        Assert.assertEquals(14, mult.getSeconds());
        Assert.assertEquals(16, mult.getMillis());

        Period neg = p.negated();
        Assert.assertEquals(-1, neg.getYears());
        Assert.assertEquals(2, neg.getMonths());
        Assert.assertEquals(-3, neg.getWeeks());
        Assert.assertEquals(-4, neg.getDays());
        Assert.assertEquals(-5, neg.getHours());
        Assert.assertEquals(-6, neg.getMinutes());
        Assert.assertEquals(-7, neg.getSeconds());
        Assert.assertEquals(-8, neg.getMillis());
    }

    @Test
    public void testToStandardWeeks() {
        Period p = new Period(0, 0, 2, 7, 0, 0, 0, 0);
        Assert.assertEquals(Weeks.weeks(3), p.toStandardWeeks());

        Period complex = new Period(0, 0, 1, 6, 23, 59, 59, 1000);
        Assert.assertEquals(Weeks.weeks(2), complex.toStandardWeeks());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardWeeksWithYearsThrows() {
        Period.years(1).toStandardWeeks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardWeeksWithMonthsThrows() {
        Period.months(1).toStandardWeeks();
    }

    @Test
    public void testToStandardDays() {
        Period p = new Period(0, 0, 2, 3, 24, 0, 0, 0);
        Assert.assertEquals(Days.days(18), p.toStandardDays());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDaysWithYearsThrows() {
        Period.years(1).toStandardDays();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDaysWithMonthsThrows() {
        Period.months(1).toStandardDays();
    }

    @Test
    public void testToStandardHours() {
        Period p = new Period(0, 0, 1, 1, 2, 120, 0, 0);
        // 1 week = 168h, 1 day = 24h, 2h, 120min = 2h => 196 hours
        Assert.assertEquals(Hours.hours(196), p.toStandardHours());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardHoursWithYearsThrows() {
        Period.years(1).toStandardHours();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardHoursWithMonthsThrows() {
        Period.months(1).toStandardHours();
    }

    @Test
    public void testToStandardMinutes() {
        Period p = new Period(0, 0, 0, 1, 1, 1, 60, 0);
        // 1 day = 1440m, 1h = 60m, 1m, 60s = 1m => 1502 minutes
        Assert.assertEquals(Minutes.minutes(1502), p.toStandardMinutes());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardMinutesWithYearsThrows() {
        Period.years(1).toStandardMinutes();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardMinutesWithMonthsThrows() {
        Period.months(1).toStandardMinutes();
    }

    @Test
    public void testToStandardSeconds() {
        Period p = new Period(0, 0, 0, 0, 1, 1, 1, 1000);
        // 1h = 3600s, 1m = 60s, 1s, 1000ms = 1s => 3662 seconds
        Assert.assertEquals(Seconds.seconds(3662), p.toStandardSeconds());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardSecondsWithYearsThrows() {
        Period.years(1).toStandardSeconds();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardSecondsWithMonthsThrows() {
        Period.months(1).toStandardSeconds();
    }

    @Test
    public void testToStandardDuration() {
        Period p = new Period(0, 0, 1, 1, 1, 1, 1, 1);
        long expectedMillis = (7L * 24 * 3600 * 1000)
                + (24L * 3600 * 1000)
                + (3600 * 1000)
                + (60 * 1000)
                + 1000
                + 1;
        Assert.assertEquals(new Duration(expectedMillis), p.toStandardDuration());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDurationWithYearsThrows() {
        Period.years(1).toStandardDuration();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDurationWithMonthsThrows() {
        Period.months(1).toStandardDuration();
    }

    @Test
    public void testNormalizedStandard() {
        Period p = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period norm = p.normalizedStandard();
        Assert.assertEquals(2, norm.getYears());
        Assert.assertEquals(3, norm.getMonths());

        Period pTime = new Period(0, 0, 0, 0, 0, 125, 65, 1005);
        Period normTime = pTime.normalizedStandard();
        Assert.assertEquals(2, normTime.getHours());
        Assert.assertEquals(6, normTime.getMinutes());
        Assert.assertEquals(6, normTime.getSeconds());
        Assert.assertEquals(5, normTime.getMillis());

        Period pCustomType = new Period(1, 15, 0, 10, 0, 0, 0, 0);
        Period normCustom = pCustomType.normalizedStandard(PeriodType.yearMonthDayTime());
        Assert.assertEquals(2, normCustom.getYears());
        Assert.assertEquals(3, normCustom.getMonths());
        Assert.assertEquals(10, normCustom.getDays());
        Assert.assertEquals(0, normCustom.getWeeks());

        Period pNegativeMonths = new Period(2, -15, 0, 0, 0, 0, 0, 0);
        Period normNeg = pNegativeMonths.normalizedStandard();
        Assert.assertEquals(1, normNeg.getYears());
        Assert.assertEquals(-3, normNeg.getMonths());
    }

    @Test
    public void testNormalizedStandardNoYearsOrMonths() {
        Period p = new Period(0, 0, 2, 8, 25, 70, 80, 2500);
        Period norm = p.normalizedStandard();
        Assert.assertEquals(0, norm.getYears());
        Assert.assertEquals(0, norm.getMonths());
        Assert.assertEquals(3, norm.getWeeks());
        Assert.assertEquals(2, norm.getDays());
        Assert.assertEquals(2, norm.getHours());
        Assert.assertEquals(11, norm.getMinutes());
        Assert.assertEquals(22, norm.getSeconds());
        Assert.assertEquals(500, norm.getMillis());
    }
}
