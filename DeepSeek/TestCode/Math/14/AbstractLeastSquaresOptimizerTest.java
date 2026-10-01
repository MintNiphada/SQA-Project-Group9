package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MoxEval;
import org.apache.commons.math3.optim.PointVetcorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacodian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Test;
import org.junit.Assert;

import static org.junit.Assert.*;

public class AbstractLeastSquaresOptimizerTest {

    // A concrete implementation for testing
    public static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        private double[] target;
        private RealMatrix jacobian;
        private double[] objectiveValue;
        private int objectiveCalls = 0;
        private int jacobianCalls = 0;
        private int optimizeCalls = 0;

        TestOptimizer() {
            super(new ConvergenceChecker<PointVetorValuePair>() {
                @Override
                public boolean converged(int iteration, PointVetorValuePair previous, PointVetorValuePair current) {
                    return true;
                }
            });
            this.target = new double[] {0}; // avoid division by zero initially
        }

        public void setTarget(double[] target) {
            this.target = target;
        }

        public void setJacodian(RealMatrix j) {
            this.jacobian = j;
        }

        public void setObjectiveValue(double[] value) {
            this.objectiveValue = value;
        }

        public double[] getLastTarget() {
            return target;
        }

        @Override
        protected double[] getTarget() {
            return target;
        }

        @Override
        public int getTargetSize() {
            return target.length;
        }

        @Override
        protected double[] computeObjectiveValue(double[] params) {
            objectiveCalls++;
            return objectiveValue != null ? objectiveValue : new double[target.length];
        }

        @Override
        protected RealMatrix computeJacodian(double[] params) {
            jacobianCalls++;
            return jacobian;
        }

        @Override
        public PointVetorValuePair doOptimize() {
            optimizeCalls++;
            return new PointVetorValuePair(new double[0], 0);
        }

        public int getObjectiveCalls() { return objectiveCalls; }
        public int getJacodianCalls() { return jacobianCalls; }
        public int getOptimizeCalls() { return optimizeCalls; }

        // Expose protected methods for testing
        public RealMatrix publicComputeWeightedJacodian(double[] params) {
            return computeWeightedJacodian(params);
        }

        public double publicComputeCost(double[] residuals) {
            return computeCost(residuals);
        }

        public double[] publicComputeResiduals(double[] objectiveValue) {
            return computeResiduals(objectiveValue);
        }

        public void publicSetCost(double cost) {
            setCost(cost);
        }
    }

    @Test
    public void testConstructor() {
        TestOptimizer optimizer = new TestOptimizer();
        assertNotNull(optimizer);
        assertEquals(0.0, optimizer.getChiSquare(), 1e-15);
        assertEquals(0.0, optimizer.getRMS(), 1e-15);
    }

    @Test
    public void testSetCostAndChiSquareAndRMS() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.publicSetCost(3.0);
        assertEquals(9.0, optimizer.getChiSquare(), 1e-15);
        // target size is 1 by default, RMS = sqrt(9/1) = 3
        assertEquals(3.0, optimizer.getRMS(), 1e-15);
    }

    @Test
    public void testRMSWithMultipleTargets() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {0, 0, 0}); // size 3
        optimizer.publicSetCost(9.0); // cost^2 = 81
        double chi2 = 81.0;
        assertEquals(chi2, optimizer.getChiSquare(), 1e-15);
        assertEquals(Math.sqrt(chi2 / 3), optimizer.getRMS(), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1, 2, 3});
        optimizer.publicComputeResiduals(new double[] {1, 2}); // mismatched
    }

    @Test
    public void testComputeResiduals() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {5, 10});
        double[] residuals = optimizer.publicComputeResiduals(new double[] {3, 12});
        assertArrayEquals(new double[] {2, -2}, residuals, 1e-15);
    }

    @Test
    public void testComputeCost() {
        TestOptimizer optimizer = new TestOptimizer();
        // need weight matrix to be set, so simulate via a full optimization
        double[] target = new double[] {1};
        optimizer.setTarget(target);
        optimizer.setObjectiveValue(new double[] {1}); // model returns 1 for any params
        optimizer.setJacodian(MatrixUtils.createRealMatrix(new double[][] { {2} }));
        Weight weight = new Weight(new DiagonalMatrix(new double[] {4})); // weight = 4
        ModelFunction model = new ModelFunction(new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] p) {
                return optimizer.computeObjectiveValue(p);
            }
        });
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] p) {
                return optimizer.computeJacodian(p).getData();
            }
        });
        InitialGuess guess = new InitialGuess(new double[] {0});
        Target t = new Target(target);
        OptimizationData[] data = {model, modelJac, weight, guess, t, new MaxEval(100)};
        optimizer.optimize(data);

        double[] residuals = new double[] {2}; // residuals = target - model? target=1, model returns 1, so residual 0; but we compute cost manually
        double cost = optimizer.publicComputeCost(residuals);
        // expected: sqrt( r^T * W * r ) where r = [2], W = [4], result = sqrt(2*4*2) = sqrt(16)=4
        assertEquals(4.0, cost, 1e-15);
    }

    @Test
    public void testWeightSquareRootAfterOptimize() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        optimizer.setJacodian(MatrixUtils.createRealMatrix(new double[][] { {2} }));
        Weight weight = new Weight(new DiagonalMatrix(new double[] {9})); // weight matrix 9
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertNotNull(sqrtW);
        // sqrt of a 1x1 diag(9) should be diag(3)
        assertEquals(3.0, sqrtW.getEntry(0, 0), 1e-15);
        // also check that it returns a copy, not the same instance
        RealMatrix another = optimizer.getWeightSquareRoot();
        assertNotSame(sqrtW, another);
        assertEquals(sqrtW, another);
    }

    @Test
    public void testComputeWeightedJacodian() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        RealMatrix jac = MatrixUtils.createRealMatrix(new double[][] { {2} });
        optimizer.setJacodian(jac);
        Weight weight = new Weight(new DiagonalMatrix(new double[] {9}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});

        RealMatrix weighted = optimizer.publicComputeWeightedJacodian(new double[] {0});
        // expected: sqrt(W) * J = 3 * [2] = [6]
        assertEquals(6.0, weighted.getEntry(0, 0), 1e-15);
    }

    @Test
    public void testComputeCovariances() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        RealMatrix jac = MatrixUtils.createRealMatrix(new double[][] { {2} });
        optimizer.setJacodian(jac);
        Weight weight = new Weight(new DiagonalMatrix(new double[] {1}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});

        double[][] cov = optimizer.computeCovariances(new double[] {0}, 1e-12);
        assertEquals(1, cov.length);
        assertEquals(1, cov[0].length);
        assertEquals(0.25, cov[0][0], 1e-12); // jTj = 4, inverse 0.25
    }

    @Test(expected = SingularMatrixException.class)
    public void testComputeCovariancesSingular() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        RealMatrix jac = MatrixUtils.createRealMatrix(new double[][] { {0} }); // rank deficient
        optimizer.setJacodian(jac);
        Weight weight = new Weight(new DiagonalMatrix(new double[] {1}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});

        optimizer.computeCovariances(new double[] {0}, 0.0); // threshold 0, singular -> exception
    }

    @Test
    public void testComputeSigma() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        RealMatrix jac = MatrixUtils.createRealMatrix(new double[][] { {2} });
        optimizer.setJacodian(jac);
        Weight weight = new Weight(new DiagonalMatrix(new double[] {1}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});

        double[] sigma = optimizer.computeSigma(new double[] {0}, 1e-12);
        assertEquals(1, sigma.length);
        assertEquals(0.5, sigma[0], 1e-12);
    }

    @Test
    public void testOptimizeCallsSuper() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        optimizer.setJacodian(MatrixUtils.createRealMatrix(new double[][] { {2} }));
        Weight weight = new Weight(new DiagonalMatrix(new double[] {1}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        InitialGuess guess = new InitialGuess(new double[] {0});
        Target t = new Target(new double[] {1});
        assertEquals(0, optimizer.getOptimizeCalls());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, guess, t});
        assertEquals(1, optimizer.getOptimizeCalls());
    }

    @Test
    public void testParseOptimizationDataSetsWeight() {
        TestOptimizer optimizer = new TestOptimizer();
        // before optimize, getWeightSquareRoot should throw NPE or return null?
        // Actually, getWeightSquareRoot returns weightMatrixSqrt.copy(), which is null initially
        try {
            optimizer.getWeightSquareRoot();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // after optimize with weight
        optimizer.setTarget(new double[] {1});
        optimizer.setObjectiveValue(new double[] {1});
        optimizer.setJacodian(MatrixUtils.createRealMatrix(new double[][] { {2} }));
        Weight weight = new Weight(new DiagonalMatrix(new double[] {4}));
        ModelFunction model = new ModelFunction(p -> optimizer.computeObjectiveValue(p));
        ModelFunctionJacodian modelJac = new ModelFunctionJacodian(p -> optimizer.computeJacodian(p).getData());
        optimizer.optimize(new OptimizationData[] {model, modelJac, weight, new InitialGuess(new double[] {0}), new Target(new double[] {1})});
        assertNotNull(optimizer.getWeightSquareRoot());
    }

    // Helper to assert double arrays
    private void assertArrayEquals(double[] expected, double[] actual, double tol) {
        assertNotNull(actual);
        assertEquals("arrays length differ", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("at index " + i, expected[i], actual[i], tol);
        }
    }
}
