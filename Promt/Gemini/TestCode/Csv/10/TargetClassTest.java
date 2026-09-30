package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayWriter;
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

public class CSVPrinterTest {

    @Test
    public void testConstructorValid() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        Assert.assertSame(sw, printer.getOut());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOut() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test
    public void testCloseAndFlush() throws IOException {
        final boolean[] flags = new boolean[2];
        Appendable customOut = new Appendable() {
            public Appendable append(CharSequence csq) { return this; }
            public Appendable append(CharSequence csq, int start, int end) { return this; }
            public Appendable append(char c) { return this; }
        };

        // Non flushable, non closeable
        CSVPrinter printer1 = new CSVPrinter(customOut, CSVFormat.DEFAULT);
        printer1.flush();
        printer1.close();

        // Flushable and closeable Appendable
        class FlushCloseWriter extends StringWriter {
            @Override
            public void flush() {
                flags[0] = true;
                super.flush();
            }
            @Override
            public void close() throws IOException {
                flags[1] = true;
                super.close();
            }
        }

        FlushCloseWriter fcw = new FlushCloseWriter();
        CSVPrinter printer2 = new CSVPrinter(fcw, CSVFormat.DEFAULT);
        printer2.flush();
        Assert.assertTrue(flags[0]);
        printer2.close();
        Assert.assertTrue(flags[1]);
    }

    @Test
    public void testPrintNullValueDefaultFormat() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print(null);
        printer.print("a");
        Assert.assertEquals("\"\",a", sw.toString());
    }

    @Test
    public void testPrintNullValueCustomNullString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print(null);
        Assert.assertEquals("NULL", sw.toString());
    }

    @Test
    public void testPrintMinimalQuotePolicy() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        // First item starting with non-alphanumeric (like '+')
        printer.print("+123");
        // Second item standard text
        printer.print("hello");
        // Value containing delimiter
        printer.print("a,b");
        // Value containing quote
        printer.print("a\"b");
        // Value containing CR / LF
        printer.print("a\nb");
        printer.print("a\rb");
        // Value ending in whitespace (<= SP)
        printer.print("word ");
        // Value starting with char <= COMMENT ('#')
        printer.println();
        printer.print("!exclamation");
        // Numeric start
        printer.print("9numbers");
        // Alphanumeric start, clean middle and end
        printer.print("Alpha");

        String expected = "\"+123\",hello,\"a,b\",\"a\"\"b\",\"a\nb\",\"a\rb\",\"word \"\r\n\"!exclamation\",9numbers,Alpha";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintQuotePolicyAll() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL).withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("test");
        printer.print(123);
        printer.print("a\"b");
        Assert.assertEquals("\"test\",\"123\",\"a\"\"b\"", sw.toString());
    }

    @Test
    public void testPrintQuotePolicyNonNumeric() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC).withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("text");
        printer.print(123);
        printer.print(45.67);
        printer.print(null);
        Assert.assertEquals("\"text\",123,45.67,\"\"", sw.toString());
    }

    @Test
    public void testPrintQuotePolicyNoneWithEscape() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("hello,world\nline2\rline3\\line4");
        Assert.assertEquals("hello\\,world\\nline2\\rline3\\\\line4", sw.toString());
    }

    @Test
    public void testPrintEscapeModeWithoutQuote() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withRecordSeparator("\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("abc");
        printer.print("a,b");
        printer.print("a\\b");
        printer.print("a\nb");
        printer.print("a\rb");
        Assert.assertEquals("abc,a\\,b,a\\\\b,a\\nb,a\\rb", sw.toString());
    }

    @Test
    public void testPrintNoQuoteNoEscape() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.newFormat('|');
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("foo");
        printer.print("bar");
        Assert.assertEquals("foo|bar", sw.toString());
    }

    @Test
    public void testPrintComments() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#').withRecordSeparator("\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        // Disabled comment if format doesn't have comment start
        CSVPrinter noCommentPrinter = new CSVPrinter(new StringWriter(), CSVFormat.DEFAULT);
        noCommentPrinter.printComment("This should not be printed");

        printer.printComment("This is a simple comment");
        printer.print("data");
        printer.printComment("Comment after data\r\nSecond line\rThird line\nFourth line");

        String expected = "# This is a simple comment\n" +
                "data\n" +
                "# Comment after data\n" +
                "# Second line\n" +
                "# Third line\n" +
                "# Fourth line\n";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintlnWithNullSeparator() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("a");
        printer.println();
        printer.print("b");
        Assert.assertEquals("ab", sw.toString());
    }

    @Test
    public void testPrintRecordVarargs() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));

        printer.printRecord("v1", "v2", 3, true);
        Assert.assertEquals("v1,v2,3,true\n", sw.toString());
    }

    @Test
    public void testPrintRecordIterable() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));

        List<Object> values = Arrays.asList("x", "y", "z");
        printer.printRecord(values);
        Assert.assertEquals("x,y,z\n", sw.toString());
    }

    @Test
    public void testPrintRecordsArray() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));

        Object[] records = new Object[] {
                new Object[] { "r1c1", "r1c2" },
                Arrays.asList("r2c1", "r2c2"),
                "r3c1"
        };

        printer.printRecords(records);
        String expected = "r1c1,r1c2\n" +
                "r2c1,r2c2\n" +
                "r3c1\n";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintRecordsIterable() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));

        List<Object> records = new ArrayList<Object>();
        records.add(new Object[] { "1", "2" });
        records.add(Arrays.asList("3", "4"));
        records.add("5");

        printer.printRecords(records);
        String expected = "1,2\n" +
                "3,4\n" +
                "5\n";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintRecordsResultSet() throws SQLException, IOException {
        final List<List<String>> data = Arrays.asList(
                Arrays.asList("1", "Alice"),
                Arrays.asList("2", "Bob")
        );

        final int[] rowIndex = new int[] { -1 };

        ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
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

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                CSVPrinterTest.class.getClassLoader(),
                new Class<?>[] { ResultSet.class },
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getMetaData".equals(name)) {
                            return metaData;
                        }
                        if ("next".equals(name)) {
                            rowIndex[0]++;
                            return rowIndex[0] < data.size();
                        }
                        if ("getString".equals(name)) {
                            int colIndex = ((Integer) args[0]) - 1;
                            return data.get(rowIndex[0]).get(colIndex);
                        }
                        return null;
                    }
                }
        );

        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));
        printer.printRecords(resultSet);

        String expected = "1,Alice\n2,Bob\n";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintMinimalQuoteSpecialBoundaries() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\n"));

        // Single empty string at newRecord
        printer.print("");
        printer.println();

        // Empty string not at newRecord
        printer.print("first");
        printer.print("");
        printer.println();

        // Starts with char greater than '9' and less than 'A' (e.g., ':')
        printer.print(":colon");
        printer.println();

        // Starts with char greater than 'Z' and less than 'a' (e.g., '[')
        printer.print("[bracket");
        printer.println();

        // Starts with char greater than 'z' (e.g., '{')
        printer.print("{brace");
        printer.println();

        // Starts with normal uppercase and lowercase letters
        printer.print("Normal");
        printer.print("lower");
        printer.println();

        // Word containing only normal chars, without quoting needed
        printer.print("CleanWord");
        printer.print("AnotherCleanWord");

        String expected = "\"\"\n" +
                "first,\n" +
                "\":colon\"\n" +
                "\"[bracket\"\n" +
                "\"{brace\"\n" +
                "Normal,lower\n" +
                "CleanWord,AnotherCleanWord";
        Assert.assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintEscapeWithNoSpecialChars() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.newFormat(',').withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("simpletext");
        Assert.assertEquals("simpletext", sw.toString());
    }

    @Test
    public void testEmptyRecordsPrint() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.printRecords(Collections.emptyList());
        printer.printRecords(new Object[0]);
        Assert.assertEquals("", sw.toString());
    }
}
