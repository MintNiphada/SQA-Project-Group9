package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class CpioArchiveInputStreamTest {

    private static final int BLOCK_SIZE = 512;

    private byte[] createOldBinaryEntryBytes(boolean swapHalfWord, String name, byte[] data, int mode) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] magic = swapHalfWord ? new byte[]{(byte) 0xC7, 0x71} : new byte[]{0x71, (byte) 0xC7};
        baos.write(magic);
        writeShort(baos, 0, swapHalfWord);
        writeShort(baos, 1, swapHalfWord);
        writeShort(baos, mode, swapHalfWord);
        writeShort(baos, 0, swapHalfWord);
        writeShort(baos, 0, swapHalfWord);
        writeShort(baos, 1, swapHalfWord);
        writeShort(baos, 0, swapHalfWord);
        writeInt(baos, 0, swapHalfWord);
        int namesize = name.length() + 1;
        writeShort(baos, namesize, swapHalfWord);
        writeInt(baos, data.length, swapHalfWord);
        baos.write(name.getBytes("US-ASCII"));
        baos.write(0);
        int headerSize = 26 + namesize;
        int pad = (4 - (headerSize % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            baos.write(0);
        }
        baos.write(data);
        int dataPad = (4 - (data.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    private void writeShort(java.io.OutputStream os, int value, boolean swap) throws IOException {
        byte[] b = new byte[2];
        if (swap) {
            b[0] = (byte) (value & 0xFF);
            b[1] = (byte) ((value >> 8) & 0xFF);
        } else {
            b[0] = (byte) ((value >> 8) & 0xFF);
            b[1] = (byte) (value & 0xFF);
        }
        os.write(b);
    }

    private void writeInt(java.io.OutputStream os, int value, boolean swap) throws IOException {
        byte[] b = new byte[4];
        if (swap) {
            b[0] = (byte) (value & 0xFF);
            b[1] = (byte) ((value >> 8) & 0xFF);
            b[2] = (byte) ((value >> 16) & 0xFF);
            b[3] = (byte) ((value >> 24) & 0xFF);
        } else {
            b[0] = (byte) ((value >> 24) & 0xFF);
            b[1] = (byte) ((value >> 16) & 0xFF);
            b[2] = (byte) ((value >> 8) & 0xFF);
            b[3] = (byte) (value & 0xFF);
        }
        os.write(b);
    }

    private byte[] createNewEntryBytes(boolean hasCrc, String name, byte[] data, int mode) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        String magic = hasCrc ? "070702" : "070701";
        baos.write(magic.getBytes("US-ASCII"));
        writeAsciiLong(baos, 1, 8, 16);
        writeAsciiLong(baos, mode, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, 1, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, data.length, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        writeAsciiLong(baos, 0, 8, 16);
        int namesize = name.length() + 1;
        writeAsciiLong(baos, namesize, 8, 16);
        long chksum = 0;
        if (hasCrc) {
            for (byte b : data) {
                chksum += b & 0xFF;
            }
        }
        writeAsciiLong(baos, chksum, 8, 16);
        baos.write(name.getBytes("US-ASCII"));
        baos.write(0);
        int headerSize = 110 + namesize;
        int pad = (4 - (headerSize % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            baos.write(0);
        }
        baos.write(data);
        int dataPad = (4 - (data.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    private void writeAsciiLong(java.io.OutputStream os, long value, int length, int radix) throws IOException {
        String s = Long.toString(value, radix);
        while (s.length() < length) {
            s = "0" + s;
        }
        os.write(s.getBytes("US-ASCII"));
    }

    private byte[] createOldAsciiEntryBytes(String name, byte[] data, int mode) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write("070707".getBytes("US-ASCII"));
        writeAsciiLong(baos, 0, 6, 8);
        writeAsciiLong(baos, 1, 6, 8);
        writeAsciiLong(baos, mode, 6, 8);
        writeAsciiLong(baos, 0, 6, 8);
        writeAsciiLong(baos, 0, 6, 8);
        writeAsciiLong(baos, 1, 6, 8);
        writeAsciiLong(baos, 0, 6, 8);
        writeAsciiLong(baos, 0, 11, 8);
        int namesize = name.length() + 1;
        writeAsciiLong(baos, namesize, 6, 8);
        writeAsciiLong(baos, data.length, 11, 8);
        baos.write(name.getBytes("US-ASCII"));
        baos.write(0);
        baos.write(data);
        int dataPad = (4 - (data.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    @Test
    public void testMatchesValidOldBinaryBigEndian() {
        byte[] signature = new byte[]{0x71, (byte) 0xC7, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testMatchesValidOldBinaryLittleEndian() {
        byte[] signature = new byte[]{(byte) 0xC7, 0x71, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testMatchesValidNew() {
        byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x31};
        assertTrue(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testMatchesValidNewCrc() {
        byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x32};
        assertTrue(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testMatchesValidOldAscii() {
        byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x37};
        assertTrue(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testMatchesInvalidLength() {
        byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30};
        assertFalse(CpioArchiveInputStream.matches(signature, 5));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] signature = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x33};
        assertFalse(CpioArchiveInputStream.matches(signature, 6));
    }

    @Test
    public void testAvailableBeforeEof() throws Exception {
        byte[] entryData = createNewEntryBytes(false, "file1", new byte[]{1, 2, 3}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals(1, in.available());
    }

    @Test
    public void testAvailableAfterEof() throws Exception {
        byte[] entryData = createNewEntryBytes(false, "file1", new byte[]{1, 2, 3}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        byte[] buf = new byte[3];
        in.read(buf);
        assertEquals(0, in.available());
    }

    @Test(expected = IOException.class)
    public void testAvailableAfterClose() throws Exception {
        byte[] entryData = createNewEntryBytes(false, "file1", new byte[]{1, 2, 3}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.close();
        in.available();
    }

    @Test
    public void testClose() throws Exception {
        byte[] entryData = createNewEntryBytes(false, "file1", new byte[]{1, 2, 3}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.close();
        try {
            in.read();
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void testGetNextEntryOldBinaryBigEndian() throws Exception {
        byte[] data = createOldBinaryEntryBytes(false, "test", new byte[]{10, 20, 30}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("test", entry.getName());
        assertEquals(3, entry.getSize());
        assertEquals(0100644, entry.getMode());
    }

    @Test
    public void testGetNextEntryOldBinaryLittleEndian() throws Exception {
        byte[] data = createOldBinaryEntryBytes(true, "test", new byte[]{10, 20, 30}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("test", entry.getName());
        assertEquals(3, entry.getSize());
    }

    @Test
    public void testGetNextEntryNew() throws Exception {
        byte[] data = createNewEntryBytes(false, "newfile", new byte[]{1, 2, 3, 4}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("newfile", entry.getName());
        assertEquals(4, entry.getSize());
    }

    @Test
    public void testGetNextEntryNewCrc() throws Exception {
        byte[] content = new byte[]{5, 6, 7};
        byte[] data = createNewEntryBytes(true, "crcfile", content, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("crcfile", entry.getName());
        assertEquals(3, entry.getSize());
        byte[] buf = new byte[3];
        int read = in.read(buf);
        assertEquals(3, read);
        assertArrayEquals(content, buf);
    }

    @Test
    public void testGetNextEntryOldAscii() throws Exception {
        byte[] data = createOldAsciiEntryBytes("oldascii", new byte[]{7, 8, 9}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("oldascii", entry.getName());
        assertEquals(3, entry.getSize());
    }

    @Test
    public void testGetNextEntryTrailer() throws Exception {
        byte[] data = createNewEntryBytes(false, "TRAILER!!!", new byte[0], 0);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNull(entry);
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryUnknownMagic() throws Exception {
        byte[] data = new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x33, 0, 0, 0, 0};
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryMode0NonTrailerNew() throws Exception {
        byte[] data = createNewEntryBytes(false, "badfile", new byte[0], 0);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryMode0NonTrailerOldAscii() throws Exception {
        byte[] data = createOldAsciiEntryBytes("badfile", new byte[0], 0);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryMode0NonTrailerOldBinary() throws Exception {
        byte[] data = createOldBinaryEntryBytes(false, "badfile", new byte[0], 0);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
    }

    @Test
    public void testReadData() throws Exception {
        byte[] content = new byte[]{10, 20, 30, 40};
        byte[] data = createNewEntryBytes(false, "file", content, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        byte[] buf = new byte[4];
        int read = in.read(buf);
        assertEquals(4, read);
        assertArrayEquals(content, buf);
        assertEquals(-1, in.read(buf));
    }

    @Test(expected = IOException.class)
    public void testReadCrcError() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        byte[] data = createNewEntryBytes(true, "crcfile", content, 0100644);
        data[data.length - 1] = 0;
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        byte[] buf = new byte[3];
        in.read(buf);
        in.read(buf);
    }

    @Test
    public void testReadPadding() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        byte[] data = createNewEntryBytes(false, "padfile", content, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        byte[] buf = new byte[3];
        assertEquals(3, in.read(buf));
        assertEquals(-1, in.read(buf));
        assertEquals(0, in.available());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidOffsetNegative() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        in.read(new byte[1], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidLengthNegative() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        in.read(new byte[1], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidOffsetPlusLength() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        in.read(new byte[1], 0, 2);
    }

    @Test
    public void testReadLenZero() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        assertEquals(0, in.read(new byte[1], 0, 0));
    }

    @Test
    public void testReadAfterClose() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.close();
        try {
            in.read(new byte[1]);
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void testSkip() throws Exception {
        byte[] content = new byte[]{1, 2, 3, 4, 5};
        byte[] data = createNewEntryBytes(false, "file", content, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        long skipped = in.skip(2);
        assertEquals(2, skipped);
        byte[] buf = new byte[3];
        int read = in.read(buf);
        assertEquals(3, read);
        assertEquals(3, buf[0]);
        assertEquals(4, buf[1]);
        assertEquals(5, buf[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        in.skip(-1);
    }

    @Test
    public void testSkipPastEnd() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        byte[] data = createNewEntryBytes(false, "file", content, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        long skipped = in.skip(10);
        assertEquals(3, skipped);
        assertEquals(-1, in.read(new byte[1]));
    }

    @Test
    public void testMultipleEntries() throws Exception {
        byte[] entry1 = createNewEntryBytes(false, "file1", new byte[]{1, 2}, 0100644);
        byte[] entry2 = createNewEntryBytes(false, "file2", new byte[]{3, 4, 5}, 0100644);
        byte[] combined = new byte[entry1.length + entry2.length];
        System.arraycopy(entry1, 0, combined, 0, entry1.length);
        System.arraycopy(entry2, 0, combined, entry1.length, entry2.length);
        ByteArrayInputStream bin = new ByteArrayInputStream(combined);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        CpioArchiveEntry e1 = in.getNextCPIOEntry();
        assertEquals("file1", e1.getName());
        byte[] buf1 = new byte[2];
        in.read(buf1);
        assertArrayEquals(new byte[]{1, 2}, buf1);
        CpioArchiveEntry e2 = in.getNextCPIOEntry();
        assertEquals("file2", e2.getName());
        byte[] buf2 = new byte[3];
        in.read(buf2);
        assertArrayEquals(new byte[]{3, 4, 5}, buf2);
    }

    @Test
    public void testConstructorWithEncoding() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin, "UTF-8");
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithBlockSize() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin, 1024);
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin, 1024, "UTF-8");
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
    }

    @Test
    public void testReadReturnsMinusOneWhenEntryNull() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        assertEquals(-1, in.read(new byte[1]));
    }

    @Test
    public void testReadReturnsMinusOneWhenEntryEof() throws Exception {
        byte[] data = createNewEntryBytes(false, "file", new byte[]{1}, 0100644);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin);
        in.getNextCPIOEntry();
        byte[] buf = new byte[1];
        in.read(buf);
        assertEquals(-1, in.read(buf));
    }

    @Test
    public void testSkipRemainderOfLastBlock() throws Exception {
        byte[] trailerData = createNewEntryBytes(false, "TRAILER!!!", new byte[0], 0);
        ByteArrayInputStream bin = new ByteArrayInputStream(trailerData);
        CpioArchiveInputStream in = new CpioArchiveInputStream(bin, BLOCK_SIZE);
        assertNull(in.getNextCPIOEntry());
    }
}
