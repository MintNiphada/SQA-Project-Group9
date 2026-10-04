package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SerializationUtilsTest {

    // A simple serializable class for testing
    private static class TestSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        private String data;

        public TestSerializable(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestSerializable that = (TestSerializable) obj;
            return data != null ? data.equals(that.data) : that.data == null;
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }

    // Custom OutputStream that throws IOException on write
    private static class FailingOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("write failed");
        }
    }

    // Custom InputStream that throws IOException on read
    private static class FailingInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("read failed");
        }
    }

    // Custom ClassLoader that refuses to load a specific class
    private static class RestrictedClassLoader extends ClassLoader {
        private final String forbiddenClassName;

        public RestrictedClassLoader(ClassLoader parent, String forbiddenClassName) {
            super(parent);
            this.forbiddenClassName = forbiddenClassName;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(forbiddenClassName)) {
                throw new ClassNotFoundException("Forbidden: " + name);
            }
            return super.loadClass(name);
        }
    }

    private ClassLoader originalContextClassLoader;

    @Before
    public void setUp() {
        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() {
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    // --- clone tests ---

    @Test
    public void testCloneNull() {
        assertNull(SerializationUtils.clone(null));
    }

    @Test
    public void testCloneValidObject() {
        TestSerializable original = new TestSerializable("hello");
        TestSerializable cloned = SerializationUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
    }

    @Test
    public void testCloneString() {
        String original = "test";
        String cloned = SerializationUtils.clone(original);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    // --- serialize(Serializable, OutputStream) tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeToStreamNullOutputStream() {
        SerializationUtils.serialize(new TestSerializable("data"), null);
    }

    @Test
    public void testSerializeToStreamValid() throws IOException {
        TestSerializable obj = new TestSerializable("data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(obj, baos);
        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
        // verify deserialization works
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        TestSerializable deserialized = (TestSerializable) ois.readObject();
        assertEquals(obj, deserialized);
    }

    @Test
    public void testSerializeToStreamNullObject() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, baos);
        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        assertNull(ois.readObject());
    }

    @Test(expected = SerializationException.class)
    public void testSerializeToStreamFailingOutputStream() {
        SerializationUtils.serialize(new TestSerializable("data"), new FailingOutputStream());
    }

    // --- serialize(Serializable) tests ---

    @Test
    public void testSerializeToBytesNull() {
        byte[] bytes = SerializationUtils.serialize(null);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testSerializeToBytesValid() {
        TestSerializable obj = new TestSerializable("data");
        byte[] bytes = SerializationUtils.serialize(obj);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    // --- deserialize(InputStream) tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeStreamNullInputStream() {
        SerializationUtils.deserialize((InputStream) null);
    }

    @Test
    public void testDeserializeStreamValid() throws IOException {
        TestSerializable obj = new TestSerializable("data");
        byte[] bytes = SerializationUtils.serialize(obj);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        TestSerializable result = (TestSerializable) SerializationUtils.deserialize(bais);
        assertEquals(obj, result);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeStreamFailingInputStream() {
        SerializationUtils.deserialize(new FailingInputStream());
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeStreamCorruptedData() {
        byte[] corrupted = new byte[]{1, 2, 3}; // not a valid serialization stream
        ByteArrayInputStream bais = new ByteArrayInputStream(corrupted);
        SerializationUtils.deserialize(bais);
    }

    // --- deserialize(byte[]) tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeBytesNullArray() {
        SerializationUtils.deserialize((byte[]) null);
    }

    @Test
    public void testDeserializeBytesValid() {
        TestSerializable obj = new TestSerializable("data");
        byte[] bytes = SerializationUtils.serialize(obj);
        TestSerializable result = (TestSerializable) SerializationUtils.deserialize(bytes);
        assertEquals(obj, result);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeBytesCorruptedData() {
        byte[] corrupted = new byte[]{1, 2, 3};
        SerializationUtils.deserialize(corrupted);
    }

    // --- constructor test ---

    @Test
    public void testConstructor() {
        new SerializationUtils(); // just for coverage
    }

    // --- ClassLoaderAwareObjectInputStream tests ---

    @Test
    public void testClassLoaderAwareObjectInputStreamConstructor() throws IOException {
        byte[] header = createSerializationHeader();
        ByteArrayInputStream bais = new ByteArrayInputStream(header);
        SerializationUtils.ClassLoaderAwareObjectInputStream in =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, getClass().getClassLoader());
        assertNotNull(in);
        in.close();
    }

    @Test
    public void testResolveClassFallbackToContextClassLoader() throws Exception {
        TestSerializable obj = new TestSerializable("fallback");
        byte[] bytes = SerializationUtils.serialize(obj);

        // Create a classloader that refuses to load TestSerializable
        ClassLoader forbiddenLoader = new RestrictedClassLoader(getClass().getClassLoader(), TestSerializable.class.getName());

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        SerializationUtils.ClassLoaderAwareObjectInputStream in =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, forbiddenLoader);

        // Context classloader is the original (can load TestSerializable)
        Object result = in.readObject();
        assertTrue(result instanceof TestSerializable);
        assertEquals(obj, result);
        in.close();
    }

    @Test(expected = ClassNotFoundException.class)
    public void testResolveClassBothClassLoadersFail() throws Exception {
        TestSerializable obj = new TestSerializable("fail");
        byte[] bytes = SerializationUtils.serialize(obj);

        // Forbid loading in both the provided classloader and the context classloader
        ClassLoader forbiddenLoader = new RestrictedClassLoader(getClass().getClassLoader(), TestSerializable.class.getName());
        ClassLoader contextForbiddenLoader = new RestrictedClassLoader(getClass().getClassLoader(), TestSerializable.class.getName());
        Thread.currentThread().setContextClassLoader(contextForbiddenLoader);

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        SerializationUtils.ClassLoaderAwareObjectInputStream in =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, forbiddenLoader);

        try {
            in.readObject();
        } finally {
            in.close();
        }
    }

    // Helper to create a valid serialization stream header
    private byte[] createSerializationHeader() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(null); // write something to generate header
        oos.close();
        byte[] full = baos.toByteArray();
        // The header is the first few bytes; we can just return the whole stream,
        // but the readObject will consume the null. To just test constructor, we need a stream
        // that has at least the header. We'll return the full stream and then read the null.
        // For constructor test, we'll just use the full stream and read the null.
        return full;
    }
}
