package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testDeprecatedConstructorsAndSolvers() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver(f);
        Assert.assertEquals(1.0, solver.solve(0.5, 1.5), 1E-6);
        Assert.assertEquals(1.0, solver.solve(0.5, 1.5, 0.8), 1E-6);
    }

    @Test
    public void testSinZero() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(Math.PI, solver.solve(f, 3.0, 4.0), 1E-6);
        Assert.assertEquals(Math.PI, solver.solve(f, 3.0, 4.0, 3.5), 1E-6);
    }

    @Test
    public void testQuinticZero() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(-1.0, solver.solve(f, -1.5, -0.5), 1E-6);
        Assert.assertEquals(0.0, solver.solve(f, -0.5, 0.5), 1E-6);
        Assert.assertEquals(1.0, solver.solve(f, 0.5, 1.5), 1E-6);
    }

    @Test
    public void testRootAtEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(0.0, solver.solve(f, 0.0, Math.PI), 1E-6);
        Assert.assertEquals(Math.PI, solver.solve(f, -1.0, Math.PI), 1E-6);
    }

    @Test
    public void testInitialGuessAtRoot() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(1.0, solver.solve(f, 0.5, 1.5, 1.0), 1E-6);
    }

    @Test
    public void testInitialGuessMinEndpointGoodEnough() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(0.0, solver.solve(f, 0.0, 1.5, 0.8), 1E-6);
    }

    @Test
    public void testInitialGuessMaxEndpointGoodEnough() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(0.0, solver.solve(f, -1.5, 0.0, -0.8), 1E-6);
    }

    @Test
    public void testInitialGuessBracketingMin() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(1.0, solver.solve(f, 0.5, 2.0, 1.2), 1E-6);
    }

    @Test
    public void testInitialGuessBracketingMax() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        Assert.assertEquals(1.0, solver.solve(f, 0.8, 2.0, 0.9), 1E-6);
    }

    @Test
    public void testEndpointsCloseToZero() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * 1e-7;
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.setFunctionValueAccuracy(1e-5);
        Assert.assertEquals(1.0, solver.solve(f, 1.0, 2.0), 1e-6);
        Assert.assertEquals(2.0, solver.solve(f, -2.0, -1.0), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonBracketing() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 0.5, 0.8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadInterval() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 2.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadSequence() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 1.0, 2.0, 2.5);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testMaxIterationsExceeded() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        solver.setMaximalIterationCount(1);
        solver.solve(f, 0.5, 1.5, 0.6);
    }

    @Test
    public void testInverseQuadraticBranch() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - 3.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        double root = solver.solve(f, 0.0, 2.0, 0.5);
        Assert.assertEquals(Math.log(3.0), root, 1e-6);
    }

    @Test
    public void testSmallStepBranches() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1e-9) * (x - 1e-9);
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.setFunctionValueAccuracy(1e-15);
        solver.setAbsoluteAccuracy(1e-12);
        UnivariateRealFunction fLinear = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1e-9;
            }
        };
        double root = solver.solve(fLinear, -1.0, 1.0, 0.0);
        Assert.assertEquals(1e-9, root, 1e-6);
    }
}
