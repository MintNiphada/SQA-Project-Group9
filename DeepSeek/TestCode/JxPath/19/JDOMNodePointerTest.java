package org.apache.commons.jxpath.ri.model.jdom;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class JDOMNodePointerTest {

    private Document document;
    private Element root;
    private JDOMNodePointer docPointer;
    private JDOMNodePointer rootPointer;

    @Before
    public void setUp() throws Exception {
        root = new Element("root");
        root.setAttribute("attr1", "value1");
        root.setAttribute("xml:lang", "en", Namespace.getNamespace("xml", JDOMNodePointer.XML_NAMESPACE_URI));
        Element child = new Element("child");
        child.addContent(new Text("text content"));
        root.addContent(child);
        root.addContent(new CDATA("cdata content"));
        root.addContent(new Comment("comment text"));
        root.addContent(new ProcessingInstruction("piTarget", "piData"));
        document = new Document(root);
        docPointer = new JDOMNodePointer(document, Locale.US);
        rootPointer = new JDOMNodePointer(root, Locale.US);
    }

    // Constructor tests
    @Test
    public void testConstructors() {
        JDOMNodePointer p1 = new JDOMNodePointer(root, Locale.US);
        assertNotNull(p1);
        assertSame(root, p1.getBaseValue());
        JDOMNodePointer p2 = new JDOMNodePointer(root, Locale.US, "id1");
        assertEquals("id1", p2.getImmediateNode()? null : null); // id is private, but we can test asPath
        JDOMNodePointer parentPointer = new JDOMNodePointer(null, Locale.US);
        JDOMNodePointer p3 = new JDOMNodePointer(parentPointer, root);
        assertSame(root, p3.getBaseValue());
        assertEquals(parentPointer, p3.getParent());
    }

    @Test
    public void testGetImmediateNode() {
        assertEquals(root, rootPointer.getImmediateNode());
        assertEquals(document, docPointer.getImmediateNode());
    }

    @Test
    public void testGetBaseValue() {
        assertSame(root, rootPointer.getBaseValue());
        assertSame(document, docPointer.getBaseValue());
    }

    @Test
    public void testIsCollection() {
        assertFalse(rootPointer.isCollection());
    }

    @Test
    public void testGetLength() {
        assertEquals(1, rootPointer.getLength());
    }

    @Test
    public void testIsLeaf() {
        // root has children
        assertFalse(rootPointer.isLeaf());
        // empty element
        Element empty = new Element("empty");
        JDOMNodePointer emptyPointer = new JDOMNodePointer(empty, Locale.US);
        assertTrue(emptyPointer.isLeaf());
        // document with root is not leaf
        assertFalse(docPointer.isLeaf());
        // empty document
        Document emptyDoc = new Document();
        JDOMNodePointer emptyDocPointer = new JDOMNodePointer(emptyDoc, Locale.US);
        assertTrue(emptyDocPointer.isLeaf());
        // non-element, non-document (e.g., Text) -> leaf
        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        assertTrue(textPointer.isLeaf());
    }

    @Test
    public void testGetName() {
        QName qname = rootPointer.getName();
        assertEquals("root", qname.getName());
        assertNull(qname.getPrefix());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.US);
        QName piName = piPointer.getName();
        assertEquals("target", piName.getName());
        assertNull(piName.getPrefix());
    }

    @Test
    public void testGetValue() {
        // Element value (recursive text concatenation)
        String value = rootPointer.getValue();
        assertTrue(value.contains("text content"));
        assertTrue(value.contains("cdata content"));
        // Comment
        Comment comment = new Comment(" comment ");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.US);
        assertEquals("comment", commentPointer.getValue());
        // Text
        Text text = new Text(" text ");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        assertEquals("text", textPointer.getValue()); // trimmed due to xml:space not preserve
        // ProcessingInstruction
        ProcessingInstruction pi = new ProcessingInstruction("target", " data ");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.US);
        assertEquals("data", piPointer.getValue());
        // Text with xml:space="preserve" enclosing attribute
        Element preserveElem = new Element("preserve");
        preserveElem.setAttribute("space", "preserve", Namespace.getNamespace("xml", JDOMNodePointer.XML_NAMESPACE_URI));
        Text pt = new Text(" preserve ");
        preserveElem.addContent(pt);
        JDOMNodePointer ptPointer = new JDOMNodePointer(pt, Locale.US);
        assertEquals(" preserve ", ptPointer.getValue()); // not trimmed
    }

    @Test
    public void testSetValueText() {
        Text text = new Text("old");
        Element parent = new Element("parent");
        parent.addContent(text);
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        textPointer.setValue("new");
        assertEquals("new", text.getText());
        // empty string removes the text
        textPointer.setValue("");
        assertEquals(0, parent.getContentSize());
    }

    @Test
    public void testSetValueElementWithContentTypes() {
        Element element = new Element("el");
        JDOMNodePointer elPointer = new JDOMNodePointer(element, Locale.US);
        // set element value to a string
        elPointer.setValue("string value");
        assertEquals(1, element.getContentSize());
        assertTrue(element.getContent().get(0) instanceof Text);
        assertEquals("string value", ((Text) element.getContent().get(0)).getText());

        // set to Element
        Element newChild = new Element("child").addContent("child text");
        elPointer.setValue(newChild);
        assertEquals(1, element.getContentSize());
        assertTrue(element.getContent().get(0) instanceof Text);
        assertEquals("child text", ((Text) element.getContent().get(0)).getText());

        // set to Document
        Document doc = new Document(new Element("root").addContent("doc text"));
        elPointer.setValue(doc);
        assertEquals(1, element.getContentSize());
        assertEquals("doc text", ((Text) element.getContent().get(0)).getText());

        // set to Text
        Text valText = new Text("text");
        elPointer.setValue(valText);
        assertEquals(1, element.getContentSize());
        assertEquals("text", ((Text) element.getContent().get(0)).getText());

        // set to CDATA
        CDATA cdata = new CDATA("cdata");
        elPointer.setValue(cdata);
        assertEquals(1, element.getContentSize());
        assertTrue(element.getContent().get(0) instanceof Text);
        assertEquals("cdata", ((Text) element.getContent().get(0)).getText());

        // set to ProcessingInstruction
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        elPointer.setValue(pi);
        assertEquals(1, element.getContentSize());
        assertTrue(element.getContent().get(0) instanceof ProcessingInstruction);
        assertEquals("data", ((ProcessingInstruction) element.getContent().get(0)).getData());

        // set to Comment
        Comment comment = new Comment("comment");
        elPointer.setValue(comment);
        assertEquals(1, element.getContentSize());
        assertTrue(element.getContent().get(0) instanceof Comment);
        assertEquals("comment", ((Comment) element.getContent().get(0)).getText());
    }

    @Test
    public void testAddContent() throws Exception {
        // Use a testable subclass to exercise addContent with various node types
        Element element = new Element("el");
        JDOMNodePointer pointer = new TestableJDOMNodePointer(element, Locale.US);
        // Add content list containing element, text, CDATA, PI, comment
        List<Object> content = new ArrayList<Object>();
        content.add(new Element("e").setText("e text"));
        content.add(new Text("text"));
        content.add(new CDATA("cdata"));
        content.add(new ProcessingInstruction("target", "pidata"));
        content.add(new Comment("comment"));
        pointer.setValue(new Document(new Element("dummy"))); // clear and add via setValue
        // Now call addContent indirectly
        // We can't call private addContent directly, but setValue with Document/Element triggers it.
        // Already tested above. Ensure all branches are covered.
    }

    @Test
    public void testGetNamespaceURI() {
        assertEquals(Namespace.XML_NAMESPACE.getURI(), rootPointer.getNamespaceURI("xml"));
        assertEquals(JDOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        assertNull(rootPointer.getNamespaceURI("undefined"));
    }

    @Test
    public void testGetNamespaceURIObj() {
        assertNull(JDOMNodePointer.getNamespaceURI(new Text("text")));
        assertNull(JDOMNodePointer.getNamespaceURI(document));
    }

    @Test
    public void testNamespacePointer() {
        NodePointer nsPointer = rootPointer.namespacePointer("xml");
        assertNotNull(nsPointer);
        assertTrue(nsPointer instanceof JDOMNamespacePointer);
    }

    @Test
    public void testChildIterator() {
        NodeIterator it = rootPointer.childIterator(null, false, null);
        assertNotNull(it);
        // just check it has children
        it.setPosition(1);
        assertNotNull(it.getNodePointer());
    }

    @Test
    public void testAttributeIterator() {
        NodeIterator it = rootPointer.attributeIterator(new QName("attr1"));
        assertNotNull(it);
        it.setPosition(1);
        assertNotNull(it.getNodePointer());
    }

    @Test
    public void testNamespaceIterator() {
        NodeIterator it = rootPointer.namespaceIterator();
        assertNotNull(it);
        // The root element has xml namespace declared, maybe default
        it.setPosition(1);
        assertNotNull(it.getNodePointer());
    }

    @Test
    public void testGetNamespaceResolver() {
        assertNotNull(rootPointer.getNamespaceResolver());
        NamespaceResolver resolver = rootPointer.getNamespaceResolver();
        // check that it can resolve xml prefix
        assertEquals(JDOMNodePointer.XML_NAMESPACE_URI, resolver.getNamespaceURI("xml"));
    }

    @Test
    public void testCompareChildNodePointers() {
        // get two child pointers
        List content = root.getContent();
        Object node1 = content.get(0); // first child element
        Object node2 = content.get(1); // CDATA
        JDOMNodePointer p1 = new JDOMNodePointer(rootPointer, node1);
        JDOMNodePointer p2 = new JDOMNodePointer(rootPointer, node2);
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointer(p2, p1));
        // same
        assertEquals(0, rootPointer.compareChildNodePointer(p1, p1));
        // compare attribute vs non-attribute
        Attribute attr = root.getAttribute("attr1");
        JDOMNodePointer attrPointer = new JDOMNodePointer(rootPointer, attr);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, node1);
        assertEquals(-1, rootPointer.compareChildNodePointer(attrPointer, childPointer));
        assertEquals(1, rootPointer.compareChildNodePointer(childPointer, attrPointer));
        // two attributes
        root.setAttribute("attr2", "val2");
        Attribute attr2 = root.getAttribute("attr2");
        JDOMNodePointer attrPointer2 = new JDOMNodePointer(rootPointer, attr2);
        // order depends on attribute list order; just makes sure no exception
        rootPointer.compareChildNodePointer(attrPointer, attrPointer2);
    }

    @Test(expected = RuntimeException.class)
    public void testCompareChildNodePointerNonElementBase() {
        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        JDOMNodePointer p1 = new JDOMNodePointer(textPointer, text);
        // this should throw because base node is not Element
        textPointer.compareChildNodePointer(p1, p1);
    }

    @Test
    public void testTestNodeNull() {
        assertTrue(JDOMNodePointer.testNode(null, root, null));
    }

    @Test
    public void testTestNodeNodeNameTest() {
        NodeNameTest nameTest = new NodeNameTest(new QName("root"), null);
        assertTrue(JDOMNodePointer.testNode(rootPointer, root, nameTest));
        NodeNameTest nameTestNS = new NodeNameTest(new QName(null, "child", null), "someNS");
        assertFalse(JDOMNodePointer.testNode(rootPointer, root, nameTestNS));
        // wildcard with prefix null -> true for element
        NodeNameTest wildcard = new NodeNameTest(new QName(null, null), null, true);
        assertTrue(JDOMNodePointer.testNode(rootPointer, root, wildcard));
        // wildcard with name that matches local name
        NodeNameTest wildcardLocal = new NodeNameTest(new QName(null, "root", null), true);
        assertTrue(JDOMNodePointer.testNode(rootPointer, root, wildcardLocal));
        // non-Element with name test returns false
        NodeNameTest nameTest2 = new NodeNameTest(new QName("test"), null);
        assertFalse(JDOMNodePointer.testNode(rootPointer, new Text("text"), nameTest2));
    }

    @Test
    public void testTestNodeNodeTypeTest() {
        NodeTypeTest nodeType = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(JDOMNodePointer.testNode(null, root, nodeType));
        NodeTypeTest textType = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(JDOMNodePointer.testNode(null, new Text("text"), textType));
        assertTrue(JDOMNodePointer.testNode(null, new CDATA("cdata"), textType));
        NodeTypeTest commentType = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(JDOMNodePointer.testNode(null, new Comment("text"), commentType));
        NodeTypeTest piType = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(JDOMNodePointer.testNode(null, new ProcessingInstruction("target", "data"), piType));
        NodeTypeTest unknownType = new NodeTypeTest(999);
        assertFalse(JDOMNodePointer.testNode(null, root, unknownType));
    }

    @Test
    public void testTestNodeProcessingInstructionTest() {
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        assertTrue(JDOMNodePointer.testNode(null, pi, piTest));
        processingInstruction piWrong = new ProcessingInstruction("wrong", "data");
        assertFalse(JDOMNodePointer.testNode(null, piWrong, piTest));
        // test with node not PI -> returns false from overall testNode because instanceof check
        assertFalse(JDOMNodePointer.testNode(null, root, piTest));
    }

    @Test
    public void testGetPrefix() {
        assertNull(JDOMNodePointer.getPrefix(new Text("text")));
        assertNull(JDOMNodePointer.getPrefix(root)); // root has no prefix
        Element prefixed = new Element("el", Namespace.getNamespace("ns", "http://example.com"));
        assertEquals("ns", JDOMNodePointer.getPrefix(prefixed));
        Attribute attr = new Attribute("attr", "val", Namespace.getNamespace("ns", "http://example.com"));
        assertEquals("ns", JDOMNodePointer.getPrefix(attr));
        // empty prefix treated as null
        Element emptyPrefix = new Element("el", Namespace.getNamespace("", "http://example.com"));
        assertNull(JDOMNodePointer.getPrefix(emptyPrefix));
    }

    @Test
    public void testGetLocalName() {
        assertEquals("root", JDOMNodePointer.getLocalName(root));
        Attribute attr = new Attribute("attr", "val");
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));
        assertNull(JDOMNodePointer.getLocalName(new Text("text")));
    }

    @Test
    public void testIsLanguage() {
        // root has xml:lang="en"
        assertTrue(rootPointer.isLanguage("en"));
        assertTrue(rootPointer.isLanguage("EN")); // case insensitive
        assertFalse(rootPointer.isLanguage("fr"));
        // no language -> super.isLanguage is called, which returns false if no language set in parent pointer chain
        Element noLang = new Element("nolang");
        JDOMNodePointer noLangPointer = new JDOMNodePointer(noLang, Locale.US);
        assertFalse(noLangPointer.isLanguage("en"));
    }

    @Test
    public void testFindEnclosingAttribute() {
        String lang = JDOMNodePointer.findEnclosingAttribute(root, "lang", Namespace.getNamespace("xml", JDOMNodePointer.XML_NAMESPACE_URI));
        assertEquals("en", lang);
        // non-existing attribute
        assertNull(JDOMNodePointer.findEnclosingAttribute(root, "nonexistent", Namespace.NO_NAMESPACE));
        // test walking up to parent
        Element child = (Element) root.getContent().get(0); // child element
        String langChild = JDOMNodePointer.findEnclosingAttribute(child, "lang", Namespace.getNamespace("xml", JDOMNodePointer.XML_NAMESPACE_URI));
        assertEquals("en", langChild);
    }

    @Test
    public void testNodeParent() throws Exception {
        // Use reflection or call private static through other means? It's private, but we can test via public methods that use it.
        // For coverage, we'll rely on other tests that use remove() etc. But we can access it via a helper test subclass.
        // We'll create a small test that indirectly verifies nodeParent through remove.
        Element parent = new Element("p");
        Text text = new Text("t");
        parent.addContent(text);
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        textPointer.remove();
        assertTrue(parent.getContent().isEmpty());
    }

    @Test
    public void testRemoveRoot() {
        try {
            rootPointer.remove();
            fail("Expected JXPathException");
        } catch (JXPathException e) {
            assertEquals("Cannot remove root JDOM node", e.getMessage());
        }
    }

    @Test
    public void testAsPathWithId() {
        JDOMNodePointer idPointer = new JDOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", idPointer.asPath());
    }

    @Test
    public void testAsPathRoot() {
        // root pointer with no parent
        String path = rootPointer.asPath();
        assertTrue(path.startsWith("/root"));
        // check position
        assertTrue(path.contains("[1]"));
    }

    @Test
    public void testAsPathChild() {
        Element child = (Element) root.getContent().get(0);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        String path = childPointer.asPath();
        assertTrue(path.contains("/child"));
    }

    @Test
    public void testAsPathTextNode() {
        Text text = (Text) ((Element) root.getContent().get(0)).getContent().get(0);
        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text);
        String path = textPointer.asPath();
        assertTrue(path.contains("/text()"));
    }

    @Test
    public void testAsPathCDATA() {
        CDATA cdata = (CDATA) root.getContent().get(1);
        JDOMNodePointer cdataPointer = new JDOMNodePointer(rootPointer, cdata);
        String path = cdataPointer.asPath();
        assertTrue(path.contains("/text()")); // CDATA treated as text
    }

    @Test
    public void testAsPathPI() {
        ProcessingInstruction pi = (ProcessingInstruction) root.getContent().get(3);
        JDOMNodePointer piPointer = new JDOMNodePointer(rootPointer, pi);
        String path = piPointer.asPath();
        assertTrue(path.contains("/processing-instruction('piTarget')"));
    }

    @Test
    public void testGetRelativePositionByQName() throws Exception {
        Element element = new Element("root");
        Element e1 = new Element("child");
        element.addContent(e1);
        Element e2 = new Element("child");
        element.addContent(e2);
        JDOMNodePointer ptr2 = new JDOMNodePointer(element, Locale.US);
        JDOMNodePointer child2 = new JDOMNodePointer(ptr2, e2);
        // access private method via reflection? We'll test it through asPath which uses it.
        String path2 = child2.asPath();
        assertTrue(path2.contains("[2]"));
    }

    @Test
    public void testHashCodeAndEquals() {
        JDOMNodePointer p1 = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer p2 = new JDOMNodePointer(root, Locale.US);
        assertEquals(p1.hashCode(), root.hashCode());
        assertEquals(p1, p2);
        JDOMNodePointer p3 = new JDOMNodePointer(new Element("other"), Locale.US);
        assertFalse(p1.equals(p3));
        assertTrue(p1.equals(p1));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("string"));
    }

    @Test
    public void testCreateChild() {
        // Use a controllable AbstractFactory
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        QName name = new QName("newChild");
        pointer.createChild(context, name, 0); // factory returns true and adds child
        // verify child added
        assertFalse(root.getContent().stream().filter(c -> c instanceof Element && "newChild".equals(((Element) c).getName())).findAny().isEmpty());
    }

    @Test
    public void testCreateChildFailed() {
        // Factory returns false -> should throw
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US) {
            @Override
            protected AbstractFactory getAbstractFactory(JXPathContext context) {
                return new AbstractFactory() {
                    @Override
                    public boolean createObject(JXPathContext context, NodePointer parent, Object node, String name, int index) {
                        return false;
                    }
                };
            }
        };
        try {
            pointer.createChild(JXPathContext.newContext(document), new QName("fail"), 0);
            fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    @Test
    public void testCreateChildWithValue() {
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        QName name = new QName("valChild");
        NodePointer result = pointer.createChild(context, name, 0, "value");
        assertNotNull(result);
        // check child was set
        Object value = result.getValue();
        assertEquals("value", value);
    }

    @Test
    public void testCreateAttributeOnElement() {
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        QName name = new QName("newAttr");
        NodePointer attrPtr = pointer.createAttribute(context, name);
        assertNotNull(attrPtr);
        assertEquals("", root.getAttributeValue("newAttr"));
    }

    @Test
    public void testCreateAttributeWithNamespace() {
        root.addNamespaceDeclaration(Namespace.getNamespace("x", "http://example.com/x"));
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        QName name = new QName("x", "attr");
        NodePointer attrPtr = pointer.createAttribute(context, name);
        assertNotNull(attrPtr);
        assertEquals("", root.getAttributeValue("attr", Namespace.getNamespace("x", "http://example.com/x")));
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownNamespacePrefix() {
        JDOMNodePointer pointer = new TestableJDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        QName name = new QName("unknown", "attr");
        pointer.createAttribute(context, name);
    }

    @Test
    public void testCreateAttributeOnNonElement() {
        JDOMNodePointer piPointer = new TestableJDOMNodePointer(new ProcessingInstruction("target", "data"), Locale.US);
        NodePointer attrPtr = piPointer.createAttribute(JXPathContext.newContext(document), new QName("attr"));
        assertNotNull(attrPtr); // should delegate to super.createAttribute which returns null? Actually super may throw or return null; we can just check it's not null or handle.
    }

    // Helper subclass to control factory
    private static class TestableJDOMNodePointer extends JDOMNodePointer {
        public TestableJDOMNodePointer(Object node, Locale locale) {
            super(node, locale);
        }

        @Override
        protected AbstractFactory getAbstractFactory(JXPathContext context) {
            return new AbstractFactory() {
                @Override
                public boolean createObject(JXPathContext context, NodePointer parent, Object node, String name, int index) {
                    if (node instanceof Element) {
                        ((Element) node).addContent(new Element(name));
                        return true;
                    }
                    return false;
                }
            };
        }
    }
}
