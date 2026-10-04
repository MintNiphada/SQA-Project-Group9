package org.apache.commons.math3.optimization.direct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    private static class SphereFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0.0;
            for (double x : point) {
                sum += x * x;
            }
            return sum;
        }
    }

    private static class RosenbrockFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0.0;
            for (int i = 0; i < point.length - 1; i++) {
                double a = point[i + 1] - point[i] * point[i];
                double b = point[i] - 1.0;
                sum += 100.0 * a * a + b * b;
            }
            return sum;
        }
    }

    private static class ConstantFunction implements MultivariateFunction {
        private final double value;

        ConstantFunction(double value) {
            this.value = value;
        }

        public double value(double[] point) {
            return value;
        }
    }

    @Test
    public void testDefaultConstructorAndOptimization() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        double[] startPoint = new double[] { 1.5, -2.0 };
        PointValuePair result = optimizer.optimize(10000, new SphereFunction(), GoalType.MINIMIZE, startPoint);
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    @Test
    public void testConstructors() {
        CMAESOptimizer opt1 = new CMAESOptimizer(10);
        Assert.assertNotNull(opt1);

        double[] sigma = new double[] { 0.2, 0.2 };
        CMAESOptimizer opt2 = new CMAESOptimizer(10, sigma);
        Assert.assertNotNull(opt2);

        @SuppressWarnings("deprecation")
        CMAESOptimizer opt3 = new CMAESOptimizer(
                10, sigma, 1000, 1e-6, true, 0, 0,
                new MersenneTwister(42L), false);
        Assert.assertNotNull(opt3);

        CMAESOptimizer opt4 = new CMAESOptimizer(
                10, sigma, 1000, 1e-6, true, 0, 0,
                new MersenneTwister(42L), true,
                new SimpleValueChecker(1e-6, 1e-6));
        Assert.assertNotNull(opt4);
    }

    @Test
    public void testMaximizeGoal() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.5, 0.5 }, 5000, 0.0, true, 0, 0,
                new MersenneTwister(1337L), false, new SimpleValueChecker(1e-7, 1e-7));
        MultivariateFunction invertedSphere = new MultivariateFunction() {
            public double value(double[] point) {
                return -(point[0] * point[0] + point[1] * point[1]);
            }
        };
        PointValuePair result = optimizer.optimize(10000, invertedSphere, GoalType.MAXIMIZE, new double[] { 1.0, 1.0 });
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    @Test
    public void testBoundedOptimizationWithStatistics() {
        int dim = 2;
        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };
        double[] sigma = new double[] { 0.5, 0.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, sigma, 5000, 0.0, true, 0, 5,
                new MersenneTwister(42L), true, new SimpleValueChecker(1e-6, 1e-6));

        PointValuePair result = optimizer.optimize(
                10000, new SphereFunction(), GoalType.MINIMIZE,
                start, lower, upper);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);

        List<Double> sigmaHistory = optimizer.getStatisticsSigmaHistory();
        List<RealMatrix> meanHistory = optimizer.getStatisticsMeanHistory();
        List<Double> fitnessHistory = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> dHistory = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmaHistory.isEmpty());
        Assert.assertFalse(meanHistory.isEmpty());
        Assert.assertFalse(fitnessHistory.isEmpty());
        Assert.assertFalse(dHistory.isEmpty());
    }

    @Test
    public void testNonActiveCMA() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.2, 0.2 }, 3000, 0.0, false, 0, 0,
                new MersenneTwister(100L), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(
                10000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 0.5, -0.5 },
                new double[] { -2.0, -2.0 },
                new double[] { 2.0, 2.0 });
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    @Test
    public void testDiagonalOnlyPositive() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.3, 0.3 }, 2000, 0.0, true, 1, 0,
                new MersenneTwister(200L), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(
                10000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 0.8, -0.8 });
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    @Test
    public void testDiagonalOnlySwitchToFullCovariance() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                12, new double[] { 0.3, 0.3 }, 2000, 0.0, true, 2, 0,
                new MersenneTwister(300L), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(
                10000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 1.0, 1.0 });
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    @Test
    public void testStopFitnessTermination() {
        double stopFitness = 0.05;
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.5, 0.5 }, 5000, stopFitness, true, 0, 0,
                new MersenneTwister(400L), false, null);
        PointValuePair result = optimizer.optimize(
                10000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 1.0, 1.0 });
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() <= stopFitness + 0.05);
    }

    @Test
    public void testStopFitnessMaximize() {
        double stopFitness = -0.05;
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.5, 0.5 }, 5000, stopFitness, true, 0, 0,
                new MersenneTwister(400L), false, null);
        MultivariateFunction negSphere = new MultivariateFunction() {
            public double value(double[] point) {
                return -(point[0] * point[0] + point[1] * point[1]);
            }
        };
        PointValuePair result = optimizer.optimize(
                10000, negSphere, GoalType.MAXIMIZE,
                new double[] { 1.0, 1.0 });
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() >= stopFitness - 0.05);
    }

    @Test
    public void testFlatFitnessFunction() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                8, new double[] { 0.5, 0.5 }, 20, 0.0, true, 0, 0,
                new MersenneTwister(500L), false, null);
        PointValuePair result = optimizer.optimize(
                100, new ConstantFunction(42.0), GoalType.MINIMIZE,
                new double[] { 0.0, 0.0 });
        Assert.assertNotNull(result);
        Assert.assertEquals(42.0, result.getValue(), 1e-6);
    }

    @Test
    public void testMaxEvaluationsBreak() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.5, 0.5 }, 5000, 0.0, true, 0, 0,
                new MersenneTwister(600L), false, null);
        PointValuePair result = optimizer.optimize(
                15, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 2.0, 2.0 });
        Assert.assertNotNull(result);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testInputSigmaDimensionMismatch() {
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[] { 0.2 });
        optimizer.optimize(1000, new SphereFunction(), GoalType.MINIMIZE, new double[] { 1.0, 2.0 });
    }

    @Test(expected = NotPositiveException.class)
    public void testInputSigmaNegative() {
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[] { -0.1, 0.2 });
        optimizer.optimize(1000, new SphereFunction(), GoalType.MINIMIZE, new double[] { 1.0, 2.0 });
    }

    @Test(expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRange() {
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[] { 5.0, 0.2 });
        optimizer.optimize(
                1000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 0.0, 0.0 },
                new double[] { -1.0, -1.0 },
                new double[] { 1.0, 1.0 });
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testMixedInfiniteAndFiniteBounds() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(
                1000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 0.0, 0.0 },
                new double[] { Double.NEGATIVE_INFINITY, -1.0 },
                new double[] { 1.0, 1.0 });
    }

    @Test
    public void testAllInfiniteBoundsHandledAsNoBounds() {
        CMAESOptimizer optimizer = new CMAESOptimizer(8, new double[] { 0.2, 0.2 });
        PointValuePair result = optimizer.optimize(
                2000, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 1.0, -1.0 },
                new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY },
                new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY });
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    @Test
    public void testRosenbrockOptimization() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                12, new double[] { 0.2, 0.2 }, 5000, 1e-10, true, 0, 0,
                new MersenneTwister(700L), false, new SimpleValueChecker(1e-8, 1e-8));
        PointValuePair result = optimizer.optimize(
                10000, new RosenbrockFunction(), GoalType.MINIMIZE,
                new double[] { -1.2, 1.0 });
        Assert.assertNotNull(result);
        Assert.assertEquals(1.0, result.getPoint()[0], 0.2);
        Assert.assertEquals(1.0, result.getPoint()[1], 0.2);
    }

    @Test
    public void testDoubleIndexClassDirectly() throws Exception {
        Class<?> doubleIndexClass = null;
        for (Class<?> cls : CMAESOptimizer.class.getDeclaredClasses()) {
            if (cls.getSimpleName().equals("DoubleIndex")) {
                doubleIndexClass = cls;
                break;
            }
        }
        Assert.assertNotNull("DoubleIndex inner class should exist", doubleIndexClass);

        Constructor<?> ctor = doubleIndexClass.getDeclaredConstructor(double.class, int.class);
        ctor.setAccessible(true);

        Object d1 = ctor.newInstance(1.5, 0);
        Object d2 = ctor.newInstance(1.5, 1);
        Object d3 = ctor.newInstance(2.5, 2);
        Object d4 = ctor.newInstance(0.5, 3);

        @SuppressWarnings("unchecked")
        Comparable<Object> comp1 = (Comparable<Object>) d1;
        Assert.assertTrue(comp1.compareTo(d3) < 0);
        Assert.assertTrue(comp1.compareTo(d4) > 0);
        Assert.assertEquals(0, comp1.compareTo(d2));

        Assert.assertTrue(d1.equals(d1));
        Assert.assertTrue(d1.equals(d2));
        Assert.assertFalse(d1.equals(d3));
        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d1.equals("StringObject"));

        Assert.assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void testFitnessFunctionFeasibilityAndRepair() throws Exception {
        Class<?> fitnessFunClass = null;
        for (Class<?> cls : CMAESOptimizer.class.getDeclaredClasses()) {
            if (cls.getSimpleName().equals("FitnessFunction")) {
                fitnessFunClass = cls;
                break;
            }
        }
        Assert.assertNotNull("FitnessFunction inner class should exist", fitnessFunClass);

        CMAESOptimizer optimizer = new CMAESOptimizer(
                10, new double[] { 0.2, 0.2 }, 100, 0.0, true, 0, 0,
                new MersenneTwister(42L), false, null);

        // Run optimize briefly to initialize optimizer fields (such as boundaries and isMinimize)
        optimizer.optimize(
                50, new SphereFunction(), GoalType.MINIMIZE,
                new double[] { 0.5, 0.5 },
                new double[] { 0.0, 0.0 },
                new double[] { 1.0, 1.0 });

        Constructor<?> ctor = fitnessFunClass.getDeclaredConstructor(CMAESOptimizer.class);
        ctor.setAccessible(true);
        Object fitnessFunction = ctor.newInstance(optimizer);

        Method isFeasibleMethod = fitnessFunClass.getDeclaredMethod("isFeasible", double[].class);
        isFeasibleMethod.setAccessible(true);

        Assert.assertTrue((Boolean) isFeasibleMethod.invoke(fitnessFunction, (Object) new double[] { 0.5, 0.5 }));
        Assert.assertFalse((Boolean) isFeasibleMethod.invoke(fitnessFunction, (Object) new double[] { -0.1, 0.5 }));
        Assert.assertFalse((Boolean) isFeasibleMethod.invoke(fitnessFunction, (Object) new double[] { 0.5, 1.2 }));

        Method encodeMethod = fitnessFunClass.getDeclaredMethod("encode", double[].class);
        encodeMethod.setAccessible(true);
        double[] encoded = (double[]) encodeMethod.invoke(fitnessFunction, (Object) new double[] { 0.5, 0.5 });
        Assert.assertEquals(0.5, encoded[0], 1e-9);

        Method decodeMethod = fitnessFunClass.getDeclaredMethod("decode", double[].class);
        decodeMethod.setAccessible(true);
        double[] decoded = (double[]) decodeMethod.invoke(fitnessFunction, (Object) new double[] { 0.5, 0.5 });
        Assert.assertEquals(0.5, decoded[0], 1e-9);

        Method repairAndDecodeMethod = fitnessFunClass.getDeclaredMethod("repairAndDecode", double[].class);
        repairAndDecodeMethod.setAccessible(true);
        double[] repDec = (double[]) repairAndDecodeMethod.invoke(fitnessFunction, (Object) new double[] { 0.5, 0.5 });
        Assert.assertEquals(0.5, repDec[0], 1e-9);

        Method valueMethod = fitnessFunClass.getDeclaredMethod("value", double[].class);
        valueMethod.setAccessible(true);
        double valOutOfBound = (Double) valueMethod.invoke(fitnessFunction, (Object) new double[] { 1.5, 0.5 });
        Assert.assertTrue(valOutOfBound > 0.0);

        Method setValueRangeMethod = fitnessFunClass.getDeclaredMethod("setValueRange", double.class);
        setValueRangeMethod.setAccessible(true);
        setValueRangeMethod.invoke(fitnessFunction, 10.0);
    }
}
