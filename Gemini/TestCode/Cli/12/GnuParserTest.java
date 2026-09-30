package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the GnuParser.
 */
public class GnuParserTest {

    private GnuParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new GnuParser();
        options = new Options();
    }

    @Test
    public void testFlattenEmptyArgs() {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenDoubleHyphen() {
        String[] args = new String[]{"--", "arg1", "-a", "--opt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "arg1", "-a", "--opt"}, result);
    }

    @Test
    public void testFlattenKnownShortOption() {
        options.addOption("a", "alpha", false, "alpha option");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenKnownLongOption() {
        options.addOption("a", "alpha", false, "alpha option");
        String[] args = new String[]{"--alpha"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha"}, result);
    }

    @Test
    public void testFlattenSpecialPropertyOption() {
        options.addOption("-D", true, "property option");
        String[] args = new String[]{"-Dkey=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-D", "key=value"}, result);
    }

    @Test
    public void testFlattenUnknownOptionNoStop() {
        String[] args = new String[]{"-z", "val", "-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "val", "-foo"}, result);
    }

    @Test
    public void testFlattenUnknownOptionStopAtNonOption() {
        String[] args = new String[]{"-z", "val1", "val2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "val1", "val2"}, result);
    }

    @Test
    public void testFlattenNonOptionArguments() {
        String[] args = new String[]{"arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenMixedArguments() {
        options.addOption("a", false, "toggle a");
        options.addOption("b", "beta", true, "beta option");
        options.addOption("-D", true, "define property");

        String[] args = new String[]{"-a", "-Dparam=foo", "--beta", "val", "extra1", "--", "-notAnOpt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-D", "param=foo", "--beta", "val", "extra1", "--", "-notAnOpt"}, result);
    }

    @Test
    public void testFlattenWithStopAtNonOptionWhenNonOptionEncountered() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "nonOption1", "-b", "nonOption2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "nonOption1", "-b", "nonOption2"}, result);
    }

    @Test
    public void testFlattenWithStopAtNonOptionUnknownOptionFirst() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-unknown", "foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-unknown", "foo", "-a"}, result);
    }

    @Test
    public void testParseSimple() throws ParseException {
        options.addOption("a", false, "toggle a");
        options.addOption("b", true, "set b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "value", "extra"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    @Test
    public void testParseDoubleHyphenTerminatesOptions() throws ParseException {
        options.addOption("a", false, "toggle a");
        options.addOption("b", false, "toggle b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
    }

    @Test
    public void testParseStopAtNonOption() throws ParseException {
        options.addOption("a", false, "toggle a");
        options.addOption("b", false, "toggle b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "non-option", "-b"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("non-option", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnrecognizedOptionThrowsException() throws ParseException {
        options.addOption("a", false, "toggle a");
        parser.parse(options, new String[]{"-unknown"});
    }

    @Test(expected = MissingOptionException.class)
    public void testParseMissingRequiredOption() throws ParseException {
        Option opt = new Option("r", "required", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{"-other"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testParseMissingArgument() throws ParseException {
        options.addOption("b", true, "needs arg");
        parser.parse(options, new String[]{"-b"});
    }

    @Test
    public void testFlattenLongOptionWithEqualsSignOptionNotDirectlyRecognized() {
        options.addOption("--", false, "double hyphen option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "foo=bar"}, result);
    }
}