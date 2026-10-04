package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

public class CpioArchiveInputStreamTest {

    private static final byte[] MAGIC_NEW_BYTES = "070701".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] MAGIC_NEW_CRC_BYTES = "070702".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] MAGIC_OLD_ASCII_BYTES = "070707".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

    @Test
    public void testMatchesLengthLessThan6() {
        final byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30};
        Assert.assertFalse(CpioArchiveInputStream.matches(signature, 5));
    }

    @Test
    public void testMatchesBinarySignatures() {
        final byte[] sig1 = new byte[]{0x71, (byte) 0xc7, 0, 0, 0, 0};
        Assert.assertTrue(CpioArchiveInputStream.matches(sig1, 6));
        final byte[] sig2 = new byte[]{(byte) 0xc7, 0x71, 0, 0, 0, 0};
        Assert.assertTrue(CpioArchiveInputStream.matches(sig2, 6));
    }

    @Test
    public void testMatchesAsciiNew() {
        final byte[] sig = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x31};
        Assert.assertTrue(CpioArchiveInputStream.matches(sig, 6));
        sig[5] = 0x32;
        Assert.assertTrue(CpioArchiveInputStream.matches(sig, 6));
        sig[5] = 0x37;
        Assert.assertTrue(CpioArchiveInputStream.matches(sig, 6));
    }

    @Test
    public void testMatchesAsciiInvalid() {
        final byte[] sig = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x30}; // last not 1,2,7
        Assert.assertFalse(CpioArchiveInputStream.matches(sig, 6));
        sig[1] = 0x36; // wrong second byte
        Assert.assertFalse(CpioArchiveInputStream.matches(sig, 6));
    }

    @Test(expected = IOException.class)
    public void testEnsureOpenThrowsAfterClose() throws IOException {
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.close();
        cis.available(); // ensureOpen will throw
    }

    @Test
    public void testAvailableReturns1Initially() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 0, "test");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        // entry not EOF yet
        Assert.assertEquals(1, cis.available());
    }

    @Test
    public void testAvailableReturns0WhenEntryEOF() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 0, "test");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        // read all data (size 0)
        cis.read(new byte[10], 0, 10); // will return -1, setting entryEOF
        Assert.assertEquals(0, cis.available());
    }

    @Test
    public void testClose() throws IOException {
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.close();
        Assert.assertTrue(cis.closed); // access private field? Can't. Use behavior: attempting anything throws.
    }

    @Test
    public void testReadAfterCloseThrows() throws IOException {
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.close();
        try {
            cis.read(new byte[1], 0, 1);
            Assert.fail("Expected IOException");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testSkipNegative() throws IOException {
        final InputStream in = new ByteArrayInputStream(new byte[0]);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        try {
            cis.skip(-1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSkipZero() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 10, "file");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        long skipped = cis.skip(0);
        Assert.assertEquals(0, skipped);
    }

    @Test
    public void testSkipWithinEntry() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 20, "file");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        long totalSkipped = 0;
        totalSkipped += cis.skip(5);
        totalSkipped += cis.skip(10);
        Assert.assertEquals(15, totalSkipped);
        // remaining 5 bytes
        final byte[] buf = new byte[5];
        final int read = cis.read(buf, 0, 5);
        Assert.assertEquals(5, read);
        // verify data from position 15..19 (we used sequential 0..19 data)
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals((byte) (15 + i), buf[i]);
        }
    }

    @Test
    public void testSkipPastEndOfEntry() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 10, "file");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        long skipped = cis.skip(15); // more than size
        Assert.assertEquals(10, skipped);
        Assert.assertEquals(0, cis.available()); // entryEOF should be true
    }

    @Test
    public void testReadInvalidOffsetLen() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 5, "f");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
    }

    @Test
    public void testReadInvalidOffsetLen() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 5, "f");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        try {
            cis.read(new byte[5], -1, 1);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
        try {
            cis.read(new byte[5], 0, -1);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
        try {
            cis.read(new byte[5], 3, 3); // 3+3 > 5
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testReadWithLenZero() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 5, "f");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
        final int read = cis.read(new byte[5], 0, 0);
        Assert.assertEquals(0, read);
    }

    @Test
    public void testReadAfterEntryEOF() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 5, "f");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        final CpioArchiveEntry first = cis.getNextEntry();
        // exhaust data
        final byte[] buf = new byte[5];
        int total = 0;
        int n;
        while ((n = cis.read(buf, 0, 5)) != -1) {
            total += n;
        }
        Assert.assertEquals(5, total);
        // subsequent read should return -1
        Assert.assertEquals(-1, cis.read(buf, 0, 1));
    }

    @Test
    public void testReadBeforeEntry() throws IOException {
        final byte[] entry = createNewFormatEntryBytes(false, 5, "f");
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        // no entry yet -> entry is null
        Assert.assertEquals(-1, cis.read(new byte[1], 0, 1));
    }

    @Test(expected = EOFException.class)
    public void testReadFullyPrematureEOF() throws IOException {
        final byte[] incompleteHeader = new byte[] {0x30}; // too short
        final InputStream in = new ByteArrayInputStream(incompleteHeader);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry(); // will attempt readFully, hits EOF
    }

    @Test
    public void testGetNextEntryNewFormat() throws IOException {
        final long fileSize = 100;
        final String name = "testfile";
        final byte[] entry = createNewFormatEntryBytes(false, fileSize, name);
        final byte[] trailer = createTrailerNewFormat(false);
        final byte[] combined = new byte[entry.length + trailer.length];
        System.arraycopy(entry, 0, combined, 0, entry.length);
        System.arraycopy(trailer, 0, combined, entry.length, trailer.length);

        final InputStream in = new ByteArrayInputStream(combined);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);

        final CpioArchiveEntry e = cis.getNextEntry();
        Assert.assertNotNull(e);
        Assert.assertEquals(name, e.getName());
        Assert.assertEquals(fileSize, e.getSize());
        Assert.assertEquals(CpioConstants.FORMAT_NEW, e.getFormat());

        // read data
        final byte[] data = new byte[(int) fileSize];
        int off = 0;
        while (off < data.length) {
            final int read = cis.read(data, off, data.length - off);
            if (read == -1) break;
            off += read;
        }
        Assert.assertEquals(fileSize, off);
        // data is sequential bytes
        for (int i = 0; i < fileSize; i++) {
            Assert.assertEquals((byte) i, data[i]);
        }

        // next entry should be trailer -> null
        Assert.assertNull(cis.getNextEntry());
    }

    @Test
    public void testGetNextEntryNewFormatCRC() throws IOException {
        final long fileSize = 50;
        final String name = "crcfile";
        final byte[] entry = createNewFormatEntryBytes(true, fileSize, name);
        final byte[] trailer = createTrailerNewFormat(true);
        final byte[] combined = new byte[entry.length + trailer.length];
        System.arraycopy(entry, 0, combined, 0, entry.length);
        System.arraycopy(trailer, 0, combined, entry.length, trailer.length);

        final InputStream in = new ByteArrayInputStream(combined);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);

        final CpioArchiveEntry e = cis.getNextEntry();
        Assert.assertNotNull(e);
        Assert.assertEquals(name, e.getName());
        Assert.assertEquals(fileSize, e.getSize());
        Assert.assertEquals(CpioConstants.FORMAT_NEW_CRC, e.getFormat());

        final byte[] data = new byte[(int) fileSize];
        int off = 0;
        while (off < data.length) {
            final int read = cis.read(data, off, data.length - off);
            if (read == -1) break;
            off += read;
        }
        Assert.assertEquals(fileSize, off);
        Assert.assertNull(cis.getNextEntry());
    }

    @Test(expected = IOException.class)
    public void testReadCRCError() throws IOException {
        // Build entry with CRC mismatch
        final long fileSize = 10;
        final String name = "crcfile";
        final byte[] entry = createNewFormatEntryBytesCRCWithBadChecksum(fileSize, name);
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        final CpioArchiveEntry e = cis.getNextEntry();
        Assert.assertNotNull(e);
        final byte[] buf = new byte[(int) fileSize];
        int off = 0;
        while (off < fileSize) {
            final int read = cis.read(buf, off, (int) fileSize - off);
            if (read == -1) break;
            off += read;
        }
        // The read should have thrown IOException on CRC check, but it's thrown after we finish reading.
        // Actually, the CRC check is done when entryBytesRead == size. The last read that reaches the end
        // will call skip(padding) and then check CRC. That read call would throw.
        // So we need to ensure the read triggers exception.
        // Already executed. Expected exception declared.
    }

    @Test
    public void testGetNextEntryOldAscii() throws IOException {
        final long fileSize = 30;
        final String name = "asciifile";
        final byte[] entry = createOldAsciiEntryBytes(fileSize, name, false);
        final byte[] trailer = createTrailerOldAscii();
        final byte[] combined = new byte[entry.length + trailer.length];
        System.arraycopy(entry, 0, combined, 0, entry.length);
        System.arraycopy(trailer, 0, combined, entry.length, trailer.length);

        final InputStream in = new ByteArrayInputStream(combined);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);

        final CpioArchiveEntry e = cis.getNextEntry();
        Assert.assertNotNull(e);
        Assert.assertEquals(name, e.getName());
        Assert.assertEquals(fileSize, e.getSize());
        Assert.assertEquals(CpioConstants.FORMAT_OLD_ASCII, e.getFormat());
        Assert.assertNull(cis.getNextEntry());
    }

    @Test(expected = IOException.class)
    public void testOldAsciiMode0NotTrailerThrows() throws IOException {
        final String name = "badmode";
        final byte[] entry = createOldAsciiEntryBytes(0, name, true); // mode=0, not trailer
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
    }

    @Test(expected = IOException.class)
    public void testNewFormatMode0NotTrailerThrows() throws IOException {
        final String name = "badmode";
        final byte[] entry = createNewFormatEntryBytes(false, 0, name, true); // mode=0, not trailer
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
    }

    @Test(expected = IOException.class)
    public void testOldBinaryMode0NotTrailerThrows() throws IOException {
        final String name = "badmode";
        final byte[] entry = createOldBinaryEntryBytes(0, name, false, true); // mode=0, not trailer
        final InputStream in = new ByteArrayInputStream(entry);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
    }

    @Test
    public void testGetNextEntryOldBinary() throws IOException {
        final boolean[] swapOptions = new boolean[]{false, true};
        for (final boolean swap : swapOptions) {
            final long fileSize = 40;
            final String name = "binaryfile";
            final byte[] entry = createOldBinaryEntryBytes(fileSize, name, swap, false);
            final byte[] trailer = createTrailerOldBinary(swap);
            final byte[] combined = new byte[entry.length + trailer.length];
            System.arraycopy(entry, 0, combined, 0, entry.length);
            System.arraycopy(trailer, 0, combined, entry.length, trailer.length);

            final InputStream in = new ByteArrayInputStream(combined);
            final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
            final CpioArchiveEntry e = cis.getNextEntry();
            Assert.assertNotNull(e);
            Assert.assertEquals(name, e.getName());
            Assert.assertEquals(fileSize, e.getSize());
            Assert.assertEquals(CpioConstants.FORMAT_OLD_BINARY, e.getFormat());
            Assert.assertNull(cis.getNextEntry());
        }
    }

    @Test
    public void testGetNextEntryTrailerOnly() throws IOException {
        final byte[] trailer = createTrailerNewFormat(false);
        final InputStream in = new ByteArrayInputStream(trailer);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        Assert.assertNull(cis.getNextEntry());
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryUnknownMagic() throws IOException {
        final byte[] data = new byte[]{0, 0, 0, 0, 0, 0}; // invalid
        final InputStream in = new ByteArrayInputStream(data);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        cis.getNextEntry();
    }

    @Test
    public void testNullEntryNamePaddingRead() throws IOException {
        // Simulate an entry with name length 1 (just null byte) -> name becomes empty string
        final byte[] entry = createMinimalNewEntry();
        final byte[] trailer = createTrailerNewFormat(false);
        final byte[] combined = new byte[entry.length + trailer.length];
        System.arraycopy(entry, 0, combined, 0, entry.length);
        System.arraycopy(trailer, 0, combined, entry.length, trailer.length);

        final InputStream in = new ByteArrayInputStream(combined);
        final CpioArchiveInputStream cis = new CpioArchiveInputStream(in);
        final CpioArchiveEntry e = cis.getNextEntry();
        Assert.assertEquals("", e.getName());
    }

    // Helper methods
    private byte[] createNewFormatEntryBytes(final boolean hasCrc, final long size, final String name) {
        return createNewFormatEntryBytes(hasCrc, size, name, false);
    }

    private byte[] createNewFormatEntryBytes(final boolean hasCrc, final long size, final String name, final boolean forceModeZero) {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            // magic
            bos.write(hasCrc ? MAGIC_NEW_CRC_BYTES : MAGIC_NEW_BYTES);
            // inode (8 hex digits, e.g., "00000001")
            bos.write(padHex(1, 8));
            // mode (force zero if needed)
            bos.write(padHex(forceModeZero ? 0 : 0100644, 8));
            bos.write(padHex(1000, 8)); // UID
            bos.write(padHex(1000, 8)); // GID
            bos.write(padHex(1,8)); // nlink
            bos.write(padHex(1234567890L,8)); // time
            bos.write(padHex(size,8)); // size
            bos.write(padHex(3,8)); // devMajor
            bos.write(padHex(0,8)); // devMinor
            bos.write(padHex(0,8)); // rdevMajor
            bos.write(padHex(0,8)); // rdevMinor
            bos.write(padHex(name.length() + 1,8)); // namesize (include null terminator)
            // chksum - for CRC we'll compute properly later; for now set zero
            bos.write(padHex(0,8));
            // name
            bos.write(name.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            bos.write(0); // null terminator
            // header pad
            final int headerSize = 6 + 13 * 8 + name.length() + 1;
            final int pad = (4 - headerSize % 4) % 4;
            for (int i = 0; i < pad; i++) bos.write(0);
            // data: sequential bytes from 0..size-1
            for (int i = 0; i < (int) size; i++) bos.write(byte) i);
            // data pad
            final int dataPad = (4 - (int) (size %4)) %4;
            for (int i=0; i < dataPad; i++) bos.write(0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    private byte[] createTrailerNewFormat(final boolean hasCrc) {
        // trailer entry: name "TRAILER!!!"
        return createNewFormatEntryBytes(hasCrc, 0, "TRAILER!!!" true); // mode0 is allowed for trailer
    }

    private byte[] createNewFormatEntryBytesCRCWithBadChecksumNullBean" />
