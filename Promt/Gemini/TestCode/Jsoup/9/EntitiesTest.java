package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testConstructor() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }

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
        assertTrue(baseMap.containsKey('©'));

        Map<Character, String> extendedMap = Entities.EscapeMode.extended.getMap();
        assertNotNull(extendedMap);
        assertTrue(extendedMap.containsKey('&'));
        assertTrue(extendedMap.containsKey('∞'));
    }

    @Test
    public void testEscapeWithOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("US-ASCII"));
        settings.escapeMode(Entities.EscapeMode.base);

        String escaped = Entities.escape("Hello & < > \" ' © \u00a9 \u0100", settings);
        assertEquals("Hello &amp; &lt; &gt; &quot; ' &copy; &copy; &#256;", escaped);
    }

    @Test
    public void testEscapeXhtml() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<div>\"Hello\" & 'World' © \u00C5</div>";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;div&gt;&quot;Hello&quot; &amp; &apos;World&apos; &#169; &#197;&lt;/div&gt;", escaped);
    }

    @Test
    public void testEscapeBase() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<a href=\"test.html?a=1&b=2\">'Quotes' & © & non-ascii: \u0100</a>";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&lt;a href=&quot;test.html?a=1&amp;b=2&quot;&gt;'Quotes' &amp; &copy; &amp; non-ascii: &#256;&lt;/a&gt;", escaped);
    }

    @Test
    public void testEscapeExtendedUtf8() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        String input = "Hello < & > \" ' © \u2265";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.extended);
        assertEquals("Hello &lt; &amp; &gt; &quot; ' &copy; &ge;", escaped);

        String inputNonMappedUtf8 = "Hello \u4e16\u754c";
        String escapedNonMapped = Entities.escape(inputNonMappedUtf8, utf8Encoder, Entities.EscapeMode.extended);
        assertEquals("Hello \u4e16\u754c", escapedNonMapped);
    }

    @Test
    public void testEscapeAsciiNonMapped() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u4e16\u754c";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&#19990;&#30028;", escaped);
    }

    @Test
    public void testUnescapeNoAmpersand() {
        String plain = "Just a plain string without entities.";
        assertSame(plain, Entities.unescape(plain));
    }

    @Test
    public void testUnescapeNamedEntities() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
        assertEquals("©", Entities.unescape("&copy;"));
        assertEquals("©", Entities.unescape("&COPY;"));
        assertEquals("≥", Entities.unescape("&ge;"));
        assertEquals("Hello <world> & 'peace' \"now\" ©",
                Entities.unescape("Hello &lt;world&gt; &amp; &apos;peace&apos; &quot;now&quot; &copy;"));
    }

    @Test
    public void testUnescapeDecimalEntities() {
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("&", Entities.unescape("&#38"));
        assertEquals("<", Entities.unescape("&#60;"));
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("©", Entities.unescape("&#169;"));
    }

    @Test
    public void testUnescapeHexEntities() {
        assertEquals("&", Entities.unescape("&#x26;"));
        assertEquals("&", Entities.unescape("&#x26"));
        assertEquals("&", Entities.unescape("&#X26;"));
        assertEquals("&", Entities.unescape("&#X26"));
        assertEquals("<", Entities.unescape("&#x3C;"));
        assertEquals("<", Entities.unescape("&#x3c;"));
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("©", Entities.unescape("&#xA9;"));
    }

    @Test
    public void testUnescapeUnknownOrInvalidEntities() {
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
        assertEquals("&unknown", Entities.unescape("&unknown"));
        assertEquals("&;", Entities.unescape("&;"));
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
        assertEquals("&#X;", Entities.unescape("&#X;"));

        // NumberFormatException on overflow
        assertEquals("&#9999999999999999999999999999;", Entities.unescape("&#9999999999999999999999999999;"));
        assertEquals("&#x9999999999999999999999999999;", Entities.unescape("&#x9999999999999999999999999999;"));
    }

    @Test
    public void testUnescapeMixedContent() {
        String input = "Text with &amp; &lt;tag&gt; and &#65; &#x42; and &unknown; plus &quot;quotes&quot; and &copy";
        String expected = "Text with & <tag> and A B and &unknown; plus \"quotes\" and ©";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeEmptyAndFull() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        assertEquals("", Entities.escape("", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("", Entities.unescape(""));
    }
}
