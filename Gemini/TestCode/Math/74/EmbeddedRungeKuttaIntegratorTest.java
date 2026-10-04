package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    private static class DummyRKIntegrator extends EmbeddedRungeKuttaIntegrator {
        private double error = 0.5;
        private int errorEvalCount = 0;
        private double fixedErrorAfterFirst = -1;

        public DummyRKIntegrator(boolean fsal, double minStep, double maxStep, double scalAbs, double scalRel) {
            super("DummyRK", fsal, new double[]{1.0}, new double[][]{{1.0}}, new double[]{0.5, 0.5},
                  new DormandPrince54StepInterpolator(), minStep, maxStep, scalAbs, scalRel);
        }

        public DummyRKIntegrator(boolean fsal, double minStep, double maxStep, double[] vecAbs, double[] vecRel) {
            super("DummyRK", fsal, new double[]{1.0}, new double[][]{{1.0}}, new double[]{0.5, 0.5},
                  new DormandPrince54StepInterpolator(), minStep, maxStep, vecAbs, vecRel);
        }

        public void setError(double error) {
            this.error = error;
        }

        public void setFixedErrorAfterFirst(double err) {
            this.fixedErrorAfterFirst = err;
        }

        @Override
        public int getOrder() {
            return 2;
        }

        @Override
        protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
            errorEvalCount++;
            if (errorEvalCount > 1 && fixedErrorAfterFirst >= 0) {
                return fixedErrorAfterFirst;
            }
            return error;
        }
    }

    private static class LinearEquations implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 2.0;
        }
    }

    private static class MultiLinearEquations implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 2;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
            yDot[1] = 2.0;
        }
    }

    @Test
    public void testGettersAndSetters() {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 10.0, 1.0e-6, 1.0e-6);

        Assert.assertEquals(0.9, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);
        Assert.assertEquals(2, integrator.getOrder());

        integrator.setSafety(0.85);
        integrator.setMinReduction(0.15);
        integrator.setMaxGrowth(8.0);

        Assert.assertEquals(0.85, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(8.0, integrator.getMaxGrowth(), 1.0e-12);
    }

    @Test
    public void testScalarToleranceIntegrationForward() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, 1.0e-6, 1.0e-6);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[]{0.0};
        double[] y = new double[1];

        double tEnd = integrator.integrate(ode, 0.0, y0, 2.0, y);
        Assert.assertEquals(2.0, tEnd, 1.0e-12);
        Assert.assertEquals(4.0, y[0], 1.0e-6);
    }

    @Test
    public void testVectorToleranceIntegrationBackward() throws DerivativeException, IntegratorException {
        double[] vecAbs = new double[]{1.0e-6, 1.0e-6};
        double[] vecRel = new double[]{1.0e-6, 1.0e-6};
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, vecAbs, vecRel);
        FirstOrderDifferentialEquations ode = new MultiLinearEquations();
        double[] y0 = new double[]{4.0, 8.0};
        double[] y = new double[2];

        double tEnd = integrator.integrate(ode, 2.0, y0, 0.0, y);
        Assert.assertEquals(0.0, tEnd, 1.0e-12);
        Assert.assertEquals(2.0, y[0], 1.0e-6);
        Assert.assertEquals(4.0, y[1], 1.0e-6);
    }

    @Test
    public void testSameArrayYAndY0() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(true, 0.001, 1.0, 1.0e-6, 1.0e-6);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[]{0.0};

        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        Assert.assertEquals(1.0, tEnd, 1.0e-12);
        Assert.assertEquals(2.0, y[0], 1.0e-6);
    }

    @Test
    public void testFsalProperty() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(true, 0.001, 0.5, 1.0e-6, 1.0e-6);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[]{1.0};
        double[] y = new double[1];

        double tEnd = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, tEnd, 1.0e-12);
        Assert.assertEquals(3.0, y[0], 1.0e-6);
    }

    @Test
    public void testErrorStepRejection() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.0001, 1.0, 1.0e-6, 1.0e-6);
        integrator.setError(2.0);
        integrator.setFixedErrorAfterFirst(0.5);
        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y0 = new double[]{0.0};
        double[] y = new double[1];

        double tEnd = integrator.integrate(ode, 0.0, y0, 1.0, y);
        Assert.assertEquals(1.0, tEnd, 1.0e-12);
        Assert.assertEquals(2.0, y[0], 1.0e-5);
    }

    @Test
    public void testDenseOutputStepHandler() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, 1.0e-6, 1.0e-6);
        final boolean[] handlerCalled = new boolean[]{false, false};

        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return true;
            }

            public void reset() {
                handlerCalled[0] = true;
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                handlerCalled[1] = true;
                Assert.assertTrue(interpolator.getCurrentTime() >= interpolator.getPreviousTime());
            }
        });

        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, new double[]{0.0}, 1.0, y);

        Assert.assertTrue(handlerCalled[0]);
        Assert.assertTrue(handlerCalled[1]);
    }

    @Test
    public void testEventStoppingIntegration() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, 1.0e-6, 1.0e-6);
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {
            }
        }, 0.1, 1.0e-6, 100);

        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[]{0.0}, 2.0, y);

        Assert.assertEquals(0.5, stopTime, 1.0e-5);
        Assert.assertEquals(1.0, y[0], 1.0e-4);
    }

    @Test
    public void testEventResetStateAndDerivatives() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(true, 0.001, 0.2, 1.0e-6, 1.0e-6);
        final boolean[] stateReset = new boolean[]{false};

        integrator.addEventHandler(new EventHandler() {
            private boolean triggered = false;

            public int eventOccurred(double t, double[] y, boolean increasing) {
                triggered = true;
                return RESET_STATE;
            }

            public double g(double t, double[] y) {
                return triggered ? 1.0 : (t - 0.4);
            }

            public void resetState(double t, double[] y) {
                y[0] += 5.0;
                stateReset[0] = true;
            }
        }, 0.01, 1.0e-6, 100);

        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[]{0.0}, 1.0, y);

        Assert.assertEquals(1.0, stopTime, 1.0e-6);
        Assert.assertTrue(stateReset[0]);
        Assert.assertEquals(7.0, y[0], 1.0e-4);
    }

    @Test
    public void testEventAtStartUlp() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, 1.0e-6, 1.0e-6);
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }

            public double g(double t, double[] y) {
                return t;
            }

            public void resetState(double t, double[] y) {
            }
        }, 0.1, 1.0e-15, 100);

        FirstOrderDifferentialEquations ode = new LinearEquations();
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[]{0.0}, 1.0, y);

        Assert.assertEquals(1.0, stopTime, 1.0e-6);
    }

    @Test(expected = IntegratorException.class)
    public void testDimensionMismatch() throws DerivativeException, IntegratorException {
        DummyRKIntegrator integrator = new DummyRKIntegrator(false, 0.001, 1.0, 1.0e-6, 1.0e-6);
        FirstOrderDifferentialEquations ode = new MultiLinearEquations();
        integrator.integrate(ode, 0.0, new double[]{0.0}, 1.0, new double[1]);
    }
}
