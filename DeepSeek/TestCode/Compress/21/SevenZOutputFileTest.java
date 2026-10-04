package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SevenZOutputFileTest {

    private File tempFile;
    private SevenZOutputFile output;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".7z");
        output = new SevenZOutputFile(tempFile);
    }

    @After
    public void tearDown() {
        try {
            output.close();
        } catch (IOException ignored) {
            // ignore
        }
        tempFile.delete();
    }

    @Test
    public void testConstructor() throws IOException {
        assertNotNull(output);
        assertTrue(tempFile.exists());
        // The constructor seeks past the signature header; no error means success.
    }

    @Test
    public void testSetContentCompression() {
        // just verify no exception
        output.setContentCompression(SevenZMethod.COPY);
        output.setContentCompression(SevenZMethod.LZMA2);
        output.setContentCompression(SevenZMethod.BZIP2);
        output.setContentCompression(SevenZMethod.DEFLATE);
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        File input = new File(tempFile.getParent(), "someFile.txt");
        SevenZArchiveEntry entry = output.createArchiveEntry(input, "entryName");
        assertNotNull(entry);
        assertFalse(entry.isDirectory());
        assertEquals("entryName", entry.getName());
        // Last modified date should be set (not null)
        assertNotNull(entry.getLastModifiedDate());
    }

    @Test
    public void testCreateArchiveEntryDirectory() throws IOException {
        File dir = tempFile.getParentFile(); // use the temp dir
        SevenZArchiveEntry entry = output.createArchiveEntry(dir, "");
        assertTrue(entry.isDirectory());
        assertNotNull(entry.getLastModifiedDate());
    }

    @Test
    public void testPutArchiveEntry() throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        output.putArchiveEntry(entry);
        // internal list is private; test indirectly via finish not throwing due to missing stream info.
        output.closeArchiveEntry(); // entry with no data
        // This just verifies that no exception is thrown.
    }

    @Test
    public void testCloseArchiveEntryEmptyStream() throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("empty");
        output.putArchiveEntry(entry);
        output.closeArchiveEntry();
        // Entry should have no stream, size 0, crc false
        assertFalse(entry.hasStream());
        assertEquals(0, entry.getSize());
        assertEquals(0, entry.getCompressedSize());
        assertFalse(entry.getHasCrc());
    }

    @Test
    public void testCloseArchiveEntryWithData() throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("data");
        output.putArchiveEntry(entry);
        output.write(1);
        output.write(new byte[]{2, 3});
        output.write(new byte[]{4, 5, 6}, 0, 3);
        output.closeArchiveEntry();
        assertTrue(entry.hasStream());
        assertTrue(entry.getSize() > 0);
        assertTrue(entry.getCompressedSize() > 0);
        assertTrue(entry.getHasCrc());
        assertNotEquals(0, entry.getCrcValue());
        assertNotEquals(0, entry.getCompressedCrcValue());
    }

    @Test
    public void testWriteMethods() throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("methods");
        output.putArchiveEntry(entry);
        // write int
        output.write(0x7A);
        // write byte array full
        output.write("hello".getBytes("UTF-8"));
        // write part (len > 0)
        byte[] buffer = new byte[10];
        for (int i = 0; i < buffer.length; i++) buffer[i] = (byte) i;
        output.write(buffer, 2, 5);
        // write with len zero (should do nothing)
        output.write(buffer, 0, 0);
        output.closeArchiveEntry();
        assertTrue(entry.hasStream());
    }

    @Test
    public void testFinishWithoutEntries() throws IOException {
        output.finish();
        assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testFinishWithEntries() throws IOException {
        // add one entry with data
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("file");
        output.putArchiveEntry(entry);
        output.write(1);
        output.closeArchiveEntry();

        output.finish();
        long size = tempFile.length();
        assertTrue(size > 0);
    }

    @Test(expected = IOException.class)
    public void testDoubleFinishThrows() throws IOException {
        output.finish();
        output.finish(); // should throw IOException
    }

    @Test
    public void testCloseWithoutFinish() throws IOException {
        // Close should call finish internally
        output.close();
        assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testCloseAfterFinish() throws IOException {
        output.finish();
        output.close(); // should not throw
        assertTrue(tempFile.exists());
    }

    @Test(expected = IOException.class)
    public void testWriteAfterCloseThrows() throws IOException {
        output.close();
        output.write(1); // should throw because file is closed
    }

    @Test
    public void testComplexHeader() throws IOException {
        // create multiple entries with all sorts of metadata
        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("dir");
        entry1.setDirectory(true);
        entry1.setHasCrc(false);
        output.putArchiveEntry(entry1);
        output.closeArchiveEntry(); // empty stream

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("file.txt");
        entry2.setHasCreationDate(true);
        entry2.setCreationDate(new Date());
        entry2.setHasAccessDate(true);
        entry2.setAccessDate(new Date());
        entry2.setHasLastModifiedDate(true);
        entry2.setLastModifiedDate(new Date());
        entry2.setHasWindowsAttributes(true);
        entry2.setWindowsAttributes(1);
        output.putArchiveEntry(entry2);
        output.write("data".getBytes("UTF-8"));
        output.closeArchiveEntry();

        SevenZArchiveEntry entry3 = new SevenZArchiveEntry();
        entry3.setName("anti");
        entry3.setAntiItem(true);
        output.putArchiveEntry(entry3);
        output.closeArchiveEntry(); // empty anti item (directory-like but anti)

        SevenZArchiveEntry entry4 = new SevenZArchiveEntry();
        entry4.setName("emptyFile");
        // not directory, no stream => empty file
        output.putArchiveEntry(entry4);
        output.closeArchiveEntry();

        // finish should process all metadata properties without throwing
        output.finish();
        assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testNoNPEWhenWritingNullArray() throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("nulltest");
        output.putArchiveEntry(entry);
        try {
            output.write((byte[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
        // close entry to keep state consistent, but it may have written nothing
        try {
            output.closeArchiveEntry();
        } catch (IOException e) {
            // might have been corrupted; just catch
        }
    }
}
