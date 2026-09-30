package org.apache.commons.codec.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CaverphoneTest {

    private final Caverphone caverphone = new Caverphone();

    @Test
    public void testCaverphoneNull() {
        assertEquals("1111111111", caverphone.caverphone(null));
    }

    @Test
    public void testCaverphoneEmpty() {
        assertEquals("1111111111", caverphone.caverphone(""));
    }

    @Test
    public void testCaverphoneNonAlphaOnly() {
        assertEquals("1111111111", caverphone.caverphone("1234567890"));
    }

    @Test
    public void testCaverphoneBasic() {
        assertEquals("STFNSN1111", caverphone.caverphone("Stevenson"));
        assertEquals("PTRSN11111", caverphone.caverphone("Peterson"));
        assertEquals("KRNKN11111", caverphone.caverphone("Cranken"));
    }

    @Test
    public void testCaverphoneStartPatterns() {
        assertEquals("KF2N111111", caverphone.caverphone("cough"));
        assertEquals("RF2N111111", caverphone.caverphone("rough"));
        assertEquals("TF2N111111", caverphone.caverphone("tough"));
        assertEquals("NF2N111111", caverphone.caverphone("enough"));
        assertEquals("TRF2N11111", caverphone.caverphone("trough"));
        assertEquals("2N11111111", caverphone.caverphone("gn"));
        assertEquals("M211111111", caverphone.caverphone("mb"));
    }

    @Test
    public void testCaverphoneReplacements() {
        assertEquals("2K11111111", caverphone.caverphone("cq"));
        assertEquals("S111111111", caverphone.caverphone("ci"));
        assertEquals("S111111111", caverphone.caverphone("ce"));
        assertEquals("S111111111", caverphone.caverphone("cy"));
        assertEquals("2K11111111", caverphone.caverphone("tch"));
        assertEquals("K111111111", caverphone.caverphone("c"));
        assertEquals("K111111111", caverphone.caverphone("q"));
        assertEquals("K111111111", caverphone.caverphone("x"));
        assertEquals("F111111111", caverphone.caverphone("v"));
        assertEquals("2K11111111", caverphone.caverphone("dg"));
        assertEquals("S111111111", caverphone.caverphone("tio"));
        assertEquals("S111111111", caverphone.caverphone("tia"));
        assertEquals("T111111111", caverphone.caverphone("d"));
        assertEquals("F111111111", caverphone.caverphone("ph"));
        assertEquals("P111111111", caverphone.caverphone("b"));
        assertEquals("S211111111", caverphone.caverphone("sh"));
        assertEquals("S111111111", caverphone.caverphone("z"));
        assertEquals("A111111111", caverphone.caverphone("a"));
        assertEquals("3111111111", caverphone.caverphone("e"));
        assertEquals("Y111111111", caverphone.caverphone("j"));
        assertEquals("Y311111111", caverphone.caverphone("y3"));
        assertEquals("A111111111", caverphone.caverphone("y"));
        assertEquals("3111111111", caverphone.caverphone("y"));
        assertEquals("3K31111111", caverphone.caverphone("3gh3"));
        assertEquals("2211111111", caverphone.caverphone("gh"));
        assertEquals("K111111111", caverphone.caverphone("g"));
        assertEquals("S111111111", caverphone.caverphone("ss"));
        assertEquals("T111111111", caverphone.caverphone("tt"));
        assertEquals("P111111111", caverphone.caverphone("pp"));
        assertEquals("K111111111", caverphone.caverphone("kk"));
        assertEquals("F111111111", caverphone.caverphone("ff"));
        assertEquals("M111111111", caverphone.caverphone("mm"));
        assertEquals("N111111111", caverphone.caverphone("nn"));
        assertEquals("W311111111", caverphone.caverphone("w3"));
        assertEquals("W311111111", caverphone.caverphone("wh3"));
        assertEquals("3111111111", caverphone.caverphone("w"));
        assertEquals("2111111111", caverphone.caverphone("w"));
        assertEquals("A111111111", caverphone.caverphone("h"));
        assertEquals("2111111111", caverphone.caverphone("h"));
        assertEquals("R311111111", caverphone.caverphone("r3"));
        assertEquals("3111111111", caverphone.caverphone("r"));
        assertEquals("2111111111", caverphone.caverphone("r"));
        assertEquals("L311111111", caverphone.caverphone("l3"));
        assertEquals("3111111111", caverphone.caverphone("l"));
        assertEquals("2111111111", caverphone.caverphone("l"));
    }

    @Test
    public void testCaverphoneFinalRemovals() {
        assertEquals("A111111111", caverphone.caverphone("2"));
        assertEquals("A111111111", caverphone.caverphone("3"));
        assertEquals("1111111111", caverphone.caverphone("23"));
    }

    @Test
    public void testEncodeString() {
        assertEquals("STFNSN1111", caverphone.encode("Stevenson"));
    }

    @Test
    public void testEncodeObjectValid() throws EncoderException {
        assertEquals("STFNSN1111", caverphone.encode((Object) "Stevenson"));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalid() throws EncoderException {
        caverphone.encode(new Integer(123));
    }

    @Test
    public void testIsCaverphoneEqual() {
        assertTrue(caverphone.isCaverphoneEqual("Stevenson", "Stevenson"));
        assertFalse(caverphone.isCaverphoneEqual("Stevenson", "Peterson"));
    }
}
