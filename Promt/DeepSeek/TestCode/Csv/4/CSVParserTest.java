package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    private static final String EMPTY_CSV = "";
    private static final String SINGLE_LINE = "a,b,c";
    private static final String TWO_RECORDS = "a,b,c\r\nd,e,f";
    private static final String WITH_COMMENT = "a,b,c\n# this is a comment\n d,e,f";
    private static final String WITH_NULL_STRING = "a,NULL,c";
    private static final String MULTI_LINE = "a,\"b\r\nc\",d";
    private static final String HEADER_LINE = "h1,h2,h3";
    private static final String DATA_LINE = "1,2,3";
    private static final String HEADER_AND_DATA = HEADER_LINE + "\r\n" + DATA_LINE;

    private CSVFormat formatDefault = CSVFormat.DEFAULT;

    @Test(expected = IllegalArgumentException.class)
    public void testCtorNullReader() throws IOException {
        new CSVParser(null, formatDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtorNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtorInvalidFormat() throws IOException {
        CSVFormat invalid = CSVFormat.DEFAULT.withQuote('\\').withEscape('\\');
        new CSVParser(new StringReader(""), invalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, formatDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        CSVParser.parse(new File("dummy"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, formatDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNull() throws IOException {
        CSVParser.parse((URL) null, Charset.forName("UTF-8"), formatDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNullCharset() throws IOException {
        CSVParser.parse(new URL("http://example.com"), null, formatDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNullFormat() throws IOException {
        CSVParser.parse(new URL("http://example.com"), Charset.forName("UTF-8"), null);
    }

    @Test
    public void testClose() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a"), formatDefault);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testIsClosed() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a"), formatDefault);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // no exception on double close
        parser.close();
    }

    @Test
    public void testGetRecordNumberInitialZero() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        assertEquals(0, parser.getRecordNumber());
    }

    @Test
    public void testGetCurrentLineNumberInitialOne() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        assertEquals(1, parser.getCurrentLineNumber());
    }

    @Test
    public void testGetRecordsEmptyInput() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(EMPTY_CSV), formatDefault);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    public void testGetRecordsSingleLine() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a", "b", "c");
    }

    @Test
    public void testGetRecordsTwoRecords() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(TWO_RECORDS), formatDefault);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a", "b", "c");
        CSVParserTest.assertRecordValues(records.get(1), "d", "e", "f");
    }

    @Test
    public void testGetRecordsWithHeader() throws IOException {
        CSVFormat format = formatDefault.withHeader();
        CSVParser parser = new CSVParser(new StringReader(HEADER_AND_DATA), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSVRecord record = records.get(0);
        CSVParserTest.assertRecordValues(record, "1", "2", "3");
        assertEquals("h1", record.get(0));
        assertEquals("2", record.get("h2"));
    }

    @Test
    public void testGetHeaderMapWithHeaderProvidedExplicitly() throws IOException {
        CSVFormat format = formatDefault.withHeader("A", "B", "C");
        CSVParser parser = new CSVParser(new StringReader(DATA_LINE), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
    }

    @Test
    public void testGetHeaderMapWithEmptyHeaderArray() throws IOException {
        CSVFormat format = formatDefault.withHeader(new String[0]);
        CSVParser parser = new CSVParser(new StringReader(HEADER_LINE + "\n" + DATA_LINE), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("h1"));
    }

    @Test
    public void testGetHeaderMapWithEmptyHeaderArrayNoData() throws IOException {
        CSVFormat format = formatDefault.withHeader(new String[0]);
        CSVParser parser = new CSVParser(new StringReader(""), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNull(headerMap); // no header read from file because no record
    }

    @Test
    public void testSkipHeaderRecord() throws IOException {
        CSVFormat format = formatDefault.withHeader("A","B","C").withSkipHeaderRecord(true);
        CSVParser parser = new CSVParser(new StringReader(HEADER_LINE + "\n" + DATA_LINE), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "1", "2", "3");
        // The header map should contain the provided headers, not the skipped line
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
    }

    @Test
    public void testNullStringHandling() throws IOException {
        CSVFormat format = formatDefault.withNullString("NULL");
        CSVParser parser = new CSVParser(new StringReader("a,NULL,c\nNULL,d,e"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a", null, "c");
        CSVParserTest.assertRecordValues(records.get(1), null, "d", "e");
    }

    @Test
    public void testNullStringCaseInsensitive() throws IOException {
        CSVFormat format = formatDefault.withNullString("null");
        CSVParser parser = new CSVParser(new StringReader("a,NULL,c"), format);
        List<CSVRecord> records = parser.getRecords();
        CSVParserTest.assertRecordValues(records.get(0), "a", null, "c");
    }

    @Test
    public void testComment() throws IOException {
        CSVFormat format = formatDefault.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader(WITH_COMMENT), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a", "b", "c");
        CSVParserTest.assertRecordValues(records.get(1), "d", "e", "f");
        // record #1 should have comment
        assertEquals("# this is a comment", records.get(1).getComment());
    }

    @Test
    public void testMultipleCommentsInRecord() throws IOException {
        CSVFormat format = formatDefault.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader("a,b,c\n#comment1\n#comment2\nd,e,f"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("#comment1\n#comment2", records.get(1).getComment());
    }

    @Test
    public void testIteratorHasNextAndNext() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(TWO_RECORDS), formatDefault);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVParserTest.assertRecordValues(it.next(), "a", "b", "c");
        assertTrue(it.hasNext());
        CSVParserTest.assertRecordValues(it.next(), "d", "e", "f");
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorNextWithoutHasNext() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        Iterator<CSVRecord> it = parser.iterator();
        CSVParserTest.assertRecordValues(it.next(), "a", "b", "c");
        // no more records
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testIteratorAfterClose() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        parser.close();
        Iterator<CSVRecord> it = parser.iterator();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals("CSVParser has been closed", e.getMessage());
        }
    }

    @Test
    public void testIteratorRemoveThrowsUnsupportedOperationException() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        Iterator<CSVRecord> it = parser.iterator();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetRecordsSequential() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), formatDefault);
        // first get some records via iterator
        Iterator<CSVRecord> it = parser.iterator();
        it.next(); // consume first record
        // now get records should return remaining
        List<CSVRecord> remaining = parser.getRecords();
        assertEquals(2, remaining.size());
        CSVParserTest.assertRecordValues(remaining.get(0), "c", "d");
        CSVParserTest.assertRecordValues(remaining.get(1), "e", "f");
    }

    @Test
    public void testMultiLineRecord() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(MULTI_LINE), formatDefault);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a", "b\r\nc", "d");
    }

    @Test
    public void testNextRecordEmpty() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(EMPTY_CSV), formatDefault);
        assertNull(parser.nextRecord());
    }

    @Test
    public void testNextRecordSingle() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(SINGLE_LINE), formatDefault);
        CSVParserTest.assertRecordValues(parser.nextRecord(), "a", "b", "c");
    }

    @Test
    public void testInvalidParseSequence() throws IOException {
        // To provoke an INVALID token, set quoteChar but not escape; unclosed quote might cause? 
        // Actually unclosed quote causes Lexer to throw, so maybe need specific configuration.
        // We'll use a custom format that makes parsing impossible: quoteChar with no escape?
        CSVFormat invalid = formatDefault.withQuote('"');
        CSVParser parser = new CSVParser(new StringReader("a,\"\rb"), invalid);
        try {
            parser.getRecords();
            fail("Expected IOException due to invalid parse sequence");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("invalid parse sequence"));
        }
    }

    @Test
    public void testEofTokenAddsRecordValue() throws IOException {
        // When EOF encountered and isReady is true, the content is added to record.
        // To test, we need a token that sets isReady true. Actually isReady is set when token has content.
        CSVParser parser = new CSVParser(new StringReader("a"), formatDefault);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        CSVParserTest.assertRecordValues(records.get(0), "a");
    }

    @Test
    public void testRecordNumberIncrement() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a\nb\nc"), formatDefault);
        assertEquals(0, parser.getRecordNumber());
        parser.nextRecord();
        assertEquals(1, parser.getRecordNumber());
        parser.nextRecord();
        assertEquals(2, parser.getRecordNumber());
        parser.nextRecord();
        assertEquals(3, parser.getRecordNumber());
    }

    @Test
    public void testGetHeaderMapCopy() throws IOException {
        CSVFormat format = formatDefault.withHeader("X","Y");
        CSVParser parser = new CSVParser(new StringReader("1,2"), format);
        Map<String, Integer> map1 = parser.getHeaderMap();
        Map<String, Integer> map2 = parser.getHeaderMap();
        assertNotSame(map1, map2);
        assertEquals(map1, map2);
    }

    @Test
    public void testParseString() throws IOException {
        CSVParser parser = CSVParser.parse(SINGLE_LINE, CSVFormat.DEFAULT);
        assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
    }

    // Helper
    private static void assertRecordValues(CSVRecord record, String... expected) {
        assertNotNull(record);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], record.get(i));
        }
    }
}
