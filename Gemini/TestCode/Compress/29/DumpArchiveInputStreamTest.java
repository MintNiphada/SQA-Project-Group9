package org.apache.commons.compress.archivers.dump;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DumpArchiveInputStreamTest {

    private byte[] createValidHeader(int type, int count, int holes, int ino, int mode, long size) {
        byte[] buffer = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(type, buffer, 0);
        DumpArchiveUtil.convert32(DumpArchiveConstants.NFS_MAGIC, buffer, 24);
        DumpArchiveUtil.convert32(count, buffer, 160);
        DumpArchiveUtil.convert32(holes, buffer, 164);
        DumpArchiveUtil.convert32(ino, buffer, 20);
        DumpArchiveUtil.convert16(mode, buffer, 32);
        DumpArchiveUtil.convert64(size, buffer, 40);

        int checksum = DumpArchiveConstants.CHECKSUM;
        for (int i = 0; i < DumpArchiveConstants.TP_SIZE; i += 4) {
            if (i != 28) {
                checksum += DumpArchiveUtil.convert32(buffer, i);
            }
        }
        DumpArchiveUtil.convert32(DumpArchiveConstants.CHECKSUM - checksum, buffer, 28);
        return buffer;
    }

    private byte[] createSummaryHeader() {
        byte[] buffer = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, buffer, 0);
        DumpArchiveUtil.convert32(DumpArchiveConstants.NFS_MAGIC, buffer, 24);
        DumpArchiveUtil.convert32(1, buffer, 160);
        DumpArchiveUtil.convert32(1, buffer, 668);
        DumpArchiveUtil.convert32(0, buffer, 672);

        int checksum = DumpArchiveConstants.CHECKSUM;
        for (int i = 0; i < DumpArchiveConstants.TP_SIZE; i += 4) {
            if (i != 28) {
                checksum += DumpArchiveUtil.convert32(buffer, i);
            }
        }
        DumpArchiveUtil.convert32(DumpArchiveConstants.CHECKSUM - checksum, buffer, 28);
        return buffer;
    }

    private byte[] createArchive(byte[]... segments) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] seg : segments) {
            baos.write(seg);
        }
        return baos.toByteArray();
    }

    @Test
    public void testMatches() {
        Assert.assertFalse(DumpArchiveInputStream.matches(new byte[31], 31));

        byte[] buf32 = new byte[32];
        DumpArchiveUtil.convert32(DumpArchiveConstants.NFS_MAGIC, buf32, 24);
        Assert.assertTrue(DumpArchiveInputStream.matches(buf32, 32));

        DumpArchiveUtil.convert32(12345, buf32, 24);
        Assert.assertFalse(DumpArchiveInputStream.matches(buf32, 32));

        byte[] valid1k = createSummaryHeader();
        Assert.assertTrue(DumpArchiveInputStream.matches(valid1k, 1024));
        valid1k[0] ^= 0xFF;
        Assert.assertFalse(DumpArchiveInputStream.matches(valid1k, 1024));
    }

    @Test(expected = ArchiveException.class)
    public void testConstructorInvalidFormat() throws Exception {
        byte[] invalid = new byte[1024];
        new DumpArchiveInputStream(new ByteArrayInputStream(invalid));
    }

    @Test(expected = ArchiveException.class)
    public void testConstructorEofOnClri() throws Exception {
        byte[] sum = createSummaryHeader();
        new DumpArchiveInputStream(new ByteArrayInputStream(sum));
    }

    @Test(expected = ArchiveException.class)
    public void testConstructorInvalidClriType() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] badClri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);
        new DumpArchiveInputStream(new ByteArrayInputStream(createArchive(sum, badClri)));
    }

    @Test(expected = ArchiveException.class)
    public void testConstructorInvalidBitsType() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] badBits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        new DumpArchiveInputStream(new ByteArrayInputStream(createArchive(sum, clri, badBits)));
    }

    @Test
    public void testEmptyArchiveEndRecord() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] bits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);
        byte[] end = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.END.code, 0, 0, 0, 0, 0);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(createArchive(sum, clri, bits, end)));
        Assert.assertNotNull(in.getSummary());
        Assert.assertEquals(1, in.getSummary().getNTRec());
        Assert.assertNull(in.getNextDumpEntry());
        Assert.assertNull(in.getNextEntry());
        Assert.assertEquals(-1, in.read(new byte[10], 0, 10));
        Assert.assertTrue(in.getBytesRead() > 0);
        Assert.assertEquals(in.getBytesRead(), (long) in.getCount());
        in.close();
        in.close();
    }

    @Test
    public void testReadDirectoryAndFileEntries() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] bits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);

        byte[] dirHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 2, 0040755, 1024);
        byte[] dirData = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(3, dirData, 0);
        DumpArchiveUtil.convert16(16, dirData, 4);
        dirData[6] = 8;
        dirData[7] = 4;
        System.arraycopy("test".getBytes(), 0, dirData, 8, 4);

        DumpArchiveUtil.convert32(2, dirData, 16);
        DumpArchiveUtil.convert16(16, dirData, 20);
        dirData[22] = 4;
        dirData[23] = 1;
        dirData[24] = '.';

        DumpArchiveUtil.convert32(2, dirData, 32);
        DumpArchiveUtil.convert16(16, dirData, 36);
        dirData[38] = 4;
        dirData[39] = 2;
        dirData[40] = '.';
        dirData[41] = '.';

        byte[] fileHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 3, 0100644, 5);
        byte[] fileData = new byte[DumpArchiveConstants.TP_SIZE];
        System.arraycopy("hello".getBytes(), 0, fileData, 0, 5);

        byte[] end = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.END.code, 0, 0, 0, 0, 0);

        byte[] archive = createArchive(sum, clri, bits, dirHeader, dirData, fileHeader, fileData, end);
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive), "UTF-8");

        DumpArchiveEntry entry1 = in.getNextDumpEntry();
        Assert.assertNotNull(entry1);
        Assert.assertTrue(entry1.isDirectory());
        Assert.assertEquals(".", entry1.getName());

        DumpArchiveEntry entry2 = in.getNextEntry();
        Assert.assertNotNull(entry2);
        Assert.assertFalse(entry2.isDirectory());
        Assert.assertEquals("./test", entry2.getName());
        Assert.assertEquals("test", entry2.getSimpleName());

        byte[] readBuf = new byte[10];
        int read = in.read(readBuf, 0, readBuf.length);
        Assert.assertEquals(5, read);
        Assert.assertEquals("hello", new String(readBuf, 0, 5));
        Assert.assertEquals(-1, in.read(readBuf, 0, readBuf.length));

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testReadAddrSegmentsAndSparse() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] bits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);

        byte[] dirHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 2, 0040755, 1024);
        byte[] dirData = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(3, dirData, 0);
        DumpArchiveUtil.convert16(16, dirData, 4);
        dirData[6] = 8;
        dirData[7] = 1;
        dirData[8] = 'f';

        byte[] fileHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 3, 0100644, 2048);
        byte[] fileData1 = new byte[DumpArchiveConstants.TP_SIZE];
        Arrays.fill(fileData1, (byte) 'A');

        byte[] addrHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.ADDR.code, 1, 0, 3, 0100644, 2048);
        byte[] fileData2 = new byte[DumpArchiveConstants.TP_SIZE];
        Arrays.fill(fileData2, (byte) 'B');

        byte[] end = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.END.code, 0, 0, 0, 0, 0);

        byte[] archive = createArchive(sum, clri, bits, dirHeader, dirData, fileHeader, fileData1, addrHeader, fileData2, end);
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive));

        DumpArchiveEntry dir = in.getNextEntry();
        Assert.assertEquals(".", dir.getName());

        DumpArchiveEntry file = in.getNextEntry();
        Assert.assertEquals("./f", file.getName());

        byte[] outBuf = new byte[2048];
        int total = 0;
        int r;
        while ((r = in.read(outBuf, total, outBuf.length - total)) > 0) {
            total += r;
        }
        Assert.assertEquals(2048, total);
        Assert.assertEquals('A', outBuf[0]);
        Assert.assertEquals('B', outBuf[1024]);

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testPendingDirectoriesResolution() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] bits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);

        byte[] fileHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 0, 0, 4, 0100644, 0);

        byte[] dirHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 2, 0040755, 1024);
        byte[] dirData = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(4, dirData, 0);
        DumpArchiveUtil.convert16(16, dirData, 4);
        dirData[6] = 8;
        dirData[7] = 3;
        System.arraycopy("sub".getBytes(), 0, dirData, 8, 3);

        byte[] end = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.END.code, 0, 0, 0, 0, 0);

        byte[] archive = createArchive(sum, clri, bits, fileHeader, dirHeader, dirData, end);
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive));

        DumpArchiveEntry entry1 = in.getNextEntry();
        Assert.assertNotNull(entry1);
        Assert.assertEquals("./sub", entry1.getName());

        DumpArchiveEntry entry2 = in.getNextEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals(".", entry2.getName());

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testSkipPriorFileRecords() throws Exception {
        byte[] sum = createSummaryHeader();
        byte[] clri = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 0, 0, 0, 0, 0);
        byte[] bits = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 0, 0, 0, 0, 0);

        byte[] dirHeader = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 2, 0040755, 1024);
        byte[] dirData = new byte[DumpArchiveConstants.TP_SIZE];
        DumpArchiveUtil.convert32(3, dirData, 0);
        DumpArchiveUtil.convert16(16, dirData, 4);
        dirData[6] = 8;
        dirData[7] = 1;
        dirData[8] = 'a';

        byte[] file1 = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE.code, 1, 0, 3, 0100644, 1024);
        byte[] fileData1 = new byte[DumpArchiveConstants.TP_SIZE];

        byte[] end = createValidHeader(DumpArchiveConstants.SEGMENT_TYPE.END.code, 0, 0, 0, 0, 0);

        byte[] archive = createArchive(sum, clri, bits, dirHeader, dirData, file1, fileData1, end);
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive));

        Assert.assertNotNull(in.getNextEntry());
        DumpArchiveEntry f1 = in.getNextEntry();
        Assert.assertNotNull(f1);
        Assert.assertEquals("./a", f1.getName());

        Assert.assertNull(in.getNextEntry());
        in.close();
    }
}
