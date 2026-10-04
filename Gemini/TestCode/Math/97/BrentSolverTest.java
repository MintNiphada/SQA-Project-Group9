package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testSinZero() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(3.0, 4.0);
        Assert.assertEquals(Math.PI, result, 1E-6);
    }

    @Test
    public void testQuinticZero() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(-0.2, 0.2);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(-0.1, 0.3);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(-0.3, 0.45, 0.3);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(0.3, 0.7);
        Assert.assertEquals(0.5, result, 1E-6);
        result = solver.solve(0.2, 0.6, 0.5);
        Assert.assertEquals(0.5, result, 1E-6);
        result = solver.solve(0.05, 0.95, 0.5);
        Assert.assertEquals(0.5, result, 1E-6);
        result = solver.solve(0.85, 1.25, 1.0);
        Assert.assertEquals(1.0, result, 1E-6);
        result = solver.solve(0.8, 1.2);
        Assert.assertEquals(1.0, result, 1E-6);
        result = solver.solve(0.85, 1.25);
        Assert.assertEquals(1.0, result, 1E-6);
        result = solver.solve(0.8, 1.2, 0.999999);
        Assert.assertEquals(1.0, result, 1E-6);
        result = solver.solve(0.8, 1.2, 1.0);
        Assert.assertEquals(1.0, result, 1E-6);
    }

    @Test
    public void testRootAtEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, Math.PI, 0.0);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(-Math.PI, 0.0, 0.0);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(0.0, 1.0, 0.5);
        Assert.assertEquals(0.0, result, 1E-6);
        result = solver.solve(-1.0, 0.0, -0.5);
        Assert.assertEquals(0.0, result, 1E-6);
    }

    @Test
    public void testInitialGuessNotBracketed() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.2, 1.2, 0.4);
        Assert.assertEquals(0.5, result, 1E-6);
        result = solver.solve(0.6, 1.2, 0.8);
        Assert.assertEquals(1.0, result, 1E-6);
    }

    @Test
    public void testInitialGuessInvalid() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver(f);
        try {
            solver.solve(0.6, 1.2, 0.5);
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            solver.solve(0.6, 1.2, 1.5);
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testEndpointsSameSign() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver(f);
        try {
            solver.solve(0.6, 0.8);
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testMaxIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return Math.sinh(x) - 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setMaximalIterationCount(1);
        try {
            solver.solve(0.0, 2.0);
            Assert.fail();
        } catch (MaxIterationsExceededException expected) {
        }
    }

    @Test
    public void testInverseQuadraticAndLinearInterpolation() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return (x + 3.0) * (x - 1.0) * (x - 1.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(-4.0, 0.0);
        Assert.assertEquals(-3.0, result, 1E-6);
        result = solver.solve(-4.0, 2.0, -1.0);
        Assert.assertEquals(-3.0, result, 1E-6);
    }

    @Test
    public void testNegativeDeltaBranches() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return 1.0 / (x - 1.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setAbsoluteAccuracy(1E-10);
        solver.setRelativeAccuracy(1E-10);
        try {
            solver.solve(0.5, 1.5);
            Assert.fail();
        } catch (MaxIterationsExceededException expected) {
        }
    }

    @Test
    public void testExactZeroAtInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return x - 2.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 4.0, 2.5);
        Assert.assertEquals(2.5, result, 1E-6);
    }

    @Test
    public void testExactZeroAtEndpoints() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return x - 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 4.0, 2.0);
        Assert.assertEquals(1.0, result, 1E-6);
        f = new UnivariateRealFunction() {
            private static final long serialVersionUID = 1L;
            public double value(double x) {
                return x - 4.0;
            }
        };
        solver = new BrentSolver(f);
        result = solver.solve(1.0, 4.0, 2.0);
        Assert.assertEquals(4.0, result, 1E-6);
    }
}
