package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class OptionBuilderTest {

    @Before
    public void resetBuilder() throws Exception {
        Method resetMethod = OptionBuilder.class.getDeclaredMethod("reset");
        resetMethod.setAccessible(true);
        resetMethod.invoke(null);
    }

    private static Object getStaticField(String name) throws Exception {
        Field field = OptionBuilder.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    @Test
    public void testWithLongOpt() throws Exception {
        OptionBuilder.withLongOpt("test");
        assertEquals("test", getStaticField("longopt"));
    }

    @Test
    public void testWithLongOptNull() throws Exception {
        OptionBuilder.withLongOpt(null);
        assertNull(getStaticField("longopt"));
    }

    @Test
    public void testHasArg() throws Exception {
        OptionBuilder.hasArg();
        assertEquals(1, getStaticField("numberOfArgs"));
    }

    @Test
    public void testHasArgTrue() throws Exception {
        OptionBuilder.hasArg(true);
        assertEquals(1, getStaticField("numberOfArgs"));
    }

    @Test
    public void testHasArgFalse() throws Exception {
        OptionBuilder.hasArg(false);
        assertEquals(Option.UNINITIALIZED, getStaticField("numberOfArgs"));
    }

    @Test
    public void testWithArgName() throws Exception {
        OptionBuilder.withArgName("name");
        assertEquals("name", getStaticField("argName"));
    }

    @Test
    public void testWithArgNameNull() throws Exception {
        OptionBuilder.withArgName(null);
        assertNull(getStaticField("argName"));
    }

    @Test
    public void testIsRequired() throws Exception {
        OptionBuilder.isRequired();
        assertEquals(true, getStaticField("required"));
    }

    @Test
    public void testIsRequiredTrue() throws Exception {
        OptionBuilder.isRequired(true);
        assertEquals(true, getStaticField("required"));
    }

    @Test
    public void testIsRequiredFalse() throws Exception {
        OptionBuilder.isRequired(false);
        assertEquals(false, getStaticField("required"));
    }

    @Test
    public void testWithValueSeparatorChar() throws Exception {
        OptionBuilder.withValueSeparator(':');
        assertEquals(':', getStaticField("valuesep"));
    }

    @Test
    public void testWithValueSeparatorDefault() throws Exception {
        OptionBuilder.withValueSeparator();
        assertEquals('=', getStaticField("valuesep"));
    }

    @Test
    public void testHasArgs() throws Exception {
        OptionBuilder.hasArgs();
        assertEquals(Option.UNLIMITED_VALUES, getStaticField("numberOfArgs"));
    }

    @Test
    public void testHasArgsWithNum() throws Exception {
        OptionBuilder.hasArgs(5);
        assertEquals(5, getStaticField("numberOfArgs"));
    }

    @Test
    public void testHasOptionalArg() throws Exception {
        OptionBuilder.hasOptionalArg();
        assertEquals(1, getStaticField("numberOfArgs"));
        assertEquals(true, getStaticField("optionalArg"));
    }

    @Test
    public void testHasOptionalArgs() throws Exception {
        OptionBuilder.hasOptionalArgs();
        assertEquals(Option.UNLIMITED_VALUES, getStaticField("numberOfArgs"));
        assertEquals(true, getStaticField("optionalArg"));
    }

    @Test
    public void testHasOptionalArgsWithNum() throws Exception {
        OptionBuilder.hasOptionalArgs(3);
        assertEquals(3, getStaticField("numberOfArgs"));
        assertEquals(true, getStaticField("optionalArg"));
    }

    @Test
    public void testWithType() throws Exception {
        Object type = new Object();
        OptionBuilder.withType(type);
        assertSame(type, getStaticField("type"));
    }

    @Test
    public void testWithTypeNull() throws Exception {
        OptionBuilder.withType(null);
        assertNull(getStaticField("type"));
    }

    @Test
    public void testWithDescription() throws Exception {
        OptionBuilder.withDescription("desc");
        assertEquals("desc", getStaticField("description"));
    }

    @Test
    public void testWithDescriptionNull() throws Exception {
        OptionBuilder.withDescription(null);
        assertNull(getStaticField("description"));
    }

    @Test
    public void testCreateCharValid() {
        Option opt = OptionBuilder.create('a');
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertNull(opt.getDescription());
        assertFalse(opt.isRequired());
        assertFalse(opt.hasOptionalArg());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertNull(opt.getType());
        assertEquals((char) 0, opt.getValueSeparator());
        assertEquals("arg", opt.getArgName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCharInvalid() {
        OptionBuilder.create('?');
    }

    @Test
    public void testCreateStringValid() {
        Option opt = OptionBuilder.create("a");
        assertEquals("a", opt.getOpt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStringInvalid() {
        OptionBuilder.create("ab");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStringNull() {
        OptionBuilder.create((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNoLongOpt() {
        OptionBuilder.create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithLongOptOnly() {
        OptionBuilder.withLongOpt("long");
        OptionBuilder.create();
    }

    @Test
    public void testCreateWithLongOptAndValidOpt() {
        OptionBuilder.withLongOpt("long");
        Option opt = OptionBuilder.create("a");
        assertEquals("a", opt.getOpt());
        assertEquals("long", opt.getLongOpt());
    }

    @Test
    public void testResetAfterCreate() throws Exception {
        OptionBuilder.withLongOpt("long");
        OptionBuilder.withDescription("desc");
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withArgName("name");
        OptionBuilder.withType(String.class);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.hasOptionalArg();
        OptionBuilder.create("a");
        assertNull(getStaticField("longopt"));
        assertNull(getStaticField("description"));
        assertEquals("arg", getStaticField("argName"));
        assertNull(getStaticField("type"));
        assertEquals(false, getStaticField("required"));
        assertEquals(Option.UNINITIALIZED, getStaticField("numberOfArgs"));
        assertEquals(false, getStaticField("optionalArg"));
        assertEquals((char) 0, getStaticField("valuesep"));
    }

    @Test
    public void testMultipleProperties() {
        OptionBuilder.withLongOpt("long");
        OptionBuilder.withDescription("desc");
        OptionBuilder.hasArgs(3);
        OptionBuilder.isRequired(true);
        OptionBuilder.withArgName("name");
        OptionBuilder.withType(Integer.class);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.hasOptionalArg();
        Option opt = OptionBuilder.create("a");
        assertEquals("a", opt.getOpt());
        assertEquals("long", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
        assertEquals(3, opt.getArgs());
        assertTrue(opt.isRequired());
        assertEquals("name", opt.getArgName());
        assertEquals(Integer.class, opt.getType());
        assertEquals('=', opt.getValueSeparator());
        assertTrue(opt.hasOptionalArg());
    }

    @Test
    public void testBuilderReturnsInstance() {
        assertSame(OptionBuilder.withLongOpt("test"), OptionBuilder.hasArg());
        assertSame(OptionBuilder.withDescription("desc"), OptionBuilder.isRequired());
    }
}
