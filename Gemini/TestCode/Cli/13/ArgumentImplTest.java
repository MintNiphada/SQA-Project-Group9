package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentImplTest {

    @Test
    public void testConstructorDefaultsAndGetters() {
        ArgumentImpl arg = new ArgumentImpl("argName", "argDescription", 1, 3, '=', ',', null, "--", null, 42);

        Assert.assertEquals("argName", arg.getPreferredName());
        Assert.assertEquals("argDescription", arg.getDescription());
        Assert.assertEquals(1, arg.getMinimum());
        Assert.assertEquals(3, arg.getMaximum());
        Assert.assertEquals('=', arg.getInitialSeparator());
        Assert.assertEquals(',', arg.getSubsequentSeparator());
        Assert.assertNull(arg.getValidator());
        Assert.assertEquals("--", arg.getConsumeRemaining());
        Assert.assertNull(arg.getDefaultValues());
        Assert.assertEquals(42, arg.getId());
        Assert.assertTrue(arg.isRequired());
        Assert.assertTrue(arg.getPrefixes().isEmpty());
        Assert.assertTrue(arg.getTriggers().isEmpty());
    }

    @Test
    public void testConstructorWithNullName() {
        ArgumentImpl arg = new ArgumentImpl(null, "desc", 0, 1, '\0', '\0', null, null, null, 0);
        Assert.assertEquals("arg", arg.getPreferredName());
        Assert.assertFalse(arg.isRequired());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMinExceedsMax() {
        new ArgumentImpl("test", "desc", 3, 2, '\0', '\0', null, null, null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTooFewDefaults() {
        List defaults = Collections.singletonList("val1");
        new ArgumentImpl("test", "desc", 2, 5, '\0', '\0', null, null, defaults, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTooManyDefaults() {
        List defaults = Arrays.asList("val1", "val2", "val3");
        new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', null, null, defaults, 0);
    }

    @Test
    public void testConstructorValidDefaults() {
        List defaults = Arrays.asList("val1", "val2");
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 1, 3, '\0', '\0', null, null, defaults, 0);
        Assert.assertEquals(defaults, arg.getDefaultValues());
    }

    @Test
    public void testStripBoundaryQuotes() {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 1, '\0', '\0', null, null, null, 0);

        Assert.assertEquals("hello", arg.stripBoundaryQuotes("\"hello\""));
        Assert.assertEquals("\"hello", arg.stripBoundaryQuotes("\"hello"));
        Assert.assertEquals("hello\"", arg.stripBoundaryQuotes("hello\""));
        Assert.assertEquals("hello", arg.stripBoundaryQuotes("hello"));
        Assert.assertEquals("", arg.stripBoundaryQuotes("\"\""));
    }

    @Test
    public void testCanProcess() {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 1, '\0', '\0', null, null, null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        Assert.assertTrue(arg.canProcess(cl, "any"));
        Assert.assertTrue(arg.canProcess(cl, (String) null));
    }

    @Test
    public void testDefaultsAndDefaultValues() {
        List defaults = Collections.singletonList("defaultVal");
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 1, '\0', '\0', null, null, defaults, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.defaults(cl);
        Assert.assertEquals(defaults, cl.getValues(arg));
    }

    @Test
    public void testProcessSimpleValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 2, '\0', '\0', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        List args = new ArrayList(Arrays.asList("val1", "\"val2\""));
        ListIterator it = args.listIterator();

        arg.process(cl, it);

        List values = cl.getValues(arg);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("val1", values.get(0));
        Assert.assertEquals("val2", values.get(1));
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testProcessStopsAtMaximum() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 2, '\0', '\0', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        List args = new ArrayList(Arrays.asList("v1", "v2", "v3"));
        ListIterator it = args.listIterator();

        arg.processValues(cl, it, arg);

        List values = cl.getValues(arg);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("v1", values.get(0));
        Assert.assertEquals("v2", values.get(1));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("v3", it.next());
    }

    @Test
    public void testProcessLooksLikeOption() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 5, '\0', '\0', null, "--", null, 0);
        // Create an option that looksLikeOption will recognize
        PropertyOption propOpt = new PropertyOption();
        WriteableCommandLine cl = new WriteableCommandLineImpl(propOpt, new ArrayList());
        List args = new ArrayList(Arrays.asList("val1", "-Dkey=value", "val2"));
        ListIterator it = args.listIterator();

        arg.processValues(cl, it, arg);

        List values = cl.getValues(arg);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("val1", values.get(0));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("-Dkey=value", it.next());
    }

    @Test
    public void testProcessConsumeRemaining() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 5, '\0', '\0', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        List args = new ArrayList(Arrays.asList("--", "-opt1", "-opt2", "val3"));
        ListIterator it = args.listIterator();

        arg.processValues(cl, it, arg);

        List values = cl.getValues(arg);
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("-opt1", values.get(0));
        Assert.assertEquals("-opt2", values.get(1));
        Assert.assertEquals("val3", values.get(2));
    }

    @Test
    public void testProcessSubsequentSplit() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 4, '=', ',', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        List args = new ArrayList(Arrays.asList("a,b,c", "d"));
        ListIterator it = args.listIterator();

        arg.processValues(cl, it, arg);

        List values = cl.getValues(arg);
        Assert.assertEquals(4, values.size());
        Assert.assertEquals("a", values.get(0));
        Assert.assertEquals("b", values.get(1));
        Assert.assertEquals("c", values.get(2));
        Assert.assertEquals("d", values.get(3));
    }

    @Test(expected = OptionException.class)
    public void testProcessSubsequentSplitExceedsMaximum() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 2, '=', ',', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        List args = new ArrayList(Collections.singletonList("a,b,c"));
        ListIterator it = args.listIterator();

        arg.processValues(cl, it, arg);
    }

    @Test
    public void testValidateSuccess() throws OptionException {
        Validator validator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                if (values.contains("invalid")) {
                    throw new InvalidArgumentException("Bad value");
                }
            }
        };

        ArgumentImpl arg = new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', validator, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        cl.addValue(arg, "valid");

        arg.validate(cl);
        arg.validate(cl, arg);
    }

    @Test(expected = OptionException.class)
    public void testValidateMissingValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 2, 5, '\0', '\0', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        cl.addValue(arg, "singleVal");

        arg.validate(cl);
    }

    @Test(expected = OptionException.class)
    public void testValidateUnexpectedValue() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 1, '\0', '\0', null, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        cl.addValue(arg, "v1");
        cl.addValue(arg, "v2");

        arg.validate(cl);
    }

    @Test(expected = OptionException.class)
    public void testValidateValidatorThrows() throws OptionException {
        Validator validator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                throw new InvalidArgumentException("Validation failed");
            }
        };

        ArgumentImpl arg = new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', validator, "--", null, 0);
        WriteableCommandLine cl = new WriteableCommandLineImpl(arg, new ArrayList());
        cl.addValue(arg, "value");

        arg.validate(cl);
    }

    @Test
    public void testHelpLines() {
        ArgumentImpl arg = new ArgumentImpl("argName", "argDesc", 1, 1, '\0', '\0', null, null, null, 0);
        List helpLines = arg.helpLines(0, Collections.EMPTY_SET, null);

        Assert.assertNotNull(helpLines);
        Assert.assertEquals(1, helpLines.size());
        HelpLine line = (HelpLine) helpLines.get(0);
        Assert.assertEquals(0, line.getIndent());
        Assert.assertEquals(arg, line.getOption());
    }

    @Test
    public void testAppendUsageBasic() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 1, '\0', '\0', null, null, null, 0);
        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, Collections.EMPTY_SET, null);
        Assert.assertEquals("arg", buffer.toString());
    }

    @Test
    public void testAppendUsageOptionalAndBracketed() {
        ArgumentImpl arg = new ArgumentImpl("file", "desc", 0, 1, '\0', '\0', null, null, null, 0);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);
        Assert.assertEquals("[<file>]", buffer.toString());
    }

    @Test
    public void testAppendUsageNumberedMultipleArgs() {
        ArgumentImpl arg = new ArgumentImpl("item", "desc", 1, 3, '\0', '\0', null, null, null, 0);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);
        Assert.assertEquals("item1 [item2 [item3]]", buffer.toString());
    }

    @Test
    public void testAppendUsageInfiniteArgs() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, Integer.MAX_VALUE, '\0', '\0', null, null, null, 0);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);
        Assert.assertEquals("[arg [arg] ...]", buffer.toString());
    }

    @Test
    public void testAppendUsageMultipleArgsWithoutOptional() {
        ArgumentImpl arg = new ArgumentImpl("param", "desc", 2, 3, '\0', '\0', null, null, null, 0);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);
        Assert.assertEquals("<param> <param> [<param>]", buffer.toString());
    }
}