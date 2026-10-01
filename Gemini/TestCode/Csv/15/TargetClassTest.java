package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    private enum TestHeaderEnum {
        COL_A, COL_B, COL_C
    }

    @Test
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD);
        assertNotNull(CSVFormat.INFORMIX_UNLOAD_CSV);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.POSTGRESQL_CSV);
        assertNotNull(CSVFormat.POSTGRESQL_TEXT);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.TDF);

        assertEquals(CSVFormat.DEFAULT, CSVFormat.Predefined.Default.getFormat());
        assertEquals(CSVFormat.EXCEL, CSVFormat.Predefined.Excel.getFormat());
        assertEquals(CSVFormat.INFORMIX_UNLOAD, CSVFormat.Predefined.InformixUnload.getFormat());
        assertEquals(CSVFormat.INFORMIX_UNLOAD_CSV, CSVFormat.Predefined.InformixUnloadCsv.getFormat());
        assertEquals(CSVFormat.MYSQL, CSVFormat.Predefined.MySQL.getFormat());
        assertEquals(CSVFormat.POSTGRESQL_CSV, CSVFormat.Predefined.PostgreSQLCsv.getFormat());
        assertEquals(CSVFormat.POSTGRESQL_TEXT, CSVFormat.Predefined.PostgreSQLText.getFormat());
        assertEquals(CSVFormat.RFC4180, CSVFormat.Predefined.RFC4180.getFormat());
        assertEquals(CSVFormat.TDF, CSVFormat.Predefined.TDF.getFormat());

        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
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
        assertFalse(format.getAutoFlush());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithCrDelimiterThrows() {
        CSVFormat.newFormat('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLfDelimiterThrows() {
        CSVFormat.newFormat('\n');
    }

    @Test
    public void testWithMethodsAndGetters() {
        CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('\'')
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker('#')
                .withEscape('\\')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withHeaderComments("Comment 1", "Comment 2")
                .withHeader("A", "B", "C")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true)
                .withIgnoreHeaderCase(true)
                .withTrim(true)
                .withTrailingDelimiter(true)
                .withAutoFlush(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new String[]{"Comment 1", "Comment 2"}, format.getHeaderComments());
        assertArrayEquals(new String[]{"A", "B", "C"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getTrim());
        assertTrue(format.getTrailingDelimiter());
        assertTrue(format.getAutoFlush());

        assertTrue(format.isCommentMarkerSet());
        assertTrue(format.isEscapeCharacterSet());
        assertTrue(format.isNullStringSet());
        assertTrue(format.isQuoteCharacterSet());
    }

    @Test
    public void testWithMethodsNoArgOverloads() {
        CSVFormat format = CSVFormat.DEFAULT
                .withAllowMissingColumnNames()
                .withIgnoreEmptyLines()
                .withIgnoreHeaderCase()
                .withIgnoreSurroundingSpaces()
                .withSkipHeaderRecord()
                .withTrailingDelimiter()
                .withTrim();

        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreEmptyLines());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getTrailingDelimiter());
        assertTrue(format.getTrim());
    }

    @Test
    public void testWithFirstRecordAsHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(format.getSkipHeaderRecord());
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
    }

    @Test
    public void testWithHeaderEnum() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeaderEnum.class);
        assertArrayEquals(new String[]{"COL_A", "COL_B", "COL_C"}, format.getHeader());

        final CSVFormat nullEnumFormat = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(nullEnumFormat.getHeader());
    }

    @Test
    public void testWithHeaderCommentsWithNullElements() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", null, 123);
        assertArrayEquals(new String[]{"Comment 1", null, "123"}, format.getHeaderComments());

        final CSVFormat nullCommentsFormat = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(nullCommentsFormat.getHeaderComments());
    }

    @Test
    public void testWithHeaderResultSetMetaData() throws SQLException {
        ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getColumnCount".equals(method.getName())) {
                            return 2;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            int idx = (Integer) args[0];
                            return "Column" + idx;
                        }
                        return null;
                    }
                });

        CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[]{"Column1", "Column2"}, format.getHeader());

        CSVFormat nullFormat = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(nullFormat.getHeader());
    }

    @Test
    public void testWithHeaderResultSet() throws SQLException {
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getColumnCount".equals(method.getName())) {
                            return 1;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            return "OnlyCol";
                        }
                        return null;
                    }
                });

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getMetaData".equals(method.getName())) {
                            return metaData;
                        }
                        return null;
                    }
                });

        CSVFormat format = CSVFormat.DEFAULT.withHeader(resultSet);
        assertArrayEquals(new String[]{"OnlyCol"}, format.getHeader());

        CSVFormat nullFormat = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(nullFormat.getHeader());
    }

    @Test
    public void testWithRecordSeparatorChar() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test
    public void testWithQuoteCharAndCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());

        format = format.withQuote((Character) null);
        assertNull(format.getQuoteCharacter());
        assertFalse(format.isQuoteCharacterSet());
    }

    @Test
    public void testWithEscapeCharAndCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());

        format = format.withEscape((Character) null);
        assertNull(format.getEscapeCharacter());
        assertFalse(format.isEscapeCharacterSet());
    }

    @Test
    public void testWithCommentMarkerCharAndCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());

        format = format.withCommentMarker((Character) null);
        assertNull(format.getCommentMarker());
        assertFalse(format.isCommentMarkerSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDelimiterLineBreakCrThrows() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDelimiterLineBreakLfThrows() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteLineBreakThrows() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteCharacterLineBreakThrows() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeLineBreakThrows() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeCharacterLineBreakThrows() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCommentMarkerLineBreakThrows() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCommentMarkerCharacterLineBreakThrows() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteEqualsDelimiterThrows() {
        CSVFormat.DEFAULT.withQuote(',').withDelimiter(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeEqualsDelimiterThrows() {
        CSVFormat.DEFAULT.withEscape(',').withDelimiter(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCommentMarkerEqualsDelimiterThrows() {
        CSVFormat.DEFAULT.withCommentMarker(',').withDelimiter(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteEqualsCommentMarkerThrows() {
        CSVFormat.DEFAULT.withQuote('#').withCommentMarker('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeEqualsCommentMarkerThrows() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNoQuoteModeWithoutEscapeThrows() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape((Character) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDuplicateHeadersThrows() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        assertEquals(f1, f1);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "NotACSVFormat");

        CSVFormat f2 = CSVFormat.DEFAULT;
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        assertNotEquals(f1, f1.withDelimiter(';'));
        assertNotEquals(f1, f1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1.withQuote((Character) null), f1);
        assertNotEquals(f1, f1.withQuote((Character) null));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1.withCommentMarker('#'), f1);
        assertNotEquals(f1.withCommentMarker('#'), f1.withCommentMarker('!'));
        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1.withEscape('\\'), f1);
        assertNotEquals(f1.withEscape('\\'), f1.withEscape('/'));
        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1.withNullString("NULL"), f1);
        assertNotEquals(f1.withNullString("NULL"), f1.withNullString("NIL"));
        assertNotEquals(f1, f1.withHeader("A", "B"));
        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(false));
        assertNotEquals(f1, f1.withSkipHeaderRecord(true));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1.withRecordSeparator((String) null), f1);
        assertNotEquals(f1, f1.withRecordSeparator((String) null));
        assertNotEquals(f1.withRecordSeparator("\n"), f1.withRecordSeparator("\r\n"));

        CSVFormat fNullFields1 = CSVFormat.newFormat(',');
        CSVFormat fNullFields2 = CSVFormat.newFormat(',');
        assertEquals(fNullFields1, fNullFields2);
        assertEquals(fNullFields1.hashCode(), fNullFields2.hashCode());
    }

    @Test
    public void testToString() {
        String s = CSVFormat.DEFAULT.toString();
        assertTrue(s.contains("Delimiter=<,>"));
        assertTrue(s.contains("QuoteChar=<\">"));
        assertTrue(s.contains("RecordSeparator=<\r\n>"));
        assertTrue(s.contains("EmptyLines:ignored"));

        CSVFormat complex = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreHeaderCase(true)
                .withSkipHeaderRecord(true)
                .withHeaderComments("Comment")
                .withHeader("Col1", "Col2");

        String sComplex = complex.toString();
        assertTrue(sComplex.contains("Escape=<\\>"));
        assertTrue(sComplex.contains("CommentStart=<#>"));
        assertTrue(sComplex.contains("NullString=<NULL>"));
        assertTrue(sComplex.contains("SurroundingSpaces:ignored"));
        assertTrue(sComplex.contains("IgnoreHeaderCase:ignored"));
        assertTrue(sComplex.contains("SkipHeaderRecord:true"));
        assertTrue(sComplex.contains("HeaderComments:[Comment]"));
        assertTrue(sComplex.contains("Header:[Col1, Col2]"));
    }

    @Test
    public void testFormat() {
        assertEquals("a,b,c", CSVFormat.DEFAULT.format("a", "b", "c"));
        assertEquals("\"a,b\",c", CSVFormat.DEFAULT.format("a,b", "c"));
    }

    @Test
    public void testPrintNullValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.print(null, sw, true);
        assertEquals("", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withNullString("NULL").print(null, sw, true);
        assertEquals("NULL", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.ALL).print(null, sw, true);
        assertEquals("\"NULL\"", sw.toString());
    }

    @Test
    public void testPrintQuoteModes() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).printRecord(sw, "a", 1, null);
        assertEquals("\"a\",\"1\",\"\"\r\n", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL).printRecord(sw, "a", 1, null);
        assertEquals("\"a\",\"1\",\r\n", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC).printRecord(sw, "a", 123, 45.67, null);
        assertEquals("\"a\",123,45.67,\"\"\r\n", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE).printRecord(sw, "a,b", "c\nd", "e\\f", "g\rh");
        assertEquals("a\\,b,c\\nd,e\\\\f,g\\rh\r\n", sw.toString());
    }

    @Test
    public void testPrintQuoteMinimalCases() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.printRecord(sw, "", "normal", "with,comma", "with\"quote", "with\nLF", "with\rCR", "#startsComment", " endsWithSpace ", "\u0001control");
        assertEquals("\"\",normal,\"with,comma\",\"with\"\"quote\",\"with\nLF\",\"with\rCR\",\"#startsComment\",\" endsWithSpace \",\"\u0001control\"\r\n", sw.toString());
    }

    @Test
    public void testPrintWithEscapeNoQuote() throws IOException {
        CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withRecordSeparator("\r\n");
        StringWriter sw = new StringWriter();
        format.printRecord(sw, "a,b", "c\\d", "e\nf", "g\rh", "plain");
        assertEquals("a\\,b,c\\\\d,e\\nf,g\\rh,plain\r\n", sw.toString());
    }

    @Test
    public void testPrintNoQuoteNoEscape() throws IOException {
        CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator("\n");
        StringWriter sw = new StringWriter();
        format.printRecord(sw, "a", "b", "c");
        assertEquals("a,b,c\n", sw.toString());
    }

    @Test
    public void testPrintWithTrim() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim();
        StringWriter sw = new StringWriter();
        format.printRecord(sw, "  hello  ", new StringBuilder("  world  "));
        assertEquals("hello,world\r\n", sw.toString());

        sw = new StringWriter();
        format.printRecord(sw, "   ", new StringBuilder("   "));
        assertEquals("\"\",\r\n", sw.toString());
    }

    @Test
    public void testPrintlnAndTrailingDelimiter() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.withTrailingDelimiter(true).println(sw);
        assertEquals(",\r\n", sw.toString());

        sw = new StringWriter();
        CSVFormat.DEFAULT.withTrailingDelimiter(false).withRecordSeparator((String) null).println(sw);
        assertEquals("", sw.toString());
    }

    @Test
    public void testParse() throws IOException {
        Reader in = new StringReader("a,b,c\n1,2,3\n");
        try (CSVParser parser = CSVFormat.DEFAULT.parse(in)) {
            assertNotNull(parser);
            assertEquals(2, parser.getRecords().size());
        }
    }

    @Test
    public void testPrintAppendable() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(sw)) {
            printer.printRecord("A", "B");
            assertEquals("A,B\r\n", sw.toString());
        }
    }

    @Test
    public void testPrinter() throws IOException {
        try (CSVPrinter printer = CSVFormat.DEFAULT.printer()) {
            assertNotNull(printer);
        }
    }

    @Test
    public void testPrintFileAndPath() throws IOException {
        File file = temporaryFolder.newFile("test_csv_format.csv");
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(file, StandardCharsets.UTF_8)) {
            printer.printRecord("1", "2");
        }
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertEquals("1,2\r\n", content);

        Path path = temporaryFolder.newFile("test_csv_format_path.csv").toPath();
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(path, StandardCharsets.UTF_8)) {
            printer.printRecord("3", "4");
        }
        content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("3,4\r\n", content);
    }
}
