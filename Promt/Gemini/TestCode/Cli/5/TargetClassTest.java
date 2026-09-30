package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

    @Test
    public void testConstructor() {
        Util util = new Util();
        Assert.assertNotNull(util);
    }

    @Test
    public void testStripLeadingHyphensDoubleHyphen() {
        Assert.assertEquals("foo", Util.stripLeadingHyphens("--foo"));
        Assert.assertEquals("", Util.stripLeadingHyphens("--"));
        Assert.assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
    }

    @Test
    public void testStripLeadingHyphensSingleHyphen() {
        Assert.assertEquals("bar", Util.stripLeadingHyphens("-bar"));
        Assert.assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test
    public void testStripLeadingHyphensNoHyphen() {
        Assert.assertEquals("baz", Util.stripLeadingHyphens("baz"));
        Assert.assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test(expected = NullPointerException.class)
    public void testStripLeadingHyphensNull() {
        Util.stripLeadingHyphens(null);
    }

    @Test
    public void testStripLeadingAndTrailingQuotesBoth() {
        Assert.assertEquals("hello world", Util.stripLeadingAndTrailingQuotes("\"hello world\""));
        Assert.assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesLeadingOnly() {
        Assert.assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesTrailingOnly() {
        Assert.assertEquals("bar", Util.stripLeadingAndTrailingQuotes("bar\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesNone() {
        Assert.assertEquals("baz", Util.stripLeadingAndTrailingQuotes("baz"));
        Assert.assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesSingleQuote() {
        Assert.assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test(expected = NullPointerException.class)
    public void testStripLeadingAndTrailingQuotesNull() {
        Util.stripLeadingAndTrailingQuotes(null);
    }
}