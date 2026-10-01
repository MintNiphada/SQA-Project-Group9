package org.apache.commons.math3.optimization.direct;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.ConvergenceChecker;
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

    private static class InvertedSphere implements MultivariateFunction {
        public double value(double[] x) {
            double f = 0;
            for (double v : x) {
                f += v * v;
            }
            return -f;
        }
    }

    private static class ConstantFunction implements MultivariateFunction {
        private final double value;
        ConstantFunction(double value) {
            this.value = value;
        }
        public double value(double[] x) {
            return value;
        }
    }

    @Test
    public void testDefaultConstructor() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        Assert.assertNotNull(optimizer.getStatisticsSigmaHistory());
        Assert.assertNotNull(optimizer.getStatisticsMeanHistory());
        Assert.assertNotNull(optimizer.getStatisticsFitnessHistory());
        Assert.assertNotNull(optimizer.getStatisticsDHistory());
        Assert.assertTrue(optimizer.getStatisticsSigmaHistory().isEmpty());
    }

    @Test
    public void testConstructors() {
        CMAESOptimizer opt1 = new CMAESOptimizer(10);
        Assert.assertNotNull(opt1);

        double[] sigma = new double[] { 0.2, 0.2 };
        CMAESOptimizer opt2 = new CMAESOptimizer(10, sigma);
        Assert.assertNotNull(opt2);

        @SuppressWarnings("deprecation")
        CMAESOptimizer opt3 = new CMAESOptimizer(10, sigma, 1000, 1e-6, true, 0, 0, new MersenneTwister(42L), true);
        Assert.assertNotNull(opt3);

        CMAESOptimizer opt4 = new CMAESOptimizer(10, sigma, 1000, 1e-6, true, 0, 0, new MersenneTwister(42L), true, new SimpleValueChecker(1e-6, 1e-6));
        Assert.assertNotNull(opt4);
    }

    @Test
    public void testOptimizeSphereMinimize() {
        int dim = 3;
        double[] start = new double[dim];
        Arrays.fill(start, 2.0);
        double[] insigma = new double[dim];
        Arrays.fill(insigma, 0.5);

        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 2000, 1e-10, true, 0, 0, new MersenneTwister(12345L), true);
        PointValuePair result = optimizer.optimize(20000, new Sphere(), GoalType.MINIMIZE, start);

        Assert.assertEquals(0.0, result.getValue(), 1e-2);
        for (double val : result.getPoint()) {
            Assert.assertEquals(0.0, val, 1e-1);
        }

        List<Double> sigmas = optimizer.getStatisticsSigmaHistory();
        List<RealMatrix> means = optimizer.getStatisticsMeanHistory();
        List<Double> fitnesses = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> dHistory = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmas.isEmpty());
        Assert.assertFalse(means.isEmpty());
        Assert.assertFalse(fitnesses.isEmpty());
        Assert.assertFalse(dHistory.isEmpty());
        Assert.assertEquals(sigmas.size(), fitnesses.size());
    }

    @Test
    public void testOptimizeSphereMaximize() {
        int dim = 2;
        double[] start = new double[] { 1.5, -1.5 };
        double[] insigma = new double[] { 0.4, 0.4 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 2000, 0, false, 0, 0, new MersenneTwister(54321L), false);
        PointValuePair result = optimizer.optimize(20000, new InvertedSphere(), GoalType.MAXIMIZE, start);

        Assert.assertEquals(0.0, result.getValue(), 1e-2);
        for (double val : result.getPoint()) {
            Assert.assertEquals(0.0, val, 1e-1);
        }
    }

    @Test
    public void testOptimizeWithBoundedConstraints() {
        int dim = 2;
        double[] start = new double[] { 2.0, 3.0 };
        double[] lower = new double[] { 1.0, 1.0 };
        double[] upper = new double[] { 5.0, 5.0 };
        double[] insigma = new double[] { 0.5, 0.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(12, insigma, 2000, 0, true, 0, 2, new MersenneTwister(42L), false);
        PointValuePair result = optimizer.optimize(30000, new Sphere(), GoalType.MINIMIZE, start, lower, upper);

        Assert.assertTrue(result.getPoint()[0] >= lower[0] - 1e-6);
        Assert.assertTrue(result.getPoint()[1] >= lower[1] - 1e-6);
        Assert.assertTrue(result.getPoint()[0] <= upper[0] + 1e-6);
        Assert.assertTrue(result.getPoint()[1] <= upper[1] + 1e-6);
        Assert.assertEquals(2.0, result.getValue(), 0.1);
    }

    @Test
    public void testDiagonalOnlyPositive() {
        int dim = 2;
        double[] start = new double[] { 1.0, 1.0 };
        double[] insigma = new double[] { 0.2, 0.2 };

        // diagonalOnly = 1 (remains diagonal)
        CMAESOptimizer optimizer1 = new CMAESOptimizer(8, insigma, 1000, 1e-8, true, 1, 0, new MersenneTwister(99L), false);
        PointValuePair result1 = optimizer1.optimize(15000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertEquals(0.0, result1.getValue(), 1e-2);

        // diagonalOnly = 3 (switches after 3 iterations)
        CMAESOptimizer optimizer2 = new CMAESOptimizer(8, insigma, 1000, 1e-8, true, 3, 0, new MersenneTwister(99L), false);
        PointValuePair result2 = optimizer2.optimize(15000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertEquals(0.0, result2.getValue(), 1e-2);
    }

    @Test
    public void testStopFitnessTermination() {
        int dim = 2;
        double[] start = new double[] { 2.0, 2.0 };
        double stopFitness = 0.5;

        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 1000, stopFitness, true, 0, 0, new MersenneTwister(11L), false);
        PointValuePair result = optimizer.optimize(10000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertTrue(result.getValue() <= stopFitness + 1e-6);
    }

    @Test
    public void testStopFitnessTerminationMaximize() {
        int dim = 2;
        double[] start = new double[] { 2.0, 2.0 };
        double stopFitness = -0.5;

        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 1000, stopFitness, true, 0, 0, new MersenneTwister(11L), false);
        PointValuePair result = optimizer.optimize(10000, new InvertedSphere(), GoalType.MAXIMIZE, start);
        Assert.assertTrue(result.getValue() >= -stopFitness - 1e-6);
    }

    @Test
    public void testFlatFitnessFunction() {
        int dim = 2;
        double[] start = new double[] { 1.0, 1.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 10, 0, true, 0, 0, new MersenneTwister(1L), false);
        PointValuePair result = optimizer.optimize(1000, new ConstantFunction(5.0), GoalType.MINIMIZE, start);
        Assert.assertEquals(5.0, result.getValue(), 1e-9);
    }

    @Test
    public void testCustomConvergenceChecker() {
        int dim = 2;
        double[] start = new double[] { 3.0, 3.0 };
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(1e-1, 1e-1);

        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 1000, 0, true, 0, 0, new MersenneTwister(77L), false, checker);
        PointValuePair result = optimizer.optimize(10000, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
    }

    @Test
    public void testTooManyEvaluationsExceptionHandled() {
        int dim = 2;
        double[] start = new double[] { 5.0, 5.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10);
        // Only 5 max evaluations allows generation loop to catch TooManyEvaluationsException
        PointValuePair result = optimizer.optimize(5, new Sphere(), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testInputSigmaDimensionMismatch() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] sigma = new double[] { 0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start);
    }

    @Test(expected = NotPositiveException.class)
    public void testInputSigmaNegative() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] sigma = new double[] { 0.1, -0.2 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start);
    }

    @Test(expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRange() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 2.0, 2.0 };
        double[] sigma = new double[] { 0.5, 3.0 }; // 3.0 > upper[1] - lower[1]
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testMixedInfiniteAndFiniteBounds() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testBoundariesOverflow() {
        double[] start = new double[] { 1.0 };
        double[] lower = new double[] { -Double.MAX_VALUE };
        double[] upper = new double[] { Double.MAX_VALUE };
        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
    }

    @Test
    public void testAllInfiniteBoundsTreatedAsNullBoundaries() {
        double[] start = new double[] { 2.0, 2.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 100, 1e-4, true, 0, 0, new MersenneTwister(13L), false);
        PointValuePair result = optimizer.optimize(1000, new Sphere(), GoalType.MINIMIZE, start, lower, upper);
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 0.1);
    }
}
