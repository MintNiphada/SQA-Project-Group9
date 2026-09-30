package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testEscapeModeEnum() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(2, modes.length);
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    @Test
    public void testConstructor() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }

    @Test
    public void testEscapeWithDocumentOutputSettings() {
        Document doc = new Document("");
        doc.outputSettings().charset("ascii");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);

        String escaped = Entities.escape("& < > \" ' \u00a0 \u03a9", doc.outputSettings());
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
        assertTrue(escaped.contains("&quot;"));
        assertTrue(escaped.contains("&nbsp;"));
        // Omega character is not in baseByVal and not in ASCII encoder, so numeric escape &#937;
        assertTrue(escaped.contains("&#937;"));

        doc.outputSettings().escapeMode(Entities.EscapeMode.extended);
        String escapedExt = Entities.escape("\u03a9", doc.outputSettings());
        assertEquals("&omega;", escapedExt);
    }

    @Test
    public void testEscapeBaseModeWithAsciiEncoder() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

        String input = "Hello & < > \" \u00a0 World \u0102";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);

        assertEquals("Hello &amp; &lt; &gt; &quot; &nbsp; World &#258;", escaped);
    }

    @Test
    public void testEscapeExtendedModeWithAsciiEncoder() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

        String input = "Alpha \u0391 & \u00a0";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);

        assertEquals("Alpha &alpha; &amp; &nbsp;", escaped);
    }

    @Test
    public void testEscapeWithUtf8Encoder() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();

        // Non-mapped character can be encoded by UTF-8 encoder directly without numeric escaping
        String input = "Normal text 123 !@# $ \u4e16\u754c & < >";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);

        assertEquals("Normal text 123 !@# $ \u4e16\u754c &amp; &lt; &gt;", escaped);
    }

    @Test
    public void testEscapeEmptyString() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String escaped = Entities.escape("", asciiEncoder, Entities.EscapeMode.base);
        assertEquals("", escaped);
    }

    @Test
    public void testUnescapeNoAmpersand() {
        String input = "Hello World, no entities here!";
        String unescaped = Entities.unescape(input);
        assertSame(input, unescaped);
    }

    @Test
    public void testUnescapeNamedEntities() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("\u00a0", Entities.unescape("&nbsp;"));
        assertEquals("\u00a9", Entities.unescape("&copy;"));
        assertEquals("\u00e5", Entities.unescape("&aring;"));
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
    }

    @Test
    public void testUnescapeDecimalEntities() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("a", Entities.unescape("&#97;"));
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("Z", Entities.unescape("&#90"));
    }

    @Test
    public void testUnescapeHexEntities() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("a", Entities.unescape("&#x61;"));
        assertEquals("B", Entities.unescape("&#X42;"));
        assertEquals("b", Entities.unescape("&#X62;"));
        assertEquals("!", Entities.unescape("&#x21"));
    }

    @Test
    public void testUnescapeUnknownNamedEntity() {
        String input = "This is &unknownentity; and &foobar;";
        String unescaped = Entities.unescape(input);
        assertEquals("This is &unknownentity; and &foobar;", unescaped);
    }

    @Test
    public void testUnescapeInvalidNumberFormat() {
        String input = "Huge &#999999999999999999999999999999999999; number";
        String unescaped = Entities.unescape(input);
        assertEquals("Huge &#999999999999999999999999999999999999; number", unescaped);
    }

    @Test
    public void testUnescapeMixedContent() {
        String input = "Prefix &lt;tag attr=\"&quot;&#65;&#x42;&copy;&amp;\"&gt; &unknown; Suffix";
        String unescaped = Entities.unescape(input);
        assertEquals("Prefix <tag attr=\"\"AB\u00a9&\"> &unknown; Suffix", unescaped);
    }

    @Test
    public void testUnescapeBoundaryAndMultipleEntities() {
        String input = "&amp;&lt;&gt;&quot;&#65;&#x41;";
        String unescaped = Entities.unescape(input);
        assertEquals("&<>\"AA", unescaped);
    }
}
