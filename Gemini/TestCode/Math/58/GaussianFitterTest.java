package org.apache.commons.math.optimization.fitting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.analysis.ParametricUnivariateRealFunction;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.ZeroException;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class GaussianFitterTest {

    private static class DummyOptimizer implements DifferentiableMultivariateVectorialOptimizer {
        private DifferentiableMultivariateVectorialFunction lastFunction;
        private double[] lastTarget;
        private double[] lastWeights;
        private double[] lastStartPoint;

        public VectorialPointValuePair optimize(int maxEval, DifferentiableMultivariateVectorialFunction f,
                                                double[] target, double[] weights, double[] startPoint) {
            this.lastFunction = f;
            this.lastTarget = target;
            this.lastWeights = weights;
            this.lastStartPoint = startPoint;
            return new VectorialPointValuePair(startPoint, target);
        }

        public int getMaxEvaluations() {
            return 0;
        }

        public int getEvaluations() {
            return 0;
        }

        public int getMaxIterations() {
            return 0;
        }

        public int getIterations() {
            return 0;
        }

        public org.apache.commons.math.optimization.ConvergenceChecker<VectorialPointValuePair> getConvergenceChecker() {
            return null;
        }

        public void setMaxEvaluations(int maxEvaluations) {}

        public void setMaxIterations(int maxIterations) {}

        public void setConvergenceChecker(org.apache.commons.math.optimization.ConvergenceChecker<VectorialPointValuePair> checker) {}
    }

    @Test
    public void testFitWithInitialGuess() {
        DummyOptimizer optimizer = new DummyOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);
        fitter.addObservedPoint(1.0, 1.0, 2.0);
        fitter.addObservedPoint(1.0, 2.0, 4.0);
        fitter.addObservedPoint(1.0, 3.0, 2.0);

        double[] initialGuess = new double[] { 4.0, 2.0, 1.0 };
        double[] result = fitter.fit(initialGuess);
        Assert.assertArrayEquals(initialGuess, result, 1e-9);

        double[] val = optimizer.lastFunction.value(initialGuess);
        Assert.assertEquals(3, val.length);

        MultivariateMatrixFunction jacobian = optimizer.lastFunction.jacobian();
        double[][] jac = jacobian.value(initialGuess);
        Assert.assertEquals(3, jac.length);
        Assert.assertEquals(3, jac[0].length);

        double[] invalidParams = new double[] { 4.0, 2.0, -1.0 };
        double[] valInvalid = optimizer.lastFunction.value(invalidParams);
        for (double v : valInvalid) {
            Assert.assertEquals(Double.POSITIVE_INFINITY, v, 1e-9);
        }

        double[][] jacInvalid = jacobian.value(invalidParams);
        for (double[] row : jacInvalid) {
            for (double entry : row) {
                Assert.assertEquals(Double.POSITIVE_INFINITY, entry, 1e-9);
            }
        }
    }

    @Test
    public void testFitWithoutGuess() {
        DummyOptimizer optimizer = new DummyOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);
        fitter.addObservedPoint(1.0, 1.0, 2.0);
        fitter.addObservedPoint(1.0, 2.0, 10.0);
        fitter.addObservedPoint(1.0, 3.0, 2.0);

        double[] result = fitter.fit();
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(10.0, result[0], 1e-9);
        Assert.assertEquals(2.0, result[1], 1e-9);
        Assert.assertTrue(result[2] > 0.0);
    }

    @Test(expected = NullArgumentException.class)
    public void testParameterGuesserNullObservations() {
        new GaussianFitter.ParameterGuesser(null);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesserLessThanThreePoints() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 4.0)
        };
        new GaussianFitter.ParameterGuesser(points);
    }

    @Test
    public void testParameterGuesserValid() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 8.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0),
            new WeightedObservedPoint(1.0, 4.0, 0.5)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess1 = guesser.guess();
        double[] guess2 = guesser.guess();
        Assert.assertArrayEquals(guess1, guess2, 1e-9);
        Assert.assertNotSame(guess1, guess2);
        Assert.assertEquals(8.0, guess1[0], 1e-9);
        Assert.assertEquals(2.0, guess1[1], 1e-9);
        Assert.assertTrue(guess1[2] > 0.0);
    }

    @Test
    public void testBasicGuessFwhmFallbackOnOutOfRange() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 10.0),
            new WeightedObservedPoint(1.0, 2.0, 20.0),
            new WeightedObservedPoint(1.0, 3.0, 30.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        Assert.assertEquals(30.0, guess[0], 1e-9);
        Assert.assertEquals(3.0, guess[1], 1e-9);
        double expectedFwhm = 3.0 - 1.0;
        double expectedSigma = expectedFwhm / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
        Assert.assertEquals(expectedSigma, guess[2], 1e-9);
    }

    @Test
    public void testComparatorBranches() throws Exception {
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 3.0)
        });
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod("createWeightedObservedPointComparator");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Comparator<WeightedObservedPoint> comp = (Comparator<WeightedObservedPoint>) m.invoke(guesser);

        Assert.assertEquals(0, comp.compare(null, null));
        Assert.assertEquals(-1, comp.compare(null, new WeightedObservedPoint(1.0, 0.0, 0.0)));
        Assert.assertEquals(1, comp.compare(new WeightedObservedPoint(1.0, 0.0, 0.0), null));

        Assert.assertEquals(-1, comp.compare(new WeightedObservedPoint(1.0, 1.0, 0.0), new WeightedObservedPoint(1.0, 2.0, 0.0)));
        Assert.assertEquals(1, comp.compare(new WeightedObservedPoint(1.0, 2.0, 0.0), new WeightedObservedPoint(1.0, 1.0, 0.0)));

        Assert.assertEquals(-1, comp.compare(new WeightedObservedPoint(1.0, 1.0, 1.0), new WeightedObservedPoint(1.0, 1.0, 2.0)));
        Assert.assertEquals(1, comp.compare(new WeightedObservedPoint(1.0, 1.0, 2.0), new WeightedObservedPoint(1.0, 1.0, 1.0)));

        Assert.assertEquals(-1, comp.compare(new WeightedObservedPoint(1.0, 1.0, 1.0), new WeightedObservedPoint(2.0, 1.0, 1.0)));
        Assert.assertEquals(1, comp.compare(new WeightedObservedPoint(2.0, 1.0, 1.0), new WeightedObservedPoint(1.0, 1.0, 1.0)));
        Assert.assertEquals(0, comp.compare(new WeightedObservedPoint(1.0, 1.0, 1.0), new WeightedObservedPoint(1.0, 1.0, 1.0)));
    }

    @Test
    public void testInterpolateXAtYZeroStep() throws Exception {
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 3.0)
        });
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod("interpolateXAtY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);
        try {
            m.invoke(guesser, new WeightedObservedPoint[] {
                new WeightedObservedPoint(1.0, 1.0, 1.0),
                new WeightedObservedPoint(1.0, 2.0, 2.0),
                new WeightedObservedPoint(1.0, 3.0, 3.0)
            }, 1, 0, 1.5);
            Assert.fail();
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof ZeroException);
        }
    }

    @Test
    public void testGetInterpolationPointsForYZeroStep() throws Exception {
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 3.0)
        });
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod("getInterpolationPointsForY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);
        try {
            m.invoke(guesser, new WeightedObservedPoint[] {
                new WeightedObservedPoint(1.0, 1.0, 1.0),
                new WeightedObservedPoint(1.0, 2.0, 2.0),
                new WeightedObservedPoint(1.0, 3.0, 3.0)
            }, 1, 0, 1.5);
            Assert.fail();
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof ZeroException);
        }
    }

    @Test
    public void testInterpolateXAtYExactMatches() throws Exception {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 10.0, 1.0),
            new WeightedObservedPoint(1.0, 20.0, 3.0),
            new WeightedObservedPoint(1.0, 30.0, 5.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod("interpolateXAtY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);

        double xExactA = (Double) m.invoke(guesser, points, 1, 1, 3.0);
        Assert.assertEquals(20.0, xExactA, 1e-9);

        double xExactB = (Double) m.invoke(guesser, points, 1, 1, 5.0);
        Assert.assertEquals(30.0, xExactB, 1e-9);

        double xMid = (Double) m.invoke(guesser, points, 1, 1, 4.0);
        Assert.assertEquals(25.0, xMid, 1e-9);
    }
}
