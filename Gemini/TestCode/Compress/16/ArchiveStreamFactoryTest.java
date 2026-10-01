package org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
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
    public void testCreateArchiveInputStreamNullInputStream() throws Exception {
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknownArchiver() throws Exception {
        factory.createArchiveInputStream("unknown_archiver", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStreamAllTypes() throws Exception {
        byte[] empty = new byte[0];

        ArchiveInputStream arIn = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(empty));
        Assert.assertTrue(arIn instanceof ArArchiveInputStream);
        arIn.close();

        ArchiveInputStream arInUpper = factory.createArchiveInputStream(ArchiveStreamFactory.AR.toUpperCase(Locale.ENGLISH), new ByteArrayInputStream(empty));
        Assert.assertTrue(arInUpper instanceof ArArchiveInputStream);
        arInUpper.close();

        ArchiveInputStream zipIn = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(empty));
        Assert.assertTrue(zipIn instanceof ZipArchiveInputStream);
        zipIn.close();

        ArchiveInputStream tarIn = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(empty));
        Assert.assertTrue(tarIn instanceof TarArchiveInputStream);
        tarIn.close();

        ArchiveInputStream jarIn = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(empty));
        Assert.assertTrue(jarIn instanceof JarArchiveInputStream);
        jarIn.close();

        ArchiveInputStream cpioIn = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(empty));
        Assert.assertTrue(cpioIn instanceof CpioArchiveInputStream);
        cpioIn.close();

        ArchiveInputStream dumpIn = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(empty));
        Assert.assertTrue(dumpIn instanceof DumpArchiveInputStream);
        dumpIn.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullArchiverName() throws Exception {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullOutputStream() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamUnknownArchiver() throws Exception {
        factory.createArchiveOutputStream("unknown_archiver", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamDumpNotSupported() throws Exception {
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStreamAllTypes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ArchiveOutputStream arOut = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, out);
        Assert.assertTrue(arOut instanceof ArArchiveOutputStream);
        arOut.close();

        ArchiveOutputStream arOutUpper = factory.createArchiveOutputStream(ArchiveStreamFactory.AR.toUpperCase(Locale.ENGLISH), out);
        Assert.assertTrue(arOutUpper instanceof ArArchiveOutputStream);
        arOutUpper.close();

        ArchiveOutputStream zipOut = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
        Assert.assertTrue(zipOut instanceof ZipArchiveOutputStream);
        zipOut.close();

        ArchiveOutputStream tarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, out);
        Assert.assertTrue(tarOut instanceof TarArchiveOutputStream);
        tarOut.close();

        ArchiveOutputStream jarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out);
        Assert.assertTrue(jarOut instanceof JarArchiveOutputStream);
        jarOut.close();

        ArchiveOutputStream cpioOut = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, out);
        Assert.assertTrue(cpioOut instanceof CpioArchiveOutputStream);
        cpioOut.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectNullStream() throws Exception {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutodetectNonMarkSupportedStream() throws Exception {
        InputStream nonMarkStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(nonMarkStream);
    }

    @Test
    public void testAutodetectZip() throws Exception {
        byte[] zipSignature = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(zipSignature));
        Assert.assertTrue(in instanceof ZipArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectAr() throws Exception {
        byte[] arSignature = new byte[]{'!', '<', 'a', 'r', 'c', 'h', '>', '\n'};
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(arSignature));
        Assert.assertTrue(in instanceof ArArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectCpio() throws Exception {
        byte[] cpioMagic = "070701".getBytes("US-ASCII");
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioMagic));
        Assert.assertTrue(in instanceof CpioArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectDump() throws Exception {
        byte[] dumpSig = new byte[32];
        int magic = 60012;
        dumpSig[24] = (byte) (magic & 0xFF);
        dumpSig[25] = (byte) ((magic >> 8) & 0xFF);
        dumpSig[26] = (byte) ((magic >> 16) & 0xFF);
        dumpSig[27] = (byte) ((magic >> 24) & 0xFF);

        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(dumpSig));
        Assert.assertTrue(in instanceof DumpArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectTarWithMagic() throws Exception {
        byte[] tarHeader = new byte[512];
        byte[] ustarMagic = "ustar\0".getBytes("US-ASCII");
        System.arraycopy(ustarMagic, 0, tarHeader, 257, ustarMagic.length);

        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(tarHeader));
        Assert.assertTrue(in instanceof TarArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectTarValidEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] tarData = baos.toByteArray();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertTrue(in instanceof TarArchiveInputStream);
        in.close();
    }

    @Test
    public void testAutodetectTarInvalidBlockFallback() {
        byte[] dummyTarBlock = new byte[512];
        for (int i = 0; i < dummyTarBlock.length; i++) {
            dummyTarBlock[i] = (byte) 0x7F;
        }

        try {
            factory.createArchiveInputStream(new ByteArrayInputStream(dummyTarBlock));
            Assert.fail("Expected ArchiveException for corrupted TAR block");
        } catch (ArchiveException e) {
            Assert.assertTrue(e.getMessage().contains("No Archiver found"));
        }
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectUnknownShortStream() throws Exception {
        byte[] shortData = new byte[]{0x01, 0x02, 0x03};
        factory.createArchiveInputStream(new ByteArrayInputStream(shortData));
    }

    @Test(expected = ArchiveException.class)
    public void testAutodetectEmptyStream() throws Exception {
        factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testAutodetectIOExceptionOnReset() {
        InputStream failingStream = new BufferedInputStream(new ByteArrayInputStream(new byte[100])) {
            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated reset failure");
            }
        };

        try {
            factory.createArchiveInputStream(failingStream);
            Assert.fail("Expected ArchiveException on IOException during reset");
        } catch (ArchiveException e) {
            Assert.assertEquals("Could not use reset and mark operations.", e.getMessage());
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getCause() instanceof IOException);
        }
    }
}
