package org.apache.commons.compress.archivers.sevenz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

public class CodersTest {

    @Test
    public void testInstantiateCodersClass() {
        Coders coders = new Coders();
        Assert.assertNotNull(coders);
    }

    @Test
    public void testCoderTableIntegrity() {
        Assert.assertNotNull(Coders.coderTable);
        Assert.assertTrue(Coders.coderTable.length >= 6);
        for (Coders.CoderId coderId : Coders.coderTable) {
            Assert.assertNotNull(coderId.method);
            Assert.assertNotNull(coderId.coder);
        }
    }

    @Test
    public void testCoderIdConstructor() {
        Coders.CoderBase base = new Coders.CopyDecoder();
        Coders.CoderId coderId = new Coders.CoderId(SevenZMethod.COPY, base);
        Assert.assertEquals(SevenZMethod.COPY, coderId.method);
        Assert.assertSame(base, coderId.coder);
    }

    @Test
    public void testAddDecoderUnsupportedMethod() {
        Coder coder = new Coder();
        coder.decompressionMethodId = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD };
        try {
            Coders.addDecoder(new ByteArrayInputStream(new byte[0]), coder, null);
            Assert.fail("Expected IOException for unsupported compression method");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported compression method"));
        }
    }

    @Test
    public void testAddEncoderUnsupportedMethod() {
        try {
            // Passing a null method will not match any coderId.method.equals(null)
            Coders.addEncoder(new ByteArrayOutputStream(), null, null);
            Assert.fail("Expected IOException for unsupported compression method");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported compression method"));
        }
    }

    @Test
    public void testCoderBaseDefaultEncodeThrowsUnsupportedOperationException() throws IOException {
        Coders.CoderBase base = new Coders.LZMADecoder();
        try {
            base.encode(new ByteArrayOutputStream(), null);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("method doesn't support writing", e.getMessage());
        }

        Coders.CoderBase aesBase = new Coders.AES256SHA256Decoder();
        try {
            aesBase.encode(new ByteArrayOutputStream(), null);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("method doesn't support writing", e.getMessage());
        }
    }

    @Test
    public void testCopyCoder() throws IOException {
        byte[] data = "Hello COPY Stream".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoder = Coders.addEncoder(baos, SevenZMethod.COPY, null);
        Assert.assertSame(baos, encoder);
        encoder.write(data);
        encoder.close();

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.COPY.getId();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream decoder = Coders.addDecoder(bais, coder, null);
        Assert.assertSame(bais, decoder);

        byte[] result = new byte[data.length];
        int read = decoder.read(result);
        Assert.assertEquals(data.length, read);
        Assert.assertArrayEquals(data, result);
        decoder.close();
    }

    @Test
    public void testDeflateCoder() throws IOException {
        byte[] original = "Deflate Compression Test String 1234567890".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = Coders.addEncoder(baos, SevenZMethod.DEFLATE, null);
        out.write(original);
        out.close();

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.DEFLATE.getId();
        InputStream in = Coders.addDecoder(new ByteArrayInputStream(baos.toByteArray()), coder, null);

        ByteArrayOutputStream decodedBaos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8];
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) != -1) {
            decodedBaos.write(buffer, 0, read);
        }
        in.close();

        Assert.assertArrayEquals(original, decodedBaos.toByteArray());
    }

    @Test
    public void testDeflateCoderReadSingleByte() throws IOException {
        byte[] original = "Single Byte Test".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = Coders.addEncoder(baos, SevenZMethod.DEFLATE, null);
        out.write(original);
        out.close();

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.DEFLATE.getId();
        InputStream in = Coders.addDecoder(new ByteArrayInputStream(baos.toByteArray()), coder, null);

        ByteArrayOutputStream decodedBaos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            decodedBaos.write(b);
        }
        in.close();

        Assert.assertArrayEquals(original, decodedBaos.toByteArray());
    }

    @Test
    public void testBZip2Coder() throws IOException {
        byte[] original = "BZip2 Compression Test Data String Repeated Repeated Repeated".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = Coders.addEncoder(baos, SevenZMethod.BZIP2, null);
        out.write(original);
        out.close();

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.BZIP2.getId();
        InputStream in = Coders.addDecoder(new ByteArrayInputStream(baos.toByteArray()), coder, null);

        ByteArrayOutputStream decodedBaos = new ByteArrayOutputStream();
        byte[] buffer = new byte[16];
        int read;
        while ((read = in.read(buffer)) != -1) {
            decodedBaos.write(buffer, 0, read);
        }
        in.close();

        Assert.assertArrayEquals(original, decodedBaos.toByteArray());
    }

    @Test
    public void testLZMADecoderDictionaryTooLarge() {
        Coders.LZMADecoder decoder = new Coders.LZMADecoder();
        Coder coder = new Coder();
        // properties: propsByte, dictSize (4 bytes little-endian)
        // 0x7F000000 = 2130706432 > LZMAInputStream.DICT_SIZE_MAX (1 << 30 = 1073741824)
        coder.properties = new byte[] {
            0x5d,
            0x00, 0x00, 0x00, 0x7F
        };

        try {
            decoder.decode(new ByteArrayInputStream(new byte[0]), coder, null);
            Assert.fail("Expected IOException for dictionary size exceeding maximum");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Dictionary larger than 4GiB maximum size"));
        }
    }

    @Test
    public void testLZMADecoderValidProperties() throws IOException {
        Coders.LZMADecoder decoder = new Coders.LZMADecoder();
        Coder coder = new Coder();
        // propsByte=0x5d, dictSize = 65536 (0x00010000)
        coder.properties = new byte[] {
            0x5d,
            0x00, 0x00, 0x01, 0x00
        };

        InputStream is = decoder.decode(new ByteArrayInputStream(new byte[0]), coder, null);
        Assert.assertNotNull(is);
        is.close();
    }

    @Test
    public void testAES256SHA256DecoderSaltIvTooLong() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // byte0: saltSize bit (bit 7) = 1, ivSize bit (bit 6) = 1, cycles = 0 -> 0b11000000 = 0xC0
        // byte1: saltSize low (bits 7-4) = 0xF0, ivSize low (bits 3-0) = 0x0F -> 0xFF
        // saltSize = 1 + 15 = 16, ivSize = 1 + 15 = 16. Total needed = 2 + 16 + 16 = 34 bytes.
        // Provide only 2 bytes in coder.properties
        coder.properties = new byte[] { (byte) 0xC0, (byte) 0xFF };

        InputStream in = Coders.addDecoder(new ByteArrayInputStream(new byte[16]), coder, "password".getBytes("UTF-8"));
        try {
            in.read();
            Assert.fail("Expected IOException due to salt/iv length exceeding properties length");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Salt size + IV size too long"));
        }
    }

    @Test
    public void testAES256SHA256DecoderMissingPassword() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[] { 0x00, 0x00 };

        InputStream in = Coders.addDecoder(new ByteArrayInputStream(new byte[16]), coder, null);
        try {
            in.read();
            Assert.fail("Expected IOException when password is missing");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot read encrypted files without a password"));
        }
    }

    @Test
    public void testAES256SHA256DecoderDirectKeyMode() throws Exception {
        // numCyclesPower = 0x3f (63) -> triggers direct key copying from salt and password
        // byte0 = 0x3f (cycles = 0x3f, salt bit = 0, iv bit = 0)
        // byte1 = 0x00
        byte[] properties = new byte[2];
        properties[0] = 0x3f;
        properties[1] = 0x00;

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = properties;

        byte[] password = new byte[32];
        for (int i = 0; i < password.length; i++) {
            password[i] = (byte) (i + 1);
        }

        byte[] plainText = new byte[16];
        Arrays.fill(plainText, (byte) 0x42);

        // Encrypt plainText using AES/CBC/NoPadding with key=password and iv=all zeros
        SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] cipherText = cipher.doFinal(plainText);

        InputStream in = Coders.addDecoder(new ByteArrayInputStream(cipherText), coder, password);
        byte[] decrypted = new byte[16];
        int totalRead = 0;
        int read;
        while (totalRead < 16 && (read = in.read(decrypted, totalRead, 16 - totalRead)) != -1) {
            totalRead += read;
        }
        in.close();

        Assert.assertEquals(16, totalRead);
        Assert.assertArrayEquals(plainText, decrypted);
    }

    @Test
    public void testAES256SHA256DecoderHashedKeyMode() throws Exception {
        // numCyclesPower = 1 (1 << 1 = 2 cycles), saltSize = 1, ivSize = 1
        // byte0: salt bit (bit 7) = 0, iv bit (bit 6) = 0, cycles = 1 -> 0x01
        // byte1: saltSize low = 1 (0x10), ivSize low = 1 (0x01) -> 0x11
        // properties = [0x01, 0x11, salt(1 byte), iv(1 byte)] -> length 4
        byte[] salt = new byte[] { 0x05 };
        byte[] iv = new byte[] { 0x0A };
        byte[] properties = new byte[] { 0x01, 0x11, salt[0], iv[0] };

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = properties;

        byte[] password = "SecretPassword123".getBytes("UTF-8");

        // Compute expected AES key
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] extra = new byte[8];
        for (long j = 0; j < (1L << 1); j++) {
            digest.update(salt);
            digest.update(password);
            digest.update(extra);
            for (int k = 0; k < extra.length; k++) {
                ++extra[k];
                if (extra[k] != 0) {
                    break;
                }
            }
        }
        byte[] aesKeyBytes = digest.digest();

        byte[] fullIv = new byte[16];
        System.arraycopy(iv, 0, fullIv, 0, 1);

        byte[] plainText = new byte[16];
        for (int i = 0; i < plainText.length; i++) {
            plainText[i] = (byte) (i * 2);
        }

        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(fullIv);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] cipherText = cipher.doFinal(plainText);

        InputStream in = Coders.addDecoder(new ByteArrayInputStream(cipherText), coder, password);

        // Read first byte using read()
        int firstByte = in.read();
        Assert.assertEquals(plainText[0] & 0xFF, firstByte);

        // Read remaining bytes using read(byte[], off, len)
        byte[] rest = new byte[15];
        int read = in.read(rest, 0, 15);
        Assert.assertEquals(15, read);
        for (int i = 0; i < 15; i++) {
            Assert.assertEquals(plainText[i + 1], rest[i]);
        }

        in.close();
    }

    @Test
    public void testDummyByteAddingInputStreamDirectly() throws Exception {
        Class<?> dummyClass = Class.forName("org.apache.commons.compress.archivers.sevenz.Coders$DummyByteAddingInputStream");
        Constructor<?> constructor = dummyClass.getDeclaredConstructor(InputStream.class);
        constructor.setAccessible(true);

        byte[] data = new byte[] { 1, 2 };
        InputStream dummyIs = (InputStream) constructor.newInstance(new ByteArrayInputStream(data));

        Assert.assertEquals(1, dummyIs.read());
        Assert.assertEquals(2, dummyIs.read());
        // First EOF returns dummy byte 0
        Assert.assertEquals(0, dummyIs.read());
        // Second EOF returns -1
        Assert.assertEquals(-1, dummyIs.read());
        dummyIs.close();

        // Test array read
        InputStream dummyIsArray = (InputStream) constructor.newInstance(new ByteArrayInputStream(new byte[] { 7 }));
        byte[] buf = new byte[4];
        int read1 = dummyIsArray.read(buf, 0, 4);
        Assert.assertEquals(1, read1);
        Assert.assertEquals(7, buf[0]);

        // Next read triggers dummy byte
        int read2 = dummyIsArray.read(buf, 0, 4);
        Assert.assertEquals(1, read2);
        Assert.assertEquals(0, buf[0]);

        // Next read returns -1
        int read3 = dummyIsArray.read(buf, 0, 4);
        Assert.assertEquals(-1, read3);
        dummyIsArray.close();
    }
}
