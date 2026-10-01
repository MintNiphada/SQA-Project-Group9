package org.mockito.internal.creation;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class DelegatingMethodTest {

    // Helper interface and class for reflection
    public interface TestInterface {
        void abstractMethod();
        String methodWithParamsAndReturn(String param1, int param2) throws Exception;
 void varargsMethod(String... args);
    }

    public static class Concrete {
        public void concreteMethod() {}
    }

    @Test
    public void testConstructorValid() throws Exception {
        Method m = Object.class.getMethod("toString");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertSame(m, dm.getJavaMethod());
    }

    @Test
    public void testConstructorNull() {
        DelegatingMethod dm = new DelegatingMethod(null);
        // When assertions are disabled, the internal method is null        assertNull(dm.getJavaMethod());
    }

    @Test
    public void testGetExceptionTypes() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("methodWithParamsAndReturn", String.class, int.class);
        DelegatingMethod dm = new DelegatingMethod(m);
        Class<?>[] exceptions = dm.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(Exception.class, exceptions[0]);
    }

    @Test
    public void testGetName() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("abstractMethod");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertEquals("abstractMethod", dm.getName());
    }

    @Test
    public void testGetParameterTypes() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("methodWithParamsAndReturn", String.class, int.class);
        DelegatingMethod dm = new DelegatingMethod(m);
        Class<?>[] params = dm.getParameterTypes();
        assertEquals(2, params.length);
        assertEquals(String.class, params[0]);
        assertEquals(int.class, params[1]);
    }

    @Test
    public void testGetReturnType() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("methodWithParamsAndReturn", String.class, int.class);
        DelegatingMethod dm = new DelegatingMethod(m);
        assertEquals(String.class, dm.getReturnType());
    }

    @Test
    public void testIsVarArgsTrue() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("varargsMethod", String[].class);
        DelegatingMethod dm = new DelegatingMethod(m);
        assertTrue(dm.isVarArgs());
    }

    @Test
    public void testIsVarArgsFalse() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("abstractMethod");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertFalse(dm.isVarArgs());
    }

    @Test
    public void testIsAbstractTrue() throws Exception {
        Method m = TestInterface.class.getDeclaredMethod("abstractMethod");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertTrue(dm.isAbstract());
    }

    @Test
    public void testIsAbstractFalse() throws Exception {
        Method m = Concrete.class.getDeclaredMethod("concreteMethod");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertFalse(dm.isAbstract());
    }

    @Test
    public void testEqualsWithEqualMethodObject() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        DelegatingMethod dm = new DelegatingMethod(toStringMethod);
        assertTrue(dm.equals(toStringMethod));
    }

    @Test
    public void testEqualsWithDifferentMethodObject() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        Method hashCodeMethod = Object.class.getMethod("hashCode");
        DelegatingMethod dm = new DelegatingMethod(toStringMethod);
        assertFalse(dm.equals(hashCodeMethod));
    }

    @Test
    public void testEqualsWithDelegatingMethodSameInternal() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        DelegatingMethod dm1 = new DelegatingMethod(toStringMethod);
        DelegatingMethod dm2 = new DelegatingMethod(toStringMethod);
        // equals compares internal method with the other DelegatingMethod object, not its internal method        assertFalse(dm1.equals(dm2));
    }

    @Test
    public void testEqualsWithNull() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        DelegatingMethod dm = new DelegatingMethod(toStringMethod);
        assertFalse(dm.equals(null));
    }

    @Test
    public void testHashCode() throws Exception {
        Method m = Object.class.getMethod("toString");
        DelegatingMethod dm = new DelegatingMethod(m);
        assertEquals(1, dm.hashCode());
        // Hash code for any DelegatingMethod is always 1        DelegatingMethod dm2 = new DelegatingMethod(Object.class.getMethod("hashCode"));
        assertEquals(1, dm2.hashCode());
    }
}
