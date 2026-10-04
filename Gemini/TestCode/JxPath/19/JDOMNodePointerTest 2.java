package org.apache.commons.jxpath.ri.model.jdom;

import java.util.ArrayList;
import java.util.List;
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
    public void testConstructorsAndBasicGetters() {
        Element root = new Element("root");
        JDOMNodePointer ptr1 = new JDOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, ptr1.getBaseValue());
        Assert.assertEquals(root, ptr1.getImmediateNode());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertNull(ptr1.getParent());

        JDOMNodePointer ptr2 = new JDOMNodePointer(root, Locale.CANADA, "myId");
        Assert.assertEquals("id('myId')", ptr2.asPath());

        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer childPtr = new JDOMNodePointer(ptr1, child);
        Assert.assertEquals(ptr1, childPtr.getParent());
        Assert.assertEquals(child, childPtr.getImmediateNode());
    }

    @Test
    public void testIteratorsAndNamespacePointers() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.US);

        NodeIterator childIt = ptr.childIterator(null, false, null);
        Assert.assertNotNull(childIt);
        Assert.assertTrue(childIt instanceof JDOMNodeIterator);

        NodeIterator attrIt = ptr.attributeIterator(new QName("test"));
        Assert.assertNotNull(attrIt);
        Assert.assertTrue(attrIt instanceof JDOMAttributeIterator);

        NodeIterator nsIt = ptr.namespaceIterator();
        Assert.assertNotNull(nsIt);
        Assert.assertTrue(nsIt instanceof JDOMNamespaceIterator);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
        Assert.assertTrue(nsPtr instanceof JDOMNamespacePointer);
    }

    @Test
    public void testGetNamespaceURI() {
        Namespace ns = Namespace.getNamespace("p", "http://test.org");
        Element elem = new Element("elem", ns);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        Assert.assertEquals("http://test.org", ptr.getNamespaceURI());

        Element noNs = new Element("noNs");
        JDOMNodePointer ptrNoNs = new JDOMNodePointer(noNs, Locale.US);
        Assert.assertNull(ptrNoNs.getNamespaceURI());

        Text text = new Text("text");
        JDOMNodePointer ptrText = new JDOMNodePointer(text, Locale.US);
        Assert.assertNull(ptrText.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceResolver() {
        Element elem = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NamespaceResolver resolver = ptr.getNamespaceResolver();
        Assert.assertNotNull(resolver);
        // Repeated call returns same cached instance
        Assert.assertSame(resolver, ptr.getNamespaceResolver());
        Assert.assertSame(ptr, resolver.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceURIByPrefix() {
        Namespace nsP = Namespace.getNamespace("p", "http://p.com");
        Namespace nsQ = Namespace.getNamespace("q", "http://q.com");
        Element root = new Element("root", nsP);
        root.addNamespaceDeclaration(nsQ);
        Document doc = new Document(root);

        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.US);
        Assert.assertEquals(Namespace.XML_NAMESPACE.getURI(), docPtr.getNamespaceURI("xml"));
        Assert.assertEquals("http://p.com", docPtr.getNamespaceURI("p"));
        Assert.assertEquals("http://q.com", docPtr.getNamespaceURI("q"));
        Assert.assertNull(docPtr.getNamespaceURI("unknown"));

        JDOMNodePointer elemPtr = new JDOMNodePointer(root, Locale.US);
        Assert.assertEquals("http://p.com", elemPtr.getNamespaceURI("p"));
        Assert.assertNull(elemPtr.getNamespaceURI("unknown"));

        Text text = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        Assert.assertEquals(Namespace.XML_NAMESPACE.getURI(), textPtr.getNamespaceURI("xml"));
        Assert.assertNull(textPtr.getNamespaceURI("p"));
    }

    @Test
    public void testIsLeaf() {
        Element emptyElem = new Element("empty");
        Assert.assertTrue(new JDOMNodePointer(emptyElem, Locale.US).isLeaf());

        Element notEmptyElem = new Element("parent");
        notEmptyElem.addContent(new Element("child"));
        Assert.assertFalse(new JDOMNodePointer(notEmptyElem, Locale.US).isLeaf());

        Document emptyDoc = new Document();
        Assert.assertTrue(new JDOMNodePointer(emptyDoc, Locale.US).isLeaf());

        Document notEmptyDoc = new Document(new Element("root"));
        Assert.assertFalse(new JDOMNodePointer(notEmptyDoc, Locale.US).isLeaf());

        Text text = new Text("leaf");
        Assert.assertTrue(new JDOMNodePointer(text, Locale.US).isLeaf());
    }

    @Test
    public void testGetName() {
        Element elemNoNs = new Element("elem");
        Assert.assertEquals(new QName(null, "elem"), new JDOMNodePointer(elemNoNs, Locale.US).getName());

        Element elemNs = new Element("elem", Namespace.getNamespace("ns", "http://test"));
        Assert.assertEquals(new QName("ns", "elem"), new JDOMNodePointer(elemNs, Locale.US).getName());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        Assert.assertEquals(new QName(null, "target"), new JDOMNodePointer(pi, Locale.US).getName());

        Text text = new Text("txt");
        Assert.assertEquals(new QName(null, null), new JDOMNodePointer(text, Locale.US).getName());
    }

    @Test
    public void testGetValueAndSetValueOnText() {
        Element root = new Element("root");
        Text text = new Text("  Hello World  ");
        root.addContent(text);

        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        Assert.assertEquals("Hello World", textPtr.getValue());

        // Preserve space via enclosing attribute
        root.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        Assert.assertEquals("  Hello World  ", textPtr.getValue());

        // Set value on text node
        textPtr.setValue("New Text");
        Assert.assertEquals("New Text", text.getText());

        // Empty value on text node removes it from parent
        textPtr.setValue("");
        Assert.assertEquals(0, root.getContent().size());
    }

    @Test
    public void testGetValueOnCommentAndPI() {
        Comment comment = new Comment("  A comment  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.US);
        Assert.assertEquals("A comment", commentPtr.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.US);
        Assert.assertEquals("pi data", piPtr.getValue());
    }

    @Test
    public void testGetValueOnElement() {
        Element root = new Element("root");
        Element child1 = new Element("child1");
        child1.addContent(new Text("Hello "));
        Element child2 = new Element("child2");
        child2.addContent(new Text("World"));
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        Assert.assertEquals("Hello World", rootPtr.getValue());
    }

    @Test
    public void testSetValueOnElement() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.US);

        // String value
        ptr.setValue("test text");
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("test text", ((Text) root.getContent().get(0)).getText());

        // Element value
        Element sourceElem = new Element("source");
        sourceElem.addContent(new Element("sub1"));
        sourceElem.addContent(new Text("subText"));
        ptr.setValue(sourceElem);
        Assert.assertEquals(2, root.getContent().size());

        // Document value
        Element docRoot = new Element("docRoot");
        docRoot.addContent(new Element("subDoc"));
        Document doc = new Document(docRoot);
        ptr.setValue(doc);
        Assert.assertEquals(1, root.getContent().size());

        // Text value
        ptr.setValue(new Text("rawText"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("rawText", ((Text) root.getContent().get(0)).getText());

        // CDATA value
        ptr.setValue(new CDATA("cdataText"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertEquals("cdataText", ((Text) root.getContent().get(0)).getText());

        // ProcessingInstruction value
        ptr.setValue(new ProcessingInstruction("target", "data"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof ProcessingInstruction);

        // Comment value
        ptr.setValue(new Comment("myComment"));
        Assert.assertEquals(1, root.getContent().size());
        Assert.assertTrue(root.getContent().get(0) instanceof Comment);

        // Empty string value
        ptr.setValue("");
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

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        NodePointer pAttr1 = new JDOMNodePointer(rootPtr, a1);
        NodePointer pAttr2 = new JDOMNodePointer(rootPtr, a2);
        NodePointer pChild1 = new JDOMNodePointer(rootPtr, c1);
        NodePointer pChild2 = new JDOMNodePointer(rootPtr, c2);

        // Same node
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pAttr1, pAttr1));
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pChild1, pChild1));

        // Attribute vs Element
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pChild1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild1, pAttr1));

        // Attribute vs Attribute
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pAttr2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pAttr2, pAttr1));

        // Element vs Element
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pChild1, pChild2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild2, pChild1));

        // Unrelated pointers
        Element unrelated = new Element("unrelated");
        NodePointer pUnrelated = new JDOMNodePointer(rootPtr, unrelated);
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pUnrelated, pUnrelated));

        // compareChildNodePointers on non-element throws RuntimeException
        Text text = new Text("txt");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        try {
            textPtr.compareChildNodePointers(pChild1, pChild2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException expected) {
            // expected
        }
    }

    @Test
    public void testTestNode() {
        Element elem = new Element("testElem", Namespace.getNamespace("p", "http://test.org"));
        Text text = new Text("hello");
        CDATA cdata = new CDATA("cdata");
        Comment comment = new Comment("comm");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        // null test
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, null));

        // NodeNameTest
        Assert.assertFalse(JDOMNodePointer.testNode(null, text, new NodeNameTest(new QName("testElem"))));
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, new NodeNameTest(new QName("p", "testElem"), "http://test.org")));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeNameTest(new QName("p", "testElem"), "http://wrong.org")));
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, new NodeNameTest(new QName(null, "*"))));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeNameTest(new QName("otherElem"))));

        Element noNsElem = new Element("myElem");
        Assert.assertTrue(JDOMNodePointer.testNode(null, noNsElem, new NodeNameTest(new QName("myElem"))));

        // NodeTypeTest
        Assert.assertTrue(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertTrue(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertTrue(JDOMNodePointer.testNode(null, cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertTrue(JDOMNodePointer.testNode(null, comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        Assert.assertTrue(JDOMNodePointer.testNode(null, pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(999)));

        // ProcessingInstructionTest
        Assert.assertTrue(JDOMNodePointer.testNode(null, pi, new ProcessingInstructionTest("target")));
        Assert.assertFalse(JDOMNodePointer.testNode(null, pi, new ProcessingInstructionTest("other")));
        Assert.assertFalse(JDOMNodePointer.testNode(null, elem, new ProcessingInstructionTest("target")));

        // Instance method testNode
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        Assert.assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
    }

    @Test
    public void testGetPrefixAndLocalName() {
        Element elem = new Element("elem", Namespace.getNamespace("p", "http://test"));
        Attribute attr = new Attribute("attr", "val", Namespace.getNamespace("a", "http://attr"));
        Element noNsElem = new Element("plain");
        Attribute noNsAttr = new Attribute("plainAttr", "val");

        Assert.assertEquals("p", JDOMNodePointer.getPrefix(elem));
        Assert.assertEquals("elem", JDOMNodePointer.getLocalName(elem));
        Assert.assertEquals("a", JDOMNodePointer.getPrefix(attr));
        Assert.assertEquals("attr", JDOMNodePointer.getLocalName(attr));

        Assert.assertNull(JDOMNodePointer.getPrefix(noNsElem));
        Assert.assertEquals("plain", JDOMNodePointer.getLocalName(noNsElem));
        Assert.assertNull(JDOMNodePointer.getPrefix(noNsAttr));
        Assert.assertEquals("plainAttr", JDOMNodePointer.getLocalName(noNsAttr));

        Assert.assertNull(JDOMNodePointer.getPrefix(new Text("txt")));
        Assert.assertNull(JDOMNodePointer.getLocalName(new Text("txt")));
    }

    @Test
    public void testIsLanguage() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPtr = new JDOMNodePointer(new JDOMNodePointer(root, Locale.US), child);
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("en-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLangElem = new Element("noLang");
        JDOMNodePointer noLangPtr = new JDOMNodePointer(noLangElem, Locale.US);
        Assert.assertTrue(noLangPtr.isLanguage("en"));
    }

    @Test
    public void testAsPath() {
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Namespace ns = Namespace.getNamespace("ns", "http://test.org");
        Element childNs = new Element("elem", ns);
        Text text1 = new Text("t1");
        CDATA cdata1 = new CDATA("c1");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        root.addContent(child1);
        root.addContent(child2);
        root.addContent(childNs);
        root.addContent(text1);
        root.addContent(cdata1);
        root.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        Assert.assertEquals("", rootPtr.asPath());

        JDOMNodePointer child1Ptr = new JDOMNodePointer(rootPtr, child1);
        Assert.assertEquals("/child[1]", child1Ptr.asPath());

        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        Assert.assertEquals("/child[2]", child2Ptr.asPath());

        JDOMNodePointer text1Ptr = new JDOMNodePointer(rootPtr, text1);
        Assert.assertEquals("/text()[1]", text1Ptr.asPath());

        JDOMNodePointer cdata1Ptr = new JDOMNodePointer(rootPtr, cdata1);
        Assert.assertEquals("/text()[2]", cdata1Ptr.asPath());

        JDOMNodePointer piPtr = new JDOMNodePointer(rootPtr, pi);
        Assert.assertEquals("/processing-instruction('target')[1]", piPtr.asPath());

        // Namespaced child path where prefix is registered
        rootPtr.getNamespaceResolver().registerNamespace("ns", "http://test.org");
        JDOMNodePointer childNsPtr = new JDOMNodePointer(rootPtr, childNs);
        Assert.assertEquals("/ns:elem[1]", childNsPtr.asPath());

        // Namespaced child path where prefix is not registered in resolver
        NamespaceResolver emptyResolver = new NamespaceResolver(null);
        JDOMNodePointer rootPtrNoResolverNs = new JDOMNodePointer(root, Locale.US) {
            private static final long serialVersionUID = 1L;
            public NamespaceResolver getNamespaceResolver() {
                return emptyResolver;
            }
        };
        JDOMNodePointer childNsUnresolvedPtr = new JDOMNodePointer(rootPtrNoResolverNs, childNs);
        Assert.assertEquals("/node()[3]", childNsUnresolvedPtr.asPath());
    }

    @Test
    public void testCreateAttribute() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        // Attribute without prefix
        NodePointer attrPtr = rootPtr.createAttribute(context, new QName("name"));
        Assert.assertNotNull(attrPtr);
        Assert.assertEquals("name", ((Attribute) attrPtr.getImmediateNode()).getName());
        Assert.assertEquals("", ((Attribute) attrPtr.getImmediateNode()).getValue());

        // Existing attribute returns iterator pointer
        NodePointer attrPtr2 = rootPtr.createAttribute(context, new QName("name"));
        Assert.assertNotNull(attrPtr2);

        // Attribute with prefix (registered)
        rootPtr.getNamespaceResolver().registerNamespace("myNs", "http://my.org");
        NodePointer nsAttrPtr = rootPtr.createAttribute(context, new QName("myNs", "attr2"));
        Assert.assertNotNull(nsAttrPtr);
        Assert.assertEquals("attr2", ((Attribute) nsAttrPtr.getImmediateNode()).getName());

        // Attribute with unknown prefix throws exception
        try {
            rootPtr.createAttribute(context, new QName("unknown", "attr3"));
            Assert.fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            // expected
        }

        // createAttribute on non-Element calls super
        Text text = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("name"));
            Assert.fail("Expected exception for creating attribute on non-Element");
        } catch (Exception expected) {
            // expected
        }
    }

    @Test
    public void testCreateChild() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        // Setting a custom factory that adds an Element child
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = rootPtr.createChild(context, new QName("child"), 0);
        Assert.assertNotNull(childPtr);
        Assert.assertEquals("child", ((Element) childPtr.getImmediateNode()).getName());

        // createChild with value
        NodePointer childWithValuePtr = rootPtr.createChild(context, new QName("childVal"), 1, "testValue");
        Assert.assertNotNull(childWithValuePtr);
        Assert.assertEquals("testValue", childWithValuePtr.getValue());

        // Failure when factory fails
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return false;
            }
        });
        try {
            rootPtr.createChild(context, new QName("failedChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException expected) {
            // expected
        }
    }

    @Test
    public void testRemove() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        // Remove child
        childPtr.remove();
        Assert.assertEquals(0, root.getContent().size());

        // Removing root throws JXPathException
        try {
            rootPtr.remove();
            Assert.fail("Expected JXPathException when removing root node");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Element e1 = new Element("test");
        Element e2 = new Element("test");

        JDOMNodePointer p1 = new JDOMNodePointer(e1, Locale.US);
        JDOMNodePointer p1_same = new JDOMNodePointer(e1, Locale.US);
        JDOMNodePointer p2 = new JDOMNodePointer(e2, Locale.US);

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p1_same));
        Assert.assertFalse(p1.equals(p2));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("Not a JDOMNodePointer"));

        Assert.assertEquals(e1.hashCode(), p1.hashCode());
    }
}
