package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;

    interface Foo {
        List<String> getList();
        String getFinalClass();
        Bar getBar();
        int getInt();
    }

    interface Bar {
        void doSomething();
        String getName();
    }

    final class FinalClass {}

    interface HasFinalReturn {
        FinalClass getFinal();
    }

    @Before
    public void setUp() {
        returnsSmartNulls = new ReturnsSmartNulls();
    }

    @Test
    public void shouldReturnEmptyListForListReturnType() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getList")).toInvocation();
        Object result = returnsSmartNulls.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    public void shouldReturnZeroForPrimitiveInt() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getInt")).toInvocation();
        Object result = returnsSmartNulls.answer(invocation);
        assertEquals(0, result);
    }

    @Test
    public void shouldReturnEmptyStringForString() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getFinalClass")).toInvocation();
        Object result = returnsSmartNulls.answer(invocation);
        assertEquals("", result);
    }

    @Test
    public void shouldReturnNullForUnimposterisableFinalClass() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(HasFinalReturn.class.getMethod("getFinal")).toInvocation();
        Object result = returnsSmartNulls.answer(invocation);
        assertNull(result);
    }

    @Test
    public void shouldReturnSmartNullForInterfaces() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getBar")).toInvocation();
        Object smartNull = returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);
        assertTrue(smartNull instanceof Bar);
    }

    @Test
    public void shouldPrintHelpfulToStringOnSmartNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getBar")).toInvocation();
        Object smartNull = returnsSmartNulls.answer(invocation);
        assertEquals("SmartNull returned by unstubbed getBar() method on mock", smartNull.toString());
    }

    @Test(expected = SmartNullPointerException.class)
    public void shouldThrowSmartNullPointerExceptionWhenMethodInvokedOnSmartNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getBar")).toInvocation();
        Bar smartNull = (Bar) returnsSmartNulls.answer(invocation);
        smartNull.doSomething();
    }

    @Test(expected = SmartNullPointerException.class)
    public void shouldThrowSmartNullPointerExceptionWhenReturningMethodInvokedOnSmartNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().method(Foo.class.getMethod("getBar")).toInvocation();
        Bar smartNull = (Bar) returnsSmartNulls.answer(invocation);
        smartNull.getName();
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsSmartNulls);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsSmartNulls);
    }
}
