package org.apache.commons.jxpath.ri.model.dom;

import java.io.ByteArrayInputStream;
import java.util.Locale;
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
import org.apache.commons.jxpath.ri.model.VariablePointer;
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
    private DocumentBuilderFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        String xml = "<root xmlns:foo=\"http://foo\" xmlns=\"http://default\" xml:lang=\"en\" id=\"rootId\">"
                + "<foo:child id=\"c1\" xml:space=\"preserve\">  Hello  </foo:child>"
                + "<child id=\"c2\">World</child>"
                + "<child id=\"c3\"><![CDATA[cdata content]]></child>"
                + "<!-- comment node -->"
                + "<?piTarget piData?>"
                + "<?piTarget piData2?>"
                + "</root>";
        document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    @Test
    public void testConstructorsAndBasicProperties() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US, "myId");
        Assert.assertEquals("myId", rootPtr.asPath());
        Assert.assertEquals(root, rootPtr.getBaseValue());
        Assert.assertEquals(root, rootPtr.getImmediateNode());
        Assert.assertEquals(1, rootPtr.getLength());
        Assert.assertTrue(rootPtr.isActual());
        Assert.assertFalse(rootPtr.isCollection());
        Assert.assertFalse(rootPtr.isLeaf());

        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, root.getFirstChild());
        Assert.assertEquals(rootPtr, childPtr.getParent());

        DOMNodePointer ptrNoId = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root.hashCode(), ptrNoId.hashCode());
        Assert.assertTrue(ptrNoId.equals(ptrNoId));
        Assert.assertTrue(ptrNoId.equals(new DOMNodePointer(root, Locale.US)));
        Assert.assertFalse(ptrNoId.equals(null));
        Assert.assertFalse(ptrNoId.equals("string"));
    }

    @Test
    public void testIsLeaf() {
        Element empty = document.createElement("empty");
        DOMNodePointer emptyPtr = new DOMNodePointer(empty, Locale.US);
        Assert.assertTrue(emptyPtr.isLeaf());
    }

    @Test
    public void testGetName() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        QName name = rootPtr.getName();
        Assert.assertEquals("root", name.getName());

        Node pi = root.getElementsByTagName("child").item(1).getNextSibling().getNextSibling();
        while (pi != null && pi.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
            pi = pi.getNextSibling();
        }
        Assert.assertNotNull(pi);
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        Assert.assertEquals("piTarget", piPtr.getName().getName());
    }

    @Test
    public void testGetPrefixAndLocalName() {
        Element root = document.getDocumentElement();
        Node fooChild = root.getElementsByTagNameNS("http://foo", "child").item(0);
        Assert.assertEquals("foo", DOMNodePointer.getPrefix(fooChild));
        Assert.assertEquals("child", DOMNodePointer.getLocalName(fooChild));

        Element simple = document.createElement("simpleTag");
        Assert.assertNull(DOMNodePointer.getPrefix(simple));
        Assert.assertEquals("simpleTag", DOMNodePointer.getLocalName(simple));
    }

    @Test
    public void testGetNamespaceURI() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Assert.assertEquals("http://default", rootPtr.getNamespaceURI());
        Assert.assertEquals("http://default", rootPtr.getNamespaceURI(""));
        Assert.assertEquals("http://default", rootPtr.getNamespaceURI((String) null));
        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPtr.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPtr.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://foo", rootPtr.getNamespaceURI("foo"));
        Assert.assertNull(rootPtr.getNamespaceURI("nonexistent"));
        // Test caching of namespace
        Assert.assertNull(rootPtr.getNamespaceURI("nonexistent"));

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("http://foo", docPtr.getNamespaceURI("foo"));
        Assert.assertEquals("http://default", docPtr.getDefaultNamespaceURI());

        Element unattached = document.createElement("unattached");
        DOMNodePointer unattachedPtr = new DOMNodePointer(unattached, Locale.US);
        Assert.assertNull(unattachedPtr.getDefaultNamespaceURI());
    }

    @Test
    public void testIsLanguage() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertTrue(rootPtr.isLanguage("en"));
        Assert.assertTrue(rootPtr.isLanguage("EN"));
        Assert.assertFalse(rootPtr.isLanguage("fr"));

        Element noLang = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.GERMAN);
        Assert.assertTrue(noLangPtr.isLanguage("de"));
    }

    @Test
    public void testGetValue() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertNotNull(rootPtr.getValue());

        Comment comment = document.createComment(" a comment ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("a comment", commentPtr.getValue());

        Comment emptyComment = document.createComment(null);
        DOMNodePointer emptyCommentPtr = new DOMNodePointer(emptyComment, Locale.US);
        Assert.assertEquals("", emptyCommentPtr.getValue());

        Text text = document.createTextNode("  some text  ");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        Assert.assertEquals("some text", textPtr.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "  some pi  ");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        Assert.assertEquals("some pi", piPtr.getValue());
    }

    @Test
    public void testSetValue() {
        Element elem = document.createElement("testSet");
        document.getDocumentElement().appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);

        ptr.setValue("New Text");
        Assert.assertEquals("New Text", ptr.getValue());

        Element replacement = document.createElement("rep");
        replacement.appendChild(document.createTextNode("childContent"));
        ptr.setValue(replacement);
        Assert.assertEquals("childContent", ptr.getValue());

        Document newDoc = factory.newDocumentBuilder().newDocument();
        Element docElem = newDoc.createElement("docElem");
        docElem.appendChild(newDoc.createTextNode("docContent"));
        newDoc.appendChild(docElem);
        ptr.setValue(newDoc);
        Assert.assertEquals("docContent", ptr.getValue());

        Text textChild = document.createTextNode("text");
        elem.appendChild(textChild);
        DOMNodePointer textPtr = new DOMNodePointer(textChild, Locale.US);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", textChild.getNodeValue());

        textPtr.setValue("");
        Assert.assertNull(textChild.getParentNode());

        Comment commentNode = document.createComment("comm");
        ptr.setValue(commentNode);
    }

    @Test
    public void testTestNode() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Assert.assertTrue(rootPtr.testNode(null));

        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName(null, "root"))));
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName(null, "*"))));
        Assert.assertTrue(rootPtr.testNode(new NodeNameTest(new QName("foo", "*"), "http://foo")));
        Assert.assertFalse(rootPtr.testNode(new NodeNameTest(new QName(null, "nonexistent"))));

        Node fooChild = root.getElementsByTagNameNS("http://foo", "child").item(0);
        DOMNodePointer fooPtr = new DOMNodePointer(fooChild, Locale.US);
        Assert.assertTrue(fooPtr.testNode(new NodeNameTest(new QName("foo", "child"), "http://foo")));

        Text textNode = document.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(textNode, Locale.US);
        Assert.assertFalse(textPtr.testNode(new NodeNameTest(new QName(null, "txt"))));

        Assert.assertTrue(rootPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        Assert.assertFalse(rootPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        Assert.assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        CDATASection cdata = document.createCDATASection("cd");
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);
        Assert.assertTrue(cdataPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = document.createComment("c");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "data");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        Assert.assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        Assert.assertFalse(piPtr.testNode(new NodeTypeTest(999)));

        Assert.assertTrue(piPtr.testNode(new ProcessingInstructionTest("piTarget")));
        Assert.assertFalse(piPtr.testNode(new ProcessingInstructionTest("otherTarget")));
        Assert.assertFalse(rootPtr.testNode(new ProcessingInstructionTest("piTarget")));
    }

    @Test
    public void testAsPath() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        rootPtr.getNamespaceResolver().registerNamespace("def", "http://default");

        Node child1 = root.getElementsByTagNameNS("http://foo", "child").item(0);
        DOMNodePointer c1Ptr = new DOMNodePointer(rootPtr, child1);
        Assert.assertTrue(c1Ptr.asPath().contains("child[1]"));

        Node text = child1.getFirstChild();
        DOMNodePointer textPtr = new DOMNodePointer(c1Ptr, text);
        Assert.assertTrue(textPtr.asPath().endsWith("/text()[1]"));

        Node pi = root.getElementsByTagName("child").item(1).getNextSibling();
        while (pi != null && pi.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
            pi = pi.getNextSibling();
        }
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, pi);
        Assert.assertTrue(piPtr.asPath().contains("/processing-instruction('piTarget')[1]"));

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("", docPtr.asPath());

        Element simpleElem = document.createElement("plain");
        DOMNodePointer plainRoot = new DOMNodePointer(simpleElem, Locale.US);
        Element subElem = document.createElement("sub");
        simpleElem.appendChild(subElem);
        DOMNodePointer subPtr = new DOMNodePointer(plainRoot, subElem);
        Assert.assertEquals("/plain[1]/sub[1]", subPtr.asPath());
    }

    @Test
    public void testIteratorsAndResolver() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        NodeIterator childIt = rootPtr.childIterator(new NodeTypeTest(Compiler.NODE_TYPE_NODE), false, null);
        Assert.assertNotNull(childIt);

        NodeIterator attrIt = rootPtr.attributeIterator(new QName(null, "id"));
        Assert.assertNotNull(attrIt);

        NodePointer nsPtr = rootPtr.namespacePointer("foo");
        Assert.assertNotNull(nsPtr);

        NodeIterator nsIt = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIt);

        NamespaceResolver nsr = rootPtr.getNamespaceResolver();
        Assert.assertNotNull(nsr);
        Assert.assertSame(nsr, rootPtr.getNamespaceResolver());
    }

    @Test
    public void testGetPointerByID() {
        JXPathContext context = JXPathContext.newContext(document);
        DOMNodePointer rootPtr = new DOMNodePointer(document.getDocumentElement(), Locale.US);

        Element root = document.getDocumentElement();
        root.setIdAttribute("id", true);
        Pointer p = rootPtr.getPointerByID(context, "rootId");
        Assert.assertTrue(p instanceof DOMNodePointer);

        Pointer pNull = rootPtr.getPointerByID(context, "nonexistentId");
        Assert.assertTrue(pNull instanceof NullPointer);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Pointer pFromDoc = docPtr.getPointerByID(context, "rootId");
        Assert.assertTrue(pFromDoc instanceof DOMNodePointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Attr attr1 = root.getAttributeNode("id");
        Attr attr2 = root.getAttributeNode("xml:lang");

        Node child1 = root.getFirstChild();
        Node child2 = root.getLastChild();

        DOMNodePointer pAttr1 = new DOMNodePointer(rootPtr, attr1);
        DOMNodePointer pAttr2 = new DOMNodePointer(rootPtr, attr2);
        DOMNodePointer pChild1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(rootPtr, child2);

        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pAttr1, pAttr1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pChild1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild1, pAttr1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pChild1, pChild2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild2, pChild1));

        int attrComp = rootPtr.compareChildNodePointers(pAttr1, pAttr2);
        Assert.assertTrue(attrComp == -1 || attrComp == 1);
    }

    @Test
    public void testRemove() {
        Element root = document.getDocumentElement();
        Element toRemove = document.createElement("toRemove");
        root.appendChild(toRemove);

        DOMNodePointer removePtr = new DOMNodePointer(toRemove, Locale.US);
        removePtr.remove();
        Assert.assertNull(toRemove.getParentNode());

        DOMNodePointer rootPtr = new DOMNodePointer(document, Locale.US);
        try {
            rootPtr.remove();
            Assert.fail("Expected JXPathException when removing root node");
        } catch (JXPathException e) {
            // Success
        }
    }

    @Test
    public void testCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(document);
        Element elem = document.createElement("elem");
        document.getDocumentElement().appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        ptr.getNamespaceResolver().registerNamespace("foo", "http://foo");

        NodePointer attrPtr = ptr.createAttribute(context, new QName("testAttr"));
        Assert.assertNotNull(attrPtr);
        Assert.assertTrue(elem.hasAttribute("testAttr"));

        NodePointer nsAttrPtr = ptr.createAttribute(context, new QName("foo", "nsAttr"));
        Assert.assertNotNull(nsAttrPtr);
        Assert.assertTrue(elem.hasAttributeNS("http://foo", "nsAttr"));

        try {
            ptr.createAttribute(context, new QName("unknownPrefix", "attr"));
            Assert.fail("Expected exception for unknown namespace prefix");
        } catch (JXPathException e) {
            // Success
        }

        Text text = document.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(new VariablePointer(new QName("var")), text);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Expected exception for creating attribute on non-element");
        } catch (Exception e) {
            // Success
        }
    }

    @Test
    public void testCreateChild() {
        JXPathContext context = JXPathContext.newContext(document);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer parent, Object parentNode, String name, int index) {
                if (parentNode instanceof Element) {
                    Element newChild = ((Element) parentNode).getOwnerDocument().createElement(name);
                    ((Element) parentNode).appendChild(newChild);
                    return true;
                }
                return false;
            }
        });

        Element root = document.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        NodePointer childPtr = rootPtr.createChild(context, new QName("newChild"), 0);
        Assert.assertNotNull(childPtr);
        Assert.assertEquals("newChild", childPtr.getName().getName());

        NodePointer childWithValue = rootPtr.createChild(context, new QName("valuedChild"), 0, "Val");
        Assert.assertNotNull(childWithValue);
        Assert.assertEquals("Val", childWithValue.getValue());

        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer parent, Object parentNode, String name, int index) {
                return false;
            }
        });

        try {
            rootPtr.createChild(context, new QName("failChild"), 0);
            Assert.fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // Success
        }
    }
}
