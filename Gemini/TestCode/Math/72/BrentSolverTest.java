package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testSinZero() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 3.0, 4.0);
        Assert.assertEquals(Math.PI, result, 1E-6);
    }

    @Test
    public void testSinZeroWithInitial() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 3.0, 4.0, 3.5);
        Assert.assertEquals(Math.PI, result, 1E-6);
    }

    @Test
    public void testSinZeroWithInitialCloseToRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 3.0, 4.0, Math.PI);
        Assert.assertEquals(Math.PI, result, 1E-6);
        Assert.assertEquals(0, solver.getIterationCount());
    }

    @Test
    public void testSinZeroEndpointRoots() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        
        double resultMin = solver.solve(f, 0.0, 1.0);
        Assert.assertEquals(0.0, resultMin, 1E-6);

        double resultMax = solver.solve(f, -1.0, 0.0);
        Assert.assertEquals(0.0, resultMax, 1E-6);
    }

    @Test
    public void testEndpointCloseToRootNonBracketing() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        BrentSolver solver = new BrentSolver();
        double resultMin = solver.solve(f, 1.0, 2.0);
        Assert.assertEquals(1.0, resultMin, 1E-6);

        double resultMax = solver.solve(f, 0.0, 1.0);
        Assert.assertEquals(1.0, resultMax, 1E-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonBracketing() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 1.0, 2.0);
    }

    @Test
    public void testSolveWithInitialBracketingSubIntervals() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        double result1 = solver.solve(f, 1.0, 4.0, 3.0);
        Assert.assertEquals(2.0, result1, 1E-6);

        double result2 = solver.solve(f, 0.0, 3.0, 1.0);
        Assert.assertEquals(2.0, result2, 1E-6);
    }

    @Test
    public void testSolveWithInitialEndpointsCloseToZero() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * (x - 4.0);
            }
        };
        BrentSolver solver = new BrentSolver();
        double resultMin = solver.solve(f, 0.0, 5.0, 2.0);
        Assert.assertEquals(0.0, resultMin, 1E-6);

        double resultMax = solver.solve(f, -1.0, 4.0, 2.0);
        Assert.assertEquals(4.0, resultMax, 1E-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveWithInitialNonBracketing() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 1.0, 3.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveWithInitialSequenceInvalid() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 4.0, 3.0, 3.5);
    }

    @Test
    public void testQuinticFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1) * (x - 2) * (x - 3) * (x - 4) * (x - 5);
            }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.5, 1.5);
        Assert.assertEquals(1.0, result, 1E-6);

        result = solver.solve(f, 0.5, 2.5, 1.2);
        Assert.assertEquals(1.0, result, 1E-6);
    }

    @Test
    public void testDeprecatedMethods() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver(f);
        
        double result1 = solver.solve(3.0, 4.0);
        Assert.assertEquals(Math.PI, result1, 1E-6);

        double result2 = solver.solve(3.0, 4.0, 3.5);
        Assert.assertEquals(Math.PI, result2, 1E-6);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testExceedMaxIterations() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.setMaximalIterationCount(1);
        solver.setAbsoluteAccuracy(1e-15);
        solver.setRelativeAccuracy(1e-15);
        solver.setFunctionValueAccuracy(1e-15);
        solver.solve(f, 3.0, 4.0);
    }

    @Test
    public void testRootCloseToTolerance() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.pow(x, 101);
            }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, -0.5, 0.7);
        Assert.assertEquals(0.0, result, 1E-3);
    }

    @Test
    public void testInverseQuadraticAndLinearBranching() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - 2.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, -2.0, 2.0, 0.0);
        Assert.assertEquals(Math.log(2.0), result, 1E-6);
    }
}
