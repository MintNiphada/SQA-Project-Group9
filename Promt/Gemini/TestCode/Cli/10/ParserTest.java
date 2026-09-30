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

public class ParserTest {

    private TestableParser parser;

    private static class TestableParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            if (arguments == null) {
                return new String[0];
            }
            List<String> list = new ArrayList<String>();
            for (String arg : arguments) {
                list.add(arg);
            }
            return list.toArray(new String[list.size()]);
        }
    }

    @Before
    public void setUp() {
        parser = new TestableParser();
    }

    @Test
    public void testParseSimple() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha flag");
        options.addOption("b", "beta", true, "Beta option");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "value", "extra1", "extra2"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
        List args = cl.getArgList();
        assertEquals(2, args.size());
        assertEquals("extra1", args.get(0));
        assertEquals("extra2", args.get(1));
    }

    @Test
    public void testParseTwoArgumentsOverload() throws Exception {
        Options options = new Options();
        options.addOption("f", "file", true, "File option");

        CommandLine cl = parser.parse(options, new String[]{"-f", "test.txt"});
        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testParseThreeArgumentsPropertiesOverload() throws Exception {
        Options options = new Options();
        options.addOption("f", "file", true, "File option");

        Properties props = new Properties();
        props.setProperty("f", "propValue.txt");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("f"));
        assertEquals("propValue.txt", cl.getOptionValue("f"));
    }

    @Test
    public void testParseThreeArgumentsStopAtNonOptionOverload() throws Exception {
        Options options = new Options();
        options.addOption("a", "all", false, "All flag");

        CommandLine cl = parser.parse(options, new String[]{"nonOption", "-a"}, true);
        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    @Test
    public void testParseNullArguments() throws Exception {
        Options options = new Options();
        options.addOption("v", false, "Verbose");

        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("v"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testDoubleDashStopsOptions() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "arg1", "--", "arg2"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        String[] remaining = cl.getArgs();
        assertEquals(3, remaining.length);
        assertEquals("-b", remaining[0]);
        assertEquals("arg1", remaining[1]);
        assertEquals("arg2", remaining[2]);
    }

    @Test
    public void testSingleDashWithStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-", "rest"}, true);
        assertTrue(cl.hasOption("a"));
        String[] remaining = cl.getArgs();
        assertEquals(1, remaining.length);
        assertEquals("rest", remaining[0]);
    }

    @Test
    public void testSingleDashWithoutStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-", "rest"}, false);
        assertTrue(cl.hasOption("a"));
        String[] remaining = cl.getArgs();
        assertEquals(2, remaining.length);
        assertEquals("-", remaining[0]);
        assertEquals("rest", remaining[1]);
    }

    @Test
    public void testUnknownOptionWithStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-unknown", "extra"}, true);
        assertTrue(cl.hasOption("a"));
        String[] remaining = cl.getArgs();
        assertEquals(2, remaining.length);
        assertEquals("-unknown", remaining[0]);
        assertEquals("extra", remaining[1]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownOptionWithoutStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");

        parser.parse(options, new String[]{"-a", "-unknown", "extra"}, false);
    }

    @Test
    public void testStopAtFirstNonOptionArg() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");

        CommandLine cl = parser.parse(options, new String[]{"nonOption", "-a", "-b"}, true);
        assertFalse(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        String[] remaining = cl.getArgs();
        assertEquals(3, remaining.length);
        assertEquals("nonOption", remaining[0]);
        assertEquals("-a", remaining[1]);
        assertEquals("-b", remaining[2]);
    }

    @Test
    public void testRequiredOptionPresent() throws Exception {
        Options options = new Options();
        Option req = new Option("r", "req", true, "Required option");
        req.setRequired(true);
        options.addOption(req);

        CommandLine cl = parser.parse(options, new String[]{"-r", "val"});
        assertTrue(cl.hasOption("r"));
        assertEquals("val", cl.getOptionValue("r"));
    }

    @Test
    public void testMissingSingleRequiredOption() {
        Options options = new Options();
        Option req = new Option("r", "req", true, "Required option");
        req.setRequired(true);
        options.addOption(req);

        try {
            parser.parse(options, new String[]{"extra"});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().startsWith("Missing required option: "));
            assertTrue(e.getMessage().contains("r"));
        } catch (ParseException e) {
            fail("Unexpected exception type: " + e);
        }
    }

    @Test
    public void testMissingMultipleRequiredOptions() {
        Options options = new Options();
        Option req1 = new Option("r", "req1", false, "Required option 1");
        req1.setRequired(true);
        Option req2 = new Option("s", "req2", false, "Required option 2");
        req2.setRequired(true);
        options.addOption(req1);
        options.addOption(req2);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().startsWith("Missing required options: "));
            assertTrue(e.getMessage().contains("r"));
            assertTrue(e.getMessage().contains("s"));
        } catch (ParseException e) {
            fail("Unexpected exception type: " + e);
        }
    }

    @Test
    public void testRequiredOptionGroupSatisfied() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = new Option("f", "file", false, "file option");
        Option opt2 = new Option("d", "dir", false, "dir option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-f"});
        assertTrue(cl.hasOption("f"));
        assertFalse(cl.hasOption("d"));
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionGroupMissing() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = new Option("f", "file", false, "file option");
        Option opt2 = new Option("d", "dir", false, "dir option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"arg1"});
    }

    @Test
    public void testOptionGroupNotRequired() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        Option opt1 = new Option("f", "file", false, "file option");
        group.addOption(opt1);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-f"});
        assertTrue(cl.hasOption("f"));
    }

    @Test
    public void testProcessArgsWithMultipleValues() throws Exception {
        Options options = new Options();
        Option multi = new Option("m", "multi", true, "multiple values");
        multi.setArgs(2);
        options.addOption(multi);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "extra"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    @Test
    public void testProcessArgsStopsAtNextOption() throws Exception {
        Options options = new Options();
        Option multi = new Option("m", "multi", true, "multiple values");
        multi.setArgs(Option.UNLIMITED_VALUES);
        Option flag = new Option("f", "flag", false, "flag");
        options.addOption(multi);
        options.addOption(flag);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "-f", "extra"});
        assertTrue(cl.hasOption("m"));
        assertTrue(cl.hasOption("f"));
        assertEquals(2, cl.getOptionValues("m").length);
        assertEquals("val1", cl.getOptionValues("m")[0]);
        assertEquals("val2", cl.getOptionValues("m")[1]);
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    @Test
    public void testProcessArgsStopsWhenMaxArgsReached() throws Exception {
        Options options = new Options();
        Option single = new Option("s", "single", true, "single value");
        single.setArgs(1);
        options.addOption(single);

        CommandLine cl = parser.parse(options, new String[]{"-s", "val1", "val2"});
        assertTrue(cl.hasOption("s"));
        assertEquals("val1", cl.getOptionValue("s"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("val2", cl.getArgs()[0]);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgument() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", "opt", true, "requires argument");
        options.addOption(opt);

        parser.parse(options, new String[]{"-o"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgumentFollowedByOption() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", "opt", true, "requires argument");
        Option flag = new Option("f", "flag", false, "flag");
        options.addOption(opt);
        options.addOption(flag);

        parser.parse(options, new String[]{"-o", "-f"});
    }

    @Test
    public void testOptionalArgumentPresent() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", "opt", true, "optional argument");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o", "val"});
        assertTrue(cl.hasOption("o"));
        assertEquals("val", cl.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgumentOmitted() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", "opt", true, "optional argument");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
    }

    @Test
    public void testStripQuotesInArgs() throws Exception {
        Options options = new Options();
        options.addOption("q", true, "quoted option");

        CommandLine cl = parser.parse(options, new String[]{"-q", "\"hello world\""});
        assertTrue(cl.hasOption("q"));
        assertEquals("hello world", cl.getOptionValue("q"));
    }

    @Test
    public void testProcessPropertiesArgOption() throws Exception {
        Options options = new Options();
        options.addOption("p", "prop", true, "Property option");

        Properties properties = new Properties();
        properties.setProperty("p", "fromProperties");

        CommandLine cl = parser.parse(options, new String[0], properties);
        assertTrue(cl.hasOption("p"));
        assertEquals("fromProperties", cl.getOptionValue("p"));
    }

    @Test
    public void testProcessPropertiesDoesNotOverrideCommandLine() throws Exception {
        Options options = new Options();
        options.addOption("p", "prop", true, "Property option");

        Properties properties = new Properties();
        properties.setProperty("p", "fromProperties");

        CommandLine cl = parser.parse(options, new String[]{"-p", "fromCommandLine"}, properties);
        assertTrue(cl.hasOption("p"));
        assertEquals("fromCommandLine", cl.getOptionValue("p"));
    }

    @Test
    public void testProcessPropertiesFlags() throws Exception {
        Options options = new Options();
        options.addOption("y", "yesFlag", false, "Yes Flag");
        options.addOption("t", "trueFlag", false, "True Flag");
        options.addOption("o", "oneFlag", false, "One Flag");
        options.addOption("n", "noFlag", false, "No Flag");

        Properties properties = new Properties();
        properties.setProperty("y", "yes");
        properties.setProperty("t", "TrUe");
        properties.setProperty("o", "1");

        CommandLine cl = parser.parse(options, new String[0], properties);
        assertTrue(cl.hasOption("y"));
        assertTrue(cl.hasOption("t"));
        assertTrue(cl.hasOption("o"));

        Properties falseProps = new Properties();
        falseProps.setProperty("n", "no");
        CommandLine cl2 = parser.parse(options, new String[0], falseProps);
        assertFalse(cl2.hasOption("n"));
    }

    @Test
    public void testPreviousOptionValuesClearedBetweenParses() throws Exception {
        Options options = new Options();
        options.addOption("v", "val", true, "Value option");

        CommandLine cl1 = parser.parse(options, new String[]{"-v", "first"});
        assertEquals("first", cl1.getOptionValue("v"));

        CommandLine cl2 = parser.parse(options, new String[0]);
        assertFalse(cl2.hasOption("v"));
        assertNull(cl2.getOptionValue("v"));
    }

    @Test
    public void testGettersAndSetters() {
        Options options = new Options();
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }

    @Test
    public void testProcessArgsDirectCall() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", true, "test opt");
        options.addOption(opt);
        parser.setOptions(options);

        List<String> list = Arrays.asList("argVal");
        ListIterator<String> it = list.listIterator();
        parser.processArgs(opt, it);
        assertEquals("argVal", opt.getValue());
    }
}