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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveInputStreamTest {

    private byte[] createZip(String name, byte[] content, int method, boolean useDataDescriptor, boolean addEocd) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        if (content != null) {
            crc.update(content);
        }
        long crcVal = crc.getValue();
        byte[] compressedData;
        int compSize;
        int uncompSize = content != null ? content.length : 0;

        if (method == ZipMethod.DEFLATED.getCode() && content != null) {
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            deflater.setInput(content);
            deflater.finish();
            byte[] buf = new byte[1024];
            int deflatedBytes = deflater.deflate(buf);
            deflater.end();
            compressedData = new byte[deflatedBytes];
            System.arraycopy(buf, 0, compressedData, 0, deflatedBytes);
            compSize = deflatedBytes;
        } else {
            compressedData = content != null ? content : new byte[0];
            compSize = uncompSize;
        }

        byte[] nameBytes = name.getBytes("UTF-8");

        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0});
        int gpFlag = 0x0800;
        if (useDataDescriptor) {
            gpFlag |= 0x0008;
        }
        baos.write(new byte[]{(byte) (gpFlag & 0xFF), (byte) ((gpFlag >> 8) & 0xFF)});
        baos.write(new byte[]{(byte) (method & 0xFF), (byte) ((method >> 8) & 0xFF)});
        baos.write(new byte[]{0, 0, 0, 0});

        if (useDataDescriptor) {
            baos.write(new byte[12]);
        } else {
            baos.write(ZipLong.getBytes(crcVal));
            baos.write(ZipLong.getBytes(compSize));
            baos.write(ZipLong.getBytes(uncompSize));
        }

        baos.write(ZipShort.getBytes(nameBytes.length));
        baos.write(ZipShort.getBytes(0));
        baos.write(nameBytes);
        baos.write(compressedData);

        if (useDataDescriptor) {
            baos.write(ZipArchiveOutputStream.DD_SIG);
            baos.write(ZipLong.getBytes(crcVal));
            baos.write(ZipLong.getBytes(compSize));
            baos.write(ZipLong.getBytes(uncompSize));
        }

        if (addEocd) {
            baos.write(ZipArchiveOutputStream.CFH_SIG);
            baos.write(new byte[]{20, 0});
            baos.write(new byte[]{20, 0});
            baos.write(new byte[]{(byte) (gpFlag & 0xFF), (byte) ((gpFlag >> 8) & 0xFF)});
            baos.write(new byte[]{(byte) (method & 0xFF), (byte) ((method >> 8) & 0xFF)});
            baos.write(new byte[]{0, 0, 0, 0});
            baos.write(ZipLong.getBytes(crcVal));
            baos.write(ZipLong.getBytes(compSize));
            baos.write(ZipLong.getBytes(uncompSize));
            baos.write(ZipShort.getBytes(nameBytes.length));
            baos.write(ZipShort.getBytes(0));
            baos.write(ZipShort.getBytes(0));
            baos.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
            baos.write(new byte[]{0, 0, 0, 0});
            baos.write(nameBytes);

            baos.write(ZipArchiveOutputStream.EOCD_SIG);
            baos.write(new byte[]{0, 0, 0, 0});
            baos.write(new byte[]{1, 0, 1, 0});
            baos.write(ZipLong.getBytes(46 + nameBytes.length));
            baos.write(ZipLong.getBytes(30 + nameBytes.length + compSize + (useDataDescriptor ? 16 : 0)));
            baos.write(new byte[]{0, 0});
        }

        return baos.toByteArray();
    }

    @Test
    public void testMatches() {
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(lfh, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipArchiveOutputStream.EOCD_SIG, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipArchiveOutputStream.DD_SIG, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes(), 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[]{1, 2, 3, 4}, 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(lfh, 3));
    }

    @Test
    public void testConstructorsAndProperties() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis1 = new ZipArchiveInputStream(bais);
        Assert.assertEquals("UTF8", zis1.encoding);
        zis1.close();

        ZipArchiveInputStream zis2 = new ZipArchiveInputStream(bais, "ASCII");
        Assert.assertEquals("ASCII", zis2.encoding);
        zis2.close();

        ZipArchiveInputStream zis3 = new ZipArchiveInputStream(bais, "UTF-8", true);
        Assert.assertEquals("UTF-8", zis3.encoding);
        zis3.close();

        ZipArchiveInputStream zis4 = new ZipArchiveInputStream(bais, "UTF-8", false, true);
        Assert.assertEquals("UTF-8", zis4.encoding);
        zis4.close();
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello World! STORED entry test data.".getBytes("UTF-8");
        byte[] zipData = createZip("test.txt", content, ZipEntry.STORED, false, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertEquals(content.length, entry.getSize());
        Assert.assertTrue(zis.canReadEntryData(entry));

        byte[] readBuf = new byte[content.length];
        int readBytes = zis.read(readBuf, 0, readBuf.length);
        Assert.assertEquals(content.length, readBytes);
        Assert.assertArrayEquals(content, readBuf);
        Assert.assertEquals(-1, zis.read(readBuf, 0, readBuf.length));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] content = "Hello World! DEFLATED entry test data with some repetitive characters repeating repeating repeating.".getBytes("UTF-8");
        byte[] zipData = createZip("deflated.txt", content, ZipEntry.DEFLATED, false, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ArchiveEntry entry = zis.getNextEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("deflated.txt", entry.getName());

        byte[] readBuf = new byte[content.length * 2];
        int totalRead = 0;
        int r;
        while ((r = zis.read(readBuf, totalRead, readBuf.length - totalRead)) > 0) {
            totalRead += r;
        }
        Assert.assertEquals(content.length, totalRead);
        byte[] actual = new byte[totalRead];
        System.arraycopy(readBuf, 0, actual, 0, totalRead);
        Assert.assertArrayEquals(content, actual);

        Assert.assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testReadDeflatedWithDataDescriptor() throws IOException {
        byte[] content = "Deflated content with data descriptor.".getBytes("UTF-8");
        byte[] zipData = createZip("dd.txt", content, ZipEntry.DEFLATED, true, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("dd.txt", entry.getName());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int r;
        while ((r = zis.read(buf, 0, buf.length)) > 0) {
            baos.write(buf, 0, r);
        }
        Assert.assertArrayEquals(content, baos.toByteArray());
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadStoredWithDataDescriptorAllowed() throws IOException {
        byte[] content = "Stored content with data descriptor allowed.".getBytes("UTF-8");
        byte[] zipData = createZip("stored_dd.txt", content, ZipEntry.STORED, true, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("stored_dd.txt", entry.getName());
        Assert.assertTrue(zis.canReadEntryData(entry));

        byte[] buf = new byte[content.length];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(content.length, read);
        Assert.assertArrayEquals(content, buf);
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testReadStoredWithDataDescriptorNotAllowedThrows() throws IOException {
        byte[] content = "Stored content with data descriptor disallowed.".getBytes("UTF-8");
        byte[] zipData = createZip("stored_dd_fail.txt", content, ZipEntry.STORED, true, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, false);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertFalse(zis.canReadEntryData(entry));
        zis.read(new byte[10], 0, 10);
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testSplitZipThrows() throws IOException {
        byte[] splitSig = ZipLong.DD_SIG.getBytes();
        byte[] data = new byte[30];
        System.arraycopy(splitSig, 0, data, 0, 4);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(data));
        zis.getNextZipEntry();
    }

    @Test
    public void testSingleSegmentSplitMarker() throws IOException {
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        byte[] zipData = createZip("test.txt", "data".getBytes("UTF-8"), ZipEntry.STORED, false, false);
        byte[] fullData = new byte[splitMarker.length + zipData.length];
        System.arraycopy(splitMarker, 0, fullData, 0, splitMarker.length);
        System.arraycopy(zipData, 0, fullData, splitMarker.length, zipData.length);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(fullData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        zis.close();
    }

    @Test(expected = ZipException.class)
    public void testUnexpectedSignatureThrows() throws IOException {
        byte[] invalidHeader = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(invalidHeader));
        zis.getNextZipEntry();
    }

    @Test
    public void testHitCentralDirectoryImmediately() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.CFH_SIG);
        for (int i = 0; i < 42; i++) {
            baos.write(0);
        }
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        for (int i = 0; i < 18; i++) {
            baos.write(0);
        }

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testCloseEntryWithoutReadingData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] z1 = createZip("file1.txt", "content1".getBytes("UTF-8"), ZipEntry.STORED, false, false);
        byte[] z2 = createZip("file2.txt", "content2".getBytes("UTF-8"), ZipEntry.STORED, false, true);
        baos.write(z1);
        baos.write(z2);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        Assert.assertEquals("file1.txt", entry1.getName());

        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        Assert.assertEquals("file2.txt", entry2.getName());

        byte[] buf = new byte[8];
        int r = zis.read(buf, 0, buf.length);
        Assert.assertEquals(8, r);
        Assert.assertEquals("content2", new String(buf, "UTF-8"));
        zis.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] zipData = createZip("skip.txt", content, ZipEntry.STORED, false, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        long skipped = zis.skip(5);
        Assert.assertEquals(5, skipped);
        byte[] buf = new byte[5];
        int read = zis.read(buf, 0, 5);
        Assert.assertEquals(5, read);
        Assert.assertEquals("56789", new String(buf, "UTF-8"));

        long remainingSkip = zis.skip(100);
        Assert.assertEquals(6, remainingSkip);
        zis.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegativeThrows() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.skip(-1);
    }

    @Test
    public void testReadBoundaryExceptions() throws IOException {
        byte[] content = "Data".getBytes("UTF-8");
        byte[] zipData = createZip("bound.txt", content, ZipEntry.STORED, false, true);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();

        byte[] buf = new byte[10];
        try {
            zis.read(buf, -1, 5);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            zis.read(buf, 0, -1);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            zis.read(buf, 5, 6);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            zis.read(buf, 11, 0);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        zis.close();
        try {
            zis.read(buf, 0, 1);
            Assert.fail();
        } catch (IOException ignored) {}
    }

    @Test
    public void testCanReadEntryData() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertFalse(zis.canReadEntryData(null));

        ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored");
        storedEntry.setMethod(ZipEntry.STORED);
        Assert.assertTrue(zis.canReadEntryData(storedEntry));

        ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("deflated");
        deflatedEntry.setMethod(ZipEntry.DEFLATED);
        Assert.assertTrue(zis.canReadEntryData(deflatedEntry));

        ZipArchiveEntry unshrinkEntry = new ZipArchiveEntry("unshrink");
        unshrinkEntry.setMethod(ZipMethod.UNSHRINKING.getCode());
        Assert.assertTrue(zis.canReadEntryData(unshrinkEntry));
    }

    @Test
    public void testZip64ExtraInLFH() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{45, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(ZipLong.getBytes(0x12345678L));
        baos.write(ZipLong.getBytes(0xFFFFFFFFL));
        baos.write(ZipLong.getBytes(0xFFFFFFFFL));

        byte[] nameBytes = "zip64.txt".getBytes("UTF-8");
        baos.write(ZipShort.getBytes(nameBytes.length));

        byte[] extra = new byte[20];
        System.arraycopy(Zip64ExtendedInformationExtraField.HEADER_ID.getBytes(), 0, extra, 0, 2);
        System.arraycopy(ZipShort.getBytes(16), 0, extra, 2, 2);
        System.arraycopy(ZipEightByteInteger.getBytes(100L), 0, extra, 4, 8);
        System.arraycopy(ZipEightByteInteger.getBytes(100L), 0, extra, 12, 8);

        baos.write(ZipShort.getBytes(extra.length));
        baos.write(nameBytes);
        baos.write(extra);
        baos.write(new byte[100]);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(100L, entry.getSize());
        Assert.assertEquals(100L, entry.getCompressedSize());
        zis.close();
    }

    @Test
    public void testUnicodeExtraField() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(ZipLong.getBytes(0L));
        baos.write(ZipLong.getBytes(0L));
        baos.write(ZipLong.getBytes(0L));

        byte[] asciiName = "ascii.txt".getBytes("US-ASCII");
        String unicodeName = "unicode_\u00E9.txt";
        UnicodePathExtraField unicodeExtra = new UnicodePathExtraField(unicodeName, asciiName);
        byte[] extra = unicodeExtra.getLocalFileDataData();

        baos.write(ZipShort.getBytes(asciiName.length));
        baos.write(ZipShort.getBytes(extra.length + 4));
        baos.write(asciiName);
        baos.write(UnicodePathExtraField.UPATH_ID.getBytes());
        baos.write(ZipShort.getBytes(extra.length));
        baos.write(extra);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(unicodeName, entry.getName());
        zis.close();
    }

    @Test
    public void testDataDescriptorWithSignatureAndZip64Sizes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{45, 0});
        baos.write(new byte[]{0x08, 0x08});
        baos.write(new byte[]{8, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[12]);
        byte[] nameBytes = "file64.txt".getBytes("UTF-8");
        baos.write(ZipShort.getBytes(nameBytes.length));
        baos.write(ZipShort.getBytes(0));
        baos.write(nameBytes);

        byte[] rawContent = "Some text for deflating".getBytes("UTF-8");
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        def.setInput(rawContent);
        def.finish();
        byte[] cbuf = new byte[128];
        int cLen = def.deflate(cbuf);
        def.end();
        baos.write(cbuf, 0, cLen);

        CRC32 crc = new CRC32();
        crc.update(rawContent);

        baos.write(ZipArchiveOutputStream.DD_SIG);
        baos.write(ZipLong.getBytes(crc.getValue()));
        baos.write(ZipEightByteInteger.getBytes(cLen));
        baos.write(ZipEightByteInteger.getBytes(rawContent.length));

        baos.write(new byte[]{0, 0, 0, 0});

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        ByteArrayOutputStream readBytes = new ByteArrayOutputStream();
        byte[] b = new byte[64];
        int r;
        while ((r = zis.read(b)) != -1) {
            readBytes.write(b, 0, r);
        }
        Assert.assertArrayEquals(rawContent, readBytes.toByteArray());
        zis.close();
    }

    @Test
    public void testEmptyStreamReturnsNull() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertEquals(-1, zis.read(new byte[10], 0, 10));
        zis.close();
    }

    @Test
    public void testDoubleClose() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.close();
    }

    @Test
    public void testStoredEntryTruncatedDataThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(ZipLong.getBytes(0L));
        baos.write(ZipLong.getBytes(20L));
        baos.write(ZipLong.getBytes(20L));
        byte[] name = "trunc.txt".getBytes("UTF-8");
        baos.write(ZipShort.getBytes(name.length));
        baos.write(ZipShort.getBytes(0));
        baos.write(name);
        baos.write("too short".getBytes("UTF-8"));

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        zis.getNextZipEntry();
        try {
            zis.getNextZipEntry();
            Assert.fail();
        } catch (EOFException expected) {}
        zis.close();
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testUnsupportedCompressionMethodThrowsOnRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{0, 0});
        baos.write(ZipShort.getBytes(ZipMethod.EXP_RESERVED.getCode()));
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(ZipLong.getBytes(0L));
        baos.write(ZipLong.getBytes(4L));
        baos.write(ZipLong.getBytes(4L));
        byte[] name = "unsupported.txt".getBytes("UTF-8");
        baos.write(ZipShort.getBytes(name.length));
        baos.write(ZipShort.getBytes(0));
        baos.write(name);
        baos.write(new byte[]{1, 2, 3, 4});

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        zis.read(new byte[10], 0, 10);
    }
}
