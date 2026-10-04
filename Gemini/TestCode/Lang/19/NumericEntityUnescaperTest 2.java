package org.apache.commons.lang3.text.translate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    @Test
    public void testTranslateDecimalEntity() throws IOException {
        String input = "&#65;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        
        Assert.assertEquals(5, consumed);
        Assert.assertEquals("A", out.toString());
    }

    @Test
    public void testTranslateHexEntityLowerCase() throws IOException {
        String input = "&#x41;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        
        Assert.assertEquals(6, consumed);
        Assert.assertEquals("A", out.toString());
    }

    @Test
    public void testTranslateHexEntityUpperCase() throws IOException {
        String input = "&#X41;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        
        Assert.assertEquals(6, consumed);
        Assert.assertEquals("A", out.toString());
    }

    @Test
    public void testTranslateSupplementaryCharacterDecimal() throws IOException {
        // Codepoint 119070 (0x1D11E - Musical Symbol G Clef)
        String input = "&#119070;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(9, consumed);
        char[] expectedChars = Character.toChars(119070);
        Assert.assertEquals(new String(expectedChars), out.toString());
    }

    @Test
    public void testTranslateSupplementaryCharacterHex() throws IOException {
        // Codepoint 0x1F600 (Grinning Face)
        String input = "&#x1F600;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(9, consumed);
        char[] expectedChars = Character.toChars(0x1F600);
        Assert.assertEquals(new String(expectedChars), out.toString());
    }

    @Test
    public void testTranslateMaxBoundaryCodePoint() throws IOException {
        // Maximum Unicode Code Point: 0x10FFFF
        String input = "&#x10FFFF;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(10, consumed);
        char[] expectedChars = Character.toChars(0x10FFFF);
        Assert.assertEquals(new String(expectedChars), out.toString());
    }

    @Test
    public void testTranslateCharAtBoundary0xFFFF() throws IOException {
        // BMP max boundary: 0xFFFF
        String input = "&#xFFFF;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(8, consumed);
        Assert.assertEquals(String.valueOf((char) 0xFFFF), out.toString());
    }

    @Test
    public void testTranslateNonEntityStartingWithAmpersand() throws IOException {
        String input = "&amp;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateAmpersandAtEndOfString() throws IOException {
        String input = "test&";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 4, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateRegularCharacter() throws IOException {
        String input = "Hello";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateInvalidNumberFormatDecimal() throws IOException {
        String input = "&#INVALID;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateInvalidNumberFormatHex() throws IOException {
        String input = "&#xZZZ;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateEmptyEntityDecimal() throws IOException {
        String input = "&#;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateEmptyEntityHex() throws IOException {
        String input = "&#x;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);

        Assert.assertEquals(0, consumed);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateFullStringUsingInheritedTranslate() {
        String input = "Prefix &#65; middle &#x42; and &#X43; suffix.";
        String expected = "Prefix A middle B and C suffix.";
        String result = unescaper.translate(input);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testTranslateStringWithoutEntities() {
        String input = "Plain text without entities & and &foo;";
        String result = unescaper.translate(input);

        Assert.assertEquals(input, result);
    }

    @Test
    public void testTranslateNullInput() {
        String result = unescaper.translate(null);
        Assert.assertNull(result);
    }

    @Test
    public void testTranslateIndexMiddleOfSequence() throws IOException {
        String input = "abc&#66;def";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 3, out);

        Assert.assertEquals(5, consumed);
        Assert.assertEquals("B", out.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testTranslateMissingSemicolonThrowsException() throws IOException {
        // The while loop scans until ';' and throws IndexOutOfBoundsException if absent
        String input = "&#65";
        StringWriter out = new StringWriter();
        unescaper.translate(input, 0, out);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testTranslateIncompletePrefixThrowsException() throws IOException {
        // String has '&#', but no following character
        String input = "&#";
        StringWriter out = new StringWriter();
        unescaper.translate(input, 0, out);
    }
}
