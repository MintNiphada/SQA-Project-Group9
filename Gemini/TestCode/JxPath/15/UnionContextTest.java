package org.apache.commons.jxpath.ri.axes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.junit.Assert;
import org.junit.Test;

public class UnionContextTest {

    private static class MockEvalContext extends EvalContext {
        private final List<List<NodePointer>> sets;
        private int setIndex = -1;
        private int nodeIndex = -1;
        private final int docOrder;

        public MockEvalContext(EvalContext parentContext, List<List<NodePointer>> sets) {
            this(parentContext, sets, 0);
        }

        public MockEvalContext(EvalContext parentContext, List<List<NodePointer>> sets, int docOrder) {
            super(parentContext);
            this.sets = sets != null ? sets : Collections.<List<NodePointer>>emptyList();
            this.docOrder = docOrder;
        }

        @Override
        public NodePointer getCurrentNodePointer() {
            if (setIndex >= 0 && setIndex < sets.size()) {
                List<NodePointer> currentSet = sets.get(setIndex);
                if (nodeIndex >= 0 && nodeIndex < currentSet.size()) {
                    return currentSet.get(nodeIndex);
                }
            }
            return null;
        }

        @Override
        public boolean nextNode() {
            if (setIndex >= 0 && setIndex < sets.size()) {
                List<NodePointer> currentSet = sets.get(setIndex);
                if (nodeIndex + 1 < currentSet.size()) {
                    nodeIndex++;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean nextSet() {
            if (setIndex + 1 < sets.size()) {
                setIndex++;
                nodeIndex = -1;
                return true;
            }
            return false;
        }

        @Override
        public int getDocumentOrder() {
            return docOrder;
        }

        @Override
        public boolean setPosition(int position) {
            return false;
        }
    }

    private static class TestPointer extends NullPointer {
        private final String id;

        public TestPointer(String id) {
            super(Locale.getDefault());
            this.id = id;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof TestPointer) {
                return id.equals(((TestPointer) object).id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "TestPointer[" + id + "]";
        }
    }

    @Test
    public void testGetDocumentOrder() {
        RootContext rootContext = new RootContext(
            new JXPathContextReferenceImpl(null, new Object(), null),
            new NullPointer(Locale.getDefault())
        );

        // Empty contexts array -> length is 0 (not > 1) -> delegates to super.getDocumentOrder()
        UnionContext uc0 = new UnionContext(rootContext, new EvalContext[0]);
        Assert.assertEquals(0, uc0.getDocumentOrder());

        // Single context in array -> length is 1 (not > 1) -> delegates to super.getDocumentOrder()
        EvalContext mock1 = new MockEvalContext(rootContext, null, 0);
        UnionContext uc1 = new UnionContext(rootContext, new EvalContext[] { mock1 });
        Assert.assertEquals(0, uc1.getDocumentOrder());

        // Multiple contexts in array -> length > 1 -> returns 1
        EvalContext mock2 = new MockEvalContext(rootContext, null, 0);
        UnionContext uc2 = new UnionContext(rootContext, new EvalContext[] { mock1, mock2 });
        Assert.assertEquals(1, uc2.getDocumentOrder());

        // Three contexts
        UnionContext uc3 = new UnionContext(rootContext, new EvalContext[] { mock1, mock2, mock1 });
        Assert.assertEquals(1, uc3.getDocumentOrder());
    }

    @Test
    public void testSetPositionEmptyContexts() {
        RootContext rootContext = new RootContext(
            new JXPathContextReferenceImpl(null, new Object(), null),
            new NullPointer(Locale.getDefault())
        );

        UnionContext uc = new UnionContext(rootContext, new EvalContext[0]);
        
        // Position 0 is valid for empty context
        Assert.assertTrue(uc.setPosition(0));
        Assert.assertEquals(0, uc.getPosition());
        Assert.assertNull(uc.getCurrentNodePointer());

        // Position 1 should fail because no elements
        Assert.assertFalse(uc.setPosition(1));
        // Calling setPosition again verifies prepared branch is not re-entered
        Assert.assertFalse(uc.setPosition(2));
    }

    @Test
    public void testSetPositionAndDeduplication() {
        RootContext rootContext = new RootContext(
            new JXPathContextReferenceImpl(null, new Object(), null),
            new NullPointer(Locale.getDefault())
        );

        NodePointer ptrA = new TestPointer("A");
        NodePointer ptrB = new TestPointer("B");
        NodePointer ptrC = new TestPointer("C");
        NodePointer ptrA2 = new TestPointer("A"); // Duplicate of A

        List<List<NodePointer>> sets1 = new ArrayList<List<NodePointer>>();
        List<NodePointer> set1_1 = new ArrayList<NodePointer>();
        set1_1.add(ptrA);
        set1_1.add(ptrB);
        sets1.add(set1_1);

        List<List<NodePointer>> sets2 = new ArrayList<List<NodePointer>>();
        List<NodePointer> set2_1 = new ArrayList<NodePointer>();
        set2_1.add(ptrA2); // Duplicate of ptrA, should be omitted
        set2_1.add(ptrC);
        sets2.add(set2_1);

        EvalContext ctx1 = new MockEvalContext(rootContext, sets1);
        EvalContext ctx2 = new MockEvalContext(rootContext, sets2);

        UnionContext uc = new UnionContext(rootContext, new EvalContext[] { ctx1, ctx2 });

        // First call prepares and sets position
        Assert.assertTrue(uc.setPosition(1));
        Assert.assertEquals(1, uc.getPosition());
        Assert.assertSame(ptrA, uc.getCurrentNodePointer());

        Assert.assertTrue(uc.setPosition(2));
        Assert.assertEquals(2, uc.getPosition());
        Assert.assertSame(ptrB, uc.getCurrentNodePointer());

        Assert.assertTrue(uc.setPosition(3));
        Assert.assertEquals(3, uc.getPosition());
        Assert.assertSame(ptrC, uc.getCurrentNodePointer());

        // Out of bounds
        Assert.assertFalse(uc.setPosition(4));
        Assert.assertFalse(uc.setPosition(10));
        
        // Negative position
        Assert.assertFalse(uc.setPosition(-1));

        // Navigate back to valid position
        Assert.assertTrue(uc.setPosition(1));
        Assert.assertSame(ptrA, uc.getCurrentNodePointer());
    }

    @Test
    public void testMultipleSetsWithinContext() {
        RootContext rootContext = new RootContext(
            new JXPathContextReferenceImpl(null, new Object(), null),
            new NullPointer(Locale.getDefault())
        );

        NodePointer ptr1 = new TestPointer("1");
        NodePointer ptr2 = new TestPointer("2");
        NodePointer ptr3 = new TestPointer("3");

        List<List<NodePointer>> sets = new ArrayList<List<NodePointer>>();
        
        List<NodePointer> set1 = new ArrayList<NodePointer>();
        set1.add(ptr1);
        sets.add(set1);

        List<NodePointer> set2 = new ArrayList<NodePointer>();
        set2.add(ptr2);
        set2.add(ptr3);
        sets.add(set2);

        EvalContext ctx = new MockEvalContext(rootContext, sets);
        UnionContext uc = new UnionContext(rootContext, new EvalContext[] { ctx });

        Assert.assertTrue(uc.setPosition(1));
        Assert.assertSame(ptr1, uc.getCurrentNodePointer());

        Assert.assertTrue(uc.setPosition(2));
        Assert.assertSame(ptr2, uc.getCurrentNodePointer());

        Assert.assertTrue(uc.setPosition(3));
        Assert.assertSame(ptr3, uc.getCurrentNodePointer());

        NodeSet nodeSet = uc.getNodeSet();
        Assert.assertNotNull(nodeSet);
        Assert.assertEquals(3, nodeSet.getPointers().size());
        Assert.assertTrue(nodeSet.getPointers().contains(ptr1));
        Assert.assertTrue(nodeSet.getPointers().contains(ptr2));
        Assert.assertTrue(nodeSet.getPointers().contains(ptr3));
    }

    @Test
    public void testUnionContextWithJXPath() {
        class Bean {
            private final String[] list1 = new String[] { "foo", "bar" };
            private final String[] list2 = new String[] { "bar", "baz" };

            public String[] getList1() {
                return list1;
            }

            public String[] getList2() {
                return list2;
            }
        }

        JXPathContext context = JXPathContext.newContext(new Bean());
        List<?> results = context.selectNodes("list1 | list2");
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("foo", results.get(0));
        Assert.assertEquals("bar", results.get(1));
        Assert.assertEquals("baz", results.get(2));
    }

    @Test(expected = NullPointerException.class)
    public void testNullContextsThrowsNpeOnSetPosition() {
        RootContext rootContext = new RootContext(
            new JXPathContextReferenceImpl(null, new Object(), null),
            new NullPointer(Locale.getDefault())
        );
        UnionContext uc = new UnionContext(rootContext, null);
        uc.setPosition(1);
    }
}
