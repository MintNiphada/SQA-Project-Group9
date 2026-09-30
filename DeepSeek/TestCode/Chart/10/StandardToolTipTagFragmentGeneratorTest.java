package org.jfree.chart.imagemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StandardToolTipTagFragmentGeneratorTest {

    @Test
    public void testConstructor() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        assertNotNull(generator);
    }

    @Test
    public void testGenerateToolTipFragment_Null() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment(null);
        assertEquals(" title=\"null\" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_EmptyString() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("");
        assertEquals(" title=\"\" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_SimpleText() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("Hello World");
        assertEquals(" title=\"Hello World\" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_WithDoubleQuotes() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("abc\"def");
        assertEquals(" title=\"abc\"def\" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_WithSpecialCharacters() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("<>&");
        assertEquals(" title=\"<>&\" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_LeadingTrailingSpaces() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("  text  ");
        assertEquals(" title=\"  text  \" alt=\"\"", actual);
    }

    @Test
    public void testGenerateToolTipFragment_MultipleLines() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment("Line1\nLine2");
        assertEquals(" title=\"Line1\nLine2\" alt=\"\"", actual);
    }
}
