package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test suite for {@link PosixParser}.
 */
public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
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
    public void testFlattenDoubleHyphenOptionWithoutEquals() {
        options.addOption(new Option("f", "foo", false, "foo option"));
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenOptionWithEquals() {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenOnly() {
        String[] args = new String[]{"--", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenSingleHyphenWithOtherTokens() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "-", "file.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-", "file.txt"}, result);
    }

    @Test
    public void testFlattenValidSingleCharOption() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        String[] args = new String[]{"-a", "-b", "val"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "val"}, result);
    }

    @Test
    public void testFlattenInvalidSingleCharOptionNoStop() {
        // -x is not defined, stopAtNonOption = false
        // token is ignored when length == 2 and not in options
        String[] args = new String[]{"-x", "extra"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"extra"}, result);
    }

    @Test
    public void testFlattenInvalidSingleCharOptionStop() {
        // -x is not defined, stopAtNonOption = true
        // eatTheRest should become true and subsequent tokens gobbled
        String[] args = new String[]{"-x", "foo", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"foo", "bar"}, result);
    }

    @Test
    public void testFlattenLongOptionWithSingleHyphen() {
        // Multi-char option configured with single hyphen like -help
        options.addOption(new Option("help", "print help"));
        String[] args = new String[]{"-help"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-help"}, result);
    }

    @Test
    public void testFlattenBurstingNoArgs() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testFlattenBurstingWithArgAttached() {
        options.addOption("a", false, "option a");
        options.addOption("f", true, "option f with arg");

        String[] args = new String[]{"-afbar.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-f", "bar.txt"}, result);
    }

    @Test
    public void testFlattenBurstingWithArgAttachedAtEnd() {
        options.addOption("a", false, "option a");
        options.addOption("f", true, "option f with arg");

        // When option with arg is at the end of the burst string, no remaining characters
        String[] args = new String[]{"-af", "bar.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-f", "bar.txt"}, result);
    }

    @Test
    public void testFlattenBurstingUnknownCharNoStop() {
        options.addOption("a", false, "option a");

        // -az where 'z' is unknown and stopAtNonOption = false
        // 'a' is matched, then 'z' is not recognized, adds the full token
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-az"}, result);
    }

    @Test
    public void testFlattenBurstingUnknownCharWithStop() {
        options.addOption("a", false, "option a");

        // -az where 'z' is unknown and stopAtNonOption = true
        // 'a' is matched, then 'z' triggers process("z")
        // Since 'a' doesn't have an arg, process("z") adds "--", "z", and eats the rest
        String[] args = new String[]{"-az", "remain1", "remain2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "remain1", "remain2"}, result);
    }

    @Test
    public void testFlattenBurstingUnknownCharWithArgOptionAndStop() {
        options.addOption("a", true, "option a has arg");

        // -az where 'a' has an arg and stopAtNonOption = true
        // -a matches and hasArg() is true, remaining "z" is consumed as argument for 'a'
        String[] args = new String[]{"-az", "other"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "z", "--", "other"}, result);
    }

    @Test
    public void testFlattenStopAtNonOptionWithCurrentOptionHavingArg() {
        options.addOption("b", true, "option b with arg");

        // -b has arg, so next non-option token "val" satisfies currentOption
        // subsequent non-option token "extra" triggers "--", "extra" and eats the rest
        String[] args = new String[]{"-b", "val", "extra", "more"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "val", "--", "extra", "more"}, result);
    }

    @Test
    public void testFlattenStopAtNonOptionWithoutCurrentOption() {
        options.addOption("a", false, "option a without arg");

        // -a has no arg, non-option token "nonOpt" triggers "--", "nonOpt" and eats the rest
        String[] args = new String[]{"-a", "nonOpt", "remain"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "nonOpt", "remain"}, result);
    }

    @Test
    public void testFlattenNoStopAtNonOption() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"nonOpt1", "-a", "nonOpt2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOpt1", "-a", "nonOpt2"}, result);
    }

    @Test
    public void testReusabilityOfParser() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");

        String[] args1 = new String[]{"-a", "foo", "bar"};
        String[] result1 = parser.flatten(options, args1, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "bar"}, result1);

        // Run parser again to ensure state is cleanly reset by init()
        String[] args2 = new String[]{"-b", "val"};
        String[] result2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[]{"-b", "val"}, result2);
    }

    @Test
    public void testParseFullCommandLine() throws ParseException {
        options.addOption("v", "verbose", false, "verbose mode");
        options.addOption("f", "file", true, "input file");

        String[] args = new String[]{"-v", "--file=test.txt", "arg1", "arg2"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    @Test
    public void testParseStopAtNonOption() throws ParseException {
        options.addOption("v", false, "verbose");
        options.addOption("f", true, "file");

        String[] args = new String[]{"-v", "nonOption", "-f", "file.txt"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("v"));
        assertFalse(cl.hasOption("f"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-f", cl.getArgs()[1]);
        assertEquals("file.txt", cl.getArgs()[2]);
    }

    @Test
    public void testBurstTokenMultipleOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        options.addOption("d", true, "option d with arg");

        String[] args = new String[]{"-abcde_value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c", "-d", "e_value"}, result);
    }

    @Test
    public void testBurstTokenSingleOptionWithArg() {
        options.addOption("k", true, "key");

        String[] args = new String[]{"-kValue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-k", "Value"}, result);
    }
}