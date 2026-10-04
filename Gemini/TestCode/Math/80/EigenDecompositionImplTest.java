package org.apache.commons.math.linear;

import org.apache.commons.math.util.MathUtils;
import org.junit.Assert;
import org.junit.Test;

public class EigenDecompositionImplTest {

    @Test
    public void testDimension1() {
        double[] main = new double[] { 5.0 };
        double[] secondary = new double[0];
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        Assert.assertEquals(5.0, ed.getRealEigenvalues()[0], 1e-12);
        Assert.assertEquals(5.0, ed.getRealEigenvalue(0), 1e-12);
        Assert.assertEquals(0.0, ed.getImagEigenvalues()[0], 1e-12);
        Assert.assertEquals(0.0, ed.getImagEigenvalue(0), 1e-12);
        Assert.assertEquals(5.0, ed.getDeterminant(), 1e-12);

        RealMatrix v = ed.getV();
        Assert.assertEquals(1, v.getRowDimension());
        Assert.assertEquals(1.0, v.getEntry(0, 0), 1e-12);

        RealMatrix vt = ed.getVT();
        Assert.assertEquals(1.0, vt.getEntry(0, 0), 1e-12);

        RealMatrix d = ed.getD();
        Assert.assertEquals(5.0, d.getEntry(0, 0), 1e-12);

        RealVector ev = ed.getEigenvector(0);
        Assert.assertEquals(1.0, ev.getEntry(0), 1e-12);

        DecompositionSolver solver = ed.getSolver();
        Assert.assertTrue(solver.isNonSingular());
        double[] b = new double[] { 10.0 };
        double[] x = solver.solve(b);
        Assert.assertEquals(2.0, x[0], 1e-12);

        RealVector bVec = new ArrayRealVector(new double[] { 10.0 });
        RealVector xVec = solver.solve(bVec);
        Assert.assertEquals(2.0, xVec.getEntry(0), 1e-12);

        RealMatrix bMat = MatrixUtils.createRealMatrix(new double[][] { { 10.0, 20.0 } });
        RealMatrix xMat = solver.solve(bMat);
        Assert.assertEquals(2.0, xMat.getEntry(0, 0), 1e-12);
        Assert.assertEquals(4.0, xMat.getEntry(0, 1), 1e-12);

        RealMatrix inv = solver.getInverse();
        Assert.assertEquals(0.2, inv.getEntry(0, 0), 1e-12);
    }

    @Test
    public void testDimension2() {
        double[] main = new double[] { 2.0, 2.0 };
        double[] secondary = new double[] { 1.0 };
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(3.0, ev[0], 1e-12);
        Assert.assertEquals(1.0, ev[1], 1e-12);
        Assert.assertEquals(3.0, ed.getDeterminant(), 1e-12);
    }

    @Test
    public void testDimension3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, -1.0, 0.0 },
            { -1.0, 2.0, -1.0 },
            { 0.0, -1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(2.0 + Math.sqrt(2.0), ev[0], 1e-10);
        Assert.assertEquals(2.0, ev[1], 1e-10);
        Assert.assertEquals(2.0 - Math.sqrt(2.0), ev[2], 1e-10);
        Assert.assertEquals(4.0, ed.getDeterminant(), 1e-10);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test
    public void testDimension4GeneralBlock() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, -2.0, 2.0 },
            { 1.0, 2.0, 0.0, 1.0 },
            { -2.0, 0.0, 3.0, -2.0 },
            { 2.0, 1.0, -2.0, -1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(4, ev.length);
        Assert.assertTrue(ev[0] >= ev[1]);
        Assert.assertTrue(ev[1] >= ev[2]);
        Assert.assertTrue(ev[2] >= ev[3]);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
            }
        }
    }

    @Test
    public void testDimension5() {
        double[] main = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        double[] secondary = new double[] { 0.5, 0.5, 0.5, 0.5 };
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(5, ev.length);
        for (int i = 0; i < 4; ++i) {
            Assert.assertTrue(ev[i] >= ev[i + 1]);
        }
    }

    @Test
    public void testZeroSecondarySplit() {
        double[] main = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] secondary = new double[] { 0.0, 0.0, 0.0 };
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(4.0, ev[0], 1e-12);
        Assert.assertEquals(3.0, ev[1], 1e-12);
        Assert.assertEquals(2.0, ev[2], 1e-12);
        Assert.assertEquals(1.0, ev[3], 1e-12);
    }

    @Test
    public void testRepeatedEigenvaluesDimension3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 0.0, 0.0 },
            { 0.0, 2.0, 0.0 },
            { 0.0, 0.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(2.0, ev[0], 1e-12);
        Assert.assertEquals(2.0, ev[1], 1e-12);
        Assert.assertEquals(2.0, ev[2], 1e-12);
    }

    @Test(expected = InvalidMatrixException.class)
    public void testNonSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        Assert.assertFalse(solver.isNonSingular());
        solver.solve(new double[] { 1.0, 1.0 });
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveRealVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.solve(new ArrayRealVector(new double[] { 1.0, 1.0 }));
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixSolveMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.solve(MatrixUtils.createRealIdentityMatrix(2));
    }

    @Test(expected = SingularMatrixException.class)
    public void testSingularMatrixGetInverse() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.getInverse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchSolveVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new double[] { 1.0, 2.0, 3.0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchSolveRealVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchSolveMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(MatrixUtils.createRealIdentityMatrix(3));
    }

    @Test
    public void testNegativeEigenvaluesAndIndefiniteMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { -2.0, 1.0, 0.0, 0.0 },
            { 1.0, -2.0, 1.0, 0.0 },
            { 0.0, 1.0, 2.0, 1.0 },
            { 0.0, 0.0, 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(4, ev.length);
        Assert.assertTrue(ev[0] > 0);
        Assert.assertTrue(ev[3] < 0);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                Assert.assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
            }
        }
    }

    @Test
    public void testLargerDimension() {
        final int n = 10;
        double[][] data = new double[n][n];
        for (int i = 0; i < n; ++i) {
            data[i][i] = 2.0 * (i + 1);
            if (i > 0) {
                data[i][i - 1] = 1.0;
                data[i - 1][i] = 1.0;
            }
        }
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        Assert.assertEquals(n, ev.length);
        for (int i = 0; i < n - 1; ++i) {
            Assert.assertTrue(ev[i] >= ev[i + 1]);
        }

        DecompositionSolver solver = ed.getSolver();
        Assert.assertTrue(solver.isNonSingular());
        RealMatrix inv = solver.getInverse();
        RealMatrix ident = matrix.multiply(inv);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                Assert.assertEquals(i == j ? 1.0 : 0.0, ident.getEntry(i, j), 1e-8);
            }
        }
    }
}
