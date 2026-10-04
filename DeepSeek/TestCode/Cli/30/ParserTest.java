package org.apache.commons.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

public class ParserTest {

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException {
            return arguments.clone();
        }
    }

    @Test
    public void testParseWithNullArguments() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, (String[]) null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseDoubleDashEatTheRest() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "alpha");
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a", "foo"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    @Test
    public void testParseSingleDashStopAtNonOptionFalse() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"-"}, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test
    public void testParseSingleDashStopAtNonOptionTrue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"-", "b"}, true);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test
    public void testParseUnrecognizedOptionStopAtNonOptionFalse() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"--unknown"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("--unknown", e.getOption());
        }
    }

    @Test
    public void testParseUnrecognizedOptionStopAtNonOptionTrue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"--unknown", "val"}, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("--unknown", cmd.getArgs()[0]);
        assertEquals("val", cmd.getArgs()[1]);
    }

    @Test
    public void testParseArgumentStopAtNonOptionTrue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"arg1", "-b"}, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("arg1", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
    }

    @Test
    public void testParseWithOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "alpha");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParseOptionWithArgument() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("a").hasArg().build());
        CommandLine cmd = parser.parse(options, new String[]{"-a", "value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseMissingRequiredOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option requiredOpt = Option.builder("x").required().build();
        options.addOption(requiredOpt);
        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            List missing = e.getMissingOptions();
            assertEquals(1, missing.size());
            assertEquals("x", missing.get(0));
        }
    }

    @Test
    public void testParseRequiredOptionPresent() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("x").required().build());
        CommandLine cmd = parser.parse(options, new String[]{"-x"});
        assertTrue(cmd.hasOption("x"));
    }

    @Test
    public void testParseRequiredOptionGroupMissing() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            List missing = e.getMissingOptions();
            assertEquals(1, missing.size());
            assertTrue(missing.get(0) instanceof OptionGroup);
        }
    }

    @Test
    public void testParseRequiredOptionGroupPresent() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option a = Option.builder("a").build();
        Option b = Option.builder("b").build();
        group.addOption(a);
        group.addOption(b);
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesNull() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[0], (Properties) null, false);
        assertNotNull(cmd);
    }

    @Test
    public void testParsePropertiesAddOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("p").hasArg().build());
        Properties props = new Properties();
        props.setProperty("p", "propValue");
        CommandLine cmd = parser.parse(options, new String[0], props, false);
        assertTrue(cmd.hasOption("p"));
        assertEquals("propValue", cmd.getOptionValue("p"));
    }

    @Test
    public void testParsePropertiesNonArgOptionTrueValue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("b").build());
        Properties props = new Properties();
        props.setProperty("b", "true");
        CommandLine cmd = parser.parse(options, new String[0], props, false);
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesNonArgOptionYesValue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("b").build());
        Properties props = new Properties();
        props.setProperty("b", "YES");
        CommandLine cmd = parser.parse(options, new String[0], props, false);
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesNonArgOptionOneValue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("b").build());
        Properties props = new Properties();
        props.setProperty("b", "1");
        CommandLine cmd = parser.parse(options, new String[0], props, false);
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesNonArgOptionNoValue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("b").build());
        Properties props = new Properties();
        props.setProperty("b", "other");
        CommandLine cmd = parser.parse(options, new String[0], props, false);
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesOptionAlreadyHasOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("a").hasArg().build());
        CommandLine cmd = parser.parse(options, new String[]{"-a", "cliValue"}, null, false);
        assertTrue(cmd.hasOption("a"));
        assertEquals("cliValue", cmd.getOptionValue("a"));
        Properties props = new Properties();
        props.setProperty("a", "propValue");
        cmd = parser.parse(options, new String[]{"-a", "cliValue"}, props, false);
        assertEquals("cliValue", cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessArgsOptionWithNoValueOptionalArg() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("o").hasArg().optionalArg(true).build();
        options.addOption(opt);
        List<String> tokens = new ArrayList<String>();
        tokens.add("non-option");
        ListIterator iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertNull(opt.getValues());
    }

    @Test
    public void testProcessArgsOptionWithValue() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("o").hasArg().build();
        options.addOption(opt);
        List<String> tokens = new ArrayList<String>();
        tokens.add("value1");
        tokens.add("value2");
        ListIterator iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals(2, opt.getValues().length);
        assertEquals("value1", opt.getValues()[0]);
        assertEquals("value2", opt.getValues()[1]);
    }

    @Test
    public void testProcessArgsOptionStopAtOtherOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("o").hasArg().build();
        options.addOption(opt);
        options.addOption("s", false, "stop");
        List<String> tokens = new ArrayList<String>();
        tokens.add("value1");
        tokens.add("-s");
        tokens.add("notused");
        ListIterator iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals(1, opt.getValues().length);
        assertEquals("value1", opt.getValues()[0]);
        assertEquals("-s", iter.next());
    }

    @Test
    public void testProcessArgsOptionRuntimeException() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("o", true, "desc") {
            @Override
            public void addValueForProcessing(String value) {
                throw new RuntimeException("test");
            }
        };
        options.addOption(opt);
        List<String> tokens = new ArrayList<String>();
        tokens.add("value1");
        tokens.add("next");
        ListIterator iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals(0, opt.getValues() == null ? 0 : opt.getValues().length);
        assertEquals("value1", iter.next());
    }

    @Test
    public void testProcessArgsMissingRequiredArg() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("o").hasArg().build();
        options.addOption(opt);
        List<String> tokens = new ArrayList<String>();
        ListIterator iter = tokens.listIterator();
        try {
            parser.processArgs(opt, iter);
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
        }
    }

    @Test
    public void testProcessOptionUnrecognized() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        List<String> tokens = new ArrayList<String>();
        try {
            parser.processOption("--unknown", tokens.listIterator());
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("--unknown", e.getOption());
        }
    }

    @Test
    public void testProcessOptionRecognizedNoArg() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "alpha");
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        List<String> tokens = new ArrayList<String>();
        parser.processOption("-a", tokens.listIterator());
        assertTrue(parser.cmd.hasOption("a"));
    }

    @Test
    public void testProcessOptionRecognizedWithArg() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("a").hasArg().build());
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        List<String> tokens = new ArrayList<String>();
        tokens.add("arg1");
        parser.processOption("-a", tokens.listIterator());
        assertTrue(parser.cmd.hasOption("a"));
        assertEquals("arg1", parser.cmd.getOptionValue("a"));
    }

    @Test
    public void testUpdateRequiredOptionsRequiredOption() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("r").required().build();
        options.addOption(opt);
        parser.setOptions(options);
        parser.processOption("-r", new ArrayList<String>().listIterator());
        assertFalse(parser.getRequiredOptions().contains("r"));
    }

    @Test
    public void testUpdateRequiredOptionsGroup() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = Option.builder("a").build();
        Option opt2 = Option.builder("b").build();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        parser.setOptions(options);
        parser.processOption("-a", new ArrayList<String>().listIterator());
        assertFalse(parser.getRequiredOptions().contains(group));
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testCheckRequiredOptionsEmpty() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        parser.setOptions(options);
        parser.checkRequiredOptions();
    }

    @Test
    public void testCheckRequiredOptionsNotEmpty() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("x").required().build());
        parser.setOptions(options);
        try {
            parser.checkRequiredOptions();
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertEquals(1, e.getMissingOptions().size());
        }
    }

    @Test
    public void testSetAndGetOptions() {
        TestParser parser = new TestParser();
        Options options = new Options();
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
    }

    @Test
    public void testGetRequiredOptionsInitially() {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption(Option.builder("a").required().build());
        parser.setOptions(options);
        List req = parser.getRequiredOptions();
        assertEquals(1, req.size());
        assertEquals("a", req.get(0));
    }

    @Test
    public void testProcessPropertiesOptionNotInOptions() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        parser.setOptions(options);
        Properties props = new Properties();
        props.setProperty("missing", "value");
        try {
            parser.processProperties(props);
            fail("Expected NullPointerException due to missing option");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testProcessPropertiesOptionWithArgumentAndExistingValues() throws ParseException {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = Option.builder("p").hasArg().build();
        opt.addValue("existing");
        options.addOption(opt);
        parser.setOptions(options);
        Properties props = new Properties();
        props.setProperty("p", "newvalue");
        parser.processProperties(props);
        assertEquals("existing", opt.getValue());
    }
}
