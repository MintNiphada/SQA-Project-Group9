package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CpioArchiveOutputStreamTest {

    @Test
    public void testConstructors() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os1 = new CpioArchiveOutputStream(baos);
        Assert.assertNotNull(os1);
        os1.close();

        CpioArchiveOutputStream os2 = new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW);
        Assert.assertNotNull(os2);
        os2.close();

        CpioArchiveOutputStream os3 = new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW, 512);
        Assert.assertNotNull(os3);
        os3.close();

        CpioArchiveOutputStream os4 = new CpioArchiveOutputStream(new ByteArrayOutputStream(), "UTF-8");
        Assert.assertNotNull(os4);
        os4.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatConstructor() {
        new CpioArchiveOutputStream(new ByteArrayOutputStream(), (short) 999);
    }

    @Test
    public void testWriteNewFormatArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 512);
        
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test1.txt", 4);
        entry.setDeviceMin(0);
        entry.setInode(0);
        entry.setDeviceMaj(1);
        entry.setRemoteDeviceMaj(0);
        entry.setRemoteDeviceMin(0);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        entry.setUID(1000);
        entry.setGID(1000);
        entry.setNumberOfLinks(1);
        entry.setTime(12345678L);

        os.putArchiveEntry(entry);
        os.write(new byte[]{1, 2, 3, 4});
        os.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test2.txt", 0);
        entry2.setInode(10);
        entry2.setDeviceMin(2);
        os.putArchiveEntry(entry2);
        os.closeArchiveEntry();

        os.finish();
        os.close();

        byte[] result = baos.toByteArray();
        Assert.assertTrue(result.length > 0);
        Assert.assertEquals(0, result.length % 512);
    }

    @Test
    public void testWriteNewCrcFormatArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        byte[] content = new byte[]{'H', 'e', 'l', 'l', 'o'};
        long crc = 0;
        for (byte b : content) {
            crc += b & 0xFF;
        }

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", content.length);
        entry.setChksum(crc);
        os.putArchiveEntry(entry);
        os.write(content, 0, content.length);
        os.closeArchiveEntry();
        os.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testCrcMismatchThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        byte[] content = new byte[]{'H', 'e', 'l', 'l', 'o'};
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_fail.txt", content.length);
        entry.setChksum(12345L);
        os.putArchiveEntry(entry);
        os.write(content);
        os.closeArchiveEntry();
    }

    @Test
    public void testWriteOldAsciiArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "file1", 3);
        entry1.setInode(0);
        entry1.setDevice(0);
        entry1.setRemoteDevice(0);
        os.putArchiveEntry(entry1);
        os.write(new byte[]{'a', 'b', 'c'});
        os.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "file2", 0);
        entry2.setInode(5);
        entry2.setDevice(2);
        entry2.setRemoteDevice(1);
        os.putArchiveEntry(entry2);
        os.closeArchiveEntry();

        os.close();
        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteOldBinaryArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "bin1", 2);
        entry1.setInode(0);
        entry1.setDevice(0);
        os.putArchiveEntry(entry1);
        os.write(new byte[]{1, 2});
        os.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "bin2", 0);
        entry2.setInode(20);
        entry2.setDevice(3);
        os.putArchiveEntry(entry2);
        os.closeArchiveEntry();

        os.close();
        Assert.assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testPutEntryAfterFinish() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.finish();
        os.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test"));
    }

    @Test(expected = IOException.class)
    public void testPutEntryWhenClosed() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.close();
        os.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test"));
    }

    @Test(expected = IOException.class)
    public void testPutEntryFormatMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "test");
        os.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testDuplicateEntryName() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        os.putArchiveEntry(entry1);
        os.closeArchiveEntry();
        os.putArchiveEntry(entry2);
    }

    @Test
    public void testAutoClosePreviousEntryOnPutNext() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "entry1", 0);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "entry2", 0);
        os.putArchiveEntry(entry1);
        os.putArchiveEntry(entry2);
        os.closeArchiveEntry();
        os.close();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWhenNoneOpen() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntrySizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "sizeMismatch", 10);
        os.putArchiveEntry(entry);
        os.write(new byte[]{1, 2, 3});
        os.closeArchiveEntry();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOutOfBoundsNegativeOffset() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOutOfBoundsNegativeLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOutOfBoundsTooLarge() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.write(new byte[10], 5, 6);
    }

    @Test
    public void testWriteZeroLengthDoesNothing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.write(new byte[10], 0, 0);
        os.close();
    }

    @Test(expected = IOException.class)
    public void testWriteWithoutEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.write(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test(expected = IOException.class)
    public void testWritePastEndOfEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test", 2);
        os.putArchiveEntry(entry);
        os.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class)
    public void testFinishTwiceThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.finish();
        os.finish();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed", 5);
        os.putArchiveEntry(entry);
        os.finish();
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        File tempFile = File.createTempFile("cpio_test", ".tmp");
        tempFile.deleteOnExit();
        ArchiveEntry entry = os.createArchiveEntry(tempFile, "createdEntry");
        Assert.assertNotNull(entry);
        Assert.assertEquals("createdEntry", entry.getName());
        tempFile.delete();
        os.close();
    }

    @Test(expected = IOException.class)
    public void testCreateArchiveEntryAfterFinished() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.finish();
        File tempFile = File.createTempFile("cpio_test", ".tmp");
        tempFile.deleteOnExit();
        try {
            os.createArchiveEntry(tempFile, "createdEntry");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testMultipleCloseCalls() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream os = new CpioArchiveOutputStream(baos);
        os.close();
        os.close();
    }
}
