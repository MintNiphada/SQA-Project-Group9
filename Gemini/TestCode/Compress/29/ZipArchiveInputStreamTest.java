package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipArchiveInputStreamTest {

    private byte[] createZip(String name, byte[] content, int method, boolean useDataDescriptor, boolean useZip64) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        if (content != null) {
            crc.update(content);
        }
        long crcVal = crc.getValue();

        byte[] compressed = content;
        if (method == ZipArchiveOutputStream.DEFLATED && content != null && content.length > 0) {
            ByteArrayOutputStream defOut = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            deflater.setInput(content);
            deflater.finish();
            byte[] buf = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buf);
                defOut.write(buf, 0, count);
            }
            deflater.end();
            compressed = defOut.toByteArray();
        } else if (content == null) {
            compressed = new byte[0];
        }

        long cSize = compressed.length;
        long size = content != null ? content.length : 0;

        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0});
        int gpFlag = (useDataDescriptor ? 8 : 0) | 2048;
        baos.write(new byte[]{(byte) (gpFlag & 0xFF), (byte) ((gpFlag >> 8) & 0xFF)});
        baos.write(new byte[]{(byte) (method & 0xFF), (byte) ((method >> 8) & 0xFF)});
        baos.write(new byte[]{0, 0, 0, 0});

        if (useDataDescriptor) {
            baos.write(new byte[12]);
        } else if (useZip64) {
            baos.write(new ZipLong(crcVal).getBytes());
            baos.write(ZipLong.ZIP64_MAGIC.getBytes());
            baos.write(ZipLong.ZIP64_MAGIC.getBytes());
        } else {
            baos.write(new ZipLong(crcVal).getBytes());
            baos.write(new ZipLong(cSize).getBytes());
            baos.write(new ZipLong(size).getBytes());
        }

        byte[] extra = new byte[0];
        if (useZip64 && !useDataDescriptor) {
            Zip64ExtendedInformationExtraField z64 = new Zip64ExtendedInformationExtraField(
                    new ZipEightByteInteger(size), new ZipEightByteInteger(cSize)
            );
            extra = z64.getLocalFileDataData();
            byte[] extraHeader = new byte[4 + extra.length];
            System.arraycopy(Zip64ExtendedInformationExtraField.HEADER_ID.getBytes(), 0, extraHeader, 0, 2);
            System.arraycopy(new ZipShort(extra.length).getBytes(), 0, extraHeader, 2, 2);
            System.arraycopy(extra, 0, extraHeader, 4, extra.length);
            extra = extraHeader;
        }

        baos.write(new ZipShort(nameBytes.length).getBytes());
        baos.write(new ZipShort(extra.length).getBytes());
        baos.write(nameBytes);
        baos.write(extra);

        baos.write(compressed);

        if (useDataDescriptor) {
            baos.write(ZipLong.DD_SIG.getBytes());
            baos.write(new ZipLong(crcVal).getBytes());
            if (useZip64) {
                baos.write(new ZipEightByteInteger(cSize).getBytes());
                baos.write(new ZipEightByteInteger(size).getBytes());
            } else {
                baos.write(new ZipLong(cSize).getBytes());
                baos.write(new ZipLong(size).getBytes());
            }
        }

        long lfhOffset = 0;
        long cdOffset = baos.size();

        baos.write(ZipArchiveOutputStream.CFH_SIG);
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{(byte) (gpFlag & 0xFF), (byte) ((gpFlag >> 8) & 0xFF)});
        baos.write(new byte[]{(byte) (method & 0xFF), (byte) ((method >> 8) & 0xFF)});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new ZipLong(crcVal).getBytes());
        baos.write(new ZipLong(cSize).getBytes());
        baos.write(new ZipLong(size).getBytes());
        baos.write(new ZipShort(nameBytes.length).getBytes());
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new ZipLong(lfhOffset).getBytes());
        baos.write(nameBytes);

        long cdSize = baos.size() - cdOffset;

        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{1, 0});
        baos.write(new byte[]{1, 0});
        baos.write(new ZipLong(cdSize).getBytes());
        baos.write(new ZipLong(cdOffset).getBytes());
        baos.write(new byte[]{0, 0});

        return baos.toByteArray();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] payload = "Hello Apache Commons Compress Deflated Test!".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("test.txt", payload, ZipArchiveOutputStream.DEFLATED, false, false);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertTrue(in.canReadEntryData(entry));

        byte[] readBuf = new byte[100];
        int read = in.read(readBuf, 0, readBuf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertArrayEquals(payload, java.util.Arrays.copyOf(readBuf, read));
        Assert.assertEquals(-1, in.read(readBuf, 0, readBuf.length));
        Assert.assertNull(in.getNextZipEntry());
        in.close();
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] payload = "Hello Stored Entry!".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("stored.txt", payload, ZipArchiveOutputStream.STORED, false, false);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", false);
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("stored.txt", entry.getName());
        Assert.assertEquals(payload.length, entry.getSize());

        byte[] buf = new byte[payload.length];
        int count = in.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, count);
        Assert.assertArrayEquals(payload, buf);
        Assert.assertEquals(-1, in.read(buf, 0, buf.length));
        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testStoredEntryWithDataDescriptorAllowed() throws IOException {
        byte[] payload = "DataDescriptor Stored Test Content".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("ddstored.txt", payload, ZipArchiveOutputStream.STORED, true, false);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, true);
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertTrue(in.canReadEntryData(entry));

        byte[] buf = new byte[256];
        int read = in.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertArrayEquals(payload, java.util.Arrays.copyOf(buf, read));
        Assert.assertNull(in.getNextZipEntry());
        in.close();
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testStoredEntryWithDataDescriptorDisallowed() throws IOException {
        byte[] payload = "DataDescriptor Stored Test Content".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("ddstored.txt", payload, ZipArchiveOutputStream.STORED, true, false);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, false);
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertFalse(in.canReadEntryData(entry));
        byte[] buf = new byte[256];
        in.read(buf, 0, buf.length);
    }

    @Test
    public void testZip64ExtraFieldProcessing() throws IOException {
        byte[] payload = "Zip64 extra field content test".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("zip64.txt", payload, ZipArchiveOutputStream.STORED, false, true);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(payload.length, entry.getSize());
        Assert.assertEquals(payload.length, entry.getCompressedSize());
        in.close();
    }

    @Test
    public void testMatches() {
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[0], 0));
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[]{1, 2, 3}, 3));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipArchiveOutputStream.LFH_SIG, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipArchiveOutputStream.EOCD_SIG, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipArchiveOutputStream.DD_SIG, 4));
        Assert.assertTrue(ZipArchiveInputStream.matches(ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes(), 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[]{0, 0, 0, 0}, 4));
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testSplitArchiveException() throws IOException {
        byte[] splitSig = ZipArchiveOutputStream.DD_SIG;
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(splitSig));
        in.getNextZipEntry();
    }

    @Test
    public void testSingleSegmentSplitMarkerSkip() throws IOException {
        byte[] payload = "Single segment split content".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("single.txt", payload, ZipArchiveOutputStream.STORED, false, false);
        byte[] splitZip = new byte[zipData.length + 4];
        System.arraycopy(ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes(), 0, splitZip, 0, 4);
        System.arraycopy(zipData, 0, splitZip, 4, zipData.length);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(splitZip));
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("single.txt", entry.getName());
        in.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] payload = "0123456789abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("skip.txt", payload, ZipArchiveOutputStream.STORED, false, false);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = in.getNextZipEntry();
        Assert.assertNotNull(entry);
        long skipped = in.skip(10);
        Assert.assertEquals(10, skipped);
        byte[] buf = new byte[10];
        int read = in.read(buf, 0, 10);
        Assert.assertEquals(10, read);
        Assert.assertEquals("abcdefghij", new String(buf, 0, read, StandardCharsets.UTF_8));
        long skippedRem = in.skip(100);
        Assert.assertEquals(16, skippedRem);
        Assert.assertEquals(0, in.skip(10));
        in.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSkip() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.skip(-1);
    }

    @Test
    public void testReadBoundsCheck() throws IOException {
        byte[] payload = "Testing Bounds".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZip("bounds.txt", payload, ZipArchiveOutputStream.STORED, false, false);
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        in.getNextZipEntry();

        byte[] buf = new byte[10];
        try {
            in.read(buf, -1, 5);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            in.read(buf, 0, -1);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            in.read(buf, 5, 10);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            in.read(buf, 11, 0);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        in.close();
    }

    @Test(expected = IOException.class)
    public void testReadClosedStream() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.read(new byte[10], 0, 10);
    }

    @Test
    public void testReadWithoutEntry() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertEquals(-1, in.read(new byte[10], 0, 10));
        in.close();
    }

    @Test
    public void testCanReadEntryData() {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertFalse(in.canReadEntryData(null));
        Assert.assertFalse(in.canReadEntryData(new ArchiveEntry() {
            @Override
            public String getName() { return "dummy"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        }));
    }

    @Test
    public void testConsecutiveEntriesAutoClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] z1 = createZip("file1.txt", "Content 1".getBytes(StandardCharsets.UTF_8), ZipArchiveOutputStream.STORED, false, false);
        byte[] z2 = createZip("file2.txt", "Content 2".getBytes(StandardCharsets.UTF_8), ZipArchiveOutputStream.STORED, false, false);
        baos.write(z1, 0, z1.length - 22 - 46);
        baos.write(z2);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry1 = in.getNextZipEntry();
        Assert.assertNotNull(entry1);
        Assert.assertEquals("file1.txt", entry1.getName());

        ZipArchiveEntry entry2 = in.getNextZipEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals("file2.txt", entry2.getName());
        in.close();
    }

    @Test
    public void testEmptyStream() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertNull(in.getNextZipEntry());
        in.close();
    }

    @Test
    public void testInvalidSignatureReturnsNull() throws IOException {
        byte[] dummy = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(dummy));
        Assert.assertNull(in.getNextZipEntry());
        in.close();
    }
}
