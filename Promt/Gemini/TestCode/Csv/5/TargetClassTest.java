package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link CSVPrinter}.
 */
public class CSVPrinterTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOut() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test
    public void testGetOut() {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());
    }

    @Test
    public void testCloseAndFlushWithCloseableAndFlushable() throws IOException {
        final boolean[] closed = {false};
        final boolean[] flushed = {false};
        final Appendable out = new Appendable() {
            public Appendable append(CharSequence csq) { return this; }
            public Appendable append(CharSequence csq, int start, int end) { return this; }
            public Appendable append(char c) { return this; }
        };
        class TestAppendable implements Appendable, Closeable, Flushable {
            public Appendable append(CharSequence csq) { return this; }
            public Appendable append(CharSequence csq, int start, int end) { return this; }
            public Appendable append(char c) { return this; }
            public void close() { closed[0] = true; }
            public void flush() { flushed[0] = true; }
        }
        final TestAppendable testOut = new TestAppendable();
        final CSVPrinter printer = new CSVPrinter(testOut, CSVFormat.DEFAULT);
        printer.flush();
        assertTrue(flushed[0]);
        printer.close();
        assertTrue(closed[0]);
    }

    @Test
    public void testCloseAndFlushWithNonCloseable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        // StringBuilder is not Closeable or Flushable; should not throw exceptions
        printer.flush();
        printer.close();
        assertEquals("", sb.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println();
        assertEquals("\r\n", sw.toString());
    }

    @Test
    public void testPrintNullValueDefault() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print(null);
        printer.print("a");
        assertEquals(",a", sw.toString());
    }

    @Test
    public void testPrintNullValueCustomNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print(null);
        printer.print("b");
        assertEquals("NULL,b", sw.toString());
    }

    @Test
    public void testPrintRecordVarargs() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b", "c");
        assertEquals("a,b,c\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord(Arrays.asList("1", "2", "3"));
        assertEquals("1,2,3\r\n", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotingFirstCharConditions() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;

        // c < '0' e.g. '/'
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("/abc");
        assertEquals("\"/abc\"", sw.toString());

        // '0' <= c <= '9' -> not quoted if normal
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("1abc");
        assertEquals("1abc", sw.toString());

        // c > '9' && c < 'A' e.g. '@'
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("@abc");
        assertEquals("\"@abc\"", sw.toString());

        // 'A' <= c <= 'Z' -> not quoted
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("Abc");
        assertEquals("Abc", sw.toString());

        // c > 'Z' && c < 'a' e.g. '['
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("[abc");
        assertEquals("\"[abc\"", sw.toString());

        // 'a' <= c <= 'z' -> not quoted
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("abc");
        assertEquals("abc", sw.toString());

        // c > 'z' e.g. '{'
        sw = new StringWriter();
        printer = new CSVPrinter(sw, format);
        printer.print("{abc");
        assertEquals("\"{abc\"", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotingSubsequentTokenSpecialStartChar() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("abc");
        // Second token starts with '#' (COMMENT char)
        printer.print("#def");
        // Third token starts with '!' (c <= COMMENT)
        printer.print("!ghi");
        assertEquals("abc,\"#def\",\"!ghi\"", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotingDelimitersAndQuotes() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        // Contains delimiter
        printer.print("a,b");
        // Contains quote
        printer.print("a\"b");
        // Contains CR
        printer.print("a\rb");
        // Contains LF
        printer.print("a\nb");
        assertEquals("\"a,b\",\"a\"\"b\",\"a\rb\",\"a\nb\"", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotingTrailingSpace() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("abc");
        // Second token has trailing space (c <= SP)
        printer.print("xyz ");
        assertEquals("abc,\"xyz \"", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotingEmptyTokens() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        // Empty token as first token on line -> quoted
        printer.print("");
        // Empty token as second token on line -> not quoted
        printer.print("");
        assertEquals("\"\",", sw.toString());
    }

    @Test
    public void testQuotePolicyAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("hello");
        printer.print(Integer.valueOf(123));
        printer.print("");
        assertEquals("\"hello\",\"123\",\"\"", sw.toString());
    }

    @Test
    public void testQuotePolicyNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("text");
        printer.print(Integer.valueOf(456));
        printer.print(Double.valueOf(78.9));
        printer.print("more text");
        assertEquals("\"text\",456,78.9,\"more text\"", sw.toString());
    }

    @Test
    public void testQuotePolicyNoneWithEscape() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
        printer.print("line1\r\nline2");
        printer.print("c\\d");
        assertEquals("a\\,b,line1\\r\\nline2,c\\\\d", sw.toString());
    }

    @Test
    public void testEscapingWithoutQuoting() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withRecordSeparator("\r\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("hello,world");
        printer.print("slash\\here");
        printer.print("cr\rand\nlf");
        printer.print("trailing,");
        printer.println();
        assertEquals("hello\\,world,slash\\\\here,cr\\rand\\nlf,trailing\\,\r\n", sw.toString());
    }

    @Test
    public void testPlainFormatWithoutQuoteOrEscape() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat('|').withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("raw");
        printer.print("data");
        printer.println();
        assertEquals("raw|data\n", sw.toString());
    }

    @Test
    public void testPrintCommentDisabled() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT; // commenting disabled by default
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("This should not be printed");
        assertEquals("", sw.toString());
    }

    @Test
    public void testPrintCommentOnNewRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("This is a comment");
        assertEquals("# This is a comment\r\n", sw.toString());
    }

    @Test
    public void testPrintCommentOnExistingRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("value");
        printer.printComment("Comment after value");
        assertEquals("value\r\n# Comment after value\r\n", sw.toString());
    }

    @Test
    public void testPrintCommentMultiLineCRLF() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("Line1\r\nLine2\rLine3\nLine4");
        assertEquals("# Line1\r\n# Line2\r\n# Line3\r\n# Line4\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsObjectArray() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final Object[] records = new Object[] {
            new Object[] {"r1c1", "r1c2"},
            Arrays.asList("r2c1", "r2c2"),
            "singleValue"
        };
        printer.printRecords(records);
        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\nsingleValue\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<Object> records = new ArrayList<Object>();
        records.add(new Object[] {"a1", "b1"});
        records.add(Arrays.asList("a2", "b2"));
        records.add("single");
        printer.printRecords(records);
        assertEquals("a1,b1\r\na2,b2\r\nsingle\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final ResultSetMetaData meta = (ResultSetMetaData) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSetMetaData.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("getColumnCount".equals(method.getName())) {
                        return 2;
                    }
                    return null;
                }
            }
        );

        final int[] row = {0};
        final String[][] data = {{"val1", "val2"}, {"val3", "val4"}};
        final ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSet.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    final String name = method.getName();
                    if ("getMetaData".equals(name)) {
                        return meta;
                    } else if ("next".equals(name)) {
                        row[0]++;
                        return row[0] <= data.length;
                    } else if ("getString".equals(name)) {
                        int col = (Integer) args[0];
                        return data[row[0] - 1][col - 1];
                    }
                    return null;
                }
            }
        );

        printer.printRecords(resultSet);
        assertEquals("val1,val2\r\nval3,val4\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsEmptyResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final ResultSetMetaData meta = (ResultSetMetaData) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSetMetaData.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("getColumnCount".equals(method.getName())) {
                        return 2;
                    }
                    return null;
                }
            }
        );

        final ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSet.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    final String name = method.getName();
                    if ("getMetaData".equals(name)) {
                        return meta;
                    } else if ("next".equals(name)) {
                        return false;
                    }
                    return null;
                }
            }
        );

        printer.printRecords(resultSet);
        assertEquals("", sw.toString());
    }

    @Test
    public void testMultipleQuotesInsideToken() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("a\"\"b");
        assertEquals("\"a\"\"\"\"b\"", sw.toString());
    }
}
