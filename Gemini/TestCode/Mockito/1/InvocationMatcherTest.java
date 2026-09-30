package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Location;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    interface TestInterface {
        void simpleMethod(String a);
        void overloadedMethod(String a);
        void overloadedMethod(Integer a);
        void overloadedMethod(Object a);
        void differentParamCount(String a, String b);
        void anotherMethod(String a);
        void varArgMethod(String... args);
    }

    private Method simpleMethod;
    private Method overloadedMethodString;
    private Method overloadedMethodInteger;
    private Method overloadedMethodObject;
    private Method differentParamCountMethod;
    private Method anotherMethod;
    private Method varArgMethod;

    @Before
    public void setUp() throws Exception {
        simpleMethod = TestInterface.class.getMethod("simpleMethod", String.class);
        overloadedMethodString = TestInterface.class.getMethod("overloadedMethod", String.class);
        overloadedMethodInteger = TestInterface.class.getMethod("overloadedMethod", Integer.class);
        overloadedMethodObject = TestInterface.class.getMethod("overloadedMethod", Object.class);
        differentParamCountMethod = TestInterface.class.getMethod("differentParamCount", String.class, String.class);
        anotherMethod = TestInterface.class.getMethod("anotherMethod", String.class);
        varArgMethod = TestInterface.class.getMethod("varArgMethod", String[].class);
    }

    @Test
    public void shouldConstructWithInvocationAndEmptyMatchers() {
        Object mock = new Object();
        Invocation invocation = mock(Invocation.class);
        when(invocation.getArguments()).thenReturn(new Object[]{"arg1"});

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(invocation, matcher.getInvocation());
        assertEquals(1, matcher.getMatchers().size());
    }

    @Test
    public void shouldConstructWithExplicitMatchers() {
        Invocation invocation = mock(Invocation.class);
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("test"));

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertSame(invocation, matcher.getInvocation());
        assertEquals(matchers, matcher.getMatchers());
    }

    @Test
    public void shouldGetMethodAndLocation() {
        Invocation invocation = mock(Invocation.class);
        Location location = mock(Location.class);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertEquals(simpleMethod, matcher.getMethod());
        assertEquals(location, matcher.getLocation());
    }

    @Test
    public void shouldProduceNonEmptyToString() {
        Object mock = new Object();
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMock()).thenReturn(mock);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getArguments()).thenReturn(new Object[]{"hello"});

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String stringRepr = matcher.toString();

        assertNotNull(stringRepr);
        assertTrue(stringRepr.contains("simpleMethod"));
    }

    @Test
    public void shouldMatchWhenMockMethodAndArgumentsMatch() {
        Object mock = new Object();
        Invocation invocation1 = mock(Invocation.class);
        when(invocation1.getMock()).thenReturn(mock);
        when(invocation1.getMethod()).thenReturn(simpleMethod);
        when(invocation1.getArguments()).thenReturn(new Object[]{"test"});

        Invocation invocation2 = mock(Invocation.class);
        when(invocation2.getMock()).thenReturn(mock);
        when(invocation2.getMethod()).thenReturn(simpleMethod);
        when(invocation2.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher matcher = new InvocationMatcher(invocation1);

        assertTrue(matcher.matches(invocation2));
    }

    @Test
    public void shouldNotMatchWhenMockIsDifferent() {
        Object mock1 = new Object();
        Object mock2 = new Object();

        Invocation invocation1 = mock(Invocation.class);
        when(invocation1.getMock()).thenReturn(mock1);
        when(invocation1.getMethod()).thenReturn(simpleMethod);
        when(invocation1.getArguments()).thenReturn(new Object[]{"test"});

        Invocation invocation2 = mock(Invocation.class);
        when(invocation2.getMock()).thenReturn(mock2);
        when(invocation2.getMethod()).thenReturn(simpleMethod);
        when(invocation2.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher matcher = new InvocationMatcher(invocation1);

        assertFalse(matcher.matches(invocation2));
    }

    @Test
    public void shouldNotMatchWhenMethodIsDifferent() {
        Object mock = new Object();

        Invocation invocation1 = mock(Invocation.class);
        when(invocation1.getMock()).thenReturn(mock);
        when(invocation1.getMethod()).thenReturn(simpleMethod);
        when(invocation1.getArguments()).thenReturn(new Object[]{"test"});

        Invocation invocation2 = mock(Invocation.class);
        when(invocation2.getMock()).thenReturn(mock);
        when(invocation2.getMethod()).thenReturn(anotherMethod);
        when(invocation2.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher matcher = new InvocationMatcher(invocation1);

        assertFalse(matcher.matches(invocation2));
    }

    @Test
    public void shouldNotMatchWhenArgumentsDoNotMatch() {
        Object mock = new Object();

        Invocation invocation1 = mock(Invocation.class);
        when(invocation1.getMock()).thenReturn(mock);
        when(invocation1.getMethod()).thenReturn(simpleMethod);
        when(invocation1.getArguments()).thenReturn(new Object[]{"test1"});

        Invocation invocation2 = mock(Invocation.class);
        when(invocation2.getMock()).thenReturn(mock);
        when(invocation2.getMethod()).thenReturn(simpleMethod);
        when(invocation2.getArguments()).thenReturn(new Object[]{"test2"});

        InvocationMatcher matcher = new InvocationMatcher(invocation1);

        assertFalse(matcher.matches(invocation2));
    }

    @Test
    public void shouldCheckHasSameMethodCorrectly() {
        Invocation invocation1 = mock(Invocation.class);
        when(invocation1.getMethod()).thenReturn(simpleMethod);

        Invocation invocationSame = mock(Invocation.class);
        when(invocationSame.getMethod()).thenReturn(simpleMethod);

        Invocation invocationDifferentName = mock(Invocation.class);
        when(invocationDifferentName.getMethod()).thenReturn(anotherMethod);

        Invocation invocationDifferentParamCount = mock(Invocation.class);
        when(invocationDifferentParamCount.getMethod()).thenReturn(differentParamCountMethod);

        Invocation invocationDifferentParamType = mock(Invocation.class);
        when(invocationDifferentParamType.getMethod()).thenReturn(overloadedMethodInteger);

        InvocationMatcher matcher = new InvocationMatcher(invocation1, Collections.<Matcher>emptyList());

        assertTrue(matcher.hasSameMethod(invocationSame));
        assertFalse(matcher.hasSameMethod(invocationDifferentName));
        assertFalse(matcher.hasSameMethod(invocationDifferentParamCount));
        assertFalse(matcher.hasSameMethod(invocationDifferentParamType));
    }

    @Test
    public void shouldCheckHasSimilarMethod() {
        Object mock = new Object();
        Object differentMock = new Object();

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMock()).thenReturn(mock);
        when(wanted.getMethod()).thenReturn(overloadedMethodString);
        when(wanted.getArguments()).thenReturn(new Object[]{"arg"});

        Invocation candidateSimilar = mock(Invocation.class);
        when(candidateSimilar.getMock()).thenReturn(mock);
        when(candidateSimilar.getMethod()).thenReturn(overloadedMethodInteger);
        when(candidateSimilar.isVerified()).thenReturn(false);
        when(candidateSimilar.getArguments()).thenReturn(new Object[]{123});

        Invocation candidateVerified = mock(Invocation.class);
        when(candidateVerified.getMock()).thenReturn(mock);
        when(candidateVerified.getMethod()).thenReturn(overloadedMethodInteger);
        when(candidateVerified.isVerified()).thenReturn(true);
        when(candidateVerified.getArguments()).thenReturn(new Object[]{123});

        Invocation candidateDifferentMock = mock(Invocation.class);
        when(candidateDifferentMock.getMock()).thenReturn(differentMock);
        when(candidateDifferentMock.getMethod()).thenReturn(overloadedMethodInteger);
        when(candidateDifferentMock.isVerified()).thenReturn(false);
        when(candidateDifferentMock.getArguments()).thenReturn(new Object[]{123});

        Invocation candidateDifferentName = mock(Invocation.class);
        when(candidateDifferentName.getMock()).thenReturn(mock);
        when(candidateDifferentName.getMethod()).thenReturn(anotherMethod);
        when(candidateDifferentName.isVerified()).thenReturn(false);
        when(candidateDifferentName.getArguments()).thenReturn(new Object[]{"arg"});

        InvocationMatcher matcher = new InvocationMatcher(wanted);

        assertTrue(matcher.hasSimilarMethod(candidateSimilar));
        assertFalse(matcher.hasSimilarMethod(candidateVerified));
        assertFalse(matcher.hasSimilarMethod(candidateDifferentMock));
        assertFalse(matcher.hasSimilarMethod(candidateDifferentName));
    }

    @Test
    public void shouldReturnFalseForSimilarMethodWhenOverloadedButSameArgs() {
        Object mock = new Object();

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMock()).thenReturn(mock);
        when(wanted.getMethod()).thenReturn(overloadedMethodString);
        when(wanted.getArguments()).thenReturn(new Object[]{"arg"});

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mock);
        when(candidate.getMethod()).thenReturn(overloadedMethodObject);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getArguments()).thenReturn(new Object[]{"arg"});

        InvocationMatcher matcher = new InvocationMatcher(wanted);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldHandleExceptionInSafelyArgumentsMatch() {
        Object mock = new Object();

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMock()).thenReturn(mock);
        when(wanted.getMethod()).thenReturn(overloadedMethodString);

        Matcher throwingMatcher = new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                throw new RuntimeException("Matcher failure");
            }
            public void describeTo(Description description) {
            }
        };

        InvocationMatcher matcher = new InvocationMatcher(wanted, Arrays.asList(throwingMatcher));

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mock);
        when(candidate.getMethod()).thenReturn(overloadedMethodObject);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getArguments()).thenReturn(new Object[]{"arg"});

        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    interface CapturingMatcher extends Matcher<Object>, CapturesArguments {
    }

    @Test
    public void shouldCaptureArgumentsFromInvocation() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getArgumentAt(0, Object.class)).thenReturn("capturedValue");

        CapturingMatcher capturingMatcher = mock(CapturingMatcher.class);

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMethod()).thenReturn(simpleMethod);
        when(wanted.getArguments()).thenReturn(new Object[]{"initial"});

        InvocationMatcher matcher = new InvocationMatcher(wanted, Arrays.<Matcher>asList(capturingMatcher));
        matcher.captureArgumentsFrom(invocation);

        verify(capturingMatcher).captureFrom("capturedValue");
    }

    @Test
    public void shouldIgnoreNonCapturingMatchersDuringCapture() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getArgumentAt(0, Object.class)).thenReturn("capturedValue");

        Matcher nonCapturingMatcher = new Equals("test");

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMethod()).thenReturn(simpleMethod);
        when(wanted.getArguments()).thenReturn(new Object[]{"initial"});

        InvocationMatcher matcher = new InvocationMatcher(wanted, Arrays.asList(nonCapturingMatcher));
        matcher.captureArgumentsFrom(invocation);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenCapturingFromVarArgsMethod() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(varArgMethod);
        when(invocation.getRawArguments()).thenReturn(new Object[]{new String[]{"a", "b"}});

        Invocation wanted = mock(Invocation.class);
        when(wanted.getMethod()).thenReturn(varArgMethod);
        when(wanted.getArguments()).thenReturn(new Object[]{new String[]{"a", "b"}});

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        matcher.captureArgumentsFrom(invocation);
    }

    @Test
    public void shouldCreateFromListOfInvocations() {
        Invocation inv1 = mock(Invocation.class);
        when(inv1.getArguments()).thenReturn(new Object[]{"1"});
        Invocation inv2 = mock(Invocation.class);
        when(inv2.getArguments()).thenReturn(new Object[]{"2"});

        List<Invocation> invocations = Arrays.asList(inv1, inv2);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }

    @Test
    public void shouldCreateFromEmptyList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
