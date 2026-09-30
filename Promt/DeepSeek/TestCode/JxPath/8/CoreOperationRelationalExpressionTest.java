package org.apache.commons.jxpath.ri.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    // Concrete test subclass that records the compare argument
    private static class TestableRelationalExpression extends CoreOperationRelationalExpression {
        private boolean evaluateResult;
        private int lastCompare = Integer.MIN_VALUE;

        TestableRelationalExpression(Expression[] args, boolean evaluateResult) {
            super(args);
            this.evaluateResult = evaluateResult;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            this.lastCompare = compare;
            return evaluateResult;
        }

        public int getLastCompare() {
            return lastCompare;
        }
    }

    // Minimal Expression subclass for testing
    private static class StubExpression extends Expression {
        private final Object value;
        StubExpression(Object value) {
            this.value = value;
        }
        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }
        // Empty implementations for other abstract methods
        @Override
        public String toString() { return "StubExpression"; }
        @Override
        public org.apache.commons.jxpath.ri.QName getQName() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.compiler.Expression[] getArguments() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.compiler.Expression getArgument(int i) { return null; }
        @Override
        public int getArgumentCount() { return 0; }
        @Override
        public org.apache.commons.jxpath.ri.compiler.ExpressionPath getExpressionPath() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.compiler.Expression[] getExpressionPathSteps() { return null; }
    }

    // Stub Context for testing
    private static class StubEvalContext extends EvalContext {
        @Override
        public org.apache.commons.jxpath.Pointer getSingleNodePointer() {
            return null;
        }
        @Override
        public org.apache.commons.jxpath.Pointer getCurrentNodePointer() {
            return null;
        }
        @Override
        public int getCurrentPosition() { return 0; }
        @Override
        public boolean nextNode() { return false; }
        @Override
        public boolean nextSet() { return false; }
        @Override
        public boolean setPosition(int pos) { return false; }
        @Override
        public org.apache.commons.jxpath.ri.model.NodePointer getCurrentNodePointerInternal() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.compiler.Expression getExpression() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.EvalContext getParentContext() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.EvalContext getRootContext() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.EvalContext getVariableContext(org.apache.commons.jxpath.ri.QName varName) { return null; }
        @Override
        public org.apache.commons.jxpath.ri.model.NodeIterator getNodeIterator() { return null; }
        @Override
        public org.apache.commons.jxpath.ri.JXPathContextReferenceImpl getJXPathContext() { return null; }
        @Override
        public java.util.Iterator getPointers() { return null; }
        @Override
        public java.util.Iterator getSingleNodePointers() { return null; }
    }

    // ---------- Tests for getPrecedence and isSymmetric ----------
    @Test
    public void testGetPrecedence() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(1), new StubExpression(2)}, true);
        assertEquals(3, op.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(1), new StubExpression(2)}, true);
        assertFalse(op.isSymmetric());
    }

    // ---------- Tests for computeValue with non-iterator operands ----------
    @Test
    public void testBothNonIteratorsEqual() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(5), new StubExpression(5)}, true);
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
        assertEquals(0, op.getLastCompare());
    }

    @Test
    public void testBothNonIteratorsLeftLess() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(3), new StubExpression(7)}, false);
        assertEquals(Boolean.FALSE, op.computeValue(new StubEvalContext()));
        assertEquals(-1, op.getLastCompare());
    }

    @Test
    public void testBothNonIteratorsLeftGreater() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(10), new StubExpression(2)}, true);
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
        assertEquals(1, op.getLastCompare());
    }

    @Test
    public void testBothNonIteratorsWithNullValues() {
        // Assuming InfoSetUtil.doubleValue(null) may throw NullPointerException
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{new StubExpression(null), new StubExpression(null)}, true);
        try {
            op.computeValue(new StubEvalContext());
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    // ---------- Tests for left iterator only ----------
    @Test
    public void testLeftIteratorContainsMatch() {
        // left: iterator with values 1,3,5; right: 4
        // EvaluateCompare returns true when ld < rd (i.e. left value < right value)
        // So 3 < 4 -> true
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Arrays.asList(1, 3, 5)),
                    new StubExpression(4)
                }, false) { // default false, but we need selective true
            @Override
            protected boolean evaluateCompare(int compare) {
                // true when left < right, i.e. compare == -1
                return compare == -1;
            }
        };
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
    }

    @Test
    public void testLeftIteratorNoMatch() {
        // left: iterator with values 1,3,5; right: 4, evaluateCompare always false
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Arrays.asList(1, 3, 5)),
                    new StubExpression(4)
                }, false);
        assertEquals(Boolean.FALSE, op.computeValue(new StubEvalContext()));
    }

    // ---------- Tests for right iterator only ----------
    @Test
    public void testRightIteratorContainsMatch() {
        // left: 4, right: iterator with values 1,3,5
        // evaluateCompare returns true when right < left (i.e. compare == 1),
        // but containsMatch swaps arguments so compute(right, left)
        // so for element 3 and left 4, ld=3, rd=4 -> compare -1 -> evaluateCompare(-1) true
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(4),
                    new StubExpression(Arrays.asList(1, 3, 5))
                }, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == -1;
            }
        };
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
    }

    // ---------- Tests for both iterators ----------
    @Test
    public void testBothIteratorsFindMatch() {
        // left: [1,2,3], right: [4,2,5], evaluateCompare true when equal (compare==0)
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Arrays.asList(1, 2, 3)),
                    new StubExpression(Arrays.asList(4, 2, 5))
                }, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
    }

    @Test
    public void testBothIteratorsNoMatch() {
        // left: [1,2], right: [3,4], evaluateCompare always false
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Arrays.asList(1, 2)),
                    new StubExpression(Arrays.asList(3, 4))
                }, false);
        assertEquals(Boolean.FALSE, op.computeValue(new StubEvalContext()));
    }

    // ---------- Test reduction: SelfContext ----------
    @Test
    public void testReductionSelfContext() {
        // left is a SelfContext that returns a Pointer with value 10, right constant 5
        // evaluateCompare returns true when left > right (compare==1)
        SelfContext self = new SelfContext(null, null) {
            @Override
            public org.apache.commons.jxpath.Pointer getSingleNodePointer() {
                return new org.apache.commons.jxpath.ri.model.NodePointer(null) {
                    @Override
                    public double getValue() {
                        return 10.0;
                    }
                };
            }
        };
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(self), // this returns the SelfContext directly
                    new StubExpression(5)
                }, true) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 1;
            }
        };
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
    }

    // ---------- Test reduction: Collection ----------
    @Test
    public void testReductionCollectionToIterator() {
        // left is a Collection, after reduce becomes Iterator
        // We already tested in iterator branches; this explicitly confirms reduction
        // by ensuring that a Collection without implementing Iterator is handled.
        final java.util.ArrayList list = new java.util.ArrayList();
        list.add(1);
        list.add(2);
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(list),
                    new StubExpression(3)
                }, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == -1; // true if left < right
            }
        };
        assertEquals(Boolean.TRUE, op.computeValue(new StubEvalContext()));
    }

    // ---------- Test InitialContext reset ----------
    @Test
    public void testInitialContextResetLeft() {
        final boolean[] resetCalled = {false};
        InitialContext initCtx = new InitialContext(null) {
            @Override
            public void reset() {
                resetCalled[0] = true;
            }
        };
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(initCtx),
                    new StubExpression(2)
                }, true);
        op.computeValue(new StubEvalContext());
        assertTrue("InitialContext.reset should be called", resetCalled[0]);
    }

    @Test
    public void testInitialContextResetRight() {
        final boolean[] resetCalled = {false};
        InitialContext initCtx = new InitialContext(null) {
            @Override
            public void reset() {
                resetCalled[0] = true;
            }
        };
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(2),
                    new StubExpression(initCtx)
                }, true);
        op.computeValue(new StubEvalContext());
        assertTrue("InitialContext.reset should be called", resetCalled[0]);
    }

    // ---------- Edge cases: empty iterators ----------
    @Test
    public void testLeftIteratorEmpty() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Collections.emptyList()),
                    new StubExpression(5)
                }, false);
        assertEquals(Boolean.FALSE, op.computeValue(new StubEvalContext()));
    }

    @Test
    public void testBothIteratorsEmpty() {
        TestableRelationalExpression op = new TestableRelationalExpression(
                new Expression[]{
                    new StubExpression(Collections.emptyList()),
                    new StubExpression(Collections.emptyList())
                }, false);
        assertEquals(Boolean.FALSE, op.computeValue(new StubEvalContext()));
    }
}
