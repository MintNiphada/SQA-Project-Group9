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
    public void testConstructorsAndBasicProperties() {
        Element root = new Element("root");
        JDOMNodePointer ptr1 = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals(root, ptr1.getBaseValue());
        Assert.assertEquals(root, ptr1.getImmediateNode());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertEquals(System.identityHashCode(root), ptr1.hashCode());
        Assert.assertEquals(ptr1, ptr1);
        Assert.assertNotEquals(ptr1, null);
        Assert.assertNotEquals(ptr1, "string");

        JDOMNodePointer ptr2 = new JDOMNodePointer(root, Locale.ENGLISH, "root-id");
        Assert.assertEquals("id('root-id')", ptr2.asPath());
        Assert.assertEquals(ptr1, ptr2);

        JDOMNodePointer ptrChild = new JDOMNodePointer(ptr1, new Element("child"));
        Assert.assertNotNull(ptrChild.getParent());
    }

    @Test
    public void testIsLeaf() {
        Element emptyElem = new Element("elem");
        JDOMNodePointer ptrEmpty = new JDOMNodePointer(emptyElem, Locale.ENGLISH);
        Assert.assertTrue(ptrEmpty.isLeaf());

        Element elemWithChild = new Element("elem");
        elemWithChild.addContent(new Text("content"));
        JDOMNodePointer ptrNonEmpty = new JDOMNodePointer(elemWithChild, Locale.ENGLISH);
        Assert.assertFalse(ptrNonEmpty.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer ptrEmptyDoc = new JDOMNodePointer(emptyDoc, Locale.ENGLISH);
        Assert.assertTrue(ptrEmptyDoc.isLeaf());

        Document docWithRoot = new Document(new Element("root"));
        JDOMNodePointer ptrDocWithRoot = new JDOMNodePointer(docWithRoot, Locale.ENGLISH);
        Assert.assertFalse(ptrDocWithRoot.isLeaf());

        Comment comment = new Comment("comm");
        JDOMNodePointer ptrComment = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertTrue(ptrComment.isLeaf());
    }

    @Test
    public void testGetName() {
        Element elemNoPrefix = new Element("tag");
        JDOMNodePointer ptr1 = new JDOMNodePointer(elemNoPrefix, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, "tag"), ptr1.getName());

        Namespace ns = Namespace.getNamespace("pfx", "http://test.org");
        Element elemWithPrefix = new Element("tag", ns);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elemWithPrefix, Locale.ENGLISH);
        Assert.assertEquals(new QName("pfx", "tag"), ptr2.getName());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr3 = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, "target"), ptr3.getName());

        Comment comment = new Comment("comm");
        JDOMNodePointer ptr4 = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertEquals(new QName(null, null), ptr4.getName());
    }

    @Test
    public void testGetNamespaceURI() {
        Namespace ns = Namespace.getNamespace("pfx", "http://test.org");
        Element elem = new Element("tag", ns);
        JDOMNodePointer ptrElem = new JDOMNodePointer(elem, Locale.ENGLISH);
        Assert.assertEquals("http://test.org", ptrElem.getNamespaceURI());
        Assert.assertEquals("http://test.org", ptrElem.getNamespaceURI("pfx"));
        Assert.assertNull(ptrElem.getNamespaceURI("unknown"));

        Element noNsElem = new Element("tag");
        JDOMNodePointer ptrNoNs = new JDOMNodePointer(noNsElem, Locale.ENGLISH);
        Assert.assertNull(ptrNoNs.getNamespaceURI());

        Document doc = new Document(elem);
        JDOMNodePointer ptrDoc = new JDOMNodePointer(doc, Locale.ENGLISH);
        Assert.assertNull(ptrDoc.getNamespaceURI());
        Assert.assertEquals("http://test.org", ptrDoc.getNamespaceURI("pfx"));
        Assert.assertNull(ptrDoc.getNamespaceURI("unknown"));

        Text text = new Text("text");
        JDOMNodePointer ptrText = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertNull(ptrText.getNamespaceURI());
        Assert.assertNull(ptrText.getNamespaceURI("pfx"));
    }

    @Test
    public void testGetPrefixAndLocalName() {
        Namespace ns = Namespace.getNamespace("pfx", "http://test.org");
        Element elem = new Element("tag", ns);
        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(elem));
        Assert.assertEquals("tag", JDOMNodePointer.getLocalName(elem));

        Element elemNoPrefix = new Element("tag");
        Assert.assertNull(JDOMNodePointer.getPrefix(elemNoPrefix));

        Attribute attr = new Attribute("attr", "val", ns);
        Assert.assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        Assert.assertEquals("attr", JDOMNodePointer.getLocalName(attr));

        Attribute attrNoPrefix = new Attribute("attr", "val");
        Assert.assertNull(JDOMNodePointer.getPrefix(attrNoPrefix));

        Text text = new Text("text");
        Assert.assertNull(JDOMNodePointer.getPrefix(text));
        Assert.assertNull(JDOMNodePointer.getLocalName(text));
    }

    @Test
    public void testGetValue() {
        Element elem = new Element("tag");
        elem.setText("  elem text  ");
        Assert.assertEquals("elem text", new JDOMNodePointer(elem, Locale.ENGLISH).getValue());

        Comment comment = new Comment("  comment text  ");
        Assert.assertEquals("comment text", new JDOMNodePointer(comment, Locale.ENGLISH).getValue());

        Text text = new Text("  text text  ");
        Assert.assertEquals("text text", new JDOMNodePointer(text, Locale.ENGLISH).getValue());

        CDATA cdata = new CDATA("  cdata text  ");
        Assert.assertEquals("cdata text", new JDOMNodePointer(cdata, Locale.ENGLISH).getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        Assert.assertEquals("pi data", new JDOMNodePointer(pi, Locale.ENGLISH).getValue());

        Document doc = new Document();
        Assert.assertNull(new JDOMNodePointer(doc, Locale.ENGLISH).getValue());
    }

    @Test
    public void testSetValueOnText() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.ENGLISH);

        ptr.setValue("new text");
        Assert.assertEquals("new text", text.getText());

        ptr.setValue("");
        Assert.assertEquals(0, parent.getContent().size());
    }

    @Test
    public void testSetValueOnElement() {
        Element elem = new Element("elem");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        ptr.setValue("plain text");
        Assert.assertEquals("plain text", elem.getTextTrim());

        Element valElem = new Element("valElem");
        valElem.addContent(new Element("sub1"));
        valElem.addContent(new Text("sub2"));
        valElem.addContent(new CDATA("sub3"));
        valElem.addContent(new ProcessingInstruction("pi", "data"));
        valElem.addContent(new Comment("comm"));
        ptr.setValue(valElem);
        Assert.assertEquals(5, elem.getContent().size());

        Document valDoc = new Document();
        Element docElem = new Element("docChild");
        valDoc.addContent(docElem);
        ptr.setValue(valDoc);
        Assert.assertEquals(1, elem.getContent().size());

        ptr.setValue(new Text("textVal"));
        Assert.assertEquals("textVal", elem.getTextTrim());

        ptr.setValue(new CDATA("cdataVal"));
        Assert.assertEquals("cdataVal", elem.getTextTrim());

        ptr.setValue(new ProcessingInstruction("piTarget", "piVal"));
        Assert.assertTrue(elem.getContent().get(0) instanceof ProcessingInstruction);

        ptr.setValue(new Comment("commentVal"));
        Assert.assertTrue(elem.getContent().get(0) instanceof Comment);

        ptr.setValue("");
        Assert.assertEquals(0, elem.getContent().size());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element parent = new Element("parent");
        Attribute a1 = new Attribute("a1", "v1");
        Attribute a2 = new Attribute("a2", "v2");
        parent.setAttribute(a1);
        parent.setAttribute(a2);

        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        parent.addContent(c1);
        parent.addContent(c2);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer pA1 = new JDOMNodePointer(parentPtr, a1);
        JDOMNodePointer pA2 = new JDOMNodePointer(parentPtr, a2);
        JDOMNodePointer pC1 = new JDOMNodePointer(parentPtr, c1);
        JDOMNodePointer pC2 = new JDOMNodePointer(parentPtr, c2);

        Assert.assertEquals(0, parentPtr.compareChildNodePointers(pA1, pA1));
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(pA1, pA2));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(pA2, pA1));
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(pA1, pC1));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(pC1, pA1));
        Assert.assertEquals(-1, parentPtr.compareChildNodePointers(pC1, pC2));
        Assert.assertEquals(1, parentPtr.compareChildNodePointers(pC2, pC1));

        Text notInParent = new Text("unknown");
        JDOMNodePointer pUnknown = new JDOMNodePointer(parentPtr, notInParent);
        Assert.assertEquals(0, parentPtr.compareChildNodePointers(pUnknown, pUnknown));

        JDOMNodePointer textParentPtr = new JDOMNodePointer(new Text("text"), Locale.ENGLISH);
        try {
            textParentPtr.compareChildNodePointers(pC1, pC2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("compareChildNodes called for"));
        }
    }

    @Test
    public void testTestNode() {
        Element elem = new Element("name");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        Assert.assertTrue(ptr.testNode(null));

        Assert.assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "name"))));
        Assert.assertFalse(ptr.testNode(new NodeNameTest(new QName(null, "other"))));
        Assert.assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "*"))));
        Assert.assertFalse(ptr.testNode(new NodeNameTest(new QName("pfx", "*"))));

        Namespace ns = Namespace.getNamespace("p", "http://test.org");
        Element nsElem = new Element("name", ns);
        JDOMNodePointer nsPtr = new JDOMNodePointer(nsElem, Locale.ENGLISH);
        Assert.assertTrue(nsPtr.testNode(new NodeNameTest(new QName("p", "name"), "http://test.org")));
        Assert.assertFalse(nsPtr.testNode(new NodeNameTest(new QName("p", "name"), "http://other.org")));

        Text text = new Text("txt");
        JDOMNodePointer ptrText = new JDOMNodePointer(text, Locale.ENGLISH);
        Assert.assertFalse(ptrText.testNode(new NodeNameTest(new QName(null, "name"))));

        Assert.assertTrue(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertTrue(new JDOMNodePointer(new Document(), Locale.ENGLISH).testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertFalse(ptrText.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        Assert.assertTrue(ptrText.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertTrue(new JDOMNodePointer(new CDATA("cd"), Locale.ENGLISH).testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertFalse(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = new Comment("comm");
        JDOMNodePointer ptrComm = new JDOMNodePointer(comment, Locale.ENGLISH);
        Assert.assertTrue(ptrComm.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertFalse(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptrPI = new JDOMNodePointer(pi, Locale.ENGLISH);
        Assert.assertTrue(ptrPI.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(ptr.testNode(new NodeTypeTest(999)));

        Assert.assertTrue(ptrPI.testNode(new ProcessingInstructionTest("target")));
        Assert.assertFalse(ptrPI.testNode(new ProcessingInstructionTest("other")));
        Assert.assertFalse(ptr.testNode(new ProcessingInstructionTest("target")));
    }

    @Test
    public void testLanguage() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        Assert.assertTrue(rootPtr.isLanguage("en"));
        Assert.assertTrue(rootPtr.isLanguage("EN-us"));
        Assert.assertFalse(rootPtr.isLanguage("fr"));

        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLangElem = new Element("noLang");
        JDOMNodePointer noLangPtr = new JDOMNodePointer(noLangElem, Locale.FRENCH);
        Assert.assertTrue(noLangPtr.isLanguage("fr"));
        Assert.assertFalse(noLangPtr.isLanguage("en"));

        Text text = new Text("sample");
        child.addContent(text);
        JDOMNodePointer textPtr = new JDOMNodePointer(childPtr, text);
        Assert.assertTrue(textPtr.isLanguage("en"));
    }

    @Test
    public void testIterators() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = ptr.childIterator(new NodeNameTest(new QName(null, "sub")), false, null);
        Assert.assertNotNull(childIt);

        NodeIterator attrIt = ptr.attributeIterator(new QName(null, "attr"));
        Assert.assertNotNull(attrIt);

        NodeIterator nsIt = ptr.namespaceIterator();
        Assert.assertNotNull(nsIt);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
    }

    @Test
    public void testCreateAttribute() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(root);

        NodePointer attrPtr1 = ptr.createAttribute(ctx, new QName("attr1"));
        Assert.assertNotNull(attrPtr1);
        Assert.assertEquals("attr1", root.getAttribute("attr1").getName());

        NodePointer attrPtr1Again = ptr.createAttribute(ctx, new QName("attr1"));
        Assert.assertNotNull(attrPtr1Again);

        Namespace ns = Namespace.getNamespace("pfx", "http://test.org");
        root.addNamespaceDeclaration(ns);
        NodePointer attrPtr2 = ptr.createAttribute(ctx, new QName("pfx", "attr2"));
        Assert.assertNotNull(attrPtr2);
        Assert.assertNotNull(root.getAttribute("attr2", ns));

        try {
            ptr.createAttribute(ctx, new QName("unknown", "attr3"));
            Assert.fail("Expected JXPathException for unknown prefix");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown namespace prefix: unknown"));
        }

        Text text = new Text("val");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        try {
            textPtr.createAttribute(ctx, new QName("attr"));
            Assert.fail("Expected JXPathException for non-element attribute creation");
        } catch (JXPathException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCreateChild() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(root);

        try {
            rootPtr.createChild(ctx, new QName("child"), 0);
            Assert.fail("Expected exception when factory is not set");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Factory is not set on the JXPathContext"));
        }

        ctx.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                if (parent instanceof Element && "child".equals(name)) {
                    ((Element) parent).addContent(new Element("child"));
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = rootPtr.createChild(ctx, new QName("child"), NodePointer.WHOLE_COLLECTION);
        Assert.assertNotNull(childPtr);

        NodePointer childWithVal = rootPtr.createChild(ctx, new QName("child"), 1, "customValue");
        Assert.assertNotNull(childWithVal);
        Assert.assertEquals("customValue", childWithVal.getValue());

        try {
            rootPtr.createChild(ctx, new QName("uncreatable"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            Assert.assertTrue(e.getMessage().contains("Factory could not create a child node"));
        }
    }

    @Test
    public void testRemove() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);

        Assert.assertEquals(1, parent.getContent().size());
        childPtr.remove();
        Assert.assertEquals(0, parent.getContent().size());

        try {
            parentPtr.remove();
            Assert.fail("Expected JXPathException when removing root node");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot remove root JDOM node"));
        }
    }

    @Test
    public void testAsPath() {
        JDOMNodePointer idPtr = new JDOMNodePointer(new Element("root"), Locale.ENGLISH, "item'1\"2");
        Assert.assertEquals("id('item&apos;1&quot;2')", idPtr.asPath());

        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Assert.assertEquals("", rootPtr.asPath());

        Element child1 = new Element("child");
        Element child2 = new Element("child");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer child1Ptr = new JDOMNodePointer(rootPtr, child1);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        Assert.assertEquals("/child[1]", child1Ptr.asPath());
        Assert.assertEquals("/child[2]", child2Ptr.asPath());

        Text text = new Text("hello");
        CDATA cdata = new CDATA("world");
        ProcessingInstruction pi = new ProcessingInstruction("test-pi", "data");
        root.addContent(text);
        root.addContent(cdata);
        root.addContent(pi);

        JDOMNodePointer textPtr = new JDOMNodePointer(rootPtr, text);
        JDOMNodePointer cdataPtr = new JDOMNodePointer(rootPtr, cdata);
        JDOMNodePointer piPtr = new JDOMNodePointer(rootPtr, pi);

        Assert.assertEquals("/text()[1]", textPtr.asPath());
        Assert.assertEquals("/text()[2]", cdataPtr.asPath());
        Assert.assertEquals("/processing-instruction('test-pi')[1]", piPtr.asPath());

        Namespace ns = Namespace.getNamespace("pfx", "http://test.org");
        Element nsChild = new Element("child", ns);
        root.addContent(nsChild);
        JDOMNodePointer nsChildPtr = new JDOMNodePointer(rootPtr, nsChild);

        NamespaceResolver nsr = new NamespaceResolver();
        nsr.registerNamespace("pfx", "http://test.org");
        nsChildPtr.setNamespaceResolver(nsr);
        Assert.assertEquals("/pfx:child[1]", nsChildPtr.asPath());

        NamespaceResolver emptyNsr = new NamespaceResolver();
        nsChildPtr.setNamespaceResolver(emptyNsr);
        Assert.assertEquals("/node()[3]", nsChildPtr.asPath());
    }

    @Test
    public void testConstants() {
        Assert.assertEquals("http://www.w3.org/XML/1998/namespace", JDOMNodePointer.XML_NAMESPACE_URI);
        Assert.assertEquals("http://www.w3.org/2000/xmlns/", JDOMNodePointer.XMLNS_NAMESPACE_URI);
    }
}
