package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class RungeKuttaIntegratorTest {

    private static class LinearEquations implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[0];
        }
    }

    private static class MultiDimEquations implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 2;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[1];
            yDot[1] = -y[0];
        }
    }

    private static class DummyRKIntegrator extends RungeKuttaIntegrator {
        public DummyRKIntegrator(double step) {
            super("dummy", new double[] { 0.5 }, new double[][] { { 0.5 } }, new double[] { 0.0, 1.0 },
                    new MidpointStepInterpolator(), step);
        }
    }

    @Test
    public void testForwardIntegration() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double tEnd = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, tEnd, 1e-10);
        Assert.assertEquals(Math.exp(1.0), y[0], 1e-4);
    }

    @Test
    public void testBackwardIntegration() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[] { Math.exp(1.0) };
        double[] y = new double[1];
        double tEnd = integrator.integrate(ode, 1.0, y0, 0.0, y);
        Assert.assertEquals(0.0, tEnd, 1e-10);
        Assert.assertEquals(1.0, y[0], 1e-4);
    }

    @Test
    public void testSameArrayYAndY0() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new EulerIntegrator(0.01);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[] { 1.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        Assert.assertEquals(1.0, tEnd, 1e-10);
        Assert.assertTrue(y[0] > 2.5 && y[0] < 2.8);
    }

    @Test
    public void testGillIntegrator() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new GillIntegrator(0.05);
        FirstOrderDifferentialEquations ode = new MultiDimEquations();
        double[] y0 = new double[] { 0.0, 1.0 };
        double[] y = new double[2];
        integrator.integrate(ode, 0.0, y0, Math.PI, y);
        Assert.assertEquals(0.0, y[0], 1e-3);
        Assert.assertEquals(-1.0, y[1], 1e-3);
    }

    @Test
    public void testMidpointIntegrator() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new MidpointIntegrator(0.05);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(Math.exp(1.0), y[0], 1e-2);
    }

    @Test
    public void testStepHandlerExecution() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        final int[] stepCount = new int[] { 0 };
        final int[] resetCount = new int[] { 0 };

        integrator.addStepHandler(new StepHandler() {
            public boolean isRequiresDenseOutput() {
                return true;
            }

            public void reset() {
                resetCount[0]++;
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
                double[] interpolated = interpolator.getInterpolatedState();
                Assert.assertNotNull(interpolated);
            }
        });

        double[] y = new double[1];
        integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(1, resetCount[0]);
        Assert.assertTrue(stepCount[0] >= 10);
    }

    @Test
    public void testEventHandlerStop() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {
            }
        }, Double.POSITIVE_INFINITY, 1e-6, 100);

        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(0.5, stopTime, 1e-4);
    }

    @Test
    public void testEventHandlerResetState() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_STATE;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {
                y[0] = 10.0;
            }
        }, Double.POSITIVE_INFINITY, 1e-6, 100);

        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1e-10);
        Assert.assertTrue(y[0] > 10.0);
    }

    @Test
    public void testEventHandlerResetDerivatives() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_DERIVATIVES;
            }

            public double g(double t, double[] y) {
                return t - 0.3;
            }

            public void resetState(double t, double[] y) {
            }
        }, Double.POSITIVE_INFINITY, 1e-6, 100);

        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1e-10);
    }

    @Test
    public void testEventAtBoundarySmallDt() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }

            public double g(double t, double[] y) {
                return t - 0.0;
            }

            public void resetState(double t, double[] y) {
            }
        }, Double.POSITIVE_INFINITY, 1e-15, 100);

        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1e-10);
    }

    @Test(expected = IntegratorException.class)
    public void testDimensionsMismatch() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.integrate(ode, 0.0, new double[] { 1.0, 2.0 }, 1.0, new double[2]);
    }

    @Test(expected = IntegratorException.class)
    public void testOutputDimensionMismatch() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, new double[2]);
    }

    @Test(expected = IntegratorException.class)
    public void testIntegrationTimeTooClose() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        integrator.integrate(ode, 1.0, new double[] { 1.0 }, 1.0, new double[1]);
    }

    @Test(expected = DerivativeException.class)
    public void testDerivativeExceptionForwarding() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                throw new DerivativeException("Derivative error", new Object[0]);
            }
        };
        integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, new double[1]);
    }

    @Test
    public void testDummyIntegrator() throws DerivativeException, IntegratorException {
        RungeKuttaIntegrator integrator = new DummyRKIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1e-10);
        Assert.assertEquals(Math.exp(1.0), y[0], 1e-2);
    }
}
