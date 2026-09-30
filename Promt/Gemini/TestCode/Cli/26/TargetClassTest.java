package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

public class OptionBuilderTest
{
    @Test
    public void testCompleteOptionWithChar()
    {
        Option option = OptionBuilder.withLongOpt("simple-option")
                                     .hasArg()
                                     .isRequired()
                                     .hasArgs()
                                     .withType(String.class)
                                     .withDescription("this is a simple option")
                                     .create('s');

        Assert.assertEquals("s", option.getOpt());
        Assert.assertEquals("simple-option", option.getLongOpt());
        Assert.assertEquals("this is a simple option", option.getDescription());
        Assert.assertEquals(String.class, option.getType());
        Assert.assertTrue(option.isRequired());
        Assert.assertTrue(option.hasArgs());
    }

    @Test
    public void testCompleteOptionWithString()
    {
        Option option = OptionBuilder.withLongOpt("simple-option")
                                     .hasArg()
                                     .isRequired()
                                     .hasArgs()
                                     .withType(String.class)
                                     .withDescription("this is a simple option")
                                     .create("s");

        Assert.assertEquals("s", option.getOpt());
        Assert.assertEquals("simple-option", option.getLongOpt());
        Assert.assertEquals("this is a simple option", option.getDescription());
        Assert.assertEquals(String.class, option.getType());
        Assert.assertTrue(option.isRequired());
        Assert.assertTrue(option.hasArgs());
    }

    @Test
    public void testTwoCompleteOptions()
    {
        Option simple = OptionBuilder.withLongOpt("simple-option")
                                     .hasArg()
                                     .isRequired()
                                     .hasArgs()
                                     .withType(String.class)
                                     .withDescription("this is a simple option")
                                     .create('s');

        Assert.assertEquals("s", simple.getOpt());
        Assert.assertEquals("simple-option", simple.getLongOpt());
        Assert.assertEquals("this is a simple option", simple.getDescription());
        Assert.assertEquals(String.class, simple.getType());
        Assert.assertTrue(simple.isRequired());
        Assert.assertTrue(simple.hasArgs());

        Option complex = OptionBuilder.withLongOpt("dimple-option")
                                      .hasArg(false)
                                      .isRequired(false)
                                      .withArgName("dimple")
                                      .withDescription("this is a dimple option")
                                      .create('d');

        Assert.assertEquals("d", complex.getOpt());
        Assert.assertEquals("dimple-option", complex.getLongOpt());
        Assert.assertEquals("this is a dimple option", complex.getDescription());
        Assert.assertNull(complex.getType());
        Assert.assertFalse(complex.isRequired());
        Assert.assertFalse(complex.hasArg());
        Assert.assertEquals("dimple", complex.getArgName());
    }

    @Test
    public void testCreateEmpty()
    {
        Option option = OptionBuilder.withLongOpt("foo").create();
        Assert.assertEquals("foo", option.getLongOpt());
        Assert.assertNull(option.getOpt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmptyWithoutLongOptThrowsException()
    {
        OptionBuilder.create();
    }

    @Test
    public void testHasArgBoolean()
    {
        Option optionWithArg = OptionBuilder.hasArg(true).create('a');
        Assert.assertEquals(1, optionWithArg.getArgs());

        Option optionWithoutArg = OptionBuilder.hasArg(false).create('b');
        Assert.assertEquals(Option.UNINITIALIZED, optionWithoutArg.getArgs());
    }

    @Test
    public void testHasArgs()
    {
        Option option = OptionBuilder.hasArgs().create('a');
        Assert.assertEquals(Option.UNLIMITED_VALUES, option.getArgs());

        Option optionNum = OptionBuilder.hasArgs(3).create('b');
        Assert.assertEquals(3, optionNum.getArgs());
    }

    @Test
    public void testHasOptionalArg()
    {
        Option option = OptionBuilder.hasOptionalArg().create('a');
        Assert.assertEquals(1, option.getArgs());
        Assert.assertTrue(option.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgs()
    {
        Option option = OptionBuilder.hasOptionalArgs().create('a');
        Assert.assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        Assert.assertTrue(option.hasOptionalArg());

        Option optionNum = OptionBuilder.hasOptionalArgs(4).create('b');
        Assert.assertEquals(4, optionNum.getArgs());
        Assert.assertTrue(optionNum.hasOptionalArg());
    }

    @Test
    public void testWithValueSeparator()
    {
        Option optionDefaultSep = OptionBuilder.withValueSeparator().create('a');
        Assert.assertEquals('=', optionDefaultSep.getValueSeparator());

        Option optionCustomSep = OptionBuilder.withValueSeparator(':').create('b');
        Assert.assertEquals(':', optionCustomSep.getValueSeparator());
    }

    @Test
    public void testWithArgName()
    {
        Option option = OptionBuilder.withArgName("file").create('f');
        Assert.assertEquals("file", option.getArgName());
    }

    @Test
    public void testIsRequired()
    {
        Option optionReq = OptionBuilder.isRequired().create('r');
        Assert.assertTrue(optionReq.isRequired());

        Option optionNotReq = OptionBuilder.isRequired(false).create('n');
        Assert.assertFalse(optionNotReq.isRequired());

        Option optionReqBool = OptionBuilder.isRequired(true).create('y');
        Assert.assertTrue(optionReqBool.isRequired());
    }

    @Test
    public void testBuilderResetAfterFailedCreate()
    {
        try
        {
            OptionBuilder.withDescription("desc").create();
            Assert.fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected, builder should be reset
        }

        Option option = OptionBuilder.create('a');
        Assert.assertNull(option.getDescription());
        Assert.assertNull(option.getLongOpt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOptionChar()
    {
        OptionBuilder.create('?');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOptionString()
    {
        OptionBuilder.create("opt with spaces");
    }
}