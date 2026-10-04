package org.apache.commons.compress.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOUtilsTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<IOUtils> constructor = IOUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        IOUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testCopyDefaultBuffer() throws IOException {
        byte[] data = new byte[20000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long bytesCopied = IOUtils.copy(in, out);

        Assert.assertEquals(data.length, bytesCopied);
        Assert.assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCopyEmptyStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long bytesCopied = IOUtils.copy(in, out);

        Assert.assertEquals(0L, bytesCopied);
        Assert.assertEquals(0, out.size());
    }

    @Test
    public void testCopyCustomBufferSize() throws IOException {
        byte[] data = "Hello, Compress!".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long bytesCopied = IOUtils.copy(in, out, 4);

        Assert.assertEquals(data.length, bytesCopied);
        Assert.assertArrayEquals(data, out.toByteArray());
    }

    @Test(expected = NullPointerException.class)
    public void testCopyNullInput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(null, out);
    }

    @Test(expected = NullPointerException.class)
    public void testCopyNullOutput() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.copy(in, null);
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        long skipped = IOUtils.skip(in, 5);
        Assert.assertEquals(5, skipped);
        Assert.assertEquals(5, in.read());

        skipped = IOUtils.skip(in, 10);
        Assert.assertEquals(4, skipped); // only 4 bytes left: 6, 7, 8, 9
        Assert.assertEquals(-1, in.read());
    }

    @Test
    public void testSkipZeroOrNegative() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        Assert.assertEquals(0, IOUtils.skip(in, 0));
        Assert.assertEquals(0, IOUtils.skip(in, -5));
        Assert.assertEquals(1, in.read());
    }

    @Test
    public void testSkipPartialAndZeroReturn() throws IOException {
        final byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        
        // Custom stream that skips at most 2 bytes per call, and returns 0 after 6 bytes
        InputStream in = new InputStream() {
            private int pos = 0;

            @Override
            public int read() {
                if (pos >= data.length) {
                    return -1;
                }
                return data[pos++] & 0xFF;
            }

            @Override
            public long skip(long n) {
                if (pos >= 6) {
                    return 0; // Simulate skip returning 0
                }
                long toSkip = Math.min(n, 2);
                toSkip = Math.min(toSkip, 6 - pos);
                pos += toSkip;
                return toSkip;
            }
        };

        long skipped = IOUtils.skip(in, 8);
        Assert.assertEquals(6, skipped);
    }

    @Test
    public void testReadFullyByteArray() throws IOException {
        byte[] data = new byte[]{10, 20, 30, 40, 50};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] dest = new byte[5];
        int read = IOUtils.readFully(in, dest);

        Assert.assertEquals(5, read);
        Assert.assertArrayEquals(data, dest);
    }

    @Test
    public void testReadFullyByteArrayShortStream() throws IOException {
        byte[] data = new byte[]{10, 20, 30};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] dest = new byte[5];
        int read = IOUtils.readFully(in, dest);

        Assert.assertEquals(3, read);
        Assert.assertEquals(10, dest[0]);
        Assert.assertEquals(20, dest[1]);
        Assert.assertEquals(30, dest[2]);
        Assert.assertEquals(0, dest[3]);
        Assert.assertEquals(0, dest[4]);
    }

    @Test
    public void testReadFullyWithOffsetAndLength() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] dest = new byte[10];
        int read = IOUtils.readFully(in, dest, 2, 5);

        Assert.assertEquals(5, read);
        byte[] expected = new byte[]{0, 0, 1, 2, 3, 4, 5, 0, 0, 0};
        Assert.assertArrayEquals(expected, dest);
    }

    @Test
    public void testReadFullyEmptyStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        byte[] dest = new byte[5];

        int read = IOUtils.readFully(in, dest);
        Assert.assertEquals(0, read);
    }

    @Test
    public void testReadFullyZeroLength() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        byte[] dest = new byte[5];

        int read = IOUtils.readFully(in, dest, 1, 0);
        Assert.assertEquals(0, read);
    }

    @Test
    public void testReadFullyFragmentedStream() throws IOException {
        final byte[] data = new byte[]{10, 20, 30, 40, 50, 60};
        // Stream that only delivers 1 byte per read(byte[], int, int) call
        InputStream in = new FilterInputStream(new ByteArrayInputStream(data)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (len == 0) {
                    return 0;
                }
                int singleByte = in.read();
                if (singleByte == -1) {
                    return -1;
                }
                b[off] = (byte) singleByte;
                return 1;
            }
        };

        byte[] dest = new byte[6];
        int read = IOUtils.readFully(in, dest, 0, 6);

        Assert.assertEquals(6, read);
        Assert.assertArrayEquals(data, dest);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeOffset() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[5], -1, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeLength() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[5], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyOffsetPlusLenExceedsLength() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[5], 3, 3);
    }

    @Test
    public void testToByteArray() throws IOException {
        byte[] expected = "Apache Commons Compress".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(expected);

        byte[] actual = IOUtils.toByteArray(in);

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testToByteArrayEmpty() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        byte[] actual = IOUtils.toByteArray(in);

        Assert.assertEquals(0, actual.length);
    }

    @Test(expected = NullPointerException.class)
    public void testToByteArrayNullStream() throws IOException {
        IOUtils.toByteArray(null);
    }

    @Test
    public void testCloseQuietlyWithNull() {
        // Should execute cleanly without exception
        IOUtils.closeQuietly(null);
    }

    @Test
    public void testCloseQuietlySuccessful() {
        final AtomicBoolean closed = new AtomicBoolean(false);
        Closeable c = new Closeable() {
            @Override
            public void close() {
                closed.set(true);
            }
        };

        IOUtils.closeQuietly(c);
        Assert.assertTrue(closed.get());
    }

    @Test
    public void testCloseQuietlySwallowsIOException() {
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated close failure");
            }
        };

        // Should swallow the IOException without rethrowing
        IOUtils.closeQuietly(c);
    }
}
