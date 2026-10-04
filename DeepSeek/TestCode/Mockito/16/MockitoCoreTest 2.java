package org.mockito.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockSettings;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.IOngoingStubbing;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubberImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.verification.api.VerificationMode;
import org.mockito.stubbing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class MockitoCoreTest {

    private MockitoCore mockitoCore;
    private Reporter reporterMock;
    private MockUtil mockUtilMock;
    private MockingProgress mockingProgressMock;

    @Before
    public void setUp() throws Exception {
        mockitoCore = new MockitoCore();
        reporterMock = mock(Reporter.class);
        mockUtilMock = mock(MockUtil.class);
        mockingProgressMock = mock(MockingProgress.class);

        // inject mocks using reflection
        java.lang.reflect.Field reporterField = MockitoCore.class.getDeclaredField("reporter");
        reporterField.setAccessible(true);
        reporterField.set(mockitoCore, reporterMock);

        java.lang.reflect.Field mockUtilField = MockitoCore.class.getDeclaredField("mockUtil");
        mockUtilField.setAccessible(true);
        mockUtilField.set(mockitoCore, mockUtilMock);

        java.lang.reflect.Field mockingProgressField = MockitoCore.class.getDeclaredField("mockingProgress");
        mockingProgressField.setAccessible(true);
        mockingProgressField.set(mockitoCore, mockingProgressMock);
    }

    @After
    public void tearDown() {
        reset(reporterMock, mockUtilMock, mockingProgressMock);
    }

    // ---- tests for mock() methods ----

    @Test
    public void testMockWithSettingsAndResetFlag() {
        Class<String> classToMock = String.class;
        MockSettingsImpl settings = mock(MockSettingsImpl.class);
        String expectedMock = "mock";
        when(mockUtilMock.createMock(classToMock, settings)).thenReturn(expectedMock);

        Object result = mockitoCore.mock(classToMock, settings, true);

        assertEquals(expectedMock, result);
        verify(mockingProgressMock).validateState();
        verify(mockingProgressMock).resetOngoingStubbing();
        verify(mockUtilMock).createMock(classToMock, settings);
    }

    @Test
    public void testMockWithSettings() {
        Class<Integer> classToMock = Integer.class;
        MockSettingsImpl settings = mock(MockSettingsImpl.class);
        Integer expectedMock = 42;
        when(mockUtilMock.createMock(classToMock, settings)).thenReturn(expectedMock);

        Object result = mockitoCore.mock(classToMock, settings);

        assertEquals(expectedMock, result);
        verify(mockingProgressMock).validateState();
        verify(mockingProgressMock).resetOngoingStubbing();
        verify(mockUtilMock).createMock(classToMock, settings);
    }

    // ---- tests for stub() ----

    @Test
    public void testStubWhenStubbingExists() {
        IOngoingStubbing stubbing = mock(IOngoingStubbing.class);
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(stubbing);

        IOngoingStubbing result = mockitoCore.stub();

        assertSame(stubbing, result);
        verify(mockingProgressMock).pullOngoingStubbing();
        verify(mockingProgressMock, never()).reset();
        verify(reporterMock, never()).missingMethodInvocation();
    }

    @Test(expected = RuntimeException.class)
    public void testStubWhenStubbingIsNullThrowsException() {
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(null);
        doThrow(new RuntimeException("missing method invocation")).when(reporterMock).missingMethodInvocation();

        mockitoCore.stub();
    }

    @Test
    public void testStubWhenStubbingIsNullVerifysReset() {
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(null);
        doThrow(new RuntimeException()).when(reporterMock).missingMethodInvocation();

        try {
            mockitoCore.stub();
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(mockingProgressMock).reset();
        verify(reporterMock).missingMethodInvocation();
    }

    // ---- tests for stub(T) ----

    @Test
    public void testStubMethodCall() {
        String methodCall = "test";
        DeprecatedOngoingStubbing<Object> depStub = mock(DeprecatedOngoingStubbing.class);
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(depStub);

        DeprecatedOngoingStubbing<Object> result = mockitoCore.stub(methodCall);

        assertSame(depStub, result);
        verify(mockingProgressMock).stubbingStarted();
        verify(mockingProgressMock).pullOngoingStubbing();
    }

    // ---- tests for when() ----

    @Test
    public void testWhen() {
        String methodCall = "test";
        OngngoingStubbing<Object> ongoingStub = mock(OngoingStubbing.class);
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(ongoingStub);

        OngngoingStubbing<Object> result = mockitoCore.when(methodCall);

        assertSame(ongoingStub, result);
        verify(mockingProgressMock).stubbingStarted();
        verify(mockingProgressMock).pullOngoingStubbing();
    }

    // ---- tests for verify(T, VerificationMode) ----

    @Test
    public void testVerifyWithNullMockThrowsException() {
        VericationMode mode = mock(VericationMode.class);
        doThrow(new RuntimeException("null passed to verify")).when(reporterMock).nullPassedToVerify();

        try {
            mockitoCore.verify(null, mode);
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(reporterMock).nullPassedToVerify();
        verify(mockingProgressMock, never()).verificationStarted(ny(VericationMode.class));
    }

    @Test
    public void testVerifyWithNonMockThrowsException() {
        Object nonMock = new Object();
        VericationMode mode = mock(VericationMode.class);
        when(mockUtilMock.isMock(nonMock)).thenReturn(false);
        doThrow(new RuntimeException("not a mock")).when(reporterMock).notAMockPassedToVerify();

        try {
            mockitoCore.verify(nonMock, mode);
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(reporterMock).notAMockPassedToVerify();
        verify(mockingProgressMock, never()).verificationStarted(ny(VericationMode.class));
    }

    @Test
    public void testVerifyWithValidMock() {
        Object mockObject = new Object();
        VericationMode mode = mock(VericationMode.class);
        when(mockUtilMock.isMock(mockObject)).thenReturn(true);

        Object result = mockitoCore.verify(mockObject, mode);

        assertSame(mockObject, result);
        verify(mockingProgressMock).verificationStarted(mode);
    }

    // ---- tests for reset() ----

    @Test
    public void testResetWithMultipleMocks() {
        Object mock1 = new Object();
        Object mock2 = new Object();

        mockitoCore.reset(mock1, mock2);

        verify(mockingProgressMock).validateState();
        verify(mockingProgressMock).reset();
        verify(mockingProgressMock).resetOngoingStubbing();
        verify(mockUtilMock).resetMock(mock1);
        verify(mockUtilMock).resetMock(mock2);
    }

    @Test
    public void testResetWithNoMocks() {
        Object[] empty = {};
        mockitoCore.reset(empty);

        verify(mockingProgressMock).validateState();
        verify(mockingProgressMock).reset();
        verify(mockingProgressMock).resetOngoingStubbing();
        verify(mockUtilMock, never()).resetMock(ny());
    }

    @Test
    public void testResetWithSingleMock() {
        Object mock = new Object();

        mockitoCore.reset(mock);

        verify(mockUtilMock).resetMock(mock);
    }

    // ---- tests for verifyNoMoreInteractions() and assertMocksNotEmpty() ----

    @Test(expected = RuntimeException.class)
    public void testVerifyNoMoreInteractionsNullMocksThrows() {
        doThrow(new RuntimeException("mocks have to be passed")).when(reporterMock).mocksHaveToBePassedToVerifyNoMoreInteractions();
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    @Test(expected = RuntimeException.class)
    public void testVerifyNoMoreInteractionsEmptyMocksThrows() {
        doThrow(new RuntimeException("mocks have to be passed")).when(reporterMock).mocksHaveToBePassedToVerifyNoMoreInteractions();
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    @Test
    public void testVerifyNoMoreInteractionsWithNullElementThrows() {
        doThrow(new RuntimeException("null passed to verifyNoMoreInteractions")).when(reporterMock).nullPassedToVerifyNoMoreInteractions();
        try {
            mockitoCore.verifyNoMoreInteractions(null, new Object());
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        // verify reporter was called before throwing
        verify(reporterMock).nullPassedToVerifyNoMoreInteractions();
    }

    @Test
    public void testVerifyNoMoreInteractionsWithNonMockThrows() {
        Object nonMock = new Object();
        when(mockUtilMock.getMockHandler(nonMock)).thenThrow(new NotAMockException("not a mock"));
        doThrow(new RuntimeException("not a mock passed")).when(reporterMock).notAMockPassedToVerifyNoMoreInteractions();

        try {
            mockitoCore.verifyNoMoreInteractions(nonMock);
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(reporterMock).notAMockPassedToVerifyNoMoreInteractions();
    }

    @Test
    public void testVerifyNoMoreInteractionsWithValidMocks() {
        Object mock1 = new Object();
        Object mock2 = new Object();
        MockHandlerInterface<Object> handler1 = mock(MockHandlerInterface.class);
        MockHandlerInterface<Object> handler2 = mock(MockHandlerInterface.class);
        when(mockUtilMock.getMockHandler(mock1)).thenReturn(handler1);
        when(mockUtilMock.getMockHandler(mock2)).thenReturn(handler2);

        mockitoCore.verifyNoMoreInteractions(mock1, mock2);

        verify(handler1).verifyNoMoreInteractions();
        verify(handler2).verifyNoMoreInteractions();
        verify(reporterMock, never()).nullPassedToVerifyNoMoreInteractions();
        verify(reporterMock, never()).notAMockPassedToVerifyNoMoreInteractions();
    }

    // ---- tests for inOrder() ----

    @Test(expected = RuntimeException.class)
    public void testInOrderWithNullMocks() {
        doThrow(new RuntimeException("mocks have to be passed to inOrder")).when(reporterMock).mocksHaveToBePassedWhenCreatingInOrder();
        mockitoCore.inOrder((Object[]) null);
    }

    @Test(expected = RuntimeException.class)
    public void testInOrderWithEmptyMocks() {
        doThrow(new RuntimeException("mocks have to be passed to inOrder")).when(reporterMock).mocksHaveToBePassedWhenCreatingInOrder();
        mockitoCore.inOrder(new Object[0]);
    }

    @Test
    public void testInOrderWithNullElementThrows() {
        doThrow(new RuntimeException("null passed when creating inOrder")).when(reporterMock).nullPassedWhenCreatingInOrder();
        try {
            mockitoCore.inOrder(null, new Object());
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(reporterMock).nullPassedWhenCreatingInOrder();
    }

    @Test
    public void testInOrderWithNonMockThrows() {
        Object nonMock = new Object();
        when(mockUtilMock.isMock(nonMock)).thenReturn(false);
        doThrow(new RuntimeException("not a mock passed when creating inOrder")).when(reporterMock).notAMockPassedWhenCreatingInOrder();

        try {
            mockitoCore.inOrder(nonMock);
            fail("Expected exception");
        } catch (RuntimeException e) {
            // expected
        }
        verify(reporterMock).notAMockPassedWhenCreatingInOrder();
    }

    @Test
    public void testInOrderWithValidMocks() {
        Object mock1 = new Object();
        Object mock2 = new Object();
        when(mockUtilMock.isMock(mock1)).thenReturn(true);
        when(mockUtilMock.isMock(mock2)).thenReturn(true);

        InOrder result = mockitoCore.inOrder(mock1, mock2);

        assertNotNull(result);
        // cannot easily assert InOrderImpl internals, but verify no reporter errors
        verify(reporterMock, never()).mocksHaveToBePassedWhenCreatingInOrder();
        verify(reporterMock, never()).nullPassedWhenCreatingInOrder();
        verify(reporterMock, never()).notAMockPassedWhenCreatingInOrder();
    }

    // ---- test for doAnswer() ----

    @Test
    public void testDoAnswer() {
        Answer answer = mock(Answer.class);

        Stubber stubber = mockitoCore.doAnswer(answer);

        assertNotNull(stubber);
        verify(mockingProgressMock).stubbingStarted();
        verify(mockingProgressMock).resetOngoingStubbing();
    }

    // ---- test for stubVoid() ----

    @Test
    public void testStubVoid() {
        Object mock = new Object();
        MockHandlerInterface<Object> handler = mock(MockHandlerInterface.class);
        VoidMethodStubbable<Object> voidStubbable = mock(VoidMethodStubbable.class);
        when(mockUtilMock.getMockHandler(mock)).thenReturn(handler);
        when(handler.voidMethodStubbable(mock)).thenReturn(voidStubbable);

        VoidMethodStubbable<Object> result = mockitoCore.stubVoid(mock);

        assertSame(voidStubbable, result);
        verify(mockingProgressMock).stubbingStarted();
    }

    // ---- test for validateMockitoUsage() ----

    @Test
    public void testValidateMockitoUsage() {
        mockitoCore.validateMockitoUsage();
        verify(mockingProgressMock).validateState();
    }

    // ---- test for getLastInvocation() ----

    @Test
    public void testGetLastInvocation() {
        OngngoingStubbingImpl<?> ongoingStubbing = mock(OngoingStubbingImpl.class);
        Invocation lastInvocation = mock(Invocation.class);
        List<Invocation> invocations = new ArrayList<Invocation>();
        invocations.add(mock(Invocation.class));
        invocations.add(lastInvocation);
        when(ongoingStubbing.getRegisteredInvocations()).thenReturn(invocations);
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(ongoingStubbing);

        Invocation result = mockitoCore.getLastInvocation();

        assertSame(lastInvocation, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetLastInvocationEmptyList() {
        OngngoingStubbingImpl<?> ongoingStubbing = mock(OngoingStubbingImpl.class);
        List<Invocation> invocations = new ArrayList<Invocation>();
        when(ongoingStubbing.getRegisteredInvocations()).thenReturn(invocations);
        when(mockingProgressMock.pullOngoingStubbing()).thenReturn(ongoingStubbing);

        mockitoCore.getLastInvocation(); // should throw IndexOutOfBoundsException
    }
}
