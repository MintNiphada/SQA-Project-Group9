package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtilsTest {

    @Test
    public void testCopyDefaultBufferSize() throws IOException {
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long count = IOUtils.copy(in, out);
        assertEquals(data.length, count);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCopyWithCustomBufferSize() throws IOException {
        byte[] data = new byte[5000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long count = IOUtils.copy(in, out, 1024);
        assertEquals(data.length, count);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testCopyEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long count = IOUtils.copy(in, out);
        assertEquals(0, count);
        assertEquals(0, out.size());
    }

    @Test
    public void testCopyBufferSizeOne() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long count = IOUtils.copy(in, out, 1);
        assertEquals(data.length, count);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testSkipPositive() throws IOException {
        byte[] data = new byte[100];
        InputStream in = new ByteArrayInputStream(data);
        long skipped = IOUtils.skip(in, 50);
        assertEquals(50, skipped);
        assertEquals(50, in.available());
    }

    @Test
    public void testSkipZero() throws IOException {
        byte[] data = new byte[100];
        InputStream in = new ByteArrayInputStream(data);
        long skipped = IOUtils.skip(in, 0);
        assertEquals(0, skipped);
        assertEquals(100, in.available());
    }

    @Test
    public void testSkipMoreThanAvailable() throws IOException {
        byte[] data = new byte[10];
        InputStream in = new ByteArrayInputStream(data);
        long skipped = IOUtils.skip(in, 100);
        assertEquals(10, skipped);
        assertEquals(0, in.available());
    }

    @Test
    public void testSkipWhenSkipReturnsZero() throws IOException {
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
            @Override
            public long skip(long n) throws IOException {
                return 0;
            }
        };
        long skipped = IOUtils.skip(in, 10);
        assertEquals(0, skipped);
    }

    @Test
    public void testReadFullyFullArray() throws IOException {
        byte[] data = new byte[50];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[50];
        int count = IOUtils.readFully(in, buf);
        assertEquals(50, count);
        assertArrayEquals(data, buf);
    }

    @Test
    public void testReadFullyPartialArray() throws IOException {
        byte[] data = new byte[50];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[100];
        int count = IOUtils.readFully(in, buf, 10, 50);
        assertEquals(50, count);
        for (int i = 0; i < 10; i++) {
            assertEquals(0, buf[i]);
        }
        for (int i = 10; i < 60; i++) {
            assertEquals(data[i - 10], buf[i]);
        }
        for (int i = 60; i < 100; i++) {
            assertEquals(0, buf[i]);
        }
    }

    @Test
    public void testReadFullyEndOfStream() throws IOException {
        byte[] data = new byte[30];
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[50];
        int count = IOUtils.readFully(in, buf);
        assertEquals(30, count);
    }

    @Test
    public void testReadFullyOffsetAndLenEndOfStream() throws IOException {
        byte[] data = new byte[20];
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[50];
        int count = IOUtils.readFully(in, buf, 5, 40);
        assertEquals(20, count);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeLen() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFullyOffsetPlusLenExceedsLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        IOUtils.readFully(in, new byte[10], 5, 6);
    }

    @Test
    public void testToByteArray() throws IOException {
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        InputStream in = new ByteArrayInputStream(data);
        byte[] result = IOUtils.toByteArray(in);
        assertArrayEquals(data, result);
    }

    @Test(expected = NullPointerException.class)
    public void testToByteArrayNullInput() throws IOException {
        IOUtils.toByteArray(null);
    }

    @Test
    public void testCloseQuietlyNull() {
        IOUtils.closeQuietly(null);
    }

    @Test
    public void testCloseQuietlyNormal() throws IOException {
        Closeable c = new Closeable() {
            boolean closed = false;
            @Override
            public void close() throws IOException {
                closed = true;
            }
        };
        IOUtils.closeQuietly(c);
    }

    @Test
    public void testCloseQuietlyThrowsIOException() {
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("test exception");
            }
        };
        IOUtils.closeQuietly(c);
    }
}
