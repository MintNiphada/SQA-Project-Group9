package org.apache.commons.compress.compressors.deflate;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class DeflateCompressorInputStreamTest {

    private byte[] compress(byte[] data, boolean withZlibHeader) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, !withZlibHeader);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        } finally {
            deflater.end();
        }
        return baos.toByteArray();
    }

    @Test
    public void testReadSingleByteWithDefaultParameters() throws IOException {
        byte[] original = new byte[]{1, 2, 3, 4, 5};
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            for (byte b : original) {
                int read = in.read();
                Assert.assertEquals(b & 0xFF, read);
            }
            Assert.assertEquals(-1, in.read());
            Assert.assertEquals(5, in.getBytesRead());
        }
    }

    @Test
    public void testReadArrayWithCustomParametersWithoutZlibHeader() throws IOException {
        byte[] original = "Hello Deflate World!".getBytes("UTF-8");
        byte[] compressed = compress(original, false);

        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed), params)) {
            byte[] buffer = new byte[original.length];
            int totalRead = 0;
            int bytesRead;
            while ((bytesRead = in.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
                totalRead += bytesRead;
            }
            Assert.assertEquals(original.length, totalRead);
            Assert.assertArrayEquals(original, buffer);
            Assert.assertEquals(-1, in.read(buffer, 0, buffer.length));
            Assert.assertEquals(original.length, in.getBytesRead());
        }
    }

    @Test
    public void testReadArrayWithCustomParametersWithZlibHeader() throws IOException {
        byte[] original = "Apache Commons Compress".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed), params)) {
            byte[] buffer = new byte[64];
            int read = in.read(buffer, 0, buffer.length);
            Assert.assertEquals(original.length, read);
            byte[] actual = new byte[read];
            System.arraycopy(buffer, 0, actual, 0, read);
            Assert.assertArrayEquals(original, actual);
            Assert.assertEquals(-1, in.read(buffer, 0, buffer.length));
        }
    }

    @Test
    public void testSkip() throws IOException {
        byte[] original = new byte[]{10, 20, 30, 40, 50, 60, 70};
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            long skipped = in.skip(3);
            Assert.assertEquals(3, skipped);
            Assert.assertEquals(40, in.read());
            long skippedMore = in.skip(2);
            Assert.assertEquals(2, skippedMore);
            Assert.assertEquals(70, in.read());
            Assert.assertEquals(-1, in.read());
        }
    }

    @Test
    public void testAvailable() throws IOException {
        byte[] original = new byte[]{1, 2, 3};
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            int available = in.available();
            Assert.assertTrue(available >= 0);
            in.read();
            Assert.assertTrue(in.available() >= 0);
        }
    }

    @Test
    public void testClose() throws IOException {
        byte[] original = new byte[]{1, 2, 3};
        byte[] compressed = compress(original, true);

        DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        in.close();
        try {
            in.read();
            Assert.fail("Expected IOException after stream is closed");
        } catch (IOException expected) {
        }
    }
}
