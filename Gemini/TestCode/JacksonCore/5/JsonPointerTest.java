package com.fasterxml.jackson.core;

import org.junit.Assert;
import org.junit.Test;

public class JsonPointerTest {

    @Test
    public void testEmptyAndNull() {
        JsonPointer ptrNull = JsonPointer.compile(null);
        Assert.assertNotNull(ptrNull);
        Assert.assertTrue(ptrNull.matches());
        Assert.assertEquals("", ptrNull.getMatchingProperty());
        Assert.assertEquals(-1, ptrNull.getMatchingIndex());
        Assert.assertTrue(ptrNull.mayMatchProperty());
        Assert.assertFalse(ptrNull.mayMatchElement());
        Assert.assertNull(ptrNull.tail());
        Assert.assertEquals("", ptrNull.toString());
        Assert.assertEquals(0, ptrNull.hashCode());

        JsonPointer ptrEmpty = JsonPointer.compile("");
        Assert.assertSame(ptrNull, ptrEmpty);
        Assert.assertSame(JsonPointer.EMPTY, ptrEmpty);

        JsonPointer ptrValueOfNull = JsonPointer.valueOf(null);
        Assert.assertSame(JsonPointer.EMPTY, ptrValueOfNull);

        JsonPointer ptrValueOfEmpty = JsonPointer.valueOf("");
        Assert.assertSame(JsonPointer.EMPTY, ptrValueOfEmpty);

        Assert.assertNull(ptrEmpty.matchProperty(""));
        Assert.assertNull(ptrEmpty.matchProperty("test"));
        Assert.assertNull(ptrEmpty.matchElement(0));
        Assert.assertNull(ptrEmpty.matchElement(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPointerNonSlashStart() {
        JsonPointer.compile("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPointerLeadingSpace() {
        JsonPointer.compile(" /abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPointerSingleChar() {
        JsonPointer.compile("a");
    }

    @Test
    public void testSimpleProperties() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        Assert.assertFalse(ptr.matches());
        Assert.assertEquals("foo", ptr.getMatchingProperty());
        Assert.assertEquals(-1, ptr.getMatchingIndex());
        Assert.assertTrue(ptr.mayMatchProperty());
        Assert.assertFalse(ptr.mayMatchElement());
        Assert.assertEquals("/foo", ptr.toString());

        JsonPointer next = ptr.matchProperty("foo");
        Assert.assertNotNull(next);
        Assert.assertTrue(next.matches());
        Assert.assertSame(JsonPointer.EMPTY, next);

        Assert.assertNull(ptr.matchProperty("bar"));
        Assert.assertNull(ptr.matchElement(0));

        JsonPointer tail = ptr.tail();
        Assert.assertNotNull(tail);
        Assert.assertTrue(tail.matches());
    }

    @Test
    public void testSimpleIndexes() {
        JsonPointer ptr = JsonPointer.compile("/0");
        Assert.assertFalse(ptr.matches());
        Assert.assertEquals("0", ptr.getMatchingProperty());
        Assert.assertEquals(0, ptr.getMatchingIndex());
        Assert.assertTrue(ptr.mayMatchProperty());
        Assert.assertTrue(ptr.mayMatchElement());

        JsonPointer next = ptr.matchElement(0);
        Assert.assertNotNull(next);
        Assert.assertTrue(next.matches());

        Assert.assertNull(ptr.matchElement(1));
        Assert.assertNull(ptr.matchElement(-1));
        Assert.assertNull(ptr.matchProperty("1"));

        JsonPointer matchProp = ptr.matchProperty("0");
        Assert.assertNotNull(matchProp);
        Assert.assertTrue(matchProp.matches());
    }

    @Test
    public void testMultiSegmentPath() {
        JsonPointer ptr = JsonPointer.compile("/users/12/address/city");
        Assert.assertEquals("/users/12/address/city", ptr.toString());

        Assert.assertEquals("users", ptr.getMatchingProperty());
        Assert.assertEquals(-1, ptr.getMatchingIndex());

        JsonPointer p1 = ptr.matchProperty("users");
        Assert.assertNotNull(p1);
        Assert.assertEquals("12", p1.getMatchingProperty());
        Assert.assertEquals(12, p1.getMatchingIndex());

        JsonPointer p2 = p1.matchElement(12);
        Assert.assertNotNull(p2);
        Assert.assertEquals("address", p2.getMatchingProperty());
        Assert.assertEquals(-1, p2.getMatchingIndex());

        JsonPointer p3 = p2.matchProperty("address");
        Assert.assertNotNull(p3);
        Assert.assertEquals("city", p3.getMatchingProperty());
        Assert.assertEquals(-1, p3.getMatchingIndex());

        JsonPointer p4 = p3.matchProperty("city");
        Assert.assertNotNull(p4);
        Assert.assertTrue(p4.matches());
        Assert.assertNull(p4.tail());
    }

    @Test
    public void testEscapedCharacters() {
        JsonPointer ptr1 = JsonPointer.compile("/~0");
        Assert.assertEquals("~", ptr1.getMatchingProperty());

        JsonPointer ptr2 = JsonPointer.compile("/~1");
        Assert.assertEquals("/", ptr2.getMatchingProperty());

        JsonPointer ptr3 = JsonPointer.compile("/a~0b/c~1d");
        Assert.assertEquals("a~b", ptr3.getMatchingProperty());
        JsonPointer tail3 = ptr3.tail();
        Assert.assertEquals("c/d", tail3.getMatchingProperty());

        JsonPointer ptr4 = JsonPointer.compile("/~01");
        Assert.assertEquals("~1", ptr4.getMatchingProperty());

        JsonPointer ptr5 = JsonPointer.compile("/~10");
        Assert.assertEquals("/0", ptr5.getMatchingProperty());

        JsonPointer ptr6 = JsonPointer.compile("/~");
        Assert.assertEquals("~", ptr6.getMatchingProperty());

        JsonPointer ptr7 = JsonPointer.compile("/a~");
        Assert.assertEquals("a~", ptr7.getMatchingProperty());

        JsonPointer ptr8 = JsonPointer.compile("/~x");
        Assert.assertEquals("~x", ptr8.getMatchingProperty());

        JsonPointer ptr9 = JsonPointer.compile("/foo~0bar~1baz");
        Assert.assertEquals("foo~bar/baz", ptr9.getMatchingProperty());

        JsonPointer ptr10 = JsonPointer.compile("/~0~1/end");
        Assert.assertEquals("~/ ", ptr10.getMatchingProperty().trim());
        Assert.assertEquals("~/", ptr10.getMatchingProperty());
        Assert.assertEquals("end", ptr10.tail().getMatchingProperty());
    }

    @Test
    public void testIndexParsingBoundaries() {
        Assert.assertEquals(0, JsonPointer.compile("/0").getMatchingIndex());
        Assert.assertEquals(1, JsonPointer.compile("/1").getMatchingIndex());
        Assert.assertEquals(9, JsonPointer.compile("/9").getMatchingIndex());
        Assert.assertEquals(42, JsonPointer.compile("/42").getMatchingIndex());
        Assert.assertEquals(123456789, JsonPointer.compile("/123456789").getMatchingIndex());
        Assert.assertEquals(Integer.MAX_VALUE, JsonPointer.compile("/" + Integer.MAX_VALUE).getMatchingIndex());

        // Overflow boundary (2147483648 is Integer.MAX_VALUE + 1, length 10)
        Assert.assertEquals(-1, JsonPointer.compile("/2147483648").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/9999999999").getMatchingIndex());

        // Length > 10
        Assert.assertEquals(-1, JsonPointer.compile("/10000000000").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/123456789012345").getMatchingIndex());

        // Non-digit characters
        Assert.assertEquals(-1, JsonPointer.compile("/-1").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/+1").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/1a").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/a1").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("/").getMatchingIndex());
        Assert.assertEquals(-1, JsonPointer.compile("//").getMatchingIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        JsonPointer p1 = JsonPointer.compile("/a/b/c");
        JsonPointer p2 = JsonPointer.valueOf("/a/b/c");
        JsonPointer p3 = JsonPointer.compile("/a/b/d");
        JsonPointer empty1 = JsonPointer.compile("");
        JsonPointer empty2 = JsonPointer.EMPTY;

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("/a/b/c"));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(empty1));

        Assert.assertTrue(empty1.equals(empty2));
        Assert.assertEquals(empty1.hashCode(), empty2.hashCode());
    }

    @Test
    public void testConstructorsDirectly() {
        JsonPointer customEmpty = new JsonPointer();
        Assert.assertTrue(customEmpty.matches());
        Assert.assertEquals("", customEmpty.getMatchingProperty());
        Assert.assertEquals(-1, customEmpty.getMatchingIndex());
        Assert.assertNull(customEmpty.tail());
        Assert.assertEquals("", customEmpty.toString());

        JsonPointer customSeg = new JsonPointer("/sub", "sub", customEmpty);
        Assert.assertFalse(customSeg.matches());
        Assert.assertEquals("sub", customSeg.getMatchingProperty());
        Assert.assertEquals(-1, customSeg.getMatchingIndex());
        Assert.assertSame(customEmpty, customSeg.tail());
        Assert.assertEquals("/sub", customSeg.toString());
    }

    @Test
    public void testRFC6901SpecificationExamples() {
        // RFC 6901 section 5 test cases
        Assert.assertEquals("", JsonPointer.compile("").toString());
        Assert.assertEquals("foo", JsonPointer.compile("/foo").getMatchingProperty());
        Assert.assertEquals("foo/0", JsonPointer.compile("/foo/0").tail().getMatchingProperty());
        Assert.assertEquals("/", JsonPointer.compile("/").getMatchingProperty());
        Assert.assertEquals("a/b", JsonPointer.compile("/a~1b").getMatchingProperty());
        Assert.assertEquals("c%d", JsonPointer.compile("/c%d").getMatchingProperty());
        Assert.assertEquals("e^f", JsonPointer.compile("/e^f").getMatchingProperty());
        Assert.assertEquals("g|h", JsonPointer.compile("/g|h").getMatchingProperty());
        Assert.assertEquals("i\\j", JsonPointer.compile("/i\\j").getMatchingProperty());
        Assert.assertEquals("k\"l", JsonPointer.compile("/k\"l").getMatchingProperty());
        Assert.assertEquals(" ", JsonPointer.compile("/ ").getMatchingProperty());
        Assert.assertEquals("m~n", JsonPointer.compile("/m~0n").getMatchingProperty());
    }
}
