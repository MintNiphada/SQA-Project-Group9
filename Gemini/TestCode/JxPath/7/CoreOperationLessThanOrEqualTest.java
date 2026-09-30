package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationLessThanOrEqualTest {

    @Test
    public void testGetSymbol() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals("<=", op.getSymbol());
    }

    @Test
    public void testComputeValueLessThan() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueEqual() {
        Constant arg1 = new Constant(Double.valueOf(5.0));
        Constant arg2 = new Constant(Double.valueOf(5.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueGreaterThan() {
        Constant arg1 = new Constant(Double.valueOf(10.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueWithNegativeNumbers() {
        Constant arg1 = new Constant(Double.valueOf(-10.5));
        Constant arg2 = new Constant(Double.valueOf(-2.5));
        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(arg2, arg1);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithZeroes() {
        Constant arg1 = new Constant(Double.valueOf(-0.0));
        Constant arg2 = new Constant(Double.valueOf(0.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals(Boolean.TRUE, op.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual opRev = new CoreOperationLessThanOrEqual(arg2, arg1);
        Assert.assertEquals(Boolean.TRUE, opRev.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithNaN() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        Constant num = new Constant(Double.valueOf(5.0));

        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(nan, num);
        Assert.assertEquals(Boolean.FALSE, op1.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(num, nan);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op3 = new CoreOperationLessThanOrEqual(nan, nan);
        Assert.assertEquals(Boolean.FALSE, op3.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithInfinities() {
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant finite = new Constant(Double.valueOf(100.0));

        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(negInf, posInf);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(posInf, negInf);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op3 = new CoreOperationLessThanOrEqual(posInf, posInf);
        Assert.assertEquals(Boolean.TRUE, op3.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op4 = new CoreOperationLessThanOrEqual(negInf, negInf);
        Assert.assertEquals(Boolean.TRUE, op4.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op5 = new CoreOperationLessThanOrEqual(finite, posInf);
        Assert.assertEquals(Boolean.TRUE, op5.computeValue((EvalContext) null));

        CoreOperationLessThanOrEqual op6 = new CoreOperationLessThanOrEqual(finite, negInf);
        Assert.assertEquals(Boolean.FALSE, op6.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithStrings() {
        Constant arg1 = new Constant("12.5");
        Constant arg2 = new Constant("15.0");
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals(Boolean.TRUE, op.computeValue((EvalContext) null));

        Constant arg3 = new Constant("25.0");
        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(arg3, arg1);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithNonNumericString() {
        Constant arg1 = new Constant("not a number");
        Constant arg2 = new Constant(Double.valueOf(10.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals(Boolean.FALSE, op.computeValue((EvalContext) null));
    }

    @Test
    public void testToStringRepresentation() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(arg1, arg2);
        Assert.assertEquals("1 <= 2", op.toString());
    }
}
