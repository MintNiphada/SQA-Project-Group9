package org.mockito.internal.creation.instance;

import org.junit.Assert;
import org.junit.Test;

public class ConstructorInstantiatorTest {

    public static class SimpleClass {
        public SimpleClass() {}
    }

    public static class ClassWithoutNoArgConstructor {
        public ClassWithoutNoArgConstructor(String param) {}
    }

    public static class ClassThrowingException {
        public ClassThrowingException() {
            throw new RuntimeException("Constructor failed intentionally");
        }
    }

    public static abstract class AbstractClass {
        public AbstractClass() {}
    }

    public interface SomeInterface {}

    public class OuterClass {
        public class InnerClass {
            public InnerClass() {}
        }
    }

    public class AnotherOuterClass {}

    public static class InnerThrowingException {
        public InnerThrowingException(OuterClass outer) {
            throw new RuntimeException("Inner constructor failed");
        }
    }

    @Test
    public void should_create_instance_with_no_arg_constructor_when_outer_is_null() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        SimpleClass instance = instantiator.newInstance(SimpleClass.class);

        Assert.assertNotNull(instance);
    }

    @Test
    public void should_fail_to_create_instance_without_no_arg_constructor_when_outer_is_null() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ClassWithoutNoArgConstructor.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'ClassWithoutNoArgConstructor'"));
            Assert.assertTrue(e.getMessage().contains("Please ensure it has parameter-less constructor."));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_fail_when_class_is_abstract_and_outer_is_null() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(AbstractClass.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'AbstractClass'"));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_fail_when_class_is_interface_and_outer_is_null() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(SomeInterface.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'SomeInterface'"));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_fail_when_constructor_throws_exception_and_outer_is_null() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ClassThrowingException.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'ClassThrowingException'"));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_create_instance_with_outer_class_instance() {
        OuterClass outerInstance = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        OuterClass.InnerClass instance = instantiator.newInstance(OuterClass.InnerClass.class);

        Assert.assertNotNull(instance);
    }

    @Test
    public void should_fail_to_create_instance_when_outer_class_type_mismatches() {
        AnotherOuterClass wrongOuter = new AnotherOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(wrongOuter);
        try {
            instantiator.newInstance(OuterClass.InnerClass.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'InnerClass'"));
            Assert.assertTrue(e.getMessage().contains("Please ensure that the outer instance has correct type"));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_fail_when_instantiating_standalone_class_with_outer_instance() {
        OuterClass outerInstance = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        try {
            instantiator.newInstance(SimpleClass.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'SimpleClass'"));
            Assert.assertTrue(e.getMessage().contains("Please ensure that the outer instance has correct type"));
            Assert.assertNotNull(e.getCause());
        }
    }

    @Test
    public void should_fail_when_inner_class_constructor_throws_exception() {
        OuterClass outerInstance = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outerInstance);
        try {
            instantiator.newInstance(InnerThrowingException.class);
            Assert.fail("Expected InstantationException");
        } catch (InstantationException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to create mock instance of 'InnerThrowingException'"));
            Assert.assertNotNull(e.getCause());
        }
    }
}
