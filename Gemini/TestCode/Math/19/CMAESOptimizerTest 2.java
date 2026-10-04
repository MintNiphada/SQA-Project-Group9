package org.apache.commons.math3.optimization.direct;

import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimplePointChecker;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    private static class Sphere implements MultivariateFunction {
        public double value(double[] x) {
            double f = 0;
            for (double v : x) {
                f += v * v;
            }
            return f;
        }
    }

    private static class Rosenbrock implements MultivariateFunction {
        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length - 1; ++i) {
                double t1 = x[i] * x[i] - x[i + 1];
                double t2 = x[i] - 1;
                f += 100 * t1 * t1 + t2 * t2;
            }
            return f;
        }
    }

    private static class FlatFitness implements MultivariateFunction {
        public double value(double[] x) {
            return 42.0;
        }
    }

    @Test
    public void testDefaultConstructor() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        Assert.assertNotNull(optimizer);
        Assert.assertTrue(optimizer.getStatisticsSigmaHistory().isEmpty());
        Assert.assertTrue(optimizer.getStatisticsMeanHistory().isEmpty());
        Assert.assertTrue(optimizer.getStatisticsFitnessHistory().isEmpty());
        Assert.assertTrue(optimizer.getStatisticsDHistory().isEmpty());
    }

    @Test
    public void testConstructors() {
        CMAESOptimizer opt1 = new CMAESOptimizer(10);
        Assert.assertNotNull(opt1);

        double[] sigma = new double[] { 0.2, 0.2 };
        CMAESOptimizer opt2 = new CMAESOptimizer(10, sigma);
        Assert.assertNotNull(opt2);

        @SuppressWarnings("deprecation")
        CMAESOptimizer opt3 = new CMAESOptimizer(10, sigma, 100, 1e-6, true, 0, 0, new MersenneTwister(42), true);
        Assert.assertNotNull(opt3);

        CMAESOptimizer opt4 = new CMAESOptimizer(10, sigma, 100, 1e-6, true, 0, 0, new MersenneTwister(42), true, new SimpleValueChecker(1e-5, 1e-5));
        Assert.assertNotNull(opt4);
    }

    @Test
    public void testOptimizeSphereMinimize() {
        int dim = 2;
        double[] start = new double[] { 1.5, -2.0 };
        double[] inSigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 1000, 1e-10, true, 0, 0, new MersenneTwister(12345), false, new SimpleValueChecker(1e-7, 1e-7));
        PointValuePair result = optimizer.optimize(10000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testOptimizeSphereMaximize() {
        int dim = 2;
        double[] start = new double[] { 0.1, -0.1 };
        double[] inSigma = new double[] { 0.2, 0.2 };
        MultivariateFunction negSphere = new MultivariateFunction() {
            public double value(double[] x) {
                return -(x[0] * x[0] + x[1] * x[1]);
            }
        };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 500, 0.0, false, 0, 0, new MersenneTwister(54321), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(5000, negSphere, GoalType.MAXIMIZE, start);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    @Test
    public void testOptimizeWithBoundaries() {
        double[] start = new double[] { 1.0, 1.0 };
        double[] inSigma = new double[] { 0.5, 0.5 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 500, 1e-9, true, 0, 2, new MersenneTwister(11), true, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(5000, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
        Assert.assertTrue(result.getValue() < 1e-3);
        Assert.assertTrue(result.getPoint()[0] >= lower[0] && result.getPoint()[0] <= upper[0]);
        Assert.assertTrue(result.getPoint()[1] >= lower[1] && result.getPoint()[1] <= upper[1]);

        List<Double> sigmas = optimizer.getStatisticsSigmaHistory();
        List<RealMatrix> means = optimizer.getStatisticsMeanHistory();
        List<Double> fitnesses = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> dHistory = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmas.isEmpty());
        Assert.assertFalse(means.isEmpty());
        Assert.assertFalse(fitnesses.isEmpty());
        Assert.assertFalse(dHistory.isEmpty());
    }

    @Test
    public void testDiagonalOnlyMode() {
        double[] start = new double[] { 2.0, -1.0 };
        double[] inSigma = new double[] { 0.3, 0.3 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 300, 1e-8, false, 1, 0, new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(4000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertTrue(result.getValue() < 1e-2);
    }

    @Test
    public void testDiagonalOnlyTransitionToFullCovariance() {
        double[] start = new double[] { 1.5, 1.5 };
        double[] inSigma = new double[] { 0.3, 0.3 };
        // diagonalOnly = 2, so after iteration 2, it switches to diagonalOnly = 0
        CMAESOptimizer optimizer = new CMAESOptimizer(12, inSigma, 300, 1e-8, true, 2, 0, new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(4000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertTrue(result.getValue() < 1e-2);
    }

    @Test
    public void testNonActiveCMA() {
        double[] start = new double[] { 1.0, -1.0 };
        double[] inSigma = new double[] { 0.2, 0.2 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 200, 1e-8, false, 0, 0, new MersenneTwister(99), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(3000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertTrue(result.getValue() < 1e-2);
    }

    @Test
    public void testFlatFitnessFunction() {
        double[] start = new double[] { 0.0, 0.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 15, 0.0, true, 0, 0, new MersenneTwister(1), false, null);
        PointValuePair result = optimizer.optimize(100, new FlatFitness(), GoalType.MINIMIZE, start);
        Assert.assertEquals(42.0, result.getValue(), 1e-9);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testInputSigmaDimensionMismatch() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] inSigma = new double[] { 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start);
    }

    @Test(expected = NotPositiveException.class)
    public void testInputSigmaNegative() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] inSigma = new double[] { 0.5, -0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRangeBoundaries() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] inSigma = new double[] { 0.5, 15.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 10.0, 10.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testMixedFiniteAndInfiniteBoundsLower() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] lower = new double[] { -10.0, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { 10.0, 10.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testMixedFiniteAndInfiniteBoundsUpper() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test
    public void testAllInfiniteBoundsTreatedAsNoBounds() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer(10);
        PointValuePair result = optimizer.optimize(1000, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
        Assert.assertNotNull(result);
    }

    @Test
    public void testTooManyEvaluationsHandling() {
        double[] start = new double[] { 5.0, 5.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(20);
        // Extremely low evaluation budget should cause generation loop to break early
        PointValuePair result = optimizer.optimize(5, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
    }

    @Test
    public void testStopFitnessTermination() {
        double[] start = new double[] { 2.0, 2.0 };
        double[] inSigma = new double[] { 0.5, 0.5 };
        double targetStopFitness = 0.5;
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 1000, targetStopFitness, true, 0, 0, new MersenneTwister(42), false, null);
        PointValuePair result = optimizer.optimize(5000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertTrue(result.getValue() <= targetStopFitness);
    }

    @Test
    public void testStopFitnessTerminationMaximize() {
        double[] start = new double[] { 0.5, 0.5 };
        double[] inSigma = new double[] { 0.2, 0.2 };
        MultivariateFunction negSphere = new MultivariateFunction() {
            public double value(double[] x) {
                return -(x[0] * x[0] + x[1] * x[1]);
            }
        };
        double targetStopFitness = -0.05;
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 1000, targetStopFitness, true, 0, 0, new MersenneTwister(42), false, null);
        PointValuePair result = optimizer.optimize(5000, negSphere, GoalType.MAXIMIZE, start);
        Assert.assertTrue(result.getValue() >= targetStopFitness);
    }

    @Test
    public void testConvergenceCheckerTermination() {
        double[] start = new double[] { 1.0, 1.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 1000, 0.0, true, 0, 0, new MersenneTwister(42), false, new SimplePointChecker<PointValuePair>(1e-1, 1e-1));
        PointValuePair result = optimizer.optimize(2000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
    }

    @Test
    public void testRosenbrockOptimization() {
        double[] start = new double[] { -1.2, 1.0 };
        double[] inSigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma, 1000, 1e-10, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker(1e-8, 1e-8));
        PointValuePair result = optimizer.optimize(5000, new Rosenbrock(), GoalType.MINIMIZE, start);
        Assert.assertEquals(1.0, result.getPoint()[0], 0.1);
        Assert.assertEquals(1.0, result.getPoint()[1], 0.1);
    }
}
