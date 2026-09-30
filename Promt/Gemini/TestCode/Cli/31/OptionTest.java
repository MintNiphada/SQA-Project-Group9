package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

public class OptionTest
{
    @Test
    public void testConstructors()
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
        Assert.assertEquals("description B", opt2.getDescription());
        Assert.assertEquals(1, opt2.getArgs());

        Option opt3 = new Option("c", "opt-c", false, "description C");
        Assert.assertEquals("c", opt3.getOpt());
        Assert.assertEquals("opt-c", opt3.getLongOpt());
        Assert.assertFalse(opt3.hasArg());
        Assert.assertEquals("description C", opt3.getDescription());
        Assert.assertEquals(Option.UNINITIALIZED, opt3.getArgs());

        Option opt4 = new Option(null, "long-only", true, "description Long");
        Assert.assertNull(opt4.getOpt());
        Assert.assertEquals("long-only", opt4.getLongOpt());
        Assert.assertTrue(opt4.hasArg());
        Assert.assertEquals("description Long", opt4.getDescription());
        Assert.assertEquals(1, opt4.getArgs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOptionName()
    {
        new Option("?", "Illegal char");
    }

    @Test
    public void testGetIdAndGetKey()
    {
        Option opt = new Option("a", "alpha", false, "desc");
        Assert.assertEquals("a", opt.getKey());
        Assert.assertEquals('a', opt.getId());

        Option longOnly = new Option(null, "alpha", false, "desc");
        Assert.assertEquals("alpha", longOnly.getKey());
        Assert.assertEquals('a', longOnly.getId());
    }

    @Test
    public void testType()
    {
        Option opt = new Option("t", "type test");
        Assert.assertNull(opt.getType());
        opt.setType(Integer.class);
        Assert.assertEquals(Integer.class, opt.getType());
    }

    @Test
    public void testLongOpt()
    {
        Option opt = new Option("o", "desc");
        Assert.assertNull(opt.getLongOpt());
        Assert.assertFalse(opt.hasLongOpt());

        opt.setLongOpt("option");
        Assert.assertEquals("option", opt.getLongOpt());
        Assert.assertTrue(opt.hasLongOpt());

        opt.setLongOpt(null);
        Assert.assertNull(opt.getLongOpt());
        Assert.assertFalse(opt.hasLongOpt());
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
        Option opt = new Option("o", "desc");
        Assert.assertEquals("desc", opt.getDescription());
        opt.setDescription("new desc");
        Assert.assertEquals("new desc", opt.getDescription());
    }

    @Test
    public void testRequired()
    {
        Option opt = new Option("o", "desc");
        Assert.assertFalse(opt.isRequired());
        opt.setRequired(true);
        Assert.assertTrue(opt.isRequired());
        opt.setRequired(false);
        Assert.assertFalse(opt.isRequired());
    }

    @Test
    public void testArgName()
    {
        Option opt = new Option("o", "desc");
        Assert.assertEquals("arg", opt.getArgName());
        Assert.assertTrue(opt.hasArgName());

        opt.setArgName(null);
        Assert.assertNull(opt.getArgName());
        Assert.assertFalse(opt.hasArgName());

        opt.setArgName("");
        Assert.assertEquals("", opt.getArgName());
        Assert.assertFalse(opt.hasArgName());

        opt.setArgName("custom");
        Assert.assertEquals("custom", opt.getArgName());
        Assert.assertTrue(opt.hasArgName());
    }

    @Test
    public void testArgsSettings()
    {
        Option opt = new Option("o", "desc");
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
        Option opt = new Option("o", "desc");
        Assert.assertFalse(opt.hasValueSeparator());
        Assert.assertEquals((char) 0, opt.getValueSeparator());

        opt.setValueSeparator('=');
        Assert.assertTrue(opt.hasValueSeparator());
        Assert.assertEquals('=', opt.getValueSeparator());
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingUninitialized()
    {
        Option opt = new Option("o", "desc");
        opt.addValueForProcessing("value");
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingExceedLimit()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValueForProcessing("v1");
        opt.addValueForProcessing("v2");
    }

    @Test
    public void testAddValueForProcessingSingle()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValueForProcessing("val1");

        Assert.assertEquals("val1", opt.getValue());
        Assert.assertEquals("val1", opt.getValue(0));
        Assert.assertEquals("val1", opt.getValue("default"));
        Assert.assertArrayEquals(new String[]{"val1"}, opt.getValues());
        Assert.assertEquals(1, opt.getValuesList().size());
        Assert.assertEquals("val1", opt.getValuesList().get(0));
    }

    @Test
    public void testAddValueForProcessingMultiple()
    {
        Option opt = new Option("o", "desc");
        opt.setArgs(3);
        opt.addValueForProcessing("v1");
        opt.addValueForProcessing("v2");

        Assert.assertEquals("v1", opt.getValue());
        Assert.assertEquals("v2", opt.getValue(1));
        Assert.assertArrayEquals(new String[]{"v1", "v2"}, opt.getValues());
        Assert.assertEquals(Arrays.asList("v1", "v2"), opt.getValuesList());
    }

    @Test
    public void testAddValueForProcessingWithSeparator()
    {
        Option opt = new Option("D", "property option");
        opt.setArgs(Option.UNLIMITED_VALUES);
        opt.setValueSeparator('=');
        opt.addValueForProcessing("key=val");

        Assert.assertArrayEquals(new String[]{"key", "val"}, opt.getValues());
    }

    @Test
    public void testAddValueForProcessingSeparatorLimitedArgs()
    {
        Option opt = new Option("m", "multi-part");
        opt.setArgs(2);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("a,b,c,d");

        Assert.assertArrayEquals(new String[]{"a", "b,c,d"}, opt.getValues());
    }

    @Test
    public void testGetValuesWhenEmpty()
    {
        Option opt = new Option("o", true, "desc");
        Assert.assertNull(opt.getValue());
        Assert.assertNull(opt.getValue(0));
        Assert.assertNull(opt.getValues());
        Assert.assertEquals("default", opt.getValue("default"));
        Assert.assertNotNull(opt.getValuesList());
        Assert.assertTrue(opt.getValuesList().isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexOutOfBounds()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValueForProcessing("v1");
        opt.getValue(5);
    }

    @Test
    public void testClearValues()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValueForProcessing("v1");
        Assert.assertEquals("v1", opt.getValue());

        opt.clearValues();
        Assert.assertNull(opt.getValue());
        Assert.assertNull(opt.getValues());
        Assert.assertTrue(opt.getValuesList().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddValueDeprecated()
    {
        Option opt = new Option("o", true, "desc");
        opt.addValue("test");
    }

    @Test
    public void testAcceptsArg()
    {
        Option opt = new Option("o", "desc");
        Assert.assertFalse(opt.acceptsArg());

        opt.setArgs(1);
        Assert.assertTrue(opt.acceptsArg());
        opt.addValueForProcessing("val");
        Assert.assertFalse(opt.acceptsArg());

        Option optUnlimited = new Option("u", "desc");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        Assert.assertTrue(optUnlimited.acceptsArg());
        optUnlimited.addValueForProcessing("val1");
        Assert.assertTrue(optUnlimited.acceptsArg());

        Option optOptional = new Option("p", "desc");
        optOptional.setOptionalArg(true);
        Assert.assertTrue(optOptional.acceptsArg());
    }

    @Test
    public void testRequiresArg()
    {
        Option opt = new Option("o", "desc");
        Assert.assertFalse(opt.requiresArg());

        opt.setArgs(1);
        Assert.assertTrue(opt.requiresArg());
        opt.addValueForProcessing("v1");
        Assert.assertFalse(opt.requiresArg());

        Option optOptional = new Option("opt", true, "desc");
        optOptional.setOptionalArg(true);
        Assert.assertFalse(optOptional.requiresArg());

        Option optUnlimited = new Option("u", "desc");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        Assert.assertTrue(optUnlimited.requiresArg());
        optUnlimited.addValueForProcessing("v1");
        Assert.assertFalse(optUnlimited.requiresArg());
    }

    @Test
    public void testToString()
    {
        Option opt1 = new Option("a", "desc a");
        Assert.assertEquals("[ option: a  :: desc a ]", opt1.toString());

        Option opt2 = new Option("b", "long-b", true, "desc b");
        Assert.assertEquals("[ option: b long-b  [ARG] :: desc b ]", opt2.toString());

        Option opt3 = new Option("c", "long-c", false, "desc c");
        opt3.setArgs(2);
        opt3.setType(String.class);
        Assert.assertEquals("[ option: c long-c [ARG...] :: desc c :: class java.lang.String ]", opt3.toString());

        Option opt4 = new Option(null, "long-only", false, "desc long only");
        Assert.assertEquals("[ option: null long-only  :: desc long only ]", opt4.toString());
    }

    @Test
    public void testEqualsAndHashCode()
    {
        Option a1 = new Option("a", "alpha", false, "desc");
        Option a2 = new Option("a", "alpha", true, "desc diff");
        Option a3 = new Option("a", "other", false, "desc");
        Option b1 = new Option("b", "alpha", false, "desc");
        Option long1 = new Option(null, "long", false, "desc");
        Option long2 = new Option(null, "long", true, "desc");
        Option long3 = new Option(null, "other", false, "desc");

        Assert.assertEquals(a1, a1);
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        Assert.assertNotEquals(a1, null);
        Assert.assertNotEquals(a1, "some string");
        Assert.assertNotEquals(a1, a3);
        Assert.assertNotEquals(a1, b1);
        Assert.assertNotEquals(a1, long1);

        Assert.assertEquals(long1, long2);
        Assert.assertEquals(long1.hashCode(), long2.hashCode());
        Assert.assertNotEquals(long1, long3);
        Assert.assertNotEquals(long1, a1);

        Option nullBoth1 = new Option(null, null, false, "desc");
        Option nullBoth2 = new Option(null, null, true, "desc2");
        Assert.assertEquals(nullBoth1, nullBoth2);
        Assert.assertEquals(nullBoth1.hashCode(), nullBoth2.hashCode());
        Assert.assertNotEquals(nullBoth1, a1);
        Assert.assertNotEquals(nullBoth1, long1);
    }

    @Test
    public void testClone()
    {
        Option opt = new Option("o", "opt", true, "desc");
        opt.setType(Integer.class);
        opt.addValueForProcessing("v1");

        Option cloned = (Option) opt.clone();
        Assert.assertNotSame(opt, cloned);
        Assert.assertEquals(opt, cloned);
        Assert.assertEquals(opt.getType(), cloned.getType());
        Assert.assertEquals(opt.getValue(), cloned.getValue());

        cloned.clearValues();
        Assert.assertNull(cloned.getValue());
        Assert.assertEquals("v1", opt.getValue());
    }

    @Test
    public void testSerialization() throws Exception
    {
        Option opt = new Option("o", "opt", true, "desc");
        opt.setOptionalArg(true);
        opt.setRequired(true);
        opt.setValueSeparator(':');
        opt.setArgName("testArg");
        opt.addValueForProcessing("val1:val2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(opt);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Option deserialized = (Option) ois.readObject();
        ois.close();

        Assert.assertEquals(opt, deserialized);
        Assert.assertEquals(opt.getOpt(), deserialized.getOpt());
        Assert.assertEquals(opt.getLongOpt(), deserialized.getLongOpt());
        Assert.assertEquals(opt.getDescription(), deserialized.getDescription());
        Assert.assertEquals(opt.isRequired(), deserialized.isRequired());
        Assert.assertEquals(opt.hasOptionalArg(), deserialized.hasOptionalArg());
        Assert.assertEquals(opt.getValueSeparator(), deserialized.getValueSeparator());
        Assert.assertEquals(opt.getArgName(), deserialized.getArgName());
        Assert.assertArrayEquals(opt.getValues(), deserialized.getValues());
    }
}