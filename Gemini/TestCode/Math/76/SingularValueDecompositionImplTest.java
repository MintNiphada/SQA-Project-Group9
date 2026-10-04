package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

public class SingularValueDecompositionImplTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testSquareMatrixDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();
        RealMatrix reconstructed = u.multiply(s).multiply(v.transpose());

        double normDiff = matrix.subtract(reconstructed).getNorm();
        Assert.assertEquals(0.0, normDiff, EPSILON);

        Assert.assertSame(u, svd.getU());
        Assert.assertSame(s, svd.getS());
        Assert.assertSame(v, svd.getV());
        Assert.assertEquals(u.transpose(), svd.getUT());
        Assert.assertEquals(v.transpose(), svd.getVT());

        double[] sv = svd.getSingularValues();
        Assert.assertEquals(2, sv.length);
        Assert.assertTrue(sv[0] >= sv[1]);
        Assert.assertEquals(sv[0], svd.getNorm(), EPSILON);
        Assert.assertEquals(sv[0] / sv[1], svd.getConditionNumber(), EPSILON);
        Assert.assertEquals(2, svd.getRank());
    }

    @Test
    public void testTallMatrixDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();
        RealMatrix reconstructed = u.multiply(s).multiply(v.transpose());

        double normDiff = matrix.subtract(reconstructed).getNorm();
        Assert.assertEquals(0.0, normDiff, EPSILON);
        Assert.assertEquals(3, u.getRowDimension());
        Assert.assertEquals(2, u.getColumnDimension());
        Assert.assertEquals(2, v.getRowDimension());
        Assert.assertEquals(2, v.getColumnDimension());
    }

    @Test
    public void testFatMatrixDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();
        RealMatrix reconstructed = u.multiply(s).multiply(v.transpose());

        double normDiff = matrix.subtract(reconstructed).getNorm();
        Assert.assertEquals(0.0, normDiff, EPSILON);
        Assert.assertEquals(2, u.getRowDimension());
        Assert.assertEquals(2, u.getColumnDimension());
        Assert.assertEquals(3, v.getRowDimension());
        Assert.assertEquals(2, v.getColumnDimension());
    }

    @Test
    public void testTruncatedDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix, 1);
        Assert.assertEquals(1, svd.getSingularValues().length);
        Assert.assertEquals(3, svd.getU().getRowDimension());
        Assert.assertEquals(1, svd.getU().getColumnDimension());
        Assert.assertEquals(3, svd.getV().getRowDimension());
        Assert.assertEquals(1, svd.getV().getColumnDimension());
    }

    @Test
    public void testRankDeficientMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 3.0, 6.0, 9.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        Assert.assertEquals(1, svd.getRank());
    }

    @Test
    public void testCovariance() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        double[] sv = svd.getSingularValues();

        RealMatrix cov = svd.getCovariance(sv[0] * 0.5);
        Assert.assertNotNull(cov);
        Assert.assertEquals(sv.length, cov.getRowDimension());
        Assert.assertEquals(sv.length, cov.getColumnDimension());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceCutoffTooHigh() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        double[] sv = svd.getSingularValues();
        svd.getCovariance(sv[0] + 1.0);
    }

    @Test
    public void testSolverSquareNonSingular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        DecompositionSolver solver = svd.getSolver();

        Assert.assertTrue(solver.isNonSingular());

        double[] b = new double[] { 5.0, 11.0 };
        double[] x = solver.solve(b);
        Assert.assertEquals(1.0, x[0], EPSILON);
        Assert.assertEquals(2.0, x[1], EPSILON);

        RealVector bVec = new ArrayRealVector(b);
        RealVector xVec = solver.solve(bVec);
        Assert.assertEquals(1.0, xVec.getEntry(0), EPSILON);
        Assert.assertEquals(2.0, xVec.getEntry(1), EPSILON);

        RealMatrix bMat = MatrixUtils.createRealMatrix(new double[][] {
            { 5.0, 10.0 },
            { 11.0, 22.0 }
        });
        RealMatrix xMat = solver.solve(bMat);
        Assert.assertEquals(1.0, xMat.getEntry(0, 0), EPSILON);
        Assert.assertEquals(2.0, xMat.getEntry(1, 0), EPSILON);
        Assert.assertEquals(2.0, xMat.getEntry(0, 1), EPSILON);
        Assert.assertEquals(4.0, xMat.getEntry(1, 1), EPSILON);

        RealMatrix inv = solver.getInverse();
        RealMatrix identity = matrix.multiply(inv);
        Assert.assertEquals(1.0, identity.getEntry(0, 0), EPSILON);
        Assert.assertEquals(0.0, identity.getEntry(0, 1), EPSILON);
        Assert.assertEquals(0.0, identity.getEntry(1, 0), EPSILON);
        Assert.assertEquals(1.0, identity.getEntry(1, 1), EPSILON);
    }

    @Test
    public void testSolverLeastSquaresOverdetermined() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 2.0 },
            { 1.0, 3.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        DecompositionSolver solver = svd.getSolver();

        Assert.assertFalse(solver.isNonSingular());

        double[] b = new double[] { 6.0, 5.0, 7.0 };
        double[] x = solver.solve(b);
        Assert.assertEquals(5.0, x[0], EPSILON);
        Assert.assertEquals(0.5, x[1], EPSILON);
    }

    @Test
    public void testSolverUnderdetermined() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        DecompositionSolver solver = svd.getSolver();

        Assert.assertFalse(solver.isNonSingular());

        double[] b = new double[] { 14.0, 32.0 };
        double[] x = solver.solve(b);
        RealVector result = matrix.operate(new ArrayRealVector(x));
        Assert.assertEquals(b[0], result.getEntry(0), EPSILON);
        Assert.assertEquals(b[1], result.getEntry(1), EPSILON);
    }

    @Test
    public void testCloneImmutability() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 0.0 },
            { 0.0, 1.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        double[] sv1 = svd.getSingularValues();
        sv1[0] = 0.0;
        double[] sv2 = svd.getSingularValues();
        Assert.assertNotEquals(sv1[0], sv2[0], EPSILON);
    }
}
