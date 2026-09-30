package com.google.gson.internal;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class UnsafeAllocatorTest {

    static class SimpleClass {
        int a = 5;
        String b = "hello";

        public SimpleClass() {
            this.a = 10;
            this.b = "world";
        }
    }

    static class ConstructorThrows {
        public ConstructorThrows() {
            throw new AssertionError("Constructor must not be invoked");
        }
    }

    static class PrivateNoArg {
        private final int value;

        private PrivateNoArg() {
            this.value = 42;
        }

        public int getValue() {
            return value;
        }
    }

    abstract static class AbstractClass {
        abstract void foo();
    }

    interface AnInterface {
        void bar();
    }

    @Test
    public void testCreateReturnsNonNullAllocator() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Assert.assertNotNull(allocator);
    }

    @Test
    public void testInstantiateSimpleClassWithoutConstructor() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        SimpleClass instance = allocator.newInstance(SimpleClass.class);

        Assert.assertNotNull(instance);
        Assert.assertEquals(0, instance.a);
        Assert.assertNull(instance.b);
    }

    @Test
    public void testInstantiateClassWithThrowingConstructor() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        ConstructorThrows instance = allocator.newInstance(ConstructorThrows.class);

        Assert.assertNotNull(instance);
        Assert.assertTrue(instance instanceof ConstructorThrows);
    }

    @Test
    public void testInstantiateClassWithPrivateConstructor() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        PrivateNoArg instance = allocator.newInstance(PrivateNoArg.class);

        Assert.assertNotNull(instance);
        Assert.assertEquals(0, instance.getValue());
    }

    @Test
    public void testInstantiateStandardJavaClass() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Integer instance = allocator.newInstance(Integer.class);

        Assert.assertNotNull(instance);
    }

    @Test
    public void testFallbackAllocatorThrowsUnsupportedOperationException() {
        UnsafeAllocator fallbackAllocator = new UnsafeAllocator() {
            @Override
            public <T> T newInstance(Class<T> c) {
                throw new UnsupportedOperationException("Cannot allocate " + c);
            }
        };

        try {
            fallbackAllocator.newInstance(SimpleClass.class);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot allocate"));
            Assert.assertTrue(e.getMessage().contains(SimpleClass.class.getName()));
        }
    }

    @Test
    public void testMultipleAllocationsProduceDistinctInstances() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        SimpleClass first = allocator.newInstance(SimpleClass.class);
        SimpleClass second = allocator.newInstance(SimpleClass.class);

        Assert.assertNotNull(first);
        Assert.assertNotNull(second);
        Assert.assertNotSame(first, second);
    }

    @Test
    public void testCustomSubclassImplementation() throws Exception {
        UnsafeAllocator customAllocator = new UnsafeAllocator() {
            @Override
            public <T> T newInstance(Class<T> c) throws Exception {
                return c.getDeclaredConstructor().newInstance();
            }
        };

        SimpleClass instance = customAllocator.newInstance(SimpleClass.class);
        Assert.assertNotNull(instance);
        Assert.assertEquals(10, instance.a);
        Assert.assertEquals("world", instance.b);
    }

    @Test
    public void testInstantiateInterfaceOrAbstractClassThrowsOrAllocates() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        try {
            AbstractClass abs = allocator.newInstance(AbstractClass.class);
            Assert.assertNotNull(abs);
        } catch (Exception expected) {
            Assert.assertTrue(expected instanceof InstantiationException
                    || expected instanceof InvocationTargetException
                    || expected instanceof UnsupportedOperationException);
        }

        try {
            AnInterface iface = allocator.newInstance(AnInterface.class);
            Assert.assertNotNull(iface);
        } catch (Exception expected) {
            Assert.assertTrue(expected instanceof InstantiationException
                    || expected instanceof InvocationTargetException
                    || expected instanceof UnsupportedOperationException);
        }
    }
}
