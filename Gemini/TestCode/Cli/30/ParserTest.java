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
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException {
            if (arguments == null) {
                return new String[0];
            }
            List<String> result = new ArrayList<String>();
            for (String arg : arguments) {
                if ("--flatten-error".equals(arg)) {
                    throw new ParseException("Flatten error");
                }
                result.add(arg);
            }
            return result.toArray(new String[result.size()]);
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
    public void testParseSimple() throws ParseException {
        Option optA = new Option("a", "alpha", false, "Option A");
        Option optB = new Option("b", "beta", true, "Option B");
        options.addOption(optA);
        options.addOption(optB);

        String[] args = new String[]{"-a", "-b", "valueB", "extraArg"};
        CommandLine cl = parser.parse(options, args);

        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("valueB", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extraArg", cl.getArgs()[0]);
    }

    @Test
    public void testParseOverloads() throws ParseException {
        Option optA = new Option("a", "alpha", false, "Option A");
        options.addOption(optA);

        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a"}, false);
        assertTrue(cl2.hasOption("a"));

        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cl3 = parser.parse(options, new String[0], props);
        assertTrue(cl3.hasOption("a"));

        CommandLine cl4 = parser.parse(options, new String[0], props, false);
        assertTrue(cl4.hasOption("a"));
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
        assertEquals(0, cl.getOptions().length);
    }

    @Test
    public void testDoubleDashStopsParsingOptions() throws ParseException {
        Option optA = new Option("a", false, "Option A");
        options.addOption(optA);

        String[] args = new String[]{"--", "-a", "--", "arg1"};
        CommandLine cl = parser.parse(options, args);

        assertFalse(cl.hasOption("a"));
        List<String> argList = cl.getArgList();
        assertEquals(2, argList.size());
        assertEquals("-a", argList.get(0));
        assertEquals("arg1", argList.get(1));
    }

    @Test
    public void testSingleDashHandling() throws ParseException {
        Option optA = new Option("a", false, "Option A");
        options.addOption(optA);

        // stopAtNonOption = false
        CommandLine cl1 = parser.parse(options, new String[]{"-", "-a"}, false);
        assertTrue(cl1.hasOption("a"));
        assertEquals(1, cl1.getArgs().length);
        assertEquals("-", cl1.getArgs()[0]);

        // stopAtNonOption = true
        CommandLine cl2 = parser.parse(options, new String[]{"-", "-a", "arg2"}, true);
        assertFalse(cl2.hasOption("a"));
        assertEquals(2, cl2.getArgs().length);
        assertEquals("-a", cl2.getArgs()[0]);
        assertEquals("arg2", cl2.getArgs()[1]);
    }

    @Test
    public void testUnrecognizedOptionThrowsException() {
        options.addOption("a", false, "Option A");
        try {
            parser.parse(options, new String[]{"-z"}, false);
            fail("Expected UnrecognizedOptionException for unrecognized option");
        } catch (UnrecognizedOptionException e) {
            assertEquals("-z", e.getOption());
        } catch (ParseException e) {
            fail("Expected UnrecognizedOptionException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testUnrecognizedOptionWithStopAtNonOption() throws ParseException {
        options.addOption("a", false, "Option A");
        CommandLine cl = parser.parse(options, new String[]{"-z", "-a", "arg1"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("-z", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("arg1", cl.getArgs()[2]);
    }

    @Test
    public void testNonOptionArgumentStopsParsingWhenStopAtNonOption() throws ParseException {
        options.addOption("a", false, "Option A");
        CommandLine cl = parser.parse(options, new String[]{"foo", "-a", "bar"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("foo", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("bar", cl.getArgs()[2]);
    }

    @Test
    public void testMissingRequiredOption() {
        Option opt = new Option("r", "req", false, "Required Option");
        opt.setRequired(true);
        options.addOption(opt);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertNotNull(e.getMissingOptions());
            assertEquals(1, e.getMissingOptions().size());
            assertEquals("r", e.getMissingOptions().get(0));
        } catch (ParseException e) {
            fail("Expected MissingOptionException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testSatisfiedRequiredOption() throws ParseException {
        Option opt = new Option("r", "req", false, "Required Option");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    @Test
    public void testRequiredOptionGroupMissing() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "Option A"));
        group.addOption(new Option("b", "Option B"));
        options.addOptionGroup(group);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertNotNull(e.getMissingOptions());
            assertEquals(1, e.getMissingOptions().size());
            assertEquals(group, e.getMissingOptions().get(0));
        } catch (ParseException e) {
            fail("Expected MissingOptionException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testRequiredOptionGroupSatisfied() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "Option A"));
        group.addOption(new Option("b", "Option B"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-b"});
        assertTrue(cl.hasOption("b"));
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testMultipleOptionGroupSelectionThrowsException() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "Option A"));
        group.addOption(new Option("b", "Option B"));
        options.addOptionGroup(group);

        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            assertEquals(group, e.getOptionGroup());
            assertEquals("b", e.getOption().getOpt());
        } catch (ParseException e) {
            fail("Expected AlreadySelectedException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testMissingArgumentException() {
        Option opt = new Option("a", true, "Option A with Arg");
        options.addOption(opt);

        try {
            parser.parse(options, new String[]{"-a"});
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertEquals(opt, e.getOption());
        } catch (ParseException e) {
            fail("Expected MissingArgumentException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testMissingArgumentFollowedByOption() {
        Option optA = new Option("a", true, "Option A");
        Option optB = new Option("b", false, "Option B");
        options.addOption(optA);
        options.addOption(optB);

        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertEquals(optA, e.getOption());
        } catch (ParseException e) {
            fail("Expected MissingArgumentException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testOptionalArgumentPresentAndAbsent() throws ParseException {
        Option opt = new Option("a", true, "Option A");
        opt.setOptionalArg(true);
        options.addOption(opt);

        // When absent
        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));
        assertNull(cl1.getOptionValue("a"));

        // When present
        CommandLine cl2 = parser.parse(options, new String[]{"-a", "val"});
        assertTrue(cl2.hasOption("a"));
        assertEquals("val", cl2.getOptionValue("a"));
    }

    @Test
    public void testMultipleArgumentsForOption() throws ParseException {
        Option opt = new Option("a", true, "Option A");
        opt.setArgs(3);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-a", "v1", "v2", "v3", "extra"});
        assertTrue(cl.hasOption("a"));
        String[] values = cl.getOptionValues("a");
        assertEquals(3, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
        assertEquals("v3", values[2]);
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    @Test
    public void testArgumentQuotingStripped() throws ParseException {
        Option opt = new Option("a", true, "Option A");
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-a", "\"quoted value\""});
        assertEquals("quoted value", cl.getOptionValue("a"));
    }

    @Test
    public void testProcessProperties() throws ParseException {
        Option optA = new Option("a", true, "Option A");
        Option optB = new Option("b", false, "Option B");
        Option optC = new Option("c", false, "Option C");
        Option optD = new Option("d", false, "Option D");
        Option optE = new Option("e", false, "Option E");
        Option optReq = new Option("r", false, "Option Req");
        optReq.setRequired(true);

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);
        options.addOption(optD);
        options.addOption(optE);
        options.addOption(optReq);

        Properties props = new Properties();
        props.setProperty("a", "propValueA");
        props.setProperty("b", "yes");
        props.setProperty("c", "true");
        props.setProperty("d", "1");
        props.setProperty("e", "no");
        props.setProperty("r", "true");

        CommandLine cl = parser.parse(options, new String[0], props);

        assertTrue(cl.hasOption("a"));
        assertEquals("propValueA", cl.getOptionValue("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertTrue(cl.hasOption("d"));
        assertFalse(cl.hasOption("e"));
        assertTrue(cl.hasOption("r"));
    }

    @Test
    public void testPropertiesDoNotOverrideCommandLine() throws ParseException {
        Option optA = new Option("a", true, "Option A");
        options.addOption(optA);

        Properties props = new Properties();
        props.setProperty("a", "propValue");

        CommandLine cl = parser.parse(options, new String[]{"-a", "cliValue"}, props);
        assertEquals("cliValue", cl.getOptionValue("a"));
    }

    @Test
    public void testPropertyForOptionInOptionGroup() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", false, "Option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("a", "true");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    @Test
    public void testOptionValuesClearedBetweenRuns() throws ParseException {
        Option optA = new Option("a", true, "Option A");
        options.addOption(optA);

        CommandLine cl1 = parser.parse(options, new String[]{"-a", "firstVal"});
        assertEquals("firstVal", cl1.getOptionValue("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a", "secondVal"});
        assertEquals("secondVal", cl2.getOptionValue("a"));
        assertEquals(1, cl2.getOptionValues("a").length);
    }

    @Test
    public void testOptionGroupClearedBetweenRuns() throws ParseException {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", false, "Option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a"});
        assertEquals("a", group.getSelected());

        // Second run with optB should not fail with AlreadySelectedException
        parser.parse(options, new String[]{"-b"});
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testFlattenExceptionPropagated() {
        try {
            parser.parse(options, new String[]{"--flatten-error"});
            fail("Expected ParseException from flatten");
        } catch (ParseException e) {
            assertEquals("Flatten error", e.getMessage());
        }
    }

    @Test
    public void testProcessArgsWithExcessArgsForOption() throws ParseException {
        Option opt = new Option("a", true, "Option A with 1 arg");
        opt.setArgs(1);

        List<String> argsList = new ArrayList<String>(Arrays.asList("arg1", "arg2"));
        ListIterator<String> iter = argsList.listIterator();

        parser.setOptions(options);
        parser.processArgs(opt, iter);

        assertEquals("arg1", opt.getValue());
        assertTrue(iter.hasNext());
        assertEquals("arg2", iter.next());
    }

    @Test
    public void testPropertiesWithInvalidArgumentDoesNotThrow() throws ParseException {
        Option opt = new Option("a", true, "Option A with int validator") {
            @Override
            public void addValueForProcessing(String value) {
                throw new RuntimeException("Validation error");
            }
        };
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("a", "invalidVal");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("a"));
        assertNull(cl.getOptionValue("a"));
    }

    @Test
    public void testGettersAndSetters() {
        assertNull(parser.getOptions());
        assertNull(parser.getRequiredOptions());

        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
        assertEquals(0, parser.getRequiredOptions().size());
    }
}