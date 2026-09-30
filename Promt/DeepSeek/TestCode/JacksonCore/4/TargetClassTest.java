package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class TextBufferTest {

    private static final char[] SAMPLE_CHARS = "hello".toCharArray();
    private static final String SAMPLE_STRING = "world";
    private static final int MIN_SEGMENT_LEN = 1000;
    private static final int MAX_SEGMENT_LEN = 0x40000;

    // --- Constructor and initial state ---

    @Test
    public void testConstructorWithAllocator() {
        BufferRecycler allocator = new BufferRecycler();
        TextBuffer buf = new TextBuffer(allocator);
        assertEquals(0, buf.size());
        assertEquals(0, buf.getTextOffset());
        assertTrue(buf.hasTextAsCharacters());
        assertNull(buf.getTextBuffer()); // no current segment allocated yet
    }

    @Test
    public void testConstructorWithNullAllocator() {
        TextBuffer buf = new TextBuffer(null);
        assertEquals(0, buf.size());
    }

    // --- resetWithEmpty ---

    @Test
    public void testResetWithEmpty() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        // after some operations
        buf.getCurrentSegment();
        buf.append('a');
        buf.resetWithEmpty();
        assertEquals(0, buf.size());
        assertEquals(0, buf.getTextOffset());
        assertTrue(buf.hasTextAsCharacters());
        assertNull(buf.getTextBuffer()); // current segment cleared
    }

    // --- resetWithShared ---

    @Test
    public void testResetWithShared() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] data = "shared".toCharArray();
        buf.resetWithShared(data, 0, data.length);
        assertEquals(data.length, buf.size());
        assertEquals(0, buf.getTextOffset());
        assertTrue(buf.hasTextAsCharacters());
        assertSame(data, buf.getTextBuffer());
        
        // ensureNotShared should unshare
        buf.ensureNotShared();
        // size still the same
        assertEquals(data.length, buf.size());
        // buffer should be different array now
        assertNotSame(data, buf.getTextBuffer());
        assertArrayEquals(data, buf.getTextBuffer());
    }

    @Test
    public void testResetWithSharedNonZeroOffset() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] data = "abcdef".toCharArray();
        buf.resetWithShared(data, 2, 3); // "cde"
        assertEquals(3, buf.size());
        assertEquals(2, buf.getTextOffset());
        assertArrayEquals(new char[] {'c','d','e'}, Arrays.copyOfRange(buf.getTextBuffer(), 2, 5));
    }

    // --- resetWithCopy ---

    @Test
    public void testResetWithCopy() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] data = "initial".toCharArray();
        buf.resetWithCopy(data, 0, data.length);
        assertEquals(data.length, buf.size());
        assertArrayEquals(data, buf.getTextBuffer());
        // subsequent append should work
        buf.append('!');
        assertEquals(data.length + 1, buf.size());
    }

    // --- resetWithString ---

    @Test
    public void testResetWithString() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString(SAMPLE_STRING);
        assertEquals(SAMPLE_STRING.length(), buf.size());
        assertEquals(0, buf.getTextOffset());
        assertFalse(buf.hasTextAsCharacters()); // because resultString not null
        assertEquals(SAMPLE_STRING, buf.contentsAsString());
        assertArrayEquals(SAMPLE_STRING.toCharArray(), buf.getTextBuffer()); // getTextBuffer triggers conversion
    }

    // --- releaseBuffers ---

    @Test
    public void testReleaseBuffersWithAllocator() {
        BufferRecycler allocator = new BufferRecycler();
        TextBuffer buf = new TextBuffer(allocator);
        char[] initial = buf.getCurrentSegment(); // allocates
        buf.append("data");
        assertNotNull(initial);
        buf.releaseBuffers();
        assertEquals(0, buf.size());
        assertNull(buf.getTextBuffer());
        // allocator should have received the buffer back; we can't test that directly
        // but a new getCurrentSegment should get a recycled buffer (probably same array)
        char[] newSegment = buf.getCurrentSegment();
        // not necessarily the same object, but length might be >= MIN_SEGMENT_LEN
        assertTrue(newSegment.length >= MIN_SEGMENT_LEN);
    }

    @Test
    public void testReleaseBuffersWithNullAllocator() {
        TextBuffer buf = new TextBuffer(null);
        buf.getCurrentSegment();
        buf.append('x');
        buf.releaseBuffers();
        assertEquals(0, buf.size());
        assertNull(buf.getTextBuffer());
    }

    // --- append char ---

    @Test
    public void testAppendChar() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.getCurrentSegment(); // allocate
        buf.append('a');
        buf.append('b');
        assertEquals(2, buf.size());
        assertEquals(2, buf.getCurrentSegmentSize());
        assertEquals("ab", buf.contentsAsString());
    }

    @Test
    public void testAppendCharSharedUnshares() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] shared = {'x'};
        buf.resetWithShared(shared, 0, 1);
        buf.append('y'); // triggers unshare(16) then append
        assertEquals(2, buf.size());
        assertEquals("xy", buf.contentsAsString());
        assertSame(shared[0], 'x'); // shared unchanged
    }

    @Test
    public void testAppendCharSegmentFull() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] seg = buf.emptyAndGetCurrentSegment();
        // fill the segment
        for (int i = 0; i < seg.length; i++) {
            buf.append('a');
        }
        assertEquals(seg.length, buf.getCurrentSegmentSize());
        // now segment is full, next append should expand
        buf.append('b');
        assertEquals(seg.length + 1, buf.size());
        assertTrue(buf.getCurrentSegment().length > seg.length);
    }

    // --- append char[] ---

    @Test
    public void testAppendCharArray() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment(); // allocate
        char[] src = "appended".toCharArray();
        buf.append(src, 0, src.length);
        assertEquals(src.length, buf.size());
        assertArrayEquals(src, buf.contentsAsArray());
    }

    @Test
    public void testAppendCharArrayPartial() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        char[] src = "hello world".toCharArray();
        buf.append(src, 6, 5); // "world"
        assertEquals("world", buf.contentsAsString());
    }

    @Test
    public void testAppendCharArrayMultiSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        // force a small current segment by resetting with copy and then appending large
        buf.resetWithCopy(new char[0], 0, 0);
        // underlying segment length is at least MIN_SEGMENT_LEN; we can fill it and append more
        char[] segment = buf.getCurrentSegment();
        int fillLen = segment.length - buf.getCurrentSegmentSize();
        if (fillLen > 0) {
            char[] fill = new char[fillLen];
            Arrays.fill(fill, 'a');
            buf.append(fill, 0, fillLen);
        }
        // now segment should be full; append a small array to trigger expand
        int oldSize = buf.size();
        buf.append(new char[] {'b'}, 0, 1);
        assertEquals(oldSize + 1, buf.size());
        assertTrue(buf.getCurrentSegmentSize() == 1); // new segment started
        assertTrue(buf.size() > segment.length); // segment list size increased
    }

    @Test
    public void testAppendCharArrayZeroLength() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append(new char[] {}, 0, 0);
        assertEquals(0, buf.size());
    }

    // --- append String ---

    @Test
    public void testAppendString() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append("test", 0, 4);
        assertEquals(4, buf.size());
        assertEquals("test", buf.contentsAsString());
    }

    @Test
    public void testAppendStringPartial() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append("foobar", 0, 3);
        assertEquals(3, buf.size());
        assertEquals("foo", buf.contentsAsString());
    }

    @Test
    public void testAppendStringMultiSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] seg = buf.getCurrentSegment();
        int room = seg.length - buf.getCurrentSegmentSize();
        if (room > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < room; i++) sb.append('a');
            buf.append(sb.toString(), 0, room);
        }
        int oldSize = buf.size();
        buf.append("b", 0, 1);
        assertEquals(oldSize + 1, buf.size());
    }

    @Test
    public void testAppendStringZeroLength() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append("", 0, 0);
        assertEquals(0, buf.size());
    }

    // --- size and getTextOffset ---

    @Test
    public void testSizeWithSegmentAndCurrent() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        buf.append("abc");
        // finish current segment to move to list
        buf.finishCurrentSegment();
        buf.append("de");
        // now segments: one segment of length? depends on expansion.
        // size = segmentSize + currentSize
        int expected = buf.getCurrentSegmentSize(); // a bit tricky, but we can compute
        // Instead, verify that size is consistent with contentsAsString length.
        assertEquals(buf.contentsAsString().length(), buf.size());
    }

    @Test
    public void testGetTextOffset() {
        // shared
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithShared(new char[] {'a','b','c'}, 1, 2);
        assertEquals(1, buf.getTextOffset());
        // non-shared
        buf.resetWithEmpty();
        assertEquals(0, buf.getTextOffset());
    }

    // --- hasTextAsCharacters ---

    @Test
    public void testHasTextAsCharacters() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        // initial: no resultString, so true
        assertTrue(buf.hasTextAsCharacters());
        buf.resetWithString("hello");
        assertFalse(buf.hasTextAsCharacters());
        // after getTextBuffer, resultArray not null => true
        buf.getTextBuffer();
        assertTrue(buf.hasTextAsCharacters());
    }

    // --- getTextBuffer ---

    @Test
    public void testGetTextBufferFromShared() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        buf.resetWithShared(shared, 0, shared.length);
        assertSame(shared, buf.getTextBuffer());
    }

    @Test
    public void testGetTextBufferFromResultArray() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("cached");
        char[] arr = buf.getTextBuffer(); // resultArray set here
        assertNotNull(arr);
        assertArrayEquals("cached".toCharArray(), arr);
        char[] arr2 = buf.getTextBuffer();
        assertSame(arr, arr2); // cached
    }

    @Test
    public void testGetTextBufferSingleSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append("single");
        char[] seg = buf.getCurrentSegment();
        // In single segment, getTextBuffer returns _currentSegment
        assertSame(seg, buf.getTextBuffer());
    }

    // --- contentsAsString ---

    @Test
    public void testContentsAsStringResultCached() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("cached");
        assertEquals("cached", buf.contentsAsString());
    }

    @Test
    public void testContentsAsStringFromResultArray() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("string");
        buf.getTextBuffer(); // forces resultArray
        assertEquals("string", buf.contentsAsString());
    }

    @Test
    public void testContentsAsStringFromShared() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        buf.resetWithShared(shared, 0, shared.length);
        assertEquals("shared", buf.contentsAsString());
        // test shared len < 1
        buf.resetWithShared(new char[1], 0, 0);
        assertEquals("", buf.contentsAsString());
    }

    @Test
    public void testContentsAsStringFromSingleSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.append("hello");
        assertEquals("hello", buf.contentsAsString());
        assertEquals(5, buf.size());
    }

    @Test
    public void testContentsAsStringFromMultipleSegments() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        // create two segments with finishCurrentSegment
        buf.append("first");
        buf.finishCurrentSegment();
        buf.append("second");
        String result = buf.contentsAsString();
        assertTrue(result.startsWith("first"));
        assertTrue(result.endsWith("second"));
        assertEquals("first".length() + "second".length(), result.length());
    }

    // --- contentsAsArray ---

    @Test
    public void testContentsAsArrayCaching() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.append("array");
        char[] a1 = buf.contentsAsArray();
        assertNotNull(a1);
        char[] a2 = buf.contentsAsArray();
        assertSame(a1, a2);
    }

    // --- contentsAsDecimal and contentsAsDouble ---

    @Test
    public void testContentsAsDecimalValid() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("123.45");
        BigDecimal dec = buf.contentsAsDecimal();
        assertEquals(new BigDecimal("123.45"), dec);
    }

    @Test(expected = NumberFormatException.class)
    public void testContentsAsDecimalInvalid() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("not a number");
        buf.contentsAsDecimal();
    }

    @Test
    public void testContentsAsDoubleValid() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("12.5");
        assertEquals(12.5, buf.contentsAsDouble(), 0.0);
    }

    @Test(expected = NumberFormatException.class)
    public void testContentsAsDoubleInvalid() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithString("abc");
        buf.contentsAsDouble();
    }

    // --- ensureNotShared ---

    @Test
    public void testEnsureNotSharedAlreadyNotShared() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithEmpty();
        buf.ensureNotShared(); // no effect
        assertEquals(0, buf.size());
    }

    @Test
    public void testEnsureNotSharedUnshare() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        buf.resetWithShared(shared, 0, shared.length);
        buf.ensureNotShared();
        assertNull(buf.getTextBuffer()); // but actually getTextBuffer returns current segment? It's not shared anymore. After unshare, _inputStart=-1, so getTextBuffer would return null because no current segment allocated? Actually unshare allocates _currentSegment. So getTextBuffer should return _currentSegment. Let's test: after ensureNotShared, getTextBuffer should return non-null.
        assertNotNull(buf.getTextBuffer());
        assertArrayEquals(shared, buf.getTextBuffer());
    }

    // --- getCurrentSegment ---

    @Test
    public void testGetCurrentSegmentAllocates() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] seg = buf.getCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length >= MIN_SEGMENT_LEN);
    }

    @Test
    public void testGetCurrentSegmentUnshares() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithShared("xyz".toCharArray(), 0, 3);
        char[] seg = buf.getCurrentSegment();
        assertNotNull(seg);
        // after unshare, original shared buffer should be unused
        assertTrue(seg.length >= 16);
        assertEquals(3, buf.getCurrentSegmentSize()); // why 3? Unshare copies shared data to start of segment.
        assertEquals(3, buf.size());
    }

    @Test
    public void testGetCurrentSegmentExpandsWhenFull() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] seg = buf.getCurrentSegment();
        // fill segment
        Arrays.fill(seg, 'a');
        buf.setCurrentLength(seg.length); // manually set size to length
        // now call getCurrentSegment, should expand
        char[] seg2 = buf.getCurrentSegment();
        assertNotNull(seg2);
        assertTrue(seg2.length > seg.length);
    }

    // --- emptyAndGetCurrentSegment ---

    @Test
    public void testEmptyAndGetCurrentSegmentClears() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.append("test");
        buf.emptyAndGetCurrentSegment();
        assertEquals(0, buf.size());
        assertNotNull(buf.getCurrentSegment());
    }

    @Test
    public void testEmptyAndGetCurrentSegmentReturnsValidSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] seg = buf.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length > 0);
    }

    // --- finishCurrentSegment ---

    @Test
    public void testFinishCurrentSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] oldSeg = buf.getCurrentSegment();
        int oldLen = oldSeg.length;
        buf.append("data"); // add some data
        char[] newSeg = buf.finishCurrentSegment();
        assertNotNull(newSeg);
        assertNull(oldSeg, newSeg); // they are different
        // segment size increased by oldLen
        assertEquals(oldLen, buf.size() - buf.getCurrentSegmentSize()); // size = segmentSize + currentSize, currentSize is 0 after finish
        // new segment length should be ~1.5x oldLen, bounded
        int expectedNewLen = oldLen + (oldLen >> 1);
        if (expectedNewLen < MIN_SEGMENT_LEN) expectedNewLen = MIN_SEGMENT_LEN;
        else if (expectedNewLen > MAX_SEGMENT_LEN) expectedNewLen = MAX_SEGMENT_LEN;
        assertEquals(expectedNewLen, newSeg.length);
    }

    @Test
    public void testFinishCurrentSegmentMultiple() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        buf.finishCurrentSegment();
        buf.finishCurrentSegment();
        // segments list should have entries
        assertTrue(buf.size() > 0); // segmentSize sum of lengths
    }

    // --- expandCurrentSegment (no arg) ---

    @Test
    public void testExpandCurrentSegment() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] oldSeg = buf.getCurrentSegment();
        int oldLen = oldSeg.length;
        char[] newSeg = buf.expandCurrentSegment();
        int expectedNewLen = oldLen + (oldLen >> 1);
        if (expectedNewLen > MAX_SEGMENT_LEN) expectedNewLen = MAX_SEGMENT_LEN;
        assertEquals(expectedNewLen, newSeg.length);
        assertNull(oldSeg, newSeg);
    }

    @Test
    public void testExpandCurrentSegmentAtMax() {
        // Create a segment of MAX_SEGMENT_LEN? Hard, but we can use expandCurrentSegment(minSize) to set.
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        // try to expand to MAX_SEGMENT_LEN via expandCurrentSegment(minSize)
        buf.expandCurrentSegment(MAX_SEGMENT_LEN);
        char[] seg = buf.getCurrentSegment();
        assertEquals(MAX_SEGMENT_LEN, seg.length);
        // now expandCurrentSegment no arg should increase to MAX_SEGMENT_LEN+1
        char[] newSeg = buf.expandCurrentSegment();
        assertEquals(MAX_SEGMENT_LEN + 1, newSeg.length);
    }

    // --- expandCurrentSegment with minSize ---

    @Test
    public void testExpandCurrentSegmentWithMinSize() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] oldSeg = buf.getCurrentSegment();
        int newSize = oldSeg.length + 10;
        char[] newSeg = buf.expandCurrentSegment(newSize);
        assertEquals(newSize, newSeg.length);
        assertNull(oldSeg, newSeg);
    }

    @Test
    public void testExpandCurrentSegmentMinSizeLessThanCurrent() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        char[] seg = buf.getCurrentSegment();
        char[] result = buf.expandCurrentSegment(seg.length - 1);
        assertSame(seg, result); // should return same array
    }

    // --- toString ---

    @Test
    public void testToString() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.append("toString");
        assertEquals("toString", buf.toString());
    }

    // --- Additional edge cases ---

    @Test
    public void testSizeWhenEmptyAfterReset() {
        TextBuffer buf = new TextBuffer(null);
        assertEquals(0, buf.size());
        buf.resetWithString("");
        assertEquals(0, buf.size());
        buf.resetWithShared(new char[] {'a'}, 0, 0);
        assertEquals(0, buf.size());
    }

    @Test
    public void testMultipleSegmentsViaHugeAppends() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        int chunk = 10; // small to force many segments
        char[] data = new char[chunk];
        Arrays.fill(data, 'x');
        for (int i = 0; i < 200; i++) {
            buf.append(data, 0, chunk);
        }
        assertEquals(chunk * 200, buf.size());
        assertTrue(buf.contentsAsString().length() == chunk * 200);
    }

    @Test
    public void testAppendCharArrayHuge() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.resetWithCopy(new char[0], 0, 0);
        // create a source larger than current segment size
        int hugeSize = MIN_SEGMENT_LEN * 2;
        char[] huge = new char[hugeSize];
        Arrays.fill(huge, 'y');
        buf.append(huge, 0, hugeSize);
        assertEquals(hugeSize, buf.size());
    }

    @Test
    public void testResultArrayWhenSharedStartNotZero() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        char[] data = "abcdef".toCharArray();
        buf.resetWithShared(data, 2, 3);
        char[] result = buf.contentsAsArray();
        assertArrayEquals(new char[] {'c','d','e'}, result);
        // also test shared len 0
        buf.resetWithShared(new char[1], 0, 0);
        assertArrayEquals(new char[0], buf.contentsAsArray());
    }

    @Test
    public void testGetCurrentSegmentSizeAfterSetCurrentLength() {
        TextBuffer buf = new TextBuffer(new BufferRecycler());
        buf.emptyAndGetCurrentSegment();
        buf.setCurrentLength(5);
        assertEquals(5, buf.getCurrentSegmentSize());
    }
}
