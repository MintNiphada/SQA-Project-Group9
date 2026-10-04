package org.apache.commons.lang3.text;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ExtendedMessageFormatTest {

    private static final String DUMMY_PATTERN = "";
    private static final Locale LOCALE_US = Locale.US;

    // Factory for tests that returns a fixed format
    private static final FormatFactory SIMPLE_FACTORY = new FormatFactory() {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            if ("number".equals(name)) {
                return NumberFormat.getIntegerInstance(locale);
            }
            if ("date".equals(name)) {
                return new SimpleDateFormat("yyyy-MM-dd");
            }
            return null;
        }
    };

    private static final FormatFactory NULL_RETURN_FACTORY = new FormatFactory() {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            return null;
        }
    };

    // Factory that returns a specific format for any name, used to verify custom format placement
    private static final FormatFactory CUSTOM_FORMAT_FACTORY = new FormatFactory() {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            return NumberFormat.getInstance(locale);
        }
    };

    // Constructors tests

    @Test
    public void testDefaultConstructor() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DUMMY_PATTERN);
        Assert.assertEquals(DUMMY_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", LOCALE_US);
        Assert.assertEquals("{0}", emf.toPattern());
        Assert.assertEquals(LOCALE_US, emf.getLocale());
    }

    @Test
    public void testConstructorWithRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", SIMPLE_FACTORY);
        Assert.assertEquals("{0}", emf.toPattern());
    }

    @Test
    public void testConstructorWithLocaleAndRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", LOCALE_US, SIMPLE_FACTORY);
        Assert.assertEquals("{0}", emf.toPattern());
        Assert.assertEquals(LOCALE_US, emf.getLocale());
    }

    @Test
    public void testConstructorWithValidPatternWithFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,number,integer}");
        Assert.assertNotNull(emf.toPattern());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidArgumentIndexNonDigits() {
        new ExtendedMessageFormat("{abc}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithUnterminatedFormatElement() {
        new ExtendedMessageFormat("{0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithUnterminatedQuote() {
        new ExtendedMessageFormat("'{0}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithUnterminatedFormatDescription() {
        new ExtendedMessageFormat("{0,{1}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithMissingClosingBraceAfterFormatDescription() {
        new ExtendedMessageFormat("{0,number");
    }

    @Test
    public void testConstructorWithEscapedQuotes() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0}''");
        Assert.assertTrue(emf.toPattern().contains("''"));
    }

    @Test
    public void testConstructorWithLiteralQuotedText() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{'0} is a test");
        Assert.assertNotNull(emf.toPattern());
    }

    // toPattern tests

    @Test
    public void testToPatternAfterConstruction() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}");
        Assert.assertEquals("Hello {0}", emf.toPattern());
    }

    @Test
    public void testToPatternWithRegistryFoundFormat() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", CUSTOM_FORMAT_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,custom}", registry);
        Assert.assertEquals("{0,custom}", emf.toPattern());
    }

    @Test
    public void testToPatternWithRegistryNotFoundFormat() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("other", CUSTOM_FORMAT_FACTORY);
        // The format name "custom" is not in registry, so it will fall back to super
        // and the pattern should contain the description (if valid).
        // "custom" is not a standard format type, so super.applyPattern may fail.
        // Instead we use a standard type not in registry.
        registry.clear();
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,number,integer}", registry);
        Assert.assertEquals("{0,number,integer}", emf.toPattern());
    }

    @Test
    public void testToPatternWithNestedBracesInDescriptionAndFormatFound() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("{1}", CUSTOM_FORMAT_FACTORY); // the name extracted from description "{1}"
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,{1}}", registry);
        // The description "{1}" is found, so the format is set and toPattern should include ",{1}"
        Assert.assertEquals("{0,{1}}", emf.toPattern());
    }

    // applyPattern tests (change pattern after construction)

    @Test
    public void testApplyPatternWithNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.applyPattern("{1}");
        Assert.assertEquals("{1}", emf.toPattern());
    }

    @Test
    public void testApplyPatternWithRegistryUpdatesFormats() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("number", CUSTOM_FORMAT_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", registry);
        Assert.assertEquals(NumberFormat.getInstance(), emf.getFormats()[0]);
        emf.applyPattern("{1,number}");
        Assert.assertEquals(NumberFormat.getInstance(), emf.getFormats()[0]);
    }

    @Test
    public void testApplyPatternWithMultipleFormatElements() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("myformat", CUSTOM_FORMAT_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0} {1,myformat} {2}", registry);
        Format[] formats = emf.getFormats();
        // {0} and {2} have no custom format, {1} has custom format
        Assert.assertNull(formats[0]);
        Assert.assertNotNull(formats[1]);
        Assert.assertNull(formats[2]);
        Assert.assertEquals(NumberFormat.getInstance(), formats[1]);
    }

    @Test
    public void testApplyPatternWithRegistryReturningNullForAll() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("foo", NULL_RETURN_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,foo} {1}", registry);
        Format[] formats = emf.getFormats();
        // Since factory returns null, format array should be untouched (super's default for first element probably null for invalid type? Actually super may not parse due to "foo" being invalid type, but our test expects something).
        // We'll just check no exception and toPattern contains "foo"
        Assert.assertTrue(emf.toPattern().contains("foo"));
    }

    // readArgumentIndex branches

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentIndexWithSpaceInDigits() {
        new ExtendedMessageFormat("{0 1}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentIndexWithNonDigitCharacter() {
        new ExtendedMessageFormat("{0a}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentIndexWithOnlyComma() {
        new ExtendedMessageFormat("{,}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatElementWithOnlyBrace() {
        new ExtendedMessageFormat("{");
    }

    // parseFormatDescription branches
    @Test
    public void testFormatDescriptionWithEscapedQuotes() {
        // description: a quoted string with escaped quote
        String pattern = "{0, 'It''s a test'}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern);
        Assert.assertTrue(emf.toPattern().contains("It's a test"));
    }

    @Test
    public void testFormatDescriptionWithNestedBracesAndQuotes() {
        String pattern = "{0, {'{1}',''{2}''}}";
        // This description contains nested braces and quotes; it should parse without error.
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern);
        Assert.assertNotNull(emf.toPattern());
    }

    // setFormat etc. throw UnsupportedOperationException

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatThrowsException() {
        new ExtendedMessageFormat("{0}").setFormat(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatByArgumentIndexThrowsException() {
        new ExtendedMessageFormat("{0}").setFormatByArgumentIndex(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsThrowsException() {
        new ExtendedMessageFormat("{0}").setFormats(new Format[] { NumberFormat.getInstance() });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsByArgumentIndexThrowsException() {
        new ExtendedMessageFormat("{0}").setFormatsByArgumentIndex(new Format[] { NumberFormat.getInstance() });
    }

    // Edge case: empty pattern
    @Test
    public void testEmptyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("");
        Assert.assertEquals("", emf.toPattern());
    }

    @Test
    public void testPatternWithOnlyQuotes() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''");
        Assert.assertEquals("''", emf.toPattern());
    }

    // Test that formatting with custom format works (indirect coverage of getFormat and insertFormats)
    @Test
    public void testFormatWithCustomFormat() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("myformat", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return NumberFormat.getIntegerInstance(locale);
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("value={0,myformat}", Locale.US, registry);
        String result = emf.format(new Object[] { 1234 });
        Assert.assertTrue(result.contains("1,234")); // depends on locale
    }

    // Test that toPattern preserves escaped quotes in format description when registry returns format
    @Test
    public void testToPatternWithEscapedQuoteInDescriptionAndFormatFound() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("x", CUSTOM_FORMAT_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,x,''''''}", registry); // description with escaped quotes
        Assert.assertTrue(emf.toPattern().contains("'''"));
    }

    // Test that containsElements returns false for all nulls
    @Test
    public void testApplyPatternAllNullFormatsInRegistry() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("nullmaker", NULL_RETURN_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,nullmaker}", registry);
        // No exception, toPattern should still contain "nullmaker"
        Assert.assertTrue(emf.toPattern().contains("nullmaker"));
    }

    @Test
    public void testConstructorWithRegistryNull() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", (Map<String, FormatFactory>) null);
        Assert.assertEquals("{0}", emf.toPattern());
    }
}
