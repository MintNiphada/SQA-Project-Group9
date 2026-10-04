package org.joda.time.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.joda.time.DateTimeConstants;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;
import org.junit.Test;

public class PeriodFormatterBuilderTest {

    @Test
    public void testToFormatterEmptyBuilder() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());
        assertEquals("", formatter.print(Period.ZERO));
        assertEquals(new Period(), formatter.parsePeriod(""));
    }

    @Test
    public void testToPrinterEmptyBuilder() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        assertNotNull(builder.toPrinter());
    }

    @Test
    public void testToParserEmptyBuilder() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        assertNotNull(builder.toParser());
    }

    @Test
    public void testClearReuseBuilder() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.clear();
        builder.appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("5", formatter.print(Period.months(5)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPeriodFormatterNull() {
        new PeriodFormatterBuilder().append((PeriodFormatter) null);
    }

    @Test
    public void testAppendPeriodFormatterValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter other = new PeriodFormatterBuilder().appendYears().toFormatter();
        builder.append(other);
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("3", formatter.print(Period.years(3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrinterParserBothNull() {
        new PeriodFormatterBuilder().append((PeriodPrinter) null, (PeriodParser) null);
    }

    @Test
    public void testAppendPrinterParserOneNull() {
        PeriodFormatterBuilder builder1 = new PeriodFormatterBuilder();
        builder1.append(new Literal("a"), null);
        PeriodFormatter f1 = builder1.toFormatter();
        assertNotNull(f1.getPrinter());
        assertNull(f1.getParser());
        assertEquals("a", f1.print(Period.ZERO));

        PeriodFormatterBuilder builder2 = new PeriodFormatterBuilder();
        builder2.append(null, new Literal("b"));
        PeriodFormatter f2 = builder2.toFormatter();
        assertNull(f2.getPrinter());
        assertNotNull(f2.getParser());
        MutablePeriod period = new MutablePeriod();
        f2.parseInto(period, "b", 0, Locale.ENGLISH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNull() {
        new PeriodFormatterBuilder().appendLiteral(null);
    }

    @Test
    public void testAppendLiteralValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("hello");
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("hello", formatter.print(Period.ZERO));
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "HeLLo", 0, Locale.ENGLISH);
        assertEquals(5, pos);
    }

    @Test
    public void testMinimumPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.minimumPrintedDigits(3).appendYears();
        assertEquals("005", builder.toFormatter().print(Period.years(5)));
    }

    @Test
    public void testMaximumParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.maximumParsedDigits(2).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "123", 0, Locale.ENGLISH);
        assertEquals(2, pos);
        assertEquals(12, period.getYears());
    }

    @Test
    public void testRejectSignedValuesTrue() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectSignedValues(true).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos1 = formatter.parseInto(period, "1", 0, Locale.ENGLISH);
        assertEquals(1, pos1);
        assertEquals(1, period.getYears());
        period.clear();
        int pos2 = formatter.parseInto(period, "+1", 0, Locale.ENGLISH);
        assertEquals(-1, pos2);
    }

    @Test
    public void testRejectSignedValuesFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectSignedValues(false).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "-12", 0, Locale.ENGLISH);
        assertEquals(3, pos);
        assertEquals(-12, period.getYears());
    }

    @Test
    public void testPrintZeroRarelyLastDefault() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        assertEquals("0", builder.toFormatter().print(Period.ZERO));
    }

    @Test
    public void testPrintZeroRarelyLastWithLaterField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        assertEquals("1", builder.toFormatter().print(Period.months(1)));
    }

    @Test
    public void testPrintZeroRarelyFirst() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendMonths();
        assertEquals("0", builder.toFormatter().print(Period.ZERO));
    }

    @Test
    public void testPrintZeroRarelyFirstWithEarlierField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendMonths();
        assertEquals("1", builder.toFormatter().print(Period.months(1)));
    }

    @Test
    public void testPrintZeroIfSupportedSupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("0", formatter.print(Period.ZERO));
    }

    @Test
    public void testPrintZeroIfSupportedUnsupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears();
        Period period = new Period(0, 0, 0, 0, 1, 0, 0, 0); // type is standard, supports years? Actually PeriodType.standard() supports years. Use PeriodType.time() with MutablePeriod.
        MutablePeriod mp = new MutablePeriod(PeriodType.time());
        mp.setHours(1);
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("", formatter.print(mp));
    }

    @Test
    public void testPrintZeroAlways() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears().appendSuffix(" years");
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("0 years", formatter.print(Period.ZERO));
    }

    @Test
    public void testPrintZeroAlwaysUnsupportedType() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears();
        MutablePeriod period = new MutablePeriod(PeriodType.time());
        period.setHours(1);
        assertEquals("0", builder.toFormatter().print(period));
    }

    @Test
    public void testPrintZeroNever() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroNever().appendYears().appendSuffix(" years");
        assertEquals("", builder.toFormatter().print(Period.ZERO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixSingleNull() {
        new PeriodFormatterBuilder().appendPrefix((String) null);
    }

    @Test
    public void testAppendPrefixSingleValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("The ").appendYears();
        assertEquals("The 1", builder.toFormatter().print(Period.years(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendPrefixPluralNull() {
        new PeriodFormatterBuilder().appendPrefix("a", null);
    }

    @Test
    public void testAppendPrefixPluralValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("year ", "years ").appendYears();
        assertEquals("year 1", builder.toFormatter().print(Period.years(1)));
        assertEquals("years 2", builder.toFormatter().print(Period.years(2)));
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendPrefixNoField() {
        new PeriodFormatterBuilder().appendPrefix("P").appendLiteral("x");
    }

    @Test
    public void testAppendPrefixComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("a").appendPrefix("b").appendYears();
        assertEquals("ab1", builder.toFormatter().print(Period.years(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixSingleNull() {
        new PeriodFormatterBuilder().appendSuffix((String) null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixNoField() {
        new PeriodFormatterBuilder().appendSuffix("suffix");
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSuffixAfterLiteral() {
        new PeriodFormatterBuilder().appendLiteral("L").appendSuffix("suffix");
    }

    @Test
    public void testAppendSuffixValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year");
        assertEquals("1 year", builder.toFormatter().print(Period.years(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSuffixPluralNull() {
        new PeriodFormatterBuilder().appendSuffix("a", null);
    }

    @Test
    public void testAppendSuffixPluralValid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year", " years");
        assertEquals("1 year", builder.toFormatter().print(Period.years(1)));
        assertEquals("2 years", builder.toFormatter().print(Period.years(2)));
    }

    @Test
    public void testAppendSuffixComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("a").appendSuffix("b");
        assertEquals("1ab", builder.toFormatter().print(Period.years(1)));
    }

    @Test
    public void testAppendYears() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendYears().toFormatter();
        assertEquals("5", formatter.print(Period.years(5)));
    }

    @Test
    public void testAppendMonths() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMonths().toFormatter();
        assertEquals("7", formatter.print(Period.months(7)));
    }

    @Test
    public void testAppendWeeks() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendWeeks().toFormatter();
        assertEquals("3", formatter.print(Period.weeks(3)));
    }

    @Test
    public void testAppendDays() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().toFormatter();
        assertEquals("2", formatter.print(Period.days(2)));
    }

    @Test
    public void testAppendHours() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendHours().toFormatter();
        assertEquals("8", formatter.print(Period.hours(8)));
    }

    @Test
    public void testAppendMinutes() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMinutes().toFormatter();
        assertEquals("15", formatter.print(Period.minutes(15)));
    }

    @Test
    public void testAppendSeconds() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendSeconds().toFormatter();
        assertEquals("30", formatter.print(Period.seconds(30)));
    }

    @Test
    public void testAppendMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMillis().toFormatter();
        assertEquals("123", formatter.print(Period.millis(123)));
    }

    @Test
    public void testAppendMillis3Digit() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMillis3Digit().toFormatter();
        assertEquals("005", formatter.print(Period.millis(5)));
    }

    @Test
    public void testAppendSecondsWithMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendSecondsWithMillis().toFormatter();
        Period period = Period.seconds(1).withMillis(234);
        assertEquals("1.234", formatter.print(period));
        period = Period.seconds(1);
        assertEquals("1.000", formatter.print(period));
    }

    @Test
    public void testAppendSecondsWithOptionalMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendSecondsWithOptionalMillis().toFormatter();
        Period period = Period.seconds(1).withMillis(234);
        assertEquals("1.234", formatter.print(period));
        period = Period.seconds(1);
        assertEquals("1", formatter.print(period));
    }

    @Test
    public void testAppendSeparatorBasic() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y").appendSeparator(" ").appendMonths().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("1y 2m", formatter.print(Period.years(1).withMonths(2)));
        assertEquals("1y", formatter.print(Period.years(1)));
        assertEquals("2m", formatter.print(Period.months(2)));
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendSeparatorAdjacent() {
        new PeriodFormatterBuilder().appendYears().appendSeparator(",").appendSeparator(",");
    }

    @Test
    public void testAppendSeparatorIfFieldsAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsAfter(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("1", formatter.print(Period.years(1)));
        assertEquals(",2", formatter.print(Period.months(2)));
    }

    @Test
    public void testAppendSeparatorIfFieldsBefore() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsBefore(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("1,", formatter.print(Period.years(1)));
        assertEquals("2", formatter.print(Period.months(2)));
    }

    @Test
    public void testAppendSeparatorTextFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y")
               .appendSeparator(",", " & ")
               .appendMonths().appendSuffix("m")
               .appendSeparator(",", " & ")
               .appendHours().appendSuffix("h");
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.years(1).withMonths(2).withHours(3);
        assertEquals("1y,2m & 3h", formatter.print(period));
        period = Period.years(1).withMonths(2);
        assertEquals("1y & 2m", formatter.print(period));
        period = Period.years(1);
        assertEquals("1y", formatter.print(period));
    }

    @Test
    public void testAppendSeparatorVariantsParsing() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ";", new String[]{"."}).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period1 = new MutablePeriod();
        int pos1 = formatter.parseInto(period1, "1;2", 0, Locale.ENGLISH);
        assertEquals(3, pos1);
        assertEquals(1, period1.getYears());
        assertEquals(2, period1.getMonths());
        MutablePeriod period2 = new MutablePeriod();
        int pos2 = formatter.parseInto(period2, "1.2", 0, Locale.ENGLISH);
        assertEquals(3, pos2);
        assertEquals(1, period2.getYears());
        assertEquals(2, period2.getMonths());
    }

    @Test
    public void testLeadingSeparatorIfFieldsAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSeparatorIfFieldsAfter("T").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("T5", formatter.print(Period.hours(5)));
        assertEquals("", formatter.print(Period.hours(0)));
    }

    @Test
    public void testSimpleAffixMethods() {
        PeriodFormatterBuilder.SimpleAffix affix = new PeriodFormatterBuilder.SimpleAffix("abc");
        assertEquals(3, affix.calculatePrintedLength(0));
        StringBuffer buf = new StringBuffer();
        affix.printTo(buf, 0);
        assertEquals("abc", buf.toString());
        Writer writer = new StringWriter();
        try {
            affix.printTo(writer, 0);
        } catch (IOException e) {
            fail();
        }
        assertEquals("abc", writer.toString());
        assertEquals(3, affix.parse("AbC", 0));
        assertEquals(~0, affix.parse("abd", 0));
        assertEquals(3, affix.scan("123abc", 0));
        assertEquals(~0, affix.scan("123xyz", 0));
    }

    @Test
    public void testPluralAffixMethods() {
        PeriodFormatterBuilder.PluralAffix affix = new PeriodFormatterBuilder.PluralAffix("y", "ies");
        assertEquals(1, affix.calculatePrintedLength(1));
        assertEquals(3, affix.calculatePrintedLength(2));
        StringBuffer buf = new StringBuffer();
        affix.printTo(buf, 1);
        assertEquals("y", buf.toString());
        buf.setLength(0);
        affix.printTo(buf, 2);
        assertEquals("ies", buf.toString());
        Writer writer = new StringWriter();
        try {
            affix.printTo(writer, 1);
        } catch (IOException e) {
            fail();
        }
        assertEquals("y", writer.toString());
        assertEquals(3, affix.parse("IES", 0));
        assertEquals(1, affix.parse("y", 0));
        assertEquals(~0, affix.parse("z", 0));
        assertEquals(2, affix.scan("abies", 1));
        assertEquals(1, affix.scan("ay", 0));
        assertEquals(~0, affix.scan("az", 0));
    }

    @Test
    public void testCompositeAffixMethods() {
        PeriodFormatterBuilder.SimpleAffix left = new PeriodFormatterBuilder.SimpleAffix("a");
        PeriodFormatterBuilder.SimpleAffix right = new PeriodFormatterBuilder.SimpleAffix("b");
        PeriodFormatterBuilder.CompositeAffix comp = new PeriodFormatterBuilder.CompositeAffix(left, right);
        assertEquals(2, comp.calculatePrintedLength(0));
        StringBuffer buf = new StringBuffer();
        comp.printTo(buf, 0);
        assertEquals("ab", buf.toString());
        assertEquals(2, comp.parse("ab", 0));
        assertEquals(~0, comp.parse("ac", 0));
        assertEquals(1, comp.scan("0ab", 0));
    }

    @Test
    public void testLiteralMethods() {
        PeriodFormatterBuilder.Literal literal = new PeriodFormatterBuilder.Literal("test");
        assertEquals(0, literal.countFieldsToPrint(Period.ZERO, 1, Locale.ENGLISH));
        assertEquals(4, literal.calculatePrintedLength(Period.ZERO, Locale.ENGLISH));
        StringBuffer buf = new StringBuffer();
        literal.printTo(buf, Period.ZERO, Locale.ENGLISH);
        assertEquals("test", buf.toString());
        Writer writer = new StringWriter();
        try {
            literal.printTo(writer, Period.ZERO, Locale.ENGLISH);
        } catch (IOException e) {
            fail();
        }
        assertEquals("test", writer.toString());
        MutablePeriod period = new MutablePeriod();
        int pos = literal.parseInto(period, "TeSt", 0, Locale.ENGLISH);
        assertEquals(4, pos);
        pos = literal.parseInto(period, "x", 0, Locale.ENGLISH);
        assertEquals(~0, pos);
    }

    @Test
    public void testSeparatorDirect() {
        PeriodFormatterBuilder.Literal before = new PeriodFormatterBuilder.Literal("B");
        PeriodFormatterBuilder.Separator sep = new PeriodFormatterBuilder.Separator(",", ";", new String[]{"."},
                before, before, true, true);
        sep.finish(new PeriodFormatterBuilder.Literal("A"), new PeriodFormatterBuilder.Literal("A"));
        MutablePeriod period = new MutablePeriod();
        int pos = sep.parseInto(period, "B,A", 0, Locale.ENGLISH);
        assertEquals(3, pos);
    }

    @Test
    public void testPrintToWriter() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendYears().appendSuffix(" years").toFormatter();
        StringWriter writer = new StringWriter();
        try {
            formatter.printTo(writer, Period.years(5));
        } catch (IOException e) {
            fail();
        }
        assertEquals("5 years", writer.toString());
    }

    @Test
    public void testParsePeriodWithComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y").appendSeparator(" ").appendMonths().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        Period parsed = formatter.parsePeriod("2y 3m");
        assertEquals(2, parsed.getYears());
        assertEquals(3, parsed.getMonths());
    }

    @Test
    public void testFieldValueForSecondsMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.seconds(2).withMillis(345);
        assertEquals("2.345", formatter.print(period));
        MutablePeriod mp = new MutablePeriod();
        int pos = formatter.parseInto(mp, "4.567", 0, Locale.ENGLISH);
        assertEquals(5, pos);
        assertEquals(4, mp.getSeconds());
        assertEquals(567, mp.getMillis());
    }

    @Test
    public void testFieldValueForSecondsOptionalMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.seconds(2).withMillis(345);
        assertEquals("2.345", formatter.print(period));
        period = Period.seconds(2);
        assertEquals("2", formatter.print(period));
        MutablePeriod mp = new MutablePeriod();
        int pos = formatter.parseInto(mp, "4.005", 0, Locale.ENGLISH);
        assertEquals(5, pos);
        assertEquals(4, mp.getSeconds());
        assertEquals(5, mp.getMillis());
    }

    @Test
    public void testParseSignedValue() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "-12", 0, Locale.ENGLISH);
        assertEquals(3, pos);
        assertEquals(-12, period.getYears());
    }

    @Test
    public void testParsePlusSignedValue() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "+12", 0, Locale.ENGLISH);
        assertEquals(3, pos);
        assertEquals(12, period.getYears());
    }

    @Test
    public void testParseNoDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "abc", 0, Locale.ENGLISH);
        assertEquals(~0, pos);
    }

    @Test
    public void testParseSuffixRequired() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears().appendSuffix(" years");
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "5 years", 0, Locale.ENGLISH);
        assertEquals(7, pos);
        assertEquals(5, period.getYears());
    }

    @Test
    public void testParsePrefixRequired() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendPrefix("age ").appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        MutablePeriod period = new MutablePeriod();
        int pos = formatter.parseInto(period, "age 5", 0, Locale.ENGLISH);
        assertEquals(5, pos);
        assertEquals(5, period.getYears());
    }

    @Test
    public void testFieldFormatterCountFieldsToPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.years(1);
        assertEquals(1, formatter.getPrinter().countFieldsToPrint(period, 1, Locale.ENGLISH));
    }

    @Test
    public void testCompositeCountFieldsToPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.years(1).withMonths(2);
        assertEquals(2, formatter.getPrinter().countFieldsToPrint(period, 5, Locale.ENGLISH));
    }

    @Test
    public void testSeparatorCountFieldsToPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = Period.years(1).withMonths(2);
        assertEquals(2, formatter.getPrinter().countFieldsToPrint(period, 5, Locale.ENGLISH));
    }

    @Test
    public void testAppendSuffixAfterSeparator() {
        try {
            new PeriodFormatterBuilder().appendYears().appendSeparator(",").appendSuffix("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }
}
