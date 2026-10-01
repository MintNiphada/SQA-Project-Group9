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
    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    @Test
    public void testBasicProperties() {
        Element root = document.createElement("root");
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        Assert.assertEquals(root, pointer.getBaseValue());
        Assert.assertEquals(root, pointer.getImmediateNode());
        Assert.assertTrue(pointer.isActual());
        Assert.assertFalse(pointer.isCollection());
        Assert.assertEquals(1, pointer.getLength());
        Assert.assertTrue(pointer.isLeaf());

        root.appendChild(document.createElement("child"));
        Assert.assertFalse(pointer.isLeaf());

        Assert.assertEquals(System.identityHashCode(root), pointer.hashCode());
        Assert.assertTrue(pointer.equals(pointer));
        DOMNodePointer pointer2 = new DOMNodePointer(root, Locale.US);
        Assert.assertTrue(pointer.equals(pointer2));
        Assert.assertFalse(pointer.equals(new DOMNodePointer(document, Locale.US)));
        Assert.assertFalse(pointer.equals("not-a-pointer"));
        Assert.assertFalse(pointer.equals(null));
    }

    @Test
    public void testGetName() {
        Element element = document.createElementNS("http://example.com/ns", "ns:elem");
        DOMNodePointer ptr1 = new DOMNodePointer(element, Locale.US);
        QName name1 = ptr1.getName();
        Assert.assertEquals("ns", name1.getPrefix());
        Assert.assertEquals("elem", name1.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("my-target", "some data");
        DOMNodePointer ptr2 = new DOMNodePointer(pi, Locale.US);
        QName name2 = ptr2.getName();
        Assert.assertNull(name2.getPrefix());
        Assert.assertEquals("my-target", name2.getName());

        Text text = document.createTextNode("hello");
        DOMNodePointer ptr3 = new DOMNodePointer(text, Locale.US);
        QName name3 = ptr3.getName();
        Assert.assertNull(name3.getPrefix());
        Assert.assertNull(name3.getName());
    }

    @Test
    public void testGetPrefixAndLocalNameFallback() {
        Element rawElement = document.createElement("simple");
        Assert.assertNull(DOMNodePointer.getPrefix(rawElement));
        Assert.assertEquals("simple", DOMNodePointer.getLocalName(rawElement));

        Element rawColon = document.createElement("pfx:tag");
        Assert.assertEquals("pfx", DOMNodePointer.getPrefix(rawColon));
        Assert.assertEquals("tag", DOMNodePointer.getLocalName(rawColon));
    }

    @Test
    public void testTestNodeNull() {
        Element element = document.createElement("elem");
        Assert.assertTrue(DOMNodePointer.testNode(element, null));
        DOMNodePointer ptr = new DOMNodePointer(element, Locale.US);
        Assert.assertTrue(ptr.testNode(null));
    }

    @Test
    public void testTestNodeNodeNameTest() {
        Element elem = document.createElementNS("http://example.com/ns", "pfx:myElem");
        Text text = document.createTextNode("text");

        NodeNameTest nonElementTest = new NodeNameTest(new QName("pfx", "myElem"), "http://example.com/ns");
        Assert.assertFalse(DOMNodePointer.testNode(text, nonElementTest));

        NodeNameTest wildcardNoPrefix = new NodeNameTest(new QName(null, "*"));
        Assert.assertTrue(DOMNodePointer.testNode(elem, wildcardNoPrefix));

        NodeNameTest wildcardWithPrefixMatch = new NodeNameTest(new QName("pfx", "*"), "http://example.com/ns");
        Assert.assertTrue(DOMNodePointer.testNode(elem, wildcardWithPrefixMatch));

        NodeNameTest wildcardWithPrefixMismatch = new NodeNameTest(new QName("pfx", "*"), "http://other.com/ns");
        Assert.assertFalse(DOMNodePointer.testNode(elem, wildcardWithPrefixMismatch));

        NodeNameTest exactMatch = new NodeNameTest(new QName("pfx", "myElem"), "http://example.com/ns");
        Assert.assertTrue(DOMNodePointer.testNode(elem, exactMatch));

        NodeNameTest wrongLocalName = new NodeNameTest(new QName("pfx", "otherElem"), "http://example.com/ns");
        Assert.assertFalse(DOMNodePointer.testNode(elem, wrongLocalName));

        Element nonNsElem = document.createElement("item");
        NodeNameTest nonNsTest = new NodeNameTest(new QName(null, "item"), null);
        Assert.assertTrue(DOMNodePointer.testNode(nonNsElem, nonNsTest));

        Element prefixedNonNsElem = document.createElement("foo:item");
        NodeNameTest prefixedNonNsTest = new NodeNameTest(new QName("foo", "item"), null);
        Assert.assertTrue(DOMNodePointer.testNode(prefixedNonNsElem, prefixedNonNsTest));
    }

    @Test
    public void testTestNodeNodeTypeTest() {
        Element elem = document.createElement("elem");
        Text text = document.createTextNode("text");
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

        NodeTypeTest unknownTest = new NodeTypeTest(999);
        Assert.assertFalse(DOMNodePointer.testNode(elem, unknownTest));
    }

    @Test
    public void testTestNodeProcessingInstructionTest() {
        ProcessingInstruction pi = document.createProcessingInstruction("target1", "data");
        Element elem = document.createElement("elem");

        ProcessingInstructionTest piTestMatch = new ProcessingInstructionTest("target1");
        ProcessingInstructionTest piTestMismatch = new ProcessingInstructionTest("target2");

        Assert.assertTrue(DOMNodePointer.testNode(pi, piTestMatch));
        Assert.assertFalse(DOMNodePointer.testNode(pi, piTestMismatch));
        Assert.assertFalse(DOMNodePointer.testNode(elem, piTestMatch));

        NodeTest dummyTest = new NodeTest() {};
        Assert.assertFalse(DOMNodePointer.testNode(elem, dummyTest));
    }

    @Test
    public void testGetNamespaceURIAndDefault() {
        Element root = document.createElementNS("http://default.com", "root");
        root.setAttribute("xmlns", "http://default.com");
        root.setAttribute("xmlns:foo", "http://foo.com");
        document.appendChild(root);

        Element child = document.createElementNS("http://default.com", "child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        Assert.assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPointer.getNamespaceURI("xml"));
        Assert.assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPointer.getNamespaceURI("xmlns"));
        Assert.assertEquals("http://default.com", childPointer.getNamespaceURI(""));
        Assert.assertEquals("http://default.com", childPointer.getNamespaceURI((String) null));
        Assert.assertEquals("http://foo.com", childPointer.getNamespaceURI("foo"));
        Assert.assertNull(childPointer.getNamespaceURI("unknownPrefix"));
        Assert.assertNull(childPointer.getNamespaceURI("unknownPrefix"));

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("http://foo.com", docPointer.getNamespaceURI("foo"));
        Assert.assertEquals("http://default.com", docPointer.getDefaultNamespaceURI());

        Element noNsElem = document.createElement("plain");
        DOMNodePointer noNsPointer = new DOMNodePointer(noNsElem, Locale.US);
        Assert.assertNull(noNsPointer.getDefaultNamespaceURI());
        Assert.assertNull(DOMNodePointer.getNamespaceURI(noNsElem));
    }

    @Test
    public void testGetNamespaceResolverAndIterators() {
        Element root = document.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);

        NamespaceResolver resolver = ptr.getNamespaceResolver();
        Assert.assertNotNull(resolver);
        Assert.assertSame(resolver, ptr.getNamespaceResolver());

        NodeIterator childIt = ptr.childIterator(new NodeTypeTest(Compiler.NODE_TYPE_NODE), false, null);
        Assert.assertNotNull(childIt);
        Assert.assertTrue(childIt instanceof DOMNodeIterator);

        NodeIterator attrIt = ptr.attributeIterator(new QName("attr"));
        Assert.assertNotNull(attrIt);
        Assert.assertTrue(attrIt instanceof DOMAttributeIterator);

        NodeIterator nsIt = ptr.namespaceIterator();
        Assert.assertNotNull(nsIt);
        Assert.assertTrue(nsIt instanceof DOMNamespaceIterator);

        NodePointer nsPtr = ptr.namespacePointer("foo");
        Assert.assertNotNull(nsPtr);
        Assert.assertTrue(nsPtr instanceof NamespacePointer);
    }

    @Test
    public void testLanguageHandling() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        document.appendChild(root);

        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        Assert.assertTrue(childPtr.isLanguage("en"));
        Assert.assertTrue(childPtr.isLanguage("EN-US"));
        Assert.assertFalse(childPtr.isLanguage("fr"));

        Element noLang = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.US);
        Assert.assertTrue(noLangPtr.isLanguage("en"));
    }

    @Test
    public void testGetValue() {
        Comment comment = document.createComment(" hello comment ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        Assert.assertEquals("hello comment", commentPtr.getValue());

        Element elem = document.createElement("elem");
        Text t1 = document.createTextNode(" Hello ");
        elem.appendChild(t1);
        CDATASection cdata = document.createCDATASection(" World ");
        elem.appendChild(cdata);
        ProcessingInstruction pi = document.createProcessingInstruction("target", " pi-data ");
        elem.appendChild(pi);
        Comment c = document.createComment("ignored");
        elem.appendChild(c);

        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);
        Assert.assertEquals("HelloWorldpi-data", elemPtr.getValue());

        elem.setAttribute("xml:space", "preserve");
        Assert.assertEquals(" Hello  World  pi-data ", elemPtr.getValue());
    }

    @Test
    public void testSetValueTextNode() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        textPtr.setValue("updated");
        Assert.assertEquals("updated", text.getNodeValue());

        textPtr.setValue("");
        Assert.assertEquals(0, root.getChildNodes().getLength());
    }

    @Test
    public void testSetValueElementNode() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        rootPtr.setValue("Simple Text");
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals("Simple Text", root.getFirstChild().getNodeValue());

        Element sourceElem = document.createElement("source");
        sourceElem.appendChild(document.createElement("c1"));
        sourceElem.appendChild(document.createElement("c2"));

        rootPtr.setValue(sourceElem);
        Assert.assertEquals(2, root.getChildNodes().getLength());
        Assert.assertEquals("c1", root.getFirstChild().getNodeName());

        Comment comment = document.createComment("my-comment");
        rootPtr.setValue(comment);
        Assert.assertEquals(1, root.getChildNodes().getLength());
        Assert.assertEquals(Node.COMMENT_NODE, root.getFirstChild().getNodeType());

        rootPtr.setValue(null);
        Assert.assertEquals(0, root.getChildNodes().getLength());
    }

    @Test
    public void testCreateAttribute() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:foo", "http://foo.com");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer attrPtr1 = ptr.createAttribute(context, new QName("simple"));
        Assert.assertNotNull(attrPtr1);
        Assert.assertTrue(root.hasAttribute("simple"));

        NodePointer attrPtr2 = ptr.createAttribute(context, new QName("foo", "bar"));
        Assert.assertNotNull(attrPtr2);
        Assert.assertTrue(root.hasAttributeNS("http://foo.com", "bar"));

        try {
            ptr.createAttribute(context, new QName("unknown", "bar"));
            Assert.fail("Should throw JXPathException for unknown namespace prefix");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown namespace prefix"));
        }

        Text textNode = document.createTextNode("hello");
        DOMNodePointer textPtr = new DOMNodePointer(textNode, Locale.US);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            Assert.fail("createAttribute on non-element should fail via super.createAttribute");
        } catch (JXPathException expected) {
        }
    }

    @Test
    public void testCreateChild() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        try {
            rootPtr.createChild(context, new QName("child"), 0);
            Assert.fail("Should fail when no factory is registered");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Factory is not set"));
        }

        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                if (node instanceof Element && "child".equals(name)) {
                    Element child = ((Element) node).getOwnerDocument().createElement("child");
                    ((Element) node).appendChild(child);
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = rootPtr.createChild(context, new QName("child"), NodePointer.WHOLE_COLLECTION);
        Assert.assertNotNull(childPtr);
        Assert.assertEquals("child", childPtr.getName().getName());

        NodePointer childWithValue = rootPtr.createChild(context, new QName("child"), 1, "testValue");
        Assert.assertNotNull(childWithValue);
        Assert.assertEquals("testValue", childWithValue.getValue());

        try {
            rootPtr.createChild(context, new QName("unsupported"), 0);
            Assert.fail("Should fail when factory cannot create child");
        } catch (JXPathAbstractFactoryException e) {
            Assert.assertTrue(e.getMessage().contains("Factory could not create a child node"));
        }
    }

    @Test
    public void testRemove() {
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        childPtr.remove();
        Assert.assertEquals(0, root.getChildNodes().getLength());

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        try {
            docPtr.remove();
            Assert.fail("Cannot remove root node without parent");
        } catch (JXPathException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot remove root DOM node"));
        }
    }

    @Test
    public void testAsPath() {
        DOMNodePointer idPtr = new DOMNodePointer(document, Locale.US, "my'id\"test");
        Assert.assertEquals("id('my&apos;id&quot;test')", idPtr.asPath());

        DOMNodePointer idPtrClean = new DOMNodePointer(document, Locale.US, "simpleId");
        Assert.assertEquals("id('simpleId')", idPtrClean.asPath());

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        Assert.assertEquals("", docPtr.asPath());

        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);
        Assert.assertEquals("", rootPtr.asPath());

        Element e1 = document.createElement("elem");
        Element e2 = document.createElement("elem");
        root.appendChild(e1);
        root.appendChild(e2);

        DOMNodePointer p1 = new DOMNodePointer(rootPtr, e1);
        DOMNodePointer p2 = new DOMNodePointer(rootPtr, e2);
        Assert.assertEquals("/elem[1]", p1.asPath());
        Assert.assertEquals("/elem[2]", p2.asPath());

        Element nsElem = document.createElementNS("http://example.com/ns", "ex:item");
        root.setAttribute("xmlns:ex", "http://example.com/ns");
        root.appendChild(nsElem);
        DOMNodePointer nsPtr = new DOMNodePointer(rootPtr, nsElem);
        Assert.assertEquals("/ex:item[1]", nsPtr.asPath());

        Element unmappedNsElem = document.createElementNS("http://unknown.com/ns", "item");
        root.appendChild(unmappedNsElem);
        DOMNodePointer unmappedNsPtr = new DOMNodePointer(rootPtr, unmappedNsElem);
        Assert.assertEquals("/node()[4]", unmappedNsPtr.asPath());

        Text text = document.createTextNode("hello");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, text);
        Assert.assertEquals("/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        root.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, pi);
        Assert.assertEquals("/processing-instruction('target')[1]", piPtr.asPath());
    }

    @Test
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        root.setAttribute("id", "elem1");
        document.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer p1 = docPtr.getPointerByID(context, "elem1");
        Assert.assertNotNull(p1);

        Pointer p2 = docPtr.getPointerByID(context, "nonExistent");
        Assert.assertTrue(p2 instanceof NullPointer);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);
        Pointer p3 = rootPtr.getPointerByID(context, "nonExistent");
        Assert.assertTrue(p3 instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = document.createElement("root");
        root.setAttribute("a1", "v1");
        root.setAttribute("a2", "v2");

        Element child1 = document.createElement("c1");
        Element child2 = document.createElement("c2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Attr a1 = root.getAttributeNode("a1");
        Attr a2 = root.getAttributeNode("a2");
        DOMNodePointer ptrA1 = new DOMNodePointer(rootPtr, a1);
        DOMNodePointer ptrA2 = new DOMNodePointer(rootPtr, a2);
        DOMNodePointer ptrC1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer ptrC2 = new DOMNodePointer(rootPtr, child2);

        Assert.assertEquals(0, rootPtr.compareChildNodePointers(ptrA1, ptrA1));
        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrC1));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrC1, ptrA1));

        int attrOrder = rootPtr.compareChildNodePointers(ptrA1, ptrA2);
        int attrOrderReverse = rootPtr.compareChildNodePointers(ptrA2, ptrA1);
        Assert.assertTrue(attrOrder != 0);
        Assert.assertEquals(-attrOrder, attrOrderReverse);

        Assert.assertEquals(-1, rootPtr.compareChildNodePointers(ptrC1, ptrC2));
        Assert.assertEquals(1, rootPtr.compareChildNodePointers(ptrC2, ptrC1));

        Element foreignChild = document.createElement("foreign");
        DOMNodePointer foreignPtr = new DOMNodePointer(rootPtr, foreignChild);
        Assert.assertEquals(0, rootPtr.compareChildNodePointers(foreignPtr, ptrC1));
    }
}
