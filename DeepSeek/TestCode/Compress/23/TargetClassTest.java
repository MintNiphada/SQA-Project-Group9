package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import org.junit.Test;

public class CodersTest {

    @Test
    public void testAddDecoderCopy() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.COPY.getId();
        InputStream result = Coders.addDecoder(in, coder, null);
        assertSame(in, result);
    }

    @Test
    public void testAddDecoderLZMAValid() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        byte[] props = new byte[5];
        props[0] = 0x5d;
        props[1] = 0x00;
        props[2] = 0x00;
        props[3] = 0x00;
        props[4] = 0x00;
        coder.properties = props;
        InputStream result = Coders.addDecoder(in, coder, null);
        assertTrue(result instanceof LZMAInputStream);
    }

    @Test(expected = IOException.class)
    public void testAddDecoderLZMADictTooLarge() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        byte[] props = new byte[5];
        props[0] = 0x5d;
        props[1] = (byte) 0xFF;
        props[2] = (byte) 0xFF;
        props[3] = (byte) 0xFF;
        props[4] = (byte) 0xFF;
        coder.properties = props;
        Coders.addDecoder(in, coder, null);
    }

    @Test
    public void testAddDecoderLZMA2() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA2.getId();
        coder.properties = new byte[1];
        InputStream result = Coders.addDecoder(in, coder, null);
        assertNotNull(result);
    }

    @Test
    public void testAddDecoderDeflate() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.DEFLATE.getId();
        InputStream result = Coders.addDecoder(in, coder, null);
        assertTrue(result instanceof InflaterInputStream);
    }

    @Test
    public void testAddDecoderBzip2() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.BZIP2.getId();
        InputStream result = Coders.addDecoder(in, coder, null);
        assertTrue(result instanceof BZip2CompressorInputStream);
    }

    @Test(expected = IOException.class)
    public void testAddDecoderAesNullPassword() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[2];
        Coders.addDecoder(in, coder, null);
    }

    @Test(expected = IOException.class)
    public void testAddDecoderAesPropertiesTooShort() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[2];
        Coders.addDecoder(in, coder, "password".getBytes());
    }

    @Test
    public void testAddDecoderAesSpecialKeyDerivation() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[16]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        byte[] props = new byte[34];
        props[0] = (byte) 0x3f;
        props[1] = 0x00;
        coder.properties = props;
        InputStream result = Coders.addDecoder(in, coder, "password".getBytes());
        assertNotNull(result);
        try {
            result.read();
            fail("Expected IOException due to invalid ciphertext");
        } catch (IOException e) {
        }
    }

    @Test
    public void testAddDecoderAesNormalKeyDerivation() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[16]);
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        byte[] props = new byte[34];
        props[0] = 0x01;
        props[1] = 0x10;
        coder.properties = props;
        InputStream result = Coders.addDecoder(in, coder, "password".getBytes());
        assertNotNull(result);
        try {
            result.read();
            fail("Expected IOException due to invalid ciphertext");
        } catch (IOException e) {
        }
    }

    @Test(expected = IOException.class)
    public void testAddDecoderUnsupportedMethod() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Coder coder = new Coder();
        coder.decompressionMethodId = new byte[] { 0x00, 0x01, 0x02 };
        Coders.addDecoder(in, coder, null);
    }

    @Test
    public void testAddEncoderCopy() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(out, SevenZMethod.COPY, null);
        assertSame(out, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddEncoderLzma() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, SevenZMethod.LZMA, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddEncoderLzma2() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, SevenZMethod.LZMA2, null);
    }

    @Test
    public void testAddEncoderDeflate() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(out, SevenZMethod.DEFLATE, null);
        assertTrue(result instanceof DeflaterOutputStream);
    }

    @Test
    public void testAddEncoderBzip2() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        OutputStream result = Coders.addEncoder(out, SevenZMethod.BZIP2, null);
        assertTrue(result instanceof BZip2CompressorOutputStream);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddEncoderAes() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, SevenZMethod.AES256SHA256, null);
    }

    @Test(expected = IOException.class)
    public void testAddEncoderUnsupportedMethod() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        Coders.addEncoder(out, null, null);
    }

    @Test
    public void testDummyByteAddingInputStreamReadNormal() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        InputStream stream = new Coders.DummyByteAddingInputStream(bais);
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
    }

    @Test
    public void testDummyByteAddingInputStreamReadAddsDummy() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        InputStream stream = new Coders.DummyByteAddingInputStream(bais);
        assertEquals(0, stream.read());
        assertEquals(-1, stream.read());
    }

    @Test
    public void testDummyByteAddingInputStreamReadArrayNormal() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        InputStream stream = new Coders.DummyByteAddingInputStream(bais);
        byte[] buf = new byte[5];
        int len = stream.read(buf, 0, 5);
        assertEquals(3, len);
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0 }, buf);
    }

    @Test
    public void testDummyByteAddingInputStreamReadArrayAddsDummy() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        InputStream stream = new Coders.DummyByteAddingInputStream(bais);
        byte[] buf = new byte[5];
        int len = stream.read(buf, 0, 5);
        assertEquals(1, len);
        assertEquals(0, buf[0]);
        len = stream.read(buf, 0, 5);
        assertEquals(-1, len);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCoderBaseEncodeThrows() throws IOException {
        Coders.CoderBase base = new Coders.CoderBase() {
            @Override
            InputStream decode(InputStream in, Coder coder, byte[] password) throws IOException {
                return null;
            }
        };
        base.encode(new ByteArrayOutputStream(), null);
    }
}
