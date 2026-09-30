package org.apache.commons.compress.archivers.ar;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArArchiveInputStreamTest {

    // Helper to create a valid AR global header
    private static final byte[] AR_HEADER = "!<arch>\n".getBytes();
    // Trailer bytes after each entry header
    private static final byte[] AR_TRAILER = "`\n".getBytes();

    // Helper to build a minimal AR archive with one entry (no file data)
    private byte[] buildMinimalArchive(String name, long length) {
        // Format: global header, then entry header fields + trailer
        // Fields: name(16), modtime(12), uid(6), gid(6), mode(8), size(10)
        // All fields are ASCII, padded with spaces.
        String nameField = String.format("%-16s", name);
        String modtime = String.format("%-12d", 0);
        String uid = String.format("%-6d", 0);
        String gid = String.format("%-6d", 0);
        String mode = String.format("%-8d", 0);
        String size = String.format("%-10d", length);

        byte[] entryHeader = (nameField + modtime + uid + gid + mode + size).getBytes();
        // entryHeader length must be 58
        assertEquals(58, entryHeader.length);

        byte[] archive = new byte[AR_HEADER.length + entryHeader.length + AR_TRAILER.length];
        System.arraycopy(AR_HEADER, 0, archive, 0, AR_HEADER.length);
        System.arraycopy(entryHeader, 0, archive, AR_HEADER.length, entryHeader.length);
        System.arraycopy(AR_TRAILER, 0, archive, AR_HEADER.length + entryHeader.length, AR_TRAILER.length);
        return archive;
    }

    // Helper to build archive with file data after entry
    private byte[] buildArchiveWithData(String name, byte[] data) {
        byte[] minimal = buildMinimalArchive(name, data.length);
        byte[] full = new byte[minimal.length + data.length];
        System.arraycopy(minimal, 0, full, 0, minimal.length);
        System.arraycopy(data, 0, full, minimal.length, data.length);
        return full;
    }

    @Test
    public void testMatchesValidSignature() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a };
        assertTrue(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesLengthLessThan8() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 7));
    }

    @Test
    public void testMatchesInvalidFirstByte() {
        byte[] sig = new byte[] { 0x00, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidSecondByte() {
        byte[] sig = new byte[] { 0x21, 0x00, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidThirdByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x00, 0x72, 0x63, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidFourthByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x00, 0x63, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidFifthByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x00, 0x68, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidSixthByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x63, 0x00, 0x3e, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidSeventhByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x00, 0x0a };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testMatchesInvalidEighthByte() {
        byte[] sig = new byte[] { 0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x00 };
        assertFalse(ArArchiveInputStream.matches(sig, 8));
    }

    @Test
    public void testConstructor() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream ais = new ArArchiveInputStream(in);
        assertNotNull(ais);
    }

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] data = new byte[] { 10, 20, 30 };
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ArArchiveInputStream ais = new ArArchiveInputStream(bin);
        assertEquals(10, ais.read());
        assertEquals(20, ais.read());
        assertEquals(30, ais.read());
        assertEquals(-1, ais.read());
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ArArchiveInputStream ais = new ArArchiveInputStream(bin);
        byte[] buf = new byte[3];
        int read = ais.read(buf);
        assertEquals(3, read);
        assertArrayEquals(new byte[] { 1, 2, 3 }, buf);
        read = ais.read(buf);
        assertEquals(2, read);
        assertEquals(4, buf[0]);
        assertEquals(5, buf[1]);
    }

    @Test
    public void testReadByteArrayWithOffset() throws IOException {
        byte[] data = new byte[] { 10, 20, 30, 40 };
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ArArchiveInputStream ais = new ArArchiveInputStream(bin);
        byte[] buf = new byte[5];
        int read = ais.read(buf, 1, 3);
        assertEquals(3, read);
        assertEquals(10, buf[1]);
        assertEquals(20, buf[2]);
        assertEquals(30, buf[3]);
        read = ais.read(buf, 0, 5);
        assertEquals(1, read);
        assertEquals(40, buf[0]);
    }

    @Test
    public void testReadReturnsMinusOneDoesNotIncrementOffset() throws IOException {
        // empty stream
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream ais = new ArArchiveInputStream(bin);
        assertEquals(-1, ais.read());
        // offset should remain 0; we can't directly check, but subsequent reads still -1
        assertEquals(-1, ais.read(new byte[1]));
    }

    @Test
    public void testClose() throws IOException {
        CloseTrackingInputStream ctis = new CloseTrackingInputStream(new ByteArrayInputStream(new byte[0]));
        ArArchiveInputStream ais = new ArArchiveInputStream(ctis);
        assertFalse(ctis.isClosed());
        ais.close();
        assertTrue(ctis.isClosed());
    }

    @Test
    public void testCloseTwice() throws IOException {
        CloseTrackingInputStream ctis = new CloseTrackingInputStream(new ByteArrayInputStream(new byte[0]));
        ArArchiveInputStream ais = new ArArchiveInputStream(ctis);
        ais.close();
        assertTrue(ctis.isClosed());
        ctis.resetClosed(); // reset flag to test second close
        ais.close();
        assertFalse(ctis.isClosed()); // should not call close again
    }

    @Test
    public void testGetNextArEntryValidNoFileData() throws IOException {
        byte[] archive = buildMinimalArchive("test.txt", 0);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(0, entry.getLength());
        // next call should return null because available() == 0
        assertNull(ais.getNextArEntry());
    }

    @Test
    public void testGetNextArEntryWithFileData() throws IOException {
        byte[] fileData = new byte[] { 1, 2, 3, 4 };
        byte[] archive = buildArchiveWithData("data.bin", fileData);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();
        assertNotNull(entry);
        assertEquals("data.bin", entry.getName());
        assertEquals(4, entry.getLength());
        // Now read the file data
        byte[] readData = new byte[4];
        int read = ais.read(readData);
        assertEquals(4, read);
        assertArrayEquals(fileData, readData);
        // After consuming file data, available() should be 0, next entry null
        assertNull(ais.getNextArEntry());
    }

    @Test
    public void testGetNextArEntryNameTrimmed() throws IOException {
        // name with trailing spaces
        byte[] archive = buildMinimalArchive("hello   ", 0);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();
        assertEquals("hello", entry.getName());
    }

    @Test
    public void testGetNextArEntryLengthParsedWithSpaces() throws IOException {
        // length field padded with spaces
        byte[] archive = buildMinimalArchive("f", 123);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();
        assertEquals(123, entry.getLength());
    }

    @Test(expected = IOException.class)
    public void testGetNextArEntryHeaderMismatch() throws IOException {
        byte[] badHeader = "XXXXXXX\n".getBytes();
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(badHeader));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class)
    public void testGetNextArEntryIncompleteHeader() throws IOException {
        byte[] incomplete = new byte[] { '!', '<', 'a' }; // less than 8 bytes
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(incomplete));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class)
    public void testGetNextArEntryTrailerMismatch() throws IOException {
        // correct global header, then entry fields, but wrong trailer
        byte[] header = AR_HEADER;
        String entryFields = String.format("%-16s%-12d%-6d%-6d%-8d%-10d", "file", 0, 0, 0, 0, 0);
        byte[] fields = entryFields.getBytes();
        byte[] badTrailer = "xx".getBytes();
        byte[] streamData = new byte[header.length + fields.length + badTrailer.length];
        System.arraycopy(header, 0, streamData, 0, header.length);
        System.arraycopy(fields, 0, streamData, header.length, fields.length);
        System.arraycopy(badTrailer, 0, streamData, header.length + fields.length, badTrailer.length);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(streamData));
        ais.getNextArEntry();
    }

    @Test
    public void testGetNextArEntryPaddingWhenOffsetOdd() throws IOException {
        // Create a stream: global header (8 bytes) + one extra byte (padding) + entry fields + trailer
        // We will read the header and then one extra byte to make offset odd before calling getNextArEntry.
        byte[] header = AR_HEADER;
        byte paddingByte = 0x00;
        String entryFields = String.format("%-16s%-12d%-6d%-6d%-8d%-10d", "padded", 0, 0, 0, 0, 0);
        byte[] fields = entryFields.getBytes();
        byte[] trailer = AR_TRAILER;
        byte[] streamData = new byte[header.length + 1 + fields.length + trailer.length];
        System.arraycopy(header, 0, streamData, 0, header.length);
        streamData[header.length] = paddingByte;
        System.arraycopy(fields, 0, streamData, header.length + 1, fields.length);
        System.arraycopy(trailer, 0, streamData, header.length + 1 + fields.length, trailer.length);

        ByteArrayInputStream bin = new ByteArrayInputStream(streamData);
        ArArchiveInputStream ais = new ArArchiveInputStream(bin);
        // Read the global header (8 bytes) to advance offset to 8
        byte[] buf = new byte[8];
        ais.read(buf);
        // Now read one more byte to make offset odd (9)
        int b = ais.read();
        assertEquals(paddingByte & 0xFF, b);
        // Now call getNextArEntry; it should detect odd offset and read one padding byte (the next byte? 
        // Actually, we already consumed the padding byte. The stream now points to the entry fields.
        // But the code checks offset % 2 != 0, and if so, reads one byte. Since offset is 9, it will read one byte,
        // which will be the first byte of the entry fields, corrupting the entry. That's not what we want.
        // We need to design the test so that the padding byte is still in the stream when getNextArEntry is called.
        // So we should NOT consume the padding byte before calling getNextArEntry. Instead, we should only read the header,
        // leaving the padding byte in the stream. Then offset will be 8 (even). To make offset odd, we need to read one byte
        // from somewhere else. But we can't read from the stream without consuming data. We need a different approach:
        // We can create a stream that has the global header, then an entry that starts at an odd offset? But AR entries must
        // start at even offset. The code handles the case where the previous entry's file data ended at an odd offset,
        // so a padding byte is inserted. So we can simulate that by having a previous entry with odd-length file data.
        // Let's create an archive with two entries: first entry with file data of odd length (e.g., 1 byte), so after reading
        // that entry and its data, offset becomes odd. Then the second entry should be read correctly after consuming the padding byte.
        // That's more realistic. We'll build an archive with two entries: first entry "f1" length 1, data byte 0x42; second entry "f2" length 0.
        // After reading first entry and its data, offset will be odd (header 8 + entry header 60 + data 1 = 69? Let's calculate:
        // global header: 8 bytes.
        // first entry: fields 58 + trailer 2 = 60 bytes, then file data 1 byte. Total after first entry = 8 + 60 + 1 = 69 (odd).
        // Then there should be a padding byte (0x0A? Actually AR format uses newline? The spec says each file data is followed by a newline? 
        // Not exactly; the padding is just to even boundary, any byte. We'll use 0x00). Then second entry fields + trailer.
        // So we'll build that and test that after reading first entry and its data, getNextArEntry returns the second entry correctly.
        // This tests the padding logic.
        byte[] firstData = new byte[] { 0x42 };
        byte[] archive = buildArchiveWithData("f1", firstData); // this gives global header + entry1 + data1 (length 1)
        // Now we need to append padding byte and second entry (length 0)
        byte[] secondEntry = buildMinimalArchive("f2", 0); // this includes global header again? No, buildMinimalArchive includes global header.
        // We need only the entry part without global header. Let's create a helper to build just the entry bytes (fields+trailer).
        byte[] entry2Bytes = buildEntryBytes("f2", 0);
        // padding byte
        byte pad = 0x00;
        byte[] fullArchive = new byte[archive.length + 1 + entry2Bytes.length];
        System.arraycopy(archive, 0, fullArchive, 0, archive.length);
        fullArchive[archive.length] = pad;
        System.arraycopy(entry2Bytes, 0, fullArchive, archive.length + 1, entry2Bytes.length);

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(fullArchive));
        // Read first entry
        ArArchiveEntry e1 = ais.getNextArEntry();
        assertEquals("f1", e1.getName());
        assertEquals(1, e1.getLength());
        // Read its data
        byte[] dataBuf = new byte[1];
        ais.read(dataBuf);
        assertEquals(0x42, dataBuf[0]);
        // Now offset should be odd (69). Next getNextArEntry should consume padding and read second entry.
        ArArchiveEntry e2 = ais.getNextArEntry();
        assertNotNull(e2);
        assertEquals("f2", e2.getName());
        assertEquals(0, e2.getLength());
        // After that, no more entries
        assertNull(ais.getNextArEntry());
    }

    // Helper to build entry bytes (fields + trailer) without global header
    private byte[] buildEntryBytes(String name, long length) {
        String nameField = String.format("%-16s", name);
        String modtime = String.format("%-12d", 0);
        String uid = String.format("%-6d", 0);
        String gid = String.format("%-6d", 0);
        String mode = String.format("%-8d", 0);
        String size = String.format("%-10d", length);
        byte[] fields = (nameField + modtime + uid + gid + mode + size).getBytes();
        byte[] entry = new byte[fields.length + AR_TRAILER.length];
        System.arraycopy(fields, 0, entry, 0, fields.length);
        System.arraycopy(AR_TRAILER, 0, entry, fields.length, AR_TRAILER.length);
        return entry;
    }

    @Test(expected = IOException.class)
    public void testGetNextArEntryEmptyStream() throws IOException {
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ais.getNextArEntry(); // should throw IOException because header read fails
    }

    @Test
    public void testGetNextEntryDelegatesToGetNextArEntry() throws IOException {
        byte[] archive = buildMinimalArchive("x", 0);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArchiveEntry entry = ais.getNextEntry();
        assertTrue(entry instanceof ArArchiveEntry);
        assertEquals("x", entry.getName());
    }

    // Custom InputStream to track close calls
    private static class CloseTrackingInputStream extends InputStream {
        private final InputStream delegate;
        private boolean closed = false;

        CloseTrackingInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }

        boolean isClosed() {
            return closed;
        }

        void resetClosed() {
            closed = false;
        }
    }
}
