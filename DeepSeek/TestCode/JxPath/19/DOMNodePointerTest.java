package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
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
    private Element child;
    private Text textNode;
    private Comment commentNode;
    private ProcessingInstruction piNode;
    private CDATASection cdataNode;
    private DOMNodePointer rootPointer;
    private DOMNodePointer childPointer;
    private DOMNodePointer textPointer;
    private DOMNodePointer commentPointer;
    private DOMNodePointer piPointer;
    private DOMNodePointer cdataPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
        root = doc.createElementNS("http://example.com/ns", "ns:root");
        root.setAttribute("xmlns:ns", "http://example.com/ns");
        root.setAttribute("xml:lang", "en");
        doc.appendChild(root);
        child = doc.createElementNS("http://example.com/ns", "ns:child");
        root.appendChild(child);
        textNode = doc.createTextNode("  some text  ");
        root.appendChild(textNode);
        commentNode = doc.createComment(" comment ");
        root.appendChild(commentNode);
        piNode = doc.createProcessingInstruction("target", " data ");
        root.appendChild(piNode);
        cdataNode = doc.createCDATASection(" cdata ");
        root.appendChild(cdataNode);

        rootPointer = new DOMNodePointer(root, Locale.US);
        childPointer = new DOMNodePointer(child, Locale.US);
        textPointer = new DOMNodePointer(textNode, Locale.US);
        commentPointer = new DOMNodePointer(commentNode, Locale.US);
        piPointer = new DOMNodePointer(piNode, Locale.US);
        cdataPointer = new DOMNodePointer(cdataNode, Locale.US);
    }

    // Constructor tests
    @Test
    public void testConstructorNodeLocale() {
        DOMNodePointer p = new DOMNodePointer(root, Locale.UK);
        assertSame(root, p.getNode());
        assertEquals(Locale.UK, p.getLocale());
    }

    @Test
    public void testConstructorNodeLocaleId() {
        DOMNodePointer p = new DOMNodePointer(root, Locale.CANADA, "myId");
        assertSame(root, p.getNode());
        assertEquals(Locale.CANADA, p.getLocale());
        assertEquals("id('myId')", p.asPath());
    }

    @Test
    public void testConstructorParentNode() {
        DOMNodePointer parent = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p = new DOMNodePointer(parent, child);
        assertSame(child, p.getNode());
        assertSame(parent, p.getParent());
    }

    // testNode instance method
    @Test
    public void testTestNodeNullTest() {
        assertTrue(rootPointer.testNode(null));
    }

    @Test
    public void testTestNodeNodeNameTestElementWildcardNoPrefix() {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null);
        assertTrue(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestElementWildcardWithPrefix() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "*"), "http://example.com/ns");
        assertTrue(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestElementNameMatch() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "root"), "http://example.com/ns");
        assertTrue(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestElementNameMatchNoNamespace() {
        NodeNameTest test = new NodeNameTest(new QName(null, "root"), null);
        assertTrue(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestElementNameMismatch() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "other"), "http://example.com/ns");
        assertFalse(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeNameTestNonElement() {
        NodeNameTest test = new NodeNameTest(new QName(null, "text"), null);
        assertFalse(textPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestNode() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(rootPointer.testNode(test));
        assertTrue(textPointer.testNode(test));
        assertTrue(commentPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(textPointer.testNode(test));
        assertTrue(cdataPointer.testNode(test));
        assertFalse(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestComment() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(commentPointer.testNode(test));
        assertFalse(textPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestPI() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(piPointer.testNode(test));
        assertFalse(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeNodeTypeTestUnknown() {
        NodeTypeTest test = new NodeTypeTest(999);
        assertFalse(rootPointer.testNode(test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestMatch() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(piPointer.testNode(test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestMismatch() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(piPointer.testNode(test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestNonPI() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertFalse(rootPointer.testNode(test));
    }

    // static testNode
    @Test
    public void testStaticTestNodeNullTest() {
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    @Test
    public void testStaticTestNodeNodeNameTest() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "root"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    // equalStrings indirectly tested via testNode

    // getName
    @Test
    public void testGetNameElement() {
        QName name = rootPointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("root", name.getName());
    }

    @Test
    public void testGetNamePI() {
        QName name = piPointer.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    @Test
    public void testGetNameOther() {
        QName name = textPointer.getName();
        assertNull(name.getPrefix());
        assertNull(name.getName());
    }

    // getNamespaceURI
    @Test
    public void testGetNamespaceURI() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI());
    }

    // childIterator
    @Test
    public void testChildIterator() {
        NodeIterator it = rootPointer.childIterator(null, false, null);
        assertNotNull(it);
        assertTrue(it instanceof DOMNodeIterator);
    }

    // attributeIterator
    @Test
    public void testAttributeIterator() {
        NodeIterator it = rootPointer.attributeIterator(new QName("xml:lang"));
        assertNotNull(it);
        assertTrue(it instanceof DOMAttributeIterator);
    }

    // namespacePointer
    @Test
    public void testNamespacePointer() {
        NodePointer np = rootPointer.namespacePointer("ns");
        assertNotNull(np);
        assertTrue(np instanceof NamespacePointer);
    }

    // namespaceIterator
    @Test
    public void testNamespaceIterator() {
        NodeIterator it = rootPointer.namespaceIterator();
        assertNotNull(it);
        assertTrue(it instanceof DOMNamespaceIterator);
    }

    // getNamespaceResolver
    @Test
    public void testGetNamespaceResolver() {
        assertNotNull(rootPointer.getNamespaceResolver());
        // caching
        assertSame(rootPointer.getNamespaceResolver(), rootPointer.getNamespaceResolver());
    }

    // getNamespaceURI(String prefix)
    @Test
    public void testGetNamespaceURINullPrefix() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI(null));
    }

    @Test
    public void testGetNamespaceURIEmptyPrefix() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI(""));
    }

    @Test
    public void testGetNamespaceURIXmlPrefix() {
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURIXmlnsPrefix() {
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
    }

    @Test
    public void testGetNamespaceURIKnownPrefix() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("ns"));
    }

    @Test
    public void testGetNamespaceURIUnknownPrefix() {
        assertNull(rootPointer.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceURIPrefixDefinedInParent() {
        // child inherits ns prefix from root
        assertEquals("http://example.com/ns", childPointer.getNamespaceURI("ns"));
    }

    @Test
    public void testGetNamespaceURIPrefixNotFound() {
        // no xmlns:foo defined
        assertNull(rootPointer.getNamespaceURI("foo"));
    }

    // getDefaultNamespaceURI
    @Test
    public void testGetDefaultNamespaceURIWithXmlns() {
        root.setAttribute("xmlns", "http://default.ns");
        DOMNodePointer p = new DOMNodePointer(root, Locale.US);
        assertEquals("http://default.ns", p.getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURINoXmlns() {
        // root has no default xmlns, but we set xmlns:ns only
        DOMNodePointer p = new DOMNodePointer(root, Locale.US);
        assertNull(p.getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURIDocumentNode() {
        Document doc2 = doc.getImplementation().createDocument(null, "doc", null);
        DOMNodePointer p = new DOMNodePointer(doc2, Locale.US);
        assertNull(p.getDefaultNamespaceURI());
    }

    // getBaseValue, getImmediateNode
    @Test
    public void testGetBaseValue() {
        assertSame(root, rootPointer.getBaseValue());
    }

    @Test
    public void testGetImmediateNode() {
        assertSame(root, rootPointer.getImmediateNode());
    }

    // isActual, isCollection, getLength
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

    // isLeaf
    @Test
    public void testIsLeafTrue() {
        assertTrue(textPointer.isLeaf());
    }

    @Test
    public void testIsLeafFalse() {
        assertFalse(rootPointer.isLeaf());
    }

    // isLanguage
    @Test
    public void testIsLanguageMatch() {
        assertTrue(rootPointer.isLanguage("en"));
    }

    @Test
    public void testIsLanguageCaseInsensitive() {
        assertTrue(rootPointer.isLanguage("EN"));
    }

    @Test
    public void testIsLanguageNoMatch() {
        assertFalse(rootPointer.isLanguage("fr"));
    }

    @Test
    public void testIsLanguageNoLangAttribute() {
        // text node has no xml:lang, falls back to super.isLanguage which returns false
        assertFalse(textPointer.isLanguage("en"));
    }

    // findEnclosingAttribute
    @Test
    public void testFindEnclosingAttributeFound() {
        assertEquals("en", DOMNodePointer.findEnclosingAttribute(root, "xml:lang"));
    }

    @Test
    public void testFindEnclosingAttributeNotFound() {
        assertNull(DOMNodePointer.findEnclosingAttribute(root, "nonexistent"));
    }

    @Test
    public void testFindEnclosingAttributeOnParent() {
        assertEquals("en", DOMNodePointer.findEnclosingAttribute(child, "xml:lang"));
    }

    // getLanguage
    @Test
    public void testGetLanguage() {
        assertEquals("en", rootPointer.getLanguage());
    }

    // setValue
    @Test
    public void testSetValueTextNodeWithString() {
        textPointer.setValue("new text");
        assertEquals("new text", textNode.getNodeValue());
    }

    @Test
    public void testSetValueTextNodeWithEmptyString() {
        Node parent = textNode.getParentNode();
        assertNotNull(parent);
        textPointer.setValue("");
        assertNull(textNode.getParentNode()); // removed
    }

    @Test
    public void testSetValueTextNodeWithNull() {
        Node parent = textNode.getParentNode();
        textPointer.setValue(null);
        assertNull(textNode.getParentNode());
    }

    @Test
    public void testSetValueElementWithString() {
        childPointer.setValue("child text");
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("child text", child.getTextContent().trim());
    }

    @Test
    public void testSetValueElementWithEmptyString() {
        childPointer.setValue("");
        assertEquals(0, child.getChildNodes().getLength());
    }

    @Test
    public void testSetValueElementWithElement() {
        Element newElem = doc.createElement("newChild");
        newElem.setTextContent("content");
        childPointer.setValue(newElem);
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("newChild", child.getFirstChild().getNodeName());
    }

    @Test
    public void testSetValueElementWithDocument() {
        Document newDoc = doc.getImplementation().createDocument(null, "rootDoc", null);
        Element docElem = newDoc.getDocumentElement();
        docElem.setTextContent("doc content");
        childPointer.setValue(newDoc);
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("rootDoc", child.getFirstChild().getNodeName());
    }

    @Test
    public void testSetValueElementWithNonElementNode() {
        Text txt = doc.createTextNode("text");
        childPointer.setValue(txt);
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("#text", child.getFirstChild().getNodeName());
    }

    // createChild
    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreateChildFactoryFails() {
        JXPathContext context = JXPathContext.newContext(doc);
        // no factory set, so getAbstractFactory returns null -> NPE? Actually getAbstractFactory throws? 
        // We'll set a factory that returns false.
        context.setFactory(new org.apache.commons.jxpath.AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                return false;
            }
        });
        rootPointer.createChild(context, new QName("newChild"), 0);
    }

    @Test
    public void testCreateChildSuccess() {
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new org.apache.commons.jxpath.AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                // simulate adding a child element
                if (parent instanceof Element) {
                    Element el = ((Element) parent).getOwnerDocument().createElement(name);
                    ((Element) parent).appendChild(el);
                    return true;
                }
                return false;
            }
        });
        NodePointer ptr = rootPointer.createChild(context, new QName("newChild"), 0);
        assertNotNull(ptr);
        assertEquals("newChild", ((Node) ptr.getBaseValue()).getNodeName());
    }

    @Test
    public void testCreateChildWithValue() {
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new org.apache.commons.jxpath.AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                Element el = ((Element) parent).getOwnerDocument().createElement(name);
                ((Element) parent).appendChild(el);
                return true;
            }
        });
        NodePointer ptr = rootPointer.createChild(context, new QName("child2"), 0, "value");
        assertEquals("value", ((Node) ptr.getBaseValue()).getTextContent());
    }

    // createAttribute
    @Test
    public void testCreateAttributeNoPrefix() {
        JXPathContext context = JXPathContext.newContext(doc);
        NodePointer ptr = rootPointer.createAttribute(context, new QName("attr1"));
        assertNotNull(ptr);
        assertTrue(root.hasAttribute("attr1"));
    }

    @Test
    public void testCreateAttributeWithPrefix() {
        JXPathContext context = JXPathContext.newContext(doc);
        NodePointer ptr = rootPointer.createAttribute(context, new QName("ns", "attr2"));
        assertNotNull(ptr);
        assertTrue(root.hasAttributeNS("http://example.com/ns", "attr2"));
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownPrefix() {
        JXPathContext context = JXPathContext.newContext(doc);
        rootPointer.createAttribute(context, new QName("unknown", "attr"));
    }

    @Test
    public void testCreateAttributeOnNonElement() {
        JXPathContext context = JXPathContext.newContext(doc);
        // text node is not Element, should call super.createAttribute which returns null? Actually super.createAttribute throws UnsupportedOperationException? 
        // We'll just verify it doesn't throw.
        NodePointer ptr = textPointer.createAttribute(context, new QName("attr"));
        // super.createAttribute returns null pointer? Actually NodePointer.createAttribute returns a NullPointer.
        assertNotNull(ptr);
        assertTrue(ptr instanceof NullPointer);
    }

    // remove
    @Test(expected = JXPathException.class)
    public void testRemoveRoot() {
        rootPointer.remove();
    }

    @Test
    public void testRemoveChild() {
        assertNotNull(child.getParentNode());
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // asPath
    @Test
    public void testAsPathWithId() {
        DOMNodePointer p = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", p.asPath());
    }

    @Test
    public void testAsPathElementNoNamespace() {
        // create element without namespace
        Element el = doc.createElement("plain");
        root.appendChild(el);
        DOMNodePointer p = new DOMNodePointer(el, Locale.US);
        String path = p.asPath();
        assertTrue(path.contains("plain["));
    }

    @Test
    public void testAsPathElementWithNamespace() {
        String path = childPointer.asPath();
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
        assertTrue(path.contains("processing-instruction('target')["));
    }

    @Test
    public void testAsPathDocumentNode() {
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        assertEquals("", docPointer.asPath());
    }

    @Test
    public void testAsPathElementWithParentNotDOMNodePointer() {
        // root's parent is null, so path is just the element part
        String path = rootPointer.asPath();
        assertTrue(path.startsWith("/ns:root["));
    }

    // getRelativePositionByQName, getRelativePositionOfElement, etc. tested via asPath

    // hashCode and equals
    @Test
    public void testHashCode() {
        assertEquals(root.hashCode(), rootPointer.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(rootPointer.equals(rootPointer));
    }

    @Test
    public void testEqualsSameNode() {
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US);
        assertTrue(rootPointer.equals(p2));
    }

    @Test
    public void testEqualsDifferentNode() {
        assertFalse(rootPointer.equals(childPointer));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(rootPointer.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(rootPointer.equals("string"));
    }

    // static getPrefix
    @Test
    public void testGetPrefixWithPrefix() {
        assertEquals("ns", DOMNodePointer.getPrefix(root));
    }

    @Test
    public void testGetPrefixNoPrefix() {
        Element el = doc.createElement("plain");
        assertNull(DOMNodePointer.getPrefix(el));
    }

    @Test
    public void testGetPrefixFromNodeName() {
        // node with prefix in name but no prefix property
        Element el = doc.createElement("pre:fixed");
        assertEquals("pre", DOMNodePointer.getPrefix(el));
    }

    // static getLocalName
    @Test
    public void testGetLocalNameWithLocalName() {
        assertEquals("root", DOMNodePointer.getLocalName(root));
    }

    @Test
    public void testGetLocalNameNoLocalName() {
        Element el = doc.createElement("plain");
        assertEquals("plain", DOMNodePointer.getLocalName(el));
    }

    @Test
    public void testGetLocalNameFromNodeName() {
        Element el = doc.createElement("pre:fixed");
        assertEquals("fixed", DOMNodePointer.getLocalName(el));
    }

    // static getNamespaceURI
    @Test
    public void testStaticGetNamespaceURIElement() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(root));
    }

    @Test
    public void testStaticGetNamespaceURIDocument() {
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(doc));
    }

    @Test
    public void testStaticGetNamespaceURINoNamespace() {
        Element el = doc.createElement("plain");
        assertNull(DOMNodePointer.getNamespaceURI(el));
    }

    // getValue
    @Test
    public void testGetValueComment() {
        assertEquals("comment", commentPointer.getValue());
    }

    @Test
    public void testGetValueText() {
        assertEquals("some text", textPointer.getValue());
    }

    @Test
    public void testGetValueCDATA() {
        assertEquals("cdata", cdataPointer.getValue());
    }

    @Test
    public void testGetValuePI() {
        assertEquals("data", piPointer.getValue());
    }

    @Test
    public void testGetValueElement() {
        // element with child text
        assertEquals("some text", rootPointer.getValue().trim());
    }

    @Test
    public void testGetValueWithXmlSpacePreserve() {
        root.setAttribute("xml:space", "preserve");
        assertEquals("  some text  ", textPointer.getValue());
    }

    // getPointerByID
    @Test
    public void testGetPointerByIDFound() {
        root.setAttribute("id", "myId");
        doc.getElementById("myId"); // ensure it's registered
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer ptr = rootPointer.getPointerByID(context, "myId");
        assertNotNull(ptr);
        assertTrue(ptr instanceof DOMNodePointer);
        assertEquals(root, ((DOMNodePointer) ptr).getNode());
    }

    @Test
    public void testGetPointerByIDNotFound() {
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer ptr = rootPointer.getPointerByID(context, "nonexistent");
        assertNotNull(ptr);
        assertTrue(ptr instanceof NullPointer);
    }

    // compareChildNodePointers
    @Test
    public void testCompareChildNodePointersSameNode() {
        assertEquals(0, rootPointer.compareChildNodePointers(textPointer, textPointer));
    }

    @Test
    public void testCompareChildNodePointersAttributeFirst() {
        Attr attr = root.getAttributeNode("xml:lang");
        DOMNodePointer attrPointer = new DOMNodePointer(attr, Locale.US);
        // attribute vs non-attribute: attribute should be -1
        assertEquals(-1, rootPointer.compareChildNodePointers(attrPointer, textPointer));
        assertEquals(1, rootPointer.compareChildNodePointers(textPointer, attrPointer));
    }

    @Test
    public void testCompareChildNodePointersTwoAttributes() {
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");
        Attr a1 = root.getAttributeNode("attr1");
        Attr a2 = root.getAttributeNode("attr2");
        DOMNodePointer p1 = new DOMNodePointer(a1, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(a2, Locale.US);
        // a1 appears before a2 in attribute order
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointers(p2, p1));
    }

    @Test
    public void testCompareChildNodePointersChildOrder() {
        // child before textNode in document order
        assertEquals(-1, rootPointer.compareChildNodePointers(childPointer, textPointer));
        assertEquals(1, rootPointer.compareChildNodePointers(textPointer, childPointer));
    }

    @Test
    public void testCompareChildNodePointersNotFound() {
        // node not a child
        Element other = doc.createElement("other");
        DOMNodePointer otherPointer = new DOMNodePointer(other, Locale.US);
        assertEquals(0, rootPointer.compareChildNodePointers(otherPointer, textPointer));
    }
}
