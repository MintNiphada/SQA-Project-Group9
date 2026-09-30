package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

public class EntitiesTest {

    // --- escape(String, CharsetEncoder, EscapeMode) tests ---

    @Test
    public void testEscapeBaseModeWithMappedCharacters() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "&<>\"";
        String expected = "&amp;&lt;&gt;&quot;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeBaseModeWithNonMappedEncodableCharacter() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "A";
        assertEquals("A", Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeBaseModeWithNonMappedNonEncodableCharacter() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "é"; // e acute, not in base map, not ASCII
        String expected = "&#233;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeExtendedModeWithMappedCharacter() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "α"; // alpha, in full map
        String expected = "&alpha;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeExtendedModeWithBaseMappedCharacter() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "&";
        String expected = "&amp;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeExtendedModeWithNonMappedNonEncodableCharacter() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        // character not in full map and not ASCII
        String input = "\u1234"; // Ethiopic character
        String expected = "&#4660;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeEmptyString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
    }

    @Test(expected = NullPointerException.class)
    public void testEscapeNullString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        Entities.escape(null, encoder, Entities.EscapeMode.base);
    }

    @Test
    public void testEscapeMixedContent() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "a&b<c>d\"eé";
        // 'a','b','c','d','e' are ASCII, '&' -> &amp;, '<' -> &lt;, '>' -> &gt;, '"' -> &quot;, 'é' -> &#233;
        String expected = "a&amp;b&lt;c&gt;d&quot;e&#233;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    // --- escape(String, Document.OutputSettings) tests ---

    @Test
    public void testEscapeWithOutputSettingsBaseMode() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(Charset.forName("US-ASCII"));
        String input = "&é";
        String expected = "&amp;&#233;";
        assertEquals(expected, Entities.escape(input, out));
    }

    @Test
    public void testEscapeWithOutputSettingsExtendedMode() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        out.charset(Charset.forName("UTF-8"));
        String input = "α";
        String expected = "&alpha;";
        assertEquals(expected, Entities.escape(input, out));
    }

    // --- unescape tests ---

    @Test
    public void testUnescapeNoAmpersand() {
        String input = "hello world";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeNamedEntityWithSemicolon() {
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeNamedEntityWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&amp"));
    }

    @Test
    public void testUnescapeDecimalNumericEntity() {
        assertEquals("&", Entities.unescape("&#38;"));
    }

    @Test
    public void testUnescapeDecimalNumericEntityWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&#38"));
    }

    @Test
    public void testUnescapeHexNumericEntityLowercase() {
        assertEquals("&", Entities.unescape("&#x26;"));
    }

    @Test
    public void testUnescapeHexNumericEntityUppercase() {
        assertEquals("&", Entities.unescape("&#X26;"));
    }

    @Test
    public void testUnescapeHexNumericEntityWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&#x26"));
    }

    @Test
    public void testUnescapeUnknownNamedEntity() {
        String input = "&bogus;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeOutOfRangeNumericEntity() {
        // 0x1F600 = 128512, > 0xFFFF, cast to char yields 0xD83D? Actually (char)128512 is 0xD83D (55357) because truncation.
        // The method will produce a character, we just verify it doesn't throw and returns something.
        String result = Entities.unescape("&#128512;");
        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals((char)128512, result.charAt(0)); // char cast truncates, but char is 16-bit, so it's (char)128512 = 0xD83D? Wait: 128512 in hex is 0x1F600. Casting to char takes lower 16 bits: 0xF600 = 62976? Actually 0x1F600 & 0xFFFF = 0xF600. So char is '\uF600'. That's a valid char. So we can assert.
        assertEquals('\uF600', result.charAt(0));
    }

    @Test
    public void testUnescapeMixedEntitiesAndText() {
        String input = "Hello &amp; world &#38; &lt;";
        String expected = "Hello & world & <";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test(expected = NullPointerException.class)
    public void testUnescapeNullString() {
        Entities.unescape(null);
    }

    @Test
    public void testUnescapeAmpersandOnly() {
        // "&" alone should not match pattern, so unchanged
        assertEquals("&", Entities.unescape("&"));
    }

    @Test
    public void testUnescapeAmpersandWithSpace() {
        assertEquals("& ", Entities.unescape("& "));
    }

    @Test
    public void testUnescapeAmpersandSemicolon() {
        assertEquals("&;", Entities.unescape("&;"));
    }

    @Test
    public void testUnescapeHashSemicolon() {
        assertEquals("&#;", Entities.unescape("&#;"));
    }

    @Test
    public void testUnescapeHashXSemicolon() {
        assertEquals("&#x;", Entities.unescape("&#x;"));
    }

    @Test
    public void testUnescapeNamedEntityAtEnd() {
        assertEquals("&", Entities.unescape("&amp"));
    }

    @Test
    public void testUnescapeMultipleEntitiesNoSemicolons() {
        assertEquals("&<", Entities.unescape("&amp&lt"));
    }

    // --- static maps tests ---

    @Test
    public void testFullMapContainsKnownEntity() throws Exception {
        java.lang.reflect.Field field = Entities.class.getDeclaredField("full");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Character> full = (Map<String, Character>) field.get(null);
        assertNotNull(full);
        assertTrue(full.containsKey("amp"));
        assertEquals(Character.valueOf('&'), full.get("amp"));
    }

    @Test
    public void testBaseByValMapContainsKnownEntity() throws Exception {
        java.lang.reflect.Field field = Entities.class.getDeclaredField("baseByVal");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Character, String> baseByVal = (Map<Character, String>) field.get(null);
        assertNotNull(baseByVal);
        assertTrue(baseByVal.containsKey('&'));
        assertEquals("amp", baseByVal.get('&'));
    }

    @Test
    public void testFullByValMapContainsKnownEntity() throws Exception {
        java.lang.reflect.Field field = Entities.class.getDeclaredField("fullByVal");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Character, String> fullByVal = (Map<Character, String>) field.get(null);
        assertNotNull(fullByVal);
        assertTrue(fullByVal.containsKey('&'));
        assertEquals("amp", fullByVal.get('&'));
        // check a character only in full
        assertTrue(fullByVal.containsKey('\u03B1')); // alpha
        assertEquals("alpha", fullByVal.get('\u03B1'));
    }

    @Test
    public void testBaseByValDoesNotContainExtendedOnly() throws Exception {
        java.lang.reflect.Field field = Entities.class.getDeclaredField("baseByVal");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Character, String> baseByVal = (Map<Character, String>) field.get(null);
        assertFalse(baseByVal.containsKey('\u03B1')); // alpha not in base
    }

    @Test
    public void testFullMapSize() throws Exception {
        java.lang.reflect.Field field = Entities.class.getDeclaredField("full");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Character> full = (Map<String, Character>) field.get(null);
        // fullArray length is 2125? Actually count entries: we can just check it's > 0
        assertTrue(full.size() > 100);
    }
}
