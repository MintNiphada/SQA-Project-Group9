package org.mockito.internal.verification;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

public class VerificationOverTimeImplTest {

    private static class CountingTimer extends Timer {
        private int count;

        public CountingTimer(int count) {
            super(0);
            this.count = count;
        }

        @Override
        public void start() {
        }

        @Override
        public boolean isCounting() {
            return count-- > 0;
        }
    }

    private static class DummyVerificationData implements VerificationData {
        public org.mockito.internal.invocation.AllInvocationsFinder getAllInvocationsFinder() {
            return null;
        }

        public java.util.List<org.mockito.invocation.Invocation> getAllInvocations() {
            return null;
        }

        public org.mockito.internal.invocation.InvocationMatcher getWanted() {
            return null;
        }
    }

    @Test
    public void testGettersAndConstructors() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
            }
        };

        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10L, 50L, delegate, true);
        Assert.assertEquals(10L, impl.getPollingPeriod());
        Assert.assertEquals(50L, impl.getDuration());
        Assert.assertSame(delegate, impl.getDelegate());

        Timer customTimer = new Timer(50L);
        VerificationOverTimeImpl implCustomTimer = new VerificationOverTimeImpl(20L, 100L, delegate, false, customTimer);
        Assert.assertEquals(20L, implCustomTimer.getPollingPeriod());
        Assert.assertEquals(100L, implCustomTimer.getDuration());
        Assert.assertSame(delegate, implCustomTimer.getDelegate());
    }

    @Test
    public void testCanRecoverFromFailure() {
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(1, 1, null, true);

        VerificationMode regularMode = new VerificationMode() {
            public void verify(VerificationData data) {
            }
        };
        Assert.assertTrue(impl.canRecoverFromFailure(regularMode));

        VerificationMode atMostMode = new AtMost(1);
        Assert.assertFalse(impl.canRecoverFromFailure(atMostMode));

        VerificationMode noMoreInteractionsMode = new NoMoreInteractions();
        Assert.assertFalse(impl.canRecoverFromFailure(noMoreInteractionsMode));
    }

    @Test
    public void testVerifySuccessImmediatelyWhenReturnOnSuccessIsTrue() {
        final int[] invocations = new int[1];
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                invocations[0]++;
            }
        };

        CountingTimer timer = new CountingTimer(5);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(1, 100, delegate, true, timer);

        impl.verify(new DummyVerificationData());
        Assert.assertEquals(1, invocations[0]);
    }

    @Test
    public void testVerifyRunsFullDurationWhenReturnOnSuccessIsFalse() {
        final int[] invocations = new int[1];
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                invocations[0]++;
            }
        };

        CountingTimer timer = new CountingTimer(3);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(1, 100, delegate, false, timer);

        impl.verify(new DummyVerificationData());
        Assert.assertEquals(3, invocations[0]);
    }

    @Test
    public void testVerifyRecoversFromMockitoAssertionErrorWhenReturnOnSuccessIsTrue() {
        final int[] invocations = new int[1];
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                invocations[0]++;
                if (invocations[0] < 3) {
                    throw new MockitoAssertionError("Temporary failure");
                }
            }
        };

        CountingTimer timer = new CountingTimer(5);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        impl.verify(new DummyVerificationData());
        Assert.assertEquals(3, invocations[0]);
    }

    @Test
    public void testVerifyRecoversFromArgumentsAreDifferentWhenReturnOnSuccessIsTrue() {
        final int[] invocations = new int[1];
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                invocations[0]++;
                if (invocations[0] < 2) {
                    throw new ArgumentsAreDifferent("Arguments differ", "expected", "actual");
                }
            }
        };

        CountingTimer timer = new CountingTimer(5);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        impl.verify(new DummyVerificationData());
        Assert.assertEquals(2, invocations[0]);
    }

    @Test
    public void testVerifyThrowsLastMockitoAssertionErrorWhenTimerExpires() {
        final MockitoAssertionError expectedError = new MockitoAssertionError("Persistent failure");
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw expectedError;
            }
        };

        CountingTimer timer = new CountingTimer(2);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        try {
            impl.verify(new DummyVerificationData());
            Assert.fail("Expected MockitoAssertionError to be thrown");
        } catch (MockitoAssertionError e) {
            Assert.assertSame(expectedError, e);
        }
    }

    @Test
    public void testVerifyThrowsLastArgumentsAreDifferentWhenTimerExpires() {
        final ArgumentsAreDifferent expectedError = new ArgumentsAreDifferent("Diff error", "expected", "actual");
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw expectedError;
            }
        };

        CountingTimer timer = new CountingTimer(2);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        try {
            impl.verify(new DummyVerificationData());
            Assert.fail("Expected ArgumentsAreDifferent to be thrown");
        } catch (ArgumentsAreDifferent e) {
            Assert.assertSame(expectedError, e);
        }
    }

    @Test
    public void testVerifyFailsImmediatelyWhenDelegateCannotRecoverWithAtMost() {
        final MockitoAssertionError expectedError = new MockitoAssertionError("At most failure");
        VerificationMode delegate = new AtMost(1) {
            @Override
            public void verify(VerificationData data) {
                throw expectedError;
            }
        };

        CountingTimer timer = new CountingTimer(5);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        try {
            impl.verify(new DummyVerificationData());
            Assert.fail("Expected MockitoAssertionError to be thrown immediately");
        } catch (MockitoAssertionError e) {
            Assert.assertSame(expectedError, e);
        }
    }

    @Test
    public void testVerifyFailsImmediatelyWhenDelegateCannotRecoverWithNoMoreInteractions() {
        final ArgumentsAreDifferent expectedError = new ArgumentsAreDifferent("Arguments differ", "exp", "act");
        VerificationMode delegate = new NoMoreInteractions() {
            @Override
            public void verify(VerificationData data) {
                throw expectedError;
            }
        };

        CountingTimer timer = new CountingTimer(5);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);

        try {
            impl.verify(new DummyVerificationData());
            Assert.fail("Expected ArgumentsAreDifferent to be thrown immediately");
        } catch (ArgumentsAreDifferent e) {
            Assert.assertSame(expectedError, e);
        }
    }

    @Test
    public void testVerifyRecoversAndResetsErrorWhenReturnOnSuccessIsFalse() {
        final int[] invocations = new int[1];
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                invocations[0]++;
                if (invocations[0] == 1) {
                    throw new MockitoAssertionError("First attempt failure");
                }
            }
        };

        CountingTimer timer = new CountingTimer(2);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, false, timer);

        impl.verify(new DummyVerificationData());
        Assert.assertEquals(2, invocations[0]);
    }

    @Test
    public void testSleepInterruptedHandledGracefully() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new MockitoAssertionError("Failure");
            }
        };

        CountingTimer timer = new CountingTimer(1);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(50, 100, delegate, true, timer);

        Thread.currentThread().interrupt();
        try {
            impl.verify(new DummyVerificationData());
            Assert.fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            // expected
        } finally {
            Thread.interrupted(); // clear interrupted status
        }
    }

    @Test
    public void testVerifyWithZeroIterationsTimer() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                Assert.fail("Delegate verify should not be called when timer count is 0");
            }
        };

        CountingTimer timer = new CountingTimer(0);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 100, delegate, true, timer);
        impl.verify(new DummyVerificationData());
    }
}
