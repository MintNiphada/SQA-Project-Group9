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
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodePointerTest {

    private Document document;
    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    @Test
    public void testConstructorsAndBasics() {
        Element root = document.createElement("root");
        document.appendChild(root);

        DOMNodePointer ptr1 = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, ptr1.getBaseValue());
        Assert.assertEquals(root, ptr1.getImmediateNode());
        Assert.assertTrue(ptr1.isActual());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertTrue(ptr1.isLeaf());

        DOMNodePointer ptr2 = new DOMNodePointer(root, Locale.GERMANY, "id123");
        Assert.assertEquals("id('id123')", ptr2.asPath());

        DOMNodePointer childPtr = new DOMNodePointer(ptr1, root);
        Assert.assertEquals(ptr1, childPtr.getParent());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element el1 = document.createElement("a");
        Element el2 = document.createElement("b");
        DOMNodePointer ptr1 = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr3 = new DOMNodePointer(el2, Locale.ENGLISH);

        Assert.assertEquals(ptr1, ptr1);
        Assert.assertEquals(ptr1, ptr2);
        Assert.assertEquals(ptr1.hashCode(), ptr2.hashCode());
        Assert.assertNotEquals(ptr1, ptr3);
        Assert.assertNotEquals(ptr1, null);
        Assert.assertNotEquals(ptr1, "not-a-pointer");
    }

    @Test
    public void testGetName() {
        Element el = document.createElementNS("http://example.com/ns", "pfx:item");
        DOMNodePointer ptr = new DOMNodePointer(el, Locale.ENGLISH);
        QName name = ptr.getName();
        Assert.assertEquals("pfx", name.getPrefix());
        Assert.assertEquals("item", name.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertEquals("targetPI", piPtr.getName().getName());

        Text text = document.createTextNode("text");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        Assert.assertNull(textPtr.getName().getName());
    }

    @Test
    public void testGetPrefixAndGetLocalName() {
        Element elNs = document.createElementNS("http://ns", "pfx:local");
        Assert.assertEquals("pfx", DOMNodePointer.getPrefix(elNs));
        Assert.assertEquals("local", DOMNodePointer.getLocalName(elNs));

        Element elSimple = document.createElement("simple");
        Assert.assertNull(DOMNodePointer.getPrefix(elSimple));
        Assert.assertEquals("simple", DOMNodePointer.getLocalName(elSimple));

        Element elPrefixedNoNs = document.createElement("foo:bar");
        Assert.assertEquals("foo", DOMNodePointer.getPrefix(elPrefixedNoNs));
        Assert.assertEquals("bar", DOMNodePointer.getLocalName(elPrefixedNoNs));
    }

    @Test
    public void testTestNode() {
        Element el = document.createElementNS("http://example.com", "ns:elem");
        document.appendChild(el);
        DOMNodePointer ptr = new DOMNodePointer(el, Locale.ENGLISH);

        // null test
        Assert.assertTrue(ptr.testNode(null));

        // NodeNameTest matching
        Assert.assertTrue(ptr.testNode(new NodeNameTest(new QName("ns", "elem"), "http://example.com")));
        Assert.assertFalse(ptr.testNode(new NodeNameTest(new QName("ns", "other"), "http://example.com")));
        Assert.assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "*"))));
        Assert.assertFalse(ptr.testNode(new NodeNameTest(new QName("ns", "*"), "http://other.com")));

        // NodeNameTest on non-element
        Text txt = document.createTextNode("hello");
        el.appendChild(txt);
        DOMNodePointer txtPtr = new DOMNodePointer(ptr, txt);
        Assert.assertFalse(txtPtr.testNode(new NodeNameTest(new QName("elem"))));

        // NodeTypeTest
        Assert.assertTrue(txtPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertTrue(txtPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertFalse(txtPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertFalse(txtPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(txtPtr.testNode(new NodeTypeTest(9999)));

        CDATASection cdata = document.createCDATASection("cdata");
        DOMNodePointer cdataPtr = new DOMNodePointer(ptr, cdata);
        Assert.assertTrue(cdataPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = document.createComment("comment");
        DOMNodePointer commentPtr = new DOMNodePointer(ptr, comment);
        Assert.assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPtr = new DOMNodePointer(ptr, pi);
        Assert.assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        // ProcessingInstructionTest
        Assert.assertTrue(piPtr.testNode(new ProcessingInstructionTest("myTarget")));
        Assert.assertFalse(piPtr.testNode(new ProcessingInstructionTest("otherTarget")));
        Assert.assertFalse(txtPtr.testNode(new ProcessingInstructionTest("myTarget")));
    }

    @Test
    public void testNamespaceURILookup() {
        Element root = document.createElementNS("http://root.com", "root");
        root.setAttribute("xmlns:test", "http://test.com");
        Element child = document.createElementNS("http://child.com", "child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        Assert.assertEquals("http://root.com", rootPtr.getDefaultNamespaceURI());
        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPtr.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPtr.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://test.com", childPtr.getNamespaceURI("test"));
        Assert.assertNull(childPtr.getNamespaceURI("unknownPrefix"));

        // Test with document pointer
        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.ENGLISH);
        Assert.assertEquals("http://test.com", docPtr.getNamespaceURI("test"));
        Assert.assertEquals("http://root.com", docPtr.getDefaultNamespaceURI());
    }

    @Test
    public void testIsLanguage() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(new DOMNodePointer(parent, Locale.ENGLISH), child);
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("EN-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLang = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.GERMAN);
        Assert.assertTrue(noLangPtr.isLanguage("de"));
    }

    @Test
    public void testGetValueAndStringValue() {
        Comment comment = document.createComment("  sample comment  ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertEquals("sample comment", commentPtr.getValue());

        Element elem = document.createElement("elem");
        elem.appendChild(document.createTextNode("  hello  "));
        elem.appendChild(document.createCDATASection("  world  "));
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);
        Assert.assertEquals("helloworld", elemPtr.getValue());

        elem.setAttribute("xml:space", "preserve");
        Assert.assertEquals("  hello    world  ", elemPtr.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "  piData  ");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertEquals("piData", piPtr.getValue());
    }

    @Test
    public void testSetValueTextNode() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getNodeValue());

        textPtr.setValue("");
        Assert.assertNull(text.getParentNode());
    }

    @Test
    public void testSetValueElementNode() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        // Set string value
        rootPtr.setValue("new text");
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals("new text", root.getFirstChild().getNodeValue());

        // Set Node value (Element)
        Element replacement = document.createElement("replacement");
        replacement.appendChild(document.createElement("sub1"));
        replacement.appendChild(document.createElement("sub2"));
        rootPtr.setValue(replacement);
        Assert.assertEquals(2, root.getChildNodes().getLength());

        // Set Node value (Other node like Comment)
        Comment c = document.createComment("new-comment");
        rootPtr.setValue(c);
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals(Node.COMMENT_NODE, root.getFirstChild().getNodeType());

        // Set empty string removes children and adds nothing
        rootPtr.setValue("");
        Assert.assertEquals(0, root.getChildNodes().getLength());
    }

    @Test
    public void testAsPath() {
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child1 = document.createElement("child");
        Element child2 = document.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.ENGLISH);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);
        DOMNodePointer c1Ptr = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer c2Ptr = new DOMNodePointer(rootPtr, child2);

        Assert.assertEquals("", docPtr.asPath());
        Assert.assertEquals("/child[1]", c1Ptr.asPath());
        Assert.assertEquals("/child[2]", c2Ptr.asPath());

        Text text1 = document.createTextNode("t1");
        Text text2 = document.createTextNode("t2");
        child2.appendChild(text1);
        child2.appendChild(text2);
        DOMNodePointer t2Ptr = new DOMNodePointer(c2Ptr, text2);
        Assert.assertEquals("/child[2]/text()[2]", t2Ptr.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "data");
        child2.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(c2Ptr, pi);
        Assert.assertEquals("/child[2]/processing-instruction('piTarget')[1]", piPtr.asPath());
    }

    @Test
    public void testAsPathWithNamespaces() {
        Element root = document.createElementNS("http://example.com", "ns:root");
        document.appendChild(root);
        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.ENGLISH);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);

        Element child = document.createElementNS("http://example.com", "ns:child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        rootPtr.getNamespaceResolver().registerNamespace("ex", "http://example.com");
        Assert.assertEquals("/ex:child[1]", childPtr.asPath());
    }

    @Test
    public void testCreateAttribute() {
        Element elem = document.createElement("testElem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        ptr.getNamespaceResolver().registerNamespace("myNs", "http://ns.com");

        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attr1 = ptr.createAttribute(context, new QName("attr1"));
        Assert.assertNotNull(attr1);
        Assert.assertTrue(elem.hasAttribute("attr1"));

        NodePointer attrNs = ptr.createAttribute(context, new QName("myNs", "attr2"));
        Assert.assertNotNull(attrNs);
        Assert.assertTrue(elem.hasAttributeNS("http://ns.com", "attr2"));

        try {
            ptr.createAttribute(context, new QName("unregistered", "attr3"));
            Assert.fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            // Success
        }

        Text text = document.createTextNode("some-text");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected exception when creating attribute on non-element");
        } catch (Exception expected) {
            // Success
        }
    }

    @Test
    public void testRemove() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        childPtr.remove();
        Assert.assertNull(child.getParentNode());

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        try {
            rootPtr.remove();
            Assert.fail("Expected JXPathException when removing root node");
        } catch (JXPathException expected) {
            Assert.assertTrue(expected.getMessage().contains("Cannot remove root DOM node"));
        }
    }

    @Test
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        Element target = document.createElement("target");
        target.setAttribute("id", "targetId");
        target.setIdAttribute("id", true);
        root.appendChild(target);
        document.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer p1 = docPtr.getPointerByID(context, "targetId");
        Assert.assertTrue(p1 instanceof DOMNodePointer);
        Assert.assertEquals(target, p1.getNode());

        Pointer p2 = docPtr.getPointerByID(context, "nonExistent");
        Assert.assertTrue(p2 instanceof NullPointer);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        Pointer p3 = rootPtr.getPointerByID(context, "targetId");
        Assert.assertEquals(target, p3.getNode());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = document.createElement("root");
        root.setAttribute("a1", "v1");
        root.setAttribute("a2", "v2");
        Element c1 = document.createElement("c1");
        Element c2 = document.createElement("c2");
        root.appendChild(c1);
        root.appendChild(c2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Attr a1 = root.getAttributeNode("a1");
        Attr a2 = root.getAttributeNode("a2");
        DOMNodePointer a1Ptr = new DOMNodePointer(rootPtr, a1);
        DOMNodePointer a2Ptr = new DOMNodePointer(rootPtr, a2);
        DOMNodePointer c1Ptr = new DOMNodePointer(rootPtr, c1);
        DOMNodePointer c2Ptr = new DOMNodePointer(rootPtr, c2);

        Assert.assertEquals(0, rootPtr.compareChildNodePointers(c1Ptr, c1Ptr));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(c1Ptr, c2Ptr));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(c2Ptr, c1Ptr));

        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(a1Ptr, c1Ptr));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(c1Ptr, a1Ptr));

        int attrComp = rootPtr.compareChildNodePointers(a1Ptr, a2Ptr);
        Assert.assertTrue(attrComp == -1 || attrComp == 1);
    }

    @Test
    public void testIterators() {
        Element root = document.createElement("root");
        root.setAttribute("attr", "val");
        root.setAttribute("xmlns:pfx", "http://pfx.com");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        NodeIterator childIt = rootPtr.childIterator(null, false, null);
        Assert.assertNotNull(childIt);

        NodeIterator attrIt = rootPtr.attributeIterator(new QName("attr"));
        Assert.assertNotNull(attrIt);

        NodeIterator nsIt = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIt);

        NodePointer nsPtr = rootPtr.namespacePointer("pfx");
        Assert.assertNotNull(nsPtr);

        NamespaceResolver nsr = rootPtr.getNamespaceResolver();
        Assert.assertNotNull(nsr);
        Assert.assertSame(nsr, rootPtr.getNamespaceResolver());
    }

    @Test
    public void testCreateChild() {
        Element root = document.createElement("root");
        document.appendChild(root);

        JXPathContext context = JXPathContext.newContext(document);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                if (node instanceof Element) {
                    Element el = (Element) node;
                    Element newChild = el.getOwnerDocument().createElement(name);
                    el.appendChild(newChild);
                    return true;
                }
                return false;
            }
        });

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        NodePointer created = rootPtr.createChild(context, new QName("newElem"), 0);
        Assert.assertNotNull(created);
        Assert.assertEquals("newElem", ((Element) created.getNode()).getTagName());

        NodePointer createdWithValue = rootPtr.createChild(context, new QName("valElem"), 0, "sampleValue");
        Assert.assertNotNull(createdWithValue);
        Assert.assertEquals("sampleValue", createdWithValue.getValue());
    }

    @Test
    public void testCreateChildFactoryFailure() {
        Element root = document.createElement("root");
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                return false;
            }
        });

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        try {
            rootPtr.createChild(context, new QName("failChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException expected) {
            Assert.assertTrue(expected.getMessage().contains("Factory could not create a child node"));
        }
    }
}
