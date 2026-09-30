package org.apache.commons.jxpath.ri.model;

import java.util.Collections;
import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

public class NodePointerTest {

    private static class ConcreteNodePointer extends NodePointer {
        private static final long serialVersionUID = 1L;
        private QName name;
        private Object value;
        private boolean leaf = false;
        private boolean collection = false;
        private int length = 1;
        private boolean container = false;
        private NodePointer immediateValuePointer;
        private String namespaceURI;
        private String defaultNamespaceURI;

        public ConcreteNodePointer(NodePointer parent) {
            super(parent);
        }

        public ConcreteNodePointer(NodePointer parent, Locale locale) {
            super(parent, locale);
        }

        public ConcreteNodePointer(NodePointer parent, QName name, Object value) {
            super(parent);
            this.name = name;
            this.value = value;
        }

        public void setName(QName name) {
            this.name = name;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }

        public void setCollection(boolean collection) {
            this.collection = collection;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setContainer(boolean container) {
            this.container = container;
        }

        public void setImmediateValuePointer(NodePointer immediateValuePointer) {
            this.immediateValuePointer = immediateValuePointer;
        }

        public void setCustomNamespaceURI(String uri) {
            this.namespaceURI = uri;
        }

        public void setDefaultNamespaceURI(String defaultNamespaceURI) {
            this.defaultNamespaceURI = defaultNamespaceURI;
        }

        @Override
        public boolean isLeaf() {
            return leaf;
        }

        @Override
        public boolean isCollection() {
            return collection;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public boolean isContainer() {
            return container;
        }

        @Override
        public QName getName() {
            return name;
        }

        @Override
        public Object getBaseValue() {
            return value;
        }

        @Override
        public Object getImmediateNode() {
            return value;
        }

        @Override
        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public NodePointer getImmediateValuePointer() {
            if (immediateValuePointer != null) {
                return immediateValuePointer;
            }
            return super.getImmediateValuePointer();
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            if (pointer1 == pointer2) {
                return 0;
            }
            String n1 = pointer1.getName() != null ? pointer1.getName().toString() : "";
            String n2 = pointer2.getName() != null ? pointer2.getName().toString() : "";
            int res = n1.compareTo(n2);
            if (res != 0) {
                return res;
            }
            return Integer.compare(pointer1.getIndex(), pointer2.getIndex());
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if ("testPrefix".equals(prefix)) {
                return "http://test";
            }
            if ("otherPrefix".equals(prefix)) {
                return "http://test";
            }
            if ("diffPrefix".equals(prefix)) {
                return "http://diff";
            }
            return namespaceURI;
        }

        @Override
        public String getNamespaceURI() {
            return namespaceURI;
        }

        @Override
        protected String getDefaultNamespaceURI() {
            return defaultNamespaceURI;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ConcreteNodePointer)) return false;
            ConcreteNodePointer other = (ConcreteNodePointer) obj;
            if (name == null ? other.name != null : !name.equals(other.name)) return false;
            return value == null ? other.value == null : value.equals(other.value);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    @Test
    public void testNewNodePointer() {
        QName qname = new QName("test");
        NodePointer npNull = NodePointer.newNodePointer(qname, null, Locale.US);
        Assert.assertNotNull(npNull);
        Assert.assertTrue(npNull instanceof NullPointer);
        Assert.assertEquals(Locale.US, npNull.getLocale());

        NodePointer npObject = NodePointer.newNodePointer(qname, "hello", Locale.GERMANY);
        Assert.assertNotNull(npObject);
        Assert.assertEquals(Locale.GERMANY, npObject.getLocale());
    }

    @Test
    public void testNewChildNodePointer() {
        QName rootName = new QName("root");
        NodePointer root = NodePointer.newNodePointer(rootName, new Object(), Locale.ENGLISH);
        QName childName = new QName("child");
        NodePointer child = NodePointer.newChildNodePointer(root, childName, "childValue");
        Assert.assertNotNull(child);
        Assert.assertEquals(root, child.getParent());
    }

    @Test
    public void testNamespaceResolverHierarchy() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, Locale.ENGLISH);
        NamespaceResolver nr = new NamespaceResolver();
        root.setNamespaceResolver(nr);
        Assert.assertSame(nr, root.getNamespaceResolver());

        ConcreteNodePointer child = new ConcreteNodePointer(root);
        Assert.assertSame(nr, child.getNamespaceResolver());

        NamespaceResolver childNr = new NamespaceResolver();
        child.setNamespaceResolver(childNr);
        Assert.assertSame(childNr, child.getNamespaceResolver());
    }

    @Test
    public void testGetParentAndImmediateParent() {
        ConcreteNodePointer root = new ConcreteNodePointer(null);
        ConcreteNodePointer container1 = new ConcreteNodePointer(root);
        container1.setContainer(true);
        ConcreteNodePointer container2 = new ConcreteNodePointer(container1);
        container2.setContainer(true);
        ConcreteNodePointer leaf = new ConcreteNodePointer(container2);

        Assert.assertSame(container2, leaf.getImmediateParentPointer());
        Assert.assertSame(root, leaf.getParent());
        Assert.assertTrue(root.isRoot());
        Assert.assertFalse(leaf.isRoot());
    }

    @Test
    public void testAttributeFlag() {
        ConcreteNodePointer np = new ConcreteNodePointer(null);
        Assert.assertFalse(np.isAttribute());
        np.setAttribute(true);
        Assert.assertTrue(np.isAttribute());
        np.setAttribute(false);
        Assert.assertFalse(np.isAttribute());
    }

    @Test
    public void testIsNodeAndIsContainer() {
        ConcreteNodePointer np = new ConcreteNodePointer(null);
        Assert.assertFalse(np.isContainer());
        Assert.assertTrue(np.isNode());

        np.setContainer(true);
        Assert.assertTrue(np.isContainer());
        Assert.assertFalse(np.isNode());
    }

    @Test
    public void testIndexAndIsActual() {
        ConcreteNodePointer np = new ConcreteNodePointer(null);
        Assert.assertEquals(NodePointer.WHOLE_COLLECTION, np.getIndex());
        Assert.assertTrue(np.isActual());

        np.setLength(3);
        np.setIndex(0);
        Assert.assertEquals(0, np.getIndex());
        Assert.assertTrue(np.isActual());

        np.setIndex(2);
        Assert.assertTrue(np.isActual());

        np.setIndex(3);
        Assert.assertFalse(np.isActual());

        np.setIndex(-1);
        Assert.assertFalse(np.isActual());

        np.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertTrue(np.isActual());
    }

    @Test
    public void testGetValueAndValuePointerRecursion() {
        ConcreteNodePointer inner = new ConcreteNodePointer(null, new QName("inner"), "innerValue");
        ConcreteNodePointer middle = new ConcreteNodePointer(null, new QName("middle"), "middleValue");
        middle.setImmediateValuePointer(inner);

        ConcreteNodePointer outer = new ConcreteNodePointer(null, new QName("outer"), "outerValue");
        outer.setImmediateValuePointer(middle);

        Assert.assertSame(inner, outer.getValuePointer());
        Assert.assertEquals("innerValue", outer.getValue());
        Assert.assertEquals("middleValue", middle.getBaseValue());
        Assert.assertEquals("innerValue", outer.getNode());
        Assert.assertEquals("innerValue", outer.getNodeValue());
    }

    @Test
    public void testGetRootNode() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, new QName("root"), "rootValue");
        ConcreteNodePointer child = new ConcreteNodePointer(root, new QName("child"), "childValue");
        ConcreteNodePointer grandChild = new ConcreteNodePointer(child, new QName("grandChild"), "grandChildValue");

        Assert.assertEquals("rootValue", grandChild.getRootNode());
        Assert.assertEquals("rootValue", root.getRootNode());
    }

    @Test
    public void testTestNode() {
        ConcreteNodePointer np = new ConcreteNodePointer(null, new QName("testPrefix", "localName"), "val");

        // null test
        Assert.assertTrue(np.testNode(null));

        // NodeNameTest matching
        NodeNameTest matchTest = new NodeNameTest(new QName("testPrefix", "localName"));
        Assert.assertTrue(np.testNode(matchTest));

        // NodeNameTest matching with different prefix but same namespace URI
        NodeNameTest aliasPrefixTest = new NodeNameTest(new QName("otherPrefix", "localName"));
        Assert.assertTrue(np.testNode(aliasPrefixTest));

        // NodeNameTest different namespace URI
        NodeNameTest diffNsTest = new NodeNameTest(new QName("diffPrefix", "localName"));
        Assert.assertFalse(np.testNode(diffNsTest));

        // NodeNameTest wildcard matching namespace
        NodeNameTest wildcardTest = new NodeNameTest(new QName("testPrefix", "*"));
        Assert.assertTrue(np.testNode(wildcardTest));

        // NodeNameTest different local name
        NodeNameTest diffNameTest = new NodeNameTest(new QName("testPrefix", "otherName"));
        Assert.assertFalse(np.testNode(diffNameTest));

        // NodeNameTest on container node
        np.setContainer(true);
        Assert.assertFalse(np.testNode(matchTest));
        np.setContainer(false);

        // NodeNameTest when node's getName() is null
        ConcreteNodePointer nullNamePointer = new ConcreteNodePointer(null, null, "val");
        Assert.assertFalse(nullNamePointer.testNode(matchTest));

        // NodeTypeTest
        NodeTypeTest nodeTypeNodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertTrue(np.testNode(nodeTypeNodeTest));

        np.setContainer(true);
        Assert.assertFalse(np.testNode(nodeTypeNodeTest));
        np.setContainer(false);

        NodeTypeTest nodeTypeTextTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        Assert.assertFalse(np.testNode(nodeTypeTextTest));
    }

    @Test
    public void testCreatePathAndRemove() {
        JXPathContext context = JXPathContext.newContext(new Object());
        ConcreteNodePointer np = new ConcreteNodePointer(null, new QName("item"), "oldValue");

        NodePointer createdPath = np.createPath(context, "newValue");
        Assert.assertSame(np, createdPath);
        Assert.assertEquals("newValue", np.getImmediateNode());

        Assert.assertSame(np, np.createPath(context));
        np.remove(); // No-op, should not throw
    }

    @Test(expected = JXPathException.class)
    public void testCreateChildWithValueThrows() {
        JXPathContext context = JXPathContext.newContext(new Object());
        ConcreteNodePointer np = new ConcreteNodePointer(null, new QName("item"), "val");
        np.createChild(context, new QName("child"), 0, "val");
    }

    @Test(expected = JXPathException.class)
    public void testCreateChildWithoutValueThrows() {
        JXPathContext context = JXPathContext.newContext(new Object());
        ConcreteNodePointer np = new ConcreteNodePointer(null, new QName("item"), "val");
        np.createChild(context, new QName("child"), 0);
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeThrows() {
        JXPathContext context = JXPathContext.newContext(new Object());
        ConcreteNodePointer np = new ConcreteNodePointer(null, new QName("item"), "val");
        np.createAttribute(context, new QName("attr"));
    }

    @Test
    public void testLocaleAndLanguage() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, Locale.CANADA_FRENCH);
        ConcreteNodePointer child = new ConcreteNodePointer(root);

        Assert.assertEquals(Locale.CANADA_FRENCH, child.getLocale());
        Assert.assertTrue(child.isLanguage("fr"));
        Assert.assertTrue(child.isLanguage("FR-CA"));
        Assert.assertFalse(child.isLanguage("en"));
    }

    @Test
    public void testIteratorsAndNamespaces() {
        ConcreteNodePointer np = new ConcreteNodePointer(null);
        Assert.assertNull(np.childIterator(null, false, null));
        Assert.assertNull(np.attributeIterator(new QName("test")));
        Assert.assertNull(np.namespaceIterator());
        Assert.assertNull(np.namespacePointer("ns"));
        Assert.assertNull(np.getNamespaceURI());
        Assert.assertNull(np.getNamespaceURI("prefix"));

        Assert.assertTrue(np.isDefaultNamespace(null));

        np.setDefaultNamespaceURI("http://default");
        np.setCustomNamespaceURI("http://default");
        Assert.assertTrue(np.isDefaultNamespace("any"));

        np.setCustomNamespaceURI("http://other");
        Assert.assertFalse(np.isDefaultNamespace("any"));
    }

    @Test
    public void testPointerByIDAndKey() {
        JXPathContext context = JXPathContext.newContext(Collections.singletonMap("k1", "v1"));
        ConcreteNodePointer np = new ConcreteNodePointer(null);
        Pointer pId = np.getPointerByID(context, "id1");
        Assert.assertNotNull(pId);
        Pointer pKey = np.getPointerByKey(context, "k1", "v1");
        Assert.assertNotNull(pKey);
    }

    @Test
    public void testAsPathAndToString() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, new QName("root"), "r");
        Assert.assertEquals("/root", root.asPath());
        Assert.assertEquals("/root", root.toString());

        ConcreteNodePointer child = new ConcreteNodePointer(root, new QName("child"), "c");
        Assert.assertEquals("/root/child", child.asPath());

        ConcreteNodePointer attr = new ConcreteNodePointer(child, new QName("attr"), "a");
        attr.setAttribute(true);
        Assert.assertEquals("/root/child/@attr", attr.asPath());

        ConcreteNodePointer col = new ConcreteNodePointer(root, new QName("items"), "col");
        col.setCollection(true);
        col.setLength(5);
        col.setIndex(2);
        Assert.assertEquals("/root/items[3]", col.asPath());

        // Child of container
        ConcreteNodePointer container = new ConcreteNodePointer(root, new QName("cont"), "c");
        container.setContainer(true);
        ConcreteNodePointer itemInCont = new ConcreteNodePointer(container, new QName("item"), "i");
        Assert.assertEquals("/root", itemInCont.asPath());
    }

    @Test
    public void testClone() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, new QName("root"), "r");
        ConcreteNodePointer child = new ConcreteNodePointer(root, new QName("child"), "c");

        ConcreteNodePointer clonedChild = (ConcreteNodePointer) child.clone();
        Assert.assertNotNull(clonedChild);
        Assert.assertNotSame(child, clonedChild);
        Assert.assertNotSame(root, clonedChild.getParent());
        Assert.assertEquals(child.getName(), clonedChild.getName());
    }

    @Test
    public void testCompareTo() {
        ConcreteNodePointer root1 = new ConcreteNodePointer(null, new QName("root1"), "r1");
        ConcreteNodePointer c1 = new ConcreteNodePointer(root1, new QName("a"), "valA");
        ConcreteNodePointer c2 = new ConcreteNodePointer(root1, new QName("b"), "valB");

        Assert.assertTrue(c1.compareTo(c2) < 0);
        Assert.assertTrue(c2.compareTo(c1) > 0);
        Assert.assertEquals(0, c1.compareTo(c1));

        ConcreteNodePointer c1Child = new ConcreteNodePointer(c1, new QName("subA"), "sub");
        Assert.assertTrue(c1Child.compareTo(c2) < 0);
        Assert.assertTrue(c2.compareTo(c1Child) > 0);

        ConcreteNodePointer root2 = new ConcreteNodePointer(null, new QName("root2"), "r2");
        try {
            c1.compareTo(root2);
            Assert.fail("Expected JXPathException when comparing across different trees");
        } catch (JXPathException expected) {
            Assert.assertTrue(expected.getMessage().contains("Cannot compare pointers"));
        }

        Assert.assertEquals(0, root1.compareTo(root1));
    }

    @Test
    public void testPrintPointerChain() {
        ConcreteNodePointer root = new ConcreteNodePointer(null, new QName("root"), "r");
        ConcreteNodePointer child = new ConcreteNodePointer(root, new QName("child"), "c");
        child.printPointerChain();
    }
}
