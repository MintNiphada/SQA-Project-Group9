package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CSVPrinterTest {

    private static final String RECORD_SEPARATOR = "\n";
    private static final String DELIMITER = ",";
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final char COMMENT_START = '#';

    private StringBuilder out;
    private CSVPrinter printer;

    private void initPrinter(CSVFormat format) throws IOException {
        out = new StringBuilder();
        printer = new CSVPrinter(out, format);
    }

    private CSVFormat baseFormat() {
        return CSVFormat.DEFAULT.withRecordSeparator(RECORD_SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOut() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() {
        new CSVPrinter(new StringBuilder(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidFormat() {
        // delimiter same as quote char should fail validation
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(QUOTE).withQuote(QUOTE);
        new CSVPrinter(new StringBuilder(), format);
    }

    @Test
    public void testFlushWhenNotFlushable() throws IOException {
        initPrinter(baseFormat());
        printer.flush();
        // no exception, nothing to assert
    }

    @Test
    public void testFlushWhenFlushable() throws IOException {
        final boolean[] flushed = {false};
        Appendable flushable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException { return this; }
            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException { return this; }
            @Override
            public Appendable append(char c) throws IOException { return this; }
            public void flush() throws IOException { flushed[0] = true; }
        };
        CSVPrinter p = new CSVPrinter(flushable, CSVFormat.DEFAULT);
        p.flush();
        assertTrue(flushed[0]);
    }

    @Test
    public void testCloseWhenNotCloseable() throws IOException {
        initPrinter(baseFormat());
        printer.close();
        // no exception
    }

    @Test
    public void testCloseWhenCloseable() throws IOException {
        final boolean[] closed = {false};
        Appendable closeable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException { return this; }
            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException { return this; }
            @Override
            public Appendable append(char c) throws IOException { return this; }
            public void close() throws IOException { closed[0] = true; }
        };
        CSVPrinter p = new CSVPrinter(closeable, CSVFormat.DEFAULT);
        p.close();
        assertTrue(closed[0]);
    }

    @Test
    public void testPrintNullValueWithNullString() throws IOException {
        CSVFormat format = baseFormat().withNullString(null);
        initPrinter(format);
        printer.print(null);
        assertEquals("", out.toString());
    }

    @Test
    public void testPrintNullValueWithCustomNullString() throws IOException {
        CSVFormat format = baseFormat().withNullString("NULL");
        initPrinter(format);
        printer.print(null);
        assertEquals("NULL", out.toString());
    }

    @Test
    public void testPrintObject() throws IOException {
        initPrinter(baseFormat());
        printer.print("Hello");
        assertEquals("Hello", out.toString());
    }

    @Test
    public void testPrintRecordWithValues() throws IOException {
        initPrinter(baseFormat());
        printer.printRecord("a", "b", "c");
        assertEquals("a,b,c" + RECORD_SEPARATOR, out.toString());
    }

    @Test
    public void testPrintRecordEmpty() throws IOException {
        initPrinter(baseFormat());
        printer.printRecord();
        assertEquals(RECORD_SEPARATOR, out.toString());
    }

    @Test
    public void testPrintRecordIterable() throws IOException {
        initPrinter(baseFormat());
        List<String> values = Arrays.asList("x", "y", "z");
        printer.printRecord(values);
        assertEquals("x,y,z" + RECORD_SEPARATOR, out.toString());
    }

    @Test
    public void testPrintRecordsObjectArray() throws IOException {
        initPrinter(baseFormat());
        Object[] values = new Object[] {"a", "b", new Object[] {"c", "d"}, Arrays.asList("e", "f"), "g"};
        printer.printRecords(values);
        String expected = "a" + RECORD_SEPARATOR +
                         "b" + RECORD_SEPARATOR +
                         "c,d" + RECORD_SEPARATOR +
                         "e,f" + RECORD_SEPARATOR +
                         "g" + RECORD_SEPARATOR;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintRecordsIterable() throws IOException {
        initPrinter(baseFormat());
        List<Object> values = new ArrayList<>();
        values.add("a");
        values.add(new Object[] {"b", "c"});
        values.add(Arrays.asList("d", "e"));
        values.add("f");
        printer.printRecords(values);
        String expected = "a" + RECORD_SEPARATOR +
                         "b,c" + RECORD_SEPARATOR +
                         "d,e" + RECORD_SEPARATOR +
                         "f" + RECORD_SEPARATOR;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintRecordsResultSet() throws Exception {
        initPrinter(baseFormat());
        ResultSet rs = createMockResultSet();
        printer.printRecords(rs);
        String expected = "1,John" + RECORD_SEPARATOR +
                         "2,Jane" + RECORD_SEPARATOR;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintCommentDisabled() throws IOException {
        CSVFormat format = baseFormat().withCommentMarker(null); // disables commenting
        initPrinter(format);
        printer.printComment("test");
        assertEquals("", out.toString());
    }

    @Test
    public void testPrintCommentEnabled() throws IOException {
        CSVFormat format = baseFormat().withCommentMarker(COMMENT_START);
        initPrinter(format);
        printer.printComment("hello world");
        assertEquals(COMMENT_START + " hello world" + RECORD_SEPARATOR, out.toString());
    }

    @Test
    public void testPrintCommentWithLineBreaks() throws IOException {
        CSVFormat format = baseFormat().withCommentMarker(COMMENT_START);
        initPrinter(format);
        printer.printComment("line1\nline2\rline3\r\nline4");
        String expected = COMMENT_START + " line1" + RECORD_SEPARATOR +
                         COMMENT_START + " line2" + RECORD_SEPARATOR +
                         COMMENT_START + " line3" + RECORD_SEPARATOR +
                         COMMENT_START + " line4" + RECORD_SEPARATOR;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintCommentAfterRecord() throws IOException {
        CSVFormat format = baseFormat().withCommentMarker(COMMENT_START);
        initPrinter(format);
        printer.printRecord("a");
        printer.printComment("comment");
        String expected = "a" + RECORD_SEPARATOR +
                         COMMENT_START + " comment" + RECORD_SEPARATOR;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        initPrinter(baseFormat());
        printer.println();
        assertEquals(RECORD_SEPARATOR, out.toString());
    }

    @Test
    public void testGetOut() throws IOException {
        initPrinter(baseFormat());
        assertSame(out, printer.getOut());
    }

    // Quoting tests

    @Test
    public void testQuotingAll() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.ALL);
        initPrinter(format);
        printer.print("abc");
        assertEquals("\"abc\"", out.toString());
    }

    @Test
    public void testQuotingNonNullNumeric() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.NON_NUMERIC);
        initPrinter(format);
        printer.print(123);
        assertEquals("123", out.toString());
        printer.println();
        printer.print("abc");
        assertEquals("\"abc\"", out.toString());
    }

    @Test
    public void testQuotingNone() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.NONE).withEscape(ESCAPE);
        initPrinter(format);
        printer.print("a,b");
        assertEquals("a\\,b", out.toString());
    }

    @Test
    public void testMinimalQuotingEmptyTokenAtStart() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("");
        assertEquals("\"\"", out.toString());
    }

    @Test
    public void testMinimalQuotingEmptyTokenNotAtStart() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("a");
        printer.print("");
        assertEquals("a,", out.toString()); // second empty is not quoted because not newRecord
    }

    @Test
    public void testMinimalQuotingLeadingSpecialChar() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("#start");
        assertEquals("\"#start\"", out.toString());
    }

    @Test
    public void testMinimalQuotingTrailingSpace() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("end ");
        assertEquals("\"end \"", out.toString());
    }

    @Test
    public void testMinimalQuotingWithDelimiter() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("a,b");
        assertEquals("\"a,b\"", out.toString());
    }

    @Test
    public void testMinimalQuotingWithQuoteChar() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("a\"b");
        assertEquals("\"a\"\"b\"", out.toString());
    }

    @Test
    public void testMinimalQuotingWithNewline() throws IOException {
        CSVFormat format = baseFormat().withQuote(QUOTE).withQuotePolicy(Quote.MINIMAL);
        initPrinter(format);
        printer.print("a\nb");
        assertEquals("\"a\nb\"", out.toString());
    }

    @Test
    public void testEscaping() throws IOException {
        CSVFormat format = baseFormat().withEscape(ESCAPE).withQuote(null); // no quoting, only escaping
        initPrinter(format);
        printer.print("a,b");
        assertEquals("a\\,b", out.toString());
    }

    @Test
    public void testEscapingLFandCR() throws IOException {
        CSVFormat format = baseFormat().withEscape(ESCAPE).withQuote(null);
        initPrinter(format);
        printer.print("line1\nline2\rline3");
        assertEquals("line1\\nline2\\rline3", out.toString());
    }

    @Test
    public void testEscapingEscapeCharItself() throws IOException {
        CSVFormat format = baseFormat().withEscape(ESCAPE).withQuote(null);
        initPrinter(format);
        printer.print("back\\slash");
        assertEquals("back\\\\slash", out.toString());
    }

    @Test
    public void testEscapingDelimiterAtStart() throws IOException {
        CSVFormat format = baseFormat().withEscape(ESCAPE).withQuote(null);
        initPrinter(format);
        printer.print(",abc");
        assertEquals("\\,abc", out.toString());
    }

    @Test
    public void testEscapingAllSpecial() throws IOException {
        CSVFormat format = baseFormat().withEscape(ESCAPE).withQuote(null);
        initPrinter(format);
        printer.print("a,b\nc\rd\\e");
        assertEquals("a\\,b\\nc\\rd\\\\e", out.toString());
    }

    private ResultSet createMockResultSet() throws SQLException {
        // Use dynamic proxy to create a minimal ResultSet
        InvocationHandler handler = new InvocationHandler() {
            private int row = -1;
            private final String[][] data = {
                {"1", "John"},
                {"2", "Jane"}
            };

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                switch (methodName) {
                    case "getMetaData":
                        return createMockMetaData();
                    case "next":
                        row++;
                        return row < data.length;
                    case "getString":
                        int columnIndex = (Integer) args[0];
                        return data[row][columnIndex - 1];
                    default:
                        // For any other method, return default value based on return type
                        Class<?> returnType = method.getReturnType();
                        if (returnType.equals(boolean.class)) return false;
                        if (returnType.equals(int.class)) return 0;
                        if (returnType.equals(long.class)) return 0L;
                        if (returnType.equals(double.class)) return 0.0;
                        if (returnType.equals(float.class)) return 0.0f;
                        if (returnType.equals(short.class)) return (short)0;
                        if (returnType.equals(byte.class)) return (byte)0;
                        if (returnType.equals(char.class)) return (char)0;
                        return null;
                }
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[] { ResultSet.class },
                handler);
    }

    private ResultSetMetaData createMockMetaData() throws SQLException {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("getColumnCount".equals(method.getName())) {
                    return 2;
                }
                return null;
            }
        };
        return (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[] { ResultSetMetaData.class },
                handler);
    }
}
