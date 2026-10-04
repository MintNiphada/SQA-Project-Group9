package org.apache.commons.math3.stat.inference;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

public class MannWhitneyUTestTest {

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUXNull() {
        new MannWhitneyUTest().mannWhitneyU(null, new double[]{1.0});
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUYNull() {
        new MannWhitneyUTest().mannWhitneyU(new double[]{1.0}, null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUTestXNull() {
        new MannWhitneyUTest().mannWhitneyUTest(null, new double[]{1.0});
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUTestYNull() {
        new MannWhitneyUTest().mannWhitneyUTest(new double[]{1.0}, null);
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUXEmpty() {
        new MannWhitneyUTest().mannWhitneyU(new double[]{}, new double[]{1.0});
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUYEmpty() {
        new MannWhitneyUTest().mannWhitneyU(new double[]{1.0}, new double[]{});
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUTestXEmpty() {
        new MannWhitneyUTest().mannWhitneyUTest(new double[]{}, new double[]{1.0});
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUTestYEmpty() {
        new MannWhitneyUTest().mannWhitneyUTest(new double[]{1.0}, new double[]{});
    }

    @Test
    public void testMannWhitneyUSimple() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {4.0, 5.0, 6.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertEquals(9.0, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUTies() {
        double[] x = {1.0, 2.0, 2.0};
        double[] y = {3.0, 4.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertEquals(6.0, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUIdenticalSamples() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertEquals(4.5, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUWithNaN() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {4.0, 5.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertTrue(Double.isNaN(u));
    }

    @Test
    public void testMannWhitneyUWithNaNAndRemovedStrategy() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {4.0, 5.0};
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.REMOVED, TiesStrategy.AVERAGE);
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(4.0, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUSingleElement() {
        double[] x = {5.0};
        double[] y = {10.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertEquals(1.0, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUTestSimple() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {4.0, 5.0, 6.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertEquals(0.0495, p, 0.001);
    }

    @Test
    public void testMannWhitneyUTestTies() {
        double[] x = {1.0, 2.0, 2.0};
        double[] y = {3.0, 4.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    public void testMannWhitneyUTestIdenticalSamples() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertEquals(1.0, p, 1e-15);
    }

    @Test
    public void testMannWhitneyUTestWithNaN() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {4.0, 5.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertTrue(Double.isNaN(p));
    }

    @Test
    public void testMannWhitneyUTestWithNaNAndRemovedStrategy() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {4.0, 5.0};
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.REMOVED, TiesStrategy.AVERAGE);
        double p = test.mannWhitneyUTest(x, y);
        Assert.assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    public void testMannWhitneyUTestSingleElement() {
        double[] x = {5.0};
        double[] y = {10.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    public void testConstructorDefault() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = {1.0, Double.NaN};
        double[] y = {2.0};
        double u = test.mannWhitneyU(x, y);
        Assert.assertTrue(Double.isNaN(u));
    }

    @Test
    public void testConstructorWithStrategies() {
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.MAXIMAL, TiesStrategy.SEQUENTIAL);
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {4.0, 5.0};
        double u = test.mannWhitneyU(x, y);
        Assert.assertFalse(Double.isNaN(u));
    }

    @Test
    public void testMannWhitneyULargeValues() {
        double[] x = {1000.0, 2000.0, 3000.0};
        double[] y = {4000.0, 5000.0, 6000.0};
        double u = new MannWhitneyUTest().mannWhitneyU(x, y);
        Assert.assertEquals(9.0, u, 1e-15);
    }

    @Test
    public void testMannWhitneyUTestLargeValues() {
        double[] x = {1000.0, 2000.0, 3000.0};
        double[] y = {4000.0, 5000.0, 6000.0};
        double p = new MannWhitneyUTest().mannWhitneyUTest(x, y);
        Assert.assertEquals(0.0495, p, 0.001);
    }
}
