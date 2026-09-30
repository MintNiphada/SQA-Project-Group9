package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int expectedComparison;

        public TestRelationalExpression(Expression[] args, int expectedComparison) {
            super(args);
            this.expectedComparison = expectedComparison;
        }

        public TestRelationalExpression(Expression arg1, Expression arg2, int expectedComparison) {
            super(new Expression[] { arg1, arg2 });
            this.expectedComparison = expectedComparison;
        }

        public TestRelationalExpression(Expression arg1, Expression arg2) {
            this(arg1, arg2, 0);
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == expectedComparison;
        }

        @Override
        public String getSymbol() {
            return "test_rel";
        }
    }

    private static class MockConstantExpression extends Expression {
        private final Object value;

        public MockConstantExpression(Object value) {
            this.value = value;
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        public boolean isContextDependent() {
            return false;
        }
    }

    @Test
    public void testGetPrecedence() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(1), new Constant(2));
        Assert.assertEquals(3, expr.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(1), new Constant(2));
        Assert.assertFalse(expr.isSymmetric());
    }

    @Test
    public void testComputeValueEqual() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(5), new Constant(5), 0);
        Object result = expr.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);

        TestRelationalExpression exprFalse = new TestRelationalExpression(
                new Constant(5), new Constant(6), 0);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeValueLessThan() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(3), new Constant(7), -1);
        Object result = expr.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);

        TestRelationalExpression exprFalse = new TestRelationalExpression(
                new Constant(7), new Constant(3), -1);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeValueGreaterThan() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(10), new Constant(2), 1);
        Object result = expr.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);

        TestRelationalExpression exprFalse = new TestRelationalExpression(
                new Constant(2), new Constant(10), 1);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeWithStringNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant("10"), new Constant("20"), -1);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        TestRelationalExpression expr2 = new TestRelationalExpression(
                new Constant("20"), new Constant("10"), 1);
        Assert.assertEquals(Boolean.TRUE, expr2.computeValue(null));
    }

    @Test
    public void testComputeWithLeftCollection() {
        List<Double> list = Arrays.asList(10.0, 20.0, 30.0);
        MockConstantExpression left = new MockConstantExpression(list);
        MockConstantExpression right = new MockConstantExpression(20.0);

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        TestRelationalExpression exprFalse = new TestRelationalExpression(left, new MockConstantExpression(50.0), 0);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeWithRightCollection() {
        List<Double> list = Arrays.asList(5.0, 15.0, 25.0);
        MockConstantExpression left = new MockConstantExpression(15.0);
        MockConstantExpression right = new MockConstantExpression(list);

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        TestRelationalExpression exprFalse = new TestRelationalExpression(new MockConstantExpression(100.0), right, 0);
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testComputeWithBothCollectionsMatch() {
        List<Double> listLeft = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> listRight = Arrays.asList(3.0, 4.0, 5.0);

        MockConstantExpression left = new MockConstantExpression(listLeft);
        MockConstantExpression right = new MockConstantExpression(listRight);

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testComputeWithBothCollectionsNoMatch() {
        List<Double> listLeft = Arrays.asList(1.0, 2.0);
        List<Double> listRight = Arrays.asList(3.0, 4.0);

        MockConstantExpression left = new MockConstantExpression(listLeft);
        MockConstantExpression right = new MockConstantExpression(listRight);

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Assert.assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test
    public void testComputeWithEmptyCollections() {
        List<Double> emptyList = Collections.emptyList();
        MockConstantExpression emptyExpr = new MockConstantExpression(emptyList);
        MockConstantExpression numExpr = new MockConstantExpression(10.0);

        TestRelationalExpression expr1 = new TestRelationalExpression(emptyExpr, numExpr, 0);
        Assert.assertEquals(Boolean.FALSE, expr1.computeValue(null));

        TestRelationalExpression expr2 = new TestRelationalExpression(numExpr, emptyExpr, 0);
        Assert.assertEquals(Boolean.FALSE, expr2.computeValue(null));

        TestRelationalExpression expr3 = new TestRelationalExpression(emptyExpr, emptyExpr, 0);
        Assert.assertEquals(Boolean.FALSE, expr3.computeValue(null));
    }

    @Test
    public void testComputeWithInitialContext() {
        JXPathContextReferenceImpl parentContext = new JXPathContextReferenceImpl(null, "testValue", null);
        NodePointer rootPointer = NodePointer.newNodePointer(null, "root", Locale.getDefault());
        RootContext rootContext = new RootContext(parentContext, rootPointer);

        InitialContext initContextLeft = new InitialContext(rootContext);
        MockConstantExpression left = new MockConstantExpression(initContextLeft);
        MockConstantExpression right = new MockConstantExpression("root");

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Object result = expr.computeValue(null);
        Assert.assertNotNull(result);

        InitialContext initContextRight = new InitialContext(rootContext);
        TestRelationalExpression expr2 = new TestRelationalExpression(new MockConstantExpression("root"), new MockConstantExpression(initContextRight), 0);
        Object result2 = expr2.computeValue(null);
        Assert.assertNotNull(result2);
    }

    @Test
    public void testComputeWithSelfContext() {
        JXPathContextReferenceImpl parentContext = new JXPathContextReferenceImpl(null, 42.0, null);
        NodePointer rootPointer = NodePointer.newNodePointer(null, 42.0, Locale.getDefault());
        RootContext rootContext = new RootContext(parentContext, rootPointer);
        InitialContext initContext = new InitialContext(rootContext);
        SelfContext selfContext = new SelfContext(initContext, new NodeTypeTest(1));

        MockConstantExpression left = new MockConstantExpression(selfContext);
        MockConstantExpression right = new MockConstantExpression(42.0);

        TestRelationalExpression expr = new TestRelationalExpression(left, right, 0);
        Object result = expr.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeWithIteratorsDirectly() {
        List<Integer> list1 = new ArrayList<Integer>();
        list1.add(5);
        list1.add(15);

        List<Integer> list2 = new ArrayList<Integer>();
        list2.add(20);
        list2.add(15);

        MockConstantExpression itLeft = new MockConstantExpression(list1.iterator());
        MockConstantExpression itRight = new MockConstantExpression(list2.iterator());

        TestRelationalExpression expr = new TestRelationalExpression(itLeft, itRight, 0);
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testComputeWithDoubleComparisonBranches() {
        CoreOperationLessThan lessThan = new CoreOperationLessThan(new Constant(5.0), new Constant(10.0));
        Assert.assertEquals(Boolean.TRUE, lessThan.computeValue(null));

        CoreOperationLessThan lessThanFalse = new CoreOperationLessThan(new Constant(10.0), new Constant(5.0));
        Assert.assertEquals(Boolean.FALSE, lessThanFalse.computeValue(null));

        CoreOperationLessThan lessThanEqual = new CoreOperationLessThan(new Constant(5.0), new Constant(5.0));
        Assert.assertEquals(Boolean.FALSE, lessThanEqual.computeValue(null));

        CoreOperationGreaterThan greaterThan = new CoreOperationGreaterThan(new Constant(10.0), new Constant(5.0));
        Assert.assertEquals(Boolean.TRUE, greaterThan.computeValue(null));

        CoreOperationGreaterThan greaterThanFalse = new CoreOperationGreaterThan(new Constant(5.0), new Constant(10.0));
        Assert.assertEquals(Boolean.FALSE, greaterThanFalse.computeValue(null));

        CoreOperationGreaterThanOrEqual gte = new CoreOperationGreaterThanOrEqual(new Constant(5.0), new Constant(5.0));
        Assert.assertEquals(Boolean.TRUE, gte.computeValue(null));

        CoreOperationLessThanOrEqual lte = new CoreOperationLessThanOrEqual(new Constant(5.0), new Constant(5.0));
        Assert.assertEquals(Boolean.TRUE, lte.computeValue(null));
    }
}
