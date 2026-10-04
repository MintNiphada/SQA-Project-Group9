package org.mockito.exceptions;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.invocation.Invocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReporterTest {

    private final Reporter reporter = new Reporter();

    private final PrintableInvocation dummyInvocation = new PrintableInvocation() {
        public Location getLocation() {
            return new Location();
        }

        public String toString() {
            return "mock.someMethod()";
        }
    };

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
        reporter.unfinishedStubbing(new Location());
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation() {
        reporter.missingMethodInvocation();
    }

    @Test(expected = UnfinishedVerificationException.class)
    public void testUnfinishedVerificationException() {
        reporter.unfinishedVerificationException(new Location());
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
        reporter.invalidUseOfMatchers(2, 1);
    }

    @Test
    public void testArgumentsAreDifferent() {
        try {
            reporter.argumentsAreDifferent("wanted()", "actual()", new Location());
            fail();
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("wanted()"));
        }
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testWantedButNotInvokedSingle() {
        reporter.wantedButNotInvoked(dummyInvocation);
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testWantedButNotInvokedWithEmptyList() {
        reporter.wantedButNotInvoked(dummyInvocation, Collections.<PrintableInvocation>emptyList());
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testWantedButNotInvokedWithNonEmptyList() {
        reporter.wantedButNotInvoked(dummyInvocation, Collections.singletonList(dummyInvocation));
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testWantedButNotInvokedInOrder() {
        reporter.wantedButNotInvokedInOrder(dummyInvocation, dummyInvocation);
    }

    @Test(expected = TooManyActualInvocations.class)
    public void testTooManyActualInvocations() {
        reporter.tooManyActualInvocations(1, 2, dummyInvocation, new Location());
    }

    @Test(expected = NeverWantedButInvoked.class)
    public void testNeverWantedButInvoked() {
        reporter.neverWantedButInvoked(dummyInvocation, new Location());
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooManyActualInvocationsInOrder() {
        reporter.tooManyActualInvocationsInOrder(1, 2, dummyInvocation, new Location());
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocationsWithLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocations(discrepancy, dummyInvocation, new Location());
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocationsWithoutLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocations(discrepancy, dummyInvocation, null);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooLittleActualInvocationsInOrderWithLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocationsInOrder(discrepancy, dummyInvocation, new Location());
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testTooLittleActualInvocationsInOrderWithoutLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocationsInOrder(discrepancy, dummyInvocation, null);
    }

    @Test(expected = NoInteractionsWanted.class)
    public void testNoMoreInteractionsWanted() {
        Invocation invocation = Mockito.mock(Invocation.class);
        Mockito.when(invocation.getLocation()).thenReturn(new Location());
        List<VerificationAwareInvocation> list = Collections.singletonList((VerificationAwareInvocation) invocation);
        reporter.noMoreInteractionsWanted(invocation, list);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testNoMoreInteractionsWantedInOrder() {
        Invocation invocation = Mockito.mock(Invocation.class);
        Mockito.when(invocation.getLocation()).thenReturn(new Location());
        reporter.noMoreInteractionsWantedInOrder(invocation);
    }

    @Test(expected = MockitoException.class)
    public void testCannotMockFinalClass() {
        reporter.cannotMockFinalClass(String.class);
    }

    @Test(expected = MockitoException.class)
    public void testCannotStubVoidMethodWithAReturnValue() {
        reporter.cannotStubVoidMethodWithAReturnValue("voidMethod");
    }

    @Test(expected = MockitoException.class)
    public void testOnlyVoidMethodsCanBeSetToDoNothing() {
        reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }

    @Test(expected = WrongTypeOfReturnValue.class)
    public void testWrongTypeOfReturnValue() {
        reporter.wrongTypeOfReturnValue("String", "Integer", "method");
    }

    @Test(expected = MockitoAssertionError.class)
    public void testWantedAtMostX() {
        reporter.wantedAtMostX(1, 2);
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void testMisplacedArgumentMatcher() {
        reporter.misplacedArgumentMatcher(new Location());
    }

    @Test(expected = SmartNullPointerException.class)
    public void testSmartNullPointerException() {
        reporter.smartNullPointerException(new Location());
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
        reporter.mockedTypeIsInconsistentWithSpiedInstanceType(String.class, Integer.valueOf(1));
    }

    @Test(expected = MockitoException.class)
    public void testCannotCallRealMethodOnInterface() {
        reporter.cannotCallRealMethodOnInterface();
    }

    @Test(expected = MockitoException.class)
    public void testCannotVerifyToString() {
        reporter.cannotVerifyToString();
    }

    @Test(expected = MockitoException.class)
    public void testMoreThanOneAnnotationNotAllowed() {
        reporter.moreThanOneAnnotationNotAllowed("field");
    }

    @Test(expected = MockitoException.class)
    public void testUnsupportedCombinationOfAnnotations() {
        reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
    }

    @Test
    public void testCannotInitializeForSpyAnnotation() {
        Exception cause = new RuntimeException("Cause");
        try {
            reporter.cannotInitializeForSpyAnnotation("fieldName", cause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("fieldName"));
            assertNotNull(e.getCause());
        }
    }

    @Test
    public void testCannotInitializeForInjectMocksAnnotation() {
        Exception cause = new RuntimeException("Cause");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("fieldName", cause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("fieldName"));
            assertNotNull(e.getCause());
        }
    }
}
