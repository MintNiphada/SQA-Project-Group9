package org.apache.commons.math.special;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Assert;
import org.junit.Test;

public class GammaTest {

    private static final double EPSILON = 1e-8;

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<Gamma> constructor = Gamma.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Gamma instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testLogGamma() {
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(Double.NaN)));
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(0.0)));
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(-1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(-100.5)));

        Assert.assertEquals(0.0, Gamma.logGamma(1.0), EPSILON);
        Assert.assertEquals(0.0, Gamma.logGamma(2.0), EPSILON);
        Assert.assertEquals(Math.log(2.0), Gamma.logGamma(3.0), EPSILON);
        Assert.assertEquals(Math.log(6.0), Gamma.logGamma(4.0), EPSILON);
        Assert.assertEquals(Math.log(24.0), Gamma.logGamma(5.0), EPSILON);
        Assert.assertEquals(Math.log(Math.sqrt(Math.PI)), Gamma.logGamma(0.5), EPSILON);
    }

    @Test
    public void testRegularizedGammaP() throws MathException {
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NaN)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(0.0, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(-1.0, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, -1.0)));

        Assert.assertEquals(0.0, Gamma.regularizedGammaP(1.0, 0.0), EPSILON);
        Assert.assertEquals(0.0, Gamma.regularizedGammaP(0.5, 0.0), EPSILON);

        Assert.assertEquals(0.6321205588285577, Gamma.regularizedGammaP(1.0, 1.0), EPSILON);
        Assert.assertEquals(0.8646647167633873, Gamma.regularizedGammaP(1.0, 2.0), EPSILON);
        Assert.assertEquals(0.3934693402873666, Gamma.regularizedGammaP(2.0, 1.0), EPSILON);
        Assert.assertEquals(0.6826894921370859, Gamma.regularizedGammaP(0.5, 0.5), EPSILON);
    }

    @Test
    public void testRegularizedGammaQ() throws MathException {
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NaN)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(0.0, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(-1.0, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, -1.0)));

        Assert.assertEquals(1.0, Gamma.regularizedGammaQ(1.0, 0.0), EPSILON);
        Assert.assertEquals(1.0, Gamma.regularizedGammaQ(0.5, 0.0), EPSILON);

        Assert.assertEquals(0.36787944117144233, Gamma.regularizedGammaQ(1.0, 1.0), EPSILON);
        Assert.assertEquals(0.1353352832366127, Gamma.regularizedGammaQ(1.0, 2.0), EPSILON);
        Assert.assertEquals(0.6065306597126334, Gamma.regularizedGammaQ(2.0, 1.0), EPSILON);
        Assert.assertEquals(0.31731050786291415, Gamma.regularizedGammaQ(0.5, 0.5), EPSILON);
    }

    @Test
    public void testRegularizedGammaPAndQSumToOne() throws MathException {
        double[] aValues = {0.5, 1.0, 1.5, 2.0, 5.0};
        double[] xValues = {0.2, 0.5, 1.0, 2.0, 5.0, 10.0};
        for (double a : aValues) {
            for (double x : xValues) {
                double p = Gamma.regularizedGammaP(a, x);
                double q = Gamma.regularizedGammaQ(a, x);
                Assert.assertEquals(1.0, p + q, EPSILON);
            }
        }
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaPMaxIterations() throws MathException {
        Gamma.regularizedGammaP(2.0, 1.0, 1e-15, 1);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaQMaxIterations() throws MathException {
        Gamma.regularizedGammaQ(2.0, 5.0, 1e-15, 1);
    }
}
