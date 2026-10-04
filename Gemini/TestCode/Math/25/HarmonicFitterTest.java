package org.apache.commons.math3.optimization.fitting;

import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class HarmonicFitterTest {

    @Test
    public void testFitWithInitialGuess() {
        HarmonicOscillator f = new HarmonicOscillator(1.2, 3.4, 0.5);
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());

        for (double x = 0.0; x < 10.0; x += 0.2) {
            fitter.addObservedPoint(1.0, x, f.value(x));
        }

        double[] initialGuess = new double[] { 1.0, 3.0, 0.0 };
        double[] fitted = fitter.fit(initialGuess);

        Assert.assertEquals(1.2, fitted[0], 1e-4);
        Assert.assertEquals(3.4, fitted[1], 1e-4);
        Assert.assertEquals(0.5, fitted[2] % (2 * FastMath.PI), 1e-4);
    }

    @Test
    public void testFitAutomaticGuess() {
        double a = 2.5;
        double omega = 1.8;
        double phi = 0.7;
        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());

        for (double x = 0.0; x < 12.0; x += 0.1) {
            fitter.addObservedPoint(1.0, x, f.value(x));
        }

        double[] fitted = fitter.fit();

        Assert.assertEquals(a, fitted[0], 1e-3);
        Assert.assertEquals(omega, fitted[1], 1e-3);
        Assert.assertEquals(phi, (fitted[2] + 2 * FastMath.PI) % (2 * FastMath.PI), 1e-3);
    }

    @Test
    public void testFitWithNoise() {
        double a = 3.0;
        double omega = 0.8;
        double phi = 1.2;
        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());

        Random random = new Random(42L);
        for (double x = 0.0; x < 20.0; x += 0.1) {
            double noise = (random.nextDouble() - 0.5) * 0.05;
            fitter.addObservedPoint(1.0, x, f.value(x) + noise);
        }

        double[] fitted = fitter.fit();

        Assert.assertEquals(a, fitted[0], 0.1);
        Assert.assertEquals(omega, fitted[1], 0.1);
        Assert.assertEquals(phi, (fitted[2] + 2 * FastMath.PI) % (2 * FastMath.PI), 0.1);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testGuesserTooFewPoints() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 1.0)
        };
        new HarmonicFitter.ParameterGuesser(points);
    }

    @Test
    public void testGuesserAccurate() {
        double a = 4.0;
        double omega = 2.0;
        double phi = 1.0;
        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);

        int n = 100;
        WeightedObservedPoint[] points = new WeightedObservedPoint[n];
        for (int i = 0; i < n; ++i) {
            double x = i * 0.1;
            points[i] = new WeightedObservedPoint(1.0, x, f.value(x));
        }

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertEquals(a, guess[0], 0.1);
        Assert.assertEquals(omega, guess[1], 0.1);
        Assert.assertEquals(phi, (guess[2] + 2 * FastMath.PI) % (2 * FastMath.PI), 0.2);
    }

    @Test
    public void testGuesserSorting() {
        double a = 2.0;
        double omega = 1.5;
        double phi = 0.5;
        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);

        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 5.0, f.value(5.0)),
            new WeightedObservedPoint(1.0, 1.0, f.value(1.0)),
            new WeightedObservedPoint(1.0, 0.0, f.value(0.0)),
            new WeightedObservedPoint(1.0, 4.0, f.value(4.0)),
            new WeightedObservedPoint(1.0, 2.0, f.value(2.0)),
            new WeightedObservedPoint(1.0, 3.0, f.value(3.0))
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertTrue(guess[0] > 0);
        Assert.assertTrue(guess[1] > 0);
    }

    @Test
    public void testGuesserReverseSorted() {
        double a = 3.0;
        double omega = 1.0;
        double phi = 0.2;
        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);

        WeightedObservedPoint[] points = new WeightedObservedPoint[10];
        for (int i = 0; i < 10; ++i) {
            double x = 10.0 - i;
            points[i] = new WeightedObservedPoint(1.0, x, f.value(x));
        }

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertTrue(guess[0] > 0);
        Assert.assertTrue(guess[1] > 0);
    }

    @Test
    public void testGuesserFallbackBranch() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 10.0),
            new WeightedObservedPoint(1.0, 1.0, 10.0),
            new WeightedObservedPoint(1.0, 2.0, 10.0),
            new WeightedObservedPoint(1.0, 3.0, 10.0),
            new WeightedObservedPoint(1.0, 4.0, 20.0),
            new WeightedObservedPoint(1.0, 5.0, 20.0),
            new WeightedObservedPoint(1.0, 6.0, 20.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertNotNull(guess);
        Assert.assertEquals(3, guess.length);
        Assert.assertTrue(guess[0] >= 0);
        Assert.assertTrue(guess[1] > 0);
    }

    @Test(expected = ZeroException.class)
    public void testGuesserZeroAbscissaRangeThrowsZeroException() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 2.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 3.0),
            new WeightedObservedPoint(1.0, 2.0, 4.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        guesser.guess();
    }

    @Test(expected = ZeroException.class)
    public void testFitZeroAbscissaRangePropagatesZeroException() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(1.0, 1.0, 5.0);
        fitter.addObservedPoint(1.0, 1.0, 6.0);
        fitter.addObservedPoint(1.0, 1.0, 7.0);
        fitter.addObservedPoint(1.0, 1.0, 8.0);

        fitter.fit();
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testFitInsufficientObservedPoints() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(1.0, 1.0, 5.0);
        fitter.addObservedPoint(1.0, 2.0, 6.0);

        fitter.fit();
    }
}
