package org.apache.commons.lang.text;

import org.junit.Assert;
import org.junit.Test;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExtendedMessageFormatTest {

    private static final FormatFactory LOWER_FACTORY = new FormatFactory() {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    return toAppendTo.append(String.valueOf(obj).toLowerCase(locale != null ? locale : Locale.getDefault()));
                }
                public Object parseObject(String source, ParsePosition pos) {
                    pos.setIndex(source.length());
                    return source;
                }
            };
        }
    };

    private static final FormatFactory UPPER_FACTORY = new FormatFactory() {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    return toAppendTo.append(String.valueOf(obj).toUpperCase(locale != null ? locale : Locale.getDefault()));
                }
                public Object parseObject(String source, ParsePosition pos) {
                    pos.setIndex(source.length());
                    return source;
                }
            };
        }
    };

    private static final FormatFactory ARG_FACTORY = new FormatFactory() {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    return toAppendTo.append(name).append(":").append(arguments).append(":").append(obj);
                }
                public Object parseObject(String source, ParsePosition pos) {
                    pos.setIndex(source.length());
                    return source;
                }
            };
        }
    };

    @Test
    public void testConstructorsAndToPattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Hello {0}");
        Assert.assertEquals("Hello {0}", emf1.toPattern());
        Assert.assertEquals("Hello World", emf1.format(new Object[]{"World"}));

        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Hello {0}", Locale.US);
        Assert.assertEquals("Hello {0}", emf2.toPattern());

        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);

        ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Hello {0,lower}", registry);
        Assert.assertEquals("Hello {0,lower}", emf3.toPattern());
        Assert.assertEquals("Hello world", emf3.format(new Object[]{"WORLD"}));

        ExtendedMessageFormat emf4 = new ExtendedMessageFormat("Hello {0,lower}", Locale.US, registry);
        Assert.assertEquals("Hello {0,lower}", emf4.toPattern());
        Assert.assertEquals("Hello world", emf4.format(new Object[]{"WORLD"}));
    }

    @Test
    public void testApplyPatternNoRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}");
        emf.applyPattern("Goodbye {0}");
        Assert.assertEquals("Goodbye {0}", emf.toPattern());
        Assert.assertEquals("Goodbye John", emf.format(new Object[]{"John"}));
    }

    @Test
    public void testCustomFormatWithArgs() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", ARG_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Format {0, custom , arg1,arg2 }", registry);
        Assert.assertEquals("Format custom:arg1,arg2:TEST", emf.format(new Object[]{"TEST"}));
        Assert.assertEquals("Format {0, custom , arg1,arg2 }", emf.toPattern());
    }

    @Test
    public void testBuiltInFormatsPassthrough() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Number: {0,number,#.##}, Custom: {1,lower}", Locale.US, registry);
        Assert.assertEquals("Number: 12.34, Custom: test", emf.format(new Object[]{12.345, "TEST"}));
    }

    @Test
    public void testQuotedStringsAndEscapedQuotes() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", UPPER_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0,upper}'' '{1}' {2,upper}", registry);
        Assert.assertEquals("''ABC'' {1} DEF", emf.format(new Object[]{"abc", "ignored", "def"}));
    }

    @Test
    public void testNestedElementsInChoiceFormat() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#none|1#one: {1,lower}|2#many: {1,lower}}", registry);
        Assert.assertEquals("one: abc", emf.format(new Object[]{1, "ABC"}));
    }

    @Test
    public void testQuotedBracesInFormatDescription() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", ARG_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,custom,'{foo}'}", registry);
        Assert.assertEquals("custom:'{foo}':BAR", emf.format(new Object[]{"BAR"}));
    }

    @Test
    public void testWhitespaceHandlingInArgumentIndex() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{  0  ,  lower  } and {  1  }", registry);
        Assert.assertEquals("val0 and val1", emf.format(new Object[]{"VAL0", "val1"}));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormat(0, DateFormat.getDateInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatByArgumentIndexUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatByArgumentIndex(0, DateFormat.getDateInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormats(new Format[]{DateFormat.getDateInstance()});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsByArgumentIndexUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatsByArgumentIndex(new Format[]{DateFormat.getDateInstance()});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatElement() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("{0,lower", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedQuotedString() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("'{0,lower}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedQuotedStringInDescription() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("{0, 'lower}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatArgumentIndexNonDigit() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("{abc,lower}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatArgumentIndexWhitespaceFollowedByInvalid() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("{0 foo,lower}", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnreadableFormatElement() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        new ExtendedMessageFormat("{0,lower invalid}", registry);
    }

    @Test
    public void testUnregisteredFormatNameFallback() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", LOWER_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,number,integer} and {1,lower}", registry);
        Assert.assertEquals("10 and test", emf.format(new Object[]{10.5, "TEST"}));
    }

    @Test
    public void testInsertFormatsWithNoCustomFormatsFound() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Plain {0} and {1,number}", registry);
        Assert.assertEquals("Plain {0} and {1,number}", emf.toPattern());
    }

    @Test
    public void testEmptyAndNonCustomRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}", Collections.emptyMap());
        Assert.assertEquals("Test val", emf.format(new Object[]{"val"}));
        Assert.assertEquals("Test {0}", emf.toPattern());
    }
}
