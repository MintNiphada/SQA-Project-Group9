package org.apache.commons.jxpath.ri.model.dom;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
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

    private DocumentBuilder builder;
    private Document doc;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
    }

    private Document createDocument(String xml) throws Exception {
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    @Test
    public void testConstructorsAndBasicGetters() {
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, pointer.getBaseValue());
        Assert.assertEquals(root, pointer.getImmediateNode());
        Assert.assertEquals(root, pointer.getNode());
        Assert.assertTrue(pointer.isActual());
        Assert.assertFalse(pointer.isCollection());
        Assert.assertEquals(1, pointer.getLength());
        Assert.assertTrue(pointer.isLeaf());

        DOMNodePointer pointerWithId = new DOMNodePointer(root, Locale.GERMANY, "myId");
        Assert.assertEquals("id('myId')", pointerWithId.asPath());

        DOMNodePointer childPointer = new DOMNodePointer(pointer, root);
        Assert.assertEquals(pointer, childPointer.getParent());
    }

    @Test
    public void testLeafDetection() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        Assert.assertTrue(pointer.isLeaf());

        Element child = doc.createElement("child");
        root.appendChild(child);
        Assert.assertFalse(pointer.isLeaf());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element e1 = doc.createElement("elem");
        Element e2 = doc.createElement("elem");

        DOMNodePointer p1 = new DOMNodePointer(e1, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(e1, Locale.US);
        DOMNodePointer p3 = new DOMNodePointer(e2, Locale.US);

        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertNotEquals(p1, p3);
        Assert.assertNotEquals(p1, null);
        Assert.assertNotEquals(p1, "someString");
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testGetName() {
        Element elem = doc.createElementNS("http://example.com/ns", "pfx:item");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.US);
        QName name = pointer.getName();
        Assert.assertEquals("pfx", name.getPrefix());
        Assert.assertEquals("item", name.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("targetPI", "data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        Assert.assertNull(piPointer.getName().getPrefix());
        Assert.assertEquals("targetPI", piPointer.getName().getName());

        Comment comment = doc.createComment("a comment");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        Assert.assertNull(commentPointer.getName().getPrefix());
        Assert.assertNull(commentPointer.getName().getName());
    }

    @Test
    public void testGetPrefixAndLocalNameStatic() {
        Element elemWithPrefix = doc.createElementNS("http://ns", "p:tag");
        Assert.assertEquals("p", DOMNodePointer.getPrefix(elemWithPrefix));
        Assert.assertEquals("tag", DOMNodePointer.getLocalName(elemWithPrefix));

        Element elemNoPrefix = doc.createElement("tagOnly");
        Assert.assertNull(DOMNodePointer.getPrefix(elemNoPrefix));
        Assert.assertEquals("tagOnly", DOMNodePointer.getLocalName(elemNoPrefix));
    }

    @Test
    public void testTestNode() {
        Element elem = doc.createElementNS("http://example.com", "ns:child");
        Text text = doc.createTextNode("hello");
        CDATASection cdata = doc.createCDATASection("cdata-text");
        Comment comment = doc.createComment("comm");
        ProcessingInstruction pi = doc.createProcessingInstruction("myTarget", "data");

        // Null test
        Assert.assertTrue(DOMNodePointer.testNode(elem, null));

        // NodeNameTest matching
        NodeNameTest nnt1 = new NodeNameTest(new QName("ns", "child"), "http://example.com");
        Assert.assertTrue(DOMNodePointer.testNode(elem, nnt1));
        Assert.assertFalse(DOMNodePointer.testNode(text, nnt1));

        // Wildcard tests
        NodeNameTest wildcardNoPrefix = new NodeNameTest(new QName(null, "*"));
        Assert.assertTrue(DOMNodePointer.testNode(elem, wildcardNoPrefix));

        NodeNameTest wildcardWithPrefixMatch = new NodeNameTest(new QName("ns", "*"), "http://example.com");
        Assert.assertTrue(DOMNodePointer.testNode(elem, wildcardWithPrefixMatch));

        NodeNameTest wildcardWithPrefixMismatch = new NodeNameTest(new QName("other", "*"), "http://other.com");
        Assert.assertFalse(DOMNodePointer.testNode(elem, wildcardWithPrefixMismatch));

        NodeNameTest nameMismatch = new NodeNameTest(new QName("ns", "wrongName"), "http://example.com");
        Assert.assertFalse(DOMNodePointer.testNode(elem, nameMismatch));

        // NodeTypeTest matching
        NodeTypeTest testNode = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertTrue(DOMNodePointer.testNode(elem, testNode));
        Assert.assertTrue(DOMNodePointer.testNode(doc, testNode));
        Assert.assertFalse(DOMNodePointer.testNode(text, testNode));

        NodeTypeTest testText = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        Assert.assertTrue(DOMNodePointer.testNode(text, testText));
        Assert.assertTrue(DOMNodePointer.testNode(cdata, testText));
        Assert.assertFalse(DOMNodePointer.testNode(elem, testText));

        NodeTypeTest testComment = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        Assert.assertTrue(DOMNodePointer.testNode(comment, testComment));
        Assert.assertFalse(DOMNodePointer.testNode(elem, testComment));

        NodeTypeTest testPI = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        Assert.assertTrue(DOMNodePointer.testNode(pi, testPI));
        Assert.assertFalse(DOMNodePointer.testNode(elem, testPI));

        NodeTypeTest testUnknownType = new NodeTypeTest(999);
        Assert.assertFalse(DOMNodePointer.testNode(elem, testUnknownType));

        // ProcessingInstructionTest
        ProcessingInstructionTest pitMatch = new ProcessingInstructionTest("myTarget");
        ProcessingInstructionTest pitMismatch = new ProcessingInstructionTest("otherTarget");
        Assert.assertTrue(DOMNodePointer.testNode(pi, pitMatch));
        Assert.assertFalse(DOMNodePointer.testNode(pi, pitMismatch));
        Assert.assertFalse(DOMNodePointer.testNode(elem, pitMatch));

        // Instance testNode delegating
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.US);
        Assert.assertTrue(elemPointer.testNode(nnt1));
    }

    @Test
    public void testNamespacesAndResolution() throws Exception {
        String xml = "<root xmlns=\"http://default.com\" xmlns:foo=\"http://foo.com\">"
                + "<foo:child xmlns:bar=\"http://bar.com\"/>"
                + "</root>";
        Document d = createDocument(xml);
        Element root = d.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);

        Assert.assertEquals("http://default.com", rootPointer.getDefaultNamespaceURI());
        Assert.assertEquals("http://default.com", rootPointer.getNamespaceURI(""));
        Assert.assertEquals("http://default.com", rootPointer.getNamespaceURI((String) null));
        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://foo.com", rootPointer.getNamespaceURI("foo"));

        // Inherited and local namespace
        Assert.assertEquals("http://foo.com", childPointer.getNamespaceURI("foo"));
        Assert.assertEquals("http://bar.com", childPointer.getNamespaceURI("bar"));
        Assert.assertNull(childPointer.getNamespaceURI("unknownPrefix"));

        // Document node pointer namespace resolution
        DOMNodePointer docPointer = new DOMNodePointer(d, Locale.US);
        Assert.assertEquals("http://default.com", docPointer.getDefaultNamespaceURI());
        Assert.assertEquals("http://foo.com", docPointer.getNamespaceURI("foo"));
    }

    @Test
    public void testLanguageHandling() {
        Element root = doc.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        Assert.assertEquals("en-US", childPointer.getLanguage());
        Assert.assertTrue(childPointer.isLanguage("en"));
        Assert.assertTrue(childPointer.isLanguage("en-US"));
        Assert.assertFalse(childPointer.isLanguage("fr"));

        Element noLangElem = doc.createElement("noLang");
        DOMNodePointer noLangPointer = new DOMNodePointer(noLangElem, Locale.GERMAN);
        Assert.assertNull(noLangPointer.getLanguage());
        Assert.assertTrue(noLangPointer.isLanguage("de"));
    }

    @Test
    public void testGetValue() {
        Element root = doc.createElement("root");
        Comment comment = doc.createComment(" a comment text ");
        root.appendChild(comment);
        Text text = doc.createTextNode(" hello ");
        root.appendChild(text);
        ProcessingInstruction pi = doc.createProcessingInstruction("target", " pi text ");
        root.appendChild(pi);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        // By default xml:space != preserve, so trimmed text and pi are concatenated
        Assert.assertEquals("hellopi text", rootPointer.getValue());

        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("a comment text", commentPointer.getValue());

        // Test with xml:space="preserve"
        root.setAttribute("xml:space", "preserve");
        Assert.assertEquals(" hello  pi text ", rootPointer.getValue());
    }

    @Test
    public void testSetValue() {
        // Text node set value
        Text textNode = doc.createTextNode("original");
        Element parent = doc.createElement("parent");
        parent.appendChild(textNode);
        DOMNodePointer textPointer = new DOMNodePointer(textNode, Locale.US);
        textPointer.setValue("updated");
        Assert.assertEquals("updated", textNode.getNodeValue());

        // Text node set empty value removes the node
        textPointer.setValue("");
        Assert.assertNull(textNode.getParentNode());

        // Element set text value
        Element elem = doc.createElement("container");
        elem.appendChild(doc.createElement("c1"));
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.US);
        elemPointer.setValue("new text content");
        Assert.assertEquals(1, elem.getChildNodes().getLength());
        Assert.assertEquals("new text content", elem.getFirstChild().getNodeValue());

        // Element set Node (Element) value
        Element sourceElem = doc.createElement("source");
        Element childA = doc.createElement("childA");
        Element childB = doc.createElement("childB");
        sourceElem.appendChild(childA);
        sourceElem.appendChild(childB);

        elemPointer.setValue(sourceElem);
        Assert.assertEquals(2, elem.getChildNodes().getLength());
        Assert.assertEquals("childA", elem.getFirstChild().getNodeName());

        // Element set other Node value (e.g., Comment)
        Comment c = doc.createComment("comment-node");
        elemPointer.setValue(c);
        Assert.assertEquals(1, elem.getChildNodes().getLength());
        Assert.assertEquals(Node.COMMENT_NODE, elem.getFirstChild().getNodeType());
    }

    @Test
    public void testRemove() {
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        childPointer.remove();
        Assert.assertNull(child.getParentNode());

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        try {
            rootPointer.remove();
            Assert.fail("Expected JXPathException when removing root node without parent");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test
    public void testAsPath() throws Exception {
        String xml = "<root id=\"r1\">"
                + "<item name=\"1\"/>"
                + "<item name=\"2\"/>"
                + "text1"
                + "<![CDATA[cdata1]]>"
                + "<?pi target1?>"
                + "<?pi target2?>"
                + "</root>";
        Document d = createDocument(xml);
        Element root = d.getDocumentElement();

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Node item2 = root.getElementsByTagName("item").item(1);
        DOMNodePointer item2Ptr = new DOMNodePointer(rootPtr, item2);
        Assert.assertEquals("/item[2]", item2Ptr.asPath());

        Node textNode = root.getChildNodes().item(2);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, textNode);
        Assert.assertEquals("/text()[1]", textPtr.asPath());

        Node cdataNode = root.getChildNodes().item(3);
        DOMNodePointer cdataPtr = new DOMNodePointer(rootPtr, cdataNode);
        Assert.assertEquals("/text()[2]", cdataPtr.asPath());

        Node piNode2 = root.getChildNodes().item(5);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, piNode2);
        Assert.assertEquals("/processing-instruction('pi')[2]", piPtr.asPath());

        // Escaping in id
        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "foo'bar\"baz");
        Assert.assertEquals("id('foo&apos;bar&quot;baz')", idPtr.asPath());

        // Document pointer path
        DOMNodePointer docPtr = new DOMNodePointer(d, Locale.US);
        Assert.assertEquals("", docPtr.asPath());
    }

    @Test
    public void testGetPointerByID() {
        Element root = doc.createElement("root");
        root.setAttribute("id", "elem1");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);

        Pointer p1 = docPtr.getPointerByID(context, "elem1");
        Assert.assertTrue(p1 instanceof DOMNodePointer);
        Assert.assertEquals(root, p1.getNode());

        Pointer p2 = docPtr.getPointerByID(context, "nonExistent");
        Assert.assertTrue(p2 instanceof NullPointer);
    }

    @Test
    public void testCreateAttribute() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        // Simple attribute
        NodePointer attrPtr = rootPtr.createAttribute(context, new QName("attr1"));
        Assert.assertNotNull(attrPtr);
        Assert.assertTrue(root.hasAttribute("attr1"));

        // Existing attribute should not be overwritten
        root.setAttribute("attr1", "original");
        rootPtr.createAttribute(context, new QName("attr1"));
        Assert.assertEquals("original", root.getAttribute("attr1"));

        // Attribute with prefix (valid)
        root.setAttribute("xmlns:pfx", "http://pfx.com");
        NodePointer attrNS = rootPtr.createAttribute(context, new QName("pfx", "attr2"));
        Assert.assertNotNull(attrNS);
        Assert.assertEquals("http://pfx.com", root.getAttributeNodeNS("http://pfx.com", "attr2").getNamespaceURI());

        // Attribute with unknown prefix
        try {
            rootPtr.createAttribute(context, new QName("unknown", "attr3"));
            Assert.fail("Expected JXPathException on unknown prefix");
        } catch (JXPathException expected) {
            // expected
        }

        // On non-element node
        Text text = doc.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected JXPathException on non-element attribute creation");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test
    public void testCreateChild() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        // Factory not set
        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory is not set");
        } catch (JXPathException expected) {
            // expected
        }

        // Set custom factory
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer parent, Object parentNode, String name, int index) {
                Element p = (Element) parentNode;
                Element child = p.getOwnerDocument().createElement(name);
                p.appendChild(child);
                return true;
            }
        });

        NodePointer childPtr = rootPtr.createChild(context, new QName("child"), 0);
        Assert.assertNotNull(childPtr);
        Assert.assertEquals("child", childPtr.getName().getName());

        // createChild with value
        NodePointer childWithValue = rootPtr.createChild(context, new QName("childVal"), 0, "text-value");
        Assert.assertEquals("text-value", childWithValue.getValue());

        // Factory returns false
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer parent, Object parentNode, String name, int index) {
                return false;
            }
        });

        try {
            rootPtr.createChild(context, new QName("failChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException when factory fails");
        } catch (JXPathAbstractFactoryException expected) {
            // expected
        }
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = doc.createElement("root");
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");
        Element child1 = doc.createElement("child1");
        Element child2 = doc.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Attr a1 = root.getAttributeNode("attr1");
        Attr a2 = root.getAttributeNode("attr2");
        DOMNodePointer ptrA1 = new DOMNodePointer(rootPtr, a1);
        DOMNodePointer ptrA2 = new DOMNodePointer(rootPtr, a2);

        DOMNodePointer ptrC1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer ptrC2 = new DOMNodePointer(rootPtr, child2);

        // Same pointers
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(ptrC1, ptrC1));

        // Attribute vs Element
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrC1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrC1, ptrA1));

        // Attribute vs Attribute
        int attrComp = rootPtr.compareChildNodePointers(ptrA1, ptrA2);
        Assert.assertTrue(attrComp != 0);

        // Child vs Child
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrC1, ptrC2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrC2, ptrC1));
    }

    @Test
    public void testIterators() {
        Element root = doc.createElement("root");
        root.setAttribute("a", "1");
        root.appendChild(doc.createElement("c"));

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        NodeIterator childIter = rootPtr.childIterator(null, false, null);
        Assert.assertNotNull(childIter);

        NodeIterator attrIter = rootPtr.attributeIterator(new QName("a"));
        Assert.assertNotNull(attrIter);

        NodeIterator nsIter = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIter);

        NodePointer nsPtr = rootPtr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
    }
}
