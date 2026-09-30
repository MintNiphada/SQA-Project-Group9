package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationGreaterThanOrEqualTest {

    @Test
    public void testGetSymbol() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        Assert.assertEquals(">=", op.getSymbol());
    }

    @Test
    public void testComputeValueGreaterThan() {
        Constant c1 = new Constant(Double.valueOf(5.0));
        Constant c2 = new Constant(Double.valueOf(3.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueEqual() {
        Constant c1 = new Constant(Double.valueOf(4.5));
        Constant c2 = new Constant(Double.valueOf(4.5));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueLessThan() {
        Constant c1 = new Constant(Double.valueOf(2.0));
        Constant c2 = new Constant(Double.valueOf(7.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueNegativeNumbers() {
        Constant c1 = new Constant(Double.valueOf(-2.0));
        Constant c2 = new Constant(Double.valueOf(-5.0));
        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(c1, c2);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(c2, c1);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithStrings() {
        Constant c1 = new Constant("10.5");
        Constant c2 = new Constant("10.5");
        CoreOperationGreaterThanOrEqual opEqual = new CoreOperationGreaterThanOrEqual(c1, c2);
        Assert.assertEquals(Boolean.TRUE, opEqual.computeValue((EvalContext) null));

        Constant c3 = new Constant("10.6");
        CoreOperationGreaterThanOrEqual opGreater = new CoreOperationGreaterThanOrEqual(c3, c1);
        Assert.assertEquals(Boolean.TRUE, opGreater.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual opLess = new CoreOperationGreaterThanOrEqual(c1, c3);
        Assert.assertEquals(Boolean.FALSE, opLess.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithNaN() {
        Constant c1 = new Constant(Double.valueOf(Double.NaN));
        Constant c2 = new Constant(Double.valueOf(1.0));
        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(c1, c2);
        Assert.assertEquals(Boolean.FALSE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(c2, c1);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(c1, c1);
        Assert.assertEquals(Boolean.FALSE, op3.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithInfinity() {
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant finite = new Constant(Double.valueOf(100.0));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(posInf, finite);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(negInf, finite);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(posInf, posInf);
        Assert.assertEquals(Boolean.TRUE, op3.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op4 = new CoreOperationGreaterThanOrEqual(negInf, negInf);
        Assert.assertEquals(Boolean.TRUE, op4.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithZeros() {
        Constant posZero = new Constant(Double.valueOf(0.0));
        Constant negZero = new Constant(Double.valueOf(-0.0));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(posZero, negZero);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(negZero, posZero);
        Assert.assertEquals(Boolean.TRUE, op2.computeValue((EvalContext) null));
    }

    @Test
    public void testToStringRepresentation() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        Assert.assertEquals("1 >= 2", op.toString());
    }
}
