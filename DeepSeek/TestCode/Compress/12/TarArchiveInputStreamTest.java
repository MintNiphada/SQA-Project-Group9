package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    // Helper to create a tar archive in memory using TarArchiveOutputStream
    private byte[] createTarWithEntries(TarArchiveEntry entry, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.putArchiveEntry(entry);
        if (data != null) {
            tos.write(data);
        }
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    private byte[] createTarWithMultipleEntries(TarArchiveEntry[] entries, byte[][] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        for (int i = 0; i < entries.length; i++) {
            tos.putArchiveEntry(entries[i]);
            if (data[i] != null) {
                tos.write(data[i]);
            }
            tos.closeArchiveEntry();
        }
        tos.close();
        return bos.toByteArray();
    }

    // Helper to compute tar header checksum (for manual header construction)
    private long computeTarChecksum(byte[] header) {
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= 148 && i < 156) {
                sum += 32; // treat checksum field as spaces
            } else {
                sum += header[i] & 0xff;
            }
        }
        return sum;
    }

    private void setTarChecksum(byte[] header) {
        long sum = computeTarChecksum(header);
        String chksum = String.format("%06o\0 ", sum);
        byte[] chkBytes = chksum.getBytes();
        System.arraycopy(chkBytes, 0, header, 148, 8);
    }

    // Create a minimal tar header with given name, size, typeflag
    private byte[] createMinimalHeader(String name, long size, byte typeflag) {
        byte[] header = new byte[512];
        // name (100 bytes)
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
        // mode (8 bytes) - default 0644
        System.arraycopy("0000644\0".getBytes(), 0, header, 100, 8);
        // uid (8 bytes) - 0
        System.arraycopy("0000000\0".getBytes(), 0, header, 108, 8);
        // gid (8 bytes) - 0
        System.arraycopy("0000000\0".getBytes(), 0, header, 116, 8);
        // size (12 bytes)
        String sizeStr = String.format("%011o", size);
        System.arraycopy(sizeStr.getBytes(), 0, header, 124, 11);
        header[135] = ' ';
        // mtime (12 bytes) - 0
        System.arraycopy("00000000000\0".getBytes(), 0, header, 136, 12);
        // chksum (8 bytes) - placeholder
        System.arraycopy("        ".getBytes(), 0, header, 148, 8);
        // typeflag
        header[156] = typeflag;
        // linkname (100 bytes) - empty
        // magic (6 bytes) - "ustar\0"
        System.arraycopy("ustar\0".getBytes(), 0, header, 257, 6);
        // version (2 bytes) - "00"
        System.arraycopy("00".getBytes(), 0, header, 263, 2);
        // uname (32 bytes) - empty
        // gname (32 bytes) - empty
        // devmajor (8 bytes) - 0
        // devminor (8 bytes) - 0
        // prefix (155 bytes) - empty
        setTarChecksum(header);
        return header;
    }

    // Create a sparse entry header with extended flag
    private byte[] createSparseHeader(boolean extended) {
        byte[] header = createMinimalHeader("sparsefile", 0, TarConstants.LF_SPARSE);
        // Set isextended byte at offset 504 (GNU sparse format)
        header[504] = (byte) (extended ? 1 : 0);
        setTarChecksum(header);
        return header;
    }

    // Create a tar with a sparse entry and optional extended sparse entries
    private byte[] createSparseTar(boolean withExtended) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // First sparse header
        byte[] header1 = createSparseHeader(withExtended);
        bos.write(header1);
        // Pad to record size (512)
        // No data blocks for sparse header (size 0)
        if (withExtended) {
            // Add an extended sparse header (not extended)
            byte[] header2 = createSparseHeader(false);
            bos.write(header2);
        }
        // End of archive: two null records (EOF)
        bos.write(new byte[1024]);
        return bos.toByteArray();
    }

    @Test
    public void testMatchesPosix() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar\0".getBytes(), 0, signature, 257, 6);
        System.arraycopy("00".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesGnuSpace() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar  ".getBytes(), 0, signature, 257, 6); // MAGIC_GNU
        System.arraycopy(" \0".getBytes(), 0, signature, 263, 2); // VERSION_GNU_SPACE
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesGnuZero() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar  ".getBytes(), 0, signature, 257, 6);
        System.arraycopy("0\0".getBytes(), 0, signature, 263, 2); // VERSION_GNU_ZERO
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesAnt() {
        byte[] signature = new byte[512];
        System.arraycopy("ustar\0".getBytes(), 0, signature, 257, 6); // MAGIC_ANT? Actually MAGIC_ANT is "ustar\0" but with version " \0"? Wait, check constants.
        // According to code: MAGIC_ANT is "ustar\0" (same as POSIX) and VERSION_ANT is " \0"? Actually TarConstants.MAGIC_ANT = "ustar\0" and VERSION_ANT = " \0".
        System.arraycopy("ustar\0".getBytes(), 0, signature, 257, 6);
        System.arraycopy(" \0".getBytes(), 0, signature, 263, 2);
        assertTrue(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testMatchesInvalidLength() {
        byte[] signature = new byte[10];
        assertFalse(TarArchiveInputStream.matches(signature, 10));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] signature = new byte[512];
        // all zeros
        assertFalse(TarArchiveInputStream.matches(signature, 512));
    }

    @Test
    public void testConstructorDefault() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tais.getRecordSize());
    }

    @Test
    public void testConstructorCustomBlockSize() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(is, 1024);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tais.getRecordSize());
    }

    @Test
    public void testConstructorCustomBlockAndRecordSize() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(is, 1024, 512);
        assertEquals(512, tais.getRecordSize());
    }

    @Test
    public void testClose() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        tais.close();
        // After close, reading should throw IOException because underlying stream is closed
        try {
            tais.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testReset() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        tais.reset(); // no-op, should not throw
    }

    @Test
    public void testAvailableNormal() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "Hello".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        assertEquals(data.length, tais.available());
        tais.read();
        assertEquals(data.length - 1, tais.available());
    }

    @Test
    public void testAvailableHugeEntry() throws Exception {
        // Create an entry with size > Integer.MAX_VALUE
        TarArchiveEntry entry = new TarArchiveEntry("huge");
        entry.setSize(Long.MAX_VALUE);
        byte[] tarBytes = createTarWithEntries(entry, null); // no data, but size is huge
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        assertEquals(Integer.MAX_VALUE, tais.available());
    }

    @Test
    public void testSkip() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "HelloWorld".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        long skipped = tais.skip(5);
        assertEquals(5, skipped);
        byte[] buf = new byte[5];
        int read = tais.read(buf);
        assertEquals(5, read);
        assertArrayEquals("World".getBytes(), buf);
    }

    @Test
    public void testSkipBeyondEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "Hello".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        long skipped = tais.skip(10);
        assertEquals(data.length, skipped);
        assertEquals(-1, tais.read());
    }

    @Test
    public void testReadNormal() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "Hello".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        byte[] buf = new byte[5];
        int read = tais.read(buf);
        assertEquals(5, read);
        assertArrayEquals(data, buf);
        assertEquals(-1, tais.read());
    }

    @Test
    public void testReadWithOffset() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "Hello".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        byte[] buf = new byte[10];
        int read = tais.read(buf, 2, 5);
        assertEquals(5, read);
        assertEquals('H', buf[2]);
        assertEquals('e', buf[3]);
    }

    @Test
    public void testReadPartialRecordLeftover() throws Exception {
        // Create an entry with data larger than one record (512 bytes) to test readBuf
        byte[] data = new byte[600];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        // Read 100 bytes first
        byte[] buf1 = new byte[100];
        int read1 = tais.read(buf1);
        assertEquals(100, read1);
        // Read another 500 bytes (should use readBuf for the remainder of the first record)
        byte[] buf2 = new byte[500];
        int read2 = tais.read(buf2);
        assertEquals(500, read2);
        // Verify data
        for (int i = 0; i < 100; i++) assertEquals(data[i], buf1[i]);
        for (int i = 0; i < 500; i++) assertEquals(data[100 + i], buf2[i]);
        // Should be at end
        assertEquals(-1, tais.read());
    }

    @Test
    public void testReadTruncatedEntry() throws Exception {
        // Entry size > 0 but no data blocks (truncated)
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        // Create tar with only header and no data blocks (just EOF records)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(entry);
        // Do not write data, just close entry (which will pad? Actually closeArchiveEntry will write zero data and pad to record boundary)
        tos.closeArchiveEntry();
        tos.close();
        byte[] tarBytes = bos.toByteArray();
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        // getNextEntry should throw RuntimeException because skip fails
        try {
            tais.getNextEntry();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testGetNextTarEntryNormal() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "data".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertEquals("file.txt", next.getName());
        assertEquals(data.length, next.getSize());
        // Read data
        byte[] buf = new byte[data.length];
        tais.read(buf);
        assertArrayEquals(data, buf);
        // Next entry should be null
        assertNull(tais.getNextTarEntry());
    }

    @Test
    public void testGetNextTarEntryDirectory() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        byte[] tarBytes = createTarWithEntries(entry, null);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertTrue(next.isDirectory());
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read());
    }

    @Test
    public void testGetNextTarEntryLongName() throws Exception {
        String longName = "this/is/a/very/long/path/that/exceeds/100/characters/and/needs/gnu/long/name/extension/for/tar/archives/with/deep/directories/file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] data = "content".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertEquals(longName, next.getName());
        assertEquals(data.length, next.getSize());
    }

    @Test
    public void testGetNextTarEntryLongNameMalformed() throws Exception {
        // Only a long name entry without following entry (Bugzilla 40334)
        String longName = "a".repeat(101);
        TarArchiveEntry longEntry = new TarArchiveEntry(longName);
        longEntry.setSize(0);
        // Create tar with only the long name entry (type 'L') and no subsequent entry
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.putArchiveEntry(longEntry);
        tos.closeArchiveEntry();
        // Do not add the actual file entry
        tos.close();
        byte[] tarBytes = bos.toByteArray();
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        // First getNextTarEntry should process long name and then return null because next entry is missing
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNull(next);
    }

    @Test
    public void testGetNextTarEntryPaxHeaders() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        entry.addPaxHeader("path", "renamed.txt");
        entry.addPaxHeader("size", "200");
        entry.addPaxHeader("gid", "123");
        entry.addPaxHeader("gname", "group");
        entry.addPaxHeader("uid", "456");
        entry.addPaxHeader("uname", "user");
        entry.addPaxHeader("linkpath", "link");
        byte[] data = new byte[100];
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertEquals("renamed.txt", next.getName());
        assertEquals(200, next.getSize());
        assertEquals(123, next.getGroupId());
        assertEquals("group", next.getGroupName());
        assertEquals(456, next.getUserId());
        assertEquals("user", next.getUserName());
        assertEquals("link", next.getLinkName());
    }

    @Test
    public void testGetNextTarEntrySparse() throws Exception {
        // Create a sparse tar with no extended entries
        byte[] tarBytes = createSparseTar(false);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertTrue(next.isGNUSparse());
        assertFalse(next.isExtended());
    }

    @Test
    public void testGetNextTarEntrySparseExtended() throws Exception {
        byte[] tarBytes = createSparseTar(true);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry next = tais.getNextTarEntry();
        assertNotNull(next);
        assertTrue(next.isGNUSparse());
        // After processing extended sparse entries, the entry should still be present
        // The extended flag is handled internally; we just verify no exception
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveEntry normal = new TarArchiveEntry("file.txt");
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tais.canReadEntryData(normal));
        TarArchiveEntry sparse = new TarArchiveEntry("sparse");
        // Make it sparse by setting typeflag? Actually isGNUSparse checks typeflag 'S'
        // We can use reflection to set typeflag, or create a sparse entry via header.
        // Simpler: create a TarArchiveEntry from a sparse header.
        byte[] sparseHeader = createSparseHeader(false);
        TarArchiveEntry sparseEntry = new TarArchiveEntry(sparseHeader);
        assertFalse(tais.canReadEntryData(sparseEntry));
    }

    @Test
    public void testGetCurrentEntrySetCurrentEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "data".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        assertNull(tais.getCurrentEntry());
        tais.getNextEntry();
        assertNotNull(tais.getCurrentEntry());
        TarArchiveEntry newEntry = new TarArchiveEntry("other");
        tais.setCurrentEntry(newEntry);
        assertEquals(newEntry, tais.getCurrentEntry());
    }

    @Test
    public void testIsAtEOFSetAtEOF() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());
    }

    @Test
    public void testParsePaxHeadersNormal() throws Exception {
        String paxData = "10 path=file\n15 gid=1000\n";
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeaders(reader);
        assertEquals(2, headers.size());
        assertEquals("file", headers.get("path"));
        assertEquals("1000", headers.get("gid"));
    }

    @Test
    public void testParsePaxHeadersMalformedLength() throws Exception {
        String paxData = "abc path=file\n";
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        // Should throw IOException because length parsing fails (non-digit)
        try {
            tais.parsePaxHeaders(reader);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testParsePaxHeadersMissingNewline() throws Exception {
        String paxData = "10 path=file"; // no trailing newline
        Reader reader = new StringReader(paxData);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeaders(reader);
        // Should parse the entry but then EOF
        assertEquals(1, headers.size());
        assertEquals("file", headers.get("path"));
    }

    @Test
    public void testParsePaxHeadersEmpty() throws Exception {
        Reader reader = new StringReader("");
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeaders(reader);
        assertTrue(headers.isEmpty());
    }

    @Test
    public void testApplyPaxHeadersToCurrentEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("original");
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.setCurrentEntry(entry);
        Map<String, String> headers = new HashMap<>();
        headers.put("path", "renamed");
        headers.put("size", "500");
        headers.put("gid", "100");
        headers.put("gname", "group");
        headers.put("uid", "200");
        headers.put("uname", "user");
        headers.put("linkpath", "link");
        headers.put("unknown", "ignored");
        tais.applyPaxHeadersToCurrentEntry(headers);
        assertEquals("renamed", entry.getName());
        assertEquals(500, entry.getSize());
        assertEquals(100, entry.getGroupId());
        assertEquals("group", entry.getGroupName());
        assertEquals(200, entry.getUserId());
        assertEquals("user", entry.getUserName());
        assertEquals("link", entry.getLinkName());
    }

    @Test
    public void testReadGNUSparseNoExtended() throws Exception {
        // Create a sparse entry without extended flag, readGNUSparse should do nothing
        byte[] tarBytes = createSparseTar(false);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextTarEntry(); // this calls readGNUSparse internally
        // No exception, entry still valid
        assertNotNull(tais.getCurrentEntry());
    }

    @Test
    public void testReadGNUSparseWithExtended() throws Exception {
        byte[] tarBytes = createSparseTar(true);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextTarEntry(); // should process extended sparse entries
        assertNotNull(tais.getCurrentEntry());
    }

    @Test
    public void testGetNextEntryReturnsTarEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "data".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        ArchiveEntry ae = tais.getNextEntry();
        assertTrue(ae instanceof TarArchiveEntry);
        assertEquals("file.txt", ae.getName());
    }

    @Test
    public void testReadAfterEOF() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "data".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        byte[] buf = new byte[10];
        int read = tais.read(buf);
        assertEquals(data.length, read);
        assertEquals(-1, tais.read(buf));
    }

    @Test
    public void testReadWithNumToReadLargerThanRemaining() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] data = "Hello".getBytes();
        entry.setSize(data.length);
        byte[] tarBytes = createTarWithEntries(entry, data);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        byte[] buf = new byte[10];
        int read = tais.read(buf, 0, 10);
        assertEquals(5, read);
        assertArrayEquals(data, buf);
    }

    @Test
    public void testReadUnexpectedEOF() throws Exception {
        // Create a tar with an entry that has size > 0 but the data is truncated (no data blocks)
        // We'll manually construct a tar with a header and then EOF records immediately.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);
        bos.write(header);
        // No data blocks, just two null records (EOF)
        bos.write(new byte[1024]);
        byte[] tarBytes = bos.toByteArray();
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        // getNextEntry will skip remaining data (which is 0 because entryOffset=0, entrySize=100, but skip will try to read and fail)
        // Actually getNextEntry will call skip(100) which will try to read and get -1, causing RuntimeException.
        try {
            tais.getNextEntry();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testReadWhenEntryOffsetEqualsEntrySize() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(0);
        byte[] tarBytes = createTarWithEntries(entry, null);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        tais.getNextEntry();
        assertEquals(-1, tais.read());
    }

    @Test
    public void testMultipleEntries() throws Exception {
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        byte[] data1 = "first".getBytes();
        entry1.setSize(data1.length);
        TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        byte[] data2 = "second".getBytes();
        entry2.setSize(data2.length);
        byte[] tarBytes = createTarWithMultipleEntries(new TarArchiveEntry[]{entry1, entry2}, new byte[][]{data1, data2});
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry e1 = tais.getNextTarEntry();
        assertEquals("file1.txt", e1.getName());
        byte[] buf = new byte[5];
        tais.read(buf);
        assertArrayEquals(data1, buf);
        TarArchiveEntry e2 = tais.getNextTarEntry();
        assertEquals("file2.txt", e2.getName());
        buf = new byte[6];
        tais.read(buf);
        assertArrayEquals(data2, buf);
        assertNull(tais.getNextTarEntry());
    }

    @Test
    public void testGetRecordSize() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]), 1024, 512);
        assertEquals(512, tais.getRecordSize());
    }
}
