package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    @Test
    public void testSimpleShortOptionWithoutArgument() throws ParseException {
        options.addOption("a", false, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSimpleShortOptionWithSeparateArgument() throws ParseException {
        options.addOption("a", true, "Option a with arg");
        CommandLine cl = parser.parse(options, new String[]{"-a", "foo"});
        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSimpleLongOptionWithSeparateArgument() throws ParseException {
        options.addOption(new Option("a", "alpha", true, "Option alpha"));
        CommandLine cl = parser.parse(options, new String[]{"--alpha", "bar"});
        assertTrue(cl.hasOption("alpha"));
        assertTrue(cl.hasOption("a"));
        assertEquals("bar", cl.getOptionValue("alpha"));
        assertEquals("bar", cl.getOptionValue("a"));
    }

    @Test
    public void testLongOptionWithEqualSign() throws ParseException {
        options.addOption(new Option("a", "alpha", true, "Option alpha"));
        CommandLine cl = parser.parse(options, new String[]{"--alpha=bar"});
        assertTrue(cl.hasOption("alpha"));
        assertEquals("bar", cl.getOptionValue("alpha"));
    }

    @Test
    public void testShortOptionWithEqualSign() throws ParseException {
        options.addOption("a", true, "Option a with arg");
        CommandLine cl = parser.parse(options, new String[]{"-a=foo"});
        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
    }

    @Test
    public void testShortOptionWithEqualSignNoArgExpected() {
        options.addOption("a", false, "Option a without arg");
        try {
            parser.parse(options, new String[]{"-a=foo"});
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }

    @Test
    public void testLongOptionWithEqualSignNoArgExpected() {
        options.addOption(new Option("a", "alpha", false, "Option alpha without arg"));
        try {
            parser.parse(options, new String[]{"--alpha=foo"});
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }

    @Test
    public void testSingleDashAsArgument() throws ParseException {
        options.addOption("a", false, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-"});
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testDoubleDashStopsParsing() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "extra"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test
    public void testNegativeNumberAsArgument() throws ParseException {
        options.addOption("n", true, "Number option");
        CommandLine cl = parser.parse(options, new String[]{"-n", "-42.5"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    @Test
    public void testNegativeNumberAsNonOption() throws ParseException {
        options.addOption("a", false, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-42"});
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-42", cl.getArgs()[0]);
    }

    @Test
    public void testConcatenatedShortOptions() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");
        CommandLine cl = parser.parse(options, new String[]{"-abc"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testConcatenatedShortOptionsWithAttachedArgument() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", true, "Option b");
        CommandLine cl = parser.parse(options, new String[]{"-abfoo"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
    }

    @Test
    public void testConcatenatedShortOptionsWithTrailingUnknownStopAtNonOption() throws ParseException {
        options.addOption("a", false, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-az", "extra"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("z", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test
    public void testConcatenatedShortOptionsWithTrailingUnknownThrows() {
        options.addOption("a", false, "Option a");
        try {
            parser.parse(options, new String[]{"-az"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }

    @Test
    public void testPartialLongOptionMatching() throws ParseException {
        options.addOption(new Option("v", "verbose", false, "Verbose output"));
        CommandLine cl = parser.parse(options, new String[]{"--verb"});
        assertTrue(cl.hasOption("verbose"));
    }

    @Test
    public void testAmbiguousLongOption() {
        options.addOption(new Option("v", "verbose", false, "Verbose output"));
        options.addOption(new Option("V", "version", false, "Version info"));
        try {
            parser.parse(options, new String[]{"--ver"});
            fail("Expected AmbiguousOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof AmbiguousOptionException);
            AmbiguousOptionException aoe = (AmbiguousOptionException) e;
            assertEquals("--ver", aoe.getOption());
            assertEquals(2, aoe.getMatchingOptions().size());
        }
    }

    @Test
    public void testAmbiguousLongOptionWithEqual() {
        options.addOption(new Option("v", "verbose", true, "Verbose output"));
        options.addOption(new Option("V", "version", true, "Version info"));
        try {
            parser.parse(options, new String[]{"--ver=value"});
            fail("Expected AmbiguousOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof AmbiguousOptionException);
        }
    }

    @Test
    public void testLongOptionWithSingleDash() throws ParseException {
        options.addOption(new Option("opt", "option", false, "Long option"));
        CommandLine cl = parser.parse(options, new String[]{"-option"});
        assertTrue(cl.hasOption("option"));
    }

    @Test
    public void testLongOptionWithSingleDashAndEqual() throws ParseException {
        options.addOption(new Option("opt", "option", true, "Long option with arg"));
        CommandLine cl = parser.parse(options, new String[]{"-option=val"});
        assertTrue(cl.hasOption("option"));
        assertEquals("val", cl.getOptionValue("option"));
    }

    @Test
    public void testLongPrefixOptionLikeJavaPropertyXmx() throws ParseException {
        Option xmx = new Option("Xmx", true, "Max heap size");
        options.addOption(xmx);
        CommandLine cl = parser.parse(options, new String[]{"-Xmx1024m"});
        assertTrue(cl.hasOption("Xmx"));
        assertEquals("1024m", cl.getOptionValue("Xmx"));
    }

    @Test
    public void testJavaPropertyOptionTwoArgs() throws ParseException {
        Option property = new Option("D", true, "Java property");
        property.setArgs(2);
        property.setValueSeparator('=');
        options.addOption(property);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    @Test
    public void testJavaPropertyOptionUnlimitedArgsAttached() throws ParseException {
        Option property = new Option("D", true, "Java property");
        property.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(property);

        CommandLine cl = parser.parse(options, new String[]{"-Dflag"});
        assertTrue(cl.hasOption("D"));
        assertEquals("flag", cl.getOptionValue("D"));
    }

    @Test
    public void testJavaPropertyOptionWithEqualNoMatchingLong() throws ParseException {
        Option property = new Option("D", true, "Java property");
        property.setArgs(2);
        options.addOption(property);

        CommandLine cl = parser.parse(options, new String[]{"-Dfoo=bar"});
        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("foo", values[0]);
        assertEquals("bar", values[1]);
    }

    @Test
    public void testMissingRequiredOption() {
        Option req = new Option("r", "required", false, "Required option");
        req.setRequired(true);
        options.addOption(req);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof MissingOptionException);
            MissingOptionException moe = (MissingOptionException) e;
            assertTrue(moe.getMissingOptions().contains("r"));
        }
    }

    @Test
    public void testMissingRequiredOptionsSatisfied() throws ParseException {
        Option req = new Option("r", "required", false, "Required option");
        req.setRequired(true);
        options.addOption(req);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    @Test
    public void testOptionGroupRequired() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "opt-a"));
        group.addOption(new Option("b", "opt-b"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals("a", group.getSelected());
    }

    @Test
    public void testOptionGroupRequiredMissing() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "opt-a"));
        group.addOption(new Option("b", "opt-b"));
        options.addOptionGroup(group);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException for OptionGroup");
        } catch (ParseException e) {
            assertTrue(e instanceof MissingOptionException);
        }
    }

    @Test
    public void testOptionGroupAlreadySelectedException() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "opt-a"));
        group.addOption(new Option("b", "opt-b"));
        options.addOptionGroup(group);

        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected AlreadySelectedException");
        } catch (ParseException e) {
            assertTrue(e instanceof AlreadySelectedException);
            AlreadySelectedException ase = (AlreadySelectedException) e;
            assertEquals(group, ase.getOptionGroup());
            assertEquals("b", ase.getOption().getOpt());
        }
    }

    @Test
    public void testMissingArgumentException() {
        options.addOption("a", true, "Option a");
        try {
            parser.parse(options, new String[]{"-a"});
            fail("Expected MissingArgumentException");
        } catch (ParseException e) {
            assertTrue(e instanceof MissingArgumentException);
            MissingArgumentException mae = (MissingArgumentException) e;
            assertEquals("a", mae.getOption().getOpt());
        }
    }

    @Test
    public void testMissingArgumentExceptionWithSubsequentOption() {
        options.addOption("a", true, "Option a");
        options.addOption("b", false, "Option b");
        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected MissingArgumentException");
        } catch (ParseException e) {
            assertTrue(e instanceof MissingArgumentException);
            MissingArgumentException mae = (MissingArgumentException) e;
            assertEquals("a", mae.getOption().getOpt());
        }
    }

    @Test
    public void testStopAtNonOption() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption1", "-b", "nonOption2"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("nonOption1", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
        assertEquals("nonOption2", cl.getArgs()[2]);
    }

    @Test
    public void testUnrecognizedOptionThrowsWhenNotStopAtNonOption() {
        options.addOption("a", false, "Option a");
        try {
            parser.parse(options, new String[]{"-a", "-z"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
            assertEquals("-z", ((UnrecognizedOptionException) e).getOption());
        }
    }

    @Test
    public void testUnrecognizedLongOptionThrows() {
        try {
            parser.parse(options, new String[]{"--unknown"});
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }

    @Test
    public void testUnrecognizedLongOptionWithEqualThrows() {
        try {
            parser.parse(options, new String[]{"--unknown=val"});
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }

    @Test
    public void testQuotesStrippedInOptionValue() throws ParseException {
        options.addOption("a", true, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "\"quoted value\""});
        assertTrue(cl.hasOption("a"));
        assertEquals("quoted value", cl.getOptionValue("a"));
    }

    @Test
    public void testSingleQuotesStrippedInOptionValue() throws ParseException {
        options.addOption("a", true, "Option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "'single quoted'"});
        assertTrue(cl.hasOption("a"));
        assertEquals("single quoted", cl.getOptionValue("a"));
    }

    @Test
    public void testNullArgumentsArray() throws ParseException {
        options.addOption("a", false, "Option a");
        CommandLine cl = parser.parse(options, (String[]) null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testPropertiesWithArgOption() throws ParseException {
        options.addOption("a", true, "Option a");
        Properties props = new Properties();
        props.setProperty("a", "propertyValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertEquals("propertyValue", cl.getOptionValue("a"));
    }

    @Test
    public void testPropertiesWithBooleanOptionTrueValues() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "yes");
        props.setProperty("c", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testPropertiesWithBooleanOptionFalseValues() throws ParseException {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");

        Properties props = new Properties();
        props.setProperty("a", "false");
        props.setProperty("b", "no");
        props.setProperty("c", "0");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertFalse(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertFalse(cl.hasOption("c"));
    }

    @Test
    public void testPropertiesIgnoredIfAlreadyOnCommandLine() throws ParseException {
        options.addOption("a", true, "Option a");

        Properties props = new Properties();
        props.setProperty("a", "propVal");

        CommandLine cl = parser.parse(options, new String[]{"-a", "cliVal"}, props);
        assertTrue(cl.hasOption("a"));
        assertEquals("cliVal", cl.getOptionValue("a"));
    }

    @Test
    public void testParseOverloads() throws ParseException {
        options.addOption("a", false, "Option a");

        CommandLine cl1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cl1.hasOption("a"));

        CommandLine cl2 = parser.parse(options, new String[]{"-a"}, true);
        assertTrue(cl2.hasOption("a"));

        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cl3 = parser.parse(options, new String[]{}, props);
        assertTrue(cl3.hasOption("a"));
    }

    @Test
    public void testMultipleValuesOption() throws ParseException {
        Option m = new Option("m", "multiple", true, "Multiple values");
        m.setArgs(3);
        options.addOption(m);

        CommandLine cl = parser.parse(options, new String[]{"-m", "v1", "v2", "v3", "extraArg"});
        assertTrue(cl.hasOption("m"));
        String[] vals = cl.getOptionValues("m");
        assertEquals(3, vals.length);
        assertEquals("v1", vals[0]);
        assertEquals("v2", vals[1]);
        assertEquals("v3", vals[2]);
        assertEquals(1, cl.getArgs().length);
        assertEquals("extraArg", cl.getArgs()[0]);
    }

    @Test
    public void testUnknownSingleDashTokenIgnoredWhenStopAtNonOption() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"foo", "bar"}, true);
        assertEquals(2, cl.getArgs().length);
        assertEquals("foo", cl.getArgs()[0]);
        assertEquals("bar", cl.getArgs()[1]);
    }

    @Test
    public void testShortOptionSingleCharUnknownOption() {
        try {
            parser.parse(options, new String[]{"-u"});
            fail("Expected UnrecognizedOptionException");
        } catch (ParseException e) {
            assertTrue(e instanceof UnrecognizedOptionException);
        }
    }
}