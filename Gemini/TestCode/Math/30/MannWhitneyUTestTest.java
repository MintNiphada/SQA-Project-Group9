package org.apache.commons.math3.stat.inference;

import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.junit.Assert;
import org.junit.Test;

public class MannWhitneyUTestTest {

    @Test
    public void testDefaultConstructor() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] y = new double[] { 4.0, 5.0, 6.0 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(9.0, u, 1e-6);
    }

    @Test
    public void testCustomConstructor() {
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.REMOVED, TiesStrategy.MAXIMUM);
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] y = new double[] { 1.0, 2.0, 3.0 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(4.5, u, 1e-6);
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUNullX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(null, new double[] { 1.0 });
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUNullY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[] { 1.0 }, null);
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUEmptyX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[] {}, new double[] { 1.0 });
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUEmptyY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[] { 1.0 }, new double[] {});
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUTestNullX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(null, new double[] { 1.0 });
    }

    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUTestNullY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[] { 1.0 }, null);
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUTestEmptyX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[] {}, new double[] { 1.0 });
    }

    @Test(expected = NoDataException.class)
    public void testMannWhitneyUTestEmptyY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[] { 1.0 }, new double[] {});
    }

    @Test
    public void testMannWhitneyUSimple() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 19, 22, 16, 29, 24 };
        double[] y = new double[] { 20, 11, 17, 12 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(17.0, u, 1e-6);
    }

    @Test
    public void testMannWhitneyUInverted() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 20, 11, 17, 12 };
        double[] y = new double[] { 19, 22, 16, 29, 24 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(17.0, u, 1e-6);
    }

    @Test
    public void testMannWhitneyUTestPValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 19, 22, 16, 29, 24 };
        double[] y = new double[] { 20, 11, 17, 12 };
        double p = test.mannWhitneyUTest(x, y);
        Assert.assertEquals(0.11125, p, 1e-4);
    }

    @Test
    public void testMannWhitneyUTestBigSamples() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        double[] y = new double[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(100.0, u, 1e-6);
        double p = test.mannWhitneyUTest(x, y);
        Assert.assertTrue(p < 0.001);
    }

    @Test
    public void testMannWhitneyUTies() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] y = new double[] { 2.0, 3.0, 4.0, 5.0 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(11.0, u, 1e-6);
        double p = test.mannWhitneyUTest(x, y);
        Assert.assertTrue(p > 0.0 && p <= 1.0);
    }

    @Test
    public void testMannWhitneyUSameValues() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[] { 1.0, 1.0, 1.0 };
        double[] y = new double[] { 1.0, 1.0, 1.0 };
        double u = test.mannWhitneyU(x, y);
        Assert.assertEquals(4.5, u, 1e-6);
        double p = test.mannWhitneyUTest(x, y);
        Assert.assertEquals(1.0, p, 1e-4);
    }
}
