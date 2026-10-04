package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Iterator;

import org.junit.Test;

public class CSVParserTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    // ---- parse(File, Charset, CSVFormat) ----
    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        File file = createTempCsvFile("a,b\n");
        try {
            CSVParser.parse(file, UTF8, null);
        } finally {
            file.delete();
        }
    }

    @Test
    public void testParseFileNormal() throws IOException {
        File file = createTempCsvFile("a,b\nc,d");
        CSVParser parser = CSVParser.parse(file, UTF8, CSVFormat.DEFAULT);
        try {
            assertNotNull(parser);
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
            assertEquals("c", records.get(1).get(0));
            assertEquals("d", records.get(1).get(1));
        } finally {
            file.delete();
        }
    }

    // ---- parse(String, CSVFormat) ----
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("a,b", null);
    }

    @Test
    public void testParseStringNormal() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
            assertEquals("c", records.get(1).get(0));
            assertEquals("d", records.get(1).get(1));
        } finally {
            parser.close();
    }

    // ---- parse(URL, Charset, CSVFormat) ----
    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        File file = createTempCsvFile("a,b\n");
        URL url = file.toURI().toURL();
        try {
            CSVParser.parse(url, null, CSVFormat.DEFAULT);
        } finally {
            file.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        File file = createTempCsvFile("a,b\n");
        URL url = file.toURI().toURL();
        try {
            CSVParser.parse(url, UTF8, null);
        } finally {
            file.delete();
        }
    }

    @Test
    public void testParseURLNormal() throws IOException {
        File file = createTempCsvFile("a,b\nc,d");
        URL url = file.toURI().toURL();
        CSVParser parser = CSVParser.parse(url, UTF8, CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
            assertEquals("c", records.get(1).get(0));
            assertEquals("d", records.get(1).get(1));
        } finally {
            file.delete();
        }
    }

    // ---- Constructor ----
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser((Reader) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("a\n"), null);
    }

    @Test
    public void testConstructorNormal() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        assertNotNull(parser);
        parser.close();
    }

    // ---- close / isClosed ----
    @Test
    public void testCloseAndIsClosed() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n"), CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testIteratorHasNextWithClosedParser() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n"), CSVFormat.DEFAULT);
        parser.close();
        Iterator<CSVRecord> it = parser.iterator();
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextWithClosedParser() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n"), CSVFormat.DEFAULT);
        parser.close();
        parser.iterator().next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n"), CSVFormat.DEFAULT);
        parser.iterator().remove();
    }

    // ---- getRecords() ----
    @Test
    public void testGetRecordsAll() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(1).get(0));
        assertEquals("d", records.get(1).get(1));
        assertEquals("e", records.get(2).get(0));
        assertEquals("f", records.get(2).get(1));
    }

    @Test
    public void testGetRecordsEmpty() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    public void testGetRecordsWithCollection() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        List<CSVRecord> dest = new ArrayList<CSVRecord>();
        List<CSVRecord> result = parser.getRecords(dest);
        assertSame(dest, result);
        assertEquals(2, dest.size());
    }

    // ---- getHeaderMap() ----
    @Test
    public void testGetHeaderMapWithoutHeader() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n"), CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
    }

    @Test
    public void testGetHeaderMapWithHeader() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2");
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), format);
        Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(0), map.get("h1"));
        assertEquals(Integer.valueOf(1), map.get("h2"));
    }

    @Test
    public void testGetHeaderMapReturnsCopy() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("x");
        CSVParser parser = new CSVParser(new StringReader("val\n"), format);
        Map<String, Integer> map1 = parser.getHeaderMap();
        Map<String, Integer> map2 = parser.getHeaderMap();
        assertNotSame(map1, map2);
        assertEquals(map1, map2);
    }

    // ---- Header duplication exception ----
    @Test(expected = IllegalArgumentException.class)
    public void testHeaderDuplicates() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "a");
        new CSVParser(new StringReader("1,2\n"), format);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderDuplicateEmptyHeadersNotIgnored() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("", "").withIgnoreEmptyHeaders(false);
        new CSVParser(new StringReader("1,2\n"), format);
    }

    // ---- Header with empty array reads from first record ----
    @Test
    public void testHeaderEmptyArrayReadsFromFirstRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();  // empty header array
        CSVParser parser = new CSVParser(new StringReader("colA,colB\nv1,v2"), format);
        Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(0), map.get("colA"));
        assertEquals(Integer.valueOf(1), map.get("colB"));
        // The header record should be consumed, the first data record is v1,v2
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("v1", records.get(0).get("colA"));
        assertEquals("v2", records.get(0).get("colB"));
    }

    // ---- Header with skipHeaderRecord ----
    @Test
    public void testHeaderSkipHeaderRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(true);
        CSVParser parser = new CSVParser(new StringReader("h1,h2\na,b\nc,d"), format);
        // header record skipped, data starts from a,b
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get("h1"));
        assertEquals("b", records.get(0).get("h2"));
        assertEquals("c", records.get(1).get("h1"));
        assertEquals("d", records.get(1).get("h2"));
    }

    @Test
    public void testHeaderSkipHeaderRecordFalse() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(false);
        CSVParser parser = new CSVParser(new StringReader("h1,h2\na,b\nc,d"), format);
        // header row not skipped, but still added as header map, first record is "h1,h2" line
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());  // header line + two data
        assertEquals("h1", records.get(0).get("h1"));
        assertEquals("h2", records.get(0).get("h2"));
        assertEquals("a", records.get(1).get("h1"));
        assertEquals("b", records.get(1).get("h2"));
    }

    // ---- Header ignoreEmptyHeaders ----
    @Test
    public void testHeaderIgnoreEmptyHeadersTrue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("", "col").withIgnoreEmptyHeaders(true);
        CSVParser parser = new CSVParser(new StringReader("val1,val2\n"), format);
        Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertEquals(2, map.size());  // empty header included? Actually with ignoreEmptyHeaders=true, empty header is allowed but duplicate detection still applies?
        // In initializeHeader, if headerRecord[0] is empty, and ignoreEmptyHeaders true, then emptyHeader? The code:
        // containsHeader && (!emptyHeader || (emptyHeader && !this.format.getIgnoreEmptyHeaders()))
        // So if ignoreEmptyHeaders==true, the condition for exception is: containsHeader && (!emptyHeader || (emptyHeader && false)) => containsHeader && (!emptyHeader || false) => containsHeader && !emptyHeader.
        // If emptyHeader is true, then !emptyHeader is false, so no exception. So duplicate empty header is allowed. But the map contains the key "" with value 0 and "col" with 1.
        assertTrue(map.containsKey(""));
        assertEquals(Integer.valueOf(0), map.get(""));
        assertEquals(Integer.valueOf(1), map.get("col"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderIgnoreEmptyHeadersFalseWithDuplicateEmpty() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("", "").withIgnoreEmptyHeaders(false);
        new CSVParser(new StringReader("1,2\n"), format);
    }

    // ---- recordNumber / lineNumber ----
    @Test
    public void testRecordNumberAndLineNumber() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        assertEquals(0, parser.getRecordNumber());
        assertNotNull(parser.getCurrentLineNumber())  // line numbers depend on lexer, shouldn't be -1, maybe 1
        // consume first record
        CSvRecord r1 = parser.iterator().next();
        assertEquals(1, parser.getRecordNumber());
        r1 = parser.iterator().next();
        assertEquals(2, parser.getRecordNumber());
        r1 = parser.iterator().next();
        assertEquals(3, parser.getRecordNumber());
    }

    // ---- NullString handling ----
    @Test
    public void testNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = new CSVParser(new StringReader("abc,NULL\nNULL,def\nghi,jkl"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals("abc", records.get(0).get(0));
        assertNull(records.get(0).get(1));
        assertNull(records.get(1).get(0));
        assertEquals("def", records.get(1).get(1));
        assertEquals("ghi", records.get(2).get(0));
        assertEquals("jkl", records.get(2).get(1));
    }

    @Test
    public void testNullStringCaseInsensitive() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = new CSVParser(new StringReader("abc,null\nNull,def"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals("abc", records.get(0).get(0));
        assertNull(records.get(0).get(1));
        assertNull(records.get(1).get(0));
        assertEquals("def", records.get(1).get(1));
    }

    @Test
    public void testNullStringNotSet() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("NULL\nabc"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals("NULL", records.get(0).get(0));  // treated as literal
        assertEquals("abc", records.get(1).get(0));
    }

    // ---- Comments ----
    @Test
    public void testComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader("a,b\n#comment line\nc,d"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertNull(records.get(0).getComment());
        assertEquals("c", records.get(1).get(0));
        assertEquals("d", records.get(1).get(1));
        assertEquals("#comment line", records.get(1).getComment());  // comment attached to next record?
        // According to CSVParser.nextRecord: if a comment token is encountered, it's accumulated and then next record is parsed.
        // So the comment become property of the next data record. In this input: a,b (record 0, no comment), 
        // then #comment line (comment token) appended -> sb created, type set to TOKEN, loop continues, then reads c,d (EORECORD) -> record 1 comment "comment line".
        // So record 1 gets the comment.
        assertEquals("comment line", records.get(1).getComment().substring(1));  // strip the #? Actually the content includes the comment marker? Let's check token content: comment token content includes the comment itself without the marker? 
        // In Lexer.nextToken, when it's COMMENT, token.content contains the comment text after the marker. So the string should be "comment line". So we'll just assert.
    }

    @Test
    public void testMultipleComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader("#line1\n#line2\ndata"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("data", records.get(0).get(0));
        // comments are merged with LF
        assertEquals("line1\nline2", records.get(0).getComment());  // without markers
    }

    // ---- Invalid parse sequence ----
    @Test(expected = IOException.class)
    public void testInvalidParseSequence() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        CSVParser parser = new CSVParser(new StringReader("a,\"unclosed"), format);
        parser.getRecords();
    }

    // ---- Multi-line record ----
    @Test
    public void testMultiLineValue() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,\"line1\nline2\"\nc,d"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("line1\nline2", records.get(0).get(1));
        assertEquals("c", records.get(1).get(0));
        assertEquals("d", records.get(1).get(1));
    }

    // ---- Empty input ----
    @Test
    public void testEmptyInput() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        assertFalse(parser.iterator().hasNext());
        assertTrue(parser.getRecords().isEmpty());
    }

    // ---- Immediate end after header? ----
    @Test
    public void testHeaderOnlyInput() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1","h2");
        CSVParser parser = new CSVParser(new StringReader("h1,h2\n"), format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
        // header map is set
        assertNotNull(parser.getHeaderMap());
    }

    // ---- EOF with isReady ----
    @Test
    public void testEOFWithContent() throws IOException {
        // Input ends without newline after a value
        CSVParser parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
    }

    // ---- Iterator calls without hasNext ----
    @Test
    public void testIteratorNextWithoutHasNext() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("x,y\n"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        CSvRecord rec = it.next(); // should work
        assertNotNull(rec);
        assertEquals("x", rec.get(0));
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextAfterLast() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("x,y\n"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        it.next(); // consume only record
        it.next(); // should throw
    }

    // ---- helper to create temp csv file ----
    private File createTempCsvFile(String content) throws IOException {
        File file = File.createTempFile("test", ".csv");
        FileWriter fw = new FileWriter(file);
        fw.write(content);
        fw.close();
        return file;
    }
}
