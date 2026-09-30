package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonPointerTest {

    @Test
    public void testCompileNullInput() {
        assertSame(JsonPointer.EMPTY, JsonPointer.compile(null));
    }

    @Test
    public void testCompileEmptyString() {
        assertSame(JsonPointer.EMPTY, JsonPointer.compile(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileInvalidInputNoSlash() {
        JsonPointer.compile("foo");
    }

    @Test
    public void testCompileValueOfAlias() {
        assertSame(JsonPointer.EMPTY, JsonPointer.valueOf(""));
        assertEquals("/foo", JsonPointer.valueOf("/foo").toString());
    }

    @Test
    public void testEmptyPointerProperties() {
        JsonPointer p = JsonPointer.EMPTY;
        assertTrue(p.matches());
        assertEquals("", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertTrue(p.mayMatchProperty());
        assertFalse(p.mayMatchElement());
        assertNull(p.tail());
        assertNull(p.matchProperty("foo"));
        assertNull(p.matchProperty(null));
        assertNull(p.matchElement(0));
        assertEquals("", p.toString());
    }

    @Test
    public void testCompileSingleSlash() {
        JsonPointer p = JsonPointer.compile("/");
        assertEquals("/", p.toString());
        assertFalse(p.matches());
        assertEquals("", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertTrue(p.mayMatchProperty());
        assertFalse(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.tail());
        assertSame(JsonPointer.EMPTY, p.matchProperty(""));
        assertNull(p.matchProperty("foo"));
        assertNull(p.matchElement(0));
        assertNull(p.matchElement(-1));
    }

    @Test
    public void testCompileSimpleProperty() {
        JsonPointer p = JsonPointer.compile("/foo");
        assertEquals("/foo", p.toString());
        assertFalse(p.matches());
        assertEquals("foo", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertTrue(p.mayMatchProperty());
        assertFalse(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.tail());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo"));
        assertNull(p.matchProperty("foo2"));
        assertNull(p.matchProperty(null));
        assertNull(p.matchElement(0));
    }

    @Test
    public void testCompileNestedProperties() {
        JsonPointer p = JsonPointer.compile("/foo/bar/baz");
        assertEquals("/foo/bar/baz", p.toString());
        assertEquals("foo", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.matches());

        JsonPointer tail = p.tail();
        assertNotNull(tail);
        assertEquals("/bar/baz", tail.toString());
        assertEquals("bar", tail.getMatchingProperty());
        assertNull(tail.matchProperty("foo"));
        assertSame(JsonPointer.EMPTY, tail.matchProperty("bar").tail());

        JsonPointer tail2 = tail.tail();
        assertEquals("/baz", tail2.toString());
        assertEquals("baz", tail2.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, tail2.tail());

        assertSame(JsonPointer.EMPTY, p.matchProperty("foo").matchProperty("bar").matchProperty("baz"));
        assertNull(p.matchProperty("foo").matchProperty("baz"));
    }

    @Test
    public void testCompileNumericSegment() {
        JsonPointer p = JsonPointer.compile("/123");
        assertEquals("/123", p.toString());
        assertEquals("123", p.getMatchingProperty());
        assertEquals(123, p.getMatchingIndex());
        assertTrue(p.mayMatchProperty());
        assertTrue(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.matchElement(123));
        assertNull(p.matchElement(122));
        assertNull(p.matchElement(-1));
        assertSame(JsonPointer.EMPTY, p.matchProperty("123"));
    }

    @Test
    public void testCompileIndexZero() {
        JsonPointer p = JsonPointer.compile("/0");
        assertEquals(0, p.getMatchingIndex());
        assertTrue(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.matchElement(0));
    }

    @Test
    public void testCompileIndexNegative() {
        JsonPointer p = JsonPointer.compile("/-1");
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.mayMatchElement());
        assertNull(p.matchElement(-1));
    }

    @Test
    public void testCompileIndexNonNumeric() {
        JsonPointer p = JsonPointer.compile("/12a");
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.mayMatchElement());
    }

    @Test
    public void testCompileIndexTooLong() {
        JsonPointer p = JsonPointer.compile("/12345678901");
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.mayMatchElement());
    }

    @Test
    public void testCompileIndexMaxInt() {
        JsonPointer p = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, p.getMatchingIndex());
        assertTrue(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.matchElement(2147483647));
        assertNull(p.matchElement(2147483646));
    }

    @Test
    public void testCompileIndexOverflow() {
        JsonPointer p = JsonPointer.compile("/2147483648");
        assertEquals(-1, p.getMatchingIndex());
        assertFalse(p.mayMatchElement());
    }

    @Test
    public void testCompileIndexLeadingZero() {
        JsonPointer p = JsonPointer.compile("/01");
        assertEquals(1, p.getMatchingIndex());
        assertTrue(p.mayMatchElement());
        assertSame(JsonPointer.EMPTY, p.matchElement(1));
    }

    @Test
    public void testCompileEscapedTilde() {
        JsonPointer p = JsonPointer.compile("/~0");
        assertEquals("~", p.getMatchingProperty());
        assertEquals(-1, p.getMatchingIndex());
        assertSame(JsonPointer.EMPTY, p.matchProperty("~"));
    }

    @Test
    public void testCompileEscapedSlash() {
        JsonPointer p = JsonPointer.compile("/~1");
        assertEquals("/", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("/"));
    }

    @Test
    public void testCompileEscapedTildeInProperty() {
        JsonPointer p = JsonPointer.compile("/foo~0bar");
        assertEquals("foo~bar", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo~bar"));
    }

    @Test
    public void testCompileEscapedSlashInProperty() {
        JsonPointer p = JsonPointer.compile("/foo~1bar");
        assertEquals("foo/bar", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo/bar"));
    }

    @Test
    public void testCompileUnknownEscape() {
        JsonPointer p = JsonPointer.compile("/foo~2bar");
        assertEquals("foo~2bar", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo~2bar"));
    }

    @Test
    public void testCompileEscapeAtStart() {
        JsonPointer p = JsonPointer.compile("/~0foo");
        assertEquals("~foo", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("~foo"));
    }

    @Test
    public void testCompileEscapeAtEnd() {
        JsonPointer p = JsonPointer.compile("/foo~0");
        assertEquals("foo~", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo~"));
    }

    @Test
    public void testCompileEscapedSlashSeparator() {
        JsonPointer p = JsonPointer.compile("/foo~1bar/baz");
        assertEquals("foo/bar", p.getMatchingProperty());
        assertNotNull(p.tail());
        assertEquals("/baz", p.tail().toString());
        assertEquals("baz", p.tail().getMatchingProperty());
    }

    @Test
    public void testCompileMultipleEscapes() {
        JsonPointer p = JsonPointer.compile("/a~0b~1c");
        assertEquals("a~b/c", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("a~b/c"));
    }

    @Test
    public void testCompileTrailingTilde() {
        JsonPointer p = JsonPointer.compile("/foo~");
        assertEquals("foo~", p.getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.matchProperty("foo~"));
    }

    @Test
    public void testCompileConsecutiveSlashes() {
        JsonPointer p = JsonPointer.compile("//");
        assertEquals("//", p.toString());
        assertEquals("", p.getMatchingProperty());
        assertNotNull(p.tail());
        assertEquals("/", p.tail().toString());
        assertEquals("", p.tail().getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.tail().tail());
        assertSame(JsonPointer.EMPTY, p.matchProperty("").matchProperty(""));
    }

    @Test
    public void testCompileTrailingSlash() {
        JsonPointer p = JsonPointer.compile("/foo/");
        assertEquals("/foo/", p.toString());
        assertEquals("foo", p.getMatchingProperty());
        assertNotNull(p.tail());
        assertEquals("/", p.tail().toString());
        assertEquals("", p.tail().getMatchingProperty());
        assertSame(JsonPointer.EMPTY, p.tail().tail());
    }

    @Test
    public void testMatchesEmpty() {
        assertTrue(JsonPointer.EMPTY.matches());
        assertFalse(JsonPointer.compile("/").matches());
    }

    @Test
    public void testMatchPropertyNull() {
        JsonPointer p = JsonPointer.compile("/foo");
        assertNull(p.matchProperty(null));
    }

    @Test
    public void testMatchElementBoundary() {
        JsonPointer p = JsonPointer.compile("/2147483647");
        assertSame(JsonPointer.EMPTY, p.matchElement(Integer.MAX_VALUE));
        assertNull(p.matchElement(Integer.MAX_VALUE - 1));
        assertNull(p.matchElement(-1));
    }

    @Test
    public void testEquals() {
        JsonPointer empty1 = JsonPointer.EMPTY;
        JsonPointer empty2 = JsonPointer.compile("");
        JsonPointer foo1 = JsonPointer.compile("/foo");
        JsonPointer foo2 = JsonPointer.compile("/foo");
        JsonPointer bar = JsonPointer.compile("/bar");
        JsonPointer nested1 = JsonPointer.compile("/foo/bar");
        JsonPointer nested2 = JsonPointer.compile("/foo/bar");
        JsonPointer otherNested = JsonPointer.compile("/foo/baz");

        assertTrue(empty1.equals(empty2));
        assertTrue(empty2.equals(empty1));
        assertTrue(foo1.equals(foo2));
        assertTrue(foo2.equals(foo1));
        assertTrue(foo1.equals(foo1)); // same object
        assertFalse(foo1.equals(null));
        assertFalse(foo1.equals("/foo")); // different type
        assertFalse(foo1.equals(bar));
        assertFalse(nested1.equals(otherNested));
        assertFalse(nested1.equals(foo1));
        assertTrue(nested1.equals(nested2));
    }

    @Test
    public void testHashCode() {
        assertEquals(JsonPointer.EMPTY.hashCode(), "".hashCode());
        assertEquals(JsonPointer.compile("/foo").hashCode(), JsonPointer.compile("/foo").hashCode());
        assertEquals(JsonPointer.compile("/foo/bar").hashCode(), "/foo/bar".hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("", JsonPointer.EMPTY.toString());
        assertEquals("/foo", JsonPointer.compile("/foo").toString());
        assertEquals("/foo/bar", JsonPointer.compile("/foo/bar").toString());
        assertEquals("/~0~1", JsonPointer.compile("/~0~1").toString());
    }
}
