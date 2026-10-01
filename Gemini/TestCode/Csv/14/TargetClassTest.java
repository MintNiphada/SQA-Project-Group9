package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CSVFormatTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private enum TestEnum {
        FIRST, SECOND, THIRD
    }

    @Test
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD_CSV);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.TDF);

        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD, CSVFormat.valueOf("InformixUnload"));
        assertEquals(CSVFormat.INFORMIX_UNLOAD_CSV, CSVFormat.valueOf("InformixUnloadCsv"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MySQL"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));

        for (final CSVFormat.Predefined predefined : CSVFormat.Predefined.values()) {
            assertNotNull(predefined.getFormat());
        }
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
        assertNull(format.getHeaderComments());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
        assertFalse(format.getTrim());
        assertFalse(format.getTrailingDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatInvalidDelimiterCR() {
        CSVFormat.newFormat('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatInvalidDelimiterLF() {
        CSVFormat.newFormat('\n');
    }

    @Test
    public void testWithDelimiter() {
        final CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');
        assertEquals('|', format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterCR() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLF() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test
    public void testWithQuote() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertTrue(format.isQuoteCharacterSet());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withQuote((Character) null);
        assertNull(formatNull.getQuoteCharacter());
        assertFalse(formatNull.isQuoteCharacterSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCR() {
        CSVFormat.DEFAULT.withQuote('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLF() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf('\n'));
    }

    @Test
    public void testWithQuoteMode() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    @Test
    public void testWithCommentMarker() {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withCommentMarker((Character) null);
        assertNull(formatNull.getCommentMarker());
        assertFalse(formatNull.isCommentMarkerSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerCR() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLF() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf('\n'));
    }

    @Test
    public void testWithEscape() {
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withEscape((Character) null);
        assertNull(formatNull.getEscapeCharacter());
        assertFalse(formatNull.isEscapeCharacterSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCR() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLF() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\n'));
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        final CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces();
        assertTrue(format.getIgnoreSurroundingSpaces());

        final CSVFormat formatFalse = format.withIgnoreSurroundingSpaces(false);
        assertFalse(formatFalse.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        final CSVFormat format = CSVFormat.RFC4180.withIgnoreEmptyLines();
        assertTrue(format.getIgnoreEmptyLines());

        final CSVFormat formatFalse = format.withIgnoreEmptyLines(false);
        assertFalse(formatFalse.getIgnoreEmptyLines());
    }

    @Test
    public void testWithRecordSeparator() {
        final CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", formatChar.getRecordSeparator());

        final CSVFormat formatString = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertEquals("\r\n", formatString.getRecordSeparator());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        assertNull(formatNull.getRecordSeparator());
    }

    @Test
    public void testWithNullString() {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());

        final CSVFormat formatNull = format.withNullString(null);
        assertNull(formatNull.getNullString());
        assertFalse(formatNull.isNullStringSet());
    }

    @Test
    public void testWithHeaderComments() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", null, 123);
        assertArrayEquals(new String[] { "Comment1", null, "123" }, format.getHeaderComments());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(formatNull.getHeaderComments());
    }

    @Test
    public void testWithHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        assertArrayEquals(new String[] { "A", "B", "C" }, format.getHeader());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(formatNull.getHeader());
    }

    @Test
    public void testWithHeaderEnum() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestEnum.class);
        assertArrayEquals(new String[] { "FIRST", "SECOND", "THIRD" }, format.getHeader());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(formatNull.getHeader());
    }

    @Test
    public void testWithHeaderResultSet() throws SQLException {
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { ResultSetMetaData.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if ("getColumnCount".equals(method.getName())) {
                            return 2;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            final int col = (Integer) args[0];
                            return "Col" + col;
                        }
                        return null;
                    }
                });

        final ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { ResultSet.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if ("getMetaData".equals(method.getName())) {
                            return metaData;
                        }
                        return null;
                    }
                });

        final CSVFormat formatRS = CSVFormat.DEFAULT.withHeader(resultSet);
        assertArrayEquals(new String[] { "Col1", "Col2" }, formatRS.getHeader());

        final CSVFormat formatMD = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[] { "Col1", "Col2" }, formatMD.getHeader());

        final CSVFormat formatNullRS = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(formatNullRS.getHeader());

        final CSVFormat formatNullMD = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(formatNullMD.getHeader());
    }

    @Test
    public void testWithSkipHeaderRecord() {
        final CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertTrue(format.getSkipHeaderRecord());

        final CSVFormat formatFalse = format.withSkipHeaderRecord(false);
        assertFalse(formatFalse.getSkipHeaderRecord());
    }

    @Test
    public void testWithFirstRecordAsHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(format.getSkipHeaderRecord());
        assertArrayEquals(new String[0], format.getHeader());
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        final CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        assertTrue(format.getAllowMissingColumnNames());

        final CSVFormat formatFalse = format.withAllowMissingColumnNames(false);
        assertFalse(formatFalse.getAllowMissingColumnNames());
    }

    @Test
    public void testWithIgnoreHeaderCase() {
        final CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase();
        assertTrue(format.getIgnoreHeaderCase());

        final CSVFormat formatFalse = format.withIgnoreHeaderCase(false);
        assertFalse(formatFalse.getIgnoreHeaderCase());
    }

    @Test
    public void testWithTrim() {
        final CSVFormat format = CSVFormat.DEFAULT.withTrim();
        assertTrue(format.getTrim());

        final CSVFormat formatFalse = format.withTrim(false);
        assertFalse(formatFalse.getTrim());
    }

    @Test
    public void testWithTrailingDelimiter() {
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        assertTrue(format.getTrailingDelimiter());

        final CSVFormat formatFalse = format.withTrailingDelimiter(false);
        assertFalse(formatFalse.getTrailingDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteEqualsDelimiter() {
        CSVFormat.newFormat(',').withQuote(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeEqualsDelimiter() {
        CSVFormat.newFormat(',').withEscape(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCommentMarkerEqualsDelimiter() {
        CSVFormat.newFormat(',').withCommentMarker(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('"');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeEqualsCommentMarker() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteModeNoneWithoutEscape() {
        CSVFormat.DEFAULT.withQuote(null).withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDuplicateHeader() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test
    public void testEqualsAndHashCode() {
        final CSVFormat f1 = CSVFormat.DEFAULT;
        final CSVFormat f2 = CSVFormat.DEFAULT;

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, null);
        assertNotEquals(f1, "otherType");

        assertNotEquals(f1, f1.withDelimiter(';'));
        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1, f1.withQuote((Character) null));
        assertNotEquals(f1.withQuote((Character) null), f1);

        assertNotEquals(f1, f1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1.withCommentMarker('#'), f1);
        assertNotEquals(f1.withCommentMarker('#'), f1.withCommentMarker('!'));

        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1.withEscape('\\'), f1);
        assertNotEquals(f1.withEscape('\\'), f1.withEscape('/'));

        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1.withNullString("NULL"), f1);
        assertNotEquals(f1.withNullString("NULL"), f1.withNullString("N/A"));

        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(false));
        assertNotEquals(f1, f1.withSkipHeaderRecord(true));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1.withRecordSeparator((String) null), f1);
        assertNotEquals(f1, f1.withRecordSeparator((String) null));

        assertNotEquals(f1, f1.withHeader("A", "B"));
        assertNotEquals(f1.withHeader("A", "B"), f1.withHeader("A", "C"));

        // Check hashCode with null fields
        final CSVFormat nullsFormat = CSVFormat.newFormat(';')
                .withQuote((Character) null)
                .withCommentMarker((Character) null)
                .withEscape((Character) null)
                .withNullString(null)
                .withRecordSeparator((String) null);
        assertNotNull(nullsFormat.hashCode());

        final CSVFormat fullFormat = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withQuoteMode(QuoteMode.MINIMAL)
                .withHeader("X")
                .withIgnoreHeaderCase(true)
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withSkipHeaderRecord(true);
        assertNotNull(fullFormat.hashCode());
    }

    @Test
    public void testToString() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withIgnoreSurroundingSpaces()
                .withIgnoreHeaderCase()
                .withHeaderComments("Comment")
                .withHeader("Col1", "Col2");

        final String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("IgnoreHeaderCase:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
        assertTrue(str.contains("HeaderComments:[Comment]"));
        assertTrue(str.contains("Header:[Col1, Col2]"));
    }

    @Test
    public void testFormat() {
        final String res = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", res);
    }

    @Test
    public void testPrintAndFormatModes() throws IOException {
        final StringBuilder sb = new StringBuilder();

        // QuoteMode.ALL
        final CSVFormat allFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals("\"1\",\"abc\"", allFormat.format(1, "abc"));

        // QuoteMode.NON_NUMERIC
        final CSVFormat nonNumericFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        assertEquals("1,\"abc\"", nonNumericFormat.format(1, "abc"));

        // QuoteMode.NONE
        final CSVFormat noneFormat = CSVFormat.DEFAULT.withQuote(null).withQuoteMode(QuoteMode.NONE).withEscape('\\');
        assertEquals("a,b\\,c,d\\ne\\rf\\\\g", noneFormat.format("a", "b,c", "d\ne\rf\\g"));

        // QuoteMode.MINIMAL with special characters
        final CSVFormat minimalFormat = CSVFormat.DEFAULT;
        assertEquals("\"\",b", minimalFormat.format("", "b"));
        assertEquals("b,\"\"", minimalFormat.format("b", ""));
        assertEquals("\"#comment\",\" space \",\"with,delim\",\"with\"\"quote\",\"with\nLF\",\"with\rCR\"",
                minimalFormat.format("#comment", " space ", "with,delim", "with\"quote", "with\nLF", "with\rCR"));

        // Null value and nullString
        final CSVFormat nullFormat = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("a,NULL,b", nullFormat.format("a", null, "b"));

        // Trim format with StringBuilder/CharSequence
        final CSVFormat trimFormat = CSVFormat.DEFAULT.withTrim();
        final StringBuilder cs = new StringBuilder("  trimmed  ");
        assertEquals("trimmed", trimFormat.format(cs));
        assertEquals("trimmed", trimFormat.format("  trimmed  "));

        // Trailing delimiter
        final CSVFormat trailingFormat = CSVFormat.DEFAULT.withTrailingDelimiter().withRecordSeparator("\n");
        sb.setLength(0);
        trailingFormat.printRecord(sb, "x", "y");
        assertEquals("x,y,\n", sb.toString());
    }

    @Test
    public void testPrintNoQuotesNoEscape() throws IOException {
        final CSVFormat format = CSVFormat.newFormat(',');
        final StringBuilder sb = new StringBuilder();
        format.print("hello", sb, true);
        format.print("world", sb, false);
        assertEquals("hello,world", sb.toString());
    }

    @Test
    public void testPrintCustomCharSequence() throws IOException {
        final CSVFormat trimFormat = CSVFormat.DEFAULT.withTrim();
        final CharSequence custom = new CharSequence() {
            private final String data = "  abc  ";
            @Override
            public int length() {
                return data.length();
            }
            @Override
            public char charAt(int index) {
                return data.charAt(index);
            }
            @Override
            public CharSequence subSequence(int start, int end) {
                return data.subSequence(start, end);
            }
            @Override
            public String toString() {
                return data;
            }
        };
        final StringBuilder sb = new StringBuilder();
        trimFormat.print(custom, sb, true);
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testParse() throws IOException {
        final Reader in = new StringReader("a,b,c\n1,2,3");
        final CSVParser parser = CSVFormat.DEFAULT.parse(in);
        assertNotNull(parser);
        assertEquals(2, parser.getRecords().size());
    }

    @Test
    public void testPrintAppendable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = CSVFormat.DEFAULT.print(sb);
        assertNotNull(printer);
        printer.printRecord("1", "2");
        assertEquals("1,2\r\n", sb.toString());
    }

    @Test
    public void testPrintFileAndPath() throws IOException {
        final File file = temporaryFolder.newFile("test.csv");
        final Path path = file.toPath();

        final Charset charset = StandardCharsets.UTF_8;
        try (final CSVPrinter printer = CSVFormat.DEFAULT.print(file, charset)) {
            printer.printRecord("A", "B");
        }
        final String content1 = new String(Files.readAllBytes(path), charset);
        assertEquals("A,B\r\n", content1);

        try (final CSVPrinter printer = CSVFormat.DEFAULT.print(path, charset)) {
            printer.printRecord("X", "Y");
        }
        final String content2 = new String(Files.readAllBytes(path), charset);
        assertEquals("X,Y\r\n", content2);
    }
}
