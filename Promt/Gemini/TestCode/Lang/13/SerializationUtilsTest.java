package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationUtilsTest {

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;
        private List<String> hobbies;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
            this.hobbies = new ArrayList<String>();
        }

        public void addHobby(String hobby) {
            this.hobbies.add(hobby);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Person person = (Person) obj;
            if (age != person.age) return false;
            if (name != null ? !name.equals(person.name) : person.name != null) return false;
            return hobbies != null ? hobbies.equals(person.hobbies) : person.hobbies == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + age;
            result = 31 * result + (hobbies != null ? hobbies.hashCode() : 0);
            return result;
        }
    }

    static class NonSerializableObject {
        private String data = "not-serializable";
    }

    static class ContainerWithNonSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private NonSerializableObject nonSerializable = new NonSerializableObject();
    }

    static class ExceptionThrowingOutputStream extends OutputStream {
        private final boolean throwOnWrite;
        private final boolean throwOnClose;

        public ExceptionThrowingOutputStream(boolean throwOnWrite, boolean throwOnClose) {
            this.throwOnWrite = throwOnWrite;
            this.throwOnClose = throwOnClose;
        }

        @Override
        public void write(int b) throws IOException {
            if (throwOnWrite) {
                throw new IOException("Simulated write error");
            }
        }

        @Override
        public void close() throws IOException {
            if (throwOnClose) {
                throw new IOException("Simulated close error");
            }
        }
    }

    static class ExceptionThrowingInputStream extends InputStream {
        private final boolean throwOnRead;
        private final boolean throwOnClose;

        public ExceptionThrowingInputStream(boolean throwOnRead, boolean throwOnClose) {
            this.throwOnRead = throwOnRead;
            this.throwOnClose = throwOnClose;
        }

        @Override
        public int read() throws IOException {
            if (throwOnRead) {
                throw new IOException("Simulated read error");
            }
            return -1;
        }

        @Override
        public void close() throws IOException {
            if (throwOnClose) {
                throw new IOException("Simulated close error");
            }
        }
    }

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new SerializationUtils());
    }

    @Test
    public void testCloneNull() {
        Person nullPerson = null;
        Assert.assertNull(SerializationUtils.clone(nullPerson));
    }

    @Test
    public void testCloneValidObject() {
        Person original = new Person("Alice", 30);
        original.addHobby("Reading");
        original.addHobby("Cycling");

        Person cloned = SerializationUtils.clone(original);

        Assert.assertNotNull(cloned);
        Assert.assertNotSame(original, cloned);
        Assert.assertNotSame(original.hobbies, cloned.hobbies);
        Assert.assertEquals(original, cloned);
        Assert.assertEquals(original.name, cloned.name);
        Assert.assertEquals(original.age, cloned.age);
        Assert.assertEquals(original.hobbies, cloned.hobbies);
    }

    @Test
    public void testClonePrimitiveWrappersAndCollections() {
        Integer originalInt = 12345;
        Integer clonedInt = SerializationUtils.clone(originalInt);
        Assert.assertEquals(originalInt, clonedInt);

        HashMap<String, String> originalMap = new HashMap<String, String>();
        originalMap.put("key1", "val1");
        originalMap.put("key2", "val2");

        HashMap<String, String> clonedMap = SerializationUtils.clone(originalMap);
        Assert.assertNotSame(originalMap, clonedMap);
        Assert.assertEquals(originalMap, clonedMap);
    }

    @Test(expected = SerializationException.class)
    public void testCloneNonSerializable() {
        ContainerWithNonSerializable container = new ContainerWithNonSerializable();
        SerializationUtils.clone(container);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeNullOutputStream() {
        SerializationUtils.serialize("testString", null);
    }

    @Test
    public void testSerializeNullObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, baos);
        byte[] bytes = baos.toByteArray();
        Assert.assertTrue(bytes.length > 0);

        Object deserialized = SerializationUtils.deserialize(bytes);
        Assert.assertNull(deserialized);
    }

    @Test
    public void testSerializeValidObjectToStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String original = "Hello, World!";
        SerializationUtils.serialize(original, baos);

        byte[] bytes = baos.toByteArray();
        Assert.assertTrue(bytes.length > 0);

        Object deserialized = SerializationUtils.deserialize(bytes);
        Assert.assertEquals(original, deserialized);
    }

    @Test(expected = SerializationException.class)
    public void testSerializeNonSerializableObjectToStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(new ContainerWithNonSerializable(), baos);
    }

    @Test(expected = SerializationException.class)
    public void testSerializeStreamThrowsOnWrite() {
        ExceptionThrowingOutputStream out = new ExceptionThrowingOutputStream(true, false);
        SerializationUtils.serialize("data", out);
    }

    @Test
    public void testSerializeStreamThrowsOnCloseIgnored() {
        ExceptionThrowingOutputStream out = new ExceptionThrowingOutputStream(false, true);
        SerializationUtils.serialize("data", out);
    }

    @Test
    public void testSerializeToByteArray() {
        byte[] bytes = SerializationUtils.serialize("Test String");
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        Object deserialized = SerializationUtils.deserialize(bytes);
        Assert.assertEquals("Test String", deserialized);
    }

    @Test(expected = SerializationException.class)
    public void testSerializeToByteArrayNonSerializable() {
        SerializationUtils.serialize(new ContainerWithNonSerializable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNullInputStream() {
        SerializationUtils.deserialize((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNullByteArray() {
        SerializationUtils.deserialize((byte[]) null);
    }

    @Test
    public void testDeserializeValidInputStream() {
        byte[] bytes = SerializationUtils.serialize("Stream Deserialization");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        Object result = SerializationUtils.deserialize(bais);
        Assert.assertEquals("Stream Deserialization", result);
    }

    @Test
    public void testDeserializeValidByteArray() {
        byte[] bytes = SerializationUtils.serialize(Integer.valueOf(9999));
        Object result = SerializationUtils.deserialize(bytes);
        Assert.assertEquals(Integer.valueOf(9999), result);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeCorruptStream() {
        byte[] corruptBytes = new byte[]{1, 2, 3, 4, 5};
        SerializationUtils.deserialize(corruptBytes);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeStreamThrowsOnRead() {
        ExceptionThrowingInputStream in = new ExceptionThrowingInputStream(true, false);
        SerializationUtils.deserialize(in);
    }

    @Test
    public void testDeserializeStreamThrowsOnCloseIgnored() {
        byte[] bytes = SerializationUtils.serialize("close-test");
        InputStream wrapped = new ByteArrayInputStream(bytes) {
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated close exception");
            }
        };

        Object result = SerializationUtils.deserialize(wrapped);
        Assert.assertEquals("close-test", result);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeClassNotFound() {
        // Serialized representation of a class that does not exist
        // AC ED 00 05 73 72 00 1B ...
        byte[] serializedFakeClass = new byte[]{
                (byte) 0xac, (byte) 0xed, 0x00, 0x05, 0x73, 0x72, 0x00, 0x1f,
                'c', 'o', 'm', '.', 'f', 'a', 'k', 'e', '.', 'N', 'o', 'n', 'E', 'x', 'i', 's', 't', 'e', 'n', 't', 'C', 'l', 'a', 's', 's',
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
                0x02, 0x00, 0x00, 0x78, 0x70
        };
        SerializationUtils.deserialize(serializedFakeClass);
    }

    @Test
    public void testClassLoaderAwareObjectInputStreamWithCustomClassLoader() throws Exception {
        byte[] bytes = SerializationUtils.serialize(new Person("Bob", 40));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        ClassLoader classLoader = getClass().getClassLoader();
        SerializationUtils.ClassLoaderAwareObjectInputStream clIn =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, classLoader);

        Object readObj = clIn.readObject();
        Assert.assertNotNull(readObj);
        Assert.assertEquals(Person.class, readObj.getClass());
        clIn.close();
    }

    @Test
    public void testClassLoaderAwareObjectInputStreamFallbackToContextClassLoader() throws Exception {
        byte[] bytes = SerializationUtils.serialize(new Person("Charlie", 25));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        // A classloader that cannot resolve Person
        ClassLoader emptyClassLoader = new ClassLoader(null) {};

        SerializationUtils.ClassLoaderAwareObjectInputStream clIn =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, emptyClassLoader);

        Object readObj = clIn.readObject();
        Assert.assertNotNull(readObj);
        Assert.assertEquals(Person.class, readObj.getClass());
        clIn.close();
    }

    @Test
    public void testClassLoaderAwareObjectInputStreamClassNotFoundThrows() throws Exception {
        byte[] serializedFakeClass = new byte[]{
                (byte) 0xac, (byte) 0xed, 0x00, 0x05, 0x73, 0x72, 0x00, 0x1f,
                'c', 'o', 'm', '.', 'f', 'a', 'k', 'e', '.', 'N', 'o', 'n', 'E', 'x', 'i', 's', 't', 'e', 'n', 't', 'C', 'l', 'a', 's', 's',
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
                0x02, 0x00, 0x00, 0x78, 0x70
        };

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedFakeClass);
        SerializationUtils.ClassLoaderAwareObjectInputStream clIn =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, getClass().getClassLoader());

        try {
            clIn.readObject();
            Assert.fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // Expected
        } finally {
            clIn.close();
        }
    }
}
