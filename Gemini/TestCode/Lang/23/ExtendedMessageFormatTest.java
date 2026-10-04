package org.apache.commons.lang3.text;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExtendedMessageFormatTest {

    private final Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();

    private static class LowerCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(String.valueOf(obj).toLowerCase(Locale.ENGLISH));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return source.toLowerCase(Locale.ENGLISH);
        }
    }

    private static class UpperCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(String.valueOf(obj).toUpperCase(Locale.ENGLISH));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return source.toUpperCase(Locale.ENGLISH);
        }
    }

    private static class DummyFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            if ("lower".equals(name)) {
                return new LowerCaseFormat();
            }
            if ("upper".equals(name)) {
                return new UpperCaseFormat();
            }
            if ("choice".equals(name)) {
                return new ChoiceFormat(arguments);
            }
            return null;
        }
    }

    @Before
    public void setUp() {
        registry.put("lower", new DummyFormatFactory());
        registry.put("upper", new DummyFormatFactory());
        registry.put("choice", new DummyFormatFactory());
    }

    @Test
    public void testConstructors() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Hello {0}");
        Assert.assertEquals("Hello {0}", emf1.toPattern());
        Assert.assertEquals(Locale.getDefault(), emf1.getLocale());

        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Hello {0}", Locale.GERMAN);
        Assert.assertEquals("Hello {0}", emf2.toPattern());
        Assert.assertEquals(Locale.GERMAN, emf2.getLocale());

        ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Hello {0,lower}", registry);
        Assert.assertEquals("Hello {0,lower}", emf3.toPattern());
        Assert.assertEquals(Locale.getDefault(), emf3.getLocale());

        ExtendedMessageFormat emf4 = new ExtendedMessageFormat("Hello {0,upper}", Locale.FRENCH, registry);
        Assert.assertEquals("Hello {0,upper}", emf4.toPattern());
        Assert.assertEquals(Locale.FRENCH, emf4.getLocale());
    }

    @Test
    public void testFormatCustom() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0,lower} and {1,upper}", registry);
        String result = emf.format(new Object[]{"FOO", "bar"});
        Assert.assertEquals("Test foo and BAR", result);
    }

    @Test
    public void testFormatBuiltInWithRegistry() {
        Locale defaultLocale = Locale.US;
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Number: {0,number,currency} Date: {1,date,short}", defaultLocale, registry);
        String expected = new MessageFormat("Number: {0,number,currency} Date: {1,date,short}", defaultLocale).format(new Object[]{12.34, new Date(0)});
        String actual = emf.format(new Object[]{12.34, new Date(0)});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFormatWithoutRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Number: {0,number,integer}");
        String expected = new MessageFormat("Number: {0,number,integer}").format(new Object[]{12.34});
        Assert.assertEquals(expected, emf.format(new Object[]{12.34}));
    }

    @Test
    public void testFormatWithCustomArguments() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,1#one|2#two}", registry);
        Assert.assertEquals("one", emf.format(new Object[]{1}));
        Assert.assertEquals("two", emf.format(new Object[]{2}));
    }

    @Test
    public void testQuoteEscapingWithoutRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("It''s '{0}'");
        Assert.assertEquals("It's {0}", emf.format(new Object[]{"dummy"}));
    }

    @Test
    public void testQuoteEscapingWithRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("It''s '{0}' {1,lower}", registry);
        Assert.assertEquals("It's {0} test", emf.format(new Object[]{"ignored", "TEST"}));
    }

    @Test
    public void testQuotedFormatDescription() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0,choice,'1#one''s'|2#two} done", registry);
        Assert.assertEquals("Test one's done", emf.format(new Object[]{1}));
    }

    @Test
    public void testNestedBracesInCustomFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Result: {0,choice,0#none|1#one {1,lower}}", registry);
        Assert.assertEquals("Result: 0#none|1#one {1,lower}", emf.toPattern());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}");
        emf.setFormat(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatByArgumentIndexThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}");
        emf.setFormatByArgumentIndex(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}");
        emf.setFormats(new Format[]{NumberFormat.getInstance()});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsByArgumentIndexThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}");
        emf.setFormatsByArgumentIndex(new Format[]{NumberFormat.getInstance()});
    }

    @Test
    public void testWhitespaceHandlingInFormatElements() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {   0   ,   lower   } and { 1 }", registry);
        Assert.assertEquals("Test foo and bar", emf.format(new Object[]{"FOO", "bar"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedQuotedString() {
        new ExtendedMessageFormat("Test 'unterminated quote {0}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedQuotedStringInDescription() {
        new ExtendedMessageFormat("Test {0,choice,'unterminated}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatElement() {
        new ExtendedMessageFormat("Test {0", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatDescription() {
        new ExtendedMessageFormat("Test {0,lower", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatArgumentIndexNonDigit() {
        new ExtendedMessageFormat("Test {invalid}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatArgumentIndexSpaces() {
        new ExtendedMessageFormat("Test { 0 1 }", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyFormatArgument() {
        new ExtendedMessageFormat("Test {}", registry);
    }

    @Test
    public void testEmptyRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0,number}", Collections.<String, FormatFactory>emptyMap());
        Assert.assertEquals("Test 12", emf.format(new Object[]{12}));
    }

    @Test
    public void testApplyPatternReapply() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Initial {0}", registry);
        Assert.assertEquals("Initial {0}", emf.toPattern());

        emf.applyPattern("Updated {0,upper}");
        Assert.assertEquals("Updated {0,upper}", emf.toPattern());
        Assert.assertEquals("Updated ABC", emf.format(new Object[]{"abc"}));

        ExtendedMessageFormat emfNoReg = new ExtendedMessageFormat("Initial {0}");
        emfNoReg.applyPattern("Updated {0}");
        Assert.assertEquals("Updated {0}", emfNoReg.toPattern());
    }

    @Test
    public void testToPatternWithEscapedQuotes() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{' {0,lower} '' '}'", registry);
        Assert.assertEquals("'{' {0,lower} '' '}'", emf.toPattern());
    }

    @Test
    public void testMultipleCustomAndBuiltInFormats() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(
                "{0,lower} - {1,number,integer} - {2,upper} - {3,date,short}",
                Locale.US,
                registry
        );
        String expected = "abc - 42 - XYZ - " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(new Date(100000000));
        String actual = emf.format(new Object[]{"ABC", 42, "xyz", new Date(100000000)});
        Assert.assertEquals(expected, actual);
    }
}
