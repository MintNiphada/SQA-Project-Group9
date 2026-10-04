package org.mockito.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullPassedToVerifyException;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;

import java.util.List;

import static org.junit.Assert.*;

public class MockitoCoreTest {

    private MockitoCore mockitoCore;

    @Before
    public void setUp() {
        mockitoCore = new MockitoCore();
        new ThreadSafeMockingProgress().reset();
    }

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().reset();
    }

    @Test
    public void shouldCreateMockWithSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockitoCore.mock(List.class, settings);
        assertNotNull(mock);
    }

    @Test
    public void shouldCreateMockWithThreeArguments() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockitoCore.mock(List.class, settings, true);
        assertNotNull(mock);
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenStubbingWithoutMethodCall() {
        mockitoCore.stub();
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenWhenCalledWithoutMethodCall() {
        mockitoCore.when("someMethodCall");
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenDeprecatedStubCalledWithoutMethodCall() {
        mockitoCore.stub("someMethodCall");
    }

    @Test
    public void shouldVerifyMockSuccessfully() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> returned = mockitoCore.verify(mock, VerificationModeFactory.times(1));
        assertSame(mock, returned);
    }

    @Test(expected = NullPassedToVerifyException.class)
    public void shouldThrowExceptionWhenNullPassedToVerify() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenNonMockPassedToVerify() {
        mockitoCore.verify("not a mock", VerificationModeFactory.times(1));
    }

    @Test
    public void shouldResetMocks() {
        List<?> mock1 = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> mock2 = mockitoCore.mock(List.class, new MockSettingsImpl());
        
        mock1.clear();
        mock2.clear();

        mockitoCore.reset(mock1, mock2);
        mockitoCore.validateMockitoUsage();
    }

    @Test
    public void shouldVerifyNoMoreInteractions() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(mock);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenNullArrayPassedToVerifyNoMoreInteractions() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenEmptyArrayPassedToVerifyNoMoreInteractions() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    @Test(expected = NullPassedToVerifyException.class)
    public void shouldThrowExceptionWhenNullElementPassedToVerifyNoMoreInteractions() {
        mockitoCore.verifyNoMoreInteractions(new Object[]{null});
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenNonMockPassedToVerifyNoMoreInteractions() {
        mockitoCore.verifyNoMoreInteractions("not a mock");
    }

    @Test
    public void shouldCreateInOrderSuccessfully() {
        List<?> mock1 = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> mock2 = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mock1, mock2);
        assertNotNull(inOrder);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenNullArrayPassedToInOrder() {
        mockitoCore.inOrder((Object[]) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenEmptyArrayPassedToInOrder() {
        mockitoCore.inOrder(new Object[0]);
    }

    @Test(expected = NullPassedToVerifyException.class)
    public void shouldThrowExceptionWhenNullElementPassedToInOrder() {
        mockitoCore.inOrder(new Object[]{null});
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenNonMockPassedToInOrder() {
        mockitoCore.inOrder("not a mock");
    }

    @Test
    public void shouldCreateDoAnswerStubber() {
        Answer<Object> dummyAnswer = invocation -> null;
        Stubber stubber = mockitoCore.doAnswer(dummyAnswer);
        assertNotNull(stubber);
    }

    @Test
    public void shouldStubVoidMethod() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        VoidMethodStubbable<?> stubbable = mockitoCore.stubVoid(mock);
        assertNotNull(stubbable);
    }

    @Test(expected = NotAMockException.class)
    public void shouldFailStubVoidOnNonMock() {
        mockitoCore.stubVoid("not a mock");
    }

    @Test
    public void shouldValidateMockitoUsage() {
        mockitoCore.validateMockitoUsage();
    }

    @Test
    public void shouldGetLastInvocationWhenStubbing() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mock.get(0);
        Invocation lastInvocation = mockitoCore.getLastInvocation();
        assertNotNull(lastInvocation);
        assertEquals("get", lastInvocation.getMethod().getName());
    }

    @Test
    public void shouldSuccessfullyStubWhenMethodCallRecorded() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.when(mock.get(0)).thenReturn("value");
        assertEquals("value", mock.get(0));
    }

    @Test
    public void shouldSuccessfullyStubUsingDeprecatedStubWhenMethodCallRecorded() {
        List<?> mock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.stub(mock.get(0)).toReturn("value");
        assertEquals("value", mock.get(0));
    }
}
