package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodePointerTest {

    private Document doc;
    private Element root;
    private Element childElement;
    private Text textNode;
    private Comment commentNode;
    private ProcessingInstruction piNode;
    private Element namespaceElement;
    private Element langElement;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
        root = doc.createElementNS("http://example.com/ns", "ns:root");
        doc.appendChild(root);
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:prefix", "http://example.com/prefix");
        root.setAttributeNS(null, "id", "rootId");

        childElement = doc.createElementNS("http://example.com/ns", "ns:child");
        root.appendChild(childElement);
        childElement.setAttributeNS(null, "attr", "value");

        textNode = doc.createTextNode("some text");
        root.appendChild(textNode);

        commentNode = doc.createComment("comment data");
        root.appendChild(commentNode);

        piNode = doc.createProcessingInstruction("target", "pi data");
        root.appendChild(piNode);

        namespaceElement = doc.createElementNS("http://example.com/ns", "ns:nsTest");
        root.appendChild(namespaceElement);

        langElement = doc.createElementNS("http://example.com/ns", "ns:lang");
        langElement.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en");
        root.appendChild(langElement);
    }

    @Test
    public void testConstructorNodeLocale() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNotNull(pointer);
        assertEquals(root, pointer.getNode());
    }

    @Test
    public void testConstructorNodeLocaleId() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US, "myId");
        assertNotNull(pointer);
        assertEquals(root, pointer.getNode());
        assertTrue(pointer.asPath().contains("id('myId')"));
    }

    @Test
    public void testConstructorParentNode() {
        DOMNodePointer parentPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, childElement);
        assertEquals(childElement, childPointer.getNode());
        assertEquals(parentPointer, childPointer.getParent());
    }

    @Test
    public void testTestNodeNullTest() {
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    @Test
    public void testTestNodeNodeNameTestElementWildcard() {
        QName name = new QName(null, "any");
        NodeNameTest test = new NodeNameTest(name, null);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeNameTestElementLocalNameMatchWildcardPrefixNull() {
        QName name = new QName(null, "child");
        NodeNameTest test = new NodeNameTest(name, null);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeNameTestElementLocalNameMatchNamespaceMatch() {
        QName name = new QName("ns", "child");
        NodeNameTest test = new NodeNameTest(name, "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeNameTestElementLocalNameMatchNamespaceMismatch() {
        QName name = new QName("ns", "child");
        NodeNameTest test = new NodeNameTest(name, "http://other.com/ns");
        assertFalse(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeNameTestNonElementNode() {
        QName name = new QName(null, "some");
        NodeNameTest test = new NodeNameTest(name, null);
        assertFalse(DOMNodePointer.testNode(textNode, test));
    }

    @Test
    public void testTestNodeNodeNameTestLocalNameMismatch() {
        QName name = new QName(null, "wrong");
        NodeNameTest test = new NodeNameTest(name, null);
        assertFalse(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypeNodeElement() {
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypeNodeDocument() {
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(doc, test));
    }

    @Test
    public void testTestNodeNodeTypeTestTextOnTextNode() {
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, test));
    }

    @Test
    public void testTestNodeNodeTypeTestTextOnCDATA() {
        CDATASection cdata = doc.createCDATASection("cdata");
        root.appendChild(cdata);
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(cdata, test));
    }

    @Test
    public void testTestNodeNodeTypeTestComment() {
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(commentNode, test));
    }

    @Test
    public void testTestNodeNodeTypeTestProcessingInstruction() {
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeNodeTypeTestDefaultCase() {
        NodeTypeTest test = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestMatch() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestMismatch() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("otherTarget");
        assertFalse(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestNonPI() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testGetNameElement() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        QName name = pointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("child", name.getName());
    }

    @Test
    public void testGetNameProcessingInstruction() {
        DOMNodePointer pointer = new DOMNodePointer(piNode, Locale.US);
        QName name = pointer.getName();
        assertEquals("target", name.getName());
    }

    @Test
    public void testGetNameOtherNodeType() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertNull(name.getName());
    }

    @Test
    public void testGetNamespaceURI() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        assertEquals("http://example.com/ns", pointer.getNamespaceURI());
    }

    @Test
    public void testChildIterator() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        NodeIterator it = pointer.childIterator(null, false, null);
        assertNotNull(it);
        assertTrue(it instanceof DOMNodeIterator);
    }

    @Test
    public void testAttributeIterator() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        NodeIterator it = pointer.attributeIterator(new QName("attr"));
        assertNotNull(it);
        assertTrue(it instanceof DOMAttributeIterator);
    }

    @Test
    public void testNamespacePointer() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        NodePointer nsPointer = pointer.namespacePointer("ns");
        assertNotNull(nsPointer);
        assertTrue(nsPointer instanceof NamespacePointer);
    }

    @Test
    public void testNamespaceIterator() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        NodeIterator it = pointer.namespaceIterator();
        assertNotNull(it);
        assertTrue(it instanceof DOMNamespaceIterator);
    }

    @Test
    public void testGetNamespaceURIPrefixNull() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNull(pointer.getNamespaceURI(null));
    }

    @Test
    public void testGetNamespaceURIPrefixEmpty() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNull(pointer.getNamespaceURI(""));
    }

    @Test
    public void testGetNamespaceURIXml() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals("http://www.w3.org/XML/1998/namespace", pointer.getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURIXmlns() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals("http://www.w3.org/2000/xmlns/", pointer.getNamespaceURI("xmlns"));
    }

    @Test
    public void testGetNamespaceURIByPrefix() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals("http://example.com/prefix", pointer.getNamespaceURI("prefix"));
    }

    @Test
    public void testGetNamespaceURIUnknownPrefix() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNull(pointer.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceURICached() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        pointer.getNamespaceURI("prefix"); // caches
        assertEquals("http://example.com/prefix", pointer.getNamespaceURI("prefix"));
    }

    @Test
    public void testGetDefaultNamespaceURI() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURIExists() {
        Element elem = doc.createElementNS(null, "test");
        elem.setAttributeNS(null, "xmlns", "http://default.com");
        root.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.US);
        assertEquals("http://default.com", pointer.getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURIDocument() {
        Document tempDoc = null;
        try {
            tempDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element docElem = tempDoc.createElementNS("http://temp.com", "docElem");
            docElem.setAttributeNS(null, "xmlns", "http://temp.com");
            tempDoc.appendChild(docElem);
            DOMNodePointer pointer = new DOMNodePointer(tempDoc, Locale.US);
            assertEquals("http://temp.com", pointer.getDefaultNamespaceURI());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetBaseValue() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals(root, pointer.getBaseValue());
    }

    @Test
    public void testGetImmediateNode() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals(root, pointer.getImmediateNode());
    }

    @Test
    public void testIsActual() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertTrue(pointer.isActual());
    }

    @Test
    public void testIsCollection() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testGetLength() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals(1, pointer.getLength());
    }

    @Test
    public void testIsLeafTrue() {
        DOMNodePointer pointer = new DOMNodePointer(commentNode, Locale.US);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafFalse() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testIsLanguageCurrentNullDelegatesToSuper() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertFalse(pointer.isLanguage("en"));
    }

    @Test
    public void testIsLanguageMatch() {
        DOMNodePointer pointer = new DOMNodePointer(langElement, Locale.US);
        assertTrue(pointer.isLanguage("en"));
    }

    @Test
    public void testIsLanguageMatchCaseInsensitive() {
        DOMNodePointer pointer = new DOMNodePointer(langElement, Locale.US);
        assertTrue(pointer.isLanguage("EN"));
    }

    @Test
    public void testGetLanguage() {
        DOMNodePointer pointer = new DOMNodePointer(langElement, Locale.US);
        assertEquals("en", pointer.getLanguage());
    }

    @Test
    public void testGetLanguageNoLang() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        assertNull(pointer.getLanguage());
    }

    @Test
    public void testSetValueTextNode() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        pointer.setValue("new text");
        assertEquals("new text", textNode.getNodeValue());
    }

    @Test
    public void testSetValueTextNodeEmptyString() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        pointer.setValue("");
        assertNull(textNode.getParentNode());
    }

    @Test
    public void testSetValueTextNodeNull() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        pointer.setValue(null);
        assertNull(textNode.getParentNode());
    }

    @Test
    public void testSetValueElementWithNodeValue() {
        Element element = doc.createElement("test");
        root.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US);
        Element newElement = doc.createElement("replacement");
        newElement.appendChild(doc.createTextNode("text"));
        pointer.setValue(newElement);
        assertEquals("text", element.getTextContent());
    }

    @Test
    public void testSetValueElementWithDocument() {
        Element element = doc.createElement("test");
        root.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US);
        Document newDoc = null;
        try {
            newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Element newDocElem = newDoc.createElement("child");
        newDoc.appendChild(newDocElem);
        pointer.setValue(newDoc);
        assertEquals("<child/>", element.getTextContent()); // check actual text
    }

    @Test
    public void testSetValueElementWithString() {
        Element element = doc.createElement("test");
        root.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US);
        pointer.setValue("string value");
        assertEquals("string value", element.getTextContent());
    }

    @Test
    public void testCreateChildFactoryFalseThrowsException() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        try {
            pointer.createChild(context, new QName("newChild"), 0);
            fail("Should throw JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    @Test
    public void testCreateChildFactoryTrueNoIteratorReturnsNullFailure() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                // Do nothing, factory says true but no node created
                return true;
            }
        });
        try {
            pointer.createChild(context, new QName("newChild"), 0);
            fail("Should throw JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    @Test
    public void testCreateChildWithIndexWholeCollection() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                // Actually create child element
                ((Element) parent).appendChild(((Element) parent).getOwnerDocument().createElementNS("http://example.com/ns", "ns:newChild"));
                return true;
            }
        });
        NodePointer result = pointer.createChild(context, new QName("ns", "newChild"), DOMNodePointer.WHOLE_COLLECTION);
        assertNotNull(result);
        assertEquals("newChild", DOMNodePointer.getLocalName((Node) result.getBaseValue()));
    }

    @Test
    public void testCreateChildWithValue() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                ((Element) parent).appendChild(((Element) parent).getOwnerDocument().createElementNS("http://example.com/ns", "ns:childWithValue"));
                return true;
            }
        });
        NodePointer result = pointer.createChild(context, new QName("ns", "childWithValue"), 0, "testValue");
        assertNotNull(result);
        assertEquals("testValue", ((Element) result.getBaseValue()).getTextContent());
    }

    @Test
    public void testCreateAttributeOnElement() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer attrPointer = pointer.createAttribute(context, new QName("newAttr"));
        assertNotNull(attrPointer);
        assertTrue(childElement.hasAttribute("newAttr"));
    }

    @Test
    public void testCreateAttributeExistingAttribute() {
        childElement.setAttribute("existing", "old");
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer attrPointer = pointer.createAttribute(context, new QName("existing"));
        assertNotNull(attrPointer);
        assertEquals("old", childElement.getAttribute("existing"));
    }

    @Test
    public void testCreateAttributeWithPrefix() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        context.registerNamespace("pref", "http://example.com/prefix");
        NodePointer attrPointer = pointer.createAttribute(context, new QName("pref", "prefixedAttr"));
        assertNotNull(attrPointer);
        assertTrue(childElement.hasAttributeNS("http://example.com/prefix", "prefixedAttr"));
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownPrefix() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    @Test
    public void testCreateAttributeOnNonElement() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = pointer.createAttribute(context, new QName("any"));
        assertNull(result); // super creates null pointer?
    }

    @Test
    public void testRemoveNode() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        pointer.remove();
        assertNull(childElement.getParentNode());
    }

    @Test(expected = JXPathException.class)
    public void testRemoveRootNode() {
        DOMNodePointer pointer = new DOMNodePointer(doc.createElement("alone"), Locale.US);
        pointer.remove();
    }

    @Test
    public void testAsPathElementDefaultNamespace() {
        DOMNodePointer pointer = new DOMNodePointer(childElement, Locale.US);
        String path = pointer.asPath();
        assertTrue(path.contains("ns:child"));
        assertTrue(path.matches(".*\\[\\d+\\]$"));
    }

    @Test
    public void testAsPathElementNoNamespace() {
        Element elem = doc.createElement("noNS");
        root.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.US);
        String path = pointer.asPath();
        assertTrue(path.contains("noNS["));
    }

    @Test
    public void testAsPathTextNode() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        String path = pointer.asPath();
        assertTrue(path.contains("/text()"));
        assertTrue(path.matches(".*\\[\\d+\\]$"));
    }

    @Test
    public void testAsPathProcessingInstruction() {
        DOMNodePointer pointer = new DOMNodePointer(piNode, Locale.US);
        String path = pointer.asPath();
        assertTrue(path.contains("processing-instruction('target')"));
    }

    @Test
    public void testAsPathDocumentNode() {
        DOMNodePointer pointer = new DOMNodePointer(doc, Locale.US);
        assertEquals("", pointer.asPath());
    }

    @Test
    public void testAsPathWithId() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", pointer.asPath());
    }

    @Test
    public void testAsPathWithIdApos() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US, "it's");
        assertEquals("id('it&apos;s')", pointer.asPath());
    }

    @Test
    public void testAsPathWithIdQuot() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US, "a\"b");
        assertEquals("id('a&quot;b')", pointer.asPath());
    }

    @Test
    public void testAsPathParentNotDOMNodePointer() {
        DOMNodePointer parentPointer = new DummyNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, childElement);
        String path = childPointer.asPath();
        assertFalse(path.contains("/"));
    }

    /**
     * Dummy NodePointer for testing asPath where parent is not DOMNodePointer.
     */
    private static class DummyNodePointer extends NodePointer {
        DummyNodePointer(Node node, Locale locale) {
            super(null, locale);
            setNode(node);
        }
        @Override
        public Object getImmediateNode() { return null; }
        @Override
        public int getLength() { return 0; }
        @Override
        public QName getName() { return null; }
        @Override
        public boolean isLeaf() { return false; }
        @Override
        public boolean isCollection() { return false; }
        @Override
        public String asPath() { return "dummyPath/"; }
    }

    @Test
    public void testGetPointerByIDFound() {
        DOMNodePointer pointer = new DOMNodePointer(doc, Locale.US);
        Pointer result = pointer.getPointerByID(JXPathContext.newContext(new Object()), "rootId");
        assertNotNull(result);
        assertTrue(result instanceof DOMNodePointer);
        assertEquals(root, ((DOMNodePointer) result).getNode());
    }

    @Test
    public void testGetPointerByIDNotFound() {
        DOMNodePointer pointer = new DOMNodePointer(doc, Locale.US);
        Pointer result = pointer.getPointerByID(JXPathContext.newContext(new Object()), "nonexistent");
        assertNotNull(result);
        assertTrue(result instanceof NullPointer);
    }

    @Test
    public void testGetPointerByIDFromElementNode() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        Pointer result = pointer.getPointerByID(JXPathContext.newContext(new Object()), "rootId");
        assertNotNull(result);
        assertEquals(root, ((DOMNodePointer) result).getNode());
    }

    @Test
    public void testCompareChildNodePointersSameNode() {
        DOMNodePointer parent = new DOMNodePointer(root, Locale.US);
        DOMNodePointer child1 = new DOMNodePointer(parent, childElement);
        DOMNodePointer child2 = new DOMNodePointer(parent, childElement);
        assertEquals(0, parent.compareChildNodePointers(child1, child2));
    }

    @Test
    public void testCompareChildNodePointersAttributeFirst() {
        Attr attr = childElement.getAttributeNode("attr");
        DOMNodePointer parent = new DOMNodePointer(childElement, Locale.US);
        DOMNodePointer attrPointer = new DOMNodePointer(parent, attr);
        DOMNodePointer textPointer = new DOMNodePointer(parent, textNode);
        assertEquals(-1, parent.compareChildNodePointers(attrPointer, textPointer));
    }

    @Test
    public void testCompareChildNodePointersTwoAttributes() {
        childElement.setAttributeNS(null, "attr2", "val2");
        Attr attr1 = childElement.getAttributeNode("attr");
        Attr attr2 = childElement.getAttributeNode("attr2");
        DOMNodePointer parent = new DOMNodePointer(childElement, Locale.US);
        DOMNodePointer p1 = new DOMNodePointer(parent, attr1);
        DOMNodePointer p2 = new DOMNodePointer(parent, attr2);
        // attr1 should come before attr2
        assertEquals(-1, parent.compareChildNodePointers(p1, p2));
    }

    @Test
    public void testCompareChildNodePointersAttributesReverseOrder() {
        childElement.setAttributeNS(null, "attr2", "val2");
        Attr attr1 = childElement.getAttributeNode("attr");
        Attr attr2 = childElement.getAttributeNode("attr2");
        DOMNodePointer parent = new DOMNodePointer(childElement, Locale.US);
        DOMNodePointer p1 = new DOMNodePointer(parent, attr2);
        DOMNodePointer p2 = new DOMNodePointer(parent, attr1);
        assertEquals(1, parent.compareChildNodePointers(p1, p2));
    }

    @Test
    public void testCompareChildNodePointersChildrenOrder() {
        root.removeChild(commentNode);
        root.removeChild(piNode);
        root.removeChild(langElement);
        root.removeChild(namespaceElement);
        DOMNodePointer parent = new DOMNodePointer(root, Locale.US);
        DOMNodePointer pText = new DOMNodePointer(parent, textNode);
        DOMNodePointer pChild = new DOMNodePointer(parent, childElement);
        // child element is before text node in document order, so -1 for pChild?
        assertEquals(-1, parent.compareChildNodePointers(pChild, pText));
    }

    @Test
    public void testHashCode() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        assertTrue(p1.equals(p1));
    }

    @Test
    public void testEqualsSameNode() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentNode() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(childElement, Locale.US);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsNull() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        assertFalse(p1.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        assertFalse(p1.equals("string"));
    }

    @Test
    public void testStaticGetPrefixWithPrefix() {
        assertEquals("ns", DOMNodePointer.getPrefix(root));
    }

    @Test
    public void testStaticGetPrefixNoPrefix() {
        Element elem = doc.createElement("noPrefix");
        assertNull(DOMNodePointer.getPrefix(elem));
    }

    @Test
    public void testStaticGetLocalNameWithLocalName() {
        assertEquals("root", DOMNodePointer.getLocalName(root));
    }

    @Test
    public void testStaticGetLocalNameFallback() {
        Element elem = doc.createElement("elem");
        assertEquals("elem", DOMNodePointer.getLocalName(elem));
    }

    @Test
    public void testStaticGetNamespaceURIForElementWithNS() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(root));
    }

    @Test
    public void testStaticGetNamespaceURIForDocument() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(doc));
    }

    @Test
    public void testStaticGetNamespaceURIForElementNoNS() {
        Element elem = doc.createElement("noNS");
        assertNull(DOMNodePointer.getNamespaceURI(elem));
    }

    @Test
    public void testGetValue() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        String value = (String) pointer.getValue();
        assertNotNull(value);
        assertTrue(value.contains("some text"));
    }

    @Test
    public void testGetValueComment() {
        DOMNodePointer pointer = new DOMNodePointer(commentNode, Locale.US);
        assertEquals("comment data", pointer.getValue());
    }

    @Test
    public void testGetValueProcessingInstruction() {
        DOMNodePointer pointer = new DOMNodePointer(piNode, Locale.US);
        assertEquals("pi data", pointer.getValue());
    }

    @Test
    public void testGetValueTextNode() {
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.US);
        assertEquals("some text", pointer.getValue());
    }

    @Test
    public void testIsLanguageParentNull() {
        NodePointer parentPointer = new DummyNodePointer(null, Locale.US);
        DOMNodePointer pointer = new DOMNodePointer(parentPointer, root);
        assertFalse(pointer.isLanguage("en"));
    }
}
