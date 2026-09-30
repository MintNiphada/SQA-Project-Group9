package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for {@link Options}.
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
    public void testEmptyOptions()
    {
        assertTrue(options.getOptions().isEmpty());
        assertTrue(options.getRequiredOptions().isEmpty());
        assertTrue(options.getOptionGroups().isEmpty());
        assertTrue(options.helpOptions().isEmpty());
        assertFalse(options.hasOption("opt"));
        assertFalse(options.hasShortOption("opt"));
        assertFalse(options.hasLongOption("opt"));
        assertNull(options.getOption("opt"));
        assertTrue(options.getMatchingOptions("opt").isEmpty());
    }

    @Test
    public void testAddOptionShortOnlyNoArg()
    {
        Options returned = options.addOption("a", "description a");
        assertSame(options, returned);

        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("-a"));
        assertTrue(options.hasShortOption("a"));
        assertTrue(options.hasShortOption("-a"));
        assertFalse(options.hasLongOption("a"));

        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertEquals("description a", opt.getDescription());
        assertFalse(opt.hasArg());
        assertFalse(opt.isRequired());
    }

    @Test
    public void testAddOptionShortOnlyWithArg()
    {
        options.addOption("b", true, "description b");

        assertTrue(options.hasOption("b"));
        Option opt = options.getOption("-b");
        assertNotNull(opt);
        assertEquals("b", opt.getOpt());
        assertTrue(opt.hasArg());
        assertEquals("description b", opt.getDescription());
    }

    @Test
    public void testAddOptionShortAndLongWithArg()
    {
        options.addOption("c", "count", true, "description c");

        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("count"));
        assertTrue(options.hasOption("-c"));
        assertTrue(options.hasOption("--count"));
        assertTrue(options.hasShortOption("c"));
        assertFalse(options.hasShortOption("count"));
        assertTrue(options.hasLongOption("count"));
        assertFalse(options.hasLongOption("c"));

        Option byShort = options.getOption("c");
        Option byLong = options.getOption("count");
        assertSame(byShort, byLong);
        assertEquals("c", byShort.getOpt());
        assertEquals("count", byShort.getLongOpt());
        assertTrue(byShort.hasArg());
    }

    @Test
    public void testAddOptionInstance()
    {
        Option opt = Option.builder("d")
                .longOpt("debug")
                .desc("debug mode")
                .hasArg()
                .required()
                .build();

        options.addOption(opt);

        assertTrue(options.hasOption("d"));
        assertTrue(options.hasOption("debug"));
        assertEquals(1, options.getOptions().size());
        assertEquals(1, options.getRequiredOptions().size());
        assertEquals("d", options.getRequiredOptions().get(0));

        assertSame(opt, options.getOption("d"));
        assertSame(opt, options.getOption("debug"));
    }

    @Test
    public void testAddDuplicateRequiredOption()
    {
        Option opt1 = Option.builder("r").longOpt("req").required().build();
        Option opt2 = Option.builder("r").longOpt("required").required().build();

        options.addOption(opt1);
        assertEquals(1, options.getRequiredOptions().size());
        assertEquals("r", options.getRequiredOptions().get(0));

        options.addOption(opt2);
        assertEquals(1, options.getRequiredOptions().size());
        assertEquals("r", options.getRequiredOptions().get(0));
        assertSame(opt2, options.getOption("r"));
        assertSame(opt2, options.getOption("required"));
    }

    @Test
    public void testAddOptionGroupRequired()
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);

        Option opt1 = Option.builder("f").longOpt("file").required().build();
        Option opt2 = Option.builder("u").longOpt("url").required().build();

        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        assertFalse(opt1.isRequired());
        assertFalse(opt2.isRequired());

        Collection<OptionGroup> groups = options.getOptionGroups();
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group));

        List<?> required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertSame(group, required.get(0));

        assertSame(group, options.getOptionGroup(opt1));
        assertSame(group, options.getOptionGroup(opt2));

        Option nonGroupOpt = new Option("x", "extra");
        options.addOption(nonGroupOpt);
        assertNull(options.getOptionGroup(nonGroupOpt));
    }

    @Test
    public void testAddOptionGroupNotRequired()
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(false);

        Option optA = new Option("a", "alpha");
        Option optB = new Option("b", "beta");

        group.addOption(optA);
        group.addOption(optB);

        options.addOptionGroup(group);

        assertTrue(options.getRequiredOptions().isEmpty());
        assertEquals(1, options.getOptionGroups().size());
        assertSame(group, options.getOptionGroup(optA));
        assertSame(group, options.getOptionGroup(optB));
    }

    @Test
    public void testGetOptionsUnmodifiable()
    {
        options.addOption("a", "description");
        Collection<Option> coll = options.getOptions();
        assertEquals(1, coll.size());

        try
        {
            coll.add(new Option("b", "description"));
            fail("getOptions() collection should be unmodifiable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Expected
        }
    }

    @Test
    public void testGetRequiredOptionsUnmodifiable()
    {
        Option opt = Option.builder("r").required().build();
        options.addOption(opt);
        List<?> req = options.getRequiredOptions();
        assertEquals(1, req.size());

        try
        {
            req.remove(0);
            fail("getRequiredOptions() list should be unmodifiable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Expected
        }
    }

    @Test
    public void testGetOptionHyphensHandling()
    {
        options.addOption("v", "verbose", false, "verbose mode");

        assertNotNull(options.getOption("v"));
        assertNotNull(options.getOption("-v"));
        assertNotNull(options.getOption("--verbose"));
        assertNotNull(options.getOption("verbose"));
        assertNotNull(options.getOption("---verbose")); // Util.stripLeadingHyphens strips first 2 hyphens -> -verbose (not found) unless matched

        assertNull(options.getOption("nonexistent"));
        assertNull(options.getOption("-nonexistent"));
    }

    @Test
    public void testGetMatchingOptionsExactMatch()
    {
        options.addOption("v", "version", false, "display version");
        options.addOption("ver", "verbose", false, "display verbose output");

        List<String> matching = options.getMatchingOptions("version");
        assertEquals(1, matching.size());
        assertEquals("version", matching.get(0));

        matching = options.getMatchingOptions("--version");
        assertEquals(1, matching.size());
        assertEquals("version", matching.get(0));
    }

    @Test
    public void testGetMatchingOptionsPartialMatch()
    {
        options.addOption(null, "config-file", true, "configuration file");
        options.addOption(null, "config-dir", true, "configuration directory");
        options.addOption(null, "context", true, "context name");

        List<String> matching = options.getMatchingOptions("config");
        assertEquals(2, matching.size());
        assertTrue(matching.contains("config-file"));
        assertTrue(matching.contains("config-dir"));

        matching = options.getMatchingOptions("--con");
        assertEquals(3, matching.size());
        assertTrue(matching.contains("config-file"));
        assertTrue(matching.contains("config-dir"));
        assertTrue(matching.contains("context"));

        matching = options.getMatchingOptions("foo");
        assertTrue(matching.isEmpty());
    }

    @Test
    public void testLongOptionOnly()
    {
        Option longOnly = Option.builder().longOpt("long-only").desc("long option only").build();
        options.addOption(longOnly);

        assertTrue(options.hasOption("long-only"));
        assertTrue(options.hasOption("--long-only"));
        assertTrue(options.hasLongOption("long-only"));
        assertTrue(options.hasLongOption("--long-only"));
        assertFalse(options.hasShortOption("long-only"));

        assertSame(longOnly, options.getOption("long-only"));
        assertSame(longOnly, options.getOption("--long-only"));
    }

    @Test
    public void testToString()
    {
        options.addOption("a", "alpha", false, "first option");
        options.addOption("b", "beta", true, "second option");

        String str = options.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[ Options: [ short "));
        assertTrue(str.contains("a"));
        assertTrue(str.contains("alpha"));
        assertTrue(str.contains("b"));
        assertTrue(str.contains("beta"));
        assertTrue(str.endsWith(" ]"));
    }

    @Test
    public void testSerialization() throws Exception
    {
        options.addOption("a", "alpha", true, "option a");
        Option reqOpt = Option.builder("r").longOpt("req").required().build();
        options.addOption(reqOpt);

        OptionGroup group = new OptionGroup();
        group.addOption(new Option("g1", "group opt 1"));
        group.addOption(new Option("g2", "group opt 2"));
        group.setRequired(true);
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
        assertEquals(4, deserialized.getOptions().size());
        assertTrue(deserialized.hasOption("a"));
        assertTrue(deserialized.hasOption("alpha"));
        assertTrue(deserialized.hasOption("req"));
        assertTrue(deserialized.hasOption("g1"));
        assertTrue(deserialized.hasOption("g2"));

        assertEquals(2, deserialized.getRequiredOptions().size());
        assertEquals(1, deserialized.getOptionGroups().size());
    }
}