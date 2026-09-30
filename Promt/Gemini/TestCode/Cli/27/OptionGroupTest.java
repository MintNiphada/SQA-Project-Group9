package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OptionGroupTest
{
    private OptionGroup group;
    private Option optA;
    private Option optB;
    private Option optLongOnly;

    @Before
    public void setUp()
    {
        group = new OptionGroup();
        optA = new Option("a", "option A description");
        optB = new Option("b", "option B description");
        optLongOnly = new Option(null, "long-only", false, "long only description");
    }

    @Test
    public void testDefaultState()
    {
        assertNull("Selected option should initially be null", group.getSelected());
        assertFalse("OptionGroup should not be required by default", group.isRequired());
        assertNotNull("Names collection should not be null", group.getNames());
        assertTrue("Names collection should initially be empty", group.getNames().isEmpty());
        assertNotNull("Options collection should not be null", group.getOptions());
        assertTrue("Options collection should initially be empty", group.getOptions().isEmpty());
        assertEquals("[]", group.toString());
    }

    @Test
    public void testAddOptionAndChaining()
    {
        OptionGroup returnedGroup = group.addOption(optA).addOption(optB);

        assertSame("addOption should return the same OptionGroup instance for chaining", group, returnedGroup);

        Collection names = group.getNames();
        assertEquals(2, names.size());
        assertTrue("Names should contain 'a'", names.contains("a"));
        assertTrue("Names should contain 'b'", names.contains("b"));

        Collection options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue("Options should contain optA", options.contains(optA));
        assertTrue("Options should contain optB", options.contains(optB));
    }

    @Test
    public void testAddLongOnlyOption()
    {
        group.addOption(optLongOnly);
        Collection names = group.getNames();
        assertEquals(1, names.size());
        assertTrue("Names should contain 'long-only'", names.contains("long-only"));
        assertTrue(group.getOptions().contains(optLongOnly));
    }

    @Test
    public void testSetSelected() throws Exception
    {
        group.addOption(optA);
        group.addOption(optB);

        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Selecting the same option again should succeed
        group.setSelected(optA);
        assertEquals("a", group.getSelected());
    }

    @Test
    public void testSetSelectedNull() throws Exception
    {
        group.addOption(optA);
        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Passing null should reset the selected option
        group.setSelected(null);
        assertNull("Selected option should be reset to null", group.getSelected());

        // Selecting another option after reset should succeed
        group.setSelected(optB);
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testSetSelectedConflictThrowsException() throws Exception
    {
        group.addOption(optA);
        group.addOption(optB);

        group.setSelected(optA);

        try
        {
            group.setSelected(optB);
            fail("Expected AlreadySelectedException when selecting a different option in the same group");
        }
        catch (AlreadySelectedException ex)
        {
            assertEquals(group, ex.getOptionGroup());
            assertEquals(optB, ex.getOption());
        }

        // Verify the original selection was not modified
        assertEquals("a", group.getSelected());
    }

    @Test
    public void testSetSelectedWithLongOnlyOption() throws Exception
    {
        group.addOption(optLongOnly);
        group.setSelected(optLongOnly);
        assertNull("Option with null opt will have null as selected opt name", group.getSelected());
    }

    @Test
    public void testSetRequired()
    {
        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    @Test
    public void testToStringSingleShortOption()
    {
        group.addOption(optA);
        assertEquals("[-a option A description]", group.toString());
    }

    @Test
    public void testToStringSingleLongOption()
    {
        group.addOption(optLongOnly);
        assertEquals("[--long-only long only description]", group.toString());
    }

    @Test
    public void testToStringMultipleOptions()
    {
        group.addOption(optA);
        group.addOption(optB);

        String str = group.toString();
        assertTrue("String representation must start with '['", str.startsWith("["));
        assertTrue("String representation must end with ']'", str.endsWith("]"));
        assertTrue("String representation must contain '-a option A description'", str.contains("-a option A description"));
        assertTrue("String representation must contain '-b option B description'", str.contains("-b option B description"));
        assertTrue("String representation must contain separator ', '", str.contains(", "));
    }

    @Test
    public void testToStringWithMixedOptions()
    {
        group.addOption(optA);
        group.addOption(optLongOnly);

        String str = group.toString();
        assertTrue("String representation must contain '-a option A description'", str.contains("-a option A description"));
        assertTrue("String representation must contain '--long-only long only description'", str.contains("--long-only long only description"));
        assertTrue("String representation must contain separator ', '", str.contains(", "));
    }

    @Test
    public void testSerialization() throws Exception
    {
        group.addOption(optA);
        group.addOption(optB);
        group.setSelected(optA);
        group.setRequired(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(group);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        OptionGroup deserializedGroup = (OptionGroup) ois.readObject();
        ois.close();

        assertNotNull(deserializedGroup);
        assertTrue(deserializedGroup.isRequired());
        assertEquals("a", deserializedGroup.getSelected());
        assertEquals(2, deserializedGroup.getNames().size());
        assertEquals(2, deserializedGroup.getOptions().size());
    }
}