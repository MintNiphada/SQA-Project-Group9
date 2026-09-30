package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationLessThanTest {

    @Test
    public void testGetSymbol() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);
        Assert.assertEquals("<", operation.getSymbol());
    }

    @Test
    public void testComputeValueTrueWhenLeftIsLessThanRight() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueFalseWhenLeftIsGreaterThanRight() {
        Constant c1 = new Constant(Double.valueOf(5.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueFalseWhenLeftEqualsRight() {
        Constant c1 = new Constant(Double.valueOf(3.14));
        Constant c2 = new Constant(Double.valueOf(3.14));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueWithNegativeNumbers() {
        Constant c1 = new Constant(Double.valueOf(-10.0));
        Constant c2 = new Constant(Double.valueOf(-5.0));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);

        CoreOperationLessThan reversed = new CoreOperationLessThan(c2, c1);
        Assert.assertEquals(Boolean.FALSE, reversed.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithStrings() {
        Constant c1 = new Constant("10.5");
        Constant c2 = new Constant("20.1");
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);

        Constant c3 = new Constant("100");
        Constant c4 = new Constant("20");
        CoreOperationLessThan op2 = new CoreOperationLessThan(c3, c4);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithInfinityAndNaN() {
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant zero = new Constant(Double.valueOf(0.0));
        Constant nan = new Constant(Double.valueOf(Double.NaN));

        CoreOperationLessThan op1 = new CoreOperationLessThan(negInf, posInf);
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(posInf, negInf);
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationLessThan op3 = new CoreOperationLessThan(zero, nan);
        Assert.assertEquals(Boolean.FALSE, op3.computeValue((EvalContext) null));

        CoreOperationLessThan op4 = new CoreOperationLessThan(nan, zero);
        Assert.assertEquals(Boolean.FALSE, op4.computeValue((EvalContext) null));

        CoreOperationLessThan op5 = new CoreOperationLessThan(nan, nan);
        Assert.assertEquals(Boolean.FALSE, op5.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithIntegerConstants() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThan operation = new CoreOperationLessThan(c1, c2);

        Object result = operation.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testNestedExpressions() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));
        Constant c3 = new Constant(Double.valueOf(3.0));

        CoreOperationAdd add = new CoreOperationAdd(new Expression[] { c1, c2 }); // 1 + 2 = 3
        CoreOperationLessThan op1 = new CoreOperationLessThan(add, c3); // 3 < 3 -> false
        Assert.assertEquals(Boolean.FALSE, op1.computeValue((EvalContext) null));

        Constant c4 = new Constant(Double.valueOf(4.0));
        CoreOperationLessThan op2 = new CoreOperationLessThan(add, c4); // 3 < 4 -> true
        Assert.assertEquals(Boolean.TRUE, op2.computeValue((EvalContext) null));
    }
}
