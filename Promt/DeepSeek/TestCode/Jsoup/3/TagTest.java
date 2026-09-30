package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void testValueOfNull() {
        try {
            Tag.valueOf(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testValueOfEmpty() {
        try {
            Tag.valueOf("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValueOfWhitespace() {
        try {
            Tag.valueOf("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValueOfKnownTagReturnsSameInstance() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertSame(p1, p2);
    }

    @Test
    public void testValueOfCaseInsensitive() {
        Tag pLower = Tag.valueOf("p");
        Tag pUpper = Tag.valueOf("P");
        assertSame(pLower, pUpper);
    }

    @Test
    public void testValueOfUnknownTag() {
        Tag unknown = Tag.valueOf("unknown");
        assertNotNull(unknown);
        assertEquals("unknown", unknown.getName());
        assertFalse(unknown.isBlock());
        assertTrue(unknown.canContainBlock());
        assertTrue(unknown.isInline());
        assertFalse(unknown.isData());
        assertFalse(unknown.isEmpty());
        assertFalse(unknown.preserveWhitespace());
        // ancestors should contain BODY
        Tag body = Tag.valueOf("body");
        assertTrue(unknown.isValidParent(body));
    }

    @Test
    public void testValueOfUnknownTagNotSame() {
        Tag u1 = Tag.valueOf("unknown");
        Tag u2 = Tag.valueOf("unknown");
        assertNotSame(u1, u2);
        assertEquals(u1, u2);
    }

    @Test
    public void testGetName() {
        assertEquals("p", Tag.valueOf("P").getName());
        assertEquals("div", Tag.valueOf("DIV").getName());
    }

    @Test
    public void testIsBlockForBlockTag() {
        assertTrue(Tag.valueOf("p").isBlock());
        assertTrue(Tag.valueOf("div").isBlock());
    }

    @Test
    public void testIsBlockForInlineTag() {
        assertFalse(Tag.valueOf("a").isBlock());
        assertFalse(Tag.valueOf("span").isBlock());
    }

    @Test
    public void testCanContainBlockForBlockTag() {
        assertFalse(Tag.valueOf("p").canContainBlock()); // setContainInlineOnly
        assertTrue(Tag.valueOf("div").canContainBlock());
    }

    @Test
    public void testCanContainBlockForInlineTag() {
        assertFalse(Tag.valueOf("a").canContainBlock());
    }

    @Test
    public void testIsInline() {
        assertFalse(Tag.valueOf("p").isInline());
        assertTrue(Tag.valueOf("a").isInline());
    }

    @Test
    public void testIsDataForDataTag() {
        assertTrue(Tag.valueOf("script").isData());
        assertTrue(Tag.valueOf("style").isData());
    }

    @Test
    public void testIsDataForNonDataTag() {
        assertFalse(Tag.valueOf("p").isData());
        assertFalse(Tag.valueOf("div").isData());
    }

    @Test
    public void testIsEmptyForEmptyTag() {
        assertTrue(Tag.valueOf("img").isEmpty());
        assertTrue(Tag.valueOf("br").isEmpty());
    }

    @Test
    public void testIsEmptyForNonEmptyTag() {
        assertFalse(Tag.valueOf("p").isEmpty());
    }

    @Test
    public void testPreserveWhitespace() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
        assertTrue(Tag.valueOf("script").preserveWhitespace());
        assertFalse(Tag.valueOf("p").preserveWhitespace());
    }

    @Test(expected = NullPointerException.class)
    public void testCanContainNullChild() {
        Tag.valueOf("div").canContain(null);
    }

    @Test
    public void testCanContainBlockInInlineOnlyParent() {
        Tag p = Tag.valueOf("p"); // canContainBlock=false
        Tag div = Tag.valueOf("div"); // block
        assertFalse(p.canContain(div));
    }

    @Test
    public void testCanContainInlineInBlockOnlyParent() {
        // No tag has canContainInline=false and canContainBlock=true? Actually data-only tags have canContainInline=false.
        Tag script = Tag.valueOf("script"); // canContainInline=false
        Tag span = Tag.valueOf("span"); // inline
        assertFalse(script.canContain(span));
    }

    @Test
    public void testCanContainOptionalClosingSelf() {
        Tag a = Tag.valueOf("a"); // optionalClosing=true
        assertFalse(a.canContain(a));
    }

    @Test
    public void testCanContainEmptyParent() {
        Tag img = Tag.valueOf("img"); // empty
        Tag span = Tag.valueOf("span");
        assertFalse(img.canContain(span));
    }

    @Test
    public void testCanContainDataParent() {
        Tag script = Tag.valueOf("script"); // isData=true
        Tag span = Tag.valueOf("span");
        assertFalse(script.canContain(span));
    }

    @Test
    public void testCanContainHeadValidChildren() {
        Tag head = Tag.valueOf("head");
        assertTrue(head.canContain(Tag.valueOf("base")));
        assertTrue(head.canContain(Tag.valueOf("script")));
        assertTrue(head.canContain(Tag.valueOf("noscript")));
        assertTrue(head.canContain(Tag.valueOf("link")));
        assertTrue(head.canContain(Tag.valueOf("meta")));
        assertTrue(head.canContain(Tag.valueOf("title")));
        assertTrue(head.canContain(Tag.valueOf("style")));
        assertTrue(head.canContain(Tag.valueOf("object")));
    }

    @Test
    public void testCanContainHeadInvalidChild() {
        Tag head = Tag.valueOf("head");
        assertFalse(head.canContain(Tag.valueOf("p")));
        assertFalse(head.canContain(Tag.valueOf("div")));
    }

    @Test
    public void testCanContainDtCannotContainDd() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        assertFalse(dt.canContain(dd));
    }

    @Test
    public void testCanContainDdCannotContainDt() {
        Tag dd = Tag.valueOf("dd");
        Tag dt = Tag.valueOf("dt");
        assertFalse(dd.canContain(dt));
    }

    @Test
    public void testCanContainValid() {
        Tag div = Tag.valueOf("div");
        Tag p = Tag.valueOf("p");
        assertTrue(div.canContain(p));
    }

    @Test
    public void testGetImplicitParentForKnownTag() {
        Tag li = Tag.valueOf("li");
        Tag ul = Tag.valueOf("ul");
        assertEquals(ul, li.getImplicitParent());
    }

    @Test
    public void testGetImplicitParentForUnknownTag() {
        Tag unknown = Tag.valueOf("unknown");
        Tag body = Tag.valueOf("body");
        assertEquals(body, unknown.getImplicitParent());
    }

    @Test
    public void testGetImplicitParentForHtml() {
        Tag html = Tag.valueOf("html");
        assertNull(html.getImplicitParent());
    }

    @Test
    public void testIsValidParentEmptyAncestors() {
        Tag html = Tag.valueOf("html"); // ancestors empty
        assertTrue(Tag.valueOf("div").isValidParent(html));
    }

    @Test
    public void testIsValidParentMatchingAncestor() {
        Tag ul = Tag.valueOf("ul");
        Tag li = Tag.valueOf("li"); // ancestors: UL, OL
        assertTrue(ul.isValidParent(li));
    }

    @Test
    public void testIsValidParentNonMatchingAncestor() {
        Tag div = Tag.valueOf("div");
        Tag li = Tag.valueOf("li");
        assertFalse(div.isValidParent(li));
    }

    @Test
    public void testEqualsSameObject() {
        Tag p = Tag.valueOf("p");
        assertTrue(p.equals(p));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(Tag.valueOf("p").equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(Tag.valueOf("p").equals("p"));
    }

    @Test
    public void testEqualsSameProperties() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentTagName() {
        assertFalse(Tag.valueOf("p").equals(Tag.valueOf("div")));
    }

    @Test
    public void testEqualsDifferentIsBlock() {
        // p is block, a is inline
        assertFalse(Tag.valueOf("p").equals(Tag.valueOf("a")));
    }

    @Test
    public void testEqualsDifferentCanContainBlock() {
        // p canContainBlock=false, div canContainBlock=true
        assertFalse(Tag.valueOf("p").equals(Tag.valueOf("div")));
    }

    @Test
    public void testEqualsDifferentCanContainInline() {
        // script canContainInline=false, p canContainInline=true
        assertFalse(Tag.valueOf("script").equals(Tag.valueOf("p")));
    }

    @Test
    public void testEqualsDifferentEmpty() {
        assertFalse(Tag.valueOf("img").equals(Tag.valueOf("p")));
    }

    @Test
    public void testEqualsDifferentOptionalClosing() {
        // a has optionalClosing, span does not
        assertFalse(Tag.valueOf("a").equals(Tag.valueOf("span")));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("p", Tag.valueOf("p").toString());
        assertEquals("div", Tag.valueOf("div").toString());
    }
}
