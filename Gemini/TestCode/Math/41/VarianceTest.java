package org.apache.commons.math.stat.descriptive.moment;

import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

public class VarianceTest {

    private static final double TOLERANCE = 1E-12;

    @Test
    public void testDefaultConstructorAndIncrements() {
        Variance v = new Variance();
        Assert.assertTrue(v.isBiasCorrected());
        Assert.assertTrue(Double.isNaN(v.getResult()));
        Assert.assertEquals(0L, v.getN());

        v.increment(10.0);
        Assert.assertEquals(1L, v.getN());
        Assert.assertEquals(0.0, v.getResult(), TOLERANCE);

        v.increment(20.0);
        Assert.assertEquals(2L, v.getN());
        Assert.assertEquals(50.0, v.getResult(), TOLERANCE);

        v.increment(30.0);
        Assert.assertEquals(3L, v.getN());
        Assert.assertEquals(100.0, v.getResult(), TOLERANCE);

        v.clear();
        Assert.assertEquals(0L, v.getN());
        Assert.assertTrue(Double.isNaN(v.getResult()));
    }

    @Test
    public void testPopulationVarianceIncrement() {
        Variance v = new Variance(false);
        Assert.assertFalse(v.isBiasCorrected());

        v.increment(10.0);
        Assert.assertEquals(0.0, v.getResult(), TOLERANCE);

        v.increment(20.0);
        v.increment(30.0);
        Assert.assertEquals(3L, v.getN());
        Assert.assertEquals(200.0 / 3.0, v.getResult(), TOLERANCE);

        v.setBiasCorrected(true);
        Assert.assertTrue(v.isBiasCorrected());
        Assert.assertEquals(100.0, v.getResult(), TOLERANCE);
    }

    @Test
    public void testExternalMomentConstructor() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(m2);
        Assert.assertTrue(v.isBiasCorrected());

        v.increment(10.0);
        Assert.assertEquals(0L, v.getN());

        m2.increment(10.0);
        m2.increment(20.0);
        m2.increment(30.0);

        Assert.assertEquals(3L, v.getN());
        Assert.assertEquals(100.0, v.getResult(), TOLERANCE);

        v.clear();
        Assert.assertEquals(3L, v.getN());

        Variance vPop = new Variance(false, m2);
        Assert.assertFalse(vPop.isBiasCorrected());
        Assert.assertEquals(200.0 / 3.0, vPop.getResult(), TOLERANCE);
    }

    @Test
    public void testCopyConstructorAndCopy() {
        Variance original = new Variance(false);
        original.increment(5.0);
        original.increment(15.0);

        Variance copy1 = new Variance(original);
        Assert.assertFalse(copy1.isBiasCorrected());
        Assert.assertEquals(original.getN(), copy1.getN());
        Assert.assertEquals(original.getResult(), copy1.getResult(), TOLERANCE);

        Variance copy2 = original.copy();
        Assert.assertFalse(copy2.isBiasCorrected());
        Assert.assertEquals(original.getN(), copy2.getN());
        Assert.assertEquals(original.getResult(), copy2.getResult(), TOLERANCE);
    }

    @Test(expected = NullArgumentException.class)
    public void testCopyNullSource() {
        Variance.copy(null, new Variance());
    }

    @Test(expected = NullArgumentException.class)
    public void testCopyNullDest() {
        Variance.copy(new Variance(), null);
    }

    @Test
    public void testEvaluateUnweighted() {
        Variance v = new Variance();
        double[] testArray = {1.0, 2.0, 4.0, 7.0, 11.0};

        double expectedSampleVar = 16.3;
        double result = v.evaluate(testArray);
        Assert.assertEquals(expectedSampleVar, result, TOLERANCE);

        Assert.assertEquals(0.0, v.evaluate(testArray, 0, 1), TOLERANCE);

        v.setBiasCorrected(false);
        double expectedPopVar = 13.04;
        Assert.assertEquals(expectedPopVar, v.evaluate(testArray), TOLERANCE);

        Assert.assertTrue(Double.isNaN(v.evaluate(new double[]{})));
    }

    @Test(expected = NullArgumentException.class)
    public void testEvaluateNullArray() {
        Variance v = new Variance();
        v.evaluate((double[]) null);
    }

    @Test
    public void testEvaluateWithPrecomputedMean() {
        Variance v = new Variance();
        double[] testArray = {1.0, 2.0, 4.0, 7.0, 11.0};
        double mean = 5.0;

        double sampleVar = v.evaluate(testArray, mean);
        Assert.assertEquals(16.3, sampleVar, TOLERANCE);

        v.setBiasCorrected(false);
        double popVar = v.evaluate(testArray, mean);
        Assert.assertEquals(13.04, popVar, TOLERANCE);

        Assert.assertEquals(0.0, v.evaluate(testArray, mean, 0, 1), TOLERANCE);
        Assert.assertTrue(Double.isNaN(v.evaluate(new double[]{}, mean, 0, 0)));
    }

    @Test
    public void testEvaluateWeighted() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 2.0, 1.0};

        double expectedWeightedVar = 0.6666666666666666;
        Assert.assertEquals(expectedWeightedVar, v.evaluate(values, weights), TOLERANCE);

        v.setBiasCorrected(false);
        double expectedWeightedPopVar = 0.5;
        Assert.assertEquals(expectedWeightedPopVar, v.evaluate(values, weights), TOLERANCE);

        Assert.assertEquals(0.0, v.evaluate(values, weights, 0, 1), TOLERANCE);
    }

    @Test
    public void testEvaluateWeightedWithPrecomputedMean() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 2.0, 1.0};
        double weightedMean = 2.0;

        double sampleVar = v.evaluate(values, weights, weightedMean);
        Assert.assertEquals(0.6666666666666666, sampleVar, TOLERANCE);

        v.setBiasCorrected(false);
        double popVar = v.evaluate(values, weights, weightedMean);
        Assert.assertEquals(0.5, popVar, TOLERANCE);

        Assert.assertEquals(0.0, v.evaluate(values, weights, weightedMean, 0, 1), TOLERANCE);
        Assert.assertTrue(Double.isNaN(v.evaluate(new double[]{}, new double[]{}, weightedMean, 0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateWeightedArrayLengthMismatch() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0, 2.0}, new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateWeightedNegativeWeight() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0, 2.0}, new double[]{1.0, -1.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateWeightedNaNWeight() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0, 2.0}, new double[]{1.0, Double.NaN});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateWeightedInfiniteWeight() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0, 2.0}, new double[]{1.0, Double.POSITIVE_INFINITY});
    }
}
