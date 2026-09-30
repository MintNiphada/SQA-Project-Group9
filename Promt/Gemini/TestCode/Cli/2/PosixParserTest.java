package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testEmptyArguments() throws Exception {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);

        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSingleDash() throws Exception {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);

        CommandLine cl = parser.parse(options, args);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testDoubleDashOnly() throws Exception {
        String[] args = new String[]{"--"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--"}, result);

        CommandLine cl = parser.parse(options, args);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testDoubleDashWithEquals() throws Exception {
        options.addOption(new Option("f", "foo", true, "Foo option"));
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    @Test
    public void testDoubleDashWithoutEquals() throws Exception {
        options.addOption(new Option("f", "foo", true, "Foo option"));
        String[] args = new String[]{"--foo", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    @Test
    public void testSingleOptionRecognized() throws Exception {
        options.addOption("a", false, "Option A");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testSingleOptionUnrecognizedStopAtNonOptionFalse() {
        String[] args = new String[]{"-u"};
        String[] result = parser.flatten(options, args, false);
        // Unrecognized 2-char token when stopAtNonOption is false is skipped in flatten
        assertArrayEquals(new String[]{}, result);
    }

    @Test
    public void testSingleOptionUnrecognizedStopAtNonOptionTrue() {
        String[] args = new String[]{"-u", "remaining1", "remaining2"};
        String[] result = parser.flatten(options, args, true);
        // When stopAtNonOption is true, -u triggers eatTheRest and remaining tokens are gobbled
        assertArrayEquals(new String[]{"remaining1", "remaining2"}, result);
    }

    @Test
    public void testOptionLongWithSingleDash() throws Exception {
        options.addOption("foo", false, "Option foo with single dash");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-foo"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("foo"));
    }

    @Test
    public void testBurstingMultipleSimpleOptions() throws Exception {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");

        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testBurstingWithOptionArgument() throws Exception {
        options.addOption("a", false, "Option a");
        options.addOption("f", true, "Option f with arg");

        String[] args = new String[]{"-afbar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-f", "bar"}, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("f"));
        assertEquals("bar", cl.getOptionValue("f"));
    }

    @Test
    public void testBurstingUnrecognizedStopAtNonOptionFalse() {
        options.addOption("a", false, "Option a");

        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-z"}, result);
    }

    @Test
    public void testBurstingUnrecognizedStopAtNonOptionTrue() {
        options.addOption("a", false, "Option a");

        String[] args = new String[]{"-az", "rest1", "rest2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "rest1", "rest2"}, result);
    }

    @Test
    public void testBurstingUnrecognizedStopAtNonOptionTrueWithOptionArg() {
        Option optA = new Option("a", true, "Option a with arg");
        options.addOption(optA);

        // When 'a' has an argument, bursting stops when a has arg and token length != i + 1
        String[] args = new String[]{"-az", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "z", "--", "extra"}, result);
    }

    @Test
    public void testNonOptionArgumentStopAtNonOptionFalse() throws Exception {
        options.addOption("a", false, "Option a");

        String[] args = new String[]{"arg1", "-a", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"arg1", "-a", "arg2"}, result);

        CommandLine cl = parser.parse(options, args, false);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    @Test
    public void testNonOptionArgumentStopAtNonOptionTrue() throws Exception {
        options.addOption("a", false, "Option a");

        String[] args = new String[]{"arg1", "-a", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "arg1", "-a", "arg2"}, result);

        CommandLine cl = parser.parse(options, args, true);
        assertFalse(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("arg2", cl.getArgs()[2]);
    }

    @Test
    public void testOptionWithArgFollowedBySeparateValueStopAtNonOptionTrue() throws Exception {
        options.addOption("f", true, "Option f with arg");

        String[] args = new String[]{"-f", "myvalue", "nonOptArg", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "myvalue", "--", "nonOptArg", "extra"}, result);

        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("f"));
        assertEquals("myvalue", cl.getOptionValue("f"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOptArg", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test
    public void testMultipleFlattensWithSameInstance() {
        options.addOption("a", false, "Option a");
        options.addOption("b", true, "Option b");

        String[] args1 = new String[]{"-a", "-b", "val1"};
        String[] result1 = parser.flatten(options, args1, false);
        assertArrayEquals(new String[]{"-a", "-b", "val1"}, result1);

        String[] args2 = new String[]{"-b", "val2", "extra"};
        String[] result2 = parser.flatten(options, args2, true);
        assertArrayEquals(new String[]{"-b", "val2", "--", "extra"}, result2);
    }

    @Test
    public void testComplexCommandLine() throws Exception {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", true, "Option c");
        options.addOption(new Option("d", "debug", false, "Debug"));
        options.addOption(new Option("f", "file", true, "File"));

        String[] args = new String[]{
                "-ab",
                "-cfoo",
                "--debug",
                "--file=output.txt",
                "-",
                "--",
                "remainingArg1",
                "remainingArg2"
        };

        String[] result = parser.flatten(options, args, false);
        String[] expected = new String[]{
                "-a", "-b",
                "-c", "foo",
                "--debug",
                "--file", "output.txt",
                "-",
                "--",
                "remainingArg1",
                "remainingArg2"
        };
        assertArrayEquals(expected, result);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals("foo", cl.getOptionValue("c"));
        assertTrue(cl.hasOption("debug"));
        assertTrue(cl.hasOption("file"));
        assertEquals("output.txt", cl.getOptionValue("file"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("remainingArg1", cl.getArgs()[1]);
        assertEquals("remainingArg2", cl.getArgs()[2]);
    }

    @Test
    public void testBurstSingleCharOptionAtEnd() {
        options.addOption("a", false, "Option a");
        options.addOption("b", true, "Option b");

        // When last char has arg, but token has no additional chars
        String[] args = new String[]{"-ab"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testBurstMultipleUnknownOptionsStopAtNonOptionFalse() {
        String[] args = new String[]{"-xyz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-x", "-y", "-z"}, result);
    }
}