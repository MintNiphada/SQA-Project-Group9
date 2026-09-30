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
import org.apache.commons.jxpath.ri.compiler.NodeTest;
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

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    @Test
    public void testConstructorsAndBasicProperties() {
        Element root = document.createElement("root");
        document.appendChild(root);

        DOMNodePointer ptr1 = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, ptr1.getBaseValue());
        Assert.assertEquals(root, ptr1.getImmediateNode());
        Assert.assertEquals(Locale.US, ptr1.getLocale());
        Assert.assertTrue(ptr1.isActual());
        Assert.assertFalse(ptr1.isCollection());
        Assert.assertEquals(1, ptr1.getLength());
        Assert.assertTrue(ptr1.isLeaf());

        DOMNodePointer ptr2 = new DOMNodePointer(root, Locale.GERMANY, "id123");
        Assert.assertEquals(root, ptr2.getNode());

        DOMNodePointer childPtr = new DOMNodePointer(ptr1, root);
        Assert.assertEquals(ptr1, childPtr.getParent());
        Assert.assertEquals(root, childPtr.getNode());

        root.appendChild(document.createElement("child"));
        Assert.assertFalse(ptr1.isLeaf());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element elem1 = document.createElement("elem1");
        Element elem2 = document.createElement("elem2");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr1Dup = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.US);

        Assert.assertTrue(ptr1.equals(ptr1));
        Assert.assertTrue(ptr1.equals(ptr1Dup));
        Assert.assertFalse(ptr1.equals(ptr2));
        Assert.assertFalse(ptr1.equals(null));
        Assert.assertFalse(ptr1.equals("Not a pointer"));

        Assert.assertEquals(System.identityHashCode(elem1), ptr1.hashCode());
    }

    @Test
    public void testGetName() {
        Element elemNS = document.createElementNS("http://example.com/ns", "ex:myElement");
        DOMNodePointer ptrElem = new DOMNodePointer(elemNS, Locale.US);
        QName nameElem = ptrElem.getName();
        Assert.assertEquals("ex", nameElem.getPrefix());
        Assert.assertEquals("myElement", nameElem.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer ptrPI = new DOMNodePointer(pi, Locale.US);
        QName namePI = ptrPI.getName();
        Assert.assertNull(namePI.getPrefix());
        Assert.assertEquals("targetPI", namePI.getName());

        Text text = document.createTextNode("sample");
        DOMNodePointer ptrText = new DOMNodePointer(text, Locale.US);
        QName nameText = ptrText.getName();
        Assert.assertNull(nameText.getPrefix());
        Assert.assertNull(nameText.getName());
    }

    @Test
    public void testGetPrefixAndLocalName() {
        Element elem = document.createElement("simple");
        Assert.assertNull(DOMNodePointer.getPrefix(elem));
        Assert.assertEquals("simple", DOMNodePointer.getLocalName(elem));

        Element elemPrefix = document.createElement("pfx:tag");
        Assert.assertEquals("pfx", DOMNodePointer.getPrefix(elemPrefix));
        Assert.assertEquals("tag", DOMNodePointer.getLocalName(elemPrefix));

        Element elemNS = document.createElementNS("http://ns", "pfx2:tag2");
        Assert.assertEquals("pfx2", DOMNodePointer.getPrefix(elemNS));
        Assert.assertEquals("tag2", DOMNodePointer.getLocalName(elemNS));
    }

    @Test
    public void testTestNodeNullAndUnknownTest() {
        Element elem = document.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        Assert.assertTrue(ptr.testNode(null));

        NodeTest unknownTest = new NodeTest() {};
        Assert.assertFalse(ptr.testNode(unknownTest));
    }

    @Test
    public void testTestNodeNameTest() {
        Element elemNS = document.createElementNS("http://example.com", "ns:item");
        Text text = document.createTextNode("text");

        Assert.assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("item"))));

        NodeNameTest wildcardNoPrefix = new NodeNameTest(new QName(null, "*"));
        Assert.assertTrue(DOMNodePointer.testNode(elemNS, wildcardNoPrefix));

        NodeNameTest matchNameAndNS = new NodeNameTest(new QName("ns", "item"), "http://example.com");
        Assert.assertTrue(DOMNodePointer.testNode(elemNS, matchNameAndNS));

        NodeNameTest matchNameWrongNS = new NodeNameTest(new QName("ns", "item"), "http://wrong.com");
        Assert.assertFalse(DOMNodePointer.testNode(elemNS, matchNameWrongNS));

        NodeNameTest mismatchName = new NodeNameTest(new QName("ns", "other"), "http://example.com");
        Assert.assertFalse(DOMNodePointer.testNode(elemNS, mismatchName));

        NodeNameTest wildcardWithNS = new NodeNameTest(new QName("ns", "*"), "http://example.com");
        Assert.assertTrue(DOMNodePointer.testNode(elemNS, wildcardWithNS));

        NodeNameTest wildcardWithWrongNS = new NodeNameTest(new QName("ns", "*"), "http://wrong.com");
        Assert.assertFalse(DOMNodePointer.testNode(elemNS, wildcardWithWrongNS));
    }

    @Test
    public void testTestNodeTypeTest() {
        Element elem = document.createElement("elem");
        Text text = document.createTextNode("txt");
        CDATASection cdata = document.createCDATASection("cdata");
        Comment comment = document.createComment("comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        Assert.assertTrue(DOMNodePointer.testNode(elem, nodeTest));
        Assert.assertTrue(DOMNodePointer.testNode(document, nodeTest));
        Assert.assertFalse(DOMNodePointer.testNode(text, nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        Assert.assertTrue(DOMNodePointer.testNode(text, textTest));
        Assert.assertTrue(DOMNodePointer.testNode(cdata, textTest));
        Assert.assertFalse(DOMNodePointer.testNode(elem, textTest));

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        Assert.assertTrue(DOMNodePointer.testNode(comment, commentTest));
        Assert.assertFalse(DOMNodePointer.testNode(elem, commentTest));

        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        Assert.assertTrue(DOMNodePointer.testNode(pi, piTest));
        Assert.assertFalse(DOMNodePointer.testNode(elem, piTest));

        NodeTypeTest invalidTypeTest = new NodeTypeTest(999);
        Assert.assertFalse(DOMNodePointer.testNode(elem, invalidTypeTest));
    }

    @Test
    public void testTestNodeProcessingInstructionTest() {
        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "data");
        Element elem = document.createElement("elem");

        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("myTarget");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("otherTarget");

        Assert.assertTrue(DOMNodePointer.testNode(pi, matchingTest));
        Assert.assertFalse(DOMNodePointer.testNode(pi, mismatchTest));
        Assert.assertFalse(DOMNodePointer.testNode(elem, matchingTest));
    }

    @Test
    public void testGetNamespaceURIAndPrefixResolution() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://default.com");
        root.setAttribute("xmlns:custom", "http://custom.com");
        root.setAttribute("xmlns:empty", "");
        document.appendChild(root);

        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);

        Assert.assertEquals("http://default.com", childPtr.getDefaultNamespaceURI());
        Assert.assertEquals("http://default.com", childPtr.getNamespaceURI(null));
        Assert.assertEquals("http://default.com", childPtr.getNamespaceURI(""));
        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPtr.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPtr.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://custom.com", childPtr.getNamespaceURI("custom"));
        Assert.assertEquals("http://custom.com", childPtr.getNamespaceURI("custom")); // from cache
        Assert.assertNull(childPtr.getNamespaceURI("nonExistent"));
        Assert.assertNull(childPtr.getNamespaceURI("empty"));

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("http://default.com", docPtr.getDefaultNamespaceURI());
        Assert.assertEquals("http://custom.com", docPtr.getNamespaceURI("custom"));

        Document emptyDoc = null;
        try {
            emptyDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception ignored) {}
        DOMNodePointer emptyDocPtr = new DOMNodePointer(emptyDoc, Locale.US);
        Assert.assertNull(emptyDocPtr.getDefaultNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIStatic() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:foo", "http://foo.com");
        document.appendChild(root);

        Element child = document.createElement("foo:child");
        root.appendChild(child);

        Assert.assertEquals("http://foo.com", DOMNodePointer.getNamespaceURI(child));
        Assert.assertNull(DOMNodePointer.getNamespaceURI(document));
    }

    @Test
    public void testGetLanguageAndIsLanguage() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        Assert.assertEquals("en-US", childPtr.getLanguage());
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("en-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLangElem = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLangElem, Locale.GERMAN);
        Assert.assertNull(noLangPtr.getLanguage());
        Assert.assertTrue(noLangPtr.isLanguage("de"));
    }

    @Test
    public void testGetValueAndStringValue() {
        Element root = document.createElement("root");
        Comment comment = document.createComment(" a comment ");
        Text text1 = document.createTextNode(" Hello ");
        Element subElem = document.createElement("sub");
        Text text2 = document.createTextNode("World ");
        CDATASection cdata = document.createCDATASection("!");
        ProcessingInstruction pi = document.createProcessingInstruction("pi", " info ");

        subElem.appendChild(text2);
        root.appendChild(text1);
        root.appendChild(subElem);
        root.appendChild(cdata);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals("Hello World !", rootPtr.getValue());

        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("a comment", commentPtr.getValue());

        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        Assert.assertEquals("info", piPtr.getValue());

        DOMNodePointer textPtr = new DOMNodePointer(text1, Locale.US);
        Assert.assertEquals("Hello", textPtr.getValue());

        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);
        Assert.assertEquals("!", cdataPtr.getValue());
    }

    @Test
    public void testSetValueTextAndCDATA() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);
        document.appendChild(root);

        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getNodeValue());

        textPtr.setValue("");
        Assert.assertNull(text.getParentNode());

        CDATASection cdata = document.createCDATASection("initialCD");
        root.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);
        cdataPtr.setValue("updatedCD");
        Assert.assertEquals("updatedCD", cdata.getNodeValue());
        cdataPtr.setValue(null);
        Assert.assertNull(cdata.getParentNode());
    }

    @Test
    public void testSetValueElement() {
        Element root = document.createElement("root");
        root.appendChild(document.createElement("oldChild1"));
        root.appendChild(document.createElement("oldChild2"));
        document.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        rootPtr.setValue("New Text Content");
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals("New Text Content", root.getFirstChild().getNodeValue());

        Element sourceElem = document.createElement("source");
        sourceElem.appendChild(document.createElement("sub1"));
        sourceElem.appendChild(document.createElement("sub2"));

        rootPtr.setValue(sourceElem);
        Assert.assertEquals(2, root.getChildNodes().getLength());
        Assert.assertEquals("sub1", root.getFirstChild().getNodeName());

        Comment comment = document.createComment("commentNode");
        rootPtr.setValue(comment);
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals(Node.COMMENT_NODE, root.getFirstChild().getNodeType());

        rootPtr.setValue("");
        Assert.assertEquals(0, root.getChildNodes().getLength());
    }

    @Test
    public void testCreateAttribute() {
        Element elem = document.createElement("root");
        elem.setAttribute("xmlns:custom", "http://custom.com");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attrPtr = ptr.createAttribute(context, new QName("attr"));
        Assert.assertNotNull(attrPtr);
        Assert.assertTrue(elem.hasAttribute("attr"));

        NodePointer attrNSPtr = ptr.createAttribute(context, new QName("custom", "nsAttr"));
        Assert.assertNotNull(attrNSPtr);
        Assert.assertTrue(elem.hasAttributeNS("http://custom.com", "nsAttr"));

        try {
            ptr.createAttribute(context, new QName("unknown", "attr"));
            Assert.fail("Should throw JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            // expected
        }

        Text text = document.createTextNode("txt");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("Creating attribute on Text node should fail");
        } catch (Exception expected) {
            // expected
        }
    }

    @Test
    public void testCreateChild() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        JXPathContext context = JXPathContext.newContext(root);

        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Expected exception when factory is not set");
        } catch (JXPathException expected) {
            // expected
        }

        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer parent, Object contextBean, String name, int index) {
                if ("child".equals(name) || "valChild".equals(name)) {
                    Element newChild = document.createElement(name);
                    ((Node) contextBean).appendChild(newChild);
                    return true;
                }
                return false;
            }
        });

        NodePointer createdChild = rootPtr.createChild(context, new QName("child"), NodePointer.WHOLE_COLLECTION);
        Assert.assertNotNull(createdChild);
        Assert.assertEquals("child", createdChild.getName().getName());

        NodePointer createdValChild = rootPtr.createChild(context, new QName("valChild"), 0, "childValue");
        Assert.assertNotNull(createdValChild);
        Assert.assertEquals("childValue", createdValChild.getValue());

        try {
            rootPtr.createChild(context, new QName("nonExistent"), 0);
            Assert.fail("Expected exception when factory returns false");
        } catch (JXPathAbstractFactoryException expected) {
            // expected
        }
    }

    @Test
    public void testRemove() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer childPtr = new DOMNodePointer(root, child);
        childPtr.remove();
        Assert.assertNull(child.getParentNode());

        DOMNodePointer rootPtr = new DOMNodePointer(document, root);
        rootPtr.remove();
        Assert.assertNull(root.getParentNode());

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        try {
            docPtr.remove();
            Assert.fail("Removing root DOM node should throw JXPathException");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test
    public void testAsPath() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "foo'bar\"baz");
        Assert.assertEquals("id('foo&apos;bar&quot;baz')", idPtr.asPath());

        Element child1 = document.createElement("child");
        Element child2 = document.createElement("child");
        Element other = document.createElement("other");
        Text text1 = document.createTextNode("text1");
        CDATASection cdata1 = document.createCDATASection("cdata1");
        ProcessingInstruction pi1 = document.createProcessingInstruction("target", "data1");
        ProcessingInstruction pi2 = document.createProcessingInstruction("target", "data2");

        root.appendChild(child1);
        root.appendChild(child2);
        root.appendChild(other);
        root.appendChild(text1);
        root.appendChild(cdata1);
        root.appendChild(pi1);
        root.appendChild(pi2);

        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);
        DOMNodePointer otherPtr = new DOMNodePointer(rootPtr, other);
        DOMNodePointer text1Ptr = new DOMNodePointer(rootPtr, text1);
        DOMNodePointer cdata1Ptr = new DOMNodePointer(rootPtr, cdata1);
        DOMNodePointer pi1Ptr = new DOMNodePointer(rootPtr, pi1);
        DOMNodePointer pi2Ptr = new DOMNodePointer(rootPtr, pi2);

        Assert.assertEquals("/child[1]", child1Ptr.asPath());
        Assert.assertEquals("/child[2]", child2Ptr.asPath());
        Assert.assertEquals("/other[1]", otherPtr.asPath());
        Assert.assertEquals("/text()[1]", text1Ptr.asPath());
        Assert.assertEquals("/text()[2]", cdata1Ptr.asPath());
        Assert.assertEquals("/processing-instruction('target')[1]", pi1Ptr.asPath());
        Assert.assertEquals("/processing-instruction('target')[2]", pi2Ptr.asPath());

        Element nsElem = document.createElementNS("http://ns.example.com", "my:item");
        root.appendChild(nsElem);
        DOMNodePointer nsPtr = new DOMNodePointer(rootPtr, nsElem);

        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("my", "http://ns.example.com");
        rootPtr.setNamespaceResolver(resolver);

        Assert.assertEquals("/my:item[1]", nsPtr.asPath());

        Element unknownNSElem = document.createElementNS("http://unknown.ns", "un:elem");
        root.appendChild(unknownNSElem);
        DOMNodePointer unknownNSPtr = new DOMNodePointer(rootPtr, unknownNSElem);
        Assert.assertEquals("/node()[5]", unknownNSPtr.asPath());

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("", docPtr.asPath());
    }

    @Test
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        root.setAttribute("id", "targetId");
        root.setIdAttribute("id", true);
        document.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = docPtr.getPointerByID(context, "targetId");
        Assert.assertTrue(found instanceof DOMNodePointer);
        Assert.assertEquals(root, found.getNode());

        Pointer notFound = docPtr.getPointerByID(context, "missingId");
        Assert.assertTrue(notFound instanceof NullPointer);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Pointer foundFromRoot = rootPtr.getPointerByID(context, "targetId");
        Assert.assertTrue(foundFromRoot instanceof DOMNodePointer);
        Assert.assertEquals(root, foundFromRoot.getNode());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = document.createElement("root");
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");

        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        document.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Attr attr1 = root.getAttributeNode("attr1");
        Attr attr2 = root.getAttributeNode("attr2");

        DOMNodePointer pAttr1 = new DOMNodePointer(rootPtr, attr1);
        DOMNodePointer pAttr2 = new DOMNodePointer(rootPtr, attr2);
        DOMNodePointer pChild1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(rootPtr, child2);

        Assert.assertEquals(0, rootPtr.compareChildNodePointers(pChild1, pChild1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pChild1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild1, pAttr1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(pChild1, pChild2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(pChild2, pChild1));

        int attrComp = rootPtr.compareChildNodePointers(pAttr1, pAttr2);
        Assert.assertTrue(attrComp == -1 || attrComp == 1);
        Assert.assertEquals(-attrComp, rootPtr.compareChildNodePointers(pAttr2, pAttr1));
    }

    @Test
    public void testIterators() {
        Element root = document.createElement("root");
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        NodeIterator childIter = rootPtr.childIterator(null, false, null);
        Assert.assertNotNull(childIter);

        NodeIterator attrIter = rootPtr.attributeIterator(new QName("test"));
        Assert.assertNotNull(attrIter);

        NodeIterator nsIter = rootPtr.namespaceIterator();
        Assert.assertNotNull(nsIter);

        NodePointer nsPtr = rootPtr.namespacePointer("xml");
        Assert.assertNotNull(nsPtr);
        Assert.assertEquals("xml", nsPtr.getName().getName());
    }
}
