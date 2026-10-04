package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.optimization.GoalType;
import org.junit.Assert;
import org.junit.Test;

public class BrentOptimizerTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testDoOptimize() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.doOptimize();
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testRelativeAccuracyNotStrictlyPositive() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(0.0);
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 5.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testAbsoluteAccuracyNotStrictlyPositive() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(-1.0);
        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 5.0);
    }

    @Test
    public void testSinMinimization() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setMaxEvaluations(100);
        optimizer.setMaximalIterationCount(100);
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 4.0, 5.0);
        Assert.assertEquals(3.0 * Math.PI / 2.0, result, 1e-8);
        Assert.assertEquals(-1.0, optimizer.getFunctionValue(), 1e-8);
        Assert.assertTrue(optimizer.getIterationCount() > 0);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test
    public void testSinMaximization() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 0.5, 3.0);
        Assert.assertEquals(Math.PI / 2.0, result, 1e-8);
        Assert.assertEquals(1.0, optimizer.getFunctionValue(), 1e-8);
    }

    @Test
    public void testInvertedInterval() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 5.0, 4.0);
        Assert.assertEquals(3.0 * Math.PI / 2.0, result, 1e-8);
    }

    @Test
    public void testQuinticMinimizationWithStartValue() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, 0.2, -0.1);
        Assert.assertEquals(-0.27195613, result, 1e-6);
    }

    @Test
    public void testParabolicStepCloseToBounds() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-4);
        optimizer.setRelativeAccuracy(1e-4);
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.999, 2.0, 1.0001);
        Assert.assertEquals(1.0, result, 1e-3);
    }

    @Test
    public void testUpdateBranches() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.cos(x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 2.0 * Math.PI, 3.0);
        Assert.assertEquals(Math.PI, result, 1e-8);
    }

    @Test
    public void testStepEvaluationBranches() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.5) * (x - 2.5) + 3.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0, 4.5);
        Assert.assertEquals(2.5, result, 1e-8);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testMaxIterationsExceeded() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setMaximalIterationCount(1);
        optimizer.setAbsoluteAccuracy(1e-15);
        optimizer.setRelativeAccuracy(1e-15);
        optimizer.optimize(f, GoalType.MINIMIZE, 3.0, 6.0);
    }

    @Test
    public void testStepSmallD() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-5);
        optimizer.setRelativeAccuracy(1e-5);
        double result = optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        Assert.assertEquals(0.0, result, 1e-4);
    }

    @Test
    public void testMaximizationGoldenSection() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return - (x - 0.7) * (x - 0.7);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, -2.0, 2.0, -1.5);
        Assert.assertEquals(0.7, result, 1e-8);
    }
}
