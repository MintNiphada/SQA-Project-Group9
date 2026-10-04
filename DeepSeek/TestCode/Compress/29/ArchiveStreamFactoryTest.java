package org.apache.commons.compress.archivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private static final String ENCODING = "UTF-8";

    @Test
    public void testDefaultConstructor() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertEquals(null, factory.getEntryEncoding());
    }

    @Test
    public void testConstructorWithEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        assertEquals(ENCODING, factory.getEntryEncoding());
    }

    @Test
    public void testGetEntryEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        assertEquals(ENCODING, factory.getEntryEncoding());
    }

    @Test
    public void testSetEntryEncodingWithNullConstructorEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.setEntryEncoding(ENCODING);
        assertEquals(ENCODING, factory.getEntryEncoding());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetEntryEncodingWithNonNullConstructorEncodingThrows() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        factory.setEntryEncoding("ISO-8859-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullArchiverName() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullStream() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveInputStream("zip", null);
    }

    @Test
    public void testCreateArchiveInputStreamAr() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("ar", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamArj() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("arj", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof ArjArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamArjWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("arj", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof ArjArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamZip() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("zip", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamZipWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("zip", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamTar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("tar", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamTarWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("tar", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamJar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("jar", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamJarWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("jar", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamCpio() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("cpio", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamCpioWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("cpio", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamDump() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream("dump", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof DumpArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamDumpWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveInputStream in = factory.createArchiveInputStream("dump", new ByteArrayInputStream(new byte[0]));
        assertTrue(in instanceof DumpArchiveInputStream);
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveInputStreamSevenZ() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveInputStream("7z", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknown() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullArchiverName() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullStream() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveOutputStream("zip", null);
    }

    @Test
    public void testCreateArchiveOutputStreamAr() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream out = factory.createArchiveOutputStream("ar", new ByteArrayOutputStream());
        assertTrue(out instanceof ArArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamZip() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream out = factory.createArchiveOutputStream("zip", new ByteArrayOutputStream());
        assertTrue(out instanceof ZipArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamZipWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveOutputStream out = factory.createArchiveOutputStream("zip", new ByteArrayOutputStream());
        assertTrue(out instanceof ZipArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamTar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream out = factory.createArchiveOutputStream("tar", new ByteArrayOutputStream());
        assertTrue(out instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamTarWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveOutputStream out = factory.createArchiveOutputStream("tar", new ByteArrayOutputStream());
        assertTrue(out instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamJar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream out = factory.createArchiveOutputStream("jar", new ByteArrayOutputStream());
        assertTrue(out instanceof JarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamCpio() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream out = factory.createArchiveOutputStream("cpio", new ByteArrayOutputStream());
        assertTrue(out instanceof CpioArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamCpioWithEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        ArchiveOutputStream out = factory.createArchiveOutputStream("cpio", new ByteArrayOutputStream());
        assertTrue(out instanceof CpioArchiveOutputStream);
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testCreateArchiveOutputStreamSevenZ() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveOutputStream("7z", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamUnknown() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDetectNullStream() throws ArchiveException {
        new ArchiveStreamFactory().createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDetectMarkNotSupported() throws ArchiveException {
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new ArchiveStreamFactory().createArchiveInputStream(in);
    }

    @Test
    public void testDetectZip() throws ArchiveException {
        byte[] signature = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof ZipArchiveInputStream);
    }

    @Test
    public void testDetectAr() throws ArchiveException {
        byte[] signature = new byte[] { 0x21, 0x3C, 0x61, 0x72, 0x63, 0x68, 0x3E, 0x0A };
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof ArArchiveInputStream);
    }

    @Test
    public void testDetectCpio() throws ArchiveException {
        byte[] signature = new byte[] { 0x30, 0x37, 0x30, 0x37, 0x30, 0x31 };
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof CpioArchiveInputStream);
    }

    @Test
    public void testDetectArj() throws ArchiveException {
        byte[] signature = new byte[] { 0x60, (byte) 0xEA };
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof ArjArchiveInputStream);
    }

    @Test(expected = StreamingNotSupportedException.class)
    public void testDetectSevenZ() throws ArchiveException {
        byte[] signature = new byte[] { 0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C };
        InputStream in = new ByteArrayInputStream(signature);
        new ArchiveStreamFactory().createArchiveInputStream(in);
    }

    @Test
    public void testDetectDump() throws ArchiveException {
        byte[] signature = new byte[32];
        signature[24] = 0x00;
        signature[25] = 0x00;
        signature[26] = (byte) 0xEA;
        signature[27] = 0x6C;
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof DumpArchiveInputStream);
    }

    @Test
    public void testDetectTarValid() throws ArchiveException {
        byte[] header = createValidTarHeader();
        InputStream in = new ByteArrayInputStream(header);
        ArchiveInputStream result = new ArchiveStreamFactory().createArchiveInputStream(in);
        assertTrue(result instanceof TarArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testDetectTarInvalidChecksum() throws ArchiveException {
        byte[] header = createInvalidTarHeader();
        InputStream in = new ByteArrayInputStream(header);
        new ArchiveStreamFactory().createArchiveInputStream(in);
    }

    @Test(expected = ArchiveException.class)
    public void testDetectNoMatch() throws ArchiveException {
        byte[] data = new byte[512];
        InputStream in = new ByteArrayInputStream(data);
        new ArchiveStreamFactory().createArchiveInputStream(in);
    }

    @Test(expected = ArchiveException.class)
    public void testDetectIOException() throws ArchiveException {
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("test");
            }
            @Override
            public boolean markSupported() {
                return true;
            }
            @Override
            public synchronized void mark(int readlimit) {}
            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("test");
            }
        };
        new ArchiveStreamFactory().createArchiveInputStream(in);
    }

    @Test
    public void testDetectEncoding() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory(ENCODING);
        byte[] signature = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
        InputStream in = new ByteArrayInputStream(signature);
        ArchiveInputStream result = factory.createArchiveInputStream(in);
        assertTrue(result instanceof ZipArchiveInputStream);
    }

    private byte[] createValidTarHeader() {
        byte[] header = new byte[512];
        String name = "test.txt";
        System.arraycopy(name.getBytes(), 0, header, 0, name.length());
        String mode = "0000644";
        System.arraycopy(mode.getBytes(), 0, header, 100, mode.length());
        header[107] = 0;
        String uid = "0000000";
        System.arraycopy(uid.getBytes(), 0, header, 108, uid.length());
        header[115] = 0;
        String gid = "0000000";
        System.arraycopy(gid.getBytes(), 0, header, 116, gid.length());
        header[123] = 0;
        String size = "00000000000";
        System.arraycopy(size.getBytes(), 0, header, 124, size.length());
        header[135] = 0;
        String mtime = "00000000000";
        System.arraycopy(mtime.getBytes(), 0, header, 136, mtime.length());
        header[147] = 0;
        for (int i = 148; i < 156; i++) {
            header[i] = (byte) ' ';
        }
        header[156] = '0';
        String magic = "ustar ";
        System.arraycopy(magic.getBytes(), 0, header, 257, magic.length());
        String version = "00";
        System.arraycopy(version.getBytes(), 0, header, 263, version.length());
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (header[i] & 0xFF);
        }
        String chksum = String.format("%06o", sum);
        byte[] chksumBytes = new byte[8];
        System.arraycopy(chksum.getBytes(), 0, chksumBytes, 0, chksum.length());
        chksumBytes[6] = 0;
        chksumBytes[7] = ' ';
        System.arraycopy(chksumBytes, 0, header, 148, 8);
        return header;
    }

    private byte[] createInvalidTarHeader() {
        byte[] header = createValidTarHeader();
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = 0;
        header[155] = ' ';
        return header;
    }
}
