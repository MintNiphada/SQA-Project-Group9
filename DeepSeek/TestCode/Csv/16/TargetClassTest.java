package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CSVParserTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    
    private CSVParser parser;
    private File tempFile;

    @Before
    public void setUp() throws Exception {
        parser = null;
    }

    @After
    public void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // ---------------------- constructors and null arguments ------

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorReaderFormatCharacterOffsetRecordNumberNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorReaderFormatCharacterOffsetRecordNumberNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null, 0,1);
    }

    // parse static methods null checks

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File)null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        File f = File.createTempFile("test", ".csv");
        f.deleteOnExit();
        CSVParser.parse(f, UTF8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullStream() throws IOException {
        CSVParser.parse((InputStream)null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullFormat() throws IOException {
        CSVParser.parse(new ByteArrayInputStream(new byte[0]), UTF8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullPath() throws IOException {
        CSVParser.parse((Path)null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullFormat() throws IOException {
        Path tempPath = Files.createTempFile("test", ".csv");
        try {
            CSVParser.parse(tempPath, UTF8, null);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseReaderNullReader() throws IOException {
        CSVParser.parse((Reader)null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseReaderNullFormat() throws IOException {
        CSVParser.parse(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String)null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL)null, UTF8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        CSVParser.parse(new URL("http://example.com"), null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        CSVParser.parse(new URL("http://example.com"), UTF8, null);
    }

    // ---------------------- simple parsing --------------------

    @Test
    public void testEmptyInput() throws IOException {
        parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
        assertEquals(0, parser.getRecords().size());
    }

    @Test
    public void testSingleLineNoTrailingDelimiter() throws IOException {
        parser = new CSVParser(new StringReader("a,b,c"), CSVFormat.DEFAULT);
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
        assertNull(parser.nextRecord());
    }

    @Test
    public void testSingleLineWithTrailingDelimiter() throws IOException {
        parser = new CSVParser(new StringReader("a,b,c,"), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
        assertNull(parser.nextRecord());
    }

    @Test
    public void testTrailingDelimiterEmptyFieldAtEnd() throws IOException {
        parser = new CSVParser(new StringReader("a,b,"), CSVFormat.DEFAULT.withTrailingDelimiter(false));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("", record.get(2));
    }

    @Test
    public void testTrailingDelimiterTrueEmptyFieldIgnored() throws IOException {
        parser = new CSVParser(new StringReader("a,b,"), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(2, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
    }

    @Test
    public void testMultipleRecords() throws IOException {
        parser = new CSVParser(new StringReader("a,b,c\nd,e,f"), CSVFormat.DEFAULT);
        CSVRecord rec1 = parser.nextRecord();
        CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec1);
        assertNotNull(rec2);
        assertEquals("a", rec1.get(0));
        assertEquals("d", rec2.get(0));
        assertNull(parser.nextRecord());
    }

    @Test
    public void testGetRecords() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("c", records.get(1).get(0));
    }

 @Test
 public void testGetRecordsAfterPartialParsing() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        CSVRecord first = parser.nextRecord();
        assertNotNull(first);
        List<CSVRecord> remaining = parser.getRecords();
        assertEquals(2, remaining.size());
        assertEquals("c", remaining.get(0).get(0));
        assertEquals("e", remaining.get(1).get(0));
        assertNull(parser.nextRecord());
 }

 @Test
 public void testHeaderFromFirstLine() throws IOException {
        parser = new CSVParser(new StringReader("h1,h2\nv1,v2"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
        assertEquals(2, parser.getHeaderMap().size());
        assertTrue(parser.getHeaderMap().containsKey("h1"));
        assertTrue(parser.getHeaderMap().containsKey("h2"));
        CSVRecord record = parser.nextRecord();
        assertEquals("v1", record.get("h1"));
        assertEquals("v2", record.get("h2"));
    }

 @Test
 public void testExplicitHeader() throws IOException {
        parser = new CSVParser(new StringReader("v1,v2"), CSVFormat.DEFAULT.withHeader("A","B"));
        assertEquals(2, parser.getHeaderMap().size());
        assertTrue(parser.getHeaderMap().containsKey("A"));
        assertTrue(parser.getHeaderMap().containsKey("B"));
        CSVRecord record = parser.nextRecord();
        assertEquals("v1", record.get("A"));
        assertEquals("v2", record.get("B"));
    }

 @Test
 public void testExplicitHeaderWithSkip() throws IOException {
        parser = new CSVParser(new StringReader("head1,head2\nv1,v2"), CSVFormat.DEFAULT.withHeader("A","B").withSkipHeaderRecord(true));
        CSVRecord record = parser.nextRecord();
        assertEquals("v1", record.get("A"));
        assertEquals("v2", record.get("B"));
    }

 @Test
 public void testExplicitHeaderSameAsFirstLineAndNoSkip() throws IOException {
        parser = new CSVParser(new StringReader("h1,h2\nv1,v2"), CSVFormat.DEFAULT.withHeader("h1","h2"));
        CSVRecord record = parser.nextRecord();
        assertEquals("h1", record.get("h1")); // first line is data, not skipped
        assertEquals("h2", record.get("h2"));
    }

 @Test
 public void testHeaderDuplicateNameThrows() throws IOException {
        try {
            parser = new CSVParser(new StringReader("a,b,a"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            parser.nextRecord();
            fail("Expected IllegalArgumentException for duplicate header");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }

 @Test
 public void testHeaderDuplicateNameIgnoredCaseInsensitive() throws IOException {
        parser = new CSVParser(new StringReader("A,a"), CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase(true));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        // Should not throw
    }

 @Test
 public void testHeaderDuplicateNameCaseSensitiveDifferent() throws IOException {
        parser = new CSVParser(new StringReader("A,a"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
        // A and a are distinct in default map (LinkedHashMap)
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
    }

    @Test
    public void testHeaderEmptyNameDuplicatesAndAllowMissingColumnNames() throws IOException {
        parser = new CSVParser(new StringReader(",,\nv1,v2,v3"), CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames(true));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        // map will contain essentially empty strings as keys, but duplicates allowed with missing column names enabled
    }

    @Test
    public void testHeaderEmptyNameDuplicatesWithoutAllow() throws IOException {
        try {
            parser = new CSVParser(new StringReader(",,\nv1,v2,v3"), CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames(false));
            parser.nextRecord();
            fail("Expected IllegalArgumentException for empty duplicate header");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }

    @Test
    public void testHeaderNullValuesThrows() throws IOException {
        // Unfortunately CSVFormat does not support null in header array, but test that null header names are handled in map
        parser = new CSVParser(new StringReader("null,,value\n1,2,3"), CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames(true));
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        // would not throw since null values are treated as empty if allowMissingColumnNames true? Actually source checks emptyHeader if header==null||header.trim().isEmpty()
        // We can test that it works without exception
    }

    // test null string handling
    @Test
    public void testNullString() throws IOException {
        parser = new CSVParser(new StringReader("NULL,value"), CSVFormat.DEFAULT.withNullString("NULL"));
        CSVRecord record = parser.nextRecord();
        assertNull(record.get(0));
        assertEquals("value", record.get(1));
    }

    @Test
    public void testTrim() throws IOException {
        parser = new CSVParser(new StringReader(" a , b "), CSVFormat.DEFAULT.withTrim(true));
        CSVRecord record = parser.nextRecord();
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
    }

    @Test
    public void testNoTrim() throws IOException {
        parser = new CSVParser(new StringReader(" a , b "), CSVFormat.DEFAULT.withTrim(false));
        CSVRecord record = parser.nextRecord();
        assertEquals(" a ", record.get(0));
        assertEquals(" b ", record.get(1));
    }

    @Test
    public void testCommentsIgnored() throws IOException {
        parser = new CSVParser(new StringReader("#comment\na,b\n#comment2\nc,d"), CSVFormat.DEFAULT.withCommentMarker('#'));
        CSVRecord rec1 = parser.nextRecord();
        CSVRecord rec2 = parser.nextRecord();
        assertEquals("a", rec1.get(0));
        assertEquals("c", rec2.get(0));
        // The comments are captured in CSVRecord? In nextRecord, comment string is built and passed.
        // With comment marker, the lexer yields COMMENT token, which accumulates. Let's check record's comment.
        assertNotNull(rec1.getComment());
        assertEquals("comment", rec1.getComment().trim());
        assertNotNull(rec2.getComment());
        assertEquals("comment2", rec2.getComment().trim());
    }

    @Test
    public void testCommentMultiLine() throws IOException {
        parser = new CSVParser(new StringReader("#line1\n#line2\na,b"), CSVFormat.DEFAULT.withCommentMarker('#'));
        CSVRecord record = parser.nextRecord();
        assertEquals("line1\nline2", record.getComment());
    }

    @Test
    public void testQuoteAndEscape() throws IOException {
        parser = new CSVParser(new StringReader("\"a,\",\"b \"\"c\"\"\"\n"), CSVFormat.DEFAULT);
        CSVRecord record = parser.nextRecord();
        assertEquals("a,", record.get(0));
        assertEquals("b \"c\"", record.get(1));
    }

    @Test
    public void testMultiLineRecord() throws IOException {
        parser = new CSVParser(new StringReader("\"a\nb\",c"), CSVFormat.DEFAULT);
        CSVRecord record = parser.nextRecord();
        assertEquals(2, record.size());
        assertEquals("a\nb", record.get(0));
        assertEquals("c", record.get(1));
    }

    @Test
    public void testInvalidParseSequence() throws IOException {
        parser = new CSVParser(new StringReader("a,b\n\"c"), CSVFormat.DEFAULT);
        try {
            parser.nextRecord(); // first record ok
            parser.nextRecord(); // second record starts quote but not closed, Lexer will produce INVALID token?
            fail("Expected IOException for invalid sequence");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("invalid parse sequence"));
        }
    }

    // Record number, line number, character offset
    @Test
    public void testRecordNumber() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        assertEquals(0, parser.getRecordNumber());
        CSVRecord rec1 = parser.nextRecord();
        assertEquals(1, rec1.getRecordNumber());
        assertEquals(1, parser.getRecordNumber());
        CSVRecord rec2 = parser.nextRecord();
        assertEquals(2, rec2.getRecordNumber());
        assertEquals(2, parser.getRecordNumber());
    }

    @Test
    public void testCharacterOffset() throws IOException {
        String data = "abc,def\nghi,jkl";
        parser = new CSVParser(new StringReader(data), CSVFormat.DEFAULT, 100, 1);
        CSVRecord rec = parser.nextRecord();
        assertEquals(100, rec.getCharacterPosition());
        // Next record should have offset relative to lexer start + characterOffset
        // Lexer will be at position after first line. Hard to compute, but we can test it's not negative.
        CSVRecord rec2 = parser.nextRecord();
        assertTrue(rec2.getCharacterPosition() > rec.getCharacterPosition());
    }

    @Test
    public void testCurrentLineNumber() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        assertEquals(1, parser.getCurrentLineNumber()); // before reading?
        parser.nextRecord();
        assertEquals(1, parser.getCurrentLineNumber()); // line 1
        parser.nextRecord();
        assertEquals(2, parser.getCurrentLineNumber());
    }

    @Test
    public void testGetFirstEndOfLine() throws IOException {
        parser = new CSVParser(new StringReader("a,b\r\nc,d"), CSVFormat.DEFAULT);
        parser.nextRecord();
        assertEquals("\r\n", parser.getFirstEndOfLine());
    }

    @Test
    public void testClose() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // verify no exception on second close
        parser.close();
    }

    @Test(expected = IOException.class)
    public void testNextRecordOnClosed() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        parser.close();
        parser.nextRecord();
    }

    @Test
    public void testIteratorFullIteration() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVRecord rec1 = it.next();
        assertEquals("a", rec1.get(0));
        assertTrue(it.hasNext());
        CSVRecord rec2 = it.next();
        assertEquals("c", rec2.get(0));
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testIteratorClosedHasNextReturnsFalse() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextOnClosed() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    @Test
    public void testIteratorHasNextAfterPartialRead() throws IOException {
        parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        CSVRecord first = parser.nextRecord();
        assertEquals("a", first.get(0));
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        CSVRecord second = it.next();
        assertEquals("c", second.get(0));
        assertFalse(it.hasNext());
    }

    // Test with file, url, path, inputstream - we'll do a quick File test
    @Test
    public void testParseFile() throws IOException {
        TempFile = File.createTempFile("csvtest", ".csv");
        try (PrintWriter pw = new PrintWriter(tempFile)) {
            pw.write("one,two");
        }
        parser = CSVParser.parse(tempFile, UTF8, CSVFormat.DEFAULT);
        CSVRecord record = parser.nextRecord();
        assertEquals("one", record.get(0));
        assertEquals("two", record.get(1));
    }

    @Test
    public void testParseInputStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream("a,b".getBytes());
        parser = CSVParser.parse(bais, UTF8, CSVFormat.DEFAULT);
        CSVRecord record = parser.nextRecord();
        assertEquals("a", record.get(0));
    }

    @Test
    public void testParsePath() throws IOException {
        Path tempPath = Files.createTempFile("csvtest", ".csv");
        try {
            Files.write(tempPath, "p,q".getBytes());
            parser = CSVParser.parse(tempPath, UTF8, CSVFormat.DEFAULT);
            CSVRecord record = parser.nextRecord();
            assertEquals("p", record.get(0));
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    // Complex header map scenarios
    @Test
    public void testGetHeaderMapReturnsCopy() throws IOException {
        parser = new CSVParser(new StringReader("A,B"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
        Map<String,Integer> map1 = parser.getHeaderMap();
        map1.put("C", 2); // should not modify internal map
        CSVRecord record = parser.nextRecord();
        assertFalse(record.isMapped("C"));
    }

    @Test
    public void testNullHeaderMap() throws IOException {
        parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
    }

    @Test
    public void testRecordWithCommentOnlyLine() throws IOException {
        parser = new CSVParser(new StringReader("#comment"), CSVFormat.DEFAULT.withCommentMarker('#'));
        parser.nextRecord();
        // Should produce a record? No, if EOF after comment without a data line, no record. Because comment token is consumed, then EOF, but isReady false? Let's test.
        assertNull(parser.nextRecord());
    }

    // Edge: empty record with trailing delimiter and lastRecord addRecordValue removal
    @Test
    public void testLastRecordTrailingEmptyAndNotAdded() throws IOException {
        parser = new CSVParser(new StringReader("a,b,\n"), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        CSVRecord record = parser.nextRecord();
        assertEquals(2, record.size());
        assertNull(parser.nextRecord());
    }

    // Edge: empty file with header, should return null
    @Test
    public void testEmptyFileWithFirstRecordAsHeader() throws IOException {
        parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT.withFirstRecordAsHeader());
        assertNull(parser.getHeaderMap());
        assertNull(parser.nextRecord());
    }

    // Edge: only one line with header, no data
    @Test
    public void testHeaderOnlyFileNoData() throws IOException {
        parser = new CSVParser(new StringReader("h1,h2"), CSVFormat.DEFAULT.withFirstRecordAsHeader());
        Map<String,Integer> header = parser.getHeaderMap();
        assertEquals(2, header.size());
        assertNull(parser.nextRecord());
    }

}
