package org.apache.commons.compress.compressors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;
import org.junit.Test;

public class CompressorStreamFactoryTest {

    @Test
    public void testDefaultConstructor() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        assertFalse(factory.getDecompressConcatenated());
        factory.setDecompressConcatenated(true);
        assertTrue(factory.getDecompressConcatenated());
        factory.setDecompressConcatenated(false);
        assertFalse(factory.getDecompressConcatenated());
    }

    @Test
    public void testParameterizedConstructor() {
        CompressorStreamFactory factoryTrue = new CompressorStreamFactory(true);
        assertTrue(factoryTrue.getDecompressConcatenated());

        CompressorStreamFactory factoryFalse = new CompressorStreamFactory(false);
        assertFalse(factoryFalse.getDecompressConcatenated());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDecompressConcatenatedThrowsWhenConstructorUsedTrue() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        factory.setDecompressConcatenated(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDecompressConcatenatedThrowsWhenConstructorUsedFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory(false);
        factory.setDecompressConcatenated(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamUnmarkedStream() throws Exception {
        InputStream unmarkable = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CompressorStreamFactory().createCompressorInputStream(unmarkable);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamUnknownSignature() throws Exception {
        byte[] dummy = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(dummy));
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamEmptyStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamIOExceptionOnRead() throws Exception {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read error");
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CompressorStreamFactory().createCompressorInputStream(broken);
    }

    @Test
    public void testAutodetectBzip2() throws Exception {
        byte[] data = new byte[] { 'B', 'Z', 'h', '9', '1', 'A', 'Y', '&', 'S', 'Y', 0, 0 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(data));
        assertNotNull(in);
        assertTrue(in instanceof BZip2CompressorInputStream);
        in.close();
    }

    @Test
    public void testAutodetectGzip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzOut = new GZIPOutputStream(baos);
        gzOut.write("test".getBytes());
        gzOut.close();

        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(in);
        assertTrue(in instanceof GzipCompressorInputStream);
        in.close();
    }

    @Test
    public void testAutodetectPack200() throws Exception {
        byte[] header = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xD0, (byte) 0x0D, 0, 0, 0, 0, 0, 0, 0, 0 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(header));
        assertNotNull(in);
        assertTrue(in instanceof Pack200CompressorInputStream);
        in.close();
    }

    @Test
    public void testAutodetectFramedSnappy() throws Exception {
        byte[] header = new byte[] { (byte) 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59, 0, 0 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(header));
        assertNotNull(in);
        assertTrue(in instanceof FramedSnappyCompressorInputStream);
        in.close();
    }

    @Test
    public void testAutodetectZ() throws Exception {
        byte[] header = new byte[] { 0x1f, (byte) 0x9d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(header));
        assertNotNull(in);
        assertTrue(in instanceof ZCompressorInputStream);
        in.close();
    }

    @Test
    public void testAutodetectXZ() throws Exception {
        if (XZUtils.isXZCompressionAvailable()) {
            byte[] header = new byte[] { (byte) 0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00, 0, 0, 0, 0, 0, 0 };
            CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(header));
            assertNotNull(in);
            assertTrue(in instanceof XZCompressorInputStream);
            in.close();
        }
    }

    @Test
    public void testAutodetectLZMA() throws Exception {
        if (LZMAUtils.isLZMACompressionAvailable()) {
            byte[] header = new byte[] { 0x5d, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 };
            CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(header));
            assertNotNull(in);
            assertTrue(in instanceof LZMACompressorInputStream);
            in.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamByNameNullName() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamByNameNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamByNameUnknownName() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream("unknown-format", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateCompressorInputStreamByNameGzip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzOut = new GZIPOutputStream(baos);
        gzOut.write("hello".getBytes());
        gzOut.close();

        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(in);
        assertTrue(in instanceof GzipCompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameBzip2() throws Exception {
        byte[] data = new byte[] { 'B', 'Z', 'h', '9', '1', 'A', 'Y', '&', 'S', 'Y', 0, 0 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BZIP2, new ByteArrayInputStream(data));
        assertNotNull(in);
        assertTrue(in instanceof BZip2CompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameXZ() throws Exception {
        if (XZUtils.isXZCompressionAvailable()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XZCompressorOutputStream xzOut = new XZCompressorOutputStream(baos);
            xzOut.write("xz test".getBytes());
            xzOut.close();

            CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.XZ, new ByteArrayInputStream(baos.toByteArray()));
            assertNotNull(in);
            assertTrue(in instanceof XZCompressorInputStream);
            in.close();
        }
    }

    @Test
    public void testCreateCompressorInputStreamByNamePack200() throws Exception {
        byte[] data = new byte[0];
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.PACK200, new ByteArrayInputStream(data));
        assertNotNull(in);
        assertTrue(in instanceof Pack200CompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameSnappyRaw() throws Exception {
        byte[] data = new byte[] { 4, 0x10, 't', 'e', 's', 't' };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.SNAPPY_RAW, new ByteArrayInputStream(data));
        assertNotNull(in);
        assertTrue(in instanceof SnappyCompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameSnappyFramed() throws Exception {
        byte[] header = new byte[] { (byte) 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED, new ByteArrayInputStream(header));
        assertNotNull(in);
        assertTrue(in instanceof FramedSnappyCompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameZ() throws Exception {
        byte[] header = new byte[] { 0x1f, (byte) 0x9d, (byte) 0x90 };
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.Z, new ByteArrayInputStream(header));
        assertNotNull(in);
        assertTrue(in instanceof ZCompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameDeflate() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream defOut = new DeflaterOutputStream(baos);
        defOut.write("deflate test".getBytes());
        defOut.close();

        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.DEFLATE, new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(in);
        assertTrue(in instanceof DeflateCompressorInputStream);
        in.close();
    }

    @Test
    public void testCreateCompressorInputStreamByNameLZMA() throws Exception {
        if (LZMAUtils.isLZMACompressionAvailable()) {
            byte[] header = new byte[] { 0x5d, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.LZMA, new ByteArrayInputStream(header));
            assertNotNull(in);
            assertTrue(in instanceof LZMACompressorInputStream);
            in.close();
        }
    }

    @Test
    public void testCreateCompressorInputStreamByNameIOException() {
        InputStream faulty = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Stream failed");
            }
        };
        try {
            new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, faulty);
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullName() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnknownName() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream("unknown-format", new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnsupportedFormats() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.LZMA, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnsupportedSnappyRaw() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.SNAPPY_RAW, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnsupportedSnappyFramed() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.SNAPPY_FRAMED, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnsupportedZ() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.Z, new ByteArrayOutputStream());
    }

    @Test
    public void testCreateCompressorOutputStreamGzip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, baos);
        assertNotNull(out);
        assertTrue(out instanceof GzipCompressorOutputStream);
        out.close();
    }

    @Test
    public void testCreateCompressorOutputStreamBzip2() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.BZIP2, baos);
        assertNotNull(out);
        assertTrue(out instanceof BZip2CompressorOutputStream);
        out.close();
    }

    @Test
    public void testCreateCompressorOutputStreamPack200() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.PACK200, baos);
        assertNotNull(out);
        assertTrue(out instanceof Pack200CompressorOutputStream);
        out.close();
    }

    @Test
    public void testCreateCompressorOutputStreamDeflate() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.DEFLATE, baos);
        assertNotNull(out);
        assertTrue(out instanceof DeflateCompressorOutputStream);
        out.close();
    }

    @Test
    public void testCreateCompressorOutputStreamXZ() throws Exception {
        if (XZUtils.isXZCompressionAvailable()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.XZ, baos);
            assertNotNull(out);
            assertTrue(out instanceof XZCompressorOutputStream);
            out.close();
        }
    }

    @Test
    public void testConstants() {
        assertEquals("bzip2", CompressorStreamFactory.BZIP2);
        assertEquals("gz", CompressorStreamFactory.GZIP);
        assertEquals("pack200", CompressorStreamFactory.PACK200);
        assertEquals("xz", CompressorStreamFactory.XZ);
        assertEquals("lzma", CompressorStreamFactory.LZMA);
        assertEquals("snappy-framed", CompressorStreamFactory.SNAPPY_FRAMED);
        assertEquals("snappy-raw", CompressorStreamFactory.SNAPPY_RAW);
        assertEquals("z", CompressorStreamFactory.Z);
        assertEquals("deflate", CompressorStreamFactory.DEFLATE);
    }
}
