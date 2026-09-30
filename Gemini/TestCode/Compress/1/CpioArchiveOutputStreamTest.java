package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CpioArchiveOutputStreamTest {

    @Test
    public void testConstructors() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out1 = new CpioArchiveOutputStream(baos);
        Assert.assertNotNull(out1);
        out1.close();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        CpioArchiveOutputStream out2 = new CpioArchiveOutputStream(baos2, CpioConstants.FORMAT_NEW);
        Assert.assertNotNull(out2);
        out2.close();

        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        CpioArchiveOutputStream out3 = new CpioArchiveOutputStream(baos3, CpioConstants.FORMAT_NEW_CRC);
        Assert.assertNotNull(out3);
        out3.close();

        ByteArrayOutputStream baos4 = new ByteArrayOutputStream();
        CpioArchiveOutputStream out4 = new CpioArchiveOutputStream(baos4, CpioConstants.FORMAT_OLD_ASCII);
        Assert.assertNotNull(out4);
        out4.close();

        ByteArrayOutputStream baos5 = new ByteArrayOutputStream();
        CpioArchiveOutputStream out5 = new CpioArchiveOutputStream(baos5, CpioConstants.FORMAT_OLD_BINARY);
        Assert.assertNotNull(out5);
        out5.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test
    public void testWriteAndFinishFormatNew() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "testfile.txt");
        byte[] content = "Hello World".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setInode(12345);
        entry.setMode(0100644);
        entry.setUID(1000);
        entry.setGID(1000);
        entry.setNumberOfLinks(1);
        entry.setTime(100000L);
        entry.setDeviceMaj(1);
        entry.setDeviceMin(2);
        entry.setRemoteDeviceMaj(0);
        entry.setRemoteDeviceMin(0);

        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry();

        out.finish();
        out.finish(); // should do nothing if already finished
        out.close();

        byte[] result = baos.toByteArray();
        Assert.assertTrue(result.length > 0);
        String resultStr = new String(result, "UTF-8");
        Assert.assertTrue(resultStr.contains("testfile.txt"));
        Assert.assertTrue(resultStr.contains("TRAILER!!!"));
    }

    @Test
    public void testWriteFormatNewCrcSuccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crcfile.txt");
        byte[] content = "CRC Test Content".getBytes("UTF-8");
        entry.setSize(content.length);
        long crc = 0;
        for (byte b : content) {
            crc += (b & 0xFF);
        }
        entry.setChksum(crc);

        out.putNextEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteFormatNewCrcFailure() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_bad.txt");
        byte[] content = "Some content".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setChksum(999999); // Invalid expected checksum

        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry(); // Should throw CRC Error
    }

    @Test
    public void testWriteOldAscii() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt");
        byte[] content = "Old ASCII Content".getBytes("UTF-8");
        entry.setSize(content.length);
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(0100644);
        entry.setUID(100);
        entry.setGID(100);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(12345678L);

        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test
    public void testWriteOldBinary() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "binary.bin");
        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        entry.setSize(content.length);
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(0100644);
        entry.setUID(100);
        entry.setGID(100);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(12345678L);

        out.putArchiveEntry((ArchiveEntry) entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test
    public void testAutoClosingPreviousEntryOnPutNext() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry1 = new CpioArchiveEntry("file1.txt");
        entry1.setSize(0);
        out.putNextEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry("file2.txt");
        entry2.setSize(0);
        out.putNextEntry(entry2);

        out.finish();
        out.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test
    public void testDefaultTimeAndFormatInheritance() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt");
        entry.setTime(-1); // should trigger default time set
        entry.setSize(0);

        out.putNextEntry(entry);
        out.finish();
        out.close();

        Assert.assertTrue(entry.getTime() > 0);
    }

    @Test(expected = IOException.class)
    public void testDuplicateEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry1 = new CpioArchiveEntry("duplicate.txt");
        entry1.setSize(0);
        out.putNextEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry("duplicate.txt");
        entry2.setSize(0);
        out.putNextEntry(entry2);
    }

    @Test(expected = IOException.class)
    public void testWriteWithoutEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[] { 1, 2, 3 });
    }

    @Test(expected = IOException.class)
    public void testWritePastEndOfEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("short.txt");
        entry.setSize(2);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntrySizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("incomplete.txt");
        entry.setSize(10);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2 });
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteZeroLengthDoesNothing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("zero.txt");
        entry.setSize(0);
        out.putNextEntry(entry);
        out.write(new byte[10], 0, 0);
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeOffset() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOutOfBounds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], 5, 6);
    }

    @Test(expected = IOException.class)
    public void testEnsureOpenThrowsOnPutNextEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.putNextEntry(new CpioArchiveEntry("test.txt"));
    }

    @Test(expected = IOException.class)
    public void testEnsureOpenThrowsOnWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.write(new byte[5], 0, 5);
    }

    @Test(expected = IOException.class)
    public void testEnsureOpenThrowsOnFinish() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.finish();
    }

    @Test(expected = IOException.class)
    public void testEnsureOpenThrowsOnCloseArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteSingleByte() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(65);
        Assert.assertEquals(1, baos.size());
        Assert.assertEquals(65, baos.toByteArray()[0]);
        out.close();
    }

    @Test
    public void testPaddingBranches() throws IOException {
        // Test padding with different alignments (e.g. entry size not aligned to 4 or 2)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry("pad1.txt");
        byte[] content = "123".getBytes("UTF-8"); // 3 bytes -> requires 1 byte padding for 4-byte boundary
        entry.setSize(content.length);
        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry();

        out.finish();
        out.close();
    }

    @Test
    public void testOldBinaryPadding() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "pad_bin.txt");
        byte[] content = "1".getBytes("UTF-8"); // 1 byte -> requires 1 byte padding for 2-byte boundary
        entry.setSize(content.length);
        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry();

        out.finish();
        out.close();
    }
}