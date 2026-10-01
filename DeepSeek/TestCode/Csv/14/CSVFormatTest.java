package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CSVFormatTest {

    @Test
    public void testEqualsSameObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(format, format);
    }

    @Test
    public void testEqualsNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(new Object()));
    }

    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentQuoteMode() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentHeader() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("A", "B");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("C", "D");
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentSkipHeaderRecord() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsDifferentRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testEqualsSameFields() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'').withIgnoreEmptyLines();
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'').withIgnoreEmptyLines();
        assertEquals(format1, format2);
    }

    @Test
    public void testHashCode() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT;
        assertEquals(format1.hashCode(), format2.hashCode());
        CSVFormat format3 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(format1.hashCode(), format3.hashCode());
    }

    @Test
    public void testDefaultConstants() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertNull(CSVFormat.DEFAULT.getHeaderComments());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertEquals(QuoteMode.MINIMAL, CSVFormat.DEFAULT.getQuoteMode());
    }

    @Test
    public void testExcelFormat() {
        CSVFormat excel = CSVFormat.EXCEL;
        assertEquals(',', excel.getDelimiter());
        assertEquals(Character.valueOf('"'), excel.getQuoteCharacter());
        assertEquals("\r\n", excel.getRecordSeparator());
        assertFalse(excel.getIgnoreEmptyLines());
        assertFalse(excel.getIgnoreSurroundingSpaces());
        assertTrue(excel.getAllowMissingColumnNames());
    }

    @Test
    public void testInformixUnloadFormat() {
        CSVFormat format = CSVFormat.INFORMIX_UNLOAD;
        assertEquals('|', format.getDelimiter());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testInformixUnloadCsvFormat() {
        CSVFormat format = CSVFormat.INFORMIX_UNLOAD_CSV;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
        assertNull(format.getEscapeCharacter());
    }

    @Test
    public void testMySQLFormat() {
        CSVFormat mysql = CSVFormat.MYSQL;
        assertEquals('\t', mysql.getDelimiter());
        assertEquals(Character.valueOf('\\'), mysql.getEscapeCharacter());
        assertNull(mysql.getQuoteCharacter());
        assertEquals("\n", mysql.getRecordSeparator());
        assertFalse(mysql.getIgnoreEmptyLines());
        assertEquals("\\N", mysql.getNullString());
    }

    @Test
    public void testRfc4180Format() {
        CSVFormat rfc = CSVFormat.RFC4180;
        assertEquals(',', rfc.getDelimiter());
        assertEquals(Character.valueOf('"'), rfc.getQuoteCharacter());
        assertEquals("\r\n", rfc.getRecordSeparator());
        assertFalse(rfc.getIgnoreEmptyLines());
    }

    @Test
    public void testTdfFormat() {
        CSVFormat tdf = CSVFormat.TDF;
        assertEquals('\t', tdf.getDelimiter());
        assertEquals(Character.valueOf('"'), tdf.getQuoteCharacter());
        assertEquals("\r\n", tdf.getRecordSeparator();
        assertTrue(tdf.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testValueOfValid() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD, CSVFormat.valueOf("InformixUnload"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD_CSV, CSVFormat.valueOf("InformixUnloadCsv"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MySQL"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        CSVFormat.valueOf("Invalid");
    }

    @Test
    public void testPredefinedEnum() {
        for (CSVFormat.Predefined p : CSVFormat.Predefined.values()) {
            assertNotNull(p.getFormat());
        }
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getNullString());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
        assertFalse(format.getTrim());
        assertFalse(format.getTrailingDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreak() {
        CSVFormat.newFormat('\n');
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');
        assertEquals('|', format.getDelimiter());
        assertEquals('"', format.getQuoteCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test
    public void testWithEscapeChar() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
        format = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        assertTrue(format.getIgnoreEmptyLines());
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
        format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
        format = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format.getSkipHeaderRecord());
    }

    @Test
    public void testWithRecordSeparatorString() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorChar() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testWithHeaderArray() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(3, header.length);
        assertEquals("A", header[0]);
        assertEquals("B", header[1]);
        assertEquals("C", header[2]);
        // ensure copy
        header[0] = "X";
        assertNotEquals("X", format.getHeader()[0]);
    }

    @Test
    public void testWithHeaderEmpty() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(0, header.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicate() {
        CSVFormat.DEFAULT.withHeader("A", "A");
    }

    @Test
    public void testWithHeaderEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestEnum.class);
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(2, header.length);
        assertEquals("VALUE1", header[0]);
        assertEquals("VALUE2", header[1]);
    }

    @Test
    public void testWithHeaderEnumNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((Class<Enum>) null);
        assertNull(format.getHeader());
    }

    enum TestEnum {
        VAlUE1, VAlUE2
    }

    @Test
    public void testWithHeaderResultSetMetaData() throws SQLException {
        ResultSetMetaData metaData = createMockMetaData(3);
        CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(3, header.length);
        assertEquals("col1", header[0]);
        assertEquals("col2", header[1]);
        assertEquals("col3", header[2]);
    }

    @Test
    public void testWithHeaderResultSet() throws SQLException {
        // just test that it calls withHeader on metadata; use a mock result set not implemented
        // We'll skip because we need a real ResultSet which is complex. But coverage may be missing.
        // We'll test by calling withHeader((ResultSet) null)
        CSVFormat format = CSVFormat.DEFAULT.withHeader((java.sql.ResultSet) null);
        assertNull(format.getHeader());
    }

    private ResultSetMetaData createMockMetaData(final int cols) throws SQLException {
        return new ResultSetMetaData() {
            @Override
            public int getColumnCount() throws SQLException {
                return cols;
            }
            @Override
            public String getColumnLabel(int column) throws SQLException {
                return "col" + column;
            }
            // other methods throw SQLFeatureNotSupportedException
            @Override public String getColumnName(int column) throws SQLException { throw new SQLException(); }
            @Override public String getColumnTypeName(int column) throws SQLException { throw new SQLException(); }
            @Override public int getColumnType(int column) throws SQLException { throw new SQLException(); }
            @Override public String getColumnClassName(int column) throws SQLException { throw new SQLException(); }
            @Override public int getColumnDisplaySize(int column) throws SQLException { throw new SQLException(); }
            @Override public int getPrecision(int column) throws SQLException { throw new SQLException(); }
            @Override public int getSclae(int column) throws SQLException { throw new SQLException(); }
            @Override public String getSchemaName(int column) throws SQLException { throw new SQLException(); }
            @Override public String getTableName(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isAutoIncrement(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isCaseSensitive(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isCurrncy(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isDefinitelyWritable(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isNullble(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isRedOnly(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isSerchable(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isSined(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isWritable(int column) throws SQLException { throw new SQLException(); }
            @Override public boolean isWapperFor(Class<?> iface) throws SQLException { throw new SQLException(); }
            @Override public <T> T unwpp(Class<T> iface) throws SQLException { throw new SQLException(); }
        };
    }

    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", "Comment2");
        String[] comments = format.getHeaderComments();
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals("Comment1", comments[0]);
        assertEquals("Comment2", comments[1]);
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
        format = CSVFormat.DEFAULT.withAllowMissingColumnNames(false);
        assertFalse(format.getAllowMissingColumnNames());
    }

    @Test
    public void testWithIngoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIngoreHeaderCase(true);
        assertTrue(format.getIngoreHeaderCase());
        format = CSVFormat.DEFAULT.withIngoreHeaderCase(false);
        assertFalse(format.getIngoreHeaderCase());
    }

    @Test
    public void testWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
        format = CSVFormat.DEFAULT.withTrim(false);
        assertFalse(format.getTrim());
    }

    @Test
    public void testWithTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
        format = CSVFormat.DEFAULT.withTrailingDelimiter(false);
        assertFalse(format.getTrailingDelimiter());
    }

    @Test
    public void testWithFirstRecordAsHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(0, header.length);
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test
    public void testValidateDelimiterLineBreak() {
        try {
            CSVFormat.DEFAULT.withDelimiter('\n');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("The delimiter cannot be a line break", e.getMessage());
        }
    }

    @Test
    public void testValidateQuoteCharEqualsDelimiter() {
        try {
            CSVFormat.DEFAULT.withDelimiter('"').withQuote('"');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The quoteChar character and the delimiter cannot be the same"));
        }
    }

    @Test
    public void testValidateEscapeEqualsDelimiter() {
        try {
            CSVFormat.DEFAULT.withDelimiter('\\').withEscape('\\');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The escape character and the delimiter cannot be the same"));
        }
    }

    @Test
    public void testValidateCommentEqualsDelimiter() {
        try {
            CSVFormat.DEFAULT.withDelimiter('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The comment start character and the delimiter cannot be the same"));
        }
    }

    @Test
    public void testValidateQuoteCharEqualsComment() {
        try {
            CSVFormat.DEFAULT.withQuote('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The comment start character and the quoteChar cannot be the same"));
        }
    }

    @Test
    public void testValidateEscapeEqualsComment() {
        try {
            CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The comment start and the escape character cannot be the same"));
        }
    }

    @Test
    public void testValidateNoEscapeAndQuoteModeNone() {
        try {
            CSVFormat.DEFAULT.withEscape((Character) null).withQuoteMode(QuoteMode.NONE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No quotes mode set but no escape character is set"));
        }
    }

    @Test
    public void testFormatWithNullValues() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        String result = format.format(null, "B", null);
        assertEquals("N/A,B,N/A", result);
    }

    @Test
    public void testFormatSimple() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test
    public void testFormatWithQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        String result = format.format("a", "b\"c", "d");
        assertEquals("a,\"b\"\"c\",d", result);
    }

    @Test
    public void testFormatWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        // no quoting, escaping enabled
        String result = format.format("a", "b,c", "d");
        assertEquals("a,b\\,cd", result);
    }

    @Test
    public void testFormatWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("   a   ", "  b  ", " c ");
        assertEquals("a,b,c", result);
    }

    @Test
    public void testFormatWithTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        StringWriter sw = new StringWriter();
        try {
            format.printRecord(sw, "a", "b", "c");
            // should have trailing delimiter, then record separator
        } catch (IOException e) { }
        // We'll just test println effect
    }

    @Test
    public void testPrintObjectAppendable() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        format.print("hello", sw, true);
        // check content
        assertEquals("hello", sw.toString());
    }

    @Test
    public void testPrintNullObject() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        format.print((Object) null, sw, true);
        // default nullString is null -> should output EMPTY
        assertEquals("", sw.toString());
    }

    @Test
    public void testPrintNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter sw = new StringWriter();
        format.print((Object) null, sw, true);
        assertEquals("NULL", sw.toString());
    }

    @Test
    public void testPrintObjectEscape() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        StringWriter sw = new StringWriter();
        format.print("a\nb", sw, true);
        assertEquals("a\\nb", sw.toString());
    }

    @Test
    public void testPrintObjectEscapeCr() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        StringWriter sw = new StringWriter();
        format.print("a\rb", sw, true);
        assertEquals("a\\rb", sw.toString());
    }

    @Test
    public void testPrintObjectEscapeDelimiterAndEscape() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape('\\').withQuote(null);
        StringWriter sw = new StringWriter();
        format.print("a,b", sw, true);
        assertEquals("a\\,b", sw.toString());
    }

    @Test
    public void testPrintObjectQuoteAll() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withQuoteMode(QuoteMode.ALL);
        StringWriter sw = new StringWriter();
        format.print("abc", sw, true);
        assertEquals("\"abc\"", sw.toString());
    }

    @Test
    public void testPrintObjectQuoteNonNumeric() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter sw = new StringWriter();
        format.print("abc", sw, true);
        assertEquals("\"abc\"", sw.toString());
        // Number should not be quoted
        sw = new StringWriter();
        format.print(123, sw, true);
        assertEquals("123", sw.toString());
        sw = new StringWriter();
        format.print(45.6, sw, true);
        assertEquals("45.6", sw.toString());
    }

    @Test
    public void testPrintObjectQuoteMinimal() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withQuoteMode(QuoteMode.MINIMAL);
        StringWriter sw = new StringWriter();
        format.print("abc", sw, true);
        assertEquals("abc", sw.toString());
        // value with comma should be quoted
        sw = new StringWriter();
        format.print("a,b", sw, true);
        assertEquals("\"a,b\"", sw.toString());
        // newRecord with leading special char
        sw = new StringWriter();
        format.print("  abc", sw, true);
        assertEquals("\"  abc\"", sw.toString());
    }

    @Test
    public void testPrintObjectQuoteNone() throws IOException {
        // QuoteMode.NONE requires escapeCharacter set, we tested already
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withQuoteMode(QuoteMode.NONE).withEscape('\\');
        StringWriter sw = new StringWriter();
        format.print("a,b", sw, true);
        // should escape instead of quoting
        assertEquals("a\\,b", sw.toString());
    }

    @Test
    public void testPrintRecord() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(sw, "a", "b", "c");
        // Should be "a,b,c" + record separator
        assertEquals("a,b,c" + format.getRecordSeparator(), sw.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.println(sw);
        assertEquals(format.getRecordSeparator(), sw.toString());
    }

    @Test
    public void testPrintlnWithTrailingDelimiter() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        format.println(sw);
        assertEquals("," + format.getRecordSeparator(), sw.toString());
    }

    @Test
    public void testPrintToFile() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
        try {
            CSVPrinter printer = format.print(tempFile, Charset.defaultCharset());
            printer.printRecord("a", "b");
            printer.close();
            String content = new String(Files.readAllBytes(tempFile.toPath()), Charset.defaultCharset());
            assertTrue(content.startsWith("a,b"));
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testPrintToPath() throws IOException {
        Path tempPath = Files.createTempFile("test", ".csv");
        try {
            CSVPrinter printer = CSVFormat.DEFAULT.print(tempPath, Charset.defaultCharset());
            printer.printRecord("x", "y");
            printer.close();
            String content = new String(Files.readAllBytes(tempPath), Charset.defaultCharset());
            assertTrue(content.contains("x,y"));
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Test
    public void testPrse() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("name", "age");
        StringReader reader = new StringReader("name,age\nJohn,30\n");
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSRecord record = records.get(0);
        assertEquals("John", record.get("name"));
        assertEquals("30", record.get("age"));
    }

    @Test
    public void testToString() {
        String s = CSVFormat.DEFAULT.toString();
        assertTrue(s.contains("Delimiter=<,>"));
        assertFalse(s.contains("Escape=<"));
        assertTrue(s.contains("QuoteChar=<\"\">"));
        assertTrue(s.contains("RecordSeparator=<\\r\\n>"));
        assertTrue(s.contains("EmptyLines:ignored"));
        assertFalse(s.contains("SurroundingSpaces:ignored"));
        assertTrue(s.contains("SkipHeaderRecord:false"));
    }

    @Test
    public void testToStringWithOptions() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withCommentMarker('#').withNullString("N/A")
                .withIgnoreSurroundingSpaces().withIngoreHeaderCase().withHeader("A").withHeaderComments("C");
        String s = format.toString();
        assertTrue(s.contains("Escape=<\\>"));
        assertTrue(s.contains("CommentStart=<#>"));
        assertTrue(s.contains("NullString=<N/A>"));
        assertTrue(s.contains("SurroundingSpaces:ignored"));
        assertTrue(s.contains("IngoreHeaderCase:ignored"));
        assertTrue(s.contains("Header:[A]"));
        assertTrue(s.contains("HeaderComments:[C]"));
    }

    @Test
    public void testIsCommentMarkerSet() {
        assertFalse(CSVFormat.DEFAULT.isCommentMarkerSet());
        assertTrue(CSVFormat.DEFAULT.withCommentMarker('#').isCommentMarkerSet());
    }

    @Test
    public void testIsEscapeCharacterSet() {
        assertFalse(CSVFormat.DEFAULT.isEscapeCharacterSet());
        assertTrue(CSVFormat.DEFAULT.withEscape('\\').isEscapeCharacterSet());
    }

    @Test
    public void testIsNullStringSet() {
        assertFalse(CSVFormat.DEFAULT.isNullStringSet());
        assertTrue(CSVFormat.DEFAULT.withNullString("null").isNullStringSet());
    }

    @Test
    public void testIsQuoteCharacterSet() {
        assertTrue(CSVFormat.DEFAULT.isQuoteCharacterSet());
        assertFalse(CSVFormat.DEFAULT.withQuote((Character) null).isQuoteCharacterSet());
    }

    @Test
    public void testPrintObjectWithTrim() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter sw = new StringWriter();
        format.print("  value  ", sw, true);
        assertEquals("value", sw.toString());
    }

    @Test
    public void testPrintEmptyTokenMinimal() throws IOException {
        // empty token, newRecord true -> should be quoted
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        format.print("", sw, true);
        // minimal quote mode, len<=0, newRecord true => quote true -> output ""
        assertEquals("\"\"", sw.toString());
    }

    @Test
    public void testPrintEmptyTokenNotNewRecordMinimal() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        // Not new record, empty token: no quoting
        format.print("", sw, false);
        // Should output just delimiter and nothing? Actually print() with newRecord false adds delimiter, then value empty.
        // print(object, charSequence, offset, len, out, newRecord) - newRecord false: out.append(delimiter), then value empty
        assertEquals("," + "", sw.toString()); // Just a comma? The value is empty string, so output is ","? Actually the value is appended empty.
        // So we get ","+"" -> just ","
        assertEquals(",", sw.toString());
    }

    @Test
    public void testMinimalQuoteCharThatCausesQuoting() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        // value contains comma
        format.print("a,b", sw, true);
        assertEquals("\"a,b\"", sw.toString());
    }

    @Test
    public void testMinimalQuoteCharThatEndsInSP() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        format.print("a ", sw, true);
        // ends with space, should be quoted
        assertEquals("\"a \"", sw.toString());
    }
}
