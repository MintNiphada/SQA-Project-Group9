package org.apache.commons.math3.optimization.fitting;

import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.ZeroException;
import org.junit.Test;
import org.junit.Assert;

public class HarmonicFitterTest {

    private static class DummyOptimizer extends DifferentiableMultivariateVectorOptimizer {
        public DummyOptimizer() {
            super(new SimpleValueChecker(1e-6, 1e-6));
        }
        @Override
        public PointVectorValuePair doOptimize() {
            return new PointVectorValuePair(getStartPoint(), null);
        }
    }

    @Test
    public void testConstructor() {
        new HarmonicFitter(new DummyOptimizer());
    }

    @Test
    public void testFitWithInitialGuess() {
        HarmonicFitter fitter = new HarmonicFitter(new DummyOptimizer());
        fitter.addObservedPoint(0.0, 1.0);
        fitter.addObservedPoint(1.0, 2.0);
        fitter.addObservedPoint(2.0, 3.0);
        fitter.addObservedPoint(3.0, 4.0);
        double[] initialGuess = {1.0, 2.0, 3.0};
        double[] result = fitter.fit(initialGuess);
        Assert.assertArrayEquals(initialGuess, result, 1e-10);
    }

    @Test
    public void testFitWithGuesser() {
        HarmonicFitter fitter = new HarmonicFitter(new DummyOptimizer());
        double a = 2.0;
        double omega = 3.0;
        double phi = 0.5;
        for (double t = 0.0; t < 2.0; t += 0.1) {
            double y = a * Math.cos(omega * t + phi);
            fitter.addObservedPoint(t, y);
        }
        double[] result = fitter.fit();
        Assert.assertEquals(a, result[0], 0.1);
        Assert.assertEquals(omega, result[1], 0.1);
        Assert.assertEquals(phi, result[2], 0.1);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesserConstructorThrowsOnInsufficientPoints() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[3];
        points[0] = new WeightedObservedPoint(1.0, 0.0, 1.0);
        points[1] = new WeightedObservedPoint(1.0, 1.0, 2.0);
        points[2] = new WeightedObservedPoint(1.0, 2.0, 3.0);
        new HarmonicFitter.ParameterGuesser(points);
    }

    @Test
    public void testParameterGuesserGuess() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[4];
        points[0] = new WeightedObservedPoint(1.0, 0.0, 1.0);
        points[1] = new WeightedObservedPoint(1.0, 0.5, 0.0);
        points[2] = new WeightedObservedPoint(1.0, 1.0, -1.0);
        points[3] = new WeightedObservedPoint(1.0, 1.5, 0.0);
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        Assert.assertEquals(3, guess.length);
        Assert.assertTrue(guess[0] > 0);
        Assert.assertTrue(guess[1] > 0);
    }

    @Test(expected = ZeroException.class)
    public void testParameterGuesserGuessWithZeroXRange() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[4];
        points[0] = new WeightedObservedPoint(1.0, 1.0, 1.0);
        points[1] = new WeightedObservedPoint(1.0, 1.0, 2.0);
        points[2] = new WeightedObservedPoint(1.0, 1.0, 3.0);
        points[3] = new WeightedObservedPoint(1.0, 1.0, 4.0);
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        guesser.guess();
    }

    @Test
    public void testParameterGuesserGuessFallback() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[4];
        points[0] = new WeightedObservedPoint(1.0, 0.0, 0.0);
        points[1] = new WeightedObservedPoint(1.0, 1.0, 1.0);
        points[2] = new WeightedObservedPoint(1.0, 2.0, 0.0);
        points[3] = new WeightedObservedPoint(1.0, 3.0, 1.0);
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        double xRange = points[3].getX() - points[0].getX();
        double expectedOmega = 2 * Math.PI / xRange;
        double yMin = 0.0;
        double yMax = 1.0;
        double expectedA = 0.5 * (yMax - yMin);
        Assert.assertEquals(expectedA, guess[0], 1e-10);
        Assert.assertEquals(expectedOmega, guess[1], 1e-10);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testFitWithNoObservationsThrows() {
        HarmonicFitter fitter = new HarmonicFitter(new DummyOptimizer());
        fitter.fit();
    }
}
