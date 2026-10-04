package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Test;

public class BisectionSolverTest {

    @Test
    public void testSinZero() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(f, 3.0, 4.0);
        Assert.assertEquals(Math.PI, result, solver.getAbsoluteAccuracy());
        Assert.assertTrue(solver.getIterationCount() > 0);
    }

    @Test
    public void testLinearFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 1.0;
            }
        };
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(f, 0.0, 1.0);
        Assert.assertEquals(0.5, result, solver.getAbsoluteAccuracy());
    }

    @Test
    public void testPolynomialFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BisectionSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(1E-10);
        double result = solver.solve(f, 1.0, 3.0);
        Assert.assertEquals(2.0, result, 1E-10);
    }

    @Test
    public void testDeprecatedConstructorAndSolveMethods() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        BisectionSolver solver = new BisectionSolver(f);
        double result1 = solver.solve(0.0, 10.0);
        Assert.assertEquals(5.0, result1, solver.getAbsoluteAccuracy());

        double result2 = solver.solve(0.0, 10.0, 2.0);
        Assert.assertEquals(5.0, result2, solver.getAbsoluteAccuracy());

        double result3 = solver.solve(f, 0.0, 10.0, 2.0);
        Assert.assertEquals(5.0, result3, solver.getAbsoluteAccuracy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIntervalMinGreaterThanMax() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BisectionSolver solver = new BisectionSolver();
        solver.solve(f, 4.0, 3.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIntervalMinEqualsMax() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BisectionSolver solver = new BisectionSolver();
        solver.solve(f, 3.0, 3.0);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testMaximalIterationCountExceeded() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        BisectionSolver solver = new BisectionSolver();
        solver.setMaximalIterationCount(2);
        solver.solve(f, 3.0, 4.0);
    }

    @Test
    public void testBranchCoverageRightInterval() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.75;
            }
        };
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(f, 0.0, 1.0);
        Assert.assertEquals(0.75, result, solver.getAbsoluteAccuracy());
    }

    @Test
    public void testBranchCoverageLeftInterval() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.25;
            }
        };
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(f, 0.0, 1.0);
        Assert.assertEquals(0.25, result, solver.getAbsoluteAccuracy());
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testFunctionEvaluationException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x);
            }
        };
        BisectionSolver solver = new BisectionSolver();
        solver.solve(f, 0.0, 1.0);
    }
}
