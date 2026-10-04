package org.apache.commons.math.stat.descriptive;

import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class SummaryStatisticsTest {

    private static class DummyStorelessStatistic extends AbstractStorelessUnivariateStatistic {
        private double val = 0.0;
        private long count = 0;

        @Override
        public void increment(double d) {
            val += d;
            count++;
        }

        @Override
        public double getResult() {
            return val;
        }

        @Override
        public long getN() {
            return count;
        }

        @Override
        public void clear() {
            val = 0.0;
            count = 0;
        }

        @Override
        public DummyStorelessStatistic copy() {
            DummyStorelessStatistic copy = new DummyStorelessStatistic();
            copy.val = this.val;
            copy.count = this.count;
            return copy;
        }
    }

    @Test
    public void testEmptySummaryStatistics() {
        SummaryStatistics stats = new SummaryStatistics();
        Assert.assertEquals(0L, stats.getN());
        Assert.assertTrue(Double.isNaN(stats.getSum()));
        Assert.assertTrue(Double.isNaN(stats.getSumsq()));
        Assert.assertTrue(Double.isNaN(stats.getMean()));
        Assert.assertTrue(Double.isNaN(stats.getStandardDeviation()));
        Assert.assertTrue(Double.isNaN(stats.getVariance()));
        Assert.assertTrue(Double.isNaN(stats.getPopulationVariance()));
        Assert.assertTrue(Double.isNaN(stats.getMax()));
        Assert.assertTrue(Double.isNaN(stats.getMin()));
        Assert.assertTrue(Double.isNaN(stats.getGeometricMean()));
        Assert.assertTrue(Double.isNaN(stats.getSumOfLogs()));
        Assert.assertTrue(Double.isNaN(stats.getSecondMoment()));
    }

    @Test
    public void testSingleValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(5.0);
        Assert.assertEquals(1L, stats.getN());
        Assert.assertEquals(5.0, stats.getSum(), 1e-10);
        Assert.assertEquals(25.0, stats.getSumsq(), 1e-10);
        Assert.assertEquals(5.0, stats.getMean(), 1e-10);
        Assert.assertEquals(0.0, stats.getStandardDeviation(), 1e-10);
        Assert.assertEquals(0.0, stats.getVariance(), 1e-10);
        Assert.assertEquals(0.0, stats.getPopulationVariance(), 1e-10);
        Assert.assertEquals(5.0, stats.getMax(), 1e-10);
        Assert.assertEquals(5.0, stats.getMin(), 1e-10);
        Assert.assertEquals(5.0, stats.getGeometricMean(), 1e-10);
        Assert.assertEquals(FastMath.log(5.0), stats.getSumOfLogs(), 1e-10);
        Assert.assertEquals(0.0, stats.getSecondMoment(), 1e-10);

        StatisticalSummary summary = stats.getSummary();
        Assert.assertEquals(stats.getMean(), summary.getMean(), 1e-10);
        Assert.assertEquals(stats.getVariance(), summary.getVariance(), 1e-10);
        Assert.assertEquals(stats.getN(), summary.getN());
        Assert.assertEquals(stats.getMax(), summary.getMax(), 1e-10);
        Assert.assertEquals(stats.getMin(), summary.getMin(), 1e-10);
        Assert.assertEquals(stats.getSum(), summary.getSum(), 1e-10);
    }

    @Test
    public void testMultipleValues() {
        SummaryStatistics stats = new SummaryStatistics();
        double[] values = {1.0, 2.0, 4.0, 8.0};
        for (double v : values) {
            stats.addValue(v);
        }
        Assert.assertEquals(4L, stats.getN());
        Assert.assertEquals(15.0, stats.getSum(), 1e-10);
        Assert.assertEquals(85.0, stats.getSumsq(), 1e-10);
        Assert.assertEquals(3.75, stats.getMean(), 1e-10);
        Assert.assertEquals(3.1, stats.getStandardDeviation(), 1e-2);
        Assert.assertEquals(9.583333333333334, stats.getVariance(), 1e-10);
        Assert.assertEquals(7.1875, stats.getPopulationVariance(), 1e-10);
        Assert.assertEquals(8.0, stats.getMax(), 1e-10);
        Assert.assertEquals(1.0, stats.getMin(), 1e-10);
        Assert.assertEquals(2.8284271247461903, stats.getGeometricMean(), 1e-10);
        Assert.assertEquals(FastMath.log(1.0 * 2.0 * 4.0 * 8.0), stats.getSumOfLogs(), 1e-10);
        Assert.assertEquals(28.75, stats.getSecondMoment(), 1e-10);
    }

    @Test
    public void testClear() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(10.0);
        stats.addValue(20.0);
        stats.clear();
        Assert.assertEquals(0L, stats.getN());
        Assert.assertTrue(Double.isNaN(stats.getMean()));
        Assert.assertTrue(Double.isNaN(stats.getSum()));

        stats.setMeanImpl(new DummyStorelessStatistic());
        stats.setVarianceImpl(new DummyStorelessStatistic());
        stats.addValue(5.0);
        stats.clear();
        Assert.assertEquals(0L, stats.getN());
        Assert.assertEquals(0.0, stats.getMean(), 1e-10);
        Assert.assertEquals(0.0, stats.getVariance(), 1e-10);
    }

    @Test
    public void testToString() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.addValue(2.0);
        String str = stats.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("SummaryStatistics:"));
        Assert.assertTrue(str.contains("n: 2"));
    }

    @Test
    public void testEqualsAndHashCode() {
        SummaryStatistics s1 = new SummaryStatistics();
        SummaryStatistics s2 = new SummaryStatistics();
        Assert.assertTrue(s1.equals(s1));
        Assert.assertFalse(s1.equals(null));
        Assert.assertFalse(s1.equals("Not a SummaryStatistics"));
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.addValue(10.0);
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s1.hashCode() == s2.hashCode());

        s2.addValue(10.0);
        Assert.assertTrue(s1.equals(s2));
        Assert.assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testCustomImplementations() {
        SummaryStatistics stats = new SummaryStatistics();

        StorelessUnivariateStatistic dummySum = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummySumsq = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummyMin = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummyMax = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummySumLog = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummyGeoMean = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummyMean = new DummyStorelessStatistic();
        StorelessUnivariateStatistic dummyVariance = new DummyStorelessStatistic();

        stats.setSumImpl(dummySum);
        stats.setSumsqImpl(dummySumsq);
        stats.setMinImpl(dummyMin);
        stats.setMaxImpl(dummyMax);
        stats.setSumLogImpl(dummySumLog);
        stats.setGeoMeanImpl(dummyGeoMean);
        stats.setMeanImpl(dummyMean);
        stats.setVarianceImpl(dummyVariance);

        Assert.assertSame(dummySum, stats.getSumImpl());
        Assert.assertSame(dummySumsq, stats.getSumsqImpl());
        Assert.assertSame(dummyMin, stats.getMinImpl());
        Assert.assertSame(dummyMax, stats.getMaxImpl());
        Assert.assertSame(dummySumLog, stats.getSumLogImpl());
        Assert.assertSame(dummyGeoMean, stats.getGeoMeanImpl());
        Assert.assertSame(dummyMean, stats.getMeanImpl());
        Assert.assertSame(dummyVariance, stats.getVarianceImpl());

        stats.addValue(2.0);
        stats.addValue(3.0);

        Assert.assertEquals(5.0, stats.getSum(), 1e-10);
        Assert.assertEquals(5.0, stats.getSumsq(), 1e-10);
        Assert.assertEquals(5.0, stats.getMin(), 1e-10);
        Assert.assertEquals(5.0, stats.getMax(), 1e-10);
        Assert.assertEquals(5.0, stats.getSumOfLogs(), 1e-10);
        Assert.assertEquals(5.0, stats.getGeoMeanImpl().getResult(), 1e-10);
        Assert.assertEquals(5.0, stats.getMean(), 1e-10);
        Assert.assertEquals(5.0, stats.getVariance(), 1e-10);
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetSumImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setSumImpl(new Sum());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetSumsqImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setSumsqImpl(new SumOfSquares());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetMinImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMinImpl(new Min());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetMaxImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMaxImpl(new Max());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetSumLogImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setSumLogImpl(new SumOfLogs());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetGeoMeanImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setGeoMeanImpl(new GeometricMean());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetMeanImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMeanImpl(new Mean());
    }

    @Test(expected = MathIllegalStateException.class)
    public void testSetVarianceImplAfterAddValue() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setVarianceImpl(new Variance());
    }

    @Test
    public void testCopyConstructorAndCopyMethod() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(2.0);
        stats.addValue(4.0);

        SummaryStatistics copy = new SummaryStatistics(stats);
        Assert.assertEquals(stats, copy);
        Assert.assertEquals(stats.getSum(), copy.getSum(), 1e-10);

        SummaryStatistics copy2 = stats.copy();
        Assert.assertEquals(stats, copy2);
    }

    @Test
    public void testCopyWithCustomImplementations() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.setSumImpl(new DummyStorelessStatistic());
        stats.setSumsqImpl(new DummyStorelessStatistic());
        stats.setMinImpl(new DummyStorelessStatistic());
        stats.setMaxImpl(new DummyStorelessStatistic());
        stats.setSumLogImpl(new DummyStorelessStatistic());
        stats.setGeoMeanImpl(new DummyStorelessStatistic());
        stats.setMeanImpl(new DummyStorelessStatistic());
        stats.setVarianceImpl(new DummyStorelessStatistic());
        stats.addValue(5.0);

        SummaryStatistics copy = stats.copy();
        Assert.assertTrue(copy.getSumImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getSumsqImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getMinImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getMaxImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getSumLogImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getGeoMeanImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getMeanImpl() instanceof DummyStorelessStatistic);
        Assert.assertTrue(copy.getVarianceImpl() instanceof DummyStorelessStatistic);
        Assert.assertEquals(stats, copy);
    }

    @Test(expected = NullArgumentException.class)
    public void testCopyNullSource() {
        SummaryStatistics.copy(null, new SummaryStatistics());
    }

    @Test(expected = NullArgumentException.class)
    public void testCopyNullDest() {
        SummaryStatistics.copy(new SummaryStatistics(), null);
    }
}
