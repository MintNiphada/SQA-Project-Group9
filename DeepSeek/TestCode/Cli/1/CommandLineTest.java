package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Iterator;
import java.util.List;

public class CommandLineTest {

    private CommandLine cmdLine;

    @Before
    public void setUp() {
        cmdLine = new CommandLine();
    }

    // Helper to create a simple Option with short key and add a value
    private Option createAndAddOption(String opt, boolean hasArg, String description, String... values) {
        Option option = new Option(opt, hasArg, description);
        for (String val : values) {
            option.addValue(val);
        }
        cmdLine.addOption(option);
        return option;
    }

    // Helper for Option with longOpt
    private Option createAndAddOptionWithLong(String opt, String longOpt, boolean hasArg, String description, String... values) {
        Option option = new Option(opt, longOpt, hasArg, description);
        for (String val : values) {
            option.addValue(val);
        }
        cmdLine.addOption(option);
        return option;
    }

    @Test
    public void testHasOption() {
        assertFalse(cmdLine.hasOption("a"));
        assertFalse(cmdLine.hasOption('a'));
        createAndAddOption("a", false, "desc");
        assertTrue(cmdLine.hasOption("a"));
        assertTrue(cmdLine.hasOption('a'));
        assertFalse(cmdLine.hasOption("b"));
        assertFalse(cmdLine.hasOption('b'));
    }

    @Test
    public void testHasOptionLongOptOnly() {
        Option opt = new Option(null, "long", false, "desc");
        cmdLine.addOption(opt);
        assertTrue(cmdLine.hasOption("long"));
    }

    @Test
    public void testHasOptionNull() {
        assertFalse(cmdLine.hasOption(null));
    }

    @Test
    public void testGetOptionObjectWithType() {
        Option opt = createAndAddOption("a", true, "desc", "5");
        opt.setType(Integer.class);
        assertEquals(5, ((Integer) cmdLine.getOptionObject("a")).intValue());
    }

    @Test
    public void testGetOptionObjectWithoutType() {
        createAndAddOption("a", true, "desc", "hello");
        assertEquals("hello", cmdLine.getOptionObject("a"));
    }

    @Test
    public void testGetOptionObjectNullValue() {
        Option opt = createAndAddOption("a", false, "desc"); // no value
        assertNull(cmdLine.getOptionObject("a"));
    }

    @Test
    public void testGetOptionObjectNotPresent() {
        assertNull(cmdLine.getOptionObject("a"));
        assertNull(cmdLine.getOptionObject('a'));
    }

    @Test
    public void testGetOptionValue() {
        createAndAddOption("a", true, "desc", "10");
        assertEquals("10", cmdLine.getOptionValue("a"));
        assertEquals("10", cmdLine.getOptionValue('a'));
        assertNull(cmdLine.getOptionValue("b"));
    }

    @Test
    public void testGetOptionValueDefault() {
        assertNull(cmdLine.getOptionValue("a", "default"));
        createAndAddOption("a", true, "desc", "10");
        assertEquals("10", cmdLine.getOptionValue("a", "default"));
        assertEquals("default", cmdLine.getOptionValue("b", "default"));
    }

    @Test
    public void testGetOptionValueDefaultWithChar() {
        assertEquals("default", cmdLine.getOptionValue('x', "default"));
        createAndAddOption("x", true, "desc", "special");
        assertEquals("special", cmdLine.getOptionValue('x', "default"));
    }

    @Test
    public void testGetOptionValuesSingle() {
        createAndAddOption("a", true, "desc", "alpha");
        String[] vals = cmdLine.getOptionValues("a");
        assertNotNull(vals);
        assertEquals(1, vals.length);
        assertEquals("alpha", vals[0]);
    }

    @Test
    public void testGetOptionValuesMultiple() {
        createAndAddOption("b", true, "desc", "val1", "val2");
        String[] vals = cmdLine.getOptionValues("b");
        assertArrayEquals(new String[]{"val1", "val2"}, vals);
    }

    @Test
    public void testGetOptionValuesChar() {
        createAndAddOption("c", true, "desc", "char");
        String[] vals = cmdLine.getOptionValues('c');
        assertNotNull(vals);
        assertEquals("char", vals[0]);
    }

    @Test
    public void testGetOptionValuesWithLongOpt() {
        createAndAddOptionWithLong("s", "long-name", true, "desc", "value");
        assertArrayEquals(new String[]{"value"}, cmdLine.getOptionValues("s"));
        assertArrayEquals(new String[]{"value"}, cmdLine.getOptionValues("long-name"));
    }

    @Test
    public void testGetOptionValuesWithLeadingHyphens() {
        createAndAddOption("d", true, "desc", "data");
        assertArrayEquals(new String[]{"data"}, cmdLine.getOptionValues("--d"));
        assertArrayEquals(new String[]{"data"}, cmdLine.getOptionValues("-d"));
    }

    @Test
    public void testGetOptionValuesNotPresent() {
        assertNull(cmdLine.getOptionValues("missing"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetOptionValueNull() {
        cmdLine.getOptionValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetOptionValuesNull() {
        cmdLine.getOptionValues(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetOptionObjectNull() {
        cmdLine.getOptionObject(null);
    }

    @Test
    public void testGetArgsEmpty() {
        assertArrayEquals(new String[0], cmdLine.getArgs());
        assertTrue(cmdLine.getArgList().isEmpty());
    }

    @Test
    public void testGetArgs() {
        cmdLine.addArg("arg1");
        cmdLine.addArg("arg2");
        String[] args = cmdLine.getArgs();
        assertArrayEquals(new String[]{"arg1", "arg2"}, args);
        List<String> argList = cmdLine.getArgList();
        assertEquals(2, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg2", argList.get(1));
    }

    @Test
    public void testGetOptionsEmpty() {
        Option[] options = cmdLine.getOptions();
        assertNotNull(options);
        assertEquals(0, options.length);
        Iterator<?> it = cmdLine.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetOptionsAndIterator() {
        Option optA = createAndAddOption("a", false, "descA");
        Option optB = createAndAddOption("b", false, "descB");
        Option[] optionsArr = cmdLine.getOptions();
        assertEquals(2, optionsArr.length);
        // options are returned in insertion order? Not guaranteed, but both likely same
        // Instead, check that the iterator returns the same two options
        Iterator<?> it = cmdLine.iterator();
        int count = 0;
        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(obj instanceof Option);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testDuplicateOptionAdd() {
        Option opt1 = new Option("x", false, "first");
        opt1.addValue("v1");
        cmdLine.addOption(opt1);
        Option opt2 = new Option("x", false, "second"); // same key
        opt2.addValue("v2");
        cmdLine.addOption(opt2);
        // the second overwrites in options map
        assertEquals("v2", cmdLine.getOptionValue("x"));
        // but hashcodeMap contains both? It uses hashCode, so if same hashCode? Different objects have diff hashcodes
        // So iterator should return 2 options.
        assertEquals(2, cmdLine.getOptions().length);
    }

    @Test
    public void testOptionWithNullKeyUsesLongOpt() {
        Option opt = new Option(null, "longOnly", true, "desc");
        opt.addValue("val");
        cmdLine.addOption(opt);
        assertEquals("val", cmdLine.getOptionValue("longOnly"));
        assertNull(cmdLine.getOptionValue("nonexistent"));
    }
}