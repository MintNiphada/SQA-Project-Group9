package org.apache.commons.math3.ode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.events.EventState;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.junit.Before;
import org.junit.Test;

public class AbstractIntegratorTest {

    private TestableIntegrator integrator;
    private StepHandler stepHandler;
    private EventHandler eventHandler;

    @Before
    public void setUp() {
        integrator = new TestableIntegrator("test");
        stepHandler = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        eventHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
    }

    // Concrete subclass for testing
    private static class TestableIntegrator extends AbstractIntegrator {
        public TestableIntegrator(String name) {
            super(name);
        }
        public TestableIntegrator() {
            super();
        }
        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            // dummy
        }
        // Public wrappers for protected methods
        public void callInitIntegration(double t0, double[] y0, double t) {
            super.initIntegration(t0, y0, t);
        }
        public void callSetEquations(ExpandableStatefulODE equations) {
            super.setEquations(equations);
        }
        public void callSetStateInitialized(boolean stateInitialized) {
            super.setStateInitialized(stateInitialized);
        }
        public double callAcceptStep(AbstractStepInterpolator interpolator,
                                     double[] y, double[] yDot, double tEnd)
                throws MaxCountExceededException, DimensionMismatchException, NoBracketingException {
            return super.acceptStep(interpolator, y, yDot, tEnd);
        }
        public void callSanityChecks(ExpandableStatefulODE equations, double t) {
            super.sanityChecks(equations, t);
        }
    }

    // Minimal step interpolator stub
    private static class TestStepInterpolator extends AbstractStepInterpolator {
        private double previousT;
        private double currentT;
        private boolean forwardFlag;
        private double[] interpolatedState;

        public TestStepInterpolator(double[] y, boolean forward) {
            super(y, forward);
            this.forwardFlag = forward;
            // initialize with some defaults
            this.previousT = 0.0;
            this.currentT = 1.0;
            this.interpolatedState = y.clone();
        }

        public void setTimes(double prev, double cur) {
            this.previousT = prev;
            this.currentT = cur;
        }

        public void setInterpolatedState(double[] state) {
            this.interpolatedState = state;
        }

        @Override
        public double getGlobalPreviousTime() {
            return previousT;
        }

        @Override
        public double getGlobalCurrentTime() {
            return currentT;
        }

        @Override
        public boolean isForward() {
            return forwardFlag;
        }

        @Override
        public double[] getInterpolatedState() {
            // clone to avoid interference
            return interpolatedState.clone();
        }

        @Override
        protected AbstractStepInterpolator doCopy() {
            return null;
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta,
                                                               double oneMinusThetaH) {
            // no-op
        }
    }

    @Test
    public void testConstructorWithName() {
        assertEquals("test", integrator.getName());
    }

    @Test
    public void testConstructorNoName() {
        TestableIntegrator ni = new TestableIntegrator();
        assertNull(ni.getName());
    }

    @Test
    public void testAddGetClearStepHandlers() {
        assertNotNull(integrator.getStepHandlers());
        assertTrue(integrator.getStepHandlers().isEmpty());
        integrator.addStepHandler(stepHandler);
        assertEquals(1, integrator.getStepHandlers().size());
        integrator.clearStepHandlers();
        assertTrue(integrator.getStepHandlers().isEmpty());
    }

    @Test
    public void testGetStepHandlersReturnsUnmodifiable() {
        integrator.addStepHandler(stepHandler);
        Collection<StepHandler> handlers = integrator.getStepHandlers();
        try {
            handlers.add(stepHandler);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetCurrentStepStartDefault() {
        assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
    }

    @Test
    public void testGetCurrentSignedStepsizeDefault() {
        assertTrue(Double.isNaN(integrator.getCurrentSignedStepsize()));
    }

    @Test
    public void testSetMaxEvaluationsNegative() {
        integrator.setMaxEvaluations(-1);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());
    }

    @Test
    public void testSetMaxEvaluationsPositive() {
        integrator.setMaxEvaluations(5);
        assertEquals(5, integrator.getMaxEvaluations());
    }

    @Test
    public void testGetEvaluationsInitiallyZero() {
        assertEquals(0, integrator.getEvaluations());
    }

    @Test
    public void testInitIntegrationResetsEvaluationsAndCallsHandlers() {
        final List<String> log = new ArrayList<String>();
        EventHandler eh = new EventHandler() {
            public void init(double t0, double[] y0, double t) { log.add("event"); }
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        StepHandler sh = new StepHandler() {
            public void init(double t0, double[] y0, double t) { log.add("step"); }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        integrator.addEventHandler(eh, 1.0, 1e-10, 10);
        integrator.addStepHandler(sh);
        // initial evaluations 0, set max to some value to check reset
        integrator.setMaxEvaluations(100);
        integrator.getEvaluations(); // just to be sure no side effect
        integrator.callInitIntegration(0.0, new double[] {1.0}, 10.0);
        // evaluations reset
        assertEquals(0, integrator.getEvaluations());
        // both init methods called
        assertEquals(2, log.size());
        assertTrue(log.contains("event"));
        assertTrue(log.contains("step"));
    }

    @Test
    public void testAddEventHandlerWithSolver() {
        integrator.addEventHandler(eventHandler, 1.0, 1e-12, 20);
        assertEquals(1, integrator.getEventHandlers().size());
    }

    @Test
    public void testAddEventHandlerTwoOverloads() {
        integrator.addEventHandler(eventHandler, 1.0, 1e-12, 20);
        integrator.addEventHandler(eventHandler, 2.0, 1e-10, 30,
                new org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver(1e-10, 5));
        assertEquals(2, integrator.getEventHandlers().size());
    }

    @Test
    public void testGetEventHandlers() {
        integrator.addEventHandler(eventHandler, 1.0, 1e-12, 20);
        integrator.addEventHandler(eventHandler, 2.0, 1e-10, 30);
        Collection<EventHandler> handlers = integrator.getEventHandlers();
        assertEquals(2, handlers.size());
        // test unmodifiable
        try {
            handlers.add(eventHandler);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testClearEventHandlers() {
        integrator.addEventHandler(eventHandler, 1.0, 1e-12, 20);
        integrator.clearEventHandlers();
        assertTrue(integrator.getEventHandlers().isEmpty());
    }

    @Test
    public void testComputeDerivatives() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[1];
                yDot[1] = -y[0];
            }
        };
        ExpandableStatefulODE ode = new ExpandableStatefulODE(eq);
        ode.setTime(0);
        ode.setPrimaryState(new double[] {0, 1});
        integrator.callSetEquations(ode);
        double[] y = new double[] {1, 0};
        double[] yDot = new double[2];
        integrator.computeDerivatives(0.0, y, yDot);
        assertEquals(1, integrator.getEvaluations());
        // check yDot computed correctly
        assertEquals(0.0, yDot[0], 1e-15);
        assertEquals(-1.0, yDot[1], 1e-15);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testComputeDerivativesExceedsMaxEvaluations() {
        integrator.setMaxEvaluations(1);
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };
        ExpandableStatefulODE ode = new ExpandableStatefulODE(eq);
        integrator.callSetEquations(ode);
        double[] y = new double[] {0};
        double[] yDot = new double[1];
        integrator.computeDerivatives(0, y, yDot); // first call ok
        integrator.computeDerivatives(0, y, yDot); // should exceed
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSanityChecksTooSmallInterval() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        ExpandableStatefulODE ode = new ExpandableStatefulODE(eq);
        ode.setTime(1.0);
        double tEnd = 1.0 + 1e-14; // dt very small
        integrator.callSanityChecks(ode, tEnd);
    }

    @Test
    public void testSanityChecksNormal() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        ExpandableStatefulODE ode = new ExpandableStatefulODE(eq);
        ode.setTime(0.0);
        integrator.callSanityChecks(ode, 10.0); // should not throw
    }

    @Test
    public void testIntegrateWithConcrete() {
        // Test the public integrate that creates expandable and calls abstract integrate
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[1];
                yDot[1] = -y[0];
            }
        };
        TestableIntegrator integ = new TestableIntegrator("integTest") {
            @Override
            public void integrate(ExpandableStatefulODE equations, double t) {
                // simple forward euler step
                double[] y = equations.getPrimaryState();
                double[] yDot = new double[y.length];
                equations.computeDerivatives(equations.getTime(), y, yDot);
                double h = t - equations.getTime();
                for (int i = 0; i < y.length; i++) {
                    y[i] += h * yDot[i];
                }
                equations.setTime(t);
            }
        };
        double[] y0 = new double[] {0.0, 1.0};
        double[] yOut = new double[2];
        double tFinal = 2.0;
        double resultTime = integ.integrate(eq, 0.0, y0, tFinal, yOut);
        assertEquals(tFinal, resultTime, 1e-15);
        // expected values: sin(t), cos(t) starting at t=0, y0=[0,1] gives y=[sin(t), cos(t)]
        assertEquals(Math.sin(tFinal), yOut[0], 1e-10);
        assertEquals(Math.cos(tFinal), yOut[1], 1e-10);
    }

    @Test
    public void testAcceptStepNoEvents() throws Exception {
        final List<StepHandler> calledHandlers = new ArrayList<StepHandler>();
        final List<Boolean> lastFlags = new ArrayList<Boolean>();
        StepHandler sh = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                calledHandlers.add(this);
                lastFlags.add(isLast);
            }
        };
        integrator.addStepHandler(sh);
        double[] y = new double[] {1.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 5.0);
        // set stateInitialized to false so reinitializeBegin called (no events)
        integrator.callSetStateInitialized(false);
        double resultTime = integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        assertEquals(5.0, resultTime, 1e-15);
        // handler called once
        assertEquals(1, calledHandlers.size());
        // isLastStep should be false because not at tEnd
        assertFalse(lastFlags.get(0));
        // isLastStep should be set false (by default) in integrator
    }

    @Test
    public void testAcceptStepIsLastStepDueToEnd() throws Exception {
        final List<Boolean> lastFlags = new ArrayList<Boolean>();
        StepHandler sh = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                lastFlags.add(isLast);
            }
        };
        integrator.addStepHandler(sh);
        double[] y = new double[] {1.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 10.0); // currentT exactly equals tEnd
        integrator.callSetStateInitialized(false);
        integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        assertTrue(lastFlags.get(0));
    }

    @Test
    public void testAcceptStepWithEventStop() throws Exception {
        final double eventTime = 3.0;
        EventHandler stopHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - eventTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.STOP; }
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(stopHandler, 0.1, 1e-10, 10);
        double[] y = new double[] {0.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 5.0);
        integrator.callSetStateInitialized(false);
        double resultTime = integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        assertEquals(eventTime, resultTime, 1e-15);
        // Verify that step handler received isLast=true
        // We can check via a capturing handler
    }

    @Test
    public void testAcceptStepWithEventReset() throws Exception {
        final double eventTime = 2.0;
        final boolean[] resetCalled = {false};
        EventHandler resetHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - eventTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.RESET_STATE; }
            public void resetState(double t, double[] y) { resetCalled[0] = true; y[0] = 99.0; }
        };
        integrator.addEventHandler(resetHandler, 0.1, 1e-10, 10);
        double[] y = new double[] {1.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 5.0);
        integrator.callSetStateInitialized(false);
        double resultTime = integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        assertEquals(eventTime, resultTime, 1e-15);
        assertTrue(resetCalled[0]);
        // y array must be updated with event state
        assertEquals(99.0, y[0], 1e-15);
    }

    @Test
    @SuppressWarnings("unused")
    public void testAcceptStepMultipleEventsOrdering() throws Exception {
        final double earlyEvent = 1.0;
        final double lateEvent = 4.0;
        final List<Double> eventTimes = new ArrayList<Double>();
        EventHandler earlyEH = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - earlyEvent; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        EventHandler lateEH = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - lateEvent; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { eventTimes.add(t); return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(earlyEH, 0.1, 1e-10, 10);
        integrator.addEventHandler(lateEH, 0.1, 1e-10, 10);
        double[] y = new double[] {0.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 5.0);
        integrator.callSetStateInitialized(false);
        integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        // The late event should fire, but the early one also should (but early event's action is CONTINUE, so it will be processed first)
        // After early event, it should check if it occurs again; if not, then proceed to late event.
        // The eventTimes list from late event will be called once.
        assertEquals(1, eventTimes.size());
        assertEquals(lateEvent, eventTimes.get(0), 1e-10);
    }

    @Test
    public void testAcceptStepEventReOccurs() throws Exception {
        // Event that occurs twice during the step: one at t=2, then again at t=4
        final double[] rootTimes = {2.0, 4.0};
        final int[] rootIndex = {0};
        EventHandler eh = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                if (rootIndex[0] < rootTimes.length) {
                    return t - rootTimes[rootIndex[0]];
                }
                return 1.0; // no more roots
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                rootIndex[0]++;
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(eh, 0.1, 1e-10, 10);
        double[] y = new double[] {0.0};
        double[] yDot = new double[1];
        TestStepInterpolator interpolator = new TestStepInterpolator(y, true);
        interpolator.setTimes(0.0, 5.0);
        integrator.callSetStateInitialized(false);
        integrator.callAcceptStep(interpolator, y, yDot, 10.0);
        // both roots processed
        assertEquals(2, rootIndex[0]);
    }

    @Test
    public void testSetStateInitialized() {
        integrator.callSetStateInitialized(true);
        // no direct getter, but acceptStep checks it
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateDimensionMismatchY0() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(eq, 0.0, new double[]{1.0}, 1.0, new double[2]);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateDimensionMismatchY() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(eq, 0.0, new double[]{1.0, 2.0}, 1.0, new double[1]);
    }

    @Test
    public void testIntegrateDimensionMatch() {
        FirstOrderDifferentialEquations eq = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        // we need an integrate that does nothing but return time; we'll use TestableIntegrator with override
        TestableIntegrator ti = new TestableIntegrator("test") {
            @Override
            public void integrate(ExpandableStatefulODE equations, double t) {
                equations.setTime(t);
            }
        };
        double[] y0 = new double[]{0.5};
        double[] yOut = new double[1];
        double result = ti.integrate(eq, 0.0, y0, 10.0, yOut);
        assertEquals(10.0, result, 1e-15);
        // yOut should be copy of primary state at end (which is unchanged)
        assertEquals(y0[0], yOut[0], 1e-15);
    }
}
