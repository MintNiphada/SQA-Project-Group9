package org.mockito.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MockitoMethod;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

public class MockHandlerTest {

    private Invocation createInvocation(final Object mock, final String methodName, final Object[] args) throws Exception {
        final Method method = Object.class.getMethod("toString");
        MockitoMethod mockitoMethod = new MockitoMethod() {
            public String getName() {
                return methodName;
            }

            public Class<?> getReturnType() {
                return Object.class;
            }

            public Class<?>[] getParameterTypes() {
                return new Class<?>[0];
            }

            public Class<?>[] getExceptionTypes() {
                return new Class<?>[0];
            }

            public boolean isVarArgs() {
                return false;
            }

            public Method getJavaMethod() {
                return method;
            }
        };
        RealMethod realMethod = new RealMethod() {
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return null;
            }
        };
        return new Invocation(mock, mockitoMethod, args, 1, realMethod);
    }

    @Test
    public void testDefaultConstructor() {
        MockHandler<Object> handler = new MockHandler<Object>();
        Assert.assertNotNull(handler.getMockSettings());
        Assert.assertNotNull(handler.getInvocationContainer());
        Assert.assertNotNull(handler.matchersBinder);
        Assert.assertNotNull(handler.mockingProgress);
    }

    @Test
    public void testConstructorWithSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<String> handler = new MockHandler<String>(settings);
        Assert.assertSame(settings, handler.getMockSettings());
        Assert.assertNotNull(handler.getInvocationContainer());
    }

    @Test
    public void testConstructorWithOldMockHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<String> oldHandler = new MockHandler<String>(settings);
        MockHandler<String> newHandler = new MockHandler<String>(oldHandler);
        Assert.assertSame(settings, newHandler.getMockSettings());
        Assert.assertNotNull(newHandler.getInvocationContainer());
    }

    @Test
    public void testVoidMethodStubbable() {
        MockHandler<String> handler = new MockHandler<String>();
        String mockInstance = "mock";
        VoidMethodStubbable<String> stubbable = handler.voidMethodStubbable(mockInstance);
        Assert.assertNotNull(stubbable);
    }

    @Test
    public void testSetAnswersForStubbingAndHandleStubbingMode() throws Throwable {
        MockHandler<String> handler = new MockHandler<String>();
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("firstResult"));
        handler.setAnswersForStubbing(answers);

        Invocation invocation = createInvocation("myMock", "toString", new Object[0]);
        Object result = handler.handle(invocation);

        Assert.assertNull(result);
        Assert.assertFalse(handler.invocationContainerImpl.hasAnswersForStubbing());

        Object secondResult = handler.handle(invocation);
        Assert.assertEquals("firstResult", secondResult);
    }

    @Test
    public void testHandleDefaultAnswerWhenNotStubbed() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("defaultResult"));
        MockHandler<String> handler = new MockHandler<String>(settings);

        Invocation invocation = createInvocation("myMock", "toString", new Object[0]);
        Object result = handler.handle(invocation);

        Assert.assertEquals("defaultResult", result);
    }

    @Test
    public void testHandleWithPreExistingStubbedInvocation() throws Throwable {
        MockHandler<String> handler = new MockHandler<String>();
        Invocation invocation = createInvocation("myMock", "toString", new Object[0]);

        handler.invocationContainerImpl.setInvocationForPotentialStubbing(new InvocationMatcher(invocation));
        handler.invocationContainerImpl.addAnswer(new Returns("stubbedReturnValue"));

        Object result = handler.handle(invocation);
        Assert.assertEquals("stubbedReturnValue", result);
    }

    @Test
    public void testHandleWithMockAwareVerificationModeOnSameMock() throws Throwable {
        final boolean[] verified = new boolean[]{false};
        VerificationMode mode = new VerificationMode() {
            public void verify(VerificationData data) {
                verified[0] = true;
            }
        };

        String mockObject = "mockObject";
        MockAwareVerificationMode mockAwareMode = new MockAwareVerificationMode(mockObject, mode);

        MockHandler<String> handler = new MockHandler<String>();
        handler.mockingProgress.verificationStarted(mockAwareMode);

        Invocation invocation = createInvocation(mockObject, "toString", new Object[0]);
        Object result = handler.handle(invocation);

        Assert.assertNull(result);
        Assert.assertTrue(verified[0]);
    }

    @Test
    public void testHandleWithMockAwareVerificationModeOnDifferentMock() throws Throwable {
        final boolean[] verified = new boolean[]{false};
        VerificationMode mode = new VerificationMode() {
            public void verify(VerificationData data) {
                verified[0] = true;
            }
        };

        String otherMock = "otherMock";
        String targetMock = "targetMock";
        MockAwareVerificationMode mockAwareMode = new MockAwareVerificationMode(otherMock, mode);

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("defaultVal"));
        MockHandler<String> handler = new MockHandler<String>(settings);
        handler.mockingProgress.verificationStarted(mockAwareMode);

        Invocation invocation = createInvocation(targetMock, "toString", new Object[0]);
        Object result = handler.handle(invocation);

        Assert.assertFalse(verified[0]);
        Assert.assertEquals("defaultVal", result);
    }

    @Test
    public void testHandleWithPlainVerificationMode() throws Throwable {
        final boolean[] verified = new boolean[]{false};
        VerificationMode mode = new VerificationMode() {
            public void verify(VerificationData data) {
                verified[0] = true;
            }
        };

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("plainModeVal"));
        MockHandler<String> handler = new MockHandler<String>(settings);
        handler.mockingProgress.verificationStarted(mode);

        Invocation invocation = createInvocation("myMock", "toString", new Object[0]);
        Object result = handler.handle(invocation);

        Assert.assertFalse(verified[0]);
        Assert.assertEquals("plainModeVal", result);
    }

    @Test
    public void testHandlePropagatesExceptionFromAnswer() throws Throwable {
        MockHandler<String> handler = new MockHandler<String>();
        Invocation invocation = createInvocation("myMock", "toString", new Object[0]);

        final RuntimeException expectedException = new RuntimeException("Stub error");
        Answer<Object> throwingAnswer = new Answer<Object>() {
            public Object answer(org.mockito.invocation.InvocationOnMock inv) throws Throwable {
                throw expectedException;
            }
        };

        handler.setAnswersForStubbing(Collections.<Answer>singletonList(throwingAnswer));
        handler.handle(invocation);

        try {
            handler.handle(invocation);
            Assert.fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            Assert.assertSame(expectedException, e);
        }
    }

    @Test
    public void testGetInvocationContainer() {
        MockHandler<Object> handler = new MockHandler<Object>();
        InvocationContainer container = handler.getInvocationContainer();
        Assert.assertNotNull(container);
        Assert.assertSame(handler.invocationContainerImpl, container);
    }
}
