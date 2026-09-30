package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UtilTest {

    @Test(expected = NullPointerException.class)
    public void testStripLeadingHyphens_Null() {
        Util.stripLeadingHyphens(null);
    }

    @Test
    public void testStripLeadingHyphens_DoubleHyphen() {
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    @Test
    public void testStripLeadingHyphens_SingleHyphen() {
        assertEquals("f", Util.stripLeadingHyphens("-f"));
    }

    @Test
    public void testStripLeadingHyphens_NoHyphen() {
        assertEquals("f", Util.stripLeadingHyphens("f"));
    }

    @Test
    public void testStripLeadingHyphens_DoubleHyphenOnly() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    @Test
    public void testStripLeadingHyphens_SingleHyphenOnly() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test
    public void testStripLeadingHyphens_EmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test
    public void testStripLeadingHyphens_DoubleHyphenThenHyphen() {
        assertEquals("-", Util.stripLeadingHyphens("---"));
    }

    @Test
    public void testStripLeadingHyphens_SingleHyphenThenLetter() {
        assertEquals("foo", Util.stripLeadingHyphens("-foo"));
    }

    @Test(expected = NullPointerException.class)
    public void testStripLeadingAndTrailingQuotes_Null() {
        Util.stripLeadingAndTrailingQuotes(null);
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_BothQuotes() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_LeadingQuoteOnly() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_TrailingQuoteOnly() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_NoQuotes() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_DoubleQuotesOnly() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_SingleQuoteOnly() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_EmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_MixedQuotes() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("\"a\"b\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_InnerQuotes() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("a\"b"));
    }
}
