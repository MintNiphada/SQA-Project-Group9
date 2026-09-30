package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class EntitiesTest {

    // --- escape tests ---

    @Test
    public void testEscapeBaseMode() {
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        String input = "& < > \" '";
        // In base mode: & -> &amp; < -> &lt; > -> &gt; " -> &quot; ' is not in base map, but can be encoded -> stays '
        String expected = "&amp; &lt; &gt; &quot; '";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeExtendedMode() {
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        // alpha (α) is in full map but not base
        String input = "α &";
        // In extended mode: α -> &alpha; & -> &amp;
        String expected = "&alpha; &amp;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeWithNonEncodableChar() {
        // Use US-ASCII encoder, which cannot encode é (U+00E9)
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        String input = "é";
        // é is not in baseByVal, and encoder cannot encode it -> numeric escape
        String expected = "&#233;";
        assertEquals(expected, Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeWithUtf8Encoder() {
        // UTF-8 can encode almost everything, so characters not in map will be kept as is
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "é"; // not in baseByVal
        // Since encoder can encode é, it stays as é
        assertEquals("é", Entities.escape(input, encoder, Entities.EscapeMode.base));
    }

    @Test
    public void testEscapeEmptyString() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
    }

    @Test(expected = NullPointerException.class)
    public void testEscapeNullString() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        Entities.escape(null, encoder, Entities.EscapeMode.base);
    }

    @Test
    public void testEscapeWithOutputSettings() {
        // Create a Document.OutputSettings with specific encoder and mode
        Document.OutputSettings out = new Document.OutputSettings();
        out.encoder(StandardCharsets.US_ASCII.newEncoder());
        out.escapeMode(Entities.EscapeMode.base);
        String input = "&";
        String expected = "&amp;";
        assertEquals(expected, Entities.escape(input, out));
    }

    // --- unescape tests ---

    @Test
    public void testUnescapeNoAmpersand() {
        String input = "hello world";
        assertEquals(input, Entities.unescape(input));
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
    public void testUnescapeNamedEntity() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
    }

    @Test
    public void testUnescapeNamedEntityWithoutSemicolon() {
        // regex allows optional semicolon
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
    }

    @Test
    public void testUnescapeNumericDecimal() {
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("A", Entities.unescape("&#65;"));
    }

    @Test
    public void testUnescapeNumericHex() {
        assertEquals("&", Entities.unescape("&#x26;"));
        assertEquals("&", Entities.unescape("&#X26;"));
        assertEquals("A", Entities.unescape("&#x41;"));
    }

    @Test
    public void testUnescapeNumericWithoutSemicolon() {
        assertEquals("&", Entities.unescape("&#38"));
        assertEquals("&", Entities.unescape("&#x26"));
    }

    @Test
    public void testUnescapeMixedContent() {
        String input = "Hello &amp; World &lt;3";
        String expected = "Hello & World <3";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeUnknownEntity() {
        // unknown entity should remain unchanged
        String input = "&unknown;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeInvalidNumeric() {
        // NumberFormatException will be caught, charval remains -1, original match kept
        String input = "&#99999999999999999999;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test
    public void testUnescapeAmpersandOnly() {
        // "&" alone does not match the pattern, so it stays as is
        assertEquals("&", Entities.unescape("&"));
    }

    @Test
    public void testUnescapeCharvalOutOfRange() {
        // charval > 0xFFFF (e.g., 0x10000) will be cast to char, producing a truncated character
        // 0x10000 cast to char becomes 0x0000 (null character)
        String input = "&#x10000;";
        String result = Entities.unescape(input);
        // The result should be a string containing the null character
        assertEquals("\u0000", result);
    }

    @Test
    public void testUnescapeMultipleEntities() {
        String input = "&amp; &lt; &gt;";
        String expected = "& < >";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeExtendedEntity() {
        // full map contains many entities, e.g., &alpha; -> α
        assertEquals("α", Entities.unescape("&alpha;"));
    }

    @Test
    public void testUnescapeExtendedEntityWithoutSemicolon() {
        assertEquals("α", Entities.unescape("&alpha"));
    }

    @Test
    public void testUnescapeNumericWithLeadingZeros() {
        assertEquals("&", Entities.unescape("&#00038;"));
        assertEquals("&", Entities.unescape("&#x00026;"));
    }

    @Test
    public void testEscapeBaseModeAllBaseEntities() {
        // Verify that all characters in baseByVal are escaped correctly
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        // Test a few known ones
        assertEquals("&amp;", Entities.escape("&", encoder, Entities.EscapeMode.base));
        assertEquals("&lt;", Entities.escape("<", encoder, Entities.EscapeMode.base));
        assertEquals("&gt;", Entities.escape(">", encoder, Entities.EscapeMode.base));
        assertEquals("&quot;", Entities.escape("\"", encoder, Entities.EscapeMode.base));
        // Test a character that is in base but not ASCII? Actually base entities are all ASCII.
    }

    @Test
    public void testEscapeExtendedModeAllFullEntities() {
        // Test that characters in fullByVal are escaped to named entities in extended mode
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        assertEquals("&alpha;", Entities.escape("α", encoder, Entities.EscapeMode.extended));
        assertEquals("&beta;", Entities.escape("β", encoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeBaseModeDoesNotEscapeExtendedEntities() {
        // In base mode, characters only in fullByVal (not baseByVal) should not be escaped to named entities
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        // α is not in baseByVal, and encoder (US-ASCII) cannot encode it -> numeric
        assertEquals("&#945;", Entities.escape("α", encoder, Entities.EscapeMode.base));
    }
}
