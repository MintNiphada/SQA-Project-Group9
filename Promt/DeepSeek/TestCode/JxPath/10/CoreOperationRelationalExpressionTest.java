package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    // Concrete implementation for testing
    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final boolean result;
        public TestRelationalExpression(Expression[] args, boolean result) {
            super(args);
            this.result = result;
        }
        @Override
        protected boolean evaluateCompare(int compare) {
            return result;
        }
    }

    // Mock Expression that returns a fixed value
    private static class MockExpression extends Expression {
        private final Object value;
        public MockExpression(Object value) {
            this.value = value;
        }
        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }
        @Override
        public String toString() {
            return "MockExpression";
        }
    }

    // Minimal EvalContext stub
    private static class MockEvalContext extends EvalContext {
        public MockEvalContext() {
            super(null);
        }
        @Override
        public NodePointer getCurrentNodePointer() {
            return null;
        }
        @Override
        public boolean setPosition(int position) {
            return false;
        }
        @Override
        public boolean nextNode() {
            return false;
        }
    }

    // Minimal NodePointer stub
    private static class MockNodePointer extends NodePointer {
        private final Object value;
        public MockNodePointer(Object value) {
            super(null);
            this.value = value;
        }
        @Override
        public Object getValue() {
            return value;
        }
        @Override
        public boolean isLeaf() {
            return true;
        }
        @Override
        public boolean isCollection() {
            return false;
        }
        @Override
        public int getLength() {
            return 1;
        }
        @Override
        public String asPath() {
            return "";
        }
        @Override
        public int hashCode() {
            return value == null ? 0 : value.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof MockNodePointer) {
                return value == null ? ((MockNodePointer) o).value == null : value.equals(((MockNodePointer) o).value);
            }
            return false;
        }
        @Override
        public Object getImmediateNode() {
            return value;
        }
        @Override
        public void setValue(Object value) {
            this.value = value;
        }
    }

    // Mock InitialContext that records reset calls
    private static class MockInitialContext extends InitialContext {
        private boolean resetCalled = false;
        public MockInitialContext() {
            super(new MockEvalContext(), new MockNodePointer(null));
        }
        @Override
        public void reset() {
            resetCalled = true;
        }
        public boolean isResetCalled() {
            return resetCalled;
        }
    }

    @Test
    public void testGetPrecedence() {
        Expression[] args = new Expression[]{new MockExpression(1), new MockExpression(2)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(3, op.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        Expression[] args = new Expression[]{new MockExpression(1), new MockExpression(2)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertFalse(op.isSymmetric());
    }

    @Test
    public void testComputeValueBothNonIteratorTrue() {
        Expression[] args = new Expression[]{new MockExpression(5), new MockExpression(3)};
        // evaluateCompare always true
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothNonIteratorFalse() {
        Expression[] args = new Expression[]{new MockExpression(5), new MockExpression(3)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueNaNLeft() {
        Expression[] args = new Expression[]{new MockExpression(Double.NaN), new MockExpression(3)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueNaNRight() {
        Expression[] args = new Expression[]{new MockExpression(3), new MockExpression(Double.NaN)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothNaN() {
        Expression[] args = new Expression[]{new MockExpression(Double.NaN), new MockExpression(Double.NaN)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueLeftIteratorContainsMatch() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        Expression[] args = new Expression[]{new MockExpression(list.iterator()), new MockExpression(2)};
        // evaluateCompare returns true only for compare == 0 (equality)
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueLeftIteratorNoMatch() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(3);
        Expression[] args = new Expression[]{new MockExpression(list.iterator()), new MockExpression(2)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueRightIteratorContainsMatch() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        Expression[] args = new Expression[]{new MockExpression(2), new MockExpression(list.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothIteratorsFindMatch() {
        List<Integer> leftList = new ArrayList<Integer>();
        leftList.add(1);
        leftList.add(2);
        List<Integer> rightList = new ArrayList<Integer>();
        rightList.add(2);
        rightList.add(3);
        Expression[] args = new Expression[]{new MockExpression(leftList.iterator()), new MockExpression(rightList.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothIteratorsNoMatch() {
        List<Integer> leftList = new ArrayList<Integer>();
        leftList.add(1);
        leftList.add(2);
        List<Integer> rightList = new ArrayList<Integer>();
        rightList.add(3);
        rightList.add(4);
        Expression[] args = new Expression[]{new MockExpression(leftList.iterator()), new MockExpression(rightList.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testReduceSelfContext() {
        NodePointer pointer = new MockNodePointer(5);
        SelfContext selfContext = new SelfContext(new MockEvalContext(), pointer);
        Expression[] args = new Expression[]{new MockExpression(selfContext), new MockExpression(3)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare > 0; // 5 > 3 => compare 1 > 0 true
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testReduceCollection() {
        Collection<Integer> collection = new ArrayList<Integer>();
        collection.add(2);
        Expression[] args = new Expression[]{new MockExpression(collection), new MockExpression(2)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testInitialContextResetLeft() {
        MockInitialContext initialContext = new MockInitialContext();
        Expression[] args = new Expression[]{new MockExpression(initialContext), new MockExpression(1)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        op.computeValue(null);
        Assert.assertTrue(initialContext.isResetCalled());
    }

    @Test
    public void testInitialContextResetRight() {
        MockInitialContext initialContext = new MockInitialContext();
        Expression[] args = new Expression[]{new MockExpression(1), new MockExpression(initialContext)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        op.computeValue(null);
        Assert.assertTrue(initialContext.isResetCalled());
    }

    @Test
    public void testComputeValueWithNullLeft() {
        Expression[] args = new Expression[]{new MockExpression(null), new MockExpression(0)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        // InfoSetUtil.doubleValue(null) returns 0.0, so compare 0 == 0 -> true
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueWithNullRight() {
        Expression[] args = new Expression[]{new MockExpression(0), new MockExpression(null)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueEmptyLeftIterator() {
        List<Integer> empty = new ArrayList<Integer>();
        Expression[] args = new Expression[]{new MockExpression(empty.iterator()), new MockExpression(1)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueEmptyRightIterator() {
        List<Integer> empty = new ArrayList<Integer>();
        Expression[] args = new Expression[]{new MockExpression(1), new MockExpression(empty.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothEmptyIterators() {
        List<Integer> empty1 = new ArrayList<Integer>();
        List<Integer> empty2 = new ArrayList<Integer>();
        Expression[] args = new Expression[]{new MockExpression(empty1.iterator()), new MockExpression(empty2.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, true);
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValueLeftIteratorMultipleMatches() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(2);
        Expression[] args = new Expression[]{new MockExpression(list.iterator()), new MockExpression(2)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueBothIteratorsMultipleMatches() {
        List<Integer> leftList = new ArrayList<Integer>();
        leftList.add(1);
        leftList.add(2);
        leftList.add(3);
        List<Integer> rightList = new ArrayList<Integer>();
        rightList.add(2);
        rightList.add(2);
        Expression[] args = new Expression[]{new MockExpression(leftList.iterator()), new MockExpression(rightList.iterator())};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueWithCompareGreaterThan() {
        Expression[] args = new Expression[]{new MockExpression(5), new MockExpression(3)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare > 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueWithCompareLessThan() {
        Expression[] args = new Expression[]{new MockExpression(3), new MockExpression(5)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare < 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueWithCompareEqual() {
        Expression[] args = new Expression[]{new MockExpression(5), new MockExpression(5)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare == 0;
            }
        };
        Assert.assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValueWithCompareNotEqual() {
        Expression[] args = new Expression[]{new MockExpression(5), new MockExpression(5)};
        CoreOperationRelationalExpression op = new TestRelationalExpression(args, false) {
            @Override
            protected boolean evaluateCompare(int compare) {
                return compare != 0;
            }
        };
        Assert.assertEquals(Boolean.FALSE, op.computeValue(null));
    }
}
