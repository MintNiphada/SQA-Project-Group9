package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipArchiveInputStreamTest {

    private byte[] createZipData(String entryName, byte[] content, int method) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setMethod(method);
        if (method == ZipArchiveOutputStream.STORED) {
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setCrc(crc.getValue());
        }
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }

    private byte[] createMultiEntryZipData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data1 = "FirstEntryContent".getBytes("UTF-8");
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry1.txt");
        entry1.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry1);
        zaos.write(data1);
        zaos.closeArchiveEntry();

        byte[] data2 = "SecondEntryStoredContentLongerString".getBytes("UTF-8");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry2.txt");
        entry2.setMethod(ZipArchiveOutputStream.STORED);
        entry2.setSize(data2.length);
        entry2.setCompressedSize(data2.length);
        CRC32 crc = new CRC32();
        crc.update(data2);
        entry2.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry2);
        zaos.write(data2);
        zaos.closeArchiveEntry();

        zaos.close();
        return baos.toByteArray();
    }

    @Test
    public void testMatches() {
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        byte[] invalid = new byte[] { 0, 1, 2, 3 };

        Assert.assertTrue(ZipArchiveInputStream.matches(lfh, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(eocd, 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(invalid, 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(lfh, 3));
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[0], 0));

        byte[] partial = new byte[] { lfh[0], lfh[1], lfh[2], 0 };
        Assert.assertFalse(ZipArchiveInputStream.matches(partial, 4));
    }

    @Test
    public void testReadEmptyStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bais);

        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello, Stored Zip!".getBytes("UTF-8");
        byte[] zipData = createZipData("testStored.txt", content, ZipArchiveOutputStream.STORED);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();

        Assert.assertNotNull(entry);
        Assert.assertEquals("testStored.txt", entry.getName());
        Assert.assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        Assert.assertEquals(content.length, entry.getSize());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = zis.read(readBuffer, 0, readBuffer.length);
        Assert.assertEquals(content.length, bytesRead);
        Assert.assertArrayEquals(content, readBuffer);

        Assert.assertEquals(-1, zis.read(readBuffer, 0, 1));
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] content = "Hello, Deflated Zip with repeated text text text!".getBytes("UTF-8");
        byte[] zipData = createZipData("testDeflated.txt", content, ZipArchiveOutputStream.DEFLATED);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ArchiveEntry entry = zis.getNextEntry();

        Assert.assertNotNull(entry);
        Assert.assertEquals("testDeflated.txt", entry.getName());

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int r;
        while ((r = zis.read(buf, 0, buf.length)) != -1) {
            result.write(buf, 0, r);
        }

        Assert.assertArrayEquals(content, result.toByteArray());
        Assert.assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testSequentialEntries() throws IOException {
        byte[] zipData = createMultiEntryZipData();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));

        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        Assert.assertNotNull(entry1);
        Assert.assertEquals("entry1.txt", entry1.getName());

        byte[] buf1 = new byte[64];
        int read1 = zis.read(buf1, 0, buf1.length);
        Assert.assertEquals("FirstEntryContent", new String(buf1, 0, read1, "UTF-8"));

        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals("entry2.txt", entry2.getName());

        byte[] buf2 = new byte[64];
        int read2 = zis.read(buf2, 0, buf2.length);
        Assert.assertEquals("SecondEntryStoredContentLongerString", new String(buf2, 0, read2, "UTF-8"));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testCloseEntryWithoutFullRead() throws IOException {
        byte[] zipData = createMultiEntryZipData();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));

        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        Assert.assertNotNull(entry1);
        // Only read 3 bytes of first entry
        byte[] buf = new byte[3];
        int r = zis.read(buf, 0, 3);
        Assert.assertEquals(3, r);

        // Advance directly to next entry
        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals("entry2.txt", entry2.getName());

        ByteArrayOutputStream result2 = new ByteArrayOutputStream();
        byte[] buf2 = new byte[16];
        int r2;
        while ((r2 = zis.read(buf2, 0, buf2.length)) != -1) {
            result2.write(buf2, 0, r2);
        }
        Assert.assertEquals("SecondEntryStoredContentLongerString", new String(result2.toByteArray(), "UTF-8"));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] zipData = createZipData("skipTest.txt", content, ZipArchiveOutputStream.STORED);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        Assert.assertNotNull(zis.getNextZipEntry());

        long skipped = zis.skip(5);
        Assert.assertEquals(5, skipped);

        byte[] remaining = new byte[11];
        int r = zis.read(remaining, 0, remaining.length);
        Assert.assertEquals(11, r);
        Assert.assertEquals("56789ABCDEF", new String(remaining, "UTF-8"));

        long skipPastEnd = zis.skip(10);
        Assert.assertEquals(0, skipPastEnd);

        zis.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        byte[] zipData = createZipData("negSkip.txt", new byte[10], ZipArchiveOutputStream.STORED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        try {
            zis.skip(-1);
        } finally {
            zis.close();
        }
    }

    @Test
    public void testReadOutOfBounds() throws IOException {
        byte[] content = "Data".getBytes("UTF-8");
        byte[] zipData = createZipData("bounds.txt", content, ZipArchiveOutputStream.STORED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();

        byte[] b = new byte[10];
        try {
            zis.read(b, -1, 5);
            Assert.fail("Expected ArrayIndexOutOfBoundsException for negative start");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            zis.read(b, 0, -1);
            Assert.fail("Expected ArrayIndexOutOfBoundsException for negative length");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            zis.read(b, 5, 6);
            Assert.fail("Expected ArrayIndexOutOfBoundsException for overflow");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            zis.read(null, 0, 1);
            Assert.fail("Expected NullPointerException or ArrayIndexOutOfBoundsException for null buffer");
        } catch (NullPointerException | ArrayIndexOutOfBoundsException expected) {
        }

        zis.close();
    }

    @Test
    public void testReadWhenClosed() throws IOException {
        byte[] zipData = createZipData("closed.txt", new byte[5], ZipArchiveOutputStream.STORED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        zis.close();

        try {
            zis.read(new byte[5], 0, 5);
            Assert.fail("Expected IOException when reading closed stream");
        } catch (IOException expected) {
            Assert.assertEquals("The stream is closed", expected.getMessage());
        }

        try {
            zis.getNextZipEntry();
            Assert.assertNull(zis.getNextZipEntry());
        } catch (Exception e) {
            Assert.fail("getNextZipEntry after close should return null, but threw: " + e);
        }
    }

    @Test
    public void testReadWhenCurrentNull() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bais);

        byte[] buf = new byte[10];
        Assert.assertEquals(-1, zis.read(buf, 0, buf.length));
        zis.close();
    }

    @Test
    public void testCentralDirectorySignatureStopsIteration() throws IOException {
        byte[] cfhSig = ZipArchiveOutputStream.CFH_SIG;
        ByteArrayInputStream bais = new ByteArrayInputStream(cfhSig);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bais);

        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testInvalidSignatureReturnsNull() throws IOException {
        byte[] invalidSig = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                                         17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 };
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidSig);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bais);

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(expected = ZipException.class)
    public void testCorruptedDeflatedDataThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Write LFH
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[2]); // version
        baos.write(new byte[2]); // general purpose flag
        baos.write(new byte[] { 8, 0 }); // method = DEFLATED (8)
        baos.write(new byte[4]); // time
        baos.write(new byte[4]); // crc
        baos.write(new byte[] { 10, 0, 0, 0 }); // comp size = 10
        baos.write(new byte[] { 10, 0, 0, 0 }); // uncomp size = 10
        baos.write(new byte[] { 4, 0 }); // file name length = 4
        baos.write(new byte[2]); // extra field length = 0
        baos.write("test".getBytes("UTF-8")); // name
        baos.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }); // invalid compressed data

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);

        byte[] buf = new byte[100];
        try {
            zis.read(buf, 0, buf.length);
        } finally {
            zis.close();
        }
    }

    @Test
    public void testConstructorWithEncodingAndUnicodeFlag() throws IOException {
        byte[] zipData = createZipData("unicode_name.txt", "content".getBytes("UTF-8"), ZipArchiveOutputStream.STORED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true);

        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("unicode_name.txt", entry.getName());
        zis.close();
    }

    @Test
    public void testDataDescriptorEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // LFH with flag bit 3 set (Data Descriptor present)
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[2]); // version
        baos.write(new byte[] { 8, 0 }); // general purpose flag = 8 (data descriptor present)
        baos.write(new byte[] { 0, 0 }); // method = STORED (0)
        baos.write(new byte[4]); // time
        baos.write(new byte[4]); // crc = 0 (in descriptor)
        baos.write(new byte[4]); // comp size = 0
        baos.write(new byte[4]); // uncomp size = 0
        baos.write(new byte[] { 4, 0 }); // name len = 4
        baos.write(new byte[2]); // extra len = 0
        baos.write("desc".getBytes("UTF-8")); // name
        // No data payload since size is 0 for this test
        // Data descriptor (16 bytes: 4 bytes signature or crc, 4 bytes crc/comp, 4 bytes comp/uncomp, 4 bytes uncomp)
        baos.write(new byte[16]);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("desc", entry.getName());

        // advance to next entry / trigger closeEntry() which reads data descriptor
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testIdempotentClose() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bais);
        zis.close();
        zis.close(); // Should not throw
    }
}
