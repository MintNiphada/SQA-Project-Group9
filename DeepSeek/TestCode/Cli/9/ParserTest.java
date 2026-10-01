package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.*;

public class ParserTest {

    private Options options;
    private TestParser parser;

    // Concrete parser that returns arguments unchanged
    private static class TestParser extends Parser {
        // Capture arguments passed to flatten for verification
        public String[] lastFlattenArgs;
        public Options lastFlattenOpts;
        public boolean lastFlattenStopAtNonOption;

        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            this.lastFlattenOpts = opts;
            this.lastFlattenArgs = arguments;
            this.lastFlattenStopAtNonOption = stopAtNonOption;
            return arguments; // identity
        }
    }

    @Before
    public void setUp() {
        parser = new TestParser();
        options = new Options();
    }

    // Helper to create an option quickly
    private Option option(String opt, String desc, boolean hasArg, boolean required) {
        Option o = OptionBuilder.withLongOpt(opt)
                .withDescription(desc)
                .hasArg(hasArg)
                .isRequired(required)
                .create(opt);
        return o;
    }

    // ==================== parse method tests ====================

    @Test
    public void testParseNullArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, (String[]) null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
        assertEquals(0, cmd.getOptions().length);
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseDoubleDashEatTheRest() throws ParseException {
        options.addOption(option("a", "a", false, false));
        String[] args = {"--", "-a", "foo"};
        CommandLine cmd = parser.parse(options, args);
        // "--" should cause eatTheRest, so "-a" and "foo" become args, not options
        assertFalse(cmd.hasOption("a"));
        List<String> argList = Arrays.asList(cmd.getArgs());
        assertTrue(argList.contains("-a"));
        assertTrue(argList.contains("foo"));
        // double dash itself not added
        assertFalse(argList.contains("--"));
    }

    @Test
    public void testParseSingleDashWithoutStopAtNonOption() throws ParseException {
        String[] args = {"-"};
        CommandLine cmd = parser.parse(options, args, false);
        // single dash is added as an argument
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test
    public void testParseSingleDashWithStopAtNonOption() throws ParseException {
        String[] args = {"-", "other"};
        CommandLine cmd = parser.parse(options, args, true);
        // stopAtNonOption true, single dash triggers eatTheRest
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("other", cmd.getArgs()[1]);
    }

    @Test
    public void testParseOption() throws ParseException {
        options.addOption(option("a", "a", false, false));
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnrecognizedOption() throws ParseException {
        String[] args = {"-x"};
        parser.parse(options, args);
    }

    @Test
    public void testParseOptionWithArgument() throws ParseException {
        options.addOption(option("a", "a", true, false));
        String[] args = {"-a", "value"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testParseOptionMissingRequiredArgument() throws ParseException {
        options.addOption(option("a", "a", true, false));
        String[] args = {"-a"};
        parser.parse(options, args);
    }

    @Test
    public void testParseOptionWithOptionalArgumentMissing() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("a")
                .withDescription("a")
                .hasOptionalArg()
                .create("a");
        options.addOption(opt);
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
    }

    @Test
    public void testParseStopAtNonOptionWithOption() throws ParseException {
        options.addOption(option("a", "a", false, false));
        String[] args = {"-a", "foo", "-b"};
        CommandLine cmd = parser.parse(options, args, true);
        // -a is recognized, then foo is non-option -> stop, -b becomes arg
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
    }

    @Test
    public void testParseStopAtNonOptionWithUnrecognizedOption() throws ParseException {
        String[] args = {"-x", "foo"};
        CommandLine cmd = parser.parse(options, args, true);
        // -x is unrecognized, stopAtNonOption true -> eatTheRest, -x and foo become args
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    @Test
    public void testParseWithProperties() throws ParseException {
        options.addOption(option("a", "a", false, false));
        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParseWithPropertiesNoValueForBoolean() throws ParseException {
        options.addOption(option("a", "a", false, false));
        Properties props = new Properties();
        props.setProperty("a", "false");
        // value not yes/true/1 -> break, option not added
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseWithPropertiesHasArg() throws ParseException {
        options.addOption(option("a", "a", true, false));
        Properties props = new Properties();
        props.setProperty("a", "propValue");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
        assertEquals("propValue", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseWithPropertiesExistingOption() throws ParseException {
        options.addOption(option("a", "a", false, false));
        Properties props = new Properties();
        props.setProperty("a", "true");
        // first add option via command line
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        // property should be skipped because cmd already has option
        // but option already present, no effect
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParseWithPropertiesHasArgExistingValues() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        // pre-set a value on the option (simulate previous parse)
        // but parse clears values, so this won't happen. We'll test via property after adding via cmd.
        // Actually, processProperties checks if opt.getValues() == null or length 0.
        // We'll test that if option already has values from command line, property value is not added.
        // But parse clears values first, so we need to test processProperties directly.
        // We'll test processProperties separately.
    }

    // ==================== processProperties tests ====================

    @Test
    public void testProcessPropertiesNull() {
        parser.processProperties(null);
        // no exception
    }

    @Test
    public void testProcessPropertiesAddsOption() throws ParseException {
        options.addOption(option("a", "a", false, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "yes");
        parser.processProperties(props);
        assertTrue(parser.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesBooleanYesTrue1() throws ParseException {
        options.addOption(option("a", "a", false, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "1");
        parser.processProperties(props);
        assertTrue(parser.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesBooleanNoValue() throws ParseException {
        options.addOption(option("a", "a", false, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "no");
        parser.processProperties(props);
        assertFalse(parser.cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesHasArgAddsValue() throws ParseException {
        options.addOption(option("a", "a", true, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "val");
        parser.processProperties(props);
        assertTrue(parser.cmd.hasOption("a"));
        assertEquals("val", parser.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesHasArgExistingValues() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        // simulate that option already has a value (e.g., from command line)
        opt.addValueForProcessing("existing");
        Properties props = new Properties();
        props.setProperty("a", "newVal");
        parser.processProperties(props);
        // value should not be overwritten because opt.getValues() != null and length > 0
        assertEquals("existing", parser.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesHasArgNoValues() throws ParseException {
        options.addOption(option("a", "a", true, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        Properties props = new Properties();
        props.setProperty("a", "val");
        parser.processProperties(props);
        assertEquals("val", parser.cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesOptionAlreadyInCmd() throws ParseException {
        options.addOption(option("a", "a", false, false));
        parser.setOptions(options);
        parser.cmd = new CommandLine();
        parser.cmd.addOption(options.getOption("a"));
        Properties props = new Properties();
        props.setProperty("a", "true");
        parser.processProperties(props);
        // should not add again, but already present
        assertTrue(parser.cmd.hasOption("a"));
    }

    // ==================== checkRequiredOptions tests ====================

    @Test(expected = MissingOptionException.class)
    public void testCheckRequiredOptionsMissing() throws ParseException {
        options.addOption(option("a", "a", false, true));
        parser.setOptions(options);
        parser.checkRequiredOptions();
    }

    @Test
    public void testCheckRequiredOptionsSatisfied() throws ParseException {
        options.addOption(option("a", "a", false, true));
        parser.setOptions(options);
        // remove the required option manually to simulate it was processed
        parser.getRequiredOptions().remove("a");
        parser.checkRequiredOptions(); // should not throw
    }

    @Test
    public void testCheckRequiredOptionsMultipleMissing() {
        options.addOption(option("a", "a", false, true));
        options.addOption(option("b", "b", false, true));
        parser.setOptions(options);
        try {
            parser.checkRequiredOptions();
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("a"));
            assertTrue(msg.contains("b"));
        }
    }

    // ==================== processOption tests ====================

    @Test(expected = UnrecognizedOptionException.class)
    public void testProcessOptionUnrecognized() throws ParseException {
        parser.setOptions(options);
        parser.processOption("-x", Collections.emptyListIterator());
    }

    @Test
    public void testProcessOptionRequiredRemoval() throws ParseException {
        options.addOption(option("a", "a", false, true));
        parser.setOptions(options);
        assertTrue(parser.getRequiredOptions().contains("a"));
        parser.processOption("-a", Collections.emptyListIterator());
        assertFalse(parser.getRequiredOptions().contains("a"));
    }

    @Test
    public void testProcessOptionGroupRequiredRemoval() throws ParseException {
        Option opt1 = option("a", "a", false, false);
        Option opt2 = option("b", "b", false, false);
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);
        options.addOptionGroup(group);
        parser.setOptions(options);
        assertTrue(parser.getRequiredOptions().contains(group));
        parser.processOption("-a", Collections.emptyListIterator());
        assertFalse(parser.getRequiredOptions().contains(group));
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testProcessOptionGroupNotRequired() throws ParseException {
        Option opt1 = option("a", "a", false, false);
        Option opt2 = option("b", "b", false, false);
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        parser.setOptions(options);
        parser.processOption("-a", Collections.emptyListIterator());
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testProcessOptionWithArg() throws ParseException {
        options.addOption(option("a", "a", true, false));
        parser.setOptions(options);
        List<String> tokens = Arrays.asList("-a", "value");
        ListIterator<String> iter = tokens.listIterator();
        parser.processOption("-a", iter);
        assertTrue(parser.cmd.hasOption("a"));
        assertEquals("value", parser.cmd.getOptionValue("a"));
    }

    // ==================== processArgs tests ====================

    @Test
    public void testProcessArgsAddsValues() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = Arrays.asList("val1", "val2", "-b");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals(2, opt.getValues().length);
        assertEquals("val1", opt.getValues()[0]);
        assertEquals("val2", opt.getValues()[1]);
    }

    @Test
    public void testProcessArgsStopsAtOption() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        options.addOption(option("b", "b", false, false));
        parser.setOptions(options);
        List<String> tokens = Arrays.asList("-b", "val");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        // should stop before -b, so no values added
        assertNull(opt.getValues());
        // iterator should be positioned before -b
        assertTrue(iter.hasNext());
        assertEquals("-b", iter.next());
    }

    @Test(expected = MissingArgumentException.class)
    public void testProcessArgsMissingRequired() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        parser.setOptions(options);
        parser.processArgs(opt, Collections.emptyListIterator());
    }

    @Test
    public void testProcessArgsOptionalArgMissing() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("a")
                .withDescription("a")
                .hasOptionalArg()
                .create("a");
        options.addOption(opt);
        parser.setOptions(options);
        parser.processArgs(opt, Collections.emptyListIterator());
        // no exception, values remain null
        assertNull(opt.getValues());
    }

    @Test
    public void testProcessArgsRuntimeExceptionOnAddValue() throws ParseException {
        Option opt = new Option("a", "a") {
            @Override
            public void addValueForProcessing(String value) {
                throw new RuntimeException("test");
            }
        };
        opt.setArgs(1);
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = Arrays.asList("value");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        // exception caught, iterator moved back, no value added
        assertNull(opt.getValues());
        assertTrue(iter.hasNext());
        assertEquals("value", iter.next());
    }

    @Test
    public void testProcessArgsStripsQuotes() throws ParseException {
        Option opt = option("a", "a", true, false);
        options.addOption(opt);
        parser.setOptions(options);
        List<String> tokens = Arrays.asList("\"quoted\"");
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);
        assertEquals("quoted", opt.getValues()[0]);
    }

    // ==================== flatten interaction tests ====================

    @Test
    public void testFlattenReceivesCorrectArguments() throws ParseException {
        String[] args = {"-a", "b"};
        parser.parse(options, args, false);
        assertArrayEquals(args, parser.lastFlattenArgs);
        assertSame(options, parser.lastFlattenOpts);
        assertFalse(parser.lastFlattenStopAtNonOption);
    }

    @Test
    public void testFlattenWithStopAtNonOption() throws ParseException {
        String[] args = {"-a", "b"};
        parser.parse(options, args, true);
        assertTrue(parser.lastFlattenStopAtNonOption);
    }

    // ==================== setOptions/getOptions/getRequiredOptions ====================

    @Test
    public void testSetOptionsStoresOptionsAndRequiredOptions() {
        options.addOption(option("a", "a", false, true));
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
        assertEquals(1, parser.getRequiredOptions().size());
        assertTrue(parser.getRequiredOptions().contains("a"));
    }
}
