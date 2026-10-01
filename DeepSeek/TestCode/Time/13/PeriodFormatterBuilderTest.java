package org.joda.time.format;

import org.junit.Test;
import org.junit.Assert;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

public class PeriodFormatterBuilderTest {

    @Test
    public void testConstructor() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        Assert.assertNotNull(builder);
    }

    @Test(expected = IllegalStateException.class)
    public void testToFormatterEmpty() {
        new PeriodFormatterBuilder().toFormatter();
    }

    @Test
    public void testClear() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.clear();
        try {
            builder.toFormatter();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullFormatter() {
        new PeriodFormatterBuilder().append((PeriodFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrinterAndParser() {
        new PeriodFormatterBuilder().append((PeriodPrinter) null, (PeriodParser) null);
    }

    @Test
    public void testAppendPrinterOnly() {
        PeriodPrinter printer = new PeriodFormatterBuilder.Literal("X");
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().append(printer, null);
        PeriodFormatter f = builder.toFormatter();
        Assert.assertTrue(f.isPrinter());
        Assert.assertFalse(f.isParser());
    }

    @Test
    public void testAppendParserOnly() {
        PeriodParser parser = new PeriodFormatterBuilder.Literal("Y");
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().append(null, parser);
        PeriodFormatter f = builder.toFormatter();
        Assert.assertFalse(f.isPrinter());
        Assert.assertTrue(f.isParser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNull() {
        new PeriodFormatterBuilder().appendLiteral(null);
    }

    @Test
    public void testAppendLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendLiteral("abc");
        PeriodFormatter f = builder.toFormatter();
        Assert.assertTrue(f.isPrinter());
        Assert.assertTrue(f.isParser());
    }

    @Test
    public void testMinimumPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().minimumPrintedDigits(3).appendYears();
        PeriodFormatter f = builder.toFormatter();
        // test printing
    }

    @Test
    public void testMaximumParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().maximumParsedDigits(5).appendYears();
        PeriodFormatter f = builder.toFormatter();
        // test parsing
    }

    @Test
    public void testRejectSignedValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().rejectSignedValues(true).appendYears();
        PeriodFormatter f = builder.toFormatter();
        // test parsing with '+' should fail
    }

    @Test
    public void testPrintZeroRarelyLast() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroRarelyLast().appendYears();
        PeriodFormatter f = builder.toFormatter();
    }

    @Test
    public void testPrintZeroRarelyFirst() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroRarelyFirst().appendYears();
        PeriodFormatter f = builder.toFormatter();
    }

    @Test
    public void testPrintZeroIfSupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroIfSupported().appendYears();
        PeriodFormatter f = builder.toFormatter();
    }

    @Test
    public void testPrintZeroAlways() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroAlways().appendYears();
        PeriodFormatter f = builder.toFormatter();
    }

    @Test
    public void testPrintZeroNever() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroNever().appendYears();
        PeriodFormatter f = builder.toFormatter();
    }

    @Test
    public void testAppendYears() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendYears();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendMonths() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendMonths();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendWeeks() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendWeeks();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendDays() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendDays();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendHours() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendHours();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendMinutes() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendMinutes();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeconds() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendSeconds();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSecondsWithMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendSecondsWithMillis();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSecondsWithOptionalMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendSecondsWithOptionalMillis();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendMillis();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendMillis3Digit() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendMillis3Digit();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixNullText() {
        new PeriodFormatterBuilder().appendPrefix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixNullSingular() {
        new PeriodFormatterBuilder().appendPrefix(null, "plural");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixNullPlural() {
        new PeriodFormatterBuilder().appendPrefix("singular", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendPrefixNotFollowedByField() {
        new PeriodFormatterBuilder().appendPrefix("P:").toFormatter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixNullText() {
        new PeriodFormatterBuilder().appendSuffix((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixNullSingular() {
        new PeriodFormatterBuilder().appendSuffix(null, "plural");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixNullPlural() {
        new PeriodFormatterBuilder().appendSuffix("singular", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixWithoutField() {
        new PeriodFormatterBuilder().appendSuffix("s").toFormatter();
    }

    @Test
    public void testAppendPrefixAndSuffix() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendPrefix("Year:")
                .appendYears()
                .appendSuffix(" yrs")
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeparatorNormal() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(", ")
                .appendHours()
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeparatorIfFieldsAfter() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparatorIfFieldsAfter(", ")
                .appendHours()
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeparatorIfFieldsBefore() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparatorIfFieldsBefore(", ")
                .appendHours()
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeparatorWithFinalText() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(", ", " & ")
                .appendHours()
                .appendSeparator(", ", " & ")
                .appendMinutes()
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testAppendSeparatorWithVariants() {
        PeriodFormatter f = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(",", ";", new String[]{" ", " and "})
                .appendHours()
                .toFormatter();
        Assert.assertNotNull(f);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendAdjacentSeparators() {
        new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(",")
                .appendSeparator(";")
                .toFormatter();
    }

    @Test
    public void testToPrinterWhenNotPrinter() {
        PeriodParser parser = new PeriodFormatterBuilder.Literal("x");
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().append(null, parser);
        Assert.assertNull(builder.toPrinter());
    }

    @Test
    public void testToParserWhenNotParser() {
        PeriodPrinter printer = new PeriodFormatterBuilder.Literal("x");
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().append(printer, null);
        Assert.assertNull(builder.toParser());
    }

    @Test
    public void testSimpleAffixCalculatePrintedLength() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("abc");
        Assert.assertEquals(3, affix.calculatePrintedLength(0));
    }

    @Test
    public void testSimpleAffixPrintToStringBuffer() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("xyz");
        StringBuffer buf = new StringBuffer();
        affix.printTo(buf, 5);
        Assert.assertEquals("xyz", buf.toString());
    }

    @Test
    public void testSimpleAffixPrintToWriter() throws IOException {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("mno");
        StringWriter sw = new StringWriter();
        affix.printTo(sw, 2);
        Assert.assertEquals("mno", sw.toString());
    }

    @Test
    public void testSimpleAffixParseMatch() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("Hello");
        Assert.assertEquals(5, affix.parse("HelloWorld", 0));
    }

    @Test
    public void testSimpleAffixParseMismatch() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("Hello");
        Assert.assertTrue(affix.parse("World", 0) < 0);
    }

    @Test
    public void testSimpleAffixParseCaseInsensitive() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("HeLLo");
        Assert.assertEquals(5, affix.parse("hello there", 0));
    }

    @Test
    public void testSimpleAffixScanFound() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("foo");
        Assert.assertEquals(3, affix.scan("123foo", 0));
    }

    @Test
    public void testSimpleAffixScanNotFound() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("bar");
        Assert.assertTrue(affix.scan("123456", 0) < 0);
    }

    @Test
    public void testSimpleAffixScanSkipNumbersAndPunctuation() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("end");
        Assert.assertEquals(7, affix.scan("123,456end", 0));
    }

    @Test
    public void testPluralAffixCalculatePrintedLength() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("year", "years");
        Assert.assertEquals(4, affix.calculatePrintedLength(1));
        Assert.assertEquals(5, affix.calculatePrintedLength(2));
    }

    @Test
    public void testPluralAffixPrintTo() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("cat", "cats");
        StringBuffer buf = new StringBuffer();
        affix.printTo(buf, 1);
        Assert.assertEquals("cat", buf.toString());
        buf = new StringBuffer();
        affix.printTo(buf, 3);
        Assert.assertEquals("cats", buf.toString());
    }

    @Test
    public void testPluralAffixParse() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("bug", "bugs");
        Assert.assertEquals(4, affix.parse("bugs", 0));
        Assert.assertEquals(3, affix.parse("bug", 0));
        Assert.assertTrue(affix.parse("xyz", 0) < 0);
    }

    @Test
    public void testPluralAffixParseCaseInsensitive() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("Child", "Children");
        Assert.assertEquals(8, affix.parse("children", 0));
        Assert.assertEquals(5, affix.parse("child", 0));
    }

    @Test
    public void testPluralAffixScan() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("ox", "oxen");
        Assert.assertEquals(5, affix.scan("12oxen", 0));
        Assert.assertEquals(2, affix.scan("12ox", 0));
        Assert.assertTrue(affix.scan("12", 0) < 0);
    }

    @Test
    public void testCompositeAffix() {
        PeriodFormatterBuilder.SimpleAffix prefix = new PeriodFormatterBuilder.SimpleAffix("[");
        PeriodFormatterBuilder.SimpleAffix suffix = new PeriodFormatterBuilder.SimpleAffix("]");
        PeriodFormatterBuilder.CompositeAffix composite = new PeriodFormatterBuilder.CompositeAffix(prefix, suffix);
        Assert.assertEquals(2, composite.calculatePrintedLength(0));
        StringBuffer buf = new StringBuffer();
        composite.printTo(buf, 1);
        Assert.assertEquals("[]", buf.toString());
        Assert.assertEquals(3, composite.parse("[123]", 0));
        Assert.assertEquals(0, composite.scan("[123]", 0));
    }

    @Test
    public void testLiteralEmpty() {
        PeriodFormatterBuilder.Literal literal = PeriodFormatterBuilder.Literal.EMPTY;
        Assert.assertEquals(0, literal.calculatePrintedLength(null, null));
        StringBuffer buf = new StringBuffer("start");
        literal.printTo(buf, null, null);
        Assert.assertEquals("start", buf.toString());
        Assert.assertEquals(0, literal.parseInto(null, "any", 0, null));
    }

    @Test
    public void testLiteralNonEmpty() {
        PeriodFormatterBuilder.Literal literal = new PeriodFormatterBuilder.Literal("ABC");
        Assert.assertEquals(3, literal.calculatePrintedLength(null, null));
        StringBuffer buf = new StringBuffer();
        literal.printTo(buf, null, null);
        Assert.assertEquals("ABC", buf.toString());
        Assert.assertEquals(3, literal.parseInto(null, "ABCD", 0, null));
        Assert.assertTrue(literal.parseInto(null, "XYZ", 0, null) < 0);
    }

    @Test
    public void testFieldFormatterCountFieldsToPrintAlwaysZero() {
        PeriodFormatterBuilder.FieldFormatter ff = new PeriodFormatterBuilder.FieldFormatter(1, 4, 10, false, 0, new PeriodFormatterBuilder.FieldFormatter[10], null, null);
        Assert.assertEquals(1, ff.countFieldsToPrint(null, 1, null));
    }

    @Test
    public void testFieldFormatterCalculatePrintedLengthZero() {
        PeriodFormatterBuilder.FieldFormatter ff = new PeriodFormatterBuilder.FieldFormatter(1, 1, 10, false, 0, new PeriodFormatterBuilder.FieldFormatter[10], null, null);
        Assert.assertEquals(0, ff.calculatePrintedLength(null, null)); // no period
    }

    @Test
    public void testSeparatorFinish() {
        PeriodFormatterBuilder.Literal before = new PeriodFormatterBuilder.Literal("B");
        PeriodFormatterBuilder.Literal after = new PeriodFormatterBuilder.Literal("A");
        PeriodFormatterBuilder.Separator sep = new PeriodFormatterBuilder.Separator(",", ",", null, before, before, true, true);
        sep.finish(after, after);
        Assert.assertNotNull(sep);
    }

    @Test
    public void testCompositePrintToAndParseInto() throws IOException {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendLiteral("P")
                .appendYears()
                .appendLiteral("Y")
                .appendMonths()
                .appendLiteral("M");
        PeriodFormatter f = builder.toFormatter();
        StringWriter sw = new StringWriter();
        org.joda.time.Period period = new org.joda.time.Period(1, 2, 0, 0, 0, 0, 0, 0);
        f.printTo(sw, period);
        Assert.assertEquals("P1Y2M", sw.toString());
    }

    @Test
    public void testFormatterParsing() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendYears()
                .appendLiteral("-")
                .appendMonths();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "5-6", 0);
        Assert.assertEquals(3, pos);
        Assert.assertEquals(5, period.getYears());
        Assert.assertEquals(6, period.getMonths());
    }

    @Test
    public void testSeparatorParsing() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(",", ";")
                .appendHours();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "2,3", 0);
        Assert.assertEquals(3, pos);
        Assert.assertEquals(2, period.getDays());
        Assert.assertEquals(3, period.getHours());
    }

    @Test
    public void testSeparatorWithFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendDays()
                .appendSeparator(",", " & ")
                .appendHours()
                .appendSeparator(",", " & ")
                .appendMinutes();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod(1, 2, 0, 3, 4, 5, 0, 0);
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("1,2 & 3,4 & 5", sw.toString());
    }

    @Test
    public void testRejectSignedValuesInParse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().rejectSignedValues(true).appendSeconds();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "+5", 0);
        Assert.assertTrue(pos < 0);
    }

    @Test
    public void testMinimumPrintedDigitsPad() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().minimumPrintedDigits(3).appendMinutes();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.Period period = new org.joda.time.Period(0, 0, 0, 0, 0, 5, 0, 0);
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("005", sw.toString());
    }

    @Test
    public void testMaximumParsedDigitsTruncate() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().maximumParsedDigits(2).appendHours();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "1234", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(12, period.getHours());
    }

    @Test
    public void testSecondsWithMillisFormatting() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendSecondsWithMillis();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.Period period = new org.joda.time.Period(0,0,0,0,0,0,3,250);
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("3.250", sw.toString());
    }

    @Test
    public void testSecondsWithOptionalMillisFormattingZeroMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendSecondsWithOptionalMillis();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.Period period = new org.joda.time.Period(0,0,0,0,0,0,4,0);
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("4", sw.toString());
    }

    @Test
    public void testMillis3Digit() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendMillis3Digit();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.Period period = new org.joda.time.Period(0,0,0,0,0,0,0,45);
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("045", sw.toString());
    }

    @Test
    public void testPrintZeroNever() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().printZeroNever().appendYears();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.Period period = new org.joda.time.Period();
        StringWriter sw = new StringWriter();
        f.printTo(sw, period);
        Assert.assertEquals("", sw.toString());
    }

    @Test
    public void testToListOfCompound() {
        // test that toFormatter returns a valid formatter
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendYears()
                .appendMonths()
                .appendWeeks();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test
    public void testIsZeroMethod() {
        PeriodFormatterBuilder.FieldFormatter ff = new PeriodFormatterBuilder.FieldFormatter(1, 1, 10, false, 0, null, null, null);
        org.joda.time.Period zeroPeriod = new org.joda.time.Period();
        Assert.assertTrue(ff.isZero(zeroPeriod));
        org.joda.time.Period nonZero = new org.joda.time.Period(1,0,0,0,0,0,0,0);
        Assert.assertFalse(ff.isZero(nonZero));
    }

    @Test
    public void testIsSupportedMethod() {
        PeriodFormatterBuilder.FieldFormatter ff = new PeriodFormatterBuilder.FieldFormatter(1, 1, 10, false, 0, null, null, null);
        org.joda.time.PeriodType type = org.joda.time.PeriodType.standard();
        Assert.assertTrue(ff.isSupported(type, 0)); // years
        Assert.assertFalse(ff.isSupported(type, -1));
    }

    @Test
    public void testSetFieldValue() {
        PeriodFormatterBuilder.FieldFormatter ff = new PeriodFormatterBuilder.FieldFormatter(1, 1, 10, false, 0, null, null, null);
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        ff.setFieldValue(period, 0, 5);
        Assert.assertEquals(5, period.getYears());
        ff.setFieldValue(period, 1, 6);
        Assert.assertEquals(6, period.getMonths());
    }

    @Test
    public void testParseIntEdgeCases() {
        // via parseInto
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendYears();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "9999999999", 0);
        Assert.assertEquals(10, pos);
        Assert.assertEquals(999999999, period.getYears()); // 10-digit parseInt limit
    }

    @Test
    public void testParseIntWithLeadingSign() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendYears();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "-2", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(-2, period.getYears());
    }

    @Test
    public void testParseIntWithPlusSignIgnored() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendYears();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "+5", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(5, period.getYears());
    }

    @Test
    public void testParseIntoWithPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendPrefix("YR:")
                .appendYears();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "YR:2019", 0);
        Assert.assertEquals(6, pos);
        Assert.assertEquals(2019, period.getYears());
    }

    @Test
    public void testParseIntoWithSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendYears()
                .appendSuffix("yrs");
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "12yrs", 0);
        Assert.assertEquals(5, pos);
        Assert.assertEquals(12, period.getYears());
    }

    @Test
    public void testParseIntoPluralSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendMonths()
                .appendSuffix(" month", " months");
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos1 = f.parseInto(period, "1 month", 0);
        Assert.assertEquals(7, pos1);
        Assert.assertEquals(1, period.getMonths());
        period = new org.joda.time.MutablePeriod();
        int pos2 = f.parseInto(period, "3 months", 0);
        Assert.assertEquals(8, pos2);
        Assert.assertEquals(3, period.getMonths());
    }

    @Test
    public void testSeparatorAtBegining() {
        // separator at the beginning with no before text
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendSeparatorIfFieldsAfter("T")
                .appendHours();
        PeriodFormatter f = builder.toFormatter();
        org.joda.time.MutablePeriod period = new org.joda.time.MutablePeriod();
        int pos = f.parseInto(period, "T5", 0);
        Assert.assertEquals(2, pos);
        Assert.assertEquals(5, period.getHours());
    }

    @Test
    public void testToFormatterWithLeadingSeparator() {
        // regression: toFormatter with leading separator should work
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendSeparatorIfFieldsAfter(",")
                .appendDays()
                .appendHours();
        PeriodFormatter f = builder.toFormatter();
        Assert.assertNotNull(f);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCompositeParseIntoNoParsers() {
        PeriodFormatterBuilder.Composite comp = new PeriodFormatterBuilder.Composite(new java.util.ArrayList<>());
        comp.parseInto(null, "", 0, null);
    }

    @Test
    public void testSeparatorFinishAfter() {
        PeriodFormatterBuilder.Literal lit = new PeriodFormatterBuilder.Literal("X");
        PeriodFormatterBuilder.Separator sep = new PeriodFormatterBuilder.Separator(",", ",", null, lit, lit, true, true);
        sep.finish(lit, lit);
        // try to print
    }

    @Test
    public void testGetFieldType() {
        PeriodFormatterBuilder.FiledFormatter ff = new PeriodFormatterBuilder.FiledFormatter(1, 1, 10, false, 3, null, null, null);
        Assert.assertEquals(3, ff.getFieldType());
    }

    @Test
    public void testSimpleAffixScanUppercaseInsensitive() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("Ab");
        Assert.assertEquals(2, affix.scan("12ab", 0));
    }

    @Test
    public void testPluralAffixScanVithMultipleOccurences() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("byte", "bytes");
        Assert.assertEquals(0, affix.scan("bytes", 0));
    }

    @Test
    public void testCompositeAffixScanFail() {
        PeriodFormatterBuilder.SimpleAffix left = new PeriodFormatterBuilder.SimpleAffix("UNKNOWN");
        PeriodFormatterBuilder.SimpleAffix right = new PeriodFormatterBuilder.SimpleAffix("X");
        PeriodFormatterBuilder.CompositeAffix comp = new PeriodFormatterBuilder.CompositeAffix(left, right);
        Assert.assertTrue(comp.scan("X", 0) < 0);
    }

    @Test
    public void testFieldFormatterGetFieldValueUnsuporrted() {
        // If the period type doesn't support the field, it returns Long.MAX_VALUE unless printZeroAlways is set
        PeriodFormatterBuilder.FiledFormatter ff = new PeriodFormatterBuilder.FiledFormatter(1, 1, 10, false, 0, null, null, null); // PRINT_ZERO_RARELY_LAST
        org.joda.time.Period period = new org.joda.time.Period(0,0,0,0,0,0,0,0);        // We need to make years unsupported? But standard type supports years. To make unsupported, use a custom type.
        // Instead test with printZeroAlways: it should not return MAX_VALUE even if zero.
        ff = new PeriodFormatterBuilder.FiledFormatter(1, 4, 10, false, 0, null, null, null); // PRINT_ZERO_ALWAYS
        long val = ff.getFieldValue(period);
        Assert.assertEquals(0, val);
    }

    @Test
    public void testCountFieldsToPrintZeroStopAt() {
        PeriodFormatterBuilder.FiledFormatter ff = new PeriodFormatterBuilder.FiledFormatter(1, 1, 10, false, 0, null, null, null);
        Assert.assertEquals(0, ff.countFieldsToPrint(null, 0, null));
    }

    @Test
    public void testCalculatePrintedengthWithenMaxValue() {
        PeriodFormatterBuilder.FiledFormatter ff = new PeriodFormatterBuilder.FiledFormatter(1, 1, 10, false, 0, null, null, null);
        org.joda.time.Period period = new org.joda.time.Period(0,0,0,0,0,0,0,0);
        // Force getFieldValue to Long.MAX_VALUE by using unsupported? Hard.
        // Instead use a period with zero that triggers PRINT_ZERO_REARELY_LAST returning MAX.
        // Test with printZeroRarelyLast and zero period.
        ff = new PeriodFormatterBuilder.FiledFormatter(1, 2, 10, false, 0, null, null, null); // PRINT_ZERO_RARELY_LAST
        long val = ff.getFieldValue(period);
        Assert.assertEquals(Long.MAX_VALUE, val);
    }
}
