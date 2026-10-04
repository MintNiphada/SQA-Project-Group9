package org.jsoup;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UncheckedIOExceptionTest {

    @Test
    public void testConstructorAndIOExceptionGetter() {
        IOException cause = new IOException("Test IO error");
        UncheckedIOException unchecked = new UncheckedIOException(cause);

        Assert.assertSame(cause, unchecked.getCause());
        Assert.assertSame(cause, unchecked.ioException());
        Assert.assertEquals("java.io.IOException: Test IO error", unchecked.getMessage());
    }

    @Test
    public void testConstructorWithNullCause() {
        UncheckedIOException unchecked = new UncheckedIOException(null);

        Assert.assertNull(unchecked.getCause());
        Assert.assertNull(unchecked.ioException());
    }

    @Test
    public void testIOExceptionCustomSubclass() {
        class CustomIOException extends IOException {
            CustomIOException(String msg) {
                super(msg);
            }
        }

        CustomIOException cause = new CustomIOException("Subclass error");
        UncheckedIOException unchecked = new UncheckedIOException(cause);

        Assert.assertSame(cause, unchecked.ioException());
        Assert.assertTrue(unchecked.ioException() instanceof CustomIOException);
    }
}
