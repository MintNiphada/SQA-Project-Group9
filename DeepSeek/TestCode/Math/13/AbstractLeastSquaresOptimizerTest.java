package org.apache.commons.math3.optimization.general;

import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.InitialGuess;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.Target;
import org.apache.commons.math3.optimization.Weight;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractLeastSquaresOptimizerTest {

    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        public TestOptimizer() {
            super();
        }
        public TestOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            // simply return the start point as the solution
            double[] p = getStartPoint();
            double[] obj = computeObjectiveValue(p);
            return new PointVectorValuePair(p, obj);
        }

        // exposed protected methods for testing
        public void callUpdateJacobian() {
            updateJacobian();
        }
        public void callUpdateResidualsAndCost() {
            updateResidualsAndCost();
        }
        public RealMatrix callComputeWeightedJacobian(double[] params) {
            return computeWeightedJacobian(params);
        }
        public double[] publicComputeResiduals(double[] objectiveValue) {
            return computeResiduals(objectiveValue);
        }
        public double publicComputeCost(double[] residuals) {
            return computeCost(residuals);
        }
        public void callSetCost(double cost) {
            setCost(cost);
        }
    }

    // Simple linear function f(p) = A * p + b, with Jacobian = A
    private static class LinearFunction implements DifferentiableMultivariateVectorFunction {
        private final RealMatrix A;
        private final double[] b;
        public LinearFunction(double[][] a, double[] b) {
            this.A = MatrixUtils.createRealMatrix(a);
            this.b = b.clone();
        }
        @Override
        public double[] value(double[] p) {
            double[] result = A.operate(p);
            for (int i = 0; i < result.length; i++) {
                result[i] += b[i];
            }
            return result;
        }
        @Override
        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] point) {
                    return A.getData();
                }
            };
        }
    }

    // Helper to construct a TestOptimizer with given data and run a dummy optimization
    private TestOptimizer createAndsetup(double[] startPoint, double[] target, double[] weights,
                                         DifferentiableMultivariateVectorFunction f) {
        TestOptimizer optimer = new TestOptimizer();
        optimer.optimize(100, f, target, weights, startPoint);
        return optimer;
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeWeightedJacobianDimensionMismatch() {
        double[] start = {1, 1};
        double[] target = {2, 3};
        double[] weights = {1, 1};
        // function returning wrong size (1 instead of 2)
        DifferentiableMultivariateVectorFunction f = new DifferentiableMultivariateVectorFunction() {
            @Override
            public double[] value(double[] p) {
                return new double[] { p[0] };
            }
            @Override
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    @Override
                    public double[][] value(double[] point) {
                        return new double[][] { {1, 0} };
                    }
                };
            }
        };
        TestOptimizer optimer = createAndsetup(start, target, weights, f);
        optimer.callComputeWeightedJacobian(start);
    }

    @Test
    public void testComputeWeightedJacobian() {
        double[][] A = {{1, 0}, {0, 1}};
        double[] b = {0, 0};
        LinearFunction f = new LinearFunction(A, b);
        double[] start = {1, 1};
        double[] target = {2, 3};
        double[] weights = {1, 1};
        TestOptimizer optimizer = createAndsetup(start, target, weights, f);
        RealMatrix jacobian = optimizer.callComputeWeightedJacobian(start);
        assertEquals(2, jacobian.getRowDimension());
        assertEquals(2, jacobian.getColumnDimension());
        // weight sqrt = identity, Jacobian of f is A = I, so weighted Jacobian = I * I = I
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(i == j ? 1.0 : 0.0, jacobian.getEntry(i, j), 1e-12);
            }
        }
        assertEquals(1, optimizer.getJacobianEvaluations());
        // second call increments
        optimizer.callComputeWeightedJacobian(start);
        assertEquals(2, optimizer.getJacobianEvaluations());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        double[] start = {1, 1};
        double[] target = {2, 3};
        double[] weights = {1, 1};
        DifferentiableMultivariateVectorFunction f = new LinearFunction(new double[][] {{1,0},{0,1}}, new double[]{0,0});
        TestOptimizer optimizer = createAndsetup(start, target, weights, f);
        optimizer.publicComputeResiduals(new double[] {1}); // wrong size
    }

    @Test
    public void testComputeResiduals() {
        double[][] A = {{1, 0}, {0, 1}};
        double[] b = {0, 0};
        DifferentiableMultivariateVectorFunction f = new LinearFunction(A, b);
        double[] start = {1, 1};
        double[] target = {2, 3};
        double[] weights = {1, 1};
        TestOptimizer optimizer = createAndsetup(start, target, weights, f);
        double[] objective = f.value(start);
        double[] residuals = optimizer.publicComputeResiduals(objective);
        assertArrayEquals(new double[] {1, 2}, residuals, 1e-12);
    }

    @Test
    public void testComputeCostIdentityWeight() {
        double[] residuals = {3, 4};
        TestOptimizer optimizer = new TestOptimizer();
        // Weights will be set via a dummy run, but for this test we can manually set weight via setUp?
        // We'll use createAndsetup, then set fake residuals, compute cost.
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        double cost = opt.publicComputeCost(residuals);
        // weight diagonal [1,1], weight matrix = I, dot product = (3*3 + 4*4) = 25, sqrt = 5
        assertEquals(5.0, cost, 1e-12);
    }

    @Test
    public void testComputeCostCustomWeight() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {4, 9}; // diagonal weight diag(4,9)
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        double[] residuals = {3, 4}; // residuals
        // weight matrix diag(4,9), weight * residuals = [12,36], dot = 12*3 + 36*4 = 36+144=180, sqrt(180)
        double cost = opt.publicComputeCost(residuals);
        assertEquals(Math.sqrt(180), cost, 1e-12);
    }

    @Test
    public void testRMSandChiSquare() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        // set cost manually
        opt.callSetCost(5);
        assertEquals(5.0, opt.getRMS(), 1e-12); // rms = sqrt(chi^2 / rows) = sqrt(25/2) = sqrt(12.5)
        assertEquals(Math.sqrt(12.5), opt.getRMS(), 1e-12);
        assertEquals(25.0, opt.getChiSquare(), 1e-12);
    }

    @Test
    public void testGetWeightSquareRoot() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {4, 9};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        RealMatrix sqrtW = opt.getWeightSquareRoot();
        // weights = [4,9] => weight = diag(4,9), sqrtW = diag(2,3)
        assertEquals(2.0, sqrtW.getEntry(0,0), 1e-12);
        assertEquals(0.0, sqrtW.getEntry(0,1), 1e-12);
        assertEquals(0.0, sqrtW.getEntry(1,0), 1e-12);
        assertEquals(3.0, sqrtW.getEntry(1,1), 1e-12);
    }

    @Test
    public void testUpdateJacobian() {
        double[][] A = {{1, 2}, {3, 4}};
        double[] b = {0, 0};
        double[] start = {1, 1};
        double[] target = {2, 3};
        double[] weights = {1, 1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        opt.callUpdateJacobian();
        double[][] storedJac = opt.weightedResidualJacobian;
        RealMatrix expected = MatrixUtils.createRealMatrix(A).scalarMultiply(-1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(expected.getEntry(i, j), storedJac[i][j], 1e-12);
            }
        }
    }

    @Test
    public void testUpdateResidualsAndCost() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        opt.callUpdateResidualsAndCost();
        // cost = sqrt( (target - f(start))^2 ) = sqrt(1^2+2^2)= sqrt(5)
        assertEquals(Math.sqrt(5), opt.cost, 1e-12);
        // weight residuals = I * residuals = [1,2]
        double[] expectedWeighted = {1, 2};
        assertArrayEquals(expectedWeighted, opt.weightedResiduals, 1e-12);
    }

    @Test
    public void testComputeCovariances() {
        double[][] A = {{2, 0}, {0, 3}}; // J = diag(2,3)
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        double[][] cov = opt.computeCovariances(start, 1e-14);
        // J^T J = diag(4,9). Inverse diag(1/4,1/9)
        assertEquals(2, cov.length);
        assertEquals(0.25, cov[0][0], 1e-10);
        assertEquals(0.0, cov[0][1], 1e-10);
        assertEquals(0.0, cov[1][0], 1e-10);
        assertEquals(1.0/9.0, cov[1][1], 1e-10);
    }

    @Test(expected = SingularMatrixException.class)
    public void testComputeCovariancesSingular() {
        double[][] A = {{0,0},{0,0}}; // zero Jacoban -> singular
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        opt.computeCovariances(start, 1e-14);
    }

    @Test
    public void testComputeSigma() {
        double[][] A = {{2, 0}, {0, 3}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        double[] sigma = opt.computeSigma(start, 1e-14);
        assertEquals(0.5, sigma[0], 1e-10);
        assertEquals(1.0/3.0, sigma[1], 1e-10);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testGuessParametersErrorsDegreesOfFreedom() {
        // rows (target length) = 2, cols = 2 => rows <= cols => NO_DEGREES_OF_FREEDOM
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3}; // 2 observations
        double[] weights = {1,1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        // need cost set, because guessParametersErrors uses getChiSquare
        opt.callSetCost(10);
        opt.guessParametersErrors(); // should throw
    }

    @Test
    public void testGuessParametersErrors() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3, 4}; // 3 observations, cols=2 => rows > cols
        double[] weights = {1,1, 1};
        TestOptimizer opt = createAndsetup(start, target, weights, new LinearFunction(A,b));
        opt.callSetCost(5); // set cost manually, chi^2 = 25
        double[] errors = opt.guessParametersErrors();
        // chi^2 = 25, rows=3, cols=2 => c = sqrt(25/(3-2)) = 5
        // cov matrix from J=I => J^T J = I, inverse = I => cov[i][i] = 1
        // error[i] = sqrt(1) * 5 = 5
        assertEquals(2, errors.length);
        assertEquals(5.0, errors[0], 1e-12);
        assertEquals(5.0, errors[1], 1e-12);
    }

    @Test
    public void testDeprecatedOptimizeWithDifferentiable() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        DifferentiableMultivariateVectorFunction f = new LinearFunction(A,b);
        TestOptimizer optimizer = new TestOptimizer();
        PointVectorValuePair result = optimizer.optimize(100, f, target, weights, start);
        assertNotNull(result);
        assertArrayEquals(start, result.getPoint(), 1e-12);
        double[] expectedValue = f.value(start);
        assertArrayEquals(expectedValue, result.getValue(), 1e-12);
    }

    @Test
    public void testDeprecatedOptimizeWithMultivariateDifferentiable() {
        double[][] A = {{1,0},{0,1}};
        double[] b = {0,0};
        double[] start = {1,1};
        double[] target = {2,3};
        double[] weights = {1,1};
        DifferentiableMultivariateVectorFunction diffFunc = new LinearFunction(A,b);
        MultivariateDifferentiableVectorFunction f = FunctionUtils.toMultivariateDifferentiableVectorFunction(diffFunc);
        TestOptimizer optimizer = new TestOptimizer();
        PointVectorValuePair result = optimizer.optimize(100, f, target, weights, start);
        assertNotNull(result);
        assertArrayEquals(start, result.getPoint(), 1e-12);
    }

    @Test
    public void testConstructorWithChecker() {
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        };
        TestOptimizer opt = new TestOptimizer(checker);
        assertNotNull(opt.getConvergenceChecker());
        // just verify no exception
    }
}
