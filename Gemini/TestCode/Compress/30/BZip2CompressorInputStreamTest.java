package org.apache.commons.compress.compressors.bzip2;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BZip2CompressorInputStreamTest {

    private static final byte[] EMPTY_BZIP2 = new byte[] {
        'B', 'Z', 'h', '9',
        0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
        0x00, 0x00, 0x00, 0x00
    };

    private static final byte[] HELLO_WORLD_BZIP2 = new byte[] {
        0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59, 0x26, 0x53, 0x59, 0x6e,
        (byte) 0xa3, (byte) 0xa9, (byte) 0xc8, 0x00, 0x00, 0x01, 0x57,
        (byte) 0x80, 0x00, 0x10, 0x40, 0x00, 0x20, 0x00, 0x21, 0x22,
        (byte) 0x9a, 0x3b, 0x20, 0x00, 0x31, 0x06, 0x40, (byte) 0x84,
        (byte) 0xb3, 0x63, 0x6e, 0x2e, (byte) 0xe4, (byte) 0x8a, 0x70,
        (byte) 0xa1, 0x20, (byte) 0xdd, 0x47, 0x53, (byte) 0x90
    };

    private static final byte[] RLE_DATA_BZIP2 = new byte[] {
        0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59, 0x26, 0x53, 0x59, (byte) 0x85,
        0x63, 0x2a, 0x44, 0x00, 0x00, 0x01, 0x41, (byte) 0x80, 0x00, 0x10,
        0x02, 0x00, 0x04, 0x00, 0x20, 0x00, 0x30, (byte) 0xcd, 0x34, 0x11,
        (byte) 0x91, (byte) 0xa7, 0x0b, 0x47, 0x45, 0x3a, (byte) 0xa8,
        (byte) 0xac, 0x1a, (byte) 0xd7, (byte) 0xb0, (byte) 0x91, 0x21,
        0x0a, (byte) 0xcc, 0x65, 0x48, 0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
        (byte) 0x85, 0x63, 0x2a, 0x44
    };

    @Test
    public void testMatches() {
        Assert.assertFalse(BZip2CompressorInputStream.matches(new byte[] {'B', 'Z'}, 2));
        Assert.assertFalse(BZip2CompressorInputStream.matches(new byte[] {'A', 'Z', 'h'}, 3));
        Assert.assertFalse(BZip2CompressorInputStream.matches(new byte[] {'B', 'A', 'h'}, 3));
        Assert.assertFalse(BZip2CompressorInputStream.matches(new byte[] {'B', 'Z', 'a'}, 3));
        Assert.assertTrue(BZip2CompressorInputStream.matches(new byte[] {'B', 'Z', 'h', '9'}, 4));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStream() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testEmptyStream() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IOException.class)
    public void testInvalidMagic() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[] {'B', 'Z', 'x', '1'}));
    }

    @Test(expected = IOException.class)
    public void testInvalidBlockSize() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[] {'B', 'Z', 'h', '0'}));
    }

    @Test(expected = IOException.class)
    public void testInvalidBlockSizeTooHigh() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[] {'B', 'Z', 'h', 'a'}));
    }

    @Test(expected = IOException.class)
    public void testBadBlockHeader() throws IOException {
        byte[] badHeader = new byte[] {
            'B', 'Z', 'h', '9',
            0x32, 0x41, 0x59, 0x26, 0x53, 0x59
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badHeader));
    }

    @Test
    public void testEmptyBzip2Decompression() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        Assert.assertEquals(-1, bzIn.read());
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, bzIn.read(buf, 0, 10));
        bzIn.close();
    }

    @Test
    public void testDecompressSingleByteRead() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(HELLO_WORLD_BZIP2));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = bzIn.read()) != -1) {
            out.write(b);
        }
        bzIn.close();
        Assert.assertEquals("hello world\n", new String(out.toByteArray()));
    }

    @Test
    public void testDecompressBufferRead() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(HELLO_WORLD_BZIP2));
        byte[] buf = new byte[256];
        int read = bzIn.read(buf, 0, buf.length);
        Assert.assertEquals(12, read);
        Assert.assertEquals("hello world\n", new String(buf, 0, read));
        Assert.assertEquals(-1, bzIn.read(buf, 0, buf.length));
        bzIn.close();
    }

    @Test
    public void testRLEDecompression() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(RLE_DATA_BZIP2));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int n;
        while ((n = bzIn.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, n);
        }
        bzIn.close();
        byte[] expected = new byte[10];
        Arrays.fill(expected, (byte) 'A');
        Assert.assertArrayEquals(expected, out.toByteArray());
    }

    @Test
    public void testConcatenatedStreams() throws IOException {
        byte[] concat = new byte[EMPTY_BZIP2.length * 2];
        System.arraycopy(EMPTY_BZIP2, 0, concat, 0, EMPTY_BZIP2.length);
        System.arraycopy(EMPTY_BZIP2, 0, concat, EMPTY_BZIP2.length, EMPTY_BZIP2.length);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(concat), true);
        Assert.assertEquals(-1, bzIn.read());
        bzIn.close();
    }

    @Test(expected = IOException.class)
    public void testConcatenatedGarbage() throws IOException {
        byte[] concat = new byte[EMPTY_BZIP2.length + 4];
        System.arraycopy(EMPTY_BZIP2, 0, concat, 0, EMPTY_BZIP2.length);
        concat[EMPTY_BZIP2.length] = 'X';
        concat[EMPTY_BZIP2.length + 1] = 'Y';
        concat[EMPTY_BZIP2.length + 2] = 'Z';
        concat[EMPTY_BZIP2.length + 3] = 'W';

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(concat), true);
        bzIn.read();
        bzIn.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBoundsNegativeOffs() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        try {
            bzIn.read(new byte[10], -1, 5);
        } finally {
            bzIn.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBoundsNegativeLen() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        try {
            bzIn.read(new byte[10], 0, -1);
        } finally {
            bzIn.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBoundsLenGreaterThanDest() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        try {
            bzIn.read(new byte[10], 5, 6);
        } finally {
            bzIn.close();
        }
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        bzIn.close();
        bzIn.read();
    }

    @Test(expected = IOException.class)
    public void testReadBufferAfterClose() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        bzIn.close();
        bzIn.read(new byte[10], 0, 5);
    }

    @Test(expected = IOException.class)
    public void testCorruptedCrc() throws IOException {
        byte[] corrupted = EMPTY_BZIP2.clone();
        corrupted[10] = (byte) 0xFF;
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(corrupted));
        bzIn.read();
        bzIn.close();
    }

    @Test(expected = IOException.class)
    public void testTruncatedStreamInBlock() throws IOException {
        byte[] truncated = Arrays.copyOf(HELLO_WORLD_BZIP2, 20);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(truncated));
        bzIn.read();
        bzIn.close();
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2));
        bzIn.close();
        bzIn.close();
    }
}
