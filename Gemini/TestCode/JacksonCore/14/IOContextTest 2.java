package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IOContextTest {

    private BufferRecycler _recycler;
    private Object _sourceRef;
    private IOContext _context;

    @Before
    public void setUp() {
        _recycler = new BufferRecycler();
        _sourceRef = "test-source-reference";
        _context = new IOContext(_recycler, _sourceRef, true);
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        assertTrue(_context.isResourceManaged());
        assertSame(_sourceRef, _context.getSourceReference());
        assertNull(_context.getEncoding());

        _context.setEncoding(JsonEncoding.UTF8);
        assertSame(JsonEncoding.UTF8, _context.getEncoding());

        IOContext unmanagedContext = new IOContext(_recycler, null, false);
        assertFalse(unmanagedContext.isResourceManaged());
        assertNull(unmanagedContext.getSourceReference());

        IOContext returned = unmanagedContext.withEncoding(JsonEncoding.UTF16_BE);
        assertSame(unmanagedContext, returned);
        assertSame(JsonEncoding.UTF16_BE, unmanagedContext.getEncoding());
    }

    @Test
    public void testConstructTextBuffer() {
        TextBuffer tb = _context.constructTextBuffer();
        assertNotNull(tb);
    }

    @Test
    public void testAllocAndReleaseReadIOBuffer() {
        byte[] buf = _context.allocReadIOBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        // Releasing with null is a no-op
        _context.releaseReadIOBuffer(null);

        // Release the allocated buffer
        _context.releaseReadIOBuffer(buf);

        // Can re-allocate after release
        byte[] buf2 = _context.allocReadIOBuffer(500);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 500);
        _context.releaseReadIOBuffer(buf2);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocReadIOBuffer() {
        _context.allocReadIOBuffer();
        _context.allocReadIOBuffer();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocReadIOBufferWithSize() {
        _context.allocReadIOBuffer(100);
        _context.allocReadIOBuffer(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidReadIOBufferSmaller() {
        _context.allocReadIOBuffer();
        byte[] wrong = new byte[10];
        _context.releaseReadIOBuffer(wrong);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidReadIOBufferSameSize() {
        byte[] buf = _context.allocReadIOBuffer();
        byte[] wrong = new byte[buf.length];
        _context.releaseReadIOBuffer(wrong);
    }

    @Test
    public void testReleaseUpgradedReadIOBuffer() {
        byte[] buf = _context.allocReadIOBuffer();
        byte[] larger = new byte[buf.length + 100];
        // Upgraded larger buffer should be accepted without throwing
        _context.releaseReadIOBuffer(larger);
    }

    @Test
    public void testAllocAndReleaseWriteEncodingBuffer() {
        byte[] buf = _context.allocWriteEncodingBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseWriteEncodingBuffer(null);
        _context.releaseWriteEncodingBuffer(buf);

        byte[] buf2 = _context.allocWriteEncodingBuffer(200);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 200);
        _context.releaseWriteEncodingBuffer(buf2);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocWriteEncodingBuffer() {
        _context.allocWriteEncodingBuffer();
        _context.allocWriteEncodingBuffer();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocWriteEncodingBufferWithSize() {
        _context.allocWriteEncodingBuffer(100);
        _context.allocWriteEncodingBuffer(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidWriteEncodingBuffer() {
        byte[] buf = _context.allocWriteEncodingBuffer();
        byte[] wrong = new byte[buf.length];
        _context.releaseWriteEncodingBuffer(wrong);
    }

    @Test
    public void testAllocAndReleaseBase64Buffer() {
        byte[] buf = _context.allocBase64Buffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseBase64Buffer(null);
        _context.releaseBase64Buffer(buf);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocBase64Buffer() {
        _context.allocBase64Buffer();
        _context.allocBase64Buffer();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidBase64Buffer() {
        byte[] buf = _context.allocBase64Buffer();
        byte[] wrong = new byte[buf.length];
        _context.releaseBase64Buffer(wrong);
    }

    @Test
    public void testAllocAndReleaseTokenBuffer() {
        char[] buf = _context.allocTokenBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseTokenBuffer(null);
        _context.releaseTokenBuffer(buf);

        char[] buf2 = _context.allocTokenBuffer(300);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 300);
        _context.releaseTokenBuffer(buf2);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocTokenBuffer() {
        _context.allocTokenBuffer();
        _context.allocTokenBuffer();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocTokenBufferWithSize() {
        _context.allocTokenBuffer(100);
        _context.allocTokenBuffer(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidTokenBuffer() {
        char[] buf = _context.allocTokenBuffer();
        char[] wrong = new char[buf.length];
        _context.releaseTokenBuffer(wrong);
    }

    @Test
    public void testReleaseUpgradedTokenBuffer() {
        char[] buf = _context.allocTokenBuffer();
        char[] larger = new char[buf.length + 50];
        _context.releaseTokenBuffer(larger);
    }

    @Test
    public void testAllocAndReleaseConcatBuffer() {
        char[] buf = _context.allocConcatBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseConcatBuffer(null);
        _context.releaseConcatBuffer(buf);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocConcatBuffer() {
        _context.allocConcatBuffer();
        _context.allocConcatBuffer();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidConcatBuffer() {
        char[] buf = _context.allocConcatBuffer();
        char[] wrong = new char[buf.length];
        _context.releaseConcatBuffer(wrong);
    }

    @Test
    public void testAllocAndReleaseNameCopyBuffer() {
        char[] buf = _context.allocNameCopyBuffer(150);
        assertNotNull(buf);
        assertTrue(buf.length >= 150);

        _context.releaseNameCopyBuffer(null);
        _context.releaseNameCopyBuffer(buf);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleAllocNameCopyBuffer() {
        _context.allocNameCopyBuffer(50);
        _context.allocNameCopyBuffer(50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReleaseInvalidNameCopyBuffer() {
        char[] buf = _context.allocNameCopyBuffer(50);
        char[] wrong = new char[buf.length];
        _context.releaseNameCopyBuffer(wrong);
    }

    @Test
    public void testReleaseUpgradedNameCopyBuffer() {
        char[] buf = _context.allocNameCopyBuffer(50);
        char[] larger = new char[buf.length + 50];
        _context.releaseNameCopyBuffer(larger);
    }

    @Test
    public void testSubclassOverrideHooks() {
        IOContext custom = new IOContext(_recycler, "subclass-test", false) {
            @Override
            public JsonEncoding getEncoding() {
                return JsonEncoding.UTF32_LE;
            }
        };
        assertEquals(JsonEncoding.UTF32_LE, custom.getEncoding());
        assertFalse(custom.isResourceManaged());
        assertEquals("subclass-test", custom.getSourceReference());
    }
}
