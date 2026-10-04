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
    public void testClearAndReusability() {
        builder.appendYears().appendSuffix("Y");
        PeriodFormatter f1 = builder.toFormatter();
        Assert.assertNotNull(f1);

        builder.clear();
        builder.appendDays().appendSuffix("D");
        PeriodFormatter f2 = builder.toFormatter();
        Assert.assertNotNull(f2);

        Period p = new Period().withYears(2).withDays(5);
        Assert.assertEquals("2Y", f1.print(p));
        Assert.assertEquals("5D", f2.print(p));
    }

    @Test(expected = IllegalStateException.class)
    public void testToFormatterEmptyThrows() {
        builder.toFormatter();
    }

    @Test
    public void testToPrinterAndToParser() {
        builder.appendYears();
        Assert.assertNotNull(builder.toPrinter());
        Assert.assertNotNull(builder.toParser());

        // Printer-only setup
        PeriodFormatterBuilder pOnlyBuilder = new PeriodFormatterBuilder();
        PeriodPrinter mockPrinter = new PeriodFormatterBuilder.Literal("P");
        pOnlyBuilder.append(mockPrinter, null);
        Assert.assertNotNull(pOnlyBuilder.toPrinter());
        Assert.assertNull(pOnlyBuilder.toParser());

        // Parser-only setup
        PeriodFormatterBuilder parseOnlyBuilder = new PeriodFormatterBuilder();
        PeriodParser mockParser = new PeriodFormatterBuilder.Literal("P");
        parseOnlyBuilder.append(null, mockParser);
        Assert.assertNull(parseOnlyBuilder.toPrinter());
        Assert.assertNotNull(parseOnlyBuilder.toParser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullFormatter() {
        builder.append((PeriodFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrinterAndParser() {
        builder.append(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullLiteral() {
        builder.appendLiteral(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrefixSingle() {
        builder.appendPrefix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrefixPlural1() {
        builder.appendPrefix(null, "s");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrefixPlural2() {
        builder.appendPrefix("s", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullSuffixSingle() {
        builder.appendSuffix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullSuffixPlural1() {
        builder.appendSuffix(null, "s");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullSuffixPlural2() {
        builder.appendSuffix("s", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixWithoutFieldThrows() {
        builder.appendSuffix("suffix");
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixOnLiteralThrows() {
        builder.appendLiteral("lit").appendSuffix("suffix");
    }

    @Test(expected = IllegalStateException.class)
    public void testPrefixNotFollowedByFieldOnAppendLiteral() {
        builder.appendPrefix("pre").appendLiteral("lit");
    }

    @Test(expected = IllegalStateException.class)
    public void testPrefixNotFollowedByFieldOnAppendSeparator() {
        builder.appendPrefix("pre").appendSeparator(",");
    }

    @Test(expected = IllegalStateException.class)
    public void testPrefixNotFollowedByFieldOnAppendFormatter() {
        PeriodFormatter f = new PeriodFormatterBuilder().appendYears().toFormatter();
        builder.appendPrefix("pre").append(f);
    }

    @Test
    public void testAllFieldsPrintingAndParsing() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
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
        String printed = formatter.print(p);
        Assert.assertEquals("1y2m3w4d5h6min7s8ms", printed);

        MutablePeriod parsed = new MutablePeriod();
        int end = formatter.parseInto(parsed, "1y2m3w4d5h6min7s8ms", 0);
        Assert.assertEquals(19, end);
        Assert.assertEquals(p, parsed.toPeriod());
    }

    @Test
    public void testMillis3Digit() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendMillis3Digit()
                .appendSuffix("ms")
                .toFormatter();

        Period p1 = new Period().withMillis(5);
        Assert.assertEquals("005ms", f.print(p1));

        Period p2 = new Period().withMillis(50);
        Assert.assertEquals("050ms", f.print(p2));

        Period p3 = new Period().withMillis(500);
        Assert.assertEquals("500ms", f.print(p3));
    }

    @Test
    public void testAppendSecondsWithMillis() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendSecondsWithMillis()
                .appendSuffix("s")
                .toFormatter();

        Period p1 = new Period().withSeconds(12).withMillis(345);
        Assert.assertEquals("12.345s", f.print(p1));
        Assert.assertEquals(7, f.getPrinter().calculatePrintedLength(p1, null));

        StringWriter sw = new StringWriter();
        f.getPrinter().printTo(sw, p1, null);
        Assert.assertEquals("12.345s", sw.toString());

        Period p2 = new Period().withSeconds(12).withMillis(0);
        Assert.assertEquals("12.000s", f.print(p2));

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "12.345s", 0);
        Assert.assertEquals(7, pos);
        Assert.assertEquals(12, mp.getSeconds());
        Assert.assertEquals(345, mp.getMillis());

        // Parse with 1 fraction digit
        mp = new MutablePeriod();
        f.parseInto(mp, "12.3s", 0);
        Assert.assertEquals(12, mp.getSeconds());
        Assert.assertEquals(300, mp.getMillis());

        // Parse with 2 fraction digits
        mp = new MutablePeriod();
        f.parseInto(mp, "12.34s", 0);
        Assert.assertEquals(12, mp.getSeconds());
        Assert.assertEquals(340, mp.getMillis());

        // Parse without fraction
        mp = new MutablePeriod();
        f.parseInto(mp, "12s", 0);
        Assert.assertEquals(12, mp.getSeconds());
        Assert.assertEquals(0, mp.getMillis());
    }

    @Test
    public void testAppendSecondsWithOptionalMillis() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendSecondsWithOptionalMillis()
                .appendSuffix("s")
                .toFormatter();

        Period p1 = new Period().withSeconds(12).withMillis(345);
        Assert.assertEquals("12.345s", f.print(p1));
        Assert.assertEquals(7, f.getPrinter().calculatePrintedLength(p1, null));

        Period p2 = new Period().withSeconds(12).withMillis(0);
        Assert.assertEquals("12s", f.print(p2));
        Assert.assertEquals(3, f.getPrinter().calculatePrintedLength(p2, null));

        // Negative values
        Period pNeg = new Period().withSeconds(-5).withMillis(-250);
        Assert.assertEquals("-5.250s", f.print(pNeg));

        MutablePeriod mp = new MutablePeriod();
        f.parseInto(mp, "-5.250s", 0);
        Assert.assertEquals(-5, mp.getSeconds());
        Assert.assertEquals(-250, mp.getMillis());
    }

    @Test
    public void testPrintZeroRarelyLastDefault() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .printZeroRarelyLast()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .appendDays().appendSuffix("d")
                .toFormatter();

        Period allZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        Assert.assertEquals("0d", f.print(allZero));

        Period monthsOnly = new Period().withMonths(2);
        Assert.assertEquals("2m", f.print(monthsOnly));
    }

    @Test
    public void testPrintZeroRarelyFirst() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .appendDays().appendSuffix("d")
                .toFormatter();

        Period allZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        Assert.assertEquals("0y", f.print(allZero));

        Period monthsOnly = new Period().withMonths(2);
        Assert.assertEquals("2m", f.print(monthsOnly));
    }

    @Test
    public void testPrintZeroAlways() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .printZeroAlways()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .toFormatter();

        Period allZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        Assert.assertEquals("0y0m", f.print(allZero));
        Assert.assertEquals(2, f.getPrinter().countFieldsToPrint(allZero, Integer.MAX_VALUE, null));
    }

    @Test
    public void testPrintZeroNever() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .toFormatter();

        Period allZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        Assert.assertEquals("", f.print(allZero));
        Assert.assertEquals(0, f.getPrinter().countFieldsToPrint(allZero, Integer.MAX_VALUE, null));
    }

    @Test
    public void testPrintZeroIfSupported() {
        PeriodType typeNoMonths = PeriodType.yearDayTime();
        PeriodFormatter f = new PeriodFormatterBuilder()
                .printZeroIfSupported()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .appendDays().appendSuffix("d")
                .toFormatter();

        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 0, typeNoMonths);
        Assert.assertEquals("0y0d", f.print(p));
    }

    @Test
    public void testPluralAffix() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays(new PeriodFormatterBuilder().minimumPrintedDigits(1))
                .appendSuffix(" day", " days")
                .toFormatter();

        Period p1 = new Period().withDays(1);
        Assert.assertEquals("1 day", f.print(p1));
        Assert.assertEquals(5, f.getPrinter().calculatePrintedLength(p1, null));

        StringWriter sw = new StringWriter();
        f.getPrinter().printTo(sw, p1, null);
        Assert.assertEquals("1 day", sw.toString());

        Period p2 = new Period().withDays(2);
        Assert.assertEquals("2 days", f.print(p2));

        Period p0 = new Period().withDays(0);
        Assert.assertEquals("0 days", f.print(p0));

        MutablePeriod mp1 = new MutablePeriod();
        Assert.assertTrue(f.parseInto(mp1, "1 day", 0) > 0);
        Assert.assertEquals(1, mp1.getDays());

        MutablePeriod mp2 = new MutablePeriod();
        Assert.assertTrue(f.parseInto(mp2, "2 days", 0) > 0);
        Assert.assertEquals(2, mp2.getDays());

        // Plural affix with singular longer than plural
        PeriodFormatter fSwapped = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" hours long", " hr")
                .toFormatter();

        MutablePeriod mp3 = new MutablePeriod();
        Assert.assertTrue(fSwapped.parseInto(mp3, "1 hours long", 0) > 0);
        Assert.assertEquals(1, mp3.getHours());

        MutablePeriod mp4 = new MutablePeriod();
        Assert.assertTrue(fSwapped.parseInto(mp4, "2 hr", 0) > 0);
        Assert.assertEquals(2, mp4.getHours());
    }

    @Test
    public void testCompositeAffixAndPrefix() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendPrefix("Pre1:")
                .appendPrefix("Pre2:")
                .appendYears()
                .appendSuffix("Suf1")
                .appendSuffix("Suf2")
                .toFormatter();

        Period p = new Period().withYears(5);
        Assert.assertEquals("Pre1:Pre2:5Suf1Suf2", f.print(p));
        Assert.assertEquals(15, f.getPrinter().calculatePrintedLength(p, null));

        StringWriter sw = new StringWriter();
        f.getPrinter().printTo(sw, p, null);
        Assert.assertEquals("Pre1:Pre2:5Suf1Suf2", sw.toString());

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "Pre1:Pre2:5Suf1Suf2", 0);
        Assert.assertEquals(15, pos);
        Assert.assertEquals(5, mp.getYears());
    }

    @Test
    public void testPrefixPlural() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendPrefix("One year: ", "Years: ")
                .appendYears()
                .toFormatter();

        Period p1 = new Period().withYears(1);
        Assert.assertEquals("One year: 1", f.print(p1));

        Period p2 = new Period().withYears(3);
        Assert.assertEquals("Years: 3", f.print(p2));

        MutablePeriod mp1 = new MutablePeriod();
        Assert.assertTrue(f.parseInto(mp1, "One year: 1", 0) > 0);
        Assert.assertEquals(1, mp1.getYears());

        MutablePeriod mp2 = new MutablePeriod();
        Assert.assertTrue(f.parseInto(mp2, "Years: 3", 0) > 0);
        Assert.assertEquals(3, mp2.getYears());
    }

    @Test
    public void testMinimumPrintedDigitsAndFormatUtilsPadded() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .minimumPrintedDigits(3)
                .appendHours()
                .toFormatter();

        Period p = new Period().withHours(7);
        Assert.assertEquals("007", f.print(p));

        StringWriter sw = new StringWriter();
        f.getPrinter().printTo(sw, p, null);
        Assert.assertEquals("007", sw.toString());
    }

    @Test
    public void testMaximumParsedDigitsAndRejectSignedValues() {
        PeriodFormatter fSigned = new PeriodFormatterBuilder()
                .maximumParsedDigits(2)
                .rejectSignedValues(true)
                .appendHours()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        // Parsing negative value when signed values rejected fails or stops before '-'
        int pos = fSigned.parseInto(mp, "-5", 0);
        Assert.assertTrue(pos < 0);

        mp = new MutablePeriod();
        // Parsing more than 2 digits stops at 2 digits
        pos = fSigned.parseInto(mp, "1234", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(12, mp.getHours());
    }

    @Test
    public void testSignedValuesAcceptedWithPlusAndMinus() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .rejectSignedValues(false)
                .appendHours()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "+15", 0);
        Assert.assertEquals(3, pos);
        Assert.assertEquals(15, mp.getHours());

        mp = new MutablePeriod();
        pos = f.parseInto(mp, "-15", 0);
        Assert.assertEquals(3, pos);
        Assert.assertEquals(-15, mp.getHours());
    }

    @Test
    public void testSeparatorsBasic() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(", ")
                .appendHours()
                .appendSeparator(", ")
                .appendMinutes()
                .toFormatter();

        Period p3 = new Period(0, 0, 0, 1, 2, 3, 0, 0);
        Assert.assertEquals("1, 2, 3", f.print(p3));
        Assert.assertEquals(7, f.getPrinter().calculatePrintedLength(p3, null));

        StringWriter sw = new StringWriter();
        f.getPrinter().printTo(sw, p3, null);
        Assert.assertEquals("1, 2, 3", sw.toString());

        Period p1 = new Period(0, 0, 0, 1, 0, 0, 0, 0);
        Assert.assertEquals("1", f.print(p1));

        Period pNone = new Period();
        Assert.assertEquals("0", f.print(pNone)); // printZeroRarelyLast default on minutes

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "1, 2, 3", 0);
        Assert.assertEquals(7, pos);
        Assert.assertEquals(1, mp.getDays());
        Assert.assertEquals(2, mp.getHours());
        Assert.assertEquals(3, mp.getMinutes());
    }

    @Test
    public void testSeparatorWithFinalTextAndVariants() {
        String[] variants = new String[] { " & ", " and " };
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(", ", " and ", variants)
                .appendHours()
                .appendSeparator(", ", " and ", variants)
                .appendMinutes()
                .toFormatter();

        Period p3 = new Period(0, 0, 0, 1, 2, 3, 0, 0);
        Assert.assertEquals("1, 2 and 3", f.print(p3));

        Period p2 = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        Assert.assertEquals("1 and 2", f.print(p2));

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "1, 2 & 3", 0);
        Assert.assertEquals(9, pos);
        Assert.assertEquals(1, mp.getDays());
        Assert.assertEquals(2, mp.getHours());
        Assert.assertEquals(3, mp.getMinutes());
    }

    @Test
    public void testSeparatorIfFieldsBeforeAndAfter() {
        PeriodFormatter fBefore = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparatorIfFieldsBefore(":")
                .appendHours()
                .toFormatter();

        Assert.assertEquals("1:2", fBefore.print(new Period(0, 0, 0, 1, 2, 0, 0, 0)));
        Assert.assertEquals("1:", fBefore.print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));
        Assert.assertEquals("2", fBefore.print(new Period(0, 0, 0, 0, 2, 0, 0, 0)));

        PeriodFormatter fAfter = new PeriodFormatterBuilder()
                .appendSeparatorIfFieldsAfter("T")
                .appendHours()
                .toFormatter();

        Assert.assertEquals("T2", fAfter.print(new Period(0, 0, 0, 0, 2, 0, 0, 0)));

        MutablePeriod mp = new MutablePeriod();
        int pos = fAfter.parseInto(mp, "T2", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(2, mp.getHours());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSeparatorNullText() {
        builder.appendSeparator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSeparatorNullFinalText() {
        builder.appendSeparator("a", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAdjacentSeparatorsThrow() {
        builder.appendDays().appendSeparator(",").appendSeparator(";").appendHours();
    }

    @Test
    public void testAppendFormatterComposite() {
        PeriodFormatter f1 = new PeriodFormatterBuilder().appendYears().appendSuffix("y").toFormatter();
        PeriodFormatter f2 = new PeriodFormatterBuilder().appendMonths().appendSuffix("m").toFormatter();

        PeriodFormatter composite = new PeriodFormatterBuilder()
                .append(f1)
                .appendLiteral(" ")
                .append(f2)
                .toFormatter();

        Period p = new Period().withYears(2).withMonths(4);
        Assert.assertEquals("2y 4m", composite.print(p));
        Assert.assertEquals(6, composite.getPrinter().calculatePrintedLength(p, null));

        MutablePeriod mp = new MutablePeriod();
        int pos = composite.parseInto(mp, "2y 4m", 0);
        Assert.assertEquals(6, pos);
        Assert.assertEquals(2, mp.getYears());
        Assert.assertEquals(4, mp.getMonths());
    }

    @Test
    public void testCompositeCountFieldsToPrint() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendYears()
                .appendMonths()
                .appendDays()
                .toFormatter();

        Period p = new Period().withYears(1).withDays(2);
        Assert.assertEquals(2, f.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, null));
        Assert.assertEquals(1, f.getPrinter().countFieldsToPrint(p, 1, null));
    }

    @Test
    public void testFieldFormatterCountFieldsToPrintStopAtZero() {
        PeriodFormatter f = new PeriodFormatterBuilder().appendYears().toFormatter();
        Period p = new Period().withYears(1);
        Assert.assertEquals(0, f.getPrinter().countFieldsToPrint(p, 0, null));
    }

    @Test
    public void testParseErrorsAndBoundaries() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendPrefix("Y:")
                .appendYears()
                .appendSuffix("years")
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        // Empty text
        Assert.assertEquals(~0, f.parseInto(mp, "", 0));
        // Prefix mismatch
        Assert.assertEquals(~0, f.parseInto(mp, "X:5years", 0));
        // Missing digits after prefix
        Assert.assertEquals(~2, f.parseInto(mp, "Y:abc", 0));
        // Suffix mismatch
        Assert.assertEquals(~3, f.parseInto(mp, "Y:5days", 0));
        // Out of bounds position
        Assert.assertEquals(10, f.parseInto(mp, "Y:5years", 10));
    }

    @Test
    public void testLargeDigitsParsing() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .maximumParsedDigits(12)
                .appendYears()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        String large = "1234567890";
        int pos = f.parseInto(mp, large, 0);
        Assert.assertEquals(10, pos);
        Assert.assertEquals(1234567890, mp.getYears());
    }

    @Test
    public void testLiteralPrintAndParse() throws IOException {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendLiteral("START-")
                .appendDays()
                .appendLiteral("-END")
                .toFormatter();

        Period p = new Period().withDays(5);
        Assert.assertEquals("START-5-END", f.print(p));
        Assert.assertEquals(11, f.getPrinter().calculatePrintedLength(p, null));
        Assert.assertEquals(0, f.getPrinter().countFieldsToPrint(p, 5, null));

        CharArrayWriter caw = new CharArrayWriter();
        f.getPrinter().printTo(caw, p, null);
        Assert.assertEquals("START-5-END", caw.toString());

        MutablePeriod mp = new MutablePeriod();
        int pos = f.parseInto(mp, "START-5-END", 0);
        Assert.assertEquals(11, pos);
        Assert.assertEquals(5, mp.getDays());

        // Parse mismatch on literal
        Assert.assertTrue(f.parseInto(mp, "FAIL-5-END", 0) < 0);
    }

    @Test
    public void testSeparatorParseErrors() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(", ")
                .appendHours()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        // Separator present but no trailing field
        int pos = f.parseInto(mp, "5, abc", 0);
        Assert.assertTrue(pos < 0);
    }

    @Test
    public void testUnsupportedFieldHandling() {
        PeriodType typeTimeOnly = PeriodType.time();
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendYears()
                .appendHours()
                .toFormatter();

        Period p = new Period(0, 0, 0, 0, 5, 0, 0, 0, typeTimeOnly);
        Assert.assertEquals("5", f.print(p));

        MutablePeriod mp = new MutablePeriod(typeTimeOnly);
        int pos = f.parseInto(mp, "5", 0);
        Assert.assertEquals(1, pos);
        Assert.assertEquals(5, mp.getHours());
        Assert.assertEquals(0, mp.getYears());
    }

    @Test
    public void testFieldFormatterHelperMethodsCoverage() {
        PeriodFormatterBuilder b = new PeriodFormatterBuilder();
        b.appendYears();
        b.appendMonths();
        b.appendWeeks();
        b.appendDays();
        b.appendHours();
        b.appendMinutes();
        b.appendSeconds();
        b.appendMillis();
        PeriodFormatter f = b.toFormatter();

        MutablePeriod mp = new MutablePeriod();
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        f.parseInto(mp, "12345678", 0);
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
    public void testPrefixMethodOverloadSingularPlural() {
        builder.appendPrefix("single", "plural");
        builder.appendDays();
        PeriodFormatter f = builder.toFormatter();

        Assert.assertEquals("single1", f.print(new Period().withDays(1)));
        Assert.assertEquals("plural2", f.print(new Period().withDays(2)));
    }

    @Test
    public void testAppendSeparatorTwoArgs() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(",", " and ")
                .appendHours()
                .toFormatter();

        Assert.assertEquals("1 and 2", f.print(new Period().withDays(1).withHours(2)));
    }
}
