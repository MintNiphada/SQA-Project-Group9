package org.jfree.chart.imagemap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link StandardToolTipTagFragmentGenerator}.
 */
public class StandardToolTipTagFragmentGeneratorTest {

    private StandardToolTipTagFragmentGenerator generator;

    @Before
    public void setUp() {
        generator = new StandardToolTipTagFragmentGenerator();
    }

    /**
     * Test constructor creation.
     */
    @Test
    public void testConstructor() {
        StandardToolTipTagFragmentGenerator instance = new StandardToolTipTagFragmentGenerator();
        assertNotNull(instance);
        assertTrue(instance instanceof ToolTipTagFragmentGenerator);
    }

    /**
     * Test generating a tooltip fragment with standard alphanumeric text.
     */
    @Test
    public void testGenerateToolTipFragmentStandard() {
        String input = "Sample Tooltip";
        String expected = " title=\"Sample Tooltip\" alt=\"\"";
        String actual = generator.generateToolTipFragment(input);
        assertEquals(expected, actual);
    }

    /**
     * Test generating a tooltip fragment with an empty string.
     */
    @Test
    public void testGenerateToolTipFragmentEmpty() {
        String input = "";
        String expected = " title=\"\" alt=\"\"";
        String actual = generator.generateToolTipFragment(input);
        assertEquals(expected, actual);
    }

    /**
     * Test generating a tooltip fragment with null value.
     */
    @Test
    public void testGenerateToolTipFragmentNull() {
        String expected = " title=\"null\" alt=\"\"";
        String actual = generator.generateToolTipFragment(null);
        assertEquals(expected, actual);
    }

    /**
     * Test generating a tooltip fragment with special characters (quotes, html entities, etc.).
     */
    @Test
    public void testGenerateToolTipFragmentSpecialCharacters() {
        String input = "Value: 100 & \"special\" <tag>";
        String expected = " title=\"Value: 100 & \"special\" <tag>\" alt=\"\"";
        String actual = generator.generateToolTipFragment(input);
        assertEquals(expected, actual);
    }

    /**
     * Test generating a tooltip fragment with whitespaces and newlines.
     */
    @Test
    public void testGenerateToolTipFragmentWhitespace() {
        String input = "   Leading and trailing   \n\t";
        String expected = " title=\"   Leading and trailing   \n\t\" alt=\"\"";
        String actual = generator.generateToolTipFragment(input);
        assertEquals(expected, actual);
    }
}