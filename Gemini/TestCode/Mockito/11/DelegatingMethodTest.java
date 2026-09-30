package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DelegatingMethodTest {

    private interface TestInterface {
        void abstractMethod(String arg1, int arg2) throws IOException, IllegalArgumentException;
        void varArgsMethod(int first, String... rest);
    }

    private static class TestClass implements TestInterface {
        @Override
        public void abstractMethod(String arg1, int arg2) throws IOException, IllegalArgumentException {
        }

        @Override
        public void varArgsMethod(int first, String... rest) {
        }

        public String concreteMethod() {
            return "result";
        }
    }

    private Method abstractMethod;
    private Method varArgsMethod;
    private Method concreteMethod;

    @Before
    public void setUp() throws NoSuchMethodException {
        abstractMethod = TestInterface.class.getMethod("abstractMethod", String.class, int.class);
        varArgsMethod = TestClass.class.getMethod("varArgsMethod", int.class, String[].class);
        concreteMethod = TestClass.class.getMethod("concreteMethod");
    }

    @Test
    public void shouldReturnCorrectJavaMethod() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);
        assertSame(concreteMethod, delegatingMethod.getJavaMethod());
    }

    @Test
    public void shouldReturnCorrectName() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);
        assertEquals("concreteMethod", delegatingMethod.getName());
    }

    @Test
    public void shouldReturnCorrectReturnType() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);
        assertEquals(String.class, delegatingMethod.getReturnType());

        DelegatingMethod voidMethod = new DelegatingMethod(abstractMethod);
        assertEquals(Void.TYPE, voidMethod.getReturnType());
    }

    @Test
    public void shouldReturnCorrectParameterTypes() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(abstractMethod);
        Class<?>[] parameterTypes = delegatingMethod.getParameterTypes();
        assertArrayEquals(new Class<?>[]{String.class, int.class}, parameterTypes);

        DelegatingMethod noArgsMethod = new DelegatingMethod(concreteMethod);
        assertArrayEquals(new Class<?>[0], noArgsMethod.getParameterTypes());
    }

    @Test
    public void shouldReturnCorrectExceptionTypes() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(abstractMethod);
        Class<?>[] exceptionTypes = delegatingMethod.getExceptionTypes();
        assertArrayEquals(new Class<?>[]{IOException.class, IllegalArgumentException.class}, exceptionTypes);

        DelegatingMethod noExceptionsMethod = new DelegatingMethod(concreteMethod);
        assertArrayEquals(new Class<?>[0], noExceptionsMethod.getExceptionTypes());
    }

    @Test
    public void shouldIdentifyVarArgs() {
        DelegatingMethod varArgsDelegating = new DelegatingMethod(varArgsMethod);
        assertTrue(varArgsDelegating.isVarArgs());

        DelegatingMethod nonVarArgsDelegating = new DelegatingMethod(concreteMethod);
        assertFalse(nonVarArgsDelegating.isVarArgs());
    }

    @Test
    public void shouldIdentifyAbstractMethod() {
        DelegatingMethod abstractDelegating = new DelegatingMethod(abstractMethod);
        assertTrue(abstractDelegating.isAbstract());

        DelegatingMethod concreteDelegating = new DelegatingMethod(concreteMethod);
        assertFalse(concreteDelegating.isAbstract());
    }

    @Test
    public void shouldDelegateEqualsToUnderlyingMethod() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);

        assertTrue(delegatingMethod.equals(concreteMethod));
        assertFalse(delegatingMethod.equals(abstractMethod));
        assertFalse(delegatingMethod.equals(null));
        assertFalse(delegatingMethod.equals("some string"));
    }

    @Test
    public void shouldReturnConsistentHashCode() {
        DelegatingMethod delegatingMethod1 = new DelegatingMethod(concreteMethod);
        DelegatingMethod delegatingMethod2 = new DelegatingMethod(abstractMethod);

        assertEquals(1, delegatingMethod1.hashCode());
        assertEquals(1, delegatingMethod2.hashCode());
    }

    @Test
    public void shouldInstantiateWithNonNullMethod() {
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);
        assertNotNull(delegatingMethod);
    }
}
