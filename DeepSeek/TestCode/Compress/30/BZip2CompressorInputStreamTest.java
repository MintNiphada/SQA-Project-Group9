package org.apache.commons.compress.compressors.bzip2;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BZip2CompressorInputStreamTest {

    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(bos);
        bzos.write(data);
        bzos.close();
        return bos.toByteArray();
    }

    @Test
    public void testMatchesValidSignature() {
        byte[] sig = new byte[] { 'B', 'Z', 'h' };
        assertTrue(BZip2CompressorInputStream.matches(sig, 3));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] sig = new byte[] { 'B', 'Z', 'x' };
        assertFalse(BZip2CompressorInputStream.matches(sig, 3));
    }

    @Test
    public void testMatchesShortLength() {
        byte[] sig = new byte[] { 'B', 'Z' };
        assertFalse(BZip2CompressorInputStream.matches(sig, 2));
    }

    @Test
    public void testMatchesLengthZero() {
        assertFalse(BZip2CompressorInputStream.matches(new byte[0], 0));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStream() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testConstructorInvalidStream() throws IOException {
        byte[] invalid = new byte[] { 1, 2, 3 };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalid));
    }

    @Test
    public void testConstructorValidStream() throws IOException {
        byte[] compressed = compress("Hello".getBytes());
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        assertNotNull(in);
        in.close();
    }

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] data = "Test".getBytes();
        byte[] compressed = compress(data);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i] & 0xff, in.read());
        }
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] data = "Hello World".getBytes();
        byte[] compressed = compress(data);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertTrue(n > 0);
        byte[] result = Arrays.copyOf(buf, n);
        assertArrayEquals(data, result);
        in.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeOffset() throws IOException {
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        in.read(new byte[10], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeLength() throws IOException {
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        in.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithOffsetPlusLengthExceeds() throws IOException {
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        in.read(new byte[10], 5, 6);
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        in.close();
        in.read();
    }

    @Test
    public void testReadEmptyStream() throws IOException {
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadLargeData() throws IOException {
        byte[] data = new byte[100000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        byte[] compressed = compress(data);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] result = new byte[data.length];
        int offset = 0;
        int n;
        while ((n = in.read(result, offset, result.length - offset)) > 0) {
            offset += n;
        }
        assertEquals(data.length, offset);
        assertArrayEquals(data, result);
        in.close();
    }

    @Test
    public void testDecompressConcatenatedTrue() throws IOException {
        byte[] data1 = "First".getBytes();
        byte[] data2 = "Second".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream out1 = new BZip2CompressorOutputStream(bos);
        out1.write(data1);
        out1.close();
        BZip2CompressorOutputStream out2 = new BZip2CompressorOutputStream(bos);
        out2.write(data2);
        out2.close();
        byte[] compressed = bos.toByteArray();
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed), true);
        byte[] buf = new byte[100];
        int n = in.read(buf);
        String result = new String(buf, 0, n);
        assertEquals("FirstSecond", result);
        in.close();
    }

    @Test
    public void testDecompressConcatenatedFalse() throws IOException {
        byte[] data1 = "First".getBytes();
        byte[] data2 = "Second".getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream out1 = new BZip2CompressorOutputStream(bos);
        out1.write(data1);
        out1.close();
        BZip2CompressorOutputStream out2 = new BZip2CompressorOutputStream(bos);
        out2.write(data2);
        out2.close();
        byte[] compressed = bos.toByteArray();
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed), false);
        byte[] buf = new byte[100];
        int n = in.read(buf);
        String result = new String(buf, 0, n);
        assertEquals("First", result);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(expected = IOException.class)
    public void testCrcError() throws IOException {
        byte[] data = "data".getBytes();
        byte[] compressed = compress(data);
        compressed[compressed.length / 2] ^= 0xff;
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[100];
        while (in.read(buf) != -1) {
        }
        in.close();
    }

    @Test
    public void testReadOffsetLengthBoundary() throws IOException {
        byte[] data = "abc".getBytes();
        byte[] compressed = compress(data);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[5];
        int n = in.read(buf, 2, 3);
        assertEquals(3, n);
        assertEquals('a', buf[2]);
        assertEquals('b', buf[3]);
        assertEquals('c', buf[4]);
        in.close();
    }

    @Test
    public void testReadPartialBuffer() throws IOException {
        byte[] data = "Hello".getBytes();
        byte[] compressed = compress(data);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[2];
        int n = in.read(buf);
        assertEquals(2, n);
        assertEquals('H', buf[0]);
        assertEquals('e', buf[1]);
        n = in.read(buf);
        assertEquals(2, n);
        assertEquals('l', buf[0]);
        assertEquals('l', buf[1]);
        n = in.read(buf);
        assertEquals(1, n);
        assertEquals('o', buf[0]);
        assertEquals(-1, in.read(buf));
        in.close();
    }
}
