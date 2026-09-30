package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationCompareTest {

    private static class TestCompareOperation extends CoreOperationCompare {
        public TestCompareOperation(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        public String getSymbol() {
            return "==";
        }
    }

    private TestCompareOperation compareOp;

    @Before
    public void setUp() {
        compareOp = new TestCompareOperation(new Constant("test1"), new Constant("test2"));
    }

    @Test
    public void testGetPrecedence() {
        assertEquals(2, compareOp.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        assertTrue(compareOp.isSymmetric());
    }

    @Test
    public void testEqualObjectsSameInstance() {
        Object obj = new Object();
        assertTrue(compareOp.equal(obj, obj));
        assertTrue(compareOp.equal((Object) null, (Object) null));
    }

    @Test
    public void testEqualNullAndNonNull() {
        assertFalse(compareOp.equal(null, new Object()));
        assertFalse(compareOp.equal(new Object(), null));
    }

    @Test
    public void testEqualPointers() {
        NodePointer np1 = NodePointer.newNodePointer(new QName("test"), "value1", Locale.ENGLISH);
        NodePointer np2 = NodePointer.newNodePointer(new QName("test"), "value1", Locale.ENGLISH);
        NodePointer np3 = NodePointer.newNodePointer(new QName("test"), "value2", Locale.ENGLISH);

        assertTrue(compareOp.equal(np1, np1));
        assertTrue(compareOp.equal(np1, np2));
        assertFalse(compareOp.equal(np1, np3));

        assertTrue(compareOp.equal(np1, "value1"));
        assertTrue(compareOp.equal("value1", np1));
        assertFalse(compareOp.equal(np1, "value2"));
    }

    @Test
    public void testEqualBooleans() {
        assertTrue(compareOp.equal(Boolean.TRUE, Boolean.TRUE));
        assertFalse(compareOp.equal(Boolean.TRUE, Boolean.FALSE));
        assertTrue(compareOp.equal(Boolean.TRUE, "true"));
        assertTrue(compareOp.equal(Boolean.TRUE, Double.valueOf(1.0)));
        assertFalse(compareOp.equal(Boolean.TRUE, Double.valueOf(0.0)));
        assertTrue(compareOp.equal("false", Boolean.FALSE));
    }

    @Test
    public void testEqualNumbers() {
        assertTrue(compareOp.equal(Integer.valueOf(10), Double.valueOf(10.0)));
        assertTrue(compareOp.equal(Double.valueOf(5.5), "5.5"));
        assertTrue(compareOp.equal("5.5", Double.valueOf(5.5)));
        assertFalse(compareOp.equal(Integer.valueOf(10), Integer.valueOf(20)));

        assertFalse(compareOp.equal(Double.valueOf(Double.NaN), Double.valueOf(Double.NaN)));
        assertFalse(compareOp.equal(Double.valueOf(Double.NaN), Integer.valueOf(1)));
        assertFalse(compareOp.equal(Integer.valueOf(1), Double.valueOf(Double.NaN)));
    }

    @Test
    public void testEqualStrings() {
        assertTrue(compareOp.equal("hello", "hello"));
        assertFalse(compareOp.equal("hello", "world"));
    }

    @Test
    public void testEqualCustomObjects() {
        Object obj1 = new Object() {
            @Override
            public boolean equals(Object o) {
                return o instanceof Object;
            }
        };
        Object obj2 = new Object();
        assertTrue(compareOp.equal(obj1, obj2));

        Object nonEqual1 = new Object();
        Object nonEqual2 = new Object();
        assertFalse(compareOp.equal(nonEqual1, nonEqual2));
    }

    @Test
    public void testContains() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        assertTrue(compareOp.contains(list.iterator(), "banana"));
        assertFalse(compareOp.contains(list.iterator(), "orange"));
        assertFalse(compareOp.contains(Collections.emptyIterator(), "apple"));
    }

    @Test
    public void testFindMatch() {
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("d", "b", "e");
        List<String> list3 = Arrays.asList("x", "y", "z");

        assertTrue(compareOp.findMatch(list1.iterator(), list2.iterator()));
        assertFalse(compareOp.findMatch(list1.iterator(), list3.iterator()));
        assertFalse(compareOp.findMatch(Collections.emptyIterator(), list2.iterator()));
        assertFalse(compareOp.findMatch(list1.iterator(), Collections.emptyIterator()));
    }

    @Test
    public void testEqualContextWithConstants() {
        Expression left = new Constant("test");
        Expression right = new Constant("test");
        assertTrue(compareOp.equal(null, left, right));

        Expression rightDiff = new Constant("other");
        assertFalse(compareOp.equal(null, left, rightDiff));
    }

    @Test
    public void testEqualContextWithCollections() {
        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("a", "b");
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        Expression rightMatch = new Constant("b");
        Expression rightNoMatch = new Constant("c");

        assertTrue(compareOp.equal(null, left, rightMatch));
        assertFalse(compareOp.equal(null, left, rightNoMatch));

        assertTrue(compareOp.equal(null, rightMatch, left));
        assertFalse(compareOp.equal(null, rightNoMatch, left));

        Expression rightCollectionMatch = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("c", "b");
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        Expression rightCollectionNoMatch = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("c", "d");
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        assertTrue(compareOp.equal(null, left, rightCollectionMatch));
        assertFalse(compareOp.equal(null, left, rightCollectionNoMatch));
    }

    @Test
    public void testEqualContextWithIterators() {
        Expression leftIter = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("1", "2").iterator();
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        Expression rightIter = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("2", "3").iterator();
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        assertTrue(compareOp.equal(null, leftIter, rightIter));
    }

    @Test
    public void testEqualContextWithInitialContextAndSelfContext() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), "nodeValue", Locale.ENGLISH);
        RootContext rootContext = new RootContext(jxContext, rootPointer);

        InitialContext initialCtx1 = new InitialContext(rootContext);
        InitialContext initialCtx2 = new InitialContext(rootContext);

        Expression exprInit1 = new Expression() {
            public Object compute(EvalContext context) {
                return initialCtx1;
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        Expression exprInit2 = new Expression() {
            public Object compute(EvalContext context) {
                return initialCtx2;
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        assertTrue(compareOp.equal(null, exprInit1, exprInit2));

        NodeTypeTest nodeTest = new NodeTypeTest(1);
        SelfContext selfCtx1 = new SelfContext(initialCtx1, nodeTest);
        SelfContext selfCtx2 = new SelfContext(initialCtx2, nodeTest);

        Expression exprSelf1 = new Expression() {
            public Object compute(EvalContext context) {
                return selfCtx1;
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        Expression exprSelf2 = new Expression() {
            public Object compute(EvalContext context) {
                return selfCtx2;
            }

            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };

        assertTrue(compareOp.equal(null, exprSelf1, exprSelf2));
    }

    @Test
    public void testEqualContextWithNullPointer() {
        Pointer nullPointer = new NullPointer(Locale.ENGLISH, "id");
        Pointer valuePointer = NodePointer.newNodePointer(new QName("test"), null, Locale.ENGLISH);

        assertTrue(compareOp.equal(nullPointer, valuePointer));
    }

    @Test
    public void testComputeValue() {
        TestCompareOperation op = new TestCompareOperation(new Constant("same"), new Constant("same"));
        assertEquals(Boolean.TRUE, op.computeValue(null));

        TestCompareOperation opDiff = new TestCompareOperation(new Constant("same"), new Constant("diff"));
        assertEquals(Boolean.FALSE, opDiff.computeValue(null));
    }
}
