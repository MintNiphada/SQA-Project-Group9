package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

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
        Option optA = new Option("a", "alpha", false, "Alpha option");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testSimpleLongOption() throws Exception
    {
        Option optAlpha = new Option("a", "alpha", false, "Alpha option");
        options.addOption(optAlpha);

        CommandLine cl = parser.parse(options, new String[]{"--alpha"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testOptionWithArgument() throws Exception
    {
        Option optB = new Option("b", "beta", true, "Beta option");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testOptionWithArgumentAttached() throws Exception
    {
        Option optB = new Option("b", "beta", true, "Beta option");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-bvalue"});
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testOptionWithArgumentEqualsSignShort() throws Exception
    {
        Option optB = new Option("b", "beta", true, "Beta option");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b=value"});
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testOptionWithArgumentEqualsSignLong() throws Exception
    {
        Option optBeta = new Option("b", "beta", true, "Beta option");
        options.addOption(optBeta);

        CommandLine cl = parser.parse(options, new String[]{"--beta=value"});
        assertTrue(cl.hasOption("beta"));
        assertEquals("value", cl.getOptionValue("beta"));
    }

    @Test
    public void testOptionWithQuotedArgument() throws Exception
    {
        Option optB = new Option("b", true, "Beta option");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b", "\"quoted value\""});
        assertTrue(cl.hasOption("b"));
        assertEquals("quoted value", cl.getOptionValue("b"));
    }

    @Test
    public void testNegativeNumberArgument() throws Exception
    {
        Option optN = new Option("n", "num", true, "Number option");
        options.addOption(optN);

        CommandLine cl = parser.parse(options, new String[]{"-n", "-42.5"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    @Test
    public void testSingleDashArgument() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a", "-"});
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testDoubleDashStopToken() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", true, "Beta");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "foo"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    @Test
    public void testConcatenatedShortOptions() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        Option optC = new Option("c", false, "Gamma");
        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        CommandLine cl = parser.parse(options, new String[]{"-abc"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testConcatenatedShortOptionsWithArgAtEnd() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", true, "Beta");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-abfoo"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
    }

    @Test
    public void testConcatenatedOptionsStopAtNonOption() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-azfoo", "-bar"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("zfoo", cl.getArgs()[0]);
        assertEquals("-bar", cl.getArgs()[1]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testConcatenatedOptionsUnrecognized() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        parser.parse(options, new String[]{"-az"});
    }

    @Test
    public void testJavaPropertyOptionWithoutEquals() throws Exception
    {
        Option optD = new Option("D", "Define property");
        optD.setArgs(2);
        optD.setValueSeparator('=');
        options.addOption(optD);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    @Test
    public void testJavaPropertyOptionUnlimitedValues() throws Exception
    {
        Option optD = new Option("D", "Define property");
        optD.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(optD);

        CommandLine cl = parser.parse(options, new String[]{"-Dfoo"});
        assertTrue(cl.hasOption("D"));
        assertEquals("foo", cl.getOptionValue("D"));
    }

    @Test
    public void testLongPrefixOption() throws Exception
    {
        Option optXmx = new Option(null, "Xmx", true, "Set max heap");
        options.addOption(optXmx);

        CommandLine cl = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cl.hasOption("Xmx"));
        assertEquals("512m", cl.getOptionValue("Xmx"));
    }

    @Test
    public void testSingleHyphenLongOption() throws Exception
    {
        Option optLong = new Option(null, "longopt", false, "Long option");
        options.addOption(optLong);

        CommandLine cl = parser.parse(options, new String[]{"-longopt"});
        assertTrue(cl.hasOption("longopt"));
    }

    @Test
    public void testSingleHyphenLongOptionWithEquals() throws Exception
    {
        Option optLong = new Option(null, "longopt", true, "Long option with arg");
        options.addOption(optLong);

        CommandLine cl = parser.parse(options, new String[]{"-longopt=value"});
        assertTrue(cl.hasOption("longopt"));
        assertEquals("value", cl.getOptionValue("longopt"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws Exception
    {
        Option optA = new Option("a", false, "Required Alpha");
        optA.setRequired(true);
        options.addOption(optA);

        parser.parse(options, new String[]{});
    }

    @Test
    public void testSatisfiedRequiredOption() throws Exception
    {
        Option optA = new Option("a", false, "Required Alpha");
        optA.setRequired(true);
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgument() throws Exception
    {
        Option optB = new Option("b", true, "Option requiring arg");
        optB.setRequired(false);
        options.addOption(optB);

        parser.parse(options, new String[]{"-b"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgumentNextIsOption() throws Exception
    {
        Option optB = new Option("b", true, "Option requiring arg");
        Option optC = new Option("c", false, "Other option");
        options.addOption(optB);
        options.addOption(optC);

        parser.parse(options, new String[]{"-b", "-c"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedShortOption() throws Exception
    {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws Exception
    {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOptionWithEquals() throws Exception
    {
        parser.parse(options, new String[]{"--unknown=val"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testOptionDoesNotAcceptArgWithEquals() throws Exception
    {
        Option optA = new Option("a", "alpha", false, "No arg option");
        options.addOption(optA);

        parser.parse(options, new String[]{"--alpha=val"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testShortOptionDoesNotAcceptArgWithEquals() throws Exception
    {
        Option optA = new Option("a", false, "No arg option");
        options.addOption(optA);

        parser.parse(options, new String[]{"-a=val"});
    }

    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOptionWithoutEquals() throws Exception
    {
        options.addOption(new Option(null, "foo", false, "Foo"));
        options.addOption(new Option(null, "foobar", false, "Foobar"));

        parser.parse(options, new String[]{"--fo"});
    }

    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOptionWithEquals() throws Exception
    {
        options.addOption(new Option(null, "foo", true, "Foo"));
        options.addOption(new Option(null, "foobar", true, "Foobar"));

        parser.parse(options, new String[]{"--fo=val"});
    }

    @Test
    public void testPartialLongOptionMatch() throws Exception
    {
        options.addOption(new Option(null, "foobar", true, "Foobar"));

        CommandLine cl = parser.parse(options, new String[]{"--fo=baz"});
        assertTrue(cl.hasOption("foobar"));
        assertEquals("baz", cl.getOptionValue("foobar"));
    }

    @Test
    public void testOptionGroupRequired() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals("a", group.getSelected());
    }

    @Test(expected = MissingOptionException.class)
    public void testOptionGroupRequiredMissing() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    @Test(expected = AlreadySelectedException.class)
    public void testOptionGroupAlreadySelected() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    @Test
    public void testStopAtNonOption() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption1", "-b", "nonOption2"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("nonOption1", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
        assertEquals("nonOption2", cl.getArgs()[2]);
    }

    @Test
    public void testStopAtNonOptionWithUnrecognizedOption() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-x", "foo"}, true);
        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-x", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    @Test
    public void testPropertiesHandling() throws Exception
    {
        Option optA = new Option("a", false, "Alpha flag");
        Option optB = new Option("b", true, "Beta with arg");
        Option optC = new Option("c", false, "Gamma flag yes");
        Option optD = new Option("d", false, "Delta flag 1");
        Option optE = new Option("e", false, "Epsilon flag no");
        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);
        options.addOption(optD);
        options.addOption(optE);

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "propValue");
        props.setProperty("c", "yes");
        props.setProperty("d", "1");
        props.setProperty("e", "no");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("propValue", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
        assertTrue(cl.hasOption("d"));
        assertFalse(cl.hasOption("e"));
    }

    @Test
    public void testPropertiesDoesNotOverrideCommandLine() throws Exception
    {
        Option optB = new Option("b", true, "Beta with arg");
        options.addOption(optB);

        Properties props = new Properties();
        props.setProperty("b", "fromProps");

        CommandLine cl = parser.parse(options, new String[]{"-b", "fromArgs"}, props);
        assertTrue(cl.hasOption("b"));
        assertEquals("fromArgs", cl.getOptionValue("b"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testPropertiesWithUndefinedOption() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("unknown", "value");

        parser.parse(options, new String[]{}, props);
    }

    @Test
    public void testPropertiesWithOptionGroupAlreadySelected() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testNullArguments() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testReuseParserInstanceCleansState() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        Option optB = new Option("b", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-b"});
        assertTrue(cl2.hasOption("b"));
        assertFalse(cl2.hasOption("a"));
    }

    @Test
    public void testParseMethodOverloads() throws Exception
    {
        Option optA = new Option("a", false, "Alpha");
        options.addOption(optA);

        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a"}, true);
        assertTrue(cl2.hasOption("a"));

        Properties props = new Properties();
        CommandLine cl3 = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cl3.hasOption("a"));
    }
}