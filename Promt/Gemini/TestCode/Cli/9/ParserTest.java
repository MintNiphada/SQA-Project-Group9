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

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            if (arguments == null) {
                return new String[0];
            }
            return arguments;
        }
    }

    private TestParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new TestParser();
        options = new Options();
    }

    @Test
    public void testSetAndGetOptions() {
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }

    @Test
    public void testParseSimple() throws Exception {
        Option optA = new Option("a", "alpha", false, "Option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testParseNullArguments() throws Exception {
        CommandLine cl = parser.parse(options, (String[]) null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testParseWithOptionArgument() throws Exception {
        Option optB = new Option("b", "beta", true, "Option b with arg");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b", "valueB"});
        assertTrue(cl.hasOption("b"));
        assertEquals("valueB", cl.getOptionValue("b"));
    }

    @Test
    public void testParseWithQuotedArgument() throws Exception {
        Option optB = new Option("b", true, "Option b with arg");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b", "\"quoted value\""});
        assertTrue(cl.hasOption("b"));
        assertEquals("quoted value", cl.getOptionValue("b"));
    }

    @Test
    public void testParseWithMultipleOptionArguments() throws Exception {
        Option optM = new Option("m", "multi", true, "Multiple args");
        optM.setArgs(2);
        options.addOption(optM);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentException() throws Exception {
        Option optB = new Option("b", true, "Option b with arg");
        options.addOption(optB);

        parser.parse(options, new String[]{"-b"});
    }

    @Test
    public void testOptionalArgumentProvided() throws Exception {
        Option optO = new Option("o", true, "Option with optional arg");
        optO.setOptionalArg(true);
        options.addOption(optO);

        CommandLine cl = parser.parse(options, new String[]{"-o", "val"});
        assertTrue(cl.hasOption("o"));
        assertEquals("val", cl.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgumentOmitted() throws Exception {
        Option optO = new Option("o", true, "Option with optional arg");
        optO.setOptionalArg(true);
        options.addOption(optO);

        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgumentFollowedByOption() throws Exception {
        Option optO = new Option("o", true, "Option with optional arg");
        optO.setOptionalArg(true);
        Option optA = new Option("a", false, "Option a");
        options.addOption(optO);
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-o", "-a"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
        assertTrue(cl.hasOption("a"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws Exception {
        options.addOption(new Option("a", false, "Option a"));
        parser.parse(options, new String[]{"-unknown"});
    }

    @Test
    public void testDoubleDashStopsOptionParsing() throws Exception {
        Option optA = new Option("a", false, "Option a");
        Option optB = new Option("b", false, "Option b");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "arg1", "--", "arg2"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));

        List args = cl.getArgList();
        assertEquals(3, args.size());
        assertEquals("-b", args.get(0));
        assertEquals("arg1", args.get(1));
        assertEquals("arg2", args.get(2));
    }

    @Test
    public void testSingleDashWithStopAtNonOptionFalse() throws Exception {
        Option optA = new Option("a", false, "Option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a", "-", "arg1"}, false);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    @Test
    public void testSingleDashWithStopAtNonOptionTrue() throws Exception {
        Option optA = new Option("a", false, "Option a");
        Option optB = new Option("b", false, "Option b");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "-", "-b", "arg1"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    @Test
    public void testUnrecognizedOptionWithStopAtNonOptionTrue() throws Exception {
        Option optA = new Option("a", false, "Option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a", "-unrecognized", "-a", "extra"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("-unrecognized", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("extra", cl.getArgs()[2]);
    }

    @Test
    public void testArgumentWithStopAtNonOptionTrue() throws Exception {
        Option optA = new Option("a", false, "Option a");
        Option optB = new Option("b", false, "Option b");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOptionArg", "-b"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOptionArg", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    @Test
    public void testArgumentWithStopAtNonOptionFalse() throws Exception {
        Option optA = new Option("a", false, "Option a");
        Option optB = new Option("b", false, "Option b");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOptionArg", "-b"}, false);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("nonOptionArg", cl.getArgs()[0]);
    }

    @Test
    public void testRequiredOptionPresent() throws Exception {
        Option optR = new Option("r", "required", false, "Required option");
        optR.setRequired(true);
        options.addOption(optR);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    @Test
    public void testSingleMissingRequiredOption() {
        Option optR = new Option("r", "required", false, "Required option");
        optR.setRequired(true);
        options.addOption(optR);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException ex) {
            assertTrue(ex.getMessage().startsWith("Missing required option:"));
        } catch (ParseException ex) {
            fail("Unexpected ParseException: " + ex);
        }
    }

    @Test
    public void testMultipleMissingRequiredOptions() {
        Option optR1 = new Option("r1", "required1", false, "Required option 1");
        optR1.setRequired(true);
        Option optR2 = new Option("r2", "required2", false, "Required option 2");
        optR2.setRequired(true);
        options.addOption(optR1);
        options.addOption(optR2);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException ex) {
            assertTrue(ex.getMessage().startsWith("Missing required options:"));
        } catch (ParseException ex) {
            fail("Unexpected ParseException: " + ex);
        }
    }

    @Test
    public void testOptionGroupRequiredAndSelected() throws Exception {
        Option opt1 = new Option("f", "file", false, "File");
        Option opt2 = new Option("d", "dir", false, "Dir");

        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-f"});
        assertTrue(cl.hasOption("f"));
        assertEquals("f", group.getSelected());
    }

    @Test(expected = MissingOptionException.class)
    public void testOptionGroupRequiredMissing() throws Exception {
        Option opt1 = new Option("f", "file", false, "File");
        Option opt2 = new Option("d", "dir", false, "Dir");

        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    @Test
    public void testOptionGroupNonRequired() throws Exception {
        Option opt1 = new Option("f", "file", false, "File");
        Option opt2 = new Option("d", "dir", false, "Dir");

        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-d"});
        assertTrue(cl.hasOption("d"));
        assertEquals("d", group.getSelected());
    }

    @Test
    public void testProcessPropertiesNull() throws Exception {
        Option optA = new Option("a", false, "Option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a"}, (Properties) null);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesOptionAlreadyPresent() throws Exception {
        Option optA = new Option("a", true, "Option a");
        options.addOption(optA);

        Properties props = new Properties();
        props.setProperty("a", "propValue");

        CommandLine cl = parser.parse(options, new String[]{"-a", "cmdValue"}, props);
        assertTrue(cl.hasOption("a"));
        assertEquals("cmdValue", cl.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesWithArg() throws Exception {
        Option optA = new Option("a", true, "Option a");
        options.addOption(optA);

        Properties props = new Properties();
        props.setProperty("a", "propValue");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("a"));
        assertEquals("propValue", cl.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesWithBooleanFlags() throws Exception {
        Option optYes = new Option("y", false, "Option yes");
        Option optTrue = new Option("t", false, "Option true");
        Option optOne = new Option("o", false, "Option 1");
        Option optNo = new Option("n", false, "Option no");

        options.addOption(optYes);
        options.addOption(optTrue);
        options.addOption(optOne);
        options.addOption(optNo);

        Properties props = new Properties();
        props.setProperty("y", "yes");
        props.setProperty("t", "true");
        props.setProperty("o", "1");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("y"));
        assertTrue(cl.hasOption("t"));
        assertTrue(cl.hasOption("o"));
    }

    @Test
    public void testProcessPropertiesWithNonTrueValueBreaksLoop() throws Exception {
        Option optNo = new Option("n", false, "Option no");
        options.addOption(optNo);

        Properties props = new Properties();
        props.setProperty("n", "false");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertFalse(cl.hasOption("n"));
    }

    @Test
    public void testProcessPropertiesArgExceptionHandled() throws Exception {
        Option optA = new Option("a", true, "Option a");
        optA.setArgs(0); // Cannot accept values
        options.addOption(optA);

        Properties props = new Properties();
        props.setProperty("a", "val");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testClearValuesOnHelpOptions() throws Exception {
        Option optA = new Option("a", true, "Option a");
        options.addOption(optA);

        CommandLine cl1 = parser.parse(options, new String[]{"-a", "first"});
        assertEquals("first", cl1.getOptionValue("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a", "second"});
        assertEquals("second", cl2.getOptionValue("a"));
        assertEquals(1, optA.getValues().length);
    }

    @Test
    public void testProcessArgsStopsOnRuntimeException() throws Exception {
        Option optA = new Option("a", true, "Option a");
        optA.setArgs(1); // Accepts only 1 arg
        options.addOption(optA);

        List tokenList = new ArrayList(Arrays.asList("val1", "val2"));
        ListIterator iter = tokenList.listIterator();

        parser.setOptions(options);
        parser.processArgs(optA, iter);

        assertEquals("val1", optA.getValue());
        assertTrue(iter.hasNext());
        assertEquals("val2", iter.next());
    }
}