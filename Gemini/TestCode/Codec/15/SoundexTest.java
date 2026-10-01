package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for {@link Soundex}.
 */
public class SoundexTest {

    private Soundex soundex;

    @Before
    public void setUp() {
        this.soundex = new Soundex();
    }

    @Test
    public void testDefaultConstructor() {
        Soundex s = new Soundex();
        Assert.assertEquals("S530", s.soundex("Smith"));
        Assert.assertEquals(4, s.getMaxLength());
    }

    @Test
    public void testCharArrayConstructor() {
        char[] mapping = Soundex.US_ENGLISH_MAPPING_STRING.toCharArray();
        Soundex s = new Soundex(mapping);
        Assert.assertEquals("S530", s.soundex("Smith"));
    }

    @Test
    public void testStringConstructor() {
        Soundex s = new Soundex(Soundex.US_ENGLISH_MAPPING_STRING);
        Assert.assertEquals("S530", s.soundex("Smith"));
    }

    @Test
    public void testStaticUsEnglishInstance() {
        Assert.assertNotNull(Soundex.US_ENGLISH);
        Assert.assertEquals("S530", Soundex.US_ENGLISH.soundex("Smith"));
    }

    @Test
    public void testUsEnglishMappingConstant() {
        Assert.assertEquals("01230120022455012623010202", Soundex.US_ENGLISH_MAPPING_STRING);
    }

    @Test
    public void testSoundexNullAndEmpty() {
        Assert.assertNull(this.soundex.soundex(null));
        Assert.assertEquals("", this.soundex.soundex(""));
        Assert.assertEquals("", this.soundex.soundex("   "));
        Assert.assertEquals("", this.soundex.soundex("12345!@#$%^&*()"));
    }

    @Test
    public void testStandardNames() {
        Assert.assertEquals("W252", this.soundex.soundex("Washington"));
        Assert.assertEquals("L000", this.soundex.soundex("Lee"));
        Assert.assertEquals("G362", this.soundex.soundex("Gutierrez"));
        Assert.assertEquals("P236", this.soundex.soundex("Pfister"));
        Assert.assertEquals("J250", this.soundex.soundex("Jackson"));
        Assert.assertEquals("T522", this.soundex.soundex("Tymczak"));
        Assert.assertEquals("A261", this.soundex.soundex("Ashcraft"));
        Assert.assertEquals("A261", this.soundex.soundex("Ashcroft"));
    }

    @Test
    public void testSoundexVariations() {
        Assert.assertEquals("E460", this.soundex.soundex("Ellery"));
        Assert.assertEquals("E460", this.soundex.soundex("Euler"));
        Assert.assertEquals("G200", this.soundex.soundex("Gauss"));
        Assert.assertEquals("G200", this.soundex.soundex("Ghosh"));
        Assert.assertEquals("H416", this.soundex.soundex("Hilbert"));
        Assert.assertEquals("H416", this.soundex.soundex("Heilbronn"));
        Assert.assertEquals("K530", this.soundex.soundex("Knuth"));
        Assert.assertEquals("K530", this.soundex.soundex("Kant"));
        Assert.assertEquals("L300", this.soundex.soundex("Lloyd"));
        Assert.assertEquals("L300", this.soundex.soundex("Ladd"));
        Assert.assertEquals("L222", this.soundex.soundex("Lukasiewicz"));
        Assert.assertEquals("L222", this.soundex.soundex("Lissajous"));
    }

    @Test
    public void testHAndWRuleSeparationSameCode() {
        // Consonants from same code group separated by W or H treated as one
        // 'B' and 'P' are code 1 -> B P => B100
        Assert.assertEquals("B100", this.soundex.soundex("BHP"));
        Assert.assertEquals("B100", this.soundex.soundex("BWP"));
        Assert.assertEquals("B100", this.soundex.soundex("BB"));
        Assert.assertEquals("B100", this.soundex.soundex("B H P"));
        Assert.assertEquals("B100", this.soundex.soundex("B W P"));
    }

    @Test
    public void testHAndWRuleSeparationDifferentCode() {
        // Consonants from different code groups separated by W or H
        // 'B' is 1, 'T' is 3 -> BHT => B300
        Assert.assertEquals("B300", this.soundex.soundex("BHT"));
        Assert.assertEquals("B300", this.soundex.soundex("BWT"));
    }

    @Test
    public void testHWConsecutiveBranches() {
        // Test HW rule where preHWChar is 'H' or 'W'
        // Index > 1: str="BHHB" -> index 3 is 'B', hwChar is str.charAt(2)='H', preHWChar is str.charAt(1)='H'
        Assert.assertEquals("B000", this.soundex.soundex("BHHB"));
        Assert.assertEquals("B000", this.soundex.soundex("BWWB"));
        Assert.assertEquals("B000", this.soundex.soundex("BHWB"));
        Assert.assertEquals("B000", this.soundex.soundex("BWHB"));

        // When preHWChar is not H/W and not same code
        // str = "ABHB" -> A(0), B(1), H, B(1)
        // index 0: A
        // index 1: B (1)
        // index 2: H (0)
        // index 3: B -> hwChar='H', preHWChar='B' (code 1), firstCode == mappedChar (1 == 1) -> returns 0
        Assert.assertEquals("A100", this.soundex.soundex("ABHB"));
        Assert.assertEquals("A100", this.soundex.soundex("ABWB"));

        // str = "ATHB" -> A(0), T(3), H, B(1)
        // index 3: B -> hwChar='H', preHWChar='T' (code 3), firstCode (3) != mappedChar (1), preHWChar not H/W -> returns '1'
        Assert.assertEquals("A310", this.soundex.soundex("ATHB"));
        Assert.assertEquals("A310", this.soundex.soundex("ATWB"));
    }

    @Test
    public void testEncodeString() {
        Assert.assertEquals("S530", this.soundex.encode("Smith"));
        Assert.assertEquals("S530", this.soundex.encode("Smyth"));
        Assert.assertNull(this.soundex.encode((String) null));
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        Object result = this.soundex.encode((Object) "Testing");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals("T235", result);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidType() throws EncoderException {
        this.soundex.encode(Integer.valueOf(12345));
    }

    @Test
    public void testDifference() throws EncoderException {
        Assert.assertEquals(4, this.soundex.difference("Smith", "Smyth"));
        Assert.assertEquals(2, this.soundex.difference("Ann", "Andrew"));
        Assert.assertEquals(1, this.soundex.difference("Margaret", "Andrew"));
        Assert.assertEquals(0, this.soundex.difference("Janet", ""));
        Assert.assertEquals(0, this.soundex.difference(null, "Smith"));
        Assert.assertEquals(0, this.soundex.difference("Smith", null));
        Assert.assertEquals(0, this.soundex.difference(null, null));
    }

    @Test
    public void testGetSetMaxLengthDeprecated() {
        Assert.assertEquals(4, this.soundex.getMaxLength());
        this.soundex.setMaxLength(6);
        Assert.assertEquals(6, this.soundex.getMaxLength());
        this.soundex.setMaxLength(2);
        Assert.assertEquals(2, this.soundex.getMaxLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCustomMappingCharacterOutOfRangeHigh() {
        // Mapping string only covers 'A' to 'C' (length 3)
        Soundex customSoundex = new Soundex("123");
        // 'D' is index 3, which is >= mapping.length -> throws IllegalArgumentException
        customSoundex.soundex("ABCD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCustomMappingCharArrayOutOfRangeHigh() {
        char[] mapping = new char[]{'1', '2', '3'};
        Soundex customSoundex = new Soundex(mapping);
        customSoundex.soundex("D");
    }

    @Test
    public void testCustomMappingValid() {
        // Full 26 length custom mapping
        String customMap = "00000000000000000000000000";
        Soundex customSoundex = new Soundex(customMap);
        Assert.assertEquals("A000", customSoundex.soundex("Alexander"));
    }

    @Test
    public void testSingleLetterAndShortStrings() {
        Assert.assertEquals("A000", this.soundex.soundex("A"));
        Assert.assertEquals("B000", this.soundex.soundex("B"));
        Assert.assertEquals("A200", this.soundex.soundex("Ac"));
        Assert.assertEquals("A230", this.soundex.soundex("Act"));
        Assert.assertEquals("A232", this.soundex.soundex("Actor"));
    }

    @Test
    public void testVowelSeparatedConsonantsWithSameCode() {
        // Consonants from same code group separated by a vowel ARE coded twice
        // 'T' and 'D' have code 3. Separated by 'O':
        Assert.assertEquals("T300", this.soundex.soundex("TOD"));
        Assert.assertEquals("T330", this.soundex.soundex("TODD"));
        Assert.assertEquals("T330", this.soundex.soundex("TOT"));
    }

    @Test
    public void testAllZeroMappingAfterFirstChar() {
        // A, E, I, O, U, Y, H, W are all mapped to '0'
        Assert.assertEquals("A000", this.soundex.soundex("Aeio uyhw"));
    }
}
