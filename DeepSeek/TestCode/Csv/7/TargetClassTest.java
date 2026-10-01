package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    // Constructor tests

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("a"), null);
    }

    @Test
    public void testConstructorValid() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        assertNotNull(parser);
        parser.close();
    }

    // Static parse methods

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        File file = new File("dummy");
        CSVParser.parse(file, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNullUrl() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNullCharset() throws IOException {
        URL url = new URL("http://example.com");
        CSVParser.parse(url, null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNullFormat() throws IOException {
        URL url = new URL("http://example.com");
        CSVParser.parse(url, Charset.defaultCharset(), null);
    }

    @Test
    public void testParseFileValid() throws Exception {
        File tempFile = File.createTempFile("csv", ".csv");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);
        writer.write("a,b\nc,d");
        writer.close();
        CSVParser parser = CSVParser.parse(tempFile, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(1).get(0));
        assertEquals("d", records.get(1).get(1));
        parser.close();
    }

    @Test
    public void testParseStringValid() throws IOException {
        CSVParser parser = CSVParser.parse("v1,v2", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("v1", records.get(0).get(0));
        assertEquals("v2", records.get(0).get(1));
        parser.close();
    }

    @Test
    public void testParseUrlValid() throws Exception {
        File tempFile = File.createTempFile("csv", ".csv");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);
        writer.write("x,y");
        writer.close();
        URL url = tempFile.toURI().toURL();
        CSVParser parser = CSVParser.parse(url, Charset.defaultCharset(), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("x", records.get(0).get(0));
        assertEquals("y", records.get(0).get(1));
        parser.close();
    }

    // getHeaderMap tests

    @Test
    public void testGetHeaderMapNoHeader() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testGetHeaderMapWithHeaderProvided() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2");
        CSVParser parser = CSVParser.parse("1,2", format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("H1"));
        assertEquals(Integer.valueOf(1), headerMap.get("H2"));
        parser.close();
    }

    @Test
    public void testGetHeaderMapIsCopy() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        CSVParser parser = CSVParser.parse("1,2", format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        headerMap.put("C", 2); // modify copy
        Map<String, Integer> headerMap2 = parser.getHeaderMap();
        assertFalse(headerMap2.containsKey("C")); // original unchanged
        assertEquals(Integer.valueOf(0), headerMap2.get("A"));
        parser.close();
    }

    @Test
    public void testGetHeaderMapFromFirstLine() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(); // empty header => read from first line
        CSVParser parser = CSVParser.parse("colX,colY\n1,2", format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("colX"));
        assertEquals(Integer.valueOf(1), headerMap.get("colY"));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("1", records.get(0).get(0));
        parser.close();
    }

    @Test
    public void testSkipHeaderRecordWithProvidedHeader() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse("shouldSkip1,shouldSkip2\nval1,val2", format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(1), headerMap.get("B"));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("val1", records.get(0).get(0));
        assertEquals("val2", records.get(0).get(1));
        parser.close();
    }

    @Test
    public void testSkipHeaderRecordWithAutoHeader() throws IOException {
        // skipHeaderRecord is ignored when header is read from first line
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse("header1,header2\nvalue1,value2", format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        // header map should be from first line
        assertEquals(Integer.valueOf(0), parser.getHeaderMap().get("header1"));
        parser.close();
    }

    // getCurrentLineNumber and getRecordNumber

    @Test
    public void testLineAndRecordNumbers() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertEquals(0, parser.getRecordNumber());
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVParser.Record rec1 = it.next();
        assertEquals(1, parser.getRecordNumber());
        // line number may be 1 or 2 depending on implementation; just check it increases
        long lineAfter1 = parser.getCurrentLineNumber();
        assertTrue(lineAfter1 >= 1);
        rec1.get(0) // access
        CSVParser.Record rec2 = it.next();
        assertEquals(2, parser.getRecordNumber());
        long lineAfter2 = parser.getCurrentLineNumber();
        assertTrue(lineAfter2 > lineAfter1);
        parser.close();
    }

    // close and isClosed

    @Test
    public void testCloseAndIsClosed() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a"), CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // closing again should not throw
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Iterator tests

    @Test
    public void testIteratorHasNextFalseOnEmpty() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertFalse(parser.iterator().hasNext());
        parser.close();
    }

    @Test
    public void testIteratorNextThrowsNoSuchElementException() throws IOException {
        CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        parser.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        it.next();
        it.remove();
    }

    @Test
    public void testIteratorOnClosedParserHasNext() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        parser.close();
        assertFalse(parser.iterator().hasNext());
    }

    @Test
    public void testIteratorOnClosedParserNextThrows() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        parser.close();
        Iterator<CSVRecord> it = parser.iterator();
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("closed"));
        }
    }

    @Test
    public void testIteratorIOExceptionWrappedInRuntimeException() throws IOException {
        Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO failure");
            }
            @Override
            public void close() throws IOException { }
        };
        CSVParser parser = new CSVParser(failingReader, CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        try {
            it.next();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertTrue(e.getCause().getMessage().contains("Simulated IO failure"));
        } finally {
            parser.close();
        }
    }

    // nextRecord and parsing edge cases

    @Test
    public void testEmptyInputNoRecords() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
        parser.close();
    }

    @Test
    public void testNormalParsing() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        CSVParser.Record rec1 = records.get(0);
        assertEquals(2, rec1.size());
        assertEquals("a", rec1.get(0));
        assertEquals("b", rec1.get(1));
        CSVParser.Record rec2 = records.get(1);
        assertEquals("c", rec2.get(0));
        assertEquals("d", rec2.get(1));
        parser.close();
    }

    @Test
    public void testNullStringHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = CSVParser.parse("NULL,value\nvalue2,NULL", format);
        Iterator<CSVRecord> it = parser.iterator();
        CSVParser.Record rec1 = it.next();
        assertNull(rec1.get(0));
        assertEquals("value", rec1.get(1));
        CSVParser.Record rec2 = it.next();
        assertEquals("value2", rec2.get(0));
        assertNull(rec2.get(1));
        parser.close();
    }

    @Test
    public void testNullStringCaseInsensitive() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("null");
        CSVParser parser = CSVParser.parse("Null,NULL", format);
        CSVParser.Record rec = parser.iterator().next();
        assertNull(rec.get(0));
        assertNull(rec.get(1));
        parser.close();
    }

    @Test
    public void testInvalidTokenSequence() throws IOException {
        // malformed quoting should cause INVALID token
        CSVParser parser = CSVParser.parse("\"unterminated", CSVFormat.DEFAULT);
        try (CSVParser p = parser) {
            p.iterator().next();
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("invalid parse sequence"));
        }
    }

    @Test
    public void testCommentHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String data = "# comment line\ndata1,data2";
        CSVParser parser = CSVParser.parse(data, format);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVParser.Record rec1 = it.next();
        // the comment line creates a record with an empty string and a comment
        assertEquals(1, rec1.size());
        assertEquals("", rec1.get(0));
        assertEquals(" comment line", rec1.getComment());
        assertTrue(it.hasNext());
        CSVParser.Record rec2 = it.next();
        assertEquals("data1", rec2.get(0));
        assertEquals("data2", rec2.get(1));
        assertFalse(it.hasNext());
        parser.close();
    }

    @Test
    public void testCommentAtEOF() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = CSVParser.parse("# only comment", format);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVParser.Record rec = it.next();
        assertEquals(1, rec.size());
        assertEquals("", rec.get(0));
        assertEquals(" only comment", rec.getComment());
        assertFalse(it.hasNext());
        parser.close();
    }

    // getRecords with collection parameter

    @Test
    public void testGetRecordsIntoCollection() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        List<CSVRecord> records = new ArrayList<CSVRecord>();
        parser.getRecords(records);
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        parser.close();
    }

    @Test
    public void testGetRecordsIntoCollectionReturnsSame() throws IOException {
        CSVParser parser = CSVParser.parse("x", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords(new ArrayList<CSVRecord>());
        assertSame(records, parser.getRecords(records)); // second call adds to same collection? Actually returns same reference
        parser.close();
    }

    // Multi-line field (quoting with newline)

    @Test
    public void testMultiLineField() throws IOException {
        String data = "a,\"b\nc\"";
        CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT);
        CSVParser.Record rec = parser.iterator().next();
        assertEquals("a", rec.get(0));
        // the second field should contain newline
        assertTrue(rec.get(1).contains("\n"));
        assertEquals("b\nc", rec.get(1));
        parser.close();
    }
}
