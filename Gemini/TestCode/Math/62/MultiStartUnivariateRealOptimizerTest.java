package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class MultiStartUnivariateRealOptimizerTest {

    @Test(expected = MathIllegalStateException.class)
    public void testGetOptimaWithoutOptimize() {
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.getOptima();
    }

    @Test
    public void testDelegatedConvergenceChecker() {
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        ConvergenceChecker<UnivariateRealPointValuePair> checker =
            new ConvergenceChecker<UnivariateRealPointValuePair>() {
                public boolean converged(int iteration, UnivariateRealPointValuePair previous, UnivariateRealPointValuePair current) {
                    return true;
                }
            };

        optimizer.setConvergenceChecker(checker);
        Assert.assertSame(checker, optimizer.getConvergenceChecker());
    }

    @Test
    public void testSetAndGetMaxEvaluations() {
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        optimizer.setMaxEvaluations(100);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
        Assert.assertEquals(100, underlying.getMaxEvaluations());
    }

    @Test
    public void testSinMinimization() throws FunctionEvaluationException {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(444284000L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.setMaxEvaluations(300);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MINIMIZE, -100.0, 100.0);
        Assert.assertEquals(-1.0, optimum.getValue(), 1e-10);
        Assert.assertTrue(optimizer.getEvaluations() > 0);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; ++i) {
            if (optima[i] != null) {
                Assert.assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-10);
                if (i > 0 && optima[i - 1] != null) {
                    Assert.assertTrue(optima[i - 1].getValue() <= optima[i].getValue());
                }
            }
        }
    }

    @Test
    public void testSinMaximizationWithStartValue() throws FunctionEvaluationException {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(444284000L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.setMaxEvaluations(300);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MAXIMIZE, -100.0, 100.0, 0.0);
        Assert.assertEquals(1.0, optimum.getValue(), 1e-10);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 1; i < optima.length; ++i) {
            if (optima[i] != null && optima[i - 1] != null) {
                Assert.assertTrue(optima[i - 1].getValue() >= optima[i].getValue());
            }
        }
    }

    @Test
    public void testOptimaCloning() throws FunctionEvaluationException {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);
        optimizer.setMaxEvaluations(100);
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);

        UnivariateRealPointValuePair[] optima1 = optimizer.getOptima();
        UnivariateRealPointValuePair[] optima2 = optimizer.getOptima();
        Assert.assertNotSame(optima1, optima2);
        Assert.assertEquals(optima1.length, optima2.length);
    }

    @Test(expected = ConvergenceException.class)
    public void testConvergenceExceptionWhenAllFail() throws FunctionEvaluationException {
        BaseUnivariateRealOptimizer<UnivariateRealFunction> failingOptimizer =
            new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
                private int maxEval;
                public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
                public int getMaxEvaluations() { return maxEval; }
                public int getEvaluations() { return 1; }
                public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}
                public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                    throw new ConvergenceException(null);
                }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                    return optimize(f, goalType, min, max);
                }
            };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(failingOptimizer, 3, g);
        optimizer.setMaxEvaluations(10);
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 1.0);
    }

    @Test(expected = ConvergenceException.class)
    public void testFunctionEvaluationExceptionWhenAllFail() throws FunctionEvaluationException {
        BaseUnivariateRealOptimizer<UnivariateRealFunction> failingOptimizer =
            new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
                private int maxEval;
                public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
                public int getMaxEvaluations() { return maxEval; }
                public int getEvaluations() { return 1; }
                public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}
                public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) throws FunctionEvaluationException {
                    throw new FunctionEvaluationException(0.0);
                }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) throws FunctionEvaluationException {
                    return optimize(f, goalType, min, max);
                }
            };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(failingOptimizer, 3, g);
        optimizer.setMaxEvaluations(10);
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 1.0);
    }

    @Test
    public void testPartialFailureSorting() throws FunctionEvaluationException {
        BaseUnivariateRealOptimizer<UnivariateRealFunction> partialOptimizer =
            new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
                private int count = 0;
                private int maxEval;
                public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
                public int getMaxEvaluations() { return maxEval; }
                public int getEvaluations() { return 1; }
                public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}
                public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) throws FunctionEvaluationException {
                    count++;
                    if (count == 1) {
                        return new UnivariateRealPointValuePair(1.0, 10.0);
                    } else if (count == 2) {
                        throw new ConvergenceException(null);
                    } else if (count == 3) {
                        throw new FunctionEvaluationException(0.0);
                    } else {
                        return new UnivariateRealPointValuePair(4.0, 5.0);
                    }
                }
                public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) throws FunctionEvaluationException {
                    return optimize(f, goalType, min, max);
                }
            };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(partialOptimizer, 4, g);
        optimizer.setMaxEvaluations(100);

        UnivariateRealPointValuePair opt = optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        Assert.assertEquals(5.0, opt.getValue(), 1e-10);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(4, optima.length);
        Assert.assertNotNull(optima[0]);
        Assert.assertEquals(5.0, optima[0].getValue(), 1e-10);
        Assert.assertNotNull(optima[1]);
        Assert.assertEquals(10.0, optima[1].getValue(), 1e-10);
        Assert.assertNull(optima[2]);
        Assert.assertNull(optima[3]);
        Assert.assertEquals(4, optimizer.getEvaluations());
    }
}
