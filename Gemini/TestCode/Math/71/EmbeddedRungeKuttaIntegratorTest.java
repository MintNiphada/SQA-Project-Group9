package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    private static class DummyRkInterpolator extends RungeKuttaStepInterpolator {
        private static final long serialVersionUID = 1L;

        public DummyRkInterpolator() {
            super();
        }

        public DummyRkInterpolator(DummyRkInterpolator interpolator) {
            super(interpolator);
        }

        @Override
        protected StepInterpolator doCopy() {
            return new DummyRkInterpolator(this);
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            System.arraycopy(currentState, 0, interpolatedState, 0, currentState.length);
            System.arraycopy(yDotK[0], 0, interpolatedDerivatives, 0, yDotK[0].length);
        }
    }

    private static class TestEmbeddedRK extends EmbeddedRungeKuttaIntegrator {
        private double fixedError = 0.5;
        private int errorEvalCount = 0;

        public TestEmbeddedRK(String name, boolean fsal, double[] c, double[][] a, double[] b,
                              RungeKuttaStepInterpolator prototype, double minStep, double maxStep,
                              double scalAbsoluteTolerance, double scalRelativeTolerance) {
            super(name, fsal, c, a, b, prototype, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
        }

        public TestEmbeddedRK(String name, boolean fsal, double[] c, double[][] a, double[] b,
                              RungeKuttaStepInterpolator prototype, double minStep, double maxStep,
                              double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) {
            super(name, fsal, c, a, b, prototype, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
        }

        public void setFixedError(double error) {
            this.fixedError = error;
        }

        @Override
        public int getOrder() {
            return 2;
        }

        @Override
        protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
            errorEvalCount++;
            if (fixedError < 0) {
                return (errorEvalCount == 1) ? 2.0 : 0.5;
            }
            return fixedError;
        }
    }

    private static class SimpleODE implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = -y[0];
        }
    }

    private static class VectorODE implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 2;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[1];
            yDot[1] = -y[0];
        }
    }

    @Test
    public void testGettersAndSetters() {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", false, c, a, b, new DummyRkInterpolator(),
                1.0e-3, 10.0, 1.0e-6, 1.0e-6);

        Assert.assertEquals(0.9, rk.getSafety(), 1.0e-12);
        Assert.assertEquals(0.2, rk.getMinReduction(), 1.0e-12);
        Assert.assertEquals(10.0, rk.getMaxGrowth(), 1.0e-12);

        rk.setSafety(0.8);
        rk.setMinReduction(0.1);
        rk.setMaxGrowth(5.0);

        Assert.assertEquals(0.8, rk.getSafety(), 1.0e-12);
        Assert.assertEquals(0.1, rk.getMinReduction(), 1.0e-12);
        Assert.assertEquals(5.0, rk.getMaxGrowth(), 1.0e-12);
    }

    @Test
    public void testIntegrateScalarForward() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", false, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = rk.integrate(new SimpleODE(), 0.0, y0, 1.0, y);

        Assert.assertEquals(1.0, stopTime, 1.0e-10);
        Assert.assertTrue(y[0] < 1.0);
    }

    @Test
    public void testIntegrateVectorToleranceAndBackward() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        double[] vecAbsTol = new double[]{1.0e-6, 1.0e-6};
        double[] vecRelTol = new double[]{1.0e-6, 1.0e-6};

        TestEmbeddedRK rk = new TestEmbeddedRK("test", true, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, vecAbsTol, vecRelTol);

        double[] y = new double[]{1.0, 0.0};
        double stopTime = rk.integrate(new VectorODE(), 1.0, y, 0.0, y);

        Assert.assertEquals(0.0, stopTime, 1.0e-10);
    }

    @Test
    public void testIntegrateWithStepHandlerAndDenseOutput() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", false, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final boolean[] handled = new boolean[]{false};
        rk.addStepHandler(new StepHandler() {
            @Override
            public boolean isResetDerivatives() {
                return false;
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                if (isLast) {
                    handled[0] = true;
                }
            }

            @Override
            public void reset() {
            }
        });

        double[] y = new double[]{1.0};
        rk.integrate(new SimpleODE(), 0.0, y, 1.0, y);
        Assert.assertTrue(handled[0]);
    }

    @Test
    public void testIntegrateWithStepRejection() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", false, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        rk.setFixedError(-1.0);

        double[] y = new double[]{1.0};
        double stopTime = rk.integrate(new SimpleODE(), 0.0, y, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1.0e-10);
        Assert.assertTrue(rk.errorEvalCount > 1);
    }

    @Test
    public void testIntegrateWithEventsAndStateReset() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", true, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final boolean[] eventTriggered = new boolean[]{false};
        rk.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                eventTriggered[0] = true;
                return EventHandler.RESET_STATE;
            }

            @Override
            public void resetState(double t, double[] y) {
                y[0] = 2.0;
            }
        }, 0.1, 1.0e-4, 100);

        double[] y = new double[]{1.0};
        double stopTime = rk.integrate(new SimpleODE(), 0.0, y, 1.0, y);
        Assert.assertEquals(1.0, stopTime, 1.0e-10);
        Assert.assertTrue(eventTriggered[0]);
    }

    @Test
    public void testIntegrateWithStopEvent() throws DerivativeException, IntegratorException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        TestEmbeddedRK rk = new TestEmbeddedRK("test", false, c, a, b, new DummyRkInterpolator(),
                1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        rk.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        }, 0.1, 1.0e-4, 100);

        double[] y = new double[]{1.0};
        double stopTime = rk.integrate(new SimpleODE(), 0.0, y, 1.0, y);
        Assert.assertEquals(0.5, stopTime, 1.0e-3);
    }
}
