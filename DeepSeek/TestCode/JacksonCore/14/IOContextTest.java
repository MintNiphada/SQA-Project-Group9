package com.fasterxml.jackson.core.io;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

public class IOContextTest {

    private BufferRecycler recycler;
    private IOContext ioContext;

    @Before
    public void setUp() {
        recycler = new TestBufferRecycler();
        ioContext = new IOContext(recycler, "source", true);
    }

    // ----------------------------------------------------------------
    // Constructor and accessors
    // ----------------------------------------------------------------

    @Test
    public void testConstructorAndGetters() {
        Object src = new Object();
        IOContext ctx = new IOContext(recycler, src, false);
        assertSame(src, ctx.getSourceReference());
        assertFalse(ctx.isResourceManaged());
        assertNull(ctx.getEncoding());
    }

    @Test
    public void testConstructorWithNullSource() {
        IOContext ctx = new IOContext(recycler, null, true);
        assertNull(ctx.getSourceReference());
        assertTrue(ctx.isResourceManaged());
    }

    @Test
    public void testSetEncoding() {
        assertNull(ioContext.getEncoding());
        ioContext.setEncoding(JsonEncoding.UTF8);
        assertSame(JsonEncoding.UTF8, ioContext.getEncoding());
    }

    @Test
    public void testWithEncoding() {
        IOContext returned = ioContext.withEncoding(JsonEncoding.UTF16_BE);
        assertSame(ioContext, returned);
        assertSame(JsonEncoding.UTF16_BE, ioContext.getEncoding());
    }

    @Test
    public void testConstructTextBuffer() {
        TextBuffer tb = ioContext.constructTextBuffer();
        assertNotNull(tb);
        // can't easily verify internal recycler, but at least not null
    }

    // ----------------------------------------------------------------
    // allocReadIOBuffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocReadIOBuffer() {
        byte[] buf = ioContext.allocReadIOBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);
        // second call should throw
        try {
            ioContext.allocReadIOBuffer();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testAllocReadIOBufferWithMinSize() {
        byte[] buf = ioContext.allocReadIOBuffer(50);
        assertNotNull(buf);
        assertTrue(buf.length >= 50);
    }

    @Test
    public void testAllocReadIOBufferAfterRelease() {
        byte[] buf = ioContext.allocReadIOBuffer();
        ioContext.releaseReadIOBuffer(buf);
        // should be able to allocate again
        byte[] buf2 = ioContext.allocReadIOBuffer();
        assertNotNull(buf2);
        assertNotSame(buf, buf2);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocReadIOBufferDoubleCallThrows() {
        ioContext.allocReadIOBuffer();
        ioContext.allocReadIOBuffer(); // should throw
    }

    // ----------------------------------------------------------------
    // allocWriteEncodingBuffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocWriteEncodingBuffer() {
        byte[] buf = ioContext.allocWriteEncodingBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    @Test
    public void testAllocWriteEncodingBufferWithMinSize() {
        byte[] buf = ioContext.allocWriteEncodingBuffer(30);
        assertNotNull(buf);
        assertTrue(buf.length >= 30);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocWriteEncodingBufferDoubleCallThrows() {
        ioContext.allocWriteEncodingBuffer();
        ioContext.allocWriteEncodingBuffer();
    }

    // ----------------------------------------------------------------
    // allocBase64Buffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocBase64Buffer() {
        byte[] buf = ioContext.allocBase64Buffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocBase64BufferDoubleCallThrows() {
        ioContext.allocBase64Buffer();
        ioContext.allocBase64Buffer();
    }

    // ----------------------------------------------------------------
    // allocTokenBuffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocTokenBuffer() {
        char[] buf = ioContext.allocTokenBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    @Test
    public void testAllocTokenBufferWithMinSize() {
        char[] buf = ioContext.allocTokenBuffer(25);
        assertNotNull(buf);
        assertTrue(buf.length >= 25);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocTokenBufferDoubleCallThrows() {
        ioContext.allocTokenBuffer();
        ioContext.allocTokenBuffer();
    }

    // ----------------------------------------------------------------
    // allocConcatBuffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocConcatBuffer() {
        char[] buf = ioContext.allocConcatBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocConcatBufferDoubleCallThrows() {
        ioContext.allocConcatBuffer();
        ioContext.allocConcatBuffer();
    }

    // ----------------------------------------------------------------
    // allocNameCopyBuffer
    // ----------------------------------------------------------------

    @Test
    public void testAllocNameCopyBuffer() {
        char[] buf = ioContext.allocNameCopyBuffer(40);
        assertNotNull(buf);
        assertTrue(buf.length >= 40);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocNameCopyBufferDoubleCallThrows() {
        ioContext.allocNameCopyBuffer(10);
        ioContext.allocNameCopyBuffer(10);
    }

    // ----------------------------------------------------------------
    // releaseReadIOBuffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseReadIOBufferNormal() {
        byte[] buf = ioContext.allocReadIOBuffer();
        ioContext.releaseReadIOBuffer(buf);
        // field should be null now
        // we can't directly inspect, but we can allocate again
        byte[] buf2 = ioContext.allocReadIOBuffer();
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseReadIOBufferNull() {
        // should do nothing
        ioContext.releaseReadIOBuffer(null);
        // still can allocate
        byte[] buf = ioContext.allocReadIOBuffer();
        assertNotNull(buf);
    }

    @Test(expected = NullPointerException.class)
    public void testReleaseReadIOBufferTwiceThrowsNPE() {
        byte[] buf = ioContext.allocReadIOBuffer();
        ioContext.releaseReadIOBuffer(buf);
        ioContext.releaseReadIOBuffer(buf); // _readIOBuffer is null -> NPE in _verifyRelease
    }

    @Test
    public void testReleaseReadIOBufferUpgradeAllowed() {
        byte[] original = ioContext.allocReadIOBuffer(); // size 100
        byte[] larger = new byte[200];
        // releasing a larger, different buffer should be allowed
        ioContext.releaseReadIOBuffer(larger);
        // field should be null, and larger buffer released
        assertTrue(((TestBufferRecycler) recycler).lastReleasedByteBuffer == larger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseReadIOBufferSmallerDifferentThrows() {
        byte[] original = ioContext.allocReadIOBuffer(); // size 100
        byte[] smaller = new byte[50];
        ioContext.releaseReadIOBuffer(smaller);
    }

    @Test
    public void testReleaseReadIOBufferSameBufferDoesNotThrow() {
        byte[] buf = ioContext.allocReadIOBuffer();
        ioContext.releaseReadIOBuffer(buf); // same reference, allowed
    }

    // ----------------------------------------------------------------
    // releaseWriteEncodingBuffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseWriteEncodingBufferNormal() {
        byte[] buf = ioContext.allocWriteEncodingBuffer();
        ioContext.releaseWriteEncodingBuffer(buf);
        byte[] buf2 = ioContext.allocWriteEncodingBuffer();
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseWriteEncodingBufferNull() {
        ioContext.releaseWriteEncodingBuffer(null);
        byte[] buf = ioContext.allocWriteEncodingBuffer();
        assertNotNull(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseWriteEncodingBufferSmallerDifferentThrows() {
        byte[] original = ioContext.allocWriteEncodingBuffer();
        byte[] smaller = new byte[original.length - 1];
        ioContext.releaseWriteEncodingBuffer(smaller);
    }

    @Test
    public void testReleaseWriteEncodingBufferUpgradeAllowed() {
        byte[] original = ioContext.allocWriteEncodingBuffer();
        byte[] larger = new byte[original.length + 10];
        ioContext.releaseWriteEncodingBuffer(larger);
        assertTrue(((TestBufferRecycler) recycler).lastReleasedByteBuffer == larger);
    }

    // ----------------------------------------------------------------
    // releaseBase64Buffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseBase64BufferNormal() {
        byte[] buf = ioContext.allocBase64Buffer();
        ioContext.releaseBase64Buffer(buf);
        byte[] buf2 = ioContext.allocBase64Buffer();
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseBase64BufferNull() {
        ioContext.releaseBase64Buffer(null);
        byte[] buf = ioContext.allocBase64Buffer();
        assertNotNull(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseBase64BufferSmallerDifferentThrows() {
        byte[] original = ioContext.allocBase64Buffer();
        byte[] smaller = new byte[1];
        ioContext.releaseBase64Buffer(smaller);
    }

    // ----------------------------------------------------------------
    // releaseTokenBuffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseTokenBufferNormal() {
        char[] buf = ioContext.allocTokenBuffer();
        ioContext.releaseTokenBuffer(buf);
        char[] buf2 = ioContext.allocTokenBuffer();
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseTokenBufferNull() {
        ioContext.releaseTokenBuffer(null);
        char[] buf = ioContext.allocTokenBuffer();
        assertNotNull(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseTokenBufferSmallerDifferentThrows() {
        char[] original = ioContext.allocTokenBuffer();
        char[] smaller = new char[original.length - 1];
        ioContext.releaseTokenBuffer(smaller);
    }

    @Test
    public void testReleaseTokenBufferUpgradeAllowed() {
        char[] original = ioContext.allocTokenBuffer();
        char[] larger = new char[original.length + 5];
        ioContext.releaseTokenBuffer(larger);
        assertTrue(((TestBufferRecycler) recycler).lastReleasedCharBuffer == larger);
    }

    // ----------------------------------------------------------------
    // releaseConcatBuffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseConcatBufferNormal() {
        char[] buf = ioContext.allocConcatBuffer();
        ioContext.releaseConcatBuffer(buf);
        char[] buf2 = ioContext.allocConcatBuffer();
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseConcatBufferNull() {
        ioContext.releaseConcatBuffer(null);
        char[] buf = ioContext.allocConcatBuffer();
        assertNotNull(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseConcatBufferSmallerDifferentThrows() {
        char[] original = ioContext.allocConcatBuffer();
        char[] smaller = new char[1];
        ioContext.releaseConcatBuffer(smaller);
    }

    // ----------------------------------------------------------------
    // releaseNameCopyBuffer
    // ----------------------------------------------------------------

    @Test
    public void testReleaseNameCopyBufferNormal() {
        char[] buf = ioContext.allocNameCopyBuffer(30);
        ioContext.releaseNameCopyBuffer(buf);
        char[] buf2 = ioContext.allocNameCopyBuffer(30);
        assertNotNull(buf2);
    }

    @Test
    public void testReleaseNameCopyBufferNull() {
        ioContext.releaseNameCopyBuffer(null);
        char[] buf = ioContext.allocNameCopyBuffer(20);
        assertNotNull(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseNameCopyBufferSmallerDifferentThrows() {
        char[] original = ioContext.allocNameCopyBuffer(50);
        char[] smaller = new char[10];
        ioContext.releaseNameCopyBuffer(smaller);
    }

    // ----------------------------------------------------------------
    // Edge cases with null recycler
    // ----------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testAllocWithNullRecyclerThrowsNPE() {
        IOContext ctx = new IOContext(null, "src", false);
        ctx.allocReadIOBuffer();
    }

    // ----------------------------------------------------------------
    // TestBufferRecycler implementation
    // ----------------------------------------------------------------

    static class TestBufferRecycler extends BufferRecycler {
        byte[] lastReleasedByteBuffer;
        char[] lastReleasedCharBuffer;

        @Override
        public byte[] allocByteBuffer(int type) {
            return new byte[100];
        }

        @Override
        public byte[] allocByteBuffer(int type, int minSize) {
            return new byte[minSize];
        }

        @Override
        public void releaseByteBuffer(int type, byte[] buf) {
            lastReleasedByteBuffer = buf;
        }

        @Override
        public char[] allocCharBuffer(int type) {
            return new char[100];
        }

        @Override
        public char[] allocCharBuffer(int type, int minSize) {
            return new char[minSize];
        }

        @Override
        public void releaseCharBuffer(int type, char[] buf) {
            lastReleasedCharBuffer = buf;
        }
    }
}
