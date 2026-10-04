package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

public class CholeskyDecompositionImplTest {

    private final double[][] testData = {
        { 4.0,  12.0, -16.0 },
        { 12.0, 37.0, -43.0 },
        { -16.0, -43.0, 98.0 }
    };

    @Test
    public void testDecompositionDimensionsAndReconstruction() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
        RealMatrix l = chol.getL();
        RealMatrix lt = chol.getLT();

        Assert.assertEquals(matrix.getRowDimension(), l.getRowDimension());
        Assert.assertEquals(matrix.getColumnDimension(), l.getColumnDimension());
        Assert.assertEquals(l, chol.getL());
        Assert.assertEquals(lt, chol.getLT());

        RealMatrix reconstructed = l.multiply(lt);
        double norm = matrix.subtract(reconstructed).getNorm();
        Assert.assertEquals(0.0, norm, 1.0e-12);
    }

    @Test
    public void testDeterminant() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
        Assert.assertEquals(36.0, chol.getDeterminant(), 1.0e-12);
    }

    @Test(expected = NonSquareMatrixException.class)
    public void testNonSquareMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 5.0, 6.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotSymmetricMatrixException.class)
    public void testNotSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 12.0, -16.0 },
            { 11.0, 37.0, -43.0 },
            { -16.0, -43.0, 98.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteMatrixThreshold() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0e-11, 0.0 },
            { 0.0, 1.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteDuringDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test
    public void testCustomThresholds() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0001, 5.0 }
        });
        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix, 1.0e-3, 1.0e-5);
        Assert.assertNotNull(chol.getL());
    }

    @Test
    public void testSolverIsNonSingular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        Assert.assertTrue(solver.isNonSingular());
    }

    @Test
    public void testSolveArray() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        double[] b = { 0.0, 6.0, 39.0 };
        double[] x = solver.solve(b);

        double[] expected = { 1.0, 1.0, 1.0 };
        Assert.assertArrayEquals(expected, x, 1.0e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveArrayDimensionMismatch() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        solver.solve(new double[] { 1.0, 2.0 });
    }

    @Test
    public void testSolveRealVectorImpl() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealVectorImpl b = new RealVectorImpl(new double[] { 0.0, 6.0, 39.0 });
        RealVector x = solver.solve(b);

        Assert.assertEquals(1.0, x.getEntry(0), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(1), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(2), 1.0e-12);
    }

    @Test
    public void testSolveGenericRealVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealVector b = new ArrayRealVector(new double[] { 0.0, 6.0, 39.0 }) {
            private static final long serialVersionUID = 1L;
        };
        RealVector x = solver.solve(b);

        Assert.assertEquals(1.0, x.getEntry(0), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(1), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(2), 1.0e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveGenericRealVectorDimensionMismatch() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealVector b = new ArrayRealVector(new double[] { 1.0, 2.0 }) {
            private static final long serialVersionUID = 1L;
        };
        solver.solve(b);
    }

    @Test
    public void testSolveMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 4.0 },
            { 6.0, 12.0 },
            { 39.0, -16.0 }
        });
        RealMatrix x = solver.solve(b);

        Assert.assertEquals(1.0, x.getEntry(0, 0), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(1, 0), 1.0e-12);
        Assert.assertEquals(1.0, x.getEntry(2, 0), 1.0e-12);

        Assert.assertEquals(1.0, x.getEntry(0, 1), 1.0e-12);
        Assert.assertEquals(0.0, x.getEntry(1, 1), 1.0e-12);
        Assert.assertEquals(0.0, x.getEntry(2, 1), 1.0e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveMatrixDimensionMismatch() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        solver.solve(b);
    }

    @Test
    public void testGetInverse() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealMatrix inverse = solver.getInverse();
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(3);
        double norm = matrix.multiply(inverse).subtract(identity).getNorm();
        Assert.assertEquals(0.0, norm, 1.0e-12);
    }
}
