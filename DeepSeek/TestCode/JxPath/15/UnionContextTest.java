package org.apache.commons.jxpath.ri.axes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class UnionContextTest {

    // --- Helper stubs ---

    private static class TestNodePointer extends NodePointer {
        private static final long serialVersionUID = 1L;
        private final int id;

        public TestNodePointer(int id) {
            super(null, null);
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestNodePointer)) return false;
            return id == ((TestNodePointer) obj).id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String asPath() {
            return "/" + id;
        }

        @Override
        public Object getImmediateNode() {
            return null;
        }

        @Override
        public int getLength() {
            return 0;
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
        public boolean isActual() {
            return true;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public NodePointer createChild(String name, Object value) {
            return null;
        }

        @Override
        public NodePointer createAttribute(String name, Object value) {
            return null;
        }

        @Override
        public NodePointer createPath(String path) {
            return null;
        }

        @Override
        public NodePointer createPath(String path, Object value) {
            return null;
        }

        @Override
        public NodePointer getImmediateValuePointer() {
            return null;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    private static class TestEvalContext extends EvalContext {
        private final List<NodePointer> pointers;
        private int currentIndex = -1;
        private boolean setStarted = false;
        private boolean setFinished = false;

        public TestEvalContext(List<NodePointer> pointers) {
            super(null, null);
            this.pointers = pointers;
        }

        @Override
        public boolean nextSet() {
            if (setFinished) return false;
            if (!setStarted) {
                setStarted = true;
                currentIndex = -1;
                return true;
            }
            setFinished = true;
            return false;
        }

        @Override
        public boolean nextNode() {
            if (!setStarted || setFinished) return false;
            currentIndex++;
            return currentIndex < pointers.size();
        }

        @Override
        public NodePointer getCurrentNodePointer() {
            if (currentIndex >= 0 && currentIndex < pointers.size()) {
                return pointers.get(currentIndex);
            }
            return null;
        }

        @Override
        public int getDocumentOrder() {
            return 0;
        }

        @Override
        public boolean setPosition(int position) {
            return false;
        }

        @Override
        public NodePointer getCurrentNodePointer(int position) {
            return null;
        }

        @Override
        public void reset() {
        }
    }

    // --- Tests ---

    @Test
    public void testGetDocumentOrder_MoreThanOneContext() {
        EvalContext[] contexts = new EvalContext[] {
            new TestEvalContext(new ArrayList<NodePointer>()),
            new TestEvalContext(new ArrayList<NodePointer>())
        };
        UnionContext union = new UnionContext(null, contexts);
        Assert.assertEquals(1, union.getDocumentOrder());
    }

    @Test
    public void testGetDocumentOrder_OneContext() {
        EvalContext[] contexts = new EvalContext[] {
            new TestEvalContext(new ArrayList<NodePointer>())
        };
        UnionContext union = new UnionContext(null, contexts);
        Assert.assertEquals(0, union.getDocumentOrder());
    }

    @Test
    public void testGetDocumentOrder_ZeroContexts() {
        EvalContext[] contexts = new EvalContext[0];
        UnionContext union = new UnionContext(null, contexts);
        Assert.assertEquals(0, union.getDocumentOrder());
    }

    @Test
    public void testSetPosition_NotPrepared_CollectsNodes() {
        List<NodePointer> list1 = new ArrayList<NodePointer>();
        list1.add(new TestNodePointer(1));
        list1.add(new TestNodePointer(2));
        List<NodePointer> list2 = new ArrayList<NodePointer>();
        list2.add(new TestNodePointer(2)); // duplicate
        list2.add(new TestNodePointer(3));

        EvalContext ctx1 = new TestEvalContext(list1);
        EvalContext ctx2 = new TestEvalContext(list2);
        EvalContext[] contexts = new EvalContext[] { ctx1, ctx2 };

        UnionContext union = new UnionContext(null, contexts);
        boolean result = union.setPosition(1);
        Assert.assertTrue(result);

        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        List<NodePointer> pointers = nodeSet.getPointers();
        Assert.assertEquals(3, pointers.size());
        Assert.assertEquals(1, ((TestNodePointer) pointers.get(0)).id);
        Assert.assertEquals(2, ((TestNodePointer) pointers.get(1)).id);
        Assert.assertEquals(3, ((TestNodePointer) pointers.get(2)).id);
    }

    @Test
    public void testSetPosition_AlreadyPrepared_NoReCollection() {
        final List<NodePointer> list1 = new ArrayList<NodePointer>();
        list1.add(new TestNodePointer(1));
        EvalContext ctx1 = new TestEvalContext(list1) {
            @Override
            public boolean nextSet() {
                throw new RuntimeException("Should not be called again");
            }
        };
        EvalContext[] contexts = new EvalContext[] { ctx1 };

        UnionContext union = new UnionContext(null, contexts);
        union.setPosition(1); // first call prepares
        // second call should not trigger collection
        boolean result = union.setPosition(1);
        Assert.assertTrue(result);
    }

    @Test
    public void testSetPosition_EmptyContexts() {
        EvalContext[] contexts = new EvalContext[0];
        UnionContext union = new UnionContext(null, contexts);
        boolean result = union.setPosition(1);
        Assert.assertFalse(result);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPosition_NullContextsArray() {
        UnionContext union = new UnionContext(null, null);
        union.setPosition(1);
    }

    @Test
    public void testSetPosition_ContextWithNoNodes() {
        EvalContext ctx = new TestEvalContext(new ArrayList<NodePointer>());
        EvalContext[] contexts = new EvalContext[] { ctx };
        UnionContext union = new UnionContext(null, contexts);
        boolean result = union.setPosition(1);
        Assert.assertFalse(result);
    }

    @Test
    public void testSetPosition_DuplicatePointersRemoved() {
        List<NodePointer> list1 = new ArrayList<NodePointer>();
        list1.add(new TestNodePointer(1));
        List<NodePointer> list2 = new ArrayList<NodePointer>();
        list2.add(new TestNodePointer(1)); // duplicate
        list2.add(new TestNodePointer(2));

        EvalContext ctx1 = new TestEvalContext(list1);
        EvalContext ctx2 = new TestEvalContext(list2);
        EvalContext[] contexts = new EvalContext[] { ctx1, ctx2 };

        UnionContext union = new UnionContext(null, contexts);
        union.setPosition(1);
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        Assert.assertEquals(2, nodeSet.getPointers().size());
    }

    @Test
    public void testSetPosition_ReturnsSuperResult() {
        // super.setPosition returns false for invalid position
        List<NodePointer> list = new ArrayList<NodePointer>();
        list.add(new TestNodePointer(1));
        EvalContext ctx = new TestEvalContext(list);
        EvalContext[] contexts = new EvalContext[] { ctx };
        UnionContext union = new UnionContext(null, contexts);
        boolean result = union.setPosition(2); // position out of bounds
        Assert.assertFalse(result);
    }
}
