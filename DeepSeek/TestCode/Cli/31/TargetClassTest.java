package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

public class OptionBuilderTest {

    @Before
    public void setUp() throws Exception {
        Method resetMethod = OptionBuilder.class.getDeclaredMethod("reset");
        resetMethod.setAccessible(true);
        resetMethod.invoke(null);
    }

    @Test
    public void testInitialState() {
        Option opt = OptionBuilder.create("x");
        assertNull(opt.getDescription());
        assertEquals("arg", opt.getArgName());
        assertNull(opt.getLongOpt());
        assertFalse(opt.isRequired());
        assertFalse(opt.hasOptionalArg());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertNull(opt.getType());
        assertEquals((char) 0, opt.getValueSeparator());
    }

    @Test
    public void testWithLongOpt() {
        OptionBuilder.withLongOpt("test");
        Option opt = OptionBuilder.create("x");
        assertEquals("test", opt.getLongOpt());
        Option opt2 = OptionBuilder.create("y");
        assertNull(opt2.getLongOpt());
    }

    @Test
    public void testHasArg() {
        OptionBuilder.hasArg();
        Option opt = OptionBuilder.create("x");
        assertEquals(1, opt.getArgs());
    }

    @Test
    public void testHasArgBooleanTrue() {
        OptionBuilder.hasArg(true);
        Option opt = OptionBuilder.create("x");
        assertEquals(1, opt.getArgs());
    }

    @Test
    public void testHasArgBooleanFalse() {
        OptionBuilder.hasArg(false);
        Option opt = OptionBuilder.create("x");
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
    }

    @Test
    public void testWithArgName() {
        OptionBuilder.withArgName("myArg");
        Option opt = OptionBuilder.create("x");
        assertEquals("myArg", opt.getArgName());
    }

    @Test
    public void testIsRequired() {
        OptionBuilder.isRequired();
        Option opt = OptionBuilder.create("x");
        assertTrue(opt.isRequired());
    }

    @Test
    public void testIsRequiredBooleanTrue() {
        OptionBuilder.isRequired(true);
        Option opt = OptionBuilder.create("x");
        assertTrue(opt.isRequired());
    }

    @Test
    public void testIsRequiredBooleanFalse() {
        OptionBuilder.isRequired(false);
        Option opt = OptionBuilder.create("x");
        assertFalse(opt.isRequired());
    }

    @Test
    public void testWithValueSeparatorChar() {
        OptionBuilder.withValueSeparator(':');
        Option opt = OptionBuilder.create("x");
        assertEquals(':', opt.getValueSeparator());
    }

    @Test
    public void testWithValueSeparatorDefault() {
        OptionBuilder.withValueSeparator();
        Option opt = OptionBuilder.create("x");
        assertEquals('=', opt.getValueSeparator());
    }

    @Test
    public void testHasArgs() {
        OptionBuilder.hasArgs();
        Option opt = OptionBuilder.create("x");
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test
    public void testHasArgsNum() {
        OptionBuilder.hasArgs(3);
        Option opt = OptionBuilder.create("x");
        assertEquals(3, opt.getArgs());
    }

    @Test
    public void testHasOptionalArg() {
        OptionBuilder.hasOptionalArg();
        Option opt = OptionBuilder.create("x");
        assertEquals(1, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgs() {
        OptionBuilder.hasOptionalArgs();
        Option opt = OptionBuilder.create("x");
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgsNum() {
        OptionBuilder.hasOptionalArgs(2);
        Option opt = OptionBuilder.create("x");
        assertEquals(2, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testWithType() {
        Object type = new Object();
        OptionBuilder.withType(type);
        Option opt = OptionBuilder.create("x");
        assertSame(type, opt.getType());
    }

    @Test
    public void testWithTypeNull() {
        OptionBuilder.withType(null);
        Option opt = OptionBuilder.create("x");
        assertNull(opt.getType());
    }

    @Test
    public void testWithDescription() {
        OptionBuilder.withDescription("desc");
        Option opt = OptionBuilder.create("x");
        assertEquals("desc", opt.getDescription());
    }

    @Test
    public void testWithDescriptionNull() {
        OptionBuilder.withDescription(null);
        Option opt = OptionBuilder.create("x");
        assertNull(opt.getDescription());
    }

    @Test
    public void testCreateChar() {
        Option opt = OptionBuilder.create('x');
        assertEquals("x", opt.getOpt());
    }

    @Test
    public void testCreateNoArg() {
        OptionBuilder.withLongOpt("long");
        Option opt = OptionBuilder.create();
        assertNull(opt.getOpt());
        assertEquals("long", opt.getLongOpt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateThrowsWhenLongOptNull() {
        OptionBuilder.create();
    }

    @Test
    public void testCreateThrowsWhenLongOptNullResetsState() {
        try {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        OptionBuilder.withLongOpt("valid");
        Option opt = OptionBuilder.create("x");
        assertEquals("valid", opt.getLongOpt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidOptMultiChar() {
        OptionBuilder.create("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidOptEmpty() {
        OptionBuilder.create("");
    }

    @Test
    public void testCreateNullOpt() {
        OptionBuilder.withLongOpt("long");
        Option opt = OptionBuilder.create(null);
        assertNull(opt.getOpt());
        assertEquals("long", opt.getLongOpt());
    }

    @Test
    public void testResetAfterCreate() {
        OptionBuilder.withLongOpt("long");
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        Option opt = OptionBuilder.create("x");
        assertNotNull(opt);
        Option opt2 = OptionBuilder.create("y");
        assertNull(opt2.getLongOpt());
        assertEquals(Option.UNINITIALIZED, opt2.getArgs());
        assertFalse(opt2.isRequired());
    }

    @Test
    public void testResetAfterFailedCreate() {
        try {
            OptionBuilder.create("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        Option opt = OptionBuilder.create("x");
        assertNull(opt.getDescription());
        assertEquals("arg", opt.getArgName());
        assertNull(opt.getLongOpt());
        assertFalse(opt.isRequired());
        assertFalse(opt.hasOptionalArg());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertNull(opt.getType());
        assertEquals((char) 0, opt.getValueSeparator());
    }

    @Test
    public void testAllSettingsCombined() {
        Object type = new Object();
        OptionBuilder.withLongOpt("long")
                     .withArgName("argName")
                     .hasArg()
                     .isRequired()
                     .withValueSeparator(':')
                     .withType(type)
                     .withDescription("desc")
                     .hasOptionalArg();
        Option opt = OptionBuilder.create('x');
        assertEquals("long", opt.getLongOpt());
        assertEquals("argName", opt.getArgName());
        assertEquals(1, opt.getArgs());
        assertTrue(opt.isRequired());
        assertEquals(':', opt.getValueSeparator());
        assertSame(type, opt.getType());
        assertEquals("desc", opt.getDescription());
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testFluentInterfaceReturnsInstance() {
        assertSame(OptionBuilder.withLongOpt("test"), OptionBuilder.withDescription("desc"));
    }
}
