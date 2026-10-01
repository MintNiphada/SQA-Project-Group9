package org.apache.commons.jxpath.ri.axes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

public class AttributeContextTest {

    private EvalContext parentContext;
    private NodeTest nodeTest;
    private AttributeContext attributeContext;
    private NodePointer parentPointer;
    private NodeIterator nodeIterator;
    private NodePointer currentPointer;

    @Before
    public void setUp() {
        parentContext = mock(EvalContext.class);
        nodeTest = mock(NodeTest.class);
        parentPointer = mock(NodePointer.class);
        nodeIterator = mock(NodeIterator.class);
        currentPointer = mock(NodePointer.class);

        when(parentContext.getCurrentNodePointer()).thenReturn(parentPointer);
        
        attributeContext = new AttributeContext(parentContext, nodeTest);
    }

    @Test
    public void testConstructor() {
        assertNotNull(attributeContext);
        // Verify initial state via getters or behavior
        assertNull(attributeContext.getCurrentNodePointer());
    }

    @Test
    public void testGetCurrentNodePointerInitial() {
        assertNull(attributeContext.getCurrentNodePointer());
    }

    @Test
    public void testReset() {
        // Setup some state
        attributeContext.setPosition(1); // This will likely fail or do nothing if not started, but let's try to advance first
        
        // To properly test reset, we need to have advanced the context
        // First, make nextNode work
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        // Replace the mock nodeTest with a real NodeNameTest mock for this specific test flow
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.getNodePointer()).thenReturn(currentPointer);
        
        // Advance
        assertTrue(ctx.nextNode());
        assertSame(currentPointer, ctx.getCurrentNodePointer());
        
        // Reset
        ctx.reset();
        
        assertNull(ctx.getCurrentNodePointer());
        // After reset, position should be 0 (or whatever super.reset does)
        // And setStarted should be false, so next call to nextNode will re-initialize iterator
    }

    @Test
    public void testNextNodeNotNodeNameTest() {
        // nodeTest is a generic NodeTest mock, not NodeNameTest
        assertFalse(attributeContext.nextNode());
        assertNull(attributeContext.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeNullIterator() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(null);
        
        assertFalse(ctx.nextNode());
        assertNull(ctx.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeIteratorPositionFail() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        when(nodeIterator.setPosition(1)).thenReturn(false);
        
        assertFalse(ctx.nextNode());
        assertNull(ctx.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeSuccess() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.getNodePointer()).thenReturn(currentPointer);
        
        assertTrue(ctx.nextNode());
        assertSame(currentPointer, ctx.getCurrentNodePointer());
    }

    @Test
    public void testNextNodeMultipleCalls() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        
        // First call
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.getNodePointer()).thenReturn(currentPointer);
        assertTrue(ctx.nextNode());
        
        // Second call
        NodePointer nextPointer = mock(NodePointer.class);
        when(nodeIterator.setPosition(2)).thenReturn(true);
        when(nodeIterator.getNodePointer()).thenReturn(nextPointer);
        assertTrue(ctx.nextNode());
        assertSame(nextPointer, ctx.getCurrentNodePointer());
        
        // Third call - fail
        when(nodeIterator.setPosition(3)).thenReturn(false);
        assertFalse(ctx.nextNode());
    }

    @Test
    public void testSetPositionForward() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        
        // Mock iterator to succeed for positions 1, 2, 3
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.setPosition(2)).thenReturn(true);
        when(nodeIterator.setPosition(3)).thenReturn(true);
        
        NodePointer p1 = mock(NodePointer.class);
        NodePointer p2 = mock(NodePointer.class);
        NodePointer p3 = mock(NodePointer.class);
        
        when(nodeIterator.getNodePointer()).thenReturn(p1, p2, p3);
        
        assertTrue(ctx.setPosition(3));
        assertSame(p3, ctx.getCurrentNodePointer());
    }

    @Test
    public void testSetPositionFail() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        
        // Fail at position 2
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.setPosition(2)).thenReturn(false);
        
        assertFalse(ctx.setPosition(2));
    }

    @Test
    public void testSetPositionBackward() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        
        // Advance to position 3
        when(nodeIterator.setPosition(1)).thenReturn(true);
        when(nodeIterator.setPosition(2)).thenReturn(true);
        when(nodeIterator.setPosition(3)).thenReturn(true);
        
        NodePointer p1 = mock(NodePointer.class);
        NodePointer p2 = mock(NodePointer.class);
        NodePointer p3 = mock(NodePointer.class);
        
        when(nodeIterator.getNodePointer()).thenReturn(p1, p2, p3);
        
        ctx.setPosition(3);
        assertSame(p3, ctx.getCurrentNodePointer());
        
        // Now set position to 1 (backward)
        // This should trigger reset() internally, then advance again
        
        // After reset, iterator is null, setStarted is false.
        // nextNode() will be called.
        // It will re-create iterator (same mock)
        // setPosition(1) on iterator
        
        // We need to ensure the mocks are set up for the second pass
        // Since we use the same nodeIterator mock, we need to be careful with state if it was stateful.
        // But here we just mock return values.
        
        // Reset happens.
        // Then loop: while (getCurrentPosition() < 1)
        // nextNode() called.
        // setStarted becomes true.
        // iterator = parentPointer.attributeIterator(name) -> returns nodeIterator
        // iterator.setPosition(1) -> returns true
        // currentNodePointer = iterator.getNodePointer() -> returns p1
        
        assertTrue(ctx.setPosition(1));
        assertSame(p1, ctx.getCurrentNodePointer());
    }
    
    @Test
    public void testSetPositionSame() {
        NodeNameTest nodeNameTest = mock(NodeNameTest.class);
        QName name = new QName("attr");
        when(nodeNameTest.getNodeName()).thenReturn(name);
        
        AttributeContext ctx = new AttributeContext(parentContext, nodeNameTest);
        
        when(parentPointer.attributeIterator(name)).thenReturn(nodeIterator);
        
        when(nodeIterator.setPosition(1)).thenReturn(true);
        NodePointer p1 = mock(NodePointer.class);
        when(nodeIterator.getNodePointer()).thenReturn(p1);
        
        ctx.setPosition(1);
        
        // Setting to same position should return true and not change state significantly
        // The loop condition is while (getCurrentPosition() < position)
        // If current is 1 and target is 1, loop doesn't run.
        // Returns true.
        
        assertTrue(ctx.setPosition(1));
        assertSame(p1, ctx.getCurrentNodePointer());
    }
}
