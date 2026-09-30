package org.mockito.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.*;
import org.mockito.exceptions.verification.*;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.matchers.LocalizedMatcher;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.Location;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ReporterTest {

    private Reporter reporter;

    private static class DummyClass {
        private String someField;

        public void voidMethod() {}
        public String methodWithArgs(String a, int b) { return null; }
        public void varArgsMethod(String first, int... rest) {}
    }

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    private Location dummyLocation() {
        return new LocationImpl();
    }

    private DescribedInvocation createDummyDescribedInvocation(final String description) {
        final Location location = dummyLocation();
        return new DescribedInvocation() {
            @Override
            public String toString() {
                return description;
            }

            @Override
            public Location getLocation() {
                return location;
            }
        };
    }

    private LocalizedMatcher createDummyLocalizedMatcher() {
        return new LocalizedMatcher(new org.mockito.ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                return true;
            }
        });
    }

    private InvocationOnMock createDummyInvocationOnMock(final Method method, final Object mock) {
        return new InvocationOnMock() {
            @Override
            public Object getMock() {
                return mock;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }

            @Override
            public Object callRealMethod() throws Throwable {
                return null;
            }
        };
    }

    @Test(expected = MockitoException.class)
    public void testCheckedExceptionInvalid() {
        reporter.checkedExceptionInvalid(new Exception("Checked"));
    }

    @Test(expected = MockitoException.class)
    public void testCannotStubWithNullThrowable() {
        reporter.cannotStubWithNullThrowable();
    }

    @Test(expected = UnfinishedStubbingException.class)
    public void testUnfinishedStubbing() {
        reporter.unfinishedStubbing(dummyLocation());
    }

    @Test(expected = MockitoException.class)
    public void testIncorrectUseOfApi() {
        reporter.incorrectUseOfApi();
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation() {
        reporter.missingMethodInvocation();
    }

    @Test(expected = UnfinishedVerificationException.class)
    public void testUnfinishedVerificationException() {
        reporter.unfinishedVerificationException(dummyLocation());
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToVerify() {
        reporter.notAMockPassedToVerify(String.class);
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerify() {
        reporter.nullPassedToVerify();
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToWhenMethod() {
        reporter.notAMockPassedToWhenMethod();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod() {
        reporter.nullPassedToWhenMethod();
    }

    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToVerifyNoMoreInteractions() {
        reporter.notAMockPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerifyNoMoreInteractions() {
        reporter.nullPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedWhenCreatingInOrder() {
        reporter.notAMockPassedWhenCreatingInOrder();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedWhenCreatingInOrder() {
        reporter.nullPassedWhenCreatingInOrder();
    }

    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedWhenCreatingInOrder() {
        reporter.mocksHaveToBePassedWhenCreatingInOrder();
    }

    @Test(expected = MockitoException.class)
    public void testInOrderRequiresFamiliarMock() {
        reporter.inOrderRequiresFamiliarMock();
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void testInvalidUseOfMatchers() {
        List<LocalizedMatcher> matchers = Arrays.asList(createDummyLocalizedMatcher(), createDummyLocalizedMatcher());
        reporter.invalidUseOfMatchers(3, matchers);
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void testIncorrectUseOfAdditionalMatchers() {
        List<LocalizedMatcher> matchers = Collections.singletonList(createDummyLocalizedMatcher());
        reporter.incorrectUseOfAdditionalMatchers("and", 2, matchers);
    }

    @Test(expected = CannotVerifyStubOnlyMock.class)
    public void testStubPassedToVerify() {
        reporter.stubPassedToVerify();
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void testReportNoSubMatchersFound() {
        reporter.reportNoSubMatchersFound("and");
    }

    @Test
    public void testArgumentsAreDifferent() {
        try {
            reporter.argumentsAreDifferent("wantedMethod()", "actualMethod()", dummyLocation());
            fail("Expected ArgumentsAreDifferent exception");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("Argument(s) are different"));
        }
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testWantedButNotInvoked() {
        reporter.wantedButNotInvoked(createDummyDescribedInvocation("foo.bar()"));
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testWantedButNotInvokedWithEmptyInvocations() {
        reporter.wantedButNotInvoked(createDummyDescribedInvocation("foo.bar()"), Collections.<DescribedInvocation>emptyList());
    }

    @Test
    public void testWantedButNotInvokedWithNonEmptyInvocations() {
        try {
            List<DescribedInvocation> invocations = Collections.singletonList(createDummyDescribedInvocation("foo.baz()"));
            reporter.wantedButNotInvoked(createDummyDescribedInvocation("foo.bar()"), invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions"));
            assertTrue(e.getMessage().contains("foo.baz()"));
        }
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testWantedButNotInvokedInOrder() {
        reporter.wantedButNotInvokedInOrder(createDummyDescribedInvocation("foo.bar()"), createDummyDescribedInvocation("foo.prev()"));
    }

    @Test(expected = TooManyActualInvocations.class)
    public void testTooManyActualInvocations() {
        reporter.tooManyActualInvocations(1, 2, createDummyDescribedInvocation("foo.bar()"), dummyLocation());
    }

    @Test(expected = NeverWantedButInvoked.class)
    public void testNeverWantedButInvoked() {
        reporter.neverWantedButInvoked(createDummyDescribedInvocation("foo.bar()"), dummyLocation());
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooManyActualInvocationsInOrder() {
        reporter.tooManyActualInvocationsInOrder(1, 2, createDummyDescribedInvocation("foo.bar()"), dummyLocation());
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocationsWithLocation() {
        reporter.tooLittleActualInvocations(new Discrepancy(2, 1), createDummyDescribedInvocation("foo.bar()"), dummyLocation());
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocationsWithNullLocation() {
        reporter.tooLittleActualInvocations(new Discrepancy(2, 0), createDummyDescribedInvocation("foo.bar()"), null);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooLittleActualInvocationsInOrder() {
        reporter.tooLittleActualInvocationsInOrder(new Discrepancy(2, 1), createDummyDescribedInvocation("foo.bar()"), dummyLocation());
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooLittleActualInvocationsInOrderWithNullLocation() {
        reporter.tooLittleActualInvocationsInOrder(new Discrepancy(2, 0), createDummyDescribedInvocation("foo.bar()"), null);
    }

    private static class DummyInvocation implements Invocation, VerificationAwareInvocation {
        private final Location location = new LocationImpl();

        @Override public int getSequenceNumber() { return 0; }
        @Override public Object getMock() { return "mockObject"; }
        @Override public Method getMethod() {
            try {
                return DummyClass.class.getMethod("voidMethod");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        @Override public Object[] getArguments() { return new Object[0]; }
        @Override public <T> T getArgumentAt(int index, Class<T> clazz) { return null; }
        @Override public Object[] getRawArguments() { return new Object[0]; }
        @Override public Class<?>[] getRawReturnType() { return new Class<?>[]{void.class}; }
        @Override public Location getLocation() { return location; }
        @Override public boolean isVerified() { return false; }
        @Override public boolean isIgnoredForVerification() { return false; }
        @Override public void ignoreForVerification() {}
        @Override public void markVerified() {}
        @Override public Object callRealMethod() throws Throwable { return null; }
        @Override public void markStubbed(org.mockito.invocation.StubInfo stubInfo) {}
        @Override public org.mockito.invocation.StubInfo stubInfo() { return null; }
    }

    @Test(expected = NoInteractionsWanted.class)
    public void testNoMoreInteractionsWanted() {
        DummyInvocation dummy = new DummyInvocation();
        List<VerificationAwareInvocation> list = new ArrayList<VerificationAwareInvocation>();
        list.add(dummy);
        reporter.noMoreInteractionsWanted(dummy, list);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testNoMoreInteractionsWantedInOrder() {
        DummyInvocation dummy = new DummyInvocation();
        reporter.noMoreInteractionsWantedInOrder(dummy);
    }

    @Test(expected = MockitoException.class)
    public void testCannotMockFinalClass() {
        reporter.cannotMockFinalClass(String.class);
    }

    @Test(expected = CannotStubVoidMethodWithReturnValue.class)
    public void testCannotStubVoidMethodWithAReturnValue() {
        reporter.cannotStubVoidMethodWithAReturnValue("voidMethod");
    }

    @Test(expected = MockitoException.class)
    public void testOnlyVoidMethodsCanBeSetToDoNothing() {
        reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }

    @Test(expected = WrongTypeOfReturnValue.class)
    public void testWrongTypeOfReturnValue() {
        reporter.wrongTypeOfReturnValue("String", "Integer", "getName");
    }

    @Test(expected = MockitoAssertionError.class)
    public void testWantedAtMostX() {
        reporter.wantedAtMostX(2, 3);
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void testMisplacedArgumentMatcher() {
        reporter.misplacedArgumentMatcher(Collections.singletonList(createDummyLocalizedMatcher()));
    }

    @Test(expected = SmartNullPointerException.class)
    public void testSmartNullPointerException() {
        reporter.smartNullPointerException("someInvocation()", dummyLocation());
    }

    @Test(expected = MockitoException.class)
    public void testNoArgumentValueWasCaptured() {
        reporter.noArgumentValueWasCaptured();
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesDoesNotAcceptNullParameters() {
        reporter.extraInterfacesDoesNotAcceptNullParameters();
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesAcceptsOnlyInterfaces() {
        reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesCannotContainMockedType() {
        reporter.extraInterfacesCannotContainMockedType(List.class);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesRequiresAtLeastOneInterface() {
        reporter.extraInterfacesRequiresAtLeastOneInterface();
    }

    @Test(expected = MockitoException.class)
    public void testMockedTypeIsInconsistentWithSpiedInstanceType() {
        reporter.mockedTypeIsInconsistentWithSpiedInstanceType(List.class, new ArrayList<Object>());
    }

    @Test(expected = MockitoException.class)
    public void testCannotCallAbstractRealMethod() {
        reporter.cannotCallAbstractRealMethod();
    }

    @Test(expected = MockitoException.class)
    public void testCannotVerifyToString() {
        reporter.cannotVerifyToString();
    }

    @Test(expected = MockitoException.class)
    public void testMoreThanOneAnnotationNotAllowed() {
        reporter.moreThanOneAnnotationNotAllowed("myField");
    }

    @Test(expected = MockitoException.class)
    public void testUnsupportedCombinationOfAnnotations() {
        reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
    }

    @Test(expected = MockitoException.class)
    public void testCannotInitializeForSpyAnnotation() {
        reporter.cannotInitializeForSpyAnnotation("myField", new RuntimeException("reason"));
    }

    @Test(expected = MockitoException.class)
    public void testCannotInitializeForInjectMocksAnnotation() {
        reporter.cannotInitializeForInjectMocksAnnotation("myField", new RuntimeException("reason"));
    }

    @Test(expected = FriendlyReminderException.class)
    public void testAtMostAndNeverShouldNotBeUsedWithTimeout() {
        reporter.atMostAndNeverShouldNotBeUsedWithTimeout();
    }

    @Test(expected = MockitoException.class)
    public void testFieldInitialisationThrewException() throws Exception {
        Field field = DummyClass.class.getDeclaredField("someField");
        reporter.fieldInitialisationThrewException(field, new RuntimeException("init failure"));
    }

    @Test(expected = MockitoException.class)
    public void testInvocationListenerDoesNotAcceptNullParameters() {
        reporter.invocationListenerDoesNotAcceptNullParameters();
    }

    @Test(expected = MockitoException.class)
    public void testInvocationListenersRequiresAtLeastOneListener() {
        reporter.invocationListenersRequiresAtLeastOneListener();
    }

    @Test(expected = MockitoException.class)
    public void testInvocationListenerThrewException() {
        InvocationListener listener = new InvocationListener() {
            @Override
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {}
        };
        reporter.invocationListenerThrewException(listener, new RuntimeException("listener failed"));
    }

    @Test(expected = MockitoException.class)
    public void testCannotInjectDependency() throws Exception {
        Field field = DummyClass.class.getDeclaredField("someField");
        Exception details = new Exception("wrapper", new RuntimeException("cause reason"));
        reporter.cannotInjectDependency(field, new Object(), details);
    }

    @Test(expected = MockitoException.class)
    public void testMockedTypeIsInconsistentWithDelegatedInstanceType() {
        reporter.mockedTypeIsInconsistentWithDelegatedInstanceType(List.class, new Object());
    }

    @Test(expected = MockitoException.class)
    public void testSpyAndDelegateAreMutuallyExclusive() {
        reporter.spyAndDelegateAreMutuallyExclusive();
    }

    @Test(expected = MockitoException.class)
    public void testInvalidArgumentRangeAtIdentityAnswerCreationTime() {
        reporter.invalidArgumentRangeAtIdentityAnswerCreationTime();
    }

    @Test
    public void testInvalidArgumentPositionRangeAtInvocationTimeNoArgs() throws Exception {
        Method method = DummyClass.class.getDeclaredMethod("voidMethod");
        InvocationOnMock invocation = createDummyInvocationOnMock(method, new Object());
        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, true, 0);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("the method has no arguments"));
            assertTrue(e.getMessage().contains("Last parameter wanted"));
        }
    }

    @Test
    public void testInvalidArgumentPositionRangeAtInvocationTimeWithArgs() throws Exception {
        Method method = DummyClass.class.getDeclaredMethod("methodWithArgs", String.class, int.class);
        InvocationOnMock invocation = createDummyInvocationOnMock(method, new Object());
        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, false, 5);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Wanted parameter at position 5"));
            assertTrue(e.getMessage().contains("the possible argument indexes for this method are"));
            assertTrue(e.getMessage().contains("[0] String"));
            assertTrue(e.getMessage().contains("[1] int"));
        }
    }

    @Test
    public void testInvalidArgumentPositionRangeAtInvocationTimeWithVarArgs() throws Exception {
        Method method = DummyClass.class.getDeclaredMethod("varArgsMethod", String.class, int[].class);
        InvocationOnMock invocation = createDummyInvocationOnMock(method, new Object());
        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, false, 3);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("<- Vararg"));
            assertTrue(e.getMessage().contains("[1+] int"));
        }
    }

    @Test(expected = WrongTypeOfReturnValue.class)
    public void testWrongTypeOfArgumentToReturn() throws Exception {
        Method method = DummyClass.class.getDeclaredMethod("methodWithArgs", String.class, int.class);
        InvocationOnMock invocation = createDummyInvocationOnMock(method, new Object());
        reporter.wrongTypeOfArgumentToReturn(invocation, "String", Integer.class, 1);
    }

    @Test(expected = MockitoException.class)
    public void testDefaultAnswerDoesNotAcceptNullParameter() {
        reporter.defaultAnswerDoesNotAcceptNullParameter();
    }

    @Test(expected = MockitoException.class)
    public void testSerializableWontWorkForObjectsThatDontImplementSerializable() {
        reporter.serializableWontWorkForObjectsThatDontImplementSerializable(DummyClass.class);
    }

    @Test(expected = MockitoException.class)
    public void testDelegatedMethodHasWrongReturnType() throws Exception {
        Method m1 = DummyClass.class.getDeclaredMethod("voidMethod");
        Method m2 = DummyClass.class.getDeclaredMethod("methodWithArgs", String.class, int.class);
        reporter.delegatedMethodHasWrongReturnType(m1, m2, new Object(), new Object());
    }

    @Test(expected = MockitoException.class)
    public void testDelegatedMethodDoesNotExistOnDelegate() throws Exception {
        Method m1 = DummyClass.class.getDeclaredMethod("voidMethod");
        reporter.delegatedMethodDoesNotExistOnDelegate(m1, new Object(), new Object());
    }

    @Test(expected = MockitoException.class)
    public void testUsingConstructorWithFancySerializable() {
        reporter.usingConstructorWithFancySerializable(SerializableMode.BASIC);
    }
}
