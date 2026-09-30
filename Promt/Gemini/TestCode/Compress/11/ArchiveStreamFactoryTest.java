package org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private ArchiveStreamFactory factory;

    @Before
    public void setUp() {
        factory = new ArchiveStreamFactory();
    }

    @Test
    public void testConstants() {
        Assert.assertEquals("ar", ArchiveStreamFactory.AR);
        Assert.assertEquals("cpio", ArchiveStreamFactory.CPIO);
        Assert.assertEquals("dump", ArchiveStreamFactory.DUMP);
        Assert.assertEquals("jar", ArchiveStreamFactory.JAR);
        Assert.assertEquals("tar", ArchiveStreamFactory.TAR);
        Assert.assertEquals("zip", ArchiveStreamFactory.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullArchiverName() throws Exception {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullStream() throws Exception {
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknownArchiver() throws Exception {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStreamByName() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);

        ArchiveInputStream arIn = factory.createArchiveInputStream("AR", in);
        Assert.assertTrue(arIn instanceof ArArchiveInputStream);

        ArchiveInputStream zipIn = factory.createArchiveInputStream("zIp", in);
        Assert.assertTrue(zipIn instanceof ZipArchiveInputStream);

        ArchiveInputStream tarIn = factory.createArchiveInputStream("TaR", in);
        Assert.assertTrue(tarIn instanceof TarArchiveInputStream);

        ArchiveInputStream jarIn = factory.createArchiveInputStream("JAR", in);
        Assert.assertTrue(jarIn instanceof JarArchiveInputStream);

        ArchiveInputStream cpioIn = factory.createArchiveInputStream("cPiO", in);
        Assert.assertTrue(cpioIn instanceof CpioArchiveInputStream);

        byte[] dumpBytes = new byte[32];
        ArchiveInputStream dumpIn = factory.createArchiveInputStream("DUMP", new ByteArrayInputStream(dumpBytes));
        Assert.assertTrue(dumpIn instanceof DumpArchiveInputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullArchiverName() throws Exception {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullStream() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamUnknownArchiver() throws Exception {
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamDumpNotSupported() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStreamByName() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        ArchiveOutputStream arOut = factory.createArchiveOutputStream("Ar", out);
        Assert.assertTrue(arOut instanceof ArArchiveOutputStream);

        ArchiveOutputStream zipOut = factory.createArchiveOutputStream("zIp", out);
        Assert.assertTrue(zipOut instanceof ZipArchiveOutputStream);

        ArchiveOutputStream tarOut = factory.createArchiveOutputStream("TAR", out);
        Assert.assertTrue(tarOut instanceof TarArchiveOutputStream);

        ArchiveOutputStream jarOut = factory.createArchiveOutputStream("Jar", out);
        Assert.assertTrue(jarOut instanceof JarArchiveOutputStream);

        ArchiveOutputStream cpioOut = factory.createArchiveOutputStream("cpio", out);
        Assert.assertTrue(cpioOut instanceof CpioArchiveOutputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectNullStream() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectMarkNotSupported() throws Exception {
        InputStream unmarkableStream = new FilterInputStream(new ByteArrayInputStream(new byte[10])) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(unmarkableStream);
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectEmptyStream() throws Exception {
        factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectUnrecognizedStream() throws Exception {
        byte[] junk = new byte[1024];
        for (int i = 0; i < junk.length; i++) {
            junk[i] = (byte) (i ^ 0x5A);
        }
        factory.createArchiveInputStream(new ByteArrayInputStream(junk));
    }

    @Test
    public void testAutodetectZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.putArchiveEntry(new ZipArchiveEntry("entry.txt"));
        zaos.write(new byte[]{1, 2, 3});
        zaos.closeArchiveEntry();
        zaos.close();

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof ZipArchiveInputStream);
        ais.close();
    }

    @Test
    public void testAutodetectAr() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArArchiveOutputStream aaos = new ArArchiveOutputStream(baos);
        aaos.putArchiveEntry(new ArArchiveEntry("entry.txt", 3));
        aaos.write(new byte[]{1, 2, 3});
        aaos.closeArchiveEntry();
        aaos.close();

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof ArArchiveInputStream);
        ais.close();
    }

    @Test
    public void testAutodetectCpio() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream caos = new CpioArchiveOutputStream(baos);
        caos.putArchiveEntry(new CpioArchiveEntry("entry.txt", 3));
        caos.write(new byte[]{1, 2, 3});
        caos.closeArchiveEntry();
        caos.close();

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof CpioArchiveInputStream);
        ais.close();
    }

    @Test
    public void testAutodetectTar() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("entry.txt");
        entry.setSize(3);
        taos.putArchiveEntry(entry);
        taos.write(new byte[]{1, 2, 3});
        taos.closeArchiveEntry();
        taos.close();

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof TarArchiveInputStream);
        ais.close();
    }

    @Test
    public void testAutodetectDump() throws Exception {
        byte[] buffer = new byte[1024];
        buffer[24] = (byte) 0x6B;
        buffer[25] = (byte) 0xEA;
        buffer[26] = 0;
        buffer[27] = 0;

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(buffer));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof DumpArchiveInputStream);
        ais.close();
    }

    @Test
    public void testAutodetectTarFallback() throws Exception {
        byte[] header = new byte[512];
        byte[] name = "fallback.txt".getBytes("US-ASCII");
        System.arraycopy(name, 0, header, 0, name.length);

        byte[] size = "00000000000 ".getBytes("US-ASCII");
        System.arraycopy(size, 0, header, 124, size.length);

        for (int i = 0; i < 8; i++) {
            header[148 + i] = ' ';
        }

        long sum = 0;
        for (byte b : header) {
            sum += (b & 0xFF);
        }

        String checkSumStr = String.format("%06o\0 ", sum);
        byte[] csBytes = checkSumStr.getBytes("US-ASCII");
        System.arraycopy(csBytes, 0, header, 148, Math.min(csBytes.length, 8));

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(header));
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        Assert.assertNotNull(ais);
        Assert.assertTrue(ais instanceof TarArchiveInputStream);
        ais.close();
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectIOExceptionHandling() throws Exception {
        InputStream failingStream = new InputStream() {
            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public synchronized void mark(int readlimit) {
            }

            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated Reset Exception");
            }

            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return len;
            }
        };

        factory.createArchiveInputStream(failingStream);
    }
}
