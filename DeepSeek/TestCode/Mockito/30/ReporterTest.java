package org.mockito.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReporterTest {

    private Reporter reporter = new Reporter();

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCheckedExceptionInvalid() {
        reporter.checkedExceptionInvalid(new Exception("test"));
    }

    @Test
    public void shouldIncludeThrowableInMessageForCheckedExceptionInvalid() {
        Throwable t = new RuntimeException("custom");
        try {
            reporter.checkedExceptionInvalid(t);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("Invalid: " + t.toString()));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotStubWithNullThrowable() {
        reporter.cannotStubWithNullThrowable();
    }

    @Test
    public void shouldContainMessageForCannotStubWithNullThrowable() {
        try {
            reporter.cannotStubWithNullThrowable();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot stub with null throwable!"));
        }
    }

    @Test(expected = UnfinishedStubbingException.class)
    public void shouldThrowUnfinishedStubbingException() {
        Location location = mock(Location.class);
        reporter.unfinishedStubbing(location);
    }

    @Test
    public void shouldIncludeLocationInUnfinishedStubbingMessage() {
        Location location = mock(Location.class);
        when(location.toString()).thenReturn("some location");
        try {
            reporter.unfinishedStubbing(location);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
            assertTrue(e.getMessage().contains("some location"));
        }
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void shouldThrowMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    @Test
    public void shouldContainMessageForMissingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument"));
        }
    }

    @Test(expected = UnfinishedVerificationException.class)
    public void shouldThrowUnfinishedVerificationException() {
        Location location = mock(Location.class);
        reporter.unfinishedVerificationException(location);
    }

    @Test
    public void shouldIncludeLocationInUnfinishedVerificationMessage() {
        Location location = mock(Location.class);
        when(location.toString()).thenReturn("verify location");
        try {
            reporter.unfinishedVerificationException(location);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
            assertTrue(e.getMessage().contains("verify location"));
        }
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowNotAMockExceptionForVerify() {
        reporter.notAMockPassedToVerify(String.class);
    }

    @Test
    public void shouldIncludeTypeNameInNotAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(Integer.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Integer"));
        }
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowNullInsteadOfMockExceptionForVerify() {
        reporter.nullPassedToVerify();
    }

    @Test
    public void shouldContainMessageForNullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() should be a mock but is null!"));
        }
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowNotAMockExceptionForWhen() {
        reporter.notAMockPassedToWhenMethod();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowNullInsteadOfMockExceptionForWhen() {
        reporter.nullPassedToWhenMethod();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForVerifyNoMoreInteractionsNoArgs() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowNotAMockExceptionForVerifyNoMoreInteractions() {
        reporter.notAMockPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowNullInsteadOfMockExceptionForVerifyNoMoreInteractions() {
        reporter.nullPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = NotAMockException.class)
    public void shouldThrowNotAMockExceptionForInOrder() {
        reporter.notAMockPassedWhenCreatingInOrder();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void shouldThrowNullInsteadOfMockExceptionForInOrder() {
        reporter.nullPassedWhenCreatingInOrder();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForInOrderNoArgs() {
        reporter.mocksHaveToBePassedWhenCreatingInOrder();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForInOrderRequiresFamiliarMock() {
        reporter.inOrderRequiresFamiliarMock();
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void shouldThrowInvalidUseOfMatchersException() {
        reporter.invalidUseOfMatchers(2, 1);
    }

    @Test
    public void shouldIncludeCountsInInvalidUseOfMatchersMessage() {
        try {
            reporter.invalidUseOfMatchers(3, 5);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("3 matchers expected, 5 recorded"));
        }
    }

    @Test(expected = ArgumentsAreDifferent.class)
    public void shouldThrowArgumentsAreDifferentWhenJUnitNotPresent() {
        Location location = mock(Location.class);
        reporter.argumentsAreDifferent("wanted", "actual", location);
    }

    @Test
    public void shouldIncludeWantedAndActualInArgumentsAreDifferentMessage() {
        Location location = mock(Location.class);
        when(location.toString()).thenReturn("actual location");
        try {
            reporter.argumentsAreDifferent("wanted", "actual", location);
            fail("Expected ArgumentsAreDifferent");
        } catch (ArgumentsAreDifferent e) {
            assertTrue(e.getMessage().contains("Argument(s) are different! Wanted:"));
            assertTrue(e.getMessage().contains("wanted"));
            assertTrue(e.getMessage().contains("actual"));
        }
    }

    @Test(expected = WantedButNotInvoked.class)
    public void shouldThrowWantedButNotInvokedWithSingleInvocation() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted invocation");
        reporter.wantedButNotInvoked(wanted);
    }

    @Test
    public void shouldIncludeWantedInWantedButNotInvokedMessage() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted invocation");
        try {
            reporter.wantedButNotInvoked(wanted);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("wanted invocation"));
        }
    }

    @Test(expected = WantedButNotInvoked.class)
    public void shouldThrowWantedButNotInvokedWithEmptyInvocations() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        List<PrintableInvocation> invocations = new ArrayList<PrintableInvocation>();
        reporter.wantedButNotInvoked(wanted, invocations);
    }

    @Test
    public void shouldIncludeZeroInteractionsMessageWhenEmptyInvocations() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        List<PrintableInvocation> invocations = new ArrayList<PrintableInvocation>();
        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    @Test(expected = WantedButNotInvoked.class)
    public void shouldThrowWantedButNotInvokedWithNonEmptyInvocations() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        PrintableInvocation other = mock(PrintableInvocation.class);
        when(other.getLocation()).thenReturn("other location");
        List<PrintableInvocation> invocations = new ArrayList<PrintableInvocation>();
        invocations.add(other);
        reporter.wantedButNotInvoked(wanted, invocations);
    }

    @Test
    public void shouldIncludeOtherInvocationsWhenNotEmpty() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        PrintableInvocation other = mock(PrintableInvocation.class);
        when(other.getLocation()).thenReturn("other location");
        List<PrintableInvocation> invocations = new ArrayList<PrintableInvocation>();
        invocations.add(other);
        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
            assertTrue(e.getMessage().contains("other location"));
        }
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldThrowVerificationInOrderFailureForWantedButNotInvokedInOrder() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        PrintableInvocation previous = mock(PrintableInvocation.class);
        when(previous.toString()).thenReturn("previous");
        when(previous.getLocation()).thenReturn("previous location");
        reporter.wantedButNotInvokedInOrder(wanted, previous);
    }

    @Test
    public void shouldIncludeWantedAndPreviousInOrderMessage() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        PrintableInvocation previous = mock(PrintableInvocation.class);
        when(previous.toString()).thenReturn("previous");
        when(previous.getLocation()).thenReturn("previous location");
        try {
            reporter.wantedButNotInvokedInOrder(wanted, previous);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
            assertTrue(e.getMessage().contains("wanted"));
            assertTrue(e.getMessage().contains("previous"));
        }
    }

    @Test(expected = TooManyActualInvocations.class)
    public void shouldThrowTooManyActualInvocations() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        reporter.tooManyActualInvocations(1, 2, wanted, firstUndesired);
    }

    @Test
    public void shouldIncludeCountsInTooManyActualInvocationsMessage() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        try {
            reporter.tooManyActualInvocations(1, 2, wanted, firstUndesired);
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 1 time"));
            assertTrue(e.getMessage().contains("But was 2 times"));
        }
    }

    @Test(expected = NeverWantedButInvoked.class)
    public void shouldThrowNeverWantedButInvoked() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        reporter.neverWantedButInvoked(wanted, firstUndesired);
    }

    @Test
    public void shouldIncludeNeverWantedMessage() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        try {
            reporter.neverWantedButInvoked(wanted, firstUndesired);
            fail("Expected NeverWantedButInvoked");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("But invoked here:"));
        }
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldThrowVerificationInOrderFailureForTooManyInOrder() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        reporter.tooManyActualInvocationsInOrder(1, 2, wanted, firstUndesired);
    }

    @Test
    public void shouldIncludeInOrderMessageForTooManyInOrder() {
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location firstUndesired = mock(Location.class);
        when(firstUndesired.toString()).thenReturn("first undesired");
        try {
            reporter.tooManyActualInvocationsInOrder(1, 2, wanted, firstUndesired);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("wanted"));
        }
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void shouldThrowTooLittleActualInvocations() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("1 time");
        when(discrepancy.getPluralizedActualCount()).thenReturn("0 times");
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location lastActualLocation = mock(Location.class);
        when(lastActualLocation.toString()).thenReturn("last location");
        reporter.tooLittleActualInvocations(discrepancy, wanted, lastActualLocation);
    }

    @Test
    public void shouldIncludeDiscrepancyInTooLittleMessage() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("2 times");
        when(discrepancy.getPluralizedActualCount()).thenReturn("1 time");
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location lastActualLocation = mock(Location.class);
        when(lastActualLocation.toString()).thenReturn("last location");
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, lastActualLocation);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 2 times"));
            assertTrue(e.getMessage().contains("But was 1 time"));
        }
    }

    @Test(expected = TooLittleActualInvocations.class)
    public void shouldThrowTooLittleActualInvocationsWithNullLastLocation() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("1 time");
        when(discrepancy.getPluralizedActualCount()).thenReturn("0 times");
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        reporter.tooLittleActualInvocations(discrepancy, wanted, null);
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldThrowVerificationInOrderFailureForTooLittleInOrder() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("1 time");
        when(discrepancy.getPluralizedActualCount()).thenReturn("0 times");
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location lastActualLocation = mock(Location.class);
        when(lastActualLocation.toString()).thenReturn("last location");
        reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, lastActualLocation);
    }

    @Test
    public void shouldIncludeInOrderMessageForTooLittleInOrder() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("1 time");
        when(discrepancy.getPluralizedActualCount()).thenReturn("0 times");
        PrintableInvocation wanted = mock(PrintableInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        Location lastActualLocation = mock(Location.class);
        when(lastActualLocation.toString()).thenReturn("last location");
        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, lastActualLocation);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
        }
    }

    @Test(expected = NoInteractionsWanted.class)
    public void shouldThrowNoInteractionsWanted() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getLocation()).thenReturn("undesired location");
        List<VerificationAwareInvocation> invocations = new ArrayList<VerificationAwareInvocation>();
        reporter.noMoreInteractionsWanted(undesired, invocations);
    }

    @Test
    public void shouldIncludeScenarioInNoInteractionsWantedMessage() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getLocation()).thenReturn("undesired location");
        VerificationAwareInvocation v = mock(VerificationAwareInvocation.class);
        when(v.toString()).thenReturn("some invocation");
        List<VerificationAwareInvocation> invocations = new ArrayList<VerificationAwareInvocation>();
        invocations.add(v);
        try {
            reporter.noMoreInteractionsWanted(undesired, invocations);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction:"));
        }
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldThrowVerificationInOrderFailureForNoMoreInteractionsInOrder() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getLocation()).thenReturn("undesired location");
        reporter.noMoreInteractionsWantedInOrder(undesired);
    }

    @Test
    public void shouldIncludeMessageForNoMoreInteractionsInOrder() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getLocation()).thenReturn("undesired location");
        try {
            reporter.noMoreInteractionsWantedInOrder(undesired);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotMockFinalClass() {
        reporter.cannotMockFinalClass(String.class);
    }

    @Test
    public void shouldIncludeClassNameInCannotMockFinalClassMessage() {
        try {
            reporter.cannotMockFinalClass(Integer.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("class java.lang.Integer"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotStubVoidMethodWithAReturnValue() {
        reporter.cannotStubVoidMethodWithAReturnValue("someMethod");
    }

    @Test
    public void shouldIncludeMethodNameInCannotStubVoidMessage() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("myVoidMethod");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("myVoidMethod"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForOnlyVoidMethodsCanBeSetToDoNothing() {
        reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }

    @Test(expected = WrongTypeOfReturnValue.class)
    public void shouldThrowWrongTypeOfReturnValue() {
        reporter.wrongTypeOfReturnValue("String", "Integer", "getValue");
    }

    @Test
    public void shouldIncludeTypesInWrongTypeOfReturnValueMessage() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "getValue");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("Integer cannot be returned by getValue()"));
            assertTrue(e.getMessage().contains("getValue() should return String"));
        }
    }

    @Test(expected = MockitoAssertionError.class)
    public void shouldThrowMockitoAssertionErrorForWantedAtMostX() {
        reporter.wantedAtMostX(2, 3);
    }

    @Test
    public void shouldIncludeCountsInWantedAtMostXMessage() {
        try {
            reporter.wantedAtMostX(2, 3);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 3"));
        }
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void shouldThrowInvalidUseOfMatchersExceptionForMisplacedArgumentMatcher() {
        Location location = mock(Location.class);
        reporter.misplacedArgumentMatcher(location);
    }

    @Test
    public void shouldIncludeLocationInMisplacedArgumentMatcherMessage() {
        Location location = mock(Location.class);
        when(location.toString()).thenReturn("some location");
        try {
            reporter.misplacedArgumentMatcher(location);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Misplaced argument matcher detected here:"));
            assertTrue(e.getMessage().contains("some location"));
        }
    }

    @Test(expected = SmartNullPointerException.class)
    public void shouldThrowSmartNullPointerException() {
        Location location = mock(Location.class);
        reporter.smartNullPointerException(location);
    }

    @Test
    public void shouldIncludeLocationInSmartNullPointerExceptionMessage() {
        Location location = mock(Location.class);
        when(location.toString()).thenReturn("stub location");
        try {
            reporter.smartNullPointerException(location);
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
            assertTrue(e.getMessage().contains("Because this method was *not* stubbed correctly:"));
            assertTrue(e.getMessage().contains("stub location"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForNoArgumentValueWasCaptured() {
        reporter.noArgumentValueWasCaptured();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForExtraInterfacesDoesNotAcceptNullParameters() {
        reporter.extraInterfacesDoesNotAcceptNullParameters();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForExtraInterfacesAcceptsOnlyInterfaces() {
        reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
    }

    @Test
    public void shouldIncludeWrongTypeInExtraInterfacesAcceptsOnlyInterfacesMessage() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(Integer.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Integer"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForExtraInterfacesCannotContainMockedType() {
        reporter.extraInterfacesCannotContainMockedType(List.class);
    }

    @Test
    public void shouldIncludeMockedTypeInExtraInterfacesCannotContainMockedTypeMessage() {
        try {
            reporter.extraInterfacesCannotContainMockedType(Comparable.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Comparable"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForExtraInterfacesRequiresAtLeastOneInterface() {
        reporter.extraInterfacesRequiresAtLeastOneInterface();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForMockedTypeIsInconsistentWithSpiedInstanceType() {
        reporter.mockedTypeIsInconsistentWithSpiedInstanceType(String.class, new Integer(5));
    }

    @Test
    public void shouldIncludeTypesInMockedTypeIsInconsistentMessage() {
        try {
            reporter.mockedTypeIsInconsistentWithSpiedInstanceType(String.class, new Integer(5));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be: Integer, but is: String"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotCallRealMethodOnInterface() {
        reporter.cannotCallRealMethodOnInterface();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotVerifyToString() {
        reporter.cannotVerifyToString();
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForMoreThanOneAnnotationNotAllowed() {
        reporter.moreThanOneAnnotationNotAllowed("fieldName");
    }

    @Test
    public void shouldIncludeFieldNameInMoreThanOneAnnotationMessage() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("myField");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("myField"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForUnsupportedCombinationOfAnnotations() {
        reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
    }

    @Test
    public void shouldIncludeAnnotationNamesInUnsupportedCombinationMessage() {
        try {
            reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("@Mock and @Spy"));
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotInitializeForSpyAnnotation() {
        Exception details = new Exception("detail message");
        reporter.cannotInitializeForSpyAnnotation("field", details);
    }

    @Test
    public void shouldIncludeFieldNameAndCauseInCannotInitializeForSpyAnnotation() {
        Exception details = new Exception("detail message");
        try {
            reporter.cannotInitializeForSpyAnnotation("myField", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("myField"));
            assertTrue(e.getMessage().contains("detail message"));
            assertEquals(details, e.getCause());
        }
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionForCannotInitializeForInjectMocksAnnotation() {
        Exception details = new Exception("inject detail");
        reporter.cannotInitializeForInjectMocksAnnotation("field", details);
    }

    @Test
    public void shouldIncludeFieldNameAndCauseInCannotInitializeForInjectMocksAnnotation() {
        Exception details = new Exception("inject detail");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("myField", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("myField"));
            assertTrue(e.getMessage().contains("inject detail"));
            assertEquals(details, e.getCause());
        }
    }
}
