package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.junit.Test;

public class ZipArchiveInputStreamTest {

    private byte[] createZipWithStoredEntry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        return bos.toByteArray();
    }

    private byte[] createZipWithDeflatedEntry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        return bos.toByteArray();
    }

    private byte[] createZipWithStoredEntryAndDataDescriptor(String name, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        GeneralPurposeBit gpBit = new GeneralPurposeBit();
        gpBit.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpBit);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        return bos.toByteArray();
    }

    private byte[] createZipWithDeflatedEntryAndDataDescriptor(String name, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.DEFLATED);
        GeneralPurposeBit gpBit = new GeneralPurposeBit();
        gpBit.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpBit);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        return bos.toByteArray();
    }

    private byte[] createZipWithZip64Entry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        Zip64ExtendedInformationExtraField z64 = new Zip64ExtendedInformationExtraField();
        z64.setSize(new ZipEightByteInteger(content.length));
        z64.setCompressedSize(new ZipEightByteInteger(content.length));
        entry.addExtraField(z64);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        return bos.toByteArray();
    }

    @Test
    public void testMatchesWithValidLFHSignature() {
        byte[] sig = ZipLong.LFH_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testMatchesWithEOCDSignature() {
        byte[] sig = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testMatchesWithDDSignature() {
        byte[] sig = ZipLong.DD_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testMatchesWithSingleSegmentSplitMarker() {
        byte[] sig = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testMatchesWithInvalidSignature() {
        byte[] sig = new byte[] { 0, 0, 0, 0 };
        assertFalse(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testMatchesWithShortLength() {
        byte[] sig = new byte[] { 0x50, 0x4b };
        assertFalse(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test
    public void testConstructorDefaultEncoding() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        assertNotNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testConstructorWithEncoding() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8");
        assertNotNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testConstructorWithUnicodeExtraFields() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", false);
        assertNotNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testConstructorWithAllowStoredEntriesWithDataDescriptor() throws IOException {
        byte[] zipBytes = createZipWithStoredEntryAndDataDescriptor("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", true, true);
        assertNotNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testGetNextEntryOnEmptyStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testGetNextEntryOnClosedStream() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.close();
        assertNull(zis.getNextEntry());
    }

    @Test
    public void testGetNextEntryOnHitCentralDirectory() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        assertNotNull(zis.getNextEntry());
        assertNull(zis.getNextEntry());
        zis.close();
    }

    @Test
    public void testGetNextEntryWithStoredEntry() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithStoredEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        zis.close();
    }

    @Test
    public void testGetNextEntryWithDeflatedEntry() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithDeflatedEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        zis.close();
    }

    @Test
    public void testGetNextEntryWithDataDescriptorStored() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithStoredEntryAndDataDescriptor("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", true, true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        zis.close();
    }

    @Test
    public void testGetNextEntryWithDataDescriptorDeflated() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithDeflatedEntryAndDataDescriptor("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        zis.close();
    }

    @Test
    public void testGetNextEntryWithZip64() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithZip64Entry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        zis.close();
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testGetNextEntryWithSplitArchive() throws IOException {
        byte[] lfh = ZipLong.LFH_SIG.getBytes();
        byte[] dd = ZipLong.DD_SIG.getBytes();
        byte[] data = new byte[dd.length + lfh.length];
        System.arraycopy(dd, 0, data, 0, dd.length);
        System.arraycopy(lfh, 0, data, dd.length, lfh.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(data));
        try {
            zis.getNextZipEntry();
        } finally {
            zis.close();
        }
    }

    @Test
    public void testGetNextEntryWithSingleSegmentSplitMarker() throws IOException {
        byte[] marker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        byte[] lfh = ZipLong.LFH_SIG.getBytes();
        byte[] data = new byte[marker.length + lfh.length];
        System.arraycopy(marker, 0, data, 0, marker.length);
        System.arraycopy(lfh, 0, data, marker.length, lfh.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(data));
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipBytes = createZipWithStoredEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        byte[] buffer = new byte[content.length];
        int read = zis.read(buffer, 0, buffer.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buffer);
        assertEquals(-1, zis.read(buffer, 0, 1));
        zis.close();
    }

    @Test
    public void testReadDeflatedEntry() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipBytes = createZipWithDeflatedEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer, 0, buffer.length)) != -1) {
            bos.write(buffer, 0, len);
        }
        assertArrayEquals(content, bos.toByteArray());
        zis.close();
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.close();
        zis.read(new byte[1], 0, 1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testReadWithInvalidOffset() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[10]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextZipEntry();
        zis.read(new byte[5], -1, 1);
    }

    @Test(expected = UnsupportedZipFeatureException.class)
    public void testReadWithUnsupportedMethod() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[10]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        entry.setMethod(ZipMethod.BZIP2.getCode());
        zis.read(new byte[1], 0, 1);
    }

    @Test
    public void testReadWithDataDescriptorStored() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipBytes = createZipWithStoredEntryAndDataDescriptor("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "UTF-8", true, true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        byte[] buffer = new byte[content.length];
        int read = zis.read(buffer, 0, buffer.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buffer);
        assertEquals(-1, zis.read(buffer, 0, 1));
        zis.close();
    }

    @Test
    public void testReadWithDataDescriptorDeflated() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipBytes = createZipWithDeflatedEntryAndDataDescriptor("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer, 0, buffer.length)) != -1) {
            bos.write(buffer, 0, len);
        }
        assertArrayEquals(content, bos.toByteArray());
        zis.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipBytes = createZipWithStoredEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextZipEntry();
        long skipped = zis.skip(5);
        assertEquals(5, skipped);
        byte[] buffer = new byte[6];
        int read = zis.read(buffer, 0, buffer.length);
        assertEquals(6, read);
        assertEquals(" World", new String(buffer));
        zis.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[10]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextZipEntry();
        zis.skip(-1);
    }

    @Test
    public void testCanReadEntryDataWithStored() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.STORED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(zis.canReadEntryData(entry));
    }

    @Test
    public void testCanReadEntryDataWithDeflated() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.DEFLATED);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(zis.canReadEntryData(entry));
    }

    @Test
    public void testCanReadEntryDataWithUnsupportedMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipMethod.BZIP2.getCode());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(zis.canReadEntryData(entry));
    }

    @Test
    public void testCanReadEntryDataWithDataDescriptorAndNotAllowed() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.STORED);
        GeneralPurposeBit gpBit = new GeneralPurposeBit();
        gpBit.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpBit);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false);
        assertFalse(zis.canReadEntryData(entry));
    }

    @Test
    public void testClose() throws IOException {
        byte[] zipBytes = createZipWithStoredEntry("test.txt", new byte[0]);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.close();
        assertTrue(true);
    }

    @Test(expected = IOException.class)
    public void testReadDeflatedWithPresetDictionary() throws IOException {
        byte[] content = new byte[100];
        byte[] zipBytes = createZipWithDeflatedEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextZipEntry();
        zis.read(new byte[1], 0, 1);
    }

    @Test(expected = IOException.class)
    public void testReadDeflatedTruncated() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipBytes = createZipWithDeflatedEntry("test.txt", content);
        byte[] truncated = new byte[zipBytes.length - 10];
        System.arraycopy(zipBytes, 0, truncated, 0, truncated.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated));
        zis.getNextZipEntry();
        byte[] buffer = new byte[1024];
        while (zis.read(buffer, 0, buffer.length) != -1) {
        }
    }

    @Test
    public void testGetNextEntryAfterCloseEntry() throws IOException {
        byte[] content1 = "First".getBytes();
        byte[] content2 = "Second".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry1 = new ZipArchiveEntry("first.txt");
        entry1.setMethod(ZipEntry.STORED);
        entry1.setSize(content1.length);
        entry1.setCompressedSize(content1.length);
        CRC32 crc = new CRC32();
        crc.update(content1);
        entry1.setCrc(crc.getValue());
        zos.putArchiveEntry(entry1);
        zos.write(content1);
        zos.closeArchiveEntry();
        ZipArchiveEntry entry2 = new ZipArchiveEntry("second.txt");
        entry2.setMethod(ZipEntry.STORED);
        entry2.setSize(content2.length);
        entry2.setCompressedSize(content2.length);
        crc.reset();
        crc.update(content2);
        entry2.setCrc(crc.getValue());
        zos.putArchiveEntry(entry2);
        zos.write(content2);
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();
        byte[] zipBytes = bos.toByteArray();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry e1 = zis.getNextZipEntry();
        assertNotNull(e1);
        assertEquals("first.txt", e1.getName());
        byte[] buf = new byte[content1.length];
        zis.read(buf, 0, buf.length);
        ZipArchiveEntry e2 = zis.getNextZipEntry();
        assertNotNull(e2);
        assertEquals("second.txt", e2.getName());
        buf = new byte[content2.length];
        zis.read(buf, 0, buf.length);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test
    public void testBoundedInputStreamRead() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveInputStream.BoundedInputStream bis = zis.new BoundedInputStream(bais, 3);
        assertEquals(1, bis.read());
        assertEquals(2, bis.read());
        assertEquals(3, bis.read());
        assertEquals(-1, bis.read());
    }

    @Test
    public void testBoundedInputStreamReadArray() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveInputStream.BoundedInputStream bis = zis.new BoundedInputStream(bais, 3);
        byte[] buf = new byte[5];
        int read = bis.read(buf, 0, 5);
        assertEquals(3, read);
        assertEquals(1, buf[0]);
        assertEquals(2, buf[1]);
        assertEquals(3, buf[2]);
        assertEquals(-1, bis.read());
    }

    @Test
    public void testBoundedInputStreamSkip() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveInputStream.BoundedInputStream bis = zis.new BoundedInputStream(bais, 4);
        long skipped = bis.skip(2);
        assertEquals(2, skipped);
        assertEquals(3, bis.read());
        assertEquals(4, bis.read());
        assertEquals(-1, bis.read());
    }

    @Test
    public void testBoundedInputStreamAvailable() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveInputStream.BoundedInputStream bis = zis.new BoundedInputStream(bais, 3);
        assertEquals(3, bis.available());
        bis.read();
        assertEquals(2, bis.available());
        bis.read();
        bis.read();
        assertEquals(0, bis.available());
    }
}
