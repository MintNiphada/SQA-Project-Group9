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

    private Document doc;
    private Element root;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();

        root = doc.createElementNS("http://example.com/ns", "ns:root");
        root.setAttribute("id", "root-id");
        root.setAttribute("xml:lang", "en-US");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://example.com/foo");
        doc.appendChild(root);
    }

    @Test
    public void testConstructorsAndBasicGetters() {
        DOMNodePointer ptr1 = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, ptr1.getBaseValue());
        Assert.assertEquals(root, ptr1.getImmediateNode());
        Assert.assertTrue(ptr1.isActual());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertEquals(Locale.US, ptr1.getLocale());

        DOMNodePointer ptr2 = new DOMNodePointer(root, Locale.GERMANY, "customId");
        Assert.assertEquals("id('customId')", ptr2.asPath());

        DOMNodePointer ptrChild = new DOMNodePointer(ptr1, root);
        Assert.assertEquals(ptr1, ptrChild.getParent());
    }

    @Test
    public void testIsLeaf() {
        Element emptyElem = doc.createElement("empty");
        DOMNodePointer ptrEmpty = new DOMNodePointer(emptyElem, Locale.US);
        Assert.assertTrue(ptrEmpty.isLeaf());

        emptyElem.appendChild(doc.createTextNode("text"));
        Assert.assertFalse(ptrEmpty.isLeaf());
    }

    @Test
    public void testGetName() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        QName rootName = rootPtr.getName();
        Assert.assertEquals("ns", rootName.getPrefix());
        Assert.assertEquals("root", rootName.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        QName piName = piPtr.getName();
        Assert.assertNull(piName.getPrefix());
        Assert.assertEquals("target", piName.getName());

        Text text = doc.createTextNode("content");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        QName textName = textPtr.getName();
        Assert.assertNull(textName.getPrefix());
        Assert.assertNull(textName.getName());
    }

    @Test
    public void testGetLocalNameAndPrefixFallback() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        try {
            Document nonNsDoc = factory.newDocumentBuilder().newDocument();
            Element elem = nonNsDoc.createElement("myprefix:myelem");
            Assert.assertEquals("myprefix", DOMNodePointer.getPrefix(elem));
            Assert.assertEquals("myelem", DOMNodePointer.getLocalName(elem));

            Element elemNoPrefix = nonNsDoc.createElement("simple");
            Assert.assertNull(DOMNodePointer.getPrefix(elemNoPrefix));
            Assert.assertEquals("simple", DOMNodePointer.getLocalName(elemNoPrefix));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetNamespaceURIStatic() {
        Assert.assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(doc));
        Assert.assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(root));

        Element child = doc.createElement("child");
        root.appendChild(child);
        Assert.assertNull(DOMNodePointer.getNamespaceURI(child));
    }

    @Test
    public void testGetNamespaceURIByPrefix() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPtr.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPtr.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://example.com/ns", rootPtr.getNamespaceURI("ns"));
        Assert.assertEquals("http://example.com/foo", rootPtr.getNamespaceURI("foo"));
        Assert.assertNull(rootPtr.getNamespaceURI("unknownPrefix"));

        // cached prefix retrieval
        Assert.assertEquals("http://example.com/ns", rootPtr.getNamespaceURI("ns"));
        Assert.assertNull(rootPtr.getNamespaceURI("unknownPrefix"));

        // Document target
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        Assert.assertEquals("http://example.com/ns", docPtr.getNamespaceURI("ns"));

        // Default namespace test
        root.setAttribute("xmlns", "http://example.com/default");
        DOMNodePointer defaultNsPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals("http://example.com/default", defaultNsPtr.getNamespaceURI(""));
        Assert.assertEquals("http://example.com/default", defaultNsPtr.getNamespaceURI(null));
        Assert.assertEquals("http://example.com/default", defaultNsPtr.getDefaultNamespaceURI());

        Element unparentedElem = doc.createElement("unparented");
        DOMNodePointer unparentedPtr = new DOMNodePointer(unparentedElem, Locale.US);
        Assert.assertNull(unparentedPtr.getDefaultNamespaceURI());
    }

    @Test
    public void testTestNode() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        // Null test matches everything
        Assert.assertTrue(rootPtr.testNode(null));
        Assert.assertTrue(DOMNodePointer.testNode(root, null));

        // NodeNameTest
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName("ns", "root"), "http://example.com/ns")));
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName("root"), "http://example.com/ns")));
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName(null, "*"))));
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName("ns", "*"), "http://example.com/ns")));
        Assert.assertFalse(rootPtr.testNode(new NodeNameTest(new QName("other"))));
        Assert.assertFalse(rootPtr.testNode(new NodeNameTest(new QName("root"), "http://other.com")));

        // Test non-element against NodeNameTest
        Text text = doc.createTextNode("hello");
        Assert.assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("hello"))));

        // NodeTypeTest
        Assert.assertTrue(DOMNodePointer.testNode(root, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertTrue(DOMNodePointer.testNode(doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        Assert.assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        CDATASection cdata = doc.createCDATASection("data");
        Assert.assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertFalse(DOMNodePointer.testNode(root, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = doc.createComment("a comment");
        Assert.assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertFalse(DOMNodePointer.testNode(root, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        Assert.assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(DOMNodePointer.testNode(root, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(DOMNodePointer.testNode(root, new NodeTypeTest(9999)));

        // ProcessingInstructionTest
        Assert.assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("target")));
        Assert.assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("other")));
        Assert.assertFalse(DOMNodePointer.testNode(root, new ProcessingInstructionTest("target")));
    }

    @Test
    public void testIsLanguage() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertTrue(rootPtr.isLanguage("en"));
        Assert.assertTrue(rootPtr.isLanguage("en-US"));
        Assert.assertFalse(rootPtr.isLanguage("fr"));

        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        Assert.assertTrue(childPtr.isLanguage("en"));

        Element noLang = doc.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.US);
        Assert.assertTrue(noLangPtr.isLanguage("en"));
    }

    @Test
    public void testGetValue() {
        Comment comment = doc.createComment(" comment text ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("comment text", commentPtr.getValue());

        Element elem = doc.createElement("elem");
        elem.appendChild(doc.createTextNode("  hello  "));
        elem.appendChild(doc.createComment(" ignore "));
        elem.appendChild(doc.createCDATASection("  world  "));
        elem.appendChild(doc.createProcessingInstruction("pi", " instruction "));
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);
        Assert.assertEquals("hello  worldinstruction", elemPtr.getValue());

        // xml:space='preserve'
        elem.setAttribute("xml:space", "preserve");
        Assert.assertEquals("  hello    world   instruction ", elemPtr.getValue());
    }

    @Test
    public void testSetValueTextAndCData() {
        Text text = doc.createTextNode("initial");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getNodeValue());

        textPtr.setValue("");
        Assert.assertNull(text.getParentNode());

        CDATASection cdata = doc.createCDATASection("initial-cdata");
        root.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);
        cdataPtr.setValue("updated-cdata");
        Assert.assertEquals("updated-cdata", cdata.getNodeValue());
        cdataPtr.setValue(null);
        Assert.assertNull(cdata.getParentNode());
    }

    @Test
    public void testSetValueElement() {
        Element target = doc.createElement("target");
        root.appendChild(target);
        DOMNodePointer targetPtr = new DOMNodePointer(target, Locale.US);

        targetPtr.setValue("simple text");
        Assert.assertEquals(1, target.getChildNodes().getLength());
        Assert.assertEquals("simple text", target.getFirstChild().getNodeValue());

        // Set value from another Element
        Element sourceElem = doc.createElement("source");
        sourceElem.appendChild(doc.createElement("child1"));
        sourceElem.appendChild(doc.createElement("child2"));
        targetPtr.setValue(sourceElem);
        Assert.assertEquals(2, target.getChildNodes().getLength());

        // Set value from another node type (Comment)
        Comment c = doc.createComment("my-comment");
        targetPtr.setValue(c);
        Assert.assertEquals(1, target.getChildNodes().getLength());
        Assert.assertEquals(Node.COMMENT_NODE, target.getFirstChild().getNodeType());

        // Set empty string clears children
        targetPtr.setValue("");
        Assert.assertEquals(0, target.getChildNodes().getLength());
    }

    @Test
    public void testCreateAttribute() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        NodePointer attrPtr = rootPtr.createAttribute(context, new QName("testAttr"));
        Assert.assertNotNull(attrPtr);
        Assert.assertTrue(root.hasAttribute("testAttr"));

        NodePointer nsAttrPtr = rootPtr.createAttribute(context, new QName("ns", "testAttr2"));
        Assert.assertNotNull(nsAttrPtr);
        Assert.assertTrue(root.hasAttributeNS("http://example.com/ns", "testAttr2"));

        try {
            rootPtr.createAttribute(context, new QName("unknown", "attr"));
            Assert.fail("Expected exception for unknown prefix");
        } catch (JXPathException expected) {
            Assert.assertTrue(expected.getMessage().contains("Unknown namespace prefix"));
        }

        Text text = doc.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected exception creating attribute on non-element");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test
    public void testRemove() {
        Element child = doc.createElement("toRemove");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        childPtr.remove();
        Assert.assertNull(child.getParentNode());

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        try {
            docPtr.remove();
            Assert.fail("Should throw exception when removing root DOM node");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot remove root DOM node"));
        }
    }

    @Test
    public void testAsPath() {
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        Assert.assertEquals("", docPtr.asPath());

        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);
        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("ns", "http://example.com/ns");
        rootPtr.getNamespaceResolver().registerNamespace("ns", "http://example.com/ns");

        Assert.assertEquals("/ns:root[1]", rootPtr.asPath());

        Element child1 = doc.createElement("child");
        Element child2 = doc.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer childPtr1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer childPtr2 = new DOMNodePointer(rootPtr, child2);
        Assert.assertEquals("/ns:root[1]/child[1]", child1.getParentNode() != null ? childPtr1.asPath() : "");
        Assert.assertEquals("/ns:root[1]/child[2]", childPtr2.asPath());

        Text t1 = doc.createTextNode("t1");
        Text t2 = doc.createTextNode("t2");
        child1.appendChild(t1);
        child1.appendChild(t2);
        DOMNodePointer t2Ptr = new DOMNodePointer(childPtr1, t2);
        Assert.assertEquals("/ns:root[1]/child[1]/text()[2]", t2Ptr.asPath());

        ProcessingInstruction pi = doc.createProcessingInstruction("my-target", "data");
        child1.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(childPtr1, pi);
        Assert.assertEquals("/ns:root[1]/child[1]/processing-instruction('my-target')[1]", piPtr.asPath());

        // Escape test in ID
        DOMNodePointer escapedIdPtr = new DOMNodePointer(root, Locale.US, "a'b\"c");
        Assert.assertEquals("id('a&apos;b&quot;c')", escapedIdPtr.asPath());
    }

    @Test
    public void testEqualsAndHashCode() {
        DOMNodePointer ptr1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer ptr2 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer ptrDoc = new DOMNodePointer(doc, Locale.US);

        Assert.assertTrue(ptr1.equals(ptr1));
        Assert.assertTrue(ptr1.equals(ptr2));
        Assert.assertFalse(ptr1.equals(ptrDoc));
        Assert.assertFalse(ptr1.equals(null));
        Assert.assertFalse(ptr1.equals("Not a pointer"));

        Assert.assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    @Test
    public void testIteratorsAndNamespacePointer() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertNotNull(rootPtr.childIterator(null, false, null));
        Assert.assertNotNull(rootPtr.attributeIterator(new QName("test")));
        Assert.assertNotNull(rootPtr.namespaceIterator());
        Assert.assertNotNull(rootPtr.namespacePointer("ns"));
    }

    @Test
    public void testGetPointerByID() {
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        Pointer p1 = docPtr.getPointerByID(context, "root-id");
        Assert.assertNotNull(p1);

        Pointer pNull = docPtr.getPointerByID(context, "non-existent");
        Assert.assertTrue(pNull instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        Element child1 = doc.createElement("c1");
        Element child2 = doc.createElement("c2");
        root.appendChild(child1);
        root.appendChild(child2);

        root.setAttribute("attr1", "v1");
        root.setAttribute("attr2", "v2");

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        DOMNodePointer pChild1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(rootPtr, child2);

        Attr attr1 = root.getAttributeNode("attr1");
        Attr attr2 = root.getAttributeNode("attr2");
        DOMNodePointer pAttr1 = new DOMNodePointer(rootPtr, attr1);
        DOMNodePointer pAttr2 = new DOMNodePointer(rootPtr, attr2);

        // Same node
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pChild1, pChild1));

        // Child element comparison
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pChild1, pChild2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild2, pChild1));

        // Attribute vs Element
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pChild1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild1, pAttr1));

        // Attribute vs Attribute
        int attrComp = rootPtr.compareChildNodePointers(pAttr1, pAttr2);
        Assert.assertTrue(attrComp == -1 || attrComp == 1);
        Assert.assertEquals(-attrComp, rootPtr.compareChildNodePointers(pAttr2, pAttr1));
    }

    @Test
    public void testCreateChild() {
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        // No factory set -> exception
        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory is not set");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Factory is not set"));
        }

        // Factory returning false -> exception
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object contextBean, String name, int index) {
                return false;
            }
        });
        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory returns false");
        } catch (JXPathAbstractFactoryException e) {
            Assert.assertTrue(e.getMessage().contains("Factory could not create"));
        }

        // Factory successfully creating object
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object contextBean, String name, int index) {
                Element newElem = doc.createElement(name);
                ((Node) contextBean).appendChild(newElem);
                return true;
            }
        });
        NodePointer created = rootPtr.createChild(context, new QName("newChild"), 0);
        Assert.assertNotNull(created);
        Assert.assertEquals("newChild", created.getName().getName());

        NodePointer createdWithValue = rootPtr.createChild(context, new QName("withValue"), 1, "testValue");
        Assert.assertNotNull(createdWithValue);
        Assert.assertEquals("testValue", createdWithValue.getValue());
    }
}
