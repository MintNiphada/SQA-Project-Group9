package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.junit.Assert;
import org.junit.Test;

public class MultiDirectionalTest {

    private static class Sphere implements MultivariateRealFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    private static class Rosenbrock implements MultivariateRealFunction {
        public double value(double[] x) {
            double x0 = x[0];
            double x1 = x[1];
            return 100.0 * Math.pow(x1 - x0 * x0, 2) + Math.pow(1.0 - x0, 2);
        }
    }

    private static class Linear2D implements MultivariateRealFunction {
        public double value(double[] point) {
            return 2.0 * point[0] + 3.0 * point[1];
        }
    }

    @Test
    public void testConstructors() {
        MultiDirectional opt1 = new MultiDirectional();
        Assert.assertNotNull(opt1);

        MultiDirectional opt2 = new MultiDirectional(2.5, 0.4);
        Assert.assertNotNull(opt2);
    }

    @Test
    public void testOptimizeSphere() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(500);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-6, 1e-6));

        RealPointValuePair optimum = optimizer.optimize(
            new Sphere(),
            GoalType.MINIMIZE,
            new double[] { 1.0, 2.0, -1.5 }
        );

        Assert.assertNotNull(optimum);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[2], 1e-3);
    }

    @Test
    public void testOptimizeRosenbrock() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional(2.0, 0.5);
        optimizer.setMaxIterations(200);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-4, 1e-4));

        RealPointValuePair optimum = optimizer.optimize(
            new Rosenbrock(),
            GoalType.MINIMIZE,
            new double[] { -1.2, 1.0 }
        );

        Assert.assertNotNull(optimum);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-2);
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-1);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1e-1);
    }

    @Test
    public void testMaximizeLinearFunction() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional(1.5, 0.6);
        optimizer.setMaxIterations(50);
        optimizer.setMaxEvaluations(200);
        optimizer.setConvergenceChecker(new RealConvergenceChecker() {
            public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
                return current.getValue() > 100.0;
            }
        });

        RealPointValuePair optimum = optimizer.optimize(
            new Linear2D(),
            GoalType.MAXIMIZE,
            new double[] { 0.0, 0.0 }
        );

        Assert.assertTrue(optimum.getValue() > 100.0);
    }

    @Test
    public void testBranchExpansionBetter() throws FunctionEvaluationException, OptimizationException {
        MultivariateRealFunction func = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return (x - 10.0) * (x - 10.0) + (y - 10.0) * (y - 10.0);
            }
        };

        MultiDirectional optimizer = new MultiDirectional(3.0, 0.25);
        optimizer.setMaxIterations(50);
        optimizer.setMaxEvaluations(200);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-3, 1e-3));

        RealPointValuePair optimum = optimizer.optimize(
            func,
            GoalType.MINIMIZE,
            new double[] { 0.0, 0.0 }
        );

        Assert.assertEquals(0.0, optimum.getValue(), 1e-2);
    }

    @Test
    public void testBranchReflectionBetterThanExpansion() throws FunctionEvaluationException, OptimizationException {
        MultivariateRealFunction func = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                if (Math.abs(x) > 5.0 || Math.abs(y) > 5.0) {
                    return 1000.0;
                }
                return x * x + y * y;
            }
        };

        MultiDirectional optimizer = new MultiDirectional(4.0, 0.5);
        optimizer.setMaxIterations(50);
        optimizer.setMaxEvaluations(300);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-4, 1e-4));

        RealPointValuePair optimum = optimizer.optimize(
            func,
            GoalType.MINIMIZE,
            new double[] { 1.0, 1.0 }
        );

        Assert.assertTrue(optimum.getValue() < 1.0);
    }

    @Test
    public void testBranchContractionLoop() throws FunctionEvaluationException, OptimizationException {
        MultivariateRealFunction func = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return Math.cos(x) * Math.sin(y);
            }
        };

        MultiDirectional optimizer = new MultiDirectional(1.1, 0.9);
        optimizer.setMaxIterations(30);
        optimizer.setMaxEvaluations(100);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-2, 1e-2));

        RealPointValuePair optimum = optimizer.optimize(
            func,
            GoalType.MINIMIZE,
            new double[] { 0.0, 0.0 }
        );

        Assert.assertNotNull(optimum);
    }

    @Test(expected = OptimizationException.class)
    public void testExceedMaxEvaluations() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(1000);
        optimizer.setMaxEvaluations(5);
        optimizer.optimize(new Sphere(), GoalType.MINIMIZE, new double[] { 10.0, 10.0 });
    }

    @Test(expected = OptimizationException.class)
    public void testExceedMaxIterations() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(1);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-15, 1e-15));
        optimizer.optimize(new Sphere(), GoalType.MINIMIZE, new double[] { 10.0, 10.0 });
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testFunctionEvaluationException() throws FunctionEvaluationException, OptimizationException {
        MultivariateRealFunction func = new MultivariateRealFunction() {
            public double value(double[] point) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(point, "Forced Exception");
            }
        };

        MultiDirectional optimizer = new MultiDirectional();
        optimizer.optimize(func, GoalType.MINIMIZE, new double[] { 1.0, 1.0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFunction() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.optimize(null, GoalType.MINIMIZE, new double[] { 1.0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGoalType() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.optimize(new Sphere(), null, new double[] { 1.0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStartPoint() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.optimize(new Sphere(), GoalType.MINIMIZE, null);
    }
}
