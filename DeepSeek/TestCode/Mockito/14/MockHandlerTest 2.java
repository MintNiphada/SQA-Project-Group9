package org.mockito.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.VoidMethodStubbableImpl;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;
import org.mockito.internal.progress.ArgumentMatcherStorage;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockHandlerTest {

    @Mock
    private MockSettingsImpl mockSettings;
    @Mock
    private InvocationContainerImpl invocationContainer;
    @Mock
    private MatchersBinder matchersBinder;
    @Mock
    private MockingProgress mockingProgress;
    @Mock
    private Invocation invocation;
    @Mock
    private Answer<Object> defaultAnswer;
    @Mock
    private VerificationMode verificationMode;
    @Mock
    private StubbedInvocationMatcher stubbedInvocation;
    @Mock
    private InvocationMatcher invocationMatcher;
    @Mock
    private ArgumentMatcherStorage argumentMatcherStorage;
    @Mock
    private List<Answer> answers;
    @Mock
    private MockHandlerInterface<Object> oldMockHandler;

    private MockHandler<Object> handler;

    @Before
    public void setUp() {
        handler = new MockHandler<Object>(mockSettings);
        // Replace internal fields with mocks for controlled testing
        handler.invocationContainerImpl = invocationContainer;
        handler.matchersBinder = matchersBinder;
        handler.mockingProgress = mockingProgress;
    }

    @Test
    public void testConstructorWithMockSettings() {
        MockHandler<Object> freshHandler = new MockHandler<Object>(mockSettings);
        assertNotNull(freshHandler.getMockSettings());
        assertNotNull(freshHandler.invocationContainerImpl);
        assertNotNull(freshHandler.matchersBinder);
        assertNotNull(freshHandler.mockingProgress);
    }

    @Test
    public void testNoArgConstructor() {
        MockHandler<Object> freshHandler = new MockHandler<Object>();
        assertNotNull(freshHandler.getMockSettings());
        assertNotNull(freshHandler.invocationContainerImpl);
        assertNotNull(freshHandler.matchersBinder);
        assertNotNull(freshHandler.mockingProgress);
    }

    @Test
    public void testConstructorWithOldMockHandler() {
        when(oldMockHandler.getMockSettings()).thenReturn(mockSettings);
        MockHandler<Object> handlerFromOld = new MockHandler<Object>(oldMockHandler);
        assertSame(mockSettings, handlerFromOld.getMockSettings());
    }

    @Test
    public void testHandleWhenHasAnswersForStubbing() throws Throwable {
        when(invocationContainer.hasAnswersForStubbing()).thenReturn(true);
        when(matchersBinder.bindMatchers(any(ArgumentMatcherStorage.class), eq(invocation)))
                .thenReturn(invocationMatcher);

        Object result = handler.handle(invocation);

        assertNull(result);
        verify(invocationContainer).setMethodForStubbing(invocationMatcher);
        verify(mockingProgress, never()).pullVerificationMode();
        verify(mockingProgress, never()).validateState();
    }

    @Test
    public void testHandleWhenVerificationModeNotNull() throws Throwable {
        when(invocationContainer.hasAnswersForStubbing()).thenReturn(false);
        when(mockingProgress.pullVerificationMode()).thenReturn(verificationMode);
        when(mockingProgress.getArgumentMatcherStorage()).thenReturn(argumentMatcherStorage);
        when(matchersBinder.bindMatchers(argumentMatcherStorage, invocation)).thenReturn(invocationMatcher);
        when(invocationContainer.getInvocations()).thenReturn(java.util.Collections.emptyList());

        Object result = handler.handle(invocation);

        assertNull(result);
        verify(mockingProgress).validateState();
        verify(verificationMode).verify(any(VerificationDataImpl.class));
        verify(invocationContainer, never()).setInvocationForPotentialStubbing(any(InvocationMatcher.class));
        verify(mockingProgress, never()).reportOngoingStubbing(any(OngoingStubbingImpl.class));
    }

    @Test
    public void testHandleWhenStubbedInvocationFound() throws Throwable {
        when(invocationContainer.hasAnswersForStubbing()).thenReturn(false);
        when(mockingProgress.pullVerificationMode()).thenReturn(null);
        when(mockingProgress.getArgumentMatcherStorage()).thenReturn(argumentMatcherStorage);
        when(matchersBinder.bindMatchers(argumentMatcherStorage, invocation)).thenReturn(invocationMatcher);
        when(invocationContainer.findAnswerFor(invocation)).thenReturn(stubbedInvocation);
        when(stubbedInvocation.answer(invocation)).thenReturn("stubbed answer");

        Object result = handler.handle(invocation);

        assertEquals("stubbed answer", result);
        verify(stubbedInvocation).captureArgumentsFrom(invocation);
        verify(stubbedInvocation).answer(invocation);
        verify(mockSettings, never()).getDefaultAnswer();
        verify(invocationContainer, never()).resetInvocationForPotentialStubbing(any(InvocationMatcher.class));
    }

    @Test
    public void testHandleWhenNoStubbedInvocation() throws Throwable {
        when(invocationContainer.hasAnswersForStubbing()).thenReturn(false);
        when(mockingProgress.pullVerificationMode()).thenReturn(null);
        when(mockingProgress.getArgumentMatcherStorage()).thenReturn(argumentMatcherStorage);
        when(matchersBinder.bindMatchers(argumentMatcherStorage, invocation)).thenReturn(invocationMatcher);
        when(invocationContainer.findAnswerFor(invocation)).thenReturn(null);
        when(mockSettings.getDefaultAnswer()).thenReturn(defaultAnswer);
        when(defaultAnswer.answer(invocation)).thenReturn("default answer");

        Object result = handler.handle(invocation);

        assertEquals("default answer", result);
        verify(invocationContainer).resetInvocationForPotentialStubbing(invocationMatcher);
        verify(stubbedInvocation, never()).answer(any(Invocation.class));
    }

    @Test(expected = NullPointerException.class)
    public void testHandleWithNullInvocation() throws Throwable {
        handler.handle(null);
    }

    @Test
    public void testVoidMethodStubbable() {
        Object mock = new Object();
        VoidMethodStubbable<Object> result = handler.voidMethodStubbable(mock);
        assertNotNull(result);
        assertTrue(result instanceof VoidMethodStubbableImpl);
    }

    @Test
    public void testGetMockSettings() {
        assertSame(mockSettings, handler.getMockSettings());
    }

    @Test
    public void testSetAnswersForStubbing() {
        handler.setAnswersForStubbing(answers);
        verify(invocationContainer).setAnswersForStubbing(answers);
    }

    @Test
    public void testGetInvocationContainer() {
        assertSame(invocationContainer, handler.getInvocationContainer());
    }
}
