package org.apache.commons.compress.archivers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    @Test
    public void testConstructorsAndEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        Assert.assertNull(factory.getEntryEncoding());
        factory.setEntryEncoding("UTF-8");
        Assert.assertEquals("UTF-8", factory.getEntryEncoding());

        ArchiveStreamFactory factory2 = new ArchiveStreamFactory("ISO-8859-1");
        Assert.assertEquals("ISO-8859-1", factory2.getEntryEncoding());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetEntryEncodingThrowsExceptionWhenConstructorUsed() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        factory.setEntryEncoding("ISO-8859-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullName() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknownArchiver() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveInputStreamSevenZ() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStreamAllTypesWithoutEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream is = new ByteArrayInputStream(new byte[0]);

        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.AR, is) instanceof ArArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.ARJ, is) instanceof ArjArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, is) instanceof ZipArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.TAR, is) instanceof TarArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.JAR, is) instanceof JarArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, is) instanceof CpioArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, is) instanceof DumpArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamAllTypesWithEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        InputStream is = new ByteArrayInputStream(new byte[0]);

        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.AR, is) instanceof ArArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.ARJ, is) instanceof ArjArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, is) instanceof ZipArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.TAR, is) instanceof TarArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.JAR, is) instanceof JarArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, is) instanceof CpioArchiveInputStream);
        Assert.assertTrue(factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, is) instanceof DumpArchiveInputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullName() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamUnknownArchiver() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveOutputStreamSevenZ() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStreamAllTypesWithoutEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream os = new ByteArrayOutputStream();

        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.AR, os) instanceof ArArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, os) instanceof ZipArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, os) instanceof TarArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, os) instanceof JarArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, os) instanceof CpioArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamAllTypesWithEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        OutputStream os = new ByteArrayOutputStream();

        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.AR, os) instanceof ArArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, os) instanceof ZipArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, os) instanceof TarArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, os) instanceof JarArchiveOutputStream);
        Assert.assertTrue(factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, os) instanceof CpioArchiveOutputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectUnmarkableStream() throws Exception {
        InputStream is = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new ArchiveStreamFactory().createArchiveInputStream(is);
    }

    @Test
    public void testAutodetectZip() throws Exception {
        byte[] data = new byte[]{0x50, 0x4b, 0x03, 0x04, 0, 0, 0, 0, 0, 0, 0, 0};
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof ZipArchiveInputStream);

        ArchiveInputStream aisEncoded = new ArchiveStreamFactory("UTF-8").createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(aisEncoded instanceof ZipArchiveInputStream);
    }

    @Test
    public void testAutodetectAr() throws Exception {
        byte[] data = "!<arch>\n".getBytes("US-ASCII");
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof ArArchiveInputStream);
    }

    @Test
    public void testAutodetectCpio() throws Exception {
        byte[] data = new byte[]{(byte) 0xc7, 0x71, 0, 0, 0, 0};
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof CpioArchiveInputStream);

        ArchiveInputStream aisEncoded = new ArchiveStreamFactory("UTF-8").createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(aisEncoded instanceof CpioArchiveInputStream);
    }

    @Test
    public void testAutodetectArj() throws Exception {
        byte[] data = new byte[]{0x60, (byte) 0xea, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof ArjArchiveInputStream);
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testAutodetectSevenZ() throws Exception {
        byte[] data = new byte[]{'7', 'z', (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C};
        new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test
    public void testAutodetectDump() throws Exception {
        byte[] data = new byte[32];
        data[24] = (byte) (DumpArchiveInputStream.NFS_MAGIC & 0xFF);
        data[25] = (byte) ((DumpArchiveInputStream.NFS_MAGIC >> 8) & 0xFF);
        data[26] = (byte) ((DumpArchiveInputStream.NFS_MAGIC >> 16) & 0xFF);
        data[27] = (byte) ((DumpArchiveInputStream.NFS_MAGIC >> 24) & 0xFF);
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof DumpArchiveInputStream);
    }

    @Test
    public void testAutodetectTar() throws Exception {
        byte[] data = new byte[512];
        System.arraycopy("ustar\0".getBytes("US-ASCII"), 0, data, 257, 6);
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
        Assert.assertTrue(ais instanceof TarArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectUnknownSignature() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectResetIOException() throws Exception {
        InputStream is = new FilterInputStream(new ByteArrayInputStream(new byte[12])) {
            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated Reset Failure");
            }
        };
        new ArchiveStreamFactory().createArchiveInputStream(is);
    }
}
