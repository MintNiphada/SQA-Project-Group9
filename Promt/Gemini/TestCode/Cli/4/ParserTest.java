package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

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

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new TestParser();
        options = new Options();
    }

    @Test
    public void testParseSimpleOptions() throws Exception {
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option");

        String[] args = new String[]{"-a", "-b", "foo"};
        CommandLine cl = parser.parse(options, args);

        assertTrue("Option -a should be present", cl.hasOption("a"));
        assertTrue("Option -b should be present", cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testParseOverloads() throws Exception {
        options.addOption("a", false, "Alpha option");
        options.addOption("b", true, "Beta option");

        // Overload 1: parse(options, arguments)
        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));

        // Overload 2: parse(options, arguments, properties)
        Properties props = new Properties();
        props.setProperty("b", "propValue");
        CommandLine cl2 = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cl2.hasOption("a"));
        assertTrue(cl2.hasOption("b"));
        assertEquals("propValue", cl2.getOptionValue("b"));

        // Overload 3: parse(options, arguments, stopAtNonOption)
        CommandLine cl3 = parser.parse(options, new String[]{"nonOption", "-a"}, true);
        assertFalse(cl3.hasOption("a"));
        assertEquals(2, cl3.getArgs().length);
        assertEquals("nonOption", cl3.getArgs()[0]);
        assertEquals("-a", cl3.getArgs()[1]);
    }

    @Test
    public void testParseWithNullArguments() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testDoubleDashStopsOptionParsing() throws Exception {
        options.addOption("a", false, "Alpha option");
        options.addOption("b", false, "Beta option");

        String[] args = new String[]{"-a", "--", "-b", "arg1", "--", "arg2"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));

        String[] extraArgs = cl.getArgs();
        assertEquals(3, extraArgs.length);
        assertEquals("-b", extraArgs[0]);
        assertEquals("arg1", extraArgs[1]);
        assertEquals("arg2", extraArgs[2]);
    }

    @Test
    public void testSingleDashWithStopAtNonOptionFalse() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"-", "-a"};
        CommandLine cl = parser.parse(options, args, false);

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testSingleDashWithStopAtNonOptionTrue() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"-", "-a", "extra"};
        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test
    public void testUnknownOptionWithStopAtNonOptionTrue() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"-unknown", "-a"};
        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownOptionWithStopAtNonOptionFalse() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"-unknown"};
        parser.parse(options, args, false);
    }

    @Test
    public void testNonOptionArgumentWithStopAtNonOptionTrue() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"arg1", "-a", "arg2"};
        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("arg2", cl.getArgs()[2]);
    }

    @Test
    public void testNonOptionArgumentWithStopAtNonOptionFalse() throws Exception {
        options.addOption("a", false, "Alpha option");
        String[] args = new String[]{"arg1", "-a", "arg2"};
        CommandLine cl = parser.parse(options, args, false);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    @Test
    public void testRequiredOptionProvided() throws Exception {
        Option reqOpt = OptionBuilder.isRequired().withLongOpt("req").hasArg().create('r');
        options.addOption(reqOpt);

        CommandLine cl = parser.parse(options, new String[]{"-r", "val"});
        assertTrue(cl.hasOption("r"));
        assertEquals("val", cl.getOptionValue("r"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws Exception {
        Option reqOpt = OptionBuilder.isRequired().withLongOpt("req").hasArg().create('r');
        options.addOption(reqOpt);

        parser.parse(options, new String[]{});
    }

    @Test
    public void testRequiredOptionGroup() throws Exception {
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOptionGroup() throws Exception {
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    @Test(expected = AlreadySelectedException.class)
    public void testMultipleOptionsInSameGroup() throws Exception {
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingMandatoryArgument() throws Exception {
        options.addOption(OptionBuilder.hasArg().create('c'));
        parser.parse(options, new String[]{"-c"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentWhenNextTokenIsOption() throws Exception {
        options.addOption(OptionBuilder.hasArg().create('c'));
        options.addOption(OptionBuilder.create('d'));
        parser.parse(options, new String[]{"-c", "-d"});
    }

    @Test
    public void testOptionalArgumentProvided() throws Exception {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o", "optVal"});
        assertTrue(cl.hasOption("o"));
        assertEquals("optVal", cl.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgumentNotProvided() throws Exception {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
    }

    @Test
    public void testArgumentQuotesStripped() throws Exception {
        options.addOption(OptionBuilder.hasArg().create('q'));
        CommandLine cl = parser.parse(options, new String[]{"-q", "\"quoted string\""});
        assertEquals("quoted string", cl.getOptionValue("q"));
    }

    @Test
    public void testOptionMultipleValuesLimitedByArgsCount() throws Exception {
        Option opt = OptionBuilder.hasArgs(2).create('m');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "val3"});
        String[] values = cl.getOptionValues("m");
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);

        String[] extraArgs = cl.getArgs();
        assertEquals(1, extraArgs.length);
        assertEquals("val3", extraArgs[0]);
    }

    @Test
    public void testProcessPropertiesWithArgOptions() throws Exception {
        options.addOption("a", true, "Option A with arg");
        options.addOption("b", true, "Option B with arg");

        Properties props = new Properties();
        props.setProperty("a", "propA");
        props.setProperty("b", "propB");

        CommandLine cl = parser.parse(options, new String[]{"-a", "cliA"}, props);
        assertEquals("cliA", cl.getOptionValue("a"));
        assertEquals("propB", cl.getOptionValue("b"));
    }

    @Test
    public void testProcessPropertiesFlagOptionsTruthy() throws Exception {
        options.addOption("f1", false, "Flag 1");
        options.addOption("f2", false, "Flag 2");
        options.addOption("f3", false, "Flag 3");

        Properties props = new Properties();
        props.setProperty("f1", "yes");
        props.setProperty("f2", "TRUE");
        props.setProperty("f3", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("f1"));
        assertTrue(cl.hasOption("f2"));
        assertTrue(cl.hasOption("f3"));
    }

    @Test
    public void testProcessPropertiesFlagOptionsFalsyBreaksLoop() throws Exception {
        options.addOption("f1", false, "Flag 1");
        options.addOption("f2", false, "Flag 2");

        Properties props = new Properties();
        // Since Properties ordering is enumeration-dependent, test with single non-truthy value
        props.setProperty("f1", "false");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertFalse(cl.hasOption("f1"));
    }

    @Test
    public void testProcessPropertiesWithExceptionAddingValue() throws Exception {
        Option opt = new Option("e", true, "Option with 0 max args") {
            @Override
            public void addValue(String value) {
                throw new RuntimeException("Cannot add value");
            }
        };
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("e", "val");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("e"));
    }

    @Test
    public void testHelpOptionsClearedOnSubsequentParse() throws Exception {
        options.addOption("h", "help", true, "Help option");

        CommandLine cl1 = parser.parse(options, new String[]{"-h", "first"});
        assertEquals("first", cl1.getOptionValue("h"));

        CommandLine cl2 = parser.parse(options, new String[]{"-h", "second"});
        assertEquals("second", cl2.getOptionValue("h"));
        assertEquals(1, cl2.getOptionValues("h").length);
    }

    @Test
    public void testDirectProcessArgsThrowsExceptionWhenMissingArg() throws Exception {
        Option opt = new Option("reqArg", true, "Requires argument");
        List<String> emptyList = Arrays.asList();
        ListIterator<String> it = emptyList.listIterator();

        try {
            parser.processArgs(opt, it);
            fail("Should throw MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertTrue(e.getMessage().contains("reqArg"));
        }
    }
}