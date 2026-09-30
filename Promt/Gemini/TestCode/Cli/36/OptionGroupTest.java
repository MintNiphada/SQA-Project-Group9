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
        optA = new Option("a", "foo", false, "description a");
        optB = new Option("b", "bar", false, "description b");
        optLongOnly = new Option(null, "longOptOnly", false, "description long only");
    }

    @Test
    public void testAddOptionAndGetters()
    {
        OptionGroup returnedGroup = group.addOption(optA);
        assertSame("addOption should support method chaining", group, returnedGroup);
        group.addOption(optB);

        Collection<String> names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));

        Collection<Option> options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optA));
        assertTrue(options.contains(optB));
    }

    @Test
    public void testSelected() throws Exception
    {
        assertNull("Selected should be null initially", group.getSelected());

        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Re-selecting the same option should be allowed
        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Reset selected to null
        group.setSelected(null);
        assertNull("Selected should be reset to null", group.getSelected());

        // Select another option after resetting
        group.setSelected(optB);
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testSelectedWithLongOptOnly() throws Exception
    {
        group.addOption(optLongOnly);
        group.setSelected(optLongOnly);
        assertEquals("longOptOnly", group.getSelected());

        // Re-select same option
        group.setSelected(optLongOnly);
        assertEquals("longOptOnly", group.getSelected());
    }

    @Test
    public void testAlreadySelectedException()
    {
        try
        {
            group.setSelected(optA);
            group.setSelected(optB);
            fail("Expected AlreadySelectedException when selecting a different option");
        }
        catch (AlreadySelectedException ex)
        {
            assertSame("Exception should refer to this group", group, ex.getOptionGroup());
            assertSame("Exception should refer to the conflicting option", optB, ex.getOption());
        }
    }

    @Test
    public void testRequired()
    {
        assertFalse("Required should default to false", group.isRequired());

        group.setRequired(true);
        assertTrue("Required should be true after setRequired(true)", group.isRequired());

        group.setRequired(false);
        assertFalse("Required should be false after setRequired(false)", group.isRequired());
    }

    @Test
    public void testToStringEmpty()
    {
        assertEquals("[]", group.toString());
    }

    @Test
    public void testToStringSingleShortOption()
    {
        group.addOption(new Option("a", "description a"));
        assertEquals("[-a description a]", group.toString());
    }

    @Test
    public void testToStringSingleShortOptionNoDescription()
    {
        group.addOption(new Option("a", null));
        assertEquals("[-a]", group.toString());
    }

    @Test
    public void testToStringSingleLongOption()
    {
        group.addOption(new Option(null, "foo", false, "description foo"));
        assertEquals("[--foo description foo]", group.toString());
    }

    @Test
    public void testToStringSingleLongOptionNoDescription()
    {
        group.addOption(new Option(null, "foo", false, null));
        assertEquals("[--foo]", group.toString());
    }

    @Test
    public void testToStringMultipleOptions()
    {
        group.addOption(new Option("a", "foo", false, "descA"));
        group.addOption(new Option(null, "bar", false, "descB"));

        String str = group.toString();
        assertTrue(str.startsWith("["));
        assertTrue(str.endsWith("]"));
        assertTrue(str.contains("-a descA"));
        assertTrue(str.contains("--bar descB"));
        assertTrue(str.contains(", "));
    }

    @Test
    public void testSerialization() throws Exception
    {
        group.addOption(optA);
        group.addOption(optB);
        group.setRequired(true);
        group.setSelected(optA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(group);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        OptionGroup deserialized = (OptionGroup) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized.isRequired());
        assertEquals("a", deserialized.getSelected());
        assertEquals(2, deserialized.getNames().size());
        assertEquals(2, deserialized.getOptions().size());
    }
}