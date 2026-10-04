package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("nonExistentEntity123"));
        assertFalse(Entities.isNamedEntity(""));
    }

    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntity123"));
    }

    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonExistentEntity123"));
    }

    @Test
    public void testEscapeModeEnum() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(3, modes.length);
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
    }

    @Test
    public void testEscapeBasicUtf8() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "<foo & bar > \"baz\"";
        String escaped = Entities.escape(input, settings);
        assertEquals("&lt;foo &amp; bar &gt; \"baz\"", escaped);
    }

    @Test
    public void testEscapeInAttribute() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "<foo & bar > \"baz\"", settings, true, false, false);
        assertEquals("<foo &amp; bar > &quot;baz&quot;", accum.toString());
    }

    @Test
    public void testEscapeNbspModes() {
        Document.OutputSettings settingsXhtml = new Document.OutputSettings();
        settingsXhtml.charset("UTF-8");
        settingsXhtml.escapeMode(Entities.EscapeMode.xhtml);

        Document.OutputSettings settingsBase = new Document.OutputSettings();
        settingsBase.charset("UTF-8");
        settingsBase.escapeMode(Entities.EscapeMode.base);

        String nbspStr = "\u00A0";
        assertEquals("&#xa0;", Entities.escape(nbspStr, settingsXhtml));
        assertEquals("&nbsp;", Entities.escape(nbspStr, settingsBase));
    }

    @Test
    public void testEscapeAsciiCharset() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "© \u00C5 \u03A9";
        String escaped = Entities.escape(input, settings);
        assertEquals("&copy; &Aring; &#x3a9;", escaped);
    }

    @Test
    public void testEscapeFallbackCharset() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("ISO-8859-1"));
        settings.escapeMode(Entities.EscapeMode.extended);

        String input = "© \u03A9";
        String escaped = Entities.escape(input, settings);
        assertEquals("© &Omega;", escaped);
    }

    @Test
    public void testEscapeSupplementaryCharacters() {
        Document.OutputSettings utfSettings = new Document.OutputSettings();
        utfSettings.charset("UTF-8");

        Document.OutputSettings asciiSettings = new Document.OutputSettings();
        asciiSettings.charset("US-ASCII");

        String emoji = "\uD83D\uDE00";
        assertEquals(emoji, Entities.escape(emoji, utfSettings));
        assertEquals("&#x1f600;", Entities.escape(emoji, asciiSettings));
    }

    @Test
    public void testEscapeWhitespaceNormalisation() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");

        StringBuilder accum1 = new StringBuilder();
        Entities.escape(accum1, "   hello   world   ", settings, false, true, true);
        assertEquals("hello world ", accum1.toString());

        StringBuilder accum2 = new StringBuilder();
        Entities.escape(accum2, "   hello   world   ", settings, false, true, false);
        assertEquals(" hello world ", accum2.toString());

        StringBuilder accum3 = new StringBuilder();
        Entities.escape(accum3, "hello world", settings, false, true, false);
        assertEquals("hello world", accum3.toString());
    }

    @Test
    public void testUnescape() {
        String escaped = "&lt;hello &amp; &quot;world&quot;&gt; &nbsp;";
        String unescaped = Entities.unescape(escaped);
        assertEquals("<hello & \"world\"> \u00A0", unescaped);
    }

    @Test
    public void testUnescapeStrict() {
        String text = "&amp &amp;";
        assertEquals("& &", Entities.unescape(text, false));
        assertEquals("&amp &", Entities.unescape(text, true));
    }
}
