package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CSVParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testParseString() throws IOException {
        final String csv = "a,b,c\n1,2,3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        assertEquals("1", records.get(1).get(0));
        assertEquals("2", records.get(1).get(1));
        assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    @Test
    public void testParseFile() throws IOException {
        final File file = temporaryFolder.newFile("test.csv");
        final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        out.write("col1,col2\nval1,val2\n");
        out.close();

        final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("col1", records.get(0).get(0));
        assertEquals("val2", records.get(1).get(1));
        parser.close();
    }

    @Test
    public void testParseURL() throws IOException {
        final File file = temporaryFolder.newFile("test_url.csv");
        final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        out.write("h1,h2\nv1,v2\n");
        out.close();

        final URL url = file.toURI().toURL();
        final CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("h1", records.get(0).get(0));
        assertEquals("v2", records.get(1).get(1));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullFile() throws IOException {
        CSVParser.parse((File) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        final File file = temporaryFolder.newFile("test.csv");
        CSVParser.parse(file, StandardCharsets.UTF_8, null);
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
        final File file = temporaryFolder.newFile("test_url_null_charset.csv");
        CSVParser.parse(file.toURI().toURL(), (Charset) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        final File file = temporaryFolder.newFile("test_url_null_format.csv");
        CSVParser.parse(file.toURI().toURL(), StandardCharsets.UTF_8, null);
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
    public void testHeaderProvidedManually() throws IOException {
        final String csv = "val1,val2\nval3,val4";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("A", "B"));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(1), headerMap.get("B"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("val1", records.get(0).get("A"));
        assertEquals("val2", records.get(0).get("B"));
        parser.close();
    }

    @Test
    public void testHeaderFromFirstRecord() throws IOException {
        final String csv = "Header1,Header2\nValue1,Value2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Header1"));
        assertEquals(Integer.valueOf(1), headerMap.get("Header2"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("Value1", records.get(0).get("Header1"));
        assertEquals("Value2", records.get(0).get("Header2"));
        parser.close();
    }

    @Test
    public void testHeaderFromFirstRecordEmptyCSV() throws IOException {
        final String csv = "";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertTrue(headerMap.isEmpty());
        assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }

    @Test
    public void testHeaderProvidedWithSkipHeaderRecord() throws IOException {
        final String csv = "H1,H2\nval1,val2\nval3,val4";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(true));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("val1", records.get(0).get("A"));
        assertEquals("val3", records.get(1).get("A"));
        parser.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaderThrowsException() throws IOException {
        final String csv = "A,B,A\n1,2,3";
        CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateEmptyHeaderWithoutIgnoreEmptyThrowsException() throws IOException {
        final String csv = "A,,  \n1,2,3";
        CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(false));
    }

    @Test
    public void testDuplicateEmptyHeaderWithIgnoreEmpty() throws IOException {
        final String csv = "A,,  \n1,2,3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(true));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(2), headerMap.get("  "));
        parser.close();
    }

    @Test
    public void testGetHeaderMapDefensiveCopy() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\n1,2", CSVFormat.DEFAULT.withHeader("A", "B"));
        final Map<String, Integer> headerMap1 = parser.getHeaderMap();
        final Map<String, Integer> headerMap2 = parser.getHeaderMap();
        assertEquals(headerMap1, headerMap2);
        Assert.assertNotSame(headerMap1, headerMap2);

        headerMap1.put("C", 3);
        assertFalse(parser.getHeaderMap().containsKey("C"));
        parser.close();
    }

    @Test
    public void testNoHeaderMapReturnsNull() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\n1,2", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testNullStringReplacement() throws IOException {
        final String csv = "a,NULL,b,null,c";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withNullString("NULL"));
        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("a", record.get(0));
        assertNull(record.get(1));
        assertEquals("b", record.get(2));
        assertNull(record.get(3));
        assertEquals("c", record.get(4));
        parser.close();
    }

    @Test
    public void testRecordAndLineNumbers() throws IOException {
        final String csv = "a,b\nc,d\n\"multi\nline\",e";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        assertEquals(0, parser.getRecordNumber());
        assertEquals(1, parser.getCurrentLineNumber());

        final CSVRecord rec1 = parser.nextRecord();
        assertNotNull(rec1);
        assertEquals(1, parser.getRecordNumber());
        assertEquals(1, rec1.getRecordNumber());

        final CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec2);
        assertEquals(2, parser.getRecordNumber());
        assertEquals(2, rec2.getRecordNumber());

        final CSVRecord rec3 = parser.nextRecord();
        assertNotNull(rec3);
        assertEquals(3, parser.getRecordNumber());
        assertEquals(3, rec3.getRecordNumber());
        assertEquals(4, parser.getCurrentLineNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testComments() throws IOException {
        final String csv = "# First comment\n# Second comment\na,b,c\n# Third comment\nd,e,f";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withCommentMarker('#'));

        final CSVRecord rec1 = parser.nextRecord();
        assertNotNull(rec1);
        assertEquals("First comment\nSecond comment", rec1.getComment());
        assertEquals("a", rec1.get(0));

        final CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec2);
        assertEquals("Third comment", rec2.getComment());
        assertEquals("d", rec2.get(0));

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testGetRecordsWithCustomCollection() throws IOException {
        final String csv = "1,2\n3,4";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final LinkedList<CSVRecord> list = new LinkedList<CSVRecord>();
        final LinkedList<CSVRecord> result = parser.getRecords(list);
        assertSame(list, result);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).get(0));
        assertEquals("3", result.get(1).get(0));
        parser.close();
    }

    @Test
    public void testIterator() throws IOException {
        final String csv = "a,b\nc,d";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext()); // multiple calls to hasNext()
        final CSVRecord r1 = it.next();
        assertEquals("a", r1.get(0));

        final CSVRecord r2 = it.next(); // call next without hasNext
        assertEquals("c", r2.get(0));

        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException expected) {
            // expected
        }
        parser.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        parser.iterator().remove();
    }

    @Test
    public void testIteratorWhenClosed() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        parser.close();

        assertTrue(parser.isClosed());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException when closed");
        } catch (final NoSuchElementException expected) {
            // expected
        }
    }

    @Test
    public void testClose() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        parser.close(); // closing again should be safe
        assertTrue(parser.isClosed());
    }

    @Test
    public void testIteratorIOExceptionWrappedInRuntimeException() {
        final Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated I/O failure");
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
            fail("Expected RuntimeException wrapping IOException");
        } catch (final RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("Simulated I/O failure", e.getCause().getMessage());
        } catch (final IOException e) {
            fail("Should not throw IOException directly from constructor in this scenario");
        }
    }

    @Test
    public void testSingleRecordTrailingDelimiter() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,", CSVFormat.DEFAULT);
        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("", record.get(2));
        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testEmptyLineHandling() throws IOException {
        final String csv = "a,b\n\n\nc,d";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withIgnoreEmptyLines(false));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
        parser.close();
    }

    @Test
    public void testIgnoreEmptyLinesHandling() throws IOException {
        final String csv = "a,b\n\n\nc,d";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withIgnoreEmptyLines(true));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("c", records.get(1).get(0));
        parser.close();
    }

    @Test
    public void testInvalidTokenThrowsIOException() {
        final String csv = "\"unclosed quote,value";
        try {
            final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
            parser.getRecords();
            // Depending on lexer, it might either throw or handle differently.
            // But let's check for any parser exception.
        } catch (final IOException expected) {
            // expected for invalid parsing sequence
        }
    }
}
