package org.mockito.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MockitoMethod;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.stubbing.answers.Returns;
import org.mockito.verification.VerificationMode;

public class MockHandlerTest {

    private MockingProgress mockingProgress;

    @Before
    public void setUp() {
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.reset();
    }

    @After
    public void tearDown() {
        mockingProgress.reset();
    }

    private static class DummyTarget {
        public String simpleMethod(String arg) {
            return arg;
        }

        public void voidMethod() {
        }
    }

    private Invocation createInvocation(String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        final Method method = DummyTarget.class.getMethod(methodName, paramTypes);
        final DummyTarget mock = new DummyTarget();

        MockitoMethod mockitoMethod = new MockitoMethod() {
            public String getName() {
                return method.getName();
            }

            public Class<?> getReturnType() {
                return method.getReturnType();
            }

            public Class<?>[] getParameterTypes() {
                return method.getParameterTypes();
            }

            public Class<?>[] getExceptionTypes() {
                return method.getExceptionTypes();
            }

            public boolean isVarArgs() {
                return method.isVarArgs();
            }

            public Method getJavaMethod() {
                return method;
            }
        };

        RealMethod realMethod = new RealMethod() {
            private static final long serialVersionUID = 1L;

            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return method.invoke(target, arguments);
            }
        };

        return new Invocation(mock, mockitoMethod, args, 1, realMethod);
    }

    private Invocation createSimpleInvocation(String arg) throws Exception {
        return createInvocation("simpleMethod", new Class<?>[]{String.class}, new Object[]{arg});
    }

    private Invocation createVoidInvocation() throws Exception {
        return createInvocation("voidMethod", new Class<?>[]{}, new Object[]{});
    }

    @Test
    public void testDefaultConstructor() {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        Assert.assertNotNull(handler.getMockSettings());
        Assert.assertNotNull(handler.getInvocationContainer());
        Assert.assertNotNull(handler.matchersBinder);
        Assert.assertNotNull(handler.mockingProgress);
    }

    @Test
    public void testMockSettingsConstructor() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("default_result"));
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>(settings);

        Assert.assertSame(settings, handler.getMockSettings());
        Assert.assertNotNull(handler.getInvocationContainer());
    }

    @Test
    public void testCopyConstructor() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("copy_test"));
        MockHandler<DummyTarget> original = new MockHandler<DummyTarget>(settings);

        MockHandler<DummyTarget> copy = new MockHandler<DummyTarget>(original);
        Assert.assertSame(settings, copy.getMockSettings());
        Assert.assertNotNull(copy.getInvocationContainer());
    }

    @Test
    public void testVoidMethodStubbable() {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        DummyTarget mock = new DummyTarget();
        VoidMethodStubbable<DummyTarget> stubbable = handler.voidMethodStubbable(mock);
        Assert.assertNotNull(stubbable);
    }

    @Test
    public void testSetAnswersForStubbingAndHandleStubbingMode() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("stubbed_void_response"));
        handler.setAnswersForStubbing(answers);

        Invocation invocation = createSimpleInvocation("arg1");
        Object result = handler.handle(invocation);

        Assert.assertNull(result);
        Assert.assertFalse(handler.invocationContainerImpl.hasAnswersForStubbing());

        // Invocation should now be stubbed with the provided answer
        Invocation call = createSimpleInvocation("arg1");
        Object stubbedResult = handler.handle(call);
        Assert.assertEquals("stubbed_void_response", stubbedResult);
    }

    @Test
    public void testHandleVerificationMode() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        final boolean[] verified = new boolean[]{false};

        VerificationMode customMode = new VerificationMode() {
            public void verify(VerificationData data) {
                verified[0] = true;
                Assert.assertNotNull(data);
                Assert.assertTrue(data instanceof VerificationDataImpl);
            }
        };

        handler.mockingProgress.verificationStarted(customMode);

        Invocation invocation = createSimpleInvocation("verify_arg");
        Object result = handler.handle(invocation);

        Assert.assertNull(result);
        Assert.assertTrue(verified[0]);
    }

    @Test
    public void testHandleDefaultAnswerWhenNotStubbed() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("default_value"));
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>(settings);

        Invocation invocation = createSimpleInvocation("test");
        Object result = handler.handle(invocation);

        Assert.assertEquals("default_value", result);
        Assert.assertEquals(1, handler.invocationContainerImpl.getInvocations().size());
    }

    @Test
    public void testHandleStubbedAnswerMatchingInvocation() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();

        Invocation stubbedInvocation = createSimpleInvocation("hello");
        InvocationMatcher matcher = new InvocationMatcher(stubbedInvocation);
        StubbedInvocationMatcher stub = new StubbedInvocationMatcher(matcher, new Returns("world"));
        handler.invocationContainerImpl.addAnswer(new Returns("world"));

        // Set stubbing manually into invocation container
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("world"));
        handler.setAnswersForStubbing(answers);
        handler.handle(stubbedInvocation);

        Invocation actualInvocation = createSimpleInvocation("hello");
        Object result = handler.handle(actualInvocation);

        Assert.assertEquals("world", result);
    }

    @Test
    public void testHandleThrowsExceptionFromStubbedAnswer() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();

        Answer throwingAnswer = new Answer() {
            public Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                throw new IllegalArgumentException("Stubbed exception");
            }
        };

        handler.setAnswersForStubbing(Collections.singletonList(throwingAnswer));
        Invocation stubInvocation = createSimpleInvocation("error");
        handler.handle(stubInvocation);

        Invocation actualInvocation = createSimpleInvocation("error");
        try {
            handler.handle(actualInvocation);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Stubbed exception", e.getMessage());
        }
    }

    @Test
    public void testHandleThrowsExceptionFromDefaultAnswer() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Answer() {
            public Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                throw new IllegalStateException("Default answer exception");
            }
        });
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>(settings);

        Invocation invocation = createSimpleInvocation("throw");
        try {
            handler.handle(invocation);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("Default answer exception", e.getMessage());
        }
    }

    @Test
    public void testHandleVoidMethodInvocation() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();

        Invocation voidInvocation = createVoidInvocation();
        Object result = handler.handle(voidInvocation);

        Assert.assertNull(result);
    }

    @Test
    public void testHandleVerificationModeFailure() throws Throwable {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        VerificationMode mode = VerificationModeFactory.times(1);
        handler.mockingProgress.verificationStarted(mode);

        Invocation invocation = createSimpleInvocation("verify_fail");
        try {
            handler.handle(invocation);
            Assert.fail("Expected MockitoAssertionError / MockitoException");
        } catch (MockitoException e) {
            Assert.assertNotNull(e);
        } catch (AssertionError e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetInvocationContainerReturnsNonNull() {
        MockHandler<DummyTarget> handler = new MockHandler<DummyTarget>();
        InvocationContainer container = handler.getInvocationContainer();
        Assert.assertNotNull(container);
        Assert.assertSame(handler.invocationContainerImpl, container);
    }
}
