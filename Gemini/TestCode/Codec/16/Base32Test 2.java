package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Base32Test {

    private static final byte[] CRLF = new byte[]{'\r', '\n'};

    // RFC 4648 Base32 test vectors
    private static final String[] BASE32_TEST_VECTORS = {
            "",
            "f",
            "fo",
            "foo",
            "foob",
            "fooba",
            "foobar"
    };

    private static final String[] BASE32_EXPECTED = {
            "",
            "MY======",
            "MZXQ====",
            "MZXW6===",
            "MZXW6YQ=",
            "MZXW6YTB",
            "MZXW6YTBOI======"
    };

    // RFC 4648 Base32hex test vectors
    private static final String[] BASE32HEX_EXPECTED = {
            "",
            "CO======",
            "CPNG====",
            "CPNMU===",
            "CPNMUOG=",
            "CPNMUOJ1",
            "CPNMUOJ1E8======"
    };

    @Test
    public void testDefaultConstructor() {
        Base32 base32 = new Base32();
        Assert.assertNotNull(base32);
        Assert.assertFalse(base32.isInAlphabet((byte) '='));
    }

    @Test
    public void testConstructorWithPad() {
        Base32 base32 = new Base32((byte) '_');
        byte[] input = "foo".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("MZXW6___", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testConstructorWithUseHex() {
        Base32 base32Hex = new Base32(true);
        for (int i = 0; i < BASE32_TEST_VECTORS.length; i++) {
            byte[] input = BASE32_TEST_VECTORS[i].getBytes(StandardCharsets.UTF_8);
            String encoded = base32Hex.encodeAsString(input);
            Assert.assertEquals(BASE32HEX_EXPECTED[i], encoded);
            byte[] decoded = base32Hex.decode(encoded);
            Assert.assertArrayEquals(input, decoded);
        }
    }

    @Test
    public void testConstructorWithUseHexAndPad() {
        Base32 base32Hex = new Base32(true, (byte) '$');
        byte[] input = "foob".getBytes(StandardCharsets.UTF_8);
        String encoded = base32Hex.encodeAsString(input);
        Assert.assertEquals("CPNMUOG$", encoded);
        byte[] decoded = base32Hex.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testConstructorWithLineLength() {
        Base32 base32 = new Base32(8);
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        // "1234567890" -> 10 bytes -> 16 chars -> 2 lines of 8
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GEZDGNBV\r\nGY3TQOJQ\r\n", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testConstructorWithLineLengthAndSeparator() {
        byte[] customSep = new byte[]{'-', '-'};
        Base32 base32 = new Base32(8, customSep);
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GEZDGNBV--GY3TQOJQ--", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testConstructorWithLineLengthSeparatorAndHex() {
        byte[] customSep = new byte[]{';'};
        Base32 base32Hex = new Base32(8, customSep, true);
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        String encoded = base32Hex.encodeAsString(input);
        Assert.assertEquals("64P36D1L;6ORJGE9G;", encoded);
        byte[] decoded = base32Hex.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testConstructorWithAllParams() {
        byte[] customSep = new byte[]{':'};
        Base32 base32 = new Base32(8, customSep, false, (byte) '.');
        byte[] input = "12345".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GEZDGNBV:", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullLineSeparatorWithPositiveLength() {
        new Base32(10, null, false, (byte) '=');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSeparatorContainsBase32Char() {
        byte[] invalidSep = new byte[]{'A'};
        new Base32(8, invalidSep);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSeparatorContainsPadChar() {
        byte[] invalidSep = new byte[]{'='};
        new Base32(8, invalidSep);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPadInAlphabet() {
        new Base32((byte) 'A');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPadInHexAlphabet() {
        new Base32(true, (byte) '0');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPadWhitespace() {
        new Base32((byte) ' ');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPadTab() {
        new Base32((byte) '\t');
    }

    @Test
    public void testRfc4648StandardVectors() {
        Base32 base32 = new Base32();
        for (int i = 0; i < BASE32_TEST_VECTORS.length; i++) {
            byte[] input = BASE32_TEST_VECTORS[i].getBytes(StandardCharsets.UTF_8);
            String encoded = base32.encodeAsString(input);
            Assert.assertEquals(BASE32_EXPECTED[i], encoded);
            byte[] decoded = base32.decode(encoded);
            Assert.assertArrayEquals(input, decoded);
        }
    }

    @Test
    public void testRfc4648HexVectors() {
        Base32 base32Hex = new Base32(true);
        for (int i = 0; i < BASE32_TEST_VECTORS.length; i++) {
            byte[] input = BASE32_TEST_VECTORS[i].getBytes(StandardCharsets.UTF_8);
            String encoded = base32Hex.encodeAsString(input);
            Assert.assertEquals(BASE32HEX_EXPECTED[i], encoded);
            byte[] decoded = base32Hex.decode(encoded);
            Assert.assertArrayEquals(input, decoded);
        }
    }

    @Test
    public void testModulusEncodings() {
        Base32 base32 = new Base32();
        // 1 byte -> modulus 1 -> 2 Base32 chars + 6 pads
        Assert.assertEquals("MY======", base32.encodeAsString(new byte[]{(byte) 'f'}));
        // 2 bytes -> modulus 2 -> 4 Base32 chars + 4 pads
        Assert.assertEquals("MZXQ====", base32.encodeAsString(new byte[]{(byte) 'f', (byte) 'o'}));
        // 3 bytes -> modulus 3 -> 5 Base32 chars + 3 pads
        Assert.assertEquals("MZXW6===", base32.encodeAsString(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'}));
        // 4 bytes -> modulus 4 -> 7 Base32 chars + 1 pad
        Assert.assertEquals("MZXW6YQ=", base32.encodeAsString(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o', (byte) 'b'}));
        // 5 bytes -> modulus 0 -> 8 Base32 chars + 0 pads
        Assert.assertEquals("MZXW6YTB", base32.encodeAsString(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o', (byte) 'b', (byte) 'a'}));
    }

    @Test
    public void testModulusDecodings() {
        Base32 base32 = new Base32();
        // Modulus 2: 2 Base32 chars decoded -> 1 byte
        Assert.assertArrayEquals(new byte[]{(byte) 'f'}, base32.decode("MY"));
        Assert.assertArrayEquals(new byte[]{(byte) 'f'}, base32.decode("MY======"));
        // Modulus 4: 4 Base32 chars decoded -> 2 bytes
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o'}, base32.decode("MZXQ"));
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o'}, base32.decode("MZXQ===="));
        // Modulus 5: 5 Base32 chars decoded -> 3 bytes
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'}, base32.decode("MZXW6"));
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'}, base32.decode("MZXW6==="));
        // Modulus 7: 7 Base32 chars decoded -> 4 bytes
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o', (byte) 'b'}, base32.decode("MZXW6YQ"));
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o', (byte) 'b'}, base32.decode("MZXW6YQ="));
        // Modulus 0: 8 Base32 chars decoded -> 5 bytes
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o', (byte) 'b', (byte) 'a'}, base32.decode("MZXW6YTB"));
    }

    @Test
    public void testDecodeModulus3And6() {
        Base32 base32 = new Base32();
        // Modulus 3: 3 Base32 chars (15 bits, drop 7 bits -> 1 byte output)
        // 'M' = 12 (01100), 'Y' = 24 (11000), 'A' = 0 (00000) -> 01100 11000 00000 = 01100110 0000000 -> 0x66 = 'f'
        byte[] decoded3 = base32.decode("MYA");
        Assert.assertArrayEquals(new byte[]{(byte) 'f'}, decoded3);

        // Modulus 6: 6 Base32 chars (30 bits, drop 6 bits -> 3 bytes output)
        // "MZXW6A" -> 5 bits each * 6 = 30 bits -> 3 bytes (0x66, 0x6f, 0x6f)
        byte[] decoded6 = base32.decode("MZXW6A");
        Assert.assertArrayEquals(new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'}, decoded6);
    }

    @Test
    public void testDecodeModulusLessThan2() {
        Base32 base32 = new Base32();
        // Modulus 1 (only 1 char, e.g. "M") -> 5 bits is less than 8 bits, so dropped/ignored
        byte[] decoded1 = base32.decode("M");
        Assert.assertArrayEquals(new byte[0], decoded1);

        // Modulus 0 with no data
        byte[] decoded0 = base32.decode("");
        Assert.assertArrayEquals(new byte[0], decoded0);
    }

    @Test
    public void testDecodeWithGarbageAndSpaces() {
        Base32 base32 = new Base32();
        // Should ignore spaces, newlines, tabs, and non-base32 chars
        byte[] decoded = base32.decode(" M Z\r\n X\t W 6 === ");
        Assert.assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), decoded);

        // Invalid characters such as '$', '%', '[', '1', '8', '9'
        byte[] decodedGarbage = base32.decode("!M$Z%X[W]6?===");
        Assert.assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), decodedGarbage);
    }

    @Test
    public void testDecodeNegativeBytes() {
        Base32 base32 = new Base32();
        // Byte array with negative values should be ignored as non-Base32 characters
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0x80, 'M', 'Y', '=', '=', '=', '=', '=', '='};
        byte[] decoded = base32.decode(input);
        Assert.assertArrayEquals("f".getBytes(StandardCharsets.UTF_8), decoded);
    }

    @Test
    public void testEncodeNegativeBytes() {
        Base32 base32 = new Base32();
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD, (byte) 0xFC, (byte) 0xFB};
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("777737H7", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testChunkedEncodingMultipleLines() {
        // Line length 16 with CRLF
        Base32 base32 = new Base32(16, CRLF);
        byte[] input = "01234567890123456789".getBytes(StandardCharsets.UTF_8); // 20 bytes -> 32 Base32 chars
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GAYDAMBQGAYDAMBQ\r\nGAYDAMBQGAYDAMBQ\r\n", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testChunkedEncodingNonMultipleOf8LineLength() {
        // Line length 10 -> rounded down to multiple of 8 in logic -> 8
        Base32 base32 = new Base32(10, new byte[]{'\n'});
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GEZDGNBV\nGY3TQOJQ\n", encoded);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testChunkedEncodingEofWithLeftovers() {
        Base32 base32 = new Base32(8, new byte[]{'#'});
        // 1 byte -> "MY======" + '#'
        String encoded1 = base32.encodeAsString(new byte[]{'f'});
        Assert.assertEquals("MY======#", encoded1);

        // 6 bytes -> "MZXW6YTB" + '#' + "MY======" + '#'
        String encoded6 = base32.encodeAsString("foobar".substring(0, 6).getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("MZXW6YTB#OI======#", base32.encodeAsString("foobar".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testZeroLineLengthWithCustomSeparator() {
        // lineLength <= 0 should disable chunking even if separator is provided
        Base32 base32 = new Base32(0, new byte[]{'\n'});
        byte[] input = "01234567890123456789".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertFalse(encoded.contains("\n"));
        Assert.assertEquals("GAYDAMBQGAYDAMBQGAYDAMBQGAYDAMBQ", encoded);
    }

    @Test
    public void testNegativeLineLength() {
        Base32 base32 = new Base32(-1);
        byte[] input = "12345".getBytes(StandardCharsets.UTF_8);
        String encoded = base32.encodeAsString(input);
        Assert.assertEquals("GEZDGNBV", encoded);
    }

    @Test
    public void testIsInAlphabet() {
        Base32 base32 = new Base32();
        Assert.assertTrue(base32.isInAlphabet((byte) 'A'));
        Assert.assertTrue(base32.isInAlphabet((byte) 'Z'));
        Assert.assertTrue(base32.isInAlphabet((byte) '2'));
        Assert.assertTrue(base32.isInAlphabet((byte) '7'));

        Assert.assertFalse(base32.isInAlphabet((byte) '1'));
        Assert.assertFalse(base32.isInAlphabet((byte) '8'));
        Assert.assertFalse(base32.isInAlphabet((byte) '0'));
        Assert.assertFalse(base32.isInAlphabet((byte) '9'));
        Assert.assertFalse(base32.isInAlphabet((byte) 'a')); // lower case not in standard decode table
        Assert.assertFalse(base32.isInAlphabet((byte) '='));
        Assert.assertFalse(base32.isInAlphabet((byte) -1));
        Assert.assertFalse(base32.isInAlphabet((byte) 127));

        Base32 base32Hex = new Base32(true);
        Assert.assertTrue(base32Hex.isInAlphabet((byte) '0'));
        Assert.assertTrue(base32Hex.isInAlphabet((byte) '9'));
        Assert.assertTrue(base32Hex.isInAlphabet((byte) 'A'));
        Assert.assertTrue(base32Hex.isInAlphabet((byte) 'V'));
        Assert.assertFalse(base32Hex.isInAlphabet((byte) 'W'));
        Assert.assertFalse(base32Hex.isInAlphabet((byte) 'Z'));
    }

    @Test
    public void testDirectEncodeAndDecodeStreaming() {
        Base32 base32 = new Base32();
        BaseNCodec.Context context = new BaseNCodec.Context();
        byte[] input = "Hello World!".getBytes(StandardCharsets.UTF_8);

        // Feed data piece by piece
        base32.encode(input, 0, 5, context);
        base32.encode(input, 5, 5, context);
        base32.encode(input, 10, 2, context);
        base32.encode(input, 0, -1, context); // EOF

        byte[] encodedBuffer = new byte[context.pos];
        System.arraycopy(context.buffer, 0, encodedBuffer, 0, context.pos);
        String encodedStr = new String(encodedBuffer, StandardCharsets.UTF_8);

        Assert.assertEquals("JBSWY3DPEBLW64TMMQQQ====", encodedStr);

        // Decode streaming
        BaseNCodec.Context decodeContext = new BaseNCodec.Context();
        byte[] encodedBytes = encodedStr.getBytes(StandardCharsets.UTF_8);
        base32.decode(encodedBytes, 0, 10, decodeContext);
        base32.decode(encodedBytes, 10, 10, decodeContext);
        base32.decode(encodedBytes, 20, 4, decodeContext);
        base32.decode(encodedBytes, 0, -1, decodeContext); // EOF

        byte[] decodedBuffer = new byte[decodeContext.pos];
        System.arraycopy(decodeContext.buffer, 0, decodedBuffer, 0, decodeContext.pos);
        Assert.assertEquals("Hello World!", new String(decodedBuffer, StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeContextEofAlreadyTrue() {
        Base32 base32 = new Base32();
        BaseNCodec.Context context = new BaseNCodec.Context();
        context.eof = true;
        byte[] input = "MZXW6YTB".getBytes(StandardCharsets.UTF_8);
        base32.decode(input, 0, input.length, context);
        Assert.assertEquals(0, context.pos);
    }

    @Test
    public void testEncodeContextEofAlreadyTrue() {
        Base32 base32 = new Base32();
        BaseNCodec.Context context = new BaseNCodec.Context();
        context.eof = true;
        byte[] input = "foobar".getBytes(StandardCharsets.UTF_8);
        base32.encode(input, 0, input.length, context);
        Assert.assertEquals(0, context.pos);
    }

    @Test
    public void testEncodeEofWithModulusZeroAndNoChunking() {
        Base32 base32 = new Base32(0);
        BaseNCodec.Context context = new BaseNCodec.Context();
        byte[] input = "12345".getBytes(StandardCharsets.UTF_8);
        base32.encode(input, 0, 5, context);
        int posBeforeEof = context.pos;
        base32.encode(input, 0, -1, context);
        Assert.assertEquals(posBeforeEof, context.pos);
    }

    @Test
    public void testDecodeWithPaddingInMiddle() {
        Base32 base32 = new Base32();
        BaseNCodec.Context context = new BaseNCodec.Context();
        // Pad in the middle causes EOF to be set and loop to terminate
        byte[] input = "MY===MZXW6YTB".getBytes(StandardCharsets.UTF_8);
        base32.decode(input, 0, input.length, context);
        Assert.assertTrue(context.eof);
        byte[] decoded = new byte[context.pos];
        System.arraycopy(context.buffer, 0, decoded, 0, context.pos);
        Assert.assertArrayEquals("f".getBytes(StandardCharsets.UTF_8), decoded);
    }

    @Test
    public void testEncodeObjectAndDecodeObject() throws EncoderException, DecoderException {
        Base32 base32 = new Base32();
        byte[] input = "ObjectEncodingTest".getBytes(StandardCharsets.UTF_8);
        Object encodedObj = base32.encode((Object) input);
        Assert.assertTrue(encodedObj instanceof byte[]);
        Object decodedObj = base32.decode(encodedObj);
        Assert.assertTrue(decodedObj instanceof byte[]);
        Assert.assertArrayEquals(input, (byte[]) decodedObj);
    }

    @Test
    public void testEncodeEmptyByteArray() {
        Base32 base32 = new Base32();
        byte[] empty = new byte[0];
        Assert.assertArrayEquals(empty, base32.encode(empty));
        Assert.assertArrayEquals(empty, base32.decode(empty));
    }

    @Test
    public void testEncodeNull() {
        Base32 base32 = new Base32();
        Assert.assertNull(base32.encode((byte[]) null));
        Assert.assertNull(base32.decode((byte[]) null));
    }

    @Test
    public void testRandomBinaryRoundTrip() {
        Base32 base32 = new Base32();
        Base32 base32Hex = new Base32(true);
        for (int len = 1; len <= 50; len++) {
            byte[] data = new byte[len];
            for (int i = 0; i < len; i++) {
                data[i] = (byte) ((i * 37 + 13) & 0xFF);
            }
            byte[] encoded = base32.encode(data);
            byte[] decoded = base32.decode(encoded);
            Assert.assertArrayEquals("Failed round-trip at length " + len, data, decoded);

            byte[] hexEncoded = base32Hex.encode(data);
            byte[] hexDecoded = base32Hex.decode(hexEncoded);
            Assert.assertArrayEquals("Failed hex round-trip at length " + len, data, hexDecoded);
        }
    }
}
