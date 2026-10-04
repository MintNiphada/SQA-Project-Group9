package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class CpioArchiveInputStreamTest {

    @Test
    public void testMatches() {
        Assert.assertFalse(CpioArchiveInputStream.matches(new byte[5], 5));

        byte[] sigOldBinary1 = new byte[]{(byte) 0x71, (byte) 0xc7, 0, 0, 0, 0};
        Assert.assertTrue(CpioArchiveInputStream.matches(sigOldBinary1, 6));

        byte[] sigOldBinary2 = new byte[]{(byte) 0xc7, (byte) 0x71, 0, 0, 0, 0};
        Assert.assertTrue(CpioArchiveInputStream.matches(sigOldBinary2, 6));

        byte[] sigNew = new byte[]{'0', '7', '0', '7', '0', '1'};
        Assert.assertTrue(CpioArchiveInputStream.matches(sigNew, 6));

        byte[] sigNewCrc = new byte[]{'0', '7', '0', '7', '0', '2'};
        Assert.assertTrue(CpioArchiveInputStream.matches(sigNewCrc, 6));

        byte[] sigOldAscii = new byte[]{'0', '7', '0', '7', '0', '7'};
        Assert.assertTrue(CpioArchiveInputStream.matches(sigOldAscii, 6));

        byte[] invalid1 = new byte[]{'1', '7', '0', '7', '0', '1'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid1, 6));

        byte[] invalid2 = new byte[]{'0', '8', '0', '7', '0', '1'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid2, 6));

        byte[] invalid3 = new byte[]{'0', '7', '1', '7', '0', '1'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid3, 6));

        byte[] invalid4 = new byte[]{'0', '7', '0', '8', '0', '1'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid4, 6));

        byte[] invalid5 = new byte[]{'0', '7', '0', '7', '1', '1'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid5, 6));

        byte[] invalid6 = new byte[]{'0', '7', '0', '7', '0', '3'};
        Assert.assertFalse(CpioArchiveInputStream.matches(invalid6, 6));
    }

    @Test
    public void testConstructorsAndAvailableAndReadEmpty() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream in1 = new CpioArchiveInputStream(bais);
        Assert.assertEquals(1, in1.available());
        Assert.assertEquals(-1, in1.read(new byte[10], 0, 10));
        Assert.assertEquals(0, in1.read(new byte[10], 0, 0));
        in1.close();

        CpioArchiveInputStream in2 = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8");
        in2.close();

        CpioArchiveInputStream in3 = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]), 512);
        in3.close();

        CpioArchiveInputStream in4 = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]), 512, "UTF-8");
        in4.close();
    }

    @Test(expected = IOException.class)
    public void testAvailableClosed() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.available();
    }

    @Test(expected = IOException.class)
    public void testReadClosed() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.read(new byte[1], 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBounds() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.read(new byte[5], -1, 1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBoundsLen() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.read(new byte[5], 0, -1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOutOfBoundsSum() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.read(new byte[5], 3, 3);
        } finally {
            in.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.skip(-1);
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadWriteFormatNew() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "testfile.txt", 4);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{'t', 'e', 's', 't'});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveEntry readEntry = in.getNextEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("testfile.txt", readEntry.getName());
        Assert.assertEquals(4, readEntry.getSize());
        Assert.assertEquals(1, in.available());

        byte[] buf = new byte[4];
        int read = in.read(buf, 0, 4);
        Assert.assertEquals(4, read);
        Assert.assertEquals("test", new String(buf));
        Assert.assertEquals(-1, in.read(buf, 0, 4));
        Assert.assertEquals(0, in.available());
        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testReadWriteFormatNewCrc() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", 5);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{'h', 'e', 'l', 'l', 'o'});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("crc.txt", readEntry.getName());

        byte[] buf = new byte[10];
        int read = in.read(buf, 0, 10);
        Assert.assertEquals(5, read);
        Assert.assertEquals(-1, in.read(buf, 0, 10));
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(expected = IOException.class)
    public void testReadWriteFormatNewCrcCorrupted() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", 5);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{'h', 'e', 'l', 'l', 'o'});
        out.closeArchiveEntry();
        out.close();

        byte[] bytes = baos.toByteArray();
        bytes[110] = (byte) (bytes[110] ^ 0xFF);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bytes));
        in.getNextCPIOEntry();
        byte[] buf = new byte[10];
        in.read(buf, 0, 10);
        in.read(buf, 0, 10);
        in.close();
    }

    @Test
    public void testReadWriteFormatOldAscii() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "oldascii.txt", 3);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{'a', 'b', 'c'});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("oldascii.txt", readEntry.getName());

        byte[] buf = new byte[3];
        Assert.assertEquals(3, in.read(buf, 0, 3));
        Assert.assertEquals("abc", new String(buf));
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test
    public void testReadWriteFormatOldBinary() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "oldbinary.txt", 4);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("oldbinary.txt", readEntry.getName());

        byte[] buf = new byte[4];
        Assert.assertEquals(4, in.read(buf, 0, 4));
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, buf);
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test
    public void testReadOldBinarySwapped() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "swap.txt", 2);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(new byte[]{9, 8});
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        byte tmp = data[0];
        data[0] = data[1];
        data[1] = tmp;

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("swap.txt", readEntry.getName());
        in.close();
    }

    @Test
    public void testSkipInEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.bin", 100);
        entry1.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry1);
        out.write(new byte[100]);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.bin", 10);
        entry2.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry2);
        out.write(new byte[10]);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry read1 = in.getNextCPIOEntry();
        Assert.assertNotNull(read1);
        Assert.assertEquals(50, in.skip(50));
        CpioArchiveEntry read2 = in.getNextCPIOEntry();
        Assert.assertNotNull(read2);
        Assert.assertEquals("file2.bin", read2.getName());
        Assert.assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(expected = IOException.class)
    public void testUnknownMagic() throws IOException {
        byte[] invalidData = new byte[]{'9', '9', '9', '9', '9', '9'};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(invalidData));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = EOFException.class)
    public void testTruncatedStream() throws IOException {
        byte[] truncated = new byte[]{'0'};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testMode0NotAllowedNew() throws IOException {
        String header = "070701"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000001"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000000"
                + "00000005"
                + "00000000"
                + "test\0\0";
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(header.getBytes("US-ASCII")));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testMode0NotAllowedOldAscii() throws IOException {
        String header = "070707"
                + "000000"
                + "000000"
                + "000000"
                + "000000"
                + "000000"
                + "000000"
                + "000000"
                + "00000000000"
                + "000005"
                + "00000000000"
                + "test\0";
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(header.getBytes("US-ASCII")));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }

    @Test(expected = IOException.class)
    public void testMode0NotAllowedOldBinary() throws IOException {
        byte[] header = new byte[]{
                (byte) 0x71, (byte) 0xc7,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                0, 0, 0, 0,
                0, 5,
                0, 0, 0, 0,
                't', 'e', 's', 't', 0, 0
        };
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(header));
        try {
            in.getNextCPIOEntry();
        } finally {
            in.close();
        }
    }
}
