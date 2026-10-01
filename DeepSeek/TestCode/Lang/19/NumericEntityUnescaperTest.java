package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper = new NumericEntityUnescaper();

    private int translate(String input, int index, StringWriter out) throws IOException {
        return unescaper.translate(input, index, out);
    }

    @Test
    public void testDecimalEntity() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#65;", 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testHexEntityLower() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x41;", 0, out);
        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testHexEntityUpper() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#X41;", 0, out);
        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDecimalEntityWithoutSemicolonThrows() throws IOException {
        translate("&#65", 0, new StringWriter());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testHexEntityWithoutSemicolonThrows() throws IOException {
        translate("&#x41", 0, new StringWriter());
    }

    @Test
    public void testInvalidDecimalEntityReturnsZero() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#12G;", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test
    public void testInvalidHexEntityReturnsZero() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#xGG;", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test
    public void testSupplementaryCharacter() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x1F600;", 0, out);
        assertEquals(9, consumed); // &#x1F600; length 9
        String expected = new String(Character.toChars(0x1F600));
        assertEquals(expected, out.toString());
    }

    @Test
    public void testBoundaryFFFF() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#65535;", 0, out);
        assertEquals(8, consumed); // &#65535; length 8
        assertEquals(1, out.toString().length());
        assertEquals(65535, out.toString().charAt(0));
    }

    @Test
    public void testBoundary10000() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x10000;", 0, out);
        assertEquals(9, consumed); // &#x10000; length 9
        String expected = new String(Character.toChars(0x10000));
        assertEquals(expected, out.toString());
    }

    @Test
    public void testNoEntity() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("abc", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test
    public void testAmpersandOnly() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAmpersandHashOnlyThrows() throws IOException {
        translate("&#", 0, new StringWriter());
    }

    @Test
    public void testAmpersandHashSemicolon() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#;", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test
    public void testAmpersandHashXSemicolon() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x;", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testEntityAtEndNoSemicolon() throws IOException {
        translate("&#65", 0, new StringWriter());
    }

    @Test
    public void testEntityWithSemicolonLater() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#65;rest", 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testEntityValueZero() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#0;", 0, out);
        assertEquals(4, consumed);
        assertEquals(1, out.toString().length());
        assertEquals(0, out.toString().charAt(0));
    }

    @Test
    public void testHexEntityValueZero() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x0;", 0, out);
        assertEquals(5, consumed);
        assertEquals(1, out.toString().length());
        assertEquals(0, out.toString().charAt(0));
    }

    @Test
    public void testEntityWithLeadingZeros() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#00065;", 0, out);
        assertEquals(8, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testHexEntityWithLeadingZeros() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x00041;", 0, out);
        assertEquals(9, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testEntityMaxUnicode() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#x10FFFF;", 0, out);
        assertEquals(10, consumed); // &#x10FFFF; length 10
        String expected = new String(Character.toChars(0x10FFFF));
        assertEquals(expected, out.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntityAboveMaxUnicodeThrows() throws IOException {
        translate("&#x110000;", 0, new StringWriter());
    }

    @Test
    public void testNegativeEntityValue() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&#-1;", 0, out);
        assertEquals(5, consumed); // &#-1; length 5
        assertEquals(1, out.toString().length());
        assertEquals((char) -1, out.toString().charAt(0));
    }

    @Test
    public void testEntityAtNonZeroIndex() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("a&#65;b", 1, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    @Test
    public void testIndexAtLastCharacter() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test
    public void testIndexAtSecondLastCharacterNotHash() throws IOException {
        StringWriter out = new StringWriter();
        int consumed = translate("&a", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }
}
