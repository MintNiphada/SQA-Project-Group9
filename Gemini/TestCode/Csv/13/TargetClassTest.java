package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;

public class CSVFormatTest {

    @Test
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals(CRLF, CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertNull(CSVFormat.DEFAULT.getHeaderComments());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
        assertFalse(CSVFormat.DEFAULT.getIgnoreHeaderCase());

        assertNotNull(CSVFormat.RFC4180);
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertNotNull(CSVFormat.EXCEL);
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());
        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());

        assertNotNull(CSVFormat.TDF);
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertNotNull(CSVFormat.MYSQL);
        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals(String.valueOf(LF), CSVFormat.MYSQL.getRecordSeparator());
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }

    @Test
    public void testPredefinedEnumAndValueOf() {
        for (CSVFormat.Predefined predefined : CSVFormat.Predefined.values()) {
            assertNotNull(predefined.getFormat());
            assertEquals(predefined.getFormat(), CSVFormat.valueOf(predefined.name()));
        }
        assertEquals(CSVFormat.DEFAULT, CSVFormat.Predefined.Default.getFormat());
        assertEquals(CSVFormat.EXCEL, CSVFormat.Predefined.Excel.getFormat());
        assertEquals(CSVFormat.MYSQL, CSVFormat.Predefined.MySQL.getFormat());
        assertEquals(CSVFormat.RFC4180, CSVFormat.Predefined.RFC4180.getFormat());
        assertEquals(CSVFormat.TDF, CSVFormat.Predefined.TDF.getFormat());
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatDelimiterLF() {
        CSVFormat.newFormat(LF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatDelimiterCR() {
        CSVFormat.newFormat(CR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLF() {
        CSVFormat.DEFAULT.withDelimiter(LF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterCR() {
        CSVFormat.DEFAULT.withDelimiter(CR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLF() {
        CSVFormat.DEFAULT.withQuote(LF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCR() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf(CR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLF() {
        CSVFormat.DEFAULT.withEscape(LF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCR() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf(CR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLF() {
        CSVFormat.DEFAULT.withCommentMarker(LF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerCR() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf(CR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteCharSameAsDelimiter() {
        CSVFormat.DEFAULT.withQuote(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeCharSameAsDelimiter() {
        CSVFormat.DEFAULT.withEscape(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCommentMarkerSameAsDelimiter() {
        CSVFormat.DEFAULT.withCommentMarker(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteCharSameAsCommentMarker() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEscapeSameAsCommentMarker() {
        CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuoteModeNoneWithoutEscape() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDuplicateHeaderEntries() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test
    public void testWithQuoteModeNoneWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertEquals(QuoteMode.NONE, format.getQuoteMode());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }

    @Test
    public void testWithHeader() {
        String[] header = new String[] { "col1", "col2", "col3" };
        CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, format.getHeader());
        
        header[0] = "altered";
        assertNotEquals("altered", format.getHeader()[0]);

        String[] gotHeader = format.getHeader();
        gotHeader[0] = "alteredAgain";
        assertNotEquals("alteredAgain", format.getHeader()[0]);

        CSVFormat nullHeader = format.withHeader((String[]) null);
        assertNull(nullHeader.getHeader());

        CSVFormat emptyHeader = format.withHeader(new String[0]);
        assertNotNull(emptyHeader.getHeader());
        assertEquals(0, emptyHeader.getHeader().length);
    }

    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", null, 123);
        String[] comments = format.getHeaderComments();
        assertNotNull(comments);
        assertEquals(3, comments.length);
        assertEquals("Comment 1", comments[0]);
        assertNull(comments[1]);
        assertEquals("123", comments[2]);

        comments[0] = "altered";
        assertNotEquals("altered", format.getHeaderComments()[0]);

        CSVFormat nullComments = format.withHeaderComments((Object[]) null);
        assertNull(nullComments.getHeaderComments());
    }

    @Test
    public void testWithHeaderResultSetMetaData() throws SQLException {
        final String[] columnLabels = new String[] { "id", "name", "age" };
        ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[] { ResultSetMetaData.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getColumnCount".equals(method.getName())) {
                            return columnLabels.length;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            int idx = ((Integer) args[0]) - 1;
                            return columnLabels[idx];
                        }
                        return null;
                    }
                });

        CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(columnLabels, format.getHeader());

        CSVFormat nullFormat = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(nullFormat.getHeader());
    }

    @Test
    public void testWithHeaderResultSet() throws SQLException {
        final String[] columnLabels = new String[] { "colA", "colB" };
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[] { ResultSetMetaData.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("getColumnCount".equals(method.getName())) {
                            return columnLabels.length;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            int idx = ((Integer) args[0]) - 1;
                            return columnLabels[idx];
                        }
                        return null;
                    }
                });

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[] { ResultSet.class },
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
        assertArrayEquals(columnLabels, format.getHeader());

        CSVFormat nullFormat = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(nullFormat.getHeader());
    }

    @Test
    public void testWithBooleanAndCharModifiers() {
        CSVFormat format = CSVFormat.DEFAULT
                .withAllowMissingColumnNames()
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withIgnoreHeaderCase()
                .withSkipHeaderRecord()
                .withNullString("NULL")
                .withQuoteMode(QuoteMode.ALL)
                .withQuote('\'')
                .withCommentMarker('#')
                .withEscape('/')
                .withRecordSeparator("\n");

        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreEmptyLines());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getSkipHeaderRecord());
        assertEquals("NULL", format.getNullString());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertEquals(Character.valueOf('/'), format.getEscapeCharacter());
        assertEquals("\n", format.getRecordSeparator());

        CSVFormat modified = format
                .withAllowMissingColumnNames(false)
                .withIgnoreEmptyLines(false)
                .withIgnoreSurroundingSpaces(false)
                .withIgnoreHeaderCase(false)
                .withSkipHeaderRecord(false)
                .withQuote(null)
                .withCommentMarker((Character) null)
                .withEscape((Character) null)
                .withRecordSeparator(';');

        assertFalse(modified.getAllowMissingColumnNames());
        assertFalse(modified.getIgnoreEmptyLines());
        assertFalse(modified.getIgnoreSurroundingSpaces());
        assertFalse(modified.getIgnoreHeaderCase());
        assertFalse(modified.getSkipHeaderRecord());
        assertNull(modified.getQuoteCharacter());
        assertNull(modified.getCommentMarker());
        assertNull(modified.getEscapeCharacter());
        assertEquals(";", modified.getRecordSeparator());
    }

    @Test
    public void testIsSetPredicates() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertFalse(format.isCommentMarkerSet());
        assertFalse(format.isEscapeCharacterSet());
        assertFalse(format.isNullStringSet());
        assertFalse(format.isQuoteCharacterSet());

        format = format.withCommentMarker('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withQuote('"');

        assertTrue(format.isCommentMarkerSet());
        assertTrue(format.isEscapeCharacterSet());
        assertTrue(format.isNullStringSet());
        assertTrue(format.isQuoteCharacterSet());
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a CSVFormat"));

        assertNotEquals(f1, f1.withDelimiter(';'));
        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1, f1.withQuote(null));
        assertNotEquals(f1.withQuote(null), f1);
        assertNotEquals(f1, f1.withEscape('\\').withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1.withCommentMarker('#'), f1);
        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1.withEscape('\\'), f1);
        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1.withNullString("NULL"), f1);
        assertNotEquals(f1, f1.withHeader("A", "B"));
        assertNotEquals(f1.withHeader("A", "B"), f1.withHeader("A", "C"));
        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(false));
        assertNotEquals(f1, f1.withSkipHeaderRecord(true));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1.withRecordSeparator((String) null), f1);
        assertNotEquals(f1, f1.withRecordSeparator((String) null));

        CSVFormat complex1 = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("N/A")
                .withQuoteMode(QuoteMode.MINIMAL)
                .withRecordSeparator("\r\n")
                .withHeader("H1", "H2")
                .withIgnoreHeaderCase(true)
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withSkipHeaderRecord(true);

        CSVFormat complex2 = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("N/A")
                .withQuoteMode(QuoteMode.MINIMAL)
                .withRecordSeparator("\r\n")
                .withHeader("H1", "H2")
                .withIgnoreHeaderCase(true)
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withSkipHeaderRecord(true);

        assertEquals(complex1, complex2);
        assertEquals(complex1.hashCode(), complex2.hashCode());
    }

    @Test
    public void testToString() {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreHeaderCase(true)
                .withSkipHeaderRecord(true)
                .withHeaderComments("Comment1")
                .withHeader("A", "B");

        String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("IgnoreHeaderCase:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:true"));
        assertTrue(str.contains("HeaderComments:[Comment1]"));
        assertTrue(str.contains("Header:[A, B]"));

        CSVFormat minimal = CSVFormat.newFormat('|');
        String minStr = minimal.toString();
        assertEquals("Delimiter=<|> SkipHeaderRecord:false", minStr);
    }

    @Test
    public void testFormat() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);

        String withQuotes = CSVFormat.DEFAULT.format("a,1", "b", "c");
        assertEquals("\"a,1\",b,c", withQuotes);
    }

    @Test
    public void testParseAndPrint() throws IOException {
        Reader reader = new StringReader("a,b,c\n1,2,3");
        CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
        assertEquals("a", parser.iterator().next().get(0));

        StringBuilder out = new StringBuilder();
        CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        assertNotNull(printer);
        printer.printRecord("x", "y");
        assertTrue(out.toString().contains("x,y"));
    }

    @Test
    public void testSerialization() throws Exception {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("N/A")
                .withHeader("A", "B")
                .withHeaderComments("HC");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(format);
        }

        CSVFormat deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (CSVFormat) ois.readObject();
        }

        assertEquals(format, deserialized);
        assertArrayEquals(format.getHeader(), deserialized.getHeader());
        assertArrayEquals(format.getHeaderComments(), deserialized.getHeaderComments());
    }
}
