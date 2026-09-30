package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommandLineTest {

    private CommandLine cmd;

    @Before
    public void setUp() {
        cmd = new CommandLine();
    }

    @Test
    public void testEmptyCommandLine() {
        assertFalse(cmd.hasOption("a"));
        assertFalse(cmd.hasOption('a'));
        assertNull(cmd.getOptionValue("a"));
        assertNull(cmd.getOptionValue('a'));
        assertEquals("default", cmd.getOptionValue("a", "default"));
        assertEquals("default", cmd.getOptionValue('a', "default"));
        assertNull(cmd.getOptionValues("a"));
        assertNull(cmd.getOptionValues('a'));
        assertNull(cmd.getOptionObject("a"));
        assertNull(cmd.getOptionObject('a'));

        assertEquals(0, cmd.getArgs().length);
        assertNotNull(cmd.getArgList());
        assertEquals(0, cmd.getArgList().size());
        assertEquals(0, cmd.getOptions().length);

        Iterator iterator = cmd.iterator();
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testAddAndGetArgs() {
        cmd.addArg("arg1");
        cmd.addArg("arg2");

        String[] args = cmd.getArgs();
        assertEquals(2, args.length);
        assertEquals("arg1", args[0]);
        assertEquals("arg2", args[1]);

        List argList = cmd.getArgList();
        assertEquals(2, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg2", argList.get(1));
    }

    @Test
    public void testHasOption() {
        Option optA = OptionBuilder.hasArg().create('a');
        cmd.addOption(optA);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption('a'));
        assertFalse(cmd.hasOption("b"));
        assertFalse(cmd.hasOption('b'));
    }

    @Test
    public void testGetOptionValueSingle() {
        Option optA = OptionBuilder.hasArg().create('a');
        optA.addValue("valueA");
        cmd.addOption(optA);

        assertEquals("valueA", cmd.getOptionValue("a"));
        assertEquals("valueA", cmd.getOptionValue('a'));
        assertEquals("valueA", cmd.getOptionValue("a", "default"));
        assertEquals("valueA", cmd.getOptionValue('a', "default"));

        assertEquals("default", cmd.getOptionValue("b", "default"));
        assertEquals("default", cmd.getOptionValue('b', "default"));
    }

    @Test
    public void testGetOptionValuesMultiple() {
        Option optM = OptionBuilder.hasArgs(3).create('m');
        optM.addValue("val1");
        optM.addValue("val2");
        optM.addValue("val3");
        cmd.addOption(optM);

        String[] valuesStr = cmd.getOptionValues("m");
        assertNotNull(valuesStr);
        assertEquals(3, valuesStr.length);
        assertArrayEquals(new String[]{"val1", "val2", "val3"}, valuesStr);

        String[] valuesChar = cmd.getOptionValues('m');
        assertNotNull(valuesChar);
        assertEquals(3, valuesChar.length);
        assertArrayEquals(new String[]{"val1", "val2", "val3"}, valuesChar);

        assertEquals("val1", cmd.getOptionValue("m"));
        assertEquals("val1", cmd.getOptionValue('m'));
    }

    @Test
    public void testGetOptionValuesWithHyphens() {
        Option optLong = OptionBuilder.withLongOpt("long-opt").hasArg().create('l');
        optLong.addValue("hyphenValue");
        cmd.addOption(optLong);

        assertEquals("hyphenValue", cmd.getOptionValue("-l"));
        assertEquals("hyphenValue", cmd.getOptionValue("--long-opt"));
        assertEquals("hyphenValue", cmd.getOptionValue("long-opt"));
        assertEquals("hyphenValue", cmd.getOptionValue("l"));
        assertArrayEquals(new String[]{"hyphenValue"}, cmd.getOptionValues("--long-opt"));
    }

    @Test
    public void testLongOptionOnly() {
        Option optOnlyLong = OptionBuilder.withLongOpt("only-long").hasArg().create();
        optOnlyLong.addValue("onlyLongVal");
        cmd.addOption(optOnlyLong);

        assertTrue(cmd.hasOption("only-long"));
        assertEquals("onlyLongVal", cmd.getOptionValue("only-long"));
        assertEquals("onlyLongVal", cmd.getOptionValue("--only-long"));
        assertArrayEquals(new String[]{"onlyLongVal"}, cmd.getOptionValues("only-long"));
    }

    @Test
    public void testGetOptionObject() {
        Option optClass = OptionBuilder.withType(PatternOptionBuilder.CLASS_VALUE).hasArg().create('c');
        optClass.addValue("java.lang.String");
        cmd.addOption(optClass);

        Object obj = cmd.getOptionObject("c");
        assertNotNull(obj);
        assertEquals(String.class, obj);

        Object objChar = cmd.getOptionObject('c');
        assertNotNull(objChar);
        assertEquals(String.class, objChar);

        assertNull(cmd.getOptionObject("nonexistent"));
        assertNull(cmd.getOptionObject('n'));
    }

    @Test
    public void testGetOptionObjectWithoutValue() {
        Option optNoVal = OptionBuilder.withType(PatternOptionBuilder.CLASS_VALUE).hasArg().create('x');
        cmd.addOption(optNoVal);

        assertNull(cmd.getOptionObject("x"));
        assertNull(cmd.getOptionObject('x'));
    }

    @Test
    public void testGetOptionObjectWithoutType() {
        Option optNoType = OptionBuilder.hasArg().create('u');
        optNoType.addValue("someValue");
        cmd.addOption(optNoType);

        Object obj = cmd.getOptionObject("u");
        assertNull(obj);
    }

    @Test
    public void testGetOptionsAndIterator() {
        Option optA = OptionBuilder.hasArg().create('a');
        Option optB = OptionBuilder.hasArg().create('b');
        cmd.addOption(optA);
        cmd.addOption(optB);

        Option[] options = cmd.getOptions();
        assertNotNull(options);
        assertEquals(2, options.length);

        Iterator iterator = cmd.iterator();
        assertNotNull(iterator);
        int count = 0;
        while (iterator.hasNext()) {
            assertNotNull(iterator.next());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testOptionWithNoArgumentsSet() {
        Option optFlag = OptionBuilder.create('f');
        cmd.addOption(optFlag);

        assertTrue(cmd.hasOption("f"));
        assertTrue(cmd.hasOption('f'));
        assertNull(cmd.getOptionValue("f"));
        assertNull(cmd.getOptionValue('f'));
        assertNull(cmd.getOptionValues("f"));
        assertNull(cmd.getOptionValues('f'));
        assertEquals("fallback", cmd.getOptionValue("f", "fallback"));
        assertEquals("fallback", cmd.getOptionValue('f', "fallback"));
    }
}