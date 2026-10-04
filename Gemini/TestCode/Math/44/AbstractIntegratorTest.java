package org.apache.commons.math.ode;

import java.util.Collection;
import org.apache.commons.math.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class AbstractIntegratorTest {

    private static class DummyIntegrator extends AbstractIntegrator {
        public DummyIntegrator() {
            super();
        }

        public DummyIntegrator(String name) {
            super(name);
        }

        public void integrate(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
            setEquations(equations);
            this.stepStart = equations.getTime();
            this.stepSize = t - equations.getTime();
            double[] y = equations.getPrimaryState();
            double[] yDot = new double[y.length];
            computeDerivatives(equations.getTime(), y, yDot);
            for (int i = 0; i < y.length; i++) {
                y[i] += yDot[i] * this.stepSize;
            }
            equations.setTime(t);
            equations.setPrimaryState(y);
        }

        public double testAcceptStep(AbstractStepInterpolator interpolator, double[] y, double[] yDot, double tEnd) {
            return acceptStep(interpolator, y, yDot, tEnd);
        }

        public void testSetStateInitialized(boolean initialized) {
            setStateInitialized(initialized);
        }

        public void testSanityChecks(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
        }

        public void testSetEquations(ExpandableStatefulODE equations) {
            setEquations(equations);
        }

        public void testResetEvaluations() {
            resetEvaluations();
        }

        public boolean isResetOccurred() {
            return resetOccurred;
        }

        public boolean isLastStep() {
            return isLastStep;
        }
    }

    private static class DummyStepInterpolator extends AbstractStepInterpolator {
        private double[] state;
        private double[] derivatives;

        public DummyStepInterpolator(double previousTime, double currentTime, double[] state, double[] derivatives, boolean forward) {
            this.previousTime = previousTime;
            this.currentTime = currentTime;
            this.h = currentTime - previousTime;
            this.interpolatedTime = currentTime;
            this.state = state.clone();
            this.derivatives = derivatives.clone();
            this.interpolatedState = state.clone();
            this.interpolatedDerivatives = derivatives.clone();
            setSoftPreviousTime(previousTime);
            setSoftCurrentTime(currentTime);
            if (!forward) {
                storeTime(currentTime);
                shift();
                storeTime(previousTime);
            }
        }

        public DummyStepInterpolator(DummyStepInterpolator interpolator) {
            super(interpolator);
            this.state = interpolator.state.clone();
            this.derivatives = interpolator.derivatives.clone();
        }

        protected StepInterpolator doCopy() {
            return new DummyStepInterpolator(this);
        }

        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            for (int i = 0; i < interpolatedState.length; i++) {
                interpolatedState[i] = state[i] + theta * (currentTime - previousTime) * derivatives[i];
                interpolatedDerivatives[i] = derivatives[i];
            }
        }
    }

    @Test
    public void testConstructorsAndGetters() {
        DummyIntegrator integrator1 = new DummyIntegrator("testIntegrator");
        Assert.assertEquals("testIntegrator", integrator1.getName());
        Assert.assertTrue(Double.isNaN(integrator1.getCurrentStepStart()));
        Assert.assertTrue(Double.isNaN(integrator1.getCurrentSignedStepsize()));
        Assert.assertEquals(Integer.MAX_VALUE, integrator1.getMaxEvaluations());
        Assert.assertEquals(0, integrator1.getEvaluations());

        DummyIntegrator integrator2 = new DummyIntegrator();
        Assert.assertNull(integrator2.getName());

        integrator1.setMaxEvaluations(100);
        Assert.assertEquals(100, integrator1.getMaxEvaluations());
        integrator1.setMaxEvaluations(-5);
        Assert.assertEquals(Integer.MAX_VALUE, integrator1.getMaxEvaluations());
    }

    @Test
    public void testStepHandlers() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        Assert.assertEquals(0, integrator.getStepHandlers().size());

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

        integrator.clearStepHandlers();
        Assert.assertEquals(0, integrator.getStepHandlers().size());
    }

    @Test
    public void testEventHandlers() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        Assert.assertEquals(0, integrator.getEventHandlers().size());

        EventHandler handler1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        EventHandler handler2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.STOP; }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(handler1, 1.0, 1e-3, 10);
        integrator.addEventHandler(handler2, 2.0, 1e-4, 20, new BracketingNthOrderBrentSolver(1e-4, 5));

        Collection<EventHandler> handlers = integrator.getEventHandlers();
        Assert.assertEquals(2, handlers.size());
        Assert.assertTrue(handlers.contains(handler1));
        Assert.assertTrue(handlers.contains(handler2));

        integrator.clearEventHandlers();
        Assert.assertEquals(0, integrator.getEventHandlers().size());
    }

    @Test
    public void testIntegrateSuccessful() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0;
            }
        };

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 5.0, y);

        Assert.assertEquals(5.0, stopTime, 1e-10);
        Assert.assertEquals(11.0, y[0], 1e-10);
        Assert.assertEquals(0.0, integrator.getCurrentStepStart(), 1e-10);
        Assert.assertEquals(5.0, integrator.getCurrentSignedStepsize(), 1e-10);
        Assert.assertEquals(1, integrator.getEvaluations());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateDimensionMismatchY0() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[1], 1.0, new double[2]);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrateDimensionMismatchY() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[2], 1.0, new double[1]);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testComputeDerivativesExceedsMaxEvaluations() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.setMaxEvaluations(1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };
        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        integrator.testSetEquations(stateful);
        integrator.computeDerivatives(0.0, new double[]{1.0}, new double[1]);
        integrator.computeDerivatives(0.0, new double[]{1.0}, new double[1]);
    }

    @Test
    public void testResetEvaluations() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 0;
            }
        };
        integrator.testSetEquations(new ExpandableStatefulODE(ode));
        integrator.computeDerivatives(0.0, new double[1], new double[1]);
        Assert.assertEquals(1, integrator.getEvaluations());
        integrator.testResetEvaluations();
        Assert.assertEquals(0, integrator.getEvaluations());
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSanityChecksFailsForSmallInterval() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(1.0);
        integrator.testSanityChecks(stateful, 1.0);
    }

    @Test
    public void testAcceptStepNoEvents() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        final boolean[] stepHandled = new boolean[1];
        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepHandled[0] = true;
            }
        });

        double[] y = new double[] { 1.0 };
        double[] yDot = new double[] { 2.0 };
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 1.0, y, yDot, true);

        double tEnd = integrator.testAcceptStep(interpolator, y, yDot, 1.0);
        Assert.assertEquals(1.0, tEnd, 1e-10);
        Assert.assertTrue(stepHandled[0]);
        Assert.assertTrue(integrator.isLastStep());
    }

    @Test
    public void testAcceptStepWithEventStop() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.5;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };
        integrator.testSetEquations(new ExpandableStatefulODE(ode));
        integrator.testSetStateInitialized(false);

        double[] y = new double[] { 0.0 };
        double[] yDot = new double[] { 1.0 };
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 1.0, y, yDot, true);

        double tEnd = integrator.testAcceptStep(interpolator, y, yDot, 1.0);
        Assert.assertEquals(0.5, tEnd, 1e-5);
        Assert.assertTrue(integrator.isLastStep());
        Assert.assertEquals(0.5, y[0], 1e-5);
    }

    @Test
    public void testAcceptStepWithEventResetState() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.4;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            public void resetState(double t, double[] y) {
                y[0] = 10.0;
            }
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };
        integrator.testSetEquations(new ExpandableStatefulODE(ode));
        integrator.testSetStateInitialized(false);

        double[] y = new double[] { 0.0 };
        double[] yDot = new double[] { 1.0 };
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 1.0, y, yDot, true);

        double tEnd = integrator.testAcceptStep(interpolator, y, yDot, 1.0);
        Assert.assertEquals(0.4, tEnd, 1e-5);
        Assert.assertTrue(integrator.isResetOccurred());
        Assert.assertEquals(10.0, y[0], 1e-5);
    }

    @Test
    public void testAcceptStepWithEventResetDerivatives() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.4;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_DERIVATIVES;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 5.0;
            }
        };
        integrator.testSetEquations(new ExpandableStatefulODE(ode));
        integrator.testSetStateInitialized(false);

        double[] y = new double[] { 0.0 };
        double[] yDot = new double[] { 1.0 };
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, 1.0, y, yDot, true);

        double tEnd = integrator.testAcceptStep(interpolator, y, yDot, 1.0);
        Assert.assertEquals(0.4, tEnd, 1e-5);
        Assert.assertTrue(integrator.isResetOccurred());
        Assert.assertEquals(5.0, yDot[0], 1e-5);
    }

    @Test
    public void testAcceptStepBackwardIntegration() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - (-0.5);
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };
        integrator.testSetEquations(new ExpandableStatefulODE(ode));
        integrator.testSetStateInitialized(false);

        double[] y = new double[] { 0.0 };
        double[] yDot = new double[] { -1.0 };
        DummyStepInterpolator interpolator = new DummyStepInterpolator(0.0, -1.0, y, yDot, false);

        double tEnd = integrator.testAcceptStep(interpolator, y, yDot, -1.0);
        Assert.assertEquals(-1.0, tEnd, 1e-5);
        Assert.assertTrue(integrator.isLastStep());
    }
}
