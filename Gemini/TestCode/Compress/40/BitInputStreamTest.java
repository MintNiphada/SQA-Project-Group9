package org.apache.commons.compress.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class BitInputStreamTest {

    @Test(expected = IllegalArgumentException.class)
    public void testReadBitsNegativeCount() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1});
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        bis.readBits(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBitsCountTooLarge() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1});
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        bis.readBits(64);
    }

    @Test
    public void testReadBitsZero() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2});
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        Assert.assertEquals(0L, bis.readBits(0));
    }

    @Test
    public void testReadBitsLittleEndian() throws IOException {
        byte[] bytes = new byte[]{(byte) 0b01010101, (byte) 0b10101010};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        Assert.assertEquals(0b1L, bis.readBits(1));
        Assert.assertEquals(0b10L, bis.readBits(2));
        Assert.assertEquals(0b01010L, bis.readBits(5));
        Assert.assertEquals(0b10101010L, bis.readBits(8));
        Assert.assertEquals(-1L, bis.readBits(1));
    }

    @Test
    public void testReadBitsBigEndian() throws IOException {
        byte[] bytes = new byte[]{(byte) 0b11000000, (byte) 0b00001111};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

        Assert.assertEquals(0b11L, bis.readBits(2));
        Assert.assertEquals(0b0000000000L, bis.readBits(10));
        Assert.assertEquals(0b1111L, bis.readBits(4));
        Assert.assertEquals(-1L, bis.readBits(1));
    }

    @Test
    public void testReadBitsMax63LittleEndian() throws IOException {
        byte[] bytes = new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        long expected = 0x7FFFFFFFFFFFFFFFL;
        Assert.assertEquals(expected, bis.readBits(63));
        Assert.assertEquals(1L, bis.readBits(1));
    }

    @Test
    public void testReadBitsMax63BigEndian() throws IOException {
        byte[] bytes = new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

        long expected = 0x7FFFFFFFFFFFFFFFL;
        Assert.assertEquals(expected, bis.readBits(63));
        Assert.assertEquals(1L, bis.readBits(1));
    }

    @Test
    public void testReadBitsEofOnEmptyStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        Assert.assertEquals(-1L, bis.readBits(1));
    }

    @Test
    public void testReadBitsEofMidStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1});
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        Assert.assertEquals(-1L, bis.readBits(9));
    }

    @Test
    public void testClearBitCache() throws IOException {
        byte[] bytes = new byte[]{0x12, 0x34, 0x56};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        Assert.assertEquals(0x2L, bis.readBits(4));
        bis.clearBitCache();
        Assert.assertEquals(0x4L, bis.readBits(4));
    }

    @Test
    public void testClose() throws IOException {
        final AtomicBoolean closed = new AtomicBoolean(false);
        InputStream in = new InputStream() {
            @Override
            public int read() {
                return 0;
            }

            @Override
            public void close() {
                closed.set(true);
            }
        };

        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        bis.close();
        Assert.assertTrue(closed.get());
    }

    @Test
    public void testMultipleReadsBigEndian() throws IOException {
        byte[] bytes = new byte[]{(byte) 0xAB, (byte) 0xCD};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

        Assert.assertEquals(0xAL, bis.readBits(4));
        Assert.assertEquals(0xBL, bis.readBits(4));
        Assert.assertEquals(0xCL, bis.readBits(4));
        Assert.assertEquals(0xDL, bis.readBits(4));
    }

    @Test
    public void testMultipleReadsLittleEndian() throws IOException {
        byte[] bytes = new byte[]{(byte) 0xAB, (byte) 0xCD};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        Assert.assertEquals(0xBL, bis.readBits(4));
        Assert.assertEquals(0xAL, bis.readBits(4));
        Assert.assertEquals(0xDL, bis.readBits(4));
        Assert.assertEquals(0xCL, bis.readBits(4));
    }
}
