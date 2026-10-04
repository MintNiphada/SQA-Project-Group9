package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class EntitiesTest {

    @Test
    public void testIsNamedEntityTrue() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
    }

    @Test
    public void testIsNamedEntityFalse() {
        assertFalse(Entities.isNamedEntity("unknown"));
        assertFalse(Entities.isNamedEntity(""));
    }

    @Test(expected = NullPointerException.class)
    public void testIsNamedEntityNull() {
        Entities.isNamedEntity(null);
    }

    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
    }

    @Test
    public void testGetCharacterByNameUnknown() {
        assertNull(Entities.getCharacterByName("unknown"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetCharacterByNameNull() {
        Entities.getCharacterByName(null);
    }

    @Test
    public void testEscapeXhtmlMode() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "&<>\"'";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.xhtml);
        assertEquals("&amp;&lt;&gt;&quot;&apos;", escaped);
    }

    @Test
    public void testEscapeBaseMode() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "&<>\"'";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("&amp;&lt;&gt;&quot;'", escaped);
    }

    @Test
    public void testEscapeExtendedMode() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "\u00A9";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.extended);
        assertEquals("&copy;", escaped);
    }

    @Test
    public void testEscapeNonEncodableChar() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u00A9";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("&#169;", escaped);
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
    public void testUnescapeNamedEntity() {
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeNumericEntityDecimal() {
        assertEquals("<", Entities.unescape("&#60;"));
    }

    @Test
    public void testUnescapeNumericEntityHex() {
        assertEquals("<", Entities.unescape("&#x3C;"));
        assertEquals("<", Entities.unescape("&#X3C;"));
    }

    @Test
    public void testUnescapeWithoutSemicolon() {
        assertEquals("<", Entities.unescape("&lt"));
        assertEquals("<", Entities.unescape("&#60"));
    }

    @Test
    public void testUnescapeStrictMode() {
        assertEquals("<", Entities.unescape("&lt;", true));
        assertEquals("&lt", Entities.unescape("&lt", true));
    }

    @Test
    public void testUnescapeUnknownEntity() {
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
    }

    @Test
    public void testUnescapeNoAmpersand() {
        String input = "hello";
        assertSame(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeMixedContent() {
        assertEquals("a<b>c&d\"e", Entities.unescape("a&lt;b&gt;c&amp;d&quot;e"));
    }

    @Test
    public void testUnescapeNumericOverflow() {
        assertEquals("\uFFFF", Entities.unescape("&#65535;"));
    }

    @Test(expected = NullPointerException.class)
    public void testUnescapeNull() {
        Entities.unescape(null);
    }

    @Test
    public void testEscapeModeMaps() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('<'));
        assertTrue(Entities.EscapeMode.base.getMap().containsKey('<'));
        assertTrue(Entities.EscapeMode.extended.getMap().containsKey('\u00A9'));
    }
}
