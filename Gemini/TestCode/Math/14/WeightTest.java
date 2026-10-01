package org.apache.commons.math3.optim.nonlinear.vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link Weight}.
 */
public class WeightTest {

    private static final double EPSILON = 1e-15;

    @Test
    public void testConstructorFromDoubleArray() {
        double[] diagonalValues = new double[] { 1.5, 2.0, 3.5 };
        Weight weight = new Weight(diagonalValues);
        RealMatrix matrix = weight.getWeight();

        Assert.assertEquals(3, matrix.getRowDimension());
        Assert.assertEquals(3, matrix.getColumnDimension());

        for (int i = 0; i < diagonalValues.length; i++) {
            for (int j = 0; j < diagonalValues.length; j++) {
                if (i == j) {
                    Assert.assertEquals(diagonalValues[i], matrix.getEntry(i, j), EPSILON);
                } else {
                    Assert.assertEquals(0.0, matrix.getEntry(i, j), EPSILON);
                }
            }
        }
    }

    @Test
    public void testConstructorFromEmptyDoubleArray() {
        double[] diagonalValues = new double[0];
        Weight weight = new Weight(diagonalValues);
        RealMatrix matrix = weight.getWeight();

        Assert.assertEquals(0, matrix.getRowDimension());
        Assert.assertEquals(0, matrix.getColumnDimension());
    }

    @Test
    public void testConstructorFromSingleElementDoubleArray() {
        double[] diagonalValues = new double[] { 42.0 };
        Weight weight = new Weight(diagonalValues);
        RealMatrix matrix = weight.getWeight();

        Assert.assertEquals(1, matrix.getRowDimension());
        Assert.assertEquals(1, matrix.getColumnDimension());
        Assert.assertEquals(42.0, matrix.getEntry(0, 0), EPSILON);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorFromNullDoubleArray() {
        new Weight((double[]) null);
    }

    @Test
    public void testConstructorFromSquareMatrix() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix originalMatrix = MatrixUtils.createRealMatrix(data);
        Weight weight = new Weight(originalMatrix);
        RealMatrix retrievedMatrix = weight.getWeight();

        Assert.assertEquals(2, retrievedMatrix.getRowDimension());
        Assert.assertEquals(2, retrievedMatrix.getColumnDimension());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Assert.assertEquals(data[i][j], retrievedMatrix.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testConstructorFromDiagonalMatrix() {
        double[] diag = new double[] { 2.0, 4.0, 8.0 };
        DiagonalMatrix originalMatrix = new DiagonalMatrix(diag);
        Weight weight = new Weight(originalMatrix);
        RealMatrix retrievedMatrix = weight.getWeight();

        Assert.assertEquals(3, retrievedMatrix.getRowDimension());
        Assert.assertEquals(3, retrievedMatrix.getColumnDimension());
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(diag[i], retrievedMatrix.getEntry(i, i), EPSILON);
        }
    }

    @Test
    public void testConstructorFromNonSquareMatrixThrowsException() {
        double[][] data = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 }
        };
        RealMatrix nonSquareMatrix = MatrixUtils.createRealMatrix(data);

        try {
            new Weight(nonSquareMatrix);
            Assert.fail("Expected NonSquareMatrixException to be thrown");
        } catch (NonSquareMatrixException e) {
            Assert.assertEquals(3, e.getArgument(0)); // column dimension
            Assert.assertEquals(2, e.getArgument(1)); // row dimension
        }
    }

    @Test(expected = NonSquareMatrixException.class)
    public void testConstructorFromNonSquareMatrixTall() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 }
        };
        RealMatrix nonSquareMatrix = MatrixUtils.createRealMatrix(data);
        new Weight(nonSquareMatrix);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorFromNullMatrix() {
        new Weight((RealMatrix) null);
    }

    @Test
    public void testImmutabilityOfWeightMatrixFromOriginalMatrix() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix originalMatrix = MatrixUtils.createRealMatrix(data);
        Weight weight = new Weight(originalMatrix);

        // Mutate original matrix
        originalMatrix.setEntry(0, 0, 999.0);

        RealMatrix retrievedMatrix = weight.getWeight();
        Assert.assertEquals(1.0, retrievedMatrix.getEntry(0, 0), EPSILON);
    }

    @Test
    public void testImmutabilityOfReturnedMatrix() {
        double[] data = new double[] { 5.0, 10.0 };
        Weight weight = new Weight(data);

        RealMatrix matrix1 = weight.getWeight();
        matrix1.setEntry(0, 0, 999.0);

        RealMatrix matrix2 = weight.getWeight();
        Assert.assertEquals(5.0, matrix2.getEntry(0, 0), EPSILON);
    }

    @Test
    public void testOptimizationDataInterface() {
        Weight weight = new Weight(new double[] { 1.0 });
        Assert.assertTrue(weight instanceof org.apache.commons.math3.optim.OptimizationData);
    }
}
