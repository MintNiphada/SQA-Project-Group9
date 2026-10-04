package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    private static class TestSecantSolver extends BaseSecantSolver {
        public TestSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        public TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        public TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                                final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    @Test
    public void testConstructors() {
        BaseSecantSolver s1 = new TestSecantSolver(1e-4, BaseSecantSolver.Method.ILLINOIS);
        Assert.assertEquals(1e-4, s1.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver s2 = new TestSecantSolver(1e-6, 1e-4, BaseSecantSolver.Method.PEGASUS);
        Assert.assertEquals(1e-6, s2.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-4, s2.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver s3 = new TestSecantSolver(1e-6, 1e-4, 1e-3, BaseSecantSolver.Method.REGULA_FALSI);
        Assert.assertEquals(1e-6, s3.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-4, s3.getAbsoluteAccuracy(), 1e-15);
        Assert.assertEquals(1e-3, s3.getFunctionValueAccuracy(), 1e-15);
    }

    @Test
    public void testExactRootsAtEndpoints() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0);
            }
        };

        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rootMin = solver.solve(100, f, 1.0, 3.0);
        Assert.assertEquals(1.0, rootMin, 1e-15);

        double rootMax = solver.solve(100, f, 0.0, 2.0);
        Assert.assertEquals(2.0, rootMax, 1e-15);
    }

    @Test
    public void testExactRootInside() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };

        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, f, 0.0, 5.0, 1.0);
        Assert.assertEquals(2.0, root, 1e-15);
    }

    @Test(expected = NoBracketingException.class)
    public void testNoBracketing() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(100, f, 1.0, 5.0);
    }

    @Test
    public void testAllowedSolutionsPegasus() {
        UnivariateRealFunction f = new SinFunction();
        BaseSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, BaseSecantSolver.Method.PEGASUS);

        double rootAny = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI, rootAny, 1e-5);

        double rootLeft = solver.solve(100, f, 3.0, 4.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rootLeft <= Math.PI);

        double rootRight = solver.solve(100, f, 3.0, 4.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rootRight >= Math.PI);

        double rootBelow = solver.solve(100, f, 3.0, 4.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test
    public void testAllowedSolutionsIllinois() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - Math.E;
            }
        };
        BaseSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, BaseSecantSolver.Method.ILLINOIS);

        double rootAny = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(1.0, rootAny, 1e-5);

        double rootLeft = solver.solve(100, f, 0.0, 2.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rootLeft <= 1.0);

        double rootRight = solver.solve(100, f, 0.0, 2.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rootRight >= 1.0);

        double rootBelow = solver.solve(100, f, 0.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test
    public void testAllowedSolutionsRegulaFalsi() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 8.0;
            }
        };
        BaseSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, BaseSecantSolver.Method.REGULA_FALSI);

        double rootAny = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, rootAny, 1e-5);

        double rootLeft = solver.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rootLeft <= 2.0);

        double rootRight = solver.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rootRight >= 2.0);

        double rootBelow = solver.solve(100, f, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test
    public void testFunctionValueAccuracyTermination() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.5;
            }
        };

        BaseSecantSolver solver = new TestSecantSolver(1e-15, 1e-15, 0.1, BaseSecantSolver.Method.ILLINOIS);

        double rootAny = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootAny)) <= 0.1);

        double rootLeft = solver.solve(100, f, 0.0, 1.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootLeft)) <= 0.1);

        double rootRight = solver.solve(100, f, 0.0, 1.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootRight)) <= 0.1);

        double rootBelow = solver.solve(100, f, 0.0, 1.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootBelow)) <= 0.1);

        double rootAbove = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootAbove)) <= 0.1);
    }

    @Test
    public void testDecreasingFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 1.0 - x;
            }
        };

        BaseSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rootBelow = solver.solve(100, f, 0.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testInvalidInterval() {
        UnivariateRealFunction f = new SinFunction();
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(100, f, 4.0, 3.0);
    }
}
