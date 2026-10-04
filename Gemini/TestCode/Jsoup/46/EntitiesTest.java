package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

public class EntitiesTest {

    @Test
    public void testIsNamedEntity() {
        Assert.assertTrue(Entities.isNamedEntity("lt"));
        Assert.assertTrue(Entities.isNamedEntity("gt"));
        Assert.assertTrue(Entities.isNamedEntity("amp"));
        Assert.assertTrue(Entities.isNamedEntity("quot"));
        Assert.assertFalse(Entities.isNamedEntity("nonExistentEntityXYZ"));
    }

    @Test
    public void testIsBaseNamedEntity() {
        Assert.assertTrue(Entities.isBaseNamedEntity("lt"));
        Assert.assertTrue(Entities.isBaseNamedEntity("amp"));
        Assert.assertTrue(Entities.isBaseNamedEntity("quot"));
        Assert.assertFalse(Entities.isBaseNamedEntity("NotEqual"));
        Assert.assertFalse(Entities.isBaseNamedEntity("nonExistentEntityXYZ"));
    }

    @Test
    public void testGetCharacterByName() {
        Assert.assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        Assert.assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        Assert.assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        Assert.assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        Assert.assertNull(Entities.getCharacterByName("nonExistentEntityXYZ"));
    }

    @Test
    public void testEscapeModeMaps() {
        Assert.assertNotNull(Entities.EscapeMode.xhtml.getMap());
        Assert.assertNotNull(Entities.EscapeMode.base.getMap());
        Assert.assertNotNull(Entities.EscapeMode.extended.getMap());
        Assert.assertEquals(3, Entities.EscapeMode.values().length);
        Assert.assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
    }

    @Test
    public void testEscapeBasic() {
        Document.OutputSettings settings = new Document.OutputSettings();
        String escaped = Entities.escape("<foo & bar > \"baz\"", settings);
        Assert.assertEquals("&lt;foo &amp; bar &gt; \"baz\"", escaped);
    }

    @Test
    public void testEscapeInAttribute() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder sb = new StringBuilder();
        Entities.escape(sb, "<foo & bar > \"baz\"", settings, true, false, false);
        Assert.assertEquals("<foo &amp; bar > &quot;baz&quot;", sb.toString());
    }

    @Test
    public void testEscapeNonAsciiUtf8() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);
        String escaped = Entities.escape("Hello \u00c5ngstr\u00f6m", settings);
        Assert.assertEquals("Hello \u00c5ngstr\u00f6m", escaped);
    }

    @Test
    public void testEscapeNonAsciiAsciiMode() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);
        String escaped = Entities.escape("Hello \u00c5ngstr\u00f6m \u00a2", settings);
        Assert.assertEquals("Hello &Aring;ngstr&ouml; &cent;", escaped);
    }

    @Test
    public void testEscapeNonAsciiFallbackCharset() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("ISO-8859-1"));
        settings.escapeMode(Entities.EscapeMode.base);
        String escaped = Entities.escape("Hello \u00c5 \u20ac", settings);
        Assert.assertTrue(escaped.contains("&euro;") || escaped.contains("&#x20ac;"));
    }

    @Test
    public void testEscapeNbsp() {
        Document.OutputSettings settingsXhtml = new Document.OutputSettings();
        settingsXhtml.escapeMode(Entities.EscapeMode.xhtml);
        settingsXhtml.charset("UTF-8");
        Assert.assertEquals("\u00a0", Entities.escape("\u00a0", settingsXhtml));

        Document.OutputSettings settingsBase = new Document.OutputSettings();
        settingsBase.escapeMode(Entities.EscapeMode.base);
        Assert.assertEquals("&nbsp;", Entities.escape("\u00a0", settingsBase));
    }

    @Test
    public void testEscapeWhitespaceNormalisation() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder sb1 = new StringBuilder();
        Entities.escape(sb1, "   foo   bar   ", settings, false, true, true);
        Assert.assertEquals("foo bar ", sb1.toString());

        StringBuilder sb2 = new StringBuilder();
        Entities.escape(sb2, "   foo   bar   ", settings, false, true, false);
        Assert.assertEquals(" foo bar ", sb2.toString());
    }

    @Test
    public void testEscapeSupplementaryCharacters() {
        Document.OutputSettings utfSettings = new Document.OutputSettings();
        utfSettings.charset("UTF-8");
        String emoji = "\uD83D\uDE00";
        Assert.assertEquals(emoji, Entities.escape(emoji, utfSettings));

        Document.OutputSettings asciiSettings = new Document.OutputSettings();
        asciiSettings.charset("US-ASCII");
        Assert.assertEquals("&#x1f600;", Entities.escape(emoji, asciiSettings));
    }

    @Test
    public void testEscapeUnmappedCharactersInAscii() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.xhtml);
        String escaped = Entities.escape("\u03c0", settings);
        Assert.assertEquals("&#x3c0;", escaped);
    }

    @Test
    public void testUnescape() {
        Assert.assertEquals("<foo & bar > \"baz\"", Entities.unescape("&lt;foo &amp; bar &gt; &quot;baz&quot;"));
        Assert.assertEquals("Hello &notanentity; World", Entities.unescape("Hello &notanentity; World"));
        Assert.assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeStrict() {
        Assert.assertEquals("&amp", Entities.unescape("&amp", true));
        Assert.assertEquals("&", Entities.unescape("&amp", false));
        Assert.assertEquals("&", Entities.unescape("&amp;", true));
    }
}
