package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper = new NumericEntityUnescaper();

    @Test
    public void testValidDecimalEntity() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#123;", 0, writer);
        assertEquals(6, consumed);
        assertEquals(String.valueOf((char) 123), writer.toString());
    }

    @Test
    public void testValidHexEntityLower() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x1A;", 0, writer);
        assertEquals(6, consumed);
        assertEquals(String.valueOf((char) 26), writer.toString());
    }

    @Test
    public void testValidHexEntityUpper() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#X1A;", 0, writer);
        assertEquals(6, consumed);
        assertEquals(String.valueOf((char) 26), writer.toString());
    }

    @Test
    public void testValidHexEntityLarge() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x10FFFF;", 0, writer);
        assertEquals(10, consumed);
        assertEquals(String.valueOf((char) 0xFFFF), writer.toString());
    }

    @Test
    public void testNoEntity() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("abc", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testEntityNotAtStart() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("abc&#123;", 3, writer);
        assertEquals(6, consumed);
        assertEquals(String.valueOf((char) 123), writer.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testMissingSemicolon() throws IOException {
        unescaper.translate("&#123", 0, new StringWriter());
    }

    @Test
    public void testInvalidHexDigits() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#xGH;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testInvalidDecimalDigits() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#12G;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testEmptyDecimalEntity() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testEmptyHexEntity() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testNegativeDecimalEntity() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#-123;", 0, writer);
        assertEquals(7, consumed);
        assertEquals(String.valueOf((char) -123), writer.toString());
    }

    @Test
    public void testLeadingZerosDecimal() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#000123;", 0, writer);
        assertEquals(9, consumed);
        assertEquals(String.valueOf((char) 123), writer.toString());
    }

    @Test
    public void testLeadingZerosHex() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x001A;", 0, writer);
        assertEquals(8, consumed);
        assertEquals(String.valueOf((char) 26), writer.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testIndexAtLastChar() throws IOException {
        unescaper.translate("&", 0, new StringWriter());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testOnlyAmpersandHash() throws IOException {
        unescaper.translate("&#", 0, new StringWriter());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testOnlyAmpersandHashX() throws IOException {
        unescaper.translate("&#x", 0, new StringWriter());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testEmptyInput() throws IOException {
        unescaper.translate("", 0, new StringWriter());
    }

    @Test(expected = IOException.class)
    public void testWriterThrowsIOException() throws IOException {
        Writer badWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test");
            }
            @Override
            public void flush() throws IOException {}
            @Override
            public void close() throws IOException {}
        };
        unescaper.translate("&#123;", 0, badWriter);
    }

    @Test(expected = NullPointerException.class)
    public void testNullWriter() throws IOException {
        unescaper.translate("&#123;", 0, null);
    }
}
