package org.apache.commons.compress.archivers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private static final ArchiveStreamFactory FACTORY = new ArchiveStreamFactory();

    // --- createArchiveInputStream(String, InputStream) tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInputStreamNullArchiverName() throws ArchiveException {
        FACTORY.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInputStreamNullStream() throws ArchiveException {
        FACTORY.createArchiveInputStream("zip", null);
    }

    @Test
    public void testCreateInputStreamAr() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("ar", in) instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamZip() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("zip", in) instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamTar() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("tar", in) instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamJar() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("jar", in) instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamCpio() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("cpio", in) instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamDump() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("dump", in) instanceof DumpArchiveInputStream);
    }

    @Test
    public void testCreateInputStreamCaseInsensitive() throws ArchiveException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertTrue(FACTORY.createArchiveInputStream("ZIP", in) instanceof ZipArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateInputStreamUnknownArchiver() throws ArchiveException {
        FACTORY.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    // --- createArchiveOutputStream(String, OutputStream) tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOutputStreamNullArchiverName() throws ArchiveException {
        FACTORY.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOutputStreamNullStream() throws ArchiveException {
        FACTORY.createArchiveOutputStream("zip", null);
    }

    @Test
    public void testCreateOutputStreamAr() throws ArchiveException {
        OutputStream out = new ByteArrayOutputStream();
        assertTrue(FACTORY.createArchiveOutputStream("ar", out) instanceof ArArchiveOutputStream);
    }

    @Test
    public void testCreateOutputStreamZip() throws ArchiveException {
        OutputStream out = new ByteArrayOutputStream();
        assertTrue(FACTORY.createArchiveOutputStream("zip", out) instanceof ZipArchiveOutputStream);
    }

    @Test
    public void testCreateOutputStreamTar() throws ArchiveException {
        OutputStream out = new ByteArrayOutputStream();
        assertTrue(FACTORY.createArchiveOutputStream("tar", out) instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateOutputStreamJar() throws ArchiveException {
        OutputStream out = new ByteArrayOutputStream();
        assertTrue(FACTORY.createArchiveOutputStream("jar", out) instanceof JarArchiveOutputStream);
    }

    @Test
    public void testCreateOutputStreamCpio() throws ArchiveException {
        OutputStream out = new ByteArrayOutputStream();
        assertTrue(FACTORY.createArchiveOutputStream("cpio", out) instanceof CpioArchiveOutputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateOutputStreamDump() throws ArchiveException {
        FACTORY.createArchiveOutputStream("dump", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateOutputStreamUnknownArchiver() throws ArchiveException {
        FACTORY.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    // --- createArchiveInputStream(InputStream) auto‑detection tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testAutoDetectNullStream() throws ArchiveException {
        FACTORY.createArchiveInputStream(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAutoDetectNoMarkSupport() throws ArchiveException {
        InputStream noMark = new InputStream() {
            @Override
            public int read() throws IOException { return -1; }
            @Override
            public boolean markSupported() { return false; }
        };
        FACTORY.createArchiveInputStream(noMark);
    }

    @Test
    public void testAutoDetectZipSignature() throws ArchiveException {
        byte[] zipSig = new byte[] { 0x50, 0x4B, 0x03, 0x04, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 };
        InputStream in = new ByteArrayInputStream(zipSig);
        assertTrue(FACTORY.createArchiveInputStream(in) instanceof ZipArchiveInputStream);
    }

    @Test
    public void testAutoDetectArSignature() throws ArchiveException {
        byte[] arSig = "!<arch>\n".getBytes();
        InputStream in = new ByteArrayInputStream(arSig);
        assertTrue(FACTORY.createArchiveInputStream(in) instanceof ArArchiveInputStream);
    }

    @Test
    public void testAutoDetectCpioSignature() throws ArchiveException {
        byte[] cpioSig = "070701".getBytes(); // new binary CPIO magic
        InputStream in = new ByteArrayInputStream(cpioSig);
        assertTrue(FACTORY.createArchiveInputStream(in) instanceof CpioArchiveInputStream);
    }

    @Test
    public void testAutoDetectDumpSignature() throws ArchiveException {
        byte[] dumpSig = new byte[32];
        dumpSig[0] = (byte) 0xE5; // dump magic first byte
        // rest remain zero
        InputStream in = new ByteArrayInputStream(dumpSig);
        assertTrue(FACTORY.createArchiveInputStream(in) instanceof DumpArchiveInputStream);
    }

    @Test
    public void testAutoDetectTarSignature() throws ArchiveException {
        byte[] tarHeader = createValidTarHeader();
        InputStream in = new ByteArrayInputStream(tarHeader);
        assertTrue(FACTORY.createArchiveInputStream(in) instanceof TarArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testAutoDetectNoMatchSmallStream() throws ArchiveException {
        byte[] garbage = new byte[20]; // less than 512, none match
        InputStream in = new ByteArrayInputStream(garbage);
        FACTORY.createArchiveInputStream(in);
    }

    @Test(expected = ArchiveException.class)
    public void testAutoDetectNoMatchLargeStream() throws ArchiveException {
        byte[] garbage = new byte[600]; // large enough, invalid data
        InputStream in = new ByteArrayInputStream(garbage);
        FACTORY.createArchiveInputStream(in);
    }

    @Test(expected = ArchiveException.class)
    public void testAutoDetectIOExceptionOnMarkReset() throws ArchiveException {
        InputStream broken = new ByteArrayInputStream(new byte[12]) {
            private boolean throwOnReset = true;
            @Override
            public synchronized void reset() throws IOException {
                if (throwOnReset) {
                    throwOnReset = false;
                    throw new IOException("mark/reset failure");
                }
                super.reset();
            }
        };
        FACTORY.createArchiveInputStream(broken);
    }

    // helper to create a minimal valid tar header (512 bytes)
    private byte[] createValidTarHeader() {
        byte[] header = new byte[512];
        // name at offset 0, up to 100. Use "a"
        header[0] = 'a';
        // mode at offset 100 (8 bytes): "000644 "
        System.arraycopy("000644 ".getBytes(), 0, header, 100, 7);
        // uid, gid: leave as zero spaces
        // size (12 bytes) offset 124: "00000000000 "
        System.arraycopy("00000000000 ".getBytes(), 0, header, 124, 12);
        // mtime (12 bytes) offset 136: zeros
        // checksum (8 bytes) offset 148: initially spaces ' '
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        // typeflag offset 156: '0' normal file
        header[156] = '0';
        // linkname: not used
        // magic at offset 257: "ustar "
        System.arraycopy("ustar ".getBytes(), 0, header, 257, 6);
        // version at offset 263: "00"
        System.arraycopy("00".getBytes(), 0, header, 263, 2);

        // compute unsigned checksum of the entire 512-byte block
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (header[i] & 0xff);
        }
        String checksumStr = Long.toString(sum, 8); // octal
        // pad left with zeros to 6 digits + null + space? The tar spec expects 6 octal digits,
        // a null byte, and a space. Common implementations fill 6 digits + space.
        checksumStr = ("000000" + checksumStr).substring(checksumStr.length()) + " ";
        byte[] checksumBytes = checksumStr.getBytes();
        System.arraycopy(checksumBytes, 0, header, 148, 7); // 6 digits + space
        header[155] = ' '; // just in case

        return header;
    }
}
