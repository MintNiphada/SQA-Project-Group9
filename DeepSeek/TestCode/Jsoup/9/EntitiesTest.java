package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class EntitiesTest {

    // --- EscapeMode enum tests ---

    @Test
    public void testEscapeModeMapsNotNull() {
        for (Entities.EscapeMode mode : Entities.EscapeMode.values()) {
            assertNotNull(mode.getMap());
        }
    }

    @Test
    public void testXhtmlMapContainsBasicEntities() {
        Map<Character, String> map = Entities.EscapeMode.xhtml.getMap();
        assertEquals("quot", map.get('"'));
        assertEquals("amp", map.get('&'));
        assertEquals("apos", map.get('\''));
        assertEquals("lt", map.get('<'));
        assertEquals("gt", map.get('>'));
        assertEquals(5, map.size());
    }

    @Test
    public void testBaseMapContainsCopy() {
        Map<Character, String> map = Entities.EscapeMode.base.getMap();
        assertEquals("copy", map.get('\u00A9'));
    }

    @Test
    public void testExtendedMapContainsOmega() {
        Map<Character, String> map = Entities.EscapeMode.extended.getMap();
        assertEquals("Omega", map.get('\u03A9'));
    }

    // --- Static maps tests ---

    @Test
    public void testFullMapContainsAmp() {
        assertEquals(Character.valueOf('&'), Entities.full.get("amp"));
    }

    @Test
    public void testXhtmlByValContainsLt() {
        assertEquals("lt", Entities.xhtmlByVal.get('<'));
    }

    @Test
    public void testBaseByValContainsCopy() {
        assertEquals("copy", Entities.baseByVal.get('\u00A9'));
    }

    @Test
    public void testFullByValContainsCopy() {
        assertEquals("copy", Entities.fullByVal.get('\u00A9'));
    }

    // --- escape(String, CharsetEncoder, EscapeMode) tests ---

    @Test(expected = NullPointerException.class)
    public void testEscapeNullStringThrowsNPE() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        Entities.escape(null, encoder, Entities.EscapeMode.base);
    }

    @Test
    public void testEscapeEmptyString() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeNoSpecialChars() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "hello";
        assertEquals("hello", Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeXhtmlMode() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "&<>\"'";
        String expected = "&amp;&lt;&gt;&quot;&apos;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.xhtml));
    }

    @Test
    public void testEscapeBaseModeEscapesCopy() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "\u00A9"; // copyright
        assertEquals("&copy;", Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeExtendedModeEscapesOmega() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "\u03A9"; // Omega
        assertEquals("&Omega;", Entities.escape(input, encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeWithAsciiEncoderNonAsciiChar() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u00A9"; // copyright, not ASCII
        // In base mode, copyright is in the map, so it should be escaped as &copy; even if encoder can't encode it.
        assertEquals("&copy;", Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeWithAsciiEncoderCharNotInMap() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u00E4"; // auml, not in xhtml map, and not ASCII
        // xhtml mode does not have auml, encoder cannot encode, so numeric entity
        assertEquals("&#228;", Entities.escape(input, encoder, Entities.EscapeMode.xhtml));
    }

    @Test
    public void testEscapeWithUtf8EncoderCharNotInMap() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "\u00E4"; // auml, not in xhtml map, but UTF-8 can encode
        assertEquals("\u00E4", Entities.escape(input, encoder, Entities.EscapeMode.xhtml));
    }

    @Test
    public void testEscapeMixedContent() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "a<b>&\"\u00A9";
        // base mode: <, &, " are in base map, copyright is in base map, 'a' is not
        String expected = "a&lt;b&gt;&amp;&quot;&copy;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    // --- escape(String, Document.OutputSettings) tests ---

    @Test
    public void testEscapeWithOutputSettingsDelegates() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.encoder(StandardCharsets.UTF_8.newEncoder());
        out.escapeMode(Entities.EscapeMode.xhtml);
        String input = "&";
        assertEquals("&amp;", Entities.escape(input, out));
    }

    @Test
    public void testEscapeWithOutputSettingsAsciiEncoder() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.encoder(Charset.forName("US-ASCII").newEncoder());
        out.escapeMode(Entities.EscapeMode.xhtml);
        String input = "\u00E4";
        assertEquals("&#228;", Entities.escape(input, out));
    }

    // --- unescape(String) tests ---

    @Test(expected = NullPointerException.class)
    public void testUnescapeNullThrowsNPE() {
        Entities.unescape(null);
    }

    @Test
    public void testUnescapeNoAmpersand() {
        assertEquals("hello", Entities.unescape("hello"));
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
    public void testUnescapeNumericDecimalWithSemicolon() {
        assertEquals("&", Entities.unescape("&#38;"));
    }

    @Test
    public void testUnescapeNumericDecimalWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&#38"));
    }

    @Test
    public void testUnescapeNumericHexLowerX() {
        assertEquals("&", Entities.unescape("&#x26;"));
    }

    @Test
    public void testUnescapeNumericHexUpperX() {
        assertEquals("&", Entities.unescape("&#X26;"));
    }

    @Test
    public void testUnescapeNumericHexWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&#x26"));
    }

    @Test
    public void testUnescapeMultipleEntities() {
        assertEquals("&<>", Entities.unescape("&amp;&lt;&gt;"));
    }

    @Test
    public void testUnescapeInvalidNamedEntity() {
        String input = "&invalid;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeInvalidNumericEntity() {
        String input = "&#xGHI;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeLargeNumericValue() {
        // value > 0xFFFF, should be truncated to char
        String input = "&#x110000;";
        String result = Entities.unescape(input);
        assertEquals(1, result.length());
        assertEquals((char) 0x10000, result.charAt(0)); // 0x110000 & 0xFFFF = 0x10000
    }

    @Test
    public void testUnescapeWithDollarSign() {
        // ensure replacement does not interpret $ as group reference
        assertEquals("& $1", Entities.unescape("&amp; $1"));
    }

    @Test
    public void testUnescapeWithBackslash() {
        assertEquals("& \\", Entities.unescape("&amp; \\"));
    }

    @Test
    public void testUnescapeMixedEntities() {
        assertEquals("& \u00A9", Entities.unescape("&amp; &copy;"));
    }

    @Test
    public void testUnescapeEntityAtEndOfString() {
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeEntityAtStartOfString() {
        assertEquals("&hello", Entities.unescape("&amp;hello"));
    }

    @Test
    public void testUnescapeConsecutiveEntities() {
        assertEquals("&&", Entities.unescape("&amp;&amp;"));
    }

    @Test
    public void testUnescapeNamedEntityWithTrailingAlphanumeric() {
        // "&amp1" should unescape to "&1" because the match is "&amp" (no semicolon, and '1' is not part of name)
        assertEquals("&1", Entities.unescape("&amp1"));
    }

    @Test
    public void testUnescapeNumericWithTrailingAlphanumeric() {
        assertEquals("&1", Entities.unescape("&#38;1"));
    }

    // --- Edge cases for static initializer ---

    @Test
    public void testFullMapSize() {
        // fullArray length is large, just check it's not empty
        assertFalse(Entities.full.isEmpty());
    }

    @Test
    public void testXhtmlByValSize() {
        assertEquals(5, Entities.xhtmlByVal.size());
    }

    @Test
    public void testBaseByValContainsLt() {
        assertEquals("lt", Entities.baseByVal.get('<'));
    }

    @Test
    public void testFullByValContainsAmp() {
        assertEquals("amp", Entities.fullByVal.get('&'));
    }
}
