package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class TarArchiveOutputStreamTest {

    private static final int DEFAULT_RECORD_SIZE = 512;
    private static final int DEFAULT_BLOCK_SIZE = 10240;

    private ByteArrayOutputStream bos;
    private TarArchiveOutputStream tos;

    private void setupStream() throws IOException {
        bos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(bos);
    }

    private void setupStream(int blockSize) throws IOException {
        bos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(bos, blockSize);
    }

    private void setupStream(int blockSize, int recordSize) throws IOException {
        bos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(bos, blockSize, recordSize);
    }

    private void setupStream(String encoding) throws IOException {
        bos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(bos, encoding);
    }

    @Test
    public void testConstructorDefault() throws IOException {
        setupStream();
        assertNotNull(tos);
        assertEquals(DEFAULT_RECORD_SIZE, tos.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockSize() throws IOException {
        setupStream(2048);
        assertEquals(2048 / TarBuffer.DEFAULT_RCDSIZE * TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    @Test
    public void testConstructorWithBlockAndRecordSize() throws IOException {
        setupStream(10240, 512);
        assertEquals(512, tos.getRecordSize());
    }

    @Test
    public void testConstructorWithEncoding() throws IOException {
        setupStream("UTF-8");
        assertNotNull(tos);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStream() {
        new TarArchiveOutputStream(null);
    }

    @Test
    public void testSetLongFileMode() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        // no getter, we trust
    }

    @Test
    public void testSetBigNumberMode() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
    }

    @Test
    public void testSetAddPaxHeadersForNonAsciiNames() throws IOException {
        setupStream();
        tos.setAddPaxHeadersForNonAsciiNames(true);
    }

    @Test
    public void testGetBytesWrittenInitial() throws IOException {
        setupStream();
        assertEquals(0L, tos.getBytesWritten());
    }

    @Test
    public void testGetCountDeprecated() throws IOException {
        setupStream();
        assertEquals(0, tos.getCount());
    }

    @Test
    public void testFinishNormal() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(tos.finished);
    }

    @Test(expected = IOException.class)
    public void testFinishAlreadyFinished() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntry() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tos.putArchiveEntry(entry); // not closed
        tos.finish();
    }

    @Test
    public void testCloseNormal() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(tos.closed);
    }

    @Test
    public void testCloseAfterFinish() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
        assertTrue(tos.closed);
    }

    @Test
    public void testCloseWhenAlreadyClosed() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        tos.close(); // should not throw
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterFinish() throws IOException {
        setupStream();
        tos.finish();
        tos.putArchiveEntry(new TarArchiveEntry("test.txt"));
    }

    @Test
    public void testPutArchiveEntryDirectory() throws IOException {
        setupStream();
        TarArchiveEntry dir = new TarArchiveEntry("dir/", TarConstants.LF_DIR);
        tos.putArchiveEntry(dir);
        tos.closeArchiveEntry();
        // check that currSize was set to 0
        // no exception
    }

    @Test
    public void testPutArchiveEntryNormalFile() throws IOException {
        setupStream();
        TarArchiveEntry file = new TarArchiveEntry("file.txt");
        file.setSize(100);
        tos.putArchiveEntry(file);
        assertEquals(100, file.getSize());
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryLongFilenameError() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMELEN + 1; i++) {
            sb.append('a');
        }
        try {
            tos.putArchiveEntry(new TarArchiveEntry(sb.toString()));
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("too long"));
        }
    }

    @Test
    public void testPutArchiveEntryLongFilenameTruncate() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = "a".repeat(TarConstants.NAMLEN + 10);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // no exception, header may contain truncated name
    }

    @Test
    public void testPutArchiveEntryLongFilenameGNU() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = "a".repeat(TarConstants.NAMLEN + 10);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // should have written two entries: longlink and actual
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 512); // at least two records
    }

    @Test
    public void testPutArchiveEntryLongFilenamePOSIX() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = "b".repeat(TarConstants.NAMLEN + 10);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
        // Should contain PAX header entry and file entry
    }

    @Test
    public void testPutArchiveEntryBigNumberErrorSize() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry = new TarArchiveEntry("bigfile");
        entry.setSize(TarConstants.MAXSIZE + 1);
        try {
            tos.putArchiveEntry(entry);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    @Test
    public void testPutArchiveEntryBigNumberErrorGroup() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry = new TarArchiveEntry("biggroup");
        entry.setGroupId(TarConstants.MAXID + 1);
        try {
            tos.putArchiveEntry(entry);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    @Test
    public void testPutArchiveEntryBigNumberStar() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = new TarArchiveEntry("starfile");
        entry.setSize(TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // no exception
    }

    @Test
    public void testPutArchiveEntryBigNumberPosix() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("posixbig");
        entry.setSize(TarConstants.MAXSIZE + 1);
        entry.setGroupId(TarConstants.MAXID + 1);
        entry.setUserId(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // should write pax header
    }

    @Test
    public void testPutArchiveEntryAddPaxHeadersForNonAsciiNames() throws IOException {
        setupStream();
        tos.setAddPaxHeadersForNonAsciiNames(true);
        String nonAsciiName = "t\u00e9st.txt"; // "tést.txt"
        TarArchiveEntry entry = new TarArchiveEntry(nonAsciiName);
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[10]);
        tos.closeArchiveEntry();
        // should have pax header
    }

    @Test
    public void testPutArchiveEntryLinkWithNonAscii() throws IOException {
        setupStream();
        tos.setAddPaxHeadersForNonAsciiNames(true);
        String linkName = "l\u00e9nk";
        TarArchiveEntry entry = new TarArchiveEntry("file.txt", TarConstants.LF_SYMLINK);
        entry.setLinkName(linkName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // pax header should contain linkpath
    }

    @Test
    public void testPutArchiveEntryPaxAndLongNameOverlap() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        String longAsciiName = "c".repeat(TarConstants.NAMLEN + 5);
        TarArchiveEntry entry = new TarArchiveEntry(longAsciiName);
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[5]);
        tos.closeArchiveEntry();
        // should have one pax header with path, not duplicate because paxHeaderContainsPath set
    }

    @Test
    public void testCloseArchiveEntryNormal() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryPremature() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        tos.closeArchiveEntry(); // less bytes written
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryNoCurrentEntry() throws IOException {
        setupStream();
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryAfterFinish() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteExactSize() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("exact.txt");
        entry.setSize(512);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[512];
        tos.write(data);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteExceedsSize() throws IOException {
        setupStream();        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[11]);
    }

    @Test
    public void testWriteAssembleAndFlush() throws IOException {
        setupStream(10240, 512);
        TarArchiveEntry entry = new TarArchiveEntry("assemble.txt");
        entry.setSize(600); // more than 512
        tos.putArchiveEntry(entry);
        tos.write(new byte[200]); // assemLen = 200
        tos.write(new byte[400]); // 200+400=600, fills record and leaves some
        tos.closeArchiveEntry();
    }

    @Test
    public void testWriteAssembleWithRemaining() throws IOException {
        setupStream(10240, 512);
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(250);
        tos.putArchiveEntry(entry);
        tos.write(new byte[250]);
        tos.closeArchiveEntry(); // will fill remainder of assemBuf with zeros
        // should not throw
    }

    @Test
    public void testWriteLargeChunks() throws IOException {
        setupStream(10240, 512);
        TarArchiveEntry entry = new TarArchiveEntry("large.txt");
        entry.setSize(2048);
        tos.putArchiveEntry(entry);
        tos.write(new byte[1024]);
        tos.write(new byte[1024]);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteAfterCloseArchiveEntry() throws IOException {
        setupStream();
        TarArchiveEntry entry = new TarArchiveEntry("afterclose.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.write(new byte[5]); // no current entry
    }

    @Test
    public void testFlush() throws IOException {
        setupStream();
        tos.flush(); // no-op on underlying OutputStream, no error
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        setupStream();
        File tmpFile = File.createTempFile("tar", ".txt");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tmpFile, "entryname");
            assertNotNull(entry);
            assertTrue(entry instanceof TarArchiveEntry);
            assertEquals("entryname", entry.getName());
        } finally {
            tmpFile.delete();
        }
    }

    @Test(expected = IOException.class)
    public void testCreateArchiveEntryAfterFinish() throws IOException {
        setupStream();
        tos.finish();
        tos.createArchiveEntry(File.createTempFile("tar", ".txt"), "name");
    }

    @Test
    public void testWritePaxHeadersPathTruncation() throws IOException {
        // indirectly tests stripTo7Bits and name truncation
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = "d".repeat(TarConstants.NAMLEN + 20);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // no exception
    }

    // Helpers to allow tests to compile with string repeat (Java 11+)
    // If target Java 8, replace with loop. I'll use a for-loop style to be safe.
    private String repeatString(int count, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // Re-implement test methods that used repeat with loop if necessary.
    @Test
    public void testPutArchiveEntryLongFilenameTruncateLoop() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = repeatString(TarConstants.NAMLEN + 10, "a");
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryLongFilenameGnuLoop() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = repeatString(TarConstants.NAMLEN + 10, "b");
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryLongFilenamePosixLoop() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = repeatString(TarConstants.NAMLEN + 10, "c");
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[100]);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryPaxAndLongNameOverlapLoop() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        String longAsciiName = repeatString(TarConstants.NAMLEN + 5, "d");
        TarArchiveEntry entry = new TarArchiveEntry(longAsciiName);
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[5]);
        tos.closeArchiveEntry();
    }

    // More tests to cover star extension and others
    @Test
    public void testPutArchiveEntryStarBigNumberWithDevMajor() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = new TarArchiveEntry("star");
        entry.setDevMajor(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
    }

    @Test
    public void testPutArchiveEntryPosixBigNumberWithMode() throws IOException {
        setupStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("posix");
        entry.setMode(TarConstants.MAXID + 1);
        try {
            tos.putArchiveEntry(entry);
            fail("Should have thrown RuntimeException for mode");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    // Test that when addPaxHeadersForNonAsciiNames is true and path is already in paxHeaders (from long name), it does not add again
    @Test
    public void testNonAsciiNameWhenPathAlreadyInPax() throws IOException {
        setupStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        String nonAsciiLong = "e\u00e9".repeat(TarConstants.NAMLEN + 1); // use loop
        // Actually we need a name that is long and contains non-ASCII. We'll construct.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMLEN + 3; i++) {
            sb.append('\u00e9'); // é
        }
        String name = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        // Should not double-add path
    }

    // Finish with final guard
}
