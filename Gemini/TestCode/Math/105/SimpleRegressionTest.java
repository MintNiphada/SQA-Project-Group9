package org.apache.commons.math.stat.regression;

import org.apache.commons.math.MathException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleRegressionTest {

    private SimpleRegression regression;

    @Before
    public void setUp() {
        regression = new SimpleRegression();
    }

    @Test
    public void testEmptyModel() {
        Assert.assertEquals(0L, regression.getN());
        Assert.assertTrue(Double.isNaN(regression.getSlope()));
        Assert.assertTrue(Double.isNaN(regression.getIntercept()));
        Assert.assertTrue(Double.isNaN(regression.predict(1.0)));
        Assert.assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue(Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue(Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue(Double.isNaN(regression.getR()));
        Assert.assertTrue(Double.isNaN(regression.getRSquare()));
        Assert.assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue(Double.isNaN(regression.getSlopeStdErr()));
    }

    @Test
    public void testOneDataPoint() {
        regression.addData(1.0, 2.0);
        Assert.assertEquals(1L, regression.getN());
        Assert.assertTrue(Double.isNaN(regression.getSlope()));
        Assert.assertTrue(Double.isNaN(regression.getIntercept()));
        Assert.assertTrue(Double.isNaN(regression.predict(1.0)));
        Assert.assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue(Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue(Double.isNaN(regression.getR()));
    }

    @Test
    public void testConstantX() {
        regression.addData(2.0, 3.0);
        regression.addData(2.0, 5.0);
        regression.addData(2.0, 7.0);

        Assert.assertEquals(3L, regression.getN());
        Assert.assertTrue(Double.isNaN(regression.getSlope()));
        Assert.assertTrue(Double.isNaN(regression.getIntercept()));
        Assert.assertTrue(Double.isNaN(regression.predict(2.0)));
        Assert.assertTrue(Double.isNaN(regression.getR()));
        Assert.assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue(Double.isNaN(regression.getSlopeStdErr()));
    }

    @Test
    public void testPerfectPositiveLinearRegression() throws MathException {
        double[][] data = { { 1.0, 2.0 }, { 2.0, 4.0 }, { 3.0, 6.0 }, { 4.0, 8.0 }, { 5.0, 10.0 } };
        regression.addData(data);

        Assert.assertEquals(5L, regression.getN());
        Assert.assertEquals(2.0, regression.getSlope(), 1e-10);
        Assert.assertEquals(0.0, regression.getIntercept(), 1e-10);
        Assert.assertEquals(12.0, regression.predict(6.0), 1e-10);
        Assert.assertEquals(0.0, regression.getSumSquaredErrors(), 1e-10);
        Assert.assertEquals(40.0, regression.getTotalSumSquares(), 1e-10);
        Assert.assertEquals(40.0, regression.getRegressionSumSquares(), 1e-10);
        Assert.assertEquals(0.0, regression.getMeanSquareError(), 1e-10);
        Assert.assertEquals(1.0, regression.getR(), 1e-10);
        Assert.assertEquals(1.0, regression.getRSquare(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlopeStdErr(), 1e-10);
        Assert.assertEquals(0.0, regression.getInterceptStdErr(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlopeConfidenceInterval(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlopeConfidenceInterval(0.01), 1e-10);
    }

    @Test
    public void testNegativeSlope() throws MathException {
        double[][] data = { { 1.0, 10.0 }, { 2.0, 8.0 }, { 3.0, 6.0 }, { 4.0, 4.0 }, { 5.0, 2.0 } };
        regression.addData(data);

        Assert.assertEquals(-2.0, regression.getSlope(), 1e-10);
        Assert.assertEquals(12.0, regression.getIntercept(), 1e-10);
        Assert.assertEquals(0.0, regression.predict(6.0), 1e-10);
        Assert.assertEquals(-1.0, regression.getR(), 1e-10);
        Assert.assertEquals(1.0, regression.getRSquare(), 1e-10);
    }

    @Test
    public void testNonTrivialRegression() throws MathException {
        double[][] data = {
            { 1.0, 2.2 },
            { 2.0, 2.8 },
            { 3.0, 4.5 },
            { 4.0, 3.9 },
            { 5.0, 5.5 }
        };
        regression.addData(data);

        Assert.assertEquals(5L, regression.getN());
        Assert.assertEquals(0.79, regression.getSlope(), 1e-2);
        Assert.assertEquals(1.41, regression.getIntercept(), 1e-2);
        Assert.assertEquals(6.15, regression.predict(6.0), 1e-2);
        Assert.assertTrue(regression.getSumSquaredErrors() > 0.0);
        Assert.assertTrue(regression.getTotalSumSquares() > 0.0);
        Assert.assertTrue(regression.getMeanSquareError() > 0.0);
        Assert.assertTrue(regression.getR() > 0.0);
        Assert.assertTrue(regression.getRSquare() > 0.0 && regression.getRSquare() < 1.0);
        Assert.assertTrue(regression.getInterceptStdErr() > 0.0);
        Assert.assertTrue(regression.getSlopeStdErr() > 0.0);
        Assert.assertTrue(regression.getSlopeConfidenceInterval() > 0.0);
        Assert.assertTrue(regression.getSignificance() > 0.0 && regression.getSignificance() < 1.0);
    }

    @Test
    public void testClear() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        Assert.assertEquals(3L, regression.getN());

        regression.clear();
        Assert.assertEquals(0L, regression.getN());
        Assert.assertTrue(Double.isNaN(regression.getSlope()));
        Assert.assertTrue(Double.isNaN(regression.getTotalSumSquares()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlopeConfidenceIntervalAlphaTooLow() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        regression.getSlopeConfidenceInterval(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlopeConfidenceIntervalAlphaNegative() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        regression.getSlopeConfidenceInterval(-0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlopeConfidenceIntervalAlphaTooHigh() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        regression.getSlopeConfidenceInterval(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlopeConfidenceIntervalAlphaGreaterThanOne() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        regression.getSlopeConfidenceInterval(1.5);
    }

    @Test
    public void testTwoObservationsDegreesOfFreedom() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 5.0);

        Assert.assertEquals(2L, regression.getN());
        Assert.assertEquals(3.0, regression.getSlope(), 1e-10);
        Assert.assertEquals(-1.0, regression.getIntercept(), 1e-10);
        Assert.assertTrue(Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue(Double.isNaN(regression.getSlopeStdErr()));
    }
}
