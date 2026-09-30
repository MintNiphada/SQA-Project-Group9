package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

public class DOMNodePointerTest {

    private Document doc;
    private Element root;
    private DOMNodePointer rootPointer;
    private DOMNodePointer elementPointer;
    private DOMNodePointer textPointer;
    private DOMNodePointer commentPointer;
    private DOMNodePointer piPointer;
    private DOMNodePointer cdataPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        doc = factory.newDocumentBuilder().newDocument();
        root = doc.createElementNS("http://example.com/ns", "root");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        doc.appendChild(root);
        Element child = doc.createElementNS("http://example.com/ns", "ns:child");
        child.setAttribute("attr", "value");
        root.appendChild(child);
        Text text = doc.createTextNode("some text");
        root.appendChild(text);
        Comment comment = doc.createComment("comment");
        root.appendChild(comment);
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        root.appendChild(pi);
        CDATASection cdata = doc.createCDATASection("cdata content");
        root.appendChild(cdata);

        rootPointer = new DOMNodePointer(doc, Locale.US);
        elementPointer = new DOMNodePointer(child, Locale.US);
        textPointer = new DOMNodePointer(text, Locale.US);
        commentPointer = new DOMNodePointer(comment, Locale.US);
        piPointer = new DOMNodePointer(pi, Locale.US);
        cdataPointer = new DOMNodePointer(cdata, Locale.US);
    }

    @Test
    public void testConstructors() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        assertNotNull(p1);
        assertEquals(root, p1.getBaseValue());

        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US, "myId");
        assertNotNull(p2);
        assertEquals(root, p2.getBaseValue());

        DOMNodePointer parent = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p3 = new DOMNodePointer(parent, root);
        assertNotNull(p3);
        assertEquals(root, p3.getBaseValue());
        assertEquals(parent, p3.getParent());
    }

    @Test
    public void testTestNodeNullTest() {
        assertTrue(DOMNodePointer.testNode(root, null));
        assertTrue(rootPointer.testNode(null));
    }

    @Test
    public void testTestNodeNodeNameTest() {
        // wildcard with no prefix
        NodeNameTest wildcardNoPrefix = new NodeNameTest(new QName(null, "*"), null);
        assertTrue(DOMNodePointer.testNode(root, wildcardNoPrefix));

        // wildcard with prefix
        NodeNameTest wildcardWithPrefix = new NodeNameTest(new QName("ns", "*"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(root, wildcardWithPrefix));

        // exact name match
        NodeNameTest exactName = new NodeNameTest(new QName("ns", "root"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(root, exactName));

        // name mismatch
        NodeNameTest wrongName = new NodeNameTest(new QName("ns", "other"), "http://example.com/ns");
        assertFalse(DOMNodePointer.testNode(root, wrongName));

        // namespace mismatch
        NodeNameTest wrongNS = new NodeNameTest(new QName("ns", "root"), "http://other.com/ns");
        assertFalse(DOMNodePointer.testNode(root, wrongNS));

        // non-element node
        NodeNameTest testOnText = new NodeNameTest(new QName(null, "text"), null);
        assertFalse(DOMNodePointer.testNode(textPointer.getImmediateNode(), testOnText));
    }

    @Test
    public void testTestNodeNodeTypeTest() {
        NodeTypeTest nodeTest = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(root, nodeTest));
        assertFalse(DOMNodePointer.testNode(textPointer.getImmediateNode(), nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textPointer.getImmediateNode(), textTest));
        assertTrue(DOMNodePointer.testNode(cdataPointer.getImmediateNode(), textTest));
        assertFalse(DOMNodePointer.testNode(root, textTest));

        NodeTypeTest commentTest = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(commentPointer.getImmediateNode(), commentTest));
        assertFalse(DOMNodePointer.testNode(root, commentTest));

        NodeTypeTest piTest = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(piPointer.getImmediateNode(), piTest));
        assertFalse(DOMNodePointer.testNode(root, piTest));
    }

    @Test
    public void testTestNodeProcessingInstructionTest() {
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piPointer.getImmediateNode(), piTest));
        ProcessingInstructionTest wrongTarget = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(piPointer.getImmediateNode(), wrongTarget));
        assertFalse(DOMNodePointer.testNode(root, piTest));
    }

    @Test
    public void testEqualStrings() {
        // private method tested indirectly via testNode, but we can test via namespace comparisons
        // We'll trust coverage from testNode tests.
    }

    @Test
    public void testGetName() {
        QName name = elementPointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("child", name.getName());

        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("target", piName.getName());

        QName rootName = rootPointer.getName();
        // root is document node, not element, so getName returns null prefix and null local name? Actually rootPointer points to Document node, not Element. Document node type is DOCUMENT_NODE, not ELEMENT_NODE, so getName will have null ns and null ln.
        assertNull(rootName.getPrefix());
        assertNull(rootName.getName());
    }

    @Test
    public void testGetNamespaceURI() {
        // instance method getNamespaceURI() returns namespace of the node
        assertEquals("http://example.com/ns", elementPointer.getNamespaceURI());
        // rootPointer points to Document, getNamespaceURI() will get document element's namespace
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI());
    }

    @Test
    public void testChildIterator() {
        NodeIterator it = rootPointer.childIterator(null, false, null);
        assertNotNull(it);
        // should have children
        assertTrue(it.setPosition(1));
        NodePointer childPtr = it.getNodePointer();
        assertNotNull(childPtr);
    }

    @Test
    public void testAttributeIterator() {
        NodeIterator it = elementPointer.attributeIterator(new QName(null, "attr"));
        assertNotNull(it);
        assertTrue(it.setPosition(1));
        NodePointer attrPtr = it.getNodePointer();
        assertNotNull(attrPtr);
        assertEquals("value", attrPtr.getValue());
    }

    @Test
    public void testNamespacePointer() {
        NodePointer nsPtr = rootPointer.namespacePointer("ns");
        assertNotNull(nsPtr);
        assertEquals("http://example.com/ns", nsPtr.getValue());
    }

    @Test
    public void testNamespaceIterator() {
        NodeIterator it = rootPointer.namespaceIterator();
        assertNotNull(it);
        // at least one namespace
        assertTrue(it.setPosition(1));
    }

    @Test
    public void testGetNamespaceURIWithPrefix() {
        // test xml prefix
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        // test xmlns prefix
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
        // test known prefix
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("ns"));
        // test unknown prefix
        assertNull(rootPointer.getNamespaceURI("unknown"));
        // test null prefix -> default namespace
        // root has no default namespace, so null
        assertNull(rootPointer.getNamespaceURI(null));
        // test empty prefix -> default namespace
        assertNull(rootPointer.getNamespaceURI(""));
    }

    @Test
    public void testGetDefaultNamespaceURI() {
        // root has no default namespace, so null
        assertNull(rootPointer.getDefaultNamespaceURI());
        // add default namespace to root
        root.setAttribute("xmlns", "http://default.com");
        assertEquals("http://default.com", rootPointer.getDefaultNamespaceURI());
    }

    @Test
    public void testGetBaseValue() {
        assertEquals(root, rootPointer.getBaseValue());
    }

    @Test
    public void testGetImmediateNode() {
        assertEquals(root, rootPointer.getImmediateNode());
    }

    @Test
    public void testIsActual() {
        assertTrue(rootPointer.isActual());
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
        assertFalse(rootPointer.isLeaf());
        assertTrue(textPointer.isLeaf());
    }

    @Test
    public void testIsLanguage() {
        // no xml:lang set
        assertFalse(rootPointer.isLanguage("en"));
        // set xml:lang on root
        root.setAttribute("xml:lang", "en-US");
        assertTrue(rootPointer.isLanguage("en"));
        assertFalse(rootPointer.isLanguage("fr"));
    }

    @Test
    public void testGetLanguage() {
        assertNull(rootPointer.getLanguage());
        root.setAttribute("xml:lang", "en");
        assertEquals("en", rootPointer.getLanguage());
    }

    @Test
    public void testSetValueOnTextNode() {
        textPointer.setValue("new text");
        assertEquals("new text", textPointer.getValue());
        // set empty string should remove node
        textPointer.setValue("");
        // after removal, parent should not have that text node
        assertNull(textPointer.getImmediateNode().getParentNode());
    }

    @Test
    public void testSetValueOnElement() {
        // clear children and set string
        elementPointer.setValue("hello");
        assertEquals("hello", elementPointer.getValue());
        // set a node
        Element newElem = doc.createElement("newChild");
        elementPointer.setValue(newElem);
        assertEquals("newChild", ((Node) elementPointer.getImmediateNode()).getNodeName());
        // set a document
        Document newDoc = null;
        try {
            newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }
        Element docElem = newDoc.createElement("docChild");
        newDoc.appendChild(docElem);
        elementPointer.setValue(newDoc);
        assertEquals("docChild", ((Node) elementPointer.getImmediateNode()).getFirstChild().getNodeName());
    }

    @Test
    public void testCreateChild() {
        JXPathContext context = JXPathContext.newContext(null);
        context.setFactory(new TestAbstractFactory());
        NodePointer childPtr = elementPointer.createChild(context, new QName(null, "newChild"), 0);
        assertNotNull(childPtr);
        assertEquals("newChild", ((Node) childPtr.getImmediateNode()).getNodeName());
    }

    @Test
    public void testCreateChildWithValue() {
        JXPathContext context = JXPathContext.newContext(null);
        context.setFactory(new TestAbstractFactory());
        NodePointer childPtr = elementPointer.createChild(context, new QName(null, "newChild"), 0, "value");
        assertNotNull(childPtr);
        assertEquals("value", childPtr.getValue());
    }

    @Test
    public void testCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(null);
        NodePointer attrPtr = elementPointer.createAttribute(context, new QName(null, "newAttr"));
        assertNotNull(attrPtr);
        assertEquals("", attrPtr.getValue());
        // attribute should exist
        assertTrue(((Element) elementPointer.getImmediateNode()).hasAttribute("newAttr"));
    }

    @Test(expected = JXPathException.class)
    public void testRemoveRoot() {
        rootPointer.remove();
    }

    @Test
    public void testRemoveChild() {
        elementPointer.remove();
        assertNull(elementPointer.getImmediateNode().getParentNode());
    }

    @Test
    public void testAsPathWithId() {
        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", idPointer.asPath());
    }

    @Test
    public void testAsPathElement() {
        // root is document, so asPath returns empty? Actually document node returns empty string.
        assertEquals("", rootPointer.asPath());
        // element pointer
        String path = elementPointer.asPath();
        assertTrue(path.contains("child"));
    }

    @Test
    public void testAsPathText() {
        String path = textPointer.asPath();
        assertTrue(path.contains("text()"));
    }

    @Test
    public void testAsPathPI() {
        String path = piPointer.asPath();
        assertTrue(path.contains("processing-instruction('target')"));
    }

    @Test
    public void testEscape() {
        // private method, tested via asPath with id containing quotes
        DOMNodePointer p = new DOMNodePointer(root, Locale.US, "it's \"quoted\"");
        String path = p.asPath();
        assertTrue(path.contains("&apos;"));
        assertTrue(path.contains("&quot;"));
    }

    @Test
    public void testHashCode() {
        assertEquals(System.identityHashCode(root), rootPointer.hashCode());
    }

    @Test
    public void testEquals() {
        assertTrue(rootPointer.equals(rootPointer));
        DOMNodePointer other = new DOMNodePointer(root, Locale.US);
        assertTrue(rootPointer.equals(other));
        assertFalse(rootPointer.equals(elementPointer));
        assertFalse(rootPointer.equals(null));
        assertFalse(rootPointer.equals("string"));
    }

    @Test
    public void testStaticGetPrefix() {
        assertEquals("ns", DOMNodePointer.getPrefix(elementPointer.getImmediateNode()));
        Element noPrefixElem = doc.createElement("noPrefix");
        assertNull(DOMNodePointer.getPrefix(noPrefixElem));
    }

    @Test
    public void testStaticGetLocalName() {
        assertEquals("child", DOMNodePointer.getLocalName(elementPointer.getImmediateNode()));
        Element noPrefixElem = doc.createElement("noPrefix");
        assertEquals("noPrefix", DOMNodePointer.getLocalName(noPrefixElem));
    }

    @Test
    public void testStaticGetNamespaceURI() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(elementPointer.getImmediateNode()));
        // test with document node
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(doc));
    }

    @Test
    public void testGetValue() {
        assertEquals("some text", textPointer.getValue());
        assertEquals("comment", commentPointer.getValue());
        assertEquals("data", piPointer.getValue());
        assertEquals("cdata content", cdataPointer.getValue());
        // element value concatenates text children
        assertEquals("some textcdata content", elementPointer.getValue().replaceAll("\\s+", ""));
    }

    @Test
    public void testGetPointerByID() {
        // set id attribute on an element
        Element elem = doc.createElement("div");
        elem.setAttribute("id", "myId");
        root.appendChild(elem);
        JXPathContext context = JXPathContext.newContext(null);
        Pointer ptr = rootPointer.getPointerByID(context, "myId");
        assertNotNull(ptr);
        assertTrue(ptr instanceof DOMNodePointer);
        assertEquals(elem, ((DOMNodePointer) ptr).getImmediateNode());
        // non-existent id
        Pointer nullPtr = rootPointer.getPointerByID(context, "nonexistent");
        assertTrue(nullPtr instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        NodePointer child1 = new DOMNodePointer(root.getFirstChild(), Locale.US);
        NodePointer child2 = new DOMNodePointer(root.getLastChild(), Locale.US);
        int result = rootPointer.compareChildNodePointers(child1, child2);
        assertTrue(result < 0); // child1 before child2
        result = rootPointer.compareChildNodePointers(child2, child1);
        assertTrue(result > 0);
        result = rootPointer.compareChildNodePointers(child1, child1);
        assertEquals(0, result);
    }

    // Helper factory for createChild tests
    static class TestAbstractFactory extends org.apache.commons.jxpath.AbstractFactory {
        @Override
        public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            if (parent instanceof Node) {
                Node parentNode = (Node) parent;
                Document doc = parentNode.getOwnerDocument();
                if (doc == null) {
                    doc = (Document) parentNode;
                }
                Element newElem = doc.createElement(name);
                parentNode.appendChild(newElem);
                return true;
            }
            return false;
        }
    }
}
