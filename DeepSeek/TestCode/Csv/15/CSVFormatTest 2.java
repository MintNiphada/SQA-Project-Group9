package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.COMMA;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE_CHAR;
import static org.apache.commons.csv.Constants.TAB;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;

public class CSVFormatTest {

    // Helper enum for testing withHeader(Class)
    enum TestHeader { Name, Email, Phone }

    // Fake ResultSetMetaData for testing withHeader(ResultSetMetaData)
    private static class FakeResultSetMetaData implements ResultSetMetaData {
        private final String[] labels;
        FakeResultSetMetaData(String... labels) {
            this.labels = labels;
        }
        @Override public int getColumnCount() { return labels.length; }
        @Override public String getColumnLabel(int column) { return labels[column - 1]; }
        // all other methods throw UnsupportedOperationException
        @Override public boolean isAutoIncrement(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isCaseSensitive(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isSearchable(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isCurrency(int column) { throw new UnsupportedOperationException(); }
        @Override public int isNullable(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isSigned(int column) { throw new UnsupportedOperationException(); }
        @Override public int getColumnDisplaySize(int column) { throw new UnsupportedOperationException(); }
        @Override public String getSchemaName(int column) { throw new UnsupportedOperationException(); }
        @Override public int getPrecision(int column) { throw new UnsupportedOperationException(); }
        @Override public int getScale(int column) { throw new UnsupportedOperationException(); }
        @Override public String getTableName(int column) { throw new UnsupportedOperationException(); }
        @Override public String getCatalogName(int column) { throw new UnsupportedOperationException(); }
        @Override public int getColumnType(int column) { throw new UnsupportedOperationException(); }
        @Override public String getColumnTypeName(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isReadOnly(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isWritable(int column) { throw new UnsupportedOperationException(); }
        @Override public boolean isDefinitelyWritable(int column) { throw new UnsupportedOperationException(); }
        @Override public String getColumnClassName(int column) { throw new UnsupportedOperationException(); }
        @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
        @Override public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
    }

    @Test
    public void testPredefinedDefaults() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD_CSV);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.POSTGRESQL_CSV);
        assertNotNull(CSVFormat.POSTGRESQL_TEXT);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.TDF);
    }

    @Test
    public void testDefaultFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(COMMA, format.getDelimiter());
        assertEquals(Character.valueOf(DOUBLE_QUOTE_CHAR), format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertTrue(format.getIgnoreEmptyLines());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertEquals(CRLF, format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
        assertFalse(format.getTrailingDelimiter());
        assertFalse(format.getTrim());
        assertFalse(format.getAutoFlush());
    }

    @Test
    public void testExcelFormat() {
        CSVFormat format = CSVFormat.EXCEL;
        assertFalse(format.getIgnoreEmptyLines());
        assertTrue(format.getAllowMissingColumnNames());
    }

    @Test
    public void testRfc4180Format() {
        CSVFormat format = CSVFormat.RFC4180;
        assertFalse(format.getIgnoreEmptyLines());
    }

    @Test
    public void testTdfFormat() {
        CSVFormat format = CSVFormat.TDF;
        assertEquals(TAB, format.getDelimiter());
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testMysqlFormat() {
        CSVFormat format = CSVFormat.MYSQL;
        assertEquals(TAB, format.getDelimiter());
        assertEquals(Character.valueOf(BACKSLASH), format.getEscapeCharacter());
        assertNull(format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("\\N", format.getNullString());
        assertEquals(QuoteMode.ALL_NON_NULL, format.getQuoteMode());
        assertFalse(format.getIgnoreEmptyLines());
    }

    @Test
    public void testInformixUnloadFormat() {
        CSVFormat format = CSVFormat.INFORMIX_UNLOAD;
        assertEquals('|', format.getDelimiter());
        assertEquals(Character.valueOf(BACKSLASH), format.getEscapeCharacter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testInformixUnloadCsvFormat() {
        CSVFormat format = CSVFormat.INFORMIX_UNLOAD_CSV;
        assertEquals(COMMA, format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testPostgresqlCsvFormat() {
        CSVFormat format = CSVFormat.POSTGRESQL_CSV;
        assertEquals(COMMA, format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getEscapeCharacter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("", format.getNullString());
        assertEquals(QuoteMode.ALL_NON_NULL, format.getQuoteMode());
    }

    @Test
    public void testPostgresqlTextFormat() {
        CSVFormat format = CSVFormat.POSTGRESQL_TEXT;
        assertEquals(TAB, format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getEscapeCharacter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("\\N", format.getNullString());
        assertEquals(QuoteMode.ALL_NON_NULL, format.getQuoteMode());
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getRecordSeparator());
        assertFalse(format.getIgnoreEmptyLines());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
        assertFalse(format.getTrailingDelimiter());
        assertFalse(format.getTrim());
        assertFalse(format.getAutoFlush());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatLineBreakDelimiterCarriageReturn() {
        CSVFormat.newFormat('\r');
    }

    @Test
    public void testValueOf() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD, CSVFormat.valueOf("InformixUnload"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD_CSV, CSVFormat.valueOf("InformixUnloadCsv"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MySQL"));
        assertEquals(CSVFormat.POSTGRESQL_CSV, CSVFormat.valueOf("PostgreSQLCsv"));
        assertEquals(CSVFormat.POSTGRESQL_TEXT, CSVFormat.valueOf("PostgreSQLText"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        CSVFormat.valueOf("NONEXISTENT");
    }

    @Test
    public void testPredefinedEnumGetFormat() {
        assertSame(CSVFormat.DEFAULT, CSVFormat.Predefined.Default.getFormat());
        assertSame(CSVFormat.EXCEL, CSVFormat.Predefined.Excel.getFormat());
        assertSame(CSVFormat.INFORMIX_UNLOAD, CSVFormat.Predefined.InformixUnload.getFormat());
        assertSame(CSVFormat.INFORMIX_UNLOAD_CSV, CSVFormat.Predefined.InformixUnloadCsv.getFormat());
        assertSame(CSVFormat.MYSQL, CSVFormat.Predefined.MySQL.getFormat());
        assertSame(CSVFormat.POSTGRESQL_CSV, CSVFormat.Predefined.PostgreSQLCsv.getFormat());
        assertSame(CSVFormat.POSTGRESQL_TEXT, CSVFormat.Predefined.PostgreSQLText.getFormat());
        assertSame(CSVFormat.RFC4180, CSVFormat.Predefined.RFC4180.getFormat());
        assertSame(CSVFormat.TDF, CSVFormat.Predefined.TDF.getFormat());
    }

    @Test
    public void testEqualsSameObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(CSVFormat.DEFAULT.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(CSVFormat.DEFAULT.equals("string"));
    }

    @Test
    public void testEqualsIdentical() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true); // DEFAULT already true, should be equal
        assertEquals(f1, f2);
    }

    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withDelimiter(';');
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentQuoteMode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withQuoteMode(QuoteMode.ALL);
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentQuoteChar() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withQuote('\'');
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentCommentMarker() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withCommentMarker('#');
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentEscapeCharacter() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withEscape('\\');
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentNullString() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withNullString("NULL");
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentHeader() {
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A", "B");
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("A", "C");
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withIgnoreSurroundingSpaces(true);
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat f1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true); // DEFAULT has true
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(f1.equals(f2));
        assertFalse(f1.equals(f2);
    }

    @Test
    public void testEqualsDifferentSkipHeaderRecord() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withSkipHeaderRecord(true);
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsDifferentRecordSeparator() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withRecordSeparator("\n");
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testHashCodeConsistent() {
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A", "B").withNullString("null");
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("A", "B").withNullString("null");
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString() {
        CSVFormat format = CSVFormat.DEFAULT;
        String str = format.toString();
        assertTrue(str.contains("Delimiter=<"));
        assertTrue(str.contains("RecordSeparator=<"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
    }

    @Test
    public void testToStringWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        String str = format.toString();
        assertTrue(str.contains("Escape=<\\"));
    }

    @Test
    public void testToStringWithQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        String str = format.toString();
        assertTrue(str.contains("QuoteChar=<'"));
    }

    @Test
    public void testToStringWithComment() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String str = format.toString();
        assertTrue(str.contains("CommentStart=<#"));
    }

    @Test
    public void testToStringWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String str = format.toString();
        assertTrue(str.contains("NullString=<NULL"));
    }

    @Test
    public void testToStringWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        String str = format.toString();
        assertTrue(str.contains("EmptyLines:ignored"));
    }

    @Test
    public void testToStringWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String str = format.toString();
        assertTrue(str.contains("SurroundingSpaces:ignored"));
    }

    @Test
    public void testToStringWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        String str = format.toString();
        assertTrue(str.contains("IgnoreHeaderCase:ignored"));
    }

    @Test
    public void testToStringWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        String str = format.toString();
        assertTrue(str.contains("Header:[Col1, Col2]"));
    }

    @Test
    public void testToStringWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", "Comment2");
        String str = format.toString();
        assertTrue(str.contains("HeaderComments:[Comment1, Comment2]"));
    }

    // Validation tests

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterCarriageReturn() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharEqualsDelimiter() {
        CSVFormat.DEFAULT.withQuote(COMMA); // delimiter is COMMA
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharEqualsDelimiter() {
        CSVFormat.DEFAULT.withEscape(COMMA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentMarkerEqualsDelimiter() {
        CSVFormat.DEFAULT.withCommentMarker(COMMA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('#').
            withQuote('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('#').
            withEscape('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteModeNoneNoEscape() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaders() {
        CSVFormat.DEFAULT.withHeader("A", "A");
    }

    // AllowMissingColumnNames

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
        format = CSVFormat.DEFAULT.withAllowMissingColumnNames(false);
        assertFalse(format.getAllowMissingColumnNames());
        format = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        assertTrue(format.getAllowMissingColumnNames());
    }

    // CommentMarker

    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());
        format = format.withCommentMarker(null);
        assertNull(format.getCommentMarker());
        assertFalse(format.isCommentMarkerSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    // Delimiter

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    // Escape

    @Test
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());
        format = format.withEscape(null);
        assertNull(format.getEscapeCharacter());
        assertFalse(format.isEscapeCharacterSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    // FirstRecordAsHeader

    @Test
    public void testWithFirstRecordAsHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertArrayEquals(new String[0], format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
    }

    // Header from String array

    @Test
    public void testWithHeaderStringArray() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        assertArrayEquals(new String[]{"A", "B"}, format.getHeader());
        // null to disable
        format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    @Test
    public void testWithEmptyHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(); // empty array
        assertArrayEquals(new String[0], format.getHeader());
    }

    // Header from Class

    @Test
    public void testWithHeaderClass() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        assertArrayEquals(new String[]{"Name", "Email", "Phone"}, format.getHeader());
    }

    @Test
    public void testWithHeaderNullClass() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((Class<?>) null);
        assertNull(format.getHeader());
    }

    // Header from ResultSetMetaData

    @Test
    public void testWithHeaderResultSetMetaData() throws SQLException {
        ResultSetMetaData metaData = new FakeResultSetMetaData("Col1", "Col2");
        CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
    }

    @Test
    public void testWithHeaderResultSetMetaDataNull() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(format.getHeader());
    }

    // HeaderResultSet (null) - since we can't easily mock ResultSet, test with null

    @Test
    public void testWithHeaderResultSetNull() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((java.sql.ResultSet) null);
        assertNull(format.getHeader());
    }

    // HeaderComments

    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", "Comment2");
        assertArrayEquals(new String[]{"Comment1", "Comment2"}, format.getHeaderComments());
        format = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(format.getHeaderComments());
        format = CSVFormat.DEFAULT.withHeaderComments(); // empty
        assertArrayEquals(new String[0], format.getHeaderComments());
    }

    // IgnoreEmptyLines

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
        format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
        format = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        assertTrue(format.getIgnoreEmptyLines());
    }

    // IgnoreHeaderCase

    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
        format = CSVFormat.DEFAULT.withIgnoreHeaderCase(false);
        assertFalse(format.getIgnoreHeaderCase());
        format = CSVFormat.DEFAULT.withIgnoreHeaderCase();
        assertTrue(format.getIgnoreHeaderCase());
    }

    // IgnoreSurroundingSpaces

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
        format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format.getIgnoreSurroundingSpaces());
        format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces();
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    // NullString

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());
        format = CSVFormat.DEFAULT.withNullString(null);
        assertNull(format.getNullString());
        assertFalse(format.isNullStringSet());
    }

    // QuoteChar

    @Test
    public void testWithQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertTrue(format.isQuoteCharacterSet());
        format = format.withQuote(null);
        assertNull(format.getQuoteCharacter());
        assertFalse(format.isQuoteCharacterSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // QuoteMode

    @Test
    public void testWithQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertEquals(QuoteMode.MINIMAL, format.getQuoteMode());
    }

    // RecordSeparator

    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertEquals("\r", format.getRecordSeparator());
        format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
        format = CSVFormat.DEFAULT.withRecordSeparator("|");
        assertEquals("|", format.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorChar() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('X');
        assertEquals("X", format.getRecordSeparator());
    }

    // SkipHeaderRecord

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
        format = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format.getSkipHeaderRecord());
        format = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertTrue(format.getSkipHeaderRecord());
    }

    // TrailingDelimiter

    @Test
    public void testWithTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
        format = CSVFormat.DEFAULT.withTrailingDelimiter(false);
        assertFalse(format.getTrailingDelimiter());
        format = CSVFormat.DEFAULT.withTrailingDelimiter();
        assertTrue(format.getTrailingDelimiter());
    }

    // Trim

    @Test
    public void testWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
        format = CSVFormat.DEFAULT.withTrim(false);
        assertFalse(format.getTrim());
        format = CSVFormat.DEFAULT.withTrim();
        assertTrue(format.getTrim());
    }

    // AutoFlush

    @Test
    public void testWithAutoFlush() {
        CSVFormat format = CSVFormat.DEFAULT.withAutoFlush(true);
        assertTrue(format.getAutoFlush());
        format = CSVFormat.DEFAULT.withAutoFlush(false);
        assertFalse(format.getAutoFlush());
    }

    // Format method

    @Test
    public void testFormat() {
        String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test
    public void testFormatWithNull() {
        String result = CSVFormat.DEFAULT.format("a", null, "c");
        assertEquals("a,,c", result);
    }

    @Test
    public void testFormatWithQuote() {
        String result = CSVFormat.DEFAULT.format("a,b");
        assertEquals("\"a,b\"", result);
    }

    @Test
    public void testFormatWithLineBreakInValue() {
        String result = CSVFormat.DEFAULT.format("a\nb");
        assertEquals("\"a\nb\"", result);
    }

    @Test
    public void testFormatWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        String result = format.format("a\\b");
        assertEquals("a\\\\b", result); // escaped backslash
    }

    @Test
    public void testFormatWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("  a  ", "b");
        assertEquals("a,b", result);
    }

    @Test
    public void testFormatWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NA");
        String result = format.format("a", null, "c");
        assertEquals("a,NA,c", result);
    }

    // Print methods (using Appendable)

    @Test
    public void testPrintObjectNewRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print("hello", sb, true);
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testPrintObjectNotNewRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print("hello", sb, false);
        assertEquals(",hello", sb.toString());
    }

    @Test
    public void testPrintNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print((Object) null, sb, true);
        assertEquals("", sb.toString());
    }

    @Test
    public void testPrintNullWithNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringBuilder sb = new StringBuilder();
        format.print((Object) null, sb, true);
        assertEquals("NULL", sb.toString());
    }

    @Test
    public void testPrintNullQuoteModeAll() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withNullString("NA");
        StringBuilder sb = new StringBuilder();
        format.print((Object) null, sb, true);
        assertEquals("\"NA\"", sb.toString());
    }

    @Test
    public void testPrintWithQuote() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print("a,c", sb, true);
        assertEquals("\"a,c\"", sb.toString());
    }

    @Test
    public void testPrintRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.printRecord(sb, "a", "b", "c");
        assertEquals("a,b,c\r\n", sb.toString()); // default record separator CRLF
    }

    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        format.printRecord(sb, "a", "b");
        assertEquals("a,b,\r\n", sb.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.println(sb);
        assertEquals("\r\n", sb.toString());
    }

    @Test
    public void testPrintlnWithTrailingDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        format.println(sb);
        assertEquals(",\r\n", sb.toString());
    }

    @Test
    public void testPrintlnNoRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        StringBuilder sb = new StringBuilder();
        format.println(sb);
        assertEquals("", sb.toString());
    }

    // Print with escape

    @Test
    public void testPrintAndEscape() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        StringBuilder sb = new StringBuilder();
        format.print("a\nb", sb, true);
        assertEquals("a\\nb", sb.toString());
    }

    // Print with quote modes

    @Test
    public void testPrintQuoteModeAll() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        StringBuilder sb = new StringBuilder();
        format.print("plain", sb, true);
        assertEquals("\"plain\"", sb.toString());
    }

    @Test
    public void testPrintQuoteModeAllNonNull() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL);
        StringBuilder sb = new StringBuilder();
        format.print((Object) null, sb, true);
        assertEquals("", sb.toString()); // null string not set, so empty
    }

    @Test
    public void testPrintQuoteModeNone() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        StringBuilder sb = new StringBuilder();
        format.print("a,c", sb, true);
        assertEquals("a\\, c"?, Actually it should escape delimiter. Let's verify: delimiter is ',' so escape should be applied: "a\\, c"? Actually printAndEscape escapes delimiter, so "a\\, c"? But the input is "a,c" so char sequence includes comma. So output should be "a\\, c". We'll assert commonly. We'll test simpler.
        format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\').withDelimiter(';');
        sb = new StringBuilder();
        format.print("a;b", sb, true);
        assertEquals("a\\;b", sb.toString());
    }

    // parse

    @Test
    public void testParseReturnsParser() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        CSVParser parser = format.parse(new StringReader("a,b\nc,d"));
        assertNotNull(parser);
        assertEquals(2, parser.getRecords().size());
    }

    // printer()

    @Test
    public void testPrinter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = format.printer();
        assertNotNull(printer);
        // no output because we didn't use it
        printer.close();
    }

    // print(File) and print(Path) not tested due to file IO complexity, but we can skip.
}
```
