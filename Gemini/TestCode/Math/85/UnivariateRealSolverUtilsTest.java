package org.apache.commons.math.analysis.solvers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Test;

public class UnivariateRealSolverUtilsTest {

    @Test
    public void testConstructor() throws Exception {
        Constructor<UnivariateRealSolverUtils> constructor = UnivariateRealSolverUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        UnivariateRealSolverUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testSolveSimple() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double root = UnivariateRealSolverUtils.solve(f, 1.0, 3.0);
        Assert.assertEquals(2.0, root, 1e-4);
    }

    @Test
    public void testSolveWithAccuracy() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };
        double root = UnivariateRealSolverUtils.solve(f, 0.0, 5.0, 1e-8);
        Assert.assertEquals(2.0, root, 1e-8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveNullFunction() throws Exception {
        UnivariateRealSolverUtils.solve(null, 1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveNullFunctionWithAccuracy() throws Exception {
        UnivariateRealSolverUtils.solve(null, 1.0, 2.0, 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveBadEndpoints() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.solve(f, 2.0, 1.0);
    }

    @Test
    public void testBracketSuccess() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.5;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(f, 3.0, 0.0, 10.0);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] <= 3.5);
        Assert.assertTrue(result[1] >= 3.5);
        Assert.assertTrue(f.value(result[0]) * f.value(result[1]) <= 0.0);
    }

    @Test
    public void testBracketWithMaxIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.5;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(f, 5.0, 0.0, 10.0, 5);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] <= 5.5);
        Assert.assertTrue(result[1] >= 5.5);
        Assert.assertTrue(f.value(result[0]) * f.value(result[1]) <= 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketNullFunction() throws Exception {
        UnivariateRealSolverUtils.bracket(null, 1.0, 0.0, 2.0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketBadMaxIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInitialBelowLower() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.bracket(f, -1.0, 0.0, 2.0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInitialAboveUpper() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 3.0, 0.0, 2.0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketLowerBoundGTEUpperBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 2.0, 2.0, 2.0, 10);
    }

    @Test(expected = ConvergenceException.class)
    public void testBracketExceedIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 100.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 0.0, -1000.0, 1000.0, 2);
    }

    @Test(expected = ConvergenceException.class)
    public void testBracketHitBoundsWithoutRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 0.0, -2.0, 2.0, 10);
    }

    @Test
    public void testMidpoint() {
        Assert.assertEquals(2.5, UnivariateRealSolverUtils.midpoint(2.0, 3.0), 1e-15);
        Assert.assertEquals(0.0, UnivariateRealSolverUtils.midpoint(-2.0, 2.0), 1e-15);
        Assert.assertEquals(-2.5, UnivariateRealSolverUtils.midpoint(-3.0, -2.0), 1e-15);
    }
}
