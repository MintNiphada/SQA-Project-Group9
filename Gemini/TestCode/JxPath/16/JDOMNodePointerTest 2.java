package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.junit.Assert;
import org.junit.Test;

public class JDOMNodePointerTest {

    @Test
    public void testConstructorsAndBasics() {
        Element element = new Element("root");
        JDOMNodePointer ptr1 = new JDOMNodePointer(element, Locale.ENGLISH);
        Assert.assertEquals(element, ptr1.getBaseValue());
        Assert.assertEquals(element, ptr1.getImmediateNode());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertTrue(ptr1.isLeaf());

        JDOMNodePointer ptr2 = new JDOMNodePointer(element, Locale.US, "myId");
        Assert.assertEquals("id('myId')", ptr2.asPath());

        JDOMNodePointer childPtr = new JDOMNodePointer(ptr1, new Element("child"));
        Assert.assertEquals(ptr1, childPtr.getParent());
    }

    @Test
    public void testIsLeaf() {
        Element element = new Element("root");
        JDOMNodePointer elemPtr = new JDOMNodePointer(element, Locale.ENGLISH);
        Assert.assertTrue(elemPtr.isLeaf());

        element.addContent(new Element("child"));
        Assert.assertFalse(elemPtr.isLeaf());

        Document doc = new Document();
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        Assert.assertTrue(docPtr.isLeaf());

        doc.setRootElement(new Element("docRoot"));
        Assert.assertFalse(docPtr.isLeaf());

        Comment comment = new Comment("comment");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertTrue(commentPtr.isLeaf());
    }

    @Test
    public void testGetName() {
        Element elemWithoutNs = new Element("root");
        JDOMNodePointer p1 = new JDOMNodePointer(elemWithoutNs, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, "root"), p1.getName());

        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elemWithNs = new Element("item", ns);
        JDOMNodePointer p2 = new JDOMNodePointer(elemWithNs, Locale.ENGLISH);
        Assert.assertEquals(new QName("pfx", "item"), p2.getName());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer p3 = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, "target"), p3.getName());

        Text text = new Text("hello");
        JDOMNodePointer p4 = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, null), p4.getName());
    }

    @Test
    public void testGetNamespaceURI() {
        Element elem = new Element("root");
        JDOMNodePointer p1 = new JDOMNodePointer(elem, Locale.ENGLISH);
        Assert.assertNull(p1.getNamespaceURI());

        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elemWithNs = new Element("item", ns);
        JDOMNodePointer p2 = new JDOMNodePointer(elemWithNs, Locale.ENGLISH);
        Assert.assertEquals("http://example.com", p2.getNamespaceURI());

        Text text = new Text("hello");
        JDOMNodePointer p3 = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertNull(p3.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIWithPrefix() {
        Namespace ns = Namespace.getNamespace("test", "http://test.com");
        Element root = new Element("root");
        root.addNamespaceDeclaration(ns);
        Document doc = new Document(root);

        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        Assert.assertEquals(Namespace.XML_NAMESPACE.getURI(), docPtr.getNamespaceURI("xml"));
        Assert.assertEquals("http://test.com", docPtr.getNamespaceURI("test"));
        Assert.assertNull(docPtr.getNamespaceURI("unknown"));

        JDOMNodePointer elemPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals("http://test.com", elemPtr.getNamespaceURI("test"));
        Assert.assertNull(elemPtr.getNamespaceURI("unknown"));

        Text text = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertEquals(Namespace.XML_NAMESPACE.getURI(), textPtr.getNamespaceURI("xml"));
        Assert.assertNull(textPtr.getNamespaceURI("test"));
    }

    @Test
    public void testGetNamespaceResolver() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);
        NamespaceResolver resolver = ptr.getNamespaceResolver();
        Assert.assertNotNull(resolver);
        Assert.assertSame(resolver, ptr.getNamespaceResolver());
    }

    @Test
    public void testIterators() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        root.setAttribute("attr", "val");

        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = ptr.childIterator(new NodeNameTest(new QName("child")), false, null);
        Assert.assertNotNull(childIt);
        Assert.assertTrue(childIt.setPosition(1));
        Assert.assertEquals(child, childIt.getNodePointer().getNode());

        NodeIterator attrIt = ptr.attributeIterator(new QName("attr"));
        Assert.assertNotNull(attrIt);
        Assert.assertTrue(attrIt.setPosition(1));
        Assert.assertEquals("attr", ((Attribute) attrIt.getNodePointer().getNode()).getName());

        NodeIterator nsIt = ptr.namespaceIterator();
        Assert.assertNotNull(nsIt);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
        Assert.assertTrue(nsPtr instanceof JDOMNamespacePointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = new Element("root");
        Attribute a1 = new Attribute("a1", "v1");
        Attribute a2 = new Attribute("a2", "v2");
        root.setAttribute(a1);
        root.setAttribute(a2);

        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        root.addContent(c1);
        root.addContent(c2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        NodePointer ptrA1 = new JDOMNodePointer(rootPtr, a1);
        NodePointer ptrA2 = new JDOMNodePointer(rootPtr, a2);
        NodePointer ptrC1 = new JDOMNodePointer(rootPtr, c1);
        NodePointer ptrC2 = new JDOMNodePointer(rootPtr, c2);

        // Same nodes
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(ptrA1, ptrA1));
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(ptrC1, ptrC1));

        // Attribute vs Non-attribute
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrC1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(c1 != null ? ptrC1 : null, ptrA1));

        // Attribute vs Attribute
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrA2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrA2, ptrA1));

        // Element vs Element
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrC1, ptrC2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrC2, ptrC1));

        // Non-element parent comparison failure
        JDOMNodePointer nonElemParent = new JDOMNodePointer("StringNode", Locale.ENGLISH);
        try {
            nonElemParent.compareChildNodePointers(ptrC1, ptrC2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("compareChildNodes called for"));
        }
    }

    @Test
    public void testGetValue() {
        Element root = new Element("root");
        root.addContent(new Text("Hello "));
        Element sub = new Element("sub");
        sub.addContent(new Text("World"));
        root.addContent(sub);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals("Hello World", rootPtr.getValue());

        Comment comment = new Comment("  a comment  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertEquals("a comment", commentPtr.getValue());

        Text text = new Text("  some text  ");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertEquals("some text", textPtr.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  some data  ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertEquals("some data", piPtr.getValue());

        // Test preserve space attribute
        Element parentWithSpace = new Element("parent");
        parentWithSpace.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        Text childText = new Text("  preserved  ");
        parentWithSpace.addContent(childText);
        JDOMNodePointer childTextPtr = new JDOMNodePointer(childText, Locale.ENGLISH);
        Assert.assertEquals("  preserved  ", childTextPtr.getValue());
    }

    @Test
    public void testSetValue() {
        Element root = new Element("root");
        Text text = new Text("initial");
        root.addContent(text);

        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getText());

        textPtr.setValue("");
        Assert.assertEquals(0, root.getContent().size());

        JDOMNodePointer elemPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        // setValue with Element
        Element other = new Element("other");
        other.addContent(new Element("nested"));
        elemPtr.setValue(other);
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("nested", ((Element) root.getContent().get(0)).getName());

        // setValue with Document
        Document doc = new Document();
        Element docRoot = new Element("docRoot");
        docRoot.addContent(new Text("docText"));
        doc.setRootElement(docRoot);
        elemPtr.setValue(doc);
        Assert.assertEquals(1, root.getContent().size());

        // setValue with Text
        elemPtr.setValue(new Text("textObj"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("textObj", ((Text) root.getContent().get(0)).getText());

        // setValue with CDATA
        elemPtr.setValue(new CDATA("cdataObj"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("cdataObj", ((Text) root.getContent().get(0)).getText());

        // setValue with PI
        elemPtr.setValue(new ProcessingInstruction("piTarget", "piData"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof ProcessingInstruction);

        // setValue with Comment
        elemPtr.setValue(new Comment("aComment"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof Comment);

        // setValue with plain string
        elemPtr.setValue("simpleString");
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("simpleString", ((Text) root.getContent().get(0)).getText());
    }

    @Test
    public void testTestNode() {
        Element elem = new Element("testElem");
        Document doc = new Document();
        Text text = new Text("text");
        CDATA cdata = new CDATA("cdata");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        // Null test
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, null));

        // NodeTypeTest
        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, nodeTest));
        Assert.assertTrue(JDOMNodePointer.testNode(null, doc, nodeTest));
        Assert.assertFalse(JDOMNodePointer.testNode(null, text, nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        Assert.assertTrue(JDOMNodePointer.testNode(null, text, textTest));
        Assert.assertTrue(JDOMNodePointer.testNode(null, cdata, textTest));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, textTest));

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        Assert.assertTrue(JDOMNodePointer.testNode(null, comment, commentTest));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, commentTest));

        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        Assert.assertTrue(JDOMNodePointer.testNode(null, pi, piTest));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, piTest));

        NodeTypeTest unknownTypeTest = new NodeTypeTest(999);
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, unknownTypeTest));

        // ProcessingInstructionTest
        ProcessingInstructionTest piNameTestMatch = new ProcessingInstructionTest("target");
        ProcessingInstructionTest piNameTestMismatch = new ProcessingInstructionTest("other");
        Assert.assertTrue(JDOMNodePointer.testNode(null, pi, piNameTestMatch));
        Assert.assertFalse(JDOMNodePointer.testNode(null, pi, piNameTestMismatch));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, piNameTestMatch));

        // NodeNameTest
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, wildcardTest));
        Assert.assertFalse(JDOMNodePointer.testNode(null, text, wildcardTest));

        NodeNameTest matchNameTest = new NodeNameTest(new QName(null, "testElem"));
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, matchNameTest));

        NodeNameTest mismatchNameTest = new NodeNameTest(new QName(null, "otherElem"));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, mismatchNameTest));

        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element nsElem = new Element("myElem", ns);
        NodeNameTest nsTest = new NodeNameTest(new QName("pfx", "myElem"), "http://example.com");
        Assert.assertTrue(JDOMNodePointer.testNode(null, nsElem, nsTest));

        NodeNameTest nsMismatchTest = new NodeNameTest(new QName("pfx", "myElem"), "http://wrong.com");
        Assert.assertFalse(JDOMNodePointer.testNode(null, nsElem, nsMismatchTest));

        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        Assert.assertTrue(ptr.testNode(matchNameTest));
    }

    @Test
    public void testGetPrefixAndGetLocalName() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elem = new Element("tag", ns);
        Attribute attr = new Attribute("attr", "val", ns);

        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(elem));
        Assert.assertEquals("tag", JDOMNodePointer.getLocalName(elem));
        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        Assert.assertEquals("attr", JDOMNodePointer.getLocalName(attr));

        Element noNsElem = new Element("tag");
        Attribute noNsAttr = new Attribute("attr", "val");
        Assert.assertNull(JDOMNodePointer.getPrefix(noNsElem));
        Assert.assertNull(JDOMNodePointer.getPrefix(noNsAttr));

        Text text = new Text("sample");
        Assert.assertNull(JDOMNodePointer.getPrefix(text));
        Assert.assertNull(JDOMNodePointer.getLocalName(text));
    }

    @Test
    public void testIsLanguage() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPtr = new JDOMNodePointer(child, Locale.ENGLISH);
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("en-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLangElem = new Element("root2");
        JDOMNodePointer noLangPtr = new JDOMNodePointer(noLangElem, Locale.GERMAN);
        Assert.assertTrue(noLangPtr.isLanguage("de"));
    }

    @Test
    public void testCreateAttribute() {
        Element elem = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);

        // Simple attribute
        NodePointer attrPtr = ptr.createAttribute(context, new QName("myAttr"));
        Assert.assertNotNull(attrPtr);
        Assert.assertEquals("myAttr", ((Attribute) attrPtr.getNode()).getName());

        // Repeated creation returns existing
        NodePointer attrPtr2 = ptr.createAttribute(context, new QName("myAttr"));
        Assert.assertEquals(attrPtr.getNode(), attrPtr2.getNode());

        // Attribute with prefix
        context.registerNamespace("pfx", "http://example.com");
        ptr.getNamespaceResolver().registerNamespace("pfx", "http://example.com");
        NodePointer nsAttrPtr = ptr.createAttribute(context, new QName("pfx", "nsAttr"));
        Assert.assertNotNull(nsAttrPtr);
        Assert.assertEquals("nsAttr", ((Attribute) nsAttrPtr.getNode()).getName());
        Assert.assertEquals("pfx", ((Attribute) nsAttrPtr.getNode()).getNamespacePrefix());

        // Unknown prefix throws exception
        try {
            ptr.createAttribute(context, new QName("unknown", "attr"));
            Assert.fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown namespace prefix: unknown"));
        }
    }

    @Test
    public void testCreateChild() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);

        // Factory not set exception
        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory is not set");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Factory is not set"));
        }

        // With factory
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer parent, Object n, String name, int index) {
                ((Element) n).addContent(new Element(name));
                return true;
            }
        });

        NodePointer created = rootPtr.createChild(context, new QName("child"), 0);
        Assert.assertNotNull(created);
        Assert.assertEquals("child", ((Element) created.getNode()).getName());

        // createChild with value
        NodePointer createdWithValue = rootPtr.createChild(context, new QName("child2"), 0, "childValue");
        Assert.assertNotNull(createdWithValue);
        Assert.assertEquals("childValue", createdWithValue.getValue());

        // Factory fails
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer parent, Object n, String name, int index) {
                return false;
            }
        });
        try {
            rootPtr.createChild(context, new QName("failingChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            Assert.assertTrue(e.getMessage().contains("Factory could not create"));
        }
    }

    @Test
    public void testRemove() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        Assert.assertEquals(1, root.getContent().size());
        childPtr.remove();
        Assert.assertEquals(0, root.getContent().size());

        try {
            rootPtr.remove();
            Assert.fail("Expected JXPathException when removing root node");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot remove root JDOM node"));
        }
    }

    @Test
    public void testAsPath() {
        Element root = new Element("root");
        Element c1 = new Element("child");
        Element c2 = new Element("child");
        Text text = new Text("hello");
        CDATA cdata = new CDATA("cdata text");
        ProcessingInstruction pi = new ProcessingInstruction("piTarget", "data");

        root.addContent(c1);
        root.addContent(c2);
        root.addContent(text);
        root.addContent(cdata);
        root.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals("", rootPtr.asPath());

        JDOMNodePointer c1Ptr = new JDOMNodePointer(rootPtr, c1);
        Assert.assertEquals("/child[1]", c1Ptr.asPath());

        JDOMNodePointer c2Ptr = new JDOMNodePointer(rootPtr, c2);
        Assert.assertEquals("/child[2]", c2Ptr.asPath());

        JDOMNodePointer textPtr = new JDOMNodePointer(rootPtr, text);
        Assert.assertEquals("/text()[1]", textPtr.asPath());

        JDOMNodePointer cdataPtr = new JDOMNodePointer(rootPtr, cdata);
        Assert.assertEquals("/text()[2]", cdataPtr.asPath());

        JDOMNodePointer piPtr = new JDOMNodePointer(rootPtr, pi);
        Assert.assertEquals("/processing-instruction('piTarget')[1]", piPtr.asPath());

        // Escape test in ID
        JDOMNodePointer idPtr = new JDOMNodePointer(root, Locale.ENGLISH, "a'b\"c");
        Assert.assertEquals("id('a&apos;b&quot;c')", idPtr.asPath());

        // Namespaced child path
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element nsElem = new Element("item", ns);
        root.addContent(nsElem);
        rootPtr.getNamespaceResolver().registerNamespace("pfx", "http://example.com");
        JDOMNodePointer nsElemPtr = new JDOMNodePointer(rootPtr, nsElem);
        Assert.assertEquals("/pfx:item[1]", nsElemPtr.asPath());

        // Namespaced element without registered prefix in resolver -> fallback to node()[index]
        Namespace ns2 = Namespace.getNamespace("unknown", "http://unregistered.com");
        Element ns2Elem = new Element("item2", ns2);
        root.addContent(ns2Elem);
        JDOMNodePointer ns2ElemPtr = new JDOMNodePointer(rootPtr, ns2Elem);
        Assert.assertTrue(ns2ElemPtr.asPath().startsWith("/node()["));
    }

    @Test
    public void testEqualsAndHashCode() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer p1 = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer p2 = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer p3 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("non-pointer"));

        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }
}
