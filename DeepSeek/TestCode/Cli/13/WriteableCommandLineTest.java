package org.apache.commons.cli2;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli2.builder.OptionBuilder;

public class WriteableCommandLineTest {

    private WriteableCommandLine cmdLine;
    private Option optionA;
    private Option optionB;

    @Before
    public void setUp() {
        cmdLine = new WriteableCommandLineImpl();
        optionA = new OptionBuilder().withName("a").withDescription("Option A").create();
        optionB = new OptionBuilder().withName("b").withDescription("Option B").create();
    }

    @Test
    public void testAddOptionNormal() {
        cmdLine.addOption(optionA);
        Assert.assertTrue("CommandLine should have optionA", cmdLine.hasOption(optionA));
    }

    @Test
    public void testAddOptionDuplicate() {
        cmdLine.addOption(optionA);
        cmdLine.addOption(optionA);
        Assert.assertTrue("CommandLine should still have optionA", cmdLine.hasOption(optionA));
    }

    @Test(expected = NullPointerException.class)
    public void testAddOptionNull() {
        cmdLine.addOption(null);
    }

    @Test
    public void testAddValueToExistingOption() {
        cmdLine.addOption(optionA);
        cmdLine.addValue(optionA, "value1");
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertNotNull("Values list should not be null", values);
        Assert.assertEquals("Should have one value", 1, values.size());
        Assert.assertEquals("value1", values.get(0));
    }

    @Test
    public void testAddMultipleValues() {
        cmdLine.addOption(optionA);
        cmdLine.addValue(optionA, "v1");
        cmdLine.addValue(optionA, "v2");
        cmdLine.addValue(optionA, "v3");
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("v1", values.get(0));
        Assert.assertEquals("v2", values.get(1));
        Assert.assertEquals("v3", values.get(2));
    }

    @Test
    public void testAddValueToOptionNotYetAdded() {
        // Some implementations may allow adding values before the option is added
        cmdLine.addValue(optionA, "early");
        cmdLine.addOption(optionA);
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("early", values.get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testAddValueNullOption() {
        cmdLine.addValue(null, "value");
    }

    @Test
    public void testAddValueNullValue() {
        cmdLine.addOption(optionA);
        cmdLine.addValue(optionA, null);
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertEquals(1, values.size());
        Assert.assertNull(values.get(0));
    }

    @Test
    public void testSetDefaultValuesForNewOption() {
        List<String> defaults = new ArrayList<>(Arrays.asList("d1", "d2"));
        cmdLine.setDefaultValues(optionA, defaults);
        cmdLine.addOption(optionA);
        // No explicit values added, so defaults should be returned
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("d1", values.get(0));
        Assert.assertEquals("d2", values.get(1));
    }

    @Test
    public void testSetDefaultValuesOverriddenByExplicitValues() {
        List<String> defaults = new ArrayList<>(Arrays.asList("def1"));
        cmdLine.setDefaultValues(optionA, defaults);
        cmdLine.addOption(optionA);
        cmdLine.addValue(optionA, "real");
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("real", values.get(0));
    }

    @Test
    public void testSetDefaultValuesEmptyList() {
        cmdLine.setDefaultValues(optionA, new ArrayList<String>());
        cmdLine.addOption(optionA);
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testSetDefaultValuesNullOption() {
        List<String> defaults = new ArrayList<>(Arrays.asList("d1"));
        cmdLine.setDefaultValues(null, defaults);
    }

    @Test
    public void testSetDefaultValuesNullList() {
        cmdLine.addOption(optionA);
        cmdLine.setDefaultValues(optionA, null);
        // Depending on implementation, getValues may return empty list or throw NPE.
        // We'll verify it doesn't NPE and returns empty.
        List<?> values = cmdLine.getValues(optionA);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void testAddSwitchTrue() {
        cmdLine.addOption(optionA);
        cmdLine.addSwitch(optionA, true);
        Assert.assertTrue("Switch should be true", cmdLine.getSwitch(optionA, false));
    }

    @Test
    public void testAddSwitchFalse() {
        cmdLine.addOption(optionA);
        cmdLine.addSwitch(optionA, false);
        Assert.assertFalse("Switch should be false", cmdLine.getSwitch(optionA, true));
    }

    @Test
    public void testAddSwitchWithoutOptionAddedFirst() {
        // Add switch before option is added
        cmdLine.addSwitch(optionA, true);
        cmdLine.addOption(optionA);
        Assert.assertTrue(cmdLine.getSwitch(optionA, false));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchDuplicateThrows() {
        cmdLine.addOption(optionA);
        cmdLine.addSwitch(optionA, true);
        cmdLine.addSwitch(optionA, false);
    }

    @Test(expected = NullPointerException.class)
    public void testAddSwitchNullOption() {
        cmdLine.addSwitch(null, true);
    }

    @Test
    public void testSetDefaultSwitchTrue() {
        cmdLine.setDefaultSwitch(optionA, Boolean.TRUE);
        cmdLine.addOption(optionA);
        // No explicit switch added, so default applies
        Assert.assertTrue(cmdLine.getSwitch(optionA, false));
    }

    @Test
    public void testSetDefaultSwitchFalse() {
        cmdLine.setDefaultSwitch(optionA, Boolean.FALSE);
        cmdLine.addOption(optionA);
        Assert.assertFalse(cmdLine.getSwitch(optionA, true));
    }

    @Test
    public void testSetDefaultSwitchOverrideByAddSwitch() {
        cmdLine.setDefaultSwitch(optionA, Boolean.TRUE);
        cmdLine.addOption(optionA);
        cmdLine.addSwitch(optionA, false);
        Assert.assertFalse(cmdLine.getSwitch(optionA, true));
    }

    @Test
    public void testSetDefaultSwitchNullDefault() {
        cmdLine.setDefaultSwitch(optionA, null);
        cmdLine.addOption(optionA);
        // Default null means no default, so getSwitch with a fallback returns fallback
        Assert.assertFalse(cmdLine.getSwitch(optionA, false));
    }

    @Test(expected = NullPointerException.class)
    public void testSetDefaultSwitchNullOption() {
        cmdLine.setDefaultSwitch(null, Boolean.TRUE);
    }

    @Test
    public void testAddProperty() {
        cmdLine.addProperty("key1", "val1");
        Assert.assertEquals("val1", cmdLine.getProperty("key1"));
    }

    @Test
    public void testAddPropertyReplacesExisting() {
        cmdLine.addProperty("key1", "val1");
        cmdLine.addProperty("key1", "val2");
        Assert.assertEquals("val2", cmdLine.getProperty("key1"));
    }

    @Test
    public void testAddPropertyNullKey() {
        cmdLine.addProperty(null, "val");
        Assert.assertEquals("val", cmdLine.getProperty(null));
    }

    @Test
    public void testAddPropertyNullValue() {
        cmdLine.addProperty("key", null);
        Assert.assertNull(cmdLine.getProperty("key"));
    }

    @Test
    public void testGetPropertyNotFound() {
        Assert.assertNull(cmdLine.getProperty("nonexistent"));
    }

    @Test
    public void testLooksLikeOptionPositive() {
        Assert.assertTrue("'-a' should look like an option", cmdLine.looksLikeOption("-a"));
        Assert.assertTrue("'--long' should look like an option", cmdLine.looksLikeOption("--long"));
    }

    @Test
    public void testLooksLikeOptionNegative() {
        Assert.assertFalse("'abc' should not look like an option", cmdLine.looksLikeOption("abc"));
        Assert.assertFalse("'' should not look like an option", cmdLine.looksLikeOption(""));
        Assert.assertFalse("'/' should not look like an option", cmdLine.looksLikeOption("/"));
        Assert.assertFalse("'-' alone should not be considered an option", cmdLine.looksLikeOption("-"));
    }

    @Test
    public void testLooksLikeOptionNull() {
        // This method may throw NullPointerException or return false, we assume safe handling
        Assert.assertFalse(cmdLine.looksLikeOption(null));
    }
}
