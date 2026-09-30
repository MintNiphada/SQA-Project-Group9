package org.mockito.internal.stubbing.answers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CallsRealMethodsTest {

    private CallsRealMethods callsRealMethods;

    @Before
    public void setUp() {
        callsRealMethods = new CallsRealMethods();
    }

    @Test
    public void shouldReturnResultFromCallRealMethod() throws Throwable {
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        String expectedResult = "realMethodResult";
        Mockito.when(invocation.callRealMethod()).thenReturn(expectedResult);

        Object result = callsRealMethods.answer(invocation);

        Assert.assertEquals(expectedResult, result);
        Mockito.verify(invocation).callRealMethod();
    }

    @Test
    public void shouldReturnNullWhenCallRealMethodReturnsNull() throws Throwable {
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Mockito.when(invocation.callRealMethod()).thenReturn(null);

        Object result = callsRealMethods.answer(invocation);

        Assert.assertNull(result);
        Mockito.verify(invocation).callRealMethod();
    }

    @Test(expected = RuntimeException.class)
    public void shouldPropagateRuntimeExceptionFromCallRealMethod() throws Throwable {
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Mockito.when(invocation.callRealMethod()).thenThrow(new RuntimeException("Error in real method"));

        callsRealMethods.answer(invocation);
    }

    @Test(expected = Exception.class)
    public void shouldPropagateCheckedExceptionFromCallRealMethod() throws Throwable {
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Mockito.when(invocation.callRealMethod()).thenThrow(new Exception("Checked exception"));

        callsRealMethods.answer(invocation);
    }

    @Test(expected = Error.class)
    public void shouldPropagateErrorFromCallRealMethod() throws Throwable {
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Mockito.when(invocation.callRealMethod()).thenThrow(new AssertionError("Assertion failed"));

        callsRealMethods.answer(invocation);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenInvocationIsNull() throws Throwable {
        callsRealMethods.answer(null);
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(callsRealMethods);
        objectOutputStream.flush();
        objectOutputStream.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object deserializedObject = objectInputStream.readObject();
        objectInputStream.close();

        Assert.assertNotNull(deserializedObject);
        Assert.assertTrue(deserializedObject instanceof CallsRealMethods);

        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Mockito.when(invocation.callRealMethod()).thenReturn("deserializedResult");

        CallsRealMethods deserializedAnswer = (CallsRealMethods) deserializedObject;
        Object result = null;
        try {
            result = deserializedAnswer.answer(invocation);
        } catch (Throwable throwable) {
            Assert.fail("Unexpected throwable during answer invocation: " + throwable.getMessage());
        }

        Assert.assertEquals("deserializedResult", result);
        Mockito.verify(invocation).callRealMethod();
    }
}
