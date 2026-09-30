package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

public class TargetClassTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    public void testCompleteOptionWithChar() {
        Option option = OptionBuilder.withLongOpt("simple-option")
                                     .withDescription("this is a simple option")
                                     .withArgName("arg")
                                     .isRequired()
                                     .hasArg()
                                     .withType(String.class)
                                     .withValueSeparator(':')
                                     .create('s');

        assertEquals("s", option.getOpt());
        assertEquals("simple-option", option.getLongOpt());
        assertEquals("this is a simple option", option.getDescription());
        assertEquals("arg", option.getArgName());
        assertTrue(option.isRequired());
        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
        assertEquals(String.class, option.getType());
        assertEquals(':', option.getValueSeparator());
        assertFalse(option.hasOptionalArg());
    }

    @Test
    public void testCompleteOptionWithString() {
        Option option = OptionBuilder.withLongOpt("multi-arg")
                                     .withDescription("option with multiple args")
                                     .withArgName("args")
                                     .isRequired(true)
                                     .hasArgs(3)
                                     .withType(Integer.class)
                                     .withValueSeparator()
                                     .create("multi");

        assertEquals("multi", option.getOpt());
        assertEquals("multi-arg", option.getLongOpt());
        assertEquals("option with multiple args", option.getDescription());
        assertEquals("args", option.getArgName());
        assertTrue(option.isRequired());
        assertEquals(3, option.getArgs());
        assertTrue(option.hasArgs());
        assertEquals(Integer.class, option.getType());
        assertEquals('=', option.getValueSeparator());
        assertFalse(option.hasOptionalArg());
    }

    @Test
    public void testCreateWithLongOptOnly() {
        Option option = OptionBuilder.withLongOpt("only-long")
                                     .withDescription("only long opt description")
                                     .create();

        assertNull(option.getOpt());
        assertEquals("only-long", option.getLongOpt());
        assertEquals("only long opt description", option.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithoutLongOptThrows() {
        OptionBuilder.withDescription("description without longOpt").create();
    }

    @Test
    public void testHasArgBoolean() {
        Option optTrue = OptionBuilder.hasArg(true).create('t');
        assertEquals(1, optTrue.getArgs());
        assertTrue(optTrue.hasArg());

        Option optFalse = OptionBuilder.hasArg(false).create('f');
        assertEquals(Option.UNINITIALIZED, optFalse.getArgs());
        assertFalse(optFalse.hasArg());
    }

    @Test
    public void testIsRequiredBoolean() {
        Option optReqTrue = OptionBuilder.isRequired(true).create('r');
        assertTrue(optReqTrue.isRequired());

        Option optReqFalse = OptionBuilder.isRequired(false).create('n');
        assertFalse(optReqFalse.isRequired());
    }

    @Test
    public void testHasArgsUnlimited() {
        Option option = OptionBuilder.hasArgs().create('u');
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasArgs());
    }

    @Test
    public void testHasOptionalArg() {
        Option option = OptionBuilder.hasOptionalArg().create('o');
        assertEquals(1, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgsUnlimited() {
        Option option = OptionBuilder.hasOptionalArgs().create('p');
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgsSpecified() {
        Option option = OptionBuilder.hasOptionalArgs(4).create('q');
        assertEquals(4, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    @Test
    public void testStateResetAfterCreate() {
        OptionBuilder.withLongOpt("temp-long")
                     .withDescription("temp-desc")
                     .withArgName("temp-arg")
                     .isRequired()
                     .hasArgs(2)
                     .withType(Double.class)
                     .withValueSeparator('/')
                     .create('1');

        Option nextOption = OptionBuilder.create('2');

        assertEquals("2", nextOption.getOpt());
        assertNull(nextOption.getLongOpt());
        assertNull(nextOption.getDescription());
        assertNull(nextOption.getArgName());
        assertFalse(nextOption.isRequired());
        assertEquals(Option.UNINITIALIZED, nextOption.getArgs());
        assertNull(nextOption.getType());
        assertEquals((char) 0, nextOption.getValueSeparator());
        assertFalse(nextOption.hasOptionalArg());
    }

    @Test
    public void testStateResetAfterException() {
        try {
            OptionBuilder.withLongOpt("failing-long")
                         .withDescription("failing-desc")
                         .isRequired()
                         .create("invalid opt string with spaces");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        Option nextOption = OptionBuilder.create('z');
        assertEquals("z", nextOption.getOpt());
        assertNull(nextOption.getLongOpt());
        assertNull(nextOption.getDescription());
        assertFalse(nextOption.isRequired());
    }

    @Test
    public void testStateResetAfterCreateNoArgException() {
        try {
            OptionBuilder.withDescription("desc").create();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        Option nextOption = OptionBuilder.create('c');
        assertEquals("c", nextOption.getOpt());
        assertNull(nextOption.getDescription());
    }
}

class OptionBuilderTest extends TargetClassTest {
}