package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.reporting.PrintSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InvocationMatcherTest {

    private Invocation invocation;
    private Method simpleMethod;
    private Method overloadedMethod;
    private Method differentMethod;
    private Object mockObject;

    public interface TestSample {
        void simpleMethod(String arg);
        void simpleMethod(Object arg);
        void differentMethod(String arg);
    }

    private interface CapturingMatcher extends Matcher<Object>, CapturesArguments {
    }

    @Before
    public void setUp() throws Exception {
        simpleMethod = TestSample.class.getMethod("simpleMethod", String.class);
        overloadedMethod = TestSample.class.getMethod("simpleMethod", Object.class);
        differentMethod = TestSample.class.getMethod("differentMethod", String.class);
        mockObject = new Object();

        invocation = mock(Invocation.class);
        when(invocation.getMock()).thenReturn(mockObject);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getRawArguments()).thenReturn(new Object[]{"test"});
    }

    @Test
    public void shouldInitializeWithExplicitMatchers() {
        Matcher matcher = new Equals("test");
        List<Matcher> matchers = Collections.singletonList(matcher);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        assertSame(invocation, invocationMatcher.getInvocation());
        assertSame(simpleMethod, invocationMatcher.getMethod());
        assertEquals(matchers, invocationMatcher.getMatchers());
    }

    @Test
    public void shouldInitializeWithEmptyMatchersFallbackToInvocationMatchers() {
        Matcher matcher = new Equals("test");
        List<Matcher> fallbackMatchers = Collections.singletonList(matcher);
        when(invocation.argumentsToMatchers()).thenReturn(fallbackMatchers);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertEquals(fallbackMatchers, invocationMatcher.getMatchers());
    }

    @Test
    public void shouldInitializeWithSingleArgumentConstructor() {
        Matcher matcher = new Equals("test");
        List<Matcher> fallbackMatchers = Collections.singletonList(matcher);
        when(invocation.argumentsToMatchers()).thenReturn(fallbackMatchers);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation);

        assertSame(invocation, invocationMatcher.getInvocation());
        assertEquals(fallbackMatchers, invocationMatcher.getMatchers());
    }

    @Test
    public void shouldDelegateLocationToInvocation() {
        Location location = mock(Location.class);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertSame(location, invocationMatcher.getLocation());
    }

    @Test
    public void shouldDelegateToStringWithoutSettings() {
        Matcher matcher = new Equals("test");
        List<Matcher> matchers = Collections.singletonList(matcher);
        when(invocation.toString(org.mockito.Matchers.eq(matchers), org.mockito.Matchers.any(PrintSettings.class))).thenReturn("invString");

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        assertEquals("invString", invocationMatcher.toString());
    }

    @Test
    public void shouldDelegateToStringWithSettings() {
        Matcher matcher = new Equals("test");
        List<Matcher> matchers = Collections.singletonList(matcher);
        PrintSettings settings = new PrintSettings();
        when(invocation.toString(matchers, settings)).thenReturn("customString");

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        assertEquals("customString", invocationMatcher.toString(settings));
    }

    @Test
    public void shouldMatchWhenMockMethodAndArgumentsMatch() {
        Matcher matcher = new Equals("test");
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.singletonList(matcher));

        assertTrue(invocationMatcher.matches(actual));
    }

    @Test
    public void shouldNotMatchWhenMockIsDifferent() {
        Matcher matcher = new Equals("test");
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(new Object());
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.singletonList(matcher));

        assertFalse(invocationMatcher.matches(actual));
    }

    @Test
    public void shouldNotMatchWhenMethodIsDifferent() {
        Matcher matcher = new Equals("test");
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(differentMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.singletonList(matcher));

        assertFalse(invocationMatcher.matches(actual));
    }

    @Test
    public void shouldNotMatchWhenArgumentsDoNotMatch() {
        Matcher matcher = new Equals("test");
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"different"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"different"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.singletonList(matcher));

        assertFalse(invocationMatcher.matches(actual));
    }

    @Test
    public void shouldCheckHasSameMethod() {
        Invocation candidate1 = mock(Invocation.class);
        when(candidate1.getMethod()).thenReturn(simpleMethod);

        Invocation candidate2 = mock(Invocation.class);
        when(candidate2.getMethod()).thenReturn(differentMethod);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertTrue(invocationMatcher.hasSameMethod(candidate1));
        assertFalse(invocationMatcher.hasSameMethod(candidate2));
    }

    @Test
    public void shouldReturnFalseForHasSimilarMethodWhenMethodNameDiffers() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(differentMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnFalseForHasSimilarMethodWhenCandidateIsVerified() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(true);
        when(candidate.getMock()).thenReturn(mockObject);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnFalseForHasSimilarMethodWhenMockDiffers() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(new Object());

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnTrueForHasSimilarMethodWhenSameMethodAndUnverified() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(new Equals("test")));

        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnFalseForHasSimilarMethodWhenOverloadedMethodHasMatchingArguments() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{"test"});
        when(candidate.getRawArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(new Equals("test")));

        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnTrueForHasSimilarMethodWhenOverloadedMethodHasDifferentArguments() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{"different"});
        when(candidate.getRawArguments()).thenReturn(new Object[]{"different"});

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(new Equals("test")));

        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnTrueForHasSimilarMethodWhenArgumentsCheckThrowsException() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{"any"});

        Matcher faultyMatcher = new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                throw new RuntimeException("Matcher failure");
            }
            public void describeTo(Description description) {
            }
        };

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.singletonList(faultyMatcher));

        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldCaptureArgumentsFromInvocation() {
        CapturingMatcher capturingMatcher = mock(CapturingMatcher.class);
        Matcher regularMatcher = mock(Matcher.class);

        Invocation targetInvocation = mock(Invocation.class);
        when(targetInvocation.getArguments()).thenReturn(new Object[]{"first", "second"});

        List<Matcher> matchers = new ArrayList<Matcher>(Arrays.asList(capturingMatcher, regularMatcher));
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        invocationMatcher.captureArgumentsFrom(targetInvocation);

        verify(capturingMatcher).captureFrom("first");
        verify(regularMatcher, never()).matches(org.mockito.Matchers.any());
    }
}
