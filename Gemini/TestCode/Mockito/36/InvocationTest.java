package org.mockito.internal.invocation;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.reporting.PrintSettings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class InvocationTest {

    private static class DummyClass {
        public void voidMethod() {}
        public int primitiveMethod(int x) { return x; }
        public String varArgMethod(String prefix, String... args) { return prefix; }
        public void exceptionMethod() throws IOException, IllegalArgumentException {}
        public String toString() { return "DummyClassInstance"; }
    }

    private static class DummyRealMethod implements RealMethod {
        private static final long serialVersionUID = 1L;
        private final Object returnValue;
        public DummyRealMethod(Object returnValue) {
            this.returnValue = returnValue;
        }
        public Object invoke(Object target, Object[] arguments) throws Throwable {
            return returnValue;
        }
    }

    private MockitoMethod createMockitoMethod(String methodName, Class<?>... paramTypes) {
        try {
            Method m = DummyClass.class.getMethod(methodName, paramTypes);
            return new MockitoMethodImpl(m);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MockitoMethodImpl implements MockitoMethod {
        private final Method method;
        public MockitoMethodImpl(Method method) {
            this.method = method;
        }
        public String getName() {
            return method.getName();
        }
        public Class<?> getReturnType() {
            return method.getReturnType();
        }
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }
        public Class<?>[] getExceptionTypes() {
            return method.getExceptionTypes();
        }
        public boolean isVarArgs() {
            return method.isVarArgs();
        }
        public Method getJavaMethod() {
            return method;
        }
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("voidMethod");
        RealMethod realMethod = new DummyRealMethod(null);
        Object[] args = new Object[0];

        Invocation invocation = new Invocation(mock, method, args, 1, realMethod);

        Assert.assertSame(mock, invocation.getMock());
        Assert.assertSame(method, invocation.getMethod());
        Assert.assertArrayEquals(args, invocation.getArguments());
        Assert.assertArrayEquals(args, invocation.getRawArguments());
        Assert.assertEquals(Integer.valueOf(1), invocation.getSequenceNumber());
        Assert.assertNotNull(invocation.getLocation());
        Assert.assertEquals(0, invocation.getArgumentsCount());
        Assert.assertEquals("voidMethod", invocation.getMethodName());
        Assert.assertEquals("void", invocation.printMethodReturnType());
        Assert.assertTrue(invocation.isVoid());
        Assert.assertTrue(invocation.returnsPrimitive());
        Assert.assertFalse(invocation.isVerified());
        Assert.assertFalse(invocation.isVerifiedInOrder());
    }

    @Test
    public void testMarkVerified() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("voidMethod");
        Invocation invocation = new Invocation(mock, method, new Object[0], 1, new DummyRealMethod(null));

        invocation.markVerified();
        Assert.assertTrue(invocation.isVerified());
        Assert.assertFalse(invocation.isVerifiedInOrder());

        invocation.markVerifiedInOrder();
        Assert.assertTrue(invocation.isVerified());
        Assert.assertTrue(invocation.isVerifiedInOrder());
    }

    @Test
    public void testCallRealMethod() throws Throwable {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("primitiveMethod", int.class);
        RealMethod realMethod = new DummyRealMethod(42);
        Invocation invocation = new Invocation(mock, method, new Object[]{10}, 1, realMethod);

        Object result = invocation.callRealMethod();
        Assert.assertEquals(42, result);
    }

    @Test
    public void testExpandVarArgsWithArray() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("varArgMethod", String.class, String[].class);
        Object[] rawArgs = new Object[]{"prefix", new String[]{"a", "b"}};

        Invocation invocation = new Invocation(mock, method, rawArgs, 1, new DummyRealMethod(null));

        Assert.assertEquals(3, invocation.getArgumentsCount());
        Assert.assertArrayEquals(new Object[]{"prefix", "a", "b"}, invocation.getArguments());
        Assert.assertSame(rawArgs, invocation.getRawArguments());
    }

    @Test
    public void testExpandVarArgsWithPrimitiveArray() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("varArgMethod", String.class, String[].class);
        Object[] rawArgs = new Object[]{"prefix", new int[]{1, 2}};

        Invocation invocation = new Invocation(mock, method, rawArgs, 1, new DummyRealMethod(null));

        Assert.assertEquals(3, invocation.getArgumentsCount());
        Assert.assertEquals("prefix", invocation.getArguments()[0]);
        Assert.assertEquals(1, invocation.getArguments()[1]);
        Assert.assertEquals(2, invocation.getArguments()[2]);
    }

    @Test
    public void testExpandVarArgsWithNullVarArgArray() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("varArgMethod", String.class, String[].class);
        Object[] rawArgs = new Object[]{"prefix", null};

        Invocation invocation = new Invocation(mock, method, rawArgs, 1, new DummyRealMethod(null));

        Assert.assertEquals(2, invocation.getArgumentsCount());
        Assert.assertArrayEquals(new Object[]{"prefix", null}, invocation.getArguments());
    }

    @Test
    public void testExpandVarArgsWithNonArrayLastArg() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("varArgMethod", String.class, String[].class);
        Object[] rawArgs = new Object[]{"prefix", "nonArray"};

        Invocation invocation = new Invocation(mock, method, rawArgs, 1, new DummyRealMethod(null));

        Assert.assertEquals(2, invocation.getArgumentsCount());
        Assert.assertArrayEquals(rawArgs, invocation.getArguments());
    }

    @Test
    public void testEqualsAndHashCode() {
        DummyClass mock1 = new DummyClass();
        DummyClass mock2 = new DummyClass();
        MockitoMethod method1 = createMockitoMethod("voidMethod");
        MockitoMethod method2 = createMockitoMethod("primitiveMethod", int.class);

        Invocation inv1 = new Invocation(mock1, method1, new Object[0], 1, new DummyRealMethod(null));
        Invocation inv1Copy = new Invocation(mock1, method1, new Object[0], 2, new DummyRealMethod(null));
        Invocation invDiffMock = new Invocation(mock2, method1, new Object[0], 1, new DummyRealMethod(null));
        Invocation invDiffMethod = new Invocation(mock1, method2, new Object[]{5}, 1, new DummyRealMethod(null));
        Invocation invDiffArgs = new Invocation(mock1, method1, new Object[]{"a"}, 1, new DummyRealMethod(null));

        Assert.assertEquals(inv1, inv1);
        Assert.assertEquals(inv1, inv1Copy);
        Assert.assertNotEquals(inv1, null);
        Assert.assertNotEquals(inv1, "someString");
        Assert.assertNotEquals(inv1, invDiffMock);
        Assert.assertNotEquals(inv1, invDiffMethod);
        Assert.assertNotEquals(inv1, invDiffArgs);
    }

    @Test(expected = RuntimeException.class)
    public void testHashCodeThrowsException() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("voidMethod");
        Invocation invocation = new Invocation(mock, method, new Object[0], 1, new DummyRealMethod(null));
        invocation.hashCode();
    }

    @Test
    public void testIsValidException() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("exceptionMethod");
        Invocation invocation = new Invocation(mock, method, new Object[0], 1, new DummyRealMethod(null));

        Assert.assertTrue(invocation.isValidException(new IOException()));
        Assert.assertTrue(invocation.isValidException(new IllegalArgumentException()));
        Assert.assertFalse(invocation.isValidException(new Exception()));
        Assert.assertFalse(invocation.isValidException(new RuntimeException()));
    }

    @Test
    public void testIsValidReturnType() {
        DummyClass mock = new DummyClass();
        MockitoMethod primMethod = createMockitoMethod("primitiveMethod", int.class);
        Invocation primInvocation = new Invocation(mock, primMethod, new Object[]{1}, 1, new DummyRealMethod(null));

        Assert.assertTrue(primInvocation.isValidReturnType(Integer.class));
        Assert.assertTrue(primInvocation.isValidReturnType(int.class));
        Assert.assertFalse(primInvocation.isValidReturnType(Long.class));

        MockitoMethod varArgMethod = createMockitoMethod("varArgMethod", String.class, String[].class);
        Invocation objInvocation = new Invocation(mock, varArgMethod, new Object[]{"a", new String[0]}, 1, new DummyRealMethod(null));

        Assert.assertTrue(objInvocation.isValidReturnType(String.class));
        Assert.assertFalse(objInvocation.isValidReturnType(Integer.class));
        Assert.assertFalse(objInvocation.returnsPrimitive());
        Assert.assertFalse(objInvocation.isVoid());
    }

    @Test
    public void testIsToString() {
        DummyClass mock = new DummyClass();
        MockitoMethod toStringMethod = createMockitoMethod("toString");
        MockitoMethod voidMethod = createMockitoMethod("voidMethod");

        Invocation toStringInv = new Invocation(mock, toStringMethod, new Object[0], 1, new DummyRealMethod(null));
        Invocation voidInv = new Invocation(mock, voidMethod, new Object[0], 1, new DummyRealMethod(null));

        Assert.assertTrue(Invocation.isToString(toStringInv));
        Assert.assertFalse(Invocation.isToString(voidInv));
    }

    @Test
    public void testToStringFormatting() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("primitiveMethod", int.class);
        Invocation invocation = new Invocation(mock, method, new Object[]{123}, 1, new DummyRealMethod(null));

        String str = invocation.toString();
        Assert.assertTrue(str.contains("primitiveMethod(123)"));

        PrintSettings multilineSettings = new PrintSettings();
        multilineSettings.setMultiline(true);
        String multilineStr = invocation.toString(multilineSettings);
        Assert.assertTrue(multilineStr.contains("\n") || multilineStr.contains("\r"));

        Invocation longInvocation = new Invocation(mock, method, new Object[]{"VeryLongStringArgumentThatForcesLineLengthToExceedMaximumLimitThresholdValue"}, 1, new DummyRealMethod(null));
        String longStr = longInvocation.toString();
        Assert.assertNotNull(longStr);
    }

    @Test
    public void testArgumentsToMatchersWithArrayAndNulls() {
        DummyClass mock = new DummyClass();
        MockitoMethod method = createMockitoMethod("varArgMethod", String.class, String[].class);
        Invocation invocation = new Invocation(mock, method, new Object[]{null, new String[]{"a"}}, 1, new DummyRealMethod(null));

        List<Matcher> matchers = invocation.argumentsToMatchers();
        Assert.assertEquals(2, matchers.size());

        PrintSettings settings = new PrintSettings();
        String result = invocation.toString(Collections.emptyList(), settings);
        Assert.assertNotNull(result);
    }
}
