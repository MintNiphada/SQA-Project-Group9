package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        File file = temporaryFolder.newFile("test.csv");
        CSVParser.parse(file, null);
    }

    @Test
    public void testParseFileSuccess() throws IOException {
        File file = temporaryFolder.newFile("test.csv");
        FileWriter writer = new FileWriter(file);
        writer.write("col1,col2\nval1,val2\n");
        writer.close();

        CSVParser parser = CSVParser.parse(file, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("col1", records.get(0).get(0));
        Assert.assertEquals("val2", records.get(1).get(1));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("a,b,c", null);
    }

    @Test
    public void testParseStringSuccess() throws IOException {
        CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("a", records.get(0).get(0));
        Assert.assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        File file = temporaryFolder.newFile("url_test.csv");
        CSVParser.parse(file.toURI().toURL(), null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        File file = temporaryFolder.newFile("url_test.csv");
        CSVParser.parse(file.toURI().toURL(), StandardCharsets.UTF_8, null);
    }

    @Test
    public void testParseURLSuccess() throws IOException {
        File file = temporaryFolder.newFile("url_test.csv");
        FileWriter writer = new FileWriter(file);
        writer.write("h1,h2\nv1,v2");
        writer.close();

        CSVParser parser = CSVParser.parse(file.toURI().toURL(), StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("h1", records.get(0).get(0));
        Assert.assertEquals("v2", records.get(1).get(1));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("a,b,c"), null);
    }

    @Test
    public void testGetHeaderMapNoHeader() throws IOException {
        CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        Assert.assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testGetHeaderMapWithHeaderAutoRead() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = CSVParser.parse("colA,colB,colC\n1,2,3\n4,5,6", format);

        Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(3, headerMap.size());
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("colA"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("colB"));
        Assert.assertEquals(Integer.valueOf(2), headerMap.get("colC"));

        // Test mutating copy does not modify internal map
        headerMap.put("extra", 99);
        Assert.assertFalse(parser.getHeaderMap().containsKey("extra"));

        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("1", records.get(0).get("colA"));
        Assert.assertEquals("6", records.get(1).get("colC"));
        parser.close();
    }

    @Test
    public void testGetHeaderMapWithPredefinedHeaderWithoutSkip() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVParser parser = CSVParser.parse("val1,val2\nval3,val4", format);

        Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(2, headerMap.size());
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("Header1"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("Header2"));

        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("val1", records.get(0).get("Header1"));
        parser.close();
    }

    @Test
    public void testGetHeaderMapWithPredefinedHeaderWithSkip() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Header1", "Header2").withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse("ignored1,ignored2\nval1,val2", format);

        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("val1", records.get(0).get("Header1"));
        Assert.assertEquals("val2", records.get(0).get("Header2"));
        parser.close();
    }

    @Test
    public void testGetHeaderMapEmptyFileAutoHeader() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = CSVParser.parse("", format);

        Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertTrue(headerMap.isEmpty());
        Assert.assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }

    @Test
    public void testNullStringHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = CSVParser.parse("NULL,hello,null,world", format);

        CSVRecord record = parser.nextRecord();
        Assert.assertNotNull(record);
        Assert.assertNull(record.get(0));
        Assert.assertEquals("hello", record.get(1));
        Assert.assertNull(record.get(2)); // case-insensitive check
        Assert.assertEquals("world", record.get(3));
        parser.close();
    }

    @Test
    public void testCommentsAndRecordNumbers() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String csv = "# First comment line\n# Second comment line\na,b,c\n# Middle comment\nd,e,f\n";
        CSVParser parser = CSVParser.parse(csv, format);

        Assert.assertEquals(0, parser.getRecordNumber());

        CSVRecord rec1 = parser.nextRecord();
        Assert.assertNotNull(rec1);
        Assert.assertEquals(1, parser.getRecordNumber());
        Assert.assertEquals(1, rec1.getRecordNumber());
        Assert.assertEquals("First comment line\nSecond comment line", rec1.getComment());
        Assert.assertEquals("a", rec1.get(0));

        CSVRecord rec2 = parser.nextRecord();
        Assert.assertNotNull(rec2);
        Assert.assertEquals(2, parser.getRecordNumber());
        Assert.assertEquals(2, rec2.getRecordNumber());
        Assert.assertEquals("Middle comment", rec2.getComment());
        Assert.assertEquals("d", rec2.get(0));

        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testGetCurrentLineNumber() throws IOException {
        String csv = "a,b\n\"multi\nline\",value\nx,y";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        CSVRecord rec1 = parser.nextRecord();
        Assert.assertNotNull(rec1);
        Assert.assertEquals(1, parser.getCurrentLineNumber());

        CSVRecord rec2 = parser.nextRecord();
        Assert.assertNotNull(rec2);
        Assert.assertEquals(3, parser.getCurrentLineNumber());

        CSVRecord rec3 = parser.nextRecord();
        Assert.assertNotNull(rec3);
        Assert.assertEquals(4, parser.getCurrentLineNumber());

        Assert.assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testIteratorStandardTraversal() throws IOException {
        CSVParser parser = CSVParser.parse("1,2\n3,4", CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();

        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.hasNext()); // multiple calls to hasNext() should not advance
        CSVRecord r1 = it.next();
        Assert.assertEquals("1", r1.get(0));
        Assert.assertEquals("2", r1.get(1));

        Assert.assertTrue(it.hasNext());
        CSVRecord r2 = it.next();
        Assert.assertEquals("3", r2.get(0));
        Assert.assertEquals("4", r2.get(1));

        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // expected
        }
        parser.close();
    }

    @Test
    public void testIteratorNextWithoutHasNext() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        CSVRecord r = it.next();
        Assert.assertEquals("a", r.get(0));

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // expected
        }
        parser.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveThrowsException() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    @Test
    public void testIsClosedAndIteratorBehaviorWhenClosed() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());

        Iterator<CSVRecord> it = parser.iterator();
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException when calling next() on closed parser");
        } catch (NoSuchElementException e) {
            Assert.assertTrue(e.getMessage().contains("closed"));
        }
    }

    @Test
    public void testIteratorWrapsIOException() {
        Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO failure");
            }

            @Override
            public void close() throws IOException {
            }
        };

        try {
            CSVParser parser = new CSVParser(failingReader, CSVFormat.DEFAULT);
            Iterator<CSVRecord> it = parser.iterator();
            it.hasNext();
            Assert.fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof IOException);
        } catch (IOException e) {
            Assert.fail("IOException should not be thrown directly here");
        }
    }

    @Test
    public void testEmptyAndWhitespaceInput() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertTrue(records.isEmpty());
        parser.close();

        CSVParser parser2 = CSVParser.parse("\r\n\r\n", CSVFormat.DEFAULT);
        List<CSVRecord> records2 = parser2.getRecords();
        Assert.assertTrue(records2.isEmpty());
        parser2.close();
    }

    @Test
    public void testTrailingDelimiter() throws IOException {
        CSVParser parser = CSVParser.parse("a,b,\nc,d,", CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(3, records.get(0).size());
        Assert.assertEquals("", records.get(0).get(2));
        Assert.assertEquals(3, records.get(1).size());
        Assert.assertEquals("", records.get(1).get(2));
        parser.close();
    }

    @Test
    public void testForEachLoopSupport() throws IOException {
        CSVParser parser = CSVParser.parse("1\n2\n3", CSVFormat.DEFAULT);
        int count = 0;
        for (CSVRecord record : parser) {
            count++;
            Assert.assertEquals(String.valueOf(count), record.get(0));
        }
        Assert.assertEquals(3, count);
        parser.close();
    }
}
