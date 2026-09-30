package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    private static class ConcreteRelationalExpression extends CoreOperationRelationalExpression {
        public ConcreteRelationalExpression(Expression[] args) {
            super(args);
        }

        @Override
        public Object computeValue(EvalContext context) {
            return null;
        }

        @Override
        public String getSymbol() {
            return "test_relational";
        }
    }

    @Test
    public void testGetPrecedence() {
        Expression[] args = new Expression[0];
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(args);
        Assert.assertEquals(3, expr.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        Expression[] args = new Expression[0];
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(args);
        Assert.assertFalse(expr.isSymmetric());
    }

    @Test
    public void testConstructorWithArguments() {
        Constant c1 = new Constant("a");
        Constant c2 = new Constant("b");
        Expression[] args = new Expression[] { c1, c2 };

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(args);

        Assert.assertNotNull(expr.getArguments());
        Assert.assertEquals(2, expr.getArguments().length);
        Assert.assertSame(c1, expr.getArguments()[0]);
        Assert.assertSame(c2, expr.getArguments()[1]);
        Assert.assertEquals(3, expr.getPrecedence());
        Assert.assertFalse(expr.isSymmetric());
        Assert.assertEquals("test_relational", expr.getSymbol());
    }

    @Test
    public void testConstructorWithNullArguments() {
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(null);
        Assert.assertNull(expr.getArguments());
        Assert.assertEquals(3, expr.getPrecedence());
        Assert.assertFalse(expr.isSymmetric());
    }
}
