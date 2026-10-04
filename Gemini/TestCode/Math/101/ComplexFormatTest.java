package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

public class ComplexFormatTest {

    private ComplexFormat complexFormat;
    private ComplexFormat complexFormatJ;

    @Before
    public void setUp() {
        complexFormat = ComplexFormat.getInstance(Locale.US);
        complexFormatJ = new ComplexFormat("j", NumberFormat.getInstance(Locale.US));
    }

    @Test
    public void testConstructors() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        ComplexFormat cf1 = new ComplexFormat();
        Assert.assertEquals("i", cf1.getImaginaryCharacter());
        Assert.assertNotNull(cf1.getRealFormat());
        Assert.assertNotNull(cf1.getImaginaryFormat());

        ComplexFormat cf2 = new ComplexFormat(nf);
        Assert.assertEquals("i", cf2.getImaginaryCharacter());
        Assert.assertEquals(nf, cf2.getRealFormat());

        ComplexFormat cf3 = new ComplexFormat(nf, nf);
        Assert.assertEquals("i", cf3.getImaginaryCharacter());
        Assert.assertEquals(nf, cf3.getRealFormat());
        Assert.assertEquals(nf, cf3.getImaginaryFormat());

        ComplexFormat cf4 = new ComplexFormat("j");
        Assert.assertEquals("j", cf4.getImaginaryCharacter());

        ComplexFormat cf5 = new ComplexFormat("j", nf);
        Assert.assertEquals("j", cf5.getImaginaryCharacter());
        Assert.assertEquals(nf, cf5.getRealFormat());

        ComplexFormat cf6 = new ComplexFormat("j", nf, nf);
        Assert.assertEquals("j", cf6.getImaginaryCharacter());
        Assert.assertEquals(nf, cf6.getRealFormat());
        Assert.assertEquals(nf, cf6.getImaginaryFormat());
    }

    @Test
    public void testGetAvailableLocales() {
        Locale[] locales = ComplexFormat.getAvailableLocales();
        Assert.assertNotNull(locales);
        Assert.assertTrue(locales.length > 0);
    }

    @Test
    public void testGetInstance() {
        ComplexFormat cfDefault = ComplexFormat.getInstance();
        Assert.assertNotNull(cfDefault);
        ComplexFormat cfLocale = ComplexFormat.getInstance(Locale.GERMANY);
        Assert.assertNotNull(cfLocale);
    }

    @Test
    public void testSetImaginaryCharacter() {
        ComplexFormat cf = new ComplexFormat();
        cf.setImaginaryCharacter("j");
        Assert.assertEquals("j", cf.getImaginaryCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacterNull() {
        ComplexFormat cf = new ComplexFormat();
        cf.setImaginaryCharacter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacterEmpty() {
        ComplexFormat cf = new ComplexFormat();
        cf.setImaginaryCharacter("");
    }

    @Test
    public void testSetImaginaryFormat() {
        ComplexFormat cf = new ComplexFormat();
        NumberFormat nf = NumberFormat.getInstance();
        cf.setImaginaryFormat(nf);
        Assert.assertSame(nf, cf.getImaginaryFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryFormatNull() {
        ComplexFormat cf = new ComplexFormat();
        cf.setImaginaryFormat(null);
    }

    @Test
    public void testSetRealFormat() {
        ComplexFormat cf = new ComplexFormat();
        NumberFormat nf = NumberFormat.getInstance();
        cf.setRealFormat(nf);
        Assert.assertSame(nf, cf.getRealFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRealFormatNull() {
        ComplexFormat cf = new ComplexFormat();
        cf.setRealFormat(null);
    }

    @Test
    public void testFormatComplexStatic() {
        Complex c = new Complex(1.0, 2.0);
        String s = ComplexFormat.formatComplex(c);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("1") && s.contains("2"));
    }

    @Test
    public void testFormatComplex() {
        Complex c1 = new Complex(1.11, 2.22);
        Assert.assertEquals("1.11 + 2.22i", complexFormat.format(c1));

        Complex c2 = new Complex(1.11, -2.22);
        Assert.assertEquals("1.11 - 2.22i", complexFormat.format(c2));

        Complex c3 = new Complex(1.11, 0.0);
        Assert.assertEquals("1.11", complexFormat.format(c3));

        Complex c4 = new Complex(0.0, 2.22);
        Assert.assertEquals("0 + 2.22i", complexFormat.format(c4));

        Complex c5 = new Complex(1.11, 2.22);
        Assert.assertEquals("1.11 + 2.22j", complexFormatJ.format(c5));

        Complex c6 = new Complex(1.11, -2.22);
        Assert.assertEquals("1.11 - 2.22j", complexFormatJ.format(c6));
    }

    @Test
    public void testFormatSpecialDoubles() {
        Complex nanBoth = new Complex(Double.NaN, Double.NaN);
        Assert.assertEquals("(NaN) + (NaN)i", complexFormat.format(nanBoth));

        Complex nanIm = new Complex(1.0, Double.NaN);
        Assert.assertEquals("1 + (NaN)i", complexFormat.format(nanIm));

        Complex infRe = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Assert.assertEquals("(Infinity) + 2i", complexFormat.format(infRe));

        Complex negInfRe = new Complex(Double.NEGATIVE_INFINITY, 2.0);
        Assert.assertEquals("(-Infinity) + 2i", complexFormat.format(negInfRe));

        Complex infIm = new Complex(1.0, Double.POSITIVE_INFINITY);
        Assert.assertEquals("1 + (Infinity)i", complexFormat.format(infIm));

        Complex negInfIm = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertEquals("1 - (Infinity)i", complexFormat.format(negInfIm));
    }

    @Test
    public void testFormatObject() {
        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);

        complexFormat.format(new Complex(1.0, 2.0), sb, fp);
        Assert.assertEquals("1 + 2i", sb.toString());

        sb = new StringBuffer();
        complexFormat.format(Double.valueOf(3.5), sb, fp);
        Assert.assertEquals("3.5", sb.toString());

        sb = new StringBuffer();
        complexFormat.format(Long.valueOf(10L), sb, fp);
        Assert.assertEquals("10", sb.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObjectInvalid() {
        complexFormat.format("not a number or complex", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testParseSimple() throws ParseException {
        Complex c1 = complexFormat.parse("1 + 1i");
        Assert.assertEquals(new Complex(1.0, 1.0), c1);

        Complex c2 = complexFormat.parse("1 - 1i");
        Assert.assertEquals(new Complex(1.0, -1.0), c2);

        Complex c3 = complexFormat.parse("1");
        Assert.assertEquals(new Complex(1.0, 0.0), c3);

        Complex c4 = complexFormat.parse("  1.5   +   2.5i  ");
        Assert.assertEquals(new Complex(1.5, 2.5), c4);

        Complex c5 = complexFormat.parse("  -1.5   -   2.5i  ");
        Assert.assertEquals(new Complex(-1.5, -2.5), c5);
    }

    @Test
    public void testParseCustomImaginary() throws ParseException {
        Complex c = complexFormatJ.parse("1.5 + 2.5j");
        Assert.assertEquals(new Complex(1.5, 2.5), c);
    }

    @Test
    public void testParseObject() {
        ParsePosition pos = new ParsePosition(0);
        Object obj = complexFormat.parseObject("1 + 2i", pos);
        Assert.assertEquals(new Complex(1.0, 2.0), obj);
        Assert.assertEquals(6, pos.getIndex());
    }

    @Test
    public void testParseInvalidSign() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = complexFormat.parse("1 * 2i", pos);
        Assert.assertNull(c);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(2, pos.getErrorIndex());
    }

    @Test
    public void testParseInvalidReal() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = complexFormat.parse("abc + 2i", pos);
        Assert.assertNull(c);
        Assert.assertEquals(0, pos.getIndex());
    }

    @Test
    public void testParseInvalidImaginary() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = complexFormat.parse("1 + abci", pos);
        Assert.assertNull(c);
        Assert.assertEquals(0, pos.getIndex());
    }

    @Test
    public void testParseInvalidImaginaryCharacter() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = complexFormat.parse("1 + 2k", pos);
        Assert.assertNull(c);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertEquals(5, pos.getErrorIndex());
    }

    @Test(expected = ParseException.class)
    public void testParseException() throws ParseException {
        complexFormat.parse("invalid");
    }

    @Test
    public void testParseSpecialNumbers() throws ParseException {
        Complex c1 = complexFormat.parse("(NaN) + (NaN)i");
        Assert.assertTrue(Double.isNaN(c1.getReal()));
        Assert.assertTrue(Double.isNaN(c1.getImaginary()));

        Complex c2 = complexFormat.parse("(Infinity) + (Infinity)i");
        Assert.assertEquals(Double.POSITIVE_INFINITY, c2.getReal(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, c2.getImaginary(), 0.0);

        Complex c3 = complexFormat.parse("(-Infinity) - (Infinity)i");
        Assert.assertEquals(Double.NEGATIVE_INFINITY, c3.getReal(), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, c3.getImaginary(), 0.0);
    }
}
