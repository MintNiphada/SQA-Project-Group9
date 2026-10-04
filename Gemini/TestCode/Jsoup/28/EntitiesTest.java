package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testEscapeModeEnum() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(3, modes.length);
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));

        Map<Character, String> xhtmlMap = Entities.EscapeMode.xhtml.getMap();
        assertNotNull(xhtmlMap);
        assertEquals("quot", xhtmlMap.get('"'));
        assertEquals("amp", xhtmlMap.get('&'));
        assertEquals("apos", xhtmlMap.get('\''));
        assertEquals("lt", xhtmlMap.get('<'));
        assertEquals("gt", xhtmlMap.get('>'));

        Map<Character, String> baseMap = Entities.EscapeMode.base.getMap();
        assertNotNull(baseMap);
        assertTrue(baseMap.containsKey('&'));
        assertTrue(baseMap.containsKey('<'));
        assertTrue(baseMap.containsKey('>'));
        assertTrue(baseMap.containsKey('"'));

        Map<Character, String> extendedMap = Entities.EscapeMode.extended.getMap();
        assertNotNull(extendedMap);
        assertTrue(extendedMap.size() > baseMap.size());
    }

    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("nbsp"));
        assertTrue(Entities.isNamedEntity("aring"));

        assertFalse(Entities.isNamedEntity("nonExistentEntity12345"));
        assertFalse(Entities.isNamedEntity(""));
        assertFalse(Entities.isNamedEntity("LT_invalid"));
    }

    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonExistentEntity12345"));
        assertNull(Entities.getCharacterByName(""));
    }

    @Test
    public void testEscapeWithOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);

        String text = "Hello & < > \" ' \u00A0 World";
        String escaped = Entities.escape(text, settings);
        assertEquals("Hello &amp; &lt; &gt; &quot; ' &nbsp; World", escaped);
    }

    @Test
    public void testEscapeWithEncoderAndModes() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

        String input = "<foo & bar> \"quote\" \u00E9 \u4e2d";

        // UTF-8 with base mode
        String escapedUtf8Base = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);
        assertEquals("&lt;foo &amp; bar&gt; &quot;quote&quot; &eacute; \u4e2d", escapedUtf8Base);

        // ASCII with base mode: unmappable non-base characters become numeric entities
        String escapedAsciiBase = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&lt;foo &amp; bar&gt; &quot;quote&quot; &eacute; &#20013;", escapedAsciiBase);

        // UTF-8 with xhtml mode: only lt, gt, amp, apos, quot mapped
        String escapedUtf8Xhtml = Entities.escape(input, utf8Encoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;foo &amp; bar&gt; &quot;quote&quot; \u00E9 \u4e2d", escapedUtf8Xhtml);

        // ASCII with xhtml mode
        String escapedAsciiXhtml = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;foo &amp; bar&gt; &quot;quote&quot; &#233; &#20013;", escapedAsciiXhtml);

        // Extended mode
        String escapedExtended = Entities.escape(input, utf8Encoder, Entities.EscapeMode.extended);
        assertEquals("&lt;foo &amp; bar&gt; &quot;quote&quot; &eacute; \u4e2d", escapedExtended);
    }

    @Test
    public void testEscapeEmptyAndPlainStrings() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
        assertEquals("abc 123", Entities.escape("abc 123", encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testUnescapeBasic() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
        assertEquals("\u00A0", Entities.unescape("&nbsp;"));
        assertEquals("& & < > \"", Entities.unescape("&amp; &amp; &lt; &gt; &quot;"));
    }

    @Test
    public void testUnescapeNoAmpersand() {
        String plain = "This is a simple plain text with no entities.";
        assertSame(plain, Entities.unescape(plain));
        assertSame(plain, Entities.unescape(plain, true));
        assertSame(plain, Entities.unescape(plain, false));
    }

    @Test
    public void testUnescapeNumericEntities() {
        // Decimal entities
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("<", Entities.unescape("&#60;"));
        assertEquals(">", Entities.unescape("&#62;"));
        assertEquals("A", Entities.unescape("&#65;"));

        // Hex entities (lowercase and uppercase 'x')
        assertEquals("&", Entities.unescape("&#x26;"));
        assertEquals("&", Entities.unescape("&#X26;"));
        assertEquals("<", Entities.unescape("&#x3c;"));
        assertEquals("<", Entities.unescape("&#x3C;"));
        assertEquals(">", Entities.unescape("&#x3e;"));
        assertEquals("\u00E9", Entities.unescape("&#xe9;"));
        assertEquals("\u4e2d", Entities.unescape("&#x4e2d;"));
    }

    @Test
    public void testUnescapeStrictVsNonStrict() {
        // Strict requires trailing semicolon
        assertEquals("&lt", Entities.unescape("&lt", true));
        assertEquals("<", Entities.unescape("&lt", false));

        assertEquals("&#65", Entities.unescape("&#65", true));
        assertEquals("A", Entities.unescape("&#65", false));

        assertEquals("&#x41", Entities.unescape("&#x41", true));
        assertEquals("A", Entities.unescape("&#x41", false));

        assertEquals("&lt;foo", Entities.unescape("&lt;foo", true));
        assertEquals("<foo", Entities.unescape("&lt;foo", true));
        assertEquals("<foo", Entities.unescape("&ltfoo", false));
    }

    @Test
    public void testUnescapeInvalidAndUnknownEntities() {
        // Unknown named entity
        assertEquals("&unknownentity;", Entities.unescape("&unknownentity;"));
        assertEquals("&unknownentity;", Entities.unescape("&unknownentity;", true));

        // Incomplete / standalone ampersands
        assertEquals("&", Entities.unescape("&"));
        assertEquals("foo & bar", Entities.unescape("foo & bar"));
        assertEquals("foo && bar", Entities.unescape("foo && bar"));
        assertEquals("& ;", Entities.unescape("& ;"));

        // Invalid numeric formats
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
    }

    @Test
    public void testUnescapeMixedText() {
        String input = "Hello &lt;world&gt;! &#38; Welcome to &#x22;jsoup&#x22; & unknown &amp; more.";
        String expected = "Hello <world>! & Welcome to \"jsoup\" & unknown & more.";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeConsecutiveEntities() {
        String input = "&lt;&gt;&amp;&quot;&apos;&#65;&#x42;";
        String expected = "<>&\"'AB";
        assertEquals(expected, Entities.unescape(input));
    }
}
