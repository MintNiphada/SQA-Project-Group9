package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class ParserTest {

    // Concrete subclass of Parser for testing
    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            // Simply return a copy of the arguments to avoid modification
            return arguments.clone();
        }
    }

    // Option subclass that throws RuntimeException in addValue
    private static class ThrowingOption extends Option {
        public ThrowingOption(String opt, boolean hasArg, String description) {
            super(opt, hasArg, description);
        }

        @Override
        public void addValue(String value) {
            throw new RuntimeException("addValue exception");
        }
    }

    private Parser createParser() {
        return new TestParser();
    }

    private Options createOptionsWithNoArgOption(String opt) {
        Options options = new Options();
        options.addOption(new Option(opt, false, "no arg option"));
        return options;
    }

    private Options createOptionsWithRequiredArgOption(String opt) {
        Options options = new Options();
        options.addOption(new Option(opt, true, "required arg option"));
        return options;
    }

    private Options createOptionsWithOptionalArgOption(String opt) {
        Options options = new Options();
        Option option = new Option(opt, false, "optional arg option");
        option.setOptionalArg(true);
        options.addOption(option);
        return options;
    }

    private Options createOptionsWithRequiredOption(String opt) {
        Options options = new Options();
        Option option = new Option(opt, false, "required option");
        option.setRequired(true);
        options.addOption(option);
        return options;
    }

    private Options createOptionsWithOptionGroup(boolean groupRequired, String... opts) {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        for (String opt : opts) {
            Option option = new Option(opt, false, "group option " + opt);
            group.addOption(option);
        }
        group.setRequired(groupRequired);
        options.addOptionGroup(group);
        return options;
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{});
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseSimpleOption() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseOptionWithRequiredArg() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(expected = MissingArgumentException.class)
    public void testParseOptionWithMissingArg() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        parser.parse(options, new String[]{"-b"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnknownOption() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        parser.parse(options, new String[]{"-x"});
    }

    @Test
    public void testParseStopAtNonOptionWithNonOption() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        options.addOption(new Option("b", false, "another"));
        CommandLine cmd = parser.parse(options, new String[]{"-a", "foo", "-b"}, true);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertArrayEquals(new String[]{"foo", "-b"}, cmd.getArgs());
    }

    @Test
    public void testParseStopAtNonOptionWithSingleDash() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, true);
        assertFalse(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"foo"}, cmd.getArgs());
    }

    @Test
    public void testParseStopAtNonOptionWithDoubleDash() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"--", "foo", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"foo", "-a"}, cmd.getArgs());
    }

    @Test
    public void testParseNonStopAtNonOptionWithDoubleDash() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"--", "foo", "-a"}, false);
        assertFalse(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"foo", "-a"}, cmd.getArgs());
    }

    @Test
    public void testParseNonStopAtNonOptionWithSingleDash() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, false);
        assertFalse(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"-", "foo"}, cmd.getArgs());
    }

    @Test
    public void testParseOptionWithQuotedValue() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "\"value with spaces\""});
        assertEquals("value with spaces", cmd.getOptionValue("b"));
    }

    @Test
    public void testParseOptionWithMultipleValues() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "v1", "v2", "-a"});
        // Add an option that doesn't expect arg to stop values
        options.addOption(new Option("a", false, "stop"));
        // Re-parse with added option
        parser = createParser();
        cmd = parser.parse(options, new String[]{"-b", "v1", "v2", "-a"});
        assertTrue(cmd.hasOption("b"));
        String[] values = cmd.getOptionValues("b");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
        assertTrue(cmd.hasOption("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testParseRequiredOptionMissing() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredOption("r");
        parser.parse(options, new String[]{});
    }

    @Test
    public void testParseRequiredOptionProvided() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredOption("r");
        CommandLine cmd = parser.parse(options, new String[]{"-r"});
        assertTrue(cmd.hasOption("r"));
    }

    @Test
    public void testParseOptionGroupRequired() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithOptionGroup(true, "a", "b");
        // Need to add group options to Options? createOptionsWithOptionGroup already does.
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testParseOptionGroupNotRequired() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithOptionGroup(false, "a", "b");
        CommandLine cmd = parser.parse(options, new String[]{"-b"});
        assertTrue(cmd.hasOption("b"));
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertiesNull() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{}, null);
        assertNotNull(cmd);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertiesOptionWithArg() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        Properties props = new Properties();
        props.setProperty("b", "propValue");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("b"));
        assertEquals("propValue", cmd.getOptionValue("b"));
    }

    @Test
    public void testParsePropertiesOptionWithNoArgYes() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        Properties props = new Properties();
        props.setProperty("a", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertiesOptionWithNoArgNo() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        options.addOption(new Option("b", false, "another"));
        Properties props = new Properties();
        props.setProperty("a", "no");
        props.setProperty("b", "yes"); // should not be processed due to break
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b")); // break prevented processing b
    }

    @Test
    public void testParsePropertiesOptionWithNoArgTrue() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        Properties props = new Properties();
        props.setProperty("a", "TrUe");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertiesOptionWithNoArgOne() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        Properties props = new Properties();
        props.setProperty("a", "1");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertiesOptionWithArgAlreadyPresent() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "cmdValue"});
        Properties props = new Properties();
        props.setProperty("b", "propValue");
        // Already present, property should be ignored
        cmd = parser.parse(options, new String[]{"-b", "cmdValue"}, props);
        assertEquals("cmdValue", cmd.getOptionValue("b"));
    }

    @Test(expected = NullPointerException.class)
    public void testParsePropertiesUndefinedOption() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        Properties props = new Properties();
        props.setProperty("undefined", "value");
        parser.parse(options, new String[]{}, props);
    }

    @Test
    public void testParsePropertiesOptionWithArgRuntimeException() throws ParseException {
        Parser parser = createParser();
        Options options = new Options();
        options.addOption(new ThrowingOption("t", true, "throwing"));
        Properties props = new Properties();
        props.setProperty("t", "value");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        // Option should be added (without value) after catching RuntimeException
        assertTrue(cmd.hasOption("t"));
        assertNull(cmd.getOptionValue("t"));
    }

    @Test
    public void testProcessArgsDirectly() throws Exception {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        options.addOption(new Option("a", false, "another"));

        // Set private field 'options' via reflection
        java.lang.reflect.Field optionsField = Parser.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        optionsField.set(parser, options);

        Option opt = new Option("b", true, "arg");
        List<String> tokens = new ArrayList<>(Arrays.asList("value1", "value2", "-a"));
        ListIterator<String> iter = tokens.listIterator();
        parser.processArgs(opt, iter);

        String[] values = opt.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("value1", values[0]);
        assertEquals("value2", values[1]);
    }

    @Test
    public void testParseOptionArgStartingWithDashNotOption() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithRequiredArgOption("b");
        // -x is not defined, so it should be treated as value for -b
        CommandLine cmd = parser.parse(options, new String[]{"-b", "-x"});
        assertEquals("-x", cmd.getOptionValue("b"));
    }

    @Test
    public void testParseStopAtNonOptionValidOptionProcessed() throws ParseException {
        Parser parser = createParser();
        Options options = createOptionsWithNoArgOption("a");
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }
}
