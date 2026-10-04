package org.apache.commons.lang3.text.translate;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    @Test
    public void testTranslateDecimalEntity() throws IOException {
        String input = "&#65;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslateHexLowerCaseEntity() throws IOException {
        String input = "&#x41;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslateHexUpperCaseEntity() throws IOException {
        String input = "&#X41;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslateNonAmpersand() throws IOException {
        String input = "test";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateAmpersandWithoutHash() throws IOException {
        String input = "&amp;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateInvalidDecimalNumberFormat() throws IOException {
        String input = "&#xyz;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateInvalidHexNumberFormat() throws IOException {
        String input = "&#xZZ;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyEntityValue() throws IOException {
        String input = "&#;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyHexEntityValue() throws IOException {
        String input = "&#x;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEntityInMiddleOfString() throws IOException {
        String input = "Prefix&#66;Suffix";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 6, writer);

        assertEquals(5, consumed);
        assertEquals("B", writer.toString());
    }

    @Test
    public void testFullStringTranslation() {
        String input = "Hello &#65;&#x42;&#X43; World!";
        String result = unescaper.translate(input);
        assertEquals("Hello ABC World!", result);
    }

    @Test
    public void testFullStringTranslationWithNoEntities() {
        String input = "Plain text without entities.";
        String result = unescaper.translate(input);
        assertEquals(input, result);
    }

    @Test
    public void testFullStringTranslationWithNull() {
        assertEquals(null, unescaper.translate(null));
    }

    @Test
    public void testTranslateUnicodeCodepoint() throws IOException {
        String input = "&#x3042;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(8, consumed);
        assertEquals("\u3042", writer.toString());
    }

    @Test
    public void testTranslateLargeDecimalEntity() throws IOException {
        String input = "&#1234;";
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(7, consumed);
        assertEquals("\u04D2", writer.toString());
    }
}
