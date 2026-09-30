package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

public class TargetClassTest {

    @Test
    public void testPredefinedFormats() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
        assertNull(CSVFormat.DEFAULT.getQuoteMode());

        assertEquals(',', CSVFormat.RFC4180.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.RFC4180.getQuoteCharacter());
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertEquals(',', CSVFormat.EXCEL.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.EXCEL.getQuoteCharacter());
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.TDF.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
    }

    @Test
    public void testNewFormat() {
        final CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatLineBreakLF() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatLineBreakCR() {
        CSVFormat.newFormat('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreakChar() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreakCharacter() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\n'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreakChar() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreakCharacter() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreakChar() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreakCharacter() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeader() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test
    public void testHeaderImmutability() {
        final String[] header = {"H1", "H2"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        final String[] returnedHeader = format.getHeader();
        assertArrayEquals(header, returnedHeader);
        assertNotSame(header, returnedHeader);

        returnedHeader[0] = "Modified";
        assertEquals("H1", format.getHeader()[0]);
    }

    @Test
    public void testWithMethods() {
        final CSVFormat custom = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('\'')
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker('#')
                .withEscape('/')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withHeader("Col1", "Col2")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true);

        assertEquals(';', custom.getDelimiter());
        assertEquals(Character.valueOf('\''), custom.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, custom.getQuoteMode());
        assertEquals(Character.valueOf('#'), custom.getCommentMarker());
        assertEquals(Character.valueOf('/'), custom.getEscapeCharacter());
        assertTrue(custom.getIgnoreSurroundingSpaces());
        assertFalse(custom.getIgnoreEmptyLines());
        assertEquals("\n", custom.getRecordSeparator());
        assertEquals("NULL", custom.getNullString());
        assertArrayEquals(new String[]{"Col1", "Col2"}, custom.getHeader());
        assertTrue(custom.getSkipHeaderRecord());
        assertTrue(custom.getAllowMissingColumnNames());

        assertTrue(custom.isQuoteCharacterSet());
        assertTrue(custom.isCommentMarkerSet());
        assertTrue(custom.isEscapeCharacterSet());
        assertTrue(custom.isNullStringSet());

        final CSVFormat disabled = custom
                .withQuote((Character) null)
                .withCommentMarker((Character) null)
                .withEscape((Character) null)
                .withNullString(null)
                .withHeader((String[]) null)
                .withRecordSeparator('\r');

        assertNull(disabled.getQuoteCharacter());
        assertNull(disabled.getCommentMarker());
        assertNull(disabled.getEscapeCharacter());
        assertNull(disabled.getNullString());
        assertNull(disabled.getHeader());
        assertEquals("\r", disabled.getRecordSeparator());

        assertFalse(disabled.isQuoteCharacterSet());
        assertFalse(disabled.isCommentMarkerSet());
        assertFalse(disabled.isEscapeCharacterSet());
        assertFalse(disabled.isNullStringSet());
    }

    @Test
    public void testValidateSameDelimiterAndQuote() {
        try {
            CSVFormat.DEFAULT.withQuote(',');
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateSameDelimiterAndEscape() {
        try {
            CSVFormat.DEFAULT.withEscape(',');
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateSameDelimiterAndCommentMarker() {
        try {
            CSVFormat.DEFAULT.withCommentMarker(',');
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateSameQuoteAndCommentMarker() {
        try {
            CSVFormat.DEFAULT.withCommentMarker('"');
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateSameEscapeAndCommentMarker() {
        try {
            CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateQuoteModeNoneWithoutEscape() {
        try {
            CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        final CSVFormat validNone = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertEquals(QuoteMode.NONE, validNone.getQuoteMode());
    }

    @Test
    public void testEqualsAndHashCode() {
        final CSVFormat f1 = CSVFormat.DEFAULT;
        final CSVFormat f2 = CSVFormat.DEFAULT;

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a CSVFormat"));

        final CSVFormat fDelimiter = f1.withDelimiter(';');
        assertFalse(f1.equals(fDelimiter));
        assertFalse(fDelimiter.equals(f1));

        final CSVFormat fQuoteMode1 = f1.withEscape('\\').withQuoteMode(QuoteMode.ALL);
        final CSVFormat fQuoteMode2 = f1.withEscape('\\').withQuoteMode(QuoteMode.MINIMAL);
        assertFalse(fQuoteMode1.equals(fQuoteMode2));

        final CSVFormat fQuoteCharNull = f1.withQuote((Character) null);
        assertFalse(f1.equals(fQuoteCharNull));
        assertFalse(fQuoteCharNull.equals(f1));
        final CSVFormat fQuoteCharOther = f1.withQuote('\'');
        assertFalse(f1.equals(fQuoteCharOther));

        final CSVFormat fComment = f1.withCommentMarker('#');
        assertFalse(f1.equals(fComment));
        assertFalse(fComment.equals(f1));
        final CSVFormat fCommentOther = f1.withCommentMarker('!');
        assertFalse(fComment.equals(fCommentOther));

        final CSVFormat fEscape = f1.withEscape('/');
        assertFalse(f1.equals(fEscape));
        assertFalse(fEscape.equals(f1));
        final CSVFormat fEscapeOther = f1.withEscape('\\');
        assertFalse(fEscape.equals(fEscapeOther));

        final CSVFormat fNullStr = f1.withNullString("NULL");
        assertFalse(f1.equals(fNullStr));
        assertFalse(fNullStr.equals(f1));
        final CSVFormat fNullStrOther = f1.withNullString("N/A");
        assertFalse(fNullStr.equals(fNullStrOther));

        final CSVFormat fHeader = f1.withHeader("A", "B");
        assertFalse(f1.equals(fHeader));
        assertFalse(fHeader.equals(f1));
        final CSVFormat fHeaderOther = f1.withHeader("A", "C");
        assertFalse(fHeader.equals(fHeaderOther));

        final CSVFormat fSpaces = f1.withIgnoreSurroundingSpaces(true);
        assertFalse(f1.equals(fSpaces));

        final CSVFormat fEmpty = f1.withIgnoreEmptyLines(false);
        assertFalse(f1.equals(fEmpty));

        final CSVFormat fSkipHeader = f1.withSkipHeaderRecord(true);
        assertFalse(f1.equals(fSkipHeader));

        final CSVFormat fRecSepNull = f1.withRecordSeparator((String) null);
        assertFalse(f1.equals(fRecSepNull));
        assertFalse(fRecSepNull.equals(f1));
        final CSVFormat fRecSepOther = f1.withRecordSeparator("\n");
        assertFalse(f1.equals(fRecSepOther));

        final CSVFormat complex1 = CSVFormat.newFormat(';')
                .withQuoteMode(null)
                .withQuote((Character) null)
                .withCommentMarker((Character) null)
                .withEscape((Character) null)
                .withNullString(null)
                .withRecordSeparator((String) null);
        final CSVFormat complex2 = CSVFormat.newFormat(';')
                .withQuoteMode(null)
                .withQuote((Character) null)
                .withCommentMarker((Character) null)
                .withEscape((Character) null)
                .withNullString(null)
                .withRecordSeparator((String) null);
        assertTrue(complex1.equals(complex2));
        assertEquals(complex1.hashCode(), complex2.hashCode());
    }

    @Test
    public void testToString() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .withHeader("A", "B");

        final String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
        assertTrue(str.contains("Header:[A, B]"));

        final CSVFormat minimal = CSVFormat.newFormat('|')
                .withIgnoreEmptyLines(false)
                .withIgnoreSurroundingSpaces(false);
        final String minStr = minimal.toString();
        assertEquals("Delimiter=<|> SkipHeaderRecord:false", minStr);
    }

    @Test
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;
        final String result = format.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test
    public void testParseAndPrint() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        final String input = "a,b,c\r\n1,2,3";
        final CSVParser parser = format.parse(new StringReader(input));
        assertNotNull(parser);

        final StringWriter out = new StringWriter();
        final CSVPrinter printer = format.print(out);
        assertNotNull(printer);
        printer.printRecord("x", "y", "z");
        assertEquals("x,y,z\r\n", out.toString());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withHeader("X", "Y")
                .withNullString("null")
                .withQuoteMode(QuoteMode.ALL_NON_NULL);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(format);
        oos.flush();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final CSVFormat deserialized = (CSVFormat) ois.readObject();

        assertEquals(format, deserialized);
        assertEquals(format.hashCode(), deserialized.hashCode());
    }
}
