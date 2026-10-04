package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BracketingNthOrderBrentSolverTest {

    @Test
    public void testConstructors() {
        BracketingNthOrderBrentSolver solver1 = new BracketingNthOrderBrentSolver();
        Assert.assertEquals(5, solver1.getMaximalOrder());
        Assert.assertEquals(1e-6, solver1.getAbsoluteAccuracy(), 1e-15);

        BracketingNthOrderBrentSolver solver2 = new BracketingNthOrderBrentSolver(1e-10, 4);
        Assert.assertEquals(4, solver2.getMaximalOrder());
        Assert.assertEquals(1e-10, solver2.getAbsoluteAccuracy(), 1e-15);

        BracketingNthOrderBrentSolver solver3 = new BracketingNthOrderBrentSolver(1e-14, 1e-10, 3);
        Assert.assertEquals(3, solver3.getMaximalOrder());
        Assert.assertEquals(1e-14, solver3.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-10, solver3.getAbsoluteAccuracy(), 1e-15);

        BracketingNthOrderBrentSolver solver4 = new BracketingNthOrderBrentSolver(1e-14, 1e-10, 1e-12, 2);
        Assert.assertEquals(2, solver4.getMaximalOrder());
        Assert.assertEquals(1e-12, solver4.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testOrderTooSmallConstructor2() {
        new BracketingNthOrderBrentSolver(1e-6, 1);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testOrderTooSmallConstructor3() {
        new BracketingNthOrderBrentSolver(1e-14, 1e-6, 1);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testOrderTooSmallConstructor4() {
        new BracketingNthOrderBrentSolver(1e-14, 1e-6, 1e-15, 1);
    }

    @Test
    public void testRootAtInitialGuess() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double root = solver.solve(100, f, 0.0, 5.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-15);
        Assert.assertEquals(1, solver.getEvaluations());
    }

    @Test
    public void testRootAtMinEndpoint() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 5.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(1.0, root, 1e-15);
    }

    @Test
    public void testRootAtMaxEndpoint() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 5.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(5.0, root, 1e-15);
    }

    @Test(expected = NoBracketingException.class)
    public void testNoBracketingException() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0) + 1.0;
            }
        };
        solver.solve(100, f, 0.0, 4.0, 2.0, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testSinFunctionRoots() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-12, 1e-10, 5);
        SinFunction f = new SinFunction();
        double root = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.PI, root, 1e-10);
    }

    @Test
    public void testAllowedSolutionsSides() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-14, 1e-8, 1e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.expm1(x);
            }
        };

        double left = solver.solve(100, f, -1.0, 1.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(left <= 0.0);
        Assert.assertEquals(0.0, left, 1e-7);

        double right = solver.solve(100, f, -1.0, 1.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(right >= 0.0);
        Assert.assertEquals(0.0, right, 1e-7);

        double below = solver.solve(100, f, -1.0, 1.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(below) <= 0.0);
        Assert.assertEquals(0.0, below, 1e-7);

        double above = solver.solve(100, f, -1.0, 1.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(above) >= 0.0);
        Assert.assertEquals(0.0, above, 1e-7);

        UnivariateFunction decreasing = new UnivariateFunction() {
            public double value(double x) {
                return -FastMath.expm1(x);
            }
        };
        double belowDec = solver.solve(100, decreasing, -1.0, 1.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(decreasing.value(belowDec) <= 0.0);

        double aboveDec = solver.solve(100, decreasing, -1.0, 1.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(decreasing.value(aboveDec) >= 0.0);
    }

    @Test
    public void testAgingAndHighOrderBranching() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-14, 1e-12, 1e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.pow(x - 0.5, 9);
            }
        };
        double root = solver.solve(300, f, 0.0, 2.0, 0.1, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.5, root, 1e-2);
    }

    @Test
    public void testShiftArraysAndReducePoints() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-15, 1e-15, 1e-15, 2);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.pow(x, 5) - 3 * x + 1;
            }
        };
        double root = solver.solve(200, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, f.value(root), 1e-12);
    }

    @Test
    public void testExactRootFoundDuringIteration() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-15, 1e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (FastMath.abs(x - 0.5) < 1e-4) {
                    return 0.0;
                }
                return x - 0.5;
            }
        };
        double root = solver.solve(100, f, 0.0, 1.0, 0.2, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.5, root, 1e-3);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-15, 1e-15, 3);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x) - 0.5;
            }
        };
        solver.solve(3, f, 0.0, FastMath.PI, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testBisectionFallbackNonMonotonic() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-12, 1e-10, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x == 0.0) ? -1.0 : (x == 1.0 ? 1.0 : (x < 0.5 ? -1.0 : 1.0));
            }
        };
        double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        Assert.assertTrue(root >= 0.0 && root <= 1.0);
    }
}
