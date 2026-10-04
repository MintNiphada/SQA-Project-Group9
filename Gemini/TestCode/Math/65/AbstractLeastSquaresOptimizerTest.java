package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static class DummyOptimizer extends AbstractLeastSquaresOptimizer {
        private boolean shouldUpdateResiduals = false;
        private boolean shouldUpdateJacobian = false;
        private boolean shouldIncrementIterations = false;

        public DummyOptimizer() {
            super();
        }

        public void setFlags(boolean updateResiduals, boolean updateJacobian, boolean incrementIterations) {
            this.shouldUpdateResiduals = updateResiduals;
            this.shouldUpdateJacobian = updateJacobian;
            this.shouldIncrementIterations = incrementIterations;
        }

        public void callUpdateJacobian() throws FunctionEvaluationException {
            updateJacobian();
        }

        public void callUpdateResidualsAndCost() throws FunctionEvaluationException {
            updateResidualsAndCost();
        }

        public void callIncrementIterationsCounter() throws OptimizationException {
            incrementIterationsCounter();
        }

        public double getCost() {
            return cost;
        }

        public double[][] getJacobianMatrix() {
            return jacobian;
        }

        public double[] getPoint() {
            return point;
        }

        public double[] getObjective() {
            return objective;
        }

        public double[] getResiduals() {
            return residuals;
        }

        @Override
        protected VectorialPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
            if (shouldIncrementIterations) {
                incrementIterationsCounter();
            }
            if (shouldUpdateResiduals) {
                updateResidualsAndCost();
            }
            if (shouldUpdateJacobian) {
                updateJacobian();
            }
            return new VectorialPointValuePair(point, objective);
        }
    }

    private DifferentiableMultivariateVectorialFunction createMockFunction(final double[] evalResult, final double[][] jacobianResult) {
        return new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return evalResult;
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return jacobianResult;
                    }
                };
            }
        };
    }

    @Test
    public void testGettersAndSetters() {
        DummyOptimizer optimizer = new DummyOptimizer();
        Assert.assertEquals(AbstractLeastSquaresOptimizer.DEFAULT_MAX_ITERATIONS, optimizer.getMaxIterations());
        Assert.assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        Assert.assertEquals(0, optimizer.getIterations());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getJacobianEvaluations());

        optimizer.setMaxIterations(50);
        Assert.assertEquals(50, optimizer.getMaxIterations());

        optimizer.setMaxEvaluations(100);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());

        VectorialConvergenceChecker checker = new SimpleVectorialValueChecker(1e-4, 1e-4);
        optimizer.setConvergenceChecker(checker);
        Assert.assertSame(checker, optimizer.getConvergenceChecker());
    }

    @Test(expected = OptimizationException.class)
    public void testOptimizeDimensionMismatch() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        DifferentiableMultivariateVectorialFunction f = createMockFunction(new double[]{1.0}, new double[][]{{1.0}});
        optimizer.optimize(f, new double[]{1.0, 2.0}, new double[]{1.0}, new double[]{0.0});
    }

    @Test
    public void testOptimizeSuccessfulExecution() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);

        double[] target = new double[]{2.0, 4.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0};
        double[] funcVal = new double[]{1.0, 2.0};
        double[][] jacobianVal = new double[][]{{2.0}, {3.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        VectorialPointValuePair result = optimizer.optimize(f, target, weights, startPoint);

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(startPoint, result.getPointRef(), 1e-10);
        Assert.assertArrayEquals(funcVal, result.getValueRef(), 1e-10);
        Assert.assertEquals(1, optimizer.getIterations());
        Assert.assertEquals(1, optimizer.getEvaluations());
        Assert.assertEquals(1, optimizer.getJacobianEvaluations());
        Assert.assertEquals(Math.sqrt(1.0 * 1.0 * 1.0 + 1.0 * 2.0 * 2.0), optimizer.getCost(), 1e-10);
    }

    @Test
    public void testIncrementIterationsCounterExceeded() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setMaxIterations(2);
        try {
            optimizer.callIncrementIterationsCounter();
            optimizer.callIncrementIterationsCounter();
            optimizer.callIncrementIterationsCounter();
            Assert.fail("Expected OptimizationException");
        } catch (OptimizationException e) {
            Assert.assertTrue(e.getCause() instanceof MaxIterationsExceededException);
        }
    }

    @Test
    public void testUpdateJacobianDimensionMismatch() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        double[] target = new double[]{2.0, 4.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0};
        double[] funcVal = new double[]{1.0, 2.0};
        double[][] jacobianVal = new double[][]{{2.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        try {
            optimizer.callUpdateJacobian();
            Assert.fail("Expected FunctionEvaluationException");
        } catch (FunctionEvaluationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUpdateResidualsAndCostEvaluationExceeded() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setMaxEvaluations(1);
        double[] target = new double[]{2.0};
        double[] weights = new double[]{1.0};
        double[] startPoint = new double[]{0.0};
        double[] funcVal = new double[]{1.0};
        double[][] jacobianVal = new double[][]{{2.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        optimizer.callUpdateResidualsAndCost();
        try {
            optimizer.callUpdateResidualsAndCost();
            Assert.fail("Expected FunctionEvaluationException");
        } catch (FunctionEvaluationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUpdateResidualsAndCostDimensionMismatch() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        double[] target = new double[]{2.0, 3.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0};
        double[] funcVal = new double[]{1.0};
        double[][] jacobianVal = new double[][]{{2.0}, {2.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        try {
            optimizer.callUpdateResidualsAndCost();
            Assert.fail("Expected FunctionEvaluationException");
        } catch (FunctionEvaluationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetRMSAndChiSquare() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);
        double[] target = new double[]{3.0, 5.0};
        double[] weights = new double[]{2.0, 3.0};
        double[] startPoint = new double[]{1.0};
        double[] funcVal = new double[]{1.0, 1.0};
        double[][] jacobianVal = new double[][]{{1.0}, {1.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        double r0 = 2.0;
        double r1 = 4.0;
        double expectedCriterion = r0 * r0 * 2.0 + r1 * r1 * 3.0;
        double expectedRMS = Math.sqrt(expectedCriterion / 2.0);
        Assert.assertEquals(expectedRMS, optimizer.getRMS(), 1e-10);

        double expectedChiSquare = (r0 * r0) / 2.0 + (r1 * r1) / 3.0;
        Assert.assertEquals(expectedChiSquare, optimizer.getChiSquare(), 1e-10);
    }

    @Test
    public void testGetCovariances() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);
        double[] target = new double[]{0.0, 0.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};
        double[] funcVal = new double[]{0.0, 0.0};
        double[][] jacobianVal = new double[][]{{1.0, 0.0}, {0.0, 2.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        double[][] covar = optimizer.getCovariances();
        Assert.assertEquals(2, covar.length);
        Assert.assertEquals(2, covar[0].length);
        Assert.assertEquals(1.0, covar[0][0], 1e-10);
        Assert.assertEquals(0.0, covar[0][1], 1e-10);
        Assert.assertEquals(0.0, covar[1][0], 1e-10);
        Assert.assertEquals(0.25, covar[1][1], 1e-10);
    }

    @Test(expected = OptimizationException.class)
    public void testGetCovariancesSingularMatrix() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);
        double[] target = new double[]{0.0, 0.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};
        double[] funcVal = new double[]{0.0, 0.0};
        double[][] jacobianVal = new double[][]{{1.0, 1.0}, {1.0, 1.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        optimizer.getCovariances();
    }

    @Test
    public void testGuessParametersErrors() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);
        double[] target = new double[]{1.0, 2.0, 3.0};
        double[] weights = new double[]{1.0, 1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};
        double[] funcVal = new double[]{0.0, 0.0, 0.0};
        double[][] jacobianVal = new double[][]{{1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        double[] errors = optimizer.guessParametersErrors();
        Assert.assertEquals(2, errors.length);
        Assert.assertTrue(errors[0] > 0);
        Assert.assertTrue(errors[1] > 0);
    }

    @Test(expected = OptimizationException.class)
    public void testGuessParametersErrorsNoDegreesOfFreedom() throws Exception {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.setFlags(true, true, true);
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};
        double[] funcVal = new double[]{0.0, 0.0};
        double[][] jacobianVal = new double[][]{{1.0, 0.0}, {0.0, 1.0}};

        DifferentiableMultivariateVectorialFunction f = createMockFunction(funcVal, jacobianVal);
        optimizer.optimize(f, target, weights, startPoint);

        optimizer.guessParametersErrors();
    }
}
