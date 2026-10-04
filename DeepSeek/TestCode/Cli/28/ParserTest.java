package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class ParserTest {

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments;
        }
    }

    private Parser parser() {
        return new TestParser();
    }

    private Options optionsWithOption(String opt, boolean hasArg) {
        Options options = new Options();
        options.addOption(opt, hasArg, "description");
        return options;
    }

    private Options optionsWithOption(String opt) {
        return optionsWithOption(opt, false);
    }

    private Option option(String opt, boolean hasArg) {
        return new Option(opt, hasArg, "description");
    }

    @Test
    public void testParseEmpty() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
        assertEquals(0, cmd.getOptions().length);
    }

    @Test
    public void testParseNullArguments() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseArgumentsOnly() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"foo", "bar"});
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("bar", cmd.getArgs()[1]);
    }

    @Test
    public void testParseSingleDash() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"-"});
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test
    public void testParseDoubleDashOnly() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"--"});
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseDoubleDashWithArgs() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"--", "a", "b"});
        assertEquals(2, cmd.getArgs().length);
        assertEquals("a", cmd.getArgs()[0]);
        assertEquals("b", cmd.getArgs()[1]);
    }

    @Test
    public void testParseOption() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseOptionWithValue() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a", true);
        CommandLine cmd = parser.parse(options, new String[]{"-a", "value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnrecognizedOption() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        parser.parse(options, new String[]{"-b"});
    }

    @Test
    public void testParseStopAtNonOptionFalse() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "foo", "-a"}, false);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
    }

    @Test
    public void testParseStopAtNonOptionTrue() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "foo", "-a"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test
    public void testParseStopAtNonOptionTrueWithUnrecognized() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-x", "foo"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    @Test
    public void testParseStopAtNonOptionTrueWithDoubleDash() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    @Test
    public void testParseStopAtNonOptionTrueWithSingleDash() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    @Test
    public void testParseClearsOptionValues() throws Exception {
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("old");
        options.addOption(opt);
        Parser parser = parser();
        parser.parse(options, new String[]{"-a", "new"});
        assertNull(opt.getValues());
        assertEquals("new", parser.parse(options, new String[]{"-a", "new"}).getOptionValue("a"));
    }

    @Test
    public void testParseClearsGroupSelection() throws Exception {
        Options options = new Options();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        options.addOptionGroup(group);
        Parser parser = parser();
        parser.parse(options, new String[]{"-b"});
        assertNull(group.getSelected());
        assertTrue(parser.parse(options, new String[]{"-a"}).hasOption("a"));
    }

    @Test
    public void testProcessOptionRequiredRemoval() throws Exception {
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        Parser parser = parser();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws Exception {
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        Parser parser = parser();
        parser.parse(options, new String[0]);
    }

    @Test
    public void testProcessOptionInGroup() throws Exception {
        Options options = new Options();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        Parser parser = parser();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testProcessOptionInRequiredGroup() throws Exception {
        Options options = new Options();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);
        options.addOptionGroup(group);
        Parser parser = parser();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredGroup() throws Exception {
        Options options = new Options();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);
        options.addOptionGroup(group);
        Parser parser = parser();
        parser.parse(options, new String[0]);
    }

    @Test
    public void testProcessArgsWithValue() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a", true);
        Option opt = options.getOption("a");
        List<String> tokens = new ArrayList<>();
        tokens.add("value");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals("value", opt.getValue());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testProcessArgsWithOptionNext() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt1 = new Option("a", true, "desc");
        Option opt2 = new Option("b", false, "desc");
        options.addOption(opt1);
        options.addOption(opt2);
        List<String> tokens = new ArrayList<>();
        tokens.add("-b");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt1, iter);
        assertNull(opt1.getValues());
        assertTrue(iter.hasNext());
        assertEquals("-b", iter.next());
    }

    @Test
    public void testProcessArgsWithRuntimeException() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        opt.setArgs(1);
        options.addOption(opt);
        List<String> tokens = new ArrayList<>();
        tokens.add("val1");
        tokens.add("val2");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals("val1", opt.getValue());
        assertTrue(iter.hasNext());
        assertEquals("val2", iter.next());
    }

    @Test(expected = MissingArgumentException.class)
    public void testProcessArgsMissingRequiredArgument() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a", true);
        Option opt = options.getOption("a");
        List<String> tokens = new ArrayList<>();
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
    }

    @Test
    public void testProcessArgsOptionalArgumentMissing() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        opt.setOptionalArg(true);
        options.addOption(opt);
        List<String> tokens = new ArrayList<>();
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertNull(opt.getValues());
    }

    @Test
    public void testProcessPropertiesNull() throws Exception {
        Parser parser = parser();
        parser.processProperties(null);
        assertTrue(true);
    }

    @Test
    public void testProcessPropertiesNoArgOptionTrue() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "true");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesNoArgOptionYes() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "yes");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesNoArgOptionOne() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "1");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesNoArgOptionInvalidValue() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        options.addOption(opt);
        Option opt2 = new Option("b", false, "desc");
        options.addOption(opt2);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "no");
        props.setProperty("b", "true");
        spy.processProperties(props);
        assertFalse(spy.cmd.hasOption("a"));
        assertFalse(spy.cmd.hasOption("b"));
    }

    @Test
    public void testProcessPropertiesWithArg() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "value");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
        assertEquals("value", spy.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesWithArgAlreadyHasValue() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("existing");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "new");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
        assertEquals("existing", spy.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesWithArgRuntimeException() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        opt.setArgs(1);
        opt.addValueForProcessing("first");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "second");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
        assertEquals("first", spy.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesOptionAlreadyInCmd() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        options.addOption(opt);
        Parser spy = new TestParser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return new String[0];
            }
        };
        spy.setOptions(options);
        spy.cmd = new CommandLine();
        spy.cmd.addOption(opt);
        Properties props = new Properties();
        props.setProperty("a", "other");
        spy.processProperties(props);
        assertTrue(spy.cmd.hasOption("a"));
        assertNull(spy.cmd.getOptionValue("a"));
    }

    @Test
    public void testCheckRequiredOptionsEmpty() throws Exception {
        Parser parser = parser();
        parser.setOptions(new Options());
        parser.setRequiredOptionsList(new ArrayList());
        parser.checkRequiredOptions();
        assertTrue(true);
    }

    @Test(expected = MissingOptionException.class)
    public void testCheckRequiredOptionsNonEmpty() throws Exception {
        Parser parser = parser();
        List required = new ArrayList();
        required.add("a");
        parser.setRequiredOptionsList(required);
        parser.checkRequiredOptions();
    }

    @Test
    public void testSetOptionsAndGetters() {
        Parser parser = parser();
        Options options = new Options();
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }

    @Test
    public void testSetOptionsInitializesRequiredOptions() {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        parser.setOptions(options);
        List required = parser.getRequiredOptions();
        assertEquals(1, required.size());
        assertEquals("a", required.get(0));
    }

    @Test
    public void testProcessOptionUnrecognized() throws Exception {
        Parser parser = parser();
        Options options = optionsWithOption("a");
        parser.setOptions(options);
        List<String> tokens = new ArrayList<>();
        tokens.add("-x");
        ListIterator<String> iter = tokens.listIterator();
        try {
            parser.processOption("-x", iter);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("-x", e.getOption());
        }
    }

    @Test
    public void testProcessOptionRequiredRemovalDirect() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = new ArrayList<>();
        ListIterator<String> iter = tokens.listIterator();
        parser.processOption("-a", iter);
        assertTrue(parser.getRequiredOptions().isEmpty());
    }

    @Test
    public void testProcessOptionGroupSelectionDirect() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        parser.setOptions(options);
        List<String> tokens = new ArrayList<>();
        ListIterator<String> iter = tokens.listIterator();
        parser.processOption("-a", iter);
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testProcessOptionWithArg() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", true, "desc");
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = new ArrayList<>();
        tokens.add("-a");
        tokens.add("value");
        ListIterator<String> iter = tokens.listIterator();
        parser.processOption("-a", iter);
        assertTrue(parser.cmd.hasOption("a"));
        assertEquals("value", parser.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessOptionNoArg() throws Exception {
        Parser parser = parser();
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = new ArrayList<>();
        tokens.add("-a");
        ListIterator<String> iter = tokens.listIterator();
        parser.processOption("-a", iter);
        assertTrue(parser.cmd.hasOption("a"));
    }

    private void setRequiredOptionsList(List list) {
        try {
            java.lang.reflect.Field field = Parser.class.getDeclaredField("requiredOptions");
            field.setAccessible(true);
            field.set(parser(), list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
