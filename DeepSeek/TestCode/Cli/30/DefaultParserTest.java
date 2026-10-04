package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Properties;

public class DefaultParserTest {

    @Test
    public void testParseWithNullArguments() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, (String[]) null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testStopAtNonOptionTrue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        String[] args = {"-x", "value"};
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.getArgList().contains("-x"));
        assertTrue(cmd.getArgList().contains("value"));
    }

    @Test
    public void testStopAtNonOptionFalseThrows() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        String[] args = {"-x"};
        try {
            parser.parse(options, args, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("-x", e.getOption());
        } catch (ParseException e) {
            fail("Expected UnrecognizedOptionException");
        }
    }

    @Test
    public void testSkipParsingAfterDoubleDash() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] args = {"--", "-a", "value"};
        CommandLine cmd = parser.parse(options, args);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgList().size());
        assertEquals("-a", cmd.getArgList().get(0));
        assertEquals("value", cmd.getArgList().get(1));
    }

    @Test
    public void testCurrentOptionAcceptsArgAndIsArgument() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        String[] args = {"-a", "value"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testCurrentOptionAcceptsArgAndNegativeNumber() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        String[] args = {"-a", "-3.14"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("-3.14", cmd.getOptionValue("a"));
    }

    @Test
    public void testLongOptionWithoutEqual() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("long", false, "desc");
        String[] args = {"--long"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("long"));
    }

    @Test
    public void testLongOptionWithEqual() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        Option opt = new Option("l", "long", true, "desc");
        options.addOption(opt);
        String[] args = {"--long=value"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("value", cmd.getOptionValue("long"));
    }

    @Test
    public void testAmbiguousLongOptionWithoutEqual() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("foobar", false, "desc");
        options.addOption("foobaz", false, "desc");
        String[] args = {"--foo"};
        try {
            parser.parse(options, args);
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertTrue(e.getMatchingOptions().contains("foobar"));
            assertTrue(e.getMatchingOptions().contains("foobaz"));
        } catch (ParseException e) {
            fail("Expected AmbiguousOptionException");
        }
    }

    @Test
    public void testAmbiguousLongOptionWithEqual() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("foo1", true, "desc");
        options.addOption("foo2", true, "desc");
        String[] args = {"--foo=value"};
        try {
            parser.parse(options, args);
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertTrue(e.getMatchingOptions().contains("foo1"));
            assertTrue(e.getMatchingOptions().contains("foo2"));
        } catch (ParseException e) {
            fail("Expected AmbiguousOptionException");
        }
    }

    @Test
    public void testOptionWithRequiredArgMissing() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        String[] args = {"-a"};
        try {
            parser.parse(options, args);
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertNotNull(e.getOption());
        } catch (ParseException e) {
            fail("Expected MissingArgumentException");
        }
    }

    @Test
    public void testCheckRequiredOptionsMissing() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addRequiredOption("a", "alpha", true, "desc");
        String[] args = {};
        try {
            parser.parse(options, args);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMissingOptions().contains("a"));
        } catch (ParseException e) {
            fail("Expected MissingOptionException");
        }
    }

    @Test
    public void testHandlePropertiesNoArgOptionTrueValues() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        Properties props = new Properties();
        props.setProperty("a", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testHandlePropertiesNoArgOptionFalseValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("b", false, "desc");
        Properties props = new Properties();
        props.setProperty("b", "false");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testHandlePropertiesWithArgOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("c", true, "desc");
        Properties props = new Properties();
        props.setProperty("c", "value");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertEquals("value", cmd.getOptionValue("c"));
    }

    @Test
    public void testHandlePropertiesOptionAlreadyPresent() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("d", true, "desc");
        Properties props = new Properties();
        props.setProperty("d", "propValue");
        String[] args = {"-d", "argValue"};
        CommandLine cmd = parser.parse(options, args, props);
        assertEquals("argValue", cmd.getOptionValue("d"));
    }

    @Test
    public void testIsNegativeNumberFalse() {
        DefaultParser parser = new DefaultParser();
        try {
            java.lang.reflect.Method method = DefaultParser.class.getDeclaredMethod("isNegativeNumber", String.class);
            method.setAccessible(true);
            assertFalse((Boolean) method.invoke(parser, "abc"));
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    public void testIsArgumentWithNegativeNumber() {
        DefaultParser parser = new DefaultParser();
        try {
            java.lang.reflect.Method isArg = DefaultParser.class.getDeclaredMethod("isArgument", String.class);
            isArg.setAccessible(true);
            assertTrue((Boolean) isArg.invoke(parser, "-1.2"));
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    public void testGetLongPrefix() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(null, "XX", false, "desc");
        parser.options = options;
        try {
            java.lang.reflect.Method method = DefaultParser.class.getDeclaredMethod("getLongPrefix", String.class);
            method.setAccessible(true);
            assertEquals("XX", method.invoke(parser, "-XXabc"));
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    public void testIsJavaPropertyTrue() {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder("D").numberOfArgs(2).build());
        parser.options = options;
        try {
            java.lang.reflect.Method method = DefaultParser.class.getDeclaredMethod("isJavaProperty", String.class);
            method.setAccessible(true);
            assertTrue((Boolean) method.invoke(parser, "Dkey"));
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    public void testHandleConcatenatedOptions() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        String[] args = {"-abvalue"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testHandleConcatenatedOptionsStopAtNonOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("x", false, "desc");
        String[] args = {"-x", "y"};
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.getArgList().contains("y"));
    }

    @Test
    public void testOptionGroupRequiredButOtherSelected() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("1", false, "desc"));
        group.addOption(new Option("2", false, "desc"));
        options.addOptionGroup(group);
        String[] args = {"-1"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("1"));
    }

    @Test
    public void testOptionGroupSelectedClearsRequiredList() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt = new Option("a", false, "desc");
        group.addOption(opt);
        options.addOptionGroup(group);
        String[] args = {"-a"};
        parser.parse(options, args);
    }

    @Test
    public void testShortOptionWithoutArgThenConcatenatedWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("s", false, "desc");
        options.addOption("v", true, "desc");
        String[] args = {"-sv", "value"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("s"));
        assertTrue(cmd.hasOption("v"));
        assertEquals("value", cmd.getOptionValue("v"));
    }

    @Test
    public void testJavaStyleOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        Option d = Option.builder("D").numberOfArgs(2).build();
        options.addOption(d);
        String[] args = {"-Dkey=value"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("key", cmd.getOptionValues("D")[0]);
        assertEquals("value", cmd.getOptionValues("D")[1]);
    }

    @Test
    public void testLongPrefixOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder().longOpt("X").hasArg().build());
        String[] args = {"-Xmx256m"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("mx256m", cmd.getOptionValue("X"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test
    public void testUnrecognizedTokenWithoutDash() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"token"});
        assertTrue(cmd.getArgList().contains("token"));
    }

    @Test
    public void testPropertyOptionWithEmptyValues() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        Option opt = new Option("p", true, "desc");
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("p", "");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertEquals("", cmd.getOptionValue("p"));
    }
}
