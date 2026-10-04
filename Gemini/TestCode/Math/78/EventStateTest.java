package org.apache.commons.math.ode.events;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EventStateTest {

    private static class DummyStepInterpolator extends AbstractStepInterpolator {
        private boolean failOnInterpolate = false;

        public DummyStepInterpolator(double t0, double t1, double[] y) {
            this.previousTime = t0;
            this.currentTime = t1;
            this.h = t1 - t0;
            this.currentState = y.clone();
            this.interpolatedState = y.clone();
            this.interpolatedDerivatives = new double[y.length];
            this.finalized = true;
            this.forward = t1 >= t0;
        }

        public DummyStepInterpolator(double t0, double t1, double[] y, boolean failOnInterpolate) {
            this(t0, t1, y);
            this.failOnInterpolate = failOnInterpolate;
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) throws DerivativeException {
            if (failOnInterpolate) {
                throw new DerivativeException("Derivative exception in interpolator", new Object[0]);
            }
            if (currentState != null) {
                System.arraycopy(currentState, 0, interpolatedState, 0, currentState.length);
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {}

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {}

        @Override
        protected StepInterpolator doCopy() {
            return this;
        }
    }

    private static class MockEventHandler implements EventHandler {
        private double root;
        private int eventOccurredAction = EventHandler.CONTINUE;
        private boolean resetCalled = false;
        private boolean failG = false;
        private boolean failReset = false;

        public MockEventHandler(double root) {
            this.root = root;
        }

        public double g(double t, double[] y) throws EventException {
            if (failG) {
                throw new EventException("g failed", new Object[0]);
            }
            return t - root;
        }

        public int eventOccurred(double t, double[] y, boolean increasing) throws EventException {
            return eventOccurredAction;
        }

        public void resetState(double t, double[] y) throws EventException {
            if (failReset) {
                throw new EventException("resetState failed", new Object[0]);
            }
            resetCalled = true;
            if (y != null && y.length > 0) {
                y[0] = 999.0;
            }
        }
    }

    @Test
    public void testGettersAndInitialState() {
        MockEventHandler handler = new MockEventHandler(5.0);
        EventState es = new EventState(handler, 2.0, -1e-6, 50);

        Assert.assertSame(handler, es.getEventHandler());
        Assert.assertEquals(2.0, es.getMaxCheckInterval(), 1e-12);
        Assert.assertEquals(1e-6, es.getConvergence(), 1e-12);
        Assert.assertEquals(50, es.getMaxIterationCount());
        Assert.assertTrue(Double.isNaN(es.getEventTime()));
        Assert.assertFalse(es.stop());
    }

    @Test
    public void testReinitializeBegin() throws EventException {
        MockEventHandler handler = new MockEventHandler(5.0);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(2.0, new double[]{1.0});
        Assert.assertTrue(Double.isNaN(es.getEventTime()));
    }

    @Test
    public void testEvaluateStepNoEvent() throws Exception {
        MockEventHandler handler = new MockEventHandler(10.0);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 4.0, new double[]{0.0});
        boolean result = es.evaluateStep(interpolator);

        Assert.assertFalse(result);
        Assert.assertTrue(Double.isNaN(es.getEventTime()));
    }

    @Test
    public void testEvaluateStepWithEventForward() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 5.0, new double[]{0.0});
        boolean result = es.evaluateStep(interpolator);

        Assert.assertTrue(result);
        Assert.assertEquals(2.5, es.getEventTime(), 1e-5);

        es.stepAccepted(2.5, new double[]{0.0});
        Assert.assertFalse(es.stop());
    }

    @Test
    public void testEvaluateStepWithEventBackward() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(5.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(5.0, 0.0, new double[]{0.0});
        boolean result = es.evaluateStep(interpolator);

        Assert.assertTrue(result);
        Assert.assertEquals(2.5, es.getEventTime(), 1e-5);
    }

    @Test
    public void testEvaluateStepAlreadyWaitingAccepted() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator1 = new DummyStepInterpolator(0.0, 5.0, new double[]{0.0});
        es.evaluateStep(interpolator1);

        DummyStepInterpolator interpolator2 = new DummyStepInterpolator(0.0, 2.5, new double[]{0.0});
        boolean result = es.evaluateStep(interpolator2);

        Assert.assertFalse(result);
    }

    @Test
    public void testEvaluateStepPastEventIgnored() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 3.0, new double[]{0.0});
        es.evaluateStep(interpolator);
        es.stepAccepted(2.5, new double[]{0.0});

        es.reinitializeBegin(2.5, new double[]{0.0});
        DummyStepInterpolator interpolatorNext = new DummyStepInterpolator(2.5, 5.0, new double[]{0.0});
        boolean result = es.evaluateStep(interpolatorNext);
        Assert.assertFalse(result);
    }

    @Test(expected = DerivativeException.class)
    public void testEvaluateStepDerivativeException() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 5.0, new double[]{0.0}, true);
        es.evaluateStep(interpolator);
    }

    @Test(expected = EventException.class)
    public void testEvaluateStepEventException() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.5);
        EventState es = new EventState(handler, 2.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        handler.failG = true;
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 5.0, new double[]{0.0});
        es.evaluateStep(interpolator);
    }

    @Test
    public void testStepAcceptedStop() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.0);
        handler.eventOccurredAction = EventHandler.STOP;
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 3.0, new double[]{0.0});
        es.evaluateStep(interpolator);
        es.stepAccepted(2.0, new double[]{0.0});

        Assert.assertTrue(es.stop());
    }

    @Test
    public void testStepAcceptedWithoutPendingEvent() throws Exception {
        MockEventHandler handler = new MockEventHandler(5.0);
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});
        es.stepAccepted(1.0, new double[]{0.0});
        Assert.assertFalse(es.stop());
    }

    @Test
    public void testResetWithoutPendingEvent() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.0);
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        double[] y = new double[]{0.0};
        boolean reset = es.reset(1.0, y);
        Assert.assertFalse(reset);
    }

    @Test
    public void testResetWithResetStateAction() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.0);
        handler.eventOccurredAction = EventHandler.RESET_STATE;
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 3.0, new double[]{0.0});
        es.evaluateStep(interpolator);
        es.stepAccepted(2.0, new double[]{0.0});

        double[] y = new double[]{0.0};
        boolean reset = es.reset(2.0, y);
        Assert.assertTrue(reset);
        Assert.assertTrue(handler.resetCalled);
        Assert.assertEquals(999.0, y[0], 1e-12);
        Assert.assertTrue(Double.isNaN(es.getEventTime()));
    }

    @Test
    public void testResetWithResetDerivativesAction() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.0);
        handler.eventOccurredAction = EventHandler.RESET_DERIVATIVES;
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 3.0, new double[]{0.0});
        es.evaluateStep(interpolator);
        es.stepAccepted(2.0, new double[]{0.0});

        double[] y = new double[]{0.0};
        boolean reset = es.reset(2.0, y);
        Assert.assertTrue(reset);
        Assert.assertFalse(handler.resetCalled);
    }

    @Test
    public void testResetWithContinueAction() throws Exception {
        MockEventHandler handler = new MockEventHandler(2.0);
        handler.eventOccurredAction = EventHandler.CONTINUE;
        EventState es = new EventState(handler, 1.0, 1e-6, 50);
        es.reinitializeBegin(0.0, new double[]{0.0});

        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 3.0, new double[]{0.0});
        es.evaluateStep(interpolator);
        es.stepAccepted(2.0, new double[]{0.0});

        double[] y = new double[]{0.0};
        boolean reset = es.reset(2.0, y);
        Assert.assertFalse(reset);
    }
}
