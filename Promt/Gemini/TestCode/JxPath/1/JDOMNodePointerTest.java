package org.apache.commons.jxpath.ri.model.jdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
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
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertTrue(ptr1.isLeaf());

        JDOMNodePointer ptr2 = new JDOMNodePointer(element, Locale.US, "id123");
        Assert.assertEquals("id('id123')", ptr2.asPath());

        JDOMNodePointer childPtr = new JDOMNodePointer(ptr1, element);
        Assert.assertEquals(ptr1, childPtr.getParent());
    }

    @Test
    public void testIsLeaf() {
        Element root = new Element("root");
        Document doc = new Document(root);

        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        Assert.assertFalse(docPtr.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer emptyDocPtr = new JDOMNodePointer(emptyDoc, Locale.ENGLISH);
        Assert.assertTrue(emptyDocPtr.isLeaf());

        JDOMNodePointer elementPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertTrue(elementPtr.isLeaf());

        root.addContent(new Element("child"));
        Assert.assertFalse(elementPtr.isLeaf());

        Text text = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertTrue(textPtr.isLeaf());
    }

    @Test
    public void testGetName() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elemWithNs = new Element("name", ns);
        JDOMNodePointer elemPtr = new JDOMNodePointer(elemWithNs, Locale.ENGLISH);
        QName qname = elemPtr.getName();
        Assert.assertEquals("pfx", qname.getPrefix());
        Assert.assertEquals("name", qname.getName());

        Element elemNoNs = new Element("simple");
        JDOMNodePointer simplePtr = new JDOMNodePointer(elemNoNs, Locale.ENGLISH);
        Assert.assertNull(simplePtr.getName().getPrefix());
        Assert.assertEquals("simple", simplePtr.getName().getName());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertNull(piPtr.getName().getPrefix());
        Assert.assertEquals("target", piPtr.getName().getName());

        Text text = new Text("text");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertNull(textPtr.getName().getName());
        Assert.assertNull(textPtr.getName().getPrefix());
    }

    @Test
    public void testGetNamespaceURI() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elem = new Element("test", ns);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        Assert.assertEquals("http://example.com", ptr.getNamespaceURI());
        Assert.assertEquals("http://example.com", ptr.getNamespaceURI("pfx"));
        Assert.assertNull(ptr.getNamespaceURI("unknown"));

        Element elemNoNs = new Element("test");
        JDOMNodePointer ptrNoNs = new JDOMNodePointer(elemNoNs, Locale.ENGLISH);
        Assert.assertNull(ptrNoNs.getNamespaceURI());

        Document doc = new Document(elem);
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        Assert.assertEquals("http://example.com", docPtr.getNamespaceURI("pfx"));
        Assert.assertNull(docPtr.getNamespaceURI("unknown"));

        Text text = new Text("abc");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertNull(textPtr.getNamespaceURI());
        Assert.assertNull(textPtr.getNamespaceURI("pfx"));
    }

    @Test
    public void testGetPrefixAndGetLocalName() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        Element elem = new Element("elemName", ns);
        Attribute attr = new Attribute("attrName", "attrVal", ns);

        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(elem));
        Assert.assertEquals("elemName", JDOMNodePointer.getLocalName(elem));

        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        Assert.assertEquals("attrName", JDOMNodePointer.getLocalName(attr));

        Element noNsElem = new Element("simple");
        Assert.assertNull(JDOMNodePointer.getPrefix(noNsElem));
        Assert.assertEquals("simple", JDOMNodePointer.getLocalName(noNsElem));

        Attribute noNsAttr = new Attribute("simpleAttr", "val");
        Assert.assertNull(JDOMNodePointer.getPrefix(noNsAttr));
        Assert.assertEquals("simpleAttr", JDOMNodePointer.getLocalName(noNsAttr));

        Object other = new Object();
        Assert.assertNull(JDOMNodePointer.getPrefix(other));
        Assert.assertNull(JDOMNodePointer.getLocalName(other));
    }

    @Test
    public void testGetValue() {
        Element elem = new Element("test");
        elem.setText("  elem text  ");
        Assert.assertEquals("elem text", new JDOMNodePointer(elem, Locale.ENGLISH).getValue());

        Comment comment = new Comment(" comment text ");
        Assert.assertEquals("comment text", new JDOMNodePointer(comment, Locale.ENGLISH).getValue());
        Comment emptyComment = new Comment("");
        Assert.assertEquals("", new JDOMNodePointer(emptyComment, Locale.ENGLISH).getValue());

        Text text = new Text("  text content  ");
        Assert.assertEquals("text content", new JDOMNodePointer(text, Locale.ENGLISH).getValue());

        CDATA cdata = new CDATA("  cdata content  ");
        Assert.assertEquals("cdata content", new JDOMNodePointer(cdata, Locale.ENGLISH).getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        Assert.assertEquals("pi data", new JDOMNodePointer(pi, Locale.ENGLISH).getValue());

        ProcessingInstruction piEmpty = new ProcessingInstruction("target", (String) null);
        Assert.assertNull(new JDOMNodePointer(piEmpty, Locale.ENGLISH).getValue());

        Assert.assertNull(new JDOMNodePointer(new Object(), Locale.ENGLISH).getValue());
    }

    @Test
    public void testSetValueOnText() {
        Element root = new Element("root");
        Text text = new Text("original");
        root.addContent(text);

        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        textPtr.setValue("modified");
        Assert.assertEquals("modified", text.getText());

        textPtr.setValue("");
        Assert.assertEquals(0, root.getContent().size());
    }

    @Test
    public void testSetValueOnElement() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        rootPtr.setValue("new text");
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof Text);
        Assert.assertEquals("new text", ((Text) root.getContent().get(0)).getText());

        Element sourceElem = new Element("src");
        sourceElem.addContent(new Element("c1"));
        sourceElem.addContent(new Text("c2"));
        sourceElem.addContent(new ProcessingInstruction("c3", "data"));
        sourceElem.addContent(new Comment("c4"));

        rootPtr.setValue(sourceElem);
        Assert.assertEquals(4, root.getContent().size());

        Document sourceDoc = new Document(new Element("docRoot"));
        rootPtr.setValue(sourceDoc);
        Assert.assertEquals(1, root.getContent().size());

        rootPtr.setValue(new Text("just text"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("just text", ((Text) root.getContent().get(0)).getText());

        rootPtr.setValue(new CDATA("just cdata"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("just cdata", ((Text) root.getContent().get(0)).getText());

        rootPtr.setValue(new ProcessingInstruction("target", "data"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof ProcessingInstruction);

        rootPtr.setValue(new Comment("a comment"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof Comment);

        rootPtr.setValue("");
        Assert.assertEquals(0, root.getContent().size());
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
        NodePointer pA1 = new JDOMNodePointer(rootPtr, a1);
        NodePointer pA2 = new JDOMNodePointer(rootPtr, a2);
        NodePointer pC1 = new JDOMNodePointer(rootPtr, c1);
        NodePointer pC2 = new JDOMNodePointer(rootPtr, c2);

        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pA1, pA1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pA1, pC1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pC1, pA1));

        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pA1, pA2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pA2, pA1));

        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pC1, pC2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pC2, pC1));

        Element outside = new Element("outside");
        NodePointer pOutside = new JDOMNodePointer(rootPtr, outside);
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pOutside, pOutside));
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pC1, pOutside));

        JDOMNodePointer nonElemPtr = new JDOMNodePointer(new Text("txt"), Locale.ENGLISH);
        try {
            nonElemPtr.compareChildNodePointers(pC1, pC2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("JXPath internal error"));
        }
    }

    @Test
    public void testTestNode() {
        Element elem = new Element("testElem", "pfx", "http://test.com");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);

        Assert.assertTrue(elemPtr.testNode(null));

        NodeNameTest wildTest = new NodeNameTest(new QName(null, "*"));
        Assert.assertTrue(elemPtr.testNode(wildTest));

        NodeNameTest matchTest = new NodeNameTest(new QName("pfx", "testElem"), "http://test.com");
        Assert.assertTrue(elemPtr.testNode(matchTest));

        NodeNameTest mismatchTest = new NodeNameTest(new QName("pfx", "otherElem"), "http://test.com");
        Assert.assertFalse(elemPtr.testNode(mismatchTest));

        NodeNameTest mismatchNSTest = new NodeNameTest(new QName("pfx", "testElem"), "http://other.com");
        Assert.assertFalse(elemPtr.testNode(mismatchNSTest));

        Text text = new Text("text");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertFalse(textPtr.testNode(matchTest));

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        NodeTypeTest commentTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        NodeTypeTest unknownTypeTest = new NodeTypeTest(999);

        Assert.assertTrue(elemPtr.testNode(nodeTest));
        Assert.assertFalse(elemPtr.testNode(textTypeTest));
        Assert.assertFalse(elemPtr.testNode(unknownTypeTest));

        Assert.assertTrue(textPtr.testNode(textTypeTest));
        Assert.assertFalse(textPtr.testNode(nodeTest));

        CDATA cdata = new CDATA("cdata");
        JDOMNodePointer cdataPtr = new JDOMNodePointer(cdata, Locale.ENGLISH);
        Assert.assertTrue(cdataPtr.testNode(textTypeTest));

        Comment comment = new Comment("comment");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertTrue(commentPtr.testNode(commentTypeTest));
        Assert.assertFalse(commentPtr.testNode(textTypeTest));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertTrue(piPtr.testNode(piTypeTest));
        Assert.assertFalse(piPtr.testNode(textTypeTest));

        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        Assert.assertTrue(piPtr.testNode(piTest));
        ProcessingInstructionTest piTestMismatch = new ProcessingInstructionTest("wrongTarget");
        Assert.assertFalse(piPtr.testNode(piTestMismatch));
        Assert.assertFalse(elemPtr.testNode(piTest));
    }

    @Test
    public void testIsLanguage() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        Assert.assertTrue(rootPtr.isLanguage("en"));
        Assert.assertTrue(rootPtr.isLanguage("EN-US"));
        Assert.assertFalse(rootPtr.isLanguage("fr"));

        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);
        Assert.assertTrue(childPtr.isLanguage("en"));

        Text childText = new Text("hello");
        child.addContent(childText);
        JDOMNodePointer textPtr = new JDOMNodePointer(childPtr, childText);
        Assert.assertTrue(textPtr.isLanguage("en"));

        CDATA cdata = new CDATA("hello");
        child.addContent(cdata);
        JDOMNodePointer cdataPtr = new JDOMNodePointer(childPtr, cdata);
        Assert.assertTrue(cdataPtr.isLanguage("en"));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        child.addContent(pi);
        JDOMNodePointer piPtr = new JDOMNodePointer(childPtr, pi);
        Assert.assertTrue(piPtr.isLanguage("en"));

        Comment comment = new Comment("comm");
        child.addContent(comment);
        JDOMNodePointer commentPtr = new JDOMNodePointer(childPtr, comment);
        Assert.assertTrue(commentPtr.isLanguage("en"));

        Element noLangRoot = new Element("noLang");
        JDOMNodePointer noLangPtr = new JDOMNodePointer(noLangRoot, Locale.GERMAN);
        Assert.assertTrue(noLangPtr.isLanguage("de"));
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
            Assert.fail("Expected JXPathException for removing root");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot remove root"));
        }
    }

    @Test
    public void testCreateAttribute() {
        Element root = new Element("root");
        JXPathContext context = JXPathContext.newContext(root);
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        NodePointer attrPtr1 = rootPtr.createAttribute(context, new QName("attr1"));
        Assert.assertNotNull(attrPtr1);
        Assert.assertEquals("", root.getAttributeValue("attr1"));

        Namespace ns = Namespace.getNamespace("pfx", "http://example.com");
        root.addNamespaceDeclaration(ns);
        NodePointer attrPtr2 = rootPtr.createAttribute(context, new QName("pfx", "attr2"));
        Assert.assertNotNull(attrPtr2);
        Assert.assertEquals("", root.getAttributeValue("attr2", ns));

        try {
            rootPtr.createAttribute(context, new QName("unknown", "attr3"));
            Assert.fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown namespace prefix"));
        }

        Text text = new Text("txt");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected JXPathException for creating attribute on non-element");
        } catch (JXPathException e) {
            // expected
        }
    }

    @Test
    public void testCreateChild() {
        Element root = new Element("root");
        JXPathContext context = JXPathContext.newContext(root);
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected JXPathException when no factory is configured");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Factory is not set"));
        }

        context.setFactory(new AbstractFactory() {
            public boolean createObject(
                JXPathContext ctx,
                NodePointer parent,
                Object contextNode,
                String name,
                int index)
            {
                if (contextNode instanceof Element) {
                    ((Element) contextNode).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = rootPtr.createChild(context, new QName("child"), 0);
        Assert.assertNotNull(childPtr);
        Assert.assertEquals("child", ((Element) childPtr.getBaseValue()).getName());

        NodePointer childWithVal = rootPtr.createChild(context, new QName("childWithVal"), 1, "testVal");
        Assert.assertNotNull(childWithVal);
        Assert.assertEquals("testVal", childWithVal.getValue());

        context.setFactory(new AbstractFactory() {
            public boolean createObject(
                JXPathContext ctx,
                NodePointer parent,
                Object contextNode,
                String name,
                int index)
            {
                return false;
            }
        });

        try {
            rootPtr.createChild(context, new QName("failChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            Assert.assertTrue(e.getMessage().contains("Factory could not create"));
        }
    }

    @Test
    public void testAsPath() {
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        root.addContent(child1);
        root.addContent(child2);

        Text text = new Text("textVal");
        child1.addContent(text);

        CDATA cdata = new CDATA("cdataVal");
        child1.addContent(cdata);

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        child1.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals("", rootPtr.asPath());

        JDOMNodePointer child1Ptr = new JDOMNodePointer(rootPtr, child1);
        Assert.assertEquals("/child[1]", child1Ptr.asPath());

        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        Assert.assertEquals("/child[2]", child2Ptr.asPath());

        JDOMNodePointer textPtr = new JDOMNodePointer(child1Ptr, text);
        Assert.assertEquals("/child[1]/text()[1]", textPtr.asPath());

        JDOMNodePointer cdataPtr = new JDOMNodePointer(child1Ptr, cdata);
        Assert.assertEquals("/child[1]/text()[2]", cdataPtr.asPath());

        JDOMNodePointer piPtr = new JDOMNodePointer(child1Ptr, pi);
        Assert.assertEquals("/child[1]/processing-instruction('target')[1]", piPtr.asPath());

        Namespace ns = Namespace.getNamespace("test", "http://test.com");
        Element nsElem = new Element("item", ns);
        root.addContent(nsElem);

        JDOMNodePointer nsPtr = new JDOMNodePointer(rootPtr, nsElem);
        Assert.assertTrue(nsPtr.asPath().contains("item[1]"));

        JDOMNodePointer idPtr = new JDOMNodePointer(root, Locale.ENGLISH, "id'with\"quotes");
        Assert.assertEquals("id('id&apos;with&quot;quotes')", idPtr.asPath());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element e1 = new Element("elem");
        Element e2 = new Element("elem");

        JDOMNodePointer ptr1a = new JDOMNodePointer(e1, Locale.ENGLISH);
        JDOMNodePointer ptr1b = new JDOMNodePointer(e1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(e2, Locale.ENGLISH);

        Assert.assertTrue(ptr1a.equals(ptr1a));
        Assert.assertTrue(ptr1a.equals(ptr1b));
        Assert.assertFalse(ptr1a.equals(ptr2));
        Assert.assertFalse(ptr1a.equals(null));
        Assert.assertFalse(ptr1a.equals("not a pointer"));

        Assert.assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    @Test
    public void testIterators() {
        Element root = new Element("root");
        root.setAttribute("attr1", "val1");
        root.addContent(new Element("child"));

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = rootPtr.childIterator(null, false, null);
        Assert.assertNotNull(childIt);

        NodeIterator attrIt = rootPtr.attributeIterator(new QName("attr1"));
        Assert.assertNotNull(attrIt);

        NodeIterator nsIt = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIt);

        NodePointer nsPtr = rootPtr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
    }
}
