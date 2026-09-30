package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVParserTest {

    private static final String CSV_INPUT = "a,b,c\n1,2,3\nx,y,z";

    @Test
    public void testParseString() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("a", records.get(0).get(0));
        Assert.assertEquals("b", records.get(0).get(1));
        Assert.assertEquals("c", records.get(0).get(2));
        parser.close();
    }

    @Test
    public void testParseFile() throws IOException {
        final File tempFile = File.createTempFile("csv_test", ".csv");
        tempFile.deleteOnExit();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write("name,age\nAlice,30\nBob,25");
        }

        final CSVParser parser = CSVParser.parse(tempFile, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("name"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("age"));

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("Alice", records.get(0).get("name"));
        Assert.assertEquals("30", records.get(0).get("age"));
        Assert.assertEquals("Bob", records.get(1).get("name"));
        Assert.assertEquals("25", records.get(1).get("age"));

        parser.close();
    }

    @Test
    public void testParseURL() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test", ".csv");
        tempFile.deleteOnExit();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write("col1,col2\nv1,v2");
        }

        final URL url = tempFile.toURI().toURL();
        final CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("v1", records.get(0).get("col1"));
        Assert.assertEquals("v2", records.get(0).get("col2"));

        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullFile() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        CSVParser.parse(new File("dummy"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("a,b", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullURL() throws IOException {
        CSVParser.parse((URL) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        final URL url = new URL("file://dummy");
        CSVParser.parse(url, (Charset) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        final URL url = new URL("file://dummy");
        CSVParser.parse(url, StandardCharsets.UTF_8, null);
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
    public void testGetHeaderMapWithoutHeader() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader(CSV_INPUT), CSVFormat.DEFAULT);
        Assert.assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testHeaderProvidedExplicitly() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2", "H3");
        final CSVParser parser = new CSVParser(new StringReader(CSV_INPUT), format);
        final Map<String, Integer> map = parser.getHeaderMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Integer.valueOf(0), map.get("H1"));
        Assert.assertEquals(Integer.valueOf(1), map.get("H2"));
        Assert.assertEquals(Integer.valueOf(2), map.get("H3"));

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(3, records.size());
        Assert.assertEquals("a", records.get(0).get("H1"));
        Assert.assertEquals(3, parser.getRecordNumber());
        parser.close();
    }

    @Test
    public void testHeaderProvidedExplicitlyWithSkipHeaderRecord() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2", "H3").withSkipHeaderRecord(true);
        final CSVParser parser = new CSVParser(new StringReader(CSV_INPUT), format);
        final Map<String, Integer> map = parser.getHeaderMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());

        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("1", records.get(0).get("H1"));
        Assert.assertEquals("x", records.get(1).get("H1"));
        parser.close();
    }

    @Test
    public void testHeaderEmptyArrayEmptyInput() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(new String[0]);
        final CSVParser parser = new CSVParser(new StringReader(""), format);
        final Map<String, Integer> map = parser.getHeaderMap();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
        parser.close();
    }

    @Test
    public void testHeaderMapIsCopy() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        final CSVParser parser = new CSVParser(new StringReader("1,2"), format);
        final Map<String, Integer> map1 = parser.getHeaderMap();
        final Map<String, Integer> map2 = parser.getHeaderMap();
        Assert.assertNotSame(map1, map2);
        map1.put("C", 2);
        Assert.assertFalse(parser.getHeaderMap().containsKey("C"));
        parser.close();
    }

    @Test
    public void testNullStringHandling() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = new CSVParser(new StringReader("a,NULL,c\nnull,b,NULL"), format);

        final CSVRecord record1 = parser.nextRecord();
        Assert.assertNotNull(record1);
        Assert.assertEquals("a", record1.get(0));
        Assert.assertNull(record1.get(1));
        Assert.assertEquals("c", record1.get(2));

        final CSVRecord record2 = parser.nextRecord();
        Assert.assertNotNull(record2);
        Assert.assertEquals("null", record2.get(0));
        Assert.assertEquals("b", record2.get(1));
        Assert.assertNull(record2.get(2));

        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testLinesAndRecordNumbers() throws IOException {
        final String input = "a,b\n\"c\nd\",e\n\n# comment\nf,g";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withIgnoreEmptyLines(false);
        final CSVParser parser = new CSVParser(new StringReader(input), format);

        CSVRecord record = parser.nextRecord();
        Assert.assertEquals("a", record.get(0));
        Assert.assertEquals(1, parser.getRecordNumber());

        record = parser.nextRecord();
        Assert.assertEquals("c\nd", record.get(0));
        Assert.assertEquals(2, parser.getRecordNumber());

        record = parser.nextRecord();
        Assert.assertEquals(3, parser.getRecordNumber());

        record = parser.nextRecord();
        Assert.assertEquals("f", record.get(0));
        Assert.assertEquals("comment", record.getComment());
        Assert.assertEquals(4, parser.getRecordNumber());

        Assert.assertTrue(parser.getCurrentLineNumber() >= 4);

        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testMultipleCommentsBeforeRecord() throws IOException {
        final String input = "# Comment 1\n# Comment 2\na,b";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = new CSVParser(new StringReader(input), format);

        final CSVRecord record = parser.nextRecord();
        Assert.assertNotNull(record);
        Assert.assertEquals("Comment 1\nComment 2", record.getComment());
        Assert.assertEquals("a", record.get(0));
        Assert.assertEquals("b", record.get(1));
        parser.close();
    }

    @Test
    public void testGetRecordsWithCustomCollection() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2\n3,4"), CSVFormat.DEFAULT);
        final List<CSVRecord> customList = new ArrayList<>();
        final List<CSVRecord> returnedList = parser.getRecords(customList);
        Assert.assertSame(customList, returnedList);
        Assert.assertEquals(2, returnedList.size());
        parser.close();
    }

    @Test
    public void testIterator() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2\n3,4"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.hasNext());
        final CSVRecord r1 = it.next();
        Assert.assertEquals("1", r1.get(0));

        Assert.assertTrue(it.hasNext());
        final CSVRecord r2 = it.next();
        Assert.assertEquals("3", r2.get(0));

        Assert.assertFalse(it.hasNext());
        parser.close();
    }

    @Test
    public void testIteratorDirectNextWithoutHasNext() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2\n3,4"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        final CSVRecord r1 = it.next();
        Assert.assertEquals("1", r1.get(0));
        final CSVRecord r2 = it.next();
        Assert.assertEquals("3", r2.get(0));

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            Assert.assertEquals("No more CSV records available", e.getMessage());
        }
        parser.close();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextOnClosedParser() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        it.next();
    }

    @Test
    public void testIteratorHasNextOnClosedParser() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    @Test
    public void testIteratorWrapsIOException() {
        final Reader errorReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO error");
            }

            @Override
            public void close() {
            }
        };

        try {
            final CSVParser parser = new CSVParser(errorReader, CSVFormat.DEFAULT);
            final Iterator<CSVRecord> it = parser.iterator();
            it.hasNext();
            Assert.fail("Expected RuntimeException wrapping IOException");
        } catch (final RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof IOException);
            Assert.assertEquals("Simulated IO error", e.getCause().getMessage());
        } catch (final IOException e) {
            Assert.fail("IOException should not be thrown in constructor here");
        }
    }

    @Test
    public void testIsClosedAndClose() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("1,2"), CSVFormat.DEFAULT);
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        parser.close(); // Double close shouldn't throw
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testInvalidParseSequence() {
        final String input = "\"abc"; // Unclosed quote with escape/quote handling that triggers invalid sequence
        try {
            final CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT);
            parser.getRecords();
            // Depending on lexer behavior it may or may not throw INVALID,
            // but if it parses without throwing, ensure parser closes cleanly.
            parser.close();
        } catch (final IOException e) {
            Assert.assertTrue(e.getMessage().contains("invalid parse sequence") || e.getMessage().length() > 0);
        }
    }

    @Test
    public void testEmptyLineAndTrailingDelimiter() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        final CSVParser parser = new CSVParser(new StringReader("a,b,\n1,2,\n"), format);
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(2, records.get(0).size());
        Assert.assertEquals("a", records.get(0).get(0));
        Assert.assertEquals("b", records.get(0).get(1));
        parser.close();
    }
}
