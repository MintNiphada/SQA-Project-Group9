package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationCompareTest {

    // Concrete subclass for testing
    private static class TestableCoreOperationCompare extends CoreOperationCompare {
        public TestableCoreOperationCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }
        @Override
        public Object compute(EvalContext context) {
            return null;
        }
        public boolean callEqual(EvalContext context, Expression left, Expression right) {
            return equal(context, left, right);
        }
        public boolean callContains(Iterator it, Object value) {
            return contains(it, value);
        }
        public boolean callFindMatch(Iterator lit, Iterator rit) {
            return findMatch(lit, rit);
        }
        public boolean callEqual(Object l, Object r) {
            return equal(l, r);
        }
    }

    // Simple Expression that returns a constant value
    private static class ConstantExpression extends Expression {
        private final Object value;
        public ConstantExpression(Object value) {
            this.value = value;
        }
        @Override
        public Object compute(EvalContext context) {
            return value;
        }
        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    // Stub for EvalContext
    private static class EvalContextStub extends EvalContext {
    }

    // Stub for InitialContext
    private static class InitialContextStub extends InitialContext {
        private boolean resetCalled = false;
        public InitialContextStub() {
            super(null, null);
        }
        @Override
        public void reset() {
            resetCalled = true;
        }
        public boolean isResetCalled() {
            return resetCalled;
        }
    }

    // Stub for SelfContext
    private static class SelfContextStub extends SelfContext {
        private Pointer pointer;
        public SelfContextStub(Pointer pointer) {
            super(null, null);
            this.pointer = pointer;
        }
        @Override
        public Pointer getSingleNodePointer() {
            return pointer;
        }
    }

    // Simple Pointer implementation with value-based equality
    private static class SimplePointer implements Pointer {
        private final Object value;
        public SimplePointer(Object value) {
            this.value = value;
        }
        @Override
        public Object getValue() {
            return value;
        }
        @Override
        public Object getNode() { return null; }
        @Override
        public Object getRootNode() { return null; }
        @Override
        public Pointer getPointerByID(String id) { return null; }
        @Override
        public String asPath() { return null; }
        @Override
        public Pointer clone() { return this; }
        @Override
        public int compareTo(Object o) { return 0; }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimplePointer)) return false;
            SimplePointer other = (SimplePointer) obj;
            return value == null ? other.value == null : value.equals(other.value);
        }
        @Override
        public int hashCode() {
            return value == null ? 0 : value.hashCode();
        }
    }

    @Test
    public void testConstructor() {
        Expression arg1 = new ConstantExpression("a");
        Expression arg2 = new ConstantExpression("b");
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(arg1, arg2);
        Expression[] args = op.getArguments();
        assertEquals(2, args.length);
        assertSame(arg1, args[0]);
        assertSame(arg2, args[1]);
    }

    @Test
    public void testGetPrecedence() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(
            new ConstantExpression("a"), new ConstantExpression("b"));
        assertEquals(2, op.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(
            new ConstantExpression("a"), new ConstantExpression("b"));
        assertTrue(op.isSymmetric());
    }

    @Test
    public void testEqualWithSimpleValues() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Expression left = new ConstantExpression("hello");
        Expression right = new ConstantExpression("hello");
        assertTrue(op.callEqual(context, left, right));
        right = new ConstantExpression("world");
        assertFalse(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithInitialContextLeft() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        InitialContextStub initCtx = new InitialContextStub();
        Expression left = new ConstantExpression(initCtx);
        Expression right = new ConstantExpression("value");
        op.callEqual(context, left, right);
        assertTrue(initCtx.isResetCalled());
    }

    @Test
    public void testEqualWithInitialContextRight() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        InitialContextStub initCtx = new InitialContextStub();
        Expression left = new ConstantExpression("value");
        Expression right = new ConstantExpression(initCtx);
        op.callEqual(context, left, right);
        assertTrue(initCtx.isResetCalled());
    }

    @Test
    public void testEqualWithSelfContextLeft() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Pointer ptr = new SimplePointer("pointerValue");
        SelfContextStub selfCtx = new SelfContextStub(ptr);
        Expression left = new ConstantExpression(selfCtx);
        Expression right = new ConstantExpression("pointerValue");
        assertTrue(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithSelfContextRight() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Pointer ptr = new SimplePointer("pointerValue");
        SelfContextStub selfCtx = new SelfContextStub(ptr);
        Expression left = new ConstantExpression("pointerValue");
        Expression right = new ConstantExpression(selfCtx);
        assertTrue(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithCollectionLeft() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Collection coll = Arrays.asList("a", "b");
        Expression left = new ConstantExpression(coll);
        Expression right = new ConstantExpression("b");
        assertTrue(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithCollectionRight() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Collection coll = Arrays.asList("a", "b");
        Expression left = new ConstantExpression("b");
        Expression right = new ConstantExpression(coll);
        assertTrue(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithBothCollections() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Collection coll1 = Arrays.asList("a", "b");
        Collection coll2 = Arrays.asList("c", "b");
        Expression left = new ConstantExpression(coll1);
        Expression right = new ConstantExpression(coll2);
        assertTrue(op.callEqual(context, left, right));
    }

    @Test
    public void testEqualWithBothCollectionsNoMatch() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        EvalContext context = new EvalContextStub();
        Collection coll1 = Arrays.asList("a", "b");
        Collection coll2 = Arrays.asList("c", "d");
        Expression left = new ConstantExpression(coll1);
        Expression right = new ConstantExpression(coll2);
        assertFalse(op.callEqual(context, left, right));
    }

    @Test
    public void testContainsFound() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertTrue(op.callContains(it, "b"));
    }

    @Test
    public void testContainsNotFound() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertFalse(op.callContains(it, "d"));
    }

    @Test
    public void testContainsEmptyIterator() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator it = Collections.emptyList().iterator();
        assertFalse(op.callContains(it, "anything"));
    }

    @Test
    public void testFindMatchFound() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator lit = Arrays.asList("a", "b").iterator();
        Iterator rit = Arrays.asList("c", "b").iterator();
        assertTrue(op.callFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchNotFound() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator lit = Arrays.asList("a", "b").iterator();
        Iterator rit = Arrays.asList("c", "d").iterator();
        assertFalse(op.callFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchWithEmptyLeft() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator lit = Collections.emptyList().iterator();
        Iterator rit = Arrays.asList("a").iterator();
        assertFalse(op.callFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchWithEmptyRight() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Iterator lit = Arrays.asList("a").iterator();
        Iterator rit = Collections.emptyList().iterator();
        assertFalse(op.callFindMatch(lit, rit));
    }

    @Test
    public void testEqualBothPointersEqual() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p1 = new SimplePointer("value");
        Pointer p2 = new SimplePointer("value");
        assertTrue(op.callEqual(p1, p2));
    }

    @Test
    public void testEqualBothPointersNotEqual() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p1 = new SimplePointer("value1");
        Pointer p2 = new SimplePointer("value2");
        assertFalse(op.callEqual(p1, p2));
    }

    @Test
    public void testEqualLeftPointer() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p = new SimplePointer("hello");
        assertTrue(op.callEqual(p, "hello"));
    }

    @Test
    public void testEqualRightPointer() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p = new SimplePointer("hello");
        assertTrue(op.callEqual("hello", p));
    }

    @Test
    public void testEqualSameReference() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Object obj = new Object();
        assertTrue(op.callEqual(obj, obj));
    }

    @Test
    public void testEqualBoolean() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual(true, Boolean.TRUE));
        assertFalse(op.callEqual(true, false));
        assertTrue(op.callEqual(true, "true"));
        assertFalse(op.callEqual(true, "false"));
    }

    @Test
    public void testEqualNumber() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual(1, 1.0));
        assertFalse(op.callEqual(1, 2));
        assertFalse(op.callEqual(Double.NaN, Double.NaN));
        assertFalse(op.callEqual(Double.NaN, 1.0));
        assertFalse(op.callEqual(1.0, Double.NaN));
    }

    @Test
    public void testEqualString() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual("hello", "hello"));
        assertFalse(op.callEqual("hello", "world"));
        assertTrue(op.callEqual("1", 1));
    }

    @Test
    public void testEqualOther() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Object obj1 = new Object();
        Object obj2 = new Object();
        assertTrue(op.callEqual(obj1, obj1));
        assertFalse(op.callEqual(obj1, obj2));
        assertFalse(op.callEqual(null, obj1));
        assertFalse(op.callEqual(obj1, null));
        assertTrue(op.callEqual(null, null));
    }

    @Test
    public void testEqualMixedBooleanAndNumber() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual(true, 1));
        assertFalse(op.callEqual(true, 0));
    }

    @Test
    public void testEqualMixedNumberAndString() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual(1, "1.0"));
        assertFalse(op.callEqual(1, "2.0"));
    }

    @Test
    public void testEqualMixedStringAndBoolean() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        assertTrue(op.callEqual("true", true));
        assertFalse(op.callEqual("false", true));
    }

    @Test
    public void testEqualBothPointersEqualReturnsTrueImmediately() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p1 = new SimplePointer("value") {
            @Override
            public boolean equals(Object obj) {
                return true;
            }
        };
        Pointer p2 = new SimplePointer("different");
        assertTrue(op.callEqual(p1, p2));
    }

    @Test
    public void testEqualBothPointersNotEqualExtractsValues() {
        TestableCoreOperationCompare op = new TestableCoreOperationCompare(null, null);
        Pointer p1 = new SimplePointer("hello") {
            @Override
            public boolean equals(Object obj) {
                return false;
            }
        };
        Pointer p2 = new SimplePointer("hello") {
            @Override
            public boolean equals(Object obj) {
                return false;
            }
        };
        assertTrue(op.callEqual(p1, p2));
    }
}
