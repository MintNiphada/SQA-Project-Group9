package org.apache.commons.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

public class CSVPrinterTest {

    // Helper: creates a CSVPrinter that writes to a StringBuilder
    private CSVPrinter createPrinter(final CSVFormat format) {
        StringBuilder sb = new StringBuilder();
        try {
            return new CSVPrinter(sb, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper: creates CSVPrinter with specific Appendable and format
    private CSVPrinter createPrinter(final Appendable out, final CSVFormat format) throws IOException {
        return new CSVPrinter(out, format);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOut() throws Exception {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws Exception {
        new CSVPrinter(new StringBuilder(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidFormat() throws Exception {
        // hybrid quoting and escaping may trigger validate failure
        CSVFormat format = CSVFormat.newFormat(',').withQuoteChar('"').withEscape('"');
        new CSVPrinter(new StringBuilder(), format);
    }

    @Test
    public void testConstructorValid() throws Exception {
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT);
        assertNotNull(printer);
    }

    @Test
    public void testCloseWhenOutIsCloseable() throws IOException {
        Appendable closeable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
        CSVPrinter printer = new CSVPrinter(closeable, CSVFormat.DEFAULT);
        printer.close();
        verify((Closeable) closeable, times(1)).close();
    }

    @Test
    public void testCloseWhenOutNotCloseable() throws IOException {
        Appendable notCloseable = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(notCloseable, CSVFormat.DEFAULT);
        // should not throw
        printer.close();
    }

    @Test(expected = IOException.class)
    public void testCloseThrows() throws IOException {
        Appendable closeable = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
        doThrow(new IOException("close error")).when((Closeable) closeable).close();
        CSVPrinter printer = new CSVPrinter(closeable, CSVFormat.DEFAULT);
        printer.close();
    }

    @Test
    public void testFlushWhenOutIsFlushable() throws IOException {
        Appendable flushable = mock(Appendable.class, withSettings().extraInterfaces(Flushable.class));
        CSVPrinter printer = new CSVPrinter(flushable, CSVFormat.DEFAULT);
        printer.flush();
        verify((Flushable) flushable, times(1)).flush();
    }

    @Test
    public void testFlushWhenOutNotFlushable() throws IOException {
        Appendable notFlushable = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(notFlushable, CSVFormat.DEFAULT);
        // should not throw
        printer.flush();
    }

    @Test(expected = IOException.class)
    public void testFlushThrows() throws IOException {
        Appendable flushable = mock(Appendable.class, withSettings().extraInterfaces(Flushable.class));
        doThrow(new IOException("flush error")).when((Flushable) flushable).flush();
        CSVPrinter printer = new CSVPrinter(flushable, CSVFormat.DEFAULT);
        printer.flush();
    }

    @Test
    public void testPrintNullWithNullStringNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print(null);
        // null string null: should print empty
        assertEquals("", sb.toString());
    }

    @Test
    public void testPrintNullWithNullString() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print(null);
        assertEquals("NULL", sb.toString());
    }

    @Test
    public void testPrintNormalObject() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("hello");
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testPrintMultipleValuesAddsDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a");
        printer.print("b");
        assertEquals("a,b", sb.toString());
    }

    // ========== printAndQuote tests ==========

    @Test
    public void testPrintAndQuotePolicyALL() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("abc");
        // with default quote char "
        assertEquals("\"abc\"", sb.toString());
    }

    @Test
    public void testPrintAndQuotePolicyALLWithEmpty() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("");
        assertEquals("\"\"", sb.toString());
    }

    @Test
    public void testPrintAndQuotePolicyNON_NUMERICWithNumber() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print(123);
        assertEquals("123", sb.toString());
    }

    @Test
    public void testPrintAndQuotePolicyNON_NUMERICWithString() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("abc");
        assertEquals("\"abc\"", sb.toString());
    }

    @Test
    public void testPrintAndQuotePolicyNONE() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('!');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a,b");
        // escaping replaces delimiter
        assertEquals("a!,b", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_EmptyTokenNewRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT); // MINIMAL
        printer.print(""); // empty token, newRecord true -> quote
        assertEquals("\"\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_EmptyTokenNotNewRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a");
        printer.print(""); // after delimiter, newRecord false, len<=0 but newRecord false -> no quote
        assertEquals("a,", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_FirstCharBelowZero() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        // '!' is ascii 33, less than '0' (48) -> quote
        printer.print("!abc");
        assertEquals("\"!abc\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_FirstCharLetter() throws IOException {
        // starts with letter, not special -> no quote
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("abc");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_FirstCharSpecialViaCommentCondition() throws IOException {
        // first char <= COMMENT ('#') but greater than '9'? Actually digits '0'-'9' are > '#' so they bypass that condition
        // we need first char that is not <'0' but <= '#', e.g., ' ' (space) is 32, <= 35 => true
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print(" abc");
        assertEquals("\" abc\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_ContainsDelimiterInside() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a,b");
        assertEquals("\"a,b\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_ContainsQuoteCharInside() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\"b");
        // quote char doubled
        assertEquals("\"a\"\"b\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_ContainsLFindCharInside() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\nb");
        assertEquals("\"a\nb\"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_EndsWithSpace() throws IOException {
        // value ending with space (<=SP) triggers quote
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("abc ");
        assertEquals("\"abc \"", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_EndsWithNonSpaceNoQuote() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("abc");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testPrintAndQuoteMInIMAL_NewRecordFirstCharSpecialRangeAboveZ() throws IOException {
        // first char > 'z' (122) e.g., '{' is 123, > 'z' -> triggers quote
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("{abc");
        assertEquals("\"{abc\"", sb.toString());
    }

    // ========== printAndEscape tests ==========

    @Test
    public void testPrintAndEscapeNoSpecialChars() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("hello");
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testPrintAndEscapeWithDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a,b");
        // delimiter escaped
        assertEquals("a!,b", sb.toString());
    }

    @Test
    public void testPrintAndEscapeWithEscape() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a!b");
        // escape escaped
        assertEquals("a!!b", sb.toString());
    }

    @Test
    public void testPrintAndEscapeWithCR() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a\rb");
        assertEquals("a!\rb", sb.toString());
    }

    @Test
    public void testPrintAndEscapeWithLF() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a\nb");
        assertEquals("a!\nb", sb.toString());
    }

    @Test
    public void testPrintAndEscapeMultipleSpecial() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a,b\nc!d");
        assertEquals("a!,b!\nc!!d", sb.toString());
    }

    // ========== printComment tests ==========

    @Test
    public void testPrintCommentDisabled() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart(null); // disabling comments
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("test");
        assertEquals("", sb.toString());
    }

    @Test
    public void testPrintCommentEnabledNewRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("hello");
        assertEquals("# hello" + format.getRecordSeparator(), sb.toString());
    }

    @Test
    public void testPrintCommentAfterValues() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a");
        printer.printComment("cmt"); // newRecord false -> println first
        // println adds record separator, then comment line, then println again
        String recSep = format.getRecordSeparator();
        String expected = "a" + recSep + "# cmt" + recSep;
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintCommentWithCRLF() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("line1\r\line2");
        String recSep = format.getRecordSeparator();
        // CRLF treated as single newline
        String expected = "# line1" + recSep + "# line2" + recSep;
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintCommentWithCR() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("line1\rline2");
        String recSep = format.getRecordSeparator();
        String expected = "# line1" + recSep + "# line2" + recSep;
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintCommentWithLF() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("line1\nline2");
        String recSep = format.getRecordSeparator();
        String expected = "# line1" + recSep + "# line2" + recSep;
        assertEquals(expected, sb.toString());
    }

    // ========== println tests ==========

    @Test
    public void testPrintlnWithNullRecordSeparator() throws IOException {
        // record separator null -> println does not append anything but sets newRecord true
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.println();
        assertEquals("", sb.toString());
        // verify newRecord is true by printing another value
        printer.print("a");
        // should not have delimiter since newRecord true
        assertEquals("a", sb.toString());
    }

    @Test
    public void testPrintlnWithRecordSeparator() throws IOException {
        StringBuilder sb = new StringBuilder();
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.println();
        assertEquals(recSep, sb.toString());
        // newRecord true, next print no delimiter
        printer.print("a");
        assertEquals(recSep + "a", sb.toString());
    }

    // ========== printRecord tests ==========

    @Test
    public void testPrintRecordIterable() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecord(java.util.Arrays.asList("a", "b", "c"));
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        assertEquals("a,b,c" + recSep, sb.toString());
    }

    @Test
    public void testPrintRecordArray() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecord("x", "y");
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        assertEquals("x,y" + recSep, sb.toString());
    }

    @Test
    public void testPrintRecordsIterable() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        java.util.List<Object> data = new java.util.ArrayList<>();
        data.add("a");
        data.add(new Object[]{"b", "c"});
        data.add(java.util.Arrays.asList("d", "e"));
        printer.printRecords(data);
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        // "a" becomes single value record, array becomes two-value record, list becomes two-value record
        assertEquals("a" + recSep + "b,c" + recSep + "d,e" + recSep, sb.toString());
    }

    @Test
    public void testPrintRecordsArray() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        Object[] data = new Object[]{"a", new Object[]{"b", "c"}, java.util.Arrays.asList("d", "e")};
        printer.printRecords(data);
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        assertEquals("a" + recSep + "b,c" + recSep + "d,e" + recSep, sb.toString());
    }

    @Test
    public void testPrintRecordsResultSet() throws Exception {
        // Setup mock ResultSet
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(meta);
        when(meta.getColumnCount()).thenReturn(2);
        when(rs.next()).thenReturn(true, true, false); // two rows
        when(rs.getString(1)).thenReturn("a", "d");
        when(rs.getString(2)).thenReturn("b", "e");

        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecords(rs);
        String recSep = CSVFormat.DEFAULT.getRecordSeparator();
        assertEquals("a,b" + recSep + "d,e" + recSep, sb.toString());
        verify(rs, times(3)).next(); // next called three times (two true, then false)
    }

    @Test
    public void testGetOut() throws IOException {
        Appendable sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        assertSame(sb, printer.getOut());
    }
}
