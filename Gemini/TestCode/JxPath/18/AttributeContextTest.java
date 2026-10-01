package org.apache.commons.jxpath.ri.axes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class AttributeContextTest {

    private static class MockNodeIterator implements NodeIterator {
        private int position = 0;
        private List<NodePointer> list;

        public MockNodeIterator(List<NodePointer> list) {
            this.list = list != null ? list : new ArrayList<NodePointer>();
        }

        public int getPosition() {
            return position;
        }

        public boolean setPosition(int position) {
            this.position = position;
            return position >= 1 && position <= list.size();
        }

        public NodePointer getNodePointer() {
            if (position >= 1 && position <= list.size()) {
                return list.get(position - 1);
            }
            return null;
        }
    }

    private static class MockNodePointer extends NodePointer {
        private NodeIterator customIterator;

        protected MockNodePointer(NodePointer parent, Locale locale) {
            super(parent, locale);
        }

        public void setAttributeIterator(NodeIterator iterator) {
            this.customIterator = iterator;
        }

        public NodeIterator attributeIterator(QName name) {
            return customIterator;
        }

        public QName getName() {
            return new QName("mockNode");
        }

        public Object getBaseValue() {
            return null;
        }

        public Object getImmediateNode() {
            return null;
        }

        public boolean isCollection() {
            return false;
        }

        public int getLength() {
            return 1;
        }

        public boolean isLeaf() {
            return true;
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        public void setValue(Object value) {
        }

        public boolean isActual() {
            return true;
        }
    }

    private static class MockEvalContext extends EvalContext {
        private NodePointer currentNodePointer;

        public MockEvalContext(NodePointer pointer) {
            super(null);
            this.currentNodePointer = pointer;
        }

        public NodePointer getCurrentNodePointer() {
            return currentNodePointer;
        }

        public boolean nextNode() {
            return false;
        }

        public boolean nextSet() {
            return false;
        }
    }

    @Test
    public void testInitialState() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);
        Assert.assertNull("Current node pointer should initially be null", context.getCurrentNodePointer());
        Assert.assertEquals("Current position should initially be 0", 0, context.getCurrentPosition());
    }

    @Test
    public void testNextNodeWithNonNodeNameTest() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);

        AttributeContext context = new AttributeContext(parentContext, nodeTest);
        boolean hasNext = context.nextNode();
        Assert.assertFalse("nextNode should return false when nodeTest is not NodeNameTest", hasNext);
        Assert.assertNull("Current node pointer should remain null", context.getCurrentNodePointer());
        Assert.assertEquals("Position should increment to 1", 1, context.getCurrentPosition());

        // Subsequent call when setStarted is already true
        boolean hasNextAgain = context.nextNode();
        Assert.assertFalse("Subsequent nextNode should also return false", hasNextAgain);
        Assert.assertEquals("Position should increment to 2", 2, context.getCurrentPosition());
    }

    @Test
    public void testNextNodeWithNullNodeTest() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);

        AttributeContext context = new AttributeContext(parentContext, null);
        boolean hasNext = context.nextNode();
        Assert.assertFalse("nextNode should return false for null nodeTest", hasNext);
        Assert.assertNull(context.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeWithNullIterator() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        parentPointer.setAttributeIterator(null);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);
        boolean hasNext = context.nextNode();
        Assert.assertFalse("nextNode should return false when attributeIterator is null", hasNext);
        Assert.assertNull(context.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeWithEmptyIterator() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodeIterator emptyIterator = new MockNodeIterator(new ArrayList<NodePointer>());
        parentPointer.setAttributeIterator(emptyIterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);
        boolean hasNext = context.nextNode();
        Assert.assertFalse("nextNode should return false when iterator has no elements", hasNext);
        Assert.assertNull(context.getCurrentNodePointer());
    }

    @Test
    public void testSuccessfulIteration() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodePointer attr2 = new MockNodePointer(parentPointer, Locale.US);
        MockNodeIterator iterator = new MockNodeIterator(Arrays.asList(attr1, attr2));
        parentPointer.setAttributeIterator(iterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        Assert.assertTrue("First nextNode should return true", context.nextNode());
        Assert.assertEquals(1, context.getCurrentPosition());
        Assert.assertSame("Should point to first attribute", attr1, context.getCurrentNodePointer());

        Assert.assertTrue("Second nextNode should return true", context.nextNode());
        Assert.assertEquals(2, context.getCurrentPosition());
        Assert.assertSame("Should point to second attribute", attr2, context.getCurrentNodePointer());

        Assert.assertFalse("Third nextNode should return false", context.nextNode());
        Assert.assertEquals(3, context.getCurrentPosition());
    }

    @Test
    public void testResetAndReiteration() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodeIterator iterator = new MockNodeIterator(Arrays.asList(attr1));
        parentPointer.setAttributeIterator(iterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        Assert.assertTrue(context.nextNode());
        Assert.assertSame(attr1, context.getCurrentNodePointer());

        context.reset();
        Assert.assertEquals("Position should be reset to 0", 0, context.getCurrentPosition());

        // Re-create iterator for parentPointer since reset clears iterator in AttributeContext
        MockNodeIterator newIterator = new MockNodeIterator(Arrays.asList(attr1));
        parentPointer.setAttributeIterator(newIterator);

        Assert.assertTrue("Should be able to iterate again after reset", context.nextNode());
        Assert.assertSame(attr1, context.getCurrentNodePointer());
    }

    @Test
    public void testSetPositionForward() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodePointer attr2 = new MockNodePointer(parentPointer, Locale.US);
        MockNodePointer attr3 = new MockNodePointer(parentPointer, Locale.US);
        MockNodeIterator iterator = new MockNodeIterator(Arrays.asList(attr1, attr2, attr3));
        parentPointer.setAttributeIterator(iterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        boolean moved = context.setPosition(2);
        Assert.assertTrue("setPosition(2) should succeed", moved);
        Assert.assertEquals(2, context.getCurrentPosition());
        Assert.assertSame("Should point to attr2", attr2, context.getCurrentNodePointer());
    }

    @Test
    public void testSetPositionBackward() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodePointer attr2 = new MockNodePointer(parentPointer, Locale.US);
        parentPointer.setAttributeIterator(new MockNodeIterator(Arrays.asList(attr1, attr2)));
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        Assert.assertTrue(context.setPosition(2));
        Assert.assertSame(attr2, context.getCurrentNodePointer());

        // Reset backing iterator for parentPointer because backward setPosition will call reset()
        parentPointer.setAttributeIterator(new MockNodeIterator(Arrays.asList(attr1, attr2)));

        boolean moved = context.setPosition(1);
        Assert.assertTrue("setPosition(1) backwards should succeed", moved);
        Assert.assertEquals(1, context.getCurrentPosition());
        Assert.assertSame("Should point to attr1", attr1, context.getCurrentNodePointer());
    }

    @Test
    public void testSetPositionPastEnd() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodeIterator iterator = new MockNodeIterator(Arrays.asList(attr1));
        parentPointer.setAttributeIterator(iterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        boolean moved = context.setPosition(5);
        Assert.assertFalse("setPosition beyond available elements should return false", moved);
    }

    @Test
    public void testSetPositionToCurrentPosition() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        MockNodeIterator iterator = new MockNodeIterator(Arrays.asList(attr1));
        parentPointer.setAttributeIterator(iterator);
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        Assert.assertTrue(context.setPosition(1));
        Assert.assertTrue("Setting to same position should return true immediately", context.setPosition(1));
        Assert.assertEquals(1, context.getCurrentPosition());
    }

    @Test
    public void testSetPositionToZeroOrNegative() {
        MockNodePointer parentPointer = new MockNodePointer(null, Locale.US);
        MockNodePointer attr1 = new MockNodePointer(parentPointer, Locale.US);
        parentPointer.setAttributeIterator(new MockNodeIterator(Arrays.asList(attr1)));
        MockEvalContext parentContext = new MockEvalContext(parentPointer);
        NodeNameTest nameTest = new NodeNameTest(new QName("attr"));

        AttributeContext context = new AttributeContext(parentContext, nameTest);

        Assert.assertTrue(context.setPosition(1));
        Assert.assertEquals(1, context.getCurrentPosition());

        // Setting to 0 triggers reset and loop doesn't run since getCurrentPosition() (0) is not < 0
        boolean result = context.setPosition(0);
        Assert.assertTrue("setPosition(0) returns true", result);
        Assert.assertEquals(0, context.getCurrentPosition());
    }
}
