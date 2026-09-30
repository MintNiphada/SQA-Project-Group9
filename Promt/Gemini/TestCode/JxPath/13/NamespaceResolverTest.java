package org.apache.commons.jxpath.ri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class NamespaceResolverTest {

    private static class StubNodeIterator implements NodeIterator {
        private final List<NodePointer> list;
        private int position = 0;

        public StubNodeIterator(List<NodePointer> list) {
            this.list = list;
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

    private static class StubNodePointer extends NodePointer {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String namespaceURI;
        private final Map<String, String> namespaces = new HashMap<String, String>();
        private final List<NodePointer> nsPointers = new ArrayList<NodePointer>();
        private boolean returnNullIterator = false;

        public StubNodePointer(String name, String namespaceURI) {
            super(null, Locale.getDefault());
            this.name = name;
            this.namespaceURI = namespaceURI;
        }

        public void addNamespace(String prefix, String uri) {
            namespaces.put(prefix, uri);
            nsPointers.add(new StubNodePointer(prefix, uri));
        }

        public void setReturnNullIterator(boolean returnNullIterator) {
            this.returnNullIterator = returnNullIterator;
        }

        public QName getName() {
            return new QName(name);
        }

        public String getNamespaceURI() {
            return namespaceURI;
        }

        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }

        public NodeIterator namespaceIterator() {
            if (returnNullIterator) {
                return null;
            }
            return new StubNodeIterator(nsPointers);
        }

        public Object getBaseValue() {
            return null;
        }

        public Object getImmediateNode() {
            return null;
        }

        public int getLength() {
            return 1;
        }

        public boolean isCollection() {
            return false;
        }

        public boolean isLeaf() {
            return true;
        }

        public boolean isActual() {
            return true;
        }

        public void setValue(Object value) {
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
            return null;
        }

        public NodeIterator attributeIterator(QName qname) {
            return null;
        }
    }

    @Test
    public void testDefaultConstructor() {
        NamespaceResolver resolver = new NamespaceResolver();
        Assert.assertNull(resolver.parent);
        Assert.assertFalse(resolver.isSealed());
        Assert.assertNull(resolver.getNamespaceContextPointer());
        Assert.assertNull(resolver.getNamespaceURI("xml"));
    }

    @Test
    public void testParentConstructor() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        Assert.assertSame(parent, child.parent);
    }

    @Test
    public void testRegisterAndGetNamespaceURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("ns", "http://example.com/ns");
        resolver.registerNamespace("foo", "http://example.com/foo");

        Assert.assertEquals("http://example.com/ns", resolver.getNamespaceURI("ns"));
        Assert.assertEquals("http://example.com/foo", resolver.getNamespaceURI("foo"));
        Assert.assertNull(resolver.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceURIWithPointerFallback() {
        NamespaceResolver resolver = new NamespaceResolver();
        StubNodePointer pointer = new StubNodePointer("root", "http://example.com/root");
        pointer.addNamespace("pointerNs", "http://example.com/pointer");
        resolver.setNamespaceContextPointer(pointer);

        Assert.assertEquals("http://example.com/pointer", resolver.getNamespaceURI("pointerNs"));

        // Explicit registration takes precedence over pointer
        resolver.registerNamespace("pointerNs", "http://example.com/override");
        Assert.assertEquals("http://example.com/override", resolver.getNamespaceURI("pointerNs"));
    }

    @Test
    public void testGetNamespaceURIWithParentFallback() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentNs", "http://example.com/parent");

        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("childNs", "http://example.com/child");

        Assert.assertEquals("http://example.com/child", child.getNamespaceURI("childNs"));
        Assert.assertEquals("http://example.com/parent", child.getNamespaceURI("parentNs"));
        Assert.assertNull(child.getNamespaceURI("nonExistent"));
    }

    @Test
    public void testGetNamespaceURIHierarchyWithPointer() {
        NamespaceResolver grandParent = new NamespaceResolver();
        grandParent.registerNamespace("gp", "http://example.com/gp");

        NamespaceResolver parent = new NamespaceResolver(grandParent);
        StubNodePointer parentPointer = new StubNodePointer("parent", "http://example.com/p");
        parentPointer.addNamespace("fromParentPointer", "http://example.com/parentPointer");
        parent.setNamespaceContextPointer(parentPointer);

        NamespaceResolver child = new NamespaceResolver(parent);

        Assert.assertEquals("http://example.com/gp", child.getNamespaceURI("gp"));
        Assert.assertEquals("http://example.com/parentPointer", child.getNamespaceURI("fromParentPointer"));
        Assert.assertNull(child.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceContextPointer() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);

        Assert.assertNull(child.getNamespaceContextPointer());

        StubNodePointer parentPointer = new StubNodePointer("parent", "http://parent");
        parent.setNamespaceContextPointer(parentPointer);
        Assert.assertSame(parentPointer, child.getNamespaceContextPointer());

        StubNodePointer childPointer = new StubNodePointer("child", "http://child");
        child.setNamespaceContextPointer(childPointer);
        Assert.assertSame(childPointer, child.getNamespaceContextPointer());
    }

    @Test
    public void testGetPrefixFromRegisteredMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        StubNodePointer pointer = new StubNodePointer("root", "http://example.com/root");
        resolver.setNamespaceContextPointer(pointer);

        resolver.registerNamespace("ns1", "http://example.com/1");
        resolver.registerNamespace("ns2", "http://example.com/2");

        Assert.assertEquals("ns1", resolver.getPrefix("http://example.com/1"));
        Assert.assertEquals("ns2", resolver.getPrefix("http://example.com/2"));
        Assert.assertNull(resolver.getPrefix("http://example.com/unknown"));
    }

    @Test
    public void testGetPrefixFromPointerNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        StubNodePointer pointer = new StubNodePointer("root", "http://example.com/root");
        pointer.addNamespace("pfx1", "http://example.com/pointer1");
        pointer.addNamespace("", "http://example.com/default"); // Empty prefix should not be registered in reverseMap
        resolver.setNamespaceContextPointer(pointer);

        Assert.assertEquals("pfx1", resolver.getPrefix("http://example.com/pointer1"));
        Assert.assertNull(resolver.getPrefix("http://example.com/default"));
    }

    @Test
    public void testGetPrefixFromParentFallback() {
        NamespaceResolver parent = new NamespaceResolver();
        StubNodePointer parentPointer = new StubNodePointer("parent", "http://parent");
        parent.setNamespaceContextPointer(parentPointer);
        parent.registerNamespace("parentPfx", "http://example.com/parent");

        NamespaceResolver child = new NamespaceResolver(parent);
        StubNodePointer childPointer = new StubNodePointer("child", "http://child");
        child.setNamespaceContextPointer(childPointer);
        child.registerNamespace("childPfx", "http://example.com/child");

        Assert.assertEquals("childPfx", child.getPrefix("http://example.com/child"));
        Assert.assertEquals("parentPfx", child.getPrefix("http://example.com/parent"));
        Assert.assertNull(child.getPrefix("http://example.com/none"));
    }

    @Test
    public void testGetPrefixWithNullIterator() {
        NamespaceResolver resolver = new NamespaceResolver();
        StubNodePointer pointer = new StubNodePointer("root", "http://example.com/root");
        pointer.setReturnNullIterator(true);
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("pfx", "http://example.com/uri");

        Assert.assertEquals("pfx", resolver.getPrefix("http://example.com/uri"));
        Assert.assertNull(resolver.getPrefix("http://example.com/unknown"));
    }

    @Test
    public void testReverseMapResetOnRegisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        StubNodePointer pointer = new StubNodePointer("root", "http://example.com/root");
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("a", "http://example.com/a");

        Assert.assertEquals("a", resolver.getPrefix("http://example.com/a"));
        Assert.assertNull(resolver.getPrefix("http://example.com/b"));

        // Registering a new namespace should invalidate reverseMap
        resolver.registerNamespace("b", "http://example.com/b");
        Assert.assertEquals("b", resolver.getPrefix("http://example.com/b"));
    }

    @Test
    public void testSeal() {
        NamespaceResolver grandParent = new NamespaceResolver();
        NamespaceResolver parent = new NamespaceResolver(grandParent);
        NamespaceResolver child = new NamespaceResolver(parent);

        Assert.assertFalse(grandParent.isSealed());
        Assert.assertFalse(parent.isSealed());
        Assert.assertFalse(child.isSealed());

        child.seal();

        Assert.assertTrue(child.isSealed());
        Assert.assertTrue(parent.isSealed());
        Assert.assertTrue(grandParent.isSealed());
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespaceWhenSealedThrowsException() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.registerNamespace("ns", "http://example.com/ns");
    }

    @Test
    public void testClone() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentNs", "http://parent");

        NamespaceResolver resolver = new NamespaceResolver(parent);
        resolver.registerNamespace("ns", "http://example.com/ns");
        StubNodePointer pointer = new StubNodePointer("node", "http://node");
        resolver.setNamespaceContextPointer(pointer);
        resolver.seal();

        Assert.assertTrue(resolver.isSealed());

        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        Assert.assertNotNull(clone);
        Assert.assertNotSame(resolver, clone);
        Assert.assertSame(parent, clone.parent);
        Assert.assertSame(pointer, clone.getNamespaceContextPointer());
        Assert.assertFalse(clone.isSealed());

        // Can register in clone because it is unsealed
        clone.registerNamespace("cloneNs", "http://example.com/clone");
        Assert.assertEquals("http://example.com/clone", clone.getNamespaceURI("cloneNs"));
        Assert.assertNull(resolver.getNamespaceURI("cloneNs"));

        // Original remains sealed
        Assert.assertTrue(resolver.isSealed());
    }
}
