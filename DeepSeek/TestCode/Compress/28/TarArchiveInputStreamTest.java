package org.apache.commons.compress.archivers.tar;

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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    private static final int RECORD_SIZE = 512;
    private static final int BLOCK_SIZE = 10240;

    private byte[] createHeader(String name, long size, byte typeflag) throws IOException {
        byte[] header = new byte[RECORD_SIZE];
        byte[] nameBytes = name.getBytes("UTF-8");
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
        String sizeStr = String.format("%011o", size);
        byte[] sizeBytes = sizeStr.getBytes("UTF-8");
        System.arraycopy(sizeBytes, 0, header, 124, sizeBytes.length);
        header[156] = typeflag;
        byte[] magic = "ustar ".getBytes("UTF-8");
        System.arraycopy(magic, 0, header, 257, magic.length);
        byte[] version = "00".getBytes("UTF-8");
        System.arraycopy(version, 0, header, 263, version.length);
        int checksum = 0;
        for (int i = 0; i < header.length; i++) {
            if (i >= 148 && i < 156) {
                checksum += ' ';
            } else {
                checksum += header[i] & 0xff;
            }
        }
        byte[] checksumBytes = String.format("%06o\0 ", checksum).getBytes("UTF-8");
        System.arraycopy(checksumBytes, 0, header, 148, checksumBytes.length);
        return header;
    }

    private byte[] createData(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }

    private byte[] createEOFBlock() {
        return new byte[RECORD_SIZE];
    }

    private byte[] createTarWithSingleEntry(String name, long size, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(createHeader(name, size, TarConstants.LF_NORMAL));
        bos.write(data);
        int padding = (int) (RECORD_SIZE - (size % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) {
            bos.write(new byte[padding]);
        }
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        return bos.toByteArray();
    }

    private byte[] createTarWithLongName(String name, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] longNameBytes = name.getBytes("UTF-8");
        byte[] longNameHeader = createHeader("././@LongLink", longNameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME);
        bos.write(longNameHeader);
        bos.write(longNameBytes);
        int padding = (int) (RECORD_SIZE - (longNameBytes.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) {
            bos.write(new byte[padding]);
        }
        bos.write(createHeader("short", data.length, TarConstants.LF_NORMAL));
        bos.write(data);
        padding = (int) (RECORD_SIZE - (data.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) {
            bos.write(new byte[padding]);
        }
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        return bos.toByteArray();
    }

    private byte[] createTarWithPaxHeaders(Map<String, String> headers, String name, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream paxData = new ByteArrayOutputStream();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String line = entry.getKey() + "=" + entry.getValue() + "\n";
            String lenStr = String.valueOf(line.length() + 1);
            paxData.write(lenStr.getBytes("UTF-8"));
            paxData.write(' ');
            paxData.write(line.getBytes("UTF-8"));
        }
        byte[] paxBytes = paxData.toByteArray();
        bos.write(createHeader("././@PaxHeader", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC));
        bos.write(paxBytes);
        int padding = (int) (RECORD_SIZE - (paxBytes.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) {
            bos.write(new byte[padding]);
        }
        bos.write(createHeader(name, data.length, TarConstants.LF_NORMAL));
        bos.write(data);
        padding = (int) (RECORD_SIZE - (data.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) {
            bos.write(new byte[padding]);
        }
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        return bos.toByteArray();
    }

    @Test
    public void testMatchesPosix() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar".getBytes(), 0, signature, 257, 5);
        System.arraycopy("00".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesGnuSpace() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar  ".getBytes(), 0, signature, 257, 8);
        System.arraycopy(" \0".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesGnuZero() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar  ".getBytes(), 0, signature, 257, 8);
        System.arraycopy("0\0".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesAnt() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar".getBytes(), 0, signature, 257, 5);
        System.arraycopy(" \0".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesInvalid() {
        byte[] signature = new byte[512];
        assertFalse(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesShortSignature() {
        byte[] signature = new byte[10];
        assertFalse(TarArchiveInputStream.matches(signature, 10));
    }

    @Test
    public void testGetNextTarEntryNormal() throws Exception {
        byte[] data = createData(100);
        byte[] tar = createTarWithSingleEntry("file.txt", 100, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("file.txt", entry.getName());
        assertEquals(100, entry.getSize());
        byte[] readData = new byte[100];
        int read = tis.read(readData, 0, 100);
        assertEquals(100, read);
        assertArrayEquals(data, readData);
        assertNull(tis.getNextTarEntry());
    }

    @Test
    public void testGetNextTarEntryTwoEntries() throws Exception {
        byte[] data1 = createData(50);
        byte[] data2 = createData(70);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(createHeader("file1", 50, TarConstants.LF_NORMAL));
        bos.write(data1);
        bos.write(new byte[RECORD_SIZE - 50]);
        bos.write(createHeader("file2", 70, TarConstants.LF_NORMAL));
        bos.write(data2);
        bos.write(new byte[RECORD_SIZE - 70]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        byte[] tar = bos.toByteArray();
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry1 = tis.getNextTarEntry();
        assertEquals("file1", entry1.getName());
        TarArchiveEntry entry2 = tis.getNextTarEntry();
        assertEquals("file2", entry2.getName());
        assertNull(tis.getNextTarEntry());
    }

    @Test
    public void testGetNextTarEntryAtEOF() throws Exception {
        byte[] tar = createTarWithSingleEntry("a", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        assertNotNull(tis.getNextTarEntry());
        assertNull(tis.getNextTarEntry());
        assertNull(tis.getNextTarEntry());
    }

    @Test
    public void testGetNextTarEntryLongName() throws Exception {
        String longName = "a".repeat(200);
        byte[] data = createData(10);
        byte[] tar = createTarWithLongName(longName, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
    }

    @Test
    public void testGetNextTarEntryLongLink() throws Exception {
        String longLink = "b".repeat(200);
        byte[] data = createData(10);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] longLinkBytes = longLink.getBytes("UTF-8");
        bos.write(createHeader("././@LongLink", longLinkBytes.length, TarConstants.LF_GNUTYPE_LONGLINK));
        bos.write(longLinkBytes);
        int padding = (int) (RECORD_SIZE - (longLinkBytes.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) bos.write(new byte[padding]);
        bos.write(createHeader("file", data.length, TarConstants.LF_SYMLINK));
        bos.write(data);
        padding = (int) (RECORD_SIZE - (data.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) bos.write(new byte[padding]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longLink, entry.getLinkName());
    }

    @Test
    public void testGetNextTarEntryPaxHeaders() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("path", "paxfile.txt");
        headers.put("size", "200");
        byte[] data = createData(200);
        byte[] tar = createTarWithPaxHeaders(headers, "original", data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("paxfile.txt", entry.getName());
        assertEquals(200, entry.getSize());
    }

    @Test
    public void testGetNextTarEntrySparse() throws Exception {
        byte[] header = createHeader("sparse", 100, TarConstants.LF_GNUTYPE_SPARSE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(new byte[100]);
        bos.write(new byte[RECORD_SIZE - 100]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertTrue(entry.isGNUSparse());
    }

    @Test
    public void testGetNextTarEntryMalformedHeader() throws Exception {
        byte[] badHeader = new byte[RECORD_SIZE];
        badHeader[0] = 1;
        ByteArrayInputStream bais = new ByteArrayInputStream(badHeader);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        try {
            tis.getNextTarEntry();
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void testGetNextTarEntryNullRecord() throws Exception {
        byte[] tar = new byte[RECORD_SIZE];
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        assertNull(tis.getNextTarEntry());
    }

    @Test
    public void testRead() throws Exception {
        byte[] data = createData(200);
        byte[] tar = createTarWithSingleEntry("f", 200, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        byte[] buf = new byte[100];
        int read = tis.read(buf, 0, 100);
        assertEquals(100, read);
        for (int i = 0; i < 100; i++) {
            assertEquals(data[i], buf[i]);
        }
    }

    @Test
    public void testReadPastEntry() throws Exception {
        byte[] data = createData(100);
        byte[] tar = createTarWithSingleEntry("f", 100, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        byte[] buf = new byte[200];
        int read = tis.read(buf, 0, 200);
        assertEquals(100, read);
        read = tis.read(buf, 0, 1);
        assertEquals(-1, read);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadNoCurrentEntry() throws Exception {
        byte[] tar = createTarWithSingleEntry("f", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.read(new byte[1], 0, 1);
    }

    @Test
    public void testReadAfterEOF() throws Exception {
        byte[] tar = createTarWithSingleEntry("f", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        assertEquals(-1, tis.read(new byte[1], 0, 1));
        assertEquals(-1, tis.read(new byte[1], 0, 1));
    }

    @Test
    public void testSkip() throws Exception {
        byte[] data = createData(200);
        byte[] tar = createTarWithSingleEntry("f", 200, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        long skipped = tis.skip(50);
        assertEquals(50, skipped);
        byte[] buf = new byte[150];
        int read = tis.read(buf, 0, 150);
        assertEquals(150, read);
        for (int i = 0; i < 150; i++) {
            assertEquals(data[i + 50], buf[i]);
        }
    }

    @Test
    public void testSkipPastEntry() throws Exception {
        byte[] data = createData(100);
        byte[] tar = createTarWithSingleEntry("f", 100, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        long skipped = tis.skip(200);
        assertEquals(100, skipped);
        assertEquals(-1, tis.read(new byte[1], 0, 1));
    }

    @Test
    public void testAvailable() throws Exception {
        byte[] data = createData(200);
        byte[] tar = createTarWithSingleEntry("f", 200, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        assertEquals(200, tis.available());
        tis.read(new byte[50], 0, 50);
        assertEquals(150, tis.available());
    }

    @Test
    public void testAvailableLarge() throws Exception {
        long size = (long) Integer.MAX_VALUE + 1;
        byte[] header = createHeader("big", size, TarConstants.LF_NORMAL);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(new byte[RECORD_SIZE]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        tis.getNextTarEntry();
        assertEquals(Integer.MAX_VALUE, tis.available());
    }

    @Test
    public void testClose() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.close();
        try {
            bais.read();
            fail("Stream should be closed");
        } catch (IOException e) {
        }
    }

    @Test
    public void testReset() {
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tis.reset();
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveEntry normal = new TarArchiveEntry("normal");
        normal.setSize(100);
        TarArchiveEntry sparse = new TarArchiveEntry("sparse");
        sparse.setSize(100);
        sparse.setGNUSparse(true);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tis.canReadEntryData(normal));
        assertFalse(tis.canReadEntryData(sparse));
        assertFalse(tis.canReadEntryData(new Object()));
    }

    @Test
    public void testGetCurrentEntry() throws Exception {
        byte[] tar = createTarWithSingleEntry("f", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        assertNull(tis.getCurrentEntry());
        tis.getNextTarEntry();
        assertNotNull(tis.getCurrentEntry());
    }

    @Test
    public void testIsEOFRecord() {
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tis.isEOFRecord(null));
        assertTrue(tis.isEOFRecord(new byte[RECORD_SIZE]));
        byte[] nonZero = new byte[RECORD_SIZE];
        nonZero[0] = 1;
        assertFalse(tis.isEOFRecord(nonZero));
    }

    @Test
    public void testReadRecord() throws Exception {
        byte[] data = new byte[RECORD_SIZE];
        data[0] = 1;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        byte[] record = tis.readRecord();
        assertNotNull(record);
        assertEquals(1, record[0]);
        record = tis.readRecord();
        assertNull(record);
    }

    @Test
    public void testSkipRecordPadding() throws Exception {
        byte[] data = createData(100);
        byte[] tar = createTarWithSingleEntry("f", 100, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        tis.skip(100);
        tis.getNextTarEntry();
    }

    @Test
    public void testTryToConsumeSecondEOFRecordWithMark() throws Exception {
        byte[] tar = new byte[RECORD_SIZE * 3];
        ByteArrayInputStream bais = new ByteArrayInputStream(tar);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
    }

    @Test
    public void testConsumeRemainderOfLastBlock() throws Exception {
        byte[] data = createData(100);
        byte[] tar = createTarWithSingleEntry("f", 100, data);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        tis.getNextTarEntry();
        tis.read(new byte[100], 0, 100);
        tis.getNextTarEntry();
    }

    @Test
    public void testGetLongNameDataTrailingNulls() throws Exception {
        String name = "test\0\0";
        byte[] nameBytes = name.getBytes("UTF-8");
        byte[] longNameHeader = createHeader("././@LongLink", nameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(longNameHeader);
        bos.write(nameBytes);
        int padding = (int) (RECORD_SIZE - (nameBytes.length % RECORD_SIZE)) % RECORD_SIZE;
        if (padding > 0) bos.write(new byte[padding]);
        bos.write(createHeader("short", 0, TarConstants.LF_NORMAL));
        bos.write(new byte[RECORD_SIZE]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertEquals("test", entry.getName());
    }

    @Test
    public void testParsePaxHeaders() throws Exception {
        String paxData = "10 path=a\n15 size=100\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tis.parsePaxHeaders(bais);
        assertEquals("a", headers.get("path"));
        assertEquals("100", headers.get("size"));
    }

    @Test
    public void testApplyPaxHeadersToCurrentEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("original");
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tis.setCurrentEntry(entry);
        Map<String, String> headers = new HashMap<>();
        headers.put("path", "newpath");
        headers.put("linkpath", "newlink");
        headers.put("gid", "1000");
        headers.put("gname", "group");
        headers.put("uid", "500");
        headers.put("uname", "user");
        headers.put("size", "200");
        headers.put("mtime", "1234567890.123");
        headers.put("SCHILY.devminor", "1");
        headers.put("SCHILY.devmajor", "2");
        tis.applyPaxHeadersToCurrentEntry(headers);
        assertEquals("newpath", entry.getName());
        assertEquals("newlink", entry.getLinkName());
        assertEquals(1000, entry.getGroupId());
        assertEquals("group", entry.getGroupName());
        assertEquals(500, entry.getUserId());
        assertEquals("user", entry.getUserName());
        assertEquals(200, entry.getSize());
        assertEquals(1234567890123L, entry.getModTime().getTime());
        assertEquals(1, entry.getDevMinor());
        assertEquals(2, entry.getDevMajor());
    }

    @Test
    public void testReadGNUSparseExtended() throws Exception {
        byte[] header = createHeader("sparse", 100, TarConstants.LF_GNUTYPE_SPARSE);
        header[482] = 1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(new byte[100]);
        bos.write(new byte[RECORD_SIZE - 100]);
        byte[] extHeader = createHeader("", 0, (byte) 'S');
        extHeader[482] = 0;
        bos.write(extHeader);
        bos.write(new byte[RECORD_SIZE]);
        bos.write(createEOFBlock());
        bos.write(createEOFBlock());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithEncoding() throws Exception {
        byte[] tar = createTarWithSingleEntry("file", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar), "UTF-8");
        assertNotNull(tis.getNextTarEntry());
    }

    @Test
    public void testConstructorWithBlockSize() throws Exception {
        byte[] tar = createTarWithSingleEntry("file", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar), 512);
        assertNotNull(tis.getNextTarEntry());
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() throws Exception {
        byte[] tar = createTarWithSingleEntry("file", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar), 512, "UTF-8");
        assertNotNull(tis.getNextTarEntry());
    }

    @Test
    public void testConstructorWithRecordSize() throws Exception {
        byte[] tar = createTarWithSingleEntry("file", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar), 512, 512);
        assertNotNull(tis.getNextTarEntry());
    }

    @Test
    public void testConstructorWithAll() throws Exception {
        byte[] tar = createTarWithSingleEntry("file", 0, new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tar), 512, 512, "UTF-8");
        assertNotNull(tis.getNextTarEntry());
    }
}
