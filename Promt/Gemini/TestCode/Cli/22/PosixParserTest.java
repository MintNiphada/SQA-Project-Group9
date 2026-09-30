package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-"}, result);

        result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenRecognizedOption() {
        options.addOption("f", "foo", false, "foo option");

        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenRecognizedOptionWithValue() {
        options.addOption("f", "foo", true, "foo option with value");

        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenUnrecognizedOptionStopAtNonOptionFalse() {
        String[] args = new String[]{"--unknown"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--", "--unknown"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenUnrecognizedOptionStopAtNonOptionTrue() {
        String[] args = new String[]{"--unknown", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"--", "--unknown", "extra1", "extra2"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionRecognized() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a"}, result);

        result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionUnrecognizedStopAtNonOptionFalse() {
        String[] args = new String[]{"-u", "other"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-u", "other"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionUnrecognizedStopAtNonOptionTrue() {
        String[] args = new String[]{"-u", "other1", "other2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-u", "other1", "other2"}, result);
    }

    @Test
    public void testFlattenMultiCharRecognizedOption() {
        options.addOption("foo", false, "option -foo");

        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-foo"}, result);
    }

    @Test
    public void testFlattenBurstTokenAllSingleOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testFlattenBurstTokenWithArgumentAttached() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");

        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    @Test
    public void testFlattenBurstTokenWithArgAtEnd() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");

        String[] args = new String[]{"-ab"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testFlattenBurstTokenUnrecognizedStopAtNonOptionFalse() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"-ax"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-ax"}, result);
    }

    @Test
    public void testFlattenBurstTokenUnrecognizedStopAtNonOptionTrue() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"-axyz", "trailing1", "trailing2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a", "--", "xyz", "trailing1", "trailing2"}, result);
    }

    @Test
    public void testFlattenBurstTokenFirstCharUnrecognizedStopAtNonOptionTrue() {
        String[] args = new String[]{"-xyz", "next"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"--", "xyz", "next"}, result);
    }

    @Test
    public void testFlattenNonOptionTokenStopAtNonOptionFalse() {
        String[] args = new String[]{"filename1", "filename2"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"filename1", "filename2"}, result);
    }

    @Test
    public void testFlattenNonOptionTokenStopAtNonOptionTrue() {
        String[] args = new String[]{"filename1", "filename2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"--", "filename1", "filename2"}, result);
    }

    @Test
    public void testParserReuseClearsState() {
        options.addOption("a", false, "option a");

        String[] firstArgs = new String[]{"unrecognized", "remaining"};
        String[] firstResult = parser.flatten(options, firstArgs, true);
        Assert.assertArrayEquals(new String[]{"--", "unrecognized", "remaining"}, firstResult);

        String[] secondArgs = new String[]{"-a"};
        String[] secondResult = parser.flatten(options, secondArgs, false);
        Assert.assertArrayEquals(new String[]{"-a"}, secondResult);
    }

    @Test
    public void testParseCommandLineIntegration() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-ab", "valB", "-c", "arg1", "arg2"};
        CommandLine cl = parser.parse(options, args);

        Assert.assertTrue(cl.hasOption("a"));
        Assert.assertTrue(cl.hasOption("b"));
        Assert.assertEquals("valB", cl.getOptionValue("b"));
        Assert.assertTrue(cl.hasOption("c"));
        Assert.assertEquals(Arrays.asList("arg1", "arg2"), cl.getArgList());
    }

    @Test
    public void testParseCommandLineStopAtNonOption() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        String[] args = new String[]{"-a", "nonOption", "-b"};
        CommandLine cl = parser.parse(options, args, true);

        Assert.assertTrue(cl.hasOption("a"));
        Assert.assertFalse(cl.hasOption("b"));
        Assert.assertEquals(Arrays.asList("nonOption", "-b"), cl.getArgList());
    }

    @Test
    public void testDoubleHyphenAloneToken() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"--", "-a"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--", "--", "-a"}, result);
    }
}