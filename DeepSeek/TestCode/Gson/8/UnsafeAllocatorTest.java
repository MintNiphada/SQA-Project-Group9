package com.google.gson.internal;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class UnsafeAllocatorTest {

    // A class with a private constructor to test instantiation without calling constructor
    private static class PrivateConstructorClass {
        private boolean constructorCalled = false;

        private PrivateConstructorClass() {
            constructorCalled = true;
        }

        public boolean isConstructorCalled() {
            return constructorCalled;
        }
    }

    @Test
    public void testCreateReturnsNonNullAllocator() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Assert.assertNotNull(allocator);
    }

    @Test
    public void testNewInstanceCreatesObjectWithoutCallingConstructor() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        PrivateConstructorClass instance = allocator.newInstance(PrivateConstructorClass.class);
        Assert.assertNotNull(instance);
        Assert.assertFalse("Constructor should not have been called", instance.isConstructorCalled());
    }

    @Test(expected = Exception.class)
    public void testNewInstanceWithInterfaceThrowsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(Runnable.class);
    }

    @Test(expected = Exception.class)
    public void testNewInstanceWithAbstractClassThrowsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(java.util.AbstractList.class);
    }

    @Test(expected = NullPointerException.class)
    public void testNewInstanceWithNullClassThrowsNullPointerException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(null);
    }

    @Test
    public void testFallbackAllocatorThrowsUnsupportedOperationException() throws Exception {
        // Use a custom class loader that hides sun.misc.Unsafe to force the fallback allocator
        ClassLoader customLoader = new ClassLoader(null) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("sun.misc.Unsafe")) {
                    throw new ClassNotFoundException(name);
                }
                // For all other classes, delegate to the system class loader
                return ClassLoader.getSystemClassLoader().loadClass(name);
            }
        };

        // Load UnsafeAllocator with the custom class loader
        Class<?> allocatorClass = customLoader.loadClass(UnsafeAllocator.class.getName());
        // Invoke the static create() method
        Method createMethod = allocatorClass.getDeclaredMethod("create");
        Object allocator = createMethod.invoke(null);

        // Now call newInstance on the fallback allocator, should throw UnsupportedOperationException
        Method newInstanceMethod = allocator.getClass().getMethod("newInstance", Class.class);
        try {
            newInstanceMethod.invoke(allocator, Object.class);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (Exception e) {
            // The invocation wraps the exception in InvocationTargetException
            Throwable cause = e.getCause();
            Assert.assertTrue("Cause should be UnsupportedOperationException",
                    cause instanceof UnsupportedOperationException);
            Assert.assertTrue("Exception message should contain class name",
                    cause.getMessage().contains(Object.class.getName()));
        }
    }
}
