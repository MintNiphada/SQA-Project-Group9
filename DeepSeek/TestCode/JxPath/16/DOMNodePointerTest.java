package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodePointerTest {

    private Document doc;
    private Element root;
    private Element childWithNS;
    private Element childWithoutNS;
    private Text textNode;
    private Comment commentNode;
    private ProcessingInstruction piNode;
    private DOMNodePointer rootPointer;
    private DOMNodePointer childWithNSPointer;
    private DOMNodePointer childWithoutNSPointer;
    private DOMNodePointer textPointer;
    private DOMNodePointer commentPointer;
    private DOMNodePointer piPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        doc = factory.newDocumentBuilder().newDocument();
        root = doc.createElementNS("http://example.com/ns", "ns:root");
        root.setPrefix("ns");
        doc.appendChild(root);

        childWithNS = doc.createElementNS("http://example.com/ns", "ns:child");
        childWithNS.setPrefix("ns");
        root.appendChild(childWithNS);

        childWithoutNS = doc.createElement("noNsChild");
        root.appendChild(childWithoutNS);

        textNode = doc.createTextNode("some text");
        root.appendChild(textNode);

        commentNode = doc.createComment("a comment");
        root.appendChild(commentNode);

        piNode = doc.createProcessingInstruction("target", "data");
        root.appendChild(piNode);

        rootPointer = new DOMNodePointer(root, Locale.US);
        childWithNSPointer = new DOMNodePointer(rootPointer, childWithNS);
        childWithoutNSPointer = new DOMNodePointer(rootPointer, childWithoutNS);
        textPointer = new DOMNodePointer(rootPointer, textNode);
        commentPointer = new DOMNodePointer(rootPointer, commentNode);
        piPointer = new DOMNodePointer(rootPointer, piNode);
    }

    @Test
    public void testConstructors() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        assertEquals(root, p1.getNode());
        assertNull(p1.getParent());
        assertEquals(Locale.US, p1.getLocale());

        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals(root, p2.getNode());
        assertEquals("myId", p2.asPath()); // id path

        DOMNodePointer p3 = new DOMNodePointer(rootPointer, childWithNS);
        assertEquals(childWithNS, p3.getNode());
        assertEquals(rootPointer, p3.getParent());
    }

    @Test
    public void testTestNodeNullTest() {
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    @Test
    public void testTestNodeNodeNameTestElement() {
        // wildcard with no prefix
        NodeNameTest wildcard = new NodeNameTest(new QName(null, "*"), null);
        assertTrue(DOMNodePointer.testNode(childWithNS, wildcard));

        // wildcard with prefix
        NodeNameTest wildcardPrefixed = new NodeNameTest(new QName("ns", "*"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(childWithNS, wildcardPrefixed));

        // exact name match with namespace
        NodeNameTest exact = new NodeNameTest(new QName("ns", "child"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(childWithNS, exact));

        // name mismatch
        NodeNameTest wrongName = new NodeNameTest(new QName("ns", "other"), "http://example.com/ns");
        assertFalse(DOMNodePointer.testNode(childWithNS, wrongName));

        // namespace mismatch
        NodeNameTest wrongNS = new NodeNameTest(new QName("ns", "child"), "http://other.com");
        assertFalse(DOMNodePointer.testNode(childWithNS, wrongNS));

        // no namespace on node, test with prefix but no namespace URI
        NodeNameTest noNS = new NodeNameTest(new QName("ns", "noNsChild"), null);
        assertFalse(DOMNodePointer.testNode(childWithoutNS, noNS)); // node has no prefix, so getPrefix returns null, equalStrings(null, "ns") false

        // test on non-element node
        assertFalse(DOMNodePointer.testNode(textNode, exact));
    }

    @Test
    public void testTestNodeNodeTypeTest() {
        // NODE_TYPE_NODE: element or document
        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(root, nodeTest));
        assertTrue(DOMNodePointer.testNode(doc, nodeTest));
        assertFalse(DOMNodePointer.testNode(textNode, nodeTest));

        // NODE_TYPE_TEXT: text or cdata
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, textTest));
        assertFalse(DOMNodePointer.testNode(root, textTest));

        // NODE_TYPE_COMMENT
        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(commentNode, commentTest));
        assertFalse(DOMNodePointer.testNode(root, commentTest));

        // NODE_TYPE_PI
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(piNode, piTest));
        assertFalse(DOMNodePointer.testNode(root, piTest));

        // unknown type
        NodeTypeTest unknown = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(root, unknown));
    }

    @Test
    public void testTestNodeProcessingInstructionTest() {
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piNode, piTest));
        ProcessingInstructionTest wrongTarget = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(piNode, wrongTarget));
        // non-PI node
        assertFalse(DOMNodePointer.testNode(root, piTest));
    }

    @Test
    public void testGetName() {
        QName name = rootPointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("root", name.getName());

        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("target", piName.getName());

        // document node
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        QName docName = docPointer.getName();
        assertNull(docName.getPrefix());
        assertNull(docName.getName());
    }

    @Test
    public void testGetNamespaceURI() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI());
        // non-element node
        assertEquals(null, textPointer.getNamespaceURI());
    }

    @Test
    public void testChildIterator() {
        NodeIterator it = rootPointer.childIterator(null, false, null);
        assertNotNull(it);
        assertTrue(it instanceof DOMNodeIterator);
    }

    @Test
    public void testAttributeIterator() {
        NodeIterator it = rootPointer.attributeIterator(new QName("attr"));
        assertNotNull(it);
        assertTrue(it instanceof DOMAttributeIterator);
    }

    @Test
    public void testNamespacePointer() {
        NodePointer np = rootPointer.namespacePointer("ns");
        assertNotNull(np);
        assertTrue(np instanceof NamespacePointer);
    }

    @Test
    public void testNamespaceIterator() {
        NodeIterator it = rootPointer.namespaceIterator();
        assertNotNull(it);
        assertTrue(it instanceof DOMNamespaceIterator);
    }

    @Test
    public void testGetNamespaceResolver() {
        NamespaceResolver resolver = rootPointer.getNamespaceResolver();
        assertNotNull(resolver);
        // second call returns same instance
        assertSame(resolver, rootPointer.getNamespaceResolver());
    }

    @Test
    public void testGetNamespaceURIString() {
        // null prefix -> default namespace
        assertNull(rootPointer.getNamespaceURI(null));
        // empty prefix -> default namespace
        assertNull(rootPointer.getNamespaceURI(""));
        // xml prefix
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        // xmlns prefix
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
        // known prefix from element
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("ns"));
        // unknown prefix
        assertNull(rootPointer.getNamespaceURI("unknown"));
        // caching: second call returns same
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("ns"));
    }

    @Test
    public void testGetDefaultNamespaceURI() {
        // no default namespace defined
        assertNull(rootPointer.getDefaultNamespaceURI());
        // set default namespace on root
        root.setAttribute("xmlns", "http://default.com");
        assertEquals("http://default.com", rootPointer.getDefaultNamespaceURI());
        // reset
        root.removeAttribute("xmlns");
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
        assertFalse(rootPointer.isLeaf()); // root has children
        assertTrue(textPointer.isLeaf()); // text node has no children
    }

    @Test
    public void testIsLanguage() {
        // no xml:lang
        assertFalse(rootPointer.isLanguage("en"));
        // set xml:lang on root
        root.setAttribute("xml:lang", "en-US");
        assertTrue(rootPointer.isLanguage("en"));
        assertFalse(rootPointer.isLanguage("fr"));
        // remove
        root.removeAttribute("xml:lang");
    }

    @Test
    public void testFindEnclosingAttribute() {
        // set attribute on parent
        root.setAttribute("testAttr", "value");
        assertEquals("value", DOMNodePointer.findEnclosingAttribute(childWithNS, "testAttr"));
        // not found
        assertNull(DOMNodePointer.findEnclosingAttribute(childWithNS, "nonexistent"));
        root.removeAttribute("testAttr");
    }

    @Test
    public void testSetValueTextNode() {
        // set non-empty string
        textPointer.setValue("new text");
        assertEquals("new text", textNode.getNodeValue());
        // set empty string -> remove node
        textPointer.setValue("");
        assertNull(textNode.getParentNode());
        // re-add for further tests
        root.appendChild(textNode);
    }

    @Test
    public void testSetValueElementWithString() {
        childWithoutNSPointer.setValue("hello");
        NodeList children = childWithoutNS.getChildNodes();
        assertEquals(1, children.getLength());
        assertEquals("hello", children.item(0).getNodeValue());
        // clear children
        childWithoutNSPointer.setValue("");
        assertEquals(0, childWithoutNS.getChildNodes().getLength());
    }

    @Test
    public void testSetValueElementWithElement() {
        Element newElem = doc.createElement("newChild");
        newElem.appendChild(doc.createTextNode("data"));
        childWithoutNSPointer.setValue(newElem);
        NodeList children = childWithoutNS.getChildNodes();
        assertEquals(1, children.getLength());
        assertEquals("newChild", children.item(0).getNodeName());
        assertEquals("data", children.item(0).getTextContent());
    }

    @Test
    public void testSetValueElementWithDocument() {
        Document newDoc = null;
        try {
            newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }
        Element docRoot = newDoc.createElement("docRoot");
        newDoc.appendChild(docRoot);
        docRoot.appendChild(newDoc.createTextNode("docData"));
        childWithoutNSPointer.setValue(newDoc);
        NodeList children = childWithoutNS.getChildNodes();
        assertEquals(1, children.getLength());
        assertEquals("docRoot", children.item(0).getNodeName());
        assertEquals("docData", children.item(0).getTextContent());
    }

    @Test
    public void testSetValueElementWithOtherNode() {
        Comment comment = doc.createComment("comment");
        childWithoutNSPointer.setValue(comment);
        NodeList children = childWithoutNS.getChildNodes();
        assertEquals(1, children.getLength());
        assertEquals(Node.COMMENT_NODE, children.item(0).getNodeType());
        assertEquals("comment", children.item(0).getNodeValue());
    }

    @Test
    public void testCreateChild() {
        JXPathContext context = new StubJXPathContext(new StubAbstractFactory(true));
        DOMNodePointer ptr = (DOMNodePointer) rootPointer.createChild(context, new QName("newChild"), 0);
        assertNotNull(ptr);
        assertEquals("newChild", ptr.getNode().getNodeName());
    }

    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreateChildFactoryFails() {
        JXPathContext context = new StubJXPathContext(new StubAbstractFactory(false));
        rootPointer.createChild(context, new QName("fail"), 0);
    }

    @Test
    public void testCreateChildWithValue() {
        JXPathContext context = new StubJXPathContext(new StubAbstractFactory(true));
        NodePointer ptr = rootPointer.createChild(context, new QName("valuedChild"), 0, "value");
        assertNotNull(ptr);
        assertEquals("value", ptr.getValue());
    }

    @Test
    public void testCreateAttribute() {
        JXPathContext context = new StubJXPathContext(null);
        NodePointer attrPtr = rootPointer.createAttribute(context, new QName("attr1"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("attr1"));
    }

    @Test
    public void testCreateAttributeWithNamespace() {
        JXPathContext context = new StubJXPathContext(null) {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("ns".equals(prefix)) return "http://example.com/ns";
                return null;
            }
        };
        NodePointer attrPtr = rootPointer.createAttribute(context, new QName("ns", "attr2"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttributeNS("http://example.com/ns", "attr2"));
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownPrefix() {
        JXPathContext context = new StubJXPathContext(null);
        rootPointer.createAttribute(context, new QName("unknown", "attr"));
    }

    @Test
    public void testRemove() {
        DOMNodePointer childPtr = new DOMNodePointer(rootPointer, childWithoutNS);
        childPtr.remove();
        assertNull(childWithoutNS.getParentNode());
    }

    @Test(expected = JXPathException.class)
    public void testRemoveRoot() {
        rootPointer.remove();
    }

    @Test
    public void testAsPathWithId() {
        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", idPtr.asPath());
    }

    @Test
    public void testAsPathElementNoNamespace() {
        String path = childWithoutNSPointer.asPath();
        assertTrue(path.startsWith("/noNsChild["));
    }

    @Test
    public void testAsPathElementWithNamespace() {
        String path = childWithNSPointer.asPath();
        assertTrue(path.contains("ns:child["));
    }

    @Test
    public void testAsPathTextNode() {
        String path = textPointer.asPath();
        assertTrue(path.startsWith("/text()["));
    }

    @Test
    public void testAsPathPINode() {
        String path = piPointer.asPath();
        assertTrue(path.startsWith("/processing-instruction('target')["));
    }

    @Test
    public void testAsPathDocumentNode() {
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        assertEquals("", docPointer.asPath());
    }

    @Test
    public void testEscape() {
        // indirectly tested via asPath with id containing quotes
        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "it's \"quoted\"");
        String path = idPtr.asPath();
        assertTrue(path.contains("&apos;"));
        assertTrue(path.contains("&quot;"));
    }

    @Test
    public void testHashCodeEquals() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals(new Object()));
    }

    @Test
    public void testGetPrefix() {
        assertEquals("ns", DOMNodePointer.getPrefix(childWithNS));
        assertNull(DOMNodePointer.getPrefix(childWithoutNS));
    }

    @Test
    public void testGetLocalName() {
        assertEquals("child", DOMNodePointer.getLocalName(childWithNS));
        assertEquals("noNsChild", DOMNodePointer.getLocalName(childWithoutNS));
    }

    @Test
    public void testGetNamespaceURIStatic() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(childWithNS));
        assertNull(DOMNodePointer.getNamespaceURI(childWithoutNS));
    }

    @Test
    public void testGetValueComment() {
        assertEquals("a comment", commentPointer.getValue());
        // comment with whitespace
        Comment wsComment = doc.createComment("  spaced  ");
        DOMNodePointer wsPtr = new DOMNodePointer(wsComment, Locale.US);
        assertEquals("spaced", wsPtr.getValue());
    }

    @Test
    public void testGetValueText() {
        assertEquals("some text", textPointer.getValue());
    }

    @Test
    public void testGetValueElement() {
        // element with child text
        assertEquals("some text", rootPointer.getValue());
    }

    @Test
    public void testStringValuePreserveSpace() {
        // set xml:space="preserve" on root
        root.setAttribute("xml:space", "preserve");
        Text preserveText = doc.createTextNode("  keep spaces  ");
        root.appendChild(preserveText);
        DOMNodePointer preservePtr = new DOMNodePointer(preserveText, Locale.US);
        assertEquals("  keep spaces  ", preservePtr.getValue());
        root.removeChild(preserveText);
        root.removeAttribute("xml:space");
    }

    @Test
    public void testGetPointerByID() {
        // set id attribute and mark as ID
        Element idElem = doc.createElement("div");
        idElem.setAttribute("id", "targetId");
        idElem.setIdAttribute("id", true);
        root.appendChild(idElem);
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        Pointer ptr = docPointer.getPointerByID(new StubJXPathContext(null), "targetId");
        assertNotNull(ptr);
        assertTrue(ptr instanceof DOMNodePointer);
        assertEquals(idElem, ((DOMNodePointer) ptr).getNode());
        // not found
        Pointer nullPtr = docPointer.getPointerByID(new StubJXPathContext(null), "nonexistent");
        assertTrue(nullPtr instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        // add attributes to root
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");
        Attr attr1 = root.getAttributeNode("attr1");
        Attr attr2 = root.getAttributeNode("attr2");
        DOMNodePointer attr1Ptr = new DOMNodePointer(rootPointer, attr1);
        DOMNodePointer attr2Ptr = new DOMNodePointer(rootPointer, attr2);

        // attribute vs non-attribute
        assertTrue(rootPointer.compareChildNodePointers(attr1Ptr, childWithNSPointer) < 0);
        assertTrue(rootPointer.compareChildNodePointers(childWithNSPointer, attr1Ptr) > 0);

        // two attributes: order in NamedNodeMap
        int cmp = rootPointer.compareChildNodePointers(attr1Ptr, attr2Ptr);
        // attr1 comes before attr2 in map
        assertTrue(cmp < 0);
        cmp = rootPointer.compareChildNodePointers(attr2Ptr, attr1Ptr);
        assertTrue(cmp > 0);

        // same node
        assertEquals(0, rootPointer.compareChildNodePointers(attr1Ptr, attr1Ptr));

        // two child elements
        assertTrue(rootPointer.compareChildNodePointers(childWithNSPointer, childWithoutNSPointer) < 0);
        assertTrue(rootPointer.compareChildNodePointers(childWithoutNSPointer, childWithNSPointer) > 0);
    }

    // Stub classes for testing
    private static class StubJXPathContext extends JXPathContext {
        private AbstractFactory factory;
        public StubJXPathContext(AbstractFactory factory) {
            this.factory = factory;
        }
        @Override
        public AbstractFactory getFactory() {
            return factory;
        }
        @Override
        public String getNamespaceURI(String prefix) {
            return null;
        }
    }

    private static class StubAbstractFactory extends AbstractFactory {
        private boolean success;
        public StubAbstractFactory(boolean success) {
            this.success = success;
        }
        @Override
        public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            if (success && parent instanceof Node) {
                Node parentNode = (Node) parent;
                Document doc = parentNode.getOwnerDocument() != null ? parentNode.getOwnerDocument() : (Document) parentNode;
                Element child = doc.createElement(name);
                parentNode.appendChild(child);
                return true;
            }
            return false;
        }
    }
}
