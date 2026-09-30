package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class TextBufferTest {

    @Test
    public void testInitialStateAndEmptyReset() {
        TextBuffer tb = new TextBuffer(null);
        Assert.assertEquals(0, tb.size());
        Assert.assertEquals(0, tb.getTextOffset());
        Assert.assertTrue(tb.hasTextAsCharacters());
        Assert.assertEquals("", tb.contentsAsString());
        Assert.assertEquals("", tb.toString());
        Assert.assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
        
        tb.resetWithEmpty();
        Assert.assertEquals(0, tb.size());
        Assert.assertEquals(0, tb.getTextOffset());
        Assert.assertNull(tb.getTextBuffer());
    }

    @Test
    public void testResetWithShared() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "Hello World".toCharArray();
        tb.resetWithShared(src, 6, 5);

        Assert.assertEquals(5, tb.size());
        Assert.assertEquals(6, tb.getTextOffset());
        Assert.assertTrue(tb.hasTextAsCharacters());
        Assert.assertSame(src, tb.getTextBuffer());
        Assert.assertEquals("World", tb.contentsAsString());
        Assert.assertArrayEquals("World".toCharArray(), tb.contentsAsArray());

        // Test with start == 0
        tb.resetWithShared(src, 0, 5);
        Assert.assertEquals(0, tb.getTextOffset());
        Assert.assertEquals(5, tb.size());
        Assert.assertArrayEquals("Hello".toCharArray(), tb.contentsAsArray());

        // Test with len < 1
        tb.resetWithShared(src, 0, 0);
        Assert.assertEquals(0, tb.size());
        Assert.assertEquals("", tb.contentsAsString());
        Assert.assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
    }

    @Test
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("TestString");

        Assert.assertEquals(10, tb.size());
        Assert.assertEquals(0, tb.getTextOffset());
        Assert.assertFalse(tb.hasTextAsCharacters());
        Assert.assertEquals("TestString", tb.contentsAsString());
        Assert.assertArrayEquals("TestString".toCharArray(), tb.getTextBuffer());
        Assert.assertTrue(tb.hasTextAsCharacters()); // After getTextBuffer, _resultArray is set
        Assert.assertArrayEquals("TestString".toCharArray(), tb.contentsAsArray());
        Assert.assertEquals("TestString", tb.contentsAsString());
    }

    @Test
    public void testResetWithCopy() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "ABCDEFGHIJ".toCharArray();
        tb.resetWithCopy(src, 3, 4);

        Assert.assertEquals(4, tb.size());
        Assert.assertEquals(0, tb.getTextOffset());
        Assert.assertTrue(tb.hasTextAsCharacters());
        Assert.assertEquals("DEFG", tb.contentsAsString());
        Assert.assertArrayEquals("DEFG".toCharArray(), tb.contentsAsArray());
    }

    @Test
    public void testAppendChar() {
        TextBuffer tb = new TextBuffer(null);
        tb.append('a');
        tb.append('b');
        tb.append('c');

        Assert.assertEquals(3, tb.size());
        Assert.assertEquals("abc", tb.contentsAsString());
        Assert.assertArrayEquals("abc".toCharArray(), tb.contentsAsArray());
    }

    @Test
    public void testAppendCharArrayAndString() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("foo".toCharArray(), 0, 3);
        tb.append("bar", 0, 3);

        Assert.assertEquals(6, tb.size());
        Assert.assertEquals("foobar", tb.contentsAsString());
    }

    @Test
    public void testAppendToSharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "Initial".toCharArray();
        tb.resetWithShared(src, 0, 7);

        tb.append('!');
        Assert.assertEquals(8, tb.size());
        Assert.assertEquals("Initial!", tb.contentsAsString());

        tb.resetWithShared(src, 0, 7);
        tb.append(" Append".toCharArray(), 0, 7);
        Assert.assertEquals(14, tb.size());
        Assert.assertEquals("Initial Append", tb.contentsAsString());

        tb.resetWithShared(src, 0, 7);
        tb.append(" String", 0, 7);
        Assert.assertEquals(14, tb.size());
        Assert.assertEquals("Initial String", tb.contentsAsString());
    }

    @Test
    public void testEnsureNotShared() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "SharedData".toCharArray();
        tb.resetWithShared(src, 0, 10);
        Assert.assertEquals(0, tb.getTextOffset());
        tb.ensureNotShared();
        Assert.assertEquals(-1, tb.getTextOffset());
        Assert.assertEquals(10, tb.size());
        Assert.assertEquals("SharedData", tb.contentsAsString());
    }

    @Test
    public void testSegmentExpansion() {
        TextBuffer tb = new TextBuffer(null);
        char[] chunk = new char[500];
        Arrays.fill(chunk, 'x');

        // Append more than initial buffer capacity (1000)
        for (int i = 0; i < 5; i++) {
            tb.append(chunk, 0, chunk.length);
        }

        Assert.assertEquals(2500, tb.size());
        char[] array = tb.contentsAsArray();
        Assert.assertEquals(2500, array.length);
        for (char c : array) {
            Assert.assertEquals('x', c);
        }

        String str = tb.contentsAsString();
        Assert.assertEquals(2500, str.length());
        Assert.assertArrayEquals(array, tb.getTextBuffer());
    }

    @Test
    public void testFinishCurrentSegmentAndExpansion() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg1 = tb.emptyAndGetCurrentSegment();
        Assert.assertNotNull(seg1);
        Assert.assertTrue(seg1.length >= TextBuffer.MIN_SEGMENT_LEN);

        Arrays.fill(seg1, 'A');
        tb.setCurrentLength(seg1.length);
        Assert.assertEquals(seg1.length, tb.getCurrentSegmentSize());

        char[] seg2 = tb.finishCurrentSegment();
        Assert.assertNotNull(seg2);
        Assert.assertTrue(seg2.length > seg1.length);
        Arrays.fill(seg2, 'B');
        tb.setCurrentLength(10);

        Assert.assertEquals(seg1.length + 10, tb.size());
        String res = tb.contentsAsString();
        Assert.assertTrue(res.startsWith("AAA"));
        Assert.assertTrue(res.endsWith("BBBBBBBBBB"));

        // Reset with empty to test clearSegments path
        tb.resetWithEmpty();
        Assert.assertEquals(0, tb.size());
    }

    @Test
    public void testExpandCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        int origLen = seg.length;
        char[] expanded = tb.expandCurrentSegment();
        Assert.assertTrue(expanded.length > origLen);
        Assert.assertSame(expanded, tb.getCurrentSegment());

        char[] expandedMin = tb.expandCurrentSegment(expanded.length + 100);
        Assert.assertEquals(expanded.length + 100, expandedMin.length);

        char[] expandedNoOp = tb.expandCurrentSegment(expandedMin.length - 10);
        Assert.assertSame(expandedMin, expandedNoOp);
    }

    @Test
    public void testSetCurrentAndReturn() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        seg[0] = 'a';
        seg[1] = 'b';
        seg[2] = 'c';
        String res = tb.setCurrentAndReturn(3);
        Assert.assertEquals("abc", res);
        Assert.assertEquals("abc", tb.contentsAsString());

        String empty = tb.setCurrentAndReturn(0);
        Assert.assertEquals("", empty);

        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = 'x';
        String multi = tb.setCurrentAndReturn(1);
        Assert.assertEquals("x", multi);
    }

    @Test
    public void testContentsAsDecimalAndDouble() {
        TextBuffer tb = new TextBuffer(null);

        // Pre-cut array
        tb.resetWithString("123.45");
        tb.contentsAsArray(); // caches resultArray
        Assert.assertEquals(new BigDecimal("123.45"), tb.contentsAsDecimal());
        Assert.assertEquals(123.45, tb.contentsAsDouble(), 0.00001);

        // Shared buffer
        char[] chars = " 678.90 ".toCharArray();
        tb.resetWithShared(chars, 1, 6);
        Assert.assertEquals(new BigDecimal("678.90"), tb.contentsAsDecimal());
        Assert.assertEquals(678.90, tb.contentsAsDouble(), 0.00001);

        // Single buffer
        tb.resetWithEmpty();
        tb.append("42.0", 0, 4);
        Assert.assertEquals(new BigDecimal("42.0"), tb.contentsAsDecimal());
        Assert.assertEquals(42.0, tb.contentsAsDouble(), 0.00001);

        // Multi-segment decimal
        tb.resetWithEmpty();
        char[] large = tb.emptyAndGetCurrentSegment();
        large[0] = '1';
        tb.setCurrentLength(1);
        tb.finishCurrentSegment();
        char[] next = tb.getCurrentSegment();
        next[0] = '0';
        tb.setCurrentLength(1);
        Assert.assertEquals(new BigDecimal("10"), tb.contentsAsDecimal());
        Assert.assertEquals(10.0, tb.contentsAsDouble(), 0.00001);
    }

    @Test
    public void testBufferRecyclerReleaseAndReuse() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);

        char[] seg = tb.emptyAndGetCurrentSegment();
        Assert.assertNotNull(seg);
        tb.append("recycled content", 0, 16);
        tb.releaseBuffers();

        // Release on already released / empty
        tb.releaseBuffers();

        // Null allocator release
        TextBuffer tbNull = new TextBuffer(null);
        tbNull.emptyAndGetCurrentSegment();
        tbNull.releaseBuffers();
    }

    @Test
    public void testLargeAppendSplitting() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();

        // Large array append exceeding initial segment size
        char[] bigChars = new char[3000];
        Arrays.fill(bigChars, 'z');
        tb.append(bigChars, 0, bigChars.length);

        Assert.assertEquals(3000, tb.size());
        Assert.assertEquals(3000, tb.contentsAsString().length());

        // Large string append exceeding segment size
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append('y');
        }
        tb.append(sb.toString(), 0, 4000);
        Assert.assertEquals(7000, tb.size());
        Assert.assertEquals(7000, tb.contentsAsString().length());
    }

    @Test
    public void testGetCurrentSegmentSharedBranch() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "shared".toCharArray();
        tb.resetWithShared(shared, 0, 6);
        char[] seg = tb.getCurrentSegment();
        Assert.assertNotNull(seg);
        Assert.assertEquals("shared", new String(seg, 0, tb.getCurrentSegmentSize()));
    }

    @Test
    public void testResetWithSegmentsCleared() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();
        tb.finishCurrentSegment();

        // resetWithShared clears segments
        tb.resetWithShared("test".toCharArray(), 0, 4);
        Assert.assertEquals(4, tb.size());

        tb.finishCurrentSegment();
        // resetWithCopy clears segments
        tb.resetWithCopy("copy".toCharArray(), 0, 4);
        Assert.assertEquals(4, tb.size());

        tb.finishCurrentSegment();
        // resetWithString clears segments
        tb.resetWithString("str");
        Assert.assertEquals(3, tb.size());
    }
}
