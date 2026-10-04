package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveInputStreamTest {

    private byte[] createSimpleZipData(String entryName, byte[] content, int method) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setMethod(method);
        if (method == ZipEntry.STORED) {
            entry.setSize(content.length);
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

        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry1.txt");
        entry1.setMethod(ZipEntry.DEFLATED);
        zaos.putArchiveEntry(entry1);
        zaos.write("Hello World 1".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry2.txt");
        byte[] content2 = "Hello World 2 STORED".getBytes("UTF-8");
        entry2.setMethod(ZipEntry.STORED);
        entry2.setSize(content2.length);
        CRC32 crc = new CRC32();
        crc.update(content2);
        entry2.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry2);
        zaos.write(content2);
        zaos.closeArchiveEntry();

        zaos.close();
        return baos.toByteArray();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] data = "Testing deflated stream content reading.".getBytes("UTF-8");
        byte[] zip = createSimpleZipData("test.txt", data, ZipEntry.DEFLATED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals("test.txt", entry.getName());
            Assert.assertTrue(in.canReadEntryData(entry));

            byte[] buf = new byte[data.length];
            int read = in.read(buf, 0, buf.length);
            Assert.assertEquals(data.length, read);
            Assert.assertArrayEquals(data, buf);

            Assert.assertEquals(-1, in.read(buf, 0, buf.length));
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] data = "Testing stored stream content reading.".getBytes("UTF-8");
        byte[] zip = createSimpleZipData("stored.txt", data, ZipEntry.STORED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ArchiveEntry entry = in.getNextEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals("stored.txt", entry.getName());

            byte[] buf = new byte[10];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int r;
            while ((r = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, r);
            }
            Assert.assertArrayEquals(data, out.toByteArray());
            Assert.assertNull(in.getNextEntry());
        }
    }

    @Test
    public void testSkipAndMultiEntry() throws IOException {
        byte[] zip = createMultiEntryZipData();
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry e1 = in.getNextZipEntry();
            Assert.assertNotNull(e1);
            Assert.assertEquals("entry1.txt", e1.getName());

            // Skip all bytes
            long skipped = in.skip(100);
            Assert.assertEquals("Hello World 1".length(), skipped);

            ZipArchiveEntry e2 = in.getNextZipEntry();
            Assert.assertNotNull(e2);
            Assert.assertEquals("entry2.txt", e2.getName());

            // Read partial
            byte[] buf = new byte[5];
            int read = in.read(buf, 0, 5);
            Assert.assertEquals(5, read);
            Assert.assertEquals("Hello", new String(buf, 0, read, "UTF-8"));

            // Closing or moving to next entry drains/skips rest
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testConstructors() throws IOException {
        byte[] empty = new byte[0];
        try (ZipArchiveInputStream in1 = new ZipArchiveInputStream(new ByteArrayInputStream(empty))) {
            Assert.assertNull(in1.getNextZipEntry());
        }
        try (ZipArchiveInputStream in2 = new ZipArchiveInputStream(new ByteArrayInputStream(empty), "UTF-8")) {
            Assert.assertNull(in2.getNextZipEntry());
        }
        try (ZipArchiveInputStream in3 = new ZipArchiveInputStream(new ByteArrayInputStream(empty), "UTF-8", false)) {
            Assert.assertNull(in3.getNextZipEntry());
        }
        try (ZipArchiveInputStream in4 = new ZipArchiveInputStream(new ByteArrayInputStream(empty), "UTF-8", true, true)) {
            Assert.assertNull(in4.getNextZipEntry());
        }
    }

    @Test
    public void testMatches() {
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(lfh, 4));

        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(eocd, 4));

        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        Assert.assertTrue(ZipArchiveInputStream.matches(dd, 4));

        byte[] split = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        Assert.assertTrue(ZipArchiveInputStream.matches(split, 4));

        Assert.assertFalse(ZipArchiveInputStream.matches(lfh, 3));
        Assert.assertFalse(ZipArchiveInputStream.matches(new byte[]{0, 0, 0, 0}, 4));
    }

    @Test
    public void testCanReadEntryData() {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertFalse(in.canReadEntryData(null));

        ZipArchiveEntry valid = new ZipArchiveEntry("valid");
        valid.setMethod(ZipEntry.DEFLATED);
        Assert.assertTrue(in.canReadEntryData(valid));

        ZipArchiveEntry storedNoDD = new ZipArchiveEntry("stored");
        storedNoDD.setMethod(ZipEntry.STORED);
        Assert.assertTrue(in.canReadEntryData(storedNoDD));

        ZipArchiveEntry storedWithDD = new ZipArchiveEntry("storedDD");
        storedWithDD.setMethod(ZipEntry.STORED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useDataDescriptor(true);
        storedWithDD.setGeneralPurposeBit(gpb);
        Assert.assertFalse(in.canReadEntryData(storedWithDD));

        ZipArchiveInputStream inAllowDD = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true);
        Assert.assertTrue(inAllowDD.canReadEntryData(storedWithDD));

        ZipArchiveEntry unsupportedMethod = new ZipArchiveEntry("unknown");
        unsupportedMethod.setMethod(99);
        Assert.assertFalse(in.canReadEntryData(unsupportedMethod));
    }

    @Test
    public void testReadBoundsCheck() throws IOException {
        byte[] data = "12345".getBytes("UTF-8");
        byte[] zip = createSimpleZipData("test.txt", data, ZipEntry.STORED);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            in.getNextZipEntry();
            byte[] buf = new byte[10];
            try {
                in.read(buf, -1, 5);
                Assert.fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException expected) {}

            try {
                in.read(buf, 0, -1);
                Assert.fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException expected) {}

            try {
                in.read(buf, 6, 5);
                Assert.fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException expected) {}
        }
    }

    @Test
    public void testReadAndCloseOperations() throws IOException {
        byte[] zip = createSimpleZipData("test.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        Assert.assertNotNull(in.getNextZipEntry());
        in.close();
        try {
            in.read(new byte[1], 0, 1);
            Assert.fail("Expected IOException on read after close");
        } catch (IOException expected) {}
        Assert.assertNull(in.getNextZipEntry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSkip() throws IOException {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            in.skip(-1);
        }
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testSplitArchiveDetection() throws IOException {
        byte[] splitSig = ZipLong.DD_SIG.getBytes();
        byte[] data = new byte[30];
        System.arraycopy(splitSig, 0, data, 0, splitSig.length);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(data))) {
            in.getNextZipEntry();
        }
    }

    @Test
    public void testSingleSegmentSplitMarker() throws IOException {
        byte[] normalZip = createSimpleZipData("file.txt", "abc".getBytes("UTF-8"), ZipEntry.STORED);
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(splitMarker.length + normalZip.length);
        bb.put(splitMarker);
        bb.put(normalZip);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(bb.array()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals("file.txt", entry.getName());
        }
    }

    @Test
    public void testZipWithDataDescriptorStoredAllowed() throws IOException {
        byte[] payload = "stored with data descriptor content".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(payload);
        long crcVal = crc.getValue();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // LFH
        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new byte[]{20, 0}); // version needed
        baos.write(new byte[]{8, 0}); // GP flag with descriptor bit (bit 3 = 8)
        baos.write(new byte[]{0, 0}); // method STORED = 0
        baos.write(new byte[]{0, 0, 0, 0}); // time
        baos.write(new byte[]{0, 0, 0, 0}); // crc zeroed
        baos.write(new byte[]{0, 0, 0, 0}); // csize zeroed
        baos.write(new byte[]{0, 0, 0, 0}); // size zeroed
        byte[] nameBytes = "test_dd.txt".getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) nameBytes.length);
        bb.putShort((short) 0); // extra len
        baos.write(bb.array());
        baos.write(nameBytes);

        // Content
        baos.write(payload);

        // Data descriptor with signature
        baos.write(ZipLong.DD_SIG.getBytes());
        bb = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) crcVal);
        bb.putInt(payload.length);
        bb.putInt(payload.length);
        baos.write(bb.array());

        // CFH
        baos.write(ZipLong.CFH_SIG.getBytes());
        baos.write(new byte[42]); // remainder of CFH min header

        // EOCD
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new byte[18]); // rest of EOCD

        byte[] zipBytes = baos.toByteArray();

        // Reading without allow flag -> throws UnsupportedZipFeatureException on read
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", true, false)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            try {
                byte[] buf = new byte[64];
                in.read(buf, 0, buf.length);
                Assert.fail("Expected UnsupportedZipFeatureException");
            } catch (UnsupportedZipFeatureException expected) {
            }
        }

        // Reading with allow flag
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", true, true)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            byte[] buf = new byte[64];
            int r = in.read(buf, 0, buf.length);
            Assert.assertEquals(payload.length, r);
            Assert.assertArrayEquals(payload, java.util.Arrays.copyOf(buf, r));
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testZip64ExtraFieldInHeader() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new byte[]{45, 0}); // version
        baos.write(new byte[]{0, 0}); // GP flag
        baos.write(new byte[]{0, 0}); // method STORED
        baos.write(new byte[]{0, 0, 0, 0}); // time
        baos.write(new byte[]{1, 2, 3, 4}); // CRC
        baos.write(ZipLong.ZIP64_MAGIC.getBytes()); // cSize
        baos.write(ZipLong.ZIP64_MAGIC.getBytes()); // size

        byte[] nameBytes = "zip64.txt".getBytes("UTF-8");

        // Zip64 extra field: header ID (0x0001), size 16, uncompressed size 8, compressed size 8
        ByteBuffer extra = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        extra.putShort((short) 1); // Zip64ExtendedInformationExtraField.HEADER_ID
        extra.putShort((short) 16);
        extra.putLong(100L); // uncompressed size
        extra.putLong(100L); // compressed size

        ByteBuffer lens = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        lens.putShort((short) nameBytes.length);
        lens.putShort((short) extra.capacity());

        baos.write(lens.array());
        baos.write(nameBytes);
        baos.write(extra.array());

        // Dummy payload of 100 bytes
        baos.write(new byte[100]);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals(100L, entry.getSize());
            Assert.assertEquals(100L, entry.getCompressedSize());
        }
    }

    @Test
    public void testReadDeflatedWithDataDescriptor() throws IOException {
        byte[] payload = "Some deflated text content for descriptor test.".getBytes("UTF-8");
        ByteArrayOutputStream deflatedStream = new ByteArrayOutputStream();
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        def.setInput(payload);
        def.finish();
        byte[] buffer = new byte[128];
        while (!def.finished()) {
            int count = def.deflate(buffer);
            deflatedStream.write(buffer, 0, count);
        }
        byte[] deflatedData = deflatedStream.toByteArray();
        long cSize = deflatedData.length;
        long size = payload.length;
        CRC32 crc = new CRC32();
        crc.update(payload);
        long crcVal = crc.getValue();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{8, 0}); // DD flag
        baos.write(new byte[]{8, 0}); // DEFLATED = 8
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});

        byte[] nameBytes = "deflated_dd.txt".getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) nameBytes.length);
        bb.putShort((short) 0);
        baos.write(bb.array());
        baos.write(nameBytes);

        baos.write(deflatedData);

        // Data descriptor (without signature or with signature)
        baos.write(ZipLong.DD_SIG.getBytes());
        bb = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) crcVal);
        bb.putInt((int) cSize);
        bb.putInt((int) size);
        baos.write(bb.array());

        // CFH to end the file properly
        baos.write(ZipLong.CFH_SIG.getBytes());
        baos.write(new byte[42]);
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new byte[18]);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            byte[] outBuf = new byte[payload.length];
            int read = in.read(outBuf, 0, outBuf.length);
            Assert.assertEquals(payload.length, read);
            Assert.assertArrayEquals(payload, outBuf);
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testEmptyArchiveOrOnlyEocd() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        baos.write(new byte[18]);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testTruncatedStream() throws IOException {
        byte[] zip = createSimpleZipData("short.txt", new byte[]{1, 2, 3, 4}, ZipEntry.STORED);
        // Cut the header in half
        byte[] truncated = new byte[15];
        System.arraycopy(zip, 0, truncated, 0, truncated.length);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(truncated))) {
            Assert.assertNull(in.getNextZipEntry());
        }
    }

    @Test
    public void testUnsupportedCompressionMethodThrowsExceptionOnRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{99, 0}); // Unsupported method 99
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{5, 0, 0, 0}); // csize = 5
        baos.write(new byte[]{5, 0, 0, 0}); // size = 5

        byte[] nameBytes = "test.bin".getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) nameBytes.length);
        bb.putShort((short) 0);
        baos.write(bb.array());
        baos.write(nameBytes);
        baos.write(new byte[]{1, 2, 3, 4, 5});

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            try {
                in.read(new byte[10], 0, 10);
                Assert.fail("Expected UnsupportedZipFeatureException");
            } catch (UnsupportedZipFeatureException expected) {
            }
        }
    }

    @Test
    public void testUnicodeExtraFieldHandling() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipLong.LFH_SIG.getBytes());
        baos.write(new byte[]{20, 0});
        baos.write(new byte[]{0, 0}); // UTF8 bit NOT set
        baos.write(new byte[]{0, 0}); // STORED
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(new byte[]{0, 0, 0, 0});

        byte[] originalName = "ascii.txt".getBytes("US-ASCII");
        String unicodeName = "unicode-\u00e9.txt";
        byte[] unicodeNameBytes = unicodeName.getBytes("UTF-8");

        CRC32 crc = new CRC32();
        crc.update(originalName);
        long origCrc = crc.getValue();

        // InfoZIP Unicode Path Extra Field: header 0x7075, length, version 1, CRC32 of orig name, UTF-8 string
        ByteBuffer extra = ByteBuffer.allocate(4 + 1 + 4 + unicodeNameBytes.length).order(ByteOrder.LITTLE_ENDIAN);
        extra.putShort((short) 0x7075);
        extra.putShort((short) (1 + 4 + unicodeNameBytes.length));
        extra.put((byte) 1);
        extra.putInt((int) origCrc);
        extra.put(unicodeNameBytes);

        ByteBuffer lens = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        lens.putShort((short) originalName.length);
        lens.putShort((short) extra.capacity());

        baos.write(lens.array());
        baos.write(originalName);
        baos.write(extra.array());

        // With useUnicodeExtraFields = true
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", true)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals(unicodeName, entry.getName());
        }

        // With useUnicodeExtraFields = false
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", false)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            Assert.assertNotNull(entry);
            Assert.assertEquals("ascii.txt", entry.getName());
        }
    }
}
