package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void testKnownBlockTags() {
        Tag div = Tag.valueOf("div");
        assertEquals("div", div.getName());
        assertEquals("div", div.toString());
        assertTrue(div.isBlock());
        assertFalse(div.isInline());
        assertTrue(div.formatAsBlock());
        assertTrue(div.canContainBlock());
        assertFalse(div.isEmpty());
        assertFalse(div.isSelfClosing());
        assertFalse(div.preserveWhitespace());
        assertFalse(div.isData());
        assertTrue(div.isKnownTag());
        assertTrue(Tag.isKnownTag("div"));

        Tag p = Tag.valueOf("P");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertFalse(p.formatAsBlock());
        assertTrue(p.canContainBlock());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertFalse(p.preserveWhitespace());
        assertTrue(p.isKnownTag());
    }

    @Test
    public void testKnownInlineTags() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
        assertFalse(span.formatAsBlock());
        assertFalse(span.canContainBlock());
        assertFalse(span.isEmpty());
        assertFalse(span.isSelfClosing());
        assertFalse(span.preserveWhitespace());
        assertFalse(span.isData());
        assertTrue(span.isKnownTag());

        Tag a = Tag.valueOf("A");
        assertEquals("a", a.getName());
        assertFalse(a.isBlock());
        assertTrue(a.isInline());
        assertFalse(a.formatAsBlock());
        assertFalse(a.canContainBlock());
        assertFalse(a.isEmpty());
        assertFalse(a.isSelfClosing());
    }

    @Test
    public void testEmptyTags() {
        Tag img = Tag.valueOf("img");
        assertEquals("img", img.getName());
        assertFalse(img.isBlock());
        assertTrue(img.isInline());
        assertFalse(img.formatAsBlock());
        assertFalse(img.canContainBlock());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.preserveWhitespace());
        assertFalse(img.isData());
        assertTrue(img.isKnownTag());

        Tag hr = Tag.valueOf("hr");
        assertTrue(hr.isBlock());
        assertFalse(hr.isInline());
        assertTrue(hr.formatAsBlock());
        assertFalse(hr.canContainBlock());
        assertTrue(hr.isEmpty());
        assertTrue(hr.isSelfClosing());
    }

    @Test
    public void testPreserveWhitespaceTags() {
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.preserveWhitespace());
        assertTrue(pre.isBlock());
        assertFalse(pre.formatAsBlock());

        Tag title = Tag.valueOf("title");
        assertTrue(title.preserveWhitespace());
        assertTrue(title.isBlock());
        assertFalse(title.formatAsBlock());

        Tag plaintext = Tag.valueOf("plaintext");
        assertTrue(plaintext.preserveWhitespace());
    }

    @Test
    public void testUnknownTags() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertTrue(custom.formatAsBlock());
        assertTrue(custom.canContainBlock());
        assertFalse(custom.isEmpty());
        assertFalse(custom.isSelfClosing());
        assertFalse(custom.preserveWhitespace());
        assertFalse(custom.isData());
        assertFalse(custom.isKnownTag());
        assertFalse(Tag.isKnownTag("custom-tag"));

        Tag customUpper = Tag.valueOf("  CUSTOM-TAG  ");
        assertEquals("custom-tag", customUpper.getName());
        assertEquals(custom, customUpper);
    }

    @Test
    public void testSetSelfClosing() {
        Tag custom = Tag.valueOf("my-element");
        assertFalse(custom.isSelfClosing());

        Tag returned = custom.setSelfClosing();
        assertSame(custom, returned);
        assertTrue(custom.isSelfClosing());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNull() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfEmpty() {
        Tag.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfWhitespaceOnly() {
        Tag.valueOf("   ");
    }

    @Test
    public void testIsKnownTag() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("b"));
        assertTrue(Tag.isKnownTag("p"));
        assertFalse(Tag.isKnownTag("DIV")); // map keys are lowercase
        assertFalse(Tag.isKnownTag("unknownTag"));
        assertFalse(Tag.isKnownTag(""));
    }

    @Test
    public void testEqualsAndHashCode() {
        Tag tag1 = Tag.valueOf("foo");
        Tag tag2 = Tag.valueOf("FOO");
        Tag tag3 = Tag.valueOf("bar");

        // Identity and null checks
        assertEquals(tag1, tag1);
        assertNotEquals(tag1, null);
        assertNotEquals(tag1, "not-a-tag");

        // Custom tags value equality
        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());

        // Different names
        assertNotEquals(tag1, tag3);
        assertNotEquals(tag1.hashCode(), tag3.hashCode());

        // Known tags equality
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("DIV");
        assertSame(div1, div2);
        assertEquals(div1, div2);
        assertEquals(div1.hashCode(), div2.hashCode());

        // Comparison between block and inline
        Tag span = Tag.valueOf("span");
        assertNotEquals(div1, span);

        // Comparison when selfClosing changes
        Tag custom1 = Tag.valueOf("custom");
        Tag custom2 = Tag.valueOf("custom");
        custom2.setSelfClosing();
        assertNotEquals(custom1, custom2);
        assertNotEquals(custom1.hashCode(), custom2.hashCode());

        // Comparison with different formatting / whitespace properties
        Tag p = Tag.valueOf("p");
        assertNotEquals(div1, p); // diff in formatAsBlock

        Tag pre = Tag.valueOf("pre");
        assertNotEquals(p, pre); // diff in preserveWhitespace

        Tag img = Tag.valueOf("img");
        assertNotEquals(span, img); // diff in empty & canContainInline
    }

    @Test
    public void testToString() {
        assertEquals("div", Tag.valueOf("div").toString());
        assertEquals("custom", Tag.valueOf("CUSTOM").toString());
    }
}
