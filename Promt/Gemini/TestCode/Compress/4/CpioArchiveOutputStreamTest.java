package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CpioArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDefaultConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        assertNotNull(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test
    public void testWriteAndReadFormatNew() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test1.txt");
        byte[] content = "Hello CPIO New Format".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setMode(CpioConstants.C_ISREG);
        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();

        out.close();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteAndReadFormatNewCrcSuccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_test.txt");
        byte[] content = "Test CRC".getBytes("UTF-8");
        entry.setSize(content.length);
        long crc = 0;
        for (byte b : content) {
            crc += (b & 0xFF);
        }
        entry.setChksum(crc);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(expected = IOException.class)
    public void testWriteFormatNewCrcMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_fail.txt");
        byte[] content = "Test CRC Fail".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setChksum(12345L); // Wrong checksum

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteFormatOldAscii() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt");
        byte[] content = "Old ASCII Content".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(CpioConstants.C_ISREG);
        entry.setUID(100);
        entry.setGID(100);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(System.currentTimeMillis() / 1000);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();
    }

    @Test
    public void testWriteFormatOldBinary() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "old_bin.bin");
        byte[] content = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        entry.setSize(content.length);
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(CpioConstants.C_ISREG);
        entry.setUID(100);
        entry.setGID(100);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(System.currentTimeMillis() / 1000);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(expected = IOException.class)
    public void testFormatMismatchThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt");
        out.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testDuplicateEntryNameThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt");
        entry1.setSize(0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);
    }

    @Test
    public void testAutoSetTimeIfNegative() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "time_test.txt");
        entry.setTime(-1);
        entry.setSize(0);

        out.putArchiveEntry(entry);
        assertTrue(entry.getTime() >= 0);
        out.close();
    }

    @Test
    public void testPutArchiveEntryClosesPreviousEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt");
        entry1.setSize(0);
        out.putArchiveEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);

        out.close();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryClosesPreviousEntryWithSizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt");
        entry1.setSize(10);
        out.putArchiveEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntrySizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "short.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteWithoutEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[] { 1, 2, 3 });
    }

    @Test(expected = IOException.class)
    public void testWritePastSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "overflow.txt");
        entry.setSize(2);
        out.putArchiveEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeOffset() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOffsetPlusLengthTooLarge() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], 5, 6);
    }

    @Test
    public void testWriteZeroLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "zero.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.write(new byte[10], 0, 0);
        out.closeArchiveEntry();
        out.close();
    }

    @Test
    public void testFinishWithOpenEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);

        try {
            out.finish();
            fail("Expected IOException on finish with unclosed entry");
        } catch (IOException e) {
            assertEquals("This archives contains unclosed entries.", e.getMessage());
        }
    }

    @Test
    public void testOperationsOnClosedStreamThrowException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();

        try {
            out.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt"));
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }

        try {
            out.closeArchiveEntry();
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }

        try {
            out.write(new byte[5], 0, 5);
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }

        try {
            out.finish();
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.close(); // Second call should be a no-op
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        File tempFile = temporaryFolder.newFile("create_entry.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Hello File".getBytes("UTF-8"));
        }

        CpioArchiveEntry entry = (CpioArchiveEntry) out.createArchiveEntry(tempFile, "entry_name.txt");
        assertNotNull(entry);
        assertEquals("entry_name.txt", entry.getName());
        assertEquals(tempFile.length(), entry.getSize());

        out.close();
    }

    @Test
    public void testPaddingBehavior() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        // Entry with odd length name and odd size to exercise padding
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "odd_name.txt");
        byte[] content = new byte[] { 1, 2, 3 };
        entry.setSize(content.length);
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }
}
