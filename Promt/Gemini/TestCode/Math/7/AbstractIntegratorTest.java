package org.apache.commons.math3.ode;

import java.util.Collection;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.analysis.solvers.RiddersSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class AbstractIntegratorTest {

    private static class ConcreteIntegrator extends AbstractIntegrator {
        private double fixedStep;

        public ConcreteIntegrator(String name, double fixedStep) {
            super(name);
            this.fixedStep = fixedStep;
        }

        public ConcreteIntegrator() {
            super();
            this.fixedStep = 0.1;
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t)
                throws NumberIsTooSmallException, DimensionMismatchException,
                MaxCountExceededException {
            sanityChecks(equations, t);
            setEquations(equations);
            resetOccurred = false;

            double[] y = equations.getCompleteState();
            double[] yDot = new double[y.length];
            initIntegration(equations.getTime(), y, t);

            boolean forward = t > equations.getTime();
            this.stepSize = forward ? fixedStep : -fixedStep;
            this.stepStart = equations.getTime();

            while ((forward && stepStart < t) || (!forward && stepStart > t)) {
                double nextTime = forward ? Math.min(stepStart + stepSize, t) : Math.max(stepStart + stepSize, t);
                computeDerivatives(stepStart, y, yDot);
                for (int i = 0; i < y.length; i++) {
                    y[i] += (nextTime - stepStart) * yDot[i];
                }

                DummyStepInterpolator interpolator = new DummyStepInterpolator(y, yDot, forward);
                interpolator.storeTime(stepStart);
                interpolator.shift();
                interpolator.storeTime(nextTime);

                double acceptedT = acceptStep(interpolator, y, yDot, t);
                stepStart = acceptedT;
                equations.setTime(stepStart);
                equations.setCompleteState(y);

                if (isLastStep) {
                    break;
                }
            }
        }
    }

    private static class LinearODE implements FirstOrderDifferentialEquations {
        private final int dimension;
        private final double rate;

        public LinearODE(int dimension, double rate) {
            this.dimension = dimension;
            this.rate = rate;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            for (int i = 0; i < dimension; i++) {
                yDot[i] = rate;
            }
        }
    }

    @Test
    public void testConstructorsAndName() {
        AbstractIntegrator integratorWithName = new ConcreteIntegrator("TestIntegrator", 0.5);
        Assert.assertEquals("TestIntegrator", integratorWithName.getName());

        AbstractIntegrator defaultIntegrator = new ConcreteIntegrator();
        Assert.assertNull(defaultIntegrator.getName());
    }

    @Test
    public void testEvaluationCounters() {
        AbstractIntegrator integrator = new ConcreteIntegrator("CounterTest", 0.1);
        Assert.assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());
        Assert.assertEquals(0, integrator.getEvaluations());

        integrator.setMaxEvaluations(100);
        Assert.assertEquals(100, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(-5);
        Assert.assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());
    }

    @Test
    public void testStepHandlerManagement() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        Assert.assertTrue(integrator.getStepHandlers().isEmpty());

        StepHandler handler1 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        StepHandler handler2 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };

        integrator.addStepHandler(handler1);
        integrator.addStepHandler(handler2);
        Collection<StepHandler> handlers = integrator.getStepHandlers();
        Assert.assertEquals(2, handlers.size());
        Assert.assertTrue(handlers.contains(handler1));
        Assert.assertTrue(handlers.contains(handler2));

        try {
            handlers.add(handler1);
            Assert.fail("getStepHandlers should return an unmodifiable collection");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        integrator.clearStepHandlers();
        Assert.assertTrue(integrator.getStepHandlers().isEmpty());
    }

    @Test
    public void testEventHandlerManagement() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        Assert.assertTrue(integrator.getEventHandlers().isEmpty());

        EventHandler handler1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        EventHandler handler2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(handler1, 1.0, 1e-6, 100);
        integrator.addEventHandler(handler2, 0.5, 1e-5, 50, new RiddersSolver());

        Collection<EventHandler> handlers = integrator.getEventHandlers();
        Assert.assertEquals(2, handlers.size());
        Assert.assertTrue(handlers.contains(handler1));
        Assert.assertTrue(handlers.contains(handler2));

        try {
            handlers.add(handler1);
            Assert.fail("getEventHandlers should return an unmodifiable collection");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        integrator.clearEventHandlers();
        Assert.assertTrue(integrator.getEventHandlers().isEmpty());
    }

    @Test
    public void testStepStartAndSize() {
        AbstractIntegrator integrator = new ConcreteIntegrator("Test", 0.2);
        Assert.assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
        Assert.assertTrue(Double.isNaN(integrator.getCurrentSignedStepsize()));

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        Assert.assertEquals(1.0, integrator.getCurrentStepStart(), 1e-10);
        Assert.assertEquals(0.2, integrator.getCurrentSignedStepsize(), 1e-10);
    }

    @Test
    public void testIntegrateSuccess() {
        AbstractIntegrator integrator = new ConcreteIntegrator("Euler", 0.1);
        FirstOrderDifferentialEquations ode = new LinearODE(2, 2.0);
        double[] y0 = new double[] { 1.0, -1.0 };
        double[] y = new double[2];

        double finalTime = integrator.integrate(ode, 0.0, y0, 2.0, y);
        Assert.assertEquals(2.0, finalTime, 1e-10);
        Assert.assertEquals(5.0, y[0], 1e-10);
        Assert.assertEquals(3.0, y[1], 1e-10);
        Assert.assertTrue(integrator.getEvaluations() > 0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateY0DimensionMismatch() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        FirstOrderDifferentialEquations ode = new LinearODE(2, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[2];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateYDimensionMismatch() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        FirstOrderDifferentialEquations ode = new LinearODE(2, 1.0);
        double[] y0 = new double[] { 0.0, 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSanityChecksSpanTooSmall() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(1.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.sanityChecks(expandable, 1.0);
    }

    @Test
    public void testSanityChecksValidSpan() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.sanityChecks(expandable, 1.0);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testMaxEvaluationsExceeded() {
        AbstractIntegrator integrator = new ConcreteIntegrator("Test", 0.01);
        integrator.setMaxEvaluations(5);
        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 10.0, y);
    }

    @Test
    public void testInitIntegrationInitializesHandlers() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        final boolean[] stepHandlerInitCalled = new boolean[] { false };
        final boolean[] eventHandlerInitCalled = new boolean[] { false };

        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {
                stepHandlerInitCalled[0] = true;
                Assert.assertEquals(0.0, t0, 1e-10);
                Assert.assertEquals(1.0, t, 1e-10);
            }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        });

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {
                eventHandlerInitCalled[0] = true;
                Assert.assertEquals(0.0, t0, 1e-10);
                Assert.assertEquals(1.0, t, 1e-10);
            }
            public double g(double t, double[] y) { return 1.0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        }, 0.5, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        Assert.assertTrue(stepHandlerInitCalled[0]);
        Assert.assertTrue(eventHandlerInitCalled[0]);
    }

    @Test
    public void testEventStoppingIntegration() {
        AbstractIntegrator integrator = new ConcreteIntegrator("StopTest", 0.1);
        final double stopTime = 0.5;

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - stopTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-9, 100, new BracketingNthOrderBrentSolver(1e-9, 5));

        FirstOrderDifferentialEquations ode = new LinearODE(1, 2.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];

        double finalT = integrator.integrate(ode, 0.0, y0, 2.0, y);
        Assert.assertEquals(stopTime, finalT, 1e-6);
        Assert.assertEquals(2.0 * stopTime, y[0], 1e-6);
    }

    @Test
    public void testEventResetState() {
        AbstractIntegrator integrator = new ConcreteIntegrator("ResetStateTest", 0.1);
        final double resetTime = 0.5;

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - resetTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            public void resetState(double t, double[] y) {
                y[0] = 100.0;
            }
        }, 0.1, 1e-9, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];

        double finalT = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, finalT, 1e-6);
        Assert.assertEquals(100.0 + (1.0 - resetTime) * 1.0, y[0], 1e-6);
    }

    @Test
    public void testEventResetDerivatives() {
        AbstractIntegrator integrator = new ConcreteIntegrator("ResetDerivTest", 0.1);
        final double resetTime = 0.5;

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - resetTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_DERIVATIVES;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-9, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];

        double finalT = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, finalT, 1e-6);
        Assert.assertEquals(1.0, y[0], 1e-6);
    }

    @Test
    public void testBackwardIntegrationWithEvents() {
        AbstractIntegrator integrator = new ConcreteIntegrator("BackwardTest", 0.1);
        final double stopTime = -0.5;

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - stopTime; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-9, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];

        double finalT = integrator.integrate(ode, 0.0, y0, -2.0, y);
        Assert.assertEquals(stopTime, finalT, 1e-6);
        Assert.assertEquals(-0.5, y[0], 1e-6);
    }

    @Test
    public void testMultipleEventsInStep() {
        AbstractIntegrator integrator = new ConcreteIntegrator("MultiEventTest", 1.0);

        EventHandler event1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.25; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        EventHandler event2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.75; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(event1, 0.1, 1e-9, 100);
        integrator.addEventHandler(event2, 0.1, 1e-9, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1, 1.0);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];

        double finalT = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, finalT, 1e-6);
        Assert.assertEquals(1.0, y[0], 1e-6);
    }

    @Test
    public void testComputeDerivativesWithoutExpandableThrowsNullPointer() {
        AbstractIntegrator integrator = new ConcreteIntegrator();
        try {
            integrator.computeDerivatives(0.0, new double[] { 0.0 }, new double[] { 0.0 });
            Assert.fail("Expected NullPointerException when expandable is not set");
        } catch (NullPointerException expected) {
            // expected
        }
    }
}
