package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultParserTest
{
    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp()
    {
        parser = new DefaultParser();
        options = new Options();
    }

    @Test
    public void testSimpleShortOption() throws Exception
    {
        options.addOption("a", false, "toggle a");
        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSimpleLongOption() throws Exception
    {
        options.addOption(null, "alpha", false, "toggle alpha");
        CommandLine cl = parser.parse(options, new String[]{"--alpha"});

        assertTrue(cl.hasOption("alpha"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testShortOptionWithArgumentSeparate() throws Exception
    {
        options.addOption("a", true, "option a with arg");
        CommandLine cl = parser.parse(options, new String[]{"-a", "foo"});

        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
    }

    @Test
    public void testShortOptionWithArgumentAttached() throws Exception
    {
        options.addOption("a", true, "option a with arg");
        CommandLine cl = parser.parse(options, new String[]{"-afoo"});

        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
    }

    @Test
    public void testShortOptionWithEqualSign() throws Exception
    {
        options.addOption("a", true, "option a with arg");
        CommandLine cl = parser.parse(options, new String[]{"-a=foo"});

        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testShortOptionWithEqualSignNoArg() throws Exception
    {
        options.addOption("a", false, "option a without arg");
        parser.parse(options, new String[]{"-a=foo"});
    }

    @Test
    public void testLongOptionWithArgumentSeparate() throws Exception
    {
        options.addOption(Option.builder("a").longOpt("alpha").hasArg().build());
        CommandLine cl = parser.parse(options, new String[]{"--alpha", "bar"});

        assertTrue(cl.hasOption("alpha"));
        assertEquals("bar", cl.getOptionValue("alpha"));
    }

    @Test
    public void testLongOptionWithArgumentEqual() throws Exception
    {
        options.addOption(Option.builder("a").longOpt("alpha").hasArg().build());
        CommandLine cl = parser.parse(options, new String[]{"--alpha=bar"});

        assertTrue(cl.hasOption("alpha"));
        assertEquals("bar", cl.getOptionValue("alpha"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testLongOptionWithEqualSignNoArg() throws Exception
    {
        options.addOption(Option.builder("a").longOpt("alpha").build());
        parser.parse(options, new String[]{"--alpha=bar"});
    }

    @Test
    public void testSingleHyphenAsArgument() throws Exception
    {
        CommandLine cl = parser.parse(options, new String[]{"-"});

        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testDoubleHyphenStopParsing() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "arg1"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    @Test
    public void testNegativeNumberAsArgument() throws Exception
    {
        options.addOption("n", true, "number option");
        CommandLine cl = parser.parse(options, new String[]{"-n", "-42"});

        assertTrue(cl.hasOption("n"));
        assertEquals("-42", cl.getOptionValue("n"));

        CommandLine clDouble = parser.parse(options, new String[]{"-n", "-42.5"});
        assertEquals("-42.5", clDouble.getOptionValue("n"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testNegativeNumberAsUnknownOption() throws Exception
    {
        options.addOption("a", false, "flag a");
        parser.parse(options, new String[]{"-42"});
    }

    @Test
    public void testNegativeNumberWithStopAtNonOption() throws Exception
    {
        CommandLine cl = parser.parse(options, new String[]{"-42", "extra"}, true);

        assertEquals(2, cl.getArgs().length);
        assertEquals("-42", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test
    public void testStopAtNonOptionBasic() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption", "-b"}, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    @Test
    public void testConcatenatedShortOptions() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");

        CommandLine cl = parser.parse(options, new String[]{"-abc"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testConcatenatedShortOptionsWithArg() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", true, "option b with arg");

        CommandLine cl = parser.parse(options, new String[]{"-abValue"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("Value", cl.getOptionValue("b"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testConcatenatedShortOptionsUnknown() throws Exception
    {
        options.addOption("a", false, "flag a");
        parser.parse(options, new String[]{"-ab"});
    }

    @Test
    public void testConcatenatedShortOptionsUnknownStopAtNonOption() throws Exception
    {
        options.addOption("a", false, "flag a");
        CommandLine cl = parser.parse(options, new String[]{"-ab", "-c"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("b", cl.getArgs()[0]);
        assertEquals("-c", cl.getArgs()[1]);
    }

    @Test
    public void testLongPrefixOption() throws Exception
    {
        options.addOption(Option.builder("Xmx").longOpt("Xmx").hasArg().build());
        CommandLine cl = parser.parse(options, new String[]{"-Xmx1024m"});

        assertTrue(cl.hasOption("Xmx"));
        assertEquals("1024m", cl.getOptionValue("Xmx"));
    }

    @Test
    public void testLongOptionSingleDash() throws Exception
    {
        options.addOption(Option.builder().longOpt("verbose").build());
        CommandLine cl = parser.parse(options, new String[]{"-verbose"});

        assertTrue(cl.hasOption("verbose"));
    }

    @Test
    public void testLongOptionSingleDashWithEqual() throws Exception
    {
        options.addOption(Option.builder().longOpt("file").hasArg().build());
        CommandLine cl = parser.parse(options, new String[]{"-file=test.txt"});

        assertTrue(cl.hasOption("file"));
        assertEquals("test.txt", cl.getOptionValue("file"));
    }

    @Test
    public void testPartialMatchingLongOption() throws Exception
    {
        options.addOption(Option.builder().longOpt("version").build());
        CommandLine cl = parser.parse(options, new String[]{"--ver"});

        assertTrue(cl.hasOption("version"));
    }

    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOptionWithoutEqual() throws Exception
    {
        options.addOption(Option.builder().longOpt("version").build());
        options.addOption(Option.builder().longOpt("verbose").build());

        parser.parse(options, new String[]{"--ver"});
    }

    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOptionWithEqual() throws Exception
    {
        options.addOption(Option.builder().longOpt("fileA").hasArg().build());
        options.addOption(Option.builder().longOpt("fileB").hasArg().build());

        parser.parse(options, new String[]{"--file=test.txt"});
    }

    @Test
    public void testJavaPropertyStyleAttached() throws Exception
    {
        Option propOption = Option.builder("D").hasArgs().valueSeparator('=').build();
        propOption.setArgs(2);
        options.addOption(propOption);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    @Test
    public void testJavaPropertyStyleKeyOnly() throws Exception
    {
        Option propOption = Option.builder("D").hasArgs().build();
        propOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(propOption);

        CommandLine cl = parser.parse(options, new String[]{"-DmyKey"});

        assertTrue(cl.hasOption("D"));
        assertEquals("myKey", cl.getOptionValue("D"));
    }

    @Test
    public void testStripQuotesFromArgument() throws Exception
    {
        options.addOption("a", true, "arg a");
        CommandLine cl1 = parser.parse(options, new String[]{"-a", "\"quoted string\""});
        assertEquals("quoted string", cl1.getOptionValue("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a", "'single quoted'"});
        assertEquals("single quoted", cl2.getOptionValue("a"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentAtEnd() throws Exception
    {
        options.addOption("a", true, "arg a");
        parser.parse(options, new String[]{"-a"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentBeforeNextOption() throws Exception
    {
        options.addOption("a", true, "arg a");
        options.addOption("b", false, "flag b");
        parser.parse(options, new String[]{"-a", "-b"});
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionMissing() throws Exception
    {
        Option opt = Option.builder("r").required().build();
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    @Test
    public void testRequiredOptionSupplied() throws Exception
    {
        Option opt = Option.builder("r").required().build();
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    @Test
    public void testOptionGroup() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-b"});
        assertTrue(cl.hasOption("b"));
        assertFalse(cl.hasOption("a"));
        assertEquals("b", group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testOptionGroupMultipleSelected() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionGroupMissing() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    @Test
    public void testRequiredOptionGroupSupplied() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testPropertiesNull() throws Exception
    {
        options.addOption("a", false, "flag a");
        CommandLine cl = parser.parse(options, new String[]{"-a"}, (Properties) null);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testPropertiesWithArg() throws Exception
    {
        options.addOption("f", true, "file option");

        Properties props = new Properties();
        props.setProperty("f", "default.txt");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("f"));
        assertEquals("default.txt", cl.getOptionValue("f"));
    }

    @Test
    public void testPropertiesWithArgAlreadySetOnCommandLine() throws Exception
    {
        options.addOption("f", true, "file option");

        Properties props = new Properties();
        props.setProperty("f", "default.txt");

        CommandLine cl = parser.parse(options, new String[]{"-f", "custom.txt"}, props);
        assertTrue(cl.hasOption("f"));
        assertEquals("custom.txt", cl.getOptionValue("f"));
    }

    @Test
    public void testPropertiesBooleanOptions() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");
        options.addOption("d", false, "flag d");
        options.addOption("e", false, "flag e");

        Properties props = new Properties();
        props.setProperty("a", "yes");
        props.setProperty("b", "true");
        props.setProperty("c", "1");
        props.setProperty("d", "no");
        props.setProperty("e", "false");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertFalse(cl.hasOption("d"));
        assertFalse(cl.hasOption("e"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testPropertiesUnknownOption() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("unknown", "value");

        parser.parse(options, new String[]{}, props);
    }

    @Test
    public void testPropertiesOptionInAlreadySelectedGroup() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testParseNullArguments() throws Exception
    {
        options.addOption("a", false, "flag a");
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testParseWithStopAtNonOptionConstructor() throws Exception
    {
        options.addOption("a", false, "flag a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "arg1", "arg2"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    @Test
    public void testMultipleValuesOption() throws Exception
    {
        Option multi = Option.builder("m").hasArgs().build();
        options.addOption(multi);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "val3"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(3, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
        assertEquals("val3", values[2]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOption() throws Exception
    {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOptionWithEqual() throws Exception
    {
        parser.parse(options, new String[]{"--unknown=val"});
    }

    @Test
    public void testUnknownLongOptionWithStopAtNonOption() throws Exception
    {
        CommandLine cl = parser.parse(options, new String[]{"--unknown", "val"}, true);
        assertEquals(2, cl.getArgs().length);
        assertEquals("--unknown", cl.getArgs()[0]);
        assertEquals("val", cl.getArgs()[1]);
    }

    @Test
    public void testUnknownLongOptionWithEqualAndStopAtNonOption() throws Exception
    {
        CommandLine cl = parser.parse(options, new String[]{"--unknown=val"}, true);
        assertEquals(1, cl.getArgs().length);
        assertEquals("--unknown=val", cl.getArgs()[0]);
    }
}