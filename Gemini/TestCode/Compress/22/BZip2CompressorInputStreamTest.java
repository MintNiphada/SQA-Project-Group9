package org.apache.commons.compress.compressors.bzip2;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BZip2CompressorInputStreamTest {

    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos)) {
            bzOut.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] compress(byte[] data, int blockSize100k) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos, blockSize100k)) {
            bzOut.write(data);
        }
        return baos.toByteArray();
    }

    @Test
    public void testMatches() {
        byte[] valid = new byte[] { 'B', 'Z', 'h', '9' };
        Assert.assertTrue(BZip2CompressorInputStream.matches(valid, 4));
        Assert.assertTrue(BZip2CompressorInputStream.matches(valid, 3));
        Assert.assertFalse(BZip2CompressorInputStream.matches(valid, 2));
        Assert.assertFalse(BZip2CompressorInputStream.matches(valid, 0));

        byte[] invalid0 = new byte[] { 'A', 'Z', 'h' };
        Assert.assertFalse(BZip2CompressorInputStream.matches(invalid0, 3));

        byte[] invalid1 = new byte[] { 'B', 'A', 'h' };
        Assert.assertFalse(BZip2CompressorInputStream.matches(invalid1, 3));

        byte[] invalid2 = new byte[] { 'B', 'Z', 'a' };
        Assert.assertFalse(BZip2CompressorInputStream.matches(invalid2, 3));
    }

    @Test
    public void testNullInputStream() {
        try {
            new BZip2CompressorInputStream(null);
            Assert.fail("Expected IOException for null InputStream");
        } catch (IOException e) {
            Assert.assertEquals("No InputStream", e.getMessage());
        }
    }

    @Test
    public void testEmptyStream() {
        try {
            new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
            Assert.fail("Expected IOException for empty stream");
        } catch (IOException e) {
            Assert.assertEquals("Stream is not in the BZip2 format", e.getMessage());
        }
    }

    @Test
    public void testInvalidHeaderNotBZh() {
        byte[] badHeader = new byte[] { 'B', 'Z', 'x', '1' };
        try {
            new BZip2CompressorInputStream(new ByteArrayInputStream(badHeader));
            Assert.fail("Expected IOException for bad header");
        } catch (IOException e) {
            Assert.assertEquals("Stream is not in the BZip2 format", e.getMessage());
        }
    }

    @Test
    public void testInvalidBlockSizeLow() {
        byte[] badBlock = new byte[] { 'B', 'Z', 'h', '0' };
        try {
            new BZip2CompressorInputStream(new ByteArrayInputStream(badBlock));
            Assert.fail("Expected IOException for block size 0");
        } catch (IOException e) {
            Assert.assertEquals("BZip2 block size is invalid", e.getMessage());
        }
    }

    @Test
    public void testInvalidBlockSizeHigh() {
        byte[] badBlock = new byte[] { 'B', 'Z', 'h', ':' };
        try {
            new BZip2CompressorInputStream(new ByteArrayInputStream(badBlock));
            Assert.fail("Expected IOException for block size :");
        } catch (IOException e) {
            Assert.assertEquals("BZip2 block size is invalid", e.getMessage());
        }
    }

    @Test
    public void testBadBlockHeader() {
        byte[] badBlockHeader = new byte[] {
            'B', 'Z', 'h', '1',
            0x11, 0x22, 0x33, 0x44, 0x55, 0x66
        };
        try {
            new BZip2CompressorInputStream(new ByteArrayInputStream(badBlockHeader));
            Assert.fail("Expected IOException for bad block header");
        } catch (IOException e) {
            Assert.assertEquals("bad block header", e.getMessage());
        }
    }

    @Test
    public void testReadSingleByteAndArrays() throws IOException {
        String testString = "Hello, BZip2 Compression World! 1234567890 repeating text repeating text";
        byte[] original = testString.getBytes("UTF-8");
        byte[] compressed = compress(original);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            int firstByte = bzIn.read();
            Assert.assertEquals(original[0] & 0xFF, firstByte);

            byte[] buffer = new byte[original.length];
            buffer[0] = (byte) firstByte;
            int readCount = bzIn.read(buffer, 1, buffer.length - 1);
            Assert.assertEquals(original.length - 1, readCount);

            int eof = bzIn.read();
            Assert.assertEquals(-1, eof);
            Assert.assertArrayEquals(original, buffer);
        }
    }

    @Test
    public void testReadBoundariesAndInvalidArguments() throws IOException {
        byte[] original = "Testing boundaries".getBytes("UTF-8");
        byte[] compressed = compress(original);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] dest = new byte[10];
            try {
                bzIn.read(dest, -1, 5);
                Assert.fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }

            try {
                bzIn.read(dest, 0, -1);
                Assert.fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }

            try {
                bzIn.read(dest, 6, 5);
                Assert.fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }

            int zeroRead = bzIn.read(dest, 0, 0);
            Assert.assertEquals(0, zeroRead);
        }
    }

    @Test
    public void testReadAfterClose() throws IOException {
        byte[] original = "Test read after close".getBytes("UTF-8");
        byte[] compressed = compress(original);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        bzIn.close();

        try {
            bzIn.read();
            Assert.fail("Expected IOException reading from closed stream");
        } catch (IOException e) {
            Assert.assertEquals("stream closed", e.getMessage());
        }

        try {
            bzIn.read(new byte[10], 0, 10);
            Assert.fail("Expected IOException reading from closed stream");
        } catch (IOException e) {
            Assert.assertEquals("stream closed", e.getMessage());
        }

        // Multiple close calls should not throw exception
        bzIn.close();
    }

    @Test
    public void testRunLengthEncodingDecode() throws IOException {
        byte[] repeated = new byte[500];
        Arrays.fill(repeated, 0, 200, (byte) 'A');
        Arrays.fill(repeated, 200, 350, (byte) 'B');
        Arrays.fill(repeated, 350, 500, (byte) 'C');

        byte[] compressed = compress(repeated);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] result = new byte[repeated.length];
            int totalRead = 0;
            int r;
            while (totalRead < result.length && (r = bzIn.read(result, totalRead, result.length - totalRead)) != -1) {
                totalRead += r;
            }

            Assert.assertEquals(repeated.length, totalRead);
            Assert.assertArrayEquals(repeated, result);
            Assert.assertEquals(-1, bzIn.read());
        }
    }

    @Test
    public void testMultipleBlocksDecompression() throws IOException {
        byte[] largeData = new byte[150000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251);
        }

        byte[] compressed = compress(largeData, 1);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = bzIn.read(buf, 0, buf.length)) != -1) {
                decompressed.write(buf, 0, read);
            }

            Assert.assertArrayEquals(largeData, decompressed.toByteArray());
        }
    }

    @Test
    public void testConcatenatedStreamsSupported() throws IOException {
        byte[] chunk1 = "First stream data. ".getBytes("UTF-8");
        byte[] chunk2 = "Second stream data. ".getBytes("UTF-8");
        byte[] comp1 = compress(chunk1);
        byte[] comp2 = compress(chunk2);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(comp1);
        combined.write(comp2);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(combined.toByteArray()), true)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = bzIn.read()) != -1) {
                out.write(b);
            }

            byte[] expected = "First stream data. Second stream data. ".getBytes("UTF-8");
            Assert.assertArrayEquals(expected, out.toByteArray());
        }
    }

    @Test
    public void testConcatenatedStreamsNotEnabled() throws IOException {
        byte[] chunk1 = "First stream data. ".getBytes("UTF-8");
        byte[] chunk2 = "Second stream data. ".getBytes("UTF-8");
        byte[] comp1 = compress(chunk1);
        byte[] comp2 = compress(chunk2);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(comp1);
        combined.write(comp2);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(combined.toByteArray()), false)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = bzIn.read()) != -1) {
                out.write(b);
            }

            Assert.assertArrayEquals(chunk1, out.toByteArray());
        }
    }

    @Test
    public void testConcatenatedGarbageAfterStream() throws IOException {
        byte[] chunk1 = "Valid stream content.".getBytes("UTF-8");
        byte[] comp1 = compress(chunk1);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(comp1);
        combined.write(new byte[] { 0x01, 0x02, 0x03, 0x04 });

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(combined.toByteArray()), true)) {
            byte[] buf = new byte[100];
            try {
                while (bzIn.read(buf, 0, buf.length) != -1) {
                    // reading until error
                }
                Assert.fail("Expected IOException on garbage after stream");
            } catch (IOException e) {
                Assert.assertEquals("Garbage after a valid BZip2 stream", e.getMessage());
            }
        }
    }

    @Test
    public void testCorruptedCRCFails() throws IOException {
        byte[] original = "CRC verification test data string".getBytes("UTF-8");
        byte[] compressed = compress(original);

        // Corrupt a byte in the payload
        compressed[compressed.length - 8] ^= 0x55;

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buf = new byte[100];
            try {
                while (bzIn.read(buf, 0, buf.length) != -1) {
                    // decompressing
                }
                Assert.fail("Expected IOException for CRC error");
            } catch (IOException e) {
                Assert.assertTrue(e.getMessage().contains("CRC error") || e.getMessage().contains("unexpected end of stream") || e.getMessage().contains("bad block header"));
            }
        }
    }

    @Test
    public void testTruncatedStream() throws IOException {
        byte[] original = "Truncation test data to verify EOF handling".getBytes("UTF-8");
        byte[] compressed = compress(original);
        byte[] truncated = Arrays.copyOf(compressed, compressed.length - 10);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(truncated))) {
            byte[] buf = new byte[100];
            try {
                while (bzIn.read(buf, 0, buf.length) != -1) {
                    // decompressing
                }
                Assert.fail("Expected IOException for truncated stream");
            } catch (IOException e) {
                Assert.assertTrue(e.getMessage().contains("unexpected end of stream") || e.getMessage().contains("CRC error"));
            }
        }
    }

    @Test
    public void testEmptyBz2BlockStream() throws IOException {
        byte[] original = new byte[0];
        byte[] compressed = compress(original);

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
            Assert.assertEquals(-1, bzIn.read());
            byte[] buf = new byte[10];
            Assert.assertEquals(-1, bzIn.read(buf, 0, buf.length));
        }
    }
}
