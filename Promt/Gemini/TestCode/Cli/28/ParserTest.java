package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ParserTest
{
    private static class DummyParser extends Parser
    {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
        {
            if (arguments == null)
            {
                return new String[0];
            }
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < arguments.length; i++)
            {
                String arg = arguments[i];
                if (stopAtNonOption)
                {
                    if (arg.startsWith("-") && !opts.hasOption(arg))
                    {
                        list.addAll(Arrays.asList(arguments).subList(i, arguments.length));
                        break;
                    }
                    if (!arg.startsWith("-"))
                    {
                        list.addAll(Arrays.asList(arguments).subList(i, arguments.length));
                        break;
                    }
                }
                list.add(arg);
            }
            return list.toArray(new String[list.size()]);
        }
    }

    private static class SimplePassthroughParser extends Parser
    {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
        {
            return arguments != null ? arguments : new String[0];
        }
    }

    private Parser parser;
    private Options options;

    @Before
    public void setUp()
    {
        parser = new SimplePassthroughParser();
        options = new Options();
    }

    @Test
    public void testParseSimpleOptions() throws ParseException
    {
        options.addOption("a", "all", false, "toggle all");
        options.addOption("b", "batch", true, "batch size");

        String[] args = new String[] { "-a", "-b", "10", "extraArg" };
        CommandLine cl = parser.parse(options, args);

        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("10", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("extraArg", cl.getArgList().get(0));
    }

    @Test
    public void testParseNullArguments() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
        assertFalse(cl.hasOption("a"));
    }

    @Test
    public void testParseTwoArgOverload() throws ParseException
    {
        options.addOption("v", false, "verbose");
        CommandLine cl = parser.parse(options, new String[] { "-v" });
        assertTrue(cl.hasOption("v"));
    }

    @Test
    public void testParseThreeArgPropertiesOverload() throws ParseException
    {
        options.addOption("p", true, "property opt");
        Properties props = new Properties();
        props.setProperty("p", "propVal");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("p"));
        assertEquals("propVal", cl.getOptionValue("p"));
    }

    @Test
    public void testParseThreeArgStopAtNonOptionOverload() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "-a", "arg1", "-b" }, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("arg1", cl.getArgList().get(0));
        assertEquals("-b", cl.getArgList().get(1));
    }

    @Test
    public void testDoubleDashStopsOptionProcessing() throws ParseException
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        String[] args = new String[] { "-a", "--", "-b", "--", "foo" };
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        List argsList = cl.getArgList();
        assertEquals(2, argsList.size());
        assertEquals("-b", argsList.get(0));
        assertEquals("foo", argsList.get(1));
    }

    @Test
    public void testSingleDashWithStopAtNonOptionTrue() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "-a", "-", "tail" }, true);
        assertTrue(cl.hasOption("a"));
        List argsList = cl.getArgList();
        assertEquals(1, argsList.size());
        assertEquals("tail", argsList.get(0));
    }

    @Test
    public void testSingleDashWithStopAtNonOptionFalse() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "-a", "-", "tail" }, false);
        assertTrue(cl.hasOption("a"));
        List argsList = cl.getArgList();
        assertEquals(2, argsList.size());
        assertEquals("-", argsList.get(0));
        assertEquals("tail", argsList.get(1));
    }

    @Test
    public void testUnrecognizedOptionWithStopAtNonOptionTrue() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "-a", "-unknown", "extra" }, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("unknown"));
        List argsList = cl.getArgList();
        assertEquals(2, argsList.size());
        assertEquals("-unknown", argsList.get(0));
        assertEquals("extra", argsList.get(1));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOptionWithStopAtNonOptionFalse() throws ParseException
    {
        options.addOption("a", false, "option a");
        parser.parse(options, new String[] { "-a", "-unknown" }, false);
    }

    @Test
    public void testArgumentEncounterWithStopAtNonOptionTrue() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "nonOption", "-a" }, true);
        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("nonOption", cl.getArgList().get(0));
        assertEquals("-a", cl.getArgList().get(1));
    }

    @Test
    public void testArgumentEncounterWithStopAtNonOptionFalse() throws ParseException
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[] { "nonOption", "-a" }, false);
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("nonOption", cl.getArgList().get(0));
    }

    @Test
    public void testClearingValuesFromPreviousParse() throws ParseException
    {
        Option opt = OptionBuilder.hasArg().create('s');
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[] { "-s", "val1" });
        assertEquals("val1", cl1.getOptionValue('s'));

        CommandLine cl2 = parser.parse(options, new String[0]);
        assertFalse(cl2.hasOption('s'));
        assertNull(opt.getValue());
    }

    @Test
    public void testClearingOptionGroupSelection() throws ParseException
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        CommandLine cl1 = parser.parse(options, new String[] { "-a" });
        assertTrue(cl1.hasOption("a"));
        assertEquals("a", group.getSelected());

        CommandLine cl2 = parser.parse(options, new String[] { "-b" });
        assertTrue(cl2.hasOption("b"));
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testRequiredOptionPresent() throws ParseException
    {
        Option opt = new Option("r", "required-opt", false, "required");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[] { "-r" });
        assertTrue(cl.hasOption("r"));
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionMissing() throws ParseException
    {
        Option opt = new Option("r", "required-opt", false, "required");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[0]);
    }

    @Test
    public void testRequiredOptionGroupPresent() throws ParseException
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "opt A"));
        group.addOption(new Option("b", "opt B"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[] { "-a" });
        assertTrue(cl.hasOption("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionGroupMissing() throws ParseException
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "opt A"));
        group.addOption(new Option("b", "opt B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[0]);
    }

    @Test(expected = AlreadySelectedException.class)
    public void testOptionGroupMultipleSelectionThrows() throws ParseException
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "opt A"));
        group.addOption(new Option("b", "opt B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[] { "-a", "-b" });
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentException() throws ParseException
    {
        options.addOption(OptionBuilder.hasArg().create('c'));
        parser.parse(options, new String[] { "-c" });
    }

    @Test
    public void testProcessArgsStopsAtNextOption() throws ParseException
    {
        options.addOption(OptionBuilder.hasArgs(2).create('c'));
        options.addOption("d", false, "option d");

        CommandLine cl = parser.parse(options, new String[] { "-c", "val1", "-d" });
        assertTrue(cl.hasOption('c'));
        assertTrue(cl.hasOption('d'));
        assertEquals(1, cl.getOptionValues('c').length);
        assertEquals("val1", cl.getOptionValues('c')[0]);
    }

    @Test
    public void testProcessArgsWithStrippingQuotes() throws ParseException
    {
        options.addOption(OptionBuilder.hasArg().create('q'));
        CommandLine cl = parser.parse(options, new String[] { "-q", "\"quotedValue\"" });
        assertTrue(cl.hasOption('q'));
        assertEquals("quotedValue", cl.getOptionValue('q'));
    }

    @Test
    public void testProcessArgsStopsWhenMaxArgsExceeded() throws ParseException
    {
        Option opt = OptionBuilder.hasArg().create('x'); // max 1 arg
        options.addOption(opt);
        options.addOption("y", false, "opt y");

        CommandLine cl = parser.parse(options, new String[] { "-x", "val1", "val2" });
        assertTrue(cl.hasOption('x'));
        assertEquals("val1", cl.getOptionValue('x'));
        assertEquals(1, cl.getArgList().size());
        assertEquals("val2", cl.getArgList().get(0));
    }

    @Test
    public void testOptionalArgument() throws ParseException
    {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[] { "-o" });
        assertTrue(cl1.hasOption('o'));
        assertNull(cl1.getOptionValue('o'));

        CommandLine cl2 = parser.parse(options, new String[] { "-o", "optVal" });
        assertTrue(cl2.hasOption('o'));
        assertEquals("optVal", cl2.getOptionValue('o'));
    }

    @Test
    public void testMultipleArgsOption() throws ParseException
    {
        Option opt = OptionBuilder.hasArgs(3).create('m');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[] { "-m", "v1", "v2", "v3" });
        assertTrue(cl.hasOption('m'));
        String[] vals = cl.getOptionValues('m');
        assertEquals(3, vals.length);
        assertEquals("v1", vals[0]);
        assertEquals("v2", vals[1]);
        assertEquals("v3", vals[2]);
    }

    @Test
    public void testProcessPropertiesNullProperties() throws ParseException
    {
        options.addOption("a", false, "opt a");
        CommandLine cl = parser.parse(options, new String[] { "-a" }, null);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesWithArguments() throws ParseException
    {
        options.addOption(OptionBuilder.hasArg().create("config"));
        Properties props = new Properties();
        props.setProperty("config", "app.properties");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("config"));
        assertEquals("app.properties", cl.getOptionValue("config"));
    }

    @Test
    public void testProcessPropertiesDoesNotOverrideCommandLine() throws ParseException
    {
        options.addOption(OptionBuilder.hasArg().create("config"));
        Properties props = new Properties();
        props.setProperty("config", "propValue");

        CommandLine cl = parser.parse(options, new String[] { "-config", "cliValue" }, props);
        assertTrue(cl.hasOption("config"));
        assertEquals("cliValue", cl.getOptionValue("config"));
    }

    @Test
    public void testProcessPropertiesBooleanOptions() throws ParseException
    {
        options.addOption("v", false, "verbose");
        options.addOption("d", false, "debug");
        options.addOption("s", false, "silent");
        options.addOption("q", false, "quiet");

        Properties props = new Properties();
        props.setProperty("v", "yes");
        props.setProperty("d", "true");
        props.setProperty("s", "1");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("s"));
        assertFalse(cl.hasOption("q"));
    }

    @Test
    public void testProcessPropertiesBooleanFalseBreak() throws ParseException
    {
        options.addOption("v", false, "verbose");

        Properties props = new Properties();
        props.setProperty("v", "false");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertFalse(cl.hasOption("v"));
    }

    @Test
    public void testProcessPropertiesWithExistingValuesInOption() throws ParseException
    {
        Option opt = OptionBuilder.hasArg().create("p");
        options.addOption(opt);

        // Pre-fill an existing value directly in option (rare case)
        opt.addValueForProcessing("preVal");

        Properties props = new Properties();
        props.setProperty("p", "propVal");

        parser.setOptions(options);
        parser.cmd = new CommandLine();
        parser.processProperties(props);

        assertTrue(parser.cmd.hasOption("p"));
        assertEquals("preVal", opt.getValue());
    }

    @Test
    public void testProcessPropertiesWithInvalidValueExceptionHandled() throws ParseException
    {
        Option opt = new Option("n", "number", true, "numeric opt")
        {
            @Override
            public boolean addValue(String value)
            {
                throw new RuntimeException("Invalid value");
            }
        };
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("n", "invalid");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("n"));
    }

    @Test
    public void testGettersAndSetters()
    {
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
        assertTrue(parser.getRequiredOptions().isEmpty());

        Option req = new Option("r", true, "req");
        req.setRequired(true);
        options.addOption(req);

        parser.setOptions(options);
        assertEquals(1, parser.getRequiredOptions().size());
        assertEquals("r", parser.getRequiredOptions().get(0));
    }

    @Test
    public void testDummyParserFlattenIntegration() throws ParseException
    {
        Parser dummy = new DummyParser();
        options.addOption("a", false, "toggle a");
        options.addOption("b", true, "option b");

        CommandLine cl = dummy.parse(options, new String[] { "-a", "extra1", "-b", "valB" }, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(3, cl.getArgList().size());
        assertEquals("extra1", cl.getArgList().get(0));
        assertEquals("-b", cl.getArgList().get(1));
        assertEquals("valB", cl.getArgList().get(2));
    }

    @Test
    public void testDirectProcessArgsThrowsWhenMissingArg()
    {
        Option opt = new Option("t", true, "takes arg");
        List<String> list = Arrays.asList();
        ListIterator iter = list.listIterator();
        try
        {
            parser.processArgs(opt, iter);
            fail("Expected MissingArgumentException");
        }
        catch (MissingArgumentException e)
        {
            assertEquals(opt, e.getOption());
        }
        catch (ParseException e)
        {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testDirectProcessOptionNonExistentThrows()
    {
        parser.setOptions(options);
        List<String> list = Arrays.asList();
        try
        {
            parser.processOption("-nonexistent", list.listIterator());
            fail("Expected UnrecognizedOptionException");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-nonexistent", e.getOption());
        }
        catch (ParseException e)
        {
            fail("Unexpected exception: " + e);
        }
    }
}