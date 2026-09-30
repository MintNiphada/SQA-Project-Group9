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
import java.util.List;

import org.junit.Test;

/**
 * Comprehensive test suite for {@link CSVFormat}.
 */
public class CSVFormatTest {

    @Test
    public void testPredefinedFormatsConstants() {
        // DEFAULT
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteChar());
        assertNull(CSVFormat.DEFAULT.getQuotePolicy());
        assertNull(CSVFormat.DEFAULT.getCommentStart());
        assertNull(CSVFormat.DEFAULT.getEscape());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());

        // RFC4180
        assertEquals(',', CSVFormat.RFC4180.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.RFC4180.getQuoteChar());
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.RFC4180.getRecordSeparator());

        // EXCEL
        assertEquals(',', CSVFormat.EXCEL.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.EXCEL.getQuoteChar());
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.EXCEL.getRecordSeparator());

        // TDF
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.TDF.getQuoteChar());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.TDF.getIgnoreEmptyLines());

        // MYSQL
        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscape());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
        assertNull(CSVFormat.MYSQL.getQuoteChar());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteChar());
        assertNull(format.getQuotePolicy());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLfDelimiterThrows() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithCrDelimiterThrows() {
        CSVFormat.newFormat('\r');
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLfThrows() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterCrThrows() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteChar('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteChar());
        assertTrue(format.isQuoting());

        CSVFormat noQuote = format.withQuoteChar((Character) null);
        assertNull(noQuote.getQuoteChar());
        assertFalse(noQuote.isQuoting());

        CSVFormat quoteObj = format.withQuoteChar(Character.valueOf('"'));
        assertEquals(Character.valueOf('"'), quoteObj.getQuoteChar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharPrimitiveLfThrows() {
        CSVFormat.DEFAULT.withQuoteChar('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharPrimitiveCrThrows() {
        CSVFormat.DEFAULT.withQuoteChar('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharObjectLfThrows() {
        CSVFormat.DEFAULT.withQuoteChar(Character.valueOf('\n'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharObjectCrThrows() {
        CSVFormat.DEFAULT.withQuoteChar(Character.valueOf('\r'));
    }

    @Test
    public void testWithQuotePolicy() {
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        assertEquals(Quote.ALL, format.getQuotePolicy());

        CSVFormat noPolicy = format.withQuotePolicy(null);
        assertNull(noPolicy.getQuotePolicy());
    }

    @Test
    public void testWithCommentStart() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        assertEquals(Character.valueOf('#'), format.getCommentStart());
        assertTrue(format.isCommentingEnabled());

        CSVFormat noComment = format.withCommentStart((Character) null);
        assertNull(noComment.getCommentStart());
        assertFalse(noComment.isCommentingEnabled());

        CSVFormat commentObj = format.withCommentStart(Character.valueOf('/'));
        assertEquals(Character.valueOf('/'), commentObj.getCommentStart());
        assertTrue(commentObj.isCommentingEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartPrimitiveLfThrows() {
        CSVFormat.DEFAULT.withCommentStart('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartPrimitiveCrThrows() {
        CSVFormat.DEFAULT.withCommentStart('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartObjectLfThrows() {
        CSVFormat.DEFAULT.withCommentStart(Character.valueOf('\n'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartObjectCrThrows() {
        CSVFormat.DEFAULT.withCommentStart(Character.valueOf('\r'));
    }

    @Test
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscape());
        assertTrue(format.isEscaping());

        CSVFormat noEscape = format.withEscape((Character) null);
        assertNull(noEscape.getEscape());
        assertFalse(noEscape.isEscaping());

        CSVFormat escapeObj = format.withEscape(Character.valueOf('!'));
        assertEquals(Character.valueOf('!'), escapeObj.getEscape());
        assertTrue(escapeObj.isEscaping());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapePrimitiveLfThrows() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapePrimitiveCrThrows() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeObjectLfThrows() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\n'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeObjectCrThrows() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\r'));
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());

        CSVFormat disabled = format.withIgnoreSurroundingSpaces(false);
        assertFalse(disabled.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());

        CSVFormat enabled = format.withIgnoreEmptyLines(true);
        assertTrue(enabled.getIgnoreEmptyLines());
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", formatChar.getRecordSeparator());

        CSVFormat formatStr = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertEquals("\r\n", formatStr.getRecordSeparator());

        CSVFormat formatNull = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        assertNull(formatNull.getRecordSeparator());
    }

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullHandling());

        CSVFormat disabled = format.withNullString(null);
        assertNull(disabled.getNullString());
        assertFalse(disabled.isNullHandling());
    }

    @Test
    public void testWithHeader() {
        String[] header = new String[]{"A", "B", "C"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, format.getHeader());

        // Verify cloning on getHeader
        String[] fetchedHeader = format.getHeader();
        assertNotSame(header, fetchedHeader);
        fetchedHeader[0] = "X";
        assertEquals("A", format.getHeader()[0]);

        // Null header
        CSVFormat noHeader = format.withHeader((String[]) null);
        assertNull(noHeader.getHeader());

        // Empty header
        CSVFormat emptyHeader = format.withHeader();
        assertNotNull(emptyHeader.getHeader());
        assertEquals(0, emptyHeader.getHeader().length);
    }

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());

        CSVFormat disabled = format.withSkipHeaderRecord(false);
        assertFalse(disabled.getSkipHeaderRecord());
    }

    @Test
    public void testValidateSuccess() {
        CSVFormat.DEFAULT.validate();
        CSVFormat.RFC4180.validate();
        CSVFormat.EXCEL.validate();
        CSVFormat.TDF.validate();
        CSVFormat.MYSQL.validate();

        CSVFormat custom = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuoteChar('\'')
                .withCommentStart('#')
                .withEscape('\\')
                .withHeader("Col1", "Col2");
        custom.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateQuoteCharSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withDelimiter('"').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateEscapeSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withEscape(',').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateCommentStartSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withCommentStart(',').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateQuoteCharSameAsCommentStartThrows() {
        CSVFormat.DEFAULT.withCommentStart('"').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateEscapeSameAsCommentStartThrows() {
        CSVFormat.DEFAULT.withEscape('#').withCommentStart('#').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateQuoteNoneWithoutEscapeThrows() {
        CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape((Character) null).validate();
    }

    @Test
    public void testValidateQuoteNoneWithEscapeSucceeds() {
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        format.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateDuplicateHeaderThrows() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A").validate();
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;

        // Reflexive
        assertTrue(f1.equals(f1));
        assertEquals(f1.hashCode(), f2.hashCode());
        assertTrue(f1.equals(f2));

        // Non-nullity & Type check
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a CSVFormat"));

        // Delimiter
        CSVFormat fDiffDelimiter = f1.withDelimiter(';');
        assertFalse(f1.equals(fDiffDelimiter));
        assertFalse(fDiffDelimiter.equals(f1));

        // QuotePolicy
        CSVFormat fDiffPolicy = f1.withQuotePolicy(Quote.ALL);
        assertFalse(f1.equals(fDiffPolicy));
        assertFalse(fDiffPolicy.equals(f1));
        CSVFormat fDiffPolicy2 = f1.withQuotePolicy(Quote.MINIMAL);
        assertFalse(fDiffPolicy.equals(fDiffPolicy2));

        // QuoteChar
        CSVFormat fNoQuote = f1.withQuoteChar((Character) null);
        assertFalse(f1.equals(fNoQuote));
        assertFalse(fNoQuote.equals(f1));
        assertTrue(fNoQuote.equals(fNoQuote));
        CSVFormat fDiffQuote = f1.withQuoteChar('\'');
        assertFalse(f1.equals(fDiffQuote));

        // CommentStart
        CSVFormat fComment1 = f1.withCommentStart('#');
        CSVFormat fComment2 = f1.withCommentStart('!');
        assertFalse(f1.equals(fComment1));
        assertFalse(fComment1.equals(f1));
        assertFalse(fComment1.equals(fComment2));
        assertTrue(fComment1.equals(f1.withCommentStart('#')));

        // Escape
        CSVFormat fEscape1 = f1.withEscape('\\');
        CSVFormat fEscape2 = f1.withEscape('^');
        assertFalse(f1.equals(fEscape1));
        assertFalse(fEscape1.equals(f1));
        assertFalse(fEscape1.equals(fEscape2));
        assertTrue(fEscape1.equals(f1.withEscape('\\')));

        // NullString
        CSVFormat fNullStr1 = f1.withNullString("NULL");
        CSVFormat fNullStr2 = f1.withNullString("N/A");
        assertFalse(f1.equals(fNullStr1));
        assertFalse(fNullStr1.equals(f1));
        assertFalse(fNullStr1.equals(fNullStr2));
        assertTrue(fNullStr1.equals(f1.withNullString("NULL")));

        // Header
        CSVFormat fHeader1 = f1.withHeader("H1", "H2");
        CSVFormat fHeader2 = f1.withHeader("H1", "H3");
        assertFalse(f1.equals(fHeader1));
        assertFalse(fHeader1.equals(f1));
        assertFalse(fHeader1.equals(fHeader2));
        assertTrue(fHeader1.equals(f1.withHeader("H1", "H2")));

        // IgnoreSurroundingSpaces
        CSVFormat fIgnoreSpaces = f1.withIgnoreSurroundingSpaces(true);
        assertFalse(f1.equals(fIgnoreSpaces));

        // IgnoreEmptyLines
        CSVFormat fIgnoreEmpty = f1.withIgnoreEmptyLines(false);
        assertFalse(f1.equals(fIgnoreEmpty));

        // SkipHeaderRecord
        CSVFormat fSkipHeader = f1.withSkipHeaderRecord(true);
        assertFalse(f1.equals(fSkipHeader));

        // RecordSeparator
        CSVFormat fNoRecSep = f1.withRecordSeparator((String) null);
        CSVFormat fRecSep2 = f1.withRecordSeparator("\n");
        assertFalse(f1.equals(fNoRecSep));
        assertFalse(fNoRecSep.equals(f1));
        assertTrue(fNoRecSep.equals(fNoRecSep));
        assertFalse(f1.equals(fRecSep2));

        // Hashcode coverage on null/different branches
        CSVFormat complexFormat = CSVFormat.newFormat(';')
                .withQuotePolicy(Quote.NON_NUMERIC)
                .withQuoteChar('\'')
                .withCommentStart('/')
                .withEscape('\\')
                .withNullString("EMPTY")
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .withSkipHeaderRecord(true)
                .withRecordSeparator("\n")
                .withHeader("A", "B");

        assertNotNull(complexFormat.hashCode());
        assertNotNull(fNoQuote.hashCode());
        assertNotNull(fNoRecSep.hashCode());
    }

    @Test
    public void testToString() {
        CSVFormat complete = CSVFormat.DEFAULT
                .withEscape('\\')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withNullString("N/A")
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withSkipHeaderRecord(true)
                .withHeader("Col1", "Col2");

        String str = complete.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<N/A>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:true"));
        assertTrue(str.contains("Header:[Col1, Col2]"));

        CSVFormat minimal = CSVFormat.newFormat(';')
                .withIgnoreEmptyLines(false)
                .withIgnoreSurroundingSpaces(false)
                .withSkipHeaderRecord(false);

        String minStr = minimal.toString();
        assertEquals("Delimiter=<;> SkipHeaderRecord:false", minStr);
    }

    @Test
    public void testFormatValues() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("\"a\",\"b\",\"c\"", formatted);

        String formattedWithNull = CSVFormat.DEFAULT.withNullString("NULL").format("a", null, "b");
        assertEquals("\"a\",NULL,\"b\"", formattedWithNull);

        String emptyFormat = CSVFormat.DEFAULT.format();
        assertEquals("", emptyFormat);
    }

    @Test
    public void testParse() throws IOException {
        String input = "a,b,c\n1,2,3";
        CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(input));
        assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        assertEquals("1", records.get(1).get(0));
        assertEquals("2", records.get(1).get(1));
        assertEquals("3", records.get(1).get(2));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        CSVFormat format = CSVFormat.DEFAULT
                .withCommentStart('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withHeader("X", "Y")
                .withSkipHeaderRecord(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(format);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CSVFormat deserialized = (CSVFormat) ois.readObject();

        assertEquals(format, deserialized);
        assertEquals(format.hashCode(), deserialized.hashCode());
        assertArrayEquals(format.getHeader(), deserialized.getHeader());
    }
}
