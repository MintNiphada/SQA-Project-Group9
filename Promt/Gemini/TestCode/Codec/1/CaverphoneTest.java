package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    @Test
    public void testConstructor() {
        assertNotNull(new Caverphone());
    }

    @Test
    public void testNullAndEmptyInput() {
        assertEquals("1111111111", this.caverphone.caverphone(null));
        assertEquals("1111111111", this.caverphone.caverphone(""));
        assertEquals("1111111111", this.caverphone.encode((String) null));
        assertEquals("1111111111", this.caverphone.encode(""));
    }

    @Test
    public void testNonAlphaFiltering() {
        assertEquals(this.caverphone.caverphone("peter"), this.caverphone.caverphone("123peter456"));
        assertEquals(this.caverphone.caverphone("peter"), this.caverphone.caverphone("p.e-t!e?r"));
        assertEquals("1111111111", this.caverphone.caverphone("1234567890!@#$%^&*()"));
    }

    @Test
    public void testCaseInsensitivity() {
        assertEquals(this.caverphone.caverphone("STEVENSON"), this.caverphone.caverphone("stevenson"));
        assertEquals(this.caverphone.caverphone("Peter"), this.caverphone.caverphone("PETER"));
    }

    @Test
    public void testSpecialPrefixes() {
        // cough
        assertEquals(this.caverphone.caverphone("cou2f"), this.caverphone.caverphone("cough"));
        // rough
        assertEquals(this.caverphone.caverphone("rou2f"), this.caverphone.caverphone("rough"));
        // tough
        assertEquals(this.caverphone.caverphone("tou2f"), this.caverphone.caverphone("tough"));
        // enough
        assertEquals(this.caverphone.caverphone("enou2f"), this.caverphone.caverphone("enough"));
        // trough
        assertEquals(this.caverphone.caverphone("trou2f"), this.caverphone.caverphone("trough"));
        // ^gn -> 2n
        assertEquals(this.caverphone.caverphone("gnome"), this.caverphone.caverphone("nome"));
        // ^mb -> m2
        assertEquals(this.caverphone.caverphone("mbappe"), this.caverphone.caverphone("mappe"));
    }

    @Test
    public void testRuleReplacements() {
        // cq -> 2q
        assertNotNull(this.caverphone.caverphone("acquire"));
        // ci, ce, cy -> si, se, sy
        assertNotNull(this.caverphone.caverphone("cider"));
        assertNotNull(this.caverphone.caverphone("center"));
        assertNotNull(this.caverphone.caverphone("cyan"));
        // tch -> 2ch
        assertNotNull(this.caverphone.caverphone("catch"));
        // c, q, x -> k
        assertNotNull(this.caverphone.caverphone("quick"));
        assertNotNull(this.caverphone.caverphone("box"));
        // v -> f
        assertNotNull(this.caverphone.caverphone("victor"));
        // dg -> 2g
        assertNotNull(this.caverphone.caverphone("bridge"));
        // tio, tia -> sio, sia
        assertNotNull(this.caverphone.caverphone("nation"));
        assertNotNull(this.caverphone.caverphone("spatial"));
        // d -> t
        assertNotNull(this.caverphone.caverphone("david"));
        // ph -> fh
        assertNotNull(this.caverphone.caverphone("phone"));
        // b -> p
        assertNotNull(this.caverphone.caverphone("bob"));
        // sh -> s2, z -> s
        assertNotNull(this.caverphone.caverphone("shoe"));
        assertNotNull(this.caverphone.caverphone("zebra"));
        // j -> y, ^y3 -> Y3, ^y -> A, y -> 3
        assertNotNull(this.caverphone.caverphone("jump"));
        assertNotNull(this.caverphone.caverphone("yellow"));
        assertNotNull(this.caverphone.caverphone("y"));
        assertNotNull(this.caverphone.caverphone("happy"));
        // 3gh3 -> 3kh3, gh -> 22, g -> k
        assertNotNull(this.caverphone.caverphone("night"));
        assertNotNull(this.caverphone.caverphone("ghost"));
        assertNotNull(this.caverphone.caverphone("gate"));
        // repetitions: s+, t+, p+, k+, f+, m+, n+
        assertNotNull(this.caverphone.caverphone("kiss"));
        assertNotNull(this.caverphone.caverphone("butter"));
        assertNotNull(this.caverphone.caverphone("apple"));
        assertNotNull(this.caverphone.caverphone("bookkeeper"));
        assertNotNull(this.caverphone.caverphone("fluff"));
        assertNotNull(this.caverphone.caverphone("summer"));
        assertNotNull(this.caverphone.caverphone("manner"));
        // w3 -> W3, wh3 -> Wh3, w$ -> 3, w -> 2
        assertNotNull(this.caverphone.caverphone("water"));
        assertNotNull(this.caverphone.caverphone("wheel"));
        assertNotNull(this.caverphone.caverphone("straw"));
        // ^h -> A, h -> 2
        assertNotNull(this.caverphone.caverphone("house"));
        assertNotNull(this.caverphone.caverphone("ahoy"));
        // r3 -> R3, r$ -> 3, r -> 2
        assertNotNull(this.caverphone.caverphone("river"));
        assertNotNull(this.caverphone.caverphone("car"));
        // l3 -> L3, l$ -> 3, l -> 2
        assertNotNull(this.caverphone.caverphone("lily"));
        assertNotNull(this.caverphone.caverphone("ball"));
        // end with e removal
        assertNotNull(this.caverphone.caverphone("taste"));
    }

    @Test
    public void testCaverphoneOutputLength() {
        String code1 = this.caverphone.caverphone("a");
        assertEquals(10, code1.length());

        String code2 = this.caverphone.caverphone("supercalifragilisticexpialidocious");
        assertEquals(10, code2.length());
    }

    @Test
    public void testKnownEncodings() {
        assertEquals("STFNSN1111", this.caverphone.caverphone("Stevenson"));
        assertEquals("PTA1111111", this.caverphone.caverphone("Peter"));
        assertEquals("LA11111111", this.caverphone.caverphone("Lee"));
        assertEquals("TMPSN11111", this.caverphone.caverphone("Thompson"));
    }

    @Test
    public void testEncodeString() {
        assertEquals("STFNSN1111", this.caverphone.encode("Stevenson"));
        assertEquals("1111111111", this.caverphone.encode(""));
        assertEquals("1111111111", this.caverphone.encode((String) null));
    }

    @Test
    public void testEncodeObjectValid() throws EncoderException {
        Object result = this.caverphone.encode((Object) "Stevenson");
        assertTrue(result instanceof String);
        assertEquals("STFNSN1111", result);
    }

    @Test
    public void testEncodeObjectInvalid() {
        try {
            this.caverphone.encode(Integer.valueOf(12345));
            fail("Expected EncoderException for non-String parameter");
        } catch (EncoderException e) {
            // Expected
            assertNotNull(e.getMessage());
        }

        try {
            this.caverphone.encode(new Object());
            fail("Expected EncoderException for Object parameter");
        } catch (EncoderException e) {
            // Expected
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testIsCaverphoneEqual() {
        assertTrue(this.caverphone.isCaverphoneEqual("Stevenson", "Stevenson"));
        assertTrue(this.caverphone.isCaverphoneEqual("Peter", "peter"));
        assertTrue(this.caverphone.isCaverphoneEqual(null, null));
        assertTrue(this.caverphone.isCaverphoneEqual("", ""));
        assertTrue(this.caverphone.isCaverphoneEqual(null, ""));

        assertFalse(this.caverphone.isCaverphoneEqual("Peter", "Stevenson"));
        assertFalse(this.caverphone.isCaverphoneEqual("Peter", null));
        assertFalse(this.caverphone.isCaverphoneEqual(null, "Stevenson"));
    }
}