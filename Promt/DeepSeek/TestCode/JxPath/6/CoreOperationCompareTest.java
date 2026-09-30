package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CoreOperationCompareTest {

    // Testable subclass exposing protected methods
    private static class TestableCoreOperationCompare extends CoreOperationCompare {
        public TestableCoreOperationCompare() {
            super(null, null);
        }

        public boolean testEqual(EvalContext context, Expression left, Expression right) {
            return equal(context, left, right);
        }

        public boolean testContains(Iterator it, Object value) {
            return contains(it, value);
        }

        public boolean testFindMatch(Iterator lit, Iterator rit) {
            return findMatch(lit, rit);
        }

        public boolean testEqual(Object l, Object r) {
            return equal(l, r);
        }
    }

    // Stub implementations
    private static class StubExpression extends Expression {
        private final Object value;
        public StubExpression(Object value) {
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
    }

    private static class StubEvalContext extends EvalContext {
        private Pointer singleNodePointer;
        public StubEvalContext() {
            super(null);
        }
        public void setSingleNodePointer(Pointer p) {
            this.singleNodePointer = p;
        }
        @Override
        public Pointer getSingleNodePointer() {
            return singleNodePointer;
        }
        @Override
        public boolean nextNode() {
            return false;
        }
        @Override
        public boolean setPosition(int position) {
            return false;
        }
        @Override
        public NodePointer getCurrentNodePointer() {
            return null;
        }
    }

    private static class StubPointer implements Pointer {
        private final Object value;
        public StubPointer(Object value) {
            this.value = value;
        }
        @Override
        public Object getValue() {
            return value;
        }
        @Override
        public Object getNode() {
            return null;
        }
        @Override
        public Object getRootNode() {
            return null;
        }
        @Override
        public void setValue(Object value) {
        }
        @Override
        public Object clone() {
            return this;
        }
        @Override
        public int compareTo(Object o) {
            return 0;
        }
        @Override
        public String asPath() {
            return null;
        }
        @Override
        public String toString() {
            return "StubPointer(" + value + ")";
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof StubPointer)) return false;
            StubPointer other = (StubPointer) obj;
            return value == null ? other.value == null : value.equals(other.value);
        }
        @Override
        public int hashCode() {
            return value == null ? 0 : value.hashCode();
        }
    }

    private final TestableCoreOperationCompare testable = new TestableCoreOperationCompare();

    // Helper to create an iterator from a list
    private Iterator iteratorOf(Object... items) {
        List list = new ArrayList(Arrays.asList(items));
        return list.iterator();
    }

    // Tests for equal(EvalContext, Expression, Expression)

    @Test
    public void testEqualContextBothNonIteratorValues() {
        EvalContext context = new StubEvalContext();
        Expression left = new StubExpression("a");
        Expression right = new StubExpression("a");
        assertTrue(testable.testEqual(context, left, right));
    }

    @Test
    public void testEqualContextLeftIteratorRightNonIterator() {
        EvalContext context = new StubEvalContext();
        Expression left = new StubExpression(iteratorOf("a", "b"));
        Expression right = new StubExpression("b");
        assertTrue(testable.testEqual(context, left, right));
    }

    @Test
    public void testEqualContextRightIteratorLeftNonIterator() {
        EvalContext context = new StubEvalContext();
        Expression left = new StubExpression("c");
        Expression right = new StubExpression(iteratorOf("c", "d"));
        assertTrue(testable.testEqual(context, left, right));
    }

    @Test
    public void testEqualContextBothIterators() {
        EvalContext context = new StubEvalContext();
        Expression left = new StubExpression(iteratorOf("x", "y"));
        Expression right = new StubExpression(iteratorOf("y", "z"));
        assertTrue(testable.testEqual(context, left, right));
    }

    @Test
    public void testEqualContextLeftCollectionRightNonIterator() {
        EvalContext context = new StubEvalContext();
        Collection col = new ArrayList(Arrays.asList("m", "n"));
        Expression left = new StubExpression(col);
        Expression right = new StubExpression("n");
        assertTrue(testable.testEqual(context, left, right));
    }

    @Test
    public void testEqualContextRightCollectionLeftNonIterator() {
        EvalContext context = new StubEvalContext();
        Collection col = new ArrayList(Arrays.asList("p", "q"));
        Expression left = new StubExpression("q");
        Expression right = new StubExpression(col);
        assertTrue(testable.testEqual(context, left, right));
    }

    // Tests for contains(Iterator, Object)

    @Test
    public void testContainsEmptyIterator() {
        assertFalse(testable.testContains(Collections.emptyIterator(), "any"));
    }

    @Test
    public void testContainsValuePresent() {
        Iterator it = iteratorOf("apple", "banana", "cherry");
        assertTrue(testable.testContains(it, "banana"));
    }

    @Test
    public void testContainsValueAbsent() {
        Iterator it = iteratorOf("apple", "banana", "cherry");
        assertFalse(testable.testContains(it, "grape"));
    }

    @Test
    public void testContainsHandlesNullValueAndElement() {
        Iterator it = iteratorOf(null, "a", null);
        assertTrue(testable.testContains(it, null));
    }

    // Tests for findMatch(Iterator, Iterator)

    @Test
    public void testFindMatchNoCommon() {
        Iterator lit = iteratorOf(1, 2, 3);
        Iterator rit = iteratorOf(4, 5, 6);
        assertFalse(testable.testFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchCommon() {
        Iterator lit = iteratorOf(1, 2, 3);
        Iterator rit = iteratorOf(3, 4, 5);
        assertTrue(testable.testFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchLeftEmpty() {
        Iterator lit = Collections.emptyIterator();
        Iterator rit = iteratorOf(1, 2);
        assertFalse(testable.testFindMatch(lit, rit));
    }

    @Test
    public void testFindMatchRightEmpty() {
        Iterator lit = iteratorOf(1, 2);
        Iterator rit = Collections.emptyIterator();
        assertFalse(testable.testFindMatch(lit, rit));
    }

    // Tests for equal(Object, Object)

    @Test
    public void testEqualObjectBothPointersEqual() {
        Pointer p1 = new StubPointer("value");
        Pointer p2 = new StubPointer("value");
        assertTrue(testable.testEqual(p1, p2));
    }

    @Test
    public void testEqualObjectBothPointersNotEqual() {
        Pointer p1 = new StubPointer("value1");
        Pointer p2 = new StubPointer("value2");
        assertFalse(testable.testEqual(p1, p2));
    }

    @Test
    public void testEqualObjectLeftPointerRightValue() {
        Pointer p = new StubPointer("data");
        Object r = "data";
        assertTrue(testable.testEqual(p, r));
    }

    @Test
    public void testEqualObjectRightPointerLeftValue() {
        Object l = "data";
        Pointer p = new StubPointer("data");
        assertTrue(testable.testEqual(l, p));
    }

    @Test
    public void testEqualObjectSameReference() {
        Object obj = new Object();
        assertTrue(testable.testEqual(obj, obj));
    }

    @Test
    public void testEqualObjectBothNull() {
        assertTrue(testable.testEqual(null, null));
    }

    @Test
    public void testEqualObjectLeftNullRightNotNull() {
        assertFalse(testable.testEqual(null, "a"));
    }

    @Test
    public void testEqualObjectLeftNotNullRightNull() {
        assertFalse(testable.testEqual("a", null));
    }

    @Test
    public void testEqualObjectBooleanBoth() {
        assertTrue(testable.testEqual(Boolean.TRUE, true));
        assertTrue(testable.testEqual(false, Boolean.FALSE));
        assertFalse(testable.testEqual(true, false));
    }

    @Test
    public void testEqualObjectBooleanAndNumber() {
        assertTrue(testable.testEqual(true, 1.0));
        assertTrue(testable.testEqual(false, 0.0));
        assertFalse(testable.testEqual(true, 0.0));
        assertFalse(testable.testEqual(false, 1.0));
    }

    @Test
    public void testEqualObjectBooleanAndString() {
        assertTrue(testable.testEqual(true, "true"));
        assertTrue(testable.testEqual(false, "false"));
        assertFalse(testable.testEqual(true, "false"));
        assertFalse(testable.testEqual(false, "true"));
    }

    @Test
    public void testEqualObjectNumberBoth() {
        assertTrue(testable.testEqual(1, 1));
        assertTrue(testable.testEqual(1.0, 1));
        assertTrue(testable.testEqual(2.5, 2.5));
        assertFalse(testable.testEqual(1, 2));
        assertFalse(testable.testEqual(1.0, 2.0));
    }

    @Test
    public void testEqualObjectNumberAndString() {
        assertTrue(testable.testEqual(1, "1.0"));
        assertTrue(testable.testEqual(1.5, "1.5"));
        assertFalse(testable.testEqual(1, "2.0"));
    }

    @Test
    public void testEqualObjectStringBoth() {
        assertTrue(testable.testEqual("abc", "abc"));
        assertFalse(testable.testEqual("abc", "def"));
    }

    @Test
    public void testEqualObjectStringAndOther() {
        // If one is String, stringValue is used for both
        assertTrue(testable.testEqual("1", 1.0));
        assertFalse(testable.testEqual("1.0", 2));
    }

    @Test
    public void testEqualObjectFallbackEquals() {
        // Neither Boolean, Number, String, Pointer, nor same reference
        Object l = new Object() {
            @Override
            public boolean equals(Object obj) {
                return obj != null && obj.getClass() == this.getClass();
            }
        };
        Object r = new Object() {
            @Override
            public boolean equals(Object obj) {
                return obj != null && obj.getClass() == this.getClass();
            }
        };
        assertTrue(testable.testEqual(l, r));
        assertFalse(testable.testEqual(l, new Object()));
    }
}
