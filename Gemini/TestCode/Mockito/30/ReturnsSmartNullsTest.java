package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class ReturnsSmartNullsTest {

    interface Foo {
        Bar getBar();
        Bar getBarWithArgs(String a, int b);
        String getString();
        int getInt();
        FinalClass getFinalClass();
    }

    interface Bar {
        void doSomething();
    }

    static final class FinalClass {
    }

    @Test
    public void testReturnsOrdinaryValueFromDelegate() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getString");
        Mockito.when(invocation.getMethod()).thenReturn(method);

        Object result = returnsSmartNulls.answer(invocation);
        Assert.assertEquals("", result);
    }

    @Test
    public void testReturnsPrimitiveValueFromDelegate() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getInt");
        Mockito.when(invocation.getMethod()).thenReturn(method);

        Object result = returnsSmartNulls.answer(invocation);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testReturnsNullForUnmockableFinalClass() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getFinalClass");
        Mockito.when(invocation.getMethod()).thenReturn(method);

        Object result = returnsSmartNulls.answer(invocation);
        Assert.assertNull(result);
    }

    @Test
    public void testSmartNullToStringWithoutArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getBar");
        Mockito.when(invocation.getMethod()).thenReturn(method);
        Mockito.when(invocation.getArguments()).thenReturn(new Object[0]);

        Object smartNull = returnsSmartNulls.answer(invocation);
        Assert.assertNotNull(smartNull);
        Assert.assertTrue(smartNull instanceof Bar);
        Assert.assertEquals("SmartNull returned by unstubbed getBar() method on mock", smartNull.toString());
    }

    @Test
    public void testSmartNullToStringWithArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getBarWithArgs", String.class, int.class);
        Mockito.when(invocation.getMethod()).thenReturn(method);
        Mockito.when(invocation.getArguments()).thenReturn(new Object[]{"test", 123});

        Object smartNull = returnsSmartNulls.answer(invocation);
        Assert.assertNotNull(smartNull);
        Assert.assertEquals("SmartNull returned by unstubbed getBarWithArgs(test, 123) method on mock", smartNull.toString());
    }

    @Test(expected = SmartNullPointerException.class)
    public void testSmartNullInvocationThrowsSmartNullPointerException() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        InvocationOnMock invocation = Mockito.mock(InvocationOnMock.class);
        Method method = Foo.class.getMethod("getBar");
        Mockito.when(invocation.getMethod()).thenReturn(method);
        Mockito.when(invocation.getArguments()).thenReturn(new Object[0]);

        Bar bar = (Bar) returnsSmartNulls.answer(invocation);
        bar.doSomething();
    }

    @Test
    public void testSerialization() throws Exception {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsSmartNulls);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertTrue(deserialized instanceof ReturnsSmartNulls);
    }
}
