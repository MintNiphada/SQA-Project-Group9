package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.JsonDeserializer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class JdkDeserializersTest {

    @Test
    public void testConstructor() {
        JdkDeserializers deserializers = new JdkDeserializers();
        Assert.assertNotNull(deserializers);
    }

    @Test
    public void testFindUUID() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, UUID.class.getName());
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof UUIDDeserializer);
    }

    @Test
    public void testFindStackTraceElement() {
        JsonDeserializer<?> deser = JdkDeserializers.find(StackTraceElement.class, StackTraceElement.class.getName());
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof StackTraceElementDeserializer);
    }

    @Test
    public void testFindAtomicBoolean() {
        JsonDeserializer<?> deser = JdkDeserializers.find(AtomicBoolean.class, AtomicBoolean.class.getName());
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof AtomicBooleanDeserializer);
    }

    @Test
    public void testFindByteBuffer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(ByteBuffer.class, ByteBuffer.class.getName());
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof ByteBufferDeserializer);
    }

    @Test
    public void testFindFromStringDeserializerTypes() {
        Class<?>[] fromStringTypes = new Class<?>[] {
            File.class,
            URL.class,
            URI.class,
            Class.class,
            Currency.class,
            Pattern.class,
            Locale.class,
            Charset.class,
            TimeZone.class,
            InetAddress.class,
            InetSocketAddress.class
        };
        for (Class<?> cls : fromStringTypes) {
            JsonDeserializer<?> deser = JdkDeserializers.find(cls, cls.getName());
            Assert.assertNotNull(deser);
            Assert.assertTrue(deser instanceof FromStringDeserializer);
        }
    }

    @Test
    public void testFindFromStringDeserializerAllFromTypes() {
        for (Class<?> cls : FromStringDeserializer.types()) {
            JsonDeserializer<?> deser = JdkDeserializers.find(cls, cls.getName());
            Assert.assertNotNull(deser);
        }
    }

    @Test
    public void testFindUnrecognizedClass() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, String.class.getName());
        Assert.assertNull(deser);
    }

    @Test
    public void testFindNullClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, null);
        Assert.assertNull(deser);
    }

    @Test
    public void testFindMismatchedTypeAndClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, UUID.class.getName());
        Assert.assertNull(deser);
    }

    @Test
    public void testFindMismatchedFromStringTypeAndClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, File.class.getName());
        Assert.assertNull(deser);
    }
}
