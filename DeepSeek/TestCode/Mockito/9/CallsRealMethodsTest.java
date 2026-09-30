package org.mockito.internal.stubbing.answers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CallsRealMethodsTest {

    @Mock
    private InvocationOnMock invocation;

    private CallsRealMethods answer;

    @Before
    public void setUp() {
        answer = new CallsRealMethods();
    }

    @Test
    public void testAnswerReturnsRealMethodResult() throws Throwable {
        Object expected = new Object();
        when(invocation.callRealMethod()).thenReturn(expected);

        Object result = answer.answer(invocation);

        assertSame(expected, result);
    }

    @Test(expected = RuntimeException.class)
    public void testAnswerPropagatesRuntimeException() throws Throwable {
        when(invocation.callRealMethod()).thenThrow(new RuntimeException("test"));

        answer.answer(invocation);
    }

    @Test(expected = IOException.class)
    public void testAnswerPropagatesCheckedException() throws Throwable {
        when(invocation.callRealMethod()).thenThrow(new IOException("test"));

        answer.answer(invocation);
    }

    @Test(expected = Error.class)
    public void testAnswerPropagatesError() throws Throwable {
        when(invocation.callRealMethod()).thenThrow(new Error("test"));

        answer.answer(invocation);
    }

    @Test(expected = NullPointerException.class)
    public void testAnswerWithNullInvocationThrowsNullPointerException() throws Throwable {
        answer.answer(null);
    }
}
