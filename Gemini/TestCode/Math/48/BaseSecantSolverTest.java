package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    private static class ConcreteSecantSolver extends BaseSecantSolver {
        ConcreteSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                             final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    @Test
    public void testSolveExactRootMin() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        double root = solver.solve(100, f, 2.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-12);
    }

    @Test
    public void testSolveExactRootMax() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        double root = solver.solve(100, f, 2.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(5.0, root, 1e-12);
    }

    @Test
    public void testSolveExactRootIntermediate() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        double root = solver.solve(100, f, 1.0, 5.0, 2.5);
        Assert.assertEquals(3.0, root, 1e-12);
    }

    @Test(expected = NoBracketingException.class)
    public void testSolveNoBracketing() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testIllinoisMethod() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 2.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-10, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.pow(2.0, 1.0 / 3.0), root, 1e-8);
    }

    @Test
    public void testPegasusMethod() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 2.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-10, 1e-14, BaseSecantSolver.Method.PEGASUS);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.pow(2.0, 1.0 / 3.0), root, 1e-8);
    }

    @Test
    public void testRegulaFalsiMethod() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - 3.0;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-8, BaseSecantSolver.Method.REGULA_FALSI);
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.log(3.0), root, 1e-6);
    }

    @Test
    public void testAllowedSolutionsByIntervalConvergence() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 2.0;
            }
        };

        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-4, 1e-15, BaseSecantSolver.Method.ILLINOIS);

        double rootLeft = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rootLeft <= Math.pow(2.0, 1.0 / 3.0));

        double rootRight = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rootRight >= Math.pow(2.0, 1.0 / 3.0));

        double rootBelow = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0);

        double rootAbove = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0);
    }

    @Test
    public void testAllowedSolutionsByFunctionValueAccuracy() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-14, 0.5, BaseSecantSolver.Method.ILLINOIS);

        double rootAny = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootAny)) <= 0.5);

        double rootLeft = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootLeft)) <= 0.5);

        double rootRight = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(Math.abs(f.value(rootRight)) <= 0.5);

        double rootBelow = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test
    public void testInvertedIntervalSolution() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 - x;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-8, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        double rootLeft = solver.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        Assert.assertEquals(2.0, rootLeft, 1e-6);

        double rootRight = solver.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(2.0, rootRight, 1e-6);
    }

    @Test
    public void testSolveWithDefaultStartValue() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };
        ConcreteSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(1.5, root, 1e-6);
    }
}
