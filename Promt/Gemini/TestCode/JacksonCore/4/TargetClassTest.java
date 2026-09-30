package com.fasterxml.jackson.core.util;

import org.junit.Test;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TextBufferTest {

    @Test
    public void testInitializationAndEmptyBuffer() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);

        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
        assertEquals("", tb.toString());

        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    @Test
    public void testReleaseBuffersWithoutAllocator() {
        TextBuffer tb = new TextBuffer(null);
        tb.append('a');
        assertEquals(1, tb.size());
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    @Test
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("foobar");

        assertEquals(6, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertFalse(tb.hasTextAsCharacters());
        assertEquals("foobar", tb.contentsAsString());
        assertArrayEquals("foobar".toCharArray(), tb.getTextBuffer());
        assertTrue(tb.hasTextAsCharacters());
        assertArrayEquals("foobar".toCharArray(), tb.contentsAsArray());
        assertEquals("foobar", tb.toString());

        // Test with segments cleared
        tb.emptyAndGetCurrentSegment();
        tb.finishCurrentSegment();
        tb.resetWithString("updated");
        assertEquals("updated", tb.contentsAsString());
    }

    @Test
    public void testResetWithSharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "abcdefghij".toCharArray();
        tb.resetWithShared(source, 2, 5);

        assertEquals(5, tb.size());
        assertEquals(2, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(source, tb.getTextBuffer());
        assertEquals("cdefg", tb.contentsAsString());
        assertArrayEquals("cdefg".toCharArray(), tb.contentsAsArray());

        // Test with start == 0
        tb.resetWithShared(source, 0, 4);
        assertArrayEquals("abcd".toCharArray(), tb.contentsAsArray());

        // Test with len < 1
        tb.resetWithShared(source, 0, 0);
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
    }

    @Test
    public void testResetWithCopy() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "abcdefghij".toCharArray();
        tb.resetWithCopy(source, 2, 5);

        assertEquals(5, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("cdefg", tb.contentsAsString());
        assertArrayEquals("cdefg".toCharArray(), tb.contentsAsArray());

        // Reset with copy when segments exist
        tb.emptyAndGetCurrentSegment();
        tb.finishCurrentSegment();
        tb.resetWithCopy(source, 0, 3);
        assertEquals("abc", tb.contentsAsString());
    }

    @Test
    public void testEnsureNotShared() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "abcdefghij".toCharArray();
        tb.resetWithShared(source, 2, 5);

        tb.ensureNotShared();
        assertEquals(5, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("cdefg", tb.contentsAsString());
        assertNotSame(source, tb.getTextBuffer());

        // Calling when already not shared should be a no-op
        tb.ensureNotShared();
        assertEquals("cdefg", tb.contentsAsString());
    }

    @Test
    public void testAppendChar() {
        TextBuffer tb = new TextBuffer(null);
        tb.append('a');
        tb.append('b');
        tb.append('c');

        assertEquals(3, tb.size());
        assertEquals("abc", tb.contentsAsString());

        // Append to shared buffer
        char[] source = "hello".toCharArray();
        tb.resetWithShared(source, 0, 5);
        tb.append('!');
        assertEquals("hello!", tb.contentsAsString());

        // Append to trigger segment expansion
        TextBuffer tb2 = new TextBuffer(null);
        char[] seg = tb2.emptyAndGetCurrentSegment();
        tb2.setCurrentLength(seg.length);
        tb2.append('Z');
        assertEquals(seg.length + 1, tb2.size());
        assertEquals('Z', tb2.contentsAsString().charAt(seg.length));
    }

    @Test
    public void testAppendCharArray() {
        TextBuffer tb = new TextBuffer(null);
        char[] text1 = "Hello".toCharArray();
        char[] text2 = " World!".toCharArray();

        tb.append(text1, 0, text1.length);
        assertEquals("Hello", tb.contentsAsString());

        tb.append(text2, 0, text2.length);
        assertEquals("Hello World!", tb.contentsAsString());

        // Large append exceeding single segment
        TextBuffer tbHuge = new TextBuffer(null);
        char[] huge = new char[5000];
        Arrays.fill(huge, 'x');
        tbHuge.append(huge, 0, huge.length);
        assertEquals(5000, tbHuge.size());
        assertEquals(new String(huge), tbHuge.contentsAsString());

        // Append to shared buffer
        tb.resetWithShared(text1, 0, text1.length);
        tb.append(text2, 0, text2.length);
        assertEquals("Hello World!", tb.contentsAsString());
    }

    @Test
    public void testAppendString() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("Hello", 0, 5);
        tb.append(" beautiful", 0, 10);
        tb.append(" World!", 0, 7);

        assertEquals("Hello beautiful World!", tb.contentsAsString());

        // Append large string exceeding single segment
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append('y');
        }
        String large = sb.toString();
        TextBuffer tbLarge = new TextBuffer(null);
        tbLarge.append(large, 0, large.length());
        assertEquals(5000, tbLarge.size());
        assertEquals(large, tbLarge.contentsAsString());

        // Append to shared buffer
        char[] shared = "Prefix: ".toCharArray();
        tbLarge.resetWithShared(shared, 0, shared.length);
        tbLarge.append("Suffix", 0, 6);
        assertEquals("Prefix: Suffix", tbLarge.contentsAsString());
    }

    @Test
    public void testGetCurrentSegmentAndFinishSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg1 = tb.getCurrentSegment();
        assertNotNull(seg1);
        assertTrue(seg1.length >= TextBuffer.MIN_SEGMENT_LEN);

        seg1[0] = 'A';
        seg1[1] = 'B';
        tb.setCurrentLength(2);
        assertEquals(2, tb.getCurrentSegmentSize());

        char[] seg2 = tb.finishCurrentSegment();
        assertNotNull(seg2);
        assertNotSame(seg1, seg2);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertEquals(2, tb.size());

        seg2[0] = 'C';
        tb.setCurrentLength(1);
        assertEquals(3, tb.size());

        assertEquals("ABC", tb.contentsAsString());
        assertArrayEquals("ABC".toCharArray(), tb.contentsAsArray());
        assertArrayEquals("ABC".toCharArray(), tb.getTextBuffer());

        // Test shared state unsharing via getCurrentSegment
        char[] shared = "Test".toCharArray();
        tb.resetWithShared(shared, 0, shared.length);
        char[] segAfterShare = tb.getCurrentSegment();
        assertNotNull(segAfterShare);
        assertEquals(4, tb.getCurrentSegmentSize());
    }

    @Test
    public void testExpandCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        int initialLen = seg.length;

        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > initialLen);
        assertSame(expanded, tb.getCurrentSegment());

        char[] expandedMin = tb.expandCurrentSegment(initialLen + 5000);
        assertTrue(expandedMin.length >= initialLen + 5000);

        char[] noExpand = tb.expandCurrentSegment(expandedMin.length - 10);
        assertSame(expandedMin, noExpand);

        // Test max segment branch logic
        TextBuffer tbMax = new TextBuffer(null);
        char[] maxSeg = new char[TextBuffer.MAX_SEGMENT_LEN];
        tbMax.resetWithCopy(maxSeg, 0, maxSeg.length);
        char[] overMax = tbMax.expandCurrentSegment();
        assertEquals(TextBuffer.MAX_SEGMENT_LEN + 1, overMax.length);
    }

    @Test
    public void testContentsAsDecimal() {
        // Shared buffer
        TextBuffer tb = new TextBuffer(null);
        char[] chars = "123.456".toCharArray();
        tb.resetWithShared(chars, 0, chars.length);
        assertEquals(new BigDecimal("123.456"), tb.contentsAsDecimal());

        // Single buffer
        tb.resetWithEmpty();
        tb.append("789.01", 0, 6);
        assertEquals(new BigDecimal("789.01"), tb.contentsAsDecimal());

        // Pre-cut array
        tb.contentsAsArray();
        assertEquals(new BigDecimal("789.01"), tb.contentsAsDecimal());

        // Segmented buffer
        tb.resetWithEmpty();
        char[] seg = tb.emptyAndGetCurrentSegment();
        Arrays.fill(seg, '1');
        tb.setCurrentLength(seg.length);
        tb.finishCurrentSegment();
        tb.append("23.5", 0, 4);
        BigDecimal expected = new BigDecimal(new String(seg) + "23.5");
        assertEquals(expected, tb.contentsAsDecimal());
    }

    @Test
    public void testContentsAsDouble() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("1234.5678", 0, 9);
        assertEquals(1234.5678, tb.contentsAsDouble(), 0.000001);
    }

    @Test(expected = NumberFormatException.class)
    public void testContentsAsDoubleInvalid() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("invalid_double", 0, 14);
        tb.contentsAsDouble();
    }

    @Test
    public void testResetWithEmptyAndSegments() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();
        tb.finishCurrentSegment();
        tb.append("test", 0, 4);

        tb.resetWithEmpty();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test
    public void testCachingBehavior() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("caching-test", 0, 12);

        // contentsAsArray caches result
        char[] arr = tb.contentsAsArray();
        assertSame(arr, tb.contentsAsArray());
        assertEquals(12, tb.size());

        // contentsAsString utilizes cached array
        assertEquals("caching-test", tb.contentsAsString());

        // Subsequent size() utilizes cached string
        assertEquals(12, tb.size());
    }

    @Test
    public void testMultiSegmentGrowthLimits() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        
        // Repeatedly finish segment to exercise MIN and MAX bounds
        for (int i = 0; i < 20; i++) {
            tb.setCurrentLength(seg.length);
            seg = tb.finishCurrentSegment();
            assertTrue(seg.length <= TextBuffer.MAX_SEGMENT_LEN);
            assertTrue(seg.length >= TextBuffer.MIN_SEGMENT_LEN);
        }
    }
}
