package org.joda.time.format;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;
import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DateTimeFormatterBuilderTest {

    @Test
    public void testBasicBuilderCreationAndClear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        Assert.assertFalse(builder.canBuildFormatter());
        Assert.assertFalse(builder.canBuildPrinter());
        Assert.assertFalse(builder.canBuildParser());

        builder.appendLiteral("2020");
        Assert.assertTrue(builder.canBuildFormatter());
        Assert.assertTrue(builder.canBuildPrinter());
        Assert.assertTrue(builder.canBuildParser());

        builder.clear();
        Assert.assertFalse(builder.canBuildFormatter());
        Assert.assertFalse(builder.canBuildPrinter());
        Assert.assertFalse(builder.canBuildParser());
    }

    @Test
    public void testToFormatterExceptions() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.toFormatter();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            builder.toPrinter();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            builder.toParser();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAppendNullsAndInvalidArguments() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();

        try {
            builder.append((DateTimeFormatter) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append((DateTimePrinter) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append((DateTimeParser) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append((DateTimePrinter) null, (DateTimeParser) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append(null, (DateTimeParser[]) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append(null, new DateTimeParser[]{null});
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.append(null, new DateTimeParser[]{null, null});
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendOptional(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendLiteral((String) null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendDecimal(null, 1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendDecimal(DateTimeFieldType.dayOfMonth(), -1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendDecimal(DateTimeFieldType.dayOfMonth(), 2, 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFixedDecimal(null, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFixedDecimal(DateTimeFieldType.year(), 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendSignedDecimal(null, 1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendSignedDecimal(DateTimeFieldType.year(), -1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendSignedDecimal(DateTimeFieldType.year(), 2, 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFixedSignedDecimal(null, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFixedSignedDecimal(DateTimeFieldType.year(), 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendText(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendShortText(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFraction(null, 1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFraction(DateTimeFieldType.secondOfDay(), -1, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendFraction(DateTimeFieldType.secondOfDay(), 2, 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendTimeZoneOffset("Z", true, 0, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            builder.appendTimeZoneOffset("Z", true, 3, 2);
            Assert.fail();
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testLiterals() throws IOException {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendLiteral("")
                .appendLiteral('X')
                .appendLiteral("Hello")
                .toFormatter();

        Assert.assertEquals("XHello", f.print(0L));

        DateTime dt = f.parseDateTime("xhello");
        Assert.assertNotNull(dt);

        StringWriter sw = new StringWriter();
        f.printTo(sw, 0L);
        Assert.assertEquals("XHello", sw.toString());

        sw = new StringWriter();
        f.printTo(sw, new LocalDate(2020, 1, 1));
        Assert.assertEquals("XHello", sw.toString());

        Assert.assertEquals(6, f.getPrinter().estimatePrintedLength());
        Assert.assertEquals(6, f.getParser().estimateParsedLength());
    }

    @Test
    public void testDecimalsAndNumbers() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendDecimal(DateTimeFieldType.year(), 4, 4)
                .appendLiteral('-')
                .appendPaddedInteger(DateTimeFieldType.monthOfYear(), 2)
                .appendLiteral('-')
                .appendUnpaddedInteger(DateTimeFieldType.dayOfMonth(), 2)
                .toFormatter();

        DateTime dt = new DateTime(2021, 5, 9, 0, 0, 0, DateTimeZone.UTC);
        Assert.assertEquals("2021-05-9", f.withZoneUTC().print(dt));

        DateTime parsed = f.withZoneUTC().parseDateTime("2021-05-09");
        Assert.assertEquals(2021, parsed.getYear());
        Assert.assertEquals(5, parsed.getMonthOfYear());
        Assert.assertEquals(9, parsed.getDayOfMonth());

        DateTimeFormatter signedF = new DateTimeFormatterBuilder()
                .appendSignedDecimal(DateTimeFieldType.year(), 2, 4)
                .appendLiteral(' ')
                .appendFixedSignedDecimal(DateTimeFieldType.yearOfCentury(), 2)
                .toFormatter();

        Assert.assertEquals("+2021 21", signedF.withZoneUTC().print(dt));
        DateTime parsedSigned = signedF.withZoneUTC().parseDateTime("-0050 50");
        Assert.assertEquals(-50, parsedSigned.getYear());
    }

    private DateTimeFormatterBuilder appendPaddedInteger(DateTimeFieldType type, int size) {
        return new DateTimeFormatterBuilder().appendDecimal(type, size, size);
    }

    private DateTimeFormatterBuilder appendUnpaddedInteger(DateTimeFieldType type, int maxDigits) {
        return new DateTimeFormatterBuilder().appendDecimal(type, 1, maxDigits);
    }

    @Test
    public void testTwoDigitYear() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(2000, false)
                .toFormatter();

        Assert.assertEquals("21", f.print(new DateTime(2021, 1, 1, 0, 0, 0, DateTimeZone.UTC)));
        Assert.assertEquals(2021, f.parseDateTime("21").getYear());
        Assert.assertEquals(1999, f.parseDateTime("99").getYear());

        DateTimeFormatter lenientF = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(2000, true)
                .toFormatter();

        Assert.assertEquals(2021, lenientF.parseDateTime("21").getYear());
        Assert.assertEquals(1985, lenientF.parseDateTime("1985").getYear());
        Assert.assertEquals(-50, lenientF.parseDateTime("-50").getYear());
        Assert.assertEquals(2050, lenientF.parseDateTime("+2050").getYear());

        DateTimeFormatter fWeekyear = new DateTimeFormatterBuilder()
                .appendTwoDigitWeekyear(2000)
                .toFormatter();
        Assert.assertEquals("21", fWeekyear.print(new DateTime(2021, 1, 1, 0, 0, 0, DateTimeZone.UTC)));
    }

    @Test
    public void testTwoDigitNegativeYear() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(1900, false)
                .toFormatter();
        Assert.assertEquals("50", f.print(new DateTime(1950, 1, 1, 0, 0, 0, DateTimeZone.UTC)));
        Assert.assertEquals(1920, f.parseDateTime("20").getYear());
        Assert.assertEquals(1890, f.parseDateTime("90").getYear());

        DateTimeFormatter fPivotNegative = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(-50, false)
                .toFormatter();
        Assert.assertEquals(-10, fPivotNegative.parseDateTime("90").getYear());
    }

    @Test
    public void testAllFieldHelpers() {
        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
        b.appendMillisOfSecond(3);
        b.appendMillisOfDay(8);
        b.appendSecondOfMinute(2);
        b.appendSecondOfDay(5);
        b.appendMinuteOfHour(2);
        b.appendMinuteOfDay(4);
        b.appendHourOfDay(2);
        b.appendClockhourOfDay(2);
        b.appendHourOfHalfday(2);
        b.appendClockhourOfHalfday(2);
        b.appendDayOfWeek(1);
        b.appendDayOfMonth(2);
        b.appendDayOfYear(3);
        b.appendWeekOfWeekyear(2);
        b.appendWeekyear(4, 4);
        b.appendMonthOfYear(2);
        b.appendYear(4, 4);
        b.appendYearOfEra(4, 4);
        b.appendYearOfCentury(2, 2);
        b.appendCenturyOfEra(2, 2);
        b.appendHalfdayOfDayText();
        b.appendDayOfWeekText();
        b.appendDayOfWeekShortText();
        b.appendMonthOfYearText();
        b.appendMonthOfYearShortText();
        b.appendEraText();
        b.appendTwoDigitWeekyear(2000, true);

        Assert.assertTrue(b.canBuildFormatter());
    }

    @Test
    public void testFractions() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendFractionOfSecond(1, 3)
                .toFormatter();

        DateTime dt = new DateTime(2020, 1, 1, 12, 0, 0, 500, DateTimeZone.UTC);
        Assert.assertEquals("5", f.print(dt));

        DateTime parsed = f.parseDateTime("23");
        Assert.assertEquals(230, parsed.getMillisOfSecond());

        DateTimeFormatter fMinMax = new DateTimeFormatterBuilder()
                .appendFractionOfMinute(2, 4)
                .appendLiteral(' ')
                .appendFractionOfHour(1, 2)
                .appendLiteral(' ')
                .appendFractionOfDay(1, 3)
                .toFormatter();
        Assert.assertNotNull(fMinMax.print(dt));

        DateTimeFormatter fLargeFraction = new DateTimeFormatterBuilder()
                .appendFraction(DateTimeFieldType.millisOfDay(), 2, 20)
                .toFormatter();
        Assert.assertNotNull(fLargeFraction.print(dt));
    }

    @Test
    public void testTimeZoneOffset() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset("Z", true, 2, 4)
                .toFormatter();

        Assert.assertEquals("Z", f.print(new DateTime(2020, 1, 1, 0, 0, 0, DateTimeZone.UTC)));

        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        Assert.assertEquals("+02:30", f.print(new DateTime(2020, 1, 1, 0, 0, 0, zonePlus2)));

        DateTime parsedUTC = f.parseDateTime("Z");
        Assert.assertEquals(0, parsedUTC.getZone().getOffset(0L));

        DateTime parsedPlus2 = f.parseDateTime("+02:30");
        Assert.assertEquals(zonePlus2.getOffset(0L), parsedPlus2.getZone().getOffset(0L));

        DateTimeFormatter fFull = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset("UTC", "UTC", true, 4, 4)
                .toFormatter();
        DateTimeZone zoneFull = DateTimeZone.forOffsetHoursMinutesSecondsMillis(1, 2, 3, 400);
        Assert.assertEquals("+01:02:03.400", fFull.print(new DateTime(2020, 1, 1, 0, 0, 0, zoneFull)));

        DateTime parsedFull = fFull.parseDateTime("+01:02:03.400");
        Assert.assertEquals(zoneFull.getOffset(0L), parsedFull.getZone().getOffset(0L));

        DateTimeFormatter fNoSep = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset(null, false, 1, 3)
                .toFormatter();
        Assert.assertEquals("+0230", fNoSep.print(new DateTime(2020, 1, 1, 0, 0, 0, zonePlus2)));
        Assert.assertEquals(zonePlus2.getOffset(0L), fNoSep.parseDateTime("+0230").getZone().getOffset(0L));

        DateTimeFormatter fEmptyZero = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset("", "", false, 1, 2)
                .toFormatter();
        Assert.assertEquals(0, fEmptyZero.parseDateTime("").getZone().getOffset(0L));
    }

    @Test
    public void testTimeZoneNamesAndIds() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("GMT", DateTimeZone.UTC);
        lookup.put("EST", DateTimeZone.forOffsetHours(-5));

        DateTimeFormatter fNames = new DateTimeFormatterBuilder()
                .appendTimeZoneName(lookup)
                .appendLiteral(' ')
                .appendTimeZoneShortName(lookup)
                .toFormatter();

        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, DateTimeZone.UTC);
        Assert.assertNotNull(fNames.print(dt));

        DateTime parsed = fNames.parseDateTime("GMT GMT");
        Assert.assertEquals(DateTimeZone.UTC, parsed.getZone());

        DateTimeFormatter fNamesNoLookup = new DateTimeFormatterBuilder()
                .appendTimeZoneName()
                .appendLiteral(' ')
                .appendTimeZoneShortName()
                .toFormatter();
        Assert.assertTrue(fNamesNoLookup.isPrinter());
        Assert.assertFalse(fNamesNoLookup.isParser());
        Assert.assertNotNull(fNamesNoLookup.print(dt));

        DateTimeFormatter fId = new DateTimeFormatterBuilder()
                .appendTimeZoneId()
                .toFormatter();

        Assert.assertEquals("UTC", fId.print(dt));
        Assert.assertEquals(DateTimeZone.UTC, fId.parseDateTime("UTC").getZone());
    }

    @Test
    public void testTextAndShortText() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendMonthOfYearText()
                .appendLiteral(' ')
                .appendDayOfWeekShortText()
                .appendLiteral(' ')
                .appendEraText()
                .toFormatter();

        DateTime dt = new DateTime(2020, 1, 1, 12, 0, 0, DateTimeZone.UTC);
        String printed = f.withLocale(Locale.ENGLISH).print(dt);
        Assert.assertTrue(printed.contains("January"));
        Assert.assertTrue(printed.contains("Wed"));
        Assert.assertTrue(printed.contains("AD"));

        DateTime parsed = f.withLocale(Locale.ENGLISH).parseDateTime("January Wed AD");
        Assert.assertEquals(1, parsed.getMonthOfYear());
        Assert.assertEquals(3, parsed.getDayOfWeek());

        DateTime parsedBce = new DateTimeFormatterBuilder().appendEraText().toFormatter()
                .withLocale(Locale.ENGLISH).parseDateTime("BCE");
        Assert.assertEquals(DateTimeConstants.BCE, parsedBce.getEra());
    }

    @Test
    public void testFixedDecimalErrorsAndBounds() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendFixedDecimal(DateTimeFieldType.year(), 4)
                .toFormatter();

        try {
            f.parseDateTime("123");
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        try {
            f.parseDateTime("12345");
            Assert.fail();
        } catch (IllegalArgumentException e) {}

        DateTimeFormatter signedF = new DateTimeFormatterBuilder()
                .appendFixedSignedDecimal(DateTimeFieldType.year(), 4)
                .toFormatter();

        Assert.assertEquals(2020, signedF.parseDateTime("+2020").getYear());
        Assert.assertEquals(-2020, signedF.parseDateTime("-2020").getYear());
        try {
            signedF.parseDateTime("+202");
            Assert.fail();
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testMatchingParserAndOptional() {
        DateTimeParser[] parsers = new DateTimeParser[]{
                new DateTimeFormatterBuilder().appendLiteral("v1:").appendYear(4, 4).toParser(),
                new DateTimeFormatterBuilder().appendLiteral("v2:").appendYear(2, 2).toParser()
        };

        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .append(null, parsers)
                .toFormatter();

        Assert.assertEquals(2020, f.parseDateTime("v1:2020").getYear());
        Assert.assertEquals(2020, f.parseDateTime("v2:20").getYear());

        DateTimeFormatter optionalF = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendOptional(new DateTimeFormatterBuilder().appendLiteral('-').appendMonthOfYear(2).toParser())
                .toFormatter();

        Assert.assertEquals(2020, optionalF.parseDateTime("2020").getYear());
        DateTime dtWithMonth = optionalF.parseDateTime("2020-05");
        Assert.assertEquals(2020, dtWithMonth.getYear());
        Assert.assertEquals(5, dtWithMonth.getMonthOfYear());
    }

    @Test
    public void testAppendPattern() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .toFormatter();

        DateTime dt = new DateTime(2021, 12, 25, 0, 0, 0, DateTimeZone.UTC);
        Assert.assertEquals("2021-12-25", f.print(dt));
        Assert.assertEquals(dt, f.parseDateTime("2021-12-25").withZone(DateTimeZone.UTC));
    }

    @Test
    public void testPartialPrintingAndWriting() throws IOException {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2)
                .appendLiteral(' ')
                .appendHourOfDay(2)
                .appendLiteral(':')
                .appendMinuteOfHour(2)
                .appendLiteral(':')
                .appendFractionOfSecond(3, 3)
                .toFormatter();

        LocalDate date = new LocalDate(2021, 5, 9);
        LocalTime time = new LocalTime(14, 30, 15, 500);

        CharArrayWriter writer = new CharArrayWriter();
        f.printTo(writer, date);
        Assert.assertTrue(writer.toString().startsWith("2021-05-09"));

        writer.reset();
        f.printTo(writer, time);
        Assert.assertTrue(writer.toString().contains("14:30"));
    }

    @Test
    public void testCompositePrinterAndParserSingletons() {
        DateTimePrinter printer = new DateTimeFormatterBuilder().appendLiteral('A').toPrinter();
        DateTimeParser parser = new DateTimeFormatterBuilder().appendLiteral('B').toParser();

        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
        b.append(printer, parser);
        Assert.assertTrue(b.canBuildPrinter());
        Assert.assertTrue(b.canBuildParser());

        DateTimeFormatter f = b.toFormatter();
        Assert.assertEquals("A", f.print(0L));
        Assert.assertNotNull(f.parseDateTime("B"));
    }

    @Test
    public void testAppendUnknownString() throws IOException {
        StringBuffer sb = new StringBuffer();
        DateTimeFormatterBuilder.appendUnknownString(sb, 3);
        Assert.assertEquals("\ufffd\ufffd\ufffd", sb.toString());

        StringWriter sw = new StringWriter();
        DateTimeFormatterBuilder.printUnknownString(sw, 2);
        Assert.assertEquals("\ufffd\ufffd", sw.toString());
    }
}
