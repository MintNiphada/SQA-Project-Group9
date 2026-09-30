package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int targetCompare;

        public TestRelationalExpression(Expression[] args) {
            super(args);
            this.targetCompare = Integer.MIN_VALUE;
        }

        public TestRelationalExpression(Expression[] args, int targetCompare) {
            super(args);
            this.targetCompare = targetCompare;
        }

        public String getSymbol() {
            return "test_op";
        }

        protected boolean evaluateCompare(int compare) {
            if (targetCompare == Integer.MIN_VALUE) {
                return compare <= 0;
            }
            return compare == targetCompare;
        }
    }

    private static class ValueExpr extends Expression {
        private final Object val;

        public ValueExpr(Object val) {
            this.val = val;
        }

        public Object computeValue(EvalContext context) {
            return val;
        }

        public Object compute(EvalContext context) {
            return val;
        }

        public boolean isContextDependent() {
            return false;
        }

        public boolean computeContextDependent() {
            return false;
        }
    }

    @Test
    public void testGetPrecedence() {
        CoreOperationRelationalExpression expr = new TestRelationalExpression(new Expression[0]);
        Assert.assertEquals(3, expr.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        CoreOperationRelationalExpression expr = new TestRelationalExpression(new Expression[0]);
        Assert.assertFalse(expr.isSymmetric());
    }

    @Test
    public void testComputeValueEqual() {
        Expression[] args = new Expression[] {
            new Constant(Double.valueOf(5.0)),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Object result = expr.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);

        CoreOperationRelationalExpression exprFalse = new TestRelationalExpression(args, 1);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeValueLessThan() {
        Expression[] args = new Expression[] {
            new Constant(Double.valueOf(3.0)),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        CoreOperationRelationalExpression exprFalse = new TestRelationalExpression(args, 1);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeValueGreaterThan() {
        Expression[] args = new Expression[] {
            new Constant(Double.valueOf(7.0)),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        CoreOperationRelationalExpression exprFalse = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeValueWithNaNLeft() {
        Expression[] args = new Expression[] {
            new Constant("not-a-number"),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testComputeValueWithNaNRight() {
        Expression[] args = new Expression[] {
            new Constant(Double.valueOf(5.0)),
            new Constant("invalid-number")
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testComputeValueWithBothNaN() {
        Expression[] args = new Expression[] {
            new Constant("abc"),
            new Constant("xyz")
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testComputeValueLeftCollectionRightScalar() {
        List<Double> leftList = Arrays.asList(Double.valueOf(10.0), Double.valueOf(3.0), Double.valueOf(8.0));
        Expression[] args = new Expression[] {
            new ValueExpr(leftList),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        CoreOperationRelationalExpression exprNoMatch = new TestRelationalExpression(
            new Expression[] { new ValueExpr(Arrays.asList(10.0, 20.0)), new Constant(Double.valueOf(5.0)) },
            -1
        );
        Assert.assertEquals(Boolean.FALSE, exprNoMatch.computeValue(null));
    }

    @Test
    public void testComputeValueLeftScalarRightCollection() {
        List<Double> rightList = Arrays.asList(Double.valueOf(2.0), Double.valueOf(8.0));
        Expression[] args = new Expression[] {
            new Constant(Double.valueOf(5.0)),
            new ValueExpr(rightList)
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        List<Double> rightListNoMatch = Arrays.asList(Double.valueOf(5.0), Double.valueOf(5.0));
        CoreOperationRelationalExpression exprNoMatch = new TestRelationalExpression(
            new Expression[] { new Constant(Double.valueOf(5.0)), new ValueExpr(rightListNoMatch) },
            1
        );
        Assert.assertEquals(Boolean.FALSE, exprNoMatch.computeValue(null));
    }

    @Test
    public void testComputeValueBothCollectionsMatch() {
        List<Double> leftList = Arrays.asList(Double.valueOf(1.0), Double.valueOf(10.0));
        List<Double> rightList = Arrays.asList(Double.valueOf(5.0), Double.valueOf(15.0));
        Expression[] args = new Expression[] {
            new ValueExpr(leftList),
            new ValueExpr(rightList)
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testComputeValueBothCollectionsNoMatch() {
        List<Double> leftList = Arrays.asList(Double.valueOf(20.0), Double.valueOf(30.0));
        List<Double> rightList = Arrays.asList(Double.valueOf(5.0), Double.valueOf(10.0));
        Expression[] args = new Expression[] {
            new ValueExpr(leftList),
            new ValueExpr(rightList)
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testEmptyCollectionHandling() {
        List<Double> emptyList = Collections.emptyList();
        Expression[] args1 = new Expression[] {
            new ValueExpr(emptyList),
            new Constant(Double.valueOf(5.0))
        };
        CoreOperationRelationalExpression expr1 = new TestRelationalExpression(args1, 0);
        Assert.assertEquals(Boolean.FALSE, expr1.computeValue(null));

        Expression[] args2 = new Expression[] {
            new Constant(Double.valueOf(5.0)),
            new ValueExpr(emptyList)
        };
        CoreOperationRelationalExpression expr2 = new TestRelationalExpression(args2, 0);
        Assert.assertEquals(Boolean.FALSE, expr2.computeValue(null));

        Expression[] args3 = new Expression[] {
            new ValueExpr(emptyList),
            new ValueExpr(emptyList)
        };
        CoreOperationRelationalExpression expr3 = new TestRelationalExpression(args3, 0);
        Assert.assertEquals(Boolean.FALSE, expr3.computeValue(null));
    }

    @Test
    public void testIteratorHandlingDirectly() {
        Iterator<Double> leftIt = Arrays.asList(Double.valueOf(3.0)).iterator();
        Iterator<Double> rightIt = Arrays.asList(Double.valueOf(5.0)).iterator();

        Expression[] args = new Expression[] {
            new ValueExpr(leftIt),
            new ValueExpr(rightIt)
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, -1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testInitialContextHandling() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(null, "5.0", Locale.getDefault());
        RootContext rootContext = new RootContext(parentContext, rootPointer);
        InitialContext initialContextLeft = new InitialContext(rootContext);
        InitialContext initialContextRight = new InitialContext(rootContext);

        Expression[] args = new Expression[] {
            new ValueExpr(initialContextLeft),
            new ValueExpr(initialContextRight)
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testSelfContextReduction() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(null, Double.valueOf(4.0), Locale.getDefault());
        RootContext rootContext = new RootContext(parentContext, rootPointer);
        InitialContext initialContext = new InitialContext(rootContext);
        SelfContext selfContext = new SelfContext(initialContext, new NodeTypeTest(1));

        Expression[] args = new Expression[] {
            new ValueExpr(selfContext),
            new Constant(Double.valueOf(4.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testNullHandling() {
        Expression[] args = new Expression[] {
            new ValueExpr(null),
            new Constant(Double.valueOf(0.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testNestedIterators() {
        List<List<Double>> nestedList = new ArrayList<List<Double>>();
        nestedList.add(Arrays.asList(Double.valueOf(1.0), Double.valueOf(2.0)));
        nestedList.add(Arrays.asList(Double.valueOf(3.0), Double.valueOf(4.0)));

        Expression[] args = new Expression[] {
            new ValueExpr(nestedList),
            new Constant(Double.valueOf(3.0))
        };
        CoreOperationRelationalExpression expr = new TestRelationalExpression(args, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }
}
