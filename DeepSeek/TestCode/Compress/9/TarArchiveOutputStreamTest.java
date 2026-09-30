package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;

public class TarArchiveOutputStreamTest {

    private String createLongName(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    @Test
    public void testDefaultConstructor() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockSize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockAndRecordSize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512, 256);
        assertEquals(256, tos.getRecordSize());
    }

    @Test
    public void testSetLongFileMode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // no exception, mode set
    }

    @Test
    public void testFinishNormal() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        assertTrue(bos.size() > 0);
        try {
            tos.finish();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testFinishWithUnclosedEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        try {
            tos.finish();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testCloseWithoutFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.close();
        assertTrue(bos.size() > 0);
        tos.close(); // should not throw
    }

    @Test
    public void testCloseAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.close();
        tos.close(); // should not throw
    }

    @Test
    public void testCloseWithUnclosedEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        try {
            tos.close();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testPutArchiveEntryNormal() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        assertTrue(bos.size() > 0);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryDirectory() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("dir/", true);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry(); // should succeed without writing data
    }

    @Test
    public void testPutArchiveEntryWhenFinished() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        try {
            tos.putArchiveEntry(entry);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testPutArchiveEntryLongNameError() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        String longName = createLongName(TarConstants.NAMELEN);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        try {
            tos.putArchiveEntry(entry);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testPutArchiveEntryLongNameTruncate() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = createLongName(TarConstants.NAMELEN);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryLongNameGnu() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = createLongName(TarConstants.NAMELEN);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        assertTrue(bos.size() > 0);
    }

    @Test
    public void testPutArchiveEntryNullEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        try {
            tos.putArchiveEntry(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCloseArchiveEntryNormal() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
    }

    @Test
    public void testCloseArchiveEntryWithAssembly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512, 256);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.closeArchiveEntry(); // should pad and write record
    }

    @Test
    public void testCloseArchiveEntryPremature() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        try {
            tos.closeArchiveEntry();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testCloseArchiveEntryNoEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        try {
            tos.closeArchiveEntry();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testCloseArchiveEntryWhenFinished() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        try {
            tos.closeArchiveEntry();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testWriteWithinSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        assertEquals(50, tos.getBytesWritten());
        tos.write(new byte[50]);
        assertEquals(100, tos.getBytesWritten());
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteExceedingSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        try {
            tos.write(new byte[101]);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testWriteWithAssembly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512, 256);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(300);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.write(new byte[100]);
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteZeroBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.write(new byte[0], 0, 0);
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteWithOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tos.write(data, 5, 10);
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteLargeChunk() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512, 256);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(600);
        tos.putArchiveEntry(entry);
        tos.write(new byte[600]);
        tos.closeArchiveEntry();
    }

    @Test
    public void testFlush() throws IOException {
        final boolean[] flushed = {false};
        OutputStream spy = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
            @Override
            public void flush() throws IOException {
                flushed[0] = true;
            }
        };
        TarArchiveOutputStream tos = new TarArchiveOutputStream(spy);
        tos.flush();
        assertTrue(flushed[0]);
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        ArchiveEntry entry = tos.createArchiveEntry(tempFile, "entryName");
        assertTrue(entry instanceof TarArchiveEntry);
        assertEquals("entryName", entry.getName());
    }

    @Test
    public void testCreateArchiveEntryWhenFinished() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try {
            tos.createArchiveEntry(tempFile, "entryName");
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }
}
