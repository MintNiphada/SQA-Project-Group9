package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;
    private Answer<Object> mockDelegate;
    private InvocationOnMock invocation;
    private Method method;

    @Before
    public void setUp() throws Exception {
        returnsSmartNulls = new ReturnsSmartNulls();
        mockDelegate = mock(Answer.class);
        Field delegateField = ReturnsSmartNulls.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        delegateField.set(returnsSmartNulls, mockDelegate);

        invocation = mock(InvocationOnMock.class);
        method = mock(Method.class);
        when(invocation.getMethod()).thenReturn(method);
    }

    @Test
    public void shouldReturnDelegateValueWhenNonNull() throws Throwable {
        Object expected = new Object();
        when(mockDelegate.answer(invocation)).thenReturn(expected);

        Object result = returnsSmartNulls.answer(invocation);

        assertSame(expected, result);
    }

    @Test
    public void shouldReturnSmartNullWhenDelegateReturnsNullAndTypeIsMockable() throws Throwable {
        when(mockDelegate.answer(invocation)).thenReturn(null);
        when(method.getReturnType()).thenReturn(Foo.class);
        when(method.getName()).thenReturn("bar");
        when(invocation.getArguments()).thenReturn(new Object[]{"arg1", 123});

        Object result = returnsSmartNulls.answer(invocation);

        assertNotNull(result);
        assertTrue(result instanceof Foo);

        String smartNullString = result.toString();
        assertTrue(smartNullString.contains("SmartNull returned by unstubbed"));
        assertTrue(smartNullString.contains("bar(\"arg1\", 123)"));

        try {
            ((Foo) result).bar();
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
        }
    }

    @Test
    public void shouldReturnNullWhenDelegateReturnsNullAndTypeNotMockable() throws Throwable {
        when(mockDelegate.answer(invocation)).thenReturn(null);
        when(method.getReturnType()).thenReturn(String.class);

        Object result = returnsSmartNulls.answer(invocation);

        assertNull(result);
    }

    private interface Foo {
        String bar();
    }
}
