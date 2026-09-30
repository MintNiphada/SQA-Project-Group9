package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
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
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodePointerTest {

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    @Test
    public void testConstructorsAndBasicGetters() {
        Element root = document.createElement("root");
        document.appendChild(root);

        DOMNodePointer pointerWithLocale = new DOMNodePointer(root, Locale.US);
        Assert.assertSame(root, pointerWithLocale.getBaseValue());
        Assert.assertSame(root, pointerWithLocale.getImmediateNode());
        Assert.assertTrue(pointerWithLocale.isActual());
        Assert.assertFalse(pointerWithLocale.isCollection());
        Assert.assertEquals(1, pointerWithLocale.getLength());
        Assert.assertTrue(pointerWithLocale.isLeaf());

        DOMNodePointer pointerWithId = new DOMNodePointer(root, Locale.US, "myId");
        Assert.assertEquals("id('myId')", pointerWithId.asPath());

        DOMNodePointer childPointer = new DOMNodePointer(pointerWithLocale, root);
        Assert.assertSame(pointerWithLocale, childPointer.getParent());
        Assert.assertSame(root, childPointer.getNode());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element elem1 = document.createElement("node1");
        Element elem2 = document.createElement("node2");
        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr1Same = new DOMNodePointer(elem1, Locale.GERMAN);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.US);

        Assert.assertTrue(ptr1.equals(ptr1));
        Assert.assertTrue(ptr1.equals(ptr1Same));
        Assert.assertFalse(ptr1.equals(ptr2));
        Assert.assertFalse(ptr1.equals("Not a pointer"));
        Assert.assertFalse(ptr1.equals(null));

        Assert.assertEquals(System.identityHashCode(elem1), ptr1.hashCode());
    }

    @Test
    public void testGetName() {
        Element elem = document.createElementNS("http://example.com/ns", "ns:testElem");
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);
        QName qname = elemPtr.getName();
        Assert.assertEquals("ns", qname.getPrefix());
        Assert.assertEquals("testElem", qname.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "piData");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        QName piQName = piPtr.getName();
        Assert.assertNull(piQName.getPrefix());
        Assert.assertEquals("piTarget", piQName.getName());

        Comment comment = document.createComment("a comment");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        QName commentQName = commentPtr.getName();
        Assert.assertNull(commentQName.getPrefix());
        Assert.assertNull(commentQName.getName());
    }

    @Test
    public void testGetPrefixAndLocalNameStatic() {
        Element elem = document.createElement("simple");
        Assert.assertNull(DOMNodePointer.getPrefix(elem));
        Assert.assertEquals("simple", DOMNodePointer.getLocalName(elem));

        Element elemPrefix = document.createElement("pfx:tag");
        Assert.assertEquals("pfx", DOMNodePointer.getPrefix(elemPrefix));
        Assert.assertEquals("tag", DOMNodePointer.getLocalName(elemPrefix));
    }

    @Test
    public void testTestNode() {
        Element elem = document.createElementNS("http://example.com", "ns:elem");
        Text text = document.createTextNode("text");
        CDATASection cdata = document.createCDATASection("cdata");
        Comment comment = document.createComment("comment");
        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "data");

        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);
        Assert.assertTrue(elemPtr.testNode(null));

        // NodeNameTest on non-element
        Assert.assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("test"))));

        // NodeNameTest wildcard without prefix
        Assert.assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName(null, "*"))));

        // NodeNameTest with matching name and namespace
        Assert.assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "elem"), "http://example.com")));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "other"), "http://example.com")));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "elem"), "http://other.com")));

        // Wildcard with prefix
        Assert.assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "*"), "http://example.com")));

        // NodeTypeTest
        Assert.assertTrue(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        Assert.assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Assert.assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        Assert.assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        Assert.assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(999)));

        // ProcessingInstructionTest
        Assert.assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("myTarget")));
        Assert.assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("otherTarget")));
        Assert.assertFalse(DOMNodePointer.testNode(elem, new ProcessingInstructionTest("myTarget")));
    }

    @Test
    public void testNamespaceURILookup() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://default.com");
        root.setAttribute("xmlns:custom", "http://custom.com");
        document.appendChild(root);

        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);

        Assert.assertEquals("http://default.com", childPtr.getDefaultNamespaceURI());
        Assert.assertEquals("http://default.com", childPtr.getNamespaceURI(""));
        Assert.assertEquals("http://default.com", childPtr.getNamespaceURI((String) null));

        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPtr.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPtr.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://custom.com", childPtr.getNamespaceURI("custom"));
        // Cached lookup
        Assert.assertEquals("http://custom.com", childPtr.getNamespaceURI("custom"));

        Assert.assertNull(childPtr.getNamespaceURI("unknownPrefix"));

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("http://default.com", docPtr.getDefaultNamespaceURI());
        Assert.assertEquals("http://custom.com", docPtr.getNamespaceURI("custom"));
    }

    @Test
    public void testNamespaceURIFallbackStatic() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:p", "http://p.com");
        document.appendChild(root);

        Element child = document.createElement("p:child");
        root.appendChild(child);

        Assert.assertEquals("http://p.com", DOMNodePointer.getNamespaceURI(child));
        Assert.assertNull(DOMNodePointer.getNamespaceURI(document.createElement("unbound")));
    }

    @Test
    public void testIterators() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        NodeIterator childIt = rootPtr.childIterator(new NodeTypeTest(Compiler.NODE_TYPE_NODE), false, null);
        Assert.assertNotNull(childIt);

        NodeIterator attrIt = rootPtr.attributeIterator(new QName("test"));
        Assert.assertNotNull(attrIt);

        NodePointer nsPtr = rootPtr.namespacePointer("custom");
        Assert.assertNotNull(nsPtr);

        NodeIterator nsIt = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIt);
    }

    @Test
    public void testLanguage() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("en-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLang = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.US);
        Assert.assertTrue(noLangPtr.isLanguage("en"));
    }

    @Test
    public void testSetValueTextAndCData() {
        Element parent = document.createElement("parent");
        Text text = document.createTextNode("initial");
        parent.appendChild(text);

        DOMNodePointer textPtr = new DOMNodePointer(parent, text);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getNodeValue());

        textPtr.setValue("");
        Assert.assertNull(text.getParentNode());

        CDATASection cdata = document.createCDATASection("cdata-init");
        parent.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(parent, cdata);
        cdataPtr.setValue(null);
        Assert.assertNull(cdata.getParentNode());
    }

    @Test
    public void testSetValueElement() {
        Element parent = document.createElement("parent");
        parent.appendChild(document.createElement("child1"));
        parent.appendChild(document.createTextNode("text"));

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.US);

        // Set string value
        parentPtr.setValue("new text");
        Assert.assertEquals(1, parent.getChildNodes().getLength());
        Assert.assertEquals("new text", parent.getFirstChild().getNodeValue());

        // Set Element value
        Element sourceElem = document.createElement("source");
        sourceElem.appendChild(document.createElement("sourceChild"));
        parentPtr.setValue(sourceElem);
        Assert.assertEquals(1, parent.getChildNodes().getLength());
        Assert.assertEquals("sourceChild", parent.getFirstChild().getNodeName());

        // Set Document value
        Document doc2 = null;
        try {
            doc2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element doc2Elem = doc2.createElement("doc2Root");
            doc2.appendChild(doc2Elem);
        } catch (Exception ignored) {
        }
        parentPtr.setValue(doc2);
        Assert.assertEquals(1, parent.getChildNodes().getLength());
        Assert.assertEquals("doc2Root", parent.getFirstChild().getNodeName());

        // Set non-element Node (e.g. Text node object)
        Text standaloneText = document.createTextNode("standalone");
        parentPtr.setValue(standaloneText);
        Assert.assertEquals(1, parent.getChildNodes().getLength());
        Assert.assertEquals("standalone", parent.getFirstChild().getNodeValue());
    }

    @Test
    public void testGetValue() {
        Comment comment = document.createComment("  a comment  ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("a comment", commentPtr.getValue());

        Text text = document.createTextNode("  sample text  ");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        Assert.assertEquals("sample text", textPtr.getValue());

        CDATASection cdata = document.createCDATASection("  sample cdata  ");
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);
        Assert.assertEquals("sample cdata", cdataPtr.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "  sample pi data  ");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        Assert.assertEquals("sample pi data", piPtr.getValue());

        Element parent = document.createElement("parent");
        parent.appendChild(document.createTextNode("Hello "));
        Element child = document.createElement("child");
        child.appendChild(document.createTextNode("World"));
        parent.appendChild(child);
        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.US);
        Assert.assertEquals("Hello World", parentPtr.getValue());
    }

    @Test
    public void testCreateAttribute() {
        Element element = document.createElement("testElem");
        DOMNodePointer elemPtr = new DOMNodePointer(element, Locale.US);
        JXPathContext context = JXPathContext.newContext(element);

        NodePointer attrPtr = elemPtr.createAttribute(context, new QName("attr1"));
        Assert.assertNotNull(attrPtr);
        Assert.assertTrue(element.hasAttribute("attr1"));

        // Create again without error
        NodePointer attrPtr2 = elemPtr.createAttribute(context, new QName("attr1"));
        Assert.assertNotNull(attrPtr2);

        element.setAttribute("xmlns:p", "http://example.com/p");
        NodePointer nsAttrPtr = elemPtr.createAttribute(context, new QName("p", "attr2"));
        Assert.assertNotNull(nsAttrPtr);

        try {
            elemPtr.createAttribute(context, new QName("unbound", "attr3"));
            Assert.fail("Expected JXPathException for unbound prefix");
        } catch (JXPathException expected) {
            // Success
        }

        Text textNode = document.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(textNode, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected exception when creating attribute on text node");
        } catch (JXPathException expected) {
            // Success
        }
    }

    @Test
    public void testRemove() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(root, child);
        childPtr.remove();
        Assert.assertNull(child.getParentNode());

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        try {
            rootPtr.remove();
            Assert.fail("Expected exception when removing root DOM node");
        } catch (JXPathException expected) {
            // Success
        }
    }

    @Test
    public void testAsPath() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals("", rootPtr.asPath());

        Element child1 = document.createElement("child");
        root.appendChild(child1);
        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        Assert.assertEquals("/child[1]", child1Ptr.asPath());

        Element child2 = document.createElement("child");
        root.appendChild(child2);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);
        Assert.assertEquals("/child[2]", child2Ptr.asPath());

        Text text = document.createTextNode("text1");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, text);
        Assert.assertEquals("/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("piTest", "data");
        root.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, pi);
        Assert.assertEquals("/processing-instruction('piTest')[1]", piPtr.asPath());

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("", docPtr.asPath());

        // Escape test
        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "a'b\"c");
        Assert.assertEquals("id('a&apos;b&quot;c')", idPtr.asPath());
    }

    @Test
    public void testAsPathWithNamespaces() {
        Element root = document.createElementNS("http://example.com/ns", "pfx:root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Element child = document.createElementNS("http://example.com/other", "other:child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        NamespaceResolver resolver = new NamespaceResolver(null);
        resolver.registerNamespace("o", "http://example.com/other");
        childPtr.setNamespaceResolver(resolver);

        Assert.assertEquals("/o:child[1]", childPtr.asPath());

        // Unbound prefix leads to node()[pos]
        NamespaceResolver emptyResolver = new NamespaceResolver(null);
        childPtr.setNamespaceResolver(emptyResolver);
        Assert.assertEquals("/node()[1]", childPtr.asPath());
    }

    @Test
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        Element elemWithId = document.createElement("target");
        elemWithId.setAttribute("id", "targetId");
        root.appendChild(elemWithId);
        document.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(document);

        Pointer ptr = docPtr.getPointerByID(ctx, "targetId");
        Assert.assertNotNull(ptr);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Pointer nullPtr = rootPtr.getPointerByID(ctx, "nonExistent");
        Assert.assertTrue(nullPtr instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        Element parent = document.createElement("parent");
        parent.setAttribute("attr1", "val1");
        parent.setAttribute("attr2", "val2");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.US);

        Attr attr1 = parent.getAttributeNode("attr1");
        Attr attr2 = parent.getAttributeNode("attr2");
        DOMNodePointer attr1Ptr = new DOMNodePointer(parentPtr, attr1);
        DOMNodePointer attr2Ptr = new DOMNodePointer(parentPtr, attr2);

        DOMNodePointer child1Ptr = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(parentPtr, child2);

        // Same pointer
        Assert.assertEquals(0, parentPtr.compareChildNodePointers(child1Ptr, child1Ptr));

        // Attribute vs Attribute
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(attr1Ptr, attr2Ptr));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(attr2Ptr, attr1Ptr));

        // Attribute vs Element
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(attr1Ptr, child1Ptr));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(child1Ptr, attr1Ptr));

        // Element vs Element
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(child1Ptr, child2Ptr));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(child2Ptr, child1Ptr));

        // Node not in parent
        Element outsideNode = document.createElement("outside");
        DOMNodePointer outsidePtr = new DOMNodePointer(parentPtr, outsideNode);
        Assert.assertEquals(0, parentPtr.compareChildNodePointers(outsidePtr, outsideNode.getParentNode() == null ? child1Ptr : outsidePtr));
    }

    @Test
    public void testCreateChild() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        // Missing factory
        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory is missing");
        } catch (JXPathException expected) {
            // Success
        }

        // With factory
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                if (node instanceof Element) {
                    Element el = ((Element) node).getOwnerDocument().createElement(name);
                    ((Element) node).appendChild(el);
                    return true;
                }
                return false;
            }
        });

        NodePointer newChild = rootPtr.createChild(context, new QName("newChild"), 0);
        Assert.assertNotNull(newChild);
        Assert.assertEquals("newChild", ((Element) newChild.getBaseValue()).getNodeName());

        // createChild with value and WHOLE_COLLECTION
        NodePointer valChild = rootPtr.createChild(context, new QName("valChild"), NodePointer.WHOLE_COLLECTION, "hello");
        Assert.assertNotNull(valChild);
        Assert.assertEquals("hello", valChild.getValue());

        // Factory fails
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                return false;
            }
        });

        try {
            rootPtr.createChild(context, new QName("failChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException when factory returns false");
        } catch (JXPathAbstractFactoryException expected) {
            // Success
        }
    }
}
