package org.mockito.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.IOngoingStubbing;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.verification.InOrderContextImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.InOrderContext;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.DeprecatedOngoingStubbing;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;

public class MockitoCoreTest {

    private MockitoCore mockitoCore;
    private MockingProgress mockingProgress;

    @Before
    public void setUp() {
        mockitoCore = new MockitoCore();
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.reset();
    }

    @After
    public void tearDown() {
        mockingProgress.reset();
        mockingProgress.validateState();
    }

    @Test
    public void shouldCreateMockSuccessfully() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> listMock = mockitoCore.mock(List.class, settings);
        Assert.assertNotNull(listMock);
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenStubbingWithoutMethodCall() {
        mockitoCore.stub();
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenWhenCalledWithoutMethodInvocation() {
        mockitoCore.when("someString");
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowExceptionWhenStubCalledWithoutMethodInvocation() {
        mockitoCore.stub("someString");
    }

    @Test
    public void shouldStubMethodCallSuccessfully() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        listMock.get(0);
        IOngoingStubbing stubbing = mockitoCore.stub();
        Assert.assertNotNull(stubbing);
    }

    @Test
    public void shouldStubWithWhenSuccessfully() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        OngoingStubbing<Object> ongoingStubbing = mockitoCore.when(listMock.get(0));
        Assert.assertNotNull(ongoingStubbing);
        ongoingStubbing.thenReturn("value");
        Assert.assertEquals("value", listMock.get(0));
    }

    @Test
    public void shouldStubWithDeprecatedStubSuccessfully() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        DeprecatedOngoingStubbing<Object> stubbing = mockitoCore.stub(listMock.get(0));
        Assert.assertNotNull(stubbing);
        stubbing.toReturn("deprecatedValue");
        Assert.assertEquals("deprecatedValue", listMock.get(0));
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowExceptionWhenVerifyingNull() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenVerifyingNonMock() {
        mockitoCore.verify("not a mock", VerificationModeFactory.times(1));
    }

    @Test
    public void shouldVerifyMockSuccessfully() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> returned = mockitoCore.verify(listMock, VerificationModeFactory.times(1));
        Assert.assertSame(listMock, returned);
        listMock.clear();
    }

    @Test
    public void shouldResetMocksSuccessfully() {
        List<?> listMock1 = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> listMock2 = mockitoCore.mock(List.class, new MockSettingsImpl());

        listMock1.add("one");
        listMock2.add("two");

        mockitoCore.reset(listMock1, listMock2);

        mockitoCore.verifyNoMoreInteractions(listMock1, listMock2);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenVerifyNoMoreInteractionsCalledWithNullArray() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenVerifyNoMoreInteractionsCalledWithEmptyArray() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowExceptionWhenVerifyNoMoreInteractionsCalledWithNullElement() {
        mockitoCore.verifyNoMoreInteractions((Object) null);
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenVerifyNoMoreInteractionsCalledWithNonMock() {
        mockitoCore.verifyNoMoreInteractions("not a mock");
    }

    @Test
    public void shouldVerifyNoMoreInteractionsSuccessfullyWhenNoInteractionsOccurred() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(listMock);
    }

    @Test(expected = NoInteractionsWanted.class)
    public void shouldFailVerifyNoMoreInteractionsWhenUnverifiedInteractionsRemain() {
        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        listMock.add("test");
        mockitoCore.verifyNoMoreInteractions(listMock);
    }

    @Test
    public void shouldPassVerifyNoMoreInteractionsAfterAllInteractionsVerified() {
        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        listMock.add("test");
        mockitoCore.verify(listMock, VerificationModeFactory.times(1)).add("test");
        mockitoCore.verifyNoMoreInteractions(listMock);
    }

    @Test
    public void shouldVerifyNoMoreInteractionsInOrderSuccessfully() {
        InOrderContext context = new InOrderContextImpl();
        List<Object> mocks = new ArrayList<Object>();
        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mocks.add(listMock);

        listMock.add("first");
        InOrder inOrder = mockitoCore.inOrder(listMock);
        inOrder.verify(listMock).add("first");

        mockitoCore.verifyNoMoreInteractionsInOrder(mocks, context);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldFailVerifyNoMoreInteractionsInOrderWhenUnverifiedInteractionsRemain() {
        InOrderContext context = new InOrderContextImpl();
        List<Object> mocks = new ArrayList<Object>();
        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        mocks.add(listMock);

        listMock.add("unverified");

        mockitoCore.verifyNoMoreInteractionsInOrder(mocks, context);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenCreatingInOrderWithNullArray() {
        mockitoCore.inOrder((Object[]) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenCreatingInOrderWithEmptyArray() {
        mockitoCore.inOrder(new Object[0]);
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowExceptionWhenCreatingInOrderWithNullElement() {
        mockitoCore.inOrder((Object) null);
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowExceptionWhenCreatingInOrderWithNonMock() {
        mockitoCore.inOrder("not a mock");
    }

    @Test
    public void shouldCreateInOrderSuccessfully() {
        List<?> listMock1 = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> listMock2 = mockitoCore.mock(List.class, new MockSettingsImpl());

        InOrder inOrder = mockitoCore.inOrder(listMock1, listMock2);
        Assert.assertNotNull(inOrder);
    }

    @Test
    public void shouldConfigureDoAnswerSuccessfully() {
        Answer<String> customAnswer = invocation -> "answered";
        Stubber stubber = mockitoCore.doAnswer(customAnswer);
        Assert.assertNotNull(stubber);

        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        stubber.when(listMock).get(0);

        Assert.assertEquals("answered", listMock.get(0));
    }

    @Test
    public void shouldStubVoidMethodSuccessfully() {
        List<?> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        VoidMethodStubbable<?> voidStubbable = mockitoCore.stubVoid(listMock);
        Assert.assertNotNull(voidStubbable);

        voidStubbable.toThrow(new RuntimeException("boom")).on().clear();
        try {
            listMock.clear();
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertEquals("boom", e.getMessage());
        }
    }

    @Test
    public void shouldValidateMockitoUsageWithoutExceptions() {
        mockitoCore.validateMockitoUsage();
    }

    @Test
    public void shouldGetLastInvocation() {
        List<String> listMock = mockitoCore.mock(List.class, new MockSettingsImpl());
        listMock.add("item");

        Invocation lastInvocation = mockitoCore.getLastInvocation();
        Assert.assertNotNull(lastInvocation);
        Assert.assertEquals("add", lastInvocation.getMethod().getName());
        Assert.assertEquals(Arrays.asList("item"), Arrays.asList(lastInvocation.getArguments()));
    }
}
