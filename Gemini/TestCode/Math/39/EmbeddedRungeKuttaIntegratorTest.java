package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.ExpandableStatefulODE;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    private static class DummyRKInterpolator extends RungeKuttaStepInterpolator {
        public DummyRKInterpolator() {
            super();
        }

        public DummyRKInterpolator(DummyRKInterpolator interpolator) {
            super(interpolator);
        }

        @Override
        protected StepInterpolator doCopy() {
            return new DummyRKInterpolator(this);
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            System.arraycopy(currentState, 0, interpolatedState, 0, currentState.length);
            System.arraycopy(yDotK[0], 0, interpolatedDerivatives, 0, yDotK[0].length);
        }
    }

    private static class DummyRKIntegrator extends EmbeddedRungeKuttaIntegrator {
        private double errorValue = 0.5;

        public DummyRKIntegrator(boolean fsal, double[] c, double[][] a, double[] b,
                                 double minStep, double maxStep,
                                 double scalAbsoluteTolerance, double scalRelativeTolerance) {
            super("dummy", fsal, c, a, b, new DummyRKInterpolator(), minStep, maxStep,
                  scalAbsoluteTolerance, scalRelativeTolerance);
        }

        public DummyRKIntegrator(boolean fsal, double[] c, double[][] a, double[] b,
                                 double minStep, double maxStep,
                                 double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) {
            super("dummy", fsal, c, a, b, new DummyRKInterpolator(), minStep, maxStep,
                  vecAbsoluteTolerance, vecRelativeTolerance);
        }

        public void setErrorValue(double errorValue) {
            this.errorValue = errorValue;
        }

        @Override
        public int getOrder() {
            return 2;
        }

        @Override
        protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
            return errorValue;
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

    @Test
    public void testGettersAndSetters() {
        double[] c = new double[]{0.5, 1.0};
        double[][] a = new double[][]{{0.5}, {0.0, 1.0}};
        double[] b = new double[]{1.0 / 6.0, 2.0 / 3.0, 1.0 / 6.0};

        DummyRKIntegrator integrator = new DummyRKIntegrator(false, c, a, b, 0.001, 1.0, 1.0e-6, 1.0e-6);

        Assert.assertEquals(0.9, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);

        integrator.setSafety(0.85);
        integrator.setMinReduction(0.15);
        integrator.setMaxGrowth(8.0);

        Assert.assertEquals(0.85, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(8.0, integrator.getMaxGrowth(), 1.0e-12);
    }

    @Test
    public void testScalarToleranceForwardIntegrationNonFSAL() {
        double[] c = new double[]{0.5, 1.0};
        double[][] a = new double[][]{{0.5}, {0.0, 1.0}};
        double[] b = new double[]{1.0 / 6.0, 2.0 / 3.0, 1.0 / 6.0};

        DummyRKIntegrator integrator = new DummyRKIntegrator(false, c, a, b, 0.001, 1.0, 1.0e-6, 1.0e-6);
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new SimpleODE());
        ode.setTime(0.0);
        ode.setPrimaryState(new double[]{1.0});

        integrator.integrate(ode, 1.0);

        Assert.assertEquals(1.0, ode.getTime(), 1.0e-12);
        Assert.assertTrue(ode.getPrimaryState()[0] < 1.0);
    }

    @Test
    public void testVectorToleranceBackwardIntegrationFSAL() {
        double[] c = new double[]{0.5, 1.0};
        double[][] a = new double[][]{{0.5}, {0.0, 1.0}};
        double[] b = new double[]{1.0 / 6.0, 2.0 / 3.0, 1.0 / 6.0};

        double[] vecAbsTol = new double[]{1.0e-6};
        double[] vecRelTol = new double[]{1.0e-6};
        DummyRKIntegrator integrator = new DummyRKIntegrator(true, c, a, b, 0.001, 1.0, vecAbsTol, vecRelTol);
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new SimpleODE());
        ode.setTime(1.0);
        ode.setPrimaryState(new double[]{0.367879});

        integrator.integrate(ode, 0.0);

        Assert.assertEquals(0.0, ode.getTime(), 1.0e-12);
        Assert.assertTrue(ode.getPrimaryState()[0] > 0.367879);
    }

    @Test
    public void testStepRejectionAndRecovery() {
        double[] c = new double[]{0.5, 1.0};
        double[][] a = new double[][]{{0.5}, {0.0, 1.0}};
        double[] b = new double[]{1.0 / 6.0, 2.0 / 3.0, 1.0 / 6.0};

        final DummyRKIntegrator integrator = new DummyRKIntegrator(true, c, a, b, 0.001, 1.0, 1.0e-6, 1.0e-6);
        integrator.setErrorValue(2.0);

        integrator.addStepHandler(new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                integrator.setErrorValue(0.1);
            }
        });

        ExpandableStatefulODE ode = new ExpandableStatefulODE(new SimpleODE());
        ode.setTime(0.0);
        ode.setPrimaryState(new double[]{1.0});

        integrator.integrate(ode, 1.0);
        Assert.assertEquals(1.0, ode.getTime(), 1.0e-12);
    }
}
