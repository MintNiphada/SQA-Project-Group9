package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PeriodFormatterBuilderTest {

    private PeriodFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new PeriodFormatterBuilder();
    }

    @Test
    public void testClearAndReset() {
        builder.appendYears().appendLiteral("Y");
        Assert.assertNotNull(builder.toFormatter());
        builder.clear();
        builder.appendMonths().appendLiteral("M");
        PeriodFormatter f = builder.toFormatter();
        Assert.assertEquals("5M", f.print(new Period().withMonths(5)));
    }

    @Test
    public void testAllFieldsPrinting() {
        PeriodFormatter f = builder
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .appendWeeks().appendSuffix("w")
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("min")
            .appendSeconds().appendSuffix("s")
            .appendMillis().appendSuffix("ms")
            .toFormatter();

        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Assert.assertEquals("1y2m3w4d5h6min7s8ms", f.print(p));

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "1y2m3w4d5h6min7s8ms", 0);
        Assert.assertEquals(21, pos);
        Assert.assertEquals(1, mp.getYears());
        Assert.assertEquals(2, mp.getMonths());
        Assert.assertEquals(3, mp.getWeeks());
        Assert.assertEquals(4, mp.getDays());
        Assert.assertEquals(5, mp.getHours());
        Assert.assertEquals(6, mp.getMinutes());
        Assert.assertEquals(7, mp.getSeconds());
        Assert.assertEquals(8, mp.getMillis());
    }

    @Test
    public void testPrintZeroSettings() {
        // Rarely last (default)
        PeriodFormatter f1 = new PeriodFormatterBuilder()
            .printZeroRarelyLast()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .toFormatter();
        Assert.assertEquals("0m", f1.print(Period.ZERO));
        Assert.assertEquals("2y", f1.print(new Period().withYears(2)));

        // Rarely first
        PeriodFormatter f2 = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .toFormatter();
        Assert.assertEquals("0y", f2.print(Period.ZERO));
        Assert.assertEquals("2m", f2.print(new Period().withMonths(2)));

        // Never
        PeriodFormatter f3 = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .toFormatter();
        Assert.assertEquals("", f3.print(Period.ZERO));

        // Always
        PeriodFormatter f4 = new PeriodFormatterBuilder()
            .printZeroAlways()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .toFormatter();
        Assert.assertEquals("0y0m", f4.print(Period.ZERO));

        // If Supported
        PeriodFormatter f5 = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .toFormatter();
        Period hoursOnly = new Period(0, 0, 0, 0, 5, 0, 0, 0, PeriodType.hours());
        Assert.assertEquals("", f5.print(hoursOnly));
    }

    @Test
    public void testSecondsWithMillis() {
        PeriodFormatter f = builder
            .appendSecondsWithMillis()
            .appendSuffix("s")
            .toFormatter();

        Period p = new Period().withSeconds(5).withMillis(120);
        Assert.assertEquals("5.120s", f.print(p));

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "5.120s", 0);
        Assert.assertEquals(5, mp.getSeconds());
        Assert.assertEquals(120, mp.getMillis());

        // Test with 1 digit millis
        mp = new MutablePeriod();
        f.parseInto(mp, "5.1s", 0);
        Assert.assertEquals(5, mp.getSeconds());
        Assert.assertEquals(100, mp.getMillis());

        // Test with 2 digit millis
        mp = new MutablePeriod();
        f.parseInto(mp, "5.12s", 0);
        Assert.assertEquals(5, mp.getSeconds());
        Assert.assertEquals(120, mp.getMillis());

        // Negative values
        mp = new MutablePeriod();
        f.parseInto(mp, "-5.120s", 0);
        Assert.assertEquals(-5, mp.getSeconds());
        Assert.assertEquals(-120, mp.getMillis());
    }

    @Test
    public void testSecondsWithOptionalMillis() {
        PeriodFormatter f = builder
            .appendSecondsWithOptionalMillis()
            .appendSuffix("s")
            .toFormatter();

        Assert.assertEquals("5s", f.print(new Period().withSeconds(5)));
        Assert.assertEquals("5.050s", f.print(new Period().withSeconds(5).withMillis(50)));

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "5s", 0);
        Assert.assertEquals(5, mp.getSeconds());
        Assert.assertEquals(0, mp.getMillis());

        mp = new MutablePeriod();
        f.parseInto(mp, "5,050s", 0);
        Assert.assertEquals(5, mp.getSeconds());
        Assert.assertEquals(50, mp.getMillis());
    }

    @Test
    public void testAppendMillis3Digit() {
        PeriodFormatter f = builder
            .appendMillis3Digit()
            .toFormatter();

        Assert.assertEquals("005", f.print(new Period().withMillis(5)));
        Assert.assertEquals("050", f.print(new Period().withMillis(50)));
        Assert.assertEquals("500", f.print(new Period().withMillis(500)));
    }

    @Test
    public void testMinAndMaxDigits() {
        PeriodFormatter f = builder
            .minimumPrintedDigits(3)
            .maximumParsedDigits(4)
            .appendYears()
            .toFormatter();

        Assert.assertEquals("005", f.print(new Period().withYears(5)));

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "123456", 0);
        Assert.assertEquals(4, pos);
        Assert.assertEquals(1234, mp.getYears());
    }

    @Test
    public void testRejectSignedValues() {
        PeriodFormatter f = builder
            .rejectSignedValues(true)
            .appendYears()
            .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "-12", 0);
        Assert.assertTrue(pos < 0);
    }

    @Test
    public void testAffixes() {
        PeriodFormatter f = builder
            .appendPrefix("Y:")
            .appendYears()
            .appendSuffix(" year", " years")
            .appendPrefix("M:")
            .appendMonths()
            .appendSuffix(" month", " months")
            .toFormatter();

        Assert.assertEquals("Y:1 yearM:2 months", f.print(new Period().withYears(1).withMonths(2)));

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "Y:1 yearM:2 months", 0);
        Assert.assertEquals(1, mp.getYears());
        Assert.assertEquals(2, mp.getMonths());

        // Composite prefix
        PeriodFormatter f2 = new PeriodFormatterBuilder()
            .appendPrefix("A-")
            .appendPrefix("B-")
            .appendDays()
            .toFormatter();
        Assert.assertEquals("A-B-3", f2.print(new Period().withDays(3)));

        mp = new MutablePeriod();
        f2.parseInto(mp, "A-B-3", 0);
        Assert.assertEquals(3, mp.getDays());
    }

    @Test
    public void testPluralAffixSwappedLength() {
        PeriodFormatter f = builder
            .appendPrefix("sing", "p")
            .appendYears()
            .appendSuffix("s", "plural")
            .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "sing1s", 0);
        Assert.assertEquals(1, mp.getYears());

        mp = new MutablePeriod();
        f.parseInto(mp, "p2plural", 0);
        Assert.assertEquals(2, mp.getYears());
    }

    @Test
    public void testSeparators() throws IOException {
        PeriodFormatter f = builder
            .appendYears()
            .appendSeparator(", ", " and ", new String[]{" or ", " & "})
            .appendMonths()
            .appendSeparator(", ", " and ")
            .appendDays()
            .toFormatter();

        Assert.assertEquals("1 year length", 13, f.calculatePrintedLength(new Period(1, 2, 0, 3, 0, 0, 0, 0)));
        Assert.assertEquals("1, 2 and 3", f.print(new Period(1, 2, 0, 3, 0, 0, 0, 0)));
        Assert.assertEquals("1 and 2", f.print(new Period(1, 2, 0, 0, 0, 0, 0, 0)));
        Assert.assertEquals("1", f.print(new Period(1, 0, 0, 0, 0, 0, 0, 0)));

        StringWriter sw = new StringWriter();
        f.printTo(sw, new Period(1, 2, 0, 3, 0, 0, 0, 0));
        Assert.assertEquals("1, 2 and 3", sw.toString());

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "1 or 2 and 3", 0);
        Assert.assertEquals(1, mp.getYears());
        Assert.assertEquals(2, mp.getMonths());
        Assert.assertEquals(3, mp.getDays());

        mp = new MutablePeriod();
        f.parseInto(mp, "1 & 2 and 3", 0);
        Assert.assertEquals(1, mp.getYears());
        Assert.assertEquals(2, mp.getMonths());
        Assert.assertEquals(3, mp.getDays());
    }

    @Test
    public void testSeparatorIfFieldsBeforeAndAfter() {
        PeriodFormatter fBefore = new PeriodFormatterBuilder()
            .appendYears()
            .appendSeparatorIfFieldsBefore(";")
            .appendMonths()
            .toFormatter();
        Assert.assertEquals("1;2", fBefore.print(new Period().withYears(1).withMonths(2)));
        Assert.assertEquals("1;", fBefore.print(new Period().withYears(1)));
        Assert.assertEquals("2", fBefore.print(new Period().withMonths(2)));

        PeriodFormatter fAfter = new PeriodFormatterBuilder()
            .appendSeparatorIfFieldsAfter("T")
            .appendHours()
            .toFormatter();
        Assert.assertEquals("T5", fAfter.print(new Period().withHours(5)));
        Assert.assertEquals("", fAfter.print(Period.ZERO));
    }

    @Test
    public void testAppendLiteralsAndNullChecks() {
        PeriodFormatter f = builder
            .appendLiteral("START-")
            .appendYears()
            .appendLiteral("-END")
            .toFormatter();
        Assert.assertEquals("START-10-END", f.print(new Period().withYears(10)));

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "START-10-END", 0);
        Assert.assertEquals("START-10-END".length(), pos);
        Assert.assertEquals(10, mp.getYears());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullFormatter() {
        builder.append((PeriodFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrinterParser() {
        builder.append((PeriodPrinter) null, (PeriodParser) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNull() {
        builder.appendLiteral(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixNull() {
        builder.appendPrefix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixPluralNull() {
        builder.appendPrefix(null, "s");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixNull() {
        builder.appendSuffix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixPluralNull() {
        builder.appendSuffix("s", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixWithoutField() {
        builder.appendSuffix("s");
    }

    @Test(expected = IllegalStateException.class)
    public void testPrefixNotFollowedByField() {
        builder.appendPrefix("P").appendLiteral("L");
    }

    @Test(expected = IllegalStateException.class)
    public void testAdjacentSeparators() {
        builder.appendYears().appendSeparator(",").appendSeparator(";");
    }

    @Test(expected = IllegalStateException.class)
    public void testToFormatterEmpty() {
        new PeriodFormatterBuilder().toFormatter();
    }

    @Test
    public void testPrinterParserSubsets() {
        PeriodFormatter f = builder.appendYears().toFormatter();
        PeriodPrinter printer = f.getPrinter();
        PeriodParser parser = f.getParser();

        PeriodFormatter printerOnly = new PeriodFormatterBuilder().append(printer, null).toFormatter();
        Assert.assertTrue(printerOnly.isPrinter());
        Assert.assertFalse(printerOnly.isParser());
        Assert.assertNull(new PeriodFormatterBuilder().append(printer, null).toParser());
        Assert.assertNotNull(new PeriodFormatterBuilder().append(printer, null).toPrinter());

        PeriodFormatter parserOnly = new PeriodFormatterBuilder().append(null, parser).toFormatter();
        Assert.assertFalse(parserOnly.isPrinter());
        Assert.assertTrue(parserOnly.isParser());
        Assert.assertNull(new PeriodFormatterBuilder().append(null, parser).toPrinter());
        Assert.assertNotNull(new PeriodFormatterBuilder().append(null, parser).toParser());
    }

    @Test
    public void testWriterPrinting() throws IOException {
        PeriodFormatter f = builder
            .minimumPrintedDigits(2)
            .appendYears().appendSuffix("Y")
            .appendSecondsWithMillis().appendSuffix("S")
            .toFormatter();

        Period p = new Period(5, 0, 0, 0, 0, 0, 3, 40);
        CharArrayWriter writer = new CharArrayWriter();
        f.printTo(writer, p);
        Assert.assertEquals("05Y03.040S", writer.toString());
    }

    @Test
    public void testParseNegativeAndPlusSigns() {
        PeriodFormatter f = builder
            .appendYears()
            .appendSuffix("Y")
            .appendMonths()
            .appendSuffix("M")
            .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "+12Y-3M", 0);
        Assert.assertEquals(12, mp.getYears());
        Assert.assertEquals(-3, mp.getMonths());
    }

    @Test
    public void testParseFailures() {
        PeriodFormatter f = builder
            .appendYears().appendSuffix("Y")
            .appendMonths().appendSuffix("M")
            .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        Assert.assertTrue(f.parseInto(mp, "12X", 0) < 0);
        Assert.assertTrue(f.parseInto(mp, "ABC", 0) < 0);
    }
}
