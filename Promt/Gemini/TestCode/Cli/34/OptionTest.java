package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class OptionTest
{
    @Test
    public void testConstructorsAndGetters()
    {
        Option opt1 = new Option("a", "description A");
        Assert.assertEquals("a", opt1.getOpt());
        Assert.assertNull(opt1.getLongOpt());
        Assert.assertFalse(opt1.hasArg());
        Assert.assertEquals("description A", opt1.getDescription());
        Assert.assertEquals(Option.UNINITIALIZED, opt1.getArgs());

        Option opt2 = new Option("b", true, "description B");
        Assert.assertEquals("b", opt2.getOpt());
        Assert.assertNull(opt2.getLongOpt());
        Assert.assertTrue(opt2.hasArg());
        Assert.assertEquals(1, opt2.getArgs());
        Assert.assertEquals("description B", opt2.getDescription());

        Option opt3 = new Option("c", "long-c", false, "description C");
        Assert.assertEquals("c", opt3.getOpt());
        Assert.assertEquals("long-c", opt3.getLongOpt());
        Assert.assertFalse(opt3.hasArg());
        Assert.assertEquals(Option.UNINITIALIZED, opt3.getArgs());
        Assert.assertEquals("description C", opt3.getDescription());

        Option opt4 = new Option(null, "long-only", true, "description Long Only");
        Assert.assertNull(opt4.getOpt());
        Assert.assertEquals("long-only", opt4.getLongOpt());
        Assert.assertTrue(opt4.hasArg());
        Assert.assertEquals(1, opt4.getArgs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOptionCharacter()
    {
        new Option("?", "illegal character");
    }

    @Test
    public void testGetIdAndGetKey()
    {
        Option opt = new Option("f", "foo", false, "desc");
        Assert.assertEquals('f', opt.getId());
        Assert.assertEquals("f", opt.getKey());

        Option longOnly = new Option(null, "bar", false, "desc");
        Assert.assertEquals('b', longOnly.getId());
        Assert.assertEquals("bar", longOnly.getKey());
    }

    @Test
    public void testType()
    {
        Option opt = new Option("t", "desc");
        Assert.assertNull(opt.getType());

        opt.setType(Integer.class);
        Assert.assertEquals(Integer.class, opt.getType());
    }

    @Test
    public void testLongOpt()
    {
        Option opt = new Option("o", "desc");
        Assert.assertFalse(opt.hasLongOpt());
        Assert.assertNull(opt.getLongOpt());

        opt.setLongOpt("option");
        Assert.assertTrue(opt.hasLongOpt());
        Assert.assertEquals("option", opt.getLongOpt());

        opt.setLongOpt(null);
        Assert.assertFalse(opt.hasLongOpt());
        Assert.assertNull(opt.getLongOpt());
    }

    @Test
    public void testOptionalArg()
    {
        Option opt = new Option("o", true, "desc");
        Assert.assertFalse(opt.hasOptionalArg());

        opt.setOptionalArg(true);
        Assert.assertTrue(opt.hasOptionalArg());

        opt.setOptionalArg(false);
        Assert.assertFalse(opt.hasOptionalArg());
    }

    @Test
    public void testDescription()
    {
        Option opt = new Option("d", "initial desc");
        Assert.assertEquals("initial desc", opt.getDescription());

        opt.setDescription("updated desc");
        Assert.assertEquals("updated desc", opt.getDescription());
    }

    @Test
    public void testRequired()
    {
        Option opt = new Option("r", "desc");
        Assert.assertFalse(opt.isRequired());

        opt.setRequired(true);
        Assert.assertTrue(opt.isRequired());

        opt.setRequired(false);
        Assert.assertFalse(opt.isRequired());
    }

    @Test
    public void testArgName()
    {
        Option opt = new Option("a", "desc");
        Assert.assertNull(opt.getArgName());
        Assert.assertFalse(opt.hasArgName());

        opt.setArgName("");
        Assert.assertEquals("", opt.getArgName());
        Assert.assertFalse(opt.hasArgName());

        opt.setArgName("file");
        Assert.assertEquals("file", opt.getArgName());
        Assert.assertTrue(opt.hasArgName());

        opt.setArgName(null);
        Assert.assertNull(opt.getArgName());
        Assert.assertFalse(opt.hasArgName());
    }

    @Test
    public void testArgsConfiguration()
    {
        Option opt = new Option("a", "desc");
        Assert.assertEquals(Option.UNINITIALIZED, opt.getArgs());
        Assert.assertFalse(opt.hasArg());
        Assert.assertFalse(opt.hasArgs());

        opt.setArgs(1);
        Assert.assertEquals(1, opt.getArgs());
        Assert.assertTrue(opt.hasArg());
        Assert.assertFalse(opt.hasArgs());

        opt.setArgs(2);
        Assert.assertEquals(2, opt.getArgs());
        Assert.assertTrue(opt.hasArg());
        Assert.assertTrue(opt.hasArgs());

        opt.setArgs(Option.UNLIMITED_VALUES);
        Assert.assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
        Assert.assertTrue(opt.hasArg());
        Assert.assertTrue(opt.hasArgs());

        opt.setArgs(0);
        Assert.assertEquals(0, opt.getArgs());
        Assert.assertFalse(opt.hasArg());
        Assert.assertFalse(opt.hasArgs());
    }

    @Test
    public void testValueSeparator()
    {
        Option opt = new Option("s", "desc");
        Assert.assertFalse(opt.hasValueSeparator());
        Assert.assertEquals((char) 0, opt.getValueSeparator());

        opt.setValueSeparator('=');
        Assert.assertTrue(opt.hasValueSeparator());
        Assert.assertEquals('=', opt.getValueSeparator());

        opt.setValueSeparator((char) 0);
        Assert.assertFalse(opt.hasValueSeparator());
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingNoArgsAllowed()
    {
        Option opt = new Option("n", "desc");
        opt.addValueForProcessing("value");
    }

    @Test
    public void testAddValueForProcessingSingleArg()
    {
        Option opt = new Option("s", true, "desc");
        opt.addValueForProcessing("val1");

        Assert.assertEquals("val1", opt.getValue());
        Assert.assertEquals("val1", opt.getValue(0));
        Assert.assertEquals("val1", opt.getValue("default"));
        Assert.assertArrayEquals(new String[]{"val1"}, opt.getValues());
        Assert.assertEquals(1, opt.getValuesList().size());
        Assert.assertEquals("val1", opt.getValuesList().get(0));
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingExceedsLimit()
    {
        Option opt = new Option("s", true, "desc");
        opt.addValueForProcessing("val1");
        opt.addValueForProcessing("val2");
    }

    @Test
    public void testAddValueForProcessingWithSeparator()
    {
        Option opt = new Option("D", "desc");
        opt.setArgs(2);
        opt.setValueSeparator('=');

        opt.addValueForProcessing("key=value");
        Assert.assertEquals("key", opt.getValue(0));
        Assert.assertEquals("value", opt.getValue(1));
        Assert.assertArrayEquals(new String[]{"key", "value"}, opt.getValues());
    }

    @Test
    public void testAddValueForProcessingWithSeparatorExceedingTokens()
    {
        Option opt = new Option("D", "desc");
        opt.setArgs(2);
        opt.setValueSeparator('=');

        opt.addValueForProcessing("key=val1=val2");
        Assert.assertEquals(2, opt.getValues().length);
        Assert.assertEquals("key", opt.getValue(0));
        Assert.assertEquals("val1=val2", opt.getValue(1));
    }

    @Test
    public void testAddValueForProcessingUnlimitedValuesWithSeparator()
    {
        Option opt = new Option("p", "desc");
        opt.setArgs(Option.UNLIMITED_VALUES);
        opt.setValueSeparator(',');

        opt.addValueForProcessing("a,b,c,d");
        Assert.assertArrayEquals(new String[]{"a", "b", "c", "d"}, opt.getValues());
    }

    @Test
    public void testGetValueDefault()
    {
        Option opt = new Option("v", true, "desc");
        Assert.assertNull(opt.getValue());
        Assert.assertNull(opt.getValue(0));
        Assert.assertNull(opt.getValues());
        Assert.assertEquals("defaultVal", opt.getValue("defaultVal"));

        opt.addValueForProcessing("actualVal");
        Assert.assertEquals("actualVal", opt.getValue("defaultVal"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexOutOfBounds()
    {
        Option opt = new Option("v", true, "desc");
        opt.addValueForProcessing("first");
        opt.getValue(5);
    }

    @Test
    public void testClearValues()
    {
        Option opt = new Option("c", true, "desc");
        opt.addValueForProcessing("temp");
        Assert.assertEquals(1, opt.getValuesList().size());

        opt.clearValues();
        Assert.assertNull(opt.getValue());
        Assert.assertNull(opt.getValues());
        Assert.assertTrue(opt.getValuesList().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeprecatedAddValue()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValue("value");
    }

    @Test
    public void testAcceptsArg()
    {
        Option opt = new Option("a", "desc");
        Assert.assertFalse(opt.acceptsArg());

        opt.setOptionalArg(true);
        Assert.assertTrue(opt.acceptsArg());

        opt.setOptionalArg(false);
        opt.setArgs(1);
        Assert.assertTrue(opt.acceptsArg());

        opt.addValueForProcessing("val");
        Assert.assertFalse(opt.acceptsArg());

        opt.setArgs(Option.UNLIMITED_VALUES);
        Assert.assertTrue(opt.acceptsArg());
    }

    @Test
    public void testRequiresArg()
    {
        Option opt = new Option("r", "desc");
        Assert.assertFalse(opt.requiresArg());

        opt.setArgs(1);
        Assert.assertTrue(opt.requiresArg());
        opt.addValueForProcessing("val");
        Assert.assertFalse(opt.requiresArg());

        Option optUnlimited = new Option("u", "desc");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        Assert.assertTrue(optUnlimited.requiresArg());
        optUnlimited.addValueForProcessing("val1");
        Assert.assertFalse(optUnlimited.requiresArg());
        optUnlimited.addValueForProcessing("val2");
        Assert.assertFalse(optUnlimited.requiresArg());

        Option optOptional = new Option("o", "desc");
        optOptional.setArgs(1);
        optOptional.setOptionalArg(true);
        Assert.assertFalse(optOptional.requiresArg());
    }

    @Test
    public void testToString()
    {
        Option opt1 = new Option("a", "desc");
        Assert.assertEquals("[ option: a  :: desc ]", opt1.toString());

        Option opt2 = new Option("b", "longOpt", false, "desc");
        Assert.assertEquals("[ option: b longOpt  :: desc ]", opt2.toString());

        Option opt3 = new Option("c", "longOpt", true, "desc");
        Assert.assertEquals("[ option: c longOpt  [ARG] :: desc ]", opt3.toString());

        Option opt4 = new Option("d", "longOpt", false, "desc");
        opt4.setArgs(2);
        Assert.assertEquals("[ option: d longOpt [ARG...] :: desc ]", opt4.toString());

        Option opt5 = new Option("e", "longOpt", true, "desc");
        opt5.setType(String.class);
        Assert.assertEquals("[ option: e longOpt  [ARG] :: desc :: class java.lang.String ]", opt5.toString());
    }

    @Test
    public void testEqualsAndHashCode()
    {
        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("a", "alpha", false, "desc");
        Option opt3 = new Option("b", "beta", false, "desc");
        Option opt4 = new Option("a", "beta", false, "desc");
        Option opt5 = new Option("b", "alpha", false, "desc");
        Option opt6 = new Option("a", null, false, "desc");
        Option opt7 = new Option("a", null, false, "desc");
        Option opt8 = new Option(null, "alpha", false, "desc");
        Option opt9 = new Option(null, "alpha", false, "desc");
        Option opt10 = new Option(null, null, false, "desc");
        Option opt11 = new Option(null, null, false, "desc");

        Assert.assertTrue(opt1.equals(opt1));
        Assert.assertTrue(opt1.equals(opt2));
        Assert.assertEquals(opt1.hashCode(), opt2.hashCode());

        Assert.assertFalse(opt1.equals(null));
        Assert.assertFalse(opt1.equals("not an option"));

        Assert.assertFalse(opt1.equals(opt3));
        Assert.assertFalse(opt1.equals(opt4));
        Assert.assertFalse(opt1.equals(opt5));

        Assert.assertTrue(opt6.equals(opt7));
        Assert.assertEquals(opt6.hashCode(), opt7.hashCode());
        Assert.assertFalse(opt1.equals(opt6));
        Assert.assertFalse(opt6.equals(opt1));

        Assert.assertTrue(opt8.equals(opt9));
        Assert.assertEquals(opt8.hashCode(), opt9.hashCode());
        Assert.assertFalse(opt1.equals(opt8));
        Assert.assertFalse(opt8.equals(opt1));

        Assert.assertTrue(opt10.equals(opt11));
        Assert.assertEquals(opt10.hashCode(), opt11.hashCode());
        Assert.assertFalse(opt10.equals(opt8));
        Assert.assertFalse(opt8.equals(opt10));
    }

    @Test
    public void testClone()
    {
        Option original = new Option("c", "cloneable", true, "desc");
        original.addValueForProcessing("val1");

        Option cloned = (Option) original.clone();
        Assert.assertNotSame(original, cloned);
        Assert.assertEquals(original, cloned);
        Assert.assertEquals(original.getValue(), cloned.getValue());

        cloned.clearValues();
        Assert.assertEquals(1, original.getValuesList().size());
        Assert.assertTrue(cloned.getValuesList().isEmpty());
    }
}