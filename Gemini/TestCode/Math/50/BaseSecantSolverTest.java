package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    private static class ConcreteBaseSecantSolver extends BaseSecantSolver {
        public ConcreteBaseSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        public ConcreteBaseSecantSolver(final double relativeAccuracy,
                                        final double absoluteAccuracy,
                                        final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        public ConcreteBaseSecantSolver(final double relativeAccuracy,
                                        final double absoluteAccuracy,
                                        final double functionValueAccuracy,
                                        final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    private static final UnivariateRealFunction LINEAR = new UnivariateRealFunction() {
        public double value(double x) {
            return x - 3.0;
        }
    };

    private static final UnivariateRealFunction QUADRATIC = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x - 4.0;
        }
    };

    private static final UnivariateRealFunction QUINTIC = new UnivariateRealFunction() {
        public double value(double x) {
            return (x - 1.0) * (x - 2.0) * (x - 3.0) * (x - 4.0) * (x - 5.0);
        }
    };

    private static final UnivariateRealFunction EXP_DECAY = new UnivariateRealFunction() {
        public double value(double x) {
            return Math.exp(x) - 2.0;
        }
    };

    @Test
    public void testConstructors() {
        BaseSecantSolver s1 = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        Assert.assertEquals(1e-6, s1.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver s2 = new ConcreteBaseSecantSolver(1e-14, 1e-6, BaseSecantSolver.Method.PEGASUS);
        Assert.assertEquals(1e-14, s2.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-6, s2.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver s3 = new ConcreteBaseSecantSolver(1e-14, 1e-6, 1e-10, BaseSecantSolver.Method.REGULA_FALSI);
        Assert.assertEquals(1e-14, s3.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-6, s3.getAbsoluteAccuracy(), 1e-15);
        Assert.assertEquals(1e-10, s3.getFunctionValueAccuracy(), 1e-15);
    }

    @Test
    public void testSolveExactLeftBound() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, LINEAR, 3.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(3.0, root, 1e-15);
    }

    @Test
    public void testSolveExactRightBound() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, LINEAR, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(3.0, root, 1e-15);
    }

    @Test
    public void testSolveExactIntermediate() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, LINEAR, 1.0, 5.0, 3.0);
        Assert.assertEquals(3.0, root, 1e-15);
    }

    @Test(expected = NoBracketingException.class)
    public void testNoBracketing() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(100, QUADRATIC, 3.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testInvalidBounds() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(100, QUADRATIC, 5.0, 3.0, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testMethodsIllinois() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, QUADRATIC, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testMethodsPegasus() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.PEGASUS);
        double root = solver.solve(100, QUADRATIC, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testMethodsRegulaFalsi() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.REGULA_FALSI);
        double root = solver.solve(100, QUADRATIC, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testAllowedSolutionLeftSide() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, EXP_DECAY, 0.0, 2.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(root <= Math.log(2.0));
    }

    @Test
    public void testAllowedSolutionRightSide() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, EXP_DECAY, 0.0, 2.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(root >= Math.log(2.0));
    }

    @Test
    public void testAllowedSolutionBelowSide() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, EXP_DECAY, 0.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(EXP_DECAY.value(root) <= 0.0);
    }

    @Test
    public void testAllowedSolutionAboveSide() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-7, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, EXP_DECAY, 0.0, 2.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(EXP_DECAY.value(root) >= 0.0);
    }

    @Test
    public void testFunctionToleranceConvergenceAllowedSides() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x >= 2.0) ? 1e-8 : -1e-8;
            }
        };

        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rAny = solver.solve(100, f, 0.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertTrue(rAny >= 0.0 && rAny <= 4.0);

        solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rLeft = solver.solve(100, f, 0.0, 4.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rLeft >= 0.0 && rLeft <= 4.0);

        solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rRight = solver.solve(100, f, 0.0, 4.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rRight >= 0.0 && rRight <= 4.0);

        solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rBelow = solver.solve(100, f, 0.0, 4.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(rBelow >= 0.0 && rBelow <= 4.0);

        solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        double rAbove = solver.solve(100, f, 0.0, 4.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(rAbove >= 0.0 && rAbove <= 4.0);
    }

    @Test
    public void testSolveWithStartValue() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, QUINTIC, 0.5, 1.5, 1.2);
        Assert.assertEquals(1.0, root, 1e-5);
    }

    @Test
    public void testSolveWithoutStartValue() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        double root = solver.solve(100, QUINTIC, 0.5, 1.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(1.0, root, 1e-5);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        BaseSecantSolver solver = new ConcreteBaseSecantSolver(1e-14, 1e-14, BaseSecantSolver.Method.REGULA_FALSI);
        solver.solve(2, QUINTIC, 0.5, 1.5, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testEnumValues() {
        BaseSecantSolver.Method[] methods = BaseSecantSolver.Method.values();
        Assert.assertEquals(3, methods.length);
        Assert.assertEquals(BaseSecantSolver.Method.REGULA_FALSI, BaseSecantSolver.Method.valueOf("REGULA_FALSI"));
        Assert.assertEquals(BaseSecantSolver.Method.ILLINOIS, BaseSecantSolver.Method.valueOf("ILLINOIS"));
        Assert.assertEquals(BaseSecantSolver.Method.PEGASUS, BaseSecantSolver.Method.valueOf("PEGASUS"));
    }
}
