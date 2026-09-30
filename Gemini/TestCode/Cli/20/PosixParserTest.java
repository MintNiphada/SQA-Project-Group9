package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testFlattenLongOptionWithoutEquals() {
        options.addOption("f", "foo", false, "foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlattenLongOptionWithEquals() {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlattenLongOptionWithEqualsEmptyValue() {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[]{"--foo="};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", ""}, result);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);

        String[] resultStop = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-"}, resultStop);
    }

    @Test
    public void testFlattenTwoCharOptionExists() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionExistsWithArg() {
        options.addOption("a", true, "option a with arg");
        String[] args = new String[]{"-a", "value", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "value", "--", "extra"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionDoesNotExistStopAtNonOption() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionDoesNotExistNoStopAtNonOption() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenMultiCharOptionExistsDirectly() {
        options.addOption("foo", false, "multi-char option");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-foo"}, result);
    }

    @Test
    public void testBurstTokenMultipleSingleOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testBurstTokenWithArgAttached() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    @Test
    public void testBurstTokenWithArgAtEnd() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-ab", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    @Test
    public void testBurstTokenUnrecognizedCharStopAtNonOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "rest1", "rest2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "rest1", "rest2"}, result);
    }

    @Test
    public void testBurstTokenUnrecognizedCharNoStopAtNonOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "rest1"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-az", "rest1"}, result);
    }

    @Test
    public void testBurstTokenUnrecognizedFirstCharStopAtNonOption() {
        String[] args = new String[]{"-zabc", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "zabc", "rest"}, result);
    }

    @Test
    public void testFlattenNonOptionWithoutCurrentOptionStopAtNonOption() {
        String[] args = new String[]{"nonOption", "another"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "nonOption", "another"}, result);
    }

    @Test
    public void testFlattenNonOptionNoStopAtNonOption() {
        String[] args = new String[]{"nonOption", "another"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption", "another"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenStopAtNonOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "--", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenMultipleConsecutiveCallsResetsState() {
        options.addOption("a", true, "option a");
        String[] args1 = new String[]{"-a", "val1", "nonOption"};
        String[] result1 = parser.flatten(options, args1, true);
        assertArrayEquals(new String[]{"-a", "val1", "--", "nonOption"}, result1);

        String[] args2 = new String[]{"-a", "val2"};
        String[] result2 = parser.flatten(options, args2, true);
        assertArrayEquals(new String[]{"-a", "val2"}, result2);
    }

    @Test
    public void testParseFullCommandLine() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption("b", "beta", true, "option b");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-ab", "valueB", "-c", "extraArg"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("valueB", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extraArg", cl.getArgs()[0]);
    }

    @Test
    public void testParseWithStopAtNonOption() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        String[] args = new String[]{"-a", "stopHere", "-b"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("stopHere", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    @Test
    public void testParseLongOptionWithEquals() throws ParseException {
        options.addOption(OptionBuilder.withLongOpt("file").hasArg().create('f'));

        String[] args = new String[]{"--file=test.txt"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("file"));
        assertEquals("test.txt", cl.getOptionValue("file"));
    }

    @Test
    public void testBurstTokenSingleHyphenOption() {
        options.addOption("k", false, "option k");
        String[] args = new String[]{"-k"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-k"}, result);
    }

    @Test
    public void testGobbleWhenEatTheRestIsNotSet() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-a"}, result);
    }
}