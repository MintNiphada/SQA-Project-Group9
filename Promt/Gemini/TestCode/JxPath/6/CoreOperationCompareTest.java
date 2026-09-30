package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CoreOperationCompareTest {

    private static class ConcreteOperationCompare extends CoreOperationCompare {
        public ConcreteOperationCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        @Override
        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public String getSymbol() {
            return "==";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }

        @Override
        public boolean isSymmetric() {
            return true;
        }
    }

    private static class DummyExpression extends Expression {
        private final Object value;

        public DummyExpression(Object value) {
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

    private static class DummyPointer implements Pointer {
        private final Object value;
        private final boolean equalsOther;

        public DummyPointer(Object value) {
            this(value, false);
        }

        public DummyPointer(Object value, boolean equalsOther) {
            this.value = value;
            this.equalsOther = equalsOther;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getNode() {
            return value;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public Object getRootNode() {
            return null;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }

        @Override
        public Pointer clone() {
            return this;
        }

        @Override
        public String asPath() {
            return "";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (equalsOther && obj instanceof DummyPointer) {
                DummyPointer other = (DummyPointer) obj;
                return value != null ? value.equals(other.value) : other.value == null;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    private ConcreteOperationCompare operation;

    @Before
    public void setUp() {
        operation = new ConcreteOperationCompare(new DummyExpression(null), new DummyExpression(null));
    }

    @Test
    public void testEqualObjectsSameReference() {
        Object obj = new Object();
        Assert.assertTrue(operation.equal(obj, obj));
        Assert.assertTrue(operation.equal((Object) null, (Object) null));
    }

    @Test
    public void testEqualPointers() {
        DummyPointer p1 = new DummyPointer("test", true);
        DummyPointer p2 = new DummyPointer("test", true);
        Assert.assertTrue(operation.equal(p1, p2));

        DummyPointer p3 = new DummyPointer("test", false);
        DummyPointer p4 = new DummyPointer("test", false);
        Assert.assertTrue(operation.equal(p3, p4));

        DummyPointer p5 = new DummyPointer("valueA", false);
        DummyPointer p6 = new DummyPointer("valueB", false);
        Assert.assertFalse(operation.equal(p5, p6));

        DummyPointer pLeft = new DummyPointer("hello");
        Assert.assertTrue(operation.equal(pLeft, "hello"));
        Assert.assertFalse(operation.equal(pLeft, "world"));

        DummyPointer pRight = new DummyPointer("world");
        Assert.assertTrue(operation.equal("world", pRight));
        Assert.assertFalse(operation.equal("hello", pRight));
    }

    @Test
    public void testEqualBooleans() {
        Assert.assertTrue(operation.equal(Boolean.TRUE, Boolean.TRUE));
        Assert.assertTrue(operation.equal(Boolean.FALSE, Boolean.FALSE));
        Assert.assertFalse(operation.equal(Boolean.TRUE, Boolean.FALSE));

        Assert.assertTrue(operation.equal(Boolean.TRUE, "true"));
        Assert.assertTrue(operation.equal(Boolean.TRUE, 1));
        Assert.assertTrue(operation.equal(Boolean.FALSE, 0));
        Assert.assertFalse(operation.equal(Boolean.TRUE, 0));
        Assert.assertFalse(operation.equal("false", Boolean.FALSE));
    }

    @Test
    public void testEqualNumbers() {
        Assert.assertTrue(operation.equal(1, 1));
        Assert.assertTrue(operation.equal(1.0, 1));
        Assert.assertTrue(operation.equal(100L, 100.0));
        Assert.assertFalse(operation.equal(1, 2));

        Assert.assertTrue(operation.equal(42, "42"));
        Assert.assertTrue(operation.equal("42.0", 42));
        Assert.assertFalse(operation.equal(42, "invalid_number"));
        Assert.assertFalse(operation.equal("invalid_number", 42));
    }

    @Test
    public void testEqualStrings() {
        Assert.assertTrue(operation.equal("abc", "abc"));
        Assert.assertFalse(operation.equal("abc", "def"));
        Assert.assertFalse(operation.equal("abc", null));
        Assert.assertFalse(operation.equal(null, "abc"));
    }

    @Test
    public void testEqualOtherObjects() {
        List<String> list1 = new ArrayList<String>(Collections.singletonList("a"));
        List<String> list2 = new ArrayList<String>(Collections.singletonList("a"));
        List<String> list3 = new ArrayList<String>(Collections.singletonList("b"));

        Assert.assertTrue(operation.equal((Object) list1, (Object) list2));
        Assert.assertFalse(operation.equal((Object) list1, (Object) list3));

        Object obj = new Object();
        Assert.assertFalse(operation.equal(obj, null));
        Assert.assertFalse(operation.equal(null, obj));
    }

    @Test
    public void testContains() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        Assert.assertTrue(operation.contains(list.iterator(), "banana"));
        Assert.assertTrue(operation.contains(list.iterator(), "apple"));
        Assert.assertTrue(operation.contains(list.iterator(), "cherry"));
        Assert.assertFalse(operation.contains(list.iterator(), "grape"));
        Assert.assertFalse(operation.contains(Collections.emptyList().iterator(), "apple"));
    }

    @Test
    public void testFindMatch() {
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("x", "b", "z");
        List<String> list3 = Arrays.asList("x", "y", "z");

        Assert.assertTrue(operation.findMatch(list1.iterator(), list2.iterator()));
        Assert.assertFalse(operation.findMatch(list1.iterator(), list3.iterator()));
        Assert.assertFalse(operation.findMatch(Collections.emptyList().iterator(), list2.iterator()));
        Assert.assertFalse(operation.findMatch(list1.iterator(), Collections.emptyList().iterator()));
    }

    @Test
    public void testEqualContextEvaluationInitialContextAndSelfContext() {
        final Pointer pointerL = new DummyPointer("val");
        final Pointer pointerR = new DummyPointer("val");

        InitialContext initialContextL = new InitialContext(null) {
            @Override
            public Pointer getSingleNodePointer() {
                return pointerL;
            }
        };

        SelfContext selfContextR = new SelfContext(null, null) {
            @Override
            public Pointer getSingleNodePointer() {
                return pointerR;
            }
        };

        Expression exprL = new DummyExpression(initialContextL);
        Expression exprR = new DummyExpression(selfContextR);
        Assert.assertTrue(operation.equal(null, exprL, exprR));

        InitialContext initialContextR = new InitialContext(null) {
            @Override
            public Pointer getSingleNodePointer() {
                return new DummyPointer("diff");
            }
        };
        Expression exprRDiff = new DummyExpression(initialContextR);
        Assert.assertFalse(operation.equal(null, exprL, exprRDiff));

        SelfContext selfContextL = new SelfContext(null, null) {
            @Override
            public Pointer getSingleNodePointer() {
                return pointerL;
            }
        };
        Expression exprSelfL = new DummyExpression(selfContextL);
        Assert.assertTrue(operation.equal(null, exprSelfL, exprR));
    }

    @Test
    public void testEqualContextEvaluationCollections() {
        Set<String> setLeft = new HashSet<String>(Arrays.asList("1", "2"));
        List<Integer> listRight = Arrays.asList(2, 3);
        List<Integer> listNoMatch = Arrays.asList(4, 5);

        Expression exprSet = new DummyExpression(setLeft);
        Expression exprListMatch = new DummyExpression(listRight);
        Expression exprListNoMatch = new DummyExpression(listNoMatch);

        Assert.assertTrue(operation.equal(null, exprSet, exprListMatch));
        Assert.assertFalse(operation.equal(null, exprSet, exprListNoMatch));
    }

    @Test
    public void testEqualContextEvaluationLeftIteratorRightScalar() {
        List<String> list = Arrays.asList("10", "20", "30");
        Expression exprIter = new DummyExpression(list.iterator());
        Expression exprScalarMatch = new DummyExpression(20);
        Expression exprScalarNoMatch = new DummyExpression(99);

        Assert.assertTrue(operation.equal(null, exprIter, exprScalarMatch));
        Assert.assertFalse(operation.equal(null, new DummyExpression(list.iterator()), exprScalarNoMatch));
    }

    @Test
    public void testEqualContextEvaluationLeftScalarRightIterator() {
        List<String> list = Arrays.asList("foo", "bar");
        Expression exprScalar = new DummyExpression("bar");
        Expression exprIter = new DummyExpression(list.iterator());

        Assert.assertTrue(operation.equal(null, exprScalar, exprIter));

        Expression exprScalarMismatch = new DummyExpression("baz");
        Assert.assertFalse(operation.equal(null, exprScalarMismatch, new DummyExpression(list.iterator())));
    }

    @Test
    public void testEqualContextEvaluationBothScalars() {
        Expression left = new DummyExpression("test");
        Expression right = new DummyExpression("test");
        Expression rightDiff = new DummyExpression("diff");

        Assert.assertTrue(operation.equal(null, left, right));
        Assert.assertFalse(operation.equal(null, left, rightDiff));
    }
}
