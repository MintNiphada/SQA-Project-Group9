package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    @Test
    public void testSigmaConstructorAndGetter() {
        double[] input = new double[] { 1.0, 2.5, 0.0 };
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(input);
        double[] retrieved = sigma.getSigma();
        Assert.assertArrayEquals(input, retrieved, 1e-9);
        Assert.assertNotSame(input, retrieved);
    }

    @Test(expected = NotPositiveException.class)
    public void testSigmaNegativeValueThrows() {
        new CMAESOptimizer.Sigma(new double[] { 1.0, -0.1, 2.0 });
    }

    @Test
    public void testPopulationSizeConstructorAndGetter() {
        CMAESOptimizer.PopulationSize popSize = new CMAESOptimizer.PopulationSize(10);
        Assert.assertEquals(10, popSize.getPopulationSize());
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testPopulationSizeZeroThrows() {
        new CMAESOptimizer.PopulationSize(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testPopulationSizeNegativeThrows() {
        new CMAESOptimizer.PopulationSize(-5);
    }

    @Test
    public void testDoubleIndexClassEqualsAndHashCode() throws Exception {
        Class<?> innerClass = Class.forName("org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer$DoubleIndex");
        java.lang.reflect.Constructor<?> ctor = innerClass.getDeclaredConstructor(double.class, int.class);
        ctor.setAccessible(true);

        Object d1 = ctor.newInstance(1.5, 0);
        Object d2 = ctor.newInstance(1.5, 1);
        Object d3 = ctor.newInstance(2.5, 0);

        Assert.assertTrue(d1.equals(d1));
        Assert.assertTrue(d1.equals(d2));
        Assert.assertFalse(d1.equals(d3));
        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d1.equals("SomeString"));

        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        @SuppressWarnings("unchecked")
        Comparable<Object> comp1 = (Comparable<Object>) d1;
        Assert.assertEquals(0, comp1.compareTo(d2));
        Assert.assertTrue(comp1.compareTo(d3) < 0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testInputSigmaDimensionMismatch() {
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 0, true, 0, 0, new MersenneTwister(42), false, null);
        MultivariateFunction f = point -> point[0] * point[0];
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5 }),
            new CMAESOptimizer.PopulationSize(5)
        );
    }

    @Test(expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRangeBoundaries() {
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 0, true, 0, 0, new MersenneTwister(42), false, null);
        MultivariateFunction f = point -> point[0] * point[0];
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.5 }),
            new SimpleBounds(new double[] { 0.0 }, new double[] { 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1.5 }),
            new CMAESOptimizer.PopulationSize(5)
        );
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testInitializeCMANonPositiveLambda() {
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 0, true, 0, 0, new MersenneTwister(42), false, null);
        MultivariateFunction f = point -> point[0] * point[0];
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.5 }),
            new CMAESOptimizer.Sigma(new double[] { 0.1 })
        );
    }

    @Test
    public void testOptimizeSphereActiveCMAWithStatistics() {
        int dim = 2;
        MultivariateFunction sphere = point -> {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        };

        RandomGenerator rng = new MersenneTwister(123456);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            300, 1e-8, true, 0, 0, rng, true, new SimpleValueChecker(1e-6, 1e-6)
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new ObjectiveFunction(sphere),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.5, -2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10)
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);

        List<Double> sigmas = optimizer.getStatisticsSigmaHistory();
        List<Double> fitness = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> means = optimizer.getStatisticsMeanHistory();
        List<RealMatrix> dList = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmas.isEmpty());
        Assert.assertFalse(fitness.isEmpty());
        Assert.assertFalse(means.isEmpty());
        Assert.assertFalse(dList.isEmpty());
    }

    @Test
    public void testOptimizeSphereMaximize() {
        MultivariateFunction invertedSphere = point -> -(point[0] * point[0] + point[1] * point[1]);

        RandomGenerator rng = new MersenneTwister(42);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            300, 1e-8, false, 0, 0, rng, false, new SimpleValueChecker(1e-6, 1e-6)
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new ObjectiveFunction(invertedSphere),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10)
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
    }

    @Test
    public void testOptimizeDiagonalOnlySwitching() {
        MultivariateFunction sphere = point -> point[0] * point[0] + point[1] * point[1];

        RandomGenerator rng = new MersenneTwister(1234);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            300, 1e-9, true, 2, 0, rng, false, null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new ObjectiveFunction(sphere),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, 3.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.8, 0.8 }),
            new CMAESOptimizer.PopulationSize(10)
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testOptimizeDiagonalOnlyPersistent() {
        MultivariateFunction sphere = point -> point[0] * point[0] + point[1] * point[1];

        RandomGenerator rng = new MersenneTwister(1234);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            200, 1e-9, true, 1, 0, rng, false, null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new ObjectiveFunction(sphere),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 1.5 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10)
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testOptimizeWithBoundsAndFeasibleCheck() {
        MultivariateFunction sphere = point -> point[0] * point[0] + point[1] * point[1];

        RandomGenerator rng = new Well19937c(987654321);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            150, 0, false, 0, 3, rng, false, null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(5000),
            new ObjectiveFunction(sphere),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, 2.0 }),
            new SimpleBounds(new double[] { 0.5, 0.5 }, new double[] { 3.0, 3.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPoint()[0] >= 0.5 - 1e-6);
        Assert.assertTrue(result.getPoint()[1] >= 0.5 - 1e-6);
    }

    @Test
    public void testFlatFitnessPlateauAdjustment() {
        MultivariateFunction flat = point -> 42.0;

        RandomGenerator rng = new MersenneTwister(1);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            15, 0, true, 0, 0, rng, true, null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(flat),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(42.0, result.getValue(), 1e-9);
        Assert.assertTrue(optimizer.getStatisticsSigmaHistory().size() > 0);
    }

    @Test
    public void testMaxEvaluationTermination() {
        MultivariateFunction sphere = point -> point[0] * point[0] + point[1] * point[1];

        RandomGenerator rng = new MersenneTwister(42);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            1000, 1e-15, true, 0, 0, rng, false, null
        );

        try {
            optimizer.optimize(
                new MaxEval(15),
                new ObjectiveFunction(sphere),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 5.0, 5.0 }),
                new CMAESOptimizer.Sigma(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.PopulationSize(20)
            );
        } catch (TooManyEvaluationsException e) {
            // Success: exceeded evaluations handled
        }
    }

    @Test
    public void testPointConvergenceCheckerBreak() {
        MultivariateFunction sphere = point -> point[0] * point[0] + point[1] * point[1];

        RandomGenerator rng = new MersenneTwister(42);
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker<>(1.0, 1.0);
        CMAESOptimizer optimizer = new CMAESOptimizer(
            1000, 0, false, 0, 0, rng, false, checker
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(5000),
            new ObjectiveFunction(sphere),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.1, 0.1 }),
            new CMAESOptimizer.Sigma(new double[] { 0.05, 0.05 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        Assert.assertNotNull(result);
    }

    @Test
    public void testPrivateStaticMatrixHelpers() throws Exception {
        Method logMethod = CMAESOptimizer.class.getDeclaredMethod("log", RealMatrix.class);
        logMethod.setAccessible(true);
        RealMatrix mat = new Array2DRowRealMatrix(new double[][] { { Math.E, 1.0 } });
        RealMatrix logMat = (RealMatrix) logMethod.invoke(null, mat);
        Assert.assertEquals(1.0, logMat.getEntry(0, 0), 1e-9);
        Assert.assertEquals(0.0, logMat.getEntry(0, 1), 1e-9);

        Method minMatrixMethod = CMAESOptimizer.class.getDeclaredMethod("min", RealMatrix.class);
        minMatrixMethod.setAccessible(true);
        double minM = (double) minMatrixMethod.invoke(null, new Array2DRowRealMatrix(new double[][] { { 3.0, 1.0 }, { 4.0, 2.0 } }));
        Assert.assertEquals(1.0, minM, 1e-9);

        Method minArrayMethod = CMAESOptimizer.class.getDeclaredMethod("min", double[].class);
        minArrayMethod.setAccessible(true);
        double minA = (double) minArrayMethod.invoke(null, new double[] { 5.0, 2.0, 8.0 });
        Assert.assertEquals(2.0, minA, 1e-9);

        Method maxArrayMethod = CMAESOptimizer.class.getDeclaredMethod("max", double[].class);
        maxArrayMethod.setAccessible(true);
        double maxA = (double) maxArrayMethod.invoke(null, new double[] { 5.0, 2.0, 8.0 });
        Assert.assertEquals(8.0, maxA, 1e-9);

        Method triuMethod = CMAESOptimizer.class.getDeclaredMethod("triu", RealMatrix.class, int.class);
        triuMethod.setAccessible(true);
        RealMatrix squareMat = new Array2DRowRealMatrix(new double[][] { { 1, 2 }, { 3, 4 } });
        RealMatrix triuMat = (RealMatrix) triuMethod.invoke(null, squareMat, 1);
        Assert.assertEquals(0.0, triuMat.getEntry(0, 0), 1e-9);
        Assert.assertEquals(2.0, triuMat.getEntry(0, 1), 1e-9);
        Assert.assertEquals(0.0, triuMat.getEntry(1, 0), 1e-9);
        Assert.assertEquals(0.0, triuMat.getEntry(1, 1), 1e-9);

        Method onesMethod = CMAESOptimizer.class.getDeclaredMethod("ones", int.class, int.class);
        onesMethod.setAccessible(true);
        RealMatrix onesMat = (RealMatrix) onesMethod.invoke(null, 2, 3);
        Assert.assertEquals(2, onesMat.getRowDimension());
        Assert.assertEquals(3, onesMat.getColumnDimension());
        Assert.assertEquals(1.0, onesMat.getEntry(1, 2), 1e-9);

        Method diagMethod = CMAESOptimizer.class.getDeclaredMethod("diag", RealMatrix.class);
        diagMethod.setAccessible(true);
        RealMatrix colVector = new Array2DRowRealMatrix(new double[][] { { 2.0 }, { 5.0 } });
        RealMatrix diagFromCol = (RealMatrix) diagMethod.invoke(null, colVector);
        Assert.assertEquals(2, diagFromCol.getRowDimension());
        Assert.assertEquals(2, diagFromCol.getColumnDimension());
        Assert.assertEquals(2.0, diagFromCol.getEntry(0, 0), 1e-9);
        Assert.assertEquals(0.0, diagFromCol.getEntry(0, 1), 1e-9);
        Assert.assertEquals(5.0, diagFromCol.getEntry(1, 1), 1e-9);

        RealMatrix colFromDiag = (RealMatrix) diagMethod.invoke(null, diagFromCol);
        Assert.assertEquals(2, colFromDiag.getRowDimension());
        Assert.assertEquals(1, colFromDiag.getColumnDimension());
        Assert.assertEquals(2.0, colFromDiag.getEntry(0, 0), 1e-9);
        Assert.assertEquals(5.0, colFromDiag.getEntry(1, 0), 1e-9);
    }
}
