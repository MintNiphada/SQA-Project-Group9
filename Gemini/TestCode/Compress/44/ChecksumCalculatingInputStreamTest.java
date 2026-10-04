package org.apache.commons.compress.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ChecksumCalculatingInputStreamTest {

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, new ByteArrayInputStream(data));

        Assert.assertEquals(1, stream.read());
        Assert.assertEquals(2, stream.read());
        Assert.assertEquals(3, stream.read());
        Assert.assertEquals(-1, stream.read());

        CRC32 expectedCrc = new CRC32();
        expectedCrc.update(data);
        Assert.assertEquals(expectedCrc.getValue(), stream.getValue());
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] data = new byte[]{10, 20, 30, 40};
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, new ByteArrayInputStream(data));

        byte[] buffer = new byte[4];
        int readCount = stream.read(buffer);

        Assert.assertEquals(4, readCount);
        Assert.assertArrayEquals(data, buffer);

        CRC32 expectedCrc = new CRC32();
        expectedCrc.update(data);
        Assert.assertEquals(expectedCrc.getValue(), stream.getValue());

        Assert.assertEquals(-1, stream.read(buffer));
    }

    @Test
    public void testReadByteArrayWithOffsetAndLength() throws IOException {
        byte[] data = new byte[]{5, 6, 7, 8, 9};
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, new ByteArrayInputStream(data));

        byte[] buffer = new byte[10];
        int readCount = stream.read(buffer, 2, 3);

        Assert.assertEquals(3, readCount);
        Assert.assertEquals(5, buffer[2]);
        Assert.assertEquals(6, buffer[3]);
        Assert.assertEquals(7, buffer[4]);

        CRC32 expectedCrc = new CRC32();
        expectedCrc.update(data, 0, 3);
        Assert.assertEquals(expectedCrc.getValue(), stream.getValue());

        int remainingRead = stream.read(buffer, 5, 5);
        Assert.assertEquals(2, remainingRead);

        int eofRead = stream.read(buffer, 0, 1);
        Assert.assertEquals(-1, eofRead);
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = new byte[]{100, 101, 102};
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, new ByteArrayInputStream(data));

        long skipped = stream.skip(10);
        Assert.assertEquals(1L, skipped);

        skipped = stream.skip(1);
        Assert.assertEquals(1L, skipped);

        skipped = stream.skip(1);
        Assert.assertEquals(1L, skipped);

        skipped = stream.skip(1);
        Assert.assertEquals(0L, skipped);

        CRC32 expectedCrc = new CRC32();
        expectedCrc.update(data);
        Assert.assertEquals(expectedCrc.getValue(), stream.getValue());
    }

    @Test
    public void testEmptyStream() throws IOException {
        byte[] data = new byte[0];
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, new ByteArrayInputStream(data));

        Assert.assertEquals(-1, stream.read());
        Assert.assertEquals(0L, stream.skip(5));
        byte[] buffer = new byte[10];
        Assert.assertEquals(-1, stream.read(buffer));
        Assert.assertEquals(0L, stream.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testReadWithNullStream() throws IOException {
        CRC32 crc = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(crc, null);
        stream.read();
    }

    @Test(expected = NullPointerException.class)
    public void testReadWithNullChecksum() throws IOException {
        byte[] data = new byte[]{1};
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(null, new ByteArrayInputStream(data));
        stream.read();
    }

    @Test(expected = NullPointerException.class)
    public void testGetValueWithNullChecksum() {
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(null, new ByteArrayInputStream(new byte[0]));
        stream.getValue();
    }
}
