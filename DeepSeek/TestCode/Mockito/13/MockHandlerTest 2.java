package org.mockito.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

import java.lang.reflect.Field;

public class MockHandlerTest {

    private MockHandler<Object> handler;
    private MockSettingsImpl mockSettings;
    private InvocationContainerImpl mockInvocationContainer;
    private MatchersBinder mockMatchersBinder;
    private MockingProgress mockProgress;

    @Before
    public void setUp() throws Exception {
        mockSettings = new MockSettingsImpl();
        handler = new MockHandler<Object>(mockSettings);

        // Replace internal fields with mocks for isolation
        mockInvocationContainer = mock(InvocationContainerImpl.class);
        mockMatchersBinder = mock(MatchersBinder.class);
        mockProgress = mock(MockingProgress.class);

        setField(handler, "invocationContainerImpl", mockInvocationContainer);
        setField(handler, "matchersBinder", mockMatchersBinder);
        setField(handler, "mockingProgress", mockProgress);
    }

    // Helper to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Constructor tests
    @Test
    public void shouldConstructWithMockSettings() {
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);
        assertNotNull(handler);
        // verify internal fields are initialized
        assertNotNull(handler.invocationContainerImpl);
        assertNotNull(handler.matchersBinder);
        assertNotNull(handler.mockingProgress);
        assertEquals(mockSettings, handler.getMockSettings());
    }

    @Test
    public void shouldConstructWithOldMockHandler() {
        MockHandler<Object> old = new MockHandler<Object>(mockSettings);
        MockHandler<Object> handler = new MockHandler<Object>(old);
        assertNotNull(handler);
        assertSame(mockSettings, handler.getMockSettings());
    }

    @Test
    public void shouldUseDefaultConstructorForTests() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
    }

    // handle - stubbing path (hasAnswersForStubbing)
    @Test
    public void shouldSetMethodForStubbingWhenAnswersForStubbingPresent() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        InvocationMatcher matcher = mock(InvocationMatcher.class);
        
        when(mockInvocationContainer.hasAnswersForStubbing()).thenReturn(true);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);
        
        Object result = handler.handle(invocation);
        
        assertNull(result);
        verify(mockInvocationContainer).setMethodForStubbing(matcher);
        verify(mockProgress, never()).pullVerificationMode();
        verify(mockProgress, never()).reportOngoingStubbing(any(OngoingStubbingImpl.class));
    }

    // handle - verification mode with matching mock
    @Test
    public void shouldVerifyWithMockAwareVerificationModeWhenMockMatches() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        Object mockObject = new Object();
        when(invocation.getMock()).thenReturn(mockObject);

        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);

        MockAwareVerificationMode verificationMode = mock(MockAwareVerificationMode.class);
        when(verificationMode.getMock()).thenReturn(mockObject);

        when(mockProgress.pullVerificationMode()).thenReturn(verificationMode);

        Object result = handler.handle(invocation);

        assertNull(result);
        verify(verificationMode).verify(any(VerificationDataImpl.class));
        verify(mockProgress, never()).reportOngoingStubbing(any(OngoingStubbingImpl.class));
    }

    // handle - verification mode with non-matching mock (bug 138 scenario)
    @Test
    public void shouldNotVerifyAndContinueWhenMockDoesNotMatch() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        Object mockObject = new Object();
        Object otherMock = new Object();
        when(invocation.getMock()).thenReturn(mockObject);

        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);

        MockAwareVerificationMode verificationMode = mock(MockAwareVerificationMode.class);
        when(verificationMode.getMock()).thenReturn(otherMock); // different mock

        when(mockProgress.pullVerificationMode()).thenReturn(verificationMode);
        when(mockInvocationContainer.findAnswerFor(invocation)).thenReturn(null);
        Answer defaultAnswer = mock(Answer.class);
        when(mockSettings.getDefaultAnswer()).thenReturn(defaultAnswer);
        when(defaultAnswer.answer(invocation)).thenReturn("value");

        Object result = handler.handle(invocation);

        assertEquals("value", result);
        verify(verificationMode, never()).verify(any(VerificationDataImpl.class));
        verify(mockInvocationContainer).setInvocationForPotentialStubbing(matcher);
        verify(mockProgress).reportOngoingStubbing(any(OngoingStubbingImpl.class));
        verify(mockInvocationContainer).resetInvocationForPotentialStubbing(matcher);
    }

    // handle - verification mode is not MockAwareVerificationMode (e.g., Times)
    @Test
    public void shouldFallThroughWhenVerificationModeNotMockAware() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);

        VerificationMode verificationMode = mock(VerificationMode.class); // not MockAware
        when(mockProgress.pullVerificationMode()).thenReturn(verificationMode);

        when(mockInvocationContainer.findAnswerFor(invocation)).thenReturn(null);
        Answer defaultAnswer = mock(Answer.class);
        when(mockSettings.getDefaultAnswer()).thenReturn(defaultAnswer);
        when(defaultAnswer.answer(invocation)).thenReturn(42);

        Object result = handler.handle(invocation);

        assertEquals(42, result);
        // VerificationMode.verify is never called because mock does not match condition
        verify(verificationMode, never()).verify(any(VerificationDataImpl.class));
        verify(mockInvocationContainer).setInvocationForPotentialStubbing(matcher);
    }

    // handle - no verification mode, stubbed invocation found
    @Test
    public void shouldReturnStubbedAnswerWhenStubbedInvocationFound() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);

        when(mockProgress.pullVerificationMode()).thenReturn(null); // no verification

        StubbedInvocationMatcher stubbed = mock(StubbedInvocationMatcher.class);
        when(stubbed.answer(invocation)).thenReturn("stubbed");
        when(mockInvocationContainer.findAnswerFor(invocation)).thenReturn(stubbed);

        Object result = handler.handle(invocation);

        assertEquals("stubbed", result);
        verify(stubbed).captureArgumentsFrom(invocation);
        verify(mockInvocationContainer, never()).resetInvocationForPotentialStubbing(any(InvocationMatcher.class));
        verify(mockProgress).reportOngoingStubbing(any(OngoingStubbingImpl.class));
    }

    // handle - no verification mode, no stubbed invocation -> default answer
    @Test
    public void shouldUseDefaultAnswerWhenNoStubbedInvocationFound() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);

        when(mockProgress.pullVerificationMode()).thenReturn(null);

        when(mockInvocationContainer.findAnswerFor(invocation)).thenReturn(null);
        Answer defaultAnswer = mock(Answer.class);
        when(mockSettings.getDefaultAnswer()).thenReturn(defaultAnswer);
        when(defaultAnswer.answer(invocation)).thenReturn("default");

        Object result = handler.handle(invocation);

        assertEquals("default", result);
        verify(mockInvocationContainer).resetInvocationForPotentialStubbing(matcher);
        verify(mockProgress).reportOngoingStubbing(any(OngoingStubbingImpl.class));
    }

    // handle - exception during stubbing path
    @Test(expected = RuntimeException.class)
    public void shouldPropagateExceptionFromMethodForStubbing() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        InvocationMatcher matcher = mock(InvocationMatcher.class);
        when(mockInvocationContainer.hasAnswersForStubbing()).thenReturn(true);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(matcher);
        doThrow(new RuntimeException("error")).when(mockInvocationContainer).setMethodForStubbing(matcher);

        handler.handle(invocation);
    }

    // voidMethodStubbable
    @Test
    public void shouldCreateVoidMethodStubbable() {
        Object mock = new Object();
        VoidMethodStubbable<Object> stubbable = handler.voidMethodStubbable(mock);
        assertNotNull(stubbable);
        // check that the returned object is of type VoidMethodStubbableImpl via implementation
        assertTrue(stubbable instanceof VoidMethodStubbableImpl);
    }

    // getMockSettings
    @Test
    public void shouldReturnMockSettings() {
        assertSame(mockSettings, handler.getMockSettings());
    }

    // setAnswersForStubbing
    @Test
    public void shouldDelegateSetAnswersForStubbing() {
        List<Answer> answers = Arrays.asList(mock(Answer.class));
        handler.setAnswersForStubbing(answers);
        verify(mockInvocationContainer).setAnswersForStubbing(answers);
    }

    // getInvocationContainer
    @Test
    public void shouldReturnInvocationContainer() {
        assertSame(mockInvocationContainer, handler.getInvocationContainer());
    }

    // handle - validation state is called
    @Test
    public void shouldValidateStateBeforeVerification() throws Throwable {
        Invocation invocation = mock(Invocation.class);
        when(mockMatchersBinder.bindMatchers(mockProgress.getArgumentMatcherStorage(), invocation)).thenReturn(mock(InvocationMatcher.class));

        when(mockProgress.pullVerificationMode()).thenReturn(null);
        when(mockInvocationContainer.findAnswerFor(invocation)).thenReturn(null);
        when(mockSettings.getDefaultAnswer()).thenReturn(mock(Answer.class));

        handler.handle(invocation);

        verify(mockProgress).validateState();
    }
}
