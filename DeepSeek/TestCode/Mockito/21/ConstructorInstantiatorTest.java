package org.mockito.internal.creation.instance;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConstructorInstantiatorTest {

    public static class PublicNoArg {
        // public no-arg constructor implicitly
    }

    public static class PrivateConstructor {
        private PrivateConstructor() {}
    }

    public static class WithOuterConstructor {
        private final Object outer;
        public WithOuterConstructor(String outer) {
            this.outer = outer;
        }
        public Object getOuter() { return outer; }
    }

    public static class WithoutMatchingConstructor {
        // no constructor taking String
    }

    public static class ThrowingNoArg {
        public ThrowingNoArg() {
            throw new RuntimeException("boom in no-arg");
        }
    }

    public static class ThrowingConstructor {
        public ThrowingConstructor(String s) {
            throw new RuntimeException("boom in constructor");
        }
    }

    @Test
    public void testNewInstance_NullOuterClass_NoArgConstructorSuccess() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        PublicNoArg instance = instantiator.newInstance(PublicNoArg.class);
        assertNotNull(instance);
        assertTrue(instance instanceof PublicNoArg);
    }

    @Test
    public void testNewInstance_NullOuterClass_NoArgConstructorFailure_PrivateConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(PrivateConstructor.class);
            fail("Expected InstantiationException");
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("parameter-less constructor"));
            assertTrue(e.getMessage().contains(PrivateConstructor.class.getSimpleName()));
        }
    }

    @Test
    public void testNewInstance_NullOuterClass_NoArgConstructorFailure_Throwing() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ThrowingNoArg.class);
            fail("Expected InstantiationException");
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("parameter-less constructor"));
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("boom in no-arg", e.getCause().getMessage());
        }
    }

    @Test
    public void testNewInstance_NonNullOuterClass_WithOuterClassSuccess() {
        String outerInstance = "hello";
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        WithOuterConstructor result = instantiator.newInstance(WithOuterConstructor.class);
        assertNotNull(result);
        assertSame(outerInstance, result.getOuter());
    }

    @Test
    public void testNewInstance_NonNullOuterClass_NoMatchingConstructor() {
        String outerInstance = "hello";
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        try {
            instantiator.newInstance(WithoutMatchingConstructor.class);
            fail("Expected InstantiationException");
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type"));
            assertTrue(e.getMessage().contains(WithoutMatchingConstructor.class.getSimpleName()));
        }
    }

    @Test    public void testNewInstance_NonNullOuterClass_ThrowingConstructor() {
        String outerInstance = "hello";
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        try {
            instantiator.newInstance(ThrowingConstructor.class);
            fail("Expected InstantiationException");
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type"));
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("boom in constructor", e.getCause().getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNewInstance_NullCls_NullOuterClass() {
        new ConstructorInstantiator(null).newInstance(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNewInstance_NullCls_NonNullOuterClass() {
        new ConstructorInstantiator("hello").newInstance(null);
    }
}
