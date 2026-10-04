package org.mockito.internal.stubbing.answers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.Invocation;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnswersValidatorTest {

    private AnswersValidator validator;
    private Invocation invocation;

    @Before
    public void setUp() {
        validator = new AnswersValidator();
        invocation = mock(Invocation.class);
    }

    @Test
    public void shouldValidateGenericAnswerWithoutErrors() {
        Answer<?> answer = mock(Answer.class);
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenThrowsExceptionWithNullThrowable() {
        ThrowsException answer = new ThrowsException(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowRuntimeExceptionForThrowsException() {
        ThrowsException answer = new ThrowsException(new RuntimeException());
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowErrorForThrowsException() {
        ThrowsException answer = new ThrowsException(new Error());
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowValidCheckedException() {
        IOException exception = new IOException();
        when(invocation.isValidException(exception)).thenReturn(true);

        ThrowsException answer = new ThrowsException(exception);
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenCheckedExceptionIsInvalid() {
        IOException exception = new IOException();
        when(invocation.isValidException(exception)).thenReturn(false);

        ThrowsException answer = new ThrowsException(exception);
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenStubbingVoidMethodWithReturnValue() {
        when(invocation.isVoid()).thenReturn(true);
        Returns answer = new Returns("test");
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenReturningNullForPrimitiveReturnType() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(true);
        when(invocation.printMethodReturnType()).thenReturn("int");
        when(invocation.getMethodName()).thenReturn("foo");

        Returns answer = new Returns(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowNullForNonPrimitiveReturnType() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(false);

        Returns answer = new Returns(null);
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenReturnTypeIsInvalid() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.isValidReturnType(String.class)).thenReturn(false);
        when(invocation.printMethodReturnType()).thenReturn("Integer");
        when(invocation.getMethodName()).thenReturn("foo");

        Returns answer = new Returns("test");
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowValidReturnValue() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.isValidReturnType(String.class)).thenReturn(true);

        Returns answer = new Returns("test");
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class)
    public void shouldFailWhenDoesNothingOnNonVoidMethod() {
        when(invocation.isVoid()).thenReturn(false);

        DoesNothing answer = new DoesNothing();
        validator.validate(answer, invocation);
    }

    @Test
    public void shouldAllowDoesNothingOnVoidMethod() {
        when(invocation.isVoid()).thenReturn(true);

        DoesNothing answer = new DoesNothing();
        validator.validate(answer, invocation);
    }
}
