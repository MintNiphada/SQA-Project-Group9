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
    public void testFlattenEmptyArgs() {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testFlattenDoubleHyphenOptionWithoutEquals() {
        options.addOption("foo", false, "Foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenOptionWithEquals() {
        options.addOption("foo", true, "Foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlattenDoubleHyphenAlone() {
        String[] args = new String[]{"--"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenSingleOptionValid() {
        options.addOption("a", false, "Option A");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenSingleOptionValidWithStopAtNonOption() {
        options.addOption("a", false, "Option A");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenSingleOptionInvalidWithoutStopAtNonOption() {
        String[] args = new String[]{"-z"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testFlattenSingleOptionInvalidWithStopAtNonOption() {
        String[] args = new String[]{"-z", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"extra1", "extra2"}, result);
    }

    @Test
    public void testFlattenMultiCharDirectOption() {
        Option opt = new Option("foo", "Multi-char opt");
        options.addOption(opt);
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-foo"}, result);
    }

    @Test
    public void testFlattenBurstingAllValidNoArgs() {
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");
        options.addOption("c", false, "Option C");

        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testFlattenBurstingWithArgOnLastOption() {
        options.addOption("a", false, "Option A");
        options.addOption("b", true, "Option B with arg");

        String[] args = new String[]{"-ab", "foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "foo"}, result);
    }

    @Test
    public void testFlattenBurstingWithArgAttachedToOption() {
        options.addOption("a", false, "Option A");
        options.addOption("b", true, "Option B with arg");

        String[] args = new String[]{"-abBAR"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "BAR"}, result);
    }

    @Test
    public void testFlattenBurstingUnrecognizedOptionWithoutStopAtNonOption() {
        options.addOption("a", false, "Option A");

        String[] args = new String[]{"-azc"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-azc"}, result);
    }

    @Test
    public void testFlattenBurstingUnrecognizedOptionWithStopAtNonOption() {
        options.addOption("a", false, "Option A");

        String[] args = new String[]{"-azc", "rest1", "rest2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a", "--", "zc", "rest1", "rest2"}, result);
    }

    @Test
    public void testFlattenNonOptionTokensWithoutStopAtNonOption() {
        String[] args = new String[]{"arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenNonOptionTokensWithStopAtNonOptionNoCurrentOption() {
        String[] args = new String[]{"arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenNonOptionTokensWithStopAtNonOptionWithCurrentOptionHavingArg() {
        options.addOption("a", true, "Option A with arg");
        String[] args = new String[]{"-a", "argValue", "arg2", "arg3"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a", "argValue", "--", "arg2", "arg3"}, result);
    }

    @Test
    public void testFlattenStateResetBetweenCalls() {
        options.addOption("a", true, "Option A with arg");

        String[] args1 = new String[]{"-a", "val1", "nonOption"};
        String[] result1 = parser.flatten(options, args1, true);
        Assert.assertArrayEquals(new String[]{"-a", "val1", "--", "nonOption"}, result1);

        String[] args2 = new String[]{"-a", "val2"};
        String[] result2 = parser.flatten(options, args2, true);
        Assert.assertArrayEquals(new String[]{"-a", "val2"}, result2);
    }

    @Test
    public void testParseFullIntegration() throws Exception {
        options.addOption("a", false, "Option A");
        options.addOption("b", true, "Option B");
        options.addOption("c", false, "Option C");

        String[] args = new String[]{"-a", "-bvalue", "-c", "file1", "file2"};
        CommandLine cl = parser.parse(options, args);

        Assert.assertTrue(cl.hasOption("a"));
        Assert.assertTrue(cl.hasOption("b"));
        Assert.assertEquals("value", cl.getOptionValue("b"));
        Assert.assertTrue(cl.hasOption("c"));
        Assert.assertEquals(Arrays.asList("file1", "file2"), cl.getArgList());
    }

    @Test
    public void testParseWithStopAtNonOptionIntegration() throws Exception {
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");

        String[] args = new String[]{"-a", "nonOption", "-b"};
        CommandLine cl = parser.parse(options, args, true);

        Assert.assertTrue(cl.hasOption("a"));
        Assert.assertFalse(cl.hasOption("b"));
        Assert.assertEquals(Arrays.asList("nonOption", "-b"), cl.getArgList());
    }

    @Test
    public void testBurstTokenDirectly() {
        options.addOption("x", false, "Option X");
        options.addOption("y", false, "Option Y");

        parser.flatten(options, new String[0], false);
        parser.burstToken("-xy", false);
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        Assert.assertArrayEquals(new String[]{"-x"}, result);
    }
}