package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChiSquareTestImplTest {

    private ChiSquareTestImpl testStatistic;

    @Before
    public void setUp() {
        testStatistic = new ChiSquareTestImpl();
    }

    @Test
    public void testConstructorsAndSetDistribution() {
        ChiSquaredDistribution customDist = new ChiSquaredDistributionImpl(2.0);
        ChiSquareTestImpl customTest = new ChiSquareTestImpl(customDist);
        Assert.assertNotNull(customTest);

        testStatistic.setDistribution(customDist);
        Assert.assertNotNull(testStatistic.getDistributionFactory());
    }

    @Test
    public void testChiSquareGoodnessOfFit() throws Exception {
        double[] expected = new double[] { 500, 500 };
        long[] observed = new long[] { 500, 500 };
        Assert.assertEquals(0.0, testStatistic.chiSquare(expected, observed), 1E-10);
        Assert.assertEquals(1.0, testStatistic.chiSquareTest(expected, observed), 1E-10);
        Assert.assertFalse(testStatistic.chiSquareTest(expected, observed, 0.05));

        expected = new double[] { 0.5, 0.5 };
        observed = new long[] { 0, 1000 };
        Assert.assertTrue(testStatistic.chiSquare(expected, observed) > 0.0);
        Assert.assertTrue(testStatistic.chiSquareTest(expected, observed) < 0.001);
        Assert.assertTrue(testStatistic.chiSquareTest(expected, observed, 0.05));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareSmallLength() {
        testStatistic.chiSquare(new double[] { 1.0 }, new long[] { 1 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareLengthMismatch() {
        testStatistic.chiSquare(new double[] { 1.0, 2.0 }, new long[] { 1, 2, 3 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareNegativeExpected() {
        testStatistic.chiSquare(new double[] { -1.0, 2.0 }, new long[] { 1, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareZeroExpected() {
        testStatistic.chiSquare(new double[] { 0.0, 2.0 }, new long[] { 1, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareNegativeObserved() {
        testStatistic.chiSquare(new double[] { 1.0, 2.0 }, new long[] { -1, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareAlphaZero() throws Exception {
        testStatistic.chiSquareTest(new double[] { 1.0, 2.0 }, new long[] { 1, 2 }, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareAlphaNegative() throws Exception {
        testStatistic.chiSquareTest(new double[] { 1.0, 2.0 }, new long[] { 1, 2 }, -0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareAlphaTooLarge() throws Exception {
        testStatistic.chiSquareTest(new double[] { 1.0, 2.0 }, new long[] { 1, 2 }, 0.51);
    }

    @Test
    public void testChiSquareIndependence() throws Exception {
        long[][] counts = new long[][] { { 40, 60 }, { 60, 40 } };
        double stat = testStatistic.chiSquare(counts);
        Assert.assertEquals(8.0, stat, 1E-10);
        double pVal = testStatistic.chiSquareTest(counts);
        Assert.assertTrue(pVal < 0.01);
        Assert.assertTrue(testStatistic.chiSquareTest(counts, 0.05));
        Assert.assertFalse(testStatistic.chiSquareTest(counts, 0.001));

        long[][] countsNoAssociation = new long[][] { { 50, 50 }, { 50, 50 } };
        Assert.assertEquals(0.0, testStatistic.chiSquare(countsNoAssociation), 1E-10);
        Assert.assertEquals(1.0, testStatistic.chiSquareTest(countsNoAssociation), 1E-10);
        Assert.assertFalse(testStatistic.chiSquareTest(countsNoAssociation, 0.05));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableOneRow() {
        testStatistic.chiSquare(new long[][] { { 10, 20 } });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableOneCol() {
        testStatistic.chiSquare(new long[][] { { 10 }, { 20 } });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableNonRectangular() {
        testStatistic.chiSquare(new long[][] { { 10, 20 }, { 30 } });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableNegativeEntry() {
        testStatistic.chiSquare(new long[][] { { 10, -20 }, { 30, 40 } });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableAlphaZero() throws Exception {
        testStatistic.chiSquareTest(new long[][] { { 10, 20 }, { 30, 40 } }, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTableAlphaTooLarge() throws Exception {
        testStatistic.chiSquareTest(new long[][] { { 10, 20 }, { 30, 40 } }, 0.6);
    }

    @Test
    public void testChiSquareDataSetsComparisonEqualCounts() throws Exception {
        long[] obs1 = new long[] { 10, 20, 30 };
        long[] obs2 = new long[] { 10, 20, 30 };
        Assert.assertEquals(0.0, testStatistic.chiSquareDataSetsComparison(obs1, obs2), 1E-10);
        Assert.assertEquals(1.0, testStatistic.chiSquareTestDataSetsComparison(obs1, obs2), 1E-10);
        Assert.assertFalse(testStatistic.chiSquareTestDataSetsComparison(obs1, obs2, 0.05));

        long[] obs3 = new long[] { 30, 20, 10 };
        double stat = testStatistic.chiSquareDataSetsComparison(obs1, obs3);
        Assert.assertTrue(stat > 0.0);
        double pVal = testStatistic.chiSquareTestDataSetsComparison(obs1, obs3);
        Assert.assertTrue(pVal < 0.05);
        Assert.assertTrue(testStatistic.chiSquareTestDataSetsComparison(obs1, obs3, 0.05));
    }

    @Test
    public void testChiSquareDataSetsComparisonUnequalCounts() throws Exception {
        long[] obs1 = new long[] { 10, 20, 30 };
        long[] obs2 = new long[] { 20, 40, 60 };
        Assert.assertEquals(0.0, testStatistic.chiSquareDataSetsComparison(obs1, obs2), 1E-10);
        Assert.assertEquals(1.0, testStatistic.chiSquareTestDataSetsComparison(obs1, obs2), 1E-10);
        Assert.assertFalse(testStatistic.chiSquareTestDataSetsComparison(obs1, obs2, 0.05));

        long[] obs3 = new long[] { 60, 40, 20 };
        double stat = testStatistic.chiSquareDataSetsComparison(obs1, obs3);
        Assert.assertTrue(stat > 0.0);
        double pVal = testStatistic.chiSquareTestDataSetsComparison(obs1, obs3);
        Assert.assertTrue(pVal < 0.05);
        Assert.assertTrue(testStatistic.chiSquareTestDataSetsComparison(obs1, obs3, 0.05));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsSmallLength() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 1 }, new long[] { 1 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsLengthMismatch() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 1, 2 }, new long[] { 1, 2, 3 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsNegativeEntry1() {
        testStatistic.chiSquareDataSetsComparison(new long[] { -1, 2 }, new long[] { 1, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsNegativeEntry2() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 1, 2 }, new long[] { 1, -2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsAllZero1() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 0, 0 }, new long[] { 1, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsAllZero2() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 1, 2 }, new long[] { 0, 0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsBothZeroEntry() {
        testStatistic.chiSquareDataSetsComparison(new long[] { 0, 2 }, new long[] { 0, 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsAlphaZero() throws Exception {
        testStatistic.chiSquareTestDataSetsComparison(new long[] { 1, 2 }, new long[] { 1, 2 }, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsAlphaTooLarge() throws Exception {
        testStatistic.chiSquareTestDataSetsComparison(new long[] { 1, 2 }, new long[] { 1, 2 }, 0.51);
    }
}
