package org.apache.commons.cli;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    private Options options;
    private TestParser parser;

    // Concrete parser for testing
    private static class TestParser extends Parser {
        private String[] lastFlattenArgs;
        private Options lastFlattenOpts;
        private boolean lastFlattenStopAtNonOption;

        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            this.lastFlattenOpts = opts;
            this.lastFlattenArgs = arguments;
            this.lastFlattenStopAtNonOption = stopAtNonOption;
            // Return arguments as-is for simple testing
            return arguments;
        }

        public String[] getLastFlattenArgs() {
            return lastFlattenArgs;
        }

        public Options getLastFlattenOpts() {
            return lastFlattenOpts;
        }

        public boolean isLastFlattenStopAtNonOption() {
            return lastFlattenStopAtNonOption;
        }
    }

    // Option that throws RuntimeException when addValueForProcessing is called
    private static class ThrowingOption extends Option {
        public ThrowingOption(String opt, String description) {
            super(opt, description);
        }

        @Override
        public void addValueForProcessing(String value) {
            throw new RuntimeException("Simulated failure");
        }
    }

    @Before
    public void setUp() {
        options = new Options();
        parser = new TestParser();
    }

    @After
    public void tearDown() {
        options = null;
        parser = null;
    }

    // Helper to create a simple option with no argument
    private Option createOption(String opt, String desc) {
        return new Option(opt, desc);
    }

    // Helper to create an option with argument (required)
    private Option createOptionWithArg(String opt, String desc) {
        return new Option(opt, true, desc);
    }

    // Helper to create an option with optional argument
    private Option createOptionWithOptionalArg(String opt, String desc) {
        Option option = new Option(opt, desc);
        option.setOptionalArg(true);
        return option;
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseDoubleDash() throws ParseException {
        String[] args = {"--", "foo", "bar"};
        CommandLine cmd = parser.parse(options, args);
        assertNotNull(cmd);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("bar", cmd.getArgs()[1]);
    }

    @Test
    public void testParseSingleDashStopAtNonOptionFalse() throws ParseException {
        String[] args = {"-"};
        CommandLine cmd = parser.parse(options, args, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test
    public void testParseSingleDashStopAtNonOptionTrue() throws ParseException {
        String[] args = {"-", "next"};
        CommandLine cmd = parser.parse(options, args, true);
        // "-" triggers eatTheRest, so "next" is added as arg
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("next", cmd.getArgs()[1]);
    }

    @Test
    public void testParseOption() throws ParseException {
        options.addOption(createOption("a", "option a"));
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
    public void testParseOptionWithArg() throws ParseException {
        options.addOption(createOptionWithArg("a", "option a"));
        String[] args = {"-a", "value"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseOptionWithOptionalArgMissing() throws ParseException {
        options.addOption(createOptionWithOptionalArg("a", "option a"));
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testParseOptionWithArgMissing() throws ParseException {
        options.addOption(createOptionWithArg("a", "option a"));
        String[] args = {"-a"};
        parser.parse(options, args);
    }

    @Test
    public void testParseStopAtNonOption() throws ParseException {
        options.addOption(createOption("a", "option a"));
        String[] args = {"foo", "-a"};
        CommandLine cmd = parser.parse(options, args, true);
        // "foo" triggers eatTheRest, so "-a" is added as arg, not processed
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test
    public void testParseStopAtNonOptionWithUnrecognizedOption() throws ParseException {
        String[] args = {"-x", "bar"};
        CommandLine cmd = parser.parse(options, args, true);
        // "-x" is unrecognized, so eatTheRest, add "-x" as arg, then "bar"
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("bar", cmd.getArgs()[1]);
    }

    @Test
    public void testProcessPropertiesNull() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0], null);
        assertNotNull(cmd);
    }

    @Test
    public void testProcessPropertiesBooleanYes() throws ParseException {
        options.addOption(createOption("a", "option a"));
        Properties props = new Properties();
        props.setProperty("a", "yes");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesBooleanTrue() throws ParseException {
        options.addOption(createOption("a", "option a"));
        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesBoolean1() throws ParseException {
        options.addOption(createOption("a", "option a"));
        Properties props = new Properties();
        props.setProperty("a", "1");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesBooleanNo() throws ParseException {
        options.addOption(createOption("a", "option a"));
        options.addOption(createOption("b", "option b"));
        Properties props = new Properties();
        props.setProperty("a", "no");
        props.setProperty("b", "yes");
        CommandLine cmd = parser.parse(options, new String[0], props);
        // "a" with value "no" should break, so "b" is not processed
        assertFalse(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testProcessPropertiesOptionWithArg() throws ParseException {
        options.addOption(createOptionWithArg("a", "option a"));
        Properties props = new Properties();
        props.setProperty("a", "value");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessPropertiesOptionAlreadyPresent() throws ParseException {
        options.addOption(createOption("a", "option a"));
        Properties props = new Properties();
        props.setProperty("a", "yes");
        // First parse to add the option via command line
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
        // The property should be skipped because cmd already has option
        // No exception expected
    }

    @Test
    public void testProcessPropertiesOptionWithArgException() throws ParseException {
        ThrowingOption opt = new ThrowingOption("a", "option a");
        opt.setArgs(1); // make it accept an argument
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("a", "value");
        CommandLine cmd = parser.parse(options, new String[0], props);
        // Even though addValueForProcessing throws, the option is still added
        assertTrue(cmd.hasOption("a"));
        // But the value should not be set
        assertNull(cmd.getOptionValue("a"));
    }

    @Test
    public void testCheckRequiredOptionsNone() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
    }

    @Test
    public void testCheckRequiredOptionsSingle() throws ParseException {
        options.addOption(createOption("a", "option a"));
        options.addOption(createOption("b", "option b"));
        // Make "a" required
        Option opt = options.getOption("a");
        opt.setRequired(true);
        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().contains("Missing required option"));
            assertTrue(e.getMessage().contains("a"));
            // Should be singular
            assertFalse(e.getMessage().contains("options"));
        }
    }

    @Test
    public void testCheckRequiredOptionsMultiple() throws ParseException {
        options.addOption(createOption("a", "option a"));
        options.addOption(createOption("b", "option b"));
        options.getOption("a").setRequired(true);
        options.getOption("b").setRequired(true);
        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().contains("Missing required options"));
            assertTrue(e.getMessage().contains("a"));
            assertTrue(e.getMessage().contains("b"));
        }
    }

    @Test
    public void testProcessArgsNormal() throws ParseException {
        Option opt = createOptionWithArg("a", "option a");
        options.addOption(opt);
        String[] args = {"-a", "val1", "val2", "-b"};
        // Add another option so that processArgs stops at "-b"
        options.addOption(createOption("b", "option b"));
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        // Only the first value after -a is consumed because processArgs stops at next option
        assertEquals("val1", cmd.getOptionValue("a"));
        // The second value "val2" should be treated as an argument? Actually, after processArgs breaks, the iterator is at "val2"? Let's see: processArgs iterates, sees "val1", adds it, then next is "val2", which is not an option (since "-b" is next? Wait, args: -a, val1, val2, -b. After -a, processArgs is called. It iterates: first "val1" added, next "val2" is not an option (doesn't start with "-"? Actually "val2" doesn't start with "-", so it would be added as another value. But the option has only one argument? Option with hasArg=true and no specified number of args defaults to 1? Actually, Option with hasArg=true and no setArgs means it takes one argument. But addValueForProcessing may throw if more than allowed? Let's check: Option.addValueForProcessing calls addValue which checks if the number of values is less than the number of args. If it exceeds, it throws RuntimeException. So adding "val2" would throw, causing processArgs to break and call iter.previous(). So the final state: option gets "val1", then "val2" causes exception, iter.previous() puts iterator back to "val2", then break. Then after processArgs returns, the main loop continues with iterator.next() which will be "val2" again? Actually, after processArgs breaks, the main loop's iterator is still at the position after the last next() call inside processArgs. processArgs called iter.next() for "val2", then caught exception, called iter.previous() (so iterator moves back to before "val2"), then break. So the main loop's next call to iterator.next() will return "val2" again. Then "val2" is not an option, so it gets added as an argument. Then next is "-b", which is processed. So cmd should have option "a" with value "val1", and arg "val2", and option "b". We'll test that.
        assertEquals("val1", cmd.getOptionValue("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("val2", cmd.getArgs()[0]);
    }

    @Test
    public void testProcessArgsQuoted() throws ParseException {
        Option opt = createOptionWithArg("a", "option a");
        options.addOption(opt);
        String[] args = {"-a", "\"quoted\""};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("quoted", cmd.getOptionValue("a"));
    }

    @Test
    public void testProcessArgsOptionEncountered() throws ParseException {
        Option opt = createOptionWithArg("a", "option a");
        options.addOption(opt);
        options.addOption(createOption("b", "option b"));
        String[] args = {"-a", "-b"};
        CommandLine cmd = parser.parse(options, args);
        // processArgs sees "-b" which is an option, so it breaks without adding value
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testProcessArgsRuntimeException() throws ParseException {
        // Use ThrowingOption to simulate exception in addValueForProcessing
        ThrowingOption opt = new ThrowingOption("a", "option a");
        opt.setArgs(1); // accept one argument
        options.addOption(opt);
        String[] args = {"-a", "value"};
        CommandLine cmd = parser.parse(options, args);
        // The exception is caught, iter.previous() called, break
        // So option is added but without value
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testProcessArgsMissingArgumentException() throws ParseException {
        Option opt = createOptionWithArg("a", "option a");
        options.addOption(opt);
        String[] args = {"-a"};
        // No more tokens, so processArgs will throw MissingArgumentException
        parser.parse(options, args);
    }

    @Test
    public void testProcessOptionRequired() throws ParseException {
        Option opt = createOption("a", "option a");
        opt.setRequired(true);
        options.addOption(opt);
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        // The required option should have been removed from requiredOptions
        assertFalse(parser.getRequiredOptions().contains("a"));
    }

    @Test
    public void testProcessOptionGroup() throws ParseException {
        Option opt1 = createOption("a", "option a");
        Option opt2 = createOption("b", "option b");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testProcessOptionGroupRequired() throws ParseException {
        Option opt1 = createOption("a", "option a");
        Option opt2 = createOption("b", "option b");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);
        options.addOptionGroup(group);
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals(opt1, group.getSelected());
        // The group should be removed from requiredOptions (even if not present, no exception)
    }

    @Test
    public void testFlattenCalled() throws ParseException {
        String[] args = {"-a"};
        options.addOption(createOption("a", "option a"));
        parser.parse(options, args);
        assertArrayEquals(args, parser.getLastFlattenArgs());
        assertSame(options, parser.getLastFlattenOpts());
        assertFalse(parser.isLastFlattenStopAtNonOption());
    }

    @Test
    public void testFlattenCalledWithStopAtNonOption() throws ParseException {
        String[] args = {"-a"};
        options.addOption(createOption("a", "option a"));
        parser.parse(options, args, true);
        assertTrue(parser.isLastFlattenStopAtNonOption());
    }
}
