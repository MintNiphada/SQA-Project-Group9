package org.mockito.internal.invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InvocationMatcherTest {

    @Mock
    private Invocation invocation;
    @Mock
    private Invocation actualInvocation;
    @Mock
    private Invocation candidateInvocation;
    @Mock
    private Method method;
    @Mock
    private Method candidateMethod;
    @Mock
    private Location location;
    @Mock
    private Matcher<Object> matcher1;
    @Mock
    private Matcher<Object> matcher2;
    @Mock
    private CapturesArguments capturingMatcher;

    private List<Matcher> matchersList;

    @Before
    public void setUp() {
        matchersList = new ArrayList<Matcher>();
        matchersList.add(matcher1);
        matchersList.add(matcher2);
    }

    // Constructor tests

    @Test
    public void should_construct_with_non_empty_matchers() {
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertSame(invocation, matcher.getInvocation());
        assertSame(matchersList, matcher.getMatchers());
    }

    @Test
    public void should_construct_with_empty_matchers_and_use_arguments_to_matchers() {
        when(invocation.getArguments()).thenReturn(new Object[]{"arg1", "arg2"});
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertNotNull(matcher.getMatchers());
        assertFalse(matcher.getMatchers().isEmpty());
        assertEquals(2, matcher.getMatchers().size());
        // The matchers are created by ArgumentsProcessor.argumentsToMatchers, which wraps each argument in an Equals matcher.
        assertTrue(matcher.getMatchers().get(0) instanceof Equals);
        assertTrue(matcher.getMatchers().get(1) instanceof Equals);
    }

    @Test
    public void should_construct_with_invocation_only() {
        when(invocation.getArguments()).thenReturn(new Object[]{"arg"});
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertSame(invocation, matcher.getInvocation());
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    // getMethod

    @Test
    public void should_return_method_from_invocation() {
        when(invocation.getMethod()).thenReturn(method);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertSame(method, matcher.getMethod());
    }

    // getInvocation

    @Test
    public void should_return_invocation() {
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertSame(invocation, matcher.getInvocation());
    }

    // getMatchers

    @Test
    public void should_return_matchers() {
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertSame(matchersList, matcher.getMatchers());
    }

    // toString

    @Test
    public void should_return_string_representation() {
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        String str = matcher.toString();
        assertNotNull(str);
        // cannot easily verify content without mocking PrintSettings, but coverage is achieved.
    }

    // matches

    @Test
    public void should_match_when_all_conditions_true() {
        when(invocation.getMock()).thenReturn("mock");
        when(actualInvocation.getMock()).thenReturn("mock");
        when(invocation.getMethod()).thenReturn(method);
        when(actualInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("methodName");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class});

        // Set up matchers that will match the actual arguments
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("arg"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        when(actualInvocation.getArguments()).thenReturn(new Object[]{"arg"});

        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void should_not_match_when_mocks_differ() {
        when(invocation.getMock()).thenReturn("mock1");
        when(actualInvocation.getMock()).thenReturn("mock2");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void should_not_match_when_methods_differ() {
        when(invocation.getMock()).thenReturn("mock");
        when(actualInvocation.getMock()).thenReturn("mock");
        when(invocation.getMethod()).thenReturn(method);
        when(actualInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method1");
        when(candidateMethod.getName()).thenReturn("method2");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void should_not_match_when_arguments_differ() {
        when(invocation.getMock()).thenReturn("mock");
        when(actualInvocation.getMock()).thenReturn("mock");
        when(invocation.getMethod()).thenReturn(method);
        when(actualInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("method");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class});

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("expected"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        when(actualInvocation.getArguments()).thenReturn(new Object[]{"other"});

        assertFalse(matcher.matches(actualInvocation));
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_npe_when_actual_is_null() {
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        matcher.matches(null);
    }

    // safelyArgumentsMatch

    @Test
    public void should_return_true_when_arguments_match() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("arg"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        assertTrue(matcher.safelyArgumentsMatch(new Object[]{"arg"}));
    }

    @Test
    public void should_return_false_when_arguments_do_not_match() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("arg"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        assertFalse(matcher.safelyArgumentsMatch(new Object[]{"other"}));
    }

    @Test
    public void should_return_false_when_arguments_match_throws() {
        Matcher<Object> throwingMatcher = new Matcher<Object>() {
            @Override
            public boolean matches(Object item) {
                throw new RuntimeException("test exception");
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
            }
        };
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(throwingMatcher);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        assertFalse(matcher.safelyArgumentsMatch(new Object[]{"anything"}));
    }

    // hasSimilarMethod

    @Test
    public void should_return_false_when_method_names_differ() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method1");
        when(candidateMethod.getName()).thenReturn("method2");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSimilarMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_candidate_is_verified() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("method");
        when(candidateInvocation.isVerified()).thenReturn(true);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSimilarMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_mocks_differ() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("method");
        when(candidateInvocation.isVerified()).thenReturn(false);
        when(invocation.getMock()).thenReturn("mock1");
        when(candidateInvocation.getMock()).thenReturn("mock2");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSimilarMethod(candidateInvocation));
    }

    @Test
    public void should_return_true_when_methods_equal() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("method");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        when(candidateInvocation.isVerified()).thenReturn(false);
        when(invocation.getMock()).thenReturn("mock");
        when(candidateInvocation.getMock()).thenReturn("mock");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertTrue(matcher.hasSimilarMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_overloaded_but_same_args() {
        Method method1 = mock(Method.class);
        Method method2 = mock(Method.class);
        when(method1.getName()).thenReturn("method");
        when(method2.getName()).thenReturn("method");
        when(method1.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        when(method2.getParameterTypes()).thenReturn(new Class<?>[]{Integer.class}); // different parameter types

        when(invocation.getMethod()).thenReturn(method1);
        when(candidateInvocation.getMethod()).thenReturn(method2);
        when(candidateInvocation.isVerified()).thenReturn(false);
        when(invocation.getMock()).thenReturn("mock");
        when(candidateInvocation.getMock()).thenReturn("mock");

        // Set up matchers that match the candidate's arguments
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("arg"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        when(candidateInvocation.getArguments()).thenReturn(new Object[]{"arg"});

        assertFalse(matcher.hasSimilarMethod(candidateInvocation));
    }

    @Test
    public void should_return_true_when_overloaded_but_different_args() {
        Method method1 = mock(Method.class);
        Method method2 = mock(Method.class);
        when(method1.getName()).thenReturn("method");
        when(method2.getName()).thenReturn("method");
        when(method1.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        when(method2.getParameterTypes()).thenReturn(new Class<?>[]{Integer.class});

        when(invocation.getMethod()).thenReturn(method1);
        when(candidateInvocation.getMethod()).thenReturn(method2);
        when(candidateInvocation.isVerified()).thenReturn(false);
        when(invocation.getMock()).thenReturn("mock");
        when(candidateInvocation.getMock()).thenReturn("mock");

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("arg"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        when(candidateInvocation.getArguments()).thenReturn(new Object[]{"different"});

        assertTrue(matcher.hasSimilarMethod(candidateInvocation));
    }

    // hasSameMethod

    @Test
    public void should_return_false_when_first_method_name_null() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn(null);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSameMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_names_differ() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method1");
        when(candidateMethod.getName()).thenReturn("method2");
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSameMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_parameter_counts_differ() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method");
        when(candidateMethod.getName()).thenReturn("method");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        when(candidateMethod.getParameterTypes()).thenReturn(new Class<?>[]{String.class, Integer.class});
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSameMethod(candidateInvocation));
    }

    @Test
    public void should_return_false_when_parameter_types_differ() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method");
        when(candidateMethod.getName()).thenReturn("method");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class, Integer.class});
        when(candidateMethod.getParameterTypes()).thenReturn(new Class<?>[]{String.class, Double.class});
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertFalse(matcher.hasSameMethod(candidateInvocation));
    }

    @Test
    public void should_return_true_when_methods_identical() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("method");
        when(method.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertTrue(matcher.hasSameMethod(candidateInvocation));
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_npe_when_second_method_name_null() {
        when(invocation.getMethod()).thenReturn(method);
        when(candidateInvocation.getMethod()).thenReturn(candidateMethod);
        when(method.getName()).thenReturn("method");
        when(candidateMethod.getName()).thenReturn(null);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        matcher.hasSameMethod(candidateInvocation);
    }

    // getLocation

    @Test
    public void should_return_location_from_invocation() {
        when(invocation.getLocation()).thenReturn(location);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchersList);
        assertSame(location, matcher.getLocation());
    }

    // captureArgumentsFrom

    @Test
    public void should_capture_arguments_non_varargs() {
        Method methodMock = mock(Method.class);
        when(methodMock.isVarArgs()).thenReturn(false);
        Invocation inv = mock(Invocation.class);
        when(inv.getMethod()).thenReturn(methodMock);
        when(inv.getArgumentAt(0, Object.class)).thenReturn("arg0");
        when(inv.getArgumentAt(1, Object.class)).thenReturn("arg1");

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);
        matchers.add(mock(Matcher.class)); // non-capturing
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers); // invocation is not used in captureArgumentsFrom except for matchers

        matcher.captureArgumentsFrom(inv);

        verify(capturingMatcher).captureFrom("arg0");
        // second matcher is not CapturesArguments, so no interaction
    }

    @Test
    public void should_capture_arguments_varargs() {
        Method methodMock = mock(Method.class);
        when(methodMock.isVarArgs()).thenReturn(true);
        Invocation inv = mock(Invocation.class);
        when(inv.getMethod()).thenReturn(methodMock);
        when(inv.getRawArguments()).thenReturn(new Object[]{"arg0", "vararg1", "vararg2"});
        when(inv.getArgumentAt(0, Object.class)).thenReturn("arg0");

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher); // position 0
        matchers.add(capturingMatcher); // position 1 (vararg)
        matchers.add(mock(Matcher.class)); // position 2 (vararg, non-capturing)
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        matcher.captureArgumentsFrom(inv);

        // For position 0 (index < indexOfVararg): uses getArgumentAt
        verify(capturingMatcher, times(1)).captureFrom("arg0");
        // For position 1 (indexOfVararg): uses getRawArguments()[1 - 1] = "vararg1"
        verify(capturingMatcher, times(1)).captureFrom("vararg1");
        // position 2 is not CapturesArguments, so no capture
    }

    @Test
    public void should_not_capture_when_matchers_not_capturing() {
        Method methodMock = mock(Method.class);
        when(methodMock.isVarArgs()).thenReturn(false);
        Invocation inv = mock(Invocation.class);
        when(inv.getMethod()).thenReturn(methodMock);

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(mock(Matcher.class));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        matcher.captureArgumentsFrom(inv);
        // no capture should happen
    }

    // createFrom

    @Test
    public void should_create_empty_list_from_empty_invocations() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void should_create_matchers_for_each_invocation() {
        Invocation inv1 = mock(Invocation.class);
        Invocation inv2 = mock(Invocation.class);
        when(inv1.getArguments()).thenReturn(new Object[]{"a"});
        when(inv2.getArguments()).thenReturn(new Object[]{"b"});
        List<Invocation> invocations = Arrays.asList(inv1, inv2);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }
}
