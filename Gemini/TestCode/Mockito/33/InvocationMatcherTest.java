package org.mockito.internal.invocation;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
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

    public void sampleMethod(String arg) {}
    public void sampleMethod(Integer arg) {}
    public void otherMethod(String arg) {}

    @Before
    public void setUp() throws Exception {
        simpleMethod = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        overloadedMethod = InvocationMatcherTest.class.getMethod("sampleMethod", Integer.class);
        differentMethod = InvocationMatcherTest.class.getMethod("otherMethod", String.class);
        
        mockObject = new Object();
        invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getMock()).thenReturn(mockObject);
    }

    @Test
    public void testConstructorWithEmptyMatchersUsesInvocationMatchers() {
        List<Matcher> defaultMatchers = Collections.<Matcher>singletonList(new IsEqual<String>("test"));
        when(invocation.argumentsToMatchers()).thenReturn(defaultMatchers);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(invocation, matcher.getInvocation());
        assertSame(simpleMethod, matcher.getMethod());
        assertEquals(defaultMatchers, matcher.getMatchers());
    }

    @Test
    public void testConstructorWithExplicitMatchers() {
        List<Matcher> customMatchers = Collections.<Matcher>singletonList(new IsEqual<String>("custom"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, customMatchers);

        assertEquals(customMatchers, matcher.getMatchers());
    }

    @Test
    public void testToStringWithoutArgs() {
        PrintSettings settings = new PrintSettings();
        when(invocation.toString(Mockito.anyListOf(Matcher.class), Mockito.any(PrintSettings.class))).thenReturn("invStr");

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertEquals("invStr", matcher.toString());
        assertEquals("invStr", matcher.toString(settings));
    }

    @Test
    public void testGetLocation() {
        Location location = mock(Location.class);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertSame(location, matcher.getLocation());
    }

    @Test
    public void testMatchesReturnsTrueWhenAllMatch() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"abc"});

        Matcher hamcrestMatcher = new IsEqual<String>("abc");
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(hamcrestMatcher));

        assertTrue(matcher.matches(actual));
    }

    @Test
    public void testMatchesReturnsFalseWhenMockDiffers() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(new Object());
        when(actual.getMethod()).thenReturn(simpleMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testMatchesReturnsFalseWhenMethodDiffers() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(differentMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testMatchesReturnsFalseWhenArgumentsDoNotMatch() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"different"});

        Matcher hamcrestMatcher = new IsEqual<String>("expected");
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(hamcrestMatcher));

        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testHasSameMethod() {
        Invocation candidateSame = mock(Invocation.class);
        when(candidateSame.getMethod()).thenReturn(simpleMethod);

        Invocation candidateDiff = mock(Invocation.class);
        when(candidateDiff.getMethod()).thenReturn(differentMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertTrue(matcher.hasSameMethod(candidateSame));
        assertFalse(matcher.hasSameMethod(candidateDiff));
    }

    @Test
    public void testHasSimilarMethodWhenMethodNameDiffers() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(differentMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodWhenCandidateIsVerified() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(true);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodWhenCandidateHasDifferentMock() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(new Object());

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodSameMethodReturnsTrue() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedAndSameArgsReturnsFalse() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{10});

        Matcher hamcrestMatcher = new IsEqual<Object>(10);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(hamcrestMatcher));

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedAndDifferentArgsReturnsTrue() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getArguments()).thenReturn(new Object[]{20});

        Matcher hamcrestMatcher = new IsEqual<Object>(10);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(hamcrestMatcher));

        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testCaptureArgumentsFrom() {
        interface CaptureMatcher extends Matcher<Object>, CapturesArguments {}
        CaptureMatcher captureMatcher1 = mock(CaptureMatcher.class);
        CaptureMatcher captureMatcher2 = mock(CaptureMatcher.class);
        Matcher regularMatcher = mock(Matcher.class);

        List<Matcher> matchers = Arrays.asList(captureMatcher1, regularMatcher, captureMatcher2);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        Invocation actual = mock(Invocation.class);
        when(actual.getArguments()).thenReturn(new Object[]{"arg0", "arg1"});

        matcher.captureArgumentsFrom(actual);

        verify(captureMatcher1).captureFrom("arg0");
        verify(captureMatcher2, never()).captureFrom(Mockito.any());
    }

    @Test
    public void testCreateFromList() {
        Invocation inv1 = mock(Invocation.class);
        when(inv1.getMethod()).thenReturn(simpleMethod);
        Invocation inv2 = mock(Invocation.class);
        when(inv2.getMethod()).thenReturn(differentMethod);

        List<Invocation> invocations = Arrays.asList(inv1, inv2);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }

    @Test
    public void testCreateFromEmptyList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertTrue(result.isEmpty());
    }
}
