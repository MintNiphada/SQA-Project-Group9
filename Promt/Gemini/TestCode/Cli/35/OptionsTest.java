package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test suite for {@link Options}.
 */
public class OptionsTest
{
    private Options options;

    @Before
    public void setUp()
    {
        options = new Options();
    }

    @Test
    public void testAddOptionShortOnlyNoArg()
    {
        Options res = options.addOption("a", "description a");
        assertEquals(options, res);
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("-a"));
        assertTrue(options.hasShortOption("a"));
        assertFalse(options.hasLongOption("a"));

        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertFalse(opt.hasArg());
        assertEquals("description a", opt.getDescription());
    }

    @Test
    public void testAddOptionShortOnlyWithArg()
    {
        Options res = options.addOption("b", true, "description b");
        assertEquals(options, res);
        assertTrue(options.hasOption("b"));
        assertTrue(options.hasShortOption("b"));
        assertFalse(options.hasLongOption("b"));

        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertEquals("b", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertTrue(opt.hasArg());
        assertEquals("description b", opt.getDescription());
    }

    @Test
    public void testAddOptionShortAndLong()
    {
        Options res = options.addOption("c", "count", false, "description c");
        assertEquals(options, res);
        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("count"));
        assertTrue(options.hasOption("-c"));
        assertTrue(options.hasOption("--count"));
        assertTrue(options.hasShortOption("c"));
        assertTrue(options.hasLongOption("count"));

        Option optByShort = options.getOption("c");
        Option optByLong = options.getOption("count");
        assertEquals(optByShort, optByLong);
        assertEquals("c", optByShort.getOpt());
        assertEquals("count", optByShort.getLongOpt());
    }

    @Test
    public void testAddOptionLongOnly()
    {
        Option longOnly = new Option(null, "verbose", false, "verbose mode");
        options.addOption(longOnly);

        assertTrue(options.hasOption("verbose"));
        assertTrue(options.hasOption("--verbose"));
        assertTrue(options.hasLongOption("verbose"));
        assertFalse(options.hasShortOption("verbose"));

        Option retrieved = options.getOption("verbose");
        assertEquals(longOnly, retrieved);
        assertEquals("verbose", retrieved.getKey());
    }

    @Test
    public void testAddRequiredOption()
    {
        Option opt = new Option("r", "required", false, "required option");
        opt.setRequired(true);

        options.addOption(opt);
        List required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertEquals("r", required.get(0));

        // Re-adding the same required option to test removal branch
        options.addOption(opt);
        required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertEquals("r", required.get(0));
    }

    @Test
    public void testAddOptionGroup()
    {
        OptionGroup group = new OptionGroup();
        Option foo = new Option("f", "foo", false, "foo option");
        foo.setRequired(true); // Should be reset to false in group
        Option bar = new Option("b", "bar", false, "bar option");
        group.addOption(foo);
        group.addOption(bar);

        options.addOptionGroup(group);

        assertFalse(foo.isRequired());
        assertTrue(options.hasOption("f"));
        assertTrue(options.hasOption("foo"));
        assertTrue(options.hasOption("b"));
        assertTrue(options.hasOption("bar"));
        assertEquals(group, options.getOptionGroup(foo));
        assertEquals(group, options.getOptionGroup(bar));
        assertTrue(options.getRequiredOptions().isEmpty());

        Collection<OptionGroup> groups = options.getOptionGroups();
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group));
    }

    @Test
    public void testAddRequiredOptionGroup()
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option foo = new Option("f", "foo", false, "foo option");
        group.addOption(foo);

        options.addOptionGroup(group);

        assertTrue(group.isRequired());
        List required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertEquals(group, required.get(0));
    }

    @Test
    public void testGetOptionGroupNull()
    {
        Option standalone = new Option("s", "standalone", false, "standalone");
        options.addOption(standalone);

        assertNull(options.getOptionGroup(standalone));
    }

    @Test
    public void testGetMatchingOptions()
    {
        options.addOption("v", "version", false, "display version");
        options.addOption("verbose", false, "verbose output");
        options.addOption("var", false, "variable");

        List<String> matches = options.getMatchingOptions("ver");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("version"));
        assertTrue(matches.contains("verbose"));
        assertFalse(matches.contains("var"));

        List<String> exactMatch = options.getMatchingOptions("--version");
        assertEquals(1, exactMatch.size());
        assertTrue(exactMatch.contains("version"));

        List<String> noMatches = options.getMatchingOptions("nonexistent");
        assertTrue(noMatches.isEmpty());
    }

    @Test
    public void testGetOptionsUnmodifiable()
    {
        options.addOption("a", "optA");
        options.addOption("b", "optB");

        Collection<Option> opts = options.getOptions();
        assertEquals(2, opts.size());

        try
        {
            opts.clear();
            fail("getOptions() should be unmodifiable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Expected
        }
    }

    @Test
    public void testGetRequiredOptionsUnmodifiable()
    {
        Option opt = new Option("r", "req", false, "req");
        opt.setRequired(true);
        options.addOption(opt);

        List required = options.getRequiredOptions();
        assertEquals(1, required.size());

        try
        {
            required.clear();
            fail("getRequiredOptions() should be unmodifiable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Expected
        }
    }

    @Test
    public void testHelpOptions()
    {
        Option a = new Option("a", "desc A");
        Option b = new Option("b", "desc B");
        options.addOption(a);
        options.addOption(b);

        List<Option> helpOpts = options.helpOptions();
        assertEquals(2, helpOpts.size());
        assertTrue(helpOpts.contains(a));
        assertTrue(helpOpts.contains(b));
    }

    @Test
    public void testGetOptionNonExistent()
    {
        assertNull(options.getOption("nonexistent"));
        assertNull(options.getOption("--nonexistent"));
        assertFalse(options.hasOption("nonexistent"));
        assertFalse(options.hasShortOption("nonexistent"));
        assertFalse(options.hasLongOption("nonexistent"));
    }

    @Test
    public void testToString()
    {
        options.addOption("a", "apple", false, "Apple option");
        String repr = options.toString();
        assertNotNull(repr);
        assertTrue(repr.startsWith("[ Options: [ short "));
        assertTrue(repr.contains("apple"));
        assertTrue(repr.contains("[ long "));
        assertTrue(repr.endsWith(" ]"));
    }

    @Test
    public void testSerialization() throws Exception
    {
        options.addOption("a", "alpha", true, "Alpha option");
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("b", "beta", false, "Beta option"));
        options.addOptionGroup(group);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(options);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Options deserialized = (Options) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized.hasOption("a"));
        assertTrue(deserialized.hasOption("alpha"));
        assertTrue(deserialized.hasOption("b"));
        assertTrue(deserialized.hasOption("beta"));
        assertEquals(2, deserialized.getOptions().size());
        assertEquals(1, deserialized.getOptionGroups().size());
    }
}