package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Collection;
import java.util.Iterator;

public class OptionGroupTest {

    private OptionGroup group;
    private Option optionA;
    private Option optionB;
    private Option optionC;

    @Before
    public void setUp() {
        group = new OptionGroup();
        optionA = new Option("a", "alpha", false, "Alpha option");
        optionB = new Option("b", "beta", false, "Beta option");
        optionC = new Option("c", false, "Charlie option");
    }

    @Test
    public void testAddOption() {
        assertSame(group, group.addOption(optionA));
        assertEquals(1, group.getOptions().size());
        assertTrue(group.getNames().contains("a"));
    }

    @Test
    public void testGetNamesEmpty() {
        assertTrue(group.getNames().isEmpty());
    }

    @Test
    public void testGetNamesAfterAdd() {
        group.addOption(optionA);
        group.addOption(optionB);
        Collection names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    @Test
    public void testGetOptionsEmpty() {
        assertTrue(group.getOptions().isEmpty());
    }

    @Test
    public void testGetOptionsAfterAdd() {
        group.addOption(optionA);
        group.addOption(optionB);
        Collection options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optionA));
        assertTrue(options.contains(optionB));
    }

    @Test
    public void testSetSelectedNullResets() throws AlreadySelectedException {
        group.addOption(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test
    public void testSetSelectedSameOption() throws AlreadySelectedException {
        group.addOption(optionA);
        group.setSelected(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedDifferentOptionThrows() throws AlreadySelectedException {
        group.addOption(optionA);
        group.addOption(optionB);
        group.setSelected(optionA);
        group.setSelected(optionB);
    }

    @Test
    public void testSetSelectedWithNullWhenNothingSelected() throws AlreadySelectedException {
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test
    public void testGetSelectedInitiallyNull() {
        assertNull(group.getSelected());
    }

    @Test
    public void testSetRequiredTrue() {
        group.setRequired(true);
        assertTrue(group.isRequired());
    }

    @Test
    public void testSetRequiredFalse() {
        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    @Test
    public void testIsRequiredDefaultFalse() {
        assertFalse(group.isRequired());
    }

    @Test
    public void testToStringEmpty() {
        assertEquals("[]", group.toString());
    }

    @Test
    public void testToStringSingleOptionShortOpt() {
        group.addOption(optionA);
        String str = group.toString();
        assertTrue(str.startsWith("["));
        assertTrue(str.endsWith("]"));
        assertTrue(str.contains("-a"));
        assertTrue(str.contains("Alpha option"));
    }

    @Test
    public void testToStringSingleOptionLongOptOnly() {
        Option longOnly = new Option(null, "long", false, "Long only");
        group.addOption(longOnly);
        String str = group.toString();
        assertTrue(str.contains("--long"));
        assertTrue(str.contains("Long only"));
    }

    @Test
    public void testToStringMultipleOptions() {
        group.addOption(optionA);
        group.addOption(optionB);
        group.addOption(optionC);
        String str = group.toString();
        assertTrue(str.contains("-a"));
        assertTrue(str.contains("Alpha option"));
        assertTrue(str.contains("-b"));
        assertTrue(str.contains("Beta option"));
        assertTrue(str.contains("-c"));
        assertTrue(str.contains("Charlie option"));
        assertTrue(str.contains(", "));
    }

    @Test
    public void testToStringOptionWithNullDescription() {
        Option opt = new Option("d", false, null);
        group.addOption(opt);
        String str = group.toString();
        assertTrue(str.contains("-d"));
        assertTrue(str.contains("null"));
    }

    @Test
    public void testAddOptionMultiple() {
        group.addOption(optionA).addOption(optionB).addOption(optionC);
        assertEquals(3, group.getOptions().size());
    }

    @Test
    public void testSetSelectedAfterReset() throws AlreadySelectedException {
        group.addOption(optionA);
        group.addOption(optionB);
        group.setSelected(optionA);
        group.setSelected(null);
        group.setSelected(optionB);
        assertEquals("b", group.getSelected());
    }

    @Test
    public void testSetSelectedWithOptionHavingNullOpt() throws AlreadySelectedException {
        Option opt = new Option(null, "long", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        assertNull(group.getSelected());
    }

    @Test
    public void testSetSelectedWithOptionHavingNullOptAndSelectedNotNull() throws AlreadySelectedException {
        Option opt1 = new Option("x", false, "desc");
        Option opt2 = new Option(null, "long", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt2);
        assertNull(group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedWithOptionHavingNullOptAndDifferentSelected() throws AlreadySelectedException {
        Option opt1 = new Option("x", false, "desc");
        Option opt2 = new Option(null, "long", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt2);
        group.setSelected(opt1);
    }
}
