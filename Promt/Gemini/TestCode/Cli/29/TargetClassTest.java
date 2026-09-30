package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UtilTest {

    @Test
    public void testConstructor() {
        Util util = new Util();
        assertNotNull(util);
    }

    @Test
    public void testStripLeadingHyphens() {
        assertNull(Util.stripLeadingHyphens(null));
        assertEquals("", Util.stripLeadingHyphens(""));
        assertEquals("f", Util.stripLeadingHyphens("-f"));
        assertEquals("foo", Util.stripLeadingHyphens("-foo"));
        assertEquals("", Util.stripLeadingHyphens("-"));
        assertEquals("f", Util.stripLeadingHyphens("--f"));
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
        assertEquals("", Util.stripLeadingHyphens("--"));
        assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
        assertEquals("foo-bar", Util.stripLeadingHyphens("foo-bar"));
        assertEquals("foo--bar", Util.stripLeadingHyphens("foo--bar"));
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
        assertEquals("one two", Util.stripLeadingAndTrailingQuotes("\"one two\""));
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo\""));
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\"\"\""));
        assertEquals("\"foo\"", Util.stripLeadingAndTrailingQuotes("\"\"foo\"\""));
        assertEquals("foo\"bar", Util.stripLeadingAndTrailingQuotes("foo\"bar"));
    }
}