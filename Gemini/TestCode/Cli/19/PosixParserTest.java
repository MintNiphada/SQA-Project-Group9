package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void testFlattenSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-"}, result);

        result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenDoubleHyphen() {
        String[] args = new String[]{"--"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlattenLongOptionWithoutEquals() {
        options.addOption("foo", "foo-long", false, "description");
        String[] args = new String[]{"--foo-long"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo-long"}, result);
    }

    @Test
    public void testFlattenLongOptionWithEquals() {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlattenSingleCharOptionValid() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenSingleCharOptionInvalidStopAtNonOptionFalse() {
        String[] args = new String[]{"-z"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[0], result);
    }

    @Test
    public void testFlattenSingleCharOptionInvalidStopAtNonOptionTrue() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-z", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlattenLongOptionWithSingleHyphen() {
        options.addOption("foo", false, "option foo");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-foo"}, result);
    }

    @Test
    public void testBurstTokenMultipleSingleCharOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testBurstTokenWithArgAttached() {
        options.addOption("a", false, "option a");
        options.addOption(OptionBuilder.hasArg().create('b'));

        String[] args = new String[]{"-abfile.txt"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-b", "file.txt"}, result);
    }

    @Test
    public void testBurstTokenUnrecognizedOptionStopAtNonOptionFalse() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"-a", "-az"}, result);
    }

    @Test
    public void testBurstTokenUnrecognizedOptionStopAtNonOptionTrue() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "rest1", "rest2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a", "--", "z", "rest1", "rest2"}, result);
    }

    @Test
    public void testNonOptionArgumentStopAtNonOptionFalse() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, false);
        Assert.assertArrayEquals(new String[]{"nonOption1", "nonOption2"}, result);
    }

    @Test
    public void testNonOptionArgumentStopAtNonOptionTrueWithoutPriorOption() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"--", "nonOption1", "nonOption2"}, result);
    }

    @Test
    public void testNonOptionArgumentFollowingOptionWithArgStopAtNonOptionTrue() {
        options.addOption(OptionBuilder.hasArg().create('a'));
        String[] args = new String[]{"-a", "value", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        Assert.assertArrayEquals(new String[]{"-a", "value", "--", "extra1", "extra2"}, result);
    }

    @Test
    public void testFullParseIntegration() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption(OptionBuilder.hasArg().create('b'));
        options.addOption("c", "c-long", false, "option c");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b", "foo", "--c-long", "extra"});
        Assert.assertTrue(cmd.hasOption('a'));
        Assert.assertTrue(cmd.hasOption('b'));
        Assert.assertEquals("foo", cmd.getOptionValue('b'));
        Assert.assertTrue(cmd.hasOption("c-long"));
        Assert.assertEquals(1, cmd.getArgs().length);
        Assert.assertEquals("extra", cmd.getArgs()[0]);
    }

    @Test
    public void testConsecutiveParseResetsState() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption(OptionBuilder.hasArg().create('b'));

        CommandLine cmd1 = parser.parse(options, new String[]{"-a", "file1"}, true);
        Assert.assertTrue(cmd1.hasOption('a'));
        Assert.assertArrayEquals(new String[]{"file1"}, cmd1.getArgs());

        CommandLine cmd2 = parser.parse(options, new String[]{"-b", "val", "file2"}, true);
        Assert.assertTrue(cmd2.hasOption('b'));
        Assert.assertEquals("val", cmd2.getOptionValue('b'));
        Assert.assertArrayEquals(new String[]{"file2"}, cmd2.getArgs());
    }

    @Test
    public void testBurstTokenDirectly() {
        options.addOption("x", false, "option x");
        options.addOption("y", false, "option y");

        parser.flatten(options, new String[0], false);
        parser.burstToken("-xy", false);
        String[] result = parser.flatten(options, new String[]{"-xy"}, false);
        Assert.assertArrayEquals(new String[]{"-x", "-y"}, result);
    }
}