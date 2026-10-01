package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Test
    public void testParseStringDefault() throws IOException {
        final String csv = "a,b,c\n1,2,3\nx,y,z";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Assert.assertFalse(parser.isClosed());

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(3, records.size());
        Assert.assertEquals("a", records.get(0).get(0));
        Assert.assertEquals("b", records.get(0).get(1));
        Assert.assertEquals("c", records.get(0).get(2));
        Assert.assertEquals(1, records.get(0).getRecordNumber());
        Assert.assertEquals("1", records.get(1).get(0));
        Assert.assertEquals("x", records.get(2).get(0));

        Assert.assertEquals(3, parser.getRecordNumber());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testParseReader() throws IOException {
        final Reader reader = new StringReader("A,B\nC,D");
        final CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("A", records.get(0).get(0));
        Assert.assertEquals("C", records.get(1).get(0));
        parser.close();
    }

    @Test
    public void testParseInputStream() throws IOException {
        final InputStream in = new ByteArrayInputStream("foo,bar\nhello,world".getBytes(UTF_8));
        final CSVParser parser = CSVParser.parse(in, UTF_8, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("foo", records.get(0).get(0));
        Assert.assertEquals("hello", records.get(1).get(0));
        parser.close();
    }

    @Test
    public void testParseFile() throws IOException {
        final File file = temporaryFolder.newFile("test.csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("name,age\nAlice,30\nBob,25".getBytes(UTF_8));
        }
        final CSVParser parser = CSVParser.parse(file, UTF_8, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(0, (int) headerMap.get("name"));
        Assert.assertEquals(1, (int) headerMap.get("age"));

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("Alice", records.get(0).get("name"));
        Assert.assertEquals("30", records.get(0).get("age"));
        Assert.assertEquals("Bob", records.get(1).get("name"));
        Assert.assertEquals("25", records.get(1).get("age"));
        parser.close();
    }

    @Test
    public void testParsePath() throws IOException {
        final Path path = temporaryFolder.newFile("test_path.csv").toPath();
        Files.write(path, "k1,k2\nv1,v2".getBytes(UTF_8));

        final CSVParser parser = CSVParser.parse(path, UTF_8, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("k1", records.get(0).get(0));
        Assert.assertEquals("v1", records.get(1).get(0));
        parser.close();
    }

    @Test
    public void testParseURL() throws IOException {
        final File file = temporaryFolder.newFile("test_url.csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("u1,u2\nu3,u4".getBytes(UTF_8));
        }
        final URL url = file.toURI().toURL();
        final CSVParser parser = CSVParser.parse(url, UTF_8, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("u1", records.get(0).get(0));
        Assert.assertEquals("u3", records.get(1).get(0));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        final File file = temporaryFolder.newFile("test_null.csv");
        CSVParser.parse(file, UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullStream() throws IOException {
        CSVParser.parse((InputStream) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullFormat() throws IOException {
        CSVParser.parse(new ByteArrayInputStream(new byte[0]), UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullPath() throws IOException {
        CSVParser.parse((Path) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullFormat() throws IOException {
        final Path path = temporaryFolder.newFile("test_path_null.csv").toPath();
        CSVParser.parse(path, UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("a,b", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        final File file = temporaryFolder.newFile("test_url_null.csv");
        CSVParser.parse(file.toURI().toURL(), null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        final File file = temporaryFolder.newFile("test_url_null2.csv");
        CSVParser.parse(file.toURI().toURL(), UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test
    public void testCustomOffsetAndRecordNumber() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT, 10, 5);
        Assert.assertEquals(4, parser.getRecordNumber());
        final CSVRecord r1 = parser.nextRecord();
        Assert.assertNotNull(r1);
        Assert.assertEquals(5, r1.getRecordNumber());
        Assert.assertEquals(10, r1.getCharacterPosition());

        final CSVRecord r2 = parser.nextRecord();
        Assert.assertNotNull(r2);
        Assert.assertEquals(6, r2.getRecordNumber());
        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testHeaderFromFirstLine() throws IOException {
        final String csv = "H1,H2,H3\nv1,v2,v3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(3, headerMap.size());
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("H1"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("H2"));
        Assert.assertEquals(Integer.valueOf(2), headerMap.get("H3"));

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("v1", records.get(0).get("H1"));
        Assert.assertEquals("v2", records.get(0).get("H2"));
        Assert.assertEquals("v3", records.get(0).get("H3"));
        parser.close();
    }

    @Test
    public void testEmptyHeaderWithEmptySource() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader());
        Assert.assertNotNull(parser.getHeaderMap());
        Assert.assertTrue(parser.getHeaderMap().isEmpty());
        Assert.assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }

    @Test
    public void testExplicitHeaderSkipHeaderRecord() throws IOException {
        final String csv = "header1,header2\nval1,val2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(true));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("A"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("B"));

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("val1", records.get(0).get("A"));
        Assert.assertEquals("val2", records.get(0).get("B"));
        parser.close();
    }

    @Test
    public void testExplicitHeaderWithoutSkipHeaderRecord() throws IOException {
        final String csv = "val1,val2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(false));
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("val1", records.get(0).get("A"));
        Assert.assertEquals("val2", records.get(0).get("B"));
        parser.close();
    }

    @Test
    public void testHeaderIgnoreCase() throws IOException {
        final String csv = "Name,Age\nJohn,28";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader().withIgnoreHeaderCase(true));
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        final CSVRecord record = records.get(0);
        Assert.assertEquals("John", record.get("NAME"));
        Assert.assertEquals("John", record.get("name"));
        Assert.assertEquals("28", record.get("AGE"));
        Assert.assertEquals("28", record.get("age"));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaderThrowsException() throws IOException {
        final String csv = "col,col\n1,2";
        CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateEmptyHeaderDisallowed() throws IOException {
        final String csv = ",,a\n1,2,3";
        CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(false));
    }

    @Test
    public void testDuplicateEmptyHeaderAllowed() throws IOException {
        final String csv = ",,a\n1,2,3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(true));
        final Map<String, Integer> map = parser.getHeaderMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(Integer.valueOf(1), map.get(""));
        Assert.assertEquals(Integer.valueOf(2), map.get("a"));
        parser.close();
    }

    @Test
    public void testHeaderMapIsNullWhenNoHeaderConfigured() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        Assert.assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testTrimAndNullString() throws IOException {
        final String csv = "  hello  ,  NULL  ,  world  ";
        final CSVFormat format = CSVFormat.DEFAULT.withTrim().withNullString("NULL");
        final CSVParser parser = CSVParser.parse(csv, format);
        final CSVRecord record = parser.nextRecord();
        Assert.assertNotNull(record);
        Assert.assertEquals("hello", record.get(0));
        Assert.assertNull(record.get(1));
        Assert.assertEquals("world", record.get(2));
        parser.close();
    }

    @Test
    public void testTrailingDelimiter() throws IOException {
        final String csv = "a,b,c,\n1,2,3,";
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        final CSVParser parser = CSVParser.parse(csv, format);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(3, records.get(0).size());
        Assert.assertEquals("a", records.get(0).get(0));
        Assert.assertEquals("b", records.get(0).get(1));
        Assert.assertEquals("c", records.get(0).get(2));
        Assert.assertEquals(3, records.get(1).size());
        Assert.assertEquals("1", records.get(1).get(0));
        Assert.assertEquals("2", records.get(1).get(1));
        Assert.assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    @Test
    public void testCommentHandling() throws IOException {
        final String csv = "# First comment\n# Second comment\na,b\n# Third comment\nc,d";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = CSVParser.parse(csv, format);

        final CSVRecord rec1 = parser.nextRecord();
        Assert.assertNotNull(rec1);
        Assert.assertEquals("First comment\nSecond comment", rec1.getComment());
        Assert.assertEquals("a", rec1.get(0));
        Assert.assertEquals("b", rec1.get(1));

        final CSVRecord rec2 = parser.nextRecord();
        Assert.assertNotNull(rec2);
        Assert.assertEquals("Third comment", rec2.getComment());
        Assert.assertEquals("c", rec2.get(0));
        Assert.assertEquals("d", rec2.get(1));

        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testGetFirstEndOfLineAndCurrentLineNumber() throws IOException {
        final String csv = "a,b\r\nc,d\r\ne,f";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Assert.assertEquals(0, parser.getCurrentLineNumber());
        final CSVRecord r1 = parser.nextRecord();
        Assert.assertNotNull(r1);
        Assert.assertEquals("\r\n", parser.getFirstEndOfLine());
        Assert.assertEquals(1, parser.getCurrentLineNumber());
        final CSVRecord r2 = parser.nextRecord();
        Assert.assertNotNull(r2);
        Assert.assertEquals(2, parser.getCurrentLineNumber());
        parser.close();
    }

    @Test
    public void testIteratorStandardTraversal() throws IOException {
        final String csv = "1,2\n3,4\n5,6";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.hasNext());
        final CSVRecord r1 = it.next();
        Assert.assertEquals("1", r1.get(0));

        final CSVRecord r2 = it.next();
        Assert.assertEquals("3", r2.get(0));

        Assert.assertTrue(it.hasNext());
        final CSVRecord r3 = it.next();
        Assert.assertEquals("5", r3.get(0));

        Assert.assertFalse(it.hasNext());
        parser.close();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextBeyondEnd() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        Assert.assertEquals("1", it.next().get(0));
        it.next();
    }

    @Test
    public void testIteratorNextWithoutHasNext() throws IOException {
        final CSVParser parser = CSVParser.parse("1\n2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        Assert.assertEquals("1", it.next().get(0));
        Assert.assertEquals("2", it.next().get(0));
        Assert.assertFalse(it.hasNext());
        parser.close();
    }

    @Test
    public void testIteratorClosedParserBehavior() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2\n3,4", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail("Expected NoSuchElementException when next() is called on closed parser iterator");
        } catch (final NoSuchElementException ignored) {
            // expected
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveThrowsException() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorCatchesIOException() {
        final Reader failingReader = new Reader() {
            @Override
            public int read(final char[] cbuf, final int off, final int len) throws IOException {
                throw new IOException("Simulated read error");
            }

            @Override
            public void close() throws IOException {
                // no-op
            }
        };

        try {
            final CSVParser parser = new CSVParser(failingReader, CSVFormat.DEFAULT);
            final Iterator<CSVRecord> it = parser.iterator();
            it.hasNext();
        } catch (final IOException e) {
            Assert.fail("CSVParser constructor should not have thrown IOException here: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidParseSequenceThrowsIOException() {
        final String csv = "a,\"b";
        try {
            final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
            parser.getRecords();
            Assert.fail("Expected IOException for unclosed quote sequence");
        } catch (final IOException e) {
            Assert.assertTrue(e.getMessage().contains("invalid parse sequence") || e.getMessage().contains("EOF"));
        }
    }

    @Test
    public void testEmptyFile() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertTrue(records.isEmpty());
        Assert.assertEquals(0, parser.getRecordNumber());
        parser.close();
    }

    @Test
    public void testMultipleClosesAreSafe() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT);
        parser.close();
        Assert.assertTrue(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testGetHeaderMapReturnsCopy() throws IOException {
        final CSVParser parser = CSVParser.parse("A,B\n1,2", CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> map1 = parser.getHeaderMap();
        final Map<String, Integer> map2 = parser.getHeaderMap();
        Assert.assertNotNull(map1);
        Assert.assertNotSame(map1, map2);
        Assert.assertEquals(map1, map2);
        map1.put("C", 3);
        Assert.assertFalse(parser.getHeaderMap().containsKey("C"));
        parser.close();
    }
}
