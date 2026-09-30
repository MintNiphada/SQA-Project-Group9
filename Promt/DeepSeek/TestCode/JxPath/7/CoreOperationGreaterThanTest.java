package org.apache.commons.jxpath.ri.compiler;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;

public class CoreOperationGreaterThanTest {

    // Helper to create an Expression that returns a constant value
    private static Expression constant(final Object value) {
        return new Expression() {
            @Override
            public Object computeValue(EvalContext context) {
                return value;
            }
        };
    }

    // Helper to create an Expression that throws when computeValue is called
    private static Expression throwingExpression() {
        return new Expression() {
            @Override
            public Object computeValue(EvalContext context) {
                throw new RuntimeException("forced error");
            }
        };
    }

    @Test
    public void testGetSymbol() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(1), constant(2));
        Assert.assertEquals(">", op.getSymbol());
    }

    @Test
    public void testGreaterThanTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(5), constant(3));
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testGreaterThanFalseEqual() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(5), constant(5));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testGreaterThanFalseLess() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(3), constant(5));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testNegativeNumbers() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(-1), constant(-2));
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testZero() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(0), constant(0));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testDoublePrecision() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(3.14), constant(3.13));
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testNaNLeft() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(Double.NaN), constant(5));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testNaNRight() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(5), constant(Double.NaN));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testNaNBoth() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(Double.NaN), constant(Double.NaN));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testPositiveInfinity() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(Double.POSITIVE_INFINITY), constant(Double.MAX_VALUE));
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testNegativeInfinity() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(Double.NEGATIVE_INFINITY), constant(0));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testNullArgumentValue() {
        // InfoSetUtil.doubleValue(null) typically returns 0.0
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(null), constant(1));
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null)); // 0.0 > 1.0 is false
    }

    @Test
    public void testStringArgumentValue() {
        // InfoSetUtil.doubleValue("123") returns 123.0
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant("200"), constant("100"));
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test(expected = NullPointerException.class)
    public void testNullExpressionLeft() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(null, constant(1));
        op.computeValue(null); // should throw NullPointerException when calling computeValue on null
    }

    @Test(expected = NullPointerException.class)
    public void testNullExpressionRight() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(constant(1), null);
        op.computeValue(null);
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionThrows() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(throwingExpression(), constant(1));
        op.computeValue(null);
    }
}
