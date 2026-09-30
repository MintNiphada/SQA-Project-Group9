package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class EntitiesTest {

    @Test
    public void testConstructor() {
        Entities entities = new Entities();
        Assert.assertNotNull(entities);
    }

    @Test
    public void testEscapeModeEnum() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        Assert.assertEquals(2, modes.length);
        Assert.assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        Assert.assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    @Test
    public void testEscapeWithOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("ascii");
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "<foo & bar \" ' \u00A0 \u03C0>";
        String escaped = Entities.escape(input, settings);
        Assert.assertEquals("&lt;foo &amp; bar &quot; ' &nbsp; &#960;&gt;", escaped);
    }

    @Test
    public void testEscapeBaseModeAscii() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "< \" & > \u00A0 \u00C0 \u03A9";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        Assert.assertEquals("&lt; &quot; &amp; &gt; &nbsp; &Agrave; &#937;", escaped);
    }

    @Test
    public void testEscapeExtendedModeAscii() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "< \" & > \u00A0 \u03A9 \u00C0";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);
        Assert.assertEquals("&lt; &quot; &amp; &gt; &NonBreakingSpace; &Omega; &Agrave;", escaped);
    }

    @Test
    public void testEscapeUtf8Encoding() {
        CharsetEncoder utfEncoder = Charset.forName("UTF-8").newEncoder();
        String input = "Hello <world> & \"quotes\" \u03A9 \u4e2d\u6587";
        String escapedBase = Entities.escape(input, utfEncoder, Entities.EscapeMode.base);
        Assert.assertEquals("Hello &lt;world&gt; &amp; &quot;quotes&quot; \u03A9 \u4e2d\u6587", escapedBase);

        String escapedExt = Entities.escape(input, utfEncoder, Entities.EscapeMode.extended);
        Assert.assertEquals("Hello &lt;world&gt; &amp; &quot;quotes&quot; &Omega; \u4e2d\u6587", escapedExt);
    }

    @Test
    public void testEscapeEmptyString() {
        CharsetEncoder utfEncoder = Charset.forName("UTF-8").newEncoder();
        Assert.assertEquals("", Entities.escape("", utfEncoder, Entities.EscapeMode.base));
        Assert.assertEquals("", Entities.escape("", utfEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapePlainCharacters() {
        CharsetEncoder utfEncoder = Charset.forName("UTF-8").newEncoder();
        String plain = "abcXYZ123!@#$^*()_-+=";
        Assert.assertEquals(plain, Entities.escape(plain, utfEncoder, Entities.EscapeMode.base));
    }

    @Test
    public void testUnescapeNoAmpersand() {
        String plain = "This is a string without any entity.";
        Assert.assertSame(plain, Entities.unescape(plain));
    }

    @Test
    public void testUnescapeNamedEntities() {
        String input = "&lt;div&gt;&quot;Hello &amp; Welcome&quot;&lt;/div&gt; &copy; &reg; &trade; &AElig; &aring;";
        String expected = "<div>\"Hello & Welcome\"</div> \u00A9 \u00AE \u2122 \u00C6 \u00E5";
        Assert.assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeNamedEntitiesWithoutSemicolon() {
        String input = "&amp &lt &gt &quot &copy";
        String expected = "& < > \" \u00A9";
        Assert.assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeDecimalEntities() {
        String input = "&#65;&#66;&#67; &#38; &#60;&#62;";
        String expected = "ABC & <>";
        Assert.assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeHexEntities() {
        String inputLower = "&#x41;&#x42;&#x43; &#x26;";
        String expected = "ABC &";
        Assert.assertEquals(expected, Entities.unescape(inputLower));

        String inputUpper = "&#X41;&#X42;&#X43; &#X26;";
        Assert.assertEquals(expected, Entities.unescape(inputUpper));
    }

    @Test
    public void testUnescapeUnknownNamedEntity() {
        String input = "&notAnEntity; &unknown; &123;";
        String unescaped = Entities.unescape(input);
        Assert.assertEquals("&notAnEntity; &unknown; &123;", unescaped);
    }

    @Test
    public void testUnescapeInvalidNumericEntity() {
        String input = "&#xZZ; &#; &##;";
        String unescaped = Entities.unescape(input);
        Assert.assertEquals("&#xZZ; &#; &##;", unescaped);
    }

    @Test
    public void testUnescapeMixedText() {
        String input = "foo &amp; bar &#38; baz &#x26; qux &lt;span&gt;test&lt;/span&gt;";
        String expected = "foo & bar & baz & qux <span>test</span>";
        Assert.assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEmptyString() {
        Assert.assertEquals("", Entities.unescape(""));
    }
}
