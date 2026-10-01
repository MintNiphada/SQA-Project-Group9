package org.apache.commons.jxpath.ri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.QName;
import org.junit.Test;
import static org.junit.Assert.*;

public class NamespaceResolverTest {

    // Minimal mocks for NodePointer and NodeIterator

    private static class TestNodePointer extends NodePointer {
        private String namespaceURI;
        private QName qname;
        private NodeIterator namespaceIter;

        public TestNodePointer(String namespaceURI, QName qname, NodeIterator namespaceIter) {
            this.namespaceURI = namespaceURI;
            this.qname = qname;
            this.namespaceIter = namespaceIter;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaceURI;
        }

        @Override
        public QName getName() {
            return qname;
        }

        @Override
        public NodeIterator namespaceIterator() {
            return namespaceIter;
        }

        // Stubs for the remaining abstract methods
        @Override public String asPath() { return ""; }
        @Override public Object getImmediateNode() { return null; }
        @Override public Object getValue() { return null; }
        @Override public void setValue(Object value) { }
        @Override public boolean isLeaf() { return false; }
        @Override public boolean isCollection() { return false; }
        @Override public int getLength() { return 0; }
        @Override public Pointer createPath() { return null; }
        @Override public Pointer createChild() { return null; }
        @Override public void remove() { }
        @Override public Object clone() { return null; }
        @Override public int compareTo(Object o) { return 0; }
        @Override public boolean equals(Object o) { return super.equals(o); }
        @Override public int hashCode() { return super.hashCode(); }
    }

    private static class TestNodeIterator implements NodeIterator {
        private List<NodePointer> nodes;
        private int pos = -1;

        public TestNodeIterator(List<NodePointer> nodes) {
            this.nodes = nodes;
        }

        @Override
        public boolean setPosition(int position) {
            if (position > 0 && position <= nodes.size()) {
                pos = position - 1;
                return true;
            }
            return false;
        }

        @Override
        public NodePointer getNodePointer() {
            if (pos >= 0 && pos < nodes.size()) {
                return nodes.get(pos);
            }
            return null;
        }

        @Override
        public int getPosition() {
            return pos + 1;
        }

        @Override
        public NodeIterator cloneIterator() {
            return new TestNodeIterator(new ArrayList<NodePointer>(nodes));
        }

        // Stubs for other abstract methods if any
        @Override public void reset() { pos = -1; }
    }

    @Test
    public void testDefaultConstructer() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.parent);
        assertFalse(resolver.isSealed());
        assertNull(resolver.getNamespaceContextPointer());
    }

    @Test
    public void testParentConstructer() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        assertSame(parent, child.parent);
    }

    @Test
    public void testRegisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "http://example.com");
        assertEquals("http://example.com", resolver.getNamespaceURI("p"));
        // reverseMap should be null after register
        assertNull(resolver.reverseMap);
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespaceWhenSealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.registerNamespace("x", "y");
    }

    @Test
    public void testSetAndGetNamespaceContextPointer() {
        NamespaceResolver resolver = new NamespaceResolver();
        NodePointer ptr = createSimplePointer();
        resolver.setNamespaceContextPointer(ptr);
        assertSame(ptr, resolver.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceContextPointerWithNullPointerNoParent() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceContextPointerDelegatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NodePointer parentPtr = createSimplePointer();
        parent.setNamespaceContextPointer(parentPtr);
        NamespaceResolver child = new NamespaceResolver(parent);
        // child has no pointer, should return parent's
        assertSame(parentPtr, child.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceContextPointerWhenBothHavePointers() {
        NamespaceResolver parent = new NamespaceResolver();
        NodePointer parentPtr = createSimplePointer();
        parent.setNamespaceContextPointer(parentPtr);
        NamespaceResolver child = new NamespaceResolver(parent);
        NodePointer childPtr = createSimplePointer();
        child.setNamespaceContextPointer(childPtr);
        assertSame(childPtr, child.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceURITromLocalMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "http://ns");
        assertEquals("http://ns", resolver.getNamespaceURI("pre"));
    }

    @Test
    public void testGetNamespaceURINotInLocalMapButFromPointer() {
        NamespaceResolver resolver = new NamespaceResolver();
        NodePointer ptr = new TestNodePointer("http://pointer-ns", new QName(null, "pointer"), null);
        resolver.setNamespaceContextPointer(ptr);
        // prefix not in local map
        assertEquals("http://pointer-ns", resolver.getNamespaceURI("anyPrefix"));
    }

    @Test
    public void testGetNamespaceURINotInLocalMapOrPointerButFromParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "parentUri");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("parentUri", child.getNamespaceURI("p"));
    }

    @Test
    public void testGetNamespaceURINotFoundAnywhere() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceURIWithNullPointerButParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "parentUri");
        NamespaceResolver child = new NamespaceResolver(parent);
        // child has no pointer, parent provides
        assertEquals("parentUri", child.getNamespaceURI("p"));
    }

    @Test
    public void testGetNamespaceURIWithNullPointerAndNoParent() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceURI("any"));
    }

    @Test
    public void testGetNamespaceURIWithNullPrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace(null, "nullKeyUri");
        assertEquals("nullKeyUri", resolver.getNamespaceURI(null));
    }

    @Test
    public void testGetPrefixFromReverseMapBuiltFromLocalMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "http://example");
        // force reverseMap creation by calling getPrefix
        String prefix = resolver.getPrefix("http://example");
        assertEquals("pre", prefix);
        // reverseMap should now be non-null
        assertNotNull(resolver.reverseMap);
    }

    @Test
    public void testGetPrefixFromPointerIteratorNodes() {
        NamespaceResolver resolver = new NamespaceResolver();
        // build a pointer with namespaceIterator that returns nodes
        QName qname1 = new QName(null, "pf1");
        NodePointer node1 = new TestNodePointer("uri1", qname1, null);
        QName qname2 = new QName(null, "pf2");
        NodePointer node2 = new TestNodePointer("uri2", qname2, null);
        List<NodePointer> nodes = new ArrayList<NodePointer>();
        nodes.add(node1);
        nodes.add(node2);
        NodeIterator iter = new TestNodeIterator(nodes);
        NodePointer ptr = new TestNodePointer("unused", new QName(null, "unused"), iter);
        resolver.setNamespaceContextPointer(ptr);
        // get prefix for uri2 should return "pf2"
        assertEquals("pf2", resolver.getPrefix("uri2"));
        assertEquals("pf1", resolver.getPrefix("uri1"));
    }

    @Test
    public void testGetPrefixSkipEmptyPrefixFromIterator() {
        NamespaceResolver resolver = new NamespaceResolver();
        QName emptyQName = new QName(null, ""); // empty local name
        NodePointer emptyNode = new TestNodePointer("emptyUri", emptyQName, null);
        QName validQName = new QName(null, "valid");
        NodePointer validNode = new TestNodePointer("validUri", validQName, null);
        List<NodePointer> nodes = new ArrayList<NodePointer>();
        nodes.add(emptyNode);
        nodes.add(validNode);
        NodeIterator iter = new TestNodeIterator(nodes);
        NodePointer ptr = new TestNodePointer("x", new QName(null, "x"), iter);
        resolver.setNamespaceContextPointer(ptr);
        // empty prefix should not be added; validUri should map to "valid"
        assertEquals("valid", resolver.getPrefix("validUri"));
        // emptyUri should not be found (since prefix empty, it was not added)
        assertNull(resolver.getPrefix("emptyUri"));
    }

    @Test
    public void testGetPrefixWhenIteratorIsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        NodePointer ptr = new TestNodePointer("uri", new QName(null, "pre"), null);
        resolver.setNamespaceContextPointer(ptr);
        // register a local entry
        resolver.registerNamespace("local", "localUri");
        // iterator is null, so only local map should be used
        assertEquals("local", resolver.getPrefix("localUri"));
        assertEquals("pre", resolver.getPrefix("uri")); // from pointer
    }

    @Test
    public void testGetPrefixFallsBackToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parent", "parentUri");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("parent", child.getPrefix("parentUri"));
        // child's reverseMap will be built from local (empty) map
        assertNotNull(child.reverseMap);
    }

    @Test
    public void testGetPrefixNotFoundAnywhere() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getPrefix("nonexistent"));
    }

    @Test
    public void testGetPrefixWithNullNamespaceURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", null);
        assertEquals("p", resolver.getPrefix(null));
    }

    @Test
    public void testSealedAndIsSealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertFalse(resolver.isSealed());
        resolver.seal();
        assertTrue(resolver.isSealed());
    }

    @Test
    public void testSealPropagatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        child.seal();
        assertTrue(child.isSealed());
        assertTrue(parent.isSealed());
    }

    @Test
    public void testSealedDoesNotThrowOnGetMethods() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "uri");
        resolver.seal();
        // get methods should still work
        assertEquals("uri", resolver.getNamespaceURI("pre"));
        assertEquals("pre", resolver.getPrefix("uri"));
    }

    @Test
    public void testCloneBasic() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "u");
        NamespaceResolver clone = (NamescapeResolver) resolver.clone();
        assertNotNull(clone);
        assertFalse(clone.isSealed());
        assertEquals("u", clone.getNamespaceURI("p"));
        // original remains unchanged (sealed unchanged, but maps are shared)
        assertFalse(resolver.isSealed());
    }

    @Test
    public void testCloneResetsSealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        NamespaceResolver clone = (NamescapeResolver) resolver.clone();
        assertTrue(resolver.isSealed());
        assertFalse(clone.isSealed());
    }

    @Test
    public void testCloneDoesNotAffectParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        NamespaceResolver clonedChild = (NamescapeResolver) child.clone();
        // cloned child has same parent reference
        assertSame(parent, clonedChild.parent);
        // parent remains unsealed unless other operations
        assertFalse(parent.isSealed());
    }

    @Test
    public void testCloneShareMaps() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "u");
        NamespaceResolver clone = (NamescapeResolver) resolver.clone();
        clone.registerNamespace("p2", "u2");
        // Because map is shallowed copy, original should also be affected
        assertEquals("u2", resolver.getNamespaceURI("p2"));
    }

    // Helper to create a simple pointer with no namespace iterator
    private NodePointer createSimplePointer() {
        return new TestNodePointer("nsUri", new QName(null, "test"), null);
    }
}
