package org.apache.commons.compress.archivers.ar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArArchiveInputStreamTest {

    private byte[] createArArchive(String name, long size, byte[] content) {
        StringBuilder header = new StringBuilder();
        header.append(ArArchiveEntry.HEADER); // 8 bytes

        // Name: 16 bytes
        StringBuilder nameField = new StringBuilder(name);
        while (nameField.length() < 16) {
            nameField.append(" ");
        }
        header.append(nameField.substring(0, 16));

        // Last modified: 12 bytes
        header.append("0           ");

        // User ID: 6 bytes
        header.append("0     ");

        // Group ID: 6 bytes
        header.append("0     ");

        // File mode: 8 bytes
        header.append("100644  ");

        // Length: 10 bytes
        StringBuilder lenField = new StringBuilder(String.valueOf(size));
        while (lenField.length() < 10) {
            lenField.append(" ");
        }
        header.append(lenField.substring(0, 10));

        // Trailer: 2 bytes
        header.append(ArArchiveEntry.TRAILER);

        byte[] headerBytes = header.toString().getBytes();
        int totalLen = headerBytes.length + (content != null ? content.length : 0);
        if (size % 2 != 0 && content != null && content.length > size) {
            // padding if needed
        }
        byte[] result = new byte[totalLen];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        if (content != null && content.length > 0) {
            System.arraycopy(content, 0, result, headerBytes.length, content.length);
        }
        return result;
    }

    @Test
    public void testMatches() {
        byte[] valid = ArArchiveEntry.HEADER.getBytes();
        Assert.assertTrue(ArArchiveInputStream.matches(valid, 8));
        Assert.assertTrue(ArArchiveInputStream.matches(valid, 10));

        Assert.assertFalse(ArArchiveInputStream.matches(valid, 7));
        Assert.assertFalse(ArArchiveInputStream.matches(valid, 0));

        for (int i = 0; i < 8; i++) {
            byte[] corrupted = valid.clone();
            corrupted[i] = (byte) (corrupted[i] ^ 0xFF);
            Assert.assertFalse(ArArchiveInputStream.matches(corrupted, 8));
        }
    }

    @Test
    public void testEmptyStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        try {
            ais.getNextArEntry();
            Assert.fail("Expected IOException on empty stream");
        } catch (IOException e) {
            Assert.assertEquals("failed to read header", e.getMessage());
        }
    }

    @Test
    public void testShortHeader() {
        ByteArrayInputStream bais = new ByteArrayInputStream("!<arc".getBytes());
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        try {
            ais.getNextArEntry();
            Assert.fail("Expected IOException on short header");
        } catch (IOException e) {
            Assert.assertEquals("failed to read header", e.getMessage());
        }
    }

    @Test
    public void testInvalidHeaderContent() {
        byte[] invalidHeader = "XXXXXXXX".getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidHeader);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        try {
            ais.getNextArEntry();
            Assert.fail("Expected IOException on invalid header content");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("invalid header"));
        }
    }

    @Test
    public void testValidHeaderOnlyReturnsNull() throws IOException {
        byte[] validHeader = ArArchiveEntry.HEADER.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(validHeader);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);

        ArArchiveEntry entry = ais.getNextArEntry();
        Assert.assertNull(entry);
    }

    @Test
    public void testReadSingleEntry() throws IOException {
        byte[] content = "Hello World!".getBytes();
        byte[] archive = createArArchive("test.txt", content.length, content);

        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);

        ArchiveEntry entry = ais.getNextEntry();
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof ArArchiveEntry);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertEquals(content.length, entry.getSize());

        byte[] readContent = new byte[content.length];
        int bytesRead = ais.read(readContent, 0, readContent.length);
        Assert.assertEquals(content.length, bytesRead);
        Assert.assertArrayEquals(content, readContent);

        Assert.assertNull(ais.getNextEntry());
        ais.close();
    }

    @Test
    public void testReadByteByByte() throws IOException {
        byte[] content = "ABC".getBytes();
        byte[] archive = createArArchive("abc.txt", content.length, content);

        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);

        ArArchiveEntry entry = ais.getNextArEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("abc.txt", entry.getName());

        int b1 = ais.read();
        Assert.assertEquals('A', b1);
        int b2 = ais.read();
        Assert.assertEquals('B', b2);
        int b3 = ais.read();
        Assert.assertEquals('C', b3);

        ais.close();
    }

    @Test
    public void testReadArrayFull() throws IOException {
        byte[] content = "Sample Data".getBytes();
        byte[] archive = createArArchive("data.bin", content.length, content);

        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);

        ais.getNextArEntry();
        byte[] buf = new byte[content.length];
        int numRead = ais.read(buf);
        Assert.assertEquals(content.length, numRead);
        Assert.assertArrayEquals(content, buf);
        ais.close();
    }

    @Test
    public void testInvalidTrailerShort() {
        StringBuilder sb = new StringBuilder();
        sb.append(ArArchiveEntry.HEADER);
        sb.append("test.txt        ");
        sb.append("0           ");
        sb.append("0     ");
        sb.append("0     ");
        sb.append("100644  ");
        sb.append("10        ");
        sb.append("`"); // 1 byte instead of 2

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        try {
            ais.getNextArEntry();
            Assert.fail("Expected IOException for short trailer");
        } catch (IOException e) {
            Assert.assertEquals("failed to read entry header", e.getMessage());
        }
    }

    @Test
    public void testInvalidTrailerContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(ArArchiveEntry.HEADER);
        sb.append("test.txt        ");
        sb.append("0           ");
        sb.append("0     ");
        sb.append("0     ");
        sb.append("100644  ");
        sb.append("10        ");
        sb.append("XX"); // Invalid trailer bytes

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        try {
            ais.getNextArEntry();
            Assert.fail("Expected IOException for invalid trailer");
        } catch (IOException e) {
            Assert.assertEquals("invalid entry header. not read the content?", e.getMessage());
        }
    }

    @Test
    public void testMultipleEntriesWithPadding() throws IOException {
        byte[] content1 = "12345".getBytes(); // odd length: 5
        byte[] content2 = "678901".getBytes(); // even length: 6

        StringBuilder sb = new StringBuilder();
        sb.append(ArArchiveEntry.HEADER);

        // Entry 1
        sb.append("file1.txt       ");
        sb.append("0           ");
        sb.append("0     ");
        sb.append("0     ");
        sb.append("100644  ");
        sb.append("5         ");
        sb.append(ArArchiveEntry.TRAILER);
        sb.append("12345");
        sb.append("\n"); // padding byte for odd length

        // Entry 2
        sb.append("file2.txt       ");
        sb.append("0           ");
        sb.append("0     ");
        sb.append("0     ");
        sb.append("100644  ");
        sb.append("6         ");
        sb.append(ArArchiveEntry.TRAILER);
        sb.append("678901");

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);

        ArArchiveEntry entry1 = ais.getNextArEntry();
        Assert.assertNotNull(entry1);
        Assert.assertEquals("file1.txt", entry1.getName());
        Assert.assertEquals(5, entry1.getSize());

        byte[] buf1 = new byte[5];
        ais.read(buf1);
        Assert.assertArrayEquals(content1, buf1);

        ArArchiveEntry entry2 = ais.getNextArEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals("file2.txt", entry2.getName());
        Assert.assertEquals(6, entry2.getSize());

        byte[] buf2 = new byte[6];
        ais.read(buf2);
        Assert.assertArrayEquals(content2, buf2);

        Assert.assertNull(ais.getNextArEntry());
        ais.close();
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        final boolean[] underlyingClosed = new boolean[1];
        InputStream mockIn = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() {
                underlyingClosed[0] = true;
            }
        };

        ArArchiveInputStream ais = new ArArchiveInputStream(mockIn);
        Assert.assertFalse(underlyingClosed[0]);
        ais.close();
        Assert.assertTrue(underlyingClosed[0]);
        ais.close(); // Should be a no-op
    }

    @Test
    public void testReadReturnsMinusOneAtEof() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ArArchiveInputStream ais = new ArArchiveInputStream(bais);
        Assert.assertEquals(-1, ais.read());
        Assert.assertEquals(-1, ais.read(new byte[10], 0, 10));
    }
}
