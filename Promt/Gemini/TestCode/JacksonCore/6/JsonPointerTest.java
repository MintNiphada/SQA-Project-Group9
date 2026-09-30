package com.fasterxml.jackson.core;

import org.junit.Assert;
import org.junit.Test;

public class JsonPointerTest {

    @Test
    public void testEmptyAndNull() {
        JsonPointer ptrNull = JsonPointer.compile(null);
        Assert.assertNotNull(ptrNull);
        Assert.assertSame(JsonPointer.EMPTY, ptrNull);
        Assert.assertTrue(ptrNull.matches());
        Assert.assertEquals("", ptrNull.getMatchingProperty());
        Assert.assertEquals(-1, ptrNull.getMatchingIndex());
        Assert.assertFalse(ptrNull.mayMatchElement());
        Assert.assertTrue(ptrNull.mayMatchProperty());
        Assert.assertNull(ptrNull.tail());
        Assert.assertEquals("", ptrNull.toString());

        JsonPointer ptrEmpty = JsonPointer.compile("");
        Assert.assertSame(JsonPointer.EMPTY, ptrEmpty);

        JsonPointer ptrValueOfNull = JsonPointer.valueOf(null);
        Assert.assertSame(JsonPointer.EMPTY, ptrValueOfNull);

        JsonPointer ptrValueOfEmpty = JsonPointer.valueOf("");
        Assert.assertSame(JsonPointer.EMPTY, ptrValueOfEmpty);
    }

    @Test
    public void testInvalidPointers() {
        try {
            JsonPointer.compile("a");
            Assert.fail("Expected IllegalArgumentException for pointer not starting with '/'");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("must start with '/'"));
        }

        try {
            JsonPointer.compile(" /");
            Assert.fail("Expected IllegalArgumentException for pointer not starting with '/'");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("must start with '/'"));
        }

        try {
            JsonPointer.valueOf("invalid");
            Assert.fail("Expected IllegalArgumentException for pointer not starting with '/'");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("must start with '/'"));
        }
    }

    @Test
    public void testSimplePropertyPointers() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        Assert.assertFalse(ptr.matches());
        Assert.assertEquals("foo", ptr.getMatchingProperty());
        Assert.assertEquals(-1, ptr.getMatchingIndex());
        Assert.assertTrue(ptr.mayMatchProperty());
        Assert.assertFalse(ptr.mayMatchElement());
        Assert.assertEquals("/foo", ptr.toString());

        JsonPointer tail = ptr.tail();
        Assert.assertNotNull(tail);
        Assert.assertSame(JsonPointer.EMPTY, tail);
        Assert.assertTrue(tail.matches());

        JsonPointer matched = ptr.matchProperty("foo");
        Assert.assertSame(JsonPointer.EMPTY, matched);

        Assert.assertNull(ptr.matchProperty("bar"));
        Assert.assertNull(ptr.matchElement(0));
    }

    @Test
    public void testNestedProperties() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar/baz");
        Assert.assertEquals("/foo/bar/baz", ptr.toString());
        Assert.assertEquals("foo", ptr.getMatchingProperty());

        JsonPointer next = ptr.matchProperty("foo");
        Assert.assertNotNull(next);
        Assert.assertEquals("/bar/baz", next.toString());
        Assert.assertEquals("bar", next.getMatchingProperty());

        JsonPointer next2 = next.matchProperty("bar");
        Assert.assertNotNull(next2);
        Assert.assertEquals("/baz", next2.toString());
        Assert.assertEquals("baz", next2.getMatchingProperty());

        JsonPointer leaf = next2.matchProperty("baz");
        Assert.assertNotNull(leaf);
        Assert.assertSame(JsonPointer.EMPTY, leaf);
        Assert.assertTrue(leaf.matches());
    }

    @Test
    public void testRootSlashPointer() {
        JsonPointer ptr = JsonPointer.compile("/");
        Assert.assertFalse(ptr.matches());
        Assert.assertEquals("", ptr.getMatchingProperty());
        Assert.assertEquals(-1, ptr.getMatchingIndex());
        Assert.assertSame(JsonPointer.EMPTY, ptr.tail());

        JsonPointer next = ptr.matchProperty("");
        Assert.assertSame(JsonPointer.EMPTY, next);
    }

    @Test
    public void testDoubleSlashPointer() {
        JsonPointer ptr = JsonPointer.compile("//");
        Assert.assertFalse(ptr.matches());
        Assert.assertEquals("", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        Assert.assertNotNull(tail);
        Assert.assertEquals("/", tail.toString());
        Assert.assertEquals("", tail.getMatchingProperty());
        Assert.assertSame(JsonPointer.EMPTY, tail.tail());
    }

    @Test
    public void testElementIndexParsing() {
        JsonPointer ptr0 = JsonPointer.compile("/0");
        Assert.assertEquals(0, ptr0.getMatchingIndex());
        Assert.assertTrue(ptr0.mayMatchElement());
        Assert.assertSame(JsonPointer.EMPTY, ptr0.matchElement(0));
        Assert.assertNull(ptr0.matchElement(1));
        Assert.assertNull(ptr0.matchElement(-1));

        JsonPointer ptr123 = JsonPointer.compile("/123");
        Assert.assertEquals(123, ptr123.getMatchingIndex());
        Assert.assertTrue(ptr123.mayMatchElement());
        Assert.assertSame(JsonPointer.EMPTY, ptr123.matchElement(123));

        JsonPointer ptrMaxInt = JsonPointer.compile("/" + Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, ptrMaxInt.getMatchingIndex());
        Assert.assertTrue(ptrMaxInt.mayMatchElement());
        Assert.assertSame(JsonPointer.EMPTY, ptrMaxInt.matchElement(Integer.MAX_VALUE));

        // Beyond Integer.MAX_VALUE with length 10
        long overflow10 = ((long) Integer.MAX_VALUE) + 1L;
        JsonPointer ptrOverflow10 = JsonPointer.compile("/" + overflow10);
        Assert.assertEquals(-1, ptrOverflow10.getMatchingIndex());
        Assert.assertFalse(ptrOverflow10.mayMatchElement());

        JsonPointer ptr999 = JsonPointer.compile("/9999999999");
        Assert.assertEquals(-1, ptr999.getMatchingIndex());
        Assert.assertFalse(ptr999.mayMatchElement());

        // Length > 10
        JsonPointer ptrLen11 = JsonPointer.compile("/10000000000");
        Assert.assertEquals(-1, ptrLen11.getMatchingIndex());
        Assert.assertFalse(ptrLen11.mayMatchElement());

        // Non-digit characters
        JsonPointer ptrNeg = JsonPointer.compile("/-1");
        Assert.assertEquals(-1, ptrNeg.getMatchingIndex());
        Assert.assertFalse(ptrNeg.mayMatchElement());

        JsonPointer ptrAlpha = JsonPointer.compile("/12a34");
        Assert.assertEquals(-1, ptrAlpha.getMatchingIndex());
        Assert.assertFalse(ptrAlpha.mayMatchElement());

        JsonPointer ptrLeadingNonDigit = JsonPointer.compile("/a123");
        Assert.assertEquals(-1, ptrLeadingNonDigit.getMatchingIndex());

        JsonPointer ptrTrailingNonDigit = JsonPointer.compile("/123a");
        Assert.assertEquals(-1, ptrTrailingNonDigit.getMatchingIndex());
    }

    @Test
    public void testEscapedTildeAndSlash() {
        // ~0 unescapes to ~
        JsonPointer ptrTilde = JsonPointer.compile("/~0");
        Assert.assertEquals("~", ptrTilde.getMatchingProperty());

        // ~1 unescapes to /
        JsonPointer ptrSlash = JsonPointer.compile("/~1");
        Assert.assertEquals("/", ptrSlash.getMatchingProperty());

        // Unrecognized escape sequence ~2 unescapes to ~2
        JsonPointer ptrOtherEscape = JsonPointer.compile("/~2");
        Assert.assertEquals("~2", ptrOtherEscape.getMatchingProperty());

        // Escaped sequence with prefix: i > 2 branch
        JsonPointer ptrPrefix = JsonPointer.compile("/abc~0def");
        Assert.assertEquals("abc~def", ptrPrefix.getMatchingProperty());

        JsonPointer ptrSlashInSegment = JsonPointer.compile("/a~1b");
        Assert.assertEquals("a/b", ptrSlashInSegment.getMatchingProperty());

        // Multiple escapes in one segment
        JsonPointer ptrMultiEscape = JsonPointer.compile("/~0~1~2");
        Assert.assertEquals("~/~2", ptrMultiEscape.getMatchingProperty());

        // Escaped segment followed by another segment
        JsonPointer ptrChained = JsonPointer.compile("/~0/next");
        Assert.assertEquals("~", ptrChained.getMatchingProperty());
        Assert.assertEquals("next", ptrChained.tail().getMatchingProperty());

        JsonPointer ptrChainedPrefix = JsonPointer.compile("/prefix~1suffix/next");
        Assert.assertEquals("prefix/suffix", ptrChainedPrefix.getMatchingProperty());
        Assert.assertEquals("next", ptrChainedPrefix.tail().getMatchingProperty());

        JsonPointer ptrMultipleTildes = JsonPointer.compile("/a~0b~1c/d~0e");
        Assert.assertEquals("a~b/c", ptrMultipleTildes.getMatchingProperty());
        Assert.assertEquals("d~e", ptrMultipleTildes.tail().getMatchingProperty());
    }

    @Test
    public void testTildeAtEndOfInput() {
        // Tilde at end of input when entering from _parseTail (i < end is false)
        JsonPointer ptr1 = JsonPointer.compile("/~");
        Assert.assertEquals("~", ptr1.getMatchingProperty());

        JsonPointer ptr2 = JsonPointer.compile("/abc/~");
        Assert.assertEquals("abc", ptr2.getMatchingProperty());
        Assert.assertEquals("~", ptr2.tail().getMatchingProperty());

        // Tilde at end inside _parseQuotedTail while loop (i < end is false for second tilde)
        JsonPointer ptr3 = JsonPointer.compile("/~0abc~");
        Assert.assertEquals("~abc~", ptr3.getMatchingProperty());

        JsonPointer ptr4 = JsonPointer.compile("/a~0b~");
        Assert.assertEquals("a~b~", ptr4.getMatchingProperty());
    }

    @Test
    public void testMatchPropertyAndMatchElement() {
        JsonPointer empty = JsonPointer.EMPTY;
        Assert.assertNull(empty.matchProperty("test"));
        Assert.assertNull(empty.matchProperty(""));
        Assert.assertNull(empty.matchElement(0));
        Assert.assertNull(empty.matchElement(-1));

        JsonPointer ptr = JsonPointer.compile("/a/1");
        Assert.assertNull(ptr.matchProperty("b"));
        JsonPointer tail = ptr.matchProperty("a");
        Assert.assertNotNull(tail);
        Assert.assertEquals(1, tail.getMatchingIndex());

        Assert.assertNull(tail.matchElement(0));
        Assert.assertNull(tail.matchElement(-5));
        Assert.assertSame(JsonPointer.EMPTY, tail.matchElement(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        JsonPointer p1 = JsonPointer.compile("/a/b/c");
        JsonPointer p2 = JsonPointer.compile("/a/b/c");
        JsonPointer p3 = JsonPointer.compile("/a/b/d");
        JsonPointer empty1 = JsonPointer.compile("");
        JsonPointer empty2 = JsonPointer.EMPTY;

        // Reflexive
        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(empty1.equals(empty2));

        // Symmetric
        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        // Inequality
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(empty1));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("/a/b/c")); // Different type

        // EMPTY hashCode
        Assert.assertEquals("".hashCode(), empty1.hashCode());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("", JsonPointer.EMPTY.toString());
        Assert.assertEquals("/foo/bar", JsonPointer.compile("/foo/bar").toString());
        Assert.assertEquals("/~0/~1", JsonPointer.compile("/~0/~1").toString());
    }
}
