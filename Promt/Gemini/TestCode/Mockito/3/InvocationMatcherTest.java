package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    private Object mock;
    private Method simpleMethod;
    private Method overloadedMethod;
    private Method varargMethod;
    private Method differentMethodName;

    static class SampleClass {
        public void simpleMethod(String arg) {}
        public void overloadedMethod(String arg) {}
        public void overloadedMethod(Integer arg) {}
        public void varargMethod(String first, String... rest) {}
        public void differentName(String arg) {}
    }

    static class DummyCapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        private final List<Object> captured = new ArrayList<Object>();

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("dummy matcher");
        }

        @Override
        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() {
            return captured;
        }
    }

    static class NonCapturingMatcher extends BaseMatcher<Object> {
        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("non capturing matcher");
        }
    }

    @Before
    public void setUp() throws Exception {
        mock = new Object();
        simpleMethod = SampleClass.class.getMethod("simpleMethod", String.class);
        overloadedMethod = SampleClass.class.getMethod("overloadedMethod", String.class);
        varargMethod = SampleClass.class.getMethod("varargMethod", String.class, String[].class);
        differentMethodName = SampleClass.class.getMethod("differentName", String.class);
    }

    private Invocation createInvocation(Object mockObj, Method method, Object[] args, Object[] rawArgs, boolean verified) {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMock()).thenReturn(mockObj);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(args != null ? args : new Object[0]);
        when(invocation.getRawArguments()).thenReturn(rawArgs != null ? rawArgs : args);
        when(invocation.isVerified()).thenReturn(verified);
        Location location = mock(Location.class);
        when(invocation.getLocation()).thenReturn(location);

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                final int idx = i;
                final Object arg = args[i];
                when(invocation.getArgumentAt(eq(idx), any(Class.class))).thenAnswer(inv -> arg);
            }
        }

        return invocation;
    }

    @Test
    public void testConstructorWithEmptyMatchersList() {
        Invocation invocation = createInvocation(mock, simpleMethod, new Object[]{"test"}, new Object[]{"test"}, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertSame(invocation, matcher.getInvocation());
        assertSame(simpleMethod, matcher.getMethod());
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    @Test
    public void testSingleArgConstructor() {
        Invocation invocation = createInvocation(mock, simpleMethod, new Object[]{"test"}, new Object[]{"test"}, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(invocation, matcher.getInvocation());
        assertSame(simpleMethod, matcher.getMethod());
        assertEquals(1, matcher.getMatchers().size());
        assertNotNull(matcher.getLocation());
    }

    @Test
    public void testConstructorWithProvidedMatchers() {
        Invocation invocation = createInvocation(mock, simpleMethod, new Object[]{"test"}, new Object[]{"test"}, false);
        Matcher customMatcher = new NonCapturingMatcher();
        List<Matcher> matchers = Collections.singletonList(customMatcher);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);
        assertEquals(1, invocationMatcher.getMatchers().size());
        assertSame(customMatcher, invocationMatcher.getMatchers().get(0));
    }

    @Test
    public void testToString() {
        Invocation invocation = createInvocation(mock, simpleMethod, new Object[]{"hello"}, new Object[]{"hello"}, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String result = matcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    @Test
    public void testMatchesSuccessful() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);
        Invocation inv2 = createInvocation(mock, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertTrue(matcher.matches(inv2));
    }

    @Test
    public void testMatchesDifferentMock() {
        Object mock2 = new Object();
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);
        Invocation inv2 = createInvocation(mock2, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test
    public void testMatchesDifferentMethod() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);
        Invocation inv2 = createInvocation(mock, differentMethodName, new Object[]{"a"}, new Object[]{"a"}, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test
    public void testMatchesDifferentArguments() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, new Object[]{"a"}, false);
        Invocation inv2 = createInvocation(mock, simpleMethod, new Object[]{"b"}, new Object[]{"b"}, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test
    public void testHasSameMethod() throws Exception {
        Method method1 = SampleClass.class.getMethod("simpleMethod", String.class);
        Method method2 = SampleClass.class.getMethod("simpleMethod", String.class);
        Method method3 = SampleClass.class.getMethod("differentName", String.class);
        Method method4 = SampleClass.class.getMethod("overloadedMethod", Integer.class);
        Method method5 = SampleClass.class.getMethod("varargMethod", String.class, String[].class);

        Invocation inv1 = createInvocation(mock, method1, new Object[]{"a"}, null, false);
        Invocation inv2 = createInvocation(mock, method2, new Object[]{"b"}, null, false);
        Invocation inv3 = createInvocation(mock, method3, new Object[]{"a"}, null, false);
        Invocation inv4 = createInvocation(mock, method4, new Object[]{1}, null, false);
        Invocation inv5 = createInvocation(mock, method5, new Object[]{"a", new String[0]}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertTrue(matcher.hasSameMethod(inv2));
        assertFalse(matcher.hasSameMethod(inv3));
        assertFalse(matcher.hasSameMethod(inv4));
        assertFalse(matcher.hasSameMethod(inv5));
    }

    @Test
    public void testHasSimilarMethodWhenSimilar() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation candidate = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodDifferentName() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation candidate = createInvocation(mock, differentMethodName, new Object[]{"a"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodVerified() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation candidate = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, true);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodDifferentMock() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation candidate = createInvocation(new Object(), simpleMethod, new Object[]{"a"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedWithDifferentArgs() throws Exception {
        Method overloadedString = SampleClass.class.getMethod("overloadedMethod", String.class);
        Method overloadedInt = SampleClass.class.getMethod("overloadedMethod", Integer.class);

        Invocation inv1 = createInvocation(mock, overloadedString, new Object[]{"string"}, null, false);
        Invocation candidate = createInvocation(mock, overloadedInt, new Object[]{123}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        // Overloaded and different args -> should return true (is similar)
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodSafelyArgumentsMatchThrows() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mock);
        when(candidate.getMethod()).thenReturn(differentMethodName);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getArguments()).thenThrow(new RuntimeException("boom"));

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testCaptureArgumentsFromNonVarargs() {
        DummyCapturingMatcher capMatcher = new DummyCapturingMatcher();
        NonCapturingMatcher nonCapMatcher = new NonCapturingMatcher();

        Invocation invocation = createInvocation(mock, simpleMethod, new Object[]{"arg1"}, new Object[]{"arg1"}, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.<Matcher>asList(capMatcher, nonCapMatcher));

        Invocation targetInvocation = createInvocation(mock, simpleMethod, new Object[]{"capturedValue"}, new Object[]{"capturedValue"}, false);

        matcher.captureArgumentsFrom(targetInvocation);

        assertEquals(1, capMatcher.getCaptured().size());
        assertEquals("capturedValue", capMatcher.getCaptured().get(0));
    }

    @Test
    public void testCaptureArgumentsFromVarargs() {
        DummyCapturingMatcher capMatcher1 = new DummyCapturingMatcher();
        DummyCapturingMatcher capMatcher2 = new DummyCapturingMatcher();
        NonCapturingMatcher nonCapMatcher = new NonCapturingMatcher();

        Object[] rawArgs = new Object[]{"firstArg", "var1", "var2"};
        Object[] args = new Object[]{"firstArg", new String[]{"var1", "var2"}};

        Invocation invocation = createInvocation(mock, varargMethod, args, rawArgs, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.<Matcher>asList(capMatcher1, capMatcher2, nonCapMatcher));

        Invocation targetInvocation = createInvocation(mock, varargMethod, args, rawArgs, false);

        matcher.captureArgumentsFrom(targetInvocation);

        assertEquals(1, capMatcher1.getCaptured().size());
        assertEquals("firstArg", capMatcher1.getCaptured().get(0));

        assertEquals(1, capMatcher2.getCaptured().size());
        assertEquals("var1", capMatcher2.getCaptured().get(0));
    }

    @Test
    public void testCreateFrom() {
        Invocation inv1 = createInvocation(mock, simpleMethod, new Object[]{"a"}, null, false);
        Invocation inv2 = createInvocation(mock, simpleMethod, new Object[]{"b"}, null, false);

        List<Invocation> invocations = Arrays.asList(inv1, inv2);
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);

        assertNotNull(matchers);
        assertEquals(2, matchers.size());
        assertSame(inv1, matchers.get(0).getInvocation());
        assertSame(inv2, matchers.get(1).getInvocation());
    }

    @Test
    public void testCreateFromEmptyList() {
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(matchers);
        assertTrue(matchers.isEmpty());
    }
}
