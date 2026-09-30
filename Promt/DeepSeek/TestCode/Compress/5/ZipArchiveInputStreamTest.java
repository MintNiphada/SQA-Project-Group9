package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import org.junit.Test;

public class ZipArchiveInputStreamTest {

    private static final byte[] LFH_SIG = ZipArchiveOutputStream.LFH_SIG;
    private static final byte[] CFH_SIG = ZipLong.CFH_SIG.getBytes();
    private static final byte[] EOCD_SIG = ZipArchiveOutputStream.EOCD_SIG;

    @Test
    public void testMatchesLFHSignature() {
        assertTrue(ZipArchiveInputStream.matches(LFH_SIG, LFH_SIG.length));
    }

    @Test
    public void testMatchesEOCDSignature() {
        assertTrue(ZipArchiveInputStream.matches(EOCD_SIG, EOCD_SIG.length));
    }

    @Test
    public void testMatchesShortLength() {
        assertFalse(ZipArchiveInputStream.matches(new byte[] { 0x50, 0x4b, 0x03, 0x04 }, 3));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] invalid = new byte[] { 0x50, 0x4b, 0x03, 0x05 };
        assertFalse(ZipArchiveInputStream.matches(invalid, invalid.length));
    }

    @Test
    public void testConstructorDefault() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis);
    }

    @Test
    public void testConstructorWithEncoding() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in, "UTF-8", true);
        assertNotNull(zis);
    }

    @Test
    public void testConstructorWithNullEncoding() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in, null, false);
        assertNotNull(zis);
    }

    @Test
    public void testGetNextZipEntryEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNull(zis.getNextZipEntry());
    }

    @Test
    public void testGetNextZipEntryCentralDirectory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(CFH_SIG, 0, 4);
        // fill rest of LFH_LEN with zeros to avoid EOF
        byte[] padding = new byte[ZipArchiveOutputStream.LFH_SIG.length - 4];
        baos.write(padding, 0, padding.length);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNull(zis.getNextZipEntry());
    }

    @Test
    public void testGetNextZipEntryInvalidSignature() throws IOException {
        byte[] invalidSig = new byte[] { 0x50, 0x4b, 0x03, 0x05 };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(invalidSig);
        byte[] padding = new byte[ZipArchiveOutputStream.LFH_SIG.length - 4];
        baos.write(padding);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNull(zis.getNextZipEntry());
    }

    @Test
    public void testGetNextZipEntryStoredNoDataDescriptor() throws IOException {
        String entryName = "test.txt";
        byte[] content = "Hello, World!".getBytes("UTF-8");
        InputStream in = createStoredEntryStream(entryName, content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(entryName, entry.getName());
        assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        assertEquals(content.length, entry.getSize());
        CRC32 crc = new CRC32();
        crc.update(content);
        assertEquals(crc.getValue(), entry.getCrc());
    }

    @Test
    public void testGetNextZipEntryDeflatedWithDataDescriptor() throws IOException {
        String entryName = "deflated.bin";
        byte[] content = "Some data to compress".getBytes("UTF-8");
        InputStream in = createDeflatedEntryStream(entryName, content, true, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(entryName, entry.getName());
        assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());
        // size is not set when data descriptor is present
        assertEquals(-1, entry.getSize());
    }

    @Test
    public void testGetNextZipEntryWithEFSFlag() throws IOException {
        String entryName = "t\u00e9st.txt"; // e acute
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream(entryName, content, false, true, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(entryName, entry.getName());
    }

    @Test
    public void testGetNextZipEntryWithUnicodeExtraFields() throws IOException {
        String originalName = "dummy";
        String unicodeName = "r\u00e9alname";
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryWithUnicodeExtraField(originalName, unicodeName, content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in, "UTF-8", true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(unicodeName, entry.getName());
    }

    @Test
    public void testGetNextZipEntryWithUnicodeExtraFieldsDisabled() throws IOException {
        String originalName = "dummy";
        String unicodeName = "r\u00e9alname";
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryWithUnicodeExtraField(originalName, unicodeName, content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in, "UTF-8", false);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(originalName, entry.getName());
    }

    @Test
    public void testGetNextZipEntryMultipleEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content1 = "first".getBytes("UTF-8");
        byte[] content2 = "second".getBytes("UTF-8");
        writeStoredEntry(baos, "file1", content1, false, false, false);
        writeStoredEntry(baos, "file2", content2, false, false, false);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        ZipArchiveEntry e1 = zis.getNextZipEntry();
        assertEquals("file1", e1.getName());
        ZipArchiveEntry e2 = zis.getNextZipEntry();
        assertEquals("file2", e2.getName());
        assertNull(zis.getNextZipEntry());
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello, stored entry!".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("stored", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buffer = new byte[content.length];
        int read = zis.read(buffer, 0, buffer.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buffer);
        assertEquals(-1, zis.read(buffer, 0, 1));
    }

    @Test
    public void testReadStoredEntryBufferBoundaries() throws IOException {
        byte[] content = new byte[ZipArchiveOutputStream.BUFFER_SIZE + 100];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        InputStream in = createStoredEntryStream("large", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buffer = new byte[content.length];
        int total = 0;
        while (total < content.length) {
            int read = zis.read(buffer, total, content.length - total);
            if (read == -1) break;
            total += read;
        }
        assertEquals(content.length, total);
        assertArrayEquals(content, buffer);
    }

    @Test
    public void testReadStoredEntryPastEnd() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("s", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buf = new byte[10];
        int read = zis.read(buf, 0, buf.length);
        assertEquals(content.length, read);
        assertEquals(-1, zis.read(buf, 0, 1));
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] content = "This is a test for deflated reading.".getBytes("UTF-8");
        InputStream in = createDeflatedEntryStream("deflated", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buffer = new byte[content.length];
        int total = 0;
        while (total < content.length) {
            int read = zis.read(buffer, total, content.length - total);
            if (read == -1) break;
            total += read;
        }
        assertEquals(content.length, total);
        assertArrayEquals(content, buffer);
    }

    @Test(expected = ZipException.class)
    public void testReadDeflatedEntryDataFormatException() throws IOException {
        // Provide invalid deflated data
        byte[] invalidDeflated = new byte[] { 0x00, 0x01, 0x02 };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeDeflatedEntry(baos, "bad", invalidDeflated, invalidDeflated, false, false, false);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buf = new byte[10];
        zis.read(buf, 0, buf.length); // should throw ZipException
    }

    @Test(expected = IOException.class)
    public void testReadClosedStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        zis.close();
        zis.read(new byte[1], 0, 1);
    }

    @Test
    public void testReadNullCurrent() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertEquals(-1, zis.read(new byte[1], 0, 1));
    }

    @Test
    public void testReadFinishedInflater() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createDeflatedEntryStream("f", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        byte[] buf = new byte[content.length];
        assertEquals(content.length, zis.read(buf, 0, buf.length));
        assertEquals(-1, zis.read(buf, 0, 1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testReadInvalidStartNegative() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("s", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        zis.read(new byte[5], -1, 1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testReadInvalidLengthNegative() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("s", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        zis.read(new byte[5], 0, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testReadInvalidBufferTooSmall() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("s", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        zis.read(new byte[5], 3, 3);
    }

    @Test
    public void testClose() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        zis.close();
        // subsequent operations should throw IOException
        try {
            zis.getNextZipEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testCloseAlreadyClosed() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        zis.close();
        zis.close(); // should not throw
    }

    @Test
    public void testSkipPositive() throws IOException {
        byte[] content = "This is a test for skipping.".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("skip", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        long skipped = zis.skip(5);
        assertEquals(5, skipped);
        byte[] rest = new byte[content.length - 5];
        int read = zis.read(rest, 0, rest.length);
        assertEquals(rest.length, read);
        byte[] expectedRest = new byte[rest.length];
        System.arraycopy(content, 5, expectedRest, 0, rest.length);
        assertArrayEquals(expectedRest, rest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        zis.skip(-1);
    }

    @Test
    public void testSkipPastEnd() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        InputStream in = createStoredEntryStream("s", content, false, false, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        assertNotNull(zis.getNextZipEntry());
        long skipped = zis.skip(100);
        assertEquals(content.length, skipped);
        assertEquals(-1, zis.read(new byte[1], 0, 1));
    }

    @Test
    public void testCloseEntryPushback() throws IOException {
        // Create two stored entries, read part of first, then get next entry to trigger closeEntry
        byte[] content1 = "first entry data".getBytes("UTF-8");
        byte[] content2 = "second entry data".getBytes("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeStoredEntry(baos, "file1", content1, false, false, false);
        writeStoredEntry(baos, "file2", content2, false, false, false);
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(in);
        ZipArchiveEntry e1 = zis.getNextZipEntry();
        assertNotNull(e1);
        // read only part of first entry
        byte[] partial = new byte[5];
        int read = zis.read(partial, 0, partial.length);
        assertEquals(5, read);
        // now get next entry, which should close first and position to second
        ZipArchiveEntry e2 = zis.getNextZipEntry();
        assertNotNull(e2);
        assertEquals("file2", e2.getName());
        // read second entry fully
        byte[] full2 = new byte[content2.length];
        int total = 0;
        while (total < content2.length) {
            int r = zis.read(full2, total, content2.length - total);
            if (r == -1) break;
            total += r;
        }
        assertEquals(content2.length, total);
        assertArrayEquals(content2, full2);
    }

    // Helper methods to create test streams

    private InputStream createStoredEntryStream(String name, byte[] content, boolean useDataDescriptor,
                                                boolean useEFS, boolean useUnicodeExtra) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeStoredEntry(baos, name, content, useDataDescriptor, useEFS, useUnicodeExtra);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void writeStoredEntry(ByteArrayOutputStream baos, String name, byte[] content,
                                  boolean useDataDescriptor, boolean useEFS, boolean useUnicodeExtra) throws IOException {
        byte[] nameBytes = name.getBytes("UTF-8");
        byte[] extra = useUnicodeExtra ? createUnicodeExtraField(nameBytes, name) : new byte[0];
        int method = ZipArchiveOutputStream.STORED;
        int flags = 0;
        if (useDataDescriptor) {
            flags |= 8;
        }
        if (useEFS) {
            flags |= ZipArchiveOutputStream.EFS_FLAG;
        }
        CRC32 crc = new CRC32();
        crc.update(content);
        long crcValue = crc.getValue();
        long size = content.length;
        long compressedSize = size;

        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(LFH_SIG);
        header.write(ZipShort.getBytes(20)); // version needed to extract
        header.write(ZipShort.getBytes(flags));
        header.write(ZipShort.getBytes(method));
        header.write(ZipLong.getBytes(ZipUtil.javaToDosTime(System.currentTimeMillis())));
        if (!useDataDescriptor) {
            header.write(ZipLong.getBytes(crcValue));
            header.write(ZipLong.getBytes(compressedSize));
            header.write(ZipLong.getBytes(size));
        } else {
            header.write(new byte[12]); // zero crc, compressed size, uncompressed size
        }
        header.write(ZipShort.getBytes(nameBytes.length));
        header.write(ZipShort.getBytes(extra.length));
        header.write(nameBytes);
        header.write(extra);
        baos.write(header.toByteArray());
        baos.write(content);
        if (useDataDescriptor) {
            ByteArrayOutputStream dd = new ByteArrayOutputStream();
            dd.write(ZipLong.getBytes(crcValue));
            dd.write(ZipLong.getBytes(compressedSize));
            dd.write(ZipLong.getBytes(size));
            baos.write(dd.toByteArray());
        }
    }

    private InputStream createDeflatedEntryStream(String name, byte[] content, boolean useDataDescriptor,
                                                  boolean useEFS, boolean useUnicodeExtra) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeDeflatedEntry(baos, name, content, content, useDataDescriptor, useEFS, useUnicodeExtra);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void writeDeflatedEntry(ByteArrayOutputStream baos, String name, byte[] uncompressed,
                                    byte[] contentForCrc, boolean useDataDescriptor, boolean useEFS,
                                    boolean useUnicodeExtra) throws IOException {
        byte[] nameBytes = name.getBytes("UTF-8");
        byte[] extra = useUnicodeExtra ? createUnicodeExtraField(nameBytes, name) : new byte[0];
        int method = ZipArchiveOutputStream.DEFLATED;
        int flags = 0;
        if (useDataDescriptor) {
            flags |= 8;
        }
        if (useEFS) {
            flags |= ZipArchiveOutputStream.EFS_FLAG;
        }
        CRC32 crc = new CRC32();
        crc.update(contentForCrc);
        long crcValue = crc.getValue();
        long size = uncompressed.length;

        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        deflater.setInput(uncompressed);
        deflater.finish();
        ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        while (!deflater.finished()) {
            int len = deflater.deflate(buf);
            if (len > 0) {
                compressedOut.write(buf, 0, len);
            }
        }
        byte[] compressed = compressedOut.toByteArray();
        long compressedSize = compressed.length;

        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(LFH_SIG);
        header.write(ZipShort.getBytes(20));
        header.write(ZipShort.getBytes(flags));
        header.write(ZipShort.getBytes(method));
        header.write(ZipLong.getBytes(ZipUtil.javaToDosTime(System.currentTimeMillis())));
        if (!useDataDescriptor) {
            header.write(ZipLong.getBytes(crcValue));
            header.write(ZipLong.getBytes(compressedSize));
            header.write(ZipLong.getBytes(size));
        } else {
            header.write(new byte[12]);
        }
        header.write(ZipShort.getBytes(nameBytes.length));
        header.write(ZipShort.getBytes(extra.length));
        header.write(nameBytes);
        header.write(extra);
        baos.write(header.toByteArray());
        baos.write(compressed);
        if (useDataDescriptor) {
            ByteArrayOutputStream dd = new ByteArrayOutputStream();
            dd.write(ZipLong.getBytes(crcValue));
            dd.write(ZipLong.getBytes(compressedSize));
            dd.write(ZipLong.getBytes(size));
            baos.write(dd.toByteArray());
        }
    }

    private InputStream createStoredEntryWithUnicodeExtraField(String originalName, String unicodeName,
                                                               byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] originalNameBytes = originalName.getBytes("UTF-8");
        byte[] unicodeExtra = createUnicodeExtraField(originalNameBytes, unicodeName);
        int method = ZipArchiveOutputStream.STORED;
        int flags = 0;
        CRC32 crc = new CRC32();
        crc.update(content);
        long crcValue = crc.getValue();
        long size = content.length;
        long compressedSize = size;

        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(LFH_SIG);
        header.write(ZipShort.getBytes(20));
        header.write(ZipShort.getBytes(flags));
        header.write(ZipShort.getBytes(method));
        header.write(ZipLong.getBytes(ZipUtil.javaToDosTime(System.currentTimeMillis())));
        header.write(ZipLong.getBytes(crcValue));
        header.write(ZipLong.getBytes(compressedSize));
        header.write(ZipLong.getBytes(size));
        header.write(ZipShort.getBytes(originalNameBytes.length));
        header.write(ZipShort.getBytes(unicodeExtra.length));
        header.write(originalNameBytes);
        header.write(unicodeExtra);
        baos.write(header.toByteArray());
        baos.write(content);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private byte[] createUnicodeExtraField(byte[] originalNameBytes, String unicodeName) throws IOException {
        ByteArrayOutputStream extra = new ByteArrayOutputStream();
        extra.write(ZipShort.getBytes(0x7075)); // Info-ZIP Unicode Path Extra Field
        byte[] unicodeNameBytes = unicodeName.getBytes("UTF-8");
        int dataLength = 1 + 4 + unicodeNameBytes.length; // version(1) + crc32(4) + name
        extra.write(ZipShort.getBytes(dataLength));
        extra.write(1); // version
        CRC32 crc = new CRC32();
        crc.update(originalNameBytes);
        extra.write(ZipLong.getBytes(crc.getValue()));
        extra.write(unicodeNameBytes);
        return extra.toByteArray();
    }
}
