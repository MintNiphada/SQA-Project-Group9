package org.mockito.internal.invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

public class InvocationMatcherTest {

    private Invocation invocation;
    private InvocationMatcher matcher;

    @Before
    public void setUp() throws Exception {
        invocation = mock(Invocation.class);
        when(invocation.getMock()).thenReturn("mock");
        Method method = String.class.getMethod("indexOf", String.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getRawArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getLocation()).thenReturn(mock(Location.class));
        matcher = new InvocationMatcher(invocation);
    }

    @Test
    public void testConstructorWithMatchers() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("test"));
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertEquals(matchers, im.getMatchers());
    }

    @Test
    public void testConstructorWithEmptyMatchers() {
        InvocationMatcher im = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertNotNull(im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    @Test
    public void testConstructorWithNullMatchers() {
        InvocationMatcher im = new InvocationMatcher(invocation, null);
        assertNotNull(im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    @Test
    public void testGetMethod() throws Exception {
        Method expected = String.class.getMethod("indexOf", String.class);
        assertEquals(expected, matcher.getMethod());
    }

    @Test
    public void testGetInvocation() {
        assertEquals(invocation, matcher.getInvocation());
    }

    @Test
    public void testGetMatchers() {
        List<Matcher> matchers = matcher.getMatchers();
        assertNotNull(matchers);
        assertEquals(1, matchers.size());
    }

    @Test
    public void testToString() {
        String str = matcher.toString();
        assertNotNull(str);
        assertTrue(str.contains("indexOf"));
    }

    @Test
    public void testMatchesTrue() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn("mock");
        Method method = invocation.getMethod();
        when(actual.getMethod()).thenReturn(method);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"test"});
        assertTrue(matcher.matches(actual));
    }

    @Test
    public void testMatchesDifferentMock() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn("different");
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testMatchesDifferentMethod() throws Exception {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn("mock");
        Method otherMethod = String.class.getMethod("length");
        when(actual.getMethod()).thenReturn(otherMethod);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testMatchesDifferentArguments() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn("mock");
        when(actual.getMethod()).thenReturn(invocation.getMethod());
        when(actual.getArguments()).thenReturn(new Object[]{"other"});
        when(actual.getRawArguments()).thenReturn(new Object[]{"other"});
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void testHasSimilarMethodSameMethodUnverified() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(invocation.getMethod());
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("mock");
        when(candidate.getArguments()).thenReturn(new Object[]{"test"});
        when(candidate.getRawArguments()).thenReturn(new Object[]{"test"});
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodDifferentName() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method otherMethod = String.class.getMethod("length");
        when(candidate.getMethod()).thenReturn(otherMethod);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("mock");
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodVerified() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(invocation.getMethod());
        when(candidate.isVerified()).thenReturn(true);
        when(candidate.getMock()).thenReturn("mock");
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodDifferentMock() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(invocation.getMethod());
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("different");
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedButSameArgs() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method method1 = String.class.getMethod("indexOf", String.class);
        Method method2 = String.class.getMethod("indexOf", String.class, int.class);
        when(candidate.getMethod()).thenReturn(method2);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("mock");
        when(candidate.getArguments()).thenReturn(new Object[]{"test"});
        when(candidate.getRawArguments()).thenReturn(new Object[]{"test"});
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedDifferentArgs() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method method1 = String.class.getMethod("indexOf", String.class);
        Method method2 = String.class.getMethod("indexOf", String.class, int.class);
        when(candidate.getMethod()).thenReturn(method2);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("mock");
        when(candidate.getArguments()).thenReturn(new Object[]{"test", 0});
        when(candidate.getRawArguments()).thenReturn(new Object[]{"test", 0});
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSameMethodTrue() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(invocation.getMethod());
        assertTrue(matcher.hasSameMethod(candidate));
    }

    @Test
    public void testHasSameMethodDifferentName() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method otherMethod = String.class.getMethod("length");
        when(candidate.getMethod()).thenReturn(otherMethod);
        assertFalse(matcher.hasSameMethod(candidate));
    }

    @Test
    public void testHasSameMethodDifferentParameterTypes() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method otherMethod = String.class.getMethod("indexOf", String.class, int.class);
        when(candidate.getMethod()).thenReturn(otherMethod);
        assertFalse(matcher.hasSameMethod(candidate));
    }

    @Test
    public void testHasSameMethodNullName() {
        Invocation candidate = mock(Invocation.class);
        Method methodWithNullName = mock(Method.class);
        when(methodWithNullName.getName()).thenReturn(null);
        when(candidate.getMethod()).thenReturn(methodWithNullName);
        assertFalse(matcher.hasSameMethod(candidate));
    }

    @Test
    public void testGetLocation() {
        Location loc = matcher.getLocation();
        assertNotNull(loc);
    }

    @Test
    public void testCaptureArgumentsFromNonVarargs() {
        Invocation inv = mock(Invocation.class);
        Method method = invocation.getMethod();
        when(inv.getMethod()).thenReturn(method);
        when(inv.getRawArguments()).thenReturn(new Object[]{"test"});
        when(inv.getArgumentAt(0, Object.class)).thenReturn("test");
        List<Matcher> matchers = new ArrayList<Matcher>();
        CapturesArguments capturingMatcher = mock(CapturesArguments.class);
        matchers.add(capturingMatcher);
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        im.captureArgumentsFrom(inv);
        verify(capturingMatcher).captureFrom("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCaptureArgumentsFromVarargs() {
        Invocation inv = mock(Invocation.class);
        Method method = mock(Method.class);
        when(method.isVarArgs()).thenReturn(true);
        when(inv.getMethod()).thenReturn(method);
        when(inv.getRawArguments()).thenReturn(new Object[]{new String[]{"a", "b"}});
        matcher.captureArgumentsFrom(inv);
    }

    @Test
    public void testCreateFrom() {
        List<Invocation> invocations = new ArrayList<Invocation>();
        invocations.add(invocation);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertEquals(1, result.size());
        assertEquals(invocation, result.get(0).getInvocation());
    }

    @Test
    public void testCreateFromEmpty() {
        List<Invocation> invocations = new ArrayList<Invocation>();
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSafelyArgumentsMatchException() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getArguments()).thenThrow(new RuntimeException("test exception"));
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void testHasSimilarMethodOverloadedButSameArgsWithException() throws Exception {
        Invocation candidate = mock(Invocation.class);
        Method method1 = String.class.getMethod("indexOf", String.class);
        Method method2 = String.class.getMethod("indexOf", String.class, int.class);
        when(candidate.getMethod()).thenReturn(method2);
        when(candidate.isVerified()).thenReturn(false);
        when(candidate.getMock()).thenReturn("mock");
        when(candidate.getArguments()).thenThrow(new RuntimeException("test exception"));
        assertTrue(matcher.hasSimilarMethod(candidate));
    }
}
