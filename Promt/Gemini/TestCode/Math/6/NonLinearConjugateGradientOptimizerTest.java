package org.apache.commons.math3.optim.nonlinear.scalar.gradient;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.junit.Assert;
import org.junit.Test;

public class NonLinearConjugateGradientOptimizerTest {

    private static class QuadraticFunction implements MultivariateFunction, MultivariateVectorFunction {
        private final double a;
        private final double b;
        private final double c;

        public QuadraticFunction(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        // f(x, y) = a*(x - 2)^2 + b*(y - 3)^2 + c
        public double value(double[] point) {
            double dx = point[0] - 2.0;
            double dy = point[1] - 3.0;
            return a * dx * dx + b * dy * dy + c;
        }

        // grad f = [2*a*(x - 2), 2*b*(y - 3)]
        public double[] value(double[] point) {
            return new double[] {
                2.0 * a * (point[0] - 2.0),
                2.0 * b * (point[1] - 3.0)
            };
        }
    }

    @Test
    public void testMinimizeFletcherReeves() {
        QuadraticFunction q = new QuadraticFunction(1.0, 2.0, 5.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimplePointChecker<PointValuePair>(1e-8, 1e-8));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new ObjectiveFunction(q),
            new ObjectiveFunctionGradient(q),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 10.0, 10.0 }),
            new NonLinearConjugateGradientOptimizer.BracketingStep(0.5)
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(5.0, optimum.getValue(), 1e-4);
    }

    @Test
    public void testMaximizePolakRibiere() {
        // Maximize -((x-2)^2 + (y-3)^2) + 10
        QuadraticFunction q = new QuadraticFunction(-1.0, -1.0, 10.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimplePointChecker<PointValuePair>(1e-8, 1e-8),
                new BrentSolver(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(q),
            new ObjectiveFunctionGradient(q),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(10.0, optimum.getValue(), 1e-4);
    }

    @Test
    public void testCustomPreconditioner() {
        QuadraticFunction q = new QuadraticFunction(2.0, 4.0, 0.0);
        Preconditioner diagPreconditioner = new Preconditioner() {
            public double[] precondition(double[] point, double[] r) {
                return new double[] { r[0] / 4.0, r[1] / 8.0 };
            }
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-9, 1e-9),
                new BrentSolver(),
                diagPreconditioner);

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(q),
            new ObjectiveFunctionGradient(q),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -5.0, 5.0 })
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testBoundsNotSupported() {
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimplePointChecker<PointValuePair>(1e-6, 1e-6));

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) { return 0; }
            }),
            new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return new double[] { 0 }; }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0 }),
            new SimpleBounds(new double[] { -1 }, new double[] { 1 })
        );
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testExceedMaxEvaluations() {
        QuadraticFunction q = new QuadraticFunction(1.0, 1.0, 0.0);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimplePointChecker<PointValuePair>(1e-15, 1e-15));

        optimizer.optimize(
            new MaxEval(2),
            new ObjectiveFunction(q),
            new ObjectiveFunctionGradient(q),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 100.0, 100.0 })
        );
    }

    @Test(expected = MathIllegalStateException.class)
    public void testUnableToBracketOptimum() {
        // Linear function has no minimum and cannot be bracketed
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] + point[1];
            }
        };
        MultivariateVectorFunction g = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0, 1.0 };
            }
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimplePointChecker<PointValuePair>(1e-6, 1e-6));

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            new ObjectiveFunctionGradient(g),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );
    }

    @Test
    public void testIdentityPreconditioner() {
        NonLinearConjugateGradientOptimizer.IdentityPreconditioner ip =
            new NonLinearConjugateGradientOptimizer.IdentityPreconditioner();
        double[] params = new double[] { 1.0, 2.0 };
        double[] r = new double[] { 3.0, -4.0 };
        double[] prec = ip.precondition(params, r);
        Assert.assertArrayEquals(r, prec, 1e-15);
        Assert.assertNotSame(r, prec);
    }

    @Test
    public void testBracketingStepGetter() {
        NonLinearConjugateGradientOptimizer.BracketingStep step =
            new NonLinearConjugateGradientOptimizer.BracketingStep(3.14);
        Assert.assertEquals(3.14, step.getBracketingStep(), 1e-15);
    }

    @Test
    public void testFormulaEnum() {
        NonLinearConjugateGradientOptimizer.Formula[] formulas =
            NonLinearConjugateGradientOptimizer.Formula.values();
        Assert.assertEquals(2, formulas.length);
        Assert.assertEquals(
            NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
            NonLinearConjugateGradientOptimizer.Formula.valueOf("FLETCHER_REEVES")
        );
        Assert.assertEquals(
            NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
            NonLinearConjugateGradientOptimizer.Formula.valueOf("POLAK_RIBIERE")
        );
    }
}
