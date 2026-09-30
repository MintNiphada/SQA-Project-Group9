package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TargetClassTest {

    @Before
    public void setUp() {
        // Reset the builder state before each test by creating a dummy option
        try {
            OptionBuilder.withLongOpt("cleanup").create();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    public void testCompleteOptionWithChar() {
        Option opt = OptionBuilder.withLongOpt("simple-option")
                .withDescription("this is a simple option")
                .hasArg()
                .isRequired()
                .withValueSeparator(':')
                .withType(String.class)
                .withArgName("filename")
                .create('s');

        assertEquals("s", opt.getOpt());
        assertEquals("simple-option", opt.getLongOpt());
        assertEquals("this is a simple option", opt.getDescription());
        assertEquals("filename", opt.getArgName());
        assertEquals(String.class, opt.getType());
        assertTrue(opt.isRequired());
        assertTrue(opt.hasArg());
        assertEquals(1, opt.getArgs());
        assertFalse(opt.hasOptionalArg());
        assertTrue(opt.hasValueSeparator());
        assertEquals(':', opt.getValueSeparator());
    }

    @Test
    public void testCompleteOptionWithString() {
        Option opt = OptionBuilder.withLongOpt("complex-option")
                .withDescription("complex description")
                .hasArgs(3)
                .isRequired(true)
                .withValueSeparator('=')
                .withType(Integer.class)
                .withArgName("number")
                .create("opt");

        assertEquals("opt", opt.getOpt());
        assertEquals("complex-option", opt.getLongOpt());
        assertEquals("complex description", opt.getDescription());
        assertEquals("number", opt.getArgName());
        assertEquals(Integer.class, opt.getType());
        assertTrue(opt.isRequired());
        assertTrue(opt.hasArgs());
        assertEquals(3, opt.getArgs());
        assertFalse(opt.hasOptionalArg());
        assertTrue(opt.hasValueSeparator());
        assertEquals('=', opt.getValueSeparator());
    }

    @Test
    public void testCreateWithLongOptOnly() {
        Option opt = OptionBuilder.withLongOpt("only-long")
                .withDescription("only long opt")
                .create();

        assertNull(opt.getOpt());
        assertEquals("only-long", opt.getLongOpt());
        assertEquals("only long opt", opt.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithoutLongOptThrowsException() {
        OptionBuilder.withDescription("missing longopt").create();
    }

    @Test
    public void testHasArgBoolean() {
        Option optWithArg = OptionBuilder.withLongOpt("opt1")
                .hasArg(true)
                .create('a');
        assertTrue(optWithArg.hasArg());
        assertEquals(1, optWithArg.getArgs());

        Option optWithoutArg = OptionBuilder.withLongOpt("opt2")
                .hasArg(false)
                .create('b');
        assertFalse(optWithoutArg.hasArg());
        assertEquals(Option.UNINITIALIZED, optWithoutArg.getArgs());
    }

    @Test
    public void testIsRequiredBoolean() {
        Option optRequired = OptionBuilder.withLongOpt("req")
                .isRequired(true)
                .create('r');
        assertTrue(optRequired.isRequired());

        Option optNotRequired = OptionBuilder.withLongOpt("not-req")
                .isRequired(false)
                .create('n');
        assertFalse(optNotRequired.isRequired());
    }

    @Test
    public void testHasArgsUnlimited() {
        Option opt = OptionBuilder.withLongOpt("unlimited")
                .hasArgs()
                .create('u');

        assertTrue(opt.hasArgs());
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test
    public void testHasOptionalArg() {
        Option opt = OptionBuilder.withLongOpt("optional")
                .hasOptionalArg()
                .create('o');

        assertTrue(opt.hasArg());
        assertTrue(opt.hasOptionalArg());
        assertEquals(1, opt.getArgs());
    }

    @Test
    public void testHasOptionalArgsUnlimited() {
        Option opt = OptionBuilder.withLongOpt("opt-unlimited")
                .hasOptionalArgs()
                .create('x');

        assertTrue(opt.hasArgs());
        assertTrue(opt.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test
    public void testHasOptionalArgsFixedNumber() {
        Option opt = OptionBuilder.withLongOpt("opt-fixed")
                .hasOptionalArgs(5)
                .create('f');

        assertTrue(opt.hasArgs());
        assertTrue(opt.hasOptionalArg());
        assertEquals(5, opt.getArgs());
    }

    @Test
    public void testDefaultValueSeparator() {
        Option opt = OptionBuilder.withLongOpt("default-sep")
                .withValueSeparator()
                .create('d');

        assertTrue(opt.hasValueSeparator());
        assertEquals('=', opt.getValueSeparator());
    }

    @Test
    public void testBuilderResetAfterCreate() {
        Option opt1 = OptionBuilder.withLongOpt("first")
                .withDescription("first option")
                .isRequired()
                .hasArgs(2)
                .withValueSeparator(':')
                .withArgName("arg1")
                .withType(Double.class)
                .create('1');

        assertNotNull(opt1);

        Option opt2 = OptionBuilder.create('2');

        assertNull(opt2.getLongOpt());
        assertNull(opt2.getDescription());
        assertFalse(opt2.isRequired());
        assertEquals(Option.UNINITIALIZED, opt2.getArgs());
        assertFalse(opt2.hasValueSeparator());
        assertEquals("arg", opt2.getArgName());
        assertNull(opt2.getType());
        assertFalse(opt2.hasOptionalArg());
    }

    @Test
    public void testBuilderResetAfterExceptionInCreate() {
        try {
            OptionBuilder.withDescription("desc").isRequired().create();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("must specify longopt", e.getMessage());
        }

        Option opt = OptionBuilder.create('z');
        assertNull(opt.getLongOpt());
        assertNull(opt.getDescription());
        assertFalse(opt.isRequired());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharOption() {
        OptionBuilder.create('?');
    }
}