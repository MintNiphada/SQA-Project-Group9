package org.apache.commons.math.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class MultiStartUnivariateRealOptimizerTest {

    @Test
    public void testDelegatedProperties() {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            private double absAcc = 1e-6;
            private double relAcc = 1e-7;
            private int maxIter = 100;
            private int maxEval = 1000;
            private int iters = 5;
            private int evals = 15;
            private double res = 2.5;
            private double val = 10.0;

            public void setMaximalIterationCount(int count) { this.maxIter = count; }
            public int getMaximalIterationCount() { return maxIter; }
            public void resetMaximalIterationCount() { this.maxIter = 100; }
            public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
            public int getMaxEvaluations() { return maxEval; }
            public void resetMaxEvaluations() { this.maxEval = 1000; }
            public void setAbsoluteAccuracy(double accuracy) { this.absAcc = accuracy; }
            public double getAbsoluteAccuracy() { return absAcc; }
            public void resetAbsoluteAccuracy() { this.absAcc = 1e-6; }
            public void setRelativeAccuracy(double accuracy) { this.relAcc = accuracy; }
            public double getRelativeAccuracy() { return relAcc; }
            public void resetRelativeAccuracy() { this.relAcc = 1e-7; }
            public int getIterationCount() { return iters; }
            public int getEvaluations() { return evals; }
            public double getResult() { return res; }
            public double getFunctionValue() { return val; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                return res;
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                return res;
            }
        };

        RandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 5, g);

        Assert.assertEquals(10.0, optimizer.getFunctionValue(), 1e-10);
        Assert.assertEquals(2.5, optimizer.getResult(), 1e-10);
        Assert.assertEquals(1e-6, optimizer.getAbsoluteAccuracy(), 1e-10);
        Assert.assertEquals(1e-7, optimizer.getRelativeAccuracy(), 1e-10);
        Assert.assertEquals(0, optimizer.getIterationCount());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(Integer.MAX_VALUE, optimizer.getMaximalIterationCount());
        Assert.assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());

        optimizer.setAbsoluteAccuracy(1e-4);
        Assert.assertEquals(1e-4, optimizer.getAbsoluteAccuracy(), 1e-10);
        optimizer.resetAbsoluteAccuracy();
        Assert.assertEquals(1e-6, optimizer.getAbsoluteAccuracy(), 1e-10);

        optimizer.setRelativeAccuracy(1e-5);
        Assert.assertEquals(1e-5, optimizer.getRelativeAccuracy(), 1e-10);
        optimizer.resetRelativeAccuracy();
        Assert.assertEquals(1e-7, optimizer.getRelativeAccuracy(), 1e-10);

        optimizer.setMaximalIterationCount(50);
        Assert.assertEquals(50, optimizer.getMaximalIterationCount());
        optimizer.resetMaximalIterationCount();

        optimizer.setMaxEvaluations(200);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetOptimaBeforeOptimize() {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            public void setMaximalIterationCount(int count) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int maxEvaluations) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 0; }
            public int getEvaluations() { return 0; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return 0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) { return 0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) { return 0; }
        };
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 3, new JDKRandomGenerator());
        optimizer.getOptima();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetOptimaValuesBeforeOptimize() {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            public void setMaximalIterationCount(int count) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int maxEvaluations) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 0; }
            public int getEvaluations() { return 0; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return 0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) { return 0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) { return 0; }
        };
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 3, new JDKRandomGenerator());
        optimizer.getOptimaValues();
    }

    @Test
    public void testOptimizeMinimizeAndSorting() throws Exception {
        final double[] returns = new double[] { 5.0, 2.0, 8.0, 1.0 };
        final double[] values = new double[] { 25.0, 4.0, 64.0, 1.0 };

        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            private int count = 0;
            private double lastVal;
            public void setMaximalIterationCount(int c) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int m) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 2; }
            public int getEvaluations() { return 3; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return lastVal; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                double res = returns[count];
                lastVal = values[count];
                count++;
                return res;
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                return optimize(f, goalType, min, max);
            }
        };

        RandomGenerator g = new JDKRandomGenerator();
        g.setSeed(42);
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 4, g);

        double opt = optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 10.0, 1.0);
        Assert.assertEquals(1.0, opt, 1e-10);
        Assert.assertEquals(8, optimizer.getIterationCount());
        Assert.assertEquals(12, optimizer.getEvaluations());

        double[] optima = optimizer.getOptima();
        double[] optimaValues = optimizer.getOptimaValues();
        Assert.assertEquals(4, optima.length);
        Assert.assertEquals(1.0, optima[0], 1e-10);
        Assert.assertEquals(2.0, optima[1], 1e-10);
        Assert.assertEquals(5.0, optima[2], 1e-10);
        Assert.assertEquals(8.0, optima[3], 1e-10);

        Assert.assertEquals(1.0, optimaValues[0], 1e-10);
        Assert.assertEquals(4.0, optimaValues[1], 1e-10);
        Assert.assertEquals(25.0, optimaValues[2], 1e-10);
        Assert.assertEquals(64.0, optimaValues[3], 1e-10);
    }

    @Test
    public void testOptimizeMaximizeAndSorting() throws Exception {
        final double[] returns = new double[] { 5.0, 2.0, 8.0 };
        final double[] values = new double[] { 25.0, 4.0, 64.0 };

        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            private int count = 0;
            private double lastVal;
            public void setMaximalIterationCount(int c) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int m) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 1; }
            public int getEvaluations() { return 1; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return lastVal; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                double res = returns[count];
                lastVal = values[count];
                count++;
                return res;
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                return optimize(f, goalType, min, max);
            }
        };

        RandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 3, g);

        double opt = optimizer.optimize(new SinFunction(), GoalType.MAXIMIZE, 0.0, 10.0);
        Assert.assertEquals(8.0, opt, 1e-10);

        double[] optima = optimizer.getOptima();
        double[] optimaValues = optimizer.getOptimaValues();
        Assert.assertEquals(8.0, optima[0], 1e-10);
        Assert.assertEquals(5.0, optima[1], 1e-10);
        Assert.assertEquals(2.0, optima[2], 1e-10);

        Assert.assertEquals(64.0, optimaValues[0], 1e-10);
        Assert.assertEquals(25.0, optimaValues[1], 1e-10);
        Assert.assertEquals(4.0, optimaValues[2], 1e-10);
    }

    @Test
    public void testOptimizerWithExceptionsAndConvergence() throws Exception {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            private int count = 0;
            private double lastVal = 0.0;
            public void setMaximalIterationCount(int c) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int m) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 1; }
            public int getEvaluations() { return 2; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return lastVal; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                    throws ConvergenceException, FunctionEvaluationException {
                int c = count++;
                if (c == 0) {
                    throw new ConvergenceException();
                } else if (c == 1) {
                    throw new FunctionEvaluationException(min);
                } else {
                    lastVal = 3.0;
                    return 1.5;
                }
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                    throws ConvergenceException, FunctionEvaluationException {
                return optimize(f, goalType, min, max);
            }
        };

        RandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 3, g);
        double res = optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 5.0);
        Assert.assertEquals(1.5, res, 1e-10);
        Assert.assertEquals(3, optimizer.getIterationCount());
        Assert.assertEquals(6, optimizer.getEvaluations());
    }

    @Test(expected = OptimizationException.class)
    public void testAllStartsThrowConvergenceException() throws Exception {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            public void setMaximalIterationCount(int c) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int m) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 0; }
            public int getEvaluations() { return 0; }
            public double getResult() { return 0; }
            public double getFunctionValue() { return 0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                    throws ConvergenceException {
                throw new ConvergenceException();
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                    throws ConvergenceException {
                return optimize(f, goalType, min, max);
            }
        };

        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 2, new JDKRandomGenerator());
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 1.0);
    }

    @Test
    public void testOptimaCloning() throws Exception {
        UnivariateRealOptimizer underlying = new UnivariateRealOptimizer() {
            public void setMaximalIterationCount(int c) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int m) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
            public int getIterationCount() { return 1; }
            public int getEvaluations() { return 1; }
            public double getResult() { return 2.0; }
            public double getFunctionValue() { return 4.0; }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                return 2.0;
            }
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                return 2.0;
            }
        };

        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 1, new JDKRandomGenerator());
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 5.0);
        double[] o1 = optimizer.getOptima();
        double[] o2 = optimizer.getOptima();
        Assert.assertNotSame(o1, o2);
        Assert.assertArrayEquals(o1, o2, 1e-10);

        double[] v1 = optimizer.getOptimaValues();
        double[] v2 = optimizer.getOptimaValues();
        Assert.assertNotSame(v1, v2);
        Assert.assertArrayEquals(v1, v2, 1e-10);
    }
}
