package org.apache.commons.jxpath.ri.compiler;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;

public class CoreOperationRelationalExpressionTest {

    // ---------- Inner interfaces and stubs -------

    private interface CompareEvaluator {
        boolean evaluate(int compare);
    }

    private static class FlexibleRelationalExpr extends CoreOperationRelationalExpression {
        private final CompareEvaluator evaluator;
        FlexibleRelationalExpr(Expression left, Expression right, CompareEvaluator evaluator) {
            super(new Expression[]{left, right});
            this.evaluator = evaluator;
        }
        @Override
        protected boolean evaluateCompare(int compare) {
            return evaluator.evaluate(compare);
        }
    }

    private static class ConstantExpression extends Expression {
        private final Object value;
        ConstantExpression(Object value) {
            this.value = value;
        }
        @Override
        public Object compute(EvalContext context) {
            return value;
        }
        @Override
        protected int getPrecedence() { return 0; }
        @Override
        protected boolean isSymmetric() { return false; }
    }

    private static class DummyEvalContext extends EvalContext {
        public DummyEvalContext() { super(null, null); }
        @Override public org.apache.commons.jxpath.ri.Pointer getSingleNodePointer() { return null; }
        @Override public org.apache.commons.jxpath.ri.NodePointer getCurrentNodePointer() { return null; }
        @Override public boolean nextNode() { return false; }
        @Override public boolean setPosition(int pos) { return false; }
        @Override public int getCurrentPosition() { return 0; }
        @Override public void reset() {}
        @Override public boolean nextSet() { return false; }
        @Override public Object getValue() { return null; }
    }

    private static class TestInitialContext extends InitialContext {
        boolean resetCalled = false;
        TestInitialContext() {
            super(new DummyEvalContext());
        }
        @Override
        public void reset() {
            resetCalled = true;
            super.reset();
        }
    }

    private static class TestSelfContext extends SelfContext {
        final Object pointer = new Object(); // dummy pointer
        TestSelfContext() {
            super(new DummyEvalContext());
        }
        @Override
        public org.apache.commons.jxpath.ri.Pointer getSingleNodePointer() {
            return (org.apache.commons.jxpath.ri.Pointer) pointer;
        }
        // getSingleNodePointer returns an Object? Actually SelfContext.getSingleNodePointer returns Pointer.
        // We'll just return null for simplicity, just check that reduce returns the Pointer object.
        // To avoid compilation issues, we override and return a dummy Pointer.
        @Override
        public org.apache.commons.jxpath.ri.Pointer getSingleNodePointer() {
            return new org.apache.commons.jxpath.ri.Pointer() {
                @Override public Object getValue() { return pointer; }
                // other methods minimal
            };
        }
    }

    // ---------- Tests for basic properties -------

    @Test
    public void testGetPrecedence() {
        ConstantExpression left = new ConstantExpression(1);
        ConstantExpression right = new ConstantExpression(2);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, expr.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        ConstantExpression left = new ConstantExpression(1);
        ConstantExpression right = new ConstantExpression(2);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertFalse(expr.isSymmetric());
    }

    // ---------- Non-iterator, numeric (includes equality) -------

    @Test
    public void testComputeValueWhenEqualNumbers() {
        ConstantExpression left = new ConstantExpression(5);
        ConstantExpression right = new ConstantExpression(5);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        DummyEvalContext context = new DummyEvalContext();
        assertEquals(Bolean.TRUE, expr.computeValue(context));
    }

    @Test
    public void testComputeValueWhenNotEqualNumbers() {
        ConstantExpression left = new ConstantExpression(5);
        ConstantExpression right = new ConstantExpression(6);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testComputeValueWhenLeftLessThanRight() {
        ConstantExpression left = new ConstantExpression(5);
        ConstantExpression right = new ConstantExpression(6);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp < 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testComputeValueWhenLeftGreaterThanRight() {
        ConstantExpression left = new ConstantExpression(6);
        ConstantExpression right = new ConstantExpression(5);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp > 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- NaN handling -------

    @Test
    public void testComputeValueLeftNaN() {
        ConstantExpression left = new ConstantExpression(Double.NaN);
        ConstantExpression right = new ConstantExpression(5);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testComputeValueRightNaN() {
        ConstantExpression left = new ConstantExpression(5);
        ConstantExpression right = new ConstantExpression(Double.NaN);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testComputeValueBothNaN() {
        ConstantExpression left = new ConstantExpression(Double.NaN);
        ConstantExpression right = new ConstantExpression(Double.NaN);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- Iterator and non-iterator -------

    @Test
    public void testLeftIteratorMatches() {
        List<Object> list = Arrays.asList(1, 2, 3);
        ConstantExpression left = new ConstantExpression(list.iterator());
        ConstantExpression right = new ConstantExpression(2);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testLeftIteratorNoMatch() {
        List<Object> list = Arrays.asList(1, 2, 3);
        ConstantExpression left = new ConstantExpression(list.iterator());
        ConstantExpression right = new ConstantExpression(5);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testRightIteratorMatches() {
        ConstantExpression left = new ConstantExpression(3);
        ConstantExpression right = new ConstantExpression(Arrays.asList(1, 3, 5).iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testRightIteratorNoMatch() {
        ConstantExpression left = new ConstantExpression(3);
        ConstantExpression right = new ConstantExpression(Arrays.asList(4, 6).iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- Both iterators -------

    @Test
    public void testBothIteratorsIntersection() {
        ConstantExpression left = new ConstantExpression(Arrays.asList(1, 2, 3).iterator());
        ConstantExpression right = new ConstantExpression(Arrays.asList(2, 4).iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testBothIteratorsNoIntersection() {
        ConstantExpression left = new ConstantExpression(Arrays.asList(1, 2).iterator());
        ConstantExpression right = new ConstantExpression(Arrays.asList(3, 4).iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testBothIteratorsWithEmptyLeft() {
        ConstantExpression left = new ConstantExpression(Collections.emptyList().iterator());
        ConstantExpression right = new ConstantExpression(Arrays.asList(1).iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testBothIteratorsWithEmptyRight() {
        ConstantExpression left = new ConstantExpression(Arrays.asList(1).iterator());
        ConstantExpression right = new ConstantExpression(Collections.emptyList().iterator());
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- Reduction: SelfContext and Collection -------

    @Test
    public void testReduceSelfContext() {
        TestSelfContext self = new TestSelfContext();
        ConstantExpression left = new ConstantExpression(self);
        ConstantExpression right = new ConstantExpression(1);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        // reduction should replace SelfContext with its single node pointer, which is a pointer returning our dummy pointer value.
        // We just verify compute does not fail and evaluateCompare is called with compare based on double values.
        // Since getSingleNodePointer().getValue() returns a dummy Object (not a Number), InfoSetUtil.doubleValue will convert it (maybe 0.0). The compare will be with right (1).
        // So result should be false if we use equality evaluator. We'll assert false.
        assertEquals(Bolean.FALSE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testReduceCollectionToListIterator() {
        // Pass a collection directly; reduce converts to iterator
        ConstantExpression left = new ConstantExpression(Arrays.asList(1, 2));
        ConstantExpression right = new ConstantExpression(2);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        // The left list will be reduced to iterator -> then containsMatch
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- InitialContext reset() verification -------

    @Test
    public void testResetLeftInitialContext() {
        TestInitialContext leftInit = new TestInitialContext();
        ConstantExpression leftExpr = new ConstantExpression(leftInit);
        ConstantExpression rightExpr = new ConstantExpression(5);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(leftExpr, rightExpr, cmp -> cmp == 0);
        expr.computeValue(new DummyEvalContext());
        assertTrue(leftInit.resetCalled);
    }

    @Test
    public void testResetRightInitialContext() {
        TestInitialContext rightInit = new TestInitialContext();
        ConstantExpression leftExpr = new ConstantExpression(5);
        ConstantExpression rightExpr = new ConstantExpression(rightInit);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(leftExpr, rightExpr, cmp -> cmp == 0);
        expr.computeValue(new DummyEvalContext());
        assertTrue(rightInit.resetCalled);
    }

    // Edge: InitialContext not reset when not instanceof
    @Test
    public void testNoResetOnNonInitialContext() {
        ConstantExpression left = new ConstantExpression(new Object());
        ConstantExpression right = new ConstantExpression(1);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> true);
        // Should not fail
        expr.computeValue(new DummyEvalContext());
    }

    // ---------- containsMatch stops on first match -------

    @Test
    public void testContainsMatchStopsEarly() {
        // Use an iterator where second element matches, but we can't directly verify short-circuiting, just ensure result is true.
        ConstantExpression left = new ConstantExpression(Arrays.asList(10, 1).iterator());
        ConstantExpression right = new ConstantExpression(1);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    // ---------- Additional edge cases -------

    @Test
    public void testComputeValueWithBooleanAndNumber() {
        ConstantExpression left = new ConstantExpression(Boolean.TRUE);
        ConstantExpression right = new ConstantExpression(1); // doubleValue of true is 1.0
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

    @Test
    public void testComputeValueWithStringAndNumber() {
        ConstantExpression left = new ConstantExpression("2");
        ConstantExpression right = new ConstantExpression(2);
        FlexibleRelationalExpr expr = new FlexibleRelationalExpr(left, right, cmp -> cmp == 0);
        assertEquals(Bolean.TRUE, expr.computeValue(new DummyEvalContext()));
    }

}
