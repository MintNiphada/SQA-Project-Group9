package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UtilTest {

    @Test
    public void testStripLeadingHyphens_null() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    @Test
    public void testStripLeadingHyphens_empty() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test
    public void testStripLeadingHyphens_noHyphen() {
        assertEquals("abc", Util.stripLeadingHyphens("abc"));
    }

    @Test
    public void testStripLeadingHyphens_singleHyphen() {
        assertEquals("bc", Util.stripLeadingHyphens("-bc"));
    }

    @Test
    public void testStripLeadingHyphens_doubleHyphen() {
        assertEquals("c", Util.stripLeadingHyphens("--c"));
    }

    @Test
    public void testStripLeadingHyphens_onlySingleHyphen() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test
    public void testStripLeadingHyphens_onlyDoubleHyphen() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    @Test
    public void testStripLeadingHyphens_multipleHyphens() {
        assertEquals("-", Util.stripLeadingHyphens("---"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("abc"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_leadingOnly() {
        assertEquals("abc\"", Util.stripLeadingAndTrailingQuotes("\"abc\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_trailingOnly() {
        assertEquals("\"abc", Util.stripLeadingAndTrailingQuotes("\"abc"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_bothQuotes() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("\"abc\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_onlyOneQuote() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test(expected = NullPointerException.class)
    public void testStripLeadingAndTrailingQuotes_null() {
        Util.stripLeadingAndTrailingQuotes(null);
    }
}
