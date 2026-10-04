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

    private byte[] createZipData(String name, byte[] content, int method, boolean useDataDescriptor, boolean zip64) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        if (content != null) {
            crc.update(content);
        }

        byte[] compressedData = content;
        if (method == ZipArchiveOutputStream.DEFLATED && content != null) {
            ByteArrayOutputStream deflatedStream = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            deflater.setInput(content);
            deflater.finish();
            byte[] buf = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buf);
                deflatedStream.write(buf, 0, count);
            }
            deflater.end();
            compressedData = deflatedStream.toByteArray();
        }

        byte[] nameBytes = name.getBytes("UTF-8");
        int flags = useDataDescriptor ? 8 : 0;
        flags |= 0x0800;

        ByteArrayOutputStream extra = new ByteArrayOutputStream();
        if (zip64) {
            extra.write(new byte[]{0x01, 0x00, 0x10, 0x00});
            extra.write(new ZipEightByteInteger(content == null ? 0 : content.length).getBytes());
            extra.write(new ZipEightByteInteger(compressedData == null ? 0 : compressedData.length).getBytes());
        }
        byte[] extraBytes = extra.toByteArray();

        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new ZipShort(20).getBytes());
        baos.write(new ZipShort(flags).getBytes());
        baos.write(new ZipShort(method).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());

        if (useDataDescriptor) {
            baos.write(new byte[12]);
        } else {
            baos.write(new ZipLong(crc.getValue()).getBytes());
            if (zip64) {
                baos.write(ZipLong.ZIP64_MAGIC.getBytes());
                baos.write(ZipLong.ZIP64_MAGIC.getBytes());
            } else {
                baos.write(new ZipLong(compressedData == null ? 0 : compressedData.length).getBytes());
                baos.write(new ZipLong(content == null ? 0 : content.length).getBytes());
            }
        }

        baos.write(new ZipShort(nameBytes.length).getBytes());
        baos.write(new ZipShort(extraBytes.length).getBytes());
        baos.write(nameBytes);
        baos.write(extraBytes);

        if (compressedData != null) {
            baos.write(compressedData);
        }

        if (useDataDescriptor) {
            baos.write(ZipLong.DD_SIG.getBytes());
            baos.write(new ZipLong(crc.getValue()).getBytes());
            baos.write(new ZipLong(compressedData == null ? 0 : compressedData.length).getBytes());
            baos.write(new ZipLong(content == null ? 0 : content.length).getBytes());
        }

        int lfhOffset = 0;
        int cdOffset = baos.size();

        baos.write(ZipLong.CFH_SIG.getBytes());
        baos.write(new ZipShort(20).getBytes());
        baos.write(new ZipShort(20).getBytes());
        baos.write(new ZipShort(flags).getBytes());
        baos.write(new ZipShort(method).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipLong(crc.getValue()).getBytes());
        baos.write(new ZipLong(compressedData == null ? 0 : compressedData.length).getBytes());
        baos.write(new ZipLong(content == null ? 0 : content.length).getBytes());
        baos.write(new ZipShort(nameBytes.length).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipLong(0).getBytes());
        baos.write(new ZipLong(lfhOffset).getBytes());
        baos.write(nameBytes);

        int cdSize = baos.size() - cdOffset;

        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(0).getBytes());
        baos.write(new ZipShort(1).getBytes());
        baos.write(new ZipShort(1).getBytes());
        baos.write(new ZipLong(cdSize).getBytes());
        baos.write(new ZipLong(cdOffset).getBytes());
        baos.write(new ZipShort(0).getBytes());

        return baos.toByteArray();
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] payload = "Hello World".getBytes("UTF-8");
        byte[] zip = createZipData("test.txt", payload, ZipArchiveOutputStream.STORED, false, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertEquals(payload.length, entry.getSize());
        Assert.assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());

        byte[] buf = new byte[64];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertEquals("Hello World", new String(buf, 0, read, "UTF-8"));
        Assert.assertEquals(-1, zis.read(buf, 0, buf.length));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] payload = "Compression test with Deflate algorithm.".getBytes("UTF-8");
        byte[] zip = createZipData("deflated.txt", payload, ZipArchiveOutputStream.DEFLATED, false, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip), "UTF-8", true);
        ZipArchiveEntry entry = (ZipArchiveEntry) zis.getNextEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("deflated.txt", entry.getName());

        byte[] buf = new byte[128];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertEquals("Compression test with Deflate algorithm.", new String(buf, 0, read, "UTF-8"));
        Assert.assertEquals(-1, zis.read(buf, 0, buf.length));
        Assert.assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testZip64ExtraFieldProcessing() throws IOException {
        byte[] payload = "Zip64 content".getBytes("UTF-8");
        byte[] zip = createZipData("zip64.txt", payload, ZipArchiveOutputStream.STORED, false, true);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("zip64.txt", entry.getName());
        Assert.assertEquals(payload.length, entry.getSize());
        Assert.assertEquals(payload.length, entry.getCompressedSize());

        byte[] buf = new byte[32];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        zis.close();
    }

    @Test
    public void testCanReadEntryData() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        Assert.assertTrue(zis.canReadEntryData(entry));

        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        Assert.assertTrue(zis.canReadEntryData(entry));

        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpb);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        Assert.assertFalse(zis.canReadEntryData(entry));

        ZipArchiveInputStream zisAllowDD = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true);
        Assert.assertTrue(zisAllowDD.canReadEntryData(entry));

        ArchiveEntry nonZipEntry = new ArchiveEntry() {
            public String getName() { return "custom"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return null; }
        };
        Assert.assertFalse(zis.canReadEntryData(nonZipEntry));
        try {
            zis.close();
            zisAllowDD.close();
        } catch (IOException ignored) {}
    }

    @Test
    public void testMatches() {
        byte[] lfhSig = ZipArchiveOutputStream.LFH_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(lfhSig, 4));

        byte[] eocdSig = ZipArchiveOutputStream.EOCD_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(eocdSig, 4));

        byte[] ddSig = ZipArchiveOutputStream.DD_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(ddSig, 4));

        byte[] splitSig = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        Assert.assertTrue(ZipArchiveInputStream.matches(splitSig, 4));

        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[]{0, 0, 0, 0}, 4));
        Assert.assertFalse(ZipArchiveInputStream.matches(lfhSig, 3));
    }

    @Test
    public void testSkip() throws IOException {
        byte[] payload = "SkipTestDataContent".getBytes("UTF-8");
        byte[] zip = createZipData("skip.txt", payload, ZipArchiveOutputStream.STORED, false, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.getNextZipEntry();

        long skipped = zis.skip(4);
        Assert.assertEquals(4, skipped);

        byte[] buf = new byte[4];
        int read = zis.read(buf, 0, 4);
        Assert.assertEquals(4, read);
        Assert.assertEquals("Test", new String(buf, 0, read, "UTF-8"));

        long remainingSkipped = zis.skip(100);
        Assert.assertEquals(payload.length - 8, remainingSkipped);

        Assert.assertEquals(0, zis.skip(10));
        zis.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            zis.skip(-1);
        } finally {
            zis.close();
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testReadOutOfBounds() throws IOException {
        byte[] zip = createZipData("oob.txt", new byte[]{1, 2, 3}, ZipArchiveOutputStream.STORED, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.getNextZipEntry();
        try {
            zis.read(new byte[5], 2, 10);
        } finally {
            zis.close();
        }
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.read(new byte[1], 0, 1);
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testUnsupportedSplitArchive() throws IOException {
        byte[] splitZip = new byte[30];
        System.arraycopy(ZipLong.DD_SIG.getBytes(), 0, splitZip, 0, 4);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(splitZip));
        try {
            zis.getNextZipEntry();
        } finally {
            zis.close();
        }
    }

    @Test
    public void testSingleSegmentSplitMarker() throws IOException {
        byte[] zip = createZipData("test.txt", "abc".getBytes("UTF-8"), ZipArchiveOutputStream.STORED, false, false);
        byte[] markerZip = new byte[zip.length + 4];
        System.arraycopy(ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes(), 0, markerZip, 0, 4);
        System.arraycopy(zip, 0, markerZip, 4, zip.length);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(markerZip));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        zis.close();
    }

    @Test
    public void testSkipRemainderOfArchiveOnCentralDirectory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipLong.CFH_SIG.getBytes());
        baos.write(new byte[42]);
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new byte[16]);
        baos.write(new ZipShort(0).getBytes());

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadDeflatedEntryWithDataDescriptor() throws IOException {
        byte[] payload = "Test deflated with DD".getBytes("UTF-8");
        byte[] zip = createZipData("deflate_dd.txt", payload, ZipArchiveOutputStream.DEFLATED, true, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("deflate_dd.txt", entry.getName());

        byte[] buf = new byte[64];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertEquals("Test deflated with DD", new String(buf, 0, read, "UTF-8"));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadStoredEntryWithDataDescriptorAllowed() throws IOException {
        byte[] payload = "Stored data descriptor test".getBytes("UTF-8");
        byte[] zip = createZipData("stored_dd.txt", payload, ZipArchiveOutputStream.STORED, true, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip), "UTF-8", true, true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("stored_dd.txt", entry.getName());

        byte[] buf = new byte[64];
        int read = zis.read(buf, 0, buf.length);
        Assert.assertEquals(payload.length, read);
        Assert.assertEquals("Stored data descriptor test", new String(buf, 0, read, "UTF-8"));

        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testReadStoredEntryWithDataDescriptorDisallowed() throws IOException {
        byte[] payload = "Stored data descriptor not allowed".getBytes("UTF-8");
        byte[] zip = createZipData("stored_dd_disallowed.txt", payload, ZipArchiveOutputStream.STORED, true, false);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip), "UTF-8", true, false);
        zis.getNextZipEntry();
        try {
            zis.read(new byte[10], 0, 10);
        } finally {
            zis.close();
        }
    }

    @Test
    public void testCloseEntryWithoutReadingAllData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] zip1 = createZipData("file1.txt", "Content 123456789".getBytes("UTF-8"), ZipArchiveOutputStream.STORED, false, false);
        baos.write(zip1);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("file1.txt", entry.getName());
        Assert.assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testEmptyInputStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertNull(zis.getNextZipEntry());
        Assert.assertEquals(-1, zis.read(new byte[10], 0, 10));
        zis.close();
    }
}
