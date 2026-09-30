package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void testValueOfPredefined() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("P");
        Tag p3 = Tag.valueOf("  p  ");

        assertSame(p1, p2);
        assertSame(p1, p3);
        assertEquals("p", p1.getName());
        assertEquals("p", p1.toString());
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
    public void testValueOfUnknownTag() {
        Tag custom1 = Tag.valueOf("customtag");
        Tag custom2 = Tag.valueOf("customtag");

        assertNotSame(custom1, custom2);
        assertEquals(custom1, custom2);
        assertEquals(custom1.hashCode(), custom2.hashCode());
        assertEquals("customtag", custom1.getName());
        assertFalse(custom1.isBlock());
        assertTrue(custom1.isInline());
        assertTrue(custom1.canContainBlock());
        assertFalse(custom1.isEmpty());
        assertFalse(custom1.isData());
        assertFalse(custom1.preserveWhitespace());
        assertEquals(Tag.valueOf("body"), custom1.getImplicitParent());
    }

    @Test
    public void testBlockAndInlineProperties() {
        Tag div = Tag.valueOf("div");
        assertTrue(div.isBlock());
        assertFalse(div.isInline());
        assertTrue(div.canContainBlock());

        Tag span = Tag.valueOf("span");
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
        assertFalse(span.canContainBlock());

        Tag p = Tag.valueOf("p");
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertFalse(p.canContainBlock());
    }

    @Test
    public void testEmptyAndDataTags() {
        Tag img = Tag.valueOf("img");
        assertTrue(img.isEmpty());
        assertFalse(img.isData());
        assertFalse(img.isBlock());
        assertTrue(img.isInline());

        Tag script = Tag.valueOf("script");
        assertFalse(script.isEmpty());
        assertTrue(script.isData());
        assertTrue(script.preserveWhitespace());

        Tag pre = Tag.valueOf("pre");
        assertFalse(pre.isEmpty());
        assertFalse(pre.isData());
        assertTrue(pre.preserveWhitespace());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanContainNull() {
        Tag div = Tag.valueOf("div");
        div.canContain(null);
    }

    @Test
    public void testCanContainBlockRules() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");

        // P cannot contain block elements
        assertFalse(p.canContain(div));
        // P can contain inline elements
        assertTrue(p.canContain(span));
        // Div can contain both block and inline
        assertTrue(div.canContain(p));
        assertTrue(div.canContain(span));
    }

    @Test
    public void testCanContainInlineRules() {
        Tag script = Tag.valueOf("script");
        Tag span = Tag.valueOf("span");

        // Script cannot contain inline elements
        assertFalse(script.canContain(span));
    }

    @Test
    public void testCanContainOptionalClosingSelf() {
        Tag a = Tag.valueOf("a");
        Tag form = Tag.valueOf("form");
        Tag li = Tag.valueOf("li");
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        Tag tr = Tag.valueOf("tr");

        // Tags with optional closing cannot contain themselves directly
        assertFalse(a.canContain(a));
        assertFalse(form.canContain(form));
        assertFalse(li.canContain(li));
        assertFalse(dt.canContain(dt));
        assertFalse(dd.canContain(dd));
        assertFalse(tr.canContain(tr));
    }

    @Test
    public void testCanContainEmptyAndData() {
        Tag img = Tag.valueOf("img");
        Tag hr = Tag.valueOf("hr");
        Tag script = Tag.valueOf("script");
        Tag title = Tag.valueOf("title");
        Tag span = Tag.valueOf("span");

        // Empty tags cannot contain anything
        assertFalse(img.canContain(span));
        assertFalse(hr.canContain(span));

        // Data tags cannot contain anything
        assertFalse(script.canContain(span));
        assertFalse(title.canContain(span));
    }

    @Test
    public void testCanContainHeadSpecialCases() {
        Tag head = Tag.valueOf("head");

        // Head can only contain specific elements
        assertTrue(head.canContain(Tag.valueOf("base")));
        assertTrue(head.canContain(Tag.valueOf("script")));
        assertTrue(head.canContain(Tag.valueOf("noscript")));
        assertTrue(head.canContain(Tag.valueOf("link")));
        assertTrue(head.canContain(Tag.valueOf("meta")));
        assertTrue(head.canContain(Tag.valueOf("title")));
        assertTrue(head.canContain(Tag.valueOf("style")));
        assertTrue(head.canContain(Tag.valueOf("object")));

        // Head cannot contain other elements
        assertFalse(head.canContain(Tag.valueOf("div")));
        assertFalse(head.canContain(Tag.valueOf("p")));
        assertFalse(head.canContain(Tag.valueOf("span")));
        assertFalse(head.canContain(Tag.valueOf("a")));
    }

    @Test
    public void testCanContainDtDdExclusions() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        Tag span = Tag.valueOf("span");

        assertFalse(dt.canContain(dd));
        assertFalse(dd.canContain(dt));
        assertTrue(dt.canContain(span));
        assertTrue(dd.canContain(span));
    }

    @Test
    public void testGetImplicitParent() {
        assertEquals(Tag.valueOf("html"), Tag.valueOf("body").getImplicitParent());
        assertEquals(Tag.valueOf("html"), Tag.valueOf("head").getImplicitParent());
        assertEquals(Tag.valueOf("ul"), Tag.valueOf("li").getImplicitParent());
        assertEquals(Tag.valueOf("tr"), Tag.valueOf("td").getImplicitParent());
        assertEquals(Tag.valueOf("table"), Tag.valueOf("tr").getImplicitParent());
        assertEquals(Tag.valueOf("head"), Tag.valueOf("meta").getImplicitParent());

        // html has no ancestors
        assertNull(Tag.valueOf("html").getImplicitParent());
    }

    @Test
    public void testIsValidParent() {
        Tag html = Tag.valueOf("html");
        Tag body = Tag.valueOf("body");
        Tag head = Tag.valueOf("head");
        Tag div = Tag.valueOf("div");
        Tag ul = Tag.valueOf("ul");
        Tag ol = Tag.valueOf("ol");
        Tag li = Tag.valueOf("li");
        Tag script = Tag.valueOf("script");

        // HTML has no ancestors, so any tag is valid parent
        assertTrue(div.isValidParent(html));
        assertTrue(body.isValidParent(html));

        // Body must have HTML parent
        assertTrue(html.isValidParent(body));
        assertFalse(div.isValidParent(body));

        // Li can be in UL or OL
        assertTrue(ul.isValidParent(li));
        assertTrue(ol.isValidParent(li));
        assertFalse(div.isValidParent(li));

        // Script can be in HEAD or BODY
        assertTrue(head.isValidParent(script));
        assertTrue(body.isValidParent(script));
        assertFalse(ul.isValidParent(script));
    }

    @Test
    public void testEqualsAndHashCode() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("DIV");
        Tag tag3 = Tag.valueOf("span");
        Tag custom1 = Tag.valueOf("custom1");
        Tag custom2 = Tag.valueOf("custom1");
        Tag custom3 = Tag.valueOf("custom2");

        // Reflexive
        assertTrue(tag1.equals(tag1));

        // Symmetric
        assertTrue(tag1.equals(tag2));
        assertTrue(tag2.equals(tag1));
        assertEquals(tag1.hashCode(), tag2.hashCode());

        // Different tags
        assertFalse(tag1.equals(tag3));
        assertFalse(tag1.equals(null));
        assertFalse(tag1.equals("div"));

        // Custom tags
        assertTrue(custom1.equals(custom2));
        assertEquals(custom1.hashCode(), custom2.hashCode());
        assertFalse(custom1.equals(custom3));
    }

    @Test
    public void testPredefinedTagProperties() {
        // Test various pre-defined tags to ensure static initializers executed properly
        Tag br = Tag.valueOf("br");
        assertTrue(br.isEmpty());
        assertTrue(br.isInline());

        Tag textarea = Tag.valueOf("textarea");
        assertTrue(textarea.isData());
        assertTrue(textarea.preserveWhitespace());

        Tag form = Tag.valueOf("form");
        assertTrue(form.isBlock());

        Tag table = Tag.valueOf("table");
        assertTrue(table.isBlock());
        assertTrue(table.canContainBlock());

        Tag frameset = Tag.valueOf("frameset");
        assertTrue(frameset.isBlock());

        Tag frame = Tag.valueOf("frame");
        assertTrue(frame.isEmpty());
    }
}
