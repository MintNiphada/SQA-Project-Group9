package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int targetCompare;

        public TestRelationalExpression(Expression left, Expression right) {
            super(new Expression[] { left, right });
            this.targetCompare = -1; // Default to less-than (<)
        }

        public TestRelationalExpression(Expression left, Expression right, int targetCompare) {
            super(new Expression[] { left, right });
            this.targetCompare = targetCompare;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == targetCompare;
        }
    }

    private static class ValueExpression extends Expression {
        private final Object value;

        public ValueExpression(Object value) {
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

    private static class TestInitialContext extends InitialContext {
        private boolean resetCalled = false;

        public TestInitialContext() {
            super(null);
        }

        @Override
        public void reset() {
            resetCalled = true;
        }
    }

    private static class TestSelfContext extends SelfContext {
        private final NodePointer pointer;

        public TestSelfContext(NodePointer pointer) {
            super(null, null);
            this.pointer = pointer;
        }

        @Override
        public NodePointer getSingleNodePointer() {
            return pointer;
        }
    }

    @Test
    public void testGetPrecedence() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(1),
                new ValueExpression(2)
        );
        Assert.assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, expr.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(1),
                new ValueExpression(2)
        );
        Assert.assertFalse(expr.isSymmetric());
    }

    @Test
    public void testComputeValueEqual() {
        TestRelationalExpression exprEqual = new TestRelationalExpression(
                new ValueExpression(5.0),
                new ValueExpression(5.0),
                0
        );
        Assert.assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        TestRelationalExpression exprNotEqual = new TestRelationalExpression(
                new ValueExpression(5.0),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprNotEqual.computeValue(null));
    }

    @Test
    public void testComputeValueLessThan() {
        TestRelationalExpression exprLessThan = new TestRelationalExpression(
                new ValueExpression(3.0),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.TRUE, exprLessThan.computeValue(null));

        TestRelationalExpression exprGreaterThan = new TestRelationalExpression(
                new ValueExpression(3.0),
                new ValueExpression(5.0),
                1
        );
        Assert.assertEquals(Boolean.FALSE, exprGreaterThan.computeValue(null));
    }

    @Test
    public void testComputeValueGreaterThan() {
        TestRelationalExpression exprGreaterThan = new TestRelationalExpression(
                new ValueExpression(8.0),
                new ValueExpression(5.0),
                1
        );
        Assert.assertEquals(Boolean.TRUE, exprGreaterThan.computeValue(null));

        TestRelationalExpression exprLessThan = new TestRelationalExpression(
                new ValueExpression(8.0),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprLessThan.computeValue(null));
    }

    @Test
    public void testComputeValueNaN() {
        // Left is NaN
        TestRelationalExpression exprLeftNaN = new TestRelationalExpression(
                new ValueExpression(Double.NaN),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprLeftNaN.computeValue(null));

        // Right is NaN
        TestRelationalExpression exprRightNaN = new TestRelationalExpression(
                new ValueExpression(5.0),
                new ValueExpression(Double.NaN),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprRightNaN.computeValue(null));

        // Both are NaN
        TestRelationalExpression exprBothNaN = new TestRelationalExpression(
                new ValueExpression(Double.NaN),
                new ValueExpression(Double.NaN),
                0
        );
        Assert.assertEquals(Boolean.FALSE, exprBothNaN.computeValue(null));

        // String that parses to NaN
        TestRelationalExpression exprStringNaN = new TestRelationalExpression(
                new ValueExpression("not-a-number"),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprStringNaN.computeValue(null));
    }

    @Test
    public void testInitialContextReset() {
        TestInitialContext leftContext = new TestInitialContext();
        TestInitialContext rightContext = new TestInitialContext();

        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(leftContext),
                new ValueExpression(rightContext),
                0
        );

        expr.computeValue(null);

        Assert.assertTrue("Left InitialContext should be reset", leftContext.resetCalled);
        Assert.assertTrue("Right InitialContext should be reset", rightContext.resetCalled);
    }

    @Test
    public void testReduceSelfContext() {
        NodePointer pointer = NodePointer.newNodePointer(
                new QName("test"),
                Double.valueOf(10.0),
                Locale.getDefault()
        );
        TestSelfContext selfContext = new TestSelfContext(pointer);

        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(selfContext),
                new ValueExpression(5.0),
                1
        );

        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testReduceCollectionLeft() {
        List<Double> leftList = Arrays.asList(10.0, 2.0, 8.0);
        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(leftList),
                new ValueExpression(5.0),
                -1
        );

        // 2.0 < 5.0 is true
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        // Check false scenario when no element matches
        TestRelationalExpression exprFalse = new TestRelationalExpression(
                new ValueExpression(Arrays.asList(10.0, 20.0)),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testReduceCollectionRight() {
        List<Double> rightList = Arrays.asList(1.0, 7.0, 3.0);
        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(5.0),
                new ValueExpression(rightList),
                1
        );

        // 5.0 > 1.0 (or 5.0 > 3.0) is true
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));

        TestRelationalExpression exprFalse = new TestRelationalExpression(
                new ValueExpression(0.0),
                new ValueExpression(rightList),
                1
        );
        Assert.assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test
    public void testTwoCollectionsFindMatch() {
        List<Double> leftList = Arrays.asList(1.0, 5.0);
        List<Double> rightList = Arrays.asList(4.0, 8.0);

        // Left contains 1.0, Right contains 4.0; 1.0 < 4.0 -> true
        TestRelationalExpression exprMatch = new TestRelationalExpression(
                new ValueExpression(leftList),
                new ValueExpression(rightList),
                -1
        );
        Assert.assertEquals(Boolean.TRUE, exprMatch.computeValue(null));

        // Left = [10.0, 20.0], Right = [1.0, 2.0]; neither < is true
        TestRelationalExpression exprNoMatch = new TestRelationalExpression(
                new ValueExpression(Arrays.asList(10.0, 20.0)),
                new ValueExpression(Arrays.asList(1.0, 2.0)),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprNoMatch.computeValue(null));
    }

    @Test
    public void testEmptyIterators() {
        List<Double> emptyList = Collections.emptyList();
        List<Double> populatedList = Collections.singletonList(5.0);

        // Left empty iterator
        TestRelationalExpression exprEmptyLeft = new TestRelationalExpression(
                new ValueExpression(emptyList),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprEmptyLeft.computeValue(null));

        // Right empty iterator
        TestRelationalExpression exprEmptyRight = new TestRelationalExpression(
                new ValueExpression(5.0),
                new ValueExpression(emptyList),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprEmptyRight.computeValue(null));

        // Both empty iterators
        TestRelationalExpression exprBothEmpty = new TestRelationalExpression(
                new ValueExpression(emptyList),
                new ValueExpression(emptyList),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprBothEmpty.computeValue(null));

        // Left populated, Right empty
        TestRelationalExpression exprPopulatedLeftEmptyRight = new TestRelationalExpression(
                new ValueExpression(populatedList),
                new ValueExpression(emptyList),
                -1
        );
        Assert.assertEquals(Boolean.FALSE, exprPopulatedLeftEmptyRight.computeValue(null));
    }

    @Test
    public void testRawIterators() {
        List<String> leftList = Arrays.asList("10", "20", "30");
        List<String> rightList = Arrays.asList("15", "25");

        Iterator<String> leftIt = leftList.iterator();
        Iterator<String> rightIt = rightList.iterator();

        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(leftIt),
                new ValueExpression(rightIt),
                -1
        );

        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testContainsMatchWithNestedIterators() {
        List<List<Double>> nestedList = new ArrayList<List<Double>>();
        nestedList.add(Arrays.asList(10.0, 20.0));
        nestedList.add(Arrays.asList(2.0, 3.0));

        // Nested iterator evaluated against 5.0: 2.0 < 5.0 is true
        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(nestedList),
                new ValueExpression(5.0),
                -1
        );
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test
    public void testHashSetBehaviorInFindMatch() {
        HashSet<Double> leftSet = new HashSet<Double>(Arrays.asList(3.0, 7.0));
        HashSet<Double> rightSet = new HashSet<Double>(Arrays.asList(1.0, 5.0));

        TestRelationalExpression expr = new TestRelationalExpression(
                new ValueExpression(leftSet),
                new ValueExpression(rightSet),
                -1
        );
        // 3.0 < 5.0 -> true
        Assert.assertEquals(Boolean.TRUE, expr.computeValue(null));
    }
}
