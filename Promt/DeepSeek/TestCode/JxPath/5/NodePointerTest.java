package org.apache.commons.jxpath.ri.model;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

public class NodePointerTest {

    // Concrete implementation for testing abstract NodePointer
    private static class TestNodePointer extends NodePointer {
        private boolean leaf;
        private boolean collection;
        private int length = 1;
        private QName name;
        private Object baseValue;
        private Object immediateNode;
        private NodePointer valuePointer;
        private String namespaceURI;
        private String defaultNamespaceURI;
        private boolean container;

        public TestNodePointer(NodePointer parent) {
            super(parent);
        }

        public TestNodePointer(NodePointer parent, Locale locale) {
            super(parent, locale);
        }

        @Override
        public boolean isLeaf() {
            return leaf;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }

        @Override
        public boolean isCollection() {
            return collection;
        }

        public void setCollection(boolean collection) {
            this.collection = collection;
        }

        @Override
        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Override
        public QName getName() {
            return name;
        }

        public void setName(QName name) {
            this.name = name;
        }

        @Override
        public Object getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        @Override
        public Object getImmediateNode() {
            return immediateNode;
        }

        public void setImmediateNode(Object immediateNode) {
            this.immediateNode = immediateNode;
        }

        @Override
        public void setValue(Object value) {
            // do nothing
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        @Override
        public NodePointer getImmediateValuePointer() {
            return valuePointer != null ? valuePointer : this;
        }

        public void setValuePointer(NodePointer valuePointer) {
            this.valuePointer = valuePointer;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaceURI;
        }

        public void setNamespaceURI(String namespaceURI) {
            this.namespaceURI = namespaceURI;
        }

        @Override
        protected String getDefaultNamespaceURI() {
            return defaultNamespaceURI;
        }

        public void setDefaultNamespaceURI(String defaultNamespaceURI) {
            this.defaultNamespaceURI = defaultNamespaceURI;
        }

        @Override
        public boolean isContainer() {
            return container;
        }

        public void setContainer(boolean container) {
            this.container = container;
        }
    }

    // Minimal JXPathContext for testing delegation methods
    private static class TestJXPathContext extends JXPathContext {
        private Pointer pointerByID;
        private Pointer pointerByKey;

        public TestJXPathContext(Pointer pointerByID, Pointer pointerByKey) {
            this.pointerByID = pointerByID;
            this.pointerByKey = pointerByKey;
        }

        @Override
        public Pointer getPointerByID(String id) {
            return pointerByID;
        }

        @Override
        public Pointer getPointerByKey(String key, String value) {
            return pointerByKey;
        }
    }

    @Test
    public void testNewNodePointerNullBean() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("test"), null, Locale.US);
        Assert.assertNotNull(pointer);
        Assert.assertTrue(pointer instanceof NullPointer);
    }

    @Test(expected = JXPathException.class)
    public void testNewNodePointerNoFactory() {
        NodePointer.newNodePointer(new QName("test"), new Object(), Locale.US);
    }

    @Test(expected = JXPathException.class)
    public void testNewChildNodePointerNoFactory() {
        TestNodePointer parent = new TestNodePointer(null);
        NodePointer.newChildNodePointer(parent, new QName("child"), new Object());
    }

    @Test
    public void testConstructorWithParent() {
        TestNodePointer parent = new TestNodePointer(null);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertSame(parent, child.getImmediateParentPointer());
    }

    @Test
    public void testConstructorWithParentAndLocale() {
        TestNodePointer parent = new TestNodePointer(null);
        Locale locale = Locale.CANADA;
        TestNodePointer child = new TestNodePointer(parent, locale);
        Assert.assertSame(parent, child.getImmediateParentPointer());
        Assert.assertEquals(locale, child.getLocale());
    }

    @Test
    public void testGetNamespaceResolver() {
        TestNodePointer parent = new TestNodePointer(null);
        NamespaceResolver resolver = new NamespaceResolver();
        parent.setNamespaceResolver(resolver);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertSame(resolver, child.getNamespaceResolver());
    }

    @Test
    public void testGetNamespaceResolverWhenNullAndParentNull() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.getNamespaceResolver());
    }

    @Test
    public void testSetNamespaceResolver() {
        TestNodePointer pointer = new TestNodePointer(null);
        NamespaceResolver resolver = new NamespaceResolver();
        pointer.setNamespaceResolver(resolver);
        Assert.assertSame(resolver, pointer.getNamespaceResolver());
    }

    @Test
    public void testGetParentSkipsContainers() {
        TestNodePointer grandParent = new TestNodePointer(null);
        TestNodePointer parent = new TestNodePointer(grandParent);
        parent.setContainer(true);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertSame(grandParent, child.getParent());
    }

    @Test
    public void testGetParentWhenParentNotContainer() {
        TestNodePointer parent = new TestNodePointer(null);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertSame(parent, child.getParent());
    }

    @Test
    public void testGetImmediateParentPointer() {
        TestNodePointer parent = new TestNodePointer(null);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertSame(parent, child.getImmediateParentPointer());
    }

    @Test
    public void testSetAttributeIsAttribute() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertFalse(pointer.isAttribute());
        pointer.setAttribute(true);
        Assert.assertTrue(pointer.isAttribute());
        pointer.setAttribute(false);
        Assert.assertFalse(pointer.isAttribute());
    }

    @Test
    public void testIsRoot() {
        TestNodePointer root = new TestNodePointer(null);
        Assert.assertTrue(root.isRoot());
        TestNodePointer child = new TestNodePointer(root);
        Assert.assertFalse(child.isRoot());
    }

    @Test
    public void testIsNode() {
        TestNodePointer pointer = new TestNodePointer(null);
        // default isContainer false -> isNode true
        Assert.assertTrue(pointer.isNode());
        pointer.setContainer(true);
        Assert.assertFalse(pointer.isNode());
    }

    @Test
    public void testIsContainerDefault() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertFalse(pointer.isContainer());
    }

    @Test
    public void testGetSetIndex() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
        pointer.setIndex(5);
        Assert.assertEquals(5, pointer.getIndex());
    }

    @Test
    public void testGetValueWhenValuePointerIsDifferent() {
        TestNodePointer pointer = new TestNodePointer(null);
        TestNodePointer valuePointer = new TestNodePointer(null);
        valuePointer.setImmediateNode("value");
        pointer.setValuePointer(valuePointer);
        Assert.assertEquals("value", pointer.getValue());
    }

    @Test
    public void testGetValueWhenValuePointerIsThis() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setImmediateNode("node");
        Assert.assertEquals("node", pointer.getValue());
    }

    @Test
    public void testGetValuePointerDefault() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertSame(pointer, pointer.getValuePointer());
    }

    @Test
    public void testGetValuePointerRecursion() {
        TestNodePointer pointer = new TestNodePointer(null);
        TestNodePointer inner = new TestNodePointer(null);
        TestNodePointer innermost = new TestNodePointer(null);
        pointer.setValuePointer(inner);
        inner.setValuePointer(innermost);
        Assert.assertSame(innermost, pointer.getValuePointer());
    }

    @Test
    public void testGetImmediateValuePointerDefault() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertSame(pointer, pointer.getImmediateValuePointer());
    }

    @Test
    public void testIsActualWholeCollection() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertTrue(pointer.isActual());
    }

    @Test
    public void testIsActualIndexInRange() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setLength(3);
        pointer.setIndex(1);
        Assert.assertTrue(pointer.isActual());
    }

    @Test
    public void testIsActualIndexOutOfRange() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setLength(3);
        pointer.setIndex(5);
        Assert.assertFalse(pointer.isActual());
    }

    @Test
    public void testIsActualNegativeIndex() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setLength(3);
        pointer.setIndex(-1);
        Assert.assertFalse(pointer.isActual());
    }

    @Test
    public void testGetNode() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setImmediateNode("node");
        Assert.assertEquals("node", pointer.getNode());
    }

    @Test
    public void testGetRootNodeWhenParentNull() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setImmediateNode("root");
        Assert.assertEquals("root", pointer.getRootNode());
    }

    @Test
    public void testGetRootNodeWhenParentNotNull() {
        TestNodePointer root = new TestNodePointer(null);
        root.setImmediateNode("root");
        TestNodePointer child = new TestNodePointer(root);
        Assert.assertEquals("root", child.getRootNode());
    }

    @Test
    public void testGetRootNodeCached() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setImmediateNode("root");
        Object first = pointer.getRootNode();
        Object second = pointer.getRootNode();
        Assert.assertSame(first, second);
    }

    @Test
    public void testSetValue() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setValue("new value"); // no exception
    }

    @Test
    public void testCompareChildNodePointers() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertEquals(0, pointer.compareChildNodePointers(pointer, pointer));
    }

    @Test
    public void testTestNodeNullTest() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertTrue(pointer.testNode(null));
    }

    @Test
    public void testTestNodeNodeNameTestContainer() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setContainer(true);
        NodeNameTest test = new NodeNameTest(new QName("name"));
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestNullName() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(null);
        NodeNameTest test = new NodeNameTest(new QName("name"));
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestPrefixMismatch() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("prefix", "local"));
        pointer.setNamespaceURI("ns1");
        NodeNameTest test = new NodeNameTest(new QName("otherPrefix", "local"));
        // test prefix "otherPrefix" -> getNamespaceURI returns null by default, so mismatch
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestPrefixMatch() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("pre", "local"));
        pointer.setNamespaceURI("ns");
        NodeNameTest test = new NodeNameTest(new QName("pre", "local"));
        Assert.assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestWildcard() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("pre", "local"));
        NodeNameTest test = new NodeNameTest(new QName("pre", "local"));
        // wildcard true
        test = new NodeNameTest(new QName("pre", "local"), true);
        Assert.assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestLocalNameMismatch() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("pre", "local"));
        NodeNameTest test = new NodeNameTest(new QName("pre", "other"));
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestNode() {
        TestNodePointer pointer = new TestNodePointer(null);
        // isNode true by default
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestNotNode() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setContainer(true); // isNode false
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNodeOtherTest() {
        TestNodePointer pointer = new TestNodePointer(null);
        NodeTest test = new NodeTest() {};
        Assert.assertFalse(pointer.testNode(test));
    }

    @Test
    public void testCreatePathWithValue() {
        TestNodePointer pointer = new TestNodePointer(null);
        NodePointer result = pointer.createPath(null, "value");
        Assert.assertSame(pointer, result);
    }

    @Test
    public void testRemove() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.remove(); // no exception
    }

    @Test
    public void testCreatePathWithoutValue() {
        TestNodePointer pointer = new TestNodePointer(null);
        NodePointer result = pointer.createPath(null);
        Assert.assertSame(pointer, result);
    }

    @Test(expected = JXPathException.class)
    public void testCreateChildWithValue() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.createChild(null, new QName("child"), 0, "value");
    }

    @Test(expected = JXPathException.class)
    public void testCreateChildWithoutValue() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.createChild(null, new QName("child"), 0);
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttribute() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.createAttribute(null, new QName("attr"));
    }

    @Test
    public void testGetLocaleWhenSet() {
        TestNodePointer pointer = new TestNodePointer(null, Locale.ITALY);
        Assert.assertEquals(Locale.ITALY, pointer.getLocale());
    }

    @Test
    public void testGetLocaleFromParent() {
        TestNodePointer parent = new TestNodePointer(null, Locale.FRANCE);
        TestNodePointer child = new TestNodePointer(parent);
        Assert.assertEquals(Locale.FRANCE, child.getLocale());
    }

    @Test
    public void testIsLanguage() {
        TestNodePointer pointer = new TestNodePointer(null, Locale.US);
        Assert.assertTrue(pointer.isLanguage("en"));
        Assert.assertFalse(pointer.isLanguage("fr"));
    }

    @Test
    public void testChildIteratorWhenValuePointerNull() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setValuePointer(null);
        Assert.assertNull(pointer.childIterator(null, false, null));
    }

    @Test
    public void testChildIteratorWhenValuePointerThis() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.childIterator(null, false, null));
    }

    @Test
    public void testChildIteratorDelegates() {
        TestNodePointer pointer = new TestNodePointer(null);
        TestNodePointer valuePointer = new TestNodePointer(null);
        pointer.setValuePointer(valuePointer);
        // valuePointer.childIterator returns null by default
        Assert.assertNull(pointer.childIterator(null, false, null));
    }

    @Test
    public void testAttributeIteratorWhenValuePointerNull() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setValuePointer(null);
        Assert.assertNull(pointer.attributeIterator(new QName("attr")));
    }

    @Test
    public void testAttributeIteratorWhenValuePointerThis() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.attributeIterator(new QName("attr")));
    }

    @Test
    public void testAttributeIteratorDelegates() {
        TestNodePointer pointer = new TestNodePointer(null);
        TestNodePointer valuePointer = new TestNodePointer(null);
        pointer.setValuePointer(valuePointer);
        Assert.assertNull(pointer.attributeIterator(new QName("attr")));
    }

    @Test
    public void testNamespaceIterator() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.namespaceIterator());
    }

    @Test
    public void testNamespacePointer() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.namespacePointer("ns"));
    }

    @Test
    public void testGetNamespaceURI() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIWithPrefix() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.getNamespaceURI("prefix"));
    }

    @Test
    public void testIsDefaultNamespaceNullPrefix() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertTrue(pointer.isDefaultNamespace(null));
    }

    @Test
    public void testIsDefaultNamespaceMatching() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setNamespaceURI("http://default");
        pointer.setDefaultNamespaceURI("http://default");
        Assert.assertTrue(pointer.isDefaultNamespace("pre"));
    }

    @Test
    public void testIsDefaultNamespaceNotMatching() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setNamespaceURI("http://other");
        pointer.setDefaultNamespaceURI("http://default");
        Assert.assertFalse(pointer.isDefaultNamespace("pre"));
    }

    @Test
    public void testGetDefaultNamespaceURI() {
        TestNodePointer pointer = new TestNodePointer(null);
        Assert.assertNull(pointer.getDefaultNamespaceURI());
    }

    @Test
    public void testGetPointerByID() {
        TestNodePointer pointer = new TestNodePointer(null);
        Pointer expected = new TestNodePointer(null);
        JXPathContext context = new TestJXPathContext(expected, null);
        Assert.assertSame(expected, pointer.getPointerByID(context, "id"));
    }

    @Test
    public void testGetPointerByKey() {
        TestNodePointer pointer = new TestNodePointer(null);
        Pointer expected = new TestNodePointer(null);
        JXPathContext context = new TestJXPathContext(null, expected);
        Assert.assertSame(expected, pointer.getPointerByKey(context, "key", "value"));
    }

    @Test
    public void testAsPathRoot() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("root"));
        Assert.assertEquals("/root", pointer.asPath());
    }

    @Test
    public void testAsPathChild() {
        TestNodePointer parent = new TestNodePointer(null);
        parent.setName(new QName("parent"));
        TestNodePointer child = new TestNodePointer(parent);
        child.setName(new QName("child"));
        Assert.assertEquals("/parent/child", child.asPath());
    }

    @Test
    public void testAsPathAttribute() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("attr"));
        pointer.setAttribute(true);
        Assert.assertEquals("/@attr", pointer.asPath());
    }

    @Test
    public void testAsPathIndexedCollection() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("item"));
        pointer.setCollection(true);
        pointer.setIndex(2);
        Assert.assertEquals("/item[3]", pointer.asPath());
    }

    @Test
    public void testAsPathContainerParent() {
        TestNodePointer parent = new TestNodePointer(null);
        parent.setName(new QName("container"));
        parent.setContainer(true);
        TestNodePointer child = new TestNodePointer(parent);
        child.setName(new QName("child"));
        // parent is container, so asPath delegates to parent
        Assert.assertEquals("/container", child.asPath());
    }

    @Test
    public void testClone() {
        TestNodePointer parent = new TestNodePointer(null);
        parent.setName(new QName("parent"));
        TestNodePointer child = new TestNodePointer(parent);
        child.setName(new QName("child"));
        NodePointer cloned = (NodePointer) child.clone();
        Assert.assertNotNull(cloned);
        Assert.assertNotSame(child, cloned);
        Assert.assertNotNull(cloned.getImmediateParentPointer());
        Assert.assertNotSame(parent, cloned.getImmediateParentPointer());
    }

    @Test
    public void testToString() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("test"));
        Assert.assertEquals("/test", pointer.toString());
    }

    @Test
    public void testCompareToSameParent() {
        TestNodePointer parent = new TestNodePointer(null);
        TestNodePointer p1 = new TestNodePointer(parent);
        TestNodePointer p2 = new TestNodePointer(parent);
        // compareChildNodePointers returns 0
        Assert.assertEquals(0, p1.compareTo(p2));
    }

    @Test
    public void testCompareToDifferentDepths() {
        TestNodePointer root = new TestNodePointer(null);
        root.setName(new QName("root"));
        TestNodePointer child1 = new TestNodePointer(root);
        child1.setName(new QName("a"));
        TestNodePointer child2 = new TestNodePointer(root);
        child2.setName(new QName("b"));
        // same parent, compareChildNodePointers returns 0, so compareTo returns 0
        Assert.assertEquals(0, child1.compareTo(child2));
    }

    @Test(expected = JXPathException.class)
    public void testCompareToDifferentTrees() {
        TestNodePointer root1 = new TestNodePointer(null);
        TestNodePointer root2 = new TestNodePointer(null);
        root1.compareTo(root2);
    }

    @Test
    public void testPrintPointerChain() {
        TestNodePointer pointer = new TestNodePointer(null);
        pointer.setName(new QName("test"));
        pointer.printPointerChain(); // no exception
    }
}
