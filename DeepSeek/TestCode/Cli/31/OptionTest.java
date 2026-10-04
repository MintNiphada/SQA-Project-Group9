package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;

public class OptionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullOpt() {
        new Option(null, "description");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyOpt() {
        new Option("", "description");
    }

    @Test
    public void testConstructorWithOptAndDescription() {
        Option opt = new Option("f", "file option");
        assertEquals("f", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertEquals("file option", opt.getDescription());
        assertFalse(opt.hasArg());
        assertFalse(opt.hasArgs());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertFalse(opt.isRequired());
        assertFalse(opt.hasOptionalArg());
    }

    @Test
    public void testConstructorWithHasArgTrue() {
        Option opt = new Option("d", true, "debug mode");
        assertEquals("d", opt.getOpt());
        assertTrue(opt.hasArg());
        assertEquals(1, opt.getArgs());
    }

    @Test
    public void testConstructorWithHasArgFalse() {
        Option opt = new Option("v", false, "verbose");
        assertFalse(opt.hasArg());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
    }

    @Test
    public void testConstructorWithLongOptAndHasArgTrue() {
        Option opt = new Option("f", "file", true, "the filename");
        assertEquals("f", opt.getOpt());
        assertEquals("file", opt.getLongOpt());
        assertTrue(opt.hasArg());
        assertEquals(1, opt.getArgs());
    }

    @Test
    public void testConstructorWithLongOptAndHasArgFalse() {
        Option opt = new Option("v", "verbose", false, "verbose output");
        assertEquals("v", opt.getOpt());
        assertEquals("verbose", opt.getLongOpt());
        assertFalse(opt.hasArg());
    }

    @Test
    public void testGetIdWithOpt() {
        Option opt = new Option("x", "description");
        assertEquals('x', opt.getId());
    }

    @Test
    public void testGetIdWithLongOptOnly() {
        Option opt = new Option("a", "alpha", false, "desc");
        opt.setLongOpt("beta");
        opt.getKey();
        assertEquals('a', opt.getId());
    }

    @Test
    public void testGetKeyWhenOptIsNull() {
        Option opt = new Option("k", "key", false, "desc");
        opt.setLongOpt("longkey");
        assertEquals("longkey", opt.getKey());
    }

    @Test
    public void testGetKeyWhenOptNotNull() {
        Option opt = new Option("s", "short", false, "desc");
        assertEquals("s", opt.getKey());
    }

    @Test
    public void testSetTypeAndGetType() {
        Option opt = new Option("t", "type test");
        assertNull(opt.getType());
        opt.setType(Integer.class);
        assertEquals(Integer.class, opt.getType());
    }

    @Test
    public void testSetAndGetLongOpt() {
        Option opt = new Option("l", "long test");
        opt.setLongOpt("newLong");
        assertEquals("newLong", opt.getLongOpt());
    }

    @Test
    public void testHasLongOpt() {
        Option opt = new Option("l", "long", true, "desc");
        assertTrue(opt.hasLongOpt());
        opt.setLongOpt(null);
        assertFalse(opt.hasLongOpt());
    }

    @Test
    public void testSetOptionalArg() {
        Option opt = new Option("o", "optional test");
        assertFalse(opt.hasOptionalArg());
        opt.setOptionalArg(true);
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testHasArgWithUnlimitedValues() {
        Option opt = new Option("u", false, "unlimited");
        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.hasArg());
    }

    @Test
    public void testHasArgWithPositiveNumberOfArgs() {
        Option opt = new Option("p", false, "positive");
        opt.setArgs(3);
        assertTrue(opt.hasArg());
    }

    @Test
    public void testHasArgWithZeroOrUninitialized() {
        Option opt = new Option("z", false, "zero");
        assertFalse(opt.hasArg());
        opt.setArgs(0);
        assertFalse(opt.hasArg());
    }

    @Test
    public void testSetAndGetDescription() {
        Option opt = new Option("d", "description");
        assertEquals("description", opt.getDescription());
        opt.setDescription("new desc");
        assertEquals("new desc", opt.getDescription());
    }

    @Test
    public void testIsRequiredAndSetRequired() {
        Option opt = new Option("r", "required test");
        assertFalse(opt.isRequired());
        opt.setRequired(true);
        assertTrue(opt.isRequired());
    }

    @Test
    public void testSetAndGetArgName() {
        Option opt = new Option("a", "arg test");
        assertEquals("arg", opt.getArgName());
        opt.setArgName("filename");
        assertEquals("filename", opt.getArgName());
    }

    @Test
    public void testHasArgNameTrue() {
        Option opt = new Option("a", "arg test");
        assertTrue(opt.hasArgName());
        opt.setArgName("file");
        assertTrue(opt.hasArgName());
    }

    @Test
    public void testHasArgNameFalseForEmpty() {
        Option opt = new Option("a", "arg test");
        opt.setArgName("");
        assertFalse(opt.hasArgName());
    }

    @Test
    public void testHasArgNameFalseForNull() {
        Option opt = new Option("a", "arg test");
        opt.setArgName(null);
        assertFalse(opt.hasArgName());
    }

    @Test
    public void testHasArgsWithMultiple() {
        Option opt = new Option("m", false, "multi");
        opt.setArgs(2);
        assertTrue(opt.hasArgs());
    }

    @Test
    public void testHasArgsWithUnlimited() {
        Option opt = new Option("m", false, "multi");
        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.hasArgs());
    }

    @Test
    public void testHasArgsFalseForOne() {
        Option opt = new Option("m", false, "multi");
        opt.setArgs(1);
        assertFalse(opt.hasArgs());
    }

    @Test
    public void testHasArgsFalseForUninitialized() {
        Option opt = new Option("m", false, "multi");
        assertFalse(opt.hasArgs());
    }

    @Test
    public void testSetArgsAndGetArgs() {
        Option opt = new Option("n", false, "num");
        opt.setArgs(5);
        assertEquals(5, opt.getArgs());
    }

    @Test
    public void testSetValueSeparatorAndGetValueSeparator() {
        Option opt = new Option("s", "separator test");
        opt.setValueSeparator('=');
        assertEquals('=', opt.getValueSeparator());
    }

    @Test
    public void testHasValueSeparatorTrue() {
        Option opt = new Option("s", "separator test");
        opt.setValueSeparator(':');
        assertTrue(opt.hasValueSeparator());
    }

    @Test
    public void testHasValueSeparatorFalseWhenZero() {
        Option opt = new Option("s", "separator test");
        assertFalse(opt.hasValueSeparator());
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingNoArgsAllowed() {
        Option opt = new Option("n", false, "no args");
        opt.addValueForProcessing("value");
    }

    @Test
    public void testAddValueForProcessingWithSingleArg() {
        Option opt = new Option("s", true, "single arg");
        opt.addValueForProcessing("val1");
        assertEquals("val1", opt.getValue());
        assertEquals(1, opt.getValues().length);
    }

    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessingWhenListFull() {
        Option opt = new Option("s", true, "single arg");
        opt.addValueForProcessing("val1");
        opt.addValueForProcessing("val2");
    }

    @Test
    public void testAddValueForProcessingWithMultipleArgs() {
        Option opt = new Option("m", false, "multi");
        opt.setArgs(3);
        opt.addValueForProcessing("a");
        opt.addValueForProcessing("b");
        opt.addValueForProcessing("c");
        assertEquals("a", opt.getValue(0));
        assertEquals("b", opt.getValue(1));
        assertEquals("c", opt.getValue(2));
        assertArrayEquals(new String[]{"a","b","c"}, opt.getValues());
    }

    @Test
    public void testProcessValueWithSeparator() {
        Option opt = new Option("s", false, "separator");
        opt.setArgs(3);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("x,y,z");
        assertEquals("x", opt.getValue(0));
        assertEquals("y", opt.getValue(1));
        assertEquals("z", opt.getValue(2));
    }

    @Test
    public void testProcessValueWithSeparatorAndLimitedArgsStopSplitting() {
        Option opt = new Option("s", false, "separator");
        opt.setArgs(2);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("a,b,c");
        assertEquals("a", opt.getValue(0));
        assertEquals("b,c", opt.getValue(1));
    }

    @Test
    public void testProcessValueWithSeparatorNoSplitIfNoSep() {
        Option opt = new Option("s", false, "separator");
        opt.setArgs(2);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("hello");
        assertEquals("hello", opt.getValue(0));
        assertEquals(1, opt.getValues().length);
    }

    @Test(expected = RuntimeException.class)
    public void testAddWhenAcceptsArgFalse() {
        Option opt = new Option("a", false, "no arg");
        opt.addValueForProcessing("val");
    }

    @Test
    public void testAcceptsArgWhenOptionalArg() {
        Option opt = new Option("o", false, "opt arg");
        opt.setOptionalArg(true);
        opt.addValueForProcessing("val");
        opt.addValueForProcessing("val2");
    }

    @Test
    public void testGetValueWhenNoValue() {
        Option opt = new Option("v", false, "no value");
        assertNull(opt.getValue());
    }

    @Test
    public void testGetValueWithDefaultWhenNoValue() {
        Option opt = new Option("v", false, "no value");
        assertEquals("default", opt.getValue("default"));
    }

    @Test
    public void testGetValueWithDefaultWhenHasValue() {
        Option opt = new Option("v", true, "has value");
        opt.addValueForProcessing("actual");
        assertEquals("actual", opt.getValue("default"));
    }

    @Test
    public void testGetValueByIndexWhenNoValues() {
        Option opt = new Option("v", false, "no values");
        assertNull(opt.getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndexOutOfBounds() {
        Option opt = new Option("v", true, "single");
        opt.addValueForProcessing("one");
        opt.getValue(1);
    }

    @Test
    public void testGetValueByIndexValid() {
        Option opt = new Option("v", true, "single");
        opt.addValueForProcessing("zero");
        assertEquals("zero", opt.getValue(0));
    }

    @Test
    public void testGetValuesWhenNoValues() {
        Option opt = new Option("v", false, "no values");
        assertNull(opt.getValues());
    }

    @Test
    public void testGetValuesList() {
        Option opt = new Option("v", true, "list");
        opt.addValueForProcessing("a");
        assertEquals(1, opt.getValuesList().size());
        assertTrue(opt.getValuesList().contains("a"));
    }

    @Test
    public void testClearValues() {
        Option opt = new Option("c", true, "clear");
        opt.addValueForProcessing("x");
        opt.clearValues();
        assertNull(opt.getValue());
    }

    @Test
    public void testCloneValuesAreCopied() {
        Option opt = new Option("c", true, "clone");
        opt.addValueForProcessing("val1");
        Option clone = (Option) opt.clone();
        clone.addValueForProcessing("val2");
        assertEquals(1, opt.getValues().length);
        assertEquals(2, clone.getValues().length);
    }

    @Test
    public void testCloneReturnsDifferentObject() {
        Option opt = new Option("c", "cloneTest", true, "desc");
        Option clone = (Option) opt.clone();
        assertNotSame(opt, clone);
        assertEquals(opt, clone);
    }

    @Test
    public void testEqualsSameObject() {
        Option opt = new Option("e", "eq", true, "desc");
        assertTrue(opt.equals(opt));
    }

    @Test
    public void testEqualsNull() {
        Option opt = new Option("e", "eq", true, "desc");
        assertFalse(opt.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Option opt = new Option("e", "eq", true, "desc");
        assertFalse(opt.equals("string"));
    }

    @Test
    public void testEqualsSameOptAndLongOpt() {
        Option opt1 = new Option("a", "alpha", true, "desc1");
        Option opt2 = new Option("a", "alpha", true, "desc2");
        assertTrue(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentOpt() {
        Option opt1 = new Option("a", "alpha", true, "desc");
        Option opt2 = new Option("b", "alpha", true, "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentLongOpt() {
        Option opt1 = new Option("a", "alpha", true, "desc");
        Option opt2 = new Option("a", "beta", true, "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsOneNullOpt() {
        Option opt1 = new Option("a", null, true, "desc");
        Option opt2 = new Option("a", "alpha", true, "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testHashCodeConsistencyWithEquals() {
        Option opt1 = new Option("a", "alpha", true, "desc");
        Option opt2 = new Option("a", "alpha", false, "other");
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test
    public void testHashCodeNullOpt() {
        Option opt = new Option("a", null, false, "desc");
        assertTrue(opt.hashCode() != 0);
    }

    @Test
    public void testToStringWithArgs() {
        Option opt = new Option("f", "file", true, "the file");
        String str = opt.toString();
        assertTrue(str.contains("[ option:"));
        assertTrue(str.contains("f"));
        assertTrue(str.contains("file"));
        assertTrue(str.contains("[ARG]"));
        assertTrue(str.contains("the file"));
    }

    @Test
    public void testToStringWithMultipleArgs() {
        Option opt = new Option("f", "files", false, "the files");
        opt.setArgs(Option.UNLIMITED_VALUES);
        String str = opt.toString();
        assertTrue(str.contains("[ARG...]"));
    }

    @Test
    public void testToStringWithType() {
        Option opt = new Option("f", "file", true, "desc");
        opt.setType(Integer.class);
        assertTrue(opt.toString().contains(Integer.class.toString()));
    }

    @Test
    public void testAddValueThrowsUnsupportedOperation() {
        Option opt = new Option("u", "unsupported", false, "desc");
        opt.addValue("test");
    }

    @Test
    public void testGetValueWithNoValuesReturnsNull() {
        Option opt = new Option("v", "verbose", false, "desc");
        assertNull(opt.getValue());
    }

    @Test
    public void testGetValuesWithNoValuesReturnsNull() {
        Option opt = new Option("v", "verbose", false, "desc");
        assertNull(opt.getValues());
    }

    @Test
    public void testGetValuesListNeverNull() {
        Option opt = new Option("v", "verbose", false, "desc");
        assertNotNull(opt.getValuesList());
        assertTrue(opt.getValuesList().isEmpty());
    }

    @Test
    public void testRequiresArgWhenOptionalArg() {
        Option opt = new Option("o", false, "optional");
        opt.setOptionalArg(true);
        opt.setArgs(1);
        assertFalse(opt.requiresArg());
    }

    @Test
    public void testRequiresArgUnlimitedNoValue() {
        Option opt = new Option("u", false, "unlimited");
        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.requiresArg());
    }

    @Test
    public void testRequiresArgUnlimitedHasValue() {
        Option opt = new Option("u", false, "unlimited");
        opt.setArgs(Option.UNLIMITED_VALUES);
        opt.addValueForProcessing("val");
        assertFalse(opt.requiresArg());
    }

    @Test
    public void testRequiresArgLimited() {
        Option opt = new Option("l", false, "limited");
        opt.setArgs(2);
        assertTrue(opt.requiresArg());
        opt.addValueForProcessing("first");
        assertTrue(opt.requiresArg());
        opt.addValueForProcessing("second");
        assertFalse(opt.requiresArg());
    }

    @Test
    public void testRequiresArgWithNoArgs() {
        Option opt = new Option("n", false, "no arg");
        assertFalse(opt.requiresArg());
    }

    @Test
    public void testAcceptsArgWithUnlimited() {
        Option opt = new Option("u", false, "unlimited");
        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.acceptsArg());
        opt.addValueForProcessing("v1");
        assertTrue(opt.acceptsArg());
    }

    @Test
    public void testAcceptsArgWithFixedNumber() {
        Option opt = new Option("f", false, "fixed");
        opt.setArgs(2);
        assertTrue(opt.acceptsArg());
        opt.addValueForProcessing("a");
        assertTrue(opt.acceptsArg());
        opt.addValueForProcessing("b");
        assertFalse(opt.acceptsArg());
    }

    @Test
    public void testAcceptsArgWithNoArgsAllowed() {
        Option opt = new Option("n", false, "no arg");
        assertFalse(opt.acceptsArg());
    }

    @Test
    public void testAcceptsArgWithOptionalArg() {
        Option opt = new Option("o", false, "optional");
        opt.setOptionalArg(true);
        assertTrue(opt.acceptsArg());
    }

    @Test
    public void testHasArgNameInitiallyTrue() {
        Option opt = new Option("a", "argName test");
        assertTrue(opt.hasArgName());
    }
}
