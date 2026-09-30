package com.fasterxml.jackson.core;

import static org.junit.Assert.*;
import org.junit.Test;

public class JsonPointerTest
{
    @Test
    public void testCompileNull() {
        JsonPointer ptr = JsonPointer.compile(null);
        assertSame(JsonPointer.EMPTY, ptr);
        assertTrue(ptr.matches());
    }

    @Test
    public void testCompileEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertSame(JsonPointer.EMPTY, ptr);
        assertTrue(ptr.matches());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileInvalidNoSlash() {
        JsonPointer.compile("foo");
    }

    @Test
    public void testCompileSimpleProperty() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.matches());
        assertEquals("/foo", ptr.toString());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
    }

    @Test
    public void testCompileNumericIndex() {
        JsonPointer ptr = JsonPointer.compile("/0");
        assertEquals("0", ptr.getMatchingProperty());
        assertEquals(0, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertTrue(ptr.mayMatchElement());

        ptr = JsonPointer.compile("/123");
        assertEquals("123", ptr.getMatchingProperty());
        assertEquals(123, ptr.getMatchingIndex());

        ptr = JsonPointer.compile("/01");
        assertEquals("01", ptr.getMatchingProperty());
        assertEquals(1, ptr.getMatchingIndex());
    }

    @Test
    public void testCompileIndexOverflow() {
        JsonPointer ptr = JsonPointer.compile("/9999999999");
        assertEquals("9999999999", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());

        ptr = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
    }

    @Test
    public void testCompileRootOnly() {
        JsonPointer ptr = JsonPointer.compile("/");
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.matches());
        assertEquals(JsonPointer.EMPTY, ptr.tail());
        assertTrue(ptr.matchProperty("").matches());
    }

    @Test
    public void testCompileMultipleSegments() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c");
        assertEquals("/a/b/c", ptr.toString());
        assertFalse(ptr.matches());
        assertEquals("a", ptr.getMatchingProperty());
        JsonPointer tail = ptr.matchProperty("a");
        assertNotNull(tail);
        assertEquals("/b/c", tail.toString());
        assertEquals("b", tail.getMatchingProperty());
        JsonPointer tail2 = tail.matchProperty("b");
        assertNotNull(tail2);
        assertEquals("/c", tail2.toString());
        assertEquals("c", tail2.getMatchingProperty());
        JsonPointer tail3 = tail2.matchProperty("c");
        assertSame(JsonPointer.EMPTY, tail3);
        assertTrue(tail3.matches());
    }

    @Test
    public void testTail() {
        JsonPointer ptr = JsonPointer.compile("/a/b");
        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertEquals("/b", tail.toString());
        tail = tail.tail();
        assertSame(JsonPointer.EMPTY, tail);
        assertNull(JsonPointer.EMPTY.tail());
    }

    @Test
    public void testMatches() {
        assertTrue(JsonPointer.EMPTY.matches());
        assertFalse(JsonPointer.compile("/a").matches());
    }

    @Test
    public void testMatchProperty() {
        JsonPointer ptr = JsonPointer.compile("/a/b");
        assertNotNull(ptr.matchProperty("a"));
        assertNull(ptr.matchProperty("b"));
        assertNull(ptr.matchProperty("A"));
        assertNull(JsonPointer.EMPTY.matchProperty("anything"));
    }

    @Test
    public void testMatchElement() {
        JsonPointer ptr = JsonPointer.compile("/0/1");
        JsonPointer next = ptr.matchElement(0);
        assertNotNull(next);
        assertEquals("/1", next.toString());
        JsonPointer last = next.matchElement(1);
        assertSame(JsonPointer.EMPTY, last);
        assertNull(ptr.matchElement(-1));
        assertNull(ptr.matchElement(1));
        assertNull(JsonPointer.EMPTY.matchElement(0));
    }

    @Test
    public void testMayMatchPropertyAndElement() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        ptr = JsonPointer.compile("/0");
        assertTrue(ptr.mayMatchProperty());
        assertTrue(ptr.mayMatchElement());
    }

    @Test
    public void testEscapedTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0");
        assertEquals("~", ptr.getMatchingProperty());
        assertNotNull(ptr.matchProperty("~"));
        ptr = JsonPointer.compile("/a~0b");
        assertEquals("a~b", ptr.getMatchingProperty());
    }

    @Test
    public void testEscapedSlash() {
        JsonPointer ptr = JsonPointer.compile("/~1");
        assertEquals("/", ptr.getMatchingProperty());
        assertNotNull(ptr.matchProperty("/"));
        ptr = JsonPointer.compile("/a~1b");
        assertEquals("a/b", ptr.getMatchingProperty());
    }

    @Test
    public void testEscapedCombination() {
        JsonPointer ptr = JsonPointer.compile("/a~0b~1c");
        assertEquals("a~b/c", ptr.getMatchingProperty());
        assertNotNull(ptr.matchProperty("a~b/c"));
    }

    @Test
    public void testEscapedLoneTildeAtEnd() {
        JsonPointer ptr = JsonPointer.compile("/~");
        assertEquals("~", ptr.getMatchingProperty());
        assertNotNull(ptr.matchProperty("~"));
    }

    @Test
    public void testEscapedInvalidEscape() {
        JsonPointer ptr = JsonPointer.compile("/~2");
        assertEquals("~2", ptr.getMatchingProperty());
    }

    @Test
    public void testEscapedInsideSegment() {
        JsonPointer ptr = JsonPointer.compile("/a/~0");
        assertEquals("a", ptr.getMatchingProperty());
        JsonPointer tail = ptr.matchProperty("a");
        assertEquals("~", tail.getMatchingProperty());
    }

    @Test
    public void testDoubleSlash() {
        JsonPointer ptr = JsonPointer.compile("//");
        assertEquals("", ptr.getMatchingProperty());
        JsonPointer tail = ptr.matchProperty("");
        assertNotNull(tail);
        assertEquals("/", tail.toString());
        assertEquals("", tail.getMatchingProperty());
        assertTrue(tail.matchProperty("").matches());
    }

    @Test
    public void testValueOf() {
        JsonPointer ptr = JsonPointer.valueOf("/foo/bar");
        assertEquals(JsonPointer.compile("/foo/bar"), ptr);
    }

    @Test
    public void testToString() {
        assertEquals("", JsonPointer.EMPTY.toString());
        assertEquals("/a", JsonPointer.compile("/a").toString());
        assertEquals("/a/b", JsonPointer.compile("/a/b").toString());
    }

    @Test
    public void testHashCode() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/foo");
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
        assertEquals(JsonPointer.EMPTY.hashCode(), "".hashCode());
    }

    @Test
    public void testEquals() {
        JsonPointer ptr1 = JsonPointer.compile("/foo/bar");
        JsonPointer ptr2 = JsonPointer.compile("/foo/bar");
        assertTrue(ptr1.equals(ptr2));
        assertTrue(ptr2.equals(ptr1));
        assertFalse(ptr1.equals(JsonPointer.compile("/foo")));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("/foo/bar"));
        assertTrue(ptr1.equals(ptr1));
        assertTrue(JsonPointer.EMPTY.equals(JsonPointer.EMPTY));
        assertTrue(JsonPointer.EMPTY.equals(JsonPointer.compile("")));
    }

    @Test
    public void testGettersOnEmpty() {
        assertEquals("", JsonPointer.EMPTY.getMatchingProperty());
        assertEquals(-1, JsonPointer.EMPTY.getMatchingIndex());
        assertTrue(JsonPointer.EMPTY.mayMatchProperty());
        assertFalse(JsonPointer.EMPTY.mayMatchElement());
    }

    @Test
    public void testMatchElementWithIndexNegativeMatch() {
        JsonPointer ptr = JsonPointer.compile("/-1");
        assertEquals(-1, ptr.getMatchingIndex());
        assertNull(ptr.matchElement(-1));
    }

    @Test
    public void testParseTailWithMultipleSlashes() {
        JsonPointer ptr = JsonPointer.compile("/a//b");
        assertEquals("a", ptr.getMatchingProperty());
        JsonPointer tail = ptr.matchProperty("a");
        assertNotNull(tail);
        assertEquals("//b", tail.toString());
        assertEquals("", tail.getMatchingProperty());
        JsonPointer tail2 = tail.matchProperty("");
        assertNotNull(tail2);
        assertEquals("/b", tail2.toString());
        assertEquals("b", tail2.getMatchingProperty());
    }

    @Test
    public void testParseQuotedTailWithTildeInMiddle() {
        JsonPointer ptr = JsonPointer.compile("/a~0/b");
        assertEquals("a~", ptr.getMatchingProperty());
        ptr = JsonPointer.compile("/a~1/b");
        assertEquals("a/", ptr.getMatchingProperty());
    }
}
