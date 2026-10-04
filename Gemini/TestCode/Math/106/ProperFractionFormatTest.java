package org.apache.commons.math.fraction;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class ProperFractionFormatTest {

    @Test
    public void testDefaultConstructor() {
        ProperFractionFormat format = new ProperFractionFormat();
        Assert.assertNotNull(format.getWholeFormat());
        Assert.assertNotNull(format.getNumeratorFormat());
        Assert.assertNotNull(format.getDenominatorFormat());
    }

    @Test
    public void testSingleFormatConstructor() {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        ProperFractionFormat format = new ProperFractionFormat(nf);
        Assert.assertNotNull(format.getWholeFormat());
        Assert.assertNotNull(format.getNumeratorFormat());
        Assert.assertNotNull(format.getDenominatorFormat());
        Assert.assertNotSame(format.getWholeFormat(), format.getNumeratorFormat());
    }

    @Test
    public void testThreeFormatConstructor() {
        NumberFormat wf = NumberFormat.getIntegerInstance(Locale.US);
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        NumberFormat df = NumberFormat.getIntegerInstance(Locale.US);
        ProperFractionFormat format = new ProperFractionFormat(wf, nf, df);
        Assert.assertSame(wf, format.getWholeFormat());
        Assert.assertSame(nf, format.getNumeratorFormat());
        Assert.assertSame(df, format.getDenominatorFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWholeFormatNull() {
        ProperFractionFormat format = new ProperFractionFormat();
        format.setWholeFormat(null);
    }

    @Test
    public void testSetWholeFormatValid() {
        ProperFractionFormat format = new ProperFractionFormat();
        NumberFormat nf = NumberFormat.getInstance();
        format.setWholeFormat(nf);
        Assert.assertSame(nf, format.getWholeFormat());
    }

    @Test
    public void testFormatFractionZeroWhole() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(1, 2);
        StringBuffer sb = format.format(fraction, new StringBuffer(), new FieldPosition(0));
        Assert.assertEquals("1 / 2", sb.toString());
    }

    @Test
    public void testFormatFractionPositiveWhole() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(7, 2);
        StringBuffer sb = format.format(fraction, new StringBuffer(), new FieldPosition(0));
        Assert.assertEquals("3 1 / 2", sb.toString());
    }

    @Test
    public void testFormatFractionNegativeWhole() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(-7, 2);
        StringBuffer sb = format.format(fraction, new StringBuffer(), new FieldPosition(0));
        Assert.assertEquals("-3 1 / 2", sb.toString());
    }

    @Test
    public void testFormatFractionIntegerOnly() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(4, 2);
        StringBuffer sb = format.format(fraction, new StringBuffer(), new FieldPosition(0));
        Assert.assertEquals("2 0 / 1", sb.toString());
    }

    @Test
    public void testParseImproperFormatDirect() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction f = format.parse("1 / 2");
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseProperPositive() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction f = format.parse("3 1 / 2");
        Assert.assertEquals(7, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseProperNegative() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction f = format.parse("-3 1 / 2");
        Assert.assertEquals(-7, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseProperWithWhitespace() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction f = format.parse("  2   3 /  4 ");
        Assert.assertEquals(11, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());
    }

    @Test
    public void testParseProperWithoutSlashReturnsWholeAsFraction() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("2 3", pos);
        Assert.assertNotNull(f);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testParseInvalidWhole() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("abc 1 / 2", pos);
        Assert.assertNull(f);
        Assert.assertEquals(0, pos.getIndex());
    }

    @Test
    public void testParseInvalidNumerator() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("3 abc / 2", pos);
        Assert.assertNull(f);
        Assert.assertEquals(0, pos.getIndex());
    }

    @Test
    public void testParseInvalidSeparator() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("3 1 X 2", pos);
        Assert.assertNull(f);
        Assert.assertEquals(0, pos.getIndex());
        Assert.assertTrue(pos.getErrorIndex() >= 0);
    }

    @Test
    public void testParseInvalidDenominator() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("3 1 / abc", pos);
        Assert.assertNull(f);
        Assert.assertEquals(0, pos.getIndex());
    }

    @Test(expected = ParseException.class)
    public void testParseStringThrowsExceptionOnInvalid() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        format.parse("invalid string");
    }
}
