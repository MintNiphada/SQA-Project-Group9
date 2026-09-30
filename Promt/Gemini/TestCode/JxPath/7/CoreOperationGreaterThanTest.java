package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationGreaterThanTest {

    @Test
    public void testGetSymbol() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant(1),
            new Constant(2)
        );
        Assert.assertEquals(">", op.getSymbol());
    }

    @Test
    public void testComputeValueGreaterThan() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant(10),
            new Constant(5)
        );
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueLessThan() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant(5),
            new Constant(10)
        );
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueEqual() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant(7.5),
            new Constant(7.5)
        );
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueWithStringNumbers() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant("100"),
            new Constant("20")
        );
        Object result = op.computeValue((EvalContext) null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueWithNaN() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new Constant("invalid_number"),
            new Constant(0)
        );
        Assert.assertEquals(Boolean.FALSE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new Constant(0),
            new Constant("invalid_number")
        );
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationGreaterThan op3 = new CoreOperationGreaterThan(
            new Constant("invalid_a"),
            new Constant("invalid_b")
        );
        Assert.assertEquals(Boolean.FALSE, op3.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithInfinity() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new Constant(Double.POSITIVE_INFINITY),
            new Constant(Double.MAX_VALUE)
        );
        Assert.assertEquals(Boolean.TRUE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new Constant(Double.NEGATIVE_INFINITY),
            new Constant(-Double.MAX_VALUE)
        );
        Assert.assertEquals(Boolean.FALSE, op2.computeValue((EvalContext) null));

        CoreOperationGreaterThan op3 = new CoreOperationGreaterThan(
            new Constant(Double.POSITIVE_INFINITY),
            new Constant(Double.POSITIVE_INFINITY)
        );
        Assert.assertEquals(Boolean.FALSE, op3.computeValue((EvalContext) null));
    }

    @Test
    public void testComputeValueWithZeroes() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new Constant(0.0),
            new Constant(-0.0)
        );
        Assert.assertEquals(Boolean.FALSE, op1.computeValue((EvalContext) null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new Constant(0.0000001),
            new Constant(0.0)
        );
        Assert.assertEquals(Boolean.TRUE, op2.computeValue((EvalContext) null));
    }

    @Test(expected = NullPointerException.class)
    public void testComputeValueWithNullExpression() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            null,
            new Constant(1)
        );
        op.computeValue((EvalContext) null);
    }
}
