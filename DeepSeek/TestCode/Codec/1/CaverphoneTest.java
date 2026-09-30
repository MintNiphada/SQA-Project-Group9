package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Test class for Caverphone.
 */
public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        caverphone = new Caverphone();
    }

    @Test
    public void testConstruct() {
        Caverphone c = new Caverphone();
        assertNotNull(c);
    }

    @Test
    public void testCaverphoneNull() {
        assertEquals("1111111111", caverphone.caverphone(null));
    }

    @Test
    public void testCaverphoneEmpty() {
        assertEquals("1111111111", caverphone.caverphone(""));
    }

    @Test
    public void testCaverphoneBasic() {
        assertEquals("1111111111", caverphone.caverphone("a"));
    }

    @Test
    public void testCaverphoneWord() {
        // Example from specification or basic encoding
        String result = caverphone.caverphone("Stevenson");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testCaverphoneName() {
        String result = caverphone.caverphone("Thompson");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testCaverphonePrefixCough() {
        // Should start with "cou2f"
        String result = caverphone.caverphone("cough");
        assertTrue(result.startsWith("c"));
    }

    @Test
    public void testCaverphonePrefixRough() {
        String result = caverphone.caverphone("rough");
        assertNotNull(result);
    }

    @Test
    public void testCaverphonePrefixTough() {
        String result = caverphone.caverphone("tough");
        assertNotNull(result);
    }

    @Test
    public void testCaverphonePrefixEnough() {
        String result = caverphone.caverphone("enough");
        assertNotNull(result);
    }

    @Test
    public void testCaverphonePrefixTrough() {
        String result = caverphone.caverphone("trough");
        assertNotNull(result);
    }

    @Test
    public void testCaverphonePrefixGn() {
        String result = caverphone.caverphone("gnome");
        assertNotNull(result);
    }

    @Test
    public void testCaverphonePrefixMb() {
        String result = caverphone.caverphone("mb");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneReplacements() {
        String result = caverphone.caverphone("queue");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneEndingE() {
        // "e$" removal
        String res1 = caverphone.caverphone("cake");
        String res2 = caverphone.caverphone("cak");
        assertEquals(res1, res2);
    }

    @Test
    public void testCaverphoneOnlyNonAlpha() {
        // After removing non a-z, becomes empty
        assertEquals("1111111111", caverphone.caverphone("1234"));
    }

    @Test
    public void testCaverphoneNonAlphaMixed() {
        String result = caverphone.caverphone("test123test");
        assertNotNull(result);
    }

    @Test
    public void testEncodeObjectString() throws Exception {
        Object result = caverphone.encode((Object) "hello");
        assertTrue(result instanceof String);
        assertEquals("1111111111".length(), ((String) result).length());
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectNonString() throws Exception {
        caverphone.encode(new Object());
    }

    @Test
    public void testEncodeString() {
        String result = caverphone.encode("test");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testIsCaverphoneEqual() {
        assertTrue(caverphone.isCaverphoneEqual("test", "test"));
        assertFalse(caverphone.isCaverphoneEqual("test", "other"));
    }

    @Test
    public void testCaverphoneAccommodateAllRules() {
        // This covers many regex replacements
        String complex = "coughroughcoughenoughmbtchphshdgjx";
        String result = caverphone.caverphone(complex);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testCaverphoneStartsWithVowel() {
        String result = caverphone.caverphone("abc");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneEndingW() {
        String result = caverphone.caverphone("cow");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneEndingR() {
        String result = caverphone.caverphone("car");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneEndingL() {
        String result = caverphone.caverphone("call");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneMultipleLetters() {
        // Repeated letters: sss -> S, ttt -> T etc.
        String result = caverphone.caverphone("ssstttpppkkkfffmmmnnn");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneYReplacement() {
        // j->y, y at start etc.
        String result = caverphone.caverphone("yay");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneGh3() {
        String result = caverphone.caverphone("3gh3");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneWh3() {
        String result = caverphone.caverphone("wh3");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneW3() {
        String result = caverphone.caverphone("w3");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneHStart() {
        String result = caverphone.caverphone("hello");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneR3() {
        String result = caverphone.caverphone("r3");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneL3() {
        String result = caverphone.caverphone("l3");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneFinalE() {
        // Remove final e again?
        String result = caverphone.caverphone("file");
        assertNotNull(result);
    }

    @Test
    public void testCaverphoneUpperCaseInput() {
        String result = caverphone.caverphone("HELLO");
        assertEquals(caverphone.caverphone("hello"), result);
    }

    @Test
    public void testCaverphoneNumbersAndSymbols() {
        String result = caverphone.caverphone("!@#$%^&*()_+");
        assertEquals("1111111111", result);
    }

    @Test
    public void testCaverphoneVeryLongString() {
        String longStr = "pneumonoultramicroscopicsilicovolcanoconiosis";
        String result = caverphone.caverphone(longStr);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testCaverphoneNullObjectEncode() throws Exception {
        try {
            caverphone.encode((Object) null);
            fail("Expected EncoderException");
        } catch (EncoderException e) {
            // expected
        }
    }

    @Test
    public void testCaverphoneEdgeCaseDoubleNonAlpha() {
        // Ensure non-alpha removal works and subsequent rules apply
        String result = caverphone.caverphone("a!b@c#");
        assertEquals(caverphone.caverphone("abc"), result);
    }
}