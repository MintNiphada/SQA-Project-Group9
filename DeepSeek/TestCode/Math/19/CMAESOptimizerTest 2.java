package org.apache.commons.math3.optimization.direct;

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    private static final double EPS = 1e-6;

    private static class ConstantFunction implements MultivariateFunction {
        private double value;
        public ConstantFunction(double value) {
            this.value = value;
        }
        public double value(double[] point) {
            return value;
        }
    }

    private static class QuadraticFunction implements MultivariateFunction {
        private final double[] optimum;
        public QuadraticFunction(double[] optimum) {
            this.optimum = optimum.clone();
        }
        public double value(double[] point) {
            double sum = 0;
            for (int i = 0; i < point.length; i++) {
                double diff = point[i] - optimum[i];
                sum += diff * diff;
            }
            return sum;
        }
    }

    private static class ThrowAfterNFunction implements MultivariateFunction {
        private int count = 0;
        private final int failAfter;
        private final MultivariateFunction delegate;
        public ThrowAfterNFunction(int failAfter, MultivariateFunction delegate) {
            this.failAfter = failAfter;
            this.delegate = delegate;
        }
        public double value(double[] point) {
            if (++count >= failAfter) {
                throw new TooManyEvaluationsException(null);
            }
            return delegate.value(point);
        }
    }

    private static CMAESOptimizer createOptimizer(int lambda, double[] inputSigma,
            int maxIterations, double stopFitness, boolean isActiveCMA,
            int diagonalOnly, int checkFeasableCount,
            RandomGenerator random, boolean generateStatistics) {
        return new CMAESOptimizer(lambda, inputSigma, maxIterations, stopFitness,
                isActiveCMA, diagonalOnly, checkFeasableCount, random, generateStatistics,
                new SimpleValueChecker());
    }

    @Test
    public void testDefaultConstructor() {
        CMAESOptimizer optim = new CMAESOptimizer();
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.01);
    }

    @Test
    public void testConstructorWithLambda() {
        CMAESOptimizer optim = new CMAESOptimizer(20);
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(200, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.01);
    }

    @Test
    public void testConstructorWithLambdaAndInputSigma() {
        CMAESOptimizer optim = new CMAESOptimizer(10, new double[]{ 0.5 });
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.01);
    }

    @Test
    public void testFullConstructor() {
        RandomGenerator rng = new MersenneTwister(12345L);
        CMAESOptimizer optim = new CMAESOptimizer(10, new double[]{ 0.3 }, 500, 0.0,
                true, 0, 0, rng, false,
                new SimpleValueChecker());
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(200, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.01);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testInputSigmaWrongDimension() {
        CMAESOptimizer optim = new CMAESOptimizer(10, new double[]{ 0.1, 0.2 });
        optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, new double[]{ 1.0 });
    }

    @Test(expected = NotPositiveException.class)
    public void testInputSigmaNegative() {
        CMAESOptimizer optim = new CMAESOptimizer(10, new double[]{ -0.1 });
        optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, new double[]{ 1.0 });
    }

    @Test(expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRange() {
        CMAESOptimizer optim = new CMAESOptimizer(10, new double[]{ 0.5 });
        double[] start = { 0.5 };
        double[] lB = { 0.0 };
        double[] uB = { 0.5 };
        optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, lB, uB, start);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testMixedBoundTypes() {
        CMAESOptimizer optim = new CMAESOptimizer(10);
        double[] lB = { 0.0, Double.NEGATIVE_INFINITY };
        double[] uB = { 1.0, Double.POSITIVE_INFINITY };
        double[] start = { 0.5, 0.5 };
        optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, lB, uB, start);
    }

    @Test
    public void testNoBoundariesMinimize() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, true, 0, 0, rng, false);
        double[] start = { 2.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 0.1);
        Assert.assertArrayEquals(new double[]{ 0.0 }, result.getPoint(), 0.1);
    }

    @Test
    public void testNoBoundariesMaximize() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, true, 0, 0, rng, false);
        // maximize - (x-2)^2 -> optimum at x=2 with value 0
        final double target = 2.0;
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                double diff = point[0] - target;
                return -(diff * diff);
            }
        };
        double[] start = { 0.0 };
        PointValuePair result = optim.optimize(100, f, GoalType.MAXIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
        Assert.assertArrayEquals(new double[]{ target }, result.getPoint(), 0.1);
    }

    @Test
    public void testWithBoundaries() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, true, 0, 0, rng, false);
        double[] lB = { 0.0 };
        double[] uB = { 1.0 };
        double[] start = { 0.5 };
        // optimum at 0.2, within bounds
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.2 }), GoalType.MINIMIZE, lB, uB, start);
        Assert.assertNotNull(result);
        Assert.assertTrue("fitness should be small", result.getValue() < 0.1);
    }

    @Test
    public void testCheckFeasableCount() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, true, 0, 5, rng, false);
        double[] lB = { 0.0 };
        double[] uB = { 1.0 };
        double[] start = { 0.5 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.2 }), GoalType.MINIMIZE, lB, uB, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.1);
    }

    @Test
    public void testDiagonalOnly() {
        RandomGenerator rng = new MersenneTwister(42L);
        // diagonalOnly = 1, keep covariance diagonal
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, true, 1, 0, rng, false);
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.1);
    }

    @Test
    public void testIsActiveCMAFalse() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 500, 0.0, false, 0, 0, rng, false);
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() < 0.1);
    }

    @Test
    public void testStopFitness() {
        // constant function value 2, stopFitness 3, so breaks after first generation
        CMAESOptimizer optim = createOptimizer(10, null, 1000, 3.0, true, 0, 0, new MersenneTwister(42L), false);
        double[] start = { 1.0 };
        MultivariateFunction f = new ConstantFunction(2.0);
        PointValuePair result = optim.optimize(100, f, GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        Assert.assertEquals(2.0, result.getValue(), EPS);
        // the algorithm should stop early, but we can't verify iterations directly.
        // At least it returned a result.
    }

    @Test
    public void testMaxIterations() {
        // set maxIterations = 1, so loop runs only once
        CMAESOptimizer optim = createOptimizer(10, null, 1, 0.0, true, 0, 0, new MersenneTwister(42L), false);
        double[] start = { 10.0 };
        PointValuePair result = optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        // The optimum value might be poor, but should be finite
        Assert.assertTrue(Double.isFinite(result.getValue()));
    }

    @Test
    public void testConvergenceChecker() {
        // checker that returns true after first call (i.e., iterations>1)
        final int[] callCount = {0};
        ConvergenceChecker<PointValuePair> checker = new ConvergenceChecker<PointValuePair>() {
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                callCount[0] = iteration;
                return iteration > 1;
            }
        };
        CMAESOptimizer optim = new CMAESOptimizer(10, null, 1000, 0.0, true, 0, 0,
                new MersenneTwister(42L), false, checker);
        double[] start = { 1.0 };
        PointValuePair result = optim.optimize(200, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        // The loop should break due to checker, not due to other criteria
        Assert.assertTrue("checker was called at least once", callCount[0] > 0);
    }

    @Test
    public void testGenerateStatistics() {
        RandomGenerator rng = new MersenneTwister(42L);
        CMAESOptimizer optim = createOptimizer(10, null, 200, 0.0, true, 0, 0, rng, true);
        double[] start = { 1.0 };
        optim.optimize(100, new QuadraticFunction(new double[]{ 0.0 }), GoalType.MINIMIZE, start);
        Assert.assertFalse("sigma history should not be empty", optim.getStatisticsigmaHistory().isEmpty());
        Assert.assertFalse("fitness history should not be empty", optim.getStatisticsFitnessHistory().isEmpty());
        Assert.assertFalse("mean history should not be empty", optim.getStatisticsMeanHistory().isEmpty());
        Assert.assertFalse("D history should not be empty", optim.getStatisticsDHistory().isEmpty());
    }

    @Test
    public void testTooManyEvaluationsException() {
        // force a TooManyEvaluationsException thrown during evaluation to test break generationLoop
        CMAESOptimizer optim = createOptimizer(10, null, 1000, 0.0, true, 0, 0, new MersenneTwister(42L), false);
        double[] start = { 1.0 };
        MultivariateFunction f = new ThrowAfterNFunction(20, new QuadraticFunction(new double[]{ 0.0 })); // throw after 20 evaluations
        PointValuePair result = optim.optimize(100, f, GoalType.MINIMIZE, start);
        Assert.assertNotNull(result);
        // even if evaluations are cut short, a point should be returned
    }

}
