package org.apache.commons.compress.archivers.cpio;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CpioArchiveInputStreamTest implements CpioConstants {

    @Test
    public void testMatches() {
        Assert.assertFalse(CpioArchiveInputStream.matches(new byte[5], 5));

        // Binary matches
        byte[] sig1 = new byte[]{(byte) 0x71, (byte) 0xc7, 0, 0, 0, 0};
        byte[] sig2 = new byte[]{(byte) 0xc7, (byte) 0x71, 0, 0, 0, 0};
        Assert.assertTrue(CpioArchiveInputStream.matches(sig1, 6));
        Assert.assertTrue(CpioArchiveInputStream.matches(sig2, 6));

        // ASCII matches
        byte[] newAscii = "070701".getBytes(StandardCharsets.US_ASCII);
        byte[] newCrc = "070702".getBytes(StandardCharsets.US_ASCII);
        byte[] oldAscii = "070707".getBytes(StandardCharsets.US_ASCII);
        Assert.assertTrue(CpioArchiveInputStream.matches(newAscii, 6));
        Assert.assertTrue(CpioArchiveInputStream.matches(newCrc, 6));
        Assert.assertTrue(CpioArchiveInputStream.matches(oldAscii, 6));

        // ASCII non-matches
        byte[] bad1 = "170701".getBytes(StandardCharsets.US_ASCII);
        byte[] bad2 = "080701".getBytes(StandardCharsets.US_ASCII);
        byte[] bad3 = "071701".getBytes(StandardCharsets.US_ASCII);
        byte[] bad4 = "070801".getBytes(StandardCharsets.US_ASCII);
        byte[] bad5 = "070711".getBytes(StandardCharsets.US_ASCII);
        byte[] bad6 = "070709".getBytes(StandardCharsets.US_ASCII);
        Assert.assertFalse(CpioArchiveInputStream.matches(bad1, 6));
        Assert.assertFalse(CpioArchiveInputStream.matches(bad2, 6));
        Assert.assertFalse(CpioArchiveInputStream.matches(bad3, 6));
        Assert.assertFalse(CpioArchiveInputStream.matches(bad4, 6));
        Assert.assertFalse(CpioArchiveInputStream.matches(bad5, 6));
        Assert.assertFalse(CpioArchiveInputStream.matches(bad6, 6));
    }

    @Test
    public void testReadFormatNew() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(FORMAT_NEW, "file1.txt", 12);
        entry1.setMode(C_ISREG | 0644);
        entry1.setUID(1001);
        entry1.setGID(1002);
        entry1.setTime(12345678L);
        out.putNextEntry(entry1);
        out.write("Hello World!".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(FORMAT_NEW, "file2.txt", 4);
        entry2.setMode(C_ISREG | 0644);
        out.putNextEntry(entry2);
        out.write("Test".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();

        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertEquals(1, in.available());

        CpioArchiveEntry r1 = in.getNextCPIOEntry();
        Assert.assertNotNull(r1);
        Assert.assertEquals("file1.txt", r1.getName());
        Assert.assertEquals(12, r1.getSize());
        Assert.assertEquals(FORMAT_NEW, r1.getFormat());
        Assert.assertEquals(1001, r1.getUID());
        Assert.assertEquals(1002, r1.getGID());
        Assert.assertEquals(12345678L, r1.getTime());

        byte[] buf = new byte[12];
        int readBytes = in.read(buf, 0, 12);
        Assert.assertEquals(12, readBytes);
        Assert.assertEquals("Hello World!", new String(buf, StandardCharsets.US_ASCII));

        // Read at EOF of current entry
        Assert.assertEquals(-1, in.read(buf, 0, 12));
        Assert.assertEquals(0, in.available());

        // Read next entry (using getNextEntry)
        CpioArchiveEntry r2 = in.getNextEntry();
        Assert.assertNotNull(r2);
        Assert.assertEquals("file2.txt", r2.getName());

        // Skip bytes in entry
        long skipped = in.skip(2);
        Assert.assertEquals(2, skipped);
        byte[] smallBuf = new byte[2];
        Assert.assertEquals(2, in.read(smallBuf, 0, 2));
        Assert.assertEquals("st", new String(smallBuf, StandardCharsets.US_ASCII));

        // Trailer reached
        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testReadFormatNewCrcSuccessAndFailure() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW_CRC, "crc.txt", 5);
        entry.setMode(C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write("12345".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        byte[] raw = baos.toByteArray();

        // Valid CRC read
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(raw));
        CpioArchiveEntry r = in.getNextCPIOEntry();
        Assert.assertNotNull(r);
        byte[] b = new byte[5];
        Assert.assertEquals(5, in.read(b, 0, 5));
        Assert.assertEquals(-1, in.read(b, 0, 5)); // Verifies CRC without exception
        Assert.assertNull(in.getNextEntry());
        in.close();

        // Corrupt entry payload to cause CRC error
        byte[] corrupted = raw.clone();
        int payloadOffset = 110 + "crc.txt".length() + 1; // 110 header bytes + name + null terminator
        // Account for header pad count
        int pad = (4 - ((110 + "crc.txt".length() + 1) % 4)) % 4;
        payloadOffset += pad;
        corrupted[payloadOffset] = (byte) '9'; // Corrupt '1' to '9'

        CpioArchiveInputStream inBad = new CpioArchiveInputStream(new ByteArrayInputStream(corrupted));
        inBad.getNextCPIOEntry();
        try {
            while (inBad.read(b, 0, b.length) != -1) {
                // Read until EOF
            }
            Assert.fail("Expected CRC Error IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("CRC Error"));
        } finally {
            inBad.close();
        }
    }

    @Test
    public void testReadFormatOldAscii() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_OLD_ASCII, "oldAscii.txt", 7);
        entry.setMode(C_ISREG | 0644);
        entry.setUID(500);
        entry.setGID(500);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        out.putNextEntry(entry);
        out.write("Content".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry r = in.getNextCPIOEntry();
        Assert.assertNotNull(r);
        Assert.assertEquals("oldAscii.txt", r.getName());
        Assert.assertEquals(7, r.getSize());
        Assert.assertEquals(FORMAT_OLD_ASCII, r.getFormat());
        Assert.assertEquals(500, r.getUID());

        byte[] buf = new byte[10];
        Assert.assertEquals(7, in.read(buf, 0, 10));
        Assert.assertEquals("Content", new String(buf, 0, 7, StandardCharsets.US_ASCII));
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test
    public void testReadFormatOldBinary() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_OLD_BINARY, "bin.txt", 8);
        entry.setMode(C_ISREG | 0755);
        entry.setDevice(1);
        entry.setInode(2);
        out.putNextEntry(entry);
        out.write("bin data".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry r = in.getNextCPIOEntry();
        Assert.assertNotNull(r);
        Assert.assertEquals("bin.txt", r.getName());
        Assert.assertEquals(8, r.getSize());
        Assert.assertEquals(FORMAT_OLD_BINARY, r.getFormat());

        byte[] buf = new byte[8];
        Assert.assertEquals(8, in.read(buf, 0, 8));
        Assert.assertEquals("bin data", new String(buf, StandardCharsets.US_ASCII));
        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testReadFormatOldBinarySwapped() throws Exception {
        // Construct binary entry manually with swapped magic 0xC771
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Magic
        baos.write(new byte[]{(byte) 0xC7, (byte) 0x71});
        // 2 bytes dev, 2 bytes ino, 2 bytes mode (0100644 octal = 0x81A4 -> C771 format is swapped)
        // Helper to write swapped short (LE):
        baos.write(new byte[]{0x01, 0x00}); // dev = 1
        baos.write(new byte[]{0x02, 0x00}); // ino = 2
        baos.write(new byte[]{(byte) 0xA4, (byte) 0x81}); // mode
        baos.write(new byte[]{0x00, 0x00}); // uid
        baos.write(new byte[]{0x00, 0x00}); // gid
        baos.write(new byte[]{0x01, 0x00}); // nlink = 1
        baos.write(new byte[]{0x00, 0x00}); // rdev
        baos.write(new byte[]{0x00, 0x00, 0x00, 0x00}); // mtime (4 bytes)
        baos.write(new byte[]{0x05, 0x00}); // namesize = 5 ("a.t\0")
        baos.write(new byte[]{0x04, 0x00, 0x00, 0x00}); // filesize = 4 (4 bytes)
        baos.write("a.t\0".getBytes(StandardCharsets.US_ASCII));
        // Header pad count: 26 bytes header + 5 bytes name = 31 bytes -> 1 pad byte
        baos.write(0);
        // Data: 4 bytes
        baos.write("data".getBytes(StandardCharsets.US_ASCII));

        // Followed by TRAILER
        baos.write(new byte[]{(byte) 0xC7, (byte) 0x71}); // Magic
        baos.write(new byte[6]); // dev, ino, mode=0
        baos.write(new byte[8]); // uid, gid, nlink, rdev
        baos.write(new byte[4]); // mtime
        baos.write(new byte[]{0x0B, 0x00}); // namesize = 11 ("TRAILER!!!\0")
        baos.write(new byte[4]); // filesize = 0
        baos.write(CPIO_TRAILER.getBytes(StandardCharsets.US_ASCII));
        baos.write(0); // Null terminator
        baos.write(0); // Pad byte

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("a.t", entry.getName());
        Assert.assertEquals(4, entry.getSize());

        byte[] b = new byte[4];
        Assert.assertEquals(4, in.read(b, 0, 4));
        Assert.assertEquals("data", new String(b, StandardCharsets.US_ASCII));

        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test
    public void testSkipWithoutReadingEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(FORMAT_NEW, "file1.txt", 100);
        entry1.setMode(C_ISREG | 0644);
        out.putNextEntry(entry1);
        out.write(new byte[100]);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(FORMAT_NEW, "file2.txt", 5);
        entry2.setMode(C_ISREG | 0644);
        out.putNextEntry(entry2);
        out.write("hello".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        // Get first entry and do not read its data; closing entry via getNextCPIOEntry
        CpioArchiveEntry r1 = in.getNextCPIOEntry();
        Assert.assertEquals("file1.txt", r1.getName());

        CpioArchiveEntry r2 = in.getNextCPIOEntry();
        Assert.assertEquals("file2.txt", r2.getName());
        byte[] b = new byte[5];
        Assert.assertEquals(5, in.read(b, 0, 5));
        Assert.assertEquals("hello", new String(b, StandardCharsets.US_ASCII));

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testReadEdgeCasesAndValidation() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "file.txt", 10);
        entry.setMode(C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write("0123456789".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), 512);

        // Before reading any entry
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, in.read(buf, 0, 10));

        in.getNextCPIOEntry();

        // len == 0
        Assert.assertEquals(0, in.read(buf, 0, 0));

        // Invalid bounds
        try {
            in.read(buf, -1, 5);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {}

        try {
            in.read(buf, 0, -1);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {}

        try {
            in.read(buf, 8, 5);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {}

        // Skip negative length
        try {
            in.skip(-1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}

        // Skip more than entry size
        long skipped = in.skip(20);
        Assert.assertEquals(10, skipped);
        Assert.assertEquals(-1, in.read(buf, 0, 10));

        in.close();

        // Stream closed exceptions
        try {
            in.available();
            Assert.fail("Expected IOException on closed stream");
        } catch (IOException expected) {}

        try {
            in.getNextCPIOEntry();
            Assert.fail("Expected IOException on closed stream");
        } catch (IOException expected) {}

        try {
            in.read(buf, 0, 10);
            Assert.fail("Expected IOException on closed stream");
        } catch (IOException expected) {}

        try {
            in.skip(5);
            Assert.fail("Expected IOException on closed stream");
        } catch (IOException expected) {}
    }

    @Test(expected = IOException.class)
    public void testUnknownMagicThrowsException() throws Exception {
        byte[] badData = "UNKNOWN_MAGIC_DATA_STREAM".getBytes(StandardCharsets.US_ASCII);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(badData));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = EOFException.class)
    public void testTruncatedStreamThrowsEOFException() throws Exception {
        byte[] truncated = new byte[]{ '0', '7', '0' };
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testModeZeroNotTrailerThrowsExceptionNewFormat() throws Exception {
        // Mode 0 on non-trailer entry is invalid
        String header = String.format("070701%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x",
                1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 5, 0); // mode is 0 (2nd field)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header.getBytes(StandardCharsets.US_ASCII));
        baos.write("test\0".getBytes(StandardCharsets.US_ASCII));
        baos.write(new byte[10]);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testModeZeroNotTrailerThrowsExceptionOldAsciiFormat() throws Exception {
        // Old ASCII header: magic(6) dev(6) ino(6) mode(6) uid(6) gid(6) nlink(6) rdev(6) mtime(11) namesize(6) filesize(11)
        String header = String.format("070707%06o%06o%06o%06o%06o%06o%06o%011o%06o%011o",
                1, 1, 0, 0, 0, 1, 0, 0, 5, 0); // mode is 0
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header.getBytes(StandardCharsets.US_ASCII));
        baos.write("test\0".getBytes(StandardCharsets.US_ASCII));

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testModeZeroNotTrailerThrowsExceptionOldBinaryFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Magic
        baos.write(new byte[]{(byte) 0x71, (byte) 0xc7});
        baos.write(new byte[]{0, 1}); // dev
        baos.write(new byte[]{0, 2}); // ino
        baos.write(new byte[]{0, 0}); // mode = 0
        baos.write(new byte[]{0, 0}); // uid
        baos.write(new byte[]{0, 0}); // gid
        baos.write(new byte[]{0, 1}); // nlink
        baos.write(new byte[]{0, 0}); // rdev
        baos.write(new byte[]{0, 0, 0, 0}); // mtime
        baos.write(new byte[]{0, 5}); // namesize = 5
        baos.write(new byte[]{0, 0, 0, 0}); // filesize = 0
        baos.write("test\0".getBytes(StandardCharsets.US_ASCII));

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test
    public void testBlockSizePadding() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int blockSize = 512;
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, FORMAT_NEW, blockSize);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "test.txt", 4);
        entry.setMode(C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write("data".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        Assert.assertEquals(0, archiveBytes.length % blockSize);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archiveBytes), blockSize);
        CpioArchiveEntry r = in.getNextCPIOEntry();
        Assert.assertNotNull(r);
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }
}
