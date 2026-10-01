package org.apache.commons.math3.optimization.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

public class CMAESOptimizerTest {

    private static final double EPS = 1e-6;

    private MultivariateFunction sphere = new MultivariateFunction() {
        public double value(double[] x) {
            double sum = 0;
            for (double xi : x) {
                sum += xi * xi;
            }
            return sum;
        }
    };

    private MultivariateFunction constantFunction = new MultivariateFunction() {
        public double value(double[] x) {
            return 1.0;
        }
    };

    // Helper to run optimization with given parameters and return result
    private PointValuePair run(CMAESOptimizer optimizer, double[] start, double[] lb, double[] ub) {
        return optimizer.optimize(100000, sphere, GoalType.MINIMIZE, start, lb, ub);
    }

    @Test
    public void testDefaultConstructor() {
        CMAESOptimizer opt = new CMAESOptimizer();
        assertNotNull(opt.getStatisticsSigmaHistory());
        assertTrue(opt.getStatisticsSigmaHistory().isEmpty());
        assertNotNull(opt.getStatisticsMeanHistory());
        assertNotNull(opt.getStatisticsFitnessHistory());
        assertNotNull(opt.getStatisticsDHistory());
    }

    @Test
    public void testConstructorWithLambda() {
        CMAESOptimizer opt = new CMAESOptimizer(10);
        assertNotNull(opt.getStatisticsSigmaHistory());
    }

    @Test
    public void testConstructorWithLambdaAndSigma() {
        double[] sigma = {0.5, 0.5};
        CMAESOptimizer opt = new CMAESOptimizer(10, sigma);
        assertNotNull(opt.getStatisticsSigmaHistory());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckParameters_WrongSigmaLength() {
        CMAESOptimizer opt = new CMAESOptimizer(10, new double[]{0.5});
        opt.optimize(1, sphere, GoalType.MINIMIZE, new double[]{1.0, 2.0}, null, null);
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckParameters_NegativeSigma() {
        CMAESOptimizer opt = new CMAESOptimizer(10, new double[]{-0.1, 0.5});
        opt.optimize(1, sphere, GoalType.MINIMIZE, new double[]{1.0, 2.0}, new double[]{-10, -10}, new double[]{10, 10});
    }

    @Test(expected = OutOfRangeException.class)
    public void testCheckParameters_SigmaOutOfRange() {
        CMAESOptimizer opt = new CMAESOptimizer(10, new double[]{100, 0.5});
        opt.optimize(1, sphere, GoalType.MINIMIZE, new double[]{1.0, 2.0}, new double[]{0, 0}, new double[]{10, 10});
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testCheckParameters_MixedBounds() {
        CMAESOptimizer opt = new CMAESOptimizer(10);
        opt.optimize(1, sphere, GoalType.MINIMIZE, new double[]{1.0, 2.0}, new double[]{-Double.MAX_VALUE, 0}, new double[]{Double.MAX_VALUE, 10});
    }

    @Test
    public void testDoOptimize_NoBounds() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 100, 0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        assertTrue(result.getValue() < 1e-6);
    }

    @Test
    public void testDoOptimize_WithBounds() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 100, 0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        double[] lb = {-10, -10};
        double[] ub = {10, 10};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start, lb, ub);
        assertTrue(result.getValue() < 1e-6);
        double[] point = result.getPoint();
        for (int i = 0; i < point.length; i++) {
            assertTrue(point[i] >= lb[i] - EPS);
            assertTrue(point[i] <= ub[i] + EPS);
        }
    }

    @Test
    public void testDoOptimize_DiagonalOnly() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 100, 0, true, 1, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        assertTrue(result.getValue() < 1e-6);
    }

    @Test
    public void testDoOptimize_ActiveCMAFalse() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 100, 0, false, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        assertTrue(result.getValue() < 1e-6);
    }

    @Test
    public void testGenerateStatistics() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 50, 0, true, 0, 0, new MersenneTwister(123), true, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        assertFalse(opt.getStatisticsSigmaHistory().isEmpty());
        assertFalse(opt.getStatisticsFitnessHistory().isEmpty());
        assertFalse(opt.getStatisticsMeanHistory().isEmpty());
        assertFalse(opt.getStatisticsDHistory().isEmpty());
    }

    @Test
    public void testStopFitness() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 100, 100.0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        // Since stopFitness = 100, the initial value is 13, which is <100, so it should stop immediately.
        assertTrue(result.getValue() <= 13.0);
    }

    @Test
    public void testMaxIterationsOne() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 1, 0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {2.0, 3.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        // After one generation loop, the best fitness should be <= the initial value.
        assertTrue(result.getValue() <= 13.0);
    }

    @Test
    public void testConvergenceChecker() {
        SimpleValueChecker checker = new SimpleValueChecker(1e-2, 1e-2);
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 1000, 0, true, 0, 0, new MersenneTwister(123), false, checker);
        double[] start = {10.0, 10.0};
        PointValuePair result = opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        assertTrue(result.getValue() < 1e-1);
    }

    @Test
    public void testFlatFitnessAdjustment() {
        // Use constant function to trigger flat fitness adaptation
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 50, 0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {1.0, 1.0};
        opt.optimize(100000, constantFunction, GoalType.MINIMIZE, start);
        // No exception means flat fitness handled
    }

    @Test
    public void testFitnessHistoryPushing() {
        CMAESOptimizer opt = new CMAESOptimizer(20, null, 20, 0, true, 0, 0, new MersenneTwister(123), false, new SimpleValueChecker());
        double[] start = {5.0, 5.0};
        opt.optimize(100000, sphere, GoalType.MINIMIZE, start);
        // Should have run several iterations, pushing onto history; implicitly covered
    }
}
