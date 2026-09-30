package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream taos;

    @Before
    public void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        taos = new TarArchiveOutputStream(baos);
    }

    @Test
    public void testDefaultConstructor() {
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, taos.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockSize() {
        TarArchiveOutputStream t = new TarArchiveOutputStream(new ByteArrayOutputStream(), 2048);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, t.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockAndRecordSize() {
        TarArchiveOutputStream t = new TarArchiveOutputStream(new ByteArrayOutputStream(), 2048, 1024);
        assertEquals(1024, t.getRecordSize());
    }

    @Test
    public void testSetLongFileMode() {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // no exception, just sets field
    }

    @Test
    public void testFinish() throws Exception {
        // Use a custom OutputStream to capture writes
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        OutputStream os = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                captured.write(b);
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                captured.write(b, off, len);
            }
        };
        TarArchiveOutputStream t = new TarArchiveOutputStream(os);
        t.finish();
        t.close(); // close to flush, but close also calls finish again, so we'll get 4 EOF records.
        // We'll just check that the last two records are zeros. But close adds two more.
        // Instead, call finish and then flush the buffer manually? Not possible.
        // We'll test that after finish and close, the output ends with 4 zero records.
        byte[] output = captured.toByteArray();
        int recordSize = t.getRecordSize();
        assertTrue(output.length >= 4 * recordSize);
        byte[] zeroRecord = new byte[recordSize];
        // last four records should be zeros
        for (int i = output.length - 4 * recordSize; i < output.length; i += recordSize) {
            assertArrayEquals(zeroRecord, Arrays.copyOfRange(output, i, i + recordSize));
        }
    }

    @Test
    public void testClose() throws Exception {
        final boolean[] closed = {false};
        OutputStream os = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
            @Override
            public void close() throws IOException {
                closed[0] = true;
            }
        };
        TarArchiveOutputStream t = new TarArchiveOutputStream(os);
        t.close();
        assertTrue(closed[0]);
        // second close should not call close again
        closed[0] = false;
        t.close();
        assertFalse(closed[0]);
    }

    @Test
    public void testCloseDoesNotDoubleFinish() throws Exception {
        // Verify that close() only calls finish() once
        final int[] finishCount = {0};
        OutputStream os = new ByteArrayOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                super.write(b, off, len);
                // count EOF records? Not reliable.
            }
        };
        TarArchiveOutputStream t = new TarArchiveOutputStream(os) {
            @Override
            public void finish() throws IOException {
                finishCount[0]++;
                super.finish();
            }
        };
        t.close();
        assertEquals(1, finishCount[0]);
        t.close();
        assertEquals(1, finishCount[0]);
    }

    @Test
    public void testPutArchiveEntryNormal() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        // currBytes should be 0, currSize 100
        // can't access private fields, but we can write and check no exception
        taos.write(new byte[100]);
        taos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryDirectory() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        taos.putArchiveEntry(entry);
        // directory size is 0
        taos.closeArchiveEntry(); // should not throw
    }

    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntryLongNameError() throws Exception {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        taos.putArchiveEntry(entry);
    }

    @Test
    public void testPutArchiveEntryLongNameTruncate() throws Exception {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry); // no exception
        taos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryLongNameGnu() throws Exception {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = "very/long/path/that/exceeds/100/characters/limit/which/is/required/to/test/the/gnu/longlink/extension/functionality";
        assertTrue(longName.length() >= TarConstants.NAMELEN);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(200);
        taos.putArchiveEntry(entry);
        taos.write(new byte[200]);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();
        // Now read back the archive to verify the long link entry
        byte[] archiveData = baos.toByteArray();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(archiveData));
        TarArchiveEntry firstEntry = tin.getNextTarEntry();
        assertNotNull(firstEntry);
        assertEquals(TarConstants.GNU_LONGLINK, firstEntry.getName());
        assertEquals(TarConstants.LF_GNUTYPE_LONGNAME, firstEntry.getLinkFlag());
        // Read the long name from the first entry's content
        byte[] nameBytes = new byte[(int) firstEntry.getSize()];
        tin.read(nameBytes);
        // The name should be the longName plus a NUL terminator
        assertEquals(longName + '\0', new String(nameBytes));
        // Next entry should be the actual file entry
        TarArchiveEntry secondEntry = tin.getNextTarEntry();
        assertNotNull(secondEntry);
        assertEquals(longName, secondEntry.getName());
        assertEquals(200, secondEntry.getSize());
        tin.close();
    }

    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntryNonTarEntry() throws Exception {
        taos.putArchiveEntry(new ArchiveEntry() {
            @Override
            public String getName() { return "test"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        });
    }

    @Test
    public void testCloseArchiveEntryExactSize() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        taos.write(new byte[100]);
        taos.closeArchiveEntry(); // no exception
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryTooFewBytes() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        taos.write(new byte[50]);
        taos.closeArchiveEntry(); // throws IOException
    }

    @Test
    public void testCloseArchiveEntryWithPartialRecord() throws Exception {
        // Use a record size of 512, write 100 bytes, then close. assemLen=100, should pad and write record.
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        taos.write(new byte[100]);
        taos.closeArchiveEntry(); // should pad and write record, then check currBytes==currSize
    }

    @Test
    public void testCloseArchiveEntryDirectory() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry(); // no exception, currSize=0, currBytes=0
    }

    @Test
    public void testWriteWithinSize() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(200);
        taos.putArchiveEntry(entry);
        taos.write(new byte[100]);
        taos.write(new byte[100]);
        taos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteExceedsSize() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        taos.write(new byte[101]);
    }

    @Test
    public void testWriteWithAssemblyBuffer() throws Exception {
        // Write small chunks to test assembly logic
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(512);
        taos.putArchiveEntry(entry);
        // Write 100 bytes, then 100, then 312 to fill exactly one record (512)
        taos.write(new byte[100]);
        taos.write(new byte[100]);
        taos.write(new byte[312]);
        taos.closeArchiveEntry();
    }

    @Test
    public void testWriteWithAssemblyBufferLessThanRecord() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(200);
        taos.putArchiveEntry(entry);
        taos.write(new byte[100]);
        taos.write(new byte[100]); // total 200, assemLen should be 200 after second write, no record written yet
        taos.closeArchiveEntry(); // should pad and write record
    }

    @Test
    public void testWriteZeroBytes() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.write(new byte[0], 0, 0); // should do nothing
        taos.closeArchiveEntry();
    }

    @Test
    public void testWriteWithOffset() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(10);
        taos.putArchiveEntry(entry);
        byte[] data = new byte[20];
        Arrays.fill(data, (byte) 1);
        taos.write(data, 5, 10); // write 10 bytes from offset 5
        taos.closeArchiveEntry();
    }

    @Test
    public void testFlush() throws Exception {
        final boolean[] flushed = {false};
        OutputStream os = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
            @Override
            public void flush() throws IOException {
                flushed[0] = true;
            }
        };
        TarArchiveOutputStream t = new TarArchiveOutputStream(os);
        t.flush();
        assertTrue(flushed[0]);
    }

    @Test
    public void testCreateArchiveEntry() throws Exception {
        File file = new File("test.txt");
        ArchiveEntry entry = taos.createArchiveEntry(file, "entryName");
        assertTrue(entry instanceof TarArchiveEntry);
        assertEquals("entryName", entry.getName());
    }

    @Test
    public void testWriteAfterCloseThrows() throws Exception {
        taos.close();
        try {
            taos.write(new byte[10]);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testPutArchiveEntryAfterCloseThrows() throws Exception {
        taos.close();
        try {
            taos.putArchiveEntry(new TarArchiveEntry("test"));
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }
}
