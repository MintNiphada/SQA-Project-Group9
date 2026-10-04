package org.apache.commons.math.linear;

import org.apache.commons.math.util.MathUtils;
import org.junit.Assert;
import org.junit.Test;

public class EigenDecompositionImplTest {

    @Test
    public void test1x1Matrix() {
        double[] main = new double[] { 5.0 };
        double[] secondary = new double[] {};
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        Assert.assertEquals(5.0, ed.getRealEigenvalue(0), 1.0e-12);
        Assert.assertEquals(0.0, ed.getImagEigenvalue(0), 1.0e-12);
        Assert.assertEquals(5.0, ed.getDeterminant(), 1.0e-12);
        RealVector v = ed.getEigenvector(0);
        Assert.assertEquals(1, v.getDimension());
        Assert.assertEquals(1.0, Math.abs(v.getEntry(0)), 1.0e-12);
        Assert.assertNotNull(ed.getD());
        Assert.assertNotNull(ed.getV());
        Assert.assertNotNull(ed.getVT());
    }

    @Test
    public void test2x2Matrix() {
        double[] main = new double[] { 2.0, 2.0 };
        double[] secondary = new double[] { 1.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] real = ed.getRealEigenvalues();
        Assert.assertEquals(2, real.length);
        Assert.assertEquals(3.0, real[0], 1.0e-12);
        Assert.assertEquals(1.0, real[1], 1.0e-12);
        Assert.assertEquals(3.0, ed.getDeterminant(), 1.0e-12);
        double[] imag = ed.getImagEigenvalues();
        Assert.assertEquals(0.0, imag[0], 1.0e-12);
        Assert.assertEquals(0.0, imag[1], 1.0e-12);
    }

    @Test
    public void test3x3Matrix() {
        double[] main = new double[] { 2.0, 2.0, 2.0 };
        double[] secondary = new double[] { -1.0, -1.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] real = ed.getRealEigenvalues();
        Assert.assertEquals(3, real.length);
        Assert.assertTrue(real[0] > real[1]);
        Assert.assertTrue(real[1] > real[2]);
        Assert.assertEquals(2.0 + Math.sqrt(2.0), real[0], 1.0e-10);
        Assert.assertEquals(2.0, real[1], 1.0e-10);
        Assert.assertEquals(2.0 - Math.sqrt(2.0), real[2], 1.0e-10);
    }

    @Test
    public void testGeneralBlockMatrix() {
        double[] main = new double[] { 4.0, 4.0, 4.0, 4.0, 4.0 };
        double[] secondary = new double[] { 1.0, 1.0, 1.0, 1.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, 0.0);
        double[] real = ed.getRealEigenvalues();
        Assert.assertEquals(5, real.length);
        for (int i = 0; i < 4; ++i) {
            Assert.assertTrue(real[i] >= real[i + 1]);
        }
        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix identity = v.multiply(vt);
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                Assert.assertEquals(i == j ? 1.0 : 0.0, identity.getEntry(i, j), 1.0e-10);
            }
        }
    }

    @Test
    public void testFromRealMatrixSymmetric() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 0.0 },
            { 2.0, 3.0, 4.0 },
            { 0.0, 4.0, 5.0 }
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1.0e-10);
            }
        }
    }

    @Test(expected = InvalidMatrixException.class)
    public void testAsymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
    }

    @Test
    public void testSolverSolveVectorAndMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, 0.0 },
            { 1.0, 4.0, 1.0 },
            { 0.0, 1.0, 4.0 }
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        Assert.assertTrue(solver.isNonSingular());

        double[] b = new double[] { 5.0, 6.0, 5.0 };
        double[] x = solver.solve(b);
        RealVector bVec = new ArrayRealVector(b);
        RealVector xVec = solver.solve(bVec);
        Assert.assertArrayEquals(x, xVec.getData(), 1.0e-10);

        RealVector ax = matrix.operate(xVec);
        Assert.assertEquals(5.0, ax.getEntry(0), 1.0e-10);
        Assert.assertEquals(6.0, ax.getEntry(1), 1.0e-10);
        Assert.assertEquals(5.0, ax.getEntry(2), 1.0e-10);

        RealMatrix bMat = MatrixUtils.createRealIdentityMatrix(3);
        RealMatrix inv = solver.solve(bMat);
        RealMatrix identity = matrix.multiply(inv);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(i == j ? 1.0 : 0.0, identity.getEntry(i, j), 1.0e-10);
            }
        }

        RealMatrix directInv = solver.getInverse();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(inv.getEntry(i, j), directInv.getEntry(i, j), 1.0e-10);
            }
        }
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveVector() {
        double[] main = new double[] { 0.0, 0.0 };
        double[] secondary = new double[] { 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        Assert.assertFalse(solver.isNonSingular());
        solver.solve(new double[] { 1.0, 2.0 });
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveRealVector() {
        double[] main = new double[] { 0.0, 0.0 };
        double[] secondary = new double[] { 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.solve(new ArrayRealVector(new double[] { 1.0, 2.0 }));
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveMatrix() {
        double[] main = new double[] { 0.0, 0.0 };
        double[] secondary = new double[] { 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.solve(MatrixUtils.createRealIdentityMatrix(2));
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixGetInverse() {
        double[] main = new double[] { 0.0, 0.0 };
        double[] secondary = new double[] { 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        ed.getSolver().getInverse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolverDimensionMismatchDoubleArray() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new double[] { 1.0, 2.0, 3.0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolverDimensionMismatchRealVector() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new ArrayRealVector(new double[] { 1.0 }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolverDimensionMismatchRealMatrix() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(MatrixUtils.createRealIdentityMatrix(3));
    }

    @Test
    public void testZeroOffDiagonalMatrix() {
        double[] main = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] secondary = new double[] { 0.0, 0.0, 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] real = ed.getRealEigenvalues();
        Assert.assertEquals(4.0, real[0], 1.0e-12);
        Assert.assertEquals(3.0, real[1], 1.0e-12);
        Assert.assertEquals(2.0, real[2], 1.0e-12);
        Assert.assertEquals(1.0, real[3], 1.0e-12);
    }

    @Test
    public void testIndefiniteMatrixEigenvectors() {
        double[] main = new double[] { -2.0, 0.0, 2.0 };
        double[] secondary = new double[] { 1.0, 1.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        RealVector v0 = ed.getEigenvector(0);
        RealVector v1 = ed.getEigenvector(1);
        RealVector v2 = ed.getEigenvector(2);
        Assert.assertEquals(1.0, v0.getNorm(), 1.0e-10);
        Assert.assertEquals(1.0, v1.getNorm(), 1.0e-10);
        Assert.assertEquals(1.0, v2.getNorm(), 1.0e-10);
    }

    @Test
    public void testLargeTridiagonalMatrix() {
        int n = 20;
        double[] main = new double[n];
        double[] secondary = new double[n - 1];
        for (int i = 0; i < n; ++i) {
            main[i] = 2.0 * (i + 1);
            if (i < n - 1) {
                secondary[i] = 1.0;
            }
        }
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, 1.0e-12);
        double[] eigenvalues = ed.getRealEigenvalues();
        Assert.assertEquals(n, eigenvalues.length);
        for (int i = 0; i < n - 1; ++i) {
            Assert.assertTrue(eigenvalues[i] >= eigenvalues[i + 1]);
        }
        Assert.assertNotNull(ed.getV());
        Assert.assertNotNull(ed.getVT());
    }

    @Test
    public void testRepeatedEigenvalues() {
        double[] main = new double[] { 2.0, 2.0, 2.0, 2.0 };
        double[] secondary = new double[] { 0.0, 0.0, 0.0 };
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        for (double val : eigenvalues) {
            Assert.assertEquals(2.0, val, 1.0e-12);
        }
        Assert.assertEquals(16.0, ed.getDeterminant(), 1.0e-12);
    }
}
