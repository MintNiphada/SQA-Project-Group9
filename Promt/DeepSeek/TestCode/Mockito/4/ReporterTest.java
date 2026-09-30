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
import org.mockito.mock.MockName;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReporterTest {

    private Reporter reporter;

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    @Test
    public void checkedExceptionInvalid() {
        Throwable t = new RuntimeException("test");
        try {
            reporter.checkedExceptionInvalid(t);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("Invalid: " + t));
        }
    }

    @Test
    public void cannotStubWithNullThrowable() {
        try {
            reporter.cannotStubWithNullThrowable();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot stub with null throwable!"));
        }
    }

    @Test
    public void unfinishedStubbing() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedStubbing(location);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Unfinished stubbing detected here:"));
            assertTrue(msg.contains(location.toString()));
            assertTrue(msg.contains("E.g. thenReturn() may be missing."));
        }
    }

    @Test
    public void incorrectUseOfApi() {
        try {
            reporter.incorrectUseOfApi();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Incorrect use of API detected here:"));
            assertTrue(e.getMessage().contains("You probably stored a reference to OngoingStubbing"));
        }
    }

    @Test
    public void missingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument which has to be 'a method call on a mock'."));
        }
    }

    @Test
    public void unfinishedVerificationException() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedVerificationException(location);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
            assertTrue(e.getMessage().contains(location.toString()));
        }
    }

    @Test
    public void notAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    @Test
    public void nullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() should be a mock but is null!"));
        }
    }

    @Test
    public void notAMockPassedToWhenMethod() {
        try {
            reporter.notAMockPassedToWhenMethod();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is not a mock!"));
        }
    }

    @Test
    public void nullPassedToWhenMethod() {
        try {
            reporter.nullPassedToWhenMethod();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is null!"));
        }
    }

    @Test
    public void mocksHaveToBePassedToVerifyNoMoreInteractions() {
        try {
            reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
        }
    }

    @Test
    public void notAMockPassedToVerifyNoMoreInteractions() {
        try {
            reporter.notAMockPassedToVerifyNoMoreInteractions();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    @Test
    public void nullPassedToVerifyNoMoreInteractions() {
        try {
            reporter.nullPassedToVerifyNoMoreInteractions();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test
    public void notAMockPassedWhenCreatingInOrder() {
        try {
            reporter.notAMockPassedWhenCreatingInOrder();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    @Test
    public void nullPassedWhenCreatingInOrder() {
        try {
            reporter.nullPassedWhenCreatingInOrder();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test
    public void mocksHaveToBePassedWhenCreatingInOrder() {
        try {
            reporter.mocksHaveToBePassedWhenCreatingInOrder();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
        }
    }

    @Test
    public void inOrderRequiresFamiliarMock() {
        try {
            reporter.inOrderRequiresFamiliarMock();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InOrder can only verify mocks that were passed in during creation of InOrder."));
        }
    }

    @Test
    public void invalidUseOfMatchers() {
        List<LocalizedMatcher> matchers = new ArrayList<LocalizedMatcher>();
        LocalizedMatcher matcher = mock(LocalizedMatcher.class);
        when(matcher.getLocation()).thenReturn(new LocationImpl());
        matchers.add(matcher);
        try {
            reporter.invalidUseOfMatchers(2, matchers);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers!"));
            assertTrue(e.getMessage().contains("2 matchers expected, 1 recorded"));
        }
    }

    @Test
    public void incorrectUseOfAdditionalMatchers() {
        Collection<LocalizedMatcher> matcherStack = new ArrayList<LocalizedMatcher>();
        LocalizedMatcher matcher = mock(LocalizedMatcher.class);
        when(matcher.getLocation()).thenReturn(new LocationImpl());
        matcherStack.add(matcher);
        try {
            reporter.incorrectUseOfAdditionalMatchers("and", 2, matcherStack);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers inside additional matcher and !"));
            assertTrue(e.getMessage().contains("2 sub matchers expected, 1 recorded"));
        }
    }

    @Test
    public void stubPassedToVerify() {
        try {
            reporter.stubPassedToVerify();
            fail("Expected CannotVerifyStubOnlyMock");
        } catch (CannotVerifyStubOnlyMock e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is a stubOnly() mock"));
        }
    }

    @Test
    public void reportNoSubMatchersFound() {
        try {
            reporter.reportNoSubMatchersFound("and");
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("No matchers found for additional matcher and"));
        }
    }

    @Test
    public void argumentsAreDifferent() {
        Location actualLocation = new LocationImpl();
        try {
            reporter.argumentsAreDifferent("wanted", "actual", actualLocation);
            fail("Expected ArgumentsAreDifferent");
        } catch (ArgumentsAreDifferent e) {
            assertTrue(e.getMessage().contains("Argument(s) are different! Wanted:"));
            assertTrue(e.getMessage().contains("wanted"));
            assertTrue(e.getMessage().contains("actual"));
        }
    }

    @Test
    public void wantedButNotInvokedSingle() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedInvocation");
        try {
            reporter.wantedButNotInvoked(wanted);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("wantedInvocation"));
        }
    }

    @Test
    public void wantedButNotInvokedWithEmptyInvocations() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedInvocation");
        List<DescribedInvocation> invocations = Collections.emptyList();
        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Wanted but not invoked:"));
            assertTrue(msg.contains("wantedInvocation"));
            assertTrue(msg.contains("Actually, there were zero interactions with this mock."));
        }
    }

    @Test
    public void wantedButNotInvokedWithNonEmptyInvocations() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedInvocation");
        DescribedInvocation other = mock(DescribedInvocation.class);
        when(other.toString()).thenReturn("otherInvocation");
        when(other.getLocation()).thenReturn(new LocationImpl());
        List<DescribedInvocation> invocations = Arrays.asList(other);
        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Wanted but not invoked:"));
            assertTrue(msg.contains("wantedInvocation"));
            assertTrue(msg.contains("However, there were other interactions with this mock:"));
            assertTrue(msg.contains("otherInvocation"));
        }
    }

    @Test
    public void wantedButNotInvokedInOrder() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wanted");
        DescribedInvocation previous = mock(DescribedInvocation.class);
        when(previous.toString()).thenReturn("previous");
        when(previous.getLocation()).thenReturn(new LocationImpl());
        try {
            reporter.wantedButNotInvokedInOrder(wanted, previous);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("wanted"));
            assertTrue(e.getMessage().contains("Wanted anywhere AFTER following interaction:"));
            assertTrue(e.getMessage().contains("previous"));
        }
    }

    @Test
    public void tooManyActualInvocations() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedMethod");
        Location firstUndesired = new LocationImpl();
        try {
            reporter.tooManyActualInvocations(1, 2, wanted, firstUndesired);
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("wantedMethod"));
            assertTrue(e.getMessage().contains("Wanted 1 time"));
            assertTrue(e.getMessage().contains("But was 2 times"));
        }
    }

    @Test
    public void neverWantedButInvoked() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedMethod");
        Location firstUndesired = new LocationImpl();
        try {
            reporter.neverWantedButInvoked(wanted, firstUndesired);
            fail("Expected NeverWantedButInvoked");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("wantedMethod"));
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("But invoked here:"));
        }
    }

    @Test
    public void tooManyActualInvocationsInOrder() {
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedMethod");
        Location firstUndesired = new LocationImpl();
        try {
            reporter.tooManyActualInvocationsInOrder(1, 2, wanted, firstUndesired);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("wantedMethod"));
        }
    }

    @Test
    public void tooLittleActualInvocations() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("2 times");
        when(discrepancy.getPluralizedActualCount()).thenReturn("1 time");
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedMethod");
        Location lastActualLocation = new LocationImpl();
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, lastActualLocation);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("wantedMethod"));
            assertTrue(e.getMessage().contains("Wanted 2 times"));
            assertTrue(e.getMessage().contains("But was 1 time"));
        }
    }

    @Test
    public void tooLittleActualInvocationsInOrder() {
        Discrepancy discrepancy = mock(Discrepancy.class);
        when(discrepancy.getPluralizedWantedCount()).thenReturn("2 times");
        when(discrepancy.getPluralizedActualCount()).thenReturn("1 time");
        DescribedInvocation wanted = mock(DescribedInvocation.class);
        when(wanted.toString()).thenReturn("wantedMethod");
        Location lastActualLocation = new LocationImpl();
        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, lastActualLocation);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("wantedMethod"));
        }
    }

    @Test
    public void noMoreInteractionsWanted() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getMock()).thenReturn("mockObject");
        when(undesired.getLocation()).thenReturn(new LocationImpl());
        List<VerificationAwareInvocation> invocations = new ArrayList<VerificationAwareInvocation>();
        VerificationAwareInvocation v = mock(VerificationAwareInvocation.class);
        when(v.getLocation()).thenReturn(new LocationImpl());
        when(v.toString()).thenReturn("someInvocation");
        invocations.add(v);
        try {
            reporter.noMoreInteractionsWanted(undesired, invocations);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction on mock 'mockObject':"));
        }
    }

    @Test
    public void noMoreInteractionsWantedInOrder() {
        Invocation undesired = mock(Invocation.class);
        when(undesired.getMock()).thenReturn("mockObject");
        when(undesired.getLocation()).thenReturn(new LocationImpl());
        try {
            reporter.noMoreInteractionsWantedInOrder(undesired);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction on mock 'mockObject':"));
        }
    }

    @Test
    public void cannotMockFinalClass() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
            assertTrue(e.getMessage().contains("final classes"));
        }
    }

    @Test
    public void cannotStubVoidMethodWithAReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("someMethod");
            fail("Expected CannotStubVoidMethodWithReturnValue");
        } catch (CannotStubVoidMethodWithReturnValue e) {
            assertTrue(e.getMessage().contains("'someMethod' is a *void method*"));
        }
    }

    @Test
    public void onlyVoidMethodsCanBeSetToDoNothing() {
        try {
            reporter.onlyVoidMethodsCanBeSetToDoNothing();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Only void methods can doNothing()!"));
        }
    }

    @Test
    public void wrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "someMethod");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("Integer cannot be returned by someMethod()"));
            assertTrue(e.getMessage().contains("someMethod() should return String"));
        }
    }

    @Test
    public void wantedAtMostX() {
        try {
            reporter.wantedAtMostX(3, 5);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 3 times but was 5"));
        }
    }

    @Test
    public void misplacedArgumentMatcher() {
        List<LocalizedMatcher> matchers = new ArrayList<LocalizedMatcher>();
        LocalizedMatcher matcher = mock(LocalizedMatcher.class);
        when(matcher.getLocation()).thenReturn(new LocationImpl());
        matchers.add(matcher);
        try {
            reporter.misplacedArgumentMatcher(matchers);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Misplaced argument matcher detected here:"));
        }
    }

    @Test
    public void smartNullPointerException() {
        Location location = new LocationImpl();
        try {
            reporter.smartNullPointerException("invocation", location);
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
            assertTrue(e.getMessage().contains("because this method call was *not* stubbed correctly:"));
        }
    }

    @Test
    public void noArgumentValueWasCaptured() {
        try {
            reporter.noArgumentValueWasCaptured();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("No argument value was captured!"));
        }
    }

    @Test
    public void extraInterfacesDoesNotAcceptNullParameters() {
        try {
            reporter.extraInterfacesDoesNotAcceptNullParameters();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept null parameters."));
        }
    }

    @Test
    public void extraInterfacesAcceptsOnlyInterfaces() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() accepts only interfaces."));
            assertTrue(e.getMessage().contains("String"));
        }
    }

    @Test
    public void extraInterfacesCannotContainMockedType() {
        try {
            reporter.extraInterfacesCannotContainMockedType(List.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept the same type as the mocked type."));
            assertTrue(e.getMessage().contains("List"));
        }
    }

    @Test
    public void extraInterfacesRequiresAtLeastOneInterface() {
        try {
            reporter.extraInterfacesRequiresAtLeastOneInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() requires at least one interface."));
        }
    }

    @Test
    public void mockedTypeIsInconsistentWithSpiedInstanceType() {
        try {
            reporter.mockedTypeIsInconsistentWithSpiedInstanceType(String.class, new ArrayList());
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be the same as the type of your spied instance."));
            assertTrue(e.getMessage().contains("ArrayList"));
            assertTrue(e.getMessage().contains("String"));
        }
    }

    @Test
    public void cannotCallAbstractRealMethod() {
        try {
            reporter.cannotCallAbstractRealMethod();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot call abstract real method on java object!"));
        }
    }

    @Test
    public void cannotVerifyToString() {
        try {
            reporter.cannotVerifyToString();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito cannot verify toString()"));
        }
    }

    @Test
    public void moreThanOneAnnotationNotAllowed() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("fieldName");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("You cannot have more than one Mockito annotation on a field!"));
            assertTrue(e.getMessage().contains("fieldName"));
        }
    }

    @Test
    public void unsupportedCombinationOfAnnotations() {
        try {
            reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("This combination of annotations is not permitted on a single field:"));
            assertTrue(e.getMessage().contains("@Mock and @Spy"));
        }
    }

    @Test
    public void cannotInitializeForSpyAnnotation() {
        Exception details = new RuntimeException("construction failed");
        try {
            reporter.cannotInitializeForSpyAnnotation("fieldName", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate a @Spy for 'fieldName' field."));
            assertTrue(e.getMessage().contains("construction failed"));
        }
    }

    @Test
    public void cannotInitializeForInjectMocksAnnotation() {
        Exception details = new RuntimeException("construction failed");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("fieldName", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'fieldName'."));
            assertTrue(e.getMessage().contains("construction failed"));
        }
    }

    @Test
    public void atMostAndNeverShouldNotBeUsedWithTimeout() {
        try {
            reporter.atMostAndNeverShouldNotBeUsedWithTimeout();
            fail("Expected FriendlyReminderException");
        } catch (FriendlyReminderException e) {
            assertTrue(e.getMessage().contains("Don't panic! I'm just a friendly reminder!"));
        }
    }

    @Test
    public void fieldInitialisationThrewException() throws NoSuchFieldException {
        Field field = ReporterTest.class.getDeclaredField("reporter"); // any field
        Throwable details = new RuntimeException("init error");
        try {
            reporter.fieldInitialisationThrewException(field, details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'reporter'"));
            assertTrue(e.getMessage().contains("init error"));
        }
    }

    @Test
    public void invocationListenerDoesNotAcceptNullParameters() {
        try {
            reporter.invocationListenerDoesNotAcceptNullParameters();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertEquals("invocationListeners() does not accept null parameters", e.getMessage());
        }
    }

    @Test
    public void invocationListenersRequiresAtLeastOneListener() {
        try {
            reporter.invocationListenersRequiresAtLeastOneListener();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertEquals("invocationListeners() requires at least one listener", e.getMessage());
        }
    }

    @Test
    public void invocationListenerThrewException() {
        InvocationListener listener = mock(InvocationListener.class);
        Throwable listenerThrowable = new RuntimeException("listener error");
        try {
            reporter.invocationListenerThrewException(listener, listenerThrowable);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("The invocation listener with type " + listener.getClass().getName()));
            assertTrue(e.getMessage().contains("threw an exception : java.lang.RuntimeExceptionlistener error"));
        }
    }

    @Test
    public void cannotInjectDependency() throws NoSuchFieldException {
        Field field = ReporterTest.class.getDeclaredField("reporter");
        Object matchingMock = "mock";
        Exception details = new RuntimeException("injection error", new RuntimeException("cause message"));
        try {
            reporter.cannotInjectDependency(field, matchingMock, details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito couldn't inject mock dependency"));
            assertTrue(e.getMessage().contains("cause message"));
        }
    }

    @Test
    public void mockedTypeIsInconsistentWithDelegatedInstanceType() {
        try {
            reporter.mockedTypeIsInconsistentWithDelegatedInstanceType(String.class, new ArrayList());
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be the same as the type of your delegated instance."));
            assertTrue(e.getMessage().contains("ArrayList"));
            assertTrue(e.getMessage().contains("String"));
        }
    }

    @Test
    public void spyAndDelegateAreMutuallyExclusive() {
        try {
            reporter.spyAndDelegateAreMutuallyExclusive();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Settings should not define a spy instance and a delegated instance at the same time."));
        }
    }

    @Test
    public void invalidArgumentRangeAtIdentityAnswerCreationTime() {
        try {
            reporter.invalidArgumentRangeAtIdentityAnswerCreationTime();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Invalid argument index."));
        }
    }

    @Test
    public void invalidArgumentPositionRangeAtInvocationTime() {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMock()).thenReturn("mock");
        Method method = Object.class.getMethods()[0]; // any method
        when(invocation.getMethod()).thenReturn(method);
        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, false, 5);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Invalid argument index for the current invocation of method :"));
            assertTrue(e.getMessage().contains("Wanted parameter at position 5"));
        }
    }

    @Test
    public void wrongTypeOfArgumentToReturn() {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMock()).thenReturn("mock");
        Method method = Object.class.getMethods()[0];
        when(invocation.getMethod()).thenReturn(method);
        try {
            reporter.wrongTypeOfArgumentToReturn(invocation, "String", Integer.class, 0);
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("The argument of type 'Integer' cannot be returned"));
            assertTrue(e.getMessage().contains("should return the type 'String'"));
        }
    }

    @Test
    public void defaultAnswerDoesNotAcceptNullParameter() {
        try {
            reporter.defaultAnswerDoesNotAcceptNullParameter();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertEquals("defaultAnswer() does not accept null parameter", e.getMessage());
        }
    }

    @Test
    public void serializableWontWorkForObjectsThatDontImplementSerializable() {
        try {
            reporter.serializableWontWorkForObjectsThatDontImplementSerializable(Object.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("You are using the setting 'withSettings().serializable()' however the type you are trying to mock 'Object'"));
            assertTrue(e.getMessage().contains("do not implement Serializable"));
        }
    }

    @Test
    public void delegatedMethodHasWrongReturnType() throws NoSuchMethodException {
        Method mockMethod = Object.class.getMethod("toString");
        Method delegateMethod = String.class.getMethod("length");
        Object mock = "mock";
        Object delegate = "delegate";
        try {
            reporter.delegatedMethodHasWrongReturnType(mockMethod, delegateMethod, mock, delegate);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Methods called on delegated instance must have compatible return types with the mock."));
            assertTrue(e.getMessage().contains("toString"));
            assertTrue(e.getMessage().contains("String"));
            assertTrue(e.getMessage().contains("int"));
        }
    }

    @Test
    public void delegatedMethodDoesNotExistOnDelegate() throws NoSuchMethodException {
        Method mockMethod = Object.class.getMethod("toString");
        Object mock = "mock";
        Object delegate = "delegate";
        try {
            reporter.delegatedMethodDoesNotExistOnDelegate(mockMethod, mock, delegate);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Methods called on mock must exist in delegated instance."));
            assertTrue(e.getMessage().contains("toString"));
        }
    }

    @Test
    public void usingConstructorWithFancySerializable() {
        try {
            reporter.usingConstructorWithFancySerializable(SerializableMode.ACROSS_CLASSLOADERS);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocks instantiated with constructor cannot be combined with ACROSS_CLASSLOADERS serialization mode."));
        }
    }
}
